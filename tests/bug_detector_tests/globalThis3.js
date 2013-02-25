/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

var x = {p:1};
x.bar = function boo() {return this.p;}

x.bar();

var y = x.bar;

y();