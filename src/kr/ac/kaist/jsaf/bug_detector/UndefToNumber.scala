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

class UndefToNumber(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  private val msg  = "Converting undefined to number."
  
  private var count = 0
  override def printStat = System.out.println("# UndefToNumber: " + count)
    
  private val definite_flag = false

  /* InternalCall: toNumber */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state = typing.mergeState(cstate)
    if (state._1 <= HeapBot)
      Unit
    else {
      inst match  {
        case CFGInternalCall(_, info, lhs, fun, arguments, loc) => {
          (fun.toString, arguments, loc)  match {
            case ("<>Global<>toNumber", List(expr), None) => {
              val value = SE.V(expr, state._1, state._2)._1
              if (definite_flag && (value._1._1 </ UndefBot || value._1._2 </ NullBot || !value._2.isEmpty))
                // maybe
                Unit
              else {
                // definite
                val value = SE.V(expr, state._1, state._2)._1
                if (definite_flag && (value.typeCount > 1))
                  Unit
                else if (value._1._1 </ UndefBot) {
                  count = count + 1
                  bugs.addMessage(info.getSpan, "warning", msg)
                }
                else
                  Unit
              }
            }
            case _ => Unit // other internal call
          }
        }
        case _ => Unit // other inst.
      }
    }
  }
  
  /* expr */
  override def checkExpr(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state = typing.mergeState(cstate)
    if (state._1 <= HeapBot)
      Unit
    else {
	  expr match {
	    case CFGBin(info, first, op, second) => {
	      op.getText match {
	        case "|" | "&" | "^" | "<<" | ">>" | ">>>" | "-" | "/" | "%" | "*" =>
	          val value_1 = SE.V(first, state._1, state._2)._1
	          val value_2 = SE.V(second, state._1, state._2)._1
	          if (definite_flag && (value_1.typeCount > 1 || value_2.typeCount > 1))
	            Unit
	          else {
	            if (value_1._1._1 </ UndefBot) {
	              count = count + 1
	              bugs.addMessage(first.getInfo.get.getSpan, "warning", msg)
	            }
	            if (value_2._1._1 </ UndefBot) {
	              count = count + 1
	              bugs.addMessage(second.getInfo.get.getSpan, "warning", msg)
	            }
	          }
            case "+" =>
              val lprim = Helper.toPrimitive(SE.V(first, state._1, state._2)._1)
              val rprim = Helper.toPrimitive(SE.V(second, state._1, state._2)._1)
              if (definite_flag && (lprim.typeCount > 1 || rprim.typeCount > 1))
                Unit
              else if (lprim._5 <= StrBot && rprim._5 <= StrBot) {
	            if (lprim._1 </ UndefBot) {
	              count = count + 1
	              bugs.addMessage(first.getInfo.get.getSpan, "warning", msg)
	            }
	            if (rprim._1 </ UndefBot) {
	              count = count + 1
	              bugs.addMessage(second.getInfo.get.getSpan, "warning", msg)
	            }
              }
		      else
		        Unit
            case "==" | "!=" =>
              val v1 = SE.V(first, state._1, state._2)._1
              val v2 = SE.V(second, state._1, state._2)._1
              val cond1 = (v1._1._4 </ NumBot && v2._1._5 </StrBot) &&  v2._1._1 </ UndefBot
              val cond2 = (v1._1._5 </ StrBot && v2._1._4 </NumBot) &&  v1._1._1 </ UndefBot
              if (definite_flag && (v1.typeCount > 1 || v2.typeCount > 1))
                Unit
              else {
                if (cond1) {
	              count = count + 1
	              bugs.addMessage(first.getInfo.get.getSpan, "warning", msg)
	            }
	            if (cond2) {
	              count = count + 1
	              bugs.addMessage(second.getInfo.get.getSpan, "warning", msg)
	            }
              }
            case "<" | "<=" | ">" | ">=" =>
              val lprim = Helper.toPrimitive(SE.V(first, state._1, state._2)._1)
              val rprim = Helper.toPrimitive(SE.V(second, state._1, state._2)._1)
              if (definite_flag && (lprim.typeCount > 1 || rprim.typeCount > 1))
                Unit
              else if ((lprim._5 <= StrBot || rprim._5 <= StrBot)) {
	            if (lprim._1 </ UndefBot) {
	              count = count + 1
	              bugs.addMessage(first.getInfo.get.getSpan, "warning", msg)
	            }
	            if (rprim._1 </ UndefBot) {
	              count = count + 1
	              bugs.addMessage(second.getInfo.get.getSpan, "warning", msg)
	            }
              }
		      else
		        Unit
            case _ => Unit
          }
        }
        case CFGUn(info, op, first) => {
          op.getText match {
            case "+" | "-" | "~" =>
              val value_1 = SE.V(first, state._1, state._2)._1
              if (definite_flag && (value_1.typeCount > 1))
                Unit
              else if (value_1._1._1 </ UndefBot) {
                count = count + 1
		        bugs.addMessage(first.getInfo.get.getSpan, "warning", msg)
              }
		      else
		        Unit
            case _ => Unit
          }
        }
        case _ => Unit
      }
    }

  }
  
}
