/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import java.util.HashMap
import kr.ac.kaist.jsaf.nodes_util.Span

class BugMessage(fileMap: HashMap[String, String]) {
  /* message list */
  var msgList = List[(String, Span, String, String, String)]()
  
  /* sorting flag */
  private var isSorted : Boolean = false
  
  /* statistics */
  private var errorCount   : Int  = 0
  private var warningCount : Int  = 0
  private var startTime    : Long = 0L
  private var endTime      : Long = 0L

  /* get file name order */
  private def getFileOrder(fname: String): String = {
    fileMap.get(fname).split("::")(1)
  }

  /* sort a message list */
  private def sortMessage() = {
    // TODO: file name should be sorted by input order, not alphanumeric order.
    if (!isSorted) {
//      msgList = msgList.sortBy((msg) => (getFileOrder(msg._1), msg._2, msg._3))
      msgList = msgList.sortBy((msg) => (msg._1, msg._2.getBegin.getLine, msg._2.getBegin.column))
      isSorted = true
    }
  }
  
  /* exception bug */
  def addMessage(span: Span, kind: String, exc: String, msg: String): Unit = {
    if (kind == "error")
      errorCount = errorCount + 1
    else if (kind == "warning")
      warningCount = warningCount + 1
    msgList = msgList :+ (convertFileName(span.getFileName), span, kind, exc, msg)
    isSorted = false
  }
  /* non-exception bug*/
  def addMessage(span: Span, kind: String, msg: String): Unit = {
    if (kind == "error")
      errorCount = errorCount + 1
    else if (kind == "warning")
      warningCount = warningCount + 1
    msgList = msgList :+ (convertFileName(span.getFileName), span, kind, "", msg)
    isSorted = false
  }
  
  def isDupMessage(span: Span, kind: String, msg: String): Boolean = {
    msgList.contains((convertFileName(span.getFileName), span, kind, "", msg)) 
  }
  def isDupMessage(span: Span, kind: String, exc: String, msg: String): Boolean = {
    msgList.contains((convertFileName(span.getFileName), span, kind, exc, msg)) 
  }

  def convertFileName(file: String) = file
      /*
    if (fileMap.containsKey(file))
      fileMap.get(file).split("::")(0)
    else file
      */

  def setStartTime(time: Long) = {
    startTime = time
  }
  
  def setEndTime(time: Long) = {
    endTime = time
  }
  
  def printMessage() = {
    if (!isSorted)
      sortMessage()
    msgList.distinct.foreach((msg) => {
      val begin = msg._2.getBegin
      val end = msg._2.getEnd
      if (msg._4 != "")
        System.out.println("%s:%d:%d~%d:%d: %s: %s: %s".format(msg._1, begin.getLine, begin.column,
                                                               end.getLine, end.column, msg._3, msg._4,
                                                               msg._5))
      else
        System.out.println("%s:%d:%d~%d:%d: %s: %s".format(msg._1, begin.getLine, begin.column,
                                                           end.getLine, end.column, msg._3, msg._5))})
  }
  
  def printTotal() = {
    System.out.println("# Errors(#)   : " + errorCount)
    System.out.println("# Warnings(#) : " + warningCount)
  }
  
  def printTime() = {
    System.out.println("# Time for bug Detector(s): %.2f".format((endTime - startTime) / 1000000000.0))
  }
}
