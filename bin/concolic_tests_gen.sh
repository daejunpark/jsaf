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

prename="$@"
name=${prename%.js}
basename=`basename "$prename" .js`
out=$name.test
dump=$name.dump
if [ -f $out ]; then rm $out
fi
echo "################################################################################" >> $out
echo "#    Copyright (c) 2013, KAIST." >> $out
echo "#    All rights reserved.\n#" >> $out
echo "#    Use is subject to license terms.\n#" >> $out
echo "#    This distribution may include materials developed by third parties." >> $out
echo "################################################################################" >> $out
echo "tests=$basename" >> $out
echo "CONCOLIC_TESTS_DIR=\${JS_HOME}/tests/concolic_tests" >> $out
echo "concolic" >> $out
echo "concolic_out_WCIequals=\\" >> $out
echo $prename
`${JS_HOME}/bin/jsaf concolic $prename > $dump`
awk '{printf "%s\\n\\\n", $0} END{printf "\\n\n"}' $dump >> $out
echo "concolic_err_equals=" >> $out
echo | tr '\n' '.'
rm $dump
echo
