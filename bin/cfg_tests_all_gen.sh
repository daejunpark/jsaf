#!/bin/sh
################################################################################
#    Copyright (c) 2012, KAIST.
#    All rights reserved.
#
#    Use is subject to license terms.
#
#    This distribution may include materials developed by third parties.
################################################################################

export WKSPACE=$JS_HOME/tests/cfg_tests

cd $WKSPACE

rm $WKSPACE/*.test
files_js=`find . -name "*.js" -print`
for fil in $files_js
do
  prename=`basename $fil`
  name=${prename%.js}
  out=$name.test
  dump=$name.dump
  echo "################################################################################" >> $out
  echo "#    Copyright (c) 2012, KAIST." >> $out
  echo "#    All rights reserved.\n#" >> $out
  echo "#    Use is subject to license terms.\n#" >> $out
  echo "#    This distribution may include materials developed by third parties." >> $out
  echo "################################################################################" >> $out
  echo "tests=$name" >> $out
  echo "CFG_TESTS_DIR=\${JS_HOME}/tests/cfg_tests" >> $out
  echo "cfg" >> $out
  echo "cfg_out_WCIequals=\\" >> $out
  `${JS_HOME}/bin/jsaf cfg $prename > $dump`
  awk '{printf "%s\\n\\\n", $0} END{printf "\\n\n"}' $dump >> $out
  echo "cfg_err_equals=" >> $out
  echo | tr '\n' '.'
  rm $dump
done
echo
cd $JS_HOME
