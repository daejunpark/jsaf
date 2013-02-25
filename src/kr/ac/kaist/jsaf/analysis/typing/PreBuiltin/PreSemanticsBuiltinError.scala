/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.Operator._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.analysis.typing.{PreSemanticsExpr => SE}

import scala.math.{min,max,floor, abs}

object PreSemanticsBuiltinError {
  def builtinError(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1
    val lset_this = h(PureLocalLoc)("@this")._1._2._2

    fun match {
      case "Error.constructor" => {
        val v_arg = getArgValue(h, ctx, "0")
        val l_e = ErrLoc
        val s = PreHelper.toString(PreHelper.toPrimitive(v_arg))
        val h_1 =
          h.update(l_e, h(ErrLoc).update("message", PropValue(ObjectValue(s,BoolTrue,BoolFalse,BoolTrue))))
        ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(l_e)), ctx), (he, ctxe))
      }
      case "Error.prototype.toString" => {
        val s_empty = AbsString.alpha("")
        val v_name = lset_this.foldLeft(ValueBot)((v, l) => v + PreHelper.Proto(h, l, AbsString.alpha("name")))
        val v_msg = lset_this.foldLeft(ValueBot)((v, l) => v + PreHelper.Proto(h, l, AbsString.alpha("message")))
        val s_1 =
          if (v_name._1._1 </ UndefBot)
            AbsString.alpha("Error")
          else
            StrBot
        val s_2 = PreHelper.toString(PValue(UndefBot, v_name._1._2, v_name._1._3, v_name._1._4, v_name._1._5))
        val s_name = s_1 + s_2
        val s_3 =
          if (v_msg._1._1 </ UndefBot)
            s_empty
          else
            StrBot
        val s_4 = PreHelper.toString(PValue(UndefBot, v_msg._1._2, v_msg._1._3, v_msg._1._4, v_msg._1._5))
        val s_msg = s_3 + s_4
        val s_5 =
          if (s_empty <= s_name)
            s_msg
          else
            StrBot
        val s_6 =
          if (s_empty <= s_msg)
            s_name
          else
            StrBot
        val s_7 = bopPlus(bopPlus(Value(s_name), Value(AbsString.alpha(": "))), Value(s_msg))._1._5
        val s_ret = s_5 + s_6 + s_7

        ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s_ret)), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

  def builtinEvalError(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1

    fun match {
      case "EvalError.constructor" => {
        val v_arg = getArgValue(h, ctx, "0")
        val l_e = EvalErrLoc
        val s = PreHelper.toString(PreHelper.toPrimitive(v_arg))
        val h_1 = h.update(l_e, h(l_e).update("message", PropValue(ObjectValue(s,BoolTrue,BoolFalse,BoolTrue))))
        ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(l_e)), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

  def builtinRangeError(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1

    fun match {
      case "RangeError.constructor" => {
        val v_arg = getArgValue(h, ctx, "0")
        val l_e = RangeErrLoc
        val s = PreHelper.toString(PreHelper.toPrimitive(v_arg))
        val h_1 = h.update(l_e, h(l_e).update("message", PropValue(ObjectValue(s,BoolTrue,BoolFalse,BoolTrue))))
        ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(l_e)), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

  def builtinReferenceError(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1

    fun match {
      case "ReferenceError.constructor" => {
        val v_arg = getArgValue(h, ctx, "0")
        val l_e = RefErrLoc
        val s = PreHelper.toString(PreHelper.toPrimitive(v_arg))
        val h_1 = h.update(l_e, h(l_e).update("message", PropValue(ObjectValue(s,BoolTrue,BoolFalse,BoolTrue))))
        ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(l_e)), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

  def builtinSyntaxError(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1

    fun match {
      case "SyntaxError.constructor" => {
        val v_arg = getArgValue(h, ctx, "0")
        val l_e = SyntaxErrLoc
        val s = PreHelper.toString(PreHelper.toPrimitive(v_arg))
        val h_1 = h.update(l_e, h(l_e).update("message", PropValue(ObjectValue(s,BoolTrue,BoolFalse,BoolTrue))))
        ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(l_e)), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

  def builtinTypeError(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1

    fun match {
      case "TypeError.constructor" => {
        val v_arg = getArgValue(h, ctx, "0")
        val l_e = TypeErrLoc
        val s = PreHelper.toString(PreHelper.toPrimitive(v_arg))
        val h_1 = h.update(l_e, h(l_e).update("message", PropValue(ObjectValue(s,BoolTrue,BoolFalse,BoolTrue))))
        ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(l_e)), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

  def builtinURIError(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1

    fun match {
      case "URIError.constructor" => {
        val v_arg = getArgValue(h, ctx, "0")
        val l_e = URIErrLoc
        val s = PreHelper.toString(PreHelper.toPrimitive(v_arg))
        val h_1 = h.update(l_e, h(l_e).update("message", PropValue(ObjectValue(s,BoolTrue,BoolFalse,BoolTrue))))
        ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(l_e)), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
