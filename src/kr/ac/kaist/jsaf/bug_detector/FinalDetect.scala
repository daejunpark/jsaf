/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import scala.collection.mutable.{HashMap => MHashMap}
import scala.collection.mutable.{HashSet => MHashSet}
import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.analysis.typing.models.ModelManager
import kr.ac.kaist.jsaf.nodes.ASTNode
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.JSAstToConcrete
import kr.ac.kaist.jsaf.nodes_util.NodeFactory
import kr.ac.kaist.jsaf.nodes_util.NodeRelation
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.nodes_util.SourceLoc
import kr.ac.kaist.jsaf.nodes.Cond
import kr.ac.kaist.jsaf.nodes.If
import kr.ac.kaist.jsaf.nodes.Expr
import kr.ac.kaist.jsaf.scala_src.nodes._

class FinalDetect(bugDetector: BugDetector) {
  val cfg           = bugDetector.cfg
  val typing        = bugDetector.typing
  val callGraph     = bugDetector.callGraph
  val shadowings    = bugDetector.shadowings
  val semantics     = bugDetector.semantics
  val bugStorage    = bugDetector.bugStorage
  val bugOption     = bugDetector.bugOption
  val varManager    = bugDetector.varManager
  val libMode       = bugDetector.libMode



  ////////////////////////////////////////////////////////////////
  // Bug Detection Main (final check)
  ////////////////////////////////////////////////////////////////

  def check(): Unit = {
    callConstFuncCheck
    conditionalBranchCheck
    shadowingCheck
    unreachableCodeCheck
    unusedFunctionCheck
    //unusedVarPropCheck
    varyingTypeArgumentsCheck
  }



  ////////////////////////////////////////////////////////////////
  // CallConstFunc Check 
  ////////////////////////////////////////////////////////////////

  private def callConstFuncCheck(): Unit = {
    // callMap   : set of fids used as a function
    // constMap  : set of fids used as a constructor
    val callMap = new MHashMap[FunctionId, CFGExpr]
    val constMap = new MHashMap[FunctionId, CFGExpr]
    for ((inst, fidSet) <- callGraph) {
      inst match {
        case CFGCall(_, _, constExpr, _, _, _) => fidSet.foreach(fid => callMap.put(fid, constExpr))
        case CFGConstruct(_, _, funExpr, _, _, _) => fidSet.foreach(fid => constMap.put(fid, funExpr))
      }
    }
    val commonFidSet = callMap.keySet & constMap.keySet
    commonFidSet.foreach((fid) => bugStorage.addMessage(cfg.getFuncInfo(fid).getSpan, CallConstFunc, null, null, getFuncName(cfg.getFuncName(fid), varManager, callMap(fid))))
  }



  ////////////////////////////////////////////////////////////////
  //  ConditionalBranch Check
  ////////////////////////////////////////////////////////////////

