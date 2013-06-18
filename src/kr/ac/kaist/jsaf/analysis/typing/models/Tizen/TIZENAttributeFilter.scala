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
import scala.Some
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
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context

object TIZENAttributeFilter extends Tizen {
  val name = "AttributeFilter"
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
    ("@construct",               AbsInternalFunc("tizen.AttributeFilter.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("attributeName", AbsConstValue(PropValue(Value(UndefTop)))),
    ("matchFlag", AbsConstValue(PropValue(Value(UndefTop)))),
    ("matchValue", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T)))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.AttributeFilter.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val attrname = getArgValue(h_1, ctx_1, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

          val o_new = ObjEmpty.update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAttributeFilter.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("attributeName", PropValue(ObjectValue(Value(Helper.toString(attrname._1)), F, T, T)))

          val (h_2, es) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              // case for "new tizen.AttributeFilter(attrname)"
              val o_new2 = o_new.
                update("matchFlag", PropValue(ObjectValue(Value(AbsString.alpha("EXACTLY")), F, T, T))).
                update("matchValue", PropValue(ObjectValue(Value(UndefTop), F, T, T)))
              (h_1.update(l_r, o_new2), TizenHelper.TizenExceptionBot)

            case UIntSingle(n) if n == 2 =>
              // case for "new tizen.AttributeFilter(attrname, matchflg)"
              val matchflg = getArgValue(h_1, ctx_1, args, "1")
              /* matchflg checking */
              val es_1 =
                if (matchflg._1._3 <= BoolTop || matchflg._1._4 <= NumTop || (matchflg._1._5 </ AbsString.alpha("EXACTLY") &&
                  matchflg._1._5 </ AbsString.alpha("FULLSTRING") &&
                  matchflg._1._5 </ AbsString.alpha("CONTAINS") && matchflg._1._5 </ AbsString.alpha("STARTSWITH") &&
                  matchflg._1._5 </ AbsString.alpha("ENDSWITH") && matchflg._1._5 </ AbsString.alpha("EXISTS")))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot

              val o_new3 = o_new.
                update("matchFlag", PropValue(ObjectValue(Value(Helper.toString(matchflg._1)), F, T, T))).
                update("matchValue", PropValue(ObjectValue(Value(UndefTop), F, T, T)))
              (h_1.update(l_r, o_new3), es_1)
            case UIntSingle(n) if n == 3 =>
              // case for "new tizen.AttributeFilter(attrname, matchflg, matchval)"
              val matchflg = getArgValue(h_1, ctx_1, args, "1")
              val matchval = getArgValue(h_1, ctx_1, args, "2")
              /* matchflg checking */
              val es_1 =
                if (matchflg._1._3 <= BoolTop || matchflg._1._4 <= NumTop || (matchflg._1._5 </ AbsString.alpha("EXACTLY") &&
                  matchflg._1._5 </ AbsString.alpha("FULLSTRING") &&
                  matchflg._1._5 </ AbsString.alpha("CONTAINS") && matchflg._1._5 </ AbsString.alpha("STARTSWITH") &&
                  matchflg._1._5 </ AbsString.alpha("ENDSWITH") && matchflg._1._5 </ AbsString.alpha("EXISTS")))
                  Set[WebAPIException](TypeMismatchError)
                else TizenHelper.TizenExceptionBot
              val o_new3 = o_new.
                update("matchFlag", PropValue(ObjectValue(Value(matchflg._1._5), F, T, T))).
                update("matchValue", PropValue(ObjectValue(Value(PValue(matchval._1._1, matchval._1._2, matchval._1._3, matchval._1._4,
                matchval._1._5), matchval._2), F, T, T)))
              (h_1.update(l_r, o_new3), es_1)
            case _ => {
              (h_1, TizenHelper.TizenExceptionBot)
            }
          }
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((Helper.ReturnStore(h_2, Value(l_r)), ctx_1), (he + h_e, ctxe + ctx_e))
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