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
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context

object TIZENApplicationControl extends Tizen {
  val name = "ApplicationControl"
  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor */
  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("tizen.ApplicationControl.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("operation", AbsConstValue(PropValue(Value(UndefTop)))),
    ("uri", AbsConstValue(PropValue(Value(UndefTop)))),
    ("mime", AbsConstValue(PropValue(Value(UndefTop)))),
    ("category", AbsConstValue(PropValue(Value(UndefTop)))),
    ("data", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T)))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.ApplicationControl.constructor" -> (
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
          val operation = getArgValue(h_2, ctx_2, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

          val es =
            if (n_arglen == 0) {
              Set[WebAPIException](TypeMismatchError)
            }
            else TizenHelper.TizenExceptionBot

          val o_new = ObjEmpty.update("@class", PropValue(AbsString.alpha("Function"))).
            update("@proto", PropValue(ObjectValue(Value(loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop)))

          val (h_4, es1) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              // case for "new tizen.ApplicationControl(operation)"
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(0))
              val h_2 = h_1.update(l_r, o_arr)

              val o_new2 = o_new.
                update("operation", PropValue(ObjectValue(Value(Helper.toString(operation._1)), F, T, T))).
                update("uri", PropValue(ObjectValue(AbsString.alpha(""), F, T, T))).
                update("mime", PropValue(ObjectValue(AbsString.alpha(""), F, T, T))).
                update("category", PropValue(ObjectValue(AbsString.alpha(""), F, T, T))).
                update("data", PropValue(ObjectValue(Value(LocSet(l_r)), F, T, T)))
              (h_2.update(l_r2, o_new2), TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              // case for "new tizen.ApplicationControl(operation, uri)
              val uri = getArgValue(h_2, ctx_2, args, "1")
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(0))
              val h_3 = h_2.update(l_r, o_arr)

              val o_new2 = o_new.
                update("operation", PropValue(ObjectValue(Value(Helper.toString(operation._1)), F, T, T))).
                update("uri", PropValue(ObjectValue(Value(Helper.toString(uri._1)), F, T, T))).
                update("mime", PropValue(ObjectValue(AbsString.alpha(""), F, T, T))).
                update("category", PropValue(ObjectValue(AbsString.alpha(""), F, T, T))).
                update("data", PropValue(ObjectValue(Value(LocSet(l_r)), F, T, T)))
              (h_3.update(l_r2, o_new2), TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 3 =>
              // case for "new tizen.ApplicationControl(operation, uri, mime)
              val uri = getArgValue(h_2, ctx_2, args, "1")
              val mime = getArgValue(h_2, ctx_2, args, "2")
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(0))
              val h_3 = h_2.update(l_r, o_arr)

              val o_new2 = o_new.
                update("operation", PropValue(ObjectValue(Value(Helper.toString(operation._1)), F, T, T))).
                update("uri", PropValue(ObjectValue(Value(Helper.toString(uri._1)), F, T, T))).
                update("mime", PropValue(ObjectValue(Value(Helper.toString(mime._1)), F, T, T))).
                update("category", PropValue(ObjectValue(AbsString.alpha(""), F, T, T))).
                update("data", PropValue(ObjectValue(Value(LocSet(l_r)), F, T, T)))
              (h_3.update(l_r2, o_new2), TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 4 =>
              // case for "new tizen.ApplicationControl(operation, uri, mime, category)
              val uri = getArgValue(h_2, ctx_2, args, "1")
              val mime = getArgValue(h_2, ctx_2, args, "2")
              val category = getArgValue(h_2, ctx_2, args, "3")
              val o_arr = Helper.NewArrayObject(AbsNumber.alpha(0))
              val h_3 = h_2.update(l_r, o_arr)

              val o_new2 = o_new.
                update("operation", PropValue(ObjectValue(Value(Helper.toString(operation._1)), F, T, T))).
                update("uri", PropValue(ObjectValue(Value(Helper.toString(uri._1)), F, T, T))).
                update("mime", PropValue(ObjectValue(Value(Helper.toString(mime._1)), F, T, T))).
                update("category", PropValue(ObjectValue(Value(Helper.toString(category._1)), F, T, T))).
                update("data", PropValue(ObjectValue(Value(LocSet(l_r)), F, T, T)))
              (h_3.update(l_r2, o_new2), TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 5 =>
              // case for "new tizen.ApplicationControl(operation, uri, mime, category, data)
              val uri = getArgValue(h_2, ctx_2, args, "1")
              val mime = getArgValue(h_2, ctx_2, args, "2")
              val category = getArgValue(h_2, ctx_2, args, "3")
              val data = getArgValue(h_2, ctx_2, args, "4")

              val o_new2 = o_new.
                update("operation", PropValue(ObjectValue(Value(Helper.toString(operation._1)), F, T, T))).
                update("uri", PropValue(ObjectValue(Value(Helper.toString(uri._1)), F, T, T))).
                update("mime", PropValue(ObjectValue(Value(Helper.toString(mime._1)), F, T, T))).
                update("category", PropValue(ObjectValue(Value(Helper.toString(category._1)), F, T, T)))

              val ess =
                if (data._1 <= PValueTop) {
                  Set[WebAPIException](TypeMismatchError)
                }
                else TizenHelper.TizenExceptionBot

              val locset = data._2.foldLeft(LocSetBot)((_L, l) => {
                if (Helper.IsArray(h_2, l) <= T) _L + l
                else _L
              })
              val o_new3 = o_new2.update("data", PropValue(ObjectValue(Value(locset), F, T, T)))

              val es2 =
                if (data._2.exists((l) =>  Helper.IsArray(h_2, l) <= F))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              (h_2.update(l_r2, o_new3), es2 ++ ess)
            case _ => {
              (HeapBot, TizenHelper.TizenExceptionBot)
            }
          }

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es1)
          ((Helper.ReturnStore(h_4, Value(l_r2)), ctx_2), (he + h_e, ctxe + ctx_e))
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