#!/bin/sh
################################################################################
#    Copyright (c) 2013, KAIST.
#    All rights reserved.
#
#    Use is subject to license terms.
#
#    This distribution may include materials developed by third parties.
################################################################################

export JS_HOME="`$(dirname $0)/js_home`"
export WKSPACE=$JS_HOME/tests/bug_detector_tests

rm $WKSPACE/*.test

files_js=`find $WKSPACE -name "*.js" -print`
for fil in $files_js
do
  prename=`basename $fil`
  name=${prename%.js}
  out=tests/bug_detector_tests/$name.test
  dump=$name.dump
  echo "################################################################################" >> $out
  echo "#    Copyright (c) 2013, KAIST." >> $out
  echo "#    All rights reserved.\n#" >> $out
  echo "#    Use is subject to license terms.\n#" >> $out
  echo "#    This distribution may include materials developed by third parties." >> $out
  echo "################################################################################" >> $out
  echo "tests=$name" >> $out
  echo "BUGDETECTOR_TESTS_DIR=\${JS_HOME}/tests/bug_detector_tests" >> $out
  echo "bug-detector" >> $out
  echo "bug-detector_out_WCIequals=\\" >> $out
  `${JS_HOME}/bin/jsaf bug-detector tests/bug_detector_tests/$name.js > $dump`
  awk '{sub(/\/Users\/sukyoungryu\/PLRG\/safe\/tests\/bug_detector_tests/, "${BUGDETECTOR_TESTS_DIR}", $0); print $0, "\\n\\"} END{printf "\\n\n"}' $dump >> $out
  echo "bug-detector_err_equals=" >> $out
  echo | tr '\n' '.'
  rm $dump
done
echo
cd $JS_HOME
