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
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._
import scala.collection.mutable.Map

class FunctionInfo() {
  var isTarget: Boolean = false
  var isCandidate: Boolean = true
  var isProcess: Boolean = false
  var params: Map[Int, List[String]] = Map[Int, List[String]]()
  //var irId: IRId = null

  def storeParam(p: Int, t: String) = params.put(p, params.get(p) match {case Some(types) => types:+t; case None => List(t)})
  //def storeIR(ir: IRId) = irId = ir
  
  def targeting = isTarget = true
  def processing = isProcess = true
  def unprocessing = isProcess = false
  def done = {isTarget = false; isCandidate = false; isProcess = false}

  override def toString = "Function Information: target? "+isTarget+" candidate? "+isCandidate+" parameter information? "+params+"\n"
}

