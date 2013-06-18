/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T, _}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.cfg.{CFGExpr, CFG}
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc

object TIZENalarm extends Tizen {
  val name = "alarm"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_alarm
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("PERIOD_MINUTE", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(60), F, T, T)))),
    ("PERIOD_HOUR", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(3600), F, T, T)))),
    ("PERIOD_DAY", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(86400), F, T, T)))),
    ("PERIOD_WEEK", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(604800), F, T, T))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("add", AbsBuiltinFunc("tizen.alarm.add",3)),
    ("remove", AbsBuiltinFunc("tizen.alarm.remove",1)),
    ("removeAll", AbsBuiltinFunc("tizen.alarm.removeAll",0)),
    ("get", AbsBuiltinFunc("tizen.alarm.get",1)),
    ("getAll", AbsBuiltinFunc("tizen.alarm.getAll",0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.alarm.add" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val v_2 = getArgValue(h, ctx, args, "1")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val (b_1, es_1) = TizenHelper.instanceOf(h, v_1, Value(TIZENAlarmAbsolute.loc_proto))
          val (b_2, es_2) = TizenHelper.instanceOf(h, v_1, Value(TIZENAlarmRelative.loc_proto))
          val es_3 =
            if (b_1._1._3 <= F && b_2._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_4 =
            if (v_2._1._5 </ StrTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_5 = n_arglen match {
            case UIntSingle(n) if n >= 3 =>
              val v_3 = getArgValue(h, ctx, args, "2")
              val (b_3, es_5) = TizenHelper.instanceOf(h, v_3, Value(TIZENApplicationControl.loc_proto))
              val es_6 =
                if (b_3._1._3 <= F) Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              es_5 ++ es_6
            case _ =>
              TizenHelper.TizenExceptionBot
          }
          // update alarm id
          val h_1 = v_1._2.foldLeft(HeapBot)((_h, l) => {
            (_h + h.update(l, h(l).update(AbsString.alpha("id"), PropValue(ObjectValue(Value(StrTop), T, T, T)))))
          })
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es_1 ++ es_2 ++ es_3 ++ es_4 ++ es_5)
          ((h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.alarm.remove" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val es =
            if (v._1._2 == NullTop || v._1._1 == UndefTop) Set[WebAPIException](InvalidValuesError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.alarm.removeAll" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((h, ctx), (he, ctxe))
        }
        )),
      ("tizen.alarm.get" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)

          val alarmId = getArgValue(h, ctx_1, args, "0")
          val es =
            if (alarmId._1._2 == NullTop || alarmId._1._1 == UndefTop) Set[WebAPIException](InvalidValuesError)
            else TizenHelper.TizenExceptionBot

          /* New TIZENAlarmAbsolute */
          val o_1 = ObjEmpty.update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAlarmAbsolute.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(alarmId._1._5), F, T, T))).
            update("date", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("period", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("daysOfTheWeek", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          /* New TIZENAlarmRelative */
          val o_2 = ObjEmpty.update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAlarmRelative.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(alarmId._1._5), F, T, T))).
            update("delay", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("period", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          val h_2 = h_1.update(l_r1, o_1 + o_2)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h_2, ctx, es)
          ((Helper.ReturnStore(h_2, Value(PValue(UndefTop),LocSet(l_r1))), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.alarm.getAll" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r1 = addrToLoc(addr1, Recent)
          val l_r2 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)

          /* New TIZENAlarmAbsolute */
          val o_1 = ObjEmpty.update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAlarmAbsolute.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("date", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("period", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("daysOfTheWeek", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          /* New TIZENAlarmRelative */
          val o_2 = ObjEmpty.update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAlarmRelative.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("delay", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("period", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          val h_3 = h_2.update(l_r1, o_1 + o_2)
          val o_3 = Helper.NewArrayObject(UInt)
          val o_4 = o_3.update("@default_number", PropValue(ObjectValue(Value(l_r1), T, T, T)))
          val h_4 = h_3.update(l_r2, o_4)

          ((Helper.ReturnStore(h_4, Value(l_r2)), ctx_2), (he, ctxe))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.alarm.removeAll" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((h, ctx), (he, ctxe))
        }
        ))
    )
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("tizen.alarm.removeAll" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }
        ))
    )
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map(
      ("tizen.alarm.removeAll" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }
        ))
    )
  }
}