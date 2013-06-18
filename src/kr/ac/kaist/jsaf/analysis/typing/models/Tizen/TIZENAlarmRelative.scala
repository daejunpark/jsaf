/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/
package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue

object TIZENAlarmRelative extends Tizen {
  val name = "AlarmRelative"
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
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("tizen.AlarmRelative.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("id", AbsConstValue(PropValue(Value(UndefTop)))),
    ("delay", AbsConstValue(PropValue(Value(UndefTop)))),
    ("period", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("getRemainingSeconds", AbsBuiltinFunc("tizen.AlarmRelative.getRemainingSeconds", 0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.AlarmRelative.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val delay = getArgValue(h_1, ctx_1, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

          val es =
            if (delay._1 </ PValueTop) {
              Set[WebAPIException](TypeMismatchError)
            }
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAlarmRelative.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(NullTop), T, T, T)))


          val (h_2, es1) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              // case for "new tizen.AlarmRelative(delay)"
              val o_new2 = o_new.
                update("delay", PropValue(ObjectValue(Value(delay._1._4), F, T, T))).
                update("period", PropValue(ObjectValue(Value(NullTop), T, T, T)))
              (h_1.update(l_r, o_new2), TizenHelper.TizenExceptionBot)

            case UIntSingle(n) if n == 2 =>
              val period = getArgValue(h_1, ctx_1, args, "1")
              // case for "new tizen.AlarmRelative(delay, period)"
              val o_new3 = o_new.
                update("delay", PropValue(ObjectValue(Value(delay._1._4), F, T, T))).
                update("period", PropValue(ObjectValue(Value(period._1._4), F, T, T)))
              (h_1.update(l_r, o_new3), TizenHelper.TizenExceptionBot)
            case _ => {
              (h_1, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es1)
          ((Helper.ReturnStore(h_2, Value(l_r)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.AlarmRelative.getRemainingSeconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val h_1 =
            if (lset_this.exists((l) => h(l)("id")._1._2._1._4 <= NumTop)) {
              Helper.ReturnStore(h, Value(PValue(UndefBot, NullTop, BoolBot, NumTop, StrBot)))
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
      ("tizen.AlarmRelative.getRemainingSeconds" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        ))
    )
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("tizen.AlarmRelative.getRemainingSeconds" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }
        ))
    )
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map(
      ("tizen.AlarmRelative.getRemainingSeconds" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }
        ))
    )
  }
}