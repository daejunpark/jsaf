/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.interpreter.objects

import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.nodes_util.{EJSCompletionType => CT}
import kr.ac.kaist.jsaf.interpreter.{InterpreterPredefine => IP, _}
import kr.ac.kaist.jsaf.interpreter.objects.JSRegExpHelper._

class JSRegExpPrototype(_I: Interpreter, _proto: JSObject)
  extends JSRegExp(_I, _proto, "RegExp", true, propTable, (s: String, i: Int) => None, "", "", 0) {
  def init(): Unit = {
    /*
     * 15.10.6 Properties of the RegExp Prototype Object
     */
    property.put("constructor", I.IH.objProp(I.IS.RegExpConstructor))
    property.put("exec", I.IH.objProp(I.IS.RegExpPrototypeExec))
    property.put("test", I.IH.objProp(I.IS.RegExpPrototypeTest))
    property.put("toString", I.IH.objProp(I.IS.RegExpPrototypeToString))
  }

  override def __callBuiltinFunction(method: JSFunction, argsObj: JSObject): Unit = {
    method match {
      case I.IS.RegExpPrototypeExec => _exec(argsObj._get("0"))
      case I.IS.RegExpPrototypeTest => _test(argsObj._get("0"))
      case I.IS.RegExpPrototypeToString => _toString()
    }
  }

  def _exec(string: Val): Unit = {
    I.IH.toObject(I.IS.tb) match {
      case r:JSRegExp =>
        val s: String = I.IH.toUnescapedString(string) // puppy 1
        val length: Int = s.length
        val lastIndex: Val = r._get("lastIndex")
        var i: Int = I.IH.toUint32(lastIndex).toInt
        val global: Val = r._get("global")
        if (!I.IH.toBoolean(global))
          i = 0
        var matchSucceeded: Boolean = false
        var rr: RegExpState = null
        while (!matchSucceeded) {
          if (i < 0 || i > length) {
            r._put("lastIndex", PVal(I.IH.mkIRNum(0)), true)
            I.IS.comp.setReturn(IP.nullV)
            return
          }
          r._match(s, i) match {
            case None =>
              i += 1
            case Some(r) =>
              rr = r
              matchSucceeded = true
          }
        }
        val e: Int = rr.endIndex
        if (I.IH.toBoolean(global))
          r._put("lastIndex", PVal(I.IH.mkIRNum(e)), true)
        val n: Int = rr.captures.size
        val a: JSArray = I.IS.ArrayConstructor.construct(Nil)
        val matchIndex: Int = i
        a._defineOwnProperty("index",
                             I.IH.mkDataProp(PVal(I.IH.mkIRNum(matchIndex)),
                                           true, true, true),
                             true)
        a._defineOwnProperty("input",
                             I.IH.mkDataProp(PVal(I.IH.mkIRStr(s)), // puppy 2
                                           true, true, true),
                             true)
        a._defineOwnProperty("length",
                             I.IH.mkDataProp(PVal(I.IH.mkIRNum(n+1)),
                                           true, true, true),
                             true)
        val matchedSubstr: String = s.substring(i, e)
        a._defineOwnProperty("0",
                             I.IH.mkDataProp(PVal(I.IH.mkIRStr(matchedSubstr)), // puppy 3
                                           true, true, true),
                             true)
        for (i <- 1 to n) {
          val captureI: Val = rr.captures(i) match {
            case Some(s) => PVal(I.IH.mkIRStr(s)) // puppy 4
            case None => IP.undefV
          }
          a._defineOwnProperty(i.toString,
                               I.IH.mkDataProp(captureI, true, true, true),
                               true)
        }
        I.IS.comp.setReturn(a)
      case _ => I.IS.comp.setThrow(IP.typeError, I.IS.span)
    }
  }

  def _test(string: Val): Unit = {
    _exec(string)
    if(I.IS.comp.Type == CT.RETURN && I.IS.comp.value.isInstanceOf[JSObject]) I.IS.comp.setReturn(IP.truePV)
    else I.IS.comp.setReturn(IP.falsePV)
  }

  def _toString(): Unit = { // puppy 5
    I.IH.toObject(I.IS.tb) match {
      case r: JSRegExp =>
        val source: String = I.IH.toUnescapedString(r._get("source"))
        val global: String = if (I.IH.toBoolean(r._get("global"))) "g" else ""
        val ignoreCase: String = if (I.IH.toBoolean(r._get("ignoreCase"))) "i" else ""
        val multiline: String = if (I.IH.toBoolean(r._get("multiline"))) "m" else ""
        val s: String = "/"+source+"/"+global+ignoreCase+multiline
        I.IS.comp.setReturn(PVal(I.IH.mkIRStr(s)))
      case _ => I.IS.comp.setThrow(IP.typeError, I.IS.span)
    }
  }
}
