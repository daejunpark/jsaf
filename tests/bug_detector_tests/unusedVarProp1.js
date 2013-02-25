var x = 3;
function Foo() {this.p = 1}
Foo.prototype.valueOf = undefined;
Foo.prototype.toString = undefined;
var y = new Foo();
x+y;
