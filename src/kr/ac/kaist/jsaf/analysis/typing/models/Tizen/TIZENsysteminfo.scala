/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.cfg.CFGExpr
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}

object TIZENsysteminfo extends Tizen {
  private val name = "systeminfo"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_systeminfo
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("getCapabilities", AbsBuiltinFunc("TIZENtizen.TIZENsysteminfo.getCapabilities",2)),
    ("getPropertyValue", AbsBuiltinFunc("TIZENtizen.TIZENsysteminfo.getPropertyValue",1)),
    ("addPropertyValueChangeListener", AbsBuiltinFunc("TIZENtizen.TIZENsysteminfo.addPropertyValueChangeListener",1)),
    ("removePropertyValueChangeListener", AbsBuiltinFunc("TIZENtizen.TIZENsysteminfo.removePropertyValueChangeListener",0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("TIZENtizen.TIZENsysteminfo.getCapabilities" -> ()),
      ("TIZENtizen.TIZENsysteminfo.getPropertyValue" -> ()),
      ("TIZENtizen.TIZENsysteminfo.addPropertyValueChangeListener" -> ()),
      ("TIZENtizen.TIZENsysteminfo.removePropertyValueChangeListener" -> ())*/
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}