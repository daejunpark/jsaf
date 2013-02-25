/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.interpreter.objects

import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.interpreter.{InterpreterPredefine => IP, _}
import scala.collection.mutable.{HashMap, HashSet}

object JSRegExpHelper {
  type CharSet = HashSet[Char]
  class RegExpState(var endIndex: Int, var captures: HashMap[Int, Option[String]])
  type MatchResult = Option[RegExpState]
  type Continuation = (RegExpState) => MatchResult
  type Matcher = (RegExpState, Continuation) => MatchResult
  type AssertionTester = (RegExpState) => Boolean

  abstract class EscapeValue
  case class CharEscapeValue(var ch: Char) extends EscapeValue
  case class IntEscapeValue(var n: Int) extends EscapeValue

  class RegExpEnv(var input: String,
                  var nCapturingParens: Int,
                  var global: Boolean,
                  var ignoreCase: Boolean,
                  var multiline: Boolean)
}
