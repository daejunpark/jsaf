/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.useful.Useful

import edu.rice.cs.plt.tuple.{Option => JOption}

import _root_.java.lang.{Double => JDouble}
import _root_.java.lang.{Integer => JInt}
import _root_.java.util.{List => JList}
import _root_.java.io.BufferedReader
import _root_.java.io.BufferedWriter
import _root_.java.io.File
import _root_.java.math.BigInteger
import _root_.java.math.BigDecimal
import _root_.java.util.ArrayList
import _root_.java.util.Arrays
import _root_.java.util.Collections
import _root_.java.util.Set
import _root_.java.util.StringTokenizer
import scala.collection.mutable.{HashMap => MHashMap}

object NodeFactory {
  // Maps the unique ids for IR nodes to their corresponding AST nodes
  private var ir2astMap = new MHashMap[Long, ASTNode] // IRNode.uid -> ASTNode
  private var irinfo2irMap = new MHashMap[Long, IRNode] // IRInfoNode.uid -> IRNode
  def initIr2ast: Unit = {ir2astMap = new MHashMap; irinfo2irMap = new MHashMap}
  def ir2ast(ir: IRNode): Option[ASTNode] = ir2astMap.get(ir.asInstanceOf[UIDObject].getUID)
  def irinfo2ir(info: IRInfoNode): Option[IRNode] = irinfo2irMap.get(info.getUID)
  def putIr2ast[A <: IRNode](ir: A, ast: ASTNode): A = {
    ir2astMap.put(ir.asInstanceOf[UIDObject].getUID, ast)
    ir match {
      case ir: IRAbstractNode => irinfo2irMap.put(ir.getInfo.getUID, ir)
      case ir: IRExpr => irinfo2irMap.put(ir.getInfo.getUID, ir)
      case ir: IRInfoNode => irinfo2irMap.put(ir.getUID, ir)
      case _ =>
    }
    ir
  }

  // For use only when there is no hope of attaching a true span.
  def makeSpan(villain: String): Span = {
    val sl = new SourceLocRats(villain,0,0,0)
    new Span(sl,sl)
  }

  def makeSpan(node: ASTNode): Span = NU.getSpan(node)

  def makeSpan(start: Span, finish: Span): Span =
    new Span(start.getBegin, finish.getEnd)

  def makeSpan(file: String, line: Int, startC: Int, endC: Int): Span =
    new Span(new SourceLocRats(file, line, startC, 0),
             new SourceLocRats(file, line, endC, 0))

  def makeSpan(start: ASTNode, finish: ASTNode): Span =
    makeSpan(NU.getSpan(start), NU.getSpan(finish))

  def makeSpan(start: ASTNode, l: JList[ASTNode]): Span = {
    val s = l.size
    if (s==0) makeSpan(start, start) else makeSpan(start, l.get(s-1))
  }

  def makeSpan(l: JList[ASTNode], finish: ASTNode): Span = {
    val s = l.size
    if (s==0) makeSpan(finish, finish) else makeSpan(l.get(0), finish)
  }

  def makeSpan(ifEmpty: String, l: JList[ASTNode]): Span = {
    val s = l.size
    if (s==0) makeSpan(ifEmpty) else makeSpan(l.get(0), l.get(s-1))
  }

  /**
   * In some situations, a begin-to-end span is not really right, and something
   * more like a set of spans ought to be used.  Even though this is not yet
   * implemented, the name is provided to allow expression of intent.
   */
  def makeSetSpan(start: ASTNode, l: JList[ASTNode]): Span = makeSpan(start, l)

  /**
   * In some situations, a begin-to-end span is not really right, and something
   * more like a set of spans ought to be used.  Even though this is not yet
   * implemented, the name is provided to allow expression of intent.
   */
  def makeSetSpan(a: ASTNode, b: ASTNode): Span = makeSpan(a,b)

  /**
   * In some situations, a begin-to-end span is not really right, and something
   * more like a set of spans ought to be used.  Even though this is not yet
   * implemented, the name is provided to allow expression of intent.
   *
   */
  def makeSetSpan(ifEmpty: String, l: JList[ASTNode]): Span = makeSpan(ifEmpty, l)
  def makeSpanInfo(span: Span): SpanInfo = new SpanInfo(span)
  def makeSpanInfo(info: SpanInfo, span: Span): SpanInfo = new SpanInfo(span)

