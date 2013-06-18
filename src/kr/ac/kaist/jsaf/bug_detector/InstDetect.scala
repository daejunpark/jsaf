/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.models.ModelManager
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.nodes_util.EJSType

class InstDetect(bugDetector: BugDetector) {
  val cfg           = bugDetector.cfg
  val typing        = bugDetector.typing
  val bugStorage    = bugDetector.bugStorage
  val bugOption     = bugDetector.bugOption
  val varManager    = bugDetector.varManager
  val stateManager  = bugDetector.stateManager
  val libMode       = bugDetector.libMode



  ////////////////////////////////////////////////////////////////
  // Get a set of property names (String) from an AbsString
  ////////////////////////////////////////////////////////////////

  private def props(heap: Heap, loc: Loc, absString: AbsString): Set[String] = {
    if (!heap.domIn(loc)) Set()
    else {
      absString match {
        // ignore @default
        case StrTop => heap(loc).map.keySet.filter(s => !s.take(1).equals("@"))
        case NumStr => heap(loc).map.keySet.filter(s => !s.take(1).equals("@") && AbsString.alpha(s) <= NumStr)
        case OtherStr => heap(loc).map.keySet.filter(s => !s.take(1).equals("@") && AbsString.alpha(s) <= OtherStr)
        case NumStrSingle(s) => Set(s)
        case OtherStrSingle(s) => Set(s)
        case StrBot => Set()
      }
    }
  }



  ////////////////////////////////////////////////////////////////
  // Check whether given arg has suitable type
  ////////////////////////////////////////////////////////////////

  /* Previous code
  private def isCorrectType(foflag: Boolean, heap: Heap, arg: LocSet): Boolean = {
    if (foflag) {
      val argObj = arg.foldLeft(ObjBot)((obj, loc) => obj + heap(loc))
      if (argObj("0")._1._1._1._1 </ PValueBot) true else false
    } else {
      val argObj = arg.foldLeft(ObjBot)((obj, loc) => obj + heap(loc))
      val objVal = argObj("0")._1._1._1
      if (objVal._1 </ PValueBot) true
      else {
        val nonFuncLocSet = objVal._2.filter((loc) => BoolTrue <= heap(loc).domIn("@function"))
        if (nonFuncLocSet.isEmpty) true else false
      }
    }
  }
  */



  ////////////////////////////////////////////////////////////////
  // Filter real function name ("anonymous_function" if unnamed)
  ////////////////////////////////////////////////////////////////

  def filterUnnamedFunction(name: String): String =
    if (NU.isFunExprName(name)) "anonymous_function" else name



  ////////////////////////////////////////////////////////////////
  // Get
  ////////////////////////////////////////////////////////////////

  def getFuncOrConstPropName(heap: Heap, funLoc: Loc, isCall: Boolean): String = {
    // Function must have [[Function]] or [[Construct]] property
    if(isCall) {
      if(BoolTrue <= Helper.IsCallable(heap, funLoc)) return "@function"
    }
    else {
      if(BoolTrue <= Helper.HasConstruct(heap, funLoc)) return "@construct"
    }
    return null
  }



  ////////////////////////////////////////////////////////////////
  // Bug Detection Main (check CFGInst)
  ////////////////////////////////////////////////////////////////

