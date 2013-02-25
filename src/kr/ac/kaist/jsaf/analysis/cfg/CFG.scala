/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.cfg

import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet
import kr.ac.kaist.jsaf.analysis.lib._
import kr.ac.kaist.jsaf.analysis.lib.SSA
import kr.ac.kaist.jsaf.analysis.lib.Utils
import kr.ac.kaist.jsaf.analysis.lib.graph.DGraph
import kr.ac.kaist.jsaf.analysis.lib.graph.{DataDependencyGraph => DDGraph}
import kr.ac.kaist.jsaf.analysis.lib.graph.{GDataDependencyGraph => GDDGraph}
import kr.ac.kaist.jsaf.analysis.lib.graph.DomTree
import kr.ac.kaist.jsaf.analysis.lib.graph.EGraph
import kr.ac.kaist.jsaf.analysis.lib.graph.FlowGraph
import kr.ac.kaist.jsaf.analysis.lib.graph.GFlowGraph
import kr.ac.kaist.jsaf.analysis.lib.graph.KindI
import kr.ac.kaist.jsaf.analysis.lib.graph.KindO
import kr.ac.kaist.jsaf.analysis.lib.graph.KindOE
import kr.ac.kaist.jsaf.analysis.lib.graph.TGraph
import kr.ac.kaist.jsaf.analysis.typing.{NotYetImplemented, CallContext, DUSet, ControlPoint}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.lib.graph.ENode
import kr.ac.kaist.jsaf.analysis.lib.graph.GENode
import kr.ac.kaist.jsaf.analysis.lib.graph.GEGraph
import kr.ac.kaist.jsaf.analysis.lib.SSA
import kr.ac.kaist.jsaf.analysis.lib.graph.DJGraph


// Node Label definition
abstract class Label
case object LEntry extends Label
case object LExit extends Label
case object LExitExc extends Label
case class LBlock(id: BlockId) extends Label

// Node Command definition
abstract class Cmd
case object Entry extends Cmd
case object Exit extends Cmd
case object ExitExc extends Cmd
// TODO: insts: consider using mutable
case class Block(insts: List[CFGInst]) extends Cmd

class CFG {
  val DEBUG = true

  // access methods -----------------------------------------------------------

  // all nodes in this cfg.
  private var nodes: List[Node] = List()
  def getNodes = nodes

  // Command corresponding to Node
  private var cmdMap: Map[Node, Cmd] = HashMap()
  def getCmd(cp: Node) = cmdMap(cp)

  // map from file name to end inst. (no op.)
  private var fileToNoOpMap: Map[String, CFGInst] = HashMap()
  def getNoOp(file: String) = fileToNoOpMap(file)

  // Node enclosing a instruction.
  private var enclosingNodeMap: Map[InstId, Node] = HashMap()
  def findEnclosingNode(i: CFGInst) = enclosingNodeMap(i.getInstId)

  // function information
  private var funcMap: Map[FunctionId, (ArgumentsName, ArgVars, LocalVars, List[Node], String, Info)] = HashMap()
  def getArgumentsName(fid: FunctionId) = funcMap(fid)._1
  def getArgVars(fid: FunctionId) = funcMap(fid)._2
  def getLocalVars(fid: FunctionId) = funcMap(fid)._3
  def getLocalNodes(fid: FunctionId) = funcMap(fid)._4
  def getFuncName(fid: FunctionId) = funcMap(fid)._5
  def getFuncInfo(fid: FunctionId) = funcMap(fid)._6
  def getFunctionIds() = funcMap.keySet


  // #PureLocal location for each function.
  // It is 1-callsite context-sensitive only for built-in calls.
  private var pureLocalMap: Map[(FunctionId, CallContext), Loc] = HashMap()
  def getPureLocal(cp: ControlPoint): Loc = {
    val key = (cp._1._1, cp._2)
    pureLocalMap.get(key) match {
      case Some(loc) => loc
      case None => {
        val loc = newLoc("PureLocal#" + key._1 + "#" + key._2)
        pureLocalMap += (key -> loc)
        loc
      }
    }
  }

  // #PureLocal location for each function.
  // It is merged as context-insensitive.
  private var mergedPureLocalMap: Map[FunctionId, Loc] = HashMap()
  def getMergedPureLocal(fid: FunctionId): Loc = {
    mergedPureLocalMap.get(fid) match {
      case Some(loc) => loc
      case None => {
        val loc = newLoc("PureLocal#" + fid)
        mergedPureLocalMap += (fid -> loc)
        loc
      }
    }
  }

  def mergePureLocal(s: State): State = {
    // s._1: Heap, h: Heap, kv: ((FunctionId, CallContext), Loc)
    val heap = pureLocalMap.foldLeft(s._1)((h, kv) => {
      val fid = kv._1._1 // function id
      val csLoc = kv._2 // context sensitive purelocal location
      val mergedLoc = getMergedPureLocal(fid)
      val h1 = h.update(mergedLoc, h(csLoc)) // merge "csLoc" into "mergedLoc"
      val h2 = h1.remove(csLoc)
      h2
    })
    State(heap, s._2)
  }

  // for global sparse analysis
  // inter-procedural successor node
  private var ipSuccMap: Map[ControlPoint, Set[ControlPoint]] = HashMap()
  def getIPSucc(cp: ControlPoint) = ipSuccMap(cp)
  def addIPEdge(from: ControlPoint, to: ControlPoint): Unit = {
    ipSuccMap += (from -> ( getSet(ipSuccMap, from) + to))
  }

  private var succMap: Map[Node, Set[Node]] = HashMap()
  def getSucc(node: Node) = succMap(node)

  // predecessor node
  private var predMap: Map[Node, Set[Node]] = HashMap()
  def getPred(node: Node) = predMap(node)

  // exception successor node
  private var excSuccMap: Map[Node, Node] = HashMap()
  def getExcSucc(node: Node) = excSuccMap.get(node)

  // exception predecessor node
  private var excPredMap: Map[Node, Set[Node]] = HashMap()
  def getExcPred(node: Node) = excPredMap(node)

  // Call <- Aftercall link
  private var callFromAftercallMap: Map[Node, Node] = HashMap()
  def getCallFromAftercall(aftercall: Node) = {
    callFromAftercallMap.get(aftercall) match {
      case Some(ac) => ac
      case None => throw new InternalError("CFGCall must have corresponding after-call")
    }
  }

  // Aftercall <- Call link
  private var aftercallFromCallMap: Map[Node, Node] = HashMap()
  def getAftercallFromCall(call: Node) = {
    aftercallFromCallMap.get(call) match {
      case Some(c) => c
      case None => throw new InternalError("CFGAfterCall must have corresponding call")
    }
  }

  // normal successor + exception successor + aftercallFromCall successor
  def getAllSucc(node: Node) = {
    val succs = getSet(succMap, node)
    val succs_exc = excSuccMap.get(node) match {
      case Some(n) => succs + n
      case None => succs
    }
    val succs_exc_ac = aftercallFromCallMap.get(node) match {
      case Some(n) => succs_exc + n
      case None => succs_exc
    }
    succs_exc_ac
  }

  private var callBlock: Set[Node] = HashSet()
  private var aftercallBlock: Set[Node] = HashSet()
  def getCalls() = callBlock
  def getAftercalls() = aftercallBlock

  // return variable for after-call node
  private var returnVarMap: Map[Node, CFGId] = HashMap()
  def getReturnVar(aftercall: Node) = returnVarMap.get(aftercall)

  // global fid
  private var globalFId = -1
  def getGlobalFId = globalFId
  def setGlobalFId(fid: FunctionId): Unit = globalFId = fid

  private def getSomeDefault[T,R](map: Map[T, R], n: T, default: R): R = {
    map.get(n) match {
      case Some(s) => s
      case None => default
    }
  }

