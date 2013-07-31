/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf;

import java.util.ArrayList;

public class ShellParameters
{
    ////////////////////////////////////////////////////////////////////////////////
    // Command Enumeration
    ////////////////////////////////////////////////////////////////////////////////
    public static final int                        CMD_USAGE = 0;
    public static final int                        CMD_PARSE = 1;
    public static final int                        CMD_UNPARSE = 2;
    public static final int                        CMD_STRICT = 3;
    public static final int                        CMD_CLONE_DETECTOR = 4;
    public static final int                        CMD_COVERAGE = 5;
    public static final int                        CMD_CONCOLIC = 6;
    public static final int                        CMD_URL = 7;
    public static final int                        CMD_WITH = 8;
    public static final int                        CMD_MODULE = 9;
    public static final int                        CMD_JUNIT = 10;
    public static final int                        CMD_DISAMBIGUATE = 11;
    public static final int                        CMD_COMPILE = 12;
    public static final int                        CMD_CFG = 13;
    public static final int                        CMD_INTERPRET = 14;
    public static final int                        CMD_ANALYZE = 15;
    public static final int                        CMD_PREANALYZE = 16; // This command should be inserted into CMD_ANALYZE as an option.
    public static final int                        CMD_SPARSE = 17; // This command should be inserted into CMD_ANALYZE as an option.
    public static final int                        CMD_HTML = 18; // This command should be inserted into CMD_ANALYZE as an option.
    public static final int                        CMD_NEW_SPARSE = 19; // This command should be inserted into CMD_ANALYZE as an option.
    public static final int                        CMD_BUG_DETECTOR = 21;
    public static final int                        CMD_WIDLPARSE = 22;
    public static final int                        CMD_HTML_SPARSE= 23;
    public static final int                        CMD_WIDLCHECK = 24;
    public static final int                        CMD_WEBAPP_CONVERT= 97;
    public static final int                        CMD_DTV_APP= 98;
    public static final int                        CMD_HELP = 99;

    ////////////////////////////////////////////////////////////////////////////////
    // Parameters
    ////////////////////////////////////////////////////////////////////////////////
    public int                                     command;
    public String                                  opt_OutFileName;
    public boolean                                 opt_DB;
    public boolean                                 opt_Time;
    public boolean                                 opt_Module;
    public boolean                                 opt_IgnoreErrorOnAST;
    public boolean                                 opt_Model;
    public boolean                                 opt_Mozilla;
    public boolean                                 opt_Verbose1;
    public boolean                                 opt_Verbose2;
    public boolean                                 opt_Verbose3;
    public boolean                                 opt_LocClone;
    public boolean                                 opt_Test;
    public boolean                                 opt_DeveloperMode;
    public boolean                                 opt_ErrorOnly;
    public boolean                                 opt_Library;
    public boolean                                 opt_MemDump;
    public boolean                                 opt_StatDump;
    public boolean                                 opt_Visual;
    public boolean                                 opt_CheckResult;
    public boolean                                 opt_NoAssert;
    public boolean                                 opt_Compare;
    public boolean                                 opt_ContextTrace;
    public boolean                                 opt_ContextInsensitive;
    public boolean                                 opt_Context1Callsite;
    public boolean                                 opt_Context2Callsite;
    public boolean                                 opt_Context3Callsite;
    public boolean                                 opt_Context4Callsite;
    public boolean                                 opt_Context5Callsite;
    public boolean                                 opt_ContextCallsiteSet;
    public boolean                                 opt_Context1Object;
    public boolean                                 opt_ContextTAJS;
    public boolean                                 opt_Context1CallsiteAndObject;
    public boolean                                 opt_Context2CallsiteAndObject;
    public boolean                                 opt_Context3CallsiteAndObject;
    public boolean                                 opt_Context4CallsiteAndObject;
    public boolean                                 opt_Context5CallsiteAndObject;
    public boolean                                 opt_Context1CallsiteOrObject;
    public boolean                                 opt_PreContextSensitive;
    public boolean                                 opt_Unsound;
    public boolean                                 opt_Dom;
    public boolean                                 opt_Tizen;
    public boolean                                 opt_jQuery;
    public boolean                                 opt_MultiThread;
    public int                                     opt_unrollingCount;
    public String                                  opt_DDGFileName;
    public String                                  opt_DDG0FileName;
    public String                                  opt_FGFileName;
    public String[]                                FileNames;
    public boolean                                 opt_Loud; // for dtv mode, show all progress messages (opposite to quiet)
    public boolean                                 opt_ShowAll; // for dtv mode, show all reported bug messages

