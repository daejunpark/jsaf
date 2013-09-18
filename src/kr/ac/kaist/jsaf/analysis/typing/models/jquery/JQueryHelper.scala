/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.models.jquery

import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolTrue=>T, BoolFalse=>F, _}
import kr.ac.kaist.jsaf.analysis.typing.Helper
import kr.ac.kaist.jsaf.analysis.typing.models.{JQueryModel, DOMHelper}
import kr.ac.kaist.jsaf.analysis.typing.models.DOMCore.{DOMNodeList, DOMElement}
import kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml.{HTMLDocument, HTMLTopElement}
import kr.ac.kaist.jsaf.analysis.cfg.CFGExpr

object JQueryHelper {

  def NewJQueryObject(): Obj =
    Helper.NewObject(JQuery.ProtoLoc)
      .update("length", PropValue(ObjectValue(AbsNumber.alpha(0), T, T, T)))

  def NewJQueryObject(len : Double): Obj =
    Helper.NewObject(JQuery.ProtoLoc)
      .update("length", PropValue(ObjectValue(AbsNumber.alpha(len), T, T, T)))

  def NewJQueryObject(n_len : AbsNumber): Obj =
    Helper.NewObject(JQuery.ProtoLoc)
      .update("length", PropValue(ObjectValue(n_len, T, T, T)))

  def MakeArray(h: Heap, arr: Value): Obj = {
    MakeArray(h, arr, Helper.NewArrayObject(AbsNumber.alpha(0)))
  }

  def MakeArray(h: Heap, v_arr: Value, o_results: Obj): Obj = {
    val n_len = o_results("length")._1._1._1._1._4 // number
    AbsNumber.concretize(n_len) match {
      case Some(n) =>
        val o_1 =
          if (v_arr._2.isEmpty)
            o_results.
              update(n.toString, PropValue(ObjectValue(v_arr._1, T, T, T))).
              update("length", PropValue(ObjectValue(AbsNumber.alpha(n+1), T, T, T)))
          else
            ObjEmpty
        val o_2 =
          if (!v_arr._2.isEmpty) {
            v_arr._2.foldLeft(o_results)((_o, l) => {
              val n_arrlen = Helper.Proto(h, l, AbsString.alpha("length"))._1._4
              AbsNumber.concretize(n_arrlen) match {
                case Some(n_arr) =>
                  val oo = (0 until n_arr.toInt).foldLeft(_o)((_o1, i) =>
                    _o1.update((n+i).toString,
                      PropValue(ObjectValue(Helper.Proto(h,l,AbsString.alpha(i.toString)), T, T, T))))
                  oo.update("length", PropValue(ObjectValue(AbsNumber.alpha(n+n_arr), T, T, T)))
                case None =>
                  if (n_arrlen <= NumBot)
                    _o
                  else
                    _o.update(NumStr, PropValue(ObjectValue(Helper.Proto(h,l,NumStr), T, T, T)))
              }
            })
          }
          else ObjEmpty
        o_1 + o_2
      case None =>
        if (n_len <= NumBot)
          ObjBot
        else {
          val o_1 =
            if (v_arr._2.isEmpty)
              o_results.update(Helper.toString(PValue(n_len)), PropValue(ObjectValue(v_arr._1, T, T, T)))
            else
              ObjEmpty
          val o_2 =
            if (!v_arr._2.isEmpty) {
              v_arr._2.foldLeft(o_results)((_o, l) =>
                _o.update(NumStr, PropValue(ObjectValue(Helper.Proto(h,l,NumStr), T, T, T))))
            }
            else ObjEmpty
          o_1 + o_2
        }
    }
  }

