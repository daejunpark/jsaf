/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class VaryingArgumentsType(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {
  
  private val msg = "calling a function '%s' with varying type arguments."
  
  private var count = 0
  override def printStat = System.out.println("# VaryingArgumentsType: " + count)
  
  private var argmap = Map[FunctionId, (Obj, Set[Span])]()
  
  /* Call, Construct */ 
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    /* state, heap context */
    val state = typing.mergeState(cstate)
    val heap = state._1
    val context = state._2
    
    if (heap <= HeapBot)
      Unit
    else {
    /* function -> set of argument object map */
      inst match {
        case CFGCall(_, info, fun, _, arguments, _) =>
          val v_arg = SE.V(arguments, heap, context)._1
          val obj_arg = v_arg._2.foldLeft(ObjBot)((o, l) => o + heap(l))
          val lset    = SE.V(fun, heap, context)._1._2
          val lset_f  = lset.filter((l) => BoolTrue <= Helper.IsCallable(heap, l))
          lset_f.foreach((l) => { 
            val fids = heap(l)("@function")._1._3
            fids.foreach((fid) => 
              argmap.get(fid) match {
                case Some((obj, spans)) =>
                  argmap = argmap + (fid -> ((obj + obj_arg), (spans + info.getSpan)))
                case None =>
                  argmap = argmap + (fid -> (obj_arg, Set(info.getSpan)))
              })
            })
        case CFGConstruct(_, info, cons, _, arguments, _) => 
          val v_arg = SE.V(arguments, heap, context)._1
          val obj_arg = v_arg._2.foldLeft(ObjBot)((o, l) => o + heap(l))
          val lset    = SE.V(cons, heap, context)._1._2
          val lset_f  = lset.filter((l) => BoolTrue <= Helper.HasConstruct(heap, l))
          lset_f.foreach((l) => { 
            val fids = heap(l)("@construct")._1._3
            fids.foreach((fid) => 
              argmap.get(fid) match {
                case Some((obj, spans)) =>
                  argmap = argmap + (fid -> ((obj + obj_arg), (spans + info.getSpan)))
                case None =>
                  argmap = argmap + (fid -> (obj_arg, Set(info.getSpan)))
              })
            })
        case _ => Unit
      }
    }
  }
    
  override def checkFinal(): Unit = {
    argmap.foreach((kv) => {
      val fid = kv._1
      /* arguments object */
      val obj = kv._2._1
      /* set of inst. info  */
      
      val sset = kv._2._2
      /* length of parameter */
      val arglen = cfg.getArgVars(fid).size
      /* bug condition, typesize > 1 */
      val cond = (0 until arglen).foldLeft(false)((b, i) =>
        b || (1 != obj(i.toString)._1._1._1.typeCount))

      /* bug check */
      if (cond) {
        sset.foreach((span) => {
	      count = count + 1
	      bugs.addMessage(span, "warning", msg.format(prFuncName(cfg.getFuncName(fid))))
	      })
      }
      else
        Unit
    })
  }
    /* Get function name */
  def prFuncName(name: String) =
    if (NU.isFunExprName(name)) "anonymous_function" else name
}
