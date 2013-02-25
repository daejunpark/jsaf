/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.compiler

import _root_.java.util.{List => JList}
import edu.rice.cs.plt.tuple.{Option => JOption}
import kr.ac.kaist.jsaf.exceptions.JSAFError.error
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.interpreter.{InterpreterPredefine => IP, _}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{ IRFactory => IF }
import kr.ac.kaist.jsaf.nodes_util.{ NodeFactory => NF }
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.nodes_util.Coverage
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.ErrorLog
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._
import kr.ac.kaist.jsaf.useful.HasAt
import kr.ac.kaist.jsaf.useful.Useful

/* Translates JavaScript AST to IR. */
class Translator(program: Program, coverage: JOption[Coverage]) extends Walker {
  val debug = false
  var isLocal = false
  var locals : List[String] = List()

  /* Error handling
   * The signal function collects errors during the AST->IR translation.
   * To collect multiple errors,
   * we should return a dummy value after signaling an error.
   */
  val errors: ErrorLog = new ErrorLog
  def signal(msg:String, hasAt:HasAt) = errors.signal(msg, hasAt)
  def signal(hasAt:HasAt, msg:String) = errors.signal(msg, hasAt)
  def signal(error: StaticError) = errors.signal(error)
  def getErrors(): JList[StaticError] = toJavaList(errors.errors)

  /*
   * For code coverage
   */
  def incCov() = toOption(coverage) match {
    case Some(cov) => cov.total = cov.total + 1
    case None =>
  }

  def makeStmtUnit(span: Span): IRStmtUnit = {
    incCov
    IF.makeStmtUnit(span)
  }

  def makeStmtUnit(span: Span, stmt: IRStmt): IRStmtUnit = {
    incCov
    IF.makeStmtUnit(span, stmt)
  }

  def makeStmtUnit(span: Span, first: IRStmt, second: IRStmt): IRStmtUnit = {
    incCov
    IF.makeStmtUnit(span, first, second)
  }

  def makeStmtUnit(span: Span, stmts: List[IRStmt]): IRStmtUnit = {
    incCov
    IF.makeStmtUnit(span, stmts)
  }

  val dummySpan = IF.dummySpan("temp")
  def freshId(span: Span, n: String): IRTmpId =
    IF.makeTId(span, NU.freshName(n), false)
  def freshId(span: Span): IRTmpId = freshId(span, "temp")
  def freshId(): IRTmpId = freshId(dummySpan, "temp")

  val globalName = NU.freshGlobalName("global")
  val global = IF.makeTId(IF.dummySpan("global"), globalName, true)
  val globalSpan = IF.dummySpan(globalName)
  var ignoreId = 0
  def varIgn() = {
    ignoreId += 1
    IF.makeTId(IF.dummySpan("ignore"), NU.ignoreName+ignoreId)
  }
  def getSpan(n: IRAbstractNode) = n.getInfo.getSpan
  def getSpan(n: AbstractNode) = n.getInfo.getSpan
  def getSpan(n: ASTNodeInfo) = n.getSpan

  /* Environment for renaming fresh labels and variables
   * created during the AST->IR translation.
   * Only the following identifiers are bound in the environment:
   *     arguments, val, break, testing, and continue.
   */
  type Env = List[(String, IRId)]
  def addE(env: Env, x: String, xid: IRId) = (x, xid)::env
  def getE(env: Env, name: String): IRId = env.find(p => p._1.equals(name)) match {
    case None =>
      val id = IF.dummyIRId(name)
      signal("Identifier " + name + " is not bound.", id)
      id
    case Some((_, id)) => id
  }

  val thisName     = "this"
  val argName      = "arguments"
  val valName      = "val"
  val newName      = "new"
  val oldName      = "old"
  val condName     = "cond"
  val breakName    = "break"
  val testingName  = "testing"
  val continueName = "continue"

  def funexprId(span: Span, lhs: Option[String]) = {
    val uniq = lhs match { case None => NU.funexprName(span)
                           case Some(name) => name+NU.funexprName(span) }
    NF.makeId(span, uniq, Some(uniq))
  }

  // Whether a given name is locally declared
  def isLocal(n: String): Boolean = locals.contains(n)

  // When we don't know whether a give id is a local variable or not
  def id2ir(env: Env, id: Id): IRId = toOption(id.getUniqueName) match {
    case None =>
      signal("Identifiers should have a unique name after the disambiguation phase:"+id.getText, id)
      IF.dummyIRId(id)
    case Some(n) if id.getText.equals(argName) =>
      if (debug) System.out.println("before getE:id2ir-"+id.getText+" "+id.getUniqueName)
      getE(env, argName)
    case Some(n) if id.isWith =>
      IF.makeWId(id.getText, n, !isLocal(n), getSpan(id))
    case Some(n) if NU.isInternal(id.getText) =>
      IF.makeTId(getSpan(id), id.getText, n, false)
    case Some(n) =>
      IF.makeUId(id.getText, n, !isLocal(n), getSpan(id), false)
  }
  def label2ir(label: Label): IRId = {
    val id = label.getId
    toOption(id.getUniqueName) match {
      case None =>
        signal("Labels should have a unique name after the disambiguation phase:"+id.getText, label)
        IF.dummyIRId(label)
      case Some(n) => IF.makeUId(id.getText, n, false, getSpan(id), false)
    }
  }

  def functional(name: Id, params: List[Id], fds: List[FunDecl],
                 vds: List[VarDecl], body: List[SourceElement], env: Env,
                 fe: Option[IRId]) = {
    val oldIsLocal = isLocal
    val oldLocals = locals
    locals = oldLocals ++ (fe match { case Some(n) => List(n.getUniqueName) case None => Nil }) ++
             params.map(_.getUniqueName.get) ++
             fds.map(_.getFtn.getName.getUniqueName.get) ++
             vds.map(_.getName.getUniqueName.get)
    isLocal = true
    val paramsspan = NU.spanAll(params, getSpan(name))
    var new_arg = freshId(paramsspan, argName)
    if (debug) System.out.println(" arg="+new_arg.getUniqueName)
    var new_env = addE(env, argName, new_arg)
    if (debug) {
      System.out.println("params.. ")
      params.foreach(p => System.out.print(" "+ p.getText))
    }
    if (params.find(_.getText.equals(argName)).isDefined) {
      new_arg = freshId(paramsspan, argName)
      if (debug) System.out.println(" arg="+new_arg.getUniqueName)
    }
    val params_vds = params.map(p => IF.makeVarStmt(false, getSpan(p), id2ir(new_env, p)))
    // x_i = arguments["i"]
    val new_params = params.zipWithIndex.map(p => IF.makeLoadStmt(false, getSpan(p._1),
                                                                  id2ir(new_env, p._1),
                                                                  new_arg,
                                                                  IF.makeString(p._2.toString)))
    val new_fds = fds.map(walkFd(_, new_env))
    new_env = new_fds.foldLeft(new_env)((e, fd) => addE(e, fd.getFtn.getName.getUniqueName, fd.getFtn.getName))
    val new_vds = vds.filterNot(_.getName.getText.equals(argName)).map(walkVd(_, new_env))
    new_env = new_vds.foldLeft(new_env)((e, vd) => addE(e, vd.getLhs.getUniqueName, vd.getLhs))
    val new_name = fe match { case Some(n) => n case None => id2ir(env, name) }
    val new_body = body.map(s => walkStmt(s.asInstanceOf[Stmt], new_env))
    isLocal = oldIsLocal
    locals = oldLocals
    (new_name, List(IF.makeTId(paramsspan, thisName), new_arg),
     // nested functions shadow parameters with the same names
     new_params filterNot (p => fds.map(_.getFtn.getName.getText) contains p.getLhs.getOriginalName),
     new_fds, params_vds++new_vds, new_body)
  }