  def check(node: Node, inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (heap <= HeapBot) unreachableCodeCheck(inst.getInfo)
    else {
      inst match {
        case CFGAlloc(_,info,id,_,_) =>
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGAllocArg(_,info,id,_,_) =>
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGAllocArray(_,info,id,_,_) =>
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGAssert(_, info, expr, isOriginalCondExpr) =>
          conditionalBranchCheck(info, expr, isOriginalCondExpr)
        case CFGCall(_, info, fun, thisArg, args, _) =>
          builtinCheck(info.getSpan, CALL_NORMAL, fun, args)
          callNonFunctionCheck(info.getSpan, fun)
          defaultValueCheck(CALL_NORMAL, fun, thisArg, args)
          if (libMode) unreferencedFunctionCheck(info.getSpan, args)
          unusedFunctionCheck(info.getSpan, fun)
          varyingTypeArgumentsCheck(info.getSpan, CALL_NORMAL, fun, args)
          wrongThisTypeCheck(info.getSpan, fun, thisArg)
        case CFGConstruct(_, info, cons, thisArg, args, _) => 
          builtinCheck(info.getSpan, CALL_CONST, cons, args)
          callNonConstructorCheck(info.getSpan, cons)
          defaultValueCheck(CALL_CONST, cons, thisArg, args)
          if (libMode) unreferencedFunctionCheck(info.getSpan, args)
          unusedFunctionCheck(info.getSpan, cons)
          varyingTypeArgumentsCheck(info.getSpan, CALL_CONST, cons, args)
        case CFGDelete(_,info,id,_) => 
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGDeleteProp(_,info,id,_,_) =>
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGExprStmt(_,info,id,_) =>           
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGInternalCall(_, info, lhs, fun, args, loc) =>
          (fun.toString, args, loc) match {
            case ("<>Global<>toObject", List(expr), Some(aNew)) =>
              if (!lhs.getText.contains("<>fun<>")) accessingNullOrUndefCheck(info.getSpan, expr)
              primitiveToObjectCheck(info.getSpan, expr)
            case ("<>Global<>toNumber", List(expr), None) =>
              bugDetector.ExprDetect.convertToNumberCheck(inst, expr, null, false, null)
            case _ => Unit
          }; unusedVarPropCheck2(inst, lhs, write, variable)
        case CFGStore(_, info, obj, index, _) =>
          unusedVarPropCheck1(inst, obj, index)
        case _ => Unit
      }
    }



    ////////////////////////////////////////////////////////////////
    //  AccessingNullOrUndef Check
    ////////////////////////////////////////////////////////////////

    def accessingNullOrUndefCheck(span: Span, expr: CFGExpr): Unit = {
      val bugCheckInstance = new BugCheckInstance
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(ObjectNull))
      val soundnessLevel = bugOption.soundnessLevel(ObjectNullOrUndef)

      mergedCState.foreach((cstate) => {
        val (callContext, state) = cstate
        val heap = state._1
        val context = state._2
        val ov = SE.V(expr, heap, context)._1
        val pv = ov._1
        val isNotObject = ov._2.isEmpty

        val isBug1 = !bugOption.NullOrUndefined_OnlyWhenPrimitive || isNotObject 
        val isBug2 = bugOption.NullOrUndefined_OnlyNullOrUndefined && isOnlyNullUndef(pv)
        val isBug3 = !bugOption.NullOrUndefined_OnlyNullOrUndefined && !isNotNullUndef(pv)
        val checkInstance = bugCheckInstance.insert(isBug1 && (isBug2 || isBug3), span, callContext, state)
        checkInstance.pValue = pv
      })

      if (!bugOption.NullOrUndefined_BugMustExistInEveryState) bugCheckInstance.filter((bug, notBug) => (bug.pValue == notBug.pValue))
      bugCheckInstance.bugList.foreach((bugEntry) => bugCheck(bugEntry.pValue, bugEntry.callContext, bugEntry.state))

      def isOnlyNullUndef(pv: PValue): Boolean = if (pv._3 <= BoolBot && pv._4 <= NumBot && pv._5 <= StrBot) true else false
      def isNotNullUndef(pv: PValue): Boolean = if (pv._1 <= UndefBot && pv._2 <= NullBot) true else false

      def bugCheck(pv: PValue, callContext: CallContext, state: State): Unit = {
        val objName = varManager.getUserVarAssign(expr) match {
          case name: BugVar0 => "'" + name.toString + "'"
          case _ => "an object"
        }
        (pv._1, pv._2) match {
          case (UndefTop, NullTop)  => bugStorage.addMessage(span, ObjectNullOrUndef, inst, if (soundnessLevel == SOUNDNESS_LEVEL_HIGH) callContext else null, objName)
          case (_, NullTop)         => bugStorage.addMessage(span, ObjectNull, inst, if (soundnessLevel == SOUNDNESS_LEVEL_HIGH) callContext else null, objName)
          case (UndefTop, _)        => bugStorage.addMessage(span, ObjectUndef, inst, if (soundnessLevel == SOUNDNESS_LEVEL_HIGH) callContext else null, objName)
          case _ => Unit
        }
      }
    }



