/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.dtv

import java.io._
import java.util.{List => JList}
import edu.rice.cs.plt.tuple.{Option => JOption}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF}
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.useful.Useful
import kr.ac.kaist.jsaf.useful.HasAt
import scala.util.parsing.json.JSON
import kr.ac.kaist.jsaf.scala_src.useful.ErrorLog
import kr.ac.kaist.jsaf.exceptions.StaticError
import kr.ac.kaist.jsaf.nodes_util.SourceLocRats
import kr.ac.kaist.jsaf.nodes_util.Span

class APIModeling extends Walker {
  val libraries:List[String] = List("$", "jQuery");
  val errors: ErrorLog = new ErrorLog
  def signal(msg:String, hasAt:HasAt) = errors.signal(msg, hasAt)
  def signal(hasAt:HasAt, msg:String) = errors.signal(msg, hasAt)
  def signal(error: StaticError) = errors.signal(error)

  private val sl = new SourceLocRats("<>Modeled API",0,0,0)
  private val span = new Span(sl,sl)
  def freshName(name: String) = "__API__" + name
  def mkId(name: String) = NF.makeId(span, name)
  def mkFreshId(name: String) = NF.makeId(span, freshName(name))
  
  def removeDuplicate(fields:List[Field]) : List[Field] = {
    // remove duplicate names, if there are various arity for same name of function, choose largest arity
    def findMaximumArityFunction(fname: String): Field = {
      var ret: Field = null
      var max: Int = -1
      fields.foreach((x: Field) => 
        if ((x.getProp.asInstanceOf[PropId]).getId.getText == fname){
          var tmp: Int = ((x.getExpr.asInstanceOf[FunExpr]).getFtn.getParams).size;
          if (max < tmp){
            max = tmp;
            ret = x;
          }
        })
      ret
    }
    (fields.map(x => findMaximumArityFunction((x.getProp.asInstanceOf[PropId]).getId.getText))).distinct
  }
  var api = "" // current api name
  var result : List[Field] = List[Field]() // fields in the made api function
  var fieldStub: List[Pair[LHS, String]] = List() // string is for checking existence of elements in stub
  var ftnStub: List[Triple[LHS, String, Int]] = List()
  def doit(program:Program) = {
    //System.out.println("before modeling...")
    val tempVariable = mkFreshId("ret")
    val sf: List[SourceElement] = { // only used in dtv mode
      api = "sf"
      result = List[Field]()
      fieldStub = List()
      ftnStub = List()
      val already = List[Pair[LHS, String]]((null, "sf.key"), (null, "sf.scene.show"), (null, "sf.scene.hide"),
        (null, "sf.scene.blur"), (null, "sf.scene.focus")) ++
        keys.map(str => (null, "sf.key." + str)) // modeled fields in DTVRewriter
      walkUnit(program)
      // returns empty stubs without modeled fields

      fieldStub.foldLeft[List[SourceElement]](List())((lst: List[SourceElement], s: Pair[LHS, String]) => already.contains((null, s._2)) match {
        case true => lst
        case false => ftnStub.find(fs => fs._2 == s._2) match {
          case Some(f) => {
            val maxarg: Int = ftnStub.foldLeft[Int](0)((max: Int, tmp: Triple[LHS, String, Int]) => if (max < tmp._3) tmp._3 else max)
            lst :+ NF.makeExprStmt(span, NF.makeAssignOpApp(span, s._1, NF.makeOp(span, "="),
               NF.makeParenthesized(span,
                 NF.makeFunExpr(span, mkId(""),
                   (0 until maxarg).map(n => mkId("arg%d".format(n))),
                   List(NF.makeReturn(span, toJavaOption(Some(NF.makeVarRef(span, mkId("__TOP")))))))
            )))
          }
          case None => lst :+ NF.makeExprStmt(span, NF.makeAssignOpApp(span, s._1, NF.makeOp(span, "="), NF.makeObjectExpr(span, List())))
        }
      })
    }
    val modeled = libraries.foldLeft[List[SourceElement]](List())((lst: List[SourceElement], currentAPI: String) => {
      api = currentAPI
      result = List[Field]()
      fieldStub = List()
      ftnStub = List()
      walkUnit(program)
      val tmp1: SourceElement = NF.makeFunDecl(span, mkId(api), List[Id](),
         toJavaList(List(NF.makeVarStmt(span,
           toJavaList(List(NF.makeVarDecl(span, tempVariable,
             toJavaOption(some(NF.makeObjectExpr(span,
               toJavaList(removeDuplicate(result))))))))),
         NF.makeReturn(span, toJavaOption(some(NF.makeVarRef(span, tempVariable)))))))
      val tmp2: List[SourceElement] = fieldStub.map(m =>
        ftnStub.find(fs => fs._2 == m._2) match {
          case Some(f) => {
            val maxarg: Int = ftnStub.foldLeft[Int](0)((max: Int, tmp: Triple[LHS, String, Int]) => if (max < tmp._3) tmp._3 else max)
            NF.makeExprStmt(span, NF.makeAssignOpApp(span, m._1, NF.makeOp(span, "="),
              NF.makeParenthesized(span,
                NF.makeFunExpr(span, mkId(""),
                  (0 until maxarg).map(n => mkId("arg%d".format(n))),
                  List(NF.makeReturn(span, toJavaOption(Some(NF.makeVarRef(span, mkId("__TOP")))))))
              )))
          }
          case None => NF.makeExprStmt(span, NF.makeAssignOpApp(span, m._1, NF.makeOp(span, "="), NF.makeObjectExpr(span, List())))
        })
      (lst :+ tmp1) ++ tmp2
    })
    program match {
      case SProgram(info, STopLevel(fds, vds, body), comments) =>
        SProgram(info, STopLevel(fds, vds, sf++modeled++body), comments)
    }
  }

