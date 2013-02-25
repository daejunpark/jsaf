/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.Helper
import scala.collection.immutable.TreeMap
import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.interpreter.InterpreterPredefine
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.nodes_util.NodeUtil
import kr.ac.kaist.jsaf.analysis.typing.Config
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import kr.ac.kaist.jsaf.analysis.typing.CallContext
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml._

class DOMModel(cfg: CFG, builtinmodel: BuiltinModel, document: Node) {
  val F = BoolFalse
  val T = BoolTrue
  val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("DOM Object"))
  var initHeap = builtinmodel.initHeap
  var fset_builtin = builtinmodel.fset_builtin
  /**
   * Code for |> operator.
   */
  case class ToPipe[A](a: A) {
    def |>[B](f: A => B) = f(a)
  }
  implicit def convert[A](s: A) = ToPipe(s)

  private var uniqueNameCounter = 0
  private def freshName(name: String) = {
    uniqueNameCounter += 1
    "<>DOM<>" + name + "<>" + uniqueNameCounter.toString
  }

  /**
   * Create cfg nodes for a dom function which is composed of ENTRY, single command body, EXIT and EXIT-EXC.
   *
   * @param DOMCall call name
   * @return created function id
   */
  def makeDomApiCFG(DOMCall: String) : FunctionId = {
    val nameArg = freshName("arguments")
    val fid = cfg.newFunction(nameArg, List[CFGId](), List[CFGId](), DOMCall, dummyInfo)
    val node = cfg.newBlock(fid)

    cfg.addEdge((fid, LEntry), node)
    cfg.addEdge(node, (fid,LExit))
    cfg.addExcEdge(node, (fid,LExitExc))
    cfg.addInst(node, 
                CFGDomApiCall(cfg.newInstId, 
                               DOMCall,
                               CFGVarRef(dummyInfo, CFGTempId(nameArg, PureLocalVar)), 
                               cfg.newAddress, cfg.newAddress, cfg.newAddress, cfg.newAddress))

    fset_builtin = fset_builtin + (fid -> DOMCall)

    (fid)
  }

  sealed abstract class AbsProperty
  case class AbsBuiltinFunc(id: String, length: Double) extends AbsProperty
  case class AbsConstValue(v: PropValue) extends AbsProperty

  /**
   * Preparing the given AbsProperty to be updated.
   * If a property is a built-in function, create a new function object and pass it to name, value and object pair.
   * If a property is a constant value, pass it to name, value and object pair. At this time, object is None.
   *
   * @param name the name of each property
   * @param v the value of each property.
   */
  def prepareForUpdate(name: String, v: AbsProperty) = {
    v match {
      case AbsBuiltinFunc(id, length) => {
        val fid = makeDomApiCFG(id)
        val loc = newLoc(id)
        val obj = Helper.NewFunctionObject(Some(fid), None, GlobalSingleton, None, AbsNumber.alpha(length))
          (name, PropValue(ObjectValue(loc, T, F, T)), Some(loc, obj))
      }
      case AbsConstValue(value) => (name, value, None)
    }
  }

  /**
   * Initialize general built-in object.
   */
  def initGeneral(
    name: String,
    loc_con: Loc,
    obj_con: Obj,
    list_con: List[Tuple2[String, AbsProperty]],
    loc_proto: Loc,
    obj_proto: Obj,
    list_proto: List[Tuple2[String, AbsProperty]],
    map: HeapMap
  ): HeapMap
  = initGeneral(name, loc_con, obj_con, list_con, Some(loc_proto, obj_proto, list_proto), map)

  /**
   * Initialize general built-in object.
   */
  def initGeneral(
    name: String,
    loc_con: Loc,
    obj_con: Obj,
    list_con: List[Tuple2[String, AbsProperty]],
    map: HeapMap
  ): HeapMap
  = initGeneral(name, loc_con, obj_con, list_con, None,  map)
  
  /**
   * Initialize general built-in object. Prototype object can be optional(for Math).
   *
   * @param name Built-in object name by which the object can be accessed.
   * @param loc_con Loc value for constructor(or object).
   * @param obj_con Initial object for constructor.
   * @param list_con Property list for constructor.
   * @param proto Loc, initial object, property list for prototype(Optional)
   * @param map global heap
   */
  def initGeneral(
    name: String,
    loc_con: Loc,
    obj_con: Obj,
    list_con: List[Tuple2[String, AbsProperty]],
    proto: Option[(Loc, Obj, List[Tuple2[String, AbsProperty]])],
    map: HeapMap
  ): HeapMap = {

    // Create function dummies for constructor
    val obj_list = list_con.map((x) => prepareForUpdate(x._1, x._2))
    // Add properties to constructor object
    val obj_con_1 = obj_list.foldLeft(obj_con)((obj, v) => obj.update(AbsString.alpha(v._1), v._2))

    // Adds to global area
    val global = map(GlobalLoc).update(AbsString.alpha(name),
                                       PropValue(ObjectValue(loc_con, T, F, T)))
    // Make builtin properties to be dumped only in verbose mode
    if (!name.startsWith("@")) Config.globalVerboseProp.add(name) 
  
    val map_1 = map + (GlobalLoc -> global) + (loc_con -> obj_con_1)

    val map_2 = proto match {
      case Some((loc, obj_proto, list_proto)) => {
        // Create function dummies for prototype object
        val proto_list = list_proto.map((x) => prepareForUpdate(x._1, x._2))
        // Add properties to prototype object
        val obj_proto_1 = proto_list.foldLeft(obj_proto)((obj, v) => obj.update(AbsString.alpha(v._1), v._2))

        // Adds new allocated objects
        val m_1 = map_1 + (loc -> obj_proto_1)
        val m_2 = proto_list.foldLeft(m_1)((m, v) => v._3 match {
          case Some((l, o)) => m + (l -> o)
          case None => m
        })
          m_2
      }
      case _ => map_1
    }

    // Adds new allocated objects
    obj_list.foldLeft(map_2)((m, v) => v._3 match {
      case Some((l, o)) => m + (l -> o)
      case None => m
    })
  }

  // Models the Node object in the DOM Core specification
  def initDOMNode(map: HeapMap): HeapMap = {
    val loc_proto = newLoc("DOMNodeProto")
    val loc_con = newLoc("DOMNodeConst")
    val loc_instance = newLoc("DOMNodeInstance")

    // Constructor object
    val obj_con = ObjEmpty.
      update(OtherStrSingle("@class"),       PropValue(AbsString.alpha("Function"))).
      update(OtherStrSingle("@proto"),       PropValue(ObjectValue(ObjProtoLoc, F, F, F))).
      update(OtherStrSingle("length"),       PropValue(ObjectValue(NumTop, F, F, F))).
      update(OtherStrSingle("prototype"),    PropValue(ObjectValue(loc_proto, F, F, F)))

    // Properties of the constructor object
    val list_con = List()
     

    // Properties of the Node Prototype Object
    val list_proto = List(
      // Fields
      ("@class",                AbsConstValue(PropValue(AbsString.alpha("Object")))),
      ("@proto",                AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
      ("ELEMENT_NODE",          AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1), F, T, T)))),
      ("ATTRIBUTE_NODE",        AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(2), F, T, T)))),
      ("TEXT_NODE",             AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(3), F, T, T)))),
      ("CDATE_SECTION_NODE",    AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(4), F, T, T)))),
      ("ENTITY_SECTION_NODE",   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(5), F, T, T)))),
      ("ENTITY_NODE",           AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(6), F, T, T)))),
      ("PROCESSING_INSTRUCTION_NODE", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(7), F, T, T)))),
      ("COMMENT_NODE",          AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(8), F, T, T)))),
      ("DOCUMENT_NODE",         AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(9), F, T, T)))),
      ("DOCUMENT_TYPE__NODE",   AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(10), F, T, T)))),
      ("DOCUMENT_FRAGMENT_NODE", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(11), F, T, T)))),
      ("NOTATION_NODE",         AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(12), F, T, T)))),
      // DOM LEVEL 1 
      ("nodeName",              AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
      ("nodeValue",             AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
      ("nodeType",              AbsConstValue(PropValue(ObjectValue(NumTop, T, T, T)))),
      ("parentNode",            AbsConstValue(PropValue(ObjectValue(loc_instance, T, T, T)))),
      // TODO : change loc_instance to the location of the NodeList instance object
      ("childNodes",            AbsConstValue(PropValue(ObjectValue(loc_instance, T, T, T)))),
      ("firstChild",            AbsConstValue(PropValue(ObjectValue(loc_instance, T, T, T)))),
      ("lastChild",             AbsConstValue(PropValue(ObjectValue(loc_instance, T, T, T)))),
      ("previousSibling",       AbsConstValue(PropValue(ObjectValue(loc_instance, T, T, T)))),
      ("nextSibling",           AbsConstValue(PropValue(ObjectValue(loc_instance, T, T, T)))),
      // DOM LEVEL 2
      ("prefix",                AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
      ("localName",             AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
      ("namespaceURI",          AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
      // Methods
      // DOM LEVEL 1
      ("appendChild",           AbsBuiltinFunc("Node.prototype.appendChild", 1)),
      ("cloneNode",             AbsBuiltinFunc("Node.prototype.cloneNode", 1)),
      ("hasChildNodes",         AbsBuiltinFunc("Node.prototype.hasChildNodes", 0)),
      ("insertBefore",          AbsBuiltinFunc("Node.prototype.insertBefore", 2)),
      ("removeChild",           AbsBuiltinFunc("Node.prototype.removeChild", 1)),
      ("replaceChild",          AbsBuiltinFunc("Node.prototype.replaceChild", 2)),
      // DOM LEVEL 2
      ("isSupported",           AbsBuiltinFunc("Node.prototype.isSupported", 2)),
      ("hasAttributes",         AbsBuiltinFunc("Node.prototype.isSupported", 0)),
      ("normalize",             AbsBuiltinFunc("Node.prototype.isSupported", 0))

     )

    // Instance object
    val obj_ins = ObjEmpty.
      update(OtherStrSingle("@class"),       PropValue(AbsString.alpha("Object"))).
      update(OtherStrSingle("@proto"),       PropValue(ObjectValue(loc_proto, F, F, F)))

   val map_1 = map + (loc_instance -> obj_ins)
     
   initGeneral("Node", loc_con, obj_con, list_con, loc_proto, ObjEmpty, list_proto, map_1)  

  }

  // Set the initial state for the DOM Core objects
  def initCore(map: HeapMap): HeapMap = {
    map |> initDOMNode
  }

  // Set the initial state for the DOM objects such as prototype and constructor objects
  def initDOM(map: HeapMap): HeapMap = {
    map |> 
      DOMNode.init |>
      DOMNodeList.init |>
      DOMText.init |>
      HTMLElement.init 
  }
  
  // Model each HTML element depending on its kind 
  def modelElement(map: HeapMap, node : Node) : (HeapMap, Loc) = {
    val nodeName = node.getNodeName
    nodeName match {
      // root element
      case "#document" =>
        val (newmap, absloc) = HTMLDocument.instantiate(map, node)
        // the root element does not have any siblings and parent
        val newElementObj=newmap(absloc).
                          update(OtherStrSingle("previousSibling"),
                                  PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                          update(OtherStrSingle("nextSibling"),
                                  PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                          update(OtherStrSingle("parentNode"),
                                  PropValue(ObjectValue(PValue(NullTop), F, T, T)))
        (newmap + (absloc -> newElementObj), absloc)
      case "HTML" =>
        // DocumentType
        if(node.getNodeType == 10)
          DOMDocumentType.instantiate(map, node)
        else {// HTMLHtmlElement 
          HTMLHtmlElement.instantiate(map, node)
        }
      case "HEAD" =>
        HTMLHeadElement.instantiate(map, node)
      case "LINK" =>
        HTMLLinkElement.instantiate(map, node)
      case "TITLE" =>
        HTMLTitleElement.instantiate(map, node)
      case "META" =>
        HTMLMetaElement.instantiate(map, node)
      case "STYLE" =>
        HTMLStyleElement.instantiate(map, node)
      case "BODY" =>
        HTMLBodyElement.instantiate(map, node)
      case "DIV" =>
        HTMLDivElement.instantiate(map, node)
      case "P" =>
        HTMLParagraphElement.instantiate(map, node)
      // Heading element
      case "H1" | "H2" | "H3" | "H4" | "H5" | "H6"  =>
        HTMLHeadingElement.instantiate(map, node)
      case "#text" =>
        DOMText.instantiate(map, node)
      case "SCRIPT" =>
        HTMLScriptElement.instantiate(map, node)
      // Special tags
      case "SUB" | "SUP" | "SPAN" | "BDO" =>
        HTMLElement.instantiate(map, node)
      // Font tags
      case "TT" | "I" | "B" | "U" | "S" | "STRIKE" | "BIG" | "SMALL" =>
        HTMLElement.instantiate(map, node)
      // Phrase tags
      case "EM" | "STRONG" | "DFN" | "CODE" | "SAMP" | "KBD" | "VAR" | "CITE" | "ACRONYM" | "ABBR" =>
        HTMLElement.instantiate(map, node)
      // List tags
      case "DD" | "DT" =>
        HTMLElement.instantiate(map, node)
      // etc
      case "NOFRAMES" | "NOSCRIPT" | "ADDRESS" | "CENTER" | "S"  =>
        HTMLElement.instantiate(map, node)
      case _ =>
        HTMLElement.instantiate(map, node)
   
    }

  }

  def modelSource(map: HeapMap, node : Node) : (HeapMap, Loc) = {
    
    val children : NodeList = node.getChildNodes
    val num_children = children.getLength
        
    val (newmap1, absloc1) = modelElement(map, node)     

    if(num_children == 0) {
      val (newmap2, absloc2) = DOMNodeList.instantiate(newmap1, 0)
      val newElementObj = newmap2(absloc1).
        update(OtherStrSingle("childNodes"),    PropValue(ObjectValue(absloc2, F, T, T))).
        update(OtherStrSingle("firstChild"),    PropValue(ObjectValue(PValue(NullTop), F, T, T))).
        update(OtherStrSingle("lastChild"),     PropValue(ObjectValue(PValue(NullTop), F, T, T)))
      (newmap2 + (absloc1 -> newElementObj), absloc1)
    }

    else { 
      var absloc_list : List[Loc]  = List()
      var newmap : HeapMap = newmap1
      for(i <- 0 until num_children) {
        val (newmap2, absloc2) = modelSource(newmap, children.item(i))
        absloc_list = absloc_list ++ List(absloc2)
        newmap = newmap2
      }
      val (newmap3, absloc3) = DOMNodeList.instantiate(newmap, absloc_list.size)
      
      var children_obj = newmap3(absloc3)
      
      val absobj_list : List[Obj] = absloc_list.zipWithIndex.map(
         ele => {
          val x=ele._1
          val i=ele._2
          // object update for the 'childNodes' field
          children_obj = children_obj.
                        update(NumStrSingle(i.toString),   PropValue(ObjectValue(absloc_list(i), T, T, T)))
          // set the 'parentNode' fields of all children nodes
          val newObj1 = newmap3(x).
                update(OtherStrSingle("parentNode"), PropValue(ObjectValue(absloc1, F, T, T)))
          // set the sibling information
          val newObj2 = if(i==0) newObj1.update(OtherStrSingle("previousSibling"), 
                                                  PropValue(ObjectValue(PValue(NullTop), F, T, T)))
                        else newObj1.update(OtherStrSingle("previousSibling"),   
                                                  PropValue(ObjectValue(absloc_list(i-1), F, T, T)))
          if (i==num_children-1)
            newObj2.update(OtherStrSingle("nextSibling"),   
                                                  PropValue(ObjectValue(PValue(NullTop), F, T, T)))
          else
            newObj2.update(OtherStrSingle("nextSibling"),   
                                                  PropValue(ObjectValue(absloc_list(i+1), F, T, T)))
        })

      // set the children information in the parent node
      val newElementObj=newmap3(absloc1).
                         update(OtherStrSingle("childNodes"),   PropValue(ObjectValue(absloc3, F, T, T))).
                         update(OtherStrSingle("firstChild"),   PropValue(ObjectValue(absloc_list(0), F, T, T))).
                         update(OtherStrSingle("lastChild"),   PropValue(ObjectValue(absloc_list(num_children-1), F, T, T)))

      val finalmap : HeapMap = ((absloc_list zip absobj_list).foldLeft(newmap3)((x, y) => x + (y._1 -> y._2)))

      (finalmap + (absloc3 -> children_obj) + (absloc1 -> newElementObj), absloc1)
    }

  }
  
  // Set the initial state for the DOM HTML objects depending on the HTML source
  def initHtml(map: HeapMap): HeapMap = {
    printDom(document, "")
    val (newmap, absloc) = modelSource(map, document)
    //printDom(document, "")
    //modelSource(toList(source.getChildElements)(0))
    //System.out.println("start")
    //toList(toList(source.getChildElements)(0).getChildElements).foreach((x:Element) => System.out.println(x.getName))
    //System.out.println("end")
    newmap
  }

  // Print the DOM tree
  def printDom(node: Node, indent: String): Unit = {
    System.out.println(indent + node.getNodeName + node.getNodeType + node.getClass().getName())
    var child : Node = node.getFirstChild
    while (child != null) {
      printDom(child, indent+" ")
      child = child.getNextSibling()
    }
  }

  var initPureLocalObj: Obj = null
  def getInitHeapPre() = {
    val initCP = ((cfg.getGlobalFId, LEntry), CallContext.globalCallContext)
    initHeap.update(cfg.getPureLocal(initCP), initPureLocalObj)
  }
  def getInitHeap() = {
    initHeap.update(SinglePureLocalLoc, initPureLocalObj)
  }

  def initialize(): Unit = {
    val s = System.nanoTime
    val F = BoolFalse
    val T = BoolTrue
    
    val m = initHeap.map |>
      initDOM |>
      initHtml
      //initCore 

    System.out.println("# Time for initial heap with DOM modeling(ms): "+(System.nanoTime - s) / 1000000.0)

    builtinmodel.initHeap = Heap(m)
    builtinmodel.fset_builtin = fset_builtin
  }
}
