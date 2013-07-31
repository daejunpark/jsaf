/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
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

import _root_.java.math.BigInteger

class DTVRewriter(dtv: DTV) extends Walker {
  val sceneNames = dtv.getSceneNames
  val errors: ErrorLog = new ErrorLog
  def signal(msg:String, hasAt:HasAt) = errors.signal(msg, hasAt)
  def signal(hasAt:HasAt, msg:String) = errors.signal(msg, hasAt)
  def signal(error: StaticError) = errors.signal(error)

  /* Global names *****************************************/
  /*
   * span info (reuse)
   * line: Scene number
   * col: Function number (1: show, 2: focus, 3: blur, 4: hide, 5: keyDown)
   *
   */

  def mkSpan(sn: Int, fn: Int) = { //sn: Scene number, fn: Functuin number
    val sl = new SourceLocRats("<>DTV Apps Framework", sn, fn, 0)
    new Span(sl, sl)
  }
  def mkId(span:Span, name: String) = NF.makeId(span, name)
  def mkRef(span:Span, scene: String, name: String) = // scene[scene].[name]
    NF.makeDot(span, NF.makeBracket(span, NF.makeVarRef(span, sceneId), NF.makeStringLiteral(span, scene, "\"")), mkId(span, name))

  def mkHeader(): List[SourceElement] = {
    // key code
    val keyElems = (0 until keys.size).map(n => NF.makeField(span, NF.makePropId(span, mkId(span, keys(n))), NF.makeIntLiteral(span, BigInteger.valueOf(n))))
    val keyObj = NF.makeObjectExpr(span, toJavaList(List(NF.makeField(span, NF.makePropId(span, mkId(span, "key")),
      NF.makeObjectExpr(span, toJavaList(keyElems))))))
    val key = NF.makeVarDecl(span, mkId(span, "sf"), toJavaOption(some(keyObj)))

    List(NF.makeVarStmt(span, toJavaList(List(key))))
  }
  def mkFooter(): List[SourceElement] = {
    //TODO: There are implicit conversion from scala list to java list (How to make empty java list?)

    // id declaration
    val blurId = mkFreshId("blur");

    // load scenes
    val sceneElems = (0 until sceneNames.size).map(n =>{
        val sName = sceneNames(n)
        val span = mkSpan(n, 0)
        NF.makeField(span, NF.makePropStr(span, sName),
        NF.makeNew(span, NF.makeFunApp(span, NF.makeVarRef(span, mkId(span, "Scene"+sceneNames(n))), List[Expr]())))})
    val sceneObj = NF.makeObjectExpr(span, toJavaList(sceneElems))
    val scene = NF.makeVarDecl(span, sceneId, toJavaOption(some(sceneObj)))

    // blur
    def enumerate(n: Int): List[Int]= n match {
      case m if (m < 0) => List[Int]()
      case 0 => List[Int](0)
      case m if (m > 0) => enumerate(m-1) ++ List[Int](m)
    }
    val blurElems = NF.makeField(span, NF.makePropStr(span, ""),
      NF.makeFunExpr(span, mkId(span, ""), List[Id](), List[SourceElement]())) :: enumerate(sceneNames.size - 1).map(n => {
        val sName = sceneNames(n)
        val span = mkSpan(n, 3)
        NF.makeField(span, NF.makePropStr(span, sName), mkRef(span, sName, "handleBlur"))})
    val blurObj = NF.makeObjectExpr(span, toJavaList(blurElems))
    val blur = NF.makeVarDecl(span, blurId, toJavaOption(some(blurObj)))

    // focused
    val focused = NF.makeVarDecl(span, focusedId, toJavaOption(some(NF.makeStringLiteral(span, "", "\""))));

    // focus
    val focusElems = (0 until sceneNames.size).map(n => {
        val sName = sceneNames(n)
        val span = mkSpan(n, 2)
        NF.makeField(span, NF.makePropStr(span, sName),
          NF.makeFunExpr(span, mkId(span, ""), List[Id](), 
           toJavaList(List(NF.makeExprStmt(span, 
                             NF.makeFunApp(span, 
                                 NF.makeBracket(span,
                                          NF.makeVarRef(span, blurId),
                                          NF.makeVarRef(span, focusedId)),
                                 List[Expr]())),
                   NF.makeExprStmt(span, NF.makeAssignOpApp(span, NF.makeVarRef(span, focusedId),
                                      NF.makeOp(span, "="),
                                      NF.makeStringLiteral(span, sName, "\""))),
                   NF.makeExprStmt(span, NF.makeFunApp(span, mkRef(span, sName, "handleFocus"),
                                       List[Expr]())))
                                   )))})
    val focusObj = NF.makeObjectExpr(span, toJavaList(focusElems))
    val focus = NF.makeVarDecl(span, focusId, toJavaOption(some(focusObj)))


    // show
    val showElems = (0 until sceneNames.size).map(n => {
        val sName = sceneNames(n)
        val span = mkSpan(n, 1)
        NF.makeField(span, NF.makePropStr(span, sName),
                            NF.makeFunExpr(span, mkId(span, ""), List[Id](), 
                                     toJavaList(List(NF.makeExprStmt(span, NF.makeFunApp(span, mkRef(span, sName, "initialize"),
                                                               List[Expr]())),
                                             NF.makeExprStmt(span, NF.makeFunApp(span, mkRef(span, sName, "handleShow"),
                                                                 List[Expr]()))))))})
    val showObj = NF.makeObjectExpr(span, toJavaList(showElems))
    val show = NF.makeVarDecl(span, showId, toJavaOption(some(showObj)))

    // hide
    val hideElems = (0 until sceneNames.size).map(n => {
        val sName = sceneNames(n)
        val span = mkSpan(n, 4)
        NF.makeField(span, NF.makePropStr(span, sName), mkRef(span, sName, "handleHide"))})
    val hideObj = NF.makeObjectExpr(span, toJavaList(hideElems))
    val hide = NF.makeVarDecl(span, hideId, toJavaOption(some(hideObj)))

    // keyDown
    val keyDownElems = (0 until sceneNames.size).map(n => {
        val sName = sceneNames(n)
        val span = mkSpan(n, 5)
        NF.makeField(span, NF.makePropStr(span, sName), mkRef(span, sName, "handleKeyDown"))})
    val keyDownObj = NF.makeObjectExpr(span, toJavaList(keyDownElems))
    val keyDown = NF.makeVarDecl(span, keyDownId, toJavaOption(some(keyDownObj)))

    /*
    // functions in init.js
    val onStart = NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeVarRef(span, mkId(span, "onStart")), List[Expr]()))
    val onDestroy = NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeVarRef(span, mkId(span, "onDestroy")), List[Expr]()))

    List(NF.makeVarStmt(span, toJavaList(List(scene, keyDown, hide, show, blur, focused, focus, key)))) ++ List(onStart, onDestroy)
    */
    List(NF.makeVarStmt(span, toJavaList(List(scene, keyDown, hide, show, blur, focused, focus))))
  }

