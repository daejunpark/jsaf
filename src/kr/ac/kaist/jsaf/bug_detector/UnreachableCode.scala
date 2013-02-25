/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.Typing
import kr.ac.kaist.jsaf.nodes_util._
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

class UnreachableCode(cfg: CFG, typing: TypingInterface, bugs: BugMessage) extends BugRule {

  /* bug message */  
  private val msg   = "Unreachable code is found."
  private var count = 0 
  private var range : List[Span] = List()
   
  override def printStat = System.out.println("# UnreachableCode: " + count) 

  override def checkFinal(): Unit = {
    if (range isEmpty) Unit
    else {
      // Sort the span list according to (filename, beginLine, beginColumn) order.
      range = range.sortBy((span) => (span.getFileName, span.getBegin.getLine, span.getBegin.column))
      var chunkBegin = range.head.getBegin
      var chunkEnd   = range.head.getEnd
      for (span <- range.tail) {
        val sb = span.getBegin
        if ((chunkEnd.getLine.toInt < sb.getLine.toInt) || ((chunkEnd.getLine.toInt == sb.getLine.toInt) && (chunkEnd.column.toInt < sb.column.toInt))) addNewBug(Some(span)) 
        else chunkify(chunkBegin, chunkEnd, sb, span.getEnd)
      }
      addNewBug(None)

      def addNewBug(span: Option[Span]): Unit = {
        count = count + 1
        bugs.addMessage(new Span(chunkBegin, chunkEnd), "warning", msg)
        span match {
          case Some(sp) => chunkBegin = sp.getBegin; chunkEnd = sp.getEnd
          case None => Unit
        }
      }
    
      def chunkify(cb: SourceLoc, ce: SourceLoc, tb: SourceLoc, te: SourceLoc): Unit = {
        if ((tb.getLine.toInt < cb.getLine.toInt) || ((tb.getLine.toInt == cb.getLine.toInt) && (tb.column.toInt < cb.column.toInt))) chunkBegin = tb
        if ((ce.getLine.toInt < te.getLine.toInt) || ((ce.getLine.toInt == te.getLine.toInt) && (ce.column.toInt < te.column.toInt))) chunkEnd = te
      }
    }
  }

  override def checkInst(inst: CFGInst, cstate: CState): Unit = {
    val state = typing.mergeState(cstate)
    if (state._1 <= HeapBot) {
      inst.getInfo match {
        case Some(info) => range = range :+ info.getSpan
        case None => System.out.println("BugDetector, UnreachableCode. Instruction has no info.")
      }
    }; Unit
  }
}
