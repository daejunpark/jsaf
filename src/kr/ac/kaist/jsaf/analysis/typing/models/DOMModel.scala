/*******************************************************************************
    Copyright (c) 2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.{AccessHelper=>AH}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse=>F, BoolTrue=>T}
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMEvent._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml5._
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU, IRFactory}


object DOMModel {
  val async_calls : List[String] = List("#LOAD", "#UNLOAD", "#KEYBOARD", "#MOUSE", "#OTHER", "#READY", "#TIME")
  val async_fun_names = List("__LOADEvent__", "__UNLOADEvent__", "__KEYBOARDEvent__", "__MOUSEEvent__", "__OTHEREvent__", "__READYEvent__")
}

class DOMModel(cfg: CFG) extends Model(cfg) {
  /* DOM list */
  val list_dom = List[ModelData](
    // DOM Core
    DOMAttr, DOMCDATASection, DOMCharacterData, DOMComment, DOMConfiguration, DOMDocument,
    DOMDocumentFragment, DOMDocumentType, DOMElement, DOMEntity, DOMEntityReference, DOMError,
    DOMException, DOMImplementation, DOMImplementationList, DOMImplementationRegistry,
    DOMImplementationSource, DOMLocator, DOMNamedNodeMap, DOMNameList, DOMNode, DOMNodeList,
    DOMNotation, DOMProcessingInstruction, DOMStringList, DOMText, DOMTypeInfo, DOMUserDataHandler,
    // DOM Event
    DocumentEvent, Event, EventException, EventListener, EventTarget, MouseEvent, MutationEvent, UIEvent,
    KeyboardEvent,
    // DOM Html
    HTMLAnchorElement, HTMLAppletElement, HTMLAreaElement, HTMLBaseElement, HTMLBaseFontElement, HTMLBodyElement,
    HTMLBRElement, HTMLButtonElement, HTMLCollection, HTMLDirectoryElement, HTMLDivElement, HTMLDListElement,
    HTMLDocument, HTMLElement, HTMLFieldSetElement, HTMLFontElement, HTMLFormElement, HTMLFrameElement,
    HTMLFrameSetElement, HTMLHeadElement, HTMLHeadingElement, HTMLHRElement, HTMLHtmlElement, HTMLIFrameElement,
    HTMLImageElement, HTMLInputElement, HTMLIsIndexElement, HTMLLabelElement, HTMLLegendElement, HTMLLIElement,
    HTMLLinkElement, HTMLMapElement, HTMLMenuElement, HTMLMetaElement, HTMLModElement, HTMLObjectElement,
    HTMLOListElement, HTMLOptGroupElement, HTMLOptionCollection, HTMLOptionElement, HTMLParagraphElement,
    HTMLParamElement, HTMLPreElement, HTMLQuoteElement, HTMLScriptElement, HTMLSelectElement, HTMLStyleElement,
    HTMLTableCaptionElement, HTMLTableCellElement, HTMLTableColElement, HTMLTableElement, HTMLTableRowElement,
    HTMLTableSectionElement, HTMLTextAreaElement, HTMLTitleElement, HTMLUListElement,
    DOMWindow,
    // HTML 5
    HTMLCanvasElement, CanvasRenderingContext2D, Navigator, CanvasGradient, DOMLocation 
  )

  private var map_fid = Map[FunctionId, String]()
  private var map_semantic = Map[String, SemanticFun]()
  private var map_presemantic =  Map[String, SemanticFun]()
  private var map_def =  Map[String, AccessFun]()
  private var map_use =  Map[String, AccessFun]()


  def initialize(h: Heap): Heap = {
    /* init function map */
    map_semantic = list_dom.foldLeft(map_semantic)((m, data) => m ++ data.getSemanticMap())
    map_presemantic = list_dom.foldLeft(map_presemantic)((m, data) => m ++ data.getPreSemanticMap())
    map_def = list_dom.foldLeft(map_def)((m, data) => m ++ data.getDefMap())
    map_use = list_dom.foldLeft(map_use)((m, data) => m ++ data.getUseMap())

    /* init api objects */
    val h_1 = list_dom.foldLeft(h)((h1, data) =>
      data.getInitList().foldLeft(h1)((h2, lp) => {
        /* List[(String, PropValue, Option[(Loc, Obj)], Option[FunctionId]
        *  property name : String
        *  property value : PropValue
        *  function loc and obj if function : Opt[(Loc, Obj)]
        *  funtion id if function : Opt[FunctionId]
        * */
        val list_props = lp._2.map((x) => prepareForUpdate("DOM", x._1, x._2))
        /* update api function map */
        list_props.foreach((v) =>
          v._4 match {
            case Some((fid, name)) => {map_fid = map_fid + (fid -> name)}
            case None => Unit
          })
        /* api object */
        val obj = h2.map.get(lp._1) match {
          case Some(o) =>
            list_props.foldLeft(o)((oo, pv) => oo.update(pv._1, pv._2))
          case None =>
            list_props.foldLeft(ObjEmpty)((o, pvo) => o.update(pvo._1, pvo._2))
        }
        /* added function object to heap if any */
        val heap = list_props.foldLeft(h2)((h3, pvo) => pvo._3 match {
          case Some((l, o)) => Heap(h3.map.updated(l, o))
          case None => h3
        })

        /* added api obejct to heap */
        Heap(heap.map.updated(lp._1, obj))
      })
    )

    // style object
    val StyleObj = Obj(ObjMapBot.
      updated("@default_number", (PropValue(StrTop), AbsentTop)).
      updated("@default_other", (PropValue(StrTop), AbsentTop))).
      update("@class", PropValue(AbsString.alpha("Object"))).
      update("@proto", PropValue(ObjectValue(Value(ObjProtoLoc), BoolFalse, BoolFalse, BoolFalse))).
      update("@extensible", PropValue(BoolTrue))

    /* initialize lookup table & event table */
    Heap(h_1.map + (IdTableLoc -> ObjEmpty) + (NameTableLoc -> ObjEmpty) + (TagTableLoc -> ObjEmpty) +
      (EventTargetTableLoc -> ObjEmpty) + (EventFunctionTableLoc -> ObjEmpty) +
      (ClassTableLoc -> ObjEmpty) + (TempStyleLoc -> StyleObj) +
      (DOMEventTimeLoc -> Helper.NewDate(Value(UInt))))
  }

  def addAsyncCall(cfg: CFG, loop_head: Node): List[Node] = {
    val fid_global = cfg.getGlobalFId
    /* dummy info for EventDispatch instruction */
    val dummy_info = IRFactory.makeInfo(IRFactory.dummySpan("DOMEvent"))
    /* dummy var for after call */
    val dummy_id = CFGTempId(NU.ignoreName+"#AsyncCall#", PureLocalVar)
    /* add async call */
    DOMModel.async_calls.foldLeft(List[Node]())((nodes, ev) => {
      /* event call */
      val event_call = cfg.newBlock(fid_global)
      cfg.addInst(event_call,
        CFGAsyncCall(cfg.newInstId, dummy_info, "DOM", ev, cfg.newProgramAddr, cfg.newProgramAddr, cfg.newProgramAddr))
      /* event after call */
      val event_after = cfg.newAfterCallBlock(fid_global, dummy_id)
      cfg.addEdge(loop_head, event_call)
      cfg.addCall(event_call, event_after)
      cfg.addEdge(event_after, loop_head)
      event_after::nodes
    })
  }

  def isModelFid(fid: FunctionId) = map_fid.contains(fid)
  def getFIdMap(): Map[FunctionId, String] = map_fid
  def getSemanticMap(): Map[String, SemanticFun] = map_semantic
  def getPreSemanticMap(): Map[String, SemanticFun] = map_presemantic
  def getDefMap(): Map[String, AccessFun] = map_def
  def getUseMap(): Map[String, AccessFun] = map_use

  def asyncSemantic(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                    name: String, list_addr: List[Address]): ((Heap, Context), (Heap, Context)) = {
    val addr1 = list_addr(0)
    val addr2 = list_addr(1)
    val addr3 = list_addr(2)
    val all_event = DOMModel.async_calls
    val all_event_name = DOMModel.async_fun_names
    val fun_table = h(EventFunctionTableLoc)
    val target_table = h(EventTargetTableLoc)

    // lset_fun: function to dispatch
    // lset_target: current target, 'this' in function body
    val (lset_fun, lset_target) = name match {

      case "#ALL" =>
        val (f, t) = all_event.foldLeft((LocSetBot, LocSetBot))((llset, e) =>
          (llset._1 ++ fun_table(e)._1._2._2, llset._2 ++ target_table(e)._1._2._2)
        )
        val lset_static = h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
          if (all_event_name.exists((e) => kv._1.startsWith(e)))
            lset ++ kv._2._1._1._1._2
          else
            lset
        )
        (f ++ lset_static, t)
      case "#NOT_LOAD_UNLOAD" =>
        val (f, t) = all_event.filterNot(_ == "#LOAD").filterNot(_ == "#UNLOAD").foldLeft((LocSetBot, LocSetBot))((llset, e) =>
          (llset._1 ++ fun_table(e)._1._2._2, llset._2 ++ target_table(e)._1._2._2)
        )
        val event_names = all_event_name.filterNot(_ == "__LOADEvent__").filterNot(_ == "__UNLOADEvent__")
        val lset_static = h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
          if (event_names.exists((e) => kv._1.startsWith(e)))
            lset ++ kv._2._1._1._1._2
          else
            lset
        )
        (f ++ lset_static, t)

      case _ =>
        val (f,t) = (fun_table(name)._1._2._2 , target_table(name)._1._2._2)
        val lset_static =
          if (name == "#TIME")
            LocSetBot
          else {
            val event_name = all_event_name(all_event.indexOf(name))
            h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
              if (kv._1.startsWith(event_name)) lset ++ kv._2._1._1._1._2
              else lset
            )
          }
        (f ++ lset_static, t)
    }
    // event call
    val l_r = addrToLoc(addr1, Recent)
    val l_arg = addrToLoc(addr2, Recent)
    val l_event = addrToLoc(addr3, Recent)
    val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
    val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, addr2)
    val (h_3, ctx_3) = Helper.Oldify(h_2, ctx_2, addr3)

    // 'this' = current target element
    val lset_this =  Helper.getThis(h_3, Value(lset_target))

    // need arguments obejct, arguments[0] = 'event object'
    val o_event = name match {
      case "#ALL" =>
        // Event object
        val proplist = MouseEvent.getInstList(lset_target) ++ KeyboardEvent.getInstList(lset_target)
        proplist.foldLeft(Helper.NewObject(ObjProtoLoc))((o, pv) =>
          o.update(pv._1, pv._2)
        )
      case "#MOUSE" =>
        // MouseEvent object
        val proplist = MouseEvent.getInstList(lset_target)
        proplist.foldLeft(Helper.NewObject(ObjProtoLoc))((o, pv) =>
          o.update(pv._1, pv._2)
        )
      case "#KEYBOARD" =>
        // KeyboardEvent object
        val proplist = KeyboardEvent.getInstList(lset_target)
        proplist.foldLeft(Helper.NewObject(ObjProtoLoc))((o, pv) =>
          o.update(pv._1, pv._2)
        )
      case _ =>
        // Event object
        Event.getInstList(lset_target).foldLeft(Helper.NewObject(ObjProtoLoc))((o, pv) =>
          o.update(pv._1, pv._2)
        )
    }


    val h_4 = h_3.update(l_event, o_event).
      update(l_arg, Helper.NewArrayObject(AbsNumber.alpha(1)))
    val h_5 = Helper.PropStore(h_4, l_arg, AbsString.alpha("0"), Value(LocSet(l_event)))
    val v_arg = Value(LocSet(l_arg))
    val o_old = h_5(SinglePureLocalLoc)
    val cc_caller = cp._2
    val n_aftercall = cfg.getAftercallFromCall(cp._1)
    val cp_aftercall = (n_aftercall, cc_caller)
    lset_fun.foreach {l_f:Loc => {
      val o_f = h_5(l_f)
      val fids = o_f("@function")._1._3
      fids.foreach {fid => {
        val ccset = cc_caller.NewCallContext(cfg, fid, l_r, lset_this)
        ccset.foreach {case (cc_new, o_new) => {
          val value = PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))
          val o_new2 =
            o_new.
              update(cfg.getArgumentsName(fid), value).
              update("@scope", o_f("@scope")._1)
          sem.addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
          sem.addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_3, o_old)
          sem.addReturnEdge(((fid, LExitExc), cc_new), cp_aftercall, ctx_3, o_old)
        }}
      }}
    }}
    val h_6 = v_arg._2.foldLeft(HeapBot)((hh, l) => {
      val pv = PropValue(ObjectValue(Value(lset_fun), BoolTrue, BoolFalse, BoolTrue))
      hh + h_5.update(l, h_5(l).update("callee", pv))
    })
    ((h_6, ctx_3), (he, ctxe))
  }
  def asyncPreSemantic(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                       name: String, list_addr: List[Address]): (Heap, Context) = {
    val addr1 = list_addr(0)
    val addr2 = list_addr(1)
    val addr3 = list_addr(2)
    val all_event = DOMModel.async_calls
    val all_event_name = DOMModel.async_fun_names
    val fun_table = h(EventFunctionTableLoc)
    val target_table = h(EventTargetTableLoc)

    // lset_fun: function to dispatch
    // lset_target: current target, 'this' in function body
    val (lset_fun, lset_target) = name match {

      case "#ALL" =>
        val (f, t) = all_event.foldLeft((LocSetBot, LocSetBot))((llset, e) =>
          (llset._1 ++ fun_table(e)._1._2._2, llset._2 ++ target_table(e)._1._2._2)
        )
        val lset_static = h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
          if (all_event_name.exists((e) => kv._1.startsWith(e)))
            lset ++ kv._2._1._1._1._2
          else
            lset
        )
        (f ++ lset_static, t)
      case "#NOT_LOAD_UNLOAD" =>
        val (f, t) = all_event.filterNot(_ == "#LOAD").filterNot(_ == "#UNLOAD").foldLeft((LocSetBot, LocSetBot))((llset, e) =>
          (llset._1 ++ fun_table(e)._1._2._2, llset._2 ++ target_table(e)._1._2._2)
        )
        val event_names = all_event_name.filterNot(_ == "__LOADEvent__").filterNot(_ == "__UNLOADEvent__")
        val lset_static = h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
          if (event_names.exists((e) => kv._1.startsWith(e)))
            lset ++ kv._2._1._1._1._2
          else
            lset
        )
        (f ++ lset_static, t)

      case _ =>
        val (f,t) = (fun_table(name)._1._2._2 , target_table(name)._1._2._2)
        val lset_static =
          if (name == "#TIME")
            LocSetBot
          else {
            val event_name = all_event_name(all_event.indexOf(name))
            h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
              if (kv._1.startsWith(event_name)) lset ++ kv._2._1._1._1._2
              else lset
            )
          }
        (f ++ lset_static, t)
    }
    // event call
    val l_r = addrToLoc(addr1, Recent)
    val l_arg = addrToLoc(addr2, Recent)
    val l_event = addrToLoc(addr3, Recent)
    val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, addr1)
    val (h_2, ctx_2) = PreHelper.Oldify(h_1, ctx_1, addr2)
    val (h_3, ctx_3) = PreHelper.Oldify(h_2, ctx_2, addr3)

    // 'this' = current target element
    val lset_this =  PreHelper.getThis(h_3, Value(lset_target))

    // need arguments obejct, arguments[0] = 'event object'
    val o_event = name match {
      case "#ALL" =>
        // Event object
        val proplist = MouseEvent.getInstList(lset_target) ++ KeyboardEvent.getInstList(lset_target)
        proplist.foldLeft(PreHelper.NewObject(ObjProtoLoc))((o, pv) =>
          o.update(pv._1, pv._2)
        )
      case "#MOUSE" =>
        // MouseEvent object
        val proplist = MouseEvent.getInstList(lset_target)
        proplist.foldLeft(PreHelper.NewObject(ObjProtoLoc))((o, pv) =>
          o.update(pv._1, pv._2)
        )
      case "#KEYBOARD" =>
        // KeyboardEvent object
        val proplist = KeyboardEvent.getInstList(lset_target)
        proplist.foldLeft(PreHelper.NewObject(ObjProtoLoc))((o, pv) =>
          o.update(pv._1, pv._2)
        )
      case _ =>
        // Event object
        Event.getInstList(lset_target).foldLeft(PreHelper.NewObject(ObjProtoLoc))((o, pv) =>
          o.update(pv._1, pv._2)
        )
    }

    val h_4 = h_3.update(l_event, o_event).
      update(l_arg, PreHelper.NewArrayObject(AbsNumber.alpha(1)))
    val h_5 = PreHelper.PropStore(h_4, l_arg, AbsString.alpha("0"), Value(LocSet(l_event)))
    val v_arg = Value(LocSet(l_arg))
    val o_old = h_5(SinglePureLocalLoc)
    val cc_caller = cp._2
    val n_aftercall = cfg.getAftercallFromCall(cp._1)
    val cp_aftercall = (n_aftercall, cc_caller)
    lset_fun.foreach {l_f:Loc => {
      val o_f = h_5(l_f)
      val fids = o_f("@function")._1._3
      fids.foreach {fid => {
        val ccset = cc_caller.NewCallContext(cfg, fid, l_r, lset_this)
        ccset.foreach {case (cc_new, o_new) => {
          val value = PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))
          val o_new2 =
            o_new.
              update(cfg.getArgumentsName(fid), value).
              update("@scope", o_f("@scope")._1)
          sem.addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
          sem.addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_3, o_old)
          sem.addReturnEdge(((fid, LExitExc), cc_new), cp_aftercall, ctx_3, o_old)
        }}
      }}
    }}
    val h_6 = v_arg._2.foldLeft(HeapBot)((hh, l) => {
      val pv = PropValue(ObjectValue(Value(lset_fun), BoolTrue, BoolFalse, BoolTrue))
      hh + h_5.update(l, h_5(l).update("callee", pv))
    })
    (h_6, ctx_3)

  }
  def asyncDef(h: Heap, ctx: Context, cfg: CFG, name: String, list_addr: List[Address]): LPSet = {
    val addr1 = list_addr(0)
    val addr2 = list_addr(1)
    val addr3 = list_addr(2)
    val l_arg = addrToLoc(addr2, Recent)
    val l_event = addrToLoc(addr3, Recent)
    val lpset_1 = AH.Oldify_def(h, ctx, addr1)
    val lpset_2 = AH.Oldify_def(h, ctx, addr2)
    val lpset_3 = AH.Oldify_def(h, ctx, addr3)
    // event object
    val lpset_4 = (AH.NewObject_def ++ MouseEvent.instProps ++ KeyboardEvent.instProps).foldLeft(LPBot)((lpset, p) => lpset + ((l_event, p)))
    // arguments object
    val lpset_5 = AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) => lpset + ((l_arg, p)))
    // arguments[0] = event
    val lpset_6 = AH.PropStore_def(h, l_arg, AbsString.alpha("0"))
    // callee
    val lpset_7 = LPSet((l_arg, "callee"))
    lpset_1 ++ lpset_2 ++ lpset_3 ++ lpset_4 ++ lpset_5 ++ lpset_6 ++ lpset_7
  }
  def asyncUse(h: Heap, ctx: Context, cfg: CFG, name: String, list_addr: List[Address]): LPSet = {
    val addr1 = list_addr(0)
    val addr2 = list_addr(1)
    val addr3 = list_addr(2)
    val all_event = DOMModel.async_calls
    val all_event_name = DOMModel.async_fun_names
    val fun_table = h(EventFunctionTableLoc)
    val target_table = h(EventTargetTableLoc)

    // lset_fun: function to dispatch
    // lset_target: current target, 'this' in function body
    val (lset_fun, lset_target, lpset_1) = name match {

      case "#ALL" =>
        val (f, t, lpset_1) = all_event.foldLeft((LocSetBot, LocSetBot, LPBot))((llpset, e) =>
          (llpset._1 ++ fun_table(e)._1._2._2, llpset._2 ++ target_table(e)._1._2._2,
            llpset._3 + (EventFunctionTableLoc, e) + (EventTargetTableLoc, e))
        )
        val (lset_static, lpset_2) = h(GlobalLoc).map.foldLeft((LocSetBot, LPBot))((set, kv) =>
          if (all_event_name.exists((e) => kv._1.startsWith(e)))
            (set._1 ++ kv._2._1._1._1._2, set._2 + (GlobalLoc, kv._1))
          else
            set
        )
        (f ++ lset_static, t, lpset_1 ++ lpset_2)
      case "#NOT_LOAD_UNLOAD" =>
        val (f, t, lpset_1) = all_event.filterNot(_ == "#LOAD").filterNot(_ == "#UNLOAD").foldLeft((LocSetBot, LocSetBot, LPBot))((llpset, e) =>
          (llpset._1 ++ fun_table(e)._1._2._2, llpset._2 ++ target_table(e)._1._2._2,
            llpset._3 + (EventFunctionTableLoc, e) + (EventTargetTableLoc, e))
        )
        val event_names = all_event_name.filterNot(_ == "__LOADEvent__").filterNot(_ == "__UNLOADEvent__")
        val (lset_static, lpset_2) = h(GlobalLoc).map.foldLeft((LocSetBot, LPBot))((set, kv) =>
          if (event_names.exists((e) => kv._1.startsWith(e)))
            (set._1 ++ kv._2._1._1._1._2, set._2 + (GlobalLoc, kv._1))
          else
            set
        )
        (f ++ lset_static, t, lpset_1 ++ lpset_2)

      case _ =>
        val (f,t) = (fun_table(name)._1._2._2 , target_table(name)._1._2._2)
        val lpset_1 =  LPBot + (EventFunctionTableLoc, name) + (EventTargetTableLoc, name)
        val (lset_static, lpset_2) =
          if (name == "#TIME")
            (LocSetBot, LPBot)
          else {
            val event_name = all_event_name(all_event.indexOf(name))
            h(GlobalLoc).map.foldLeft((LocSetBot, LPBot))((set, kv) =>
              if (kv._1.startsWith(event_name))
                (set._1 ++ kv._2._1._1._1._2, set._2 + (GlobalLoc, kv._1))
              else
                set
            )
          }
        (f ++ lset_static, t, lpset_1 ++ lpset_2)
    }

    val l_arg = addrToLoc(addr2, Recent)
    val l_event = addrToLoc(addr3, Recent)
    val LP_2 = AH.Oldify_use(h, ctx, addr1)
    val LP_3 = AH.Oldify_use(h, ctx, addr2)
    val LP_4 = AH.Oldify_use(h, ctx, addr3)

    // this
    val LP_5 = AH.getThis_use(h, Value(lset_target))

    // event object
    val LP_6 = (AH.NewObject_def ++ MouseEvent.instProps ++ KeyboardEvent.instProps).foldLeft(LPBot)((lpset, p) => lpset + ((l_event, p)))
    // arguments object
    val LP_7 = AH.NewArrayObject_def.foldLeft(LPBot)((lpset, p) => lpset + ((l_arg, p)))
    // arguments[0] = event
    val LP_8 = AH.PropStore_def(h, l_arg, AbsString.alpha("0"))

    // function
    val LP_9 = lset_fun.foldLeft(LPBot)((S, l_f) => S + ((l_f, "@function")))
    // callee
    val LP_10 = LPSet((l_arg, "callee"))
    // because of PureLocal object is weak updated in edges, all the element are needed
    val LP_11 = h(SinglePureLocalLoc).map.foldLeft(LPBot)((S, kv) => S + ((SinglePureLocalLoc, kv._1)))
    val LP_12 = LPSet(Set((ContextLoc, "3"), (ContextLoc, "4")))
    lpset_1 ++ LP_2 ++ LP_3 ++ LP_4 ++ LP_5 ++ LP_6 ++ LP_7 ++ LP_8 ++ LP_9 ++ LP_10 ++ LP_11 ++ LP_12
  }

  def asyncCallgraph(h: Heap, inst: CFGInst, map: Map[CFGInst, Set[FunctionId]],
                     name: String, list_addr: List[Address]): Map[CFGInst, Set[FunctionId]] = {
    val all_event = DOMModel.async_calls //List("#LOAD", "#UNLOAD", "#KEYBOARD", "#MOUSE", "#OTHER", "#TIME")
    val all_event_name = DOMModel.async_fun_names
    val fun_table = h(EventFunctionTableLoc)
    val target_table = h(EventTargetTableLoc)

    // lset_fun: function to dispatch
    val lset_fun = name match {
      case "#ALL" =>
        val f = all_event.foldLeft(LocSetBot)((llset, e) =>
          llset ++ fun_table(e)._1._2._2
        )
        val lset_static = h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
          if (all_event_name.exists((e) => kv._1.startsWith(e)))
            lset ++ kv._2._1._1._1._2
          else
            lset
        )
        f ++ lset_static
      case "#NOT_LOAD_UNLOAD" =>
        val f = all_event.filterNot(_ == "#LOAD").filterNot(_ == "#UNLOAD").foldLeft(LocSetBot)((llset, e) =>
          llset ++ fun_table(e)._1._2._2
        )
        val event_names = all_event_name.filterNot(_ == "__LOADEvent__").filterNot(_ == "__UNLOADEvent__")
        val lset_static = h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
          if (event_names.exists((e) => kv._1.startsWith(e)))
            lset ++ kv._2._1._1._1._2
          else
            lset
        )
        f ++ lset_static
      case _ =>
        val f = fun_table(name)._1._2._2
        val lset_static =
          if (name == "#TIME")
            LocSetBot
          else {
            val event_name = all_event_name(all_event.indexOf(name))
            h(GlobalLoc).map.foldLeft(LocSetBot)((lset, kv) =>
              if (kv._1.startsWith(event_name)) lset ++ kv._2._1._1._1._2
              else lset
            )
          }
        f ++ lset_static
    }
    lset_fun.foldLeft(map)((_m, l) => {
      if (BoolTrue <= PreHelper.IsCallable(h,l)) {
        _m.get(inst) match {
          case None => _m + (inst -> h(l)("@function")._1._3.toSet)
          case Some(set) => _m + (inst -> (set ++ h(l)("@function")._1._3.toSet))
        }
      } else {
        _m
      }
    })
  }
}
