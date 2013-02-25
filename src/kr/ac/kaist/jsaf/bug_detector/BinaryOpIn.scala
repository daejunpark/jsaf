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

class BinaryOpIn(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  /* bug message */  
  private val msg = "Right-hand side operand '%s' at In-op is non-object."

  /* 
  * flag  : definite only case
  * count : the number of total non-object operands at In operator
  */
  private var definite_only = true 
  private var count = 0

  override def printStat = System.out.println("# BinaryOpIn: " + count) 
    
  /* CFGBin -> In op */
  override def checkExpr(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (state._1 <= HeapBot)  Unit
    else {
      expr match {
        case CFGBin(info, first, op, second) => 
          op.getText match {
            case "in" => check(info, second)
            case _ => Unit
          }
        case _ => Unit
      }
    }
  
    def check(info: Info, obj: CFGExpr): Unit = {
      /* definite only */
      if (SE.V(obj, heap, context)._1._2.subsetOf(LocSetBot)) {
        count = count + 1
        bugs.addMessage(info.getSpan, "error", msg.format(obj.toString))
      }
      /* maybe */
      else if (!definite_only) {
        // to be implemented ...
      }
      Unit
    }
  }
}
