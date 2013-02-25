/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic

import _root_.java.util.{List => JList}
import kr.ac.kaist.jsaf.interpreter.Interpreter
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.EJSOp
import kr.ac.kaist.jsaf.nodes_util.{ IRFactory => IF }
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._

import scala.collection.mutable.HashMap
import scala.util.Random
/* Now, consider only binary operation and integer type. 
 * Reference:
 * statement    ::= begin 
                  | assign 
                  | if comparison then statements (else statements)?
                  | while comparison {statements}
 * assign       ::= variable = expression
 * variable     ::= local variable
 * expression   ::= constant | variable | binop
 * binop        ::= variable op variable
 * op           ::= + | - | * | / | %
 * comparison   ::= variable cmp variable
 * cmp          ::= < | <= | > | >= | == | !=
 */
class SymbolicHelper(I: Interpreter) {
  val symbolicMemory = new HashMap[String, String]
  val symbol = "s"
  val input_symbol = "i"
  var index, input_index = 0
  
  var report = List[Info]()
  var input = List[Int]()
  var random = new Random()

  var max_depth = 3
  var depth = 0

  def initialize(I: List[Int]) = {
    System.out.println("Initialize()")
    input = I
    index = 0
    input_index = 0
    symbolicMemory.clear()
    report = List[Info]()
  }
  def executeAssignment(id: IRId, expr: IRExpr, c1: Option[String], c2: Option[String]) = expr match {
    /* variable op varialbe */
    //TODO: extend the range to cover all expressions, first and second
    case SIRBin(_, first, op, second) => first match {
      case v1:IRId => second match {
        case v2:IRId => op.getKind match {
          //TODO: find simple way to distinguish operation type 
          // op is supported by the constraint solver
          case EJSOp.BIN_COMP_REL_INSTANCEOF => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_REL_IN => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_BIT_SHIFT_LEFT => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_BIT_SHIFT_SRIGHT => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_BIT_SHIFT_USRIGHT => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_REL_LESS => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_REL_GREATER => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_REL_LESSEQUAL => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_REL_GREATEREQUAL => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_EQ_EQUAL => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_EQ_NEQUAL => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_EQ_SEQUAL => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_COMP_EQ_SNEQUAL => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_BIT_BIT_AND => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_BIT_BIT_XOR => symbolicMemory -= id.getUniqueName
          case EJSOp.BIN_BIT_BIT_OR => symbolicMemory -= id.getUniqueName
          //TODO: how to handle the case, concrete value is error
          case _ => 
            System.out.println("EXECUTE_ASSIGNMENT()")
            if (symbolicMemory.contains(v1.getUniqueName) &&
                symbolicMemory.contains(v2.getUniqueName)) {
              // only if linear constraints supported
              var context = ""
              if (op.getKind == EJSOp.BIN_ARITH_MUL_MULTIPLICATION ||
                  op.getKind == EJSOp.BIN_ARITH_MUL_DIVISION ||
                  op.getKind == EJSOp.BIN_ARITH_MUL_REMINDER) {
                  
                val c = c2 match { case Some(c) => c
                                     case None => "" }
                //TODO: consider when c is undecided. For example, x = y * z
                context = symbolicMemory(v1.getUniqueName) + op.getText + c 
              } 
              else {
                context = symbolicMemory(v1.getUniqueName) + op.getText + symbolicMemory(v2.getUniqueName)
              }
              val sid = symbol + index
              symbolicMemory(id.getUniqueName) = sid
              index += 1
           
              val info = new Info(false, sid, Some(op.getText), context, None)
              report = report:+info
            }
            else if (symbolicMemory.contains(v1.getUniqueName)) {
              val c = c2 match { case Some(c) => c
                                 case None => "" }
              //TODO: consider when c is undecided. For example, x = y * z
              val context = symbolicMemory(v1.getUniqueName) + op.getText + c 
              val sid = symbol + index
              symbolicMemory(id.getUniqueName) = sid
              index += 1
              val info = new Info(false, sid, Some(op.getText), context, None)
              report = report:+info
            }
            else if (symbolicMemory.contains(v2.getUniqueName)) {
              val c = c1 match { case Some(c) => c
                                 case None => "" }
              //TODO: consider when c is undecided. For example, x = y * z
              val context = symbolicMemory(v2.getUniqueName) + op.getText + c 
              val sid = symbol + index
              symbolicMemory(id.getUniqueName) = sid
              index += 1
              val info = new Info(false, sid, Some(op.getText), context, None)
              report = report:+info
            }
            else 
              symbolicMemory -= id.getUniqueName
        }
      }
    }
    case SIRUn(_, op, expr) =>
    case SIRLoad(_, obj, index) =>

    /* variable */
    case v:IRId =>
      System.out.println("EXECUTE_ASSIGNMENT()")
      if (symbolicMemory.contains(v.getUniqueName)) {
        symbolicMemory(id.getUniqueName) = symbolicMemory(v.getUniqueName)
        // Do not need to report because symbolic memory is replaced
      }
      else
        symbolicMemory -= id.getUniqueName

    case _:IRThis =>

    /* constant value */
    case n:IRNumber =>
      System.out.println("EXECUTE_ASSIGNMENT()")
      symbolicMemory -= id.getUniqueName

    case s:IRString =>
    case b:IRBool =>
    case _:IRUndef =>
    case _:IRNull =>
  }

