/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import _root_.java.math.BigInteger
import _root_.java.lang.{Integer => JInteger}
import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.CallContext._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.bug_detector.StateManager
import kr.ac.kaist.jsaf.concolic.{ConstraintForm, FunctionInfo, IRGenerator}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{IRFactory => IF, NodeFactory => NF, NodeRelation => NR, NodeUtil => NU}
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Maps._
import scala.collection.mutable.HashMap
import scala.util.Random

/* Calculates code coverage. */
class Coverage() {
  var total = 0
  var executed = 0
  var execSet = scala.collection.immutable.Set[Long](0);

  // To print condition expressions
  var conditions = List[IRNode]()
  def printCondition(cond: IRNode) = 
    if (!conditions.contains(cond)) {
      conditions = conditions:+cond
      val ircode = new JSIRUnparser(cond).doit
      System.out.println(ircode)
    }

  // For concolic test
  var inputIR: Option[IRStmt] = None
  var input = List[Int]()
  var inum = 0
  var constraints = List[ConstraintForm]()
  var target:String = null
  var isFirst: Boolean = true

  // For analysis
  var cfg: CFG = null
  var typing: TypingInterface = null
  var semantics: Semantics = null
  var stateManager: StateManager = null
  var functions: HashMap[String, FunctionInfo] = HashMap[String,FunctionInfo]()
  functions.put("<>Concolic<>Main", new FunctionInfo)

  def toInt(n: JInteger):Int = n.intValue()
  def setInput(result: JList[JInteger]) = { 
    var tmp = map(result, toInt)
    input = toList(tmp)
  }
  def setInputNumber(n: Int) = inum = n
  // TODO: target environment setting
  // TODO: function call setting
  def setupCall():Option[IRStmt] = {
    if (target == null) return None
    for (k <- NR.ast2irMap.keySet) {
      k match {
        case SFunDecl(info, f@SFunctional(fds, vds, body, name, params), strict) =>
          if (name.getText == target) {
            val dummySpan = IF.dummySpan("forConcolic")
            val dummyInfo = NF.makeSpanInfoComment(dummySpan)
            val fun = new FunExpr(dummyInfo, f)
            var args = List[Expr]()
            var env = List[(String, IRId)]()
            setInputNumber(params.length)
            for (i <- 0 until params.length)
              if (i < input.length) {
                args = args:+NF.makeIntLiteral(dummySpan, new BigInteger(input(i).toString)) 
              }
              else
                args = args:+NF.makeIntLiteral(dummySpan, new BigInteger(new Random().nextInt(10).toString)) 
            val funapp = new FunApp(dummyInfo, fun, args)
            val res = IF.makeTId(funapp, dummySpan, NU.ignoreName)
            
            return Some(IRGenerator.funAppToIR(funapp, env, res))
          }
        case _ =>
      }
    }
    return None
  }

  def getConstraints:JList[ConstraintForm] = toJavaList(constraints)

  def continue:Boolean = {isFirst = false; constraints.nonEmpty}
  def existCandidate = functions.filter(x => x._2.isCandidate).nonEmpty
  def removeTarget = {
    functions.get(target) match {
      case Some(info) => info.done
      case None => println("Target should be function type")
    }
    if (functions.nonEmpty) {
      var filters = functions.filter(x => x._2.isCandidate)
      if (filters.nonEmpty) {
        filters.head._2.targeting
        target = filters.head._1
      }
      else
        target = null
    }
    isFirst = true
  }
  def setProcessing(fun: String) = functions.get(fun) match { case Some(f) => f.processing; case None => }
  def setUnprocessing(fun: String) = functions.get(fun) match { case Some(f) => f.unprocessing; case None => }
  def checkTarget(fun: String) = functions.get(fun) match { case Some(f) => f.isTarget; case None => false }
  def checkProcessing(fun: String) = functions.get(fun) match { case Some(f) => f.isProcess; case None => false }

  // using static analysis, store function information
  def updateFunction = {
    for (k <- NodeRelation.cfg2irMap.keySet) {
      k match {
        case inst@CFGCall(iid, info, fun, thisArg, arguments, addr) =>
          val cfgNode = cfg.findEnclosingNode(inst)
          val cstate = stateManager.getInputCState(cfgNode, inst.getInstId, _MOST_SENSITIVE)
          for ((callContext, state) <- cstate) {
            val controlPoint: ControlPoint = (cfgNode, callContext)  
            semantics.getIPSucc(controlPoint) match {
              case Some(succMap) => 
                for ((succCP, (succContext, succObj)) <- succMap) {
                  val fid = succCP._1._1
                  if (!functions.contains(cfg.getFuncName(fid))) {
                    println("Coverage: updateFunction: "+cfg.getFuncName(fid))
                    var finfo = new FunctionInfo
                    val argvars = cfg.getArgVars(fid)
                    for ((callContext, state) <- stateManager.getOutputCState(succCP._1, inst.getInstId, _MOST_SENSITIVE)) {
                      val arglset = state._1(SinglePureLocalLoc)(cfg.getArgumentsName(fid))._1._1._1._2
                      var i = 0
                      val h_n = argvars.foldLeft(state._1)((hh, x) => {
                        val v_i = arglset.foldLeft(ValueBot)((vv, argloc) => {
                          vv + Helper.Proto(hh, argloc, AbsString.alpha(i.toString))
                        })
            
                        val v_i_types = v_i.typeKinds
                        if (finfo.isNewType(i, v_i_types)) {
                          if (v_i_types.contains("Object")) 
                            computePropertyList(state, v_i._2.toSet) match { case Some(props) => finfo.storeObjectProperties(i, props.foldLeft[List[String]](List())((list, p) => p._1::list)); case None => }                        
 
                          finfo.storeParameter(i, v_i.typeKinds)
                          finfo.setCandidate
                        }
                        i += 1
                        Helper.CreateMutableBinding(hh, x, v_i)
                      })
                    }
                    functions.put(cfg.getFuncName(fid), finfo)
                  }
                }
              case None =>
            }
          }
        case _ =>
      }
    }
  }

  def computePropertyList(state: State, lset: Set[Loc]): Option[List[(String, Absent)]]  = {
    if (state <= StateBot) 
      None
    else {
      val h = state._1
      val obj = 
        if (lset.size == 0)
          ObjBot
        else if (lset.size == 1)
          h(lset.head)
        else
          lset.tail.foldLeft(h(lset.head))((o, l) => o + h(l))
      val props = obj.getProps.foldLeft[List[(String, Absent)]](List())((list, p) => (p, obj(p)._2)::list)
      /*for ((p, x) <- props) {
        println(p)
        println(obj(p)._1._2.typeKinds)
      }*/
      Some(props)
    }
  }

  /*def storeIR(id: IRId) = functions.get(id.getUniqueName) match { case Some(f) => f.storeIR(id); case None => } 

  def getIR(fun: String): IRId = functions.get(fun) match { case Some(f) => f.irId; case None => }*/
}
