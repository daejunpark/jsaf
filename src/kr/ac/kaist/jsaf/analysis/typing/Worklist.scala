/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import scala.collection.immutable.{ TreeMap, TreeSet, HashSet, HashMap }
import scala.collection.mutable.{HashMap => MHashMap, HashSet => MHashSet, Stack => MStack}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.lib.graph.DGraph
import kr.ac.kaist.jsaf.analysis.lib.WorkTreeSet
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.scala_src.useful.WorkTrait

/**
 * Worklist manager
 */
class Worklist(order: OrderMap, quiet: Boolean) {
  private var headorder: OrderMap = TreeMap[Node, Int]()
  private var backedges: HashMap[Node, HashSet[Node]] = HashMap()

  private var worklist = WorkTreeSet.Empty
  def getWorkList = worklist
  def head = this.synchronized { worklist.head._2 }

  private var useWorkManager = false
  private var workTrait: WorkTrait = null
  def setUseWorkManager(_useWorkManager: Boolean, _workTrait: WorkTrait): Unit = {
    useWorkManager = _useWorkManager
    workTrait = _workTrait
  }

  // Caller ControlPoint stack set
  class CPStackSetRef {
    var refCount = 1
    var cpStackSet: CPStackSet = null
  }
  private val callerCPMap = new MHashMap[ControlPoint, CPStackSetRef]
  private val cpStackSetRefPool = new MStack[CPStackSetRef]
  private def getNewCPStackSetRef(_cpStackSet: CPStackSet = null): CPStackSetRef = {
    val cpStackSetRef = if(cpStackSetRefPool.isEmpty) new CPStackSetRef else cpStackSetRefPool.pop()
    cpStackSetRef.refCount = 1
    cpStackSetRef.cpStackSet = _cpStackSet
    cpStackSetRef
  }

  def isEmpty = this.synchronized { worklist.isEmpty }
  def getSize = this.synchronized { worklist.size }

  def init(map: TreeMap[Node, Int], backmap: HashMap[Node, HashSet[Node]]): Unit = {
    headorder = map
    backedges = backmap
  }

  def getHead(): (ControlPoint, Option[CPStackSet]) = this.synchronized {
    val (head, tail) = worklist.headAndTail
    worklist = tail
    val callerCPStackSet = callerCPMap.get(head._2) match {
      case Some(callerCPStackSetRef) =>
        if(callerCPStackSetRef.refCount == 1) {
          callerCPMap.remove(head._2)
          cpStackSetRefPool.push(callerCPStackSetRef)
        }
        else callerCPStackSetRef.refCount-= 1
        if(callerCPStackSetRef.cpStackSet != null) Some(callerCPStackSetRef.cpStackSet) else None
      case None =>
        callerCPMap.remove(head._2)
        None
    }
    (head._2, callerCPStackSet)
  }

  def add(cp: ControlPoint, callerCPSetOpt: Option[CPStackSet], increaseRefCount: Boolean): Unit = this.synchronized {
    val ov =
      order.get(cp._1) match {
        case Some(o) => o
        case None => 0 // case for an empty block
      }
    worklist += ((ov, cp))
    addCallerCPSet(cp, callerCPSetOpt, increaseRefCount)
    if(useWorkManager) Shell.workManager.pushWork(workTrait)
  }

  def add(origin: Node, cp: ControlPoint, callerCPSetOpt: Option[CPStackSet], increaseRefCount: Boolean): Unit = this.synchronized {
    backedges.get(cp._1) match {
      case Some(backnodes) if backnodes.contains(origin) =>
        worklist += ((headorder(cp._1), cp))
        addCallerCPSet(cp, callerCPSetOpt, increaseRefCount)
        if(useWorkManager) Shell.workManager.pushWork(workTrait)
      case Some(backnodes) => add(cp, callerCPSetOpt, increaseRefCount)
      case _ => add(cp, callerCPSetOpt, increaseRefCount)
    }
  }

  def add(cp_pred: ControlPoint, cp: ControlPoint, cfg: CFG, callerCPSetOpt: Option[CPStackSet], increaseRefCount: Boolean): Boolean = this.synchronized {
    var isWorkAdded = false
    callerCPSetOpt match {
      case Some(callerCPSet) =>
        for(callerCPStack <- callerCPSet) {
          cp match {
            // Call -> Entry
            case ((_, LEntry), _) =>
              val newCallerCPStack = callerCPStack.clone()
              newCallerCPStack.push(cp_pred)
              add(cp, Some(MHashSet(newCallerCPStack)), increaseRefCount); isWorkAdded = true
            // Exit or ExitExc -> Aftercall
            case _ =>
              val topCP = callerCPStack.top
              if(cp._1 == cfg.getAftercallFromCallMap.getOrElse(topCP._1, null) ||
                cp._1 == cfg.getAftercatchFromCallMap.getOrElse(topCP._1, null)) {
                if(callerCPStack.size <= 1) add(cp, None, increaseRefCount)
                else {
                  val newCallerCPStack = callerCPStack.clone()
                  newCallerCPStack.pop()
                  add(cp, Some(MHashSet(newCallerCPStack)), increaseRefCount)
                }
                isWorkAdded = true
              }
          }
        }
      case None =>
        cp match {
          // Call -> Entry
          case ((_, LEntry), _) => add(cp, Some(MHashSet(MStack(cp_pred))), increaseRefCount); isWorkAdded = true
          // Exit or ExitExc -> Aftercall
          case _ => add(cp, None, increaseRefCount); isWorkAdded = true
        }
    }
    isWorkAdded
  }

