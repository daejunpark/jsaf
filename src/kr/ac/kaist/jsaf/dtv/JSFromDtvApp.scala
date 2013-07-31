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
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.exceptions.UserError
import kr.ac.kaist.jsaf.useful.Useful
import scala.util.parsing.json.JSON

/* 
 * dirName: directory name which ends with '/'
 */
class JSFromDtvApp(dirName: String) extends Walker {
  // valid test (check if "dirName" is valid or not)
  val file: File = new File(dirName)
  if (!file.isDirectory){
    throw new UserError("Need a directory instead of " + file.getCanonicalPath + ".")
  }

  // fetch scene names from app.json and add to the list
  private val json = parseJson
  val sceneNames = obtainSceneNames
  val sceneFileNames: List[String] = sceneNames.map(x => "app/scenes/" + x + ".js")

  val fileNames = (anotherJSFiles ++ List("app/init.js") ++ sceneFileNames).map(x => dirName + x)


  /*
   * parses app.json files and returns parsed code
   */
  private def parseJson(): Map[String, Any] = {
    // test if there exist "app.json" or not
    val file: File = new File(dirName + "app.json")
    if (!file.exists){
      throw new UserError("Cannot find file " + file.getCanonicalPath + ".")
    }

    // read "app.json"
    val in: BufferedReader = Useful.utf8BufferedFileReader(file)
    var code: String = ""
    var line: String = in.readLine
    while (line != null){
      code = code + line
      line = in.readLine
    }

    // parse "app.json"
    // WARNING: Scala standard json library is slow and not very nice.
    JSON.parseFull(code) match {
      case Some(m: Map[String, Any]) => m
      case None => throw new UserError("Cannot parse app.json")
    }
  }

  /*
   * returns a list of scene names
   */
  private def obtainSceneNames() : List[String]= {
    try {
      json("scenes") match{
        case l: List[String] => l
        case _ => List[String]()
      }
    } catch { // the json file doesn't have "scenes" field
      case _ => List[String]()
    }
  }

  /*
   * gets another js files from [files] tag of app.json and returns a list of file names
   */
  private def anotherJSFiles() : List[String] = {
    try {
      json("files") match{
        case l: List[String] => l.filter(s => s.endsWith(".js"))
        case _ => List[String]()
      }
    } catch {
      case _ => List[String]()
    }
  }

  def getFileNames(): JList[String] = toJavaList(fileNames)
  def getScenes(): JList[String] = toJavaList(sceneNames) //JavaList
}
