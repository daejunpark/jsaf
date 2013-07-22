/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import scala.collection.JavaConversions
import kr.ac.kaist.jsaf.concolic.{Z3, Instrumentor}
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.nodes_util.Coverage
import kr.ac.kaist.jsaf.nodes.IRRoot
import kr.ac.kaist.jsaf.interpreter.Interpreter
import kr.ac.kaist.jsaf.Shell
import edu.rice.cs.plt.tuple.{Option => JOption}

////////////////////////////////////////////////////////////////////////////////
// Concolic Test
////////////////////////////////////////////////////////////////////////////////
object ConcolicMain {
  /**
   * Working on a very simple concolic testing...
   */
  def concolic: Int = {
    if (Shell.params.FileNames.length == 0) throw new UserError("The concolic command needs a file to perform concolic testing.")
    val fileNames = JavaConversions.seqAsJavaList(Shell.params.FileNames)

    var return_code = 0
    val coverage = new Coverage
    val irOpt: JOption[IRRoot] = Shell.fileToIR(fileNames, JOption.none[String], JOption.some[Coverage](coverage)).first
    if (irOpt.isSome) {
      val ir: IRRoot = new Instrumentor(irOpt.unwrap).doit
      val z3 = new Z3
      val interpreter = new Interpreter
      do {
        do {
          System.out.println
          val result = z3.solve(coverage.getConstraints, coverage.inputNum)
          if (result.isSome) coverage.setInput(result.unwrap)
          interpreter.doit(ir, JOption.some[Coverage](coverage), true)
        } while (coverage.cont)
        coverage.removeTarget
      } while (coverage.existCandidate)
    }
    else return_code = -2

    return_code
  }
}
