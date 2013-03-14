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

class ExprDetect(cfg: CFG, typing: TypingInterface, bugStorage: BugStorage) {

  ////////////////////////////////////////////////////////////////
  // Convert property name from AbsString
  ////////////////////////////////////////////////////////////////

  def getPropName(name: AbsString): String = 
    AbsString.concretize(name) match {
      case Some(propName) => propName
      case _ => "unknown_property"
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

  def check(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = {
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
              defaultValueCheck(opStr, first, second)
            case "+" | "<" | ">" | "<=" | ">=" => 
              defaultValueCheck(opStr, first, second)
              convertToNumberCheck2(opStr, first, second)
            case "|" | "&" | "^" | "<<" | ">>" | ">>>" | "-" | "/" | "%" | "*" => 
              convertToNumberCheck2(opStr, first, second)
            case "==" => 
              defaultValueCheck(opStr, first, second)
              convertToNumberCheck2(opStr, first, second)
            case "!=" => 
              convertToNumberCheck2(opStr, first, second)
            case "in" => 
              defaultValueCheckMain(first, "String")
              binaryInOpCheck(info.getSpan, second)
            case "instanceof" => 
              binaryInstanceOfCheck(info.getSpan, first, second)
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
            case "+" | "-" => 
              defaultValueCheckMain(expr, "Number")
              convertToNumberCheck1(expr)
            case "~" => 
              convertToNumberCheck1(expr)
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
      val objLocSet  = SE.V(obj, heap, context)._1._2
      val propStr    = SE.V(index, heap, context)._1._1._5
      val propExist  = objLocSet.foldLeft[AbsBool](BoolBot)((flag, loc) => flag + Helper.HasProperty(heap, loc, propStr))
      if (!definite_only && BoolTop <= propExist) Unit   // maybe
      else if (BoolFalse <= propExist) bugStorage.addMessage1(span, 1, getPropName(propStr))
    }



    ////////////////////////////////////////////////////////////////
    // AbsentRead Check (Variable check)
    ////////////////////////////////////////////////////////////////

    def absentReadVariableCheck(span: Span, id: CFGId): Unit = 
      if (!Helper.Lookup(heap, id)._2.isEmpty) bugStorage.addMessage1(span, 2, id.getText)



    ////////////////////////////////////////////////////////////////
    // BinaryInOp Check
    ////////////////////////////////////////////////////////////////

    def binaryInOpCheck(span: Span, obj: CFGExpr): Unit = 
      if (SE.V(obj, heap, context)._1._2.subsetOf(LocSetBot)) bugStorage.addMessage1(span, 3, obj.toString)



    ////////////////////////////////////////////////////////////////
    // BinaryInstanceOf Check
    ////////////////////////////////////////////////////////////////

    def binaryInstanceOfCheck(span: Span, first: CFGExpr, second: CFGExpr): Unit = {
      val originalLocSet = SE.V(second, heap, context)._1._2
      if (originalLocSet.subsetOf(LocSetBot)) bugStorage.addMessage1(span, 4, second.toString)
      else {
        val callableLocSet = originalLocSet filter((l) => BoolTrue <= Helper.IsCallable(heap, l)) 
        if (callableLocSet.size < originalLocSet.size) bugStorage.addMessage1(span, 5, second.toString)
        else {
          val isObjectProto = callableLocSet.foldLeft[Boolean](false)((isObjProto, l) => 
            if (!isObjProto && !(heap(l)("@proto")._1._1._1._2 contains ObjProtoLoc)) false else true)
          if (!isObjectProto) bugStorage.addMessage1(span, 6, second.toString)
        }
      } 
    }



    ////////////////////////////////////////////////////////////////
    // ConvertToNumber Check (# of args: 1)
    ////////////////////////////////////////////////////////////////

    def convertToNumberCheck1(expr: CFGExpr): Unit = {
      val v = SE.V(expr, heap, context)._1
      if (definite_only && (v.typeCount > 1)) Unit
      else if (v._1._1 </ UndefBot) bugStorage.addMessage0(expr.getInfo.get.getSpan, 15)
      else Unit
    }



    ////////////////////////////////////////////////////////////////
    // ConvertToNumber Check (# of args: 2) 
    ////////////////////////////////////////////////////////////////

    def convertToNumberCheck2(op: String, expr1: CFGExpr, expr2: CFGExpr): Unit = {
      op match {
        case "|" | "&" | "^" | "<<" | ">>" | ">>>" | "-" | "/" | "%" | "*" => 
          convertToNumberCheck1(expr1); convertToNumberCheck1(expr2)
        case "+" | "<" | ">" | "<=" | ">=" =>
          val lprim = Helper.toPrimitive(SE.V(expr1, heap, context)._1)
          val rprim = Helper.toPrimitive(SE.V(expr2, heap, context)._1)
          if (definite_only && (lprim.typeCount > 1 || rprim.typeCount > 1)) Unit
          else if (lprim._5 <= StrBot && rprim._5 <= StrBot) {
            // How to show the warnings? Show expr2 also when expr1 has warning?
            if (lprim._1 </ UndefBot) bugStorage.addMessage0(expr1.getInfo.get.getSpan, 15)
            if (rprim._1 </ UndefBot) bugStorage.addMessage0(expr2.getInfo.get.getSpan, 15)
          }; else Unit
        case "==" | "!=" =>
          val v1 = SE.V(expr1, state._1, state._2)._1
          val v2 = SE.V(expr2, state._1, state._2)._1
          val (pv1, pv2) = (v1._1, v2._1)
          val isExpr1Undef = (pv1._4 </ NumBot && pv1._5 </StrBot) &&  pv2._1 </ UndefBot
          val isExpr2Undef = (pv2._5 </ StrBot && pv2._4 </NumBot) &&  pv1._1 </ UndefBot
          if (definite_only && (v1.typeCount > 1 || v2.typeCount > 1)) Unit // Maybe
          else if (isExpr1Undef) bugStorage.addMessage0(expr1.getInfo.get.getSpan, 15)
          else if (isExpr2Undef) bugStorage.addMessage0(expr2.getInfo.get.getSpan, 15)
      }
    }



    ////////////////////////////////////////////////////////////////
    // DefaultValue (called by main function)
    ////////////////////////////////////////////////////////////////

    def defaultValueCheck(op: String, expr1: CFGExpr, expr2: CFGExpr): Unit = {
      op match {
        case "==" =>
          val v1 = SE.V(expr1, heap, context)._1
          val v2 = SE.V(expr2, heap, context)._1
          if (!definite_only && (v1._1._1 </ UndefBot || v1._1._2 </ NullBot || v1._1._3 </ BoolBot)) Unit // Maybe
          else if ((NumBot <= v1._1._4 || StrBot <= v1._1._5) && (!v2._2.subsetOf(LocSetBot))) defaultValueCheckMain(expr2, "Number"); 
          else if ((!v1._2.subsetOf(LocSetBot)) && (NumBot <= v2._1._4 || StrBot <= v2._1._5)) defaultValueCheckMain(expr1, "Number"); 
        case _ => 
          defaultValueCheckMain(expr1, "Number")
          defaultValueCheckMain(expr2, "Number")
      }
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
            if (isBuiltin && !v1._2.subsetOf(LocSetBot)) errorFound = implicitTypeConversionCheck(v1)
            if (!errorFound && !v1._2.subsetOf(LocSetBot)) errorFound = defaultValueTypeErrorCheck(v1)
            if (!errorFound) {
              val v2 = heap(l)(checkList._2)._1._1._1 // Value
              if (isBuiltin && !v2._2.subsetOf(LocSetBot)) errorFound = implicitTypeConversionCheck(v2) 
              if (!errorFound && !v2._2.subsetOf(LocSetBot)) errorFound = defaultValueTypeErrorCheck(v2)
            } 
            if (!errorFound) infoCheck(exprInfo, 16, name)
            errorFound = false
          }
      )}

