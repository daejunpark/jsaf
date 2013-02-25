/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.lib.graph

import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet
import kr.ac.kaist.jsaf.analysis.typing.ControlPoint

//class DJGraph[T](entry: T) extends DomTree[T](HashMap[T, T](), HashMap[T, Set[T]]((entry, HashSet()))) {
class DJGraph(entry: ControlPoint) extends DomTree[ControlPoint](HashMap[ControlPoint, ControlPoint](), HashMap[ControlPoint, Set[ControlPoint]]((entry, HashSet()))) {
  // D edge = idom
  // J edge = join
  var join = HashMap[ControlPoint, Set[ControlPoint]]()
  // assume level 0 is the level of root
  var level = HashMap[ControlPoint, Int]((entry, 0))
  private def getLevel(node: ControlPoint): Int = {
    level.get(node) match {
      case Some(i) => i
      case None => 0
    }
  }
  private def setLevel(node: ControlPoint, l: Int): Unit = {
    level += node -> l
  }

  private def getSomeDefault[T,R](map: Map[T, R], n: T, default: R): R = {
    map.get(n) match {
      case Some(s) => s
      case None => default
    }
  }
  private def updateLevelNumber(node: ControlPoint): Unit = {
    setLevel(node, calcLevel(node))
  }
  private def calcLevel(node: ControlPoint): Int = {
    var lv = 0
    var current = node
    while(!hasParent(current)) {
      lv+=1
      current = getParent(current)
    }
    lv
  }
  private def getSet[T,R](map: Map[T, Set[R]], n: T) = {
    getSomeDefault(map,n,Set[R]())
  }

  private def nearest_common_ancestor(x: ControlPoint, y: ControlPoint): ControlPoint = {
//    System.err.println("x = " + x + " level : " + getLevel(x) + " // y = " + y + " level : " + getLevel(y))
    if(x == y) x
    else {
      if(getLevel(x) < getLevel(y)) nearest_common_ancestor(x, idom(y))
      else nearest_common_ancestor(idom(x), y)
    }
  }

  private def getSubTree(node: ControlPoint): Set[ControlPoint] = {
    getChildren(node).foldLeft(HashSet[ControlPoint](node))((set, n) => set ++ getSubTree(n))
  }
  private def dominanceFrontier(x: ControlPoint): Set[ControlPoint] = {
    getSubTree(x).foldLeft(HashSet[ControlPoint]())((set, y) => {
      set ++ getSet(join, y).filter(z => getLevel(z) <= getLevel(x))
    })
  }
  private def DF(nodes: Set[ControlPoint]): Set[ControlPoint] = {
    nodes.foldLeft(HashSet[ControlPoint]())((set, node) => set ++ dominanceFrontier(node))
  }
  def iDominanceFrontier(nodes: Set[ControlPoint]): Set[ControlPoint] = {
    val idf = DF(nodes) ++ nodes
//    System.err.println("nodes : " + nodes + " // idf : " + idf)
    if(idf.equals(nodes)) idf
    else iDominanceFrontier(nodes ++ idf)
  }

  private def cutDEdge(src: ControlPoint, dst: ControlPoint): Unit = {
    // cut from idom
    idom = idom - dst
    // cut from dom_tree
    dom_tree += src -> (getChildren(src) - dst)
  }

  private def linkDEdge(src: ControlPoint, dst: ControlPoint): Unit = {
    // link to idom
    idom += dst -> src
    // link to dom_tree
    dom_tree += src -> (getChildren(src) + dst)
  }

  private def linkJEdge(src: ControlPoint, dst: ControlPoint): Unit = {
    join += src -> (getSet(join, src) + dst)
  }


  def updateInsertEdge(fg: TGraph[ControlPoint], x: ControlPoint, y: ControlPoint): Unit = {
    // if it is a new node, target node can be just added to the dom tree and no JEdge
    if(!hasParent(y)) {
      linkDEdge(x, y)
      setLevel(y, getLevel(x) + 1)
    } else {
      val z = nearest_common_ancestor(x, y)
      val DomAffected = (iDominanceFrontier(HashSet[ControlPoint](y)) + y).filter(w => getLevel(w) > getLevel(z) + 1)
      var affectedSet = HashSet[ControlPoint]()
      if(z!=x)
        linkJEdge(x, y)
      DomAffected.foreach( w => {
        val u = getParent(w)
        cutDEdge(u, w)
        if((fg.getExcSuccs(u) ++ fg.getNormalSuccs(u)).contains(w))
          linkJEdge(u, w)
        linkDEdge(z, w)
        affectedSet ++= getSubTree(w)
      })
      affectedSet.foreach( w => updateLevelNumber(w) )
    }
  }
}