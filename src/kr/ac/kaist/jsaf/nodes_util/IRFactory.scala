/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF}
import kr.ac.kaist.jsaf.nodes_util._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.useful.Useful

import edu.rice.cs.plt.tuple.{Option => JOption}

import _root_.java.lang.{Integer => JInt}
import _root_.java.lang.{Double => JDouble}
import _root_.java.util.{List => JList}
import _root_.java.io.BufferedWriter
import _root_.java.io.File
import _root_.java.math.BigInteger
import _root_.java.util.ArrayList
import _root_.java.util.Arrays
import _root_.java.util.Collections
import _root_.java.util.Set
import _root_.java.util.StringTokenizer

object IRFactory {
  val dummyAst = NF.makeNoOp(NF.makeSpanInfo(NF.makeSpan("dummyAST")), "dummyAST")
  // For use only when there is no hope of attaching a true span.
  def dummySpan(villain: String): Span = {
    val name = if (villain.length != 0) villain else "dummySpan"
    val sl = new SourceLocRats(name,0,0,0)
    new Span(sl,sl)
  }
  def dummyIRId(name: String): IRId = makeTId(dummyAst, dummySpan(name), name)
  def dummyIRId(id: Id): IRId = {
    val name = id.getText
    makeTId(dummyAst, dummySpan(name), name)
  }
  def dummyIRId(label: Label): IRId = {
    val name = label.getId.getText
    makeTId(dummyAst, dummySpan(name), name)
  }
  def dummyIRStmt(ast: ASTNode, span: Span): IRSeq =
    makeSeq(ast, span, Nil.asInstanceOf[List[IRStmt]])
  def dummyIRExpr(): IRExpr = makeTId(dummyAst, dummySpan("_"), "_")
  def dummyIRStmt(ast: ASTNode, span: Span, msg: String): IRSeq =
    makeSeq(dummyAst, span, List(makeExprStmt(dummyAst, span, dummyIRId(msg), dummyIRExpr)))
  def dummySpanInfo(villain: String): IRSpanInfo =
    makeSpanInfo(false, dummyAst, dummySpan(villain))

  def makeSpanInfo(fromSource: Boolean, ast: ASTNode, span: Span): IRSpanInfo =
    new IRSpanInfo(fromSource, ast, span)
  def makeFunctional(fromSource: Boolean, ast: ASTNode,
                     name: IRId, params: JList[IRId], args: JList[IRStmt],
                     fds: JList[IRFunDecl], vds: JList[IRVarStmt],
                     body: JList[IRStmt]): IRFunctional =
    new IRFunctional(fromSource, ast, name, params, args, fds, vds, body)

  def makeFunctional(fromSource: Boolean, ast: ASTNode,
                     name: IRId, params: JList[IRId], body: IRStmt): IRFunctional =
    makeFunctional(fromSource, ast, name, params, toJavaList(Nil), toJavaList(Nil),
                   toJavaList(Nil), toJavaList(List(body)))

  def makeFunctional(fromSource: Boolean, ast: ASTNode, name: IRId, params: JList[IRId],
                     body: JList[IRStmt]): IRFunctional =
    makeFunctional(fromSource, ast, name, params, toJavaList(Nil), toJavaList(Nil),
                   toJavaList(Nil), body)

  def makeRoot(): IRRoot =
    makeRoot(false, dummyAst, dummySpan("disambiguatorOnly"), toJavaList(Nil),
             toJavaList(Nil), toJavaList(Nil))

  def makeRoot(fromSource: Boolean, ast: ASTNode, span: Span, irs: JList[IRStmt]): IRRoot =
    makeRoot(fromSource, ast, span, toJavaList(Nil), toJavaList(Nil), irs)

  def makeRoot(fromSource: Boolean, ast: ASTNode, span: Span, fds: JList[IRFunDecl], vds: JList[IRVarStmt],
               irs: JList[IRStmt]): IRRoot =
    new IRRoot(makeSpanInfo(fromSource, ast, span), fds, vds, irs)

  def makeFunExpr(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, name: IRId,
                  params: JList[IRId], body: IRStmt): IRFunExpr =
    makeFunExpr(fromSource, ast, span, lhs, name, params, toJavaList(Nil), toJavaList(Nil),
                toJavaList(Nil), toJavaList(List(body)))

