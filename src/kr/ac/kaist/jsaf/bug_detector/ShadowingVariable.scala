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
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.nodes.AbstractNode
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}

class ShadowingVariable(cfg: CFG, typing: TypingInterface, bugs: BugMessage,
                        shadowingErrors: List[StaticError]) extends BugRule {
  
  /* print statistics */
  override def printStat = System.out.println("# ShadowingVariable: " + shadowingErrors.length)

  /* bug check */
  override def checkFinal(): Unit =
    for (error <- shadowingErrors) error.location match {
      case Some(loc) => bugs.addMessage(loc.asInstanceOf[AbstractNode].getInfo.getSpan, "warning", error.description)
      case _ => Unit
    }
}
