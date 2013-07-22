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
object WIDLChecker extends Walker {
  // api |-> definitions
  var dbs: MMap[String, List[WDefinition]] = MHashMap[String, List[WDefinition]]()
  // a pair of the name and an AST node for the current entity in an API
  var current: Option[(String, WInterfaceMember)] = None
  
  // interface |-> parent interfaces
  var impdbs: MMap[String, List[String]] = MHashMap[String, List[String]]()
  // interface |-> variables
  var vardbs: MMap[String, List[(String, WInterfaceMember)]] = MHashMap[String, List[(String, WInterfaceMember)]]()
  // (interface, variable) |-> type
  var typedbs: MMap[(String, String), String] = MHashMap[(String, String), String]()
  // interface |-> functions
  var fundbs: MMap[String, List[(String, WInterfaceMember)]] = MHashMap[String, List[(String, WInterfaceMember)]]()
  
  def getType(typ: WType): Option[String] = typ match {
    case SWNamedType(_, _, n) => Some(n)
    case _ => None // Not yet implemented : In case of other types...?
  }
  
  def addAllMembers(now: String, wdefs: List[WDefinition]): Unit = {
    wdefs.foreach(wdef => wdef match {
      case SWModule(_, _, n, list) => addAllMembers(n, list);
      case SWInterface(_, _, n, _, list) => addAllInterfaceMembers(n, list);
      case SWImplementsStatement(_, _, n, m) =>
        if(impdbs.contains(n)) impdbs.update(n, m::impdbs(n))
        else impdbs.update(n, List(m))
      case _ => None
    })
  }
  
  def addAllInterfaceMembers(now: String, wintmems: List[WInterfaceMember]): Unit = {
    wintmems.foreach(wintmem => wintmem match {
      case SWConst(_, _, t, n, _) => getType(t) match {
        case Some(typ) =>
          typedbs.update((now, n), typ)
          if (vardbs.contains(now)) vardbs.update(now, (n, wintmem)::vardbs(now))
          else vardbs.update(now, List((n, wintmem)))
        case _ =>
      }
      case SWAttribute(_, _, t, n, _) => getType(t) match {
        case Some(typ) =>
          typedbs.update((now, n), typ)
          if (vardbs.contains(now)) vardbs.update(now, (n, wintmem)::vardbs(now))
          else vardbs.update(now, List((n, wintmem)))
        case _ =>
      }
      case  SWOperation(_, _, _, _, n, _, _) => n match {
        case Some(n) =>
          if (fundbs.contains(now)) fundbs.update(now, (n, wintmem)::fundbs(now))
          else fundbs.update(now, List((n, wintmem)))
        case None =>
      }
      case _ =>
    })
  }
  
  def dotToStr(dot: LHS): Option[String] = dot match {
    case SDot(_, d:Dot, SId(_, x, _, _)) => dotToStr(d) match {
      case Some(str) => Some(str+"."+x)
      case None => None
    }
    case SDot(_, SVarRef(_, SId(_, o, _, _)), SId(_, x, _, _)) => Some(o+"."+x)
    case SVarRef(_, SId(_, x, _, _)) => Some(x)
    case _ => None
  }

  def readDB(api: String) = dbs.get(api) match {
    case None =>
      val db = WIDLToDB.readDB(api)
      dbs.update(api, db);
      addAllMembers("Window", db); db
    case Some(db) => db
  }

  private var nestedTries = 0
  private val sl = new SourceLocRats("WIDLChecker",0,0,0)
  private val span = new Span(sl,sl)
  def freshName(name: String) = "__WIDLChecker__" + name
  def mkId(name: String) = NF.makeId(span, name)
  def mkFreshId(name: String) = NF.makeId(span, freshName(name))
  
  def initAll = {
    // Initialize variables
    dbs = MHashMap[String, List[WDefinition]]()
    current = None
    impdbs = MHashMap[String, List[String]]()
    vardbs = MHashMap[String, List[(String, WInterfaceMember)]]()
    typedbs = MHashMap[(String, String), String]()
    fundbs = MHashMap[String, List[(String, WInterfaceMember)]]()
    nestedTries = 0
    warningOccurred = false
    errorOccurred = false
  }

