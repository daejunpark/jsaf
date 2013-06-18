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
import kr.ac.kaist.jsaf.analysis.typing.Helper
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import scala.Some

// Modeled based on the HTML5 specification - W3C Candidate Recommendation 17 December 2012, Section 5.5.3.
// http://www.w3.org/TR/html5/browsers.html#dom-location 
object DOMLocation extends DOM {
  private val name = "Location"

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
    ("href", AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
    // URL decomposition IDL attributes
    ("protocol", AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
    ("host", AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
    ("hostname", AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
    ("port", AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
    ("pathname", AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
    ("search", AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T)))),
    ("hash", AbsConstValue(PropValue(ObjectValue(StrTop, T, T, T))))
  )

  /* prorotype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(ObjProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    // API
    ("assign",    AbsBuiltinFunc("Location.assign", 1)),
    ("replace",   AbsBuiltinFunc("Location.replace", 1)),
    ("reload",   AbsBuiltinFunc("Location.reload", 0))
  )

  /* initial property list */
  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_ins, prop_cons), (loc_proto, prop_proto)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //case "Location.assign"
      //case "Location.replace"
      //case "Location.reload"
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //case "Location.assign"
      //case "Location.replace"
      //case "Location.reload"
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      //case "Location.assign"
      //case "Location.replace"
      //case "Location.reload"
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      //case "Location.assign"
      //case "Location.replace"
      //case "Location.reload"
    )
  }


  /* instance */
  def getInstance(): Option[Loc] = Some (loc_ins)


}
