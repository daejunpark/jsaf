/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import scala.collection.immutable.HashSet
import scala.collection.mutable.{HashMap => MHashMap}
import scala.collection.mutable.{Map => MMap}
import scala.runtime.RichDouble

import kr.ac.kaist.jsaf.analysis.asserts._
import kr.ac.kaist.jsaf.analysis.asserts.{ASSERTHelper => AH}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.Operator._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes_util.EJSOp
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.nodes.IROp
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.analysis.typing.{PreSemanticsExpr => PSE}
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolTrue => BTrue, BoolFalse => BFalse}
import kr.ac.kaist.jsaf.analysis.typing.DomSemantics._

class Semantics(cfg : CFG, worklist: Worklist) {
  // Semantics of inter-procedural edge from cp1 to cp2 with context label ctx.
  def E(cp1: ControlPoint, cp2: ControlPoint, ctx: Context, obj: Obj, s: State): State = {
    cp2 match {
      case ((_, LEntry),_) =>
        // System.out.println("== "+cp1 +" -> "+cp2+" ==")
        // System.out.println(DomainPrinter.printHeap(4, s._1))
        // System.out.println("== Object ==")
        // System.out.println(DomainPrinter.printObj(4, obj))
        // call edge
        if (s._1 == HeapBot) {
          StateBot
        } else {
          val env_obj = Helper.NewDeclEnvRecord(obj("@scope")._1._2._2)
          val obj2 = obj - "@scope"
          val h1 = s._1
          val h2 = h1.remove(SinglePureLocalLoc)
          val h3 = h2.update(SinglePureLocalLoc, obj2)
          val h4 = obj2("@env")._1._2._2.foldLeft(HeapBot)((hh, l_env) => {
            hh + h3.update(l_env, env_obj)
          })
          // Localization
          val h5 =
            if (cfg.optionLocalization) {
              val useset = cfg.getLocalizationSet(cp2._1._1)
              h4.restrict(useset)
            } else {
              h4
            }
          State(h5, ctx)
        }

      case _ => cp1 match {
        case ((_, LExit),_) =>
          // System.out.println("== "+cp1 +" -> "+cp2+" ==")
          // System.out.println(DomainPrinter.printHeap(4, s._1))
          // System.out.println("== Object ==")
          // System.out.println(DomainPrinter.printObj(4, obj))
          // exit return edge
          if (s._1 == HeapBot || s._2.isBottom) {
            StateBot
          } else {
            val returnVar = cfg.getReturnVar(cp2._1) match {
              case Some(x) => x
              case None => throw new InternalError("After-call node must have return variable")
            }
            val h1 = s._1
            val ctx1 = s._2
            val (ctx2, obj1) = Helper.FixOldify(ctx, obj, ctx1._3, ctx1._4)
            if (ctx2.isBottom) StateBot
            else {
              val v = h1(SinglePureLocalLoc)("@return")._1._2
              val h2 = h1.update(SinglePureLocalLoc, obj1)
              val h3 = Helper.VarStore(h2, returnVar, v)
              State(h3, ctx2)
            }
          }

        case ((_, LExitExc),_) =>
          // exit-exc return edge
          if (s._1 == HeapBot || s._2.isBottom) {
            StateBot
          } else {
            val h1 = s._1
            val ctx1 = s._2
            val (ctx2, obj1) = Helper.FixOldify(ctx, obj, ctx1._3, ctx1._4)
            if (ctx2.isBottom) StateBot
            else {
              val v = h1(SinglePureLocalLoc)("@exception")._1._2
              val v_old = obj1("@exception_all")._1._2
              val h2 = h1.update(SinglePureLocalLoc,
                                 obj1.update("@exception", PropValue(v)).
                                      update("@exception_all", PropValue(v + v_old)))
              State(h2, ctx2)
            }
          }

        case _ => throw new InternalError("Inter-procedural edge must be call or return edge.")
      }
    }
  }

  def C(cp: ControlPoint, c: Cmd, s: State): (State, State) = {
    val h = s._1
    val ctx = s._2

    if (h <= HeapBot) {
      (StateBot, StateBot)
    } else {
      val ((h_1, ctx_1), (he_1, ctxe_1)) = c match {
        case Entry =>
          val (fid, l) = cp._1
          val x_argvars = cfg.getArgVars(fid)
          val x_localvars = cfg.getLocalVars(fid)
          val lset_arg = h(SinglePureLocalLoc)(cfg.getArgumentsName(fid))._1._1._1._2
          var i = 0
          val h_n = x_argvars.foldLeft(h)((hh, x) => {
            val v_i = lset_arg.foldLeft(ValueBot)((vv, l_arg) => {
              vv + Helper.Proto(hh, l_arg, AbsString.alpha(i.toString))
            })
            i = i + 1
            Helper.CreateMutableBinding(hh, x, v_i)
          })
          val h_m = x_localvars.foldLeft(h_n)((hh, x) => {
            Helper.CreateMutableBinding(hh, x, Value(UndefTop))
          })
          ((h_m, ctx), (HeapBot, ContextBot))

        case Exit => ((h, ctx), (HeapBot, ContextBot))
        case ExitExc => ((h, ctx), (HeapBot, ContextBot))

        case Block(insts) => {
          insts.foldLeft(((h, ctx), (HeapBot, ContextBot)))(
            (states, inst) => {
              val ((h_new, ctx_new), (he_new, ctxe_new)) = I(cp, inst, states._1._1, states._1._2, states._2._1, states._2._2)
              // System.out.println("out heap#####\n" + DomainPrinter.printHeap(4, h_new))
              // System.out.println("out context#####\n" + DomainPrinter.printContext(4, ctx_new))
              ((h_new, ctx_new), (he_new, ctxe_new))
              // val h_merged = states._1._1 + h_new
              // val c_merged = states._1._2 + ctx_new
              // val lpdefset = Access.I_def(inst, h_merged, c_merged)
              // val lpuseset = Access.I_use(inst, h_merged, c_merged)
              // val defset = lpdefset.toSet
              // val useset = lpuseset.toSet

              // val realdef = (Access.heap_diff(states._1._1, h_new)).toSet
              // if ((!realdef.subsetOf(defset)) && (h_new != HeapBot)){
              //   val omitted = realdef &~ defset
              //   System.err.println("* Warning: access-analysis defset is unsound for "+inst)
              //   System.err.println("== defset ==")
              //   defset.foreach((v) => System.err.println("("+v._1 + ", "+ v._2 +")"))
              //   System.err.println("== real defset ==")
              //   realdef.foreach((v) => System.err.println("("+v._1 + ", "+ v._2 +")"))
              //   System.err.println("== missing defset ==")
              //   omitted.foreach((v) => System.err.println("("+v._1 + ", "+ v._2 +")"))
              //   System.err.println("== Before ==")
              //   System.err.println(DomainPrinter.printHeap(4, states._1._1))
              //   System.err.println("== After ==")
              //   System.err.println(DomainPrinter.printHeap(4, h_new))
              // }

              // if (useset.isEmpty) {
              //   System.err.println("* Warning: useset semantic function is missing for "+inst)
              // } else {
              //   val ((h_use, ctx_use), (he_use, ctxe_use)) =
              //     try {
              //       I(cp, inst, states._1._1.restrict(lpuseset), states._1._2.restrict(lpuseset), states._2._1, states._2._2)
              //     } catch {
              //       case e => {
              //         System.err.println("* Warning: access-analysis useset is unsound for "+inst)
              //         System.err.println("== restricted heap ==")
              //         System.err.println(DomainPrinter.printHeap(4, states._1._1.restrict(lpuseset)))
              //         System.err.println("== defset ==")
              //         defset.foreach((v) => System.err.println("("+v._1 + ", "+ v._2 +")"))
              //         System.err.println("== useset ==")
              //         useset.foreach((v) => System.err.println("("+v._1 + ", "+ v._2 +")"))
              //         System.err.println("== Heap ==")
              //         System.err.println(DomainPrinter.printHeap(4, states._1._1))
              //         e.printStackTrace()
              //         ((h_new, ctx_new), (he_new, ctxe_new))
              //       }
              //     }

              //   val check = try {
              //     Access.heap_check(h_new, h_use, lpdefset)
              //   } catch {
              //     case e => {
              //       e.printStackTrace()
              //     }
              //     true
              //   }
              //   if (!check) {
              //     System.err.println("* Warning: access-analysis useset is unsound for "+inst)
              //     System.err.println("== restricted heap ==")
              //     System.err.println(DomainPrinter.printHeap(4, states._1._1.restrict(lpuseset)))
              //     System.err.println("== defset ==")
              //     defset.foreach((v) => System.err.println("("+v._1 + ", "+ v._2 +")"))
              //     System.err.println("== useset ==")
              //     useset.foreach((v) => System.err.println("("+v._1 + ", "+ v._2 +")"))
              //     System.err.println("== restricted output Heap ==")
              //     System.err.println(DomainPrinter.printHeap(4, h_use))
              //     System.err.println("== normal output Heap ==")
              //     System.err.println(DomainPrinter.printHeap(4, h_new))
              //     System.err.println("== normal input heap ==")
              //     System.err.println(DomainPrinter.printHeap(4, states._1._1))
              //   }
              // }
              // ((h_new, ctx_new), (he_new, ctxe_new))
              /*   the end of test code for access analysis */
            })
        }
      }
      (State(h_1, ctx_1), State(he_1, ctxe_1))
    }
  }

