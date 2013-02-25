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

class ConditionalBranch(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  /* bug message */
  private val tmsg = "The conditional expression is always true."
  private val fmsg = "The conditional expression is always false."
  private val definite_only = true
  private var count = 0

  override def printStat = System.out.println("# ConditionalBranch: " + count)

  /* CFGAssert -> expr check */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (heap <= HeapBot) Unit
    else {
      inst match {
        case CFGAssert(_, info, expr, isOriginalCondExpr) =>
          val epv = SE.V(expr, heap, context)._1._1
          // if -> Maybe, else -> definite
          if (!definite_only && (epv._1 </ UndefBot || epv._2 </ NullBot || epv._4 </ NumBot || epv._5 </ StrBot)) Unit
          else if (isOriginalCondExpr && info.isFromSource) {
            epv._3 match {
              case BoolTrue => addNewBug(info, true)
              case BoolFalse => addNewBug(info, false)
              case _ => Unit
            }
          } else Unit
        case _ => Unit
      }
    }
  }

  def addNewBug(info: Info, kind: Boolean): Unit = {
    count = count + 1
    if (kind) bugs.addMessage(info.getSpan, "warning", tmsg)
    else bugs.addMessage(info.getSpan, "warning", fmsg)
    Unit
  }
}
