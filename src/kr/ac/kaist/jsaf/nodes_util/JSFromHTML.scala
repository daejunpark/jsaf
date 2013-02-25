/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import _root_.java.io._
import _root_.java.net._
import _root_.java.util.{List => JList}
import _root_.java.lang.{Integer => JInteger}
import edu.rice.cs.plt.tuple.{Option => JOption}
import kr.ac.kaist.jsaf.scala_src.nodes._
import net.htmlparser.jericho._
import kr.ac.kaist.jsaf.nodes.Program
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.useful.Triple
import org.cyberneko.html.parsers._
import org.apache.html.dom.HTMLDocumentImpl
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.html.HTMLDocument

class JSFromHTML(filename: String) extends Walker {
  val file = new File(filename)
  val source : Source  = new Source(file)
  val scriptelements : JList[Element] = source.getAllElements(HTMLElementName.SCRIPT)
  def getSource() = source

  // use of Neko HTML parser for the DOM tree
  val document = { val parser : DOMParser = new DOMParser
                   parser.parse(filename)
                   parser.getDocument }
  def getDocument() = document
  
  /*
   * Parse all code in the <script> tags, and return an AST
   */
  def parseScripts = {
    //System.out.println(source);
    val codecontents: JList[Triple[String, JInteger, String]] = 
       toList(scriptelements).map(x => 
        { val srcname = x.getAttributeValue("src")
          // embedded script code
          if(srcname == null) {
            val s:Segment = x.getContent
            //System.out.println(s.getRowColumnVector().getRow() + " : " + s.toString)
            new Triple(filename, new JInteger(s.getRowColumnVector().getRow()), s.toString())
          }
          // code from external source
          else {
            val srcsource = new File(srcname)
            val path = if(srcsource.isAbsolute()) srcname 
                       else {
                         val parentpath = file.getParent()
                         if(parentpath == null) srcname
                         else parentpath + "/" + srcname
                       }
            val source = scala.io.Source.fromFile(path).mkString
            new Triple(path, new JInteger(1), source)
          }
        }
      )
    Parser.scriptToAST(codecontents)
  }
}