    ////////////////////////////////////////////////////////////////////////////////
    // Constructor and Initialize
    ////////////////////////////////////////////////////////////////////////////////
    private String                                 ErrorMessage;

    public ShellParameters()
    {
        ErrorMessage = null;
        Clear();
    }

    public void Clear()
    {
        command = CMD_USAGE;
        opt_OutFileName = null;
        opt_DB = false;
        opt_Time = false;
        opt_Module = false;
        opt_IgnoreErrorOnAST = false;
        opt_Model = false;
        opt_Mozilla = false;
        opt_Verbose1 = false;
        opt_Verbose2 = false;
        opt_Verbose3 = false;
        opt_LocClone = false;
        opt_Test = false;
        opt_DeveloperMode = false;
        opt_ErrorOnly = false;
        opt_Library = false;
        opt_MemDump = false;
        opt_StatDump = false;
        opt_Visual = false;
        opt_CheckResult = false;
        opt_NoAssert = false;
        opt_Compare = false;
        opt_ContextTrace = false;
        opt_ContextInsensitive = false;
        opt_Context1Callsite = false;
        opt_Context2Callsite = false;
        opt_Context3Callsite = false;
        opt_Context4Callsite = false;
        opt_Context5Callsite = false;
        opt_ContextCallsiteSet = false;
        opt_Context1Object = false;
        opt_ContextTAJS = false;
        opt_Context1CallsiteAndObject = false;
        opt_Context2CallsiteAndObject = false;
        opt_Context3CallsiteAndObject = false;
        opt_Context4CallsiteAndObject = false;
        opt_Context5CallsiteAndObject = false;
        opt_Context1CallsiteOrObject = false;
        opt_PreContextSensitive = false;
        opt_Unsound = false;
        opt_Dom = false;
        opt_Tizen = false;
        opt_jQuery = false;
        opt_MultiThread = false;
        opt_unrollingCount = 0;
        opt_DDGFileName = null;
        opt_DDG0FileName = null;
        opt_FGFileName = null;
        FileNames = new String[0];
        opt_Loud = false;
        opt_ShowAll = true;
    }

