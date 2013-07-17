/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.{CallContext, Helper, Config}
import kr.ac.kaist.jsaf.analysis.typing.models.builtin.BuiltinGlobal
import kr.ac.kaist.jsaf.{Shell, ShellParameters}

object ModelManager {
  private var model_map = Map[String, Model]()

  var fset_builtin = Map[FunctionId, String]()


  // !!!!  call once !!!!
  private def initModelMap(cfg: CFG): Unit = {

    /* builin model */
    model_map = Map[String, Model](("Builtin" -> new BuiltinModel(cfg)))

    /* dom model */
    Shell.params.command match {
      case ShellParameters.CMD_HTML | ShellParameters.CMD_HTML_SPARSE =>
        model_map = model_map + ("DOM" -> new DOMModel(cfg))
      case _ =>
        if (Config.domMode)
          model_map = model_map + ("DOM" -> new DOMModel(cfg))
        else
          ()
    }

    /* tizen model */
    if (Config.tizenMode)
      model_map = model_map + ("Tizen" -> new TizenModel(cfg))
    /* jQuery model */
    if (Config.jqMode)
      model_map = model_map + ("jQuery" -> new JQueryModel(cfg))
  }

  def initialize(cfg: CFG, heap: Heap): Heap = {
    /* init model */
    initModelMap(cfg)

    /* add async call to CFG */
    // last nodes
    val ns_last = cfg.getPred(((cfg.getGlobalFId, LExit)))
    // loop head
    val n_head = cfg.newBlock(cfg.getGlobalFId)
    // add async calls
    val ns_calls = model_map.foldLeft(List[Node]())((nodes, kv) =>
      nodes ++ kv._2.addAsyncCall(cfg, n_head)
    )
    if (!ns_calls.isEmpty) {
      // last node -> loop head
      cfg.addEdge(ns_last.toList, n_head)
      // loop head -> exit
      cfg.addEdge(n_head, ((cfg.getGlobalFId, LExit)))
      // async after call -> exit
      cfg.addEdge(ns_calls, ((cfg.getGlobalFId, LExit)))
      // async after call -> exc-exit */
      cfg.addExcEdge(ns_calls,(cfg.getGlobalFId,LExitExc))
    }
    /* init heap*/
    model_map.foldLeft(heap)((_h, kv) => kv._2.initialize(_h))
  }

  def getModel(name: String): Model = model_map(name)

  def getFIdMap(): Map[FunctionId, String] = {
    model_map.foldLeft[Map[FunctionId, String]](Map())((m, kv) => m ++ kv._2.getFIdMap())
  }
  def getFIdMap(name:String): Map[FunctionId, String] = model_map(name).getFIdMap()

  def isModelFId(fid : FunctionId) = {
    model_map.exists((kv) => kv._2.isModelFid(fid))
  }
  def isModelFId(model: String, fid : FunctionId) = {
    model_map(model).getFIdMap().contains(fid)
  }

  def getFuncName(fid: FunctionId): String = {
    for((_, model) <- model_map) {
      model.getFIdMap.get(fid) match {
        case Some(funcName) => return funcName
        case None =>
      }
    }
    null
  }

  // TODO
  def isModelLoc(loc : Loc) = {
    true
  }

}
