function f(a, b, c) {
  var x, y = true, z = c + 8;
  var v = this, w = null, u = 4.8, t = "done";
  with ({z:a}) {
    var x, y = true, z = 4 + 8;
    var v = this, w = null, u = b, t = "done";
    return z;
  }
}
f(1, 2, 3);
