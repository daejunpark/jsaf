/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import java.io.File
import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.interpreter.InterpreterPredefine
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.analysis.typing.models._
import scala.util.parsing.json.JSONObject
import kr.ac.kaist.jsaf.analysis.typing.{AccessHelper=>AH}
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

import scala.math.{min,max,floor,abs}

object AccessBuiltin {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address, addr3: Address, addr4: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)
    
    val object_name = 
      if (fun.indexOf('.') == -1)
        fun
      else
        fun.take(fun.indexOf('.'))
    
    object_name match  {
      case "Global"         => AccessBuiltinGlobal.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "Object"         => AccessBuiltinObject.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "Function"       => AccessBuiltinFunction.builtinCall_def(h, ctx, fun, args, addr1, addr2, addr3,  addr4)
      case "Array"          => AccessBuiltinArray.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "String"         => AccessBuiltinString.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "Boolean"        => AccessBuiltinBoolean.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "Number"         => AccessBuiltinNumber.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "Math"           => AccessBuiltinMath.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "Date"           => AccessBuiltinDate.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "RegExp"         => AccessBuiltinRegExp.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "Error"          => AccessBuiltinError.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "EvalError"      => AccessBuiltinError.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "RangeError"     => AccessBuiltinError.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "ReferenceError" => AccessBuiltinError.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "SyntaxError"    => AccessBuiltinError.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "TypeError"      => AccessBuiltinError.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "URIError"       => AccessBuiltinError.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case "JSON"           => AccessBuiltinJSON.builtinCall_def(h, ctx, fun, args, addr1, addr2)
      case _                => LPBot
    }
  }
  
  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address, addr3: Address, addr4: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)
    
    val object_name = 
      if (fun.indexOf('.') == -1)
        fun
      else
        fun.take(fun.indexOf('.'))
    
    object_name match  {
      case "Global"         => AccessBuiltinGlobal.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "Object"         => AccessBuiltinObject.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "Function"       => AccessBuiltinFunction.builtinCall_use(h, ctx, fun, args, addr1, addr2, addr3, addr4)
      case "Array"          => AccessBuiltinArray.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "String"         => AccessBuiltinString.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "Boolean"        => AccessBuiltinBoolean.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "Number"         => AccessBuiltinNumber.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "Math"           => AccessBuiltinMath.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "Date"           => AccessBuiltinDate.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "RegExp"         => AccessBuiltinRegExp.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "Error"          => AccessBuiltinError.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "EvalError"      => AccessBuiltinError.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "RangeError"     => AccessBuiltinError.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "ReferenceError" => AccessBuiltinError.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "SyntaxError"    => AccessBuiltinError.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "TypeError"      => AccessBuiltinError.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "URIError"       => AccessBuiltinError.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case "JSON"           => AccessBuiltinJSON.builtinCall_use(h, ctx, fun, args, addr1, addr2)
      case _                => LPBot
    }
  }
}  
  
  
object AccessBuiltinGlobal {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    
    fun match {
      /* global */
      case "Global.parseInt" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Global.encodeURIComponent" => {
        val es = Set[Exception](URIError)
        val LP1 = AH.RaiseException_def(es)
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2
      }
      case "Global.isNaN" | "Global.isFinite" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Global.alert" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      /* global */
      case "Global.parseInt" => {
        getArgValue_use(h, ctx, "0") ++
        getArgValue_use(h, ctx, "1") + (SinglePureLocalLoc, "@return")
      }
      case "Global.encodeURIComponent" => {
        val es = Set[Exception](URIError)
        AH.RaiseException_use(es) + (SinglePureLocalLoc, "@return")
      }
      case "Global.isNaN" | "Global.isFinite" => {
        getArgValue_use(h, ctx, "0") + (SinglePureLocalLoc, "@return")
      }
      case "Global.alert" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1") + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinObject {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)
        
    fun match {
      /* Object */
      case "Object" => {
        val v = getArgValue(h, ctx, "0") // [value]
        val (lpset1, es) =
          if ((v._1._3 </ BoolBot) || (v._1._4 </ NumBot) || (v._1._5 </ StrBot)) {
            val _es =
              if (!(v._1._1 <= UndefBot) || !(v._1._2 <= NullBot)) Set[Exception](TypeError)
              else ExceptionBot
            val _v_new = Value(PValue(UndefBot, NullBot, v._1._3, v._1._4, v._1._5))
            (AH.toObject_def(h,ctx, _v_new , addr1), _es)
          } else (LPBot, ExceptionBot)
        val LP2 =
          if ((v._1._1 </ UndefBot) || (v._1._2 </ NullBot)) {
            val _l_r = addrToLoc(addr1, Recent)
            AH.Oldify_def(h, ctx, addr1) ++
            AH.NewObject_def.foldLeft(LPBot)((S,p) => S + ((_l_r, p)))
          } else LPBot
        val LP_3 = LPSet((SinglePureLocalLoc, "@return"))
        val LP_4 = AH.RaiseException_def(es)
        lpset1 ++ LP2 ++ LP_3 ++ LP_4
      }
      case "Object.constructor" => {
        val v = getArgValue(h, ctx, "0") // [value]
        val (lpset1, es) =
          if ((v._1._3 </ BoolBot) || (v._1._4 </ NumBot) || (v._1._5 </ StrBot)) {
            val _es =
              if (!(v._1._1 <= UndefBot) || !(v._1._2 <= NullBot)) Set[Exception](TypeError)
              else ExceptionBot
            val _v_new = Value(PValue(UndefBot, NullBot, v._1._3, v._1._4, v._1._5))
            val props1 =
		      if (_v_new._1._5 </ StrBot) AH.NewString_def(v._1._5)
		      else Set()
		    val props2 =
		      if (_v_new._1._3 </ BoolBot) AH.NewBoolean_def
		      else Set()
		    val props3 =
		      if (_v_new._1._4 </ NumBot) AH.NewNumber_def
		      else Set()
		    val props = props1 ++ props2 ++ props3
		    val lpset = 
		      lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ props.foldLeft(lpset)((_lpset, p) => _lpset + (l, p)))
            (lpset, _es)
          } else (LPBot, ExceptionBot)
        val LP2 =
          if ((v._1._1 </ UndefBot) || (v._1._2 </ NullBot)) {
            lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.NewObject_def.foldLeft(lpset)((_lpset, p) => _lpset + (l, p)))
          } else LPBot
          
