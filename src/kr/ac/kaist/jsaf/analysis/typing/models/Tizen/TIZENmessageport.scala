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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import java.lang.InternalError
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc

object TIZENmessageport extends Tizen {
  private val name = "messageport"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_messageport
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
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("requestLocalMessagePort", AbsBuiltinFunc("tizen.messageport.requestLocalMessagePort", 1)),
    ("requestTrustedLocalMessagePort", AbsBuiltinFunc("tizen.messageport.requestTrustedLocalMessagePort", 1)),
    ("requestRemoteMessagePort", AbsBuiltinFunc("tizen.messageport.requestRemoteMessagePort", 2)),
    ("requestTrustedRemoteMessagePort", AbsBuiltinFunc("tizen.messageport.requestTrustedRemoteMessagePort", 2))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.messageport.requestLocalMessagePort" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val v = getArgValue(h_1, ctx_1, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))
          val es =
            if (n_arglen == 0)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENLocalMessagePort.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("messagePortName", PropValue(ObjectValue(Value(v._1._5), F, T, T))).
            update("isTrusted", PropValue(ObjectValue(Value(BoolTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.messageport.requestTrustedLocalMessagePort" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val v = getArgValue(h_1, ctx_1, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))
          val es =
            if (n_arglen == 0)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENLocalMessagePort.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("messagePortName", PropValue(ObjectValue(Value(v._1._5), F, T, T))).
            update("isTrusted", PropValue(ObjectValue(Value(T), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.messageport.requestRemoteMessagePort" -> (
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
          val es =
            if (n_arglen == 0)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v_1._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_2 =
            if (v_2._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENRemoteMessagePort.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("messagePortName", PropValue(ObjectValue(Value(v_2._1._5), F, T, T))).
            update("appId", PropValue(ObjectValue(Value(v_1._1._5), F, T, T))).
            update("isTrusted", PropValue(ObjectValue(Value(BoolTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ es_2)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.messageport.requestTrustedRemoteMessagePort" -> (
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
          val es =
            if (n_arglen == 0)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v_1._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_2 =
            if (v_2._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENRemoteMessagePort.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("messagePortName", PropValue(ObjectValue(Value(v_2._1._5), F, T, T))).
            update("appId", PropValue(ObjectValue(Value(v_1._1._5), F, T, T))).
            update("isTrusted", PropValue(ObjectValue(Value(T), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ es_2)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}

object TIZENLocalMessagePort extends Tizen {
  private val name = "LocalMessagePort"
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
    ("addMessagePortListener", AbsBuiltinFunc("tizen.LocalMessagePort.addMessagePortListener", 1)),
    ("removeMessagePortListener", AbsBuiltinFunc("tizen.LocalMessagePort.removeMessagePortListener", 1))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
    /*  ("tizen.LocalMessagePort.addMessagePortListener" -> ()),
      ("tizen.LocalMessagePort.removeMessagePortListener" -> ())*/
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}

object TIZENRemoteMessagePort extends Tizen {
  private val name = "RemoteMessagePort"
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
    ("sendMessage", AbsBuiltinFunc("tizen.RemoteMessagePort.sendMessage", 2))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.RemoteMessagePort.sendMessage" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val es =
            if (n_arglen == 0)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v_1._2.exists((l) => Helper.IsArray(h,l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val es_2 = v_1._2.foldLeft(TizenHelper.TizenExceptionBot)((_es, ll) => {
            val n_length = Operator.ToUInt32(Helper.Proto(h, ll, AbsString.alpha("length")))
            val ess = n_length match {
              case NumBot =>
                TizenHelper.TizenExceptionBot
              case UIntSingle(n) => {
                val es__ = (0 until n.toInt).foldLeft(TizenHelper.TizenExceptionBot)((_e, i) => {
                  val vi = Helper.Proto(h, ll, AbsString.alpha(i.toString))
                  val esi =
                    if (vi._1._5 </ StrTop) Set[WebAPIException](TypeMismatchError)
                    else TizenHelper.TizenExceptionBot
                  _e ++ esi
                })
                es__
              }
              case _ => {
                val vi = Helper.Proto(h, ll, AbsString.alpha("@default_number"))
                val esi =
                  if (vi._1._5 </ StrTop) Set[WebAPIException](TypeMismatchError)
                  else TizenHelper.TizenExceptionBot
                esi
              }
            }
            _es ++ ess
          })

          val (h_1, es_3) = n_arglen match {
            case UIntSingle(n) if n == 2 =>
              val v_2 = getArgValue(h, ctx, args, "1")
              val (b_1, es_4) = TizenHelper.instanceOf(h, v_2, Value(TIZENLocalMessagePort.loc_proto))
              val es_5 =
                if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              (h, es_4 ++ es_5)
            case _ => (h, TizenHelper.TizenExceptionBot)
          }

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ es_2 ++ es_3)
          ((h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}