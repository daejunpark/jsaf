// From TestV8. 
// 
// Once this test passes, uncomment the corresponding lines in array_length 
// test in TestV8.
//
// Currently gives the output:
//
// testarray2.js:15: [definite] RangeError, assigning invalid value to array 'length' property
// testarray2.js:15: [definite] Dead assignment, property toString is never read
// testarray2.js:15: [definite] Unreachable function
//
// 

a = []
a.length = { toString : function () { return 42; }}

//a.length
var __result1 = a.length;  // for SAFE
var __expect1 = 42;  // for SAFE
