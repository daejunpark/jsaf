/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.typing.domain._
import scala.collection.immutable.HashSet
import kr.ac.kaist.jsaf.analysis.typing.Helper
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T, _}

object TizenHelper {
  def instanceOf(h: Heap, v_1: Value, v_2: Value): (Value, Set[WebAPIException]) = {
    val lset_1 = v_1._2
    val lset_2 = v_2._2
    val lset_3 = lset_2.filter((l) => T <= Helper.HasInstance(h, l))
    val v_proto = lset_3.foldLeft(ValueBot)((v, l) => v + Helper.Proto(h,l,OtherStrSingle("prototype")))
    val lset_4 = v_proto._2
    val lset_5 = lset_2.filter((l) => F <= Helper.HasInstance(h, l))
    val b_1 = lset_1.foldLeft[Value](ValueBot)((v_1, l_1) =>
      lset_4.foldLeft[Value](v_1)((v_2, l_2) => v_2 + Helper.inherit(h, l_1, l_2)))
    val b_2 =
      if ((v_1._1 </ PValueBot) && !(lset_4.isEmpty))
        Value(F)
      else
        Value(BoolBot)
    val es =
      if ((v_2._1 </ PValueBot) || !(lset_5.isEmpty) || (v_proto._1 </ PValueBot))
        Set[WebAPIException](TypeMismatchError)
      else
        TizenHelper.TizenExceptionBot
    val b = b_1 + b_2
    (b, es)
  }
  def addCallbackHandler(h: Heap, s: AbsString, v_fun: Value, args: Value): Heap = {
    val callback_table = h(TizenCallbackTableLoc)
    val callbackarg_table = h(TizenCallbackArgTableLoc)
    val propv_fun = PropValue(v_fun)
    val propv_args = PropValue(args)
    val callback_list = s match {
      case StrTop | OtherStr => List("#NOARGCB", "#STRCB", "#NUMCB", "#ERRCB", "#APPINFOCB", "#BTDEVCB", "#BTDEVARRCB", "#CALITEMARRCB",
        "#CHENTRYARRCB", "#CONTACTARRCB", "#PERSONARRCB", "#CONTENTCB", "#MSGARRCB", "#MSGCONVARRCB", "#MSGFOLDERARRCB",
        "#PKGINFOCB", "#READERCB", "#APPINFOARRSUCCESSCB", "#FINDAPPCTRLSUCCESSCB", "#APPCONTEXTARRAYSUCCESSCB",
        "#APPCTRLDATAARRAYREPLYCB.onsuccess", "#BTSOCKETSUCCESSCB", "#BTSERVSUCCESSCB", "#CALEVENTARRSUCCESSCB", "#CALARRSUCCESSCB",
        "#CALCHANGECB.onitemsremoved", "#ADDRBOOKARRSUCCESSCB", "#ADDRBOOKCHANGECB.oncontactsremoved", "#PERSONSCHANGECB.onpersonsremoved",
        "#CONTENTARRSUCESSCB", "#CONTENTDIRARRSUCCESSCB", "#DATACTRLERRCB", "#DATACTRLINSERTSUCCESSCB", "#DATACTRLSELECTSUCCESSCB",
        "#DATACTRLGETVALSUCCESSCB", "#SYNCPROGRESSCB.onprogress", "#SYNCPROGRESSCB.onfailed", "#DOWNLOADCB.onprogress", "#DOWNLOADCB.oncompleted",
        "#DOWNLOADCB.onfailed", "#FILESUCCESSCB", "#FILESYSSTORARRSUCCESSCB", "#FILESTREAMSUCCESSCB", "#FILEARRSUCCESSCB", "#MSGPORTCB",
        "#MSGRECIPIENTSCB", "#MSGBODYSUCCESSCB", "#MSGATTACHMENTSUCCESSCB", "#MSGSERVARRSUCCESSCB", "#NFCTAGDETECTCB.onattach", "#NFCPEERDETECTCB.onattach",
        "#NDEFMSGREADCB", "#BYTEARRSUCCESSCB", "#SETYPECHANGECB", "#PKGINFOARRSUCCESSCB", "#PKGPROGRESSCB.onprogress", "#SCREENSTATECHANGECB",
        "#PUSHNOTICB", "#READERARRSUCCESSCB", "#SESSIONSUCCESSCB", "#CHANNELSUCCESSCB", "#TRANSMITSUCCESSCB", "#SYSINFOPROPSUCCESSCB")
      case OtherStrSingle(s_ev) =>
        if (isNoArgCBAttribute(s_ev)) List("#NOARGCB")
        else if (isStrCBAttribute(s_ev)) List("#STRCB")
        else if (isNumCBAttribute(s_ev)) List("#NUMCB")
        else if (isAppInfoCBAttribute(s_ev)) List("#APPINFOCB")
        else if (isBTDevCBAttribute(s_ev))List("#BTDEVCB")
        else if (isBTDevArrCBAttribute(s_ev))List("#BTDEVARRCB")
        else if (isCalItemArrCBAttribute(s_ev))List("#CALITEMARRCB")
        else if (isCHEntryArrCBAttribute(s_ev))List("#CHENTRYARRCB")
        else if (isContactArrCBAttribute(s_ev))List("#CONTACTARRCB")
        else if (isPersonArrCBAttribute(s_ev))List("#PERSONARRCB")
        else if (isContentCBAttribute(s_ev))List("#CONTENTCB")
        else if (isMsgArrCBAttribute(s_ev))List("#MSGARRCB")
        else if (isMsgConvArrCBAttribute(s_ev))List("#MSGCONVARRCB")
        else if (isMsgFolderArrCBAttribute(s_ev))List("#MSGFOLDERARRCB")
        else if (isPkgInfoCBAttribute(s_ev))List("#PKGINFOCB")
        else if (isReaderCBAttribute(s_ev))List("#READERCB")
        else if (s_ev == "errorCB")List("#ERRCB")
        else if (s_ev == "AppInfoArraySuccessCB")List("#APPINFOARRSUCCESSCB")
        else if (s_ev == "FindAppCtrlSuccessCB")List("#FINDAPPCTRLSUCCESSCB")
        else if (s_ev == "AppContextArraySuccessCB")List("#APPCONTEXTARRAYSUCCESSCB")
        else if (s_ev == "AppCtrlDataArrayReplyCB.onsuccess")List("#APPCTRLDATAARRAYREPLYCB")
        else if (s_ev == "BTSocketSuccessCB")List("#BTSOCKETSUCCESSCB")
        else if (s_ev == "BTServicdSuccessCB")List("#BTSERVSUCCESSCB")
        else if (s_ev == "CalEventArrSuccessCB")List("#CALEVENTARRSUCCESSCB")
        else if (s_ev == "CalArrSuccessCB")List("#CALARRSUCCESSCB")
        else if (s_ev == "CalChangeCB.onitemsremoved")List("#CALCHANGECB.onitemsremoved")
        else if (s_ev == "AddrBookArrSuccessCB")List("#ADDRBOOKARRSUCCESSCB")
        else if (s_ev == "AddrBookChangeCB.oncontactsremoved")List("#ADDRBOOKCHANGECB.oncontactsremoved")
        else if (s_ev == "PersonsChangeCB.onpersonsremoved")List("#PERSONSCHANGECB.onpersonsremoved")
        else if (s_ev == "ContentArrSuccessCB")List("#CONTENTARRSUCESSCB")
        else if (s_ev == "ContentDirArraySuccessCB")List("#CONTENTDIRARRSUCCESSCB")
        else if (s_ev == "DataCtrlErrCB")List("#DATACTRLERRCB")
        else if (s_ev == "DataCtrlInsertSuccessCB")List("#DATACTRLINSERTSUCCESSCB")
        else if (s_ev == "DataCtrlSelectSuccessCB")List("#DATACTRLSELECTSUCCESSCB")
        else if (s_ev == "DataCtrlGetValSuccessCB")List("#DATACTRLGETVALSUCCESSCB")
        else if (s_ev == "SyncProgressCB.onprogress")List("#SYNCPROGRESSCB.onprogress")
        else if (s_ev == "SyncProgressCB.onfailed")List("#SYNCPROGRESSCB.onfailed")
        else if (s_ev == "DownloadCB.onprogress")List("#DOWNLOADCB.onprogress")
        else if (s_ev == "DownloadCB.oncompleted")List("#DOWNLOADCB.oncompleted")
        else if (s_ev == "DownloadCB.onfailed")List("#DOWNLOADCB.onfailed")
        else if (s_ev == "FileSuccessCB")List("#FILESUCCESSCB")
        else if (s_ev == "FileSystemStorArrSuccessCB")List("#FILESYSSTORARRSUCCESSCB")
        else if (s_ev == "FileSystemStorSuccessCB")List("#APPINFOARRSUCCESSCB")
        else if (s_ev == "FileStreamSuccessCB")List("#FILESTREAMSUCCESSCB")
        else if (s_ev == "FileArrSuccessCB")List("#FILEARRSUCCESSCB")
        else if (s_ev == "MsgPortCB")List("#MSGPORTCB")
        else if (s_ev == "MsgRecipientsCB")List("#MSGRECIPIENTSCB")
        else if (s_ev == "MsgBodySuccessCB")List("#MSGBODYSUCCESSCB")
        else if (s_ev == "MsgAttachmentSuccessCB")List("#MSGATTACHMENTSUCCESSCB")
        else if (s_ev == "MsgServiceArrSuccessCB")List("#MSGSERVARRSUCCESSCB")
        else if (s_ev == "NFCTagDetectCB.onattach")List("#NFCTAGDETECTCB.onattach")
        else if (s_ev == "NFCPeerDetectCB.onattach")List("#NFCPEERDETECTCB.onattach")
        else if (s_ev == "NDEFMessageReadCB")List("#NDEFMSGREADCB")
        else if (s_ev == "ByteArrSuccessCB")List("#BYTEARRSUCCESSCB")
        else if (s_ev == "SETypeChangeCB")List("#SETYPECHANGECB")
        else if (s_ev == "PkgInfoArrSuccessCB")List("#PKGINFOARRSUCCESSCB")
        else if (s_ev == "PkgProgressCB.onprogress")List("#PKGPROGRESSCB.onprogress")
        else if (s_ev == "ScreenStateChangeCB")List("#SCREENSTATECHANGECB")
        else if (s_ev == "PushNotiCB")List("#PUSHNOTICB")
        else if (s_ev == "ReaderArrSuccessCB")List("#READERARRSUCCESSCB")
        else if (s_ev == "SessionSuccessCB")List("#SESSIONSUCCESSCB")
        else if (s_ev == "ChannelSuccessCB")List("#CHANNELSUCCESSCB")
        else if (s_ev == "TransmitSuccessCB")List("#TRANSMITSUCCESSCB")
        else if (s_ev == "SystemInfoPropSuccessCB")List("#SYSINFOPROPSUCCESSCB")
        else List()
      case NumStrSingle(_) => /* Error ?*/ List()
      case NumStr => /* Error ?*/ List()
      case StrBot => List()
    }
    val (o_fun, o_args) = callback_list.foldLeft((callback_table, callbackarg_table))((o, s_ev) =>
      (o._1.update(s_ev, o._1(s_ev)._1 + propv_fun), o._2.update(s_ev, o._2(s_ev)._1 + propv_args))
    )
    h.update(TizenCallbackTableLoc, o_fun).update(TizenCallbackArgTableLoc, o_args)
  }
  def TizenRaiseException(h:Heap, ctx:Context, es:Set[WebAPIException]): (Heap,Context) = {
    if (es.isEmpty)
      (HeapBot, ContextBot)
    else {
      val v_old = h(SinglePureLocalLoc)("@exception_all")._1._2
      val v_e = Value(PValueBot,
        es.foldLeft(LocSetBot)((lset,exc)=> lset + TizenNewExceptionLoc(exc)))
      val h_1 = h.update(SinglePureLocalLoc,
        h(SinglePureLocalLoc).update("@exception", PropValue(v_e)).
          update("@exception_all", PropValue(v_e + v_old)))
      (h_1,ctx)
    }
  }

