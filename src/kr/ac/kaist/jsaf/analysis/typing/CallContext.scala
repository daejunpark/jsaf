/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import scala.collection.immutable.HashSet
import kr.ac.kaist.jsaf.analysis.typing.AddressManager._

/* Interface */
object CallContext {
  var globalCallContext: CallContext = null
  
  def initialize = Config.contextSensitivityMode match {
    case Config.Context_Insensitive => globalCallContext = Insensitive.globalCallContext
    case Config.Context_OneCallsite => globalCallContext = OneCallsite.globalCallContext
    case Config.Context_OneObject => globalCallContext = OneObject.globalCallContext
    case Config.Context_OneObjectTAJS => globalCallContext = OneObjectTAJS.globalCallContext
    case Config.Context_OneCallsiteAndObject => globalCallContext = OneCallsiteAndObject.globalCallContext
    case Config.Context_OneCallsiteOrObject => globalCallContext = OneCallsiteOrObject.globalCallContext
    case Config.Context_KCallsite => globalCallContext = KCallsite.globalCallContext
    case Config.Context_KCallsiteAndObject => globalCallContext = KCallsiteAndObject.globalCallContext
    case Config.Context_CallsiteSet => globalCallContext = CallsiteSet.globalCallContext
  }
  
  def getModeName = Config.contextSensitivityMode match {
    case Config.Context_Insensitive => Insensitive.getModeName
    case Config.Context_OneCallsite => OneCallsite.getModeName
    case Config.Context_OneObject => OneObject.getModeName
    case Config.Context_OneObjectTAJS => OneObjectTAJS.getModeName
    case Config.Context_OneCallsiteAndObject => OneCallsiteAndObject.getModeName
    case Config.Context_OneCallsiteOrObject => OneCallsiteOrObject.getModeName
    case Config.Context_KCallsite => KCallsite.getModeName
    case Config.Context_KCallsiteAndObject => KCallsiteAndObject.getModeName
    case Config.Context_CallsiteSet => CallsiteSet.getModeName
  } 

  ////////////////////////////////////////////////////////////////////////////////
  // Context sensitivity mode flags
  ////////////////////////////////////////////////////////////////////////////////
  type SensitivityFlagType =                    Int

  val _INSENSITIVE:                             SensitivityFlagType = 0x00000000
  val _1_CALLSITE:                              SensitivityFlagType = 0x00000001
  val _1_OBJECT:                                SensitivityFlagType = 0x00000002
  val _MOST_SENSITIVE:                          SensitivityFlagType = 0xFFFFFFFF
}

abstract class CallContext {
  // cfg: control flow graph
  // fid: callee FunctionId
  // l: environment(scope) of callee (call instruction id can be used to separate the callsite)
  // lset: this value of callee
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset_this: LocSet): Set[(CallContext, Obj)]
  def compare(that: CallContext): Int
  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext
  def toString2(): String = {""}
}


////////////////////////////////////////////////////////////////////////////////
// Context-insensitive
////////////////////////////////////////////////////////////////////////////////
private object Insensitive {
  val globalCallContext = Insensitive(GlobalCallsite)
  def getModeName = "Insensitive"
}

