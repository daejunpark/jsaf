/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.domain

object AbsBool {
  def alpha(bool: Boolean): AbsBool = {
    if (bool) BoolTrue else BoolFalse
  }
}

sealed abstract class AbsBool {
  /* partial order */
  def <= (that: AbsBool) = {
    if (this == that) true
    else if (this == BoolBot) true
    else if (that == BoolTop) true
    else false
  }
  
  /* not a partial order */
  def </ (that : AbsBool) = {
    if (this == that) false
    else if (this == BoolBot) false
    else if (that == BoolTop) false
    else true
  }
  
  /* join */
  def + (that: AbsBool) = {
    if (this == that) this
    else if (this == BoolBot) that
    else if (that == BoolBot) this
    else BoolTop
  }

  /* meet */
  def <> (that: AbsBool ) = {
    if (this == that) this
    else if (this == BoolTop) that
    else if (that == BoolTop) this
    else BoolBot
  }

  /* abstract operator 'equal to' */
  def === (that: AbsBool): AbsBool = {
    (this, that) match {
      case (BoolBot, _) => BoolBot
      case (_, BoolBot) => BoolBot
      case (BoolTop, _) => BoolTop
      case (_, BoolTop) => BoolTop
      case (BoolTrue, BoolTrue) => BoolTrue
      case (BoolFalse, BoolFalse) => BoolTrue
      case _ => BoolFalse
    }
  }
  
  override def toString(): String = {
    this match {
      case BoolTop => "Bool"
      case BoolBot => "Bot"
      case BoolTrue => "true"
      case BoolFalse => "false"
    }
  }
}

case object BoolTop extends AbsBool
case object BoolBot extends AbsBool
case object BoolTrue extends AbsBool
case object BoolFalse extends AbsBool
