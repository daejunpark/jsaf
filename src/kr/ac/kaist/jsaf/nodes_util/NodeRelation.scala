/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.nodes_util

import scala.collection.mutable.{HashMap => MHashMap}
import scala.collection.mutable.{HashSet => MHashSet}
import scala.collection.mutable.{ListBuffer => MList}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.cfg.{Block => CFGCmdBlock}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes.{Node => ASTRootNode}
import kr.ac.kaist.jsaf.scala_src.nodes._

object NodeRelation {
  ////////////////////////////////////////////////////////////////////////////////
  // Custom HashMap
  ////////////////////////////////////////////////////////////////////////////////
  class MHashMapEx[KeyType, ValueType] extends MHashMap[KeyType, ValueType] {
    // Custom hash function uses the uid as a hash value
    override def elemHashCode(key: KeyType): Int = {
      key match {
        case ast: AbstractNode => ast.getUID.toInt
        case ast: ScopeBody => ast.getUID.toInt
        case ir: IRAbstractNode => ir.getUID.toInt
        case ir: IRExpr => ir.getUID.toInt
        case ir: IROp => ir.getUID.toInt
        case ir: IRInfoNode => ir.getUID.toInt
        case _ => key.##
      }
    }

    // Key comparison
    override def elemEquals(key1: KeyType, key2: KeyType): Boolean = {
      val key1Value = key1 match {
        case key1: AbstractNode => key1.getUID
        case key1: ScopeBody => key1.getUID
        case _ => return key1 == key2
      }
      val key2Value = key2 match {
        case key2: AbstractNode => key2.getUID
        case key2: ScopeBody => key2.getUID
        case _ => return key1 == key2
      }
      key1Value == key2Value
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Root nodes
  ////////////////////////////////////////////////////////////////////////////////
  var astRoot:                                  Program = null
  var irRoot:                                   IRRoot = null
  var cfgRoot:                                  CFGNode = null

  ////////////////////////////////////////////////////////////////////////////////
  // Parent & Children relation
  ////////////////////////////////////////////////////////////////////////////////
  // AST's parent & children
  type ASTParentMap =                           MHashMapEx[ASTRootNode, ASTRootNode]
  type ASTChildMap =                            MHashMapEx[ASTRootNode, MList[ASTRootNode]]
  var astParentMap:                             ASTParentMap = null
  var astChildMap:                              ASTChildMap = null

  // IR's parent & children
  type IRParentMap =                            MHashMapEx[IRNode, IRNode]
  type IRChildMap =                             MHashMapEx[IRNode, MList[IRNode]]
  var irParentMap:                              IRParentMap = null
  var irChildMap:                               IRChildMap = null

  // CFG's parent & children
  type CFGParentMap =                           MHashMap[CFGNode, CFGNode]
  type CFGChildMap =                            MHashMap[CFGNode, MList[CFGNode]]
  var cfgParentMap:                             CFGParentMap = null
  var cfgChildMap:                              CFGChildMap = null

  ////////////////////////////////////////////////////////////////////////////////
  // AST <-> IR <-> CFG relation
  ////////////////////////////////////////////////////////////////////////////////
  // For AST -> (Set[IR], Set[CFG])
  type AST2IRMap =                              MHashMapEx[ASTRootNode, MList[IRNode]]
  type AST2CFGMap =                             MHashMapEx[ASTRootNode, MList[CFGNode]]
  var ast2irMap:                                AST2IRMap = null
  var ast2cfgMap:                               AST2CFGMap = null

  // For IR -> (AST, Set[CFG])
  type IR2ASTMap =                              MHashMapEx[IRNode, ASTRootNode]
  type IR2CFGMap =                              MHashMapEx[IRNode, MList[CFGNode]]
  var ir2astMap:                                IR2ASTMap = null
  var ir2cfgMap:                                IR2CFGMap = null

  // For CFG -> (AST, IR)
  type CFG2ASTMap =                             MHashMap[CFGNode, ASTRootNode]
  type CFG2IRMap =                              MHashMap[CFGNode, IRNode]
  var cfg2astMap:                               CFG2ASTMap = null
  var cfg2irMap:                                CFG2IRMap = null

  ////////////////////////////////////////////////////////////////////////////////
  // Reset & Set
  ////////////////////////////////////////////////////////////////////////////////
  var isSet                                     = false

  // Reset
  def reset(): Unit = {
    isSet = false

    // Root node
    astRoot = null
    irRoot = null
    cfgRoot = null

    // Parent & Child
    astParentMap = null
    astChildMap = null
    irParentMap = null
    irChildMap = null
    cfgParentMap = null
    cfgChildMap = null

    // AST <-> IR <-> CFG
    ast2irMap = null
    ast2cfgMap = null
    ir2astMap = null
    ir2cfgMap = null
    cfg2astMap = null
    cfg2irMap = null
  }

  // Set
  def set(ast: Program, ir: IRRoot, cfg: CFG, quiet: Boolean): Unit = {
    // Root node
    astRoot = ast
    irRoot = ir
    // cfgRoot = ?

    // Parent & Children
    astParentMap = new ASTParentMap
    astChildMap = new ASTChildMap
    irParentMap = new IRParentMap
    irChildMap = new IRChildMap
    cfgParentMap = new CFGParentMap
    cfgChildMap = new CFGChildMap

    // AST <-> IR <-> CFG
    ast2irMap = new AST2IRMap
    ast2cfgMap = new AST2CFGMap
    ir2astMap = new IR2ASTMap
    ir2cfgMap = new IR2CFGMap
    cfg2astMap = new CFG2ASTMap
    cfg2irMap = new CFG2IRMap

    // Put AST's parent & children
    def putAST_AST(parent: Any, child: Any): Unit = {
      if(parent != null && !parent.isInstanceOf[ASTRootNode] || !child.isInstanceOf[ASTRootNode]) return
      val parentNode = if(parent.isInstanceOf[ASTRootNode]) parent.asInstanceOf[ASTRootNode] else null
      val childNode = child.asInstanceOf[ASTRootNode]
      astParentMap.put(childNode, parentNode)
      astChildMap.getOrElseUpdate(parentNode, new MList).append(childNode)
    }

    // Put IR's parent & children
    def putIR_IR(parent: Any, child: Any): Unit = {
      if(parent != null && !parent.isInstanceOf[IRNode] || !child.isInstanceOf[IRNode]) return
      val parentNode = if(parent.isInstanceOf[IRNode]) parent.asInstanceOf[IRNode] else null
      val childNode = child.asInstanceOf[IRNode]
      irParentMap.put(childNode, parentNode)
      irChildMap.getOrElseUpdate(parentNode, new MList).append(childNode)
    }

    // Put CFG's parent & children
    def putCFG_CFG(parent: Any, child: Any): Unit = {
      if(parent != null && !parent.isInstanceOf[CFGNode] || !child.isInstanceOf[CFGNode]) return
      val parentNode = if(parent.isInstanceOf[CFGNode]) parent.asInstanceOf[CFGNode] else null
      val childNode = child.asInstanceOf[CFGNode]
      cfgParentMap.put(childNode, parentNode)
      cfgChildMap.getOrElseUpdate(parentNode, new MList).append(childNode)
    }

    // Put AST <-> IR
    def putAST_IR(ir: Any): Unit = {
      val irNode = ir.asInstanceOf[IRNode]
      NodeFactory.ir2ast(irNode) match {
        case Some(ast) =>
          val irList = ast2irMap.getOrElseUpdate(ast, new MList)
          if(!irList.contains(irNode)) irList.append(irNode)
          ir2astMap.get(irNode) match {
            case Some(_) => //throw new RuntimeException("Error!")
            case None => ir2astMap.put(irNode, ast)
          }
        case None =>
      }
    }

    // Put AST <-> CFG
    def putAST_CFG(cfg: Any, info: IRInfoNode): Unit = {
      val cfgNode = cfg.asInstanceOf[CFGNode]
      NodeFactory.irinfo2ir(info) match {
        case Some(irNode) =>
          putIR_CFG(irNode, cfgNode)
          NodeFactory.ir2ast(irNode) match {
            case Some(ast) =>
              val cfgList = ast2cfgMap.getOrElseUpdate(ast, new MList)
              if(!cfgList.contains(cfgNode)) cfgList.append(cfgNode)
              cfg2astMap.get(cfgNode) match {
                case Some(_) => //throw new RuntimeException("Error!")
                case None => cfg2astMap.put(cfgNode, ast)
              }
            case None =>
          }
        case None =>
      }
    }

    // Put IR <-> CFG
    def putIR_CFG(irNode: IRNode, cfgNode: CFGNode): Unit = {
      val cfgList = ir2cfgMap.getOrElseUpdate(irNode, new MList)
      if(!cfgList.contains(cfgNode)) cfgList.append(cfgNode)
      cfg2irMap.get(cfgNode) match {
        case Some(_) => //throw new RuntimeException("Error!")
        case None => cfg2irMap.put(cfgNode, irNode)
      }
    }

    // AST walk
    def walkAST(parent: Any, ast: Any): Unit = {
      putAST_AST(parent, ast)
      ast match {
        case SProgram(info, body, comments) => walkAST(ast, body)
        case SModDecl(info, name, body) => walkAST(ast, name); walkAST(ast, body)
        case SModExpVarStmt(info, vds) => walkAST(ast, vds)
        case SModExpFunDecl(info, fd) => walkAST(ast, fd)
        case SModExpGetter(info, fd) => walkAST(ast, fd)
        case SModExpSetter(info, fd) => walkAST(ast, fd)
        case SModExpSpecifiers(info, names) => walkAST(ast, names)
        case SModImpDecl(info, imports) => walkAST(ast, imports)
        case SNoOp(info, desc) =>
        case SStmtUnit(info, stmts) => walkAST(ast, stmts)
        case SFunDecl(info, ftn) => walkAST(ast, ftn)
        case SBlock(info, stmts, internal) => walkAST(ast, stmts)
        case SVarStmt(info, vds) => walkAST(ast, vds)
        case SEmptyStmt(info) =>
        case SExprStmt(info, expr, internal) => walkAST(ast, expr)
        case SIf(info, cond, trueBranch, falseBranch) => walkAST(ast, cond); walkAST(ast, trueBranch); walkAST(ast, falseBranch)
        case SDoWhile(info, body, cond) => walkAST(ast, body); walkAST(ast, cond)
        case SWhile(info, cond, body) => walkAST(ast, cond); walkAST(ast, body)
        case SFor(info, init, cond, action, body) => walkAST(ast, init); walkAST(ast, cond); walkAST(ast, action); walkAST(ast, body)
        case SForIn(info, lhs, expr, body) => walkAST(ast, lhs); walkAST(ast, expr); walkAST(ast, body)
        case SForVar(info, vars, cond, action, body) => walkAST(ast, vars); walkAST(ast, cond); walkAST(ast, action); walkAST(ast, body)
        case SForVarIn(info, _var, expr, body) => walkAST(ast, _var); walkAST(ast, expr); walkAST(ast, body)
        case SContinue(info, target) => walkAST(ast, target)
        case SBreak(info, target) => walkAST(ast, target)
        case SReturn(info, expr) => walkAST(ast, expr)
        case SWith(info, expr, stmt) => walkAST(ast, expr); walkAST(ast, stmt)
        case SSwitch(info, cond, frontCases, _def, backCases) => walkAST(ast, cond); walkAST(ast, frontCases); walkAST(ast, _def); walkAST(ast, backCases)
        case SLabelStmt(info, label, stmt) => walkAST(ast, label); walkAST(ast, stmt)
        case SThrow(info, expr) => walkAST(ast, expr)
        case STry(info, body, catchBlock, fin) => walkAST(ast, body); walkAST(ast, catchBlock); walkAST(ast, fin)
        case SDebugger(info) =>
        case SVarDecl(info, id, expr) => walkAST(ast, id); walkAST(ast, expr)
        case SCase(info, cond, body) => walkAST(ast, cond); walkAST(ast, body)
        case SCatch(info, id, body) => walkAST(ast, id); walkAST(ast, body)
        case SModImpSpecifierSet(info, imports, module) => walkAST(ast, imports); walkAST(ast, module)
        case SModImpAliasClause(info, name, alias) => walkAST(ast, name); walkAST(ast, alias)
        case SExprList(info, exprs) => walkAST(ast, exprs)
        case SCond(info, cond, trueBranch, falseBranch) => walkAST(ast, cond); walkAST(ast, trueBranch); walkAST(ast, falseBranch)
        case SInfixOpApp(info, left, op, right) => walkAST(ast, left); walkAST(ast, op); walkAST(ast, right)
        case SPrefixOpApp(info, op, right) => walkAST(ast, op); walkAST(ast, right)
        case SUnaryAssignOpApp(info, lhs, op) => walkAST(ast, lhs); walkAST(ast, op)
        case SAssignOpApp(info, lhs, op, right) => walkAST(ast, lhs); walkAST(ast, op); walkAST(ast, right)
        case SThis(info) =>
        case SNull(info) =>
        case SBool(info, bool) =>
        case SDoubleLiteral(info, text, num) =>
        case SIntLiteral(info, intVal, radix) =>
        case SStringLiteral(info, quote, escaped) =>
        case SRegularExpression(info, body, flag) =>
        case SVarRef(info, id) => walkAST(ast, id)
        case SArrayExpr(info, elements) => walkAST(ast, elements)
        case SArrayNumberExpr(info, elements) => walkAST(ast, elements)
        case SObjectExpr(info, members) => walkAST(ast, members)
        case SParenthesized(info, expr) => walkAST(ast, expr)
        case SFunExpr(info, ftn) => walkAST(ast, ftn)
        case SBracket(info, obj, index) => walkAST(ast, obj); walkAST(ast, index)
        case SDot(info, obj, member) => walkAST(ast, obj); walkAST(ast, member)
        case SNew(info, lhs) => walkAST(ast, lhs)
        case SFunApp(info, fun, args) => walkAST(ast, fun); walkAST(ast, args)
        case SPropId(info, id) => walkAST(ast, id)
        case SPropStr(info, str) =>
        case SPropNum(info, num) =>
        case SField(info, prop, expr) => walkAST(ast, prop); walkAST(ast, expr)
        case SGetProp(info, prop, ftn) => walkAST(ast, prop); walkAST(ast, ftn)
        case SSetProp(info, prop, ftn) => walkAST(ast, prop); walkAST(ast, ftn)
        case SId(info, text, uniqueName, _with) =>
        case SOp(info, text) =>
        case SAnonymousFnName(info) =>
        case SPath(info, names) => walkAST(ast, names)
        case SModExpStarFromPath(info, modules) => walkAST(ast, modules)
        case SModExpStar(info) =>
        case SModExpAlias(info, name, alias) => walkAST(ast, name); walkAST(ast, alias)
        case SModExpName(info, name) => walkAST(ast, name)
        case SModImpAlias(info, name, alias) => walkAST(ast, name); walkAST(ast, alias)
        case SModImpName(info, name) => walkAST(ast, name)
        case SLabel(info, id) => walkAST(ast, id)
        case SComment(info, comment) =>
        case STopLevel(fds, vds, stmts) => walkAST(ast, fds); walkAST(ast, vds); walkAST(ast, stmts)
        case SFunctional(id, params, fds, vds, stmts) => walkAST(ast, id); walkAST(ast, params); walkAST(ast, fds); walkAST(ast, vds); walkAST(ast, stmts)
        case astList: List[_] => for(ast <- astList) walkAST(parent, ast)
        case Some(ast) => walkAST(parent, ast)
        case None =>
      }
    }

    // IR walk
    def walkIR(parent: Any, ir: Any): Unit = {
      putIR_IR(parent, ir)
      ir match {
        case SIRRoot(info, fds, vds, irs) => putAST_IR(ir); walkIR(ir, fds); walkIR(ir, vds); walkIR(ir, irs)
        case SIRExprStmt(info, lhs, right, isRef) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, right)
        case SIRDelete(info, lhs, id) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, id)
        case SIRDeleteProp(info, lhs, obj, index) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, obj); walkIR(ir, index)
        case SIRObject(info, lhs, members, proto) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, members); walkIR(ir, proto)
        case SIRArray(info, lhs, elements) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, elements)
        case SIRArrayNumber(info, lhs, elements) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, elements)
        case SIRArgs(info, lhs, elements) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, elements)
        case SIRCall(info, lhs, fun, thisB, args) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, fun); walkIR(ir, thisB); walkIR(ir, args)
        case SIRInternalCall(info, lhs, fun, first, second) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, fun); walkIR(ir, first); walkIR(ir, second)
        case SIRNew(info, lhs, fun, args) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, fun); walkIR(ir, args)
        case SIRFunExpr(info, lhs, ftn) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, ftn)
        case SIREval(info, lhs, arg) => putAST_IR(ir); walkIR(ir, lhs); walkIR(ir, arg)
        case SIRStmtUnit(info, stmts) => putAST_IR(ir); walkIR(ir, stmts)
        case SIRStore(info, obj, index, rhs) => putAST_IR(ir); walkIR(ir, obj); walkIR(ir, index); walkIR(ir, rhs)
        case SIRFunDecl(info, ftn) => putAST_IR(ir); walkIR(ir, ftn)
        case SIRBreak(info, label) => putAST_IR(ir); walkIR(ir, label)
        case SIRReturn(info, expr) => putAST_IR(ir); walkIR(ir, expr)
        case SIRWith(info, id, stmt) => putAST_IR(ir); walkIR(ir, id); walkIR(ir, stmt)
        case SIRLabelStmt(info, label, stmt) => putAST_IR(ir); walkIR(ir, label); walkIR(ir, stmt)
        case SIRVarStmt(info, lhs, fromParam) => putAST_IR(ir); walkIR(ir, lhs)
        case SIRThrow(info, expr) => putAST_IR(ir); walkIR(ir, expr)
        case SIRSeq(info, stmts) => putAST_IR(ir); walkIR(ir, stmts)
        case SIRIf(info, expr, trueB, falseB) => putAST_IR(ir); walkIR(ir, expr); walkIR(ir, trueB); walkIR(ir, falseB)
        case SIRWhile(info, cond, body) => putAST_IR(ir); walkIR(ir, cond); walkIR(ir, body)
        case SIRTry(info, body, name, catchB, finallyB) => putAST_IR(ir); walkIR(ir, body); walkIR(ir, name); walkIR(ir, catchB); walkIR(ir, finallyB)
        case SIRNoOp(info, desc) => putAST_IR(ir)
        case SIRField(info, prop, expr) => putAST_IR(ir); walkIR(ir, prop); walkIR(ir, expr)
        case SIRGetProp(info, ftn) => putAST_IR(ir); walkIR(ir, ftn)
        case SIRSetProp(info, ftn) => putAST_IR(ir); walkIR(ir, ftn)
        case SIRBin(info, first, op, second) => putAST_IR(ir); walkIR(ir, first); walkIR(ir, op); walkIR(ir, second)
        case SIRUn(info, op, expr) => putAST_IR(ir); walkIR(ir, op); walkIR(ir, expr)
        case SIRLoad(info, obj, index) => putAST_IR(ir); walkIR(ir, obj); walkIR(ir, index)
        case SIRUserId(info, originalName, uniqueName, global, _with) => putAST_IR(ir)
        case SIRTmpId(info, originalName, uniqueName, global) => putAST_IR(ir)
        case SIRThis(info) => putAST_IR(ir)
        case SIRNumber(info, text, num) => putAST_IR(ir)
        case SIRString(info, str, escaped) => putAST_IR(ir)
        case SIRBool(info, bool) => putAST_IR(ir)
        case SIRUndef(info) => putAST_IR(ir)
        case SIRNull(info) => putAST_IR(ir)
        case SIROp(text, kind) =>
        case SIRFunctional(fromSource, name, params, args, fds, vds, body) => walkIR(ir, name); walkIR(ir, params); walkIR(ir, args); walkIR(ir, fds); walkIR(ir, vds); walkIR(ir, body)
        case SIRSpanInfo(fromSource, span) =>
        case irList: List[_] => for(ir <- irList) walkIR(parent, ir)
        case Some(ir) => walkIR(parent, ir)
        case None =>
      }
    }

    // CFG walk
    def walkCFG(parent: Any, cfg: Any): Unit = {
      putCFG_CFG(parent, cfg)
      cfg match {
        case CFGAlloc(iid, info, lhs, proto, addr) => putAST_CFG(cfg, info); walkCFG(cfg, lhs); walkCFG(cfg, proto)
        case CFGAllocArray(iid, info, lhs, length, addr) => putAST_CFG(cfg, info); walkCFG(cfg, lhs)
        case CFGAllocArg(iid, info, lhs, length, addr) => putAST_CFG(cfg, info); walkCFG(cfg, lhs)
        case CFGExprStmt(iid, info, lhs, expr) => putAST_CFG(cfg, info); walkCFG(cfg, lhs); walkCFG(cfg, expr)
        case CFGDelete(iid, info, lhs, expr) => putAST_CFG(cfg, info); walkCFG(cfg, lhs); walkCFG(cfg, expr)
        case CFGDeleteProp(iid, info, lhs, obj, index) => putAST_CFG(cfg, info); walkCFG(cfg, lhs); walkCFG(cfg, obj); walkCFG(cfg, index)
        case CFGStore(iid, info, obj, index, rhs) => putAST_CFG(cfg, info); walkCFG(cfg, obj); walkCFG(cfg, index); walkCFG(cfg, rhs)
        case CFGFunExpr(iid, info, lhs, name, fid, addr1, addr2, addr3) => putAST_CFG(cfg, info); walkCFG(cfg, lhs); walkCFG(cfg, name)
        case CFGConstruct(iid, info, cons, thisArg, arguments, addr) => putAST_CFG(cfg, info); walkCFG(cfg, cons); walkCFG(cfg, thisArg); walkCFG(cfg, arguments)
        case CFGCall(iid, info, fun, thisArg, arguments, addr) => putAST_CFG(cfg, info); walkCFG(cfg, fun); walkCFG(cfg, thisArg); walkCFG(cfg, arguments)
        case CFGInternalCall(iid, info, lhs, fun, arguments, addr) => putAST_CFG(cfg, info); walkCFG(cfg, lhs); walkCFG(cfg, fun); walkCFG(cfg, arguments)
        case CFGAPICall(iid, model, fun, arguments) =>
        case CFGAssert(iid, info, expr, flag) => putAST_CFG(cfg, info); walkCFG(cfg, expr)
        case CFGCatch(iid, info, name) => putAST_CFG(cfg, info); walkCFG(cfg, name)
        case CFGReturn(iid, info, expr) => putAST_CFG(cfg, info); walkCFG(cfg, expr)
        case CFGThrow(iid, info, expr) => putAST_CFG(cfg, info); walkCFG(cfg, expr)
        case CFGNoOp(iid, info, desc) => putAST_CFG(cfg, info)
        case CFGAsyncCall(iid, info, modelType, callType, addr1, addr2, addr3) => putAST_CFG(cfg, info)
        case CFGVarRef(info, id) => putAST_CFG(cfg, info); walkCFG(cfg, id)
        case CFGBin(info, first, op, second) => putAST_CFG(cfg, info); walkCFG(cfg, first); walkCFG(cfg, second)
        case CFGUn(info, op, expr) => putAST_CFG(cfg, info); walkCFG(cfg, expr)
        case CFGLoad(info, obj, index) => putAST_CFG(cfg, info); walkCFG(cfg, obj); walkCFG(cfg, index)
        case CFGNumber(text, num) =>
        case CFGString(str) =>
        case CFGBool(bool) =>
        case CFGNull() =>
        case CFGThis(info) => putAST_CFG(cfg, info)
        case CFGUserId(info, text, kind, originalName, fromWith) => putAST_CFG(cfg, info)
        case CFGTempId(text, kind) =>
        case cfgList: List[_] => for(cfg <- cfgList) walkCFG(parent, cfg)
        case Some(cfg) => walkCFG(parent, cfg)
        case None =>
      }
    }

    // Start time
    val startTime = System.nanoTime;

    // AST's parent & children
    walkAST(null, ast)

    // IR's parent & children
    // AST <-> IR
    walkIR(null, ir)

    // CFG's parent & children
    // AST <-> CFG
    // IR <-> CFG
    for(node <- cfg.getNodes) {
      cfg.getCmd(node) match {
        case CFGCmdBlock(insts) => for(inst <- insts) walkCFG(null, inst)
        case _ =>
      }
    }

    isSet = true

    // Elapsed time
    if(!quiet) {
      val elapsedTime = (System.nanoTime - startTime) / 1000000000.0;
      System.out.format("# Time for node relation computation(s): %.2f\n", new java.lang.Double(elapsedTime))
    }

    //dump
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Utils
  ////////////////////////////////////////////////////////////////////////////////
  // Get UID
  def getUID(node: Any): Long = {
    node match {
      case ast: AbstractNode => ast.getUID
      case ast: ScopeBody => ast.getUID
      case ir: IRAbstractNode => ir.getUID
      case ir: IRExpr => ir.getUID
      case ir: IROp => ir.getUID
      case ir: IRInfoNode => ir.getUID
      case _ => 0
    }
  }

  // AST to String
  def astToString(ast: ASTRootNode): String = {
    ast match {
      case ast: ASTNode => JSAstToConcrete.doit(ast).replace('\n', ' ')
      case ast: ScopeBody => ""
    }
  }

  // IR to String
  def irToString(ir: IRNode): String = new JSIRUnparser(ir).doit.replace('\n', ' ')

  // CFG to String
  def cfgToString(cfg: CFGNode): String = cfg match {
    case inst: CFGInst => "[" + inst.getInstId + "] " + inst
    case _ => cfg.toString
  }

  // Used in BugStorage
  def getParentASTStmtOrCond(ast: ASTRootNode): ASTNode = {
    var node = ast
    while(true) {
      if(node == null) return null
      if(node.isInstanceOf[Stmt] || node.isInstanceOf[Cond]) return node.asInstanceOf[ASTNode]
      //println("AST" + node.getClass().getSimpleName() + '[' + getUID(node) + "] : " + astToString(node))
      node = astParentMap(node)
    }
    null
  }

  // Is ancestor
  def isAncestor(ancestor: ASTRootNode, child: ASTRootNode): Boolean = {
    var node = child
    while(true) {
      if(node == null) return false
      astParentMap.get(node) match {
        case Some(parent) => if(parent == ancestor) return true else node = parent
        case None => return false
      }
    }
    false
  }
  def isAncestor(ancestor: IRNode, child: IRNode): Boolean = {
    var node = child
    while(true) {
      if(node == null) return false
      irParentMap.get(node) match {
        case Some(parent) => if(parent == ancestor) return true else node = parent
        case None => return false
      }
    }
    false
  }
  def isAncestor(ancestor: CFGNode, child: CFGNode): Boolean = {
    var node = child
    while(true) {
      if(node == null) return false
      cfgParentMap.get(node) match {
        case Some(parent) => if(parent == ancestor) return true else node = parent
        case None => return false
      }
    }
    false
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Dump
  ////////////////////////////////////////////////////////////////////////////////
  def dump(): Unit = {
    // Parent & Children
    println("astParentMap.size = " + astParentMap.size)
    println("astChildMap.size = " + astChildMap.size)
    println("irParentMap.size = " + irParentMap.size)
    println("irChildMap.size = " + irChildMap.size)
    println("cfgParentMap.size = " + cfgParentMap.size)
    println("cfgChildMap.size = " + cfgChildMap.size)
    println

    // AST's parent
    /*println("*** AST's parent ***")
    for((child, parent) <- astParentMap) {
      if(parent == null) println("AST no parent. (Root)")
      else println("AST" + parent.getClass().getSimpleName() + '[' + getUID(parent) + "] : " + astToString(parent))
      println("    AST" + child.getClass().getSimpleName() + '[' + getUID(child) + "] : " + astToString(child))
    }
    println*/

    // AST's children
    /*println("*** AST's children ***")
    for((parent, childList) <- astChildMap) {
      if(parent == null) println("AST no parent. (Root)")
      else println("AST" + parent.getClass().getSimpleName() + '[' + getUID(parent) + "] : " + astToString(parent))
      for(child <- childList) println("    AST" + child.getClass().getSimpleName() + '[' + getUID(child) + "] : " + astToString(child))
    }
    println*/

    // AST's parent & children
    /*{
      println("*** AST's parent & children ***")
      var indent = 0
      def printAST(ast: ASTRootNode): Unit = {
        for(i <- 0 until indent) print(' ')
        println("AST" + ast.getClass.getSimpleName + '[' + getUID(ast) + ']')
        astChildMap.get(ast) match {
          case Some(children) => indent+= 2; for(child <- children) printAST(child); indent-= 2
          case None =>
        }
      }
      printAST(astRoot)
      println
    }*/

    // IR's parent
    /*println("*** IR's parent ***")
    for((child, parent) <- irParentMap) {
      if(parent == null) println("IR no parent. (Root)")
      else println(parent.getClass().getSimpleName() + '[' + getUID(parent) + "] : " + irToString(parent))
      println("    " + child.getClass().getSimpleName() + '[' + getUID(child) + "] : " + irToString(child))
    }
    println*/

    // IR's children
    /*println("*** IR's children ***")
    for((parent, childList) <- irChildMap) {
      if(parent == null) println("IR no parent. (Root)")
      else println(parent.getClass().getSimpleName() + '[' + getUID(parent) + "] : " + irToString(parent))
      for(child <- childList) println("    " + child.getClass().getSimpleName() + '[' + getUID(child) + "] : " + irToString(child))
    }
    println*/

    // CFG's parent
    /*println("*** CFG's parent ***")
    for((child, parent) <- cfgParentMap) {
      if(parent == null) println("CFG no parent. (Root)")
      else println(parent.getClass().getSimpleName() + " : " + cfgToString(parent))
      println("    " + child.getClass().getSimpleName() + " : " + cfgToString(child))
    }
    println*/

    // CFG's children
    /*println("*** CFG's children ***")
    for((parent, childList) <- cfgChildMap) {
      if(parent == null) println("CFG no parent. (Root)")
      else println(parent.getClass().getSimpleName() + " : " + cfgToString(parent))
      for(child <- childList) println("    " + child.getClass().getSimpleName() + " : " + cfgToString(child))
    }
    println*/

    // AST <-> IR <-> CFG
    println("ast2irMap.size = " + ast2irMap.size)
    println("ast2cfgMap.size = " + ast2cfgMap.size)
    println("ir2astMap.size = " + ir2astMap.size)
    println("ir2cfgMap.size = " + ir2cfgMap.size)
    println("cfg2astMap.size = " + cfg2astMap.size)
    println("cfg2irMap.size = " + cfg2irMap.size)
    println

    // AST -> IR
    /*println("*** AST -> IR ***")
    for((ast, irList) <- ast2irMap) {
      println("AST" + ast.getClass().getSimpleName() + '[' + getUID(ast) + "] : " + astToString(ast))
      for(ir <- irList) println("    " + ir.getClass().getSimpleName() + '[' + getUID(ir) + "] : " + irToString(ir))
    }
    println*/

    // IR -> AST
    /*println("*** IR -> AST ***")
    for((ir, ast) <- ir2astMap) {
      println(ir.getClass().getSimpleName() + '[' + getUID(ir) + "] : " + irToString(ir))
      println("    AST" + ast.getClass().getSimpleName() + '[' + getUID(ast) + "] : " + astToString(ast))
    }
    println*/

    // AST -> CFG
    /*println("*** AST -> CFG ***")
    for((ast, cfgList)<- ast2cfgMap) {
      println("AST" + ast.getClass().getSimpleName() + '[' + getUID(ast) + "] : " + astToString(ast))
      for(cfg <- cfgList) println("    " + cfg.getClass().getSimpleName() + " : " + cfgToString(cfg))
    }
    println*/

    // CFG -> AST
    /*println("*** CFG -> AST ***")
    for((cfg, ast)<- cfg2astMap) {
      println(cfg.getClass().getSimpleName() + " : " + cfgToString(cfg))
      println("    AST" + ast.getClass().getSimpleName() + '[' + getUID(ast) + "] : " + astToString(ast))
    }
    println*/

    // IR -> CFG
    /*println("*** IR -> CFG ***")
    for((ir, cfgList)<- ir2cfgMap) {
      println(ir.getClass().getSimpleName() + '[' + getUID(ir) + "] : " + irToString(ir))
      for(cfg <- cfgList) println("    " + cfg.getClass().getSimpleName() + " : " + cfgToString(cfg))
    }
    println*/

    // CFG -> IR
    /*println("*** CFG -> IR ***")
    for((cfg, ir)<- cfg2irMap) {
      println(cfg.getClass().getSimpleName() + " : " + cfgToString(cfg))
      println("    " + ir.getClass().getSimpleName() + '[' + getUID(ir) + "] : " + irToString(ir))
    }
    println*/
  }
}
