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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap

object TIZENBluetoothAdapter extends Tizen {
  val name = "BluetoothAdapter"
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
    ("name", AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T)))),
    ("address", AbsConstValue(PropValue(ObjectValue(Value(StrTop), F, T, T)))),
    ("powered", AbsConstValue(PropValue(ObjectValue(Value(BoolTop), F, T, T)))),
    ("visible", AbsConstValue(PropValue(ObjectValue(Value(BoolTop), F, T, T))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("setName", AbsBuiltinFunc("tizen.BluetoothAdapter.setName", 3)),
    ("setPowered", AbsBuiltinFunc("tizen.BluetoothAdapter.setPowered", 3)),
    ("setVisible", AbsBuiltinFunc("tizen.BluetoothAdapter.setVisible", 4)),
    ("discoverDevices", AbsBuiltinFunc("tizen.BluetoothAdapter.discoverDevices", 2)),
    ("stopDiscovery", AbsBuiltinFunc("tizen.BluetoothAdapter.stopDiscovery", 2)),
    ("getKnownDevices", AbsBuiltinFunc("tizen.BluetoothAdapter.getKnownDevices", 2)),
    ("getDevice", AbsBuiltinFunc("tizen.BluetoothAdapter.getDevice", 3)),
    ("createBonding", AbsBuiltinFunc("tizen.BluetoothAdapter.createBonding", 3)),
    ("destroyBonding", AbsBuiltinFunc("tizen.BluetoothAdapter.destroyBonding", 3)),
    ("registerRFCOMMServiceByUUID", AbsBuiltinFunc("tizen.BluetoothAdapter.registerRFCOMMServiceByUUID", 4))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.BluetoothAdapter.setName" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val name = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

          val es =
            if (name._1._1 <= UndefTop || name._1._2 <= NullTop || name._1._3 <= BoolTop || name._1._4 <= NumTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_2, es_1) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
                (HeapBot, TizenHelper.TizenExceptionBot)
              }
              else {
                val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                  (_h + Helper.PropStore(h, l, AbsString.alpha("name"), Value(name._1._5)))
                })
                (h_1, TizenHelper.TizenExceptionBot)
              }
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h, ctx, args, "1")

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

              if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
                (HeapBot, ess ++ ess2)
              }
              else {
                val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                  (_h + Helper.PropStore(h, l, AbsString.alpha("name"), Value(name._1._5)))
                })
                /* register success callback */ // TODO: not yet implemented
                (h_1, ess ++ ess2)
              }
            case UIntSingle(n) if n == 3 =>
              val sucCB = getArgValue(h, ctx, args, "1")
              val errCB = getArgValue(h, ctx, args, "2")

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

              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
                /* register Error callback */ // TODO: not yet implemented
                (HeapBot, ess ++ ess2 ++ ese ++ ese2)
              }
              else {
                val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                  (_h + Helper.PropStore(h, l, AbsString.alpha("name"), Value(name._1._5)))
                })
                /* register success callback */ // TODO: not yet implemented
                (h_1, ess ++ ess2 ++ ese ++ ese2)
              }
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_2, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.setPowered" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val state = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

          val es =
            if (state._1._1 <= UndefTop || state._1._2 <= NullTop || state._1._4 <= NumTop || state._1._5 <= StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_2, es_1) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                (_h + Helper.PropStore(h, l, AbsString.alpha("powered"), Value(BoolTop)))
              })
              /* register successCallback and errorCallback */ // TODO: not yet implemented
              (h_1, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h, ctx, args, "1")

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

              val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                (_h + Helper.PropStore(h, l, AbsString.alpha("powered"), Value(BoolTop)))
              })
              /* register success callback*/ // TODO: not yet implemented
              (h_1, ess ++ ess2)
            case UIntSingle(n) if n == 3 =>
              val sucCB = getArgValue(h, ctx, args, "1")
              val errCB = getArgValue(h, ctx, args, "2")

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

              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                (_h + Helper.PropStore(h, l, AbsString.alpha("powered"), Value(BoolTop)))
              })
              /* register success callback */ // TODO: not yet implemented
              (h_1, ess ++ ess2 ++ ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_2, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.setVisible" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val mode = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

          val es =
            if (mode._1._1 <= UndefTop || mode._1._2 <= NullTop || mode._1._4 <= NumTop || mode._1._5 <= StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_2, es_1) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
                (HeapBot, TizenHelper.TizenExceptionBot)
              }
              else {
                val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                  (_h + Helper.PropStore(h, l, AbsString.alpha("visible"), Value(mode._1._3)))
                })
                (h_1, TizenHelper.TizenExceptionBot)
              }
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h, ctx, args, "1")

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

              if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
                (HeapBot, ess ++ ess2)
              }
              else {
                val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                  (_h + Helper.PropStore(h, l, AbsString.alpha("visible"), Value(mode._1._3)))
                })
                /* register success callback */ // TODO: not yet implemented
                (h_1, ess ++ ess2)
              }
            case UIntSingle(n) if n == 3 =>
              val sucCB = getArgValue(h, ctx, args, "1")
              val errCB = getArgValue(h, ctx, args, "2")

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

              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h,l) <= T) _L + l
                else _L
              })

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
                /* register Error callback */ // TODO: not yet implemented
                (HeapBot, ess ++ ess2 ++ ese ++ ese2)
              }
              else {
                val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
                  (_h + Helper.PropStore(h, l, AbsString.alpha("visible"), Value(mode._1._3)))
                })
                /* register success callback */ // TODO: not yet implemented
                (h_1, ess ++ ess2 ++ ese ++ ese2)
              }
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h + h_2, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.discoverDevices" -> (
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

          val sucCB = getArgValue(h_2, ctx_2, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))

          val ess =
            if (sucCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
            if (Helper.IsCallable(h_2,l) <= T) _L + l
            else _L
          })

          val ess2 =
            if (sucCB._2.exists((l) => Helper.IsCallable(h_2, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          /* check if bluetooth device is turned on/off */
          val lset_this = h_2(SinglePureLocalLoc)("@this")._1._2._2

          if (lset_this.exists((l) => h_2(l)("powered")._1._2._1._3 != T)) {
            /* register Error callback */ // TODO: not yet implemented
          }

          val h_6 = sucCB._2.foldLeft(h_2)((_h, l) => {
            if (Helper.HasOwnProperty(_h, l, AbsString.alpha("onstarted")) <= T) {
              /* register onstarted function */ // TODO: not yet implemented
            }
            val h_3 =
              if (Helper.HasOwnProperty(_h, l, AbsString.alpha("ondevicefound")) <= T) {
                val o_dev = ObjEmpty.
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
                  update("isConnected", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
                  update("uuids", PropValue(ObjectValue(Value(StrTop), F, T, T)))
                h_2.update(l_r, o_dev)
                /* register ondevicefound function*/ // TODO: not yet implemented
              }
              else h_2
            if (Helper.HasOwnProperty(_h, l, AbsString.alpha("ondevicedisappeared")) <= T) {
              val addr = Value(StrTop)
              /* register ondevicedisappeared function*/ // TODO: not yet implemented
            }
            val h_5 =
              if (Helper.HasOwnProperty(_h, l, AbsString.alpha("onfinished")) <= T) {
                val o_dev2 = ObjEmpty.
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
                  update("isConnected", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
                  update("uuids", PropValue(ObjectValue(Value(StrTop), F, T, T)))
                val h_4 = h_3.update(l_r, o_dev2)
                val o_arr = Helper.NewArrayObject(UInt)
                val o_arr1 = o_arr.update("@default_number", PropValue(ObjectValue(l_r, T, T, T)))
                h_4.update(l_r2, o_arr1)
                /* register onfinished function*/ // TODO: not yet implemented
              }
              else h_3
            h_5
          })

          val (h_7, es) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              (HeapBot, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val errCB = getArgValue(h_6, ctx_2, args, "2")
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locset2 = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h_6,l) <= T) _L + l
                else _L
              })

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h_6, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              /* register error callback */ // TODO: not yet implemented
              (HeapBot, ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess ++ ess2)
          ((h_6 + h_7, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.stopDiscovery" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val (h_1, es) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              val sucCB = getArgValue(h, ctx, args, "0")
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

              /* check if bluetooth device is turned on/off */
              val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

              if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
                /* register Error callback */ // TODO: not yet implemented
              }

              /* register success callback */ // TODO: not yet implemented
              (h, ess ++ ess2)
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h, ctx, args, "0")
              val errCB = getArgValue(h, ctx, args, "1")
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

              /* check if bluetooth device is turned on/off */
              val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

              if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
                /* register Error callback */ // TODO: not yet implemented
              }

              /* register success callback and error callback */ // TODO: not yet implemented
              (h, ess ++ ess2 ++ ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.getKnownDevices" -> (
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

          val sucCB = getArgValue(h_2, ctx_2, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_2, ctx_2, args, "length"))

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
          /* check if bluetooth device is turned on/off */
          val lset_this = h_2(SinglePureLocalLoc)("@this")._1._2._2

          if (lset_this.exists((l) => h_2(l)("powered")._1._2._1._3 != T)) {
            /* register Error callback */ // TODO: not yet implemented
          }

          val o_dev = ObjEmpty.
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
            update("isConnected", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("uuids", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_3 = h_2.update(l_r, o_dev)
          val o_arr = Helper.NewArrayObject(UInt)
          val o_arr1 = o_arr.update("@default_number", PropValue(ObjectValue(l_r, T, T, T)))
          val h_4 = h_3.update(l_r2, o_arr1)

          val (h_5, es) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              /* register success callback */ // TODO: not yet implemented
              (HeapBot, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val errCB = getArgValue(h_4, ctx_2, args, "1")
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
              /* register success callback and error callback */ // TODO: not yet implemented
              (HeapBot, ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess ++ ess2)
          ((h_5, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.getDevice" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val addr = getArgValue(h_1, ctx_1, args, "0")
          val sucCB = getArgValue(h_1, ctx_1, args, "1")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

          val es =
            if (addr._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val ess =
            if (sucCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
            if (Helper.IsCallable(h_1,l) <= T) _L + l
            else _L
          })

          val ess2 =
            if (sucCB._2.exists((l) =>  Helper.IsCallable(h_1, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          /* check if bluetooth device is turned on/off */
          val lset_this = h_1(SinglePureLocalLoc)("@this")._1._2._2

          if (lset_this.exists((l) => h_1(l)("powered")._1._2._1._3 != T)) {
            /* register Error callback */ // TODO: not yet implemented
          }

          val o_dev = ObjEmpty.
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
            update("isConnected", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("uuids", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_2 = h_1.update(l_r, o_dev)

          val (h_3, es_1) = n_arglen match {
            case UIntSingle(n) if n == 2 =>
              /* register success callback */ // TODO: not yet implemented
              (HeapBot, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 3 =>
              val errCB = getArgValue(h_2, ctx_1, args, "2")
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h_2,l) <= T) _L + l
                else _L
              })

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register success callback and error callback */ // TODO: not yet implemented
              (HeapBot, ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess ++ ess2 ++ es_1)
          ((h_2 + h_3, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.createBonding" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val addr = getArgValue(h_1, ctx_1, args, "0")
          val sucCB = getArgValue(h_1, ctx_1, args, "1")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

          val es =
            if (addr._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val ess =
            if (sucCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
            if (Helper.IsCallable(h_1,l) <= T) _L + l
            else _L
          })

          val ess2 =
            if (sucCB._2.exists((l) =>  Helper.IsCallable(h_1, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          /* check if bluetooth device is turned on/off */
          val lset_this = h_1(SinglePureLocalLoc)("@this")._1._2._2

          if (lset_this.exists((l) => h_1(l)("powered")._1._2._1._3 != T)) {
            /* register Error callback */ // TODO: not yet implemented
          }

          val o_dev = ObjEmpty.
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
            update("isConnected", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("uuids", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_2 = h_1.update(l_r, o_dev)

          val (h_3, es_1) = n_arglen match {
            case UIntSingle(n) if n == 2 =>
              /* register success callback */ // TODO: not yet implemented
              (HeapBot, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 3 =>
              val errCB = getArgValue(h_2, ctx_1, args, "2")
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h_2,l) <= T) _L + l
                else _L
              })

              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register success callback and error callback */ // TODO: not yet implemented
              (HeapBot, ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ ess ++ ess2 ++ es_1)
          ((h_2 + h_3, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.destroyBonding" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val addr = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val es =
            if (addr._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          /* check if bluetooth device is turned on/off */
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          if (lset_this.exists((l) => h(l)("powered")._1._2._1._3 != T)) {
            /* register Error callback */ // TODO: not yet implemented
          }

          val (h_1, es_1) = n_arglen match {
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h, ctx, args, "1")
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
              /* register success callback */ // TODO: not yet implemented
              (HeapBot, ess ++ ess2)
            case UIntSingle(n) if n == 3 =>
              val sucCB = getArgValue(h, ctx, args, "1")
              val errCB = getArgValue(h, ctx, args, "2")
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
              /* register success callback and error callback */ // TODO: not yet implemented
              (HeapBot, ess ++ ess2 ++ ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h + h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.BluetoothAdapter.registerRFCOMMServiceByUUID" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val uuid = getArgValue(h_1, ctx_1, args, "0")
          val name = getArgValue(h_1, ctx_1, args, "1")
          val sucCB = getArgValue(h_1, ctx_1, args, "2")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

          val es =
            if (uuid._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (name._1 </ PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val ess =
            if (sucCB._1 <= PValueTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
            if (Helper.IsCallable(h_1,l) <= T) _L + l
            else _L
          })

          val ess2 =
            if (sucCB._2.exists((l) =>  Helper.IsCallable(h_1, l) <= F))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_2, es_2) = n_arglen match {
            case UIntSingle(n) if n == 4 =>
              val errCB = getArgValue(h_1, ctx_1, args, "3")
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h_1,l) <= T) _L + l
                else _L
              })
              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h_1, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* check if bluetooth device is turned on/off */
              val lset_this = h_1(SinglePureLocalLoc)("@this")._1._2._2
              if (lset_this.exists((l) => h_1(l)("powered")._1._2._1._3 != T)) {
                /* register Error callback */ // TODO: not yet implemented
              }
              (h_1, ese ++ ese2)
            case _ => {
              (h_1, TizenHelper.TizenExceptionBot)
            }
          }
          val o_serv = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENBluetoothServiceHandler.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("uuid", PropValue(ObjectValue(Value(Helper.toString(uuid._1)), F, T, T))).
            update("name", PropValue(ObjectValue(Value(Helper.toString(name._1)), F, T, T))).
            update("isConnected", PropValue(ObjectValue(Value(T), F, T, T))).
            update("onconnect", PropValue(ObjectValue(Value(NullTop), F, T, T)))
          val h_3 = h_2.update(l_r, o_serv)
          /* register success callback */ // TODO: not yet implemented
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ ess ++ ess2 ++ es_2)
          ((h_3, ctx), (he + h_e, ctxe + ctx_e))
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

object TIZENBluetoothServiceHandler extends Tizen {
  val name = "BluletoothServiceHandler"
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
    ("unregister", AbsBuiltinFunc("tizen.BluetoothServiceHandler.unregister", 2))
  )
  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.BluetoothServiceHandler.unregister" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val h_1 = lset_this.foldLeft(HeapBot)((_h, l) => {
            (_h + Helper.PropStore(h, l, AbsString.alpha("isConnected"), Value(AbsBool.alpha(false))))
          })

          val (h_2, es) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              val sucCB = getArgValue(h_1, ctx, args, "0")
              val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h_1,l) <= T) _L + l
                else _L
              })
              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h_1, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register success Callback */
              (HeapBot, ess ++ ess2)
            case UIntSingle(n) if n == 2 =>
              val sucCB = getArgValue(h_1, ctx, args, "0")
              val errCB = getArgValue(h_1, ctx, args, "1")
              val ess =
                if (sucCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val locset = sucCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h_1,l) <= T) _L + l
                else _L
              })
              val ess2 =
                if (sucCB._2.exists((l) =>  Helper.IsCallable(h_1, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val ese =
                if (errCB._1 <= PValueTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val locsete = errCB._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsCallable(h_1,l) <= T) _L + l
                else _L
              })
              val ese2 =
                if (errCB._2.exists((l) =>  Helper.IsCallable(h_1, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              /* register success callback and error callback */
              (HeapBot, ess ++ ess2 ++ ese ++ ese2)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h_1 + h_2, ctx), (he + h_e, ctxe + ctx_e))
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