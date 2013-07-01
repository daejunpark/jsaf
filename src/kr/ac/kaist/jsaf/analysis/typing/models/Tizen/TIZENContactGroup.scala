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
import kr.ac.kaist.jsaf.analysis.typing.models.builtin.BuiltinDate
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context

object TIZENContactGroup extends Tizen {
  private val name = "ContactGroup"
  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")
  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("tizen.ContactGroup.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F))))
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
      ("tizen.ContactGroup.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val v_1 = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENContactGroup.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("name", PropValue(ObjectValue(Value(Helper.toString(v_1._1)), T, T, T)))
          val (h_2, es) = n_arglen match {
            case UIntSingle(n) if n <= 1 =>
              val o_new2 = o_new.
                update("id", PropValue(ObjectValue(Value(NullTop), F, T, T))).
                update("addressBookId", PropValue(ObjectValue(Value(NullTop), F, T, T))).
                update("ringtoneURI", PropValue(ObjectValue(Value(NullTop), T, T, T))).
                update("photoURI", PropValue(ObjectValue(Value(NullTop), T, T, T))).
                update("readOnly", PropValue(ObjectValue(Value(F), F, T, T)))
              val h_2 = lset_this.foldLeft(h)((_h, l) => _h.update(l, o_new2))
              (h_2, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n == 2 =>
              val v_2 = getArgValue(h, ctx, args, "1")
              val o_new2 = o_new.
                update("id", PropValue(ObjectValue(Value(NullTop), F, T, T))).
                update("addressBookId", PropValue(ObjectValue(Value(NullTop), F, T, T))).
                update("ringtoneURI", PropValue(ObjectValue(Value(Helper.toString(v_2._1)), T, T, T))).
                update("photoURI", PropValue(ObjectValue(Value(NullTop), T, T, T))).
                update("readOnly", PropValue(ObjectValue(Value(F), F, T, T)))
              val h_2 = lset_this.foldLeft(h)((_h, l) => _h.update(l, o_new2))
              (h_2, TizenHelper.TizenExceptionBot)
            case UIntSingle(n) if n >= 3 =>
              val v_2 = getArgValue(h, ctx, args, "1")
              val v_3 = getArgValue(h, ctx, args, "2")
              val o_new2 = o_new.
                update("id", PropValue(ObjectValue(Value(NullTop), F, T, T))).
                update("addressBookId", PropValue(ObjectValue(Value(NullTop), F, T, T))).
                update("ringtoneURI", PropValue(ObjectValue(Value(Helper.toString(v_2._1)), T, T, T))).
                update("photoURI", PropValue(ObjectValue(Value(Helper.toString(v_3._1)), T, T, T))).
                update("readOnly", PropValue(ObjectValue(Value(F), F, T, T)))
              val h_2 = lset_this.foldLeft(h)((_h, l) => _h.update(l, o_new2))
              (h_2, TizenHelper.TizenExceptionBot)
            case _ => {
              (h, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_2, Value(lset_this)), ctx), (he + h_e, ctxe + ctx_e))
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