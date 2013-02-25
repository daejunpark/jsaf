/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.compiler

import _root_.java.io.File
import _root_.java.io.FileNotFoundException
import _root_.java.io.IOException
import _root_.java.io.BufferedReader

import xtc.parser.SemanticValue
import xtc.parser.ParseError

import kr.ac.kaist.jsaf.exceptions.JSAFError
import kr.ac.kaist.jsaf.exceptions.MultipleStaticError
import kr.ac.kaist.jsaf.exceptions.ParserError
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.exceptions.SyntaxError
import kr.ac.kaist.jsaf.nodes.RegExpPattern
import kr.ac.kaist.jsaf.parser.RegExp
import kr.ac.kaist.jsaf.useful.Files
import kr.ac.kaist.jsaf.useful.Useful


object RegExpParser {
  class Result(pattern: Option[RegExpPattern], errors: List[SyntaxError])
        extends StaticPhaseResult(errors) {
    var patterns = pattern match {
        case None => Set[RegExpPattern]()
        case Some(p) => Set(p)
      }
  }

  /**
   * Parses a file as a pattern.
   * Converts checked exceptions like IOException and FileNotFoundException
   * to SyntaxError with appropriate error message.
   * Validates the parse by calling
   * parsePattern (see also description of exceptions there).
   */
  def parseFileConvertExn(file: File) =
    try {
      val filename = file.getCanonicalPath
      parsePattern(Useful.utf8BufferedFileReader(file), filename)
    } catch {
      case fnfe:FileNotFoundException =>
        throw convertExn(fnfe, file)
      case ioe:IOException =>
        throw convertExn(ioe, file)
    }

  def parsePattern(in: BufferedReader, filename: String) = {
    val syntaxLogFile = filename + ".log"
    try {
      val parser = new RegExp(in, filename)
      val parseResult = parser.pPattern(0)
      if (parseResult.hasValue) {
        parseResult.asInstanceOf[SemanticValue].value.asInstanceOf[RegExpPattern]
      } else throw new ParserError(parseResult.asInstanceOf[ParseError], parser)
    } finally {
      try {
        Files.rm(syntaxLogFile)
      } catch { case ioe:IOException => }
      try {
        in.close
      } catch { case ioe:IOException => }
    }
  }

  def convertExn(ioe: IOException, f: File) = {
    var desc = "Unable to read file"
    if (ioe.getMessage != null) desc += " (" + ioe.getMessage + ")"
    JSAFError.makeSyntaxError(desc)
  }

  def convertExn(fnfe: FileNotFoundException, f: File) =
    JSAFError.makeSyntaxError("Cannot find file " + f.getAbsolutePath)
}