  def conditionalBranchCheck(): Unit = {
    // Insert left CFGAsserts
    bugDetector.traverseInsts((node, inst) => {
      inst match {
        case inst: CFGAssert => bugStorage.insertConditionMap(node, inst, BoolBot, false)
        case _ =>
      }
    })

    for ((astStmt, resultSet) <- bugStorage.conditionMap) {
      val condExpr = astStmt match {
        case SCond(_, condExpr, _, _) => condExpr
        case SIf (_, condExpr, _, _) => condExpr
        case _ => null
      }
      if (condExpr != null) {
        //System.out.println(astStmt + ", (" + JSAstToConcrete.doit(condExpr) + ")")
        for (((node, assert), result) <- resultSet) {
          // Get a CFGAssert instruction in node
          def getAssertInst(node: Node): CFGAssert = {
            cfg.getCmd(node) match {
              case Block(insts) if (insts.length > 0 && insts.head.isInstanceOf[CFGAssert]) =>
                val assert = insts.head.asInstanceOf[CFGAssert]
                if (bugStorage.getASTNodeFromCFGAssert(assert) == astStmt) assert else null
              case _ => null
            }
          }

          // Check whether the node is leaf or not
          val level1Succ = new MHashSet[Node] ++ cfg.getAllSucc(node)
          val level2Succ = new MHashSet[Node]
          for (node <- level1Succ) level2Succ++= (cfg.getAllSucc(node))
          val isLeaf = !(level1Succ ++ level2Succ).exists(succNode => getAssertInst(succNode) != null)
          //System.out.println(assert + " = " + result + ", isLeaf = " + isLeaf)
          //System.out.println(node + "'s level1Succ = " + level1Succ)
          //System.out.println(node + "'s level2Succ = " + (level2Succ -- level1Succ))

          if (isLeaf) {
            var rootAssertInst: CFGAssert = null
            def followUp(node: Node, level: Int): Unit = {
              val assertInst = getAssertInst(node)
              if (assertInst == null) {
                val predNodes = cfg.getAllPred(node)
                if (level == 1 && predNodes.size > 0) for (predNode <- predNodes) followUp(predNode, 2)
                else bugStorage.addMessage(condExpr.getInfo.getSpan, CondBranch, rootAssertInst, null, JSAstToConcrete.doit(condExpr), rootAssertInst.flag.toString)
                return
              }
              else rootAssertInst = assertInst
              //System.out.println("  > node = " + node + ", " + assertInst + " = " + resultSet.get((node, assertInst)))

              resultSet.get((node, assertInst)) match {
                case Some(result) => if (result != BoolTrue) return
                case None => return
              }

              for (predNode <- cfg.getAllPred(node)) followUp(predNode, 1)
            }
            followUp(node, 1)
          }
        }
      }
    }
  }



  ////////////////////////////////////////////////////////////////
  // Shadowing Check
  ////////////////////////////////////////////////////////////////

  private def shadowingCheck(): Unit = shadowings.foreach((bug) => bugStorage.addMessage(bug.span, bug.bugKind, null, null, bug.arg1, bug.arg2))



  ////////////////////////////////////////////////////////////////
  // UnreachableCode Check
  ////////////////////////////////////////////////////////////////

  private def unreachableCodeCheck(): Unit = {
    val astList = bugStorage.getUnreachableASTList
    var (prevSpan, prevCode): (Span, String) = (null, null)
    for (ast <- astList) {
      // Span, Code
      val span = ast.getInfo.getSpan
      val code = getOmittedCode(ast)

      // Debug
      //println(span + " : " + code)

      if(prevSpan == null || prevCode == null) {prevSpan = span; prevCode = code}
      else if(equals(prevSpan, span)) {if(prevCode.length < code.length) {prevSpan = span; prevCode = code}}
      else if(contains(prevSpan, span)) Unit
      else if(contains(span, prevSpan)) {prevSpan = span; prevCode = code}
      // else TODO: continuous ASTs!
      else {report; prevSpan = span; prevCode = code}
    }
    report

    def report(): Unit = {
      if (prevSpan == null || prevCode == null) return
      bugStorage.addMessage(prevSpan, UnreachableCode, null, null, prevCode)
      prevSpan = null; prevCode = null
    }
    def equals(a: Span, b: Span): Boolean = a.getBegin == b.getBegin && a.getEnd == b.getEnd
    def contains(a: Span, b: Span): Boolean = {
      val (aS, aE, bS, bE) = (a.getBegin, a.getEnd, b.getBegin, b.getEnd)
      val (aSL, aSC, aEL, aEC) = (aS.getLine, aS.column, aE.getLine, aE.column) // Start(Line, Column) ~ End(Line ,Column)
      val (bSL, bSC, bEL, bEC) = (bS.getLine, bS.column, bE.getLine, bE.column) // Start(Line, Column) ~ End(Line ,Column)
      (aSL < bSL || aSL == bSL && aSC <= bSC) && (aEL > bEL || aEL == bEL && aEC >= bEC) // a contains b
    }
  }



