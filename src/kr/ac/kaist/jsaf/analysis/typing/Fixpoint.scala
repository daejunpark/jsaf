/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import scala.collection.mutable.{HashMap => MHashMap}
import scala.collection.immutable.HashMap

class Fixpoint(cfg: CFG, worklist: Worklist, inTable: Table, quiet: Boolean, locclone: Boolean) {
  private val sem = new Semantics(cfg, worklist, locclone)
  def getSemantics = sem
  var count = 0
    
  def compute(): Unit = {
    worklist.add(((cfg.getGlobalFId, LEntry), CallContext.globalCallContext))
    //System.out.println()
    loop()
    if (Config.libMode)
      libraryLoop()
    System.out.println()
  }

  private def loop(): Unit = {
    while (!worklist.isEmpty) {
      if (!quiet) {
        System.out.print("\r  Dense Iteration: "+count+"   ")
        worklist.dump()
      }
      count = count+1

      val cp = worklist.getHead()

      val inS = readTable(cp)
      val (outS, outES) = sem.C(cp, cfg.getCmd(cp._1), inS)

      val succs = cfg.getSucc(cp._1)
      val esucc = cfg.getExcSucc.get(cp._1)
      
      // Propagate normal output state (outS) along normal edges.
      succs.foreach(node => {
        val cp_succ = (node, cp._2)
        val oldS = readTable(cp_succ)
        val newS = oldS + outS
        if (!(newS <= oldS)) {
          worklist.add(cp_succ)
          updateTable(cp_succ, newS)
        }
      })

      // Propagate exception output state (outES) along exception edges.
      // 1) If successor is catch, current exception value is assigned to catch variable and 
      //    previous exception values are restored.
      // 2) If successor is finally, current exception value is propagated further along
      //    finally block's "normal" edges.
      esucc match {
        case Some(node) =>
          val cp_succ = (node, cp._2)
          val oldS = readTable(cp_succ)
          val newS = oldS + outES
          if (!(newS <= oldS)) {
            worklist.add(cp_succ)
            updateTable(cp_succ, newS)
          }
        case None => ()
      } 
      
      // Propagate along inter-procedural edges
      // This step must be performed after evaluating abstract transfer function
      // because 'call' instruction can add inter-procedural edges.
      sem.getIPSucc(cp) match {
        case None => ()
        case Some(succMap) =>
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
            val newS = oldS + sem.E(cp, cp_succ, kv._2._1, kv._2._2, outS)
            if (!(newS <= oldS)) {
              worklist.add(cp_succ)
              updateTable(cp_succ, newS)
            }
          })
      }
    }
  }
  
  private def libraryLoop(): Unit = {
    System.out.println("\n* Library Mode *");
    // exit node of global function
    val globalNode = (cfg.getGlobalFId, LExit)
    // global call context
    val globalCC = CallContext.globalCallContext
    // state of global exit node
    val exitState = readTable((globalNode, globalCC))
    if (exitState <= StateBot)
      Unit
    else {
      val exitHeap = exitState._1
      // this value for library function
      val lset_this = LocSet(LibModeObjTopLoc)
      exitHeap.map.foreach((kv) => {
        val obj = kv._2
        AbsString.concretize(obj("@class")._1._2._1._5) match {
          case Some(s) if s == "Function" =>
            obj("@function")._1._3.foreach((fid) => {
              if (cfg.isUserFunction(fid)) {
                val l_r = addrToLoc(cfg.newProgramAddr(), Recent)
                val ccset = globalCC.NewCallContext(cfg, fid, l_r, lset_this)
	            ccset.foreach {case (cc_new, o_new) => {
  	              val o_arg = Obj(ObjMapBot.
	                updated("@default_number", (PropValue(ObjectValue(LibModeValueTop,BoolTrue,BoolTrue,BoolTrue)), AbsentTop)).
	                updated("@default_other", (PropValueBot, AbsentTop)).
	                updated("@class", (PropValue(AbsString.alpha("Arguments")), AbsentBot)).
	                updated("@proto", (PropValue(ObjectValue(ObjProtoLoc, BoolFalse, BoolFalse, BoolFalse)), AbsentBot)).
	                updated("@extensible", (PropValue(BoolTrue), AbsentBot)).
	                updated("length", (PropValue(ObjectValue(UInt, BoolTrue, BoolFalse, BoolTrue)), AbsentBot)))
	              val l_arg = addrToLoc(cfg.newProgramAddr(), Recent)
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
	              worklist.add(((fid, LEntry), cc_new))
	              }}
              }
              else
                Unit
            })
          case _ => Unit
        }})
        
      loop()
      Unit
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
