/*******************************************************************************
    Copyright (c) 2012, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic

import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.concolic.{SymbolicHelper => SH}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{ IRFactory => IF }
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._


class SymbolicExecutor(program: IRRoot) extends IRWalker {

  def doit() = {
    walk(program)
//    SH.printMemory()
  }

  //def printMemory() = 
    //System.out.println("%s", SH.symbolicMemory.toString)
  override def walk(node:Any): Any = node match {

    /* begin
     * ==>
     * Initialize();
     *
     * SymbolicExecutor should perform "Initialize()"
     * when it encounters SIRRoot.
     */
    case SIRRoot(info, fds, vds, irs) =>
      SH.initialize
      // evaluate the code
      fds.foreach(fd => walk(fd))
      vds.foreach(vd => walk(vd))
      irs.foreach(ir => walk(ir))

    case SIRInternalCall(info, lhs, fun, expr, idOpt) =>
      fun.getUniqueName match {
        /* v = e
         * ==>
         * v = e
         * EXECUTE_ASSIGNMENT(v, e);
         *
         * v = e
         * SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteAssignment", e, Some(v))
         */
        case "<>Concolic<>ExecuteAssignment" =>
         // SH.executeAssignment(idOpt.get, expr)

        /* var v
         * ==>
         * var v;
         * v = GETINPUT(v);
         *
         * var v;
         * SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>GetInput", v, None)
         */
        case "<>Concolic<>GetInput" =>
          SH.getInput(expr.asInstanceOf[IRId])

        /* if (e) then s (else s)?
         * ==>
         * EXECUTE_CONDITION(e);
         * if (e) then s (else s)?
         *
         * SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteCondition", e, None)
         * if (e) then s (else s)?
         */
        case "<>Concolic<>ExecuteCondition" =>
         // SH.executeCondition(expr)

        case _ =>
          // evaluate the code
          super.walk(node)
      }

    /* while (e) s
     * ==>
     * EXECUTE_CONDITION(e);
     * while (e) {
     *   CHECK_LOOP();
     *   s
     * }
     *
     * SIRInternalCall(info, "<>Concolic<>Instrumentor", "<>Concolic<>ExecuteCondition", e, None)
     * while (e) {
     *   s
     * }
     * SymbolicExecutor should perform "CHECK_LOOP()"
     * when it encounters SIRWhile.
     */
    case SIRWhile(info, cond, body) =>
      // evaluate SH.checkLoop before evaluating the body
      super.walk(cond)
      SH.checkLoop
      super.walk(body)

    case _ =>
      // evaluate the code
      super.walk(node)
  }
}
