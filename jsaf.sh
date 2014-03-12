#!/bin/bash

cmd=`basename $0`
jsaf=`dirname $0`

tmp=`mktemp /tmp/$cmd.XXXXXXXXXX`
rm -f $tmp

export JS_HOME=$jsaf
$jsaf/bin/jsaf parse -out $tmp $1
[ -f $tmp ] || exit 1
$jsaf/bin/jsaf unparse $tmp

rm -f $tmp
