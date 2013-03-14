/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.nodes.AbstractNode
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.nodes_util.SourceLoc

class FinalDetect(cfg: CFG, typing: TypingInterface, bugStorage: BugStorage, semantics: Semantics, callGraph: Map[CFGInst, FidSet], shadowings: List[BugInfo]) {

  ////////////////////////////////////////////////////////////////
  // Bug Detection Main (finally check)
  ////////////////////////////////////////////////////////////////

  def check(): Unit = {
    callConstFuncCheck
    shadowingCheck
    unreachableCodeCheck
    unusedFunctionCheck
    unusedVarPropCheck
    varyingTypeArgumentsCheck
  }



  ////////////////////////////////////////////////////////////////
  // CallConstFunc Check 
  ////////////////////////////////////////////////////////////////

  private def callConstFuncCheck(): Unit = {
    // callSet   : set of fids used as a function
    // constSet  : set of fids used as a constructor
    val (callSet, constSet) = callGraph.foldLeft[(FidSet, FidSet)]((Set(), Set()))((fidSetPair, calledFunction) => 
      calledFunction._1 match {
        case CFGCall(_,info,_,_,_,_) => 
          bugStorage.updateFuncMap(calledFunction._2, info)
          (fidSetPair._1 ++ calledFunction._2, fidSetPair._2)
        case CFGConstruct(_,info,_,_,_,_) => 
          bugStorage.updateFuncMap(calledFunction._2, info)
          (fidSetPair._1, fidSetPair._2 ++ calledFunction._2)
      })
    val bothUsed = callSet & constSet 
    if (!bothUsed.isEmpty) bothUsed.foreach((fid) => bugStorage.getInfoSet(fid).foreach((info) => 
      bugStorage.addMessage1(info.getSpan, 10, getFuncName(cfg.getFuncName(fid)))))
  }



  ////////////////////////////////////////////////////////////////
  // Shadowing Check
  ////////////////////////////////////////////////////////////////

  private def shadowingCheck(): Unit = shadowings.foreach((bug) => bugStorage.addMessage2(bug.span, bug.bugKind, bug.arg1, bug.arg2))



  ////////////////////////////////////////////////////////////////
  // UnreachableCode Check
  ////////////////////////////////////////////////////////////////

  private def unreachableCodeCheck(): Unit = {
    if (!bugStorage.isRangeEmpty) {
      bugStorage.sortRange
      val head = bugStorage.getRangeHead
      var chunkBegin = head.getBegin
      var chunkEnd   = head.getEnd
      for (span <- bugStorage.getRangeTail) {
        val spanBegin = span.getBegin
        val spanEnd   = span.getEnd
        if ((chunkEnd.getLine.toInt < spanBegin.getLine.toInt) || ((chunkEnd.getLine.toInt == spanBegin.getLine.toInt) && (chunkEnd.column.toInt < spanBegin.column.toInt))) {
          bugStorage.addMessage0(new Span(chunkBegin, chunkEnd), 30)
          chunkBegin = span.getBegin; chunkEnd = span.getEnd
        } else {
          if ((spanBegin.getLine.toInt < chunkBegin.getLine.toInt) || ((spanBegin.getLine.toInt == chunkBegin.getLine.toInt) && 
            (spanBegin.column.toInt < chunkBegin.column.toInt))) chunkBegin = spanBegin
          if ((chunkEnd.getLine.toInt < spanEnd.getLine.toInt) || ((chunkEnd.getLine.toInt == spanEnd.getLine.toInt) && 
            (chunkEnd.column.toInt < spanEnd.column.toInt))) chunkEnd = spanEnd
        }
      }
      bugStorage.addMessage0(new Span(chunkBegin, chunkEnd), 30)
    }
  }



  ////////////////////////////////////////////////////////////////
  // UnusedFunction Check
  ////////////////////////////////////////////////////////////////

  private def unusedFunctionCheck(): Unit = {
    val funcCount = cfg.getFuncCount
    var fidSet    = (0 to (funcCount-1) toSet) filterNot (typing.builtinFset contains)
    var unusedFunctions: List[Int] = List()

    for (fid <- bugStorage.filterUsedFunctions(fidSet toList)) {
      if (typing.getStateAtFunctionEntry(fid).isEmpty) {
        bugStorage.addMessage1(cfg.getFuncInfo(fid).getSpan, 31, getFuncName(cfg.getFuncName(fid)))
        bugStorage.appendUnusedFunction(fid)
      }
    }
  }



