var x = 3;
function Foo() {this.p = 1}
var y = new Foo();
x+y; // No TypeError
Foo.prototype.valueOf = undefined;
Foo.prototype.toString = undefined;
var z = new Foo();
z+x; // TypeError