  def I(cp: ControlPoint, i: CFGInst, h: Heap, ctx: Context, he: Heap, ctxe: Context) = {
    // System.out.println("\nInstruction: "+i)
    // System.out.println("in heap#####\n" + DomainPrinter.printHeap(4, h))
    // System.out.println("in context#####\n" + DomainPrinter.printContext(4, ctx))
    if (h == HeapBot) {
      ((h, ctx), (he, ctxe))
    } else {
      val s = i match {
        case CFGAlloc(_, _, x, e, a_new) => {
          val l_r = addrToLoc(a_new, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, a_new)
          val (ls_v, es) = e match {
            case None => (ObjProtoSingleton, ExceptionBot)
            case Some(proto) => {
              val (v,es_) = SE.V(proto, h_1, ctx_1)
              if (v._1 </ PValueBot)
                (v._2 ++ ObjProtoSingleton, es_)
              else
                (v._2, es_)
            }
          }
          val h_2 = Helper.allocObject(h_1, ls_v, l_r)
          val h_3 = Helper.VarStore(h_2, x, Value(l_r))
          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
          val s = (he + h_e, ctxe + ctx_e)

            ((h_3, ctx_1), s)
        }
        case CFGAllocArray(_, _, x, n, a_new) => {
          val l_r = addrToLoc(a_new, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, a_new)
          val np = AbsNumber.alpha(n.toInt)
          val h_2 = h_1.update(l_r, Helper.NewArrayObject(np))
          val h_3 = Helper.VarStore(h_2, x, Value(l_r))
          ((h_3, ctx_1), (he, ctxe))
        }
        case CFGAllocArg(_, _, x, n, a_new) => {
          val l_r = addrToLoc(a_new, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, a_new)
          val np = AbsNumber.alpha(n.toInt)
          val h_2 = h_1.update(l_r, Helper.NewArgObject(np))
          val h_3 = Helper.VarStore(h_2, x, Value(l_r))
          ((h_3, ctx_1), (he, ctxe))
        }
        case CFGExprStmt(_, _, x, e) => {
          val (v,es) = SE.V(e, h, ctx)
          val (h_1, ctx_1) =
            if (v </ ValueBot) {
              (Helper.VarStore(h, x, v), ctx)
            } else {
              (HeapBot, ContextBot)
            }
          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
          ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        case CFGDelete(_, _, lhs, expr) =>
          expr match {
            case CFGVarRef(_, id) => {
              val lset_base = Helper.LookupBase(h, id)
              val (h_1, b) =
                if (lset_base.isEmpty) {
                  (h, BoolTrue)
                } else {
                  val abs_id = AbsString.alpha(id.toString)
                  lset_base.foldLeft[(Heap,AbsBool)](HeapBot, BoolBot)((v, l_base) => {
                    val (h_d, b_d) = Helper.Delete(h, l_base, abs_id)
                    (v._1 + h_d, v._2 + b_d)
                  })
                }
              val h_2 = Helper.VarStore(h_1, lhs, Value(b))
              ((h_2, ctx), (he, ctxe))
            }

            case _ => {
              val (v, es) = SE.V(expr, h, ctx)
              val (h_1, ctx_1) =
                if (v </ ValueBot) {
                  (Helper.VarStore(h, lhs, Value(BoolTrue)), ctx)
                } else {
                  (HeapBot, ContextBot)
                }
              val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
              ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
            }
          }
        case CFGDeleteProp(_, _, lhs, obj, index) => {
          val (v_index, es) = SE.V(index, h, ctx)
          val (h_2, ctx_2) =
            if (v_index <= ValueBot) (HeapBot, ContextBot)
            else {
              // lset must not be empty because obj is coming through <>toObject.
              val lset = SE.V(obj, h, ctx)._1._2

              val sset = Helper.toStringSet(Helper.toPrimitive(v_index))
              val (h_1, b) = lset.foldLeft[(Heap, AbsBool)](HeapBot, BoolBot)((res1, l) => {
                sset.foldLeft(res1)((res2, s) => {
                  val (h_,b_) = Helper.Delete(h,l,s)
                  (res2._1 + h_, res2._2 + b_)
                })
              })
              (Helper.VarStore(h_1, lhs, Value(b)), ctx)
            }
          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
          ((h_2, ctx_2), (he + h_e, ctxe + ctx_e))
        }
        case CFGStore(_, _, obj, index, rhs) => {
          // TODO: toStringSet should be used in more optimized way
          val (h_1, ctx_1, es_1) = {
            val (v_index, es_index) = SE.V(index, h, ctx)
            if (v_index <= ValueBot) (HeapBot, ContextBot, es_index)
            else {
              val (v_rhs, es_rhs) = SE.V(rhs, h, ctx)
              if (v_rhs <= ValueBot) (HeapBot, ContextBot, es_index ++ es_rhs)
              else {
                // lset must not be empty because obj is coming through <>toObject.
                val lset = SE.V(obj, h, ctx)._1._2

                // iterate over set of strings for index
                val sset = Helper.toStringSet(Helper.toPrimitive(v_index))
                val (h_2, es_2) = sset.foldLeft((HeapBot, es_index ++ es_rhs))((res, s) => {
                  // non-array objects
                  val lset_narr = lset.filter(l => (BFalse <= Helper.IsArray(h, l)) && BTrue <= Helper.CanPut(h, l, s))
                  // array objects
                  val lset_arr = lset.filter(l => (BTrue <= Helper.IsArray(h, l)) && BTrue <= Helper.CanPut(h, l, s))
                  // can not store
                  val h_cantput =
                    if (lset.exists((l) => BFalse <= Helper.CanPut(h, l, s))) h
                    else HeapBot
                  // store for non-array object
                  //val h_narr = lset_narr.foldLeft(HeapBot)((_h, l) => _h + Helper.PropStore(h, l, s, v_rhs))
                  val h_narr =
                    if (lset_narr.size == 0)
                      HeapBot
                    else if (lset_narr.size == 1)
                      Helper.PropStore(h, lset_narr.head, s, v_rhs)
                    else {
                      lset_narr.foldLeft(h)((hh, l) => Helper.PropStoreWeak(hh, l, s, v_rhs))
                    }
                  // 15.4.5.1 [[DefineOwnProperty]] of Array
                  val (h_arr, ex) = lset_arr.foldLeft((HeapBot, ExceptionBot))((_hex, l) => {
                    // 3. s is length
                    val (h_length, ex_len) =
                      if (AbsString.alpha("length") <= s) {
                        val v_newLen = Value(Operator.ToUInt32(v_rhs))
                        val n_oldLen = h(l)("length")._1._1._1._1._4 // number
                        val b_g = (n_oldLen < v_newLen._1._4)
                        val b_eq = (n_oldLen === v_newLen._1._4)
                        val b_canputLen = Helper.CanPut(h, l, AbsString.alpha("length"))
                        // 3.d
                        val n_value = Helper.toNumber(v_rhs._1) + Helper.toNumber(Helper.objToPrimitive(v_rhs._2, "Number"))
                        val ex_len =
                          if (BFalse <= (n_value === v_newLen._1._4)) Set[Exception](RangeError)
                          else Set[Exception]()
                        val h_normal =
                          if (BTrue <= (n_value === v_newLen._1._4)) {
                            // 3.f
                          val h1 =
                            if ((BTrue <= b_g || BTrue <= b_eq) && BTrue <= b_canputLen)
                              Helper.PropStore(h, l, AbsString.alpha("length"), v_rhs)
                            else HeapBot
                          // 3.g
                          val h2 =
                            if (BFalse <= b_canputLen) h
                            else HeapBot
                          // 3.j, 3.l
                          val h3 =
                            if (BFalse <= b_g && BTrue <= b_canputLen) {
                              val _h1 = Helper.PropStore(h, l, AbsString.alpha("length"), v_rhs)
                              (AbsNumber.concretize(v_newLen._1._4), AbsNumber.concretize(n_oldLen)) match {
                                case (Some(n1), Some(n2)) =>
                                  (n1.toInt until n2.toInt).foldLeft(_h1)((__h, i) =>
                                    Helper.Delete(__h, l, AbsString.alpha(i.toString))._1)
                                case _ =>
                                  if (v_newLen._1._4 <= NumBot || n_oldLen <= NumBot)
                                    HeapBot
                                  else
                                    Helper.Delete(_h1, l, NumStr)._1
                              }
                            }
                            else HeapBot
                          h1 + h2 + h3
                          }
                          else
                            HeapBot
                        (h_normal, ex_len)
                      }
                      else
                        (HeapBot, ExceptionBot)
                    // 4. s is array index
                    val h_index =
                      if (BTrue <= Helper.IsArrayIndex(s)) {
                      val n_oldLen = h(l)("length")._1._1._1._1._4 // number
                      val n_index = Operator.ToUInt32(Value(Helper.toNumber(PValue(s))))
                      val b_g = (n_oldLen < n_index)
                      val b_eq = (n_oldLen === n_index)
                      val b_canputLen = Helper.CanPut(h, l, AbsString.alpha("length"))
                      // 4.b
                      val h1 =
                        if ((BTrue <= b_g || BTrue <= b_eq) && BFalse <= b_canputLen)  h
                        else HeapBot
                        // 4.c
                      val h2 =
                        if (BTrue <= (n_index < n_oldLen))  Helper.PropStore(h, l, s, v_rhs)
                        else HeapBot
                      // 4.e
                      val h3 =
                        if ((BTrue <= b_g || BTrue <= b_eq) && BTrue <= b_canputLen) {
                          val _h3 = Helper.PropStore(h, l, s, v_rhs)
                        val v_newIndex = Operator.bopPlus(Value(n_index), Value(AbsNumber.alpha(1)))
                        Helper.PropStore(_h3, l, AbsString.alpha("length"), v_newIndex)
                        }
                        else HeapBot
                      h1 + h2 + h3
                      }
                      else
                        HeapBot
                    // 5. other
                    val h_normal =
                      if (s != AbsString.alpha("length") && BFalse <= Helper.IsArrayIndex(s)) Helper.PropStore(h, l, s, v_rhs)
                      else HeapBot
                    (_hex._1 + h_length + h_index + h_normal, _hex._2 ++ ex_len)
                  })
                  (res._1 + h_cantput + h_narr + h_arr, res._2 ++ ex)
                })
                (h_2, ctx, es_2)
              }
            }
          }
          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es_1)
          ((h_1, ctx_1) , (he + h_e, ctxe + ctx_e))
        }

        case CFGFunExpr(_, _, lhs, None, fid, a_new1, a_new2, None) => {
          val l_r1 = addrToLoc(a_new1, Recent)
          val l_r2 = addrToLoc(a_new2, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, a_new1)
          val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, a_new2)
          val o_new = Helper.NewObject(ObjProtoLoc)
          val n = AbsNumber.alpha(cfg.getArgVars(fid).length)
          val fvalue = Value(PValueBot, LocSet(l_r1))
          val scope = h_2(SinglePureLocalLoc)("@env")._1._2._2
          val h_3 = h_2.update(l_r1, Helper.NewFunctionObject(fid, scope, l_r2, n))

          val pv = PropValue(ObjectValue(fvalue, BTrue, BFalse, BTrue))
          val h_4 = h_3.update(l_r2, o_new.update("constructor", pv))

          val h_5 = Helper.VarStore(h_4, lhs, fvalue)
          ((h_5, ctx_2), (he, ctxe))
        }
        case CFGFunExpr(_, _, lhs, Some(name), fid, a_new1, a_new2, Some(a_new3)) => {
          val l_r1 = addrToLoc(a_new1, Recent)
          val l_r2 = addrToLoc(a_new2, Recent)
          val l_r3 = addrToLoc(a_new3, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, a_new1)
          val (h_2, ctx_2) = Helper.Oldify(h_1, ctx_1, a_new2)
          val (h_3, ctx_3) = Helper.Oldify(h_2, ctx_2, a_new3)
          val o_new = Helper.NewObject(ObjProtoLoc)
          val n = AbsNumber.alpha(cfg.getArgVars(fid).length)
          val scope = h_3(SinglePureLocalLoc)("@env")._1._2._2
          val o_env = Helper.NewDeclEnvRecord(scope)
          val fvalue = Value(PValueBot, LocSet(l_r1))
          val h_4 = h_3.update(l_r1, Helper.NewFunctionObject(fid, LocSet(l_r3), l_r2, n))
          val h_5 = h_4.update(l_r2, o_new.update("constructor", PropValue(ObjectValue(fvalue, BTrue, BFalse, BTrue))))
          val h_6 = h_5.update(l_r3, o_env.update(name, PropValue(ObjectValue(fvalue, BFalse, BoolBot, BFalse))))
          val h_7 = Helper.VarStore(h_6, lhs, fvalue)
          ((h_7, ctx_3), (he, ctxe))
        }
        case CFGConstruct(_, _, cons, thisArg, arguments, a_new) => {
          // cons, thisArg and arguments must not be bottom
          val l_r = addrToLoc(a_new, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, a_new)
          val (v_1, es_1) = SE.V(cons, h_1, ctx_1)
          val lset = v_1._2
          val lset_f = lset.filter(l => BTrue <= Helper.HasConstruct(h_1,l))
          val lset_this = Helper.getThis(h_1, SE.V(thisArg, h_1, ctx_1)._1)
          val v_arg = SE.V(arguments, h_1, ctx_1)._1
          val o_old = h_1(SinglePureLocalLoc)
          val cc_caller = cp._2
          val n_aftercall = cfg.getAftercallFromCall(cp._1)
          val cp_aftercall = (n_aftercall, cc_caller)
          lset_f.foreach {l_f => {
            val o_f = h_1(l_f)
            val fids = o_f("@construct")._1._3
            fids.foreach {fid => {
              val ccset = cc_caller.NewCallContext(cfg, fid, l_r, lset_this)
              ccset.foreach {case (cc_new, o_new) => {
                val value = PropValue(ObjectValue(v_arg, BTrue, BFalse, BFalse))
                val o_new2 =
                  o_new.
                    update(cfg.getArgumentsName(fid), value).
                    update("@scope", o_f("@scope")._1)
                addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
                addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_1, o_old)
                addReturnEdge(((fid, LExitExc), cc_new), cp_aftercall, ctx_1, o_old)
              }}
            }}
          }}
          val h_2 = v_arg._2.foldLeft(HeapBot)((hh, l) => {
            val pv = PropValue(ObjectValue(Value(lset_f), BTrue, BFalse, BTrue))
            hh + h_1.update(l, h_1(l).update("callee", pv))
          })

          // exception handling
          val cond = lset.exists(l => BFalse <= Helper.HasConstruct(h_1,l))
          val es_2 =
            if (cond) Set(TypeError)
            else ExceptionBot
          val es_3 =
            if (v_1._1 </ PValueBot) Set(TypeError)
            else ExceptionBot

          val es = es_1 ++ es_2 ++ es_3
          val (h_e, ctx_e) = Helper.RaiseException(h_1, ctx_1, es)

          val s_1 = (he + h_e, ctxe + ctx_e)

          val h_3 =
            if (lset_f.isEmpty) HeapBot
            else h_2

          ((h_3, ctx_1), s_1)
        }
        case CFGCall(_, _, fun, thisArg, arguments, a_new) => {
          // cons, thisArg and arguments must not be bottom
          val l_r = addrToLoc(a_new, Recent)
          val (h_1, ctx_1) = Helper.Oldify(h, ctx, a_new)
          val (v_1, es_1) = SE.V(fun, h_1, ctx_1)
          val lset = v_1._2
          val lset_f = lset.filter(l => BTrue <= Helper.IsCallable(h_1,l))
          val lset_this = Helper.getThis(h_1, SE.V(thisArg, h_1, ctx_1)._1)
          val v_arg = SE.V(arguments, h_1, ctx_1)._1
          val o_old = h_1(SinglePureLocalLoc)
          val cc_caller = cp._2
          val n_aftercall = cfg.getAftercallFromCall(cp._1)
          val cp_aftercall = (n_aftercall, cc_caller)
          lset_f.foreach {l_f => {
            val o_f = h_1(l_f)
            val fids = o_f("@function")._1._3
            fids.foreach {fid => {
              val ccset = cc_caller.NewCallContext(cfg, fid, l_r, lset_this)
              ccset.foreach {case (cc_new, o_new) => {
                val value = PropValue(ObjectValue(v_arg, BTrue, BFalse, BFalse))
                val o_new2 =
                  o_new.
                    update(cfg.getArgumentsName(fid), value).
                    update("@scope", o_f("@scope")._1)
                addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
                addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_1, o_old)
                addReturnEdge(((fid, LExitExc), cc_new), cp_aftercall, ctx_1, o_old)
              }}
            }}
          }}
          val h_2 = v_arg._2.foldLeft(HeapBot)((hh, l) => {
            val pv = PropValue(ObjectValue(Value(lset_f), BTrue, BFalse, BTrue))
            hh + h_1.update(l, h_1(l).update("callee", pv))
          })

          // exception handling
          val cond = lset.exists(l => BFalse <= Helper.IsCallable(h_1,l))
          val es_2 =
            if (cond) {
              Set(TypeError)
            } else {
              ExceptionBot
            }
          val es_3 =
            if (v_1._1 </ PValueBot) {
              Set(TypeError)
            } else {
              ExceptionBot
            }
          val es = es_1 ++ es_2 ++ es_3
          val (h_e, ctx_e) = Helper.RaiseException(h_1, ctx_1, es)

          val s_1 = (he + h_e, ctxe + ctx_e)

          val h_3 =
            if (lset_f.isEmpty) HeapBot
            else h_2

          ((h_3, ctx_1), s_1)
        }
        /* Assert */
        case CFGAssert(_, info, expr, _) => {
          if(Config.assertMode)
            B(info, expr, h, ctx, he, ctxe)
          else
            ((h, ctx), (he, ctxe))
        }
        case CFGCatch(_, _, name) => {
          val v_old = h(SinglePureLocalLoc)("@exception_all")._1._2
          val h_1 = Helper.CreateMutableBinding(h, name, h(SinglePureLocalLoc)("@exception")._1._2)
          val new_obj = h_1(SinglePureLocalLoc).update("@exception", PropValue(v_old))
          val h_2 = h_1.update(SinglePureLocalLoc, new_obj)
          ((h_2, ctx), (HeapBot, ContextBot))
        }
        case CFGReturn(_, _, expr) => {
          val (v,es) =
            expr match {
              case Some(expr) => SE.V(expr, h, ctx)
              case None => (Value(UndefTop),Set[Exception]())
            }
          val (h_1, ctx_1) =
            if (v </ ValueBot) {
              (h.update(SinglePureLocalLoc, h(SinglePureLocalLoc).update("@return", PropValue(v))), ctx)
            } else {
              (HeapBot, ContextBot)
            }
          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
          ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
        }
        case CFGThrow(_, _, expr) => {
          val (v,es) = SE.V(expr,h,ctx)
          val v_old = h(SinglePureLocalLoc)("@exception_all")._1._2
          val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
          val new_obj =
            h(SinglePureLocalLoc).
              update("@exception", PropValue(v)).
              update("@exception_all", PropValue(v + v_old))
          val h_1 = h.update(SinglePureLocalLoc, new_obj)

          ((HeapBot, ContextBot), (h_1 + h_e, ctx + ctx_e))
        }
        case CFGBuiltinCall(_, fun, args, addr1, addr2, addr3, addr4) => {
          SemanticsBuiltin.builtinCall(this, h, ctx, he, ctxe, cp, cfg, fun, args, addr1, addr2, addr3, addr4)
        }
        case CFGDomApiCall(_, fun, args, addr1, addr2, addr3, addr4) => {
          SemanticsDOM.DOMCall(this, h, ctx, he, ctxe, cp, cfg, fun, args, addr1, addr2, addr3, addr4)
        }
        case CFGInternalCall(_, _, lhs, fun, arguments, loc) => {
          (fun.toString, arguments, loc)  match {
            case ("<>Global<>toObject", List(expr), Some(a_new)) => {
              val (v,es_1) = SE.V(expr, h, ctx)
              val (h_3, ctx_3, es_3) =
                if (v </ ValueBot) {
                  val (v_1, h_1, ctx_1, es_2) = Helper.toObject(h, ctx, v, a_new)
                  val (h_2, ctx_2) =
                    if (v_1 </ ValueBot) {
                      (Helper.VarStore(h_1, lhs, v_1), ctx_1)
                    } else {
                      (HeapBot, ContextBot)
                    }
                  (h_2, ctx_2, es_1 ++ es_2)
                } else {
                  (HeapBot, ContextBot, es_1)
                }
              val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es_3)
              ((h_3, ctx_3), (he + h_e, ctxe + ctx_e))
            }
            case ("<>Global<>isObject", List(expr), None) => {
              val (v,es) = SE.V(expr, h, ctx)
              val (h_1, ctx_1) =
                if (v </ ValueBot) {
                  val b_1 =
                    if (!v._2.isEmpty) BoolTrue
                    else BoolBot
                  val b_2 =
                    if (v._1 </ PValueBot) BoolFalse
                    else BoolBot
                  val b = b_1 + b_2
                  (Helper.VarStore(h, lhs, Value(b)), ctx)
                } else {
                  (HeapBot, ContextBot)
                }
              val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
              ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
            }
            case ("<>Global<>toNumber", List(expr), None) => {
              val (v,es) = SE.V(expr, h, ctx)
              val (h_1, ctx_1) =
                if (v </ ValueBot) {
                  val pv = Helper.toPrimitive(v)
                  (Helper.VarStore(h, lhs, Value(Helper.toNumber(pv))), ctx)
                } else {
                  (HeapBot, ContextBot)
                }
              val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
              ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
            }
            case ("<>Global<>toBoolean", List(expr), None) => {
              val (v,es) = SE.V(expr, h, ctx)
              val (h_1, ctx_1) =
                if (v </ ValueBot) {
                  (Helper.VarStore(h, lhs, Value(Helper.toBoolean(v))), ctx)
                } else {
                  (HeapBot, ContextBot)
                }
              val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
              ((h_1, ctx_1), (he + h_e, ctxe + ctx_e))
            }
            case ("<>Global<>getBase", List(expr_2), None) => {
              val x_2 = expr_2.asInstanceOf[CFGVarRef].id
              val lset_base = Helper.LookupBase(h, x_2)
              ((Helper.VarStore(h, lhs, Value(lset_base)), ctx), (he, ctxe))
            }
            case ("<>Global<>iteratorInit", List(expr), None) => {
              ((h, ctx), (he, ctxe))
            }
            case ("<>Global<>iteratorHasNext", List(expr_2, expr_3), None) => {
              ((Helper.VarStore(h, lhs, Value(PValue(BoolTop))), ctx), (he, ctxe))
            }
            case ("<>Global<>iteratorNext", List(expr_2, expr_3), None) => {
              ((Helper.VarStore(h, lhs, Value(PValue(StrTop))), ctx), (he, ctxe))
            }
            case _ => {
              System.out.println(fun.toString)
              throw new NotYetImplemented()
            }
          }
        }
        case CFGNoOp(_, _, _) => {
          ((h, ctx), (he, ctxe))
        }
      }
      s
    }
  }


  // Inter-procedural edge set.
  // These edges are added while processing call instruction.
  val ipSuccMap: MMap[ControlPoint, MMap[ControlPoint, (Context,Obj)]] = MHashMap()
  def getIPSucc(cp: ControlPoint): Option[MMap[ControlPoint, (Context,Obj)]] = ipSuccMap.get(cp)

  // Adds inter-procedural call edge from call-node cp1 to entry-node cp2.
  // Edge label ctx records callee context, which is joined if the edge existed already.
  def addCallEdge(cp1: ControlPoint, cp2: ControlPoint, ctx: Context, obj: Obj) = {
    ipSuccMap.get(cp1) match {
      case None =>
        ipSuccMap.update(cp1, MHashMap((cp2, (ctx, obj))))
      case Some(map2) =>
        map2.get(cp2) match {
          case None =>
            map2.update(cp2, (ctx, obj))
          case Some((old_ctx, old_obj)) =>
            map2.update(cp2, (old_ctx + ctx, old_obj + obj))
        }
    }
  }

  // Adds inter-procedural return edge from exit or exit-exc node cp1 to after-call node cp2.
  // Edge label ctx records caller context, which is joined if the edge existed already.
  // If change occurs, cp1 is added to worklist as side-effect.
  def addReturnEdge(cp1: ControlPoint, cp2: ControlPoint, ctx: Context, obj: Obj) = {
    ipSuccMap.get(cp1) match {
      case None =>
        ipSuccMap.update(cp1, MHashMap((cp2, (ctx, obj))))
        worklist.add(cp1)
      case Some(map2) =>
        map2.get(cp2) match {
          case None =>
            map2.update(cp2, (ctx, obj))
            worklist.add(cp1)
          case Some((old_ctx, old_obj)) =>
            var changed = false
            val new_ctx =
              if (ctx <= old_ctx) old_ctx
              else {
                changed = true
                old_ctx + ctx
              }
            val new_obj =
              if (obj <= old_obj) old_obj
              else {
                changed = true
                old_obj + obj
              }
            if (changed) {
              map2.update(cp2, (new_ctx, new_obj))
              worklist.add(cp1)
            }
        }
    }
  }

  val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGSemantics"))
  // Assert semantics
  def B(info:Info, expr: CFGExpr, h: Heap, ctx: Context, he: Heap, ctxe: Context) = {
    val relSet = expr match {
      case CFGBin(info, first, op, second) if AH.isRelationalOperator(op) =>
        getRel(expr, State(h, ctx)) ++ getRel(CFGBin(info, second, AH.reflectiveIROp(op), first), State(h, ctx))
      case CFGBin(info, first, op, second) if AH.isObjectOperator(op) =>
        HashSet(RelExpr(first, op, second))
      case _ => getRel(expr, State(h, ctx))
    }

    // transform notIn and notInstanceof to ! in ! instanceof to evaluate them.
    val (v, es) = expr match{
      case CFGBin(info, first, op, second) if op.getKind == EJSOp.BIN_COMP_REL_NOTIN =>
        SE.V(CFGUn(info, IRFactory.makeOp("!"), CFGBin(info, first, IRFactory.makeOp("in"), second)), h, ctx)
      case CFGBin(info, first, op, second) if op.getKind == EJSOp.BIN_COMP_REL_NOTINSTANCEOF =>
        SE.V(CFGUn(info, IRFactory.makeOp("!"), CFGBin(info, first, IRFactory.makeOp("instanceof"), second)), h, ctx)
      case _ =>
        SE.V(expr, h, ctx)
    }

    val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)
    if(BoolTrue <= Helper.toBoolean(v)) {
      (relSet.foldLeft((h, ctx))((s12, re) => {
        val ((h_b, ctx_b)) = s12
        val ((h_a, ctx_a)) = X(re, h, ctx)
        (h_b <> h_a, ctx_b <> ctx_a)
      }), (he + h_e, ctxe + ctx_e))
    }
    else
      ((HeapBot, ContextBot), (he + h_e, ctxe + ctx_e))
  }

  def X(re: RelExpr, h: Heap, ctx: Context):(Heap, Context) = {
    re match {
      case RelExpr(first, op, second) if AH.isRelationalOperator(op) =>
        val v1 = SE.V(first, h, ctx)._1
        val v2 = SE.V(second, h, ctx)._1
        Pruning(first match {
          case id@CFGVarRef(_, _) => Id(id)
          case prop@CFGLoad(_, _, _) => Prop(prop)
          case _ => throw new InternalError("e1 of RelExpr must be a PrunExpression")
        }, v1, op, v2, h, ctx)
      case RelExpr(first, op, second) if AH.isObjectOperator(op) =>
        Pruning(re, h, ctx)
      case _ => (h, ctx)
    }
  }

  def Pruning(re: RelExpr, h: Heap, ctx: Context):(Heap, Context) = {
    val (e1, op, e2) = re match {
      case RelExpr(first, op, second) => (first, op, second)
    }
    val (v1, v2) = ((SE.V(e1, h, ctx))._1, (SE.V(e2, h, ctx))._1)
    val s = Helper.toString(Helper.toPrimitive(v1))
    val L_base = op.getKind match {
      case EJSOp.BIN_COMP_REL_IN =>
        (v2._2).foldLeft(LocSetBot)((l_set, l) => l_set ++ Helper.ProtoBase(h, l, s))
      case EJSOp.BIN_COMP_REL_NOTIN =>
        v2._2
      case EJSOp.BIN_COMP_REL_INSTANCEOF =>
        v2._2
      case EJSOp.BIN_COMP_REL_NOTINSTANCEOF =>
        v2._2
      case _ => throw new InternalError("Pruning2 function receives only object operators")
    }

    L_base.size match {
      case 1 => (op.getKind match {
        case EJSOp.BIN_COMP_REL_IN => s match {
          case NumStrSingle(x) =>
              // make property definitely exist
              h.update(L_base.head, h(L_base.head).update(s, (h(L_base.head)(s))._1))
          case OtherStrSingle(x) =>
              h.update(L_base.head, h(L_base.head).update(s, (h(L_base.head)(s))._1))
          case _ => h
        }
        case EJSOp.BIN_COMP_REL_NOTIN => s match {
          case NumStrSingle(x) =>
              // remove property. it may be definitely absent or possibly absent(when default value exist)
              AH.DeleteAll(h, L_base.head, s)
          case OtherStrSingle(x) =>
              AH.DeleteAll(h, L_base.head, s)
          case _ => h
        }
        case EJSOp.BIN_COMP_REL_INSTANCEOF if ((v1._2).size == 1) =>
              AH.PruneInstanceof((v1._2).head, L_base.head, BoolTrue, h)
        case EJSOp.BIN_COMP_REL_NOTINSTANCEOF if ((v1._2).size == 1) =>
              AH.PruneInstanceof((v1._2).head, L_base.head, BoolFalse, h)
        case _ => h
      }, ctx)
      case _ => (h, ctx)
    }
  }



