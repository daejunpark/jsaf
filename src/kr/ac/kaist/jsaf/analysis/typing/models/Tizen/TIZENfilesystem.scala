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

object TIZENfilesystem extends Tizen {
  private val name = "filesystem"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_filesystem
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
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("maxPathLength", AbsConstValue(PropValue(Value(NumTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("resolve", AbsBuiltinFunc("tizen.filesystem.resolve", 4)),
    ("getStorage", AbsBuiltinFunc("tizen.filesystem.getStorage", 3)),
    ("listStorages", AbsBuiltinFunc("tizen.filesystem.listStorages", 2)),
    ("addStorageStateChangeListener", AbsBuiltinFunc("tizen.filesystem.addStorageStateChangeListener", 2)),
    ("removeStorageStateChangeListener", AbsBuiltinFunc("tizen.filesystem.removeStorageStateChangeListener", 1))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("tizen.filesystem.resolve" -> ()),
      ("tizen.filesystem.getStorage" -> ()),
      ("tizen.filesystem.listStorages" -> ()),
      ("tizen.filesystem.addStorageStateChangeListener" -> ()),
      ("tizen.filesystem.removeStorageStateChangeListener" -> ()) */
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}