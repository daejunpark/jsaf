/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.typing.domain._
import scala.runtime.{RichInt, RichDouble}

object Operator {
  /* unary operator */
  /* void */
  def uVoid(value:Value): Value = {
    Value(PValue(UndefTop), LocSetBot)
  }
  /* + */
  def uopPlus(value:Value): Value = {
    Value(Helper.toNumber(value.pvalue) + Helper.toNumber(Helper.objToPrimitive(value.locset, "Number")))
  }
  /* - */
  def uopMinus(value:Value): Value = {
    val oldValue = Helper.toNumber(value.pvalue) + Helper.toNumber(Helper.objToPrimitive(value.locset, "Number"))
    oldValue match {
      case NaN =>  Value(NaN)
      case UIntSingle(0) =>  Value(oldValue)
      case UIntSingle(n) =>  Value(NUIntSingle(-n))
      case NUIntSingle(n)=>
        val neg = -n
        val intnum = neg.toInt
        val diff:Double = neg - intnum.toDouble
        if ((diff == 0) && neg >= 0) Value(UIntSingle(neg))
        else Value(NUIntSingle(neg))
      case UInt =>  Value(NUInt)
      case NUInt =>  Value(NumTop)
      case PosInf =>  Value(NegInf)
      case NegInf =>  Value(PosInf)
      case _ =>  Value(oldValue)
    }
  }
  /* ~ */
  def uopBitNeg(value:Value): Value = {
    val oldValue =	ToInt32(value) 
    oldValue match {
      case UIntSingle(n) =>  Value(NUIntSingle(~(n.toInt)))
      case NUIntSingle(n)=>  Value(UIntSingle(~(n.toInt)))
      case UInt =>  Value(NUInt)
      case NUInt =>  Value(UInt)
      case _ =>  Value(oldValue)
    }
  }
  /* ! */
  def uopNeg(value:Value): Value = {
    val oldValue = Helper.toBoolean(value)
    oldValue match {
      case BoolTrue =>  Value(BoolFalse)
      case BoolFalse =>  Value(BoolTrue)
      case _ =>  Value(oldValue)
    }
  }
  /* binary operator */
  /* | */
  def bopBitOr (left: Value, right: Value): Value = {
    val lnum = ToInt32(left)
    val rnum = ToInt32(right)
    (lnum, rnum) match {
      case (UIntSingle(0), n) =>  Value(n)
      case (n, UIntSingle(0)) =>  Value(n)
      case (UIntSingle(n1), UIntSingle(n2)) =>  Value(UIntSingle(n1.toInt|n2.toInt))
      case (UIntSingle(n1), NUIntSingle(n2)) =>
        if (n2>0) Value(UIntSingle(n1.toInt|n2.toInt))
        else Value(NUIntSingle(n1.toInt|n2.toInt))
      case (UIntSingle(n1), UInt) =>  Value(UInt)
      case (UIntSingle(n1), NUInt) =>  Value(NumTop)
      case (NUIntSingle(n1), UIntSingle(n2)) =>
        if (n1>0) Value(UIntSingle(n1.toInt|n2.toInt))
        else Value(NUIntSingle(n1.toInt|n2.toInt))
      case (NUIntSingle(n1), NUIntSingle(n2)) =>
        if (n1>0 && n2>0) Value(UIntSingle(n1.toInt|n2.toInt))
        else Value(NUIntSingle(n1.toInt|n2.toInt))
      case (NUIntSingle(n1), UInt) =>
        if (n1>0) Value(UInt)
        else Value(NUInt)
      case (NUIntSingle(n1), NUInt) => Value(NumTop)
      case (UInt, UIntSingle(n2)) =>  Value(UInt)
      case (UInt, NUIntSingle(n2)) =>
        if (n2>0) Value(UInt)
        else Value(NUInt)
      case (UInt, UInt) =>  Value(UInt)
      case _ =>  Value(NumTop)
    }
  }
  /* & */
  def bopBitAnd (left: Value, right: Value): Value = {
    val lnum = ToInt32(left)
    val rnum = ToInt32(right)
    (lnum, rnum) match {
      case (_, UIntSingle(0)) =>  Value(UIntSingle(0))
      case (UIntSingle(0), _) =>  Value(UIntSingle(0))
      case (UIntSingle(n1), UIntSingle(n2)) =>  Value(UIntSingle(n1.toInt&n2.toInt))
      case (UIntSingle(n1), NUIntSingle(n2)) =>  Value(UIntSingle(n1.toInt&n2.toInt))
      case (UIntSingle(n1), _) =>  Value(UInt)
      case (NUIntSingle(n1), UIntSingle(n2)) =>  Value(UIntSingle(n1.toInt&n2.toInt))
      case (NUIntSingle(n1), NUIntSingle(n2)) =>
        if (n1<0 && n2<0) Value(NUIntSingle(n1.toInt&n2.toInt))
        else Value(UIntSingle(n1.toInt&n2.toInt))
      case (NUIntSingle(n1), UInt) =>  Value(UInt)
      case (NUIntSingle(n1), NUInt) =>
        if (n1>0) Value(UInt)
        else Value(NumTop)
      case (UInt, _) =>  Value(UInt)
      case (NUInt, UIntSingle(n2)) =>  Value(UInt)
      case (NUInt, UInt) =>  Value(UInt)
      case (NUInt, NUIntSingle(n2)) =>
        if (n2>0) Value(UInt)
        else Value(NumTop)
      case (NUInt, NUInt) =>  Value(NumTop)
      case _ =>  Value(NumTop)
    }
  }
  /* ^ */
  def bopBitXor (left: Value, right: Value): Value = {
    val lnum = ToInt32(left)
    val rnum = ToInt32(right)
    (lnum, rnum) match {
      case (UIntSingle(0), n) =>  Value(n)
      case (n, UIntSingle(0)) =>  Value(n)
      case (UIntSingle(n1), UIntSingle(n2)) =>  Value(UIntSingle(n1.toInt^n2.toInt))
      case (UIntSingle(n1), UInt) => Value(UInt)
      case (UIntSingle(n1), NUIntSingle(n2)) =>
        if (n2>0) Value(UIntSingle(n1.toInt^n2.toInt))
        else Value(NUIntSingle(n1.toInt^n2.toInt))
      case (UIntSingle(n1), NUInt) =>  Value(NumTop)
      case (NUIntSingle(n1), UIntSingle(n2)) =>
        if (n1>0) Value(UIntSingle(n1.toInt^n2.toInt))
        else Value(NUIntSingle(n1.toInt^n2.toInt))
      case (NUIntSingle(n1), NUIntSingle(n2)) =>
        if ((n1>0&&n2>0) || (n1<0&&n2<0)) Value(UIntSingle(n1.toInt^n2.toInt))
        else Value(NUIntSingle(n1.toInt^n2.toInt))
      case (NUIntSingle(n1), UInt) =>
        if (n1>0) Value(UInt)
        else Value(NUInt)
      case (NUIntSingle(n1), NUInt) =>  Value(NumTop)
      case (UInt, UIntSingle(n2)) =>  Value(UInt)
      case (UInt, UInt) =>  Value(UInt)
      case (UInt, NUIntSingle(n2)) =>
        if (n2>0) Value(UInt)
        else Value(NUInt)
      case (UInt, NUInt) =>  Value(NumTop)
      case (NUInt, _) =>  Value(NumTop)
      case _ =>  Value(NumTop)
    }
  }
  /* << */
  def bopLShift (left: Value, right: Value): Value = {
    val lnum = ToInt32(left)
    val rnum = ToUInt32(right)
    val shiftCount = rnum match {
      case UIntSingle(n) =>  n.toInt&0x1F	// one shift count
      case NUIntSingle(n) =>  n.toInt&0x1F	// one shift count
      case _ =>  0x20  // multi shift count
    }
    lnum match {
      case UIntSingle(n) =>
        if (shiftCount!=0x20) {	// one shift count
          val res = n.toInt<<shiftCount
          if (res>=0) Value(UIntSingle(res))
          else Value(NUIntSingle(res))
        }
        else Value(NumTop)	// multi shift count
      case NUIntSingle(n) =>
        if (shiftCount!=0x20) {	// one shift count
          val res = n.toInt<<shiftCount
          if (res>=0) Value(UIntSingle(res))
          else Value(NUIntSingle(res))          
        }
        else Value(NumTop)  // multi shift count
      case _ =>  Value(NumTop)
    }
  }
  /* >> */
  def bopRShift (left: Value, right: Value): Value = {
    val lnum = ToInt32(left)
    val rnum = ToUInt32(right)
    val shiftCount = rnum match {
      case UIntSingle(n) =>	 n.toInt&0x1F  // one shift count
      case NUIntSingle(n) =>  n.toInt&0x1F  // one shift count
      case _ =>  0x20  // multi shift count
    }
    lnum match {
      case UIntSingle(n) =>
        if(shiftCount!=0x20) Value(UIntSingle(n.toInt>>shiftCount))	// one shift count
        else Value(NumTop)  // multi shift count
      case NUIntSingle(n) =>
        if(shiftCount!=0x20) Value(NUIntSingle(n.toInt>>shiftCount)) // one shift count
        else Value(NumTop)  // multi shift count        
      case UInt|NUInt =>  Value(lnum)
      case _ =>  Value(NumTop)
    }    
  }
  /* >>> */
  def bopURShift (left: Value, right: Value): Value = {
    val lnum = ToUInt32(left)
    val rnum = ToUInt32(right)
    val shiftCount = rnum match {
      case UIntSingle(n) =>  n.toInt&0x1F  // one shift count
      case NUIntSingle(n) =>  n.toInt&0x1F  // one shift count
      case _ =>  0x20  // multi shift count
    }
    lnum match {
      case UIntSingle(n) =>
        if(shiftCount != 0x20) Value(UIntSingle(n.toInt>>>shiftCount))  // one shift count
        else Value(NumTop)  // multi shift count
      case NUIntSingle(n) =>
        if(shiftCount != 0x20) Value(UIntSingle(n.toInt>>>shiftCount))  // one shift count
        else Value(NumTop)  // multi shift count
      case UInt|NUInt =>  Value(lnum)
      case _ =>  Value(NumTop)
    }
  }
  /* + */
  def bopPlus (left: Value, right: Value): Value = {
    val lpval = left.pvalue
    val rpval = right.pvalue
    val lprim = Helper.toPrimitive(left)
    val rprim = Helper.toPrimitive(right)

    def concatAbsStr(left:AbsString, right:AbsString):AbsString = {
      val str1 = AbsString.concretize(left)
      val str2 = AbsString.concretize(right)
      (str1, str2) match {
        case (Some(s1), Some(s2)) =>
          AbsString.alpha(s1.concat(s2))
        case (Some(s1), None) =>
          if (right <= StrBot)
            StrBot
          else if (s1 == "")
            right
          else
            StrTop
        case (None, Some(s2)) =>
          if (left <= StrBot)
            StrBot
          else if (s2 == "")
            left
          else
            StrTop
        case _ =>
          if (left <= StrBot || right <= StrBot)
            StrBot
          else
            StrTop
      }
        /*
        case (NumStrSingle(s1), NumStrSingle(s2)) =>
          if (s1.equals("0")) AbsString.alpha(s1.concat(s2))
          else AbsString.alpha(s1.concat(s2))
        case (NumStrSingle(s1), OtherStrSingle(s2)) =>
          if (s2.equals("")) AbsString.alpha(s1)	// "1" + ""
          else AbsString.alpha(s1.concat(s2))
        case (NumStrSingle(s1), NumStr) =>  NumStr
        case (NumStrSingle(s1), OtherStr) =>  OtherStr
        case (OtherStrSingle(s1), NumStrSingle(s2)) =>
          if (s1.equals("")) AbsString.alpha(s2)	// " + "1"
          else AbsString.alpha(s1.concat(s2))
        case (OtherStrSingle(s1), OtherStrSingle(s2))=>  AbsString.alpha(s1.concat(s2))
        case (OtherStrSingle(s1), OtherStr) =>  OtherStr
        case (OtherStrSingle(s1), NumStr) =>
          if (s1.equals("")) NumStr
          else OtherStr
        case (OtherStr, OtherStrSingle(s2)) =>  OtherStr
        case (OtherStr, OtherStr) =>  OtherStr
        case (NumStr, _) =>  StrTop	// NumStr={0, ...}, s2={""}
        case (_, _) =>  StrTop */
    }
    
    (lprim._5, rprim._5) match {
      case (StrBot, StrBot) =>
        (Helper.toNumber(lprim), Helper.toNumber(rprim)) match {
          case (NumBot, _)|(_, NumBot) =>  Value(NumBot)
          case (NaN, _) | (_, NaN) =>  Value(NaN)
          case (Infinity, Infinity) =>  Value(NumTop)	// {NaN, PosInf, NegInf}
          case (Infinity, _) | (_, Infinity) =>  Value(Infinity)
          case (PosInf, NegInf)|(NegInf, PosInf) =>  Value(NaN)
          case (PosInf, _) | (_, PosInf) =>  Value(PosInf)
          case (NegInf, _) | (_, NegInf) =>  Value(NegInf)
          case (UIntSingle(0), n) =>  Value(n)
          case (n, UIntSingle(0)) =>  Value(n)
          case (UIntSingle(n1), UIntSingle(n2)) =>  Value(UIntSingle(n1+n2))
          case (UIntSingle(n), UInt) =>  Value(UInt)
          case (UIntSingle(n1), NUIntSingle(n2)) =>
            val sum = n1+n2
            val sumInt = sum.toInt
            if (sum >= 0 && sum-sumInt==0.0) Value(UIntSingle(sum))
            else Value(NUIntSingle(sum))
          case (UInt, UIntSingle(n2)) =>  Value(UInt)
          case (UInt, NUIntSingle(n2)) =>  Value(NumTop)
          case (UInt, UInt) =>  Value(UInt)
          case (NUIntSingle(n1), NUIntSingle(n2)) =>
            val sum = n1+n2
            val sumInt = sum.toInt
            if (sum >= 0 && sum-sumInt==0.0) Value(UIntSingle(sum))
            else Value(NUIntSingle(sum))
          case (NUIntSingle(n1), UIntSingle(n2)) =>
            val sum = n1+n2
            val sumInt = sum.toInt
            if (sum >= 0 && sum-sumInt==0.0) Value(UIntSingle(sum))
            else Value(NUIntSingle(sum))
          case (NUIntSingle(n1), UInt) =>
            if (n1>=0 && (n1-n1.toInt)==0.0) Value(UInt)
            else Value(NumTop)
          case (_, _) =>  Value(NumTop)
        }
      case (l, StrBot) =>
        val res1 = concatAbsStr(l, Helper.toString(PValue(rprim._1)))
        val res2 = concatAbsStr(l, Helper.toString(PValue(rprim._2)))
        val res3 = concatAbsStr(l, Helper.toString(PValue(rprim._3)))
        val res4 = concatAbsStr(l, Helper.toString(PValue(rprim._4)))
        val res5 = concatAbsStr(l, Helper.toString(PValue(rprim._5))) 
        Value(res1 + res2 + res3 + res4 + res5) + bopPlus(Value(PValue(lprim._1, lprim._2, lprim._3, lprim._4, StrBot)), Value(rprim))
      case (StrBot, r) =>
        val res1 = concatAbsStr(Helper.toString(PValue(lprim._1)), r)
        val res2 = concatAbsStr(Helper.toString(PValue(lprim._2)), r)
        val res3 = concatAbsStr(Helper.toString(PValue(lprim._3)), r)
        val res4 = concatAbsStr(Helper.toString(PValue(lprim._4)), r)
        val res5 = concatAbsStr(Helper.toString(PValue(lprim._5)), r)
        Value(res1 + res2 + res3 + res4 + res5) + bopPlus(Value(lprim), Value(PValue(rprim._1, rprim._2, rprim._3, rprim._4, StrBot)))
      case (l, r) =>
        val resR1 = concatAbsStr(Helper.toString(PValue(lprim._1)), r)
        val resR2 = concatAbsStr(Helper.toString(PValue(lprim._2)), r)
        val resR3 = concatAbsStr(Helper.toString(PValue(lprim._3)), r)
        val resR4 = concatAbsStr(Helper.toString(PValue(lprim._4)), r)
        val resR5 = concatAbsStr(Helper.toString(PValue(lprim._5)), r)
        
        val resL1 = concatAbsStr(l, Helper.toString(PValue(rprim._1)))
        val resL2 = concatAbsStr(l, Helper.toString(PValue(rprim._2)))
        val resL3 = concatAbsStr(l, Helper.toString(PValue(rprim._3)))
        val resL4 = concatAbsStr(l, Helper.toString(PValue(rprim._4)))
        val resL5 = concatAbsStr(l, Helper.toString(PValue(rprim._5)))
        Value(resR1 + resR2 + resR3 + resR4 + resR5 + resL1 + resL2 + resL3 + resL4 + resL5) + bopPlus(Value(PValue(lprim._1, lprim._2, lprim._3, lprim._4, StrBot)), Value(PValue(rprim._1, rprim._2, rprim._3, rprim._4, StrBot))) 
    }
  }
  /* - */
  def bopMinus (left: Value, right: Value): Value = {
    val lnum = Helper.toNumber(left.pvalue) + Helper.toNumber(Helper.objToPrimitive(left.locset, "Number"))  // TODO : toNumber function for obj was not implemented yet.
    val rnum = Helper.toNumber(right.pvalue) + Helper.toNumber(Helper.objToPrimitive(right.locset, "Number"))
    // bopPlus(Value(lnum), uopMinus(Value(rnum)))
    (lnum, rnum) match {
      case (NumBot, _)|(_, NumBot) =>  Value(NumBot)
      case (NaN, _)|(_, NaN) =>  Value(NaN)
      case (Infinity, _)|(_, Infinity) =>  Value(NumTop)
      case (PosInf, PosInf)|(NegInf, NegInf) =>  Value(NaN)
      case (PosInf, _)|(_, NegInf) =>  Value(PosInf)
      case (NegInf, _)|(_, PosInf) =>  Value(NegInf)
      case (UIntSingle(n1), UIntSingle(n2)) =>
        val res = n1-n2
        if (res >= 0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (UIntSingle(n1), NUIntSingle(n2)) =>
        val res = n1-n2
        if (((res-res.toInt)==0.0) && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (NUIntSingle(n1), UIntSingle(n2)) =>  Value(NUIntSingle(n1-n2))
      case (NUIntSingle(n1), NUIntSingle(n2)) =>
        val res = n1-n2
        if (((res-res.toInt)==0.0) && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (NUIntSingle(n1), UInt) =>  Value(NUInt)
      case (UInt, NUIntSingle(n2)) =>
        if (n2<0 && (n2-n2.toInt)==0.0) Value(UInt)
        else Value(NUInt)
      case (UInt, NUInt) =>  Value(NumTop)
      case (NUInt, UIntSingle(n2)) =>  Value(NUInt)
      case (NUInt, UInt) =>  Value(NUInt)
      case (_, _) =>  Value(NumTop)
    }
  }
  /* * */
  def bopMul (left: Value, right: Value) = {
    val leftNum = Helper.toNumber(left.pvalue) + Helper.toNumber(Helper.objToPrimitive(left.locset, "Number"))  // TODO : toNumber function for obj was not implemented yet.
    val rightNum = Helper.toNumber(right.pvalue) + Helper.toNumber(Helper.objToPrimitive(right.locset, "Number"))
    (leftNum, rightNum) match {
      case (NumBot, _)|(_, NumBot) =>  Value(NumBot)
      /* 11.5.1 first */
      case (NaN, _)|(_, NaN) =>  Value(NaN)
      /* 11.5.1 third */
      case (PosInf|NegInf|Infinity, UIntSingle(0)) =>  Value(NaN)
      case (UIntSingle(0), PosInf|NegInf|Infinity) =>  Value(NaN)
      /* 11.5.1 fourth */
      case (PosInf, PosInf)|(NegInf, NegInf) =>  Value(PosInf)
      case (PosInf, NegInf)|(NegInf, PosInf) =>  Value(NegInf)
      case (Infinity, PosInf|NegInf) =>  Value(Infinity)
      case (Infinity, Infinity) =>  Value(Infinity)
      case (PosInf|NegInf, Infinity) =>  Value(Infinity)
      /* 11.5.1 fifth */
      case (PosInf, UIntSingle(_)) =>  Value(PosInf)
      case (PosInf, UInt)|(UInt, PosInf) =>  Value(NumTop)	// NaN or PosInf
      case (PosInf, NUIntSingle(n)) =>
      	if (n>0) Value(PosInf)
      	else Value(NegInf)
      case (PosInf, NUInt)|(NUInt, PosInf) =>  Value(Infinity)
      case (UIntSingle(_), PosInf) =>  Value(PosInf)
      case (NUIntSingle(n), PosInf) =>
      	if (n>0) Value(PosInf)
      	else Value(NegInf)
      case (NegInf, UIntSingle(_)) =>  Value(NegInf)
      case (NegInf, UInt)|(UInt, NegInf) =>  Value(NumTop)	// NaN or NegInf
      case (NegInf, NUIntSingle(n)) =>
      	if (n>0) Value(NegInf)
      	else Value(PosInf)
      case (NegInf, NUInt)|(NUInt, NegInf) =>  Value(Infinity)
      case (UIntSingle(_), NegInf) =>  Value(NegInf)
      case (NUIntSingle(n), NegInf) =>
      	if (n>0) Value(NegInf)
      	else Value(PosInf)
      case (Infinity, UInt)|(UInt, Infinity) =>  Value(NumTop)	// NaN or Infinity
      case (Infinity, UIntSingle(_)) =>  Value(Infinity) // 0 was filtered
      case (Infinity, NUIntSingle(_)) =>  Value(Infinity)
      case (Infinity, NUInt)|(NUInt, Infinity) =>  Value(Infinity)
      case (UIntSingle(_), Infinity) =>  Value(Infinity) // 0 was filtered
      case (NUIntSingle(_), Infinity) =>  Value(Infinity)

      /* 11.5.1 sixth */
      case (UIntSingle(0), _) =>  Value(UIntSingle(0))
      case (_, UIntSingle(0)) =>  Value(UIntSingle(0))
      case (UIntSingle(n1), UIntSingle(n2)) =>  Value(AbsNumber.alpha(n1*n2))
      case (UIntSingle(n1), UInt) =>  Value(UInt)
      case (UIntSingle(n1), NUIntSingle(n2)) =>
        val res = n1*n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (UIntSingle(n1), NUInt) =>  Value(NUInt)
      case (UInt, UIntSingle(n2)) =>  Value(UInt)
      case (UInt, UInt) =>  Value(UInt)
      case (UInt, NUIntSingle(n2)) =>  Value(NumTop) // UInt(0) or NUInt
      case (UInt, NUInt) =>  Value(NumTop) // UInt(0) or NUInt      
      case (NUIntSingle(n1), UIntSingle(n2)) =>
        val res = n1*n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (NUIntSingle(n1), UInt) =>  Value(NumTop)	// UInt(0) of NUInt
      case (NUIntSingle(n1), NUIntSingle(n2)) =>
        val res = n1*n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (NUIntSingle(n1), NUInt) =>  Value(NumTop)      
      case (NUInt, _) =>  Value(NumTop)
      case _ =>  Value(NumTop)
    }
  }
  /* / */
  def bopDiv(left: Value, right: Value): Value = {
    val leftNum = Helper.toNumber(left.pvalue) + Helper.toNumber(Helper.objToPrimitive(left.locset, "Number"))  // TODO : toNumber function for obj was not implemented yet.
    val rightNum = Helper.toNumber(right.pvalue) + Helper.toNumber(Helper.objToPrimitive(right.locset, "Number"))
    (leftNum, rightNum) match {
      case (NumBot, _)|(_, NumBot) =>  Value(NumBot)
      /* 11.5.2 first */
      case (NaN, _)|(_, NaN) =>  Value(NaN)
      /* 11.5.2 third */
      case (PosInf|NegInf|Infinity, PosInf|NegInf|Infinity) =>  Value(NaN)
      /* 11.5.2 fourth */
      case (PosInf, UIntSingle(0)) =>  Value(PosInf)
      case (NegInf, UIntSingle(0)) =>  Value(NegInf)
      case (Infinity, UIntSingle(0)) =>  Value(Infinity)
      /* 11.5.2 fifth */
      case (PosInf, UIntSingle(_)) =>  Value(PosInf)
      case (PosInf, UInt) =>  Value(PosInf)
      case (PosInf, NUIntSingle(n)) =>
      	if (n>0) Value(PosInf)
      	else Value(NegInf)
      case (PosInf, NUInt) =>  Value(Infinity)
      case (NegInf, UIntSingle(_)) =>  Value(NegInf)
      case (NegInf, UInt) =>  Value(NegInf)
      case (NegInf, NUIntSingle(n)) =>
      	if (n>0) Value(NegInf)
      	else Value(PosInf)
      case (NegInf, NUInt) =>  Value(Infinity)
      case (Infinity, _) =>  Value(Infinity)
      /* 11.5.2 sixth */
      case (_, PosInf) =>  Value(UIntSingle(0))
      case (_, NegInf) =>  Value(UIntSingle(0))
      case (_, Infinity) =>  Value(UIntSingle(0))
      /* 11.5.2  seventh */
      case (UIntSingle(0), UIntSingle(0)) =>  Value(NaN)
      case (UIntSingle(0), _) =>  Value(UIntSingle(0))
      /* 11.5.2  eighth */
      case (UIntSingle(n), UIntSingle(0)) =>  Value(PosInf)
      case (UInt, UIntSingle(0)) =>  Value(NumTop)	// UInt may have 0
      case (NUIntSingle(n), UIntSingle(0)) =>
      	if (n>0) Value(PosInf)
      	else Value(NegInf)
      case (NUInt, UIntSingle(0)) =>  Value(Infinity)
      /* 11.5.2  ninth */
      case (UIntSingle(n1), UIntSingle(n2)) =>
        val res = n1/n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (UIntSingle(n1), UInt) =>  Value(NumTop)	// UInt may have 0
      case (UIntSingle(n1), NUIntSingle(n2)) =>
        val res = n1/n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (UIntSingle(n1), NUInt) =>  Value(NumTop)      
      case (NUIntSingle(n1), UIntSingle(n2)) =>  Value(NUIntSingle(n1/n2))
      case (NUIntSingle(n1), UInt) =>  Value(NumTop)	// UInt may have 0 : Infinity
      case (NUIntSingle(n1), NUIntSingle(n2)) =>
        val res = n1/n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (NUIntSingle(n1), NUInt) =>  Value(NumTop)      
      case (UInt, _) =>  Value(NumTop)	// UInt may have 0      
      case (NUInt, UIntSingle(n2)) =>  Value(NUInt)
      case (NUInt, _) =>  Value(NumTop)	// UInt may have 0
      case _ =>  Value(NumTop)
    }
  }
  /* % */
  def bopMod(left: Value, right: Value): Value = {
    val leftNum = Helper.toNumber(left.pvalue) + Helper.toNumber(Helper.objToPrimitive(left.locset, "Number"))  // TODO : toNumber function for obj was not implemented yet.
    val rightNum = Helper.toNumber(right.pvalue) + Helper.toNumber(Helper.objToPrimitive(right.locset, "Number"))
    (leftNum, rightNum) match {
      case (NumBot, _)|(_, NumBot) =>  Value(NumBot)
       /* 11.5.3 first */
      case (NaN, _)|(_, NaN) =>  Value(NaN)
      /* 11.5.3 third */
      case (PosInf|NegInf|Infinity, _) =>  Value(NaN)
      case (_, UIntSingle(0)) =>  Value(NaN)
      /* 11.5.3 fifth */
      case (UIntSingle(0), _) =>  Value(UIntSingle(0))
      /* 11.5.3 fourth */
      case (n, PosInf|NegInf|Infinity) =>  Value(n)
      /* 11.5.3 sixth */
      case (UIntSingle(n1), UIntSingle(n2)) =>  Value(UIntSingle(n1%n2))
      case (UIntSingle(n1), UInt) =>  Value(NumTop)	// UInt may have 0
      case (UIntSingle(n1), NUIntSingle(n2)) =>
        val res = n1%n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (UIntSingle(n1), NUInt) =>  Value(NumTop)
      case (UInt, UIntSingle(n2)) =>  Value(UInt)
      case (UInt, _) =>  Value(NumTop)	// 0%0 = NaN
      case (NUIntSingle(n1), UIntSingle(n2)) =>
        val res = n1%n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (NUIntSingle(n1), NUIntSingle(n2)) =>
        val res = n1%n2
        if ((res-res.toInt)==0.0 && res>=0) Value(UIntSingle(res))
        else Value(NUIntSingle(res))
      case (NUIntSingle(n1), _) =>  Value(NumTop)
      case (NUInt, UIntSingle(n2)) =>  Value(NUInt)
      case (NUInt, _) =>  Value(NumTop)
      case _ =>  Value(NumTop)
    }
  }
  /* == */
  def bopEq(left: Value, right: Value): Value = {
    // 1
    val b1 =
      // a 
      (left._1._1 === right._1._1) +
      // b
      (left._1._2 === right._1._2) +
      // c
      (left._1._4 === right._1._4) +
      // d
      (left._1._5 === right._1._5) +
      // e
      (left._1._3 === right._1._3) +
      // f
      (if (!left._2.isEmpty && !right._2.isEmpty) {
        val intersect = left._2.intersect(right._2) 
        if (intersect.isEmpty) BoolFalse
        else if (left._2.size == 1 && right._2.size == 1 && isRecentLoc(intersect.head)) BoolTrue
        else BoolTop}
      else BoolBot)
    // 2
    val b2 = 
      if (NullTop <= left._1._2 && UndefTop <= right._1._1) BoolTrue
      else BoolBot
    // 3
    val b3 = 
      if (UndefTop <= left._1._1 && NullTop <= right._1._2) BoolTrue
      else BoolBot
    // 4
    val b4 = 
      if (left._1._4 </ NumBot && right._1._5 </ StrBot)
        left._1._4 === Helper.toNumber(PValue(right._1._5))
      else BoolBot
    // 5
    val b5 = 
      if (left._1._5 </ StrBot && right._1._4 </ NumBot)
        Helper.toNumber(PValue(left._1._5)) === right._1._4
      else BoolBot
    // 6
    val b6 = 
      if (left._1._3 </ BoolBot) {
        val num = Helper.toNumber(PValue(left._1._3))
        val b6_1 =
          if (right._1._4 </ NumBot)
            num === right._1._4
          else
            BoolBot
        val b6_4 =
          if (right._1._5 </ StrBot)
            num === Helper.toNumber(PValue(right._1._5))
          else
            BoolBot
        val b6_8 =
          if (!right._2.isEmpty)
            num === Helper.objToPrimitive(right._2, "Number")._4
          else
            BoolBot
        val b6_10 =
          if (right._1._1 </ UndefBot || right._1._2 </ NullBot)
            BoolFalse
          else
            BoolBot
        b6_1 + b6_4 + b6_8 + b6_10
      }
      else
        BoolBot
    // 7
    val b7 = 
      if (right._1._3 </ BoolBot) {
        val num = Helper.toNumber(PValue(right._1._3))
        val b7_1 =
          if (left._1._4 </ NumBot)
            left._1._4 === num
          else
            BoolBot
        val b7_4 =
          if (left._1._5 </ StrBot)
            Helper.toNumber(PValue(left._1._5)) === num
          else
            BoolBot
        val b7_8 =
          if (!left._2.isEmpty)
            Helper.objToPrimitive(left._2, "Number")._4 === num
          else
            BoolBot
        val b7_10 =
          if (left._1._1 </ UndefBot || left._1._2 </ NullBot)
            BoolFalse
          else
            BoolBot
        b7_1 + b7_4 + b7_8 + b7_10
      }
      else
        BoolBot
    // 8
    val b8 =
      if (!right._2.isEmpty) {
        val b8_num =
          if (left._1._4 </ NumBot)
            left._1._4 === Helper.objToPrimitive(right._2, "Number")._4
          else
            BoolBot
        val b8_str =
          if (left._1._5 </ StrBot)
            left._1._5 === Helper.objToPrimitive(right._2, "Number")._5
          else
            BoolBot
        b8_num + b8_str
      }
      else
        BoolBot
    // 9
    val b9 =
      if (!left._2.isEmpty) {
        val b9_num =
          if (right._1._4 </ NumBot)
            right._1._4 === Helper.objToPrimitive(left._2, "Number")._4
          else
            BoolBot
        val b9_str =
          if (right._1._5 </ StrBot)
            right._1._5 === Helper.objToPrimitive(left._2, "Number")._5
          else
            BoolBot
        b9_num + b9_str
      }
      else
        BoolBot
    // 10
    val b10 =
      if (   (   (left._1._1 </ UndefBot || left._1._2 </ NullBot) 
              && (right._1._4 </ NumBot || right._1._5 </ StrBot || !right._2.isEmpty))
          || (   (right._1._1 </ UndefBot || right._1._2 </ NullBot) 
              && (left._1._4 </ NumBot || left._1._5 </ StrBot || !left._2.isEmpty)))
        BoolFalse
      else
        BoolBot
    Value(b1 + b2 + b3 + b4 + b5 + b6 + b7 + b8 + b9 + b10)
  }
  
  /* != */
  def bopNeq(left: Value, right: Value): Value = {
    bopEq(left,right).pvalue._3 match {
      case BoolTrue =>  Value(BoolFalse)
      case BoolFalse =>  Value(BoolTrue)
      case BoolTop =>  Value(BoolTop)
      case BoolBot =>  Value(BoolBot)
    }
  }
  
  /* === */
  def bopSEq(left: Value, right: Value): Value = {
    // 1
    val b_type =
      if ((left + right).typeCount > 1)
        BoolFalse
      else
        BoolBot
    val b_same =
      // 2
      (left._1._1 === right._1._1) +
      // 3
      (left._1._2 === right._1._2) +
      // 4
      (left._1._4 === right._1._4) +
      // 5
      (left._1._5 === right._1._5) +
      // 6
      (left._1._3 === right._1._3) +
      // 7
      (if (!left._2.isEmpty && !right._2.isEmpty) {
        val intersect = left._2.intersect(right._2) 
        if (intersect.isEmpty) BoolFalse
        else if (left._2.size == 1 && right._2.size == 1 && isRecentLoc(intersect.head)) BoolTrue
        else BoolTop}
      else BoolBot)
    Value(b_type + b_same)
  }
  
  /* !== */
  def bopSNeq(left: Value, right: Value): Value = {
    bopSEq(left, right).pvalue._3 match {
      case BoolTrue =>  Value(BoolFalse)
      case BoolFalse =>  Value(BoolTrue)
      case BoolTop =>  Value(BoolTop)
      case BoolBot =>  Value(BoolBot)
    }
  }
  /* < */
  def bopLess(left: Value, right: Value): Value = {
   	_bopLess(Helper.toPrimitive(left), Helper.toPrimitive(right))
  }
  def _bopLess(px:PValue, py:PValue): Value = {
    (px.strval, py.strval) match {
      case (StrBot, _) | (_, StrBot) =>
        val nx = Helper.toNumber(px)
        val ny = Helper.toNumber(py)
        compLessNumNum(nx, ny) match {
          case PValue(UndefTop,_,_,_,_) =>  Value(BoolFalse)
          case b =>  Value(b)
        }
      case (s1, s2) =>  // String < String
        val lnext = PValue(px._1, px._2, px._3, px._4, StrBot)
        val rnext = PValue(py._1, py._2, py._3, py._4, StrBot)
      	Value(compLessStrStr(s1, s2)) + _bopLess(px, rnext) + _bopLess(lnext, py) 
    }
  }
  /* > */
  def bopGreater(left: Value, right: Value): Value = {
   	_bopGreater(Helper.toPrimitive(left), Helper.toPrimitive(right))
  }
  def _bopGreater(px:PValue, py:PValue): Value = {
    (py.strval, px.strval) match {
      case (StrBot, _) | (_, StrBot) =>
        val nx = Helper.toNumber(px)
        val ny = Helper.toNumber(py)
        compLessNumNum(ny, nx) match {
          case PValue(UndefTop,_,_,_,_) =>  Value(BoolFalse)
          case b =>  Value(b)
        }
      case (s2, s1) => { // String < String
        val lnext = PValue(px._1, px._2, px._3, px._4, StrBot)
        val rnext = PValue(py._1, py._2, py._3, py._4, StrBot)
        Value(compLessStrStr(s2, s1)) + _bopGreater(lnext, py) + _bopGreater(px, rnext)
      }
    }
  }

  /* <= */
  def bopLessEq(left: Value, right: Value): Value = {
   	_bopLessEq(Helper.toPrimitive(left), Helper.toPrimitive(right))
  }
  def _bopLessEq(px:PValue, py:PValue): Value = {
    (py.strval, px.strval) match {
      case (StrBot, _) | (_, StrBot) =>
        val nx = Helper.toNumber(px)
        val ny = Helper.toNumber(py)
        compLessNumNum(ny, nx) match {
          case PValue(UndefTop,_,_,_,_) =>  Value(BoolFalse)
          case PValue(_, _, BoolTrue, _, _) =>  Value(BoolFalse)
          case PValue(_, _, BoolFalse, _, _) =>  Value(BoolTrue)
          case n =>  Value(n)
        }
      case (s2, s1) =>	// String < String
        val lnext = PValue(px._1, px._2, px._3, px._4, StrBot)
        val rnext = PValue(py._1, py._2, py._3, py._4, StrBot)
        val strRes = compLessStrStr(s2, s1) match {
          case BoolTrue =>  Value(BoolFalse)
          case BoolFalse =>  Value(BoolTrue)
          case n =>  Value(n)
        }
        strRes + _bopLessEq(lnext, py) + _bopLessEq(px, rnext)
    }
  }

  /* >= */
  def bopGreaterEq(left: Value, right: Value): Value = {
   	_bopGreaterEq(Helper.toPrimitive(left), Helper.toPrimitive(right))
  }
  def _bopGreaterEq(px:PValue, py:PValue):Value = {
    (px.strval, py.strval) match {
      case (StrBot, _) | (_, StrBot)=>
        val nx = Helper.toNumber(px)
        val ny = Helper.toNumber(py)
        compLessNumNum(nx, ny) match {
          case PValue(UndefTop,_,_,_,_) =>  Value(BoolFalse)
          case PValue(_, _, BoolTrue, _, _) =>  Value(BoolFalse)
          case PValue(_, _, BoolFalse, _, _) =>  Value(BoolTrue)
          case n =>  Value(n)
        }
      case (s1, s2) =>	// String < String
        val lnext = PValue(px._1, px._2, px._3, px._4, StrBot)
        val rnext = PValue(py._1, py._2, py._3, py._4, StrBot)
        val strRes = compLessStrStr(s1, s2) match {
          case BoolTrue =>  Value(BoolFalse)
          case BoolFalse =>  Value(BoolTrue)
          case n =>  Value(n)
        }
        strRes + _bopGreaterEq(lnext, py) + _bopGreaterEq(px, rnext)
    }
  }

  private def compLessStrStr(px:AbsString, py:AbsString): AbsBool = {
    val str1 = AbsString.concretize(px)
    val str2 = AbsString.concretize(py)
    (str1, str2) match {
      case (Some(s1), Some(s2)) =>
        if (s1 < s2)
          BoolTrue
        else
          BoolFalse
      case _ =>
        if (px <= StrBot || py <= StrBot)
          BoolBot
        else
          BoolTop
    }
    /*
    (px, py) match {
      case (NumStrSingle(s1), NumStrSingle(s2)) =>
        if (s1.compareTo(s2)<0) BoolTrue
        else BoolFalse
      case (NumStrSingle(s1), OtherStrSingle(s2)) =>
        if (s1.compareTo(s2)<0) BoolTrue
        else BoolFalse
	  case (OtherStrSingle(s1), NumStrSingle(s2)) =>
	    if (s1.compareTo(s2)<0) BoolTrue
	    else BoolFalse
	  case (OtherStrSingle(s1), OtherStrSingle(s2)) =>
	    if (s1.compareTo(s2)<0) BoolTrue
	    else BoolFalse
	  case (OtherStrSingle(n1), NumStr) =>
	    if (n1.compareTo("0")<0) BoolTrue
	    else BoolTop
	  case (NumStr, NumStrSingle(n2)) =>
	    if (n2.compareTo("0")==0) BoolFalse
	    else BoolTop
	  case (NumStr, OtherStrSingle(n2)) =>
	    if (n2.compareTo("0")<0) BoolFalse
	    else BoolTop
	  case _ =>  BoolTop
	} */
  }
  
  // 11.8.5 The Abstract Relational Comparison Algorithm
  def compLessNumNum(nx:AbsNumber, ny:AbsNumber):PValue = {
    // 3.c.
    (nx, ny) match {
      case (NumBot, _) =>  PValue(BoolBot)
      case (_, NumBot) =>  PValue(BoolBot)
      // 11.8.5.3.c, 11.8.5.3.d
      case (NaN, _) | (_, NaN) =>  PValue(UndefTop)
      // 11.8.5.3.e
      case (PosInf, PosInf) | (NegInf, NegInf) =>  PValue(BoolFalse)
      // 11.8.5.3.h, 11.8.5.3.i, 11.8.5.3.j, 11.8.5.3.k
      case (Infinity, NegInf) | (PosInf, Infinity) =>  PValue(BoolFalse)
      case (Infinity, _) | (_, Infinity) =>  PValue(BoolTop)
      case (PosInf, _) =>  PValue(BoolFalse)
      case (_, PosInf) =>  PValue(BoolTrue)
      case (NegInf, _) =>  PValue(BoolTrue)
      case (_, NegInf) =>  PValue(BoolFalse)
      // 11.8.5.3.l
      case (UIntSingle(n1), UIntSingle(n2)) =>
        if (n1<n2) PValue(BoolTrue)
        else PValue(BoolFalse)
      case (UIntSingle(n1), NUIntSingle(n2)) =>
        if (n1<n2) PValue(BoolTrue)
        else PValue(BoolFalse)
	  case (NUIntSingle(n1), NUIntSingle(n2)) =>
        if (n1<n2) PValue(BoolTrue)
        else PValue(BoolFalse)
	  case (NUIntSingle(n1), UIntSingle(n2)) =>
	    if (n1<n2) PValue(BoolTrue)
	    else PValue(BoolFalse)
	  case (NUIntSingle(n1), UInt) =>
	    if (n1<0) PValue(BoolTrue)
	    else PValue(BoolTop)
	  case (UInt, UIntSingle(n2)) =>
	    if (n2==0) PValue(BoolFalse)
	    else PValue(BoolTop)
	  case (UInt, NUIntSingle(n2)) =>
	    if (n2<0) PValue(BoolFalse)
	    else PValue(BoolTop)
	  case _ =>  PValue(BoolTop)
	}
  }

  // 5.2 Algorithm Conventions. The notation "x modulo y" computes ...
  def modulo(x:Double, y:Long):Long = {
    var result = math.abs(x.toLong) % math.abs(y)
    if(math.signum(x) < 0) return math.signum(y) * (math.abs(y) - result)
    math.signum(y) * result
  }

  def ToInteger(value: Value): AbsNumber = {
    val number = Helper.toNumber(Helper.toPrimitive(value))
    number match {
      case NaN => AbsNumber.alpha(0)
      case Infinity | PosInf | NegInf | UIntSingle(_) => number
      case NUIntSingle(n) => AbsNumber.alpha(math.signum(n)*math.floor(math.abs(n)))
      case UInt => UInt
      case NUInt => NumTop
      case NumBot => NumBot
      case NumTop => NumTop
    }
  }

  def ToInt32(value:Value):AbsNumber = {
    val pv = value.pvalue
    val number = Helper.toNumber(pv) + Helper.toNumber(Helper.objToPrimitive(value.locset, "Number"))
    number match {
      case NaN | PosInf | NegInf | Infinity =>  UIntSingle(0)
      case UIntSingle(n) =>  UIntSingle(n)        
      case NUIntSingle(n)=>
       val posInt = math.signum(n)*math.floor(math.abs(n))
        val int32bit = modulo(posInt, 0x100000000L);
        if (int32bit >= 0x80000000L) {
          val int32bitS = int32bit-0x100000000L
          if (int32bitS>=0) UIntSingle(int32bitS.toInt)
          else NUIntSingle(int32bitS.toInt)
        }
        else {
          if (int32bit>=0) UIntSingle(int32bit.toInt)
          else NUIntSingle(int32bit.toInt)
        }
      case UInt =>  UInt
      case NUInt =>  NumTop
      case _ =>  NumTop
    }
  }

  def ToUInt32(value:Value):AbsNumber = {
    val pv = value.pvalue
    val number = Helper.toNumber(pv) + Helper.toNumber(Helper.objToPrimitive(value.locset, "Number"))
    number match {
      case NaN | UIntSingle(0) | PosInf | NegInf | Infinity =>  UIntSingle(0)
      case UIntSingle(n) =>
        var posInt = math.signum(n)*math.floor(math.abs(n))
        val int32bit = modulo(posInt, 0x100000000L);
        UIntSingle(int32bit.toInt)
      case NUIntSingle(n) =>
        var posInt = math.signum(n)*math.floor(math.abs(n))
        val int32bit = modulo(posInt, 0x100000000L);
        UIntSingle(int32bit.toInt)
      case NumBot => NumBot
      case _ =>  UInt
    }
  }

  def ToUInt16(value:Value):AbsNumber = {
    val pv = value.pvalue
    val number = Helper.toNumber(pv) + Helper.toNumber(Helper.objToPrimitive(value.locset, "Number"))
    number match {
      case NaN | UIntSingle(0) | PosInf | NegInf | Infinity =>  UIntSingle(0)
      case UIntSingle(n) =>
        var posInt = math.signum(n)*math.floor(math.abs(n))
        val int16bit = modulo(posInt, 0x10000L);
        UIntSingle(int16bit.toInt)
      case NUIntSingle(n) =>
        var posInt = math.signum(n)*math.floor(math.abs(n))
        val int16bit = modulo(posInt, 0x10000L);
        UIntSingle(int16bit.toInt)
      case _ =>  UInt
    }
  }

  def parseInt(string: AbsString, radix: AbsNumber) : AbsNumber = {
    // radix match {
    //   case NumTop | UInt => NumTop
    //   case UIntSingle(n) =>
    //     if (n < 2 || n > 36) {
    //       NaN
    //     } else {
    //       val stripPrefix =
    //         if (n != 16) false
    //         else true
    //       // TODO
    //       NumTop
    //     }
    // }
    NumTop
  }
}
