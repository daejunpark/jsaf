/*******************************************************************************
    Copyright (c) 2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.tests

import _root_.java.util.{List => JList}
import _root_.java.io.BufferedReader
import _root_.java.io.InputStreamReader
import _root_.java.io.InputStream
import _root_.java.io.File
import _root_.java.io.FileFilter
import _root_.java.io.FilenameFilter
import _root_.java.io.IOException
import _root_.java.io.PrintStream
import _root_.java.util.Arrays
import _root_.java.util.Collections
import _root_.java.util.Random

import kr.ac.kaist.jsaf.ProjectProperties
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.exceptions.SyntaxError
import kr.ac.kaist.jsaf.parser.WIDL
import kr.ac.kaist.jsaf.scala_src.useful.Arrays._
import kr.ac.kaist.jsaf.useful.Files
import kr.ac.kaist.jsaf.useful.TestCaseWrapper
import kr.ac.kaist.jsaf.useful.Useful
import kr.ac.kaist.jsaf.useful.WireTappedPrintStream

import junit.framework.Assert
import junit.framework.TestCase
import junit.framework.TestSuite

object WIDLParserJUTest extends TestCaseWrapper {

  val SEP = File.separator
  val WIDLPARSER_FAIL_TESTS_DIR = ProjectProperties.BASEDIR + SEP + "tests/widl_tests" + SEP

  def suite(): TestSuite =
    new WIDLParserTestSuite("WIDLParserJUTest", WIDLPARSER_FAIL_TESTS_DIR)

  class WIDLParserTestSuite(_name: String,  _failTestDir: String) extends TestSuite(_name) {
    val VERBOSE = true

    // relative to the top directory
    var dir = _failTestDir
    if (!dir.endsWith(SEP)) dir += SEP
    val jsFilter = new FilenameFilter() {
                     def accept(dir: File, name: String) = name.endsWith(".widl")
                   }
    // Permute filenames for randomness
    for (filename <- shuffle(new File(dir).list(jsFilter)))
      addTest(new WIDLParserTestCase(new File(dir + filename)))

    class WIDLParserTestCase(_file: File) extends TestCase(_file.getName) {
      var file = _file

      override def runTest() = {
        // do not print stuff to stdout for JUTests
        val oldOut = System.out
        val oldErr = System.err
        val wt_err = WireTappedPrintStream.make(System.err, true)
        val wt_out = WireTappedPrintStream.make(System.out, true)
        System.setErr(wt_err)
        System.setOut(wt_out)

        parseFile(file)
        System.setErr(oldErr)
        System.setOut(oldOut)
      }

      def parseFile(f: File) =
        try {
          val parser = new WIDL(Useful.utf8BufferedFileReader(f), f.getName)
          parser.pWIDL(0)
        } catch {
          case se:SyntaxError => new Parser.Result(None, List(se))
        }
    }
  }
}
