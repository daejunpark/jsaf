/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.tests

import junit.framework.TestSuite
import java.io.File

object WIDLCheckerJUTest {
  val SEP = File.separator
  val WIDLCHECKER_FAIL_TESTS_DIR = "tests/widlchecker_tests"

  def main(args: String*) = junit.textui.TestRunner.run(suite)

  def suite() = {
    val suite = new TestSuite("Test all .js files in 'tests/widlchecker_tests.")
    val failsOnly = true // false if we want to print out the test results
    //$JUnit-BEGIN$
    suite.addTest(FileTests.compilerSuite(WIDLCHECKER_FAIL_TESTS_DIR, failsOnly, false))
    //$JUnit-END$
    suite
  }
}