        val LP_3 = LPSet((SinglePureLocalLoc, "@return"))
        val LP_4 = AH.RaiseException_def(es)
        lpset1 ++ LP2 ++ LP_3 ++ LP_4
      }
      case "Object.getPrototypeOf" => {
        val v_1 = getArgValue(h, ctx, "0")
        val es =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val v_2 = v_1._2.foldLeft(ValueBot)(
          (_v, l) => _v + h(l)("@proto")._1._1._1)
        val LP1 = AH.RaiseException_def(es)
        if (v_2 </ ValueBot) LP1 ++ LPSet((SinglePureLocalLoc, "@return"))
        else LP1
      }
      case "Object.getOwnPropertyDescriptor" => {
        val v_1 = getArgValue(h, ctx, "0")
        val es =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_def(h, ctx, addr1)
        val LP2 = LPSet((l_r, "value")) + (l_r, "writable") + (l_r, "enumerable") + (l_r, "configurable")
        val LP3 = AH.RaiseException_def(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.getOwnPropertyNames" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_def(h, ctx, addr1)
        val v = getArgValue(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) =>
          lpset ++ AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p) => _lpset + (l_r, p))
          ++ h(l).getProps.foldLeft((LPBot, 0))((_lpset_n, p) => 
            (_lpset_n._1 + (l, _lpset_n._2.toString), _lpset_n._2 + 1))._1)
        val LP3 = AH.RaiseException_def(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.create" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_def(h, ctx, addr1)
        val v_1 = getArgValue(h, ctx, "0")
        val es_1 = 
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val v_2 = getArgValue(h, ctx, "1")
        val es_2 = 
          if (v_2._1 </ PValueBot && n_arglen == UIntSingle(2)) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = AH.NewObject_def.foldLeft(LPBot)((lpset, p) => lpset + (l_r, p))
        val LP3 = 
          if (n_arglen == UIntSingle(2))
            v_2._2.foldLeft(LPBot)((lpset, l) => lpset ++ AH.DefineProperties_def(h, l_r, l))
          else
            LPBot
        val LP4 = AH.RaiseException_def(es_1 ++ es_2)
        LP1 ++ LP2 ++ LP3 ++ LP4 + (SinglePureLocalLoc, "@return")
      }
      case "Object.defineProperty" =>{
        val v_1 = getArgValue(h, ctx, "0")
        val es_1 = 
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val s_name = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "1")))
        val v_2 = getArgValue(h, ctx, "2")
        val es_2 = 
          if (v_2._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP1 = v_1._2.foldLeft(LPBot)((lpset, l_1) =>
          lpset ++ v_2._2.foldLeft(LPBot)((_lpset, l_2) =>
            _lpset ++ AH.DefineProperty_def(h, l_1, s_name, l_2)) )
        val LP2 = AH.RaiseException_def(es_1 ++ es_2)
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
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
        val LP1 = v_1._2.foldLeft(LPBot)((lpset, l_1) => 
          lpset ++ v_2._2.foldLeft(LPBot)((_lpset, l_2) => _lpset ++ AH.DefineProperties_def(h, l_1, l_2)))
        
        val LP2 = AH.RaiseException_def(es_1 ++ es_2)
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
	  case "Object.seal" => {
        val v = getArgValue(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP1 = v._2.foldLeft(LPBot)((lpset, l) => {
          val _LP1 = h(l).getProps.foldLeft(LPBot)((_lpset, p) => _lpset + (l, p))
          lpset ++ _LP1 + (l, "@extensible")
          })
        val LP2 = AH.RaiseException_def(es)
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
      case "Object.freeze" => {
        val v = getArgValue(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP1 = v._2.foldLeft(LPBot)((lpset, l) => {
          val _LP1 = h(l).getProps.foldLeft(LPBot)((_lpset, p) => _lpset + (l, p))
          lpset ++ _LP1 + (l, "@extensible")
          })
        val LP2 = AH.RaiseException_def(es)
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
      case "Object.preventExtensions" => {
        val v = getArgValue(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP1 = v._2.foldLeft(LPBot)((lpset, l) => lpset + (l, "@extensible"))
        val LP2 = AH.RaiseException_def(es)
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
	  case "Object.isSealed" | "Object.isFrozen" | "Object.isExtensible" => {
        val v = getArgValue(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
      }
      case "Object.keys" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_def(h, ctx, addr1)
        val v = getArgValue(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) =>
          lpset ++ AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p) => _lpset + (l_r, p))
          ++ h(l).getProps.foldLeft((LPBot, 0))((_lpset_n, p) => 
            (_lpset_n._1 + (l, _lpset_n._2.toString), _lpset_n._2 + 1))._1)
        val LP3 = AH.RaiseException_def(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.prototype.toString"
         | "Object.prototype.toLocaleString" 
         | "Object.prototype.valueOf" 
	     | "Object.prototype.hasOwnProperty" 
	     | "Object.prototype.isPrototypeOf" 
	     | "Object.prototype.propertyIsEnumerable" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
	  case _ => LPBot
    }
  }
   
  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      /* Object */
      case "Object" =>  {
        val v = getArgValue(h, ctx, "0") // [value]
        val LP1 = getArgValue_use(h, ctx, "0")
        val (lpset2, es) =
          if ((v._1._3 </ BoolBot) || (v._1._4 </ NumBot) || (v._1._5 </ StrBot)) {
            val _es =
              if (!(v._1._1 <= UndefBot) || !(v._1._2 <= NullBot)) Set[Exception](TypeError)
              else ExceptionBot
            val _v_new = Value(PValue(UndefBot, NullBot, v._1._3, v._1._4, v._1._5))
            (AH.toObject_use(h,ctx, _v_new , addr1), _es)
          } else (LPBot, ExceptionBot)
        val LP3 =
          if ((v._1._1 </ UndefBot) || (v._1._2 </ NullBot)) {
            AH.Oldify_use(h, ctx, addr1)
          } else LPBot
        val LP4 = LPSet((SinglePureLocalLoc, "@return"))
        val LP5 = AH.RaiseException_use(es)
        LP1 ++ lpset2 ++ LP3 ++ LP4 ++ LP5
      }
      case "Object.constructor" => {
        /* may def */
        val v = getArgValue(h, ctx, "0") // [value]
        val LP1 = getArgValue_use(h, ctx, "0")
        val (lpset2, es) =
          if ((v._1._3 </ BoolBot) || (v._1._4 </ NumBot) || (v._1._5 </ StrBot)) {
            val _es =
              if (!(v._1._1 <= UndefBot) || !(v._1._2 <= NullBot)) Set[Exception](TypeError)
              else ExceptionBot
            val _v_new = Value(PValue(UndefBot, NullBot, v._1._3, v._1._4, v._1._5))
            val props1 =
		      if (_v_new._1._5 </ StrBot) AH.NewString_def(v._1._5)
		      else Set()
		    val props2 =
		      if (_v_new._1._3 </ BoolBot) AH.NewBoolean_def
		      else Set()
		    val props3 =
		      if (_v_new._1._4 </ NumBot) AH.NewNumber_def
		      else Set()
		    val props = props1 ++ props2 ++ props3
		    val lpset = 
		      lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ props.foldLeft(lpset)((_lpset, p) => _lpset + (l, p)))
            (lpset, _es)
          } else (LPBot, ExceptionBot)
        val LP3 =
          if ((v._1._1 </ UndefBot) || (v._1._2 </ NullBot)) {
            lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.NewObject_def.foldLeft(lpset)((_lpset, p) => _lpset + (l, p)))
          } else LPBot
          
        val LP4 = LPSet((SinglePureLocalLoc, "@return"))
        val LP5 = AH.RaiseException_use(es)
        LP1 ++ lpset2 ++ LP3 ++ LP4 ++ LP5 + (SinglePureLocalLoc, "@this")
      }
      case "Object.getPrototypeOf" => {
        val v_1 = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val es =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v_1._2.foldLeft(LPBot)((lpset, l) => lpset + (l, "@proto"))
        val LP3 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.getOwnPropertyDescriptor" => {
        val v_1 = getArgValue(h, ctx, "0")
        val s_prop = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "1")))
        val LP1 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1")
        val es =
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v_1._2.foldLeft(LPBot)((lpset, l) => lpset ++ AH.absPair(h, l, s_prop))
        val LP3 = AH.Oldify_use(h, ctx, addr1)
        val LP4 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 ++ LP4 + (SinglePureLocalLoc, "@return")
      }
      case "Object.getOwnPropertyNames" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_use(h, ctx, addr1)
        val v = getArgValue(h, ctx, "0")
        val LP2 = getArgValue_use(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP3 = v._2.foldLeft(LPBot)((lpset, l) => {
          val props = h(l).getProps
          lpset ++ props.foldLeft(LPBot)((_lpset, p) => _lpset + (l, p)) + (l, "@default_number") + (l, "@default_number")
          })
        val LP4 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 ++ LP4 + (SinglePureLocalLoc, "@return")
      }
      case "Object.create" =>{
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_use(h, ctx, addr1)
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = getArgValue(h, ctx, "1")
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP2 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1") ++ getArgValue_use(h, ctx, "length")
        val es_1 = 
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val es_2 = 
          if (v_2._1 </ PValueBot && n_arglen == UIntSingle(2)) Set[Exception](TypeError)
          else ExceptionBot
        val LP3 = v_2._2.foldLeft(LPBot)((lpset, l_2) => lpset ++ AH.DefineProperties_use(h, l_r, l_2))
        val LP4 = AH.RaiseException_use(es_1 ++ es_2)
        LP1 ++ LP2 ++ LP3 ++ LP4 + (SinglePureLocalLoc, "@return")
      }
      case "Object.defineProperty" =>{
        val v_1 = getArgValue(h, ctx, "0")
        val s_name = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "1")))
        val v_2 = getArgValue(h, ctx, "2")
        val LP1 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1") ++ getArgValue_use(h, ctx, "2")
        val es_1 = 
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val es_2 = 
          if (v_2._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = 
          v_1._2.foldLeft(LPBot)((lpset, l_1) =>
            lpset ++ v_2._2.foldLeft(LPBot)((_lpset, l_2) => 
              _lpset ++ AH.DefineProperty_use(h, l_1, s_name, l_2)++ AH.DefineProperty_def(h, l_1, s_name, l_2)) )
        val LP3 = AH.RaiseException_use(es_1 ++ es_2)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.defineProperties" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = getArgValue(h, ctx, "1")
        val LP1 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1")
        val es_1 = 
          if (v_1._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val es_2 = 
          if (v_2._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = 
          v_1._2.foldLeft(LPBot)((lpset, l_1) =>
            lpset ++ v_2._2.foldLeft(LPBot)((_lpset, l_2) =>
              _lpset ++ AH.DefineProperties_use(h, l_1, l_2) ++ AH.DefineProperties_def(h, l_1, l_2)))
        val LP3 = AH.RaiseException_use(es_1 ++ es_2)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
	  case "Object.seal" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) =>
          lpset ++ h(l).getProps.foldLeft(LPBot)((_lpset, p) => _lpset + (l, p)) + (l, "@extensible"))
        val LP3 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.freeze" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) =>
          lpset ++ h(l).getProps.foldLeft(LPBot)((_lpset, p) => _lpset + (l, p)) + (l, "@extensible"))
        val LP3 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.preventExtensions" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) => lpset + (l, "@extensible"))
        val LP3 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
	  case "Object.isSealed" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) => {
            val props = h(l).getProps
            lpset ++ props.foldLeft(LPBot)((_lpset, p) => _lpset + (l, p)) + (l, "@extensible")
          })
        val LP3 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.isFrozen" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) => {
            val props = h(l).getProps
            lpset ++ props.foldLeft(LPBot)((_lpset, p) => _lpset + (l, p)) + (l, "@extensible")
          })
        val LP3 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.isExtensible" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) => lpset + (l, "@extensible"))
        val LP3 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Object.keys" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_use(h, ctx, addr1)
        val v = getArgValue(h, ctx, "0")
        val LP2 = getArgValue_use(h, ctx, "0")
        val es = 
          if (v._1 </ PValueBot) Set[Exception](TypeError)
          else ExceptionBot
        val LP3 = v._2.foldLeft(LPBot)((lpset, l) => {
          val props = h(l).getProps
          lpset ++ props.foldLeft(LPBot)((_lpset, p) => _lpset + (l, p)) + (l, "@default_number") + (l, "@default_number")
          })
        val LP4 = AH.RaiseException_use(es)
        LP1 ++ LP2 ++ LP3 ++ LP4 + (SinglePureLocalLoc, "@return")
      }
      case "Object.prototype.toString" |
           "Object.prototype.toLocaleString" => {
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
        LP1 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
	  case "Object.prototype.valueOf" => {
	    LPSet((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
	  }
	  case "Object.prototype.hasOwnProperty" => {
        val v = getArgValue(h, ctx, "0") // V
        val p = Helper.toString(Helper.toPrimitive(v))

        val LP_1 = getArgValue_use(h, ctx, "0")
        val LP_2 = lset_this.foldLeft(LPBot)((S, l) => S ++ AH.HasOwnProperty_use(h, l, p))

        LP_1 ++ LP_2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
	  case "Object.prototype.isPrototypeOf" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) => lpset + (l, "@proto"))
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "Object.prototype.propertyIsEnumerable" => {
        val s = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "0")))
        val LP1 = getArgValue_use(h, ctx, "0")
        val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.absPair(h, l, s))
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
	  case _ =>
	    getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinFunction {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address, addr3: Address, addr4: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)
    
    fun match {
      /* Function */
	  case "Function.constructor" => {
	    lset_this.foldLeft(LPBot)((lpset, l) =>
            lpset ++ AH.PropStore_def(h, l, OtherStrSingle("length")))
	  }
	  case "Function.prototype" => {
	    LPSet((SinglePureLocalLoc, "@return"))
	  }
	  case "Function.prototype.toString" => {
	    val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Function")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
	  }
	  case "Function.prototype.apply" => {
        val l_r1 = addrToLoc(addr1, Recent)
        val l_r2 = addrToLoc(addr2, Recent)
        val l_r3 = addrToLoc(addr3, Recent)
        val LP1 = AH.Oldify_def(h, ctx, addr1) ++ AH.Oldify_def(h, ctx, addr2) ++ AH.Oldify_def(h, ctx, addr3)

        val cond = lset_this.exists((l) => BoolFalse <= Helper.IsCallable(h,l))
        val es1 =
          if (cond) Set[Exception](TypeError)
          else Set[Exception]()
        
        val v_arg = getArgValue(h, ctx, "1")
        val v_arg1 = Value(PValue(UndefBot, NullBot, v_arg._1._3, v_arg._1._4, v_arg._1._5), v_arg._2)
        val (v_arg2, es2) =
          if (v_arg1._1 </ PValueBot)
            (Value(PValueBot, v_arg1._2), Set[Exception](TypeError))
          else 
            (v_arg1, Set[Exception]())
            
        val LP2 = 
          if (!v_arg2._2.isEmpty) {
            v_arg2._2.foldLeft(LPBot)((lpset, l) => {
              val n_arglen = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
              val _LP1 = n_arglen match {
                case NumBot => LPBot
                case UIntSingle(n_len) =>
                  (0 until n_len.toInt).foldLeft(LPBot)((_lpset, i) => _lpset + (l_r3, i.toString))
                case _ => AH.absPair(h, l_r3, NumStr)
              }
              lpset ++ _LP1
            })
          }
          else
            LPBot
        
        val v_this = getArgValue(h, ctx, "0")
        val lset_argthis = Helper.getThis(h, v_this)
        val v_this2 =  Value(PValue(UndefBot, NullBot, v_this._1._3, v_this._1._4, v_this._1._5), lset_argthis)
        val LP3 = AH.toObject_def(h, ctx, v_this2, addr4)
                     
        val LP4 = LPSet((l_r3, "callee"))
        
        val LP5 = AH.RaiseException_def(es1 ++ es2)
        
        LP1 ++ LP2 ++ LP3 ++ LP4 ++ LP5 + (SinglePureLocalLoc, "@return")
      }
      case "Function.prototype.call" => {
        val l_r1 = addrToLoc(addr1, Recent)
        val l_r2 = addrToLoc(addr2, Recent)
        val LP1 = AH.Oldify_def(h, ctx, addr1) ++ AH.Oldify_def(h, ctx, addr2)

        // 1.
        val cond = lset_this.exists((l) => BoolFalse <= Helper.IsCallable(h,l))
        val es =
          if (cond) Set[Exception](TypeError)
          else Set[Exception]()
        
        // 2., 3. create Arguments object
        val len = Operator.bopMinus(getArgValue(h, ctx, "length"), Value(AbsNumber.alpha(1)))
        val LP2 = AbsNumber.concretize(len._1._4) match {
            case Some(n) =>
              (0 until n.toInt).foldLeft(LPBot)((lpset, i) => lpset + (l_r1, i.toString))
            case None => LPBot
          }
        
        val v_this = getArgValue(h, ctx, "0")
        val lset_argthis = Helper.getThis(h, v_this)
        val v_this2 =  Value(PValue(UndefBot, NullBot, v_this._1._3, v_this._1._4, v_this._1._5), lset_argthis)
        val LP3 = AH.toObject_def(h, ctx, v_this2, addr4)
       
        val LP4 = LPSet((l_r1, "callee"))
        
        val LP5 = AH.RaiseException_def(es)

        LP1 ++ LP2 ++ LP3 ++ LP4 ++ LP5 + (SinglePureLocalLoc, "@return")
      }
	  case _ => LPBot
    }
  }
  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address, addr3: Address, addr4: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {          
      /* Function */
	  case "Function.constructor" => {
	    lset_this.foldLeft(LPBot)((lpset, l) =>
            lpset ++ AH.PropStore_use(h, l, OtherStrSingle("length"))) + (SinglePureLocalLoc, "@this")
	  }
	  case "Function.prototype" => {
	    LPSet((SinglePureLocalLoc, "@return"))
	  }
	  case "Function.prototype.toString" => {
	    val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Function")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
        val LP2 = AH.RaiseException_use(es) 
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
	  }
	  case "Function.prototype.apply" => {
        val l_r1 = addrToLoc(addr1, Recent)
        val l_r2 = addrToLoc(addr2, Recent)
        val l_r3 = addrToLoc(addr3, Recent)
        val LP1 = AH.Oldify_use(h, ctx, addr1) ++ AH.Oldify_use(h, ctx, addr2) ++ AH.Oldify_use(h, ctx, addr3)

        // 1.
        val cond = lset_this.exists((l) => BoolFalse <= Helper.IsCallable(h,l))
        val es1 =
          if (cond) Set[Exception](TypeError)
          else Set[Exception]()
        val lset_f = lset_this.filter((l) => BoolTrue <= Helper.IsCallable(h,l))

        // 2. create empty Arguments object
        val v_arg = getArgValue(h, ctx, "1")
        val LP2 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1")
        
        // 3.
        val v_arg1 = Value(PValue(UndefBot, NullBot, v_arg._1._3, v_arg._1._4, v_arg._1._5), v_arg._2)
        val (v_arg2, es2) =
          if (v_arg1._1 </ PValueBot)
            (Value(PValueBot, v_arg1._2), Set[Exception](TypeError))
          else 
            (v_arg1, Set[Exception]())
            
        // 4. ~ 8. create Arguments object with argArray
        val LP3 = 
          if (!v_arg2._2.isEmpty) {
            v_arg2._2.foldLeft(LPBot)((lpset, l) => {
              val n_arglen = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
              val _LP1 = AH.Proto_use(h, l, AbsString.alpha("length"))
              val _LP2 = n_arglen match {
                case UIntSingle(n_len) =>
                  (0 until n_len.toInt).foldLeft(LPBot)((_lpset, i) =>
                    _lpset ++ AH.Proto_use(h, l, AbsString.alpha(i.toString)))
                case NumBot => LPBot
                case _ => AH.Proto_use(h, l, NumStr)
              }
              lpset ++ _LP1 ++ _LP2
            })
          }
          else
            LPBot
        
        val v_arg3 = Value(l_r3)
        
        // *  in our own semantics, this value should be object
        val v_this = getArgValue(h, ctx, "0")
        val lset_argthis = Helper.getThis(h, v_this)
        val v_this2 =  Value(PValue(UndefBot, NullBot, v_this._1._3, v_this._1._4, v_this._1._5), lset_argthis)
        val LP4 = AH.toObject_use(h, ctx, v_this2, addr4) ++ AH.getThis_use(h, v_this)
        

        val LP5 = lset_f.foldLeft(LPBot)((lpset, l_f) => lpset + (l_f, "@function") + (l_f, "@scope"))
        
        val LP6 = AH.RaiseException_def(es1 ++ es2)

        LP1 ++ LP2 ++ LP3 ++ LP4 ++ LP5 ++ LP6 + (SinglePureLocalLoc, "@return") + (ContextLoc, "1") + (SinglePureLocalLoc, "@this")
      }
      case "Function.prototype.call" => {
        val l_r1 = addrToLoc(addr1, Recent)
        val l_r2 = addrToLoc(addr2, Recent)
        val LP1 = AH.Oldify_use(h, ctx, addr1) ++ AH.Oldify_use(h, ctx, addr2)

        // 1.
        val cond = lset_this.exists((l) => BoolFalse <= Helper.IsCallable(h,l))
        val es =
          if (cond) Set[Exception](TypeError)
          else Set[Exception]()
        val lset_f = lset_this.filter((l) => BoolTrue <= Helper.IsCallable(h,l))
        
        // 2., 3. create Arguments object
        val len = Operator.bopMinus(getArgValue(h, ctx, "length"), Value(AbsNumber.alpha(1)))
        val LP2 = getArgValue_use(h, ctx, "length") ++ getArgValue_use(h, ctx, "0")
        val LP3 =
          AbsNumber.concretize(len._1._4) match {
            case Some(n) =>
              (0 until n.toInt).foldLeft(LPBot)((lpset, i) =>
                lpset ++ getArgValue_use(h, ctx, (i+1).toString))
            case None => LPBot
          }
        val v_arg = Value(l_r1)
        
        val v_this = getArgValue(h, ctx, "0")
        val lset_argthis = Helper.getThis(h, v_this)
        val v_this2 =  Value(PValue(UndefBot, NullBot, v_this._1._3, v_this._1._4, v_this._1._5), lset_argthis)
        val LP4 = AH.toObject_use(h, ctx, v_this2, addr4) ++ AH.getThis_use(h, v_this)
        
        val LP5 = lset_f.foldLeft(LPBot)((lpset, l_f) => lpset + (l_f, "@function") + (l_f, "@scope"))
          
        val LP6 = AH.RaiseException_def(es)

        LP1 ++ LP2 ++ LP3 ++ LP4 ++ LP5 ++ LP6 + (SinglePureLocalLoc, "@return") + (ContextLoc, "1") + (SinglePureLocalLoc, "@this")
      }
	  case _ =>
	    getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinArray {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    def getArgValueAbs(h : Heap, ctx: Context, s : AbsString): Value = {
      val lset = SE.V(args,h,ctx)._1._2
      val v = lset.foldLeft(ValueBot)((v_1, l) => v_1 + Helper.Proto(h,l,s))
      v
    }
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    
    fun match {
      /* Array */
      case "Array" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_def(h,ctx,addr1)
        val v_1 = getArgValue(h, ctx, "0")
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
       
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
        val LP2 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, prop) => _lpset + (l_r, prop))
        val LP3 = AH.RaiseException_def(es2)
        
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Array.constructor" => {
        val v_1 = getArgValue(h, ctx, "0")
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
       
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
      }
      
      case "Array.isArray" 
         | "Array.prototype.toString"
         | "Array.prototype.toLocaleString" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Array.prototype.concat" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_def(h,ctx,addr1)
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
       
        val o = n_arglen match {
          case NumBot => ObjBot
          case UIntSingle(n_arg) => {
            val elem_list = (0 until n_arg.toInt).foldLeft[List[Value]](List(Value(lset_this)))((list, i) =>
              list :+ getArgValue(h, ctx, i.toString))
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
            val v_all = Value(lset_this) + getArgValueAbs(h, ctx, NumStr)
            val lset_array = v_all._2.filter((l) => AbsString.alpha("Array") <= h(l)("@class")._1._2._1._5)
            val v_array = lset_array.foldLeft(ValueBot)((_v, l) => _v + Helper.Proto(h, l, NumStr))
            Helper.NewArrayObject(UInt).update(NumStr, PropValue(ObjectValue(v_all + v_array, BoolTrue,BoolTrue,BoolTrue)))
          }
        }
        
        val LP2 = o.map.keySet.foldLeft(LPBot)((lpset, x) =>
          if (!x.take(1).equals("@") && AbsString.alpha(x) <= NumStr)
            lpset +  (l_r, x)
          else lpset) + (l_r, "@default_number")
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
      case "Array.prototype.join" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Array.prototype.pop" => {
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
      }
      case "Array.prototype.push" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
       
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
      }
      case "Array.prototype.reverse" => {
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
      }
      case "Array.prototype.shift" => {
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
      }
      case "Array.prototype.slice" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_def(h, ctx, addr1)
        
        val n_start = Operator.ToInteger(getArgValue(h, ctx, "0"))
        val n_end = Operator.ToInteger(getArgValue(h, ctx, "1"))
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
		          val _LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) => lpset + (l_r, p))
		          (0 until span).foldLeft(_LP1)((lpset, i) => lpset + (l_r, i.toString))
                }
                case NumBot => LPBot
                case _ =>
                  AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) => lpset + (l_r, p))
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
                    AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) => lpset + (l_r, p))
                }
              })
            }
        }
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
      case "Array.prototype.splice" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_def(h, ctx, addr1)
        
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val n_start = Operator.ToInteger(getArgValue(h, ctx, "0"))
        val n_count = Operator.ToInteger(getArgValue(h, ctx, "1"))
        val LP2 = (AbsNumber.concretize(n_start), AbsNumber.concretize(n_count)) match {
          case (Some(start), Some(count)) =>
            lset_this.foldLeft(LPBot)((lpset, l) => {
              val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
              val _LP = n_len match {
                case UIntSingle(n_len) => {
		          val delCount = min(max(count, 0), n_len - start).toInt
		          val o_new = Helper.NewArrayObject(AbsNumber.alpha(delCount))
		          val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=> _lpset + (l_r, p))
		          val __LP2 = (0 until delCount).foldLeft(LPBot)((_lpset, i) => _lpset + (l_r, i.toString))
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
                  val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=> _lpset + (l_r, p))
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
                    val __LP1 = AH.NewArrayObject_def.foldLeft(LPBot)((_lpset, p)=> _lpset + (l_r, p))
                    val __LP2 = AH.PropStore_def(h, l, NumStr)
                    val __LP3 = AH.Delete_def(h, l, NumStr)
                    __LP1 ++ __LP2 ++ __LP3
                }
                _lpset ++ _LP
              })
            }
        }
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
      case "Array.prototype.unshift" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
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
      }
      case "Array.prototype.indexOf" 
         | "Array.prototype.lastIndexOf" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case _ => LPBot
    }
  }
  
  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    def getArgValueAbs(h : Heap, ctx: Context, s : AbsString): Value = {
      val lset = SE.V(args,h,ctx)._1._2
      val v = lset.foldLeft(ValueBot)((v_1, l) => v_1 + Helper.Proto(h,l,s))
      v
    }
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      /* Array */
      case "Array" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_use(h,ctx,addr1)
        val v_1 = getArgValue(h, ctx, "0")
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP2 = getArgValue_use(h, ctx, "length") ++ getArgValue_use(h, ctx, "0")
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
            (0 until n.toInt).foldLeft(LPBot)((_lpset, i) => _lpset + (l_r, i.toString))
          case NumBot => LPBot
          case _ =>  AH.absPair(h, l_r, NumStr)
        }
        val LP4 = AH.RaiseException_use(es2)
        LP1 ++ LP2 ++ LP3 ++ LP4 + ((SinglePureLocalLoc, "@return"))
      }
      case "Array.constructor" => {
        val v_1 = getArgValue(h, ctx, "0")
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP1 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "length")
        
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
      }
      case "Array.isArray" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = getArgValue_use(h, ctx, "0")
        val LP2 = v._2.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
        LP1 ++ LP2 + ((SinglePureLocalLoc, "@return"))
      }
      case "Array.prototype.toString"
         | "Array.prototype.toLocaleString" => {
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
      }
      case "Array.prototype.concat" => {
        val l_r = addrToLoc(addr1, Recent)
        val LP1 = AH.Oldify_use(h, ctx, addr1)
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP2 = getArgValue_use(h, ctx, "length")
        
        val LP3_this = 
          lset_this.foldLeft(LPBot)((lpset, l) => 
            lpset ++ AH.Proto_use(h,l, NumStr) ++ AH.Proto_use(h,l,AbsString.alpha("length")) + (l, "@class")) + (SinglePureLocalLoc, "@this")
        
        val LP3_arg = n_arglen match {
          case NumBot => LPBot
          case UIntSingle(n_arg) => {    
            (0 until n_arg.toInt).foldLeft(LPBot)((lpset, i) => {
              val v = getArgValue(h, ctx, i.toString)
              val _LP1 = getArgValue_use(h, ctx, i.toString)
              val _LP2 = v._2.foldLeft(LPBot)((lpset, l) => 
                lpset ++ AH.Proto_use(h,l, NumStr) ++ AH.Proto_use(h,l,AbsString.alpha("length")) + (l, "@class"))
              lpset ++ _LP1 ++ _LP2
            })
          }
          case _ => {
            val v_all = getArgValueAbs(h, ctx, NumStr)
            val _LP1 = getArgValueAbs_use(h, ctx, NumStr)
            val _LP2 = v_all._2.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h, l, NumStr) + (l, "@class"))
            _LP1 ++ _LP2
          }
        }
        
        LP1 ++ LP2 ++ LP3_this ++ LP3_arg + ((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
      }
      case "Array.prototype.join" => {
        val LP1 = getArgValue_use(h, ctx, "0")
        
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
      }
      case "Array.prototype.pop" => {
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
      }
      case "Array.prototype.push" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP1 = getArgValue_use(h, ctx, "length")
  
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
                      getArgValue_use(h, ctx, (i.toString)))
                    val __LP2 = AH.PropStore_use(h, l, AbsString.alpha("length"))
                    /* may def */
                    val __LP3 = AH.PropStore_def(h, l, AbsString.alpha("length"))
                    __LP1 ++ __LP2 ++ __LP3
                  }
                  case _ => {
                    val __LP1 = getArgValueAbs_use(h, ctx, NumStr)
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
            val _LP1 = getArgValueAbs_use(h, ctx, NumStr)
            val _LP2 = lset_this.foldLeft(LPBot)((lpset, l) =>
              lpset ++ AH.PropStore_use(h, l, NumStr) ++
              /* may def */
              AH.PropStore_def(h, l, NumStr))
            _LP1 ++ _LP2
          }
        }
        LP1 ++ LP2 + ((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
      }
      case "Array.prototype.reverse" => {
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
      }
      case "Array.prototype.shift" => {
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
      }
      case "Array.prototype.slice" => {
        val LP1 = AH.Oldify_use(h, ctx, addr1)
        
        val n_start = Operator.ToInteger(getArgValue(h, ctx, "0"))
        val n_end = Operator.ToInteger(getArgValue(h, ctx, "1"))
        val LP2 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1")
        
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
      }
      case "Array.prototype.splice" => {
        val LP1 = AH.Oldify_use(h, ctx, addr1)

        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val n_start = Operator.ToInteger(getArgValue(h, ctx, "0"))
        val n_count = Operator.ToInteger(getArgValue(h, ctx, "1"))
        val LP2 = getArgValue_use(h, ctx, "length") ++ getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1")
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
		                    val lpset2 = getArgValue_use(h, ctx, (i+2).toString)
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
		                    val lpset2 = getArgValue_use(h, ctx, (i+2).toString)
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
		                val ___LP2 = getArgValueAbs_use(h, ctx, NumStr)
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
                  val __LP3 = getArgValueAbs_use(h, ctx, NumStr)
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
                    val __LP4 = getArgValueAbs_use(h, ctx, NumStr)
                    val __LP5 = AH.Delete_def(h, l, NumStr)
                    val __LP6 = AH.PropStore_def(h, l, NumStr)
                    __LP1 ++ __LP2 ++ __LP3 ++ __LP4 ++ __LP5 ++ __LP6
                }
                lpset ++ _LP1 ++ _LP2
              })
            }
        }
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return")
      }
      case "Array.prototype.unshift" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP1 = getArgValue_use(h, ctx, "length")
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
                      val lpset1 = getArgValue_use(h, ctx, i.toString)
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
      }
      case "Array.prototype.indexOf" => {
        val LP1 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1")  ++ getArgValue_use(h, ctx, "length")
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
      }
      case "Array.prototype.lastIndexOf" => {
        val LP1 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1")  ++ getArgValue_use(h, ctx, "length")
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
      }
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}



