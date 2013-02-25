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

class BinaryOpInstanceOf(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  /* bug message */  
  private val omsg = "Right-hand side operand '%s' at InstanceOf is non-object."
  private val fmsg = "Right-hand side operand '%s' at InstanceOf is non-function object."
  private val pmsg = "Right-hand side operand '%s' at InstanceOf is of non-object prototype."

  /* 
  * flag    : definite only case
  * ocount  : the number of total non-object
  * fcount  : the number of total non-function object
  * pcount  : the number of total non-object prototype
  */
  private var definite_only = true 
  private var ocount = 0
  private var fcount = 0
  private var pcount = 0

  override def printStat = System.out.println("# BinaryOpInstanceOf: " + (ocount + fcount + pcount)) 
    
  /* CFGBin -> InstanceOf op */
  override def checkExpr(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (state._1 <= HeapBot)  Unit
    else {
      expr match {
        case CFGBin(info, first, op, second) => 
          op.getText match {
            case "instanceof" => 
              val lset_original = SE.V(second, heap, context)._1._2
              /* RHS is non-object */
              if (lset_original.subsetOf(LocSetBot)) {
                ocount = ocount + 1
                bugs.addMessage(info.getSpan, "error", omsg.format(second.toString))
              }
              else {
                val lset_filtered = lset_original filter((l) => BoolTrue <= Helper.IsCallable(heap, l)) 
                /* RHS is non-function object */
                if (lset_filtered.size < lset_original.size) {
                  fcount = fcount + 1
                  bugs.addMessage(info.getSpan, "error", fmsg.format(second.toString))
                }
                else {
                  /* RHS is of non-object prototype: Definite only (not sound) */
                  val isObjectProto = lset_filtered.foldLeft[Boolean](false)((isObjProto, l) => 
                                if (!isObjProto && !(heap(l)("@proto")._1._1._1._2 contains ObjProtoLoc)) false else true)
                  if (!isObjectProto) {
                    pcount = pcount + 1
                    // Downgrade to warning due to its low severity
                    bugs.addMessage(info.getSpan, "warning", pmsg.format(second.toString))
                  }
                }
              } 
            case _ => Unit
          }
        case _ => Unit
      }}
    Unit
  }
}
