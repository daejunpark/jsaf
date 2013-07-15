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
    ("length",                   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1), F, F, F))))
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
     // imprecise semantics
     ("RegExp.prototype.exec" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          // allocate new location 
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r = addrToLoc(addr1, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
 
          // argument value
          val argVal = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          if(argVal </ StrBot) {
            val newobj = Helper.NewArrayObject(UInt)
              .update("index", PropValue(ObjectValue(UInt, T, T, T)))
              .update("input", PropValue(ObjectValue(argVal, T, T, T)))
              .update("@default_number", PropValue(ObjectValue(StrTop, T, T, T)))
            val h_2 = h_1.update(l_r, newobj)
            ((Helper.ReturnStore(h_2, Value(NullTop) + Value(l_r)), ctx_1), (he, ctxe))
          }
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
