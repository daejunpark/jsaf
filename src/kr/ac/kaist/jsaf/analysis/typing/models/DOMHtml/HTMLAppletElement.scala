/*******************************************************************************
    Copyright (c) 2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml

import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import org.w3c.dom.Node
import org.w3c.dom.Element
import kr.ac.kaist.jsaf.analysis.typing.Helper
import kr.ac.kaist.jsaf.analysis.cfg.CFG
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.AddressManager._

object HTMLAppletElement extends DOM {
  private val name = "HTMLAppletElement"

  /* predefined locatoins */
  val loc_cons = newSystemRecentLoc(name + "Cons")
  val loc_proto = newSystemRecentLoc(name + "Proto")

  /* constructor or object*/
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

  /* semantics */
  // no function
     
  /* instance */
  override def getInstance(cfg: CFG): Option[Loc] = Some(newRecentLoc())
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
      ("alt", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("alt")), T, T, T))),
      ("archive", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("archive")), T, T, T))),
      ("code", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("code")), T, T, T))),
      ("codeBase", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("codeBase")), T, T, T))),
      ("height", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("height")), T, T, T))),
      ("name", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("name")), T, T, T))),
      ("width", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("width")), T, T, T))),
      // Modified in DOM Level 2
      ("hspace",  PropValue(ObjectValue(Helper.toNumber(PValue(AbsString.alpha(e.getAttribute("hspace")))), T, T, T))),
      ("object", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("object")), T, T, T))),
      ("vspace",  PropValue(ObjectValue(Helper.toNumber(PValue(AbsString.alpha(e.getAttribute("vspace")))), T, T, T))))
    case _ => {
      System.err.println("* Warning: " + node.getNodeName + " cannot have instance objects.")
      List()
    }
  }
 
  def getInsList(align: PropValue, alt: PropValue, archive: PropValue, code: PropValue, codeBase: PropValue, 
                 height: PropValue, name: PropValue, width: PropValue, hspace: PropValue, oobject: PropValue, vspace: PropValue): List[(String, PropValue)] = List(
    ("@class",    PropValue(AbsString.alpha("Object"))),
    ("@proto",    PropValue(ObjectValue(loc_proto, F, F, F))),
    ("@extensible", PropValue(BoolTrue)),
    // DOM Level 1
    ("align", align),
    ("alt", alt),
    ("archive", archive),
    ("code", code),
    ("codeBase", codeBase),
    ("height", height),
    ("name", name),
    ("width", width),
    // Modified in DOM Level 2
    ("hspace",  hspace),
    ("object", oobject),
    ("vspace",  vspace)
  )
  
  override def default_getInsList(): List[(String, PropValue)] = {    
    val align = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val alt = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val archive = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val code = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val codeBase = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val height = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val name = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val width = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val hspace = PropValue(ObjectValue(NumTop, T, T, T))
    val oobject = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val vspace = PropValue(ObjectValue(NumTop, T, T, T))
    // This object has all properties of the HTMLElement object 
    HTMLElement.default_getInsList ::: 
      getInsList(align, alt, archive, code, codeBase, height, name, width, hspace, oobject, vspace)
  }
}
