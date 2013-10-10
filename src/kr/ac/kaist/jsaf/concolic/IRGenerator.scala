/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic

import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{IRFactory => IF}
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF}
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.{NodeRelation => NR}
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.ErrorLog
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.useful.HasAt

object IRGenerator {
  val errors: ErrorLog = new ErrorLog
  def signal(msg: String, hasAt: HasAt) = errors.signal(msg, hasAt)

  val dummySpan = IF.dummySpan("forConcolic")
  def freshId(ast: ASTNode, span: Span, n: String): IRTmpId = 
    IF.makeTId(ast, span, NU.freshName(n), false)
  def freshId(span: Span, n: String): IRTmpId =
    IF.makeTId(span, NU.freshName(n))
  def freshId(): IRTmpId = freshId(dummySpan, "temp")

  val globalName = NU.freshGlobalName("global")
  val global = IF.makeTId(IF.dummySpan("global"), globalName, true)
  def getSpan(n: AbstractNode) = n.getInfo.getSpan
  def getSpan(n: ASTSpanInfo) = n.getSpan

  type Env = List[(String, IRId)]

  val argName = "arguments"

  def funexprId(span: Span, lhs: Option[String]) = {
    val uniq = lhs match { case None => NU.funexprName(span)
                           case Some(name) => name+NU.funexprName(span) }
    NF.makeId(span, uniq, Some(uniq))
  }

  def containsUserId(e: IRExpr): Boolean = e match {
    case SIRBin(_, first, _, second) => containsUserId(first) || containsUserId(second)
    case SIRUn(_, _, expr) => containsUserId(expr)
    case SIRLoad(_, _:IRUserId, _) => true
    case SIRLoad(_, _, index) => containsUserId(index)
    case _:IRUserId => true
    case _ => false
  }

  def mkExprS(ast: ASTNode, id: IRId, e: IRExpr): IRExprStmt =
    mkExprS(ast, getSpan(ast.getInfo), id, e)
  def mkExprS(ast: ASTNode, span: Span, id: IRId, e: IRExpr) =
    if (containsUserId(e)) IF.makeExprStmt(ast, span, id, e, true)
    else IF.makeExprStmt(ast, span, id, e)

  def toObject(ast: ASTNode, span: Span, lhs: IRId, arg: IRExpr) =
    IF.makeInternalCall(ast, span, lhs, IF.makeTId(ast, span, NU.toObjectName, true), arg)

  def literalToIR(e: Expr, env: Env, res: IRId): (List[IRStmt], IRExpr) = e match {
    case n:Null => (List(), IF.makeNull(n))

    case b@SBool(info, isBool) =>
      (List(), if (isBool) IF.makeBool(true, b, true) else IF.makeBool(true, b, false))

    case SDoubleLiteral(info, text, num) =>
      (List(), IF.makeNumber(true, e, text, num))

    case SIntLiteral(info, intVal, radix) =>
      (List(), IF.makeNumber(true, e, intVal.toString, intVal.doubleValue))

    case SStringLiteral(info, _, str) =>
      (List(), IF.makeString(true, e, NU.unescapeJava(str)))
  }

  def funExprToIR(e: Expr, env: Env, res: IRId, lhs: Option[String]):(List[IRStmt], IRExpr) = e match {
    case SFunExpr(info, SFunctional(fds, vds, body, name, params)) =>
      val span = getSpan(info)
      val id = if (name.getText.equals("")) funexprId(span, lhs) else name
      val new_name = IF.makeUId(id.getText, id.getUniqueName.get, false,
                                e, getSpan(id.getInfo), false)
      
      for (k <- NR.ir2astMap.keySet) {
        k match {
          case SIRFunctional(info, name, params, args, fds, vds, body) =>
            if (id.getText == name.getUniqueName) 
              return (List(IF.makeFunExpr(true, e, span, res, name, params, args, fds, vds, body)), res)
          case _ =>
        }
      }

      signal("IRFunctional sholud exists", e)
      return (List(IF.dummyIRStmt(e, span)), res)
  }
  def funAppToIR(e: FunApp, env: Env, res: IRId): IRStmt = e match {
    case SFunApp(info, fun, args) =>
      val fspan = getSpan(fun)
      val obj1 = freshId(fun, fspan, "obj1")
      val obj = freshId(fun, fspan, "obj")
      val argsspan = NU.spanAll(args, fspan)
      val arg = freshId(e, argsspan, argName)
      val (ss, r) = funExprToIR(fun, env, obj1, None)
      val newargs = args.map(_ => freshId)
      val results = args.zipWithIndex.map(a => (newargs.apply(a._2),
                                                literalToIR(a._1, env, newargs.apply(a._2))))
      new IRStmtUnit(IF.makeSpanInfo(true, dummySpan), toJavaList((ss:+toObject(fun, fspan, obj, r))++
       results.foldLeft(List[IRStmt]())((l,tp) => l++tp._2._1:+(mkExprS(e, tp._1, tp._2._2)))++
       List(IF.makeArgs(e, argsspan, arg, newargs.map(p => Some(p))),
            IF.makeCall(true, e, getSpan(info), res, obj, global, arg))))
  }
}
