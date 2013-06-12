/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import scala.collection.mutable.{HashMap => MHashMap}
import scala.collection.mutable.{Map=>MMap}
import java.io._
import java.util.{List => JList}
import edu.rice.cs.plt.tuple.{Option => JOption}
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.nodes_util.{NodeFactory => NF}
import kr.ac.kaist.jsaf.nodes_util.{NodeUtil => NU}
import kr.ac.kaist.jsaf.nodes_util.{WIDLToString => WS}
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.useful.Useful
import kr.ac.kaist.jsaf.useful.HasAt
import scala.util.parsing.json.JSON
import kr.ac.kaist.jsaf.scala_src.useful.ErrorLog
import kr.ac.kaist.jsaf.exceptions.StaticError

// libraries = ["webapis.tv.channel", ...]
class WIDLChecker(program:Program, libraries: JList[String]) extends Walker {
  val libs = toList(libraries).map(s => {
               val index = s.lastIndexOf(File.separatorChar)
               val file = s.substring(index+1, s.length)
               if (index > 0) (s, file.substring(0,file.lastIndexOf('.'))) else (s, s)
             })
  var dbs: MMap[String, List[WDefinition]] = MHashMap[String, List[WDefinition]]()
  var curDB = List[WDefinition]()

  def readDB(api: String) = dbs.get(api) match {
    case None =>
      val db = WIDLToDB.readDB(api)
      dbs.update(api, db); db
    case Some(db) => db
  }

  def dotToStr(dot: LHS): Option[String] = dot match {
    case SDot(_, d:Dot, SId(_, x, _, _)) => dotToStr(d) match {
      case Some(str) => Some(str+"."+x)
      case None => None
    }
    case SDot(_, SVarRef(_, SId(_, o, _, _)), SId(_, x, _, _)) => Some(o+"."+x)
    case SVarRef(_, SId(_, x, _, _)) => Some(x)
    case _ => None
  }

  def isAPI(dot: LHS) = dotToStr(dot) match {
    case Some(str) => libs.find(p => str.equals(p._2))
    case _ => None
  }

  def isVarAPI(dot: LHS) = dotToStr(dot) match {
    case Some(str) => vars.find(p => str.equals(p._1))
    case _ => None
  }

  private val sl = new SourceLocRats("WIDLChecker",0,0,0)
  private val span = new Span(sl,sl)
  def freshName(name: String) = "__WIDLChecker__" + name
  def mkId(name: String) = NF.makeId(span, name)
  def mkFreshId(name: String) = NF.makeId(span, freshName(name))
  
  def doit() = {
    walkUnit(program)
    if (!errorOccurred && !warningOccurred) println("OK")
  }


  /* global variables */
  var numOfTries = 0
  var vars = List[(String, Any)]() // varName, value
  var funs = List[(String, Int)]() // funName, #ofArgs
  var fappMethodRetType = ""
  var fappType = ""

  /* error-related things */
  var warningOccurred = false
  var errorOccurred = false
  val error_CF = "Cannot Found"
  val error_NP = "The Number of Parameters - Not Matched" 
  val error_CMCF = "Callback Method Cannot Found"
  val error_CMPNM = "Callback Method Parameter - Not Matched"
  val error_PTTE = "Parameter Type - TypeMismatch Error"
  val error_DMNSM = "Dictionary Member - No Such Member"
  val error_DMTNM = "Dictionary Member - Type Not Match"
  val error_MCF = "Member Cannot Found"
  val error_CARM = "Cannot Assign any values to Readonly Member"
  val warning_TC = "Use Try-Catch for"
  def printErrMsg(errType: String, name: String) = {
    if (!errorOccurred) println("ERROR : " + errType + " '" + name + "'")
    errorOccurred = true
  }
  def printWarningMsg(errType: String, name: String) = {
    warningOccurred = true
    println("WARNING : " + errType + " '" + name + "'")
  }


