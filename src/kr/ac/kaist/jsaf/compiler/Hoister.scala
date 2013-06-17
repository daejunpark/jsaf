/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.compiler

import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.ShellParameters
import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.useful.HasAt
import kr.ac.kaist.jsaf.Shell

class Hoister(program: Program) extends Walker {
  /* Error handling
   * The signal function collects errors during the Hoister phase.
   * To collect multiple errors,
   * we should return a dummy value after signaling an error.
   */
  var errors = List[BugInfo]()
  def signal(span: Span, bugKind: Int, arg1: String, arg2: String): Unit =
    errors ++= List(new BugInfo(span, bugKind, arg1, arg2))
  def getErrors(): JList[BugInfo] = toJavaList(errors)
  // Variable declarations in for statements
  var fvds = List[VarDecl]()
  var inFor = false
  def contains(vds: List[VarDecl], vd: VarDecl) = {
    val name = vd.getName.getText
    val span = vd.getInfo.getSpan    
    vds.find(v => v.getName.getText.equals(name) && v.getInfo.getSpan.equals(span)).isDefined
  }

  // Utility functions
  def assignOp(info: ASTNodeInfo) = SOp(info, "=")
  def assignS(i: ASTNodeInfo, name: Id, expr: Expr) =
    SExprStmt(i, walk(SAssignOpApp(i, SVarRef(i, name), assignOp(i), expr)).asInstanceOf[Expr], true)
  def hoistVds(vds: List[VarDecl]) =
    vds.foldRight(List[Stmt]())((vd, res) => vd match {
                                case SVarDecl(_,_,None) => res
                                case SVarDecl(i,n,Some(e)) => List(assignS(i,n,e))++res
                               })
  // Get the declared variable and function names and the names on lhs of assignments
  // in the current lexical scope
  class hoistWalker(node: Any, isTopLevel: Boolean) extends Walker {
    var varDecls = List[VarDecl]()
    var funDecls = List[FunDecl]()
    var varNames = List[(Span, String)]()
    def doit() = { walk(node); (varDecls, funDecls, varNames) }
    override def walk(node: Any) = node match {
      case fd:FunDecl => funDecls ++= List(fd); fd
      case vd@SVarDecl(i, n, _) =>
        if (isTopLevel && Shell.pred != null && Shell.pred.contains(n.getText)) vd
        else if (isTopLevel && Shell.pred == null && (new Predefined(new ShellParameters())).contains(n.getText)) vd
        else {
          val vds = List(SVarDecl(i, n, None))
          varDecls ++= vds
          if (inFor) fvds ++= vds
          vd
        }
      case SForVar(info, vars, cond, action, body) =>
        val oldInFor = inFor
        inFor = true
        val vds = walk(vars).asInstanceOf[List[VarDecl]]
        inFor = oldInFor
        SForVar(info, vds,
                walk(cond).asInstanceOf[Option[Expr]],
                walk(action).asInstanceOf[Option[Expr]],
                walk(body).asInstanceOf[Stmt])
      case SForVarIn(info, vd, expr, body) =>
        val oldInFor = inFor
        inFor = true
        val walkedVd = walk(vd).asInstanceOf[VarDecl]
        inFor = oldInFor
        SForVarIn(info, walkedVd,
                  walk(expr).asInstanceOf[Expr], walk(body).asInstanceOf[Stmt])
      case fe:FunExpr => fe
      case gp:GetProp => gp
      case sp:SetProp => sp
      case ae@SAssignOpApp(_, SVarRef(i, name), _, _) =>
        varNames ++= List((i.getSpan, name.getText))
        ae
      case _ => super.walk(node)
    }
  }

  // Remove function declarations in the current lexical scope
  object rmFunDeclWalker extends Walker {
    override def walk(node: Any) = node match {
      case fd:FunDecl => SEmptyStmt(fd.getInfo)
      case fe:FunExpr => fe
      case gp:GetProp => gp
      case sp:SetProp => sp
      case _ => super.walk(node)
    }
  }

  /* The main entry function */
  def doit() = NU.simplifyWalker.walk(walk(program).asInstanceOf[Program])

