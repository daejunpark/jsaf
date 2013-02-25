{
  with({x: 3, y:7, z:4}) {
    var a = x + y;
    var b = a/++z;
    try {
      b*b;
    }
    catch (w) {
      w = 3;
    }
  }
}
