// From TestV8. 
// 
// Once this test passes, uncomment the corresponding line in the to_precision 
// test in TestV8.
//
// Currently gives the output:
//
// testtoprecision1.js:2: [info] Abstract value: "-1.235e+27"

// Should be -1.234e+27
//dumpValue((-1.2345e+27).toPrecision(4));
var __result1 = (-1.2345e+27).toPrecision(4);  // for SAFE
var __expect1 = "-1.234e+27";  // for SAFE
