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
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

import scala.math.{min,max,floor, abs}

object SemanticsBuiltinBoolean {
  def builtinBoolean(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

    fun match {
      case "Boolean" => {
        // 15.6.1.1 Boolean(value)
        val v_1 = getArgValue(h, ctx, "0")
        val arg_length = getArgValue(h, ctx, "length")._1._4

        // Returns a Boolean value computed by ToBoolean(value).
        val value =
          if (!(arg_length <= NumBot)) Value(Helper.toBoolean(v_1))
          else ValueBot
        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Boolean.constructor" => {
        // 15.6.2.1 new Boolean(value)
        val v_1 = getArgValue(h, ctx, "0")
        val arg_length = getArgValue(h, ctx, "length")._1._4

        // [[PrimitiveValue]]
        val primitive_value =
          if (!(arg_length <= NumBot)) Helper.toBoolean(v_1)
          else BoolBot

        val h_1 = lset_this.foldLeft(h)((_h, l) => _h.update(l, Helper.NewBoolean(primitive_value)))

        if (primitive_value </ BoolBot) {
          ((Helper.ReturnStore(h_1, Value(lset_this)), ctx), (he, ctxe))
        }
        else
          ((HeapBot, ContextBot), (he, ctxe))
      }
      case "Boolean.prototype.toString" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Boolean")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val lset_bool = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == OtherStrSingle("Boolean"))
        val b = lset_bool.foldLeft[AbsBool](BoolBot)((_b, l) => _b + h(l)("@primitive")._1._2._1._3)
        val s = b match {
            case BoolTrue => OtherStrSingle("true")
            case BoolFalse => OtherStrSingle("false")
            case BoolTop => OtherStr
            case BoolBot => StrBot
          }
        val (h_1, c_1) =
          if (s == StrBot){
            (HeapBot, ContextBot)
          }
          else {
            (Helper.ReturnStore(h, Value(s)), ctx)
          }
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
        ((h_1, c_1), (h_e, ctx_e))
      }
      case "Boolean.prototype.valueOf" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Boolean")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val lset_bool = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == OtherStrSingle("Boolean"))
        val b = lset_bool.foldLeft[AbsBool](BoolBot)((_b, l) => _b + h(l)("@primitive")._1._2._1._3)
        val (h_1, c_1) =
          if (b == BoolBot) {
            (HeapBot, ContextBot)
          }
          else {
            (Helper.ReturnStore(h, Value(b)), ctx)
          }
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
        ((h_1, c_1), (h_e, ctx_e))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
