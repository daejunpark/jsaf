/*******************************************************************************
    Copyright (c) 2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models

import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse=>F, BoolTrue=>T}
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml5._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMObject._
import kr.ac.kaist.jsaf.analysis.typing.{InternalError, Operator, Helper}
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.domain.OtherStrSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.NumStrSingle
import kr.ac.kaist.jsaf.analysis.cfg.CFG
import org.cyberneko.html.parsers._
import org.apache.html.dom.HTMLDocumentImpl
import org.xml.sax.InputSource
import org.w3c.dom.{DOMException=>DE, Element, Node, Document, DocumentFragment}
import org.w3c.dom.{Attr, DocumentType, Text, Comment, NodeList}
import java.io._

object DOMHelper {
  private val validHtmlTags: Set[String] = Set(
   // HTML 4.01
   "HTML", "HEAD", "LINK", "TITLE", "META", "BASE", "ISINDEX", "STYLE", "BODY", "FORM",
   "SELECT", "OPTGROUP", "OPTION", "INPUT", "TEXTAREA", "BUTTON", "LABEL", "FIELDSET",
   "LEGEND", "UL", "OL", "DL", "DIR", "MENU", "LI", "DIV", "P", "H1", "H2", "H3", "H4",
   "H5", "H6", "BLACKQUOTE", "Q", "PRE", "BR", "BASEFONT","FONT", "HR", "INS", "DEL", "A",
   "IMG", "OBJECT", "PARAM", "APPLET", "MAP", "AREA", "SCRIPT", "TABLE", "CAPTION", "COL",
   "THEAD", "TFOOT", "TBODY", "TR", "TH", "TD", "FRAMESET", "FRAME", "IFRAME", "SUB", "SUP",
   "SPAN", "BDO", "TT", "I", "B", "U", "S", "STRIKE", "BIG", "SMALL", "EM", "STRONG", "DFN",
   "CODE", "SAMP", "KBD", "VAR", "CITE", "ACRONYM", "ABBR", "DD", "DT", "NOFRAMES", "NOSCRIPT",
   "ADDRESS", "CENTER",
   // HTML 5
   "CANVAS"
  )

  var temp_eventMap: HashMap[String, FunSet] = HashMap(
    ("#LOAD", FunSetBot), ("#UNLOAD", FunSetBot), ("#KEYBOARD", FunSetBot), ("#MOUSE", FunSetBot), ("#OTHER", FunSetBot))

  val InterfaceObject =
    ObjEmpty.
      update("@class", PropValue(AbsString.alpha("Function"))).
      update("@proto", PropValue(ObjectValue(ObjProtoLoc, BoolFalse, BoolFalse, BoolFalse))).
      update("@extensible", PropValue(BoolTrue)).
      update("@hasinstance", PropValue(Value(NullTop)))


  // check if a given tag name is a valid HTML tag name
  def isValidHtmlTag(tagname: String): Boolean = validHtmlTags.contains(tagname)
  
  // Return a property list of an element with the given tag name
  def getInsList(tagname: String): List[(String, PropValue)] = tagname match {
    case "HTML" => HTMLHtmlElement.default_getInsList
    /* Not yet implemented */
    case "HEAD" => HTMLHeadElement.default_getInsList
    case "LINK" => HTMLLinkElement.default_getInsList
    case "TITLE" => HTMLTitleElement.default_getInsList
    case "META" => HTMLMetaElement.default_getInsList
    case "BASE" => HTMLBaseElement.default_getInsList
    case "ISINDEX" => HTMLIsIndexElement.default_getInsList
    case "STYLE" => HTMLStyleElement.default_getInsList
    case "BODY" => HTMLBodyElement.default_getInsList
    case "FORM" => HTMLFormElement.default_getInsList
    case "SELECT" => HTMLSelectElement.default_getInsList
    case "OPTGROUP" => HTMLOptGroupElement.default_getInsList
    case "OPTION" => HTMLOptionElement.default_getInsList
    case "INPUT" => HTMLInputElement.default_getInsList
    case "TEXTAREA" => HTMLTextAreaElement.default_getInsList
    case "BUTTON" => HTMLButtonElement.default_getInsList
    case "LABEL" => HTMLLabelElement.default_getInsList
    case "FIELDSET" => HTMLFieldSetElement.default_getInsList
    case "LEGEND" => HTMLLegendElement.default_getInsList
    case "UL" => HTMLUListElement.default_getInsList
    case "OL" => HTMLOListElement.default_getInsList
    case "DL" => HTMLDListElement.default_getInsList
    case "DIR" => HTMLDirectoryElement.default_getInsList
    case "MENU" => HTMLMenuElement.default_getInsList
    case "LI" => 
      HTMLElement.default_getInsList:::List(
        ("@class",  PropValue(AbsString.alpha("Object"))),
        ("@proto",  PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
        ("@extensible",  PropValue(BoolTrue)))
    case "DIV" => HTMLDivElement.default_getInsList
    case "P" => HTMLParagraphElement.default_getInsList
    // Heading element
    case "H1" | "H2" | "H3" | "H4" | "H5" | "H6"  =>
      HTMLHeadingElement.default_getInsList
    // Quote element
    case "BLACKQUOTE" | "Q" => HTMLQuoteElement.default_getInsList
    case "PRE" => HTMLPreElement.default_getInsList
    case "BR" => HTMLBRElement.default_getInsList
    // BASEFONT Element : deprecated
    case "BASEFONT" => HTMLBaseFontElement.default_getInsList
    // FONT Element : deprecated
    case "FONT" => HTMLFontElement.default_getInsList
    case "HR" => HTMLHRElement.default_getInsList
    case "INS" | "DEL" => HTMLModElement.default_getInsList
    case "A" => HTMLAnchorElement.default_getInsList
    case "IMG" => HTMLImageElement.default_getInsList
    case "OBJECT" => HTMLObjectElement.default_getInsList
    case "PARAM" => HTMLParamElement.default_getInsList
    // APPLET element : deprecated
    case "APPLET" => HTMLAppletElement.default_getInsList
    case "MAP" => HTMLMapElement.default_getInsList
    case "AREA" => HTMLAreaElement.default_getInsList
    case "SCRIPT" => HTMLScriptElement.default_getInsList
    case "TABLE" => HTMLTableElement.default_getInsList
    case "CAPTION" => HTMLTableCaptionElement.default_getInsList
    case "COL" => HTMLTableColElement.default_getInsList
    case "THEAD" | "TFOOT" | "TBODY" => HTMLTableSectionElement.default_getInsList
    case "TR"  => HTMLTableRowElement.default_getInsList
    case "TH" | "TD"  => HTMLTableCellElement.default_getInsList
    case "FRAMESET"  => HTMLFrameSetElement.default_getInsList
    case "FRAME"  => HTMLFrameElement.default_getInsList
    case "IFRAME"  => HTMLIFrameElement.default_getInsList
    // Special tags
    case "SUB" | "SUP" | "SPAN" | "BDO" =>
      HTMLElement.default_getInsList:::List(
        ("@class",  PropValue(AbsString.alpha("Object"))),
        ("@proto",  PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
        ("@extensible",  PropValue(BoolTrue)))
    // Font tags
    case "TT" | "I" | "B" | "U" | "S" | "STRIKE" | "BIG" | "SMALL" =>
      HTMLElement.default_getInsList:::List(
        ("@class",  PropValue(AbsString.alpha("Object"))),
        ("@proto",  PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
        ("@extensible",  PropValue(BoolTrue)))
    // Phrase tags
    case "EM" | "STRONG" | "DFN" | "CODE" | "SAMP" | "KBD" | "VAR" | "CITE" | "ACRONYM" | "ABBR" =>
      HTMLElement.default_getInsList:::List(
        ("@class",  PropValue(AbsString.alpha("Object"))),
        ("@proto",  PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
        ("@extensible",  PropValue(BoolTrue)))
    // List tags
    case "DD" | "DT" =>
      HTMLElement.default_getInsList:::List(
        ("@class",  PropValue(AbsString.alpha("Object"))),
        ("@proto",  PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
        ("@extensible",  PropValue(BoolTrue)))
    // etc
    case "NOFRAMES" | "NOSCRIPT" | "ADDRESS" | "CENTER"  =>
      HTMLElement.default_getInsList:::List(
        ("@class",  PropValue(AbsString.alpha("Object"))),
        ("@proto",  PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
        ("@extensible",  PropValue(BoolTrue)))
    // HTML5
    case "CANVAS"  =>
      HTMLCanvasElement.default_getInsList
    case "DATALIST"  =>
      HTMLDataListElement.default_getInsList
    case _ =>
      HTMLUnknownElement.default_getInsList
  }
  
  def RaiseDOMException(h:Heap, ctx:Context, es:Set[Int]): (Heap,Context) = {
    if (es.isEmpty)
      (HeapBot, ContextBot)
    else {
      val v_old = h(SinglePureLocalLoc)("@exception_all")._1._2
      val v_e = Value(PValueBot,
        es.foldLeft(LocSetBot)((lset,exc)=> lset + DOMExceptionLoc(exc)))
      val h_1 = h.update(SinglePureLocalLoc,
        h(SinglePureLocalLoc).update("@exception", PropValue(v_e)).
          update("@exception_all", PropValue(v_e + v_old)))
      (h_1,ctx)
    }
  }
 
  def PreRaiseDOMException(h:Heap, ctx:Context, PureLocalLoc: Loc, es:Set[Int]): (Heap,Context) = {
    if (es.isEmpty)
      (h, ctx)
    else {
      val v_old = h(PureLocalLoc)("@exception_all")._1._2
      val v_e = Value(PValueBot,
        es.foldLeft(LocSetBot)((lset,exc)=> lset + DOMExceptionLoc(exc)))
      val h_1 = h.update(PureLocalLoc,
        h(PureLocalLoc).update("@exception", PropValue(v_e)).
          update("@exception_all", PropValue(v_e + v_old)))
      (h_1,ctx)
    }
  }
 
  def RaiseDOMException_def(es:Set[Int]): LPSet = {
    if (es.isEmpty)
      (LPBot)
    else {
      LPSet(Set((SinglePureLocalLoc, "@exception_all"), (SinglePureLocalLoc, "@exception")))
    }
  }
 
  def RaiseDOMException_use(es:Set[Int]): LPSet = {
    if (es.isEmpty)
      (LPBot)
    else {
      LPSet((SinglePureLocalLoc, "@exception_all"))
    }
  }

  def DOMExceptionLoc(exc: Int): Loc = {
    exc match {
      case DOMException.INDEX_SIZE_ERR =>              DOMException.DOMErrIndexSize
      case DOMException.DOMSTRING_SIZE_ERR =>          DOMException.DOMErrDomstringSize
      case DOMException.HIERARCHY_REQUEST_ERR =>       DOMException.DOMErrHierarchyRequest
      case DOMException.WRONG_DOCUMENT_ERR =>          DOMException.DOMErrWrongDocument
      case DOMException.INVALID_CHARACTER_ERR =>       DOMException.DOMErrInvalidCharacter
      case DOMException.NO_DATA_ALLOWED_ERR =>         DOMException.DOMErrNoDataAllowed
      case DOMException.NO_MODIFICATION_ALLOWED_ERR => DOMException.DOMErrNoModificationAllowed
      case DOMException.NOT_FOUND_ERR =>               DOMException.DOMErrNotFound
      case DOMException.NOT_SUPPORTED_ERR =>           DOMException.DOMErrNotSupported
      case DOMException.INUSE_ATTRIBUTE_ERR =>         DOMException.DOMErrInuseAttribute
      case DOMException.INVALID_STATE_ERR =>           DOMException.DOMErrInvalidState
      case DOMException.SYNTAX_ERR =>                  DOMException.DOMErrSyntax
      case DOMException.INVALID_MODIFICATION =>        DOMException.DOMErrInvalidModification
      case DOMException.NAMESPACE_ERR =>               DOMException.DOMErrNamespace
      case DOMException.INVALID_ACCESS_ERR =>          DOMException.DOMErrInvalidAccess
      case DOMException.VALIDATION_ERR =>              DOMException.DOMErrValidation
      case DOMException.TYPE_MISMATCH_ERR =>           DOMException.DOMErrTypeMismatch
    }
  }

  def addEventHandler(h: Heap, s: AbsString, v_fun: Value, v_target: Value): Heap = {
    val fun_table = h(EventFunctionTableLoc)
    val target_table = h(EventTargetTableLoc)
    val propv_fun = PropValue(v_fun)
    val propv_target = PropValue(v_target)
    val event_list = s match {
      case StrTop | OtherStr => List("#LOAD", "#UNLOAD", "#KEYBOARD", "#MOUSE", "#MESSAGE", "#OTHER")
      case OtherStrSingle(s_ev) =>
        if (isLoadEventAttribute(s_ev)) List("#LOAD")
        else if (isUnloadEventAttribute(s_ev)) List("#UNLOAD")
        else if (isKeyboardEventAttribute(s_ev) || isKeyboardEventProperty(s_ev)) List("#KEYBOARD")
        else if (isMouseEventAttribute(s_ev) || isMouseEventProperty(s_ev)) List("#MOUSE")
        else if (isMessageEventAttribute(s_ev) || isMessageEventProperty(s_ev)) List("#MESSAGE")
        else if (isOtherEventAttribute(s_ev) || isOtherEventProperty(s_ev))List("#OTHER")
        else List()
      case NumStrSingle(_) => /* Error ?*/ List()
      case NumStr => /* Error ?*/ List()
      case StrBot => List()
    }
    val o_fun = event_list.foldLeft(fun_table)((o, s_ev) =>
      o.update(s_ev, o(s_ev)._1 + propv_fun)
    )
    val o_target = event_list.foldLeft(target_table)((o, s_ev) =>
      o.update(s_ev, o(s_ev)._1 + propv_target)
    )
    h.update(EventFunctionTableLoc, o_fun).update(EventTargetTableLoc, o_target)
  }

  def addEventHandler_def(h: Heap, s: AbsString): LPSet = {
    val event_list = s match {
      case StrTop | OtherStr => List("#LOAD", "#UNLOAD", "#KEYBOARD", "#MOUSE", "#OTHER")
      case OtherStrSingle(s_ev) =>
        if (isLoadEventAttribute(s_ev)) List("#LOAD")
        else if (isUnloadEventAttribute(s_ev)) List("#UNLOAD")
        else if (isKeyboardEventAttribute(s_ev) || isKeyboardEventProperty(s_ev)) List("#KEYBOARD")
        else if (isMouseEventAttribute(s_ev) || isMouseEventProperty(s_ev)) List("#MOUSE")
        else if (isOtherEventAttribute(s_ev) || isOtherEventProperty(s_ev))List("#OTHER")
        else List()
      case NumStrSingle(_) => /* Error ?*/ List()
      case NumStr => /* Error ?*/ List()
      case StrBot => List()
    }
    val LP1 = event_list.foldLeft(LPBot)((lpset, s_ev) =>
      lpset + (EventFunctionTableLoc, s_ev)
    )
    val LP2 = event_list.foldLeft(LPBot)((lpset, s_ev) =>
      lpset + (EventTargetTableLoc, s_ev)
    )
    LP1 ++ LP2
  }

  def addEventHandler_use(h: Heap, s: AbsString): LPSet = {
    val event_list = s match {
      case StrTop | OtherStr => List("#LOAD", "#UNLOAD", "#KEYBOARD", "#MOUSE", "#OTHER")
      case OtherStrSingle(s_ev) =>
        if (isLoadEventAttribute(s_ev)) List("#LOAD")
        else if (isUnloadEventAttribute(s_ev)) List("#UNLOAD")
        else if (isKeyboardEventAttribute(s_ev) || isKeyboardEventProperty(s_ev)) List("#KEYBOARD")
        else if (isMouseEventAttribute(s_ev) || isMouseEventProperty(s_ev)) List("#MOUSE")
        else if (isOtherEventAttribute(s_ev) || isOtherEventProperty(s_ev))List("#OTHER")
        else List()
      case NumStrSingle(_) => /* Error ?*/ List()
      case NumStr => /* Error ?*/ List()
      case StrBot => List()
    }
    val LP1 = event_list.foldLeft(LPBot)((lpset, s_ev) =>
      lpset + (EventFunctionTableLoc, s_ev)
    )
    val LP2 = event_list.foldLeft(LPBot)((lpset, s_ev) =>
      lpset + (EventTargetTableLoc, s_ev)
    )
    LP1 ++ LP2
  }


  def isLoadEventAttribute(attr: String): Boolean = { 
    attr=="load" || attr=="onload"
  }

  def isUnloadEventAttribute(attr: String): Boolean = { 
    attr=="unload" || attr=="onunload" 
  }

  def isKeyboardEventAttribute(attr: String): Boolean = { 
    attr=="onkeypress" || attr=="onkeydown" || attr=="onkeyup" 
  }
  def isKeyboardEventProperty(attr: String): Boolean = {
    attr=="keypress" || attr=="keydown" || attr=="keyup"
  }

  def isMouseEventAttribute(attr: String): Boolean = {
    attr=="onclick" || attr=="ondbclick" || attr=="onmousedown" || attr=="onmouseup" ||
    attr=="onmouseover" || attr=="onmousemove" || attr=="onmouseout"
  }
  def isMouseEventProperty(attr: String): Boolean = {
    attr=="click" || attr=="dbclick" || attr=="mousedown" || attr=="mouseup" ||
    attr=="mouseover" || attr=="mousemove" || attr=="mouseout" ||
      // for jQuery
    attr=="scroll" || attr=="mouseleave" || attr=="mouseenter"
  }

  def isMessageEventAttribute(attr: String): Boolean = {
    attr=="onmessage"
  }
  def isMessageEventProperty(attr: String): Boolean = {
    attr=="message"
  }

  def isOtherEventAttribute(attr: String): Boolean = {
    attr=="onfocus" || attr=="onblur" || attr=="onsubmit" || attr=="onreset" || 
    attr=="onselect" || attr=="onchange" || attr=="onresize" || attr=="onselectstart"
  }

  def isOtherEventProperty(attr: String): Boolean = {
    attr=="focus" || attr=="blur" || attr=="submit" || attr=="reset" ||
    attr=="select" || attr=="change" || attr=="resize" || attr=="selectstart" ||
    // DOM Events Level 3
    attr=="compositionstart" || attr=="compositionend" || 
    // HTML 5
    attr=="hashchange" ||
    // input
    attr=="input" ||
    // for jQuery
    attr=="error" || attr == "focusin" || attr =="focusout"
  }

  def isReadyEventProperty(attr: String): Boolean = {
    attr == "DOMContentLoaded" || attr == "onreadystatechange"
  }
  
  /* DOM Tree API */
  // check if a dom node with the location 'l_target' is an inclusive descendant of the node with the locaiton 'l_root'
  def contains(h: Heap, lset_visited: LocSet, l_root: Loc, l_target: Loc): Boolean = {
    if (!lset_visited.contains(l_root)) {
      if(l_root == l_target)
        true
      else {
        // check childrens of 'l_root'next elements
        val lset_children = Helper.Proto(h, l_root, AbsString.alpha("childNodes"))._2.foldLeft(LocSetBot)((lset, l_n) =>
          lset ++ Helper.Proto(h, l_n, NumStr)._2)
        lset_children.foldLeft(false)((b, l_child) => 
          if(contains(h, lset_visited + l_root, l_child, l_target)==false) 
            b
          else
            true
         )
      }
    }
    else
      false
  }

  def findByAttr(h: Heap, l_root: Loc, attr_name: String, s: AbsString, contain: Boolean): LocSet = {
    val ab_attr_name = AbsString.alpha(attr_name)
    val ab_childNodes = AbsString.alpha("childNodes")
    def search(lset_visited: LocSet, l_this: Loc): LocSet = {
      if (!lset_visited.contains(l_this)) {
        // get attribute value
        val v_attr = DOMHelper.getAttribute(h, LocSet(l_this), ab_attr_name)
        // next elements
        val lset_children = Helper.Proto(h, l_this, ab_childNodes)._2.foldLeft(LocSetBot)((lset, l_n) =>
          lset ++ Helper.Proto(h, l_n, NumStr)._2)
        if ((!contain && v_attr._1._5 </ StrBot && v_attr._1._5 <= s) || (contain && BoolTrue <= v_attr._1._5.contains(s)))
          lset_children.foldLeft(LocSet(l_this))((lset, l_child) => lset ++ search(lset_visited + l_this, l_child))
        else
          lset_children.foldLeft(LocSetBot)((lset, l_child) => lset ++ search(lset_visited + l_this, l_child))
      }
      else
        LocSetBot
    }
    search(LocSetBot, l_root)
  }

  def findByProp(h: Heap, l_root: Loc, prop_name: String, s: AbsString, tag: Boolean): LocSet = {
    val ab_prop_name = AbsString.alpha(prop_name)
    val ab_childNodes = AbsString.alpha("childNodes")
    val ab_asterisk = AbsString.alpha("*")
    val ab_empty = AbsString.alpha("")
    def search(lset_visited: LocSet, l_this: Loc): LocSet = {
      if (!lset_visited.contains(l_this)) {
        // get property value
        val v_prop = Helper.Proto(h, l_this, ab_prop_name)
        // next elements
        val lset_children = Helper.Proto(h, l_this, ab_childNodes)._2.foldLeft(LocSetBot)((lset, l_n) =>
          lset ++ Helper.Proto(h, l_n, NumStr)._2)
        if ((v_prop._1._5 </ ab_empty && BoolTrue <= (v_prop._1._5 === s)) || (tag && ab_asterisk <= s))
          lset_children.foldLeft(LocSet(l_this))((lset, l_child) => lset ++ search(lset_visited + l_this, l_child))
        else
          lset_children.foldLeft(LocSetBot)((lset, l_child) => lset ++ search(lset_visited + l_this, l_child))
      }
      else
        LocSetBot
    }
    search(LocSetBot, l_root)
  }




  def findById(h: Heap, l_root: Loc, s_id : AbsString): LocSet = {
    findByProp(h, l_root, "id", s_id, false)
  }

  def findById(h: Heap, s_id : AbsString): LocSet = {
    findByProp(h, HTMLDocument.GlobalDocumentLoc, "id", s_id, false)
  }

  def findByClass(h: Heap, l_root: Loc, s_class : AbsString): LocSet = {
    findByAttr(h, l_root, "class", s_class, true)
  }

  def findByClass(h: Heap, s_class : AbsString): LocSet = {
    findByAttr(h, HTMLDocument.GlobalDocumentLoc, "class", s_class, true)
  }

  def findByTag(h: Heap, l_root: Loc, s_tag : AbsString): LocSet = {
    /* The HTML DOM returns the tagName of an HTML element in the canonical uppercase form */
    s_tag match {
      case NumStrSingle(s) =>
        findByProp(h, l_root, "tagName", AbsString.alpha(s.toUpperCase), true)
      case OtherStrSingle(s) =>
        findByProp(h, l_root, "tagName", AbsString.alpha(s.toUpperCase), true)
      case _ =>
        findByProp(h, l_root, "tagName", s_tag, true)
    }
  }

  def findByTag(h: Heap, s_tag : AbsString): LocSet = {
    findByTag(h, HTMLDocument.GlobalDocumentLoc, s_tag)
  }

  def findByName(h: Heap, l_root: Loc, s_name : AbsString): LocSet = {
    findByAttr(h, l_root, "name", s_name, false)
  }

  def findByName(h: Heap, s_name : AbsString): LocSet = {
    findByAttr(h, HTMLDocument.GlobalDocumentLoc, "name", s_name, false)
  }

  // to nextSibling
  def findByPropWidth(h: Heap, l_root: Loc, prop_name: String, s: AbsString, tag: Boolean): LocSet = {
    val ab_prop_name = AbsString.alpha(prop_name)
    val ab_nextSibling = AbsString.alpha("nextSibling")
    val ab_asterisk = AbsString.alpha("*")
    def search(lset_visited: LocSet, l_this: Loc): LocSet = {
      if (!lset_visited.contains(l_this)) {
        // get property value
        val v_attr = Helper.Proto(h, l_this, ab_prop_name)
        // next elements
        val lset_children = Helper.Proto(h, l_this, ab_nextSibling)._2
        if (BoolTrue <= (v_attr._1._5 === s) || (tag && ab_asterisk <= s))
          lset_children.foldLeft(LocSet(l_this))((lset, l_child) => lset ++ search(lset_visited + l_this, l_child))
        else
          lset_children.foldLeft(LocSetBot)((lset, l_child) => lset ++ search(lset_visited + l_this, l_child))
      }
      else
        LocSetBot
    }
    search(LocSetBot, l_root)
  }

  def findByTagWidth(h: Heap, l_root: Loc, s_tag : AbsString): LocSet = {
    findByPropWidth(h, l_root, "tagName", s_tag, true)
  }

  // get all parents
  def getParents(h: Heap, l_root: Loc): LocSet = {
    val ab_parentNode = AbsString.alpha("parentNode")
    def search(lset_visited: LocSet, l_this: Loc): LocSet = {
      if (!lset_visited.contains(l_this)) {
        // next elements
        val lset_parent = Helper.Proto(h, l_this, ab_parentNode)._2
        lset_parent.foldLeft(LocSet(l_this))((lset, l_parent) => lset ++ search(lset_visited + l_this, l_parent))
      }
      else
        LocSetBot
    }
    search(LocSetBot, l_root)
  }

  /*
  def getByAttrAll(h: Heap, l_root: Loc, attr: String, s : AbsString, tag: Boolean): LocSet = {
    var lset_visited = LocSetBot
    var lset_ret = LocSetBot
    def search(l: Loc): Unit = {
      if (!lset_visited.contains(l)) {
        lset_visited += l
        val v_attr = Helper.Proto(h, l, AbsString.alpha(attr))
        if (s <= v_attr._1._5 || (tag && AbsString.alpha("*") <= s))
          lset_ret += l
        val v_childNodes = Helper.Proto(h, l, AbsString.alpha("childNodes"))
        val lset_childs = v_childNodes._2.foldLeft(LocSetBot)((lset, l_n) => lset ++ Helper.Proto(h, l_n, NumStr)._2)
        lset_childs.foreach((_l) => search(_l))
      }
    }
    search(l_root)
    return lset_ret
  }

  def getByAttrFirst(h: Heap, l_root: Loc, attr: String, s : AbsString, tag: Boolean): LocSet = {
    var lset_visited = LocSetBot
    var lset_ret = LocSetBot
    def search(l: Loc): Unit = {
      if (!lset_visited.contains(l) && lset_ret.isEmpty) {
          lset_visited += l
        val v_attr = Helper.Proto(h, l, AbsString.alpha(attr))
        if (s <= v_attr._1._5 || (tag && AbsString.alpha("*") <= s))
          lset_ret += l
        val v_childNodes = Helper.Proto(h, l, AbsString.alpha("childNodes"))
        val lset_childs = v_childNodes._2.foldLeft(LocSetBot)((lset, l_n) => lset ++ Helper.Proto(h, l_n, NumStr)._2)
        lset_childs.foreach((_l) => search(_l))
      }
    }
    search(l_root)
    return lset_ret
  }
  */


  // E
  private val reg_tag= """^(\*|[\w\-]+)$""".r
  // E#id
  private val reg_id = """^#([\w\-_]+)$""".r
  // E.class
  private val reg_class = """^\.([\w\-_]+)$""".r
  // E[attr_name]
  private val reg_attr = """^\[([\w\-_]+)\]$""".r
  // filter
  private val reg_filter = """^(#[\w\-]+|\.[\w\-]+|\[[\w\-]+\])$""".r
  // tag or filter
  private val reg_tag_filter = """^(\*|[\w\-]+|#[\w\-]+|\.[\w\-]+|\[[\w\-]+\])$""".r
  // blank
  private val reg_blank = """^([\s]+)$""".r
  // combinator
  private val reg_combi = """"^(>|\+|~)$""".r
  // blank combinator
  private val reg_blank_combi = """"^([\s]+|>|\+|~)$""".r
  // tokens
  private val reg_token = """\*|[\w]+|>|\+|~|#[\w\-]+|\.[\w\-]+|\[[\w\-]+\]|[\s]+""".r
  // tokenizer for jQuery
  private val jq_token = """\*|[\w]+|>|\+|~|#[\w\-]+|\.[\w\-]+|\[[\w\-]+\]|[\s]+|:[\w\-]+""".r
  // tokenizer for options of jQuery
  private val jq_options = """^:([\w\-_]+)$""".r
  // jQuery filter
  private val jq_filter = """^(#[\w\-]+|\.[\w\-]+|:[\w\-]+|\[[\w\-]+\])$""".r


  /* simple, partial implementation of querySelectorAll */
  def querySelectorAll(h: Heap, l_root: Loc, s_selector: String): LocSet = {
//    val tokens = reg_token.findAllIn(s_selector.trim).toList
    val tokens = jq_token.findAllIn(s_selector.trim).toList

    def filterByTag(lset: LocSet, name: String):LocSet = {
      if (name == "*")
        lset
      else
        lset.filter((l) =>
          AbsString.alpha(name.toUpperCase) <= Helper.Proto(h, l, AbsString.alpha("tagName"))._1._5)
    }
    def filterById(lset: LocSet, name: String): LocSet = {
      lset.filter((l) =>
        AbsString.alpha(name) <= Helper.Proto(h, l, AbsString.alpha("id"))._1._5)
    }
    def filterByClass(lset: LocSet, name: String): LocSet = {
      lset.filter((l) =>
        BoolTrue <= getAttribute(h, l, AbsString.alpha("class"))._1._5.contains(AbsString.alpha(name)))
    }
    def filterByAttr(lset: LocSet, name: String): LocSet = {
      lset.filter((l) =>
        BoolTrue <= Helper.HasOwnProperty(h, l, AbsString.alpha(name)))
    }
    def filter(lset: LocSet, filter: String): LocSet = {
      filter.take(1) match {
        case "#" => filterById(lset, filter.drop(1))
        case "." => filterByClass(lset, filter.drop(1))
        case "[" => filterByAttr(lset, filter.drop(1).dropRight(1))
        case ":" => lset
        case _ =>  filterByTag(lset, filter)
      }
    }

    def combinator(lset: LocSet, combinator: String): LocSet = {
      combinator match {
        case reg_blank(_) => // all descendant
          lset.foldLeft(LocSetBot)((ls, l) => ls ++ findByTag(h, l, AbsString.alpha("*")))
        case ">" => // children
          lset.foldLeft(LocSetBot)((ls, l) =>
            ls ++ Helper.Proto(h, l, AbsString.alpha("childNodes"))._2.foldLeft(LocSetBot)((ls2, l2) =>
              ls2 ++ Helper.Proto(h, l2, NumStr)._2))
        case "+" => // nextSibling
          lset.foldLeft(LocSetBot)((ls, l) => ls ++ Helper.Proto(h, l, AbsString.alpha("nextSibling"))._2)
        case "~" => // all following siblings
          lset.foldLeft(LocSetBot)((ls, l) => ls ++ findByTagWidth(h, l, AbsString.alpha("*")))
      }
    }
    def iter(toks: List[String], lset_find:LocSet, prev: Option[String]): LocSet = {
      toks match {
        case List() =>
          prev match {
            case Some(tok) =>
              tok match {
                case reg_combi(_) => LocSetBot // syntax error
                case _ => lset_find
              }
            case None =>
              LocSetBot // nothing
          }
        case token::tail => //process(lset_find, prev, token, tail)
          if (prev.isEmpty) {
            token match {
              case ">"|"+"|"~" => LocSetBot // syntax error
              // alphabet
              case reg_tag(name) =>
                val lset_next = findByTag(h, HTMLDocument.GlobalDocumentLoc, AbsString.alpha(name))
                iter(tail, lset_next, Some(token))
              // start with '.'
              case reg_class(name) =>
                val lset_next = findByClass(h, HTMLDocument.GlobalDocumentLoc, AbsString.alpha(name))
                iter(tail, lset_next, Some(token))
              // start with '#'
              case reg_id(name) =>
                val lset_next = findById(h, HTMLDocument.GlobalDocumentLoc, AbsString.alpha(name))
                iter(tail, lset_next, Some(token))
              // start with ':'
              case jq_options(_) =>
                val lset_all = findByTag(h, HTMLDocument.GlobalDocumentLoc, AbsString.alpha("*"))
                iter(tail, lset_all, Some(token))
              // others
              case reg_filter(f) =>
                val lset_all = findByTag(h, HTMLDocument.GlobalDocumentLoc, AbsString.alpha("*"))
                val lset_next = filter(lset_all, f)
                iter(tail, lset_next, Some(token))
            }
          }
          else {
            val prev_tok = prev.get
            if (reg_blank.findFirstIn(prev_tok).nonEmpty || reg_combi.findFirstIn(prev_tok).nonEmpty){// prev is blank or combi
              if (reg_combi.findFirstIn(token).nonEmpty) {
                if (reg_blank.findFirstIn(prev_tok).nonEmpty)  // prev=blank, current=combi
                  iter(tail, lset_find, Some(token))
                else // prev=combi current=combi, syntaxError
                  LocSetBot
              }
              else if (reg_blank.findFirstIn(token).nonEmpty) // current= blank
                iter(tail, lset_find, Some(prev_tok))
              else {// current= filter or tag
                val lset_combi = combinator(lset_find, prev_tok)
                val lset_next = filter(lset_combi, token)
                iter(tail, lset_next, Some(token))
              }
            }
            else { /* prev is tag or filter*/
              if (reg_tag.findFirstIn(token).nonEmpty)
                LocSetBot  // should not be happen
              else if (reg_filter.findFirstIn(token).nonEmpty)
                iter(tail, filter(lset_find, token), Some(token))
              else // blank or combi
                iter(tail, lset_find, Some(token))

            }

          }
      }
      /*
   (prev_tok, token) match {
     /* prev is blank or combi */
     case (reg_blank_combi(_), reg_blank(_)) => // ignore blank
       iter(tail, lset_find, Some(prev_tok))
     case (reg_blank(_), reg_combi(_)) => // ignore blank
       iter(tail, lset_find, Some(token))
     case (reg_combi(_), reg_combi(_)) => // syntax error
       LocSetBot
     case (reg_blank_combi(c), reg_tag_filter(f)) => // combinator
       val lset_combi = combinator(lset_find, c)
       val lset_next = filterByTag(lset_combi, f)
       iter(tail, lset_next, Some(token))
     /* prev is tag or filter*/
     case (reg_tag_filter(_), reg_tag(_)) => // should not be happen
       LocSetBot
     case (reg_tag_filter(_), reg_filter(f)) => // [attr]
       iter(tail, filter(lset_find, f), Some(token))
     case (reg_tag_filter(_), reg_blank_combi(_)) => // go next
       iter(tail, lset_find, Some(token))

   }  */
  }
  val lset_ret = iter(tokens, LocSetBot, None)
  //println("return: " + lset_ret)
  lset_ret
}


  def querySelectorAll(h: Heap, s_selector: String): LocSet = {
    querySelectorAll(h, HTMLDocument.GlobalDocumentLoc, s_selector)
  }

  def querySelectorAll(h: Heap, l_root: Loc, s_selector: AbsString): LocSet = {
    AbsString.concretize(s_selector) match {
      case Some(s) =>
        querySelectorAll(h, l_root, s)
      case None =>
        if (s_selector </ StrBot)
          HTMLTopElement.getInsLoc(h)
        else
          LocSetBot

    }
  }

  def querySelectorAll(h: Heap, s_selector: AbsString): LocSet = {
    querySelectorAll(h, HTMLDocument.GlobalDocumentLoc, s_selector)
  }

  def addTag(h: Heap, tag_name : String, l_tag: Loc, l_child: Loc): Heap = {
    val s_uppper = tag_name.toUpperCase
    val element_proplist = DOMElement.getInsList(PropValue(ObjectValue(AbsString.alpha(s_uppper), F, T, T))):::DOMHelper.getInsList(s_uppper)
    val o_tag = element_proplist.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
      .update("childNodes", PropValue(ObjectValue(l_child, F, T, T)))
    val o_child = DOMNodeList.getInsList(0).foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
    h.update(l_tag, o_tag).update(l_child, o_child)
  }
  def addTagTop(h: Heap, l_tag: Loc, l_child: Loc): Heap = {
    val o_tag = HTMLTopElement.default_getInsList().foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2, AbsentTop)).
      update("childNodes", PropValue(ObjectValue(l_child, F, T, T)))
    val o_child =  DOMNodeList.getInsList(0).foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
    h.update(l_tag, o_tag).update(l_child, o_child)
  }

  // check the HIERARCHY_REQUEST_ERR exception in the DOM tree 
  def checkHierarchyException(funname: String, h: Heap, lset_target: LocSet, lset_child: LocSet): Unit = {
    println(funname + " - CHECK: HIERARCHY_REQUEST_ERR exception")
    /*
    checkHRE(h, lset_target, lset_child) match {
      case BoolTop =>
        println(funname + " - WARNING: HIERARCHY_REQUEST_ERR exception: you are trying to attach a parent of a target node as a child")
      case BoolTrue =>
        println(funname + " - ERROR: HIERARCHY_REQUEST_ERR exception: you are trying to attach a parent of a target node as a child")
      case _ => ()
    }
    */
  }


  private def checkNFE(h: Heap, lset_target: LocSet, lset_child: LocSet): AbsBool = {
   
    if(!lset_target.isEmpty) {     
      lset_target.foldLeft[AbsBool](BoolBot)((b, l_node) => {
        // childNodes
        val lset_childNodes = Helper.Proto(h, l_node, AbsString.alpha("childNodes"))._2
        lset_childNodes.foldLeft(b)((bb, l_child) => {
          val n_len = Operator.ToUInt32(Helper.Proto(h, l_child, AbsString.alpha("length")))
          n_len match {
            case UIntSingle(n) =>
              val n_index = (0 until n.toInt).indexWhere((i) => {                
                BoolTrue <= Operator.bopSEq(Helper.Proto(h, l_child, AbsString.alpha(i.toString)), Value(lset_child))._1._3
                }
              )
              // exception
              if(n_index < 0)
                b + BoolTrue
              // no exception
              else
                b + BoolFalse
            // exception may occur
            case NumTop | UInt => BoolTop
            // cannot fall into this case
            case _ =>
              println("INTERNAL ERROR : childnodes.length is not either of UIntSingle, NumTop, and UInt") 
              bb
          }
        })
      })
    }
    else BoolTrue
 
  }

  // check the NOT_FOUND_ERR exception in the DOM tree 
  def checkNotFoundException(funname: String, h: Heap, lset_target: LocSet, lset_child: LocSet): Unit = {
    println(funname + " - CHECK: NOT_FOUND_ERR exception")
    checkNFE(h, lset_target, lset_child) match {
      case BoolTop =>
        println(funname + " - WARNING: NOT_FOUND_ERR exception: the argument node is not a child of a target node")
      case BoolTrue =>
        println(funname + " - ERROR: NOT_FOUND_ERR exception: the argument node is not a child of a target node")
      case _ => ()
    }
  }



  def appendChild(h: Heap, lset_target: LocSet, lset_child: LocSet): Heap = {
    if (!lset_target.isEmpty && !lset_child.isEmpty) {
      val (h_1, lastC_lset) = lset_target.foldLeft((h, LocSetBot))((d, l_node) => {
        /* lastChild */
        val l_childlset = Helper.Proto(d._1, l_node, AbsString.alpha("lastChild"))._2
        /* current childNodes */
        val lset_ns = Helper.Proto(d._1, l_node, AbsString.alpha("childNodes"))._2
        val (h_append, f_childlset) = lset_ns.foldLeft((d._1, LocSetBot))((dd, l_ns) => {
          /* length of current childNodes */
          val n_len = Operator.ToUInt32(Helper.Proto(dd._1, l_ns, AbsString.alpha("length")))
          val hhh_2 = n_len match {
            case UIntSingle(n) =>
              /* childNodes[length] := new_child */
              val hhh_1 = Helper.PropStore(dd._1, l_ns, AbsString.alpha(n.toInt.toString), Value(lset_child))
              /* childNodes["length"] := length + 1 */
              Helper.PropStore(hhh_1, l_ns, AbsString.alpha("length"), Value(AbsNumber.alpha(n+1)))
            case NumTop | UInt =>
              Helper.PropStore(dd._1, l_ns, NumStr, Value(lset_child))
            case _ => dd._1 /* exception ?? */
          }
          val firstChild_lset =  Helper.Proto(hhh_2, l_ns, AbsString.alpha(0.toInt.toString))._2
          (hhh_2, dd._2 ++ firstChild_lset)
        })
        // 'firstChild' update
        val h_append_1 = Helper.PropStore(h_append, l_node, AbsString.alpha("firstChild"),Value(f_childlset))
        // 'lastChild' update
        val h_append_2 = Helper.PropStore(h_append_1, l_node, AbsString.alpha("lastChild"), Value(lset_child))
        (h_append_2, d._2 ++ l_childlset)
      })
      // 'nextSibling' update'
      val h_2 = lastC_lset.foldLeft(h_1)((_h, l) => Helper.PropStore(_h, l, AbsString.alpha("nextSibling"),Value(lset_child)))
      // 'parentNode', 'previousSibling' update of the appended child
      lset_child.foldLeft(h_2)((_h, l) => {
        val _h_1 = Helper.PropStore(_h, l, AbsString.alpha("parentNode"),Value(lset_target))
        if(!lastC_lset.isEmpty)
          Helper.PropStore(_h_1, l, AbsString.alpha("previousSibling"), Value(lastC_lset))
        else
          Helper.PropStore(_h_1, l, AbsString.alpha("previousSibling"), Value(NullTop))

      })
    }
    else
      HeapBot
  }

  def prependChild(h: Heap, lset_target: LocSet, lset_child: LocSet): Heap = {
    if (!lset_target.isEmpty && !lset_child.isEmpty) {
      val h_1 = lset_target.foldLeft(h)((h1, l_node) => {
        /* current childNodes */
        val lset_ns = Helper.Proto(h1, l_node, AbsString.alpha("childNodes"))._2
        val h_append = lset_ns.foldLeft(h1)((h2, l_ns) => {
          /* length of current childNodes */
          val n_len = Operator.ToUInt32(Helper.Proto(h2, l_ns, AbsString.alpha("length")))
          n_len match {
            case UIntSingle(n) =>
              /* move 0->1, 1->2, ... n_len-1 -> n_len */
              val h5 = (0 until n.toInt).foldLeft(h2)((h3, i) => {
                val v = Helper.Proto(h3, l_ns, AbsString.alpha((n-i-1).toInt.toString))
                Helper.PropStore(h3, l_ns, AbsString.alpha((n-i).toInt.toString), v)
              })
              /* childNodes[0] := new_child */
              val h6 = Helper.PropStore(h5, l_ns, AbsString.alpha("0"), Value(lset_child))
              /* childNodes["length"] := length + 1 */
              Helper.PropStore(h6, l_ns, AbsString.alpha("length"), Value(AbsNumber.alpha(n+1)))
            case NumTop | UInt =>
              Helper.PropStore(h2, l_ns, NumStr, Value(lset_child))
            case _ => h2 /* exception ?? */
          }
        })
        val o_target = h_append(l_node).update("firstChild", PropValue(ObjectValue(Value(lset_child),F,T,F)))
        h_append.update(l_node, o_target)
      })
      h_1
    }
    else
      HeapBot
  }

  def NewChildNodeListObj(length: Double): Obj = {
    val childNodes_list = DOMNodeList.getInsList(0)
    childNodes_list.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
  }

  def removeChild(h: Heap, lset_parent: LocSet, lset_child: LocSet): Heap = {
    /* arguments */
    if (!lset_child.isEmpty) {
      /* location for clone node */
      val h_1 = lset_parent.foldLeft(h)((h1, l_node) => {
        val lset_ns = Helper.Proto(h, l_node, AbsString.alpha("childNodes"))._2
        val (h1_2, firstChild_lset, lastChild_lset) = lset_ns.foldLeft((h1, LocSetBot, LocSetBot))((hl, l_ns) => {
          val n_len = Operator.ToUInt32(Helper.Proto(h, l_ns, AbsString.alpha("length")))
          val (h_2, lchild_lset) = n_len match {
            case UIntSingle(n) =>
              val n_index = (0 until n.toInt).indexWhere((i) => {
                BoolTrue <= Operator.bopSEq(Helper.Proto(hl._1, l_ns, AbsString.alpha(i.toString)), Value(lset_child))._1._3
              })
              if (n_index < 0)
                (hl._1, LocSetBot)
              else {
                val hhh_1 = Helper.Delete(hl._1, l_ns, AbsString.alpha(n_index.toString))._1
                val hhh_2 = (n_index+1 until n.toInt).foldLeft(hhh_1)((_h, i) => {
                  val v_next = Helper.Proto(_h, l_ns,  AbsString.alpha(i.toString))
                  val _h1 = Helper.Delete(_h, l_ns, AbsString.alpha(i.toString))._1
                  Helper.PropStore(_h1, l_ns, AbsString.alpha((i-1).toString), v_next)
                })
                // decrease the length of childNodes by 1
                val hhh_3 = Helper.PropStore(hhh_2, l_ns, AbsString.alpha("length"), Value(AbsNumber.alpha(n - 1)))
                val lastC_lset = if(n-1==0) Helper.Proto(hhh_3, l_ns, AbsString.alpha("0"))._2
                                 else Helper.Proto(hhh_3, l_ns, AbsString.alpha((n-2).toString))._2
                (hhh_3, lastC_lset)
              }

            case NumTop | UInt =>
              val b_eq = Operator.bopSEq(Helper.Proto(hl._1, l_ns, NumStr), Value(lset_child))._1._3
              val h2_1 =
                if (BoolTrue <= b_eq) Helper.Delete(hl._1, l_ns, NumStr)._1
                else HeapBot
              val h2_2 =
                if (BoolFalse <= b_eq) hl._1
                else HeapBot
              val h2_3 = h2_1 + h2_2
              (h2_3, Helper.Proto(h2_3, l_ns, NumStr)._2)
            case _ => (hl._1, LocSetBot) /* exception ?? */
          }
          val fchild_lset = Helper.Proto(h_2, l_ns,  AbsString.alpha("0"))._2
          (h_2, hl._2 ++ fchild_lset, hl._3 ++ lchild_lset)

        })
        /* 'firstChild' and 'lastChild' update of the parentNode */
        val firstChildVal = if(firstChild_lset.isEmpty) Value(NullTop) else Value(firstChild_lset)
        val lastChildVal = if(lastChild_lset.isEmpty) Value(NullTop) else Value(lastChild_lset)
        val h1_3 = Helper.PropStore(h1_2, l_node, AbsString.alpha("firstChild"), firstChildVal)
        Helper.PropStore(h1_3, l_node, AbsString.alpha("lastChild"), lastChildVal)
      })
      /* 'parentNode', 'previousSibling', 'nextSibling' update of the removed child */
      val (h_2, preSib, nextSib) = lset_child.foldLeft((h_1, ValueBot, ValueBot))((d, l) => {
        val preS = Helper.Proto(d._1, l, AbsString.alpha("previousSibling"))
        val nextS = Helper.Proto(d._1, l, AbsString.alpha("nextSibling"))
        val h_2_1 = Helper.PropStore(d._1, l, AbsString.alpha("parentNode"), Value(NullTop))
        val h_2_2 = Helper.PropStore(h_2_1, l, AbsString.alpha("previousSibling"), Value(NullTop))
        val h_3_3 = Helper.PropStore(h_2_2, l, AbsString.alpha("nextSibling"), Value(NullTop))
        (h_3_3, preS + d._2, nextS + d._3)
      })

      /* 'nextSibling' update of the previous sibling of the removed child */
      val h_3 = preSib._2.foldLeft(h_2)((_h, l) =>
        Helper.PropStore(_h, l, AbsString.alpha("nextSibling"), nextSib)
      )
      
      /* 'previousSibling' update of the next sibling of the removed child */
      val h_4 = nextSib._2.foldLeft(h_3)((_h, l) =>
        Helper.PropStore(_h, l, AbsString.alpha("previousSibling"), preSib)
      )

      h_4
    }
    else
      HeapBot
  }

  def getAttribute(h: Heap, l_elem: Loc, s_attr: AbsString): Value = {
    // read the list of attributes in the current node
    val lset_attrs = Helper.Proto(h, l_elem, AbsString.alpha("attributes"))._2
    lset_attrs.foldLeft(ValueBot)((v, l_attrs) => {
      val v_attr_2 = Helper.HasOwnProperty(h, l_attrs, s_attr) match {
        case BoolBot => ValueBot
        // in case that the current node does not have an attribute with the given name
        case BoolFalse => Value(NullTop)
        // in case that the current node may have an attribute with the given name
        case _ =>
          val attr_lset = Helper.Proto(h, l_attrs, s_attr)._2 // get attribute
          attr_lset.foldLeft(ValueBot)((v, l_attr) => {
            v + Helper.Proto(h, l_attr, AbsString.alpha("value")) // get value
          })
      }
      v + v_attr_2
    })
  }

  def getAttribute(h: Heap, lset_elem: LocSet, s_attr: AbsString): Value = {
    lset_elem.foldLeft(ValueBot)((v, l_elem) => {
        // read the list of attributes in the current node
        val lset_attrs = Helper.Proto(h, l_elem, AbsString.alpha("attributes"))._2
        val v_attr_1 = lset_attrs.foldLeft(ValueBot)((v, l_attrs) => {
          val v_attr_2 = Helper.HasOwnProperty(h, l_attrs, s_attr) match {
            case BoolBot => ValueBot
            // in case that the current node does not have an attribute with the given name
            case BoolFalse => Value(NullTop)
            // in case that the current node may have an attribute with the given name
            case _ =>
              val attr_lset = Helper.Proto(h, l_attrs, s_attr)._2 // get attribute
              attr_lset.foldLeft(ValueBot)((v, l_attr) => {
                v + Helper.Proto(h, l_attr, AbsString.alpha("value")) // get value
              })
          }
          v + v_attr_2
        })
        v + v_attr_1
      })
  }

  def removeAttribute(h: Heap, lset_elem: LocSet, s_attr: AbsString): Heap = {
    lset_elem.foldLeft(h)((_h, l_elem) => {
      // read the list of attributes in the current node
      val lset_attrs = Helper.Proto(_h, l_elem, AbsString.alpha("attributes"))._2
      lset_attrs.foldLeft(_h)((__h, l_attr) => {
        var h_1 = Helper.Delete(__h, l_attr, s_attr)._1
        val attributes_obj = h_1(l_attr)
        val length_pval = attributes_obj("length")._1._1._1._1
        // decrease 'length' of 'attributes' by 1
        val length_val = Helper.toNumber(length_pval) match {
          case UIntSingle(v) => UIntSingle(v-1)
          case _ => Helper.toNumber(length_pval)
        }
        val attributes_obj_new = attributes_obj.update(AbsString.alpha("length"), PropValue(ObjectValue(length_val, T, T, T)))
        h_1.update(l_attr, attributes_obj_new)
      })
    })
  }

  def setAttribute(h: Heap, l_elem: Loc ,l_attr: Loc, l_text: Loc, l_child1: Loc, l_child2: Loc, attr_name:AbsString, attr_val: AbsString): Heap = {
    /* imprecise semantics : no exception handling */
    if(attr_name </ StrBot || attr_val </StrBot) {
      val name = PropValue(ObjectValue(attr_name, F, T, T))
      val value = PropValue(ObjectValue(attr_val, T, T, T))
      // create a new Attr node object
      val attr_obj_list = DOMAttr.default_getInsList(name, value, PropValue(ObjectValue(l_child1, F, T, T)), PropValue(ObjectValue(l_text, F, T, T)))
      val attr_obj = attr_obj_list.foldLeft(ObjEmpty) ((obj, v) => obj.update(AbsString.alpha(v._1), v._2))
      // create a new text node object
      val text_obj_list = DOMText.default_getInsList(value, PropValue(ObjectValue(l_attr, F, T, T)), PropValue(ObjectValue(l_child2, F, T, T)))
      val text_obj = text_obj_list.foldLeft(ObjEmpty) ((obj, v) => obj.update(AbsString.alpha(v._1), v._2))

      // objects for 'childNodes' of the Attr node
      val child_obj_list1 = DOMNamedNodeMap.getInsList(1)
      val child_obj1 = child_obj_list1.foldLeft(ObjEmpty.update(AbsString.alpha("0"), PropValue(ObjectValue(l_text, T, T, T))))((obj, v) =>
        obj.update(AbsString.alpha(v._1), v._2))
      // objects for 'childNodes' of the Text node
      val child_obj_list2 = DOMNamedNodeMap.getInsList(0)
      val child_obj2 = child_obj_list2.foldLeft(ObjEmpty)((obj, v) => obj.update(AbsString.alpha(v._1), v._2))

      // update 'className' property if the value of the 'class' attribute would be changed
      val thisobj = h(l_elem)
      val className = Helper.Proto(h, l_elem, AbsString.alpha("className"))
      val h_in1 = attr_name match {
        case StrTop =>
          // join the old value and new value
          val thisobj_new = thisobj.update(AbsString.alpha("className"), value + PropValue(className))
          h.update(l_elem, thisobj_new)
        case OtherStr =>
          // join the old value and new value
          val thisobj_new = thisobj.update(AbsString.alpha("className"), value + PropValue(className))
          h.update(l_elem, thisobj_new)
        case OtherStrSingle(v) if v=="class" =>
          // update 'className' property with a new value
          val thisobj_new = thisobj.update(AbsString.alpha("className"), value)
          h.update(l_elem, thisobj_new)
        case _ => h
      }

      // read the list of attributes in the current node
      val attributes_lset = Helper.Proto(h_in1, l_elem, AbsString.alpha("attributes"))._2
      val h_ret = attributes_lset.foldLeft(h_in1)((h_in2, l_attributes) => {
        val attributes_obj = h_in2(l_attributes)
        val length_pval = attributes_obj("length")._1._1._1._1
        // increate 'length' of 'attributes' by 1
        val length_val = Helper.toNumber(length_pval) match {
          case UIntSingle(v) => UIntSingle(v+1)
          case _ => Helper.toNumber(length_pval)
        }
        val attributes_obj_new =
          attributes_obj.update(attr_name, PropValue(ObjectValue(l_attr, T, T, T))).
            update(Helper.toString(length_pval), PropValue(ObjectValue(l_attr, T, T, T))).
            update(AbsString.alpha("length"), PropValue(ObjectValue(length_val, T, T, T)))
        // update heap
        h_in2.update(l_attr, attr_obj).update(l_text, text_obj).update(l_attributes, attributes_obj_new).update(l_child1, child_obj1).update(l_child2, child_obj2)
      })
      h_ret
    }
    else
      HeapBot
  }

  def setAttribute(h: Heap, lset_elem: LocSet ,l_attr: Loc, l_text: Loc, l_child1: Loc, l_child2: Loc, l_classentry: Loc, attr_name:AbsString, attr_val: AbsString): Heap = {
    /* imprecise semantics : no exception handling */
    if(attr_name </ StrBot || attr_val </StrBot) {
      val name = PropValue(ObjectValue(attr_name, F, T, T))
      val value = PropValue(ObjectValue(attr_val, T, T, T))
      // create a new Attr node object
      val attr_obj_list = DOMAttr.default_getInsList(name, value, PropValue(ObjectValue(l_child1, F, T, T)), PropValue(ObjectValue(l_text, F, T, T)))
      val attr_obj = attr_obj_list.foldLeft(ObjEmpty) ((obj, v) => obj.update(AbsString.alpha(v._1), v._2))
      // create a new text node object
      val text_obj_list = DOMText.default_getInsList(value, PropValue(ObjectValue(l_attr, F, T, T)), PropValue(ObjectValue(l_child2, F, T, T)))
      val text_obj = text_obj_list.foldLeft(ObjEmpty) ((obj, v) => obj.update(AbsString.alpha(v._1), v._2))

      // objects for 'childNodes' of the Attr node
      val child_obj_list1 = DOMNamedNodeMap.getInsList(1)
      val child_obj1 = child_obj_list1.foldLeft(ObjEmpty.update(AbsString.alpha("0"), PropValue(ObjectValue(l_text, T, T, T))))((attr_nameobj, v) => attr_nameobj.update(AbsString.alpha(v._1), v._2))
      // objects for 'childNodes' of the Text node
      val child_obj_list2 = DOMNamedNodeMap.getInsList(0)
      val child_obj2 = child_obj_list2.foldLeft(ObjEmpty)((obj, v) => obj.update(AbsString.alpha(v._1), v._2))

      val h_5 = lset_elem.foldLeft(h)((h_in, l_this) => {
        // update 'className' property if the value of the 'class' attribute would be changed
        val thisobj = h_in(l_this)
        val className = Helper.Proto(h_in, l_this, AbsString.alpha("className"))
        val h_in1 = attr_name match {
          case StrTop =>
            // join the old value and new value
            val thisobj_new = thisobj.update(AbsString.alpha("className"), value + PropValue(className))
            h_in.update(l_this, thisobj_new)
          case OtherStr =>
            // join the old value and new value
            val thisobj_new = thisobj.update(AbsString.alpha("className"), value + PropValue(className))
            h_in.update(l_this, thisobj_new)
          case OtherStrSingle(v) if v=="class" =>
            // update 'className' property with a new value
            val thisobj_new = thisobj.update(AbsString.alpha("className"), value)
            h_in.update(l_this, thisobj_new)
          case _ => h_in
        }

        // read the list of attributes in the current node
        val attributes_lset = Helper.Proto(h_in1, l_this, AbsString.alpha("attributes"))._2
        attributes_lset.foldLeft(h_in1)((h_in2, l_attributes) => {
          val attributes_obj = h_in2(l_attributes)
          val length_pval = attributes_obj("length")._1._1._1._1
          // increate 'length' of 'attributes' by 1
          val length_val = Helper.toNumber(length_pval) match {
            case UIntSingle(v) => UIntSingle(v+1)
            case _ => Helper.toNumber(length_pval)
          }
          val attributes_obj_new =
            attributes_obj.update(attr_name, PropValue(ObjectValue(l_attr, T, T, T))).
              update(Helper.toString(length_pval), PropValue(ObjectValue(l_attr, T, T, T))).
              update(AbsString.alpha("length"), PropValue(ObjectValue(length_val, T, T, T)))
          // update heap
          h_in2.update(l_attr, attr_obj).update(l_text, text_obj).update(l_attributes, attributes_obj_new).update(l_child1, child_obj1).update(l_child2, child_obj2)
        })
      })
      // ClassTable update
      classTableUpdate(h_5, attr_name, lset_elem, l_classentry)
    }
    else
      HeapBot
  }

  def getNextElementSibling(h: Heap, l: Loc): LocSet = {
    var visited = LocSetBot
    def iter(l_current: Loc): LocSet = {
      if (visited.contains(l_current))
        LocSetBot
      else {
        visited += l_current
        val lset_sibling = Helper.Proto(h, l_current, AbsString.alpha("nextSibling"))._2
        lset_sibling.foldLeft(LocSetBot)((ls, l_s) => {
          val n_type = Helper.Proto(h, l_s, AbsString.alpha("nodeType"))._1._4
          val lset1 =
            if (AbsNumber.alpha(DOMNode.ELEMENT_NODE) <= n_type)
              ls + l_s
            else
              LocSetBot
          val lset2 =
            if (AbsNumber.alpha(DOMNode.ELEMENT_NODE) </ n_type)
              iter(l_s)
            else
              LocSetBot
          lset1 ++ lset2
        })
      }
    }
    iter(l)
  }

  // update classTable used by getElementsByClassName
  def classTableUpdate(h: Heap, className: AbsString, target: LocSet, newNode_loc: Loc): Heap = {
    /* class look-up table update */
    val class_table = h(ClassTableLoc)
    val new_heap =
      if(className </ StrBot){
       val mapped_node = class_table(className)
       // in case that the mapping does not exist
       if(mapped_node._2 </ AbsentBot) {
         val nodelist_proplist = DOMNodeList.getInsList(1) ++ List(
           ("0", PropValue(ObjectValue(Value(target), T, T, T))))
         val nodelist_obj = nodelist_proplist.foldLeft(ObjEmpty)((o, p) => o.update(p._1, p._2))
         val new_class_table = class_table.update(className, PropValue(ObjectValue(newNode_loc, T, T, T)))
         h.update(newNode_loc, nodelist_obj).update(ClassTableLoc, new_class_table)
       }
       // in case that the mapping already exists
       else {
         // we know only a single location can be mapped for a class name in the inital heap  
         val loc_nodelist = mapped_node._1._1._1._2
         val new_h = loc_nodelist.foldLeft(h)((_h, l) => {
           val obj_nodelist = h(l)
           val new_nodelist = AbsNumber.concretize(obj_nodelist("length")._1._1._1._1._4) match {
             case Some(n) => 
               val len = n.toInt
               obj_nodelist.update(
                 AbsString.alpha(len.toString), PropValue(ObjectValue(Value(target), T, T, T))).update(
                 AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(len + 1), T, T, T)))
             case None => obj_nodelist.update(NumStr, PropValue(ObjectValue(Value(target), T, T, T)))
           }
           _h.update(l, new_nodelist)
         })
         new_h
       }
     }
     else h

     new_heap
  }
  
  /* Parse the HTML source string, s, and return a corresponding document fragment */
  def stringToDocumentFragment(s: String): DocumentFragment = {
    val parser = new DOMFragmentParser()
    val document = new HTMLDocumentImpl()
    val fragment = document.createDocumentFragment()
    parser.parse(new InputSource(new StringReader(s)), fragment)
    fragment
  }

  // Print a concrete DOM tree
  def printDom(node: Node, indent: String): Unit = {
    System.out.println(indent + node.getNodeName + node.getNodeType + node.getClass().getName())
    var child : Node = node.getFirstChild
    while (child != null) {
      printDom(child, indent+" ")
      child = child.getNextSibling()
    }
  }

  def clearAllChildNodes(h: Heap, l_root: Loc): Heap = {
    val h_1 = if(BoolTrue <= Helper.HasOwnProperty(h, l_root, AbsString.alpha("firstChild"))) 
                 Helper.PropStore(h, l_root, AbsString.alpha("firstChild"), Value(NullTop))
              else h
    val h_2 = if(BoolTrue <= Helper.HasOwnProperty(h_1, l_root, AbsString.alpha("lastChild"))) 
                 Helper.PropStore(h_1, l_root, AbsString.alpha("lastChild"), Value(NullTop))
              else h_1
    val nodelist_proplist = DOMNodeList.getInsList(0)
    val h_3 = if(BoolTrue <= Helper.HasOwnProperty(h_2, l_root, AbsString.alpha("childNodes"))){ 
                val childNodes_lset = Helper.Proto(h_2, l_root, AbsString.alpha("childNodes"))._2
                childNodes_lset.foldLeft(h_2)((_h, l) => {
                  addInstance(_h, l, nodelist_proplist)
                })
              }
              else h_2
    h_3
  }

  def updateDOMTree(h: Heap, ctx: Context, l_root: Loc, source: Value, cfg: CFG, addresskey: Int): (Heap, Context) = {
    val s_str = Helper.toString(Helper.toPrimitive(source))
    if(s_str </ StrBot) {
      AbsString.concretize(s_str) match {
        case None =>  // StrTop | NumStr | OtherStr
          val (h_1, ctx_1, l_topnode) = cfgAddrToLoc(h, ctx, cfg.getStoreAddress(addresskey))
          val (h_2, ctx_2, l_childNodes) = cfgAddrToLoc(h_1, ctx_1, cfg.getStoreAddress(addresskey))
          val h2 = HTMLTopElement.setInsLoc(h_2, l_topnode)
          // property list for HTMLTopElement
          val topelement_proplist = HTMLTopElement.getInsList(l_topnode, l_childNodes)
          val h_3 = addInstance(h2, l_topnode, topelement_proplist)
          // property list for 'childNodes'
          val childNodes_proplist = DOMNodeList.getInsListTop(l_topnode)
          val h_4 = addInstance(h_3, l_childNodes, childNodes_proplist)
          // clear all child nodes of the updated root node
          val h_5 = clearAllChildNodes(h_4, l_root)
          /* weak update of 'firstChild', 'lastChild', and 'childNodes' of the updated root node */
          val h_6 = Helper.PropStoreWeak(h_5, l_root, AbsString.alpha("firstChild"), Value(l_topnode))
          val h_7 = Helper.PropStoreWeak(h_6, l_root, AbsString.alpha("lastChild"), Value(l_topnode))
          val childNodes_lset = Helper.Proto(h_7, l_root, AbsString.alpha("childNodes"))._2
          val h_8 = childNodes_lset.foldLeft(h_7)((_h, l) => {
            Helper.PropStoreWeak(_h, l, AbsString.alpha("length"), Value(UInt))
            Helper.PropStoreWeak(_h, l, AbsString.alpha("@default_number"), Value(l_topnode))
          })
          /* weak update of 'parentNode' of the new HTMLTopElement */
          val h_9 = Helper.PropStoreWeak(h_8, l_topnode, AbsString.alpha("parentNode"), Value(l_root))
          System.err.println("* Warning: The value to be assigned to 'innerHTML' is not concrete; analysis results may be unsound.") 
          (h_9, ctx_2)
        case Some(t) => 
          val _h = clearAllChildNodes(h, l_root)
          // parse HTML fragment and produces a concrete DOM tree
          val fragment = stringToDocumentFragment(t)
          val ((h_1, ctx_1), l_newroot) = buildDOMTree(_h, ctx, fragment, cfg, None, addresskey, false)
          // location set of 'firstChild' of the new DOM tree root
          var firstChild_lset = Helper.Proto(h_1, l_newroot, AbsString.alpha("firstChild"))._2
          var h_3 = h_1
          while(!firstChild_lset.isEmpty) {
            val h_2 = removeChild(h_3, LocSet(l_newroot), firstChild_lset)
            h_3 = appendChild(h_2, LocSet(l_root), firstChild_lset)
            firstChild_lset = Helper.Proto(h_3, l_newroot, AbsString.alpha("firstChild"))._2
          }
          (h_3, ctx_1)
      }
    }
    else (h, ctx)
  }


  // Handle side effects caused by DOM property update
  // Property update is performed in Helper.PropStore, not in this function
  def updateDOMProp(h: Heap, ctx: Context, l: Loc, s: AbsString, v: Value, cfg: CFG, addresskey: Int): (Heap, Context) = {
    AbsString.concretize(s) match {
      case Some(str) => str match {
        /* innerHTML : update the DOM tree */
        case "innerHTML" => 
          System.err.println("* Warning: Assigning a value to 'innerHTML'.") 
          cfg.initStoreAddressIndex(addresskey)
          updateDOMTree(h, ctx, l, v, cfg, addresskey)
        // case "id" =>
        // case "onclick" =>
        case _ => (h, ctx)
      }
      case None if OtherStr <= s => 
        System.err.println("* Warning: 'innerHTML' of an HTML element might be updated but the DOM tree is not updated.") 
        (h, ctx)
      case _ => (h, ctx)
    }
  }

  // Add the instance object in the heap 
  private def addInstance(h: Heap, loc_ins: Loc, list_ins: List[(String, PropValue)]): Heap = {
    // create the instance object and update properties
    val obj_ins = list_ins.foldLeft(ObjEmpty) ((obj, v) => obj.update(AbsString.alpha(v._1), v._2))
    h.update(loc_ins, obj_ins)
  }



  // Initialize named properties in Document
  // WHATWG HTML Living Standard - Section 3.1.4 DOM tree accessors
  private def updateDocumentNamedProps(h: Heap, ctx: Context, node: Node, cfg: CFG, ins_loc: Loc, addresskey: Int, init: Boolean) : (Heap, Context) = {
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
               (h.update(HTMLDocument.GlobalDocumentLoc, new_docobj), ctx)
            }
            // in case that the 'id' property already exists
            else {
               val loc_existing = propval._1._1._1._2
               val (h1, ctx1, val_locset) = loc_existing.foldLeft((h, ctx, LocSetBot))((hcl, ll) => {
                 val obj_existing = hcl._1(ll)
                 val tagName = obj_existing("tagName")
                 val len = obj_existing("length")
                 // in case that the 'tagName' property exists: DOM element
                 if(tagName._2 <= AbsentBot) {
                   // HTMLCollection
                   val (h_1, ctx_1, loc_collection) = if(init) (hcl._1, hcl._2, HTMLCollection.getInstance(cfg).get)
                                            else cfgAddrToLoc(hcl._1, hcl._2, cfg.getStoreAddress(addresskey))
                   val collection_proplist = HTMLCollection.getInsList(2) ::: 
                     List(("0", PropValue(ObjectValue(ll, T, T, T))), ("1", PropValue(ObjectValue(ins_loc, T, T, T))))
                   (addInstance(h_1, loc_collection, collection_proplist), ctx_1, hcl._3 + loc_collection)
                 }
                 // in case that the 'tagName' property does not exist and 'length' exists: HTMLCollection
                 else if(len._2 <= AbsentBot) {
                   val n_len = Operator.ToUInt32(len._1._1._1)
                   n_len match {
                   case UIntSingle(n) =>
                     val new_collection = obj_existing.update(
                       AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
                       AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
                     (hcl._1.update(ll, new_collection), hcl._2, hcl._3 + ll)
                   case NumTop | UInt =>
                     val new_collection = obj_existing.update(NumStr, PropValue(ObjectValue(ins_loc, T, T, T)))
                     (hcl._1.update(ll, new_collection), hcl._2, hcl._3 + ll)
      
                   case _ => (hcl._1, hcl._2, hcl._3)
                   }
                 }
                 else (hcl._1, hcl._2, hcl._3)
               })
               val new_docobj = docobj.update(AbsString.alpha(id), PropValue(ObjectValue(Value(val_locset), T, T, T)))
               (h1.update(HTMLDocument.GlobalDocumentLoc, new_docobj), ctx1)
            }
          case "APPLET" | "FORM" | "IFRAME" | "IMG" | "OBJECT" if name != "" =>
            val propval = docobj(name)
            // in case that the 'name' property does not exist
            if(propval._2 </ AbsentBot) {
               val new_docobj = docobj.update(AbsString.alpha(name), PropValue(ObjectValue(ins_loc, T, T, T)))
               (h.update(HTMLDocument.GlobalDocumentLoc, new_docobj), ctx)
            }
            // in case that the 'name' property already exists
            else {
               val loc_existing = propval._1._1._1._2
               val (h1, ctx1, val_locset) = loc_existing.foldLeft((h, ctx, LocSetBot))((hcl, ll) => {
                 val obj_existing = hcl._1(ll)
                 val tagName = obj_existing("tagName")
                 val len = obj_existing("length")
                 // in case that the 'tagName' property exists: DOM element
                 if(tagName._2 <= AbsentBot) {
                   // HTMLCollection
                   val (h_1, ctx_1, loc_collection) = if(init) (hcl._1, hcl._2, HTMLCollection.getInstance(cfg).get)
                                            else cfgAddrToLoc(hcl._1, hcl._2, cfg.getStoreAddress(addresskey))
                   val collection_proplist = HTMLCollection.getInsList(2) ::: 
                     List(("0", PropValue(ObjectValue(ll, T, T, T))), ("1", PropValue(ObjectValue(ins_loc, T, T, T))))
                   (addInstance(h_1, loc_collection, collection_proplist), ctx_1, hcl._3 + loc_collection)
                 }
                 // in case that the 'tagName' property does not exist and 'length' exists: HTMLCollection
                 else if(len._2 <= AbsentBot) {
                   val n_len = Operator.ToUInt32(len._1._1._1)
                   n_len match {
                   case UIntSingle(n) =>
                     val new_collection = obj_existing.update(
                       AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
                       AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
                     (hcl._1.update(ll, new_collection), hcl._2, hcl._3 + ll)
                   case NumTop | UInt =>
                     val new_collection = obj_existing.update(NumStr, PropValue(ObjectValue(ins_loc, T, T, T)))
                     (hcl._1.update(ll, new_collection), hcl._2, hcl._3 + ll)
      
                   case _ => (hcl._1, hcl._2, hcl._3)
                   }
                 }
                 else (hcl._1, hcl._2, hcl._3)
               })
               val new_docobj = docobj.update(AbsString.alpha(name), PropValue(ObjectValue(Value(val_locset), T, T, T)))
               (h1.update(HTMLDocument.GlobalDocumentLoc, new_docobj), ctx1)
               
            }
          case _ => (h, ctx)
      }
      // Non-element node
      case _ => (h, ctx)
    }
  }


  // Update the id lookup table, name lookup table, tag lookup table, and class lookup table 
  // for getElementById, getElementsByName, getElementsByTagName,
  // also initialize the event target table
  private def updateLookupTables(h: Heap, ctx: Context, node: Node, cfg: CFG, ins_loc: Loc, addresskey: Int, init: Boolean) : (Heap, Context) = node match {
    // Element node
    case e: Element => 
      /* id look-up table update */
      val id_table = h(IdTableLoc)
      val id = e.getAttribute("id")
      val h_1 = 
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
         h.update(IdTableLoc, new_id_table)
        }
        else h

       /* name look-up table update */
       val name_table = h_1(NameTableLoc)
       val name = e.getAttribute("name")
       val (h_2, ctx_2) =
       // if the element has a name,
         if(name!=""){
           val mapped_node = name_table(name)
            // in case that the mapping does not exist
           if(mapped_node._2 </ AbsentBot) {
             val (h_1_1, ctx_2_1, loc_nodelist) = if(init) (h_1, ctx, DOMNodeList.getInstance(cfg).get)
                                            else cfgAddrToLoc(h_1, ctx, cfg.getStoreAddress(addresskey))
             val nodelist_proplist = DOMNodeList.getInsList(1) :+
               ("0", PropValue(ObjectValue(ins_loc, T, T, T)))
             val new_name_table = name_table.update(AbsString.alpha(name), PropValue(ObjectValue(loc_nodelist, T, T, T)))
             (addInstance(h_1_1, loc_nodelist, nodelist_proplist).update(NameTableLoc, new_name_table), ctx_2_1)
           }
           // in case that the mapping already exists
           else {
             val nodelist_locset = mapped_node._1._1._1._2
             val newheap2 = nodelist_locset.foldLeft(h_1)((h, l) => {
               val nodelist_obj = h(l)
               val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
               n_len match {
                 case UIntSingle(n) =>
                   val new_nodelist1 = nodelist_obj.update(
                     AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
                     AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
                   h.update(l, new_nodelist1)
                 case NumTop | UInt =>
                   val new_nodelist1 = nodelist_obj.update(NumStr, PropValue(ObjectValue(ins_loc, T, T, T)))
                   h.update(l, new_nodelist1)
      
                case _ => h

              }
             })
             (newheap2, ctx)
           }
         }
         else (h_1, ctx) 
       
       /* tag look-up table update */
       val tag_table = h_2(TagTableLoc)
       val tag = e.getTagName
       val (h_3, ctx_3) =
       // if the element has a tag name,
         if(tag!=null){
           val mapped_node = tag_table(tag)
           // in case that the mapping does not exist
           if(mapped_node._2 </ AbsentBot) {
             val (h_2_1, ctx_3_1, loc_nodelist) = if(init) (h_2, ctx_2, DOMNodeList.getInstance(cfg).get)
                                            else cfgAddrToLoc(h_2, ctx_2, cfg.getStoreAddress(addresskey))
             val nodelist_proplist = DOMNodeList.getInsList(1) :+
               ("0", PropValue(ObjectValue(ins_loc, T, T, T)))
             val new_tag_table = tag_table.update(AbsString.alpha(tag), PropValue(ObjectValue(loc_nodelist, T, T, T)))
             (addInstance(h_2_1, loc_nodelist, nodelist_proplist).update(TagTableLoc, new_tag_table), ctx_3_1)
           }
           // in case that the mapping already exists
           else {
             val nodelist_locset = mapped_node._1._1._1._2
             val newheap2 = nodelist_locset.foldLeft(h_2)((h, l) => {
               val nodelist_obj = h(l)
               val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
               n_len match {
                 case UIntSingle(n) =>
                   val new_nodelist1 = nodelist_obj.update(
                     AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
                     AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
                   h.update(l, new_nodelist1)
                 case NumTop | UInt =>
                   val new_nodelist1 = nodelist_obj.update(NumStr, PropValue(ObjectValue(ins_loc, T, T, T)))
                   h.update(l, new_nodelist1)
      
                case _ => h

              }
             })
             (newheap2, ctx_2)
           }
         }
         else (h_2, ctx_2)

       /* class look-up table update */
       val class_table = h_3(ClassTableLoc)
       val classname = e.getAttribute("class")
       val (h_4, ctx_4) =
       // if the element has a class name,
         if(classname!=""){
           val mapped_node = class_table(classname)
           // in case that the mapping does not exist
           if(mapped_node._2 </ AbsentBot) {
             val (h_3_1, ctx_4_1, loc_nodelist) = if(init) (h_3, ctx_3, DOMNodeList.getInstance(cfg).get)
                                            else cfgAddrToLoc(h_3, ctx_3, cfg.getStoreAddress(addresskey))
             val nodelist_proplist = DOMNodeList.getInsList(1) :+
               ("0", PropValue(ObjectValue(ins_loc, T, T, T)))
             val new_class_table = class_table.update(AbsString.alpha(classname), PropValue(ObjectValue(loc_nodelist, T, T, T)))
             (addInstance(h_3_1, loc_nodelist, nodelist_proplist).update(ClassTableLoc, new_class_table), ctx_4_1)
           }
           // in case that the mapping already exists
           else {
             val nodelist_locset = mapped_node._1._1._1._2
             val newheap2 = nodelist_locset.foldLeft(h_3)((h, l) => {
               val nodelist_obj = h(l)
               val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
               n_len match {
                 case UIntSingle(n) =>
                   val new_nodelist1 = nodelist_obj.update(
                     AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(ins_loc, T, T, T))).update(
                     AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
                   h.update(l, new_nodelist1)
                 case NumTop | UInt =>
                   val new_nodelist1 = nodelist_obj.update(NumStr, PropValue(ObjectValue(ins_loc, T, T, T)))
                   h.update(l, new_nodelist1)
      
                case _ => h

              }
             })
             (newheap2, ctx_3)
           }
         }
         else (h_3, ctx_3)

       /* event target table update */
       val event_target_table = h_4(EventTargetTableLoc)
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
      
       (h_4.update(EventTargetTableLoc, event_target_table_5), ctx_4)

  // non-Element node
    case _ => (h, ctx)
  }



  // update the 'form' property of target object and 'elements' property in HTMLFormElement
  private def updateFormProps(h: Heap, name: String, id: String, formloc : Option[Loc], targetloc : Loc): Heap = {
    formloc match {
      // update the 'elements' property in HTMLFormElement
      case Some(l) =>
        // HTMLFormElement object
        val form_obj = h(l)
        val new_form_obj = form_obj.update(AbsString.alpha(name), PropValue(ObjectValue(targetloc, T, T, T)))
        val elements_locset = form_obj("elements")._1._1._1._2
        val newheap2 = elements_locset.foldLeft(h)((h, ll) => {
          val elements_obj = h(ll)
          val n_len = Operator.ToUInt32(Helper.Proto(h, ll, AbsString.alpha("length")))
          n_len match {
            case UIntSingle(n) =>
              val new_elements1 = elements_obj.update(
                 AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(targetloc, T, T, T))).update(
                 AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
              val new_elements2 = if(name!="") new_elements1.update(AbsString.alpha(name), PropValue(ObjectValue(targetloc, T, T, T)))
                               else new_elements1
              val new_elements3 = if(id!="") new_elements2.update(AbsString.alpha(id), PropValue(ObjectValue(targetloc, T, T, T)))
                               else new_elements2
              h.update(ll, new_elements3)
            case NumTop | UInt =>
              val new_elements1 = elements_obj.update(NumStr, PropValue(ObjectValue(targetloc, T, T, T)))
                 
              val new_elements2 = if(name!="") new_elements1.update(AbsString.alpha(name), PropValue(ObjectValue(targetloc, T, T, T)))
                               else new_elements1
              val new_elements3 = if(id!="") new_elements2.update(AbsString.alpha(id), PropValue(ObjectValue(targetloc, T, T, T)))
                               else new_elements2
              h.update(ll, new_elements3)
              
            case _ => h
          }

        })
       val new_obj = newheap2(targetloc).update(AbsString.alpha("form"), PropValue(ObjectValue(l, F, T, T)))
       newheap2.update(targetloc, new_obj).update(l, new_form_obj)
     case None => h
    }
  }
    
  // Model a node in a dom tree
  def modelNode(h: Heap, ctx: Context, node : Node, cfg: CFG, form : Option[Loc], addresskey: Int, init: Boolean) : ((Heap, Context), Loc) = {
    val nodeName = node.getNodeName
    val (_h, _ctx, loc) = if(init) (h, ctx, DOMNode.getInstance(cfg).get)
                          else cfgAddrToLoc(h, ctx, cfg.getStoreAddress(addresskey))
    val (h_1, ctx_1, ins_loc) = node match {
      // Attr
      case a: Attr =>
        val newheap=addInstance(_h, loc, DOMAttr.getInsList(node))
        // the Attr object does not have any siblings and parent
        val newAttrObj=newheap(loc).
                        update(OtherStrSingle("previousSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("nextSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("parentNode"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T)))
          (newheap.update(loc, newAttrObj), _ctx, loc)

      // DocumentType
      case d: DocumentType => 
        val newheap=addInstance(_h, loc, DOMDocumentType.getInsList(node))
        (newheap, _ctx, loc)
      // Text node
      case t: Text =>
        val newheap=addInstance(_h, loc, DOMText.getInsList(node))
        (newheap, _ctx, loc)
      // Comment node
      case c: Comment =>
        val newheap=addInstance(_h, loc, DOMComment.getInsList(node))
        (newheap, _ctx, loc)
      // DocumentFragment node
      case d: DocumentFragment =>
        val newheap=addInstance(_h, loc, DOMDocumentFragment.getInsList(node))
        // the DocumentFragment object does not have any siblings and parent
        val newObj=newheap(loc).
                        update(OtherStrSingle("previousSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("nextSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("parentNode"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T)))
        (newheap.update(loc, newObj), _ctx, loc)

      // Document
      case d: Document =>
        val loc = HTMLDocument.getInstance(cfg).get
        val newheap=addInstance(_h, loc, HTMLDocument.getInsList(node))

        // 'forms' property
        val (newheap_1, ctx_1, loc_forms) = if(init) (newheap, _ctx, HTMLCollection.getInstance(cfg).get)
                                            else cfgAddrToLoc(newheap, _ctx, cfg.getStoreAddress(addresskey))
        val newheap2 = addInstance(newheap_1, loc_forms, HTMLCollection.getInsList(0))
        
        // 'images' property
        val (newheap2_1, ctx_2, loc_images) = if(init) (newheap2, ctx_1, HTMLCollection.getInstance(cfg).get)
                                            else cfgAddrToLoc(newheap2, ctx_1, cfg.getStoreAddress(addresskey))
        val newheap3 = addInstance(newheap2_1, loc_images, HTMLCollection.getInsList(0))
        
        // 'scripts' property
        val (newheap3_1, ctx_3, loc_scripts) = if(init) (newheap3, ctx_2, HTMLCollection.getInstance(cfg).get)
                                            else cfgAddrToLoc(newheap3, ctx_2, cfg.getStoreAddress(addresskey))
        val newheap4 = addInstance(newheap3_1, loc_scripts, HTMLCollection.getInsList(0))
        
        // 'all' property
        val (newheap4_1, ctx_4, loc_all) =  if(init) (newheap4, ctx_3, HTMLCollection.getInstance(cfg).get)
                                            else cfgAddrToLoc(newheap4, ctx_3, cfg.getStoreAddress(addresskey))
        val newheap5 = addInstance(newheap4_1, loc_all, HTMLAllCollection.getInsList(0))

        // the root element does not have any siblings and parent
        val newElementObj=newheap4(loc).
                        update(OtherStrSingle("previousSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("nextSibling"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("parentNode"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("offsetParent"),
                                PropValue(ObjectValue(PValue(NullTop), F, T, T))).
                        update(OtherStrSingle("forms"),
                                PropValue(ObjectValue(Value(loc_forms), F, T, T))).
                        update(OtherStrSingle("images"),
                                PropValue(ObjectValue(Value(loc_images), F, T, T))).
                        update(OtherStrSingle("scripts"),
                                PropValue(ObjectValue(Value(loc_scripts), F, T, T))).
                        update(OtherStrSingle("all"),
                                PropValue(ObjectValue(Value(loc_all), F, T, T)))
          (newheap5.update(loc, newElementObj), ctx_4, loc)
      // Element
      case e: Element => 
        val (_newheap, ctx1_1, _insloc) = nodeName match {
          case "HTML" =>
            val newheap=addInstance(_h, loc, HTMLHtmlElement.getInsList(node))
              // update 'documentElement' of HTMLDocument
            val doc_loc = HTMLDocument.getInstance().get
            val new_doc = newheap(doc_loc).
            update(OtherStrSingle("documentElement"), PropValue(ObjectValue(loc, F, T, T)))
            (newheap.update(doc_loc, new_doc), _ctx, loc)
          case "HEAD" =>
            val newheap=addInstance(_h, loc, HTMLHeadElement.getInsList(node))
            /* 'document.head' update */
            // 'document' object
            val docobj = newheap(HTMLDocument.GlobalDocumentLoc)
            val newdocobj = docobj.update(AbsString.alpha("head"), PropValue(ObjectValue(Value(loc), F, T, T))) 
            (newheap.update(HTMLDocument.GlobalDocumentLoc, newdocobj), _ctx, loc)
          case "LINK" =>
            val newheap=addInstance(_h, loc, HTMLLinkElement.getInsList(node))
            (newheap, _ctx, loc)
          case "TITLE" =>
            val newheap=addInstance(_h, loc, HTMLTitleElement.getInsList(node))
            (newheap, _ctx, loc)
          case "META" =>
            val newheap=addInstance(_h, loc, HTMLMetaElement.getInsList(node))
            (newheap, _ctx, loc)
          case "BASE" =>
            val newheap=addInstance(_h, loc, HTMLBaseElement.getInsList(node))
            (newheap, _ctx, loc)
          case "ISINDEX" =>
            val newheap=addInstance(_h, loc, HTMLIsIndexElement.getInsList(node))
            (newheap, _ctx, loc)
          case "STYLE" =>
            val newheap=addInstance(_h, loc, HTMLIsIndexElement.getInsList(node))
            (newheap, _ctx, loc)
          case "BODY" =>
            val loc = HTMLBodyElement.getInstance(cfg).get
            val newheap=addInstance(_h, loc, HTMLBodyElement.getInsList(node))
            (newheap, _ctx, loc)
          case "FORM" =>
            val newheap=addInstance(_h, loc, HTMLFormElement.getInsList(node))
            /* 'document.forms' update */
            // 'document' object
            val docobj = newheap(HTMLDocument.GlobalDocumentLoc)

            val forms_locset = docobj("forms")._1._1._1._2
            val newheap2 = forms_locset.foldLeft(newheap)((h, l) => {
              val forms_obj = h(l)
              val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
              n_len match {
                case UIntSingle(n) =>
                  val new_forms1 = forms_obj.update(
                     AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(loc, T, T, T))).update(
                     AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
                  val name = e.getAttribute("name")
                  val new_forms2 = if(name!="") new_forms1.update(AbsString.alpha(name), PropValue(ObjectValue(loc, T, T, T)))
                                   else new_forms1
                  h.update(l, new_forms2)
                case NumTop | UInt =>
                  val new_forms1 = forms_obj.update(NumStr, PropValue(ObjectValue(loc, T, T, T)))
                  val name = e.getAttribute("name")
                  val new_forms2 = if(name!="") new_forms1.update(AbsString.alpha(name), PropValue(ObjectValue(loc, T, T, T)))
                                   else new_forms1
                  h.update(l, new_forms2)
      
                case _ => h

              }
            })
            // 'elements' property
            val (newheap2_1, ctx_1, loc_elements) = if(init) (newheap2, _ctx, HTMLCollection.getInstance(cfg).get)
                               else cfgAddrToLoc(newheap2, _ctx, cfg.getStoreAddress(addresskey))

            val newheap3 = addInstance(newheap2_1, loc_elements, HTMLCollection.getInsList(0))

            val new_formobj = newheap3(loc).
                        update(OtherStrSingle("elements"),
                                PropValue(ObjectValue(Value(loc_elements), F, T, T)))

            (newheap3.update(loc, new_formobj), ctx_1, loc)
          case "SELECT" =>
            val newheap=addInstance(_h, loc, HTMLSelectElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), _ctx, loc)
          case "OPTGROUP" =>
            val newheap=addInstance(_h, loc, HTMLOptGroupElement.getInsList(node))
            (newheap, _ctx, loc)
          case "OPTION" =>
            val newheap=addInstance(_h, loc, HTMLOptionElement.getInsList(node))
            (newheap, _ctx, loc)
          case "INPUT" =>
            val newheap=addInstance(_h, loc, HTMLInputElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), _ctx, loc)
          case "TEXTAREA" =>
            val newheap=addInstance(_h, loc, HTMLTextAreaElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), _ctx, loc)
          case "BUTTON" =>
            val newheap=addInstance(_h, loc, HTMLButtonElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), _ctx, loc)
          case "LABEL" =>
            val newheap=addInstance(_h, loc, HTMLLabelElement.getInsList(node))
            (newheap, _ctx, loc)
          case "FIELDSET" =>
            val newheap=addInstance(_h, loc, HTMLFieldSetElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), _ctx, loc)
          case "LEGEND" =>
            val newheap=addInstance(_h, loc, HTMLLegendElement.getInsList(node))
            (newheap, _ctx, loc)
          case "UL" =>
            val newheap=addInstance(_h, loc, HTMLUListElement.getInsList(node))
            (newheap, _ctx, loc)
          case "OL" =>
            val newheap=addInstance(_h, loc, HTMLOListElement.getInsList(node))
            (newheap, _ctx, loc)
          case "DL" =>
            val newheap=addInstance(_h, loc, HTMLDListElement.getInsList(node))
            (newheap, _ctx, loc)
          case "DIR" =>
            val newheap=addInstance(_h, loc, HTMLDirectoryElement.getInsList(node))
            (newheap, _ctx, loc)
          case "MENU" =>
            val newheap=addInstance(_h, loc, HTMLMenuElement.getInsList(node))
            (newheap, _ctx, loc)
          case "LI" =>
            val newheap=addInstance(_h, loc, HTMLLIElement.getInsList(node))
            (newheap, _ctx, loc)
          case "DIV" =>
            val newheap=addInstance(_h, loc, HTMLDivElement.getInsList(node))
            (newheap, _ctx, loc)
          case "P" =>
            val newheap=addInstance(_h, loc, HTMLParagraphElement.getInsList(node))
            (newheap, _ctx, loc)
          // Heading element
          case "H1" | "H2" | "H3" | "H4" | "H5" | "H6"  =>
            val newheap=addInstance(_h, loc, HTMLHeadingElement.getInsList(node))
            (newheap, _ctx, loc)
          // Quote element
          case "BLACKQUOTE" | "Q" =>
            val newheap=addInstance(h, loc, HTMLQuoteElement.getInsList(node))
            (newheap, _ctx, loc)
          case "PRE" =>
            val newheap=addInstance(_h, loc, HTMLPreElement.getInsList(node))
            (newheap, _ctx, loc)
          case "BR" =>
            val newheap=addInstance(_h, loc, HTMLBRElement.getInsList(node))
            (newheap, _ctx, loc)
          // BASEFONT Element : deprecated
          case "BASEFONT" =>
            val newheap=addInstance(_h, loc, HTMLBaseFontElement.getInsList(node))
            (newheap, _ctx, loc)
          // FONT Element : deprecated
          case "FONT" =>
            val newheap=addInstance(_h, loc, HTMLFontElement.getInsList(node))
            (newheap, _ctx, loc)
          case "HR" =>
            val newheap=addInstance(_h, loc, HTMLHRElement.getInsList(node))
            (newheap, _ctx, loc)
          case "INS" | "DEL" =>
            val newheap=addInstance(_h, loc, HTMLModElement.getInsList(node))
            (newheap, _ctx, loc)
          case "A" =>
            val newheap=addInstance(_h, loc, HTMLAnchorElement.getInsList(node))
            (newheap, _ctx, loc)
          case "IMG" =>
            val newheap=addInstance(_h, loc, HTMLImageElement.getInsList(node))
            /* 'document.images' update */
            // 'document' object
            val docobj = newheap(HTMLDocument.GlobalDocumentLoc)
            val images_locset = docobj("images")._1._1._1._2
            val newheap2 = images_locset.foldLeft(newheap)((h, l) => {
              val images_obj = h(l)
              val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
              n_len match {
                case UIntSingle(n) =>
                  val new_images1 = images_obj.update(
                     AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(loc, T, T, T))).update(
                     AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
                  val name = e.getAttribute("name")
                  val new_images2 = if(name!="") new_images1.update(AbsString.alpha(name), PropValue(ObjectValue(loc, T, T, T)))
                                   else new_images1
                  h.update(l, new_images2)
                case NumTop | UInt =>
                  val new_images1 = images_obj.update(NumStr, PropValue(ObjectValue(loc, T, T, T)))
                  val name = e.getAttribute("name")
                  val new_images2 = if(name!="") new_images1.update(AbsString.alpha(name), PropValue(ObjectValue(loc, T, T, T)))
                                   else new_images1
                  h.update(l, new_images2)
      
                case _ => h

              }
            })
            (newheap2, _ctx, loc)
          case "OBJECT" =>
            val newheap=addInstance(_h, loc, HTMLObjectElement.getInsList(node))
            (updateFormProps(newheap, e.getAttribute("name"), e.getAttribute("id"), form, loc), _ctx, loc)
          case "PARAM" =>
            val newheap=addInstance(_h, loc, HTMLParamElement.getInsList(node))
            (newheap, _ctx, loc)
          // APPLET element : deprecated
          case "APPLET" =>
            val newheap=addInstance(_h, loc, HTMLAppletElement.getInsList(node))
            (newheap, _ctx, loc)
          case "MAP" =>
            val newheap=addInstance(_h, loc, HTMLMapElement.getInsList(node))
            (newheap, _ctx, loc)
          case "AREA" =>
            val newheap=addInstance(_h, loc, HTMLAreaElement.getInsList(node))
            (newheap, _ctx, loc)
          case "SCRIPT" =>
            val newheap=addInstance(_h, loc, HTMLScriptElement.getInsList(node))
            /* 'document.scripts' update */
            // 'document' object
            val docobj = newheap(HTMLDocument.GlobalDocumentLoc)

            val scripts_locset = docobj("scripts")._1._1._1._2
            val newheap2 = scripts_locset.foldLeft(newheap)((h, l) => {
              val scripts_obj = h(l)
              val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
              n_len match {
                case UIntSingle(n) =>
                  val new_scripts1 = scripts_obj.update(
                     AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(loc, T, T, T))).update(
                     AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
                  val name = e.getAttribute("name")
                  val new_scripts2 = if(name!="") new_scripts1.update(AbsString.alpha(name), PropValue(ObjectValue(loc, T, T, T)))
                                   else new_scripts1
                  h.update(l, new_scripts2)
                case NumTop | UInt =>
                  val new_scripts1 = scripts_obj.update(NumStr, PropValue(ObjectValue(loc, T, T, T)))
                  val name = e.getAttribute("name")
                  val new_scripts2 = if(name!="") new_scripts1.update(AbsString.alpha(name), PropValue(ObjectValue(loc, T, T, T)))
                                   else new_scripts1
                  h.update(l, new_scripts2)
                case _ => h

              }
            })
            (newheap2, _ctx, loc)
          case "TABLE" =>
            val newheap=addInstance(_h, loc, HTMLTableElement.getInsList(node))
            (newheap, _ctx, loc)
          case "CAPTION" =>
            val newheap=addInstance(_h, loc, HTMLTableCaptionElement.getInsList(node))
            (newheap, _ctx, loc)
          case "COL" =>
            val newheap=addInstance(_h, loc, HTMLTableColElement.getInsList(node))
            (newheap, _ctx, loc)
          case "THEAD" | "TFOOT" | "TBODY" =>
            val newheap=addInstance(_h, loc, HTMLTableSectionElement.getInsList(node))
            (newheap, _ctx, loc)
          case "TR"  =>
            val newheap=addInstance(_h, loc, HTMLTableRowElement.getInsList(node))
            (newheap, _ctx, loc)
          case "TH" | "TD"  =>
            val newheap=addInstance(_h, loc, HTMLTableCellElement.getInsList(node))
            (newheap, _ctx, loc)
          case "FRAMESET"  =>
            val newheap=addInstance(_h, loc, HTMLFrameSetElement.getInsList(node))
            (newheap, _ctx, loc)
          case "FRAME"  =>
            val newheap=addInstance(_h, loc, HTMLFrameElement.getInsList(node))
            (newheap, _ctx, loc)
          case "IFRAME"  =>
            val newheap=addInstance(_h, loc, HTMLIFrameElement.getInsList(node))
            (newheap, _ctx, loc)
          // Special tags
          case "SUB" | "SUP" | "SPAN" | "BDO" | "BDI" =>
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(_h, loc, prop_list)
            (newheap, _ctx, loc)    
          // Font tags
          case "TT" | "I" | "B" | "U" | "S" | "STRIKE" | "BIG" | "SMALL" =>
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(_h, loc, prop_list)
            (newheap, _ctx, loc)    
          // Phrase tags
          case "EM" | "STRONG" | "DFN" | "CODE" | "SAMP" | "KBD" | "VAR" | "CITE" | "ACRONYM" | "ABBR" =>
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(_h, loc, prop_list)
            (newheap, _ctx, loc)    
          // List tags
          case "DD" | "DT" =>
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(_h, loc, prop_list)
            (newheap, _ctx, loc)    
          // etc
          case "NOFRAMES" | "NOSCRIPT" | "ADDRESS" | "CENTER"  =>
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(_h, loc, prop_list)
            (newheap, _ctx, loc)
          /* HTML 5 */
          case "CANVAS"  =>
            val newheap=addInstance(_h, loc, HTMLCanvasElement.getInsList(node))
            (newheap, _ctx, loc)
          case "DATALIST"  =>
            val newheap=addInstance(_h, loc, HTMLDataListElement.getInsList(node))
            (newheap, _ctx, loc)
          case "HEADER" | "FOOTER" | "ARTICLE" | "SECTION" | "NAV" =>
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(_h, loc, prop_list)
            (newheap, _ctx, loc)    

          case _ =>
            System.err.println("* Warning: " + node.getNodeName + " - not modeled yet.")
            val prop_list = HTMLElement.getInsList(node)++List(
              ("@class",   PropValue(AbsString.alpha("Object"))),
              ("@proto",   PropValue(ObjectValue(HTMLElement.getProto.get, F, F, F))),
              ("@extensible",   PropValue(BoolTrue)))
            val newheap = addInstance(h, loc, prop_list)
            (newheap, _ctx, loc)
        }
        // update the "style" and "innerText" property
        val (_newheap1, ctx1_2, styleloc) = if(init) (_newheap, ctx1_1, CSSStyleDeclaration.getInstance(cfg).get)
                                else cfgAddrToLoc(_newheap, ctx1_1, cfg.getStoreAddress(addresskey))

        val _newheap2 = addInstance(_newheap1, styleloc, CSSStyleDeclaration.getInsList)
        val newelem = _newheap2(_insloc).update("style", PropValue(ObjectValue(Value(styleloc), T, T, T))).
                                         update("innerText", _newheap2(_insloc)("textContent")._1)
        /* 'document.all' update */
        // 'document' object
        val docobj = _newheap2(HTMLDocument.GlobalDocumentLoc)
        
        val all_locset = docobj("all")._1._1._1._2
        val _newheap3 = all_locset.foldLeft(_newheap2)((h, l) => {
          val all_obj = h(l)
          val n_len = Operator.ToUInt32(Helper.Proto(h, l, AbsString.alpha("length")))
          n_len match {
            case UIntSingle(n) =>
              val new_all1 = all_obj.update(
                 AbsString.alpha(n.toInt.toString), PropValue(ObjectValue(_insloc, T, T, T))).update(
                 AbsString.alpha("length"), PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
              val id = e.getAttribute("id")
              val new_all2 = if(id!="") new_all1.update(AbsString.alpha(id), PropValue(ObjectValue(_insloc, T, T, T)))
                               else new_all1
              h.update(l, new_all2)
            case NumTop | UInt =>
              val new_all1 = all_obj.update(NumStr, PropValue(ObjectValue(_insloc, T, T, T)))
              val id = e.getAttribute("id")
              val new_all2 = if(id!="") new_all1.update(AbsString.alpha(id), PropValue(ObjectValue(loc, T, T, T)))
                               else new_all1
              h.update(l, new_all2)
            case _ => h

          }
        })
        val _newheap4 = HTMLTopElement.setInsLoc(_newheap3, _insloc)
        (_newheap4.update(_insloc, newelem), ctx1_2, _insloc)
      case _ =>
        // the node, not modeled yet, gets a dummy location for the 'Element' node 
        val newheap=addInstance(_h, loc, List())
        System.err.println("* Warning: " + node.getNodeName + " - not modeled yet.")
        (newheap, _ctx, loc)

    }

    // 'attributes' property update
    val attributes = node.getAttributes
    val (h_2, ctx_2, attributes_val) = if(attributes==null) (h_1, ctx_1, PropValue(ObjectValue(NullTop, F, T, T)))
      else {
        val length = attributes.getLength
        val (h_1_1, ctx_2_1, attributes_loc) = if(init) (h_1, ctx_1, DOMNamedNodeMap.getInstance(cfg).get)
                                      else cfgAddrToLoc(h_1, ctx_1, cfg.getStoreAddress(addresskey))
        val (h_2_1, ctx_2_2, attributes_objlist) = (0 until length).foldLeft[(Heap, Context, List[(String, PropValue)])]((h_1_1, ctx_2_1, DOMNamedNodeMap.getInsList(length)))((hcl, i) => {
           val attr = attributes.item(i)
           val ((newheap, new_ctx), attr_loc) = buildDOMTree(hcl._1, hcl._2, attr, cfg, None, addresskey, init)
           val newlist: List[(String, PropValue)] = hcl._3 ++ List(
            (i.toString, PropValue(ObjectValue(attr_loc, T, T, T))),
            (attr.getNodeName.toLowerCase, PropValue(ObjectValue(attr_loc, T, T, T)))
           )
           (newheap, new_ctx, newlist) 
        })
        (addInstance(h_2_1, attributes_loc, attributes_objlist), ctx_2_2, PropValue(ObjectValue(attributes_loc, F, T, T)))           
      }
    
    val ins_obj_new = h_2(ins_loc).update(AbsString.alpha("attributes"), attributes_val)
    
    // initialize id, name, tag, and event look-up tables
    val (h_3, ctx_3) = updateLookupTables(h_2.update(ins_loc, ins_obj_new), ctx_2, node, cfg, ins_loc, addresskey, init)
    // initialize named properties in Document
    val (h_4, ctx_4) = updateDocumentNamedProps(h_3, ctx_3, node, cfg, ins_loc, addresskey, init)
    ((h_4, ctx_4), ins_loc)
  }
  
  
  private def cfgAddrToLoc(h: Heap, ctx: Context, addr: Address): (Heap, Context, Loc) = {
    val l = addrToLoc(addr, Recent)
    val (h1, ctx1) = Helper.Oldify(h, ctx, addr)
    (h1, ctx1, l)
  }


  // Construct a DOM tree for the html source
  // 'form' : keeps a location of HTMLFormElement if any.
  // 'init' : indicates whether this function is called before analysis or not
  def buildDOMTree(h: Heap, ctx: Context, node : Node, cfg: CFG, form : Option[Loc], addresskey: Int, init: Boolean) : ((Heap, Context), Loc) = {
    val children : NodeList = node.getChildNodes
    val num_children = children.getLength
        
    val ((h_1, _ctx), absloc1) = modelNode(h, ctx, node, cfg, form, addresskey, init)     

    if(num_children == 0) {
      val (h_1_1, ctx_1, absloc2) = if(init) (h_1, _ctx, DOMNodeList.getInstance(cfg).get) 
                                    else cfgAddrToLoc(h_1, _ctx, cfg.getStoreAddress(addresskey))
      val h_2 = addInstance(h_1_1, absloc2, DOMNodeList.getInsList(0))
      val newElementObj = h_2(absloc1).
        update(OtherStrSingle("childNodes"),    PropValue(ObjectValue(absloc2, F, T, T))).
        update(OtherStrSingle("firstChild"),    PropValue(ObjectValue(PValue(NullTop), F, T, T))).
        update(OtherStrSingle("lastChild"),     PropValue(ObjectValue(PValue(NullTop), F, T, T)))
      ((h_2.update(absloc1, newElementObj), ctx_1), absloc1)
    }

    else {
      val formelement = if(node.getNodeName == "FORM") Some(absloc1) else form
      val (h_2, ctx_1, absloc_list) = (0 until num_children).foldLeft[(Heap, Context, List[Loc])]((h_1, _ctx, List()))((hcl, i) => {
        val ((_h, _ctx1), absloc2) = buildDOMTree(hcl._1, hcl._2, children.item(i), cfg, formelement, addresskey, init)
        (_h, _ctx1, hcl._3 :+ absloc2)
      })

      val (h_2_1, ctx_2, absloc3) = if(init) (h_2, ctx_1, DOMNodeList.getInstance(cfg).get)
                                    else cfgAddrToLoc(h_2, ctx_1, cfg.getStoreAddress(addresskey))
      val h_3 = addInstance(h_2_1, absloc3, DOMNodeList.getInsList(num_children))
      
      var children_obj = h_3(absloc3)
      
      val absobj_list : List[Obj] = absloc_list.zipWithIndex.map(
         ele => {
          val x=ele._1
          val i=ele._2
          // object update for the 'childNodes' field
          children_obj = children_obj.
                        update(NumStrSingle(i.toString),   PropValue(ObjectValue(absloc_list(i), T, T, T)))
          // set the 'parentNode' and 'offsetParent' fields of all children nodes
          // 'offsetParent' could be more precise with null
          val newObj1 = h_3(x).
                update(OtherStrSingle("parentNode"), PropValue(ObjectValue(absloc1, F, T, T))).
                update(OtherStrSingle("offsetParent"), PropValue(ObjectValue(Value(absloc1) + Value(NullTop), F, T, T)))
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
      val newElementObj=h_3(absloc1).
                         update(OtherStrSingle("childNodes"),   PropValue(ObjectValue(absloc3, F, T, T))).
                         update(OtherStrSingle("firstChild"),   PropValue(ObjectValue(absloc_list(0), F, T, T))).
                         update(OtherStrSingle("lastChild"),   PropValue(ObjectValue(absloc_list(num_children-1), F, T, T)))

      val h_4: Heap = ((absloc_list zip absobj_list).foldLeft(h_3)((_h, y) => _h.update(y._1, y._2)))

      ((h_4.update(absloc3, children_obj).update(absloc1, newElementObj), ctx_2), absloc1)
    }

  }


}
