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
import org.w3c.dom.Node
import org.w3c.dom.Element
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore.DOMNode
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore.DOMElement
import kr.ac.kaist.jsaf.analysis.cfg.CFG
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import scala.Some

object HTMLElement extends DOM {
  private val name = "HTMLElement"

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
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(DOMElement.loc_proto), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue)))
  )

  /* global */
  private val prop_global: List[(String, AbsProperty)] = List(
    (name, AbsConstValue(PropValue(ObjectValue(loc_cons, T, F, T))))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto), (GlobalLoc, prop_global)
  )

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

  override def getProto(): Option[Loc] = Some(loc_proto) 

  /* semantics */  
  // no function

  /* instance */
  override def getInstance(cfg: CFG): Option[Loc] = Some(addrToLoc(cfg.newProgramAddr, Recent))
  /* list of properties in the instance object */
  override def getInsList(node: Node): List[(String, PropValue)] = node match {
    case e: Element => 
      // This instance object has all properties of the Element object
      DOMElement.getInsList(node) ++ List(
      // DOM Level 1
      ("id",   PropValue(ObjectValue(AbsString.alpha(e.getAttribute("id")), T, T, T))),
      ("title",   PropValue(ObjectValue(AbsString.alpha(e.getAttribute("title")), T, T, T))),
      ("lang",   PropValue(ObjectValue(AbsString.alpha(e.getAttribute("lang'")), T, T, T))),
      ("dir",   PropValue(ObjectValue(AbsString.alpha(e.getAttribute("dir")), T, T, T))),
      ("className",   PropValue(ObjectValue(AbsString.alpha(e.getAttribute("className")), T, T, T))))
    case _ => {
      System.err.println("* Warning: " + node.getNodeName + " cannot have instance objects.")
      List()
    }
  }
  
  def getInsList(id: PropValue, title: PropValue, lang: PropValue, dir: PropValue, className: PropValue): List[(String, PropValue)] = {
    // DOM Level 1
    List(("id", id), 
    ("title", title),
    ("lang", lang), 
    ("dir", dir),
    ("className", className))
  }

  override def default_getInsList(): List[(String, PropValue)] =
    getInsList(PropValue(ObjectValue(AbsString.alpha(""), T, T, T)),
               PropValue(ObjectValue(AbsString.alpha(""), T, T, T)),
               PropValue(ObjectValue(AbsString.alpha(""), T, T, T)),
               PropValue(ObjectValue(AbsString.alpha(""), T, T, T)),
               PropValue(ObjectValue(AbsString.alpha(""), T, T, T)))

}
