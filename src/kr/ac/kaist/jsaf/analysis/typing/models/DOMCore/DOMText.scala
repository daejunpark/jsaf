/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMCore

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
import kr.ac.kaist.jsaf.analysis.typing.CallContext


object DOMText {
  
  val loc_proto = newLoc("DOMTextProto")
  
  val F = BoolFalse
  val T = BoolTrue
  
  def init(map: HeapMap) : HeapMap = {
    val loc_cons = newLoc("DOMTextConst")

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
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(ObjProtoLoc, F, F, F)))
   
    val global_object = map(GlobalLoc).update(AbsString.alpha("Text"), 
                                       PropValue(ObjectValue(loc_cons, T, F, T)))   
    
    val newmap = map + (GlobalLoc -> global_object) + (loc_proto -> obj_proto) + (loc_cons -> obj_con)
    newmap
  }
    
  def instantiate(map: HeapMap, node: Node) : (HeapMap, Loc) = {
    val loc_instance = newLoc("DOMTextInstance")
    
    // Instance Object
    val obj_ins = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Object"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(loc_proto, F, F, F)))
  
    // This object has all properties and functions of the Node object 
    val obj_ins1 = DOMNode.update_Node(obj_ins, node) 
    (map + (loc_instance -> obj_ins1), loc_instance) 
  }
}
