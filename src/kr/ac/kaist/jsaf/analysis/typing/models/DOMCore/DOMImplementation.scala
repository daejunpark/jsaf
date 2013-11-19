/*******************************************************************************
    Copyright (c) 2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMCore

import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.AddressManager._

object DOMImplementation extends DOM {
  private val name = "DOMImplementation"

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
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("hasFeature",         AbsBuiltinFunc("DOMImplementation.hasFeature", 2)),
    ("createDocumentType", AbsBuiltinFunc("DOMImplementation.createDocumentType", 3)),
    ("createDocument",     AbsBuiltinFunc("DOMImplementation.createDocument", 3)),
    ("getFeature",         AbsBuiltinFunc("DOMImplementation.getFeature", 2))
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
      //case "DOMImplementation.hasFeature"     => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.createDocumentType" => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.createDocument" => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.getFeature" => ((h,ctx),(he,ctxe))
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //TODO: not yet implemented
      //case "DOMImplementation.hasFeature"     => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.createDocumentType" => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.createDocument" => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.getFeature" => ((h,ctx),(he,ctxe))
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      //TODO: not yet implemented
      //case "DOMImplementation.hasFeature"     => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.createDocumentType" => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.createDocument" => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.getFeature" => ((h,ctx),(he,ctxe))
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      //TODO: not yet implemented
      //case "DOMImplementation.hasFeature"     => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.createDocumentType" => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.createDocument" => ((h,ctx),(he,ctxe))
      //case "DOMImplementation.getFeature" => ((h,ctx),(he,ctxe))
    )
  }

  /* instance */
  //def instantiate() = Unit // not yet implemented
  // intance of DOMImplementation should have no property
}
