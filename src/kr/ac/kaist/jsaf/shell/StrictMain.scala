/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import java.io.{BufferedWriter, FileWriter, IOException}
import java.util.HashMap
import scala.collection.JavaConversions
import kr.ac.kaist.jsaf.exceptions.{UserError}
import kr.ac.kaist.jsaf.compiler.{StrictModeChecker, Parser}
import kr.ac.kaist.jsaf.nodes.Program
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.useful.Pair
import kr.ac.kaist.jsaf.useful.Useful
import kr.ac.kaist.jsaf.nodes_util.NodeUtil
import edu.rice.cs.plt.tuple.{Option => JOption}

////////////////////////////////////////////////////////////////////////////////
// Strict Mode Checker
////////////////////////////////////////////////////////////////////////////////
object StrictMain {
  /**
   * Check whether a given program satisfies
   * the ECMAScript 5 strict mode restrictions.
   * If you want to dump what restrictions are not satisfied,
   * then give -out somefile.
   * Not yet fully implemented.
   */
  def strict(): Int = {
    if (Shell.params.FileNames.length == 0) throw new UserError("The unparse command needs a file to unparse.")
    val fileNames = JavaConversions.seqAsJavaList(Shell.params.FileNames)

    val pair: Pair[Program, HashMap[String, String]] = Parser.fileToAST(fileNames)
    val pgm: Program = pair.first
    val errors = new StrictModeChecker(pgm).doit
    if (Shell.params.opt_OutFileName != null) {
      try {
        val ppair: Pair[FileWriter, BufferedWriter] = Useful.filenameToBufferedWriter(Shell.params.opt_OutFileName)
        Shell.reportErrors(NodeUtil.getFileName(pgm), Shell.flattenErrors(errors), JOption.some[Pair[FileWriter, BufferedWriter]](ppair))
      }
      catch {
        case e: IOException => {
          throw new IOException("IOException " + e + "while writing " + Shell.params.opt_OutFileName)
        }
      }
    }
    else {
      Shell.reportErrors(NodeUtil.getFileName(pgm), Shell.flattenErrors(errors), JOption.none[Pair[FileWriter, BufferedWriter]])
    }
  }
}