  def containsUserId(e: IRExpr): Boolean = e match {
    case SIRBin(_, first, _, second) => containsUserId(first) || containsUserId(second)
    case SIRUn(_, _, expr) => containsUserId(expr)
    case SIRLoad(_, _:IRUserId, _) => true
    case SIRLoad(_, _, index) => containsUserId(index)
    case _:IRUserId => true
    case _ => false
  }

  def isIgnore(id: IRId) = id.getUniqueName.startsWith(NU.ignoreName)
  def mkExprS(info: ASTNodeInfo, id: IRId, e: IRExpr): IRExprStmt =
    mkExprS(getSpan(info), id, e)
  def mkExprS(span: Span, id: IRId, e: IRExpr) =
    if (containsUserId(e)) IF.makeExprStmt(span, id, e, true)
    else IF.makeExprStmt(span, id, e)

  def makeListIgnore(info: ASTNodeInfo, ss: List[IRStmt], expr: IRExpr) = expr match {
    case SIRId(_, _, uniqueName, _) if uniqueName.startsWith(NU.ignoreName) => ss
    case _ => ss:+IF.makeExprStmtIgnore(info.getSpan, varIgn, expr)
  }

  def makeList(info: ASTNodeInfo, ss: List[IRStmt], expr: IRExpr, id: IRId) = expr match {
    case SIRId(_, _, uniqueName, _) if uniqueName.equals(id.getUniqueName) => ss
    case _ => ss:+mkExprS(info, id, expr)
  }

  def makeSeq(info: ASTNodeInfo, ss: List[IRStmt], expr: IRExpr, id: IRId) = expr match {
    case SIRId(_, _, uniqueName, _) if uniqueName.equals(id.getUniqueName) => IF.makeSeq(getSpan(info), ss)
    case _ => IF.makeSeq(getSpan(info), ss:+mkExprS(info, id, expr))
  }

  def toObject(span: Span, lhs: IRId, arg: IRExpr) =
    IF.makeInternalCall(span, lhs, IF.makeTId(span, NU.toObjectName, true), arg)

  def toNumber(span: Span, lhs: IRId, id: IRId) =
    IF.makeInternalCall(span, lhs, IF.makeTId(span, NU.freshGlobalName("toNumber"), true), id)

  def getBase(span: Span, lhs: IRId, f: IRId) =
    IF.makeInternalCall(span, lhs, IF.makeTId(span, NU.freshGlobalName("getBase"), true), f)

  def iteratorInit(span: Span, iterator: IRId, obj: IRId) =
    IF.makeInternalCall(span, iterator,
                        IF.makeTId(span, NU.freshGlobalName("iteratorInit"), true), obj)
  def iteratorHasNext(span: Span, cond: IRId, obj: IRId, iterator: IRId) =
    IF.makeInternalCall(span, cond,
                        IF.makeTId(span, NU.freshGlobalName("iteratorHasNext"), true),
                        obj, iterator)
  def iteratorKey(span: Span, key: IRId, obj: IRId, iterator: IRId) =
    IF.makeInternalCall(span, key,
                        IF.makeTId(span, NU.freshGlobalName("iteratorNext"), true),
                        obj, iterator)

  def isObject(span: Span, lhs: IRId, id: IRId) =
    IF.makeInternalCall(span, lhs, IF.makeTId(span, NU.freshGlobalName("isObject"), true), id)

  def isEval(n: Expr) = n match {
    case SVarRef(info, SId(_, _, Some(id), _)) => id.equals("eval")
    case _ => false
  }
  def isPrint(n: Expr) = n match {
    case SVarRef(info, SId(_, id, _, _)) => id.equals(NU.internalPrint)
    case _ => false
  }
  def isPrintIS(n: Expr) = n match {
    case SVarRef(info, SId(_, id, _, _)) => id.equals(NU.internalPrintIS)
    case _ => false
  }
  def isGetTickCount(n: Expr) = n match {
    case SVarRef(info, SId(_, id, _, _)) => id.equals(NU.internalGetTickCount)
    case _ => false
  }
  def isToObject(n: Expr) = n match {
    case SVarRef(info, SId(_, id, _, _)) => id.equals(NU.toObjectName)
    case _ => false
  }

  def containsLhs(res: IRExpr, lhs: Expr, env: Env): Boolean = {
    def getLhs(l: Expr): Option[Expr] = l match {
      case SParenthesized(_, expr) => getLhs(expr)
      case vr:VarRef => Some(vr)
      case SDot(info, obj, member) =>
        getLhs(SBracket(info, obj, NF.makeStringLiteral(getSpan(member), member.getText, "\"")))
      case br:Bracket => Some(br)
      case _ => None
    }
    getLhs(lhs) match {
      case Some(SVarRef(_, id)) =>
        val irid = id2ir(env, id)
        res match {
          case b:IRBin => containsLhs(b.getFirst, lhs, env) || containsLhs(b.getSecond, lhs, env)
          case u:IRUn => containsLhs(u.getExpr, lhs, env)
          case l:IRLoad => l.getObj.getUniqueName.equals(irid.getUniqueName) || containsLhs(l.getIndex, lhs, env)
          case id:IRId => id.getUniqueName.equals(irid.getUniqueName)
          case _ => false
        }
      case Some(_:Bracket) => true
      case _ => false
    }
  }

  /* The main entry function */
  def doit() = NU.simplifyIRWalker.walk(walkProgram(program))

  /*
   * AST2IR_P : Program -> IRRoot
   */
  def walkProgram(pgm: Program): IRRoot = pgm match {
    case SProgram(info, STopLevel(fds, vds, sts), comments) =>
      val env = List()
      IF.makeRoot(true, getSpan(info), fds.map(walkFd(_, env)), vds.map(walkVd(_, env)),
                  sts.map(s => walkStmt(s.asInstanceOf[Stmt], env)))
  }

