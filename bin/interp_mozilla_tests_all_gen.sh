#!/bin/sh
################################################################################
#    Copyright (c) 2012, KAIST.
#    All rights reserved.
#
#    Use is subject to license terms.
#
#    This distribution may include materials developed by third parties.
################################################################################

export WKSPACE=$JS_HOME/tests/interpreter_mozilla_tests/$1

cd $WKSPACE

rm $WKSPACE/*.test
files_js=`find $WKSPACE -name "*.js" -print`
for fil in $files_js
do
  prename=`basename $fil`
  name=${prename%.js}
  in=$name.in
  out=$name.test
  dump=$name.dump
  if [ $name = "browser" ]; then continue
  elif [ $name = "shell" ]; then continue
  fi
  echo "################################################################################" >> $out
  echo "#    Copyright (c) 2012, KAIST." >> $out
  echo "#    All rights reserved.\n#" >> $out
  echo "#    Use is subject to license terms.\n#" >> $out
  echo "#    This distribution may include materials developed by third parties." >> $out
  echo "################################################################################" >> $out
  echo "tests=$name" >> $out
  echo "INTERPRETER_TESTS_DIR=\${JS_HOME}/tests/interpreter_mozilla_tests/$1" >> $out
  echo "interpret_mozilla" >> $out
  echo "interpret_mozilla_out_WCIequals=\\" >> $out
  #`${JS_HOME}/bin/jsaf interpret -mozilla $fil > $dump`
  cat $WKSPACE/../../shell.js $WKSPACE/../shell.js $WKSPACE/shell.js $fil > $in
  `java -jar ${JS_HOME}/third_party/rhino/js.jar $in > $dump`
  awk '{printf "%s\\n\\\n", $0} END{printf "\\n\n"}' $dump >> $out
  echo "interpret_err_equals=" >> $out
  echo | tr '\n' '.'
  rm $in
  rm $dump
done
echo
cd $JS_HOME