  ////////////////////////////////////////////////////////////////
  // UnusedFunction Check
  ////////////////////////////////////////////////////////////////

  private def unusedFunctionCheck(): Unit = {
    val fidSet = cfg.getFunctionIds.filterNot(fid => typing.builtinFset.contains(fid))
    for (fid <- bugStorage.filterUsedFunctions(fidSet.toList)) {
      if (typing.getStateAtFunctionEntry(fid).isEmpty) {
        val funExpr: CFGFunExpr = bugStorage.funExprMap.getOrElse(fid, null)
        val bugKind = if (libMode) UnreferencedFunction else UncalledFunction
        bugStorage.addMessage(cfg.getFuncInfo(fid).getSpan, bugKind, null, null, getFuncName(cfg.getFuncName(fid), varManager, funExpr))
        bugStorage.appendUnusedFunction(fid)
      }
    }

    /* Previous code
    val funcCount = cfg.getFuncCount
    val fidSet    = (0 until funcCount toSet) filterNot (typing.builtinFset contains)

    for (fid <- bugStorage.filterUsedFunctions(fidSet toList)) {
      if (typing.getStateAtFunctionEntry(fid).isEmpty) {
        bugStorage.addMessage(cfg.getFuncInfo(fid).getSpan, (if (libMode) UnreferencedFunction else UncalledFunction), null, null, getFuncName(cfg.getFuncName(fid)))
        bugStorage.appendUnusedFunction(fid)
      }
    }
    */
  }



  ////////////////////////////////////////////////////////////////
  // UnusedVarProp Check
  ////////////////////////////////////////////////////////////////

  private def unusedVarPropCheck(): Unit = {
    val startNode: Node = (cfg.getGlobalFId, LEntry)
    var worklist: List[Node] = List()
    var fixpoint: RWMap = Map()
    var count = 0

    worklist = getSuccs(startNode)
    while (!worklist.isEmpty) {
      count = count + 1
      val curr = worklist.head
      worklist = worklist.tail
      val curStat = getStat(curr)
      val newStat = findUnread(curStat, curr)
      val succ = getSuccs(curr)
      succ.foreach((node) => {
        if (newStat != getStat(node)) {
          worklist = worklist :+ node
          addStat(node, newStat)
        }
      })
    }

    getStat((cfg.getGlobalFId, LExit)).foreach((e) => if (e._1) bugStorage.unreadReport(e._5, e._2, e._4, e._3))

    def addStat(node: Node, list: List[RWEntry]) = fixpoint += (node -> list)
    def getStat(node: Node): List[RWEntry] = fixpoint.get(node) match {
      case Some(entry) => entry
      case None => List()
    }

    def findUnread(curStat: List[RWEntry], current: Node): List[RWEntry] = {
      val currRW = bugStorage.getRWEntry(current)
      val retStat = currRW.foldLeft[List[RWEntry]](curStat)((list, rw) => list.find((e) => (e._2 == rw._2) && (e._3 == rw._3) && (e._4 == rw._4)) match {
        case Some(entry) => (entry._1, rw._1) match {
          case (true, true) => bugStorage.unreadReport(entry._5, entry._2, entry._4, entry._3)
          case _ => 
        }; list.filterNot(_ == entry) :+ rw
        case None => list :+ rw
      })
      retStat  
    }

    def getSuccs(current: Node): List[Node] = {
      typing.getStateAfterNode(current).keys.foldLeft[List[Node]](cfg.getSucc(current) toList)((list, callContext) =>
        semantics.getIPSucc((current, callContext)) match {
          case Some(ccMap) => ccMap.keys.foldLeft(list)((l, k) => l :+ k._1)
          case None => list
    })}
  }




