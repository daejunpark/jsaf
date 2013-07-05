/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import java.util.{HashMap => JMap}
import scala.collection.mutable.Stack
import scala.collection.mutable.{HashMap => MHashMap}
import scala.collection.mutable.{HashSet => MHashSet}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.CallContext
import kr.ac.kaist.jsaf.analysis.typing.ControlPoint
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes.ASTNode
import kr.ac.kaist.jsaf.nodes_util.NodeFactory
import kr.ac.kaist.jsaf.nodes_util.NodeRelation
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.nodes_util.SourceLoc
import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.nodes.Cond
import kr.ac.kaist.jsaf.nodes.If

class BugStorage(bugDetector: BugDetector, fileMap: JMap[String, String]) {
  val cfg          = bugDetector.cfg
  val semantics    = bugDetector.semantics
  val stateManager = bugDetector.stateManager



  ////////////////////////////////////////////////////////////////
  //  BugMessage helper functions
  ////////////////////////////////////////////////////////////////

  private var bugList: BugList        = List()
  private var bugStat: BugStat        = new BugStat(bugDetector.libMode)
  private var isSorted: Boolean       = false
  private var traceMap: TraceMap      = Map()

  private def sortMessage(): Unit = if (!isSorted) {
    bugList = bugList.sortBy((bug) => (bug._2, bug._5, bug._3.getLine, bug._3.column))
    isSorted = true
  }

  private def formatMsg(bugType: BugType, msg: String): String = {
    if (bugType == Warning) "[Warning] " + msg
    else if (bugType == TypeError) "[TypeError] " + msg
    else if (bugType == ReferenceError) "[ReferenceError] " + msg
    else msg
  }

  private def isRedundant(span: Span, bugType: BugType, bugMsg: String): Boolean = 
    bugList find {e => e._2 == span.getFileNameOnly && e._3 == span.getBegin && e._4 == span.getEnd  && e._5 == bugType && e._6 == bugMsg} match {
      case Some(_) => true
      case None => false
    }

  def addMessage(span: Span, bugKind: BugKind, inst: CFGInst, callContext: CallContext, args: Any*): Unit = {
    val (bugType, bugMsg, argNum) = bugTable(bugKind)

    // Check argument count
    if (args.length != argNum) {
      System.out.println("Warning, addMessage@BugStorage. Bug #" + bugKind + " provides " + (if (args.length < argNum) "less" else "more") + " arguments to addMessage.")
      return
    }

    // Build a formatted bug message with arguments
    val fullBugMsg: String = argNum match {
      case 0 => bugMsg
      case 1 => bugMsg.format(args(0))
      case 2 => bugMsg.format(args(0), args(1))
      case 3 => bugMsg.format(args(0), args(1), args(2))
      case 4 => bugMsg.format(args(0), args(1), args(2), args(3))
      case 5 => bugMsg.format(args(0), args(1), args(2), args(3), args(4))
      case _ => "Warning, addMessage@BugStorage. No such bugs with '%d' arguments.".format(argNum)
    }

    if (!isRedundant(span, bugType, fullBugMsg)) {
      val newBId = newBugId
      bugStat.increaseBugCounter(bugKind, bugType)
      bugList = bugList :+ (newBId, span.getFileNameOnly, span.getBegin, span.getEnd, bugType, fullBugMsg)
      if (inst != null && callContext != null) traceMap += (newBId -> (inst, callContext)) 
      isSorted = false
    }
  }

