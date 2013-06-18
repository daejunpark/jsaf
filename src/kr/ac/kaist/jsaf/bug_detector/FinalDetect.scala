/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import scala.collection.mutable.{HashMap => MHashMap}
import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.analysis.typing.models.ModelManager
import kr.ac.kaist.jsaf.nodes.AbstractNode
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.JSAstToConcrete
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
        case CFGCall(_,_,_,_,_,_) => (fidSetPair._1 ++ calledFunction._2, fidSetPair._2)
        case CFGConstruct(_,_,_,_,_,_) => (fidSetPair._1, fidSetPair._2 ++ calledFunction._2)
      })
    val bothUsed = callSet & constSet 
    if (!bothUsed.isEmpty) bothUsed.foreach((fid) => bugStorage.addMessage(cfg.getFuncInfo(fid).getSpan, CallConstFunc, null, null, getFuncName(cfg.getFuncName(fid))))
  }



  ////////////////////////////////////////////////////////////////
  //  ConditionalBranch Check
  ////////////////////////////////////////////////////////////////

  def conditionalBranchCheck(): Unit = {
    // Remap ASTExpr to ASTStmt
    bugDetector.traverseInsts((node, inst) => {
      // Get CFGInfo
      inst.getInfo match {
        case Some(info) =>
          // Get ASTStmt and ASTExpr
          val astStmt = info.getAst
          val condExpr = astStmt match {
            case SCond(_, condExpr, _, _) => condExpr
            case SIf(_, condExpr, _, _) => condExpr
            case _ => null
          }
          if(condExpr != null) {
            // Walk ASTExpr
            def walkExpr(expr: Expr): Unit = {
              bugStorage.remapConditionMap(expr, astStmt)
              expr match {
                case SExprList(_, exprList) => for(expr <- exprList) walkExpr(expr)
                case SCond(_, condExpr, trueBranchExpr, falseBranchExpr) => walkExpr(condExpr); walkExpr(trueBranchExpr); walkExpr(falseBranchExpr)
                case SInfixOpApp(_, leftExpr, _, rightExpr) => walkExpr(leftExpr); walkExpr(rightExpr)
                case SPrefixOpApp(_, _, expr) => walkExpr(expr)
                case SAssignOpApp(_, _, _, expr) => walkExpr(expr)
                case _ =>
              }
            }
            walkExpr(condExpr)
          }
        case None =>
      }
    })

    for((stmt, resultSet) <- bugStorage.conditionMap) {
      val condExpr = stmt match {
        case SCond(_, condExpr, _, _) => condExpr
        case SIf(_, condExpr, _, _) => condExpr
        case _ => null
      }
      if(condExpr != null) {
        //System.out.println(stmt + ", (" + JSAstToConcrete.doit(condExpr) + ")")
        for(((node, assert), result) <- resultSet) {
          // Get a CFGAssert instruction in node
          def getAssertInst(node: Node): CFGAssert = {
            cfg.getCmd(node) match {
              case Block(insts) if(insts.length > 0 && insts.head.isInstanceOf[CFGAssert] &&
                bugStorage.getRemappedASTNode(insts.head.asInstanceOf[CFGAssert].info.getAst) == stmt) => insts.head.asInstanceOf[CFGAssert]
              case _ => null
            }
          }

          // Check whether the node is leaf or not
          val isLeaf = !cfg.getSucc(node).exists(succNode => getAssertInst(succNode) != null)
          //System.out.println(assert + " = " + result + ", isLeaf = " + isLeaf)

          if(isLeaf) {
            var rootAssertInst: CFGAssert = null
            def followUp(node: Node): Unit = {
              val assertInst = getAssertInst(node)
              if(assertInst == null) {
                bugStorage.addMessage(condExpr.getInfo.getSpan, CondBranch, rootAssertInst, null, JSAstToConcrete.doit(condExpr), rootAssertInst.flag.toString)
                return
              }
              else rootAssertInst = assertInst
              //System.out.println("  > node = " + node + ", " + assertInst + " = " + resultSet.get((node, assertInst)))

              resultSet.get((node, assertInst)) match {
                case Some(result) => if(result != BoolTrue) return
                case None => return
              }

              for(predNode <- cfg.getPred(node)) followUp(predNode)
            }
            followUp(node)
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
    if (!bugStorage.isRangeEmpty) {
      bugStorage.sortRange
      val head = bugStorage.getRangeHead
      var chunkBegin = head.getBegin
      var chunkEnd   = head.getEnd
      for (span <- bugStorage.getRangeTail) {
        val spanBegin = span.getBegin
        val spanEnd   = span.getEnd
        if ((chunkEnd.getLine.toInt < spanBegin.getLine.toInt) || ((chunkEnd.getLine.toInt == spanBegin.getLine.toInt) && (chunkEnd.column.toInt < spanBegin.column.toInt))) {
          bugStorage.addMessage(new Span(chunkBegin, chunkEnd), UnreachableCode, null, null)
          chunkBegin = span.getBegin; chunkEnd = span.getEnd
        } else {
          if ((spanBegin.getLine.toInt < chunkBegin.getLine.toInt) || ((spanBegin.getLine.toInt == chunkBegin.getLine.toInt) && 
            (spanBegin.column.toInt < chunkBegin.column.toInt))) chunkBegin = spanBegin
          if ((chunkEnd.getLine.toInt < spanEnd.getLine.toInt) || ((chunkEnd.getLine.toInt == spanEnd.getLine.toInt) && 
            (chunkEnd.column.toInt < spanEnd.column.toInt))) chunkEnd = spanEnd
        }
      }
      bugStorage.addMessage(new Span(chunkBegin, chunkEnd), UnreachableCode, null, null)
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
        bugStorage.addMessage(cfg.getFuncInfo(fid).getSpan, (if (libMode) UnreferencedFunction else UnusedFunction), null, null, getFuncName(cfg.getFuncName(fid)))
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
          case Some(unusedSet) => unusedSet.foreach((bug) => bugStorage.addMessage(bug._4, (if (bug._1) UnusedVariable else UnusedProperty), null, null, bugStorage.getOriginalName(bug._3)))
          case None => Unit
    }})

    ////////////////////////////////////////////////////////////////
    // addSuccessors: add new node to checklist.
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
    for((fid, (argObjSet, span)) <- bugStorage.detectedFuncMap) {
      // function name, expected maximum argument length
      val funcName = cfg.getFuncName(fid)
      val funcArgList = cfg.getArgVars(fid)
      val (funcArgLen, isBuildinFunc) = ModelManager.getFIdMap("Builtin").get(fid) match {
        case Some(builtinFuncName) => (argSizeMap(builtinFuncName)._2, true)
        case None => (funcArgList.length, false)
      }
      //println("- " + funcName + "(" + funcArgLen + ") #" + fid)

      // maximum argument length
      var maxArgObjLength = 0
      val argObjLengthMap = new MHashMap[Obj, Int]()
      for(argObj <- argObjSet) {
        val (propValue, _) = argObj("length")
        propValue.objval.value.pvalue.numval.getConcreteValue match {
          case Some(lengthPropDouble) =>
            val lengthPropInt = lengthPropDouble.toInt
            if(maxArgObjLength < lengthPropInt) maxArgObjLength = lengthPropInt
            argObjLengthMap.put(argObj, lengthPropInt)
          case None => // TODO?
        }
      }
      //println("  maximum argument length = " + maxArgObjLength)

      // for each argument index 0, 1, 2, ...
      for(i <- 0 until maxArgObjLength) {
        var joinedValue: Value = ValueBot
        for((argObj, argObjLength) <- argObjLengthMap) {
          if(argObjLength <= maxArgObjLength) {
            val (propValue, _) = argObj(i.toString)
            joinedValue+= propValue.objval.value
            //println("  argObj[" + i + "] = " + propValue.objval.value)
          }
        }
        val joinedValueTypeCount = joinedValue.pvalue.typeCount
        val isBug = (joinedValueTypeCount > 1 || joinedValueTypeCount == 1 && joinedValue.locset.size > 0)
        if(isBug) {
          var typeKinds: String = joinedValue.typeKinds
          if(isBuildinFunc) {
            // Built-in function
            val ordinal = i + 1 match {
              case 1 => "1st"
              case 2 => "2nd"
              case _ => i + "th"
            }
            bugStorage.addMessage(span, VaryingTypeArguments, null, null, getFuncName(funcName), ordinal + " ", "", typeKinds)
          }
          else {
            // User function
            val argName = funcArgList(i) match {
              case CFGUserId(_, _, _, originalName, _) => originalName
              case CFGTempId(text, _) => text
            }
            bugStorage.addMessage(span, VaryingTypeArguments, null, null, getFuncName(funcName), "", "'" + argName + "' ", typeKinds)
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
