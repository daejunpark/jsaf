/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.models.jquery

import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.{AccessHelper => AH, _}
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.cfg._

object JQueryUtility extends ModelData {
  private val prop_const: List[(String, AbsProperty)] = List(
    ("contains",      AbsBuiltinFunc("jQuery.contains", 2)),
    ("each",          AbsBuiltinFuncAftercall("jQuery.each", 3)),
    ("extend",        AbsBuiltinFunc("jQuery.extend", 0)),
    ("globalEval",    AbsBuiltinFunc("jQuery.globalEval", 1)),
    ("grep",          AbsBuiltinFunc("jQuery.grep", 3)),
    ("inArray",       AbsBuiltinFunc("jQuery.inArray", 3)),
    ("isArray",       AbsBuiltinFunc("jQuery.isArray", 1)),
    ("isEmptyObject", AbsBuiltinFunc("jQuery.isEmptyObject", 1)),
    ("isFunction",    AbsBuiltinFunc("jQuery.isFunction", 1)),
    ("isNumeric",     AbsBuiltinFunc("jQuery.isNumeric", 1)),
    ("isPlainObject", AbsBuiltinFunc("jQuery.isPlainObject", 1)),
    ("isWindow",      AbsBuiltinFunc("jQuery.isWindow", 1)),
    ("isXMLDoc",      AbsBuiltinFunc("jQuery.isXMLDoc", 1)),
    ("makeArray",     AbsBuiltinFunc("jQuery.makeArray", 2)),
    ("map",           AbsBuiltinFuncAftercall("jQuery.map", 3)),
    ("merge",         AbsBuiltinFunc("jQuery.merge", 2)),
    ("noop",          AbsBuiltinFunc("jQuery.noop", 0)),
    ("now",           AbsBuiltinFunc("jQuery.now", 0)),
    ("parseHTML",     AbsBuiltinFunc("jQuery.parseHTML", 3)),
    ("parseJSON",     AbsBuiltinFunc("jQuery.parseJSON", 1)),
    ("parseXML",      AbsBuiltinFunc("jQuery.parseXML", 1)),
    ("trim",          AbsBuiltinFunc("jQuery.trim", 1)),
    ("type",          AbsBuiltinFunc("jQuery.type", 1)),
    ("unique",        AbsBuiltinFunc("jQuery.unique", 1))
  )

