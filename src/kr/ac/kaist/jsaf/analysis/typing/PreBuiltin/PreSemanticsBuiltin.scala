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

object PreSemanticsBuiltin {

  def builtinCall(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address, addr3: Address, addr4: Address): ((Heap, Context),(Heap, Context)) = {
          val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

        val PureLocalLoc = cfg.getPureLocal(cp)
        def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1
        val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx, PureLocalLoc)._1
        val lset_this = h(PureLocalLoc)("@this")._1._2._2
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
          case "Global"         => PreSemanticsBuiltinGlobal.builtinGlobal(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "Object"         => PreSemanticsBuiltinObject.builtinObject(sem, h, ctx, he, ctxe, cp, cfg, fun, args, addr1)
          case "Function"       => PreSemanticsBuiltinFunction.builtinFunction(sem, h, ctx, he, ctxe, cp, cfg, fun, args, addr1, addr2, addr3, addr4)
          case "Array"          => PreSemanticsBuiltinArray.builtinArray(sem, h, ctx, he, ctxe, cp, cfg, fun, args, addr1)
          case "String"         => PreSemanticsBuiltinString.builtinString(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "Boolean"        => PreSemanticsBuiltinBoolean.builtinBoolean(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "Number"         => PreSemanticsBuiltinNumber.builtinNumber(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "Math"           => PreSemanticsBuiltinMath.builtinMath(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "Date"           => PreSemanticsBuiltinDate.builtinDate(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "RegExp"         => PreSemanticsBuiltinRegExp.builtinRegExp(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "Error"          => PreSemanticsBuiltinError.builtinError(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "EvalError"      => PreSemanticsBuiltinError.builtinEvalError(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "RangeError"     => PreSemanticsBuiltinError.builtinRangeError(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "ReferenceError" => PreSemanticsBuiltinError.builtinReferenceError(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "SyntaxError"    => PreSemanticsBuiltinError.builtinSyntaxError(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "TypeError"      => PreSemanticsBuiltinError.builtinTypeError(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "URIError"       => PreSemanticsBuiltinError.builtinURIError(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case "JSON"           => PreSemanticsBuiltinJSON.builtinJSON(sem, h, ctx, he, ctxe, cp, cfg, fun, args)
          case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
        }
  }

  def builtinGlobal(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx, PureLocalLoc)._1
    val lset_this = h(PureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      case "Global.parseInt" => {
            // 15.1.2.2 parseInt(string, radix)
            val v_1 = getArgValue(h, ctx,"0") /* string */
            val v_2 = getArgValue(h, ctx, "1") /* radix */

            val inputString = PreHelper.toString(PreHelper.toPrimitive(v_1))
            // TODO: Simple implementation. Must be revised. Not the same as the original.
            val r = Operator.ToInt32(v_2)

            val value = Operator.parseInt(inputString, r)
            val rtn = Value(value)

            ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctx))
          }
          case "Global.encodeURIComponent" => {
            // TODO
            val value = Value(StrTop)
            val es = Set[Exception](URIError)
            val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)

            ((PreHelper.ReturnStore(h_e, PureLocalLoc, value), ctx_e), (he + h_e, ctxe + ctx_e))
          }
          case "Global.isNaN" => {
            val n = PreHelper.toNumber(PreHelper.toPrimitive(getArgValue(h, ctx, "0")))
            val b =
              if (NaN == n)
                BoolTrue
              else if (NaN </ n)
                BoolFalse
              else if (NaN <= n)
                BoolTop
              else
                BoolBot
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(b)), ctx), (he, ctxe))
          }
          case "Global.isFinite" => {
            val n = PreHelper.toNumber(PreHelper.toPrimitive(getArgValue(h, ctx, "0")))
            val b =
              if (NaN == n || PosInf == n || NegInf == n)
                BoolFalse
              else if (NaN </ n && PosInf </ n && NegInf </ n)
                BoolTrue
              else if (NaN <= n || PosInf <= n || NegInf <= n)
                BoolTop
              else
                BoolBot
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(b)), ctx), (he, ctxe))
          }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

}
