/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.domain

object AbsNull {
  def alpha: AbsNull = NullTop
}

sealed abstract class AbsNull extends AbsBase {
  /* partial order */
  def <= (that: AbsNull) = {
    (this == NullBot) || (that == NullTop)    
  }

  /* not a partial order */
  def </ (that: AbsNull) = {
    (this == NullTop) && (that == NullBot)
  }

  /* join */
  def + (that: AbsNull) = {
    if (this == NullTop || that == NullTop) NullTop
    else NullBot
  }

  /* meet */
  def <> (that: AbsNull ) = {
    if (this == NullTop && that == NullTop) NullTop
    else NullBot
  }

  /* abstract operator 'equal to' */
  def === (that: AbsNull): AbsBool = {
    (this, that) match {
      case (NullBot, _) => BoolBot
      case (_, NullBot) => BoolBot
      case _ => BoolTrue
    }
  }
  
  override def toString(): String = {
    this match {
      case NullTop => "null"
      case NullBot => "Bot"
    }
  }

  override def isTop(): Boolean = {this == NullTop}

  override def isBottom(): Boolean = {this == NullBot}

  override def isConcrete(): Boolean = {
    this == NullTop
  }

  def getConcreteValue(): Option[AbsNull] = {
    if(isConcrete) Some(NullTop) else None
  }

  override def toAbsString(): AbsString = {
    if(this == NullTop) OtherStrSingle("null") else StrBot
  }
}

case object NullTop extends AbsNull
case object NullBot extends AbsNull
