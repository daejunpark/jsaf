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
import kr.ac.kaist.jsaf.analysis.typing.{InitHeap, Helper, Config, CallContext}
import scala.collection.immutable.TreeMap
import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.interpreter.InterpreterPredefine
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.nodes_util.NodeUtil
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import org.w3c.dom._
import org.w3c.dom.Node
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml5._

class DOMBuilder(cfg: CFG, init: InitHeap, document: Node) {
  val F = BoolFalse
  val T = BoolTrue
  val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("DOM Object"))
  var initHeap = init.getInitHeap()

  var fset_dom = Map[FunctionId, String]()
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

  // Add the instance object in the heap 
  private def addInstance(heap: HeapMap, loc_ins: Loc, list_ins: List[(String, PropValue)]): HeapMap = {
    // create the instance object and update properties
    val obj_ins = list_ins.foldLeft(ObjEmpty) ((obj, v) => obj.update(AbsString.alpha(v._1), v._2))
    heap + (loc_ins -> obj_ins)
  }

  // Initialize named properties in Document
  // WHATWG HTML Living Standard - Section 3.1.4 DOM tree accessors
  private def initDocumentNamedProps(h: HeapMap, node: Node, ins_loc: Loc) : HeapMap = {
    val nodeName = node.getNodeName
    node match {
      // Element node
      case e: Element => 
        // document object
        val docobj = h(HTMLDocument.GlobalDocumentLoc)
        val id = e.getAttribute("id")
        val name = e.getAttribute("name")
        nodeName match {
          // TODO: HTMLEmbedElement(not modeled yet), name propertis for HTMLIFrameElement
          case "APPLET" | "OBJECT" | "IMG" if id != "" && (nodeName!="IMG" || name!="") => 
            val propval = docobj(id)
            // in case that the 'id' property does not exist
            if(propval._2 </ AbsentBot) {
               val new_docobj = docobj.update(AbsString.alpha(id), PropValue(ObjectValue(ins_loc, T, T, T)))
               h + (HTMLDocument.GlobalDocumentLoc -> new_docobj)
            }
            // in case that the 'id' property already exists
            else {
               // we know only a single location can be mapped for 'id' in the initial heap
               val loc_existing = propval._1._1._1._2.head
               val obj_existing = h(loc_existing)
               // length value
               val tagName = obj_existing("tagName")
               // in case that the 'tagName' property exists: DOM element
               if(tagName._2 <= AbsentBot) {
                 // HTMLCollection
                 val loc_collection = HTMLCollection.getInstance(cfg).get
                 val collection_proplist = HTMLCollection.getInsList(2) ::: 
                   List(("0", PropValue(ObjectValue(loc_existing, T, T, T))), ("1", PropValue(ObjectValue(ins_loc, T, T, T))))
                 val new_docobj = docobj.update(AbsString.alpha(id), PropValue(ObjectValue(loc_collection, T, T, T)))
                 addInstance(h, loc_collection, collection_proplist) + (HTMLDocument.GlobalDocumentLoc -> new_docobj)
               }
               // in case that the 'tagName' property does not exist: HTMLCollection
               else {
                 // we know a concrete value for the property value is always present in the initial heap
                 val collection_length: Int = AbsNumber.concretize(obj_existing("length")._1._1._1._1._4).get.toInt
                 val new_collection = obj_existing.update(
                   AbsString.alpha(collection_length.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
                   AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(collection_length+1), T, T, T)))
                 h + (loc_existing -> new_collection)
               }
            }

          case "APPLET" | "FORM" | "IFRAME" | "IMG" | "OBJECT" if name != "" =>
            val propval = docobj(name)
            // in case that the 'name' property does not exist
            if(propval._2 </ AbsentBot) {
               val new_docobj = docobj.update(AbsString.alpha(name), PropValue(ObjectValue(ins_loc, T, T, T)))
               h + (HTMLDocument.GlobalDocumentLoc -> new_docobj)
            }
            // in case that the 'name' property already exists
            else {
               // we know only a single location can be mapped for 'name' in the initial heap
               val loc_existing = propval._1._1._1._2.head
               val obj_existing = h(loc_existing)
               // length value
               val tagName = obj_existing("tagName")
               // in case that the 'tagName' property exists: DOM element
               if(tagName._2 <= AbsentBot) {
                 // HTMLCollection
                 val loc_collection = HTMLCollection.getInstance(cfg).get
                 val collection_proplist = HTMLCollection.getInsList(2) ::: 
                   List(("0", PropValue(ObjectValue(loc_existing, T, T, T))), ("1", PropValue(ObjectValue(ins_loc, T, T, T))))
                 val new_docobj = docobj.update(AbsString.alpha(name), PropValue(ObjectValue(loc_collection, T, T, T)))
                 addInstance(h, loc_collection, collection_proplist) + (HTMLDocument.GlobalDocumentLoc -> new_docobj)
               }
               // in case that the 'tagName' property does not exists: HTMLCollection
               else {
                 // we know a concrete value for the property value is always present in the initial heap
                 val collection_length: Int = AbsNumber.concretize(obj_existing("length")._1._1._1._1._4).get.toInt
                 val new_collection = obj_existing.update(
                   AbsString.alpha(collection_length.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
                   AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(collection_length+1), T, T, T)))
                 h + (loc_existing -> new_collection)
               }
            }
          case _ => h
      }
      // Non-element node
      case _ => h
    }
  }

  // Initialize the id lookup table, name lookup table, tag lookup table 
  // for getElementById, getElementsByName, getElementsByTagName,
  // also initialize the event target table
  private def initLookupTables(heap: HeapMap, node: Node, ins_loc: Loc) : HeapMap = node match {
    // Element node
    case e: Element => 
      /* id look-up table update */
      val id_table = heap(IdTableLoc)
      val id = e.getAttribute("id")
      val new_idmap = 
      // if the element has an id,
        if(id!="") {
          val mapped_node = id_table(id)
          // DOM Level 3 Core : If more than one element has an ID attribute with that value, 
          //   what is returned is undefined
          val new_value =  
            // in case that the mapping does not exist
            if(mapped_node._2 </ AbsentBot) Value(ins_loc)
            // in case that the mapping already exists
            else {
              System.err.println("* Warning: More than one element has the ID, " + id + ".")  
              Value(UndefTop)
            }
          val new_id_table = id_table.update(AbsString.alpha(id), PropValue(ObjectValue(new_value, T, T, T)))
         (heap + (IdTableLoc -> new_id_table))
        }
        else heap

       /* name look-up table update */
       val name_table = new_idmap(NameTableLoc)
       val name = e.getAttribute("name")
       val new_namemap =
       // if the element has a name,
         if(name!=""){
           val mapped_node = name_table(name)
            // in case that the mapping does not exist
           if(mapped_node._2 </ AbsentBot) {
             val loc_nodelist = DOMNodeList.getInstance(cfg).get
             val nodelist_proplist = DOMNodeList.getInsList(1) :+
               ("0", PropValue(ObjectValue(ins_loc, T, T, T)))
             val new_name_table = name_table.update(AbsString.alpha(name), PropValue(ObjectValue(loc_nodelist, T, T, T)))
             addInstance(new_idmap, loc_nodelist, nodelist_proplist) + (NameTableLoc -> new_name_table)
           }
           // in case that the mapping already exists
           else {
             // we know only a single location can be mapped for a name in the inital heap  
             val loc_nodelist = mapped_node._1._1._1._2.head
             val obj_nodelist = new_idmap(loc_nodelist)
             // we know a concreate value for the mapped element size is always present in the initial heap  
             val nodelist_length: Int = AbsNumber.concretize(obj_nodelist("length")._1._1._1._1._4).get.toInt
             val new_nodelist = obj_nodelist.update(
               AbsString.alpha(nodelist_length.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
               AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(nodelist_length + 1), T, T, T)))
             (new_idmap + (loc_nodelist -> new_nodelist))
           }
         }
         else new_idmap 
         
       /* tag look-up table update */
       val tag_table = new_idmap(TagTableLoc)
       val tag = e.getTagName
       val new_tagmap =
       // if the element has a tag name,
         if(tag!=null){
           val mapped_node = tag_table(tag)
           // in case that the mapping does not exist
           if(mapped_node._2 </ AbsentBot) {
             val loc_nodelist = DOMNodeList.getInstance(cfg).get
             val nodelist_proplist = DOMNodeList.getInsList(1) :+
               ("0", PropValue(ObjectValue(ins_loc, T, T, T)))
             val new_tag_table = tag_table.update(AbsString.alpha(tag), PropValue(ObjectValue(loc_nodelist, T, T, T)))
             addInstance(new_namemap, loc_nodelist, nodelist_proplist) + (TagTableLoc -> new_tag_table)
           }
           // in case that the mapping already exists
           else {
             // we know only a single location can be mapped for a name in the inital heap  
             val loc_nodelist = mapped_node._1._1._1._2.head
             val obj_nodelist = new_namemap(loc_nodelist)
             // we know a concreate value for the mapped element size is always present in the initial heap  
             val nodelist_length: Int = AbsNumber.concretize(obj_nodelist("length")._1._1._1._1._4).get.toInt
             val new_nodelist = obj_nodelist.update(
               AbsString.alpha(nodelist_length.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
               AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(nodelist_length + 1), T, T, T)))
             (new_namemap + (loc_nodelist -> new_nodelist))
           }
         }
         else new_namemap 

       /* class look-up table update */
       val class_table = new_tagmap(ClassTableLoc)
       val classname = e.getAttribute("class")
       val new_classmap =
       // if the element has a class name,
         if(classname!=""){
           val mapped_node = class_table(classname)
           // in case that the mapping does not exist
           if(mapped_node._2 </ AbsentBot) {
             val loc_nodelist = DOMNodeList.getInstance(cfg).get
             val nodelist_proplist = DOMNodeList.getInsList(1) :+
               ("0", PropValue(ObjectValue(ins_loc, T, T, T)))
             val new_class_table = class_table.update(AbsString.alpha(classname), PropValue(ObjectValue(loc_nodelist, T, T, T)))
             addInstance(new_tagmap, loc_nodelist, nodelist_proplist) + (ClassTableLoc -> new_class_table)
           }
           // in case that the mapping already exists
           else {
             // we know only a single location can be mapped for a class name in the inital heap  
             val loc_nodelist = mapped_node._1._1._1._2.head
             val obj_nodelist = new_tagmap(loc_nodelist)
             // we know a concreate value for the mapped element size is always present in the initial heap  
             val nodelist_length: Int = AbsNumber.concretize(obj_nodelist("length")._1._1._1._1._4).get.toInt
             val new_nodelist = obj_nodelist.update(
               AbsString.alpha(nodelist_length.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
               AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(nodelist_length + 1), T, T, T)))
             (new_tagmap + (loc_nodelist -> new_nodelist))
           }
         }
         else new_tagmap 

       /* event target table update */
       val event_target_table = new_classmap(EventTargetTableLoc)
       val load_targets: LocSet = event_target_table("#LOAD")._1._2._2
       val unload_targets: LocSet = event_target_table("#UNLOAD")._1._2._2
       val keyboard_targets: LocSet = event_target_table("#KEYBOARD")._1._2._2
       val mouse_targets: LocSet = event_target_table("#MOUSE")._1._2._2
       val other_targets: LocSet = event_target_table("#OTHER")._1._2._2

       val hasLoadEvent: Boolean = e.getAttribute("load")!="" || e.getAttribute("onload")!=""
       val hasUnloadEvent: Boolean = e.getAttribute("unload")!="" || e.getAttribute("onunload")!=""
       val hasKeyboardEvent: Boolean = e.getAttribute("onkeypress")!="" || e.getAttribute("onkeydown")!="" || e.getAttribute("onkeyup")!=""
       val hasMouseEvent: Boolean = e.getAttribute("onclick")!="" || e.getAttribute("ondbclick")!="" || e.getAttribute("onmousedown")!="" || 
                                    e.getAttribute("onmouseup")!="" || e.getAttribute("onmouseover")!="" || e.getAttribute("onmousemove")!="" ||
                                    e.getAttribute("onmouseout")!=""
       val hasOtherEvent: Boolean = e.getAttribute("onfocus")!="" || e.getAttribute("onblur")!="" || e.getAttribute("onsubmit")!="" ||
                                    e.getAttribute("onreset")!="" || e.getAttribute("onselect")!="" || e.getAttribute("onchange")!="" ||
                                    e.getAttribute("onresize")!="" || e.getAttribute("onselectstart")!=""
       val event_target_table_1 = 
         if(hasLoadEvent) event_target_table.update(AbsString.alpha("#LOAD"), PropValue(Value(load_targets + ins_loc)))
         else event_target_table
       val event_target_table_2 = 
         if(hasUnloadEvent) event_target_table_1.update(AbsString.alpha("#UNLOAD"), PropValue(Value(unload_targets + ins_loc)))
         else event_target_table_1
       val event_target_table_3 = 
         if(hasKeyboardEvent) event_target_table_2.update(AbsString.alpha("#KEYBOARD"), PropValue(Value(keyboard_targets + ins_loc)))
         else event_target_table_2
       val event_target_table_4 = 
         if(hasMouseEvent) event_target_table_3.update(AbsString.alpha("#MOUSE"), PropValue(Value(mouse_targets + ins_loc)))
         else event_target_table_3
       val event_target_table_5 = 
         if(hasOtherEvent) event_target_table_4.update(AbsString.alpha("#OTHER"), PropValue(Value(other_targets + ins_loc)))
         else event_target_table_4
      
       new_classmap + (EventTargetTableLoc -> event_target_table_5)

  // non-Element node
    case _ => heap
  }

  // update the 'form' property of target object and 'elements' property in HTMLFormElement
  private def updateFormProps(heap: HeapMap, name: String, id: String, formloc : Option[Loc], targetloc : Loc): HeapMap = {
    formloc match {
      // update the 'elements' property in HTMLFormElement
      case Some(l) =>
        // HTMLFormElement object
        val form_obj = heap(l)
        // we know only a single location can be mapped to 'elements' in the initial heap
        val elements_loc = form_obj("elements")._1._1._1._2.head
        val elements_obj = heap(elements_loc)
        // we know the concrete value of a  property value is always present in the initial heap
        val collection_length: Int = AbsNumber.concretize(elements_obj("length")._1._1._1._1._4).get.toInt
          val new_elements1 = elements_obj.update(
            AbsString.alpha(collection_length.toString), PropValue(ObjectValue(targetloc, T, T, T))).update(
            AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(collection_length+1), T, T, T)))
          val new_elements2 = if(name!="") new_elements1.update(AbsString.alpha(name), PropValue(ObjectValue(targetloc, T, T, T)))
                              else new_elements1
          val new_elements3 = if(id!="") new_elements2.update(AbsString.alpha(id), PropValue(ObjectValue(targetloc, T, T, T)))
                              else new_elements2
          // update the 'form' property 
          val new_obj = heap(targetloc).update(AbsString.alpha("form"), PropValue(ObjectValue(l, F, T, T)))
          heap + (elements_loc -> new_elements3) + (targetloc -> new_obj)
       case None => heap
    }
  }
    
  // Model a node in a dom tree
  def modelNode(map: HeapMap, node : Node, form : Option[Loc]) : (HeapMap, Loc) = {
    val nodeName = node.getNodeName
    val (newmap, ins_loc) = node match {
      // Attr
      case a: Attr =>
        val loc = DOMAttr.getInstance(cfg).get
        val newheap=addInstance(map, loc, DOMAttr.getInsList(node))
        // the Attr object does not have any siblings and parent
        val newAttrObj=newheap(loc).
                        update(OtherStrSingle("previousSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("nextSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("parentNode"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T)))
          (newheap + (loc -> newAttrObj), loc)

      // DocumentType
      case d: DocumentType => 
        val loc = DOMDocumentType.getInstance(cfg).get
        val newheap=addInstance(map, loc, DOMDocumentType.getInsList(node))
        (newheap, loc)
      // Text node
      case t: Text =>
        val loc = DOMText.getInstance(cfg).get
        val newheap=addInstance(map, loc, DOMText.getInsList(node))
        (newheap, loc)
      // Comment node
      case c: Comment =>
        val loc = DOMComment.getInstance(cfg).get
        val newheap=addInstance(map, loc, DOMComment.getInsList(node))
        (newheap, loc)
      // Document
      case d: Document =>
        val loc= HTMLDocument.getInstance(cfg).get
        val newheap=addInstance(map, loc, HTMLDocument.getInsList(node))

        // 'forms' property
        val loc_forms = HTMLCollection.getInstance(cfg).get
        val newheap2 = addInstance(newheap, loc_forms, HTMLCollection.getInsList(0))

        // the root element does not have any siblings and parent
        val newElementObj=newheap2(loc).
                        update(OtherStrSingle("previousSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("nextSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("parentNode"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("forms"),
                                PropValue(ObjectValue(Value(loc_forms), F, T, T)))
          // 'document' property
          val global_object = map(GlobalLoc).update(AbsString.alpha("document"), PropValue(ObjectValue(loc, T, F, T)))
          (newheap2 + (loc -> newElementObj) + (GlobalLoc -> global_object), loc)
      // Element
      case e: Element => 
        val (_newheap, _insloc) = nodeName match {
          case "HTML" =>
            val loc = HTMLHtmlElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLHtmlElement.getInsList(node))
              // update 'documentElement' of HTMLDocument
            val doc_loc = HTMLDocument.getInstance().get
            val new_doc = newheap(doc_loc).
            update(OtherStrSingle("documentElement"), PropValue(ObjectValue(loc, F, T, T)))
            (newheap + (doc_loc -> new_doc), loc)
          case "HEAD" =>
            val loc= HTMLHeadElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLHeadElement.getInsList(node))
            (newheap, loc)
          case "LINK" =>
            val loc= HTMLLinkElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLLinkElement.getInsList(node))
            (newheap, loc)
          case "TITLE" =>
            val loc= HTMLTitleElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLTitleElement.getInsList(node))
            (newheap, loc)
          case "META" =>
            val loc= HTMLMetaElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLMetaElement.getInsList(node))
            (newheap, loc)
          case "BASE" =>
            val loc= HTMLBaseElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLBaseElement.getInsList(node))
            (newheap, loc)
          case "ISINDEX" =>
            val loc= HTMLIsIndexElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLIsIndexElement.getInsList(node))
            (newheap, loc)
          case "STYLE" =>
            val loc= HTMLIsIndexElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLIsIndexElement.getInsList(node))
            (newheap, loc)
          case "BODY" =>
            val loc= HTMLBodyElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLBodyElement.getInsList(node))
            (newheap, loc)
          case "FORM" =>
            val loc= HTMLFormElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLFormElement.getInsList(node))
            /* 'document.forms' update */
            // 'document' object
            val docobj = newheap(HTMLDocument.GlobalDocumentLoc)
            // we know only a single location can be mapped for 'forms' in the initial heap
            val forms_loc = docobj("forms")._1._1._1._2.head
            val forms_obj = newheap(forms_loc)
            // we know a concrete value for the property value is always present in the initial heap
            val collection_length: Int = AbsNumber.concretize(forms_obj("length")._1._1._1._1._4).get.toInt
            val new_forms1 = forms_obj.update(
                   AbsString.alpha(collection_length.toString), PropValue(ObjectValue(loc, T, T, T))).update(
                   AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(collection_length+1), T, T, T)))
            val name = e.getAttribute("name")
            val new_forms2 = if(name!="") new_forms1.update(AbsString.alpha(name), PropValue(ObjectValue(loc, T, T, T)))
                             else new_forms1

            // 'elements' property
            val loc_elements = HTMLCollection.getInstance(cfg).get
            val newheap2 = addInstance(newheap, loc_elements, HTMLCollection.getInsList(0))

            val new_formobj = newheap2(loc).
                        update(OtherStrSingle("elements"),
                                PropValue(ObjectValue(Value(loc_elements), F, T, T)))

            (newheap2 + (forms_loc -> new_forms2) + (loc -> new_formobj), loc)
          case "SELECT" =>
            val loc= HTMLSelectElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLSelectElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), loc)
          case "OPTGROUP" =>
            val loc= HTMLOptGroupElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLOptGroupElement.getInsList(node))
            (newheap, loc)
          case "OPTION" =>
            val loc= HTMLOptionElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLOptionElement.getInsList(node))
            (newheap, loc)
          case "INPUT" =>
            val loc= HTMLInputElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLInputElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), loc)
          case "TEXTAREA" =>
            val loc= HTMLTextAreaElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLTextAreaElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), loc)
          case "BUTTON" =>
            val loc= HTMLButtonElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLButtonElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), loc)
          case "LABEL" =>
            val loc= HTMLLabelElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLLabelElement.getInsList(node))
            (newheap, loc)
          case "FIELDSET" =>
            val loc= HTMLFieldSetElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLFieldSetElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), loc)
          case "LEGEND" =>
            val loc= HTMLLegendElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLLegendElement.getInsList(node))
            (newheap, loc)
          case "UL" =>
            val loc= HTMLUListElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLUListElement.getInsList(node))
            (newheap, loc)
          case "OL" =>
            val loc= HTMLOListElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLOListElement.getInsList(node))
            (newheap, loc)
          case "DL" =>
            val loc= HTMLDListElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLDListElement.getInsList(node))
            (newheap, loc)
          case "DIR" =>
            val loc= HTMLDirectoryElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLDirectoryElement.getInsList(node))
            (newheap, loc)
          case "MENU" =>
            val loc= HTMLMenuElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLMenuElement.getInsList(node))
            (newheap, loc)
          case "LI" =>
            val loc= HTMLLIElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLLIElement.getInsList(node))
            (newheap, loc)
          case "DIV" =>
            val loc= HTMLDivElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLDivElement.getInsList(node))
            (newheap, loc)
          case "P" =>
            val loc= HTMLParagraphElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLParagraphElement.getInsList(node))
            (newheap, loc)
          // Heading element
          case "H1" | "H2" | "H3" | "H4" | "H5" | "H6"  =>
            val loc= HTMLHeadingElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLHeadingElement.getInsList(node))
            (newheap, loc)
          // Quote element
          case "BLACKQUOTE" | "Q" =>
            val loc= HTMLQuoteElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLQuoteElement.getInsList(node))
            (newheap, loc)
          case "PRE" =>
            val loc= HTMLPreElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLPreElement.getInsList(node))
            (newheap, loc)
          case "BR" =>
            val loc= HTMLBRElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLBRElement.getInsList(node))
            (newheap, loc)
          // BASEFONT Element : deprecated
          case "BASEFONT" =>
            val loc= HTMLBaseFontElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLBaseFontElement.getInsList(node))
            (newheap, loc)
          // FONT Element : deprecated
          case "FONT" =>
            val loc= HTMLFontElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLFontElement.getInsList(node))
            (newheap, loc)
          case "HR" =>
            val loc= HTMLHRElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLHRElement.getInsList(node))
            (newheap, loc)
          case "INS" | "DEL" =>
            val loc= HTMLModElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLModElement.getInsList(node))
            (newheap, loc)
          case "A" =>
            val loc= HTMLAnchorElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLAnchorElement.getInsList(node))
            (newheap, loc)
          case "IMG" =>
            val loc= HTMLImageElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLImageElement.getInsList(node))
            (newheap, loc)
          case "OBJECT" =>
            val loc= HTMLObjectElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLObjectElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), loc)
          case "PARAM" =>
            val loc= HTMLParamElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLParamElement.getInsList(node))
            (newheap, loc)
          // APPLET element : deprecated
          case "APPLET" =>
            val loc= HTMLAppletElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLAppletElement.getInsList(node))
            (newheap, loc)
          case "MAP" =>
            val loc= HTMLMapElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLMapElement.getInsList(node))
            (newheap, loc)
          case "AREA" =>
            val loc= HTMLAreaElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLAreaElement.getInsList(node))
            (newheap, loc)
          case "SCRIPT" =>
            val loc= HTMLScriptElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLScriptElement.getInsList(node))
            (newheap, loc)
          case "TABLE" =>
            val loc= HTMLTableElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLTableElement.getInsList(node))
            (newheap, loc)
          case "CAPTION" =>
            val loc= HTMLTableCaptionElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLTableCaptionElement.getInsList(node))
            (newheap, loc)
          case "COL" =>
            val loc= HTMLTableColElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLTableColElement.getInsList(node))
            (newheap, loc)
          case "THEAD" | "TFOOT" | "TBODY" =>
            val loc= HTMLTableSectionElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLTableSectionElement.getInsList(node))
            (newheap, loc)
          case "TR"  =>
            val loc= HTMLTableRowElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLTableRowElement.getInsList(node))
            (newheap, loc)
          case "TH" | "TD"  =>
            val loc=HTMLTableCellElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLTableCellElement.getInsList(node))
            (newheap, loc)
          case "FRAMESET"  =>
            val loc = HTMLFrameSetElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLFrameSetElement.getInsList(node))
            (newheap, loc)
          case "FRAME"  =>
            val loc = HTMLFrameElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLFrameElement.getInsList(node))
            (newheap, loc)
          case "IFRAME"  =>
            val loc = HTMLIFrameElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLIFrameElement.getInsList(node))
            (newheap, loc)
          // Special tags
          case "SUB" | "SUP" | "SPAN" | "BDO" | "BDI" =>
            val loc = HTMLElement.getInstance(cfg).get
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(map, loc, prop_list)
            (newheap, loc)    
          // Font tags
          case "TT" | "I" | "B" | "U" | "S" | "STRIKE" | "BIG" | "SMALL" =>
            val loc = HTMLElement.getInstance(cfg).get
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(map, loc, prop_list)
            (newheap, loc)    
          // Phrase tags
          case "EM" | "STRONG" | "DFN" | "CODE" | "SAMP" | "KBD" | "VAR" | "CITE" | "ACRONYM" | "ABBR" =>
            val loc = HTMLElement.getInstance(cfg).get
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(map, loc, prop_list)
            (newheap, loc)    
          // List tags
          case "DD" | "DT" =>
            val loc = HTMLElement.getInstance(cfg).get
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(map, loc, prop_list)
            (newheap, loc)    
          // etc
          case "NOFRAMES" | "NOSCRIPT" | "ADDRESS" | "CENTER"  =>
            val loc = HTMLElement.getInstance(cfg).get
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(map, loc, prop_list)
            (newheap, loc)  
          case "CANVAS"  =>
            val loc = HTMLCanvasElement.getInstance(cfg).get
            val newheap=addInstance(map, loc, HTMLCanvasElement.getInsList(node))
            (newheap, loc)
          case _ =>
            System.err.println("* Warning: " + node.getNodeName + " - not modeled yet.")
            val loc = HTMLElement.getInstance(cfg).get
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(map, loc, prop_list)
            (newheap, loc)
        }
        HTMLTopElement.setInsLoc(_insloc)
        (_newheap, _insloc)
      case _ =>
        // the node, not modeled yet, gets a dummy location for the 'Element' node 
        val loc =  HTMLElement.getInstance(cfg).get
        val newheap=addInstance(map, loc, List())
        System.err.println("* Warning: " + node.getNodeName + " - not modeled yet.")
        (newheap, loc)

    }

    // 'attributes' property update
    var newmap1=newmap
    val attributes = node.getAttributes
    val attributes_val = if(attributes==null) PropValue(ObjectValue(NullTop, F, T, T))
      else {
        val length = attributes.getLength
        val attributes_loc = DOMNamedNodeMap.getInstance(cfg).get
        var attributes_objlist = DOMNamedNodeMap.getInsList(length)
        for(i <- 0 until length) {
          val attr = attributes.item(i)
          // get abstract location for each attributes object
          val (newheap, attr_loc) = modelSource(newmap1, attr, None)
          newmap1=newheap
          attributes_objlist = attributes_objlist ++ List(
            (i.toString, PropValue(ObjectValue(attr_loc, T, T, T))),
            (attr.getNodeName, PropValue(ObjectValue(attr_loc, T, T, T)))
          )
        }
        newmap1 = addInstance(newmap1, attributes_loc, attributes_objlist)
        PropValue(ObjectValue(attributes_loc, F, T, T))            
      }
    
    val ins_obj_new = newmap1(ins_loc).update(AbsString.alpha("attributes"), attributes_val)
    
    // initialize id, name, tag, and event look-up tables
    val newmap2 = initLookupTables((newmap1 + (ins_loc -> ins_obj_new)), node, ins_loc)
    // initialize named properites in Document
    val newmap3 = initDocumentNamedProps(newmap2, node, ins_loc)
    (newmap3, ins_loc)
  }

  // Construct a DOM tree for the html source
  // 'form' : keeps a location of HTMLFormElement if any.
  def modelSource(map: HeapMap, node : Node, form : Option[Loc]) : (HeapMap, Loc) = {
    
    val children : NodeList = node.getChildNodes
    val num_children = children.getLength
        
    val (newmap1, absloc1) = modelNode(map, node, form)     

    if(num_children == 0) {
      val absloc2 = DOMNodeList.getInstance(cfg).get
      val newmap2 = addInstance(newmap1, absloc2, DOMNodeList.getInsList(0))
      val newElementObj = newmap2(absloc1).
        update(OtherStrSingle("childNodes"),    PropValue(ObjectValue(absloc2, F, T, T))).
        update(OtherStrSingle("firstChild"),    PropValue(ObjectValue(PValue(NullTop), F, T, T))).
        update(OtherStrSingle("lastChild"),     PropValue(ObjectValue(PValue(NullTop), F, T, T)))
      (newmap2 + (absloc1 -> newElementObj), absloc1)
    }

    else {
      var absloc_list : List[Loc]  = List()
      var newmap : HeapMap = newmap1
      val formelement = if(node.getNodeName == "FORM") Some(absloc1) else form
      for(i <- 0 until num_children) {
        val (newmap2, absloc2) = modelSource(newmap, children.item(i), formelement)
        absloc_list = absloc_list ++ List(absloc2)
        newmap = newmap2
      }

      val absloc3 = DOMNodeList.getInstance(cfg).get
      val newmap3 = addInstance(newmap, absloc3, DOMNodeList.getInsList(num_children))
      
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

  // Initilize the event target table and the event function table
  private def initEventTables(heap: HeapMap) : HeapMap =  {
    val eventTTable = heap(EventTargetTableLoc)
    val eventTTable_update = eventTTable.update(
      AbsString.alpha("#LOAD"), PropValue(ObjectValueBot, ValueBot, FunSetBot)).update(
      AbsString.alpha("#UNLOAD"), PropValue(ObjectValueBot, ValueBot, FunSetBot)).update(
      AbsString.alpha("#KEYBOARD"), PropValue(ObjectValueBot, ValueBot, FunSetBot)).update(
      AbsString.alpha("#MOUSE"), PropValue(ObjectValueBot, ValueBot, FunSetBot)).update(
      AbsString.alpha("#OTHER"), PropValue(ObjectValueBot, ValueBot, FunSetBot))

/*
    val eventFTable = heap(EventFunctionTableLoc)
    val eventFTable_update = eventFTable.update(
      AbsString.alpha("#LOAD"), PropValue(ObjectValueBot, ValueBot, DOMHelper.temp_eventMap("#LOAD"))).update(
      AbsString.alpha("#UNLOAD"), PropValue(ObjectValueBot, ValueBot, DOMHelper.temp_eventMap("#UNLOAD"))).update(
      AbsString.alpha("#KEYBOARD"), PropValue(ObjectValueBot, ValueBot, DOMHelper.temp_eventMap("#KEYBOARD"))).update(
      AbsString.alpha("#MOUSE"), PropValue(ObjectValueBot, ValueBot, DOMHelper.temp_eventMap("#MOUSE"))).update(
      AbsString.alpha("#OTHER"), PropValue(ObjectValueBot, ValueBot, DOMHelper.temp_eventMap("#OTHER")))
*/
    heap + (EventTargetTableLoc -> eventTTable_update) // + (EventFunctionTableLoc -> eventFTable_update)
  }
  
  // Set the initial state for the DOM HTML objects depending on the HTML source
  def initHtml(map: HeapMap): HeapMap = {
    // printDom(document, "")
    val map_1 = initEventTables(map)
    val (newmap, absloc) = modelSource(map_1, document, None)
    // printDom(document, "")
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

  def initialize(quite: Boolean): Unit = {
    val s = System.nanoTime
    //val domModel = new DOMModel(cfg)
    // check start address of DOM Tree
    cfg.setHtmlStartAddr
    // put DOM prototype and constructor objects in the initial heap
    val newheap = init.getInitHeap.map
    val m = newheap |>
      initHtml
    // check end address of DOM Tree
    cfg.setHtmlEndAddr
    if(!quite) System.out.println("# Time for initial heap with DOM modeling(ms): "+(System.nanoTime - s) / 1000000.0)
    //builtinmodel.initHeap = Heap(m)
    init.setInitHeap(Heap(m))
    //fset_dom = domModel.fset_dom
  }
}
