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
    case Some(str) => 
      var res = libs.find(p => str.equals(p._2))
      if (res != None) res
      else {
        varMethodType.foreach{ case(x, y, z) =>
          if (str == x) res = Some((x, z))
        }
        res
      }
    case _ => None
  }

  private val sl = new SourceLocRats("WIDLChecker",0,0,0)
  private val span = new Span(sl,sl)
  def freshName(name: String) = "__WIDLChecker__" + name
  def mkId(name: String) = NF.makeId(span, name)
  def mkFreshId(name: String) = NF.makeId(span, freshName(name))
  
  def doit() = {
    walkUnit(program)
    if (!errorOccurred) println("OK")
  }


  var methodReturnType = ""
  var paramTypes = List[String]()
  var functions = List[String]()
  var inputParams = List[Any]()
  var temp = List[Any]()
  var inputDict = List[(String, Any, String)]()
  var curReferVar = ""
  var varMethodType = List[(String, String, String)]()

  var errorOccurred = false
  val error_CF = "Cannot Found"
  val error_NP = "The Number of Parameters - Not Matched" 
  val error_CMCF = "Callback Method Cannot Found"
  val error_PTTE = "Parameter Type - TypeMismatchError"
  val error_DMNSM = "Dictionary Member - No Such Member"
  val error_DMTNM = "Dictionary Member - Type Not Match"
  val error_MCF = "Member Cannot Found"
  val error_CARM = "Cannot Assign any values to Readonly Member"
  def printErrMsg(errType: String, name: String) = {
    errorOccurred = true
    println("ERROR : " + errType + " '" + name + "'")
  }

  def isVarForAPI(s: String): Boolean = {
    varMethodType.foreach{ case(x, y, z) =>
      if (x == s) return true
    }
    return false
  }
  def getTypeOfVar(s: String): String = {
    varMethodType.foreach{ case(x, y, z) =>
      if (x == s) return z
    }
    return ""
  }

  def getVarName(node: Any): String = node match {
    case fa@SDot(_, obj, SId(_, x, _, _)) => getVarName(obj)+"."+x
    case fa@SVarRef(info, SId(_, x, _, _)) => x+"//"
  } 

  override def walkUnit(node: Any): Unit = node match {
    case fa@SAssignOpApp(_, lhs, SOp(_, op), r) =>
      if (op == "=") {
        var vtype = ""
        r match {
          case fa@SIntLiteral(info, intval, radix) => vtype = "int"
          case fa@SStringLiteral(info, quote, str) => vtype = "string"
          case fa@SDoubleLiteral(info, str, doubleval) => vtype = "double"
        }
        getVarName(lhs).split("//.") match {
          case Array(v, attr) =>
            curDB.foreach{ content =>
              val info = getInfoInDB(content, getTypeOfVar(v))
              var isAttrInAPI = false
              info.foreach{ case(attrTyp, attrName) =>
                if (attrName == attr) { 
                  isAttrInAPI = true
                  // does js code try assigning values to readonly attribute?
                  if (!errorOccurred && attrTyp.contains("r/"))
                    printErrMsg(error_CARM, v+"."+attr)
                }
              }
              // is 'attr' a member of the interface?
              if (!errorOccurred && !isAttrInAPI)
                printErrMsg(error_MCF, attr)
            }
          case _ => None
        }
      }

    case fa@SDot(_, obj, SId(_, x, _, _)) =>
      isAPI(obj) match {
        case Some(p) =>
          if (!isVarForAPI(p._1)) {
            curDB = readDB(p._1)
            curDB.foreach{ content =>
              val methodInfo = getInfoInDB(content, x)
              if (!errorOccurred && methodInfo.length == 0) // is const in webapi?
                printErrMsg(error_CF, x)
            
              // get const value
              methodInfo.foreach{ case(constval, constcase) =>
                if (constcase == "const") temp ++= List((constval, "int"))
              }
            }
          }
          else {
            // check if the variable assigned by API has a member 'x'.
            curDB.foreach{ content =>
              val info = getInfoInDB(content, p._2)
              var isAttrInAPI = false
              info.foreach{ case(attrTyp, attrName) =>
                if (attrName == x) isAttrInAPI = true
              }
              // is 'x' a member of the interface?
              if (!errorOccurred && !isAttrInAPI)
                printErrMsg(error_MCF, x)

            }
          }
            
        case _ => walkUnit(obj)
      }
     case fa@SFunApp(info, fun, args) =>
       args.foreach(walkUnit(_))
       fun match {
         case SDot(_, obj, SId(_, x, _, _)) =>
           isAPI(obj) match {
             case Some(p) =>
               curDB = readDB(p._1)
               curDB.foreach{ content =>
                 val methodInfo = getInfoInDB(content, x)
                 if (!errorOccurred && methodInfo.length == 0) // is method in webapi?
                   printErrMsg(error_CF, x)

                 // extract information of the method
                 var numOfOptParam = 0
                 methodInfo.foreach{ case(typname, typcase) =>
                   if (typcase == "return") methodReturnType = typname
                   else paramTypes ++= List(typname)
                   if (typname.contains("o/")) numOfOptParam += 1
                 }
                 if (curReferVar != "") {
                   varMethodType ++= List((curReferVar, x, methodReturnType))
                   curReferVar = ""
                 }
                 
                 // get information of parameters in javascript
                 if (inputDict.length > 0)
                   inputParams ++= List((inputDict, "dict"))
                 inputParams ++= temp

                 if (!errorOccurred && (inputParams.length > paramTypes.length
                     || inputParams.length < paramTypes.length-numOfOptParam)) // is # of parameters right?
                   printErrMsg(error_NP, x)
                 
                 for (a <- 0 until (paramTypes.length-inputParams.length))
                   inputParams ++= List(("","","")) // input arbitrary value

                 paramTypes.zip(inputParams).foreach { case(partype, inparam) => inparam match {
                   case (name, typ) => name match {
                     case inDict:List[(String, Any, String)] =>
                       val dictInfo = getInfoInDB(content, partype)
                       if (!errorOccurred && dictInfo.length == 0) // is there the dictionary?
                         printErrMsg(error_CF, partype)
                       
                       inDict.foreach{ case(inname, inval, intype) =>
                         var isExistent = false
                         var isProperType = false
                         dictInfo.foreach{ case(dmname, dmtype) =>
                           if (dmname == inname) {
                             isExistent = true
                             if (((dmtype == "long" || dmtype == "short") && intype == "int") ||
                                  (dmtype == intype))
                               isProperType = true
                           }
                         }
                         if (!errorOccurred && !isExistent) // is dictionary member right?
                           printErrMsg(error_DMNSM, inname)
                         if (!errorOccurred && isExistent && !isProperType) // is the member type proper?
                           printErrMsg(error_DMTNM, inname)
                       }
                     case pname:Any =>
                       val ptype = partype.replace("o/", "")
                       var isForVar = false
                       if (typ == "funOrVar") {
                         var isExistent = false
                         functions.foreach{ func =>
                           if (func == name) isExistent = true
                         }
                         varMethodType.foreach{ case(v, m, t) =>
                           if (v == name && t == ptype) {
                             isExistent = true
                             isForVar = true
                           }
                         }
                         // is the callback function OR variable existent?
                         if (!errorOccurred && !isExistent)
                           printErrMsg(error_CMCF, AnyToStr(pname))
                       }
                       // is the parameter type right?
                       if (!errorOccurred && (
                           (typ == "funOrVar" && (!ptype.contains("Callback") && !isForVar)) ||
                           (typ == "int" && !ptype.contains("short") &&
                            !ptype.contains("int") && !ptype.contains("long")) ||
                           (typ == "string" && !ptype.contains("string")) ||
                           (typ == "double" && !ptype.contains("double") && !ptype.contains("float"))) )
                         printErrMsg(error_PTTE, AnyToStr(pname))
                     case _ => None
                   }
                   case _ => None
                 }}
                 
                 // initialize
                 temp = List[Any]()
                 paramTypes = List[String]()
                 inputParams = List[Any]()
               }
             case _ => walkUnit(fun)
           }
         case _ => walkUnit(fun)
       }

     case fa@SVarDecl(info, SId(_, x, _, _), expr) => expr match {
       case Some(p) =>
         curReferVar = x
         walkUnit(p)
       case _ => None
     }
       
     case fa@SFunctional(fds, vds, body, name, params) =>
       name match {
         case SId(_, x, _, _) =>
           if (x != "") functions ++= List(x)
       }
     case fa@SField(info, prop, expr) =>
       prop match {
         case SPropId(_, SId(_, x, _, _)) =>
           expr match {
             case SIntLiteral(_, intval, _) => inputDict ++= List((x, intval, "int"))
             case SStringLiteral(_, _, str) => inputDict ++= List((x, str, "string"))
             case SDoubleLiteral(_, _, doubleval) => inputDict ++= List((x, doubleval, "double"))
             case _ => None
           }
         case _ => None
       }
     case fa@SVarRef(info, SId(_, x, _, _)) => temp ++= List((x, "funOrVar"))
     case fa@SIntLiteral(info, intval, radix) => temp ++= List((intval, "int"))
     case fa@SStringLiteral(info, quote, str) => temp ++= List((str, "string"))
     case fa@SDoubleLiteral(info, str, doubleval) => temp ++= List((doubleval, "double"))

     case _ => super.walkUnit(node)
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
             optNames.foreach{ optName =>
               if (optName == name) {
                 var types = List[String]()
                 var typecases = List[String]()
                 types ++= List(typeName(o.getTyp()))
                 typecases ++= List("return")
                 toList(o.getArgs()).foreach{ a =>
                   var optional = ""
                   toList(a.getAttributes()).foreach{ b => b match {
                     case opt:WEAOptional => optional = "o/"
                   }}
                   types ++= List(optional+typeName(a.getTyp()))
                   typecases ++= List("param")
                 }
                 res ++= types.zip(typecases)
               }
             }
           case c:WConst =>
             if (c.getName() == name)
               res ++= List( (defValue(c.getValue()), "const") )
           case a:WAttribute =>
             if (i.getName() == name) {
               var attr = ""
               toList(a.getAttrs()).foreach{ at => at match {
                 case att:WEAReadonly => attr = "r/"
               }}
               res ++= List( (attr+typeName(a.getTyp()), a.getName()) )
             }
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



