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
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue


object TIZENtizen extends Tizen {
  private val name = "tizen"
  /* predefined locations */
  val loc_obj = newPredefLoc(name + "Obj")
  val loc_proto = newPredefLoc(name + "Proto")
  val loc_alarm = newPredefLoc(name + "alarm")
  val loc_application = newPredefLoc(name + "application")
  val loc_bluetooth = newPredefLoc(name + "bluetooth")
  val loc_bookmark = newPredefLoc(name + "bookmark")
  val loc_calendar = newPredefLoc(name + "calendar")
  val loc_callhistory = newPredefLoc(name + "callhistory")
  val loc_contact = newPredefLoc(name + "contact")
  val loc_content = newPredefLoc(name + "content")
  val loc_datacontrol = newPredefLoc(name + "datacontrol")
  val loc_datasync = newPredefLoc(name + "datasync")
  val loc_download = newPredefLoc(name + "download")
  val loc_filesystem = newPredefLoc(name + "filesystem")
  val loc_messageport = newPredefLoc(name + "messageport")
  val loc_messaging = newPredefLoc(name + "messaging")
  val loc_networkbearerselection = newPredefLoc(name + "networkbearerselection")
  val loc_nfc = newPredefLoc(name + "nfc")
  val loc_notification = newPredefLoc(name + "notification")
  val loc_package = newPredefLoc(name + "package")
  val loc_power = newPredefLoc(name + "power")
  val loc_push = newPredefLoc(name + "push")
  val loc_seService = newPredefLoc(name + "seService")
  val loc_systeminfo = newPredefLoc(name + "systeminfo")
  val loc_systemsetting = newPredefLoc(name + "systemsetting")
  val loc_time = newPredefLoc(name + "time")