  ////////////////////////////////////////////////////////////////
  // UnusedVarProp Check
  ////////////////////////////////////////////////////////////////

  private def unusedVarPropCheck(): Unit = {
    // checkList : Stores nodes to be traversed
    // unusedMap : Stores unused write set
    var checkList: List[Node] = List((cfg.getGlobalFId, LEntry))
    var unusedMap: WMap = Map()
    
    /*
    * Traverse cfg starting from the global entry node. 
    * At each node (current), compare write variable or property 
    * with the ones of predecessors (stored in beforeCheck Set). 
    * After comparison, store unused write variables or properties 
    * into afterCheck Set.
    */
    while (!checkList.isEmpty) {
      /*
      * current     : current node to be checked
      * predSet     : predecessors of current
      * beforeCheck : Set of unused Writes before comparison
      * afterCheck  : Set of unused Writes after comparison
      */
      val current = checkList.head
      val predSet = cfg.getPred(current)
      
      // before comparison
      val beforeCheck = predSet.foldLeft[Set[WEntry]](Set())((set, pred) =>
        unusedMap.get(pred) match {
          case Some(wset) => set ++ wset
          case None => set
        })
        
      // comparing ... 
      val afterCheck = bugStorage.getRWEntry(current) match {
        case Some(rwSequence) =>
          rwSequence.foldLeft[Set[WEntry]](beforeCheck)((set, rw) =>
            if (rw._1) { // write variables or properties
              val found = set.find((e) => ((e._1 == rw._2) && (e._2 == rw._3) && (e._3 == rw._4)))
              found match { // Found means these variables or properties are written again before read.
                case Some(exist) => // So, these are never used, but not interesting to users (SKIP)
                  val removed = set.filterNot((e) => ((e._1 == rw._2) && (e._2 == rw._3) && (e._3 == rw._4))) 
                  removed + ((rw._2, rw._3, rw._4, rw._5))
                case None => // newly found write variables or properties (APPEND)
                  set + ((rw._2, rw._3, rw._4, rw._5))
              }
            }
            else { // read variables or properties (SKIP)
              set.filterNot((e) => ((e._1 == rw._2) && (e._2 == rw._3) && (e._3 == rw._4)))
            }
          )
        case None => beforeCheck
      }

      // update unusedMap
      unusedMap.get(current) match {
        case Some(currentWSet) =>
          // unchanged
          if (currentWSet == afterCheck) checkList = checkList.tail
          else { // changed 
            unusedMap = unusedMap + (current -> afterCheck)
            checkList = checkList.tail ++ addSuccessors(current)
          }
        case None => // changed
          unusedMap = unusedMap + (current -> afterCheck)
          checkList = checkList.tail ++ addSuccessors(current)
      }
    }
    
    // check terminal node
    cfg.getNodes.foreach((node) => 
      if (cfg.getAllSucc(node).isEmpty) {
        unusedMap.get(node) match {
          case Some(unusedSet) => unusedSet.foreach((bug) => bugStorage.addMessage1(bug._4, if (bug._1) 33 else 32, bugStorage.getOriginalName(bug._3)))
          case None => Unit
    }})

    ////////////////////////////////////////////////////////////////
    // addSuccessros: add new node to checklist.
    // If the last instruction of current node is call instruction, 
    // add following IPSucc node (from Semantics) to the checklist.
    // Otherwise, add successor nodes (from cfg) to the checkList.
    ////////////////////////////////////////////////////////////////

    def addSuccessors(current: Node): List[Node] = {
      typing.getStateBeforeNode(current).keys.foldLeft[List[Node]](List())((list, callContext) =>
        semantics.getIPSucc((current, callContext)) match {
          case Some(ccMap) => ccMap.keys.foldLeft(list)((l, k) => l :+ k._1)
          case None => list ++ cfg.getSucc(current)
        }
      )
    }
  }



  ////////////////////////////////////////////////////////////////
  // VaryingTypeArguments Check 
  ////////////////////////////////////////////////////////////////

  private def varyingTypeArgumentsCheck(): Unit = {
    bugStorage.applyToDetectedFuncMap((fid: FunctionId, obj: Obj, spanSet: Set[Span]) => {
      val arglen = cfg.getArgVars(fid).size
      val isBug = (0 until arglen).foldLeft(false)((b, i) => b || (1 != obj(i.toString)._1._1._1.typeCount))
      if (isBug) spanSet.foreach((span) => bugStorage.addMessage1(span, 34, getFuncName(cfg.getFuncName(fid))))
    })
  }
}
