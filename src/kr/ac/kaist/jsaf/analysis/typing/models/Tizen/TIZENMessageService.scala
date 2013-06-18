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

object TIZENMessageService extends Tizen {
  private val name = "MessageService"
  /* predefined locations */
  val loc_obj = newPredefLoc(name + "Obj")
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
    ("id", AbsConstValue(PropValue(Value(UndefTop)))),
    ("type", AbsConstValue(PropValue(Value(UndefTop)))),
    ("name", AbsConstValue(PropValue(Value(UndefTop)))),
    ("messageStorage", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("sendMessage", AbsBuiltinFunc("tizen.MessageService.sendMessage", 3)),
    ("loadMessageBody", AbsBuiltinFunc("tizen.MessageService.loadMessageBody", 3)),
    ("loadMessageAttachment", AbsBuiltinFunc("tizen.MessageService.loadMessageAttachment", 3)),
    ("sync", AbsBuiltinFunc("tizen.MessageService.sync", 3)),
    ("syncFolder", AbsBuiltinFunc("tizen.MessageService.syncFolder", 4)),
    ("stopSync", AbsBuiltinFunc("tizen.MessageService.stopSync", 1))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.MessageService.sendMessage" -> (
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
          val v_1 = getArgValue(h_2, ctx_2, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))
          val (b_1, es_1) = TizenHelper.instanceOf(h_2, v_1, Value(TIZENMessage.loc_proto))
          val es_2 =
            if (b_1._1._3 <= F)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_3, es) = n_arglen match {
            case UIntSingle(n) if n == 2 =>
              val v_2 = getArgValue(h_2, ctx_2, args, "1")
              val es1 =
                if (v_2._2.exists((l) => Helper.IsCallable(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
              val o_arr1 = o_arr.
                update("0", PropValue(ObjectValue(Value(StrTop), T, T, T)))
              val h_3 = h_2.update(l_r1, o_arr1)
              val h_4 = TizenHelper.addCallbackHandler(h_3, AbsString.alpha("MsgRecipientsCB"), Value(v_2._2), Value(l_r1))
              (h_4, es1)
            case UIntSingle(n) if n == 3 =>
              val v_2 = getArgValue(h_2, ctx_2, args, "1")
              val v_3 = getArgValue(h_2, ctx_2, args, "2")
              val es1 =
                if (v_2._2.exists((l) => Helper.IsCallable(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val es2 =
                if (v_3._2.exists((l) => Helper.IsCallable(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
              val o_arr1 = Helper.NewArrayObject(AbsNumber.alpha(1))
              val o_arr2 = o_arr.update("0", PropValue(ObjectValue(Value(StrTop), T, T, T)))
              val o_arr3 = o_arr1.update("0", PropValue(ObjectValue(Value(TIZENtizen.loc_err), T, T, T)))
            val h_3 = h_2.update(l_r1, o_arr2).update(l_r2, o_arr3)
              val h_4 = TizenHelper.addCallbackHandler(h_3, AbsString.alpha("MsgRecipientsCB"), Value(v_2._2), Value(l_r1))
              val h_5 = TizenHelper.addCallbackHandler(h_4, AbsString.alpha("errorCB"), Value(v_3._2), Value(l_r2))
              (h_5, es1 ++ es2)
            case _ =>
              (h_2, TizenHelper.TizenExceptionBot)
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ es_2)
          ((h_3, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        ))/*,
      ("tizen.MessageService.loadMessageBody" -> ()),
      ("tizen.MessageService.loadMessageAttachment" -> ()),
      ("tizen.MessageService.sync" -> ()),
      ("tizen.MessageService.syncFolder" -> ()),
      ("tizen.MessageService.stopSync" -> ())*/

    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}