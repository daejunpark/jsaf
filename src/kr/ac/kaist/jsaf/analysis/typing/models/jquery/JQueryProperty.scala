/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.models.jquery

import kr.ac.kaist.jsaf.analysis.typing.AddressManager._

import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.{AccessHelper=>AH}

object JQueryProperty extends ModelData {

  // TODO: browser, fx, support object ??

  private val prop_const: List[(String, AbsProperty)] = List(
    ("browser",  AbsConstValue(PropValue(ObjectValue(NullTop, F, F, F)))),
    ("fx",       AbsConstValue(PropValue(ObjectValue(NullTop, F, F, F)))),
    ("support",  AbsConstValue(PropValue(ObjectValue(NullTop, F, F, F))))
  )

  private val prop_proto: List[(String, AbsProperty)] = List(
    ("jquery", AbsConstValue(PropValue(ObjectValue(AbsString.alpha("1.8.2"), F, F, F))))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (JQuery.ConstLoc, prop_const), (JQuery.ProtoLoc, prop_proto)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map()
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map()
  }
}
