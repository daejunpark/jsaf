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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import java.lang.InternalError

object TIZENSyncProfileInfo extends Tizen {
  private val name = "SyncProfileInfo"
  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")
  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("tizen.SyncProfileInfo.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("profileId", AbsConstValue(PropValue(Value(UndefTop)))),
    ("profileName", AbsConstValue(PropValue(Value(UndefTop)))),
    ("syncInfo", AbsConstValue(PropValue(Value(UndefTop)))),
    ("serviceInfo", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue)))
  )

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto)
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.SyncProfileInfo.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val v_1 = getArgValue(h_1, ctx_1, args, "0")
          val v_2 = getArgValue(h_1, ctx_1, args, "1")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))
          val es_1 =
            if (v_1._1._5 <= StrBot) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (b_1, es_2) = TizenHelper.instanceOf(h_1, v_2, Value(TIZENSyncInfo.loc_proto))
          val es_3 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENSyncProfileInfo.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("profileId", PropValue(ObjectValue(Value(NullTop), F, T, T))).
            update("profileName", PropValue(ObjectValue(Value(v_1._1._5), F, T, T))).
            update("syncInfo", PropValue(ObjectValue(Value(v_2._2), F, T, T)))
          val (h_2, es) = n_arglen match {
            case UIntSingle(n) if n <= 1 =>
              (h_1, Set[WebAPIException](TypeMismatchError))
            case UIntSingle(n) if n == 2 =>
              val o_new2 = o_new.
                update("serviceInfo", PropValue(ObjectValue(Value(NullTop), F, T, T)))
              val h_2 = h_1.update(l_r1, o_new2)
              (h_2, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n >= 3 =>
              val v_3 = getArgValue(h_1, ctx_1, args, "2")
              val es1 = v_3._2.foldLeft(TizenHelper.TizenExceptionBot)((_e, ll) => {
                val n_length = Operator.ToUInt32(Helper.Proto(h_1, ll, AbsString.alpha("length")))
                val es2 = n_length match {
                  case NumBot => _e
                  case UIntSingle(n) => {
                    val e__ = (0 until n.toInt).foldLeft(_e)((__e, i) => {
                      val vi = Helper.Proto(h_1, ll, AbsString.alpha(i.toString))
                      val (b_2, es3) = TizenHelper.instanceOf(h_1, vi, Value(TIZENSyncServiceInfo.loc_proto))
                      val es4 =
                        if (b_2._1._3 <= F)
                          Set[WebAPIException](TypeMismatchError)
                        else TizenHelper.TizenExceptionBot
                      __e ++ es3 ++ es4
                    })
                    e__
                  }
                  case _ => {
                    val vi = Helper.Proto(h_1, ll, AbsString.alpha("@default_number"))
                    val (b_2, es3) = TizenHelper.instanceOf(h_1, vi, Value(TIZENSyncServiceInfo.loc_proto))
                    val es4 =
                      if (b_2._1._3 <= F)
                        Set[WebAPIException](TypeMismatchError)
                      else TizenHelper.TizenExceptionBot
                    es3 ++ es4
                  }
                }
                _e ++ es2
              })
              val o_new2 = o_new.
                update("serviceInfo", PropValue(ObjectValue(Value(v_3._2), F, T, T)))
              val h_2 = h_1.update(l_r1, o_new2)
              (h_2, es1)
            case _ => (h_1, TizenHelper.TizenExceptionBot)
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ es_2 ++ es_3)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}