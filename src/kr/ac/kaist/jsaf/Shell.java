/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.iter.IterUtil;

import xtc.parser.SemanticValue;
import xtc.parser.ParseError;
import xtc.parser.Result;

import kr.ac.kaist.jsaf.analysis.cfg.CFGBuilder;
import kr.ac.kaist.jsaf.analysis.cfg.CFG;
import kr.ac.kaist.jsaf.analysis.cfg.DotWriter;
import kr.ac.kaist.jsaf.analysis.typing.*;
import kr.ac.kaist.jsaf.analysis.typing.domain.State;
import kr.ac.kaist.jsaf.analysis.typing.models.BuiltinModel;
import kr.ac.kaist.jsaf.analysis.typing.models.DOMBuilder;
import kr.ac.kaist.jsaf.analysis.visualization.*;
import kr.ac.kaist.jsaf.bug_detector.BugDetector;
import kr.ac.kaist.jsaf.bug_detector.BugInfo;
import kr.ac.kaist.jsaf.clone_detector.CloneDetector;
import kr.ac.kaist.jsaf.compiler.Disambiguator;
import kr.ac.kaist.jsaf.compiler.Hoister;
import kr.ac.kaist.jsaf.compiler.module.ModuleRewriter;
import kr.ac.kaist.jsaf.compiler.Parser;
import kr.ac.kaist.jsaf.compiler.Predefined;
import kr.ac.kaist.jsaf.compiler.StrictModeChecker;
import kr.ac.kaist.jsaf.compiler.Translator;
import kr.ac.kaist.jsaf.compiler.WithRewriter;
import kr.ac.kaist.jsaf.concolic.Instrumentor;
import kr.ac.kaist.jsaf.concolic.Z3;
import kr.ac.kaist.jsaf.exceptions.MultipleStaticError;
import kr.ac.kaist.jsaf.exceptions.ParserError;
import kr.ac.kaist.jsaf.exceptions.StaticError;
import kr.ac.kaist.jsaf.exceptions.UserError;
import kr.ac.kaist.jsaf.exceptions.WrappedException;
import kr.ac.kaist.jsaf.interpreter.Interpreter;

import kr.ac.kaist.jsaf.nodes.Program;
import kr.ac.kaist.jsaf.nodes.IRRoot;
import kr.ac.kaist.jsaf.nodes.WDefinition;
import kr.ac.kaist.jsaf.nodes_util.ASTIO;
import kr.ac.kaist.jsaf.nodes_util.Coverage;
import kr.ac.kaist.jsaf.nodes_util.IRFactory;
import kr.ac.kaist.jsaf.nodes_util.JSFromUrl;
import kr.ac.kaist.jsaf.nodes_util.JSFromHTML;
import kr.ac.kaist.jsaf.nodes_util.JSAstToConcrete;
import kr.ac.kaist.jsaf.nodes_util.JSIRUnparser;
import kr.ac.kaist.jsaf.nodes_util.NodeUtil;
import kr.ac.kaist.jsaf.nodes_util.WIDLChecker;
import kr.ac.kaist.jsaf.nodes_util.WIDLToDB;
import kr.ac.kaist.jsaf.nodes_util.WIDLToString;
import kr.ac.kaist.jsaf.parser.WIDL;
import kr.ac.kaist.jsaf.tests.FileTests;
import kr.ac.kaist.jsaf.tests.SemanticsTest;
import kr.ac.kaist.jsaf.useful.Files;
import kr.ac.kaist.jsaf.useful.Pair;
import kr.ac.kaist.jsaf.useful.Triple;
import kr.ac.kaist.jsaf.useful.Useful;
import kr.ac.kaist.jsaf.useful.MemoryMeasurer;
import kr.ac.kaist.jsaf.scala_src.useful.WorkManager;
import org.w3c.dom.Node;

public final class Shell {
    ////////////////////////////////////////////////////////////////////////////////
    // Settings and Environment variables
    ////////////////////////////////////////////////////////////////////////////////
    public static boolean                       debug = false;

    public static ShellParameters               params = new ShellParameters();
    private static boolean                      opt_DisambiguateOnly = false;
    private static String                       printTimeTitle = null;
    private static long                         startTime;

    public static WorkManager                   workManager = new WorkManager();
    public static Predefined                    pred;

