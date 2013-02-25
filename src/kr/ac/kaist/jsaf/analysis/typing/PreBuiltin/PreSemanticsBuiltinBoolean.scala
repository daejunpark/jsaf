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

object PreSemanticsBuiltinBoolean {
  def builtinBoolean(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1
    val lset_this = h(PureLocalLoc)("@this")._1._2._2

    fun match {
      case "Boolean" => {
        // 15.6.1.1 Boolean(value)
        val v_1 = getArgValue(h, ctx, "0")
        val arg_length = getArgValue(h, ctx, "length")._1._4

        // Returns a Boolean value computed by ToBoolean(value).
        val value =
          if (!(arg_length <= NumBot)) Value(PreHelper.toBoolean(v_1))
          else ValueBot
        ((PreHelper.ReturnStore(h, PureLocalLoc, value), ctx), (he, ctxe))
      }
      case "Boolean.constructor" => {
        // 15.6.2.1 new Boolean(value)
        val v_1 = getArgValue(h, ctx, "0")
        val arg_length = getArgValue(h, ctx, "length")._1._4

        // [[PrimitiveValue]]
        val primitive_value =
          if (!(arg_length <= NumBot)) PreHelper.toBoolean(v_1)
          else BoolBot

        val h_1 = lset_this.foldLeft(h)((_h, l) => _h.update(l, PreHelper.NewBoolean(primitive_value)))

        if (primitive_value </ BoolBot) {
          ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(lset_this)), ctx), (he, ctxe))
        }
        else
          ((h_1, ctx), (he, ctxe))
      }
      case "Boolean.prototype.toString" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Boolean")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        // @class may be OtherStr by Boolean.constructor
//        val lset_bool = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == OtherStrSingle("Boolean"))
        val lset_bool = lset_this.filter((l) => OtherStrSingle("Boolean") <= h(l)("@class")._1._2._1._5)
        val b = lset_bool.foldLeft[AbsBool](BoolBot)((_b, l) => _b + h(l)("@primitive")._1._2._1._3)
        val s = b match {
            case BoolTrue => OtherStrSingle("true")
            case BoolFalse => OtherStrSingle("false")
            case BoolTop => OtherStr
            case BoolBot => StrBot
          }
        val (h_1, c_1) =
          if (s == StrBot){
            (h, ctx)
          }
          else {
            (PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx)
          }
        val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)
        ((h_1, c_1), (h_e, ctx_e))
      }
      case "Boolean.prototype.valueOf" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Boolean")))
            Set[Exception](TypeError)
          else
            ExceptionBot
          // @class may be OtherStr by Boolean.constructor
//        val lset_bool = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == OtherStrSingle("Boolean"))
        val lset_bool = lset_this.filter((l) => OtherStrSingle("Boolean") <= h(l)("@class")._1._2._1._5)
        val b = lset_bool.foldLeft[AbsBool](BoolBot)((_b, l) => _b + h(l)("@primitive")._1._2._1._3)
        val (h_1, c_1) =
          if (b == BoolBot) {
            System.out.println("BOOLEAN BOT?")
            (h, ctx)
          }
          else {
            System.out.println("BOOLEAN BOT=" + b)
            (PreHelper.ReturnStore(h, PureLocalLoc, Value(b)), ctx)
          }
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, c_1, PureLocalLoc, es)
        ((h_e, ctx_e), (h_e, ctx_e))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
