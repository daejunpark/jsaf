/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf

import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{HashMap => MHashMap}

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.nodes_util.SourceLoc
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}

package object bug_detector {
  /******************** TYPE DEFINITION **********************
  * BId     : Bug Identifier
  * BugType : Type of Bug (TypeError, ReferenceError, Warning)
  * BugList : List of reported bugs hold by the BugInfo
  *         : FileName, Begin, End, BugType, BugMessage
  * BugMap  : Map each Bug to its BugType, message and argNum
  * FidSet  : Set of FunctionIds
  ************************************************************/
  type BId      = Int
  type BugType  = Int
  type BugList  = List[(String, SourceLoc, SourceLoc, Int, String)]
  type BugMap   = MMap[BId, (BugType, String, Int)]
  type FidSet   = Set[FunctionId]

  /************ TYPE DEFINITION (UnusedVarProp) **************
  * RWEntry     : (Boolean, Boolean, Loc, String, Info)
  *  L Boolean  : true => Write    | false => Read
  *  L Boolean  : true => Variable | false => Property
  *  L Loc      : Location
  *  L String   : Variable or Property name
  *  L Info     : Info
  * WEntry      : (Boolean, Loc, String, Info)
  * RWMAP       : Map[Node, List[RWEntry]]
  * WMap        : Map[Node, List[WEntry]]
  ************************************************************/
  type RWEntry  = (Boolean, Boolean, Loc, String, Span)
  type WEntry   = (Boolean, Loc, String, Span)
  type RWMap    = Map[Node, List[RWEntry]] 
  type WMap     = Map[Node, Set[WEntry]] 

  /* BugType Constant, Definite Flag */
  val Warning         = 1
  val TypeError       = 2
  val ReferenceError  = 3 
  val definite_only   = true

  /* Constants */
  val write     = true
  val read      = false
  val variable  = true
  val property  = false
  val normalCall  = true
  val constCall   = false
  val checkObject   = true
  val checkFunction = false

  /* Stores all message formats */
  val bugTable: BugMap = MHashMap()

  def addBugMsgFormat(bid: BId, kind: BugType, msg: String, argNum: Int): Int = {bugTable(bid) = (kind, msg, argNum); bid}

  /* List of all bugs */
  val AbsentReadProperty    :BId = addBugMsgFormat(1, Warning, "Reading absent property '%s' of an object.", 1)
  val AbsentReadVariable    :BId = addBugMsgFormat(2, ReferenceError, "Reading absent variable '%s'.", 1)
  val BinaryOpIn            :BId = addBugMsgFormat(3, TypeError, "Right-hand side operand ‘%s’ of In operator is non-object.", 1)
  val BinaryOpInstanceOf1   :BId = addBugMsgFormat(4, TypeError, "Right-hand side operand ‘%s’ of InstanceOf operator is non-object.", 1)
  val BinaryOpInstanceOf2   :BId = addBugMsgFormat(5, TypeError, "Right-hand side operand ‘%s’ of InstanceOf operator is non-function object.", 1)
  val BinaryOpInstanceOf3   :BId = addBugMsgFormat(6, Warning, "Right-hand side operand ‘%s’ of InstanceOf operator is of non-object prototype.", 1)
  val BuiltinArgSizeFew     :BId = addBugMsgFormat(7, Warning, "Too few arguments to function '%s'.", 1)
  val BuiltinArgSizeMany    :BId = addBugMsgFormat(8, Warning, "Too many arguments to function '%s'.", 1)
  val BuiltinWrongType      :BId = addBugMsgFormat(9, Warning, "First argument of ‘%s’ should be a(n) ‘%s’.", 2)
  val CallConstFunc         :BId = addBugMsgFormat(10, Warning, "Calling a function '%s' both as a function and a constructor.", 1)
  val CallNonConstructor    :BId = addBugMsgFormat(11, TypeError, "Calling a non-constructor '%s' as a constructor.", 1)
  val CallNonFunction       :BId = addBugMsgFormat(12, TypeError, "Calling a non-function as a function.", 0)
  val CondBranchTrue        :BId = addBugMsgFormat(13, Warning, "The conditional expression is always true.", 0)
  val CondBranchFalse       :BId = addBugMsgFormat(14, Warning, "The conditional expression is always false.", 0)
  val ConvertUndeftoNum     :BId = addBugMsgFormat(15, Warning, "Trying to convert undefined to number.", 0)
  val DefaultValueTypeError :BId = addBugMsgFormat(16, TypeError, "Computing default value (toString, valueOf) of the object '%s' yields TypeError.", 1)
  val GlobalThisDefinite    :BId = addBugMsgFormat(17, Warning, "'this' refers the global object.", 0)
  val GlobalThisMaybe       :BId = addBugMsgFormat(18, Warning, "'this' may refer the global object.", 0)
  val ImplicitCalltoString  :BId = addBugMsgFormat(19, Warning, "Implicit toString type-conversion to object '%s' by non-builtin toString method.", 1)
  val ImplicitCallvalueOf   :BId = addBugMsgFormat(20, Warning, "Implicit valueOf type-conversion to object '%s' by non-builtin valueOf method.", 1)
  val ObjectNull            :BId = addBugMsgFormat(21, TypeError, "Accessing a property of null.", 0)
  val ObjectNullOrUndef     :BId = addBugMsgFormat(22, TypeError, "Accessing a property of null (or undefined).", 0)
  val ObjectUndef           :BId = addBugMsgFormat(23, TypeError, "Accessing a property of undefined.", 0)
  val PrimitiveToObject     :BId = addBugMsgFormat(24, Warning, "Trying to convert primitive value(%s) to object.", 1)
  val ShadowedFuncByFunc    :BId = addBugMsgFormat(25, Warning, "Function '%s' is shadowed by a function at '%s'.", 2)
  val ShadowedParamByFunc   :BId = addBugMsgFormat(26, Warning, "Parameter '%s' is shadowed by a function at '%s'.", 2)
  val ShadowedVarByFunc     :BId = addBugMsgFormat(27, Warning, "Variable '%s' is shadowed by a function at '%s'.", 2)
  val ShadowedVarByParam    :BId = addBugMsgFormat(28, Warning, "Variable '%s' is shadowed by a paramater at '%s'.", 2)
  val ShadowedVarByVar      :BId = addBugMsgFormat(29, Warning, "Variable '%s' is shadowed by a variable at '%s'.", 2)
  val UnreachableCode       :BId = addBugMsgFormat(30, Warning, "Unreachable code is found.", 0)
  val UnusedFunction        :BId = addBugMsgFormat(31, Warning, "Function '%s' is never used.", 1)
  val UnusedProperty        :BId = addBugMsgFormat(32, Warning, "Property '%s' is never used.", 1)
  val UnusedVariable        :BId = addBugMsgFormat(33, Warning, "Variable '%s' is never used.", 1)
  val VaryingTypeArguments  :BId = addBugMsgFormat(34, Warning, "Calling a function '%s' with arguments of varying type.", 1)
  val WrongThisType         :BId = addBugMsgFormat(35, TypeError, "Native function '%s' is called when its 'this' value is not of the expected object type.", 1)

  def getFuncName(name: String) = if (NU.isFunExprName(name)) "anonymous_function" else name

  /* 
  * ToStringSet: Set of builtin methods that use ToString internally 
  * ToNumberSet: Set of builtin methods that use ToNumber internally 
  * (Optional) distinguish different arguments in built-in Methods?
  */
  val ToStringSet: Set[String] = Set("String.prototype.charAt", "String.prototype.charCodeAt", "String.prototype.concat", 
    "String.prototype.indexOf", "String.prototype.lastIndexOf", "String.prototype.localeCompare", "String.prototype.match", 
    "String.prototype.replace", "String.prototype.search", "String.prototype.slice", "String.prototype.slice", 
    "String.prototype.split", "String.prototype.substring", "String.prototype.toLowerCase", "String.prototype.trim")

  val ToNumberSet: Set[String] = Set("Global.isNaN", "Global.isFinite", "Date.prototype.setTime", "Date.prototype.setMilliseconds",
    "Date.prototype.setUTCMilliseconds", "Date.prototype.setSeconds", "Date.prototype.setUTCSeconds", "Date.prototype.setMinutes", 
    "Date.prototype.setUTCMinutes", "Date.prototype.setHours", "Date.prototype.setUTCHours", "Date.prototype.setDate", "Date.prototype.setUTCDate",
    "Date.prototype.setMonth", "Date.prototype.setUTCMonth", "Date.prototype.setFullYear", "Date.prototype.setUTCFullYear")

  /* Set of all toString and valueOf builtin methods */
  val internalMethodMap: Map[String, Set[String]] = Map(
    "toString" -> Set("Object.prototype.toString", "Function.prototype.toString", "Array.prototype.toString", 
                      "String.prototype.toString", "Boolean.prototype.toString", "Number.prototype.toString", 
                      "Date.prototype.toString", "RegExp.prototype.toString", "Error.prototype.toString"), 
    "valueOf"  -> Set("Object.prototype.valueOf", "String.prototype.valueOf", "Boolean.prototype.valueOf", 
                      "Number.prototype.valueOf", "Date.prototype.valueOf"))
  
  /* Map each builtin methods to its argument size (min, max)  */ 
  val argSizeMap: Map[String, (Int, Int)] = Map(
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

  /* Map each builtin methods to its argument type */ 
  val argTypeMap: Map[String, (Boolean, String)] = Map(
    "Object.getPrototypeOf" -> (checkObject, "Object"),
    "Object.getOwnPropertyDescriptor" -> (checkObject, "Object"),
    "Object.getOwnPropertyNames" -> (checkObject, "Object"),
    "Object.create" -> (checkObject, "Object"),
    "Object.defineProperty" -> (checkObject, "Object"),
    "Object.defineProperties" -> (checkObject, "Object"),
    "Object.seal" -> (checkObject, "Object"),
    "Object.freeze" -> (checkObject, "Object"),
    "Object.preventExtensions" -> (checkObject, "Object"),
    "Object.isSealed" -> (checkObject, "Object"),
    "Object.isFrozen" -> (checkObject, "Object"),
    "Object.isExtensible" -> (checkObject, "Object"),
    "Object.keys" -> (checkObject, "Object"),
    "Array.prototype.sort" -> (checkFunction, "Function"),
    "Array.prototype.every" -> (checkFunction, "Function"),
    "Array.prototype.some" -> (checkFunction, "Function"),
    "Array.prototype.forEach" -> (checkFunction, "Function"),
    "Array.prototype.map" -> (checkFunction, "Function"),
    "Array.prototype.filter" -> (checkFunction, "Function"),
    "Array.prototype.reduce" -> (checkFunction, "Function"),
    "Array.prototype.reduceRight" -> (checkFunction, "Function"))

  /* Set of builtin methods that cannot be used as a constructor */
  val nonConsSet: Set[String] = Set(
    "Global.eval", "Global.parseInt", "Global.parseFloat", "Global.isNaN", "Global.isFinite",
    "Global.decodeURI", "Global.decodeURIComponent", "Global.encodeURI", "Global.encodeURIComponent",
    "Math.abs", "Math.acos", "Math.asin", "Math.atan", "Math.atan2", "Math.ceil", "Math.cos", 
    "Math.exp", "Math.floor", "Math.log", "Math.max", "Math.min", "Math.pow", "Math.random", 
    "Math.round", "Math.sin", "Math.sqrt", "Math.tan", "JSON.parse", "JSON.stringify")

  /* Map each builtin methods with its proper type of 'this' */
  val thisTypeMap: Map[String, String] = Map(
    "String.prototype.toString" -> "String", "String.prototype.valueOf" -> "String", 
    "Boolean.prototype.toString" -> "Boolean", "Boolean.prototype.valueOf" -> "Boolean", 
    "Number.prototype.toString" -> "Number", "Number.prototype.valueOf" -> "Number")
}
