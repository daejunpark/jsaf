/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.cfg.FunctionId
import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap
import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{HashMap => MHashMap}
import kr.ac.kaist.jsaf.analysis.lib.{HeapTreeMap, ObjTreeMap, LocTreeSet, IntTreeSet}
import kr.ac.kaist.jsaf.analysis.typing.AddressManager._

package object domain {
  /* abstract location */
  type Loc = Int
  type Address = Int
  type RecencyTag = Int
  val Recent = 0
  val Old = 1

  // interface between two abstract domains.
  def absUndefToString(au: AbsUndef): AbsString = {
    au match {
      case UndefTop => OtherStrSingle("undefined") // AbsString.alpha("undefined")
      case _ => StrBot
    }
  }
  def absNullToString(an: AbsNull): AbsString = {
    an match {
      case NullTop => OtherStrSingle("null") // AbsString.alpha("null")
      case _ => StrBot
    }
  }

  def absBoolToString(ab: AbsBool): AbsString = {
    ab match {
      case BoolTop => OtherStr // AbsString.alpha("true") + AbsString.alpha("false")
      case BoolBot => StrBot
      case BoolTrue => OtherStrSingle("true") // AbsString.alpha("true")
      case BoolFalse => OtherStrSingle("false") // AbsString.alpha("false")
    }
  }

  def absNumberToString(an: AbsNumber): AbsString = {
    an match {
      case NumTop => NumStr
      case NumBot => StrBot
      case Infinity => NumStr
      case PosInf => NumStrSingle("Infinity")
      case NegInf => NumStrSingle("-Infinity")
      case NaN => NumStrSingle("NaN")
      case UInt => NumStr
      case NUInt => NumStr
      case UIntSingle(n) => NumStrSingle(n.toInt.toString)
      case NUIntSingle(n) =>
        if (0 == (n - n.toInt))
          AbsString.alpha(n.toInt.toString)
        else
          AbsString.alpha(n.toString)
    }
  }


  // To filter out refined location
  var posMask: Option[Int] = None 
  var negMask: Option[Int] = None 
  
  def setMaskValue(shift: Int) = {
    posMask = Some((1 << shift) - 1)
    negMask = Some((((1 << 31) - 1) | (1 << 31)) - posMask.get)
  }

  /* callsite address for global code */
  val GlobalCallsite = 0

  /* predefined locations */
  val GlobalLoc: Loc        = newSystemLoc("Global", Recent)
  val SinglePureLocalLoc: Loc = newSystemLoc("PureLocal", Recent)
  val CollapsedLoc: Loc     = newSystemLoc("Collapsed", Old)
  val ObjProtoLoc: Loc      = newSystemLoc("ObjProto", Recent)
  val FunctionProtoLoc: Loc = newSystemLoc("FunctionProto", Recent)

  val JSONObjTopLoc: Loc    = newSystemLoc("JSONObjTop", Old)
  val LibModeObjTopLoc: Loc = newSystemLoc("LibModeObjTop", Old)

  /* special location standing for Context */
  val ContextLoc: Loc       = newSystemLoc("Context", Old)

  /* DOM event TimeStamp */
  val DOMEventTimeLoc: Loc = newSystemLoc("DOMEventTime", Old)

  /* HTML lookup table */
  val IdTableLoc: Loc   = newSystemLoc("IdTable", Recent)
  val NameTableLoc: Loc = newSystemLoc("NameTable", Recent)
  val TagTableLoc: Loc  = newSystemLoc("TagTable", Recent)
  val ClassTableLoc: Loc  = newSystemLoc("ClassTable", Recent)

  /* Event table */
  val EventTargetTableLoc: Loc   = newSystemLoc("EventTargetTable", Recent)
  val EventFunctionTableLoc: Loc = newSystemLoc("EventFunctionTable", Recent)
  val EventSelectorTableLoc: Loc = newSystemLoc("EventSelectorTable", Recent)

  /* temp use */
  val TempStyleLoc: Loc  = newSystemLoc("TempStyle", Old)

  /* Tizen Callback table */
  val TizenCallbackTableLoc: Loc = newSystemLoc("TizenCallbackTable", Recent)
  val TizenCallbackArgTableLoc: Loc = newSystemLoc("TizenCallbackArgTable", Recent)

  /* Map type for Heap */
  type HeapMap = HeapTreeMap
  val HeapMapBot: HeapMap = HeapTreeMap.Empty

  /* Map type for Obj */
  type ObjMap = ObjTreeMap
  val ObjMapBot: ObjMap = ObjTreeMap.Empty

  /* Address set type */
  type AddrSet = IntTreeSet
  val AddrSetBot: AddrSet = IntTreeSet.Empty
  def AddrSet(a: Address): AddrSet = AddrSetBot + a

  /* Location set type */
  type LocSet = LocTreeSet
  val LocSetBot: LocSet = LocTreeSet.Empty
  def LocSet(l: Loc): LocSet = LocSetBot + l
  
  /* Function set type */
  type FunSet = IntTreeSet
  val FunSetBot: FunSet = IntTreeSet.Empty
  def FunSet(fid: FunctionId): FunSet = FunSetBot + fid
  
  /* singleton location sets */
  // val PureLocalSingleton = LocSet(PureLocalLoc)
  val GlobalSingleton = LocSet(GlobalLoc)
  val CollapsedSingleton = LocSet(CollapsedLoc)
  val ObjProtoSingleton = LocSet(ObjProtoLoc)
  
  /* bottom value */
  val PValueBot = PValue(UndefBot, NullBot, BoolBot, NumBot, StrBot)
    
  val ValueBot = Value(PValueBot, LocSetBot)

  val ObjectValueBot = ObjectValue(ValueBot, BoolBot, BoolBot, BoolBot)

  // null represents symbolic bottom for intersection domain of must-oldified set.
  // Actual addresses in the bottom (all possible addresses) are not needed in the analysis.
  val ContextBot = Context(LocSetBot, LocSetBot, AddrSetBot, null)
  val ContextEmpty = Context(LocSetBot, LocSetBot, AddrSetBot, AddrSetBot)

  val PropValueBot = PropValue(ObjectValueBot, ValueBot, FunSetBot)

  val HeapBot = Heap(HeapMapBot)
  
  val ObjBot: Obj = Obj(ObjMapBot.
    updated("@default_number", (PropValueBot, AbsentBot)).
    updated("@default_other", (PropValueBot, AbsentBot)))
  
  val ObjEmpty: Obj = Obj(ObjMapBot.
    updated("@default_number", (PropValueBot, AbsentTop)).
    updated("@default_other", (PropValueBot, AbsentTop)))
  
  val StateBot = State(HeapBot, ContextBot)

  val ExceptionBot = HashSet[Exception]()
  
  /* top value */
  val PValueTop = PValue(UndefTop, NullTop, BoolTop, NumTop, StrTop)

  // Pseudo top value for JSON parsing results.
  val JSONValueTop = Value(PValueTop, LocSet(JSONObjTopLoc))
  val JSONObjectValueTop = ObjectValue(JSONValueTop, BoolTop,BoolTop,BoolTop)
  val JSONObjTop = Obj(ObjMapBot.
    updated("@class", (PropValue(Value(StrTop)), AbsentTop)).
    updated("@extensible", (PropValue(Value(BoolTop)), AbsentTop)).
    updated("@proto", (PropValue(Value(PValue(NullTop))), AbsentTop)).
    updated("@default_number", (PropValue(JSONObjectValueTop), AbsentTop)).
    updated("@default_other", (PropValue(JSONObjectValueTop), AbsentTop)))

  // Pseudo top value for unknown values in library mode.
  // Should be used only when Config.libMode is turned on.
  val FIdTop = -2
  val LibModeValueTop = Value(PValueTop, LocSet(LibModeObjTopLoc))
  val LibModeObjectValueTop = ObjectValue(LibModeValueTop, BoolTop,BoolTop,BoolTop)
  val LibModeObjTop = Obj(ObjMapBot.
    updated("@class", (PropValue(Value(StrTop)), AbsentTop)).
    updated("@extensible", (PropValue(Value(BoolTop)), AbsentTop)).
    updated("@proto", (PropValue(Value(PValue(NullTop))), AbsentTop)).
    updated("@function",  (PropValue(ObjectValueBot, ValueBot, FunSet(FIdTop)), AbsentTop)).
    updated("@construct", (PropValue(ObjectValueBot, ValueBot, FunSet(FIdTop)), AbsentTop)).
    updated("@hasinstance", (PropValue(Value(NullTop)), AbsentTop)).
    updated("@default_number", (PropValue(LibModeObjectValueTop), AbsentTop)).
    updated("@default_other", (PropValue(LibModeObjectValueTop), AbsentTop)))

  val LPBot = LPSet(HashMap[Loc,Set[String]]())
  val LBot = LocSetBot
}
