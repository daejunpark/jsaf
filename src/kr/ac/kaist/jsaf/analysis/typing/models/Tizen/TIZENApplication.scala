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
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue

object TIZENApplication extends Tizen {
  val name = "Application"
  /* predefined locations */
  val loc_obj = newPredefLoc(name + "Obj")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor */
  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("appInfo", AbsConstValue(PropValue(Value(UndefTop)))),
    ("contextId", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("exit", AbsBuiltinFunc("tizen.Application.exit", 0)),
    ("hide", AbsBuiltinFunc("tizen.Application.hide", 0)),
    ("getRequestedAppControl", AbsBuiltinFunc("tizen.Application.getRequestedAppControl", 0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
        ("tizen.Application.exit" -> (
          (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
            /* make context undefined */
            ((h, ctx), (he, ctxe))
          }
          )),
        ("tizen.Application.hide" -> (
          (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
            /* make context undefined */
            ((h, ctx), (he, ctxe))
          }
          )),
        ("tizen.Application.getRequestedAppControl" -> (
          (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
            val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
            val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
            if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
            val addr_env = set_addr.head
            val addr1 = cfg.getAPIAddress(addr_env, 0)
            val addr2 = cfg.getAPIAddress(addr_env, 1)
            val addr3 = cfg.getAPIAddress(addr_env, 2)
            val addr4 = cfg.getAPIAddress(addr_env, 3)
            val addr5 = cfg.getAPIAddress(addr_env, 4)
            val l_r = addrToLoc(addr1, Recent)
            val l_r2 = addrToLoc(addr2, Recent)
            val l_r3 = addrToLoc(addr3, Recent)
            val l_r4 = addrToLoc(addr4, Recent)
            val l_r5 = addrToLoc(addr5, Recent)
            val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
            val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)
            val (h_3, ctx_3) = Helper.Oldify(h_2, ctx_2, addr3)
            val (h_4, ctx_4) = Helper.Oldify(h_3, ctx_3, addr4)
            val (h_5, ctx_5) = Helper.Oldify(h_4, ctx_4, addr5)

            val o_sarr = Helper.NewArrayObject(UInt)
            val o_sarr2 = o_sarr.update("@default_number", PropValue(ObjectValue(Value(StrTop), T, T, T)))
            val h_6 = h_5.update(l_r, o_sarr2)

            val o_new = ObjEmpty.
              update("@class", PropValue(AbsString.alpha("Object"))).
              update("@proto", PropValue(ObjectValue(Value(TIZENApplicationControlData.loc_proto), F, F, F))).
              update("@extensible", PropValue(T)).
              update("@scope", PropValue(Value(NullTop))).
              update("@hasinstance", PropValue(Value(NullTop))).
              update("key", PropValue(ObjectValue(Value(StrTop), F, T, T))).
              update("value", PropValue(ObjectValue(Value(l_r), F, T, T)))
            val h_7 = h_6.update(l_r2, o_new)
            val o_arr = Helper.NewArrayObject(UInt)
            val o_arr2 = o_arr.update("@default_number", PropValue(ObjectValue(l_r2, T, T, T)))
            val h_8 = h_7.update(l_r3, o_arr2)

            val o_new1 = ObjEmpty.
              update("@class", PropValue(AbsString.alpha("Object"))).
              update("@proto", PropValue(ObjectValue(Value(TIZENApplicationControl.loc_proto), F, F, F))).
              update("@extensible", PropValue(T)).
              update("@scope", PropValue(Value(NullTop))).
              update("@hasinstance", PropValue(Value(NullTop))).
              update("operation", PropValue(ObjectValue(Value(StrTop), F, T, T))).
              update("uri", PropValue(ObjectValue(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop), F, T, T))).
              update("mime", PropValue(ObjectValue(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop), F, T, T))).
              update("category", PropValue(ObjectValue(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop), F, T, T))).
              update("data", PropValue(ObjectValue(Value(l_r3), F, T, T)))
            val h_9 = h_8.update(l_r4, o_new1)

            val o_new2 = ObjEmpty.update("@class", PropValue(AbsString.alpha("Object"))).
              update("@proto", PropValue(ObjectValue(Value(TIZENRequestedApplicationControl.loc_proto), F, F, F))).
              update("@extensible", PropValue(T)).
              update("@scope", PropValue(Value(NullTop))).
              update("@hasinstance", PropValue(ObjectValue(Value(NullTop), F, F, F))).
              update("appControl", PropValue(ObjectValue(Value(l_r4), F, T, T))).
              update("callerAppId", PropValue(ObjectValue(Value(StrTop), F, T, T)))
            val h_10 = h_9.update(l_r5, o_new2)
            ((Helper.ReturnStore(h_10, Value(l_r5)), ctx_5), (he, ctxe))
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

object TIZENApplicationInformation extends Tizen {
  val name = "ApplicationInformation"
  /* predefined locations */
  val loc_obj = newPredefLoc(name + "Obj")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor */
  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("id", AbsConstValue(PropValue(Value(StrTop)))),
    ("name", AbsConstValue(PropValue(Value(StrTop)))),
    ("iconPath", AbsConstValue(PropValue(Value(StrTop)))),
    ("version", AbsConstValue(PropValue(Value(StrTop)))),
    ("show", AbsConstValue(PropValue(Value(BoolTop)))),
    ("categories", AbsConstValue(PropValue(Value(StrTop)))),
    ("installDate", AbsConstValue(PropValue(Value(UndefTop)))),
    ("size", AbsConstValue(PropValue(Value(NumTop)))),
    ("packageId", AbsConstValue(PropValue(Value(StrTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T)))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map()
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

object TIZENRequestedApplicationControl extends Tizen {
  val name = "RequestedApplicationControl"
  /* predefined locations */
 // val loc_obj = newPredefLoc(name + "Obj")
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_proto, prop_proto)
  )
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("replyResult", AbsBuiltinFunc("tizen.RequestedApplicationControl.replyResult", 1)),
    ("replyFailure", AbsBuiltinFunc("tizen.RequestedApplicationControl.replyFailure", 0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.RequestedApplicationControl.replyResult" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val data = getArgValue(h, ctx, args, "0")
          val es =
            if (data._1 <= PValueTop && data._1._2 </ NullTop) {
              Set[WebAPIException](TypeMismatchError)
            }
            else TizenHelper.TizenExceptionBot

          val es2 =
            if (data._2.exists((l) =>  Helper.IsArray(h, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es2)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
      )),
      ("tizen.RequestedApplicationControl.replyFailure" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((h, ctx), (he, ctxe))
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