  private def getSet[T,R](map: Map[T, Set[R]], n: T) = {
    getSomeDefault(map,n,Set[R]())
  }

  def dump_du(du: DUSet) = {
    System.out.println("== DU Set ==")
    nodes.foreach((n) => {
      System.out.println("* Node "+n)
      System.out.println("defset: "+du(n)._1.toString)
      System.out.println("useset: "+du(n)._2.toString)
    })
  }

  def dump_duset() = {
    System.out.println("== DU Set ==")
    nodes.foreach((n) => {
      val du = intraDefuseMap.get(n._1) match {
        case None => HashMap[Node, (LocSet, LocSet)]()
        case Some(du_) => du_
      }
      val joinpoints = joinpointsMap.get(n._1) match {
        case None => HashMap[ENode, LocSet]()
        case Some(j) => j
      }
      // entry point might be a joinpoint
      def phis(n:Node) = getLocSet(joinpoints, (n, KindI)) ++ entryPhis(n)
      def entryPhis(n: Node):LocSet = if(n._2 == LEntry || n._2 == LExit || n._2 == LExitExc) getLocSet(afdset, n._1)
                                      else LocSetBot
      def getDUSet(n:Node):(LocSet, LocSet) = {
        du.get(n) match {
          case None => (LocSetBot, LocSetBot)
          case Some(duset) => duset
        }
      }
      if((getDUSet(n)._1 ++ getDUSet(n)._2 ++ phis(n)) != LocSetBot) {
        System.out.println("* Node "+n)
        System.out.println("defset: "+DomainPrinter.printLocSet(getDUSet(n)._1) + "\n\t&& phis: " + DomainPrinter.printLocSet(phis(n)))
        System.out.println("useset: "+DomainPrinter.printLocSet(getDUSet(n)._2) + "\n\t&& phis: " + DomainPrinter.printLocSet(phis(n)))
      }
    })
  }

  // builder methods ----------------------------------------------------------
  def getInterDDG = {
    // inter-procedural DDG.
    // interDDG.toDot_dugraph()
    interDDG
  }

  var bypasses: HashMap[ControlPoint, LocSet] = HashMap()
  var bypassesUpdated: HashSet[(ControlPoint, FunctionId)] = HashSet()
  def getBypassingSet(cp: ControlPoint) = {
    bypasses.get(cp) match {
      case Some(s) => s
      case None => LocSetBot
    }
  }

  var bypassesExc: HashMap[ControlPoint, LocSet] = HashMap()
  var bypassesExcUpdated: HashSet[(ControlPoint, FunctionId)] = HashSet()
  def getBypassingExcSet(cp: ControlPoint) = {
    bypassesExc.get(cp) match {
      case Some(s) => s
      case None => LocSetBot
    }
  }
  var bypassingMap: HashMap[Node, LocSet] = HashMap()

  def updateBypassing(cp: ControlPoint, callee: FunctionId): Boolean = {
    if (!bypassesUpdated.contains((cp, callee))) {
      bypassesUpdated += ((cp, callee))
      // sound approximation passing set from call to after-call.
      val bypass_set = bypassingMap(cp._1) // candidate loc set to bypass
      // bypassed set by callee
      val bypassed_set = afdset(callee) // def set from the function
      // locset which must be bypassed.
      val need_set = bypass_set -- bypassed_set // candidate loc set - def set

      // insert bypassing locset
      bypasses.get(cp) match {
        case Some(s) => bypasses += (cp -> (s ++ need_set)) // is here a dead code?
        case None => bypasses += (cp -> need_set)
      }
      true
    } else {
      false
    }
  }

  def updateBypassingExc(cp: ControlPoint, callee: FunctionId): Boolean = {
    if (!bypassesExcUpdated.contains((cp, callee))) {
      bypassesUpdated += ((cp, callee))
      // sound approximation passing set from call to after-catch.
      val bypass_set = bypassingMap(cp._1)
      // bypassed set by callee
      val bypassed_set = afdset(callee)
      // locset which must be bypassed.
      val need_set = bypass_set -- bypassed_set

      bypassesExc.get(cp) match {
        case Some(s) => bypassesExc += (cp -> (s ++ need_set))
        case None => bypassesExc += (cp -> need_set)
      }
      true
    } else {
      false
    }
  }

  // flow and data dependency graph for each (function, context)
  var flowGraphMap: HashMap[(FunctionId, CallContext), (FlowGraph, DDGraph)] = HashMap()
  var flowGraphMapG: HashMap[(FunctionId, CallContext), FlowGraph] = HashMap()

  def getFlowGraph(fid: FunctionId, cc: CallContext) = {
    flowGraphMap.get((fid, cc)) match {
      case Some(pair) => pair
      case None => {
        val nodes = getReachableNodes(fid).toSet // in other words, these nodes are belong to the function. (node.fid == fid)
        val g = FlowGraph.makeGraph(nodes, (fid, LEntry)) // create a flow graph (new cfg)
        val ddg = new DDGraph(nodes, (fid, LEntry)) // create a data dependency graph
        flowGraphMap += ((fid,cc) -> (g, ddg))
        // System.out.println("==== make new Call context : fid : " + fid + ", C-C : " + cc)

        (g, ddg)
      }
    }
  }

  def getFlowGraph_(fid: FunctionId, cc: CallContext) = {
    flowGraphMapG.get((fid, cc)) match {
      case Some(fg) => fg
      case None => {
        val nodes = getReachableNodes(fid).toSet // in other words, these nodes are belong to the function. (node.fid == fid)
        val g = FlowGraph.makeGraph(nodes, (fid, LEntry)) // create a flow graph (new cfg)
        flowGraphMapG += ((fid,cc) -> g)

        // for global sparse analysis
        // add nodes for new call-context
        globalDDG.addNewCallContext(nodes.map(node =>((node, cc))))
        // System.out.println("==== make new Call context : fid : " + fid + ", C-C : " + cc)

        g
      }
    }
  }

  // dump FlowGraph & DDGraph to .dot file
  def dump_dugraph() = {
    flowGraphMap.foreach(kv => {
      System.out.println("== FG for " + getFuncName(kv._1._1) + " at " + kv._1._2 + " ==")
      kv._2._1.toDot_dugraph()
      System.out.println("== DDG for " + getFuncName(kv._1._1) + " at " + kv._1._2 + " ==")
      kv._2._2.toDot_dugraph()
    })
  }

  // Compute the defuse set between the functions
  def interFuncDefuse(callgraph: Map[FunctionId, Set[FunctionId]], fdu: Map[FunctionId, (LPSet, LPSet)]): Map[FunctionId, (LPSet, LPSet)] = {
    // reachableFuncs = globalFid + callgraph's FunctionIds
    val reachableFuncs = callgraph.foldLeft(Set(globalFId))((s, kv) => s ++ kv._2)
    val numOfFuncs = reachableFuncs.size

    // Number the functions in reverse post-order
    val fid2id: Map[FunctionId,Int] = Utils.get_reverse_postorder(globalFId, numOfFuncs, ((fid) => callgraph(fid)))
    // Build an array to access(Int -> FunctionId) in O(1)
    // (Reverse numbers again...)
    val id2fid: Array[FunctionId] = new Array(numOfFuncs)
    fid2id.foreach((n) => id2fid(numOfFuncs - n._2 - 1) = n._1)

    /**
     * If there exists a call edge from A to B (A -> B) then do this. (A and B are functions.)
     *   - A's def set += B's def set
     *   - A's use set += B's use set
     * until no change.
     * (To reach the fix-point quickly, traverses the functions in DFS order.)
     */
    def fixpoint(afdu: Map[FunctionId, (LPSet, LPSet)]): Map[FunctionId, (LPSet, LPSet)] = {
      val afdu2 = (0 to numOfFuncs - 1).foldLeft(afdu)((m, id) => {
        val fid = id2fid(id) // caller FunctionId
        val callees = getSet(callgraph, fid) // callee FunctionId set
        val du = callees.foldLeft(m(fid))((du2, succ) => {
          val succ_du = m(succ) // callee's (def, use) set
          (du2._1 ++ succ_du._1, du2._2 ++ succ_du._2) // "caller's (def, use) set" += "callee's (def, use) set"
        })
        m + (fid -> du)
      })
      if (afdu == afdu2)
        afdu
      else fixpoint(afdu2)
    }

    fixpoint(fdu)
  }

