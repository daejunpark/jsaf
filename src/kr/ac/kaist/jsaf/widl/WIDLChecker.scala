/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.widl

import scala.collection.mutable.{HashMap => MHashMap, Map => MMap}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.cfg.{Node => CNode}
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.CallContext._
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.bug_detector.StateManager
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes.{Node => ANode}
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF, NodeUtil => NU, NodeRelation, Span, SourceLocRats}
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import edu.rice.cs.plt.tuple.{Option => JOption}

// libraries = ["webapis.tv.channel", ...]
object WIDLChecker extends Walker {
  ////////////////////////////////////////////////////////////////////////////////
  // Database
  ////////////////////////////////////////////////////////////////////////////////
  // api |-> definitions
  var dbs: MMap[String, List[WDefinition]] = MHashMap[String, List[WDefinition]]()
  // a pair of the name and an AST node for the current entity in an API
  var current: Option[(String, WInterfaceMember)] = None

  ////////////////////////////////////////////////////////////////////////////////
  // WIDL Nodes
  ////////////////////////////////////////////////////////////////////////////////
  val enumMap =                                 new MHashMap[String, WEnum]
  val interfaceMap =                            new MHashMap[String, WInterface]
  val implementsMap =                           new MHashMap[String, WImplementsStatement]
  val typedefMap =                              new MHashMap[String, WTypedef]
  // interface |-> parent interfaces
  var impdbs: MMap[String, List[String]] = MHashMap[String, List[String]]()
  // interface |-> variables
  var vardbs: MMap[String, List[(String, WInterfaceMember)]] = MHashMap[String, List[(String, WInterfaceMember)]]()
  // (interface, variable) |-> type
  var typedbs: MMap[(String, String), String] = MHashMap[(String, String), String]()
  // interface |-> functions
  var fundbs: MMap[String, List[(String, WInterfaceMember)]] = MHashMap[String, List[(String, WInterfaceMember)]]()

  ////////////////////////////////////////////////////////////////////////////////
  // Analysis
  ////////////////////////////////////////////////////////////////////////////////
  var cfg: CFG =                                null
  var typing: TypingInterface =                 null
  var semantics: Semantics =                    null
  var stateManager: StateManager =              null

  ////////////////////////////////////////////////////////////////////////////////
  // Function Argument Size
  ////////////////////////////////////////////////////////////////////////////////
  val argSizeMap =                              new MHashMap[String, (Int, Int)]

  ////////////////////////////////////////////////////////////////////////////////
  // ...
  ////////////////////////////////////////////////////////////////////////////////
  def getType(typ: WType): Option[String] = typ match {
    case SWNamedType(_, _, n) => Some(n)
    case _ => None // Not yet implemented : In case of other types...?
  }
  
  def isErrorCallback(typ: WType): Boolean = getType(typ) match {
    case Some(t) => t.endsWith("ErrorCallback")
    case None => false
  }
  