private case class Insensitive(builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val obj_new = Helper.NewPureLocal(Value(LocSet(l)), lset)
    val cc_new = 
      if (cfg.isUserFunction(fid)) {
        Insensitive.globalCallContext
      } else {
        // additional 1-callsite context-sensitivity for built-in calls.
//        Insensitive.globalCallContext
        Insensitive(locToAddr(l))
      }
    HashSet((cc_new, obj_new))
  }
  
  def compare(other: CallContext): Int = {
    other match {
      case that: Insensitive => 
        if (this.builtin < that.builtin) -1
        else if (this.builtin > that.builtin) 1
        else 0
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = this

  override def toString = builtin.toString
}


////////////////////////////////////////////////////////////////////////////////
// 1-callsite call context
////////////////////////////////////////////////////////////////////////////////
private object OneCallsite {
  val globalCallContext = OneCallsite(GlobalCallsite, GlobalCallsite)
  def getModeName = "1-callsite"
}

private case class OneCallsite(addr: Address, builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val obj_new = Helper.NewPureLocal(Value(LocSet(l)), lset)
    val cc_new = 
      if (cfg.isUserFunction(fid)) {
        OneCallsite(locToAddr(l), GlobalCallsite)
      } else {
        // additional 1-callsite context-sensitivity for built-in calls.
        OneCallsite(this.addr, locToAddr(l))
      }
    HashSet((cc_new, obj_new))
  }
  
  def compare(other: CallContext): Int = {
    other match {
      case that: OneCallsite => 
        if (this.addr < that.addr) -1
        else if (this.addr > that.addr) 1
        else {
          if (this.builtin < that.builtin) -1
          else if (this.builtin > that.builtin) 1
          else 0
        }
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = {
    if ((flag & CallContext._1_CALLSITE) != 0) this
    else new OneCallsite(GlobalCallsite, builtin)
  }

  override def toString = "(" + addr.toString + "," + builtin.toString + ")"
}


////////////////////////////////////////////////////////////////////////////////
// 1-object call context
////////////////////////////////////////////////////////////////////////////////
private object OneObject {
  val globalCallContext = OneObject(GlobalLoc, GlobalCallsite)
  def getModeName = "1-object"
}

private case class OneObject(loc: Loc, builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val env_new = LocSet(l)
    if (cfg.isUserFunction(fid)) {
      lset.foldLeft(HashSet[(CallContext, Obj)]())((result, l_this) => {
        val this_new = if (l_this == GlobalLoc) this.loc else l_this
        val cc_new = OneObject(this_new, GlobalCallsite)
        val obj_new = Helper.NewPureLocal(Value(env_new), LocSet(l_this))
        result + ((cc_new, obj_new))
      })
    } else {
      // additional 1-callsite context-sensitivity for built-in calls.
      val cc_new = OneObject(this.loc, locToAddr(l))
      val obj_new = Helper.NewPureLocal(Value(env_new), lset)
      HashSet((cc_new, obj_new))
    }
  }
  
  def compare(other: CallContext): Int = {
    other match {
      case that: OneObject => 
        val loc_cmp = this.loc - that.loc
        if (loc_cmp != 0) loc_cmp
        else {
          if (this.builtin < that.builtin) -1
          else if (this.builtin > that.builtin) 1
          else 0
        }
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = {
    if ((flag & CallContext._1_OBJECT) != 0) this
    else new OneObject(GlobalLoc, builtin)
  }

  override def toString = "(" + DomainPrinter.printLoc(loc) + "," + builtin.toString + ")"
}


////////////////////////////////////////////////////////////////////////////////
// TAJS-style 1-object call context
////////////////////////////////////////////////////////////////////////////////
private object OneObjectTAJS {
  val globalCallContext = OneObjectTAJS(GlobalSingleton, GlobalCallsite)
  def getModeName = "1-object (TAJS)"
}

private case class OneObjectTAJS(lset: LocSet, builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val obj_new = Helper.NewPureLocal(Value(LocSet(l)), lset)
    val cc_new = 
      if (cfg.isUserFunction(fid)) {
        OneObjectTAJS(lset, GlobalCallsite)
      } else {
        // additional 1-callsite context-sensitivity for built-in calls.
        OneObjectTAJS(this.lset, locToAddr(l))
      }
    HashSet((cc_new, obj_new))
  }

  def compare(other: CallContext): Int = {
    other match {
      case that: OneObjectTAJS => 
        val lset_cmp = this.lset.compare(that.lset)
        if (lset_cmp != 0) lset_cmp
        else {
          if (this.builtin < that.builtin) -1
          else if (this.builtin > that.builtin) 1
          else 0
        }
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = {
    if ((flag & CallContext._1_OBJECT) != 0) this
    else new OneObjectTAJS(LocSet(GlobalLoc), builtin)
  }

  override def toString = "({" + DomainPrinter.printLocSet(lset) + "}," + builtin.toString + ")"
}


////////////////////////////////////////////////////////////////////////////////
// (1-callsite and 1-object) call context
////////////////////////////////////////////////////////////////////////////////
private object OneCallsiteAndObject {
  val globalCallContext = OneCallsiteAndObject(GlobalCallsite, GlobalLoc, GlobalCallsite)
  def getModeName = "1-callsite and 1-object"
}

private case class OneCallsiteAndObject(addr: Address, loc: Loc, builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    // 1-callsite and 1-object
    val env_new = LocSet(l)
    if (cfg.isUserFunction(fid)) {
      lset.foldLeft(HashSet[(CallContext, Obj)]())((result, l_this) => {
        val this_new = if (l_this == GlobalLoc) this.loc else l_this
        val cc_new = OneCallsiteAndObject(locToAddr(l), this_new, GlobalCallsite)
        val obj_new = Helper.NewPureLocal(Value(env_new), LocSet(l_this))
        result + ((cc_new, obj_new))
      })
    } else {
      val cc_new = OneCallsiteAndObject(this.addr, this.loc, locToAddr(l))
      val obj_new = Helper.NewPureLocal(Value(env_new), lset)
      HashSet((cc_new, obj_new))
    }
  }

  def compare(other: CallContext): Int = {
    other match {
      case that: OneCallsiteAndObject =>
        val addr_cmp = this.addr - that.addr
        if (addr_cmp != 0) return addr_cmp

        val loc_cmp = this.loc - that.loc
        if (loc_cmp != 0) return loc_cmp

        val builtin_cmp = this.builtin - that.builtin
        if (builtin_cmp != 0) return builtin_cmp

        return 0
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = {
    if ((flag & CallContext._1_CALLSITE) != 0 && (flag & CallContext._1_OBJECT) != 0) this
    else {
      var addr: Address = GlobalCallsite
      var loc: Loc = GlobalLoc
      if ((flag & CallContext._1_CALLSITE) != 0) addr = locToAddr(this.addr)
      if ((flag & CallContext._1_OBJECT) != 0) loc = this.loc
      new OneCallsiteAndObject(addr, loc, GlobalCallsite)
    }
  }

  override def toString = "(" + addr.toString + "," + DomainPrinter.printLoc(loc) + "," + builtin.toString + ")"

  override def toString2: String = {
    val callsiteValue = "env = #" + locName(addrToLoc(addr, Recent))
    val thisValue = "this = #" + locName(loc)
    val builtinValue = "built-in = #" + locName(addrToLoc(builtin, Recent))
    "(" + callsiteValue + ", " + thisValue + ")"
  }
}


////////////////////////////////////////////////////////////////////////////////
// (1-callsite or 1-object) call context
////////////////////////////////////////////////////////////////////////////////
private object OneCallsiteOrObject {
  val globalCallContext = OneCallsiteOrObject(GlobalCallsite, GlobalLoc, GlobalCallsite)
  def getModeName = "1-callsite or 1-object"
}

private case class OneCallsiteOrObject(addr: Address, loc: Loc, builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    // 1-callsite or 1-object
    val env_new = LocSet(l)
    if (cfg.isUserFunction(fid)) {
      lset.foldLeft(HashSet[(CallContext, Obj)]())((result, l_this) => {
        if (l_this == GlobalLoc) {
          // 1-callsite
          val cc_new = OneCallsiteOrObject(locToAddr(l), this.loc, GlobalCallsite)
          val obj_new = Helper.NewPureLocal(Value(env_new), lset)
          result + ((cc_new, obj_new))
        }
        else {
          // 1-object
          val cc_new = OneCallsiteOrObject(GlobalCallsite, l_this, GlobalCallsite)
          val obj_new = Helper.NewPureLocal(Value(env_new), LocSet(l_this))
          result + ((cc_new, obj_new))
        }
      })
    } else {
      val cc_new = OneCallsiteOrObject(this.addr, this.loc, locToAddr(l))
      val obj_new = Helper.NewPureLocal(Value(env_new), lset)
      HashSet((cc_new, obj_new))
    }
  }

  def compare(other: CallContext): Int = {
    other match {
      case that: OneCallsiteOrObject =>
        val addr_cmp = this.addr - that.addr
        if (addr_cmp != 0) return addr_cmp

        val loc_cmp = this.loc - that.loc
        if (loc_cmp != 0) return loc_cmp

        val builtin_cmp = this.builtin - that.builtin
        if (builtin_cmp != 0) return builtin_cmp

        return 0
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = {
    if ((flag & CallContext._1_CALLSITE) != 0 && (flag & CallContext._1_OBJECT) != 0) this
    else {
      var addr: Address = GlobalCallsite
      var loc: Loc = GlobalLoc
      if ((flag & CallContext._1_CALLSITE) != 0) addr = locToAddr(this.addr)
      if ((flag & CallContext._1_OBJECT) != 0) loc = this.loc
      new OneCallsiteAndObject(addr, loc, GlobalCallsite)
    }
  }

  override def toString = "(" + addr.toString + "," + DomainPrinter.printLoc(loc) + "," + builtin.toString + ")"
}


////////////////////////////////////////////////////////////////////////////////
// k-callsite call context
////////////////////////////////////////////////////////////////////////////////
private object KCallsite {
  val globalCallContext = KCallsite(List())
  def getModeName = Config.contextSensitivityDepth.toString + "-callsite"
    
  /**
   * Lexicographic ordering of two Address Lists.
   */
  def compareList(x: List[Address], y: List[Address]): Int = {
    (x, y) match {
      case (Nil, Nil) => 0
      case (Nil, _) => -1
      case (_, Nil) => 1
      case (x1 :: xs, y1 :: ys) =>
        val cmp = x1 - y1
        if (cmp != 0) cmp
        else compareList(xs, ys)
    }
  } 
}

private case class KCallsite(callsiteList: List[Address]) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val k = 
      if (cfg.isUserFunction(fid)) {
        Config.contextSensitivityDepth
      } else {
        // additional depth for built-in calls.
        Config.contextSensitivityDepth + 1
      }
    val newCallsiteList = (locToAddr(l) :: this.callsiteList).take(k)
    val newPureLocal = Helper.NewPureLocal(Value(LocSet(l)), lset)
    HashSet((KCallsite(newCallsiteList), newPureLocal))  
  }
  
  def compare(other: CallContext): Int = {
    other match {
      case that: KCallsite => KCallsite.compareList(this.callsiteList, that.callsiteList)
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  // TODO: apply appropriate filtering if necessary
  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = this

  override def toString = callsiteList.toString
}


////////////////////////////////////////////////////////////////////////////////
// k-callsite and 1-object call context
////////////////////////////////////////////////////////////////////////////////
private object KCallsiteAndObject {
  val globalCallContext = KCallsiteAndObject(List(), GlobalLoc)
  def getModeName = Config.contextSensitivityDepth.toString + "-callsite and 1-object"
}

private case class KCallsiteAndObject(callsiteList: List[Address], thisLoc: Loc) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val k = 
      if (cfg.isUserFunction(fid)) {
        Config.contextSensitivityDepth
      } else {
        // additional depth for built-in calls.
        Config.contextSensitivityDepth + 1
      }
    val newCallsiteList = (locToAddr(l) :: this.callsiteList).take(k)
    val newEnv = LocSet(l)

    lset.foldLeft(HashSet[(CallContext, Obj)]())((result, l_this) => {
      val newPureLocal = Helper.NewPureLocal(Value(newEnv), LocSet(l_this))
      val newThisLoc = if (l_this == GlobalLoc) this.thisLoc else l_this
      val newCallContext = KCallsiteAndObject(newCallsiteList, newThisLoc)
      result + ((newCallContext, newPureLocal))
    })
  }
  
  def compare(other: CallContext): Int = {
    other match {
      case that: KCallsiteAndObject =>
        val callsite_cmp = KCallsite.compareList(this.callsiteList, that.callsiteList)
        if (callsite_cmp != 0) callsite_cmp
        else this.thisLoc - that.thisLoc
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  // TODO: apply appropriate filtering if necessary
  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = this

  override def toString = "(" + callsiteList.toString + ", " + DomainPrinter.printLoc(thisLoc) + ")"
}


////////////////////////////////////////////////////////////////////////////////
// callsite-set call context
////////////////////////////////////////////////////////////////////////////////
private object CallsiteSet {
  val globalCallContext = CallsiteSet(LocSetBot)
  def getModeName = "callsite-set"
}

private case class CallsiteSet(callsiteSet: LocSet) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val newPureLocal = Helper.NewPureLocal(Value(LocSet(l)), lset)
    val newCallsiteSet = this.callsiteSet + locToAddr(l)
    HashSet((CallsiteSet(newCallsiteSet), newPureLocal))  
  }
  
  def compare(other: CallContext): Int = {
    other match {
      case that: CallsiteSet => this.callsiteSet.compare(that.callsiteSet)
      case _ => throw new InternalError("compare must be called on same CallContext kinds")
    }
  }

  // TODO: apply appropriate filtering if necessary
  def filterSensitivity(flag: CallContext.SensitivityFlagType): CallContext = this

  override def toString = callsiteSet.toString
}
