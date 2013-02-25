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

object PreSemanticsBuiltinMath {
  def builtinMath(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                 fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1

    fun match {
      case "Math.abs" => {
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
        val rtn = v match {
          case NumBot|NaN|UInt|NUInt => Value(v)
          case NegInf|PosInf|Infinity =>  Value(Infinity)
          case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.abs(n)))
          case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.abs(n)))
          case _ =>  Value(NumTop)
       }
       ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
      }
      case "Math.acos" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
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
      case "Math.asin" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
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
      case "Math.atan" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
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
      case "Math.atan2" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v_2 = getArgValue(h, ctx, "1") /* Value */
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
      case "Math.ceil" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
        val rtn = v match {
          case NumBot|NaN|Infinity|PosInf|NegInf|UInt =>  Value(v)
          case UIntSingle(n) =>  Value(v)
          case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.ceil(n)))
          case _ =>  Value(NumTop)
        }
        ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
      case "Math.cos" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
        val rtn = v match {
          case NumBot =>  Value(NumBot)
          case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
          case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.cos(n)))
          case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.cos(n)))
          case _ =>  Value(NumTop)
        }
        ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
      case "Math.exp" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
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
      case "Math.floor" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
        val rtn = v match {
          case NumBot|NumTop|NaN|Infinity|PosInf|NegInf|UInt =>  Value(v)
          case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.floor(n)))
          case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.floor(n)))
          case _ =>  Value(NumTop)
        }
        ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
      case "Math.max" =>
        val n_arglen = getArgValue(h, ctx, "length")._1._4
        def n_arg = (i : Int) => PreHelper.toNumber(PreHelper.toPrimitive(getArgValue(h, ctx, i.toString)))
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
      case "Math.min" =>
        val n_arglen = getArgValue(h, ctx, "length")._1._4
        def n_arg = (i : Int) => PreHelper.toNumber(PreHelper.toPrimitive(getArgValue(h, ctx, i.toString)))
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
      case "Math.pow" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v_2 = getArgValue(h, ctx, "1") /* Value */
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
      case "Math.log" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
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
      case "Math.random" =>
        ((PreHelper.ReturnStore(h, PureLocalLoc, Value(NumTop)), ctx), (he, ctxe))
      case "Math.round" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
        val rtn = v match {
          case NumBot|NaN|Infinity|PosInf|NegInf|UInt|NUInt => Value(v)
          case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.round(n)))
          case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.round(n)))
          case _ =>  Value(NumTop)
        }
        ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
      case "Math.sin" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
        val rtn = v match {
          case NumBot => Value(v)
          case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
          case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.sin(n)))
          case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.sin(n)))
          case _ =>  Value(NumTop)
        }
        ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
      case "Math.sqrt" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
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
      case "Math.tan" =>
        val v_1 = getArgValue(h, ctx, "0") /* Value */
        val v = PreHelper.toNumber(PreHelper.toPrimitive(v_1))
        val rtn = v match {
          case NumBot => Value(v)
          case NaN|Infinity|PosInf|NegInf =>  Value(NaN)
          case UIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.tan(n)))
          case NUIntSingle(n) =>  Value(AbsNumber.alpha(scala.math.tan(n)))
          case _ =>  Value(NumTop)
        }
        ((PreHelper.ReturnStore(h, PureLocalLoc, rtn), ctx), (he, ctxe))
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

}
