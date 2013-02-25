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
import org.w3c.dom.DocumentType
import kr.ac.kaist.jsaf.analysis.typing.CallContext
import org.w3c.dom.html._


object DOMDocumentType {
  
  val loc_proto = newLoc("DOMDocumentTypeProto")
  
  val F = BoolFalse
  val T = BoolTrue
  
  def init(map: HeapMap) : HeapMap = {
    val loc_cons = newLoc("DOMDocumentTypeConst")

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
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(DOMNode.get_prototype, F, F, F)))
   
    val global_object = map(GlobalLoc).update(AbsString.alpha("DocumentType"), 
                                       PropValue(ObjectValue(loc_cons, T, T, T)))   
    
    val newmap = map + (GlobalLoc -> global_object) + (loc_proto -> obj_proto) + (loc_cons -> obj_con) 
    newmap
  }
    
  def instantiate(map: HeapMap, node: Node) : (HeapMap, Loc) = {
    val loc_instance = newLoc("DOMDocumentTypeInstance")
    
    val doctypenode = node.asInstanceOf[DocumentType]
    val publicId = doctypenode.getPublicId
    val systemId = doctypenode.getSystemId
    val internalSubset = doctypenode.getInternalSubset
    // Instance Object
    val obj_ins = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Object"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(loc_proto, F, F, F))).
    // DOM Level 1: Read-Only properties
      update(OtherStrSingle("name"),   PropValue(ObjectValue(AbsString.alpha(doctypenode.getName), F, T, T))).
      // TODO : set the 'entities' field and 'notations' field to NamedNodeMap
      update(OtherStrSingle("entities"),   PropValue(ObjectValue(AbsString.alpha(""), F, T, T))).
      update(OtherStrSingle("notations"),   PropValue(ObjectValue(AbsString.alpha(""), F, T, T))).
    // DOM Level 2: Read-Only properties
      update(OtherStrSingle("publicId"),   PropValue(ObjectValue(AbsString.alpha(if(publicId!=null) publicId else ""), F, T, T))).
      update(OtherStrSingle("systemId"),   PropValue(ObjectValue(AbsString.alpha(if(systemId!=null) systemId else ""), F, T, T))).
      update(OtherStrSingle("internalSubset"),   PropValue(if(internalSubset!=null) ObjectValue(AbsString.alpha(internalSubset), F, T, T)
                                                           else ObjectValue(PValue(NullTop), F, T, T)))
  
    // This object has all properties and functions of the Node object 
    val obj_ins1 = DOMNode.update_Node(obj_ins, node) 
    (map + (loc_instance -> obj_ins1), loc_instance)
  }
}