  private val prop_proto: List[(String, AbsProperty)] = List(
    ("extend",   AbsBuiltinFunc("jQuery.prototype.extend", 0))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (JQuery.ConstLoc, prop_const), (JQuery.ProtoLoc, prop_proto)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {

    Map(
      ("jQuery.each" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          /* new addr */
          val list_addr = getAddrList(h, cfg)
          val addr1 = list_addr(0)
          val addr2 = list_addr(1)
          val addr3 = list_addr(2)
          /* new loc */
          val l_arg = addrToLoc(addr1, Recent)
          val l_cc = addrToLoc(addr2, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)
          val (h_3, ctx_3) = Helper.Oldify(h_2, ctx_2, addr3)

          /* target */
          val lset_target = getArgValue(h_3, ctx_3, args, "0")._2
          /* fun */
          val lset_fun = getArgValue(h_3, ctx_3, args, "1")._2.filter((l) => BoolTrue <= Helper.IsCallable(h,l))

          val b_isarr = lset_target.foldLeft[AbsBool](BoolBot)((b, l) => b + JQueryHelper.isArraylike(h_3, l))
          // arg1
          val v_index =
            b_isarr match {
              case BoolTop => Value(UInt) + Value(StrTop)
              case BoolTrue => Value(UInt)
              case BoolFalse => Value(StrTop)
              case BoolBot => ValueBot
            }
          val s_index = Helper.toString(v_index._1)
          // arg2
          val v_elem = lset_target.foldLeft(ValueBot)((v, l) => v + Helper.Proto(h_3, l, s_index))

          // create Arguments object
          val o_arg = Helper.NewArgObject(AbsNumber.alpha(2))
            .update("0", PropValue(ObjectValue(v_index, T, T, T)))
            .update("1", PropValue(ObjectValue(v_elem, T, T, T)))
          val h_4 = h_3.update(l_arg, o_arg)
          val v_arg = Value(l_arg)

          val lset_argthis = Helper.getThis(h_4, v_elem)
          val v_this2 = Value(PValue(UndefBot, NullBot, v_elem._1._3, v_elem._1._4, v_elem._1._5), lset_argthis)
          val (callee_this, h_5, ctx_5, es3) = Helper.toObject(h_4, ctx_3, v_this2, addr3)


          val o_old = h_5(SinglePureLocalLoc)
          val cc_caller = cp._2
          val n_aftercall = cfg.getAftercallFromCall(cp._1)
          val cp_aftercall = (n_aftercall, cc_caller)
          val n_aftercatch = cfg.getAftercatchFromCall(cp._1)
          val cp_aftercatch = (n_aftercatch, cc_caller)
          lset_fun.foreach((l_f) => {
            val o_f = h_5(l_f)
            o_f("@function")._1._3.foreach((fid) => {
              cc_caller.NewCallContext(cfg, fid, l_cc, callee_this._2).foreach((pair) => {
                val (cc_new, o_new) = pair
                val o_new2 = o_new.
                  update(cfg.getArgumentsName(fid),
                  PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))).
                  update("@scope", o_f("@scope")._1)
                sem.addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
                sem.addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_5, o_old)
                sem.addReturnEdge(((fid, LExitExc), cc_new), cp_aftercatch, ctx_5, o_old)
              })
            })
          })
          val h_6 = v_arg._2.foldLeft(HeapBot)((hh, l) => {
            hh + h_5.update(l, h_5(l).update("callee",
              PropValue(ObjectValue(Value(lset_fun), BoolTrue, BoolFalse, BoolTrue))))
          })

          ((h_6, ctx_5), (he, ctxe))
        })),
      ("jQuery.isArray" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val b_1 =
            if (v._1 </ PValueBot) BoolFalse
            else BoolBot
          val b_2 = v._2.foldLeft[AbsBool](BoolBot)((_b, l) => {
            // XXX : Check whether it is correct or not
            if (!h.domIn(l)) BoolBot
            else  {
              val _b1 =
                if (AbsString.alpha("Array") <= h(l)("@class")._1._2._1._5) BoolTrue
                else BoolBot
              val _b2 =
                if (AbsString.alpha("Array") </ h(l)("@class")._1._2._1._5) BoolFalse
                else BoolBot
              _b + _b1 + _b2}})
          val b = b_1 + b_2
          if (b </ BoolBot)
            ((Helper.ReturnStore(h, Value(b)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("jQuery.extend" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_len = getArgValue(h, ctx, args, "length")._1._4
          val (h_ret, v_ret) =
            AbsNumber.concretize(n_len) match {
              case Some(n) =>
                val list_args = (0 until n.toInt).foldLeft(List[Value]())((list, i) => list :+ getArgValue(h, ctx, args, i.toString))
                JQueryHelper.extend(h, list_args)
              case None =>
                if (n_len </ NumBot)
                // giveup, unsound
                  (h, Value(h(SinglePureLocalLoc)("@this")._1._2._2))
                else
                  (HeapBot, ValueBot)
            }
          if (!(h_ret <= HeapBot))
            ((Helper.ReturnStore(h_ret, v_ret), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("jQuery.prototype.extend" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_len = getArgValue(h, ctx, args, "length")._1._4
          val (h_ret, v_ret) =
            AbsNumber.concretize(n_len) match {
              case Some(n) =>
                val list_args = (0 until n.toInt).foldLeft(List[Value]())((list, i) => list :+ getArgValue(h, ctx, args, i.toString))
                JQueryHelper.extend(h, list_args)
              case None =>
                if (n_len </ NumBot)
                // giveup, unsound
                  (h, Value(h(SinglePureLocalLoc)("@this")._1._2._2))
                else
                  (HeapBot, ValueBot)
            }
          if (!(h_ret <= HeapBot))
            ((Helper.ReturnStore(h_ret, v_ret), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("jQuery.trim" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val s_arg = getArgValue(h, ctx, args, "0")._1._5
          val v_ret = AbsString.concretize(s_arg) match {
            case Some(s) =>
              Value(AbsString.alpha(s.trim()))
            case None =>
              if (s_arg </ StrBot)
                Value(StrTop)
              else
                ValueBot
          }
          if (s_arg </ StrBot)
            ((Helper.ReturnStore(h, v_ret), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("jQuery.type" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_arg = getArgValue(h, ctx, args, "0")
          val pv_arg = v_arg._1
          val s_1 =
            if (pv_arg._1 </ UndefBot)
              AbsString.alpha("undefined")
            else
              StrBot
          val s_2 =
            if (pv_arg._2 </ NullBot)
              AbsString.alpha("null")
            else
              StrBot
          val s_3 =
            if (pv_arg._3 </ BoolBot)
              AbsString.alpha("boolean")
            else
              StrBot
          val s_4 =
            if (pv_arg._4 </ NumBot)
              AbsString.alpha("number")
            else
              StrBot
          val s_5 =
            if (pv_arg._5 </ StrBot)
              AbsString.alpha("string")
            else
              StrBot
          // [[class]] ?
          val s_6 = v_arg._2.foldLeft[AbsString](StrBot)((s, l) => {
            val s_class = h(l)("@class")._1._2._1._5
            AbsString.concretize(s_class) match {
              case Some(name) =>
                if (name.contains("Error"))
                  s + AbsString.alpha("error")
                else if (name.equals("Arguments"))
                  s + AbsString.alpha("object")
                else
                  s + AbsString.alpha(name.toLowerCase)
              case None =>
                // should not be happen
                s
            }

          })
          val s_ret = s_1 + s_2 + s_3 + s_4 + s_5 + s_6
          if (s_ret </ StrBot)
            ((Helper.ReturnStore(h, Value(s_ret)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }))
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map()
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map()
  }
}
