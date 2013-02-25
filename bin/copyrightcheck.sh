#!/bin/sh

################################################################################
#    Copyright (c) 2012-2013, KAIST.
#    All rights reserved.
#
#    Use is subject to license terms.
#
#    This distribution may include materials developed by third parties.
################################################################################

export WKSPACE=$JS_HOME

cd $WKSPACE

# returns OK if $1 does NOT contain $2
strdiff() {
  [ "${1#*$2*}" = "$1" ] && return 0
  return 1
}

for dir in $WKSPACE/src $WKSPACE/lib
do
  cd $dir
  files_2013_java=`find $dir -name "*.java" -newer ../timestamp -print`
  files_2013_rats=`find $dir -name "*.rats" -newer ../timestamp -print`
  files_2013_scala=`find $dir -name "*.scala" -newer ../timestamp -print`
  files_2013_cpp=`find $dir -name "*.c++" -newer ../timestamp -print`
  files_2013_header=`find $dir -name "*.h" -newer ../timestamp -print`

  for fil in $files_2013_java $files_2013_rats $files_2013_scala $files_2013_cpp $files_2013_header
  do
    res=`grep "2013, KAIST" $fil`
    if strdiff "$res" "2013, KAIST, S-Core." ; then
      sres=`grep "Oracle and/or its affiliates" $fil`
      if strdiff "$sres" "Oracle and/or its affiliates" ; then
        ires=`grep "IBM Corporation" $fil`
        if strdiff "$ires" "IBM Corporation" ; then
          cres=`grep "2013, S-Core" $fil`
          if strdiff "$cres" "2013, S-Core, KAIST." ; then
            nres=`grep "package kr.ac.kaist.jsaf.nodes;" $fil`
            if strdiff "$nres" "package kr.ac.kaist.jsaf.nodes;" ; then
              if (strdiff "$fil" "kr/ac/kaist/jsaf/parser/JS.java") &&
                 (strdiff "$fil" "kr/ac/kaist/jsaf/parser/IR.java") &&
                 (strdiff "$fil" "kr/ac/kaist/jsaf/parser/RegExp.java") &&
                 (strdiff "$fil" "kr/ac/kaist/jsaf/parser/WIDL.java") &&
                 (strdiff "$fil" "kr/ac/kaist/jsaf/scala_src/nodes/JSAst.scala") &&
                 (strdiff "$fil" "kr/ac/kaist/jsaf/scala_src/nodes/JSIR.scala") &&
                 (strdiff "$fil" "kr/ac/kaist/jsaf/scala_src/nodes/JSRegExp.scala") &&
                 (strdiff "$fil" "kr/ac/kaist/jsaf/scala_src/nodes/JSWIDL.scala") ; then
                echo $fil
              fi
            fi
          fi
        fi
      fi
    fi
  done
  cd ..
done
