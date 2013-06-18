/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.domain

abstract class AbsBase {
  def isTop(): Boolean
  def isBottom(): Boolean
  def isConcrete(): Boolean
  def toAbsString(): AbsString
  def getConcreteValueAsString(defaultString: String = ""): String = {
    if(isConcrete) toString else defaultString
  }
}
