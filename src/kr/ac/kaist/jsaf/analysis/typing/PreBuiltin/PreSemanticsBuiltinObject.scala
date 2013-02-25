/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import scala.math.abs
import scala.math.floor
import scala.math.max
import scala.math.min

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.Operator._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.{PreSemanticsExpr => SE}
import kr.ac.kaist.jsaf.nodes_util.IRFactory

object PreSemanticsBuiltinObject {
  def builtinObject(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr, addr1: Address): ((Heap, Context),(Heap, Context)) = {
    val PureLocalLoc = cfg.getPureLocal(cp)
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1
    val lset_this = h(PureLocalLoc)("@this")._1._2._2

    fun match {
      case "Object" => {
        // 15.2.2.1 new Object( [value] )
        val v = getArgValue(h, ctx, "0") // [value]

        // 1.a. If Type(value) is Object, then simply return value.
        //      We do not consider an implementation-dependent actions for a host object.
        val (v_1, h_1, ctx_1) =
          if (!v._2.isEmpty) (Value(v._2), h, ctx)
          else (ValueBot, h, ctx)

        // 1.b. If Type(value) is String, return ToObject(value)
        // 1.c. If Type(value) is Boolean, return ToObject(value)
        // 1.d. If Type(value) is Number, return ToObject(value)
        val (v_2, h_2, ctx_2, es) =
            if ((v._1._3 </ BoolBot) || (v._1._4 </ NumBot) || (v._1._5 </ StrBot)) {
              val _v_new = Value(PValue(UndefBot, NullBot, v._1._3, v._1._4, v._1._5))
              val (_v, _h, _ctx, _es) = PreHelper.toObject(h_1, ctx_1, _v_new, addr1)
              (_v, _h, _ctx, _es)
            } else {
              (ValueBot, h_1, ctx_1, ExceptionBot)
            }
        // 2. Assert: The argument value was not supplied or its type was Null or Undefined.
        val (v_3, h_3, ctx_3) =
            if ((v._1._1 </ UndefBot) || (v._1._2 </ NullBot)) {
              val (_h_1, _ctx_1) = PreHelper.Oldify(h_2, ctx_2, addr1)
              val _l_r = addrToLoc(addr1, Recent)
              val _h = PreHelper.allocObject(_h_1, ObjProtoSingleton, _l_r)
              (Value(_l_r), _h, _ctx_1)
            } else {
              (ValueBot, h_2, ctx_2)
            }

        val v_4 = v_1 + v_2 + v_3
        // val h_4 = h_1 + h_2 + h_3
        val h_4 = h_3
        val ctx_4 = ctx_3

        val (h_e, ctx_e) = PreHelper.RaiseException(h_4, ctx_4, PureLocalLoc, es)
        val s = (he + h_e, ctxe + ctx_e)

          if (v_4 </ ValueBot)
            ((PreHelper.ReturnStore(h_4, PureLocalLoc, v_4), ctx_4), s)
          else
            ((h_4, ctx_4), s)
      }
      case "Object.constructor" => {
        // 15.2.2.1 new Object( [value] )
        val v = getArgValue(h, ctx, "0") // [value]

        // 1.a. If Type(value) is Object, then simply return value.
        //      We do not consider an implementation-dependent actions for a host object.
        val (v_1, h_1, ctx_1) =
          if (!v._2.isEmpty) (Value(v._2), h, ctx)
          else (ValueBot, h, ctx)

        // 1.b. If Type(value) is String, return ToObject(value)
        // 1.c. If Type(value) is Boolean, return ToObject(value)
        // 1.d. If Type(value) is Number, return ToObject(value)
        val (v_2, h_2, ctx_2) =
          if ((v._1._3 </ BoolBot) || (v._1._4 </ NumBot) || (v._1._5 </ StrBot)) {
            val _v_new = Value(PValue(UndefBot, NullBot, v._1._3, v._1._4, v._1._5))
            val o_1 =
              if (!(_v_new._1._5 <= StrBot)) PreHelper.NewString(v._1._5)
              else ObjBot
            val o_2 =
              if (!(_v_new._1._3 <= BoolBot)) PreHelper.NewBoolean(v._1._3)
              else ObjBot
            val o_3 =
              if (!(_v_new._1._4 <= NumBot)) PreHelper.NewNumber(v._1._4)
              else ObjBot
            val o = o_1 + o_2 + o_3
            val _h = lset_this.foldLeft(h)((_h, l) => _h.update(l, o))
            (Value(lset_this), _h, ctx)
          } else {
            (ValueBot, h, ctx)
          }
        // 2. Assert: The argument value was not supplied or its type was Null or Undefined.
        val (v_3, h_3, ctx_3) =
          if ((v._1._1 </ UndefBot) || (v._1._2 </ NullBot)) {
            val _h = lset_this.foldLeft(h_2)((_h, l) => PreHelper.allocObject(_h, ObjProtoSingleton, l))
            (Value(lset_this), _h, ctx)
          } else {
            (ValueBot, h_2, ctx_2)
          }

        val v_4 = v_1 + v_2 + v_3
//        val h_4 = h_1 + h_2 + h_3
//        val ctx_4 = ctx_1 + ctx_2 + ctx_3
        val h_4 = h_3
        val ctx_4 = ctx_3
          if (v_4 </ ValueBot)
            ((PreHelper.ReturnStore(h_4, PureLocalLoc, v_4), ctx_4), (he, ctxe))
          else
            ((h_4, ctx_4), (he, ctxe))
      }
      case "Object.getPrototypeOf" => {
        val v_1 = getArgValue(h, ctx, "0")
        val es =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val v_2 = v_1._2.foldLeft(ValueBot)(
            (_v, l) => _v + h(l)("@proto")._1._1._1)
        val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)
        val (h_1, ctx_1) =
            if (v_2 </ ValueBot) (PreHelper.ReturnStore(h_e, PureLocalLoc,v_2), ctx_e)
            else (h, ctx)
        ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
      }
      case "Object.getOwnPropertyDescriptor" => {
        val v_1 = getArgValue(h, ctx, "0")
        val s_prop = PreHelper.toString(PreHelper.toPrimitive(getArgValue(h, ctx, "1")))
        val es =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val (propv, absent) = v_1._2.foldLeft[(PropValue, Absent)]((PropValueBot, AbsentBot))((pva, l) => {
          val (pv, a) = h(l)(s_prop)
          (pva._1 + pv, pva._2 + a)})
        val (v_2, h_2, ctx_2) =
          if (AbsentTop <= absent || propv <= PropValueBot )
            (Value(UndefTop), h, ctx)
          else
            (ValueBot, h, ctx)
        val ov = propv._1
        val (v_3, h_3, ctx_3) =
          if (Value(PValue(UndefBot, ov._1._1._2, ov._1._1._3,ov._1._1._4,ov._1._1._5), ov._1._2) </ ValueBot) {
            val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
            val l_r = addrToLoc(addr1, Recent)
            val o_new = PreHelper.NewObject(ObjProtoLoc)
            val o_1 =
              if (true) // isDataDescriptor(H(v),s)
                o_new.
                update("value", PropValue(ObjectValue(ov._1, BoolTrue, BoolTrue, BoolTrue))).
                update("writable", PropValue(ObjectValue(ov._2, BoolTrue, BoolTrue, BoolTrue)))
              else
                o_new
            val o_2 =  o_1.
              update("enumerable", PropValue(ObjectValue(ov._3, BoolTrue, BoolTrue, BoolTrue))).
              update("configurable", PropValue(ObjectValue(ov._4, BoolTrue, BoolTrue, BoolTrue)))
            val h_2 = h_1.update(l_r, o_2)
            (Value(LocSet(l_r)), h_2, ctx_1)
          }
          else
            (ValueBot, h_2, ctx_2)
        val v_4 = v_2 + v_3
//        val h_4 = h_2 + h_3
        val h_4 = h_3
        val (h_e, ctx_e) = PreHelper.RaiseException(h_4, ctx_3, PureLocalLoc, es)
        val (h_5, ctx_5) =
            if (v_4 </ ValueBot) (PreHelper.ReturnStore(h_e, PureLocalLoc, v_4), ctx_e)
            else (h_4, ctx_3)
        ((h_5, ctx_5), (he + h_e, ctxe + ctx_e))
      }
      case "Object.getOwnPropertyNames" => {
        val v_1 = getArgValue(h, ctx, "0")
        val es =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val o = v_1._2.foldLeft(ObjEmpty)((_o, l) => _o + h(l))
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
        val l_r = addrToLoc(addr1, Recent)
        val o_new = PreHelper.NewArrayObject(UIntSingle(0))
        val (o_1,_) = o.getProps.foldLeft((o_new, 0))(
            (on, s)=> {
              val _o = on._1.update(on._2.toString(), PropValue(ObjectValue(AbsString.alpha(s), BoolTrue, BoolTrue, BoolTrue)))
              val _n = on._2 + 1
              (_o, _n)})
        val h_2 = h_1.update(l_r, o_1)
        val (h_e, ctx_e) = PreHelper.RaiseException(h_2, ctx_1, PureLocalLoc, es)
          ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(LocSet(l_r))), ctx_e), (he+h_e, ctxe+ctx_e))
      }
      case "Object.create" =>{
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
        val l_r = addrToLoc(addr1, Recent)
        val v_1 = getArgValue(h_1, ctx_1, "0")
        val es_1 =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, "length"))
        val v_2 = getArgValue(h_1, ctx_1, "1")
        val es_2 =
          if (v_2._1 </ PValueBot && n_arglen == UIntSingle(2)) Set[Exception](TypeError)
          else ExceptionBot
        val h_2 = h_1.update(l_r, PreHelper.NewObject(v_1._2))
        val h_3 =
          if (n_arglen == UIntSingle(2)) {
            v_2._2.foldLeft(h_2)((_h, l_2) => {PreHelper.DefineProperties(_h, l_r, l_2)})
          }
          else {
            h_2
          }
        val (h_e, ctx_e) = PreHelper.RaiseException(h_3, ctx_1, PureLocalLoc, es_1 ++ es_2)
        ((PreHelper.ReturnStore(h_3, PureLocalLoc, Value(LocSet(l_r))), ctx_1), (he+h_e, ctxe+ctx_e))
      }
      case "Object.defineProperty" =>{
        val v_1 = getArgValue(h, ctx, "0")
        val es_1 =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val s_name = PreHelper.toString(PreHelper.toPrimitive(getArgValue(h, ctx, "1")))
        val v_2 = getArgValue(h, ctx, "2")
        val es_2 =
          if (v_2._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val h_1 =
          v_1._2.foldLeft(h)((_h, l_1) =>
            v_2._2.foldLeft(_h)((__h, l_2) =>
              PreHelper.DefineProperty(__h, l_1, s_name, l_2)) )
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx, PureLocalLoc, es_1 ++ es_2)
          if (Value(v_1._2) </ ValueBot)
            ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(v_1._2)), ctx_e), (he+h_e, ctxe+ctx_e))
          else
            ((h_1, ctx), (he+h_e, ctxe+ctx_e))
      }
      case "Object.defineProperties" => {
        val v_1 = getArgValue(h, ctx, "0")
        val es_1 =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val v_2 = getArgValue(h, ctx, "1")
        val es_2 =
          if (v_2._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val h_1 =
          v_1._2.foldLeft(h)((_h, l_1) =>
            v_2._2.foldLeft(_h)((__h, l_2) => PreHelper.DefineProperties(__h, l_1, l_2)))
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx, PureLocalLoc, es_1 ++ es_2)
          if (Value(v_1._2) </ ValueBot)
            ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(v_1._2)), ctx_e), (he+h_e, ctxe+ctx_e))
          else
            ((h_1, ctx), (he+h_e, ctxe+ctx_e))
      }
      case "Object.seal" => {
        val v = getArgValue(h, ctx, "0")
        val es =
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val h_1 = v._2.foldLeft(h)((_h, l) => {
          val obj = _h(l)
          val obj_1 = obj.getProps.foldLeft(obj)((_o, s) => {
            val ov = _o(s)._1._1
            _o.update(s, PropValue(ObjectValue(ov._1,ov._2,ov._3,BoolFalse)))
          })
          val obj_2 = obj_1.update("@extensible", PropValue(BoolFalse))
          _h.update(l, obj_2)
        })
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx, PureLocalLoc, es)
        if (Value(v._2) </ ValueBot)
          ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(v._2)), ctx_e), (he+h_e, ctxe+ctx_e))
        else
          ((h_1, ctx), (he+h_e, ctxe+ctx_e))
      }
      case "Object.freeze" => {
        val v = getArgValue(h, ctx, "0")
        val es =
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val h_1 = v._2.foldLeft(h)((_h, l) => {
          val obj = _h(l)
          val obj_1 = obj.getProps.foldLeft(obj)((_o, s) => {
            val ov = _o(s)._1._1
            _o.update(s, PropValue(ObjectValue(ov._1,BoolFalse,ov._3,BoolFalse)))
          })
          val obj_2 = obj_1.update("@extensible", PropValue(BoolFalse))
          _h.update(l, obj_2)
        })
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx, PureLocalLoc, es)
        if (Value(v._2) </ ValueBot)
          ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(v._2)), ctx_e), (he+h_e, ctxe+ctx_e))
        else
          ((h_1, ctx), (he+h_e, ctxe+ctx_e))
      }
      case "Object.preventExtensions" => {
        val v = getArgValue(h, ctx, "0")
        val es =
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val h_1 = v._2.foldLeft(h)((_h, l) =>
          _h.update(l, _h(l).update("@extensible", PropValue(BoolFalse))))
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx, PureLocalLoc, es)
        if (Value(v._2) </ ValueBot)
          ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(v._2)), ctx_e), (he+h_e, ctxe+ctx_e))
        else
          ((h_1, ctx), (he+h_e, ctxe+ctx_e))
      }
      case "Object.isSealed" => {
        val v = getArgValue(h, ctx, "0")
        val es =
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val b = v._2.foldLeft[AbsBool](BoolBot)((_b, l) => {
            val o = h(l)
            val props = o.getProps
            val b_f =
              if (props.exists((s) => BoolTrue <= o(s)._1._1._4))
                BoolFalse
              else  BoolBot
            val b_t =
              if (props.forall((s) => BoolFalse <= o(s)._1._1._4)) {
                val v_ex = o("@extensible")._1._2
                if (Value(BoolTop) <= v_ex)  BoolTop
                else if (Value(BoolFalse) <= v_ex) BoolTrue
                else if (Value(BoolTrue) <= v_ex) BoolFalse
                else BoolBot
              }
              else
                BoolBot
            _b + b_f + b_t
          })
        val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)
          if (b </ BoolBot)
            ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(b)), ctx_e), (he+h_e, ctxe+ctx_e))
          else
            ((h, ctx), (he+h_e, ctxe+ctx_e))
      }
      case "Object.isFrozen" => {
        val v = getArgValue(h, ctx, "0")
        val es =
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val b = v._2.foldLeft[AbsBool](BoolBot)((_b, l) => {
            val o = h(l)
            val props = o.getProps
            val b_f =
              if (props.exists((s) => (BoolTrue <= o(s)._1._1._2 || BoolTrue <= o(s)._1._1._4)))
                BoolFalse
              else
                BoolBot
            val b_t =
              if (props.forall((s) => (BoolFalse <= o(s)._1._1._2 && BoolFalse <= o(s)._1._1._4))) {
                val v_ex = o("@extensible")._1._2
                if (Value(BoolTop) <= v_ex)  BoolTop
                else if (Value(BoolFalse) <= v_ex) BoolTrue
                else if (Value(BoolTrue) <= v_ex) BoolFalse
                else BoolBot
              }
              else
                BoolBot
            _b + b_f + b_t
          })
        val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)
          if (b </ BoolBot)
            ((PreHelper.ReturnStore(h_e, PureLocalLoc, Value(b)), ctx_e), (he+h_e, ctxe+ctx_e))
          else
            ((h, ctx), (he+h_e, ctxe+ctx_e))
      }
      case "Object.isExtensible" => {
        val v = getArgValue(h, ctx, "0")
        val es =
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val v_ex = v._2.foldLeft[Value](ValueBot)((_v, l) =>
          _v + h(l)("@extensible")._1._2)
        val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)
        if (v </ ValueBot)
          ((PreHelper.ReturnStore(h_e, PureLocalLoc, v_ex), ctx_e), (he+h_e, ctxe+ctx_e))
        else
          ((h, ctx), (he+h_e, ctxe+ctx_e))
      }
      case "Object.keys" => {
        val l_r = addrToLoc(addr1, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
        val v = getArgValue(h_1, ctx_1, "0")
        val es =
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val o = v._2.foldLeft(ObjBot)((_o, l) => {
          val map_enum = h_1(l).map.filter((kv)=> BoolTrue <= kv._2._1._1._3)
          val o_new = PreHelper.NewArrayObject(AbsNumber.alpha(map_enum.size))
          map_enum.foldLeft(o_new)((_o, kv) => _o.update(NumStr, PropValue(ObjectValue(AbsString.alpha(kv._1),BoolTrue,BoolTrue,BoolTrue))))
        })
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx_1, PureLocalLoc, es)
        if (o </ ObjBot) {
          val h_2 = h_e.update(l_r, o)
          ((PreHelper.ReturnStore(h_2, PureLocalLoc, Value(l_r)), ctx_1), (he+h_e, ctxe+ctx_e))
        }
        else
          ((h_1, ctx_1), (he+h_e, ctxe+ctx_e))
      }
      case "Object.prototype.toString" |
           "Object.prototype.toLocaleString" => {
        val s = lset_this.foldLeft[AbsString](StrBot)((_s, l) =>
          _s + h(l)("@class")._1._2._1._5 match {
        case NumStrSingle(s) =>
              AbsString.alpha("[object " + s + "]")
        case OtherStrSingle(s) =>
              AbsString.alpha("[object " + s + "]")
        case StrBot =>
              StrBot
        case _ =>
              OtherStr
      })
        if (s </ StrBot)
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
        else
          ((h, ctx), (he, ctxe))
      }
      case "Object.prototype.valueOf" => {
        if (Value(lset_this) </ ValueBot)
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(lset_this)), ctx), (he, ctxe))
        else
          ((h, ctx), (he, ctxe))
      }
      case "Object.prototype.hasOwnProperty" => {
        // 15.2.4.5 Object.prototype.hasOwnProperty(V)
        val s = PreHelper.toString(PreHelper.toPrimitive(getArgValue(h, ctx, "0")))
        val b = lset_this.foldLeft[AbsBool](BoolBot)((b,l) => {
          b + PreHelper.HasOwnProperty(h, l, s)
        })
        if (b </ BoolBot)
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(b)), ctx), (he, ctxe))
        else
          ((h, ctx), (he, ctxe))
      }
      case "Object.prototype.isPrototypeOf" => {
        val v = getArgValue(h, ctx, "0")
        val b_1 =
          if (v._1 </ PValueBot)
            BoolFalse
          else
            BoolBot
        val b_2 = v._2.foldLeft[AbsBool](BoolBot)((b,l) => {
          val v_proto = h(l)("@proto")._1._1._1
          val b_3 =
            if (NullTop <= v_proto._1._2)
              BoolFalse
            else
              BoolBot
          val b_4 = Operator.bopEq(Value(lset_this), Value(v_proto._2))._1._3
          b + b_3 + b_4})
        val b = b_1 + b_2
        if (b </ BoolBot)
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(b)), ctx), (he, ctxe))
        else
          ((h, ctx), (he, ctxe))
      }
      case "Object.prototype.propertyIsEnumerable" => {
        val s = PreHelper.toString(PreHelper.toPrimitive(getArgValue(h, ctx, "0")))
        val b =
          lset_this.foldLeft[AbsBool](BoolBot)((_b, l) => {
            val ov = h(l)(s)._1._1
            val b_1 =
              if (UndefTop <= ov._1._1._1)
                BoolFalse
              else
                BoolBot
            val b_2 =
              ov._3
            _b + b_1 + b_2
      })
        if (b </ BoolBot)
          ((PreHelper.ReturnStore(h, PureLocalLoc, Value(b)), ctx), (he, ctxe))
        else
          ((h, ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }
}
