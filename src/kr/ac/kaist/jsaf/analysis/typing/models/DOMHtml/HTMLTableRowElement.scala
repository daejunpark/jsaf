/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.ControlPoint
import org.w3c.dom.Node
import org.w3c.dom.Element
import kr.ac.kaist.jsaf.analysis.typing.Helper
import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc

object HTMLTableRowElement extends DOM {
  private val name = "HTMLTableRowElement"

  /* predefined locatoins */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor */
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("length", AbsConstValue(PropValue(ObjectValue(Value(AbsNumber.alpha(0)), F, F, F)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F))))
  )
  
  /* prorotype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(HTMLElement.loc_proto), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("insertCell", AbsBuiltinFunc("HTMLTableRowElement.insertCell", 1)),
    ("deleteCell", AbsBuiltinFunc("HTMLTableRowElement.deleteCell", 1))
  )

  /* global */
  private val prop_global: List[(String, AbsProperty)] = List(
    (name, AbsConstValue(PropValue(ObjectValue(loc_cons, T, F, T))))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto), (GlobalLoc, prop_global)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //TODO: not yet implemented
      //case "HTMLTableRowElement.insertCell" => ((h,ctx),(he,ctxe))
      //case "HTMLTableRowElement.deleteCell" => ((h,ctx),(he,ctxe))
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //TODO: not yet implemented
      //case "HTMLTableRowElement.insertCell" => ((h,ctx),(he,ctxe))
      //case "HTMLTableRowElement.deleteCell" => ((h,ctx),(he,ctxe))
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      //TODO: not yet implemented
      //case "HTMLTableRowElement.insertCell" => ((h,ctx),(he,ctxe))
      //case "HTMLTableRowElement.deleteCell" => ((h,ctx),(he,ctxe))
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      //TODO: not yet implemented
      //case "HTMLTableRowElement.insertCell" => ((h,ctx),(he,ctxe))
      //case "HTMLTableRowElement.deleteCell" => ((h,ctx),(he,ctxe))
    )
  }

  /* instance */
  override def getInstance(cfg: CFG): Option[Loc] = Some(addrToLoc(cfg.newProgramAddr, Recent))
  /* list of properties in the instance object */
  override def getInsList(node: Node): List[(String, PropValue)] = node match {
    case e: Element => 
      // This object has all properties of the HTMLElement object 
      HTMLElement.getInsList(node) ++ List(
      ("@class",    PropValue(AbsString.alpha("Object"))),
      ("@proto",    PropValue(ObjectValue(loc_proto, F, F, F))),
      ("@extensible", PropValue(BoolTrue)),
      // DOM Level 1
      ("align", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("align")), T, T, T))),
      ("bgColor", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("bgColor")), T, T, T))),
      ("ch", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("ch")), T, T, T))),
      ("chOff", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("chOff")), T, T, T))),
      ("vAlign", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("vAlign")), T, T, T))),
      // Modified in DOM Level 2
      ("rowIndex",     PropValue(ObjectValue(Helper.toNumber(PValue(AbsString.alpha(e.getAttribute("rowIndex")))), T, T, T))),
      ("sectionRowIndex",     PropValue(ObjectValue(Helper.toNumber(PValue(AbsString.alpha(e.getAttribute("sectionRowIndex")))), T, T, T))))
      // TODO: 'cells' in DOM Level 1
    case _ => {
      System.err.println("* Warning: " + node.getNodeName + " cannot have instance objects.")
      List()
    }
  }
   
  def getInsList(align: PropValue, bgColor: PropValue, ch: PropValue, chOff: PropValue, vAlign: PropValue,
                 rowIndex: PropValue, sectionRowIndex: PropValue): List[(String, PropValue)] = List(
    ("@class",    PropValue(AbsString.alpha("Object"))),
    ("@proto",    PropValue(ObjectValue(loc_proto, F, F, F))),
    ("@extensible", PropValue(BoolTrue)),
    // DOM Level 1
    ("align", align),
    ("bgColor", bgColor),
    ("ch", ch),
    ("chOff", chOff),
    ("vAlign", vAlign),
    ("rowIndex", rowIndex),
    ("sectionRowIndex", sectionRowIndex)
  )
  
  override def default_getInsList(): List[(String, PropValue)] = {    
    val align = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val bgColor = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val ch = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val chOff = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val vAlign = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val rowIndex = PropValue(ObjectValue(NumTop, T, T, T))
    val sectionRowIndex = PropValue(ObjectValue(NumTop, T, T, T))
    // This object has all properties of the HTMLElement object 
    HTMLElement.default_getInsList ::: 
      getInsList(align, bgColor, ch, chOff, vAlign, rowIndex, sectionRowIndex)
  }


}
