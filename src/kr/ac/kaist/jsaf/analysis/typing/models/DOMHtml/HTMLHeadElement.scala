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


object HTMLHeadElement {
  
  val loc_proto = newLoc("HTMLHeadProto")
  
  val F = BoolFalse
  val T = BoolTrue
  
  def init(map: HeapMap) : HeapMap = {
    val loc_cons = newLoc("HTMLHeadConst")

    // Constructor Object
    val obj_con = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Function"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(ObjProtoLoc, F, F, F))).
      update(OtherStrSingle("length"),    PropValue(ObjectValue(AbsNumber.alpha(0), F, F, F))).
      update(OtherStrSingle("prototype"), PropValue(ObjectValue(loc_proto, F, F, F)))
     
    // Prototype Object
    val obj_proto = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Object"))).
      // TODO : changhe the prototype to CharacterData object
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(HTMLElement.get_prototype, F, F, F)))
   
    val global_object = map(GlobalLoc).update(AbsString.alpha("HTMLHeadElement"), 
                                       PropValue(ObjectValue(loc_cons, T, T, T)))   
    
    val newmap = map + (GlobalLoc -> global_object) + (loc_proto -> obj_proto) + (loc_cons -> obj_con) 
    newmap
  }
  
  def instantiate(map: HeapMap, node: Node) : (HeapMap, Loc) = {
    val loc_instance = newLoc("HTMLHeadInstance")
    
    // Instance Object
    val elementnode = node.asInstanceOf[Element]
    val obj_ins = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Object"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(loc_proto, F, F, F))).
    // DOM Level 1
      update(OtherStrSingle("profile"),    PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("profile")), T, T, T)))
  
    // This object has all properties and functions of the HTMLElement object 
    val obj_ins1 = HTMLElement.update_Element(obj_ins, node) 
 
    (map + (loc_instance -> obj_ins1), loc_instance)
  } 
}
