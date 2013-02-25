/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/


package kr.ac.kaist.jsaf.bug_detector
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._

trait BugRule {
  def printStat(): Unit = Unit

  def checkFinal(): Unit = Unit
  def checkInst(inst: CFGInst, cstate: CState): Unit = Unit
  def checkExpr(inst: CFGInst, expr: CFGExpr, cstate: CState): Unit = Unit
}
