/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.webapp_converter

import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF, NodeUtil => NU, Span, SpanInfo}
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.ErrorLog
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.useful.HasAt

class WebappConverter extends Walker {
  var logList: List[ConversionInfo] = List()

  /* Error handling
   * The signal function collects errors during the AST->IR translation.
   * To collect multiple errors,
   * we should return a dummy value after signaling an error.
   */
  val errors: ErrorLog = new ErrorLog
  def signal(msg:String, hasAt:HasAt) = errors.signal(msg, hasAt)
  def signal(hasAt:HasAt, msg:String) = errors.signal(msg, hasAt)
  def signal(error: StaticError) = errors.signal(error)
  def getErrors(): JList[StaticError] = toJavaList(errors.errors)

  def doit(program: Program) = {
    walk(program).asInstanceOf[Program]
  }

  /* 1. NameSpace */
  val NSE_newprop: List[String] = List("CalendarEventId", "CalendarAlarm") // new webapis.###
  val NSE_newpropfunc: List[String] = List("Contact", "ContractRef", "ContactName", "ContactOrganization", "ContactWebSite",
      "ContactAnniversary", "ContractAddress", "ContactPhoneNumber", "ContactEmailAddress", "ContactGroup",
      "Message", "MessageAttachment ", "NFCMessage", "NFCRecord", "NDEFRecordText",
      "NDEFRecordURI ", "NDEFRecordMedia", "TZDate", "TimeDuration", "AttributeFilter",
      "AttributeRangeFilter", "CompositeFilter", "SortMode", "SimpleCoordinates", "Item") // new webapis.###(..)
  val NSE_dotprop3: List[Pair[String, String]] = List(("allshare", "serviceconnector"), ("chord", "serviceconnector")) // webapis.###.###.*
  val NSE_dotprop2: List[String] = List("callhistory", "contact", "content", "filesystem", "messaging",
      "nfc", "NFCManager", "power", "systeminfo", "time",
      "WebAPIException", "allshare", "chord" ) // webapis.###.*
  val NSE_ex: List[String] = List("WebAPIException") // exceptional case: ###.* -> tizen.###.*

  /* 2. Platform Specific Value */
  val PSV_strtochange: List[String] = List("http://samsungapps.com/appcontrol/operation/view",
    "http://samsungapps.com/appcontrol/operation/pick",
    "http://samsungapps.com/appcontrol/operation/create_content",
    "http://samsungapps.com/appcontrol/operation/dial")
  val PSV_strtowarn: List[String] = List("http://samsungapps.com/appcontrol/operation/default",
    "http://samsungapps.com/appcontrol/operation/edit",
    "http://samsungapps.com/appcontrol/operation/send")
  val PSV_dotprop3func: List[Pair[String, String]] = List(("alarm", "add"), ("application", "launch"), ("application", "launchAppControl"),
    ("application", "getAppInfo"), ("application", "getAppSharedURI"), ("application", "getAppContext")) // webapis.###.###(...)

  /* 3. Platform Specific API */
  val PSA_dotprop3func: List[Pair[String, String]] = List(("filechooser", "showOpenFileBrowser"), ("network", "getAvailableNetworks")) // webapis.###.###(...)
  val PSA_logdog: List[String] = List("d", "i", "e", "v", "w") // logdog.#(...) -> console.log(...)

  def printLogMsg(beginSpan: Span, endSpan: Span, msg: String) =
    System.out.println("%s:%d:%d~%d:%d: %s".format(beginSpan.getFileName, beginSpan.getBegin.getLine, beginSpan.getBegin.column, endSpan.getEnd.getLine, endSpan.getEnd.column, msg))

  /*
  def debug_check_listed(s: String) = {
    NSE_newprop.contains(s) || NSE_newpropfunc.contains(s) || NSE_dotprop3.map(e => e._2).contains(s) || NSE_dotprop2.contains(s) || NSE_ex.contains(s) ||
    PSV_dotprop3func.map(e => e._2).contains(s) ||
    PSA_dotprop3func.map(e => e._2).contains(s) || PSA_logdog.contains(s)
  }
  */

  /* constants from ConversionInfo.java*/
  val LEVEL_INFO: String        = ConversionInfo.LEVEL_INFO
  val LEVEL_ERROR: String       = ConversionInfo.LEVEL_ERROR

  val TYPE_API_NAME_SPACE: Int  = ConversionInfo.TYPE_API_NAME_SPACE
  val TYPE_PLATFORM_VALUES: Int = ConversionInfo.TYPE_PLATFORM_VALUES
  val TYPE_PLATFORM_API: Int    = ConversionInfo.TYPE_PLATFORM_API
  val TYPE_PLATFORM_HTML: Int   = ConversionInfo.TYPE_PLATFORM_HTML

  def addLog(_level: String, _type: Int, _msg: String, _path: String, _line: Int, _block: Int, _org: String, _conv: String): Unit = {
    val elem: ConversionInfo = new ConversionInfo()
    elem.level = _level
    elem.`type` = _type
    elem.message = _msg
    elem.fillePath = _path // TODO: typo?
    elem.lineNum = _line
    elem.blockNum = _block
    elem.originalSECAPI = _org
    elem.convetedTizenAPI = _conv
    logList ++= List(elem)
  }

