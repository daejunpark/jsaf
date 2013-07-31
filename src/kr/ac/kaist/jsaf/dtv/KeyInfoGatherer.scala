/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.dtv

import java.io._
import java.util.{List => JList}
import edu.rice.cs.plt.tuple.{Option => JOption}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes.{Node => ND}
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF}
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.useful.Useful
import kr.ac.kaist.jsaf.useful.HasAt
import scala.util.parsing.json.JSON
import kr.ac.kaist.jsaf.scala_src.useful.ErrorLog
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.nodes_util.SourceLocRats
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.nodes_util.{NodeRelation => NR}

class FindBreak extends Walker {
  var exist: Boolean = false
  override def walk(node: Any): Any = node match {
    case SBreak(_, _) => {
      exist = true
    }
    case SFor(_, _, _, _, _) =>
    case SForVar(_, _, _, _, _) =>
    case SForIn(_, _, _, _) =>
    case SForVarIn(_, _, _, _) =>
    case SDoWhile(_, _, _) =>
    case SWhile(_, _, _) =>
    case _ => super.walk(node)
  }
}

class CaseTraveler extends Walker {
  var cfgnodes: List[CFGNode] = List()
  override def walk(node: Any): Any = node match {
    case nd: ND => {
      if (NR.ast2cfgMap.contains(nd)) cfgnodes ++= NR.ast2cfgMap(nd)
      super.walk(nd)
    }
    case _ => super.walk(node)
  }
}

class SwitchTraveler(sceneName: SceneId) extends Walker {
  var key2cfg: Map[KeyId, List[CFGNode]] = Map()
  var keys: List[KeyId] = List[KeyId]()
  override def walk(node: Any): Any = node match {
    case SCase(_, cond, body) => {
      cond match {
        case SDot(_, SDot(_, SVarRef(_, SId(_, "sf", _, _)), SId(_, "key", _, _)), SId(_, keyName, _, _)) => keys :+= keyName
        case _ =>
      }
      if (body != null && body.length != 0){
        keys.foreach(nd => {
          val tmp = body.foldLeft[List[CFGNode]](List[CFGNode]())((l, bd) => {
            val ct: CaseTraveler = new CaseTraveler
            ct.walk(bd)
            l ++ ct.cfgnodes
          })
          if (key2cfg.contains(nd)){
            key2cfg += (nd -> (key2cfg(nd) ++ tmp))
          }else{
            key2cfg += (nd -> tmp)
          }
        })
        val fb: FindBreak = new FindBreak
        fb.walk(body)
        if (fb.exist) keys = List[KeyId]()
      }
    }
    case _ => super.walk(node)
  }
}

class FindSwitch(sceneName: SceneId, keyVarName: Id) extends Walker {
  var key2cfg: Map[KeyId, List[CFGNode]] = Map()
  override def walk(node: Any): Any = {
    node match {
      case SSwitch(_, SVarRef(_, id), front, defaultC, back) if (id.getUniqueName == keyVarName.getUniqueName) => {
        val fc: SwitchTraveler = new SwitchTraveler(sceneName)
        fc.walk(front)
        fc.walk(back)
        fc.walk(defaultC)
        key2cfg = fc.key2cfg
      }
      case _ => super.walk(node)
    }
  }
}

class FindKeyDownFunc extends Walker {
  var scene2cfgnode: Map[SceneId, Map[KeyId, List[CFGNode]]] = Map()
  override def walk(node: Any): Any = node match {
    case ass@SAssignOpApp(_, lhs, _, right) => lhs match {
      case SDot(_, SDot(_, SVarRef(_, SId(_, fname, _, _)), SId(_, "prototype", _, _)), SId(_, "handleKeyDown", _, _)) => {
        findFuncBody(fname.substring(5), right) //sceneName: truncate "Scene"
        ass
      }
      case _ => ass
    }
    case xs:List[_] => xs.map(x => walk(x))
    case xs:Option[_] => xs.map(x => walk(x))
    case _ => super.walk(node)
  }

  def findFuncBody(sceneName: SceneId, node: Any): Any = node match {
    case SParenthesized(_, expr) => findFuncBody(sceneName, expr)
    case SFunExpr(_, ftn) => ftn match{
      case SFunctional(_, _, body, _, params) => {
        if (params.length >= 1){
          val keyVarName = params(0)
          val fs: FindSwitch = new FindSwitch(sceneName, keyVarName)
          body.foreach(nd => fs.walk(nd))
          scene2cfgnode += (sceneName -> fs.key2cfg)
        }
      }
      case _ =>
    }
    case _ =>
  }
}

class KeyInfoGatherer(program: Program){
  val errors: ErrorLog = new ErrorLog
  val scene2cfgnode: Map[SceneId, Map[KeyId, List[CFGNode]]] = doit

  def doit = {
    val fkdf: FindKeyDownFunc = new FindKeyDownFunc()
    fkdf.walk(program).asInstanceOf[Program]
    fkdf.scene2cfgnode
  }
}
