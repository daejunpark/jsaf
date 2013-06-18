/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.domain

object AbsUndef {
  def alpha: AbsUndef = UndefTop
}

sealed abstract class AbsUndef extends AbsBase {
  /* partial order */
  def <= (that: AbsUndef) = {
    (this == UndefBot) || (that == UndefTop)
  }

  /* not a partial order */
  def </ (that: AbsUndef) = {
    (this == UndefTop) && (that == UndefBot)
  }

  /* join */
  def + (that: AbsUndef) = {
    if (this == UndefTop || that == UndefTop) UndefTop
    else UndefBot
  }

  /* meet */
  def <> (that: AbsUndef ) = {
    if (this == UndefTop && that == UndefTop) UndefTop
    else UndefBot
  }
  
  /* abstract operator 'equal to' */
  def === (that: AbsUndef): AbsBool = {
    (this, that) match {
      case (UndefBot, _) => BoolBot
      case (_, UndefBot) => BoolBot
      case _ => BoolTrue
    }
  }
  
  override def toString(): String = {
    this match {
      case UndefTop => "undefined"
      case UndefBot => "Bot"
    }
  }

  override def isTop(): Boolean = {this == UndefTop}

  override def isBottom(): Boolean = {this == UndefBot}

  override def isConcrete(): Boolean = {
    this == UndefTop
  }

  def getConcreteValue(): Option[AbsUndef] = {
    if(isConcrete) Some(UndefTop) else None
  }

  override def toAbsString(): AbsString = {
    if(isConcrete) OtherStrSingle("undefined") else StrBot
  }
}

case object UndefTop extends AbsUndef
case object UndefBot extends AbsUndef
