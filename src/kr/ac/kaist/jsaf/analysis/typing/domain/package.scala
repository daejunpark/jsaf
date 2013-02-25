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

package object domain {
  /* abstract location */
  type Loc = Int
  type Address = Int
  type RecencyTag = Int
  val Recent = 0
  val Old = 1
  
  
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
  
  val StringProtoLoc: Loc   = registerPredefLoc(-4, Recent, "StringProto")
  val ObjProtoLoc: Loc      = registerPredefLoc(-5, Recent, "ObjProto")
  val NumberProtoLoc: Loc   = registerPredefLoc(-6, Recent, "NumberProto")
  val FunctionProtoLoc: Loc = registerPredefLoc(-7, Recent, "FunctionProto")
  val BooleanProtoLoc: Loc  = registerPredefLoc(-8, Recent, "BooleanProto")
  val ArrayProtoLoc: Loc    = registerPredefLoc(-9, Recent, "ArrayProto")
  val DateProtoLoc: Loc     = registerPredefLoc(-10, Recent, "DateProto")
  
  val StringConstLoc: Loc   = registerPredefLoc(-11, Recent, "StringConst")
  val ObjConstLoc: Loc      = registerPredefLoc(-12, Recent, "ObjConst")
  val NumberConstLoc: Loc   = registerPredefLoc(-13, Recent, "NumberConst")
  val FunctionConstLoc: Loc = registerPredefLoc(-14, Recent, "FunctionConst")
  val BooleanConstLoc: Loc  = registerPredefLoc(-15, Recent, "BooleanConst")
  val ArrayConstLoc: Loc    = registerPredefLoc(-16, Recent, "ArrayConst")
  
  /* error instance */
  val ErrLoc: Loc           = registerPredefLoc(-17, Old, "Err")
  val EvalErrLoc: Loc       = registerPredefLoc(-18, Old, "EvalErr")
  val RangeErrLoc: Loc      = registerPredefLoc(-19, Old, "RangeErr")
  val RefErrLoc: Loc        = registerPredefLoc(-20, Old, "RefErr")
  val SyntaxErrLoc: Loc     = registerPredefLoc(-21, Old, "SyntaxErr")
  val TypeErrLoc: Loc       = registerPredefLoc(-22, Old, "TypeErr")
  val URIErrLoc: Loc        = registerPredefLoc(-23, Old, "URIErr")
  
  /* error prototype object */
  val ErrProtoLoc:Loc       = registerPredefLoc(-24, Recent, "ErrProto")
  val EvalErrProtoLoc:Loc   = registerPredefLoc(-25, Recent, "EvalErrProto")
  val RangeErrProtoLoc: Loc = registerPredefLoc(-26, Recent, "RangeErrProto")
  val RefErrProtoLoc: Loc   = registerPredefLoc(-27, Recent, "RefErrProto")
  val SyntaxErrProtoLoc: Loc= registerPredefLoc(-28, Recent, "SyntaxErrProto")
  val TypeErrProtoLoc: Loc  = registerPredefLoc(-29, Recent, "TypeErrProto")
  val URIErrProtoLoc: Loc   = registerPredefLoc(-30, Recent, "URIErrProto")
  
  val ObjPseudoTopLoc: Loc  = registerPredefLoc(-31, Old, "ObjPseudoTop")

  /* special location standing for Context */
  val ContextLoc: Loc       = registerPredefLoc(-32, Old, "Context")

  /* start address for PureLocal/builtin function addresses */
  var startAddress          = -33
  def newLoc(name: String) = {
    val addr = startAddress
    startAddress -= 1
    registerPredefLoc(addr, Recent, name)
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
  
  val FIdTop = -2
  
  //act like ValueTop.(incorrect ValueTop, should not compare with other value
  val ValuePseudoTop = Value(PValueTop, LocSet(ObjPseudoTopLoc))
  val ObjectValuePseudoTop = ObjectValue(ValuePseudoTop, BoolTop,BoolTop,BoolTop) 
  val ObjPseudoTop = Obj(ObjMapBot.
    updated("@class", (PropValue(Value(StrTop)), AbsentTop)).
    updated("@extensible", (PropValue(Value(BoolTop)), AbsentTop)).
    updated("@proto", (PropValue(Value(PValue(NullTop))), AbsentTop)).
    updated("@function",  (PropValue(ObjectValueBot, ValueBot, FunSet(FIdTop)), AbsentTop)).
    updated("@construct", (PropValue(ObjectValueBot, ValueBot, FunSet(FIdTop)), AbsentTop)).
    updated("@default_number", (PropValue(ObjectValuePseudoTop), AbsentTop)).
    updated("@default_other", (PropValue(ObjectValuePseudoTop), AbsentTop)))

  val LPBot = LPSet(HashMap[Loc,Set[String]]())
  val LBot = LocSetBot
}
