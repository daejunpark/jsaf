/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.domain

object AbsNumber {
  // note: scala's Integer is deprecated.
  def alpha(num: java.lang.Integer): AbsNumber = {
    if (num >= 0) UIntSingle(num.toDouble)
    else NUIntSingle(num.toDouble)
  }
  
  def alpha(num: Double): AbsNumber = {
    if (num.isNaN)
      NaN
    else if (num.isNegInfinity)
      NegInf
    else if (num.isPosInfinity)
      PosInf
    else {
      val intnum = num.toInt
        val diff:Double = num - intnum.toDouble
        if ((diff == 0) && num >= 0) UIntSingle(num)
        else NUIntSingle(num)
    }
  }

  def concretize(an: AbsNumber): Option[Double] = {
    an match {
      case NumTop => None // cannot be concretized
      case NumBot => None
      case Infinity => None
      case PosInf => Some(Double.PositiveInfinity)
      case NegInf => Some(Double.NegativeInfinity)
      case NaN => Some(Double.NaN)
      case UInt => None // cannot be concretized
      case NUInt => None // cannot be concretized
      case UIntSingle(v) => Some(v)
      case NUIntSingle(v) => Some(v)
    }
  }
  
  /*
  def alpha(num: Double): AbsNumber = {
    val richnum = new RichDouble(num)
    if (richnum.isValidInt && richnum >= 0) UIntSingle(richnum.toInt) // long??
    else NUIntSingle(num)
  }
  */
}

sealed abstract class AbsNumber extends AbsBase {
  /* partial order */
  def <= (that : AbsNumber) = (this, that) match {
    case (NumBot,_) => true
    case (_, NumTop) => true
    case (NaN, NaN) => true
    case (PosInf, PosInf) => true
    case (PosInf, Infinity) => true
    case (NegInf, NegInf) => true
    case (NegInf, Infinity) => true
    case (Infinity, Infinity) => true
    case (UIntSingle(a), UIntSingle(b)) => a == b
    case (UIntSingle(_), UInt) => true
    case (NUIntSingle(a), NUIntSingle(b)) => a == b
    case (NUIntSingle(_), NUInt) => true
    case (UInt, UInt) => true
    case (NUInt, NUInt) => true
    case _ => false
  }
  
  /* not a partial order */
  def </ (that : AbsNumber) = !(this <= that)

  def < (that: AbsNumber) = {
    if (this <= NumBot || that <= NumBot) BoolBot
    else {
      (AbsNumber.concretize(this), AbsNumber.concretize(that)) match {
        case (Some(v_1), Some(v_2)) => AbsBool.alpha(v_1 < v_2)
        case (None, Some(v_2)) => {
          this match {
            case UInt => {
              if (v_2 <= 0) BoolFalse
              else BoolTop
            }
            case _ => BoolTop
          }
        }
        case _ => BoolTop
      }
    }
  }

  /* abstract operator 'equal to' */
  def === (that: AbsNumber): AbsBool = {
    if (this <= NumBot || that <= NumBot)
      BoolBot
    else if (this <= NaN || that <= NaN)
      BoolFalse
    else {
      (AbsNumber.concretize(this), AbsNumber.concretize(that)) match {
        case (Some(v_1), Some(v_2)) => AbsBool.alpha(v_1 == v_2)
        case _ =>
          (this <= that, that <= this) match {
            case (false, false) => BoolFalse
            case _ => BoolTop
          }
      }
    }
  }

  /* join */
  def + (that: AbsNumber) = (this, that) match {
    case (NumTop, _) => this
    case (_, NumTop) => that
    case (NumBot, _) => that
    case (_, NumBot) => this
    case (PosInf, NegInf) => Infinity
    case (NegInf, PosInf) => Infinity
    case (UIntSingle(a), UIntSingle(b)) =>
      if (a==b) this
      else UInt
    case (NUIntSingle(a), NUIntSingle(b)) =>
      if (a==b) this
      else NUInt
    case _ =>
      (this<=that, that<=this) match {
        case (true, _) => that
        case (_, true) => this
        case _ => NumTop
      }
  }

  /* meet */
  def <> (that: AbsNumber ) = {
      (this<=that, that<=this) match {
        case (true, _) => this
        case (_, true) => that
        case _ => NumBot
      }
  }

  override def toString(): String = {
    this match {
      case NumTop => "Number"
      case NumBot => "Bot"
      case Infinity => "Inf"
      case PosInf => "+inf"
      case NegInf => "-inf"
      case NaN => "NaN"
      case UInt => "UInt"
      case NUInt => "NUInt"
      case UIntSingle(n) => n.toString
      case NUIntSingle(n) => n.toString
    }
  }

  override def isTop(): Boolean = {this == NumTop}

  override def isBottom(): Boolean = {this == NumBot}

  override def isConcrete(): Boolean = {
    if(this == Infinity || this == PosInf || this == NegInf || this == NaN) return true
    this match {
      case _: UIntSingle => true
      case _: NUIntSingle => true
      case _ => false
    }
  }

  def getConcreteValue(): Option[Double] = {
    this match {
      case PosInf => Some(Double.PositiveInfinity)
      case NegInf => Some(Double.NegativeInfinity)
      case NaN => Some(Double.NaN)
      case UIntSingle(value) => Some(value)
      case NUIntSingle(value) => Some(value)
      case _ => None
    }
  }

  override def toAbsString(): AbsString = {
    this match {
      case PosInf => NumStrSingle("Infinity")
      case NegInf => NumStrSingle("-Infinity")
      case NaN => NumStrSingle("NaN")
      case UIntSingle(value) => NumStrSingle(value.toInt.toString)
      case NUIntSingle(value) =>
        if(value == value.toInt) AbsString.alpha(value.toInt.toString)
        else AbsString.alpha(value.toString)
      case _ => StrBot
    }
  }
}

case object NumTop extends AbsNumber
case object NumBot extends AbsNumber
case object Infinity extends AbsNumber
case object PosInf extends AbsNumber
case object NegInf extends AbsNumber
case object NaN extends AbsNumber
case object UInt extends AbsNumber
case object NUInt extends AbsNumber

case class UIntSingle(value : Double) extends AbsNumber {
  override def toString() = value.toLong.toString
}

case class NUIntSingle(value : Double) extends AbsNumber {
  override def toString() = value.toString
}
