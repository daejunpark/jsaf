package kr.ac.kaist.jsaf.dtv

import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.lib.graph.{DataDependencyGraph => DDGraph}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF}
import kr.ac.kaist.jsaf.nodes_util._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.nodes._

class SceneScenarioBuilder (sceneGraph: Map[SceneId, Map[KeyId, Set[SceneId]]], startScenes: Set[SceneId]){
  var visit: Set[SceneId] = startScenes
  var scenario: List[SourceElement] = List()
  var path: List[KeyId] = List()

  private val sl = new SourceLocRats("<>DTV Apps Framework",-1,-1,-1)
  private val span = new Span(sl,sl)
  def mkId(name: String) = NF.makeId(span, name)
  val onStart = NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeVarRef(span, mkId("onStart")), List[Expr]()))
  val onDestroy = NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeVarRef(span, mkId("onDestroy")), List[Expr]()))

  def buildScenario: Unit = {
    scenario :+= onStart
    scenario ++= path.map(k =>
      NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeBracket(span, NF.makeVarRef(span, keyDownId), NF.makeVarRef(span, focusedId)),
        List[Expr](NF.makeDot(span, NF.makeDot(span, NF.makeVarRef(span, mkId("sf")), mkId("key")), mkId(k)))))
    )
    scenario :+= NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeBracket(span, NF.makeVarRef(span, keyDownId), NF.makeVarRef(span, focusedId)),
      List[Expr](NF.makeDot(span, NF.makeDot(span, NF.makeVarRef(span, mkId("sf")), mkId("key")), mkId("ENTER"))))) //dummy key
    scenario :+= onDestroy
  }

  def step(scset:Set[SceneId], key:KeyId): Set[SceneId] = {
    var ret:Set[SceneId] = Set()
    for (sc <- scset){
      if (sceneGraph.contains(sc) && sceneGraph(sc).contains(key)) ret ++= sceneGraph(sc)(key)
    }
    ret
  }
  def newbie_huh(scset:Set[SceneId], key:KeyId): Boolean = {
    var ret: Boolean = false
    for (sc <- scset.filter(sc => sceneGraph.contains(sc))){
      for (k <- keys.filter(k => sceneGraph(sc).contains(k))){
        for (sc2 <- sceneGraph(sc)(k)){
          if (!visit.contains(sc2)) ret = true
        }
      }
    }
    ret
  }
  def traverse(scset: Set[SceneId]): Unit = {
    for (sc <- scset){
      visit += sc
    }
    var flag: Boolean = true
    for (k <- keys) {
      val next: Set[SceneId] = step(scset, k)
      if (newbie_huh(next, k)) {
        path :+= k
        traverse(next)
        path = path.init
        flag = false
      }
    }
    if (flag) {
      buildScenario
    }
  }

  def doit(program: Program): Program = {
    traverse(startScenes)
    program match {
      case SProgram(info, STopLevel(fds, vds, body), comments) =>
        SProgram(info, STopLevel(fds, vds, body++scenario), comments)
    }
  }
}