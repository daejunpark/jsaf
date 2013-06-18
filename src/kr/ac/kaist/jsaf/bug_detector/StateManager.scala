/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import scala.collection.mutable.SynchronizedMap
import scala.collection.mutable.{HashMap => MHashMap}
import scala.collection.mutable.{HashSet => MHashSet}
import scala.collection.immutable.{HashMap => IHashMap}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.CallContext._
import kr.ac.kaist.jsaf.analysis.typing.domain._

class StateManager(bugDetector: BugDetector) {
  ////////////////////////////////////////////////////////////////////////////////
  // From BugDetector
  ////////////////////////////////////////////////////////////////////////////////
  val cfg                                       = bugDetector.cfg
  val typing                                    = bugDetector.typing
  val semantics                                 = bugDetector.semantics
  val varManager                                = bugDetector.varManager

  ////////////////////////////////////////////////////////////////////////////////
  // Types
  ////////////////////////////////////////////////////////////////////////////////
  type cacheKeyType =                           (Node, InstId, sensitivityFlagType)

  ////////////////////////////////////////////////////////////////////////////////
  // States
  ////////////////////////////////////////////////////////////////////////////////
  val cache =                                   new MHashMap[cacheKeyType, CState]()// with SynchronizedMap[cacheKeyType, CState]

  def getCState(node: Node, instId: InstId = -1, sensitivityFlag: sensitivityFlagType = _MOST_SENSITIVE): CState = {
    // This is a condition key value.
    val cacheKey: cacheKeyType = (node, instId, sensitivityFlag)

    // Try to get the cached CState.
    cache.get(cacheKey) match {
      case Some(cstate) => return cstate // cache hit
      case None =>
    }

    // Get original CState from Table.
    var result: CState = new IHashMap[CallContext, State]()
    typing.readTable(node) match {
      case Some(cstate) =>
        // Merge states depending on sensitivity flag.
        for((callContext, state) <- cstate) {
          val filteredCallContext: CallContext = callContext.filterSensitivity(sensitivityFlag)
          val mergedState: State = result.get(filteredCallContext) match {
            case Some(previousMergedState) => previousMergedState + state
            case None => state
          }
          result+= (filteredCallContext -> mergedState)

          // Insert ControlPoint relation
          /*val succControlPointSet = controlPointSuccMap.get((node, callContext)).get
          for(succControlPoint <- succControlPointSet)  insertControlPointRelation((node, filteredCallContext), succControlPoint)
          val predControlPointSet = controlPointPredMap.get((node, callContext)).get
          for(predControlPoint <- predControlPointSet)  insertControlPointRelation(predControlPoint, (node, filteredCallContext))*/
        }
      case None =>
    }

    // Insert into the cache.
    cache.put(cacheKey, result)

    // Return the result.
    result
  }

  ////////////////////////////////////////////////////////////////////////////////
  // ControlPoint
  ////////////////////////////////////////////////////////////////////////////////
  // ControlPoint relation. For example, (caller context -> callee context)
  val controlPointSuccMap: MHashMap[ControlPoint, MHashSet[ControlPoint]] = MHashMap() // (pred -> Set[succ])
  val controlPointPredMap: MHashMap[ControlPoint, MHashSet[ControlPoint]] = MHashMap() // (succ -> Set[pred])
  def insertControlPointRelation(pred: ControlPoint, succ: ControlPoint): Unit = {
    // (pred -> Set[succ])
    val succSetOption = controlPointSuccMap.get(pred)
    val succSet = if(succSetOption.isDefined) succSetOption.get else {
      val newSuccSet = new MHashSet[ControlPoint]()
      controlPointSuccMap.put(pred, newSuccSet)
      newSuccSet
    }
    succSet.add(succ)

    // (succ -> Set[pred])
    val predSetOption = controlPointPredMap.get(succ)
    val predSet = if(predSetOption.isDefined) predSetOption.get else {
      val newPredSet = new MHashSet[ControlPoint]()
      controlPointPredMap.put(succ, newPredSet)
      newPredSet
    }
    predSet.add(pred)
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Constructor
  ////////////////////////////////////////////////////////////////////////////////
  {
    // Initialize cache
    // For each node
    for(node <- cfg.getNodes) {
      typing.readTable(node) match {
        case Some(cstate) =>
          // Insert the input CState of the node into the cache
          var cacheKey: cacheKeyType = (node, -1, _MOST_SENSITIVE)
          cache.put(cacheKey, cstate)

          cfg.getCmd(node) match {
            case Block(insts) =>
              // Analyze each instruction
              var normalCState: CState = cstate
              //var exceptionState: State = StateBot
              for(inst <- insts) {
                // Insert the instruction input CState into the cache
                cacheKey = (node, inst.getInstId, _MOST_SENSITIVE)
                cache.put(cacheKey, normalCState)

                val previousNormalCState = normalCState
                normalCState = new IHashMap[CallContext, State]
                //exceptionState = StateBot
                for((callContext, state) <- previousNormalCState) {
                  val (newNormalState, newExceptionState) = semantics.I((node, callContext), inst, state.heap, state.context, HeapBot, ContextBot)
                  normalCState+= (callContext -> State(newNormalState._1, newNormalState._2))
                  //exceptionState+= State(newExceptionState._1, newExceptionState._2)

                  // Insert variable info
                  varManager.insertInfo(node, inst, state)
                }
              }
            case _ =>
          }
        case None =>
      }
    }

    // Initialize ControlPoint relation (copy from semantic.ipSuccMap)
    for((predControlPoint, succControlPointMap) <- semantics.ipSuccMap) {
      for((succControlPoint, _) <- succControlPointMap) {
        insertControlPointRelation(predControlPoint, succControlPoint)
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Dump
  ////////////////////////////////////////////////////////////////////////////////
  def dump(node: Node, inst: CFGInst, cstate: CState): Unit = {
    println("CState count = " + cstate.size)
    for((callContext, state) <- cstate) dump(node, inst, callContext, state)
  }

  def dump(node: Node, inst: CFGInst, callContext: CallContext, state: State): Unit = {
    val nodeStr = node.toString()
    val sb = new StringBuilder

    for (i <- 0 to 60 - nodeStr.length) sb.append("=")
    println("=========================================================================")
    println("========  " + nodeStr + "  " + sb.toString)
    println("- Command")
    println("    [" + inst.getInstId + "] " + inst.toString)

    val callContextStr = "(cc:" + callContext.toString + ")"

    if(state == StateBot) {
      println("Bottom " + callContextStr)
    }
    else {
      print("- Context " + callContextStr + " = ")
      println(DomainPrinter.printContext(0, state._2))

      println("- Heap " + callContextStr)
      println(DomainPrinter.printHeap(4, state._1, cfg))
    }
    println("=========================================================================")
  }
}
