/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.cfg.CFGExpr
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}

object TIZENSystemInfoDeviceCapability extends Tizen {
  private val name = "SystemInfoDeviceCapability"
  /* predefined locations */
  val loc_obj = newPredefLoc(name + "Obj")
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto)
  )
  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("bluetooth", AbsConstValue(PropValue(Value(BoolTop)))),
    ("nfc", AbsConstValue(PropValue(Value(BoolTop)))),
    ("multiTouchCount", AbsConstValue(PropValue(Value(NumTop)))),
    ("inputKeyboard", AbsConstValue(PropValue(Value(BoolTop)))),
    ("wifi", AbsConstValue(PropValue(Value(BoolTop)))),
    ("wifiDirect", AbsConstValue(PropValue(Value(BoolTop)))),
    ("openglesVersion1_1", AbsConstValue(PropValue(Value(BoolTop)))),
    ("openglesVersion2_0", AbsConstValue(PropValue(Value(BoolTop)))),
    ("fmRadio", AbsConstValue(PropValue(Value(BoolTop)))),
    ("platformVersion", AbsConstValue(PropValue(Value(StrTop)))),
    ("webApiVersion", AbsConstValue(PropValue(Value(StrTop)))),
    ("nativeApiVersion", AbsConstValue(PropValue(Value(StrTop)))),
    ("platformName", AbsConstValue(PropValue(Value(StrTop)))),
    ("cameraFront", AbsConstValue(PropValue(Value(BoolTop)))),
    ("cameraFrontFlash", AbsConstValue(PropValue(Value(BoolTop)))),
    ("cameraBack", AbsConstValue(PropValue(Value(BoolTop)))),
    ("cameraBackFlash", AbsConstValue(PropValue(Value(BoolTop)))),
    ("location", AbsConstValue(PropValue(Value(BoolTop)))),
    ("locationGps", AbsConstValue(PropValue(Value(BoolTop)))),
    ("locationWps", AbsConstValue(PropValue(Value(BoolTop)))),
    ("microphone", AbsConstValue(PropValue(Value(BoolTop)))),
    ("usbHost", AbsConstValue(PropValue(Value(BoolTop)))),
    ("usbAccessory", AbsConstValue(PropValue(Value(BoolTop)))),
    ("screenOutputRca", AbsConstValue(PropValue(Value(BoolTop)))),
    ("screenOutputhHdmi", AbsConstValue(PropValue(Value(BoolTop)))),
    ("platformCoreCpuArch", AbsConstValue(PropValue(Value(StrTop)))),
    ("platformCoreFpuArch", AbsConstValue(PropValue(Value(StrTop)))),
    ("sipVoip", AbsConstValue(PropValue(Value(BoolTop)))),
    ("duid", AbsConstValue(PropValue(Value(StrTop)))),
    ("speechRecognition", AbsConstValue(PropValue(Value(BoolTop)))),
    ("accelerometer", AbsConstValue(PropValue(Value(BoolTop)))),
    ("barometer", AbsConstValue(PropValue(Value(BoolTop)))),
    ("gyroscope", AbsConstValue(PropValue(Value(BoolTop)))),
    ("magnetometer", AbsConstValue(PropValue(Value(BoolTop)))),
    ("proximity", AbsConstValue(PropValue(Value(BoolTop))))

  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue)))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(

    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}