  def makeFunExpr(fromSource: Boolean, ast: ASTNode,
                  span: Span, lhs: IRId, name: IRId, params: JList[IRId], args: JList[IRStmt],
                  fds: JList[IRFunDecl], vds: JList[IRVarStmt], body: JList[IRStmt]): IRFunExpr =
    new IRFunExpr(makeSpanInfo(fromSource, ast, span), lhs,
                  makeFunctional(fromSource, ast, name, params, args, fds, vds, body))

  def makeEval(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, arg: IRExpr) =
    new IREval(makeSpanInfo(fromSource, ast, span), lhs, arg)

  def makeUn(fromSource: Boolean, ast: ASTNode, span: Span, op: IROp, expr: IRExpr) =
    new IRUn(makeSpanInfo(fromSource, ast, span), op, expr)

  def makeDelete(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, expr: IRId) =
    new IRDelete(makeSpanInfo(fromSource, ast, span), lhs, expr)

  def makeDeleteProp(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, obj: IRId, index: IRExpr) =
    new IRDeleteProp(makeSpanInfo(fromSource, ast, span), lhs, obj, index)

  def makeObject(fromSource: Boolean, ast: ASTNode, span: Span,
                 lhs: IRId, members: List[IRMember], proto: IRId): IRObject =
    makeObject(fromSource, ast, span, lhs, toJavaList(members), Some(proto))

  def makeObject(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, members: List[IRMember]): IRObject =
    makeObject(fromSource, ast, span, lhs, toJavaList(members), None)

  def makeObject(fromSource: Boolean, ast: ASTNode, span: Span,
                 lhs: IRId, members: JList[IRMember], proto: Option[IRId]): IRObject =
    new IRObject(makeSpanInfo(fromSource, ast, span), lhs, members, proto)

  def makeArray(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, elements: List[Option[IRExpr]]) : IRArray = {
    val new_elements = toJavaList(elements.map(toJavaOption(_)))
    makeArray(fromSource, ast, span, lhs, new_elements)
  }

  def makeArray(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, elements: JList[JOption[IRExpr]]) : IRArray =
    new IRArray(makeSpanInfo(fromSource, ast, span), lhs, elements)

  def makeArrayNumber(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, elements: JList[JDouble]) : IRStmt =
    new IRArrayNumber(makeSpanInfo(fromSource, ast, span), lhs, elements)

  def makeArgs(ast: ASTNode, span: Span, lhs: IRId, elements: List[Option[IRExpr]]) : IRArgs = {
    val new_elements = toJavaList(elements.map(toJavaOption(_)))
    makeArgs(ast, span, lhs, new_elements)
  }

  def makeArgs(ast: ASTNode, span: Span, lhs: IRId, elements: JList[JOption[IRExpr]]) : IRArgs =
    new IRArgs(makeSpanInfo(false, ast, span), lhs, elements)

  def makeLoad(fromSource: Boolean, ast: ASTNode, span: Span, obj: IRId, index: IRExpr) =
    new IRLoad(makeSpanInfo(fromSource, ast, span), obj, index)

  def makeInternalCall(ast: ASTNode, span: Span, lhs: IRId, fun: IRId, arg: IRExpr) : IRInternalCall =
    makeInternalCall(ast, span, lhs, fun, arg, None)

  def makeInternalCall(ast: ASTNode, span: Span, lhs: IRId, fun: IRId, arg1: IRId, arg2: IRId) : IRInternalCall =
    makeInternalCall(ast, span, lhs, fun, arg1, Some(arg2))

  def makeInternalCall(ast: ASTNode, span: Span, lhs: IRId, fun: IRId, arg1: IRExpr, arg2: Option[IRId]) : IRInternalCall =
    new IRInternalCall(makeSpanInfo(false, ast, span), lhs, fun, arg1, toJavaOption(arg2))

  def makeCall(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, fun: IRId, thisB: IRId, args: IRId) : IRCall =
    new IRCall(makeSpanInfo(fromSource, ast, span), lhs, fun, thisB, args)

  def makeNew(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, fun: IRId, args: List[IRId]) : IRNew =
    makeNew(fromSource, ast, span, lhs, fun, toJavaList(args))

  def makeNew(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, fun: IRId, args: JList[IRId]) : IRNew =
    new IRNew(makeSpanInfo(fromSource, ast, span), lhs, fun, args)

  def makeBin(fromSource: Boolean, ast: ASTNode, span: Span, first: IRExpr, op: IROp, second: IRExpr) =
    new IRBin(makeSpanInfo(fromSource, ast, span), first, op, second)