  // Get all reachable nodes from a specific node.
  private def reachable(e: Node): List[Node] = {
    var visited = Set[Node]()
    var result = List[Node]()

    def dfs(n: Node): Unit = {
      visited += (n)
      getAllSucc(n).foreach((c) => {
        if (!visited.contains(c))
          dfs(c)
      })
      result = (n) :: result
    }
    dfs(e)

    visited
    result
  }

  // Reachable nodes from LEntry node for each function
  private var reachableNodes = HashMap[FunctionId, List[Node]]()
  def computeReachableNodes(): Unit = computeReachableNodes(false)
  def computeReachableNodes(quiet: Boolean): Unit = {
    val functions = getFunctionIds() // get all function id set

    // for each function, computes reachable nodes from the function entry node
    if (!quiet)
      System.out.println("# computes reachable nodes")
    functions.foreach(fid => {
      reachableNodes += (fid -> (reachable((fid, LEntry))))
    })
  }
  def getReachableNodes(fid: FunctionId): List[Node] = {
    reachableNodes.get(fid) match {
      case Some(s) => s
      case None => {
        System.err.println("* Warning: there is no pre-computed reachable node for "+fid)
        getNodes.filter(n => n._1 == fid) // just filter out by checking "note.fid == fid"
      }
    }
  }

  // defuse set of the function
  private var intraDefuseMap: Map[FunctionId, Map[Node, (LocSet,LocSet)]] = HashMap()

  // use set of the function
  private var usesetForFuncMap: Map[FunctionId, LocSet] = HashMap()
  private var localization: Boolean = false
  def optionLocalization = localization
  def updateUseset(fid: FunctionId, set: LocSet) = {
    usesetForFuncMap.get(fid) match {
      case Some(s) => usesetForFuncMap += (fid -> (s ++ set))
      case None => usesetForFuncMap += (fid -> set)
    }
  }
  def getLocalizationSet(fid: FunctionId) = {
    usesetForFuncMap(fid)
  }

  // def set of the function
  private var afdset: HashMap[FunctionId, LocSet] = HashMap()
  /**
   * fid : FunctionId to compute intra defuse set
   * du : defuse set of nodes
   * afdu : defuse set of functions
   * callgraph : call edges from Node A(in fid) to callee(FunctionId set)
   */
  def computesIntraDefUseSet(fid: FunctionId, du: DUSet, afdu: Map[FunctionId, (LPSet, LPSet)], callgraph: Map[Node, Set[FunctionId]]) = {
    localization = true;

    // collects all callee FunctionIds
    val callees = callgraph.foldLeft(HashSet[FunctionId]())((S, kv) => S ++ kv._2)
    // collects all def sets of callee functions
    callees.foreach(fid => afdset += (fid -> afdu(fid)._1.toLSet))

    val nodes = getReachableNodes(fid)
    val intraDU =
      nodes.foldLeft[Map[Node,(LocSet,LocSet)]](Map())((m, n) => {
        n._2 match { // n._2: Label
          case LEntry => {
            // get the use set of this function and callees.
            val func_du = afdu(fid)
            updateUseset(fid, func_du._2.toLSet)
            // useset = empty
            // defset = all.
            m + (n -> (LBot, LBot)) // (LBot, LBot) ?
          }
          case LExit | LExitExc=> {
            // get the def set of this function and callees
            val func_du = afdu(fid)
            val node_du = du(n)
            // useset = (defset_for_func ++ defset_of_succs_func) ++ useset_for_node + #PureLocal
            // defset = empty
            m + (n -> (LBot, func_du._1.toLSet ++ node_du._2.toLSet + SinglePureLocalLoc))
          }
          case _ => {
            val duset = du(n)
            val defset_1 = duset._1.toLSet
            val useset_1 = duset._2.toLSet

            // call node
            val (defset_2, useset_2) =
              if (callBlock.contains(n)) {
                val succs_du =
                  callgraph.get(n) match {
                    case Some(succs) => {
                      succs.foldLeft((LBot, LBot))((S, succ) => {
                        val afdu_succ = afdu(succ)
                        (S._1 ++ afdu_succ._1.toLSet, S._2 ++ afdu_succ._2.toLSet)
                      })
                    }
                    case None => (LBot, LBot)
                  }
                // useset = useset_for_node ++ useset_for_succs_func + #ContextLoc
                // defset = defset_for_node
                val useset = useset_1 ++ succs_du._2 + ContextLoc
                val defset = defset_1
                (defset, useset)
              } else {
                (defset_1, useset_1)
              }

            // after-call node
            val (defset_3, useset_3) =
              if (aftercallBlock.contains(n)) {
                val call_node = callFromAftercallMap(n)
                val succs_du =
                  callgraph.get(call_node) match {
                    case Some(succs) => {
                      succs.foldLeft((LBot, LBot))((S, succ) => {
                        val afdu_succ = afdu(succ)
                        (S._1 ++ afdu_succ._1.toLSet, S._2 ++ afdu_succ._2.toLSet)
                      })
                    }
                    case None => (LBot, LBot)
                  }
                // -- defset_for_succs_func in useset: defined values in succs functions will be come through IP edges.
                // useset = useset_for_node -- defset_for_succs_func - #PureLocal - #ContextLoc
                // defset = defset_for_succs_func ++ defset_for_node + #PureLocal + #ContextLoc
                bypassingMap += (call_node -> succs_du._1) // sets which will be used by IP edges.
                (defset_2 ++ succs_du._1 + SinglePureLocalLoc + ContextLoc, useset_2 -- succs_du._1 - SinglePureLocalLoc - ContextLoc)
              } else {
                (defset_2, useset_2)
              }

            m + (n -> (defset_3, useset_3))
          }
        }
      })
    // XXX: there are some missing defs which is from callee's exit-exc.
    // fortunately, after-call's defs provide it now.
    val catches = excPredMap.keySet
    catches.foldLeft(intraDU)((m, cn) => {
      val preds = getSet(excPredMap, cn)
      val defs =
        preds.foldLeft(LocSetBot)((S, pred) => {
          m.get(pred) match {
            case Some(s) => S ++ s._1 ++ s._2
            case None => S
          }
        })
      // preds' defs must be included in use set since they are may-def.
      m.get(cn) match {
        case Some(du) => m + (cn -> (du._1 ++ defs, du._2 ++ defs))
        case None => m
      }
    })
  }

  /**
   * du : defuse set of nodes
   */
  def computesGlobalDefUseSet(du: DUSet, nodes: HashSet[Node]) = {
    nodes.foldLeft[Map[Node,(LocSet,LocSet)]](Map())((m, n) => {
      m + (n -> (du(n)._1.toLSet, du(n)._2.toLSet))
    })
  }

