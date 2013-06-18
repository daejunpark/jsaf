/*******************************************************************************
    Copyright (c) 2013, S-Core, KAIST.
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


object MouseEvent extends DOM {
  private val name = "MouseEvent"

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
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F))))
  )

  /* prorotype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(UIEvent.loc_proto), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("initMouseEvent", AbsBuiltinFunc("MouseEvent.initMouseEvent", 15))
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
      //case "MouseEvent.initMouseEvent"
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //case "MouseEvent.initMouseEvent"
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      //case "MouseEvent.initMouseEvent"
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      //case "MouseEvent.initMouseEvent"
    )
  }

  /* instance */
  def getInstList(lset_target: LocSet): List[(String, PropValue)] = {
    // this object has all properties of the UIEvent object
    UIEvent.getInstList(lset_target) ++ 
    List(
      // Introduced in DOM Level 2
      ("screenX", PropValue(ObjectValue(Value(NumTop), F,T,F))),
      ("screenY", PropValue(ObjectValue(Value(NumTop), F,T,F))),
      ("clientX", PropValue(ObjectValue(Value(NumTop), F,T,F))),
      ("clientY", PropValue(ObjectValue(Value(NumTop), F,T,F))),
      ("ctrlKey", PropValue(ObjectValue(Value(BoolTop), F,T,F))),
      ("shiftKey", PropValue(ObjectValue(Value(BoolTop), F,T,F))),
      ("altKey", PropValue(ObjectValue(Value(BoolTop), F,T,F))),
      ("metaKey", PropValue(ObjectValue(Value(BoolTop), F,T,F))),
      ("button", PropValue(ObjectValue(Value(UInt), F,T,F)))
    //("relateTarget", ...) 
    )
  }
  
  val instProps = Set("screenX", "screenY", "clientX", "clientY", "ctrlKey", "shiftKey", "altKey", "metaKey", "button")
}