  def reportDetectedBugs(errorOnly: Boolean, quiet: Boolean): Unit = {
    val isTracingMode = bugDetector.params.opt_ContextTrace

    // Sort message list if not.
    if (!isSorted) sortMessage

    val errorList = bugList.filterNot((bug) => bug._4 == Warning)
    if (errorOnly && !errorList.isEmpty) {  // Show error messages only.
      val warningCount = bugList.size - errorList.size
      errorList.foreach((bug) => printBug(bug))
      System.out.println("%d warnings will be shown after errors are fixed...".format(warningCount))
    } else {  // Show all messages.
      bugList.foreach((bug) => printBug(bug))
    }
    // Report Statistics.
    bugStat.reportBugStatistics(quiet)

    // Print specific bug information.
    def printBug(bug: BugEntry): Unit = {
      val (bugId, fileName, spanBegin, spanEnd, bugType, bugMessage) = bug
      System.out.println("%s:%d:%d~%d:%d: %s".format(fileName, spanBegin.getLine, spanBegin.column, spanEnd.getLine, spanEnd.column, formatMsg(bugType, bugMessage)))
      if (isTracingMode) traceMap.get(bugId) match {
        case Some(traceInfo: TraceEntry) => printContextTrace(traceInfo._1, traceInfo._2)
        case None => Unit
      }
    }
  }

  // Print call-context trace to a bug.
  def printContextTrace(inst: CFGInst, callContext: CallContext): Unit = {
    val traceStack = new Stack[(Int, ControlPoint, CFGInst)]()
    traceStack.push((0, (cfg.findEnclosingNode(inst), callContext), inst))
    while (!traceStack.isEmpty) {
      val (currentLevel, (currentNode, currentCallContext), currentInst) = traceStack.pop

      // Instruction info
      val currentInst2: CFGInst = if (currentInst != null) currentInst else {
        cfg.getCmd(currentNode) match {
          case Block(insts) => insts.last
          case _ => null
        }
      }
      val source: String = if (currentInst2 == null) "" else {
        val instSpanString = currentInst2.getInfo match {
          case Some(info) => "(at line " + info.getSpan().getBegin().getLine() + ")"
          case None => ""
        }
        "\"" + currentInst2.toString() + "\"" + instSpanString + " "
      }

      // Function info
      val funcId = currentNode._1
      val funcName = if (funcId == cfg.getGlobalFId) "global function"
        else {
          var tempFuncName = cfg.getFuncName(funcId)
          val index = tempFuncName.indexOf("<>")
          if (index != -1) tempFuncName = tempFuncName.substring(0, index)
          "function " + tempFuncName
        }
      val funcSpan = cfg.getFuncInfo(funcId).getSpan()
      val funcSpanBegin = funcSpan.getBegin()
      val funcSpanEnd = funcSpan.getEnd()
      val funcLineNumber = if (funcId == cfg.getGlobalFId) "" else "(at line " + funcSpanBegin.getLine + ")"

      printf("  Context trace: [%d] ", currentLevel)
      for(i <- 0 until currentLevel) printf("  ")

      // "call(<>obj<>6, <>fun<>8, <>arguments<>7) @ #8"(at line 8) in function f(at line 6), (env = #8, this = Global)
      printf("%sin %s%s", source, funcName, funcLineNumber)
      if (bugDetector.params.opt_DeveloperMode) printf(", %s", currentCallContext.toString2)
      println()

      // Follow up the trace (Call relation "1(callee) : n(caller)" is possible)
      val controlPointPredSet = stateManager.controlPointPredMap.get((funcId, LEntry), currentCallContext) //semantics.getIPPred((funcId, LEntry), currentCallContext)
      if (controlPointPredSet.isDefined) {
        for(controlPointPred <- controlPointPredSet.get) {
          traceStack.push((currentLevel + 1, controlPointPred, null))
        }
      }
    }
  }



  ////////////////////////////////////////////////////////////////
  //  UnusedVarProp
  //  RWMap   : Store read and written variables & properties
  //  nameMap : Map internal name to its original name
  ////////////////////////////////////////////////////////////////

  private var RWMap: RWMap = Map()
  private var nameMap: Map[String, String] = Map() 

