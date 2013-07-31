/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.dtv

class DTV {
  private var sceneNames: List[String] = List()
  def setSceneNames(l: List[String]) = sceneNames = l
  def getSceneNames = sceneNames
}