  def checkUseStrictDirective(body: List[SourceElement]) = body match {
    case (e@SExprStmt(_,SStringLiteral(_,_,txt),_))::_ =>
      val s = NU.unescapeJava(txt)
      if (!NU.removeEscapeSeqLineCont(s).equals(s))
        throw new StaticError("A Use Strict Directive may not contain an EscapeSequence or LineContinuation.",
                              Some(e))
    case _ =>
  }

  def isInVd(vd: VarDecl, ds: List[VarDecl], vars: List[(Span, String)]) = {
    val name = vd.getName.getText
    if (!contains(fvds, vd))
      vars.find(p => name.equals(p._2) &&
                     p._1.getBegin.getLine <= vd.getInfo.getSpan.getBegin.getLine &&
                     p._1.getBegin.column <= vd.getInfo.getSpan.getBegin.column) match {
           case Some((span, n)) =>
             signal(span, ShadowedVarByVar, name, vd.getInfo.getSpan.toStringWithoutFiles)
           case _ =>
      }
    ds.exists(d => d.getName.getText.equals(name))
  }
  def isInFd(fd: FunDecl, ds: List[FunDecl]) = {
    val name = fd.getFtn.getName.getText
    ds.exists(d => if (d.getFtn.getName.getText.equals(name)) {
                     signal(d.getInfo.getSpan, ShadowedFuncByFunc, name, fd.getInfo.getSpan.toStringWithoutFiles)
                     true
                   } else false)
  }
  def isVdInFd(vd: VarDecl, ds: List[FunDecl]) = {
    val name = vd.getName.getText
    ds.exists(d => if (d.getFtn.getName.getText.equals(name)) {
                     signal(vd.getInfo.getSpan, ShadowedVarByFunc, name, d.getInfo.getSpan.toStringWithoutFiles)
                     true
                   } else false)
  }
  def fdShadowParam(fd: FunDecl, params: List[Id]) = {
    val name = fd.getFtn.getName.getText
    signal(params.find(p => p.getText.equals(name)).get.getInfo.getSpan, ShadowedParamByFunc, name,
           fd.getInfo.getSpan.toStringWithoutFiles)
  }
  def vdShadowParam(vd: VarDecl, params: List[Id]) = {
    val name = vd.getName.getText
    signal(params.find(p => p.getText.equals(name)).get.getInfo.getSpan, ShadowedVarByParam, name,
           vd.getInfo.getSpan.toStringWithoutFiles)
  }
  def hoist(body: List[SourceElement], isTopLevel: Boolean, params: List[Id]) = {
    val param_names = params.map(_.getText)
    val (vdss, fdss, varss) = body.map(s => new hoistWalker(s, isTopLevel).doit).unzip3
    // hoisted variable declarations
    val vds = vdss.flatten.asInstanceOf[List[VarDecl]]
    // hoisted function declarations
    val fds = fdss.flatten.map(walk).asInstanceOf[List[FunDecl]]
    val vars = varss.flatten.asInstanceOf[List[(Span, String)]]
    // duplicated variable declarations removed
    // first-come wins
    val vdsUniq = vds.foldLeft(List[VarDecl]())((res, vd) =>
                               if (isInVd(vd, res, vars)) res
                               else res++List(vd))
    // duplicated function declarations removed
    // last-come wins
    val fdsUniq = fds.foldRight(List[FunDecl]())((fd, res) =>
                                if (isInFd(fd, res)) res
                                else List(fd)++res)
    // variables with the same names with functions removed
    // function wins
    val vdsUniq2 = vdsUniq.foldRight(List[VarDecl]())((vd, res) =>
                                     if (isVdInFd(vd, fdsUniq)) res
                                     else List(vd)++res)
    // functions shadowing parameters
    fdsUniq.filter(fd => param_names contains fd.getFtn.getName.getText).foreach(fdShadowParam(_,params))
    // variables shadowing parameters
    vdsUniq2.filter(vd => param_names contains vd.getName.getText).foreach(vdShadowParam(_,params))

    (fdsUniq, vdsUniq2,
     walk(rmFunDeclWalker.walk(body)).asInstanceOf[List[SourceElement]])
  }

