/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */
package kr.ac.kaist.jsaf.tests

import java.io.File
import java.io.OutputStream
import java.io.PrintStream

import scala.collection.immutable.HashMap

import junit.framework.Assert.fail
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import kr.ac.kaist.jsaf.analysis.cfg.CFG
import kr.ac.kaist.jsaf.analysis.cfg.CFGBuilder
import kr.ac.kaist.jsaf.analysis.cfg.LExit
import kr.ac.kaist.jsaf.analysis.typing.CallContext
import kr.ac.kaist.jsaf.analysis.typing.Config
import kr.ac.kaist.jsaf.analysis.typing.TypingInterface
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.analysis.typing.PreTyping
import kr.ac.kaist.jsaf.analysis.typing.SparseTyping
import kr.ac.kaist.jsaf.analysis.typing.DSparseTyping
import kr.ac.kaist.jsaf.analysis.typing.Access
import kr.ac.kaist.jsaf.analysis.typing.domain.Absent
import kr.ac.kaist.jsaf.analysis.typing.domain.DomainPrinter
import kr.ac.kaist.jsaf.analysis.typing.domain.GlobalCallsite
import kr.ac.kaist.jsaf.analysis.typing.domain.GlobalLoc
import kr.ac.kaist.jsaf.analysis.typing.domain.PropValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Value
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import kr.ac.kaist.jsaf.analysis.typing.models.BuiltinModel
import kr.ac.kaist.jsaf.compiler.Disambiguator
import kr.ac.kaist.jsaf.compiler.Hoister
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.compiler.Translator
import kr.ac.kaist.jsaf.nodes.IRRoot
import kr.ac.kaist.jsaf.nodes.Program
import kr.ac.kaist.jsaf.scala_src.useful.Options._

import kr.ac.kaist.jsaf.useful.TestCaseWrapper

// class definition for eclipse JUnit runner
class AssertJUTest

object AssertJUTest {
  val SEMANTICS_TESTS_DIR = "tests/typing_tests/assert"
  val EXCLUDE = Set(
    "XXX",
    "NYI"
  )
  val RESULT = "__result"
  val EXPECT = "__expect"

  def main(args: String*) = junit.textui.TestRunner.run(suite)

  def suite(): Test = {
    val suite = new TestSuite("Assert Test")
    val testcases = collectTestcase(SEMANTICS_TESTS_DIR)
    for (tc <- testcases) {
      //$JUnit-BEGIN$
      suite.addTest(new SemanticsTest(SEMANTICS_TESTS_DIR, tc))
      //$JUnit-END$
    }
    suite
  }

  private def collectTestcase(dirname: String) = {
    val dir = FileTests.directoryAsFile(dirname)
    val filtered = dir.list.toSeq.filter(fname =>
      fname.endsWith(".js") &&
      !EXCLUDE.exists(prefix => fname.startsWith(prefix)))
    filtered.sorted
  }

  private class NullOutputStream extends OutputStream {
    override def write(x: Int) = ()
  }

  private class SemanticsTest(dir: String, tc: String) extends TestCase(tc) {
    override def runTest() = {
      // silence stdout, stderr
      val oldOut = System.out
      val oldErr = System.err
      val nullStream = new PrintStream(new NullOutputStream())
      System.setErr(nullStream)
      System.setOut(nullStream)

      try {
        val typing = analyze(new File(dir, tc))
        checkResult(typing);
      } finally {
        // recover stdout, stderr
        System.setErr(oldErr)
        System.setOut(oldOut)
      }
    }

