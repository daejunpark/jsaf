/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic

import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.Coverage
import kr.ac.kaist.jsaf.nodes_util.{IRFactory => IF}
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._

/* Instrumented IR statements:
 *   [IRRoot]
 *   [IRExprStmt]
 *   [IRCall]
 *   [IRFunExpr]
 *   [IRFunctional]
 *   [IRFunDecl]
 *   [IRReturn]
 *   [IRVarStmt]
 *   [IRSeq]
 *   [IRIf]
 *   [IRWhile]
 *   [IRStmtUnit]
 * Instead of the 'end' statement in LCT, we have to report the terminal
 * information at the end of Interpreter.
 *
 * IR statements in consideration to instrument:
 *   [IRStore]
 *   [IRArray]
 *   [IRBreak]
 *   [IRLabelStmt]
 *   [IRThrow]
 *   [IRTry]
 *
 * Not yet targeted for instrumentation which is related with function call/object:
 *   [IRDelete]
 *   [IRDeleteProp]
 *   [IRObject]
 *   [IRArgs]
 *   [IRInternalCall]
 *   [IRNew]
 *
 * Not instrumented IR statements:
 *   [IREval]
 *   [IRWith]
 */

class Instrumentor(program: IRRoot, coverage: Coverage) extends IRWalker {

  def doit() = walk(program, IF.dummyIRId(NU.freshConcolicName("Main"))).asInstanceOf[IRRoot]

  val dummyId = IF.dummyIRId(NU.freshConcolicName("Instrumentor"))
  def storeEnvironment(info: IRSpanInfo, v: IRId, env: IRId) = 
    SIRInternalCall(info, dummyId, 
                    IF.makeTId(info.getSpan, NU.freshConcolicName("StoreEnvironment")), v, Some(env))

  def executeAssignment(info: IRSpanInfo, e: IRExpr, v: IRId, env: IRId) =
    SIRSeq(info, List(storeEnvironment(info, v, env), 
        SIRInternalCall(info, dummyId,
                    IF.makeTId(info.getSpan, NU.freshConcolicName("ExecuteAssignment")), e, Some(v))))
  def getInput(info: IRSpanInfo, v: IRId, env: IRId) =
    SIRInternalCall(info, dummyId,
                    IF.makeTId(info.getSpan, NU.freshConcolicName("GetInput")), v, Some(env))
  def executeCondition(info: IRSpanInfo, e: IRExpr, env: IRId) =
    SIRInternalCall(info, dummyId,
                    IF.makeTId(info.getSpan, NU.freshConcolicName("ExecuteCondition")), e, Some(env))
  def addFunction(info: IRSpanInfo, id: IRId) =
    SIRInternalCall(info, dummyId, 
                    IF.makeTId(info.getSpan, NU.freshConcolicName("AddFunction")), id, None)

  def walkVarStmt(info: IRSpanInfo, v: IRId, env: IRId) =
    SIRInternalCall(info, dummyId,
                    IF.makeTId(info.getSpan, NU.freshConcolicName("WalkVarStmt")), v, Some(env))
  def walkFunctional(info: IRSpanInfo, node: IRFunctional): IRFunctional = node match {
    case SIRFunctional(i, name, params, args, fds, vds, body) =>
      SIRFunctional(i, name, params,
        args.map(walk(_, name).asInstanceOf[IRStmt]),
        fds.map(walk(_, name).asInstanceOf[IRFunDecl]),
        vds,
        List(addFunction(info, name))++vds.map(walkVarStmt(_, name))++params.map(getInput(info, _, name))++body.map(walk(_, name).asInstanceOf[IRStmt]))  
    }
  /* var x
   * ==>
   * var x;
   * walkVarStmt(x);
   */
  def walkVarStmt(node: IRVarStmt, env: IRId): IRStmt = node match {
    case SIRVarStmt(info, lhs, fromparam) => walkVarStmt(info, lhs, env)
  }

