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

class ThisGlobal(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
  
  private val msg_may = "'this' may refers global object."
  private val msg_def = "'this' refers global object."
  
  private var count = 0
  override def printStat = System.out.println("# ThisGlobal: " + count)
    
  private val definite_flag = false
  
  /* This */
  override def checkExpr(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    /* state */
    val state = typing.mergeState(cstate)
    
    if (state._1 <= HeapBot)
      Unit
    else {
      expr match {
        case CFGThis(info) => {
          /* current function */
          val fid = cfg.findEnclosingNode(inst)._1
          /* this */
          val lset_this = state._1(SinglePureLocalLoc)("@this")._1._2._2
    
          /* Bug condition */
          // current function is not global 
          val cond1 = (fid != cfg.getGlobalFId)
          // this value has a global object (maybe)
          val cond2 = lset_this.contains(GlobalLoc)    
          // and it is only abstract location (definite)
          val cond3 = lset_this.size == 1
    
          /* bug check */
          if (definite_flag && !cond3)
            // maybe
            Unit
          else if (cond1 && cond2) {
            // definite
            val begin = info.getSpan.getBegin
            if (cond3) {
              count = count + 1
              bugs.addMessage(info.getSpan, "warning", msg_def)
            }
            else {
              count = count + 1
              bugs.addMessage(info.getSpan, "warning", msg_may)
            }
          }
          else
            Unit
        }
        case _ => Unit
      }
    }
  }
}