    def analyze(file: File): TypingInterface = {
      val typing_selection = "sparse" // "pre", "dense", "sparse", "dsparse"
      val cc_selection = "1tajs" // "no", "1callsite", "1obj", "1tajs"
      
      // setup testing options
      Config.setTestMode(true)
      Config.setAssertMode(true)

      // parse
      var program: Program = Parser.parseFileConvertExn(file)

      // hoist
      val hoister = new Hoister(program);
      program = hoister.doit().asInstanceOf[Program]

      // disambiguate
      val disambiguator = new Disambiguator(program, false)
      program = disambiguator.doit().asInstanceOf[Program];

      // translate to IR
      val translator = new Translator(program, toJavaOption(None));
      val ir: IRRoot = translator.doit().asInstanceOf[IRRoot];

      // build CFG
      val builder = new CFGBuilder(ir);
      val cfg: CFG = builder.build();

      // initialize heap
      val model = new BuiltinModel(cfg)
      model.initialize()

      // typing
      val typing = typing_selection match {
        case "pre"     => cfg.computeReachableNodes(); new PreTyping(cfg, false)
        case "dense"   => new Typing(cfg)
        case "sparse"  => cfg.computeReachableNodes(); new SparseTyping(cfg, false)
        case "dsparse" => cfg.computeReachableNodes(); new DSparseTyping(cfg, false)
      }
      
      cc_selection match {
        case "no"        => Config.setContextSensitivityMode(Config.Context_Insensitive)
        case "1callsite" => Config.setContextSensitivityMode(Config.Context_OneCallsite)
        case "1obj"      => Config.setContextSensitivityMode(Config.Context_OneObject)
        case "1tajs"     => Config.setContextSensitivityMode(Config.Context_OneObjectTAJS)
      }
      
      Config.setAssertMode(true)
      typing.analyze(model)
      
      typing_selection match {
        case "pre" |"dense" =>
          typing.analyze(model)
        case "sparse" =>
          // pre analysis
          val preTyping = new PreTyping(cfg, false);
          preTyping.analyze(model);
          val pre_result = preTyping.getMergedState()
          // computes def/use set
          val duanalysis = new Access(cfg, preTyping.computeCallGraph(), pre_result);
          duanalysis.process();
          // computes def/use graph
          cfg.drawIntraDefUseGraph(preTyping.computeCallGraph(), duanalysis.result);
          // Analyze
          typing.analyze(model, duanalysis.result);
        case "dsparse" =>
          // pre analysis
          val preTyping = new PreTyping(cfg, false);
          preTyping.analyze(model);
          val pre_result = preTyping.getMergedState()
          // computes def/use set
          val duanalysis = new Access(cfg, preTyping.computeCallGraph(), pre_result);
          duanalysis.process();
          // computes def/use graph
          cfg.drawIntraDDG(preTyping.computeCallGraph(), duanalysis.result);
          // Analyze
          typing.analyze(model, duanalysis.result);
      }

      // return resulting Typing instance
      typing
    }

    def checkResult(typing: TypingInterface) = {
      // find global object at program exit node
      val state = typing.readTable(((typing.cfg.getGlobalFId, LExit), CallContext.globalCallContext))
      val heap = state._1
      val map: Map[String, (PropValue, Absent)] =
        try {
          heap(GlobalLoc).asInstanceOf[Obj].map.toMap
        } catch {
          case _ =>
            fail("Global object is not found at program exit node")
            HashMap()
        }

      // collect result/expect values
      var resultMap: Map[Int, Value] = HashMap()
      var expectMap: Map[Int, Value] = HashMap()
      for ((prop, pvalue) <- map) {
        try {
          if (prop.startsWith(RESULT)) {
            val index = prop.substring(RESULT.length).toInt
            resultMap += (index -> pvalue._1._1._1)
          } else if (prop.startsWith(EXPECT)) {
            val index = prop.substring(EXPECT.length).toInt
            expectMap += (index -> pvalue._1._1._1)
          }
        } catch {
          case _ => fail("Invalid result/expect variable found: " + prop.toString)
        }
      }

      // invalid number of result/expect entries
      if (resultMap.size == 0)
        fail("No result/expect variable is detected")
      if (resultMap.size != expectMap.size)
        fail("Unmatched result/expect variable")

      // check expect <= result
      for ((index, result) <- resultMap.toSeq.sortBy(_._1)) {
        expectMap.get(index) match {
          case None =>
            fail("No corresponding expect variable is detected for " +
                 RESULT +
                 index.toString)
          case Some(expect) =>
            val success = expect <= result
            if (!success) {
              val sb = new StringBuilder
              sb.append(RESULT)
              sb.append(index.toString)
              sb.append(" = {")
              sb.append(DomainPrinter.printValue(result))
              sb.append("} >= {")
              sb.append(DomainPrinter.printValue(expect))
              sb.append("}")
              fail(sb.toString)
            }
        }
      }
    }
  }
}
