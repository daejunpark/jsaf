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

object SemanticsBuiltinNumber {
  def builtinNumber(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))    
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
        
    fun match {
      case "Number" => {
        // 15.7.1.1 Number( [value] )
        val v_1 = getArgValue(h, ctx, "0")
        val arg_length = getArgValue(h, ctx, "length")._1._4

        // If value is not supplied, +0 is returned.
        val value_1 =
          if (AbsNumber.alpha(0) <= arg_length) Value(AbsNumber.alpha(0))
          else ValueBot
        // Returns a Number value computed by ToNumber(value).
        val value_2 =
          if (AbsNumber.alpha(0) != arg_length && !(arg_length <= NumBot)) Value(Helper.toNumber(Helper.toPrimitive(v_1)))
          else ValueBot
        val value = value_1 + value_2

        ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
      }
      case "Number.constructor" => {
        // 15.7.2.1 new Number( [value] )
        val v_1 = getArgValue(h, ctx, "0")
        val arg_length = getArgValue(h, ctx, "length")._1._4

        // [[PrimitiveValue]]
        val value_1 =
          if (AbsNumber.alpha(0) <= arg_length) AbsNumber.alpha(0)
          else NumBot
        val value_2 =
          if (AbsNumber.alpha(0) != arg_length && !(arg_length <= NumBot)) Helper.toNumber(Helper.toPrimitive(v_1))
          else NumBot
        val primitive_value = value_1 + value_2

        val h_1 = lset_this.foldLeft(h)((_h, l) => _h.update(l, Helper.NewNumber(primitive_value)))
        
        if (primitive_value </ NumBot)
          ((Helper.ReturnStore(h_1, Value(lset_this)), ctx), (he, ctxe))
        else
          ((HeapBot, ContextBot), (he, ctxe))
      }
      case "Number.prototype.toString" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val lset_num = lset_this.filter((l) => OtherStrSingle("Number") <= h(l)("@class")._1._2._1._5)
        val v_prim = lset_num.foldLeft(ValueBot)((_v, _l) => _v + h(_l)("@primitive")._1._2)
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("Number")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val (v, es2) =
          n_arglen match {
            case UIntSingle(n_arglen) if n_arglen == 0 =>
              (Value(Helper.toString(v_prim._1)), ExceptionBot)
            case UIntSingle(n_arglen) if n_arglen > 0 => {
              val es =
                if (BoolTrue <= Operator.bopGreater(getArgValue(h, ctx, "0"), Value(AbsNumber.alpha(36)))._1._3)
                  Set[Exception](RangeError)
                else if (BoolTrue <= Operator.bopLess(getArgValue(h, ctx, "0"), Value(AbsNumber.alpha(2)))._1._3)
                  Set[Exception](RangeError)
                else
                  ExceptionBot
              (Value(StrTop), es)
            }
            case NumBot => (ValueBot, ExceptionBot)
            case _ => {
              (Value(StrTop), Set[Exception](RangeError))
            }
          }
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es++es2)
        if (v </ ValueBot)
          ((Helper.ReturnStore(h, v), ctx), (he + h_e, ctxe + ctx_e))
        else
          ((HeapBot, ContextBot), (he + h_e, ctxe + ctx_e))
      }
      case "Number.prototype.toLocaleString" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val v_prim = lset_this.foldLeft(ValueBot)((_v, _l) => _v + h(_l)("@primitive")._1._2)
        val v = Value(Helper.toString(v_prim._1))
        if (v </ ValueBot)
          ((Helper.ReturnStore(h, v), ctx), (he, ctxe))
        else
          ((HeapBot, ContextBot), (he, ctxe))
      }
      case "Number.prototype.valueOf" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Number")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val lset_num = lset_this.filter((l) => OtherStrSingle("Number") <= h(l)("@class")._1._2._1._5)
        val n = lset_num.foldLeft[AbsNumber](NumBot)((_b, l) => _b + h(l)("@primitive")._1._2._1._4)
        val (h_1, c_1) =
          if (n == NumBot)
            (HeapBot, ContextBot)
          else
            (Helper.ReturnStore(h, Value(n)), ctx)
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
        ((h_1, c_1), (he + h_e, ctxe + ctx_e))
      }
      case "Number.prototype.toFixed" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, AbsNumber.alpha(0) + v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(20)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(0)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
        ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he + h_e, ctxe + ctx_e))
      }
      case "Number.prototype.toExponential" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(20)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(0)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
        ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he + h_e, ctxe + ctx_e))
      }
      case "Number.prototype.toPrecision" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(21)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(1)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
        ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he + h_e, ctxe + ctx_e))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
