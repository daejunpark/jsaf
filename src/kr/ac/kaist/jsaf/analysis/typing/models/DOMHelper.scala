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
import kr.ac.kaist.jsaf.analysis.typing.{InternalError, Operator, Helper}
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.domain.OtherStrSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.NumStrSingle
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.domain.OtherStrSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.NumStrSingle
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.domain.OtherStrSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.NumStrSingle

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
    case _ =>
      System.err.println("* Warning: " + tagname + " - not a valid tag name.")
      List()
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
    attr=="scroll" || attr=="mouseleave" ||attr=="mouseenter"
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
    // for jQuery
    attr=="error" || attr == "focusin" || attr =="focusout"
  }

  def isReadyEventProperty(attr: String): Boolean = {
    attr == "DOMContentLoaded" || attr == "onreadystatechange"
  }


  def findByAttr(h: Heap, l_root: Loc, attr_name: String, s: AbsString, contain: Boolean): LocSet = {
    def search(lset_visited: LocSet, l_this: Loc): LocSet = {
      if (!lset_visited.contains(l_this)) {
        // get attribute value
        val v_attr = DOMHelper.getAttribute(h, LocSet(l_this),  AbsString.alpha(attr_name))
        // next elements
        val lset_children = Helper.Proto(h, l_this, AbsString.alpha("childNodes"))._2.foldLeft(LocSetBot)((lset, l_n) =>
          lset ++ Helper.Proto(h, l_n, NumStr)._2)
        if ((!contain && v_attr._1._5 <= s) || (contain && BoolTrue <= v_attr._1._5.contains(s)))
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
    def search(lset_visited: LocSet, l_this: Loc): LocSet = {
      if (!lset_visited.contains(l_this)) {
        // get property value
        val v_attr = Helper.Proto(h, l_this, AbsString.alpha(prop_name))
        // next elements
        val lset_children = Helper.Proto(h, l_this, AbsString.alpha("childNodes"))._2.foldLeft(LocSetBot)((lset, l_n) =>
          lset ++ Helper.Proto(h, l_n, NumStr)._2)
        if (v_attr._1._5 <= s || (tag && AbsString.alpha("*") <= s))
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
    def search(lset_visited: LocSet, l_this: Loc): LocSet = {
      if (!lset_visited.contains(l_this)) {
        // get property value
        val v_attr = Helper.Proto(h, l_this, AbsString.alpha(prop_name))
        // next elements
        val lset_children = Helper.Proto(h, l_this, AbsString.alpha("nextSibling"))._2
        if (v_attr._1._5 <= s || (tag && AbsString.alpha("*") <= s))
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
    def search(lset_visited: LocSet, l_this: Loc): LocSet = {
      if (!lset_visited.contains(l_this)) {
        // next elements
        val lset_parent = Helper.Proto(h, l_this, AbsString.alpha("parentNode"))._2
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


  /* simple, partial implementation of querySelectorAll */
  def querySelectorAll(h: Heap, l_root: Loc, s_selector: String): LocSet = {
    val tokens = reg_token.findAllIn(s_selector.trim).toList

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
              case reg_tag(name) =>
                val lset_next = findByTag(h, HTMLDocument.GlobalDocumentLoc, AbsString.alpha(token))
                iter(tail, lset_next, Some(token))
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
  println("return: " + lset_ret)
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
          HTMLTopElement.getInsLoc
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

  def appendChild(h: Heap, lset_target: LocSet, lset_child: LocSet): Heap = {
    if (!lset_target.isEmpty && !lset_child.isEmpty) {
      val h_1 = lset_target.foldLeft(h)((hh, l_node) => {
        /* current childNodes */
        val lset_ns = Helper.Proto(hh, l_node, AbsString.alpha("childNodes"))._2
        val h_append = lset_ns.foldLeft(hh)((hhh, l_ns) => {
          /* length of current childNodes */
          val n_len = Operator.ToUInt32(Helper.Proto(hhh, l_ns, AbsString.alpha("length")))
          n_len match {
            case UIntSingle(n) =>
              /* childNodes[length] := new_child */
              val hhh_1 = Helper.PropStore(hhh, l_ns, AbsString.alpha(n.toInt.toString), Value(lset_child))
              /* childNodes["length"] := length + 1 */
              Helper.PropStore(hhh_1, l_ns, AbsString.alpha("length"), Value(AbsNumber.alpha(n+1)))
            case NumTop | UInt =>
              Helper.PropStore(hhh, l_ns, NumStr, Value(lset_child))
            case _ => hhh /* exception ?? */
          }
        })
        val o_target = h_append(l_node).update("lastChild", PropValue(ObjectValue(Value(lset_child),F,T,F)))
        h_append.update(l_node, o_target)
      })
      h_1
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
        lset_ns.foldLeft(h1)((h2, l_ns) => {
          val n_len = Operator.ToUInt32(Helper.Proto(h, l_ns, AbsString.alpha("length")))
          n_len match {
            case UIntSingle(n) =>
              val n_index = (0 until n.toInt).indexWhere((i) => {
                BoolTrue <= Operator.bopSEq(Helper.Proto(h2, l_ns, AbsString.alpha(i.toString)), Value(lset_child))._1._3
              })
              if (n_index < 0)
                h2
              else {
                val hhh_1 = Helper.Delete(h2, l_ns, AbsString.alpha(n_index.toString))._1
                val hhh_2 = (n_index+1 until n.toInt).foldLeft(hhh_1)((_h, i) => {
                  val v_next = Helper.Proto(_h, l_ns,  AbsString.alpha(i.toString))
                  val _h1 = Helper.Delete(_h, l_ns, AbsString.alpha(i.toString))._1
                  Helper.PropStore(_h1, l_ns, AbsString.alpha((i-1).toString), v_next)
                })
                // decrease the length of childNodes by 1
                Helper.PropStore(hhh_2, l_ns, AbsString.alpha("length"), Value(AbsNumber.alpha(n - 1)))
              }

            case NumTop | UInt =>
              val b_eq = Operator.bopSEq(Helper.Proto(h2, l_ns, NumStr), Value(lset_child))._1._3
              val h2_1 =
                if (BoolTrue <= b_eq) Helper.Delete(h2, l_ns, NumStr)._1
                else HeapBot
              val h2_2 =
                if (BoolFalse <= b_eq) h2
                else HeapBot
              h2_1 + h2_2
            case _ => h2 /* exception ?? */
          }
        })
      })
      h_1
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

  def setAttribute(h: Heap, lset_elem: LocSet ,l_attr: Loc, l_text: Loc, l_child1: Loc, l_child2: Loc, attr_name:AbsString, attr_val: AbsString): Heap = {
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
      h_5
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
}