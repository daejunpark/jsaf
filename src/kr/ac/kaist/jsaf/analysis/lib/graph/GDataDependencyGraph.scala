/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.lib.graph

import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet
import scala.collection.mutable.{ Stack }
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain.{Loc, LocSet, LocSetBot, DomainPrinter}
import kr.ac.kaist.jsaf.analysis.lib._
import kr.ac.kaist.jsaf.analysis.typing.ControlPoint
import kr.ac.kaist.jsaf.analysis.typing.CallContext

class GDataDependencyGraph(var nodes: Set[ControlPoint], var entry: ControlPoint) {
  private var succs: Map[ControlPoint, Set[ControlPoint]] = HashMap()
  private var excSucc: Map[ControlPoint, ControlPoint] = HashMap()
  private var preds: Map[ControlPoint, Set[ControlPoint]] = HashMap()
  private var excPred: Map[ControlPoint, Set[ControlPoint]] = HashMap()
  private var dusetMap: Map[(ControlPoint,ControlPoint), LocSet] = HashMap()
  private var excdusetMap: Map[(ControlPoint,ControlPoint), LocSet] = HashMap()

  def addNewCallContext(nodes:Set[ControlPoint]) = {
    this.nodes ++= nodes
  }

  def getNormalSuccs(node: ControlPoint) = getSet(succs, node)
  def getExcSucc(node: ControlPoint) = excSucc.get(node)
  def getSuccs(node: ControlPoint) = {
    excSucc.get(node) match {
      case Some(n) => getNormalSuccs(node) + n
      case None => getNormalSuccs(node)
    }
  }
  def getNormalPreds(node: ControlPoint) = getSet(preds, node)
  def getExcPred(node: ControlPoint) = getSet(excPred, node)
  def getPreds(node: ControlPoint) = {
    getSet(preds, node) ++ getSet(excPred, node)
  }

  def toEdgeOnlyGraph : DGraph[ControlPoint] = {
    val ssa = new DGraph[ControlPoint](nodes, entry)
    succs.foreach(kv => {
      val src = kv._1
      kv._2.foreach(dst => ssa.addEdge(src, dst))
    })
    excSucc.foreach(kv => {
      val src = kv._1
      val dst = kv._2
      ssa.addEdge(src, dst)
    })
    ssa
  }

  def +(g: GDataDependencyGraph): GDataDependencyGraph = {
    val new_succs = g.succs.foldLeft(this.succs)((succs, kv) => {
      succs.get(kv._1) match {
        case Some(v) => succs + (kv._1 -> (v ++ kv._2))
        case None => succs + (kv._1 -> kv._2)
      }
    })
    val new_excSucc = g.excSucc.foldLeft(this.excSucc)((succs, kv) => {
      succs + (kv._1 -> kv._2)
    })
    val new_dusetMap = g.dusetMap.foldLeft(this.dusetMap)((map, kv) => {
      map + (kv._1 -> kv._2)
    })
    val new_preds = g.preds.foldLeft(this.preds)((preds, kv) => {
      preds.get(kv._1) match {
        case Some(v) => preds + (kv._1 -> (v ++ kv._2))
        case None => preds + (kv._1 -> kv._2)
      }
    })
    val new_excPred = g.excPred.foldLeft(this.excPred)((excPred, kv) => {
      excPred.get(kv._1) match {
        case Some(v) => excPred + (kv._1 -> (v ++ kv._2))
        case None => excPred + (kv._1 -> kv._2)
      }
    })
    val new_excdusetMap = g.dusetMap.foldLeft(this.excdusetMap)((map, kv) => {
      map + (kv._1 -> kv._2)
    })

    var newgraph = new GDataDependencyGraph(this.nodes ++ g.getNodes, this.entry)
    newgraph.succs = new_succs
    newgraph.excSucc = new_excSucc
    newgraph.preds = new_preds
    newgraph.excPred = new_excPred
    newgraph.dusetMap = new_dusetMap
    newgraph.excdusetMap = new_excdusetMap
    newgraph
  }

  def getDUSet(src: ControlPoint, dest: ControlPoint): LocSet =
    dusetMap.get(src,dest) match {
      case Some(s) => s
      case None => LocSetBot
    }

  def getExcDUSet(src: ControlPoint, dest: ControlPoint): LocSet =
    excdusetMap.get(src,dest) match {
      case Some(s) => s
      case None => LocSetBot
    }

  def dump_candidates = {
    System.out.println("Preds===")
    preds.foreach(nl => System.out.println(nl))
    System.out.println("Candidates===")
    nodes.foreach(n => System.out.println(getPreds(n)))
    System.out.println("dusetMap===")
    dusetMap.foreach(dul => {
      val (edge, lset) = dul
      System.out.print(edge + ",Set(")
      lset.foreach(loc => System.out.print(DomainPrinter.printLoc(loc) + ","))
      System.out.println(")")
    })
  }

  def getNodes = nodes

  def addEdge(src: ControlPoint, dest: ControlPoint, l: Loc) = {
    val oldSet = getDUSet(src, dest)
    if (oldSet.contains(l)) {
      false
    } else {
      // System.err.println("Edge: "+src+" -> "+dest+" { "+ DomainPrinter.printLoc(l) +" }")
      succs += (src -> (getNormalSuccs(src) +dest))
      preds += (dest -> (getSet(preds, dest) + src))
      dusetMap += ((src, dest) -> (oldSet + l))
      true
    }
  }

