/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import _root_.java.lang.{Integer => JInteger}
import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.concolic.ConstraintForm
import kr.ac.kaist.jsaf.scala_src.useful.Lists._

/* Calculates code coverage. */
class Coverage() {
  var total = 0
  var executed = 0
  var execSet = scala.collection.immutable.Set[Long](0);

  /* For concolic test */
  var input = List[Int]()
  var inputNum = 0
  var constraints = List[ConstraintForm]()

  def toInt(n: JInteger):Int = 
    n.intValue()
  def setInput(result: JList[JInteger]) = { 
    var tmp = map(result, toInt)
    input = toList(tmp)
  }
  def getConstraints:JList[ConstraintForm] = 
    toJavaList(constraints)
    
  def cont() = constraints.nonEmpty
}
