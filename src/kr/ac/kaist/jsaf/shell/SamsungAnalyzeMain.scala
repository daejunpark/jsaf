/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import java.io.{BufferedWriter, FileWriter}
import java.util.HashMap
import scala.collection.JavaConversions
import kr.ac.kaist.jsaf.analysis.cfg.{DotWriter, CFG, CFGBuilder}
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMBuilder
import kr.ac.kaist.jsaf.analysis.visualization.Visualization
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.bug_detector.BugDetector
import kr.ac.kaist.jsaf.dtv._
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.nodes.{IRRoot, Program}
import kr.ac.kaist.jsaf.nodes_util._
import kr.ac.kaist.jsaf.{Samsung, ShellParameters}
import kr.ac.kaist.jsaf.useful.{MemoryMeasurer, Pair}
import kr.ac.kaist.jsaf.tests.SemanticsTest
import edu.rice.cs.plt.tuple.{Option => JOption}

////////////////////////////////////////////////////////////////////////////////
// Analyze
//   - PreAnalyze
//   - Sparse
//   - HTML
//   - BugDetector
////////////////////////////////////////////////////////////////////////////////
object SamsungAnalyzeMain {
  /**
   * Analyze a JavaScript file. (Work in progress)
   */
  def analyze: Int = {
    val quiet = Samsung.params.command == ShellParameters.CMD_BUG_DETECTOR ||
                (Samsung.params.command == ShellParameters.CMD_DTV_APP && !Samsung.params.opt_Loud)
    val locclone = Samsung.params.opt_LocClone

    if (Samsung.params.FileNames.length == 0) throw new UserError("Need a file to analyze")
    var fileName: String = Samsung.params.FileNames(0)
    val fileNames = JavaConversions.seqAsJavaList(Samsung.params.FileNames)

    // Analysis Config
    if (Samsung.params.opt_Verbose1) Config.setVerbose(1)
    if (Samsung.params.opt_Verbose2) Config.setVerbose(2)
    if (Samsung.params.opt_Verbose3) Config.setVerbose(3)

    if (Samsung.params.opt_Test) {
      Config.setTestMode(Samsung.params.opt_Test)
      System.out.println("Test mode enabled.")
    }

    if (Samsung.params.opt_Library) {
      Config.setLibMode(Samsung.params.opt_Library)
      System.out.println("Library mode enabled.")
    }

    if (Samsung.params.opt_NoAssert) {
      Config.setAssertMode(!Samsung.params.opt_NoAssert)
      System.out.println("Assert mode disabled.")
    }

    if (Samsung.params.opt_Compare) Config.setCompareMode

    // Context-sensitivity for main analysis
    var context: Int = -1
    context = Config.contextSensitivityMode

    // Temporary parameter setting for bug-detector
    if (Samsung.params.command == ShellParameters.CMD_BUG_DETECTOR) {
      context = Config.Context_OneCallsiteAndObject
      Samsung.params.opt_MultiThread = true
    }

    // Context-sensitivity mode
    if (Samsung.params.opt_ContextInsensitive) context = Config.Context_Insensitive
    else if (Samsung.params.opt_Context1Callsite) context = Config.Context_OneCallsite
    else if (Samsung.params.opt_Context2Callsite) context = Config.Context_KCallsite
    else if (Samsung.params.opt_Context3Callsite) context = Config.Context_KCallsite
    else if (Samsung.params.opt_Context4Callsite) context = Config.Context_KCallsite
    else if (Samsung.params.opt_Context5Callsite) context = Config.Context_KCallsite
    else if (Samsung.params.opt_ContextCallsiteSet) context = Config.Context_CallsiteSet
    else if (Samsung.params.opt_Context1Object) context = Config.Context_OneObject
    else if (Samsung.params.opt_ContextTAJS) context = Config.Context_OneObjectTAJS
    else if (Samsung.params.opt_Context1CallsiteAndObject) context = Config.Context_OneCallsiteAndObject
    else if (Samsung.params.opt_Context2CallsiteAndObject) context = Config.Context_KCallsiteAndObject
    else if (Samsung.params.opt_Context3CallsiteAndObject) context = Config.Context_KCallsiteAndObject
    else if (Samsung.params.opt_Context4CallsiteAndObject) context = Config.Context_KCallsiteAndObject
    else if (Samsung.params.opt_Context5CallsiteAndObject) context = Config.Context_KCallsiteAndObject
    else if (Samsung.params.opt_Context1CallsiteOrObject) context = Config.Context_OneCallsiteOrObject

    Config.setContextSensitivityMode(context)

    // Context-sensitivity depth for k-callsite sensitivity
    if (Samsung.params.opt_Context2Callsite) Config.setContextSensitivityDepth(2)
    else if (Samsung.params.opt_Context3Callsite) Config.setContextSensitivityDepth(3)
    else if (Samsung.params.opt_Context4Callsite) Config.setContextSensitivityDepth(4)
    else if (Samsung.params.opt_Context5Callsite) Config.setContextSensitivityDepth(5)
    else if (Samsung.params.opt_Context2CallsiteAndObject) Config.setContextSensitivityDepth(2)
    else if (Samsung.params.opt_Context3CallsiteAndObject) Config.setContextSensitivityDepth(3)
    else if (Samsung.params.opt_Context4CallsiteAndObject) Config.setContextSensitivityDepth(4)
    else if (Samsung.params.opt_Context5CallsiteAndObject) Config.setContextSensitivityDepth(5)

    // Context-sensitivity for pre-analysis
    if (Samsung.params.opt_PreContextSensitive || Samsung.params.command == ShellParameters.CMD_PREANALYZE) {
      if (!quiet) System.out.println("Context-sensitivity is turned on for pre-analysis.")
      Config.setPreContextSensitiveMode(true)
    }

    // Unrolling count
    Config.setDefaultUnrollingCount(Samsung.params.opt_unrollingCount)

    // Unsound mode
    if (Samsung.params.opt_Unsound) {
      Config.setUnsoundMode(Samsung.params.opt_Unsound)
      System.out.println("Unsound mode enabled.")
    }

    // for HTML
    if (Samsung.params.command == ShellParameters.CMD_HTML ||
        Samsung.params.command == ShellParameters.CMD_HTML_SPARSE) {
      if (Samsung.params.FileNames.length > 1) throw new UserError("Only one HTML file supported at a time.")
      val low = fileName.toLowerCase
      if (!(low.endsWith(".html") || low.endsWith(".xhtml") || low.endsWith(".htm"))) throw new UserError("Not an HTML file.")
      // DOM mode
      Config.setDomMode
    }

    // for Tizen
    if (Samsung.params.opt_Tizen) {
      Config.setTizenMode
      System.out.println("Tizen mode enabled.")
    }

    if (!quiet) System.out.println("Context-sensitivity mode is \"" + kr.ac.kaist.jsaf.analysis.typing.CallContext.getModeName + "\".")

    // Initialize
    val return_code = 0
    val analyzeStartTime = System.nanoTime
    if (!quiet) System.out.println("\n* Initialize *")

    // Initialize AbsString cache
    kr.ac.kaist.jsaf.analysis.typing.domain.AbsString.initCache

    // Read a JavaScript file and translate to IR
    var start = System.nanoTime
    var pair: Pair[Program, HashMap[String, String]] = null

    // for HTML
    var jshtml: JSFromHTML = null
    // for DTV App
    var jsdtv: JSFromDtvApp = null
    var dtv: DTV = null

    if (Samsung.params.command == ShellParameters.CMD_HTML ||
        Samsung.params.command == ShellParameters.CMD_HTML_SPARSE) {
      jshtml = new JSFromHTML(fileName)
      // Parse JavaScript code in the target html file
      pair = jshtml.parseScripts
    }
    else if (Samsung.params.command == ShellParameters.CMD_DTV_APP) {
      Config.setDomMode
      if (!fileName.endsWith("/")) {
        fileName = fileName + '/'
      }
      jsdtv = new JSFromDtvApp(fileName)
      dtv = new DTV()
      dtv.setSceneNames(jsdtv.sceneNames)
      jshtml = new JSFromHTML(fileName + "index.html")
      pair = jshtml.parseScripts(jsdtv.getFileNames)
    }
    else pair = Parser.fileToAST(fileNames)

    var program: Program = pair.first
    var program0: Program = null
    if (Samsung.params.command == ShellParameters.CMD_DTV_APP){
      val apiModeler = new APIModeling()
      program = apiModeler.doit(program)
      val dtvRewriter = new DTVRewriter(dtv)
      program = dtvRewriter.addGlobal(program)
      program = dtvRewriter.doit(program).asInstanceOf[Program]
      program0 = program
      program = dtvRewriter.addKeyDownHandler(program)
      /*
        try{
          ASTIO.writeJavaAst(program, "ast.dump");
          System.out.println("Dumped parse tree to ast.dump.");
          String astcode = JSAstToConcrete.doit(program);
          Pair<FileWriter, BufferedWriter> tmppair = Useful.filenameToBufferedWriter("ast.dump");
          FileWriter fw = tmppair.first();
          BufferedWriter writer = tmppair.second();
          writer.write(astcode);
          writer.close();
          fw.close();
        } catch (IOException e){
          throw new IOException("IOException " + e +
                                "while writing " + params.opt_OutFileName);
        }
      */
    }

    val fileMap: HashMap[String, String] = pair.second
    var irErrors = Samsung.ASTtoIR(fileName, program, JOption.none[String], JOption.none[Coverage])
    var irOpt: JOption[IRRoot] = irErrors.first
    var program2: Program = irErrors.third // Disambiguated and hoisted and with written

    val irTranslationTime = (System.nanoTime - start) / 1000000000.0
    if (!quiet) printf("# Time for IR translation(s): %.2f\n", irTranslationTime)

    // Check the translation result
    if (irOpt.isNone) return -2
    var ir: IRRoot = irOpt.unwrap

    // Build CFG
    start = System.nanoTime
    val builder: CFGBuilder = new CFGBuilder(ir)
    var cfg: CFG = builder.build
    NodeRelation.set(program2, ir, cfg, quiet)
    val cfgBuildingTime = (System.nanoTime - start) / 1000000000.0
    if (!quiet) {
      printf("# Time for CFG building(s): %.2f\n", cfgBuildingTime)
      printf("# Time for front end(s): %.2f\n", irTranslationTime + cfgBuildingTime)
    }
    var errors = builder.getErrors
    if (!(errors.isEmpty)) {
      Samsung.reportErrors(NodeUtil.getFileName(ir), Samsung.flattenErrors(errors), JOption.none[Pair[FileWriter, BufferedWriter]])
    }

    if (!quiet) {
      System.out.println("\n* Analyze *")
      printf("# Initial peak memory(mb): %.2f\n", MemoryMeasurer.peakMemory)
    }

    if (Samsung.params.command == ShellParameters.CMD_DTV_APP){
      System.out.println("build event scenarios...")

      val init = new InitHeap(cfg)
      init.initialize

      (new DOMBuilder(cfg, init, jshtml.getDocument)).initialize

      cfg.computeReachableNodes
      val typingInterface = new DSparseTyping(cfg, quiet, locclone)

      init.checkPredefined

      val preTyping = new PreTyping(cfg, quiet, false)
      preTyping.analyze(init)

      // unsound because states among instructions are omitted.
      val pre_result = preTyping.getMergedState

      val kig = new KeyInfoGatherer(program2)
      val sg = new SceneGraphBuilder(cfg, dtv)
      sg.drawSG(preTyping.computeCallGraph, kig.scene2cfgnode)
      val ssb = new SceneScenarioBuilder(sg.getSceneGraph, sg.getStartScenes)
      program = ssb.doit(program0)

      /*
      try{
        ASTIO.writeJavaAst(program, "ast.dump");
        System.out.println("Dumped parse tree to ast.dump.");

        String astcode = JSAstToConcrete.doit(program);
        Pair<FileWriter, BufferedWriter> tmppair = Useful.filenameToBufferedWriter("ast2.dump");
        FileWriter fw = tmppair.first();
        BufferedWriter writer = tmppair.second();
        writer.write(astcode);
        writer.close();
        fw.close();
        } catch (IOException e){
        throw new IOException("IOException " + e +
        "while writing " + params.opt_OutFileName);
      }
      */

      irErrors = Samsung.ASTtoIR(fileName, program, JOption.none[String], JOption.none[Coverage])
      irOpt = irErrors.first
      program2 = irErrors.third // Disambiguated and hoisted and with written

      if(irOpt.isNone) return -2
      ir = irOpt.unwrap

      // Build a CFG
      System.out.println("rebuild cfg...")
      val builder = new CFGBuilder(ir)
      cfg = builder.build
      NodeRelation.set(program2, ir, cfg, quiet)

      errors = builder.getErrors
      if (!(errors.isEmpty)) {
        Samsung.reportErrors(NodeUtil.getFileName(ir),
                             Samsung.flattenErrors(errors),
                             JOption.none[Pair[FileWriter,BufferedWriter]])
      }
    }

    // compare mode to test the html pre-analysis
    if(Samsung.params.opt_Compare) 
      Config.setCompareMode

    // Initialize bulit-in models
    val previousBasicBlocks: Int = cfg.getNodes.size
    start = System.nanoTime
    //val model: BuiltinModel = new BuiltinModel(cfg);
    //model.initialize();
    val init = new InitHeap(cfg)
    init.initialize

    val builtinModelInitializationTime = (System.nanoTime - start) / 1000000000.0
    val presentBasicBlocks = cfg.getNodes.size
    if (!quiet) {
      System.out.println("# Basic block(#): " + previousBasicBlocks + "(source) + " + (presentBasicBlocks - previousBasicBlocks) + "(bulit-in) = " + presentBasicBlocks)
      printf("# Time for initial heap(s): %.2f\n", builtinModelInitializationTime)
    }

    // Set the initial state with DOM objects
    if (Config.domMode && jshtml != null) new DOMBuilder(cfg, init, jshtml.getDocument).initialize

    if (Samsung.params.command == ShellParameters.CMD_PREANALYZE ||
        Samsung.params.command == ShellParameters.CMD_SPARSE ||
        Samsung.params.command == ShellParameters.CMD_NEW_SPARSE ||
        Samsung.params.command == ShellParameters.CMD_BUG_DETECTOR ||
        Samsung.params.command == ShellParameters.CMD_DTV_APP ||
        Samsung.params.command == ShellParameters.CMD_HTML_SPARSE) {
      cfg.computeReachableNodes(quiet)
    }

    // Create Typing
    var typingInterface: TypingInterface = null
    if (Samsung.params.command == ShellParameters.CMD_ANALYZE ||
        Samsung.params.command == ShellParameters.CMD_HTML) typingInterface = new Typing(cfg, quiet, locclone)
    else if (Samsung.params.command == ShellParameters.CMD_PREANALYZE) typingInterface = new PreTyping(cfg, quiet, true)
    else if (Samsung.params.command == ShellParameters.CMD_SPARSE) typingInterface = new SparseTyping(cfg, quiet, locclone)
    else if (Samsung.params.command == ShellParameters.CMD_NEW_SPARSE ||
             Samsung.params.command == ShellParameters.CMD_BUG_DETECTOR ||
             Samsung.params.command == ShellParameters.CMD_DTV_APP ||
             Samsung.params.command == ShellParameters.CMD_HTML_SPARSE) typingInterface = new DSparseTyping(cfg, quiet, locclone)
    else throw new UserError("Cannot create the Typing. The command is unknown.")

    // Compare with Pre Analysis
    /*
    if (Config.compare && params.command != ShellParameters.CMD_PREANALYZE) {
      Config.setContextSensitivityMode(new Integer(Config.Context_Insensitive))
      val preTyping = new PreTyping(cfg, quiet)
      preTyping.analyze(model)
      Config.setPreTyping(preTyping.state)
      preTyping.dump
    }
    */

    // Check global variables in initial heap against list of predefined variables.
    init.checkPredefined

    // Analyze
    if (Samsung.params.command == ShellParameters.CMD_ANALYZE ||
        Samsung.params.command == ShellParameters.CMD_PREANALYZE ||
        Samsung.params.command == ShellParameters.CMD_HTML) {
      if (Samsung.params.opt_Compare) {
        // compare mode
        val preCFG = builder.build
        //val preModel = new BuiltinModel(preCFG);
        //preModel.initialize();
        val pre_init = new InitHeap(preCFG)
        pre_init.initialize

        // Set the initial state with DOM objects
        if (Config.domMode && (jshtml != null || jsdtv != null))
          new DOMBuilder(preCFG, pre_init, jshtml.getDocument).initialize

        val preTyping: PreTyping = new PreTyping(preCFG, true, false)
        preTyping.analyze(pre_init)
        System.out.println("**PreAnalysis dump**")
        preTyping.dump
        typingInterface.setCompare(preTyping.getMergedState, preTyping.cfg)
      }
      // Analyze
      typingInterface.analyze(init)
    }
    else if (Samsung.params.command == ShellParameters.CMD_SPARSE ||
             Samsung.params.command == ShellParameters.CMD_NEW_SPARSE ||
             Samsung.params.command == ShellParameters.CMD_BUG_DETECTOR ||
             Samsung.params.command == ShellParameters.CMD_DTV_APP ||
             Samsung.params.command == ShellParameters.CMD_HTML_SPARSE) {
      val preTyping = new PreTyping(cfg, quiet, false)
      preTyping.analyze(init)

      // unsound because states among instructions are omitted.
      val pre_result = preTyping.getMergedState
      // computes def/use set
      val access_start = System.nanoTime
      val duanalysis = new Access(cfg, preTyping.computeCallGraph, pre_result)
      duanalysis.process(quiet)
      val accessTime = (System.nanoTime - access_start) / 1000000000.0
      if (!quiet) printf("# Time for access analysis(s): %.2f\n", accessTime)

      // computes def/use graph
      if (typingInterface.env != null) typingInterface.env.drawDDG(preTyping.computeCallGraph, duanalysis.result, quiet)

      // Analyze
      typingInterface.analyze(init, duanalysis.result)
    }

    // Report a result
    if (!quiet) {
      printf("# Peak memory(mb): %.2f\n", MemoryMeasurer.peakMemory)
      printf("# Result heap memory(mb): %.2f\n", MemoryMeasurer.measureHeap)
    }
    if (Samsung.params.opt_MemDump) {
      System.out.println("\n* Dump *")
      typingInterface.dump
      if (Samsung.params.command == ShellParameters.CMD_PREANALYZE) typingInterface.dump_callgraph
    }
    if (Samsung.params.opt_Visual && typingInterface.isInstanceOf[Typing]) {
      System.out.println("\n* Visualization *")
      val vs: Visualization = new Visualization(typingInterface.asInstanceOf[Typing], fileMap, NodeUtil.getFileName(ir), Samsung.toOption(Samsung.params.opt_OutFileName))
      vs.run
    }

    if (!quiet) {
      System.out.println("\n* Statistics *")
      System.out.println("# Total state count: " + typingInterface.getStateCount)
      typingInterface.statistics(Samsung.params.opt_StatDump)
    }
    if (Samsung.params.opt_CheckResult) {
      SemanticsTest.checkResult(typingInterface)
      System.out.println("Test pass")
    }

    // Execute Bug Detector
    System.out.println("\n* Bug Detector *")
    NodeRelation.set(program2, ir, cfg, quiet)
    val detector = new BugDetector(cfg, typingInterface, fileMap, quiet, irErrors.second)
    detector.detectBug

    if (!quiet) printf("\nAnalysis took %.2fs\n", (System.nanoTime - analyzeStartTime) / 1000000000.0)

    val isGlobalSparse = false
    if (Samsung.params.opt_DDGFileName != null) {
      DotWriter.ddgwrite(cfg, typingInterface.env, Samsung.params.opt_DDGFileName + ".dot", Samsung.params.opt_DDGFileName + ".svg", "dot", false, isGlobalSparse)
    }
    if (Samsung.params.opt_DDG0FileName != null) {
      DotWriter.ddgwrite(cfg, typingInterface.env, Samsung.params.opt_DDG0FileName + ".dot", Samsung.params.opt_DDG0FileName + ".svg", "dot", true, isGlobalSparse)
    }
    if (Samsung.params.opt_FGFileName != null) {
      DotWriter.fgwrite(cfg, typingInterface.env, Samsung.params.opt_FGFileName + ".dot", Samsung.params.opt_FGFileName + ".svg", "dot", isGlobalSparse)
    }
    if (!quiet) System.out.println("Ok")

    return_code
  }
}
