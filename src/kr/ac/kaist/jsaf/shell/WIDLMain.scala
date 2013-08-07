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
import kr.ac.kaist.jsaf.analysis.cfg.CFGBuilder
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMBuilder
import kr.ac.kaist.jsaf.bug_detector.{StateManager, BugDetector}
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.exceptions.{ParserError, UserError}
import kr.ac.kaist.jsaf.nodes_util._
import kr.ac.kaist.jsaf.nodes.{IRRoot, WDefinition, Program}
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.useful.{Files, Pair}
import kr.ac.kaist.jsaf.parser.WIDL
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.widl.{WIDLToString, WIDLToDB, WIDLChecker}
import edu.rice.cs.plt.tuple.{Option => JOption}
import org.cyberneko.html.parsers.DOMParser
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
   */
  def widlparse: Int = {
    val return_code = 0
    if (Shell.params.FileNames.length == 0) throw new UserError("The widlparse command needs a file to parse.")
    val name = Shell.params.FileNames(0)
    val widl: ArrayList[WDefinition] = if (name.endsWith(".widl")) parseWidl(name)
                                       else parseDir(name)
    if (Shell.params.opt_DB != null) WIDLToDB.storeToDB(Shell.params.opt_DB, widl)
    else System.out.println(WIDLToString.doit(widl))
    return_code
  }

  val SEP = File.separator
  val widlFilter = new FilenameFilter() {
                       def accept(dir: File, name: String) = name.endsWith(".widl")
                   }
  def parseDir(_dir: String): ArrayList[WDefinition] = {
    var dir = _dir
    if (!dir.endsWith(SEP)) dir += SEP
    val result = new ArrayList[WDefinition]()
    toList(new File(dir).list(widlFilter).map(f => dir+f).toList).foreach(name => result.addAll(parseWidl(name)))
    result                                                                                         
  }

  def parseWidl(fileName: String): ArrayList[WDefinition] = {
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
        parseResult.asInstanceOf[SemanticValue].value.asInstanceOf[ArrayList[WDefinition]]
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
    val JSFileName = Shell.params.FileNames(0)
    val JSFileNameList = new ArrayList[String]; JSFileNameList.add(JSFileName)
    val DBFileNames = Shell.params.FileNames.drop(1).toList

    // AST
    val pair: Pair[Program, HashMap[String, String]] = Parser.fileToAST(JSFileNameList)
    val ast_0: Program = pair.first
    val fileMap: HashMap[String, String] = pair.second
    // IR
    val irErrors = Shell.ASTtoIR(JSFileName, ast_0, JOption.none[String], JOption.none[Coverage])
    val irOpt: JOption[IRRoot] = irErrors.first
    if (irOpt.isNone) return -2
    val ir: IRRoot = irOpt.unwrap
    val ast_n: Program = irErrors.third // Disambiguated and hoisted and with written
    // CFG
    val builder = new CFGBuilder(ir)
    val cfg = builder.build
    val cfgErrors = builder.getErrors
    if (!cfgErrors.isEmpty) return Shell.reportErrors(NodeUtil.getFileName(ir), Shell.flattenErrors(cfgErrors), JOption.none[Pair[FileWriter, BufferedWriter]])
    // Node Relation
    NodeRelation.set(ast_n, ir, cfg, true)
    // DOM, WIDL mode
    Config.setDomMode
    Config.setWIDLMode
    // Initialize AbsString cache
    kr.ac.kaist.jsaf.analysis.typing.domain.AbsString.initCache
    // Initialize Web IDL Libraries(Database) for InitHeap
    WIDLChecker.setLibraries(DBFileNames)
    // InitHeap
    val initHeap = new InitHeap(cfg)
    initHeap.initialize
    // Set the initial state with DOM objects
    val domParser = new DOMParser
    domParser.parse(new org.xml.sax.InputSource(new java.io.StringReader("<HTML></HTML>")))
    new DOMBuilder(cfg, initHeap, domParser.getDocument).initialize(true)
    // Analyze
    WIDLChecker.cfg = cfg
    WIDLChecker.typing = new Typing(cfg, true, false)
    WIDLChecker.typing.analyze(initHeap)
    WIDLChecker.semantics = new Semantics(cfg, Worklist.computes(cfg, true), false)
    WIDLChecker.stateManager = new StateManager(cfg, WIDLChecker.typing, WIDLChecker.semantics)
    // Memory dump
    /*System.out.println("\n* Dump *")
    Config.setVerbose(2)
    WIDLChecker.typing.dump*/
    // BugDetector Test
    /*System.out.println("\n* Bug Detector *")
    NodeRelation.set(pgm2, ir, cfg, false)
    val detector = new BugDetector(cfg, typing, fileMap, false, irErrors.second)
    detector.detectBug*/
    // Run Web IDL checker
    WIDLChecker.doit(ast_n)

    0
  }
}
