function f1() {f2();}
function f2() {f3();}
function f3() {f4();/*dumpState();*/}
function f4() {x=y;}

var x = 1, y = 2;
f1();
//dumpValue(x);
var __result1 = x;  // for SAFE
var __expect1 = 2;  // for SAFE
