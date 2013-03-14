/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
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
  // For use only when there is no hope of attaching a true span.
  def dummySpan(villain: String): Span = {
    val name = if (villain.length != 0) villain else "dummySpan"
    val sl = new SourceLocRats(name,0,0,0)
    new Span(sl,sl)
  }
  def dummyIRId(name: String): IRId = makeTId(dummySpan(name), name)
  def dummyIRId(id: Id): IRId = {
    val name = id.getText
    makeTId(dummySpan(name), name)
  }
  def dummyIRId(label: Label): IRId = {
    val name = label.getId.getText
    makeTId(dummySpan(name), name)
  }
  def dummyIRStmt(span: Span): IRSeq =
    makeSeq(span, Nil.asInstanceOf[List[IRStmt]])
  def dummyIRExpr(): IRExpr = makeTId(dummySpan("_"), "_")
  def dummyIRStmt(span: Span, msg: String): IRSeq =
    makeSeq(span, List(makeExprStmt(span, dummyIRId(msg), dummyIRExpr)))
  def dummySpanInfo(villain: String): IRSpanInfo =
    makeSpanInfo(false, dummySpan(villain))

  def makeSpanInfo(fromSource: Boolean, span: Span): IRSpanInfo =
    new IRSpanInfo(fromSource, span)
  def makeFunctional(fromSource: Boolean,
                     name: IRId, params: JList[IRId], args: JList[IRStmt],
                     fds: JList[IRFunDecl], vds: JList[IRVarStmt],
                     body: JList[IRStmt]): IRFunctional =
    new IRFunctional(fromSource, name, params, args, fds, vds, body)

  def makeFunctional(fromSource: Boolean,
                     name: IRId, params: JList[IRId], body: IRStmt): IRFunctional =
    makeFunctional(fromSource, name, params, toJavaList(Nil), toJavaList(Nil),
                   toJavaList(Nil), toJavaList(List(body)))

  def makeFunctional(fromSource: Boolean, name: IRId, params: JList[IRId],
                     body: JList[IRStmt]): IRFunctional =
    makeFunctional(fromSource, name, params, toJavaList(Nil), toJavaList(Nil),
                   toJavaList(Nil), body)

  def makeRoot(): IRRoot =
    makeRoot(false, dummySpan("disambiguatorOnly"), toJavaList(Nil),
             toJavaList(Nil), toJavaList(Nil))

  def makeRoot(fromSource: Boolean, span: Span, irs: JList[IRStmt]): IRRoot =
    makeRoot(fromSource, span, toJavaList(Nil), toJavaList(Nil), irs)

  def makeRoot(fromSource: Boolean, span: Span, fds: JList[IRFunDecl], vds: JList[IRVarStmt],
               irs: JList[IRStmt]): IRRoot =
    new IRRoot(makeSpanInfo(fromSource, span), fds, vds, irs)

  def makeFunExpr(fromSource: Boolean, span: Span, lhs: IRId, name: IRId,
                  params: JList[IRId], body: IRStmt): IRFunExpr =
    makeFunExpr(fromSource, span, lhs, name, params, toJavaList(Nil), toJavaList(Nil),
                toJavaList(Nil), toJavaList(List(body)))

  def makeFunExpr(fromSource: Boolean,
                  span: Span, lhs: IRId, name: IRId, params: JList[IRId], args: JList[IRStmt],
                  fds: JList[IRFunDecl], vds: JList[IRVarStmt], body: JList[IRStmt]): IRFunExpr =
    new IRFunExpr(makeSpanInfo(fromSource, span), lhs,
                  makeFunctional(fromSource, name, params, args, fds, vds, body))

  def makeEval(fromSource: Boolean, span: Span, lhs: IRId, arg: IRExpr) =
    new IREval(makeSpanInfo(fromSource, span), lhs, arg)

  def makeUn(fromSource: Boolean, span: Span, op: IROp, expr: IRExpr) =
    new IRUn(makeSpanInfo(fromSource, span), op, expr)

  def makeDelete(fromSource: Boolean, span: Span, lhs: IRId, expr: IRId) =
    new IRDelete(makeSpanInfo(fromSource, span), lhs, expr)

  def makeDeleteProp(fromSource: Boolean, span: Span, lhs: IRId, obj: IRId, index: IRExpr) =
    new IRDeleteProp(makeSpanInfo(fromSource, span), lhs, obj, index)

  def makeObject(fromSource: Boolean, span: Span,
                 lhs: IRId, members: List[IRMember], proto: IRId): IRObject =
    makeObject(fromSource, span, lhs, toJavaList(members), Some(proto))

  def makeObject(fromSource: Boolean, span: Span, lhs: IRId, members: List[IRMember]): IRObject =
    makeObject(fromSource, span, lhs, toJavaList(members), None)

  def makeObject(fromSource: Boolean, span: Span,
                 lhs: IRId, members: JList[IRMember], proto: Option[IRId]): IRObject =
    new IRObject(makeSpanInfo(fromSource, span), lhs, members, proto)

  def makeArray(fromSource: Boolean, span: Span, lhs: IRId, elements: List[Option[IRExpr]]) : IRArray = {
    val new_elements = toJavaList(elements.map(toJavaOption(_)))
    makeArray(fromSource, span, lhs, new_elements)
  }

  def makeArray(fromSource: Boolean, span: Span, lhs: IRId, elements: JList[JOption[IRExpr]]) : IRArray =
    new IRArray(makeSpanInfo(fromSource, span), lhs, elements)

  def makeArrayNumber(fromSource: Boolean, span: Span, lhs: IRId, elements: JList[JDouble]) : IRStmt =
    new IRArrayNumber(makeSpanInfo(fromSource, span), lhs, elements)

  def makeArgs(span: Span, lhs: IRId, elements: List[Option[IRExpr]]) : IRArgs = {
    val new_elements = toJavaList(elements.map(toJavaOption(_)))
    makeArgs(span, lhs, new_elements)
  }

  def makeArgs(span: Span, lhs: IRId, elements: JList[JOption[IRExpr]]) : IRArgs =
    new IRArgs(makeSpanInfo(false, span), lhs, elements)

  def makeLoad(fromSource: Boolean, span: Span, obj: IRId, index: IRExpr) =
    new IRLoad(makeSpanInfo(fromSource, span), obj, index)

  def makeInternalCall(span: Span, lhs: IRId, fun: IRId, arg: IRExpr) : IRInternalCall =
    makeInternalCall(span, lhs, fun, arg, None)

  def makeInternalCall(span: Span, lhs: IRId, fun: IRId, arg1: IRId, arg2: IRId) : IRInternalCall =
    makeInternalCall(span, lhs, fun, arg1, Some(arg2))

  def makeInternalCall(span: Span, lhs: IRId, fun: IRId, arg1: IRExpr, arg2: Option[IRId]) : IRInternalCall =
    new IRInternalCall(makeSpanInfo(false, span), lhs, fun, arg1, toJavaOption(arg2))

  def makeCall(fromSource: Boolean, span: Span, lhs: IRId, fun: IRId, thisB: IRId, args: IRId) : IRCall =
    new IRCall(makeSpanInfo(fromSource, span), lhs, fun, thisB, args)

  def makeNew(fromSource: Boolean, span: Span, lhs: IRId, fun: IRId, args: List[IRId]) : IRNew =
    makeNew(fromSource, span, lhs, fun, toJavaList(args))

  def makeNew(fromSource: Boolean, span: Span, lhs: IRId, fun: IRId, args: JList[IRId]) : IRNew =
    new IRNew(makeSpanInfo(fromSource, span), lhs, fun, args)

  def makeBin(fromSource: Boolean, span: Span, first: IRExpr, op: IROp, second: IRExpr) =
    new IRBin(makeSpanInfo(fromSource, span), first, op, second)

  def makeLoadStmt(fromSource: Boolean, span: Span, lhs: IRId, obj: IRId, index: IRExpr) =
    makeExprStmt(span, lhs, makeLoad(fromSource, span, obj, index))

  def makeExprStmt(span: Span, lhs: IRId, right: IRExpr): IRExprStmt =
    makeExprStmt(span, lhs, right, false)

  def makeExprStmtIgnore(span: Span, lhs: IRId, right: IRExpr): IRExprStmt =
    makeExprStmt(span, lhs, right, true)

  def makeExprStmt(span: Span, lhs: IRId, right: IRExpr, isRef: Boolean): IRExprStmt =
    new IRExprStmt(makeSpanInfo(false, span), lhs, right, isRef)

  def makeFunDecl(fromSource: Boolean, span: Span,
                  name: IRId, params: JList[IRId], body: IRStmt): IRFunDecl =
    makeFunDecl(fromSource, span, name, params, toJavaList(Nil), toJavaList(Nil),
                toJavaList(Nil), toJavaList(List(body)))

  def makeFunDecl(fromSource: Boolean,
                  span: Span, name: IRId, params: JList[IRId], args: JList[IRStmt],
                  fds: JList[IRFunDecl], vds: JList[IRVarStmt], body: JList[IRStmt]): IRFunDecl =
    new IRFunDecl(makeSpanInfo(fromSource, span),
                  makeFunctional(fromSource, name, params, args, fds, vds, body))

  def makeBreak(fromSource: Boolean, span: Span, label: IRId): IRBreak =
    new IRBreak(makeSpanInfo(fromSource, span), label)

  def makeReturn(fromSource: Boolean, span: Span, expr: JOption[IRExpr]) =
    new IRReturn(makeSpanInfo(fromSource, span), expr)

  def makeLabelStmt(fromSource: Boolean, span: Span, label: IRId, stmt: IRStmt): IRLabelStmt =
    new IRLabelStmt(makeSpanInfo(fromSource, span), label, stmt)

  def makeWith(fromSource: Boolean, span: Span, id: IRId, stmt: IRStmt) =
    new IRWith(makeSpanInfo(fromSource, span), id, stmt)

  def makeThrow(fromSource: Boolean, span: Span, expr: IRExpr) =
    new IRThrow(makeSpanInfo(fromSource, span), expr)

  def makeVarStmt(fromSource: Boolean, span: Span, lhs: IRId, fromParam: Boolean): IRVarStmt =
    new IRVarStmt(makeSpanInfo(fromSource, span), lhs, fromParam)

  def makeIf(fromSource: Boolean, span: Span, cond: IRExpr, trueB: IRStmt, falseB: JOption[IRStmt]) =
    new IRIf(makeSpanInfo(fromSource, span), cond, trueB, falseB)

  def makeWhile(fromSource: Boolean, span: Span, cond: IRExpr, body: IRStmt) =
    new IRWhile(makeSpanInfo(fromSource, span), cond, body)

  def makeTry(fromSource: Boolean, span: Span,
              body: IRStmt, name: JOption[IRId], catchB: JOption[IRStmt], finallyB: JOption[IRStmt]) =
    new IRTry(makeSpanInfo(fromSource, span), body, name, catchB, finallyB)

  def makeStore(fromSource: Boolean, span: Span, obj: IRId, index: IRExpr, rhs: IRExpr) =
    new IRStore(makeSpanInfo(fromSource, span), obj, index, rhs)

  def makeSeq(span: Span, first: IRStmt, second: IRStmt): IRSeq =
    makeSeq(span, List(first, second))

  def makeSeq(span: Span): IRSeq =
    makeSeq(span, Nil)

  def makeSeq(span: Span, stmt: IRStmt): IRSeq =
    makeSeq(span, List(stmt))

  def makeSeq(span: Span, stmts: List[IRStmt]): IRSeq =
    new IRSeq(makeSpanInfo(false, span), toJavaList(stmts))

  def makeStmtUnit(span: Span): IRStmtUnit =
    makeStmtUnit(span, Useful.list().asInstanceOf[JList[IRStmt]])

  def makeStmtUnit(span: Span, stmt: IRStmt): IRStmtUnit =
    makeStmtUnit(span, Useful.list(stmt))

  def makeStmtUnit(span: Span, first: IRStmt, second: IRStmt): IRStmtUnit =
    makeStmtUnit(span, Useful.list(first, second))

  def makeStmtUnit(span: Span, stmts: List[IRStmt]): IRStmtUnit =
    makeStmtUnit(span, toJavaList(stmts))

  def makeStmtUnit(span: Span, stmts: JList[IRStmt]): IRStmtUnit =
    new IRStmtUnit(makeSpanInfo(true, span), stmts)

  def makeGetProp(fromSource: Boolean, span: Span, prop: IRId, body: IRStmt): IRGetProp =
    makeGetProp(fromSource, span,
                makeFunctional(fromSource, prop, toJavaList(Nil).asInstanceOf[JList[IRId]], body))

  def makeGetProp(fromSource: Boolean,
                  span: Span, name: IRId, params: JList[IRId], args: JList[IRStmt],
                  fds: JList[IRFunDecl], vds: JList[IRVarStmt],
                  body: JList[IRStmt]): IRGetProp =
    makeGetProp(fromSource, span, makeFunctional(true, name, params, args, fds, vds, body))

  def makeGetProp(fromSource: Boolean, span: Span, functional: IRFunctional): IRGetProp =
    new IRGetProp(makeSpanInfo(fromSource, span), functional)

  def makeSetProp(fromSource: Boolean,
                  span: Span, name: IRId, params: JList[IRId], args: JList[IRStmt],
                  fds: JList[IRFunDecl], vds: JList[IRVarStmt],
                  body: JList[IRStmt]): IRSetProp =
    makeSetProp(fromSource, span, makeFunctional(true, name, params, args, fds, vds, body))

  def makeSetProp(fromSource: Boolean, span: Span, prop: IRId, id: IRId, body: IRStmt): IRSetProp =
    makeSetProp(fromSource, span, makeFunctional(true, prop, toJavaList(List(id)), body))

  def makeSetProp(fromSource: Boolean, span: Span, functional: IRFunctional) =
    new IRSetProp(makeSpanInfo(fromSource, span), functional)

  def makeField(fromSource: Boolean, span: Span, prop: IRId, expr: IRExpr) =
    new IRField(makeSpanInfo(fromSource, span), prop, expr)

  val defaultInfo = new IRSourceInfo(false)
  val trueInfo = new IRSourceInfo(true)
  def makeSourceInfo(fromSource: Boolean) =
    if (fromSource) trueInfo else defaultInfo

  val makeUndef = new IRUndef(defaultInfo)
  val makeNull = new IRNull(defaultInfo)
  val makeNullFromSource = new IRNull(trueInfo)

  def makeBool(fromSource: Boolean, bool: Boolean) =
    new IRBool(makeSourceInfo(fromSource), bool)
  val trueV = makeBool(false, true)
  val falseV = makeBool(false, false)
  val trueVFromSource = makeBool(true, true)
  val falseVFromSource = makeBool(true, false)

  def makeNumber(fromSource: Boolean, text: String, num: Double) =
    new IRNumber(makeSourceInfo(fromSource), text, num)

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
  def makeString(str: String): IRString = makeString(false, str, None)
  def makeString(fromSource: Boolean, str1: String, str2: Option[String]): IRString = str2 match {
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
      if (str1.equals(escaped)) new IRString(makeSourceInfo(fromSource), str1, None)
      else new IRString(makeSourceInfo(fromSource), str1, str2)
  }

  def makeThis(span: Span) =
    new IRThis(makeSpanInfo(true, span))

  // make a user id
  def makeUId(originalName: String, uniqueName: String, isGlobal: Boolean,
              span: Span, isWith: Boolean): IRUserId =
    new IRUserId(makeSpanInfo(true, span), originalName, uniqueName, isGlobal, isWith)

  // make a withRewriter-generated id
  def makeWId(originalName: String, uniqueName: String, isGlobal: Boolean,
              span: Span): IRUserId =
    makeUId(originalName, uniqueName, isGlobal, span, true)

  // make a non-global user id
  def makeNGId(uniqueName: String, span: Span): IRUserId =
    makeUId(uniqueName, uniqueName, false, span, false)

  def makeNGId(originalName: String, uniqueName: String, span: Span): IRUserId =
    makeUId(originalName, uniqueName, false, span, false)

  // make a global user id
  def makeGId(uniqueName: String): IRUserId =
    makeUId(uniqueName, uniqueName, true, dummySpan(uniqueName), false)

  // make a global user id
  def makeGId(originalName: String, uniqueName: String, span: Span): IRUserId =
    makeUId(originalName, uniqueName, true, span, false)

  // make a non-global temporary id
  def makeTId(span: Span, uniqueName: String): IRTmpId =
    makeTId(false, span, uniqueName, uniqueName, false)

  def makeTId(fromSource: Boolean, span: Span, uniqueName: String): IRTmpId =
    makeTId(fromSource, span, uniqueName, uniqueName, false)

  // make a temporary id
  def makeTId(span: Span, uniqueName: String, isGlobal: Boolean): IRTmpId =
    makeTId(false, span, uniqueName, uniqueName, isGlobal)

  def makeTId(span: Span, originalName: String, uniqueName: String, isGlobal: Boolean): IRTmpId =
    makeTId(false, span, originalName, uniqueName, isGlobal)

  def makeTId(fromSource: Boolean, span: Span, originalName: String, uniqueName: String,
              isGlobal: Boolean): IRTmpId =
    new IRTmpId(makeSpanInfo(fromSource, span), originalName, uniqueName, isGlobal)

  def makeOp(name: String, kind: Int = 0) = {
    new IROp(name, if(kind == 0) EJSOp.strToEJSOp(name) else kind)
  }

  def makeInfo(span: Span) =
    new IRSpanInfo(false, span)

  def makeNoOp(span: Span, desc: String) =
    new IRNoOp(makeSpanInfo(false, span), desc)

  val oneV = makeNumber(false, "1", 1)
}