  def addJQueryEvent(h: Heap, v_elem: Value, s_types: AbsString, v_handler: Value, v_data: Value, v_selector: Value) = {

    val fun_table = h(EventFunctionTableLoc)
    val target_table = h(EventTargetTableLoc)
    val selector_table = h(EventSelectorTableLoc)
    val propv_fun = PropValue(v_handler)
    val propv_target = PropValue(v_elem)
    val propv_selector = PropValue(v_selector)
    val event_list = s_types match {
      case StrTop | OtherStr => JQueryModel.aysnc_calls
      case OtherStrSingle(s_ev) =>
        if (DOMHelper.isLoadEventAttribute(s_ev)) List("#LOAD")
        else if (DOMHelper.isUnloadEventAttribute(s_ev)) List("#UNLOAD")
        else if (DOMHelper.isKeyboardEventAttribute(s_ev) || DOMHelper.isKeyboardEventProperty(s_ev)) List("#KEYBOARD")
        else if (DOMHelper.isMouseEventAttribute(s_ev) || DOMHelper.isMouseEventProperty(s_ev)) List("#MOUSE")
        else if (DOMHelper.isOtherEventAttribute(s_ev) || DOMHelper.isOtherEventProperty(s_ev))List("#OTHER")
        else if (DOMHelper.isReadyEventProperty(s_ev)) List("#READY")
        else List()
      case NumStrSingle(_) => /* Error ?*/ List()
      case NumStr => /* Error ?*/ List()
      case StrBot => List()
    }
    val o_fun = event_list.foldLeft(fun_table)((o, s_ev) =>
      o.update(s_ev, o(s_ev)._1 + propv_fun)
    )
    val o_target = event_list.foldLeft(target_table)((o, s_ev) =>
      o.update(s_ev, o(s_ev)._1 + propv_target)
    )
    val o_selector = event_list.foldLeft(selector_table)((o, s_ev) =>
      o.update(s_ev, o(s_ev)._1 + propv_selector)
    )
    h.update(EventFunctionTableLoc, o_fun).update(EventTargetTableLoc, o_target).update(EventSelectorTableLoc, o_selector)
  }


  private val reg_quick = """^(?:[^#<]*(<[\w\W]+>)[^>]*$|#([\w\-]*)$)""".r
  private val reg_id = """([\w]+)""".r

