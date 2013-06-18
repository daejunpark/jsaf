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

object TIZENNFCAdapter extends Tizen {
  private val name = "NFCAdapter"
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
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("powered", AbsConstValue(PropValue(Value(BoolTop)))),
    ("seType", AbsConstValue(PropValue(Value(StrTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("setPowered", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.setPowered",3)),
    ("setCardEmulation", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.setCardEmulation",3)),
    ("setCardEmulationChangeListener", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.setCardEmulationChangeListener",1)),
    ("unsetCardEmulationChangeListener", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.unsetCardEmulationChangeListener",0)),
    ("setTagListener", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.setTagListener",2)),
    ("setPeerListener", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.setPeerListener",1)),
    ("unsetTagListener", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.unsetTagListener",0)),
    ("unsetPeerListener", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.unsetPeerListener",0)),
    ("getCachedMessage", AbsBuiltinFunc("TIZENtizen.TIZENNFCAdapter.getCachedMessage",0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(

    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}