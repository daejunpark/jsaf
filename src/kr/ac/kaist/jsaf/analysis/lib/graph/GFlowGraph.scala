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
import kr.ac.kaist.jsaf.analysis.lib._
import scala.math.min
import kr.ac.kaist.jsaf.analysis.typing.ControlPoint
import kr.ac.kaist.jsaf.analysis.typing.CallContext

/**
 * Global Control-flow Graph.
 * GTGraph has two kinds of edges: normal edge and exception edge.
 * In GTGraph, call and after-call nodes are not connected with an edge.
 * This graph contains infeasible paths which are eliminated by localization.
 * Currently, it does not consider recovered CFG...EFG
 */

class GFlowGraph(entry: ControlPoint) extends TGraph[ControlPoint](entry){
  private var recoveredCFG: HashSet[ControlPoint] = HashSet()
  private var recoveredEFG: HashSet[ControlPoint] = HashSet()

  def isRecovered(n: ControlPoint) = recoveredCFG.contains(n)
  def isExcRecovered(n: ControlPoint) = recoveredEFG.contains(n)
  def recovered(n: ControlPoint) = recoveredCFG += n
  def excRecovered(n: ControlPoint) = recoveredEFG += n

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

  def getLabel(node: ControlPoint): String = {
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

  def toDot_dugraph() = {
    System.out.println("digraph \"DirectedGraph\" {")
    succs.foreach{ case (src, dsts) => {
      dsts.foreach(dst => System.out.println(getLabel(src)+ "->"+getLabel(dst)+"["+NormalEdgeStyle+"];"))
    }}
    excSucc.foreach{ case (src, dst) => {
      System.out.println(getLabel(src)+ "->"+getLabel(dst)+"["+ExcEdgeStyle+"];")
    }}
    System.out.println("}")
  }

  def toDot_String(getFuncName:FunctionId => String, getLabel:(ControlPoint => String)): String = {
    var str = ""
    var i = 0
    var nodeMap:Map[(FunctionId,CallContext), Set[ControlPoint]] = HashMap()
    nodes.foreach(cp => {
      nodeMap += ((cp._1._1,cp._2) -> (getSet(nodeMap, (cp._1._1,cp._2)) + cp))
    })

    nodeMap.foreach(kv => {
      str += "subgraph cluster" + i + " {\n"
      str += "label = \"fid : " + getFuncName(kv._1._1) + "(" + kv._1._1 + ")" + i + "\";\n"
      i += 1
      kv._2.foreach(node => str += getLabel(node) + ";\n")
      str += "{rank=source;"
      kv._2.filter(cp => if(cp._1._2 == LEntry) true else false).foreach(cp => str += getLabel(cp)+";")
      str += "}\n{rank=sink;"
      kv._2.filter(cp => if(cp._1._2 == LExit || cp._1._2 == LExitExc) true else false).foreach(cp => str += getLabel(cp)+";")
      str += "}\n}\n"
    })

    succs.foreach{ case (src, dsts) => {
      dsts.foreach(dst => str += (getLabel(src)+ "->"+getLabel(dst)+"["+NormalEdgeStyle+"];\n"))
    }}
    excSucc.foreach{ case (src, dst) => {
      str += (getLabel(src)+ "->"+getLabel(dst)+"["+ExcEdgeStyle+"];\n")
    }}
    str
  }
}

object GFlowGraph {
  def makeGraph(entry: ControlPoint): GFlowGraph = {
    val g = new GFlowGraph(entry)
    g.addNode(entry)
    g
  }
  def makeGraph(nodes: Set[ControlPoint], entry: ControlPoint, succs: ControlPoint => Set[ControlPoint], succs_e: ControlPoint => Set[ControlPoint]):GFlowGraph = {
    var succs_map = HashMap[ControlPoint, Set[ControlPoint]]()
    var preds_map = HashMap[ControlPoint, Set[ControlPoint]]()
    var esuccs_map = HashMap[ControlPoint, ControlPoint]()
    var epreds_map = HashMap[ControlPoint, Set[ControlPoint]]()

    nodes.foreach((n) => {
      val succs_set = succs(n)
      if (!succs_set.isEmpty) {
        succs_map += (n -> succs_set)
        succs_set.foreach((succ) => {
          preds_map.get(succ) match {
            case Some(s) => preds_map += (succ -> (s + n))
            case None => preds_map += (succ -> HashSet(n))
          }
        })
      }
    })
    nodes.foreach((n) => {
      val esuccs_set = succs_e(n)
      if (!esuccs_set.isEmpty) {
        esuccs_map += (n -> esuccs_set.head)
        esuccs_set.foreach(succ => {
          epreds_map.get(succ) match {
            case Some(s) => epreds_map += (succ -> (s + n))
            case None => epreds_map += (succ -> HashSet(n))
          }
        })
      }
    })

    val g = new GFlowGraph(entry)
    g.addNodes(nodes)
    g.succs = succs_map
    g.preds = preds_map
    g.excSucc = esuccs_map
    g.excPreds = epreds_map
    g
  }
}