  /*
   * AST2IR_FD : FunDecl -> Env -> IRFunDecl
   */
  def walkFd(fd: FunDecl, env: Env): IRFunDecl = fd match {
    case SFunDecl(info, SFunctional(fds, vds, body, name, params)) =>
      val (new_name, new_params, args, new_fds, new_vds, new_body) =
          functional(name, params, fds, vds, body, env, None)
      IF.makeFunDecl(true, getSpan(info), new_name, new_params, args,
                     new_fds, new_vds, new_body)
  }

  /*
   * AST2IR_VD : VarDecl -> Env -> IRVarStmt
   */
  def walkVd(vd: VarDecl, env: Env): IRVarStmt = vd match {
    case SVarDecl(info, name, expr) =>
      expr match {
        case None =>
        case _ =>
          signal("Variable declarations should not have any initialization expressions after the disambiguation phase.", vd)
      }
      IF.makeVarStmt(true, getSpan(info), id2ir(env, name))
  }

  /*
   * AST2IR_S : Stmt -> Env -> IRStmt
   */
  def walkStmt(s: Stmt, env: Env): IRStmt = s match {
    case SBlock(info, stmts, true) =>
      IF.makeSeq(info.getSpan, stmts.map(walkStmt(_, env)))

    case SBlock(info, stmts, false) =>
      makeStmtUnit(info.getSpan, stmts.map(walkStmt(_, env)))

    case SStmtUnit(info, stmts) =>
      IF.makeSeq(info.getSpan, stmts.map(walkStmt(_, env)))

    case SEmptyStmt(info) => makeStmtUnit(info.getSpan)

    case SExprStmt(info, expr@SAssignOpApp(_, _, op, _), isInternal) if op.getText.equals("=") =>
      val (ss, _) = walkExpr(expr, env, varIgn)
   // val ss1 = NU.filterIgnore(ss)
      if (isInternal) IF.makeSeq(info.getSpan, ss)
      else makeStmtUnit(info.getSpan, ss)

    case SExprStmt(info, expr, isInternal) =>
      val (ss, r) = walkExpr(expr, env, varIgn)
      if (isInternal) IF.makeSeq(info.getSpan, makeListIgnore(info, ss, r))
      else makeStmtUnit(info.getSpan, makeListIgnore(info, ss, r))

    case SIf(info, SInfixOpApp(_, left, op, right), trueB, falseB) if op.getText.equals("&&") =>
      val span = getSpan(info)
      val leftspan = getSpan(left)
      val new1 = freshId(leftspan, "new1")
      val (ss1, r1) = walkExpr(left, env, new1)
      val (ss2, r2) = walkExpr(right, env, freshId(getSpan(right), "new2"))
      val lab = freshId(span, "label")
      val ifStmt = IF.makeIf(true, span, if(ss2.isEmpty) r1 else new1,
                             IF.makeSeq(span,
                                        ss2:+IF.makeIf(true, span, r2,
                                                       IF.makeSeq(getSpan(trueB), walkStmt(trueB, env),
                                                                  IF.makeBreak(false, span, lab)), None)),
                             None)
      val body = falseB match {
                   case None => ifStmt
                   case Some(stmt) => IF.makeSeq(span, List(ifStmt, walkStmt(stmt, env)))
                 }
      makeStmtUnit(span, IF.makeSeq(span, ss1++(if(!ss2.isEmpty) List(mkExprS(leftspan, new1, r1)) else Nil):+
                                    IF.makeLabelStmt(false, span, lab, body)))

    case SIf(info, SInfixOpApp(_, left, op, right), trueB, falseB) if op.getText.equals("||") =>
      val span = getSpan(info)
      val leftspan = getSpan(left)
      val new1 = freshId(leftspan, "new1")
      val (ss1, r1) = walkExpr(left, env, new1)
      val (ss2, r2) = walkExpr(right, env, freshId(getSpan(right), "new2"))
      val lab1 = freshId(span, "label1")
      val lab2 = freshId(span, "label2")
      val ifStmts = ((IF.makeIf(true, span, if(ss2.isEmpty) r1 else new1,
                                IF.makeBreak(false, span, lab1), None))::ss2):+
                    IF.makeIf(true, span, r2, IF.makeBreak(false, span, lab1), None)
      val body1 = falseB match {
                    case None => IF.makeSeq(span, ifStmts:+IF.makeBreak(false, span, lab2))
                    case Some(stmt) =>
                      IF.makeSeq(span, ifStmts++List(walkStmt(stmt, env),
                                                     IF.makeBreak(false, span, lab2)))
                  }
      val body2 = IF.makeSeq(span, IF.makeLabelStmt(false, span, lab1, body1), walkStmt(trueB, env))
      makeStmtUnit(span, IF.makeSeq(span, ss1++(if(!ss2.isEmpty) List(mkExprS(leftspan, new1, r1)) else Nil):+
                                    IF.makeLabelStmt(false, span, lab2, body2)))

    case SIf(info, SParenthesized(_, expr), trueBranch, falseBranch) =>
      walkStmt(SIf(info, expr, trueBranch, falseBranch), env)

    case SIf(info, cond, trueBranch, falseBranch) =>
      val span = getSpan(info)
      val (ss, r) = walkExpr(cond, env, freshId(getSpan(cond), newName))
      makeStmtUnit(info.getSpan,
                   ss:+IF.makeIf(true, span, r, walkStmt(trueBranch, env),
                                  falseBranch match {
                                    case None => None
                                    case Some(stmt) => Some(walkStmt(stmt, env))
                                  }))

    case SSwitch(info, cond, frontCases, defCase, backCases) =>
      val span = getSpan(info)
      val condVal = freshId(getSpan(cond), valName)
      val breakLabel = freshId(span, breakName)
      val (ss, r) = walkExpr(cond, env, condVal)
      val switchS =
          IF.makeLabelStmt(false, span, breakLabel,
                           IF.makeSeq(span,
                                      makeSeq(info, ss, r, condVal),
                                      walkCase(span, backCases.reverse,
                                               defCase, frontCases.reverse,
                                               addE(addE(env, breakName, breakLabel),
                                                    valName, condVal), List())))
      makeStmtUnit(span, switchS)

    case SDoWhile(info, body, cond) =>
      val span = getSpan(info)
      val newone = freshId(getSpan(cond), "new1")
      val labelName = freshId(span, breakName)
      val cont = freshId(span, continueName)
      val new_env = addE(addE(env, breakName, labelName), continueName, cont)
      val (ss, r) = walkExpr(cond, env, newone)
      val new_body = IF.makeSeq(span,
                                List(IF.makeLabelStmt(false, span, cont, walkStmt(body, new_env)),
                                     IF.makeSeq(span, ss)))
      val stmt = IF.makeSeq(span, List(new_body, IF.makeWhile(true, span, r, new_body)))
      makeStmtUnit(span, IF.makeLabelStmt(false, span, labelName, stmt))

    case SWhile(info, cond, body) =>
      val span = getSpan(info)
      val newone = freshId(getSpan(cond), "new1")
      val labelName = freshId(span, breakName)
      val cont = freshId(span, continueName)
      val new_env = addE(addE(env, breakName, labelName), continueName, cont)
      val (ss, r) = walkExpr(cond, env, newone)
      val ssList = List(IF.makeSeq(span, ss))
      val new_body = IF.makeSeq(span,
                                List(IF.makeLabelStmt(false, span, cont, walkStmt(body, new_env)))++ssList)
      val stmt = IF.makeSeq(span, ssList:+IF.makeWhile(true, span, r, new_body))
      makeStmtUnit(span, IF.makeLabelStmt(false, span, labelName, stmt))

    case SFor(info, init, cond, action, body) =>
      val span = getSpan(info)
      val labelName = freshId(span, breakName)
      val cont = freshId(span, continueName)
      val new_env = addE(addE(env, breakName, labelName), continueName, cont)
      val front = init match { case None => List()
                               case Some(iexpr) =>
                                 val (ss1, r1) = walkExpr(iexpr, env, varIgn)
                                 makeList(info, ss1, r1, varIgn)
                             }
      val back = action match { case None => List()
                                case Some(aexpr) =>
                                  val (ss3, r3) = walkExpr(aexpr, env, varIgn)
                                  makeList(info, ss3, r3, varIgn)
                             }
      val bodyspan = getSpan(body)
      val nbody = IF.makeLabelStmt(false, bodyspan, cont, walkStmt(body, new_env))
      val stmt = cond match {
        case None =>
          IF.makeSeq(span, List(IF.makeSeq(span, front),
                                IF.makeWhile(true, bodyspan, IF.trueV,
                                             IF.makeSeq(bodyspan,
                                                        List(nbody, IF.makeSeq(bodyspan, back))))))
        case Some(cexpr) =>
          val newtwo = freshId(getSpan(cexpr), "new2")
          val (ss2, r2) = walkExpr(cexpr, env, newtwo)
          val new_body = List(nbody, IF.makeSeq(bodyspan, back++ss2))
          IF.makeSeq(span, List(IF.makeSeq(span, front++ss2),
                                IF.makeWhile(true, span, r2, IF.makeSeq(span, new_body))))
      }
      makeStmtUnit(span, IF.makeLabelStmt(false, span, labelName, stmt))

    case SForIn(info, lhs, expr, body) =>
      val span = getSpan(info)
      val labelName = freshId(span, breakName)
      val objspan = getSpan(expr)
      val newone = freshId(objspan, "new1")
      val obj = freshId(objspan, "obj")
      val iterator = freshId(objspan, "iterator")
      val condone = freshId(objspan, "cond1")
      val key = freshId(objspan, "key")
      val cont = freshId(objspan, continueName)
      val new_env = addE(addE(env, breakName, labelName), continueName, cont)
      val iteratorCheck = iteratorHasNext(span, condone, obj, iterator)
      val (ss, r) = walkExpr(expr, env, newone)
      val bodyspan = getSpan(body)
      val new_body = IF.makeSeq(bodyspan,
                                List(iteratorKey(bodyspan, key, obj, iterator))++
                                walkLval(lhs, addE(env, oldName, freshId(getSpan(lhs), oldName)),
                                         List(), key, false)._1++
                                List(IF.makeLabelStmt(false, bodyspan, cont, walkStmt(body, new_env)),
                                     IF.makeSeq(bodyspan, iteratorCheck)))
      val stmt = IF.makeSeq(span,
                            List(IF.makeSeq(span, ss++List(toObject(objspan, obj, r),
                                                           iteratorInit(span, iterator, obj),
                                                           iteratorCheck)),
                                 IF.makeWhile(true, bodyspan, condone, new_body)))
      makeStmtUnit(span, IF.makeLabelStmt(false, span, labelName, stmt))

    case _:ForVar =>
      signal("ForVar should be replaced by Hoister.", s)
      IF.dummyIRStmt(getSpan(s))

    case _:ForVarIn =>
      signal("ForVarIn should be replaced by Hoister.", s)
      IF.dummyIRStmt(getSpan(s))

    case SContinue(info, target) =>
      val span = getSpan(info)
      target match {
        case None =>
          makeStmtUnit(span, IF.makeBreak(true, span, getE(env, continueName)))
        case Some(x) =>
          makeStmtUnit(span, IF.makeBreak(true, span, label2ir(x)))
      }

    case SBreak(info, target) =>
      val span = getSpan(info)
      target match {
        case None => makeStmtUnit(span, IF.makeBreak(true, span, getE(env, breakName)))
        case Some(tg) => makeStmtUnit(span, IF.makeBreak(true, span, label2ir(tg)))
      }

    case r@SReturn(info, expr) =>
      val span = getSpan(info)
      expr match {
        case None =>
          makeStmtUnit(span, IF.makeReturn(true, span, None))
        case Some(expr) =>
          val new1 = freshId(getSpan(expr), "new1")
          val (ss, r) = walkExpr(expr, env, new1)
          makeStmtUnit(span, ss:+IF.makeReturn(true, span, Some(r)))
    }

    case SWith(info, expr, stmt) =>
      val span = getSpan(info)
      val objspan = getSpan(expr)
      val new1 = freshId(objspan, "new1")
      val new2 = freshId(objspan, "new2")
      val (ss, r) = walkExpr(expr, env, new1)
      makeStmtUnit(span,
                   ss++List(toObject(objspan, new2, r),
                            IF.makeWith(true, span, new2, walkStmt(stmt, env))))

    case SLabelStmt(info, label, stmt) =>
      val span = getSpan(info)
      makeStmtUnit(span, IF.makeLabelStmt(true, span, label2ir(label), walkStmt(stmt, env)))

    case SThrow(info, expr) =>
      val span = getSpan(info)
      val new1 = freshId(getSpan(expr), "new1")
      val (ss, r) = walkExpr(expr, env, new1)
      makeStmtUnit(span, ss:+IF.makeThrow(true, span, r))

    case STry(info, body, catchBlock, fin) =>
      val span = getSpan(info)
      val (id, catchBody) = catchBlock match {
                              case Some(SCatch(_, x@SId(i, text, Some(name), _), s)) =>
                                locals = name+:locals
                                val result = (Some(IF.makeUId(text, name, false, getSpan(i), false)),
                                              Some(walkStmt(s, env)))
                                locals = locals.tail
                                result
                              case _ => (None, None)
                            }
      makeStmtUnit(span,
                   IF.makeTry(true, span, walkStmt(body, env), id, catchBody,
                              fin match {
                                case None => None
                                case Some(s) => Some(walkStmt(s, env))}))

    case SDebugger(info) => IF.makeStmtUnit(getSpan(info))

    case _:VarStmt =>
      signal("VarStmt should be replaced by the hoister.", s)
      IF.dummyIRStmt(getSpan(s))

    case SNoOp(info, desc) =>
      IF.makeNoOp(getSpan(info), desc)
  }

