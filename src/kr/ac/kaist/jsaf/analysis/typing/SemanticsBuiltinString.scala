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

object SemanticsBuiltinString {

  def builtinString(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

    fun match {
      case "String" => {
        // 15.5.1.1 String( [value] )
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val s = n_arglen match {
          case UIntSingle(n) if n == 0 =>
            AbsString.alpha("")
          case UIntSingle(n) if n > 0 =>
            Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "0")))
          case NumBot => StrBot
          case _ => StrTop
        }
        if (s </ StrBot)
          ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
        else
          ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.constructor" => {
        // 15.5.2.1 new String( [value] )
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val s = n_arglen match {
          case UIntSingle(n) if n == 0 =>
            AbsString.alpha("")
          case UIntSingle(n) if n > 0 =>
            Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "0")))
          case NumBot => StrBot
          case _ => StrTop
        }
          val h_1 = lset_this.foldLeft(h)((_h, l) => _h.update(l, Helper.NewString(s)))
          if (s </ StrBot)
            ((Helper.ReturnStore(h_1, Value(lset_this)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.fromCharCode" => {
        // 15.5.3.2 String.fromCharCode( [char0 [, char1[, ...]]] )
        val arg_length = getArgValue(h, ctx, "length")._1._4
        val value_1 =
          if (AbsNumber.alpha(0) <= arg_length) Value(AbsString.alpha(""))
          else ValueBot
        val value_2 =
          if (arg_length </ NumBot) {
            AbsNumber.concretize(arg_length) match {
              case Some(n) => {
                val s = (0 until n.toInt).foldLeft(AbsString.alpha(""))((s, i) => {
                  val v = Operator.ToUInt16(getArgValue(h, ctx, i.toString))
                  s.concat(AbsString.fromCharCode(v))
                })
                Value(s)
              }
              case None => Value(StrTop)
            }
          } else {
            ValueBot
          }
        val value = value_1 + value_2
          if (value </ ValueBot)
            ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))

      }
      case "String.prototype.toString" |
           "String.prototype.valueOf" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("String")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val lset_string = lset_this.filter((l) => AbsString.alpha("String") <= h(l)("@class")._1._2._1._5)
        val v = lset_string.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
          if (v </ ValueBot)
            ((Helper.ReturnStore(h, v), ctx), (he + h_e, ctxe + ctx_e))
          else
            ((HeapBot, ContextBot), (he + h_e, ctxe + ctx_e))
      }
      case "String.prototype.charAt" => {
        // 15.5.4.4 String.prototype.charAt(pos)
        val n_pos = Operator.ToInteger(getArgValue(h, ctx, "0"))

        // 1. Call CheckObjectCoercible passing the this value as its argument.
        //   Don't need to check this because <>getBase always returns a location which points to an object.
        // Let S be the result of calling ToString, giving it the this value as its argument.]
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

        val s_this = Helper.toString(Helper.toPrimitive(v_this))
        val n_size = s_this.length()

        val value_1 =
          if (BoolTrue <= (n_pos < AbsNumber.alpha(0))) Value(AbsString.alpha(""))
          else ValueBot
        val value_2 =
          if (BoolTrue <= (n_size < n_pos) || BoolTrue <= (n_size === n_pos))  Value(AbsString.alpha(""))
          else ValueBot
        val value_3 = Value(s_this.charAt(n_pos))
        val value = value_1 + value_2 + value_3

          if (value </ ValueBot)
            ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.charCodeAt" => {
        // 15.5.4.5 String.prototype.charCodeAt(pos)
        val n_pos = Operator.ToInteger(getArgValue(h, ctx, "0"))

        // 1. Call CheckObjectCoercible passing the this value as its argument.
        //   Don't need to check this because <>getBase always returns a location which points to an object.
        // Let S be the result of calling ToString, giving it the this value as its argument.
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

        val s_this = Helper.toString(Helper.toPrimitive(v_this))
        val n_size = s_this.length()

        val value_1 =
          if (BoolTrue <= (n_pos < AbsNumber.alpha(0))) Value(NaN)
          else ValueBot
        val value_2 =
          if (BoolTrue <= (n_size < n_pos) || BoolTrue <= (n_size === n_pos))  Value(NaN)
          else ValueBot
        val value_3 = Value(s_this.charCodeAt(n_pos))
        val value = value_1 + value_2 + value_3

          if (value </ ValueBot)
            ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.concat" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))
        val s = n_arglen match {
          case UIntSingle(n) if n == 0 => s_this
          case UIntSingle(n) if n > 0 =>
            (0 until n.toInt).foldLeft(s_this)((_s, i) =>
              _s.concat(Helper.toString(Helper.toPrimitive((getArgValue(h, ctx, i.toString))))))
          case NumBot => StrBot
          case _ => StrTop
        }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.indexOf" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))

        val s_search = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "0")))
        val n_pos = Operator.ToInteger(getArgValue(h, ctx, "1"))

        val n = (AbsString.concretize(s_this), AbsString.concretize(s_search),AbsNumber.concretize(n_pos)) match {
          case (Some(ss_this), Some(ss_search), Some(nn_pos)) =>
            AbsNumber.alpha(ss_this.indexOf(ss_search, nn_pos.toInt).toDouble)
          case _ =>
              if (s_this <= StrBot || s_search <= StrBot || n_pos <= NumBot)
                NumBot
              else
                NumTop
          }

          if (n </ NumBot)
            ((Helper.ReturnStore(h, Value(n)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.lastIndexOf" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))

        val s_search = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "0")))
        val v_pos = getArgValue(h, ctx, "1")
        val v_pos1 =
          if (v_pos._1._1 </ UndefBot)
            Value(PValue(UndefBot, v_pos._1._2, v_pos._1._3, PosInf + v_pos._1._4, v_pos._1._5), v_pos._2)
          else v_pos
        val n_pos = Operator.ToInteger(v_pos1)

        val n = (AbsString.concretize(s_this), AbsString.concretize(s_search),AbsNumber.concretize(n_pos)) match {
          case (Some(ss_this), Some(ss_search), Some(nn_pos)) =>
            AbsNumber.alpha(ss_this.lastIndexOf(ss_search, nn_pos.toInt).toDouble)
          case _ =>
              if (s_this <= StrBot || s_search <= StrBot || n_pos <= NumBot)
                NumBot
              else
                NumTop
          }
          if (n </ NumBot)
            ((Helper.ReturnStore(h, Value(n)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.localeCompare" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))
        val s_that = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "0")))
        val n = (AbsString.concretize(s_this), AbsString.concretize(s_that)) match {
          case (Some(s_this), Some(s_that)) =>
            AbsNumber.alpha(s_this.compare(s_that).toDouble)
          case _ =>
            if (s_this <= StrBot || s_that <= StrBot)
              NumBot
            else
              NumTop
          }

          if (n </ NumBot)
            ((Helper.ReturnStore(h, Value(n)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.slice" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))
        val n_start = Operator.ToInteger(getArgValue(h, ctx, "0"))
        val v_end = getArgValue(h, ctx, "1")
        val n_end =
          if (v_end._1._1 </ UndefBot) {
            val n = s_this match {
              case NumStrSingle(s) => AbsNumber.alpha(s.length)
              case OtherStrSingle(s) => AbsNumber.alpha(s.length)
              case StrBot => NumBot
              case _ => UInt
            }
            Operator.ToInteger(Value(PValue(UndefBot, v_end._1._2, v_end._1._3, n + v_end._1._4, v_end._1._5), v_end._2))
          }
          else
            Operator.ToInteger(v_end)
        val s = (AbsString.concretize(s_this),
            AbsNumber.concretize(n_start),
            AbsNumber.concretize(n_end)) match {
          case (Some(_s), Some(start), Some(end)) =>
            val from =
              if (start < 0) max(_s.length + start, 0).toInt
              else min(start, _s.length).toInt
            val to =
              if (end < 0) max(_s.length + end, 0).toInt
              else min(end, _s.length).toInt
            if (from >= to)
              AbsString.alpha("")
            else
              AbsString.alpha(_s.slice(from, to))
          case _ =>
            if (s_this <= StrBot || n_start <= NumBot || n_end <= NumBot)
              StrBot
            else
              StrTop
        }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.substring" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))
        val n_start = Operator.ToInteger(getArgValue(h, ctx, "0"))
        val v_end = getArgValue(h, ctx, "1")
        val n_end =
          if (v_end._1._1 </ UndefBot) {
            val n = s_this match {
              case NumStrSingle(s) => AbsNumber.alpha(s.length)
              case OtherStrSingle(s) => AbsNumber.alpha(s.length)
              case StrBot => NumBot
              case _ => UInt
            }
            Operator.ToInteger(Value(PValue(UndefBot, v_end._1._2, v_end._1._3, n + v_end._1._4, v_end._1._5), v_end._2))
          }
          else
            Operator.ToInteger(v_end)
        val s = (AbsString.concretize(s_this),
            AbsNumber.concretize(n_start),
            AbsNumber.concretize(n_end)) match {
          case (Some(_s), Some(start), Some(end)) =>
            val finalStart =
              if (start.isNaN || start < 0) min(0, _s.length)
              else min(start, _s.length)
            val finalEnd =
              if (end.isNaN || end < 0)  min(0, _s.length)
              else min(end, _s.length)
            val from = min(finalStart, finalEnd).toInt
            val to = max(finalStart, finalEnd).toInt
            AbsString.alpha(_s.substring(from, to))
          case _ =>
            if (s_this <= StrBot || n_start <= NumBot || n_end <= NumBot)
              StrBot
            else
              StrTop
        }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.toLowerCase" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))

        val s = AbsString.concretize(s_this) match {
          case Some(_s) => AbsString.alpha(_s.toLowerCase)
          case None => s_this
        }
        if (s </ StrBot)
          ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
        else
          ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.toLocaleLowerCase" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))

        val s = AbsString.concretize(s_this) match {
          case Some(_s) => AbsString.alpha(_s.toLowerCase)
          case None => s_this
        }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.toUpperCase" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))

        val s = AbsString.concretize(s_this) match {
          case Some(_s) => AbsString.alpha(_s.toUpperCase)
          case None => s_this
        }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.toLocaleUpperCase" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))

        val s = AbsString.concretize(s_this) match {
          case Some(_s) => AbsString.alpha(_s.toUpperCase)
          case None => s_this
        }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case "String.prototype.trim" => {
        // [[default Value]] ??
        val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
        // TODO: v_this must be the result of [[DefaultValue]](string)
        val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
        val s_this = Helper.toString(Helper.toPrimitive(v_this))
        System.out.println("v_this = " + DomainPrinter.printValue(v_this))
        System.out.println("s_this = " + s_this)
        val s = AbsString.concretize(s_this) match {
          case Some(_s) => AbsString.alpha(_s.trim)
          case None => 
            if (s_this <= StrBot)
              s_this
            else
              StrTop
        }

        System.out.println("s = " + s)

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

}
