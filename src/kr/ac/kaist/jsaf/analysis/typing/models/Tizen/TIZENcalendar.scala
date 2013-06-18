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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import java.lang.InternalError
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.builtin.BuiltinArray
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap

object TIZENcalendarObj extends Tizen {
  val name = "calendar"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_calendar
  val loc_proto = newPredefLoc(name + "Proto")

  val loc_tzdate: Loc        = newPreDefLoc("TZDate", Old)
  val loc_timedur: Loc       = newPreDefLoc("TimeDuration", Old)
  val loc_simplecoordi: Loc  = newPreDefLoc("SimpleCoordinate", Old)
  val loc_calalarm: Loc      = newPreDefLoc("CalendarAlarm", Old)
  val loc_calattend: Loc     = newPreDefLoc("CalendarAttendee", Old)
  val loc_calrecur: Loc      = newPreDefLoc("CalendarRecurrenceRule", Old)
  val loc_contref: Loc       = newPreDefLoc("ContactRef", Old)
  val loc_bydayvalarr: Loc       = newPreDefLoc("ByDayValueArr", Old)
  val loc_shortarr: Loc       = newPreDefLoc("ShortArr", Old)
  val loc_tzdatearr: Loc       = newPreDefLoc("TZDateArr", Old)

  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("getCalendars", AbsBuiltinFunc("tizen.calendar.getCalendars",3)),
    ("getUnifiedCalendar", AbsBuiltinFunc("tizen.calendar.getUnifiedCalendar",1)),
    ("getDefaultCalendar", AbsBuiltinFunc("tizen.calendar.getDefaultCalendar",1)),
    ("getCalendar", AbsBuiltinFunc("tizen.calendar.getCalendar",2))
  )

  private val prop_tzdate_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENTZDate.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T)))
  )
  private val prop_timedur_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENTimeDuration.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("length",               AbsConstValue(PropValue(Value(NumTop)))),
    ("unit",                 AbsConstValue(PropValue(Value(AbsString.alpha("MSECS") + AbsString.alpha("SECS") +
                                                          AbsString.alpha("MINS") + AbsString.alpha("HOURS") + AbsString.alpha("DAYS")))))
  )
  private val prop_simplecoordi_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENSimpleCoordinates.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("latitude", AbsConstValue(PropValue(Value(NumTop)))),
    ("longitude", AbsConstValue(PropValue(Value(NumTop))))
  )
  private val prop_calalarm_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENCalendarAlarm.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("absoluteDate", AbsConstValue(PropValue(Value(loc_tzdate)))),
    ("before", AbsConstValue(PropValue(Value(loc_timedur)))),
    ("method", AbsConstValue(PropValue(Value(AbsString.alpha("SOUND") + AbsString.alpha("DISPLAY"))))),
    ("description", AbsConstValue(PropValue(Value(StrTop))))
  )
  private val prop_calattend_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENCalendarAttendee.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("uri", AbsConstValue(PropValue(Value(StrTop)))),
    ("name", AbsConstValue(PropValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop))))),
    ("role", AbsConstValue(PropValue(Value(AbsString.alpha("REQ_PARTICIPANT") + AbsString.alpha("OPT_PARTICIPANT") +
                                          AbsString.alpha("NON_PARTICIPANT") + AbsString.alpha("CHAIR"))))),
    ("status", AbsConstValue(PropValue(Value(AbsString.alpha("PENDING") + AbsString.alpha("ACCEPTED") +
                                            AbsString.alpha("DECLINED") + AbsString.alpha("TENTATIVE") +
                                            AbsString.alpha("DELEGATED") + AbsString.alpha("COMPLETED") +
                                            AbsString.alpha("IN_PROCESS"))))),
    ("RSVP", AbsConstValue(PropValue(Value(BoolTop)))),
    ("type", AbsConstValue(PropValue(Value(AbsString.alpha("INDIVIDUAL") + AbsString.alpha("GROUP") +
                                          AbsString.alpha("RESOURCE") + AbsString.alpha("ROOM") +
                                          AbsString.alpha("UNKNOWN"))))),
    ("group", AbsConstValue(PropValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop))))),
    ("delegatorURI", AbsConstValue(PropValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop))))),
    ("delegateURI", AbsConstValue(PropValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop))))),
    ("contactRef", AbsConstValue(PropValue(Value(loc_contref))))
  )
  private val prop_calrecur_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENCalendarRecurrenceRule.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("frequency", AbsConstValue(PropValue(Value(AbsString.alpha("DAILY") + AbsString.alpha("WEEKLY") +
                                              AbsString.alpha("MONTHLY") + AbsString.alpha("YEARLY"))))),
    ("interval", AbsConstValue(PropValue(Value(NumTop)))),
    ("untilDate", AbsConstValue(PropValue(Value(loc_tzdate)))),
    ("occurrenceCount", AbsConstValue(PropValue(Value(NumTop)))),
    ("daysOfTheWeek", AbsConstValue(PropValue(Value(loc_bydayvalarr)))),
    ("setPositions", AbsConstValue(PropValue(Value(loc_shortarr)))),
    ("exceptions", AbsConstValue(PropValue(Value(loc_tzdatearr))))
  )
  private val prop_contref_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENContactRef.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("addressBookId", AbsConstValue(PropValue(Value(StrTop)))),
    ("contactId", AbsConstValue(PropValue(Value(StrTop))))
  )
  private val prop_bydayvalarr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(AbsString.alpha("MO") + AbsString.alpha("TU") +
                                                                  AbsString.alpha("WE") + AbsString.alpha("TH") +
                                                                  AbsString.alpha("FR") + AbsString.alpha("SA") +
                                                                  AbsString.alpha("SU")), T, T, T))))
  )
  private val prop_shortarr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(NumTop), T, T, T))))
  )
  private val prop_tzdatearr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(loc_tzdate), T, T, T))))
  )
  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto), (loc_tzdate, prop_tzdate_ins), (loc_timedur, prop_timedur_ins),
    (loc_simplecoordi, prop_simplecoordi_ins), (loc_calalarm, prop_calalarm_ins), (loc_calattend, prop_calattend_ins),
    (loc_calrecur, prop_calrecur_ins), (loc_contref, prop_contref_ins), (loc_bydayvalarr, prop_bydayvalarr_ins),
    (loc_shortarr, prop_shortarr_ins), (loc_tzdatearr, prop_tzdatearr_ins)
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("tizen.calendar.getCalendars" -> ()),*/
      ("tizen.calendar.getUnifiedCalendar" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val caltype = getArgValue(h_1, ctx_1, args, "0")
          val es =
            if (caltype._1._5 </ AbsString.alpha("EVENT") || caltype._1._5 </ AbsString.alpha("TASK"))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENCalendar.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.calendar.getDefaultCalendar" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val caltype = getArgValue(h_1, ctx_1, args, "0")
          val es =
            if (caltype._1._5 </ AbsString.alpha("EVENT") || caltype._1._5 </ AbsString.alpha("TASK"))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENCalendar.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.calendar.getCalendar" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val caltype = getArgValue(h_1, ctx_1, args, "0")
          val id = getArgValue(h_1, ctx_1, args, "1")
          val es =
            if (caltype._1._5 </ AbsString.alpha("EVENT") || caltype._1._5 </ AbsString.alpha("TASK"))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENCalendar.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(Helper.toString(id._1)), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_2, Value(PValue(UndefTop), LocSet(l_r1))), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map()
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map()
  }
}

