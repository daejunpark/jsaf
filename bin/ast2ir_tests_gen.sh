#!/bin/sh
################################################################################
#    Copyright (c) 2012, KAIST.
#    All rights reserved.
#
#    Use is subject to license terms.
#
#    This distribution may include materials developed by third parties.
################################################################################

export WKSPACE=$JS_HOME/tests/ast2ir_tests

prename="$@"
name=${prename%.js}
basename=`basename "$prename" .js`
out=$name.test
dump=$name.dump
if [ -f $out ]; then rm $out
fi
echo "################################################################################" >> $out
echo "#    Copyright (c) 2012, KAIST." >> $out
echo "#    All rights reserved.\n#" >> $out
echo "#    Use is subject to license terms.\n#" >> $out
echo "#    This distribution may include materials developed by third parties." >> $out
echo "################################################################################" >> $out
echo "tests=$basename" >> $out
echo "AST2IR_TESTS_DIR=\${JS_HOME}/tests/ast2ir_tests" >> $out
echo "compile" >> $out
echo "compile_out_WCIequals=\\" >> $out
`${JS_HOME}/bin/jsaf compile $prename > $dump`
awk '{printf "%s\\n\\\n", $0} END{printf "\\n\n"}' $dump >> $out
echo "compile_err_equals=" >> $out
echo | tr '\n' '.'
rm $dump
echo