object AccessBuiltinString {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    
    fun match {
      /* String */
      case "String" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "String.constructor" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val s = n_arglen match {
          case UIntSingle(n) if n == 0 =>
            AbsString.alpha("")
          case UIntSingle(n) if n > 0 =>
            Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "0")))
          case NumBot => StrBot
          case _ => StrTop
        } 
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => 
            AH.NewString_def(s).foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
        LP1 + (SinglePureLocalLoc, "@return")
      }
      case "String.fromCharCode" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "String.prototype.toString" | 
           "String.prototype.valueOf" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("String")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
      }
      case "String.prototype.charAt" 
         | "String.prototype.charCodeAt" 
         | "String.prototype.concat" 
         | "String.prototype.indexOf" 
         | "String.prototype.lastIndexOf" 
         | "String.prototype.localeCompare" 
         | "String.prototype.slice" 
         | "String.prototype.substring" 
         | "String.prototype.toLowerCase" 
         | "String.prototype.toLocaleLowerCase" 
         | "String.prototype.toUpperCase" 
         | "String.prototype.toLocaleUpperCase" 
         | "String.prototype.trim" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      /* String */
      case "String" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP1 = getArgValue_use(h, ctx, "length")
        val LP2 = n_arglen match {
          case UIntSingle(n) if n == 0 => LPBot
          case UIntSingle(n) if n > 0 => getArgValue_use(h, ctx, "0")
          case UInt | NumTop => getArgValue_use(h, ctx, "0")
          case _ => LPBot
        }
         LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
      case "String.constructor" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP1 = getArgValue_use(h, ctx, "length")
        val LP2 = n_arglen match {
          case UIntSingle(n) if n > 0 => getArgValue_use(h, ctx, "0")
          case _ => LPBot
        }
        /* may def */
        val s = n_arglen match {
          case UIntSingle(n) if n == 0 =>
            AbsString.alpha("")
          case UIntSingle(n) if n > 0 =>
            Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, "0")))
          case NumBot => StrBot
          case _ => StrTop
        } 
        
        val LP3 = lset_this.foldLeft(LPBot)((lpset, l) => 
            AH.NewString_def(s).foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "String.fromCharCode" => {
        val arg_length = getArgValue(h, ctx, "length")._1._4
        val LP1 = getArgValue_use(h, ctx, "length")
        val LP2 =
          if (arg_length </ NumBot) {
            AbsNumber.concretize(arg_length) match {
              case Some(n) => 
                (0 until n.toInt).foldLeft(LPBot)((lpset, i) => getArgValue_use(h, ctx, i.toString))
              case None =>
                if (arg_length <= NumBot) LPBot
                else getArgValueAbs_use(h, ctx, NumStr)
            }
          } else {
            LPBot
          }
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return")
      }
      case "String.prototype.toString"
         | "String.prototype.valueOf" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("String")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
        val lset_string = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == AbsString.alpha("String"))
        val LP2 = lset_string.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        LP1 ++ LP2 ++ AH.RaiseException_use(es) + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "String.prototype.charAt"
         | "String.prototype.charCodeAt" => {
        val LP1 = getArgValue_use(h, ctx, "length")
        val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "String.prototype.concat" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP1 = getArgValue_use(h, ctx, "length")
        val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        val LP3 = n_arglen match {
          case UIntSingle(n) if n == 0 => LPBot
          case UIntSingle(n) if n > 0 =>
            (0 until n.toInt).foldLeft(LPBot)((lpset, i) => getArgValue_use(h, ctx, i.toString))
          case UInt | NumTop => getArgValueAbs_use(h, ctx, NumStr)
          case _ => LPBot
        }
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "String.prototype.localeCompare" => {
        val LP1 = getArgValue_use(h, ctx, "0")
        val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "String.prototype.indexOf"
         | "String.prototype.lastIndexOf"
         | "String.prototype.slice"
         | "String.prototype.substring" => {
        val LP1 = getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1")
        val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "String.prototype.toLowerCase" 
         | "String.prototype.toLocaleLowerCase" 
         | "String.prototype.toUpperCase" 
         | "String.prototype.toLocaleUpperCase" 
         | "String.prototype.trim" => {
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        LP1 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinBoolean {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    
    fun match {
      /* Boolean */
      case "Boolean" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Boolean.constructor" => {
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => 
            AH.NewBoolean_def.foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
        LP1 + (SinglePureLocalLoc, "@return")
      }
      case "Boolean.prototype.toString"
         | "Boolean.prototype.valueOf" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Boolean")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
      }
      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      case "Boolean" => {
        getArgValue_use(h, ctx, "0") ++ 
        getArgValue_use(h, ctx, "length") + (SinglePureLocalLoc, "@return")
      }
      case "Boolean.constructor" => {
        /* may def */
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => 
            AH.NewBoolean_def.foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
        LP1 ++ getArgValue_use(h, ctx, "0") ++ 
        getArgValue_use(h, ctx, "length") + (SinglePureLocalLoc, "@return") + ((SinglePureLocalLoc, "@this"))
      }
      
      case "Boolean.prototype.toString"
         | "Boolean.prototype.valueOf" => {
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Boolean")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val LP2 = AH.RaiseException_use(es)
        val lset_bool = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == OtherStrSingle("Boolean"))
        val LP3 = lset_bool.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinNumber {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    
    fun match {
      /* Number */
      case "Number" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Number.constructor" => {
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => 
            AH.NewNumber_def.foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
        LP1 + (SinglePureLocalLoc, "@return")
      }
      case "Number.prototype.toString" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val es1 =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("Number")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val es2 =
          n_arglen match {
            case UIntSingle(n_arglen) if n_arglen == 0 =>  ExceptionBot
            case UIntSingle(n_arglen) if n_arglen > 0 => {
                if (BoolTrue <= Operator.bopGreater(getArgValue(h, ctx, "0"), Value(AbsNumber.alpha(36)))._1._3)
                  Set[Exception](RangeError)
                else if (BoolTrue <= Operator.bopLess(getArgValue(h, ctx, "0"), Value(AbsNumber.alpha(2)))._1._3)
                  Set[Exception](RangeError)
                else
                  ExceptionBot
            }
            case NumBot => ExceptionBot
            case _ => Set[Exception](RangeError)
          }
        AH.RaiseException_def(es1 ++ es2) + (SinglePureLocalLoc, "@return")
      }
      case "Number.prototype.toLocaleString" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Number.prototype.valueOf" => {
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Number")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
      }
      case "Number.prototype.toFixed" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, AbsNumber.alpha(0) + v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(20)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(0)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
      }
      case "Number.prototype.toExponential" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(20)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(0)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
      }
      case "Number.prototype.toPrecision" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(21)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(1)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return")
      }
      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
   val val_this = Value(lset_this)

    fun match {
      /* Number */
      case "Number"  => {
        getArgValue_use(h, ctx, "0") ++ 
        getArgValue_use(h, ctx, "length") + (SinglePureLocalLoc, "@return")
      }
      case "Number.constructor"=> {
        /* may def */
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => 
            AH.NewNumber_def.foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
        LP1 ++ getArgValue_use(h, ctx, "0") ++ 
        getArgValue_use(h, ctx, "length") + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "Number.prototype.toString" => {
        val n_arglen = Operator.ToUInt32(getArgValue(h, ctx, "length"))
        val LP1 = getArgValue_use(h, ctx, "length")
        val es1 =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != AbsString.alpha("Number")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val lset_num = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == OtherStrSingle("Number"))
        val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
        val v_prim = lset_num.foldLeft(ValueBot)((_v, _l) => _v + h(_l)("@primitive")._1._2)
        val LP3 = lset_num.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        val (es2, lpset4) =
          n_arglen match {
            case UIntSingle(n_arglen) if n_arglen == 0 =>  (ExceptionBot, LPBot)
            case UIntSingle(n_arglen) if n_arglen > 0 => {
                if (BoolTrue <= Operator.bopGreater(getArgValue(h, ctx, "0"), Value(AbsNumber.alpha(36)))._1._3)
                  (Set[Exception](RangeError), getArgValue_use(h, ctx, "0"))
                else if (BoolTrue <= Operator.bopLess(getArgValue(h, ctx, "0"), Value(AbsNumber.alpha(2)))._1._3)
                  (Set[Exception](RangeError), getArgValue_use(h, ctx, "0"))
                else
                  (ExceptionBot, getArgValue_use(h, ctx, "0"))
            }
            case NumBot => (ExceptionBot, LPBot)
            case _ => (Set[Exception](RangeError), getArgValue_use(h, ctx, "0"))
          }
        val LP5 = AH.RaiseException_use(es1 ++ es2)
        LP1 ++ LP2 ++ LP3 ++ lpset4 ++ LP5 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "Number.prototype.toLocaleString" => {
        val LP1= lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        LP1 ++ getArgValue_use(h, ctx, "length") + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "Number.prototype.valueOf" => {
        val lset_num = lset_this.filter((l) => h(l)("@class")._1._2._1._5 == OtherStrSingle("Number"))
        val es =
          if (lset_this.exists((l) => h(l)("@class")._1._2._1._5 != OtherStrSingle("Number")))
            Set[Exception](TypeError)
          else
            ExceptionBot
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
        val LP2 = lset_num.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
        LP1 ++ LP2 ++ AH.RaiseException_use(es) + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "Number.prototype.toFixed" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, AbsNumber.alpha(0) + v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(20)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(0)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        getArgValue_use(h, ctx, "0") ++ AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "Number.prototype.toExponential" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(20)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(0)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        getArgValue_use(h, ctx, "0") ++ AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "Number.prototype.toPrecision" => {
        val v_1 = getArgValue(h, ctx, "0")
        val v_2 = 
          if (UndefTop <= v_1._1._1)
            Value(PValue(UndefBot, v_1._1._2, v_1._1._3, v_1._1._4, v_1._1._5), v_1._2)
          else
            v_1
        val es =
          if (BoolTrue <= Operator.bopGreater(v_2, Value(AbsNumber.alpha(21)))._1._3)
            Set[Exception](RangeError)
          else if (BoolTrue <= Operator.bopLess(v_2, Value(AbsNumber.alpha(1)))._1._3)
            Set[Exception](RangeError)
          else
            ExceptionBot
        getArgValue_use(h, ctx, "0") ++ AH.RaiseException_def(es) + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinMath {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    
    fun match {
      /* Math */
      case "Math.abs"  | "Math.acos"   | "Math.asin"
         | "Math.atan" | "Math.atan2"  | "Math.ceil"
         | "Math.cos"  | "Math.exp"    | "Math.floor"
         | "Math.max"  | "Math.min"    | "Math.pow"
         | "Math.log"  | "Math.random" | "Math.round"
         | "Math.sin"  | "Math.sqrt"   | "Math.tan"   => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      /* Math */
      case "Math.random" => LPSet((SinglePureLocalLoc, "@return"))
      case "Math.abs"   | "Math.acos"  | "Math.asin"
         | "Math.atan"  | "Math.ceil"  | "Math.cos"
         | "Math.exp"   | "Math.floor" | "Math.log"
         | "Math.round" | "Math.sin"   | "Math.sqrt" | "Math.tan" => {
        getArgValue_use(h, ctx, "0") + ((SinglePureLocalLoc, "@return")) 
      }
      case "Math.atan2" | "Math.pow" => {
        getArgValue_use(h, ctx, "0") ++ getArgValue_use(h, ctx, "1") + ((SinglePureLocalLoc, "@return"))
      } 
      case "Math.max"   | "Math.min" => {
        val n_arglen = getArgValue(h, ctx, "length")._1._4
        val LP1 = getArgValue_use(h, ctx, "length")
        val LP2 = n_arglen match {
          case UIntSingle(n) =>
            (0 until n.toInt).foldLeft(LPBot)((lpset, i) => lpset ++ getArgValue_use(h, ctx, i.toString))
          case _ => LPBot
        }
        LP1 ++ LP2 + ((SinglePureLocalLoc, "@return"))
      }          
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinDate {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    
    fun match {
      /* Date */
      case "Date" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Date.constructor" => {
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => 
            AH.NewDate_def.foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
        LP1 + (SinglePureLocalLoc, "@return")
      }
      case "Date.now" 
         | "Date.parse"
         | "Date.prototype.toString"
         | "Date.prototype.toDateString"
         | "Date.prototype.toTimeString"
         | "Date.prototype.toLocaleString"
         | "Date.prototype.toLocaleDateString"
         | "Date.prototype.toLocaleTimeString"
         | "Date.prototype.toUTCString"
         | "Date.prototype.toISOString"
         | "Date.prototype.valueOf"
         | "Date.prototype.getTime"
         | "Date.prototype.getFullYear"
         | "Date.prototype.getMonth"
         | "Date.prototype.getDate"
         | "Date.prototype.getDay"
         | "Date.prototype.getHours"
         | "Date.prototype.getMinutes"
         | "Date.prototype.getSeconds"
         | "Date.prototype.getMilliseconds"
         | "Date.prototype.getTimezoneOffset"
         | "Date.prototype.getUTCFullYear"
         | "Date.prototype.getUTCMonth"
         | "Date.prototype.getUTCDate" 
         | "Date.prototype.getUTCDay" 
         | "Date.prototype.getUTCHours" 
         | "Date.prototype.getUTCMinutes"
         | "Date.prototype.getUTCSeconds" 
         | "Date.prototype.getUTCMilliseconds" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Date.prototype.setTime" 
         | "Date.prototype.setMilliseconds"
      	 | "Date.prototype.setSeconds"
      	 | "Date.prototype.setMinutes"
      	 | "Date.prototype.setHours"
      	 | "Date.prototype.setDate"
      	 | "Date.prototype.setMonth"
      	 | "Date.prototype.setFullYear" 
      	 | "Date.prototype.setUTCMilliseconds" 
      	 | "Date.prototype.setUTCSeconds" 
      	 | "Date.prototype.setUTCMinutes" 
      	 | "Date.prototype.setUTCHours" 
      	 | "Date.prototype.setUTCDate" 
      	 | "Date.prototype.setUTCMonth" 
      	 | "Date.prototype.setUTCFullYear" => {
      	val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))   
        LP1 + (SinglePureLocalLoc, "@return")
      }
      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      /* Date */
      case "Date" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Date.constructor" => {
        /* may def */
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => 
            AH.NewDate_def.foldLeft(lpset)((_lpset, prop) => _lpset + (l, prop)))
        LP1 ++ getArgValue_use(h, ctx, "length") ++
        getArgValue_use(h, ctx, "0") + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case "Date.now" 
         | "Date.parse"
         | "Date.prototype.toString"
         | "Date.prototype.toDateString"
         | "Date.prototype.toTimeString"
         | "Date.prototype.toLocaleString"
         | "Date.prototype.toLocaleDateString"
         | "Date.prototype.toLocaleTimeString"
         | "Date.prototype.toUTCString"
         | "Date.prototype.toISOString" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Date.prototype.valueOf"
         | "Date.prototype.getTime" => {
       val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))
       LP1 ++ LPSet((SinglePureLocalLoc, "@return")) + (SinglePureLocalLoc, "@this")
      }
      case "Date.prototype.getFullYear"
         | "Date.prototype.getMonth"
         | "Date.prototype.getDate"
         | "Date.prototype.getDay"
         | "Date.prototype.getHours"
         | "Date.prototype.getMinutes"
         | "Date.prototype.getSeconds"
         | "Date.prototype.getMilliseconds"
         | "Date.prototype.getTimezoneOffset"
         | "Date.prototype.getUTCFullYear"
         | "Date.prototype.getUTCMonth"
         | "Date.prototype.getUTCDate" 
         | "Date.prototype.getUTCDay" 
         | "Date.prototype.getUTCHours" 
         | "Date.prototype.getUTCMinutes"
         | "Date.prototype.getUTCSeconds" 
         | "Date.prototype.getUTCMilliseconds" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "Date.prototype.setTime" => {
        val LP1 = getArgValue_use(h, ctx, "0")
        val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive")) 
        LP1 ++ LP2 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      } 
      case "Date.prototype.setMilliseconds"
      	 | "Date.prototype.setSeconds"
      	 | "Date.prototype.setMinutes"
      	 | "Date.prototype.setMinutes"
      	 | "Date.prototype.setHours"
      	 | "Date.prototype.setDate"
      	 | "Date.prototype.setMonth"
      	 | "Date.prototype.setFullYear" 
      	 | "Date.prototype.setUTCMilliseconds" 
      	 | "Date.prototype.setUTCSeconds" 
      	 | "Date.prototype.setUTCMinutes" 
      	 | "Date.prototype.setUTCHours" 
      	 | "Date.prototype.setUTCDate" 
      	 | "Date.prototype.setUTCMonth" 
      	 | "Date.prototype.setUTCFullYear" => {
      	val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset + (l, "@primitive"))   
        LP1 + (SinglePureLocalLoc, "@return") + (SinglePureLocalLoc, "@this")
      }
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinRegExp {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    
    fun match {

      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {

      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinError {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    
    fun match {
      /* Error */
      case "Error" | "Error.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 =
          if (Value(PValue(UndefBot, v._1._2, v._1._3, v._1._4, v._1._5), v._2) </ ValueBot)
            LPSet((ErrLoc, "message"))
          else LPBot
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2
      }
      case "Error.prototype.toString" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "EvalError" | "EvalError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = 
          if (Value(PValue(UndefBot, v._1._2, v._1._3, v._1._4, v._1._5), v._2) </ ValueBot)
            LPSet((EvalErrLoc, "message"))
          else LPBot
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2
      }
      case "RangeError" | "RangeError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 =
          if (Value(PValue(UndefBot, v._1._2, v._1._3, v._1._4, v._1._5), v._2) </ ValueBot)
            LPSet((RangeErrLoc, "message"))
          else LPBot
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2
      }
      case "ReferenceError" | "ReferenceError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 =
          if (Value(PValue(UndefBot, v._1._2, v._1._3, v._1._4, v._1._5), v._2) </ ValueBot)
            LPSet((RefErrLoc, "message"))
          else LPBot
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2
      }
      case "SyntaxError" | "SyntaxError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 =
          if (Value(PValue(UndefBot, v._1._2, v._1._3, v._1._4, v._1._5), v._2) </ ValueBot)
            LPSet((SyntaxErrLoc, "message"))
          else LPBot
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2
      }
      case "TypeError" | "TypeError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 =
          if (Value(PValue(UndefBot, v._1._2, v._1._3, v._1._4, v._1._5), v._2) </ ValueBot)
            LPSet((TypeErrLoc, "message"))
          else LPBot
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2
      }
      case "URIError" | "URIError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 =
          if (Value(PValue(UndefBot, v._1._2, v._1._3, v._1._4, v._1._5), v._2) </ ValueBot)
            LPSet((URIErrLoc, "message"))
          else LPBot
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2
      }
      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val getFromThis_use = (x : String) => Access.V_use(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
    val val_this = Value(lset_this)

    fun match {
      /* Error */
      case "Error.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = LPSet((ErrLoc, "message"))
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2 ++ getArgValue_use(h, ctx, "0")
      }
      case "Error.prototype.toString" => {
        val LP1 = lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h, l, AbsString.alpha("name")))
        val LP2 = lset_this.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h, l, AbsString.alpha("message")))
        val LP3 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2 ++ LP3 + (SinglePureLocalLoc, "@this")
      }
      case "EvalError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = LPSet((EvalErrLoc, "message"))
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2 ++ getArgValue_use(h, ctx, "0")
      }
      case "RangeError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = LPSet((RangeErrLoc, "message"))
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2 ++ getArgValue_use(h, ctx, "0")
      }
      case "ReferenceError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = LPSet((RefErrLoc, "message"))
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2 ++ getArgValue_use(h, ctx, "0")
      }
      case "SyntaxError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = LPSet((SyntaxErrLoc, "message"))
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2 ++ getArgValue_use(h, ctx, "0")
      }
      case "TypeError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = LPSet((TypeErrLoc, "message"))
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2 ++ getArgValue_use(h, ctx, "0")
      }
      case "URIError.constructor" => {
        val v = getArgValue(h, ctx, "0")
        val LP1 = LPSet((URIErrLoc, "message"))
        val LP2 = LPSet((SinglePureLocalLoc, "@return"))
        LP1 ++ LP2 ++ getArgValue_use(h, ctx, "0")
      }
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}

object AccessBuiltinJSON {
  def builtinCall_def(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    
    fun match {
      /* JSON */
      case "JSON.parse" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "JSON.stringify" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case _ => LPBot
    }
  }

  def builtinCall_use(h: Heap, ctx: Context,
                  fun: String, args: CFGExpr, addr1: Address, addr2: Address): LPSet = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))

    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
    val getFromThis = (x : String) => SE.V(CFGLoad(dummyInfo, CFGThis(dummyInfo), CFGString(x)), h, ctx)._1
    def getArgValue_use(h : Heap, ctx: Context, x : String):LPSet = Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
    def getArgValueAbs_use(h : Heap, ctx: Context, s : AbsString): LPSet = {
      val lset = SE.V(args, h, ctx)._1._2
      val LP1 = Access.V_use(args, h, ctx)
      val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
      LP1 ++ LP2
    }
    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

    fun match {
      /* JSON */
      case "JSON.parse" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case "JSON.stringify" => {
        LPSet((SinglePureLocalLoc, "@return"))
      }
      case _ =>
        getArgValue_use(h, ctx, "length") ++ getArgValueAbs_use(h, ctx, NumStr) + ((SinglePureLocalLoc, "@return"))
    }
  }
}
