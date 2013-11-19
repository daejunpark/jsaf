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

object JQueryAjax extends ModelData {

  private val prop_const: List[(String, AbsProperty)] = List(
    ("ajax",           AbsBuiltinFunc("jQuery.ajax", 2)),
    ("ajaxPrefilter",  AbsBuiltinFunc("jQuery.ajaxPrefilter", 2)),
    ("ajaxSetup",      AbsBuiltinFunc("jQuery.ajaxSetup", 2)),
    ("ajaxTransport",  AbsBuiltinFunc("jQuery.ajaxTransport", 2)),
    ("get",            AbsBuiltinFunc("jQuery.get", 4)),
    ("getJSON",        AbsBuiltinFunc("jQuery.getJSON", 3)),
    ("getScript",      AbsBuiltinFunc("jQuery.getScript", 2)),
    ("param",          AbsBuiltinFunc("jQuery.param", 2)),
    ("post",           AbsBuiltinFunc("jQuery.post", 4))
  )

  private val prop_proto: List[(String, AbsProperty)] = List(
    ("ajaxComplete",   AbsBuiltinFunc("jQuery.prototype.ajaxComplete", 1)),
    ("ajaxError",      AbsBuiltinFunc("jQuery.prototype.ajaxError", 1)),
    ("ajaxSend",       AbsBuiltinFunc("jQuery.prototype.ajaxSend", 1)),
    ("ajaxStart",      AbsBuiltinFunc("jQuery.prototype.ajaxStart", 1)),
    ("ajaxStop",       AbsBuiltinFunc("jQuery.prototype.ajaxStop", 1)),
    ("ajaxSuccess",    AbsBuiltinFunc("jQuery.prototype.ajaxSuccess", 1)),
    // event load ("load",           AbsBuiltinFunc("jQuery.prototype.load", 3)),
    ("serialize",      AbsBuiltinFunc("jQuery.prototype.serialize", 0)),
    ("serializeArray", AbsBuiltinFunc("jQuery.prototype.serializeArray", 0))
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
