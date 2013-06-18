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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context

object TIZENContactEmailAddress extends Tizen {
  private val name = "ContactEmailAddress"
  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")
  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("tizen.ContactEmailAddress.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("email", AbsConstValue(PropValue(Value(UndefTop)))),
    ("isDefault", AbsConstValue(PropValue(Value(UndefTop)))),
    ("types", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T)))
  )

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto)
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.ContactEmailAddress.constructor" -> (
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

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENContactEmailAddress.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("email", PropValue(ObjectValue(Value(Helper.toString(v_1._1)), T, T, T)))

          val (h_3, es_1) = n_arglen match {
            case UIntSingle(n) if n <= 1 =>
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
              val o_arr1 = o_arr.update("0", PropValue(ObjectValue(Value(AbsString.alpha("WORK")), T, T, T)))
              val h_3 = h_2.update(l_r1, o_arr1)
              val o_new2 = o_new.
                update("isDefault", PropValue(ObjectValue(Value(F), T, T, T))).
                update("types", PropValue(ObjectValue(Value(l_r1), T, T, T)))
              val h_4 = h_3.update(l_r2, o_new2)
              (h_4, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val v_2 = getArgValue(h_2, ctx_2, args, "1")
              val es_1 =
                if (v_2._2.exists((l) => Helper.IsArray(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
              val o_arr1 = o_arr.update("0", PropValue(ObjectValue(Value(AbsString.alpha("WORK")), T, T, T)))
              val obj = v_2._2.foldLeft(o_arr1)((_o, ll) => {
                val n_length = Operator.ToUInt32(Helper.Proto(h_2, ll, AbsString.alpha("length")))
                val obj_1 = n_length match {
                  case NumBot => _o
                  case UIntSingle(n) => {
                    val (o__, cnt) = (0 until n.toInt).foldLeft((_o, 0))((__o, i) => {
                      val vi = Helper.Proto(h_2, ll, AbsString.alpha(i.toString))
                      val o_ =
                        if (vi._1._5 == AbsString.alpha("WORK") || vi._1._5 == AbsString.alpha("HOME")){
                          val o = __o._1.update(__o._2.toString(), PropValue(ObjectValue(Value(vi._1._5), T, T, T)))
                          (o, __o._2 + 1)
                        }
                        else __o
                      o_
                    })
                    o__
                  }
                  case _ => {
                    val vi = Helper.Proto(h_2, ll, AbsString.alpha("@default_number"))
                    val o_ =
                      if (vi._1._5 == AbsString.alpha("WORK") || vi._1._5 == AbsString.alpha("HOME")){
                        _o.update("@default_number", PropValue(ObjectValue(Value(vi._1._5), T, T, T)))
                      }
                      else _o
                    o_
                  }
                }
                obj_1
              })
              val h_3 = h_2.update(l_r1, obj)
              val o_new2 = o_new.
                update("isDefault", PropValue(ObjectValue(Value(F), T, T, T))).
                update("types", PropValue(ObjectValue(Value(l_r1), T, T, T)))
              val h_4 = h_3.update(l_r2, o_new2)
              (h_4, es_1)
            case _ =>
              val v_2 = getArgValue(h_2, ctx_2, args, "1")
              val v_3 = getArgValue(h_2, ctx_2, args, "2")
              val es_1 =
                if (v_2._2.exists((l) => Helper.IsArray(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val es_2 =
                if (v_3._1._3 </ BoolTop)
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(1))
              val o_arr1 = o_arr.update("0", PropValue(ObjectValue(Value(AbsString.alpha("VOICE")), T, T, T)))
              val obj = v_2._2.foldLeft(o_arr1)((_o, ll) => {
                val n_length = Operator.ToUInt32(Helper.Proto(h_2, ll, AbsString.alpha("length")))
                val obj_1 = n_length match {
                  case NumBot => _o
                  case UIntSingle(n) => {
                    val (o__, cnt) = (0 until n.toInt).foldLeft((_o, 0))((__o, i) => {
                      val vi = Helper.Proto(h_2, ll, AbsString.alpha(i.toString))
                      val o_ =
                        if (vi._1._5 == AbsString.alpha("WORK") || vi._1._5 == AbsString.alpha("HOME")){
                          System.err.println(vi._1._5)
                          val o = __o._1.
                            update("length", PropValue(ObjectValue(Value(AbsNumber.alpha(__o._2+1)), T, T, T))).
                            update(__o._2.toString(), PropValue(ObjectValue(Value(vi._1._5), T, T, T)))
                          (o, __o._2 + 1)
                        }
                        else __o
                      o_
                    })
                    o__
                  }
                  case _ => {
                    val vi = Helper.Proto(h_2, ll, AbsString.alpha("@default_number"))
                    val o_ =
                      if (vi._1._5 == AbsString.alpha("WORK") || vi._1._5 == AbsString.alpha("HOME")){
                        _o.update("@default_number", PropValue(ObjectValue(Value(vi._1._5), T, T, T)))
                      }
                      else _o
                    o_
                  }
                }
                obj_1
              })
              val h_3 = h_2.update(l_r1, obj)
              val o_new2 = o_new.
                update("isDefault", PropValue(ObjectValue(Value(v_3._1._3), T, T, T))).
                update("types", PropValue(ObjectValue(Value(l_r1), T, T, T)))
              val h_4 = h_3.update(l_r2, o_new2)
              (h_4, es_1 ++ es_2)
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es_1)
          ((Helper.ReturnStore(h_3, Value(l_r2)), ctx_2), (he + h_e, ctxe + ctx_e))
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