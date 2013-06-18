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
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue

object TIZENStatusNotification extends Tizen {
  private val name = "StatusNotification"
  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")
  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("TIZENtizen.TIZENStatusNotification.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("id", AbsConstValue(PropValue(Value(UndefTop)))),
    ("type", AbsConstValue(PropValue(Value(UndefTop)))),
    ("postedName", AbsConstValue(PropValue(Value(UndefTop)))),
    ("title", AbsConstValue(PropValue(Value(UndefTop)))),
    ("content", AbsConstValue(PropValue(Value(UndefTop)))),
    ("statusType", AbsConstValue(PropValue(Value(UndefTop)))),
    ("iconPath", AbsConstValue(PropValue(Value(UndefTop)))),
    ("subIconPath", AbsConstValue(PropValue(Value(UndefTop)))),
    ("number", AbsConstValue(PropValue(Value(UndefTop)))),
    ("detailInfo", AbsConstValue(PropValue(Value(UndefTop)))),
    ("backgroundImagePath", AbsConstValue(PropValue(Value(UndefTop)))),
    ("thumbnails", AbsConstValue(PropValue(Value(UndefTop)))),
    ("soundPath", AbsConstValue(PropValue(Value(UndefTop)))),
    ("vibration", AbsConstValue(PropValue(Value(UndefTop)))),
    ("appControl", AbsConstValue(PropValue(Value(UndefTop)))),
    ("appId", AbsConstValue(PropValue(Value(UndefTop)))),
    ("progressType", AbsConstValue(PropValue(Value(UndefTop)))),
    ("progressValue", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue)))
  )

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto)
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("TIZENtizen.TIZENStatusNotification.constructor" -> ())  */
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}