    ////////////////////////////////////////////////////////////////
    //  BuiltinArgSize & BuiltinWrongType Check
    ////////////////////////////////////////////////////////////////

    def builtinCheck(span: Span, isCall: Boolean, fun: CFGExpr, args: CFGExpr): Unit = {
      // Check for each CState
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, CallContext._MOST_SENSITIVE)
      for((callContext, state) <- mergedCState) {
        val funLocSet = SE.V(fun, state.heap, state.context)._1.locset
        val argLocSet = SE.V(args, state.heap, state.context)._1.locset

        // Check for each function location set
        funLocSet.foreach(funLoc => {
          // Function must have [[Function]] or [[Construct]] property
          var propertyName: String = getFuncOrConstPropName(state.heap, funLoc, isCall)
          if(propertyName != null) {
            // Check for each function id set
            state.heap(funLoc)(propertyName)._1.funid.foreach(fid => {
              ModelManager.getFIdMap("Builtin").get(fid) match {
                case Some(funName) =>
                  // BuiltinArgSize
                  // Check for each argument location set
                  argLocSet.foreach(argLoc => {
                    state.heap(argLoc)("length")._1.objval.value.pvalue.numval match {
                      case UIntSingle(n) =>
                        val argSize = argSizeMap(funName)
                        val comp: String = (if(n < argSize._1) "few" else if(n > argSize._2) "many" else null)
                        val checkInstance = bugCheckInstance.insert(comp != null, span, callContext, state)
                        checkInstance.bugKind = BuiltinArgSize
                        checkInstance.fid = fid
                        checkInstance.string1 = comp
                      case _ =>
                    }
                  })

                  // BuiltinWrongType
                  argTypeMap.get(funName) match {
                    case Some((argIndex, jsType)) =>
                      // Check bug options
                      var checkType = true
                      if(jsType == EJSType.OBJECT && !bugOption.BuiltinWrongType_CheckObjectType) checkType = false
                      if(jsType == EJSType.OBJECT_FUNCTION && !bugOption.BuiltinWrongType_CheckFunctionType) checkType = false

                      if(checkType) {
                        // Check for each argument location set
                        argLocSet.foreach(argLoc => {
                          val arg = state.heap(argLoc)
                          val obj = arg(argIndex.toString)._1.objval.value
                          val isBug = jsType match {
                            case EJSType.OBJECT =>
                              bugOption.BuiltinWrongType_TypeMustBeCorrectDefinitely match {
                                case true => obj.locset.isEmpty || obj.pvalue </ PValueBot
                                case false => obj.locset.isEmpty
                              }
                            case EJSType.OBJECT_FUNCTION =>
                              bugOption.BuiltinWrongType_TypeMustBeCorrectDefinitely match {
                                case true => obj.locset.isEmpty || obj.locset.exists(loc => BoolTrue != state.heap(loc).domIn("@function")) || obj.pvalue </ PValueBot
                                case false => obj.locset.isEmpty || obj.locset.exists(loc => BoolFalse <= state.heap(loc).domIn("@function"))
                            }
                          }
                          val checkInstance = bugCheckInstance.insert(isBug, span, callContext, state)
                          checkInstance.bugKind = BuiltinWrongType
                          checkInstance.fid = fid
                          checkInstance.string1 = if(argIndex == 0) "First" else "Second"
                          checkInstance.string2 = jsType match {
                            case EJSType.OBJECT => "an object type"
                            case EJSType.OBJECT_FUNCTION => "a function type"
                          }
                        })
                      }
                    case None =>
                  }
                case None => 
              }
            })
          }
        })
      }