object TIZENCalendar extends Tizen {
  val name = "Calendar"
  /* predefined locations */
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_proto, prop_proto)
  )
  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("get", AbsBuiltinFunc("tizen.Calendar.get", 1)),
    ("add", AbsBuiltinFunc("tizen.Calendar.add", 1)),
    ("addBatch", AbsBuiltinFunc("tizen.Calendar.addBatch", 3)),
    ("update", AbsBuiltinFunc("tizen.Calendar.update", 2)),
    ("updateBatch", AbsBuiltinFunc("tizen.Calendar.updateBatch", 4)),
    ("remove", AbsBuiltinFunc("tizen.Calendar.remove", 1)),
    ("removeBatch", AbsBuiltinFunc("tizen.Calendar.removeBatch", 3)),
    ("find", AbsBuiltinFunc("tizen.Calendar.find", 4)),
    ("addChangeListener", AbsBuiltinFunc("tizen.Calendar.addChangeListener", 1)),
    ("removeChangeListener", AbsBuiltinFunc("tizen.Calendar.removeChangeListener", 1))
  )
  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("tizen.Calendar.get" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
      )),*/
      ("tizen.Calendar.add" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val v_1 = getArgValue(h_1, ctx_1, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h_1, v_1, Value(TIZENCalendarItem.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENCalendarEventId.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("rid", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("uid", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val h_3 = v_1._2.foldLeft(h_2)((_h, l) => {
            _h + Helper.PropStore(_h, l, AbsString.alpha("id"), Value(PValue(StrTop), LocSet(l_r1)))
          })
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_3, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
/*      ("tizen.Calendar.addBatch" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),*/
      ("tizen.Calendar.update" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val (b_1, es) = TizenHelper.instanceOf(h, v_1, Value(TIZENCalendarItem.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_2 = n_arglen match {
            case UIntSingle(n) if n == 2 =>
              val v_2 = getArgValue(h, ctx, args, "1")
              val es_3 =
                if (v_2._1._3 </ BoolTop) Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              es_3
            case _ => {
              TizenHelper.TizenExceptionBot
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ es_2)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
/*      ("tizen.Calendar.updateBatch" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),*/
      ("tizen.Calendar.remove" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v_1, Value(TIZENCalendarEventId.loc_proto))
          val es_1 =
            if (v_1._1._5 </ StrTop && b_1._1._3 <= F)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
/*      ("tizen.Calendar.removeBatch" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),
      ("tizen.Calendar.find" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),
      ("tizen.Calendar.addChangeListener" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),*/
      ("tizen.Calendar.removeChangeListener" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val es =
            if (v_1._1._4 </ NumTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        ))
    )
  }
  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map()
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map()
  }
}