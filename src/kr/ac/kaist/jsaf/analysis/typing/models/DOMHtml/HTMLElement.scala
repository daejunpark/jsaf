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
import kr.ac.kaist.jsaf.analysis.typing.CallContext
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore.DOMNode
import org.w3c.dom.Element

object HTMLElement {
  
  val loc_proto = newLoc("HTMLElementProto")
  
  val F = BoolFalse
  val T = BoolTrue

  def get_prototype() : Loc = loc_proto
   /**
   * Code for |> operator.
   */
  case class ToPipe[A](a: A) {
    def |>[B](f: A => B) = f(a)
  }
  implicit def convert[A](s: A) = ToPipe(s)

 
  def init(map: HeapMap) : HeapMap = {
    val loc_cons = newLoc("HTMLElementConst")

    // Constructor Object
    val obj_con = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Function"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(ObjProtoLoc, F, F, F))).
      update(OtherStrSingle("length"),    PropValue(ObjectValue(AbsNumber.alpha(0), F, F, F))).
      update(OtherStrSingle("prototype"), PropValue(ObjectValue(loc_proto, F, F, F)))
     
    // Prototype Object
    val obj_proto = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Object"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(ObjProtoLoc, F, F, F)))
         
    val global_object = map(GlobalLoc).update(AbsString.alpha("HTMLElement"), 
                                       PropValue(ObjectValue(loc_cons, T, T, T)))   
    
    val newmap = map + (GlobalLoc -> global_object) + (loc_proto -> obj_proto) + (loc_cons -> obj_con)
    newmap |> HTMLDocument.init |>
           HTMLHtmlElement.init |>
           HTMLLinkElement.init |>
           HTMLTitleElement.init |>
           HTMLMetaElement.init |>
           HTMLStyleElement.init |>
           HTMLBodyElement.init |>
           HTMLDivElement.init |>
           HTMLParagraphElement.init |>
           HTMLHeadingElement.init |>
           HTMLScriptElement.init 
  }
  
  def instantiate(map: HeapMap, node: Node) : (HeapMap, Loc) = {
    val loc_instance = newLoc("HTMLElementInstance")
    // Instance Object
    val elementnode = node.asInstanceOf[Element]
    System.out.println("node id start:" + elementnode.getAttribute("id") + "end")
    val obj_ins = ObjEmpty.
      update(OtherStrSingle("@class"),    PropValue(AbsString.alpha("Object"))).
      update(OtherStrSingle("@proto"),    PropValue(ObjectValue(loc_proto, F, F, F))).
    // DOM Level 1
      update(OtherStrSingle("id"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("id")), T, T, T))).
      update(OtherStrSingle("title"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("title")), T, T, T))).
      update(OtherStrSingle("lang"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("lang'")), T, T, T))).
      update(OtherStrSingle("dir"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("dir")), T, T, T))).
      update(OtherStrSingle("className"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("className")), T, T, T)))
    // This object has all properties and functions of the Node object 
    val obj_ins1 = DOMNode.update_Node(obj_ins, node) 

    (map + (loc_instance -> obj_ins), loc_instance)
  }
  // update the HTML element with the properties of the Element object
  def update_Element(obj : Obj, node : Node) : Obj = {
    // This object has all properties and functions of the Node object 
    val obj_ins = DOMNode.update_Node(obj, node) 
    val elementnode = node.asInstanceOf[Element]
    obj_ins.
    // DOM Level 1
      update(OtherStrSingle("id"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("id")), T, T, T))).
      update(OtherStrSingle("title"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("title")), T, T, T))).
      update(OtherStrSingle("lang"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("lang'")), T, T, T))).
      update(OtherStrSingle("dir"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("dir")), T, T, T))).
      update(OtherStrSingle("className"),   PropValue(ObjectValue(AbsString.alpha(elementnode.getAttribute("className")), T, T, T)))
  }
}