      // Filter out bugs depending on options
      if(!bugOption.BuiltinWrongType_TypeMustBeCorrectInEveryState) {
        bugCheckInstance.filter((bug, notBug) => (bug.bugKind == BuiltinWrongType && bug.bugKind == notBug.bugKind && bug.fid == notBug.fid && bug.string1 == notBug.string1 && bug.string2 == notBug.string2))
      }

      // Report bugs
      for(b <- bugCheckInstance.bugList) {
        b.bugKind match {
          case BuiltinArgSize =>
            bugStorage.addMessage(span, b.bugKind, inst, b.callContext, b.string1, getFuncName(cfg.getFuncName(b.fid)))
          case BuiltinWrongType =>
            bugStorage.addMessage(span, b.bugKind, inst, b.callContext, b.string1, getFuncName(cfg.getFuncName(b.fid)), b.string2)
        }
      }
    }



    ////////////////////////////////////////////////////////////////
    //  BuiltinArgumentSize Check
    ////////////////////////////////////////////////////////////////

    def builtinArgumentSizeCheck(span: Span, isCall: Boolean, fun: CFGExpr, args: CFGExpr): Unit = {
      // Check for each CState
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(BuiltinArgSize))
      for((callContext, state) <- mergedCState) {
        val funLocSet = SE.V(fun, state.heap, state.context)._1.locset
        val argLocSet = SE.V(args, state.heap, state.context)._1.locset

        // Check for each function location set
        funLocSet.foreach(funLoc => {
          // Function must have [[Function]] or [[Construct]] property
          var propertyName: String = getFuncOrConstPropName(state.heap, funLoc, isCall)
          if(propertyName != null) {
            // Check for each function id set
            state.heap(funLoc)(propertyName)._1.funid.foreach(fid => {
              ModelManager.getFIdMap("Builtin").get(fid) match {
                case Some(funName) =>
                  // Check for each argument location set
                  argLocSet.foreach(argLoc => {
                    state.heap(argLoc)("length")._1.objval.value.pvalue.numval match {
                      case UIntSingle(n) =>
                        val argSize = argSizeMap(funName)
                        val comp: String = (if(n < argSize._1) "few" else if(n > argSize._2) "many" else null)
                        val checkInstance = bugCheckInstance.insert(comp != null, span, callContext, state)
                        checkInstance.bugKind = BuiltinArgSize
                        checkInstance.fid = fid
                        checkInstance.string1 = comp
                      case _ =>
                    }
                  })
                case None =>
              }
            })
          }
        })
      }

      // Report bugs
      for(b <- bugCheckInstance.bugList) {
        bugStorage.addMessage(span, BuiltinArgSize, inst, b.callContext, b.string1, getFuncName(cfg.getFuncName(b.fid)))
      }
    }



    ////////////////////////////////////////////////////////////////
    //  CallNonFunction Check
    ////////////////////////////////////////////////////////////////

    def callNonFunctionCheck(span: Span, fun: CFGExpr): Unit = {
      // Get the function name
      var funId: String = varManager.getUserVarAssign(fun) match {
        case bv: BugVar0 => "the non-function '" + bv.toString + "'"
        case _ => "a non-function"
      }

      // Check for each CState
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(CallNonFunction))
      for ((callContext, state) <- mergedCState) {
        val funLocSet = SE.V(fun, state.heap, state.context)._1.locset

        // Check for each CState
        funLocSet.foreach(funLoc => {
          val isCallable = Helper.IsCallable(state.heap, funLoc)

          // Collect function's callablility
          val isBug = bugOption.CallNonFunction_FunctionMustBeCallableDefinitely match {
            case true => isCallable != BoolTrue
            case false => isCallable <= BoolFalse
          }
          val checkInstance = bugCheckInstance.insert(isBug, span, callContext, state)
          checkInstance.loc = funLoc
        })
      }

      // Filter out bugs depending on options
      if (!bugOption.CallNonFunction_FunctionMustBeCallableInEveryState) {
        bugCheckInstance.filter((bug, notBug) => (bug.loc == notBug.loc))
      }
      if (!bugOption.CallNonFunction_FunctionMustBeCallableForEveryLocation) {
        bugCheckInstance.filter((bug, notBug) => (bug.callContext == notBug.callContext && bug.state == notBug.state))
      }

      // Report bugs
      bugCheckInstance.bugList.foreach((e) => bugStorage.addMessage(span, CallNonFunction, inst, e.callContext, funId))
    }



    ////////////////////////////////////////////////////////////////
    //  CallNonConstructor Check
    ////////////////////////////////////////////////////////////////

    def callNonConstructorCheck(span: Span, cons: CFGExpr): Unit = {
      val originalLocSet = SE.V(cons, heap, context)._1._2
      val filteredLocSet = originalLocSet.filter((loc) => BoolTrue <= Helper.HasConstruct(heap, loc))
      originalLocSet.foreach((loc) => heap(loc)("@function")._1._3.foreach((fid) =>
          typing.builtinFset.get(fid) match {
            case Some(builtinName) =>
              if ((nonConsSet contains builtinName) || (filteredLocSet.size < originalLocSet.size)) 
                bugStorage.addMessage(span, CallNonConstructor, inst, null, filterUnnamedFunction(cfg.getFuncName(fid)))
            case None => Unit
      }))
    }



    ////////////////////////////////////////////////////////////////
    //  ConditionalBranch Check
    ////////////////////////////////////////////////////////////////

    def conditionalBranchCheck(info: Info, expr: CFGExpr, isOriginalCondExpr: Boolean): Unit = {
      //if (!isOriginalCondExpr) return
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(CondBranch))

      // Check for each CState
      for ((callContext, state) <- mergedCState) {
        // expr value
        val value: Value = SE.V(expr, state.heap, state.context)._1
        val pvalue: PValue = value.pvalue

        // undefined
        if (pvalue.undefval == UndefTop) {
          val checkInstance = bugCheckInstance.insert(true, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.UNDEFINED
          checkInstance.string1 = "undefined"
          checkInstance.string2 = "false"
        }
        // null
        if (pvalue.nullval == NullTop) {
          val checkInstance = bugCheckInstance.insert(true, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.NULL
          checkInstance.string1 = "null"
          checkInstance.string2 = "false"
        }
        // Boolean
        if (pvalue.boolval == BoolTrue || pvalue.boolval == BoolFalse) {
          val checkInstance = bugCheckInstance.insert(true, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.BOOLEAN
          checkInstance.string1 = pvalue.boolval.toString
          checkInstance.string2 = if (pvalue.boolval == BoolTrue) "true" else "false"
        }
        else if (pvalue.boolval == BoolTop) {
          val checkInstance = bugCheckInstance.insert(false, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.BOOLEAN
        }
        // Number
        if (pvalue.numval == Infinity || pvalue.numval == PosInf || pvalue.numval == NegInf || pvalue.numval == NaN || pvalue.numval == NUInt ||
            pvalue.numval.isInstanceOf[UIntSingle] || pvalue.numval.isInstanceOf[NUIntSingle]) {
          val checkInstance = bugCheckInstance.insert(true, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.NUMBER
          checkInstance.string1 = pvalue.numval.toString
          checkInstance.string2 = pvalue.numval match {
            case NaN => "false"
            case UIntSingle(n) if (n == 0) => "false"
            case NUIntSingle(n) if (n == 0) => "false"
            case _ => "true"
          }
        }
        else if (pvalue.numval == NumTop || pvalue.numval == UInt) {
          val checkInstance = bugCheckInstance.insert(false, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.NUMBER
        }
        // String
        if (pvalue.strval == NumStr || pvalue.strval.isInstanceOf[NumStrSingle] || pvalue.strval.isInstanceOf[OtherStrSingle]) {
          val checkInstance = bugCheckInstance.insert(true, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.STRING
          checkInstance.string1 = pvalue.strval.toString
          checkInstance.string2 = pvalue.strval match {
            case NumStrSingle(s) if (s == "") => "false"
            case OtherStrSingle(s) if (s == "") => "false"
            case _ => "true"
          }
        }
        else if (pvalue.strval == StrTop || pvalue.strval == OtherStr) {
          val checkInstance = bugCheckInstance.insert(false, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.STRING
        }
        // Object
        if (!value.locset.isEmpty) {
          val checkInstance = bugCheckInstance.insert(true, info.getSpan, callContext, state)
          checkInstance.valueType = EJSType.OBJECT
          checkInstance.string1 = "Object"
          checkInstance.string2 = "true"
        }
      }

      // Filter out bugs depending on options
      if (!bugOption.CondBranch_ConditionMustBeTrueOrFalseInEveryState) {
        bugCheckInstance.filter((bug, notBug) => bug.valueType == notBug.valueType)
      }
      if (!bugOption.CondBranch_ConditionMustBeTrueOrFalseDefinitely) {
        bugCheckInstance.filter((bug, notBug) => (bug.callContext == notBug.callContext && bug.state == notBug.state))
      }

      // Expression must be only one boolean value(true or false)
      var result: AbsBool = BoolBot
      for(checkInstance <- bugCheckInstance.bugList) {
        result+= (checkInstance.string2 match {
          case "true" => BoolTrue
          case "false" => BoolFalse
        })
      }
      bugStorage.insertConditionMap(node, inst.asInstanceOf[CFGAssert], result)
      /*if(result == BoolBot || result == BoolTop) return

      // Report bugs
      bugCheckInstance.bugList.foreach((e) => bugStorage.addMessage(e.span, CondBranch, inst, e.callContext, e.string2, 
        (if (e.string2 != "false" && e.string2 != "true") ", where its value is " + e.string1 else "") + "."))*/
    }



    ////////////////////////////////////////////////////////////////
    //  DefaultValue (called by main function)
    ////////////////////////////////////////////////////////////////

    def defaultValueCheck(isCall: Boolean, fun: CFGExpr, thisArg: CFGExpr, args: CFGExpr): Unit = {
      val objLocSet = SE.V(fun, heap, context)._1._2
      objLocSet.foreach((loc) =>
        heap(loc)("@function")._1._3.foreach((fid) =>
          typing.builtinFset.get(fid) match {
            case Some(builtinName) =>
              if (isCall) {
                // ToString
                if (builtinName == "String") bugDetector.ExprDetect.defaultValueCheck(inst, args, "String")
                else if (ToStringSet contains builtinName) bugDetector.ExprDetect.defaultValueCheck(inst, thisArg, "String")
                // ToString:  JSON.stringify check when replacer, space or value is Object and its [[Class]] is String 
                //            or when replacer is Object and its [[Class]] is Number (what's replacer ??)
                else if (builtinName == "JSON.strinify" && !SE.V(args, heap, context)._1._2.subsetOf(LocSetBot)) 
                  SE.V(args, heap, context)._1._2.foreach((loc) => 
                    if (AbsString.alpha("String") == heap(loc)("@class")._1._2._1._5) bugDetector.ExprDetect.defaultValueCheck(inst, args, "String")
                    else if (AbsString.alpha("Number") == heap(loc)("@class")._1._2._1._5) bugDetector.ExprDetect.defaultValueCheck(inst, args, "Number")) 
                // ToNumber
                else if (builtinName == "Number") bugDetector.ExprDetect.defaultValueCheck(inst, args, "Number")
                else if (ToNumberSet contains builtinName) bugDetector.ExprDetect.defaultValueCheck(inst, args, "Number")
              } else {
                if (builtinName == "String")      bugDetector.ExprDetect.defaultValueCheck(inst, args, "String")
                else if (builtinName == "Number") bugDetector.ExprDetect.defaultValueCheck(inst, args, "Number")
                else if (builtinName == "Date")   bugDetector.ExprDetect.defaultValueCheck(inst, args, "Number")
              }
            case None => Unit
      }))
    }



    ////////////////////////////////////////////////////////////////
    //  PrimitiveToObject Check
    ////////////////////////////////////////////////////////////////

    def primitiveToObjectCheck(span: Span, expr: CFGExpr): Unit = {
      // Check for each CState
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(PrimitiveToObject))
      for ((callContext, state) <- mergedCState) {
        // expr value
        val value: Value = SE.V(expr, state.heap, state.context)._1
        val pvalue: PValue = value.pvalue

        // undefined (type error)
        //if (pvalue.undefval != UndefBot) bugCheckInstance.insertWithStrings(true, span, callContext, state, "undefined")
        // null (type error)
        //if (pvalue.nullval != NullBot) bugCheckInstance.insertWithStrings(true, span, callContext, state, "null")
        // boolean
        if (pvalue.boolval != BoolBot) bugCheckInstance.insertWithStrings(true, span, callContext, state, "boolean")
        // number
        if (pvalue.numval != NumBot) bugCheckInstance.insertWithStrings(true, span, callContext, state, "number")
        // string
        if (pvalue.strval != StrBot) {
          if (bugOption.PrimitiveToObject_CheckEvenThoughPrimitiveIsString) bugCheckInstance.insertWithStrings(true, span, callContext, state, "string")
          else bugCheckInstance.insert(false, span, callContext, state)
        }
        // Object
        if (!value.locset.isEmpty) bugCheckInstance.insert(false, span, callContext, state)
      }

      // Filter out bugs depending on options
      if (!bugOption.PrimitiveToObject_PrimitiveMustBeConvertedInEveryState) {
        bugCheckInstance.filter((bug, notBug) => true)
      }
      if (!bugOption.PrimitiveToObject_PrimitiveMustBeConvertedDefinitely) {
        bugCheckInstance.filter((bug, notBug) => (bug.callContext == notBug.callContext && bug.state == notBug.state))
      }

      // Group by CState to collect types
      bugCheckInstance.group(checkInstance => (checkInstance.callContext, checkInstance.state).hashCode)

      for ((_, checkInstanceList) <- bugCheckInstance.groupedBugList) {
        // Collect types
        var types = ""
        for (checkInstance <- checkInstanceList) {
          if (types.length() == 0) types = checkInstance.string1
          else types+= ", " + checkInstance.string1
        }
        // Report bugs
        if (checkInstanceList.length > 0) bugStorage.addMessage(span, PrimitiveToObject, inst, checkInstanceList.head.callContext, types)
      }
    }



    ////////////////////////////////////////////////////////////////
    //  UnreachableCode Check
    ////////////////////////////////////////////////////////////////

    def unreachableCodeCheck(rawInfo: Option[Info]): Unit = rawInfo match {
      case Some(info) => if (info.isFromSource) bugStorage.appendUnreachableInstruction(info.getSpan)
      case None => System.out.println("Warning, InstDetect@bugDetector. No Info in unreachableCode.")
    }


    ////////////////////////////////////////////////////////////////
    //  UnreferencedFunction Check 
    ////////////////////////////////////////////////////////////////

    def unreferencedFunctionCheck(span: Span, args: CFGExpr): Unit = {
      val argLocSet = SE.V(args, heap, context)._1._2
      val argObj = argLocSet.foldLeft(ObjBot)((obj, loc) => obj + heap(loc))
      val argLen = argObj("length")._1._1._1._1._4
      argLen match {
        case UIntSingle(n) => (0 to (n.toInt - 1)).foreach((i) => argObj(i.toString)._1._1._1._2.foreach((l) => 
          heap(l)("@function")._1._3.foreach((fid) => bugStorage.appendUsedFunction(fid))))
        case _ => Unit 
      }
    }



    ////////////////////////////////////////////////////////////////
    //  UnusedFunction Check 
    ////////////////////////////////////////////////////////////////

    def unusedFunctionCheck(span: Span, fun: CFGExpr): Unit = {
      val fval = SE.V(fun, heap, context)._1
      fval._2.foreach((loc) => heap(loc)("@function")._1._3.foreach((fid) => bugStorage.appendUsedFunction(fid)))
    }



    ////////////////////////////////////////////////////////////////
    //  UnusedVarProp Check (# of args: 2)
    ////////////////////////////////////////////////////////////////

    def unusedVarPropCheck1(inst: CFGInst, obj: CFGExpr, index: CFGExpr): Unit = {
      val locSet = SE.V(obj, heap, context)._1._2
      val s = SE.V(index, heap, context)._1._1._5
      locSet.foreach((loc: Loc) => AbsString.concretize(s) match {
        case Some(name) => bugStorage.updateRWMap(cfg.findEnclosingNode(inst), write, property, name, loc, inst.getInfo.get.getSpan)
        case None => Unit // ignore StrTop, NumStr, OtherStr
      })
    }



    ////////////////////////////////////////////////////////////////
    //  UnusedVarProp Check (# of arg: 1 with flags)
    ////////////////////////////////////////////////////////////////

    def unusedVarPropCheck2(inst: CFGInst, id: CFGId, rwflag: Boolean, pvflag: Boolean): Unit = {
      id match {
        case CFGUserId(_, name, _, originalName, _) =>
          val locSet = Helper.LookupBase(heap, id)
          locSet.foreach((loc: Loc) => bugStorage.updateRWMap(cfg.findEnclosingNode(inst), rwflag, pvflag, name, loc, inst.getInfo.get.getSpan))
          if (bugStorage.isInternalName(name)) bugStorage.updateNameMap(name, originalName)
        case CFGTempId(_, _) => Unit
      }
    }



    ////////////////////////////////////////////////////////////////
    //  VaryingTypeArguments Check
    ////////////////////////////////////////////////////////////////

    def varyingTypeArgumentsCheck(span: Span, isCall: Boolean, fun: CFGExpr, args: CFGExpr): Unit = {
      // Check for each CState
      val mergedCState = stateManager.getCState(node, inst.getInstId, CallContext._MOST_SENSITIVE)
      for((callContext, state) <- mergedCState) {
        val funLocSet = SE.V(fun, state.heap, state.context)._1.locset
        val argLocSet = SE.V(args, state.heap, state.context)._1.locset

        // Check for each function location set
        for(funLoc <- funLocSet) {
          // Function must have [[Function]] or [[Construct]] property
          var propertyName: String = getFuncOrConstPropName(state.heap, funLoc, isCall)
          if(propertyName != null) {
            val fidSet = state.heap(funLoc)(propertyName)._1.funid
            for(fid <- fidSet) {
              for(argLoc <- argLocSet) {
                val argObj = state.heap(argLoc)
                bugStorage.updateDetectedFuncMap(fid, argObj, span)
              }
            }
          }
        }
      }

      /* Previous code
      val argLocSet = SE.V(args, heap, context)._1._2
      val argObj = argLocSet.foldLeft(ObjBot)((obj, loc) => obj + heap(loc))
      val locSet = SE.V(fun, heap, context)._1._2
      val filteredLocSet = if (isCall) locSet.filter((loc) => BoolTrue <= Helper.IsCallable(heap, loc)) else
          locSet.filter((loc) => BoolTrue <= Helper.HasConstruct(heap, loc))
      filteredLocSet.foreach((loc) =>  
        (if (isCall) heap(loc)("@function")._1._3 else heap(loc)("@construct")._1._3).foreach((fid) => 
          bugStorage.updateDetectedFuncMap(fid, argObj, span)))
      */
    }



    ////////////////////////////////////////////////////////////////
    //  WrongThisType Check
    ////////////////////////////////////////////////////////////////

    def wrongThisTypeCheck(span: Span, fun: CFGExpr, thisArg: CFGExpr): Unit = {
      val objLocSet = SE.V(fun, heap, context)._1._2
      objLocSet.foreach((loc) => heap(loc)("@function")._1._3.foreach((fid) =>
        typing.builtinFset.get(fid) match {
          case Some(builtinName) =>
            if (thisTypeMap contains builtinName) {
              val thisLocs = Helper.getThis(heap, SE.V(thisArg, heap, context)._1)
              thisLocs.foreach((loc) => if (heap(loc)("@class")._1._2._1._5 != thisTypeMap(builtinName)) 
                bugStorage.addMessage(span, WrongThisType, inst, null, builtinName))
            }
          case None => Unit
      }))
    }
  }
}
