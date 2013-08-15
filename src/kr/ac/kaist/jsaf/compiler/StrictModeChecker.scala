/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.compiler

import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.exceptions.SyntaxError
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.ErrorLog
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._
import kr.ac.kaist.jsaf.useful.HasAt

/* When processed using strict mode the three types of ECMAScript code
 * are referred to as strict global code, strict eval code, and strict
 * function code. (10.1.1)
 */
/* Checks whether a JavaScript program satisfies the strict mode restrictions.
 * The following restrictions are not yet checked.
 *
 * [R4] Assignment to an undeclared identifier or otherwise unresolvable
 *      reference does not create a property in the global object. When a simple
 *      assignment occurs within strict mode code, its LeftHandSide must not
 *      evaluate to an unresolvable Reference. If it does a ReferenceError
 *      exception is thrown (8.7.2). The LeftHandSide also may not be a reference
 *      to a data property with the attribute value {[[Writable]]:false}, to
 *      an accessor property with the attribute value {[[Set]]:undefined}, nor
 *      to a non-existent property of an object whose [[Extensible]] internal
 *      property has the value false. In these cases a TypeError exception is
 *      thrown (11.13.1).
 * [R5] The identifier eval or arguments may not appear as the
 *      LeftHandSideExpression of an Assignment operator (11.13) or of a
 *      PostfixExpression (11.3) or as the UnaryExpression operated upon by
 *      a Prefix Increment (11.4.4) or a Prefix Decrement (11.4.5) operator.
 * [R6] Arguments objects for strict mode functions define non-configurable
 *      accessor properties named "caller" and "callee" which throw a TypeError
 *      exception on access (10.6).
 * [R7] Arguments objects for strict mode functions do not dynamically share
 *      their array indexed property values with the corresponding formal
 *      parameter bindings of their functions. (10.6).
 * [R8] For strict mode functions, if an arguments object is created the binding
 *      of the local identifier arguments to the arguments object is immutable
 *      and hence may not be the target of an assignment expression. (10.5).
 * [R11] Strict mode eval code cannot instantiate variables or functions in the
 *       variable environment of the caller to eval. Instead, a new variable
 *       environment is created and that environment is used for declaration
 *       binding instantiation for the eval code (10.4.2).
 * [R12] If this is evaluated within strict mode code, then the this value is
 *       not coerced to an object. A this value of null or undefined is not
 *       converted to the global object and primitive values are not converted
 *       to wrapper objects. The this value passed via a function call (including
 *       calls made using Function.prototype.apply and Function.prototype.call)
 *       do not coerce the passed this value to an object (10.4.3, 11.1.1,
 *       15.3.4.3, 15.3.4.4).
 * [R13] When a delete operator occurs within strict mode code, a SyntaxError is
 *       thrown if its UnaryExpression is a direct reference to a variable,
 *       function argument, or function name(11.4.1).
 * [R14] When a delete operator occurs within strict mode code, a TypeError is
 *       thrown if the property to be deleted has the attribute
 *       { [[Configurable]]:false } (11.4.1).
 * [R20] An implementation may not extend, beyond that defined in this
 *       specification, the meanings within strict mode functions of properties
 *       named caller or arguments of function instances. ECMAScript code may not
 *       create or modify properties with these names on function objects that
 *       correspond to strict mode functions (10.6, 13.2, 15.3.4.5.3).
 * [R21] It is a SyntaxError to use within strict mode code the identifiers eval
 *       or arguments as the Identifier of a FunctionDeclaration or
 *       FunctionExpression or as a formal parameter name (13.1). Attempting to
 *       dynamically define such a strict mode function using the Function
 *       constructor (15.3.2) will throw a SyntaxError exception.
 */
class StrictModeChecker(program: Program) extends Walker {

  val errors: ErrorLog = new ErrorLog
  def signal(msg:String, hasAt:HasAt) = errors.signal(msg, hasAt)
  def signal(hasAt:HasAt, msg:String) = errors.signal(msg, hasAt)
  def signal(error: StaticError) = errors.signal(error)
  def getErrors(): List[StaticError] = errors.errors

