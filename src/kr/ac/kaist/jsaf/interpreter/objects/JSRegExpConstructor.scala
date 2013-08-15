/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.interpreter.objects

import kr.ac.kaist.jsaf.interpreter._
import kr.ac.kaist.jsaf.utils.regexp.JSRegExpSolver

class JSRegExpConstructor(_I: Interpreter, _proto: JSObject)
  extends JSFunction13(_I, _proto, "Array", true, propTable(), _I.IH.dummyFtn(0), EmptyEnv(), true) {
  def init(): Unit = {
    /*
     * 15.10.5 Properties of the RegExp Constructor
     */
    property.put("length", I.IH.numProp(2))
    // { [[Writable]]: false, [[Enumerable]]: false, [[Configurable]]: false }
    property.put("prototype", I.IH.mkDataProp(I.IS.RegExpPrototype))
  }

  /*
   * 15.10.4 The RegExp Constructor
   * 15.10.4.1 new RegExp(pattern, flags)
   */
  def construct(pattern: Val, flags: Val): JSRegExp = {
    val (p, f) = pattern match {
      case r: JSRegExp =>
        if (I.IH.isUndef(flags)) {
          (r.pattern, r.flags)
        } else {
          // 15.10.4.1. "If *pattern* is an object R whose [[Class]] internal property is 'RegExp'
          // and *flags* is not **undefined**, then throw a **TypeError** exception."
          throw new TypeErrorException
        }
      case pv:PVal =>
        val p = if (I.IH.isUndef(pattern)) "" else I.IH.toString(pattern)
        val f = if (I.IH.isUndef(flags)) "" else I.IH.toString(flags)
        (p, f)
    }

    val s =
      if (p == "") "(?:)"
      else p
    val (matcher, b_g, b_i, b_m, nCapturingParens) = JSRegExpSolver.parse(s, f)

    val prop = propTable()
    prop.put("source", I.IH.strProp(s))
    prop.put("global", I.IH.boolProp(b_g))
    prop.put("ignoreCase", I.IH.boolProp(b_i))
    prop.put("multiline", I.IH.boolProp(b_m))
    prop.put("lastIndex", I.IH.mkDataProp(PVal(I.IH.mkIRNum(0)), true, false, false))

    new JSRegExp(I, I.IS.RegExpPrototype, "RegExp", true, prop, matcher, p, f, nCapturingParens)
  }

  override def _construct(argsObj: JSObject): JSRegExp = {
    construct(argsObj._get("0"), argsObj._get("1"))
  }
}
