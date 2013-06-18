/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.builtin

import kr.ac.kaist.jsaf.analysis.cfg.{CFGExpr, CFG}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.{AccessHelper=>AH}

object BuiltinMath extends ModelData {

  val ConstLoc = newPreDefLoc("MathConst", Recent)

  private val prop_const: List[(String, AbsProperty)] = List(
    ("@class",      AbsConstValue(PropValue(AbsString.alpha("Math")))),
    ("@proto",      AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("constructor", AbsConstValue(PropValue(ObjectValue(Value(BuiltinObject.ConstLoc), F, F, F)))),
    ("E",       AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(2.7182818284590452354), F, F, F)))),
    ("LN10",    AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(2.302585092994046), F, F, F)))),
    ("LN2",     AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0.6931471805599453), F, F, F)))),
    ("LOG2E",   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1.4426950408889634), F, F, F)))),
    ("LOG10E",  AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0.4342944819032518), F, F, F)))),
    ("PI",      AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(3.1415926535897932), F, F, F)))),
    ("SQRT1_2", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0.7071067811865476), F, F, F)))),
    ("SQRT2",   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1.4142135623730951), F, F, F)))),
    // 15.8.2 Function Properties of the Math Object
    ("abs",     AbsBuiltinFunc("Math.abs",    1)),
    ("acos",    AbsBuiltinFunc("Math.acos",   1)),
    ("asin",    AbsBuiltinFunc("Math.asin",   1)),
    ("atan",    AbsBuiltinFunc("Math.atan",   1)),
    ("atan2",   AbsBuiltinFunc("Math.atan2",  1)),
    ("ceil",    AbsBuiltinFunc("Math.ceil",   1)),
    ("cos",     AbsBuiltinFunc("Math.cos",    1)),
    ("exp",     AbsBuiltinFunc("Math.exp",    1)),
    ("floor",   AbsBuiltinFunc("Math.floor",  1)),
    ("log",     AbsBuiltinFunc("Math.log",    1)),
    ("max",     AbsBuiltinFunc("Math.max",    2)),
    ("min",     AbsBuiltinFunc("Math.min",    2)),
    ("pow",     AbsBuiltinFunc("Math.pow",    2)),
    ("random",  AbsBuiltinFunc("Math.random", 0)),
    ("round",   AbsBuiltinFunc("Math.round",  1)),
    ("sin",     AbsBuiltinFunc("Math.sin",    1)),
    ("sqrt",    AbsBuiltinFunc("Math.sqrt",   1)),
    ("tan",     AbsBuiltinFunc("Math.tan",    1))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (ConstLoc, prop_const)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("name" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((HeapBot, ContextBot),(HeapBot, ContextBot))
        }
        )),
      ("Math.abs" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN|UInt|NUInt => Value(v)
            case NegInf|PosInf|Infinity =>  Value(Infinity)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.abs(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.abs(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.acos" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot => Value(v)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>
              if (-1>n || 1 < n) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.acos(n)))
            case NUIntSingle(n) =>
              if (-1>n || 1 < n) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.acos(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.asin" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot => Value(v)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>
              if (-1>n || 1<n) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.asin(n)))
            case NUIntSingle(n) =>
              if (-1>n || 1<n) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.asin(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.atan" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN =>  Value(v)
            case Infinity =>  Value(NUInt)
            case PosInf =>  Value(NUIntSingle(scala.math.Pi/2))
            case NegInf =>  Value(NUIntSingle(-scala.math.Pi/2))
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.atan(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.atan(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.atan2" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v_2 = getArgValue(h, ctx, args, "1") /* Value */
          val vy = Helper.toNumber(Helper.toPrimitive(v_1))
          val vx = Helper.toNumber(Helper.toPrimitive(v_2))
          val rtn = (vy, vx) match {
            case (NumBot, _) =>  Value(NumBot)
            case (_, NumBot) =>  Value(NumBot)
            case (NaN, _) =>  Value(NaN)
            case (_, NaN) =>  Value(NaN)
            case (NumTop, _) =>  Value(NumTop)
            case (_, NumTop) =>  Value(NumTop)

            case (UInt|NUInt, PosInf) =>  Value(UIntSingle(0))
            case (UIntSingle(n), PosInf) =>  Value(UIntSingle(0))
            case (NUIntSingle(n), PosInf) =>  Value(UIntSingle(0))

            case (UInt|NUInt, NegInf) =>  Value(UInt)
            case (UIntSingle(n), NegInf) =>  Value(NUIntSingle(scala.math.Pi))
            case (NUIntSingle(n), NegInf) =>
              if (n < 0) Value(NUIntSingle(scala.math.Pi))
              else Value(NUIntSingle(-scala.math.Pi))

            case (UInt|NUInt, Infinity) =>  Value(UInt)

            case (PosInf, UInt|NUInt) =>  Value(NUIntSingle(scala.math.Pi/2))
            case (PosInf, UIntSingle(n)) =>  Value(NUIntSingle(scala.math.Pi/2))
            case (PosInf, NUIntSingle(n)) =>  Value(NUIntSingle(scala.math.Pi/2))

            case (NegInf, UInt|NUInt) =>  Value(NUIntSingle(-scala.math.Pi/2))
            case (NegInf, UIntSingle(n)) =>  Value(NUIntSingle(-scala.math.Pi/2))
            case (NegInf, NUIntSingle(n)) =>  Value(NUIntSingle(-scala.math.Pi/2))

            case (PosInf, PosInf) =>  Value(NUIntSingle(scala.math.Pi/4))
            case (PosInf, NegInf) =>  Value(NUIntSingle(3*scala.math.Pi/4))
            case (NegInf, PosInf) =>  Value(NUIntSingle(-scala.math.Pi/4))
            case (NegInf, NegInf) =>  Value(NUIntSingle(-3*scala.math.Pi/4))

            case (Infinity, Infinity|PosInf|NegInf) =>  Value(NUInt)
            case (Infinity|PosInf|NegInf, Infinity) =>  Value(NUInt)

            case (UIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.atan2(n1, n2)))
            case (UIntSingle(n1), NUIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.atan2(n1, n2)))
            case (NUIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.atan2(n1, n2)))
            case (NUIntSingle(n1), NUIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.atan2(n1, n2)))

            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.ceil" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN|Infinity|PosInf|NegInf|UInt =>  Value(v)
            case UIntSingle(n) =>  Value(v)
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.ceil(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.cos" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot =>  Value(NumBot)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.cos(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.cos(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.exp" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN|PosInf|NUInt =>  Value(v)
            case Infinity|UInt|NumTop =>  Value(NumTop)
            case NegInf =>  Value(UIntSingle(0))
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.exp(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.exp(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.floor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NumTop|NaN|Infinity|PosInf|NegInf|UInt =>  Value(v)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.floor(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.floor(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.max" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_arglen = getArgValue(h, ctx, args, "length")._1._4
          def n_arg = (i : Int) => Helper.toNumber(Helper.toPrimitive(getArgValue(h, ctx, args, i.toString)))
          val n_1 =
            n_arglen match {
              case UIntSingle(n) =>
                if (n == 0)
                  NegInf
                else {
                  val n_3 =
                    if ((0 to n.toInt -1).exists((i:Int) => NaN <= n_arg(i)))
                      NaN
                    else
                      NumBot
                  n_3 + (0 to n.toInt -1).foldLeft[AbsNumber](NumBot)(
                    (an, i)=> {
                      (0 to n.toInt -1).foldLeft(an)(
                        (ann, j) => {
                          if (Value(BoolTrue) <= Operator.bopGreaterEq(Value(n_arg(i)), Value(n_arg(j))))
                            ann + n_arg(i)
                          else
                            ann
                        })
                    })
                }
              case NUIntSingle(_)|NaN|NegInf|PosInf|Infinity|UInt|NUInt|NumTop => NumTop
              case NumBot => NumBot
            }
          ((Helper.ReturnStore(h, Value(n_1)), ctx), (he, ctxe))
        })),
      ("Math.min" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_arglen = getArgValue(h, ctx, args, "length")._1._4
          def n_arg = (i : Int) => Helper.toNumber(Helper.toPrimitive(getArgValue(h, ctx, args, i.toString)))
          val n_1 =
            n_arglen match {
              case UIntSingle(n) =>
                if (n == 0)
                  PosInf
                else {
                  val n_3 =
                    if ((0 to n.toInt -1).exists((i:Int) => NaN <= n_arg(i)))
                      NaN
                    else
                      NumBot
                  n_3 + (0 to n.toInt -1).foldLeft[AbsNumber](NumBot)(
                    (an, i)=> {
                      (0 to n.toInt -1).foldLeft(an)(
                        (ann, j) => {
                          if (Value(BoolTrue) <= Operator.bopLessEq(Value(n_arg(i)), Value(n_arg(j))))
                            ann + n_arg(i)
                          else
                            ann
                        })
                    })
                }
              case NUIntSingle(_)|NaN|NegInf|PosInf|Infinity|UInt|NUInt|NumTop => NumTop
              case NumBot => NumBot
            }
          ((Helper.ReturnStore(h, Value(n_1)), ctx), (he, ctxe))
        })),
      ("Math.pow" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v_2 = getArgValue(h, ctx, args, "1") /* Value */
          val vx = Helper.toNumber(Helper.toPrimitive(v_1))
          val vy = Helper.toNumber(Helper.toPrimitive(v_2))
          val rtn = (vx, vy) match {
            case (NumTop, _) => Value(vx)
            case (_, NumTop) => Value(vy)

            case (NumBot, _) => Value(NumBot)
            case (_, NumBot) => Value(NumBot)

            case (_, NaN) =>  Value(NaN)
            case (_, UIntSingle(0)) =>  Value(UIntSingle(1))
            case (NaN, _) =>  Value(NaN)

            case (UIntSingle(0), PosInf) =>  Value(UIntSingle(0))
            case (UIntSingle(0), NegInf) =>  Value(PosInf)
            case (UIntSingle(0), UIntSingle(n)) =>  Value(UIntSingle(0))
            case (UIntSingle(0), NUIntSingle(n)) =>
              if (n<0) Value(PosInf)
              else Value(UIntSingle(0))

            case (PosInf|NegInf|Infinity, PosInf) =>  Value(PosInf)
            case (PosInf|NegInf|Infinity, NegInf) =>  Value(UIntSingle(0))

            case (PosInf, UIntSingle(n)) =>  Value(PosInf)
            case (PosInf, NUIntSingle(n)) =>
              if (0<n) Value(PosInf)
              else Value(UIntSingle(0))

            case (NegInf, UIntSingle(n)) =>
              if (n%2!=1) Value(PosInf)
              else Value(NegInf)
            case (NegInf, NUIntSingle(n)) =>
              if (0<n) {
                if (n%2!=1) Value(PosInf)
                else Value(NegInf)
              } else {
                Value(UIntSingle(0))
              }

            case (Infinity, UIntSingle(n)) =>
              if (n%2!=1) Value(PosInf)
              else Value(NumTop)
            case (Infinity, NUIntSingle(n)) =>
              if (n>0) {
                if (n%2 != 1) Value(PosInf)
                else Value(NumTop)
              } else {
                Value(UIntSingle(0))
              }

            case (UIntSingle(n), PosInf) =>
              if (n < -1 || 1 < n) Value(PosInf)
              else if (-1 < n && n < 1) Value(UIntSingle(0))
              else Value(NaN)
            case (NUIntSingle(n), PosInf) =>
              if (n < -1 || 1 < n) Value(PosInf)
              else if (-1 < n && n < 1) Value(UIntSingle(0))
              else Value(NaN)


            case (UIntSingle(n), NegInf) =>
              if (n < -1 || 1 < n) Value(UIntSingle(0))
              else if (-1 < n && n < 1) Value(PosInf)
              else Value(NaN)
            case (NUIntSingle(n), NegInf) =>
              if (n < -1 || 1 < n) Value(PosInf)
              else if (-1 < n && n < 1) Value(UIntSingle(0))
              else Value(NaN)

            case (UIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.pow(n1, n2)))
            case (UIntSingle(n1), NUIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.pow(n1, n2)))
            case (NUIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.pow(n1, n2)))
            case (NUIntSingle(n1), NUIntSingle(n2)) =>
              val intnum = n2.toInt
              val diff:Double = n2 - intnum.toDouble
              if (n1 < 0 && (diff != 0)) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.pow(n1, n2)))


            case _ => Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.log" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|PosInf =>  Value(v)
            case NaN|NegInf =>  Value(NaN)
            case UIntSingle(n) =>
              if (n<0) Value(NaN)
              else if (n==0) Value(NegInf)
              else Value(AbsNumber.alpha(scala.math.log(n)))
            case NUIntSingle(n) =>
              if (n<0) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.log(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.random" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          ((Helper.ReturnStore(h, Value(NumTop)), ctx), (he, ctxe))
        })),
      ("Math.round" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN|Infinity|PosInf|NegInf|UInt|NUInt => Value(v)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.round(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.round(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.sin" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot => Value(v)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.sin(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.sin(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.sqrt" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|PosInf => Value(v)
            case NaN|NegInf =>  Value(NaN)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.sqrt(n)))
            case NUIntSingle(n) =>
              if (n<0)   Value(NaN)
              else Value(AbsNumber.alpha(scala.math.sqrt(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        })),
      ("Math.tan" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0") /* Value */
          val v = Helper.toNumber(Helper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot => Value(v)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.tan(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.tan(n)))
            case _ =>  Value(NumTop)
          }
          ((Helper.ReturnStore(h, rtn), ctx), (he, ctxe))
        }))
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("Global.parseInt" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          // 15.1.2.2 parseInt(string, radix)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* string */
          val v_2 = getArgValue_pre(h, ctx, args, "1", PureLocalLoc) /* radix */

          val inputString = PreHelper.toString(PreHelper.toPrimitive(v_1))
          // TODO: Simple implementation. Must be revised. Not the same as the original.
          val r = Operator.ToInt32(v_2)

          val value = Operator.parseInt(inputString, r)
          val rtn = Value(value)

          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctx))
        })
        ),
      ("Math.abs" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN|UInt|NUInt => Value(v)
            case NegInf|PosInf|Infinity =>  Value(Infinity)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.abs(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.abs(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.acos" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot => Value(v)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>
              if (-1>n || 1 < n) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.acos(n)))
            case NUIntSingle(n) =>
              if (-1>n || 1 < n) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.acos(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.asin" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot => Value(v)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>
              if (-1>n || 1<n) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.asin(n)))
            case NUIntSingle(n) =>
              if (-1>n || 1<n) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.asin(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.atan" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN =>  Value(v)
            case Infinity =>  Value(NUInt)
            case PosInf =>  Value(NUIntSingle(scala.math.Pi/2))
            case NegInf =>  Value(NUIntSingle(-scala.math.Pi/2))
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.atan(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.atan(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.atan2" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v_2 = getArgValue_pre(h, ctx, args, "1", PureLocalLoc) /* Value */
          val vy = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val vx = PreHelper.toNumber(PreHelper.toPrimitive(v_2))
          val rtn = (vy, vx) match {
            case (NumBot, _) =>  Value(NumBot)
            case (_, NumBot) =>  Value(NumBot)
            case (NaN, _) =>  Value(NaN)
            case (_, NaN) =>  Value(NaN)
            case (NumTop, _) =>  Value(NumTop)
            case (_, NumTop) =>  Value(NumTop)

            case (UInt|NUInt, PosInf) =>  Value(UIntSingle(0))
            case (UIntSingle(n), PosInf) =>  Value(UIntSingle(0))
            case (NUIntSingle(n), PosInf) =>  Value(UIntSingle(0))

            case (UInt|NUInt, NegInf) =>  Value(UInt)
            case (UIntSingle(n), NegInf) =>  Value(NUIntSingle(scala.math.Pi))
            case (NUIntSingle(n), NegInf) =>
              if (n < 0) Value(NUIntSingle(scala.math.Pi))
              else Value(NUIntSingle(-scala.math.Pi))

            case (UInt|NUInt, Infinity) =>  Value(UInt)

            case (PosInf, UInt|NUInt) =>  Value(NUIntSingle(scala.math.Pi/2))
            case (PosInf, UIntSingle(n)) =>  Value(NUIntSingle(scala.math.Pi/2))
            case (PosInf, NUIntSingle(n)) =>  Value(NUIntSingle(scala.math.Pi/2))

            case (NegInf, UInt|NUInt) =>  Value(NUIntSingle(-scala.math.Pi/2))
            case (NegInf, UIntSingle(n)) =>  Value(NUIntSingle(-scala.math.Pi/2))
            case (NegInf, NUIntSingle(n)) =>  Value(NUIntSingle(-scala.math.Pi/2))

            case (PosInf, PosInf) =>  Value(NUIntSingle(scala.math.Pi/4))
            case (PosInf, NegInf) =>  Value(NUIntSingle(3*scala.math.Pi/4))
            case (NegInf, PosInf) =>  Value(NUIntSingle(-scala.math.Pi/4))
            case (NegInf, NegInf) =>  Value(NUIntSingle(-3*scala.math.Pi/4))

            case (Infinity, Infinity|PosInf|NegInf) =>  Value(NUInt)
            case (Infinity|PosInf|NegInf, Infinity) =>  Value(NUInt)

            case (UIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.atan2(n1, n2)))
            case (UIntSingle(n1), NUIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.atan2(n1, n2)))
            case (NUIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.atan2(n1, n2)))
            case (NUIntSingle(n1), NUIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.atan2(n1, n2)))

            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.ceil" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN|Infinity|PosInf|NegInf|UInt =>  Value(v)
            case UIntSingle(n) =>  Value(v)
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.ceil(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.cos" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot =>  Value(NumBot)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.cos(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.cos(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.exp" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN|PosInf|NUInt =>  Value(v)
            case Infinity|UInt|NumTop =>  Value(NumTop)
            case NegInf =>  Value(UIntSingle(0))
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.exp(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.exp(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.floor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NumTop|NaN|Infinity|PosInf|NegInf|UInt =>  Value(v)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.floor(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.floor(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.max" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val n_arglen = getArgValue_pre(h, ctx, args, "length", PureLocalLoc)._1._4
          def n_arg = (i : Int) => PreHelper.toNumber(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, i.toString, PureLocalLoc)))
          val n_1 =
            n_arglen match {
              case UIntSingle(n) =>
                if (n == 0)
                  NegInf
                else {
                  val n_3 =
                    if ((0 to n.toInt -1).exists((i:Int) => NaN <= n_arg(i)))
                      NaN
                    else
                      NumBot
                  n_3 + (0 to n.toInt -1).foldLeft[AbsNumber](NumBot)(
                    (an, i)=> {
                      (0 to n.toInt -1).foldLeft(an)(
                        (ann, j) => {
                          if (Value(BoolTrue) <= Operator.bopGreaterEq(Value(n_arg(i)), Value(n_arg(j))))
                            ann + n_arg(i)
                          else
                            ann
                        })
                    })
                }
              case NUIntSingle(_)|NaN|NegInf|PosInf|Infinity|UInt|NUInt|NumTop => NumTop
              case NumBot => NumBot
            }
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(n_1)), ctx), (he, ctxe))
        })),
      ("Math.min" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val n_arglen = getArgValue_pre(h, ctx, args, "length", PureLocalLoc)._1._4
          def n_arg = (i : Int) => PreHelper.toNumber(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, i.toString, PureLocalLoc)))
          val n_1 =
            n_arglen match {
              case UIntSingle(n) =>
                if (n == 0)
                  PosInf
                else {
                  val n_3 =
                    if ((0 to n.toInt -1).exists((i:Int) => NaN <= n_arg(i)))
                      NaN
                    else
                      NumBot
                  n_3 + (0 to n.toInt -1).foldLeft[AbsNumber](NumBot)(
                    (an, i)=> {
                      (0 to n.toInt -1).foldLeft(an)(
                        (ann, j) => {
                          if (Value(BoolTrue) <= Operator.bopLessEq(Value(n_arg(i)), Value(n_arg(j))))
                            ann + n_arg(i)
                          else
                            ann
                        })
                    })
                }
              case NUIntSingle(_)|NaN|NegInf|PosInf|Infinity|UInt|NUInt|NumTop => NumTop
              case NumBot => NumBot
            }
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(n_1)), ctx), (he, ctxe))
        })),
      ("Math.pow" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v_2 = getArgValue_pre(h, ctx, args, "1", PureLocalLoc) /* Value */
          val vx = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val vy = PreHelper.toNumber(PreHelper.toPrimitive(v_2))
          val rtn = (vx, vy) match {
            case (NumTop, _) => Value(vx)
            case (_, NumTop) => Value(vy)

            case (NumBot, _) => Value(NumBot)
            case (_, NumBot) => Value(NumBot)

            case (_, NaN) =>  Value(NaN)
            case (_, UIntSingle(0)) =>  Value(UIntSingle(1))
            case (NaN, _) =>  Value(NaN)

            case (UIntSingle(0), PosInf) =>  Value(UIntSingle(0))
            case (UIntSingle(0), NegInf) =>  Value(PosInf)
            case (UIntSingle(0), UIntSingle(n)) =>  Value(UIntSingle(0))
            case (UIntSingle(0), NUIntSingle(n)) =>
              if (n<0) Value(PosInf)
              else Value(UIntSingle(0))

            case (PosInf|NegInf|Infinity, PosInf) =>  Value(PosInf)
            case (PosInf|NegInf|Infinity, NegInf) =>  Value(UIntSingle(0))

            case (PosInf, UIntSingle(n)) =>  Value(PosInf)
            case (PosInf, NUIntSingle(n)) =>
              if (0<n) Value(PosInf)
              else Value(UIntSingle(0))

            case (NegInf, UIntSingle(n)) =>
              if (n%2!=1) Value(PosInf)
              else Value(NegInf)
            case (NegInf, NUIntSingle(n)) =>
              if (0<n) {
                if (n%2!=1) Value(PosInf)
                else Value(NegInf)
              } else {
                Value(UIntSingle(0))
              }

            case (Infinity, UIntSingle(n)) =>
              if (n%2!=1) Value(PosInf)
              else Value(NumTop)
            case (Infinity, NUIntSingle(n)) =>
              if (n>0) {
                if (n%2 != 1) Value(PosInf)
                else Value(NumTop)
              } else {
                Value(UIntSingle(0))
              }

            case (UIntSingle(n), PosInf) =>
              if (n < -1 || 1 < n) Value(PosInf)
              else if (-1 < n && n < 1) Value(UIntSingle(0))
              else Value(NaN)
            case (NUIntSingle(n), PosInf) =>
              if (n < -1 || 1 < n) Value(PosInf)
              else if (-1 < n && n < 1) Value(UIntSingle(0))
              else Value(NaN)


            case (UIntSingle(n), NegInf) =>
              if (n < -1 || 1 < n) Value(UIntSingle(0))
              else if (-1 < n && n < 1) Value(PosInf)
              else Value(NaN)
            case (NUIntSingle(n), NegInf) =>
              if (n < -1 || 1 < n) Value(PosInf)
              else if (-1 < n && n < 1) Value(UIntSingle(0))
              else Value(NaN)

            case (UIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.pow(n1, n2)))
            case (UIntSingle(n1), NUIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.pow(n1, n2)))
            case (NUIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(scala.math.pow(n1, n2)))
            case (NUIntSingle(n1), NUIntSingle(n2)) =>
              val intnum = n2.toInt
              val diff:Double = n2 - intnum.toDouble
              if (n1 < 0 && (diff != 0)) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.pow(n1, n2)))


            case _ => Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.log" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|PosInf =>  Value(v)
            case NaN|NegInf =>  Value(NaN)
            case UIntSingle(n) =>
              if (n<0) Value(NaN)
              else if (n==0) Value(NegInf)
              else Value(AbsNumber.alpha(scala.math.log(n)))
            case NUIntSingle(n) =>
              if (n<0) Value(NaN)
              else Value(AbsNumber.alpha(scala.math.log(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.random" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
        })),
      ("Math.round" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|NaN|Infinity|PosInf|NegInf|UInt|NUInt => Value(v)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.round(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.round(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.sin" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot => Value(v)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.sin(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.sin(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.sqrt" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot|PosInf => Value(v)
            case NaN|NegInf =>  Value(NaN)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.sqrt(n)))
            case NUIntSingle(n) =>
              if (n<0)   Value(NaN)
              else Value(AbsNumber.alpha(scala.math.sqrt(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        })),
      ("Math.tan" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc) /* Value */
          val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
          val rtn = v match {
            case NumBot => Value(v)
            case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
            case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.tan(n)))
            case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.tan(n)))
            case _ =>  Value(NumTop)
          }
          ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
        }))
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("Math.abs" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.acos" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.asin" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.atan" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.atan2" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.ceil" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.cos"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.exp"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.floor"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.max"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.min"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.pow"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.log"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.random"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.round" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.sin"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.sqrt"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.tan"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }))
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      ("Math.abs" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.acos" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.asin" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.atan" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.atan2" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.ceil" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.cos"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.exp"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.floor"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.max"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_arglen = getArgValue(h, ctx, args, "length")._1._4
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 = n_arglen match {
            case UIntSingle(n) =>
              (0 until n.toInt).foldLeft(LPBot)((lpset, i) => lpset ++ getArgValue_use(h, ctx, args, i.toString))
            case _ => LPBot
          }
          LP1 ++ LP2 + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.min"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_arglen = getArgValue(h, ctx, args, "length")._1._4
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 = n_arglen match {
            case UIntSingle(n) =>
              (0 until n.toInt).foldLeft(LPBot)((lpset, i) => lpset ++ getArgValue_use(h, ctx, args, i.toString))
            case _ => LPBot
          }
          LP1 ++ LP2 + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.pow"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.log"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.random"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Math.round" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.sin"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.sqrt"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        })),
      ("Math.tan"-> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          getArgValue_use(h, ctx, args, "0") + ((SinglePureLocalLoc, "@return"))
        }))
    )
  }
}