  def makeLoadStmt(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, obj: IRId, index: IRExpr) =
    makeExprStmt(ast, span, lhs, makeLoad(fromSource, ast, span, obj, index))

  def makeExprStmt(ast: ASTNode, span: Span, lhs: IRId, right: IRExpr): IRExprStmt =
    makeExprStmt(ast, span, lhs, right, false)

  def makeExprStmtIgnore(ast: ASTNode, span: Span, lhs: IRId, right: IRExpr): IRExprStmt =
    makeExprStmt(ast, span, lhs, right, true)

  def makeExprStmt(ast: ASTNode, span: Span, lhs: IRId, right: IRExpr, isRef: Boolean): IRExprStmt =
    new IRExprStmt(makeSpanInfo(false, ast, span), lhs, right, isRef)

  def makeFunDecl(fromSource: Boolean, ast: ASTNode, span: Span,
                  name: IRId, params: JList[IRId], body: IRStmt): IRFunDecl =
    makeFunDecl(fromSource, ast, span, name, params, toJavaList(Nil), toJavaList(Nil),
                toJavaList(Nil), toJavaList(List(body)))

  def makeFunDecl(fromSource: Boolean, ast: ASTNode,
                  span: Span, name: IRId, params: JList[IRId], args: JList[IRStmt],
                  fds: JList[IRFunDecl], vds: JList[IRVarStmt], body: JList[IRStmt]): IRFunDecl =
    new IRFunDecl(makeSpanInfo(fromSource, ast, span),
                  makeFunctional(fromSource, ast, name, params, args, fds, vds, body))

  def makeBreak(fromSource: Boolean, ast: ASTNode, span: Span, label: IRId): IRBreak =
    new IRBreak(makeSpanInfo(fromSource, ast, span), label)

  def makeReturn(fromSource: Boolean, ast: ASTNode, span: Span, expr: JOption[IRExpr]) =
    new IRReturn(makeSpanInfo(fromSource, ast, span), expr)

  def makeLabelStmt(fromSource: Boolean, ast: ASTNode, span: Span, label: IRId, stmt: IRStmt): IRLabelStmt =
    new IRLabelStmt(makeSpanInfo(fromSource, ast, span), label, stmt)

  def makeWith(fromSource: Boolean, ast: ASTNode, span: Span, id: IRId, stmt: IRStmt) =
    new IRWith(makeSpanInfo(fromSource, ast, span), id, stmt)

  def makeThrow(fromSource: Boolean, ast: ASTNode, span: Span, expr: IRExpr) =
    new IRThrow(makeSpanInfo(fromSource, ast, span), expr)

  def makeVarStmt(fromSource: Boolean, ast: ASTNode, span: Span, lhs: IRId, fromParam: Boolean): IRVarStmt =
    new IRVarStmt(makeSpanInfo(fromSource, ast, span), lhs, fromParam)

  def makeIf(fromSource: Boolean, ast: ASTNode, span: Span, cond: IRExpr, trueB: IRStmt, falseB: JOption[IRStmt]) =
    new IRIf(makeSpanInfo(fromSource, ast, span), cond, trueB, falseB)

  def makeWhile(fromSource: Boolean, ast: ASTNode, span: Span, cond: IRExpr, body: IRStmt) =
    new IRWhile(makeSpanInfo(fromSource, ast, span), cond, body)

  def makeTry(fromSource: Boolean, ast: ASTNode, span: Span,
              body: IRStmt, name: JOption[IRId], catchB: JOption[IRStmt], finallyB: JOption[IRStmt]) =
    new IRTry(makeSpanInfo(fromSource, ast, span), body, name, catchB, finallyB)

  def makeStore(fromSource: Boolean, ast: ASTNode, span: Span, obj: IRId, index: IRExpr, rhs: IRExpr) =
    new IRStore(makeSpanInfo(fromSource, ast, span), obj, index, rhs)

  def makeSeq(ast: ASTNode, span: Span, first: IRStmt, second: IRStmt): IRSeq =
    makeSeq(ast, span, List(first, second))

  def makeSeq(ast: ASTNode, span: Span): IRSeq =
    makeSeq(ast, span, Nil)

  def makeSeq(ast: ASTNode, span: Span, stmt: IRStmt): IRSeq =
    makeSeq(ast, span, List(stmt))

  def makeSeq(ast: ASTNode, span: Span, stmts: List[IRStmt]): IRSeq =
    new IRSeq(makeSpanInfo(false, ast, span), toJavaList(stmts))

