/******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import java.io.{IOException, BufferedWriter, File, FileFilter, FileNotFoundException,
                FileWriter, FilenameFilter}
import java.lang.{Integer => JInteger}
import java.util.HashMap
import scala.collection.JavaConversions
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.nodes_util.{JSAstToConcrete, JSFromHTML}
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.nodes.Program
import kr.ac.kaist.jsaf.scala_src.useful.Arrays._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.Samsung
import kr.ac.kaist.jsaf.useful.{Useful, Pair, Files}
import kr.ac.kaist.jsaf.webapp_converter.{ConvertingResultStorage, ConversionInfo, ConvertingResult, WebappConverter}
import java.util

////////////////////////////////////////////////////////////////////////////////
// Web App Converter
////////////////////////////////////////////////////////////////////////////////
object SamsungWebappConvertMain {
  /**
   * Convert SEC Web App into Tizen Web App
   *  - identify a set of JavaScript related files in an "SEC Web App' package
   *  - extract JavaScript code snippet
   *  - parse the extracted JavaScript code into AST
   *  - manipulate AST based on API DB
   *  - unparse the modified AST into JavaScript related files
   */
  var convertingResult: ConvertingResultStorage = null
  val SEP = File.separator
  val jsFilter = new FilenameFilter() {
                     def accept(dir: File, name: String) = name.endsWith(".js")
                 }
  val htmlFilter = new FilenameFilter() {
                       def accept(dir: File, name: String) = name.endsWith(".html")
                   }
  // Navigate subdirectories
  val dirFilter = new FileFilter() {
                      def accept(file: File) =
                        (file.isDirectory && !file.getName.charAt(0).equals("."))
                  }
  def convert: Int = {
    if (Samsung.params.FileNames.length == 0) throw new UserError("Need a directory to convert.")
    var dir = Samsung.params.FileNames(0)
    if (!dir.endsWith(SEP)) dir += SEP
    val file = new File(dir)
    if (!file.isDirectory) throw new UserError("%s is not a directory.".format(dir))
    convertingResult = new ConvertingResultStorage
    convertDir(Samsung.params.FileNames(0))
    0
  }

  /* If this directory contains .js and .html files, convert them all at once.
   * Otherwise, navigate subdirectories.
  */
  def convertDir(_dir: String): Unit = {
    var dir = _dir
    if (!dir.endsWith(SEP)) dir += SEP
    // Permute filenames for randomness
    val file = new File(dir)
    if (!file.isDirectory) throw new UserError("%s is not a directory.".format(_dir))
    file.list(jsFilter).foreach(f => convertJs(dir+f))
    file.list(htmlFilter).foreach(f => convertHtml(dir+f))
    for (subdir <- shuffle(file.listFiles(dirFilter)))
      convertDir(dir + subdir.getName + SEP)
  }

  /* Convert a .js file.
   */
  def convertJs(f: String) = {
    System.out.println("File "+f)
    val file = new File(f)
    try {
      convertAST(Parser.parsePgm(file, file.getCanonicalPath, new JInteger(0), false), f, false)
    } catch {
      case fnfe:FileNotFoundException =>
        throw Parser.convertExn(fnfe, f)
      case ioe:IOException =>
        throw Parser.convertExn(ioe)
    }
  }

  def convertAST(_program: Program, infile: String, isHTML: Boolean) = {
    val converter: WebappConverter = new WebappConverter
    val program = converter.doit(_program).asInstanceOf[Program]
    if (!converter.logList.isEmpty) {
      convertingResult.appendConversionInfoList(converter.logList)
      val converted = JSAstToConcrete.doit(program)
      val outfile = if (isHTML) infile+".converted"
                    else { Files.mv(infile, infile+".org"); infile }
      try {
        val pair: Pair[FileWriter, BufferedWriter] = Useful.filenameToBufferedWriter(outfile)
        val (fw, writer) = (pair.first, pair.second)
        writer.write(converted)
        writer.close
        fw.close
      }
      catch {
        case e: IOException => {
          throw new IOException("IOException " + e + "while writing " + outfile)
        }
      }
      if (isHTML)
        System.out.println("Scripts embedded in " + infile + " are converted and saved to " + outfile)
      else System.out.println(" is converted.")
    } else
      System.out.println(" is not converted.")
  }

  /* Convert an .html file.
   */
  def convertHtml(f: String) = {
    val file = new File(f)
    val jshtml = new JSFromHTML(file.getCanonicalPath)
    convertAST(jshtml.parseNoSrcEventScripts.first, f, true)
  }
}