  def makeTopLevel(body: JList[SourceElement]): TopLevel =
    makeTopLevel(toJavaList(Nil), toJavaList(Nil), body)

  def makeTopLevel(fds: JList[FunDecl], vds: JList[VarDecl],
                   body: JList[SourceElement]): TopLevel =
    new TopLevel(fds, vds, body)

  def makeProgram(span: Span, elements: JList[SourceElement],
                  comments: JList[Comment]): Program =
    makeProgram(makeSpanInfo(span), makeTopLevel(elements), comments)

  def makeProgram(info: SpanInfo, body: List[SourceElement],
                  comments: List[Comment]): Program =
    makeProgram(info, makeTopLevel(toJavaList(body)), toJavaList(comments))

  def makeProgram(info: SpanInfo, toplevel: TopLevel,
                  comments: JList[Comment]): Program =
    new Program(info, toplevel, comments)

  def makeModDecl(span: Span, name: Id, body: JList[SourceElement]) =
    new ModDecl(makeSpanInfo(span), name, makeTopLevel(body))

  def makeModExpVarStmt(span: Span, vds: JList[VarDecl]) =
    new ModExpVarStmt(makeSpanInfo(span), vds)

  def makeModExpFunDecl(span: Span, fd: FunDecl) =
    new ModExpFunDecl(makeSpanInfo(span), fd)

  def makeModExpGetter(span: Span, name: Id, body: JList[SourceElement]) =
    new ModExpGetter(makeSpanInfo(span),
                     makeGetProp(span, makePropId(span, name), body))

  def makeModExpSetter(span: Span, name: Id, param: Id, body: JList[SourceElement]) =
    new ModExpSetter(makeSpanInfo(span),
                     makeSetProp(span, makePropId(span, name), param, body))

  def makeModExpSpecifiers(span: Span, names: JList[ModExpSpecifier]) =
    new ModExpSpecifiers(makeSpanInfo(span), names)

  def makeExportName(span: Span, name: Id): ModExpSpecifier =
    new ModExpName(makeSpanInfo(span), makePath(name))

  def makeExportName(span: Span, name: Id, path: Path): ModExpSpecifier =
    new ModExpName(makeSpanInfo(span), makePath(name, path))

  def makeStarFromPath(span: Span, path: Path): ModExpSpecifier =
    new ModExpStarFromPath(makeSpanInfo(span), path)

  def makeStar(span: Span): ModExpSpecifier =
    new ModExpStar(makeSpanInfo(span))

  def makeExportAlias(span: Span, name: Id, alias: Path): ModExpSpecifier =
    new ModExpAlias(makeSpanInfo(span), name, alias)

  def makeModImpDecl(span: Span, imports: JList[ModImport]) =
    new ModImpDecl(makeSpanInfo(span), imports)

  def makeModImpSpecifierSet(span: Span, imports: JList[ModImpSpecifier], module: Path): ModImport =
    new ModImpSpecifierSet(makeSpanInfo(span), imports, module)

  def makeModImpAlias(span: Span, name: Path, alias: Id): ModImport =
    new ModImpAliasClause(makeSpanInfo(span), name, alias)

  def makeImportAlias(span: Span, name: Id, alias: Id): ModImpSpecifier =
    new ModImpAlias(makeSpanInfo(span), name, alias)

  def makeImportName(span: Span, name: Id): ModImpSpecifier =
    new ModImpName(makeSpanInfo(span), name)

  def makeFunctional(name: Id, fds: JList[FunDecl], vds: JList[VarDecl],
                     body: JList[SourceElement], params: JList[Id]) =
    new Functional(fds, vds, body, name, params)

  def makeFunDecl(span: Span, name: Id, params: JList[Id],
                  body: JList[SourceElement]) =
    new FunDecl(makeSpanInfo(span),
                makeFunctional(name, toJavaList(Nil), toJavaList(Nil), body, params))

