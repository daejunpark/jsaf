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
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore.DOMException
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml5._
import kr.ac.kaist.jsaf.analysis.typing.Helper

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
    attr=="mouseover" || attr=="mousemove" || attr=="mouseout"
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
    attr=="hashchange"
  }

  def isReadyEventProperty(attr: String): Boolean = {
    attr == "DOMContentLoaded" || attr == "onreadystatechange"
  }

  def getByAttr(h: Heap, l_root: Loc, attr: String, s : AbsString, tag: Boolean): LocSet = {
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


  def getById(h: Heap, l_root: Loc, s_id : AbsString): LocSet = {
    getByAttrFirst(h, l_root, "id", s_id, false)
  }

  def getById(h: Heap, s_id : AbsString): LocSet = {
    getByAttrFirst(h, HTMLDocument.GlobalDocumentLoc, "id", s_id, false)
  }

  def getByClass(h: Heap, l_root: Loc, s_class : AbsString): LocSet = {
    getByAttr(h, l_root, "class", s_class, false)
  }

  def getByClass(h: Heap, s_class : AbsString): LocSet = {
    getByAttr(h, HTMLDocument.GlobalDocumentLoc, "class", s_class, false)
  }

  def getByTag(h: Heap, l_root: Loc, s_tag : AbsString): LocSet = {
    /* The HTML DOM returns the tagName of an HTML element in the canonical uppercase form */
    s_tag match {
      case NumStrSingle(s) =>
        getByAttr(h, l_root, "tagName", AbsString.alpha(s.toUpperCase), true)
      case OtherStrSingle(s) =>
        getByAttr(h, l_root, "tagName", AbsString.alpha(s.toUpperCase), true)
      case _ =>
        getByAttr(h, l_root, "tagName", s_tag, true)
    }
  }

  def getByTag(h: Heap, s_tag : AbsString): LocSet = {
    getByTag(h, HTMLDocument.GlobalDocumentLoc, s_tag)
  }

  def getByName(h: Heap, l_root: Loc, s_name : AbsString): LocSet = {
    getByAttr(h, l_root, "name", s_name, false)
  }

  def getByName(h: Heap, s_name : AbsString): LocSet = {
    getByAttr(h, HTMLDocument.GlobalDocumentLoc, "name", s_name, false)
  }

  // E#id
  private val reg_id = """^([\w]*)#([\w\-]*)$""".r
  // E.class
  private val reg_class = """^([\w]*)\.([\w\-]*)$""".r
  // E[attr_name]
  private val reg_attrname = """^([\w]*)\[([\w\-]*)\]$""".r
  // E F
  private val reg_descendant = """^([\w]*)\s*([\w]*)$""".r
  // E > F , E + F, E ~ F
  private val reg_combinator = """^([\w]*)\s*([>\+~])\s*([\w]*)$""".r


  // simple, partial implementation of querySelectorAll
  // DO Not support combination
  def querySelectorAll(h: Heap, l_root: Loc, s_selector: String): LocSet = {
    val lset_1 = reg_id.unapplySeq(s_selector) match {
      case Some(list_id) =>
        val lset_tag = getByTag(h, AbsString.alpha(list_id(0)))
        lset_tag.filter((l) => AbsString.alpha(list_id(1)) <= Helper.Proto(h, l, AbsString.alpha("id"))._1._5)
      case None => LocSetBot
    }
    val lset_2 = reg_class.unapplySeq(s_selector) match {
      case Some(list_class) =>
        val lset_tag = getByTag(h, AbsString.alpha(list_class(0)))
        lset_tag.filter((l) => AbsString.alpha(list_class(1)) <= Helper.Proto(h, l, AbsString.alpha("class"))._1._5)
      case None => LocSetBot
    }
    val lset_3 = reg_attrname.unapplySeq(s_selector) match {
      case Some(list_attr) =>
        val lset_tag = getByTag(h, AbsString.alpha(list_attr(0)))
        lset_tag.filter((l) => BoolTrue <= Helper.HasOwnProperty(h, l, AbsString.alpha(list_attr(1))))
      case None => LocSetBot
    }
    val lset_4 = reg_descendant.unapplySeq(s_selector) match {
      case Some(list_des) =>
        val lset_tag = getByTag(h, AbsString.alpha(list_des(0)))
        lset_tag.foldLeft(LocSetBot)((lset, l) => lset ++ getByTag(h, l, AbsString.alpha(list_des(1))))
      case None => LocSetBot
    }
    val lset_5 = reg_combinator.unapplySeq(s_selector) match {
      case Some(list_com) =>
        val lset_tag = getByTag(h, AbsString.alpha(list_com(0)))
      // TODO
        list_com(1) match {
          case ">" => LocSetBot
          case "+" | "~" => LocSetBot
          case _ => LocSetBot
        }
      case None => LocSetBot
    }
    lset_1 ++ lset_2 ++ lset_3 ++ lset_4 ++ lset_5
  }
  def querySelectorAll(h: Heap, s_selector: String): LocSet = {
    querySelectorAll(h, HTMLDocument.GlobalDocumentLoc, s_selector)
  }
  def querySelectorAll(h: Heap, l_root: Loc, s_selector: AbsString): LocSet = {
    AbsString.concretize(s_selector) match {
      case Some(s) =>
        querySelectorAll(h, l_root, s)
      case None =>
        if (s_selector <= StrBot)
          LocSetBot
        else
          HTMLTopElement.getInsLoc
    }
  }
  def querySelectorAll(h: Heap, s_selector: AbsString): LocSet = {
    querySelectorAll(h, HTMLDocument.GlobalDocumentLoc, s_selector)
  }
}

