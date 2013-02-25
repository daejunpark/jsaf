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

class AbsentVariableRead(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
  
  private val msg = "Reading absent variable '%s'."
    
  private var count = 0
  override def printStat = System.out.println("# AbsentVariableRead: " + count) 
    
  /* VarRef */
  override def checkExpr(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state = typing.mergeState(cstate)
    if (state._1 <= HeapBot)
      Unit
    else {
      expr match {
        case CFGVarRef(info, id) => {
          val (_, es) = Helper.Lookup(state._1, id)
          if (!es.isEmpty) {
            bugs.addMessage(info.getSpan, "error", msg.format(id.getText))
            count = count + 1
          }
          else
            Unit
        }
        case _ => Unit
      }
      
    }
  }
}
