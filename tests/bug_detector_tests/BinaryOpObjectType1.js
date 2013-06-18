var o;
if(Math.random()) o = null;
if(Math.random()) o = false;
if(Math.random()) o = 5432;
if(Math.random()) o = "asdf";
//if(Math.random()) o = {};

if(Math.random()) {
	"x" in o;
}
else {
	"x" instanceof o;
}
