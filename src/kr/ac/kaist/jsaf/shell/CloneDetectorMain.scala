/******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.shell

import kr.ac.kaist.jsaf.clone_detector.CloneDetector

////////////////////////////////////////////////////////////////////////////////
// Clone Detector
////////////////////////////////////////////////////////////////////////////////
object CloneDetectorMain {
  /**
   * Reports detected clones in the file.
   */
  def cloneDetector: Int = {
    CloneDetector.doit
    0
  }
}
