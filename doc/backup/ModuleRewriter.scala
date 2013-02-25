/*******************************************************************************
    Copyright (c) 2012, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.compiler

import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU, _}
import kr.ac.kaist.jsaf.scala_src.nodes._
import java.util.HashMap

class ModuleRewriter(program: Program) extends Walker {
  val debug = false

  type Identifier = String

  type Path = List[Identifier]
  abstract class QualName
  case class QualIntName(p: Path, x: Identifier) extends QualName {
    override def toString(): String = {
      var s: String = ""
      for (t <- p) s += t+"."
      s + "("+x+")"
    }
  }
  case class QualExtName(p: Path, x: Identifier) extends QualName {
    override def toString(): String = {
      var s: String = ""
      for (t <- p) s += t+"."
      s + x
    }
  }
  class ExpQualName(p: Path) {
    override def toString(): String = p+".*"
  }

  abstract class Type
  case object Var extends Type {
    override def toString(): String = "var"
  }
  case object Module extends Type {
    override def toString(): String = "module"
  }

  abstract class Scope
  case object Epsilon extends Scope {
    override def toString(): String = "epsilon"
  }
  case object Local extends Scope {
    override def toString(): String = "local"
  }
  case class Export(qe: QualExtName) extends Scope {
    override def toString(): String = "export "+qe
  }

  abstract class Reference
  case object BottomRef extends Reference {
    override def toString(): String = "⊥"
  }
  case class Ref(t: Type, name: QualName) extends Reference {
    override def toString(): String = t+" "+name
  }

  abstract class ExpReference
  case object BottomExpRef extends ExpReference {
    override def toString(): String = "⊥"
  }
  case object TopExpRef extends ExpReference {
    override def toString(): String = "⊤"
  }

  type Env = (HashMap[QualName, List[(Reference, Scope)]], HashMap[ExpQualName, ExpReference])

  abstract class EnvChain
  case object EmptyChain extends EnvChain
  case class DeclChain(outer: EnvChain, inner: Env) extends EnvChain
  case class ObjChain(outer: EnvChain, inner: Identifier) extends EnvChain

  def doit(): Program = {
    // TODO:
    val env: Env = makeEnv(program, Epsilon)
    val p: Program = api()
    val p1: Program = instantiate(program, env, Nil).asInstanceOf[Program]
    val p2: Program = initialize(program, env, Nil).asInstanceOf[Program]
    val p3: Program = finalize(program, env, Nil).asInstanceOf[Program]
    val p4: Program = desugarR(program, env, Nil).asInstanceOf[Program]
    p.getBody.getStmts.addAll(p1.getBody.getStmts)
    p.getBody.getStmts.addAll(p2.getBody.getStmts)
    p.getBody.getStmts.addAll(p3.getBody.getStmts)
    p.getBody.getStmts.addAll(p4.getBody.getStmts)
    p
  }

  def api(): Program = {
    val window = SVarStmt(defInfo, List(SVarDecl(defInfo, SId(defInfo, "window", None, false), Some(SThis(defInfo)))))
    SProgram(defInfo, STopLevel(Nil, Nil, List(window)))
    SProgram(defInfo, STopLevel(Nil, Nil, Nil))
  }

  def printEnv(env: Env): Unit = {
    System.err.print("Env = {")
    val it1 = env._1.entrySet.iterator
    while (it1.hasNext) {
      val pair = it1.next
      val (x, rs) = (pair.getKey, pair.getValue)
      var i = 0
      while (i < rs.length) {
        val (r, s) = rs(i)
        System.err.print("("+x+","+r+" "+s+")")
        i += 1
        if (i < rs.length) System.err.print(", ")
      }
      if (it1.hasNext) System.err.print(", ")
    }
    System.err.print("}+{")
    val it2 = env._2.entrySet.iterator
    while (it2.hasNext) {
      val pair = it2.next
      val (x, r) = (pair.getKey, pair.getValue)
      System.err.print("("+x+","+r+")")
      if (it2.hasNext) System.err.print(", ")
    }
    System.err.println("}")
  }

  def makeEnv(node: Any, scope: Scope): Env = {
    val env = (new HashMap[QualName, List[(Reference, Scope)]], new HashMap[ExpQualName, ExpReference])
    scope match {
      case Epsilon => while (updateEnvM(env, node, Nil)) 42
      case Local => while (updateEnvF(env, node)) 42
    }
    if (debug) printEnv(env)
    env
  }

  def lookup(env: Env, p0: Path, p: Path): Reference = p match {
    case List(x) =>
      val (r, s) = lookupEnv(env, QualIntName(p0, x))
      r match {
        case BottomRef => lookupBottom(env, p0, p)
        case _ => r
      }
    case m :: rest =>
      val (r, s) = lookupEnv(env, QualIntName(p0, m))
      r match {
        case Ref(Module, QualIntName(p1, m1)) => lookup_(env, p1 ++ List(m1), rest)
        case _ => lookupBottom(env, p0, p)
      }
    // Unreachable
    case Nil => BottomRef
  }

  def lookupBottom(env: Env, p0: Path, p: Path): Reference = {
    if (p0.length >= 1) lookup(env, p0.dropRight(1), p)
    else BottomRef
  }

  def lookup_(env: Env, p0: Path, p: Path): Reference = p match {
    case List(x) =>
      val (r, s) = lookupEnv(env, QualExtName(p0, x))
      r
    case m :: rest =>
      val (r, s) = lookupEnv(env, QualExtName(p0, m))
      r match {
        case Ref(Module, QualIntName(p1, m1)) => lookup_(env, p1 ++ List(m1), rest)
        case _ => BottomRef
      }
    // Unreachable
    case Nil => BottomRef
  }

  def lookupEnv(env: Env, key: QualName): (Reference, Scope) = {
    val rs = env._1.get(key)
    var (r: Reference, s: Scope) = (BottomRef, Epsilon)
    if (rs != null) {
      for ((tr, ts) <- rs) {
        if (!tr.equals(BottomRef)) r = tr
        if (!ts.equals(Epsilon)) s = ts
      }
    }
    (r, s)
  }

  def updateEnv(env: Env, key: QualName, value: (Reference, Scope)): Boolean = env._1.get(key) match {
    case l: List[(Reference, Scope)] =>
      var exist: Boolean = false
      for (v <- l) exist |= v equals value
      if (!exist) env._1.put(key, value :: l)
      !exist
    case _ =>
      env._1.put(key, List(value))
      true
  }

  // TODO:
  def updateEnvF(env: Env, node: Any): Boolean = node match {
    case SProgram(info, STopLevel(fds, vds, stmts)) =>
      updateEnvF(env, stmts)
    case SBlock(info, stmts, _) =>
      updateEnvF(env, stmts)
    case SVarStmt(info, vds: List[VarDecl]) =>
      var updated: Boolean = false
      for (vd <- vds) {
        val x: Identifier = vd.getName.getText
        updated |= updateEnv(env, QualExtName(Nil, x), (Ref(Var, QualExtName(Nil, x)), Local))
      }
      updated
    case SForVar(info, vds: List[VarDecl], cond, action, body) =>
      var updated: Boolean = false
      for (vd <- vds) {
        val x: Identifier = vd.getName.getText
        updated |= updateEnv(env, QualExtName(Nil, x), (Ref(Var, QualExtName(Nil, x)), Local))
      }
      updated | updateEnvF(env, body)
    case SForVarIn(info, vd: VarDecl, expr, body) =>
      var updated: Boolean = false
      val x: Identifier = vd.getName.getText
      updated |= updateEnv(env, QualExtName(Nil, x), (Ref(Var, QualExtName(Nil, x)), Local))
      updated | updateEnvF(env, body)
    case SFunDecl(info, name, f) =>
      val x: Identifier = name.getText
      updateEnv(env, QualExtName(Nil, x), (Ref(Var, QualExtName(Nil, x)), Local))
    // TODO: Stmt<S_1, ..., S_k>
    case stmts: List[_] =>
      var updated: Boolean = false
      for (stmt <- stmts) updated |= updateEnvF(env, stmt)
      updated
    case _ => false
  }

  def updateEnvM(env: Env, node: Any, p: Path): Boolean = node match {
    case SProgram(info, STopLevel(fds, vds, stmts)) =>
      updateEnvM(env, stmts, p)
    case SBlock(info, stmts, _) =>
      updateEnvM(env, stmts, p)
    case SVarStmt(info, vds) => p match {
      case Nil =>
        var updated: Boolean = false
        for (vd <- vds) {
          val x: Identifier = vd.getName.getText
          updated |= updateEnv(env, QualIntName(p, x), (Ref(Var, QualIntName(p, x)), Export(QualExtName(p, x))))
          updated |= updateEnv(env, QualExtName(p, x), (Ref(Var, QualExtName(p, x)), Epsilon))
        }
        updated
      case _ =>
        var updated: Boolean = false
        for (vd <- vds) {
          val x: Identifier = vd.getName.getText
          updated |= updateEnv(env, QualIntName(p, x), (Ref(Var, QualIntName(p, x)), Epsilon))
        }
        updated
    }
    case SModDef(info, name, STopLevel(fds, vds, stmts)) =>
      var updated: Boolean = false
      val x: Identifier = name.getText
      updated |= updateEnv(env, QualIntName(p, x), (Ref(Module, QualIntName(p, x)), Epsilon))
      updated | updateEnvM(env, stmts, p ++ List(x))
    case SModAlias(info, name, SQualifiedName(info_, names)) => lookup(env, p, names.map(x => x.getText)) match {
      case Ref(Module, varphi) =>
        var updated: Boolean = false
        val x: Identifier = name.getText
        updated |= updateEnv(env, QualIntName(p, x), (BottomRef, Epsilon))
        updated | updateEnv(env, QualIntName(p, x), (Ref(Module, varphi), Epsilon))
      case _ =>
        val x: Identifier = name.getText
        updateEnv(env, QualIntName(p, x), (BottomRef, Epsilon))
    }
    case SModImpDecl(info, SQualifiedName(info_, names)) => lookup(env, p, names.map(x => x.getText)) match {
      case Ref(_, varphi) =>
        var updated: Boolean = false
        val x: Identifier = names.last.getText
        updated |= updateEnv(env, QualIntName(p, x), (BottomRef, Epsilon))
        updated | updateEnv(env, QualIntName(p, x), (Ref(Var, varphi), Epsilon))
      case _ =>
        val x: Identifier = names.last.getText
        updateEnv(env, QualIntName(p, x), (BottomRef, Epsilon))
    }
    case SModImpAlias(info, SQualifiedName(info_, names), alias) => lookup(env, p, names.map(x => x.getText)) match {
      case Ref(_, varphi) =>
        var updated: Boolean = false
        val x: Identifier = alias.getText
        updated |= updateEnv(env, QualIntName(p, x), (BottomRef, Epsilon))
        updated | updateEnv(env, QualIntName(p, x), (Ref(Var, varphi), Epsilon))
      case _ =>
        val x: Identifier = alias.getText
        updateEnv(env, QualIntName(p, x), (BottomRef, Epsilon))
    }
    // TODO: ModImpAll
    case SModExpVarStmt(info, vds) =>
      var updated: Boolean = false
      for (vd <- vds) {
        val x: Identifier = vd.getName.getText
        updated |= updateEnv(env, QualIntName(p, x), (Ref(Var, QualIntName(p, x)), Export(QualExtName(p, x))))
        updated |= updateEnv(env, QualExtName(p, x), (Ref(Var, QualExtName(p, x)), Epsilon))
      }
      updated
    case SModExpFunDecl(info, fd) =>
      var updated: Boolean = false
      val x: Identifier = fd.getName.getText
      updated |= updateEnv(env, QualIntName(p, x), (Ref(Var, QualIntName(p, x)), Export(QualExtName(p, x))))
      updated | updateEnv(env, QualExtName(p, x), (Ref(Var, QualExtName(p, x)), Epsilon))
    case SModExpModDecl(info, md) => md match {
      case SModDef(info, name, STopLevel(fds, vds, stmts)) =>
        var updated: Boolean = false
        val x: Identifier = name.getText
        updated |= updateEnv(env, QualIntName(p, x), (Ref(Module, QualIntName(p, x)), Epsilon))
        updated |= updateEnv(env, QualExtName(p, x), (Ref(Module, QualIntName(p, x)), Epsilon))
        updated | updateEnvM(env, stmts, p ++ List(x))
      case SModAlias(info, name, SQualifiedName(info_, names)) => lookup(env, p, names.map(x => x.getText)) match {
        case r@Ref(Module, _) =>
          // TODO:
          var updated: Boolean = false
          val x: Identifier = name.getText
          updated |= updateEnv(env, QualIntName(p, x), (BottomRef, Epsilon))
          updated |= updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
          updated |= updateEnv(env, QualIntName(p, x), (r, Epsilon))
          updated | updateEnv(env, QualExtName(p, x), (r, Epsilon))
        case _ =>
          var updated: Boolean = false
          val x: Identifier = name.getText
          updated |= updateEnv(env, QualIntName(p, x), (BottomRef, Epsilon))
          updated | updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
      }
    }
    case SModExpId(info, name) =>
      val x = name.getText
      val (r, s) = lookupEnv(env, QualIntName(p, x))
      r match {
        case Ref(Var, QualIntName(p1, x1)) =>
          var updated: Boolean = false
          updated |= updateEnv(env, QualIntName(p, x), (BottomRef, Export(QualExtName(p, x))))
          updated |= updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
          updated | updateEnv(env, QualExtName(p, x), (Ref(Var, QualExtName(p, x)), Epsilon))
        case Ref(Var, QualExtName(p1, x1)) =>
          var updated: Boolean = false
          updated |= updateEnv(env, QualIntName(p, x), (BottomRef, Export(QualExtName(p, x))))
          updated |= updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
          updated | updateEnv(env, QualExtName(p, x), (Ref(Var, QualExtName(p1, x1)), Epsilon))
        case _ =>
          var updated: Boolean = false
          updated |= updateEnv(env, QualIntName(p, x), (BottomRef, Export(QualExtName(p, x))))
          updated | updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
      }
    case SModExpIdAlias(info, alias, name) =>
      val (x, x0) = (alias.getText, name.getText)
      val (r, s) = lookupEnv(env, QualIntName(p, x0))
      r match {
        case Ref(Var, QualIntName(p1, x1)) =>
          var updated: Boolean = false
          updated |= updateEnv(env, QualIntName(p, x0), (BottomRef, Export(QualExtName(p, x))))
          updated |= updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
          updated | updateEnv(env, QualExtName(p, x), (Ref(Var, QualExtName(p, x)), Epsilon))
        case Ref(Var, QualExtName(p1, x1)) =>
          var updated: Boolean = false
          updated |= updateEnv(env, QualIntName(p, x0), (BottomRef, Export(QualExtName(p, x))))
          updated |= updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
          updated | updateEnv(env, QualExtName(p, x), (Ref(Var, QualExtName(p1, x1)), Epsilon))
        case _ =>
          var updated: Boolean = false
          updated |= updateEnv(env, QualIntName(p, x0), (BottomRef, Export(QualExtName(p, x))))
          updated | updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
      }
    case SModExpQIdAlias(info, alias, SQualifiedName(info_, names)) => lookup(env, p, names.map(x => x.getText)) match {
      case r@Ref(Var, _) =>
        var updated: Boolean = false
        val x: Identifier = alias.getText
        updated |= updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
        updated | updateEnv(env, QualExtName(p, x), (r, Epsilon))
      case _ =>
        val x: Identifier = alias.getText
        updateEnv(env, QualExtName(p, x), (BottomRef, Epsilon))
    }
    // TODO:
    case SForVar(info, vds: List[VarDecl], cond, action, body) =>
      for (vd <- vds) {
        val x: Identifier = vd.getName.getText
        updateEnv(env, QualIntName(p, x), (Ref(Var, QualIntName(p, x)), Epsilon))
      }
      updateEnvM(env, body, p)
    // TODO:
    case SForVarIn(info, vd: VarDecl, expr, body) =>
      var updated: Boolean = false
      val x: Identifier = vd.getName.getText
      updated |= updateEnv(env, QualIntName(p, x), (Ref(Var, QualIntName(p, x)), Epsilon))
      updated | updateEnvM(env, body, p)
    // TODO:
    case SFunDecl(info, name, f) =>
      val x: Identifier = name.getText
      updateEnv(env, QualIntName(p, x), (Ref(Var, QualIntName(p, x)), Epsilon))
    // TODO: Stmt<S_1, ..., S_k>
    case stmts: List[_] =>
      var updated: Boolean = false
      for (stmt <- stmts) updated |= updateEnvM(env, stmt, p)
      updated
    case _ => false 
  }

  val defInfo = program.getInfo

  def instantiate(env: Env, p: Path, m: String): ObjectExpr = {
    val p1: Path = p ++ List(m)
    var members: List[Member] = Nil
    // TODO:
    val it = env._1.entrySet.iterator
    while (it.hasNext) {
      val pair = it.next
      val (x, rs) = (pair.getKey, pair.getValue)
      x match {
        case QualExtName(p2, m2) if p1 equals p2 =>
          // TODO: null instead of undefined
          var v: Expr = SNull(defInfo)
          for ((r, s) <- rs) {
            v = r match {
              case Ref(Module, QualIntName(p3, m3)) => //if (p1 equals p3) && (m equals m3) =>
                instantiate(env, p3, m3)
              case _ =>
                SNull(defInfo)
            }
          }
          members ::= SField(defInfo, SPropStr(defInfo, m2), v)
        case _ =>
      }
    }
    if (debug) {
      System.err.print("Module ")
      for (x <- p) System.err.print(x+".")
      System.err.print(m+" exported")
      var first = true
      for (m <- members) {
        if (first) first = false
        else System.err.print(",")
        System.err.print(" "+m.asInstanceOf[Field].getProp.asInstanceOf[PropStr].getStr)
      }
      System.err.println(".")
    }
    SObjectExpr(defInfo, members)
  }

  def instantiate(node: Any, env: Env, p: Path): Any = node match {
    case SProgram(info, body) =>
      SProgram(info, instantiate(body, env, p).asInstanceOf[TopLevel])
    case STopLevel(fds, vds, stmts) =>
      STopLevel(fds, vds, instantiate(stmts, env, p).asInstanceOf[List[SourceElement]])
    case SModDef(info, name, body) =>
      val obj = instantiate(env, p, name.getText)
      SVarStmt(info, List(SVarDecl(defInfo, name, Some(obj))))
    case xs: List[_] =>
      for (x <- xs;
           y = instantiate(x, env, p);
           if !y.isInstanceOf[EmptyStmt])
      yield y
    case _ => SEmptyStmt(defInfo)
  }

  def canonical(p: Path): LHS = {
    var lhs: LHS = null // SVarRef(defInfo, SId(defInfo, "window", None, false))
    var first: Boolean = true
    for (x <- p) {
      if (first) {
        lhs = SVarRef(defInfo, SId(defInfo, x, None, false))
        first = false
      } else {
        lhs = SDot(defInfo, lhs, SId(defInfo, x, None, false))
      }
    }
    lhs
  }

  def imports(env: Env, p: Path): (List[Id], List[Expr]) = {
    var (params: List[Id], args: List[Expr]) = (Nil, Nil)
    val it = env._1.entrySet.iterator
    // TODO:
    while (it.hasNext) {
      val pair = it.next
      val (x, rs) = (pair.getKey, pair.getValue)
      x match {
        case QualIntName(p1, m1) if p equals p1 =>
          for ((r, s) <- rs) {
            r match {
              case Ref(Var, QualExtName(p2, m2)) if !((p1 equals p2) && (m1 equals m2)) =>
                params ::= SId(defInfo, m1, None, false)
                args ::= canonical(p2 ++ List(m2))
              case _ =>
            }
          }
        case _ =>
      }
    }
    (params, args)
  }

  def getExcept(stmts: List[SourceElement]): List[Identifier] = {
    val fds: List[FunDecl] = for (x <- stmts if x.isInstanceOf[FunDecl]) yield x.asInstanceOf[FunDecl]
    val mds: List[ModDef] = for (x <- stmts if x.isInstanceOf[ModDef]) yield x.asInstanceOf[ModDef]
    val emds: List[ModDef] = for (x <- stmts;
                                  y = x match {
                                    case SModExpModDecl(info, md: ModDef) => Some(md)
                                    case _ => None
                                  };
                                  if y.isDefined)
                             yield y.get
    fds.map(x => x.getName.getText) ++ (mds ++ emds).map(x => x.getName.getText)
  }

  def getProps(env: Env, p: Path, except: List[Identifier]): (List[Identifier], List[Identifier]) = {
    var (priProp: List[Identifier], pubProp: List[Identifier]) = (Nil, Nil)
    // TODO:
    val it = env._1.entrySet.iterator
    while (it.hasNext) {
      val pair = it.next
      val (x, rs) = (pair.getKey, pair.getValue)
      x match {
        case QualIntName(p1, m1) if p equals p1 =>
          if (!env._1.containsKey(QualExtName(p1, m1)) && !except.contains(m1))
            priProp ::= m1
        case QualExtName(p1, m1) if p equals p1 =>
          if (!except.contains(m1)) {
            for ((r, s) <- rs) {
              r match {
                case Ref(Var, QualExtName(p2, m2)) if !((p1 equals p2) && (m1 equals m2)) =>
                  pubProp ::= m1
                case _ =>
              }
            }
          }
        case _ =>
      }
    }
    (priProp, pubProp)
  }

  def updateFun(node: Any, env: Env, p: Path,
                priProp: List[Identifier], pubProp: List[Identifier]): Any = node match {
    case _: ModImport => SEmptyStmt(defInfo)
    case SModExpId(info, name) =>
      val lhs = SDot(info, SThis(info), name)
      val expr = SAssignOpApp(info, lhs, SOp(info, "="), SVarRef(info, name))
      SExprStmt(info, expr, false)
    case _: ModExport => SEmptyStmt(defInfo)
    case _: FunDecl => SEmptyStmt(defInfo)
    case xs: List[_] =>
      val body = for (x <- xs;
                      y = updateFun(x, env, p, priProp, pubProp);
                      if !y.isInstanceOf[EmptyStmt])
                 yield y.asInstanceOf[SourceElement]
      val params = (priProp ++ pubProp).map(x => SId(defInfo, x, None, false))
      val ftn = SFunExpr(defInfo, None, SFunctional(Nil, Nil, body, params))
      val lhs = SDot(defInfo, SThis(defInfo), SId(defInfo, "업데이트", None, false))
      List(SExprStmt(defInfo, SAssignOpApp(defInfo, lhs, SOp(defInfo, "="), ftn), false))
    case _ => node
  }

  def constructor(node: Any, env: Env, p: Path): Any = node match {
    case STopLevel(_, _, stmts) =>
      val fds: List[FunDecl] = for (x <- stmts if x.isInstanceOf[FunDecl]) yield x.asInstanceOf[FunDecl]
      val (priProp, pubProp) = getProps(env, p, getExcept(stmts))
      val vds = List(SVarStmt(defInfo, priProp.map(x => SVarDecl(defInfo, SId(defInfo, x, None, false), None))))
      val const = constructor(stmts, env, p).asInstanceOf[List[SourceElement]]
      val update = updateFun(stmts, env, p, priProp, pubProp).asInstanceOf[List[SourceElement]]
      SFunExpr(defInfo, None, SFunctional(Nil, Nil, fds ++ vds ++ const ++ update, Nil))
    case SModDef(info, name, body) => initialize(node, env, p)
    case SModExpModDecl(info, md) => initialize(node, env, p)
    case SModExpFunDecl(info, SFunDecl(info_, name, ftn)) =>
      val lhs = SDot(info, SThis(info), name)
      val expr = SAssignOpApp(info, lhs, SOp(info, "="), SFunExpr(info_, None, ftn))
      SExprStmt(info, expr, false)
    case xs: List[_] =>
      for (x <- xs;
           y = constructor(x, env, p);
           if !y.isInstanceOf[EmptyStmt])
      yield y
    case _ => SEmptyStmt(defInfo)
  }

  def initialize(node: Any, env: Env, p: Path): Any = node match {
    case SProgram(info, body) =>
      SProgram(info, initialize(body, env, p).asInstanceOf[TopLevel])
    case STopLevel(fds, vds, stmts) =>
      STopLevel(fds, vds, initialize(stmts, env, p).asInstanceOf[List[SourceElement]])
    case SModDef(info, name, body) =>
      val (params, args) = imports(env, p ++ List(name.getText))
      val const = constructor(body, env, p ++ List(name.getText)).asInstanceOf[FunExpr]
      val lhs = SVarRef(info, name)
      val expr = SAssignOpApp(info, lhs, SOp(info, "="), SNew(info, SFunApp(info, const, Nil)))
      SExprStmt(info, expr, false)
    case SModExpModDecl(info, SModDef(info_, name, body)) =>
      val (params, args) = imports(env, p ++ List(name.getText))
      val const = constructor(body, env, p ++ List(name.getText)).asInstanceOf[FunExpr]
      val lhs = SDot(info, SThis(info), name)
      val expr = SAssignOpApp(info, lhs, SOp(info, "="), SNew(info, SFunApp(info, const, Nil)))
      SExprStmt(info, expr, false)
    case xs: List[_] =>
      for (x <- xs;
           y = initialize(x, env, p);
           if !y.isInstanceOf[EmptyStmt])
      yield y
    case _ => SEmptyStmt(defInfo)
  }

  def updateArgs(env: Env, p: Path, priProp: List[Identifier], pubProp: List[Identifier]): List[Expr] = {
    val pri = priProp.map(prop => {
      val rs = env._1.get(QualIntName(p, prop))
      var arg: Expr = null
      for ((r, s) <- rs) {
        r match {
          case Ref(Var, QualExtName(p1, m1)) =>
            arg = canonical(p1 ++ List(m1))
          case _ =>
        }
      }
      arg
    })
    val pub = pubProp.map(prop => {
      val rs = env._1.get(QualExtName(p, prop))
      var arg: Expr = null
      for ((r, s) <- rs) {
        r match {
          case Ref(Var, QualExtName(p1, m1)) if !((p equals p1) && (prop equals m1)) =>
            arg = canonical(p1 ++ List(m1))
          case _ =>
        }
      }
      arg
    })
    pri ++ pub
  }

  def finalize(node: Any, env: Env, p: Path): Any = node match {
    case SProgram(info, body) =>
      SProgram(info, finalize(body, env, p).asInstanceOf[TopLevel])
    case STopLevel(fds, vds, stmts) =>
      STopLevel(fds, vds, finalize(stmts, env, p).asInstanceOf[List[SourceElement]])
    case SModDef(info, name, STopLevel(fds, vds, stmts)) =>
      val p1 = p ++ List(name.getText)
      val pf = p1 ++ List("업데이트")
      val (priProp, pubProp) = getProps(env, p1, getExcept(stmts))
      val args = updateArgs(env, p1, priProp, pubProp)
      val s1 = SExprStmt(defInfo, SFunApp(defInfo, canonical(pf), args), false)
      val s2 = SExprStmt(defInfo, SPrefixOpApp(defInfo, SOp(defInfo, "delete"), canonical(pf)), false)
      val s3 = SExprStmt(defInfo, SFunApp(defInfo, SDot(defInfo, SVarRef(defInfo, SId(defInfo, "Object", None, false)), SId(defInfo, "seal", None, false)), List(canonical(p1))), false)
      val s4 = finalize(stmts, env, p1).asInstanceOf[List[Stmt]]
      // TODO: Implement Object.seal
      SBlock(defInfo, List(s1, s2, s3) ++ s4, false)
      SBlock(defInfo, List(s1, s2) ++ s4, false)
    case SModExpModDecl(info, md: ModDef) => finalize(md, env, p)
    case xs: List[_] =>
      for (x <- xs;
           y = finalize(x, env, p);
           if !y.isInstanceOf[EmptyStmt])
      yield y
    case _ => SEmptyStmt(defInfo)
  }

  def desugarR(node: Any, env: Env, p: Path): Any = node match {
    case SProgram(info, body) =>
      SProgram(info, desugarR(body, env, p).asInstanceOf[TopLevel])
    case STopLevel(fds, vds, stmts) =>
      STopLevel(fds, vds, desugarR(stmts, env, p).asInstanceOf[List[SourceElement]])
    case SModDef(info, name, body) =>
      val expr = SVarRef(name.getInfo, name)
      val top = desugarR(body, env, p ++ List(name.getText)).asInstanceOf[TopLevel]
      val STopLevel(_, _, stmts) = top
      SWith(info, expr, SBlock(info, stmts.asInstanceOf[List[Stmt]], false))
      SEmptyStmt(info)
    case SModExpModDecl(info, md) => desugarR(md, env, p)
    case SModAlias(info, name, alias) => SEmptyStmt(info)
    case _: ModImport => SEmptyStmt(defInfo)
    case SModExpVarStmt(info, vds) => SVarStmt(info, vds)
    case _: ModExport => SEmptyStmt(defInfo)
    case xs: List[_] => for (x <- xs) yield desugarR(x, env, p)
    case _ => node
  }
}
