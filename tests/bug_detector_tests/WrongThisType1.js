new String("abc").toString();
new String("def").valueOf();

new Boolean(true).toString();
new Boolean(false).valueOf();

new Number(1).toString();
new Number(2).valueOf();

c = function () { };

c.prototype = String.prototype;
if (Math.random()) new c().toString();
if (Math.random()) new c().valueOf();

c.prototype = Boolean.prototype;
if (Math.random()) new c().toString();
if (Math.random()) new c().valueOf();

c.prototype = Number.prototype;
if (Math.random()) new c().toString();
if (Math.random()) new c().valueOf();
