function test1(p1, p2, p3, p3, p4) {
  alert('test1');
}

var test2 = function test2(p1, p2, p3, p4, p4, p5) {
  alert('test2');
}

// This code should generate an error later.
var test3 = new Function("p1", "p2", "p3", "p4", "p5", "p5", "alert('test3');");
