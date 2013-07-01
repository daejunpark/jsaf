/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.domain

import java.util.{HashMap => JHashMap}

object AbsString {
  private var alpha_cache: JHashMap[String, AbsString] = null
  /* regexp, number string */
  private val hex = "(0[xX][0-9a-fA-F]+)".r
  private val exp = "[eE][+-]?[0-9]+"
  private val dec1 = "[0-9]+\\.[0-9]*(" +exp+ ")?"
  private val dec2 = "\\.[0-9]+(" +exp+ ")?"
  private val dec3 = "[0-9]+(" +exp+ ")?"
  private val dec = "([+-]?(Infinity|(" +dec1+ ")|(" +dec2 + ")|(" +dec3 + ")))"
  val num_regexp = ("NaN|(" +hex+ ")|(" +dec+ ")").r
  
  def initCache: Unit = {
    clearCache
    alpha_cache = new JHashMap()
  }

  def clearCache: Unit = {
    if (alpha_cache != null) {
      alpha_cache.synchronized {
        alpha_cache.clear
      }
    }
    alpha_cache = null
  }

  def alpha(str: String): AbsString = {
    // check cached result
    if (alpha_cache != null) {
      alpha_cache.synchronized {
        val cached = alpha_cache.get(str)
        if (cached != null) return cached
      }
    }
    
    // compute result if not cached
    val result =
      if (str.matches(num_regexp.toString))
        NumStrSingle(str)
      else
        OtherStrSingle(str)
    
    // cache computed result
    if (alpha_cache != null) {
      alpha_cache.synchronized {
        alpha_cache.put(str, result)
      }
    }
    
    // return result
    result
  }

  def fromCharCode(n: AbsNumber): AbsString = {
    if (n </ NumBot) {
      AbsNumber.concretize(n) match {
        case Some(v) => AbsString.alpha("%c".format(v.toInt))
        case None => StrTop
      }
    } else {
      StrBot
    }
  }

  /**
   * Concretize the given abstract string.
   * Returns None if the value cannot be concretized.
   *
   * @param str a given abstract string to be concretized.
   * @return a concretized string.
   */
  def concretize(str: AbsString): Option[String] = {
    str match {
      case StrTop => None // cannot be concretized.
      case StrBot => None
      case NumStr => None // cannot be concretized.
      case OtherStr => None // cannot be concretized.
      case NumStrSingle(v) => Some(v)
      case OtherStrSingle(v) => Some(v)
    }
  }
}

sealed abstract class AbsString extends AbsBase {
  /* partial order */
  def <= (that : AbsString) = (this, that) match {
    case (StrBot, _) => true
    case (_, StrTop) => true
    case (NumStrSingle(a), NumStrSingle(b)) => a == b
    case (NumStrSingle(_), NumStr) => true
    case (OtherStrSingle(a), OtherStrSingle(b)) => a == b
    case (OtherStrSingle(_), OtherStr) => true
    case (NumStr, NumStr) => true
    case (OtherStr, OtherStr) => true
    case _ => false
  }

  /* not a partial order */
  def </ (that : AbsString) = !(this <= that)

  /* join */
  def + (that: AbsString ) = (this, that) match {
    case (StrTop, _) => this
    case (_, StrTop) => that
    case (StrBot, _) => that
    case (_, StrBot) => this
    case (NumStrSingle(a), NumStrSingle(b)) =>
      if (a==b) this
      else NumStr
    case (OtherStrSingle(a), OtherStrSingle(b)) =>
      if (a==b) this
      else OtherStr
    case _ =>
      (this<=that, that<=this) match {
        case (true, _) => that
        case (_, true) => this
        case _ => StrTop
      }
  }

  /* meet */
  def <> (that: AbsString ) = {
      (this<=that, that<=this) match {
        case (true, _) => this
        case (_, true) => that
        case _ => StrBot
      }
  }

  /* abstract operator 'equal to' */
  def === (that: AbsString): AbsBool = {
    if (this <= StrBot || that <= StrBot)
      BoolBot
    else {
      (AbsString.concretize(this), AbsString.concretize(that)) match {
        case (Some(s1), Some(s2)) =>
          AbsBool.alpha(s1 == s2)
        case _ =>
          (this <= that, that <= this) match {
            case (false, false) => BoolFalse
            case _ => BoolTop
          }
      }
    } 
  }
  