  def init(h: Heap, v_selector: Value, v_context: Value,
           l_jq: Loc, l_tag: Loc, l_child: Loc): (Heap, Value) = {
    //val h_start = h
    //val ctx_start = ctx_3

    val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2

    // 1) Handle $(""), $(null), $(undefined), $(false)
    val (h_ret1, v_ret1) =
      if (UndefTop <= v_selector._1._1 || NullTop <= v_selector._1._2 ||
        F <= v_selector._1._3 || AbsString.alpha("") <= v_selector._1._5) {
        // empty jQuery object
        (h.update(l_jq, NewJQueryObject), Value(l_jq))
      }
      else
        (HeapBot, ValueBot)

    // 2) Handle $(DOMElement)
    val (h_ret2, v_ret2) =
      if (!v_selector._2.isEmpty) {
        v_selector._2.foldLeft((h, ValueBot))((hv, l) => {
          val v_nodeType = Helper.Proto(h, l, AbsString.alpha("nodeType"))
          if (T <= Helper.toBoolean(v_nodeType)) {
            // jQuery object
            val o_jq = NewJQueryObject(1)
              .update("context", PropValue(ObjectValue(v_selector, T, T, T)))
              .update("0",       PropValue(ObjectValue(v_selector, T, T, T)))
            val _h1 = h.update(l_jq, o_jq)
            (hv._1 + _h1, hv._2 + Value(lset_this))
          }
          else
            hv
        })
      }
      else
        (HeapBot, ValueBot)

    // 3) Handle HTML strings
    val (h_ret3, v_ret3) = v_selector._1._5 match {
      case OtherStrSingle(s) =>
        val matches = reg_quick.unapplySeq(s)
        matches match {
          case Some(mlist) =>
            val tag_name = mlist(0)
            // HANDLE: $(html) -> $(array)
            // unsoud, support only tag name
            if (tag_name != null) {
              //val s_tag = tag_name.filter((c) => c != '<' && c != '>').toUpperCase
              val s_tag = reg_id.findFirstIn(tag_name).get
              // jQuery object
              val o_jq =NewJQueryObject(UInt)
                .update("selector",   PropValue(ObjectValue(v_selector, T, T, T)))
                .update("prevObject", PropValue(ObjectValue(Value(JQuery.RootJQLoc), T, T, T)))
                .update("0",          PropValue(ObjectValue(Value(l_tag), T, T, T)))
              val _h1 = DOMHelper.addTag(h, s_tag, l_tag, l_child).update(l_jq, o_jq)
              (_h1, Value(l_jq))
            }
            // HANDLE: $(#id)
            else {
              val s_id = mlist(1)
              // getElementById
              val lset_id = DOMHelper.findById(h, AbsString.alpha(s_id))
              // jQuery object
              val o_jq = NewJQueryObject(lset_id.size)
                .update("selector", PropValue(ObjectValue(v_selector, T, T, T)))
              val o_jq1 =
                if (lset_id.isEmpty)
                  o_jq
                else
                  o_jq.update("0", PropValue(ObjectValue(Value(lset_id), T, T, T)))
              (h.update(l_jq, o_jq1), Value(l_jq))
            }
          case None =>
            // HANDLE: $(expr, $(...))
            // else if ( !context || context.jquery ) {
            val (h1, v1) =
//              if (v_context._1._1 </ UndefBot) {
              if (v_context._2 == LocSetBot) {
                // prev = rootjQuery
                val lset_find = DOMHelper.querySelectorAll(h, s)
                // jQuery object
                val o_jq =NewJQueryObject(lset_find.size)
                  .update("selector",   PropValue(ObjectValue(v_selector, T, T, T)))
                  .update("prevObject", PropValue(ObjectValue(Value(JQuery.RootJQLoc), T, T, T)))
                val o_jq1 =
                  if (lset_find.isEmpty)
                    o_jq
                  else
                    o_jq.update(NumStr, PropValue(ObjectValue(Value(lset_find), T, T, T)))
                (h.update(l_jq, o_jq1), Value(l_jq))
              }
              else {
                // TODO : we should find elements using selector in the context
                // prev = rootjQuery
                val lset_find = DOMHelper.querySelectorAll(h, s)
                // jQuery object
                val o_jq =NewJQueryObject(lset_find.size)
                  .update("selector",   PropValue(ObjectValue(v_selector, T, T, T)))
                  .update("prevObject", PropValue(ObjectValue(Value(JQuery.RootJQLoc), T, T, T)))
                val o_jq1 =
                  if (lset_find.isEmpty)
                    o_jq
                  else
                    o_jq.update(NumStr, PropValue(ObjectValue(Value(lset_find), T, T, T)))
                (h.update(l_jq, o_jq1), Value(l_jq))
              }
//               (HeapBot, ValueBot)

            val v_jquery = v_context._2.foldLeft(ValueBot)((v,l) =>
              v + Helper.Proto(h, l, AbsString.alpha("jquery"))
            )
            val (h2, v2) =
              if (UndefTop <= v_context._1._1 && v_jquery._1._1 </ UndefBot) {
                // prev = context
                val lset_context = v_context._2.foldLeft(LocSetBot)((lset, l) => lset ++ h(l)(NumStr)._1._1._1._2)
                val lset_find = lset_context.foldLeft(LocSetBot)((lset, l) => lset ++ DOMHelper.querySelectorAll(h, s))
                // jQuery object
                val o_jq = NewJQueryObject(lset_find.size)
                  .update("selector", PropValue(ObjectValue(v_selector, T, T, T)))
                  .update("prevObject", PropValue(ObjectValue(v_context, T, T, T)))
                val o_jq1 =
                  if (lset_find.isEmpty)
                    o_jq
                  else
                    o_jq.update(NumStr, PropValue(ObjectValue(Value(lset_find), T, T, T)))
                (h.update(l_jq, o_jq1), Value(l_jq))
              }
              else
                (HeapBot, ValueBot)

            // TODO: HANDLE: $(expr, context)
            // (which is just equivalent to: $(context).find(expr)
            // return this.constructor( context ).find( selector );

            (h1 + h2, v1 + v2)
        }
      case OtherStr | StrTop =>
        // top element
        val _h1 = DOMHelper.addTagTop(h, l_tag, l_jq)
        // jQuery object
        val o_jq = NewJQueryObject(UInt)
          .update("selector", PropValue(ObjectValue(v_selector, T, T, T)))
          .update("prevObject", PropValue(ObjectValue(Value(JQuery.RootJQLoc) + Value(l_jq), T, T, T)))
          .update(NumStr, PropValue(ObjectValue(Value(l_tag) + Value(HTMLTopElement.getInsLoc), T, T, T)))
        (_h1.update(l_jq, o_jq), Value(l_jq))
      case NumStrSingle(_) | NumStr =>
        // jQuery object
        val o = NewJQueryObject(0)
          .update("selector", PropValue(ObjectValue(v_selector, T, T, T)))
          .update("prevObject", PropValue(ObjectValue(Value(JQuery.RootJQLoc) + Value(l_jq), T, T, T)))
          .update("context", PropValue(ObjectValue(HTMLDocument.GlobalDocumentLoc, T, T, T)))
        (h.update(l_jq, o), Value(l_jq))
      case StrBot =>
        (HeapBot, ValueBot)
    }

    // 4) HANDLE: $(function), Shortcut for document ready event
    val lset_f = v_selector._2.filter(l => T <= Helper.IsCallable(h, l))
    val (h_ret4, v_ret4) =
      if (!lset_f.isEmpty) {
        val h1 = addJQueryEvent(h, Value(HTMLDocument.GlobalDocumentLoc),
          AbsString.alpha("DOMContentLoaded"), Value(lset_f), ValueBot, ValueBot)
        (h1, Value(JQuery.RootJQLoc))
      }
      else
        (HeapBot, ValueBot)

    // Handle: else
    val (h_ret5, v_ret5) = v_selector._2.foldLeft((HeapBot, ValueBot))((hv, l) => {
      // jquery  object
      val o_1 =
        if (Helper.Proto(h, l, AbsString.alpha("selector"))._1._1 </ UndefBot) {
          NewJQueryObject().
            update("selector", PropValue(ObjectValue(Helper.Proto(h, l, AbsString.alpha("selector")), T, T, T))).
            update("context", PropValue(ObjectValue(Helper.Proto(h, l, AbsString.alpha("context")), T, T, T)))
        }
        else
          NewJQueryObject()
      // make array
      val o_2 = MakeArray(h, v_selector, o_1)
      val _h1 = h.update(l_jq, o_2)
      (hv._1 + _h1, hv._2 + Value(l_jq))
    })

    val h_ret = h_ret1 + h_ret2 + h_ret3 + h_ret4 + h_ret5
    val v_ret = v_ret1 + v_ret2 + v_ret3 + v_ret4 + v_ret5
    (h_ret, v_ret)
  }