  /*
   * Print debug string.
   *   This can be turned on or off by bDebugPrint variable.
   */
  var bDebugPrint: Boolean = false
  def DebugPrint(string: String) = if(bDebugPrint) System.out.println(string)

  /*
   * Find a duplicated identifier from Id list.
   *   Returns a duplicated identifier string, or null string if a duplicated identifier is not found.
   */
  def findDuplicatedId(idList: List[Id]): String = {
    /*
     * Using set container that is converted from list may be more efficient for finding a duplicated id.
     *   But formally, the list size is small. So n^2 iteration...
     */
    for(id1 <- idList)
      for(id2 <- idList)
        if(id1.ne(id2) && id1.getText.compareTo(id2.getText) == 0)
          return id1.getText

    return ""
  }

  /*
   * Find a duplicated DataProperty from Property list.
   *   Returns a duplicated DataProperty string, or null string if a duplicated DataProperty is not found.
   */
  def findDuplicatedDataProperty(prop: List[Member]): String = {
    var dataProp1Str: String = ""
    var dataProp2Str: String = ""
    for(prop1 <- prop if prop1.isInstanceOf[Field]; dataProp1Str = NU.prop2Str(prop1.asInstanceOf[Field].getProp)) {
      for(prop2 <- prop if prop1.ne(prop2) if prop2.isInstanceOf[Field]; dataProp2Str = NU.prop2Str(prop2.asInstanceOf[Field].getProp)) {
        if(dataProp1Str.compareTo(dataProp2Str) == 0) return dataProp1Str
      }
    }
    return ""
  }

  def doit(): JList[StaticError] = {
    walk(program)
    toJavaList(errors.errors)
  }

