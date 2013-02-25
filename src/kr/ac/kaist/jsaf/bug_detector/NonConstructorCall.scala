/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class NonConstructorCall(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  /* bug message */  
  private val msg   = "Non-constructor '%s' is called as a constructor."
  private var count = 0
  // Need to consider new "Math", new "JSON"
  private val nonConsSet: Set[String] = Set(
    "Global.eval", "Global.parseInt", "Global.parseFloat", "Global.isNaN", "Global.isFinite",
    "Global.decodeURI", "Global.decodeURIComponent", "Global.encodeURI", "Global.encodeURIComponent",
    "Math.abs", "Math.acos", "Math.asin", "Math.atan", "Math.atan2", "Math.ceil", "Math.cos", 
    "Math.exp", "Math.floor", "Math.log", "Math.max", "Math.min", "Math.pow", "Math.random", 
    "Math.round", "Math.sin", "Math.sqrt", "Math.tan", "JSON.parse", "JSON.stringify")
      

  override def printStat = System.out.println("# NonConstructorCall: " + count) 
    
  /* CFGConstruct -> Built-in function properties or objects */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state   = typing.mergeState(cstate)
    val heap    = state._1
    val context = state._2

    if (state._1 <= HeapBot)  Unit
    else {
      inst match {
        case CFGConstruct(_, info, cons, _, _, _) => 
          val cons_val = SE.V(cons, heap, context)._1
          val locs_original = cons_val._2
          val locs_filtered = locs_original filter((l) => BoolTrue <= Helper.HasConstruct(heap, l))
          locs_original.foreach((loc) =>
            heap(loc)("@function")._1._3.foreach((fid) =>
              typing.builtinFset.get(fid) match {
                case Some(builtinName) =>
                  if ((nonConsSet contains builtinName) || (locs_filtered.size < locs_original.size)) {
                    count = count + 1
                    bugs.addMessage(info.getSpan, "error", msg.format(prFuncName(cfg.getFuncName(fid))))
                  }; Unit
                case None => Unit
              }
            )
          )
        case _ => Unit
      }
    }
  }

  /* Get function name */
  def prFuncName(name: String) =
    if (NU.isFunExprName(name)) "anonymous_function" else name
}