  val loc_err: Loc = newPreDefLoc("WebAPIError", Old)

  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class",                      AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",                      AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance",                AbsConstValue(PropValue(Value(NullTop)))),
    ("AlarmAbsolute",          AbsConstValue(PropValue(ObjectValue(Value(TIZENAlarmAbsolute.loc_cons), F, F, F)))),
    ("AlarmRelative",          AbsConstValue(PropValue(ObjectValue(Value(TIZENAlarmRelative.loc_cons), F, F, F)))),
    ("ApplicationControl",     AbsConstValue(PropValue(ObjectValue(Value(TIZENApplicationControl.loc_cons), F, F, F)))),
    ("ApplicationControlData", AbsConstValue(PropValue(ObjectValue(Value(TIZENApplicationControlData.loc_cons), F, F, F)))),
    ("AttributeFilter",        AbsConstValue(PropValue(ObjectValue(Value(TIZENAttributeFilter.loc_cons), F, F, F)))),
    ("AttributeRangeFilter",   AbsConstValue(PropValue(ObjectValue(Value(TIZENAttributeRangeFilter.loc_cons), F, F, F)))),
    ("BookmarkFolder",         AbsConstValue(PropValue(ObjectValue(Value(TIZENBookmarkFolder.loc_cons), F, F, F)))),
    ("BookmarkItem",           AbsConstValue(PropValue(ObjectValue(Value(TIZENBookmarkItem.loc_cons), F, F, F)))),
    ("CalendarAlarm",          AbsConstValue(PropValue(ObjectValue(Value(TIZENCalendarAlarm.loc_cons), F, F, F)))),
    ("CalendarAttendee",       AbsConstValue(PropValue(ObjectValue(Value(TIZENCalendarAttendee.loc_cons), F, F, F)))),
    ("CalendarEvent",          AbsConstValue(PropValue(ObjectValue(Value(TIZENCalendarEvent.loc_cons), F, F, F)))),
    ("CalendarEventId",        AbsConstValue(PropValue(ObjectValue(Value(TIZENCalendarEventId.loc_cons), F, F, F)))),
    ("CalendarRecurrenceRule", AbsConstValue(PropValue(ObjectValue(Value(TIZENCalendarRecurrenceRule.loc_cons), F, F, F)))),
    ("CalendarTask",           AbsConstValue(PropValue(ObjectValue(Value(TIZENCalendarTask.loc_cons), F, F, F)))),
    ("CompositeFilter",        AbsConstValue(PropValue(ObjectValue(Value(TIZENCompositeFilter.loc_cons), F, F, F)))),
    ("Contact",                AbsConstValue(PropValue(ObjectValue(Value(TIZENContact.loc_cons), F, F, F)))),
    ("ContactAddress",         AbsConstValue(PropValue(ObjectValue(Value(TIZENContactAddress.loc_cons), F, F, F)))),
    ("ContactAnniversary",     AbsConstValue(PropValue(ObjectValue(Value(TIZENContactAnniversary.loc_cons), F, F, F)))),
    ("ContactEmailAddress",    AbsConstValue(PropValue(ObjectValue(Value(TIZENContactEmailAddress.loc_cons), F, F, F)))),
    ("ContactGroup",           AbsConstValue(PropValue(ObjectValue(Value(TIZENContactGroup.loc_cons), F, F, F)))),
    ("ContactName",            AbsConstValue(PropValue(ObjectValue(Value(TIZENContactName.loc_cons), F, F, F)))),
    ("ContactOrganization",    AbsConstValue(PropValue(ObjectValue(Value(TIZENContactOrganization.loc_cons), F, F, F)))),
    ("ContactPhoneNumber",     AbsConstValue(PropValue(ObjectValue(Value(TIZENContactPhoneNumber.loc_cons), F, F, F)))),
    ("ContactRef",             AbsConstValue(PropValue(ObjectValue(Value(TIZENContactRef.loc_cons), F, F, F)))),
    ("ContactWebSite",         AbsConstValue(PropValue(ObjectValue(Value(TIZENContactWebSite.loc_cons), F, F, F)))),
    ("DownloadRequest",        AbsConstValue(PropValue(ObjectValue(Value(TIZENDownloadRequest.loc_cons), F, F, F)))),
    ("Message",                AbsConstValue(PropValue(ObjectValue(Value(TIZENMessage.loc_cons), F, F, F)))),
    ("MessageAttachment",      AbsConstValue(PropValue(ObjectValue(Value(TIZENMessageAttachment.loc_cons), F, F, F)))),
    ("NDEFMessage",            AbsConstValue(PropValue(ObjectValue(Value(TIZENNDEFMessage.loc_cons), F, F, F)))),
    ("NDEFRecord",             AbsConstValue(PropValue(ObjectValue(Value(TIZENNDEFRecord.loc_cons), F, F, F)))),
    ("NDEFRecordMedia",        AbsConstValue(PropValue(ObjectValue(Value(TIZENNDEFRecordMedia.loc_cons), F, F, F)))),
    ("NDEFRecordText",         AbsConstValue(PropValue(ObjectValue(Value(TIZENNDEFRecordText.loc_cons), F, F, F)))),
    ("NDEFRecordURI",          AbsConstValue(PropValue(ObjectValue(Value(TIZENNDEFRecordURI.loc_cons), F, F, F)))),
    ("SimpleCoordinates",      AbsConstValue(PropValue(ObjectValue(Value(TIZENSimpleCoordinates.loc_cons), F, F, F)))),
    ("SortMode",               AbsConstValue(PropValue(ObjectValue(Value(TIZENSortMode.loc_cons), F, F, F)))),
    ("StatusNotification",     AbsConstValue(PropValue(ObjectValue(Value(TIZENStatusNotification.loc_cons), F, F, F)))),
    ("SyncInfo",               AbsConstValue(PropValue(ObjectValue(Value(TIZENSyncInfo.loc_cons), F, F, F)))),
    ("SyncProfileInfo",        AbsConstValue(PropValue(ObjectValue(Value(TIZENSyncProfileInfo.loc_cons), F, F, F)))),
    ("SyncServiceInfo",        AbsConstValue(PropValue(ObjectValue(Value(TIZENSyncServiceInfo.loc_cons), F, F, F)))),
    ("TZDate",                 AbsConstValue(PropValue(ObjectValue(Value(TIZENTZDate.loc_cons), F, F, F)))),
    ("TimeDuration",           AbsConstValue(PropValue(ObjectValue(Value(TIZENTimeDuration.loc_cons), F, F, F)))),
    ("alarm",                  AbsConstValue(PropValue(ObjectValue(Value(loc_alarm), F, F, F)))),
    ("application",            AbsConstValue(PropValue(ObjectValue(Value(loc_application), F, F, F)))),
    ("bluetooth",              AbsConstValue(PropValue(ObjectValue(Value(loc_bluetooth), F, F, F)))),
    ("bookmark",               AbsConstValue(PropValue(ObjectValue(Value(loc_bookmark), F, F, F)))),
    ("calendar",               AbsConstValue(PropValue(ObjectValue(Value(loc_calendar), F, F, F)))),
    ("callhistory",            AbsConstValue(PropValue(ObjectValue(Value(loc_callhistory), F, F, F)))),
    ("contact",                AbsConstValue(PropValue(ObjectValue(Value(loc_contact), F, F, F)))),
    ("content",                AbsConstValue(PropValue(ObjectValue(Value(loc_content), F, F, F)))),
    ("datacontrol",            AbsConstValue(PropValue(ObjectValue(Value(loc_datacontrol), F, F, F)))),
    ("datasync",               AbsConstValue(PropValue(ObjectValue(Value(loc_datasync), F, F, F)))),
    ("download",               AbsConstValue(PropValue(ObjectValue(Value(loc_download), F, F, F)))),
    ("filesystem",             AbsConstValue(PropValue(ObjectValue(Value(loc_filesystem), F, F, F)))),
    ("messageport",            AbsConstValue(PropValue(ObjectValue(Value(loc_messageport), F, F, F)))),
    ("messaging",              AbsConstValue(PropValue(ObjectValue(Value(loc_messaging), F, F, F)))),
    ("networkbearerselection", AbsConstValue(PropValue(ObjectValue(Value(loc_networkbearerselection), F, F, F)))),
    ("nfc",                    AbsConstValue(PropValue(ObjectValue(Value(loc_nfc), F, F, F)))),
    ("notification",           AbsConstValue(PropValue(ObjectValue(Value(loc_notification), F, F, F)))),
    ("package",                  AbsConstValue(PropValue(ObjectValue(Value(loc_package), F, F, F)))),
    ("power",                  AbsConstValue(PropValue(ObjectValue(Value(loc_power), F, F, F)))),
    ("push",                  AbsConstValue(PropValue(ObjectValue(Value(loc_push), F, F, F)))),
    ("seService",             AbsConstValue(PropValue(ObjectValue(Value(loc_seService), F, F, F)))),
    ("systeminfo",             AbsConstValue(PropValue(ObjectValue(Value(loc_systeminfo), F, F, F)))),
    ("systemsetting",          AbsConstValue(PropValue(ObjectValue(Value(loc_systemsetting), F, F, F)))),
    ("time",                   AbsConstValue(PropValue(ObjectValue(Value(loc_time), F, F, F))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class",                      AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto",                      AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(BoolTrue))),
    ("listActivatedFeatures",       AbsBuiltinFunc("tizen.listActivatedFeatures", 0)),
    ("listAvailableFeatures",       AbsBuiltinFunc("tizen.listAvailableFeatures", 0))
  )
  /* global */
  private val prop_global: List[(String, AbsProperty)] = List(
    (name,       AbsConstValue(PropValue(ObjectValue(Value(loc_obj), F, F, F))))
  )

  private val prop_err_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENWebAPIError.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("code",               AbsConstValue(PropValue(Value(StrTop)))),
    ("name",                 AbsConstValue(PropValue(Value(StrTop)))),
    ("message",                 AbsConstValue(PropValue(Value(StrTop))))
  )

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto), (GlobalLoc, prop_global), (loc_err, prop_err_ins)
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("tizen.listActivatedFeatures" -> ()),
      ("tizen.listAvailableFeatures" -> ())  */
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {Map()}
  override def getDefMap(): Map[String, AccessFun] = {Map()}
  override def getUseMap(): Map[String, AccessFun] = {Map()}
}