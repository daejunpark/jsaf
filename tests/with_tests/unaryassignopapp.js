{
  with ({x : 3, y : 7, z : 5}) {
    var a = x++ + y;
    var b = a/z++; 
    try {b}
    catch (w) {
      w++;
    }
  }
}
