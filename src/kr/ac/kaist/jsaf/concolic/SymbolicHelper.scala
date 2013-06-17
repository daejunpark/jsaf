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

import scala.collection.mutable.HashMap
import scala.util.Random
/* Now, consider only binary operation and integer type. 
 * Reference:
 * s    ::= begin 
          | x = e
          | x = x(x, x)
          | x = function f (x, x) {s*}
          | function f (x, x) {s*}
          | return e?
          | var x
          | s*
          | if (e) then s (else s)?
          | while (e) s
 * e    ::= e op e
          | x
          | num
          | this
          | true
          | false
 * op   ::= + | - | * | / | % | < | <= | > | >= | == | !=
 */
class SymbolicHelper(I: Interpreter) {
  val symbolic_memory = new HashMap[String, String]
  val symbol = "s"
  val input_symbol = "i"
  var index, input_index = 0
  
  var report = List[Info]()
  // Generated input datas
  var input = List[Int]()
  var random = new Random()

  var max_depth = 3
  var depth = 0

  // Function information
  val function_info = new HashMap[String, FunctionInfo]
  function_info("<>Concolic<>Main") = new FunctionInfo()
  function_info("<>Concolic<>Main").is_target = true
  function_info("<>Concolic<>Main").is_candidate = true

  // Mapping symbolic helper function to environment in which the function is defined
  var environments = new HashMap[String, IRId]

  def initialize(I: List[Int], covered: String, target: String) = {
    System.out.println("Initialize()")
    input = I
    index = 0
    input_index = 0
    symbolic_memory.clear()
    report = List[Info]()

    if (covered.length != 0)
      function_info(covered).is_covered = true

    if (target.length != 0) {
      function_info(target).is_target = true
      System.out.println("target: ", target, function_info(target).is_target)
    }
  }
 
  def storeEnvironment(v: IRId, env: IRId) =
    environments(v.getUniqueName) = env
  
  def getEnvironment(v: IRId) = environments(v.getUniqueName)

