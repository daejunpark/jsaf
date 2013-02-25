/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic

import _root_.java.util.{List => JList}
import _root_.edu.rice.cs.plt.tuple.{Option => JavaOption}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.EJSOp
import kr.ac.kaist.jsaf.nodes_util.{ IRFactory => IF }
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._

class ConstraintForm() {
  var lhs: String = ""
  var op: Option[String] = None
  var rhs: Option[ConstraintForm] = None

  def makeConstraint(left: String) = 
    lhs = left
  
  def makeConstraint(left: String, operator:String, right:ConstraintForm) = { 
    lhs = left
    op = Some(operator)
    rhs = Some(right)
  }

  def getLhs:String = lhs
  def getOp:JavaOption[String] = toJavaOption(op)
  def getRhs:JavaOption[ConstraintForm] = toJavaOption(rhs)
  
  override def toString:String = {
    var operator = op match { case Some(x) => x
                              case None => "" }
    var right = rhs match { case Some(x) => x.toString
                            case None => "" }
    return lhs + operator + right
  }
}

