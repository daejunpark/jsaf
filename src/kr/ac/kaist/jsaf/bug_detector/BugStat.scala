/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import java.util.{HashMap => JMap}
import kr.ac.kaist.jsaf.nodes_util.Span
import kr.ac.kaist.jsaf.bug_detector._

class BugStat() {
  private var startTime : Long = 0L
  private var endTime   : Long = 0L

  private var warningCount        : Int = 0
  private var typeErrorCount      : Int = 0
  private var referenceErrorCount : Int = 0

  private var AbsentReadProperty:Int = 0 
  private var AbsentReadVariable:Int = 0   
  private var BinaryOpIn:Int = 0          
  private var BinaryOpInstanceOf1:Int = 0
  private var BinaryOpInstanceOf2:Int = 0
  private var BinaryOpInstanceOf3:Int = 0
  private var BuiltinArgSizeFew:Int = 0
  private var BuiltinArgSizeMany:Int = 0
  private var BuiltinWrongType:Int = 0
  private var CallConstFunc:Int = 0
  private var CallNonConstructor:Int = 0
  private var CallNonFunction:Int = 0
  private var CondBranchTrue:Int = 0
  private var CondBranchFalse:Int = 0
  private var ConvertUndeftoNum:Int = 0
  private var DefaultValueTypeError:Int = 0
  private var GlobalThisDefinite:Int = 0  
  private var GlobalThisMaybe:Int = 0    
  private var ImplicitCalltoString:Int = 0
  private var ImplicitCallvalueOf:Int = 0
  private var ObjectNull:Int = 0        
  private var ObjectNullOrUndef:Int = 0  
  private var ObjectUndef:Int = 0       
  private var PrimitiveToObject:Int = 0  
  private var ShadowedFuncByFunc:Int = 0
  private var ShadowedParamByFunc:Int = 0
  private var ShadowedVarByFunc:Int = 0
  private var ShadowedVarByParam:Int = 0
  private var ShadowedVarByVar:Int = 0
  private var UnreachableCode:Int = 0   
  private var UnusedFunction:Int = 0  
  private var UnusedProperty:Int = 0    
  private var UnusedVariable:Int = 0  
  private var VaryingTypeArguments:Int = 0 
  private var WrongThisType:Int = 0    

  def increaseBugCounter(bugKind: BId, bugType: BugType): Unit = {
    bugKind match {
      case 1 => AbsentReadProperty = AbsentReadProperty + 1 
      case 2 => AbsentReadVariable = AbsentReadVariable + 1   
      case 3 => BinaryOpIn = BinaryOpIn + 1          
      case 4 => BinaryOpInstanceOf1 = BinaryOpInstanceOf1 + 1
      case 5 => BinaryOpInstanceOf2 = BinaryOpInstanceOf2 + 1
      case 6 => BinaryOpInstanceOf3 = BinaryOpInstanceOf3 + 1
      case 7 => BuiltinArgSizeFew = BuiltinArgSizeFew + 1
      case 8 => BuiltinArgSizeMany = BuiltinArgSizeMany + 1
      case 9 => BuiltinWrongType = BuiltinWrongType + 1
      case 10 => CallConstFunc = CallConstFunc + 1
      case 11 => CallNonConstructor = CallNonConstructor + 1
      case 12 => CallNonFunction = CallNonFunction + 1
      case 13 => CondBranchTrue = CondBranchTrue + 1
      case 14 => CondBranchFalse = CondBranchFalse + 1
      case 15 => ConvertUndeftoNum = ConvertUndeftoNum + 1
      case 16 => DefaultValueTypeError = DefaultValueTypeError + 1
      case 17 => GlobalThisDefinite = GlobalThisDefinite + 1  
      case 18 => GlobalThisMaybe = GlobalThisMaybe + 1    
      case 19 => ImplicitCalltoString = ImplicitCalltoString + 1
      case 20 => ImplicitCallvalueOf = ImplicitCallvalueOf + 1
      case 21 => ObjectNull = ObjectNull + 1        
      case 22 => ObjectNullOrUndef = ObjectNullOrUndef + 1  
      case 23 => ObjectUndef = ObjectUndef + 1       
      case 24 => PrimitiveToObject = PrimitiveToObject + 1  
      case 25 => ShadowedFuncByFunc = ShadowedFuncByFunc + 1
      case 26 => ShadowedParamByFunc = ShadowedParamByFunc + 1
      case 27 => ShadowedVarByFunc = ShadowedVarByFunc + 1
      case 28 => ShadowedVarByParam = ShadowedVarByParam + 1
      case 29 => ShadowedVarByVar = ShadowedVarByVar + 1
      case 30 => UnreachableCode = UnreachableCode + 1   
      case 31 => UnusedFunction = UnusedFunction + 1  
      case 32 => UnusedProperty = UnusedProperty + 1    
      case 33 => UnusedVariable = UnusedVariable + 1  
      case 34 => VaryingTypeArguments = VaryingTypeArguments + 1 
      case 35 => WrongThisType = WrongThisType + 1    
    }; bugType match {
      case 1 => warningCount = warningCount + 1
      case 2 => typeErrorCount = typeErrorCount + 1
      case 3 => referenceErrorCount = referenceErrorCount + 1
    }
  }