  def getRWEntry(node: Node): Option[List[RWEntry]] = RWMap.get(node) 
  def isInternalName(name: String): Boolean = if (name.size > 2 && name.take(2) == "<>") true else false
  def getOriginalName(name: String): String = if (isInternalName(name)) nameMap(name) else name
  def updateNameMap(internalName: String, originalName: String): Unit = nameMap = nameMap + (internalName -> originalName) 
  def updateRWMap(node: Node, rwflag: Boolean, pvflag: Boolean, name: String, loc: Loc, span: Span): Unit = {
    RWMap.get(node) match {
      case Some(entries) => RWMap = RWMap + (node -> (entries :+ (rwflag, pvflag, loc, name, span)))
      case None => RWMap = RWMap + (node -> List((rwflag, pvflag, loc, name, span)))
    }
  }


  
  ////////////////////////////////////////////////////////////////
  //  UnreachableCode
  //  range     : Store span of unreachable instructions
  //  sortRange : Sort the span list according to 
  //              (filename, beginLine, beginColumn) order.
  ////////////////////////////////////////////////////////////////

  private var range : List[Span] = List()

  def appendUnreachableInstruction(span: Span): Unit = range = range :+ span
  def isRangeEmpty(): Boolean = if (range isEmpty) true else false
  def sortRange(): Unit = range = range.sortBy((span) => (span.getFileNameOnly, span.getBegin.getLine, span.getBegin.column))
  def getRangeHead(): Span = range.head
  def getRangeTail(): List[Span] = range.tail



  ////////////////////////////////////////////////////////////////
  //  UnusedFunction
  //  usedFunctions   : Store fids of used function
  ////////////////////////////////////////////////////////////////

  private var usedFunctions: Set[Int] = Set()
  private var unusedFunctions: Set[Int] = Set()

  def appendUsedFunction(fid: Int): Unit = usedFunctions += fid
  def appendUnusedFunction(fid: Int): Unit = unusedFunctions += fid
  def filterUsedFunctions(fidSet: List[Int]): List[Int] = fidSet filterNot (usedFunctions.toList contains)



  ////////////////////////////////////////////////////////////////
  //  VaryingTypeArguments
  //  detectedFuncMap : Store fids of functions with varying
  //                    type arguments
  ////////////////////////////////////////////////////////////////

  val detectedFuncMap = new MHashMap[FunctionId, (MHashSet[Obj], Span)]

  def updateDetectedFuncMap(fid: Int, argObj: Obj, span: Span): Unit = {
    val argObjSet = detectedFuncMap.get(fid) match {
      case Some((argObjSet, _)) => argObjSet
      case None =>
        val newArgObjSet = new MHashSet[Obj]
        detectedFuncMap.put(fid, (newArgObjSet, span))
        newArgObjSet
    }
    argObjSet.add(argObj)
  }
  /*def applyToDetectedFuncMap(fun:(FunctionId, Obj, Set[Span]) => Unit): Unit = 
    detectedFuncMap.foreach((elem) => fun(elem._1, elem._2._1, elem._2._2))*/



  ////////////////////////////////////////////////////////////////
  //  BugStat (record bug detection time)
  ////////////////////////////////////////////////////////////////

  def recordStartTime(time: Long): Unit = bugStat.setStartTime(time)
  def recordEndTime(time: Long): Unit = bugStat.setEndTime(time)



  ////////////////////////////////////////////////////////////////
  //  Conditional expression(CFGAssert) result collection
  ////////////////////////////////////////////////////////////////

  val conditionMap = new MHashMap[ASTNode, MHashMap[(Node, CFGAssert), AbsBool]]()

  def insertConditionMap(node: Node, assert: CFGAssert, result: AbsBool, change: Boolean = true): Unit = {
    val astStmt = getASTNodefromCFGAssert(assert) match {
      case astStmt: Cond => astStmt
      case astStmt: If => astStmt
      case _ => return
    }
    val resultMap = conditionMap.getOrElseUpdate(astStmt, new MHashMap)
    if(!change && resultMap.get((node, assert)).isDefined) return
    resultMap.put((node, assert), result)
  }

  def getASTNodefromCFGAssert(assert: CFGAssert): ASTNode = {
    NodeRelation.cfg2astMap.get(assert) match {
      case Some(astNode) =>
        NodeRelation.getParentASTStmtOrCond(astNode) match {
          case astStmt: ASTNode => return astStmt
          case _ =>
        }
      case None =>
    }
    null
  }
}

class BugInfo(val span: Span, val bugKind: Int, val arg1: String, val arg2: String) {}