  override def walkUnit(node: Any): Unit = node match {
    case fa@STry(_, body, catchB, _) =>
      numOfTries += 1
      walkUnit(body)
      walkUnit(catchB)
      numOfTries -= 1

    case fa@SVarDecl(_, SId(_, x, _, _), expr) => expr match {
      case Some(p) =>
        walkUnit(p)
        p match {
          case fa@SFunApp(_, _, _) => vars ++= List((x, Some((fappMethodRetType, fappType))))
          case _ => vars ++= List((x, p))
        }
      case _ => None
    }

    case fa@SFunDecl(_, SFunctional(_, _, body, SId(_, x, _, _), params)) =>
      funs ++= List((x, params.length))

    case fa@SDot(_, obj, SId(_, x, _, _)) => isAPI(obj) match {
      case Some(p) =>
        curDB = readDB(p._1)
        val dotInfo = getInfo(curDB, x)
        /***** 1. check *****/
        if (dotInfo.length == 0) printErrMsg(error_CF, x)
      case _ => isVarAPI(obj) match {
        case Some((p, Some((v, m)))) =>
          val typ = v.toString.replace("e/", "")
          chkInterfaceMember(x, getInfo(curDB, typ))
        case _ => None
      }
    }
    
    case fa@SFunApp(info, fun, args) => fun match {
      case SDot(_, obj, SId(_, x, _, _)) => isAPI(obj) match {
        case Some(p) =>
          curDB = readDB(p._1)
          val argsInfo = getInfo(curDB, x)

          /***** 1. check *****/
          if (argsInfo.length == 0) printErrMsg(error_CF, x)

          /* separate return type and argument types,
             and count the number of optional argument. */
          var methodRetType = ""
          var argsWAPI = List[String]()
          var numOfOpt = 0
          argsInfo.foreach{ case(typ, which) =>
            if (which == "return") methodRetType = typ
            else if (which == "param") {
              if (typ.contains("o/")) numOfOpt += 1
              argsWAPI ++= List(typ)
            }
          }
          
          /***** 2. 1) check *****/
          if (args.length < argsWAPI.length-numOfOpt || args.length > argsWAPI.length)
            printErrMsg(error_NP, x)

          /* analyze arguments of the method in javascript code. */
          args.zip(argsWAPI).foreach{ case(arg, typ) =>
            /***** 2. 2) E) check *****/
            if (!chkArg(arg, typ, getInfo(curDB, typ)))
              printErrMsg(error_PTTE, x)
          }
          
          /***** 3. check *****/
          if (methodRetType.contains("e/") && numOfTries == 0)
            printWarningMsg(warning_TC, x)

          fappMethodRetType = methodRetType
          fappType = x

        case _ => None
      }
      case _ => None
    }

    case _ => super.walkUnit(node)
  }


  /* Analyze type and detail content of an argument in a funapp. */
  def chkArg(node: Any, typ: String, info: List[(String, String)]): Boolean = node match {
    case fa@SObjectExpr(_, elmts) =>
      toList(elmts).foreach{ e => 
        if (!chkDictMem(e, info)) return false
      }
      true
    case fa@SVarRef(_, SId(_, y, _, _)) => 
      vars.foreach{ case(name, v) => 
        if (name == y) {
          v match {
            case Some((retTyp, apiTyp)) => 
              if (typ == retTyp.toString.replace("e/", "")) return true
            case _ => return chkArg(v, typ, info)
          }
        }
      }
      if (typ.contains("Callback")) return isCallback(y, typ)
      false
    case fa@SDot(_, obj, SId(_, y, _, _)) => isAPI(obj) match {
      case Some(p) => 
        val dotInfo = getInfo(readDB(p._1), y)
        /***** 1. check *****/
        if (dotInfo.length == 0) printErrMsg(error_CF, y)
        chkDot(dotInfo, typ)
      case _ => isVarAPI(obj) match {
        case Some((p, Some((v, m)))) =>
          val typ = v.toString.replace("e/", "")
          val varInfo = getInfo(curDB, typ)
          if (chkInterfaceMember(y, varInfo)) {
            varInfo.foreach{ case(n, t) =>
              if (n == y) {
                if (typ.contains("short") || typ.contains("int") || typ.contains("long"))
                  return chkLiteral(t, List("long", "short", "int"))
                else if (typ.contains("String"))
                  return chkLiteral(t, List("String"))
                else if (typ.contains("double") || typ.contains("float"))
                  return chkLiteral(t, List("float", "double"))
                else
                  return (typ.replace("o/", "") == t)
              }
            }
          }
          false
        case _ => false
      }
    }
    case fa@SIntLiteral(_, y, _) => chkLiteral(typ, List("long", "short", "int"))
    case fa@SStringLiteral(_, _, y) => chkLiteral(typ, List("String"))
    case fa@SDoubleLiteral(_, _, y) => chkLiteral(typ, List("float", "double"))
    case _ => false
  }

  /* Check if member 'name' is in the Interface. */
  def chkInterfaceMember(name: String, check: List[(String, String)]): Boolean = {
    check.foreach{ case(n, t) =>
      if (name == n) return true
    }
    /***** 4. check *****/
    printErrMsg(error_MCF, name)
    false
  }

  /* Check if the types of dot values are same. */
  def chkDot(dotInfo: List[(String, String)], typ: String): Boolean = {
    val dotTyp = dotInfo.last._1.replace("o/", "")
    if (typ.replace("o/", "") == dotTyp) return true
    else false
  }

  /* Check if the types of literal values are same. */
  def chkLiteral(typ: String, check: List[String]): Boolean = {
    var isSameType = false
    check.foreach{ c =>
      if (typ.contains(c)) isSameType = true
    }
    isSameType
  }

  /* Analyze each dictionary member. */
  def chkDictMem(node: Any, info: List[(String, String)]): Boolean = node match {
    case fa@SField(_, SPropId(_, SId(_, x, _, _)), expr) => expr match {
      case fa@SIntLiteral(_, v, _) => chkDictMemLiteral(x, info, List("long", "short", "int"))
      case fa@SStringLiteral(_, _, v) => chkDictMemLiteral(x, info, List("String"))
      case fa@SDoubleLiteral(_, _, v) => chkDictMemLiteral(x, info, List("float", "double"))
      case _ => true
    }
    case _ => true // chkDictMem(node, info)
  }

