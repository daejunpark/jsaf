/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.typing.domain._

abstract class WebAPIException {
/*  val code
  val name
  val message*/
  override def toString(): String = {
    this match {
      case UnknownError => "UnknownErr"
      case TypeMismatchError => "TypeMismatchErr"
      case InvalidValuesError => "InvalidValuesErr"
    }
  }
}

case object UnknownError extends WebAPIException
case object TypeMismatchError extends WebAPIException
case object InvalidValuesError extends WebAPIException