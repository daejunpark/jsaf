/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import _root_.java.lang.{Integer => JInteger}
import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.CallContext._
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.bug_detector.StateManager
import kr.ac.kaist.jsaf.concolic.ConstraintForm
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeRelation => NR}
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Maps._
import scala.collection.mutable.HashMap

/* Calculates code coverage. */
class Coverage() {
  var total = 0
  var executed = 0
  var execSet = scala.collection.immutable.Set[Long](0);

  // For concolic test
  var input = List[Int]()
  var inum = 0
  var constraints = List[ConstraintForm]()
  var functions = List[String]()
  var targetFunc = ""
  var coveredFunc = ""

  // For analysis
  var cfg: CFG = null
  var typing: TypingInterface = null
  var semantics: Semantics = null
  var stateManager: StateManager = null

  def toInt(n: JInteger):Int = 
    n.intValue()

  def setInput(result: JList[JInteger]) = { 
    var tmp = map(result, toInt)
    input = toList(tmp)
  }

  def getConstraints:JList[ConstraintForm] = 
    toJavaList(constraints)
    
  def continue() = constraints.nonEmpty

  def existCandidate() = functions.nonEmpty

  def removeTarget() = {
    coveredFunc = functions(0)
    functions = functions diff List(coveredFunc)
    if (functions.nonEmpty)
        targetFunc = functions(0)
  }

  // using static analysis, store function information
  def storeFuncInfo(node: Any): Unit = node match {
    case f@SIRCall(info, lhs, fun, thisB, args) =>
      NR.ir2cfgMap.get(f) match {
        case Some(cfgList) => 
          for (cfgInst <- cfgList) {
            cfgInst match {
              case inst@CFGCall(iid, info, fun, thisArg, arguments, addr) =>
                println("StoreFuncInfo => IRCall[" + f.getUID +']' + NR.cfgToString(cfgInst))
                val cfgNode = cfg.findEnclosingNode(inst)
                val cstate = stateManager.getInputCState(cfgNode, inst.getInstId, _MOST_SENSITIVE)
                for ((callContext, state) <- cstate) {
                  val argLocSet = SE.V(arguments, state.heap, state.context)._1.locset
                  for (argLoc <- argLocSet) {
                    println("* for argument loc #" + argLoc)
                    /*for (i <- 0 until args.length) {
                      val argObj = state.heap(argLoc)
                      argObj.map.get(i.toString) match {
                        case Some((propValue, _)) =>
                          println(" [" + i +"] = " + propValue.objval.value)
                        case None => println(" [" + i + "] = ")
                      }
                    }*/
                  }
                }
              case _ =>
            }
          }
        case None =>
      }
  }
}
