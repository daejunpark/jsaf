/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import java.io.{FileNotFoundException, IOException}
import java.util.HashMap
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.nodes.Program
import kr.ac.kaist.jsaf.nodes_util.{ASTIO, NodeFactory}
import kr.ac.kaist.jsaf.useful.Pair
import scala.collection.JavaConversions

////////////////////////////////////////////////////////////////////////////////
// Parse
////////////////////////////////////////////////////////////////////////////////
object ParseMain {
  /**
   * Parse a file. If the file parses ok it will say "Ok".
   * If you want a dump then give -out somefile.
   */
  def parse(): Int = {
    if (Shell.params.FileNames.length == 0) throw new UserError("Need a file to parse")
    val fileNames = JavaConversions.seqAsJavaList(Shell.params.FileNames)

    val return_code = 0
    try {
      NodeFactory.initIr2ast
      val pgm: Program = Parser.fileToAST(fileNames)
      System.out.println("Ok")
      if (Shell.params.opt_OutFileName != null) {
        try {
          ASTIO.writeJavaAst(pgm, Shell.params.opt_OutFileName)
          System.out.println("Dumped parse tree to " + Shell.params.opt_OutFileName)
        }
        catch {
          case e: IOException => {
            throw new IOException("IOException " + e + "while writing " + Shell.params.opt_OutFileName)
          }
        }
      }
    }
    catch {
      case f: FileNotFoundException => {
        throw new UserError(f + " not found")
      }
    }

    if (Shell.params.opt_Time) Shell.printTimeTitle = "Parsing"

    return_code
  }

  def parse(fileName: String, outFileName: String): Int = {
    Shell.params.Clear
    Shell.params.opt_OutFileName = outFileName
    Shell.params.FileNames = new Array[String](1)
    Shell.params.FileNames(0) = fileName
    parse()
  }
}