  override def walk(node:Any): Unit = node match {
    case SArrayExpr(_, elements) =>
      DebugPrint("SArrayExpr")
      walk(elements)
    case SAssignOpApp(_, lhs, op, right) =>
      DebugPrint("SAssignOpApp")
      walk(lhs); walk(op); walk(right);
    case SBlock(_, stmts, _) =>
      DebugPrint("SBlock")
      walk(stmts)
    case SBool(_, isBool) =>
      DebugPrint("SBool")
    case SBracket(_, obj, index) =>
      DebugPrint("SBracket")
      walk(obj); walk(index)
    case SBreak(_, target) =>
      DebugPrint("SBreak")
      walk(target)
    case SCase(_, cond, body) =>
      DebugPrint("SCase")
      walk(cond); walk(body)
    /*
     * [R17] It is a SyntaxError if a TryStatement with a Catch occurs within strict
     *       code and the Identifier of the Catch production is eval or arguments
     *       (12.14.1)
     */
    case n@SCatch(_, id, body) =>
      DebugPrint("SCatch")
      if(NU.isEvalOrArguments(id))
        errors.signal("\"" + id.getText + "\" cannot be used as an identifier of the catch statement within strict mode code.", n)
      walk(id); walk(body)
    case SCond(_, cond, trueBranch, falseBranch) =>
      DebugPrint("SCond")
      walk(cond); walk(trueBranch); walk(falseBranch)
    case SContinue(_, target) =>
      DebugPrint("SContinue")
      walk(target)
    case SDebugger(_) =>
      DebugPrint("SDebugger")
    case SDoWhile(_, body, cond) =>
      DebugPrint("SDoWhile")
      walk(body); walk(cond)
    case SDot(_, obj, member) =>
      DebugPrint("SDot")
      walk(obj); walk(member)
    case SEmptyStmt(_) =>
      DebugPrint("SEmptyStmt")
    case SExprList(_, exprs) =>
      DebugPrint("SExprList")
      walk(exprs)
    case SExprStmt(_, expr, _) =>
      DebugPrint("SExprStmt")
      walk(expr)
    case SField(_, prop, expr) =>
      DebugPrint("SField")
      walk(prop); walk(expr)
    case SDoubleLiteral(_, _, _) =>
      DebugPrint("SDoubleLiteral")
    case SFor(_, init, cond, action, body) =>
      DebugPrint("SFor")
      walk(init); walk(cond); walk(action); walk(body)
    case SForIn(_, lhs, expr, body) =>
      DebugPrint("SForIn")
      walk(lhs); walk(expr); walk(body)
    case SForVar(_, vars, cond, action, body) =>
      DebugPrint("SForVar")
      walk(vars); walk(cond); walk(action); walk(body)
    case SForVarIn(_, varjs, expr, body) =>
      DebugPrint("SForVarIn")
      walk(varjs); walk(expr); walk(body)
    case SFunApp(_, fun, args) =>
      DebugPrint("SFunApp")
      walk(fun); walk(args)
    /*
     * [R18] It is a SyntaxError if the identifier eval or arguments appears within
     *       a FormalParameterList of a strict mode FunctionDeclaration or
     *       FunctionExpression (13.1)
     * [R19] A strict mode function may not have two or more formal parameters that
     *       have the same name. An attempt to create such a function using a
     *       FunctionDeclaration, FunctionExpression, or Function constructor is
     *       a SyntaxError (13.1, 15.3.2).
     */
    case n@SFunDecl(_, SFunctional(_, _, body, name, params)) =>
      DebugPrint("SFunDecl")
      // R18 check
      for(id <- params)
        if(NU.isEvalOrArguments(id))
          errors.signal("\"" + id.getText + "\" cannot be used as an identifier of the formal parameter within strict mode code.", n)
      // R19 check
      var dupId: String = findDuplicatedId(params);
      if(dupId != "")
          errors.signal("A strict mode function may not have two or more formal parameters that have the same name. \"" + dupId + "\"", n)
      walk(name); walk(params); walk(body)
    case n@SFunExpr(_, SFunctional(_, _, body, name, params)) =>
      DebugPrint("SFunExpr")
      // R18 check
      for(id <- params)
        if(NU.isEvalOrArguments(id))
          errors.signal("\"" + id.getText + "\" cannot be used as an identifier of the formal parameter within strict mode code.", n)
      // R19 check
      var dupId: String = findDuplicatedId(params);
      if(dupId != "")
          errors.signal("A strict mode function may not have two or more formal parameters that have the same name. \"" + dupId + "\"", n)
      walk(name); walk(params); walk(body)
    case SGetProp(_, prop, SFunctional(_, _, body, name, _)) =>
      DebugPrint("SGetProp")
      walk(prop); walk(body)
    /*
     * [R1] The identifiers "implements", "interface", "let", "package", "private",
     *      "protected", "public", "static", and "yield" are classified as
     *      FutureReservedWord tokens within strict mode code. (7.6.12).
     */
    case n:Id =>
      DebugPrint("SId \"" + n.getText + "\"")
      if(NU.isFutureReserved(n))
        errors.signal("\"" + n.getText + "\" is classified as FutureReservedWord token within strict mode code.", n)
    case SIf(_, cond, trueBranch, falseBranch) =>
      DebugPrint("SIf")
      walk(cond); walk(trueBranch); walk(falseBranch)
    case SInfixOpApp(_, left, op, right) =>
      DebugPrint("SInfixOpApp")
      walk(left); walk(op); walk(right)
    /*
     * [R2] A conforming implementation, when processing strict mode code, may
     *      not extend the syntax of NumericLiteral (7.8.3) to include
     *      OctalIntegerLiteral as described in B.1.1.
     */
    case n@SIntLiteral(_, intVal, radix) =>
      DebugPrint("SIntLiteral")
      if(radix == 8)
        errors.signal("Strict mode code may not include the syntax of OctalIntegerLiteral \"0" +
                      intVal.toString(radix) + "\".", n)
    case SLabel(_, id) =>
      DebugPrint("SLabel")
      walk(id)
    case SLabelStmt(_, label, stmt) =>
      DebugPrint("SLabelStmt")
      walk(label); walk(stmt)
    case SNew(_, lhs) =>
      DebugPrint("SNew")
      walk(lhs)
    case SNull(_) =>
      DebugPrint("SNull")
    /*
     * [R9] It is a SyntaxError if strict mode code contains an ObjectLiteral with
     *      more than one definition of any data property (11.1.5).
     */
    case n@SObjectExpr(_, members) =>
      DebugPrint("SObjectExpr")
      var dupDataProperty: String = findDuplicatedDataProperty(members)
      if(dupDataProperty != "")
          errors.signal("ObjectLiteral may not have two or more definitions of any data property that have the same name within strict mode code. \"" + dupDataProperty + "\"", n)
      walk(members)
    case SOp(_, text) =>
      DebugPrint("SOp")
    case SParenthesized(_, expr) =>
      DebugPrint("SParenthesized")
      walk(expr)
    case SPrefixOpApp(_, op, right) =>
      DebugPrint("SPrefixOpApp")
      walk(op); walk(right)
    case SProgram(_, STopLevel(_, _, program)) =>
      DebugPrint("SProgram")
      walk(program)
    case SPropId(_, id) =>
      DebugPrint("SPropId")
    case SPropNum(_, num) =>
      DebugPrint("SPropNum")
    case SPropStr(_, str) =>
      DebugPrint("SPropStr")
    case SRegularExpression(_, _, _) =>
      DebugPrint("SRegularExpression")
    case SReturn(_, expr) =>
      DebugPrint("SReturn")
      walk(expr)
    /*
     * [R10] It is a SyntaxError if the Identifier "eval" or the Identifier
     *       "arguments" occurs as the Identifier in a PropertySetParameterList of
     *       a PropertyAssignment that is contained in strict code or if its
     *       FunctionBody is strict code (11.1.5).
     */
    case n@SSetProp(_, prop, SFunctional(_, _, body, name, List(id))) =>
      DebugPrint("SSetProp")
      if(NU.isEvalOrArguments(id))
        errors.signal("\"" + id.getText + "\" cannot be used as an identifier of the property set parameter within strict mode code.", n)
      walk(prop); walk(id); walk(body)
    /*
     * [R3] A conforming implementation, when processing strict mode code
     *      (see 10.1.1), may not extend the syntax of EscapeSequence to include
     *      OctalEscapeSequence as described in B.1.2.
     */
    case n@SStringLiteral(__, _, str) =>
      DebugPrint("SStringLiteral \"" + str + "\"")
      if(NU.hasOctalEscapeSequence(str))
        errors.signal("Strict mode code may not include the syntax of OctalEscapeSequence \"" +
                      str + "\".", n)
    case SSwitch(_, cond, frontCases, defjs, backCases) =>
      DebugPrint("SSwitch")
      walk(cond); walk(frontCases); walk(defjs); walk(backCases)
    case SThis(_) =>
      DebugPrint("SThis")
    case SThrow(_, expr) =>
      DebugPrint("SThrow")
      walk(expr)
    case STry(_, body, catchBlock, fin) =>
      DebugPrint("STry")
      walk(body); walk(catchBlock); walk(fin)
    case SUnaryAssignOpApp(_, lhs, op) =>
      DebugPrint("SUnaryAssignOpApp")
      walk(lhs); walk(op)
    /*
     * [R15] It is a SyntaxError if a VariableDeclaration or VariableDeclarationNoIn
     *       occurs within strict code and its Identifier is eval or arguments
     *       (12.2.1).
     */
    case n@SVarDecl(_, name, expr) =>
      DebugPrint("SVarDecl")
      if(NU.isEvalOrArguments(name))
        errors.signal("\"" + name.getText + "\" cannot be used as an identifier of the variable declaration within strict mode code.", n)
      walk(name); walk(expr)
    case SVarRef(_, id) =>
      DebugPrint("SVarRef")
      walk(id)
    case SVarStmt(_, vds) =>
      DebugPrint("SVarStmt")
      vds.foreach(walk _)
    case SWhile(_, cond, body) =>
      DebugPrint("SWhile")
      walk(cond); walk(body)
    /*
     * [R16] Strict mode code may not include a WithStatement.
     *       The occurrence of a WithStatement in such a context is
     *       a SyntaxError (12.10).
     */
    case n:With =>
      errors.syntaxError("Strict mode code may not include the with statement.", n)
    case xs:List[_] => xs.foreach(walk _)
    case xs:Option[_] => xs.foreach(walk _)
    case _ =>
  }
}
