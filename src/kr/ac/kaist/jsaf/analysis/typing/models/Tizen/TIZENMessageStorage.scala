/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import java.lang.InternalError

object TIZENMessageStorage extends Tizen {
  private val name = "MessageStorage"
  /* predefined locations */
  val loc_obj = newPredefLoc(name + "Obj")
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
    ("addDraftMessage", AbsBuiltinFunc("tizen.MessageStorage.addDraftMessage", 3)),
    ("findMessages", AbsBuiltinFunc("tizen.MessageStorage.findMessages", 6)),
    ("removeMessages", AbsBuiltinFunc("tizen.MessageStorage.removeMessages", 3)),
    ("updateMessages", AbsBuiltinFunc("tizen.MessageStorage.updateMessages", 3)),
    ("findConversations", AbsBuiltinFunc("tizen.MessageStorage.findConversations", 6)),
    ("removeConversations", AbsBuiltinFunc("tizen.MessageStorage.removeConversations", 3)),
    ("findFolders", AbsBuiltinFunc("tizen.MessageStorage.findFolders", 3)),
    ("addMessagesChangeListener", AbsBuiltinFunc("tizen.MessageStorage.addMessagesChangeListener", 2)),
    ("addConversationsChangeListener", AbsBuiltinFunc("tizen.MessageStorage.addConversationsChangeListener", 2)),
    ("addFoldersChangeListener", AbsBuiltinFunc("tizen.MessageStorage.addFoldersChangeListener", 2)),
    ("removeChangeListener", AbsBuiltinFunc("tizen.MessageStorage.removeChangeListener", 1))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("tizen.MessageStorage.addDraftMessage" -> ()),
      ("tizen.MessageStorage.findMessages" -> ()),
      ("tizen.MessageStorage.removeMessages" -> ()),
      ("tizen.MessageStorage.updateMessages" -> ()),
      ("tizen.MessageStorage.findConversations" -> ()),
      ("tizen.MessageStorage.removeConversations" -> ()),
      ("tizen.MessageStorage.findFolders" -> ()),
      ("tizen.MessageStorage.addMessagesChangeListener" -> ()),
      ("tizen.MessageStorage.addConversationsChangeListener" -> ()),
      ("tizen.MessageStorage.addFoldersChangeListener" -> ()),
      ("tizen.MessageStorage.removeChangeListener" -> ())*/
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}