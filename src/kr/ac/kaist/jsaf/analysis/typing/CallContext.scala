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


/* Interface */
object CallContext {
  var globalCallContext: CallContext = null
  
  def initialize = Config.contextSensitivityMode match {
    case Config.Context_Insensitive => globalCallContext = Insensitive.globalCallContext
    case Config.Context_OneCallsite => globalCallContext = OneCallsite.globalCallContext
    case Config.Context_OneObject => globalCallContext = OneObject.globalCallContext
    case Config.Context_OneObjectTAJS => globalCallContext = OneObjectTAJS.globalCallContext
  }
  
  def getModeName = Config.contextSensitivityMode match {
    case Config.Context_Insensitive => Insensitive.getModeName
    case Config.Context_OneCallsite => OneCallsite.getModeName
    case Config.Context_OneObject => OneObject.getModeName
    case Config.Context_OneObjectTAJS => OneObjectTAJS.getModeName
  } 
}

abstract class CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset_this: LocSet): Set[(CallContext, Obj)]
  def compare(that: CallContext): Int
}


/* Context-insensitive */
private object Insensitive {
  val globalCallContext = Insensitive(GlobalCallsite)
  def getModeName = "Insensitive"
}

private case class Insensitive(builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val obj_new = Helper.NewPureLocal(LocSet(l), lset)
    val cc_new = 
      if (cfg.isUserFunction(fid)) {
        Insensitive.globalCallContext
      } else {
        // additional 1-callsite context-sensitivity for built-in calls.
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
  
  override def toString = builtin.toString
}


/* 1-callsite call context */
private object OneCallsite {
  val globalCallContext = OneCallsite(GlobalCallsite, GlobalCallsite)
  def getModeName = "1-callsite"
}

private case class OneCallsite(addr: Address, builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val obj_new = Helper.NewPureLocal(LocSet(l), lset)
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

  override def toString = "(" + addr.toString + "," + builtin.toString + ")"
}


/* 1-object call context */
private object OneObject {
  val globalCallContext = OneObject(GlobalLoc, GlobalCallsite)
  def getModeName = "1-object"
}

private case class OneObject(loc: Loc, builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val env_new = LocSet(l)
    if (cfg.isUserFunction(fid)) {
      lset.foldLeft(HashSet[(CallContext, Obj)]())((result, l_this) => {
        val cc_new = OneObject(l_this, GlobalCallsite)
        val obj_new = Helper.NewPureLocal(env_new, LocSet(l_this))
        result + ((cc_new, obj_new))
      })
    } else {
      // additional 1-callsite context-sensitivity for built-in calls.
      val cc_new = OneObject(this.loc, locToAddr(l))
      val obj_new = Helper.NewPureLocal(env_new, lset)
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

  override def toString = "(" + DomainPrinter.printLoc(loc) + "," + builtin.toString + ")"
}


/* TAJS-style 1-object call context */
private object OneObjectTAJS {
  val globalCallContext = OneObjectTAJS(GlobalSingleton, GlobalCallsite)
  def getModeName = "1-object (TAJS)"
}

private case class OneObjectTAJS(lset: LocSet, builtin: Address) extends CallContext {
  def NewCallContext(cfg: CFG, fid: FunctionId, l: Loc, lset: LocSet): Set[(CallContext, Obj)] = {
    val obj_new = Helper.NewPureLocal(LocSet(l), lset)
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

  override def toString = "({" + DomainPrinter.printLocSet(lset) + "}," + builtin.toString + ")"
}