  def makeFunExpr(span: Span, name: Id, params: JList[Id],
                  body: JList[SourceElement]) =
    new FunExpr(makeSpanInfo(span),
                makeFunctional(name, toJavaList(Nil), toJavaList(Nil), body, params))

  def makeBlock(span: Span, stmts: JList[Stmt]) =
    new Block(makeSpanInfo(span), stmts)

  def makeVarStmt(span: Span, vds: JList[VarDecl]) =
    new VarStmt(makeSpanInfo(span), vds)

  def makeEmptyStmt(span: Span) =
    new EmptyStmt(makeSpanInfo(span))

  def makeExprStmt(span: Span, expr: Expr) =
    new ExprStmt(makeSpanInfo(span), expr)

  def makeIf(span: Span, cond: Expr, trueB: Stmt, falseB: JOption[Stmt]) =
    new If(makeSpanInfo(span), cond, trueB, falseB)

  def makeDoWhile(span: Span, body: Stmt, cond: Expr) =
    new DoWhile(makeSpanInfo(span), body, cond)

  def makeWhile(span: Span, cond: Expr, body: Stmt) =
    new While(makeSpanInfo(span), cond, body)

  def makeFor(span: Span, init: JOption[Expr], cond: JOption[Expr],
              action: JOption[Expr], body: Stmt) =
    new For(makeSpanInfo(span), init, cond, action, body)

  def makeForVar(span: Span, vars: JList[VarDecl], cond: JOption[Expr],
                 action: JOption[Expr], body: Stmt) =
    new ForVar(makeSpanInfo(span), vars, cond, action, body)

  def makeForIn(span: Span, lhs: LHS, expr: Expr, body: Stmt) =
    new ForIn(makeSpanInfo(span), lhs, expr, body)

  def makeForVarIn(span: Span, vd: VarDecl, expr: Expr, body: Stmt) =
    new ForVarIn(makeSpanInfo(span), vd, expr, body)

  def makeContinue(span: Span, target: JOption[Label]) =
    new Continue(makeSpanInfo(span), target)

  def makeBreak(span: Span, target: JOption[Label]) =
    new Break(makeSpanInfo(span), target)

  def makeReturn(span: Span, expr: JOption[Expr]) =
    new Return(makeSpanInfo(span), expr)

  def makeWith(span: Span, expr: Expr, stmt: Stmt) =
    new With(makeSpanInfo(span), expr, stmt)

  def makeSwitch(span: Span, expr: Expr, front: JList[Case]): Switch =
    makeSwitch(span, expr, front, none[JList[Stmt]], toJavaList(Nil))

  def makeSwitch(span: Span, expr: Expr, front: JList[Case],
                 defaultC: JOption[JList[Stmt]], back: JList[Case]): Switch =
    new Switch(makeSpanInfo(span), expr, front, defaultC, back)

  def makeLabelStmt(span: Span, label: Label, stmt: Stmt) =
    new LabelStmt(makeSpanInfo(span), label, stmt)

  def makeThrow(span: Span, expr: Expr) =
    new Throw(makeSpanInfo(span), expr)

  def makeTry(span: Span, body: JList[Stmt], catchB: Catch): Try =
    makeTry(span, body, some(catchB), none[JList[Stmt]])

  def makeTry(span: Span, body: JList[Stmt], fin: JList[Stmt]): Try =
    makeTry(span, body, none[Catch], some(fin))

  def makeTry(span: Span, body: JList[Stmt], catchB: Catch, fin: JList[Stmt]): Try =
    makeTry(span, body, some(catchB), some(fin))

  def makeTry(span: Span, body: JList[Stmt], catchB: JOption[Catch], fin: JOption[JList[Stmt]]): Try =
    new Try(makeSpanInfo(span), body, catchB, fin)

  def makeDebugger(span: Span) =
    new Debugger(makeSpanInfo(span))

  def makeVarDecl(span: Span, name: Id, expr: JOption[Expr]) =
    new VarDecl(makeSpanInfo(span), name, expr)

  def makeCase(span: Span, cond: Expr, body: JList[Stmt]) =
    new Case(makeSpanInfo(span), cond, body)

  def makeCatch(span: Span, id: Id, body: JList[Stmt]) =
    new Catch(makeSpanInfo(span), id, body)

