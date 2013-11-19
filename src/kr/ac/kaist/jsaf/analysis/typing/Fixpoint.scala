/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.debug.DebugConsole
import kr.ac.kaist.jsaf.Shell
import kr.ac.kaist.jsaf.scala_src.useful.WorkTrait
import kr.ac.kaist.jsaf.analysis.typing.AddressManager._

class Fixpoint(cfg: CFG, worklist: Worklist, inTable: Table, quiet: Boolean, locclone: Boolean) {
  private val sem = new Semantics(cfg, worklist, locclone)
  def getSemantics = sem
  var count = 0
  val countRef = new AnyRef
  var isLocCountExceeded = false
  var locCountExceededMessage = ""
  var isTimeout = false
  var startTime: Long = 0

  def compute(): Unit = {
    Config.setDebugger(Shell.params.opt_debugger)

    // Analysis start time
    startTime = System.nanoTime()

    // Single-thread option has a priority
    if(Shell.params.opt_SingleThread || !Shell.params.opt_MultiThread) {
      /** Single-thread */

      // Debugger
      if (Config.debugger) DebugConsole.initialize(cfg, worklist, sem, inTable)

      // Add entry node
      worklist.add(((cfg.getGlobalFId, LEntry), CallContext.globalCallContext), None, false)

      // Loop
      loop()

      // Library mode
      if (Config.libMode) {
        libraryLoop()
        loop()
      }
      System.out.println()

      if (Config.debugger) DebugConsole.runFinished()
    }
    else {
      /** Multi-thread */

      // Initialize WorkManager
      Shell.workManager.initialize()

      // Add entry node
      worklist.setUseWorkManager(true, new FixpointWork)
      worklist.add(((cfg.getGlobalFId, LEntry), CallContext.globalCallContext), None, false)

      // Wait until all works are finished.
      Shell.workManager.waitFinishEvent()
      if(!worklist.isEmpty) throw new RuntimeException("*** Worklist is not empty! (1)")

      // Library mode
      if (Config.libMode) {
        libraryLoop()

        // Wait until all works are finished.
        Shell.workManager.waitFinishEvent()
        if(!worklist.isEmpty) throw new RuntimeException("*** Worklist is not empty! (2)")
      }
      System.out.println()

      // Deinitialize WorkManager
      Shell.workManager.deinitialize()
    }

    if(isLocCountExceeded) {
      System.out.println("*** Max location count(" + Shell.params.opt_MaxLocCount + ") is exceeded!")
      System.out.println("  " + locCountExceededMessage)
    }
    if(isTimeout) System.out.println("*** Analysis time out! (" + Shell.params.opt_Timeout + " sec)")

    if (Shell.params.opt_ExitDump) {
      System.out.println("** Dump Exit Heap **\n=========================================================================")
      System.out.println(DomainPrinter.printHeap(4, readTable(((cfg.getGlobalFId,LExit),CallContext.globalCallContext))._1, cfg))
      System.out.println("=========================================================================")
    }

    if (Shell.params.opt_BottomDump) sem.dumpHeapBottoms()
  }

  class FixpointWork extends WorkTrait {
    override def doit(): Unit = {
      var cp: ControlPoint = null
      var callerCPSetOpt: Option[CPStackSet] = null
      worklist.synchronized {
        // Worklist check
        if(worklist.isEmpty) return

        // Worklist.head print
        if(!quiet) {
          System.out.print("\r  Dense Iteration: " + count + "(" + worklist.getSize + ")   ")
          worklist.dump()
        }

        // Debugger is used for only single-thread mode
        if (Config.debugger)
          DebugConsole.runFixpoint(count)

        // Iteration count
        count+= 1

        // Get a work
        val (_cp, _callerCPSetOpt) = worklist.getHead()
        cp = _cp; callerCPSetOpt = _callerCPSetOpt
      }

      // Analysis termination check
      if(isLocCountExceeded || isTimeout) return
      if(Shell.params.opt_Timeout > 0) {
        if((System.nanoTime() - startTime) / 1000000000 > Shell.params.opt_Timeout) {isTimeout = true; return}
      }

      // Read a state
      val inS = readTable(cp)

      // Execute
      val (outS, outES) = try {
        sem.C(cp, cfg.getCmd(cp._1), inS, callerCPSetOpt) // TODO: Multi-thread safety check
      }
      catch {
        case e: MaxLocCountError =>
          if(locCountExceededMessage == "") locCountExceededMessage = e.getMessage
          isLocCountExceeded = true
          return
      }

      // Propagate normal output state (outS) along normal edges.
      val succs = cfg.getSucc_Lock(cp._1)
      succs.foreach(node => { // TODO: Multi-thread safety check
        val cp_succ = (node, cp._2)
        val oldS = readTable(cp_succ)
        if(!(outS <= oldS)) {
          val newS = if(cfg.getAllPred(cp_succ._1).size <= 1) outS else oldS + outS
          updateTable(cp_succ, newS)
          worklist.add(cp_succ, callerCPSetOpt, false)
        }
      })

      // Propagate exception output state (outES) along exception edges.
      // 1) If successor is catch, current exception value is assigned to catch variable and
      //    previous exception values are restored.
      // 2) If successor is finally, current exception value is propagated further along
      //    finally block's "normal" edges.
      val esucc = cfg.getExcSucc_Lock(cp._1)
      esucc match { // TODO: Multi-thread safety check
        case Some(node) =>
          val cp_succ = (node, cp._2)
          val oldES = readTable(cp_succ)
          if(!(outES <= oldES)) {
            val newES = oldES + outES
            updateTable(cp_succ, newES)
            worklist.add(cp_succ, callerCPSetOpt, false)
          }
        case None => ()
      }

      // Propagate along inter-procedural edges
      // This step must be performed after evaluating abstract transfer function
      // because 'call' instruction can add inter-procedural edges.
      sem.getIPSucc(cp) match {
        case None => ()
        case Some(succMap) =>
          succMap.synchronized {
            succMap.foreach(kv => {
            // bypassing if IP edge is exception flow.
            val cp_succ = kv._1
              /*
                cp._1._2 match {
                  case LExitExc => {
                    val n_aftercall = kv._1._1
                    cfg.getExcSucc.get(n_aftercall) match {
                      case None => throw new InternalError("After-call node must have exception successor")
                      case Some(node) => (node, kv._1._2)
                    }
                  }
                  case _ => kv._1
                }
              */
              val oldS = readTable(cp_succ)
              val outS2 = sem.E(cp, cp_succ, kv._2._1, kv._2._2, outS) // TODO: Multi-thread safety check
              if(!(outS2 <= oldS)) {
                val newS = oldS + outS2
                // no-return-state option has a priority
                if(Shell.params.opt_ReturnStateOff || !Shell.params.opt_ReturnStateOn) {
                  updateTable(cp_succ, newS)
                  worklist.add(cp_succ, None, false)
                }
                else {
                  worklist.add(cp, cp_succ, cfg, callerCPSetOpt, false, Unit => updateTable(cp_succ, newS))
                }
              }
            })
          }
      }
    }
  }

