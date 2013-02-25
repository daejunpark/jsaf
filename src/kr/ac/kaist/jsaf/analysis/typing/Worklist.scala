/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.cfg._
import scala.collection.immutable.{ TreeMap, TreeSet, HashSet, HashMap }
import kr.ac.kaist.jsaf.analysis.lib.graph.DGraph
import kr.ac.kaist.jsaf.analysis.lib.Utils
import kr.ac.kaist.jsaf.analysis.lib.WorkTreeSet

/**
 * Worklist manager
 */
class Worklist(order: OrderMap, quiet: Boolean) {
  var headorder: OrderMap = TreeMap[Node, Int]()
  var backedges: HashMap[Node, HashSet[Node]] = HashMap()

  var worklist = WorkTreeSet.Empty

  def isEmpty = worklist.isEmpty

  def init(map: TreeMap[Node, Int], backmap: HashMap[Node, HashSet[Node]]): Unit = {
    headorder = map
    backedges = backmap
  }

  def getHead(): ControlPoint = {
    val (head, tail) = worklist.headAndTail
    worklist = tail
    head._2
  }

  def add(cp: ControlPoint): Unit = {
    val ov =
      order.get(cp._1) match {
        case Some(o) => o
        case None => 0 // case for an empty block
      }
    worklist += ((ov, cp))
  }

  def add(origin: Node, cp: ControlPoint): Unit = {
    backedges.get(cp._1) match {
      case Some(backnodes) if backnodes.contains(origin) => worklist += ((headorder(cp._1), cp))
      case Some(backnodes) => add(cp)
      case _ => add(cp)
    }
  }

  def getOrder() = { order }

  def dump() {
    if (!quiet)
      System.out.print("next: "+worklist.head._2._1+"   ")
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

  def computesSparse(cfg: CFG, quiet: Boolean): Worklist = {
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

    val interDDG = cfg.getInterDDG.prunedGraph
    makeSCCGraph(interDDG)

    val wl = new Worklist(map, quiet)
    wl.init(headmap, backedges)

    val elapsedTime = (System.nanoTime - s) / 1000000000.0;
    if (!quiet)
      System.out.format("# Time for worklist order computation(s): %.2f\n", new java.lang.Double(elapsedTime))
    wl
  }
}
