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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
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
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc

object TIZENapplicationObj extends Tizen {
  val name = "application"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_application
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(LocSet(GlobalLoc))))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("getCurrentApplication", AbsBuiltinFunc("tizen.applicationObj.getCurrentApplication",0)),
    ("kill", AbsBuiltinFunc("tizen.applicationObj.kill",3)),
    ("launch", AbsBuiltinFunc("tizen.applicationObj.launch",3)),
    ("launchAppControl", AbsBuiltinFunc("tizen.applicationObj.launchAppControl",5)),
    ("findAppControl", AbsBuiltinFunc("tizen.applicationObj.findAppControl",3)),
    ("getAppsContext", AbsBuiltinFunc("tizen.applicationObj.getAppsContext",2)),
    ("getAppContext", AbsBuiltinFunc("tizen.applicationObj.getAppContext",1)),
    ("getAppsInfo", AbsBuiltinFunc("tizen.applicationObj.getAppsInfo",2)),
    ("getAppInfo", AbsBuiltinFunc("tizen.applicationObj.getAppInfo",1)),
    ("getAppCerts", AbsBuiltinFunc("tizen.applicationObj.getAppCerts",1)),
    ("addAppInfoEventListener", AbsBuiltinFunc("tizen.applicationObj.addAppInfoEventListener",1)),
    ("removeAppInfoEventListener", AbsBuiltinFunc("tizen.applicationObj.removeAppInfoEventListener",1))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.applicationObj.getCurrentApplication" -> (
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

          val o_appinfo = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENApplicationInformation.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("iconPath", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("version", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("show", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("categories", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("installDate", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("size", PropValue(ObjectValue(Value(NumTop), F, T, T))).
            update("packageId", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_3 = h_2.update(l_r1, o_appinfo)

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENApplication.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("appInfo", PropValue(ObjectValue(Value(l_r1), F, T, T))).
            update("contextId", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_4 = h_3.update(l_r2, o_new)
          ((Helper.ReturnStore(h_4, Value(l_r2)), ctx_2), (he, ctxe))
        }
        )),
      ("tizen.applicationObj.kill" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val ctxid = getArgValue(h,ctx,args,"0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val es =
            if (ctxid._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          /* register successCallback and errorCallback */
          val (h_1, es_1) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              (h,TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h,ctx,args,"1")

             /* val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot*/

              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register successCallback */ // TODO: not yet implemented
              val h_n = TizenHelper.addCallbackHandler(h, AbsString.alpha("successCB"), Value(locset), Value(UndefTop))
              (h_n, /*ess ++ */ess2)
            case UIntSingle(n) if n == 3 =>
              val sucCB = getArgValue(h,ctx,args,"1")
              val errCB = getArgValue(h,ctx,args,"2")

              val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              /* register successCallback and errorCallback*/ // TODO: not yet implemented
              TizenHelper.addCallbackHandler(h, AbsString.alpha("successCB"), Value(locset), Value(UndefTop))
              //TizenHelper.addCallbackHandler(h, AbsString.alpha("errorCB"), Value(locsete), Value(UndefTop))
              (h, ess ++ ese ++ ess2 ++ ese2)
            case _ => {
              (h, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.launch" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val appid = getArgValue(h,ctx,args,"0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val es =
            if (appid._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          /* register successCallback and errorCallback */
          val (h_1, es_1) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              (h,TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h,ctx,args,"1")

              val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register successCallback */ // TODO: not yet implemented
              (h, ess ++ ess2)
            case UIntSingle(n) if n == 3 =>
              val sucCB = getArgValue(h,ctx,args,"1")
              val errCB = getArgValue(h,ctx,args,"2")

              val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              /* register successCallback and errorCallback*/ // TODO: not yet implemented
              (h, ess ++ ese ++ ess2 ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.launchAppControl" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val appctrl = getArgValue(h,ctx,args,"0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val es =
            if (appctrl._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          /* register successCallback and errorCallback */
          val (h_1, es_1) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              (h,TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h,ctx,args,"1")

              val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register successCallback */ // TODO: not yet implemented
              (h, ess ++ ess2)
            case UIntSingle(n) if n == 3 =>
              val sucCB = getArgValue(h,ctx,args,"1")
              val errCB = getArgValue(h,ctx,args,"2")

              val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              /* register successCallback and errorCallback*/ // TODO: not yet implemented
              (h, ess ++ ese ++ ess2 ++ ese2)
            case UIntSingle(n) if n == 4 =>
              val sucCB = getArgValue(h,ctx,args,"1")
              val errCB = getArgValue(h,ctx,args,"2")
              val replyCB = getArgValue(h,ctx,args,"3")

              val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val esr =
                if (replyCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val locsetr = replyCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val esr2 =
                if (replyCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              /* register successCallback and errorCallback*/ // TODO: not yet implemented
              (h, ess ++ ese ++ esr ++ ess2 ++ ese2 ++ esr2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.findAppControl" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r = addrToLoc(addr1, Recent)
          val l_r1 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = Helper.Oldify(h_1, ctx_1, addr2)
          val appctrl = getArgValue(h_2, ctx_2, args, "0")
          val sucCB = getArgValue(h_2, ctx_2, args, "1")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))

          val es =
            if (appctrl._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val ess =
            if (sucCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot


          val o_new = ObjEmpty.update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENApplicationInformation.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(Value(StrTop))).
            update("name", PropValue(Value(StrTop))).
            update("iconPath", PropValue(Value(StrTop))).
            update("version", PropValue(Value(StrTop))).
            update("show", PropValue(Value(BoolTop))).
            update("categories", PropValue(Value(StrTop))).
            update("installDate", PropValue(Value(StrTop))).
            update("size", PropValue(Value(NumTop))).
            update("packageId", PropValue(Value(StrTop)))

          val h_3 = h_2.update(l_r, o_new)
          val o_arr = Helper.NewArrayObject(UInt)
          val o_arr2 = o_arr.update("@default_number", PropValue(ObjectValue(l_r, T, T, T)))
          val h_4 = h_3.update(l_r1, o_arr2)

          /* register success Callback and error Callback */
          val (h_5, es_2) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              val es_1 = Set[WebAPIException](TypeMismatchError)
              (h_4,es_1)
            case UIntSingle(n) if n == 2 =>
              /* register success callback */ // TODO: not yet implemented
              (h_4,TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 3 =>
              val errCB = getArgValue(h_4, ctx_2, args, "2")
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register success callback and error callback */ // TODO: not yet implemented
              (h_4, ese)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess ++ es_2)
          ((h_5, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.getAppsContext" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r = addrToLoc(addr1, Recent)
          val l_r1 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = Helper.Oldify(h_1, ctx_1, addr2)
          val sucCB = getArgValue(h_2, ctx_2, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))
          val es =
            if (sucCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
            if (Helper.IsCallable(h,l) <= T) _L + l
            else _L
          })

          val ess =
            if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(Value(StrTop))).
            update("appId", PropValue(Value(StrTop)))
          val h_3 = h_2.update(l_r, o_new)
          val o_arr = Helper.NewArrayObject(UInt)
          val o_arr2 = o_arr.update("@default_number", PropValue(ObjectValue(l_r, T, T, T)))
          val h_4 = h_3.update(l_r1, o_arr2)

          /* register success Callback and error Callback */
          val (h_5, es_2) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              /* register success callback */ // TODO: not yet implemented
              (h_4,TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val errCB = getArgValue(h_4, ctx_2, args, "1")
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val locset2 = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register success callback and error callback */ // TODO: not yet implemented
              (h_4, ese ++ ess2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess ++ es_2)
          ((h_5, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.getAppContext" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val ctxid = getArgValue(h_1, ctx_1, args, "0")

          val es =
            if (ctxid._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("appId", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          val h_2 = h_1.update(l_r, o_new)

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_2, Value(l_r)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.getAppsInfo" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r = addrToLoc(addr1, Recent)
          val l_r1 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = Helper.Oldify(h_1, ctx_1, addr2)
          val sucCB = getArgValue(h_2, ctx_2, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))
          val es =
            if (sucCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
            if (Helper.IsCallable(h,l) <= T) _L + l
            else _L
          })

          val ess =
            if (sucCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENApplicationInformation.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(Value(StrTop))).
            update("name", PropValue(Value(StrTop))).
            update("iconPath", PropValue(Value(StrTop))).
            update("version", PropValue(Value(StrTop))).
            update("show", PropValue(Value(BoolTop))).
            update("categories", PropValue(Value(StrTop))).
            update("installDate", PropValue(Value(StrTop))).
            update("size", PropValue(Value(NumTop))).
            update("packageId", PropValue(Value(StrTop)))

          val h_3 = h_2.update(l_r, o_new)
          val o_arr = Helper.NewArrayObject(UInt)
          val o_arr2 = o_arr.update("@default_number", PropValue(ObjectValue(l_r, T, T, T)))
          val h_4 = h_3.update(l_r1, o_arr2)

          /* register success Callback and error Callback */
          val (h_5, es_2) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              /* register success callback */ // TODO: not yet implemented
              (h_4,TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val errCB = getArgValue(h_4, ctx_2, args, "1")
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locset2 = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ess2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register success callback and error callback */ // TODO: not yet implemented
              (h_4, ese ++ ess2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess ++ es_2)
          ((h_5, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.getAppInfo" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val appid = getArgValue(h_1, ctx_1, args, "0")

          val es =
            if (appid._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENApplicationInformation.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("iconPath", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("version", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("show", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("categories", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("installDate", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("size", PropValue(ObjectValue(Value(NumTop), F, T, T))).
            update("packageId", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          val o_new2 =
            if (appid._1._2 <= NullTop || appid._1._1 <= UndefTop)
              o_new.update("id", PropValue(ObjectValue(Value(StrTop), F, T, T)))
            else
              o_new.update("id", PropValue(ObjectValue(Value(Helper.toString(appid._1)), F, T, T)))

          val h_2 = h_1.update(l_r, o_new2)

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_2, Value(l_r)), ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.getAppCerts" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r = addrToLoc(addr1, Recent)
          val l_r1 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = Helper.Oldify(h_1, ctx_1, addr2)
          val appid = getArgValue(h_2, ctx_2, args, "0")

          val es =
            if (appid._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(lset_env))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("type", PropValue(ObjectValue(Value(AbsString.alpha("AUTHOR_ROOT") +
                                            AbsString.alpha("AUTHOR_INTERMEDIATE") +
                                            AbsString.alpha("AUTHOR_SIGNER") +
                                            AbsString.alpha("DISTRIBUTOR_ROOT") +
                                            AbsString.alpha("DISTRIBUTOR_INTERMEDIATE") +
                                            AbsString.alpha("DISTRIBUTOR_SIGNER") +
                                            AbsString.alpha("DISTRIBUTOR2_ROOT") +
                                            AbsString.alpha("DISTRIBUTOR2_INTERMEDIATE") +
                                            AbsString.alpha("DISTRIBUTOR2_SIGNER")
                                          ), F, T, T))).
            update("value", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          val h_3 = h_2.update(l_r, o_new)
          val o_arr = Helper.NewArrayObject(UInt)
          val o_arr2 = o_arr.update("@default_number", PropValue(ObjectValue(l_r, T, T, T)))
          val h_4 = h_3.update(l_r1, o_arr2)

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_4, Value(l_r1)), ctx_2), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.addAppInfoEventListener" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val eventCB = getArgValue(h, ctx, args, "0")
          val es =
            if (eventCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val locset = eventCB._2.foldLeft(LocSetBot)((_L, l) => {
            if (Helper.IsCallable(h,l) <= T) _L + l
            else _L
          })

          val ess =
            if (eventCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          /* register event function */ // TODO: not yet implemented
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess)
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.applicationObj.removeAppInfoEventListener" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((h, ctx), (he, ctxe))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.applicationObj.removeAppInfoEventListener" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((h, ctx), (he, ctxe))
        }
        ))
    )
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("tizen.applicationObj.removeAppInfoEventListener" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }
        ))
    )
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map(
      ("tizen.applicationObj.removeAppInfoEventListener" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }
        ))
    )
  }
}