  def drawIntraDefUseGraph(cg: Map[CFGInst, Set[FunctionId]], du: DUSet): Unit =
    drawIntraDefUseGraph(cg, du, false)
  def drawIntraDefUseGraph(cg: Map[CFGInst, Set[FunctionId]], du: DUSet, quiet: Boolean): Unit = {
    if (!quiet)
      System.out.println("* Computes defuse graph")
    val reachableFuncs = cg.foldLeft(Set(globalFId))((s, kv) => s ++ kv._2)
    val numOfFuncs = reachableFuncs.size

    /*
     * Computes callgraph
     *   from: Map[CFGInst, Set[FunctionId]]
     *     to: Map[Node, Set[FunctionId]]
     */
    val callgraph_node: Map[Node, Set[FunctionId]] =
      cg.foldLeft[Map[Node, Set[FunctionId]]](Map())((m, kv) => {
        val caller = findEnclosingNode(kv._1)
        m.get(caller) match {
          case Some(callees) => m + (caller -> (kv._2 ++ callees))
          case None => m + (caller -> kv._2)
        }
      })
    /*
     * Computes callgraph
     *   from: Map[Node, Set[FunctionId]]
     *     to: Map[FunctionId, Set[FunctionId]]
     */
    val callgraph: Map[FunctionId, Set[FunctionId]] =
      callgraph_node.foldLeft[Map[FunctionId, Set[FunctionId]]](Map())((m, kv) => {
        val caller = kv._1._1
        m.get(caller) match {
          case Some(callees) => m + (caller -> (kv._2 ++ callees))
          case None => m + (caller -> kv._2)
        }
      })

    // computes defuse set for each function.
    val fdu =
      reachableFuncs.foldLeft[Map[FunctionId, (LPSet, LPSet)]](Map())((m, fid) => {
        val nodes = getReachableNodes(fid)
        val duset = nodes.foldLeft((LPBot,LPBot))((S, n) => {
          (S._1 ++ du(n)._1, S._2 ++ du(n)._2)
        })
        m + (fid -> duset)
      })

    // fixpoint computation for defuse set considering call relation.
    val afdu = interFuncDefuse(callgraph, fdu)

    // computes intra-procedural defuse graph
    // for each reachable functions,
    reachableFuncs.foreach((fid) => {
      //System.out.println("* For each reachable function "+fid+",")
      val nodes = getReachableNodes(fid).toSet

      // computes intra-procedural defuse set considering call relation.
      val intra_defuse = computesIntraDefUseSet(fid, du, afdu, callgraph_node)
      intraDefuseMap += (fid -> intra_defuse)

      // constructs new control-flow graph including call/after-call, exception edges.
      def succs(node: Node): Set[Node] = {
        val n_1 = aftercallFromCallMap.get(node) match {
          case Some(n) => HashSet(n)
          case None => HashSet()
        }
        getSucc(node) ++ n_1
      }
      def succs_e(node: Node): Set[Node] = {
        excSuccMap.get(node) match {
          case Some(n) => HashSet[Node](n)
          case None => HashSet[Node]()
        }
      }

      val cfg = TGraph.makeGraph[Node](nodes, (fid, LEntry), succs, succs_e)
      val ecfg = EGraph.makeGraph(cfg)

      // variable set
      val variables = intra_defuse.foldLeft(LBot)((s, du) => s ++ du._2._1 ++ du._2._2)
      //System.out.println("* Draw intra def/use graph for "+fid)
      //System.out.println("  The number of du entries: "+variables.size)

      // computes dominance frontier
      val dt = SSA.buildDomTree[ENode](ecfg)

      // computes joinpoints for entry of this function.
      val joinpoints = SSA.computesJoinpoints(ecfg, intra_defuse, variables, dt)
      joinpointsMap += (fid -> joinpoints)

      val ddg = SSA.draw_defuse(ecfg, dt, intra_defuse, joinpoints, variables)
      val ddg_edgeonly = ddg.toEdgeOnlyGraph

      if (interDDG == null) interDDG = ddg_edgeonly
      else interDDG = interDDG + ddg_edgeonly

      intraCFGMap += (fid -> cfg)
    })

    callgraph_node.foreach(kv => {
      val call = kv._1
      val aftercall = getAftercallFromCall(call)
      val aftercatch = excSuccMap(aftercall)
      kv._2.foreach(callee => {
        val entry = (callee, LEntry)
        val exit = (callee, LExit)
        val exitexc = (callee, LExitExc)

        interDDG.addEdge(call, entry)
        interDDG.addEdge(exit, aftercall)
        interDDG.addEdge(exitexc, aftercatch)
      })
    })
    interDDG.entry = (0, LEntry)
  }

  def drawIntraDDG(cg: Map[CFGInst, Set[FunctionId]], du: DUSet): Unit =
    drawIntraDDG(cg, du, false)

  def drawIntraDDG(cg: Map[CFGInst, Set[FunctionId]], du: DUSet, quiet: Boolean): Unit = {
    if (!quiet)
      System.out.println("* Computes defuse graph")
    val reachableFuncs = cg.foldLeft(Set(globalFId))((s, kv) => s ++ kv._2)
    val numOfFuncs = reachableFuncs.size

    // computes callgraph
    val callgraph_node: Map[Node, Set[FunctionId]] =
      cg.foldLeft[Map[Node, Set[FunctionId]]](Map())((m, kv) => {
        val caller = findEnclosingNode(kv._1)
        m.get(caller) match {
          case Some(callees) => m + (caller -> (kv._2 ++ callees))
          case None => m + (caller -> kv._2)
        }
      })
    val callgraph: Map[FunctionId, Set[FunctionId]] =
      callgraph_node.foldLeft[Map[FunctionId, Set[FunctionId]]](Map())((m, kv) => {
        val caller = kv._1._1
        m.get(caller) match {
          case Some(callees) => m + (caller -> (kv._2 ++ callees))
          case None => m + (caller -> kv._2)
        }
      })

    // computes defuse set for each function.
    val fdu =
      reachableFuncs.foldLeft[Map[FunctionId, (LPSet, LPSet)]](Map())((m, fid) => {
        val nodes = getReachableNodes(fid)
        val duset = nodes.foldLeft((LPBot,LPBot))((S, n) => {
          (S._1 ++ du(n)._1, S._2 ++ du(n)._2)
        })
        m + (fid -> duset)
      })

    // fixpoint computation for defuse set considering call relation.
    val afdu = interFuncDefuse(callgraph, fdu)

    // computes intra-procedural defuse graph
    // for each reachable functions,
    reachableFuncs.foreach((fid) => {
      //System.out.println("* For each reachable function "+fid+",")
      val nodes = getReachableNodes(fid).toSet

      // computes intra-procedural defuse set considering call relation.
      val intra_defuse = computesIntraDefUseSet(fid, du, afdu, callgraph_node)
      intraDefuseMap += (fid -> intra_defuse)

      // constructs new control-flow graph including call/after-call, exception edges.
      def succs(node: Node): Set[Node] = {
        val n_1 = aftercallFromCallMap.get(node) match {
          case Some(n) => HashSet(n)
          case None => HashSet()
        }
        getSucc(node) ++ n_1
      }
      def succs_e(node: Node): Set[Node] = {
        excSuccMap.get(node) match {
          case Some(n) => HashSet[Node](n)
          case None => HashSet[Node]()
        }
      }

      val cfg = TGraph.makeGraph(nodes, (fid, LEntry), succs, succs_e)
      val ecfg = EGraph.makeGraph(cfg)

      // variable set
      val variables = intra_defuse.foldLeft(LBot)((s, du) => s ++ du._2._1 ++ du._2._2)
      //System.out.println("* Draw intra def/use graph for "+fid)
      //System.out.println("  The number of du entries: "+variables.size)

      // computes dominance frontier
      val dt = SSA.buildDomTree[ENode](ecfg)

      // computes joinpoints for entry of this function.
      val joinpoints = SSA.computesJoinpoints(ecfg, intra_defuse, variables, dt)
      joinpointsMap += (fid -> joinpoints)

      val ddg = SSA.draw_defuse(ecfg, dt, intra_defuse, joinpoints, variables)

      if (interDDG_ == null) interDDG_ = ddg
      else interDDG_ = interDDG_ + ddg

      intraCFGMap += (fid -> cfg)
    })
    interDDG = interDDG_.toEdgeOnlyGraph
    // interDDG_.toDot_dugraph()

    callgraph_node.foreach(kv => {
      val call = kv._1
      val aftercall = getAftercallFromCall(call)
      val aftercatch = excSuccMap(aftercall)
      kv._2.foreach(callee => {
        val entry = (callee, LEntry)
        val exit = (callee, LExit)
        val exitexc = (callee, LExitExc)

        interDDG.addEdge(call, entry)
        interDDG.addEdge(exit, aftercall)
        interDDG.addEdge(exitexc, aftercatch)
      })
    })
    interDDG.entry = (0, LEntry)
  }

