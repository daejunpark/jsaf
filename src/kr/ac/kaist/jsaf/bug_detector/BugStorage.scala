/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import java.util.{HashMap => JMap}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.nodes_util.SourceLoc
import kr.ac.kaist.jsaf.bug_detector._

class BugStorage(fileMap: JMap[String, String]) {

  ////////////////////////////////////////////////////////////////
  //  BugMessage helper functions
  ////////////////////////////////////////////////////////////////

  private var bugList: BugList  = List()
  private var bugStat: BugStat  = new BugStat()
  private var isSorted: Boolean = false

  private def sortMessage(): Unit = if (!isSorted) {
    bugList = bugList.sortBy((bug) => (bug._1, bug._2.getLine, bug._2.column))
    isSorted = true
  }

  private def formatMsg(bugType: BugType, msg: String): String = {
    if (bugType == Warning) "[Warning] " + msg
    else if (bugType == TypeError) "[TypeError] " + msg
    else if (bugType == ReferenceError) "[ReferenceError] " + msg
    else msg
  }
  
  def addMessage0(span: Span, bugKind: BId): Unit = {
    val (bugType, bugMsg, argNum) = bugTable(bugKind)
    if (0 < argNum) System.out.println("Warning, BugInfo. Bug '%d' provides less arguments to addMessage.".format(bugKind))
    else bugList = bugList :+ (span.getFileName, span.getBegin, span.getEnd, bugType, bugMsg)
    bugStat.increaseBugCounter(bugKind, bugType)
    isSorted = false
  }

  def addMessage1(span: Span, bugKind: BId, arg1: String): Unit = {
    val (bugType, bugMsg, argNum): (Int, String, Int) = bugTable(bugKind)
    if (argNum == 1) bugList = bugList :+ (span.getFileName, span.getBegin, span.getEnd, bugType, bugMsg.format(arg1))
    else if (argNum < 1) System.out.println("Warning, BugInfo. Bug #%d provides less arguments to addMessage.".format(bugKind))
    else System.out.println("Warning, BugInfo. Bug #%d provides more arguments to addMessage.".format(bugKind))
    bugStat.increaseBugCounter(bugKind, bugType)
    isSorted = false
  }

  def addMessage2(span: Span, bugKind: BId, arg1: String, arg2: String): Unit = {
    val (bugType, bugMsg, argNum): (Int, String, Int) = bugTable(bugKind)
    if (argNum == 2) bugList = bugList :+ (span.getFileName, span.getBegin, span.getEnd, bugType, bugMsg.format(arg1, arg2))
    else if (argNum < 2) System.out.println("Warning, BugInfo. Bug #%d provides less arguments to addMessage.".format(bugKind))
    else System.out.println("Warning, BugInfo. Bug #%d provides more arguments to addMessage.".format(bugKind))
    bugStat.increaseBugCounter(bugKind, bugType)
    isSorted = false
  }

  def reportDetectedBugs(quiet: Boolean):Unit = {
    if (!isSorted) sortMessage
    bugList.distinct.foreach((bug) => System.out.println("%s:%d:%d~%d:%d: %s".format(bug._1, 
      bug._2.getLine, bug._2.column, bug._3.getLine, bug._3.column, formatMsg(bug._4, bug._5))))
    bugStat.reportBugStatistics(quiet)
  }



  ////////////////////////////////////////////////////////////////
  //  CallConstFunc
  //  funcMap : Store read and written variables & properties
  ////////////////////////////////////////////////////////////////

  private var funcMap: Map[FunctionId, Set[Info]] = Map()
  def updateFuncMap(fidSet: FidSet, info: Info): Unit = fidSet.foreach((fid) =>
    funcMap.get(fid) match {
      case Some(infoSet) => funcMap = funcMap + (fid -> (infoSet + info))
      case None => funcMap = funcMap + (fid -> Set(info)) 
  })
  def getInfoSet(fid: FunctionId): Set[Info] = funcMap(fid)



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
  def sortRange(): Unit = range = range.sortBy((span) => (span.getFileName, span.getBegin.getLine, span.getBegin.column))
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

  private var detectedFuncMap: Map[FunctionId, (Obj, Set[Span])] = Map()

  def updateDetectedFuncMap(fid: Int, argObj: Obj, span: Span): Unit = {
    detectedFuncMap.get(fid) match {
      case Some((obj, spans)) => detectedFuncMap = detectedFuncMap + (fid -> ((obj + argObj), (spans + span)))
      case None => detectedFuncMap = detectedFuncMap + (fid -> (argObj, Set(span)))
  }}
  def applyToDetectedFuncMap(fun:(FunctionId, Obj, Set[Span]) => Unit): Unit = 
    detectedFuncMap.foreach((elem) => fun(elem._1, elem._2._1, elem._2._2))



  ////////////////////////////////////////////////////////////////
  //  BugStat (record bug detection time)
  ////////////////////////////////////////////////////////////////

  def recordStartTime(time: Long): Unit = bugStat.setStartTime(time)
  def recordEndTime(time: Long): Unit = bugStat.setEndTime(time)
}

class BugInfo(val span: Span, val bugKind: Int, val arg1: String, val arg2: String) {}
