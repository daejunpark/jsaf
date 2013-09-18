/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.builtin

import scala.math.{min,max,floor, abs}
import kr.ac.kaist.jsaf.analysis.cfg.{CFGExpr, CFG}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.{AccessHelper=>AH}
import scala.collection.immutable.HashSet
import kr.ac.kaist.jsaf.utils.regexp.JSRegExpSolver

object BuiltinString extends ModelData {

  val ConstLoc = newPreDefLoc("StringConst", Recent)
  val ProtoLoc = newPreDefLoc("StringProto", Recent)

  private val prop_const: List[(String, AbsProperty)] = List(
    ("@class",                   AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto",                   AbsConstValue(PropValue(ObjectValue(Value(FunctionProtoLoc), F, F, F)))),
    ("@extensible",              AbsConstValue(PropValue(T))),
    ("@scope",                   AbsConstValue(PropValue(Value(NullTop)))),
    ("@function",                AbsInternalFunc("String")),
    ("@construct",               AbsInternalFunc("String.constructor")),
    ("@hasinstance",             AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype",                AbsConstValue(PropValue(ObjectValue(Value(ProtoLoc), F, F, F)))),
    ("length",                   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1), F, F, F)))),
    ("fromCharCode",             AbsBuiltinFunc("String.fromCharCode", 1))
  )

  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class",         AbsConstValue(PropValue(AbsString.alpha("String")))),
    ("@proto",         AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
    ("@extensible",    AbsConstValue(PropValue(BoolTrue))),
    ("constructor",    AbsConstValue(PropValue(ObjectValue(ConstLoc, F, F, F)))),
    ("toString",       AbsBuiltinFunc("String.prototype.toString", 0)),
    ("valueOf",        AbsBuiltinFunc("String.prototype.valueOf", 0)),
    ("charAt",         AbsBuiltinFunc("String.prototype.charAt", 1)),
    ("charCodeAt",     AbsBuiltinFunc("String.prototype.charCodeAt", 1)),
    ("concat",         AbsBuiltinFunc("String.prototype.concat", 1)),
    ("indexOf",        AbsBuiltinFunc("String.prototype.indexOf", 1)),
    ("lastIndexOf",    AbsBuiltinFunc("String.prototype.lastIndexOf", 1)),
    ("localeCompare",  AbsBuiltinFunc("String.prototype.localeCompare", 1)),
    ("match",          AbsBuiltinFunc("String.prototype.match", 1)),
    ("replace",        AbsBuiltinFunc("String.prototype.replace", 2)),
    ("search",         AbsBuiltinFunc("String.prototype.search", 1)),
    ("slice",          AbsBuiltinFunc("String.prototype.slice", 2)),
    ("split",          AbsBuiltinFunc("String.prototype.split", 2)),
    ("substring",      AbsBuiltinFunc("String.prototype.substring", 2)),
    // ECMASCript 5 Appendix B.2.3 String.prototype.substr(start, length)
    ("substr",      AbsBuiltinFunc("String.prototype.substr", 2)),
    ("toLowerCase",    AbsBuiltinFunc("String.prototype.toLowerCase", 0)),
    ("toLocaleLowerCase", AbsBuiltinFunc("String.prototype.toLocaleLowerCase", 0)),
    ("toUpperCase",       AbsBuiltinFunc("String.prototype.toUpperCase", 0)),
    ("toLocaleUpperCase", AbsBuiltinFunc("String.prototype.toLocaleUpperCase", 0)),
    ("trim",              AbsBuiltinFunc("String.prototype.trim", 0))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (ConstLoc, prop_const), (ProtoLoc, prop_proto)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      "String" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          // 15.5.1.1 String( [value] )
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val s = n_arglen match {
            case UIntSingle(n) if n == 0 =>
              AbsString.alpha("")
            case UIntSingle(n) if n > 0 =>
              Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
            case NumBot => StrBot
            case _ => StrTop
          }
          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }),
      "String.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // 15.5.2.1 new String( [value] )
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val s = n_arglen match {
            case UIntSingle(n) if n == 0 =>
              AbsString.alpha("")
            case UIntSingle(n) if n > 0 =>
              Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
            case NumBot => StrBot
            case _ => StrTop
          }
          val h_1 = lset_this.foldLeft(h)((_h, l) => _h.update(l, Helper.NewString(s)))
          if (s </ StrBot)
            ((Helper.ReturnStore(h_1, Value(lset_this)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }),
      "String.fromCharCode" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          // 15.5.3.2 String.fromCharCode( [char0 [, char1[, ...]]] )
          val arg_length = getArgValue(h, ctx, args, "length")._1._4
          val value_1 =
            if (AbsNumber.alpha(0) <= arg_length) Value(AbsString.alpha(""))
            else ValueBot
          val value_2 =
            if (arg_length </ NumBot) {
              AbsNumber.concretize(arg_length) match {
                case Some(n) => {
                  val s = (0 until n.toInt).foldLeft(AbsString.alpha(""))((s, i) => {
                    val v = Operator.ToUInt16(getArgValue(h, ctx, args, i.toString))
                    s.concat(AbsString.fromCharCode(v))
                  })
                  Value(s)
                }
                case None => Value(StrTop)
              }
            } else {
              ValueBot
            }
          val value = value_1 + value_2
          if (value </ ValueBot)
            ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))

        }),
      "String.prototype.toString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val es = notGenericMethod(h, lset_this, "String")
          val lset_string = lset_this.filter((l) => AbsString.alpha("String") <= h(l)("@class")._1._2._1._5)
          val v = Value(Helper.defaultToString(h, lset_string))

          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
          if (v </ ValueBot)
            ((Helper.ReturnStore(h, v), ctx), (he + h_e, ctxe + ctx_e))
          else
            ((HeapBot, ContextBot), (he + h_e, ctxe + ctx_e))
        }),
      "String.prototype.valueOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val es = notGenericMethod(h, lset_this, "String")
          val lset_string = lset_this.filter((l) => AbsString.alpha("String") <= h(l)("@class")._1._2._1._5)
          val v = lset_string.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
          if (v </ ValueBot)
            ((Helper.ReturnStore(h, v), ctx), (he + h_e, ctxe + ctx_e))
          else
            ((HeapBot, ContextBot), (he + h_e, ctxe + ctx_e))
        }),
      "String.prototype.charAt" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // 15.5.4.4 String.prototype.charAt(pos)
          val n_pos = Operator.ToInteger(getArgValue(h, ctx, args, "0"))

          // 1. Call CheckObjectCoercible passing the this value as its argument.
          //   Don't need to check this because <>getBase always returns a location which points to an object.
          // Let S be the result of calling ToString, giving it the this value as its argument.]
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          val n_size = s_this.length()

          val value_1 =
            if (BoolTrue <= (n_pos < AbsNumber.alpha(0))) Value(AbsString.alpha(""))
            else ValueBot
          val value_2 =
            if (BoolTrue <= (n_size < n_pos) || BoolTrue <= (n_size === n_pos)) Value(AbsString.alpha(""))
            else ValueBot
          val value_3 = Value(s_this.charAt(n_pos))
          val value = value_1 + value_2 + value_3

          if (value </ ValueBot)
            ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }),
      "String.prototype.charCodeAt" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // 15.5.4.5 String.prototype.charCodeAt(pos)
          val n_pos = Operator.ToInteger(getArgValue(h, ctx, args, "0"))

          // 1. Call CheckObjectCoercible passing the this value as its argument.
          //   Don't need to check this because <>getBase always returns a location which points to an object.
          // Let S be the result of calling ToString, giving it the this value as its argument.
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          val n_size = s_this.length()

          val value_1 =
            if (BoolTrue <= (n_pos < AbsNumber.alpha(0))) Value(NaN)
            else ValueBot
          val value_2 =
            if (BoolTrue <= (n_size < n_pos) || BoolTrue <= (n_size === n_pos)) Value(NaN)
            else ValueBot
          val value_3 = Value(s_this.charCodeAt(n_pos))
          val value = value_1 + value_2 + value_3

          if (value </ ValueBot)
            ((Helper.ReturnStore(h, value), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }),
      "String.prototype.concat" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          val s = n_arglen match {
            case UIntSingle(n) if n == 0 => s_this
            case UIntSingle(n) if n > 0 =>
              (0 until n.toInt).foldLeft(s_this)((_s, i) =>
                _s.concat(Helper.toString(Helper.toPrimitive((getArgValue(h, ctx, args, i.toString))))))
            case NumBot => StrBot
            case _ => StrTop
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }),
      "String.prototype.indexOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))

          val s_search = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          val n_pos = Operator.ToInteger(getArgValue(h, ctx, args, "1"))

          val n = (AbsString.concretize(s_this), AbsString.concretize(s_search), AbsNumber.concretize(n_pos)) match {
            case (Some(ss_this), Some(ss_search), Some(nn_pos)) =>
              AbsNumber.alpha(ss_this.indexOf(ss_search, nn_pos.toInt).toDouble)
            case _ =>
              if (s_this <= StrBot || s_search <= StrBot || n_pos <= NumBot)
                NumBot
              else
                NumTop
          }

          if (n </ NumBot)
            ((Helper.ReturnStore(h, Value(n)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }),
      "String.prototype.lastIndexOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))

          val s_search = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          val v_pos = getArgValue(h, ctx, args, "1")
          val v_pos1 =
            if (v_pos._1._1 </ UndefBot)
              Value(PValue(UndefBot, v_pos._1._2, v_pos._1._3, PosInf + v_pos._1._4, v_pos._1._5), v_pos._2)
            else v_pos
          val n_pos = Operator.ToInteger(v_pos1)

          val n = (AbsString.concretize(s_this), AbsString.concretize(s_search), AbsNumber.concretize(n_pos)) match {
            case (Some(ss_this), Some(ss_search), Some(nn_pos)) =>
              AbsNumber.alpha(ss_this.lastIndexOf(ss_search, nn_pos.toInt).toDouble)
            case _ =>
              if (s_this <= StrBot || s_search <= StrBot || n_pos <= NumBot)
                NumBot
              else
                NumTop
          }
          if (n </ NumBot)
            ((Helper.ReturnStore(h, Value(n)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }),
      "String.prototype.localeCompare" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          val s_that = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          val n = (AbsString.concretize(s_this), AbsString.concretize(s_that)) match {
            case (Some(s_this), Some(s_that)) =>
              AbsNumber.alpha(s_this.compare(s_that).toDouble)
            case _ =>
              if (s_this <= StrBot || s_that <= StrBot)
                NumBot
              else
                NumTop
          }

          if (n </ NumBot)
            ((Helper.ReturnStore(h, Value(n)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }),
      "String.prototype.match" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          // allocate new location
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)

          val lset_this = h_1(SinglePureLocalLoc)("@this")._1._2._2
          val lset_prim = lset_this.filter((l) => BoolTrue <= h_1(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h_1(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          // argument value
          val argVal = getArgValue(h, ctx, args, "0")
          if (argVal <= ValueBot)
            ((HeapBot, ContextBot), (he, ctxe))
          else {
            val newobj = Helper.NewArrayObject(UInt)
              .update("index", PropValue(ObjectValue(UInt, T, T, T)))
              .update("input", PropValue(ObjectValue(s_this, T, T, T)))
              .update("@default_number", PropValue(ObjectValue(StrTop, T, T, T)))
            val h_2 = h_1.update(l_r, newobj)
            ((Helper.ReturnStore(h_2, Value(NullTop) + Value(l_r)), ctx_1), (he, ctxe))
          }
        }),
      "String.prototype.replace" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          // argument value
          val argSearchValue = getArgValue(h, ctx, args, "0")
          val argReplaceValue = getArgValue(h, ctx, args, "1")

          val v_this = h(SinglePureLocalLoc)("@this")._1._2
          val v_this_ = Value(PValue(UndefBot, NullBot, v_this._1._3, v_this._1._4, v_this._1._5), v_this._2)

          val es =
            if (v_this._1._1 </ UndefBot || v_this._1._2 </ NullBot) HashSet[Exception](TypeError)
            else ExceptionBot

          // invoke v_this.toString()
          val string = Helper.toString(Helper.toPrimitive_better(h, v_this_))

          val lset_regexp = argSearchValue._2.filter(l => AbsString.alpha("RegExp") <= h(l)("@class")._1._2._1._5)
          val lset_objs = argSearchValue._2.filter(l => AbsString.alpha("RegExp") != h(l)("@class")._1._2._1._5 && h(l)("@class")._1._2._1._5 </ StrBot)
          val lsetf_replaceValue = argReplaceValue._2.filter(l => !h(l)("@function")._1._3.isEmpty)
          val lseto_replaceValue = argReplaceValue._2.filter(l => h(l)("@function")._1._3.isEmpty)
          val s_searchValue = Helper.toString(Helper.toPrimitive(Value(argSearchValue._1, lset_objs)))
          val s_replaceValue_ = Helper.toString(Helper.toPrimitive(Value(argReplaceValue._1, lseto_replaceValue)))

          val (he_, ctxe_) = Helper.RaiseException(h, ctx, es)
          val he_1 = he + he_
          val ctxe_1 = ctxe + ctxe_

          val s_replaceValue =
            if (string </ StrBot && argSearchValue </ ValueBot && !lsetf_replaceValue.isEmpty) {
              if (!Config.quietMode)
               System.err.println("* Warning: Semantics of the API function 'String.prototype.replace(searchValue, function)' are not defined.")
              StrTop
            } else {
              s_replaceValue_
            }
          if (string </ StrBot && argSearchValue </ ValueBot && s_replaceValue </ StrBot) {
            // search positions
            // m is for callback function.
            val (h_1, ctx_1, sr, m) =
              try {
                (string.getConcreteValue(), s_searchValue.getConcreteValue(), lset_regexp.size, s_replaceValue.getConcreteValue()) match {
                  // concrete case for "string".replace("string", "string")
                  case (Some(c_string), Some(c_searchValue), 0, Some(_)) => {
                    val array = new Array[Option[String]](1)
                    array.update(0, Some(c_searchValue))
                    val idx = c_string.indexOf(c_searchValue)
                    if (idx < 0) (h, ctx, Nil, 0)
                    else (h, ctx, List((idx + c_searchValue.length(), array)), 0)
                  }
                  // concrete case for "string".replace("regexp", "string")
                  case (Some(c_string), None, 1, Some(_)) if s_searchValue <= StrBot => {
                    val l = lset_regexp.head
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
                    (src, b_g, b_i, b_m, idx) match {
                      // concrete regexp object. global is true.
                      case (Some(source), Some(true), Some(i), Some(m), Some(index)) => {
                        val flags = "g" + (if (i) "i" else "") + (if (m) "m" else "")

                        val (matcher, _, _, _, nCapturingParens) = JSRegExpSolver.parse(source, flags)
                        var previousLastIndex: Int = 0
                        var lastIndex: Int = 0
                        var lastMatch: Boolean = true
                        var a: List[(Int, Array[Option[String]])] = Nil

                        while (lastMatch) {
                          val (array, lastidx, idx, length) = JSRegExpSolver.exec(matcher, c_string, lastIndex)
                          lastIndex = lastidx
                          array match {
                            case None => lastMatch = false
                            case Some(list) => {
                              if (lastidx == previousLastIndex) {
                                previousLastIndex = lastidx + 1
                              } else {
                                previousLastIndex = lastidx
                              }
                              a ::= (lastidx, list)
                            }
                          }
                        }
                        (h, ctx, a.reverse, nCapturingParens)
                      }
                      // concrete regexp object. global is false.
                      case (Some(source), Some(false), Some(i), Some(m), Some(index)) => {
                        // set the global property to true.
                        val flags = "g" + (if (i) "i" else "") + (if (m) "m" else "")

                        val (matcher, _, _, _, nCapturingParens) = JSRegExpSolver.parse(source, flags)

                        val (array, lastidx, _, _) = JSRegExpSolver.exec(matcher, c_string, 0)

                        array match {
                          case None => (h, ctx, Nil, nCapturingParens)
                          case Some(list) => (h, ctx, List((lastidx, list)), nCapturingParens)
                        }
                      }
                      // otherwise.
                      case _ if a_src </ StrBot && a_g </ BoolBot && a_i </ BoolBot && a_m </ BoolBot && a_idx </ ValueBot => throw new InternalError("not a concrete case")
                      case _ => {
                        // bottom case
                        (HeapBot, ContextBot, Nil, 0)
                      }
                    }
                  }
                  // otherwise
                  case _ => throw new InternalError("not a concrete case")
                }
              } catch {
                case e: InternalError => {
                  // update lastIndex if regexp object's global is true
                  val h_1 = lset_regexp.foldLeft(h)((h_, l) => {
                    if (BoolTrue <= h_(l)("global")._1._1._1._1._3)
                      Helper.PropStore(h_, l, AbsString.alpha("lastIndex"), Value(UInt))
                    else
                      h_
                  })
                  (h_1, ctx, Nil, 0)
                }
              }
            // replace values
            val s_replaced = (string.getConcreteValue(), sr, s_replaceValue.getConcreteValue()) match {
              case (Some(c_string), h::t, Some(newstring)) => {
                val len = newstring.length
                var replaced: String = ""
                var lastEndIndex: Int = 0
                for ((index, a) <- sr) {
                  val matchStr: String = a(0) match {
                    case Some(s) => s
                    case None => throw new InternalError("matched array[0] must be string.")
                  }
                  replaced += c_string.substring(lastEndIndex, index - matchStr.length())
                  lastEndIndex = index
                  var i = 0
                  while (i < len) {
                    if (i+2 <= len && newstring.substring(i, i+2) == "$$") {
                      replaced += '$'
                      i += 2
                    } else if (i+2 <= len && newstring.substring(i, i+2) == "$&") {
                      replaced += matchStr
                      i += 2
                    } else if (i+2 <= len && newstring.substring(i, i+2) == "$`") {
                      replaced += c_string.substring(0, index)
                      i += 2
                    } else if (i+2 <= len && newstring.substring(i, i+2) == "$'") {
                      replaced += c_string.substring(index)
                      i += 2
                    } else if (i+3 <= len && newstring(i) == '$' &&
                      '0' <= newstring(i+1) && newstring(i+1) <= '9' &&
                      '0' <= newstring(i+2) && newstring(i+2) <= '9' &&
                      newstring.substring(i+1, i+3) != "00") {
                      val v = a(newstring.substring(i+1, i+3).toInt)
                      val rs = v match {
                        case Some(s) => s
                        case None => ""
                      }
                      replaced += rs
                      i += 3
                    } else if (i+2 <= len && newstring(i) == '$' &&
                    '1' <= newstring(i+1) && newstring(i+1) <= '9') {
                      val v = a(newstring.substring(i+1, i+2).toInt)
                      val rs = v match {
                        case Some(s) => s
                        case None => ""
                      }
                      replaced += rs
                      i += 2
                    } else {
                      replaced += newstring(i)
                      i += 1
                    }
                  }
                }
                replaced += c_string.substring(lastEndIndex)
                AbsString.alpha(replaced)
              }
              case (Some(c_string), Nil, _) => string
              case _ => StrTop
            }
            val h_2 = Helper.ReturnStore(h_1, Value(s_replaced))
            ((h_2, ctx_1), (he_1, ctxe_1))
          } else {
            ((HeapBot, ContextBot), (he_1, ctxe_1))
          }
        }),

      ("String.prototype.slice" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          val n_start = Operator.ToInteger(getArgValue(h, ctx, args, "0"))
          val v_end = getArgValue(h, ctx, args, "1")
          val n_end =
            if (v_end._1._1 </ UndefBot) {
              val n = s_this match {
                case NumStrSingle(s) => AbsNumber.alpha(s.length)
                case OtherStrSingle(s) => AbsNumber.alpha(s.length)
                case StrBot => NumBot
                case _ => UInt
              }
              Operator.ToInteger(Value(PValue(UndefBot, v_end._1._2, v_end._1._3, n + v_end._1._4, v_end._1._5), v_end._2))
            }
            else
              Operator.ToInteger(v_end)
          val s = (AbsString.concretize(s_this),
            AbsNumber.concretize(n_start),
            AbsNumber.concretize(n_end)) match {
            case (Some(_s), Some(start), Some(end)) =>
              val from =
                if (start < 0) max(_s.length + start, 0).toInt
                else min(start, _s.length).toInt
              val to =
                if (end < 0) max(_s.length + end, 0).toInt
                else min(end, _s.length).toInt
              if (from >= to)
                AbsString.alpha("")
              else
                AbsString.alpha(_s.slice(from, to))
            case _ =>
              if (s_this <= StrBot || n_start <= NumBot || n_end <= NumBot)
                StrBot
              else
                StrTop
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),

      // Temporary implementation
      // 1) RegExp is not covered
      // 2) java split is not fully compatible with JS split
      // 3) Cover more corner cases (eg. when separator is undefined)
      "String.prototype.split" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))

          val v_arg = getArgValue(h, ctx, args, "0")
          val lset_1 = v_arg._2.filter(l => AbsString.alpha("RegExp") <= h(l)("@class")._1._2._1._5)
          val lset_2 = v_arg._2.filter(l => {
            val s = h(l)("@class")._1._2._1._5
            AbsString.alpha("RegExp") != s && s </ StrBot
          })
          if (!lset_1.isEmpty) {
            if (!Config.quietMode)
              System.err.println("* Warning: Semantics of the API function 'String.prototype.split(regexp)' are not defined.")
          }

          val s_separator = Helper.toString(Helper.toPrimitive(Value(v_arg._1, lset_2)))
          val v_limit = getArgValue(h, ctx, args, "1")

          if (s_this <= StrBot || s_separator <= StrBot || v_limit <= ValueBot)
            ((HeapBot, ContextBot), (he, ctxe))
          else {
            // allocate new location
            val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
            val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
            if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
            val addr_env = set_addr.head
            val addr1 = cfg.getAPIAddress(addr_env, 0)
            val l_r = addrToLoc(addr1, Recent)
            val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)

            // concretize string inputs
            val cs_this_opt = AbsString.concretize(s_this)
            val cs_separator_opt = AbsString.concretize(s_separator)
            val limit_undefined = v_limit <= Value(UndefTop)

            // make output array
            val o_array =
              (cs_this_opt, cs_separator_opt, limit_undefined) match {
                case (Some(cs_this), Some(cs_separator), true) =>
                  val splitted = cs_this.split("\\Q" + cs_separator + "\\E")
                  var obj_new = Helper.NewArrayObject(AbsNumber.alpha(splitted.length))
                  for (i <- 0 until splitted.length) {
                    val value = Value(AbsString.alpha(splitted(i)))
                    obj_new = obj_new.update(i.toString, PropValue(ObjectValue(value, T, T, T)))
                  }
                  obj_new
                case _ =>
                  val obj_new = Helper.NewArrayObject(UInt)
                  obj_new.update(NumStr, PropValue(ObjectValue(Value(StrTop), T, T, T)))
              }
            val h_2 = h_1.update(l_r, o_array)
            ((Helper.ReturnStore(h_2, Value(l_r)), ctx_1), (he, ctxe))
          }
        }),

      ("String.prototype.substring" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          val n_start = Operator.ToInteger(getArgValue(h, ctx, args, "0"))
          val v_end = getArgValue(h, ctx, args, "1")
          val n_end =
            if (v_end._1._1 </ UndefBot) {
              val n = s_this match {
                case NumStrSingle(s) => AbsNumber.alpha(s.length)
                case OtherStrSingle(s) => AbsNumber.alpha(s.length)
                case StrBot => NumBot
                case _ => UInt
              }
              Operator.ToInteger(Value(PValue(UndefBot, v_end._1._2, v_end._1._3, n + v_end._1._4, v_end._1._5), v_end._2))
            }
            else
              Operator.ToInteger(v_end)
          val s = (AbsString.concretize(s_this),
            AbsNumber.concretize(n_start),
            AbsNumber.concretize(n_end)) match {
            case (Some(_s), Some(start), Some(end)) =>
              val finalStart =
                if (start.isNaN || start < 0) min(0, _s.length)
                else min(start, _s.length)
              val finalEnd =
                if (end.isNaN || end < 0)  min(0, _s.length)
                else min(end, _s.length)
              val from = min(finalStart, finalEnd).toInt
              val to = max(finalStart, finalEnd).toInt
              AbsString.alpha(_s.substring(from, to))
            case _ =>
              if (s_this <= StrBot || n_start <= NumBot || n_end <= NumBot)
                StrBot
              else
                StrTop
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),

      ("String.prototype.substr" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          // 1. Call ToString, giving it the this value as its argument.
          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          // 2. Call ToInteger(start).
          val n_start = Operator.ToInteger(getArgValue(h, ctx, args, "0"))
          val v_length = getArgValue(h, ctx, args, "1")
          // 3. If length is undefined, use +∞; otherwise call ToInteger(length).
          val n_length =
            if (v_length._1._1 </ UndefBot) {
              PosInf             
            }
            else
              Operator.ToInteger(v_length)
          // 4. Compute the number of characters in Result(1).
          val n_len = s_this match {
                case NumStrSingle(s) => AbsNumber.alpha(s.length)
                case OtherStrSingle(s) => AbsNumber.alpha(s.length)
                case StrBot => NumBot
                case _ => UInt
              }

          val s = (AbsString.concretize(s_this),
            AbsNumber.concretize(n_start),
            AbsNumber.concretize(n_length), AbsNumber.concretize(n_len)) match {
            case (Some(_s), Some(start), Some(length), Some(len)) =>
              // 5. If Result(2) is positive or zero, use Result(2); else use max(Result(4)+Result(2),0).
              val finalStart =
                if (start < 0) max(len+start, 0)
                else start
              // 6. Compute min(max(Result(3),0), Result(4)–Result(5)).
              val finalLength = min(max(length, 0), len-finalStart)
              // 7. If Result(6) ≤ 0, return the empty String “”.
              if(finalLength <= 0) AbsString.alpha("")
              // 8. Return a String containing Result(6) consecutive characters from Result(1) beginning with the character at
              //   position Result(5).
              else {               
                val from = finalStart.toInt
                val to = (finalStart + finalLength).toInt
                AbsString.alpha(_s.substring(from, to))
              }
            case _ =>
              if (s_this <= StrBot || n_start <= NumBot || n_length <= NumBot || n_len <= NumBot)
                StrBot
              else
                StrTop
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),

      ("String.prototype.toLowerCase" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.toLowerCase)
            case None => s_this
          }
          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("String.prototype.toLocaleLowerCase" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.toLowerCase)
            case None => s_this
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("String.prototype.toUpperCase" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.toUpperCase)
            case None => s_this
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("String.prototype.toLocaleUpperCase" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.toUpperCase)
            case None => s_this
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("String.prototype.trim" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = Helper.toString(Helper.toPrimitive(v_this))
          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.trim)
            case None =>
              if (s_this <= StrBot)
                s_this
              else
                StrTop
          }

          if (s </ StrBot)
            ((Helper.ReturnStore(h, Value(s)), ctx), (he, ctxe))
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }))
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("String" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          // 15.5.1.1 String( [value] )
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))
          val s = n_arglen match {
            case UIntSingle(n) if n == 0 =>
              AbsString.alpha("")
            case UIntSingle(n) if n > 0 =>
              PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
            case NumBot => StrBot
            case _ => StrTop
          }
          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.constructor" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // 15.5.2.1 new String( [value] )
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))
          val s = n_arglen match {
            case UIntSingle(n) if n == 0 =>
              AbsString.alpha("")
            case UIntSingle(n) if n > 0 =>
              PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
            case NumBot => StrBot
            case _ => StrTop
          }
          val h_1 = lset_this.foldLeft(h)((_h, l) => _h.update(l, PreHelper.NewString(s)))
          if (s </ StrBot)
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, Value(lset_this)), ctx), (he, ctxe))
          else
            ((h_1, ctx), (he, ctxe))
        })),
      ("String.fromCharCode" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          // 15.5.3.2 String.fromCharCode( [char0 [, char1[, ...]]] )
          val arg_length = getArgValue_pre(h, ctx, args, "length", PureLocalLoc)._1._4
          val value_1 =
            if (AbsNumber.alpha(0) <= arg_length) Value(AbsString.alpha(""))
            else ValueBot
          val value_2 =
            if (arg_length </ NumBot) {
              AbsNumber.concretize(arg_length) match {
                case Some(n) => {
                  val s = (0 until n.toInt).foldLeft(AbsString.alpha(""))((s, i) => {
                    val v = Operator.ToUInt16(getArgValue_pre(h, ctx, args, i.toString, PureLocalLoc))
                    s.concat(AbsString.fromCharCode(v))
                  })
                  Value(s)
                }
                case None => Value(StrTop)
              }
            } else {
              ValueBot
            }
          val value = value_1 + value_2
          if (value </ ValueBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, value), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.toString" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val es =
            if (lset_this.exists((l) => {
              val v = h(l)("@class")._1._2._1._5
              v != AbsString.alpha("String") && v </ StrBot
            }))
              Set[Exception](TypeError)
            else
              ExceptionBot
          val lset_string = lset_this.filter((l) => AbsString.alpha("String") <= h(l)("@class")._1._2._1._5)
          val v = lset_string.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

          val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)
          if (v </ ValueBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, v), ctx), (he + h_e, ctxe + ctx_e))
          else
            ((h, ctx), (he + h_e, ctxe + ctx_e))
        })),
      ("String.prototype.valueOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val es =
            if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("String")))
              Set[Exception](TypeError)
            else
              ExceptionBot
          val lset_string = lset_this.filter((l) => AbsString.alpha("String") <= h(l)("@class")._1._2._1._5)
          val v = lset_string.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

          val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)
          if (v </ ValueBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, v), ctx), (he + h_e, ctxe + ctx_e))
          else
            ((h, ctx), (he + h_e, ctxe + ctx_e))
        })),
      ("String.prototype.charAt" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // 15.5.4.4 String.prototype.charAt(pos)
          val n_pos = Operator.ToInteger(getArgValue_pre(h, ctx, args, "0", PureLocalLoc))

          // 1. Call CheckObjectCoercible passing the this value as its argument.
          //   Don't need to check this because <>getBase always returns a location which points to an object.
          // Let S be the result of calling ToString, giving it the this value as its argument.]
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))
          val n_size = s_this.length()

          val value_1 =
            if (BoolTrue <= (n_pos < AbsNumber.alpha(0))) Value(AbsString.alpha(""))
            else ValueBot
          val value_2 =
            if (BoolTrue <= (n_size < n_pos) || BoolTrue <= (n_size === n_pos))  Value(AbsString.alpha(""))
            else ValueBot
          val value_3 = Value(s_this.charAt(n_pos))
          val value = value_1 + value_2 + value_3

          if (value </ ValueBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, value), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.charCodeAt" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // 15.5.4.5 String.prototype.charCodeAt(pos)
          val n_pos = Operator.ToInteger(getArgValue_pre(h, ctx, args, "0", PureLocalLoc))

          // 1. Call CheckObjectCoercible passing the this value as its argument.
          //   Don't need to check this because <>getBase always returns a location which points to an object.
          // Let S be the result of calling ToString, giving it the this value as its argument.
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)

          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))
          val n_size = s_this.length()

          val value_1 =
            if (BoolTrue <= (n_pos < AbsNumber.alpha(0))) Value(NaN)
            else ValueBot
          val value_2 =
            if (BoolTrue <= (n_size < n_pos) || BoolTrue <= (n_size === n_pos))  Value(NaN)
            else ValueBot
          val value_3 = Value(s_this.charCodeAt(n_pos))
          val value = value_1 + value_2 + value_3

          if (value </ ValueBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, value), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.concat" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue_pre(h, ctx, args, "length", PureLocalLoc))
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))
          val s = n_arglen match {
            case UIntSingle(n) if n == 0 => s_this
            case UIntSingle(n) if n > 0 =>
              (0 until n.toInt).foldLeft(s_this)((_s, i) =>
                _s.concat(PreHelper.toString(PreHelper.toPrimitive((getArgValue(h, ctx, args, i.toString))))))
            case NumBot => StrBot
            case _ => StrTop
          }

          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.indexOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))

          val s_search = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
          val n_pos = Operator.ToInteger(getArgValue_pre(h, ctx, args, "1", PureLocalLoc))

          val n = (AbsString.concretize(s_this), AbsString.concretize(s_search),AbsNumber.concretize(n_pos)) match {
            case (Some(ss_this), Some(ss_search), Some(nn_pos)) =>
              AbsNumber.alpha(ss_this.indexOf(ss_search, nn_pos.toInt).toDouble)
            case _ =>
              if (s_this <= StrBot || s_search <= StrBot || n_pos <= NumBot)
                NumBot
              else
                NumTop
          }

          if (n </ NumBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(n)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.lastIndexOf" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))

          val s_search = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
          val v_pos = getArgValue_pre(h, ctx, args, "1", PureLocalLoc)
          val v_pos1 =
            if (v_pos._1._1 </ UndefBot)
              Value(PValue(UndefBot, v_pos._1._2, v_pos._1._3, PosInf + v_pos._1._4, v_pos._1._5), v_pos._2)
            else v_pos
          val n_pos = Operator.ToInteger(v_pos1)

          val n = (AbsString.concretize(s_this), AbsString.concretize(s_search),AbsNumber.concretize(n_pos)) match {
            case (Some(ss_this), Some(ss_search), Some(nn_pos)) =>
              AbsNumber.alpha(ss_this.lastIndexOf(ss_search, nn_pos.toInt).toDouble)
            case _ =>
              if (s_this <= StrBot || s_search <= StrBot || n_pos <= NumBot)
                NumBot
              else
                NumTop
          }
          if (n </ NumBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(n)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.localeCompare" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))
          val s_that = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
          val n = (AbsString.concretize(s_this), AbsString.concretize(s_that)) match {
            case (Some(s_this), Some(s_that)) =>
              AbsNumber.alpha(s_this.compare(s_that).toDouble)
            case _ =>
              if (s_this <= StrBot || s_that <= StrBot)
                NumBot
              else
                NumTop
          }

          if (n </ NumBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(n)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.slice" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))
          val n_start = Operator.ToInteger(getArgValue_pre(h, ctx, args, "0", PureLocalLoc))
          val v_end = getArgValue_pre(h, ctx, args, "1", PureLocalLoc)
          val n_end =
            if (v_end._1._1 </ UndefBot) {
              val n = s_this match {
                case NumStrSingle(s) => AbsNumber.alpha(s.length)
                case OtherStrSingle(s) => AbsNumber.alpha(s.length)
                case StrBot => NumBot
                case _ => UInt
              }
              Operator.ToInteger(Value(PValue(UndefBot, v_end._1._2, v_end._1._3, n + v_end._1._4, v_end._1._5), v_end._2))
            }
            else
              Operator.ToInteger(v_end)
          val s = (AbsString.concretize(s_this),
            AbsNumber.concretize(n_start),
            AbsNumber.concretize(n_end)) match {
            case (Some(_s), Some(start), Some(end)) =>
              val from =
                if (start < 0) max(_s.length + start, 0).toInt
                else min(start, _s.length).toInt
              val to =
                if (end < 0) max(_s.length + end, 0).toInt
                else min(end, _s.length).toInt
              if (from >= to)
                AbsString.alpha("")
              else
                AbsString.alpha(_s.slice(from, to))
            case _ =>
              if (s_this <= StrBot || n_start <= NumBot || n_end <= NumBot)
                StrBot
              else
                StrTop
          }

          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.substring" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))
          val n_start = Operator.ToInteger(getArgValue_pre(h, ctx, args, "0", PureLocalLoc))
          val v_end = getArgValue_pre(h, ctx, args, "1", PureLocalLoc)
          val n_end =
            if (v_end._1._1 </ UndefBot) {
              val n = s_this match {
                case NumStrSingle(s) => AbsNumber.alpha(s.length)
                case OtherStrSingle(s) => AbsNumber.alpha(s.length)
                case StrBot => NumBot
                case _ => UInt
              }
              Operator.ToInteger(Value(PValue(UndefBot, v_end._1._2, v_end._1._3, n + v_end._1._4, v_end._1._5), v_end._2))
            }
            else
              Operator.ToInteger(v_end)
          val s = (AbsString.concretize(s_this),
            AbsNumber.concretize(n_start),
            AbsNumber.concretize(n_end)) match {
            case (Some(_s), Some(start), Some(end)) =>
              val finalStart =
                if (start.isNaN || start < 0) min(0, _s.length)
                else min(start, _s.length)
              val finalEnd =
                if (end.isNaN || end < 0)  min(0, _s.length)
                else min(end, _s.length)
              val from = min(finalStart, finalEnd).toInt
              val to = max(finalStart, finalEnd).toInt
              AbsString.alpha(_s.substring(from, to))
            case _ =>
              if (s_this <= StrBot || n_start <= NumBot || n_end <= NumBot)
                StrBot
              else
                StrTop
          }

          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.toLowerCase" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.toLowerCase)
            case None => s_this
          }
          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.toLocaleLowerCase" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.toLowerCase)
            case None => s_this
          }

          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.toUpperCase" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.toUpperCase)
            case None => s_this
          }

          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.toLocaleUpperCase" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.toUpperCase)
            case None => s_this
          }

          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        })),
      ("String.prototype.trim" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_this = h(PureLocalLoc)("@this")._1._2._2
          // [[default Value]] ??
          val lset_prim = lset_this.filter((l) => BoolTrue <= h(l).domIn("@primitive"))
          // TODO: v_this must be the result of [[DefaultValue]](string)
          val v_this = lset_prim.foldLeft(ValueBot)((_v, l) => _v + h(l)("@primitive")._1._2)
          val s_this = PreHelper.toString(PreHelper.toPrimitive(v_this))

          val s = AbsString.concretize(s_this) match {
            case Some(_s) => AbsString.alpha(_s.trim)
            // "  12345  " is considered as OtherStr.
            case None => s_this + NumStr
          }

          if (s </ StrBot)
            ((PreHelper.ReturnStore(h, PureLocalLoc, Value(s)), ctx), (he, ctxe))
          else
            ((h, ctx), (he, ctxe))
        }))
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("String" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.constructor" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val s = n_arglen match {
            case UIntSingle(n) if n == 0 =>
              AbsString.alpha("")
            case UIntSingle(n) if n > 0 =>
              Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
            case NumBot => StrBot
            case _ => StrTop
          }
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) =>
            AH.NewString_def(s).foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
          LP1 + (SinglePureLocalLoc, "@return")
        })),
      ("String.fromCharCode" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.toString" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val es =
            if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("String")))
              Set[Exception](TypeError)
            else
              ExceptionBot
          AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
        })),
      ("String.prototype.valueOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val es =
            if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("String")))
              Set[Exception](TypeError)
            else
              ExceptionBot
          AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
        })),
      ("String.prototype.charAt" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.charCodeAt" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.concat" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.indexOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.lastIndexOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.localeCompare" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.slice" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.substring" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.toLowerCase" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.toLocaleLowerCase" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.toUpperCase" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.toLocaleUpperCase" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("String.prototype.trim" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }))
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      ("String" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 = n_arglen match {
            case UIntSingle(n) if n == 0 => LPBot
            case UIntSingle(n) if n > 0 => getArgValue_use(h, ctx, args, "0")
            case UInt | NumTop => getArgValue_use(h, ctx, args, "0")
            case _ => LPBot
          }
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
        })),
      ("String.constructor" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 = n_arglen match {
            case UIntSingle(n) if n > 0 => getArgValue_use(h, ctx, args, "0")
            case _ => LPBot
          }
          /* may def */
          val s = n_arglen match {
            case UIntSingle(n) if n == 0 =>
              AbsString.alpha("")
            case UIntSingle(n) if n > 0 =>
              Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
            case NumBot => StrBot
            case _ => StrTop
          }

          val LP3 = lset_this.foldLeft(LPBot)((lpset, l) =>
            AH.NewString_def(s).foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
          LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.fromCharCode" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val arg_length = getArgValue(h, ctx, args, "length")._1._4
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 =
            if (arg_length </ NumBot) {
              AbsNumber.concretize(arg_length) match {
                case Some(n) =>
                  (0 until n.toInt).foldLeft(LPBot)((lpset, i) => getArgValue_use(h, ctx, args, i.toString))
                case None =>
                  if (arg_length <= NumBot) LPBot
                  else getArgValueAbs_use(h, ctx, args, NumStr)
              }
            } else {
              LPBot
            }
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
        })),
      ("String.prototype.toString" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val es =
            if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("String")))
              Set[Exception](TypeError)
            else
              ExceptionBot
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
          val lset_string = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == AbsString.alpha("String"))
          val LP2 = lset_string.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 ++ AH.RaiseException_use(es) + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.valueOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val es =
            if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("String")))
              Set[Exception](TypeError)
            else
              ExceptionBot
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
          val lset_string = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == AbsString.alpha("String"))
          val LP2 = lset_string.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 ++ AH.RaiseException_use(es) + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.charAt" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.charCodeAt" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.concat" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, args, "length"))
          val LP1 = getArgValue_use(h, ctx, args, "length")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          val LP3 = n_arglen match {
            case UIntSingle(n) if n == 0 => LPBot
            case UIntSingle(n) if n > 0 =>
              (0 until n.toInt).foldLeft(LPBot)((lpset, i) => getArgValue_use(h, ctx, args, i.toString))
            case UInt | NumTop => getArgValueAbs_use(h, ctx, args, NumStr)
            case _ => LPBot
          }
          LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.localeCompare" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "0")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.indexOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.lastIndexOf" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.slice" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.substring" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1")
          val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.toLowerCase" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.toLocaleLowerCase" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.toUpperCase" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.toLocaleUpperCase" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        })),
      ("String.prototype.trim" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
          LP1 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
        }))
    )
  }
}
