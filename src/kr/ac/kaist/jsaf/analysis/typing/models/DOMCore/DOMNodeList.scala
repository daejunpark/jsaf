/*******************************************************************************
    Copyright (c) 2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMCore

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc

object DOMNodeList extends DOM {
  private val name = "NodeList"

  /* predefined locatoins */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("length", AbsConstValue(PropValue(ObjectValue(Value(AbsNumber.alpha(0)), F, F, F)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F))))
  )

  /* prorotype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("item",   AbsBuiltinFunc("DOMNodeList.item", 1))
  )

  /* global */
  private val prop_global: List[(String, AbsProperty)] = List(
    (name, AbsConstValue(PropValue(ObjectValue(loc_cons, T, F, T))))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto), (GlobalLoc, prop_global)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("DOMNodeList.item" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          /* arguments */
          val n_index = Helper.toNumber(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          if (n_index </ NumBot) {
            val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
            val n_length = lset_this.foldLeft[AbsNumber](NumBot)((n, l) =>
              n + Helper.toNumber(Helper.toPrimitive(Helper.Proto(h, l, AbsString.alpha("length")))))
            val s_index = Helper.toString(PValue(n_index))
            // Returns the indexth item in the collection.
            val v_return = lset_this.foldLeft(ValueBot)((v, l) => v + Helper.Proto(h, l, s_index))
            // If index is greater than or equal to the number of nodes in the list, this returns null.
            val v_null = (n_index < n_length) match {
              case BoolBot | BoolTrue  => ValueBot
              case BoolTop | BoolFalse => Value(NullTop)
            }
            ((Helper.ReturnStore(h, v_return + v_null), ctx), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }))
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("DOMNodeList.item" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          /* arguments */
          val n_index = PreHelper.toNumber(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
          if (n_index </ NumBot) {
            val lset_this = h(PureLocalLoc)("@this")._1._2._2
            val n_length = lset_this.foldLeft[AbsNumber](NumBot)((n, l) =>
              n + PreHelper.toNumber(PreHelper.toPrimitive(Helper.Proto(h, l, AbsString.alpha("length")))))
            val s_index = PreHelper.toString(PValue(n_index))
            // Returns the indexth item in the collection.
            val v_return = lset_this.foldLeft(ValueBot)((v, l) => v + PreHelper.Proto(h, l, s_index))
            // If index is greater than or equal to the number of nodes in the list, this returns null.
            val v_null = (n_index < n_length) match {
              case BoolBot | BoolTrue  => ValueBot
              case BoolTop | BoolFalse => Value(NullTop)
            }
            ((PreHelper.ReturnStore(h, PureLocalLoc, v_return + v_null), ctx), (he, ctxe))
          }
          else
            ((h, ctx), (he, ctxe))
        }))
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("DOMNodeList.item" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }))
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      ("DOMNodeList.item" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          /* arguments */
          val n_index = Helper.toNumber(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          val LP1 = getArgValue_use(h, ctx, args, "0")
          if (n_index </ NumBot) {
            val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
            val LP2 = lset_this.foldLeft(LPBot)((lpset, l) =>
              lpset ++ AccessHelper.Proto_use(h, l, AbsString.alpha("length")))
            val s_index = Helper.toString(PValue(n_index))
            // Returns the indexth item in the collection.
            val LP3 = lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AccessHelper.Proto_use(h, l, s_index))
            LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
          }
          else
            LP1
        }))
    )
  }

  /* instance */
  override def getInstance(cfg: CFG): Option[Loc] = Some(addrToLoc(cfg.newProgramAddr, Recent))
  /* list of properties in the instance object */
  def getInsList(length: Int): List[(String, PropValue)] = List(
    ("@class",  PropValue(AbsString.alpha("Object"))),
    ("@proto",  PropValue(ObjectValue(loc_proto, F, F, F))),
    ("@extensible", PropValue(BoolTrue)),
    // DOM Level 1
    ("length",   PropValue(ObjectValue(AbsNumber.alpha(length), F, T, T)))
  )
  def getInsList(length: PropValue): List[(String, PropValue)] = List(
    ("@class",  PropValue(AbsString.alpha("Object"))),
    ("@proto",  PropValue(ObjectValue(loc_proto, F, F, F))),
    ("@extensible", PropValue(BoolTrue)),
    // DOM Level 1
    ("length",   length)
  )

  def getInsListTop(top_loc: Loc): List[(String, PropValue)] = 
    getInsList(PropValue(ObjectValue(UInt, F, T, T))) ++ List(
    ("@default_number", PropValue(ObjectValue(Value(top_loc), T, T, T)))
  )


}
