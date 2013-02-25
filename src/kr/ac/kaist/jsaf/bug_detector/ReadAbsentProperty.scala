/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import scala.util.control.Breaks._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class ReadAbsentProperty(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
 
  private val msg  = "Reading absent property '%s' of an object."
  
  private var count = 0
  override def printStat = System.out.println("# ReadAbsentProperty: " + count)
    
  private val definite_flag = false

  /* Load */
  override def checkExpr(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state = typing.mergeState(cstate)
    val heap = state._1
    val context = state._2

    if (state._1 <= HeapBot)
      Unit
    else {
      expr match {
        case CFGLoad(info, obj, index) => {
          val o = SE.V(obj, heap, context)._1
          val s = SE.V(index, heap, context)._1._1._5
          val b_exists = o._2.foldLeft[AbsBool](BoolBot)((b, l) => b + Helper.HasProperty(heap, l, s))
          if (definite_flag && BoolTop <= b_exists)
            // maybe
            Unit
          else if (BoolFalse <= b_exists) {
            // definitely
            count = count + 1
            bugs.addMessage(info.getSpan, "warning", msg.format(printPropertyName(s)))
          }
        }
        case _ => Unit
      }
    }

    def printPropertyName(x: AbsString): String = {
      AbsString.concretize(x) match {
        case Some(propName) => propName
        case _ => "unknown_property"
      }
    }
    
  }
}