  def makeStmtUnit(ast: ASTNode, span: Span): IRStmtUnit =
    makeStmtUnit(ast, span, Useful.list().asInstanceOf[JList[IRStmt]])

  def makeStmtUnit(ast: ASTNode, span: Span, stmt: IRStmt): IRStmtUnit =
    makeStmtUnit(ast, span, Useful.list(stmt))

  def makeStmtUnit(ast: ASTNode, span: Span, first: IRStmt, second: IRStmt): IRStmtUnit =
    makeStmtUnit(ast, span, Useful.list(first, second))

  def makeStmtUnit(ast: ASTNode, span: Span, stmts: List[IRStmt]): IRStmtUnit =
    makeStmtUnit(ast, span, toJavaList(stmts))

  def makeStmtUnit(ast: ASTNode, span: Span, stmts: JList[IRStmt]): IRStmtUnit =
    new IRStmtUnit(makeSpanInfo(true, ast, span), stmts)

  def makeGetProp(fromSource: Boolean, ast: ASTNode, span: Span, prop: IRId, body: IRStmt): IRGetProp =
    makeGetProp(fromSource, ast, span,
                makeFunctional(fromSource, ast, prop, toJavaList(Nil).asInstanceOf[JList[IRId]], body))

  def makeGetProp(fromSource: Boolean, ast: ASTNode,
                  span: Span, name: IRId, params: JList[IRId], args: JList[IRStmt],
                  fds: JList[IRFunDecl], vds: JList[IRVarStmt],
                  body: JList[IRStmt]): IRGetProp =
    makeGetProp(fromSource, ast, span, makeFunctional(true, ast, name, params, args, fds, vds, body))

  def makeGetProp(fromSource: Boolean, ast: ASTNode, span: Span, functional: IRFunctional): IRGetProp =
    new IRGetProp(makeSpanInfo(fromSource, ast, span), functional)

  def makeSetProp(fromSource: Boolean, ast: ASTNode,
                  span: Span, name: IRId, params: JList[IRId], args: JList[IRStmt],
                  fds: JList[IRFunDecl], vds: JList[IRVarStmt],
                  body: JList[IRStmt]): IRSetProp =
    makeSetProp(fromSource, ast, span, makeFunctional(true, ast, name, params, args, fds, vds, body))

  def makeSetProp(fromSource: Boolean, ast: ASTNode, span: Span, prop: IRId, id: IRId, body: IRStmt): IRSetProp =
    makeSetProp(fromSource, ast, span, makeFunctional(true, ast, prop, toJavaList(List(id)), body))

  def makeSetProp(fromSource: Boolean, ast: ASTNode, span: Span, functional: IRFunctional) =
    new IRSetProp(makeSpanInfo(fromSource, ast, span), functional)

  def makeField(fromSource: Boolean, ast: ASTNode, span: Span, prop: IRId, expr: IRExpr) =
    new IRField(makeSpanInfo(fromSource, ast, span), prop, expr)

  val defaultInfo = new IRSourceInfo(false, dummyAst)
  def trueInfo(ast: ASTNode) = new IRSourceInfo(true, dummyAst)
  def makeSourceInfo(fromSource: Boolean, ast: ASTNode) =
    if (fromSource) trueInfo(ast) else defaultInfo

  val makeUndef = new IRUndef(defaultInfo)
  val makeNull = new IRNull(defaultInfo)
  def makeNullFromSource(ast: ASTNode) = new IRNull(trueInfo(ast))

  def makeBool(fromSource: Boolean, ast: ASTNode, bool: Boolean) =
    new IRBool(makeSourceInfo(fromSource, ast), bool)
  val trueV = makeBool(false, dummyAst, true)
  val falseV = makeBool(false, dummyAst, false)
  val trueVFromSource = makeBool(true, dummyAst, true)
  val falseVFromSource = makeBool(true, dummyAst, false)

  def makeNumber(fromSource: Boolean, text: String, num: Double): IRNumber =
    makeNumber(fromSource, dummyAst, text, num)
  def makeNumber(fromSource: Boolean, ast: ASTNode, text: String, num: Double): IRNumber =
    new IRNumber(makeSourceInfo(fromSource, ast), text, num)

