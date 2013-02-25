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
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class WrongThisType(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  /* bug message */  
  private val msg   = "Native function '%s' is called when its this value is not of the expected object type."
  private var count = 0
  private val checkMap: Map[String, String] = Map("String.prototype.toString" -> "String", 
    "String.prototype.valueOf" -> "String", "Boolean.prototype.toString" -> "Boolean", 
    "Boolean.prototype.valueOf" -> "Boolean", "Number.prototype.toString" -> "Number", 
    "Number.prototype.valueOf" -> "Number")
      

  override def printStat = System.out.println("# WrongThisType: " + count) 
    
  /* 
   * CFGThis -> check String  (15.5.4.2, 15.5.4.3), 
   *                  Boolean (15.6.4.2, 15.6.4.3),
   *                  Number  (15.7.4.2, 15.7.4.4).
  */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (state._1 <= HeapBot)  Unit
    else {
      inst match {
        case CFGCall(_, info, fun, thisArg, _, _) => 
          val obj_val = SE.V(fun, heap, context)._1
          obj_val._2.foreach((loc) =>
            heap(loc)("@function")._1._3.foreach((fid) =>
              typing.builtinFset.get(fid) match {
                case Some(builtinName) =>
                  if (checkMap contains builtinName) {
                    val this_locs = Helper.getThis(heap, SE.V(thisArg, heap, context)._1)
                    this_locs.foreach((loc) => if (heap(loc)("@class")._1._2._1._5 != checkMap(builtinName)) 
                      bugs.addMessage(info.getSpan, "error", msg.format(builtinName))
                    )
                  }; Unit
                case None => Unit
              }
            )
          )
        case _ => Unit
      }
    }
  }
}
