/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import kr.ac.kaist.jsaf.analysis.typing.CallContext

class BugOption(defaultForUser: Boolean = true) {
  ////////////////////////////////////////////////////////////////////////////////
  // Settings
  ////////////////////////////////////////////////////////////////////////////////
  val soundnessLevel:                           Array[SoundnessLevel] = new Array(MAX_BUG_COUNT)
  val contextSensitive:                         Array[CallContext.sensitivityFlagType] = new Array(MAX_BUG_COUNT)

  var AbsentReadProperty_PropertyMustExistInEveryState          = false
  var AbsentReadProperty_PropertyMustExistInEveryLocation       = false
  var AbsentReadProperty_PropertyMustExistDefinitely            = false
  var AbsentReadProperty_CheckAbstractIndexValue                = false

  var AbsentReadVariable_VariableMustExistInEveryState          = false
  var AbsentReadVariable_VariableMustExistDefinitely            = false

  var BinaryOpSecondType_OperandMustBeCorrectInEveryState       = false
  var BinaryOpSecondType_OperandMustBeCorrectInEveryLocation    = false
  var BinaryOpSecondType_OperandMustBeCorrectDefinitely         = false

  var BuiltinWrongType_TypeMustBeCorrectInEveryState            = false
  var BuiltinWrongType_TypeMustBeCorrectDefinitely              = false
  var BuiltinWrongType_CheckObjectType                          = true
  var BuiltinWrongType_CheckFunctionType                        = true

  var CallNonFunction_FunctionMustBeCallableInEveryState        = false
  var CallNonFunction_FunctionMustBeCallableForEveryLocation    = false
  var CallNonFunction_FunctionMustBeCallableDefinitely          = false

  var CondBranch_ConditionMustBeTrueOrFalseInEveryState         = false
  var CondBranch_ConditionMustBeTrueOrFalseDefinitely           = false

  var ConvertUndefToNum_UndefMustBeConvertedInEveryState        = false
  var ConvertUndefToNum_VariableMustHaveUndefinedOnly           = true
  var ConvertUndefToNum_ToNumberMustBeCalledDefinitely          = true

  var ImplicitTypeConvert_MustBeConvertedInEveryState           = false
  var ImplicitTypeConvert_MustBeConvertedDefinitely             = true
  var ImplicitTypeConvert_CheckNullAndUndefined                 = true
  var ImplicitTypeConvert_CheckStringAndNumber                  = false
  var ImplicitTypeConvert_CheckBooleanAndUndefined              = true
  var ImplicitTypeConvert_CheckBooleanAndNull                   = true
  var ImplicitTypeConvert_CheckBooleanAndNumber                 = true
  var ImplicitTypeConvert_CheckBooleanAndString                 = true
  var ImplicitTypeConvert_CheckObjectAndNumber                  = false
  var ImplicitTypeConvert_CheckObjectAndString                  = false
  var ImplicitTypeConvert_CheckObjectAndBoolean                 = false

  var NullOrUndefined_BugMustExistInEveryState                  = false
  var NullOrUndefined_OnlyWhenPrimitive                         = true
  var NullOrUndefined_OnlyNullOrUndefined                       = true

  var PrimitiveToObject_PrimitiveMustBeConvertedInEveryState    = false
  var PrimitiveToObject_PrimitiveMustBeConvertedDefinitely      = false
  var PrimitiveToObject_CheckEvenThoughPrimitiveIsString        = false

  var VaryingTypeArguments_CheckUndefined                       = false

