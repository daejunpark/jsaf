/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class BuiltinArgumentSize(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
  
  private val msg_few = "Too few arguments to function(fid: %d)."
  private val msg_many = "Too many arguments to function(fid: %d)."
    
  private var count = 0
  override def printStat = System.out.println("# BuiltinArgumentSize: " + count) 
  
  private val argSizeMap = Map[String, (Int, Int)](
      "Global.eval" -> (1,1), "Global.parseInt" -> (1,2), "Global.parseFloat" -> (1,1),	
      "Global.isNaN" -> (1,1), "Global.isFinite" -> (1,1), "Global.decodeURI" -> (1,1),
      "Global.decodeURIComponent" -> (1,1), "Global.encodeURI" -> (1,1), "Global.encodeURIComponent" -> (1,1),
      "Global.alert" -> (0,1),
      "Object" -> (0,1), "Object.constructor" -> (0,1), "Object.getPrototypeOf" -> (1,1),
      "Object.getOwnPropertyDescriptor" -> (2,2), "Object.getOwnPropertyNames" -> (1,1), "Object.create" -> (1,2),
      "Object.defineProperty" -> (3,3), "Object.defineProperties" -> (2,2), "Object.seal" -> (1,1),
      "Object.freeze" -> (1,1), "Object.preventExtensions" -> (1,1), "Object.isSealed" -> (1,1),
      "Object.isFrozen" -> (1,1), "Object.isExtensible" -> (1,1), "Object.keys" -> (1,1),
      "Object.prototype.toString" -> (0,0), "Object.prototype.toLocaleString" -> (0,0), "Object.prototype.valueOf" -> (0,0),
      "Object.prototype.hasOwnProperty" -> (1,1), "Object.prototype.isPrototypeOf" -> (1,1), "Object.prototype.propertyIsEnumerable" -> (1,1),
      "Function" -> (0,Int.MaxValue), "Function.constructor" -> (0,Int.MaxValue), "Function.prototype.toString" -> (0,0),
      "Function.prototype.apply" -> (1,2), "Function.prototype.call" -> (1,Int.MaxValue), "Function.prototype.bind" -> (1,Int.MaxValue),
      "Array" -> (0,Int.MaxValue), "Array.constructor" -> (0,Int.MaxValue), "Array.isArray" -> (1,1),
      "Array.prototype.toString" -> (0,0), "Array.prototype.toLocaleString" -> (0,0), "Array.prototype.concat" -> (0,Int.MaxValue),		
      "Array.prototype.join" -> (0,1), "Array.prototype.pop" -> (0,0), "Array.prototype.push" -> (0,Int.MaxValue),
      "Array.prototype.reverse" -> (0,0), "Array.prototype.shift" -> (0,0),		 "Array.prototype.slice" -> (1,2),		
      "Array.prototype.sort" -> (1,1), "Array.prototype.splice" -> (2,Int.MaxValue), "Array.prototype.unshift" -> (0,Int.MaxValue),
      "Array.prototype.indexOf" -> (1,2), "Array.prototype.lastIndexOf" -> (1,2), "Array.prototype.every" -> (1,2),
      "Array.prototype.some" -> (1,2), "Array.prototype.forEach" -> (1,2), "Array.prototype.map" -> (1,2),
      "Array.prototype.filter" -> (1,2), "Array.prototype.reduce" -> (1,2), "Array.prototype.reduceRight" -> (1,2),
      "String" -> (0,1), "String.constructor" -> (0,1), "String.fromCharCode" -> (0,Int.MaxValue),
      "String.prototype.toString" -> (0,0), "String.prototype.valueOf" -> (0,0), "String.prototype.charAt" -> (1,1),
      "String.prototype.charCodeAt" -> (1,1), "String.prototype.concat" -> (0,Int.MaxValue), "String.prototype.indexOf" -> (1,2),
      "String.prototype.lastIndexOf" -> (1,2), "String.prototype.localeCompare" -> (1,1), "String.prototype.match" -> (1,1),
      "String.prototype.replace" -> (2,2), "String.prototype.search" -> (1,1), "String.prototype.slice" -> (1,2),
      "String.prototype.split" -> (0,2), "String.prototype.substring" -> (1,2), "String.prototype.toLowerCase" -> (0,0),
      "String.prototype.toLocaleLowerCase" -> (0,0), "String.prototype.toUpperCase" -> (0,0), "String.prototype.toLocaleUpperCase" -> (0,0),
      "String.prototype.trim" -> (0,0),
      "Boolean" -> (1,1),	 "Boolean.constructor" -> (1,1), "Boolean.prototype.toString" -> (1,1), "Boolean.prototype.valueOf" -> (0,0),
      "Number" -> (0,1), "Number.constructor" -> (0,1), "Number.prototype.toString" -> (0,1), "Number.prototype.toLocaleString" -> (0,0),
      "Number.prototype.valueOf" -> (0,0), "Number.prototype.toFixed" -> (0,1), "Number.prototype.toExponential" -> (0,1),
      "Number.prototype.toPrecision" -> (0,1), 
      "Math.abs" -> (1,1), "Math.acos" -> (1,1), "Math.asin" -> (1,1),	"Math.atan" -> (1,1), "Math.atan2" -> (2,2),		
      "Math.ceil" -> (1,1),	"Math.cos" -> (1,1), "Math.exp" -> (1,1), "Math.floor" -> (1,1), "Math.log" -> (1,1),
      "Math.max" -> (0,Int.MaxValue), "Math.min" -> (0,Int.MaxValue), "Math.pow" -> (2,2), "Math.random" -> (0,0),
      "Math.round" -> (1,1), "Math.sin" -> (1,1), "Math.sqrt" -> (1,1), "Math.tan" -> (1,1),
      "Date" -> (0,7), "Date.constructor" -> (0,7), "Date.parse" -> (1,1), "Date.UTC" -> (2,7), "Date.now" -> (0,0),
      "Date.prototype.toString" -> (0,0), "Date.prototype.toDateString" -> (0,0), "Date.prototype.toTimeString" -> (0,0),
      "Date.prototype.toLocaleString" -> (0,0), "Date.prototype.toLocaleDateString" -> (0,0), "Date.prototype.toLocaleTimeString" -> (0,0),
      "Date.prototype.valueOf" -> (0,0), "Date.prototype.getTime" -> (0,0), "Date.prototype.getFullYear" -> (0,0),
      "Date.prototype.getUTCFullYear" -> (0,0), "Date.prototype.getMonth" -> (0,0), "Date.prototype.getUTCMonth" -> (0,0),
      "Date.prototype.getDate" -> (0,0), "Date.prototype.getUTCDate" -> (0,0), "Date.prototype.getDay" -> (0,0),
      "Date.prototype.getUTCDay" -> (0,0), "Date.prototype.getHours" -> (0,0), "Date.prototype.getUTCHours" -> (0,0),
      "Date.prototype.getMinutes" -> (0,0), "Date.prototype.getUTCMinutes" -> (0,0), "Date.prototype.getSeconds" -> (0,0),
      "Date.prototype.getUTCSeconds" -> (0,0), "Date.prototype.getMilliseconds" -> (0,0), "Date.prototype.getUTCMilliseconds" -> (0,0),
      "Date.prototype.getTimezoneOffset" -> (0,0), "Date.prototype.setTime" -> (1,1), "Date.prototype.setMilliseconds" -> (1,1),
      "Date.prototype.setUTCMilliseconds" -> (1,1), "Date.prototype.setSeconds" -> (1,2), "Date.prototype.setUTCSeconds" -> (1,2),
      "Date.prototype.setMinutes" -> (1,3), "Date.prototype.setUTCMinutes" -> (1,3), "Date.prototype.setHours" -> (1,4),
      "Date.prototype.setUTCHours" -> (1,4), "Date.prototype.setDate" -> (1,1), "Date.prototype.setUTCDate" -> (1,1),
      "Date.prototype.setMonth" -> (1,2), "Date.prototype.setUTCMonth" -> (1,2), "Date.prototype.setFullYear" -> (1,3),
      "Date.prototype.setUTCFullYear" -> (1,3), "Date.prototype.toUTCString" -> (0,0), "Date.prototype.toISOString" -> (0,0),
      "Date.prototype.toJSON" -> (1,1),
      "RegExp" -> (1,2), "RegExp.constructor" -> (1,2), "RegExp.prototype.exec" -> (1,1), "RegExp.prototype.test" -> (1,1),
      "RegExp.prototype.toString" -> (0,0),
      "Error" -> (0,1), "Error.constructor" -> (0,1), "Error.prototype.toString" -> (0,0), "EvalError.constructor" -> (0,1),
      "RangeError.constructor	" -> (0,1), "ReferenceError.constructor" -> (0,1), "SyntaxError.constructor" -> (0,1),
      "TypeError.constructor" -> (0,1), "URIError.constructor" -> (0,1),
      "JSON.parse" -> (1,2), "JSON.stringify" -> (1,3))
    
     
  /* Call, Construct */   
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    /* state, heap context */
    val state = typing.mergeState(cstate)
    val heap = state._1
    val context = state._2
    
    if (heap <= HeapBot)
      Unit
    else {
      inst match {
        case CFGCall(_, info, fun, _, arguments, _) =>
          val v_fun = SE.V(fun, heap, context)._1
          val lset_f = v_fun._2.filter((l) => BoolTrue <= Helper.IsCallable(heap,l))
          val v_arg = SE.V(arguments, heap, context)._1
          val obj_arg = v_arg._2.foldLeft(ObjBot)((o, l) => o + heap(l))
          val arglen = obj_arg("length")._1._1._1._1._4
          arglen match {
            case UIntSingle(n) =>
              lset_f.foreach((l) =>
                heap(l)("@function")._1._3.foreach((fid) =>
                  typing.builtinFset.get(fid) match {
                    case Some(name) => 
                      val arg_size = argSizeMap(name)
                      if (arg_size._1 > n) {
                        bugs.addMessage(info.getSpan, "warning", msg_few.format(fid))
                        count = count + 1
                      }
                      else if (arg_size._2 < n) {
                        bugs.addMessage(info.getSpan, "warning", msg_many.format(fid))
                        count = count + 1
                      }
                    case None => Unit
                  }))
            case _ => Unit
          }
        case CFGConstruct(_, info, cons, _, arguments, _) => 
          val v_fun = SE.V(cons, heap, context)._1
          val lset_f = v_fun._2.filter((l) => BoolTrue <= Helper.HasConstruct(heap,l))
          val v_arg = SE.V(arguments, heap, context)._1
          val obj_arg = v_arg._2.foldLeft(ObjBot)((o, l) => o + heap(l))
          val arglen = obj_arg("length")._1._1._1._1._4
          arglen match {
            case UIntSingle(n) =>
              lset_f.foreach((l) =>
                heap(l)("@function")._1._3.foreach((fid) =>
                  typing.builtinFset.get(fid) match {
                    case Some(name) => 
                      val arg_size = argSizeMap(name)
                      if (arg_size._1 > n) {
                        bugs.addMessage(info.getSpan, "warning", msg_few.format(fid))
                        count = count + 1
                      }
                      else if (arg_size._2 < n) {
                        bugs.addMessage(info.getSpan, "warning", msg_many.format(fid))
                        count = count + 1
                      }
                    case None => Unit
                  }))
            case _ => Unit
          }
        case _ => Unit
      }
    }
  }

}
