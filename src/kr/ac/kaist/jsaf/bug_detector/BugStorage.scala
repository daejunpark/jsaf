/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import java.util.{HashMap => JMap}
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Queue
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
  private var bugStat: BugStat        = new BugStat(bugDetector)
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
      for (i <- 0 until currentLevel) printf("  ")

      // "call(<>obj<>6, <>fun<>8, <>arguments<>7) @ #8"(at line 8) in function f(at line 6), (env = #8, this = Global)
      printf("%sin %s%s", source, funcName, funcLineNumber)
      if (bugDetector.params.opt_DeveloperMode) printf(", %s", currentCallContext.toString2)
      println()

      // Follow up the trace (Call relation "1(callee) : n(caller)" is possible)
      val controlPointPredSet = stateManager.controlPointPredMap.get((funcId, LEntry), currentCallContext) //semantics.getIPPred((funcId, LEntry), currentCallContext)
      if (controlPointPredSet.isDefined) {
        for (controlPointPred <- controlPointPredSet.get) {
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
  private var funcSet: Set[(String, Loc)] = Set()
  private var nameMap: Map[String, String] = Map() 

  def getRWEntry(node: Node): List[RWEntry] = RWMap.get(node) match {
    case Some(list) => list
    case None => List()
  }  
  def isInternalName(name: String): Boolean = if (name.size > 2 && name.take(2) == "<>") true else false
  def getOriginalName(name: String): String = if (isInternalName(name)) nameMap(name) else name
  def unreadReport(span: Span, isVar: Boolean, name: String, loc: Loc): Unit = {
    if (funcSet contains (name, loc)) funcSet = funcSet - ((name, loc))
    else addMessage(span, UnusedVarProp, null, null, (if (isVar) "variable '" else "property '") + getOriginalName(name) + "'")
  }
  def updateFuncSet(loc: Loc, name: String): Unit = funcSet += ((name, loc))
  def updateNameMap(internalName: String, originalName: String): Unit = nameMap = nameMap + (internalName -> originalName) 
  def updateRWMap(node: Node, readSet: List[RWEntry], writeSet: List[RWEntry]): Unit = RWMap.get(node) match {
    case Some(e) => RWMap += (node -> (e ++ readSet ++ writeSet))
    case None => RWMap += (node -> (readSet ++ writeSet))
  }

/*
  // version 2
  def updateRWMap(node: Node, readSet: List[RWEntry], writeSet: List[RWEntry]): Unit = {
    var newEntries: List[RWEntry] = RWMap.get(node) match {
      case Some(entries) => entries
      case None => List()
    }
    readSet.foreach((e) => check(e._1, e._2, e._3, e._4, e._5))
    writeSet.foreach((e) => check(e._1, e._2, e._3, e._4, e._5))
    RWMap += (node -> newEntries)

    def check(rwflag: Boolean, pvflag: Boolean, loc: Loc, name: String, span: Span) = {
      val newEntry = (rwflag, pvflag, loc, name, span)
      val matchSet = newEntries.filter((e) => (e._2 == pvflag) && (e._3 == loc) && (e._4 == name))
      if (!matchSet.isEmpty) {
        val entry = matchSet.last
        (entry._1, rwflag) match {
          case (true, true) => unreadReport(entry._5, entry._2, entry._4, entry._3)
          case _ => // pass 
        }
        if (matchSet.size > 1) newEntries = newEntries.filterNot(_ == entry)
      }
      newEntries = newEntries :+ newEntry
    }
    //println("RESULT := " + RWMap(node))
  } 
*/

/*
  // version 1
  def updateRWMap(node: Node, readSet: List[RWEntry], writeSet: List[RWEntry]): Unit = {
    var newEntries: List[RWEntry] = RWMap.get(node) match {
      case Some(entries) => entries
      case None => List()
    }
    readSet.foreach((e) => check(e._1, e._2, e._3, e._4, e._5))
    writeSet.foreach((e) => check(e._1, e._2, e._3, e._4, e._5))
    RWMap += (node -> newEntries)

    def check(rwflag: Boolean, pvflag: Boolean, loc: Loc, name: String, span: Span) = {
      newEntries.find((e) => (e._2 == pvflag) && (e._3 == loc) && (e._4 == name)) match {
        case Some(entry) => (entry._1, rwflag) match {
          case (true, true) => unreadReport(entry._5, entry._2, entry._4, entry._3)
          case _ => // pass 
        }; newEntries = newEntries.filterNot(_ == entry) :+ (rwflag, pvflag, loc, name, span)
        case None => newEntries = newEntries :+ (rwflag, pvflag, loc, name, span) 
      }
    }
    println("rwmap := " + RWMap(node))
  } 
*/

  
  ////////////////////////////////////////////////////////////////
  //  UnreachableCode
  ////////////////////////////////////////////////////////////////

  type reachabilityMap = MHashMap[kr.ac.kaist.jsaf.nodes.Node, MHashSet[Node]]
  val reachableAST = new reachabilityMap
  val unreachableAST = new reachabilityMap

  def insertReachabilityAST(ast: kr.ac.kaist.jsaf.nodes.Node, cfgNode: Node, reachable: Boolean): Unit = {
    val selectedAST = if (reachable) reachableAST else unreachableAST
    val asts = new Queue[kr.ac.kaist.jsaf.nodes.Node]
    asts.enqueue(ast)
    while(!asts.isEmpty) {
      val ast = asts.dequeue
      val cfgNodeSet = selectedAST.getOrElseUpdate(ast, new MHashSet)
      if (!cfgNodeSet.contains(cfgNode)) {
        cfgNodeSet.add(cfgNode)
        if (reachable) {
          // Add parent ASTs (All parents are reachable)
          NodeRelation.astParentMap.get(ast) match {
            case Some(parent) => if (!reachableAST.contains(parent)) asts.enqueue(parent)
            case None =>
          }
        }
        else {
          // Add child ASTs (All children are unreachable)
          /*NodeRelation.astChildMap.get(ast) match {
            case Some(children) => for (child <- children) if (!unreachableAST.contains(child)) asts.enqueue(child)
            case None =>
          }*/

          // Check parent AST
          NodeRelation.astParentMap.get(ast) match {
            case Some(parent) =>
              NodeRelation.astChildMap.get(parent) match {
                case Some(children) =>
                  if (!children.exists(child => !unreachableAST.contains(child))) {
                    if (!unreachableAST.contains(parent)) asts.enqueue(parent)
                  }
                case None =>
              }
            case None =>
          }
        }
      }
    }
  }

  def getUnreachableASTList: ListBuffer[ASTNode] = {
    // Debug
    //println("*** unreachableAST ***")
    //printReachability(NodeRelation.astRoot, reachableAST, unreachableAST)

    // Filter reachable nodes again
    val newUnreachableAST1 = unreachableAST.clone
    for((urAST, urCFGNodeSet) <- unreachableAST) {
      reachableAST.get(urAST) match {
        case Some(rCFGNodeSet) => newUnreachableAST1.remove(urAST)
        case None =>
      }
    }

    // Debug
    //println("*** newUnreachableAST1 ***")
    //printReachability(NodeRelation.astRoot, reachableAST, newUnreachableAST1)

    // Filter child nodes
    val newUnreachableAST2 = newUnreachableAST1.clone
    for((urAST, _) <- newUnreachableAST1) {
      NodeRelation.astParentMap.get(urAST) match {
        case Some(parent) =>
          if(isAncestor(parent)) newUnreachableAST2.remove(urAST)

          def isAncestor(ast: kr.ac.kaist.jsaf.nodes.Node): Boolean = {
            var node = ast
            while(true) {
              if(node == null) return false
              if(newUnreachableAST1.contains(node)) return true
              NodeRelation.astParentMap.get(node) match {
                case Some(parent) => node = parent
                case None => return false
              }
            }
            false
          }
        case None =>
      }
    }

    // Debug
    //println("*** newUnreachableAST2 ***")
    //printReachability(NodeRelation.astRoot, reachableAST, newUnreachableAST2)

    // To list
    val list = new ListBuffer[ASTNode]
    for((urAST, _) <- newUnreachableAST2) {
      urAST match {
        case ast: ASTNode => list.append(ast)
        case _ =>
      }
    }

    // Sort by span
    list.sortBy(ast => {
      val span = ast.getInfo.getSpan
      (span.getFileNameOnly, span.getBegin.getLine, span.getBegin.column, span.getEnd.getLine, span.getEnd.column)
    })
  }

  def printReachability(ast: kr.ac.kaist.jsaf.nodes.Node, reachableAST: reachabilityMap, unreachableAST: reachabilityMap): Unit = {
    var indent = 0
    printAST(ast)
    def printAST(ast: kr.ac.kaist.jsaf.nodes.Node): Unit = {
      for(i <- 0 until indent) print(' ')
      print("AST(" + ast.getClass.getSimpleName + "): \"")
      if(ast.isInstanceOf[ASTNode]) print(BugHelper.getOmittedCode(ast.asInstanceOf[ASTNode], 32))
      print("\", rCFG =")
      reachableAST.get(ast) match {
        case Some(rCFGNodeSet) => for (node <- rCFGNodeSet) print(" " + node._2.asInstanceOf[LBlock].id)
        case None =>
      }
      print(", urCFG =")
      unreachableAST.get(ast) match {
        case Some(urCFGNodeSet) => for (node <- urCFGNodeSet) print(" " + node._2.asInstanceOf[LBlock].id)
        case None =>
      }
      println
      NodeRelation.astChildMap.get(ast) match {
        case Some(children) =>
          indent+= 2
          for (child <- children) printAST(child)
          indent-= 2
        case None =>
      }
    }
  }


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

  val detectedFuncMap = new MHashMap[FunctionId, (CFGExpr, MHashSet[Obj], Span)]

  def updateDetectedFuncMap(fid: Int, funExpr: CFGExpr, argObj: Obj, span: Span): Unit = {
    val argObjSet = detectedFuncMap.get(fid) match {
      case Some((_, argObjSet, _)) => argObjSet
      case None =>
        val newArgObjSet = new MHashSet[Obj]
        detectedFuncMap.put(fid, (funExpr, newArgObjSet, span))
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
    val astStmt = getASTNodeFromCFGAssert(assert) match {
      case astStmt: Cond => astStmt
      case astStmt: If => astStmt
      case _ => return
    }
    val resultMap = conditionMap.getOrElseUpdate(astStmt, new MHashMap)
    if (!change && resultMap.get((node, assert)).isDefined) return
    resultMap.put((node, assert), result)
  }

  def getASTNodeFromCFGAssert(assert: CFGAssert): ASTNode = {
    NodeRelation.cfg2astMap.get(assert) match {
      case Some(astNode) => return NodeRelation.getParentASTStmtOrCond(astNode)
      case None =>
    }
    null
  }



  ////////////////////////////////////////////////////////////////
  //  Function expressions
  ////////////////////////////////////////////////////////////////

  private val funExprMap = new MHashMap[FunctionId, CFGFunExpr]

  def insertFunExpr(fid: FunctionId, funExpr: CFGFunExpr): Unit = funExprMap.put(fid, funExpr)
  def getFunExpr(fid: FunctionId): CFGFunExpr = funExprMap.getOrElse(fid, null)
}

class BugInfo(val span: Span, val bugKind: Int, val arg1: String, val arg2: String) {}