  def makeExprList(span: Span, es: JList[Expr]) =
    new ExprList(makeSpanInfo(span), es)

  def makeCond(span: Span, cond: Expr, trueB: Expr, falseB: Expr) =
    new Cond(makeSpanInfo(span), cond, trueB, falseB)

  def makeInfixOpApp(span: Span, left: Expr, op: Op, right: Expr) =
    new InfixOpApp(makeSpanInfo(span), left, op, right)

  def makePrefixOpApp(span: Span, op: Op, right: Expr) =
    new PrefixOpApp(makeSpanInfo(span), op, right)

  def makeUnaryAssignOpApp(span: Span, lhs: LHS, op: Op) =
    new UnaryAssignOpApp(makeSpanInfo(span), lhs, op)

  def makeAssignOpApp(span: Span, lhs: LHS, op: Op, right: Expr) =
    new AssignOpApp(makeSpanInfo(span), lhs, op, right)

  def makeBracket(span: Span, lhs: LHS, index: Expr) =
    new Bracket(makeSpanInfo(span), lhs, index)

  def makeDot(span: Span, lhs: LHS, member: Id) =
    new Dot(makeSpanInfo(span), lhs, member)

  def makeNew(span: Span, lhs: LHS) =
    new New(makeSpanInfo(span), lhs)

  def makeFunApp(span: Span, lhs: LHS, args: JList[Expr]) =
    new FunApp(makeSpanInfo(span), lhs, args)

  def makeThis(span: Span) =
    new This(makeSpanInfo(span))

  def makeNull(span: Span) =
    new Null(makeSpanInfo(span))

  def makeBool(span: Span, bool: Boolean) =
    new Bool(makeSpanInfo(span), bool)

  def makeVarRef(span: Span, id: Id) =
    new VarRef(makeSpanInfo(span), id)

  def makeArrayNumberExpr(span: Span, elmts: JList[JDouble]) = {
    if (elmts.size > 1000)
      new ArrayNumberExpr(makeSpanInfo(span), elmts)
    else
      makeArrayExpr(span, toJavaList(toList(elmts).map(e => some(makeDoubleLiteral(span, e.toString, e).asInstanceOf[Expr]))))
  }

  def makeArrayExpr(span: Span, elmts: JList[JOption[Expr]]) =
    new ArrayExpr(makeSpanInfo(span), elmts)

  def makeObjectExpr(span: Span, elmts: JList[Member]) =
    new ObjectExpr(makeSpanInfo(span), elmts)

  def makeParenthesized(span: Span, expr: Expr) =
    new Parenthesized(makeSpanInfo(span), expr)

    /*
     * DecimalLiteral ::=
     *   DecimalIntegerLiteral . DecimalDigits? ExponentPart?
     * | DecimalIntegerLiteral ExponentPart?
     * | . DecimalDigits ExponentPart?
     *
     * DecimalIntegerLiteral ::=
     *   0
     * | NonZeroDigit DecimalDigits?
     *
     * DecimalDigit ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
     *
     * NonZeroDigit ::= 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
     *
     * ExponentPart ::= (e | E) (+ | -)? DecimalDigit+
     */
  def makeNumericLiteral(writer: BufferedWriter, span: Span,
                         beforeDot: String, dot: String,
                         afterDot: String, exponent: String) = {
    if ((beforeDot+dot).equals("") ||
        ((beforeDot+afterDot).equals("") && !dot.equals("")) ||
        (!beforeDot.equals("") && dot.equals("") && !afterDot.equals("")))
      NU.log(writer, span, "Syntax Error: expected a numeral but got "+
             beforeDot+dot+afterDot+exponent)
    if (!beforeDot.equals("") && !beforeDot.equals("0") && beforeDot.charAt(0) == '0')
      NU.log(writer, span, "Syntax Error: a numeral begins with 0.")
    if (dot.equals("")) {
      if (exponent.equals("")) makeIntLiteral(span, new BigInteger(beforeDot))
      else {
        var exp = 0
        val second = exponent.charAt(1)
        if (Character.isDigit(second))
          exp = JInt.parseInt(exponent.substring(1))
        else if (second.equals('-'))
          exp = -1 * JInt.parseInt(exponent.substring(2))
        else exp = JInt.parseInt(exponent.substring(2))
        if (exp < 0) {
          var str = beforeDot+dot+afterDot+exponent
          str = new BigDecimal(str).toString
          makeDoubleLiteral(span, str, JDouble.valueOf(str))
        } else makeIntLiteral(span, new BigInteger(beforeDot).multiply(BigInteger.TEN.pow(exp)))
      }
    } else {
      val str = beforeDot+dot+afterDot+exponent
      makeDoubleLiteral(span, str, JDouble.valueOf(str))
    }
  }