  private def printDetectingTime() = System.out.println("# Time for bug Detection(s): %.2f".format((endTime - startTime) / 1000000000.0))
  private def printTotalBugCount() = {
    System.out.println("# ReferenceErrors(#) : " + referenceErrorCount)
    System.out.println("# TypeErrors(#)      : " + typeErrorCount)
    System.out.println("# Warnings(#)        : " + warningCount)
    System.out.println("\n")
  }
  private def printBugStatistics() = {
    System.out.println("\n* Bug detector statistics *")
    System.out.println("#  AbsentRead              : " + (AbsentReadProperty + AbsentReadVariable))   
    System.out.println("#  BinaryOperator          : " + (BinaryOpIn + BinaryOpInstanceOf1 + BinaryOpInstanceOf2 + BinaryOpInstanceOf3))
    System.out.println("#  BuiltinArgSize          : " + (BuiltinArgSizeFew + BuiltinArgSizeMany))
    System.out.println("#  BuiltinWrongType        : " + BuiltinWrongType)
    System.out.println("#  CallConstFunc           : " + CallConstFunc)
    System.out.println("#  CallNonConstructor      : " + CallNonConstructor)
    System.out.println("#  CallNonFunction         : " + CallNonFunction)
    System.out.println("#  ConditionalBranch       : " + (CondBranchTrue + CondBranchFalse))
    System.out.println("#  ConvertToNumber         : " + ConvertUndeftoNum)
    System.out.println("#  DefaultValueTypeError   : " + DefaultValueTypeError)
    System.out.println("#  GlobalThisDefinite      : " + (GlobalThisDefinite +  GlobalThisMaybe))    
    System.out.println("#  ImplicitTypeConversion  : " + (ImplicitCalltoString + ImplicitCallvalueOf))
    System.out.println("#  AccessingNullOrUndef    : " + (ObjectNull + ObjectNullOrUndef + ObjectUndef))       
    System.out.println("#  PrimitiveToObject       : " + PrimitiveToObject)  
    System.out.println("#  ShadowingVarPropFunc    : " + (ShadowedFuncByFunc + ShadowedParamByFunc + ShadowedVarByFunc + ShadowedVarByParam + ShadowedVarByVar))
    System.out.println("#  UnreachableCode         : " + UnreachableCode)   
    System.out.println("#  UnusedVarPropFunc       : " + (UnusedFunction + UnusedProperty + UnusedVariable))  
    System.out.println("#  VaryingTypeArguments    : " + VaryingTypeArguments) 
    System.out.println("#  WrongThisType           : " + WrongThisType)    
  }

  def setStartTime(time: Long): Unit = startTime = time
  def setEndTime(time: Long): Unit = endTime = time

  def reportBugStatistics(quiet: Boolean) = {
    printTotalBugCount
    if (!quiet) {
      printBugStatistics
      printDetectingTime
    }
  }
}