  def extend(h: Heap, args: List[Value]): (Heap, Value) = {
    val len = args.length
    if (len <= 0) {
      (HeapBot, ValueBot)
    }
    else if (len == 1) {
      // target = this
      val lset_this = h(SinglePureLocalLoc)("@this")._1._2._2
      val lset_arg1 = args(0)._2
      val h_ret = lset_this.foldLeft(h)((h1, l1) =>
        lset_arg1.foldLeft(h1)((h2, l2) => {
          val props = h2(l2).getProps
          val h2_1 = props.foldLeft(h2)((h3, p) =>
            Helper.PropStore(h3, l1, AbsString.alpha(p), Helper.Proto(h3, l2, AbsString.alpha(p)))
          )
          val o_arg1 = h2_1(l2)
          val o_target = h2_1(l1)
          val o_target_new = o_target
            .update("@default_number", o_arg1("@default_number")._1 + o_target("@default_number")._1, AbsentTop)
            .update("@default_other", o_arg1("@default_other")._1 + o_target("@default_other")._1, AbsentTop)
          h2_1.update(l1, o_target_new)
        })
      )
      (h_ret, Value(lset_this))
    }
    else {
      val v_arg1 = args(0)
      val (target, list_obj) =
        if (v_arg1._1._3 </ BoolBot)
          (args(1), args.tail.tail)
        else
          (v_arg1, args.tail)
      val lset_target = target._2
      val lset_obj = list_obj.foldLeft(LocSetBot)((lset, v) => lset ++ v._2)
      val h_ret = lset_target.foldLeft(h)((h1, l1) =>
        lset_obj.foldLeft(h1)((h2, l2) => {
          val props = h2(l2).getProps
          val h2_1 = props.foldLeft(h2)((h3, p) =>
            Helper.PropStore(h3, l1, AbsString.alpha(p), Helper.Proto(h3, l2, AbsString.alpha(p)))
          )
          val o_arg1 = h2_1(l2)
          val o_target = h2_1(l1)
          val o_target_new = o_target
            .update("@default_number", o_arg1("@default_number")._1 + o_target("@default_number")._1, AbsentTop)
            .update("@default_other", o_arg1("@default_other")._1 + o_target("@default_other")._1, AbsentTop)
          h2_1.update(l1, o_target_new)
        })
      )
      (h_ret, Value(lset_target))
    }
  }

  def pushStack(h: Heap, lset_prev: LocSet, lset_next: LocSet): Heap = {
    val v_context = lset_prev.foldLeft(ValueBot)((v, l) => v+ Helper.Proto(h, l, AbsString.alpha("context")))
    lset_next.foldLeft(h)((h1, l1) => {
      val h1_1 = Helper.PropStore(h1, l1, AbsString.alpha("context"), v_context)
      Helper.PropStore(h1_1, l1, AbsString.alpha("prevObject"), Value(lset_prev))
    })
  }

  def isArraylike(h: Heap, l: Loc): AbsBool = {
    val n_len = Helper.Proto(h, l, AbsString.alpha("length"))._1._4
    val s_class = h(l)("@class")._1._2._1._5
    val b1 =
      if (n_len </ NumBot && AbsString.alpha("Function") </ s_class)
        T
      else
        BoolBot
    val b2 =
      if (n_len <= NumBot || AbsString.alpha("Function") <= s_class)
        F
      else
        BoolBot
    b1 + b2
  }
}
