/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import java.text.Format
import _root_.java.util.{HashMap=>JHashMap}
import _root_.java.util.{List => JList}

class BugDetector(cfg: CFG, typing: TypingInterface, fileMap: JHashMap[String, String],
                  quiet: Boolean, shadowingErrors: JList[StaticError]) {

  val messages = new BugMessage(fileMap)
  val semantics = new Semantics(cfg, Worklist.computes(cfg, quiet))
  val callgraph = typing.computeCallGraph

  /* Bug Rules */
  var rules = List[BugRule](
    // Errors
    new AbsentVariableRead(cfg, typing, messages),
    new BinaryOpIn(cfg, typing, messages),
    new BinaryOpInstanceOf(cfg, typing, messages),
    new BuiltinArgumentSize(cfg, typing, messages),
    new BuiltinWrongType(cfg, typing, messages),
    new CallNonFunction(cfg, typing, messages),
    new DefaultValue(cfg, typing, messages),
    new NonConstructorCall(cfg, typing, messages),
    new NullOrUndefined(cfg, typing, messages),
    new WrongThisType(cfg, typing, messages),
    // Warnings
    new CallNewFunction(cfg, typing, callgraph, messages),
    new ConditionalBranch(cfg, typing, messages),
    new PrimitiveToObject(cfg, typing, messages),
    new ReadAbsentProperty(cfg, typing, messages),
    new ShadowingVariable(cfg, typing, messages, toList(shadowingErrors)),
    new ThisGlobal(cfg, typing, messages),
    new UndefToNumber(cfg, typing, messages),
    new UnreachableCode(cfg, typing, messages),
    new UnusedFunctions(cfg, typing, messages),
    new UnusedVarProp(cfg, typing, messages, semantics),
    new VaryingArgumentsType(cfg, typing, messages)
  )


  def detectBug() = {
    messages.setStartTime(System.nanoTime)

    /* collect bugs while traversing CFG */
    traverse

    /* collect bugs from the final state of program */
    rules.foreach((rule: BugRule) => rule.checkFinal)

    messages.setEndTime(System.nanoTime)

    printBugReport
    if (!quiet) printStat
  }

  def printBugReport() = {
    messages.printMessage
    messages.printTotal
  }

  def printStat() = {
    System.out.println("\n* Bug detector statistics *")
    messages.printTime
    rules.foreach((rule: BugRule) => rule.printStat)
  }

  def traverse() = {
    // Traverse all nodes in CFG.
    cfg.getNodes.foreach((node) => {
      typing.readTable(node) match {
        case None => Unit
        case Some(map) => 
          C(node, map, cfg.getCmd(node))
      }
    })

    def C(node: Node, map: CState, cmd: Cmd): Unit = {
      cmd match {
        case Entry => Unit
        case Exit => Unit
        case ExitExc => Unit
        case Block(insts) => {
          insts.foldLeft[CState](map)((stateMap, inst) => {
            I(node, inst, stateMap)
            // compute next normal states and exception state using semantics
            val (cs, es) = stateMap.foldLeft[(CState,State)]((HashMap(),StateBot))((ms, kv) => {
              val (s,es) = semantics.I((node, kv._1), inst, kv._2._1, kv._2._2, HeapBot, ContextBot)
              (ms._1 + (kv._1 -> State(s._1, s._2)), ms._2 + State(es._1, es._2))
            })
            cs
          })
          Unit
        }
      }
    }

    def I(node: Node, inst: CFGInst, stateMap: CState) = {
      rules.foreach((rule: BugRule) => rule.checkInst(inst, stateMap))
      inst match {
        case CFGAlloc(_, _ , x, e, a_new) => {
          e match {
            case None => Unit
            case Some(expr) => V(node, inst, expr, stateMap)
        }}
        case CFGAllocArray(_, _, x, n, a_new) => Unit
        case CFGAllocArg(_, _, x, n, a_new) => Unit
        case CFGAssert(_, info, expr, _) => V(node, inst, expr, stateMap)
        case CFGBuiltinCall(_, fun, args, addr1, addr2, addr3, addr4) => Unit
        case CFGCall(_, _, fun, base, arguments, a_new) => {
          V(node, inst, fun, stateMap)
          V(node, inst, base, stateMap)
          V(node, inst, arguments, stateMap)
        }
        case CFGConstruct(_, _, cons, base, arguments, a_new) => {
          V(node, inst, cons, stateMap)
          V(node, inst, base, stateMap)
          V(node, inst, arguments, stateMap)
        }
        case CFGCatch(_, _, name) => Unit
        case CFGDelete(_, _, lhs, expr) => V(node, inst, expr, stateMap)
        case CFGDeleteProp(_, _, lhs, obj, index) => {
          V(node, inst, obj, stateMap)
          V(node, inst, index, stateMap)
        }
        case CFGDomApiCall(_, fun, args, _, _, _, _) => Unit
        case CFGExprStmt(_, _,x, expr) => V(node, inst, expr, stateMap)
        case CFGFunExpr(_, _, lhs, name, fid, a_new1, a_new2, a_new3) => Unit
        case CFGInternalCall(_, _, lhs, fun, arguments, loc) => {
          (fun.toString, arguments, loc)  match {
            case ("<>Global<>toObject", List(expr), Some(a_new)) =>
              V(node, inst, expr, stateMap)
            case ("<>Global<>toNumber", List(expr), None) =>
              V(node, inst, expr, stateMap)
            case ("<>Global<>isObject", List(expr), None) =>
              V(node, inst, expr, stateMap)
            case ("<>Global<>getBase", List(expr), None) =>
              V(node, inst, expr, stateMap)
            case ("<>Global<>iteratorInit", List(expr), None) =>
              V(node, inst, expr, stateMap)
            case ("<>Global<>iteratorHasNext", List(expr_2, expr_3), None) =>
              V(node, inst, expr_2, stateMap)
              V(node, inst, expr_3, stateMap)
            case ("<>Global<>iteratorNext", List(expr_2, expr_3), None) =>
              V(node, inst, expr_2, stateMap)
              V(node, inst, expr_3, stateMap)
            case _ => Unit
        }}
        case CFGReturn(_, _, expr) => {
          expr match {
            case None => Unit
            case Some(e) => V(node, inst, e, stateMap)
        }}
        case CFGStore(_, _, obj, index, rhs) => {
          V(node, inst, obj, stateMap)
          V(node, inst, index, stateMap)
          V(node, inst, rhs, stateMap)
        }
        case CFGThrow(_, _, expr) => V(node, inst, expr, stateMap)
        case _ => Unit
      }
    }

    def V(node: Node, inst: CFGInst, expr: CFGExpr, stateMap: CState): Unit = {
      rules.foreach((rule: BugRule) => rule.checkExpr(inst, expr, stateMap))
      expr match {
        case CFGBin(info, first, op, second) => {
          V(node, inst, first, stateMap)
          V(node, inst, second, stateMap)
        }
        case CFGLoad(info, obj, index) => {
          V(node, inst, obj, stateMap)
          V(node, inst, index, stateMap)
        }
        case CFGThis(info) => Unit
        case CFGUn(info, op, first) => V(node, inst, first, stateMap)
        case CFGVarRef(info, id) => Unit
        case _ => Unit
      }
    }
  }
}