    ////////////////////////////////////////////////////////////////////////////////
    // Main Entry point
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Create the program parameters for runtime debugging. (Ex: Eclipse)
     */
    private static String[] createDebugParameter(String[] originalTokens) {
        if(!debug) return originalTokens;

        ArrayList<String> tokens = new ArrayList<String>();
        tokens.add("interpret");
        tokens.add("tests/interpreter_tests/arith.js");

        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * Main entry point for the jsaf shell.
     * In order to support accurate testing of error messages, this method immediately
     * forwards to its two parameter helper method.
     * *** Please do not directly add code to this method, as it will interfere with testing.
     * *** Tests will silently fail.
     * *** Instead, add code to its helper method.
     */
    public static void main(String[] tokens) throws Throwable {
        tokens = createDebugParameter(tokens);
        /*
        for (String token : tokens) {
            System.out.println(token);
        }
        */
        // Call the internal main function
        main(false, tokens);
    }

    /**
     * Helper method that allows main to be called from tests
     * (without having to worry about System.exit).
     */
    public static void main(boolean runFromTests, String[] tokens) throws Throwable {
        int return_code = -1;

        // If there is no parameter then just print a usage message.
        if(tokens.length == 0) printUsageMessage();
        else return_code = subMain(tokens);

        // If there is an error and this main function is not called by the test
        //   then call the System.exit function to return the error code.
        if(return_code != 0 && !runFromTests) System.exit(return_code);
    }

    public static int subMain(String[] tokens) throws Throwable {
        // Now match the assembled string.
        int return_code = 0;
        try {
            // Parse parameters
            String errorMessage = params.Set(tokens);
            if(errorMessage != null) throw new UserError(errorMessage);
            pred = new Predefined(params);

            // Set the start time.
            startTime = System.currentTimeMillis();

            switch(params.command) {
            default :
            case ShellParameters.CMD_USAGE :
                printUsageMessage();
                break;
            case ShellParameters.CMD_PARSE :
                return_code = parse();
                break;
            case ShellParameters.CMD_UNPARSE :
                unparse();
                break;
            case ShellParameters.CMD_WIDLPARSE :
                return_code = widlparse();
                break;
            case ShellParameters.CMD_WIDLCHECK :
                return_code = widlcheck();
                break;
            case ShellParameters.CMD_STRICT :
                return_code = strict();
                break;
            case ShellParameters.CMD_CLONE_DETECTOR :
                return_code = cloneDetector();
                break;
            case ShellParameters.CMD_COVERAGE :
                return_code = coverage();
                break;
            case ShellParameters.CMD_CONCOLIC :
                return_code = concolic();
                break;
            case ShellParameters.CMD_URL :
                return_code = url();
                break;
            case ShellParameters.CMD_WITH :
                return_code = with();
                break;
            case ShellParameters.CMD_MODULE :
                return_code = module();
                break;
            case ShellParameters.CMD_JUNIT :
                return_code = junit();
                break;
            case ShellParameters.CMD_DISAMBIGUATE :
                opt_DisambiguateOnly = true;
                return_code = compile();
                break;
            case ShellParameters.CMD_COMPILE :
                compile();
                break;
            case ShellParameters.CMD_CFG :
                return_code = cfgBuilder();
                break;
            case ShellParameters.CMD_INTERPRET :
                return_code = interpret();
                break;
            case ShellParameters.CMD_ANALYZE :
                return_code = analyze();
                break;
            case ShellParameters.CMD_PREANALYZE :
                return_code = analyze();
                break;
            case ShellParameters.CMD_SPARSE :
                return_code = analyze();
                break;
            case ShellParameters.CMD_NEW_SPARSE :
                return_code = analyze();
                break;
            case ShellParameters.CMD_HTML :
            case ShellParameters.CMD_HTML_SPARSE :
                return_code = analyze();
                break;
            case ShellParameters.CMD_BUG_DETECTOR :
                return_code = analyze();
                break;
            case ShellParameters.CMD_HELP :
                printHelpMessage();
                break;
            }
        } catch (ParserError e) {
            System.err.println(e);
            return_code = -1;
        } catch (StaticError e) {
            System.err.println(e);
            return_code = -1;
        } catch (UserError e) {
            System.err.println(e);
            return_code = -1;
        } catch (IOException error) {
            System.err.println(error.getMessage());
            return_code = -2;
        }

        // Print elapsed time.
        if(printTimeTitle != null)
            System.out.println(printTimeTitle + " took " + (System.currentTimeMillis() - startTime) + "ms.");

        return return_code;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Usage and Help messages
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Helper method to print usage message.
     */
    private static void printUsageMessage() {
        System.err.print(
            "Usage:\n" +
            " parse [-out file] [-time] somefile.js ...\n" +
            " unparse [-out file] somefile.tjs\n" +
            " widlparse [-db] somefile.widl\n" +
            " widlcheck somefile.js api1 ...\n" +
            " strict [-out file] somefile.js\n" +
            " clone-detector\n" +
            " coverage somefile.js\n" +
            " concolic somefile.js\n" +
            " url [-out file] someurl\n" +
            " with [-out file] somefile.js ...\n" +
            " module [-out file] somefile.js ...\n" +
            " junit sometest.test ...\n" +
            " disambiguate [-out file] somefile.js ...\n" +
            " compile [-out file] [-time] somefile.js ...\n" +
            " cfg [-out file] [-test] [-model] [-dom] somefile.js ...\n" +
            " interpret [-out file] [-time] [-mozilla] somefile.js ...\n" +
            " analyze [-verbose] [-test] [-memdump] [-statdump] [-visual] [-checkResult]\n" +
            "         [-context-insensitive] [-context-1-callsite] [-context-1-object]\n" +
            "         [-context-tajs] [-unsound]\n" +
            "         somefile.js\n" +
            " bug-detector somefile.js\n" +
            "\n" +
            " help\n"
        );
    }

    /**
     * Helper method to print help message.
     */
    private static void printHelpMessage() {
        System.err.print
        ("Invoked as script: jsf args\n"+
         "Invoked by java: java ... kr.ac.kaist.jsaf.Shell args\n"+
         "jsaf parse [-out file] [-time] somefile.js ...\n"+
         "  Parses files. If parsing succeeds the message \"Ok\" will be printed.\n"+
         "  The files are concatenated in the given order before being parsed.\n"+
         "  If -out file is given, the parsed AST will be written to the file.\n"+
         "  If -time is given, the time it takes will be printed.\n"+
         "\n"+
         "jsaf unparse [-out file] somefile.tjs\n"+
         "  Converts a parsed file back to JavaScript source code. The output will be dumped to stdout if -out is not given.\n"+
         "  If -out file is given, the unparsed source code will be written to the file.\n"+
         "\n"+
         "jsaf widlparse [-db] somefile.widl\n"+
         "  Parses a Web IDL file.\n"+
         "  If -out file is given, the parsed AST will be written to the file.\n"+
         "\n"+
         "jsaf widlcheck somefile.js api1.db ..."+
         "  Checks uses of APIS in Web IDL.\n"+
         "\n"+
         "jsaf strict [-out file] somefile.js\n"+
         "  Checks whether a file satisfies the strict mode restrictions.\n"+
         "  If it succeeds the message \"Ok\" will be printed.\n"+
         "  If -out file is given, the messages about what restrictions are violated, if any, will be printed.\n"+
         "\n"+
         "jsaf clone-detector"+
         "  Runs the JavaScript clone detector.\n"+
         "\n"+
         "jsaf coverage somefile.js"+
         "  Calculates a very simple statement coverage.\n"+
         "\n"+
         "jsaf concolic somefile.js"+
         "  Working on a very simple concolic testing...\n"+
         "\n"+
         "jsaf url [-out file] someurl"+
         "  Extracts JavaScript source code from a url and writes it to a file, if any.\n"+
         "  If -out file is given, the extracted source code will be written to the file.\n"+
         "\n"+
         "jsaf with [-out file] somefile.js ...\n"+
         "  Rewrites JavaScript source codes using the with statement to another one without using the with statement.\n"+
         "  If it succeeds the message \"Ok\" will be printed.\n"+
         "  The files are concatenated in the given order before being parsed.\n"+
         "  If -out file is given, the rewritten source code will be written to the file.\n"+
         "\n"+
         "jsaf module [-out file] somefile.js ...\n"+
         "  Rewrites JavaScript source codes using the module syntax to another one without using the module syntax.\n"+
         "  The files are concatenated in the given order before being parsed.\n"+
         "  If -out file is given, the rewritten source code will be written to the file.\n"+
         "\n"+
         "jsaf junit somefile1.test ...\n"+
         "  Runs the system test file(s) somefile1.test (etc) in a junit textui harness.\n"+
         "\n"+
         "jsaf disambiguate [-out file] somefile.js ...\n"+
         "  Disambiguates references in JavaScript source files."+
         "  The files are concatenated in the given order before being parsed.\n"+
         "  If -out file is given, the disambiguated AST will be written to the file.\n"+
         "\n"+
         "jsaf compile [-out file] [-time] somefile.js ...\n"+
         "  Translates JavaScript source files to IR."+
         "  If the compilation succeeds the message \"Ok\" will be printed.\n"+
         "  The files are concatenated in the given order before being parsed.\n"+
         "  If -out file is given, the resulting IR will be written to the file.\n"+
         "  If -time is given, the time it takes will be printed.\n"+
         "\n"+
         "jsaf cfg [-out file] [-test] [-model] [-library] somefile.js ...\n"+
         "  Builds a control flow graph for JavaScript source files.\n"+
         "  The files are concatenated in the given order before being parsed.\n"+
         "  If -out file is given, the resulting CFG will be written to the file.\n"+
         "  If -test is specified, predefined values for testing purpose will be provided.\n" +
         "  If -model is specified, the resulting CFG will include built-in models.\n" +
         "  If -library is specified, ...\n" +
         "\n"+
         "jsaf interpret [-out file] [-time] [-mozilla] somefile.js ...\n"+
         "  Interprets JavaScript files.\n"+
         "  If the interpretation succeeds the result will be printed.\n"+
         "  The files are concatenated in the given order before being parsed.\n"+
         "  If -out file is given, the parsed IR will be written to the file.\n"+
         "  If -time is given, the time it takes will be printed.\n"+
         "  If -mozilla is given, the shell files are prepended.\n"+
         "\n"+
         "jsaf analyze [-verbose] [-test] [-memdump] [-statdump] [-visual] [-checkResult]\n"+
         "             [-context-insensitive] [-context-1-callsite] [-context-1-object]\n"+
         "             [-context-tajs] [-unsound]\n"+
         "             somefile.js\n"+
         "  Analyzes a JavaScript source.\n"+
         "  If -verbose is specified, analysis results will be printed in verbose format.\n" +
         "  If -test is specified, predefined values for testing purpose will be provided.\n" +
         "  If -memdump is specified, result memory will be dumped to screen.\n"+
         "  If -statdump is specified, statistics will be printed in dump format.\n"+
         "  If -visual is specified, result will be printed in web-based visualization format.\n"+
         "  If -checkResult is specified, expected result will be checked as in unit tests.\n"+
         "  If -context-insensitive is specified, context-sensitivity will be turned-off.\n" +
         "  If -context-1-callsite is specified, context-sensitivity will distinguish last callsite.\n" +
         "  If -context-1-object is specified, context-sensitivity will distinguish this values at last callsite.\n" +
         "  If -context-tajs is specified, TAJS-style 1-object context-sensitivity will be used.\n" +
         "  If -unsound is specified, unsound semantics is used.\n" +
         "jsaf bug-detector somefile.js\n"+
         "  Reports possible bugs in JavaScript source files.\n"
        );
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 1. Parse
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Parse a file. If the file parses ok it will say "Ok".
     * If you want a dump then give -out somefile.
     */
    private static int parse() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("Need a file to parse");
        List<String> fileNames = Arrays.asList(params.FileNames);

        int return_code = 0;
        try {
            Pair<Program, HashMap<String, String>> pair = Parser.fileToAST(fileNames);
            Program pgm = pair.first();
            System.out.println("Ok");
            if (params.opt_OutFileName != null){
                try{
                    ASTIO.writeJavaAst(pgm, params.opt_OutFileName);
                    System.out.println("Dumped parse tree to " + params.opt_OutFileName);
                } catch (IOException e){
                    throw new IOException("IOException " + e +
                            "while writing " + params.opt_OutFileName);
                }
            }
        } catch (FileNotFoundException f) {
            throw new UserError(f + " not found");
        }
        if (params.opt_Time) printTimeTitle = "Parsing";
        return return_code;
    }

    public static int parse(String fileName, String outFileName) throws UserError, InterruptedException, IOException {
        params.Clear();
        params.opt_OutFileName = outFileName;
        params.FileNames = new String[1];
        params.FileNames[0] = fileName;
        return parse();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 2. UnParse
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * UnParse a file.
     * If you want a dump then give -out somefile.
     */
    private static void unparse() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The unparse command needs a file to unparse.");
        String fileName = params.FileNames[0];

        Option<Program> result = ASTIO.readJavaAst(fileName);
        if (result.isSome()) {
            String code = JSAstToConcrete.doit(result.unwrap());
            if (params.opt_OutFileName != null){
                try{
                    Pair<FileWriter, BufferedWriter> pair = Useful.filenameToBufferedWriter(params.opt_OutFileName);
                    FileWriter fw = pair.first();
                    BufferedWriter writer = pair.second();
                    writer.write(code);
                    writer.close();
                    fw.close();
                } catch (IOException e){
                    throw new IOException("IOException " + e +
                                          "while writing " + params.opt_OutFileName);
                }
            } else {
                System.out.println(code);
            }
        } else {
            System.out.println("Error! Reading the " + fileName + " file failed!");
        }
    }

    public static void unparse(String fileName, String outFileName) throws UserError, InterruptedException, IOException {
        params.Clear();
        params.opt_OutFileName = outFileName;
        params.FileNames = new String[1];
        params.FileNames[0] = fileName;
        unparse();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 3. Strict Mode Checker
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Check whether a given program satisfies
     * the ECMAScript 5 strict mode restrictions.
     * If you want to dump what restrictions are not satisfied,
     * then give -out somefile.
     * Not yet fully implemented.
     */
    private static int strict() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The unparse command needs a file to unparse.");
        List<String> fileNames = Arrays.asList(params.FileNames);

        Pair<Program,HashMap<String, String>> pair = Parser.fileToAST(fileNames);
        Program pgm = pair.first();
        List<StaticError> errors = new StrictModeChecker(pgm).doit();
        if (params.opt_OutFileName != null){
            try {
                Pair<FileWriter, BufferedWriter> ppair = Useful.filenameToBufferedWriter(params.opt_OutFileName);
                return reportErrors(NodeUtil.getFileName(pgm),
                                    flattenErrors(errors), Option.<Pair<FileWriter, BufferedWriter>>some(ppair));
            } catch (IOException e){
                throw new IOException("IOException " + e +
                                      "while writing " + params.opt_OutFileName);
                }
        } else {
            return reportErrors(NodeUtil.getFileName(pgm),
                                flattenErrors(errors), Option.<Pair<FileWriter, BufferedWriter>>none());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 4. Clone Detector
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Reports detected clones in the file.
     */
    private static int cloneDetector() throws UserError, InterruptedException, IOException {
        CloneDetector.doit();
        return 0;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 5. Code Coverage
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Calculates a very simple statement coverage.
     */
    private static int coverage() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The coverage command needs a file to calculate code coverage.");
        List<String> fileNames = Arrays.asList(params.FileNames);

        int return_code = 0;
        Coverage coverage = new Coverage();
        Option<IRRoot> irOpt = fileToIR(fileNames, Option.<String>none(), Option.<Coverage>some(coverage)).first();
        if (irOpt.isSome()) {
            IRRoot ir = irOpt.unwrap();
            /*
             * The following 2 lines are to print IR program for debug.
             * Check getE method in nodes_util/JSIRUnparser.scala to get unsimplified name.
             */
            /*
            String ircode = new JSIRUnparser(ir).doit();
            System.out.println(ircode);
            */
            // Interpret ir...
            new Interpreter().doit(ir, Option.<Coverage>some(coverage), true);
            // Calculate the coverage!
            System.out.println("Total statements: " + coverage.total());
            System.out.println("Executed statements: " + coverage.executed());
            //System.out.println("Coverage percentage: " + coverage.executed/coverage.total);
            return return_code;
        } else return -2;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 6. Concolic Test
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Working on a very simple concolic testing...
     */
    private static int concolic() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The concolic command needs a file to perform concolic testing.");
        List<String> fileNames = Arrays.asList(params.FileNames);

        int return_code = 0;
        Coverage coverage = new Coverage();
        Option<IRRoot> irOpt = fileToIR(fileNames, Option.<String>none(), Option.<Coverage>some(coverage)).first();
        if (irOpt.isSome()) {
            IRRoot ir = irOpt.unwrap();
            ir = new Instrumentor(ir).doit();
            //ASTIO.writeJavaAst(ir, "System.out", System.out);

            //TODO: Rotate until reaching the coverage or having limited depth
            //Temporarily execute two round
            Z3 z3 = new Z3();
            Interpreter interpreter = new Interpreter();
			do {
				do {
					System.out.println();
					Option<List<Integer>> result = z3.solve(coverage.getConstraints(), coverage.inputNum());
					if (result.isSome())
						coverage.setInput(result.unwrap());
					interpreter.doit(ir, Option.<Coverage>some(coverage), true);
				} while (coverage.cont());
				coverage.removeTarget();	
			} while (coverage.existCandidate());
            return return_code;
        } else return -2;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 7. JavaScript from URL
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Extracts JavaScript source code from a url, if any.
     * If -out file is given, the extracted source code will be written to the file.
     */
    private static int url() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The url command needs a url of html page.");

        new JSFromUrl(params.FileNames[0], params.opt_OutFileName).doit();
        return 0;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 8. with Rewriter
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Rewrite a JavaScript source code using the with statement
     * to another one without using the with statement.
     * If you want to dump the rewritten code,
     * then give -out somefile.
     * Not yet fully implemented.
     */
    private static int with() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The with command needs a file to rewrite.");
        List<String> fileNames = Arrays.asList(params.FileNames);

        Pair<Program, HashMap<String, String>> pair = Parser.fileToAST(fileNames);
        Program program = pair.first();
        program = (Program)new Hoister(program).doit();
        program = (Program)new Disambiguator(program, opt_DisambiguateOnly).doit();
        program = (Program)new WithRewriter(program, false).doit();
        String rewritten = JSAstToConcrete.doit(program);
        if (params.opt_OutFileName != null){
            try{
                Pair<FileWriter, BufferedWriter> ppair = Useful.filenameToBufferedWriter(params.opt_OutFileName);
                FileWriter fw = ppair.first();
                BufferedWriter writer = ppair.second();
                writer.write(rewritten);
                writer.close();
                fw.close();
            } catch (IOException e){
                throw new IOException("IOException " + e +
                                      "while writing " + params.opt_OutFileName);
            }
        } else {
            System.out.println(rewritten);
        }
        return 0;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 9. Module Rewriter
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Rewrite a JavaScript source code using the module syntax
     * to another one without using the module syntax.
     * If you want to dump the rewritten code,
     * then give -out somefile.
     * Not yet fully implemented.
     */
    private static int module() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The module command needs a file to rewrite.");
        List<String> fileNames = Arrays.asList(params.FileNames);

        params.opt_Module = true;
        params.opt_IgnoreErrorOnAST = true;

        Pair<Program, HashMap<String, String>> pair = Parser.fileToAST(fileNames);
        Program program = pair.first();
        program = (Program)new ModuleRewriter(program).doit();
        String rewritten = JSAstToConcrete.doit(program);
        if (params.opt_OutFileName != null){
            try{
                Pair<FileWriter, BufferedWriter> ppair = Useful.filenameToBufferedWriter(params.opt_OutFileName);
                FileWriter fw = ppair.first();
                BufferedWriter writer = ppair.second();
                writer.write(rewritten);
                writer.close();
                fw.close();
            } catch (IOException e){
                throw new IOException("IOException " + e +
                                      "while writing " + params.opt_OutFileName);
            }
        } else {
            System.out.println(rewritten);
        }
        return 0;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 10. junit Test
    ////////////////////////////////////////////////////////////////////////////////
    private static int junit() throws UserError, IOException {
        if (params.FileNames.length == 0) throw new UserError("Need a file to run junit tests.");
        List<String> fileNames = Arrays.asList(params.FileNames);
        junit.textui.TestRunner.run(FileTests.suiteFromListOfFiles(fileNames, "", "", "", true, false));
        return 0;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 11. Disambiguate and
    // 12. Compile
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Compile a file. If the file compiles ok it will say "Ok".
     * If you want a dump then give -out somefile.
     */
    private static int compile() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("Need a file to compile.");
        List<String> fileNames = Arrays.asList(params.FileNames);

        int return_code = 0;
        Option<IRRoot> irOpt = fileToIR(fileNames, toOption(params.opt_OutFileName));
        if (irOpt.isSome()) {
            if (opt_DisambiguateOnly) return 0;
            IRRoot ir = irOpt.unwrap();
            String ircode = new JSIRUnparser(ir).doit();
            if (params.opt_OutFileName != null){
                String outfile = params.opt_OutFileName;
                try{
                    Pair<FileWriter, BufferedWriter> pair = Useful.filenameToBufferedWriter(outfile);
                    FileWriter fw = pair.first();
                    BufferedWriter writer = pair.second();
                    // IR dumping
                    ASTIO.writeJavaAst(ir, outfile);
                    //writer.write(ircode);
                    writer.close();
                    fw.close();
                    System.out.println("Dumped IR to " + outfile);
                } catch (IOException e){
                    throw new IOException("IOException " + e +
                                          "while writing " + outfile);
                }
            } else System.out.println(ircode);
        } else return -2;
        if (params.opt_Time) printTimeTitle = "Compilation";
        return return_code;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 13. CFG Builder
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Build a control flow graph.
     * If you want a dump then give -out somefile.
     */
    private static int cfgBuilder() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("Need a file to build a control flow graph.");
        List<String> fileNames = Arrays.asList(params.FileNames);

        if (params.opt_Test) {
            Config.setTestMode(new Boolean(params.opt_Test));
            System.out.println("Test mode enabled.");
        }

        if(params.opt_Dom) {
            Config.setDomMode();
            System.out.println("DOM mode enabled.");
        }

		if(params.opt_Tizen) {
            Config.setTizenMode();
            System.out.println("Tizen mode enabled.");
        }

        if(params.opt_Library) {
            Config.setLibMode(new Boolean(params.opt_Library));
            System.out.println("Library mode enabled.");
        }

        int return_code = 0;
        Option<IRRoot> irOpt = fileToIR(fileNames, toOption(params.opt_OutFileName));
        if (irOpt.isSome()) {
            IRRoot ir = irOpt.unwrap();
            CFGBuilder builder = new CFGBuilder(ir);
            CFG cfg = builder.build();
            List<StaticError> errors = builder.getErrors();
            if (!(errors.isEmpty())) {
                reportErrors(NodeUtil.getFileName(ir),
                             flattenErrors(errors),
                             Option.<Pair<FileWriter,BufferedWriter>>none());
            }
            if (params.opt_Model) {
                //BuiltinModel builtinmodel = new BuiltinModel(cfg);
                //builtinmodel.initialize();
                InitHeap init = new InitHeap(cfg);
                init.initialize();
            }
            if (params.opt_OutFileName != null){
                String outfile = params.opt_OutFileName;
                DotWriter.write(cfg, outfile+".dot", outfile+".svg", "dot");
            } else {
                cfg.dump();
            }
        } else return -2;
        return return_code;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 14. Interpreter
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Interpret a JavaScript file. (Work in progress)
     * If the file interprets ok it will print the result.
     */
    private static int interpret() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("Need a file to interpret.");
        List<String> fileNames = Arrays.asList(params.FileNames);
        boolean printComp;
        if (!params.opt_Mozilla) printComp = true;
        else {
            printComp = false;
            File f1 = new File(fileNames.get(0));
            String safe = "";
            fileNames = new ArrayList<String>();
            List<String> tmp = new ArrayList<String>();
            if (f1.canRead()) {
                tmp.add(f1.getCanonicalPath());
                f1 = f1.getParentFile();
                while (f1 != null) {
                    String[] list = f1.list();
                    boolean done = true;
                    for (int i = list.length-1; i >= 0; i--) {
                        if (list[i].equals("shell.js")) {
                            done = false;
                            break;
                        }
                    }
                    if (done) {
                        if (safe != "") tmp.add(safe);
                        break;
                    }
                    tmp.add(f1.getCanonicalPath() + "/shell.js");
                    safe = f1.getCanonicalPath() + "/safe.js";
                    f1 = f1.getParentFile();
                }
            }
            for (int i = tmp.size()-1; i >= 0; i--)
                fileNames.add(tmp.get(i));
        }

        int return_code = 0;
        Option<IRRoot> irOpt = fileToIR(fileNames, toOption(params.opt_OutFileName));
        if (irOpt.isSome()) {
            IRRoot ir = irOpt.unwrap();
            /*
             * The following 2 lines are to print IR program for debug.
             * Check getE method in nodes_util/JSIRUnparser.scala to get unsimplified name.
             */
            /*
            String ircode = new JSIRUnparser(ir).doit();
            System.out.println(ircode);
            */
            // Interpret ir...
            new Interpreter().doit(ir, Option.<Coverage>none(), printComp);
            if (params.opt_Time) printTimeTitle = "Interpretation";
        }
        return return_code;
    }

        /*
         * for debugging IR itself using the IR parser
         *
        BufferedReader in = Useful.utf8BufferedFileReader(new File(file));
        try {
            IR parser = new IR(in, file);
            xtc.parser.Result parseResult = parser.pFile(0);
            if (parseResult.hasValue()) {
                IRRoot root = (IRRoot)((SemanticValue) parseResult).value;
                // Interpret irs...
                new Interpreter(root).doit();

                if (out.isSome()){
                    String outfile = out.unwrap();
                    try{
                        for (IRStmt ir : root.getIrs())
                            ASTIO.writeJavaAst(ir, outfile);
                        System.out.println("Dumped IR to " + outfile);
                    } catch (IOException e){
                        throw new IOException("IOException " + e +
                                              "while writing " + outfile);
                    }
                }
            } else throw new ParserError((ParseError)parseResult, parser);
        } finally {
            try {
                in.close();
            } catch (IOException e) {}
        }
        */

    ////////////////////////////////////////////////////////////////////////////////
    // 15. Analyze
    //     - PreAnalyze
    //     - Sparse
    //     - HTML
    //     - BugDetector
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Analyze a JavaScript file. (Work in progress)
     */
    private static int analyze() throws UserError, InterruptedException, IOException {
        boolean quiet = params.command == ShellParameters.CMD_BUG_DETECTOR;
        boolean locclone = params.opt_LocClone;

        if (params.FileNames.length == 0) throw new UserError("Need a file to analyze");
        String fileName = params.FileNames[0];
        List<String> fileNames = Arrays.asList(params.FileNames);

        if(params.opt_Verbose1) Config.setVerbose(1);
        if(params.opt_Verbose2) Config.setVerbose(2);
        if(params.opt_Verbose3) Config.setVerbose(3);

        if(params.opt_Test) {
            Config.setTestMode(new Boolean(params.opt_Test));
            System.out.println("Test mode enabled.");
        }

        if(params.opt_Library) {
            Config.setLibMode(new Boolean(params.opt_Library));
            System.out.println("Library mode enabled.");
        }

        if(params.opt_NoAssert) {
            Config.setAssertMode(new Boolean(!params.opt_NoAssert));
            System.out.println("Assert mode disabled.");
        }

        if(params.opt_Compare) Config.setCompareMode(true);

        // Context-sensitivity for main analysis
        int context = -1;
        context = Config.contextSensitivityMode();

        // Temporary parameter setting for bug-detector
        if(params.command == ShellParameters.CMD_BUG_DETECTOR) {
            context = Config.Context_OneCallsiteAndObject();
            params.opt_MultiThread = true;
        }

        if(params.opt_ContextInsensitive) context = Config.Context_Insensitive();
        else if(params.opt_Context1Callsite) context = Config.Context_OneCallsite();
        else if(params.opt_Context2Callsite) context = Config.Context_KCallsite();
        else if(params.opt_Context3Callsite) context = Config.Context_KCallsite();
        else if(params.opt_Context4Callsite) context = Config.Context_KCallsite();
        else if(params.opt_Context5Callsite) context = Config.Context_KCallsite();
        else if(params.opt_ContextCallsiteSet) context = Config.Context_CallsiteSet();
        else if(params.opt_Context1Object) context = Config.Context_OneObject();
        else if(params.opt_ContextTAJS) context = Config.Context_OneObjectTAJS();
        else if(params.opt_Context1CallsiteAndObject) context = Config.Context_OneCallsiteAndObject();
        else if(params.opt_Context2CallsiteAndObject) context = Config.Context_KCallsiteAndObject();
        else if(params.opt_Context3CallsiteAndObject) context = Config.Context_KCallsiteAndObject();
        else if(params.opt_Context4CallsiteAndObject) context = Config.Context_KCallsiteAndObject();
        else if(params.opt_Context5CallsiteAndObject) context = Config.Context_KCallsiteAndObject();
        else if(params.opt_Context1CallsiteOrObject) context = Config.Context_OneCallsiteOrObject();

        Config.setContextSensitivityMode(new Integer(context));

        // Context-sensitivity depth for k-callsite sensitivity
        if (params.opt_Context2Callsite) Config.setContextSensitivityDepth(new Integer(2));
        else if (params.opt_Context3Callsite) Config.setContextSensitivityDepth(new Integer(3));
        else if (params.opt_Context4Callsite) Config.setContextSensitivityDepth(new Integer(4));
        else if (params.opt_Context5Callsite) Config.setContextSensitivityDepth(new Integer(5));
        else if (params.opt_Context2CallsiteAndObject) Config.setContextSensitivityDepth(new Integer(2));
        else if (params.opt_Context3CallsiteAndObject) Config.setContextSensitivityDepth(new Integer(3));
        else if (params.opt_Context4CallsiteAndObject) Config.setContextSensitivityDepth(new Integer(4));
        else if (params.opt_Context5CallsiteAndObject) Config.setContextSensitivityDepth(new Integer(5));
        
        
        // Context-sensitivity for pre-analysis
        if (params.opt_PreContextSensitive || params.command == ShellParameters.CMD_PREANALYZE) {
            if (!quiet) System.out.println("Context-sensitivity is turned on for pre-analysis.");
            Config.setPreContextSensitiveMode(true);
        }

        if(params.opt_Unsound) {
          Config.setUnsoundMode(new Boolean(params.opt_Unsound));
          System.out.println("Unsound mode enabled.");
        }
        
        // for HTML
        if(params.command == ShellParameters.CMD_HTML || params.command == ShellParameters.CMD_HTML_SPARSE) {
            if(params.FileNames.length > 1) throw new UserError("Only one HTML file supported at a time.");
            String low = fileName.toLowerCase();
            if(!(low.endsWith(".html") || low.endsWith(".xhtml") || low.endsWith(".htm")))
                throw new UserError("Not an HTML file.");
            // DOM mode
            Config.setDomMode();
        }

		// for Tizen
        if(params.opt_Tizen) {
            Config.setTizenMode();
            System.out.println("Tizen mode enabled.");
        }

        if (!quiet)
            System.out.println("Context-sensitivity mode is \"" + kr.ac.kaist.jsaf.analysis.typing.CallContext.getModeName() + "\".");

        // Initialize
        int return_code = 0;
        long analyzeStartTime = System.nanoTime();
        if (!quiet)
            System.out.println("\n* Initialize *");

        // Initialize AbsString cache
        kr.ac.kaist.jsaf.analysis.typing.domain.AbsString.initCache();

        // Read a JavaScript file and translate to IR
        long start = System.nanoTime();
        Pair<Program, HashMap<String, String>> pair;

        // for HTML
        JSFromHTML jshtml = null;
        if (params.command == ShellParameters.CMD_HTML || params.command == ShellParameters.CMD_HTML_SPARSE) {
            jshtml = new JSFromHTML(fileName);
            // Parse JavaScript code in the target html file
            pair = jshtml.parseScripts();
        }
        else
            pair = Parser.fileToAST(fileNames);
        Program program = pair.first();
        HashMap<String,String> fileMap = pair.second();
        Pair<Option<IRRoot>, List<BugInfo>> irErrors = ASTtoIR(fileName, program, Option.<String>none(), Option.<Coverage>none());
        Option<IRRoot> irOpt = irErrors.first();

        double irTranslationTime = (System.nanoTime() - start) / 1000000000.0;
        if (!quiet)
            System.out.format("# Time for IR translation(s): %.2f\n", irTranslationTime);

        // Check the translation result
        if(irOpt.isNone()) return -2;
        IRRoot ir = irOpt.unwrap();

        // Build a CFG
        start = System.nanoTime();
        CFGBuilder builder = new CFGBuilder(ir);
        CFG cfg = builder.build();
        double cfgBuildingTime = (System.nanoTime() - start) / 1000000000.0;
        if (!quiet) {
            System.out.format("# Time for CFG building(s): %.2f\n", cfgBuildingTime);
            System.out.format("# Time for front end(s): %.2f\n", (irTranslationTime + cfgBuildingTime));
        }
        List<StaticError> errors = builder.getErrors();
        if (!(errors.isEmpty())) {
            reportErrors(NodeUtil.getFileName(ir),
                         flattenErrors(errors),
                         Option.<Pair<FileWriter,BufferedWriter>>none());
        }

        if (!quiet) {
            System.out.println("\n* Analyze *");
            System.out.format("# Initial peak memory(mb): %.2f\n", MemoryMeasurer.peakMemory());
        }
        // compare mode to test the html pre-analysis
        if(params.opt_Compare) 
          Config.setCompareMode();

        // Initialize bulit-in models
        int previousBasicBlocks = cfg.getNodes().size();
        start = System.nanoTime();
        //BuiltinModel model = new BuiltinModel(cfg);
        //model.initialize();
        InitHeap init = new InitHeap(cfg);
        init.initialize();

        double builtinModelInitializationTime = (System.nanoTime() - start) / 1000000000.0;
        int presentBasicBlocks = cfg.getNodes().size();
        if (!quiet) {
            System.out.println("# Basic block(#): " + previousBasicBlocks + "(source) + " + (presentBasicBlocks - previousBasicBlocks) + "(bulit-in) = " + presentBasicBlocks);
            System.out.format("# Time for initial heap(s): %.2f\n", builtinModelInitializationTime);
        }

        // Set the initial state with DOM objects
        if(Config.domMode() && jshtml != null) {
            (new DOMBuilder(cfg, init, jshtml.getDocument())).initialize();
        }

        if(params.command == ShellParameters.CMD_PREANALYZE ||
            params.command == ShellParameters.CMD_SPARSE ||
            params.command == ShellParameters.CMD_NEW_SPARSE ||
            params.command == ShellParameters.CMD_BUG_DETECTOR ||
            params.command == ShellParameters.CMD_HTML_SPARSE
           ) {
            // computes reachable nodes for each function(including built-in functions)
            cfg.computeReachableNodes(quiet);
        }

        // Create Typing
        TypingInterface typingInterface = null;
        switch(params.command)
        {
        case ShellParameters.CMD_ANALYZE :
        case ShellParameters.CMD_HTML :
            typingInterface = new Typing(cfg, quiet, locclone);
            break;
        case ShellParameters.CMD_PREANALYZE :
            typingInterface = new PreTyping(cfg, quiet, true);
            break;
        case ShellParameters.CMD_SPARSE :
            typingInterface = new SparseTyping(cfg, quiet, locclone);
            break;
        case ShellParameters.CMD_NEW_SPARSE :
        case ShellParameters.CMD_BUG_DETECTOR :
        case ShellParameters.CMD_HTML_SPARSE :
            typingInterface = new DSparseTyping(cfg, quiet, locclone);
            break;
        default :
            throw new UserError("Cannot create the Typing. The command is unknown.");
        }

        // Compare with Pre Analysis
        /*
        if(Config.compare() && params.command != ShellParameters.CMD_PREANALYZE) {
            Config.setContextSensitivityMode(new Integer(Config.Context_Insensitive()));
            PreTyping preTyping = new PreTyping(cfg, quiet);
            preTyping.analyze(model);
            Config.setPreTyping(preTyping.state());
            preTyping.dump();
        }
        */

        // Check global variables in initial heap against list of predefined variables.
        init.checkPredefined();
        
        // Analyze
        switch(params.command)
        {
        case ShellParameters.CMD_ANALYZE :
        case ShellParameters.CMD_PREANALYZE :
        case ShellParameters.CMD_HTML :
        	if(params.opt_Compare) {
                // compare mode 
                CFG preCFG = builder.build();
                //BuiltinModel preModel = new BuiltinModel(preCFG);
                //preModel.initialize();
                InitHeap pre_init = new InitHeap(preCFG);
                pre_init.initialize();

                // Set the initial state with DOM objects
                if(Config.domMode() && jshtml != null) {
                    (new DOMBuilder(preCFG, pre_init, jshtml.getDocument())).initialize();
                }

                PreTyping preTyping = new PreTyping(preCFG, true, false);
                preTyping.analyze(pre_init);
                System.out.println("**PreAnalysis dump**");
                preTyping.dump();
        		typingInterface.setCompare(preTyping.getMergedState(), preTyping.cfg());
        	}
            // Analyze
            typingInterface.analyze(init);
            break;
        case ShellParameters.CMD_SPARSE :
        case ShellParameters.CMD_NEW_SPARSE :
        case ShellParameters.CMD_BUG_DETECTOR :
        case ShellParameters.CMD_HTML_SPARSE:
            PreTyping preTyping = new PreTyping(cfg, quiet, false);
            preTyping.analyze(init);

            // unsound because states among instructions are omitted.
            State pre_result = preTyping.getMergedState();
            // computes def/use set
            long access_start = System.nanoTime();
            Access duanalysis = new Access(cfg, preTyping.computeCallGraph(), pre_result);
            duanalysis.process(quiet);
            double accessTime = (System.nanoTime() - access_start) / 1000000000.0;
            if (!quiet)
                System.out.format("# Time for access analysis(s): %.2f\n", accessTime);

            // computes def/use graph
            if (typingInterface.env() != null)
                typingInterface.env().drawDDG(preTyping.computeCallGraph(), duanalysis.result(), quiet);

            // Analyze
            typingInterface.analyze(init, duanalysis.result());
        }

        // Report a result
        if (!quiet) {
          System.out.format("# Peak memory(mb): %.2f\n", MemoryMeasurer.peakMemory());
          System.out.format("# Result heap memory(mb): %.2f\n", MemoryMeasurer.measureHeap());
        }
        if (params.opt_MemDump) {
            System.out.println("\n* Dump *");
            typingInterface.dump();
            if(params.command == ShellParameters.CMD_PREANALYZE) typingInterface.dump_callgraph();
        }
        if (params.opt_Visual && typingInterface instanceof Typing) {
            System.out.println("\n* Visualization *");
            Visualization vs = new Visualization((Typing)typingInterface, fileMap, NodeUtil.getFileName(ir), toOption(params.opt_OutFileName));
            vs.run();
        }

        if (!quiet) {
            System.out.println("\n* Statistics *");
            System.out.println("# Total state count: " + typingInterface.getStateCount());
            typingInterface.statistics(params.opt_StatDump);
        }
        if (params.opt_CheckResult) {
            SemanticsTest.checkResult(typingInterface);
            System.out.println("Test pass");
        }

        // Execute Bug Detector
        System.out.println("\n* Bug Detector *");
        BugDetector detector = new BugDetector(params, cfg, typingInterface, fileMap, quiet, irErrors.second());
        detector.detectBug();

        if (!quiet)
            System.out.format("\nAnalysis took %.2fs\n", (System.nanoTime() - analyzeStartTime) / 1000000000.0);

        boolean isGlobalSparse = false;
        if(params.opt_DDGFileName != null) {
          DotWriter.ddgwrite(cfg, typingInterface.env(), params.opt_DDGFileName+".dot", params.opt_DDGFileName+".svg", "dot", false, isGlobalSparse);
        }
        if(params.opt_DDG0FileName != null) {
          DotWriter.ddgwrite(cfg, typingInterface.env(), params.opt_DDG0FileName+".dot", params.opt_DDG0FileName+".svg", "dot", true, isGlobalSparse);
        }
        if(params.opt_FGFileName != null) {
          DotWriter.fgwrite(cfg, typingInterface.env(), params.opt_FGFileName+".dot", params.opt_FGFileName+".svg", "dot", isGlobalSparse);
        }
        if (!quiet)
            System.out.println("Ok");

        return return_code;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 16. Web IDL Parse
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Parse a Web IDL file.
     * If you want a dump then give -out somefile.
     */
    private static int widlparse() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The widlparse command needs a file to parse.");
        String fileName = params.FileNames[0];

        int return_code = 0;
        try {
            FileInputStream fs = new FileInputStream(new File(fileName));
            InputStreamReader sr = new InputStreamReader(fs, Charset.forName("UTF-8"));
            BufferedReader in = new BufferedReader(sr);
            WIDL parser = new WIDL(in, fileName);
            xtc.parser.Result parseResult = parser.pWIDL(0);
            in.close(); sr.close(); fs.close();
            if (parseResult.hasValue()) {
                List<WDefinition> widl = (List<WDefinition>)(((SemanticValue)parseResult).value);
                String code = WIDLToString.doit(widl);
                if (params.opt_DB){
                    // store WIDL information into a DB
                    WIDLToDB.storeToDB(fileName, widl);
                } else {
                    System.out.println(code);
                }
            } else {
                System.out.println("WIDL parsing failed.");
                throw new ParserError((ParseError)parseResult, parser, 0);
            }
        } catch (FileNotFoundException f) {
            throw new UserError(fileName + " not found");
        } finally {
            Files.rm(fileName + ".db");
            Files.rm(fileName + ".test");
        }
        return return_code;
    }

    public static int widlparse(String fileName) throws UserError, InterruptedException, IOException {
        params.Clear();
        params.FileNames = new String[1];
        params.FileNames[0] = fileName;

        return widlparse();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 17. Web IDL Use Check
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Check the uses of APIs in Web IDL.
     */
    private static int widlcheck() throws UserError, InterruptedException, IOException {
        if (params.FileNames.length == 0) throw new UserError("The widlcheck command needs a file to parse.");
        String fileName = params.FileNames[0];
        List<String> fileNames = Arrays.asList(Arrays.copyOfRange(params.FileNames, 1, params.FileNames.length));

        int return_code = 0;
        List<String> file = new ArrayList();
        file.add(fileName);
        Pair<Program, HashMap<String, String>> pair = Parser.fileToAST(file);
        Program pgm = pair.first();
        new WIDLChecker(pgm, fileNames).doit();
        return return_code;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Compile to IR
    ////////////////////////////////////////////////////////////////////////////////
    // Triple<String, Integer, String> : filename, starting line number, JavaScript source
    private static Option<IRRoot> scriptToIR(List<Triple<String, Integer, String>> scripts, Option<String> out) throws UserError, IOException {
        return scriptToIR(scripts, out, Option.<Coverage>none());
    }

    private static Option<IRRoot> scriptToIR(List<Triple<String, Integer, String>> scripts, Option<String> out, Option<Coverage> coverage) throws UserError, IOException {
        Program program = Parser.scriptToAST(scripts).first();
        return ASTtoIR(scripts.get(0).first(), program, out, coverage).first();
    }

    private static Option<IRRoot> fileToIR(List<String> files, Option<String> out) throws UserError, IOException {
        return fileToIR(files, out, Option.<Coverage>none()).first();
    }

    private static Pair<Option<IRRoot>, HashMap<String, String>> fileToIR(List<String> files, Option<String> out, boolean getFileMap) throws UserError, IOException {
        if (getFileMap) return fileToIR(files, out, Option.<Coverage>none());
        else return new Pair<Option<IRRoot>, HashMap<String, String>>(fileToIR(files, out, Option.<Coverage>none()).first(), new HashMap<String, String>());
    }

    private static Pair<Option<IRRoot>, HashMap<String, String>> fileToIR(List<String> files, Option<String> out, Option<Coverage> coverage) throws UserError, IOException {
        Pair<Program, HashMap<String, String>> pair;
        // html file support 
        if(files.size() == 1 && (files.get(0).toLowerCase().endsWith(".html") || files.get(0).toLowerCase().endsWith(".xhtml") || files.get(0).toLowerCase().endsWith(".htm"))) { 
            // DOM mode
            Config.setDomMode();
            JSFromHTML jshtml = new JSFromHTML(files.get(0));
            // Parse JavaScript code in the target html file
            pair = jshtml.parseScripts();
        }
        else
            pair = Parser.fileToAST(files);

        // Pair<Program, HashMap<String,String>> pair = Parser.fileToAST(files);
        Program program = pair.first();
        HashMap<String,String> fileMap = pair.second();
        return new Pair<Option<IRRoot>, HashMap<String, String>>(ASTtoIR(files.get(0), program, out, coverage).first(), fileMap);
    }

    public static Pair<Option<IRRoot>, List<BugInfo>> ASTtoIR(String file, Program pgm, Option<String> out, Option<Coverage> coverage) throws UserError, IOException {
        try {
            Program program = pgm;

            // Module Rewriter
            if (params.opt_Module) {
                ModuleRewriter moduleRewriter = new ModuleRewriter(program);
                program = (Program)moduleRewriter.doit();
            }

            // Hoister
            Hoister hoister = new Hoister(program);
            program = (Program)hoister.doit();
            List<BugInfo> shadowingErrors = hoister.getErrors();
            /* Testing Hoister...
            if (out.isSome()){
                String outfile = out.unwrap();
                try{
                    ASTIO.writeJavaAst(program, outfile);
                    System.out.println("Dumped hoisted code to " + outfile);
                } catch (IOException e){
                    throw new IOException("IOException " + e +
                                          "while writing " + outfile);
                }
            }
            */

            // Disambiguator
            Disambiguator disambiguator = new Disambiguator(program, opt_DisambiguateOnly);
            program = (Program)disambiguator.doit();
            List<StaticError> errors = disambiguator.getErrors();
            // Testing Disambiguator...
            if (opt_DisambiguateOnly) {
                if (out.isSome()) {
                    String outfile = out.unwrap();
                    try {
                        Pair<FileWriter, BufferedWriter> pair = Useful.filenameToBufferedWriter(outfile);
                        FileWriter fw = pair.first();
                        BufferedWriter writer = pair.second();
                        writer.write(JSAstToConcrete.doitInternal(program));
                        writer.close();
                        fw.close();
                    } catch (IOException e){
                        throw new IOException("IOException " + e +
                                              "while writing " + outfile);
                    }
                } else if (errors.isEmpty()) {
                    System.out.println(JSAstToConcrete.doit(program));
                }
                reportErrors(NodeUtil.getFileName(program),
                             flattenErrors(errors),
                             Option.<Pair<FileWriter,BufferedWriter>>none());
                if (opt_DisambiguateOnly && errors.isEmpty())
                  return new Pair<Option<IRRoot>, List<BugInfo>>(Option.some(IRFactory.makeRoot()),
                                                                  Useful.<BugInfo>list());
                return new Pair<Option<IRRoot>, List<BugInfo>>(Option.<IRRoot>none(),
                                                                Useful.<BugInfo>list());
            } else {
                WithRewriter withRewriter = new WithRewriter(program, false);
                program = (Program)withRewriter.doit();
                errors.addAll(withRewriter.getErrors());
                Translator translator = new Translator(program, coverage);
                IRRoot ir = (IRRoot)translator.doit();
                errors.addAll(translator.getErrors());
                if (errors.isEmpty()) {
                    return new Pair<Option<IRRoot>, List<BugInfo>>(Option.some(ir),
                                                                    shadowingErrors);
                } else {
                    reportErrors(NodeUtil.getFileName(program),
                                 flattenErrors(errors),
                                 Option.<Pair<FileWriter,BufferedWriter>>none());
                    return new Pair<Option<IRRoot>, List<BugInfo>>((params.opt_IgnoreErrorOnAST ? Option.some(ir) : Option.<IRRoot>none()),
                                                                    Useful.<BugInfo>list());
                }
            }
        } catch (FileNotFoundException f) {
            throw new UserError(file + " not found");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Error Handling
    ////////////////////////////////////////////////////////////////////////////////
    public static List<? extends StaticError> flattenErrors(Iterable<? extends StaticError> ex) {
        List<StaticError> result = new LinkedList<StaticError>();
        for (StaticError err: ex) {
            result.addAll(flattenErrors(err));
        }
        return result;
    }

    private static List<? extends StaticError> flattenErrors(StaticError ex) {
        List<StaticError> result = new LinkedList<StaticError>();
        if (ex instanceof MultipleStaticError) {
            for (StaticError err : ((MultipleStaticError)ex).toJList())
                result.addAll(flattenErrors(err));
        } else result.add(new WrappedException(ex));
        return result;
    }

    public static int reportErrors(String file_name, List<? extends StaticError> errors,
                                   Option<Pair<FileWriter,BufferedWriter>> pair) throws IOException {
        int return_code = 0;
        if (!IterUtil.isEmpty(errors)) {
            for (StaticError error: IterUtil.sort(errors)) {
                if (pair.isSome()) pair.unwrap().second().write(error.getMessage());
                else System.out.println(error.getMessage());
            }
            String err_string;
            int num_errors = IterUtil.sizeOf(errors);
            if (num_errors == 0) {
                // Unreachable code?
                err_string = "File " + file_name + " compiled successfully.";
            } else {
                err_string = "File " + file_name + " has " + num_errors + " error" +
                    (num_errors == 1 ? "." : "s.");
            }
            if (pair.isSome()) pair.unwrap().second().write(err_string);
            else System.out.println(err_string);
            return_code = -2;
        }
        if (pair.isSome()) {
            pair.unwrap().second().close();
            pair.unwrap().first().close();
        }
        return return_code;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // etc
    ////////////////////////////////////////////////////////////////////////////////
    public static Option<String> toOption(String str) {
        if(str == null) return Option.<String>none();
        else return Option.<String>some(str);
    }
}