    /**
     * @param Parameter tokens.
     * @return Error message if there is an error.
     *         null otherwise.
     */
    public String Set(String[] args)
    {
        ErrorMessage = null;
        Clear();
        Parse(args);
        return ErrorMessage;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Parsing
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * @param Parameter tokens to parse.
     */
    private void Parse(String[] args)
    {
        // There is no parameter.
        if(args.length == 0) return;

        // Set feasible options for each command.
        ArrayList<String> feasibleOptions = new ArrayList<String>();

        // For all commands
        feasibleOptions.add("-module");
        feasibleOptions.add("-ignoreErrorOnAST");

        // For each command
        String cmd = args[0];
        if(cmd.compareTo("parse") == 0)
        {
            command = CMD_PARSE;
            feasibleOptions.add("-out");
            feasibleOptions.add("-time");
        }
        else if(cmd.compareTo("unparse") == 0)
        {
            command = CMD_UNPARSE;
            feasibleOptions.add("-out");
        }
        else if(cmd.compareTo("widlparse") == 0)
        {
            command = CMD_WIDLPARSE;
            feasibleOptions.add("-db");
        }
        else if(cmd.compareTo("widlcheck") == 0)
        {
            command = CMD_WIDLCHECK;
        }
        else if(cmd.compareTo("strict") == 0)
        {
            command = CMD_STRICT;
            feasibleOptions.add("-out");
        }
        else if(cmd.compareTo("clone-detector") == 0)
        {
            command = CMD_CLONE_DETECTOR;
        }
        else if(cmd.compareTo("coverage") == 0)
        {
            command = CMD_COVERAGE;
        }
        else if(cmd.compareTo("concolic") == 0)
        {
            command = CMD_CONCOLIC;
        }
        else if(cmd.compareTo("url") == 0)
        {
            command = CMD_URL;
            feasibleOptions.add("-out");
        }
        else if(cmd.compareTo("with") == 0)
        {
            command = CMD_WITH;
            feasibleOptions.add("-out");
        }
        else if(cmd.compareTo("module") == 0)
        {
            command = CMD_MODULE;
            feasibleOptions.add("-out");
        }
        else if(cmd.compareTo("junit") == 0)
        {
            command = CMD_JUNIT;
        }
        else if(cmd.compareTo("disambiguate") == 0)
        {
            command = CMD_DISAMBIGUATE;
            feasibleOptions.add("-out");
        }
        else if(cmd.compareTo("compile") == 0)
        {
            command = CMD_COMPILE;
            feasibleOptions.add("-out");
            feasibleOptions.add("-time");
        }
        else if(cmd.compareTo("cfg") == 0)
        {
            command = CMD_CFG;
            feasibleOptions.add("-out");
            feasibleOptions.add("-dom");
            feasibleOptions.add("-tizen");
            feasibleOptions.add("-test");
            feasibleOptions.add("-model");
            feasibleOptions.add("-library");
            feasibleOptions.add("-unroll");
        }
        else if(cmd.compareTo("interpret") == 0)
        {
            command = CMD_INTERPRET;
            feasibleOptions.add("-out");
            feasibleOptions.add("-time");
            feasibleOptions.add("-mozilla");
        }
        else if(cmd.compareTo("interpret_mozilla") == 0)
        {
            command = CMD_INTERPRET;
            feasibleOptions.add("-out");
            feasibleOptions.add("-time");
            feasibleOptions.add("-mozilla");
            opt_Mozilla = true;
        }
        else if(cmd.compareTo("analyze") == 0 ||
                cmd.compareTo("preanalyze") == 0 ||
                cmd.compareTo("sparse") == 0 ||
                cmd.compareTo("sparse-ddg") == 0 ||
                cmd.compareTo("html") == 0 ||
                cmd.compareTo("html-sparse") == 0)
        {
            if(cmd.compareTo("analyze") == 0) command = CMD_ANALYZE;
            else if(cmd.compareTo("preanalyze") == 0) command = CMD_PREANALYZE;
            else if(cmd.compareTo("sparse") == 0) command = CMD_SPARSE;
            else if(cmd.compareTo("sparse-ddg") == 0) command = CMD_NEW_SPARSE;
            else if(cmd.compareTo("html") == 0) command = CMD_HTML;
            else if(cmd.compareTo("html-sparse") == 0) command = CMD_HTML_SPARSE;
            feasibleOptions.add("-verbose1");
            feasibleOptions.add("-verbose2");
            feasibleOptions.add("-verbose3");
            feasibleOptions.add("-locclone");
            feasibleOptions.add("-test");
            feasibleOptions.add("-library");
            feasibleOptions.add("-memdump");
            feasibleOptions.add("-statdump");
            feasibleOptions.add("-visual");
            feasibleOptions.add("-checkResult");
            feasibleOptions.add("-no-assert");
            feasibleOptions.add("-compare");
            feasibleOptions.add("-context-insensitive");
            feasibleOptions.add("-context-1-callsite");
            feasibleOptions.add("-context-2-callsite");
            feasibleOptions.add("-context-3-callsite");
            feasibleOptions.add("-context-4-callsite");
            feasibleOptions.add("-context-5-callsite");
            feasibleOptions.add("-context-callsite-set");
            feasibleOptions.add("-context-1-object");
            feasibleOptions.add("-context-tajs");
            feasibleOptions.add("-context-1-callsite-and-object");
            feasibleOptions.add("-context-2-callsite-and-object");
            feasibleOptions.add("-context-3-callsite-and-object");
            feasibleOptions.add("-context-4-callsite-and-object");
            feasibleOptions.add("-context-5-callsite-and-object");
            feasibleOptions.add("-context-1-callsite-or-object");
            feasibleOptions.add("-pre-context-sensitive");
            feasibleOptions.add("-unsound");
            feasibleOptions.add("-multi-thread");
            feasibleOptions.add("-unroll");
            feasibleOptions.add("-ddgout");
            feasibleOptions.add("-ddg0out");
            feasibleOptions.add("-fgout");
            feasibleOptions.add("-tizen");
            feasibleOptions.add("-jq");
        }
        else if(cmd.compareTo("bug-detector") == 0)
        {
            command = CMD_BUG_DETECTOR;
            feasibleOptions.add("-dev");
            feasibleOptions.add("-erroronly");
            feasibleOptions.add("-locclone");
            feasibleOptions.add("-library");
            feasibleOptions.add("-context-trace");
            feasibleOptions.add("-context-insensitive");
            feasibleOptions.add("-context-1-callsite");
            feasibleOptions.add("-context-2-callsite");
            feasibleOptions.add("-context-3-callsite");
            feasibleOptions.add("-context-4-callsite");
            feasibleOptions.add("-context-5-callsite");
            feasibleOptions.add("-context-callsite-set");
            feasibleOptions.add("-context-1-object");
            feasibleOptions.add("-context-1-callsite-and-object");
            feasibleOptions.add("-context-2-callsite-and-object");
            feasibleOptions.add("-context-3-callsite-and-object");
            feasibleOptions.add("-context-4-callsite-and-object");
            feasibleOptions.add("-context-5-callsite-and-object");
            feasibleOptions.add("-context-1-callsite-or-object");
            feasibleOptions.add("-unroll");
        }
        else if(cmd.compareTo("dtv-app") == 0)
        {
            command = CMD_DTV_APP;
            opt_ShowAll = false;
            feasibleOptions.add("-dev");
            feasibleOptions.add("-erroronly");
            feasibleOptions.add("-locclone");
            feasibleOptions.add("-test");
            feasibleOptions.add("-library");
            feasibleOptions.add("-context-trace");
            feasibleOptions.add("-context-insensitive");
            feasibleOptions.add("-context-1-callsite");
            feasibleOptions.add("-context-2-callsite");
            feasibleOptions.add("-context-3-callsite");
            feasibleOptions.add("-context-4-callsite");
            feasibleOptions.add("-context-5-callsite");
            feasibleOptions.add("-context-callsite-set");
            feasibleOptions.add("-context-1-object");
            feasibleOptions.add("-context-1-callsite-and-object");
            feasibleOptions.add("-context-2-callsite-and-object");
            feasibleOptions.add("-context-3-callsite-and-object");
            feasibleOptions.add("-context-4-callsite-and-object");
            feasibleOptions.add("-context-5-callsite-and-object");
            feasibleOptions.add("-context-1-callsite-or-object");
            feasibleOptions.add("-loud");
            feasibleOptions.add("-showall");
        }
        else if(cmd.compareTo("webapp-convert") == 0)
        {
            command = CMD_WEBAPP_CONVERT;
            opt_ShowAll = false;
        }
        else if(cmd.compareTo("help") == 0)
        {
            command = CMD_HELP;
        }
        else
        {
            command = CMD_USAGE;
            return;
        }

        // Extract option parameters.
        for(int i = 1; i < args.length; i++)
        {
            // Is this an option parameter?
            if(args[i].charAt(0) == '-')
            {
                // Is this a feasible parameter for this command?
                if(feasibleOptions.contains(args[i]) == false) ErrorMessage = (args[i] + " is not a valid flag for `jsaf " + cmd + "`");
                // Set the option.
                else i+= SetOption(args, i);

                // Is there an error?
                if(ErrorMessage != null) break;
            }
            else
            {
                // Copy the rest parameters to input filenames.
                int Rest = args.length - i;
                FileNames = new String[Rest];
                for(int j = 0; j < Rest;j ++) FileNames[j] = args[i + j];
                break;
            }
        }

        if(ErrorMessage != null) Clear();
    }

    private int SetOption(String[] args, int index)
    {
        int ConsumedParameterCount = 0;

        String opt = args[index];
        if(opt.compareTo("-out") == 0 ||
           opt.compareTo("-unroll") == 0 ||
           opt.compareTo("-ddgout") == 0 ||
           opt.compareTo("-ddg0out") == 0 ||
           opt.compareTo("-fgout") == 0)
        {
            if(index + 1 >= args.length)
            {
                ErrorMessage = "`" + opt + "` parameter needs an output filename. See help.";
            } else {
                if(opt.compareTo("-out") == 0) opt_OutFileName = args[index + 1];
                else if(opt.compareTo("-unroll") == 0) opt_unrollingCount = Integer.parseInt(args[index + 1]);
                else if(opt.compareTo("-ddgout") == 0) opt_DDGFileName = args[index + 1];
                else if(opt.compareTo("-ddg0out") == 0) opt_DDG0FileName = args[index + 1];
                else if(opt.compareTo("-fgout") == 0) opt_FGFileName = args[index + 1];
                ConsumedParameterCount = 1;
            }
        }
        else if(opt.compareTo("-db") == 0) opt_DB = true;
        else if(opt.compareTo("-time") == 0) opt_Time = true;
        else if(opt.compareTo("-module") == 0) opt_Module = true;
        else if(opt.compareTo("-ignoreErrorOnAST") == 0) opt_IgnoreErrorOnAST = true;
        else if(opt.compareTo("-model") == 0) opt_Model = true;
        else if(opt.compareTo("-mozilla") == 0) opt_Mozilla = true;
        else if(opt.compareTo("-verbose1") == 0) opt_Verbose1 = true;
        else if(opt.compareTo("-verbose2") == 0) opt_Verbose2 = true;
        else if(opt.compareTo("-verbose3") == 0) opt_Verbose3 = true;
        else if(opt.compareTo("-locclone") == 0) opt_LocClone = true;
        else if(opt.compareTo("-dev") == 0) opt_DeveloperMode = true;
        else if(opt.compareTo("-erroronly") == 0) opt_ErrorOnly = true;
        else if(opt.compareTo("-test") == 0) opt_Test = true;
        else if(opt.compareTo("-library") == 0) opt_Library = true;
        else if(opt.compareTo("-memdump") == 0) opt_MemDump = true;
        else if(opt.compareTo("-statdump") == 0) opt_StatDump = true;
        else if(opt.compareTo("-visual") == 0) opt_Visual = true;
        else if(opt.compareTo("-checkResult") == 0) opt_CheckResult = true;
        else if(opt.compareTo("-no-assert") == 0) opt_NoAssert = true;
        else if(opt.compareTo("-compare") == 0) opt_Compare = true;
        else if(opt.compareTo("-context-trace") == 0) opt_ContextTrace = true;
        else if(opt.compareTo("-context-insensitive") == 0) opt_ContextInsensitive = true;
        else if(opt.compareTo("-context-1-callsite") == 0) opt_Context1Callsite = true;
        else if(opt.compareTo("-context-2-callsite") == 0) opt_Context2Callsite = true;
        else if(opt.compareTo("-context-3-callsite") == 0) opt_Context3Callsite = true;
        else if(opt.compareTo("-context-4-callsite") == 0) opt_Context4Callsite = true;
        else if(opt.compareTo("-context-5-callsite") == 0) opt_Context5Callsite = true;
        else if(opt.compareTo("-context-callsite-set") == 0) opt_ContextCallsiteSet = true;
        else if(opt.compareTo("-context-1-object") == 0) opt_Context1Object = true;
        else if(opt.compareTo("-context-tajs") == 0) opt_ContextTAJS = true;
        else if(opt.compareTo("-context-1-callsite-and-object") == 0) opt_Context1CallsiteAndObject = true;
        else if(opt.compareTo("-context-2-callsite-and-object") == 0) opt_Context2CallsiteAndObject = true;
        else if(opt.compareTo("-context-3-callsite-and-object") == 0) opt_Context3CallsiteAndObject = true;
        else if(opt.compareTo("-context-4-callsite-and-object") == 0) opt_Context4CallsiteAndObject = true;
        else if(opt.compareTo("-context-5-callsite-and-object") == 0) opt_Context5CallsiteAndObject = true;
        else if(opt.compareTo("-context-1-callsite-or-object") == 0) opt_Context1CallsiteOrObject = true;
        else if(opt.compareTo("-pre-context-sensitive") == 0) opt_PreContextSensitive = true;
        else if(opt.compareTo("-unsound") == 0) opt_Unsound = true;
        else if(opt.compareTo("-dom") == 0) opt_Dom = true;
        else if(opt.compareTo("-tizen") == 0) opt_Tizen = true;
        else if(opt.compareTo("-jq") == 0) opt_jQuery = true;
        else if(opt.compareTo("-multi-thread") == 0) opt_MultiThread = true;
        else if(opt.compareTo("-loud") == 0) opt_Loud = true;
        else if(opt.compareTo("-showall") == 0) opt_ShowAll = true;
        else
        {
            ErrorMessage = "`" + opt + "` is no match for option parameter.";
        }

        return ConsumedParameterCount;
    }
}
