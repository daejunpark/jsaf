/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.lib.graph

import kr.ac.kaist.jsaf.analysis.typing.ControlPoint

/**
 * Global Expanded Control-flow Graph.
 * 
 * GEGraph = GENode x GEEdge
 * GENode = ControlPoint x Kind
 * GEEdge = GENode x GENode x EdgeType
 *
 * Kind ::= I | O | OE
 * EdgeType ::= Normal | Exception
 *
 * Each of CFG node consists of three kinds of ENode: I, O, OE.
 * - I: Input node.
 * - O: Normal Out node.
 * - OE: Exception Out node.
 *
 *     input
 *      edge
 *       |
 *       I
 *      / \
 *     O  OE
 *     |   |
 * normal  exception
 * out     out
 * edge    edge
 */

class GEGraph(nodes: Set[GENode], override var entry: GENode, graph: TGraph[ControlPoint]) extends Graph[GENode]{
  def getNodes: Set[GENode] = nodes

  def getSuccs(enode: GENode): Set[GENode] = {
    val node = enode._1
    enode._2 match {
      case KindI => Set((node, KindO), (node, KindOE))
      case KindO => graph.getNormalSuccs(node).map(n => (n, KindI))
      case KindOE => graph.getExcSuccs(node).map(n => (n, KindI))
    }
  }

  def getPreds(enode: GENode): Set[GENode] = {
    val node = enode._1
    enode._2 match {
      case KindI => graph.getNormalPreds(node).map(n => (n, KindO)) ++ graph.getExcPreds(node).map(n => (n, KindOE))
      case KindO => Set((node, KindI))
      case KindOE => Set((node, KindI))
    }
  }
}

object GEGraph {
  def makeGraph(graph: GFlowGraph): GEGraph = {
    val nodes =
      graph.getNodes.foldLeft(Set[GENode]())((N, n) => {
        N + ((n, KindI)) + ((n, KindO)) + ((n, KindOE))
      })

    new GEGraph(nodes, (graph.entry, KindO), graph)
  }
}
