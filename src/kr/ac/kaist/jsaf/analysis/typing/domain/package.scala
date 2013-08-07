/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.cfg.FunctionId
import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap
import scala.collection.immutable.TreeMap
import scala.collection.immutable.TreeSet
import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{HashMap => MHashMap}
import kr.ac.kaist.jsaf.analysis.lib.{HeapTreeMap, ObjTreeMap, LocTreeSet, IntTreeSet}
import kr.ac.kaist.jsaf.analysis.typing.models.builtin._
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.State
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context

package object domain {
  /* abstract location */
  type Loc = Int
  type Address = Int
  type RecencyTag = Int
  val Recent = 0
  val Old = 1

  // To filter out refined location
  var posMask: Option[Int] = None 
  var negMask: Option[Int] = None 
  
  def setMaskValue(shift: Int) = {
    posMask = Some((1 << shift) - 1)
    negMask = Some((((1 << 31) - 1) | (1 << 31)) - posMask.get)
  }

  def addrToLoc(addr: Address, recency: RecencyTag): Loc = (addr << 1) | recency
  
  def locToAddr(loc: Loc): Address = loc >> 1
  
  def oldifyLoc(loc: Address): Loc = loc | 1
  
  def isRecentLoc(loc: Address): Boolean = ((loc & 1) == Recent)
  
  def isOldLoc(loc: Address): Boolean = ((loc & 1) == Old)

  // Note that location range is -2^30 ~ (2^30 - 1)
  def compareLoc(a: Loc, b: Loc): Int = a - b
  
  /* callsite address for global code */
  val GlobalCallsite = 0
  
  /* location name */
  val predefTable: MMap[Address, String] = MHashMap()
  def registerPredefLoc(addr: Address, recency: RecencyTag, name: String): Loc = {
    predefTable(addr) = name
    addrToLoc(addr, recency)
  }

  // val builtinTable: MMap[Address, String] = MHashMap()
  // def registerBuiltinLoc(addr: Address, recency: RecencyTag, name: String): Loc = {
  //   builtinTable(addr) = name
  //   (addr, recency)
  // }
  
  def locName(loc: Loc): String = {
    val addr = locToAddr(loc)
    predefTable.get(addr) match {
      case Some(name) => name
      case None => addr.toString
    }
  }
  // builtinTable.get(addr) match {
  //       case Some(name) => name
  //       case None => addr.toString
  //     }
  //   }
  // }
  
  /* predefined locations */
  val GlobalLoc: Loc        = registerPredefLoc(-1, Recent, "Global")
  val SinglePureLocalLoc: Loc = registerPredefLoc(-2, Recent, "PureLocal")
  val CollapsedLoc: Loc     = registerPredefLoc(-3, Old, "Collapsed")
  val ObjProtoLoc: Loc      = registerPredefLoc(-5, Recent, "ObjProto")
  val FunctionProtoLoc: Loc = registerPredefLoc(-7, Recent, "FunctionProto")
  /*
  val StringProtoLoc: Loc   = BuiltinString.ProtoLoc //registerPredefLoc(-4, Recent, "StringProto")
  val NumberProtoLoc: Loc   = BuiltinNumber.ProtoLoc //registerPredefLoc(-6, Recent, "NumberProto")
  val BooleanProtoLoc: Loc  = BuiltinBoolean.ProtoLoc //registerPredefLoc(-8, Recent, "BooleanProto")
  val ArrayProtoLoc: Loc    = BuiltinArray.ProtoLoc //registerPredefLoc(-9, Recent, "ArrayProto")
  val DateProtoLoc: Loc     = BuiltinDate.ProtoLoc //registerPredefLoc(-10, Recent, "DateProto")
  
  val StringConstLoc: Loc   = BuiltinString.ConstLoc //(-11, Recent, "StringConst")
  val ObjConstLoc: Loc      = BuiltinObject.ConstLoc //registerPredefLoc(-12, Recent, "ObjConst")
  val NumberConstLoc: Loc   = BuiltinNumber.ConstLoc //registerPredefLoc(-13, Recent, "NumberConst")
  val FunctionConstLoc: Loc = BuiltinFunction.ConstLoc //registerPredefLoc(-14, Recent, "FunctionConst")
  val BooleanConstLoc: Loc  = BuiltinBoolean.ConstLoc //registerPredefLoc(-15, Recent, "BooleanConst")
  val ArrayConstLoc: Loc    = BuiltinArray.ConstLoc //registerPredefLoc(-16, Recent, "ArrayConst")
  
  /* error instance */
  val ErrLoc: Loc           = BuiltinError.ErrLoc //registerPredefLoc(-17, Old, "Err")
  val EvalErrLoc: Loc       = BuiltinError.EvalErrLoc //registerPredefLoc(-18, Old, "EvalErr")
  val RangeErrLoc: Loc      = BuiltinError.RangeErrLoc //registerPredefLoc(-19, Old, "RangeErr")
  val RefErrLoc: Loc        = BuiltinError.RefErrLoc //registerPredefLoc(-20, Old, "RefErr")
  val SyntaxErrLoc: Loc     = BuiltinError.SyntaxErrLoc //registerPredefLoc(-21, Old, "SyntaxErr")
  val TypeErrLoc: Loc       = BuiltinError.TypeErrLoc //registerPredefLoc(-22, Old, "TypeErr")
  val URIErrLoc: Loc        = BuiltinError.URIErrLoc //registerPredefLoc(-23, Old, "URIErr")
  
  /* error prototype object */
  val ErrProtoLoc:Loc       = BuiltinError.ErrProtoLoc //registerPredefLoc(-24, Recent, "ErrProto")
  val EvalErrProtoLoc:Loc   = BuiltinError.EvalErrProtoLoc //registerPredefLoc(-25, Recent, "EvalErrProto")
  val RangeErrProtoLoc: Loc = BuiltinError.RangeErrProtoLoc //registerPredefLoc(-26, Recent, "RangeErrProto")
  val RefErrProtoLoc: Loc   = BuiltinError.RefErrProtoLoc //registerPredefLoc(-27, Recent, "RefErrProto")
  val SyntaxErrProtoLoc: Loc= BuiltinError.SyntaxErrProtoLoc //registerPredefLoc(-28, Recent, "SyntaxErrProto")
  val TypeErrProtoLoc: Loc  = BuiltinError.TypeErrProtoLoc //registerPredefLoc(-29, Recent, "TypeErrProto")
  val URIErrProtoLoc: Loc   = BuiltinError.URIErrProtoLoc //registerPredefLoc(-30, Recent, "URIErrProto")*/
  
  val JSONObjTopLoc: Loc    = registerPredefLoc(-31, Old, "JSONObjTop")
  val LibModeObjTopLoc: Loc = registerPredefLoc(-32, Old, "LibModeObjTop")

  /* special location standing for Context */
  val ContextLoc: Loc       = registerPredefLoc(-33, Old, "Context")

  /* DOM error instance
  val DOMErrIndexSize: Loc             = registerPredefLoc(-34, Old, "DOMErrIndexSize")
  val DOMErrDomstringSize: Loc         = registerPredefLoc(-35, Old, "DOMErrDomstringSize")
  val DOMErrHierarchyRequest: Loc      = registerPredefLoc(-36, Old, "DOMErrHierarchyRequest")
  val DOMErrWrongDocument: Loc         = registerPredefLoc(-37, Old, "DOMErrWrongDocument")
  val DOMErrInvalidCharacter: Loc      = registerPredefLoc(-38, Old, "DOMErrInvalidCharacter")
  val DOMErrNoDataAllowed: Loc         = registerPredefLoc(-39, Old, "DOMErrNoDataAllowed")
  val DOMErrNoModificationAllowed: Loc = registerPredefLoc(-40, Old, "DOMErrNoModificationAllowed")
  val DOMErrNotFound: Loc              = registerPredefLoc(-41, Old, "DOMErrNotFound")
  val DOMErrNotSupported: Loc          = registerPredefLoc(-42, Old, "DOMErrNotSupported")
  val DOMErrInuseAttribute: Loc        = registerPredefLoc(-43, Old, "DOMErrInuseAttribute")
  val DOMErrInvalidState: Loc          = registerPredefLoc(-44, Old, "DOMErrInvalidState")
  val DOMErrSyntax: Loc                = registerPredefLoc(-45, Old, "DOMErrSyntax")
  val DOMErrInvalidModification: Loc   = registerPredefLoc(-46, Old, "DOMErrInvalidModification")
  val DOMErrNamespace: Loc             = registerPredefLoc(-47, Old, "DOMErrNamespace")
  val DOMErrInvalidAccess: Loc         = registerPredefLoc(-48, Old, "DOMErrInvalidAccess")
  val DOMErrValidation: Loc            = registerPredefLoc(-49, Old, "DOMErrValidation")
  val DOMErrTypeMismatch: Loc          = registerPredefLoc(-50, Old, "DOMErrTypeMismatch")
  */

  /* DOM event TimeStamp */
  val DOMEventTimeLoc: Loc = registerPredefLoc(-51, Old, "DOMEventTime")

  /* HTML lookup table */
  val IdTableLoc: Loc   = registerPredefLoc(-52, Recent, "IdTable")
  val NameTableLoc: Loc = registerPredefLoc(-53, Recent, "NameTable")
  val TagTableLoc: Loc  = registerPredefLoc(-54, Recent, "TagTable")
  val ClassTableLoc: Loc  = registerPredefLoc(-55, Recent, "ClassTable")

  /* Event table */
  val EventTargetTableLoc: Loc   = registerPredefLoc(-56, Recent, "EventTargetTable")
  val EventFunctionTableLoc: Loc = registerPredefLoc(-57, Recent, "EventFunctionTable")
  val EventSelectorTableLoc: Loc = registerPredefLoc(-58, Recent, "EventSelectorTable")

  /* temp use */
  val TempStyleLoc: Loc  = registerPredefLoc(-59, Old, "TempStyle")
  // CFG holds the start address of predefined location

  /* start address for PureLocal/builtin function addresses */
  // Do not change or explicitly reset this address.
  private var predefStartAddr = -60

  // String name to predefined address table to ensure same address between analysis runs.
  // This approach assumes that all predefined names are distinct.
  val reversePredefTable: MMap[String, Address] = MHashMap()
  
  def newPredefLoc(name: String): Loc = {
    reversePredefTable.get(name) match {
      case Some(addr) => addrToLoc(addr, Recent)
      case None =>
        val addr = predefStartAddr
        predefStartAddr -= 1
        reversePredefTable(name) = addr
        registerPredefLoc(addr, Recent, name)
    }
  }

  def newPreDefLoc(name: String, tag: Int): Loc = {
    reversePredefTable.get(name) match {
      case Some(addr) => addrToLoc(addr, tag)
      case None =>
        val addr = predefStartAddr
        predefStartAddr -= 1
        reversePredefTable(name) = addr
        registerPredefLoc(addr, tag, name)
    }
  }

  def getPreDefLoc(name: String): Option[Loc] = {
    reversePredefTable.get(name) match {
      case Some(addr) => Some(addrToLoc(addr, Recent))
      case None => None
    }
  }

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

  /* Tizen Callback table */
  val TizenCallbackTableLoc: Loc = newPreDefLoc("TizenCallbackTable", Recent)
  val TizenCallbackArgTableLoc: Loc = newPreDefLoc("TizenCallbackArgTable", Recent)
}
