/*******************************************************************************
    Copyright (c) 2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMCore

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import org.w3c.dom.Document
import org.w3c.dom.Node
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml5.DOMLocation
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml.HTMLTopElement
import scala.Some
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.OtherStrSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.NumStrSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.domain.OtherStrSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Obj
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.NumStrSingle

object DOMDocument extends DOM {
  private val name = "Document"

  /* predefined locatoins */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("length", AbsConstValue(PropValue(ObjectValue(Value(AbsNumber.alpha(0)), F, F, F)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F))))
  )

  /* prorotype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(DOMNode.loc_proto), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("createElement",               AbsBuiltinFunc("DOMDocument.createElement", 1)),
    ("createDocumentFragment",      AbsBuiltinFunc("DOMDocument.createDocumentFragment", 0)),
    ("createTextNode",              AbsBuiltinFunc("DOMDocument.createTextNode", 1)),
    ("createComment",               AbsBuiltinFunc("DOMDocument.createComment", 1)),
    ("createCDATASection",          AbsBuiltinFunc("DOMDocument.createCDATASection", 1)),
    ("createProcessingInstruction", AbsBuiltinFunc("DOMDocument.createProcessingInstruction", 2)),
    ("createAttribute",             AbsBuiltinFunc("DOMDocument.createAttribute", 1)),
    ("createEntityReference",       AbsBuiltinFunc("DOMDocument.createEntityReference", 1)),
    ("getElementsByTagName",        AbsBuiltinFunc("DOMDocument.getElementsByTagName", 1)),
    ("importNode",                  AbsBuiltinFunc("DOMDocument.importNode", 2)),
    ("createElementNS",             AbsBuiltinFunc("DOMDocument.createElementNS", 2)),
    ("createAttributeNS",           AbsBuiltinFunc("DOMDocument.createAttributeNS", 2)),
    ("getElementsByTagNameNS",      AbsBuiltinFunc("DOMDocument.getElementsByTagNameNS", 1)),
    ("getElementById",              AbsBuiltinFunc("DOMDocument.getElementById", 2)),
    ("getElementsByClassName",      AbsBuiltinFunc("DOMDocument.getElementsByClassName", 2)),
    ("adoptNode",                   AbsBuiltinFunc("DOMDocument.adoptNode", 1)),
    ("normalizeDocument",           AbsBuiltinFunc("DOMDocument.normalizeDocument", 0)),
    ("renameNode",                  AbsBuiltinFunc("DOMDocument.renameNode", 3)),
    ("querSelector",                AbsBuiltinFunc("DOMDocument.querSelector", 0)),
    ("querSelectorAll",             AbsBuiltinFunc("DOMDocument.querSelectorAll", 0))
  )

  /* global */
  private val prop_global: List[(String, AbsProperty)] = List(
    (name, AbsConstValue(PropValue(ObjectValue(loc_cons, T, F, T))))
  )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto), (GlobalLoc, prop_global)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //TODO: not yet implemented
      ("DOMDocument.createElement" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r = addrToLoc(addr1, Recent)
          val l_nodes = addrToLoc(addr2, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = Helper.Oldify(h_1, ctx_1, addr2)

          val s_tag = Helper.toString(Helper.toPrimitive(getArgValue(h_2, ctx_2, args, "0")))

          // DOMException object with the INVALID_CHARACTER_ERR exception code
          val es = Set(DOMException.INVALID_CHARACTER_ERR)
          val (he_1, ctxe_1) = DOMHelper.RaiseDOMException(h_2, ctx_2, es)

          /* imprecise semantics */
          s_tag match {
            // may cause the INVALID_CHARACTER_ERR exception
            case StrTop | OtherStr =>
              val element_obj_proplist = HTMLTopElement.default_getInsList()
              val element_obj = element_obj_proplist.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2, AbsentTop))
              // 'childNodes' update
              val childNodes_list = DOMNodeList.getInsList(0)
              val childNodes = childNodes_list.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
              val element_obj_up = element_obj.update("childNodes", PropValue(ObjectValue(l_nodes, F, T, T)))
              val h_3= h_2.update(l_nodes, childNodes).update(l_r, element_obj_up)
              ((Helper.ReturnStore(h_3, Value(l_r)), ctx_2), (he + he_1, ctxe + ctxe_1))
            // cause the INVALID_CHARACTER_ERR exception
            case NumStr | NumStrSingle(_) =>
              ((HeapBot, ContextBot), (he + he_1, ctxe + ctxe_1))
            case OtherStrSingle(s) =>
              val tag_name = s.toUpperCase
              if(DOMHelper.isValidHtmlTag(tag_name)) {
                val element_obj_proplist = DOMElement.getInsList(PropValue(ObjectValue(AbsString.alpha(tag_name), F, T, T))):::DOMHelper.getInsList(tag_name)
                val element_obj = element_obj_proplist.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
                // 'childNodes' update
                val childNodes_list = DOMNodeList.getInsList(0)
                val childNodes = childNodes_list.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
                val element_obj_up = element_obj.update("childNodes", PropValue(ObjectValue(l_nodes, F, T, T)))
                val h_3= h_2.update(l_nodes, childNodes).update(l_r, element_obj_up)
                ((Helper.ReturnStore(h_3, Value(l_r)), ctx_2), (he, ctxe))
              }
              // An invalid tag name causes the INVALID_CHARACTER_ERR exception
              else
                ((HeapBot, ContextBot), (he + he_1, ctxe + ctxe_1))
            case StrBot =>
              ((HeapBot, ContextBot), (he, ctxe))
          }
        })),
      //case "DOMDocument.createDocumentFragment" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createTextNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createComment" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createCDATASection" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createProcessingInstruction" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createAttribute" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createEntityReference" => ((h, ctx), (he, ctxe))
      ("DOMDocument.getElementsByTagName" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          /* arguments */
          val s_name = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          if (s_name </ StrBot) {
            val obj_table = h(TagTableLoc)
            val propv_element = obj_table(s_name.toUpperCase)
            val (h_1, ctx_1, v_empty) =
              if (propv_element._2 </ AbsentBot) {
                val l_r = addrToLoc(addr1, Recent)
                val (_h, _ctx)  = Helper.Oldify(h, ctx, addr1)
                /* empty NodeList */
                val o_empty = Obj(DOMNodeList.getInsList(0).foldLeft(ObjEmpty.map)((o,pv) =>
                  o.updated(pv._1, (pv._2, AbsentBot))))
                val _h1 = _h.update(l_r, o_empty)
                (_h1, _ctx, Value(l_r))
              } else (h, ctx, ValueBot)
            /* imprecise semantic */
            ((Helper.ReturnStore(h_1, propv_element._1._1._1 + v_empty), ctx_1), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      //case "DOMDocument.importNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createElementNS" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createAttributeNS" => ((h, ctx), (he, ctxe))
      ("DOMDocument.getElementsByTagNameNS" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          /* arguments */
          val s_ns = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          val s_name = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "1")))
          if (s_ns </StrBot && s_name </ StrBot) {
            val obj_table = h(TagTableLoc)
            val propv_element = obj_table(s_name.toUpperCase)
            val (h_1, ctx_1, v_empty) =
              if (propv_element._2 </ AbsentBot) {
                val l_r = addrToLoc(addr1, Recent)
                val (_h, _ctx)  = Helper.Oldify(h, ctx, addr1)
                /* empty NodeList */
                val o_empty = Obj(DOMNodeList.getInsList(0).foldLeft(ObjEmpty.map)((o,pv) =>
                  o.updated(pv._1, (pv._2, AbsentBot))))
                val _h1 = _h.update(l_r, o_empty)
                (_h1, _ctx, Value(l_r))
              } else (h, ctx, ValueBot)
            /* imprecise semantic */
            ((Helper.ReturnStore(h_1, propv_element._1._1._1 + v_empty), ctx_1), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("DOMDocument.getElementById" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          /* arguments */
          val s_id = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          if (s_id </ StrBot) {
            val lset_find = DOMHelper.findById(h, s_id)
            val v_null = if (lset_find.isEmpty) Value(NullTop) else ValueBot
            /* imprecise semantic */
            ((Helper.ReturnStore(h, Value(lset_find) + v_null), ctx), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("DOMDocument.getElementsByClassName" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          /* arguments */
          val s_class = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          if (s_class </ StrBot) {
            val propv_element = h(ClassTableLoc)(s_class)
            val (h_1, ctx_1, v_empty) =
              if (propv_element._2 </ AbsentBot) {
                val l_r = addrToLoc(addr1, Recent)
                val (_h, _ctx)  = Helper.Oldify(h, ctx, addr1)
                /* empty NodeList */
                val o_empty = Obj(DOMNodeList.getInsList(0).foldLeft(ObjEmpty.map)((o,pv) =>
                  o.updated(pv._1, (pv._2, AbsentBot))))
                val _h1 = _h.update(l_r, o_empty)
                (_h1, _ctx, Value(l_r))
              } else (h, ctx, ValueBot)
            /* imprecise semantic */
            ((Helper.ReturnStore(h_1, propv_element._1._1._1 + v_empty), ctx_1), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      //case "DOMDocument.adoptNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.normalizeDocument" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.renameNode" => ((h, ctx), (he, ctxe))
      ("DOMDocument.querySelector" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val list_addr = getAddrList(h, cfg)
          val addr1 = list_addr(0)

          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          /* arguments */
          val s_selector = getArgValue(h, ctx, args, "0")._1._5
          if (s_selector </ StrBot) {
            val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
            // start here
            val l_result = addrToLoc(addr1, Recent)
            val lset_find = lset_this.foldLeft(LocSetBot)((ls, l) => ls ++ DOMHelper.querySelectorAll(h_1, l, s_selector))
            val (h_ret, v_ret) =
              if(lset_find.isEmpty)
                (h_1, Value(NullTop))
              else {
                val o_result = Helper.NewObject(ObjProtoLoc)
                  .update("0", PropValue(ObjectValue(Value(lset_find), T,T,T)))
                  .update("length", PropValue(ObjectValue(AbsNumber.alpha(0), T,T,T)))
                val h_2 = h_1.update(l_result, o_result)
                (h_2, Value(lset_find))
              }
            ((Helper.ReturnStore(h_ret, v_ret), ctx_1), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        })),
      ("DOMDocument.querySelectorAll" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val list_addr = getAddrList(h, cfg)
          val addr1 = list_addr(0)

          val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
          /* arguments */
          val s_selector = getArgValue(h, ctx, args, "0")._1._5
          if (s_selector </ StrBot) {
            val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
            // start here
            val l_result = addrToLoc(addr1, Recent)
            val lset_find = lset_this.foldLeft(LocSetBot)((ls, l) => ls ++ DOMHelper.querySelectorAll(h_1, l, s_selector))
            val (h_ret, v_ret) =
              if(lset_find.isEmpty)
                (h_1, Value(NullTop))
              else {
                val o_result = Helper.NewObject(ObjProtoLoc)
                  .update(NumStr, PropValue(ObjectValue(Value(lset_find), T,T,T)))
                  .update("length", PropValue(ObjectValue(Value(UInt), T,T,T)))
                val h_2 = h_1.update(l_result, o_result)
                (h_2, Value(lset_find))
              }
            ((Helper.ReturnStore(h_ret, v_ret), ctx_1), (he, ctxe))
          }
          else
            ((HeapBot, ContextBot), (he, ctxe))
        }))
    )
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map(
      //TODO: not yet implemented
      ("DOMDocument.createElement" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r = addrToLoc(addr1, Recent)
          val l_nodes = addrToLoc(addr2, Recent)
          val (h_1, ctx_1)  = PreHelper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = PreHelper.Oldify(h_1, ctx_1, addr2)

          val s_tag = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h_2, ctx_2, args, "0", PureLocalLoc)))

          // DOMException object with the INVALID_CHARACTER_ERR exception code
          val es = Set(DOMException.INVALID_CHARACTER_ERR)
          val (he_1, ctxe_1) = DOMHelper.PreRaiseDOMException(h_2, ctx_2, PureLocalLoc, es)
          /* imprecise semantics */
          s_tag match {
            // may cause the INVALID_CHARACTER_ERR exception
            case StrTop | OtherStr =>
              val element_obj_proplist = HTMLTopElement.default_getInsList()
              val element_obj = element_obj_proplist.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2, AbsentTop))
              // 'childNodes' update
              val childNodes_list = DOMNodeList.getInsList(0)
              val childNodes = childNodes_list.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
              val element_obj_up = element_obj.update("childNodes", PropValue(ObjectValue(l_nodes, F, T, T)))
              val h_3= h_2.update(l_nodes, childNodes).update(l_r, element_obj_up)
              ((PreHelper.ReturnStore(h_3, PureLocalLoc, Value(l_r)), ctx_2), (he + he_1, ctxe + ctxe_1))
            // cause the INVALID_CHARACTER_ERR exception
            case NumStr | NumStrSingle(_) =>
              ((h_2, ctx_2), (he + he_1, ctxe + ctxe_1))
            case OtherStrSingle(s) =>
              println("OtherStrSinglne")
              val tag_name = s.toUpperCase
              if(DOMHelper.isValidHtmlTag(tag_name)) {
                val element_obj_proplist = DOMElement.getInsList(PropValue(ObjectValue(AbsString.alpha(tag_name), F, T, T))):::DOMHelper.getInsList(tag_name)
                val element_obj = element_obj_proplist.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
                // 'childNodes' update
                val childNodes_list = DOMNodeList.getInsList(0)
                val childNodes = childNodes_list.foldLeft(ObjEmpty)((x, y) => x.update(y._1, y._2))
                val element_obj_up = element_obj.update("childNodes", PropValue(ObjectValue(l_nodes, F, T, T)))
                val h_3= h_2.update(l_nodes, childNodes).update(l_r, element_obj_up)
                ((PreHelper.ReturnStore(h_3, PureLocalLoc, Value(l_r)), ctx_2), (he, ctxe))
              }
              // An invalid tag name causes the INVALID_CHARACTER_ERR exception
              else
                ((h_2, ctx_2), (he + he_1, ctxe + ctxe_1))
            case StrBot =>
              ((h_2, ctx_2), (he, ctxe))
          }
        })),
      //case "DOMDocument.createDocumentFragment" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createTextNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createComment" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createCDATASection" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createProcessingInstruction" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createAttribute" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createEntityReference" => ((h, ctx), (he, ctxe))
      ("DOMDocument.getElementsByTagName" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          /* arguments */
          val s_name = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
          if (s_name </ StrBot) {
            val obj_table = h(TagTableLoc)
            val propv_element = obj_table(s_name.toUpperCase)
            val (h_1, ctx_1, v_empty) =
              if (propv_element._2 </ AbsentBot) {
                val l_r = addrToLoc(addr1, Recent)
                val (_h, _ctx)  = PreHelper.Oldify(h, ctx, addr1)
                /* empty NodeList */
                val o_empty = Obj(DOMNodeList.getInsList(0).foldLeft(ObjEmpty.map)((o,pv) =>
                  o.updated(pv._1, (pv._2, AbsentBot))))
                val _h1 = _h.update(l_r, o_empty)
                (_h1, _ctx, Value(l_r))
              } else (h, ctx, ValueBot)
            /* imprecise semantic */
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, propv_element._1._1._1 + v_empty), ctx_1), (he, ctxe))
          }
          else
            ((h, ctx), (he, ctxe))
        })),
      //case "DOMDocument.importNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createElementNS" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createAttributeNS" => ((h, ctx), (he, ctxe))
      ("DOMDocument.getElementsByTagNameNS" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          /* arguments */
          val s_ns = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
          val s_name = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "1", PureLocalLoc)))
          if (s_ns </StrBot && s_name </ StrBot) {
            val obj_table = h(TagTableLoc)
            val propv_element = obj_table(s_name.toUpperCase)
            val (h_1, ctx_1, v_empty) =
              if (propv_element._2 </ AbsentBot) {
                val l_r = addrToLoc(addr1, Recent)
                val (_h, _ctx)  = PreHelper.Oldify(h, ctx, addr1)
                /* empty NodeList */
                val o_empty = Obj(DOMNodeList.getInsList(0).foldLeft(ObjEmpty.map)((o,pv) =>
                  o.updated(pv._1, (pv._2, AbsentBot))))
                val _h1 = _h.update(l_r, o_empty)
                (_h1, _ctx, Value(l_r))
              } else (h, ctx, ValueBot)
            /* imprecise semantic */
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, propv_element._1._1._1 + v_empty), ctx_1), (he, ctxe))
          }
          else
            ((h, ctx), (he, ctxe))
        })),
      ("DOMDocument.getElementById" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          /* arguments */
          val s_id = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
          if (s_id </ StrBot) {
            val obj_table = h(IdTableLoc)
            val propv_element = obj_table(s_id)
            val v_null = if (propv_element._2 </ AbsentBot) Value(NullTop) else ValueBot
            /* imprecise semantic */
            ((PreHelper.ReturnStore(h, PureLocalLoc, propv_element._1._1._1 + v_null), ctx), (he, ctxe))
          }
          else {
            ((h, ctx), (he, ctxe))
          }
        })),
      ("DOMDocument.getElementsByClassName" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val PureLocalLoc = cfg.getPureLocal(cp)
          val lset_env = h(PureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          /* arguments */
          val s_class = PreHelper.toString(PreHelper.toPrimitive(getArgValue_pre(h, ctx, args, "0", PureLocalLoc)))
          if (s_class </ StrBot) {
            val propv_element = h(ClassTableLoc)(s_class)
            val (h_1, ctx_1, v_empty) =
              if (propv_element._2 </ AbsentBot) {
                val l_r = addrToLoc(addr1, Recent)
                val (_h, _ctx)  = Helper.Oldify(h, ctx, addr1)
                /* empty NodeList */
                val o_empty = Obj(DOMNodeList.getInsList(0).foldLeft(ObjEmpty.map)((o,pv) =>
                  o.updated(pv._1, (pv._2, AbsentBot))))
                val _h1 = _h.update(l_r, o_empty)
                (_h1, _ctx, Value(l_r))
              } else (h, ctx, ValueBot)
            /* imprecise semantic */
            ((PreHelper.ReturnStore(h_1, PureLocalLoc, propv_element._1._1._1 + v_empty), ctx_1), (he, ctxe))
          }
          else
            ((h, ctx), (he, ctxe))
        }))
      //case "DOMDocument.adoptNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.normalizeDocument" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.renameNode" => ((h, ctx), (he, ctxe))
    )
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map(
      ("DOMDocument.createElement" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //val l_r = addrToLoc(addr1, Recent)
          //val l_nodes = addrToLoc(addr2, Recent)
          //val LP1 = AccessHelper.Oldify_def(h, ctx, addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) =>
            lp ++ AccessHelper.Oldify_def(h, ctx, cfg.getAPIAddress(a, 0)))
          //val LP2 = AccessHelper.Oldify_def(h, ctx, addr2)
          val LP2 = set_addr.foldLeft(LPBot)((lp, a) =>
            lp ++ AccessHelper.Oldify_def(h, ctx, cfg.getAPIAddress(a, 1)))

          val s_tag = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))

          // DOMException object with the INVALID_CHARACTER_ERR exception code
          val es = Set(DOMException.INVALID_CHARACTER_ERR)
          val LP3 = DOMHelper.RaiseDOMException_def(es)

          /* imprecise semantics */
          val LP4 = s_tag match {
            // may cause the INVALID_CHARACTER_ERR exception
            case StrTop | OtherStr =>
              val element_obj_proplist = HTMLTopElement.default_getInsList
              val LP4_1 = element_obj_proplist.foldLeft(LPBot)((lpset, prop) =>
                lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0),Recent), prop._1)))
              // 'childNodes' update
              val childNodes_list = DOMNodeList.getInsList(0)
              val LP4_2 = childNodes_list.foldLeft(LPBot)((lpset, prop) =>
                lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 1),Recent), prop._1)))
              //LP4_1 ++ LP4_2 + (l_r, "childNodes")
              LP4_1 ++ LP4_2 ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0),Recent), "childNodes"))
            // cause the INVALID_CHARACTER_ERR exception
            case NumStr | NumStrSingle(_) => LPBot
            case OtherStrSingle(s) =>
              val tag_name = s.toUpperCase
              if(DOMHelper.isValidHtmlTag(tag_name)) {
                val element_obj_proplist = DOMElement.getInsList(PropValue(ObjectValue(AbsString.alpha(tag_name), F, T, T))):::DOMHelper.getInsList(tag_name)
                //val LP4_1 = element_obj_proplist.foldLeft(LPBot)((lpset, prop) => lpset + (l_r, prop._1))
                val LP4_1 = element_obj_proplist.foldLeft(LPBot)((lpset, prop) =>
                  lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0),Recent), prop._1)))
                // 'childNodes' update
                val childNodes_list = DOMNodeList.getInsList(0)
                //val LP4_2 = childNodes_list.foldLeft(LPBot)((lpset, prop) => lpset + (l_nodes, prop._1))
                val LP4_2 = childNodes_list.foldLeft(LPBot)((lpset, prop) =>
                  lpset ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 1),Recent), prop._1)))
                //LP4_1 ++ LP4_2 + (l_r, "childNodes")
                LP4_1 ++ LP4_2 ++ set_addr.foldLeft(LPBot)((lp, a) => lp + (addrToLoc(cfg.getAPIAddress(a, 0),Recent), "childNodes"))
              }
              // An invalid tag name causes the INVALID_CHARACTER_ERR exception
              else
                LPBot
            case StrBot => LPBot
          }
          LP1 ++ LP2 ++ LP3 ++ LP4 + (SinglePureLocalLoc, "@return")
        })),
      //case "DOMDocument.createDocumentFragment" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createTextNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createComment" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createCDATASection" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createProcessingInstruction" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createAttribute" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createEntityReference" => ((h, ctx), (he, ctxe))
      ("DOMDocument.getElementsByTagName" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      //case "DOMDocument.importNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createElementNS" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createAttributeNS" => ((h, ctx), (he, ctxe))
      ("DOMDocument.getElementsByTagNameNS" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("DOMDocument.getElementById" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        })),
      ("DOMDocument.getElementsByClassName" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          LPSet((SinglePureLocalLoc, "@return"))
        }))
      //case "DOMDocument.adoptNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.normalizeDocument" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.renameNode" => ((h, ctx), (he, ctxe))
    )
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map(
      //TODO: not yet implemented
      ("DOMDocument.createElement" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          //val l_r = addrToLoc(addr1, Recent)
          //val l_nodes = addrToLoc(addr2, Recent)
          //val LP1 = AccessHelper.Oldify_use(h, ctx, addr1)
          val LP1 = set_addr.foldLeft(LPBot)((lp, a) =>
            lp ++ AccessHelper.Oldify_use(h, ctx, cfg.getAPIAddress(a, 0)))
          //val LP2 = AccessHelper.Oldify_use(h, ctx, addr2)
          val LP2 = set_addr.foldLeft(LPBot)((lp, a) =>
            lp ++ AccessHelper.Oldify_use(h, ctx, cfg.getAPIAddress(a, 1)))

          val s_tag = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          val LP3 = getArgValue_use(h, ctx, args, "0")

          // DOMException object with the INVALID_CHARACTER_ERR exception code
          val es = Set(DOMException.INVALID_CHARACTER_ERR)
          val LP4 = DOMHelper.RaiseDOMException_use(es)
          LP1 ++ LP2 ++ LP3 ++ LP4 + (SinglePureLocalLoc, "@return")
        })),
      /* imprecise semantics */
      //case "DOMDocument.createDocumentFragment" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createTextNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createComment" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createCDATASection" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createProcessingInstruction" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createAttribute" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createEntityReference" => ((h, ctx), (he, ctxe))
      ("DOMDocument.getElementsByTagName" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          /* arguments */
          val s_name = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          getArgValue_use(h, ctx, args, "0") ++ AccessHelper.absPair(h, TagTableLoc, s_name) + (SinglePureLocalLoc, "@return")
        })),

      //case "DOMDocument.importNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createElementNS" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.createAttributeNS" => ((h, ctx), (he, ctxe))
      ("DOMDocument.getElementsByTagNameNS" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val s_name = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "1")))
          getArgValue_use(h, ctx, args, "0") ++ getArgValue_use(h, ctx, args, "1") ++
            AccessHelper.absPair(h, TagTableLoc, s_name) + (SinglePureLocalLoc, "@return")
        })),
      ("DOMDocument.getElementById" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val s_id = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          getArgValue_use(h, ctx, args, "0") ++ AccessHelper.absPair(h, IdTableLoc, s_id) + (SinglePureLocalLoc, "@return")
        })),
      ("DOMDocument.getElementsByClassName" -> (
        (h: Heap, ctx: Context, cfg: CFG, fun: String, args: CFGExpr) => {
          val s_class = Helper.toString(Helper.toPrimitive(getArgValue(h, ctx, args, "0")))
          getArgValue_use(h, ctx, args, "0") ++ AccessHelper.absPair(h, ClassTableLoc, s_class) + (SinglePureLocalLoc, "@return")
        }))

      //case "DOMDocument.adoptNode" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.normalizeDocument" => ((h, ctx), (he, ctxe))
      //case "DOMDocument.renameNode" => ((h, ctx), (he, ctxe))
    )
  }
  /* instance */

  /* list of properties in the instance object */
  override def getInsList(node: Node): List[(String, PropValue)] = node match {
    case d: Document =>
      val xmlEncoding = d.getXmlEncoding;
      // This instance object has all properties of the Node object
      DOMNode.getInsList(node) ++ List(
        // Introduced in DOM Level 3
        ("inputEncoding",   PropValue(ObjectValue(AbsString.alpha(d.getInputEncoding), F, T, T))),
        ("xmlEncoding",   PropValue(ObjectValue(AbsString.alpha(if(xmlEncoding!=null) xmlEncoding else ""), F, T, T))),
        ("xmlStandalone",   PropValue(ObjectValue((if(d.getXmlStandalone==true) T else F), T, T, T))),
        ("xmlVersion",   PropValue(ObjectValue(AbsString.alpha(d.getXmlVersion), T, T, T))),
        ("strictErrorChecking",   PropValue(ObjectValue((if(d.getStrictErrorChecking==true) T else F), F, T, T))),
        ("documentURI",   PropValue(ObjectValue(AbsString.alpha(d.getDocumentURI), T, T, T))),
        // HTML5 : location object
        ("location",   PropValue(ObjectValue(DOMLocation.getInstance.get, F, T, T))))
    // 'documentElement' in DOM Level 1 is updated after the HTMLHtmlElement node is created
    // TODO: 'implementation' in DOM Level 1, 'doctype' in DOM Level 3
    case _ => {
      System.err.println("* Warning: " + node.getNodeName + " cannot be an instance of Document.")
      List()
    }
  }
}
