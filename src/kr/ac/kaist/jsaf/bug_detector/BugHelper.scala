/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes.ASTNode
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU, JSAstToConcrete}

object BugHelper {
  ////////////////////////////////////////////////////////////////
  // Get function name
  ////////////////////////////////////////////////////////////////

  def getFuncName(funcName: String, varManager: VarManager = null, expr: CFGNode = null): String = {
    if (!NU.isFunExprName(funcName)) return funcName
    if (varManager != null && expr != null) {
      expr match {
        case expr: CFGExpr =>
          val bugVar0 = varManager.getUserVarAssign(expr)
          if (bugVar0 != null) return bugVar0.toString
        case expr: CFGFunExpr =>
          var isFirst = true
          val funcName = new StringBuilder
          for (rhs <- varManager.getUserVarAssignR(expr.lhs)) {
            if (isFirst) isFirst = false else funcName.append(", ")
            funcName.append(rhs.toString)
          }
          if (funcName.length > 0) return funcName.toString
        case _ =>
      }
    }
    "anonymous_function"
  }



  ////////////////////////////////////////////////////////////////
  // Get [[Function]] or [[Construct]] property
  ////////////////////////////////////////////////////////////////

  def getFuncOrConstPropName(heap: Heap, funLoc: Loc, isCall: Boolean): String = {
    // Function must have [[Function]] or [[Construct]] property
    if (isCall) {
      if (BoolTrue <= Helper.IsCallable(heap, funLoc)) return "@function"
    }
    else {
      if (BoolTrue <= Helper.HasConstruct(heap, funLoc)) return "@construct"
    }
    null
  }



  ////////////////////////////////////////////////////////////////
  // Get omitted code from a AST node
  ////////////////////////////////////////////////////////////////

  def getOmittedCode(ast: ASTNode, maxLength: Int = 48): String = {
    val originalCode = JSAstToConcrete.doit(ast)
    var newCode = ""
    var isFirst = true
    for (line <- originalCode.split('\n')) {
      if (newCode.length < maxLength) {
        if (isFirst) isFirst = false else newCode+= ' '
        newCode+= line.replace('\t', ' ').trim
      }
    }
    if (newCode.length > maxLength) newCode = newCode.substring(0, maxLength) + " ..."
    newCode
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

  def props(heap: Heap, loc: Loc, absString: AbsString): Set[String] = {
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
  // PValue to String
  ////////////////////////////////////////////////////////////////

  def pvalueToString(pvalue: PValue, concreteOnly: Boolean = true): String = {
    var result = ""
    pvalue.foreach(absValue => {
      if (!absValue.isBottom && (!concreteOnly || absValue.isConcrete)) {
        if (result.length == 0) result+= absValue.toString
        else result+= ", " + absValue.toString
      }
    })
    result
  }
}
