/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import java.io._
import java.nio.charset.Charset
import java.util.{ArrayList, HashMap}
import scala.collection.JavaConversions
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.exceptions.{ParserError, UserError}
import kr.ac.kaist.jsaf.nodes_util.{WIDLChecker, WIDLToDB, WIDLToString}
import kr.ac.kaist.jsaf.nodes.{WDefinition, Program}
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.useful.{Files, Pair}
import kr.ac.kaist.jsaf.parser.WIDL
import xtc.parser.{ParseError, SemanticValue}

////////////////////////////////////////////////////////////////////////////////
// Web IDL
////////////////////////////////////////////////////////////////////////////////
object WIDLMain {
  ////////////////////////////////////////////////////////////////////////////////
  // Parse
  ////////////////////////////////////////////////////////////////////////////////
  /**
   * Parse a Web IDL file.
   * If you want a dump then give -out somefile.
   */
  def widlparse: Int = {
    if (Shell.params.FileNames.length == 0) throw new UserError("The widlparse command needs a file to parse.")
    val fileName = Shell.params.FileNames(0)

    val return_code = 0
    try {
      val fs = new FileInputStream(new File(fileName))
      val sr = new InputStreamReader(fs, Charset.forName("UTF-8"))
      val in = new BufferedReader(sr)
      val parser = new WIDL(in, fileName)
      val parseResult = parser.pWIDL(0)
      in.close
      sr.close
      fs.close
      if (parseResult.hasValue) {
        val widl = parseResult.asInstanceOf[SemanticValue].value.asInstanceOf[ArrayList[WDefinition]]
        val code = WIDLToString.doit(widl)
        if (Shell.params.opt_DB) WIDLToDB.storeToDB(fileName, widl)
        else System.out.println(code)
      }
      else {
        System.out.println("WIDL parsing failed.")
        throw new ParserError(parseResult.asInstanceOf[ParseError], parser, 0)
      }
    }
    catch {
      case f: FileNotFoundException => {
        throw new UserError(fileName + " not found")
      }
    }
    finally {
      Files.rm(fileName + ".db")
      Files.rm(fileName + ".test")
    }

    return_code
  }

  def widlparse(fileName: String): Int = {
    Shell.params.Clear
    Shell.params.FileNames = new Array[String](1)
    Shell.params.FileNames(0) = fileName
    widlparse
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Use Check
  ////////////////////////////////////////////////////////////////////////////////
  /**
   * Check the uses of APIs in Web IDL.
   */
  def widlcheck: Int = {
    if (Shell.params.FileNames.length == 0) throw new UserError("The widlcheck command needs a file to parse.")
    val fileName = Shell.params.FileNames(0)
    val fileNames = JavaConversions.seqAsJavaList(Shell.params.FileNames.drop(1))

    val return_code = 0
    val file = new ArrayList[String]
    file.add(fileName)

    val pair: Pair[Program, HashMap[String, String]] = Parser.fileToAST(file)
    val pgm: Program = pair.first
    WIDLChecker.doit(pgm, fileNames)

    return_code
  }
}
