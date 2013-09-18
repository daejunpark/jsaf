

/* bqzk7a8ih1jk3hy30mnqy9jxb */

var dust={};
function getGlobal(){return(function(){return this.dust
}).call(null)
}(function(dust){dust.helpers={};
dust.cache={};
dust.register=function(name,tmpl){if(!name){return
}dust.cache[name]=tmpl
};
dust.render=function(name,context,callback){var chunk=new Stub(callback).head;
dust.load(name,chunk,Context.wrap(context,name)).end()
};
dust.stream=function(name,context){var stream=new Stream();
dust.nextTick(function(){dust.load(name,stream.head,Context.wrap(context,name)).end()
});
return stream
};
dust.renderSource=function(source,context,callback){return dust.compileFn(source)(context,callback)
};
dust.compileFn=function(source,name){var tmpl=dust.loadSource(dust.compile(source,name));
return function(context,callback){var master=callback?new Stub(callback):new Stream();
dust.nextTick(function(){tmpl(master.head,Context.wrap(context,name)).end()
});
return master
}
};
dust.load=function(name,chunk,context){var tmpl=dust.cache[name];
if(tmpl){return tmpl(chunk,context)
}else{if(dust.onLoad){return chunk.map(function(chunk){dust.onLoad(name,function(err,src){if(err){return chunk.setError(err)
}if(!dust.cache[name]){dust.loadSource(dust.compile(src,name))
}dust.cache[name](chunk,context).end()
})
})
}return chunk.setError(new Error("Template Not Found: "+name))
}};
dust.loadSource=function(source,path){return eval(source)
};
if(Array.isArray){dust.isArray=Array.isArray
}else{dust.isArray=function(arr){return Object.prototype.toString.call(arr)=="[object Array]"
}
}dust.nextTick=(function(){if(typeof process!=="undefined"){return process.nextTick
}else{return function(callback){setTimeout(callback,0)
}
}})();
dust.isEmpty=function(value){if(dust.isArray(value)&&!value.length){return true
}if(value===0){return false
}return(!value)
};
dust.filter=function(string,auto,filters){if(filters){for(var i=0,len=filters.length;
i<len;
i++){var name=filters[i];
if(name==="s"){auto=null
}else{if(typeof dust.filters[name]==="function"){string=dust.filters[name](string)
}}}}if(auto){string=dust.filters[auto](string)
}return string
};
dust.filters={h:function(value){return dust.escapeHtml(value)
},j:function(value){return dust.escapeJs(value)
},u:encodeURI,uc:encodeURIComponent,js:function(value){if(!JSON){return value
}return JSON.stringify(value)
},jp:function(value){if(!JSON){return value
}return JSON.parse(value)
}};
function Context(stack,global,blocks){this.stack=stack;
this.global=global;
this.blocks=blocks
}dust.makeBase=function(global){return new Context(new Stack(),global)
};
Context.wrap=function(context,name){if(context instanceof Context){return context
}var global={};
global.__template_name__=name;
return new Context(new Stack(context),global)
};
Context.prototype.get=function(key){var ctx=this.stack,value;
while(ctx){if(ctx.isObject){value=ctx.head[key];
if(!(value===undefined)){return value
}}ctx=ctx.tail
}return this.global?this.global[key]:undefined
};
Context.prototype.getPath=function(cur,down){var ctx=this.stack,len=down.length;
if(cur&&len===0){return ctx.head
}ctx=ctx.head;
var i=0;
while(ctx&&i<len){ctx=ctx[down[i]];
i++
}return ctx
};
Context.prototype.push=function(head,idx,len){return new Context(new Stack(head,this.stack,idx,len),this.global,this.blocks)
};
Context.prototype.rebase=function(head){return new Context(new Stack(head),this.global,this.blocks)
};
Context.prototype.current=function(){return this.stack.head
};
Context.prototype.getBlock=function(key,chk,ctx){if(typeof key==="function"){key=key(chk,ctx).data.join("");
chk.data=[]
}var blocks=this.blocks;
if(!blocks){return
}var len=blocks.length,fn;
while(len--){fn=blocks[len][key];
if(fn){return fn
}}};
Context.prototype.shiftBlocks=function(locals){var blocks=this.blocks,newBlocks;
if(locals){if(!blocks){newBlocks=[locals]
}else{newBlocks=blocks.concat([locals])
}return new Context(this.stack,this.global,newBlocks)
}return this
};
function Stack(head,tail,idx,len){this.tail=tail;
this.isObject=!dust.isArray(head)&&head&&typeof head==="object";
this.head=head;
this.index=idx;
this.of=len
}function Stub(callback){this.head=new Chunk(this);
this.callback=callback;
this.out=""
}Stub.prototype.flush=function(){var chunk=this.head;
while(chunk){if(chunk.flushable){this.out+=chunk.data.join("")
}else{if(chunk.error){this.callback(chunk.error);
this.flush=function(){};
return
}else{return
}}chunk=chunk.next;
this.head=chunk
}this.callback(null,this.out)
};
function Stream(){this.head=new Chunk(this)
}Stream.prototype.flush=function(){var chunk=this.head;
while(chunk){if(chunk.flushable){this.emit("data",chunk.data.join(""))
}else{if(chunk.error){this.emit("error",chunk.error);
this.flush=function(){};
return
}else{return
}}chunk=chunk.next;
this.head=chunk
}this.emit("end")
};
Stream.prototype.emit=function(type,data){if(!this.events){return false
}var handler=this.events[type];
if(!handler){return false
}if(typeof handler=="function"){handler(data)
}else{var listeners=handler.slice(0);
for(var i=0,l=listeners.length;
i<l;
i++){listeners[i](data)
}}};
Stream.prototype.on=function(type,callback){if(!this.events){this.events={}
}if(!this.events[type]){this.events[type]=callback
}else{if(typeof this.events[type]==="function"){this.events[type]=[this.events[type],callback]
}else{this.events[type].push(callback)
}}return this
};
Stream.prototype.pipe=function(stream){this.on("data",function(data){stream.write(data,"utf8")
}).on("end",function(){stream.end()
}).on("error",function(err){stream.error(err)
});
return this
};
function Chunk(root,next,taps){this.root=root;
this.next=next;
this.data=[];
this.flushable=false;
this.taps=taps
}Chunk.prototype.write=function(data){var taps=this.taps;
if(taps){data=taps.go(data)
}this.data.push(data);
return this
};
Chunk.prototype.end=function(data){if(data){this.write(data)
}this.flushable=true;
this.root.flush();
return this
};
Chunk.prototype.map=function(callback){var cursor=new Chunk(this.root,this.next,this.taps),branch=new Chunk(this.root,cursor,this.taps);
this.next=branch;
this.flushable=true;
callback(branch);
return cursor
};
Chunk.prototype.tap=function(tap){var taps=this.taps;
if(taps){this.taps=taps.push(tap)
}else{this.taps=new Tap(tap)
}return this
};
Chunk.prototype.untap=function(){this.taps=this.taps.tail;
return this
};
Chunk.prototype.render=function(body,context){return body(this,context)
};
Chunk.prototype.reference=function(elem,context,auto,filters){if(typeof elem==="function"){elem.isFunction=true;
elem=elem.apply(context.current(),[this,context,null,{auto:auto,filters:filters}]);
if(elem instanceof Chunk){return elem
}}if(!dust.isEmpty(elem)){return this.write(dust.filter(elem,auto,filters))
}else{return this
}};
Chunk.prototype.section=function(elem,context,bodies,params){if(typeof elem==="function"){elem=elem.apply(context.current(),[this,context,bodies,params]);
if(elem instanceof Chunk){return elem
}}var body=bodies.block,skip=bodies["else"];
if(params){context=context.push(params)
}if(dust.isArray(elem)){if(body){var len=elem.length,chunk=this;
if(len>0){if(context.stack.head){context.stack.head["$len"]=len
}for(var i=0;
i<len;
i++){if(context.stack.head){context.stack.head["$idx"]=i
}chunk=body(chunk,context.push(elem[i],i,len))
}if(context.stack.head){context.stack.head["$idx"]=undefined;
context.stack.head["$len"]=undefined
}return chunk
}else{if(skip){return skip(this,context)
}}}}else{if(elem===true){if(body){return body(this,context)
}}else{if(elem||elem===0){if(body){return body(this,context.push(elem))
}}else{if(skip){return skip(this,context)
}}}}return this
};
Chunk.prototype.exists=function(elem,context,bodies){var body=bodies.block,skip=bodies["else"];
if(!dust.isEmpty(elem)){if(body){return body(this,context)
}}else{if(skip){return skip(this,context)
}}return this
};
Chunk.prototype.notexists=function(elem,context,bodies){var body=bodies.block,skip=bodies["else"];
if(dust.isEmpty(elem)){if(body){return body(this,context)
}}else{if(skip){return skip(this,context)
}}return this
};
Chunk.prototype.block=function(elem,context,bodies){var body=bodies.block;
if(elem){body=elem
}if(body){return body(this,context)
}return this
};
Chunk.prototype.partial=function(elem,context,params){var partialContext;
if(params){partialContext=dust.makeBase(context.global);
partialContext.blocks=context.blocks;
if(context.stack&&context.stack.tail){partialContext.stack=context.stack.tail
}partialContext=partialContext.push(params);
partialContext=partialContext.push(context.stack.head)
}else{partialContext=context
}if(typeof elem==="function"){return this.capture(elem,partialContext,function(name,chunk){dust.load(name,chunk,partialContext).end()
})
}return dust.load(elem,this,partialContext)
};
Chunk.prototype.helper=function(name,context,bodies,params){if(dust.helpers[name]){return dust.helpers[name](this,context,bodies,params)
}else{return this
}};
Chunk.prototype.capture=function(body,context,callback){return this.map(function(chunk){var stub=new Stub(function(err,out){if(err){chunk.setError(err)
}else{callback(out,chunk)
}});
body(stub.head,context).end()
})
};
Chunk.prototype.setError=function(err){this.error=err;
this.root.flush();
return this
};
function Tap(head,tail){this.head=head;
this.tail=tail
}Tap.prototype.push=function(tap){return new Tap(tap,this)
};
Tap.prototype.go=function(value){var tap=this;
while(tap){value=tap.head(value);
tap=tap.tail
}return value
};
var HCHARS=new RegExp(/[&<>\"\']/),AMP=/&/g,LT=/</g,GT=/>/g,QUOT=/\"/g,SQUOT=/\'/g;
dust.escapeHtml=function(s){if(typeof s==="string"){if(!HCHARS.test(s)){return s
}return s.replace(AMP,"&amp;").replace(LT,"&lt;").replace(GT,"&gt;").replace(QUOT,"&quot;").replace(SQUOT,"&#39;")
}return s
};
var BS=/\\/g,FS=/\//g,CR=/\r/g,LS=/\u2028/g,PS=/\u2029/g,NL=/\n/g,LF=/\f/g,SQ=/'/g,DQ=/"/g,TB=/\t/g;
dust.escapeJs=function(s){if(typeof s==="string"){return s.replace(BS,"\\\\").replace(FS,"\\/").replace(DQ,'\\"').replace(SQ,"\\'").replace(CR,"\\r").replace(LS,"\\u2028").replace(PS,"\\u2029").replace(NL,"\\n").replace(LF,"\\f").replace(TB,"\\t")
}return s
}
})(dust);
if(typeof exports!=="undefined"){if(typeof process!=="undefined"){require("./server")(dust)
}module.exports=dust
};

/* 39gmm0e77xjrikdpkxed7aywi */

(function(dust){var _console=(typeof console!=="undefined")?console:{log:function(){}};
function isSelect(context){var value=context.current();
return typeof value==="object"&&value.isSelect===true
}function jsonFilter(key,value){if(typeof value==="function"){return value.toString()
}return value
}function filter(chunk,context,bodies,params,filterOp){params=params||{};
var body=bodies.block,actualKey,expectedValue,filterOpType=params.filterOpType||"";
if(typeof params.key!=="undefined"){actualKey=dust.helpers.tap(params.key,chunk,context)
}else{if(isSelect(context)){actualKey=context.current().selectKey;
if(context.current().isResolved){filterOp=function(){return false
}
}}else{_console.log("No key specified for filter in:"+filterOpType+" helper ");
return chunk
}}expectedValue=dust.helpers.tap(params.value,chunk,context);
if(filterOp(coerce(expectedValue,params.type,context),coerce(actualKey,params.type,context))){if(isSelect(context)){context.current().isResolved=true
}if(body){return chunk.render(body,context)
}else{_console.log("Missing body block in the "+filterOpType+" helper ");
return chunk
}}else{if(bodies["else"]){return chunk.render(bodies["else"],context)
}}return chunk
}function coerce(value,type,context){if(value){switch(type||typeof(value)){case"number":return +value;
case"string":return String(value);
case"boolean":value=(value==="false"?false:value);
return Boolean(value);
case"date":return new Date(value);
case"context":return context.get(value)
}}return value
}var helpers={"tap":function(input,chunk,context){var output=input;
if(typeof input==="function"){if(input.isFunction===true){output=input()
}else{output="";
chunk.tap(function(data){output+=data;
return""
}).render(input,context).untap();
if(output===""){output=false
}}}return output
},"sep":function(chunk,context,bodies){var body=bodies.block;
if(context.stack.index===context.stack.of-1){return chunk
}if(body){return bodies.block(chunk,context)
}else{return chunk
}},"idx":function(chunk,context,bodies){var body=bodies.block;
if(body){return bodies.block(chunk,context.push(context.stack.index))
}else{return chunk
}},"contextDump":function(chunk,context,bodies,params){var p=params||{},to=p.to||"output",key=p.key||"current",dump;
to=dust.helpers.tap(to,chunk,context),key=dust.helpers.tap(key,chunk,context);
if(key==="full"){dump=JSON.stringify(context.stack,jsonFilter,2)
}else{dump=JSON.stringify(context.stack.head,jsonFilter,2)
}if(to==="console"){_console.log(dump);
return chunk
}else{return chunk.write(dump)
}},"if":function(chunk,context,bodies,params){var body=bodies.block,skip=bodies["else"];
if(params&&params.cond){var cond=params.cond;
cond=dust.helpers.tap(cond,chunk,context);
if(eval(cond)){if(body){return chunk.render(bodies.block,context)
}else{_console.log("Missing body block in the if helper!");
return chunk
}}if(skip){return chunk.render(bodies["else"],context)
}}else{_console.log("No condition given in the if helper!")
}return chunk
},"math":function(chunk,context,bodies,params){if(params&&typeof params.key!=="undefined"&&params.method){var key=params.key,method=params.method,operand=params.operand,round=params.round,mathOut=null,operError=function(){_console.log("operand is required for this math method");
return null
};
key=dust.helpers.tap(key,chunk,context);
operand=dust.helpers.tap(operand,chunk,context);
switch(method){case"mod":if(operand===0||operand===-0){_console.log("operand for divide operation is 0/-0: expect Nan!")
}mathOut=parseFloat(key)%parseFloat(operand);
break;
case"add":mathOut=parseFloat(key)+parseFloat(operand);
break;
case"subtract":mathOut=parseFloat(key)-parseFloat(operand);
break;
case"multiply":mathOut=parseFloat(key)*parseFloat(operand);
break;
case"divide":if(operand===0||operand===-0){_console.log("operand for divide operation is 0/-0: expect Nan/Infinity!")
}mathOut=parseFloat(key)/parseFloat(operand);
break;
case"ceil":mathOut=Math.ceil(parseFloat(key));
break;
case"floor":mathOut=Math.floor(parseFloat(key));
break;
case"round":mathOut=Math.round(parseFloat(key));
break;
case"abs":mathOut=Math.abs(parseFloat(key));
break;
default:_console.log("method passed is not supported")
}if(mathOut!==null){if(round){mathOut=Math.round(mathOut)
}if(bodies&&bodies.block){return chunk.render(bodies.block,context.push({isSelect:true,isResolved:false,selectKey:mathOut}))
}else{return chunk.write(mathOut)
}}else{return chunk
}}else{_console.log("Key is a required parameter for math helper along with method/operand!")
}return chunk
},"select":function(chunk,context,bodies,params){var body=bodies.block;
if(params&&typeof params.key!=="undefined"){var key=dust.helpers.tap(params.key,chunk,context);
if(body){return chunk.render(bodies.block,context.push({isSelect:true,isResolved:false,selectKey:key}))
}else{_console.log("Missing body block in the select helper ");
return chunk
}}else{_console.log("No key given in the select helper!")
}return chunk
},"eq":function(chunk,context,bodies,params){if(params){params.filterOpType="eq"
}return filter(chunk,context,bodies,params,function(expected,actual){return actual===expected
})
},"ne":function(chunk,context,bodies,params){if(params){params.filterOpType="ne";
return filter(chunk,context,bodies,params,function(expected,actual){return actual!==expected
})
}return chunk
},"lt":function(chunk,context,bodies,params){if(params){params.filterOpType="lt";
return filter(chunk,context,bodies,params,function(expected,actual){return actual<expected
})
}},"lte":function(chunk,context,bodies,params){if(params){params.filterOpType="lte";
return filter(chunk,context,bodies,params,function(expected,actual){return actual<=expected
})
}return chunk
},"gt":function(chunk,context,bodies,params){if(params){params.filterOpType="gt";
return filter(chunk,context,bodies,params,function(expected,actual){return actual>expected
})
}return chunk
},"gte":function(chunk,context,bodies,params){if(params){params.filterOpType="gte";
return filter(chunk,context,bodies,params,function(expected,actual){return actual>=expected
})
}return chunk
},"default":function(chunk,context,bodies,params){if(params){params.filterOpType="default"
}return filter(chunk,context,bodies,params,function(expected,actual){return true
})
},"size":function(chunk,context,bodies,params){var key,value=0,nr,k;
params=params||{};
key=params.key;
if(!key||key===true){value=0
}else{if(dust.isArray(key)){value=key.length
}else{if(!isNaN(parseFloat(key))&&isFinite(key)){value=key
}else{if(typeof key==="object"){nr=0;
for(k in key){if(Object.hasOwnProperty.call(key,k)){nr++
}}value=nr
}else{value=(key+"").length
}}}}return chunk.write(value)
}};
dust.helpers=helpers
})(typeof exports!=="undefined"?module.exports=require("dustjs-linkedin"):dust);

/* d228wbwzysn60azozcfg7gzoa */

(function(){dust.helpers.miniprofile_popup=function(j,e,c,h){var d,g,f,i,k;
if(h&&h.url){d=dust.helpers.tap(h.url,j,e);
g=h.tracking||"";
i=dust.helpers.tap(h.searchClass,j,e)||"";
f=dust.helpers.tap(h.getJs,j,e)||"";
k=dust.helpers.tap(h.template,j,e)||"";
j.write('<span data-tracking="'+g+'"');
if(i){j.write(' class="'+i+" "+dust.filters.h(d)+'"')
}else{j.write(' class="miniprofile-container '+dust.filters.h(d)+'"')
}if(d){j.write(' data-li-url="'+dust.filters.h(d)+'"')
}if(f){j.write(' data-li-getjs="'+f+'"')
}if(k){j.write(' data-li-tl="'+k+'"')
}j.write("><strong>");
j.render(c.block,e);
j.write("</strong></span>")
}return j
};
dust.helpers.module=function(l,e,c,f){var h,j,d,g,i,k;
if(f){h=(typeof f.hasHdr==="undefined"||f.hasHdr.toLowerCase()==="true");
j=f.hdrTag||"h3";
d=f.id||"module-id"+Math.floor(Math.random()*1001);
g=(f.moduleClass)?" "+f.moduleClass:"";
i=f.type||"util";
k=dust.helpers.tap(f.title,l,e)||"";
l.write('<div class="leo-module mod-'+i+g+'" id="'+d+'">');
if(h){l.write('<div class="header"><'+dust.filters.h(j)+">"+dust.filters.h(k)+"</"+dust.filters.h(j)+"></div>")
}l.write('<div class="content">');
l.render(c.block,e);
l.write("</div></div>")
}return l
};
dust.jsControl={};
dust.jsControl.count=1;
dust.jsControl.controls={};
dust.jsControl.controlIds=[];
var a="control-dust-client";
var b="control-dust-server";
dust.jsControl.controlIdentifier=a;
dust.jsControl.contextIdentifier="";
if(typeof(window)==="undefined"){dust.jsControl.controlIdentifier=b;
dust.jsControl.contextIdentifier=Math.floor(Math.random()*100000001)+"-"
}dust.helpers.jsControlFlush=function(d,e,c,f){var g;
if(dust&&dust.jsControl&&dust.jsControl.controlIds&&dust.jsControl.controlIds.length){g='"'+dust.jsControl.controlIds.join(",")+'";';
d.write('<script type="text/javascript">').write("if (dust && dust.jsControl) {").write("if (!dust.jsControl.flushControlIds) {").write('dust.jsControl.flushControlIds = "";').write("} else {").write('dust.jsControl.flushControlIds += ",";').write("}").write("dust.jsControl.flushControlIds += "+g).write("}").write("<\/script>");
dust.jsControl.controlIds=[]
}return d
};
dust.helpers.jsControl=function(d,e,c,i){if(i&&i.name){var f=dust.jsControl.controlIdentifier+"-"+dust.jsControl.contextIdentifier+dust.jsControl.count,g=i.name,h;
dust.jsControl.controlIds.push(f);
if(dust.jsControl.controls[g]!=="initialized"&&i.disableControlInitData===undefined){dust.jsControl.controls[g]="initialized";
h="tl/shared/js-control/"+g.replace(/LI\./,"_").replace(/\./g,"_").toLowerCase();
if(dust.cache[h]){d.partial(h,e)
}}d.write('<script id="'+f+'" type="linkedin/control" class="li-control">');
d.write('LI.Controls.addControl("'+f+'", "'+i.name+'", ');
if(c.block){d.render(c.block,e)
}else{d.write("{}")
}d.write(")<\/script>");
dust.jsControl.count++;
if(dust.jsControl.controlIdentifier===b){dust.helpers.jsControlFlush(d,e,c,i)
}}return d
};
dust.helpers.partial=function(m,e,c,h){var l={},g;
if(h){g=h.key?h.key:"partial";
for(var f in h){if(f!=="key"){l[f]=dust.helpers.tap(h[f],m,e)
}}}var d=e.get(g);
if(d){for(var j in d){l[j]=d[j]
}}l.isPartial=true;
if(h&&h.template){var o=h.template;
if(o.indexOf(":")===-1){return m.partial(o,dust.makeBase(l))
}else{var n=o.indexOf(":");
var k=o.substring(parseInt(n+1,10));
o=o.substring(0,parseInt(n,10));
var i=e.get(k);
if(i){for(var j in i){l[j]=i[j]
}}return m.partial(o,dust.makeBase(l))
}}else{return c.block(m,dust.makeBase(l))
}};
dust.helpers.param=function(d,f,c,h){if(f.global&&f.global.isPartial){if(h){var e=h.key,i=h.defaultVal,g=f.global[e];
if(e&&(typeof g==="undefined")&&(typeof i!=="undefined")){f.global[e]=i
}}}return d
};
dust.helpers.replace=function(k,d,c,g){var g=g||{},l=dust.helpers.tap(g.value,k,d)||"",h=dust.helpers.tap(g.target,k,d)||"",f=dust.helpers.tap(g.replacement,k,d)||"",e=!!g.toLower,i=!!g.toUpper,j=g.target&&new RegExp(h,"g"),m=l.replace(j,f);
m=i&&m.toUpperCase()||m;
m=e&&m.toLowerCase()||m;
return k.write(m)
};
dust.helpers.log=function(d,e,c,f){if(f&&f.info&&window&&window.console){window.console.log("log:",f.info)
}return d
};
dust.i18n=dust.i18n||{};
dust.i18n.cache=dust.i18n.cache||{};
dust.helpers.i18n=function(f,h,d,j){if(j&&j.hide==="true"){return f
}if(j&&(typeof j.key!=="undefined")){var g=j.key,c=j.template||h.global.__template_name__;
if(typeof c!=="undefined"){var e=dust.i18n.cache[c];
if(e){var i=e[j.key];
if(i){if(!j.output){return f.write(i)
}else{h.stack.head[g]=i;
return f
}}}var i=j.text;
if(i){return f.write(i)
}else{if(d.block){return f.render(d.block,h)
}}}return f
}}
})();