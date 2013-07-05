/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import scala.collection.mutable.{HashMap => MHashMap}
import scala.collection.mutable.{Map=>MMap}
import java.io._
import java.util.{List => JList}
import edu.rice.cs.plt.tuple.{Option => JOption}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF}
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.{WIDLToString => WS}
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.useful.Useful
import kr.ac.kaist.jsaf.useful.HasAt
import scala.util.parsing.json.JSON
import kr.ac.kaist.jsaf.scala_src.useful.ErrorLog
import kr.ac.kaist.jsaf.exceptions.StaticError

// libraries = ["webapis.tv.channel", ...]
class WIDLChecker(program:Program, libraries: JList[String]) extends Walker {
  val libs = toList(libraries).map(s => {
               val index = s.lastIndexOf(File.separatorChar)
               val file = s.substring(index+1, s.length)
               if (index > 0) (s, file.substring(0,file.lastIndexOf('.'))) else (s, s)
             })
  var dbs: MMap[String, List[WDefinition]] = MHashMap[String, List[WDefinition]]()

  def readDB(api: String) = dbs.get(api) match {
    case None =>
      val db = WIDLToDB.readDB(api)
      dbs.update(api, db); db
    case Some(db) => db
  }

  def dotToStr(dot: LHS): Option[String] = dot match {
    case SDot(_, d:Dot, SId(_, x, _, _)) => dotToStr(d) match {
      case Some(str) => Some(str+"."+x)
      case None => None
    }
    case SDot(_, SVarRef(_, SId(_, o, _, _)), SId(_, x, _, _)) => Some(o+"."+x)
    case _ => None
  }

  def isAPI(dot: LHS) = dotToStr(dot) match {
    case Some(str) => libs.find(p => str.equals(p._2))
    case _ => None
  }

  private val sl = new SourceLocRats("WIDLChecker",0,0,0)
  private val span = new Span(sl,sl)
  def freshName(name: String) = "__WIDLChecker__" + name
  def mkId(name: String) = NF.makeId(span, name)
  def mkFreshId(name: String) = NF.makeId(span, freshName(name))
  
  def doit() = walkUnit(program)

  override def walkUnit(node: Any): Unit = node match {
    case fa@SDot(_, obj, SId(_, x, _, _)) =>
      isAPI(obj) match {
        case Some(p) =>
          System.out.println("Found: db="+p._1+" x="+x)
          val db = readDB(p._1)
        case _ => walkUnit(obj)
      }
     case fa@SFunApp(info, fun, args) =>
       args.foreach(walkUnit(_))
       fun match{ case SDot(_, obj, SId(_, x, _, _)) =>
                    isAPI(obj) match {
                      case Some(p) =>
                        System.out.println("Found: db="+p._1+" x="+x)
                        val db = readDB(p._1)
                      case _ => walkUnit(fun)
                    }
                  case _ => walkUnit(fun)
                }
     case _ => super.walkUnit(node)
   }
}
