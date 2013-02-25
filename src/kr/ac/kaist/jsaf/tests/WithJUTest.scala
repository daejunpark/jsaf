/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
******************************************************************************/

package kr.ac.kaist.jsaf.tests

import _root_.java.io.BufferedWriter
import _root_.java.io.BufferedReader
import _root_.java.io.File
import _root_.java.io.Reader
import _root_.java.io.FileWriter
import _root_.java.io.FileReader
import _root_.java.io.FileFilter
import _root_.java.io.FilenameFilter

import _root_.javax.script.ScriptEngineManager;
import _root_.javax.script.ScriptEngine;

import kr.ac.kaist.jsaf.ProjectProperties
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.compiler.Hoister
import kr.ac.kaist.jsaf.compiler.WithRewriter
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.nodes.Program
import kr.ac.kaist.jsaf.nodes_util.JSAstToConcrete
import kr.ac.kaist.jsaf.useful.TestCaseWrapper
import kr.ac.kaist.jsaf.useful.Useful
import kr.ac.kaist.jsaf.useful.WireTappedPrintStream
import kr.ac.kaist.jsaf.scala_src.useful.Arrays._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._

import junit.framework.Assert
import junit.framework.TestCase
import junit.framework.TestSuite

object WithJUTest extends TestCaseWrapper {

  val SEP = File.separator
  val WITHREWRITER_FAIL_TESTS_DIR = "tests/with_tests"

  def suite(): TestSuite =
    new WithRewriterTestSuite("WithRewriterJUTest", WITHREWRITER_FAIL_TESTS_DIR)

  class WithRewriterTestSuite(_name: String, _failTestDir: String) extends TestSuite(_name) {
    val VERBOSE = true

    // relative to the top directory
    var failTestDir = _failTestDir
    val _ = addWithRewriterTests(failTestDir)

    def addWithRewriterTests(_dir: String): Unit = {
      var dir = _dir
      if (!dir.endsWith(SEP)) dir += SEP
      val jsFilter = new FilenameFilter() {
        def accept(dir: File, name: String) = name.endsWith(".js")
      }
      //Permute filenames for randomness
      for (filename <- shuffle(new File(dir).list(jsFilter)))
        addTest(new WithRewriterTestCase(new File(dir + filename)))

      // Navigate subdirectories
      val dirFilter = new FileFilter() {
        def accept(file: File) =
          (file.isDirectory && file.getName.charAt(0) != '.')
      }

      for (subdir <- shuffle(new File(dir).listFiles(dirFilter)))
        addWithRewriterTests(dir + subdir.getName + SEP)
    }

    class WithRewriterTestCase(_file: File) extends TestCase(_file.getName) {
      var file = _file

      override def runTest() = {
        // do not print stuff to stdout for JUTests
          /*
        val oldOut = System.out
        val oldErr = System.err
        val wt_err = WireTappedPrintStream.make(System.err, true)
        val wt_out = WireTappedPrintStream.make(System.out, true)
        System.setErr(wt_err)
        System.setOut(wt_out)
          */

        assertWithRewriterSucceeds(file)
        /*
        System.setErr(oldErr)
        System.setOut(oldOut)
        */
      }

      def assertWithRewriterSucceeds(file: File) = {
/*
        val rewritten_file : BufferedWriter = new BufferedWriter(new FileWriter("/Users/jjh/with_tests/with_rewriter/src/com/google/javascript/rhino/Tests/tests_with/passed_test/" + file.getName + ".rewritten"))
        rewritten_file.write(new JSAstToConcrete(new WithRewriter(new Hoister(Parser.parseFileConvertExn(file)).doit.asInstanceOf[Program]).doit.asInstanceOf[Program]).doit)
        rewritten_file.close()
*/

        val manager : ScriptEngineManager = new ScriptEngineManager()
        val engine : ScriptEngine = manager.getEngineByName("javascript")

        val reader : Reader = new BufferedReader(new FileReader(file))
        val builder : StringBuilder = new StringBuilder()
        val chars : Array[Char] = new Array[Char](file.length().toInt)
        var length : Int = reader.read(chars) 

        while (0 < length) {
          builder.appendAll(chars, 0, length)
          length = reader.read(chars)
        }

        val oldOut = System.out
        val oldErr = System.err
        val wt_err = WireTappedPrintStream.make(System.err, true)
        val wt_out = WireTappedPrintStream.make(System.out, true)
        System.setErr(wt_err)
        System.setOut(wt_out)

        val original : String = builder.toString()
        val modified : String = new JSAstToConcrete(new WithRewriter(new Hoister(Parser.parseFileConvertExn(file)).doit.asInstanceOf[Program], true).doit.asInstanceOf[Program]).doit

        val result1 : String = engine.eval(original).toString.replaceAll("\\s+$", "")
        val result2 : String = engine.eval(modified).toString.replaceAll("\\s+$", "")

        System.setErr(oldErr)
        System.setOut(oldOut)

        if (!result1.equals(result2))
          System.out.println("Failed: " + file.getName)// + "\nExpect : " + result1 + "\nActual : " + result2)
                 /*
        if (result1.equals(result2)) System.out.println("Passed: " + file.getName)// + "\nExpect : " + result1 + "\nActual : " + result2)
        else System.out.println("Failed: " + file.getName)// + "\nExpect : " + result1 + "\nActual : " + result2)
                 */

        //Assert.assertFalse("With-rewriting of " + file.getName + " produces a different value: (" + result1 + ", " + result2 + ")", )
      }
    }
  }
}
