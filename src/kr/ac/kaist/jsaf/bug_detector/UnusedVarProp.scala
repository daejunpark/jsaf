/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr=>SE}

class UnusedVarProp(cfg: CFG, typing: TypingInterface, bugs: BugMessage, semantics: Semantics) extends BugRule{
  /* 
  * RWEntry     : (Boolean, Boolean, Loc, String, Info)
  *  L Boolean  : true => Write    | false => Read
  *  L Boolean  : true => Variable | false => Property
  *  L Loc      : Location
  *  L String   : Variable or Property name
  *  L Info     : Info
  * WEntry      : (Boolean, Loc, String, Info)
  * RWMAP       : Map[Node, List[RWEntry]]
  * WMap        : Map[Node, List[WEntry]]
  */
  type RWEntry  = (Boolean, Boolean, Loc, String, Span)
  type WEntry   = (Boolean, Loc, String, Span)
  type RWMap    = Map[Node, List[RWEntry]] 
  type WMap     = Map[Node, Set[WEntry]] 
  private val write     = true
  private val read      = false
  private val variable  = true
  private val property  = false

  /*
  * vmsg, pmsg      : Bug Messages
  * RWMap           : Stores written & read variables or properties
  * count           : Number of unused variables or properties
  */
  private val vmsg  = "Variable '%s' is never used."
  private val pmsg  = "Property '%s' is never used."
  private var RWMap : RWMap = Map()
  private var count = 0

  /* map internal name to original name */
  private var nameMap: Map[String,String] = Map() 
  
  /* print total number of unused variables or properties */
  override def printStat = System.out.println("# UnusedVarProp: " + count)
  
