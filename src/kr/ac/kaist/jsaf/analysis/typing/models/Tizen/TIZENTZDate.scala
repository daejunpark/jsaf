/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T, _}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing._
import java.lang.InternalError
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc

object TIZENTZDate extends Tizen {
  private val name = "TZDate"
  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")

  val loc_tzdate: Loc        = newPreDefLoc("TZDate", Old)
  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(FunctionProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("tizen.TZDate.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("getDate", AbsBuiltinFunc("tizen.TZDate.getDate", 0)),
    ("setDate", AbsBuiltinFunc("tizen.TZDate.setDate", 1)),
    ("getDay", AbsBuiltinFunc("tizen.TZDate.getDay", 0)),
    ("getFullYear", AbsBuiltinFunc("tizen.TZDate.getFullYear", 0)),
    ("setFullYear", AbsBuiltinFunc("tizen.TZDate.setFullYear", 1)),
    ("getHours", AbsBuiltinFunc("tizen.TZDate.getHours", 0)),
    ("setHours", AbsBuiltinFunc("tizen.TZDate.setHours", 1)),
    ("getMilliseconds", AbsBuiltinFunc("tizen.TZDate.getMilliseconds", 0)),
    ("setMilliseconds", AbsBuiltinFunc("tizen.TZDate.setMilliseconds", 1)),
    ("getMinutes", AbsBuiltinFunc("tizen.TZDate.getMinutes", 0)),
    ("setMinutes", AbsBuiltinFunc("tizen.TZDate.setMinutes", 1)),
    ("getMonth", AbsBuiltinFunc("tizen.TZDate.getMonth", 0)),
    ("setMonth", AbsBuiltinFunc("tizen.TZDate.setMonth", 1)),
    ("getSeconds", AbsBuiltinFunc("tizen.TZDate.getSeconds", 0)),
    ("getUTCDate", AbsBuiltinFunc("tizen.TZDate.getUTCDate", 0)),
    ("setUTCDate", AbsBuiltinFunc("tizen.TZDate.setUTCDate", 1)),
    ("getUTCDay", AbsBuiltinFunc("tizen.TZDate.getUTCDay", 0)),
    ("getUTCFullYear", AbsBuiltinFunc("tizen.TZDate.getUTCFullYear", 0)),
    ("setUTCFullYear", AbsBuiltinFunc("tizen.TZDate.setUTCFullYear", 1)),
    ("getUTCHours", AbsBuiltinFunc("tizen.TZDate.getUTCHours", 0)),
    ("setUTCHours", AbsBuiltinFunc("tizen.TZDate.setUTCHours", 0)),
    ("getUTCMilliseconds", AbsBuiltinFunc("tizen.TZDate.getUTCMilliseconds", 0)),
    ("setUTCMilliseconds", AbsBuiltinFunc("tizen.TZDate.setUTCMilliseconds", 1)),
    ("getUTCMinutes", AbsBuiltinFunc("tizen.TZDate.getUTCMinutes", 0)),
    ("setUTCMinutes", AbsBuiltinFunc("tizen.TZDate.setUTCMinutes", 1)),
    ("getUTCMonth", AbsBuiltinFunc("tizen.TZDate.getUTCMonth", 0)),
    ("setUTCMonth", AbsBuiltinFunc("tizen.TZDate.setUTCMonth", 1)),
    ("getUTCSeconds", AbsBuiltinFunc("tizen.TZDate.getUTCSeconds", 0)),
    ("setUTCSeconds", AbsBuiltinFunc("tizen.TZDate.setUTCSeconds", 1)),
    ("getTimezone", AbsBuiltinFunc("tizen.TZDate.getTimezone", 0)),
    ("toTimezone", AbsBuiltinFunc("tizen.TZDate.toTimezone", 1)),
    ("toLocalTimezone", AbsBuiltinFunc("tizen.TZDate.toLocalTimezone", 0)),
    ("toUTC", AbsBuiltinFunc("tizen.TZDate.toUTC", 0)),
    ("difference", AbsBuiltinFunc("tizen.TZDate.difference", 1)),
    ("equalsTo", AbsBuiltinFunc("tizen.TZDate.equalsTo", 1)),
    ("earlierThan", AbsBuiltinFunc("tizen.TZDate.earlierThan", 1)),
    ("laterThan", AbsBuiltinFunc("tizen.TZDate.laterThan", 1)),
    ("addDuration", AbsBuiltinFunc("tizen.TZDate.addDuration", 1)),
    ("toLocaleDateString", AbsBuiltinFunc("tizen.TZDate.toLocaleDateString", 0)),
    ("toLocaleTimeString", AbsBuiltinFunc("tizen.TZDate.toLocaleTimeString", 0)),
    ("toLocaleString", AbsBuiltinFunc("tizen.TZDate.toLocaleString", 0)),
    ("toDateString", AbsBuiltinFunc("tizen.TZDate.toDateString", 0)),
    ("toTimeString", AbsBuiltinFunc("tizen.TZDate.toTimeString", 0)),
    ("toString", AbsBuiltinFunc("tizen.TZDate.toString", 0)),
    ("getTimezoneAbbreviation", AbsBuiltinFunc("tizen.TZDate.getTimezoneAbbreviation", 0)),
    ("secondsFromUTC", AbsBuiltinFunc("tizen.TZDate.secondsFromUTC", 0)),
    ("isDST", AbsBuiltinFunc("tizen.TZDate.isDST", 0)),
    ("getPreviousDSTTransition", AbsBuiltinFunc("tizen.TZDate.getPreviousDSTTransition", 0)),
    ("getNextDSTTransition", AbsBuiltinFunc("tizen.TZDate.getNextDSTTransition", 0))
  )

  private val prop_tzdate_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENTZDate.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T)))
  )

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto), (loc_tzdate, prop_tzdate_ins)
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.TZDate.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          //val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          //val v_1 = getArgValue(h_2, ctx_2, args, "0")
          //val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENTZDate.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop)))
          val h_2 = h_1.update(l_r1, o_new)

          //val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he, ctxe))
        }
        )),
      ("tizen.TZDate.getDate" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setDate" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getDay" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.getFullYear" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setFullYear" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getHours" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setHours" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getMilliseconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setMilliseconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getMinutes" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setMinutes" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getMonth" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setMonth" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getSeconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.getUTCDate" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setUTCDate" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getUTCDay" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.getUTCFullYear" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setUTCFullYear" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getUTCHours" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setUTCHours" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getUTCMilliseconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setUTCMilliseconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getUTCMinutes" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setUTCMinutes" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getUTCMonth" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setUTCMonth" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getUTCSeconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.setUTCSeconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.getTimezone" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.toTimezone" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._4 <= NumBot)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h, Value(loc_tzdate)), ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.toLocalTimezone" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(loc_tzdate)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.toUTC" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(loc_tzdate)), ctx), (he, ctxe))
        }
        )),
/*      ("tizen.TZDate.difference" -> (

        )),*/
      ("tizen.TZDate.equalsTo" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v, Value(TIZENTZDate.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((Helper.ReturnStore(h, Value(BoolTop)), ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.earlierThan" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v, Value(TIZENTZDate.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((Helper.ReturnStore(h, Value(BoolTop)), ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.laterThan" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v, Value(TIZENTZDate.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((Helper.ReturnStore(h, Value(BoolTop)), ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.addDuration" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v, Value(TIZENTimeDuration.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((Helper.ReturnStore(h, Value(loc_tzdate)), ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.TZDate.toLocaleDateString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.toLocaleTimeString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.toLocaleString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.toDateString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.toTimeString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.toString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.getTimezoneAbbreviation" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.secondsFromUTC" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.isDST" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(BoolTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.getPreviousDSTTransition" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(PValue(NullTop), LocSet(loc_tzdate))), ctx), (he, ctxe))
        }
        )),
      ("tizen.TZDate.getNextDSTTransition" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(PValue(NullTop), LocSet(loc_tzdate))), ctx), (he, ctxe))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}