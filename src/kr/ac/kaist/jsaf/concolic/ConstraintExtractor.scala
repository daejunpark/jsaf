/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic

import _root_.java.util.BitSet
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

import scala.collection.mutable.Queue

class ConstraintExtractor(I: Interpreter) {
  abstract class SymbolicTree 
  case class Node(v: Boolean, e: Option[Tuple2[String, String]], pc: Option[ConstraintForm], vector: BitSet) extends SymbolicTree
    {
      var visit = v
      var expr = e
      var PC = pc
      var bitvector = vector
    }
  case class Statement(node: Node, children: SymbolicTree) extends SymbolicTree
  case class Condition(node: Node, left: SymbolicTree, right: SymbolicTree) extends SymbolicTree
  
  // store where node locates in the symbolic execution tree 
  var bitset = new BitSet()
  bitset.set(0)
  var tree:SymbolicTree = new Node(true, None, None, bitset )
  var unvisited = new Queue[Node]
  // indicate expanded node in the symbolic execution tree
  var expanded = new BitSet()
  expanded.set(0)
  var constraint = List[ConstraintForm]()
  
  def extract(report: List[Info]) = {
    constraint = List[ConstraintForm]()
    /* Construct the symbolic execution tree */
    var subtree:SymbolicTree = Node(true, None, None, expanded)
    var r:List[Info] = report.drop(expanded.length-1)
    // FOR DUBUGGING
    System.out.println("Report: " + report.map(_.expr))
    System.out.println("Affected Report: " + r.map(_.expr))
    System.out.println("Expanded Node: " + expanded)
    for (info <- r) 
      subtree = insert(subtree, info)
    tree = combine(tree, expanded.length-1, subtree)
    
    if (unvisited.isEmpty) 
      System.out.println("DONE")
    else {
      var target = unvisited.dequeue
      expanded = target.bitvector
      collect(tree, target.bitvector, target.bitvector.length-2)
    }
  }

  def combine(origin: SymbolicTree, index: Int, additional: SymbolicTree):SymbolicTree = {
    if (index > 0) {
      origin match {
        case Condition(node, left, right) =>
            if (expanded.get(index-1))
              Condition(node, combine(left, index-1, additional), right)
            else
              Condition(node, left, combine(right, index-1, additional))
        case Statement(node, child) => 
            Statement(node, combine(child, index-1, additional))
        case Node(v, e, pc, vector) =>
            System.out.println("WRONG COMBINATION 1")
            Node(v, e, pc, vector)
      }
    }
    else {
      origin match {
        case Condition(node, left, right) =>
            System.out.println("WRONG COMBINATION 2")
            Condition(node, left, right)
        case Statement(node, child) =>  
            System.out.println("WRONG COMBINATION 3")
            Statement(node, child)
        case Node(v, e, pc, vector) =>
            additional match {
              case Condition(node, left, right) => Condition(Node(!v, e, pc, vector), left, right)
              case Statement(node, child) => Statement(Node(!v, e, pc, vector), child)
              case Node(v2, e2, pc2, vector2) =>  Node(!v, e, pc, vector)
            }
      }
    }  
  }

  def insert(t: SymbolicTree, info: Info):SymbolicTree = t match {
    case Node(v, e, pc, vector) => 
      if (info.cond) 
      {
        var b1 = bitShift(vector)
        b1.set(0, info.branchTaken)
        var n1 = Node(true, None, negate(info.branchTaken, info), b1)
        
        var b2 = bitShift(vector)
        b2.set(0, !info.branchTaken)
        var n2 = Node(false, None, negate(!info.branchTaken, info), b2)
        
        unvisited += n2
        // left for true branch and right for false branch
        if (info.branchTaken)
          Condition(Node(v, e, pc, vector), n1, n2)
        else
          Condition(Node(v, e, pc, vector), n2, n1)
        }
      else 
      {
        var b = bitShift(vector)
        b.set(0)
        var cond = new ConstraintForm()
        var rhs = new ConstraintForm()
        rhs = parsing(info.expr._2)
        cond.makeConstraint(info.expr._1, "=", rhs)
        Statement(Node(v, e, pc, vector), Node(true, Some(info.expr), Some(cond), b))
      }
    case Statement(node, child) => Statement(node, insert(child, info))
    case Condition(node, left, right) =>
      if(isVisit(left))
        Condition(node, insert(left, info), right)
      else 
        Condition(node, left, insert(right, info))
  }

