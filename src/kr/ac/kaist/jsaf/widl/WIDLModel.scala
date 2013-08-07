/******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.widl

import scala.collection.mutable.{HashMap => MHashMap, ListBuffer}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.cfg.{Node => CNode}
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.models.builtin.{BuiltinArray, BuiltinDate}
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T, _}
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.scala_src.nodes._

class WIDLModel(cfg: CFG) extends Model(cfg) {
  val verbose = false

  ////////////////////////////////////////////////////////////////////////////////
  // Model Maps
  ////////////////////////////////////////////////////////////////////////////////
  private var map_fid = Map[FunctionId, String]()
  private var map_semantic = Map[String, SemanticFun]()
  private val map_presemantic = Map[String, SemanticFun]()
  private val map_def = Map[String, AccessFun]()
  private val map_use = Map[String, AccessFun]()

  ////////////////////////////////////////////////////////////////////////////////
  // Initialization List
  ////////////////////////////////////////////////////////////////////////////////
  type InitList =                               ListBuffer[LocPropMap]
  val initList =                                new InitList
  def applyInitList(heap: Heap): Heap = {
    var newHeap = heap
    for(locProps <- initList) {
      val (loc, props) = (locProps._1, locProps._2)
      /* List[(String, PropValue, Option[(Loc, Obj)], Option[FunctionId] */
      val prepareList = props.map(x => prepareForUpdate("WIDL", x._1, x._2))
      for(prepare <- prepareList) {
        val (name, propValue, obj, func) = prepare
        /* added function object to heap if any */
        obj match {
          case Some((loc, obj)) => newHeap = Heap(newHeap.map.updated(loc, obj))
          case None =>
        }
        /* update api function map */
        func match {
          case Some((fid, name)) => map_fid = map_fid + (fid -> name)
          case None => Unit
        }
      }
      /* api object */
      val obj = newHeap.map.get(loc) match {
        case Some(old) => prepareList.foldLeft(old)((o, prepare) => o.update(prepare._1, prepare._2))
        case None => prepareList.foldLeft(ObjEmpty)((o, prepare) => o.update(prepare._1, prepare._2))
      }
      /* added api object to heap */
      newHeap = Heap(newHeap.map.updated(loc, obj))
    }
    initList.clear()
    newHeap
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Semantic Function
  ////////////////////////////////////////////////////////////////////////////////
  val returnTypeMap =                           new MHashMap[String, WType]
  def semantic(sem: Semantics, heap: Heap, context: Context, heapExc: Heap, contextExc: Context, cp: ControlPoint, cfg: CFG, funcName: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    returnTypeMap.get(funcName) match {
      case Some(returnType) =>
        initList.clear()
        WIDLHelper.WType2Value(returnType, this) match {
          case Some(returnValue) =>
            return ((Helper.ReturnStore(applyInitList(heap), returnValue), context), (heapExc, contextExc))
          case None =>
        }
      case None =>
    }
    ((heap, context), (heapExc, contextExc))
  }

  ////////////////////////////////////////////////////////////////////////////////
  // New Object
  ////////////////////////////////////////////////////////////////////////////////
  type LocPropMap =                             (Loc, PropMap)
  type PropMap =                                MHashMap[String, AbsProperty]
  val globalProps =                             new PropMap
  val newLocPropsMap =                          new MHashMap[String, LocPropMap]
  def newObjectLocProps(locName: String, protoLoc: Loc = ObjProtoLoc): LocPropMap = {
    val loc = newPreDefLoc(locName, Recent)
    val props = new PropMap
    props.put("@class", AbsConstValue(PropValue(AbsString.alpha("Object"))))
    props.put("@proto", AbsConstValue(PropValue(ObjectValue(Value(protoLoc), F, F, F))))
    props.put("@extensible", AbsConstValue(PropValue(T)))

    val locProps: LocPropMap = (loc, props)
    initList.append(locProps)
    newLocPropsMap.put(locName, locProps)
    locProps
  }
  def newFunctionLocProps(locName: String, argSize: Int): (LocPropMap, LocPropMap) = {
    val protoLocProps = newObjectLocProps(locName + ".prototype")
    val loc = newPreDefLoc(locName, Recent)
    val props = new PropMap
    props.put("@class", AbsConstValue(PropValue(AbsString.alpha("Function"))))
    props.put("@proto", AbsConstValue(PropValue(ObjectValue(Value(FunctionProtoLoc), F, F, F))))
    props.put("@extensible", AbsConstValue(PropValue(T)))
    props.put("@scope", AbsConstValue(PropValue(Value(NullTop))))
    props.put("@function", AbsInternalFunc(locName))
    props.put("@construct", AbsInternalFunc(locName + ".constructor"))
    props.put("@hasinstance", AbsConstValue(PropValue(Value(NullTop))))
    props.put("prototype", AbsConstValue(PropValue(ObjectValue(Value(protoLocProps._1), F, F, F))))
    props.put("length", AbsConstValue(PropValue(ObjectValue(Value(AbsNumber.alpha(argSize)), F, F, F))))

    val locProps: LocPropMap = (loc, props)
    initList.append(locProps)
    newLocPropsMap.put(locName, locProps)
    (locProps, protoLocProps)
  }
  def newArrayLocProps(locName: String, defaultNumber: Value): LocPropMap = {
    val loc = newPreDefLoc(locName, Recent)
    val props = new PropMap
    props.put("@class", AbsConstValue(PropValue(AbsString.alpha("Array"))))
    props.put("@proto", AbsConstValue(PropValue(ObjectValue(Value(BuiltinArray.ProtoLoc), F, F, F))))
    props.put("@extensible", AbsConstValue(PropValue(T)))
    props.put("@default_number", AbsConstValue(PropValue(ObjectValue(defaultNumber, T, T, T))))
    props.put("length", AbsConstValue(PropValue(ObjectValue(Value(UInt), T, F, F))))

    val locProps: LocPropMap = (loc, props)
    initList.append(locProps)
    newLocPropsMap.put(locName, locProps)
    locProps
  }
  def newDateLocProps(locName: String): LocPropMap = {
    val loc = newPreDefLoc(locName, Recent)
    val props = new PropMap
    props.put("@class", AbsConstValue(PropValue(AbsString.alpha("Date"))))
    props.put("@proto", AbsConstValue(PropValue(ObjectValue(Value(BuiltinDate.ProtoLoc), F, F, F))))
    props.put("@extensible", AbsConstValue(PropValue(T)))
    props.put("@primitive", AbsConstValue(PropValue(Value(NumTop))))

    val locProps: LocPropMap = (loc, props)
    initList.append(locProps)
    newLocPropsMap.put(locName, locProps)
    locProps
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Create Web IDL Interface
  ////////////////////////////////////////////////////////////////////////////////
  val interfaceNameIndexMap =                   new MHashMap[String, Int]
  val createdInterfacesMap =                    new MHashMap[(Loc, String), (LocPropMap, LocPropMap)]
  def getNextInterfaceName(name: String): String = {
    val i = interfaceNameIndexMap.getOrElseUpdate(name, -1) + 1
    if(i >= 0) interfaceNameIndexMap.put(name, i)
    name + i
  }
  def createInterfaceFromName(targetLoc: Loc, interfaceName: String): (LocPropMap, LocPropMap) = {
    // Find interface node
    WIDLChecker.interfaceMap.get(interfaceName) match {
      case Some(interfaceNode) => createInterfaceFromNode(targetLoc, interfaceNode)
      case None =>
        if (verbose)
          System.out.println("* \"" + interfaceName + "\" is not an interface.")
        (null, null)
    }
  }
  def createInterfaceFromNode(targetLoc: Loc, interfaceNode: WInterface): (LocPropMap, LocPropMap) = {
    // Interface name
    val interfaceName = interfaceNode.getName
    // If this interface is already created then return it
    createdInterfacesMap.get((targetLoc, interfaceName)) match {
      case Some((locProps, protoLocProps)) => return (locProps, protoLocProps)
      case None =>
    }
    // Get the object for this interface
    val (locProps, protoLocProps) = if(targetLoc == -1) {
      // Create Object or Function Object
      if(!WIDLHelper.isCallback(interfaceNode)) newFunctionLocProps(interfaceName, /* Constructor argument size */ 0)
      else (newObjectLocProps(interfaceName), null)
    }
    else {
      // Use the provided object
      ((targetLoc, new PropMap), null)
    }
    createdInterfacesMap.put((targetLoc, interfaceName), (locProps, protoLocProps))
    initList.append(locProps)
    // Bind to global object
    if(!WIDLHelper.isNoInterfaceObject(interfaceNode)) {
      globalProps.put(interfaceName, AbsConstValue(PropValue(ObjectValue(locProps._1, T, F, T))))
    }
    // Members
    val i = interfaceNode.getMembers.iterator()
    while(i.hasNext) {
      val member = i.next().asInstanceOf[WMember]
      member match {
        // 4.4.5. Constants
        case SWConst(info, attrs, typ, name, value) =>
          val constValue = PropValue(ObjectValue(WIDLHelper.WLiteral2Value(value), F, T, F))
          locProps._2.put(name, AbsConstValue(constValue))
          if(protoLocProps != null) protoLocProps._2.put(name, AbsConstValue(constValue))
          else if (verbose)
            System.out.println("* SWConst (typ = " + typ + ", name1 = " + name + ", value = " + value + ") of \"" + interfaceName + "\" interface is not created.")
        // 4.4.6. Attributes
        case attribute@SWAttribute(info, attrs, typ1, name, exns) =>
          WIDLHelper.WType2Value(typ1, this) match {
            case Some(value) =>
              val isUnforgeable = WIDLHelper.isUnforgeable(attribute)
              val isWritable = AbsBool.alpha(!WIDLHelper.isReadOnly(member))
              val isConfigurable = AbsBool.alpha(!isUnforgeable)
              val absProp: AbsProperty = AbsConstValue(PropValue(ObjectValue(value, isWritable, T, isConfigurable)))
              // If the attribute was declared with the [Unforgeable] extended attribute,
              // then the property exists on every object that implements the interface.
              // Otherwise, it exists on the interface’s interface prototype object.
              if(isUnforgeable) {
                // Implements case
                if(targetLoc != -1) locProps._2.put(name, absProp)
              }
              else {
                // Not implements case
                if(targetLoc != -1) locProps._2.put(name, absProp)
                else protoLocProps._2.put(name, absProp)
              }
            case None =>
              if (verbose)
                System.out.println("* SWAttribute(typ1 = " + typ1 + ", name = " + name + ", exns = " + exns + ") of \"" + interfaceName + "\" interface is not created.")
          }
        // 4.4.7. Operations
        case operation@SWOperation(info, attrs, qualifiers, returnType, name, args, exns) =>
          name match {
            case Some(name) =>
              // Select target object
              val isCallback = WIDLHelper.isCallback(interfaceNode)
              val isStatic = WIDLHelper.isStatic(operation)
              val (locPropsSel, funcName) = (isCallback | isStatic) match {
                case true => (locProps, interfaceName + '.' + name)
                case false => (protoLocProps, interfaceName + ".prototype." + name)
              }
              // Argument size
              val argMinSize = args.length - WIDLHelper.getOptionalParameterCount(args)
              val argMaxSize = args.length
              WIDLChecker.argSizeMap.put(funcName, (argMinSize, argMaxSize))
              locPropsSel._2.put(name, AbsBuiltinFunc(funcName, args.length)) // T, T, T ?
              // Insert semantic function and return type
              map_semantic+= (funcName -> semantic)
              returnTypeMap.put(funcName, returnType)
            case None =>
              if (verbose)
                System.out.println("* SWOperation (qualifiers + " + qualifiers + ", returnType = " + returnType + ", name = " + name + ", args = " + args + ", exns = " + exns + ") of \"" + interfaceName + "\" interface is not created.")
          }
      }
    }
    // If this interface inherits another interface
    if(interfaceNode.getParent.isSome) {
      val parentName = interfaceNode.getParent.unwrap().getName
      if(!newLocPropsMap.contains(parentName)) createInterfaceFromName(-1, interfaceNode.getParent.unwrap().getName)
      getPreDefLoc(parentName + ".prototype") match {
        case Some(parentProtoLoc) => locProps._2.put("@proto", AbsConstValue(PropValue(ObjectValue(Value(parentProtoLoc), F, F, F))))
        case None =>
          if (verbose)
            System.out.println("* \"" + parentName + ".prototype\" does not exist.")
      }
    }
    // If this interface implements another interface
    WIDLChecker.implementsMap.get(interfaceName) match {
      case Some(implementsNode) => doImplements(protoLocProps._1, implementsNode)
      case none =>
    }
    // Return the created object(interface)
    (locProps, protoLocProps)
  }
  // 4.5. Implements statements
  def doImplements(targetLoc: Loc, implementsNode: WImplementsStatement): Unit = {
    WIDLChecker.interfaceMap.get(implementsNode.getParent) match {
      case Some(interfaceNode) => createInterfaceFromNode(targetLoc, interfaceNode)
      case None =>
        if (verbose)
          System.out.println("* \"" + implementsNode.getParent + "\" is not an interface.")
    }
  }

  /**
   * Note
   *   - "interface" and "implements" don't have cycles.
   *   - We cannot represent loc-top! ("any" type and "object" type has problem... ~_~)
   *   - If "Window" implements some interface then the interface.prototype's properties are copied
   *     to "Window" not to "Window.prototype". (Temporary wrong implementation)
   */
  def initialize(h: Heap): Heap = {
    // Top-down from "Window" object
    WIDLChecker.implementsMap.get("Window") match {
      case Some(implementsNode) => doImplements(GlobalLoc, implementsNode)
      case none =>
    }

    // Bind interfaces to "Window" object directly
    for((interfaceName, interfaceNode) <- WIDLChecker.interfaceMap) {
      if(!WIDLHelper.isNoInterfaceObject(interfaceNode)) createInterfaceFromNode(-1, interfaceNode)
    }

    if(globalProps.size > 0) initList.append((GlobalLoc, globalProps))

    ////////////////////////////////////////////////////////////////////////////////
    // Initialize Heap
    ////////////////////////////////////////////////////////////////////////////////
    applyInitList(h)
  }
  def addAsyncCall(cfg: CFG, loop_head: CNode): (List[CNode],List[CNode]) = (List(), List())
  def isModelFid(fid: FunctionId) = map_fid.contains(fid)
  def getFIdMap(): Map[FunctionId, String] = map_fid
  def getSemanticMap(): Map[String, SemanticFun] = map_semantic
  def getPreSemanticMap(): Map[String, SemanticFun] = map_presemantic
  def getDefMap(): Map[String, AccessFun] = map_def
  def getUseMap(): Map[String, AccessFun] = map_use
  def asyncSemantic(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                    name: String, list_addr: List[Address]): ((Heap, Context), (Heap, Context)) = {
    ((HeapBot, ContextBot),(HeapBot, ContextBot))
  }
  def asyncPreSemantic(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                       name: String, list_addr: List[Address]): (Heap, Context) = {
    (HeapBot, ContextBot)
  }
  def asyncDef(h: Heap, ctx: Context, cfg: CFG, name: String, list_addr: List[Address]): LPSet = LPBot
  def asyncUse(h: Heap, ctx: Context, cfg: CFG, name: String, list_addr: List[Address]): LPSet = LPBot
  def asyncCallgraph(h: Heap, inst: CFGInst, map: Map[CFGInst, Set[FunctionId]],
                     name: String, list_addr: List[Address]): Map[CFGInst, Set[FunctionId]] = Map()
}