  ////////////////////////////////////////////////////////////////
  // VaryingTypeArguments Check 
  ////////////////////////////////////////////////////////////////

  private def varyingTypeArgumentsCheck(): Unit = {
    for ((fid, (funExpr, argObjSet, span)) <- bugStorage.detectedFuncMap) {
      // function name, expected maximum argument length
      val funcName = cfg.getFuncName(fid)
      val funcArgList = cfg.getArgVars(fid)
      val (funcArgLen, isBuiltinFunc) = ModelManager.getFIdMap("Builtin").get(fid) match {
        case Some(builtinFuncName) => (argSizeMap(builtinFuncName)._2, true)
        case None => (funcArgList.length, false)
      }
      //println("- " + funcName + "(" + funcArgLen + ") #" + fid)

      // maximum argument length
      var maxArgObjLength = 0
      val argObjLengthMap = new MHashMap[Obj, Int]()
      for (argObj <- argObjSet) {
        val (propValue, _) = argObj("length")
        propValue.objval.value.pvalue.numval.getConcreteValue match {
          case Some(lengthPropDouble) =>
            val lengthPropInt = lengthPropDouble.toInt
            if (maxArgObjLength < lengthPropInt) maxArgObjLength = lengthPropInt
            argObjLengthMap.put(argObj, lengthPropInt)
          case None => // TODO?
        }
      }
      //println("  maximum argument length = " + maxArgObjLength)

      //println("funcName = " + funcName)
      //println("funcArgList.length = " + funcArgList.length)
      //println("maxArgObjLength = " + maxArgObjLength)
      // for each argument index 0, 1, 2, ...
      for (i <- 0 until maxArgObjLength) {
        var joinedValue: Value = ValueBot
        for ((argObj, argObjLength) <- argObjLengthMap) {
          if (argObjLength <= maxArgObjLength) {
            val (propValue, _) = argObj(i.toString)
            joinedValue+= propValue.objval.value
            //println("  argObj[" + i + "] = " + propValue.objval.value)
          }
        }
        var joinedValueTypeCount = joinedValue.pvalue.typeCount
        if (!bugOption.VaryingTypeArguments_CheckUndefined && joinedValueTypeCount > 1 && joinedValue.pvalue.undefval != UndefBot) joinedValueTypeCount-= 1
        val isBug: Boolean = (joinedValueTypeCount > 1 || joinedValueTypeCount == 1 && joinedValue.locset.size > 0)
        if (isBug) {
          var typeKinds: String = joinedValue.typeKinds
          if (isBuiltinFunc || i >= funcArgList.length) {
            // Built-in function
            val ordinal = i + 1 match {
              case 1 => "1st"
              case 2 => "2nd"
              case _ => i + "th"
            }
            bugStorage.addMessage(span, VaryingTypeArguments, null, null, getFuncName(funcName, varManager, funExpr), ordinal + " ", "", typeKinds)
          }
          else {
            // User function
            val argName = funcArgList(i) match {
              case CFGUserId(_, _, _, originalName, _) => originalName
              case CFGTempId(text, _) => text
            }
            bugStorage.addMessage(span, VaryingTypeArguments, null, null, getFuncName(funcName, varManager, funExpr), "", "'" + argName + "' ", typeKinds)
          }
          //println("    joined argObj[" + i + "] = " + joinedValue + ", isBug = " + isBug)
        }
      }
    }
    /* Previous code
    bugStorage.applyToDetectedFuncMap((fid: FunctionId, obj: Obj, spanSet: Set[Span]) => {
      val arglen = cfg.getArgVars(fid).size
      val isBug = (0 until arglen).foldLeft(false)((b, i) => b || (1 != obj(i.toString)._1._1._1.typeCount))
      if (isBug) bugStorage.addMessage(cfg.getFuncInfo(fid).getSpan, VaryingTypeArguments, null, null, getFuncName(cfg.getFuncName(fid)))
    })
    */
  }
}