  /* Analyze each literal of a dictionary member. */
  def chkDictMemLiteral(x: String, info: List[(String, String)], check: List[String]): Boolean = {
    var isExistent = false
    var isSameType = false
    info.foreach{ case(name, typ) =>
      if (x == name) {
        isExistent = true
        check.foreach{ id => if (typ.contains(id)) isSameType = true }
      }
    }
    /***** 2. 2) A) check *****/
    if (!isExistent) printErrMsg(error_DMNSM, x)
    /***** 2. 2) B) check *****/
    if (!isSameType) printErrMsg(error_DMTNM, x)
    
    if (isExistent && isSameType) true
    else false
  }


  /* If an arg should be a callback method,
     and the arg in JS code seems to be a function,
     then check if it's for callback. */
  def isCallback(name: String, cbTyp: String): Boolean = {
    var isExistent = false
    var numOfArgs = -1
    funs.foreach{ case(m, v) =>
      if (m == name) {
        isExistent = true
        numOfArgs = v
      }
    }
    /***** 2. 2) C) check *****/
    if (!isExistent) {
      printErrMsg(error_CMCF, name)
      return false
    }
 
    var isNumOfArgsRight = false
    val info = getInfo(curDB, cbTyp)
    info.foreach{ case(m, v) =>
      if (numOfArgs == v.toInt) isNumOfArgsRight = true
    }
    /***** 2. 2) D) check *****/
    if (!isNumOfArgsRight) {
      printErrMsg(error_CMPNM, name)
      return false
    }
    true
  }



  /******* DB-related defs *******/
  
  def getInfo(db: List[Any], x: String): List[(String, String)] = {
    db.foreach{ c =>
      val argsInfo = getInfoInDB(c, x)
      if (argsInfo.length > 0) return argsInfo
    }
    return Nil
  }

   def getInfoInDB(content: Any, name: String): List[(String, String)] = content match {
     case c:WModule =>
       var res = List[(String,String)]()
       val defs = toList(c.getDefs())
       defs.foreach{ d => res ++= getInfoInDB(d, name) }
       res
     case i:WInterface =>
       var res = List[(String,String)]()
       val members = toList(i.getMembers())
       members.foreach{ member => member match {
         case im:WInterfaceMember => im match {
           case o:WOperation =>
             val optNames = o.getName()
             var raiseException = ""
             toList(o.getExns()).foreach{ a => 
               toList(a.getName()).foreach{ exn =>
                 if (exn == "WebAPIException") raiseException = "e/"
               }
             }
             optNames.foreach{ optName =>
               if (i.getName() == name) {
                 res ++= List(( optName, toList(o.getArgs()).length.toString ))
               }
               else if (optName == name) {
                 var types = List[String]()
                 var typecases = List[String]()
                 types ++= List(raiseException + typeName(o.getTyp()))
                 typecases ++= List("return")
                 toList(o.getArgs()).foreach{ a =>
                   var optional = ""
                   toList(a.getAttributes()).foreach{ b => b match {
                     case opt:WEAOptional => optional = "o/"
                   }}
                   types ++= List(optional + typeName(a.getTyp()))
                   typecases ++= List("param")
                 }
                 res ++= types.zip(typecases)
               }
             }
           case c:WConst =>
             if (i.getName() == name)
               res ++= List(( c.getName(), typeName(c.getTyp()) ))
             else if (c.getName() == name)
               res ++= List(( typeName(c.getTyp()), "const" ))
              // res ++= List( (defValue(c.getValue()), "const") )
           case a:WAttribute =>
             var attr = ""
             toList(a.getAttrs()).foreach{ at => at match {
               case att:WEAReadonly => attr = "r/"
             }}
             if (i.getName() == name)
               res ++= List(( a.getName(), typeName(a.getTyp()) ))
             else if (a.getName() == name)
               res ++= List( (attr+typeName(a.getTyp()), a.getName()) )
           case _ => None//res ++= List[(String, String)]()
         }
       }}
       res
     case c:WDictionary =>
       var res = List[(String, String)]()
       if (c.getName() == name) {
         var names = List[String]()
         var types = List[String]()
         toList(c.getMembers()).foreach{ a =>
           names ++= List(a.getName())
           types ++= List(typeName(a.getTyp()))
         }
         res ++= names.zip(types)
       }
       res
     case _ => List[(String, String)]()
   }

   def typeName(typ: WType): String = typ match {
     case t:WNamedType => t.getName()
     case _ => ""
   }
   
   def defValue(value: WLiteral): String = value match {
     case v:WInteger => v.getValue()
     case _ => ""
   }
   
   def AnyToStr(any: Any): String = any match {
     case s:String => s
     case _ => ""
   }
}



