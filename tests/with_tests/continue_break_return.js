function f(x) {return --x;}
var i = 10;
while ((i = f(i)) > 0) {
  if (i > 3) continue;
  else break;
}
i;