  def concat(that: AbsString) = {
    (this, that) match {
      case (StrBot, _) => StrBot
      case (_, StrBot) => StrBot
      case (NumStrSingle(s_1), NumStrSingle(s_2)) => AbsString.alpha(s_1 + s_2)
      case (OtherStrSingle(s_1), NumStrSingle(s_2)) => AbsString.alpha(s_1 + s_2)
      case (NumStrSingle(s_1), OtherStrSingle(s_2)) => AbsString.alpha(s_1 + s_2)
      case (OtherStrSingle(s_1), OtherStrSingle(s_2)) => AbsString.alpha(s_1 + s_2)
      case _ => StrTop
    }
  }

  def charAt(pos: AbsNumber) = {
    AbsString.concretize(this) match {
      case Some(s) => {
        pos match {
          case UIntSingle(d) => {
            if (d >= s.length || d < 0)
              AbsString.alpha("")
            else {               
              val i = d.toInt
              AbsString.alpha(s.substring(i, i+1))
            }
          }
          case _ => StrTop
        }
      }
      case _ => StrTop
    }
  }

  def charCodeAt(pos: AbsNumber) = {
    AbsString.concretize(this) match {
      case Some(s) => {
        pos match {
          case UIntSingle(d) => {
            if (d >= s.length || d < 0)
              NaN
            else {               
              val i = d.toInt
              AbsNumber.alpha(s.substring(i, i+1).head.toInt)
            }
          }
          case _ => UInt
        }
      }
      case _ => UInt
    }
  }

  def contains(s: AbsString): AbsBool = {
    this match {
      case StrTop => BoolTop
      case StrBot => BoolBot
      case NumStr => BoolTop
      case OtherStr => BoolTop
      case NumStrSingle(v) =>
        AbsString.concretize(s) match {
          case Some(_s) => AbsBool.alpha(v.contains(_s))
          case None =>
            if (s </ StrBot)
              BoolTop
            else
              BoolBot
        }
      case OtherStrSingle(v) =>
        AbsString.concretize(s) match {
          case Some(_s) => AbsBool.alpha(v.contains(_s))
          case None =>
            if (s </ StrBot)
              BoolTop
            else
              BoolBot
        }
    }
  }

  def length(): AbsNumber = {
    this match {
      case StrTop => NumTop
      case StrBot => NumBot
      case NumStr => NumTop
      case OtherStr => NumTop
      case NumStrSingle(v) => AbsNumber.alpha(v.length)
      case OtherStrSingle(v) => AbsNumber.alpha(v.length)
    }
  }

  def toUpperCase(): AbsString = {
    this match {
      case StrTop => StrTop
      case StrBot => StrBot
      case NumStr => StrTop
      case OtherStr => OtherStr
      case NumStrSingle(v) => AbsString.alpha(v.toUpperCase)
      case OtherStrSingle(v) => AbsString.alpha(v.toUpperCase)
    }
  }

  override def toString(): String = {
    this match {
      case StrTop => "String"
      case StrBot => "Bot"
      case NumStr => "NumStr"
      case OtherStr => "OtherStr"
      case NumStrSingle(s) => "\"" + s + "\""
      case OtherStrSingle(s) => "\"" + s + "\""
    }
  }

  override def isTop(): Boolean = {this == StrTop}

  override def isBottom(): Boolean = {this == StrBot}

  override def isConcrete(): Boolean = {
    this match {
      case _: NumStrSingle => true
      case _: OtherStrSingle => true
      case _ => false
    }
  }

  def getConcreteValue(): Option[String] = {
    this match {
      case NumStrSingle(value) => Some(value)
      case OtherStrSingle(value) => Some(value)
      case _ => None
    }
  }

  override def toAbsString(): AbsString = {
    this match {
      case _: NumStrSingle => this
      case _: OtherStrSingle => this
      case _ => StrBot
    }
  }
}

case object StrTop extends AbsString
case object StrBot extends AbsString
case object NumStr extends AbsString
case object OtherStr extends AbsString
case class NumStrSingle(value : String) extends AbsString
case class OtherStrSingle(value : String) extends AbsString
