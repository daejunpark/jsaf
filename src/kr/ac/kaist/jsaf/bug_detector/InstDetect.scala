/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class InstDetect(cfg: CFG, typing: TypingInterface, bugStorage: BugStorage) {

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
  // Report bug if info exists (DefaultValue)
  ////////////////////////////////////////////////////////////////

  def infoCheck(rawInfo: Option[Info], flag: BId, arg: String): Boolean = {
    rawInfo match {
      case Some(info) => bugStorage.addMessage1(info.getSpan, flag, arg); return true
      case None => System.out.println("bugDetector, Bug '%d'. Expression has no info.".format(flag)); return false
    }
  }



  ////////////////////////////////////////////////////////////////
  // Check whether given arg has suitable type
  ////////////////////////////////////////////////////////////////

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



  ////////////////////////////////////////////////////////////////
  // Filter real function name ("anonymous_function" if unnamed)
  ////////////////////////////////////////////////////////////////

  def filterUnnamedFunction(name: String): String =
    if (NU.isFunExprName(name)) "anonymous_function" else name



  ////////////////////////////////////////////////////////////////
  // Bug Detection Main (check CFGInst)
  ////////////////////////////////////////////////////////////////

  def check(inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (heap <= HeapBot) unreachableCodeCheck(inst.getInfo.get.getSpan)
    else {
      inst match {
        case CFGAlloc(_,info,id,_,_) =>
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGAllocArg(_,info,id,_,_) =>
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGAllocArray(_,info,id,_,_) =>
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGAssert(_, info, expr, isOriginalCondExpr) =>
          conditionalBranachCheck(info, expr, isOriginalCondExpr)
        case CFGCall(_, info, fun, thisArg, args, _) =>
          builtinArgumentSizeCheck(info.getSpan, normalCall, fun, args)
          builtinWrongTypeCheck(info.getSpan, normalCall, fun, args)
          callNonFunctionCheck(info.getSpan, fun)
          defaultValueCheck(normalCall, fun, thisArg, args)
          unusedFunctionCheck(info.getSpan, fun)
          varyingTypeArgumentsCheck(info.getSpan, normalCall, fun, args)
          wrongThisTypeCheck(info.getSpan, fun, thisArg)
        case CFGConstruct(_, info, cons, thisArg, args, _) => 
          builtinArgumentSizeCheck(info.getSpan, constCall, cons, args)
          builtinWrongTypeCheck(info.getSpan, constCall, cons, args)
          callNonConstructorCheck(info.getSpan, cons)
          defaultValueCheck(constCall, cons, thisArg, args)
          unusedFunctionCheck(info.getSpan, cons)
          varyingTypeArgumentsCheck(info.getSpan, constCall, cons, args)
        case CFGDelete(_,info,id,_) => 
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGDeleteProp(_,info,id,_,_) =>
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGExprStmt(_,info,id,_) =>           
          unusedVarPropCheck2(inst, id, write, variable)
        case CFGInternalCall(_, info, lhs, fun, args, loc) =>
          (fun.toString, args, loc) match {
            case ("<>Global<>toObject", List(expr), Some(aNew)) =>
              accessingNullOrUndefCheck(info.getSpan, expr)
              primitiveToObjectCheck(info.getSpan, expr)
            case ("<>Global<>toNumber", List(expr), None) => 
              convertUndefToNumCheck(info.getSpan, expr)
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
      val pv = SE.V(expr, heap, context)._1._1
      if (definite_only && (pv._3 </ BoolBot || pv._4 </ NumBot || pv._5 </ StrBot)) Unit 
      else (pv._1, pv._2) match {
        case (UndefTop, NullTop)  => bugStorage.addMessage0(span, 22)
        case (_, NullTop)         => bugStorage.addMessage0(span, 21)
        case (UndefTop, _)        => bugStorage.addMessage0(span, 23)
        case _ => Unit
      }
    }



    ////////////////////////////////////////////////////////////////
    //  BuiltinWrongType Check
    ////////////////////////////////////////////////////////////////

    def builtinWrongTypeCheck(span: Span, isCall: Boolean, fun: CFGExpr, arguments: CFGExpr): Unit = {
      val funLocSet = SE.V(fun, heap, context)._1._2
      val callableLocSet = if (isCall) funLocSet.filter((loc) => BoolTrue <= Helper.IsCallable(heap, loc)) else
        funLocSet.filter((loc) => BoolTrue <= Helper.HasConstruct(heap, loc))
      val argLocSet = SE.V(arguments, heap, context)._1._2
      callableLocSet.foreach((loc) => heap(loc)("@function")._1._3.foreach((fid) => typing.builtinFset.get(fid) match {
        case Some(name) => argTypeMap.get(name) match {
          case Some((objType, rightType)) => 
            if (isCorrectType(objType, heap, argLocSet)) bugStorage.addMessage2(span, 9, cfg.getFuncName(fid) toString, rightType)
          case None => Unit
        }
        case None => Unit
      }))
    }



    ////////////////////////////////////////////////////////////////
    //  BuiltinArgumentSize Check
    ////////////////////////////////////////////////////////////////

    def builtinArgumentSizeCheck(span: Span, isCall: Boolean, fun: CFGExpr, args: CFGExpr): Unit = {
      val fval = SE.V(fun, heap, context)._1
      val aval = SE.V(args, heap, context)._1
      val funcLocSet = if (isCall) fval._2.filter((loc) => BoolTrue <= Helper.IsCallable(heap, loc)) else 
        fval._2.filter((loc) => BoolTrue <= Helper.HasConstruct(heap, loc))
      val argObj = aval._2.foldLeft(ObjBot)((obj, loc) => obj + heap(loc))
      val argLen = argObj("length")._1._1._1._1._4
      argLen match {
        case UIntSingle(n) =>
          funcLocSet.foreach((l) => heap(l)("@function")._1._3.foreach((fid) =>
            typing.builtinFset.get(fid) match {
              case Some(name) => 
                val arg_size = argSizeMap(name)
                if (arg_size._1 > n) bugStorage.addMessage1(span, 7, getFuncName(cfg.getFuncName(fid)))
                else if (arg_size._2 < n) bugStorage.addMessage1(span, 8, getFuncName(cfg.getFuncName(fid)))
              case None => Unit
          }))
        case _ => Unit
      }
    }



    ////////////////////////////////////////////////////////////////
    //  CallNonFunction Check
    ////////////////////////////////////////////////////////////////

    def callNonFunctionCheck(span: Span, fun: CFGExpr): Unit = {
      val locSet = SE.V(fun, heap, context)._1._2
      val filteredLocSet = locSet.filter((loc) => BoolFalse <= Helper.IsCallable(heap, loc))
      if (filteredLocSet.size > 0) bugStorage.addMessage0(span, 12)
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
                bugStorage.addMessage1(span, 11, filterUnnamedFunction(cfg.getFuncName(fid)))
            case None => Unit
      }))
    }



    ////////////////////////////////////////////////////////////////
    //  ConditionalBranch Check
    ////////////////////////////////////////////////////////////////

    def conditionalBranachCheck(info: Info, expr: CFGExpr, isOriginalCondExpr: Boolean): Unit = {
      val epv = SE.V(expr, heap, context)._1._1
      if (!definite_only && (epv._1 </ UndefBot || epv._2 </ NullBot || epv._4 </ NumBot || epv._5 </ StrBot)) Unit // maybe
      else if (isOriginalCondExpr && info.isFromSource) { // definite only
        epv._3 match {
          case BoolTrue => bugStorage.addMessage0(info.getSpan, 13)
          case BoolFalse => bugStorage.addMessage0(info.getSpan, 14)
          case _ => Unit
        }
      } 
    }



    ////////////////////////////////////////////////////////////////
    //  ConvertUndefToNum Check
    ////////////////////////////////////////////////////////////////

    def convertUndefToNumCheck(span: Span, expr: CFGExpr): Unit = {
      val v = SE.V(expr, heap, context)._1
      if (definite_only && (v._1._1 </ UndefBot || v._1._2 </ NullBot || !v._2.isEmpty)) Unit
      else if (definite_only && (v.typeCount > 1)) Unit
      else if (v._1._1 </ UndefBot) bugStorage.addMessage0(span, 15)
      else Unit
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
                if (builtinName == "String") defaultValueCheckMain(args, "String")
                else if (ToStringSet contains builtinName) defaultValueCheckMain(thisArg, "String")
                // ToString:  JSON.stringify check when replacer, space or value is Object and its [[Class]] is String 
                //            or when replacer is Object and its [[Class]] is Number (what's replacer ??)
                else if (builtinName == "JSON.strinify" && !SE.V(args, heap, context)._1._2.subsetOf(LocSetBot)) 
                  SE.V(args, heap, context)._1._2.foreach((loc) => 
                    if (AbsString.alpha("String") == heap(loc)("@class")._1._2._1._5) defaultValueCheckMain(args, "String")
                    else if (AbsString.alpha("Number") == heap(loc)("@class")._1._2._1._5) defaultValueCheckMain(args, "Number")) 
                // ToNumber
                else if (builtinName == "Number") defaultValueCheckMain(args, "Number")
                else if (ToNumberSet contains builtinName) defaultValueCheckMain(args, "Number")
              } else {
                if (builtinName == "String")      defaultValueCheckMain(args, "String")
                else if (builtinName == "Number") defaultValueCheckMain(args, "Number")
                else if (builtinName == "Date")   defaultValueCheckMain(args, "Number")
              }
            case None => Unit
      }))
    }



    ////////////////////////////////////////////////////////////////
    //  DefaultValue (called internally)
    ////////////////////////////////////////////////////////////////

    def defaultValueCheckMain(expr: CFGExpr, hint: String): Unit = {
      val exprVal    = SE.V(expr, heap, context)._1 // Value
      val exprInfo   = expr.getInfo
      val name       = expr.toString

      var errorFound = false
      var isBuiltin  = false

      val checkList  = hint match {
        case "String" => ("toString", "valueOf")
        case "Number" => ("valueOf", "toString")
      }

      exprVal._2.foreach((loc) => if (heap(loc)("@function")._1._3.isEmpty) isBuiltin = false 
        else heap(loc)("@function")._1._3.foreach((fid) => if (typing.builtinFset contains fid) isBuiltin = true))

      if (!exprVal._2.subsetOf(LocSetBot)) {
        exprVal._2.foreach((loc) => 
          for (l <- heap(loc)("@proto")._1._1._1._2) {
            val v1 = heap(l)(checkList._1)._1._1._1  // Value
            if ((isBuiltin && !v1._2.subsetOf(LocSetBot)) && (!implicitTypeConversionCheck(v1) && !v1._2.subsetOf(LocSetBot)) && !defaultValueTypeErrorCheck(v1)) {
              val v2 = heap(l)(checkList._2)._1._1._1 // Value
              if ((isBuiltin && !v2._2.subsetOf(LocSetBot)) && (!implicitTypeConversionCheck(v2) && !v2._2.subsetOf(LocSetBot)) && !defaultValueTypeErrorCheck(v2))
                infoCheck(exprInfo, 17, name)
            }; errorFound = false
          }
      )}

      ////////////////////////////////////////////////////////////////
      // ImplicitTypeConversion Check 
      ////////////////////////////////////////////////////////////////

      def implicitTypeConversionCheck(value: Value): Boolean = {
        value._2.foldLeft[Boolean](false)((retBool, iloc) => heap(iloc)("@function")._1._3.foldLeft[Boolean](false)((tempBool, fid) => typing.builtinFset.get(fid) match {
          case Some(builtinName) => if (!(internalMethodMap(checkList._1) contains builtinName)) tempBool || infoCheck(exprInfo, 20, name) else tempBool
          case None => tempBool || infoCheck(exprInfo, 21, name)
        }))
      }

      ////////////////////////////////////////////////////////////////
      // DefaultValueTypeError Check
      ////////////////////////////////////////////////////////////////

      def defaultValueTypeErrorCheck(value: Value): Boolean = {
        value._2.foldLeft[Boolean](false)((retBool, sloc) =>
          Helper.IsCallable(heap, sloc) match {
            case BoolTrue   => retBool || true
            case BoolFalse  => retBool || false
            case _          => retBool || false // Maybe
        })
      }
    }



    ////////////////////////////////////////////////////////////////
    //  PrimitiveToObject Check
    ////////////////////////////////////////////////////////////////

    def primitiveToObjectCheck(span: Span, expr: CFGExpr): Unit = {
      val v = SE.V(expr, heap, context)._1
      if (definite_only && (v._1._1 </ UndefBot || v._1._2 </ NullBot || !v._2.isEmpty)) Unit
      else {
        val s = (if (v._1._3 </ BoolBot) List("boolean") else List()) ++ (if (v._1._4 </ NumBot) List("number") else List())
        if (!s.isEmpty) {
          val arg = if (s.size == 1) s.head else s.tail.foldLeft(s.head)((chunk, elem) => chunk + ", " + elem)
          bugStorage.addMessage1(span, 24, arg)
        } else Unit
      }
    }



    ////////////////////////////////////////////////////////////////
    //  UnreachableCode Check
    ////////////////////////////////////////////////////////////////

    def unreachableCodeCheck(span: Span): Unit = bugStorage.appendUnreachableInstruction(span)



    ////////////////////////////////////////////////////////////////
    //  UnusedFunction Check (# of arg: 1)
    ////////////////////////////////////////////////////////////////

    def unusedFunctionCheck(span: Span, fun: CFGExpr): Unit = {
      fun match {
        case CFGVarRef(_, id) =>
          val (v, es) = Helper.Lookup(heap, id) 
          if (es.isEmpty && v._1 <= PValueBot) {
            val name = id.toString
            id.getVarKind match {
              case GlobalVar        => setObjectReferences(GlobalLoc, name)
              case PureLocalVar     => setObjectReferences(SinglePureLocalLoc, name)
              case CapturedVar      => setObjectReferences(SinglePureLocalLoc, name)
              case CapturedCatchVar => setObjectReferences(CollapsedLoc, name)
            }
          } 
        case _ => Unit
      }

      ////////////////////////////////////////////////////////////////
      // Store fids of a referenced function
      ////////////////////////////////////////////////////////////////

      def setObjectReferences(loc: Loc, name: String): Unit = heap(loc)(name)._1._1._1._2.foreach((l: Loc) => 
        heap(l)("@function")._1._3.foreach((fid) => bugStorage.appendUsedFunction(fid)))
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
      val argLocSet = SE.V(args, heap, context)._1._2
      val argObj = argLocSet.foldLeft(ObjBot)((obj, loc) => obj + heap(loc))
      val locSet = SE.V(fun, heap, context)._1._2
      val filteredLocSet = if (isCall) locSet.filter((loc) => BoolTrue <= Helper.IsCallable(heap, loc)) else
          locSet.filter((loc) => BoolTrue <= Helper.HasConstruct(heap, loc))
      filteredLocSet.foreach((loc) =>  
        (if (isCall) heap(loc)("@function")._1._3 else heap(loc)("@construct")._1._3).foreach((fid) => 
          bugStorage.updateDetectedFuncMap(fid, argObj, span)))
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
                bugStorage.addMessage1(span, 35, builtinName))
            }
          case None => Unit
      }))
    }
  }
}