/*
  def X(relSet: Set[RelExpr], h: Heap, ctx: Context):(Heap, Context) = {
    relSet.foldLeft((h, ctx))((s12, re) => {
      val ((h_f, ctx_f)) = s12
      re match {
        case RelExpr(first: PrunExpr, op: IROp, second: CFGExpr) =>
          val (v1, es1) = SE.V(first.get, h, ctx)
          val (v2, es2) = SE.V(second, h, ctx)
          val (h_e1, ctx_e1) = Helper.RaiseException(h, ctx, es1)
          val (h_e2, ctx_e2) = Helper.RaiseException(h, ctx, es2)
          val (h_e, ctx_e) = (h_e1 + h_e2, ctx_e1 + ctx_e2)
          val (h1, ctx1) = Pruning(first, v1, op, v2, h, ctx)
          (h_f <> h1, ctx_f <> ctx1)
      }
    })
  }
*/

  def Pruning(pe: PrunExpr, v1: Value, op: IROp, v2: Value, h: Heap, ctx: Context):(Heap, Context) = {
    val (l_loc, s) = pe match {
      case Id(id: CFGVarRef) =>
        (LocSetBot, AbsString.alpha(id.toString))
      case Prop(CFGLoad(info: Info, obj: CFGExpr, index: CFGExpr)) =>
        (SE.V(obj, h, ctx)._1._2, Helper.toString(Helper.toPrimitive(SE.V(index, h, ctx)._1)))
    }
    val L_base = pe match {
      case Id(CFGVarRef(_, id)) => Helper.LookupBase(h, id)
      case Prop(_) => l_loc.foldLeft(LocSetBot)((lset, l) => lset ++ Helper.ProtoBase(h, l, s))
    }

//    System.out.println("L_BASE : start from " + l_loc.toString + " with String : " + s.toString +" size = " + L_base.size)
//    L_base.foldLeft(())((x, l) => System.out.println(l))

    L_base.size match {
      case 1 =>
        s match {
          case NumStrSingle(x) =>
            val ov = h(L_base.head)(s)._1._1
            val (v, abs) = AH.K(op, v2, v1._2)
            val propv = PropValue(ObjectValue(v <> v1, ov._2, ov._3, ov._4), h(L_base.head)(s)._1._2, h(L_base.head)(s)._1._3)
            (h.update(L_base.head, h(L_base.head).update(x, PropValue(ObjectValue(v,ov._2,ov._3,ov._4)), abs <> h(L_base.head)(s)._2)), ctx)
          case OtherStrSingle(x) =>
            val ov = h(L_base.head)(s)._1._1
            val (v, abs) = AH.K(op, v2, v1._2)
            val propv = PropValue(ObjectValue(v <> v1, ov._2, ov._3, ov._4), h(L_base.head)(s)._1._2, h(L_base.head)(s)._1._3)
            (h.update(L_base.head, h(L_base.head).update(x, PropValue(ObjectValue(v,ov._2,ov._3,ov._4)), abs <> h(L_base.head)(s)._2)), ctx)
          case _ => (h, ctx)
        }
      case _ => (h, ctx)
    }
  }
  // get Relational Expressions
  def getRel(expr: CFGExpr, s: State): Set[RelExpr] = {
    expr match {
      case CFGBin(info, first, op, second) =>
        first match {
          // $e <> e'
          case id@CFGVarRef(_, _: CFGUserId) => HashSet(RelExpr(first, op, second))
          case prop@CFGLoad(_, _, _) => HashSet(RelExpr(first, op, second))
          case CFGBin(inInfo, e1, op1, e2) if validity(e1, e2, second, s) =>
            op1.getKind match {
              // (e1 + e2) <> second
              case EJSOp.ETC_PLUS => getRel(CFGBin(info, e1, op, CFGBin(dummyInfo, second, IRFactory.makeOp("-"), e2)), s) ++
                                     getRel(CFGBin(info, e2, op, CFGBin(dummyInfo, second, IRFactory.makeOp("-"), e1)), s)
              // (e1 - e2) <> second
              case EJSOp.ETC_MINUS => getRel(CFGBin(info, e1, op, CFGBin(dummyInfo, second, IRFactory.makeOp("+"), e2)), s) ++
                                      getRel(CFGBin(info, e2, AH.reflectiveIROp(op), CFGBin(dummyInfo, second, IRFactory.makeOp("-"), e1)), s)
              // (n * e2) <> second
              case EJSOp.BIN_ARITH_MUL_MULTIPLICATION if e1.isInstanceOf[CFGNumber] =>
                getRel(CFGBin(info, CFGBin(inInfo, e2, op1, e1), op, second), s)
              // (e1 * n) <> second and n > 0
              case EJSOp.BIN_ARITH_MUL_MULTIPLICATION if (e2.isInstanceOf[CFGNumber] && e2.asInstanceOf[CFGNumber].toNumber > 0) =>
                getRel(CFGBin(info, e1, op, CFGBin(dummyInfo, second, IRFactory.makeOp("/"), e2)), s)
              // (e1 * n) <> second and n < 0
              case EJSOp.BIN_ARITH_MUL_MULTIPLICATION if (e2.isInstanceOf[CFGNumber] && e2.asInstanceOf[CFGNumber].toNumber < 0) =>
                getRel(CFGBin(info, e1, AH.reflectiveIROp(op), CFGBin(dummyInfo, second, IRFactory.makeOp("/"), e2)), s)
              // (e1 / n) <> second and n > 0
              case EJSOp.BIN_ARITH_MUL_DIVISION if (e2.isInstanceOf[CFGNumber] && e2.asInstanceOf[CFGNumber].toNumber > 0) =>
                getRel(CFGBin(info, e1, op, CFGBin(dummyInfo, second, IRFactory.makeOp("*"), e2)), s)
              // (e1 / n) <> second and n < 0
              case EJSOp.BIN_ARITH_MUL_DIVISION if (e2.isInstanceOf[CFGNumber] && e2.asInstanceOf[CFGNumber].toNumber > 0) =>
                getRel(CFGBin(info, e1, AH.reflectiveIROp(op), CFGBin(dummyInfo, second, IRFactory.makeOp("*"), e2)), s)
              case _ => HashSet[RelExpr]()
            }
          case _ => HashSet[RelExpr]()
        }
      case _ => HashSet[RelExpr]()
    }
  }
  def validity(expr: CFGExpr, s:State):Boolean = {
    val (h, ctx) = (s._1, s._2)
    val v = SE.V(expr, h, ctx)._1
    if (v._1._1 <= UndefBot && v._1._2 <= NullBot && (v._1._4 <= UInt || v._1._4 <= NUInt) &&
        v._1._5 <= StrBot && v._2.isEmpty)
      true
    else
      false
  }
  def validity(expr1: CFGExpr, expr2: CFGExpr, s:State):Boolean = {
    validity( expr1, s) && validity(expr2, s)
  }
  def validity(expr1: CFGExpr, expr2: CFGExpr, expr3: CFGExpr, s:State):Boolean = {
    validity(expr1, s) && validity(expr2, s) && validity(expr3, s)
  }


  // E function for Preanalysis
  def PreE(cp1: ControlPoint, cp2: ControlPoint, ctx: Context, obj: Obj, s: State): State = {
    cp2 match {
      case ((_, LEntry),_) =>
        // System.out.println("== "+cp1 +" -> "+cp2+" ==")
        // System.out.println(DomainPrinter.printHeap(4, s._1))
        // System.out.println("== Object ==")
        // System.out.println(DomainPrinter.printObj(4, obj))
        // call edge
        if (s._1 == HeapBot) StateBot
        else {
          // make new decl env
          val env_obj = PreHelper.NewDeclEnvRecord(obj("@scope")._1._2._2)
          // obj2 is new PureLocal
          val obj2 = obj // - "@scope"
          val h1 = s._1
          // weak update PureLocal
          val h2 = h1.update(cfg.getPureLocal(cp2), obj2)
          // add new env rec to direct l_env to env_obj
          val h3 = obj2("@env")._1._2._2.foldLeft(h2)((hh, l_env) => {
            hh.update(l_env, env_obj)
          })
          State(h3, ctx + s._2)
        }

      case _ => cp1 match {
        case ((_, LExit),_) =>
          // System.out.println("== "+cp1 +" -> "+cp2+" ==")
          // System.out.println(DomainPrinter.printHeap(4, s._1))
          // System.out.println("== Object ==")
          // System.out.println(DomainPrinter.printObj(4, obj))
          // exit return edge
          if (s._1 == HeapBot) StateBot
          else {
            val returnVar = cfg.getReturnVar(cp2._1) match {
              case Some(x) => x
              case None => throw new InternalError("After-call node must have return variable")
            }
            val h1 = s._1
            val ctx1 = s._2
            val v = h1(cfg.getPureLocal(cp1))("@return")._1._2
            val (ctx2, obj1) = PreHelper.FixOldify(ctx + ctx1, obj, ctx1._3, ctx1._4)
            val h2 = h1.update(cfg.getPureLocal(cp2), obj1)
            val h3 = PreHelper.VarStore(h2, cfg.getPureLocal(cp2), returnVar, v)

            // System.out.println("===result===")
            // System.out.println(DomainPrinter.printHeap(4, h3))
            State(h3, ctx2)
          }

        case ((_, LExitExc),_) =>
          // exit-exc return edge
          if (s._1 == HeapBot) StateBot
          else {
            val h1 = s._1
            val ctx1 = s._2
            val v = h1(cfg.getPureLocal(cp1))("@exception")._1._2
            val (ctx2, obj1) = PreHelper.FixOldify(ctx + ctx1, obj, ctx1._3, ctx1._4)
            val v_old = obj1("@exception_all")._1._2
            val h2 = h1.update(cfg.getPureLocal(cp2),
                               obj1.update("@exception", PropValue(v)).
                                    update("@exception_all", PropValue(v + v_old)))
            State(h2, ctx2)
          }

        case _ => throw new InternalError("Inter-procedural edge must be call or return edge.")
      }
    }
  }

  // C function for Preanalysis
  def PreC(cp: ControlPoint, c: Cmd, s: State): State = {
    val h = s._1
    val ctx = s._2
    val PureLocalLoc = cfg.getPureLocal(cp)
    /*
    if (h <= HeapBot)
      StateBot, StateBot)
    else {
    */
      val (h_1, ctx_1) = c match {
        case Entry =>
          val (fid, l) = cp._1
          val x_argvars = cfg.getArgVars(fid)
          val x_localvars = cfg.getLocalVars(fid)
          val lset_arg = h(PureLocalLoc)(cfg.getArgumentsName(fid))._1._1._1._2
          var i = 0
          val h_n = x_argvars.foldLeft(h)((hh, x) => {
            val v_i = lset_arg.foldLeft(ValueBot)((vv, l_arg) => {
              vv + PreHelper.Proto(hh, l_arg, AbsString.alpha(i.toString))
            })
            i = i + 1
            PreHelper.CreateMutableBinding(hh, PureLocalLoc, x, v_i)
          })
          val h_m = x_localvars.foldLeft(h_n)((hh, x) => {
            PreHelper.CreateMutableBinding(hh, PureLocalLoc, x, Value(UndefTop))
          })
          (h_m, ctx)

        case Exit => (h, ctx)
        case ExitExc => (h, ctx)

        case Block(insts) =>
          insts.foldLeft((h, ctx))(
            (states, inst) => {
/*
                      System.out.println("***************************************************************************")
                      System.out.println("===========================  Before ===============================")
                      System.out.println("- Instr : " + inst)
                      System.out.print("- Context " + " = ")
                      System.out.println(DomainPrinter.printContext(0, states._2))
                      System.out.println("- Heap " )
                      System.out.println(DomainPrinter.printHeap(4, states._1))
*/
              val (h_1, ctx_1) = PreI(cp, inst, states._1, states._2)
              // if( !(State(states._1, states._2) <= State(h_1, ctx_1)) ) {
              //       System.out.println("***************************************************************************")
              //       System.out.println("===========================  Before instr State  ==========================")
              //       System.out.println("- Instr : " + inst)
              //       System.out.print("- Context " + " = ")
              //       System.out.println(DomainPrinter.printContext(0, states._2))
              //       System.out.println("- Heap " )
              //       System.out.println(DomainPrinter.printHeap(4, states._1))

              //       System.out.println("=========================================================================")
              //       System.out.println()
              //       System.out.println("===========================  Current instr State  ========================")
              //       System.out.println("- Instr : " + inst)
              //       System.out.print("- Context " + " = ")
              //       System.out.println(DomainPrinter.printContext(0, ctx_1))
              //       System.out.println("- Heap " )
              //       System.out.println(DomainPrinter.printHeap(4, h_1))
              //       System.out.println("=========================================================================")
              //       System.out.println()
              // }
              (h_1, ctx_1)
            })
      }
      State(h_1, ctx_1)
    // }
  }

  // I function for preanalysis
  def PreI(cp: ControlPoint, i: CFGInst, h: Heap, ctx: Context): (Heap, Context)= {
    // System.out.println("\nInstruction: "+i)
    // System.out.println("in heap#####\n" + DomainPrinter.printHeap(4, h))

    val PureLocalLoc = cfg.getPureLocal(cp)
    val s = i match {
      case CFGAlloc(_, _, x, e, a_new) => {
        val l_r = addrToLoc(a_new, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, a_new)
        val (ls_v, es) = e match {
          case None => (ObjProtoSingleton, ExceptionBot)
          case Some(proto) => {
            val (v,es_) = PSE.V(proto, h_1, ctx_1, PureLocalLoc)
            if (v._1 </ PValueBot) {
              (v._2 ++ ObjProtoSingleton, es_)
            } else {
              (v._2, es_)
            }
          }
        }
        val h_2 = PreHelper.allocObject(h_1, ls_v, l_r)
        val h_3 = PreHelper.VarStore(h_2, PureLocalLoc, x, Value(l_r))
        val (h_e, ctx_e) = PreHelper.RaiseException(h_3, ctx_1, PureLocalLoc, es)

          (h_e, ctx_e)
      }
      case CFGAllocArray(_, _, x, n, a_new) => {
        val l_r = addrToLoc(a_new, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, a_new)
        val np = AbsNumber.alpha(n.toInt)
        val h_2 = h_1.update(l_r, PreHelper.NewArrayObject(np))
        val h_3 = PreHelper.VarStore(h_2, PureLocalLoc, x, Value(l_r))
          (h_3, ctx_1)
      }
      case CFGAllocArg(_, _, x, n, a_new) => {
        val l_r = addrToLoc(a_new, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, a_new)
        val np = AbsNumber.alpha(n.toInt)
        val h_2 = h_1.update(l_r, PreHelper.NewArgObject(np))
        val h_3 = PreHelper.VarStore(h_2, PureLocalLoc, x, Value(l_r))
          (h_3, ctx_1)
      }
      case CFGExprStmt(_, _, x, e) => {
        val (v,es) = PSE.V(e, h, ctx, PureLocalLoc)
        val (h_1, ctx_1) =
          if (v </ ValueBot) {
            (PreHelper.VarStore(h, PureLocalLoc, x, v), ctx)
          } else {
            (h, ctx)
          }
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx, PureLocalLoc, es)
          (h_e, ctx_e)
      }
      case CFGDelete(_, _, lhs, expr) =>
        expr match {
          case CFGVarRef(_, id) => {
            val lset_base = PreHelper.LookupBase(h, PureLocalLoc, id)
            val (h_1, b) =
              if (lset_base.isEmpty) (h, BoolTrue)
              else {
                val abs_id = AbsString.alpha(id.toString)
                lset_base.foldLeft[(Heap,AbsBool)](h, BoolBot)((v, l_base) => {
                  val (h_d, b_d) = PreHelper.Delete(v._1, l_base, abs_id)
                  (h_d, v._2 + b_d)
                })
              }
            val h_2 = PreHelper.VarStore(h_1, PureLocalLoc, lhs, Value(b))
              (h_2, ctx)
          }

          case _ => {
            val (v, es) = PSE.V(expr, h, ctx, PureLocalLoc)
            val (h_1, ctx_1) =
              if (v </ ValueBot)
                (PreHelper.VarStore(h, PureLocalLoc, lhs, Value(BoolTrue)), ctx)
              else
                (h, ctx)
            val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx_1, PureLocalLoc, es)
              (h_e, ctx_e)
          }
        }

      case CFGDeleteProp(_, _, lhs, obj, index) => {
        val (v_index, es) = PSE.V(index, h, ctx, PureLocalLoc)
        val (h_2, ctx_2) =
          if (v_index <= ValueBot) (h, ctx)
          else {
            // lset must not be empty because obj is coming through <>toObject.
            val lset = PSE.V(obj, h, ctx, PureLocalLoc)._1._2

            val sset = PreHelper.toStringSet(PreHelper.toPrimitive(v_index))
            val (h_1, b) = lset.foldLeft[(Heap, AbsBool)](h, BoolBot)((res1, l) => {
              sset.foldLeft(res1)((res2, s) => {
                val (h_, b_) = PreHelper.Delete(res2._1, l, s)
                (h_, res2._2 + b_)
              })
            })
            (PreHelper.VarStore(h_1, PureLocalLoc, lhs, Value(b)), ctx)
          }
        val (h_e, ctx_e) = PreHelper.RaiseException(h_2, ctx_2, PureLocalLoc, es)
        (h_e, ctx_e)
      }

      case CFGStore(_, _, obj, index, rhs) => {
        // TODO: toStringSet should be used in more optimized way
        // TODO: optimize. lset seems very big.
        val (h_1, ctx_1, es_1) = {
          val (v_index, es_index) = PSE.V(index, h, ctx, PureLocalLoc)
          if (v_index <= ValueBot) (h, ctx, es_index)
          else {
            val (v_rhs, es_rhs) = PSE.V(rhs, h, ctx, PureLocalLoc)
            if (v_rhs <= ValueBot) (h, ctx, es_index ++ es_rhs)
            else {
              // lset must not be empty because obj is coming through <>toObject.
              val lset = PSE.V(obj, h, ctx, PureLocalLoc)._1._2

              // iterate over set of strings for index
              val sset = PreHelper.toStringSet(PreHelper.toPrimitive(v_index))
              val (h_2, es_2) = sset.foldLeft((h, es_index ++ es_rhs))((res, s) => {
                // non-array objects
                val lset_narr = lset.filter(l => (BFalse <= PreHelper.IsArray(h, l)) && BTrue <= PreHelper.CanPut(h, l, s))
                // array objects
                val lset_arr = lset.filter(l => (BTrue <= PreHelper.IsArray(h, l)) && BTrue <= PreHelper.CanPut(h, l, s))
                // store for non-array object
                val h_narr = lset_narr.foldLeft(h)((_h, l) => _h + PreHelper.PropStore(h, l, s, v_rhs))
                // 15.4.5.1 [[DefineOwnProperty]] of Array
                val (h_arr, ex) = lset_arr.foldLeft((h, ExceptionBot))((_hex, l) => {
                  // 3. s is length
                  val (h_length, ex_len) =
                    if (AbsString.alpha("length") <= s) {
                      val v_newLen = Value(Operator.ToUInt32(v_rhs))
                      val n_oldLen = h(l)("length")._1._1._1._1._4 // number
                      val b_g = (n_oldLen < v_newLen._1._4)
                      val b_eq = (n_oldLen === v_newLen._1._4)
                      val b_canputLen = PreHelper.CanPut(h, l, AbsString.alpha("length"))
                      // 3.d
                      val n_value = PreHelper.toNumber(v_rhs._1) + PreHelper.toNumber(PreHelper.objToPrimitive(v_rhs._2, "Number"))
                      val ex_len =
                        if (BFalse <= (n_value === v_newLen._1._4)) Set[Exception](RangeError)
                        else Set[Exception]()
                      val h_normal =
                        if (BTrue <= (n_value === v_newLen._1._4)) {
                          // 3.f
                        val h1 =
                          if ((BTrue <= b_g || BTrue <= b_eq) && BTrue <= b_canputLen)
                            PreHelper.PropStore(h, l, AbsString.alpha("length"), v_rhs)
                          else h
                        // 3.g
                        val h2 =
                          if (BFalse <= b_canputLen) h
                          else h
                        // 3.j, 3.l
                        val h3 =
                          if (BFalse <= b_g && BTrue <= b_canputLen) {
                            val _h1 = PreHelper.PropStore(h, l, AbsString.alpha("length"), v_rhs)
                            (AbsNumber.concretize(v_newLen._1._4), AbsNumber.concretize(n_oldLen)) match {
                              case (Some(n1), Some(n2)) =>
                                (n1.toInt until n2.toInt).foldLeft(_h1)((__h, i) =>
                                  PreHelper.Delete(__h, l, AbsString.alpha(i.toString))._1)
                              case _ =>
                                if (v_newLen._1._4 <= NumBot || n_oldLen <= NumBot)
                                  h
                                else
                                  PreHelper.Delete(_h1, l, NumStr)._1
                            }
                          }
                          else h
                        h1 + h2 + h3
                        }
                        else
                          h
                      (h_normal, ex_len)
                    }
                    else
                      (h, ExceptionBot)
                  // 4. s is array index
                  val h_index =
                    if (BTrue <= PreHelper.IsArrayIndex(s)) {
                    val n_oldLen = h(l)("length")._1._1._1._1._4 // number
                    val n_index = Operator.ToUInt32(Value(PreHelper.toNumber(PValue(s))))
                    val b_g = (n_oldLen < n_index)
                    val b_eq = (n_oldLen === n_index)
                    val b_canputLen = PreHelper.CanPut(h, l, AbsString.alpha("length"))
                    // 4.b
                    val h1 =
                      if ((BTrue <= b_g || BTrue <= b_eq) && BFalse <= b_canputLen)  h
                      else h
                    val h2 =
                      if (BTrue <= b_canputLen) {
                        // 4.c
                        val __h1 = PreHelper.PropStore(h, l, s, v_rhs)
                        // 4.e
                        if (BTrue <= b_g || BTrue <= b_eq) {
                        val v_newIndex = Operator.bopPlus(Value(n_index), Value(AbsNumber.alpha(1)))
                        PreHelper.PropStore(__h1, l, AbsString.alpha("length"), v_newIndex)
                        }
                        else __h1
                      }
                      else h
                    h1 + h2
                    }
                    else
                      h
                  // 5. other
                  val h_normal =
                    if (s != AbsString.alpha("length") && BFalse <= PreHelper.IsArrayIndex(s)) PreHelper.PropStore(h, l, s, v_rhs)
                    else h
                  (_hex._1 + h_length + h_index + h_normal, _hex._2 ++ ex_len)
                })
                (res._1 + h_narr + h_arr, res._2 ++ ex)
              })
              (h_2, ctx, es_2)
            }
          }
        }
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx_1, PureLocalLoc, es_1)
        (h_e, ctx_e)
      }

      case CFGFunExpr(_, _, lhs, None, fid, a_new1, a_new2, None) => {
        val l_r1 = addrToLoc(a_new1, Recent)
        val l_r2 = addrToLoc(a_new2, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, a_new1)
        val (h_2, ctx_2) = PreHelper.Oldify(h_1, ctx_1, a_new2)
        val o_new = PreHelper.NewObject(ObjProtoLoc)
        val n = AbsNumber.alpha(cfg.getArgVars(fid).length)
        val fvalue = Value(PValueBot, LocSet(l_r1))
        val scope = h_2(PureLocalLoc)("@env")._1._2._2
        val h_3 = h_2.update(l_r1, PreHelper.NewFunctionObject(fid, scope, l_r2, n))
        val h_4 = h_3.update(l_r2, o_new.update("constructor",
          PropValue(ObjectValue(fvalue, BoolTrue, BoolFalse, BoolTrue))))
        val h_5 = PreHelper.VarStore(h_4, PureLocalLoc, lhs, fvalue)
        (h_5, ctx_2)
      }
      case CFGFunExpr(_, _, lhs, Some(name), fid, a_new1, a_new2, Some(a_new3)) => {
        val l_r1 = addrToLoc(a_new1, Recent)
        val l_r2 = addrToLoc(a_new2, Recent)
        val l_r3 = addrToLoc(a_new3, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, a_new1)
        val (h_2, ctx_2) = PreHelper.Oldify(h_1, ctx_1, a_new2)
        val (h_3, ctx_3) = PreHelper.Oldify(h_2, ctx_2, a_new3)
        val o_new = PreHelper.NewObject(ObjProtoLoc)
        val n = AbsNumber.alpha(cfg.getArgVars(fid).length)
        val scope = h_3(PureLocalLoc)("@env")._1._2._2
        val o_env = PreHelper.NewDeclEnvRecord(scope)
        val fvalue = Value(PValueBot, LocSet(l_r1))
        val h_4 = h_3.update(l_r1, PreHelper.NewFunctionObject(fid, LocSet(l_r3), l_r2, n))
        val h_5 = h_4.update(l_r2, o_new.update("constructor",
          PropValue(ObjectValue(fvalue, BoolTrue, BoolFalse, BoolTrue))))
        val h_6 = h_5.update(l_r3, o_env.update(name,
          PropValue(ObjectValue(fvalue, BoolFalse, BoolBot, BoolFalse))))
        val h_7 = PreHelper.VarStore(h_6, PureLocalLoc, lhs, fvalue)
        (h_7, ctx_3)
      }
      case CFGConstruct(_, _, cons, thisArg, arguments, a_new) => {
        // cons, thisArg and arguments must not be bottom
        val l_r = addrToLoc(a_new, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, a_new)
        val (v_1, es_1) = PSE.V(cons, h_1, ctx_1, PureLocalLoc)
        val lset = v_1._2
        val lset_f = lset.filter((l) => BoolTrue <= PreHelper.HasConstruct(h_1,l))
        val lset_this = PreHelper.getThis(h_1, PSE.V(thisArg, h_1, ctx_1, PureLocalLoc)._1)
        // val lset_this = PreHelper.getThis(h_1, SE.V(thisArg, h_1, ctx_1, PureLocalLoc)._1) ++
        //                 h(PureLocalLoc)("@this")._1._2._2 // set this value with current this value
        val v_arg = PSE.V(arguments, h_1, ctx_1, PureLocalLoc)._1
        val o_old = h_1(PureLocalLoc)
        val cc_caller = cp._2
        val n_aftercall = cfg.getAftercallFromCall(cp._1)
        val cp_aftercall = (n_aftercall, cc_caller)
        lset_f.foreach((l_f) => {
          val o_f = h_1(l_f)
          o_f("@construct")._1._3.foreach((fid) => {
            cc_caller.NewCallContext(cfg, fid, l_r, lset_this).foreach((pair) => {
              val (cc_new, o_new) = pair
              val o_new2 = o_new.
                update(cfg.getArgumentsName(fid),
                       PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))).
                update("@scope", o_f("@scope")._1)

              addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
              addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_1, o_old)
              addReturnEdge(((fid, LExitExc), cc_new), cp_aftercall, ctx_1, o_old)
            })
          })
        })
        val h_2 = v_arg._2.foldLeft(h_1)((hh, l) => {
          hh.update(l, hh(l).update("callee",
            PropValue(ObjectValue(Value(lset_f), BoolTrue, BoolFalse, BoolTrue))))
        })

        // exception handling
        val cond = lset.exists((l) => BoolFalse <= PreHelper.HasConstruct(h_1,l))
        val es_2 =
          if (cond) {
            Set(TypeError)
          } else {
            ExceptionBot
          }
        val es_3 =
          if (v_1._1 </ PValueBot) {
            Set(TypeError)
          } else {
            ExceptionBot
          }
        val es = es_1 ++ es_2 ++ es_3
        val (h_e, ctx_e) = PreHelper.RaiseException(h_2, ctx_1, PureLocalLoc, es)
        (h_e, ctx_e)
      }
      case CFGCall(_, _, fun, thisArg, arguments, a_new) => {
        // cons, thisArg and arguments must not be bottom
        val l_r = addrToLoc(a_new, Recent)
        val (h_1, ctx_1) = PreHelper.Oldify(h, ctx, a_new)
        val (v_1, es_1) = PSE.V(fun, h_1, ctx_1, PureLocalLoc)
        val lset = v_1._2
        val lset_f = lset.filter((l) => BoolTrue <= PreHelper.IsCallable(h_1,l))
        val lset_this = PreHelper.getThis(h_1, PSE.V(thisArg, h_1, ctx_1, PureLocalLoc)._1)
        // val lset_this = PreHelper.getThis(h_1, SE.V(thisArg, h_1, ctx_1, PureLocalLoc)._1) ++
        //                 h(PureLocalLoc)("@this")._1._2._2 // set this value with current this value
        val v_arg = PSE.V(arguments, h_1, ctx_1, PureLocalLoc)._1
        val o_old = h_1(PureLocalLoc)
        val cc_caller = cp._2
        val n_aftercall = cfg.getAftercallFromCall(cp._1)
        val cp_aftercall = (n_aftercall, cc_caller)
        val propv_arg = PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))
        lset_f foreach (l_f => {
          val o_f = h_1(l_f)
          val fids = o_f("@function")._1._3
          fids foreach (fid => {
            val ccset = cc_caller.NewCallContext(cfg, fid, l_r, lset_this)
            ccset.foreach {case (cc_new, o_new) => {
              val o_new2 = o_new.update(cfg.getArgumentsName(fid), propv_arg).
              update("@scope", o_f("@scope")._1)
              addCallEdge(cp, ((fid,LEntry), cc_new), ContextEmpty, o_new2)
              addReturnEdge(((fid,LExit), cc_new), cp_aftercall, ctx_1, o_old)
              addReturnEdge(((fid, LExitExc), cc_new), cp_aftercall, ctx_1, o_old)
            }}
          })
        })
        val h_2 = v_arg._2.foldLeft(h_1)((hh, l) => {
          hh.update(l, hh(l).update("callee",
            PropValue(ObjectValue(Value(lset_f), BoolTrue, BoolFalse, BoolTrue))))
        })

        // exception handling
        val cond = lset.exists((l) => BoolFalse <= PreHelper.IsCallable(h_1,l))
        val es_2 =
          if (cond) {
            Set(TypeError)
          } else {
            ExceptionBot
          }
        val es_3 =
          if (v_1._1 </ PValueBot) {
            Set(TypeError)
          } else {
            ExceptionBot
          }
        val es = es_1 ++ es_2 ++ es_3
        val (h_e, ctx_e) = PreHelper.RaiseException(h_2, ctx_1, PureLocalLoc, es)
        (h_e, ctx_e)
      }
      /* Assert */
      case CFGAssert(_, info, expr, _) => {
        val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, PSE.V(expr, h, ctx, PureLocalLoc)._2)
        (h_e, ctx_e)
      }
      case CFGCatch(_, _, name) => {
        val v_old = h(PureLocalLoc)("@exception_all")._1._2
        val h_1 = PreHelper.CreateMutableBinding(h, PureLocalLoc, name, h(PureLocalLoc)("@exception")._1._2)
        val h_2 = h_1.update(PureLocalLoc,
                             h_1(PureLocalLoc).update("@exception", PropValue(v_old)))
        (h_2, ctx)
      }
      case CFGReturn(_, _, expr) => {
        val (v,es) = expr match {
          case Some(expr) => PSE.V(expr, h, ctx, PureLocalLoc)
          case None => (Value(UndefTop),Set[Exception]())
        }
        val (h_1, ctx_1) =
          if (v </ ValueBot) {
            (h.update(PureLocalLoc, h(PureLocalLoc).update("@return", PropValue(v))), ctx)
          } else {
            (h, ctx)
          }
        val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx, PureLocalLoc, es)
        (h_e, ctx_e)
      }
      case CFGThrow(_, _, expr) => {
        val (v,es) = PSE.V(expr,h,ctx, PureLocalLoc)
        val v_old = h(PureLocalLoc)("@exception_all")._1._2
        val (h_e, ctx_e) = PreHelper.RaiseException(h, ctx, PureLocalLoc, es)
        val h_1 = h_e.update(PureLocalLoc,
                           h_e(PureLocalLoc).update("@exception", PropValue(v + v_old)).
                                           update("@exception_all", PropValue(v + v_old)))
        (h_1, ctx_e)
      }
      case CFGBuiltinCall(_, fun, args, addr1, addr2, addr3, addr4) => {
        val bCall = PreSemanticsBuiltin.builtinCall(this, h, ctx, h, ctx, cp, cfg, fun, args, addr1, addr2, addr3, addr4)
        (bCall._1._1 + bCall._2._1, bCall._1._2 + bCall._2._2)
      }
      case CFGInternalCall(_, _, lhs, fun, arguments, loc) => {
        (fun.toString, arguments, loc)  match {
          case ("<>Global<>toObject", List(expr), Some(a_new)) => {
            val (v,es_1) = PSE.V(expr, h, ctx, PureLocalLoc)
            val (h_1, ctx_1, es_2) =
              if (v </ ValueBot) {
                val (v_1, h_2, ctx_, es_) = PreHelper.toObject(h, ctx, v, a_new)
                val h_3 = PreHelper.VarStore(h_2, PureLocalLoc, lhs, v_1)
                (h_3, ctx_, es_)
              } else {
                (h, ctx, Set[Exception]())
              }
            val es = es_1 ++ es_2
            val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx_1, PureLocalLoc, es)
            (h_e, ctx_e)
          }
          case ("<>Global<>isObject", List(expr), None) => {
            val (v,es) = PSE.V(expr, h, ctx, PureLocalLoc)
            val (h_1, ctx_1) =
              if (v </ ValueBot) {
                val b_1 =
                  if (!v._2.isEmpty) BoolTrue
                  else BoolBot
                val b_2 =
                  if (v._1 </ PValueBot) BoolFalse
                  else BoolBot
                val b = b_1 + b_2
                (PreHelper.VarStore(h, PureLocalLoc, lhs, Value(b)), ctx)
              } else {
                (h, ctx)
              }
            val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx_1, PureLocalLoc, es)
            (h_e, ctx_e)
          }
          case ("<>Global<>toNumber", List(expr), None) => {
            val (v,es) = PSE.V(expr, h, ctx, PureLocalLoc)
            val (h_1, ctx_1) =
              if (v </ ValueBot) {
                val pv = PreHelper.toPrimitive(v)
                (PreHelper.VarStore(h, PureLocalLoc, lhs, Value(PreHelper.toNumber(pv))), ctx)
              } else {
                (h, ctx)
              }
            val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx_1, PureLocalLoc, es)
            (h_e, ctx_e)
          }
          case ("<>Global<>toBoolean", List(expr), None) => {
            val (v,es) = PSE.V(expr, h, ctx, PureLocalLoc)
            val (h_1, ctx_1) =
              if (v </ ValueBot) {
                (PreHelper.VarStore(h, PureLocalLoc, lhs, Value(PreHelper.toBoolean(v))), ctx)
              } else {
                (h, ctx)
              }
            val (h_e, ctx_e) = PreHelper.RaiseException(h_1, ctx_1, PureLocalLoc, es)
            (h_e, ctx_e)
          }
          case ("<>Global<>getBase", List(expr_2), None) => {
            val x_2 = expr_2.asInstanceOf[CFGVarRef].id
            val lset_base = PreHelper.LookupBase(h, PureLocalLoc, x_2)
            (PreHelper.VarStore(h, PureLocalLoc, lhs, Value(lset_base)), ctx)
          }
          case ("<>Global<>iteratorInit", List(expr), None) => {
            (h, ctx)
          }
          case ("<>Global<>iteratorHasNext", List(expr_2, expr_3), None) => {
            (PreHelper.VarStore(h, PureLocalLoc, lhs, Value(PValue(BoolTop))), ctx)
          }
          case ("<>Global<>iteratorNext", List(expr_2, expr_3), None) => {
            (PreHelper.VarStore(h, PureLocalLoc, lhs, Value(PValue(StrTop))), ctx)
          }
          case _ => {
              System.out.println(fun.toString)
            throw new NotYetImplemented()
          }
        }
      }
      case CFGNoOp(_, _, _) => {
        (h, ctx)
      }
    }
    //System.out.println("out heap#####\n" + DomainPrinter.printHeap(4, s._1._1))
    s
//  }
  }
}
