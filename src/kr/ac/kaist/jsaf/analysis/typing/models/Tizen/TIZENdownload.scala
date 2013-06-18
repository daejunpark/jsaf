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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import java.lang.InternalError

object TIZENdownload extends Tizen {
  private val name = "download"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_download
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
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("start", AbsBuiltinFunc("tizen.download.start", 2)),
    ("cancel", AbsBuiltinFunc("tizen.download.cancel", 1)),
    ("pause", AbsBuiltinFunc("tizen.download.pause", 1)),
    ("resume", AbsBuiltinFunc("tizen.download.resume", 1)),
    ("getState", AbsBuiltinFunc("tizen.download.getState", 1)),
    ("getDownloadRequest", AbsBuiltinFunc("tizen.download.getDownloadRequest", 1)),
    ("getMIMEType", AbsBuiltinFunc("tizen.download.getMIMEType", 1)),
    ("setListener", AbsBuiltinFunc("tizen.download.setListener", 2))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.download.start" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val v_1 = getArgValue(h_1, ctx_1, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))
          val (b_1, es_1) = TizenHelper.instanceOf(h_1, v_1, Value(TIZENDownloadRequest.loc_proto))
          val es_2 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_2, es_3) = n_arglen match {
            case UIntSingle(n) if n >= 2 =>
              val v_2 = getArgValue(h_1, ctx_1, args, "1")
              val es1 =
                if (v_2._2.exists((l) => Helper.IsCallable(h_1, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(3))
              val o_arr1 = o_arr.
                update("0", PropValue(ObjectValue(Value(NumTop), T, T, T))).
                update("1", PropValue(ObjectValue(Value(NumTop), T, T, T))).
                update("2", PropValue(ObjectValue(Value(NumTop), T, T, T)))
              val h_2 = h_1.update(l_r1, o_arr1)
            /*  val h_4 = TizenHelper.addCallbackHandler(h_3,AbsString.alpha("DownloadCB.onprogress"),v_2,)*/
              (h_2, es1)
            case _ => (h_1, TizenHelper.TizenExceptionBot)
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es_1 ++ es_2 ++ es_3)
          ((h_2, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.download.cancel" -> (
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
            if (n_arglen == AbsNumber.alpha(0)) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v._1._4 </ NumTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
          val o_arr1 = o_arr.
            update("0", PropValue(ObjectValue(Value(NumTop), T, T, T)))
          val h_2 = h_1.update(l_r1, o_arr1)
          /*val h_3 = TizenHelper.addCallbackHandler(h_2,AbsString.alpha("DownloadCB.oncanceled"),v_2,)*/
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_2, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.download.pause" -> (
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
            if (n_arglen == AbsNumber.alpha(0)) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v._1._4 </ NumTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
          val o_arr1 = o_arr.
            update("0", PropValue(ObjectValue(Value(NumTop), T, T, T)))
          val h_2 = h_1.update(l_r1, o_arr1)
          /*  val h_3 = TizenHelper.addCallbackHandler(h_2,AbsString.alpha("DownloadCB.onpaused"),v_2,)*/
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_2, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.download.resume" -> (
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
            if (n_arglen == AbsNumber.alpha(0)) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v._1._4 </ NumTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
          val o_arr1 = o_arr.
            update("0", PropValue(ObjectValue(Value(NumTop), T, T, T)))
          val h_2 = h_1.update(l_r1, o_arr1)
          /*  val h_3 = TizenHelper.addCallbackHandler(h_2,AbsString.alpha("DownloadCB.onprogress"),v_2,)*/
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_2, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.download.getState" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val es =
            if (n_arglen == AbsNumber.alpha(0)) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v._1._4 </ NumTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val v1 = Value(AbsString.alpha("QUEUED") + AbsString.alpha("DOWNLOADING") + AbsString.alpha("PAUSED") +
            AbsString.alpha("CANCELED") + AbsString.alpha("COMPLETED") + AbsString.alpha("FAILED"))
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((Helper.ReturnStore(h, v1), ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.download.getDownloadRequest" -> (
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
            if (n_arglen == AbsNumber.alpha(0)) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v._1._4 </ NumTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENDownloadRequest.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("url", PropValue(ObjectValue(Value(StrTop), T, T, T))).
            update("destination", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))).
            update("fileName", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))).
            update("networkType", PropValue(ObjectValue(Value(AbsString.alpha("CELLULAR") + AbsString.alpha("WIFI") +
                                                             AbsString.alpha("ALL")), T, T, T))).
            update("httpHeader", PropValue(ObjectValue(Value(NullTop), T, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_2, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.download.getMIMEType" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val es =
            if (n_arglen == AbsNumber.alpha(0)) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v._1._4 </ NumTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he + h_e, ctxe + ctx_e))
        }
        ))/*,
      ("tizen.download.setListener" -> (
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
            if (n_arglen == AbsNumber.alpha(0)) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v_1._1._4 </ NumTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (obj, es_2) = v_2._2.foldLeft((o_new, TizenHelper.TizenExceptionBot))((_o, l) => {
            val v_1 = Helper.Proto(h_5, l, AbsString.alpha("onprogress"))
            val v_2 = Helper.Proto(h_5, l, AbsString.alpha("onpaused"))
            val v_3 = Helper.Proto(h_5, l, AbsString.alpha("oncanceled"))
            val v_4 = Helper.Proto(h_5, l, AbsString.alpha("oncompleted"))
            val v_5 = Helper.Proto(h_5, l, AbsString.alpha("onfailed"))
            val es1 =
              if (v_1._2.exists((ll) => Helper.IsCallable(h_1, ll) <= F))
                Set[WebAPIException](TypeMismatchError)
              else TizenHelper.TizenExceptionBot
            val es2 =
              if (v_2._2.exists((ll) => Helper.IsCallable(h_1, ll) <= F))
                Set[WebAPIException](TypeMismatchError)
              else TizenHelper.TizenExceptionBot
            val es3 =
              if (v_3._2.exists((ll) => Helper.IsCallable(h_1, ll) <= F))
                Set[WebAPIException](TypeMismatchError)
              else TizenHelper.TizenExceptionBot
            val es4 =
              if (v_4._2.exists((ll) => Helper.IsCallable(h_1, ll) <= F))
                Set[WebAPIException](TypeMismatchError)
              else TizenHelper.TizenExceptionBot
            val es5 =
              if (v_5._2.exists((ll) => Helper.IsCallable(h_1, ll) <= F))
                Set[WebAPIException](TypeMismatchError)
              else TizenHelper.TizenExceptionBot
          })

          val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
          val o_arr1 = o_arr.
            update("0", PropValue(ObjectValue(Value(NumTop), T, T, T)))
          val h_2 = h_1.update(l_r1, o_arr1)
          /*  val h_3 = TizenHelper.addCallbackHandler(h_2,AbsString.alpha("DownloadCB.onpaused"),v_2,)*/
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_2, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        ))*/
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}