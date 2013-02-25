/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml

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
import org.w3c.dom.Element
import kr.ac.kaist.jsaf.analysis.typing.CallContext
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore.DOMNode
import org.w3c.dom.html._

object HTMLLinkElement {
  
  val loc_proto = newLoc("HTMLLinkProto")
  
  val F = BoolFalse
  val T = BoolTrue
  
  def init(map: HeapMap) : HeapMap = {
    val loc_cons = newLoc("HTMLLinkConst")

    // Constructor Object
    val obj_con = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Function"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(ObjProtoLoc, F, F, F))).
      update(OtherStrSingle("length"),    PropValue(ObjectValue(AbsNumber.alpha(0), F, F, F))).
      update(OtherStrSingle("prototype"), PropValue(ObjectValue(loc_proto, F, F, F)))
     
    // Prototype Object
    val obj_proto = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Object"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(HTMLElement.get_prototype, F, F, F)))
   
    val global_object = map(GlobalLoc).update(AbsString.alpha("HTMLLinkElement"), 
                                       PropValue(ObjectValue(loc_cons, T, T, T)))   
    
    val newmap = map + (GlobalLoc -> global_object) + (loc_proto -> obj_proto) + (loc_cons -> obj_con)
    newmap
  }
  
  def instantiate(map: HeapMap, node: Node) : (HeapMap, Loc) = {
    val loc_instance = newLoc("HTMLLinkInstance")
    
    // Instance Object
    var elementnode = node.asInstanceOf[Element]
    val obj_ins = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Object"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(loc_proto, F, F, F))).
    // DOM Level 1
      update(OtherStrSingle("disabled"),   PropValue(ObjectValue((if(elementnode.getAttribute("disabled")=="true") T else F), T, T, T))).
      update(OtherStrSingle("charset"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("charset")), T, T, T))).
      update(OtherStrSingle("href"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("href")), T, T, T))).
      update(OtherStrSingle("hreflang"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("hreflang")), T, T, T))).
      update(OtherStrSingle("media"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("media")), T, T, T))).
      update(OtherStrSingle("rel"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("rel")), T, T, T))).
      update(OtherStrSingle("rev"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("rev")), T, T, T))).
      update(OtherStrSingle("target"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("target")), T, T, T))).
      update(OtherStrSingle("type"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("type")), T, T, T)))
  
    // This object has all properties and functions of the Element object 
    val obj_ins1 = HTMLElement.update_Element(obj_ins, node) 
    (map + (loc_instance -> obj_ins1), loc_instance)  
  }
}
