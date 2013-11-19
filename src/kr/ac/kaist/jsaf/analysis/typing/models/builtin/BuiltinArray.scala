/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.builtin

import scala.math.{min,max,floor, abs}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.{AccessHelper=>AH}
import kr.ac.kaist.jsaf.analysis.typing.domain.NUIntSingle
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFuncCallback
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.AddressManager._

object BuiltinArray extends ModelData {

  val ConstLoc = newSystemLoc("ArrayConst", Recent)
  val ProtoLoc = newSystemLoc("ArrayProto", Recent)

  private val prop_const: List[(String, AbsProperty)] = List(
    ("@class",                   AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto",                   AbsConstValue(PropValue(ObjectValue(Value(FunctionProtoLoc), F, F, F)))),
    ("@extensible",              AbsConstValue(PropValue(T))),
    ("@scope",                   AbsConstValue(PropValue(Value(NullTop)))),
    ("@function",                AbsInternalFunc("Array")),
    ("@construct",               AbsInternalFunc("Array.constructor")),
    ("@hasinstance",             AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype",                AbsConstValue(PropValue(ObjectValue(Value(ProtoLoc), F, F, F)))),
    ("length",                   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1), F, F, F)))),
    ("isArray",                  AbsBuiltinFunc("Array.isArray", 1))
  )

  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
    ("constructor",          AbsConstValue(PropValue(ObjectValue(ProtoLoc, F, F, F)))),
    ("length",               AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0), F, F, F)))),
    ("toString",             AbsBuiltinFunc("Array.prototype.toString", 0)),
    ("toLocaleString",       AbsBuiltinFunc("Array.prototype.toLocaleString", 0)),
    ("concat",               AbsBuiltinFunc("Array.prototype.concat", 1)),
    ("join",                 AbsBuiltinFunc("Array.prototype.join", 1)),
    ("pop",                  AbsBuiltinFunc("Array.prototype.pop", 0)),
    ("push",                 AbsBuiltinFunc("Array.prototype.push", 1)),
    ("reverse",              AbsBuiltinFunc("Array.prototype.reverse", 0)),
    ("shift",                AbsBuiltinFunc("Array.prototype.shift", 0)),
    ("slice",                AbsBuiltinFunc("Array.prototype.slice", 2)),
    ("sort",                 AbsBuiltinFunc("Array.prototype.sort", 1)),
    ("splice",               AbsBuiltinFunc("Array.prototype.splice", 2)),
    ("unshift",              AbsBuiltinFunc("Array.prototype.unshift", 1)),
    ("indexOf",              AbsBuiltinFunc("Array.prototype.indexOf", 1)),
    ("lastIndexOf",          AbsBuiltinFunc("Array.prototype.lastIndexOf", 1)),
    ("every",                AbsBuiltinFunc("Array.prototype.every", 1)),
    ("some",                 AbsBuiltinFunc("Array.prototype.some", 1)),
    ("forEach",              AbsBuiltinFunc("Array.prototype.forEach", 1)),
    ("map",                  AbsBuiltinFunc("Array.prototype.map", 1)),
    ("filter",               AbsBuiltinFunc("Array.prototype.filter", 1)),
    ("reduce",               AbsBuiltinFuncCallback("Array.prototype.reduce", 1)),
    ("reduceRight",          AbsBuiltinFuncCallback("Array.prototype.reduce", 1))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (ConstLoc, prop_const), (ProtoLoc, prop_proto)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("Array" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val v_1 = getArgValue(h_1, ctx_1, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

          // case for "new Array(n)"
          val (h_arg_1, es1) =
            if(v_1 </ ValueBot) {
              val es = v_1._1._4 match {
                case UInt => ExceptionBot
                case UIntSingle(_) => ExceptionBot
                case NumBot => ExceptionBot
                case _ => Set[Exception](RangeError)
              }
              val v_notNum = Value(PValue(v_1._1._1,v_1._1._2,v_1._1._3,NumBot,v_1._1._5), v_1._2)
              // case for new Array("value")
              val o_notNum =
                if (v_notNum </ ValueBot) {
                  Helper.NewArrayObject(AbsNumber.alpha(1)).
                    update("0", PropValue(ObjectValue(v_notNum, BoolTrue, BoolTrue, BoolTrue)), AbsentBot)
                }
                else
                  ObjBot
              // case for new Array(len)
              val o_num =
                if (v_1._1._4 </ NumBot) {
                  Helper.NewArrayObject(Operator.ToUInt32(Value(v_1._1._4)))
                }
                else
                  ObjBot

              (h_1.update(l_r, o_notNum + o_num), es)
            }
            else {
              (HeapBot, ExceptionBot)
            }

          val (h_2, es2) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              (h_arg_1, es1)
            case UIntSingle(n) if n != 1 => {
              // case for "new Array([v_1[, v_2[, ...])
              val o = (0 until n.toInt).foldLeft(Helper.NewArrayObject(n_arglen))((_o, i) =>
                _o.update(i.toString, PropValue(ObjectValue(getArgValue(h_1, ctx_1, args, i.toString), BoolTrue, BoolTrue, BoolTrue))))

              (h_1.update(l_r, o), ExceptionBot)

            }
            case NumBot => (HeapBot, ExceptionBot)
            case _ => {
              val o = Helper.NewArrayObject(UInt).
                update(NumStr, PropValue(ObjectValue(getArgValueAbs(h_1, ctx_1, args, NumStr),BoolTrue,BoolTrue,BoolTrue)))
              val h_uint = h_1.update(l_r, o)
              (h_arg_1 + h_uint, es1)
            }
          }

          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es2)

          if (!(h_2 <= HeapBot))
            ((Helper.ReturnStore(h_2, Value(l_r)), ctx), (he + h_e, ctxe + ctx_e))
          else
            ((HeapBot, ContextBot), (he + h_e, ctxe + ctx_e))
        })),
      ("Array.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val v_1 = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          // case for "new Array(n)"
          val (h_arg_1, es1) =
            if(v_1 </ ValueBot) {
              val es = v_1._1._4 match {
                case UInt => ExceptionBot
                case UIntSingle(_) => ExceptionBot
                case NumBot => ExceptionBot
                case _ => Set[Exception](RangeError)
              }
              val v_notNum = Value(PValue(v_1._1._1,v_1._1._2,v_1._1._3,NumBot,v_1._1._5), v_1._2)
              // case for new Array("value")
              val o_notNum =
                if (v_notNum </ ValueBot) {
                  Helper.NewArrayObject(AbsNumber.alpha(1)).
                    update("0", PropValue(ObjectValue(v_notNum, BoolTrue, BoolTrue, BoolTrue)), AbsentBot)
                }
                else
                  ObjBot
              // case for new Array(len)
              val o_num =
                if (v_1._1._4 </ NumBot) {
                  Helper.NewArrayObject(Operator.ToUInt32(Value(v_1._1._4)))
                }
                else
                  ObjBot

              (lset_this.foldLeft(h)((_h,l) => _h.update(l, o_notNum + o_num)), es)
            }
            else {
              (HeapBot, ExceptionBot)
            }

          val (h_2, es2) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              (h_arg_1, es1)
            case UIntSingle(n) if n != 1 => {
              // case for "new Array([v_1[, v_2[, ...])
              val o = (0 until n.toInt).foldLeft(Helper.NewArrayObject(n_arglen))((_o, i) =>
                _o.update(i.toString, PropValue(ObjectValue(getArgValue(h, ctx, args, i.toString), BoolTrue, BoolTrue, BoolTrue))))
              (lset_this.foldLeft(h)((_h,l) => _h.update(l, o)), ExceptionBot)
            }
            case NumBot => (HeapBot, ExceptionBot)
            case _ => {
              val o = Helper.NewArrayObject(UInt).
                update(NumStr, PropValue(ObjectValue(getArgValueAbs(h, ctx, args, NumStr),BoolTrue,BoolTrue,BoolTrue)))
              val h_uint = lset_this.foldLeft(h)((_h,l) => _h.update(l, o))
              (h_arg_1 + h_uint, es1)
            }
          }

          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es2)

          if (!(h_2 <= HeapBot))
            ((Helper.ReturnStore(h_2, Value(lset_this)), ctx), (he + h_e, ctxe + ctx_e))
          else
            ((HeapBot, ContextBot), (he + h_e, ctxe + ctx_e))
        })),
      ("Array.isArray" -> (
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
      ("Array.prototype.toString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val s_sep = AbsString.alpha(",")
          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h, l, AbsString.alpha("length"))))
          val s = n_len match {
            case UIntSingle(n) if n == 0 => AbsString.alpha("")
            case UIntSingle(n) if n > 0 => {
              val v_f = lset_this.foldLeft(ValueBot)((_v, l) =>_v + Helper.Proto(h, l, AbsString.alpha("0")))
              val v_f2 = Value(PValue(UndefBot,NullBot,v_f._1._3,v_f._1._4,v_f._1._5), v_f._2)
              val s_first =
                if (v_f._1._1 </ UndefBot || v_f._1._2 </ NullBot)
                  AbsString.alpha("") + Helper.toString(Helper.toPrimitive(v_f2))
                else
                  Helper.toString(Helper.toPrimitive(v_f))
              (1 until n.toInt).foldLeft(s_first)((_s, i) =>{
                val v_i = lset_this.foldLeft(ValueBot)((_v, l) =>_v + Helper.Proto(h, l, AbsString.alpha(i.toString)))
                val v_i2 = Value(PValue(UndefBot,NullBot,v_i._1._3,v_i._1._4,v_i._1._5), v_i._2)
                val s_i =
                  if (v_i._1._1 </ UndefBot || v_i._1._2 </ NullBot)
                    AbsString.alpha("") + Helper.toString(Helper.toPrimitive(v_i2))
                  else
                    Helper.toString(Helper.toPrimitive(v_i))
                _s.concat(s_sep).concat(s_i)
              })
            }
            case NumBot => StrBot
            case _ => StrTop
          }
          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
        "Array.prototype.toLocaleString" -> (
          (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
            val v_this = h(SinglePureLocalLoc)("@this")._1._2

            // Get a new address
            val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
            val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
            if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
            val addr_env = set_addr.head
            val addr1 = cfg.getAPIAddress(addr_env, 0)
            val addr2 = cfg.getAPIAddress(addr_env, 1)

            // 1. Let array be the result of calling ToObject passing the this value as the argument.
            val (v_this2, h_1, ctx_1, es_1) = Helper.toObject(h, ctx, v_this, addr1)
            val lset_this = v_this2._2

            // 2. Let arrayLen be the result of calling the [[Get]] internal method of array with argument "length".
            val v_len = lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h_1, l, AbsString.alpha("length")))
            // 3. Let len be ToUint32(arrayLen).
            val n_len = Operator.ToUInt32(v_len)
            // 4. Let separator be the String value for the list-separator String appropriate for the host environment‘s current locale (this is derived in an implementation-defined way).
            val s_sep = AbsString.alpha(",")

            val (h_2, ctx_2, es_2, s) = n_len.getConcreteValue() match {
              case Some(n) if n == 0 => (h_1, ctx_1, ExceptionBot, AbsString.alpha(""))
              case Some(n) if n > 0 => {
                val v_f = lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h_1, l, AbsString.alpha("0")))
                val v_f2 = Value(PValue(UndefBot, NullBot, v_f._1._3, v_f._1._4, v_f._1._5), v_f._2)
                val s_first =
                  if (v_f._1._1 </ UndefBot || v_f._1._2 </ NullBot)
                    AbsString.alpha("") + Helper.toString(Helper.toPrimitive(v_f2))
                  else
                    Helper.toString(Helper.toPrimitive(v_f))

                // b. Let func be the result of calling the [[Get]] internal method of elementObj with argument "toLocaleString".
                val func = v_f._2.foldLeft(ValueBot)((S, l) => S + Helper.Proto(h_1, l, AbsString.alpha("toLocaleString")))
                val notfn = func._2.filter(l => BoolFalse <= Helper.IsCallable(h_1, l))
                // c. If IsCallable(func) is false, throw a TypeError exception.
                val es_1 =
                  if (!notfn.isEmpty || func._1 </ PValueBot) {
                    Set[Exception](TypeError)
                  } else {
                    ExceptionBot
                  }

                val (s, es_2) = (1 until n.toInt).foldLeft((s_first, es_1))((_s, i) => {
                  val v_i = lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h, l, AbsString.alpha(i.toString)))
                  val v_i2 = Value(PValue(UndefBot, NullBot, v_i._1._3, v_i._1._4, v_i._1._5), v_i._2)
                  val s_i =
                    if (v_i._1._1 </ UndefBot || v_i._1._2 </ NullBot)
                      AbsString.alpha("") + Helper.toString(Helper.toPrimitive(v_i2))
                    else
                      Helper.toString(Helper.toPrimitive(v_i))
                  // ii. Let func be the result of calling the [[Get]] internal method of elementObj with argument "toLocaleString".
                  val func = v_i._2.foldLeft(ValueBot)((S, l) => S + Helper.Proto(h_1, l, AbsString.alpha("toLocaleString")))
                  val notfn = func._2.filter(l => BoolFalse <= Helper.IsCallable(h_1, l))
                  // iii. If IsCallable(func) is false, throw a TypeError exception.
                  val es_i =
                    if (!notfn.isEmpty || func._1 </ PValueBot) {
                      Set[Exception](TypeError)
                    } else {
                      ExceptionBot
                    }

                  (_s._1.concat(s_sep).concat(s_i), _s._2 ++ es_i)
                })

                (h_1, ctx_1, es_2, s)
              }
              case None if n_len <= NumBot => (HeapBot, ContextBot, ExceptionBot, StrBot)
              case _ => {
                // 5. If len is zero, return the empty String.
                // 6. Let firstElement be the result of calling the [[Get]] internal method of array with argument "0".
                val elements_1 =
                  if (BoolTrue <= Operator.bopLessEq(Value(AbsNumber.alpha(1)), Value(n_len))._1._3) {
                    lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h_1, l, AbsString.NumTop))
                  } else {
                    ValueBot
                  }
                // a. Let elementObj be ToObject(firstElement).
                val (elements_2, h_2, ctx_2, es_2) =
                  if (elements_1 </ ValueBot) {
                    Helper.toObject(h_1, ctx_1, elements_1, addr2)
                  } else {
                    (ValueBot, h_1, ctx_1, ExceptionBot)
                  }

                // ii. Let func be the result of calling the [[Get]] internal method of elementObj with argument "toLocaleString".
                val func = elements_2._2.foldLeft(ValueBot)((S, l) => S + Helper.Proto(h_2, l, AbsString.alpha("toLocaleString")))

                // iii. If IsCallable(func) is false, throw a TypeError exception.
                val notfn = func._2.filter(l => BoolFalse <= Helper.IsCallable(h_2, l))
                val es_3 =
                  if (!notfn.isEmpty) {
                    Set[Exception](TypeError)
                  } else {
                    ExceptionBot
                  }

                val es = es_2 ++ es_3

                (h_2, ctx_2, es, StrTop)
              }
            }

            val (h_3, ctx_3) =
              if (s </ StrBot) {
                (Helper.ReturnStore(h_2, Value(s)), ctx_2)
              } else {
                (HeapBot, ContextBot)
              }
            val es = es_1 ++ es_2
            val (h_e, ctx_e) = Helper.RaiseException(h_3, ctx_3, es)

            ((h_3, ctx_3), (he + h_e, ctxe + ctx_e))
          }),
      ("Array.prototype.concat" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val lset_this = h_1(SinglePureLocalLoc)("@this")._1._2._2

          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

          val o = n_arglen match {
            case NumBot => ObjBot
            case UIntSingle(n_arg) => {
              val elem_list = (0 until n_arg.toInt).foldLeft[List[Value]](List(Value(lset_this)))((list, i) =>
                list :+ getArgValue(h_1, ctx_1, args, i.toString))
              val obj = Helper.NewArrayObject(AbsNumber.alpha(0))
              val index = AbsNumber.alpha(0)
              val (obj_1, len) = elem_list.foldLeft((obj, index))((oi, elem) => {
                val lset_array = elem._2.filter((l) => AbsString.alpha("Array") <= h(l)("@class")._1._2._1._5)
                val lset_narray = elem._2.filter((l) => AbsString.alpha("Array") != h(l)("@class")._1._2._1._5)
                val v_narray = Value(elem._1, lset_narray)
                val o = oi._1
                val index = oi._2
                val (o_1, n_index_1) =
                  if (!lset_array.isEmpty) {
                    lset_array.foldLeft[(Obj,AbsNumber)]((ObjBot, NumBot))((_oi, l) => {
                      val n_len = Operator.ToUInt32(Helper.Proto(h_1, l, AbsString.alpha("length")))
                      val __o = n_len match {
                        case UIntSingle(n) => {
                          (0 until n.toInt).foldLeft(o)((o_new, i)=>
                            o_new.update(Helper.toString(Operator.bopPlus(Value(index), Value(AbsNumber.alpha(i)))._1),
                              PropValue(ObjectValue(Helper.Proto(h_1, l, AbsString.alpha(i.toString)),BoolTrue,BoolTrue,BoolTrue))))
                        }
                        case NumBot => ObjBot
                        case _ =>
                          val v_all = Helper.Proto(h_1, l, NumStr)
                          o.update(NumStr, PropValue(ObjectValue(v_all,BoolTrue,BoolTrue,BoolTrue)))
                      }
                      val __i = Operator.bopPlus(Value(index), Value(n_len))._1._4
                      (_oi._1 + __o , _oi._2 + __i)
                    })
                  }
                  else
                    (ObjBot, NumBot)
                val (o_2, n_index_2) =
                  if (v_narray </ ValueBot) {
                    val _o = o.update(Helper.toString(PValue(index)), PropValue(ObjectValue(elem, BoolTrue, BoolTrue, BoolTrue)))
                    val _i = Operator.bopPlus(Value(index), Value(AbsNumber.alpha(1)))._1._4
                    (_o, _i)
                  }
                  else
                    (ObjBot, NumBot)
                (o_1 + o_2, n_index_1 + n_index_2)})
              obj_1.update("length", PropValue(ObjectValue(Value(len), BoolTrue, BoolFalse, BoolFalse)))
            }
            case _ => {
              val v_all = Value(lset_this) + getArgValueAbs(h_1, ctx_1, args, NumStr)
              val lset_array = v_all._2.filter((l) => AbsString.alpha("Array") <= h(l)("@class")._1._2._1._5)
              val v_array = lset_array.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h_1, l, NumStr))
              Helper.NewArrayObject(UInt).update(NumStr, PropValue(ObjectValue(v_all + v_array, BoolTrue,BoolTrue,BoolTrue)))
            }
          }
          if (o </ ObjBot){
            val h_2 = h_1.update(l_r, o)
            ((Helper.ReturnStore(h_2, Value(l_r)), ctx), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.join" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val v_sep = getArgValue(h, ctx, args, "0")
          val v_sep2 = Value(PValue(UndefBot,v_sep._1._2,v_sep._1._3,v_sep._1._4,v_sep._1._5), v_sep._2)
          val s_sep =
            if (v_sep._1._1 </ UndefBot)
              AbsString.alpha(",") + Helper.toString(Helper.toPrimitive(v_sep2))
            else
              Helper.toString(Helper.toPrimitive(v_sep))

          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) =>
            _v + Helper.Proto(h, l, AbsString.alpha("length"))))

          val s = n_len match {
            case UIntSingle(n) if n == 0 => AbsString.alpha("")
            case UIntSingle(n) if n > 0 => {
              val v_f = lset_this.foldLeft(ValueBot)((_v, l) =>_v + Helper.Proto(h, l, AbsString.alpha("0")))
              val v_f2 = Value(PValue(UndefBot,NullBot,v_f._1._3,v_f._1._4,v_f._1._5), v_f._2)
              val s_first =
                if (v_f._1._1 </ UndefBot || v_f._1._2 </ NullBot)
                  AbsString.alpha("") + Helper.toString(Helper.toPrimitive(v_f2))
                else
                  Helper.toString(Helper.toPrimitive(v_f))
              (1 until n.toInt).foldLeft(s_first)((_s, i) =>{
                val v_i = lset_this.foldLeft(ValueBot)((_v, l) =>_v + Helper.Proto(h, l, AbsString.alpha(i.toString)))
                val v_i2 = Value(PValue(UndefBot,NullBot,v_i._1._3,v_i._1._4,v_i._1._5), v_i._2)
                val s_i =
                  if (v_i._1._1 </ UndefBot || v_i._1._2 </ NullBot)
                    AbsString.alpha("") + Helper.toString(Helper.toPrimitive(v_i2))
                  else
                    Helper.toString(Helper.toPrimitive(v_i))
                _s.concat(s_sep).concat(s_i)
              })
            }
            case _ => StrTop
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.pop" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) =>
            _v + Helper.Proto(h, l, AbsString.alpha("length"))))

          val (h_1, v) = lset_this.foldLeft((h, ValueBot))((hv, l) => {
            if (!(hv._1 <= HeapBot)) {
              val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
              val (_h, _v) = n_len match {
                case UIntSingle(n) if n == 0 => {
                  val __h = Helper.PropStore(hv._1, l, AbsString.alpha("length"), Value(AbsNumber.alpha(0)))
                  (__h, Value(UndefTop))
                }
                case UIntSingle(n) if n > 0 => {
                  val __v = Helper.Proto(hv._1, l,  AbsString.alpha((n-1).toInt.toString))
                  val __h = Helper.Delete(hv._1, l, AbsString.alpha((n-1).toInt.toString))._1
                  (Helper.PropStore(__h, l, AbsString.alpha("length"), Value(AbsNumber.alpha((n-1)))), __v)
                }
                case NumBot =>
                  (HeapBot, ValueBot)
                case _ => {
                  val __v = Helper.Proto(hv._1, l, NumStr)
                  val __h = Helper.Delete(hv._1, l, NumStr)._1
                  (Helper.PropStore(__h, l, AbsString.alpha("length"), Value(UInt)), __v)
                }
              }
              (_h, hv._2 + _v)
            }
            else {
              (HeapBot, ValueBot)
            }
          })
          if (v </ ValueBot)
            ((Helper.ReturnStore(h_1, v), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.push" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val (h_1, v) = n_arglen match {
            case NumBot => (HeapBot, ValueBot)
            case UIntSingle(n_arg) => {
              lset_this.foldLeft((h, ValueBot))((hv, l) => {
                if (!(hv._1 <= HeapBot)) {
                  val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                  n_len match {
                    case NumBot => (HeapBot, ValueBot)
                    case UIntSingle(n) => {
                      val _h = (0 until n_arg.toInt).foldLeft(hv._1)((__h, i) =>
                        Helper.PropStore(__h, l, AbsString.alpha((i+n).toInt.toString), getArgValue(h, ctx, args, (i.toString))))
                      val _v = Value(AbsNumber.alpha(n_arg+n))
                      val _h1 = Helper.PropStore(_h, l, AbsString.alpha("length"), _v)
                      (_h1, hv._2 + _v)
                    }
                    case _ => {
                      val v_argall = getArgValueAbs(h, ctx, args, NumStr)
                      (Helper.PropStore(hv._1, l, NumStr, v_argall), Value(UInt))
                    }
                  }
                }
                else {
                  (HeapBot, ValueBot)
                }
              })
            }
            case _ => {
              val v_argall = getArgValueAbs(h, ctx, args, NumStr)
              (lset_this.foldLeft(h)((_h, l) => Helper.PropStore(_h, l, NumStr, v_argall)), Value(UInt))
            }
          }
          if (v </ ValueBot)
            ((Helper.ReturnStore(h_1, v), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.reverse" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val h_1 = lset_this.foldLeft(h)((_hh, l) => {
            val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            n_len match {
              case UIntSingle(n) => {
                (0 until floor(n/2).toInt).foldLeft(_hh)((_h, i) =>{
                  val s_low = AbsString.alpha(i.toString)
                  val s_up = AbsString.alpha((n-i-1).toInt.toString)
                  val v_low = Helper.Proto(_h, l, s_low)
                  val v_up = Helper.Proto(_h, l, s_up)
                  val b_low = Helper.HasProperty(_h, l, s_low)
                  val b_up = Helper.HasProperty(_h, l, s_up)
                  val _h1 =
                    if (BoolTrue <= b_low && BoolTrue <= b_up) {
                      val __h1 = Helper.PropStore(_h, l, s_low, v_up)
                      Helper.PropStore(__h1, l, s_up, v_low)
                    }
                    else  {
                      HeapBot
                    }
                  val _h2 =
                    if (BoolFalse <= b_low && BoolTrue <= b_up) {
                      val __h1 = Helper.PropStore(_h, l, s_low, v_up)
                      Helper.Delete(__h1, l, s_up)._1
                    }
                    else  {
                      HeapBot
                    }
                  val _h3 =
                    if (BoolTrue <= b_low && BoolFalse <= b_up) {
                      val __h1 = Helper.PropStore(_h, l, s_up,  v_low)
                      Helper.Delete(__h1, l, s_low)._1
                    }
                    else  {
                      HeapBot
                    }
                  val _h4 =
                    if (BoolFalse <= b_low && BoolFalse <= b_up)  _h
                    else  {
                      HeapBot
                    }
                  _h1 + _h2 + _h3 + _h4
                })
              }
              case NumBot => HeapBot
              case _ =>
                val _h1 = Helper.PropStore(_hh, l, NumStr, Helper.Proto(_hh, l, NumStr))
                val _h2 = Helper.Delete(_h1, l, NumStr)._1
                (_h1 + _h2)
            }
          })
          if (!(h_1 <= HeapBot))
            ((Helper.ReturnStore(h_1, Value(lset_this)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.shift" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val (h_1, v) = lset_this.foldLeft((h,ValueBot))((_hv, l) => {
            val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            val _h = _hv._1
            val _v = _hv._2
            n_len match {
              case UIntSingle(n) => {
                if (n == 0) {
                  (Helper.PropStore(_h, l, AbsString.alpha("length"), Value(AbsNumber.alpha(0))), _v + Value(UndefTop))
                }
                else {
                  val v_first = Helper.Proto(_h, l, AbsString.alpha("0"))
                  val _h1 = (1 until n.toInt).foldLeft(_h)((__h, i) => {
                    val s_from = AbsString.alpha(i.toString)
                    val s_to = AbsString.alpha((i-1).toString)
                    val b = Helper.HasProperty(__h, l, s_from)
                    val __h1 =
                      if (BoolTrue <= b)  Helper.PropStore(__h, l, s_to, Helper.Proto(__h, l, s_from))
                      else HeapBot
                    val __h2 =
                      if (BoolFalse <= b)  Helper.Delete(__h, l, s_to)._1
                      else HeapBot
                    __h1 + __h2
                  })
                  val _h2 = Helper.Delete(_h1, l, AbsString.alpha((n-1).toInt.toString))._1
                  val _h3 = Helper.PropStore(_h2, l, AbsString.alpha("length"), Value(AbsNumber.alpha(n-1)))
                  (_h3, _v + v_first)
                }
              }
              case NumBot => (HeapBot, ValueBot)
              case _ => {
                val _v = Helper.Proto(_h, l, NumStr)
                val _h1 = Helper.Delete(_h, l, NumStr)._1
                val _h2 = Helper.PropStore(_h1, l, AbsString.alpha("length"), Value(UInt))
                (_h2, _v)
              }
            }
          })
          if (v </ ValueBot)
            ((Helper.ReturnStore(h_1, v), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.slice" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val lset_this = h_1(SinglePureLocalLoc)("@this")._1._2._2
          val n_start = Operator.ToInteger(getArgValue(h_1, ctx_1, args, "0"))
          val v_end = getArgValue(h, ctx, args, "1")
          //val n_end = Operator.ToInteger(getArgValue(h_1, ctx_1, "1"))

          val o = (AbsNumber.concretize(n_start)) match {
            case (Some(start)) =>
              lset_this.foldLeft(ObjBot)((_o, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h_1, l, AbsString.alpha("length")))
                val n_end =
                  if (v_end._1._1 </ UndefBot)  n_len + v_end._1._4
                  else Operator.ToInteger(v_end)
                _o + (AbsNumber.concretize(n_end) match {
                  case Some(end) =>
                    n_len match {
                      case UIntSingle(n) => {
                        val from =
                          if (start < 0) max(n + start, 0).toInt
                          else min(start, n).toInt
                        val to =
                          if (end < 0) max(n + end, 0).toInt
                          else min(end, n).toInt
                        val span = max(to-from, 0)
                        val o_new = Helper.NewArrayObject(AbsNumber.alpha(span))
                        (0 until span).foldLeft(o_new)((__o, i) => {
                          val b = Helper.HasProperty(h_1, l, AbsString.alpha(i.toString))
                          val _o1 =
                            if (BoolTrue <= b)
                              __o.update(AbsString.alpha(i.toString),
                                PropValue(ObjectValue(Helper.Proto(h_1, l, AbsString.alpha((from+i).toString)),BoolTrue,BoolTrue,BoolTrue)))
                            else ObjBot
                          val _o2 =
                            if (BoolFalse <= b) __o
                            else ObjBot
                          _o1 + _o2 })
                      }
                      case NumBot => ObjBot
                      case _ =>
                        val o_new = Helper.NewArrayObject(UInt)
                        o_new.update(NumStr, PropValue(ObjectValue(Helper.Proto(h_1, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                    }
                  case None =>
                    if (n_end <= NumBot)
                      ObjBot
                    else
                      lset_this.foldLeft(ObjBot)((_o, l) => {
                        val n_len = Operator.ToUInt32(Helper.Proto(h_1, l, AbsString.alpha("length")))
                        n_len match {
                          case NumBot => ObjBot
                          case _ =>
                            val o_new = Helper.NewArrayObject(UInt)
                            o_new.update(NumStr, PropValue(ObjectValue(Helper.Proto(h_1, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                        } })
                })
              })
            case _ =>
              if (n_start <= NumBot)
                ObjBot
              else {
                lset_this.foldLeft(ObjBot)((_o, l) => {
                  val n_len = Operator.ToUInt32(Helper.Proto(h_1, l, AbsString.alpha("length")))
                  n_len match {
                    case NumBot => ObjBot
                    case _ =>
                      val o_new = Helper.NewArrayObject(UInt)
                      o_new.update(NumStr, PropValue(ObjectValue(Helper.Proto(h_1, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                  }
                })
              }
          }
          if (o </ ObjBot) {
            val h_2 = h_1.update(l_r, o)
            ((Helper.ReturnStore(h_2, Value(l_r)), ctx_1), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.splice" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val lset_this = h_1(SinglePureLocalLoc)("@this")._1._2._2

          val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))
          val n_start = Operator.ToInteger(getArgValue(h_1, ctx_1, args, "0"))
          val n_count = Operator.ToInteger(getArgValue(h_1, ctx_1, args, "1"))
          val (h_2, o) = (AbsNumber.concretize(n_start), AbsNumber.concretize(n_count)) match {
            case (Some(start), Some(count)) =>
              lset_this.foldLeft((HeapBot, ObjBot))((_ho, l) => {
                val _h = _ho._1
                val _o = _ho._2
                val n_len = Operator.ToUInt32(Helper.Proto(h_1, l, AbsString.alpha("length")))
                n_len match {
                  case UIntSingle(n_len) => {
                    val from =
                      if (start < 0) max(n_len + start, 0).toInt
                      else min(start, n_len).toInt
                    val delCount = min(max(count, 0), n_len - start).toInt
                    val o_new = Helper.NewArrayObject(AbsNumber.alpha(delCount))
                    val o_1 = (0 until delCount).foldLeft(o_new)((__o, i) => {
                      val b = Helper.HasProperty(h_1, l, AbsString.alpha(i.toString))
                      val _o1 =
                        if (BoolTrue <= b)
                          __o.update(AbsString.alpha(i.toString),
                            PropValue(ObjectValue(Helper.Proto(h_1, l, AbsString.alpha((from+i).toString)),BoolTrue,BoolTrue,BoolTrue)))
                        else ObjBot
                      val _o2 =
                        if (BoolFalse <= b) __o
                        else ObjBot
                      _o1 + _o2
                    })
                    val _h1 = n_arglen match {
                      case UIntSingle(n_arglen) => {
                        val add_count = n_arglen.toInt - 2
                        val move_start = start + count
                        if (add_count < count) {
                          val __h1 = (move_start.toInt until n_len.toInt).foldLeft(h_1)((__h, i) => {
                            val s_from = AbsString.alpha(i.toString)
                            val s_to = AbsString.alpha((i - count+add_count).toInt.toString)
                            val v = Helper.Proto(__h, l, s_from)
                            val b = Helper.HasProperty(__h, l, s_from)
                            val __h1 =
                              if (BoolTrue <= b) Helper.PropStore(__h, l, s_to, v)
                              else HeapBot
                            val __h2 =
                              if (BoolFalse <= b) Helper.Delete(__h, l, s_to)._1
                              else HeapBot
                            __h1 + __h2
                          })
                          val __h2 = (0 until add_count).foldLeft(__h1)((__h, i) =>
                            Helper.PropStore(__h, l, AbsString.alpha((start + i).toInt.toString), getArgValue(__h, ctx_1, args, (i+2).toString)))
                          val new_length = n_len + add_count - count
                          val __h3 = (new_length.toInt until n_len.toInt).foldLeft(__h2)((__h, i) =>
                            Helper.Delete(__h, l, AbsString.alpha(i.toString))._1)
                          Helper.PropStore(__h3, l,  AbsString.alpha("length"), Value(AbsNumber.alpha(new_length)))
                        }
                        else {
                          val __h1 = (0 until (n_len-move_start).toInt).foldLeft(h_1)((__h, i) => {
                            val s_from = AbsString.alpha((n_len -1 - i).toInt.toString)
                            val s_to = AbsString.alpha((n_len -1 -i + add_count - count).toInt.toString)
                            val v = Helper.Proto(__h, l, s_from)
                            val b = Helper.HasProperty(__h, l, s_from)
                            val __h1 =
                              if (BoolTrue <= b) Helper.PropStore(__h, l, s_to, v)
                              else HeapBot
                            val __h2 =
                              if (BoolFalse <= b) Helper.Delete(__h, l, s_to)._1
                              else HeapBot
                            __h1 + __h2
                          })
                          val __h2 = (0 until add_count).foldLeft(__h1)((__h, i) =>
                            Helper.PropStore(__h, l, AbsString.alpha((start + i).toInt.toString), getArgValue(__h, ctx_1, args, (i+2).toString)))
                          val new_length = n_len + add_count - count
                          Helper.PropStore(__h2, l,  AbsString.alpha("length"), Value(AbsNumber.alpha(new_length)))
                        }
                      }
                      case NumBot => HeapBot
                      case _ =>
                        val _h1 = Helper.PropStore(h_1, l, NumStr, getArgValueAbs(h_1, ctx_1, args, NumStr))
                        Helper.Delete(_h1, l, NumStr)._1
                    }
                    (_h + _h1, _o + o_1)
                  }
                  case NumBot => (HeapBot, ObjBot)
                  case _ =>
                    val o_new = Helper.NewArrayObject(UInt)
                    val o_1 = o_new.update(NumStr, PropValue(ObjectValue(Helper.Proto(h_1, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                    val _h1 = Helper.PropStore(h_1, l, NumStr, getArgValueAbs(h_1, ctx_1, args, NumStr))
                    val _h2 = Helper.Delete(_h1, l, NumStr)._1
                    (_h + _h2, _o + o_1)
                }
              })
            case _ =>
              if (n_start <= NumBot || n_count <= NumBot) {
                (HeapBot, ObjBot)
              }
              else {
                lset_this.foldLeft((HeapBot, ObjBot))((_ho, l) => {
                  val n_len = Operator.ToUInt32(Helper.Proto(h_1, l, AbsString.alpha("length")))
                  val _h = _ho._1
                  val _o = _ho._2
                  n_len match {
                    case NumBot => (HeapBot, ObjBot)
                    case _ =>
                      val o_new = Helper.NewArrayObject(UInt)
                      val _o1 = o_new.update(NumStr, PropValue(ObjectValue(Helper.Proto(h_1, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                      val _h1 = Helper.Delete(h_1, l, NumStr)._1
                      val _h2 = Helper.PropStore(_h1, l, NumStr, getArgValueAbs(_h1, ctx_1, args, NumStr))
                      val _h3 = Helper.PropStore(_h2, l, AbsString.alpha("length"), Value(UInt))
                      (_h + _h3, _o + _o1)
                  }
                })
              }
          }
          if (o </ ObjBot) {
            val h_3 = h_2.update(l_r, o)
            ((Helper.ReturnStore(h_3, Value(l_r)), ctx_1), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.unshift" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val (h_1, n) = n_arglen match {
            case UIntSingle(n_arglen) => {
              lset_this.foldLeft[(Heap, AbsNumber)]((h, NumBot))((_hv, l) => {
                val _h = _hv._1
                val _v = _hv._2
                if (_h <= HeapBot)
                  (HeapBot, NumBot)
                else {
                  val n_len = Operator.ToUInt32(Helper.Proto(_h, l, AbsString.alpha("length")))
                  n_len match {
                    case UIntSingle(k) => {
                      val _h1 = (0 until k.toInt).foldLeft(_h)((__h, i) => {
                        val s_from = AbsString.alpha((k - 1 - i).toInt.toString)
                        val s_to = AbsString.alpha((k -1 -i +n_arglen).toInt.toString)
                        val b = Helper.HasProperty(__h, l, s_from)
                        val __h1 =
                          if (BoolTrue <= b) Helper.PropStore(__h, l, s_to, Helper.Proto(__h, l, s_from))
                          else HeapBot
                        val __h2 =
                          if (BoolFalse <= b) Helper.Delete(__h, l, s_to)._1
                          else HeapBot
                        (__h1 + __h2)
                      })
                      val _h2 = (0 until n_arglen.toInt).foldLeft(_h1)((__h, i) => {
                        val v_i = getArgValue(h, ctx, args, i.toString)
                        Helper.PropStore(__h, l, AbsString.alpha(i.toString), v_i)
                      })
                      val _h3 = Helper.PropStore(_h2, l, AbsString.alpha("length"), Value(AbsNumber.alpha(n_arglen + k)))
                      (_h3, _v + AbsNumber.alpha(n_arglen + k))
                    }
                    case NumBot =>
                      (HeapBot, NumBot)
                    case _ => {
                      val _h1 = Helper.PropStore(_h, l, NumStr, Helper.Proto(_h, l, NumStr))
                      val _h2 = Helper.Delete(_h, l, NumStr)._1
                      (_h1 + _h2, _v + UInt)
                    }
                  }
                }
              })
            }
            case NumBot => (HeapBot, NumBot)
            case _ => {
              lset_this.foldLeft[(Heap, AbsNumber)]((h, NumBot))((_hv, l) => {
                val _h = _hv._1
                val _v = _hv._2
                if (_h <= HeapBot)
                  (HeapBot, NumBot)
                else {
                  val n_len = Operator.ToUInt32(Helper.Proto(_h, l, AbsString.alpha("length")))
                  n_len match {
                    case NumBot =>
                      (HeapBot, NumBot)
                    case _ => {
                      val _h1 = Helper.PropStore(_h, l, NumStr, Helper.Proto(_h, l, NumStr))
                      val _h2 = Helper.Delete(_h, l, NumStr)._1
                      (_h1 + _h2, _v + UInt)
                    }
                  }
                }
              })
            }
          }
          if (n </ NumBot)
            ((Helper.ReturnStore(h_1, Value(n)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.indexOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val n_index = n_arglen match {
            case UIntSingle(n) => {
              val v_search = getArgValue(h, ctx, args, "0")
              lset_this.foldLeft[AbsNumber](NumBot)((_n, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                _n + (n_len match {
                  case UIntSingle(n_len) => {
                    if (n_len == 0)
                      AbsNumber.alpha(-1)
                    else {
                      val start =
                        if (n > 1) Operator.ToInteger(getArgValue(h, ctx, args, "1"))
                        else AbsNumber.alpha(0)
                      start match {
                        case UIntSingle(n_start) => {
                          if (n_start >= n_len)
                            AbsNumber.alpha(-1)
                          else {
                            val k =
                              if (n_start < 0) (n_len - abs(n_start))
                              else n_start
                            val (index, flag)= (k.toInt until n_len.toInt).foldLeft[(AbsNumber, Boolean)]((NumBot, false))((__nb, i) => {
                              if (__nb._2)
                                __nb
                              else {
                                val __n = __nb._1
                                Operator.bopSEq(v_search, Helper.Proto(h, l, AbsString.alpha(i.toString)))._1._3 match {
                                  case BoolTop => (__n + AbsNumber.alpha(i), false)
                                  case BoolBot => (NumBot, true)
                                  case BoolTrue => (AbsNumber.alpha(i), true)
                                  case BoolFalse => (__n, false)
                                }}})
                            if (flag)
                              index
                            else
                              index + AbsNumber.alpha(-1)
                          }
                        }
                        case NUIntSingle(n_start) => {
                          if (n_start >= n_len)
                            AbsNumber.alpha(-1)
                          else {
                            val k =
                              if (n_start < 0) (n_len - abs(n_start))
                              else n_start
                            val (index, flag)= (k.toInt until n_len.toInt).foldLeft[(AbsNumber, Boolean)]((NumBot, false))((__nb, i) => {
                              if (__nb._2)
                                __nb
                              else {
                                val __n = __nb._1
                                Operator.bopSEq(v_search, Helper.Proto(h, l, AbsString.alpha(i.toString)))._1._3 match {
                                  case BoolTop => (__n + AbsNumber.alpha(i), false)
                                  case BoolBot => (NumBot, true)
                                  case BoolTrue => (AbsNumber.alpha(i), true)
                                  case BoolFalse => (__n, false)
                                }}})
                            if (flag)
                              index
                            else
                              index + AbsNumber.alpha(-1)
                          }
                        }
                        case NumBot => _n
                        case _ => NumTop
                      }
                    }
                  }
                  case NumBot => _n
                  case _ => NumTop
                })
              })
            }
            case NumBot => NumBot
            case _ => NumTop
          }
          if (n_index </ NumBot)
            ((Helper.ReturnStore(h, Value(n_index)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("Array.prototype.lastIndexOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val n_index = n_arglen match {
            case UIntSingle(n) => {
              val v_search = getArgValue(h, ctx, args, "0")
              lset_this.foldLeft[AbsNumber](NumBot)((_n, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                _n + (n_len match {
                  case UIntSingle(n_len) => {
                    if (n_len == 0)
                      AbsNumber.alpha(-1)
                    else {
                      val start =
                        if (n > 1) Operator.ToInteger(getArgValue(h, ctx, args, "1"))
                        else AbsNumber.alpha(n_len - 1)
                      start match {
                        case UIntSingle(n_start) => {
                          val k =
                            if (n_start >= 0) min(n_start, n_len-1)
                            else (n_len - abs(n_start))
                          val (index, flag)= (0 until k.toInt).foldLeft[(AbsNumber, Boolean)]((NumBot, false))((__nb, i) => {
                            if (__nb._2)
                              __nb
                            else {
                              val __n = __nb._1
                              val i_back = (k - i).toInt
                              Operator.bopSEq(v_search, Helper.Proto(h, l, AbsString.alpha(i_back.toString)))._1._3 match {
                                case BoolTop => (__n + AbsNumber.alpha(i_back), false)
                                case BoolBot => (NumBot, true)
                                case BoolTrue => (AbsNumber.alpha(i_back), true)
                                case BoolFalse => (__n, false)
                              }}})
                          if (flag)
                            index
                          else
                            index + AbsNumber.alpha(-1)
                        }
                        case NUIntSingle(n_start) => {
                          val k =
                            if (n_start >= 0) min(n_start, n_len-1)
                            else (n_len - abs(n_start))
                          val (index, flag)= (0 until k.toInt).foldLeft[(AbsNumber, Boolean)]((NumBot, false))((__nb, i) => {
                            if (__nb._2)
                              __nb
                            else {
                              val __n = __nb._1
                              val i_back = (k - i).toInt
                              Operator.bopSEq(v_search, Helper.Proto(h, l, AbsString.alpha(i_back.toString)))._1._3 match {
                                case BoolTop => (__n + AbsNumber.alpha(i_back), false)
                                case BoolBot => (NumBot, true)
                                case BoolTrue => (AbsNumber.alpha(i_back), true)
                                case BoolFalse => (__n, false)
                              }}})
                          if (flag)
                            index
                          else
                            index + AbsNumber.alpha(-1)
                        }
                        case NumBot => _n
                        case _ => NumTop
                      }
                    }
                  }
                  case NumBot => _n
                  case _ => NumTop
                })
              })
            }
            case NumBot => NumBot
            case _ => NumTop
          }
          if (n_index </ NumBot)
            ((Helper.ReturnStore(h, Value(n_index)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      "Array.prototype.reduce.init" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_this = h(SinglePureLocalLoc)("@this")._1._2
          val v_callbackfn = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val v_initial =
            n_arglen.getConcreteValue() match {
              case Some(d) if d >= 2 => Some(getArgValue(h, ctx, args, "1"))
              case None if AbsNumber.alpha(2) <= n_arglen => Some(getArgValue(h, ctx, args, "1"))
              case None => None
              case _ => None
            }
          val bInitValue = v_initial.isDefined

          // Get a new address
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)

          // 1. Let O be the result of calling ToObject passing the this value as the argument.
          val (v_this2, h_1, ctx_1, es_1) = Helper.toObject(h, ctx, v_this, addr1)
          val lset_this = v_this2._2

          // 2. Let lenValue be the result of calling the [[Get]] internal method of O with the argument "length".
          val v_len = lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h_1, l, AbsString.alpha("length")))
          // 3. Let len be ToUint32(lenValue).
          val n_len = Operator.ToUInt32(v_len)

          // 4. If IsCallable(callbackfn) is false, throw a TypeError exception.
          val es_2 =
            if (BoolFalse <= Helper.IsCallable(h_1, v_callbackfn))
              Set[Exception](TypeError)
            else
              ExceptionBot

          // 5. If len is 0 and initialValue is not present, throw a TypeError exception.
          val es_3 =
            if ((AbsNumber.alpha(0) <= n_len) && !bInitValue)
              Set[Exception](TypeError)
            else
              ExceptionBot

          // If initialValue is not present
          // c. If kPresent is false, throw a TypeError exception.
          val es_4 =
            n_len.getConcreteValue() match {
              case Some(len) if !bInitValue => {
                val present =
                  (0 to (len.toInt-1)).foldLeft(false)((b, k) => {
                    if (b) b
                    else {
                      val v = lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h_1, l, AbsString.alpha(k.toString)))
                      val v_1 = Value(PValue(UndefBot, v._1._2, v._1._3, v._1._4, v._1._5), v._2)
                      v_1 </ ValueBot
                    }
                  })
                if (!present) Set[Exception](TypeError)
                else ExceptionBot
              }
              case _ => Set[Exception](TypeError)
            }

          val es = es_1 ++ es_2 ++ es_3 ++ es_4
          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)

          ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
        }),
      "Array.prototype.reduce.call" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_this = h(SinglePureLocalLoc)("@this")._1._2

          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head

          val v_callbackfn = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val v_initial =
            n_arglen.getConcreteValue() match {
              case Some(d) if d >= 2 => Some(getArgValue(h, ctx, args, "1"))
              case None if AbsNumber.alpha(2) <= n_arglen => Some(getArgValue(h, ctx, args, "1"))
              case None => None
              case _ => None
            }

          val addr1 = cfg.getAPIAddress(addr_env, 1)
          val addr2 = cfg.getAPIAddress(addr_env, 2)
          val addr3 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val l_r2 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)

          val (v_this2, h_3, ctx_3, es_1) = Helper.toObject(h_2, ctx_2, v_this, addr3)

          // 1.
          val cond = v_callbackfn._2.exists((l) => BoolFalse <= Helper.IsCallable(h_3, l))
          val es =
            if (cond) Set[Exception](TypeError)
            else Set[Exception]()
          val (h_e, ctx_e) = Helper.RaiseException(h_3, ctx_3, es)
          val lset_f = v_callbackfn._2.filter((l) => BoolTrue <= Helper.IsCallable(h_3, l))
          val lset_this = v_this2._2

          val value = lset_this.foldLeft(ValueBot)((v, l) => v + Helper.Proto(h_3, l, absNumberToString(AbsNumber.naturalNumbers)))
          val temp = h_3(SinglePureLocalLoc)("temp")._1._1._1
          val temp_2 = v_initial match {
            case Some(v) => v + temp + value
            case None => temp + value
          }

          // 2., 3. create Arguments object
          val o_arg =
            Helper.NewArgObject(AbsNumber.alpha(4))
              .update(AbsString.alpha("0"), PropValue(ObjectValue(temp_2, T, T, T))) // accumulator
              .update(AbsString.alpha("1"), PropValue(ObjectValue(value, T, T, T))) // kValue
              .update(AbsString.alpha("2"), PropValue(ObjectValue(Value(AbsNumber.naturalNumbers), T, T, T))) // k
              .update(AbsString.alpha("3"), PropValue(ObjectValue(v_this2, T, T, T))) // O

          val h_4 = h_3.update(l_r1, o_arg)
          val v_arg = Value(l_r1)

          val callee_this = Value(GlobalSingleton)

          val o_old = h_4(SinglePureLocalLoc)
          val cc_caller = cp._2
          val n_aftercall = cfg.getAftercallFromCall(cp._1)
          val cp_aftercall = (n_aftercall, cc_caller)
          val n_aftercatch = cfg.getAftercatchFromCall(cp._1)
          val cp_aftercatch = (n_aftercatch, cc_caller)
          lset_f.foreach((l_f) => {
            val o_f = h_4(l_f)
            o_f("@function")._1._3.foreach((fid) => {
              cc_caller.NewCallContext(cfg, fid, l_r2, callee_this._2).foreach((pair) => {
                val (cc_new, o_new) = pair
                val o_new2 = o_new.
                  update(cfg.getArgumentsName(fid),
                  PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))).
                  update("@scope", o_f("@scope")._1)
                sem.addCallEdge(cp, ((fid, LEntry), cc_new), ContextEmpty, o_new2)
                sem.addReturnEdge(((fid, LExit), cc_new), cp_aftercall, ctx_3, o_old)
                sem.addReturnEdge(((fid, LExitExc), cc_new), cp_aftercatch, ctx_3, o_old)
              })
            })
          })
          val h_5 = v_arg._2.foldLeft(HeapBot)((hh, l) => {
            hh + h_4.update(l, h_4(l).update("callee",
              PropValue(ObjectValue(Value(lset_f), BoolTrue, BoolFalse, BoolTrue))))
          })

          val s_1 = (he + h_e, ctxe + ctx_e)
          ((h_5, ctx_3), s_1)
        })
    )
  }


  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("Array" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = PreHelper.Oldify(h, ctx, addr1)
          val v_1 = getArgValue_pre(h_1, ctx_1, args, "0", PureLocalLoc)
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h_1, ctx_1, args, "length", PureLocalLoc))

          // case for "new Array(n)"
          val (h_arg_1, es1) =
            if(v_1 </ ValueBot) {
              val es = v_1._1._4 match {
                case UInt => ExceptionBot
                case UIntSingle(_) => ExceptionBot
                case NumBot => ExceptionBot
                case _ => Set[Exception](RangeError)
              }
              val v_notNum = Value(PValue(v_1._1._1,v_1._1._2,v_1._1._3,NumBot,v_1._1._5), v_1._2)
              // case for new Array("value")
              val o_notNum =
                if (v_notNum </ ValueBot) {
                  PreHelper.NewArrayObject(AbsNumber.alpha(1)).
                    update("0", PropValue(ObjectValue(v_notNum, BoolTrue, BoolTrue, BoolTrue)), AbsentBot)
                }
                else
                  ObjBot
              // case for new Array(len)
              val o_num =
                if (v_1._1._4 </ NumBot) {
                  PreHelper.NewArrayObject(Operator.ToUInt32(Value(v_1._1._4)))
                }
                else
                  ObjBot
              (h_1.update(l_r, o_notNum + o_num), es)
            }
            else {
              (h, ExceptionBot)
            }

          val (h_2, es2) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              (h_arg_1, es1)
            case UIntSingle(n) if n != 1 => {
              // case for "new Array([v_1[, v_2[, ...])
              val o = (0 until n.toInt).foldLeft(PreHelper.NewArrayObject(n_arglen))((_o, i) =>
                _o.update(i.toString, PropValue(ObjectValue(getArgValue_pre(h_1, ctx_1, args, i.toString, PureLocalLoc), BoolTrue, BoolTrue, BoolTrue))))
              (h_1.update(l_r, o), ExceptionBot)
            }
            case NumBot => (h_1, ExceptionBot)
            case _ => {
              val o = PreHelper.NewArrayObject(UInt).
                update(NumStr, PropValue(ObjectValue(getArgValueAbs_pre(h_1, ctx_1, args, NumStr, PureLocalLoc),BoolTrue,BoolTrue,BoolTrue)))
              val h_uint = h_1.update(l_r, o)
              (h_arg_1 + h_uint, es1)
            }
          }

          val (h_e, ctx_e) = PreHelper.RaiseException(h_2, ctx, PureLocalLoc, es2)

          if (!(h_e <= HeapBot))
            ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(l_r)), ctx_e), (he + h_e, ctxe + ctx_e))
          else
            ((h_e, ctx_e), (he + h_e, ctxe + ctx_e))
        })),
      ("Array.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val v_1 = getArgValue_pre(h, ctx, args, "0", PureLocalLoc)
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))

          // case for "new Array(n)"
          val (h_arg_1, es1) =
            if(v_1 </ ValueBot) {
              val es = v_1._1._4 match {
                case UInt => ExceptionBot
                case UIntSingle(_) => ExceptionBot
                case NumBot => ExceptionBot
                case _ => Set[Exception](RangeError)
              }
              val v_notNum = Value(PValue(v_1._1._1,v_1._1._2,v_1._1._3,NumBot,v_1._1._5), v_1._2)
              // case for new Array("value")
              val o_notNum =
                if (v_notNum </ ValueBot) {
                  PreHelper.NewArrayObject(AbsNumber.alpha(1)).
                    update("0", PropValue(ObjectValue(v_notNum, BoolTrue, BoolTrue, BoolTrue)), AbsentBot)
                }
                else
                  ObjBot
              // case for new Array(len)
              val o_num =
                if (v_1._1._4 </ NumBot) {
                  PreHelper.NewArrayObject(Operator.ToUInt32(Value(v_1._1._4)))
                }
                else
                  ObjBot
              (lset_this.foldLeft(h)((_h,l) => _h.update(l, o_notNum + o_num)), es)
            }
            else {
              (h, ExceptionBot)
            }

          val (h_2, es2) = n_arglen match {
            case UIntSingle(n) if n == 1 =>
              (h_arg_1, es1)
            case UIntSingle(n) if n != 1 => {
              // case for "new Array([v_1[, v_2[, ...])
              val o = (0 until n.toInt).foldLeft(PreHelper.NewArrayObject(n_arglen))((_o, i) =>
                _o.update(i.toString, PropValue(ObjectValue(getArgValue_pre(h, ctx, args, i.toString, PureLocalLoc), BoolTrue, BoolTrue, BoolTrue))))
              (lset_this.foldLeft(h)((_h,l) => _h.update(l, o)), ExceptionBot)
            }
            case NumBot => (HeapBot, ExceptionBot)
            case _ => {
              val o = PreHelper.NewArrayObject(UInt).
                update(NumStr, PropValue(ObjectValue(getArgValueAbs_pre(h, ctx, args, NumStr, PureLocalLoc),BoolTrue,BoolTrue,BoolTrue)))
              val h_uint = lset_this.foldLeft(h)((_h,l) => _h.update(l, o))
              (h_arg_1 + h_uint, es1)
            }
          }

          val (h_e, ctx_e) = PreHelper.RaiseException(h_2, ctx, PureLocalLoc, es2)

          if (!(h_e <= HeapBot))
            ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(lset_this)), ctx_e), (he + h_e, ctxe + ctx_e))
          else
            ((h_e, ctx_e), (he + h_e, ctxe + ctx_e))
        })),
      ("Array.isArray" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v = getArgValue_pre(h, ctx, args, "0", PureLocalLoc)
          val b_1 =
            if (v._1 </ PValueBot) BoolFalse
            else BoolBot
          val b_2 = v._2.foldLeft[AbsBool](BoolBot)((_b, l) => {
            val _b1 =
              if (AbsString.alpha("Array") <= h(l)("@class")._1._2._1._5) BoolTrue
              else BoolBot
            val _b2 =
              if (AbsString.alpha("Array") </ h(l)("@class")._1._2._1._5) BoolFalse
              else BoolBot
            _b + _b1 + _b2})
          val b = b_1 + b_2
          if (b </ BoolBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(b)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("Array.prototype.toString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val s_sep = AbsString.alpha(",")
          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) =>
            _v + PreHelper.Proto(h, l, AbsString.alpha("length"))))
          val s = n_len match {
            case UIntSingle(n) if n == 0 => AbsString.alpha("")
            case UIntSingle(n) if n > 0 => {
              val v_f = lset_this.foldLeft(ValueBot)((_v, l) =>_v + PreHelper.Proto(h, l, AbsString.alpha("0")))
              val v_f2 = Value(PValue(UndefBot,NullBot,v_f._1._3,v_f._1._4,v_f._1._5), v_f._2)
              val s_first =
                if (v_f._1._1 </ UndefBot || v_f._1._2 </ NullBot)
                  AbsString.alpha("") + PreHelper.toString(PreHelper.toPrimitive(v_f2))
                else
                  PreHelper.toString(PreHelper.toPrimitive(v_f))
              (1 until n.toInt).foldLeft(s_first)((_s, i) =>{
                val v_i = lset_this.foldLeft(ValueBot)((_v, l) =>_v + PreHelper.Proto(h, l, AbsString.alpha(i.toString)))
                val v_i2 = Value(PValue(UndefBot,NullBot,v_i._1._3,v_i._1._4,v_i._1._5), v_i._2)
                val s_i =
                  if (v_i._1._1 </ UndefBot || v_i._1._2 </ NullBot)
                    AbsString.alpha("") + PreHelper.toString(PreHelper.toPrimitive(v_i2))
                  else
                    PreHelper.toString(PreHelper.toPrimitive(v_i))
                _s.concat(s_sep).concat(s_i)
              })
            }
            case _ => StrTop
          }
          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      "Array.prototype.toLocaleString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_this = h(PureLocalLoc)("@this")._1._2

          // Get a new address
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)

          // 1. Let array be the result of calling ToObject passing the this value as the argument.
          val (v_this2, h_1, ctx_1, es_1) = PreHelper.toObject(h, ctx, v_this, addr1)
          val lset_this = v_this2._2

          // 2. Let arrayLen be the result of calling the [[Get]] internal method of array with argument "length".
          val v_len = lset_this.foldLeft(ValueBot)((_v, l) => _v + PreHelper.Proto(h_1, l, AbsString.alpha("length")))
          // 3. Let len be ToUint32(arrayLen).
          val n_len = Operator.ToUInt32(v_len)

          val elements_1 =
            if (BoolTrue <= Operator.bopLessEq(Value(AbsNumber.alpha(1)), Value(n_len))._1._3) {
              lset_this.foldLeft(ValueBot)((_v, l) => _v + PreHelper.Proto(h_1, l, AbsString.NumTop))
            } else {
              ValueBot
            }
          // a. Let elementObj be ToObject(firstElement).
          val (_, h_2, ctx_2, es_2) = PreHelper.toObject(h_1, ctx_1, elements_1, addr2)

          // iii. If IsCallable(func) is false, throw a TypeError exception.
          val es_3 = Set[Exception](TypeError)

          val (h_3, ctx_3) = (PreHelper.ReturnStore(h_2, PureLocalLoc, Value(StrTop)), ctx_2)
          val es = es_1 ++ es_2 ++ es_3
          val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)

          ((h_3, ctx_3), (he + h_e, ctxe + ctx_e))
        }),
      ("Array.prototype.concat" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = PreHelper.Oldify(h, ctx, addr1)
          val lset_this = h_1(PureLocalLoc)("@this")._1._2._2

          val n_arglen = Operator.ToUInt32(getArgValue_pre(h_1, ctx_1, args, "length", PureLocalLoc))

          val o = n_arglen match {
            case NumBot => ObjBot
            case UIntSingle(n_arg) => {
              val elem_list = (0 until n_arg.toInt).foldLeft[List[Value]](List(Value(lset_this)))((list, i) =>
                list :+ getArgValue_pre(h_1, ctx_1, args, i.toString, PureLocalLoc))
              val obj = PreHelper.NewArrayObject(AbsNumber.alpha(0))
              val index = AbsNumber.alpha(0)
              val (obj_1, len) = elem_list.foldLeft((obj, index))((oi, elem) => {
                val lset_array = elem._2.filter((l) => AbsString.alpha("Array") <= h_1(l)("@class")._1._2._1._5)
                val lset_narray = elem._2.filter((l) => AbsString.alpha("Array") != h_1(l)("@class")._1._2._1._5)
                val v_narray = Value(elem._1, lset_narray)
                val o = oi._1
                val index = oi._2
                val (o_1, n_index_1) =
                  if (!lset_array.isEmpty) {
                    lset_array.foldLeft[(Obj,AbsNumber)]((ObjBot, NumBot))((_oi, l) => {
                      val n_len = Operator.ToUInt32(PreHelper.Proto(h_1, l, AbsString.alpha("length")))
                      val __o = n_len match {
                        case UIntSingle(n) => {
                          (0 until n.toInt).foldLeft(o)((o_new, i)=>
                            o_new.update(PreHelper.toString(Operator.bopPlus(Value(index), Value(AbsNumber.alpha(i)))._1),
                              PropValue(ObjectValue(PreHelper.Proto(h_1, l, AbsString.alpha(i.toString)),BoolTrue,BoolTrue,BoolTrue))))
                        }
                        case NumBot => ObjBot
                        case _ =>
                          val v_all = PreHelper.Proto(h_1, l, NumStr)
                          o.update(NumStr, PropValue(ObjectValue(v_all,BoolTrue,BoolTrue,BoolTrue)))
                      }
                      val __i = Operator.bopPlus(Value(index), Value(n_len))._1._4
                      (_oi._1 + __o , _oi._2 + __i)
                    })
                  }
                  else
                    (ObjBot, NumBot)
                val (o_2, n_index_2) =
                  if (v_narray </ ValueBot) {
                    val _o = o.update(PreHelper.toString(PValue(index)), PropValue(ObjectValue(elem, BoolTrue, BoolTrue, BoolTrue)))
                    val _i = Operator.bopPlus(Value(index), Value(AbsNumber.alpha(1)))._1._4
                    (_o, _i)
                  }
                  else
                    (ObjBot, NumBot)
                (o_1 + o_2, n_index_1 + n_index_2)})
              obj_1.update("length", PropValue(ObjectValue(Value(len), BoolTrue, BoolFalse, BoolFalse)))
            }
            case _ => {
              val v_all = Value(lset_this) + getArgValueAbs_pre(h_1, ctx_1, args, NumStr, PureLocalLoc)
              val lset_array = v_all._2.filter((l) => AbsString.alpha("Array") <= h_1(l)("@class")._1._2._1._5)
              val v_array = lset_array.foldLeft(ValueBot)((_v, l) => _v + PreHelper.Proto(h_1, l, NumStr))
              PreHelper.NewArrayObject(UInt).update(NumStr, PropValue(ObjectValue(v_all + v_array, BoolTrue,BoolTrue,BoolTrue)))
            }
          }
          if (o </ ObjBot){
            val h_2 = h_1.update(l_r, o)
            ((PreHelper.ReturnStore(h_2, PureLocalLoc, Value(l_r)), ctx), (he, ctxe))
          }
          else
            ((h_1, ctx), (he, ctxe))
        })),
      ("Array.prototype.join" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val v_sep = getArgValue_pre(h, ctx, args, "0", PureLocalLoc)
          val v_sep2 = Value(PValue(UndefBot,v_sep._1._2,v_sep._1._3,v_sep._1._4,v_sep._1._5), v_sep._2)
          val s_sep =
            if (v_sep._1._1 </ UndefBot)
              AbsString.alpha(",") + PreHelper.toString(PreHelper.toPrimitive(v_sep2))
            else
              PreHelper.toString(PreHelper.toPrimitive(v_sep))

          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) =>
            _v + PreHelper.Proto(h, l, AbsString.alpha("length"))))

          val s = n_len match {
            case UIntSingle(n) if n == 0 => AbsString.alpha("")
            case UIntSingle(n) if n > 0 => {
              val v_f = lset_this.foldLeft(ValueBot)((_v, l) =>_v + PreHelper.Proto(h, l, AbsString.alpha("0")))
              val v_f2 = Value(PValue(UndefBot,NullBot,v_f._1._3,v_f._1._4,v_f._1._5), v_f._2)
              val s_first =
                if (v_f._1._1 </ UndefBot || v_f._1._2 </ NullBot)
                  AbsString.alpha("") + PreHelper.toString(PreHelper.toPrimitive(v_f2))
                else
                  PreHelper.toString(PreHelper.toPrimitive(v_f))
              (1 until n.toInt).foldLeft(s_first)((_s, i) =>{
                val v_i = lset_this.foldLeft(ValueBot)((_v, l) =>_v + PreHelper.Proto(h, l, AbsString.alpha(i.toString)))
                val v_i2 = Value(PValue(UndefBot,NullBot,v_i._1._3,v_i._1._4,v_i._1._5), v_i._2)
                val s_i =
                  if (v_i._1._1 </ UndefBot || v_i._1._2 </ NullBot)
                    AbsString.alpha("") + PreHelper.toString(PreHelper.toPrimitive(v_i2))
                  else
                    PreHelper.toString(PreHelper.toPrimitive(v_i))
                _s.concat(s_sep).concat(s_i)
              })
            }
            case _ => StrTop
          }

          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("Array.prototype.pop" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) =>
            _v + PreHelper.Proto(h, l, AbsString.alpha("length"))))

          val (h_1, v) = lset_this.foldLeft((h, ValueBot))((hv, l) => {
            if (!(hv._1 <= HeapBot)) {
              val n_len = Operator.ToUInt32(PreHelper.Proto(hv._1, l, AbsString.alpha("length")))
              val (_h, _v) = n_len match {
                case UIntSingle(n) if n == 0 => {
                  val __h = PreHelper.PropStore(hv._1, l, AbsString.alpha("length"), Value(AbsNumber.alpha(0)))
                  (__h, Value(UndefTop))
                }
                case UIntSingle(n) if n > 0 => {
                  val __v = PreHelper.Proto(hv._1, l,  AbsString.alpha((n-1).toInt.toString))
                  val __h = PreHelper.Delete(hv._1, l, AbsString.alpha((n-1).toInt.toString))._1
                  (PreHelper.PropStore(__h, l, AbsString.alpha("length"), Value(AbsNumber.alpha((n-1)))), __v)
                }
                case NumBot =>
                  (hv._1, ValueBot)
                case _ => {
                  val __v = PreHelper.Proto(hv._1, l, NumStr)
                  val __h = PreHelper.Delete(hv._1, l, NumStr)._1
                  (PreHelper.PropStore(__h, l, AbsString.alpha("length"), Value(UInt)), __v)
                }
              }
              (_h, hv._2 + _v)
            }
            else {
              (hv._1, hv._2)
            }
          })
          if (v </ ValueBot)
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, v), ctx), (he, ctxe))
          else
            ((h_1, ctx), (he, ctxe))
        })),
      ("Array.prototype.push" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))
          val (h_1, v) = n_arglen match {
            case NumBot => (h, ValueBot)
            case UIntSingle(n_arg) => {
              lset_this.foldLeft((h, ValueBot))((hv, l) => {
                if (!(hv._1 <= HeapBot)) {
                  val n_len = Operator.ToUInt32(PreHelper.Proto(hv._1, l, AbsString.alpha("length")))
                  n_len match {
                    case NumBot => (hv._1, ValueBot)
                    case UIntSingle(n) => {
                      val _h = (0 until n_arg.toInt).foldLeft(hv._1)((__h, i) => {
                        PreHelper.PropStore(__h, l, AbsString.alpha((i+n).toInt.toString), getArgValue_pre(__h, ctx, args, (i.toString), PureLocalLoc))
                      })
                      val _v = Value(AbsNumber.alpha(n_arg+n))
                      val _h1 = PreHelper.PropStore(_h, l, AbsString.alpha("length"), _v)
                      (_h1, hv._2 + _v)
                    }
                    case _ => {
                      val v_argall = getArgValueAbs_pre(hv._1, ctx, args, NumStr, PureLocalLoc)
                      val _h = PreHelper.PropStore(hv._1, l, AbsString.alpha("length"), Value(UInt))
                      (PreHelper.PropStore(_h, l, NumStr, v_argall), Value(UInt))
                    }
                  }
                }
                else {
                  (hv._1, hv._2)
                }
              })
            }
            case _ => {
              val v_argall = getArgValueAbs_pre(h, ctx, args, NumStr, PureLocalLoc)
              (lset_this.foldLeft(h)((_h, l) => {
                val h1 = PreHelper.PropStore(_h, l, NumStr, v_argall)
                PreHelper.PropStore(h1, l, AbsString.alpha("length"), Value(UInt))
              }), Value(UInt))
            }
          }
          if (v </ ValueBot)
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, v), ctx), (he, ctxe))
          else
            ((h_1, ctx), (he, ctxe))
        })),
      ("Array.prototype.reverse" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val h_1 = lset_this.foldLeft(h)((_h, l) => {
            val n_len = Operator.ToUInt32(PreHelper.Proto(h, l, AbsString.alpha("length")))
            n_len match {
              case UIntSingle(n) => {
                (0 until floor(n/2).toInt).foldLeft(_h)((_h, i) =>{
                  val s_low = AbsString.alpha(i.toString)
                  val s_up = AbsString.alpha((n-i-1).toInt.toString)
                  val v_low = PreHelper.Proto(_h, l, s_low)
                  val v_up = PreHelper.Proto(_h, l, s_up)
                  val b_low = PreHelper.HasProperty(_h, l, s_low)
                  val b_up = PreHelper.HasProperty(_h, l, s_up)
                  val _h1 =
                    if (BoolTrue <= b_low && BoolTrue <= b_up) {
                      val __h1 = PreHelper.PropStore(_h, l, s_low, v_up)
                      PreHelper.PropStore(__h1, l, s_up, v_low)
                    }
                    else  {
                      _h
                    }
                  val _h2 =
                    if (BoolFalse <= b_low && BoolTrue <= b_up) {
                      val __h1 = PreHelper.PropStore(_h1, l, s_low, v_up)
                      PreHelper.Delete(__h1, l, s_up)._1
                    }
                    else  {
                      _h1
                    }
                  val _h3 =
                    if (BoolTrue <= b_low && BoolFalse <= b_up) {
                      val __h1 = PreHelper.PropStore(_h2, l, s_up,  v_low)
                      PreHelper.Delete(__h1, l, s_low)._1
                    }
                    else  {
                      _h2
                    }
                  val _h4 = _h3
                  _h4
                  //                 _h1 + _h2 + _h3 + _h4
                })
              }
              case NumBot => _h
              case _ =>
                val _h1 = PreHelper.PropStore(_h, l, NumStr, PreHelper.Proto(_h, l, NumStr))
                val _h2 = PreHelper.Delete(_h1, l, NumStr)._1
                //                (_h1 + _h2)
                _h2
            }
          })
          if (!(h_1 <= HeapBot))
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(lset_this)), ctx), (he, ctxe))
          else
            ((h_1, ctx), (he, ctxe))
        })),
      ("Array.prototype.shift" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val (h_1, v) = lset_this.foldLeft((h,ValueBot))((_hv, l) => {
            val n_len = Operator.ToUInt32(PreHelper.Proto(_hv._1, l, AbsString.alpha("length")))
            val _h = _hv._1
            val _v = _hv._2
            n_len match {
              case UIntSingle(n) => {
                if (n == 0) {
                  (PreHelper.PropStore(_h, l, AbsString.alpha("length"), Value(AbsNumber.alpha(0))), _v + Value(UndefTop))
                }
                else {
                  val v_first = PreHelper.Proto(_h, l, AbsString.alpha("0"))
                  val _h1 = (1 until n.toInt).foldLeft(_h)((__h, i) => {
                    val s_from = AbsString.alpha(i.toString)
                    val s_to = AbsString.alpha((i-1).toString)
                    val b = PreHelper.HasProperty(__h, l, s_from)
                    val __h1 =
                      if (BoolTrue <= b)  PreHelper.PropStore(__h, l, s_to, PreHelper.Proto(__h, l, s_from))
                      else __h
                    val __h2 =
                      if (BoolFalse <= b)  PreHelper.Delete(__h1, l, s_to)._1
                      else __h1
                    __h2
                    //                  __h1 + __h2
                  })
                  val _h2 = PreHelper.Delete(_h1, l, AbsString.alpha((n-1).toInt.toString))._1
                  val _h3 = PreHelper.PropStore(_h2, l, AbsString.alpha("length"), Value(AbsNumber.alpha(n-1)))
                  (_h3, _v + v_first)
                }
              }
              case NumBot => (_h, ValueBot)
              case _ => {
                val _v = PreHelper.Proto(_h, l, NumStr)
                val _h1 = PreHelper.Delete(_h, l, NumStr)._1
                val _h2 = PreHelper.PropStore(_h1, l, AbsString.alpha("length"), Value(UInt))
                (_h2, _v + Value(UInt))
              }
            }
          })
          if (v </ ValueBot)
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, v), ctx), (he, ctxe))
          else
            ((h_1, ctx), (he, ctxe))
        })),
      ("Array.prototype.slice" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
          val lset_this = h_1(PureLocalLoc)("@this")._1._2._2
          val n_start = Operator.ToInteger(getArgValue_pre(h_1, ctx_1, args, "0", PureLocalLoc))
          val v_end = getArgValue_pre(h, ctx, args, "1", PureLocalLoc)

          val o = (AbsNumber.concretize(n_start)) match {
            case (Some(start)) =>
              lset_this.foldLeft(ObjBot)((_o, l) => {
                val n_len = Operator.ToUInt32(PreHelper.Proto(h_1, l, AbsString.alpha("length")))
                val n_end =
                  if (v_end._1._1 </ UndefBot)  n_len + v_end._1._4
                  else Operator.ToInteger(v_end)
                AbsNumber.concretize(n_end) match {
                  case Some(end) =>
                    n_len match {
                      case UIntSingle(n) => {
                        val from =
                          if (start < 0) max(n + start, 0).toInt
                          else min(start, n).toInt
                        val to =
                          if (end < 0) max(n + end, 0).toInt
                          else min(end, n).toInt
                        val span = max(to-from, 0)
                        val o_new = _o + PreHelper.NewArrayObject(AbsNumber.alpha(span))
                        (0 until span).foldLeft(o_new)((__o, i) => {
                          val b = PreHelper.HasProperty(h_1, l, AbsString.alpha(i.toString))
                          val _o1 =
                            if (BoolTrue <= b)
                              __o.update(AbsString.alpha(i.toString),
                                PropValue(ObjectValue(PreHelper.Proto(h_1, l, AbsString.alpha((from+i).toString)),BoolTrue,BoolTrue,BoolTrue)))
                            else ObjBot
                          val _o2 =
                            if (BoolFalse <= b) __o
                            else ObjBot
                          _o1 + _o2 })
                      }
                      case NumBot => ObjBot
                      case _ =>
                        val o_new = _o + PreHelper.NewArrayObject(UInt)
                        o_new.update(NumStr, PropValue(ObjectValue(PreHelper.Proto(h_1, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                    }
                  case None =>
                    if (n_end <= NumBot)
                      ObjBot
                    else
                      lset_this.foldLeft(ObjBot)((_o, l) => {
                        val n_len = Operator.ToUInt32(PreHelper.Proto(h_1, l, AbsString.alpha("length")))
                        n_len match {
                          case NumBot => ObjBot
                          case _ =>
                            val o_new = _o + PreHelper.NewArrayObject(UInt)
                            o_new.update(NumStr, PropValue(ObjectValue(PreHelper.Proto(h_1, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                        } })
                }
              })
            case _ =>
              if (n_start <= NumBot)
                ObjBot
              else {
                lset_this.foldLeft(ObjBot)((_o, l) => {
                  val n_len = Operator.ToUInt32(PreHelper.Proto(h_1, l, AbsString.alpha("length")))
                  n_len match {
                    case NumBot => ObjBot
                    case _ =>
                      val o_new = _o + PreHelper.NewArrayObject(UInt)
                      o_new.update(NumStr, PropValue(ObjectValue(PreHelper.Proto(h_1, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                  }
                })
              }
          }
          if (o </ ObjBot) {
            val h_2 = h_1.update(l_r, o)
            ((PreHelper.ReturnStore(h_2, PureLocalLoc, Value(l_r)), ctx_1), (he, ctxe))
          }
          else {
            ((h_1, ctx_1), (he, ctxe))
          }
        })),
      ("Array.prototype.splice" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
          val lset_this = h_1(PureLocalLoc)("@this")._1._2._2

          val n_arglen = Operator.ToUInt32(getArgValue_pre(h_1, ctx_1, args, "length", PureLocalLoc))
          val n_start = Operator.ToInteger(getArgValue_pre(h_1, ctx_1, args, "0", PureLocalLoc))
          val n_count = Operator.ToInteger(getArgValue_pre(h_1, ctx_1, args, "1", PureLocalLoc))
          val (h_2, o) = (AbsNumber.concretize(n_start),
            AbsNumber.concretize(n_count)) match {
            case (Some(start), Some(count)) =>
              lset_this.foldLeft((h_1, ObjBot))((_ho, l) => {
                val _h = _ho._1
                val _o = _ho._2
                val n_len = Operator.ToUInt32(PreHelper.Proto(_h, l, AbsString.alpha("length")))
                n_len match {
                  case UIntSingle(n_len) => {
                    val from =
                      if (start < 0) max(n_len + start, 0).toInt
                      else min(start, n_len).toInt
                    val delCount = min(max(count, 0), n_len - start).toInt
                    val o_new = PreHelper.NewArrayObject(AbsNumber.alpha(delCount))
                    val o_1 = (0 until delCount).foldLeft(o_new)((__o, i) => {
                      val b = PreHelper.HasProperty(_h, l, AbsString.alpha(i.toString))
                      val _o1 =
                        if (BoolTrue <= b)
                          __o.update(AbsString.alpha(i.toString),
                            PropValue(ObjectValue(PreHelper.Proto(_h, l, AbsString.alpha((from+i).toString)),BoolTrue,BoolTrue,BoolTrue)))
                        else ObjBot
                      val _o2 =
                        if (BoolFalse <= b) __o
                        else ObjBot
                      _o1 + _o2
                    })
                    val _h1 = n_arglen match {
                      case UIntSingle(n_arglen) => {
                        val add_count = n_arglen.toInt - 2
                        val move_start = start + count
                        if (add_count < count) {
                          val __h1 = (move_start.toInt until n_len.toInt).foldLeft(_h)((__h, i) => {
                            val s_from = AbsString.alpha(i.toString)
                            val s_to = AbsString.alpha((i - count+add_count).toInt.toString)
                            val v = PreHelper.Proto(__h, l, s_from)
                            val b = PreHelper.HasProperty(__h, l, s_from)
                            val __h1 =
                              if (BoolTrue <= b)
                                PreHelper.PropStore(__h, l, s_to, v)
                              else __h
                            val __h2 =
                              if (BoolFalse <= b) PreHelper.Delete(__h, l, s_to)._1
                              else __h
                            __h1 + __h2
                          })
                          val __h2 = (0 until add_count).foldLeft(__h1)((__h, i) =>
                            PreHelper.PropStore(__h, l, AbsString.alpha((start + i).toInt.toString), getArgValue_pre(__h, ctx_1, args, (i+2).toString, PureLocalLoc)))
                          val new_length = n_len + add_count - count
                          val __h3 = (new_length.toInt until n_len.toInt).foldLeft(__h2)((__h, i) =>
                            PreHelper.Delete(__h, l, AbsString.alpha(i.toString))._1)
                          PreHelper.PropStore(__h3, l,  AbsString.alpha("length"), Value(AbsNumber.alpha(new_length)))
                        }
                        else {
                          val __h1 = (0 until (n_len-move_start).toInt).foldLeft(_h)((__h, i) => {
                            val s_from = AbsString.alpha((n_len -1 - i).toInt.toString)
                            val s_to = AbsString.alpha((n_len -1 -i + add_count - count).toInt.toString)
                            val v = PreHelper.Proto(__h, l, s_from)
                            val b = PreHelper.HasProperty(__h, l, s_from)
                            val __h1 =
                              if (BoolTrue <= b) PreHelper.PropStore(__h, l, s_to, v)
                              else __h
                            val __h2 =
                              if (BoolFalse <= b) PreHelper.Delete(__h1, l, s_to)._1
                              else __h1
                            __h2
                            //__h1 + __h2
                          })
                          val __h2 = (0 until add_count).foldLeft(__h1)((__h, i) =>
                            PreHelper.PropStore(__h, l, AbsString.alpha((start + i).toInt.toString), getArgValue_pre(__h, ctx_1, args, (i+2).toString, PureLocalLoc)))
                          val new_length = n_len + add_count - count
                          PreHelper.PropStore(__h2, l,  AbsString.alpha("length"), Value(AbsNumber.alpha(new_length)))
                        }
                      }
                      case NumBot => _h
                      case _ =>
                        val _h1 = PreHelper.PropStore(h_1, l, NumStr, getArgValueAbs_pre(_h, ctx_1, args, NumStr, PureLocalLoc))
                        val _h2 = PreHelper.Delete(_h1, l, NumStr)._1
                        PreHelper.PropStore(_h2, l, AbsString.alpha("length"),Value(UInt))
                    }
                    //                  (_h + _h1, _o + o_1)
                    (_h1, _o + o_1)
                  }
                  case NumBot => (_h, _o)
                  case _ =>
                    val o_new = PreHelper.NewArrayObject(UInt)
                    val o_1 = o_new.update(NumStr, PropValue(ObjectValue(PreHelper.Proto(_h, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                    val _h1 = PreHelper.PropStore(_h, l, NumStr, getArgValueAbs_pre(_h, ctx_1, args, NumStr, PureLocalLoc))
                    val _h2 = PreHelper.Delete(_h1, l, NumStr)._1
                    val _h3 = PreHelper.PropStore(_h2, l, AbsString.alpha("length"),Value(UInt))
                    (_h3, _o + o_1)
                  // (_h + _h2, _o + o_1)
                }
              })
            case _ =>
              if (n_start <= NumBot || n_count <= NumBot) {
                (h_1, ObjBot)
              }
              else {
                lset_this.foldLeft((h_1, ObjBot))((_ho, l) => {
                  val n_len = Operator.ToUInt32(PreHelper.Proto(h_1, l, AbsString.alpha("length")))
                  val _h = _ho._1
                  val _o = _ho._2
                  n_len match {
                    case NumBot => (_h, _o)
                    case _ =>
                      val o_new = PreHelper.NewArrayObject(UInt)
                      val _o1 = o_new.update(NumStr, PropValue(ObjectValue(PreHelper.Proto(_h, l, NumStr),BoolTrue,BoolTrue,BoolTrue)))
                      val _h1 = PreHelper.Delete(_h, l, NumStr)._1
                      val _h2 = PreHelper.PropStore(_h1, l, NumStr, getArgValueAbs_pre(_h1, ctx_1, args, NumStr, PureLocalLoc))
                      val _h3 = PreHelper.PropStore(_h2, l, AbsString.alpha("length"),Value(UInt))
                      (_h3, _o + _o1)
                  }
                })
              }
          }
          if (o </ ObjBot) {
            val h_3 = h_2.update(l_r, o)
            ((PreHelper.ReturnStore(h_3, PureLocalLoc, Value(l_r)), ctx_1), (he, ctxe))
          }
          else{
            ((h_2, ctx_1), (he, ctxe))
          }
        })),
      ("Array.prototype.unshift" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))
          val (h_1, n) = n_arglen match {
            case UIntSingle(n_arglen) => {
              lset_this.foldLeft[(Heap, AbsNumber)]((h, NumBot))((_hv, l) => {
                val _h = _hv._1
                val _v = _hv._2
                if (_h <= HeapBot)
                  (_h, _v)
                else {
                  val n_len = Operator.ToUInt32(PreHelper.Proto(_h, l, AbsString.alpha("length")))
                  n_len match {
                    case UIntSingle(k) => {
                      val _h1 = (0 until k.toInt).foldLeft(_h)((__h, i) => {
                        val s_from = AbsString.alpha((k - 1 - i).toInt.toString)
                        val s_to = AbsString.alpha((k -1 -i +n_arglen).toInt.toString)
                        val b = PreHelper.HasProperty(__h, l, s_from)
                        val __h1 =
                          if (BoolTrue <= b) PreHelper.PropStore(__h, l, s_to, PreHelper.Proto(__h, l, s_from))
                          else __h
                        val __h2 =
                          if (BoolFalse <= b) PreHelper.Delete(__h1, l, s_to)._1
                          else __h1
                        __h2
                        // (__h1 + __h2)
                      })
                      val _h2 = (0 until n_arglen.toInt).foldLeft(_h1)((__h, i) => {
                        val v_i = getArgValue_pre(__h, ctx, args, i.toString, PureLocalLoc)
                        PreHelper.PropStore(__h, l, AbsString.alpha(i.toString), v_i)
                      })
                      val _h3 = PreHelper.PropStore(_h2, l, AbsString.alpha("length"), Value(AbsNumber.alpha(n_arglen + k)))
                      (_h3, _v + AbsNumber.alpha(n_arglen + k))
                    }
                    case NumBot =>
                      (_h, _v)
                    case _ => {
                      val _h1 = PreHelper.PropStore(_h, l, NumStr, PreHelper.Proto(_h, l, NumStr))
                      val _h2 = PreHelper.Delete(_h1, l, NumStr)._1
                      val _h3 = PreHelper.PropStore(_h2, l, AbsString.alpha("length"),Value(UInt))
                      (_h3, _v + UInt)
                      // (_h1 + _h2, _v + UInt)
                    }
                  }
                }
              })
            }
            case NumBot => (h, NumBot)
            case _ => {
              lset_this.foldLeft[(Heap, AbsNumber)]((h, NumBot))((_hv, l) => {
                val _h = _hv._1
                val _v = _hv._2
                if (_h <= HeapBot)
                  (_h, _v)
                else {
                  val n_len = Operator.ToUInt32(PreHelper.Proto(_h, l, AbsString.alpha("length")))
                  n_len match {
                    case NumBot =>
                      (_h, _v)
                    case _ => {
                      val _h1 = PreHelper.PropStore(_h, l, NumStr, PreHelper.Proto(_h, l, NumStr))
                      val _h2 = PreHelper.Delete(_h1, l, NumStr)._1
                      val _h3 = PreHelper.PropStore(_h2, l, AbsString.alpha("length"), Value(UInt))
                      (_h3, _v + UInt)
                      // (_h1 + _h2 + _h3, _v + UInt)
                    }
                  }
                }
              })
            }
          }
          if (n </ NumBot)
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(n)), ctx), (he, ctxe))
          else
            ((h_1, ctx), (he, ctxe))
        })),
      ("Array.prototype.indexOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))
          val n_index = n_arglen match {
            case UIntSingle(n) => {
              val v_search = getArgValue_pre(h, ctx, args, "0", PureLocalLoc)
              lset_this.foldLeft[AbsNumber](NumBot)((_n, l) => {
                val n_len = Operator.ToUInt32(PreHelper.Proto(h, l, AbsString.alpha("length")))
                _n + (n_len match {
                  case UIntSingle(n_len) => {
                    if (n_len == 0)
                      AbsNumber.alpha(-1)
                    else {
                      val start =
                        if (n > 1) Operator.ToInteger(getArgValue_pre(h, ctx, args, "1", PureLocalLoc))
                        else AbsNumber.alpha(0)
                      start match {
                        case UIntSingle(n_start) => {
                          if (n_start >= n_len)
                            AbsNumber.alpha(-1)
                          else {
                            val k =
                              if (n_start < 0) (n_len - abs(n_start))
                              else n_start
                            val (index, flag)= (k.toInt until n_len.toInt).foldLeft[(AbsNumber, Boolean)]((NumBot, false))((__nb, i) => {
                              if (__nb._2)
                                __nb
                              else {
                                val __n = __nb._1
                                Operator.bopSEq(v_search, PreHelper.Proto(h, l, AbsString.alpha(i.toString)))._1._3 match {
                                  case BoolTop => (__n + AbsNumber.alpha(i), false)
                                  case BoolBot => (NumBot, true)
                                  case BoolTrue => (AbsNumber.alpha(i), true)
                                  case BoolFalse => (__n, false)
                                }}})
                            if (flag)
                              index
                            else
                              index + AbsNumber.alpha(-1)
                          }
                        }
                        case NUIntSingle(n_start) => {
                          if (n_start >= n_len)
                            AbsNumber.alpha(-1)
                          else {
                            val k =
                              if (n_start < 0) (n_len - abs(n_start))
                              else n_start
                            val (index, flag)= (k.toInt until n_len.toInt).foldLeft[(AbsNumber, Boolean)]((NumBot, false))((__nb, i) => {
                              if (__nb._2)
                                __nb
                              else {
                                val __n = __nb._1
                                Operator.bopSEq(v_search, PreHelper.Proto(h, l, AbsString.alpha(i.toString)))._1._3 match {
                                  case BoolTop => (__n + AbsNumber.alpha(i), false)
                                  case BoolBot => (NumBot, true)
                                  case BoolTrue => (AbsNumber.alpha(i), true)
                                  case BoolFalse => (__n, false)
                                }}})
                            if (flag)
                              index
                            else
                              index + AbsNumber.alpha(-1)
                          }
                        }
                        case NumBot => _n
                        case _ => NumTop
                      }
                    }
                  }
                  case NumBot => _n
                  case _ => NumTop
                })
              })
            }
            case NumBot => NumBot
            case _ => NumTop
          }
          if (n_index </ NumBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(n_index)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("Array.prototype.lastIndexOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))
          val n_index = n_arglen match {
            case UIntSingle(n) => {
              val v_search = getArgValue_pre(h, ctx, args, "0", PureLocalLoc)
              lset_this.foldLeft[AbsNumber](NumBot)((_n, l) => {
                val n_len = Operator.ToUInt32(PreHelper.Proto(h, l, AbsString.alpha("length")))
                _n + (n_len match {
                  case UIntSingle(n_len) => {
                    if (n_len == 0)
                      AbsNumber.alpha(-1)
                    else {
                      val start =
                        if (n > 1) Operator.ToInteger(getArgValue_pre(h, ctx, args, "1", PureLocalLoc))
                        else AbsNumber.alpha(n_len - 1)
                      start match {
                        case UIntSingle(n_start) => {
                          val k =
                            if (n_start >= 0) min(n_start, n_len-1)
                            else (n_len - abs(n_start))
                          val (index, flag)= (0 until k.toInt).foldLeft[(AbsNumber, Boolean)]((NumBot, false))((__nb, i) => {
                            if (__nb._2)
                              __nb
                            else {
                              val __n = __nb._1
                              val i_back = (k - i).toInt
                              Operator.bopSEq(v_search, PreHelper.Proto(h, l, AbsString.alpha(i_back.toString)))._1._3 match {
                                case BoolTop => (__n + AbsNumber.alpha(i_back), false)
                                case BoolBot => (NumBot, true)
                                case BoolTrue => (AbsNumber.alpha(i_back), true)
                                case BoolFalse => (__n, false)
                              }}})
                          if (flag)
                            index
                          else
                            index + AbsNumber.alpha(-1)
                        }
                        case NUIntSingle(n_start) => {
                          val k =
                            if (n_start >= 0) min(n_start, n_len-1)
                            else (n_len - abs(n_start))
                          val (index, flag)= (0 until k.toInt).foldLeft[(AbsNumber, Boolean)]((NumBot, false))((__nb, i) => {
                            if (__nb._2)
                              __nb
                            else {
                              val __n = __nb._1
                              val i_back = (k - i).toInt
                              Operator.bopSEq(v_search, PreHelper.Proto(h, l, AbsString.alpha(i_back.toString)))._1._3 match {
                                case BoolTop => (__n + AbsNumber.alpha(i_back), false)
                                case BoolBot => (NumBot, true)
                                case BoolTrue => (AbsNumber.alpha(i_back), true)
                                case BoolFalse => (__n, false)
                              }}})
                          if (flag)
                            index
                          else
                            index + AbsNumber.alpha(-1)
                        }
                        case NumBot => _n
                        case _ => NumTop
                      }
                    }
                  }
                  case NumBot => _n
                  case _ => NumTop
                })
              })
            }
            case NumBot => NumBot
            case _ => NumTop
          }
          if (n_index </ NumBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(n_index)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      "Array.prototype.reduce.init" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_this = h(PureLocalLoc)("@this")._1._2

          // Get a new address
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)

          // 1. Let O be the result of calling ToObject passing the this value as the argument.
          val (_, h_1, ctx_1, es_1) = PreHelper.toObject(h, ctx, v_this, addr1)
          val es_2 = Set[Exception](TypeError)

          val es = es_1 ++ es_2
          val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)

          ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
        }),
      "Array.prototype.reduce.call" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val v_this = h(PureLocalLoc)("@this")._1._2

          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head

          val v_callbackfn = getArgValue_pre(h, ctx, args, "0", PureLocalLoc)
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))
          val v_initial =
            n_arglen.getConcreteValue() match {
              case Some(d) if d >= 2 => Some(getArgValue_pre(h, ctx, args, "1", PureLocalLoc))
              case None if AbsNumber.alpha(2) <= n_arglen => Some(getArgValue_pre(h, ctx, args, "1", PureLocalLoc))
              case None => None
              case _ => None
            }

          val addr1 = cfg.getAPIAddress(addr_env, 1)
          val addr2 = cfg.getAPIAddress(addr_env, 2)
          val addr3 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val l_r2 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2) = PreHelper.Oldify(h_1, ctx_1, addr2)

          val (v_this2, h_3, ctx_3, _) = PreHelper.toObject(h_2, ctx_2, v_this, addr3)

          // 1.
          val es = Set[Exception](TypeError)
          val (h_e, ctx_e) = PreHelper.RaiseException(h_3, ctx_3, PureLocalLoc, es)
          val lset_f = v_callbackfn._2.filter((l) => BoolTrue <= PreHelper.IsCallable(h_3, l))
          val lset_this = v_this2._2

          val value = lset_this.foldLeft(ValueBot)((v, l) => v + PreHelper.Proto(h_3, l, absNumberToString(AbsNumber.naturalNumbers)))
          val temp = h_3(PureLocalLoc)("temp")._1._1._1
          val temp_2 = v_initial match {
            case Some(v) => v + temp + value
            case None => temp + value
          }

          // 2., 3. create Arguments object
          val o_arg =
            PreHelper.NewArgObject(AbsNumber.alpha(4))
              .update(AbsString.alpha("0"), PropValue(ObjectValue(temp_2, T, T, T))) // accumulator
              .update(AbsString.alpha("1"), PropValue(ObjectValue(value, T, T, T))) // kValue
              .update(AbsString.alpha("2"), PropValue(ObjectValue(Value(AbsNumber.naturalNumbers), T, T, T))) // k
              .update(AbsString.alpha("3"), PropValue(ObjectValue(v_this2, T, T, T))) // O

          val h_4 = h_3.update(l_r1, o_arg)
          val v_arg = Value(l_r1)

          val callee_this = Value(GlobalSingleton)

          val o_old = h_4(PureLocalLoc)
          val cc_caller = cp._2
          val n_aftercall = cfg.getAftercallFromCall(cp._1)
          val cp_aftercall = (n_aftercall, cc_caller)
          val n_aftercatch = cfg.getAftercatchFromCall(cp._1)
          val cp_aftercatch = (n_aftercatch, cc_caller)
          lset_f.foreach((l_f) => {
            val o_f = h_4(l_f)
            o_f("@function")._1._3.foreach((fid) => {
              cc_caller.NewCallContext(cfg, fid, l_r2, callee_this._2).foreach((pair) => {
                val (cc_new, o_new) = pair
                val o_new2 = o_new.
                  update(cfg.getArgumentsName(fid),
                  PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))).
                  update("@scope", o_f("@scope")._1)
                sem.addCallEdge(cp, ((fid, LEntry), cc_new), ContextEmpty, o_new2)
                sem.addReturnEdge(((fid, LExit), cc_new), cp_aftercall, ctx_3, o_old)
                sem.addReturnEdge(((fid, LExitExc), cc_new), cp_aftercatch, ctx_3, o_old)
              })
            })
          })
          val h_5 = v_arg._2.foldLeft(h_4)((hh, l) => {
            hh.update(l, hh(l).update("callee",
              PropValue(ObjectValue(Value(lset_f), BoolTrue, BoolFalse, BoolTrue))))
          })

          val s_1 = (he + h_e, ctxe + ctx_e)
          ((h_5, ctx_3), s_1)
        })
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("Array" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          //val addr_env = set_addr.head
          //val addr1 = cfg.getAPIAddress(addr_env, 0)
          //val l_r = addrToLoc(addr1, Recent)
          //val LP1 = AH.Oldify_def(h,ctx,addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.Oldify_def(h,ctx,cfg.getAPIAddress(a, 0)))
          val v_1 = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val es1 = v_1._1._4 match {
            case UInt => ExceptionBot
            case UIntSingle(_) => ExceptionBot
            case NumBot => ExceptionBot
            case _ => Set[Exception](RangeError)
          }
          val es2 = n_arglen match {
            case UIntSingle(n) if n == 1 => es1
            case UIntSingle(n) if n != 1 => ExceptionBot
            case NumBot => ExceptionBot
            case _ => es1
          }
          // val LP2 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, prop) => _lpset + (l_r,prop))
          val LP2 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, prop) =>
            _lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent), prop)))
          val LP3 = AH.RaiseException_def(es2)

          LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
        })),
      ("Array.constructor" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val v_1 = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val es1 = v_1._1._4 match {
            case UInt => ExceptionBot
            case UIntSingle(_) => ExceptionBot
            case NumBot => ExceptionBot
            case _ => Set[Exception](RangeError)
          }
          val es2 = n_arglen match {
            case UIntSingle(n) if n == 1 => es1
            case UIntSingle(n) if n != 1 => ExceptionBot
            case NumBot => ExceptionBot
            case _ => es1
          }
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) =>
            AH.NewArrayObject_def.foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
          val LP2 = AH.RaiseException_def(es2)

          LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
        })),

      ("Array.isArray" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Array.prototype.toString" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      "Array.prototype.toLocaleString" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_this = h(SinglePureLocalLoc)("@this")._1._2

          // Get a new address
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          val set_addr1 = set_addr.foldLeft(LocSetBot)((S, addr_env) => S + cfg.getAPIAddress(addr_env, 0))
          val set_addr2 = set_addr.foldLeft(LocSetBot)((S, addr_env) => S + cfg.getAPIAddress(addr_env, 1))

          // 1. Let array be the result of calling ToObject passing the this value as the argument.
          val (v_this2, h_1, ctx_1, es_1) = set_addr1.foldLeft((ValueBot, HeapBot, ContextBot, ExceptionBot))((S, addr1) => {
            val (v, h_1, c, es) = Helper.toObject(h, ctx, v_this, addr1)
            (S._1 + v, S._2 + h_1, S._3 + c, S._4 ++ es)
          })
          val lset_this = v_this2._2

          val elements_1 = lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h_1, l, AbsString.NumTop))
          val es_2 = Set[Exception](TypeError)

          val es = es_1 ++ es_2

          val LP_1 = set_addr1.foldLeft(LPBot)((S, addr1) => S ++ AH.toObject_def(h, ctx, v_this, addr1))
          val LP_2 = set_addr2.foldLeft(LPBot)((S, addr2) => AH.toObject_def(h_1, ctx_1, elements_1, addr2))
          val LP_3 = LPSet((SinglePureLocalLoc, "@return"))
          val LP_4 = AH.RaiseException_def(es)

          LP_1 ++ LP_2 ++ LP_3 ++ LP_4
        }),
      ("Array.prototype.concat" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          //val addr_env = set_addr.head
          //val addr1 = cfg.getAPIAddress(addr_env, 0)
          //val l_r = addrToLoc(addr1, Recent)
          //val LP1 = AH.Oldify_def(h,ctx,addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.Oldify_def(h,ctx,cfg.getAPIAddress(a, 0)))
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val o = n_arglen match {
            case NumBot => ObjBot
            case UIntSingle(n_arg) => {
              val elem_list = (0 until n_arg.toInt).foldLeft[List[Value]](List(Value(lset_this)))((list, i) =>
                list :+ getArgValue(h, ctx, args, i.toString))
              val obj = Helper.NewArrayObject(AbsNumber.alpha(0))
              val index = AbsNumber.alpha(0)
              val (obj_1, len) = elem_list.foldLeft((obj, index))((oi, elem) => {
                val lset_array = elem._2.filter((l) => AbsString.alpha("Array") <= h(l)("@class")._1._2._1._5)
                val lset_narray = elem._2.filter((l) => AbsString.alpha("Array") != h(l)("@class")._1._2._1._5)
                val v_narray = Value(elem._1, lset_narray)
                val o = oi._1
                val index = oi._2
                val (o_1, n_index_1) =
                  if (!lset_array.isEmpty) {
                    lset_array.foldLeft[(Obj,AbsNumber)]((ObjBot, NumBot))((_oi, l) => {
                      val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                      val __o = n_len match {
                        case UIntSingle(n) => {
                          (0 until n.toInt).foldLeft(o)((o_new, i)=>
                            o_new.update(Helper.toString(Operator.bopPlus(Value(index), Value(AbsNumber.alpha(i)))._1),
                              PropValue(ObjectValue(Helper.Proto(h, l, AbsString.alpha(i.toString)),BoolTrue,BoolTrue,BoolTrue))))
                        }
                        case NumBot => ObjBot
                        case _ =>
                          val v_all = Helper.Proto(h, l, NumStr)
                          o.update(NumStr, PropValue(ObjectValue(v_all,BoolTrue,BoolTrue,BoolTrue)))
                      }
                      val __i = Operator.bopPlus(Value(index), Value(n_len))._1._4
                      (_oi._1 + __o , _oi._2 + __i)
                    })
                  }
                  else
                    (ObjBot, NumBot)
                val (o_2, n_index_2) =
                  if (v_narray </ ValueBot) {
                    val _o = o.update(Helper.toString(PValue(index)), PropValue(ObjectValue(elem, BoolTrue, BoolTrue, BoolTrue)))
                    val _i = Operator.bopPlus(Value(index), Value(AbsNumber.alpha(1)))._1._4
                    (_o, _i)
                  }
                  else
                    (ObjBot, NumBot)
                (o_1 + o_2, n_index_1 + n_index_2)})
              obj_1.update("length", PropValue(ObjectValue(Value(len), BoolTrue, BoolFalse, BoolFalse)))
            }
            case _ => {
              val v_all = Value(lset_this) + getArgValueAbs(h, ctx, args, NumStr)
              val lset_array = v_all._2.filter((l) => AbsString.alpha("Array") <= h(l)("@class")._1._2._1._5)
              val v_array = lset_array.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h, l, NumStr))
              Helper.NewArrayObject(UInt).update(NumStr, PropValue(ObjectValue(v_all + v_array, BoolTrue,BoolTrue,BoolTrue)))
            }
          }

          val LP2 = o.map.keySet.foldLeft(LPBot)((lpset, x) =>
            if (!x.take(1).equals("@") && AbsString.alpha(x) <= NumStr)
              //lpset +  (l_r, x)
              lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent), x))
            //else lpset) + (l_r, "@default_number")
            else lpset) ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent), "@default_number"))
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.join" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Array.prototype.pop" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => {
            val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            val LP = n_len match {
              case UIntSingle(n) if n == 0 =>
                AH.PropStore_def(h, l, AbsString.alpha("length"))
              case UIntSingle(n) if n > 0 => {
                val _LP1 = AH.Delete_def(h, l, AbsString.alpha((n-1).toInt.toString))
                val _LP2 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                _LP1 ++ _LP2
              }
              case NumBot => LPBot
              case _ => {
                val _LP1 = AH.Delete_def(h, l, NumStr)
                val _LP2 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                _LP1 ++ _LP2
              }
            }
            lpset ++ LP
          })

          LP1 + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.push" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))

          val LP1 = n_arglen match {
            case NumBot => LPBot
            case UIntSingle(n_arg) => {
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val _LP = n_len match {
                  case NumBot => LPBot
                  case UIntSingle(n) => {
                    val __LP1 =
                      (0 until n_arg.toInt).foldLeft(LPBot)((lpset, i) =>
                        lpset ++ AH.PropStore_def(h, l, AbsString.alpha((i+n).toInt.toString)))
                    val __LP2 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                    __LP1 ++ __LP2
                  }
                  case _ =>
                    AH.PropStore_def(h, l, NumStr)
                }
                lpset ++ _LP
              })
            }
            case _ =>
              lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.PropStore_def(h, l, NumStr))
          }
          LP1 + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.reverse" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP = lset_this.foldLeft(LPBot)((lpset, l) => {
            val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            val _LP = n_len match {
              case UIntSingle(n) => {
                (0 until floor(n/2).toInt).foldLeft(LPBot)((lpset, i) =>{
                  val s_low = AbsString.alpha(i.toString)
                  val s_up = AbsString.alpha((n-i-1).toInt.toString)
                  val b_low = Helper.HasProperty(h, l, s_low)
                  val b_up = Helper.HasProperty(h, l, s_up)
                  val _LP1 =
                    if (BoolTrue <= b_low && BoolTrue <= b_up) {
                      AH.PropStore_def(h, l, s_low) ++ AH.PropStore_def(h, l, s_up)
                    }
                    else  LPBot
                  val _LP2 =
                    if (BoolFalse <= b_low && BoolTrue <= b_up) {
                      AH.PropStore_def(h, l, s_low) ++ AH.Delete_def(h, l, s_up)
                    }
                    else  LPBot
                  val _LP3 =
                    if (BoolTrue <= b_low && BoolFalse <= b_up) {
                      AH.PropStore_def(h, l, s_up) ++ AH.Delete_def(h, l, s_low)
                    }
                    else LPBot
                  lpset ++ _LP1 ++ _LP2 ++ _LP3
                })
              }
              case NumBot => LPBot
              case _ =>
                AH.PropStore_def(h, l, NumStr) ++ AH.Delete_def(h, l, NumStr)
            }
            lpset ++ _LP
          })
          LP + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.shift" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP = lset_this.foldLeft(LPBot)((lpset, l) => {
            val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            val _LP = n_len match {
              case UIntSingle(n) => {
                if (n == 0) {
                  AH.PropStore_def(h, l, AbsString.alpha("length"))
                }
                else {
                  val _LP1 = (1 until n.toInt).foldLeft(LPBot)((_lpset, i) => {
                    val s_from = AbsString.alpha(i.toString)
                    val s_to = AbsString.alpha((i-1).toString)
                    val b = Helper.HasProperty(h, l, s_from)
                    val __LP1 =
                      if (BoolTrue <= b)  AH.PropStore_def(h, l, s_to)
                      else LPBot
                    val __LP2 =
                      if (BoolFalse <= b)  AH.Delete_def(h, l, s_to)
                      else LPBot
                    _lpset ++ __LP1 ++ __LP2
                  })
                  val _LP2 = AH.Delete_def(h, l, AbsString.alpha((n-1).toInt.toString))
                  val _LP3 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                  _LP1 ++ _LP2 ++ _LP3
                }
              }
              case NumBot => LPBot
              case _ => {
                AH.Delete_def(h, l, NumStr) ++ AH.PropStore_def(h, l, AbsString.alpha("length"))
              }
            }
            lpset ++ _LP
          })
          LP + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.slice" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          //val addr_env = set_addr.head
          //val addr1 = cfg.getAPIAddress(addr_env, 0)
          //val l_r = addrToLoc(addr1, Recent)
          //val LP1 = AH.Oldify_def(h, ctx, addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.Oldify_def(h,ctx,cfg.getAPIAddress(a, 0)))

          val n_start = Operator.ToInteger(getArgValue(h, ctx, args, "0"))
          val n_end = Operator.ToInteger(getArgValue(h, ctx, args, "1"))
          val LP2 = (AbsNumber.concretize(n_start), AbsNumber.concretize(n_end)) match {
            case (Some(start), Some(end)) =>
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val _LP = n_len match {
                  case UIntSingle(n) => {
                    val from =
                      if (start < 0) max(n + start, 0).toInt
                      else min(start, n).toInt
                    val to =
                      if (end < 0) max(n + end, 0).toInt
                      else min(end, n).toInt
                    val span = max(to-from, 0)
                    //val _LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) => lpset + (l_r, p))
                    val _LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) =>
                      lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent),p)))
                    //(0 until span).foldLeft(_LP1)((lpset, i) => lpset + (l_r, i.toString))
                    (0 until span).foldLeft(_LP1)((lpset, i) =>
                      lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent), i.toString)))
                  }
                  case NumBot => LPBot
                  case _ =>
                    //AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) => lpset + (l_r, p))
                    AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) =>
                      lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent),p)))
                }
                lpset ++ _LP
              })
            case _ =>
              if (n_start <= NumBot || n_end <= NumBot)
                LPBot
              else {
                lset_this.foldLeft(LPBot)((lpset, l) => {
                  val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                  n_len match {
                    case NumBot => LPBot
                    case _ =>
                      //AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) => lpset + (l_r, p))
                      AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) =>
                        lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent),p)))
                  }
                })
              }
          }
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.splice" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          //val addr_env = set_addr.head
          //val addr1 = cfg.getAPIAddress(addr_env, 0)
          //val l_r = addrToLoc(addr1, Recent)
          //val LP1 = AH.Oldify_def(h, ctx, addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.Oldify_def(h,ctx,cfg.getAPIAddress(a, 0)))

          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val n_start = Operator.ToInteger(getArgValue(h, ctx, args, "0"))
          val n_count = Operator.ToInteger(getArgValue(h, ctx, args, "1"))
          val LP2 = (AbsNumber.concretize(n_start), AbsNumber.concretize(n_count)) match {
            case (Some(start), Some(count)) =>
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val _LP = n_len match {
                  case UIntSingle(n_len) => {
                    val delCount = min(max(count, 0), n_len - start).toInt
                    val o_new = Helper.NewArrayObject(AbsNumber.alpha(delCount))
                    //val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=> _lpset + (l_r, p))
                    val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=>
                      _lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent),p)))
                    //val __LP2 = (0 until delCount).foldLeft(LPBot)((_lpset, i) => _lpset + (l_r, i.toString))
                    val __LP2 = (0 until delCount).foldLeft(LPBot)((_lpset, i) =>
                      _lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent),i.toString)))
                    val __LP3 = n_arglen match {
                      case UIntSingle(n_arglen) => {
                        val add_count = n_arglen.toInt - 2
                        val move_start = start + count
                        if (add_count < count) {
                          val ___LP1 = (move_start.toInt until n_len.toInt).foldLeft(LPBot)((__lpset, i) => {
                            val s_to = AbsString.alpha((i - count+add_count).toInt.toString)
                            __lpset ++ AH.PropStore_def(h, l, s_to) ++ AH.Delete_def(h, l, s_to)
                          })
                          val ___LP2 = (0 until add_count).foldLeft(LPBot)((__lpset, i) =>
                            __lpset ++ AH.PropStore_def(h, l, AbsString.alpha((start + i).toInt.toString)))
                          val new_length = n_len + add_count - count
                          val ___LP3 = (new_length.toInt until n_len.toInt).foldLeft(LPBot)((__lpset, i) =>
                            __lpset ++ AH.Delete_def(h, l, AbsString.alpha(i.toString)))
                          val ___LP4 = AH.PropStore_def(h, l,  AbsString.alpha("length"))
                          ___LP1 ++ ___LP2 ++ ___LP3 ++ ___LP4
                        }
                        else {
                          val ___LP1 = (0 until (n_len-move_start).toInt).foldLeft(LPBot)((__lpset, i) => {
                            val s_to = AbsString.alpha((n_len -1 -i + add_count - count).toInt.toString)
                            __lpset ++ AH.PropStore_def(h, l, s_to) ++ AH.Delete_def(h, l, s_to)
                          })
                          val ___LP2 = (0 until add_count).foldLeft(LPBot)((__lpset, i) =>
                            __lpset ++ AH.PropStore_def(h, l, AbsString.alpha((start + i).toInt.toString)))
                          val new_length = n_len + add_count - count
                          val ___LP3 = AH.PropStore_def(h, l,  AbsString.alpha("length"))
                          ___LP1 ++ ___LP2 ++ ___LP3
                        }
                      }
                      case NumBot => LPBot
                      case _ => AH.PropStore_def(h, l, NumStr) ++ AH.Delete_def(h, l, NumStr)
                    }
                    __LP1 ++ __LP2 ++ __LP3
                  }
                  case NumBot => LPBot
                  case _ =>
                    //val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=> _lpset + (l_r, p))
                    val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=>
                      _lpset ++set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent),p)))
                    val __LP2 = AH.PropStore_def(h, l, NumStr)
                    val __LP3 = AH.Delete_def(h, l, NumStr)
                    __LP1 ++ __LP2 ++ __LP3
                }
                lpset ++ _LP
              })
            case _ =>
              if (n_start <= NumBot || n_count <= NumBot)
                LPBot
              else {
                lset_this.foldLeft(LPBot)((_lpset, l) => {
                  val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                  val _LP = n_len match {
                    case NumBot => LPBot
                    case _ =>
                      //val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=> _lpset + (l_r, p))
                      val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=>
                        _lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent),p)))
                      val __LP2 = AH.PropStore_def(h, l, NumStr)
                      val __LP3 = AH.Delete_def(h, l, NumStr)
                      __LP1 ++ __LP2 ++ __LP3
                  }
                  _lpset ++ _LP
                })
              }
          }
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.unshift" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP = n_arglen match {
            case UIntSingle(n_arglen) => {
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val _LP = n_len match {
                  case UIntSingle(k) => {
                    val __LP1 = (0 until k.toInt).foldLeft(LPBot)((_lpset, i) => {
                      val s_to = AbsString.alpha((k -1 -i +n_arglen).toInt.toString)
                      _lpset ++ AH.PropStore_def(h, l, s_to) ++ AH.Delete_def(h, l, s_to)
                    })
                    val __LP2 = (0 until n_arglen.toInt).foldLeft(LPBot)((_lpset, i) => {
                      _lpset ++ AH.PropStore_def(h, l, AbsString.alpha(i.toString))
                    })
                    val __LP3 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                    __LP1 ++ __LP2 ++ __LP3
                  }
                  case NumBot => LPBot
                  case _ =>
                    AH.PropStore_def(h, l, NumStr) ++ AH.Delete_def(h, l, NumStr)
                }
                lpset ++ _LP
              })
            }
            case NumBot => LPBot
            case _ => {
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val _LP = n_len match {
                  case NumBot => LPBot
                  case _ =>
                    AH.PropStore_def(h, l, NumStr) ++ AH.Delete_def(h, l, NumStr)
                }
                lpset ++ _LP
              })
            }
          }
          LP + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.indexOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("Array.prototype.lastIndexOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      "Array.prototype.reduce.init" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_this = h(SinglePureLocalLoc)("@this")._1._2

          // Get a new address
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)

          val es = Set[Exception](TypeError)

          val LP_1 = AH.toObject_def(h, ctx, v_this, addr1)
          val LP_2 = AH.RaiseException_def(es)

          LP_1 ++ LP_2
        }),
      "Array.prototype.reduce.call" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_this = h(SinglePureLocalLoc)("@this")._1._2

          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head

          val addr1 = cfg.getAPIAddress(addr_env, 1)
          val addr2 = cfg.getAPIAddress(addr_env, 2)
          val addr3 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)

          val es = Set[Exception](TypeError)

          val props = AH.NewArgObject_def ++ Set("0", "1", "2", "3")

          val LP_1 = AH.Oldify_def(h, ctx, addr1)
          val LP_2 = AH.Oldify_def(h_1, ctx_1, addr2)
          val LP_3 = AH.toObject_def(h_2, ctx_2, v_this, addr3)
          val LP_4 = props.foldLeft(LPBot)((S, p) => S + (l_r1, p))
          val LP_5 = LPSet((l_r1, "callee"))
          val LP_6 = AH.RaiseException_def(es)

          LP_1 ++ LP_2 ++ LP_3 ++ LP_4 ++ LP_5 ++ LP_6
        })
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      ("Array" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          //val addr_env = set_addr.head
          //val addr1 = cfg.getAPIAddress(addr_env, 0)
          //val l_r = addrToLoc(addr1, Recent)
          //val LP1 = AH.Oldify_use(h,ctx,addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.Oldify_use(h,ctx,cfg.getAPIAddress(a, 0)))
          val v_1 = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP2 = getArgValue_use(h, ctx, args, "length") ++ getArgValue_use(h, ctx, args, "0")
          val es1 = v_1._1._4 match {
            case UInt => ExceptionBot
            case UIntSingle(_) => ExceptionBot
            case NumBot => ExceptionBot
            case _ => Set[Exception](RangeError)
          }
          val es2 = n_arglen match {
            case UIntSingle(n) if n == 1 => es1
            case UIntSingle(n) if n != 1 => ExceptionBot
            case NumBot => ExceptionBot
            case _ => es1
          }
          val LP3 = n_arglen match {
            case UIntSingle(n) =>
              //(0 until n.toInt).foldLeft(LPBot)((_lpset, i) => _lpset + (l_r, i.toString))
              (0 until n.toInt).foldLeft(LPBot)((_lpset, i) =>
                set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0), Recent), i.toString)))
            case NumBot => LPBot
            case _ =>
              //AH.absPair(h, l_r, NumStr)
              set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.absPair(h, addrToLoc(cfg.getAPIAddress(a, 0), Recent), NumStr))
          }
          val LP4 = AH.RaiseException_use(es2)
          LP1 ++ LP2 ++ LP3 ++ LP4 + ((SinglePureLocalLoc, "@return"))
        })),
      ("Array.constructor" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val v_1 = getArgValue(h, ctx, args, "0")
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP1 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "length")

          val es1 = v_1._1._4 match {
            case UInt => ExceptionBot
            case UIntSingle(_) => ExceptionBot
            case NumBot => ExceptionBot
            case _ => Set[Exception](RangeError)
          }
          val es2 = n_arglen match {
            case UIntSingle(n) if n == 1 => es1
            case UIntSingle(n) if n != 1 => ExceptionBot
            case NumBot => ExceptionBot
            case _ => es1
          }
          val LP2 = n_arglen match {
            case UIntSingle(n) =>
              lset_this.foldLeft(LPBot)((lpset, l) =>
                (0 until n.toInt).foldLeft(lpset)((_lpset, i) => _lpset + (l, i.toString)))
            case NumBot => LPBot
            case _ =>  lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.absPair(h, l, NumStr))
          }
          /* may def */
          val LP3 = lset_this.foldLeft(LPBot)((lpset, l) =>
            AH.NewArrayObject_def.foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))

          val LP4 = AH.RaiseException_use(es2)

          LP1 ++ LP2 ++ LP3 ++ LP4 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("Array.isArray" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val v = getArgValue(h, ctx, args, "0")
          val LP1 = getArgValue_use(h, ctx, args, "0")
          val LP2 = v._2.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
          LP1 ++ LP2 + ((SinglePureLocalLoc, "@return"))
        })),
      ("Array.prototype.toString" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) =>
            _v + Helper.Proto(h, l, AbsString.alpha("length"))))
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h, l, AbsString.alpha("length")))
          val LP2 = n_len match {
            case UIntSingle(n) if n == 0 => LPBot
            case UIntSingle(n) if n > 0 => {
              val _LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset  ++ AH.Proto_use(h, l, AbsString.alpha("0")))
              val _LP2 = (1 until n.toInt).foldLeft(LPBot)((lpset, i) =>{
                lpset ++ lset_this.foldLeft(LPBot)((_lpset, l) => _lpset ++ AH.Proto_use(h, l, AbsString.alpha(i.toString)))
              })
              _LP1 ++ _LP2
            }
            case UInt | NumTop => lset_this.foldLeft(LPBot)((lpset, l) => lpset  ++ AH.Proto_use(h, l, NumStr))
            case _ => LPBot
          }
          LP1 ++ LP2 + ((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
        })),
      "Array.prototype.toLocaleString" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_this = h(SinglePureLocalLoc)("@this")._1._2

          // Get a new address
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          val set_addr1 = set_addr.foldLeft(LocSetBot)((S, addr_env) => S + cfg.getAPIAddress(addr_env, 0))
          val set_addr2 = set_addr.foldLeft(LocSetBot)((S, addr_env) => S + cfg.getAPIAddress(addr_env, 1))

          // 1. Let array be the result of calling ToObject passing the this value as the argument.
          val (v_this2, h_1, ctx_1, es_1) = set_addr1.foldLeft((ValueBot, HeapBot, ContextBot, ExceptionBot))((S, addr1) => {
            val (v, h_1, c, es) = Helper.toObject(h, ctx, v_this, addr1)
            (S._1 + v, S._2 + h_1, S._3 + c, S._4 ++ es)
          })
          val lset_this = v_this2._2

          val elements_1 = lset_this.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h_1, l, AbsString.NumTop))
          val (elements_2, h_2, ctx_2, es_2) = set_addr2.foldLeft((ValueBot, HeapBot, ContextBot, ExceptionBot))((S, addr2) => {
            val (v, _h, c, es) = Helper.toObject(h_1, ctx_1, elements_1, addr2)
            (S._1 + v, S._2 + _h, S._3 + c, S._4 ++ es)
          })
          val func = elements_2._2.foldLeft(ValueBot)((S, l) => S + Helper.Proto(h_2, l, AbsString.alpha("toLocaleString")))
          val es_3 = Set[Exception](TypeError)
          val es = es_1 ++ es_2 ++ es_3

          val LP_1 = LPSet((SinglePureLocalLoc, "@this"))
          val LP_2 = LPSet((SinglePureLocalLoc, "@env"))
          val LP_3 = set_addr1.foldLeft(LPBot)((S, addr1) => S ++ AH.toObject_use(h, ctx, v_this, addr1))
          val LP_4 = lset_this.foldLeft(LPBot)((S, l) => S ++ AH.Proto_use(h, l, AbsString.alpha("length")))
          val LP_5 = lset_this.foldLeft(LPBot)((S, l) => S ++ AH.Proto_use(h, l, AbsString.NumTop))

          val LP_6 = set_addr2.foldLeft(LPBot)((S, addr2) => AH.toObject_use(h_1, ctx_1, elements_1, addr2))
          val LP_7 = elements_2._2.foldLeft(LPBot)((S, l) => S ++ AH.Proto_use(h_2, l, AbsString.alpha("toLocaleString")))
          val LP_8 = func._2.foldLeft(LPBot)((S, l) => S ++ AH.IsCallable_use(h, l))
          val LP_9 = LPSet((SinglePureLocalLoc, "@return"))
          val LP_10 = AH.RaiseException_use(es)

          LP_1 ++ LP_2 ++ LP_3 ++ LP_4 ++ LP_5 ++ LP_6 ++ LP_7 ++ LP_8 ++ LP_9 ++ LP_10
        }),
      ("Array.prototype.concat" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          //val addr_env = set_addr.head
          //val addr1 = cfg.getAPIAddress(addr_env, 0)
          //val LP1 = AH.Oldify_use(h, ctx, addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.Oldify_use(h,ctx,cfg.getAPIAddress(a, 0)))
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP2 = getArgValue_use(h, ctx, args, "length")

          val LP3_this =
            lset_this.foldLeft(LPBot)((lpset, l) =>
              lpset ++ AH.Proto_use(h,l, NumStr) ++ AH.Proto_use(h,l,AbsString.alpha("length")) + (l, "@class")) + (SinglePureLocalLoc, "@this")

          val LP3_arg = n_arglen match {
            case NumBot => LPBot
            case UIntSingle(n_arg) => {
              (0 until n_arg.toInt).foldLeft(LPBot)((lpset, i) => {
                val v = getArgValue(h, ctx, args, i.toString)
                val _LP1 = getArgValue_use(h, ctx, args, i.toString)
                val _LP2 = v._2.foldLeft(LPBot)((lpset, l) =>
                  lpset ++ AH.Proto_use(h,l, NumStr) ++ AH.Proto_use(h,l,AbsString.alpha("length")) + (l, "@class"))
                lpset ++ _LP1 ++ _LP2
              })
            }
            case _ => {
              val v_all = getArgValueAbs(h, ctx, args, NumStr)
              val _LP1 = getArgValueAbs_use(h, ctx, args, NumStr)
              val _LP2 = v_all._2.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h, l, NumStr) + (l, "@class"))
              _LP1 ++ _LP2
            }
          }

          LP1 ++ LP2 ++ LP3_this ++ LP3_arg + ((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
        })),
      ("Array.prototype.join" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "0")

          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) =>
            _v + Helper.Proto(h, l, AbsString.alpha("length"))))
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h, l,  AbsString.alpha("length")))

          val LP3 = n_len match {
            case UIntSingle(n) if n > 0 => {
              val _LP1 = lset_this.foldLeft(LPBot)((_lpset, l) => _lpset ++ AH.Proto_use(h, l, AbsString.alpha("0")))
              val _LP2 = (1 until n.toInt).foldLeft(LPBot)((_lpset, i) =>
                _lpset ++ lset_this.foldLeft(LPBot)((__lpset, l) =>
                  __lpset ++ AH.Proto_use(h, l, AbsString.alpha(i.toString))))
              _LP1 ++ _LP2
            }
            case _ => LPBot
          }
          LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("Array.prototype.pop" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_len = Operator.ToUInt32(lset_this.foldLeft(ValueBot)((_v, l) =>
            _v + Helper.Proto(h, l, AbsString.alpha("length"))))
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h, l, AbsString.alpha("length")))

          val LP2 =  lset_this.foldLeft(LPBot)((lpset, l) => {
            val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
            val _LP2 = n_len match {
              case UIntSingle(n) if n == 0 =>
                AH.PropStore_use(h, l, AbsString.alpha("length")) ++
                  /* may def */
                  AH.PropStore_def(h, l, AbsString.alpha("length"))
              case UIntSingle(n) if n > 0 => {
                val __LP1 = AH.Proto_use(h, l, AbsString.alpha((n-1).toInt.toString))
                val __LP2 = AH.Delete_use(h, l, AbsString.alpha((n-1).toInt.toString))
                val __LP3 = AH.PropStore_use(h, l,  AbsString.alpha("length"))
                /* may def */
                val __LP4 = AH.Delete_def(h, l, AbsString.alpha((n-1).toInt.toString))
                val __LP5 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5
              }
              case NumBot => LPBot
              case _ => {
                val __LP1 = AH.Proto_use(h, l, NumStr)
                val __LP2 = AH.Delete_use(h, l, NumStr)
                val __LP3 = AH.PropStore_use(h, l,  AbsString.alpha("length"))
                /* may def */
                val __LP4 = AH.Delete_def(h, l, NumStr)
                val __LP5 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5
              }
            }
            lpset ++ _LP1 ++ _LP2
          })
          LP1 ++ LP2 + ((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
        })),
      ("Array.prototype.push" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP1 = getArgValue_use(h, ctx, args, "length")

          val LP2 = n_arglen match {
            case NumBot => LPBot
            case UIntSingle(n_arg) => {
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                val _LP2 = n_len match {
                  case NumBot => LPBot
                  case UIntSingle(n) => {
                    val __LP1 = (0 until n_arg.toInt).foldLeft(LPBot)((_lpset, i) =>
                      _lpset ++
                        AH.PropStore_use(h, l, AbsString.alpha((i+n).toInt.toString)) ++
                        /* may def */
                        AH.PropStore_def(h, l, AbsString.alpha((i+n).toInt.toString)) ++
                        getArgValue_use(h, ctx, args, (i.toString)))
                    val __LP2 = AH.PropStore_use(h, l, AbsString.alpha("length"))
                    /* may def */
                    val __LP3 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                    __LP1 ++ __LP2 ++ __LP3
                  }
                  case _ => {
                    val __LP1 = getArgValueAbs_use(h, ctx, args, NumStr)
                    val __LP2 = AH.PropStore_use(h, l, NumStr)
                    /* may def */
                    val __LP3 = AH.PropStore_def(h, l, NumStr)
                    __LP1 ++ __LP2 ++ __LP3
                  }
                }
                lpset ++ _LP1 ++ _LP2
              })
            }
            case _ => {
              val _LP1 = getArgValueAbs_use(h, ctx, args, NumStr)
              val _LP2 = lset_this.foldLeft(LPBot)((lpset, l) =>
                lpset ++ AH.PropStore_use(h, l, NumStr) ++
                  /* may def */
                  AH.PropStore_def(h, l, NumStr))
              _LP1 ++ _LP2
            }
          }
          LP1 ++ LP2 + ((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
        })),
      ("Array.prototype.reverse" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => {
            val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
            val _LP2 = n_len match {
              case UIntSingle(n) => {
                (0 until floor(n/2).toInt).foldLeft(LPBot)((_lpset, i) =>{
                  val s_low = AbsString.alpha(i.toString)
                  val s_up = AbsString.alpha((n-i-1).toInt.toString)
                  val v_low = Helper.Proto(h, l, s_low)
                  val __LP1 = AH.Proto_use(h, l, s_low)
                  val v_up = Helper.Proto(h, l, s_up)
                  val __LP2 = AH.Proto_use(h, l, s_up)

                  val b_low = Helper.HasProperty(h, l, s_low)
                  val b_up = Helper.HasProperty(h, l, s_up)
                  val __LP3 =
                    if (BoolTrue <= b_low && BoolTrue <= b_up) {
                      AH.PropStore_use(h, l, s_low) ++ AH.PropStore_use(h, l, s_up) ++
                        /* may def */
                        AH.PropStore_def(h, l, s_low) ++ AH.PropStore_def(h, l, s_up)
                    }
                    else  LPBot
                  val __LP4 =
                    if (BoolFalse <= b_low && BoolTrue <= b_up) {
                      AH.PropStore_use(h, l, s_low) ++ AH.Delete_use(h, l, s_up) ++
                        /* may def */
                        AH.PropStore_def(h, l, s_low) ++ AH.Delete_def(h, l, s_up)
                    }
                    else LPBot
                  val __LP5 =
                    if (BoolTrue <= b_low && BoolFalse <= b_up) {
                      AH.PropStore_def(h, l, s_up) ++ AH.Delete_def(h, l, s_low) ++
                        /* may def */
                        AH.PropStore_def(h, l, s_up) ++ AH.Delete_def(h, l, s_low)
                    }
                    else  LPBot
                  __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5
                })
              }
              case NumBot => LPBot
              case _ =>
                val __LP1 = AH.Proto_use(h, l, NumStr)
                val __LP2 = AH.PropStore_use(h, l, NumStr)
                val __LP3 = AH.Delete_use(h, l, NumStr)
                /* may def */
                val __LP4 = AH.PropStore_def(h, l, NumStr)
                val __LP5 = AH.Delete_def(h, l, NumStr)
                __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5
            }
            lpset ++ _LP1 ++ _LP2
          })
          LP1 + ((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
        })),
      ("Array.prototype.shift" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => {
            val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
            val _LP2 = n_len match {
              case UIntSingle(n) => {
                if (n == 0) {
                  AH.PropStore_use(h, l, AbsString.alpha("length")) ++
                    AH.PropStore_def(h, l, AbsString.alpha("length"))
                }
                else {
                  val __LP1 = AH.Proto_use(h, l, AbsString.alpha("0"))
                  val __LP2 = (1 until n.toInt).foldLeft(LPBot)((_lpset, i) => {
                    val s_from = AbsString.alpha(i.toString)
                    val s_to = AbsString.alpha((i-1).toString)
                    val b = Helper.HasProperty(h, l, s_from)
                    val ___LP1 =
                      if (BoolTrue <= b)
                        AH.PropStore_use(h, l, s_to) ++ AH.Proto_use(h, l, s_from) ++
                          /* may def */
                          AH.PropStore_def(h, l, s_to)
                      else LPBot
                    val ___LP2 =
                      if (BoolFalse <= b)
                        AH.Delete_use(h, l, s_to) ++
                          /* may def */
                          AH.Delete_use(h, l, s_to)
                      else LPBot
                    _lpset ++ ___LP1 ++ ___LP2
                  })
                  val __LP3 = AH.Delete_use(h, l, AbsString.alpha((n-1).toInt.toString))
                  val __LP4 = AH.PropStore_use(h, l, AbsString.alpha("length"))
                  /* may def */
                  val __LP5 = AH.Delete_def(h, l, AbsString.alpha((n-1).toInt.toString))
                  val __LP6 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                  __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5 ++ __LP6
                }
              }
              case NumBot => LPBot
              case _ => {
                val __LP1 = AH.Proto_use(h, l, NumStr)
                val __LP2 = AH.Delete_use(h, l, NumStr)
                val __LP3 = AH.PropStore_use(h, l, AbsString.alpha("length"))
                val __LP4 = AH.Delete_def(h, l, NumStr)
                val __LP5 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5
              }
            }
            lpset ++ _LP1 ++ _LP2
          })
          LP1 + ((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
        })),
      ("Array.prototype.slice" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          //val addr_env = set_addr.head
          //val addr1 = cfg.getAPIAddress(addr_env, 0)
          //val LP1 = AH.Oldify_use(h, ctx, addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.Oldify_use(h,ctx,cfg.getAPIAddress(a, 0)))

          val n_start = Operator.ToInteger(getArgValue(h, ctx, args, "0"))
          val n_end = Operator.ToInteger(getArgValue(h, ctx, args, "1"))
          val LP2 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")

          val LP3 = (AbsNumber.concretize(n_start), AbsNumber.concretize(n_end)) match {
            case (Some(start), Some(end)) =>
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                val _LP2 = n_len match {
                  case UIntSingle(n) => {
                    val from =
                      if (start < 0) max(n + start, 0).toInt
                      else min(start, n).toInt
                    val to =
                      if (end < 0) max(n + end, 0).toInt
                      else min(end, n).toInt
                    val span = max(to-from, 0)
                    (0 until span).foldLeft(LPBot)((_lpset, i) => {
                      val b = Helper.HasProperty(h, l, AbsString.alpha(i.toString))
                      if (BoolTrue <= b)
                        _lpset ++ AH.Proto_use(h, l,  AbsString.alpha((from+i).toString))
                      else _lpset
                    })
                  }
                  case NumBot => LPBot
                  case _ => AH.Proto_use(h, l, NumStr)
                }
                lpset ++ _LP1 ++ _LP2
              })
            case _ =>
              if (n_start <= NumBot || n_end <= NumBot)
                LPBot
              else {
                lset_this.foldLeft(LPBot)((lpset, l) => {
                  val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                  val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                  val _LP2 = n_len match {
                    case NumBot => LPBot
                    case _ => AH.Proto_use(h, l, NumStr)
                  }
                  lpset ++ _LP1 ++ _LP2
                })
              }
          }
          LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.splice" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          //val addr_env = set_addr.head
          //val addr1 = cfg.getAPIAddress(addr_env, 0)

          //val LP1 = AH.Oldify_use(h, ctx, addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) => lp ++ AH.Oldify_use(h,ctx,cfg.getAPIAddress(a, 0)))

          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val n_start = Operator.ToInteger(getArgValue(h, ctx, args, "0"))
          val n_count = Operator.ToInteger(getArgValue(h, ctx, args, "1"))
          val LP2 = getArgValue_use(h, ctx, args, "length") ++ getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")
          val LP3 = (AbsNumber.concretize(n_start), AbsNumber.concretize(n_count)) match {
            case (Some(start), Some(count)) =>
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                val _LP2 = n_len match {
                  case UIntSingle(n_len) => {
                    val from =
                      if (start < 0) max(n_len + start, 0).toInt
                      else min(start, n_len).toInt
                    val delCount = min(max(count, 0), n_len - start).toInt
                    val __LP1 = (0 until delCount).foldLeft(LPBot)((_lpset, i) => {
                      val b = Helper.HasProperty(h, l, AbsString.alpha(i.toString))
                      if (BoolTrue <= b)
                        _lpset ++ AH.Proto_use(h, l,AbsString.alpha((from+i).toString))
                      else _lpset
                    })
                    val __LP2 = n_arglen match {
                      case UIntSingle(n_arglen) => {
                        val add_count = n_arglen.toInt - 2
                        val move_start = start + count
                        if (add_count < count) {
                          val ___LP1 = (move_start.toInt until n_len.toInt).foldLeft(LPBot)((__lpset, i) => {
                            val s_from = AbsString.alpha(i.toString)
                            val s_to = AbsString.alpha((i - count+add_count).toInt.toString)
                            val ____LP1 = AH.Proto_use(h, l, s_from)
                            val b = Helper.HasProperty(h, l, s_from)
                            val ____LP2 =
                              if (BoolTrue <= b)
                                AH.PropStore_use(h, l, s_to) ++
                                  /* may def */
                                  AH.PropStore_def(h, l, s_to)
                              else LPBot
                            val ____LP3 =
                              if (BoolFalse <= b)
                                AH.Delete_use(h, l, s_to) ++
                                  /* may def */
                                  AH.Delete_def(h, l, s_to)
                              else LPBot
                            __lpset ++ ____LP1 ++ ____LP2 ++ ____LP3
                          })
                          val ___LP2 = (0 until add_count).foldLeft(LPBot)((__lpset, i) => {
                            val lpset1 = AH.PropStore_use(h, l, AbsString.alpha((start + i).toInt.toString)) ++
                              /* may def */
                              AH.PropStore_def(h, l, AbsString.alpha((start + i).toInt.toString))
                            val lpset2 = getArgValue_use(h, ctx, args, (i+2).toString)
                            __lpset ++ lpset1 ++ lpset2 })
                          val new_length = n_len + add_count - count
                          val ___LP3 = (new_length.toInt until n_len.toInt).foldLeft(LPBot)((__lpset, i) =>
                            __lpset ++ AH.Delete_use(h, l, AbsString.alpha(i.toString)) ++
                              /* may def */
                              AH.Delete_def(h, l, AbsString.alpha(i.toString)) )
                          val ___LP4 = AH.PropStore_use(h, l,  AbsString.alpha("length")) ++
                            /* may def */
                            AH.PropStore_def(h, l,  AbsString.alpha("length"))
                          ___LP1 ++ ___LP2 ++ ___LP3 ++ ___LP4
                        }
                        else {
                          val ___LP1 = (0 until (n_len-move_start).toInt).foldLeft(LPBot)((__lpset, i) => {
                            val s_from = AbsString.alpha((n_len -1 - i).toInt.toString)
                            val s_to = AbsString.alpha((n_len -1 -i + add_count - count).toInt.toString)
                            val lpset1 = AH.Proto_use(h, l, s_from)
                            val b = Helper.HasProperty(h, l, s_from)
                            val lpset2 =
                              if (BoolTrue <= b)
                                AH.PropStore_use(h, l, s_to) ++
                                  /* may def */
                                  AH.PropStore_def(h, l, s_to)
                              else LPBot
                            val lpset3 =
                              if (BoolFalse <= b)
                                AH.Delete_use(h, l, s_to) ++
                                  /* may def */
                                  AH.Delete_def(h, l, s_to)
                              else LPBot
                            __lpset ++ lpset1 ++ lpset2 ++ lpset3
                          })
                          val ___LP2 = (0 until add_count).foldLeft(LPBot)((__lpset, i) => {
                            val lpset1 = AH.PropStore_use(h, l, AbsString.alpha((start + i).toInt.toString)) ++
                              /* may def */
                              AH.PropStore_def(h, l, AbsString.alpha((start + i).toInt.toString))
                            val lpset2 = getArgValue_use(h, ctx, args, (i+2).toString)
                            __lpset ++ lpset1 ++ lpset2
                          })
                          val new_length = n_len + add_count - count
                          val ___LP3 = AH.PropStore_use(h, l,  AbsString.alpha("length")) ++
                            /* may def */
                            AH.PropStore_def(h, l,  AbsString.alpha("length"))
                          ___LP1 ++ ___LP2 ++ ___LP3
                        }
                      }
                      case NumBot => LPBot
                      case _ =>
                        val ___LP1 = AH.PropStore_use(h, l, NumStr)
                        val ___LP2 = getArgValueAbs_use(h, ctx, args, NumStr)
                        val ___LP3 = AH.Delete_use(h, l, NumStr)
                        val ___LP4 = AH.PropStore_def(h, l, NumStr)
                        val ___LP5 = AH.Delete_def(h, l, NumStr)
                        ___LP1 ++ ___LP2 ++ ___LP3 ++ ___LP4 ++ ___LP5
                    }
                    __LP1 ++ __LP2
                  }
                  case NumBot => LPBot
                  case _ =>
                    val __LP1 = AH.Proto_use(h, l, NumStr)
                    val __LP2 = AH.PropStore_use(h, l, NumStr)
                    val __LP3 = getArgValueAbs_use(h, ctx, args, NumStr)
                    val __LP4 = AH.Delete_use(h, l, NumStr)
                    val __LP5 = AH.PropStore_def(h, l, NumStr)
                    val __LP6 = AH.Delete_def(h, l, NumStr)
                    __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5 ++ __LP6
                }
                lpset ++ _LP1 ++ _LP2
              })
            case _ =>
              if (n_start <= NumBot || n_count <= NumBot)
                LPBot
              else {
                lset_this.foldLeft(LPBot)((lpset, l) => {
                  val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                  val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                  val _LP2 = n_len match {
                    case NumBot => LPBot
                    case _ =>
                      val __LP1 = AH.Proto_use(h, l, NumStr)
                      val __LP2 = AH.Delete_use(h, l, NumStr)
                      val __LP3 = AH.PropStore_use(h, l, NumStr)
                      val __LP4 = getArgValueAbs_use(h, ctx, args, NumStr)
                      val __LP5 = AH.Delete_def(h, l, NumStr)
                      val __LP6 = AH.PropStore_def(h, l, NumStr)
                      __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5 ++ __LP6
                  }
                  lpset ++ _LP1 ++ _LP2
                })
              }
          }
          LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
        })),
      ("Array.prototype.unshift" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 = n_arglen match {
            case UIntSingle(n_arglen) => {
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val lpset1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                val lpset2 = n_len match {
                  case UIntSingle(k) => {
                    val _LP1 = (0 until k.toInt).foldLeft(LPBot)((lpset, i) => {
                      val s_from = AbsString.alpha((k - 1 - i).toInt.toString)
                      val s_to = AbsString.alpha((k -1 -i +n_arglen).toInt.toString)
                      val b = Helper.HasProperty(h, l, s_from)
                      val lpset1 =
                        if (BoolTrue <= b)
                          AH.Proto_use(h, l, s_from) ++ AH.PropStore_use(h, l, s_to) ++
                            /* may def */
                            AH.PropStore_def(h, l, s_to)
                        else LPBot
                      val lpset2 =
                        if (BoolFalse <= b)
                          AH.Delete_use(h, l, s_to) ++
                            /* may def */
                            AH.Delete_def(h, l, s_to)
                        else LPBot
                      lpset ++ lpset1 ++ lpset2 })
                    val _LP2 = (0 until n_arglen.toInt).foldLeft(LPBot)((lpset, i) => {
                      val lpset1 = getArgValue_use(h, ctx, args, i.toString)
                      val lpset2 = AH.PropStore_use(h, l, AbsString.alpha(i.toString))
                      /* may def */
                      AH.PropStore_def(h, l, AbsString.alpha(i.toString))
                      lpset ++ lpset1 ++ lpset2 })
                    val _LP3 = AH.PropStore_use(h, l, AbsString.alpha("length")) ++
                      /* may def */
                      AH.PropStore_def(h, l, AbsString.alpha("length"))
                    _LP1 ++ _LP2 ++ _LP3
                  }
                  case NumBot => LPBot
                  case _ => {
                    val _LP1 = AH.Proto_use(h, l, NumStr)
                    val _LP2 = AH.PropStore_use(h, l, NumStr)
                    val _LP3 = AH.Delete_use(h, l, NumStr)
                    /* may def */
                    val _LP4 = AH.PropStore_def(h, l, NumStr)
                    val _LP5 = AH.Delete_def(h, l, NumStr)
                    _LP1 ++ _LP2 ++ _LP3 ++ _LP4 ++ _LP5
                  }
                }
                lpset ++ lpset1 ++ lpset2
              })
            }
            case NumBot => LPBot
            case _ => {
              lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val lpset1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                val lpset2 = n_len match {
                  case NumBot => LPBot
                  case _ =>
                    val _LP1 = AH.Proto_use(h, l, NumStr)
                    val _LP2 = AH.PropStore_use(h, l, NumStr)
                    val _LP3 = AH.Delete_use(h, l, NumStr)
                    val _LP4 = AH.PropStore_def(h, l, NumStr)
                    val _LP5 = AH.Delete_def(h, l, NumStr)
                    _LP1 ++ _LP2 ++ _LP3 ++ _LP4 ++ _LP5
                }
                lpset ++ lpset1 ++ lpset2
              })
            }
          }
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("Array.prototype.indexOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")  ++ getArgValue_use(h, ctx, args, "length")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) =>{
            val len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            lpset ++ (len match {
              case UIntSingle(n) =>
                (0 until n.toInt).foldLeft(LPBot)((_lpset, i) => _lpset ++ AH.Proto_use(h,l,AbsString.alpha(i.toString)))
              case NumBot => LPBot
              case _ => AH.Proto_use(h, l, NumStr)
            })})
          /*
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
          val LP1 = getArgValue_use(h, ctx, "length")
          val LP2 = n_arglen match {
            case UIntSingle(n) => {
              val v_search = getArgValue(h, ctx, "0")
              val _LP1 = getArgValue_use(h, ctx, "0")
              val _LP2 = lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val lpset1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                val lpset2 = n_len match {
                  case UIntSingle(n_len) => {
                    if (n_len == 0)
                      LPBot
                    else {
                      val start =
                        if (n > 1) Operator.ToInteger(getArgValue(h, ctx, "1"))
                        else AbsNumber.alpha(0)
                      val __LP1 =
                        if (n > 1) getArgValue_use(h, ctx, "1")
                        else LPBot
                      val __LP2 = AbsNumber.concretize(start) match {
                        case Some(n_start) =>
                          val k =
                          if (n_start >= 0) min(n_start, n_len-1)
                          else (n_len - abs(n_start))
                          (0 until k.toInt).foldLeft(LPBot)((_lpset, i) =>
                            _lpset ++ AH.Proto_use(h, l, AbsString.alpha(i.toInt.toString)))
                        case None => LPBot
                      }
                      __LP1 ++ __LP2
                    }
                  }
                  case NumBot => LPBot
                  case _ => LPBot
                }
                lpset ++ lpset1 ++ lpset2 })
              _LP1 ++ _LP2
            }
            case _ => LPBot
          }*/
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("Array.prototype.lastIndexOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")  ++ getArgValue_use(h, ctx, args, "length")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) =>{
            val len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
            lpset ++ (len match {
              case UIntSingle(n) =>
                (0 until n.toInt).foldLeft(LPBot)((_lpset, i) => _lpset ++ AH.Proto_use(h,l,AbsString.alpha(i.toString)))
              case NumBot => LPBot
              case _ => AH.Proto_use(h, l, NumStr)
            })})
          /*
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
          val LP1 = getArgValue_use(h, ctx, "length")
          val LP2 = n_arglen match {
            case UIntSingle(n) => {
              val v_search = getArgValue(h, ctx, "0")
              val _LP1 = getArgValue_use(h, ctx, "0")
              val _LP2 = lset_this.foldLeft(LPBot)((lpset, l) => {
                val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
                val lpset1 = AH.Proto_use(h, l, AbsString.alpha("length"))
                val lpset2 = n_len match {
                  case UIntSingle(n_len) => {
                    val start =
                      if (n > 1) Operator.ToInteger(getArgValue(h, ctx, "1"))
                      else AbsNumber.alpha(n_len - 1)
                    val __LP1 = getArgValue_use(h, ctx, "1")
                    val __LP2 = AbsNumber.concretize(start) match {
                        case Some(n_start) =>
                          val k =
                          if (n_start >= 0) min(n_start, n_len-1)
                          else (n_len - abs(n_start))
                          (0 until k.toInt).foldLeft(LPBot)((_lpset, i) =>
                            _lpset ++ AH.Proto_use(h, l, AbsString.alpha(i.toInt.toString)))
                        case None => LPBot
                      }
                    __LP1 ++ __LP2
                  }
                  case _ => LPBot
                }
                lpset ++ lpset1 ++ lpset2  })
              _LP1 ++ _LP2
            }
            case _ => LPBot
          }
          */
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      "Array.prototype.reduce.init" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_this = h(SinglePureLocalLoc)("@this")._1._2
          val v_callbackfn = getArgValue(h, ctx, args, "0")

          // Get a new address
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)

          // 1. Let O be the result of calling ToObject passing the this value as the argument.
          val (v_this2, h_1, _, _) = Helper.toObject(h, ctx, v_this, addr1)
          val lset_this = v_this2._2

          val es = Set[Exception](TypeError)

          val LP_1 = LPSet((SinglePureLocalLoc, "@this"))
          val LP_2 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")
          val LP_3 = getArgValue_use(h, ctx, args, "length")
          val LP_4 = LPSet((SinglePureLocalLoc, "@env"))
          val LP_5 = AH.toObject_use(h, ctx, v_this, addr1)
          val LP_6 = lset_this.foldLeft(LPBot)((S, l) => S ++ AH.Proto_use(h_1, l, AbsString.alpha("length)")))
          val LP_7 = lset_this.foldLeft(LPBot)((S, l) => S ++ AH.Proto_use(h_1, l, AbsString.NumTop))
          val LP_8 = AH.IsCallable_use(h_1, v_callbackfn)
          val LP_9 = AH.RaiseException_use(es)

          LP_1 ++ LP_2 ++ LP_3 ++ LP_4 ++ LP_5 ++ LP_6 ++ LP_7 ++ LP_8 ++ LP_9
        }),
      "Array.prototype.reduce.call" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_this = h(SinglePureLocalLoc)("@this")._1._2

          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head

          val v_callbackfn = getArgValue(h, ctx, args, "0")

          val addr1 = cfg.getAPIAddress(addr_env, 1)
          val addr2 = cfg.getAPIAddress(addr_env, 2)
          val addr3 = cfg.getAPIAddress(addr_env, 0)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)

          val (v_this2, h_3, _, _) = Helper.toObject(h_2, ctx_2, v_this, addr3)
          val lset_this = v_this2._2

          val es = Set[Exception](TypeError)

          val LP_1 = LPSet((SinglePureLocalLoc, "@this"))
          val LP_2 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")
          val LP_3 = getArgValue_use(h, ctx, args, "length")
          val LP_4 = LPSet((SinglePureLocalLoc, "@env"))
          val LP_5 = AH.Oldify_use(h, ctx, addr1)
          val LP_6 = AH.Oldify_use(h_1, ctx_1, addr2)
          val LP_7 = AH.toObject_use(h_2, ctx_2, v_this, addr3)
          val LP_8 = lset_this.foldLeft(LPBot)((S, l) => S ++ AH.Proto_use(h_1, l, AbsString.alpha("length)")))
          val LP_9 = lset_this.foldLeft(LPBot)((S, l) => S ++ AH.Proto_use(h_1, l, AbsString.NumTop))
          val LP_10 = AH.IsCallable_use(h_3, v_callbackfn)
          val LP_11 = AH.RaiseException_use(es)
          val LP_12 = LPSet((SinglePureLocalLoc, "temp"))

          LP_1 ++ LP_2 ++ LP_3 ++ LP_4 ++ LP_5 ++ LP_6 ++ LP_7 ++ LP_8 ++ LP_9 ++ LP_10 ++ LP_11 ++ LP_12
        })
    )
  }
}
