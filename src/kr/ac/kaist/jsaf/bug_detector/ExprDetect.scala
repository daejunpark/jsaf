/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.nodes.IROp
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.nodes_util.EJSOp
import kr.ac.kaist.jsaf.analysis.typing.CState
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class ExprDetect(bugDetector: BugDetector) {
  val cfg           = bugDetector.cfg
  val typing        = bugDetector.typing
  val bugStorage    = bugDetector.bugStorage
  val bugOption     = bugDetector.bugOption
  val varManager    = bugDetector.varManager
  val stateManager  = bugDetector.stateManager



  ////////////////////////////////////////////////////////////////
  // ConvertToNumber Check
  ////////////////////////////////////////////////////////////////

  def convertToNumberCheck(node: Node, inst: CFGInst, expr1: CFGExpr, expr2: CFGExpr, doToPrimitive: Boolean, conditionFunction: (PValue, PValue) => Boolean): Unit = {
    // Get spans
    val expr1Span = expr1.getInfo match {
      case Some(info) => info.getSpan
      case None => inst.getInfo.get.getSpan
    }
    val expr2Span = if(expr2 == null) null else expr2.getInfo match {
      case Some(info) => info.getSpan
      case None => inst.getInfo.get.getSpan
    }

    // Get variable names
    val varId1: String = varManager.getUserVarAssign(expr1) match {
      case bv: BugVar0 if(bv.toString != "undefined") => " '" + bv.toString + "' can be undefined."
      case _ => ""
    }
    val varId2: String = {
      if(expr2 != null) {
        varManager.getUserVarAssign(expr2) match {
          case bv: BugVar0 if(bv.toString != "undefined") => " '" + bv.toString + "' can be undefined."
          case _ => ""
        }
      }
      else null
    }

    // Check for each CState
    val bugCheckInstance = new BugCheckInstance()
    val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(CallNonFunction))
    for((callContext, state) <- mergedCState) {
      // For expr1
      val value1: Value = SE.V(expr1, state.heap, state.context)._1
      val pvalue1: PValue = if(doToPrimitive) Helper.toPrimitive(value1) else value1.pvalue

      // For expr2 (this can be null)
      val value2: Value = if(expr2 != null) SE.V(expr2, state.heap, state.context)._1 else null
      val pvalue2: PValue = if(expr2 != null) {if(doToPrimitive) Helper.toPrimitive(value2) else value2.pvalue} else null

      if(conditionFunction == null || conditionFunction(pvalue1, pvalue2)) {
        if(!bugOption.ConvertUndefToNum_VariableMustHaveUndefinedOnly || pvalue1.typeCount == 1 && value1.locset.isEmpty) {
          // Check possibility of being undefined
          val checkInstance = bugCheckInstance.insert(pvalue1.undefval == UndefTop, expr1Span, callContext, state)
          checkInstance.pValue = pvalue1
          checkInstance.string1 = varId1
        }
        if(expr2 != null) {
          if(!bugOption.ConvertUndefToNum_VariableMustHaveUndefinedOnly || pvalue2.typeCount == 1 && value2.locset.isEmpty) {
            // Check possibility of being undefined
            val checkInstance = bugCheckInstance.insert(pvalue2.undefval == UndefTop, expr2Span, callContext, state)
            checkInstance.pValue = pvalue2
            checkInstance.string1 = varId2
          }
        }
      }
    }

    // Filter out bugs depending on options
    if(!bugOption.ConvertUndefToNum_UndefMustBeConvertedInEveryState) {
      bugCheckInstance.filter((bug, notBug) => (bug.pValue == notBug.pValue))
    }

    // Report bugs
    for(checkInstance <- bugCheckInstance.bugList) bugStorage.addMessage(checkInstance.span, ConvertUndefToNum, inst, checkInstance.callContext, checkInstance.string1)
  }



  ////////////////////////////////////////////////////////////////
  //  DefaultValue + ImplicitTypeConversion Check
  ////////////////////////////////////////////////////////////////

  def defaultValueCheck(inst: CFGInst, expr: CFGExpr, hint: String): Unit = {
    val node = cfg.findEnclosingNode(inst)
    val bugCheckInstance = new BugCheckInstance()
    val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(DefaultValueTypeError))

    for ((callContext, state) <- mergedCState) {
      val heap      = state._1
      val context   = state._2
      val exprVal   = SE.V(expr, heap, context)._1
      val exprPVal  = exprVal._1
      val exprLoc   = exprVal._2
      val exprInfo  = expr.getInfo
      val name      = varManager.getUserVarAssign(expr) match {
        case name: BugVar0 => "'" + name.toString + "'"
        case _ => "an object"
      }
      var errorFound = false
      var isBuiltin  = false
      val checkList  = hint match {
        case "String" => ("toString", "valueOf")
        case "Number" => ("valueOf", "toString")
      }

      // To check ImplicitTypeConversion in built-in functions
      exprLoc.foreach((loc) => if (heap(loc)("@function")._1._3.isEmpty) isBuiltin = false 
        else heap(loc)("@function")._1._3.foreach((fid) => if (typing.builtinFset contains fid) isBuiltin = true))

      // Bug Detect
      if (!exprLoc.isEmpty) {
        exprLoc.foreach((loc) => {
          val check1 = heap(loc)(checkList._1)._1._1._1
          if (isBuiltin && !check1._2.isEmpty) errorFound = implicitTypeConversionCheck(check1, checkList._1)
          if (!errorFound && !check1._2.isEmpty) errorFound = defaultValueTypeErrorCheck(check1)
          if (errorFound) infoCheck(DefaultValueTypeError)
          else {
            val check2 = heap(loc)(checkList._2)._1._1._1
            if (isBuiltin && !check2._2.isEmpty) errorFound = implicitTypeConversionCheck(check2, checkList._2)
            if (!errorFound && !check2._2.isEmpty) errorFound = defaultValueTypeErrorCheck(check2)
            if (errorFound) infoCheck(DefaultValueTypeError)
          }
          errorFound = false
        })
      }

      ////////////////////////////////////////////////////////////////
      // DefaultValueTypeError Check
      ////////////////////////////////////////////////////////////////

      def defaultValueTypeErrorCheck(value: Value): Boolean = {
        value._2.foldLeft[Boolean](false)((retBool, loc) =>
          Helper.IsCallable(heap, loc) match {
            case BoolTrue   => retBool || true
            case BoolFalse  => retBool || false
            case _          => retBool || false // Maybe
        })
      }

      ////////////////////////////////////////////////////////////////
      // ImplicitTypeConversion Check 
      ////////////////////////////////////////////////////////////////

      def implicitTypeConversionCheck(value: Value, hint: String): Boolean = {
        value._2.foldLeft[Boolean](false)((retBool, iloc) => retBool || 
          heap(iloc)("@function")._1._3.foldLeft[Boolean](false)((tempBool, fid) => typing.builtinFset.get(fid) match {
            case Some(builtinName) => if (!(internalMethodMap(hint) contains builtinName)) tempBool || infoCheck(ImplicitCalltoString) else tempBool
            case None => tempBool || infoCheck(ImplicitCallvalueOf)
          })
        )
      }

      ////////////////////////////////////////////////////////////////
      // Report bug if info exists 
      ////////////////////////////////////////////////////////////////

      def infoCheck(flag: BugKind): Boolean = {
        exprInfo match {
          case Some(info) => bugStorage.addMessage(info.getSpan, flag, null, null, name); return true
          case None => System.out.println("bugDetector, Bug '%d'. Expression has no info.".format(flag)); return false
        }
      }
    }
  }



  ////////////////////////////////////////////////////////////////
  // Convert property name from AbsString
  ////////////////////////////////////////////////////////////////

  def getPropName(name: AbsString): String = 
    AbsString.concretize(name) match {
      case Some(propName) => propName
      case _ => "unknown_property"
    }



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
  // Bug Detection Main (check CFGExpr)
  ////////////////////////////////////////////////////////////////

  def check(node: Node, inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (heap <= HeapBot) Unit
    else {
      expr match {
        case CFGBin(info, first, op, second) => 
          val opStr = op.getText
          opStr match {
            case "*" | "/" | "%" =>     
              defaultValueCheck2(inst, opStr, first, second)
            case "+" | "<" | ">" | "<=" | ">=" => 
              defaultValueCheck2(inst, opStr, first, second)
              convertToNumberCheck2(opStr, first, second)
            case "|" | "&" | "^" | "<<" | ">>" | ">>>" | "-" | "/" | "%" | "*" => 
              convertToNumberCheck2(opStr, first, second)
            case "==" => 
              defaultValueCheck2(inst, opStr, first, second)
              convertToNumberCheck2(opStr, first, second)
              implicitTypeConversionEqualityComparison(info.getSpan, opStr, first, second)
            case "!=" =>
              convertToNumberCheck2(opStr, first, second)
              implicitTypeConversionEqualityComparison(info.getSpan, opStr, first, second)
            case "in" => 
              defaultValueCheck(inst, first, "String")
              binaryOpSecondTypeCheck(info.getSpan, op, second)
            case "instanceof" => 
              binaryOpSecondTypeCheck(info.getSpan, op, second)
            case _ => Unit
          }
        case CFGLoad(info, obj, index) => 
          absentReadPropertyCheck(info.getSpan, obj, index)
          unusedVarPropCheck(inst, expr, read, property)
        case CFGThis(info) => 
          globalThisCheck(info.getSpan, cfg.findEnclosingNode(inst)._1) 
        case CFGUn(info, op, expr) =>
          val opStr = op.getText
          opStr match {
            // 11.4.6 Unary + Operator
            // 11.4.7 Unary - Operator
            case "+" | "-" => 
              defaultValueCheck(inst, expr, "Number")
              convertToNumberCheck(node, inst, expr, null, true, null)
              //convertToNumberCheck1(expr)
            // 11.4.8 Bitwise NOT Operator ( ~ )
            case "~" => 
              convertToNumberCheck(node, inst, expr, null, true, null)
              //convertToNumberCheck1(expr)
            case  _  => Unit
          }
        case CFGVarRef(info, id) => 
          absentReadVariableCheck(info.getSpan, id)
          unusedVarPropCheck(inst, expr, read, variable)
        case _ => Unit
      }
    }



    ////////////////////////////////////////////////////////////////
    // AbsentRead Check (Property check)
    ////////////////////////////////////////////////////////////////

    def absentReadPropertyCheck(span: Span, obj: CFGExpr, index: CFGExpr): Unit = {
      // Get the object name and property name
      var objId: String = varManager.getUserVarAssign(obj) match {
        case bv: BugVar0 => "'" + bv.toString + "'"
        case _ => "an object"
      }
      val propId: String = varManager.getUserVarAssign(index) match {
        case bv: BugVar0 => bv.toString
        case _ => null
      }

      // Check for each CState
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(AbsentReadProperty))
      for ((callContext, state) <- mergedCState) {
        val objLocSet = SE.V(obj, state.heap, state.context)._1.locset
        val propValue = SE.V(index, state.heap, state.context)._1.pvalue

        // Check for each object location
        objLocSet.foreach(objLoc => {
          // Check for each primitive value
          propValue.foreach(absValue => {
            if (absValue.isConcrete || (!absValue.isBottom && bugOption.AbsentReadProperty_CheckAbstractIndexValue)) {
              val propStr = absValue.toAbsString
              val propExist = Helper.HasProperty(state.heap, objLoc, propStr)

              // Collect property's existence
              val isBug = bugOption.AbsentReadProperty_PropertyMustExistDefinitely match {
                case true => propExist != BoolTrue
                case false => propExist <= BoolFalse
              }

              val checkInstance = bugCheckInstance.insert(isBug, span, callContext, state)
              checkInstance.loc = objLoc
              checkInstance.absValue = absValue
            }
          })
        })
      }
  
      // Filter out bugs depending on options
      if (!bugOption.AbsentReadProperty_PropertyMustExistInEveryState) {
        bugCheckInstance.filter((bug, notBug) => (bug.loc == notBug.loc && bug.absValue == notBug.absValue))
      }
      if (!bugOption.AbsentReadProperty_PropertyMustExistInEveryLocation) {
        bugCheckInstance.filter((bug, notBug) => (bug.callContext == notBug.callContext && bug.state == notBug.state && bug.absValue == notBug.absValue))
      }

      // Report bugs
      // If the index(propId) is CFGUserId (show possible values of the index variable)
      if (propId != null) {
        // Group by CState to collect values of the index variable
        bugCheckInstance.group(checkInstance => (checkInstance.callContext, checkInstance.state).hashCode)

        for ((_, checkInstanceList) <- bugCheckInstance.groupedBugList) {
          // Collect values of the index variable
          var concreteValues: List[AbsBase] = List()
          checkInstanceList.foreach((ci) => if (!concreteValues.contains(ci.absValue)) concreteValues = concreteValues :+ ci.absValue)
          val msg = if (concreteValues.isEmpty) "." else  ", where property '" + propId + "' can be " + concreteValues.tail.foldLeft(concreteValues.head.toString)((str, s) => str + ", " + s.toString) + "."
          bugStorage.addMessage(checkInstanceList.head.span, AbsentReadProperty, inst, checkInstanceList.head.callContext, propId, objId, msg)
        }
      }
      else bugCheckInstance.bugList.foreach((e) => bugStorage.addMessage(e.span, AbsentReadProperty, inst, e.callContext, getPropName(e.absValue.toAbsString), objId, "."))
    }



    ////////////////////////////////////////////////////////////////
    // AbsentRead Check (Variable check)
    ////////////////////////////////////////////////////////////////

    def absentReadVariableCheck(span: Span, id: CFGId): Unit = {
      // Check for user variable only
      if (!id.isInstanceOf[CFGUserId]) return
      val idAbsString = AbsString.alpha(id.getText)

      // Check for each CState
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(AbsentReadVariable))
      for ((callContext, state) <- mergedCState) {
        val doesExist: AbsBool = id.getVarKind match {
          case PureLocalVar => BoolTrue
          case CapturedVar => BoolTrue
          case CapturedCatchVar => BoolTrue
          case GlobalVar => Helper.HasProperty(state.heap, GlobalLoc, idAbsString)
        }

        // Collect variable's existence
        val isBug = bugOption.AbsentReadVariable_VariableMustExistDefinitely match {
          case true => doesExist != BoolTrue
          case false => doesExist <= BoolFalse
        }
        bugCheckInstance.insert(isBug, span, callContext, state)
      }

      // Filter out bugs depending on options
      if (!bugOption.AbsentReadVariable_VariableMustExistInEveryState) bugCheckInstance.filter((bug, notBug) => true)

      // Report bugs
      bugCheckInstance.bugList.foreach((e) => bugStorage.addMessage(e.span, AbsentReadVariable, inst, e.callContext, id.getText))
    }



    ////////////////////////////////////////////////////////////////
    // BinaryOpSecondType Check (in & instanceof)
    ////////////////////////////////////////////////////////////////

    def binaryOpSecondTypeCheck(span: Span, op: IROp, second: CFGExpr): Unit = {
      // Check for each CState
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(BinaryOpSecondType))
      for((callContext, state) <- mergedCState) {
        val value = SE.V(second, state.heap, state.context)._1
        val pvalue = value.pvalue

        // Check object type (in & instanceof)
        val isBug = bugOption.BinaryOpSecondType_OperandMustBeCorrectDefinitely match {
          case true => 
            value.locset.isEmpty || !pvalue.undefval.isBottom ||
            !pvalue.nullval.isBottom || !pvalue.boolval.isBottom ||
            !pvalue.numval.isBottom || !pvalue.strval.isBottom
          case false => value.locset.isEmpty
        }
        val checkInstance = bugCheckInstance.insertWithStrings(isBug, span, callContext, state, "non-object")
        checkInstance.loc = Integer.MIN_VALUE
        checkInstance.pValue = pvalue

        // Check function type (instanceof)
        if (op.getKind == EJSOp.BIN_COMP_REL_INSTANCEOF) {
          value.locset.foreach(loc => {
            val isBug = bugOption.BinaryOpSecondType_OperandMustBeCorrectDefinitely match {
              case true =>
                Helper.IsCallable(state.heap, loc) != BoolTrue ||
                !pvalue.undefval.isBottom || !pvalue.nullval.isBottom || !pvalue.boolval.isBottom ||
                !pvalue.numval.isBottom || !pvalue.strval.isBottom
              case false =>
                Helper.IsCallable(state.heap, loc) == BoolFalse
            }
            val checkInstance = bugCheckInstance.insertWithStrings(isBug, span, callContext, state, "non-function object")
            checkInstance.loc = loc
            checkInstance.pValue = pvalue
          })
        }
      }

      // Filter out bugs depending on options
      if (!bugOption.BinaryOpSecondType_OperandMustBeCorrectInEveryState) {
        bugCheckInstance.filter((bug, notBug) => bug.loc == notBug.loc && bug.string1 == notBug.string1)
      }
      if (!bugOption.BinaryOpSecondType_OperandMustBeCorrectInEveryLocation) {
        bugCheckInstance.filter((bug, notBug) => bug.callContext == notBug.callContext && bug.state == notBug.state && bug.string1 == notBug.string1)
      }

      // Report bugs
      for(checkInstance <- bugCheckInstance.bugList) {
        var string = "."
        if (second.isInstanceOf[CFGVarRef]) {
          val concreteValue = pvalueToString(checkInstance.pValue)
          if (concreteValue.length > 0) string = ", where operand '" + second.toString + "' can be " + concreteValue + "."
        }
        bugStorage.addMessage(checkInstance.span, BinaryOpSecondType, inst, checkInstance.callContext, second.toString, op.getText, checkInstance.string1, string)
      }
    }



    ////////////////////////////////////////////////////////////////
    // ConvertToNumber Check (# of args: 2) 
    ////////////////////////////////////////////////////////////////

    def convertToNumberCheck2(op: String, expr1: CFGExpr, expr2: CFGExpr): Unit = {
      op match {
        // 11.5 Multiplicative Operators
        // 11.6.2 The Subtraction Operator ( - )
        // 11.7 Bitwise Shift Operators
        case "*" | "/" | "%" | "-" | "<<" | ">>" | ">>>" | "&" | "^" | "|" =>
          convertToNumberCheck(node, inst, expr1, expr2, false, null)

        // 11.6.1 The Addition operator ( + )
        case "+" =>
          convertToNumberCheck(node, inst, expr1, expr2, true, (pvalue1: PValue, pvalue2: PValue) => {
            // "7. If Type(lprim) is String or Type(rprim) is String," does not call ToNumber function.
            if (bugOption.ConvertUndefToNum_ToNumberMustBeCalledDefinitely) {
              pvalue1.strval == StrBot && pvalue2.strval == StrBot
            }
            else {
              !(pvalue1.typeCount == 1 && pvalue1.strval != StrBot ||
              pvalue2.typeCount == 1 && pvalue2.strval != StrBot)
            }
          }: Boolean)

        // 11.8 Relational Operators
        case "<" | ">" | "<=" | ">=" =>
          convertToNumberCheck(node, inst, expr1, expr2, true, (pvalue1: PValue, pvalue2: PValue) => {
            var conditionResult = false
            // 11.8.5 The Abstract Relational Comparison Algorithm
            // "4. Else, both px and py are Strings" does not call ToNumber function.
            if (bugOption.ConvertUndefToNum_ToNumberMustBeCalledDefinitely) {
              pvalue1.strval == StrBot && pvalue2.strval == StrBot
            }
            else {
              !(pvalue1.typeCount == 1 && pvalue1.strval != StrBot ||
              pvalue2.typeCount == 1 && pvalue2.strval != StrBot)
            }
          }: Boolean)

        // 11.9 Equality Operators
        case "==" | "!=" =>
          convertToNumberCheck(node, inst, expr1, expr2, false, (pvalue1: PValue, pvalue2: PValue) => {
            // 11.9.3 The Abstract Equality Comparison Algorithm
            if (bugOption.ConvertUndefToNum_ToNumberMustBeCalledDefinitely) {
              val pvalue1TypeCount = pvalue1.typeCount
              val pvalue2TypeCount = pvalue2.typeCount

              // "4. If Type(x) is Number and Type(y) is String,"
              pvalue1TypeCount == 1 && pvalue2TypeCount == 1 && pvalue1.numval != NumBot && pvalue2.strval != StrBot ||
              // "5. If Type(x) is String and Type(y) is Number,"
              pvalue1TypeCount == 1 && pvalue2TypeCount == 1 && pvalue1.strval != StrBot && pvalue2.numval != StrBot ||
              // "6. If Type(x) is Boolean,"
              pvalue1TypeCount == 1 && pvalue1.boolval != BoolBot ||
              // "7. If Type(y) is Boolean,"
              pvalue2TypeCount == 1 && pvalue2.boolval != BoolBot
              // "8. If Type(x) is either String or Number and Type(y) is Object,"
              // "9. If Type(x) is Object and Type(y) is either String or Number,"
            }
            else {
              // "4. If Type(x) is Number and Type(y) is String,"
              pvalue1.numval != NumBot && pvalue2.strval != StrBot ||
              // "5. If Type(x) is String and Type(y) is Number,"
              pvalue1.strval != StrBot && pvalue2.numval != StrBot
              // "6. If Type(x) is Boolean,"
              pvalue1.boolval != BoolBot ||
              // "7. If Type(y) is Boolean,"
              pvalue2.boolval != BoolBot
              // TODO: (doToPrimitive parameter must be true)
              // "8. If Type(x) is either String or Number and Type(y) is Object,"
              // "9. If Type(x) is Object and Type(y) is either String or Number,"
            }
          }: Boolean)
      }
    }



    ////////////////////////////////////////////////////////////////
    // DefaultValue (called by main function)
    ////////////////////////////////////////////////////////////////

    def defaultValueCheck2(inst: CFGInst, op: String, expr1: CFGExpr, expr2: CFGExpr): Unit = {
      op match {
        case "==" =>
          val v1 = SE.V(expr1, heap, context)._1
          val v2 = SE.V(expr2, heap, context)._1
          if (!definite_only && (v1._1._1 </ UndefBot || v1._1._2 </ NullBot || v1._1._3 </ BoolBot)) Unit // Maybe
          else if ((NumBot <= v1._1._4 || StrBot <= v1._1._5) && (!v2._2.subsetOf(LocSetBot))) defaultValueCheck(inst, expr2, "Number"); 
          else if ((!v1._2.subsetOf(LocSetBot)) && (NumBot <= v2._1._4 || StrBot <= v2._1._5)) defaultValueCheck(inst, expr1, "Number"); 
        case _ => 
          defaultValueCheck(inst, expr1, "Number")
          defaultValueCheck(inst, expr2, "Number")
      }
    }



    ////////////////////////////////////////////////////////////////
    // GlobalThis Check
    ////////////////////////////////////////////////////////////////

    def globalThisCheck(span: Span, fid: Int): Unit = {
      val lset_this = heap(SinglePureLocalLoc)("@this")._1._2._2
      val notGlobal = (fid != cfg.getGlobalFId)     // true: current function is not the global object.
      val mayGlobal = lset_this.contains(GlobalLoc) // true: "MAYBE" this refers the global object.
      val defGlobal = lset_this.size == 1           // true: "DEFINITELY" this refers the global object.
      /* bug check */
      if (!definite_only && !defGlobal) Unit        // maybe
      else if (notGlobal && mayGlobal) bugStorage.addMessage(span, (if (defGlobal) GlobalThisDefinite else GlobalThisMaybe), inst, null)
    }



    ////////////////////////////////////////////////////////////////
    // Implicit Type Conversion Check for "11.9.3 The Abstract Equality Comparison Algorithm"
    ////////////////////////////////////////////////////////////////

    def implicitTypeConversionEqualityComparison(span: Span, op: String, expr1: CFGExpr, expr2: CFGExpr): Unit = {
      if(inst.isInstanceOf[CFGAssert] && !inst.asInstanceOf[CFGAssert].flag) return

      // Check for each CState
      val bugCheckInstance = new BugCheckInstance()
      val mergedCState = stateManager.getCState(node, inst.getInstId, bugOption.contextSensitive(CallNonFunction))
      for ((callContext, state) <- mergedCState) {
        // expr1, expr2
        val value1: Value = SE.V(expr1, state.heap, state.context)._1
        val value2: Value = SE.V(expr2, state.heap, state.context)._1
        val pvalue1: PValue = value1.pvalue
        val pvalue2: PValue = value2.pvalue

        def nonBugCase(): Boolean = {
          // undefined == undefined ?
          pvalue1.undefval != UndefBot && pvalue2.undefval != UndefBot ||
          // null == null ?
          pvalue1.nullval != NullBot && pvalue2.nullval != NullBot ||
          // number == undefined ?
          pvalue1.numval != NumBot && pvalue2.undefval != UndefBot ||
          pvalue1.undefval != UndefBot && pvalue2.numval != NumBot ||
          // number == null ?
          pvalue1.numval != NumBot && pvalue2.nullval != NullBot ||
          pvalue1.nullval != NullBot && pvalue2.numval != NumBot ||
          // number == number ?
          pvalue1.numval != NumBot && pvalue2.numval != NumBot ||
          // string == undefined ?
          pvalue1.strval != StrBot && pvalue2.undefval != UndefBot ||
          pvalue1.undefval != UndefBot && pvalue2.strval != StrBot ||
          // string == null ?
          pvalue1.strval != StrBot && pvalue2.nullval != NullBot ||
          pvalue1.nullval != NullBot && pvalue2.strval != StrBot ||
          // string == string ?
          pvalue1.strval != StrBot && pvalue2.strval != StrBot ||
          // boolean == boolean ?
          pvalue1.boolval != BoolBot && pvalue2.boolval != BoolBot ||
          // object == undefined ?
          !value1.locset.isEmpty && pvalue2.undefval != UndefBot ||
          pvalue1.undefval != UndefBot && !value2.locset.isEmpty ||
          // object == null ?
          !value1.locset.isEmpty && pvalue2.nullval != NullBot ||
          pvalue1.nullval != NullBot && !value2.locset.isEmpty ||
          // object == object ?
          !value1.locset.isEmpty && !value2.locset.isEmpty
        }

        // Insert a bug check instance
        def insertBugCheckInstance(isBug: Boolean, value1Type: String, value2Type: String, absValue1: String, absValue2: String): Unit = {
          val checkInstance = bugCheckInstance.insert(isBug, span, callContext, state)
          checkInstance.value1 = value1
          checkInstance.value2 = value2
          checkInstance.string1 = value1Type
          checkInstance.string3 = value2Type
          if (absValue1.length > 0) checkInstance.string2 = "(" + absValue1 + ")" else checkInstance.string2 = ""
          if (absValue2.length > 0) checkInstance.string4 = "(" + absValue2 + ")" else checkInstance.string4 = ""
        }

        var isBug = false

        // null == undefined ?
        if (bugOption.ImplicitTypeConvert_CheckNullAndUndefined) {
          if (pvalue1.nullval != NullBot && pvalue2.undefval != UndefBot ||
            pvalue1.undefval != UndefBot && pvalue2.nullval != NullBot) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (pvalue1.nullval != NullBot) insertBugCheckInstance(isBug, "null", "undefined", "", "")
              else insertBugCheckInstance(isBug, "undefined", "null", "", "")
            }
          }
        }

        // string == number ?
        if (bugOption.ImplicitTypeConvert_CheckStringAndNumber) {
          if (pvalue1.strval != StrBot && pvalue2.numval != NumBot ||
            pvalue1.numval != NumBot && pvalue2.strval != StrBot) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (pvalue1.strval != StrBot) insertBugCheckInstance(isBug, "string", "number", pvalue1.strval.getConcreteValueAsString(), pvalue2.numval.getConcreteValueAsString())
              else insertBugCheckInstance(isBug, "number", "string", pvalue1.numval.getConcreteValueAsString(), pvalue2.strval.getConcreteValueAsString())
            }
          }
        }

        // boolean == undefined ?
        if (bugOption.ImplicitTypeConvert_CheckBooleanAndUndefined) {
          if (pvalue1.boolval != BoolBot && pvalue2.undefval != UndefBot ||
            pvalue1.numval != NumBot && pvalue2.boolval != BoolBot) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (pvalue1.boolval != BoolBot) insertBugCheckInstance(isBug, "boolean", "undefined", pvalue1.boolval.getConcreteValueAsString(), "")
              else insertBugCheckInstance(isBug, "undefined", "boolean", "", pvalue2.boolval.getConcreteValueAsString())
            }
          }
        }

        // boolean == null ?
        if (bugOption.ImplicitTypeConvert_CheckBooleanAndNull) {
          if (pvalue1.boolval != BoolBot && pvalue2.nullval != NullBot ||
            pvalue1.nullval != NullBot && pvalue2.boolval != BoolBot) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (pvalue1.boolval != BoolBot) insertBugCheckInstance(isBug, "boolean", "null", pvalue1.boolval.getConcreteValueAsString(), "")
              else insertBugCheckInstance(isBug, "null", "boolean", "", pvalue2.boolval.getConcreteValueAsString())
            }
          }
        }

        // boolean == number ?
        if (bugOption.ImplicitTypeConvert_CheckBooleanAndNumber) {
          if (pvalue1.boolval != BoolBot && pvalue2.numval != NumBot ||
            pvalue1.numval != NumBot && pvalue2.boolval != BoolBot) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (pvalue1.boolval != BoolBot) insertBugCheckInstance(isBug, "boolean", "number", pvalue1.boolval.getConcreteValueAsString(), pvalue2.numval.getConcreteValueAsString())
              else insertBugCheckInstance(isBug, "number", "boolean", pvalue1.numval.getConcreteValueAsString(), pvalue2.boolval.getConcreteValueAsString())
            }
          }
        }

        // boolean == string ?
        if (bugOption.ImplicitTypeConvert_CheckBooleanAndString) {
          if (pvalue1.boolval != BoolBot && pvalue2.strval != StrBot ||
            pvalue1.strval != StrBot && pvalue2.boolval != BoolBot) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (pvalue1.boolval != BoolBot) insertBugCheckInstance(isBug, "boolean", "string", pvalue1.boolval.getConcreteValueAsString(), pvalue2.strval.getConcreteValueAsString())
              else insertBugCheckInstance(isBug, "string", "boolean", pvalue1.strval.getConcreteValueAsString(), pvalue2.boolval.getConcreteValueAsString())
            }
          }
        }

        // object == number ?
        if (bugOption.ImplicitTypeConvert_CheckObjectAndNumber) {
          if (!value1.locset.isEmpty && pvalue2.numval != NumBot ||
            pvalue1.numval != NumBot && !value2.locset.isEmpty) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (!value1.locset.isEmpty) insertBugCheckInstance(isBug, "object", "number", "", pvalue2.numval.getConcreteValueAsString())
              else insertBugCheckInstance(isBug, "number", "object", pvalue1.numval.getConcreteValueAsString(), "")
            }
          }
        }

        // object == string ?
        if (bugOption.ImplicitTypeConvert_CheckObjectAndString) {
          if (!value1.locset.isEmpty && pvalue2.strval != StrBot ||
            pvalue1.strval != StrBot && !value2.locset.isEmpty) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (!value1.locset.isEmpty) insertBugCheckInstance(isBug, "object", "string", "", pvalue2.strval.getConcreteValueAsString())
              else insertBugCheckInstance(isBug, "string", "object", pvalue1.strval.getConcreteValueAsString(), "")
            }
          }
        }

        // object == boolean ?
        if (bugOption.ImplicitTypeConvert_CheckObjectAndBoolean) {
          if (!value1.locset.isEmpty && pvalue2.boolval != BoolBot ||
            pvalue1.boolval != BoolBot && !value2.locset.isEmpty) {
            if (!bugOption.ImplicitTypeConvert_MustBeConvertedDefinitely || !nonBugCase) {
              isBug = true
              if (!value1.locset.isEmpty) insertBugCheckInstance(isBug, "object", "boolean", "", pvalue2.boolval.getConcreteValueAsString())
              else insertBugCheckInstance(isBug, "boolean", "object", pvalue1.boolval.getConcreteValueAsString(), "")
            }
          }
        }

        // Insert a bug check instance
        if (!isBug) insertBugCheckInstance(isBug, "", "", "", "")
      }

      // Filter out bugs depending on options
      if (!bugOption.ImplicitTypeConvert_MustBeConvertedInEveryState) bugCheckInstance.filter((bug, notBug) => true)

      // Report bugs
      bugCheckInstance.bugList.foreach((e) => bugStorage.addMessage(e.span, ImplicitTypeConvert, inst, e.callContext, e.string1, e.string2, op, e.string3, e.string4))
    }



    ////////////////////////////////////////////////////////////////
    // UnusedVarProp Check (store read entries to RWMap)
    ////////////////////////////////////////////////////////////////

    def unusedVarPropCheck(inst: CFGInst, expr: CFGExpr, rwflag: Boolean, pvflag: Boolean): Unit = {
      expr match {
        case CFGLoad(info, obj, index) => 
          val s = SE.V(index, heap, context)._1._1._5
          val locSet = SE.V(obj, heap, context)._1._2
          val locSetBase = locSet.foldLeft(LocSetBot)((locset, loc) => locset ++ Helper.ProtoBase(heap, loc, s))
          locSetBase.foreach((loc: Loc) => props(heap, loc, s).foreach((name) => 
            bugStorage.updateRWMap(cfg.findEnclosingNode(inst), rwflag, pvflag, name, loc, info.getSpan)))
        case CFGVarRef(info, id) => 
          id match {
            case CFGUserId(_, name, _, originalName, _) =>
              val locSet = Helper.LookupBase(heap, id)
              locSet.foreach((loc: Loc) => bugStorage.updateRWMap(cfg.findEnclosingNode(inst), rwflag, pvflag, name, loc, info.getSpan))
              if (bugStorage.isInternalName(name)) bugStorage.updateNameMap(name, originalName)
            case CFGTempId(_, _) => Unit
          }
        case _ => Unit
      }
    }
  }
}
