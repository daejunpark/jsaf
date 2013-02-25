/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.DomSemantics

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.Operator._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.analysis.typing._

import scala.math.{min,max,floor, abs}

object SemanticsDOMNode {
  def DOMNode(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

    // TODO : filling in semantics; currently all functions return the value 1 
    fun match {
      case "Node.prototype.appendChild" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Node.prototype.cloneNode" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Node.prototype.hasChildNodes" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Node.prototype.insertBefore" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Node.prototype.removeChild" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Node.prototype.replaceChild" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Node.prototype.isSupported" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Node.prototype.hasAttributes" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Node.prototype.normalize" => {
        val value = Value(AbsNumber.alpha(1))
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of the DOM API function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
