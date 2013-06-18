/*******************************************************************************
    Copyright (c) 2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml5

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing.{ControlPoint, Helper, PreHelper}
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import scala.Some

// Modeled based on the HTML5 specification - W3C Candidate Recommendation 17 December 2012, Section 6.5.1.
// http://www.w3.org/TR/html5/webappapis.html#the-navigator-object  
object Navigator extends DOM {
  private val name = "Navigator"

  /* predefined locatoins */
  val loc_ins = newPredefLoc(name + "Ins")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor */
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("length", AbsConstValue(PropValue(ObjectValue(Value(AbsNumber.alpha(0)), F, F, F)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    // property
    // Navigator implements NavigatorID (Section 6.5.1.1)
    ("appName", AbsConstValue(PropValue(ObjectValue(StrTop, F, T, T)))),
    ("appVersion", AbsConstValue(PropValue(ObjectValue(StrTop, F, T, T)))),
    ("platform", AbsConstValue(PropValue(ObjectValue(StrTop, F, T, T)))),
    ("userAgent", AbsConstValue(PropValue(ObjectValue(StrTop, F, T, T)))),
    // Navigator implements NavigatorOnLine (Section 6.5.1, Section 5.7.10)
    ("onLine", AbsConstValue(PropValue(ObjectValue(BoolTop, F, T, T))))
  )

  /* prorotype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    // API
    // Navigator implements NavigatorContentUtils (Section 6.5.1.2)
    ("registerProtocolHandler",   AbsBuiltinFunc("Navigator.registerProtocolHandler", 3)),
    ("registerContentHandler",   AbsBuiltinFunc("Navigator.registerContentHandler", 3)),
    ("isProtocolHandlerRegistered",   AbsBuiltinFunc("Navigator.isProtocolHandlerRegistered", 2)),
    ("isContentHandlerRegistered",   AbsBuiltinFunc("Navigator.isContentHandlerRegistered", 2)),
    ("unregisterProtocolHandler",   AbsBuiltinFunc("Navigator.unregisterProtocolHandler", 2)),
    ("unregisterContentHandler",   AbsBuiltinFunc("Navigator.unregisterContentHandler", 2)),
    // Navigator implements NavigatorStorageUtils (Section 6.5.1.5)
    ("yieldForStorageUpdates",   AbsBuiltinFunc("Navigator.yieldForStorageUpdates", 0))
  )

  /* no constructor */
  /* initial property list */
  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_ins, prop_cons), (loc_proto, prop_proto)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //case "Navigator.registerProtocolHandler"
      //case "Navigator.registerContentHandler"
      //case "Navigator.isProtocolHandlerRegistered"
      //case "Navigator.isContentHandlerRegistered"
      //case "Navigator.unregisterProtocolHandler"
      //case "Navigator.unregisterContentHandler"
      //case "Navigator.yieldForStorageUpdates"
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //case "Navigator.registerProtocolHandler"
      //case "Navigator.registerContentHandler"
      //case "Navigator.isProtocolHandlerRegistered"
      //case "Navigator.isContentHandlerRegistered"
      //case "Navigator.unregisterProtocolHandler"
      //case "Navigator.unregisterContentHandler"
      //case "Navigator.yieldForStorageUpdates"
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      //case "Navigator.registerProtocolHandler"
      //case "Navigator.registerContentHandler"
      //case "Navigator.isProtocolHandlerRegistered"
      //case "Navigator.isContentHandlerRegistered"
      //case "Navigator.unregisterProtocolHandler"
      //case "Navigator.unregisterContentHandler"
      //case "Navigator.yieldForStorageUpdates"
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      //case "Navigator.registerProtocolHandler"
      //case "Navigator.registerContentHandler"
      //case "Navigator.isProtocolHandlerRegistered"
      //case "Navigator.isContentHandlerRegistered"
      //case "Navigator.unregisterProtocolHandler"
      //case "Navigator.unregisterContentHandler"
      //case "Navigator.yieldForStorageUpdates"
    )
  }

  /* instance */
  def getInstance(): Option[Loc] = Some (loc_ins)
}