  override def walk(node: Any): Any = node match {
    case SNew(span, SDot(span2, SVarRef(span3, SId(span4, "webapis", uniq4, b4)), SId(span5, prop, uniq5, b5))) if NSE_newprop.contains(prop) => {
      // 1. new webapis.### -> new tizen.###
      val org: String = "new webapis.%s".format(prop)
      val conv: String = "new tizen.%s".format(prop)
      val msg: String = "[%s] converted namespace: %s -> %s".format(LEVEL_INFO, org, conv)
      val sp: Span = span.getSpan()
      addLog(LEVEL_INFO, TYPE_API_NAME_SPACE, msg, sp.getFileName, sp.getBegin.getLine, -1, org, conv)
      printLogMsg(span.getSpan(), span5.getSpan(), msg)
      SNew(span, SDot(span2, SVarRef(span3, SId(span4, "tizen", uniq4, b4)), SId(span5, prop, uniq5, b5)))
    }
    case SNew(span, SFunApp(span2, SDot(span3, SVarRef(span4, SId(span5, "webapis", uniq5, b5)), SId(span6, prop, uniq6, b6)), args)) if NSE_newpropfunc.contains(prop) => {
      // 1. new webapis.###(...) -> new tizen.###(...)
      val mapArgsWalk = args.map(e => walk(e).asInstanceOf[Expr])
      val org: String = "new webapis.%s(...)".format(prop)
      val conv: String = "new tizen.%s(...)".format(prop)
      val msg: String = "[%s] converted namespace: %s -> %s".format(LEVEL_INFO, org, conv)
      val sp: Span = span.getSpan()
      addLog(LEVEL_INFO, TYPE_API_NAME_SPACE, msg, sp.getFileName, sp.getBegin.getLine, -1, org, conv)
      printLogMsg(span.getSpan(), span5.getSpan(), msg)
      SNew(span, SFunApp(span2, SDot(span3, SVarRef(span4, SId(span5, "tizen", uniq5, b5)), SId(span6, prop, uniq6, b6)), mapArgsWalk))
    }
    case SDot(span, SVarRef(span3, SId(span4, name4, uniq4, b4)), SId(span2, prop2, uniq2, b2)) if NSE_ex.contains(name4)=> {
      // 1. ###.* -> tizen.###.*
      val org: String = "%s.%s".format(name4, prop2)
      val conv: String = "tizen.%s.%s".format(name4, prop2)
      val msg: String = "[%s] converted namespace: %s -> %s".format(LEVEL_INFO, org, conv)
      val sp: Span = span.getSpan()
      addLog(LEVEL_INFO, TYPE_API_NAME_SPACE, msg, sp.getFileName, sp.getBegin.getLine, -1, org, conv)
      printLogMsg(span.getSpan(), span2.getSpan(), msg)
      SDot(span, SDot(span, SVarRef(span3, NF.makeId(span3.getSpan(), "tizen")), SId(span4, name4, uniq4, b4)), SId(span2, prop2, uniq2, b2))
    }
    case SDot(span1, SDot(span, SDot(span3, SVarRef(span5, SId(span6, "webapis", uniq6, b6)), SId(span4, prop4, uniq4, b4)), SId(span2, prop2, uniq2, b2)), SId(span7, prop7, uniq7, b7))
      if NSE_dotprop3.contains((prop4, prop2)) => {
        // 1. webapis.###.###.* -> tizen.###.###.*
        val org: String = "webapis.%s.%s.%s".format(prop4, prop2, prop7)
        val conv: String = "tizen.%s.%s.%s".format(prop4, prop2, prop7)
        val msg: String = "[%s] converted namespace: %s -> %s".format(LEVEL_INFO, org, conv)
        val sp: Span = span1.getSpan()
        addLog(LEVEL_INFO, TYPE_API_NAME_SPACE, msg, sp.getFileName, sp.getBegin.getLine, -1, org, conv)
        printLogMsg(span1.getSpan(), span7.getSpan(), msg)
        SDot(span1, SDot(span, SDot(span3, SVarRef(span5, SId(span6, "tizen", uniq6, b6)), SId(span4, prop4, uniq4, b4)), SId(span2, prop2, uniq2, b2)), SId(span7, prop7, uniq7, b7))
    }
    case SDot(span, SDot(span3, SVarRef(span5, SId(span6, "webapis", uniq6, b6)), SId(span4, prop4, uniq4, b4)), SId(span2, prop2, uniq2, b2)) if NSE_dotprop2.contains(prop4) => {
      // 1. webapis.###.* -> tizen.###.*
      val org: String = "webapis.%s.%s".format(prop4, prop2)
      val conv: String = "tizen.%s.%s".format(prop4, prop2)
      val msg: String = "[%s] converted namespace: %s -> %s".format(LEVEL_INFO, org, conv)
      val sp: Span = span.getSpan()
      addLog(LEVEL_INFO, TYPE_API_NAME_SPACE, msg, sp.getFileName, sp.getBegin.getLine, -1, org, conv)
      printLogMsg(span.getSpan(), span2.getSpan(), msg)
      SDot(span, SDot(span3, SVarRef(span5, SId(span6, "tizen", uniq6, b6)), SId(span4, prop4, uniq4, b4)), SId(span2, prop2, uniq2, b2))
    }
    case SStringLiteral(span, quote, str) if PSV_strtochange.contains(str) => {
      // 2. url in PSV0010 with replacement
      val res: String = str.replaceFirst("""samsungapps\.com""", "tizen.org")
      val msg: String = "[%s] converted platform specific value: %s -> %s".format(LEVEL_INFO, str, res)
      val sp: Span = span.getSpan()
      addLog(LEVEL_INFO, TYPE_PLATFORM_VALUES, msg, sp.getFileName, sp.getBegin.getLine, -1, str, res)
      printLogMsg(span.getSpan(), span.getSpan(), msg)
      SStringLiteral(span, quote, res)
    }
    case s@SStringLiteral(span, quote, str) if PSV_strtowarn.contains(str) => {
      // 2. url in PSV0010 without replacement
      val msg: String = "[%s] not converted platform specific value: %s".format(LEVEL_ERROR, str, null)
      val sp: Span = span.getSpan()
      addLog(LEVEL_ERROR, TYPE_PLATFORM_VALUES, msg, sp.getFileName, sp.getBegin.getLine, -1, str, null)
      printLogMsg(span.getSpan(), span.getSpan(), msg)
      s
    }
    case SFunApp(span, SDot(span2, SDot(span4, SVarRef(span6, SId(span7, "webapis", uniq7, b7)), SId(span5, prop5, uniq5, b5)), SId(span3, prop3, uniq3, b3)), args)
      if PSA_dotprop3func.contains((prop5, prop3)) => {
      // 3. (PSA0010, PSA0020) webapis.###.###(...) -> warning
      val mapArgsWalk = args.map(e => walk(e).asInstanceOf[Expr])
      val org: String = "webapis.%s.%s(...)".format(prop5, prop3)
      val msg: String = "[%s] not converted platform specific API: %s".format(LEVEL_ERROR, org)
      val sp: Span = span.getSpan()
      addLog(LEVEL_ERROR, TYPE_PLATFORM_API, msg, sp.getFileName, sp.getBegin.getLine, -1, org, null)
      printLogMsg(span.getSpan(), span3.getSpan(), msg)
      SFunApp(span, SDot(span2, SDot(span4, SVarRef(span6, SId(span7, "webapis", uniq7, b7)), SId(span5, prop5, uniq5, b5)), SId(span3, prop3, uniq3, b3)), mapArgsWalk)
    }
    case SFunApp(span, SDot(span2, SDot(span4, SVarRef(span6, SId(span7, "webapis", uniq7, b7)), SId(span5, prop5, uniq5, b5)), SId(span3, prop3, uniq3, b3)), args)
      if PSV_dotprop3func.contains((prop5, prop3)) => {
      // 2. (PSV0020) webapis.###.###(...) -> warning
      val mapArgsWalk = args.map(e => walk(e).asInstanceOf[Expr]) 
      val org: String = "webapis.%s.%s(...)".format(prop5, prop3)
      val msg: String = "[%s] not converted platform specific value: %s".format(LEVEL_ERROR, org)
      val sp: Span = span.getSpan()
      addLog(LEVEL_ERROR, TYPE_PLATFORM_VALUES, msg, sp.getFileName, sp.getBegin.getLine, -1, org, null)
      printLogMsg(span.getSpan(), span3.getSpan(), msg)
      SFunApp(span, SDot(span2, SDot(span4, SVarRef(span6, SId(span7, "webapis", uniq7, b7)), SId(span5, prop5, uniq5, b5)), SId(span3, prop3, uniq3, b3)), mapArgsWalk)
    }
    case SFunApp(span, SDot(span2, SVarRef(span4, SId(span5, "logdog", uniq5, b5)), SId(span3, prop3, uniq3, b3)), args) if PSA_logdog.contains(prop3) => {
      // 3. logdog.#(...) -> console.log(...)
      val mapArgsWalk = args.map(e => walk(e).asInstanceOf[Expr])
      val org: String = "logdog.%s(...)".format(prop3)
      val conv: String = "console.log(...)"
      val msg: String = "[%s] converted platform specific API: %s -> %s".format(LEVEL_INFO, org, conv)
      val sp: Span = span.getSpan()
      addLog(LEVEL_INFO, TYPE_PLATFORM_API, msg, sp.getFileName, sp.getBegin.getLine, -1, org, conv)
      printLogMsg(span.getSpan(), span3.getSpan(), msg)
      SFunApp(span, SDot(span2, SVarRef(span4, SId(span5, "console", uniq5, b5)), SId(span3, "log", uniq3, b3)), mapArgsWalk)
    }

    case xs: List[_] => xs.map(x => walk(x))
    case xs: Option[_] => xs.map(x => walk(x))
    case _ => super.walk(node)
  }
}