  var globalFG_ : TGraph[Node] = null
  var globalDT: DomTree[ENode] = null
  def drawGlobalDDG(cg: Map[CFGInst, Set[FunctionId]], du: DUSet): Unit =
    drawGlobalDDG(cg, du, false)
  def drawGlobalDDG(cg: Map[CFGInst, Set[FunctionId]], du: DUSet, quiet: Boolean): Unit = {
    // Initialize call context for context-sensitivity
    CallContext.initialize

    if (!quiet)
      System.out.println("* Computes defuse graph")
    val reachableFuncs = cg.foldLeft(Set(globalFId))((s, kv) => s ++ kv._2)
    val numOfFuncs = reachableFuncs.size

    // computes callgraph
    val callgraph_node: Map[Node, Set[FunctionId]] =
      cg.foldLeft[Map[Node, Set[FunctionId]]](Map())((m, kv) => {
        val caller = findEnclosingNode(kv._1)
        m.get(caller) match {
          case Some(callees) => m + (caller -> (kv._2 ++ callees))
          case None => m + (caller -> kv._2)
        }
      })
    val callgraph: Map[FunctionId, Set[FunctionId]] =
      callgraph_node.foldLeft[Map[FunctionId, Set[FunctionId]]](Map())((m, kv) => {
        val caller = kv._1._1
        m.get(caller) match {
          case Some(callees) => m + (caller -> (kv._2 ++ callees))
          case None => m + (caller -> kv._2)
        }
      })

    // computes defuse set for each function.
    val fdu =
      reachableFuncs.foldLeft[Map[FunctionId, (LPSet, LPSet)]](Map())((m, fid) => {
        val nodes = getReachableNodes(fid)
        val duset = nodes.foldLeft((LPBot,LPBot))((S, n) => {
          (S._1 ++ du(n)._1, S._2 ++ du(n)._2)
        })
        m + (fid -> duset)
      })

    // fixpoint computation for defuse set considering call relation.
    val afdu = interFuncDefuse(callgraph, fdu)

    // for global sparse analysis
    var reachableNodes = HashSet[Node]()
    var globalVariables = LocSetBot

    // computes intra-procedural defuse graph
    // for each reachable functions,
    reachableFuncs.foreach((fid) => {
      //System.out.println("* For each reachable function "+fid+",")
      val nodes = getReachableNodes(fid).toSet

      // for global sparse analysis
      reachableNodes ++= nodes

      // computes intra-procedural defuse set considering call relation.
      val intra_defuse = computesIntraDefUseSet(fid, du, afdu, callgraph_node)
      intraDefuseMap += (fid -> intra_defuse)

      // constructs new control-flow graph including call/after-call, exception edges.
      def succs(node: Node): Set[Node] = {
        val n_1 = aftercallFromCallMap.get(node) match {
          case Some(n) => HashSet(n)
          case None => HashSet()
        }
        getSucc(node) ++ n_1
      }
      def succs_e(node: Node): Set[Node] = {
        excSuccMap.get(node) match {
          case Some(n) => HashSet[Node](n)
          case None => HashSet[Node]()
        }
      }

      val cfg = TGraph.makeGraph(nodes, (fid, LEntry), succs, succs_e)
      val ecfg = EGraph.makeGraph(cfg)

      // variable set
      val variables = intra_defuse.foldLeft(LBot)((s, du) => s ++ du._2._1 ++ du._2._2)
      //System.out.println("* Draw intra def/use graph for "+fid)
      //System.out.println("  The number of du entries: "+variables.size)

      // for global sparse analysis
      globalVariables ++= variables

      // computes dominance frontier
      val dt = SSA.buildDomTree[ENode](ecfg)

      // computes joinpoints for entry of this function.
      val joinpoints = SSA.computesJoinpoints(ecfg, intra_defuse, variables, dt)
      joinpointsMap += (fid -> joinpoints)

      val ddg = SSA.draw_defuse(ecfg, dt, intra_defuse, joinpoints, variables)

      if (interDDG_ == null) interDDG_ = ddg
      else interDDG_ = interDDG_ + ddg

      intraCFGMap += (fid -> cfg)
    })
    interDDG = interDDG_.toEdgeOnlyGraph
    // interDDG_.toDot_dugraph()

    var call_to_entry: Map[Node, Set[Node]] = HashMap()
    var exit_to_aftercall: Map[Node, Set[Node]] = HashMap()
    var excexit_to_aftercatch: Map[Node, Set[Node]] = HashMap()
    def addMap[T,R](map:Map[T, Set[R]], key:T, elem:R) = {
      map.get(key) match {
        case Some(s) => map + (key -> (s + elem))
        case None => map + (key -> HashSet[R](elem))
      }
    }
    callgraph_node.foreach(kv => {
      val call = kv._1
      val aftercall = getAftercallFromCall(call)
      val aftercatch = excSuccMap(aftercall)
      kv._2.foreach(callee => {
        val entry = (callee, LEntry)
        val exit = (callee, LExit)
        val exitexc = (callee, LExitExc)

        interDDG.addEdge(call, entry)
        interDDG.addEdge(exit, aftercall)
        interDDG.addEdge(exitexc, aftercatch)

        // for global sparse analysis
        call_to_entry = addMap(call_to_entry, call, entry)
        exit_to_aftercall = addMap(exit_to_aftercall, exit, aftercall)
        excexit_to_aftercatch = addMap(excexit_to_aftercatch, exitexc, aftercatch)
      })
    })


    // for global sparse analysis
    def globalSuccs(node: Node): Set[Node] = {
      getSucc(node) ++ getSet(call_to_entry, node) ++ getSet(exit_to_aftercall, node)
    }
    def globalSuccs_e(node: Node): Set[Node] = {
      val exc_node = excSuccMap.get(node) match {
        case Some(n) => HashSet[Node](n)
        case None => HashSet[Node]()
      }
      exc_node ++ getSet(excexit_to_aftercatch, node)
    }

    // make global flow-graph_init
    globalFG_ = TGraph.makeGraph(reachableNodes, (globalFId, LEntry), globalSuccs, globalSuccs_e)
    val globaleEG = EGraph.makeGraph(globalFG_)

    // computes dominance frontier
    globalDT = SSA.buildDomTree[ENode](globaleEG)

    globalDUMap = computesGlobalDefUseSet(du, reachableNodes)
//    System.out.println("* GlobalDUMap")
//    globalDUMap.foreach(kv => {
//      System.out.println("= "+kv._1)
//      System.out.println("defuse: " + DomainPrinter.printLocSet(kv._2._1))
//      System.out.println("useset: " + DomainPrinter.printLocSet(kv._2._2))
//    })

    // computes joinpoints for entry of this function.
    globalJoinpoints = SSA.computesJoinpoints(globaleEG, globalDUMap, globalVariables, globalDT)
    globalDDG_ = SSA.draw_defuse(globaleEG, globalDT, globalDUMap, globalJoinpoints, globalVariables)

    interDDG.entry = (0, LEntry)
    globalFG = GFlowGraph.makeGraph(((globalFId, LEntry), CallContext.globalCallContext))
  }

  // Pre-computed DDG for whole program(nodes between procedures are not connected).
  private var interDDG_ : DDGraph = null
  private var globalDDG_ : DDGraph = null
  private var globalJoinpoints: Map[ENode, LocSet] = null
  private var globalDUMap : Map[Node,(LocSet,LocSet)] = null

