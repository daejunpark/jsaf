/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ***************************************************************************** */

package kr.ac.kaist.jsaf.utils

import scala.collection.mutable.{HashSet => MHashSet, HashMap => MHashMap}

package object regexp {
  type CharSet = MHashSet[Char]
  type Continuation = (RegExpState) => MatchResult
  type Matcher = (RegExpState, Continuation) => MatchResult
  type MatchFunc = (String, Int) => MatchResult
  type AssertionTester = (RegExpState) => Boolean

  class RegExpState(var endIndex: Int, var captures: MHashMap[Int, Option[String]])
  type MatchResult = Option[RegExpState]

  abstract class EscapeValue
  case class CharEscapeValue(var ch: Char) extends EscapeValue
  case class IntEscapeValue(var n: Int) extends EscapeValue

  class RegExpEnv(var input: String,
                  var nCapturingParens: Int,
                  var global: Boolean,
                  var ignoreCase: Boolean,
                  var multiline: Boolean)
}