  ////////////////////////////////////////////////////////////////////////////////
  // Constructor
  ////////////////////////////////////////////////////////////////////////////////
  {
    if(defaultForUser) setToDefaultForUser()
    else setToDefaultForDeveloper()
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Default settings
  ////////////////////////////////////////////////////////////////////////////////
  // For user
  def setToDefaultForUser(): Unit = {
    for(i <- 0 until MAX_BUG_COUNT) {
      soundnessLevel(i) = SOUNDNESS_LEVEL_LOW
      contextSensitive(i) = CallContext._MOST_SENSITIVE
    }

    // AbsentReadProperty
    AbsentReadProperty_PropertyMustExistInEveryState = false
    AbsentReadProperty_PropertyMustExistInEveryLocation = false
    AbsentReadProperty_PropertyMustExistDefinitely = false
    AbsentReadProperty_CheckAbstractIndexValue = false

    // AbsentReadVariable
    AbsentReadVariable_VariableMustExistInEveryState = false
    AbsentReadVariable_VariableMustExistDefinitely = false

    // BinaryOpSecondType
    BinaryOpSecondType_OperandMustBeCorrectInEveryState = false
    BinaryOpSecondType_OperandMustBeCorrectInEveryLocation = false
    BinaryOpSecondType_OperandMustBeCorrectDefinitely = false

    // BuiltinWrongType
    BuiltinWrongType_TypeMustBeCorrectInEveryState = false
    BuiltinWrongType_TypeMustBeCorrectDefinitely = false
    BuiltinWrongType_CheckObjectType = true
    BuiltinWrongType_CheckFunctionType = true

    // CallNonFunction
    CallNonFunction_FunctionMustBeCallableInEveryState = false
    CallNonFunction_FunctionMustBeCallableForEveryLocation = false
    CallNonFunction_FunctionMustBeCallableDefinitely = false

    // CondBranch
    CondBranch_ConditionMustBeTrueOrFalseInEveryState = false
    CondBranch_ConditionMustBeTrueOrFalseDefinitely = false

    // ConvertUndefToNum
    ConvertUndefToNum_UndefMustBeConvertedInEveryState = false
    ConvertUndefToNum_VariableMustHaveUndefinedOnly = true
    ConvertUndefToNum_ToNumberMustBeCalledDefinitely = true

    // ImplicitTypeConvert
    ImplicitTypeConvert_MustBeConvertedInEveryState = false
    ImplicitTypeConvert_MustBeConvertedDefinitely = true
    ImplicitTypeConvert_CheckNullAndUndefined = true
    ImplicitTypeConvert_CheckStringAndNumber = false
    ImplicitTypeConvert_CheckBooleanAndUndefined = true
    ImplicitTypeConvert_CheckBooleanAndNull = true
    ImplicitTypeConvert_CheckBooleanAndNumber = true
    ImplicitTypeConvert_CheckBooleanAndString = true
    ImplicitTypeConvert_CheckObjectAndNumber = false
    ImplicitTypeConvert_CheckObjectAndString = false
    ImplicitTypeConvert_CheckObjectAndBoolean = false

    // NullOrUndefined
    val nullOrUndefinedSoundness = SOUNDNESS_LEVEL_LOW
    soundnessLevel(ObjectNull) = nullOrUndefinedSoundness
    soundnessLevel(ObjectNullOrUndef) = nullOrUndefinedSoundness
    soundnessLevel(ObjectUndef) = nullOrUndefinedSoundness
    NullOrUndefined_BugMustExistInEveryState = false
    NullOrUndefined_OnlyWhenPrimitive = true
    NullOrUndefined_OnlyNullOrUndefined = true

    // PrimitiveToObject
    PrimitiveToObject_PrimitiveMustBeConvertedInEveryState = false
    PrimitiveToObject_PrimitiveMustBeConvertedDefinitely = false
    PrimitiveToObject_CheckEvenThoughPrimitiveIsString = false

    // VaryingTypeArguments
    VaryingTypeArguments_CheckUndefined = false
  }

  // For SAFE developer
  def setToDefaultForDeveloper(): Unit = {
    for(i <- 0 until MAX_BUG_COUNT) {
      soundnessLevel(i) = SOUNDNESS_LEVEL_HIGH
      contextSensitive(i) = CallContext._MOST_SENSITIVE
    }

    // AbsentReadProperty
    AbsentReadProperty_PropertyMustExistInEveryState = true
    AbsentReadProperty_PropertyMustExistInEveryLocation = true
    AbsentReadProperty_PropertyMustExistDefinitely = true
    AbsentReadProperty_CheckAbstractIndexValue = true

    // AbsentReadVariable
    AbsentReadVariable_VariableMustExistInEveryState = true
    AbsentReadVariable_VariableMustExistDefinitely = true

    // BinaryOpSecondType
    BinaryOpSecondType_OperandMustBeCorrectInEveryState = true
    BinaryOpSecondType_OperandMustBeCorrectInEveryLocation = true
    BinaryOpSecondType_OperandMustBeCorrectDefinitely = true

    // BuiltinWrongType
    BuiltinWrongType_TypeMustBeCorrectInEveryState = true
    BuiltinWrongType_TypeMustBeCorrectDefinitely = true
    BuiltinWrongType_CheckObjectType = true
    BuiltinWrongType_CheckFunctionType = true

    // CallNonFunction
    CallNonFunction_FunctionMustBeCallableInEveryState = true
    CallNonFunction_FunctionMustBeCallableForEveryLocation = true
    CallNonFunction_FunctionMustBeCallableDefinitely = true

    // CondBranch
    CondBranch_ConditionMustBeTrueOrFalseInEveryState = true
    CondBranch_ConditionMustBeTrueOrFalseDefinitely = true

    // ConvertUndefToNum
    ConvertUndefToNum_UndefMustBeConvertedInEveryState = true
    ConvertUndefToNum_VariableMustHaveUndefinedOnly = false
    ConvertUndefToNum_ToNumberMustBeCalledDefinitely = false

    // ImplicitTypeConvert
    ImplicitTypeConvert_MustBeConvertedInEveryState = true
    ImplicitTypeConvert_MustBeConvertedDefinitely = false
    ImplicitTypeConvert_CheckNullAndUndefined = true
    ImplicitTypeConvert_CheckStringAndNumber = true
    ImplicitTypeConvert_CheckBooleanAndUndefined = true
    ImplicitTypeConvert_CheckBooleanAndNull = true
    ImplicitTypeConvert_CheckBooleanAndNumber = true
    ImplicitTypeConvert_CheckBooleanAndString = true
    ImplicitTypeConvert_CheckObjectAndNumber = true
    ImplicitTypeConvert_CheckObjectAndString = true
    ImplicitTypeConvert_CheckObjectAndBoolean = true

    // NullOrUndefined
    NullOrUndefined_BugMustExistInEveryState = true
    NullOrUndefined_OnlyWhenPrimitive = false
    NullOrUndefined_OnlyNullOrUndefined = false

    // PrimitiveToObject
    PrimitiveToObject_PrimitiveMustBeConvertedInEveryState = true
    PrimitiveToObject_PrimitiveMustBeConvertedDefinitely = true
    PrimitiveToObject_CheckEvenThoughPrimitiveIsString = true

    // VaryingTypeArguments
    VaryingTypeArguments_CheckUndefined = true
  }
}
