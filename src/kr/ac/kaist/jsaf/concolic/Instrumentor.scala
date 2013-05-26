/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic

import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{ IRFactory => IF }
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._

/* Instrumented IR statements:
 *   [IRRoot]
 *   [IRExprStmt]
 *   [IRVarStmt]
 *   [IRIf]
 *   [IRWhile]
 * Instead of the 'end' statement in LCT, we have to report the terminal
 * information at the end of Interpreter.
 *
 * IR statements in consideration to instrument:
 *   [IRStore]
 *   [IRArray]
 *   [IRBreak]
 *   [IRReturn]
 *   [IRLabelStmt]
 *   [IRThrow]
 *   [IRSeq]
 *   [IRTry]
 *   [IRStmtUnit]
 *
 * Not yet considered IR statements to instrument, i.e., it is related to
 * function call/object:
 *   [IRDelete]
 *   [IRDeleteProp]
 *   [IRObject]
 *   [IRArgs]
 *   [IRCall]
 *   [IRInternalCall]
 *   [IRNew]
 *   [IRFunExpr]
 *   [IRFunctional]
 *   [IRFunDecl]
 *
 * Not instrumented IR statements:
 *   [IREval]
 *   [IRWith]
 */

class Instrumentor(program: IRRoot) extends IRWalker {

  def doit() = walk(program).asInstanceOf[IRRoot]

  val dummyId = IF.dummyIRId(NU.freshConcolicName("Instrumentor"))
  def executeAssignment(info: IRSpanInfo, e: IRExpr, v: IRId) =
    SIRInternalCall(info, dummyId,
                    IF.makeTId(IF.dummyAst, info.getSpan, NU.freshConcolicName("ExecuteAssignment")), e, Some(v))
  def getInput(info: IRSpanInfo, v: IRId) =
    SIRInternalCall(info, dummyId,
                    IF.makeTId(IF.dummyAst, info.getSpan, NU.freshConcolicName("GetInput")), v, None)
  def executeCondition(info: IRSpanInfo, e: IRExpr) =
    SIRInternalCall(info, dummyId,
                    IF.makeTId(IF.dummyAst, info.getSpan, NU.freshConcolicName("ExecuteCondition")), e, None)

    /* var v
     * ==>
     * var v;
     * v = GETINPUT(v);
     *
     * var v;
     * SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>GetInput", v, None)
     */
  def walkVarStmt(node: IRVarStmt): IRStmt = node match {
    case SIRVarStmt(info, lhs, fromparam) => getInput(info, lhs)
  }

  override def walk(node:Any): Any = node match {

    /* begin
     * ==>
     * Initialize();
     *
     * SymbolicExecutor should perform "Initialize()"
     * when it encounters SIRRoot.
     */
    case SIRRoot(info, fds, vds, irs) =>
      SIRRoot(info, fds.map(walk(_).asInstanceOf[IRFunDecl]),
              vds,
              vds.map(walkVarStmt(_))++irs.map(walk(_).asInstanceOf[IRStmt]))

    /* v = e
     * ==>
     * v = e
     * EXECUTE_ASSIGNMENT(v, e);
     *
     * v = e
     * SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteAssignment", e, Some(v))
     */
    case SIRExprStmt(info, lhs, right, ref) =>
      SIRSeq(info, List(node.asInstanceOf[IRStmt], executeAssignment(info, right, lhs)))

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
      SIRIf(info, expr, 
            SIRSeq(info, 
                   List(executeCondition(info, expr),
                        walk(trueB).asInstanceOf[IRStmt])),
            falseB match { case Some(s) => 
                               Some(SIRSeq(info,
                                           List(executeCondition(info, expr),
                                                walk(s).asInstanceOf[IRStmt])))
                           case None => Some(executeCondition(info, expr))})

      /*SIRSeq(info,
             List(SIRIf(info, expr, walk(trueB).asInstanceOf[IRStmt],
                        falseB match { case Some(s) => Some(walk(s).asInstanceOf[IRStmt])
                                       case None => None }), 
                  executeCondition(info, expr)))*/

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
      SIRSeq(info,
             List(SIRWhile(info, cond, 
                           SIRSeq(info, 
                                  List(executeCondition(info, cond),
                                       walk(body).asInstanceOf[IRStmt]))),
                  executeCondition(info, cond)))

    case SIRStore(info, obj, index, rhs) => node
    case SIRArray(info, lhs, elems) => node
    case SIRArrayNumber(info, lhs, elements) => node
    case SIRBreak(info, label) => node
    case SIRReturn(info, expr) => node

    case SIRLabelStmt(info, label, stmt) =>
      SIRLabelStmt(info, label, walk(stmt).asInstanceOf[IRStmt])

    case SIRThrow(info, expr) => node

    case SIRSeq(info, stmts) =>
      SIRSeq(info, stmts.map(walk(_).asInstanceOf[IRStmt]))

    case SIRTry(info, body, name, catchB, finallyB) =>
      SIRTry(info, walk(body).asInstanceOf[IRStmt], name,
             catchB match { case Some(s) => Some(walk(s).asInstanceOf[IRStmt])
                            case None => None },
             finallyB match { case Some(s) => Some(walk(s).asInstanceOf[IRStmt])
                              case None => None })

    case SIRStmtUnit(info, stmts) =>
      SIRStmtUnit(info, stmts.map(walk(_).asInstanceOf[IRStmt]))

    case SIRDelete(info, lhs, id) => node
    case SIRDeleteProp(info, lhs, obj, index) => node
    case SIRObject(info, lhs, members, proto) => node
    case SIRArgs(info, lhs, elems) => node
    case SIRCall(info, lhs, fun, thisB, args) => node
    case SIRInternalCall(info, lhs, fun, first, second) => node
    case SIRNew(info, lhs, fun, args) => node

    case SIRFunExpr(info, lhs, ftn) =>
      SIRFunExpr(info, lhs, walk(ftn).asInstanceOf[IRFunctional])

    case SIRFunctional(i, j, name, params, args, fds, vds, body) =>
      SIRFunctional(i, j, name, params, args.map(walk(_).asInstanceOf[IRStmt]),
                    fds.map(walk(_).asInstanceOf[IRFunDecl]), vds,
                    vds.map(walkVarStmt(_))++body.map(walk(_).asInstanceOf[IRStmt]))

    case SIRFunDecl(info, ftn) =>
      SIRFunDecl(info, walk(ftn).asInstanceOf[IRFunctional])

    case SIREval(info, lhs, arg) => node
    case SIRWith(info, id, stmt) =>
      SIRWith(info, id, walk(stmt).asInstanceOf[IRStmt])

    case _ => node
  }
}