  private def loop(): Unit = {
    val work = new FixpointWork
    while(!worklist.isEmpty) work.doit()
  }

  private def libraryLoop(): Unit = {
    System.out.println("\n* Library Mode *");
    // exit node of global function
    val globalNode = (cfg.getGlobalFId, LExit)
    // global call context
    val globalCC = CallContext.globalCallContext
    // state of global exit node
    val exitState = readTable((globalNode, globalCC))
    if (exitState <= StateBot) return
    val exitHeap = exitState._1
    // this value for library function
    val lset_this = LocSet(LibModeObjTopLoc)
    exitHeap.map.foreach((kv) => {
      val obj = kv._2
      AbsString.concretize(obj("@class")._1._2._1._5) match {
        case Some(s) if s == "Function" =>
          obj("@function")._1._3.foreach((fid) => {
            if (cfg.isUserFunction(fid)) {
              val l_r = newRecentLoc()
              val ccset = globalCC.NewCallContext(cfg, fid, l_r, lset_this)
          ccset.foreach {case (cc_new, o_new) => {
              val o_arg = Obj(ObjMapBot.
              updated("@default_number", (PropValue(ObjectValue(LibModeValueTop,BoolTrue,BoolTrue,BoolTrue)), AbsentTop)).
              updated("@default_other", (PropValueBot, AbsentTop)).
              updated("@class", (PropValue(AbsString.alpha("Arguments")), AbsentBot)).
              updated("@proto", (PropValue(ObjectValue(ObjProtoLoc, BoolFalse, BoolFalse, BoolFalse)), AbsentBot)).
              updated("@extensible", (PropValue(BoolTrue), AbsentBot)).
              updated("length", (PropValue(ObjectValue(UInt, BoolTrue, BoolFalse, BoolTrue)), AbsentBot)))
              val l_arg = newRecentLoc()
              val v_arg = Value(l_arg)
              val value = PropValue(ObjectValue(v_arg, BoolTrue, BoolFalse, BoolFalse))
              val o_new2 = o_new.update(cfg.getArgumentsName(fid), value).
                update("@scope", obj("@scope")._1)

              val env_obj = Helper.NewDeclEnvRecord(o_new2("@scope")._1._2)

              val obj2 = o_new2 - "@scope"
              val h1 = exitState._1.update(l_arg, o_arg) // arguments object update
              val h2 = h1.remove(SinglePureLocalLoc)
              val h3 = h2.update(SinglePureLocalLoc, obj2)
              val h4 = obj2("@env")._1._2._2.foldLeft(HeapBot)((hh, l_env) => {
                hh + h3.update(l_env, env_obj) })
                // Localization, ignore
                /*val h5 =
                  if (cfg.optionLocalization) {
                    val useset = cfg.getLocalizationSet(cp2._1._1)
                    h4.restrict(useset)
                  } else
                    h4 */
                // state set up
                updateTable(((fid, LEntry), cc_new), State(h4, ContextEmpty))
                // add to worklist
                worklist.add(((fid, LEntry), cc_new), None, false)
              }}
            }
          })
        case _ =>
      }
    })
  }
  
  private def readTable(cp: ControlPoint): State = inTable.synchronized {
    inTable.get(cp._1) match {
      case None => StateBot
      case Some(map) => map.get(cp._2) match {
        case None => StateBot
        case Some(state) => state
      }
    }
  }
  
  private def updateTable(cp: ControlPoint, state: State): Unit = inTable.synchronized {
    inTable.get(cp._1) match {
      case None =>
        inTable.update(cp._1, HashMap((cp._2, state)))
      case Some(map) =>
        inTable.update(cp._1, map.updated(cp._2, state))
    }
  }
}

