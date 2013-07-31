/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf

import kr.ac.kaist.jsaf.nodes.Id
import kr.ac.kaist.jsaf.analysis.cfg.CFGNode
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF}
import kr.ac.kaist.jsaf.nodes_util._

package object dtv {
  // scene id
  type SceneId = String
  
  // key id
  type KeyId = String

  // key list
  val keys: List[String] = List("UP", "DOWN", "LEFT", "RIGHT", "ENTER", "TOOLS", "RETURN", "EXIT", "INFO", "RED", "GREEN", "YELLOW", "BLUE", "N0", "N1", "N2", "N3", "N4", "N5", "N6", "N7", "N8", " N9", "PRECH", "VOL_UP", "VOL_DOWN", "MUTE", "CH_UP", "CH_DOWN", "RW", "PAUSE", "FF", "REC", "PLAY", "STOP")

  val sl = new SourceLocRats("<>DTV Apps Framework",-1,-1,-1)
  val span = new Span(sl,sl)
  def freshName(name: String) = "__DTVApp__" + name
  def mkFreshId(name: String) = NF.makeId(span, freshName(name))

  // AFW 2.0 function names
  val focusId = mkFreshId("focus")
  val showId = mkFreshId("show")
  val hideId = mkFreshId("hide")
  val sceneId = mkFreshId("scene")
  val keyDownId = mkFreshId("keyDown")
  val focusedId = mkFreshId("focused");
}
