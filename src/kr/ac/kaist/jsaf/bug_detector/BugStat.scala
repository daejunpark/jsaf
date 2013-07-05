/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import java.util.{HashMap => JMap}
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.bug_detector._

class BugStat(lib: Boolean) {
  private var startTime : Long = 0L
  private var endTime   : Long = 0L

  private var totalCount           : Int = 0
  private var warningCount         : Int = 0
  private var typeErrorCount       : Int = 0
  private var referenceErrorCount  : Int = 0
  private var BugCount: Array[Int] = new Array(MAX_BUG_COUNT)

  def increaseBugCounter(bugKind: BugKind, bugType: BugType): Unit = {
    bugType match {
      case Warning => warningCount = warningCount + 1
      case TypeError => typeErrorCount = typeErrorCount + 1
      case ReferenceError => referenceErrorCount = referenceErrorCount + 1
    }
    BugCount(bugKind) += 1
  }

  private def countTotalBugs(): Unit = totalCount = referenceErrorCount + typeErrorCount + warningCount
  private def printDetectingTime() = System.out.println("# Time for bug Detection(s): %.2f".format((endTime - startTime) / 1000000000.0))
  private def divideByZeroCheck(flag: BugType): Float = if (totalCount <= 0) 0 toFloat else 
    (flag match {case ReferenceError => referenceErrorCount; case TypeError => typeErrorCount; case Warning => warningCount}).toFloat/totalCount*100
  private def printTotalBugCount() = {
    countTotalBugs
    System.out.println
    System.out.println("============== Total Count ==============")
    System.out.println("|  ReferenceErrors   : %6d (%6.2f%%) |".format(referenceErrorCount, divideByZeroCheck(ReferenceError))) 
    System.out.println("|  TypeErrors        : %6d (%6.2f%%) |".format(typeErrorCount, divideByZeroCheck(TypeError)))
    System.out.println("|  Warnings          : %6d (%6.2f%%) |".format(warningCount, divideByZeroCheck(Warning)))
    System.out.println("=========================================")
  }
  private def getUnusedCount(): Int = {
    if (lib) BugCount(UnreferencedFunction) + BugCount(UnusedProperty) + BugCount(UnusedVariable)
    else BugCount(UnusedFunction) + BugCount(UnusedProperty) + BugCount(UnusedVariable)
  }
  private def printBugStatistics() = {
    System.out.println("============ Statistics =============")
    System.out.println("|  AbsentRead              : %6d |".format((BugCount(AbsentReadProperty) + BugCount(AbsentReadVariable))))   
    System.out.println("|  BinaryOperator          : %6d |".format((BugCount(BinaryOpSecondType))))
    System.out.println("|  BuiltinWrongArgType     : %6d |".format(BugCount(BuiltinWrongArgType)))
    System.out.println("|  CallConstFunc           : %6d |".format(BugCount(CallConstFunc)))
    System.out.println("|  CallNonConstructor      : %6d |".format(BugCount(CallNonConstructor)))
    System.out.println("|  CallNonFunction         : %6d |".format(BugCount(CallNonFunction)))
    System.out.println("|  ConditionalBranch       : %6d |".format((BugCount(CondBranch))))
    System.out.println("|  ConvertToNumber         : %6d |".format(BugCount(ConvertUndefToNum)))
    System.out.println("|  DefaultValueTypeError   : %6d |".format(BugCount(DefaultValueTypeError)))
    System.out.println("|  FunctionArgSize         : %6d |".format((BugCount(FunctionArgSize))))
    System.out.println("|  GlobalThis              : %6d |".format((BugCount(GlobalThisDefinite) + BugCount(GlobalThisMaybe))))
    System.out.println("|  ImplicitTypeConversion  : %6d |".format((BugCount(ImplicitCalltoString) + BugCount(ImplicitCallvalueOf) + BugCount(ImplicitTypeConvert))))
    System.out.println("|  AccessingNullOrUndef    : %6d |".format((BugCount(ObjectNullOrUndef))))
    System.out.println("|  PrimitiveToObject       : %6d |".format(BugCount(PrimitiveToObject)))
    System.out.println("|  Shadowing               : %6d |".format((BugCount(ShadowedFuncByFunc) + BugCount(ShadowedParamByFunc) + BugCount(ShadowedVarByFunc) + BugCount(ShadowedVarByParam) + BugCount(ShadowedVarByVar))))
    System.out.println("|  UnreachableCode         : %6d |".format(BugCount(UnreachableCode)))
    System.out.println("|  Unused                  : %6d |".format(getUnusedCount))
    System.out.println("|  VaryingTypeArguments    : %6d |".format(BugCount(VaryingTypeArguments)))
    System.out.println("|  WrongThisType           : %6d |".format(BugCount(WrongThisType)))
    System.out.println("=====================================")
  }

  def setStartTime(time: Long): Unit = startTime = time
  def setEndTime(time: Long): Unit = endTime = time

  def reportBugStatistics(quiet: Boolean) = {
    printTotalBugCount
    printBugStatistics
    if (!quiet) printDetectingTime
  }
}
