/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import java.io._
import java.net._
import java.nio.charset.CodingErrorAction
import java.util.{List => JList, HashMap}
import java.lang.{Integer => JInteger}
import scala.io.Codec
import edu.rice.cs.plt.tuple.{Option => JOption}
import kr.ac.kaist.jsaf.scala_src.nodes._
import net.htmlparser.jericho._
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.nodes.Program
import kr.ac.kaist.jsaf.compiler.Parser
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.useful.{Useful, Triple, Pair}
import kr.ac.kaist.jsaf.analysis.typing.Config
import org.cyberneko.html.parsers._
import org.apache.html.dom.HTMLDocumentImpl
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.html.HTMLDocument
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHelper
import org.xml.sax.InputSource

class JSFromHTML(filename: String) extends Walker {
  val file = new File(filename)
  val source : Source  = new Source(file)
  source.fullSequentialParse
  val scriptelements : JList[Element] = source.getAllElements(HTMLElementName.SCRIPT)
  def getSource(): Source = source

  // use of Neko HTML parser for the DOM tree
  val document = { val parser : DOMParser = new DOMParser
                   val inputStream: InputStream = new FileInputStream(filename)
                   parser.setFeature("http://xml.org/sax/features/namespaces", false)
                   parser.parse(new InputSource(inputStream))
                   parser.getDocument }
  def getDocument(): Document = document
  
  /*
   * Parse all code in the <script> tags, and return an AST
   */
  def parseScripts(): Program = {
    //System.out.println(source);
    // filter out script elements that have non-JavaScript code
    val filtered_scriptelements = toList(scriptelements).filter(x =>
      {
        val scripttype = x.getAttributeValue("type")
        if(scripttype==null || scripttype.toLowerCase=="text/javascript")
          true
        else false
      })

    // filter out modeled library and enable model
    val nonmodeled_scriptelements = filtered_scriptelements.filter((e) => {
      val srcname = e.getAttributeValue("src")
      //if (srcname != null && isModeledLibrary(srcname)) {
      if (srcname != null && Config.jqMode && isModeledLibrary(srcname)) {
        enableModel(srcname); // side-effect
        false
      }
      else
        true
    })

    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    // get a list of JavaScript code in script elements
    val codecontents: JList[Triple[String, JInteger, String]] = nonmodeled_scriptelements.map(x =>
      { 
        var srcname = x.getAttributeValue("src")
        // embedded script code
        if(srcname == null) {
          val s:Segment = x.getContent
          //System.out.println(s.getRowColumnVector().getRow() + " : " + s.toString)
          new Triple(filename, new JInteger(s.getRowColumnVector().getRow()), s.toString())
        }
        // code from external source
        else {
          // extract a JavaScript file name from a string such as "main.js?135895164373817"
          if (!srcname.endsWith(".js") && srcname.containsSlice(".js"))
            srcname = srcname.take(srcname.replaceAll(".js", "<>").indexOf('<')).concat(".js")
          val srcsource = new File(srcname)
          val path = if(srcsource.isAbsolute()) srcname 
                     else {
                       val parentpath = file.getParent()
                       if(parentpath == null) srcname
                       else parentpath + "/" + srcname
                     }
          val pathf = new File(path)
          if (pathf.exists()){
            val source = scala.io.Source.fromFile(path)
            val result = new Triple(path, new JInteger(1), source.mkString)
            source.close
            result
          } else {
            throw new UserError("WARNING: Cannot find " + srcname)
          }
        }
      })

    // Collect event handler code on the event attributes
    var loadevent_count = 1
    var unloadevent_count = 1
    var keyboardevent_count = 1
    var mouseevent_count = 1
    var messageevent_count = 1
    var otherevent_count = 1
    val elementsList = toList(source.getAllElements)
    val eventsources: JList[Triple[String, JInteger, String]] =
      elementsList.foldLeft(List[Triple[String, JInteger, String]]())((event_list, e) => {
        val attributes = e.getAttributes
        if(attributes!=null) {
          val attrIterator = e.getAttributes.iterator()
          var e_list: JList[Triple[String, JInteger, String]] = List()
          // Search all specified attributes
          while(attrIterator.hasNext) {
            val attr = attrIterator.next
            val name = attr.getKey
            val value = attr.getValue
            // load event attribute
            if(DOMHelper.isLoadEventAttribute(name) && value!=null){
              val eventsource = "function __LOADEvent__" + loadevent_count + "(event) { " + value + "}\n"
              loadevent_count += 1
              e_list.add(new Triple(filename, new JInteger(attr.getRowColumnVector().getRow()), eventsource))
            }
            // unload event attribute
            else if(DOMHelper.isUnloadEventAttribute(name) && value!=null){
              val eventsource = "function __UNLOADEvent__" + unloadevent_count + "(event) { " + value + "}\n"
              unloadevent_count += 1
              e_list.add(new Triple(filename, new JInteger(attr.getRowColumnVector().getRow()), eventsource))
            }
            // keyboard event attribute
            else if(DOMHelper.isKeyboardEventAttribute(name) && value!=null){
              val eventsource = "function __KEYBOARDEvent__" + keyboardevent_count + "(event) { " + value + "}\n"
              keyboardevent_count += 1
              e_list.add(new Triple(filename, new JInteger(attr.getRowColumnVector().getRow()), eventsource))
            }
            // mouse event attribute
            else if(DOMHelper.isMouseEventAttribute(name) && value!=null){
              val eventsource = "function __MOUSEEvent__" + mouseevent_count + "(event) { " + value + "}\n"
              mouseevent_count += 1
              e_list.add(new Triple(filename, new JInteger(attr.getRowColumnVector().getRow()), eventsource))
            }
            // message event attribute
            else if(DOMHelper.isMessageEventAttribute(name) && value!=null){
              val eventsource = "function __MESSAGEEvent__" + messageevent_count + "(event) { " + value + "}\n"
              messageevent_count += 1
              e_list.add(new Triple(filename, new JInteger(attr.getRowColumnVector().getRow()), eventsource))
            }
            // other event attribute
            else if(DOMHelper.isOtherEventAttribute(name) && value!=null){
              val eventsource = "function __OTHEREvent__" + otherevent_count + "(event) { " + value + "}\n"
              otherevent_count += 1
              e_list.add(new Triple(filename, new JInteger(attr.getRowColumnVector().getRow()), eventsource))
            }
          }
          event_list:::toList(e_list)
        }
        else event_list
      })
      codecontents.addAll(eventsources)
      Parser.scriptToAST(codecontents)
  }

  private val regex_jquery = """.*jquery[^/]*\.js""".r
  private val regex_mobile = """.*mobile[^/]*\.js""".r

  /* enable model */
  def enableModel(srcname: String): Unit = {
    if (regex_jquery.findFirstIn(srcname).nonEmpty && regex_mobile.findFirstIn(srcname).isEmpty)
      {}  // Config.setJQueryMode
  }

  private def list_regex_lib = List(
    regex_jquery
  )
  /* check library */
  def isModeledLibrary(srcname: String): Boolean = {
    list_regex_lib.exists((regex) =>
      regex_jquery.findFirstIn(srcname).nonEmpty && regex_mobile.findFirstIn(srcname).isEmpty)
  }
}
