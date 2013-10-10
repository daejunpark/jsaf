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
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.bug_detector.StateManager
import kr.ac.kaist.jsaf.concolic.{ConstraintForm, FunctionInfo}
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
  var target:String = null

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
  def getConstraints:JList[ConstraintForm] = toJavaList(constraints)

  def continue = constraints.nonEmpty
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
                    var finfo = new FunctionInfo
                    val argvars = cfg.getArgVars(fid)
                    for ((callContext, state) <- stateManager.getOutputCState(succCP._1, inst.getInstId, _MOST_SENSITIVE)) {
                      val arglset = state._1(SinglePureLocalLoc)(cfg.getArgumentsName(fid))._1._1._1._2
                      var i = 0
                      val h_n = argvars.foldLeft(state._1)((hh, x) => {
                        val v_i = arglset.foldLeft(ValueBot)((vv, argloc) => {
                          vv + Helper.Proto(hh, argloc, AbsString.alpha(i.toString))
                        })
                        finfo.storeParam(i, v_i.typeKinds)
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

  /*def storeIR(id: IRId) = functions.get(id.getUniqueName) match { case Some(f) => f.storeIR(id); case None => } 

  def getIR(fun: String): IRId = functions.get(fun) match { case Some(f) => f.irId; case None => }*/
}
