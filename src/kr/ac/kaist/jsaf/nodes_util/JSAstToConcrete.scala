/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import _root_.java.util.ArrayList
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._
import edu.rice.cs.plt.iter.IterUtil
import edu.rice.cs.plt.tuple.Option

object JSAstToConcrete extends Walker {

  val width = 50
  var internal = false
  var testWith = false
  val significantBits = NU.significantBits

  def doit(node: ASTNode): String = walk(node)
  def doitInternal(node: ASTNode) = {
    internal = true
    walk(node)
  }
  def doitTestWith(node: ASTNode) = {
    testWith = true
    walk(node)
  }

  /* indentation utilities *************************************************/
  var indent = 0
  val tab: StringBuilder = new StringBuilder("  ")
  def increaseIndent = indent += 1
  def decreaseIndent = indent -= 1
  def getIndent = {
    val s: StringBuilder = new StringBuilder
    for (i <- 0 to indent-1) s.append(tab)
    s.toString
  }
  def isOneline(node: Any):Boolean = node match {
    case SBlock => false
    case Some(in) => isOneline(in)
    case _ => !(node.isInstanceOf[Block])
  }

  /* utility methods ********************************************************/

  /*  make sure it is parenthesized */
  def inParentheses(str: String) =
    if (str.startsWith("(") && str.endsWith(")")) str
    else new StringBuilder("(").append(str).append(")").toString

  def join(all: List[Any], sep: String, result: StringBuilder): StringBuilder = all match {
    case Nil => result
    case _ => result.length match {
      case 0 => {
        join(all.tail, sep, result.append(walk(all.head)))
      }
      case _ =>
        if (result.length > width && sep.equals(", "))
          join(all.tail, sep, result.append(", \n"+getIndent).append(walk(all.head)))
        else
          join(all.tail, sep, result.append(sep).append(walk(all.head)))
    }
  }

  var uniq_id = 0
  def fresh() = { uniq_id += 1; uniq_id.toString }
  type Env = List[(String, String)]
  var env = Nil.asInstanceOf[Env]
  def addE(uniq: String, new_uniq: String) = env = (uniq, new_uniq)::env
  def getE(uniq: String): String = env.find(p => p._1.equals(uniq)) match {
    case None =>
      val new_uniq = fresh
      addE(uniq, new_uniq)
      new_uniq
    case Some((_, new_uniq)) => new_uniq
  }

  def pp(s: StringBuilder, str: String) = {
    for (c <- str) c match {
      case '\u0008' => s.append("\\b")
      case '\t' => s.append("\\t")
      case '\n' => s.append("\\n")
      case '\f' => s.append("\\f")
      case '\r' => s.append("\\r")
      case '\u000b' => s.append("\\v")
      case '"'  => s.append("\"")
      case '\'' => s.append("'")
      case '\\' => s.append("\\")
      case c => s.append(c+"")
    }
  }

  def prFtn(s: StringBuilder, fds: List[FunDecl], vds: List[VarDecl],
            body: List[SourceElement]) = {
    fds match {
      case Nil =>
      case _ =>
        increaseIndent
        s.append(getIndent).append(join(fds, "\n"+getIndent, new StringBuilder("")))
        decreaseIndent
        s.append("\n").append(getIndent)
    }
    vds match {
      case Nil =>
      case _ =>
        increaseIndent
        s.append(getIndent)
        vds.foreach(vd => vd match {
                    case SVarDecl(_,n,_,_) =>
                      s.append("var "+n.getText+";\n"+getIndent)})
        decreaseIndent
        s.append("\n").append(getIndent)
      }
    increaseIndent
    s.append(getIndent).append(join(body, "\n"+getIndent, new StringBuilder("")))
    decreaseIndent
  }

  class StringBuilderHelper(s: StringBuilder) {
    def toStringP() = inParentheses(s.toString)
  }
  implicit def stringBuilderWrapper(s: StringBuilder) = new StringBuilderHelper(s)

