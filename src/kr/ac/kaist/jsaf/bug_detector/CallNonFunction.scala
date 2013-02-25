/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
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

class CallNonFunction(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
  
  private val msg = "Non-function is called as a function."
  
  private var count = 0
  override def printStat = System.out.println("# CallNonFunction: " + count)


  /* Call, Construct */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2 

    if (heap <= HeapBot)
      Unit
    else {
      inst match {
        case CFGCall(_, info, fun, _, _, _) =>
          val lset    = SE.V(fun, heap, context)._1._2
          val lset_f  = lset.filter((l) => BoolFalse <= Helper.IsCallable(heap, l))
          if (lset_f.size > 0) {
            count = count + 1
            bugs.addMessage(info.getSpan, "error", msg)
          }
        case _ => Unit
      }
    }
  }
}
