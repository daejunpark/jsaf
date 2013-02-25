/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.Operator._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.analysis.typing.{PreSemanticsExpr => SE}

import scala.math.{min,max,floor, abs}

object PreSemanticsBuiltinDate {
  def builtinDate(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1
    val lset_this = h(PureLocalLoc)("@this")._1._2._2

    fun match {
      case "Date" => {
        if(Config.preAnalysis)
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(StrTop)), ctx), (he, ctxe))
        else
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(StrTop)), ctx), (he, ctxe))
      }
      case "Date.constructor" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val pv_1 = PreHelper.toPrimitive(getArgValue(h, ctx, "0"))
        val n_prim_3 =
          if (pv_1._5 </ StrBot)
            //parse
            NumTop
          else
            NumBot
        val pv_nonstr = PValue(pv_1._1, pv_1._2, pv_1._3, pv_1._4, StrBot)
        val n_prim_4 =
          if (pv_nonstr </ PValueBot)
            PreHelper.toNumber(pv_nonstr)
          else
            NumBot
        val n_prim = n_arglen match {
          // 15.9.3.2 new Date(value)
          case UIntSingle(n) if n == 1 => (n_prim_3 + n_prim_4)
          // 15.9.3.1 new Date( year, month [, date [, hours [, minutes [, seconds [, ms ]]]]] )
          // 15.9.3.3 new Date()
          case UIntSingle(n) if n != 1 => NumTop
          case NumBot => NumBot
          case _ => NumTop
        }

        val h_1 = lset_this.foldLeft(h)((_h, l) => _h.update(l, PreHelper.NewDate(Value(n_prim))))
        if (n_prim </ NumBot)
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(lset_this)), ctx), (he, ctxe))
        else
          ((h_1, ctx),(he,ctxe))
      }
      case "Date.now" => {
        ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.parse" => {
        ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.toString" |
           "Date.prototype.toDateString" |
           "Date.prototype.toTimeString" |
           "Date.prototype.toLocaleString" |
           "Date.prototype.toLocaleDateString" |
           "Date.prototype.toLocaleTimeString" |
           "Date.prototype.toUTCString" |
           "Date.prototype.toISOString" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(StrTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.valueOf" => {
        val v = lset_this.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        if (v </ ValueBot)
          ((PreHelper.ReturnStore(h, PureLocalLoc, v), ctx), (he, ctxe))
        else
          ((h, ctx), (he, ctxe))
      }
      case "Date.prototype.getTime" => {
        val v = lset_this.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        if (v </ ValueBot)
          ((PreHelper.ReturnStore(h, PureLocalLoc, v), ctx), (he, ctxe))
        else
          ((h, ctx), (he, ctxe))
      }
      case "Date.prototype.getFullYear" |
           "Date.prototype.getMonth" |
           "Date.prototype.getDate" |
           "Date.prototype.getDay" |
           "Date.prototype.getHours" |
           "Date.prototype.getMinutes" |
           "Date.prototype.getSeconds" |
           "Date.prototype.getMilliseconds" |
           "Date.prototype.getTimezoneOffset" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.getUTCFullYear" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.getUTCMonth" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.getUTCDate" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.getUTCDay" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.getUTCHours" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.getUTCMinutes" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.getUTCSeconds" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.getUTCMilliseconds" => {
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      }
      case "Date.prototype.setTime" => {
        val time = getArgValue(h, ctx, "0")
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(time))))
        ((PreHelper.ReturnStore(h_1, PureLocalLoc, time), ctx), (he, ctxe))
      }
      case "Date.prototype.setMilliseconds" |
           "Date.prototype.setSeconds" |
           "Date.prototype.setMinutes" |
           "Date.prototype.setMinutes" |
           "Date.prototype.setHours" |
           "Date.prototype.setDate" |
           "Date.prototype.setMonth" |
           "Date.prototype.setFullYear" => {
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(Value(NumTop)))))
        if (!(h_1 <= HeapBot))
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
        else
          ((h_1, ctx), (he, ctxe))
      }
      case "Date.prototype.setUTCMilliseconds" => {
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(Value(NumTop)))))
          if (!(h_1 <= HeapBot))
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
          else
            ((h_1, ctx), (he, ctxe))
      }
      case "Date.prototype.setUTCSeconds" => {
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(Value(NumTop)))))
        if (!(h_1 <= HeapBot))
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
        else
          ((h_1, ctx), (he, ctxe))
      }
      case "Date.prototype.setUTCMinutes" => {
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(Value(NumTop)))))
        if (!(h_1 <= HeapBot))
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
        else
          ((h_1, ctx), (he, ctxe))
      }
      case "Date.prototype.setUTCHours" => {
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(Value(NumTop)))))
        if (!(h_1 <= HeapBot))
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
        else
          ((h_1, ctx), (he, ctxe))
      }
      case "Date.prototype.setUTCDate" => {
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(Value(NumTop)))))
        if (!(h_1 <= HeapBot))
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
        else
          ((h_1, ctx), (he, ctxe))
      }
      case "Date.prototype.setUTCMonth" => {
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(Value(NumTop)))))
        if (!(h_1 <= HeapBot))
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
        else
          ((h_1, ctx), (he, ctxe))
      }
      case "Date.prototype.setUTCFullYear" => {
        val h_1 = lset_this.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@primitive", PropValue(Value(NumTop)))))
        if (!(h_1 <= HeapBot))
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
        else
          ((h_1, ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
