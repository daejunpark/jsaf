/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic

import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.EJSOp
import kr.ac.kaist.jsaf.nodes_util.{ IRFactory => IF }
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._

class Info(stype: Boolean, id: String, operator: Option[String], context: String, branch: Option[Boolean]) {
  val cond = stype
  /* v = left op right */
  val op = operator
  val expr = (id, context)
  val branchTaken = branch match { case Some(b) => b; case None => true }

  override def toString = context 
}

