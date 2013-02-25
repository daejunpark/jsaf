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
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class PrimitiveToObject(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  private val msg  = "Converting primitive value(%s) to object."
  
  private var count = 0
  override def printStat = System.out.println("# PrimitiveToObject: " + count)

  private val definite_flag = false
    
  /* InternalCall: toObject */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state = typing.mergeState(cstate)
    if (state._1 <= HeapBot)
      Unit
    else {
      inst match  {
        case CFGInternalCall(_, info, lhs, fun, arguments, loc) => {
          (fun.toString, arguments, loc)  match {
            case ("<>Global<>toObject", List(expr), Some(a_new)) => {
              val value = SE.V(expr, state._1, state._2)._1
              if (definite_flag && (value._1._1 </ UndefBot || value._1._2 </ NullBot || !value._2.isEmpty))
                // maybe
                Unit
              else {
                // definite
                val s1 = 
                  if (value._1._3 </ BoolBot) List("boolean")
                  else List()
                val s2 =
                  if (value._1._4 </ NumBot) List("number")
                  else List()
                //val s3 =
                //  if (value._1._5 </ StrBot) List("string")
                //  else List()
                val s = s1 ++ s2 //++ s3
                if (s.isEmpty)
                  Unit
                else if (s.size == 1) {
                  count = count + 1
                  bugs.addMessage(info.getSpan, "warning", msg.format(s.head))
                  }
                else {
                  val str = s.tail.foldLeft(s.head)((_str, _s) => _str + ", " + _s)
                  count = count + 1
                  bugs.addMessage(info.getSpan, "warning", msg.format(str))
                }
              }
            }
            case _ => Unit
          }
        }
        case _ => Unit 
      }
    }
  }
}
