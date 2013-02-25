/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}

class UnusedFunctions(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
  
  private val msg         = "Function '%s' is not reachable."

  /*
  * funcCount             : total number of functions in CFG 
  * fidSet                : set of all function ids (to be checked) 
  * defFunctions          : set of defined functions
  * useFunctions          : set of used functions
  */
  private val funcCount     = cfg.getFuncCount
  private var fidSet        = (0 to (funcCount-1) toSet) filterNot (typing.builtinFset contains)
  private var defFunctions  = Set[Int]()
  private var useFunctions  = Set[Int]()

  /*
  * unusedFunctions    : list of unused function ids 
  * checkedFunctions   : number of checked functions
  */
  private var unusedFunctions  = List[Int]()
  private var checkedFunctions = 0  

  /* 
  * internalCallMap [fid] => List[objectName1, objectName2, ...] 
  * : Stores fid with objects that own this function

  private var internalCallMap: Map[Int, List(String)] = Map()
  */

  /* return used functions' fid set */
  def getUsedFIdSet: Set[Int] = useFunctions

  /* print statistics */
  override def printStat = System.out.println("# UnusedFunctions: " + unusedFunctions.size)

  /* bug check */
  override def checkFinal(): Unit = {
    for (fid <- ((fidSet toList) filterNot (useFunctions.toList contains))) {
      checkedFunctions += 1
      if (typing.getStateAtFunctionEntry(fid).isEmpty) {
        bugs.addMessage(cfg.getFuncInfo(fid).getSpan, "warning", msg.format(prFuncName(cfg.getFuncName(fid))))
        unusedFunctions = unusedFunctions :+ fid
      }
    }
    Unit
  }

  /* Call, Construct, FunExpr */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1

    if (heap <= HeapBot) Unit
    else {
      inst match {
        case CFGCall(_, info, fun, _, _, _)             => funUse(inst, fun, cstate)
        case CFGConstruct(_, info, cons, _, _, _)       => funUse(inst, cons, cstate)
        case CFGFunExpr(_, _, lhs, name, fid, _, _, _)  => funDef(inst, cstate)
        case _ => Unit
      }
    }
  }
  
  /* store function definitions */
  def funDef(inst: CFGInst, cstate: CState): Unit = {
    val state = typing.mergeState(cstate)
    val heap = state._1
    val context = state._2
    if (heap <= HeapBot) Unit
    else {
      inst.getInfo match {
        case Some(info) =>
          inst match {
            case CFGFunExpr(_,_,_,_,fid,_,_,_) => defFunctions += fid
            case _ => print("BugDetector, unusedFunctions.funDef() is called only by CFGFunExpr.\n")
          }
        case _ => print("BugDetector, unusedFunctions. Info is missing.\n")
      }
      Unit
    }
  }

  /* check function usages and delete fids from the fidSet not to be checked in check() */
  def funUse(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (heap <= HeapBot) Unit
    else {
      inst.getInfo match {
        case Some(info) =>
          expr match {
            case CFGVarRef(_, id) =>
              val (v, es) = Helper.Lookup(heap, id) 
              if (es.isEmpty && v._1 <= PValueBot) {
                val x = id.toString
                id.getVarKind match {
                  case GlobalVar        => setObjectReferences(GlobalLoc, x)
                  case PureLocalVar     => setObjectReferences(SinglePureLocalLoc, x)
                  case CapturedVar      => setObjectReferences(SinglePureLocalLoc, x)
                  case CapturedCatchVar => setObjectReferences(CollapsedLoc, x)
                }
                /*
                if (!v._2.isEmpty && (x contains ("<>arguments<>"))) {
                  v._2.foreach((l: Loc) => 
                    for(i <- 0 to (heap(l)("length")._1._1._1._1._4.toString toInt) - 1) {
                      setObjectReferences(l, i toString)
              })} */
              } 
            case _ => Unit
          }
        case _ => print("BugDetector, unusedFunctions. Info is missing.\n")
      }
    }

    def setObjectReferences(loc: Loc, x: String): Unit = {
      heap(loc)(x)._1._1._1._2.foreach((l: Loc) => heap(l)("@function")._1._3.foreach((fid) => useFunctions += fid))
      Unit
    }
  }

  /* Return the number of checked functions */
  def getCheckedFunctions(): Int = checkedFunctions
  
  /* Return the list of unused function ids */
  def getunusedFunctions(): List[Int] = unusedFunctions

  /* Get function name */
  def prFuncName(name: String) =
    if (NU.isFunExprName(name)) "anonymous_function" else name
}