  def walkFunExpr(e: Expr, env: Env, res: IRId, lhs: Option[String]) = e match {
    case SFunExpr(info, SFunctional(fds, vds, body, name, params)) =>
      val span = getSpan(info)
      val id = if (name.getText.equals("")) funexprId(span, lhs) else name
      val new_name = IF.makeUId(id.getText, id.getUniqueName.get, false,
                                getSpan(id.getInfo), false)
      val (_, new_params, args, new_fds, new_vds, new_body) =
          functional(name, params, fds, vds, body, env, Some(new_name))
      (List(IF.makeFunExpr(true, span, res, new_name, new_params, args,
                           new_fds, new_vds, new_body)), res)
  }

  /*
   * AST2IR_E : Expr -> Env -> IRId -> List[IRStmt] * IRExpr
   */
  def walkExpr(e: Expr, env: Env, res: IRId): (List[IRStmt], IRExpr) = e match {
    case SExprList(info, Nil) =>
      (Nil, IF.makeUndef)

    case SExprList(info, exprs) =>
      val stmts = exprs.dropRight(1).foldLeft(List[IRStmt]())((l, e) => {
                    val tmp = freshId
                    val (ss, r) = walkExpr(e, env, tmp)
                    l++ss:+(mkExprS(info, tmp, r))})
      val (ss2, r2) = walkExpr(exprs.last, env, res)
      (stmts++ss2, r2)

    case SCond(info, SInfixOpApp(_, left, op, right), trueB, falseB) if op.getText.equals("&&") =>
      val span = getSpan(info)
      val newa = freshId(getSpan(left), "newa")
      val (ssa, ra) = walkExpr(left, env, newa)
      val (ssb, rb) = walkExpr(right, env, freshId(getSpan(right), "newb"))
      val (ss2, r2) = walkExpr(trueB, env, res)
      val (ss3, r3) = walkExpr(falseB, env, res)
      val lab = freshId(span, "label")
      val ifStmt = IF.makeIf(true, span, if(ssb.isEmpty) ra else newa,
                             IF.makeSeq(span, ssb:+
                                              IF.makeIf(true, span, rb,
                                                        IF.makeSeq(span, makeList(info, ss2, r2, res):+
                                                                   IF.makeBreak(false, span, lab)), None)),
                             None)
      val body = IF.makeSeq(span, List(ifStmt)++makeList(info, ss3, r3, res))
      (ssa++(if(!ssb.isEmpty) List(mkExprS(span, newa, ra)) else Nil):+
       IF.makeLabelStmt(false, span, lab, body), res)

    case SCond(info, SInfixOpApp(_, left, op, right), trueB, falseB) if op.getText.equals("||") =>
      val span = getSpan(info)
      val newa = freshId(getSpan(left), "newa")
      val (ssa, ra) = walkExpr(left, env, newa)
      val (ssb, rb) = walkExpr(right, env, freshId(getSpan(right), "newb"))
      val (ss2, r2) = walkExpr(trueB, env, res)
      val (ss3, r3) = walkExpr(falseB, env, res)
      val lab1 = freshId(span, "label1")
      val lab2 = freshId(span, "label2")
      val ifStmts = ((IF.makeIf(true, span, if(ssb.isEmpty) ra else newa,
                                IF.makeBreak(false, span, lab1), None))::ssb):+
                    IF.makeIf(true, span, rb, IF.makeBreak(false, span, lab1), None)
      val body1 = IF.makeSeq(span, ifStmts++makeList(info, ss3, r3, res):+IF.makeBreak(false, span, lab2))
      val body2 = IF.makeSeq(span, IF.makeLabelStmt(false, span, lab1, body1), makeSeq(info, ss2, r2, res))
      (ssa++(if(!ssb.isEmpty) List(mkExprS(span, newa, ra)) else Nil):+
       IF.makeLabelStmt(false, span, lab2, body2), res)

    case SCond(info, SParenthesized(_, expr), trueBranch, falseBranch) =>
      walkExpr(SCond(info, expr, trueBranch, falseBranch), env, res)

    case SCond(info, cond, trueBranch, falseBranch) =>
      val span = getSpan(info)
      val new1 = freshId(getSpan(cond), "new1")
      val (ss1, r1) = walkExpr(cond, env, new1)
      val (ss2, r2) = walkExpr(trueBranch, env, res)
      val (ss3, r3) = walkExpr(falseBranch, env, res)
      (ss1:+IF.makeIf(true, span, r1, makeSeq(info, ss2, r2, res),
                      Some(makeSeq(info, ss3, r3, res))), res)

    case SAssignOpApp(info, lhs, SOp(_, text), right:FunExpr)
         if text.equals("=") && NU.isName(lhs) =>
      val name = NU.getName(lhs)
      val (ss, r) = walkFunExpr(right, env, res, Some(name))
      if (containsLhs(r, lhs, env))
        walkLval(lhs, env, ss, r, false)
      else
        (walkLval(lhs, env, ss, r, false)._1, r)

    case SAssignOpApp(info, lhs, op, right) =>
      val span = getSpan(info)
      if (op.getText.equals("=")) {
        val (ss, r) = walkExpr(right, env, res)
        if (containsLhs(r, lhs, env))
          walkLval(lhs, env, ss, r, false)
        else
          (walkLval(lhs, env, ss, r, false)._1, r)
      } else {
        val y = freshId(getSpan(right), "y")
        val oldVal = freshId(getSpan(lhs), oldName)
        val (ss, r) = walkExpr(right, env, y)
        val bin = IF.makeBin(true, span, oldVal, IF.makeOp(op.getText.substring(0,op.getText.length-1)), r)
        (walkLval(lhs, addE(env, oldName, oldVal), ss, bin, true)._1, bin)
      }

    case SUnaryAssignOpApp(info, lhs, op) =>
      if (op.getText.equals("++") || op.getText.equals("--")) {
        val lhsspan = getSpan(lhs)
        val oldVal = freshId(lhsspan, oldName)
        val newVal = freshId(lhsspan, "new")
        (walkLval(lhs, addE(env, oldName, oldVal), List(toNumber(lhsspan, newVal, oldVal)),
                  IF.makeBin(true, getSpan(info), newVal, IF.makeOp(if (op.getText.equals("++")) "+" else "-"), IF.oneV), true)._1,
         newVal)
      } else {
        signal("Invalid UnaryAssignOpApp operator: "+op.getText, e)
        (List(), IF.dummyIRExpr)
      }

    case SPrefixOpApp(info, op, right) =>
      val span = getSpan(info)
      val rightspan = getSpan(right)
      val opText = op.getText
      if (opText.equals("++") || opText.equals("--")) {
        val oldVal = freshId(rightspan, oldName)
        val newVal = freshId(rightspan, "new")
        val bin = IF.makeBin(true, span, newVal, IF.makeOp(if (opText.equals("++")) "+" else "-"), IF.oneV)
        (walkLval(right, addE(env, oldName, oldVal),
                  List(toNumber(rightspan, newVal, oldVal)),
                  bin, true)._1, bin)
      } else if (opText.equals("delete")) {
        NU.unwrapParen(right) match {
          case SVarRef(_, name) =>
            (List(IF.makeDelete(true, span, res, id2ir(env, name))), res)
          case SDot(sinfo, obj, member) =>
            walkExpr(SPrefixOpApp(info, op,
                                  SBracket(sinfo, obj, NF.makeStringLiteral(getSpan(member), member.getText, "\""))),
                     env, res)
          case SBracket(_, lhs, e) =>
            val objspan = getSpan(lhs)
            val obj1 = freshId(objspan, "obj1")
            val field1 = freshId(getSpan(e), "field1")
            val obj = freshId(objspan, "obj")
            val (ss1, r1) = walkExpr(lhs, env, obj1)
            val (ss2, r2) = walkExpr(e, env, field1)
            ((ss1:+toObject(objspan, obj, r1))++ss2:+
             IF.makeDeleteProp(true, span, res, obj, r2), res)
          case _ =>
            val y = freshId(getSpan(right), "y")
            val (ss, r) = walkExpr(right, env, y)
            (ss:+IF.makeExprStmtIgnore(span, varIgn, r),
             IF.makeTId(span, IP.varTrue, true))
        }
      } else {
        val y = freshId(getSpan(right), "y")
        val (ss, r) = walkExpr(right, env, y)
        (ss, IF.makeUn(true, span, IF.makeOp(opText), r))
      }

    case SInfixOpApp(info, left, op, right) if op.getText.equals("&&") =>
      val span = getSpan(info)
      val y = freshId(getSpan(left), "y")
      val z = freshId(getSpan(right), "z")
      val (ss1, r1) = walkExpr(left, env, y)
      val (ss2, r2) = walkExpr(right, env, z)
      (ss1:+IF.makeIf(true, span, r1,
                      IF.makeSeq(span, ss2++List(mkExprS(info, res, r2))),
                      Some(mkExprS(info, res, r1))),
       res)

    case SInfixOpApp(info, left, op, right) if op.getText.equals("||") =>
      val span = getSpan(info)
      val y = freshId(getSpan(left), "y")
      val z = freshId(getSpan(right), "z")
      val (ss1, r1) = walkExpr(left, env, y)
      val (ss2, r2) = walkExpr(right, env, z)
      (ss1:+IF.makeIf(true, span, r1, mkExprS(info, res, r1),
                      Some(IF.makeSeq(span,
                                      ss2:+mkExprS(info, res, r2)))),
       res)

    case SInfixOpApp(info, left, op, right) =>
      val span = getSpan(info)
      val leftspan = getSpan(left)
      val y = freshId(leftspan, "y")
      val z = freshId(getSpan(right), "z")
      val (ss1, r1) = walkExpr(left, env, y)
      val (ss2, r2) = walkExpr(right, env, z)
      ss2 match {
        case Nil =>
          (ss1, IF.makeBin(true, span, r1, IF.makeOp(op.getText), r2))
        case _ =>
          ((ss1:+mkExprS(leftspan, y, r1))++ss2, IF.makeBin(true, span, y, IF.makeOp(op.getText), r2))
      }

    case SVarRef(info, id) => (List(), id2ir(env, id))

    case SArrayNumberExpr(info, elements) =>
      (List(IF.makeArrayNumber(true, getSpan(info), res, elements)), res)

    case SArrayExpr(info, elements) =>
      val newelems = elements.map(elem => elem match {
          case Some(e) =>
            val tmp = freshId
            Some((tmp, walkExpr(e, env, tmp)))
          case _ => None})
      val stmts = newelems.foldLeft(List[IRStmt]())((l, p) => p match {
                    case None => l
                    case Some((t, (ss,r))) => l++ss:+(mkExprS(info, t, r))
                  })
      (stmts:+IF.makeArray(true, getSpan(info), res, newelems.map(elem => elem match {
                           case Some(e) => Some(e._1)
                           case _ => None})), res)

    case SObjectExpr(info, members) =>
      val new_members = members.map(walkMember(_, env, freshId))
      val stmts = new_members.foldLeft(List[IRStmt]())((l,p) => l++p._1)
      (stmts:+IF.makeObject(true, getSpan(info), res, new_members.map(p => p._2)), res)

    case fe:FunExpr => walkFunExpr(e, env, res, None)

    case SParenthesized(_, expr) => walkExpr(expr, env, res)

    case SDot(info, first, member) =>
      val objspan = getSpan(first)
      val obj1 = freshId(objspan, "obj1")
      val obj = freshId(objspan, "obj")
      val (ss1, r1) = walkExpr(first, env, obj1)
      val str = member.getText
      (ss1:+toObject(objspan, obj, r1),
       IF.makeLoad(true, getSpan(info), obj,
                   IF.makeString(true, NU.unescapeJava(str), Some(str))))

    case SBracket(info, first, SStringLiteral(_, _, str)) =>
      val objspan = getSpan(first)
      val obj1 = freshId(objspan, "obj1")
      val obj = freshId(objspan, "obj")
      val (ss1, r1) = walkExpr(first, env, obj1)
      (ss1:+toObject(objspan, obj, r1),
       IF.makeLoad(true, getSpan(info), obj,
                   IF.makeString(true, NU.unescapeJava(str), Some(str))))

    case SBracket(info, first, index) =>
      val objspan = getSpan(first)
      val obj1 = freshId(objspan, "obj1")
      val field1 = freshId(getSpan(index), "field1")
      val obj = freshId(objspan, "obj")
      val (ss1, r1) = walkExpr(first, env, obj1)
      val (ss2, r2) = walkExpr(index, env, field1)
      ((ss1:+toObject(objspan, obj, r1))++ss2,
       IF.makeLoad(true, getSpan(info), obj, r2))

    case n@SNew(info, SParenthesized(_, e)) if e.isInstanceOf[LHS] =>
      walkExpr(SNew(info, e.asInstanceOf[LHS]), env, res)

    case n@SNew(info, lhs) =>
      val span = getSpan(info)
      val objspan = getSpan(lhs)
      val fun = freshId(objspan, "fun")
      val fun1 = freshId(objspan, "fun1")
      val arg = freshId(objspan, argName)
      val obj = freshId(objspan, "obj")
      val newObj = freshId(objspan, "newObj")
      val cond = freshId(objspan, "cond")
      val proto = freshId(objspan, "proto")
      val (ftn, args) = lhs match {
          case SFunApp(_, f, as) =>
            val newargs = as.map(a => freshId(getSpan(a)))
            val results = as.zipWithIndex.map(a => (newargs.apply(a._2),
                                                    walkExpr(a._1, env, newargs.apply(a._2))))
            (f, results.foldLeft(List[IRStmt]())((l,tp) => l++tp._2._1:+(mkExprS(info, tp._1, tp._2._2))):+
                IF.makeArray(false, span, arg, newargs.map(p => Some(p))))
          case _ => (lhs, List(IF.makeArray(false, span, arg, Nil)))
      }
      val (ssl, rl) = walkExpr(ftn, env, fun1)
      ((ssl:+toObject(objspan, fun, rl))++args++
            List(/*
                  * 15.3.4.5.2
                  proto = fun["prototype"]
                  obj = {[[Prototype]] = proto}
                  newObj = new fun(obj, arg)
                  cond = isObject(newObj)
                  if (cond) then x = newObj else x = obj
                 */
                 IF.makeLoadStmt(false, span, proto, fun, IF.makeString("prototype")),
                 IF.makeObject(false, span, obj, Nil, Some(proto)),
                 IF.makeNew(true, span, newObj, fun, List(obj, arg)),
                 isObject(span, cond, newObj),
                 IF.makeIf(false, span, cond, mkExprS(info, res, newObj),
                           Some(mkExprS(info, res, obj)))), res)

    case SFunApp(info, fun, List(arg)) if (isToObject(fun)) =>
      val (ss, r) = walkExpr(arg, env, freshId(getSpan(arg), "new1"))
      (ss:+toObject(getSpan(fun), res, r), res)

    case SFunApp(info, fun, List(arg)) if (isEval(fun)) =>
      val newone = freshId(getSpan(arg), "new1")
      val (ss, r) = walkExpr(arg, env, newone)
      (ss:+IF.makeEval(true, getSpan(info), res, r), res)

    // _<>_print()
    case SFunApp(info, fun, List(arg)) if (isPrint(fun)) =>
      val newone = freshId(getSpan(arg), "new1")
      val (ss, r) = walkExpr(arg, env, newone)
      (ss:+IF.makeInternalCall(getSpan(info), res,
                               IF.makeGId(NU.freshGlobalName("print")), r), res)

    // _<>_printIS()
    case SFunApp(info, fun, Nil) if (isPrintIS(fun)) =>
      (List(IF.makeInternalCall(getSpan(info), res,
                                IF.makeGId(NU.freshGlobalName("printIS")), res)), res)

    // _<>_getTickCount()
    case SFunApp(info, fun, Nil) if (isGetTickCount(fun)) =>
      (List(IF.makeInternalCall(getSpan(info), res,
                                IF.makeGId(NU.freshGlobalName("getTickCount")), res)), res)

    case SFunApp(info, SParenthesized(_,e), args) if e.isInstanceOf[LHS] =>
      walkExpr(SFunApp(info, e.asInstanceOf[LHS], args), env, res)

    case SFunApp(info, SDot(i,obj,member), args) =>
      walkExpr(SFunApp(info,
                       SBracket(i, obj, NF.makeStringLiteral(getSpan(member), member.getText, "\"")),
                       args), env, res)

    case SFunApp(info, v@SVarRef(_, fid), args) =>
      val fspan = getSpan(v)
      val obj = freshId(fspan, "obj")
      val argsspan = NU.spanAll(args, fspan)
      val arg = freshId(argsspan, argName)
      val fun = freshId(fspan, "fun")
      val fir = id2ir(env, fid)
      val newargs = args.map(_ => freshId)
      val results = args.zipWithIndex.map(a => (newargs.apply(a._2),
                                                walkExpr(a._1, env, newargs.apply(a._2))))
      (List(toObject(fspan,obj,fir))++
       results.foldLeft(List[IRStmt]())((l,tp) => l++tp._2._1:+(mkExprS(info,tp._1,tp._2._2)))++
       List(IF.makeArgs(argsspan, arg, newargs.map(p => Some(p))),
            getBase(fspan, fun, fir),
            IF.makeCall(true, getSpan(info), res, obj, fun, arg)), res)

    case SFunApp(info, b@SBracket(_,first,index), args) =>
      val firstspan = getSpan(first)
      val obj1 = freshId(firstspan, "obj1")
      val field1 = freshId(getSpan(index), "field1")
      val obj = freshId(firstspan, "obj")
      val fun = freshId(firstspan, "fun")
      val argsspan = NU.spanAll(args, getSpan(b))
      val arg = freshId(argsspan, argName)
      val (ssl, rl) = walkExpr(first, env, obj1)
      val (ssr, rr) = walkExpr(index, env, field1)
      val newargs = args.map(_ => freshId)
      val results = args.zipWithIndex.map(a => (newargs.apply(a._2),
                                                walkExpr(a._1, env, newargs.apply(a._2))))
      (((ssl:+toObject(firstspan, obj, rl))++ssr)++
       results.foldLeft(List[IRStmt]())((l,tp) => l++tp._2._1:+(mkExprS(info, tp._1, tp._2._2)))++
       List(IF.makeArgs(argsspan, arg, newargs.map(p => Some(p))),
            toObject(firstspan, fun, IF.makeLoad(true, firstspan, obj, rr)),
            IF.makeCall(true, getSpan(info), res, fun, obj, arg)), res)

    case SFunApp(info, fun, args) =>
      val fspan = getSpan(fun)
      val obj1 = freshId(fspan, "obj1")
      val obj = freshId(fspan, "obj")
      val argsspan = NU.spanAll(args, fspan)
      val arg = freshId(argsspan, argName)
      val (ss, r) = walkExpr(fun, env, obj1)
      val newargs = args.map(_ => freshId)
      val results = args.zipWithIndex.map(a => (newargs.apply(a._2),
                                                walkExpr(a._1, env, newargs.apply(a._2))))
      ((ss:+toObject(fspan, obj, r))++
       results.foldLeft(List[IRStmt]())((l,tp) => l++tp._2._1:+(mkExprS(info, tp._1, tp._2._2)))++
       List(IF.makeArgs(argsspan, arg, newargs.map(p => Some(p))),
            IF.makeCall(true, getSpan(info), res, obj, global, arg)), res)

    case t:This => (List(), IF.makeThis(getSpan(t)))

    case n:Null => (List(), IF.makeNullFromSource)

    case SBool(info, isBool) =>
      (List(), if (isBool) IF.trueVFromSource else IF.falseVFromSource)

    case SDoubleLiteral(info, text, num) =>
      (List(), IF.makeNumber(true, text, num))

    case SIntLiteral(info, intVal, radix) =>
      (List(), IF.makeNumber(true, intVal.toString, intVal.doubleValue))

    case SStringLiteral(info, _, str) =>
      (List(), IF.makeString(true, NU.unescapeJava(str), Some(str)))

    case SRegularExpression(info, body, flags) =>
      val regexp = "RegExp"
      walkExpr(SNew(info, SFunApp(info, SVarRef(info, SId(info, regexp, Some(regexp), false)),
                                  List(SStringLiteral(info, "\"", body),
                                       SStringLiteral(info, "\"", flags)))),
               env, res)
  }

