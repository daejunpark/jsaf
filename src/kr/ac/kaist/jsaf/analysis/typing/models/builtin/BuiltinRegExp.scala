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
import kr.ac.kaist.jsaf.utils.regexp.JSRegExpSolver
import scala.collection.immutable.HashSet

object BuiltinRegExp extends ModelData {

  val ConstLoc = newPreDefLoc("RegExpConst", Recent)
  val ProtoLoc = newPreDefLoc("RegExpProto", Recent)

  private val prop_const: List[(String, AbsProperty)] = List(
    ("@class",                   AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto",                   AbsConstValue(PropValue(ObjectValue(Value(FunctionProtoLoc), F, F, F)))),
    ("@extensible",              AbsConstValue(PropValue(T))),
    ("@scope",                   AbsConstValue(PropValue(Value(NullTop)))),
    ("@function",                AbsInternalFunc("RegExp")),
    ("@construct",               AbsInternalFunc("RegExp.constructor")),
    ("@hasinstance",             AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype",                AbsConstValue(PropValue(ObjectValue(Value(ProtoLoc), F, F, F)))),
    ("length",                   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(2), F, F, F))))
  )

  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("RegExp")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
    ("constructor",          AbsConstValue(PropValue(ObjectValue(ConstLoc, F, F, F)))),
    ("exec",                 AbsBuiltinFunc("RegExp.prototype.exec", 1)),
    ("test",                 AbsBuiltinFunc("RegExp.prototype.test", 1)),
    ("toString",             AbsBuiltinFunc("RegExp.prototype.toString", 0))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (ConstLoc, prop_const), (ProtoLoc, prop_proto)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      "RegExp" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          // API address allocation
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)

          val v_1 = getArgValue(h, ctx, args, "0")
          val v_2 = getArgValue(h, ctx, args, "1")

          // case for pattern is undefined.
          val p_1 =
            if (v_1._1._1 </ UndefBot) AbsString.alpha("")
            else StrBot
          // case for flags is undefined.
          val f_1 =
            if (v_2._1._1 </ UndefBot) AbsString.alpha("")
            else StrBot

          // case for pattern is an object whose [[class]] is RegExp.
          val lset_1 = v_1._2.filter(l => AbsString.alpha("RegExp") <= h(l)("@class")._1._2._1._5)
          // case for pattern is an object whose [[class]] is not a RegExp.
          val lset_2 = v_1._2.filter(l => AbsString.alpha("RegExp") </ h(l)("@class")._1._2._1._5)

          // case for pattern is a value which is not an undefined or an object whose [[class] is not a RegExp.
          val v_1_ = Value(PValue(UndefBot, v_1._1._2, v_1._1._3, v_1._1._4, v_1._1._5), lset_2)
          val p_3 = Helper.toString(Helper.toPrimitive(v_1_))
          // case for flags is a value which is not an undefined or an object.
          val v_2_ = Value(PValue(UndefBot, v_2._1._2, v_2._1._3, v_2._1._4, v_2._1._5), v_2._2)
          val f_2 = Helper.toString(Helper.toPrimitive(v_2_))

          // If pattern is an object R whose [[Class] internal property is "RegExp" and
          // flags is not undefined, then throw a "TypeError" exception.
          val es_1 = if (!lset_1.isEmpty && !(v_2_ <= ValueBot)) {
            HashSet[Exception](TypeError)
          } else {
            ExceptionBot
          }

          // case for pattern is a value or an object whose [[class]] is not a RegExp.
          val p = p_1 + p_3
          // case for flags is a value or an object.
          val f = f_1 + f_2

          val (oo, es_2) = (p.getConcreteValue(), f.getConcreteValue()) match {
            case (Some(pattern), Some(flags)) => {
              try {
                val s =
                  if (pattern == "") "(?:)"
                  else pattern
                val (_, b_g, b_i, b_m, _) = JSRegExpSolver.parse(s, flags)

                val o = Helper.NewRegExp(AbsString.alpha(s), AbsBool.alpha(b_g), AbsBool.alpha(b_i), AbsBool.alpha(b_m))
                (Some(o), ExceptionBot)
              } catch {
                case _ => {
                  (None, HashSet[Exception](SyntaxError))
                }
              }
            }
            case _ if p </ StrBot && f </ StrBot => (Some(Helper.NewRegExp(p, BoolTop, BoolTop, BoolTop)), HashSet[Exception](SyntaxError))
            case _ => (None, ExceptionBot)
          }

          val (h_1, ctx_1) = oo match {
            case Some(o) => {
              val (h_1_, ctx_1_) = Helper.Oldify(h, ctx, addr1)
              val h_2_ = h_1_.update(l_r, o)
              (h_2_, ctx_1_)
            }
            case None => (h, ctx)
          }

          val v_rtn_1 = oo match {
            case Some(o) => Value(l_r)
            case None => ValueBot
          }
          val v_rtn_2 =
            if ((!lset_1.isEmpty) && v_2._1._1 </ UndefBot) Value(lset_1)
            else ValueBot

          val v_rtn = v_rtn_1 + v_rtn_2

          val (h_2, ctx_2) =
            if (v_rtn </ ValueBot) (Helper.ReturnStore(h_1, v_rtn), ctx_1)
            else (HeapBot, ContextBot)

          val es = es_1 ++ es_2
          val (he_1, ctxe_1) = Helper.RaiseException(h, ctx, es)
          ((h_2, ctx_2), (he + he_1, ctxe + ctxe_1))
        }),
      "RegExp.constructor" -> (
      (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
        val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
        val v_1 = getArgValue(h, ctx, args, "0")
        val v_2 = getArgValue(h, ctx, args, "1")

        // case for pattern is undefined.
        val p_1 =
          if (v_1._1._1 </ UndefBot) AbsString.alpha("")
          else StrBot
        // case for flags is undefined.
        val f_1 =
          if (v_2._1._1 </ UndefBot) AbsString.alpha("")
          else StrBot

        // case for pattern is an object whose [[class]] is RegExp.
        val lset_1 = v_1._2.filter(l => AbsString.alpha("RegExp") <= h(l)("@class")._1._2._1._5)
        val p_2 = lset_1.foldLeft[AbsString](StrBot)((s, l) => s + h(l)("source")._1._1._1._1._5)

        // case for pattern is an object whose [[class]] is not a RegExp.
        val lset_2 = v_1._2.filter(l => AbsString.alpha("RegExp") </ h(l)("@class")._1._2._1._5)

        // case for pattern is a value which is not an undefined or an object whose [[class] is not a RegExp.
        val v_1_ = Value(PValue(UndefBot, v_1._1._2, v_1._1._3, v_1._1._4, v_1._1._5), lset_2)
        val p_3 = Helper.toString(Helper.toPrimitive(v_1_))
        // case for flags is a value which is not an undefined or an object.
        val v_2_ = Value(PValue(UndefBot, v_2._1._2, v_2._1._3, v_2._1._4, v_2._1._5), v_2._2)
        val f_2 = Helper.toString(Helper.toPrimitive(v_2_))

        // If pattern is an object R whose [[Class] internal property is "RegExp" and
        // flags is not undefined, then throw a "TypeError" exception.
        val es_1 = if (!lset_1.isEmpty && !(v_2_ <= ValueBot)) {
          HashSet[Exception](TypeError)
        } else {
          ExceptionBot
        }

        // case for pattern is a value or an object
        val p = p_1 + p_2 + p_3
        // case for flags is a value or an object.
        val f = f_1 + f_2

        val (oo, es_2) = (p.getConcreteValue(), f.getConcreteValue()) match {
          case (Some(pattern), Some(flags)) => {
            try {
              val s =
                if (pattern == "") "(?:)"
                else pattern
              val (_, b_g, b_i, b_m, _) = JSRegExpSolver.parse(s, flags)

              val o = Helper.NewRegExp(AbsString.alpha(s), AbsBool.alpha(b_g), AbsBool.alpha(b_i), AbsBool.alpha(b_m))
              (Some(o), ExceptionBot)
            } catch {
              case _ => {
                (None, HashSet[Exception](SyntaxError))
              }
            }
          }
          case _ if p </ StrBot && f </ StrBot => (Some(Helper.NewRegExp(p, BoolTop, BoolTop, BoolTop)), HashSet[Exception](SyntaxError))
          case _ => (None, ExceptionBot)
        }

        val (h_1, ctx_1) = oo match {
          case Some(o) => {
            val h_1_ = lset_this.foldLeft(h)((h_, l) => h_.update(l, o))
            (h_1_, ctx)
          }
          case _ => (HeapBot, ContextBot)
        }

        val es = es_1 ++ es_2
        val (he_1, ctxe_1) = Helper.RaiseException(h, ctx, es)
        ((h_1, ctx_1), (he + he_1, ctxe + ctxe_1))
      }),
     // imprecise semantics
      "RegExp.prototype.exec" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          // allocate new location
          val v_1 = getArgValue(h, ctx, args, "0")
          val argVal = Helper.toString(Helper.toPrimitive(v_1))
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)

          val lset_1 = lset_this.filter(l => AbsString.alpha("RegExp") <= h(l)("@class")._1._2._1._5)
          val lset_2 = lset_this.filter(l => AbsString.alpha("RegExp") </ h(l)("@class")._1._2._1._5)

          // if 'this' object is an object whose [[class]] is a 'RegExp'
          val (h_4, ctx_4) =
            if (!lset_1.isEmpty) {
              val l = lset_1.head
              val src = h(l)("source")._1._1._1._1._5.getConcreteValue()
              val b_g = h(l)("global")._1._1._1._1._3.getConcreteValue()
              val b_i = h(l)("ignoreCase")._1._1._1._1._3.getConcreteValue()
              val b_m = h(l)("multiline")._1._1._1._1._3.getConcreteValue()
              val idx = Operator.ToInteger(h(l)("lastIndex")._1._1._1).getConcreteValue()
              val s_1 = argVal.getConcreteValue()

              val (h_3, ctx_3) = (lset_1.size, src, b_g, b_i, b_m, idx, s_1) match {
                case (1, Some(source), Some(g), Some(i), Some(m), Some(lastIdx), Some(arg)) => {
                  val flags = (if (g) "g" else "") + (if (i) "i" else "") + (if (m) "m" else "")

                  val (matcher, _, _, _, _) = JSRegExpSolver.parse(source, flags)

                  val lastIdx_ : Int = if (g) lastIdx.toInt else 0
                  val (array, lastIndex, index, length) = JSRegExpSolver.exec(matcher, arg, lastIdx_)

                  // XXX: Need to check the semantics of [[Put]] internal method.
                  val h_1 =
                    if (g) Helper.PropStore(h, l, AbsString.alpha("lastIndex"), Value(AbsNumber.alpha(lastIndex)))
                    else h

                  val (h_2, ctx_1) = array match {
                    case Some(_) => Helper.Oldify(h_1, ctx, addr1)
                    case None => (h_1, ctx)
                  }

                  array match {
                    case Some(arr) => {
                      val newobj = Helper.NewArrayObject(AbsNumber.alpha(length))
                        .update("index", PropValue(ObjectValue(AbsNumber.alpha(index), T, T, T)))
                        .update("input", PropValue(ObjectValue(argVal, T, T, T)))

                      val newobj_1 = (0 to length - 1).foldLeft(newobj)((no, i) => {
                        val v = arr(i) match {
                          case Some(s) => Value(AbsString.alpha(s))
                          case None => Value(UndefTop)
                        }
                        no.update(AbsString.alpha(i.toString), PropValue(ObjectValue(v, T, T, T)))
                      })
                      val h_3 = h_2.update(l_r, newobj_1)
                      (Helper.ReturnStore(h_3, Value(l_r)), ctx_1)
                    }
                    case None => {
                      (Helper.ReturnStore(h_2, Value(NullTop)), ctx_1)
                    }
                  }
                }
                case _ => {
                  // argument value
                  if (argVal </ StrBot) {
                    val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
                    val newobj = Helper.NewArrayObject(UInt)
                      .update("index", PropValue(ObjectValue(UInt, T, T, T)))
                      .update("input", PropValue(ObjectValue(argVal, T, T, T)))
                      .update("@default_number", PropValue(ObjectValue(Value(StrTop) + Value(UndefTop), T, T, T)))

                    val h_2 = Helper.PropStore(h_1, l, AbsString.alpha("lastIndex"), Value(UInt))
                    val h_3 = h_2.update(l_r, newobj)
                    (Helper.ReturnStore(h_3, Value(l_r) + Value(NullTop)), ctx_1)
                  } else {
                    (HeapBot, ContextBot)
                  }
                }
              }
              (h_3, ctx_3)
            } else {
              (HeapBot, ContextBot)
            }


          // if 'this' object is not an object whose [[class]] is a 'RegExp', throw a TypeError exception.
          val es =
            if (!lset_2.isEmpty) HashSet[Exception](TypeError)
            else ExceptionBot
          val (he_1, ctxe_1) = Helper.RaiseException(h, ctx, es)

          ((h_4, ctx_4), (he + he_1, ctxe + ctxe_1))
        }),
      "RegExp.prototype.test" -> (
       (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
         val v_1 = getArgValue(h, ctx, args, "0")
         val argVal = Helper.toString(Helper.toPrimitive(v_1))
         val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
         val lset_1 = lset_this.filter(l => AbsString.alpha("RegExp") <= h(l)("@class")._1._2._1._5)
         val lset_2 = lset_this.filter(l => AbsString.alpha("RegExp") </ h(l)("@class")._1._2._1._5)

         // if 'this' object is an object whose [[class]] is a 'RegExp'
         val (h_4, ctx_4) =
           if (!lset_1.isEmpty) {
             val l = lset_1.head
             val a_src = h(l)("source")._1._1._1._1._5
             val a_g = h(l)("global")._1._1._1._1._3
             val a_i = h(l)("ignoreCase")._1._1._1._1._3
             val a_m = h(l)("multiline")._1._1._1._1._3
             val a_idx = h(l)("lastIndex")._1._1._1
             val src = a_src.getConcreteValue()
             val b_g = a_g.getConcreteValue()
             val b_i = a_i.getConcreteValue()
             val b_m = a_m.getConcreteValue()
             val idx = Operator.ToInteger(a_idx).getConcreteValue()
             val s_1 = argVal.getConcreteValue()

             val (a_lastidx, b_rtn) = (lset_1.size, src, b_g, b_i, b_m, idx, s_1) match {
               // case for a concrete input.
               case (1, Some(source), Some(g), Some(i), Some(m), Some(lastIdx), Some(arg)) => {
                 val flags = (if (g) "g" else "") + (if (i) "i" else "") + (if (m) "m" else "")

                 val (matcher, _, _, _, _) = JSRegExpSolver.parse(source, flags)

                 val lastIdx_ : Int = if (g) lastIdx.toInt else 0
                 val (array, lastIndex, _, _) = JSRegExpSolver.exec(matcher, arg, lastIdx_)

                 val absLastIndex = AbsNumber.alpha(lastIndex)

                 val b_rtn_ = array match {
                   case Some(_) => BoolTrue
                   case _ => BoolFalse
                 }
                 (absLastIndex, b_rtn_)
               }
               // case for an abstract input which is not a bottom.
               case _ if a_src </ StrBot && a_g </ BoolBot && a_i </ BoolBot && a_m </ BoolBot && a_idx </ ValueBot && argVal </ StrBot => (UInt, BoolTop)
               // otherwise.
               case _ => (NumBot, BoolBot)
             }

             // XXX: Need to check the semantics of [[Put]] internal method.
             val h_2 =
               if (BoolTrue <= a_g) Helper.PropStore(h, l, AbsString.alpha("lastIndex"), Value(a_lastidx))
               else h

             val (h_3, ctx_3) =
               if (b_rtn </ BoolBot) {
                 (Helper.ReturnStore(h_2, Value(b_rtn)), ctx)
               } else {
                 (HeapBot, ContextBot)
               }

             (h_3, ctx_3)
           } else {
             (HeapBot, ContextBot)
           }

         val es =
           if (!lset_2.isEmpty) HashSet[Exception](TypeError)
           else ExceptionBot
         val (he_1, ctxe_1) = Helper.RaiseException(h, ctx, es)

         ((h_4, ctx_4), (he + he_1, ctxe + ctxe_1))
       }),
      "RegExp.prototype.toString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

          val lset_1 = lset_this.filter(l => AbsString.alpha("RegExp") <= h(l)("@class")._1._2._1._5)
          val lset_2 = lset_this.filter(l => AbsString.alpha("RegExp") </ h(l)("@class")._1._2._1._5)

          val (s_src, b_g, b_i, b_m) =
             lset_1.foldLeft
                [(AbsString, AbsBool, AbsBool, AbsBool)]((StrBot, BoolBot, BoolBot, BoolBot))((s, l) => {
            (s._1 + h(l)("source")._1._1._1._1._5,
              s._2 + h(l)("global")._1._1._1._1._3,
              s._3 + h(l)("ignoreCase")._1._1._1._1._3,
              s._4 + h(l)("multiline")._1._1._1._1._3)
          })

          val s_rtn = (s_src.getConcreteValue(), b_g.getConcreteValue(), b_i.getConcreteValue(), b_m.getConcreteValue()) match {
            case (Some(s), Some(g), Some(i), Some(m)) => {
              val flags = (if (g) "g" else "") + (if (i) "i" else "") + (if (m) "m" else "")
              AbsString.alpha("/"+s+"/"+flags)
            }
            case _ if s_src </ StrBot && b_g </ BoolBot && b_i </ BoolBot && b_m </ BoolBot => StrTop
            case _ => StrBot
          }

          val (h_1, ctx_1) =
            if (s_rtn </ StrBot) (Helper.ReturnStore(h, Value(s_rtn)), ctx)
            else (HeapBot, ContextBot)

          val es =
            if (!lset_2.isEmpty) HashSet[Exception](TypeError)
            else ExceptionBot
          val (he_1, ctxe_1) = Helper.RaiseException(h, ctx, es)

          ((h_1, ctx_1), (he + he_1, ctxe + ctxe_1))
        })
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
