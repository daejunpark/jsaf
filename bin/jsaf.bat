@ECHO OFF
REM ################################################################################
REM #    Copyright (c) 2012, KAIST.
REM #    All rights reserved.
REM #
REM #    Use is subject to license terms.
REM #
REM #    This distribution may include materials developed by third parties.
REM ################################################################################
set SV=2.9.1
ECHO ON

java -Xms128m -Xmx512m -cp "%JS_HOME%/build;%JS_HOME%/third_party/junit/junit.jar;%JS_HOME%/third_party/commons-lang/commons-lang3-3.1.jar;%JS_HOME%/third_party/wala/wala.util.jar;%JS_HOME%/third_party/wala/wala.cast.jar;%JS_HOME%/third_party/wala/wala.cast.js.jar;%JS_HOME%/third_party/jericho/jericho-html-3.3.jar;%JS_HOME%/third_party/xtc/xtc.jar;%JS_HOME%/third_party/plt/plt.jar;%JAVA_HOME%/lib/tools.jar;%JS_HOME%/third_party/astgen/astgen.jar;%JS_HOME%/third_party/scala/scala-compiler-%SV%.jar;%JS_HOME%/third_party/scala/scala-library-%SV%.jar;%JS_HOME%/third_party/json/lift-json_2.9.1-2.4.jar;%JS_HOME%/third_party/nekohtml/nekohtml.jar;%JS_HOME%/third_party/xerces2/xercesImpl.jar;%JS_HOME%/third_party/xerces2/xml-apis.jar;%JS_HOME%/third_party/z3/com.microsoft.z3.jar" kr.ac.kaist.jsaf.Shell %1 %2 %3 %4 %5 %6 %7 %8 %9
