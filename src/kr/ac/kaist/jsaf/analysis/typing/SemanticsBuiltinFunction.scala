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
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

import scala.math.{min,max,floor, abs}

object SemanticsBuiltinFunction {

  def builtinFunction(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address, addr3: Address, addr4: Address): ((Heap, Context),(Heap, Context)) = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

    fun match {
      case "Function.constructor" => {
        // update "length" property to this object.
        val h_1 = lset_this.foldLeft(HeapBot)((_h,l) =>
                         _h + Helper.PropStore(h, l, OtherStrSingle("length"), Value(NumTop)))
        ((h_1, ctx), (he, ctxe))
      }
      case "Function.prototype" => {
        ((Helper.ReturnStore(h, Value(UndefTop)), ctx), (he, ctxe))
      }
      case "Function.prototype.toString" => {
       val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Function")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
        ((Helper.ReturnStore(h, Value(StrTop)), ctx), (he + h_e, ctxe + ctx_e))
      }
      case "Function.prototype.apply" => {
        val l_r1 = addrToLoc(addr1, Recent)
        val l_r2 = addrToLoc(addr2, Recent)
        val l_r3 = addrToLoc(addr3, Recent)
        val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
        val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)
        val (h_3, ctx_3) = Helper.Oldify(h_2, ctx_2, addr3)
        val lset_this = h_3(SinglePureLocalLoc)("@this")._1._2._2

        // 1.
        val cond = lset_this.exists((l) => BoolFalse <= Helper.IsCallable(h_3,l))
        val es1 =
          if (cond) Set[Exception](TypeError)
          else Set[Exception]()
        val lset_f = lset_this.filter((l) => BoolTrue <= Helper.IsCallable(h_3,l))

        // 2. create empty Arguments object
        val v_arg = getArgValue(h_3, ctx_3, "1")
        val o_arg1 =
          if (v_arg._1._1 </ UndefBot || v_arg._1._2 </ NullBot)  Helper.NewArgObject(AbsNumber.alpha(0))
          else  ObjBot

        // 3.
        val v_arg1 = Value(PValue(UndefBot, NullBot, v_arg._1._3, v_arg._1._4, v_arg._1._5), v_arg._2)
        val (v_arg2, es2) =
          if (v_arg1._1 </ PValueBot)
            (Value(PValueBot, v_arg1._2), Set[Exception](TypeError))
          else
            (v_arg1, Set[Exception]())

        // 4. ~ 8. create Arguments object with argArray
        val o_arg2 =
          if (!v_arg2._2.isEmpty) {
            v_arg2._2.foldLeft(ObjBot)((_o, l) => {
              val n_arglen = Operator.ToUInt32(Helper.Proto(h_3, l, AbsString.alpha("length")))
              n_arglen match {
                case UIntSingle(n_len) =>
                  val o = Helper.NewArgObject(AbsNumber.alpha(n_len))
                  (0 until n_len.toInt).foldLeft(o)((_o, i) => {
                    val value = Helper.Proto(h_3, l, AbsString.alpha(i.toString))
                    val propv = PropValue(ObjectValue(value, BoolTrue, BoolTrue, BoolTrue))
                    _o.update(i.toString, propv)
                    })
                case NumBot => ObjBot
                case _ =>
                  val value = Helper.Proto(h_3, l, NumStr)
                  val propv = PropValue(ObjectValue(value, BoolTrue, BoolTrue, BoolTrue))
                  Helper.NewArgObject(n_arglen).update(NumStr, propv)
              }
            })
          }
          else
            ObjBot
        val o_arg3 = o_arg1 + o_arg2

        val v_arg3 = Value(l_r3)
        val h_4 = h_3.update(l_r3, o_arg3)

        // *  in our own semantics, this value should be object
        val v_this = getArgValue(h_4, ctx_3, "0")
        val lset_argthis = Helper.getThis(h_4, v_this)
        val v_this2 =  Value(PValue(UndefBot, NullBot, v_this._1._3, v_this._1._4, v_this._1._5), lset_argthis)
        val (callee_this, h_5, ctx_5, es3) = Helper.toObject(h_4, ctx_3, v_this2, addr4)

        val o_old = h_5(SinglePureLocalLoc)
        val cc_caller = cp._2
        val n_aftercall = cfg.getAftercallFromCall(cp._1)
        val cp_aftercall = (n_aftercall, cc_caller)
        lset_f.foreach((l_f) => {
          val o_f = h_5(l_f)
          o_f("@function")._1._3.foreach((fid) => {
            cc_caller.NewCallContext(cfg, fid, l_r2, callee_this._2).foreach((pair) => {
              val (cc_new, o_new) = pair
              val o_new2 = o_new.
                update(cfg.getArgumentsName(fid),
                       PropValue(ObjectValue(v_arg3, BoolTrue, BoolFalse, BoolFalse))).
                update("@scope", o_f("@scope")._1)
              sem.addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
              sem.addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_5, o_old)
              sem.addReturnEdge(((fid, LExitExc), cc_new), cp_aftercall, ctx_5, o_old)
            })
          })
        })

