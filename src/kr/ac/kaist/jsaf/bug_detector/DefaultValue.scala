/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class DefaultValue(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
  /* 
  * "DefaultValue" deals with two kinds of ERROR bugs: DefaultValueTypeError, ImplicitTypeConversion.
  * DefaultValueTypeError   : TypeError when computing default value (toString, valueOf) for object
  * ImplicitTypeConversion  : toString or valueOf method of built-in (native) object is implicitly called when it was redefined 
  *                         : toString or valueOf method of built-in (native) object is implicitly called by user-defined object
  * DefaultValueTypeError occurs if both internal methods toString or valueOf of an object are not callable by [[IsCallable]],
  * while ImplicitTypeConversion occurs during calling internal method of an object, toString or valueOf by internal method [[Get]].
  * So, as the order of usage, DefaultValueTypeError is not reported when ImplicitTypeConversion error occurs. Reported, otherwise.
  */

  /* 
  * BUG MESSAGE   : DefaultValueTypeError (1) ImplicitTypeConversion (2)
  * definite_only : true => detect definite case.
  *               : false => detect maybe case also.
  */
  private val dvmsg = "TypeError when computing default value (toString, valueOf) for object '%s'."
  private val ctmsg = "Implicit toString type-conversion to object '%s' by non-builtin toString method."
  private val cvmsg = "Implicit valueOf type-conversion to object '%s' by non-builtin valueOf method."
  private val definite_only = true

  /* builtin method (toString, valueOf) check for ImplicitTypeConversion */
  private val typeConversionMap: Map[String, Set[String]] = Map(
    "toString" -> Set("Object.prototype.toString", "Function.prototype.toString", "Array.prototype.toString", 
                      "String.prototype.toString", "Boolean.prototype.toString", "Number.prototype.toString", 
                      "Date.prototype.toString", "RegExp.prototype.toString", "Error.prototype.toString"), 
    "valueOf"  -> Set("Object.prototype.valueOf", "String.prototype.valueOf", "Boolean.prototype.valueOf", 
                      "Number.prototype.valueOf", "Date.prototype.valueOf"))

  /* (Optional) distinguish different arguments in built-in Methods? */
  private val builtinMethodMap : Map[String, Set[String]] = Map(
    "String" -> Set("String.prototype.charAt", "String.prototype.charCodeAt", "String.prototype.concat", 
                    "String.prototype.indexOf", "String.prototype.lastIndexOf", "String.prototype.localeCompare", 
                    "String.prototype.match", "String.prototype.replace", "String.prototype.search", 
                    "String.prototype.slice", "String.prototype.slice", "String.prototype.split", 
                    "String.prototype.substring", "String.prototype.toLowerCase", "String.prototype.trim"),
    "Number" -> Set("Global.isNaN", "Global.isFinite", "Date.prototype.setTime", "Date.prototype.setMilliseconds",
      "Date.prototype.setUTCMilliseconds", "Date.prototype.setSeconds", "Date.prototype.setUTCSeconds",
      "Date.prototype.setMinutes", "Date.prototype.setUTCMinutes", "Date.prototype.setHours", 
      "Date.prototype.setUTCHours", "Date.prototype.setDate", "Date.prototype.setUTCDate","Date.prototype.setMonth", 
      "Date.prototype.setUTCMonth", "Date.prototype.setFullYear", "Date.prototype.setUTCFullYear"))

  /* 
  * dvcount : number of bugs of DefaultValueTypeError.
  * tccount : number of bugs of ImplicitTypeConversion.
  */
  private var dvcount = 0
  private var tccount = 0

  override def printStat = {
    System.out.println("# DefaultValueTypeError: " + dvcount) 
    System.out.println("# ImplicitTypeConversion: " + tccount) 
  }

  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (state._1 <= HeapBot) Unit
    else {
      inst match {
        case CFGCall(_, info, fun, thisArg, args, _) =>
          val obj_val = SE.V(fun, heap, context)._1
          obj_val._2.foreach((loc) =>
            heap(loc)("@function")._1._3.foreach((fid) =>
              typing.builtinFset.get(fid) match {
                case Some(builtinName) =>
                  /* ToString */
                  if (builtinName == "String") check(args, "String")
                  else if (optionFilter(builtinMethodMap.get("String")) contains builtinName) check(thisArg, "String")
                  /*
                  * dealing with JSON.stringify when replacer, space or value is Object and its [[Class]] is String 
                  * ToDo ToString check when replacer is Object and its [[Class]] is Number (what's replacer ??)
                  */
                  else if (builtinName == "JSON.strinify" && !SE.V(args, heap, context)._1._2.subsetOf(LocSetBot)) 
                    SE.V(args, heap, context)._1._2.foreach((loc) => 
                      if (AbsString.alpha("String") == heap(loc)("@class")._1._2._1._5) check(args, "String")
                      else if (AbsString.alpha("Number") == heap(loc)("@class")._1._2._1._5) check(args, "Number")) 
                  /* ToNumber */
                  else if (builtinName == "Number") check(args, "Number")
                  else if (optionFilter(builtinMethodMap.get("Number")) contains builtinName) check(args, "Number")
                case None => Unit
         }))
        case CFGConstruct(_, info, cons, thisArg, args, _) => 
          val obj_val = SE.V(cons, heap, context)._1
          obj_val._2.foreach((loc) =>
            heap(loc)("@function")._1._3.foreach((fid) =>
              typing.builtinFset.get(fid) match {
                case Some(builtinName) =>
                  if (builtinName == "String")      check(args, "String")
                  else if (builtinName == "Number") check(args, "Number")
                  else if (builtinName == "Date")   check(args, "Number")
                case None => Unit
         }))
        case _ => Unit
      }
    }

    def optionFilter(optSet: Option[Set[String]]): Set[String] = {
      optSet match {
        case Some(set) => set
        case None => Set(): Set[String]
      }
    }

    /* check when toPrimitive with Object argument is called */
    def check(expr: CFGExpr, hint: String): Unit = {
      val expr_val    = SE.V(expr, heap, context)._1 // Value
      val expr_info   = expr.getInfo
      var errorFound  = false
      val checkList   = hint match {
        case "String" => ("toString", "valueOf")
        case "Number" => ("valueOf", "toString")
      }
      if (!expr_val._2.subsetOf(LocSetBot)) {
        expr_val._2.foreach((loc) => 
          for (l <- heap(loc)("@proto")._1._1._1._2) {
            val name = cfg.getFuncName(loc)
            val cv1  = heap(l)(checkList._1)._1._1._1
            // BUG CHECK :: ImplicitTypeConversion
            cv1._2.foreach((iloc) => heap(iloc)("@function")._1._3.foreach((fid) =>
              typing.builtinFset.get(fid) match {
                case Some(builtinName) => 
                  if (!(typeConversionMap(checkList._1) contains builtinName)) {
                    errorFound = true; addITCBug(checkList._1, expr.toString, expr_info)
                  }
                case None => errorFound = true; addITCBug(checkList._1, expr.toString, expr_info)
            }))
            // BUG CHECK :: DefaultValueTypeError (IsCallable(checkList._1))
            if (!errorFound && !cv1._2.subsetOf(LocSetBot)) cv1._2.foreach((sloc) =>
              Helper.IsCallable(heap, sloc) match {
                case BoolTrue   => errorFound = true
                case BoolFalse  => errorFound = false
                case _          => Unit       // Maybe
              })
            if (errorFound) Unit              // First IsCallable passed
            else {
              // BUG CHECK :: ImplicitTypeConversion
              cv1._2.foreach((iloc) => heap(iloc)("@function")._1._3.foreach((fid) =>
                typing.builtinFset.get(fid) match {
                  case Some(builtinName) => 
                    if (!(typeConversionMap(checkList._2) contains builtinName)) {
                      errorFound = true; addITCBug(checkList._2, expr.toString, expr_info)
                    }
                  case None => errorFound = true; addITCBug(checkList._1, expr.toString, expr_info)
              }))
              // BUG CHECK :: DefaultValueTypeError (IsCallable(checkList._2))
              val cv2 = heap(l)(checkList._2)._1._1._1
              if (!cv2._2.subsetOf(LocSetBot)) cv2._2.foreach((vloc) =>
                Helper.IsCallable(heap, vloc) match {
                  case BoolTrue   => errorFound = true
                  case BoolFalse  => errorFound = false  
                  case _          => Unit     // Maybe
            })}
            if (!errorFound) {
              addDVTEBug(expr.toString, expr_info)
              Unit
            }
            errorFound = false
    })}}
  }
    
  override def checkExpr(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (state._1 <= HeapBot) Unit
    else {
      expr match {
        case CFGBin(info, first, op, second) => 
          op.getText match {
            /* toPrimitive(Obj) */
            case "+"  => check(first, "Number"); check(second, "Number")
            /* Multiplicative Operators (*, /, %) :: ToNumber */
            case "*"  => check(first, "Number"); check(second, "Number")
            case "/"  => check(first, "Number"); check(second, "Number")
            case "%"  => check(first, "Number"); check(second, "Number")
            case "<"  => check(first, "Number"); check(second, "Number")
            case ">"  => check(first, "Number"); check(second, "Number")
            case "<=" => check(first, "Number"); check(second, "Number")
            case ">=" => check(first, "Number"); check(second, "Number")
            case "==" => 
              val f_val = SE.V(first, heap, context)._1
              val s_val = SE.V(second, heap, context)._1
              if (!definite_only && (f_val._1._1 </ UndefBot || f_val._1._2 </ NullBot || f_val._1._3 </ BoolBot)) Unit               // Maybe
              else if ((NumBot <= f_val._1._4 || StrBot <= f_val._1._5) && (!s_val._2.subsetOf(LocSetBot))) check(second, "Number");  // Definite
              else if ((!f_val._2.subsetOf(LocSetBot)) && (NumBot <= s_val._1._4 || StrBot <= s_val._1._5)) check(first, "Number");   // Definite
            /* ToString(Obj) => toPrimitive(Obj) */
            case "in" => check(first, "String");
            case _ => Unit
          }
        /* ToNumber(Obj) => toPrimitive(Obj) */
        case CFGUn(info, op, expr) =>
          op.getText match {
            case "+" => check(expr, "Number")
            case "-" => check(expr, "Number")
            case _   => Unit
          }
        case _ => Unit
      }
    }

    /* check when toPrimitive with Object argument is called */
    def check(expr: CFGExpr, hint: String): Unit = {
      val expr_val    = SE.V(expr, heap, context)._1 // Value
      val expr_info   = expr.getInfo
      var errorFound  = false
      var isBuiltin   = false
      // Set up the order of builtin method check
      val checkList   = hint match {
        case "String" => ("toString", "valueOf")
        case "Number" => ("valueOf", "toString")
      }
      // Built-in Object Check
      expr_val._2.foreach((loc) => heap(loc)("@function")._1._3.foreach((fid) => 
        if (typing.builtinFset contains fid) isBuiltin = true))
      // Bug Detection main
      if (!expr_val._2.subsetOf(LocSetBot)) {
        expr_val._2.foreach((loc) => 
          for (l <- heap(loc)("@proto")._1._1._1._2) {
            val name = cfg.getFuncName(loc)
            val cv1  = heap(l)(checkList._1)._1._1._1
            // BUG CHECK :: ImplicitTypeConversion
            if (isBuiltin && !cv1._2.subsetOf(LocSetBot)) {
              cv1._2.foreach((iloc) => heap(iloc)("@function")._1._3.foreach((fid) =>
                typing.builtinFset.get(fid) match {
                  case Some(builtinName) => 
                    if (!(typeConversionMap(checkList._1) contains builtinName)) {
                      errorFound = true; addITCBug(checkList._1, expr.toString, expr_info)
                    }
                  case None => errorFound = true; addITCBug(checkList._1, expr.toString, expr_info)
              }))
            } 
            // BUG CHECK :: DefaultValueTypeError (IsCallable(checkList._1))
            if (!errorFound && !cv1._2.subsetOf(LocSetBot)) cv1._2.foreach((sloc) =>
              Helper.IsCallable(heap, sloc) match {
                case BoolTrue   => errorFound = true
                case BoolFalse  => errorFound = false
                case _          => Unit       // Maybe
              })
            if (errorFound) Unit              // First IsCallable passed
            else {
              val cv2 = heap(l)(checkList._2)._1._1._1
              // BUG CHECK :: ImplicitTypeConversion
              if (isBuiltin && !cv2._2.subsetOf(LocSetBot)) {
                cv2._2.foreach((iloc) => heap(iloc)("@function")._1._3.foreach((fid) =>
                  typing.builtinFset.get(fid) match {
                    case Some(builtinName) => 
                      if (!(typeConversionMap(checkList._2) contains builtinName)) {
                        errorFound = true; addITCBug(checkList._2, expr.toString, expr_info)
                      }
                    case None => errorFound = true; addITCBug(checkList._2, expr.toString, expr_info)
                }))
              } 
              // BUG CHECK :: DefaultValueTypeError (IsCallable(checkList._2))
              if (!errorFound && !cv2._2.subsetOf(LocSetBot)) cv2._2.foreach((vloc) =>
                Helper.IsCallable(heap, vloc) match {
                  case BoolTrue   => errorFound = true
                  case BoolFalse  => errorFound = false  
                  case _          => Unit     // Maybe
            })}
            if (!errorFound) addDVTEBug(expr.toString, expr_info)
            errorFound = false
    })}}
  }

  def addITCBug(kind: String, name: String, rawInfo: Option[Info]): Unit = {
    println("YO")
    rawInfo match {
      case Some(info) => 
        tccount = tccount + 1; 
        kind match {
          case "toString" => bugs.addMessage(info.getSpan, "warning", ctmsg.format(name))
          case "valueOf"  => bugs.addMessage(info.getSpan, "warning", cvmsg.format(name))
          case _          => println("bugDetector, ImplicitTypeConversion. Wrong kind to addITCBug.")
        }
      case None => println("bugDetector, ImplicitTypeConversion. Expression has no info.")
    }; Unit
  }

  def addDVTEBug(name: String, rawInfo: Option[Info]): Unit = {
    rawInfo match {
      case Some(info) => 
        dvcount = dvcount + 1; 
        bugs.addMessage(info.getSpan, "error", dvmsg.format(name))
      case None => println("bugDetector, DefaultValueTypeError. Expression has no info.")
    }; Unit
  }
}