      ////////////////////////////////////////////////////////////////
      // ImplicitTypeConversion Check 
      ////////////////////////////////////////////////////////////////

      def implicitTypeConversionCheck(value: Value): Boolean = {
        value._2.foldLeft[Boolean](false)((retBool, iloc) => retBool || 
          heap(iloc)("@function")._1._3.foldLeft[Boolean](false)((tempBool, fid) => typing.builtinFset.get(fid) match {
            case Some(builtinName) => if (!(internalMethodMap(checkList._1) contains builtinName)) tempBool || infoCheck(exprInfo, 20, name) else tempBool
            case None => tempBool || infoCheck(exprInfo, 21, name)
          })
        )
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
    //  GlobalThis Check
    ////////////////////////////////////////////////////////////////

    def globalThisCheck(span: Span, fid: Int): Unit = {
      val lset_this = heap(SinglePureLocalLoc)("@this")._1._2._2
      val notGlobal = (fid != cfg.getGlobalFId)     // true: current function is not the global object.
      val mayGlobal = lset_this.contains(GlobalLoc) // true: "MAYBE" this refers the global object.
      val defGlobal = lset_this.size == 1           // true: "DEFINITELY" this refers the global object.
      /* bug check */
      if (!definite_only && !defGlobal) Unit        // maybe
      else if (notGlobal && mayGlobal) bugStorage.addMessage0(span, if (defGlobal) 17 else 18)
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
