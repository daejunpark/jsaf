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
import kr.ac.kaist.jsaf.analysis.typing.models.builtin.BuiltinArray
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

object TIZENmessaging extends Tizen {
  private val name = "messaging"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_messaging
  val loc_proto = newPredefLoc(name + "Proto")

  val loc_msgservice: Loc        = newPreDefLoc("MessageService", Old)
  val loc_msgstorage: Loc        = newPreDefLoc("MessageStorage", Old)
  val loc_msgbody: Loc        = newPreDefLoc("MessageBody", Old)
  val loc_msgattach: Loc        = newPreDefLoc("MessageAttachment", Old)
  val loc_msgattacharr: Loc        = newPreDefLoc("MessageAttachmentArr", Old)

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto), (loc_msgservice, prop_msgservice_ins), (loc_msgstorage, prop_msgstorage_ins),
    (loc_msgbody, prop_msgbody_ins), (loc_msgattach, prop_msgattach_ins), (loc_msgattacharr, prop_msgattacharr_ins)
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
    ("getMessageServices", AbsBuiltinFunc("tizen.messaging.getMessageServices", 3))
  )

  private val prop_msgservice_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENMessageService.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("id",               AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T)))),
    ("type",                 AbsConstValue(PropValue(ObjectValue(Value(AbsString.alpha("messaging.sms") + AbsString.alpha("messaging.mms") +
                                                            AbsString.alpha("messaging.email")), F, T, T)))),
    ("name",               AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T)))),
    ("messageStorage",               AbsConstValue(PropValue(ObjectValue(Value(loc_msgstorage), F, T, T))))
  )

  private val prop_msgstorage_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENMessageStorage.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T)))
  )

  private val prop_msgbody_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENMessageBody.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("messageId",          AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T)))),
    ("loaded",          AbsConstValue(PropValue(ObjectValue(Value(BoolTop), F, T, T)))),
    ("plainBody",          AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T)))),
    ("htmlBody",          AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T)))),
    ("inlineAttachments",          AbsConstValue(PropValue(ObjectValue(Value(loc_msgattacharr), T, T, T))))
  )

  private val prop_msgattach_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENMessageAttachment.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("id", AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T)))),
    ("messageId", AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T)))),
    ("mimeType", AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T)))),
    ("filePath", AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T))))
  )

  private val prop_msgattacharr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(loc_msgattach), T, T, T))))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.messaging.getMessageServices" -> (
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
          val v_2 = getArgValue(h_2, ctx_2, args, "1")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))

          val es =
            if (v_1._1._5 </ AbsString.alpha("messaging.sms") && v_1._1._5 </ AbsString.alpha("messaging.mms") &&
              v_1._1._5 </ AbsString.alpha("messaging.email"))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v_2._2.exists((l) => Helper.IsCallable(h_2, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
          val o_arr1 = o_arr.
            update("0", PropValue(ObjectValue(Value(loc_msgservice), T, T, T)))
          val h_3 = h_2.update(l_r1, o_arr1)
          val (h_4, es_2) = n_arglen match {
            case UIntSingle(n) if n == 2 =>
              val h_4 = TizenHelper.addCallbackHandler(h_3, AbsString.alpha("MsgServiceArrSuccessCB"), Value(v_2._2), Value(l_r1))
              (h_4, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n >= 3 =>
              val v_3 = getArgValue(h_3, ctx_2, args, "2")
              val es_2 =
                if (v_3._2.exists((l) => Helper.IsCallable(h_3, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1)).
                update("0", PropValue(ObjectValue(Value(TIZENtizen.loc_err), T, T, T)))
              val h_4 = h_3.update(l_r2, o_arr)
              val h_5 = TizenHelper.addCallbackHandler(h_4, AbsString.alpha("MsgServiceArrSuccessCB"), Value(v_2._2), Value(l_r1))
              val h_6 = TizenHelper.addCallbackHandler(h_5, AbsString.alpha("errorCB"), Value(v_3._2), Value(l_r2))
              (h_6, es_2)
            case _ =>
              (h_3, TizenHelper.TizenExceptionBot)
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ es_2)
          ((h_4, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}