  def TizenNewExceptionLoc(exc: WebAPIException): Loc = {
    exc match {
      case UnknownError => TizenUnknownErrorLoc
      case TypeMismatchError => TizenTypeMismatchErrorLoc
      case InvalidValuesError => TizenInvalidValuesErrorLoc
    }
  }

  val TizenExceptionBot = HashSet[WebAPIException]()

  def isNoArgCBAttribute(attr: String): Boolean = {
    attr=="successCB" || attr=="NFCPeerDetectCB.ondetach" || attr=="NFCTagDetectCB.ondetach" || attr=="NetworkSuccessCB.onsuccess" ||
    attr=="NetworkSuccessCB.ondisconnected" || attr=="BluetoothDiscvDevsSuccessCB.onstarted" || attr=="AppCtrlDataArrayReplyCB.onfailure"
  }

  def isStrCBAttribute(attr: String): Boolean = {
    attr=="AppInfoEventCB.onuninstalled" || attr=="BluetoothDiscvDevsSuccessCB.ondevicedisappeared" || attr=="ContentScanSuccessCB" ||
    attr=="ContentChangeCB.oncontentremoved" || attr=="SyncProgressCB.oncompleted" || attr=="SyncProgressCB.onstopped" ||
    attr=="FileStringSuccessCB" || attr=="PackageProgressCB.oncomplete" || attr=="PackageInfoEventCB.onuninstalled" ||
    attr=="PushRegisterSuccessCB" || attr=="SystemSettingSuccessCB"
  }

