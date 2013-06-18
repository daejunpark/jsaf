/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolTrue => T, BoolFalse => F}
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.analysis.typing.domain.Context


abstract class Model(cfg: CFG) {
  def initialize(h: Heap): Heap
  def addAsyncCall(cfg: CFG, loop_head: Node): List[Node]
  def isModelFid(fid: FunctionId): Boolean
  def getSemanticMap(): Map[String, SemanticFun]
  def getPreSemanticMap(): Map[String, SemanticFun]
  def getDefMap(): Map[String, AccessFun]
  def getUseMap(): Map[String, AccessFun]
  def getFIdMap(): Map[FunctionId, String]
  def asyncSemantic(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                    name: String, list_addr: List[Address]): ((Heap, Context), (Heap, Context))
  def asyncPreSemantic(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                       name: String, list_addr: List[Address]): (Heap, Context)
  def asyncDef(h: Heap, ctx: Context, cfg: CFG, name: String, list_addr: List[Address]): LPSet
  def asyncUse(h: Heap, ctx: Context, cfg: CFG, name: String, list_addr: List[Address]): LPSet
  def asyncCallgraph(h: Heap, inst: CFGInst, map: Map[CFGInst, Set[FunctionId]],
                     name: String, list_addr: List[Address]): Map[CFGInst, Set[FunctionId]]

  /* fresh name */
  private var uniqueNameCounter = 0
  private def freshName(name: String) = {
    uniqueNameCounter += 1
    "<>API<>" + name + "<>" + uniqueNameCounter.toString
  }
  private val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("API Object"))
  /**
   * Create cfg nodes for a dom function which is composed of ENTRY, single command body, EXIT and EXIT-EXC.
   *
   * @param funName function name
   * @return created function id
   */
  def makeAPICFG(modelName: String, funName: String) : FunctionId = {
    val nameArg = freshName("arguments")
    val fid = cfg.newFunction(nameArg, List[CFGId](), List[CFGId](), funName, dummyInfo)
    val node = cfg.newBlock(fid)

    cfg.addEdge((fid, LEntry), node)
    cfg.addEdge(node, (fid,LExit))
    cfg.addExcEdge(node, (fid,LExitExc))
    cfg.addInst(node,
      CFGAPICall(cfg.newInstId,
        modelName, funName,
        CFGVarRef(dummyInfo, CFGTempId(nameArg, PureLocalVar))))

    (fid)
  }
  def makeAftercallAPICFG(modelName: String, funName: String) : FunctionId = {
    val nameArg = freshName("arguments")
    val rtn = freshName("temp")
    val fid = cfg.newFunction(nameArg, List[CFGId](), List[CFGId](), funName, dummyInfo)
    val call_node = cfg.newBlock(fid)
    val return_node = cfg.newAfterCallBlock(fid, CFGTempId(rtn, PureLocalVar))
    val return_exc_node = cfg.newAfterCatchBlock(fid)

    cfg.addEdge((fid, LEntry), call_node)
    cfg.addEdge(return_node, (fid,LExit))
    cfg.addCall(call_node, return_node, return_exc_node)
    cfg.addExcEdge(call_node, (fid,LExitExc))
    cfg.addEdge(return_exc_node, (fid,LExitExc))

    // []built-in-call
    cfg.addInst(call_node,
      CFGAPICall(cfg.newInstId,
        modelName, funName,
        CFGVarRef(dummyInfo, CFGTempId(nameArg, PureLocalVar))))

    // after-call(x)
    // return x;
    cfg.addInst(return_node,
      CFGReturn(cfg.newInstId, dummyInfo,
        Some(CFGVarRef(dummyInfo, CFGTempId(rtn, PureLocalVar)))))

    (fid)
  }

  /**
   * Preparing the given AbsProperty to be updated.
   * If a property is a built-in function, create a new function object and pass it to name, value and object pair.
   * If a property is a constant value, pass it to name, value and object pair. At this time, object is None.
   *
   * @param name the name of each property
   * @param v the value of each property.
   */
  def prepareForUpdate(model: String, name: String, v: AbsProperty): (String, PropValue, Option[(Loc, Obj)], Option[(FunctionId,String)]) = {
    v match {
      case AbsBuiltinFunc(id, length) => {
        val fid = makeAPICFG(model, id)
        val loc = newPredefLoc(id)
        val obj = Helper.NewFunctionObject(Some(fid), None, Value(NullTop), None, AbsNumber.alpha(length))
        (name, PropValue(ObjectValue(loc, T, F, T)), Some(loc, obj), Some(fid, id))
      }
      case AbsBuiltinFuncAftercall(id, length) => {
        val fid = makeAftercallAPICFG(model, id)
        val loc = newPredefLoc(id)
        val obj = Helper.NewFunctionObject(Some(fid), None, Value(NullTop), None, AbsNumber.alpha(length))
        (name, PropValue(ObjectValue(loc, T, F, T)), Some(loc, obj), Some(fid, id))
      }
      case AbsInternalFunc(id) => {
        val fid = makeAPICFG(model, id)
        (name, PropValue(ObjectValueBot, ValueBot, FunSet(fid)), None, Some(fid, id))
      }
      case AbsConstValue(value) => (name, value, None, None)
    }
  }
}