  /* Store write variables or properties */
  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state       = typing.mergeState(cstate)
    val heap        = state._1
    val context     = state._2
    if (heap <= HeapBot) Unit
    else {
      inst match {
        case CFGAlloc(_, info, id, _, _)            => filterUserId(heap, inst, id, write, variable, info.getSpan)
        case CFGAllocArray(_, info, id, _, _)       => filterUserId(heap, inst, id, write, variable, info.getSpan)
        case CFGAllocArg(_, info, id, _, _)         => filterUserId(heap, inst, id, write, variable, info.getSpan)
        case CFGDelete(_, info, id, _)              => filterUserId(heap, inst, id, write, variable, info.getSpan)
        case CFGDeleteProp(_, info, id, _, _)       => filterUserId(heap, inst, id, write, variable, info.getSpan)
        case CFGExprStmt(_, info, id, _)            => filterUserId(heap, inst, id, write, variable, info.getSpan)
        //case CFGFunExpr(_, info, id, _, _, _, _, _) => filterUserId(heap, inst, id, write, variable, info.getSpan)
        case CFGInternalCall(_, info, id, _, _, _)  => filterUserId(heap, inst, id, write, variable, info.getSpan)
        case CFGStore(_, info, obj, index, _)       =>
          val lset = SE.V(obj, heap, context)._1._2
          val s = SE.V(index, heap, context)._1._1._5
          /*
          val lpset = lset.foldLeft[Set[(Loc, Set[String])]](Set())((lpset, l) =>
            AbsString.concretize(s) match {
              case Some(prop) => lpset + Set((l, Set(prop)))
              // ignore StrTop, NumStr, OtherStr
              case None => lpset
            })
          storeRWMap(inst, write, property, lpset, info.getSpan)
          */
          lset.foreach((l: Loc) => 
            AbsString.concretize(s) match {
              case Some(prop) => storeRWMap(inst, write, property, l, prop, info.getSpan)
              // ignore StrTop, NumStr, OtherStr
              case None => Unit
            })
        case _ => Unit
      }
    }
  }
    
  /* Store read variables or properties */
  override def checkExpr(inst:CFGInst, expr: CFGExpr, cstate: CState): Unit = {
    val state       = typing.mergeState(cstate)
    val heap        = state._1
    val context     = state._2
    if (heap <= HeapBot) Unit
    else {
      expr match {
        case CFGVarRef(info, id) => 
          filterUserId(heap, inst, id, read, variable, info.getSpan) 
        case CFGLoad(info, obj, index) => 
          val lset = SE.V(obj, heap, context)._1._2
          val s = SE.V(index, heap, context)._1._1._5
          val lset_b = lset.foldLeft(LocSetBot)((ls, l) => ls ++ Helper.ProtoBase(heap, l, s))
          /*
          val lpset = lset_b.foldLeft[Set[(Loc, Set[String])]](Set())((lpset, l) =>
            lpset ++ Set((l, props(heap, l, s))))
          storeRWMap(inst, read, property, lpset, info.getSpan)
          */
          lset_b.foreach((l: Loc) => 
            props(heap,l,s).foreach((prop) => storeRWMap(inst, read, property, l, prop, info.getSpan)))
        case _ => Unit
      }
    }
  }
  
  private def filterUserId(heap: Heap, inst: CFGInst, id: CFGId, rwflag: Boolean, pvflag: Boolean, span: Span): Unit = {
      val name = id.toString     
      id match {
        case CFGUserId(_, text, _, origin, _) =>
          if (isInternalName(text)) 
            nameMap = nameMap + (text -> origin)
          val lset_base = Helper.LookupBase(heap, id)
          lset_base.foreach((loc: Loc) => storeRWMap(inst, rwflag, pvflag, loc, name, span))
        case CFGTempId(_, _) => Unit
      }
  }
  
  private def storeRWMap(inst: CFGInst, rwflag: Boolean, pvflag: Boolean, loc:Loc, prop: String, span: Span): Unit = {
    val node = cfg.findEnclosingNode(inst)
    RWMap.get(node) match {
      case Some(entries) =>
        RWMap = RWMap + (node -> (entries :+ (rwflag, pvflag, loc, prop, span)))
      case None =>
        RWMap = RWMap + (node -> List((rwflag, pvflag, loc, prop, span)))
    }
  }
  
  private def props(h: Heap, l: Loc, abs: AbsString): Set[String] = {
    if (!h.domIn(l)) Set()
    else {
      abs match {
        // ignore @default
        case StrTop =>
          h(l).map.keySet.filter(x => !x.take(1).equals("@"))
        case NumStr =>
          h(l).map.keySet.filter(x => !x.take(1).equals("@") && AbsString.alpha(x) <= NumStr)
        case OtherStr =>
          h(l).map.keySet.filter(x => !x.take(1).equals("@") && AbsString.alpha(x) <= OtherStr)
        case NumStrSingle(s) =>
          Set(s)
        case OtherStrSingle(s) =>
          Set(s)
        case StrBot => Set()
      }
    }
  }

  /* bug check */
  override def checkFinal(): Unit = {
    /*
    * checkList : Stores nodes that is to be traversed
    * unusedMap : Stores unused write set
    */
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
      
      /* before comparison */
      val beforeCheck = predSet.foldLeft[Set[WEntry]](Set())((set, pred) =>
        unusedMap.get(pred) match {
          case Some(wset) => set ++ wset
          case None => set
        })
        
      /*    comparing ...  */
      val afterCheck = RWMap.get(current) match {
        case Some(rwSequence) =>
          rwSequence.foldLeft[Set[WEntry]](beforeCheck)((set, rw) =>
            if (rw._1) { // write
              val found = set.find((e) => ((e._1 == rw._2) && (e._2 == rw._3) && (e._3 == rw._4)))
              found match {
                case Some(exist) => // never read, 
                  /* but useless info. for user 
                  count = count + 1
                  bugReport(exist) */
                  val removed  = set.filterNot((e) => ((e._1 == rw._2) && (e._2 == rw._3) && (e._3 == rw._4))) 
                  removed + ((rw._2, rw._3, rw._4, rw._5))
                case None =>
                  // added new write 
                  set + ((rw._2, rw._3, rw._4, rw._5))
              }
            }
            else { // read
              set.filterNot((e) => ((e._1 == rw._2) && (e._2 == rw._3) && (e._3 == rw._4)))
            }
          )
        case None => beforeCheck
      }

      /* update unusedMap */
      unusedMap.get(current) match {
        case Some(currentWSet) =>
          if (currentWSet == afterCheck)    /* unchanged */
            checkList = checkList.tail
          else {                              /* changed */
            unusedMap = unusedMap + (current -> afterCheck)
            checkList = checkList.tail ++ addSuccessors(current)
          }
        case None =>                           /* changed */
          unusedMap = unusedMap + (current -> afterCheck)
          checkList = checkList.tail ++ addSuccessors(current)
      }
    }
    
    /* check terminal node */
    cfg.getNodes.foreach((node) =>
      if (cfg.getAllSucc(node).isEmpty) {
        unusedMap.get(node) match {
          case Some(set) =>
            set.foreach((w) => {
              
              bugReport(w)
            })
          case None => Unit
        }
      }
      else Unit
    )

    /* 
    * If the last instruction of current node is call instruction, 
    * add following IPSucc node (from Semantics) to the checklist.
    * Otherwise, add successor nodes (from cfg) to the checkList.
    */
    def addSuccessors(current: Node): List[Node] = {
      typing.getStateBeforeNode(current).keys.foldLeft[List[Node]](List())((list, cc) =>
        semantics.getIPSucc((current, cc)) match {
          case Some(ccmap) => ccmap.keys.foldLeft(list)((l, k) => l :+ k._1)
          case None => list ++ cfg.getSucc(current)
        }
      )
    }
  }
  
  /* register bug to the bugMessage */
  private def bugReport(bug: WEntry): Unit = {
    /* bug : [VarorProp | Location | Name | Info] */
    if (!bugs.isDupMessage(bug._4, "warning", vmsg.format(toOriginName(bug._3)))) {
      count = count + 1
      if (bug._1) bugs.addMessage(bug._4, "warning", vmsg.format(toOriginName(bug._3)))
      else bugs.addMessage(bug._4, "warning", pmsg.format(toOriginName(bug._3)))
    }
  }
  
  private def toOriginName(name: String): String = {
    if (isInternalName(name))
      nameMap(name)
    else
      name
  }
  
  private def isInternalName(name: String): Boolean = {
    if (name.size > 2 && name.take(2) == "<>") 
      true
    else
      false
  }
  
}