  def isNumCBAttribute(attr: String): Boolean = {
    attr=="DataControlSuccessCB" || attr=="DownloadCB.onpaused" || attr=="DownloadCB.oncanceled"
  }
  def isAppInfoCBAttribute(attr: String): Boolean = {
    attr=="AppInfoEventCB.oninstalled" || attr=="AppInfoEventCB.onupdated"
  }
  def isBTDevCBAttribute(attr: String): Boolean = {
    attr=="BluetoothDevSuccessCB" || attr=="BluetoothDiscvDevsSuccessCB.ondevicefound"
  }
  def isBTDevArrCBAttribute(attr: String): Boolean = {
    attr=="BluetoothDevArraySuccessCB" || attr=="BluetoothDiscvDevsSuccessCB.onfinished"
  }
  def isCalItemArrCBAttribute(attr: String): Boolean = {
    attr=="CalendarItemArraySuccessCB" || attr=="CalendarChangeCB.onitemsadded" || attr=="CalendarChangeCB.onitemsupdated"
  }
  def isCHEntryArrCBAttribute(attr: String): Boolean = {
    attr=="CHEntryArraySuccessCB" || attr=="CHChangeCB.onadded" || attr=="CHChangeCB.onchanged"
  }
  def isContactArrCBAttribute(attr: String): Boolean = {
    attr=="ContactArraySuccessCB" || attr=="AddrBookChangeCB.oncontactsadded" || attr=="AddrBookChangeCB.oncontactsupdated"
  }
  def isPersonArrCBAttribute(attr: String): Boolean = {
    attr=="PersonArraySuccessCB" || attr=="PersonsChangeCB.onpersonsadded" || attr=="PersonsChangeCB.onpersonsupdated"
  }
  def isContentCBAttribute(attr: String): Boolean = {
    attr=="ContentChangeCB.oncontentadded" || attr=="ContentChangeCB.oncontentupdated"
  }
  def isMsgArrCBAttribute(attr: String): Boolean = {
    attr=="MsgArraySuccessCB" || attr=="MsgsChangeCB.messagesadded" || attr=="MsgsChangeCB.messagesupdated" ||
    attr=="MsgsChangeCB.messagesremoved"
  }
  def isMsgConvArrCBAttribute(attr: String): Boolean = {
    attr=="MsgConvArraySuccessCB" || attr=="MsgConvsChangeCB.conversationsadded" || attr=="MsgConvsChangeCB.conversationsupdated" ||
    attr=="MsgConvsChangeCB.conversationsremoved"
  }
  def isMsgFolderArrCBAttribute(attr: String): Boolean = {
    attr=="MsgFolderArrSuccessCB" || attr=="MsgFoldersChangeCB.foldersadded" || attr=="MsgFoldersChangeCB.foldersupdated" ||
    attr=="MsgFoldersChangeCB.foldersremoved"
  }
  def isPkgInfoCBAttribute(attr: String): Boolean = {
    attr=="PkgInfoEventCB.oninstalled" || attr=="PkgInfoEventCB.onupdated"
  }
  def isReaderCBAttribute(attr: String): Boolean = {
    attr=="SEChangeListener.onSEReady" || attr=="SEChangeListener.onSENotReady"
  }
}