  // inter-procedural Data Dependency Graph for computing worklist order.
  private var interDDG: DGraph[Node] = null

  private var intraCFGMap: HashMap[FunctionId, TGraph[Node]] = HashMap()
  private var joinpointsMap: Map[FunctionId, Map[ENode, LocSet]] = HashMap()

  def isEmptyNode(node: Node) = {
    if (aftercallBlock.contains(node)) false
    else {
      getCmd(node) match {
        case Block(i) => (i.length == 0) || (intraDefuseMap(node._1)(node)._1.isEmpty)
        case _ => false
      }
    }
  }

  def recoverGOutEdges(src: ControlPoint): HashSet[(ControlPoint, ControlPoint)] = {
    if (!globalFG.isRecovered(src)) {
      globalFG.recovered(src)

      val dsts = getSucc(src._1)
      dsts.foldLeft[HashSet[(ControlPoint,ControlPoint)]](HashSet())((S, dst) => {
        val S_2 =
          if (isEmptyNode(dst)) {
//            System.out.println("node "+dst+" is empty.")
            recoverGOutEdges((dst, src._2))
          }
          else
            HashSet()
        S ++ S_2 + ((src, (dst, src._2)))
      })
    } else {
      HashSet()
    }
  }

  def recoverGOutExcEdge(src: ControlPoint): HashSet[(ControlPoint, ControlPoint)] = {
    if (!globalFG.isExcRecovered(src)) {
      globalFG.excRecovered(src)

      val dsts =
        getExcSucc(src._1) match {
          case Some(n) => HashSet(n)
          case None => HashSet()
        }
//      System.out.println("recover out exc-edges from "+src+" to "+dsts)
      dsts.foldLeft[HashSet[(ControlPoint,ControlPoint)]](HashSet())((S, dst) => {
        val S_2 =
          if (isEmptyNode(dst)) {
            //            System.out.println("node "+dst+" is empty.")
            recoverGOutEdges((dst, src._2))
          }
          else
            HashSet()
        S ++ S_2 + ((src, (dst, src._2)))
      })
    } else {
      HashSet()
    }
  }

  def recoverOutEdges(fg: FlowGraph, src: Node): HashSet[(Node,Node)] = {
    if (!fg.isRecovered(src)) {
      fg.recovered(src)
      val fid = src._1
      val dsts = intraCFGMap(fid).getNormalSuccs(src)
      // System.out.println("recover out edges from "+src+" to "+dsts)
      dsts.foldLeft[HashSet[(Node,Node)]](HashSet())((S, dst) => {
        val S_2 =
          if (isEmptyNode(dst)) {
            // System.out.println("node "+dst+" is empty.")
            recoverOutEdges(fg, dst)
          }
          else
            HashSet()
        S ++ S_2 + ((src, dst))
      })
    } else {
      HashSet()
    }
  }

  def recoverOutExcEdge(fg: FlowGraph, src: Node): HashSet[(Node,Node)] = {
    if (!fg.isExcRecovered(src)) {
      fg.excRecovered(src)
      val fid = src._1
      val dsts = intraCFGMap(fid).getExcSuccs(src)
      // System.out.println("recover out exc-edges from "+src+" to "+dsts)
      dsts.foldLeft[HashSet[(Node,Node)]](HashSet())((S, dst) => S + ((src, dst)))
    } else {
      HashSet()
    }
  }

  def recover_intra_dugraph(fg: FlowGraph, ddg: DDGraph, edges: Set[(Node,Node)], excEdges: Set[(Node,Node)]): Set[Node] = {
    val fid = fg.fid
    var recovered_nodes = Set[Node]()

    // add new edges
    edges.foreach(edge => fg.addEdge(edge._1, edge._2))
    excEdges.foreach(edge => fg.addExcEdge(edge._1, edge._2))
    val g = EGraph.makeGraph(fg)

    // find target nodes to be recovered
    val cs_normal = edges.foldLeft(Set[Node]())((cs, edge) => cs + edge._2)
    val cs_exc = excEdges.foldLeft(Set[Node]())((cs, edge) => cs + edge._2)

    val du = intraDefuseMap(fid)
    val joinpoints = joinpointsMap(fid)
    val dt = SSA.buildDomTree[ENode](g)
    val du_ =
      du.foldLeft(Map[ENode, (LocSet, LocSet)]())((S, e) => {
        S +
        ((e._1,KindI) -> (LocSetBot, e._2._2)) +
        ((e._1,KindO) -> (e._2._1, LocSetBot)) +
        ((e._1,KindOE) -> (e._2._1, LocSetBot))
      })
    val phis = getLocSet(joinpoints, _: ENode)
    def lhsof(n: ENode) = {
      n._2 match {
        case KindO | KindOE => du_(n)._1 ++ phis((n._1, KindI))
        case KindI => LocSetBot
      }
    }
    // add DDG edges for each of locs from esrc to dst.
    def connect(esrc: ENode, dst: Node, locs: LocSet) = {
      if (!locs.isEmpty) {
        esrc match {
          case (src,KindO) => {
            if (ddg.addEdges(src, dst, locs))
              recovered_nodes += src
          }
          case (src,KindOE) => {
            if (ddg.addExcEdges(src, dst, locs))
              recovered_nodes += src
          }
          case _ => throw new InternalError("Impossible case")
        }
      }
    }

    // recover reaching defs for each of locs.
    def reaching_def(dst: Node, src: ENode, locs: LocSet): Unit = {
      if (!locs.isEmpty) {
        val defs = lhsof(src)

        val defined_here =
          if (dt.hasParent(src)) {
            defs.intersect(locs)
          } else {
            // assumes that every variable is defined at entry node.
            locs
          }

        connect(src, dst, defined_here)

        val rest = (locs -- defined_here)
        if (!rest.isEmpty) {
          reaching_def(dst, dt.getParent(src), rest)
        }
      }
    }

    // for each nodes to be recovered,
    (cs_exc ++ cs_normal).foreach(dst => {
      val dest = (dst, KindI)
      val locs = du(dst)._2 ++ phis(dest)

      g.getPreds(dest).foreach(pred => {
        reaching_def(dst, pred, locs)
      })
    })

    recovered_nodes
  }

  // for global sparse analysis
  // Global Flow Graph
  var globalFG:GFlowGraph = null
  def getGlobalFG:GFlowGraph = globalFG
  // Global DDG for incremental generation.
  private var globalDDG:GDDGraph =  new GDDGraph(HashSet[ControlPoint](), ((globalFId, LEntry), CallContext.globalCallContext))
  def getGlobalDDG:GDDGraph = globalDDG
  // DJ Graph for incremental dominator tree
  var dj_graph = new DJGraph(((globalFId, LEntry), CallContext.globalCallContext))