  def makeNumericLiteral(writer: BufferedWriter, span: Span,
                         beforeDot: String) = {
    if (beforeDot.equals(""))
      NU.log(writer, span, "Syntax Error: expected a numeral but got "+
             beforeDot)
    if (!beforeDot.equals("") && !beforeDot.equals("0") && beforeDot.charAt(0) == '0')
      NU.log(writer, span, "Syntax Error: a numeral begins with 0.")
    JDouble.valueOf(beforeDot)
  }

  def makeIntLiteral(span: Span, intVal: BigInteger, radix: Int = 10) =
    new IntLiteral(makeSpanInfo(span), intVal, radix)

  def makeDoubleLiteral(span: Span, str: String, doubleVal: Double) =
    new DoubleLiteral(makeSpanInfo(span), str, doubleVal)

  def makeHexIntegerLiteral(span: Span, num: String) =
    makeIntLiteral(span, new BigInteger(num, 16), 16)

  def makeOctalIntegerLiteral(span: Span, num: String) =
    makeIntLiteral(span, new BigInteger(num, 8), 8)

  def makeStringLiteral(span: Span, str: String, quote: String) =
    new StringLiteral(makeSpanInfo(span), quote, str)

  def makeRegularExpression(span: Span, body: String, flags: String) =
    new RegularExpression(makeSpanInfo(span), body, flags)

  def makeField(span: Span, prop: Property, expr: Expr) =
    new Field(makeSpanInfo(span), prop, expr)

  def makeGetProp(span: Span, prop: Property, body: JList[SourceElement]) =
    new GetProp(makeSpanInfo(span), prop,
                makeFunctional(NU.prop2Id(prop), toJavaList(Nil), toJavaList(Nil), body,
                               toJavaList(Nil)))

  def makeSetProp(span: Span, prop: Property, id: Id,
                  body: JList[SourceElement]) =
    new SetProp(makeSpanInfo(span), prop,
                makeFunctional(NU.prop2Id(prop), toJavaList(Nil), toJavaList(Nil), body,
                               toJavaList(List(id))))

  def makePropId(span: Span, id: Id) =
    new PropId(makeSpanInfo(span), id)

  def makePropStr(span: Span, str: String) =
    new PropStr(makeSpanInfo(span), str)

  def makePropNum(span: Span, num: NumberLiteral) =
    new PropNum(makeSpanInfo(span), num)

  def makeId(span: Span, name: String, uniq: String): Id =
    makeId(span, name, some(uniq))

  def makeId(span: Span, name: String): Id =
    makeId(span, name, None)

  def makeId(span: Span, name: String, uniq: Option[String]): Id =
    new Id(makeSpanInfo(span), name, uniq, false)

  def makeOp(span: Span, name: String) =
    new Op(makeSpanInfo(span), name)

  def makeLabel(span: Span, id: Id) =
    new Label(makeSpanInfo(span), id)

  def makeComment(span: Span, comment: String): Comment =
    new Comment(makeSpanInfo(span), comment)

  def makePath(id: Id): Path =
    makePath(NU.getSpan(id), toJavaList(List(id)))

  def makePath(id: Id, path: Path): Path =
    makePath(makeSpan(id, path), toJavaList(toList(path.getNames):+id))

  def makePath(p: Path, path: Path): Path =
    makePath(makeSpan(p, path), toJavaList(toList(path.getNames)++toList(p.getNames)))

  def makePath(span: Span, ids: JList[Id]): Path =
    new Path(makeSpanInfo(span), ids)

  def makeNoOp(info: SpanInfo, desc: String) =
    new NoOp(info, desc)
}
