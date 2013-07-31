package kr.ac.kaist.jsaf.dtv

import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.lib._
import kr.ac.kaist.jsaf.analysis.lib.graph.{DataDependencyGraph => DDGraph, _}
import scala.collection.immutable.HashSet

class SceneGraphBuilder(cfg: CFG, dtv: DTV) extends SparseEnv(cfg) {
  val sceneNames = dtv.getSceneNames
  private var sceneGraph: Map[SceneId, Map[KeyId, Set[SceneId]]] = null
  private var startScenes: Set[SceneId] = null

  def getSceneGraph = sceneGraph
  def getStartScenes = startScenes

  // mapping from FunctionId to SceneId
  def f2s(fid: FunctionId): Option[SceneId] = {
    val idx:Int = cfg.getFuncInfo(fid).getSpan.getBegin.getLine
    cfg.getFuncInfo(fid).getSpan.getBegin.getFileName match{
      case "<>DTV Apps Framework" => if (idx < 0) None else Some(sceneNames(idx))
      case _ => None
    }
  }

  def drawSG(cg: Map[CFGInst, Set[FunctionId]], scene2cfgnode: Map[SceneId, Map[KeyId, List[CFGNode]]]) = {
    // compute callgraph
    val callgraph_node: Map[Node, Set[FunctionId]] =
      cg.foldLeft[Map[Node, Set[FunctionId]]](Map())((m, kv) => {
        val caller = getCFG.findEnclosingNode(kv._1)
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

    // recursively accumulate function calls
    val reccallgraph: Map[FunctionId, Set[FunctionId]] = {
      def auxrec(fid: FunctionId): Set[FunctionId] = {
        var visit:Set[FunctionId] = Set[FunctionId](fid)
        def rec(fid: FunctionId): Unit = {
          visit = visit + fid
          callgraph.get(fid) match{
            case Some(s) => s.foreach(f => visit.contains(f) match{
              case true =>
              case false => rec(f)
            })
            case None =>
          }
        }
        rec(fid)
        visit
      }
      callgraph.foldLeft[Map[FunctionId, Set[FunctionId]]](Map()){
        case (m, (caller, callees)) => {
          val tmp = callees.foldLeft[Set[FunctionId]](Set())((s, f) => s ++ auxrec(f)) + caller
          m.get(caller) match{
            case Some(exist) => m + (caller -> (tmp ++ exist))
            case None => m + (caller -> tmp)
          }
        }
      }
    }

    val rec_cfg2ftn = cg.foldLeft[Map[CFGInst, Set[FunctionId]]](Map()){
      case (m, (inst, callees)) => {
        val tmp = callees.foldLeft[Set[FunctionId]](Set())((s, f) => reccallgraph.contains(f) match {
          case true => s ++ reccallgraph(f)
          case false => s
        })
        m.get(inst) match{
          case Some(exist) => m + (inst -> (tmp ++ exist))
          case None => m + (inst -> tmp)
        }
      }
    }

    // map from CFGInst to Set[SceneId]
    val cfg2scene = rec_cfg2ftn.foldLeft[Map[CFGInst, Set[SceneId]]](Map()){
      case (m, (inst, callees)) => {
        val tmp = callees.foldLeft[Set[SceneId]](Set())((s, f) => f2s(f) match {
          case Some(sc) => if (cfg.getFuncInfo(f).getSpan.getBegin.column == 2) s + sc else s //wanna get only focus function
          case None => s
        })
        m.get(inst) match{
          case Some(exist) => m + (inst -> (tmp ++ exist))
          case None => m + (inst -> tmp)
        }
      }
    }

    sceneGraph = scene2cfgnode.foldLeft[Map[SceneId, Map[KeyId, Set[SceneId]]]](Map()){
      case (m, (scid, m2)) => {
        val tmp = m2.foldLeft[Map[KeyId, Set[SceneId]]](Map()) {
          case (mm, (key, cfgnodes)) => {
            val tmp2: Set[SceneId] = cfgnodes.foldLeft[Set[SceneId]](Set())((s, nd) => nd match {
              case inst:CFGInst => {
                if (cfg2scene.contains(inst)) s ++ cfg2scene(inst)
                else s
              }
              case _ => s
            })
            mm.get(key) match {
              case Some(exist2) => mm + (key -> (tmp2 ++ exist2))
              case None => mm + (key -> tmp2)
            }
          }
        }
        m.get(scid) match{
          case Some(exist) => m + (scid -> (tmp ++ exist))
          case None => m + (scid -> tmp)
        }
      }
    }

    for (fid <- cfg.getFunctionIds){
      if (cfg.getFuncName(fid) == "onStart"){
        if (startScenes == null)
          startScenes = reccallgraph(fid).foldLeft[Set[SceneId]](Set())((s, f) =>
            f2s(f) match {
              case Some(sc) => s + sc
              case None => s
            }
          )
      }
    }
    if (startScenes == null){
      System.out.println("No start scenes")
      startScenes = Set()
    }

    sceneGraph
  }
}