  def recover_dugraph(edges: Set[(ControlPoint,ControlPoint)], excEdges: Set[(ControlPoint,ControlPoint)]): Set[ControlPoint] = {
    var recovered_nodes = Set[ControlPoint]()

    // 1. process intra flow graph
    // add new edges in intra flow-graph

    // adds edges to the global flow graph.
    edges.foreach(edge => globalFG.addNode(edge._2))
    excEdges.foreach(edge => globalFG.addNode(edge._2))
    edges.foreach(edge => globalFG.addEdge(edge._1, edge._2))
    excEdges.foreach(edge => globalFG.addExcEdge(edge._1, edge._2))

    // to add DJ Graph edge
//    edges.foreach(edge => dj_graph.updateInsertEdge(getGlobalFG, edge._1, edge._2))
//    excEdges.foreach(edge => dj_graph.updateInsertEdge(getGlobalFG, edge._1, edge._2))
    // XXX: DEBUG LOG
//    globalFG.toDot_dugraph()

    // 5. make EGraph using Global Control Flow(GFlowGraph)
    val globalEG = GEGraph.makeGraph(globalFG)
//    System.out.println("* globalEG")
//    System.out.println(globalEG.toDot_dugraph())
    val globalDT = SSA.buildDomTree[GENode](globalEG)


    // merge all nodes in CFG for global sparse analysis
    // 6. get a duset map from ENode to a pair of a def set and a use set
    val du_ = globalDUMap.foldLeft(Map[ENode, (LocSet, LocSet)]())((S, e) => {
      S +
      ((e._1, KindI) -> (LocSetBot, e._2._2)) +
      ((e._1, KindO) -> (e._2._1, LocSetBot)) +
      ((e._1, KindOE) -> (e._2._1, LocSetBot))
    })

    // get all locations which are defined and used in a program
    // Warning : #PureLocal#n is not used in a global sparse analysis. Instead, #PureLocal is used in each function call-context
    // 7. using a duset map, get locations which are used or defined in a program.
    val variables = du_.foldLeft(LBot)((s, du) => s ++ du._2._1 ++ du._2._2)

    // 8. calculate joinpoints using global flow-graph and dominance tree
    val joinpoints = globalJoinpoints

    // XXX: DEBUG LOG
//    System.out.println("* joinpoints")
//    joinpoints.foreach(s => {
//       System.out.println(s._1 + " - " + DomainPrinter.printLocSet(s._2))
//    })

    // 9. Set phi-nodes in a global flow-graph using joinpoints
    //    entry should not be a phi-node
    //    entryPhis is only for debugging
    def phis(n:GENode) = {
      getLocSet(joinpoints, (n._1._1,n._2))// ++ entryPhis(n._1._1)
    }
    def entryPhis(n:Node):LocSet = {
      afdset.get(n._1) match {
        case Some(lset) if(n._2 == LEntry) => lset
        case _ => LocSetBot
      }
    }
    // get def set ++ phis in a node
    def lhsof(n: GENode) = {
      n._2 match {
        case KindO | KindOE => {
          du_.get(toENode(n)) match {
            case Some(s) => s._1 ++ phis((n._1, KindI))
            case None => phis((n._1, KindI))
          }
        }
        case KindI => LocSetBot
      }
    }
    // add DDG edges for each of locs from esrc to dst.
    def connect(esrc: GENode, dst: ControlPoint, locs: LocSet) = {
      if (!locs.isEmpty) {
        esrc match {
          case (src,KindO) => {
//            System.out.println("connect: "+esrc+" -> "+dst+" with "+DomainPrinter.printLocSet(locs))
            if (globalDDG.addEdges(src, dst, locs))
              recovered_nodes += src
          }
          case (src,KindOE) => {
//            System.out.println("connect: "+esrc+" -> "+dst+" with "+DomainPrinter.printLocSet(locs))
            if (globalDDG.addExcEdges(src, dst, locs))
              recovered_nodes += src
          }
          case _ => throw new InternalError("Impossible case")
        }
      }
    }
    def toENode(genode: GENode):ENode = {
      (genode._1._1, genode._2)
    }

    // recover reaching defs for each of locs.
    def reaching_def(dst: ControlPoint, src: GENode, locs: LocSet): Unit = {
      if (!locs.isEmpty) {
        val defs = lhsof(src)
        val defined_here =
          if (globalDT.hasParent(src)) {
            defs.intersect(locs)
          } else {
            // assumes that every variable is defined at global entry node.
            locs
          }

        connect(src, dst, defined_here)

        val rest = (locs -- defined_here)
          if (!rest.isEmpty) {
            reaching_def(dst, globalDT.getParent(src), rest)
          }
      }
    }

    // find target nodes to be recovered in a call-context
    val cs_normal = edges.foldLeft(Set[ControlPoint]())((cs, edge) => cs + edge._2)
    val cs_exc = excEdges.foldLeft(Set[ControlPoint]())((cs, edge) => cs + edge._2)

    // for each nodes to be recovered,
    (cs_exc ++ cs_normal).foreach(dst => {
      val dest = (dst, KindI)
      val locs = globalDUMap(dst._1)._2 ++ phis((dst, KindI))
//      System.out.println("dest: "+dest)
//      System.out.println("locs: "+locs)

      globalEG.getPreds(dest).foreach(pred => {
        reaching_def(dst, pred, locs)
      })
    })
//    System.out.println("recovered_nodes: "+recovered_nodes)

    recovered_nodes
  }

  // removable
  def toDot_DDG() = {
    System.out.println("==Original Candidates==")
    interDDG_.dump_candidates
    System.out.println("==Original==")
    interDDG_.toDot_dugraph()
    System.out.println("==After==")
    dump_dugraph()
    System.out.println("==JoinpointsMap==")
    joinpointsMap.foreach(fid_map => {
      val (fid, map) = fid_map
      map.foreach(nl => {
        val (node, locSet) = nl
        System.out.println(" joinpoint : " + node + " locset : " + locSet)
      })
    })
  }

  def draw_intra_dugraph_incremental(fg: FlowGraph, ddg: DDGraph, edges: Set[(Node,Node)], excEdges: Set[(Node,Node)]): Set[Node] = {
    val fid = fg.fid
    val du = intraDefuseMap(fid)
    val joinpoints = joinpointsMap(fid)

    val variables = du.foldLeft(LBot)((s, du) => s ++ du._2._1 ++ du._2._2)

    // add new edges
    edges.foreach(edge => fg.addEdge(edge._1, edge._2))
    excEdges.foreach(edge => fg.addExcEdge(edge._1, edge._2))

    val g = EGraph.makeGraph(fg)
    SSA.draw_defuse_(g, ddg, du, joinpoints, variables)
  }

  private var funcCount = 0
  private var userFuncCount = 0
  private var blockCount = 0
  private var addrCount = 1
  private var instCount = 0

  def getFuncCount() = funcCount
  def getAddrCount = addrCount

  def setUserFuncCount = userFuncCount = funcCount
  def getUserFuncCount = userFuncCount
  def isUserFunction(fid: FunctionId): Boolean = (fid < userFuncCount) && (fid != FIdTop)

  def newFunction(argsName: ArgumentsName, args: ArgVars, locals: LocalVars, name: String, info: Info): FunctionId = {
    // set-up function information
    val fid = funcCount;
    funcCount += 1

    // set-up Entry
    val entryNode = (fid, LEntry)
    initNode(entryNode)
    cmdMap += (entryNode -> Entry)

    // set-up Exit
    val exitNode = (fid, LExit)
    initNode(exitNode)
    cmdMap += (exitNode -> Exit)

    // set-up ExitExc
    val exitExcNode = (fid, LExitExc)
    initNode(exitExcNode)
    cmdMap += (exitExcNode -> ExitExc)

    // set-up function map
    funcMap += (fid -> (argsName, args, locals, List(entryNode, exitNode, exitExcNode), name, info))
    fid
  }


  def addTopFunction(argsName: ArgumentsName, args: ArgVars, locals: LocalVars, name: String, info: Info): Unit = {
    // set-up Entry
    val entryNode = (FIdTop, LEntry)
    initNode(entryNode)
    cmdMap += (entryNode -> Entry)

    // set-up Exit
    val exitNode = (FIdTop, LExit)
    initNode(exitNode)
    cmdMap += (exitNode -> Exit)

    // set-up ExitExc
    val exitExcNode = (FIdTop, LExitExc)
    initNode(exitExcNode)
    cmdMap += (exitExcNode -> ExitExc)

    // set-up function map
    funcMap += (FIdTop -> (argsName, args, locals, List(entryNode, exitNode, exitExcNode), name, info))
  }

  def newBlock(fid: FunctionId): BlockNode = {
    // set-up Block node
    val bid = blockCount
    blockCount += 1
    val blockNode = (fid, LBlock(bid))
    initNode(blockNode)

    // initialize as empty block
    cmdMap += (blockNode -> Block(Nil))

    // add Block node to the funcMap
    funcMap(fid)._4 :+ blockNode

    blockNode
  }

