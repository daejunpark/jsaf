/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap
import scala.collection.mutable.{Set => MSet}
import scala.collection.mutable.{HashSet => MHashSet}
import kr.ac.kaist.jsaf.analysis.typing.domain._

object Config {
  /**
   * Verbose output.
   * If turned on, all built-in objects and predefined properties of #global will be
   * included when printing analysis results.
   */
  var verbose = false
  def setVerbose(flag: Boolean) = verbose = flag
  val globalVerboseProp: MSet[String] = MHashSet()

  /**
   * Test mode.
   * If turned on, special values, such as value bottom and top, will be provided as
   * predefined global variables.
   */
  var testMode = false
  def setTestMode(flag: Boolean) = testMode = flag
  val testModeProp = HashMap[String, Value](
    ("__BOT", ValueBot),
    ("__TOP", Value(PValueTop)),
    ("__UInt", Value(UInt)),
    ("__Global", Value(GlobalLoc)),
    ("__BoolTop", Value(BoolTop)),
    ("__NumTop", Value(NumTop)),
    ("__StrTop", Value(StrTop)),
    ("__ObjConstLoc", Value(ObjConstLoc)),
    ("__ArrayConstLoc", Value(ArrayConstLoc)),
    ("__RefErrLoc", Value(RefErrLoc)),
    ("__RangeErrLoc", Value(RangeErrLoc)),
    ("__TypeErrLoc", Value(TypeErrLoc)),
    ("__RefErrProtoLoc", Value(RefErrProtoLoc)),
    ("__RangeErrProtoLoc", Value(RangeErrProtoLoc)),
    ("__TypeErrProtoLoc", Value(TypeErrProtoLoc)),
    ("__ErrProtoLoc", Value(ErrProtoLoc))
  )
  
  /** Library mode.
   * 
   */
  var libMode = false
  def setLibMode(flag: Boolean) = libMode = flag
  val libModeProp = HashMap[String, Value](("<>TopVal<>", ValuePseudoTop))
  
  /**
   * Assert flag.
   * If turned on, the program apply the assert semantics while analyzing JavaScript code.
   */
  var assertMode = true
  def setAssertMode(flag: Boolean) = assertMode = flag

  /**
   * PreAnalysis flag.
   */
  var preAnalysis = false
  def setPreAnalysisMode(flag: Boolean) = preAnalysis = flag

  /**
   * Context-sensitivity mode
   */
  val Context_Insensitive = 0
  val Context_OneCallsite = 1
  val Context_OneObject = 2
  val Context_OneObjectTAJS = 3
  var contextSensitivityMode = Context_OneObjectTAJS
  def setContextSensitivityMode(mode: Int) = contextSensitivityMode = mode

  /**
   * Unsound flag.
   */
  var unsoundMode = false
  def setUnsoundMode(flag: Boolean) = unsoundMode = flag
  
  /**
   * Debug flag for internal checking and statistics.
   */
  val DEBUG = true

  var compare = false
  def setCompareMode(flag: Boolean) = compare = flag
  def setPreTyping(state: State) = preTyping = state
  var preTyping = StateBot
  var preDebug = false
  def setPreDebug(flag: Boolean) = preDebug = flag
}
