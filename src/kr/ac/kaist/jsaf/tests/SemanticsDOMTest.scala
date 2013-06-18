/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */
package kr.ac.kaist.jsaf.tests

import java.io.File
import kr.ac.kaist.jsaf.{Shell, ShellParameters}

//import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.analysis.cfg.CFG
import kr.ac.kaist.jsaf.analysis.cfg.CFGBuilder
import kr.ac.kaist.jsaf.analysis.cfg.LExit
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.models.{ModelManager, BuiltinModel, DOMBuilder}
import kr.ac.kaist.jsaf.compiler.Disambiguator
import kr.ac.kaist.jsaf.compiler.Hoister
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.compiler.Translator
import kr.ac.kaist.jsaf.compiler.WithRewriter
import kr.ac.kaist.jsaf.nodes.IRRoot
import kr.ac.kaist.jsaf.nodes.Program
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.nodes_util.JSFromHTML

class SemanticsDOMTest(dir: String, tc: String) extends SemanticsTest(dir, tc) {
  // Currently, sparse analysis is not supported for analyzing HTML.
  override val TYPING_MODE = "sparse"

  override def analyze(file: File): TypingInterface = {
    Shell.params.Set(Array[String]("html", "-context-1-callsite", "-test"))
    // setup testing options
    Config.setTestMode(true)
    Config.setAssertMode(true)
    // enable DOM
    Config.setDomMode

    // html preprocess, parse
    //val jshtml = new JSFromHTML(file.getPath)
    val jshtml = new JSFromHTML(file.getCanonicalPath)
    var program: Program = jshtml.parseScripts().first

    // hoist
    val hoister = new Hoister(program)
    program = hoister.doit().asInstanceOf[Program]

    // disambiguate
    val disambiguator = new Disambiguator(program, false)
    program = disambiguator.doit().asInstanceOf[Program];

    // with rewrite
    val withRewriter = new WithRewriter(program, false);
    program = withRewriter.doit().asInstanceOf[Program]

    // translate to IR
    val translator = new Translator(program, toJavaOption(None));
    val ir: IRRoot = translator.doit().asInstanceOf[IRRoot];

    // build CFG
    val builder = new CFGBuilder(ir);
    val cfg: CFG = builder.build();

    // initialize heap
    //val model = new BuiltinModel(cfg)
    //model.initialize()
    val init = new InitHeap(cfg)
    init.initialize()

    val dom_model = new DOMBuilder(cfg, init, jshtml.getDocument())
    dom_model.initialize();

    // typing
    val typing = 
      TYPING_MODE match {
        case "pre"     => cfg.computeReachableNodes(); new PreTyping(cfg, false, true)
        case "dense"   => new Typing(cfg, false)
        case "sparse"  => cfg.computeReachableNodes(); new SparseTyping(cfg, false, false)
        case "dsparse" => cfg.computeReachableNodes(); new DSparseTyping(cfg, false, false)
      }

    CONTEXT_SENSITIVITY match {
      case "no"        => Config.setContextSensitivityMode(Config.Context_Insensitive)
      case "1callsite" => Config.setContextSensitivityMode(Config.Context_OneCallsite)
      case "1obj"      => Config.setContextSensitivityMode(Config.Context_OneObject)
      case "1tajs"     => Config.setContextSensitivityMode(Config.Context_OneObjectTAJS)
    }

    Config.setAssertMode(true)
    typing.analyze(init)

    TYPING_MODE match {
      case "pre" |"dense" =>
        typing.analyze(init)
      case "sparse" | "dsparse" =>
        // pre analysis
        val preTyping = new PreTyping(cfg, false, false);
        preTyping.analyze(init);
        val pre_result = preTyping.getMergedState
        // computes def/use set
        val duanalysis = new Access(cfg, preTyping.computeCallGraph(), pre_result);
        duanalysis.process();
        // computes def/use graph
        typing.env.drawDDG(preTyping.computeCallGraph(), duanalysis.result)
        // Analyze
        typing.analyze(init, duanalysis.result);
    }

    // return resulting Typing instance
    typing
  }
}
