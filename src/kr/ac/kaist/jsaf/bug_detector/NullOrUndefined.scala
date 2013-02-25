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
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class NullOrUndefined(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  private val msg_null  = "Object '%s' is null."
  private val msg_undef = "Object '%s' is undefined."
  private val msg_both  = "Object '%s' is null or undefined."
  
  private var count = 0
  override def printStat = System.out.println("# NullOrUndefined: " + count)

  private var recent: PValue = PValueBot
  private val definite_flag = false

  /* Store, DeleteProp, InternalCall: toObject */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1

    if (heap <= HeapBot) Unit
    else {
      inst match {
        case CFGInternalCall(_, info, lhs, fun, arguments, loc) =>
          (fun.toString, arguments, loc)  match {
            case ("<>Global<>toObject", List(expr), Some(a_new)) =>
              val pv = SE.V(expr, state._1, state._2)._1._1
              if (definite_flag && (pv._3 </ BoolBot || pv._4 </ NumBot || pv._5 </ StrBot))
                Unit  // maybe undef or null
              else    // definitely undef or null
        	    (pv._1, pv._2) match {
	              case (UndefTop, NullTop) =>
	                count = count + 1
	                bugs.addMessage(info.getSpan, "error", msg_both.format(expr.toString))
	              case (_, NullTop) =>
	                count = count + 1
	                bugs.addMessage(info.getSpan, "error", msg_null.format(expr.toString))
	              case (UndefTop, _) => 
	                count = count + 1
	                bugs.addMessage(info.getSpan, "error", msg_undef.format(expr.toString))
	              case _ => Unit
	            }
            case _ => Unit
          }
        case _ => Unit
      }
    }
  }
  
}