  val zero  = new IRString(defaultInfo, "0", None)
  val one   = new IRString(defaultInfo, "1", None)
  val two   = new IRString(defaultInfo, "2", None)
  val three = new IRString(defaultInfo, "3", None)
  val four  = new IRString(defaultInfo, "4", None)
  val five  = new IRString(defaultInfo, "5", None)
  val six   = new IRString(defaultInfo, "6", None)
  val seven = new IRString(defaultInfo, "7", None)
  val eight = new IRString(defaultInfo, "8", None)
  val nine  = new IRString(defaultInfo, "9", None)
  def makeString(str: String): IRString = makeString(false, dummyAst, str, None)
  def makeString(fromSource: Boolean, ast: ASTNode, str1: String, str2: Option[String]): IRString = str2 match {
    case None =>
      if(str1.equals("0")) zero
      else if(str1.equals("1")) one
      else if(str1.equals("2")) two
      else if(str1.equals("3")) three
      else if(str1.equals("4")) four
      else if(str1.equals("5")) five
      else if(str1.equals("6")) six
      else if(str1.equals("7")) seven
      else if(str1.equals("8")) eight
      else if(str1.equals("9")) nine
      else new IRString(defaultInfo, str1, str2)
    case Some(escaped) =>
      if (str1.equals(escaped)) new IRString(makeSourceInfo(fromSource, ast), str1, None)
      else new IRString(makeSourceInfo(fromSource, ast), str1, str2)
  }

  def makeThis(ast: ASTNode, span: Span) =
    new IRThis(makeSpanInfo(true, ast, span))

  // make a user id
  def makeUId(originalName: String, uniqueName: String, isGlobal: Boolean,
              ast: ASTNode, span: Span, isWith: Boolean): IRUserId =
    new IRUserId(makeSpanInfo(true, ast, span), originalName, uniqueName, isGlobal, isWith)

  // make a withRewriter-generated id
  def makeWId(originalName: String, uniqueName: String, isGlobal: Boolean,
              ast: ASTNode, span: Span): IRUserId =
    makeUId(originalName, uniqueName, isGlobal, ast, span, true)

  // make a non-global user id
  def makeNGId(uniqueName: String, ast: ASTNode, span: Span): IRUserId =
    makeUId(uniqueName, uniqueName, false, ast, span, false)

  def makeNGId(originalName: String, uniqueName: String, ast: ASTNode, span: Span): IRUserId =
    makeUId(originalName, uniqueName, false, ast, span, false)

  // make a global user id
  def makeGId(ast: ASTNode, uniqueName: String): IRUserId =
    makeUId(uniqueName, uniqueName, true, ast, dummySpan(uniqueName), false)

  // make a global user id
  def makeGId(ast: ASTNode, originalName: String, uniqueName: String, span: Span): IRUserId =
    makeUId(originalName, uniqueName, true, ast, span, false)

  // make a non-global temporary id
  def makeTId(ast: ASTNode, span: Span, uniqueName: String): IRTmpId =
    makeTId(false, ast, span, uniqueName, uniqueName, false)

  def makeTId(fromSource: Boolean, ast: ASTNode, span: Span, uniqueName: String): IRTmpId =
    makeTId(fromSource, ast, span, uniqueName, uniqueName, false)

  // make a temporary id
  def makeTId(ast: ASTNode, span: Span, uniqueName: String, isGlobal: Boolean): IRTmpId =
    makeTId(false, ast, span, uniqueName, uniqueName, isGlobal)

  def makeTId(ast: ASTNode, span: Span, originalName: String, uniqueName: String, isGlobal: Boolean): IRTmpId =
    makeTId(false, ast, span, originalName, uniqueName, isGlobal)

  def makeTId(fromSource: Boolean, ast: ASTNode, span: Span, originalName: String, uniqueName: String,
              isGlobal: Boolean): IRTmpId =
    new IRTmpId(makeSpanInfo(fromSource, ast, span), originalName, uniqueName, isGlobal)

  def makeOp(name: String, kind: Int = 0) = {
    new IROp(name, if(kind == 0) EJSOp.strToEJSOp(name) else kind)
  }

  def makeInfo(span: Span): IRSpanInfo =
    makeInfo(dummyAst, span)
  def makeInfo(ast: ASTNode, span: Span): IRSpanInfo =
    new IRSpanInfo(false, ast, span)

  def makeNoOp(ast: ASTNode, span: Span, desc: String) =
    new IRNoOp(makeSpanInfo(false, ast, span), desc)

  val oneV = makeNumber(false, dummyAst, "1", 1)
}