  /*
   * if the node is form of [api].field1.field2...,
   *   then add [api].field1 = { }, [api].field1.field2 = { }, ... to stub
   *   (for avoiding type error due to reference absent properties of api)
   * if node is form of our target, it add an element to stub (if not exist)
   *   and returns the added element and string to check,
   * otherwise, it returns (null, "")
   */
  def fillStub(node: Any) : Pair[LHS, String] = node match {
    case vr@SVarRef(_, SId(_, text, _, _)) if text == api => (vr, text)
    case SDot(_, lhs, SId(_, text, _, _)) => {
      val rec = fillStub(lhs)
      if (rec != (null, "")){ // If we should make the stub
        fieldStub.find(elmt => (elmt._2 == (rec._2 + "." + text))) match {
          case Some(elmt) => elmt
          case None => {
            val ret = (NF.makeDot(span, rec._1, mkId(text)), rec._2 + "." + text)
            fieldStub :+= ret
            ret
          }
        }
      } else {
        (null, "")
      }
    }
    case _ => {
      (null, "")
    }
  }

  // here, we can reduce time complexity with refactoring
  // we simply ignore shadowing here
  override def walkUnit(node: Any): Unit = node match {
     case fa@SFunApp(info, fun, args) => {
       args.foreach(walkUnit(_))
       walkUnit(fun)
       fillStub(fun) match {
         case (null, "") =>
         case (lhs, str) => ftnStub :+= (lhs, str, args.size)
       }
       fun match{
         case SDot(_, SFunApp(_, SVarRef(_, SId(_, x, _, _)), args2), member) if x == api => member match{
           case id@SId(_, text, _, _) =>
             result ++=
             List(NF.makeField(span, NF.makePropId(span, mkId(text)),
               NF.makeFunExpr(span, mkId(""), (0 until args.length).map(x => mkId("arg%d".format(x))),
                                 toJavaList(List[SourceElement](NF.makeReturn(span, toJavaOption(some(NF.makeVarRef(span, mkId("__TOP"))))))))))
           case _ =>
         }
         case _ =>
       }
     }
     case d@SDot(_, lhs, member) => {
       walkUnit(lhs)
       fillStub(d)
     }
     case _ => super.walkUnit(node)
   }
}