  def tempEventHandler(): List[SourceElement] = { //temporal strategy for handling keyDown event (with while)
    val onStart = NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeVarRef(span, mkId(span, "onStart")), List[Expr]()))
    val onDestroy = NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeVarRef(span, mkId(span, "onDestroy")), List[Expr]()))
    List[SourceElement](onStart) ++ sceneNames.map(sc => {
        NF.makeExprStmt(span, NF.makeFunApp(span, NF.makeBracket(span, NF.makeVarRef(span, keyDownId), NF.makeVarRef(span, focusedId)),
          List[Expr](NF.makeVarRef(span, mkId(span, "__TOP")))))
      }) ++ List[SourceElement](onDestroy)
  }
  /*
  def tempEventHandler(): List[SourceElement] = {
    val scb: SceneScenarioBuilder = new SceneScenarioBuilder(Set("page_0")) //TODO: traverse from onStart
    scb.doit
    scb.scenario
  }
  */

  /*
  def doit() = NU.simplifyWalker.walk(walk(program).asInstanceOf[Program])
  */
  def doit(program:Program) = {
    //System.out.println("before rewriting...")
    walk(program).asInstanceOf[Program]
  }

  def addGlobal(program: Program): Program = program match {
    case SProgram(info, STopLevel(fds, vds, body), comments) =>
      SProgram(info, STopLevel(fds, vds, mkHeader++body++mkFooter), comments)
  }

  def addKeyDownHandler(program: Program): Program = program match {
    case SProgram(info, STopLevel(fds, vds, body), comments) =>
      SProgram(info, STopLevel(fds, vds, body++tempEventHandler), comments)
  }

  override def walk(node: Any): Any = node match {
    case fa@SFunApp(info, fun, args) => //TODO: rewriting core!!!
      val mapArgsWalk = args.map(e => walk(e).asInstanceOf[Expr])
      val funAppWalk = SFunApp(info, walk(fun).asInstanceOf[LHS], mapArgsWalk)
      var scene:Expr = NF.makeNull(span) // arbitrary value
      var argList:List[Expr] = List[Expr]()
      var flag:Boolean = true
      mapArgsWalk match{
        case head::Nil  =>
          scene = head
          argList = List[Expr]()
        case head::tail =>
          scene = head
          argList = tail
        case _ =>
          flag = false
      }
      fun match{
        case SDot(_, SDot(_, SVarRef(_, SId(_, "sf", _, _)), SId(_, "scene", _, _)), member1) => member1 match{
          case id@SId(_, text2, _, _) => 
            if (!flag && (text2=="hide" || text2=="focus" || text2=="show")){
              //TODO : print warning or error message
              signal(text2 + " should take a scene name as an argument", id)
              funAppWalk
            }else if (text2 == "hide"){
              SFunApp(info, SBracket(info, SVarRef(info, hideId), scene), argList)
            }else if (text2 == "focus"){
              SFunApp(info, SBracket(info, SVarRef(info, focusId), scene), argList)
            }else if (text2 == "show"){
              SFunApp(info, SBracket(info, SVarRef(info, showId), scene), argList)
            }else{
              funAppWalk
            }
         case _ => funAppWalk
        }
        case _ => funAppWalk
      }
    case xs:List[_] => xs.map(x => walk(x))
    case xs:Option[_] => xs.map(x => walk(x))
    case _ => super.walk(node)
  }
}
