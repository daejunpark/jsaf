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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue

object TIZENBluetoothDevice extends Tizen {
  val name = "BluetoothDevice"
  /* predefined locations */
  val loc_obj = newPredefLoc(name + "Obj")
  val loc_proto = newPredefLoc(name + "Proto")
  val loc_devClass = newPredefLoc(name + "deviceClass")

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
    ("name", AbsConstValue(PropValue(Value(StrTop)))),
    ("address", AbsConstValue(PropValue(Value(StrTop)))),
    ("deviceClass", AbsConstValue(PropValue(ObjectValue(Value(loc_devClass), F, F, F)))),
    ("isBonded", AbsConstValue(PropValue(Value(BoolTop)))),
    ("isTrusted", AbsConstValue(PropValue(Value(BoolTop)))),
    ("isConnected", AbsConstValue(PropValue(Value(BoolTop)))),
    ("uuids", AbsConstValue(PropValue(Value(StrTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("connectToServiceByUUID", AbsBuiltinFunc("tizen.BluetoothDevice.connectToServiceByUUID", 3))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.BluetoothDevice.connectToServiceByUUID" -> (
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
          val uuid = getArgValue(h_2, ctx_2, args, "0")
          val sucCB = getArgValue(h_2, ctx_2, args, "1")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))

          val es =
            if (uuid._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val ess =
            if (sucCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
            if (Helper.IsCallable(h_2,l) <= T) _L + l
            else _L
          })
          val ess2 =
            if (sucCB._2.exists((l) =>  Helper.IsCallable(h_2, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val o_peer = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENBluetoothDevice.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("address", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("deviceClass", PropValue(ObjectValue(Value(TIZENBluetoothDevice.loc_devClass), F, F, F))).
            update("isBonded", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("isTrusted", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("isConnected", PropValue(ObjectValue(AbsBool.alpha(true), F, T, T))).
            update("uuids", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_3 = h_2.update(l_r, o_peer)
          val o_socket = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENBluetoothSocket.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("uuid", PropValue(ObjectValue(Value(Helper.toString(uuid._1)), F, T, T))).
            update("state", PropValue(ObjectValue(Value(AbsString.alpha("OPEN")), F, T, T))).
            update("peer", PropValue(ObjectValue(Value(l_r), F, F, F))).
            update("onmessage", PropValue(ObjectValue(Value(NullTop), F, T, T))).
            update("onclose", PropValue(ObjectValue(Value(NullTop), F, T, T))).
            update("onerror", PropValue(ObjectValue(Value(NullTop), F, T, T)))
          val h_4 = h_3.update(l_r2, o_socket)

          val (h_5, es_1) = n_arglen match {
            case UIntSingle(n) if n == 2 =>
              /* register success callback */
              (HeapBot, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 3 =>
              val errCB = getArgValue(h_4, ctx_2, args, "2")
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h_4,l) <= T) _L + l
                else _L
              })
              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h_4, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register success callback and error callback */
              (HeapBot, ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess ++ ess2 ++ es_1)
          ((h_4 + h_5, ctx), (he + h_e, ctxe + ctx_e))
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

object TIZENBluetoothClass extends Tizen {
  val name = "BluetoothClass"
  /* predefined locations */
  val loc_obj = TIZENBluetoothDevice.loc_devClass
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
    ("major", AbsConstValue(PropValue(Value(NumTop)))),
    ("minor", AbsConstValue(PropValue(Value(NumTop)))),
    ("services", AbsConstValue(PropValue(Value(AbsNumber.alpha(0x0001) + AbsNumber.alpha(0x0008) + AbsNumber.alpha(0x0010) +
      AbsNumber.alpha(0x0020) + AbsNumber.alpha(0x0040) + AbsNumber.alpha(0x0080) + AbsNumber.alpha(0x0100) + AbsNumber.alpha(0x0200) +
      AbsNumber.alpha(0x0400)))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("hasService", AbsBuiltinFunc("tizen.BluetoothClass.hasService", 1))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.BluetoothClass.hasService" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val service = getArgValue(h, ctx, args, "0")
          val es =
            if (service._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h, Value(BoolTop)), ctx), (he + h_e, ctxe + ctx_e))
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

object TIZENBluetoothSocket extends Tizen {
  val name = "BluetoothSocket"
  /* predefined locations */
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("uuid", AbsConstValue(PropValue(Value(NullTop)))),
    ("state", AbsConstValue(PropValue(Value(NullTop)))),
    ("peer", AbsConstValue(PropValue(Value(TIZENBluetoothDevice.loc_proto)))),
    ("onmessage", AbsConstValue(PropValue(Value(NullTop)))),
    ("onclose", AbsConstValue(PropValue(Value(NullTop)))),
    ("onerror", AbsConstValue(PropValue(Value(NullTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("writeData", AbsBuiltinFunc("tizen.BluetoothSocket.writeData", 1)),
    ("readData", AbsBuiltinFunc("tizen.BluetoothSocket.readData", 0)),
    ("close", AbsBuiltinFunc("tizen.BluetoothSocket.close", 0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.BluetoothSocket.writeData" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val data = getArgValue(h, ctx, args, "0")
          val es = if (data._1 </ PValueTop)
            Set[WebAPIException](TypeMismatchError)
          else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothSocket.readData" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        }
        )),
      ("tizen.BluetoothSocket.close" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
            (_h + Helper.PropStore(h, l, AbsString.alpha("state"), Value(AbsString.alpha("CLOSED"))))
          })
          val h_2 = lset_this.foldLeft(HeapBot)((_h, l) => {
            (_h + Helper.PropStore(h, l, AbsString.alpha("peer"), Value(NullTop)))
          })
          if (lset_this.exists((l) => h_2(l)("onclose")._1._2._1._2 </ NullTop)) {
            /* register onclose callback */
          }
          ((h_2, ctx), (he, ctxe))
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