  override def walk(node: Any): Any = node match {
    case SProgram(info, STopLevel(Nil, Nil, program), comments) =>
      checkUseStrictDirective(program)
      val (fds, vds, new_program) = hoist(program, true, Nil)
      SProgram(info, STopLevel(fds, vds, new_program), comments)
    case pgm:Program =>
      throw new StaticError("Program before the hoisting phase should not have hoisted declarations.",
                            Some(pgm))
      pgm
    case SFunDecl(info, SFunctional(Nil, Nil, body, name, params)) =>
      checkUseStrictDirective(body)
      val (fds, vds, new_body) = hoist(body, false, params)
      SFunDecl(info, SFunctional(fds, vds, new_body, name, params))
    case fd:FunDecl =>
      throw new StaticError("Function declarations before the hoisting phase should not have hoisted declarations.",
                            Some(fd))
      fd
    case SFunExpr(info, SFunctional(Nil, Nil, body, name, params)) =>
      checkUseStrictDirective(body)
      val (fds, vds, new_body) = hoist(body, false, params)
      SFunExpr(info, SFunctional(fds, vds, new_body, name, params))
    case fe:FunExpr =>
      throw new StaticError("Function expressions before the hoisting phase should not have hoisted declarations.",
                            Some(fe))
      fe
    case SGetProp(info, prop, SFunctional(Nil, Nil, body, name, params)) =>
      checkUseStrictDirective(body)
      val (fds, vds, new_body) = hoist(body, false, params)
      SGetProp(info, prop, SFunctional(fds, vds, new_body, name, params))
    case gp:GetProp =>
      throw new StaticError("Function expressions before the hoisting phase should not have hoisted declarations.",
                            Some(gp))
      gp
    case SSetProp(info, prop, SFunctional(Nil, Nil, body, name, params)) =>
      checkUseStrictDirective(body)
      val (fds, vds, new_body) = hoist(body, false, params)
      SSetProp(info, prop, SFunctional(fds, vds, new_body, name, params))
    case sp:SetProp =>
      throw new StaticError("Function expressions before the hoisting phase should not have hoisted declarations.",
                            Some(sp))
      sp
    case SVarStmt(info, vds) => SStmtUnit(info, hoistVds(vds))
    case SForVar(info, vars, cond, action, body) =>
      val new_info = NU.spanInfoAll(vars)
      SBlock(new_info,
             List(SStmtUnit(new_info, hoistVds(vars)),
                 walk(SFor(info, None, cond, action, body)).asInstanceOf[Stmt]),
             false)
    case SForVarIn(info, SVarDecl(i,n,None), expr, body) =>
      walk(SForIn(info, SVarRef(i,n), expr, body))
    case SForVarIn(info, SVarDecl(i,n,Some(e)), expr, body) =>
      SBlock(info,
             List(SStmtUnit(info, List(walk(assignS(i,n,e)).asInstanceOf[Stmt])),
                  walk(SForIn(info, SVarRef(i,n), expr, body)).asInstanceOf[Stmt]),
             false)
    case SLabelStmt(info, label, SForVar(i, vars, cond, action, body)) =>
      val new_info = NU.spanInfoAll(vars)
      SBlock(new_info,
             List(SStmtUnit(info, hoistVds(vars)),
                  SLabelStmt(info, label,
                             walk(SFor(i, None, cond, action, body)).asInstanceOf[Stmt])),
             false)
    case SLabelStmt(info, label, SForVarIn(finfo, SVarDecl(i,n,Some(e)), expr, body)) =>
      SBlock(info, List(SStmtUnit(info, List(walk(assignS(i,n,e)).asInstanceOf[Stmt])),
                        SLabelStmt(info, label,
                                   walk(SForIn(info, SVarRef(i,n), expr, body)).asInstanceOf[Stmt])),
             false)

    case SLabelStmt(info, label, stmt) =>
      SLabelStmt(info, label, walk(stmt).asInstanceOf[Stmt])

    case _ => super.walk(node)
  }
}
