/*******************************************************************************
    Copyright (c) 2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMEvent

import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.ControlPoint
import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue

// Modeled based on W3C DOM Level 3 Events
object KeyboardEvent extends DOM {
  private val name = "KeyboardEvent"

  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor */
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("length", AbsConstValue(PropValue(ObjectValue(Value(AbsNumber.alpha(0)), F, F, F)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("DOM_KEY_LOCATION_STANDARD",       AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(0), F, T, T)))),
    ("DOM_KEY_LOCATION_LEFT",  AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(1), F, T, T)))),
    ("DOM_KEY_LOCATION_RIGHT", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(2), F, T, T)))),
    ("DOM_KEY_LOCATION_NUMPAD", AbsConstValue(PropValue(ObjectValue(AbsNumber.alpha(3), F, T, T))))
  )

  /* prorotype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(UIEvent.loc_proto), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("getModifierState", AbsBuiltinFunc("KeyboardEvent.getModifierState", 1)),
    ("initKeyboardEvent", AbsBuiltinFunc("KeyboardEvent.initKeyboardEvent", 7)),
    ("initKeyboardEventNS", AbsBuiltinFunc("KeyboardEvent.initKeyboardEventNS", 8))
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
      //case "KeyboardEvent.getModifierState"
      //case "KeyboardEvent.initKeyboardEvent"
      //case "KeyboardEvent.initKeyboardEventNS"
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //case "KeyboardEvent.getModifierState"
      //case "KeyboardEvent.initKeyboardEvent"
      //case "KeyboardEvent.initKeyboardEventNS"
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      //case "KeyboardEvent.getModifierState"
      //case "KeyboardEvent.initKeyboardEvent"
      //case "KeyboardEvent.initKeyboardEventNS"
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      //case "KeyboardEvent.getModifierState"
      //case "KeyboardEvent.initKeyboardEvent"
      //case "KeyboardEvent.initKeyboardEventNS"
    )
  }

  /* instance */
  def getInstList(lset_target: LocSet): List[(String, PropValue)] = {
    // this object has all properties of the UIEvent object
    UIEvent.getInstList(lset_target) ++ 
    List(
      ("keyIdentifier", PropValue(ObjectValue(Value(StrTop), F,T,F))),
      ("keyLocation", PropValue(ObjectValue(Value(UInt), F,T,F))),
      ("ctrlKey", PropValue(ObjectValue(Value(BoolTop), F,T,F))),
      ("shiftKey", PropValue(ObjectValue(Value(BoolTop), F,T,F))),
      ("altKey", PropValue(ObjectValue(Value(BoolTop), F,T,F))),
      ("metaKey", PropValue(ObjectValue(Value(BoolTop), F,T,F)))
    )
  }

  val instProps = Set("keyIdentifier", "keyLocation", "ctrlKey", "shiftKey", "altKey", "metaKey")
}