  private def addCallerCPSet(cp: ControlPoint, callerCPSetOpt: Option[CPStackSet], increaseRefCount: Boolean): Unit = {
    callerCPSetOpt match {
      case Some(callerCPSet) =>
        callerCPMap.get(cp) match {
          case Some(prevCallerCPSetRef) =>
            if(increaseRefCount) prevCallerCPSetRef.refCount+= 1
            if(prevCallerCPSetRef.cpStackSet == null) prevCallerCPSetRef.cpStackSet = callerCPSet
            else prevCallerCPSetRef.cpStackSet++= callerCPSet
          case None => callerCPMap.put(cp, getNewCPStackSetRef(callerCPSet))
        }
      case None => callerCPMap.put(cp, getNewCPStackSetRef())
    }
  }

  def getOrder() = this.synchronized { order }

  def dump() {
    if (!quiet) this.synchronized {
      System.out.print("next: "+worklist.head._2._1+"   ")
    }
  }
}

object Worklist {
  def computes(cfg: CFG) : Worklist = {
    computes(cfg, false)
  }

  def computes(cfg: CFG, quiet: Boolean) : Worklist = {
    val s = System.nanoTime
    val empty = TreeMap[Node, Int]()
    val (map, _) = cfg.getNodes.foldLeft((empty,0))((m,n) => (m._1 + (n -> m._2),m._2+1))
    val wl = new Worklist(map, quiet)
    val elapsedTime = (System.nanoTime - s) / 1000000000.0;
    if (!quiet)
      System.out.format("# Time for worklist order computation(s): %.2f\n", new java.lang.Double(elapsedTime))
    wl
  }

  def computesSparse(interDDG: DGraph[Node], quiet: Boolean): Worklist = {
    val s = System.nanoTime
    var map = TreeMap[Node, Int]()
    var headmap = TreeMap[Node, Int]()
    var backedges = HashMap[Node, HashSet[Node]]()
    var order_i = 0

    def findLoophead(g: DGraph[Node]): Node = {
      // check back-edges to find a loop head.
      var refs: HashMap[Node, Int] = g.getNodes.foldLeft(HashMap[Node,Int]())((M, n) => M + (n -> 0))
      g.succs.foreach(kv => {
        kv._2.foreach(n => refs += (n -> (refs(n) + 1)))
      })
      val (entry, _) = refs.foldLeft(refs.head)((h, ref) => if (ref._2 > h._2) ref else h)
      entry
    }

    def makeSCCGraph(g: DGraph[Node]): Unit = {
      val nodes = g.sccs
      def getNode(n: Node) = nodes.filter(ns => ns.contains(n)).head

      // constructs abstract graph for a given graph.
      val entry = getNode(g.entry)
      val (max, ntoi) = nodes.foldLeft((0, HashMap[HashSet[Node], Int]()))((pair, n) => (pair._1 + 1, pair._2 + (n -> pair._1)))
      val nodes_i = (0 to (max-1)).foldLeft(HashSet[Int]())((S, i) => S + i)
      val entry_i = ntoi(entry)
      val iton = new Array[HashSet[Node]](max)
      ntoi.foreach(kv => iton(kv._2) = kv._1)
      val edges_i =
        g.getNodes.foldLeft(HashSet[(Int, Int)]())((S, n) => {
          val succs = g.getSuccs(n)
          val src = getNode(n)
          succs.foldLeft(S)((S_, n2) => {
            val dst = getNode(n2)
            S_ + ((ntoi(src), ntoi(dst)))
          })
        })
      val agraph = DGraph.fromEdges[Int](nodes_i, entry_i, edges_i)

      // computes topological order for the abstract graph.
      agraph.topologicalOrder.foreach(n => {
        val sets: HashSet[Node] = iton(n)
        if (sets.size > 1) {
          // travers each of concrete graph
          val subgraph = DGraph.pruning(g,sets)
          val loophead = findLoophead(subgraph)
          val backnodes = subgraph.removeInedges(loophead)

          backedges += (loophead -> backnodes)
          subgraph.entry = loophead
          makeSCCGraph(subgraph)
        } else {
          map += (sets.head -> order_i)
          order_i += 1
        }
      })

      headmap += (g.entry -> order_i)
      order_i += 1
    }

    makeSCCGraph(interDDG.prunedGraph)

    val wl = new Worklist(map, quiet)
    wl.init(headmap, backedges)

    val elapsedTime = (System.nanoTime - s) / 1000000000.0;
    if (!quiet)
      System.out.format("# Time for worklist order computation(s): %.2f\n", new java.lang.Double(elapsedTime))
    wl
  }
}