  def prop2ir(prop: Property) = prop match {
    case SPropId(info, id) => IF.makeNGId(id.getText, getSpan(info))
    case SPropStr(info, str) => IF.makeTId(true, getSpan(info), str)
    case SPropNum(info, SDoubleLiteral(_,t,_)) => IF.makeTId(true, getSpan(info), t)
    case SPropNum(info, SIntLiteral(_,i,_)) => IF.makeTId(true, getSpan(info), i.toString)
  }
  /*
   * AST2IR_M : Member -> Env -> IRId -> List[IRStmt] * IRMember
   */
  def walkMember(m: Member, env: Env, res: IRId) = {
    val span = getSpan(m.getInfo)
    m match {
      case SField(_, prop, expr) =>
        val (ss, r) = walkExpr(expr, env, res)
        (ss, IF.makeField(true, span, prop2ir(prop), r))
      case SGetProp(_, prop, SFunctional(fds, vds, body, name, params)) =>
        val (new_name, new_params, args, new_fds, new_vds, new_body) =
            functional(NU.prop2Id(prop), params, fds, vds, body, env, None)
        (List(),
         IF.makeGetProp(true, span, new_name, new_params, args, new_fds, new_vds, new_body))
      case SSetProp(_, prop, SFunctional(fds, vds, body, name, params)) =>
        val (new_name, new_params, args, new_fds, new_vds, new_body) =
            functional(NU.prop2Id(prop), params, fds, vds, body, env, None)
        (List(),
         IF.makeSetProp(true, span, new_name, new_params, args, new_fds, new_vds, new_body))
    }
  }

