/*******************************************************************************
    Copyright (c) 2012, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf;

import java.io.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

import kr.ac.kaist.jsaf.parser.*;

public class Main {
    public static void main(String[] args) {
        try {
            /*
            String text = "12 * (5 - 6);";
            ANTLRStringStream in = new ANTLRStringStream(text);
            */
            ANTLRFileStream in = new ANTLRFileStream(args[0]);
            JSLexer lexer = new JSLexer(in);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JSParser parser = new JSParser(tokens);
            JSParser.program_return returnValue = parser.program();
            /* print tree if building tress */
            if (returnValue != null) {
                CommonTree tree = (CommonTree)returnValue.getTree();
                System.out.println(tree.toStringTree());
                /* print DOT input if building tress */
                DOTTreeGenerator gen = new DOTTreeGenerator();
                StringTemplate st = gen.toDOT(tree);
                System.out.println(st);
            }

            /* walk an AST with a tree parser */
            /*
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
            nodes.setTokenStream(tokens);
            TP walker = new TP(nodes); // created from TP.g
            TP.program_return returnValue2 = walker.program();
            CommonTree tree2 = (CommonTree)returnValue2.getTree();
            // if tree parser constructs trees
            if (returnValue2 != null)
                System.out.println(tree2.toStringTree());
            */
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
