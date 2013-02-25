/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.domain

import scala.collection.immutable.TreeSet
import kr.ac.kaist.jsaf.analysis.cfg.FunctionId
import scala.collection.immutable.HashSet
import kr.ac.kaist.jsaf.analysis.typing.Config

object DomainPrinter {
  def printHeap(ind: Int, heap: Heap, verbose: Boolean): String = {
    val printer = new DomainPrinter(verbose)
    printer.indent(ind)
    printer.ppHeap(ind, heap)
    printer.toString
  }

  def printHeap(ind: Int, heap: Heap): String = {
    val printer = new DomainPrinter(Config.verbose)
    printer.indent(ind)
    printer.ppHeap(ind, heap)
    printer.toString
  }

  def printContext(ind: Int, ctx: Context): String = {
    val printer = new DomainPrinter(Config.verbose)
    printer.indent(ind)
    printer.ppContext(ind, ctx)
    printer.toString
  }
  
  def printValue(value: Value): String = {
    val printer = new DomainPrinter(Config.verbose)
    printer.ppValue(value)
    printer.toString
  }

  def printLoc(l: Loc): String = {
    val printer = new DomainPrinter(Config.verbose)
    printer.ppLoc(l)
    printer.toString
  }
  
  def printLocSet(lset: LocSet): String = {
    val printer = new DomainPrinter(Config.verbose)
    printer.ppLocSet(lset)
    printer.toString
  }

  def printObj(ind: Int, o: Obj): String = {
    val printer = new DomainPrinter(Config.verbose)
    printer.indent(ind)
    printer.ppObj(ind, o, Config.verbose)
    printer.toString
  }
}

private class DomainPrinter(verbose: Boolean) {
  val sb = new StringBuilder()
  
  def indent(n: Int): Unit = {
    for (i <- 0 to n-1) sb.append(" ")
  }

  def newline(ind: Int, first: Boolean): Boolean = {
    if (!first) {
      sb.append("\n")
      indent(ind)
    }
    false
  }
  
  def ppHeap(ind: Int, heap: Heap): Unit = {
    var first = true
    for ((loc, obj) <- heap.map.toSeq.sortBy(_._1)) {
      // for non-verbose mode, locations for built-in are skipped.
      if (verbose || locToAddr(loc) >= locToAddr(CollapsedLoc)) {
        first = newline(ind, first)
        val len = ppLoc(loc)
        sb.append(" -> ")
        if (verbose || locToAddr(loc) != locToAddr(GlobalLoc)) {
          ppObj(ind+len+4, obj, true)
        } else {
          ppObj(ind+len+4, obj, false)
        }
      }
    }
    
    if (first) sb.append("Bot")
  }

  def ppContext(ind: Int, ctx: Context): Unit = {
    sb.append("{")
//    ppLocSet(ctx._1);
    sb.append("} X {")
//    ppLocSet(ctx._2)
    if (Config.verbose) {
      sb.append("} X {")
      ppAddrSet(ctx._3)
      sb.append("} X ")
      if (ctx._4 == null) {
        sb.append("Top")
      } else {
        sb.append("{")
        ppAddrSet(ctx._4)
        sb.append("}")
      }
    } else {
      sb.append("}")
    }
  }
  
  def ppObj(ind: Int, obj: Obj, verbose: Boolean): Unit = {
    var first = true
    val map = obj.map
    for ((prop, (pv,abs)) <- map.toSeq.sortBy(_._1)) {
      val show = verbose match {
        case true => true
        case false => Config.testMode match {
          case true =>
            !Config.globalVerboseProp(prop) &&
            !Config.testModeProp.contains(prop)
          case false =>
            !Config.globalVerboseProp(prop)
        }
      }
      
      if (show) { 
        first = newline(ind, first)
        val len = ppProp(prop)
        val arrow = abs match {
          case AbsentBot => "  -> "
          case AbsentTop => "  @-> "
        }
        sb.append(arrow)
        ppPropValue(ind+len+arrow.length, pv)
      }
    }
    if (first) sb.append("{ }")
  }
  
  def ppPropValue(ind: Int, pv: PropValue): Unit = {
    var first = true

    val ov = pv._1
    if (ov != ObjectValueBot) {
      first = newline(ind, first)
      sb.append("[")
      ppBool(ov._2)
      ppBool(ov._3)
      ppBool(ov._4)
      sb.append("] ")
      ppValue(ov._1)
    }
    
    val v = pv._2
    if (v != ValueBot) {
      first = newline(ind, first)
      sb.append("[VAL] ")
      ppValue(v)
    }
    
    val fun = pv._3
    if (fun != FunSetBot) {
      first = newline(ind, first)
      sb.append("[FUN] ")
      ppFunSet(fun)
    }
    
    if (first) sb.append("Bot")
  }

  def ppValue(v: Value): Unit = {
    var first = true
    
    if (v._1 != PValueBot) {
      sb.append(v._1.toString)
      first = false
    }
    
    if (v._2 != LocSetBot) {
      if (!first) sb.append(", ")
      ppLocSet(v._2)
      first = false
    }
    
    if (first) sb.append("Bot")
  }
  
  def ppProp(prop: String): Int = {
    val str = prop
    sb.append(str)
    str.length
  }
  
  def ppLoc(loc: Loc): Int = {
    val name = locName(loc)
    val str = isRecentLoc(loc) match {
      case true => "#" + name
      case false => "##" + name
    }
    sb.append(str)
    str.length 
  }

  def ppAddrSet(set: AddrSet): Unit = {
    var first = true
    for (addr <- set.toSeq.sorted) {
      if (first) {
        first = false
      } else {
        sb.append(", ")
      }
      ppLoc(addrToLoc(addr, Recent))
    }
  }

  def ppLocSet(set: LocSet): Unit = {
    var first = true
    for (loc <- set.toSeq.sorted) {
      if (first) {
        first = false
      } else {
        sb.append(", ")
      }
      ppLoc(loc)
    }
  }
  
  def ppFunSet(set: FunSet): Unit = {
    var first = true
    sb.append("{")
    for (fid <- set.toSeq.sorted) {
      if (first) {
        first = false
      } else {
        sb.append(", ")
      }
      sb.append(fid.toString)
    }
    sb.append("}")
  }

  def ppBool(b: AbsBool): Unit = {
    val str = b match {
      case BoolBot => "B"
      case BoolFalse => "f"
      case BoolTrue => "t"
      case BoolTop => "T"
    }
    sb.append(str)
  }
    
  override def toString = sb.toString
}
