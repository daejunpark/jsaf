/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import _root_.java.util.ArrayList
import _root_.java.util.{List => JList}

import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._
import edu.rice.cs.plt.iter.IterUtil
import edu.rice.cs.plt.tuple.Option

/* Converts a WIDL AST to a string which is the concrete version of that node
 *
 * Caveats:
 * 1. Comments are not preserved.
 *
 * Possible improvements:
 * 1. We may want to keep comments.
 */
class WIDLToString(program: JList[WDefinition]) extends Walker {

  val width = 50
  val significantBits = NU.significantBits

  def doit() = walk(program)

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
      case '"'  => s.append("\\\"")
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
                    case SVarDecl(_,n,_) =>
                      s.append("var "+n.getText+";\n"+getIndent)})
        decreaseIndent
        s.append("\n").append(getIndent)
      }
    increaseIndent
    s.append(getIndent).append(join(body, "\n"+getIndent, new StringBuilder("")))
    decreaseIndent
  }

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
      /*
    case SWAnyType(info, suffix) =>
    case SWArgument(info, attributes, typ, name, def) =>
    case SWArrayType(info, suffix, typ) =>
    case SWAttribute(info, attrs, typ, name) =>
    case SWBoolean(info, value) =>
    case SWCallback(info, attrs, name, returnType, args) =>
    case SWConst(info, attrs, typ, name, value) =>
    case SWDictionary(info, attrs, name, parent, members) =>
    case SWDictionaryMember(info, attrs, typ, name, def) =>
    case SWEAAttribute =>
    case SWEACallback =>
    case SWEAConst =>
    case SWEACreator =>
    case SWEADeleter =>
    case SWEADictionary =>
    case SWEAEllipsis =>
    case SWEAEnum =>
    case SWEAException =>
    case SWEAGetter =>
    case SWEAImplements =>
    case SWEAInherit =>
    case SWEAInterface =>
    case SWEALegacycaller =>
    case SWEAOptional =>
    case SWEAPartial =>
    case SWEAQuestion =>
    case SWEAReadonly =>
    case SWEASetter =>
    case SWEAStatic =>
    case SWEAString(str) =>
    case SWEAStringifier =>
    case SWEATypedef =>
    case SWEAUnrestricted =>
    case SWEnum(info, attrs, name, enumValueList) =>
    case SWException(info, attrs, name, parent, members) =>
    case SWExceptionField(info, attrs, typ, name) =>
    case SWFloat(info, value) =>
    case SWId(info, name) =>
    case SWImplementsStatement(info, attrs, name, parent) =>
    case SWInteger(info, value) =>
    case SWInterface(info, attrs, name, parent, members) =>
    case SWNamedType(info, suffix, name) =>
    case SWNull(info) =>
    case SWOperation(info, attrs, qualifiers, typ, name, args) =>
      */
    case SWQCreator => "creator"
    case SWQDeleter => "deleter"
    /*
    case SWQGetter =>
    case SWQLegacycaller =>
    case SWQSetter =>
    case SWQStatic =>
    case SWSequenceType(info, suffix, typ) =>
    case SWSpanInfo(span) =>
    case SWString(info, str) =>
    case SWTSArray =>
    case SWTSQuestion =>
    case SWTypedef(info, attrs, typ, name) =>
    */
    case SWUnionType(info, suffix, types) =>
      val s: StringBuilder = new StringBuilder
      s.append("Union[")
      suffix.foreach(e => s.append(walk(e)).append(", "))
      s.append("](")
      types.foreach(e => s.append(walk(e)).append(", "))
      s.append(")")
      s.toString
    case _:NoOp => ""
    case Some(in) => walk(in)
    case None => ""
    case _ => "#@#"+node.getClass.toString
  }
}
