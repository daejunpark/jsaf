/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet
import scala.collection.mutable.{HashMap => MHashMap}
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._

class DSparseFixpoint(cfg: CFG, worklist: Worklist, inTable: Table, quiet: Boolean) {
  private val sem = new Semantics(cfg, worklist)
  def getSemantics = sem
  var count = 0
  var duset: DUSet = Map()
  var time = 0.0

  def compute(du: DUSet): Unit = {
    duset = du
    worklist.add(((cfg.getGlobalFId, LEntry), CallContext.globalCallContext))
    System.out.println()
    loop()
    System.out.println()
    if (!quiet)
      System.out.println("# edge recovering time: "+time)
  }

  private var cache = HashMap[Int, (ControlPoint, State, State, State)]()

  private def makeKey(cp: ControlPoint, s: State): Int = {
    ((cp.hashCode() % 128) << 7 + (s.hashCode() % 128))
  }

  private def cmdsize(cmd: Cmd) = {
    cmd match {
      case Block(i) => i.length
      case _ => 1
    }
  }

  private def loop(): Unit = {
    while (!worklist.isEmpty) {
      if (!quiet)
        System.out.print("\r  Sparse Iteration: "+count+"        ")
      worklist.dump()
      count = count+1

      val cp = worklist.getHead()
      val (fg, ddg) = cfg.getFlowGraph(cp._1._1, cp._2)

      val inS = readTable(cp)

      // System.out.println("\n== InHeap ==\n" + DomainPrinter.printHeap(4,inS._1, true))

      // val (outS, outES) = sem.C(cp, cfg.getCmd(cp._1), inS)
      val cmd = cfg.getCmd(cp._1)
      val (outS, outES) =
        if (15 < cmdsize(cmd)) {
          val key = makeKey(cp, inS)
          cache.get(key) match {
            case Some((cpc, inSc, outSc, outESc)) if ((cpc == cp) && (inSc == inS)) => (outSc, outESc)
            case _ => {
              val (outSo, outESo) = sem.C(cp, cmd, inS)
              if (outSo != StateBot)
                cache += (key -> (cp, inS, outSo, outESo))
              (outSo, outESo)
            }
          }
        } else {
          sem.C(cp, cmd, inS)
        }

      val recover_start = System.nanoTime()
      val edges =
        if (outS._1 != HeapBot && !cfg.getCalls.contains(cp._1)) {
            cfg.recoverOutEdges(fg, cp._1)
        } else {
          HashSet[(Node,Node)]()
        }
      val excEdges =
        if (outES._1 != HeapBot) {
          cfg.recoverOutExcEdge(fg, cp._1)
        } else {
          HashSet[(Node,Node)]()
        }

      if (!edges.isEmpty || !excEdges.isEmpty) {
        val recovered = cfg.recover_intra_dugraph(fg, ddg, edges, excEdges) - cp._1
        recovered.foreach(node => worklist.add((node, cp._2)))
      }
      val recover_time = (System.nanoTime() - recover_start) / 1000000000.0;
      time += recover_time

      if (outS._1 != HeapBot) {
        // System.out.println("\n== Heap ==\n" + DomainPrinter.printHeap(4,outS._1, false))
        val succs = ddg.getNormalSuccs(cp._1)

        // Propagate normal output state (outS) along normal edges.
        succs.foreach(node => {
          val cp_succ = (node, cp._2)
          val oldS = readTable(cp_succ)
          val succ_set = ddg.getDUSet(cp._1, node)
          // System.out.println("Propagates a heap from "+cp._1+" -> "+node)
          // System.out.println(DomainPrinter.printHeap(4, (outS.restrict(succ_set))._1))
          val outS2 = outS.restrict(succ_set)
          if (!(outS2 <= oldS)) {
            val newS = oldS + outS2
            worklist.add(cp._1, cp_succ)
            updateTable(cp_succ, newS)
          }
        })

        if (cfg.getCalls.contains(cp._1)) {
          // Propagate normal output state along call/after-call edges.
          val bypass_set = cfg.getBypassingSet(cp)
          if (!bypass_set.isEmpty) {
            val cp_succ = (cfg.getAftercallFromCall(cp._1), cp._2)
            val oldS = readTable(cp_succ)
            val outS2 = outS.restrict(bypass_set)
            if (!(outS2 <= oldS)) {
              val newS = oldS + outS2
              worklist.add(cp._1, cp_succ)
              updateTable(cp_succ, newS)
            }
          }
        }
      }

      if (outES._1 != HeapBot) {
        // System.out.println("\n== ExcHeap ==\n" + DomainPrinter.printHeap(4,outES._1, true))
        val esucc = ddg.getExcSucc(cp._1)

        // Propagate exception output state (outES) along exception edges.
        // 1) If successor is catch, current exception value is assigned to catch variable and
        //    previous exception values are restored.
        // 2) If successor is finally, current exception value is propagated further along
        //    finally block's "normal" edges.
        esucc match {
          case Some(node) => {
            val cp_succ = (node, cp._2)
            val oldS = readTable(cp_succ)
            val succ_set = ddg.getExcDUSet(cp._1, node)
            val outES2 = outES.restrict(succ_set)
            // System.out.println("Propagates a excheap from "+cp._1+" -> "+node)
            // System.out.println(DomainPrinter.printHeap(4, outES._1))
            if (!(outES2 <= oldS)) {
              val newES = oldS + outES2
              worklist.add(cp._1, cp_succ)
              updateTable(cp_succ, newES)
            }
          }
          case None => ()
        }

        if (cfg.getCalls.contains(cp._1)) {
          // Propagate exception output state along call/after-call edges.
          val bypass_set = cfg.getBypassingExcSet(cp)
          if (!bypass_set.isEmpty) {
            val cp_exc = cfg.getExcSucc(cfg.getAftercallFromCall(cp._1)) match {
              case None => throw new InternalError("After-call node must have exception successor")
              case Some(node) => (node, cp._2)
            }
            val oldS = readTable(cp_exc)
            val outS2 = outES.restrict(bypass_set)
            if (!(outS2 <= oldS)) {
              val newS = oldS + outS2
              worklist.add(cp._1, cp_exc)
              updateTable(cp_exc, newS)
            }
          }
        }
      }

      // Propagate along inter-procedural edges
      // This step must be performed after evaluating abstract transfer function
      // because 'call' instruction can add inter-procedural edges.
      sem.getIPSucc(cp) match {
        case None => ()
        case Some(succMap) =>
          succMap.foreach(kv => {

            // bypassing if IP edge is exception flow.
            val cp_aftercall = kv._1
            val cp_succ =
              cp._1._2 match {
                case LExitExc => {
                  val n_aftercall = kv._1._1
                  cfg.getExcSucc(n_aftercall) match {
                    case None => throw new InternalError("After-call node must have exception successor")
                    case Some(node) => (node, kv._1._2)
                  }
                }
                case _ => kv._1
              }
            val oldS = readTable(cp_succ)
            val outS_E = sem.E(cp, cp_succ, kv._2._1, kv._2._2, outS)

            // if cp is after-call, call-to-after-call cfg must be recovered.
            (cp) match {
              case ((_, LExit), _) => {
                if (outS._1 != HeapBot) {
                  val recover_start = System.nanoTime()
                  val n_call = cfg.getCallFromAftercall(cp_aftercall._1)
                  val call = (n_call, cp_aftercall._2)
                  // System.out.println("try to recover normal: "+call)
                  if (cfg.updateBypassing(call, cp._1._1)) {
                    worklist.add(cp._1, call)
                  }
                  val (fg, ddg) = cfg.getFlowGraph(call._1._1, call._2)
                  val edges = cfg.recoverOutEdges(fg, call._1)
                  if (!edges.isEmpty) {
                    val recovered = cfg.recover_intra_dugraph(fg, ddg, edges, Set())
                    recovered.foreach(node => worklist.add((node, call._2)))
                  }
                  val recover_time = (System.nanoTime() - recover_start) / 1000000000.0;
                  time += recover_time
                }
              }
              case ((_, LExitExc), _) => {
                if (outS._1 != HeapBot) {
                  val recover_start = System.nanoTime()
                  val n_call = cfg.getCallFromAftercall(cp_aftercall._1)
                  val call = (n_call, cp_aftercall._2)
                  // System.out.println("try to recover exception: "+call)
                  if (cfg.updateBypassingExc(call, cp._1._1)) {
                    worklist.add(cp._1, call)
                  }
                  val (fg, ddg) = cfg.getFlowGraph(call._1._1, call._2)

                  // recover CFG edge of call.
                  val edges = cfg.recoverOutEdges(fg, call._1)
                  // recover EFG edge of aftercall.
                  val exc_edges = cfg.recoverOutExcEdge(fg, cp_aftercall._1)

                  if (!edges.isEmpty || !exc_edges.isEmpty) {
                    val recovered = cfg.recover_intra_dugraph(fg, ddg, edges, exc_edges)
                    recovered.foreach(node => worklist.add((node, call._2)))
                  }

                  val recover_time = (System.nanoTime() - recover_start) / 1000000000.0;
                  time += recover_time
                }
              }
              case _ => ()
            }
          if (!(outS_E <= oldS)) {
            val newS = oldS + outS_E
            worklist.add(cp._1, cp_succ)
            updateTable(cp_succ, newS)
          }
        })
      }
    }
  }

  private def readTable(cp: ControlPoint): State = {
    inTable.get(cp._1) match {
      case None => StateBot
      case Some(map) => map.get(cp._2) match {
        case None => StateBot
        case Some(state) => state
      }
    }
  }

  private def updateTable(cp: ControlPoint, state: State): Unit = {
    inTable.get(cp._1) match {
      case None =>
        inTable.update(cp._1, HashMap((cp._2, state)))
      case Some(map) =>
        inTable.update(cp._1, map.updated(cp._2, state))
    }
  }
}
