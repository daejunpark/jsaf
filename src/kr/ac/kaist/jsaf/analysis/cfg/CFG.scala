/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.cfg

import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet
import kr.ac.kaist.jsaf.analysis.lib._
import kr.ac.kaist.jsaf.analysis.typing.{Config, CallContext, DUSet, ControlPoint}
import kr.ac.kaist.jsaf.analysis.typing.domain._


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
  def getFunctionIds = funcMap.keySet


  // #PureLocal location for each function.
  // It is 1-callsite context-sensitive only for built-in calls.
  private var pureLocalMap: Map[(FunctionId, CallContext), Loc] = HashMap()
  def getPureLocal(cp: ControlPoint): Loc = {
    val key = (cp._1._1, cp._2)
    pureLocalMap.get(key) match {
      case Some(loc) => loc
      case None => {
        val loc = newProgramLoc("PureLocal#" + key._1 + "#" + key._2)
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
        val loc = newProgramLoc("PureLocal#" + fid)
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

  // successor node
  private var succMap: Map[Node, Set[Node]] = HashMap()
  def getSucc(node: Node) = succMap(node)

  // predecessor node
  private var predMap: Map[Node, Set[Node]] = HashMap()
  def getPred(node: Node) = predMap(node)

  // exception successor node
  private var excSuccMap: Map[Node, Node] = HashMap()
  def getExcSucc = excSuccMap

  // exception predecessor node
  private var excPredMap: Map[Node, Set[Node]] = HashMap()
  def getExcPred = excPredMap

  // Call <- Aftercall link
  private var callFromAftercallMap: Map[Node, Node] = HashMap()
  def getCallFromAftercallMap = callFromAftercallMap
  def getCallFromAftercall(aftercall: Node) = {
    callFromAftercallMap.get(aftercall) match {
      case Some(ac) => ac
      case None => throw new InternalError("CFGCall must have corresponding after-call")
    }
  }

  // Aftercall <- Call link
  private var aftercallFromCallMap: Map[Node, Node] = HashMap()
  def getAftercallFromCallMap = aftercallFromCallMap
  def getAftercallFromCall(call: Node) = {
    aftercallFromCallMap.get(call) match {
      case Some(c) => c
      case None => throw new InternalError("CFGAfterCall must have corresponding call")
    }
  }

  // Call <- Aftercatch link
  private var callFromAftercatchMap: Map[Node, Node] = HashMap()
  def getCallFromAftercaatchMap = callFromAftercatchMap
  def getCallFromAftercatch(aftercatch: Node) = {
    callFromAftercatchMap.get(aftercatch) match {
      case Some(ac) => ac
      case None => throw new InternalError("CFGCall must have corresponding after-catch("+aftercatch+")")
    }
  }

  // Aftercatch <- Call link
  private var aftercatchFromCallMap: Map[Node, Node] = HashMap()
  def getAftercatchFromCallMap = aftercatchFromCallMap
  def getAftercatchFromCall(call: Node) = {
    aftercatchFromCallMap.get(call) match {
      case Some(c) => c
      case None => throw new InternalError("CFGAfterCatch must have corresponding call("+call+")")
    }
  }

  // normal successor + exception successor + aftercallFromCall successor + aftercatchFromCall successor
  def getAllSucc(node: Node): Set[Node] = {
    val succs = getSet(succMap, node)
    val succs_exc = excSuccMap.get(node) match {
      case Some(n) => succs + n
      case None => succs
    }
    val succs_exc_ac = aftercallFromCallMap.get(node) match {
      case Some(n) => succs_exc + n
      case None => succs_exc
    }
    val succs_exc_ac_ac = aftercatchFromCallMap.get(node) match {
      case Some(n) => succs_exc_ac + n
      case None => succs_exc_ac
    }
    succs_exc_ac_ac
  }

  // normal predecessor + exception predecessor + aftercallFromCall predecessor + aftercatchFromCall predecessor
  def getAllPred(node: Node): Set[Node] = {
    val preds = getSet(predMap, node)
    val preds_exc = excPredMap.get(node) match {
      case Some(n) => preds ++ n
      case None => preds
    }
    val preds_exc_ac = callFromAftercallMap.get(node) match {
      case Some(n) => preds_exc + n
      case None => preds_exc
    }
    val preds_exc_ac_ac = callFromAftercatchMap.get(node) match {
      case Some(n) => preds_exc_ac + n
      case None => preds_exc_ac
    }
    preds_exc_ac_ac
  }

  private var callBlock: Set[Node] = HashSet()
  private var aftercallBlock: Set[Node] = HashSet()
  private var aftercatchBlock: Set[Node] = HashSet()
  def getCalls = callBlock
  def getAftercalls = aftercallBlock
  def getAftercatches = aftercatchBlock

  // return variable for after-call node
  private var returnVarMap: Map[Node, CFGId] = HashMap()
  def getReturnVar(aftercall: Node) = returnVarMap.get(aftercall)

  // global fid
  private var globalFId = -1
  def getGlobalFId = globalFId
  def setGlobalFId(fid: FunctionId): Unit = globalFId = fid

  def dump_du(du: DUSet): Unit = {
    System.out.println("== DU Set ==")
    nodes.foreach((n) => {
      System.out.println("* Node "+n)
      System.out.println("defset: "+du(n)._1.toString)
      System.out.println("useset: "+du(n)._2.toString)
    })
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
  def computeReachableNodes(): Unit = computeReachableNodes(quiet = false)
  def computeReachableNodes(quiet: Boolean): Unit = {
    val functions = getFunctionIds // get all function id set

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

  private var funcCount = 0
  private var userFuncCount = 0
  private var blockCount = 0
  private var instCount = 0

  /* address counter for user loation */
  private var programAddrCount = 1

  var htmlStartAddr = -1
  var htmlEndAddr = -1

  /* new address for builtin function */
  private val addrPerCallSite =  // Function.prototype.apply uses 4 addresses
    if (Config.tizenMode) 8
    else 4
  private var apiAddrMap: Map[Address, List[Address]] = HashMap()
  def getAPIAddress(addr: Address, index: Int): Address = apiAddrMap(addr)(index)
  def getAPIAddress(addr: Address): List[Address] = apiAddrMap(addr)
  def addAPIAddress(addr: Address) = {
    val new_list_1 = (0 until addrPerCallSite).foldLeft[List[Int]](List())((list, i) =>
      list :+ (programAddrCount + i)
    )
    apiAddrMap += (addr -> new_list_1)
    programAddrCount = programAddrCount + addrPerCallSite

    val new_list_2 = (0 until addrPerCallSite).foldLeft[List[Int]](List())((list, i) =>
      list :+ (programAddrCount + i)
    )
    programAddrCount = programAddrCount + addrPerCallSite
    val env_1 = new_list_1(1) // addr for @env
    val env_2 = new_list_2(1) // addr for @env

    apiAddrMap += (env_1 -> new_list_2)
    apiAddrMap += (env_2 -> new_list_2)
  }

  def getFuncCount = funcCount
  def getAddrCount = programAddrCount

  def setHtmlStartAddr= htmlStartAddr = programAddrCount
  def setHtmlEndAddr = htmlEndAddr = programAddrCount

  def isHtmlAddr(addr: Address) = (htmlStartAddr <=  addr) && (addr < htmlEndAddr)

  def setUserFuncCount(): Unit = userFuncCount = funcCount
  def getUserFuncCount = userFuncCount
  def isUserFunction(fid: FunctionId): Boolean = (fid < userFuncCount) && (fid != FIdTop)

  def newFunction(argsName: ArgumentsName, args: ArgVars, locals: LocalVars, name: String, info: Info): FunctionId = {
    // set-up function information
    val fid = funcCount
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

  def newAfterCatchBlock(fid: FunctionId): BlockNode = {
    val blockNode = newBlock(fid)

    // add Block node to the funcMap
    funcMap(fid)._4 :+ blockNode

    blockNode
  }

  private def initNode(node: Node): Unit = {
    // Extend nodes list.
    nodes ::= node

    // Extend succ/pred mappings with empty sets.
    // This will ensure that succ/pjchred mappings always exist for every node.
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
  /*
  def addCall(call: BlockNode, aftercall: BlockNode): Unit = {
    callFromAftercallMap += (aftercall -> call)
    aftercallFromCallMap += (call -> aftercall)
    callBlock += call
    aftercallBlock += aftercall
  }
  */
  def addCall(call: BlockNode, aftercall: BlockNode, aftercatch: BlockNode): Unit = {
    callFromAftercallMap += (aftercall -> call)
    aftercallFromCallMap += (call -> aftercall)
    callBlock += call
    aftercallBlock += aftercall
    
    callFromAftercatchMap += (aftercatch -> call)
    aftercatchFromCallMap += (call -> aftercatch)
    aftercatchBlock += aftercatch
  }


  def addInst(node: BlockNode, inst: CFGInst): Unit = {
    val block = cmdMap(node).asInstanceOf[Block]
    /* TODO: need optimization - inefficient quadratic list append */
    cmdMap += (node -> Block(block.insts ++ List(inst)))
    enclosingNodeMap = enclosingNodeMap + (inst.getInstId -> node)
  }

  /* newProgramAddr : Unit -> Address */
  def newProgramAddr(): Address = {
    val addr = programAddrCount
    programAddrCount += 1
    addr
  }
  /* newProgramLoc : String -> Loc */
  def newProgramLoc(name: String): Loc = {
    val addr = programAddrCount
    programAddrCount += 1
    registerPredefLoc(addr, Recent, name)
  }

  /* newInstId : Unit -> InstId */
  def newInstId(): InstId = {
    val iid = instCount
    instCount += 1
    iid
  }

  def addFileNoOp(file: String, noop: CFGInst): Unit = {
    fileToNoOpMap += (file -> noop)
  }

  def dump(): Unit = {
    for (node <- nodes) dump(node)
  }
  def dump(node: Node): Unit = {
    cmdMap(node) match {
      case Block(insts) =>
        System.out.println(node.toString())
        returnVarMap.get(node) match {
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

class InternalError(msg: String) extends RuntimeException(msg)