  def doit(program:Program, libraries: JList[String]) = {
    initAll
    toList(libraries).foreach(lib => readDB(lib))
    typedbs.update(("top-level", "window"), "Window")
    walkUnit(program)
    if (!errorOccurred && !warningOccurred) System.out.println("OK")
  }

  /* error-related things */
  var warningOccurred = false
  var errorOccurred = false
  val error_CF = "Name %s is not found in the API %s."
  val warning_EH = "Function %s may raise an exception;\n              call the function inside the try statement."
  def printErrMsgLHS(obj: LHS, x: String): Unit = {
    val api = dotToStr(obj) match {
                case Some(apis) => apis
                case _ => obj.toString
              }
    printErrMsg(obj, error_CF.format(x, api))
  }

  def printErrMsg(n: Node, msg: String): Unit = {
    errorOccurred = true
    System.out.println(String.format("%s:\n    Error : %s", n.at, msg))
  }
  def printWarnMsg(n: Node, msg: String) = {
    warningOccurred = true
    System.out.println(String.format("%s:\n    Warning : %s", n.at, msg))
  }

  // find a pair of an owner type and a member
  def getMembers(typ: String): List[(String, (String, WInterfaceMember))] =
    impdbs.getOrElse(typ, Nil).foldLeft(vardbs.getOrElse(typ, Nil).map(v => (typ,v)) ++
                                        fundbs.getOrElse(typ, Nil).map(f => (typ,f)))((res,p) => res ++ getMembers(p))

  def getAPI(obj: LHS, y: String): Option[String] = obj match {
    case SDot(_, dot:LHS, SId(_, x, _, _)) => getAPI(dot, x) match {
      case Some(typ) =>
        (getMembers(typ).find(p => p._2._1.equals(x))) match {
          case Some((ty,_)) => Some(typedbs(ty, x))
          case None => None
        }
      case None => None
    }
    case SVarRef(_, SId(_, x, _, _)) if x.equals("webapis") =>
      Some(typedbs(("WindowWebAPIs", "webapis")))
    case _ => None
  }

  def isAPI(obj: LHS): Boolean = obj match {
    case SDot(_, dot, _) => isAPI(dot)
    case SVarRef(_, SId(_, x, _, _)) if x.equals("webapis") => true
    case _ => false
  }

  override def walkUnit(node: Any): Unit = node match {
    /*
     * webapis.tv.channel.tuneUp
     *
     * WindowWebAPIs defines webapis of type WebAPIs
     * WebAPIs implements { WebAPIsTVObject }
     * WebAPIsTVObject defines tv of type TV
     * TV implements { WebAPIsTVChannelManager }
     * WebAPIsTVChannelManager defines channel of type TVChannelManager
     * TVChannelManager defines { tuneUp, ... }
     */
    case fa@SDot(_, obj, SId(_, x, _, _)) if (isAPI(obj)) =>
      getAPI(obj, x) match {
        case Some(typ) =>
          (getMembers(typ).find(p => p._2._1.equals(x))) match {
            case Some(pair) => current = Some(pair._2)
            case None => printErrMsgLHS(obj, x)
            }
        case _ => printErrMsgLHS(obj, x)
      }
    
    case fa@SFunApp(_, fun@SDot(_, obj, SId(_, x, _, _)), args) if (isAPI(obj)) =>
      current = None // assertion
      walkUnit(fun)
      current match {
        case Some((_, op:WOperation)) =>
          if (!op.getExns.isEmpty && nestedTries == 0)
            printWarnMsg(fa, warning_EH.format(dotToStr(fun).getOrElse(x)))
        case _ =>
      }
      current = None
      args.foreach(walkUnit)

    case STry(_, body, catchB, _) =>
      nestedTries += 1
      walkUnit(body)
      walkUnit(catchB)
      nestedTries -= 1

    case _ => super.walkUnit(node)
  }
}