  /* The rule of separators(indentation, semicolon and newline) in unparsing pattern matchings.
   * This rule is applied recursively
   * Principle: All case already has indentation at the front and newline at the end.
   *
   * Root case: See [case SProgram].
   *    program's type is List<SourceElement>.
   *    Each <SourceElement> has indentation at the front
   *    and newline at the end to keep the principle.
   *
   * Branch case(Stmt or ListofStmt): SBlock, SFunDecl, SVarStmt, SExprStmt...
   *    Add indentation and newline to keep the principle in inner cases.
   *    When its type is [Stmt], add ";" at the end.
   *
   * Leaf case(not Stmt, may have inner case): SExprList, SArrayExpr, ...
   *    Don't add indentation, newline and ";".
   *    They are already added.
   *    But other separators(like ", " or " ") may be added.
   *
   */
  override def walk(node:Any):String = node match {
    case SArrayExpr(info, elements) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("[")
      elements.foreach(e => s.append(walk(e)).append(", "))
      s.append("]")
      s.toString
    case SArrayNumberExpr(info, elements) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("[")
      s.append("A LOT!!! "+elements.size+" elements are not printed here.")
      s.append("]")
      s.toString
    case SAssignOpApp(info, lhs, op, right) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(lhs)).append(" ")
      s.append(walk(op)).append(" ")
      s.append(walk(right))
      s.toStringP
    case SBlock(info, stmts, _) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("{\n")
      increaseIndent
      s.append(getIndent).append(join(stmts, "\n"+getIndent, new StringBuilder("")))
      decreaseIndent
      s.append("\n").append(getIndent).append("}")
      s.toString
    case SStmtUnit(info, stmts) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("{\n")
      increaseIndent
      s.append(getIndent).append(join(stmts, "\n"+getIndent, new StringBuilder("")))
      decreaseIndent
      s.append("\n").append(getIndent).append("}")
      s.toString
    case SBool(info, isBool) =>
      walk(info)+(if(isBool) "true" else "false")
    case SBracket(info, obj, index) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(obj)).append("[").append(walk(index)).append("]")
      s.toStringP
    case SBreak(info, target) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("break")
      if(target.isSome) s.append(" ").append(walk(target))
      s.append(";")
      s.toString
    case SCase(info, cond, body) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("case ").append(walk(cond))
      s.append(":\n")
      increaseIndent
      s.append(getIndent).append(join(body, "\n"+getIndent, new StringBuilder("")))
      decreaseIndent
      s.append("\n")
      s.toString
    case SCatch(info, id, body) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(body)
      s.append("catch(").append(walk(id)).append(")\n")
      increaseIndent
      s.append("{")
      s.append(getIndent).append(join(body, "\n"+getIndent, new StringBuilder("")))
      s.append("}\n")
      decreaseIndent
      s.toString
    case SCond(info, cond, trueBranch, falseBranch) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(cond)).append(" ? ").append(walk(trueBranch)).append(" : ").append(walk(falseBranch))
      s.toStringP
    case SContinue(info, target) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("continue")
      if(target.isSome) s.append(" ").append(walk(target))
      s.append(";")
      s.toString
    case SDebugger(info) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("debugger;")
      s.toString
    case SDoWhile(info, body, cond) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(body)
      s.append("do\n")
      if(oneline) increaseIndent
      s.append(getIndent).append(walk(body))
      if(oneline) decreaseIndent
      s.append("while(")
      s.append(walk(cond)).append(");")
      s.toString
    case SDot(info, obj, member) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(obj)).append(".").append(walk(member))
      s.toStringP
    case SEmptyStmt(info) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("undefined;")
      s.toString
    case SExprList(info, exprs) =>
      join(exprs, ", ", new StringBuilder(walk(info))).toString
    case SExprStmt(info, expr, _) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(expr)+";")
      s.toString
    case SField(info, prop, expr) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(prop)).append(" : ").append(walk(expr))
      s.toString
    case SDoubleLiteral(info, text, num) =>
      walk(info)+ /*text*/ num.toString().replace('E','e');
    case SFor(info, init, cond, action, body) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(body)
      s.append("for(")
      if(init.isSome) s.append(walk(init))
      s.append(";")
      if(cond.isSome) s.append(walk(cond))
      s.append(";")
      if(action.isSome) s.append(walk(action))
      s.append(")\n")
      if(oneline) increaseIndent
      s.append(getIndent).append(walk(body))
      if(oneline) decreaseIndent
      s.toString
    case SForIn(info, lhs, expr, body) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(body)
      s.append("for(")
      s.append(walk(lhs)).append(" in ").append(walk(expr)).append(")\n")
      if(oneline) increaseIndent
      s.append(getIndent).append(walk(body))
      if(oneline) decreaseIndent
      s.toString
    case SForVar(info, vars, cond, action, body) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(body)
      s.append("for(var ")
      s.append(join(vars, ", ", new StringBuilder("")))
      s.append(";")
      if(cond.isSome) s.append(walk(cond))
      s.append(";")
      if(action.isSome) s.append(walk(action))
      s.append(")\n")
      if(oneline) increaseIndent
      s.append(getIndent).append(walk(body))
      if(oneline) decreaseIndent
      s.toString
    case SForVarIn(info, varjs, expr, body) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(body)
      s.append("for(var ")
      s.append(walk(varjs)).append(" in ").append(walk(expr)).append(")\n")
      if(oneline) increaseIndent
      s.append(getIndent).append(walk(body))
      if(oneline) decreaseIndent
      s.toString
    case SFunApp(info, fun, args) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(fun)).append("(")
      s.append(join(args, ", ", new StringBuilder("")))
      s.append(")")
      s.toStringP
    case SFunDecl(info, SFunctional(fds, vds, body, name, params), _) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("function ").append(walk(name)).append("(")
      s.append(join(params, ", ", new StringBuilder("")))
      s.append(") \n").append(getIndent).append("{\n")
      prFtn(s, fds, vds, toList(body.getBody))
      s.append("\n").append(getIndent).append("}")
      s.toString
    case SFunExpr(info, SFunctional(fds, vds, body, name, params)) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("(function ")
      if(!NU.isFunExprName(name.getText)) s.append(walk(name))
      s.append("(")
      s.append(join(params, ", ", new StringBuilder("")))
      s.append(") \n").append(getIndent).append("{\n")
      prFtn(s, fds, vds, toList(body.getBody))
      s.append("\n").append(getIndent).append("})")
      s.toStringP
    case SGetProp(info, prop, SFunctional(fds, vds, body, _, _)) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("get ").append(walk(prop)).append("()\n").append(getIndent).append("{\n")
      prFtn(s, fds, vds, toList(body.getBody))
      s.append("\n").append(getIndent).append("}")
      s.toString
    case SId(info, text, Some(uniq), _) =>
      walk(info)+(if (internal && NU.isInternal(uniq))
                    uniq.dropRight(significantBits)+getE(uniq.takeRight(significantBits))
                  else text)
    case SId(info, text, None, _) =>
      walk(info)+text
    case SIf(info, cond, trueBranch, falseBranch) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(trueBranch)
      s.append("if(").append(walk(cond)).append(")\n")
      if(oneline) increaseIndent
      s.append(getIndent).append(walk(trueBranch))
      if(oneline) decreaseIndent
      if(falseBranch.isSome){
        oneline = isOneline(falseBranch)
        s.append("\n").append(getIndent).append("else\n")
        if(oneline) increaseIndent
        s.append(getIndent).append(walk(falseBranch))
        if(oneline) decreaseIndent
      }
      s.toString
    case SInfixOpApp(info, left, op, right) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(left)).append(" ")
      s.append(walk(op)).append(" ")
      s.append(walk(right))
      s.toStringP
    case SIntLiteral(info, intVal, radix) =>
      val str = radix match {
        /*
        case 8 => "0" + intVal.toString(8)
        case 16 => "0x" + intVal.toString(16)
        */
        case _ => intVal.toString
      }
      walk(info)+str
    case SLabel(info, id) =>
      walk(info)+walk(id)
    case SLabelStmt(info, label, stmt) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(label)).append(" : ").append(walk(stmt))
      s.toString
    case SNew(info, lhs) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("new ").append(walk(lhs))
      s.toStringP
    case SNull(info) =>
      walk(info)+"null"
    case SObjectExpr(info, members) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("{\n")
      increaseIndent
      s.append(getIndent).append(join(members, ",\n"+getIndent, new StringBuilder("")))
      decreaseIndent
      s.append("\n").append(getIndent).append("}")
      s.toString
    case SOp(info, text) =>
      walk(info)+text
    case SParenthesized(info, expr) =>
      walk(info)+inParentheses(walk(expr))
    case SPrefixOpApp(info, op, right) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(op)).append(" ").append(walk(right))
      s.toStringP
    case SProgram(info, STopLevel(fds, vds, program)) =>
      val s: StringBuilder = new StringBuilder
      prFtn(s, fds, vds, NU.toStmts(program))
      s.append(walk(info))
      s.toString
    case SPropId(info, id) =>
      walk(info)+walk(id)
    case SPropNum(info, num) => walk(info)+walk(num)
    case SPropStr(info, str) =>
      walk(info)+(if (str.equals("\"")) "'\"'" else "\""+str+"\"")
    case SRegularExpression(info, body, flags) =>
      walk(info)+(if (testWith) "/"+body+"/"+flags
                  else "/"+NU.unescapeJava(body)+"/"+NU.unescapeJava(flags))
    case SReturn(info, expr) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("return")
      if (expr.isSome) s.append(" ").append(walk(expr))
      s.append(";")
      s.toString
    case SSetProp(info, prop, SFunctional(fds, vds, body, _, List(id))) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("set ").append(walk(prop)).append("(")
      s.append(walk(id)).append(") \n").append(getIndent).append("{\n")
      prFtn(s, fds, vds, toList(body.getBody))
      s.append("\n").append(getIndent).append("}")
      s.toString
    case SStringLiteral(info, quote, txt) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(quote)
                           /*
      if (NU.getKeepComments) pp(s, txt)
      else pp(s, NU.unescapeJava(txt))
                           */
      pp(s, NU.unescapeJava(txt))
      s.append(quote)
      s.toString
    case SSwitch(info, cond, frontCases, defjs, backCases) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("switch(").append(walk(cond)).append("){\n")
      increaseIndent
      s.append(getIndent).append(join(frontCases, "\n"+getIndent, new StringBuilder("")))
      if(defjs.isSome){
        s.append("\n").append(getIndent).append("default:")
        increaseIndent
        s.append("\n").append(getIndent).append(join(defjs.unwrap, "\n"+getIndent, new StringBuilder("")))
        decreaseIndent
      }
      s.append("\n").append(getIndent).append(join(backCases, "\n"+getIndent, new StringBuilder("")))
      decreaseIndent
      s.append("\n").append(getIndent).append("}")
      s.toString
    case SThis(info) =>
      walk(info)+"this"
    case SThrow(info, expr) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("throw ").append(walk(expr)).append(";")
      s.toString
    case STry(info, body, catchBlock, fin) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("try\n{")
      increaseIndent
      s.append(getIndent).append(join(body, "\n"+getIndent, new StringBuilder("")))
      s.append("}")
      decreaseIndent
      if(catchBlock.isSome) s.append("\n").append(getIndent).append(walk(catchBlock))
      if(fin.isSome){
        var oneline: Boolean = isOneline(fin)
        s.append("\n").append(getIndent).append("finally\n{")
        increaseIndent
        s.append(getIndent).append(join(fin.get, "\n"+getIndent, new StringBuilder("")))
        s.append("}\n")
        decreaseIndent
      }
      s.toString
    case SUnaryAssignOpApp(info, lhs, op) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(lhs)).append(" ").append(walk(op))
      s.toStringP
    case SVarDecl(info, name, expr, _) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(name))
      if(expr.isSome) s.append(" = ").append(walk(expr))
      s.toString
    case SVarRef(info, id) =>
      walk(info)+walk(id)
    case SVarStmt(info, vds) => vds match {
      case Nil => walk(info)
      case _ =>
        val s: StringBuilder = new StringBuilder
        s.append(walk(info))
        s.append("var ")
        s.append(join(vds, ", ", new StringBuilder(""))).append(";")
        s.toString
    }
    case SWhile(info, cond, body) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(body)
      s.append("while(")
      s.append(walk(cond)).append(")\n")
      if(oneline) increaseIndent
      s.append(getIndent).append(walk(body))
      if(oneline) decreaseIndent
      s.toString
    case SWith(info, expr, stmt) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      var oneline: Boolean = isOneline(stmt)
      s.append("with(")
      s.append(walk(expr)).append(")\n")
      if(oneline) increaseIndent
      s.append(getIndent).append(walk(stmt))
      if(oneline) decreaseIndent
      s.toString

    // Module syntax
    case SModDecl(info, name, STopLevel(fds, vds, program)) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("module ").append(name.getText).append(" {\n")
      prFtn(s, fds, vds, NU.toStmts(program))
      s.append("\n}")
      s.toString
    case SModExpVarStmt(info, vds) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("export var ")
      s.append(join(vds, ", ", new StringBuilder(""))).append(";")
      s.toString
    case SModExpFunDecl(info, fd) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("export ").append(walk(fd))
      s.toString
    case SModExpGetter(info, fd) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("export ").append(walk(fd))
      s.toString
    case SModExpSetter(info, fd) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("export ").append(walk(fd))
      s.toString
    case SModExpSpecifiers(info, names) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("export ")
      s.append(join(names, ", ", new StringBuilder(""))).append(";")
      s.toString
    case SModImpDecl(info, imports) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("import ")
      s.append(join(imports, ", ", new StringBuilder(""))).append(";")
      s.toString
    case SModImpSpecifierSet(info, imports, module) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("{").append(join(imports, ", ", new StringBuilder(""))).append("} from ")
      s.append(walk(module))
      s.toString
    case SModImpAliasClause(info, name, alias) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(name)).append(" as ").append(walk(alias))
      s.toString
    case SModExpStarFromPath(info, path) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append("* from ").append(walk(path))
      s.toString
    case SModExpStar(info) => "*"
    case SModExpAlias(info, name, alias) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(name)).append(" : ").append(walk(alias))
      s.toString
    case SModExpName(info, name) => name match {
      case SPath(i, names) if names.length == 1 =>
        walk(info)+walk(names.head)
      case SPath(i, names) =>
        val s: StringBuilder = new StringBuilder
        s.append(walk(info))
        s.append(walk(names.last)).append(" from ").append(walk(SPath(i, names.dropRight(1))))
        s.toString
    }
    case SModImpAlias(info, name, alias) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(walk(name)).append(" : ").append(walk(alias))
      s.toString
    case SModImpName(info, name) =>
      walk(info)+walk(name)
    case SPath(info, names) =>
      val s: StringBuilder = new StringBuilder
      s.append(walk(info))
      s.append(join(names, ".", new StringBuilder("")))
      s.toString
    case SComment(info, comment) =>
      comment + "\n"
    case SASTSpanInfo(_, comment) => walk(comment)
    case _:NoOp => ""

    case Some(in) => walk(in)
    case None => ""
    case _ => "#@#"+node.getClass.toString
  }
}
