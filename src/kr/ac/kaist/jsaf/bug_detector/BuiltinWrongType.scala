/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class BuiltinWrongType(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
  
  private val msg = "First argument of %s should be a(n) %s"
  
  private var count = 0
  override def printStat = System.out.println("# BuiltinWrongType: " + count)
  
  private val isNotObject = (h: Heap, arg: LocSet, i: Int) => {
    val o_arg = arg.foldLeft(ObjBot)((o, l) => o + h(l))
    val v = o_arg(i.toString)._1._1._1
    if (v._1 </ PValueBot)
      true
    else
      false
  } 
  private val isNotFunction = (h: Heap, arg: LocSet, i: Int) => {
    val o_arg = arg.foldLeft(ObjBot)((o, l) => o + h(l))
    val v = o_arg(i.toString)._1._1._1
    if (v._1 </ PValueBot)
      true
    else {
      val lset_nf = v._2.filter((l) => BoolTrue <= h(l).domIn("@function"))
      if (lset_nf.isEmpty)
        true
      else
        false
    }
  }
  
  private val argTestMap = Map[String, ((Heap, LocSet, Int) => Boolean, String)](
      "Object.getPrototypeOf" -> (isNotObject, "Object"),
      "Object.getOwnPropertyDescriptor" -> (isNotObject, "Object"),
      "Object.getOwnPropertyNames" -> (isNotObject, "Object"),
      "Object.create" -> (isNotObject, "Object"),
      "Object.defineProperty" -> (isNotObject, "Object"),
      "Object.defineProperties" -> (isNotObject, "Object"),
      "Object.seal" -> (isNotObject, "Object"),
      "Object.freeze" -> (isNotObject, "Object"),
      "Object.preventExtensions" -> (isNotObject, "Object"),
      "Object.isSealed" -> (isNotObject, "Object"),
      "Object.isFrozen" -> (isNotObject, "Object"),
      "Object.isExtensible" -> (isNotObject, "Object"),
      "Object.keys" -> (isNotObject, "Object"),
      "Array.prototype.sort" -> (isNotFunction, "Function"),
      "Array.prototype.every" -> (isNotFunction, "Function"),
      "Array.prototype.some" -> (isNotFunction, "Function"),
      "Array.prototype.forEach" -> (isNotFunction, "Function"),
      "Array.prototype.map" -> (isNotFunction, "Function"),
      "Array.prototype.filter" -> (isNotFunction, "Function"),
      "Array.prototype.reduce" -> (isNotFunction, "Function"),
      "Array.prototype.reduceRight" -> (isNotFunction, "Function"))
    
  /* Call, Construct */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    /* state, heap context */
    val state = typing.mergeState(cstate)
    val heap = state._1
    val context = state._2
    
    if (heap <= HeapBot)
      Unit
    else {
      inst match {
        case CFGCall(_, info, fun, _, arguments, _) =>
          val v_fun = SE.V(fun, heap, context)._1
          val lset_f = v_fun._2.filter((l) => BoolTrue <= Helper.IsCallable(heap,l))
          val v_arg = SE.V(arguments, heap, context)._1
          lset_f.foreach((l) =>
            heap(l)("@function")._1._3.foreach((fid) =>
              typing.builtinFset.get(fid) match {
                case Some(name) => 
                  argTestMap.get(name) match {
                    case Some((testfun, typ)) =>
                      if (testfun(heap, v_arg._2, 0)) {
                        count = count + 1
                        bugs.addMessage(info.getSpan, "warning", msg.format(cfg.getFuncName(fid),typ))
                      }
                      else
                        Unit
                    case None => Unit
                  }
                case None => Unit
              }))
        case CFGConstruct(_, info, cons, _, arguments, _) => 
          val v_fun = SE.V(cons, heap, context)._1
          val lset_f = v_fun._2.filter((l) => BoolTrue <= Helper.HasConstruct(heap,l))
          val v_arg = SE.V(arguments, heap, context)._1
          lset_f.foreach((l) =>
            heap(l)("@function")._1._3.foreach((fid) =>
              typing.builtinFset.get(fid) match {
                case Some(name) => 
                  argTestMap.get(name) match {
                    case Some((testfun, typ)) =>
                      if (testfun(heap, v_arg._2, 0)) {
                        count = count + 1
                        bugs.addMessage(info.getSpan, "warning", msg.format(cfg.getFuncName(fid), typ))
                      }
                      else
                        Unit
                    case None => Unit
                  }
                case None => Unit
              }))
        case _ => Unit
      }
    }
  }
}