  def isOptional(attr: WEAttribute): Boolean = attr match {
    case SWEAOptional(_) => true
    case _ => false
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
      dbs.update(api, db)
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

  def setLibraries(libraries: List[String]): Unit = {
    // Reset
    initAll
    // Read libraries (databases)
    for(lib <- libraries) readDB(lib)
    typedbs.update(("top-level", "window"), "Window")
    // Collect some nodes
    walkWIDL()
  }

  // add bindings for webapis Interface constructor calls
  // for each webapis Interface constructor call:
  //     new webapis.CalendarTask(...)
  // rewrite the above call to the following:
  //     new webapis_CalendarTask(...)
  // and add the following binding:
  //     webapis_CalendarTask = CalendarTask
  def rewriteWebapisConstructors(program: Program) = {
    var bindings : List[SourceElement] = List[SourceElement]()
    val equalsOp = NF.makeOp(program.getInfo.getSpan, "=")
    object astWalker extends Walker {
      override def walk(node: Any): Any = {
        node match {
          case SNew(i0, SFunApp(i, SDot(_, SVarRef(_, SId(_, name, _, _)), id), args))
               if name.equals("webapis") =>
            val lhs = SVarRef(i, SId(i, "webapis_"+id.getText, None, false))
            bindings ++= List(SExprStmt(i, SAssignOpApp(i, lhs, equalsOp,
                                                        SVarRef(i, id)), true))
            SNew(i0, SFunApp(i, lhs, super.walk(args).asInstanceOf[List[Expr]]))
          case _ => super.walk(node)
        }
      }
    }
    astWalker.walk(program) match {
      case SProgram(info, STopLevel(fds, vds, stmts)) =>
        SProgram(info, STopLevel(fds, vds, bindings++stmts))
    }
  }

  def doit(_program: Program, libraries: List[String] = null) = {
    var program = _program
    // Set libraries (database)
    if(libraries != null) setLibraries(libraries)
    // add bindings for webapis Interface constructor calls
    program = rewriteWebapisConstructors(program)
    // Check
    walkUnit(program)
    // Report
    if (!errorOccurred && !warningOccurred) System.out.println("OK")
  }

  def walkWIDL(): Unit = {
    object widlWalker extends WIDLWalker {
      override def walk(node: Any): Unit = {
        node match {
          case node@SWInterface(info, attrs, name, parent, members) => interfaceMap.put(name, node)
          case node@SWEnum(info, attrs, name, enumValueList) => enumMap.put(name, node)
          case node@SWTypedef(info, attrs, typ, name) => typedefMap.put(name, node)
          case node@SWImplementsStatement(info, attrs, name, parent) => implementsMap.put(name, node)
          case _ =>
        }
        super.walk(node)
      }
    }
    for((dbNames, widlList) <- dbs) for(widl <- widlList) widlWalker.walk(widl)

    // Debug
    /*def getId(id: JOption[WId]): String = if(id.isSome) id.unwrap().getName else ""
    println("\n* Interface Map")
    for((name, node) <- interfaceMap) {
      println("[" + WIDLHelper.getFirstAttr(node) + "], name = " + name + ", parent = " + getId(node.getParent))
      val i = node.getMembers.iterator()
      while(i.hasNext) println("    " + i.next())
    }
    println("\n* Implements Map")
    for((name, node) <- implementsMap) println("[" + WIDLHelper.getFirstAttr(node) + "], name = " + name + ", from = " + node.getParent)*/
  }

  /* error-related things */
  var warningOccurred = false
  var errorOccurred = false
  val errSpace = "\n            "
  val wnSpace = "\n              "
  val error_CF = "Name %s is not found in the API %s."
  val error_AN = "Number of the arguments to %s is %s;"+errSpace+"provide arguments of size from %s to %s."
  val error_AT = "Argument Type Error at %s"
  val warning_EC = "Call to %s is missing an error callback function;"+wnSpace+"provide an error callback function."
  val warning_EH = "Function %s may raise an exception;"+wnSpace+"call the function inside the try statement."
  def printErrMsgLHS(obj: LHS, x: String): Unit = {
    val api = dotToStr(obj) match {
                case Some(apis) => apis
                case _ => obj.toString
              }
    printErrMsg(obj, error_CF.format(x, api))
  }

  def printErrMsg(n: ANode, msg: String): Unit = {
    errorOccurred = true
    System.out.println(String.format("%s:\n    Error : %s", n.at, msg))
  }
  def printWarnMsg(n: ANode, msg: String) = {
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
          var numOfArgument = op.getArgs.size
          var numOfOptional = 0
          toList(op.getArgs).zipWithIndex.foreach(pair => pair._1 match {
            case SWArgument(_, attrs, t, _, _) =>
              /*NodeRelation.ast2cfgMap.get(fa) match {
                case Some(cfgList) =>
                  for(cfgInst <- cfgList) {
                    cfgInst match {
                      case inst@CFGCall(iid, info, fun, thisArg, arguments, addr) =>
                        println("HERE!!! => ASTFunApp[" + fa.getUID + ']' + NodeRelation.cfgToString(cfgInst))
                        val cfgNode = cfg.findEnclosingNode(inst)
                        val cstate = stateManager.getInputCState(cfgNode, inst.getInstId, _MOST_SENSITIVE)
                        for((callContext, state) <- cstate) {
                          val argLocSet = SE.V(arguments, state.heap, state.context)._1.locset
                          for(argLoc <- argLocSet) {
                            println("* For argument loc #" + argLoc)
                            for(i <- 0 until args.length) {
                              val argObj = state.heap(argLoc)
                              argObj.map.get(i.toString) match {
                                case Some((propValue, _)) =>
                                  println("  [" + i + "] = " + propValue.objval.value)
                                  if(propValue.objval.value.pvalue.numval != NumBot) {
                                  }
                                case None => println("  [" + i + "] =")
                              }
                            }
                          }
                        }
                        //stateManager.dump(cfgNode, inst, cstate)
                      case _ =>
                    }
                  }
                case None =>
              }*/
              if(!attrs.filter(attr => isOptional(attr)).isEmpty) {
                numOfOptional = numOfOptional + 1
                if(pair._2 >= args.length && isErrorCallback(t))
                  printWarnMsg(fa, warning_EC.format(dotToStr(fun).getOrElse(x)))
              }
            case _ =>
          })
          if(numOfArgument-numOfOptional > args.length || args.length > numOfArgument)
            printErrMsg(fa, error_AN.format(dotToStr(fun).getOrElse(x), args.length,
                                            numOfArgument-numOfOptional, numOfArgument))
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