  def collect(t: SymbolicTree, bitset: BitSet, index: Int):List[ConstraintForm] = t match {
    //TODO: combine disffused symbolic expression to one single constraint 
    //according to the supporting form of yices solver.
    case Node(v, e, pc, vector) => 
      if (!bitset.equals(vector)) 
        System.out.println(bitset.toString + ", " + vector.toString)
      pc match { case Some(c) => constraint = constraint:+c
                 case None => }
      return constraint
    case Statement(node, child) => 
      if (!bitset.get(index))
        System.out.println(index.toString)
      node.PC match { case Some(c) => constraint = constraint:+ c
                      case None => }
      collect(child, bitset, index-1)
    case Condition(node, left, right) =>
      node.PC match { case Some(c) => constraint = constraint:+ c
                      case None => }
      if (bitset.get(index))
        collect(left, bitset, index-1)
      else
        collect(right, bitset, index-1)
  }

  /* Helper functions */
  def bitShift(v: BitSet):BitSet = {
    var res = new BitSet()
    var i = 0
    for (i <- 0 to v.length)
      res.set(i+1, v.get(i))
    return res
  }

  def isVisit(t: SymbolicTree):Boolean = t match {
    case Node(v, e, pc, vector) => v
    case Statement(node, child) => node.visit
    case Condition(node, right, left) =>  node.visit
  }
    
  def negate(trueB: Boolean, info: Info):Option[ConstraintForm] = info.op match {
    case Some(c) => 
      val values = info.expr._2.split(c)
    
      var cond = new ConstraintForm()
      cond.lhs = values(0)
      var rhs = new ConstraintForm()
      rhs.makeConstraint(values(1))
      cond.rhs = Some(rhs)
      if (!trueB) {
        c match {
          /* infix */
          case "<" => cond.op = Some(">=")
          case "<=" => cond.op = Some(">")
          case ">" => cond.op = Some("<=")
          case ">=" => cond.op = Some("<")
          case "==" => cond.op = Some("!=")
          case "!=" => cond.op = Some("==")
        }
      }
      else 
        cond.op = info.op
      return Some(cond)
    case None =>  None
  }
  
  def height(t: SymbolicTree):Int = t match {
    case Node(v, e, pc, vector) => 0
    case Statement(node, child) => 1 + height(child)
    case Condition(node, left, right) =>
      if(isVisit(left)) 
        1 + height(left)
      else 
        1 + height(right)
  }

  def parsing(expr: String):ConstraintForm = {
    var res = new ConstraintForm()
    var operations = Array('+', '-', '*', '/', '%')
    var isop = false
    for (op <- operations) {
      if (expr.contains(op)) {
        isop = true
        var parse = expr.split(op)    
        var rhs = new ConstraintForm()
        rhs.makeConstraint(parse(1))
        res.makeConstraint(parse(0), op.toString, rhs)
      }
    }
    if (!isop)
      res.makeConstraint(expr)
    return res
  }

  def toString(t: SymbolicTree):String = t match {
    case Node(v, e, pc, vector) =>
      val expr = e match { case Some(c) => c
                           case None => ""}
      val cond = pc match { case Some(c) => c.toString
                            case None => "root" }
      "(" + v.toString + "/ " + expr + "/ " + cond + "/ " + vector.toString + ") "
    case Statement(node, child) => 
      toString(node) + "(" + toString(child) + ") "
    case Condition(node, left, right) =>
      toString(node) + "(" + toString(left) + "," + toString(right) + ") "
  }

  def print() = {
    System.out.println("Symbolic execution tree: " + toString(tree))
    System.out.println("Selected constraint: ")
    for (elem <- constraint)
        System.out.println(elem.toString)
  }
}
      
        
  
