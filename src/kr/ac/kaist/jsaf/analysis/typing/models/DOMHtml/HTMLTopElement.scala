/*******************************************************************************
    Copyright (c) 2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import org.w3c.dom.Node
import org.w3c.dom.Element
import kr.ac.kaist.jsaf.analysis.cfg.CFG
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml5.HTMLCanvasElement
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore.DOMElement

object HTMLTopElement extends DOM {

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List()

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map()
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map()
  }

  // a set of all locations of all instance objects in a DOM tree
  // this set is updated when the DOM tree is constructed
  private var insLoc = LocSetBot

  def getInsLoc(): LocSet = insLoc
  def setInsLoc(l: Loc): Unit = {
    insLoc = insLoc + l
  }

  // this object has all properites of all html elements
  override def default_getInsList(): List[(String, PropValue)] = {
    val elementList = List(
      // DOM Html
      HTMLAnchorElement, HTMLAppletElement, HTMLAreaElement, HTMLBaseElement, HTMLBaseFontElement, HTMLBodyElement,
      HTMLBRElement, HTMLButtonElement, HTMLDirectoryElement, HTMLDivElement, HTMLDListElement, HTMLFieldSetElement, 
      HTMLFontElement, HTMLFormElement, HTMLFrameElement, HTMLFrameSetElement, HTMLHeadElement, HTMLHeadingElement, 
      HTMLHRElement, HTMLHtmlElement, HTMLIFrameElement, HTMLImageElement, HTMLInputElement, HTMLIsIndexElement, 
      HTMLLabelElement, HTMLLegendElement, HTMLLIElement, HTMLLinkElement, HTMLMapElement, HTMLMenuElement, 
      HTMLMetaElement, HTMLModElement, HTMLObjectElement, HTMLOListElement, HTMLOptGroupElement, HTMLOptionElement, 
      HTMLParagraphElement, HTMLParamElement, HTMLPreElement, HTMLQuoteElement, HTMLScriptElement, HTMLSelectElement, 
      HTMLStyleElement, HTMLTableCaptionElement, HTMLTableCellElement, HTMLTableColElement, HTMLTableElement, HTMLTableRowElement,
      HTMLTableSectionElement, HTMLTextAreaElement, HTMLTitleElement, HTMLUListElement,
      // HTML 5
      HTMLCanvasElement)
    val proplist = elementList.foldLeft[List[(String, PropValue)]](List())((propl, ele) =>
      propl:::ele.default_getInsList()
    )
    // this object has all properties in DOMElement
    DOMElement.getInsList(PropValue(ObjectValue(StrTop, F, T, T))):::proplist
  }
}
