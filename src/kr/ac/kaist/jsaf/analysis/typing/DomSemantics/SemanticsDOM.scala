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

object SemanticsDOM {

  def DOMCall(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address, addr3: Address, addr4: Address): ((Heap, Context),(Heap, Context)) = {
          val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

        def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
        val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
        val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
        val val_this = Value(lset_this)
        /* for Date */
        val calendar = java.util.Calendar.getInstance()
        def getCalendarUTCTime(long_time: Long, what: Int): Int = {
          calendar.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
          calendar.setTimeInMillis(long_time)
          calendar.get(what)
        }
        val object_name =
          if (fun.indexOf('.') == -1)
            fun
          else
            fun.take(fun.indexOf('.'))

        object_name match  {
          case "Node"         => 
            SemanticsDOMNode.DOMNode(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case _ =>
        System.err.println("* Warning: Semantics of the DOM API function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
        }
  }


}
