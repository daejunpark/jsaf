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

object JQueryDeferred extends ModelData {
  private val prop_const: List[(String, AbsProperty)] = List(
    ("Deferred",       AbsBuiltinFunc("jQuery.Deferred", 1))
  )

  private val prop_proto: List[(String, AbsProperty)] = List(
    ("promise",         AbsBuiltinFunc("jQuery.prototype.promise", 2))
  )
  // TODO:  deferred object
  /*
  private val prop_deferred: List[(String, AbsProperty)] = List(
    ("always", AbsBuiltinFunc("jQuery.prototype.clearQueue", 1)),
    ("done",       AbsBuiltinFunc("jQuery.prototype.data", 1)),
    ("faile",    AbsBuiltinFunc("jQuery.prototype.dequeue", 1)),
    ("isRejected",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("isResolved", AbsBuiltinFunc("jQuery.prototype.removeData", 1)),
    ("notify",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("notifyWith",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("pipe",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("progress",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("promise",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("reject",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("rejectWith",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("resolve",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("resolveWith",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("state",      AbsBuiltinFunc("jQuery.prototype.queue", 2)),
    ("then",      AbsBuiltinFunc("jQuery.prototype.queue", 2))
  )
  */

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
