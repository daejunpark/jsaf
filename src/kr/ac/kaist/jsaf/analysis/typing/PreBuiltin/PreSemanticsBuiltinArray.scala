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

object PreSemanticsBuiltinArray {

  def builtinArray(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr, addr1: Address): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1
    def getArgValueAbs(h : Heap, ctx: Context, s : AbsString): Value = {
      val lset = SE.V(args,h,ctx, PureLocalLoc)._1._2
      val v = lset.foldLeft(ValueBot)((v_1, l) => v_1 + PreHelper.Proto(h,l,s))
      v
    }
    val lset_this = h(PureLocalLoc)("@this")._1._2._2

    fun match {
      case "Array" => {
        val l_r = addrToLoc(addr1, Recent)
        val (h_1, ctx_1)  = PreHelper.Oldify(h, ctx, addr1)
        val v_1 = getArgValue(h_1, ctx_1, "0")
        val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, "length"))

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
                _o.update(i.toString, PropValue(ObjectValue(getArgValue(h_1, ctx_1, i.toString), BoolTrue, BoolTrue, BoolTrue))))
                (h_1.update(l_r, o), ExceptionBot)
            }
            case NumBot => (h_1, ExceptionBot)
            case _ => {
              val o = PreHelper.NewArrayObject(UInt).
                update(NumStr, PropValue(ObjectValue(getArgValueAbs(h_1, ctx_1, NumStr),BoolTrue,BoolTrue,BoolTrue)))
              val h_uint = h_1.update(l_r, o)
              (h_arg_1 + h_uint, es1)
            }
        }

        val (h_e, ctx_e) = PreHelper.RaiseException(h_2, ctx, PureLocalLoc, es2)

        if (!(h_e <= HeapBot))
          ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(l_r)), ctx_e), (he + h_e, ctxe + ctx_e))
        else
          ((h_e, ctx_e), (he + h_e, ctxe + ctx_e))
      }
      case "Array.constructor" => {
        val v_1 = getArgValue(h, ctx, "0")
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))

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
                _o.update(i.toString, PropValue(ObjectValue(getArgValue(h, ctx, i.toString), BoolTrue, BoolTrue, BoolTrue))))
              (lset_this.foldLeft(h)((_h,l) => _h.update(l, o)), ExceptionBot)
            }
            case NumBot => (HeapBot, ExceptionBot)
            case _ => {
              val o = PreHelper.NewArrayObject(UInt).
                update(NumStr, PropValue(ObjectValue(getArgValueAbs(h, ctx, NumStr),BoolTrue,BoolTrue,BoolTrue)))
              val h_uint = lset_this.foldLeft(h)((_h,l) => _h.update(l, o))
              (h_arg_1 + h_uint, es1)
            }
          }

        val (h_e, ctx_e) = PreHelper.RaiseException(h_2, ctx, PureLocalLoc, es2)

        if (!(h_e <= HeapBot))
          ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(lset_this)), ctx_e), (he + h_e, ctxe + ctx_e))
        else
          ((h_e, ctx_e), (he + h_e, ctxe + ctx_e))
      }
      case "Array.isArray" => {
        val v = getArgValue(h, ctx, "0")
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
      }
      case "Array.prototype.toString" |
           "Array.prototype.toLocaleString" => {
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
      }
      case "Array.prototype.concat" => {
        val l_r = addrToLoc(addr1, Recent)
        val (h_1, ctx_1)  = PreHelper.Oldify(h, ctx, addr1)
        val lset_this = h_1(PureLocalLoc)("@this")._1._2._2

        val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, "length"))

        val o = n_arglen match {
          case NumBot => ObjBot
          case UIntSingle(n_arg) => {
            val elem_list = (0 until n_arg.toInt).foldLeft[List[Value]](List(Value(lset_this)))((list, i) =>
              list :+ getArgValue(h_1, ctx_1, i.toString))
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
            val v_all = Value(lset_this) + getArgValueAbs(h_1, ctx_1, NumStr)
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
      }
      case "Array.prototype.join" => {
        val v_sep = getArgValue(h, ctx, "0")
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
      }
      case "Array.prototype.pop" => {

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
      }
      case "Array.prototype.push" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
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
                      PreHelper.PropStore(__h, l, AbsString.alpha((i+n).toInt.toString), getArgValue(__h, ctx, (i.toString)))
                    })
                    val _v = Value(AbsNumber.alpha(n_arg+n))
                    val _h1 = PreHelper.PropStore(_h, l, AbsString.alpha("length"), _v)
                    (_h1, hv._2 + _v)
                  }
                  case _ => {
                    val v_argall = getArgValueAbs(hv._1, ctx, NumStr)
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
            val v_argall = getArgValueAbs(h, ctx, NumStr)
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
      }
      case "Array.prototype.reverse" => {
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
      }
      case "Array.prototype.shift" => {
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
      }
      case "Array.prototype.slice" => {
        val l_r = addrToLoc(addr1, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
        val lset_this = h_1(PureLocalLoc)("@this")._1._2._2
        val n_start = Operator.ToInteger(getArgValue(h_1, ctx_1, "0"))
        val v_end = getArgValue(h, ctx, "1")

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
      }
      case "Array.prototype.splice" => {
        val l_r = addrToLoc(addr1, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
        val lset_this = h_1(PureLocalLoc)("@this")._1._2._2

        val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, "length"))
        val n_start = Operator.ToInteger(getArgValue(h_1, ctx_1, "0"))
        val n_count = Operator.ToInteger(getArgValue(h_1, ctx_1, "1"))
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
                            PreHelper.PropStore(__h, l, AbsString.alpha((start + i).toInt.toString), getArgValue(__h, ctx_1, (i+2).toString)))
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
                            PreHelper.PropStore(__h, l, AbsString.alpha((start + i).toInt.toString), getArgValue(__h, ctx_1, (i+2).toString)))
                          val new_length = n_len + add_count - count
                          PreHelper.PropStore(__h2, l,  AbsString.alpha("length"), Value(AbsNumber.alpha(new_length)))
                        }
                      }
                      case NumBot => _h
                      case _ =>
                          val _h1 = PreHelper.PropStore(h_1, l, NumStr, getArgValueAbs(_h, ctx_1, NumStr))
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
                  val _h1 = PreHelper.PropStore(_h, l, NumStr, getArgValueAbs(_h, ctx_1, NumStr))
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
                    val _h2 = PreHelper.PropStore(_h1, l, NumStr, getArgValueAbs(_h1, ctx_1, NumStr))
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
      }
      case "Array.prototype.unshift" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
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
                      val v_i = getArgValue(__h, ctx, i.toString)
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
      }
      case "Array.prototype.indexOf" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val n_index = n_arglen match {
          case UIntSingle(n) => {
            val v_search = getArgValue(h, ctx, "0")
            lset_this.foldLeft[AbsNumber](NumBot)((_n, l) => {
              val n_len = Operator.ToUInt32(PreHelper.Proto(h, l, AbsString.alpha("length")))
              _n + (n_len match {
                case UIntSingle(n_len) => {
                  if (n_len == 0)
                    AbsNumber.alpha(-1)
                  else {
                    val start =
                      if (n > 1) Operator.ToInteger(getArgValue(h, ctx, "1"))
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
                            AbsNumber.alpha(-1)
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
                            AbsNumber.alpha(-1)
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
      }
      case "Array.prototype.lastIndexOf" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val n_index = n_arglen match {
          case UIntSingle(n) => {
            val v_search = getArgValue(h, ctx, "0")
            lset_this.foldLeft[AbsNumber](NumBot)((_n, l) => {
              val n_len = Operator.ToUInt32(PreHelper.Proto(h, l, AbsString.alpha("length")))
              _n + (n_len match {
                case UIntSingle(n_len) => {
                  if (n_len == 0)
                    AbsNumber.alpha(-1)
                  else {
                    val start =
                      if (n > 1) Operator.ToInteger(getArgValue(h, ctx, "1"))
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
                          AbsNumber.alpha(-1)
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
                          AbsNumber.alpha(-1)
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
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
