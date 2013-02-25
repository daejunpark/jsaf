/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.models

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.Helper
import scala.collection.immutable.TreeMap
import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.interpreter.InterpreterPredefine
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.nodes_util.NodeUtil
import kr.ac.kaist.jsaf.analysis.typing.Config
import kr.ac.kaist.jsaf.analysis.typing.CallContext

class BuiltinModel(cfg: CFG) {
  val F = BoolFalse
  val T = BoolTrue
  val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("Built-in Object"))
  var fset_builtin = Map[FunctionId, String]()

  /**
   * Code for |> operator.
   */
  case class ToPipe[A](a: A) {
    def |>[B](f: A => B) = f(a)
  }
  implicit def convert[A](s: A) = ToPipe(s)

  private var uniqueNameCounter = 0
  private def freshName(name: String) = {
    uniqueNameCounter += 1
    "<>Builtin<>" + name + "<>" + uniqueNameCounter.toString
  }

  /**
   * Create cfg nodes for a built-in function which is composed of ENTRY, single command body, EXIT and EXIT-EXC.
   *
   * @param builtinCall built-in call name
   * @return created function id
   */
  def makeBuiltinCFG(builtinCall: String) : FunctionId = {
    val nameArg = freshName("arguments")
    val fid = cfg.newFunction(nameArg, List[CFGId](), List[CFGId](), builtinCall, dummyInfo)
    val node = cfg.newBlock(fid)

    cfg.addEdge((fid, LEntry), node)
    cfg.addEdge(node, (fid,LExit))
    cfg.addExcEdge(node, (fid,LExitExc))
    cfg.addInst(node, 
                CFGBuiltinCall(cfg.newInstId, 
                               builtinCall,
                               CFGVarRef(dummyInfo, CFGTempId(nameArg, PureLocalVar)), 
                               cfg.newAddress, cfg.newAddress, cfg.newAddress, cfg.newAddress))

    fset_builtin = fset_builtin + (fid -> builtinCall)

    (fid)
  }

  def makeBuiltinAftercallCFG(builtinCall: String) : FunctionId = {
    val nameArg = freshName("arguments")
    val rtn = freshName("temp")
    val fid = cfg.newFunction(nameArg, List[CFGId](), List[CFGId](), builtinCall, dummyInfo)
    val call_node = cfg.newBlock(fid)
    val return_node = cfg.newAfterCallBlock(fid, CFGTempId(rtn, PureLocalVar))

    cfg.addEdge((fid, LEntry), call_node)
    cfg.addEdge(return_node, (fid,LExit))
    cfg.addCall(call_node, return_node)
    cfg.addExcEdge(call_node, (fid,LExitExc))
    cfg.addExcEdge(return_node, (fid,LExitExc))

    // []built-in-call
    cfg.addInst(call_node, 
                CFGBuiltinCall(cfg.newInstId,
                               builtinCall, 
                               CFGVarRef(dummyInfo, CFGTempId(nameArg, PureLocalVar)),
                               cfg.newAddress, cfg.newAddress, cfg.newAddress, cfg.newAddress))

    // after-call(x)
    // return x;
    cfg.addInst(return_node, 
                CFGReturn(cfg.newInstId, dummyInfo, 
                          Some(CFGVarRef(dummyInfo, CFGTempId(rtn, PureLocalVar)))))

    fset_builtin = fset_builtin + (fid -> builtinCall)

    (fid)
  }

  sealed abstract class AbsProperty
  case class AbsBuiltinFunc(id: String, length: Double) extends AbsProperty
  case class AbsBuiltinFuncAftercall(id: String, length: Double) extends AbsProperty
  case class AbsConstValue(v: PropValue) extends AbsProperty

  /**
   * Preparing the given AbsProperty to be updated.
   * If a property is a built-in function, create a new function object and pass it to name, value and object pair.
   * If a property is a constant value, pass it to name, value and object pair. At this time, object is None.
   *
   * @param name the name of each property
   * @param v the value of each property.
   */
  def prepareForUpdate(name: String, v: AbsProperty) = {
    v match {
      case AbsBuiltinFunc(id, length) => {
        val fid = makeBuiltinCFG(id)
        val loc = newLoc(id)
        val obj = Helper.NewFunctionObject(Some(fid), None, GlobalSingleton, None, AbsNumber.alpha(length))
          (name, PropValue(ObjectValue(loc, T, F, T)), Some(loc, obj))
      }
      case AbsBuiltinFuncAftercall(id, length) => {
        val fid = makeBuiltinAftercallCFG(id)
        val loc = newLoc(id)
        val obj = Helper.NewFunctionObject(Some(fid), None, GlobalSingleton, None, AbsNumber.alpha(length))
          (name, PropValue(ObjectValue(loc, T, F, T)), Some(loc, obj))
      }
      case AbsConstValue(value) => (name, value, None)
    }
  }

  /**
   * Initialize general built-in object.
   */
  def initGeneral(
    name: String,
    loc_con: Loc,
    obj_con: Obj,
    list_con: List[Tuple2[String, AbsProperty]],
    loc_proto: Loc,
    obj_proto: Obj,
    list_proto: List[Tuple2[String, AbsProperty]],
    map: HeapMap
  ): HeapMap
  = initGeneral(name, loc_con, obj_con, list_con, Some(loc_proto, obj_proto, list_proto), map)

  /**
   * Initialize general built-in object.
   */
  def initGeneral(
    name: String,
    loc_con: Loc,
    obj_con: Obj,
    list_con: List[Tuple2[String, AbsProperty]],
    map: HeapMap
  ): HeapMap
  = initGeneral(name, loc_con, obj_con, list_con, None,  map)
  
  /**
   * Initialize general built-in object. Prototype object can be optional(for Math).
   *
   * @param name Built-in object name by which the object can be accessed.
   * @param loc_con Loc value for constructor(or object).
   * @param obj_con Initial object for constructor.
   * @param list_con Property list for constructor.
   * @param proto Loc, initial object, property list for prototype(Optional)
   * @param map global heap
   */
  def initGeneral(
    name: String,
    loc_con: Loc,
    obj_con: Obj,
    list_con: List[Tuple2[String, AbsProperty]],
    proto: Option[(Loc, Obj, List[Tuple2[String, AbsProperty]])],
    map: HeapMap
  ): HeapMap = {

    // Create function dummies for constructor
    val obj_list = list_con.map((x) => prepareForUpdate(x._1, x._2))
    // Add properties to constructor object
    val obj_con_1 = obj_list.foldLeft(obj_con)((obj, v) => obj.update(AbsString.alpha(v._1), v._2))

    // Adds to global area
    val global = map(GlobalLoc).update(AbsString.alpha(name),
                                       PropValue(ObjectValue(loc_con, T, F, T)))
    // Make builtin properties to be dumped only in verbose mode
    if (!name.startsWith("@")) Config.globalVerboseProp.add(name) 
  
    val map_1 = map + (GlobalLoc -> global) + (loc_con -> obj_con_1)

    val map_2 = proto match {
      case Some((loc, obj_proto, list_proto)) => {
        // Create function dummies for prototype object
        val proto_list = list_proto.map((x) => prepareForUpdate(x._1, x._2))
        // Add properties to prototype object
        val obj_proto_1 = proto_list.foldLeft(obj_proto)((obj, v) => obj.update(AbsString.alpha(v._1), v._2))

        // Adds new allocated objects
        val m_1 = map_1 + (loc -> obj_proto_1)
        val m_2 = proto_list.foldLeft(m_1)((m, v) => v._3 match {
          case Some((l, o)) => m + (l -> o)
          case None => m
        })
          m_2
      }
      case _ => map_1
    }

    // Adds new allocated objects
    obj_list.foldLeft(map_2)((m, v) => v._3 match {
      case Some((l, o)) => m + (l -> o)
      case None => m
    })
  }

  def initGlobal(map: HeapMap): HeapMap = {
    val list_obj = List(
      ("@class",             AbsConstValue(PropValue(AbsString.alpha("Object")))), // implementation dependent
      ("@proto",             AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))), // implementation dependent
      ("@extensible",        AbsConstValue(PropValue(BoolTrue))),
      // 15.1.1 Value Properties of the Global Object
      ("NaN",                AbsConstValue(PropValue(ObjectValue(NaN, F, F, F)))),
      ("Infinity",           AbsConstValue(PropValue(ObjectValue(PosInf, F, F, F)))),
      ("undefined",          AbsConstValue(PropValue(ObjectValue(UndefTop, F, F, F)))),
      // 15.1.2 Function Properties of the Global Object
      ("eval",               AbsBuiltinFunc("Global.eval", 1)),
      ("parseInt",           AbsBuiltinFunc("Global.parseInt", 2)),
      ("parseFloat",         AbsBuiltinFunc("Global.parseFloat", 1)),
      ("isNaN",              AbsBuiltinFunc("Global.isNaN", 1)),
      ("isFinite",           AbsBuiltinFunc("Global.isFinite", 1)),
      // 15.1.3 URI Handling Function Properties
      ("decodeURI",          AbsBuiltinFunc("Global.decodeURI", 1)),
      ("decodeURIComponent", AbsBuiltinFunc("Global.decodeURIComponent", 1)),
      ("encodeURI",          AbsBuiltinFunc("Global.encodeURI", 1)),
      ("encodeURIComponent", AbsBuiltinFunc("Global.encodeURIComponent", 1)),
      // predefined constant variables from IR
      (InterpreterPredefine.varTrue, AbsConstValue(PropValue(ObjectValue(BoolTrue, F, F, F)))),
      (InterpreterPredefine.varOne, AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1.0), F, F, F)))),
      (NodeUtil.freshGlobalName("global"), AbsConstValue(PropValue(ObjectValue(Value(GlobalSingleton), F, F, F)))),
      // Browser supports
      ("alert",              AbsBuiltinFunc("Global.alert", 1))
    )

    // Create function dummies for prototype object
    val obj_list = list_obj.map((x) => prepareForUpdate(x._1, x._2))
    // Add properties to global object
    val global = obj_list.foldLeft(map(GlobalLoc))((obj, v) => {
      // Make builtin properties to be dumped only in verbose mode
      if (!v._1.startsWith("@")) Config.globalVerboseProp.add(v._1)
      
      obj.update(AbsString.alpha(v._1), v._2)
    })

    // Adds new allocated objects
    val map_1 =
      obj_list.foldLeft(map)((m, v) => v._3 match {
        case Some((l, o)) => m + (l -> o)
        case None => m
      })

    map_1 + (GlobalLoc -> global)
  }

  def initObject(map: HeapMap): HeapMap = {
    val loc_con = ObjConstLoc
    val loc_proto = ObjProtoLoc

    // 15.2.3 The Object constructor
    val fid_obj = makeBuiltinCFG("Object")
    val fid_con = makeBuiltinCFG("Object.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_obj), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.2.3 Properties of the Object Constructor
    val list_con = List(
      ("getPrototypeOf",           AbsBuiltinFunc("Object.getPrototypeOf", 1)),
      ("getOwnPropertyDescriptor", AbsBuiltinFunc("Object.getOwnPropertyDescriptor", 2)),
      ("getOwnPropertyNames",      AbsBuiltinFunc("Object.getOwnPropertyNames", 1)),
      ("create",                   AbsBuiltinFunc("Object.create", 2)),
      ("defineProperty",           AbsBuiltinFunc("Object.defineProperty", 3)),
      ("defineProperties",         AbsBuiltinFunc("Object.defineProperties", 2)),
      ("seal",                     AbsBuiltinFunc("Object.seal", 1)),
      ("freeze",                   AbsBuiltinFunc("Object.freeze", 1)),
      ("preventExtensions",        AbsBuiltinFunc("Object.preventExtensions", 1)),
      ("isSealed",                 AbsBuiltinFunc("Object.isSealed", 1)),
      ("isFrozen",                 AbsBuiltinFunc("Object.isFrozen", 1)),
      ("isExtensible",             AbsBuiltinFunc("Object.isExtensible", 1)),
      ("keys",                     AbsBuiltinFunc("Object.keys", 1))
    )

    // 15.2.4 Properties of the Object Prototype Object
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(PValue(NullTop), F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("toString",             AbsBuiltinFunc("Object.prototype.toString", 0)),
      ("toLocaleString",       AbsBuiltinFunc("Object.prototype.toLocaleString", 0)),
      ("valueOf",              AbsBuiltinFunc("Object.prototype.valueOf", 0)),
      ("hasOwnProperty",       AbsBuiltinFunc("Object.prototype.hasOwnProperty", 1)),
      ("isPrototypeOf",        AbsBuiltinFunc("Object.prototype.isPrototypeOf", 1)),
      ("propertyIsEnumerable", AbsBuiltinFunc("Object.prototype.propertyIsEnumerable", 1))
    )

    initGeneral("Object", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map)
  }

  def initFunction(map: HeapMap): HeapMap = {
    val loc_con = FunctionConstLoc
    val loc_proto = FunctionProtoLoc

    // 15.3.2 The Function constructor
    val fid_con = makeBuiltinCFG("Function.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_con), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.3.3 Properties of the Function Constructor
    val list_con = List(
    )

    // 15.3.4 Properties of the Function Prototype Object
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Function")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("length",               AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0), F, F, F)))),
      ("toString",             AbsBuiltinFunc("Function.prototype.toString", 0)),
      ("apply",                AbsBuiltinFuncAftercall("Function.prototype.apply", 2)),
      ("call",                 AbsBuiltinFuncAftercall("Function.prototype.call", 1)),
      ("bind",                 AbsBuiltinFuncAftercall("Function.prototype.bind", 1))
    )
    
    // 15.3.4 Functin.prototype is a function 
    val proto_fid = makeBuiltinCFG("Function.prototype")
    val obj_proto = ObjEmpty.update("@function", PropValue(ObjectValueBot, ValueBot, FunSet(proto_fid)))

    initGeneral("Function", loc_con, obj_con, list_con, loc_proto, obj_proto, list_proto, map)
  }

  def initArray(map: HeapMap): HeapMap = {
    val loc_con = ArrayConstLoc
    val loc_proto = ArrayProtoLoc

    // 15.4.2 The Array Constructor
    val fid_obj = makeBuiltinCFG("Array")
    val fid_con = makeBuiltinCFG("Array.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_obj), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.4.3 Properties of the Array Constructor
    val list_con = List(
      ("isArray",             AbsBuiltinFunc("Array.isArray", 1))
    )

    // 15.4.4 Properties of the Array Prototype Object
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Array")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
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
      ("reduce",               AbsBuiltinFunc("Array.prototype.reduce", 1)),
      ("reduceRight",               AbsBuiltinFunc("Array.prototype.reduceRight", 1))
    )

    initGeneral("Array", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map)
  }

  def initString(map: HeapMap): HeapMap = {
    val loc_con = StringConstLoc
    val loc_proto = StringProtoLoc

    // 15.5.3 String constructor
    val fid_obj = makeBuiltinCFG("String")
    val fid_con = makeBuiltinCFG("String.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_obj), Some(fid_con), GlobalSingleton, Some(loc_proto), AbsNumber.alpha(7))

    // 15.5.3 Properties of the String Constructor
    val list_con = List(
      ("fromCharCode",           AbsBuiltinFunc("String.fromCharCode", 1))
    )

    // 15.5.4 Properties of the String Prototype Object
    val list_proto = List(
      ("@class",         AbsConstValue(PropValue(AbsString.alpha("String")))),
      ("@proto",         AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("@extensible",    AbsConstValue(PropValue(BoolTrue))),
      ("constructor",    AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
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
      ("toLowerCase",    AbsBuiltinFunc("String.prototype.toLowerCase", 0)),
      ("toLocaleLowerCase", AbsBuiltinFunc("String.prototype.toLocaleLowerCase", 0)),
      ("toUpperCase",    AbsBuiltinFunc("String.prototype.toUpperCase", 0)),
      ("toLocaleUpperCase", AbsBuiltinFunc("String.prototype.toLocaleUpperCase", 0)),
      ("trim",           AbsBuiltinFunc("String.prototype.trim", 0))
    )

    initGeneral("String", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map)
  }

  def initBoolean(map: HeapMap): HeapMap = {
    val loc_con = BooleanConstLoc
    val loc_proto = BooleanProtoLoc

    // 15.5.3 Boolean constructor
    val fid_obj = makeBuiltinCFG("Boolean")
    val fid_con = makeBuiltinCFG("Boolean.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_obj), Some(fid_con), GlobalSingleton, Some(loc_proto), AbsNumber.alpha(1))

    // 15.6.3 Properties of the Boolean Constructor
    val list_con = List(
    )
    
    // 15.6.4 Properties of the Boolean Prototype Object
    val list_proto = List(
      ("@class",         AbsConstValue(PropValue(AbsString.alpha("Boolean")))),
      ("@proto",         AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("@extensible",    AbsConstValue(PropValue(BoolTrue))),
      ("constructor",    AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("toString",       AbsBuiltinFunc("Boolean.prototype.toString", 0)),
      ("valueOf",        AbsBuiltinFunc("Boolean.prototype.valueOf", 0))
    )

    initGeneral("Boolean", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map)
  }

  def initNumber(map: HeapMap): HeapMap = {
    val loc_con = NumberConstLoc
    val loc_proto = NumberProtoLoc

    // 15.7.2 Number constructor
    val fid_obj = makeBuiltinCFG("Number")
    val fid_con = makeBuiltinCFG("Number.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_obj), Some(fid_con), GlobalSingleton, Some(loc_proto), AbsNumber.alpha(1))

    // 15.7.3 Properties of the Number Constructor
    val list_con = List(
      ("MAX_VALUE",         AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(Double.MaxValue), F, F, F)))),
      ("MIN_VALUE",         AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(java.lang.Double.MIN_VALUE), F, F, F)))),
      ("NaN",               AbsConstValue(PropValue(ObjectValue(NaN, F, F, F)))),
      ("NEGATIVE_INFINITY", AbsConstValue(PropValue(ObjectValue(NegInf, F, F, F)))),
      ("POSITIVE_INFINITY", AbsConstValue(PropValue(ObjectValue(PosInf, F, F, F))))
    )
    
    // 15.7.4 Properties of the Number Prototype Object
    val list_proto = List(
      ("@class",         AbsConstValue(PropValue(AbsString.alpha("Number")))),
      ("@proto",         AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("@extensible",    AbsConstValue(PropValue(BoolTrue))),
      ("@primitive",     AbsConstValue(PropValue(AbsNumber.alpha(+0)))),
      ("constructor",    AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("toString",       AbsBuiltinFunc("Number.prototype.toString", 0)),
      ("toLocaleString",       AbsBuiltinFunc("Number.prototype.toLocaleString", 0)),
      ("valueOf",        AbsBuiltinFunc("Number.prototype.valueOf", 0)),
      ("toFixed",        AbsBuiltinFunc("Number.prototype.toFixed", 1)),
      ("toExponential",        AbsBuiltinFunc("Number.prototype.toExponential", 1)),
      ("toPrecision",        AbsBuiltinFunc("Number.prototype.toPrecision", 1))
    )

    initGeneral("Number", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map)
  }

  def initMath(map: HeapMap): HeapMap = {
    val loc_con = newLoc("MathConst")

    // 15.8 The Math Object
    val obj_con = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("Math"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(ObjProtoLoc, F, F, F))).
      update(OtherStrSingle("constructor"), PropValue(ObjectValue(ObjConstLoc, F, F, F)))

    val list_con = List(
      // 15.8.1 Value Properties of the Math Object
      ("E",       AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(2.7182818284590452354), F, F, F)))),
      ("LN10",    AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(2.302585092994046), F, F, F)))),
      ("LN2",     AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0.6931471805599453), F, F, F)))),
      ("LOG2E",   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1.4426950408889634), F, F, F)))),
      ("LOG10E",  AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0.4342944819032518), F, F, F)))),
      ("PI",      AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(3.1415926535897932), F, F, F)))),
      ("SQRT1_2", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0.7071067811865476), F, F, F)))),
      ("SQRT2",   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1.4142135623730951), F, F, F)))),
      // 15.8.2 Function Properties of the Math Object
      ("abs",     AbsBuiltinFunc("Math.abs",    1)),
      ("acos",    AbsBuiltinFunc("Math.acos",    1)),
      ("asin",    AbsBuiltinFunc("Math.asin",    1)),
      ("atan",    AbsBuiltinFunc("Math.atan",    1)),
      ("atan2",   AbsBuiltinFunc("Math.atan2",    1)),
      ("ceil",    AbsBuiltinFunc("Math.ceil",   1)),
      ("cos",     AbsBuiltinFunc("Math.cos",    1)),
      ("exp",     AbsBuiltinFunc("Math.exp",    1)),
      ("floor",   AbsBuiltinFunc("Math.floor",  1)),
      ("log",     AbsBuiltinFunc("Math.log",    1)),
      ("max",     AbsBuiltinFunc("Math.max",    2)),
      ("min",     AbsBuiltinFunc("Math.min",    2)),
      ("pow",     AbsBuiltinFunc("Math.pow",    2)),
      ("random",  AbsBuiltinFunc("Math.random", 0)),
      ("round",   AbsBuiltinFunc("Math.round",  1)),
      ("sin",     AbsBuiltinFunc("Math.sin",    1)),
      ("sqrt",    AbsBuiltinFunc("Math.sqrt",   1)),
      ("tan",     AbsBuiltinFunc("Math.tan",   1))
    )

    initGeneral("Math", loc_con, obj_con, list_con, map)
  }

  def initDate(map: HeapMap): HeapMap = {
    val loc_proto = DateProtoLoc
    val loc_con = newLoc("DateConst")

    // 15.9.3 The Date constructor
    val fid_obj = makeBuiltinCFG("Date")
    val fid_con = makeBuiltinCFG("Date.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_obj), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(7))

    // 15.9.4 Properties of the Date Constructor
    val list_con = List(
      ("parse", AbsBuiltinFunc("Date.parse", 1)),
      ("UTC",   AbsBuiltinFunc("Date.UTC", 7)),
      ("now",   AbsBuiltinFunc("Date.now", 0))
    )

    // 15.9.5 Properties of the Date Prototype Object
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Date")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("toString", AbsBuiltinFunc("Date.prototype.toString", 0)),
      ("toDateString", AbsBuiltinFunc("Date.prototype.toDateString", 0)),
      ("toTimeString", AbsBuiltinFunc("Date.prototype.toTimeString", 0)),
      ("toLocaleString", AbsBuiltinFunc("Date.prototype.toLocaleString", 0)),
      ("toLocaleDateString", AbsBuiltinFunc("Date.prototype.toLocaleDateString", 0)),
      ("toLocaleTimeString", AbsBuiltinFunc("Date.prototype.toLocaleTimeString", 0)),
      ("valueOf", AbsBuiltinFunc("Date.prototype.valueOf", 0)),
      ("getTime", AbsBuiltinFunc("Date.prototype.getTime", 0)),
      ("getFullYear", AbsBuiltinFunc("Date.prototype.getFullYear", 0)),
      ("getUTCFullYear", AbsBuiltinFunc("Date.prototype.getUTCFullYear", 0)),
      ("getMonth", AbsBuiltinFunc("Date.prototype.getMonth", 0)),
      ("getUTCMonth", AbsBuiltinFunc("Date.prototype.getUTCMonth", 0)),
      ("getDate", AbsBuiltinFunc("Date.prototype.getDate", 0)),
      ("getUTCDate", AbsBuiltinFunc("Date.prototype.getUTCDate", 0)),
      ("getDay", AbsBuiltinFunc("Date.prototype.getDay", 0)),
      ("getUTCDay", AbsBuiltinFunc("Date.prototype.getUTCDay", 0)),
      ("getHours", AbsBuiltinFunc("Date.prototype.getHours", 0)),
      ("getUTCHours", AbsBuiltinFunc("Date.prototype.getUTCHours", 0)),
      ("getMinutes", AbsBuiltinFunc("Date.prototype.getMinutes", 0)),
      ("getUTCMinutes", AbsBuiltinFunc("Date.prototype.getUTCMinutes", 0)),
      ("getSeconds", AbsBuiltinFunc("Date.prototype.getSeconds", 0)),
      ("getUTCSeconds", AbsBuiltinFunc("Date.prototype.getUTCSeconds", 0)),
      ("getMilliseconds", AbsBuiltinFunc("Date.prototype.getMilliseconds", 0)),
      ("getUTCMilliseconds", AbsBuiltinFunc("Date.prototype.getUTCMilliseconds", 0)),
      ("getTimezoneOffset", AbsBuiltinFunc("Date.prototype.getTimezoneOffset", 0)),
      ("setTime", AbsBuiltinFunc("Date.prototype.setTime", 1)),
      ("setMilliseconds", AbsBuiltinFunc("Date.prototype.setMilliseconds", 1)),
      ("setUTCMilliseconds", AbsBuiltinFunc("Date.prototype.setUTCMilliseconds", 1)),
      ("setSeconds", AbsBuiltinFunc("Date.prototype.setSeconds", 2)),
      ("setUTCSeconds", AbsBuiltinFunc("Date.prototype.setUTCSeconds", 2)),
      ("setMinutes", AbsBuiltinFunc("Date.prototype.setMinutes", 3)),
      ("setUTCMinutes", AbsBuiltinFunc("Date.prototype.setUTCMinutes", 3)),
      ("setHours", AbsBuiltinFunc("Date.prototype.setHours", 4)),
      ("setUTCHours", AbsBuiltinFunc("Date.prototype.setUTCHours", 4)),
      ("setDate", AbsBuiltinFunc("Date.prototype.setDate", 1)),
      ("setUTCDate", AbsBuiltinFunc("Date.prototype.setUTCDate", 1)),
      ("setMonth", AbsBuiltinFunc("Date.prototype.setMonth", 2)),
      ("setUTCMonth", AbsBuiltinFunc("Date.prototype.setUTCMonth", 2)),
      ("setFullYear", AbsBuiltinFunc("Date.prototype.setFullYear", 3)),
      ("setUTCFullYear", AbsBuiltinFunc("Date.prototype.setUTCFullYear", 3)),
      ("toUTCString", AbsBuiltinFunc("Date.prototype.toUTCString", 0)),
      ("toISOString", AbsBuiltinFunc("Date.prototype.toISOString", 0)),
      ("toJSON", AbsBuiltinFunc("Date.prototype.toJSON", 1))
    )

    initGeneral("Date", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map)
  }

  def initRegExp(map: HeapMap): HeapMap = {
    val loc_proto = newLoc("RegExpProto")
    val loc_con = newLoc("RegExpConst")

    // 15.10.4 The RegExp constructor
    val fid_obj = makeBuiltinCFG("RegExp")
    val fid_con = makeBuiltinCFG("RegExp.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_obj), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(2))

    // 15.10.5 Properties of the RegExp Constructor
    val list_con = List(
    )

    // 15.10.6 Properties of the RegExp Prototype Object
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("RegExp")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("exec",                 AbsBuiltinFunc("RegExp.prototype.exec", 1)),
      ("test",                 AbsBuiltinFunc("RegExp.prototype.test", 1)),
      ("toString",             AbsBuiltinFunc("RegExp.prototype.toString", 0))
    )

    initGeneral("RegExp", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map)
  }

  def initError(map: HeapMap): HeapMap = {
    val loc_proto = ErrProtoLoc
    val loc_con = newLoc("ErrConst")

    // 15.11.2 The Error constructor
    val fid_con = makeBuiltinCFG("Error.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_con), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.11.3 Properties of the Error Constructor
    val list_con = List(
    )

    // 15.11.4 Properties of the Error Prototype Object
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Error")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("name",                 AbsConstValue(PropValue(ObjectValue(AbsString.alpha("Error"), T, F, T)))),
      ("message",              AbsConstValue(PropValue(ObjectValue(AbsString.alpha(""), T, F, T)))),
      ("toString",             AbsBuiltinFunc("Error.prototype.toString", 0))
    )
    
    
    // 15.11.5 Error instance (only one location)
    val err = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("Error"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(ErrProtoLoc, F, F, F))).
      update(OtherStrSingle("@extensible"), PropValue(BoolTrue)).
      update(OtherStrSingle("length"),      PropValue(ObjectValue(AbsNumber.alpha(1), F, F, F)))

      
    val map_1 = map + (ErrLoc -> err)

    initGeneral("Error", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map_1)
  }
  
  def initEvalError(map: HeapMap): HeapMap = {
    val loc_proto = EvalErrProtoLoc
    val loc_con = newLoc("EvalErrConst")

    // 15.11.7.3 The NativeError Constructors
    val fid_con = makeBuiltinCFG("EvalError.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_con), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.11.7.5 Properties of the NativeError Constructors
    val list_con = List(
    )

    // 15.11.7.7 Properties of the NativeError Prototype Objects
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Error")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ErrProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("name",                 AbsConstValue(PropValue(ObjectValue(AbsString.alpha("EvalError"), T, F, T)))),
      ("message",              AbsConstValue(PropValue(ObjectValue(AbsString.alpha(""), T, F, T))))
    )
    
    //15.11.7.11 NativeError instance (only one location)
    val evalerr = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("Error"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(EvalErrProtoLoc, F, F, F))).
      update(OtherStrSingle("@extensible"), PropValue(BoolTrue))
      
    val map_1 = map + (EvalErrLoc -> evalerr)
      
    initGeneral("EvalError", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map_1)
  }
  def initRangeError(map: HeapMap): HeapMap = {
    val loc_proto = RangeErrProtoLoc
    val loc_con = newLoc("RangeErrConst")

    // 15.11.7.3 The NativeError Constructors
    val fid_con = makeBuiltinCFG("RangeError.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_con), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.11.7.5 Properties of the NativeError Constructors
    val list_con = List(
    )

    // 15.11.7.7 Properties of the NativeError Prototype Objects
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Error")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ErrProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("name",                 AbsConstValue(PropValue(ObjectValue(AbsString.alpha("RangeError"), T, F, T)))),
      ("message",              AbsConstValue(PropValue(ObjectValue(AbsString.alpha(""), T, F, T))))
    )
    
    //15.11.7.11 NativeError instance (only one location)
    val rangeerr = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("Error"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(RangeErrProtoLoc, F, F, F))).
      update(OtherStrSingle("@extensible"), PropValue(BoolTrue))
      
    val map_1 = map + (RangeErrLoc -> rangeerr)
      
    initGeneral("RangeError", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map_1)
  }
  def initRefError(map: HeapMap): HeapMap = {
    val loc_proto = RefErrProtoLoc
    val loc_con = newLoc("RefErrConst")

    // 15.11.7.3 The NativeError Constructors
    val fid_con = makeBuiltinCFG("ReferenceError.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_con), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.11.7.5 Properties of the NativeError Constructors
    val list_con = List(
    )

    // 15.11.7.7 Properties of the NativeError Prototype Objects
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Error")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ErrProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("name",                 AbsConstValue(PropValue(ObjectValue(AbsString.alpha("ReferenceError"), T, F, T)))),
      ("message",              AbsConstValue(PropValue(ObjectValue(AbsString.alpha(""), T, F, T))))
    )
    
    //15.11.7.11 NativeError instance (only one location)
    val referr = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("Error"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(RefErrProtoLoc, F, F, F))).
      update(OtherStrSingle("@extensible"), PropValue(BoolTrue))
      
    val map_1 = map + (RefErrLoc -> referr)
      
    initGeneral("ReferenceError", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map_1)
  }
  def initSyntaxError(map: HeapMap): HeapMap = {
    val loc_proto = SyntaxErrProtoLoc
    val loc_con = newLoc("SyntaxErrConst")

    // 15.11.7.3 The NativeError Constructors
    val fid_con = makeBuiltinCFG("SyntaxError.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_con), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.11.7.5 Properties of the NativeError Constructors
    val list_con = List(
    )

    // 15.11.7.7 Properties of the NativeError Prototype Objects
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Error")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ErrProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("name",                 AbsConstValue(PropValue(ObjectValue(AbsString.alpha("SyntaxError"), T, F, T)))),
      ("message",              AbsConstValue(PropValue(ObjectValue(AbsString.alpha(""), T, F, T))))
    )
    
    //15.11.7.11 NativeError instance (only one location)
    val sytaxerr = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("Error"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(SyntaxErrProtoLoc, F, F, F))).
      update(OtherStrSingle("@extensible"), PropValue(BoolTrue))
      
    val map_1 = map + (SyntaxErrLoc -> sytaxerr)
      
    initGeneral("SyntaxError", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map_1)
  }
  def initTypeError(map: HeapMap): HeapMap = {
    val loc_proto = TypeErrProtoLoc
    val loc_con = newLoc("TypeErrProto")

    // 15.11.7.3 The NativeError Constructors
    val fid_con = makeBuiltinCFG("TypeError.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_con), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.11.7.5 Properties of the NativeError Constructors
    val list_con = List(
    )

    // 15.11.7.7 Properties of the NativeError Prototype Objects
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Error")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ErrProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("name",                 AbsConstValue(PropValue(ObjectValue(AbsString.alpha("TypeError"), T, F, T)))),
      ("message",              AbsConstValue(PropValue(ObjectValue(AbsString.alpha(""), T, F, T))))
    )
    
    //15.11.7.11 NativeError instance (only one location)
    val typeerr = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("Error"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(TypeErrProtoLoc, F, F, F))).
      update(OtherStrSingle("@extensible"), PropValue(BoolTrue))
      
    val map_1 = map + (TypeErrLoc -> typeerr)
      
    initGeneral("TypeError", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map_1)
  }
  def initURIError(map: HeapMap): HeapMap = {
    val loc_proto = URIErrProtoLoc
    val loc_con = newLoc("URIErrConst")

    // 15.11.7.3 The NativeError Constructors
    val fid_con = makeBuiltinCFG("URIError.constructor")
    val obj_con = Helper.NewFunctionObject(Some(fid_con), Some(fid_con), GlobalSingleton, Some(loc_proto), F, F, F, AbsNumber.alpha(1))

    // 15.11.7.5 Properties of the NativeError Constructors
    val list_con = List(
    )

    // 15.11.7.7 Properties of the NativeError Prototype Objects
    val list_proto = List(
      ("@class",               AbsConstValue(PropValue(AbsString.alpha("Error")))),
      ("@proto",               AbsConstValue(PropValue(ObjectValue(ErrProtoLoc, F, F, F)))),
      ("@extensible",          AbsConstValue(PropValue(BoolTrue))),
      ("constructor",          AbsConstValue(PropValue(ObjectValue(loc_con, F, F, F)))),
      ("name",                 AbsConstValue(PropValue(ObjectValue(AbsString.alpha("URIError"), T, F, T)))),
      ("message",              AbsConstValue(PropValue(ObjectValue(AbsString.alpha(""), T, F, T))))
    )
    
    //15.11.7.11 NativeError instance (only one location)
    val urierr = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("Error"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(URIErrProtoLoc, F, F, F))).
      update(OtherStrSingle("@extensible"), PropValue(BoolTrue))
      
    val map_1 = map + (URIErrLoc -> urierr)
      
    initGeneral("URIError", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map_1)
  }
  def initJSON(map: HeapMap): HeapMap = {
    val loc_con = newLoc("JSONConst")

    // 15.12 The JSON Object
    val obj_con = ObjEmpty.
      update(OtherStrSingle("@class"),      PropValue(AbsString.alpha("JSON"))).
      update(OtherStrSingle("@proto"),      PropValue(ObjectValue(ObjProtoLoc, F, F, F))).
      update(OtherStrSingle("constructor"), PropValue(ObjectValue(ObjConstLoc, F, F, F)))

    val list_con = List(
      ("parse",     AbsBuiltinFunc("JSON.parse",     2)), // the value of length property is from Chrome browser.
      ("stringify", AbsBuiltinFunc("JSON.stringify", 3))  // the value of length property is from Chrome browser.
    )

    initGeneral("JSON", loc_con, obj_con, list_con, map)
  }

  var initPureLocalObj: Obj = null
  var initHeap: Heap = HeapBot
  def getInitHeapPre() = {
    val initCP = ((cfg.getGlobalFId, LEntry), CallContext.globalCallContext)
    initHeap.update(cfg.getPureLocal(initCP), initPureLocalObj)
  }
  def getInitHeap() = {
    initHeap.update(SinglePureLocalLoc, initPureLocalObj)
  }

  def initialize(): Unit = {
    val F = BoolFalse
    val T = BoolTrue

    // predefined global values for test mode
    val global0 = Config.testMode match {
      case false => ObjEmpty
      case true =>
        Config.testModeProp.foldLeft(ObjEmpty)((obj, kv) => 
          obj.update(AbsString.alpha(kv._1), PropValue(ObjectValue(kv._2, F, F, F))))
    }
    // predefined global values for library mode
    val global1 = Config.libMode match {
      case false => global0
      case true =>
        Config.libModeProp.foldLeft(global0)((obj, kv) => 
          obj.update(AbsString.alpha(kv._1), PropValue(ObjectValue(kv._2, F, F, F))))
    }
      
    // Set up global pure local object.
    // return statement is not allowed in global code.  
    val globalPureLocal = Helper.NewPureLocal(GlobalSingleton, GlobalSingleton) - "@return"
    initPureLocalObj = globalPureLocal
    
    val map = HeapMapBot.
      updated(GlobalLoc, global1).
      updated(SinglePureLocalLoc, globalPureLocal).
      updated(CollapsedLoc, ObjEmpty).
      updated(ObjPseudoTopLoc, ObjPseudoTop)

    val m = map |>
      initGlobal |>
      initObject |>
      initFunction |>
      initArray |>
      initString |>
      initBoolean |>
      initNumber |>
      initMath |>
      initDate |>
      initRegExp |>
      initError |>
      initEvalError |>
      initRangeError |>
      initRefError |>
      initSyntaxError |>
      initTypeError |>
      initURIError |>
      initJSON

    initHeap = Heap(m)
  }
}
