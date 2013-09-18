/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.widl

import scala.collection.mutable.{ HashMap => MHashMap, Map => MMap, ListBuffer => MListBuffer }
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.cfg.{ Node => CNode }
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.CallContext._
import kr.ac.kaist.jsaf.analysis.typing.{ SemanticsExpr => SE }
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.bug_detector.StateManager
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes.{ Node => ANode }
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF, NodeUtil => NU, Walkers, NodeRelation, Span, SourceLocRats}
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import edu.rice.cs.plt.tuple.{ Option => JOption }
import java.util.{List => JList}

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
  var constructorMap = new MHashMap[String, MListBuffer[WEAConstructor]]
  var enumMap = new MHashMap[String, WEnum]
  var interfaceMap = new MHashMap[String, WInterface]
  var implementsMap = new MHashMap[String, MListBuffer[WImplementsStatement]]
  var typedefMap = new MHashMap[String, WTypedef]
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
  var cfg: CFG = null
  var argObj: Obj = null
  var argState: State = null
  var typing: TypingInterface = null
  var semantics: Semantics = null
  var stateManager: StateManager = null

  ////////////////////////////////////////////////////////////////////////////////
  // Function Argument Size
  ////////////////////////////////////////////////////////////////////////////////
  var argSizeMap = new MHashMap[String, (Int, Int)]

  ////////////////////////////////////////////////////////////////////////////////
  // Soundness for argument type cheching
  ////////////////////////////////////////////////////////////////////////////////
  val soundness = true

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

  def checkArgumentType(propValue: PropValue, apitype: String): (Boolean, String) = {
    val undefval = propValue.objval.value.pvalue.undefval
    val nullval = propValue.objval.value.pvalue.nullval
    val boolval = propValue.objval.value.pvalue.boolval
    val strval = propValue.objval.value.pvalue.strval
    val numval = propValue.objval.value.pvalue.numval
    val locset = propValue.objval.value.locset
    val notAllBottom = !undefval.isBottom || !nullval.isBottom || !boolval.isBottom || !strval.isBottom || !numval.isBottom
    val notInf = !(numval == PosInf || numval == NegInf || numval == NaN || numval == Infinity)
    val notOtherStr = !(strval == OtherStr || strval.isInstanceOf[OtherStrSingle])
    if (soundness) {
      apitype match {
        case "any" => (true, "any")
        case "boolean" => (true, "boolean")
        case "byte" => (locset.isEmpty, "byte")
        case "octet" => (locset.isEmpty, "octet")
        case "short" => (locset.isEmpty, "short")
        case "unsigned short" => (locset.isEmpty, "unsigned short")
        case "long" => (locset.isEmpty, "long")
        case "unsigned long" => (locset.isEmpty, "unsigned long")
        case "long long" => (locset.isEmpty, "long long")
        case "unsigned long long" => (locset.isEmpty, "unsigned long long")
        case "float" => (undefval.isBottom && notInf && !numval.isTop && notOtherStr && !strval.isTop, "float")
        case "unrestricted float" => (locset.isEmpty, "unrestricted float")
        case "double" => (undefval.isBottom && notInf && !numval.isTop && notOtherStr && !strval.isTop, "double")
        case "unrestricted double" => (locset.isEmpty, "unrestricted double")
        case "DOMString" => (true, "DOMString")
        case typ =>
          var result = false;
          var allLocResult = true;
          enumMap.get(typ) match {
            case Some(enum) => result = true;
            case _ =>
          }
          interfaceMap.get(typ) match {
            case Some(interface) =>
              val interfaceMemberTypeMap = new MHashMap[String, String]
              for (mem <- toList(interface.getMembers)) {
                mem match {
                  case SWConst(_, _, t, n, _) => getType(t) match {
                    case Some(t) => interfaceMemberTypeMap.update(n, t)
                    case _ =>
                  }
                  case SWAttribute(_, _, t, n, _) => getType(t) match {
                    case Some(t) => interfaceMemberTypeMap.update(n, t)
                    case _ =>
                  }
                  case SWOperation(_, _, _, t, n, args, _) => (n, getType(t)) match {
                    case (Some(n), Some(t)) => interfaceMemberTypeMap.update(n, t)
                    case _ =>
                  }
                  case _ =>
                }
              }
              for (loc <- locset) {
                var num = 0
                for (prop <- argState.heap(loc).getProps) {
                  if (interfaceMemberTypeMap.contains(prop) &&
                      checkArgumentType(argState.heap(loc).map(prop)._1, interfaceMemberTypeMap(prop))._1) {
                      num = num + 1
                      if (num == interfaceMemberTypeMap.size && num == argState.heap(loc).getProps.size) result = true
                  } else {
                    allLocResult = false
                  }
                }
              }
            case _ =>
          }
          typedefMap.get(typ) match {
            case Some(typedef) =>
              getType(typedef.getTyp) match {
                case Some(t) => result = checkArgumentType(propValue, t)._1
                case _ => 
              }
            case _ =>
          }
          (allLocResult && result, typ)
      }
    } else {
      apitype match {
        case "any" => (true, "any")
        case "boolean" => (true, "boolean")
        case "byte" => (notAllBottom, "byte")
        case "octet" => (notAllBottom, "octet")
        case "short" => (notAllBottom, "short")
        case "unsigned short" => (notAllBottom, "unsigned short")
        case "long" => (notAllBottom, "long")
        case "unsigned long" => (notAllBottom, "unsigned long")
        case "long long" => (notAllBottom, "long long")
        case "unsigned long long" => (notAllBottom, "unsigned long long")
        case "float" => (!nullval.isBottom || !boolval.isBottom || (!strval.isBottom && notOtherStr) ||
          (!numval.isBottom && notInf), "float")
        case "unrestricted float" => (notAllBottom, "unrestricted float")
        case "double" => (!nullval.isBottom || !boolval.isBottom || (!strval.isBottom && notOtherStr) ||
          (!numval.isBottom && notInf), "double")
        case "unrestricted double" => (notAllBottom, "sunrestricted double")
        case "DOMString" => (true, "DOMString")
        case typ =>
          var result = false;
          enumMap.get(typ) match {
            case Some(enum) => result = true; // how can we know if typ is an element of enum?
            case _ =>
          }
          interfaceMap.get(typ) match {
            case Some(interface) =>
              val interfaceMemberTypeMap = new MHashMap[String, String]
              for (mem <- toList(interface.getMembers)) {
                mem match {
                  case SWConst(_, _, t, n, _) => getType(t) match {
                    case Some(t) => interfaceMemberTypeMap.update(n, t)
                    case _ =>
                  }
                  case SWAttribute(_, _, t, n, _) => getType(t) match {
                    case Some(t) => interfaceMemberTypeMap.update(n, t)
                    case _ =>
                  }
                  case SWOperation(_, _, _, t, n, _, _) => (n, getType(t)) match {
                    case (Some(n), Some(t)) => interfaceMemberTypeMap.update(n, t)
                    case _ =>
                  }
                  case _ =>
                }
              }
              for (loc <- locset) {
                var num = 0
                for (prop <- argState.heap(loc).getProps) {
                  if (interfaceMemberTypeMap.contains(prop) &&
                      checkArgumentType(argState.heap(loc).map(prop)._1, interfaceMemberTypeMap(prop))._1) {
                      num = num + 1
                      if (num == interfaceMemberTypeMap.size && num == argState.heap(loc).getProps.size) result = true
                  }
                }
              }
            case _ =>
          }
          typedefMap.get(typ) match {
            case Some(typedef) =>
              getType(typedef.getTyp) match {
                case Some(t) => result = checkArgumentType(propValue, t)._1
                case _ => 
              }
            case _ =>
          }
          (result, typ)
      }
    }
  }

  def addAllMembers(now: String, wdefs: List[WDefinition]): Unit = {
    wdefs.foreach(wdef => wdef match {
      case SWModule(_, _, n, list) => addAllMembers(n, list);
      case SWInterface(_, _, n, _, list) => addAllInterfaceMembers(n, list);
      case SWImplementsStatement(_, _, n, m) =>
        if (impdbs.contains(n)) impdbs.update(n, m :: impdbs(n))
        else impdbs.update(n, List(m))
      case _ => None
    })
  }

  def addAllInterfaceMembers(now: String, wintmems: List[WInterfaceMember]): Unit = {
    wintmems.foreach(wintmem => wintmem match {
      case SWConst(_, _, t, n, _) => getType(t) match {
        case Some(typ) =>
          typedbs.update((now, n), typ)
          if (vardbs.contains(now)) vardbs.update(now, (n, wintmem) :: vardbs(now))
          else vardbs.update(now, List((n, wintmem)))
        case _ =>
      }
      case SWAttribute(_, _, t, n, _) => getType(t) match {
        case Some(typ) =>
          typedbs.update((now, n), typ)
          if (vardbs.contains(now)) vardbs.update(now, (n, wintmem) :: vardbs(now))
          else vardbs.update(now, List((n, wintmem)))
        case _ =>
      }
      case SWOperation(_, _, _, _, n, _, _) => n match {
        case Some(n) =>
          if (fundbs.contains(now)) fundbs.update(now, (n, wintmem) :: fundbs(now))
          else fundbs.update(now, List((n, wintmem)))
        case None =>
      }
      case _ =>
    })
  }

  def dotToStr(dot: LHS): Option[String] = dot match {
    case SDot(_, d: Dot, SId(_, x, _, _)) => dotToStr(d) match {
      case Some(str) => Some(str + "." + x)
      case None => None
    }
    case SDot(_, SVarRef(_, SId(_, o, _, _)), SId(_, x, _, _)) => Some(o + "." + x)
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
  private val sl = new SourceLocRats("WIDLChecker", 0, 0, 0)
  private val span = new Span(sl, sl)
  def freshName(name: String) = "__WIDLChecker__" + name
  def mkId(name: String) = NF.makeId(span, name)
  def mkFreshId(name: String) = NF.makeId(span, freshName(name))

  def initAll = {
    // Initialize variables
    dbs = MHashMap[String, List[WDefinition]]()
    current = None
    constructorMap = new MHashMap[String, MListBuffer[WEAConstructor]]
    enumMap = new MHashMap[String, WEnum]
    interfaceMap = new MHashMap[String, WInterface]
    implementsMap = new MHashMap[String, MListBuffer[WImplementsStatement]]
    typedefMap = new MHashMap[String, WTypedef]
    argSizeMap = new MHashMap[String, (Int, Int)]
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
    for (lib <- libraries) readDB(lib)
    typedbs.update(("top-level", "window"), "Window")
    // Collect some nodes
    walkWIDL()
  }

  def doit(_program: Program, libraries: List[String] = null) = {
    var program = _program
    // Set libraries (database)
    if(libraries != null) setLibraries(libraries)
    // Check
    walkUnit(program)
    // Report
    if (!errorOccurred && !warningOccurred) System.out.println("OK")
  }

  def walkWIDL(): Unit = {
    object WIDLWalker extends Walkers {
      override def walkWIDL(parent: Any, node: Any): Unit = {
        node match {
          case node@SWInterface(info, attrs, name, parent, members) =>
            for(attr <- attrs) {
              attr match {
                case const: WEAConstructor => constructorMap.getOrElseUpdate(name, new MListBuffer).append(const)
                case _ =>
              }
            }
            if(interfaceMap.get(name).isDefined) System.out.println("* \"" + name + "\" has multiple definitions.")
            interfaceMap.put(name, node)
          case node@SWEnum(info, attrs, name, enumValueList) => enumMap.put(name, node)
          case node@SWTypedef(info, attrs, typ, name) => typedefMap.put(name, node)
          case node@SWImplementsStatement(info, attrs, name, parent) => implementsMap.getOrElseUpdate(name, new MListBuffer).append(node)
          case _ =>
        }
        super.walkWIDL(parent, node)
      }
    }
    for((dbNames, widlList) <- dbs) WIDLWalker.walkWIDL(null, widlList)

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
  val error_AN = "Number of the arguments to %s is %s;" + errSpace + "provide arguments of size from %s to %s."
  val error_AN2 = "Number of the arguments to %s is %s;" + errSpace + "provide arguments of size of %s."
  val error_AT = "Argument #%s of the function %s is wrong;" + errSpace + "provide %s type for argument #%s."
  val warning_AT = "Argument #%s of the function %s may be wrong;" + wnSpace + "provide %s type for argument #%s."
  val warning_EC = "Call to %s is missing an error callback function;" + wnSpace + "provide an error callback function."
  val warning_EH = "Function %s may raise an exception;" + wnSpace + "call the function inside the try statement."
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
    impdbs.getOrElse(typ, Nil).foldLeft(vardbs.getOrElse(typ, Nil).map(v => (typ, v)) ++
      fundbs.getOrElse(typ, Nil).map(f => (typ, f)))((res, p) => res ++ getMembers(p))

  def getAPI(obj: LHS, y: String): Option[String] = obj match {
    case SDot(_, dot: LHS, SId(_, x, _, _)) => getAPI(dot, x) match {
      case Some(typ) =>
        (getMembers(typ).find(p => p._2._1.equals(x))) match {
          case Some((ty, _)) => Some(typedbs(ty, x))
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

  def checkArgs(fa: LHS, name: String, params: JList[WArgument], args: List[Expr]) = {
    NodeRelation.ast2cfgMap.get(fa) match {
      case Some(cfgList) =>
        for (cfgInst <- cfgList) {
          cfgInst match {
            case inst@CFGCall(_, _, _, _, arguments, _) =>
              val cfgNode = cfg.findEnclosingNode(inst)
              val cstate = stateManager.getInputCState(cfgNode, inst.getInstId, _MOST_SENSITIVE)
              for ((callContext, state) <- cstate) {
                val argLocSet = SE.V(arguments, state.heap, state.context)._1.locset
                for (argLoc <- argLocSet) {
                  argState = state
                  argObj = state.heap(argLoc)
                }
              }
            case _ =>
          }
        }
      case None =>
    }
    var numOfOptional = 0
    var numOfArgument = params.size
    toList(params).zipWithIndex.foreach(pair => pair._1 match {
      case SWArgument(_, attrs, t, _, _) =>
        if (pair._2 < args.length) {
          var result = (false, "?")
          if (argObj != null && argObj.map != null) {
            (argObj.map.get(pair._2.toString), getType(t)) match {
              case (Some(objTuple), Some(typ)) => result = checkArgumentType(objTuple._1, typ)
              case _ =>
            }
          }
          if (!result._1) {
            if (soundness)
              printWarnMsg(fa, warning_AT.format(pair._2 + 1, name, result._2, pair._2 + 1))
            else
              printErrMsg(fa, error_AT.format(pair._2 + 1, name, result._2, pair._2 + 1))
          }
        }
        if (!attrs.filter(attr => isOptional(attr)).isEmpty) {
          numOfOptional = numOfOptional + 1
          if (pair._2 >= args.length && isErrorCallback(t))
            printWarnMsg(fa, warning_EC.format(name))
        }
      case _ =>
    })
    if (numOfArgument - numOfOptional > args.length || args.length > numOfArgument)
      if (numOfOptional != 0)
        printErrMsg(fa, error_AN.format(name, args.length,
                                        numOfArgument - numOfOptional, numOfArgument))
      else
        printErrMsg(fa, error_AN.format(name, args.length, numOfArgument))
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
          getMembers(typ).find(p => p._2._1.equals(x)) match {
            case Some(pair) => current = Some(pair._2)
            case None => printErrMsgLHS(obj, x)
          }
        case _ => printErrMsgLHS(obj, x)
      }

    case fa@SFunApp(_, fun@SDot(_, obj, SId(_, x, _, _)), args) if (isAPI(obj)) =>
      current = None // assertion
      walkUnit(fun)
      current match {
        case Some((_, op: WOperation)) =>
          if (!op.getExns.isEmpty && nestedTries == 0)
            printWarnMsg(fa, warning_EH.format(dotToStr(fun).getOrElse(x)))
          checkArgs(fa, dotToStr(fun).getOrElse(x), op.getArgs, args)
        case _ =>
      }
      current = None
      args.foreach(walkUnit)

    case SNew(_, fa@SFunApp(_, SVarRef(_, SId(_, f, _, _)), args)) if f.startsWith("<>webapis_") =>
      constructorMap.get(f.drop(10)) match {
        case Some(constructorList) =>
          for (constructor <- constructorList)
            checkArgs(fa, f.drop(10), constructor.getArgs, args)
        case None =>
      }
      args.foreach(walkUnit)

    case STry(_, body, catchB, _) =>
      nestedTries += 1
      walkUnit(body)
      walkUnit(catchB)
      nestedTries -= 1

    case _ => super.walkUnit(node)
  }
}