  def getInput(id: IRId):Int = {
    System.out.println("GET_INPUT()")
    //TODO: find other ways to generate symbolic/input identifier
    symbolicMemory(id.getUniqueName) = symbol + index
    val info = new Info(false, symbol + index, None, input_symbol + input_index, None)
    report = report:+info

    // TODO: decide limit on random number 
    // For simplicity, for now
    var res = random.nextInt(10)
    if (input_index < input.length)
      res = input(input_index)
    // FOR DEBUGGING
    System.out.println("GET_INPUT: " + res);

    index += 1
    input_index += 1
    return res
  }

  def executeCondition(expr: IRExpr, branchTaken: Option[Boolean], c1: Option[String], c2: Option[String]) = expr match {
    case SIRBin(_, first, op, second) => first match {
      case v1:IRId => second match {
        case v2:IRId => op.getKind match {
          //TODO: find simple way to distinguish operation type 
          case EJSOp.BIN_COMP_REL_INSTANCEOF => 
          case EJSOp.BIN_COMP_REL_IN =>
          case EJSOp.BIN_ARITH_MUL_MULTIPLICATION => 
          case EJSOp.BIN_ARITH_MUL_DIVISION => 
          case EJSOp.BIN_ARITH_MUL_REMINDER => 
          case EJSOp.ETC_PLUS => 
          case EJSOp.ETC_MINUS => 
          case EJSOp.BIN_BIT_SHIFT_LEFT => 
          case EJSOp.BIN_BIT_SHIFT_SRIGHT => 
          case EJSOp.BIN_BIT_SHIFT_USRIGHT => 
          case EJSOp.BIN_COMP_EQ_SEQUAL => 
          case EJSOp.BIN_COMP_EQ_SNEQUAL => 
          case EJSOp.BIN_BIT_BIT_AND => 
          case EJSOp.BIN_BIT_BIT_XOR => 
          case EJSOp.BIN_BIT_BIT_OR => 
          //TODO: how to handle the case, concrete value is error
          //TODO: construct branch bitvector
          case _ =>
            System.out.println("EXECUTE_CONDITION()")
            if (symbolicMemory.contains(v1.getUniqueName) &&
                symbolicMemory.contains(v2.getUniqueName)) {
                val context = symbolicMemory(v1.getUniqueName) + op.getText + symbolicMemory(v2.getUniqueName)
                val info = new Info(true, "", Some(op.getText), context, branchTaken)
                report = report:+info
            }
            else if (symbolicMemory.contains(v1.getUniqueName)) {
              val c = c2 match { case Some(c) => c
                                 case None => "" }
              //TODO: consider when c is undefined. For example, x = y * z
              val context = symbolicMemory(v1.getUniqueName) + op.getText + c 
              val info = new Info(true, "", Some(op.getText), context, branchTaken)
              report = report:+info
            }
            else if (symbolicMemory.contains(v2.getUniqueName)) {
              val c = c1 match { case Some(c) => c
                                 case None => "" }
              //TODO: consider when c is undecided. For example, x = y * z
              val context = symbolicMemory(v2.getUniqueName) + op.getText + c 
              val info = new Info(true, "", Some(op.getText), context, branchTaken)
              report = report:+info
            }
        }
      }
    }
  }
  
  def checkLoop():Boolean = {
    System.out.println("CHECK_LOOP()")
    if (depth < max_depth) {
      depth = depth + 1
      return true
    }
    else {
      depth = 0
      return false
    }
  }
  def toStr(expr: IRExpr): String = expr match {
    case SIRBin(_, first, op, second) =>
      op.getText + toStr(expr)
    case SIRLoad(_, obj, index) =>
      obj.getOriginalName + "[" + toStr(index) + "]"
    case id:IRId => id.getOriginalName
    case _:IRThis => "this"
    case n:IRNumber => n.getText
    case s:IRString => s.getStr
    case b:IRBool => b.isBool.toString
    case _:IRUndef => "undefined"
    case _:IRNull => "null"
  }

  def print() = {
    System.out.println("Symbolic memory = " +  symbolicMemory.toString)
    System.out.println("Symbolic report = " + report.map(_.expr))
    System.out.println("Input = " + input.toString)
  }
}
