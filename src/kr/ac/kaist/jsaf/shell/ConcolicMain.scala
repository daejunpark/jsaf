/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import java.io.{BufferedWriter, FileWriter}
import java.util.HashMap
import scala.collection.JavaConversions
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.analysis.cfg.CFGBuilder
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.bug_detector.StateManager
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.concolic.{Z3, Instrumentor}
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.interpreter.Interpreter
import kr.ac.kaist.jsaf.nodes.{IRRoot, IRStmt, Program}
import kr.ac.kaist.jsaf.nodes_util._
import kr.ac.kaist.jsaf.useful.Pair
import edu.rice.cs.plt.tuple.{Option => JOption}
import kr.ac.kaist.jsaf.nodes_util.Coverage

////////////////////////////////////////////////////////////////////////////////
// Concolic Test
////////////////////////////////////////////////////////////////////////////////
object ConcolicMain {
  /**
   * Working on a very simple concolic testing...
   */
  def concolic: Int = {
    if (Shell.params.FileNames.length == 0) throw new UserError("The concolic command needs a file to perform concolic testing.")
    val fileName: String = Shell.params.FileNames(0)
    val fileNames = JavaConversions.seqAsJavaList(Shell.params.FileNames)

    var return_code = 0
    val coverage = new Coverage
    
    val program: Program = Parser.fileToAST(fileNames)
    val irErrors = Shell.ASTtoIR(fileName, program, JOption.none[String], JOption.some[Coverage](coverage))
    val irOpt: JOption[IRRoot] = irErrors.first
    val program2: Program = irErrors.third
    //val irOpt: JOption[IRRoot] = Shell.fileToIR(fileNames, JOption.none[String], JOption.some[Coverage](coverage)).first
    if (irOpt.isSome) {
      // Initialize AddressManager
      AddressManager.reset()

      var ir: IRRoot = irOpt.unwrap
      val builder = new CFGBuilder(ir)
      val cfg = builder.build
      val errors = builder.getErrors
      if (!(errors.isEmpty))
        Shell.reportErrors(NodeUtil.getFileName(ir), Shell.flattenErrors(errors), JOption.none[Pair[FileWriter, BufferedWriter]])
      NodeRelation.set(program2, ir, cfg, true)
      // Initialize AbsString cache
      kr.ac.kaist.jsaf.analysis.typing.domain.AbsString.initCache
      val initHeap = new InitHeap(cfg)
      initHeap.initialize

      coverage.cfg = cfg
      coverage.typing = new Typing(cfg, true, false)
      coverage.typing.analyze(initHeap)
      coverage.semantics = new Semantics(cfg, Worklist.computes(cfg, true), false)
      coverage.stateManager = new StateManager(cfg, coverage.typing, coverage.semantics) 
      // Store function information using the result of the analysis  
      coverage.updateFunction
      
      val instrumentor = new Instrumentor(ir, coverage)
      ir = instrumentor.doit
      val z3 = new Z3
      val interpreter = new Interpreter
      do {
        do {
          System.out.println
          val result = z3.solve(coverage.getConstraints, coverage.inum)
          if (result.isSome) coverage.setInput(result.unwrap)
          coverage.inputIR = coverage.setupCall match { 
            case Some(ir) => Some(instrumentor.walk(ir, IRFactory.dummyIRId(coverage.target)).asInstanceOf[IRStmt]) 
            case None => None
          }
          interpreter.doit(ir, JOption.some[Coverage](coverage), true)
        } while (coverage.continue)
        coverage.removeTarget
      } while (coverage.existCandidate)
    }
    else return_code = -2

    return_code
  }
}
