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
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}

class CallNewFunction(cfg: CFG, typing: TypingInterface, callgraph: Map[CFGInst, Set[FunctionId]], bugs: BugMessage) extends BugRule {
  
  private val msg = "Calling a function object '%s' both as a function and a constructor."
  private var count = 0
  private var calleeMap = Map[FunctionId, Set[Info]]()
  
  override def printStat = System.out.println("# CallNewFunction: " + count)
  
  
  override def checkFinal(): Unit = {
    /* (call set, new set) */
    val (cset, nset) = callgraph.foldLeft[(Set[FunctionId], Set[FunctionId])]((Set(), Set()))((setpair, kv) =>
      kv._1 match {
        case CFGCall(_, info, _, _, _, _) =>
          kv._2.foreach((fid) =>
            calleeMap.get(fid) match {
              case Some(iset) => calleeMap = calleeMap + (fid -> (iset + info))
              case None => calleeMap = calleeMap + (fid -> Set(info)) });
          (setpair._1 ++ kv._2, setpair._2)
        case CFGConstruct(_, info, _, _, _, _) => 
          kv._2.foreach((fid) =>
            calleeMap.get(fid) match {
              case Some(iset) => calleeMap = calleeMap + (fid -> (iset + info))
              case None => calleeMap = calleeMap + (fid -> Set(info)) });
          (setpair._1, setpair._2 ++ kv._2)
      })
    /* Bug condition */
    // called as a function and a constructor
    val bugset = (cset & nset)
    val cond1 =  !bugset.isEmpty
    
    /* bug check */
    if (cond1) {
      bugset.foreach((fid) =>
        calleeMap(fid).foreach((info) => {
	      count = count + 1
	      bugs.addMessage(info.getSpan, "warning", msg.format(prFuncName(cfg.getFuncName(fid))))
        }))
    }
    else
      Unit
  }
  /* Get function name */
  def prFuncName(name: String) =
    if (NU.isFunExprName(name)) "anonymous_function" else name
}