  def newAfterCallBlock(fid: FunctionId, returnVar: CFGId): BlockNode = {
    val blockNode = newBlock(fid)
    returnVarMap += (blockNode -> returnVar)

    // add Block node to the funcMap
    funcMap(fid)._4 :+ blockNode

    blockNode
  }

  private def initNode(node: Node): Unit = {
    // Extend nodes list.
    nodes ::= node

    // Extend succ/pred mappings with empty sets.
    // This will ensure that succ/pred mappings always exist for every node.
    // Note that exception successor is not set, but single node.
    succMap += (node -> HashSet())
    predMap += (node -> HashSet())
    excPredMap += (node -> HashSet())
  }

  def addEdge(from: Node, to: Node): Unit = {
    succMap += (from -> (succMap(from) + to))
    predMap += (to -> (predMap(to) + from))
  }

  def removeEdge(from: Node, to: Node): Unit = {
    succMap += (from -> (succMap(from) - to))
    predMap += (to -> (predMap(to) - from))
  }

  def addEdge(from: List[Node], to: Node): Unit = {
    from.foreach((fr) => addEdge(fr, to))
  }

  def addExcEdge(from: Node, to: Node): Unit = {
    if (DEBUG) checkExcSucc(from, to)
    excSuccMap += (from -> to)
    excPredMap += (to -> (excPredMap(to) + from))
  }

  def removeExcEdge(from: Node, to: Node): Unit = {
    excSuccMap -= from
    excPredMap += (to -> (predMap(to) - from))
  }

  def addExcEdge(from: List[Node], to: Node): Unit = {
    from.foreach((fr) => addExcEdge(fr, to))
  }

  private def checkExcSucc(from: Node, to: Node): Unit = {
    excSuccMap.get(from) match {
      case Some(n) =>
        if (n != to) throw new InternalError("Exception successor must be single node.")
      case None => ()
    }
  }

  def addCall(call: BlockNode, aftercall: BlockNode): Unit = {
    callFromAftercallMap += (aftercall -> call)
    aftercallFromCallMap += (call -> aftercall)
    callBlock += call
    aftercallBlock += aftercall
  }

  def addInst(node: BlockNode, inst: CFGInst): Unit = {
    val block = cmdMap(node).asInstanceOf[Block]
    /* TODO: need optimization - inefficient quadratic list append */
    cmdMap += (node -> Block(block.insts ++ List(inst)))
    enclosingNodeMap = enclosingNodeMap + (inst.getInstId -> node)
  }

  /* newAddress : Unit -> Address */
  def newAddress(): Address = {
    val addr = addrCount
    addrCount += 1
    addr
  }

  def newLoc(name: String) = {
    val addr = addrCount
    addrCount += 1
    registerPredefLoc(addr, Recent, name)
  }

  /* newInstId : Unit -> InstId */
  def newInstId(): InstId = {
    val iid = instCount
    instCount += 1
    iid
  }

  def addFileNoOp(file: String, noop: CFGInst) = {
    fileToNoOpMap += (file -> noop)
  }

  def dump(): Unit = {
    for (key <- nodes) {
      cmdMap(key) match {
        case Block(insts) =>
          System.out.println(key.toString)
          returnVarMap.get(key) match {
            case Some(returnVar) =>
              System.out.println("    [EDGE] after-call(" + returnVar + ")")
            case None => ()
          }
          for (inst <- insts) {
            System.out.println("    [" + inst.getInstId + "] " + inst.toString)
          }
          System.out.println("\n")
        case _ => ()
      }
    }
  }
  def getDDGStr(ddg0: Boolean): String = {
    var str = "digraph \"DirectedGraph\" {\n"
    if(ddg0) {
      str += interDDG_.toDot_String
    }
    else {
      var i = 0
      flowGraphMap.foreach(kv => {
        str += "subgraph cluster" + i + " {\n"
        str += "label = \"fid : " + getFuncName(kv._1._1) + ", CallContext : " + kv._1._2 + ";\n"
        i += 1
        str += kv._2._2.toDot_String
        str += "\n}\n"
      })
    }
    str += "\n}"
    str
  }

  def getFGStr: String = {
    var str = "digraph \"DirectedGraph\" {\n"
    var i = 0
    flowGraphMap.foreach(kv => {
      str += "subgraph cluster" + i + " {\n"
      str += ("label = \"fid : " + getFuncName(kv._1._1) + "(" + kv._1._1 + "), CallContext : " + kv._1._2 + "\";\n")
      i += 1
      str += kv._2._1.toDot_String
      str += "\n}\n"
    })
    str += "\n}"
    str
  }

  def getGFGStr: String = {
    def getLabel(node: Node): String = {
      node._2 match {
        case LBlock(id) => "Block"+id
        case LEntry => "Entry" + node._1
        case LExit => "Exit" + node._1
        case LExitExc => "ExitExc" + node._1
      }
    }
    def getDomLabel(node: ENode): String = {
      node._1._2 match {
        case LBlock(id) => "DOMBlock"+id
        case LEntry => "DOMEntry" + node._1._1
        case LExit => "DOMExit" + node._1._1
        case LExitExc => "DOMExitExc" + node._1._1
      }
    }
    var str = "digraph \"DirectedGraph\" {\n"
    var f_nodes:Map[FunctionId, Set[Node]] = HashMap()
    globalFG_.getNodes.foreach(node => f_nodes += (node._1 -> (getSet(f_nodes, node._1) + node)))
    var i = 0
    f_nodes.foreach(kv => {
      str += "subgraph cluster" + i + " {\n"
      str += ("label = \"fid : " + getFuncName(kv._1) + "\";\n")
      kv._2.foreach(node => str += getLabel(node) + ";\n")
      i += 1
      str += "\n}\n"
    })
    str += globalFG_.toDot_String(getLabel(_: Node))

    str += "subgraph cluster" + i + " {\n"
    str += ("label = \"Dominator tree\";\n")
    str += globalDT.toDot_String(getDomLabel(_: ENode))
    str += "\n}\n"
    def getIncrDomLabel(node: ControlPoint): String = {
      node._1._2 match {
        case LBlock(id) => "iDOMBlock"+id
        case LEntry => "iDOMEntry" + node._1._1
        case LExit => "iDOMExit" + node._1._1
        case LExitExc => "iDOMExitExc" + node._1._1
      }
    }
    i+=1
    str += "subgraph cluster" + i + " {\n"
    str += ("label = \"Dominator tree\";\n")
    str += dj_graph.toDot_String(getIncrDomLabel(_: ControlPoint))
    str += "\n}\n"
    str += "\n}"
    str
  }

  def getGDDGStr(ddg0: Boolean): String = {

    implicit def getLabel(node: Node): String = {
      node._2 match {
        case LBlock(id) => "Block"+id
        case LEntry => "Entry" + node._1
        case LExit => "Exit" + node._1
        case LExitExc => "ExitExc" + node._1
      }
    }
    var str = "digraph \"DirectedGraph\" {\n"
    if(ddg0) {
      var f_nodes:Map[FunctionId, Set[Node]] = HashMap()
      globalDDG_.getNodes.foreach(node => f_nodes += (node._1 -> (getSet(f_nodes, node._1) + node)))
      var i = 0
      f_nodes.foreach(kv => {
        str += "subgraph cluster" + i + " {\n"
        str += ("label = \"fid : " + getFuncName(kv._1) + "\";\n")
        kv._2.foreach(node => str += getLabel(node) + "\n")
        i += 1
        str += "\n}\n"
      })
      str += globalDDG_.toDot_String
    }
    else {
      str += globalDDG.toDot_String(getFuncName(_:FunctionId))
    }
    str += "\n}"
    str
  }
}

class InternalError(msg: String) extends RuntimeException(msg)