  /* If its environment, that is the function in which the 'executeAssignment' statement is instrumented is targeted, 
   * the symbolic execution proceeds and its change is reported to build symbolic execution tree. 
   * Otherwise, just report symbolic variables which don't represent local variables. 
   * When local variables are in that expressions, just use concrete value instead of symbolic varialbes of local variables. 
   */
  def executeAssignment(loc: String, id: IRId, expr: IRExpr, c1: Option[String], c2: Option[String], env: IRId) = { 
    if (checkFocus(env)) {
      expr match {
        /* variable op varialbe */
        //TODO: extend the range to cover all expressions, first and second
        case SIRBin(_, first, op, second) => first match {
          case v1:IRId => second match {
            case v2:IRId => op.getKind match {
              //TODO: find simple way to distinguish operation type 
              // op is supported by the constraint solver
              case EJSOp.BIN_COMP_REL_INSTANCEOF => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_COMP_REL_IN => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_BIT_SHIFT_LEFT => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_BIT_SHIFT_SRIGHT => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_BIT_SHIFT_USRIGHT => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_COMP_EQ_SEQUAL => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_COMP_EQ_SNEQUAL => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_BIT_BIT_AND => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_BIT_BIT_XOR => symbolic_memory -= id.getUniqueName
              case EJSOp.BIN_BIT_BIT_OR => symbolic_memory -= id.getUniqueName
              case _ => 
                // BIN_ARITH_MUL_MULTIPLICATION
                // BIN_ARITH_MUL_DIVISION
                // BIN_ARITH_MUL_REMINDER
                // ETC_PLUS
                // ETC_MINUS

                // BIN_COMP_REL_LESS
                // BIN_COMP_REL_GREATER
                // BIN_COMP_REL_LESSEQUAL
                // BIN_COMP_REL_GREATEREQUAL
                // BIN_COMP_EQ_EQUAL
                // BIN_COMP_EQ_NEQUAL
                
                System.out.println("EXECUTE_ASSIGNMENT()")
                if (symbolic_memory.contains(v1.getUniqueName) || symbolic_memory.contains(v2.getUniqueName)) {
                  //TODO: When c2 or c1 are None, we have to error reporting. 
                  var res1 = c1 match {
                    case Some(c) => c
                    case None => ""
                  }
                  var res2 = c2 match {
                    case Some(c) => c
                    case None => ""
                  }
                  var context = ""
                  if (symbolic_memory.contains(v1.getUniqueName) && symbolic_memory.contains(v2.getUniqueName)) {
                    // only if linear constraints supported
                    if (op.getKind == EJSOp.BIN_ARITH_MUL_MULTIPLICATION ||
                        op.getKind == EJSOp.BIN_ARITH_MUL_DIVISION ||
                        op.getKind == EJSOp.BIN_ARITH_MUL_REMINDER) {
                      context = symbolic_memory(v1.getUniqueName) + op.getText + res2 
                    } 
                    else 
                      context = symbolic_memory(v1.getUniqueName) + op.getText + symbolic_memory(v2.getUniqueName)
                  }
                  else if (symbolic_memory.contains(v1.getUniqueName)) {
                    context = symbolic_memory(v1.getUniqueName) + op.getText + res2 
                  }
                  else if (symbolic_memory.contains(v2.getUniqueName)) {
                    context = symbolic_memory(v2.getUniqueName) + op.getText + res1 
                  }
                  val sid = symbol + index
                  symbolic_memory(id.getUniqueName) = sid
                  index += 1
                  val info = new Info(false, sid, Some(op.getText), context, None)
                  report = report:+info
                }
                else 
                  symbolic_memory -= id.getUniqueName
            }
          //TODO: Error reporting. 
          }
        }
        case SIRUn(_, op, expr) =>
        case SIRLoad(_, obj, index) =>

        /* variable */
        case v:IRId =>
          System.out.println("EXECUTE_ASSIGNMENT()")
          if (symbolic_memory.contains(v.getUniqueName)) {
            symbolic_memory(id.getUniqueName) = symbolic_memory(v.getUniqueName)
            // Do not need to report because symbolic memory is replaced
          }
          else
            symbolic_memory -= id.getUniqueName

        case _:IRThis =>

        /* constant value */
        case n:IRNumber =>
          System.out.println("EXECUTE_ASSIGNMENT()")
          symbolic_memory -= id.getUniqueName

        case s:IRString =>
        case b:IRBool =>
        case _:IRUndef =>
        case _:IRNull =>
      }
    }
    else {
      //TODO: What about wrong variable?
      if (!loc.equals("LocalVariable")) {
        expr match {
          case SIRBin(_, first, op, second) => first match {
            case v1:IRId => second match {
              case v2:IRId => op.getKind match {
                case EJSOp.BIN_COMP_REL_INSTANCEOF => 
                case EJSOp.BIN_COMP_REL_IN =>    
                case EJSOp.BIN_BIT_SHIFT_LEFT =>                
                case EJSOp.BIN_BIT_SHIFT_SRIGHT =>                 
                case EJSOp.BIN_BIT_SHIFT_USRIGHT =>                
                case EJSOp.BIN_COMP_EQ_SEQUAL =>                 
                case EJSOp.BIN_COMP_EQ_SNEQUAL =>                 
                case EJSOp.BIN_BIT_BIT_AND =>                 
                case EJSOp.BIN_BIT_BIT_XOR =>                   
                case EJSOp.BIN_BIT_BIT_OR =>
                case _ => 
                  System.out.println("EXECUTE_ASSIGNMENT()")
                  if (symbolic_memory.contains(v1.getUniqueName) || symbolic_memory.contains(v2.getUniqueName)) {
                    var res1 = c1 match {
                      case Some(c) => c
                      case None => ""
                    }
                    var res2 = c2 match {
                      case Some(c) => c
                      case None => ""
                    }
                    var context = ""
                    if (symbolic_memory.contains(v1.getUniqueName) && symbolic_memory.contains(v2.getUniqueName)) {
                      if (op.getKind == EJSOp.BIN_ARITH_MUL_MULTIPLICATION ||
                          op.getKind == EJSOp.BIN_ARITH_MUL_DIVISION ||
                          op.getKind == EJSOp.BIN_ARITH_MUL_REMINDER) {
                        context = symbolic_memory(v1.getUniqueName) + op.getText + res2 
                      } 
                      else
                        context = symbolic_memory(v1.getUniqueName) + op.getText + symbolic_memory(v2.getUniqueName)
                    }
                    else if (symbolic_memory.contains(v1.getUniqueName)) {
                      context = symbolic_memory(v1.getUniqueName) + op.getText + res2 
                    }
                    else if (symbolic_memory.contains(v2.getUniqueName)) {
                      context = symbolic_memory(v2.getUniqueName) + op.getText + res1 
                    }
                    val sid = symbol + index
                    symbolic_memory(id.getUniqueName) = sid
                    index += 1
                    val info = new Info(false, sid, Some(op.getText), context, None)
                    report = report:+info
                  }
                  // TODO: Decide between two methods: recording the changes of symbolic variables in untargeted functions as concrete values, or just ignoring.
              }
            }
          }
          case SIRUn(_, op, expr) =>
          case SIRLoad(_, obj, index) =>
          case v:IRId =>
            System.out.println("EXECUTE_ASSIGNMENT()")
            if (symbolic_memory.contains(v.getUniqueName)) {
              symbolic_memory(id.getUniqueName) = symbolic_memory(v.getUniqueName)
            }
          case _:IRThis =>
          case n:IRNumber =>
            System.out.println("EXECUTE_ASSIGNMENT()")
            symbolic_memory -= id.getUniqueName
          case s:IRString =>
          case b:IRBool =>
          case _:IRUndef =>
          case _:IRNull =>
        }
      }
    }
  }
  
