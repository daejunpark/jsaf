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
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue

object TIZENAlarmAbsolute extends Tizen {
  val name = "AlarmAbsolute"
  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor */
  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(FunctionProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("tizen.AlarmAbsolute.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("id", AbsConstValue(PropValue(Value(UndefTop)))),
    ("date", AbsConstValue(PropValue(Value(UndefTop)))),
    ("period", AbsConstValue(PropValue(Value(UndefTop)))),
    ("daysOfTheWeek", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("getNextScheduledDate", AbsBuiltinFunc("tizen.AlarmAbsolute.getNextScheduledDate", 0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.AlarmAbsolute.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r = addrToLoc(addr1, Recent)
          val l_r2 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = Helper.Oldify(h_1, ctx_1, addr2)
          val v_1 = getArgValue(h_2, ctx_2, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))

          val es_1 =
            if (v_1._1 <= PValueTop) {
              Set[WebAPIException](TypeMismatchError)
            }
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAlarmAbsolute.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(NullTop), F, T, T)))

          val (h_3, es_2) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(0))
              val h_3 = h_2.update(l_r, o_arr)
              // case for "new tizen.AlarmAbsolute(date)"
              val o_new2 = o_new.
                update("date", PropValue(ObjectValue(Value(v_1._2), F, T, T))).
                update("period", PropValue(ObjectValue(Value(NullTop), F, T, T))).
                update("daysOfTheWeek", PropValue(ObjectValue(Value(l_r), F, T, T)))
              (h_3.update(l_r2, o_new2), TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val v_2 = getArgValue(h_2, ctx_2, args, "1")
              if (v_2._1._4 </ NumBot) {
                // case for "new tizen.AlarmAbsolute(date, period)"
                val o_arr = Helper.NewArrayObject(AbsNumber.alpha(0))
                val h_3 = h_2.update(l_r, o_arr)
                val o_new2 = o_new.
                  update("date", PropValue(ObjectValue(Value(v_1._2), F, T, T))).
                  update("period", PropValue(ObjectValue(Value(v_2._1._4), F, T, T))).
                  update("daysOfTheWeek", PropValue(ObjectValue(Value(l_r), F, T, T)))
                (h_3.update(l_r2, o_new2), TizenHelper.TizenExceptionBot)
              }
              else {
                // case for "new tizen.AlarmAbsolute(date, daysOfTheWeek)"
                val es =
                  if (v_2._2.exists((l) => Helper.IsArray(h_2, l) <= F))
                    Set[WebAPIException](TypeMismatchError)
                  else TizenHelper.TizenExceptionBot
                val o_new2 = o_new.
                  update("date", PropValue(ObjectValue(Value(v_1._2), F, T, T))).
                  update("period", PropValue(ObjectValue(Value(NullTop), F, T, T))).
                  update("daysOfTheWeek", PropValue(ObjectValue(Value(v_2._2), F, T, T)))
                (h_2.update(l_r2, o_new2), es)
              }
            case _ => {
              (h_2, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es_1 ++ es_2)
          ((Helper.ReturnStore(h_3, Value(l_r2)), ctx_2), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.AlarmAbsolute.getNextScheduledDate" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val h_1 =
            if (lset_this.exists((l) => h(l)("id")._1._2._1._5 <= StrTop)) {
              Helper.ReturnStore(h, Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)))
            }
            else {
              Helper.ReturnStore(h, Value(NullTop))
            }
          ((h_1, ctx), (he, ctxe))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.AlarmAbsolute.getNextScheduledDate" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        ))
    )
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("tizen.AlarmAbsolute.getNextScheduledDate" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }
        ))
    )
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map(
      ("tizen.AlarmAbsolute.getNextScheduledDate" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }
        ))
    )
  }
}