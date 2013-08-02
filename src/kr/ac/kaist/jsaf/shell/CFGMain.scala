/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import java.io.{BufferedWriter, FileWriter}
import scala.collection.JavaConversions
import kr.ac.kaist.jsaf.analysis.cfg.{DotWriter, CFG, CFGBuilder}
import kr.ac.kaist.jsaf.analysis.typing.{InitHeap, Config}
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.nodes_util.NodeUtil
import kr.ac.kaist.jsaf.nodes.IRRoot
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.useful.Pair
import edu.rice.cs.plt.tuple.{Option => JOption}

////////////////////////////////////////////////////////////////////////////////
// CFG Builder
////////////////////////////////////////////////////////////////////////////////
object CFGMain {
  /**
   * Build a control flow graph.
   * If you want a dump then give -out somefile.
   */
  def cfgBuilder: Int = {
    if (Shell.params.FileNames.length == 0) throw new UserError("Need a file to build a control flow graph.")
    val fileNames = JavaConversions.seqAsJavaList(Shell.params.FileNames)

    if (Shell.params.opt_Test) {
      Config.setTestMode(Shell.params.opt_Test)
      System.out.println("Test mode enabled.")
    }
    if (Shell.params.opt_Dom) {
      Config.setDomMode
      System.out.println("DOM mode enabled.")
    }
    if (Shell.params.opt_Tizen) {
      Config.setTizenMode
      System.out.println("Tizen mode enabled.")
    }
    if (Shell.params.opt_Library) {
      Config.setLibMode(Shell.params.opt_Library)
      System.out.println("Library mode enabled.")
    }

    var return_code = 0
    val irOpt: JOption[IRRoot] = Shell.fileToIR(fileNames, Shell.toOption(Shell.params.opt_OutFileName))
    if (irOpt.isSome) {
      val ir: IRRoot = irOpt.unwrap
      val builder = new CFGBuilder(ir)
      val cfg: CFG = builder.build
      val errors = builder.getErrors
      if (!(errors.isEmpty)) {
        Shell.reportErrors(NodeUtil.getFileName(ir), Shell.flattenErrors(errors), JOption.none[Pair[FileWriter, BufferedWriter]])
      }
      if (Shell.params.opt_Model) {
        val init: InitHeap = new InitHeap(cfg)
        init.initialize
      }
      if (Shell.params.opt_OutFileName != null) {
        val outfile: String = Shell.params.opt_OutFileName
        DotWriter.write(cfg, outfile + ".dot", outfile + ".svg", "dot")
      }
      else cfg.dump
    }
    else return_code = -2

    return_code
  }
}