  // when the function is targeted to test
  def getInput(id: IRId, env: IRId):Option[Int] = {
    System.out.println("GET_INPUT()")
    if (checkFocus(env)) {
      //TODO: find other ways to generate symbolic/input identifier
      symbolic_memory(id.getUniqueName) = symbol + index
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
      return Some(res)
    }
    return None
  }

  def walkVarStmt(id: IRId, env: IRId) = {
    System.out.println("WALK_VAR_STMT()")
    if (checkFocus(env)) {
      symbolic_memory(id.getUniqueName) = symbol + index
      index += 1
    }
  }

  def executeCondition(expr: IRExpr, branchTaken: Option[Boolean], c1: Option[String], c2: Option[String], env: IRId) = {
  //TODO: Don't need to be option type
  //TODO: need rewriter to modify the expressions syntatically accepted to the expressions supported by symbolic helper
    System.out.println("EXECUTE_CONDITION()")
    System.out.println(env.getUniqueName, function_info.contains(env.getUniqueName), checkFocus(env))
    if (checkFocus(env)) {
      expr match {
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
              //TODO: construct branch bitvector
              case _ =>
                if (symbolic_memory.contains(v1.getUniqueName) || symbolic_memory.contains(v2.getUniqueName)) {
                  //TODO: how to handle the case, concrete value is error
                  val res1 = c1 match {
                    case Some(c) => c
                    case None => ""
                  }
                  val res2 = c2 match {
                    case Some(c) => c
                    case None => ""
                  }
                  var context = ""
                  if (symbolic_memory.contains(v1.getUniqueName) && symbolic_memory.contains(v2.getUniqueName)) 
                    context = symbolic_memory(v1.getUniqueName) + op.getText + symbolic_memory(v2.getUniqueName)
                  
                  else if (symbolic_memory.contains(v1.getUniqueName)) 
                    context = symbolic_memory(v1.getUniqueName) + op.getText + res2
                  else if (symbolic_memory.contains(v2.getUniqueName)) 
                    context = symbolic_memory(v2.getUniqueName) + op.getText + res1 
                  val info = new Info(true, "", Some(op.getText), context, branchTaken)
                  report = report:+info
                }
            }
          }
        }
        case v:IRId =>
          if (symbolic_memory.contains(v.getUniqueName)) {
            val info = new Info(true, "", None, symbolic_memory(v.getUniqueName), branchTaken)
            report = report:+info
          }
      }
    }
    else  
      function_info(env.getUniqueName).is_candidate = true;
  }

  def addFunction(f: IRId) = {
    System.out.println("ADD_FUNCTION()")
    if (!function_info.contains(f.getUniqueName)) {
      function_info(f.getUniqueName) = new FunctionInfo()
    }
  }
  
  def checkFocus(f: IRId):Boolean = {
    System.out.println("CHECK_FOCUS()")
    if (function_info.contains(f.getUniqueName))
      return function_info(f.getUniqueName).is_target
    else
      return false;
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
    System.out.println("Symbolic memory = " +  symbolic_memory.toString)
    System.out.println("Symbolic report = " + report.map(_.expr))
    System.out.println("Input = " + input.toString)
  }
}