  def addEdges(src: ControlPoint, dest: ControlPoint, locs: LocSet) = {
    val oldSet = getDUSet(src, dest)
    if (locs.subsetOf(oldSet)) {
      false
    } else {
      // System.out.println("addEdges: "+src+" -> "+dest + " with "+locs)
      succs += (src -> (getNormalSuccs(src) +dest))
      preds += (dest -> (getSet(preds, dest) + src))
      dusetMap += ((src, dest) -> (oldSet ++ locs))
      true
    }
  }

  def addExcEdge(src: ControlPoint, dest: ControlPoint, l: Loc) = {
    val oldSet = getExcDUSet(src, dest)
    if (oldSet.contains(l)) {
      false
    } else {
      // System.err.println("ExcEdge: "+src+" -> "+dest+" { "+ DomainPrinter.printLoc(l) +" }")
      excSucc += (src -> dest)
      excPred += (dest -> (getSet(excPred, dest) + src))
      excdusetMap += ((src, dest) -> (oldSet + l))
      true
    }
  }

  def addExcEdges(src: ControlPoint, dest: ControlPoint, locs: LocSet) = {
    val oldSet = getExcDUSet(src, dest)
    if (locs.subsetOf(oldSet)) {
      false
    } else {
      excSucc += (src -> dest)
      excPred += (dest -> (getSet(excPred, dest) + src))
      excdusetMap += ((src, dest) -> (oldSet ++ locs))
      true
    }
  }

  def reachable: Set[ControlPoint] = {
    val e = entry
    var visited = HashSet[ControlPoint]()

    def dfs(n: ControlPoint): Unit = {
      visited += (n)
      getSuccs(n).foreach((c) => {
        if (!visited.contains(c))
          dfs(c)
      })
    }
    dfs(e)

    visited
  }

  def dump() = {
    System.out.println("entry: "+entry)
    succs.foreach(kv => {
      val src = kv._1
      kv._2.foreach(dst => System.out.println(src+ " => "+dst))
    })
  }

  var count:Int = -1
  var mapCtx:Map[(FunctionId,CallContext), Int] = HashMap()
  def getCallId(fid:FunctionId, cc:CallContext):Int = {
    mapCtx.get((fid, cc)) match {
      case Some(i) => i
      case None =>
        count+=1
        mapCtx += (fid, cc) -> count
        count
    }
  }

  implicit def getLabel(node: ControlPoint): String = {
    val ccstr = "_"+getCallId(node._1._1, node._2)+"_"
    node._1._2 match {
      case LBlock(id) => ccstr+"Block"+id
      case LEntry => ccstr+"Entry" + node._1._1
      case LExit => ccstr+"Exit" + node._1._1
      case LExitExc => ccstr+"ExitExc" + node._1._1
    }
  }
  private def NormalEdgeStyle:String = "fontname=\"Consolas\" style=solid"

  private def ExcEdgeStyle:String = "fontname=\"Consolas\" style=dashed"

  private def ppLocs(set: LocSet) = {
    "{ " + set.foldLeft("")((S,l) => S + DomainPrinter.printLoc(l) + ", ") + " }"
  }

  def toDot_dugraph() = {
    System.out.println("digraph \"DirectedGraph\" {")
    getNodes.foreach(src => {
      getNormalSuccs(src).foreach(dst => {
        val duset = getDUSet(src, dst)
        System.out.println(getLabel(src)+ "->"+getLabel(dst)+"["+NormalEdgeStyle+", label=\"" + ppLocs(duset) + "\"];")
      })
    })
    getNodes.foreach(src => {
      getExcSucc(src) match {
        case Some(dst) => {
          val duset = getExcDUSet(src, dst)
          System.out.println(getLabel(src)+ "->"+getLabel(dst)+"["+ExcEdgeStyle+", label=\"" + ppLocs(duset) + "\"];")
        }
        case _ => ()
      }
    })
    System.out.println("}")
  }

  def toDot_String(getFuncName: FunctionId=>String): String = {
    var str = ""
    var i = 0
    var nodeMap:Map[(FunctionId,CallContext), Set[ControlPoint]] = HashMap()
    getNodes.foreach(cp => {
      nodeMap += ((cp._1._1,cp._2) -> (getSet(nodeMap, (cp._1._1,cp._2)) + cp))
    })

    nodeMap.foreach(kv => {
      str += "subgraph cluster" + i + " {\n"
      str += "label = \"fid : " + getFuncName(kv._1._1) + "(" + kv._1._1 + ")" + i + " cc : " + kv._1._2+ "\";\n"
      i += 1
      kv._2.foreach(node => str += getLabel(node) + ";\n")
      str += "{rank=source;"
      kv._2.filter(cp => if(cp._1._2 == LEntry) true else false).foreach(cp => str += getLabel(cp)+";")
      str += "}\n{rank=sink;"
      kv._2.filter(cp => if(cp._1._2 == LExit || cp._1._2 == LExitExc) true else false).foreach(cp => str += getLabel(cp)+";")
      str += "}\n}\n"
    })

    getNodes.foreach(src => {
      getNormalSuccs(src).foreach(dst => {
        val duset = getDUSet(src, dst)
        str += (getLabel(src)+ "->"+getLabel(dst)+"["+NormalEdgeStyle+", label=\"" + ppLocs(duset) + "\"];\n")
      })
    })
    getNodes.foreach(src => {
      getExcSucc(src) match {
        case Some(dst) => {
          str += (getLabel(src)+ "->"+getLabel(dst)+"["+ExcEdgeStyle+"];\n")
        }
        case _ => ()
      }
    })
    str
  }
}