        val (h_e, ctx_e) = Helper.RaiseException(h_5, ctx_5, es1++es2++es3)
        val s_1 = (he + h_e, ctxe + ctx_e)

        val h_6 = v_arg3._2.foldLeft(HeapBot)((hh, l) => {
          hh + h_5.update(l, h_5(l).update("callee",
            PropValue(ObjectValue(Value(lset_f), BoolTrue, BoolFalse, BoolTrue))))
        })

        ((h_6, ctx_5), s_1)
      }
      case "Function.prototype.call" => {
        val l_r1 = addrToLoc(addr1, Recent)
        val l_r2 = addrToLoc(addr2, Recent)
        val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
        val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)
        val lset_this = h_2(SinglePureLocalLoc)("@this")._1._2._2

        // 1.
        val cond = lset_this.exists((l) => BoolFalse <= Helper.IsCallable(h_2,l))
        val es =
          if (cond) Set[Exception](TypeError)
          else Set[Exception]()
        val (h_e, ctx_e) = Helper.RaiseException(h_2, ctx_2, es)
        val lset_f = lset_this.filter((l) => BoolTrue <= Helper.IsCallable(h_2,l))


        // 2., 3. create Arguments object
        val len = Operator.bopMinus(getArgValue(h_2, ctx_2, "length"), Value(AbsNumber.alpha(1)))
        val np = len._1._4
        val o_arg = Helper.NewArgObject(np)
        val o_arg_1 =
          AbsNumber.concretize(np) match {
            case Some(n) => (0 until n.toInt).foldLeft(o_arg)((_o, i) =>
              _o.update(AbsString.alpha(i.toString), PropValue(ObjectValue(getArgValue(h_2, ctx_2, (i+1).toString), BoolTrue, BoolTrue, BoolTrue))))
            case None =>
              o_arg
          }
        val h_3 = h_2.update(l_r1, o_arg_1)
        val v_arg = Value(l_r1)


        val v_this = getArgValue(h_3, ctx_2, "0")
        val lset_argthis = Helper.getThis(h_3, v_this)
        val v_this2 =  Value(PValue(UndefBot, NullBot, v_this._1._3, v_this._1._4, v_this._1._5), lset_argthis)
        val (callee_this, h_4, ctx_4, es3) = Helper.toObject(h_3, ctx_2, v_this2, addr4)


        val o_old = h_4(SinglePureLocalLoc)
        val cc_caller = cp._2
        val n_aftercall = cfg.getAftercallFromCall(cp._1)
        val cp_aftercall = (n_aftercall, cc_caller)
        lset_f.foreach((l_f) => {
          val o_f = h_4(l_f)
          o_f("@function")._1._3.foreach((fid) => {
            cc_caller.NewCallContext(cfg, fid, l_r2, callee_this._2).foreach((pair) => {
              val (cc_new, o_new) = pair
              val o_new2 = o_new.
                update(cfg.getArgumentsName(fid),
                       PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))).
                update("@scope", o_f("@scope")._1)
              sem.addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
              sem.addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_4, o_old)
              sem.addReturnEdge(((fid, LExitExc), cc_new), cp_aftercall, ctx_4, o_old)
            })
          })
        })
        val h_5 = v_arg._2.foldLeft(HeapBot)((hh, l) => {
          hh + h_4.update(l, h_4(l).update("callee",
            PropValue(ObjectValue(Value(lset_f), BoolTrue, BoolFalse, BoolTrue))))
        })

        val s_1 = (he + h_e, ctxe + ctx_e)
        ((h_5, ctx_4), s_1)

      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
