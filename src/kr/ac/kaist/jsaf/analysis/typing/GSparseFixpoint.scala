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

class GSparseFixpoint(cfg: CFG, worklist: Worklist, inTable: Table, quiet: Boolean) {
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
      val ddg = cfg.getGlobalDDG

      val inS = readTable(cp)
//      System.out.println("\n== InHeap ==\n" + DomainPrinter.printHeap(4,inS._1, true))

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
//      System.out.println("\n== OutHeap ==\n" + DomainPrinter.printHeap(4,outS._1, true))

      val recover_start = System.nanoTime()

      // TODO
      val ipedges: HashSet[(ControlPoint, ControlPoint)] = {
        sem.getIPSucc(cp) match {
          case Some(m) => m.foldLeft(HashSet[(ControlPoint,ControlPoint)]())((S, kv) => {
            S + ((cp, kv._1))
          })
          case None => HashSet[(ControlPoint,ControlPoint)]()
        }
      }
      val edges: HashSet[(ControlPoint, ControlPoint)] =
        if (outS._1 != HeapBot) {
            cfg.recoverGOutEdges(cp) ++ ipedges
        } else {
          ipedges
        }
      val excEdges: HashSet[(ControlPoint, ControlPoint)] =
        if (outES._1 != HeapBot) {
          cfg.recoverGOutExcEdge(cp)
        } else {
          HashSet[(ControlPoint,ControlPoint)]()
        }

      if (!edges.isEmpty || !excEdges.isEmpty) {
        val recovered = cfg.recover_dugraph(edges, excEdges) - cp
        recovered.foreach(worklist.add)
      }
      val recover_time = (System.nanoTime() - recover_start) / 1000000000.0;
      time += recover_time

      if (outS._1 != HeapBot) {
//        System.out.println("\n== Heap ==\n" + DomainPrinter.printHeap(4,outS._1, false))
        val succs = ddg.getNormalSuccs(cp)

        // Propagate normal output state (outS) along normal edges.
        succs.foreach(cp_succ => {
          // val cp_succ = (node, cp._2)
          val oldS = readTable(cp_succ)
          // XXX: #PureLocal must not be passed between functions.
          val succ_set =
            if (cp_succ._1._2 == LEntry || cp._1._2 == LExit || cp._1._2 == LExitExc)
              ddg.getDUSet(cp, cp_succ) - SinglePureLocalLoc
            else
              ddg.getDUSet(cp, cp_succ)

//          System.out.println("Propagates a heap from "+cp._1+" -> "+cp_succ +" with "+DomainPrinter.printLocSet(succ_set))
//          System.out.println(DomainPrinter.printHeap(4, (outS.restrict(succ_set))._1))
          val outS2 = outS.restrict(succ_set)
          if (!(outS2 <= oldS)) {
            val newS = oldS + outS2
            worklist.add(cp._1, cp_succ)
            updateTable(cp_succ, newS)
          }
        })
      }

      if (outES._1 != HeapBot) {
//        System.out.println("\n== ExcHeap ==\n" + DomainPrinter.printHeap(4,outES._1, true))
        val esucc = ddg.getExcSucc(cp)

        // Propagate exception output state (outES) along exception edges.
        // 1) If successor is catch, current exception value is assigned to catch variable and
        //    previous exception values are restored.
        // 2) If successor is finally, current exception value is propagated further along
        //    finally block's "normal" edges.
        esucc match {
          case Some(cp_succ) => {
            // val cp_succ = (node, cp._2)
            val oldS = readTable(cp_succ)
            val succ_set = ddg.getExcDUSet(cp, cp_succ)
            val outES2 = outES.restrict(succ_set)
//            System.out.println("Propagates a excheap from "+cp._1+" -> "+cp_succ +" with "+DomainPrinter.printLocSet(succ_set))
//            System.out.println(DomainPrinter.printHeap(4, outES2._1))
            if (!(outES2 <= oldS)) {
              val newES = oldS + outES2
              worklist.add(cp._1, cp_succ)
              updateTable(cp_succ, newES)
            }
          }
          case None => ()
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
//                  val fg = cfg.getFlowGraph_(call._1._1, call._2)
                  val edges = cfg.recoverGOutEdges(call)
                  if (!edges.isEmpty) {
                    // for global sparse analysis
                    // add inter-procedural edges in global CFG
//                    cfg.addIPEdge(cp, cp_aftercall)

                    val recovered = cfg.recover_dugraph(edges, Set())
                    recovered.foreach(worklist.add)
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
//                  val fg = cfg.getFlowGraph_(call._1._1, call._2)

                  // recover CFG edge of call.
                  val edges = cfg.recoverGOutEdges(call)
                  // recover EFG edge of aftercall.
                  val exc_edges = cfg.recoverGOutExcEdge(cp_aftercall)

                  if (!edges.isEmpty || !exc_edges.isEmpty) {
                    // for global sparse analysis
                    // add inter-procedural edges in global CFG
//                    cfg.addIPEdge(cp, cp_aftercall)

                    val recovered = cfg.recover_dugraph(edges, exc_edges)
                    recovered.foreach(worklist.add)
                  }

                  val recover_time = (System.nanoTime() - recover_start) / 1000000000.0;
                  time += recover_time
                }
              }
              case _ => ()
            }
          if (!(outS_E <= oldS)) {

            // for global sparse analysis
            // add inter-procedural edges in global CFG
//            cfg.addIPEdge(cp, kv._1)

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