  type CaseEnv = List[(Option[Expr], IRId)]
  def addCE(env: CaseEnv, x: Option[Expr], xid: IRId):CaseEnv = (x, xid)::env
  def addRightCE(env: CaseEnv, xid: IRId):CaseEnv = env ++ List((None, xid)).asInstanceOf[CaseEnv]
  /*
   * AST2IR_CC : List[Case] * Option[List[Stmt]] * List[Case] -> Env -> List[Option[Expr] * IRId] -> IRStmt
   */
  def walkCase(switchSpan: Span, backCases:List[Case], defCase:Option[List[Stmt]],
               frontCases:List[Case], env: Env, caseEnv: CaseEnv): IRStmt =
    (backCases, defCase, frontCases) match {
      case (head::tail, _, _) =>
        val SCase(info, condExpr, body) = head
        // span is currently set to the head statement of the default case
        val span = getSpan(info)
        val newLabel = freshId(span, "Case2Label")
        IF.makeSeq(span,
                   IF.makeLabelStmt(false, span, newLabel,
                                    walkCase(switchSpan, tail, defCase, frontCases, env,
                                             addCE(caseEnv, Some(condExpr), newLabel)).asInstanceOf[IRStmt]),
                   walkStmt(body, env))
      case (Nil, Some(stmt), _) =>
        // span is currently set to the default cases
        val span = if (stmt.isEmpty) switchSpan else getSpan(stmt.head)
        val newLabel = freshId(NU.spanAll(stmt, span), "default")
        IF.makeSeq(span,
                   IF.makeLabelStmt(false, span, newLabel,
                                    walkCase(switchSpan, List(), None, frontCases, env,
                                             addRightCE(caseEnv, newLabel))),
                   if (stmt.isEmpty) IF.dummyIRStmt(span)
                   else IF.makeSeq(span, stmt.map(walkStmt(_, env))))
      case (Nil, None, head::tail) =>
        val SCase(info, condExpr, body) = head
        // span is currently set to the head statement of the default case
        val span = getSpan(info)
        val newLabel = freshId(getSpan(head), "Case1Label")
        IF.makeSeq(span,
                   IF.makeLabelStmt(false, span, newLabel,
                                    walkCase(switchSpan, List(), None, tail, env,
                                             addCE(caseEnv, Some(condExpr), newLabel))),
                   walkStmt(body, env))
      case (Nil, None, Nil) =>
        IF.makeSeq(switchSpan,
                   walkScond(switchSpan, caseEnv, env),
                   IF.makeBreak(false, switchSpan, getE(env, breakName)))
    }

