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
import java.lang.InternalError
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
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc

object TIZENdatacontrol extends Tizen {
  private val name = "datacontrol"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_datacontrol
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
    ("getDataControlConsumer", AbsBuiltinFunc("tizen.datacontrol.getDataControlConsumer", 3))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.datacontrol.getDataControlConsumer" -> (
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
          val v_3 = getArgValue(h_1, ctx_1, args, "2")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))
          val es =
            if (n_arglen == 0) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_1 =
            if (v_1._1._5 </ StrTop || v_2._1._5 </ StrTop) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_2 =
            if (v_3._1._5 != AbsString.alpha("MAP") && v_3._1._5 != AbsString.alpha("SQL"))
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val o_new =
            ObjEmpty.
              update("@class", PropValue(AbsString.alpha("Object"))).
              update("@extensible", PropValue(T)).
              update("@scope", PropValue(Value(NullTop))).
              update("@hasinstance", PropValue(Value(NullTop))).
              update("providerId", PropValue(ObjectValue(Value(v_1._1._5), T, T, T))).
              update("dataId", PropValue(ObjectValue(Value(v_2._1._5), T, T, T))).
              update("type", PropValue(ObjectValue(Value(v_3._1._5), T, T, T)))
          val o_new2 =
            if (v_3._1._5 == AbsString.alpha("MAP"))
              o_new.update("@proto", PropValue(ObjectValue(Value(TIZENMappedDataControlConsumer.loc_proto), F, F, F)))
            else if (v_3._1._5 == AbsString.alpha("SQL"))
              o_new.update("@proto", PropValue(ObjectValue(Value(TIZENSQLDataControlConsumer.loc_proto), F, F, F)))
            else o_new
          val h_2 = h_1.update(l_r1, o_new2)
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

object TIZENDataControlConsumerObject extends Tizen {
  private val name = "SQLDataControlConsumerObject"
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
    ("type", AbsConstValue(PropValue(Value(UndefTop)))),
    ("providerId", AbsConstValue(PropValue(Value(UndefTop)))),
    ("dataId", AbsConstValue(PropValue(Value(UndefTop))))
  )
  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T)))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}


object TIZENSQLDataControlConsumer extends Tizen {
  private val name = "SQLDataControlConsumer"
  /* predefined locations */
  val loc_proto = newPredefLoc(name + "Proto")
  val loc_parent = TIZENDataControlConsumerObject.loc_proto

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_proto, prop_proto)
  )
  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("insert", AbsBuiltinFunc("tizen.SQLDataControlConsumer.insert", 4)),
    ("update", AbsBuiltinFunc("tizen.SQLDataControlConsumer.update", 4)),
    ("remove", AbsBuiltinFunc("tizen.SQLDataControlConsumer.remove", 4)),
    ("select", AbsBuiltinFunc("tizen.SQLDataControlConsumer.select", 7))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.SQLDataControlConsumer.insert" -> (
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
          val es_1 =
            if (v_1._1._4 </ NumTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val es_2 = v_2._2.foldLeft(TizenHelper.TizenExceptionBot)((_es, l) => {
            val v1 = Helper.Proto(h_2, l, AbsString.alpha("columns"))
            val v2 = Helper.Proto(h_2, l, AbsString.alpha("values"))
            val es1 =
              if (v1._2.exists((l) => Helper.IsArray(h_2, l) <= F))
                Set[WebAPIException](TypeMismatchError)
              else TizenHelper.TizenExceptionBot
            val es2 =
              if (v2._2.exists((l) => Helper.IsArray(h_2, l) <= F))
                Set[WebAPIException](TypeMismatchError)
              else TizenHelper.TizenExceptionBot
            _es ++ es1 ++ es2
          })
          val (h_3, es) = n_arglen match {
            case UIntSingle(n) if n == 3 =>
              val v_3 = getArgValue(h_2, ctx_2, args, "2")
              val es1 =
                if (v_3._2.exists((l) => Helper.IsCallable(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(2))
              val o_arr1 = o_arr.
                update("0", PropValue(ObjectValue(Value(v_1._1._4), T, T, T))).
                update("1", PropValue(ObjectValue(Value(NumTop), T, T, T)))
              val h_3 = h_2.update(l_r1, o_arr1)
              val h_4 = TizenHelper.addCallbackHandler(h_3, AbsString.alpha("DataCtrlInsertSuccessCB"), Value(v_3._2), Value(l_r1))
              (h_4, es1)
            case UIntSingle(n) if n == 4 =>
              val v_3 = getArgValue(h_2, ctx_2, args, "2")
              val v_4 = getArgValue(h_2, ctx_2, args, "3")
              val es1 =
                if (v_3._2.exists((l) => Helper.IsCallable(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val es2 =
                if (v_4._2.exists((l) => Helper.IsCallable(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(2))
              val o_arr1 = Helper.NewArrayObject(AbsNumber.alpha(2))
              val o_arr2 = o_arr.
                update("0", PropValue(ObjectValue(Value(v_1._1._4), T, T, T))).
                update("1", PropValue(ObjectValue(Value(NumTop), T, T, T)))
              val o_arr3 = o_arr1.
                update("0", PropValue(ObjectValue(Value(v_1._1._4), T, T, T))).
                update("1", PropValue(ObjectValue(Value(TIZENtizen.loc_err), T, T, T)))/*WebAPIError*/
              val h_3 = h_2.update(l_r1, o_arr2).update(l_r2, o_arr3)
              val h_4 = TizenHelper.addCallbackHandler(h_3, AbsString.alpha("DataCtrlInsertSuccessCB"), Value(v_3._2), Value(l_r1))
              val h_5 = TizenHelper.addCallbackHandler(h_4, AbsString.alpha("DataCtrlErrCB"), Value(v_4._2), Value(l_r2))
              (h_5, es1 ++ es2)
            case _ =>
              (h_2, TizenHelper.TizenExceptionBot)
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1 ++ es_2)
          ((h_3, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        ))/*,
      ("tizen.SQLDataControlConsumer.update" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),
      ("tizen.SQLDataControlConsumer.remove" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),
      ("tizen.SQLDataControlConsumer.select" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        ))*/
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}

object TIZENMappedDataControlConsumer extends Tizen {
  private val name = "MappedDataControlConsumer"
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
    ("addValue", AbsBuiltinFunc("tizen.MappedDataControlConsumer.addValue", 5)),
    ("removeValue", AbsBuiltinFunc("tizen.MappedDataControlConsumer.removeValue", 5)),
    ("getValue", AbsBuiltinFunc("tizen.MappedDataControlConsumer.getValue", 4)),
    ("updateValue", AbsBuiltinFunc("tizen.MappedDataControlConsumer.updateValue", 6))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("tizen.MappedDataControlConsumer.addValue" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),
      ("tizen.MappedDataControlConsumer.removeValue" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),
      ("tizen.MappedDataControlConsumer.getValue" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        )),
      ("tizen.MappedDataControlConsumer.updateValue" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {

        }
        ))*/
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}