  var array = List[String]()
  def storeIR(name: String): Unit = {
    if (array.indexOf(name) < 0)
      array = array:+name
  }
  def printIRs() = System.out.println(array)
  def walk(node: Any, env: IRId): Any = node match {

    /* begin
     * ==>
     * Initialize();
     *
     * SymbolicExecutor should perform "Initialize()"
     * when it encounters SIRRoot.
     */
    case SIRRoot(info, fds, vds, irs) =>
      storeIR("SIRRoot")
      SIRRoot(info, fds.map(walk(_, env).asInstanceOf[IRFunDecl]),
              vds, 
              vds.map(walkVarStmt(_, env))++irs.map(walk(_, env).asInstanceOf[IRStmt]))

    /* x = e
     * ==>
     * x = e
     * EXECUTE_ASSIGNMENT(x, e);
     *
     * x = e
     * SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteAssignment", e, Some(x))
     */
    case SIRExprStmt(info, lhs, right, ref) =>
      storeIR("SIRExprStmt")
      SIRSeq(info, List(node.asInstanceOf[IRStmt], executeAssignment(info, right, lhs, env)))

    /* x = x(x, x)
     * ==>
     * STORE_FUNCTION_INFORMATION(node); 
     * x = x(x, x)
     *
     */
    case SIRCall(info, lhs, fun, thisB, args) =>       
      storeIR("SIRCall")
      coverage.storeFuncInfo(node)
      node

    /* x = function f (x, x) {s*}
     * ==>
     * ADD_FUNCTION(f)
     * if (CHECK_FOCUS(f))
     *   args = GET_INPUTS(args)
     *   s*
     * }
     *
     * SymbolicExecutor should perform "CHECK_FOCUS()" 
     */
    case SIRFunExpr(info, lhs, ftn) =>  
      storeIR("SIRFunExpr")
      SIRFunExpr(info, lhs, walkFunctional(info, ftn).asInstanceOf[IRFunctional])

    /* function f (x, x) {s*}
     * ==>
     * ADD_FUNCTION(f)
     * if (CHECK_FOCUS(f))
     *   args = GET_INPUTS(args)
     *   s*
     * }
     *
     * SymbolicExecutor should perform "CHECK_FOCUS()" 
     */
    case SIRFunDecl(info, ftn) => 
      storeIR("SIRFunDecl")
      SIRFunDecl(info, walkFunctional(info, ftn).asInstanceOf[IRFunctional])

    case SIRFunctional(i, name, params, args, fds, vds, body) => 
      storeIR("SIRFunctional")
      node

    /* return e?
     * ==>
     * return e?
     */
    case SIRReturn(info, expr) => 
      storeIR("SIRReturn")
      node

    case SIRSeq(info, stmts) =>
      storeIR("SIRSeq")
      SIRSeq(info, stmts.map(walk(_, env).asInstanceOf[IRStmt]))
    
    /* if (e) then s (else s)?
     * ==>
     * if (e) 
     * then {EXECUTE_CONDITION(e); s;} 
     * else {EXECUTE_CONDITIOIN(e); s;?}
     *
     * if (e) 
     * then {SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteCondition", e, None) s} 
     * else {SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteCondition", e, None) s?}
     */
    case SIRIf(info, expr, trueB, falseB) =>
      storeIR("SIRIf")
      SIRIf(info, expr, 
        SIRSeq(info, List(executeCondition(info, expr, env), walk(trueB, env).asInstanceOf[IRStmt])),
        falseB match { case Some(s) => Some(SIRSeq(info, List(executeCondition(info, expr, env), walk(s, env).asInstanceOf[IRStmt])))
                       case None => Some(executeCondition(info, expr, env))})

    /* while (e) s
     * ==>
     * while (e) {
     *   EXECUTE_CONDITION(e);
     *   CHECK_LOOP();
     *   s
     * }
     * EXECUTE_CONDITION(e);
     *
     * while (e) {
     *   SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteCondition", e, None)
     *   s
     * }
     * SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteCondition", e, None)
     *
     * SymbolicExecutor should perform "CHECK_LOOP()" 
     * and "EXECUTE_CONDITION(e)" when it encounters SIRWhile.
     */
     //TODO: Report condition only once
    case SIRWhile(info, cond, body) =>
      storeIR("SIRWhile")
      SIRSeq(info, List(SIRWhile(info, cond, 
        SIRSeq(info, List(executeCondition(info, cond, env), walk(body, env).asInstanceOf[IRStmt]))),
        executeCondition(info, cond, env)))

    case SIRStore(info, obj, index, rhs) => 
      storeIR("SIRStore")
      node

    case SIRArray(info, lhs, elems) => 
      storeIR("SIRArray")
      node
    
    case SIRArrayNumber(info, lhs, elements) => 
      storeIR("SIRArrayNumber")
      node

    case SIRBreak(info, label) => 
      storeIR("SIRBreak")
      node

    case SIRLabelStmt(info, label, stmt) =>
      storeIR("SIRLabelStmt")
      SIRLabelStmt(info, label, walk(stmt, env).asInstanceOf[IRStmt])

    case SIRThrow(info, expr) => 
      storeIR("SIRThrow")
      node

    case SIRTry(info, body, name, catchB, finallyB) =>
      storeIR("SIRTry")
      SIRTry(info, walk(body, env).asInstanceOf[IRStmt], name,
             catchB match { case Some(s) => Some(walk(s, env).asInstanceOf[IRStmt])
                            case None => None },
             finallyB match { case Some(s) => Some(walk(s, env).asInstanceOf[IRStmt])
                              case None => None })

    case SIRStmtUnit(info, stmts) =>
      storeIR("SIRStmtUnit")
      SIRStmtUnit(info, stmts.map(walk(_, env).asInstanceOf[IRStmt]))

    case SIRDelete(info, lhs, id) => 
      storeIR("SIRDelete")
      node

    case SIRDeleteProp(info, lhs, obj, index) => 
      storeIR("SIRDeleteProp")
      node

    case SIRObject(info, lhs, members, proto) => 
      storeIR("SIRObject")
      node

    case SIRArgs(info, lhs, elems) => 
      storeIR("SIRArgs")
      node

    case SIRInternalCall(info, lhs, fun, first, second) =>
      storeIR("SIRInternalCall")
      node

    case SIRNew(info, lhs, fun, args) => 
      storeIR("SIRNew")
      node

    case SIREval(info, lhs, arg) => 
      storeIR("SIREval")
      node

    case SIRWith(info, id, stmt) =>
      storeIR("SIRWith")
      SIRWith(info, id, walk(stmt, env).asInstanceOf[IRStmt])

    case _ => node
  }
}