  /*
   * AST2IR_SC : List[Option[Expr] * IRId] -> Env -> IRStmt
   */
  def walkScond(switchSpan: Span, caseEnv:CaseEnv, env:Env): IRStmt =
    caseEnv match {
      case (Some(expr), label)::tail =>
        val span = getSpan(expr) // span is a position of the expression
        val cond = freshId(getSpan(expr), condName)
        val (ss, r) = walkExpr(expr, env, cond)
        val comp = IF.makeBin(false, span, getE(env, valName), IF.makeOp("==="), r)
        IF.makeSeq(span, ss:+IF.makeIf(true, span, comp, IF.makeBreak(false, span, label),
                                       Some(walkScond(switchSpan, tail, env))))
      case List((None, label)) => IF.makeBreak(false, switchSpan, label)
      case _ => IF.makeSeq(switchSpan)
    }

  /*
   * AST2IR_LVAL : Expr -> Env -> List[IRStmt] -> IRExpr -> boolean -> List[IRStmt] * IRExpr
   */
  def walkLval(lhs:Expr, env:Env, stmts:List[IRStmt], e:IRExpr,
               keepOld:Boolean): (List[IRStmt], IRExpr) = lhs match {
    case SParenthesized(_, expr) =>
      walkLval(expr, env, stmts, e, keepOld)
    case SVarRef(info, id) =>
      if (debug) System.out.println("  id="+id.getText+" "+id.getUniqueName)
      val irid = id2ir(env, id)
      if (debug) System.out.println("VarRef: irid="+irid.getUniqueName)
      if (keepOld)
        (List(mkExprS(info, getE(env, oldName), irid))++stmts:+mkExprS(info, irid, e), irid)
      else
        (stmts:+mkExprS(info, irid, e), irid)
    case SDot(info, obj, member) =>
      walkLval(SBracket(info, obj, NF.makeStringLiteral(getSpan(member), member.getText, "\"")),
               env, stmts, e, keepOld)
    case SBracket(info, first, index) =>
      val span = getSpan(info)
      val firstspan = getSpan(first)
      val obj1   = freshId(firstspan, "obj1")
      val field1 = freshId(getSpan(index), "field1")
      val obj    = freshId(firstspan, "obj")
      val (ss1, r1) = walkExpr(first, env, obj1)
      val (ss2, r2) = walkExpr(index, env, field1)
      val front = (ss1:+toObject(firstspan, obj, r1))++ss2
      val back = stmts:+IF.makeStore(true, span, obj, r2, e)
      if (keepOld)
        ((front:+IF.makeLoadStmt(true, span, getE(env, oldName), obj, r2))++back,
         IF.makeLoad(true, span, obj, r2))
      else (front++back, IF.makeLoad(true, span, obj, r2))
    case _ =>
      signal("ReferenceError!", lhs)
      (List(), IF.dummyIRExpr)
  }
}
