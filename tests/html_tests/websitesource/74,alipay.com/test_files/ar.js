/*
 * Light JavaScript Library
 * The core of Light
 * 
 * @copyright Copyright 2011, alipay.com
 * @author janlay@gmail.com
 *
 * $Id: core.js 20163 2011-11-02 09:15:19Z qiuping.zhou $
 */
window.light||function(window,undefined){var document=window.document,navigator=window.navigator,location=window.location;
var urlParts,ajaxLocation,rurl=/^([\w\+\.\-]+:)(?:\/\/([^\/?#:]*)(?::(\d+))?([^?#]*))?/;
try{ajaxLocation=location.href}catch(e){ajaxLocation=document.createElement("a");
ajaxLocation.href="";ajaxLocation=ajaxLocation.href}urlParts=rurl.exec(ajaxLocation.toLowerCase())||[];
var light={version:"0.1",timestamp:new Date().getTime(),debug:urlParts[2].indexOf("alipay.com")===-1,baseDomain:function(){var parts=urlParts[2].split(".");
return parts.length>2?parts[parts.length-2]+"."+parts[parts.length-1]:urlParts[2]
}(),urlParts:urlParts,toString:function(){var result="Light JavaScript Library version "+light.version;
if(light.debug){result+=", debug enabled"}result+=".";return result},toArray:function(list){return Array.prototype.slice.apply(list)
},register:function(path,root,obj){var items=path.split("/"),parent=root||light;if(!items[0]){parent=window;
items.shift()}var name,me=parent;for(var i=0,l=items.length-1;i<l;i++){if(!(name=items[i])){continue
}parent=parent[name]=parent[name]||{}}name=items[i];if(name){parent=parent[name]=obj===undefined?{}:obj
}return parent},extend:function(deep){var args=light.toArray(arguments);if(typeof args[0]!=="boolean"){args.unshift((deep=false))
}if(args.length<2){return null}var start=2,o=args[1],obj;if(args.length===2){start=1;
o=light}for(var i=start,l=args.length;i<l;i++){obj=args[i];if(!obj||typeof obj!=="object"){continue
}for(var prop in obj){var item=obj[prop];if(item===o||!obj.hasOwnProperty(prop)){continue
}if(light.isArray(item)){o[prop]=Array.prototype.concat.call(item)}else{if(deep&&item instanceof Object&&!light.isFunction(item)&&!item.nodeType){var tmp=o[prop]||{};
o[prop]=light.extend(true,tmp,obj[prop])}else{if(item!==undefined){o[prop]=item}}}}}return o
},deriveFrom:function(superClass,instanceMembers,staticMembers){if(arguments.length<2){return superClass
}var klass=(instanceMembers&&instanceMembers.init)||function(){superClass.constructor.apply(this,arguments)
};light.extend(true,klass.prototype,superClass.prototype,instanceMembers);klass.constructor=klass;
staticMembers&&light.extend(true,klass,staticMembers);klass.__super=superClass;return klass
},module:function(name,obj){var o=light.register(name,null,obj);if(light.isFunction(obj)){o.constructor=obj
}return o},each:function(object,callback,args){if(!object){return object}var length=object.length;
if(length!==undefined&&"reverse" in object){var i=0;while(i<length){if(callback.call(object[i],i,object[i],object)===false){break
}i++}}else{var name;for(name in object){if(callback.call(object[name],name,object[name],object)===false){break
}}}return object},isFunction:function(obj){return light.type(obj)==="function"},isArray:Array.isArray||function(obj){return light.type(obj)==="array"
},isWindow:function(obj){return obj&&typeof obj==="object"&&"setInterval" in obj},type:function(obj){return(obj===null||obj===undefined)?String(obj):class2type[Object.prototype.toString.call(obj)]||"object"
},has:function(path){if(!path){return false}var parts=path.split("/"),head=light,i,len;
if(!parts[0]){head=window;parts.shift()}for(i=0,len=parts.length;i<len;i++){head=head[parts[i]];
if(head===undefined){return false}}return true},noop:function(){}};var class2type={};
light.each("Boolean Number String Function Array Date RegExp Object".split(" "),function(i,name){class2type["[object "+name+"]"]=name.toLowerCase()
});window.light=light}(window);
/*
 * Light JavaScript Library
 * Utilities for Light
 * 
 * @copyright Copyright 2011, alipay.com
 * @author janlay@gmail.com
 *
 * $Id: util.js 20304 2011-11-03 09:31:32Z qiuping.zhou $
 */
light.log||light.extend({log:function(){if(!light.debug||!window.console||!console.log){return function(){if(!light.debug){return
}try{window.console&&console.log&&console.log.apply(console,arguments)}catch(e){}}
}if(Function.prototype.bind){return function(){if(!light.debug){return}var fn=Function.prototype.bind.call(console.log,console);
fn.apply(console,arguments)}}else{if(console.log.apply){return function(){if(!light.debug){return
}console.log.apply(console,arguments)}}else{return light.debug?console.log:light.noop
}}}(),inspect:function(obj){if(window.JSON&&JSON.stringify){return JSON.stringify(obj)
}else{if(typeof obj==="object"){var value,result=[],root=obj;for(var prop in root){if(typeof(value=root[prop])=="object"){result.push(arguments.callee(value))
}else{result.push(prop+"="+root[prop])}}return result.join("\n")}else{return String(obj)
}}},track:function(){var buffer=[],send=function(seed){if(window.Tracker){Tracker.click(seed)
}else{buffer.push(seed);window.setTimeout(function(){send(buffer.shift())},100)}};
return function(seed,withClientInfo){if(!seed){return}if(withClientInfo){var ua=light.client.info,ver=ua.browser.version;
ver=ver?ver[0]:"na";seed+="-"+(ua.browser.name||"na")+"-"+(ua.engine.name||"na")+"-"+ver
}send(seed)}}(),trim:function(text){if(!text){return""}return String.prototype.trim?String.prototype.trim.apply(text):text.replace(/^\s+|\s+$/g,"")
},substitute:function(template,map){if(!template){return""}if(!map){return template
}if(typeof template!=="string"){throw"invalid template"}return template.replace(new RegExp("{\\w+}","gmi"),function(property){var prop=property.substr(1,property.length-2);
return prop in map?map[prop].toString():""})},encode:encodeURIComponent||escape,decode:decodeURIComponent||unescape,param:function(obj,splitter,connector){splitter=splitter||"=";
var stack=[];light.each(obj,function(property,value){if(!property||!obj.hasOwnProperty(property)){return
}stack.push(light.encode(property)+splitter+light.encode(value))});return stack.join(connector||"&")
},unparam:function(text,splitter,connector){var obj={};if(!text){return obj}splitter=splitter||"=";
light.each(text.split(connector||"&"),function(i,item){var pair=item.split(splitter,2);
if(!pair[0]||pair.length!==2){return}obj[light.decode(pair[0])]=light.decode(pair[1])
});return obj},trimTag:function(html){if(!html||!document.createElement){return""
}var el=document.createElement("DIV");el.innerHTML=html;var text=el.textContent||el.innerText||"";
el=null;return text},escapeHTML:function(html){if(!html){return""}var str=html.replace(/>/g,"&gt;");
str=str.replace(/</g,"&lt;");str=str.replace(/&/g,"&amp;");str=str.replace(/"/g,"&quot;");
str=str.replace(/'/g,"&#039;");return str},unescapeHTML:function(text){if(!text){return""
}var str=text.replace(/&gt;/g,">");str=str.replace(/&lt;/g,"<");str=str.replace(/&amp;/g,"&");
str=str.replace(/&quot;/g,'"');str=str.replace(/&#039;/g,"'");return str},toJSON:function(source){if(typeof source!=="string"||!source){return null
}var data=light.trim(source);return window.JSON&&JSON.parse?JSON.parse(data):(new Function("return "+data))()
}});light.queue||(function(){var queue=function(){this.stack=[];var that=this,args=[].slice.call(arguments,0);
args&&light.each(args,function(arg){that.add(arg)})};queue.prototype={add:function(fn){this.stack.push(fn)
},clear:function(){this.stack=[]},invoke:function(){var that=this,args=[].slice.call(arguments,0);
fn=this.stack.shift();this.next||(this.next=function(){if(that.stack.length){that.invoke.apply(that,args)
}});fn.apply(null,[this.next].concat(args))}};light.queue=queue})();
/*
 * Light JavaScript Library
 * Client information dection
 * 
 * @copyright Copyright 2011, alipay.com
 * @author janlay@gmail.com
 *
 * $Id: info.js 18350 2011-09-19 12:35:25Z taibo $
 */
light.has("client/info")||function(window,light,undefined){var document=window.document,navigator=window.navigator,location=window.location;
var userAgent=navigator.userAgent?navigator.userAgent.toLowerCase():"",platform=navigator.platform||"",vendor=navigator.vendor||"",external=window.external;
var data={device:{pc:"windows",ipad:"ipad",ipod:"ipod",iphone:"iphone",mac:"macintosh",android:"android",nokia:/nokia([^\/ ]+)/},os:{windows:/windows nt (\d)\.(\d)/,macos:/mac os x (\d+)[\._](\d+)(?:[\._](\d+))?/,linux:"linux",ios:/iphone os (\d)[\._](\d)/,android:/android (\d)\.(\d)/,chromeos:/cros i686 (\d+)\.(\d+)(?:\.(\d+))?/,windowsce:userAgent.indexOf("windows ce ")>0?(/windows ce (\d)\.(\d)/):"windows ce",symbian:/symbianos\/(\d+)\.(\d+)/,blackberry:"blackberry"},engine:{trident:/msie (\d+)\.(\d)/,webkit:/applewebkit\/(\d+)\.(\d+)/,gecko:/gecko\/(\d+)/,presto:/presto\/(\d+).(\d+)/},browser:{"360":function(){if(!info.os.windows){return false
}if(external){try{return external.twGetVersion(external.twGetSecurityID(window)).split(".")
}catch(e){try{return external.twGetRunPath.toLowerCase().indexOf("360se")!==-1||!!external.twGetSecurityID(window)
}catch(e){}}}return(/360(?:se|chrome)/)},mx:function(){if(!info.os.windows){return false
}if(external){try{return(external.mxVersion||external.max_version).split(".")}catch(e){}}return userAgent.indexOf("maxthon ")!==-1?(/maxthon (\d)\.(\d)/):"maxthon"
},sg:/ se (\d)\./,tw:function(){if(!info.os.windows){return false}if(external){try{return external.twGetRunPath.toLowerCase().indexOf("theworld")!==-1
}catch(e){}}return"theworld"},qq:function(){return userAgent.indexOf("qqbrowser/")>0?(/qqbrowser\/(\d+)\.(\d+)\.(\d+)(?:\.(\d+))?/):(/tencenttraveler (\d)\.(\d)/)
},ie:userAgent.indexOf("trident/")>0?(/trident\/(\d+)\.(\d+)/):(/msie (\d+)\.(\d+)/),chrome:/chrome\/(\d+)\.(\d+)\.(\d+)(?:\.([ab\d]+))?/,safari:/version\/(\d+)\.(\d+)(?:\.([ab\d]+))? safari\//,firefox:/firefox\/(\d+)\.([ab\d]+)/,opera:/opera.+version\/(\d+)\.([ab\d]+)/},feature:{"64bitBrowser":"win64; x64;","64bitOS":/win64|wow64/,security:/ (i|u|s|sv1)[;\)]/,simulator:function(){return info.os.ios&&screen.width>960
}}};var detected=-1,notDetected=0,info={},has=function(type,name,version){var currentVersion;
if(!info[type]||!(currentVersion=info[type][name])){return false}if(!version){return true
}var v=version;if(typeof v==="string"){v=v.split(".")}else{if(typeof v==="number"){v=[v]
}}var v1,v2;for(var i=0,len=Math.max(v.length,currentVersion.length);i<len;i++){v1=parseInt(v[i],10)||0;
v2=parseInt(currentVersion[i],10)||0;if(v1!==v2){return v1<v2}}return true};light.each(data,function(item,itemData){info["has"+item.charAt(0).toUpperCase()+item.slice(1)]=function(name,version){return has(item,name,version)
};var entry=info[item]={};light.each(itemData,function(name,expression){var version=[notDetected],expr=light.isFunction(expression)?expression.apply(info):expression;
if(expr){if(expr===true){version=[detected]}else{if(typeof expr==="string"){version=[userAgent.indexOf(expr)!==-1?detected:notDetected]
}else{var v=expr;if(expr.exec){v=expr.exec(userAgent)||[];v.length&&v.shift()}for(var i=0;
i<v.length;i++){version[i]=parseInt(v[i],10)||0}}}}var found=!!version[0];if(found){entry[name]=entry.version=version;
entry.name=name}return !found})});if(!info.engine.name&&window.ActiveXObject){if(document.documentMode){info.engine.trident=info.engine.version=[document.documentMode,0]
}else{if(!info.engine.trident){info.engine.trident=info.engine.version=[detected]
}}info.engine.name="trident"}else{if(!info.os.windows&&info.hasEngine("trident",6)){info.os.windows=info.os.version=[detected];
info.os.name="windows"}}if(info.browser.ie&&userAgent.indexOf("trident/")>0){info.browser.ie[0]=info.browser.version[0]=info.browser.version[0]+4
}light.module("client/info",info)}(window,light);
/*
 * Light JavaScript Library
 * Cookie or cookieless storage implement
 * 
 * @copyright Copyright 2011, alipay.com
 * @author janlay@gmail.com
 *
 * $Id: storage.js 18350 2011-09-19 12:35:25Z taibo $
 */
light.has("client/storage")||function(window,light,undefined){var document=window.document,navigator=window.navigator,location=window.location;
var userDataId="__ud",userDataHtml='<input type="hidden" id="'+userDataId+'" style="behavior:url("#default#userData")"/>',userDataExists=false,getUserData=function(){if(!userDataExists){light.write(userDataHtml);
userDataExists=true}return light.get(userDataId)};var storage={cookie:null,defaultStorage:window.localStorage,set:function(name,value){if(storage.cookie&&navigator.cookieEnabled){var sCookie=name+"="+encodeURIComponent(value);
if(!storage.cookie.days){var exp=new Date(new Date().getTime()+storage.cookie.days*365*24*60*60*1000);
sCookie+="; expires="+exp.toGMTString()}if(storage.cookie.domain){sCookie+="; domain=."+storage.cookie.domain
}sCookie+="; path=."+(storage.cookie.path||light.urlParts[4]||"/");document.cookie=sCookie
}if(advanced){storage.defaultStorage.setItem(name,value)}else{var node=getUserData();
if(node){node.setAttribute(name,value);try{node.save(userDataId)}catch(e){}}}},get:function(name,defaultValue){var value;
if(advanced){value=storage.defaultStorage.getItem(name)}else{var node=getUserData();
if(node){try{node.load(userDataId)}catch(e){}value=node.getAttribute(name)}}if(!value&&storage.cookie&&navigator.cookieEnabled){var cookie=document.cookie,start=cookie.indexOf(name+"=");
if(start!=-1){start+=name.length+1;var end=cookie.indexOf(";",start);if(end==-1){end=cookie.length
}value=light.decode(cookie.substring(start,end)||"")}}return value||defaultValue||""
}};var advanced=!!storage.defaultStorage;light.module("client/storage",storage)}(window,light);this.Tracker||function(c){var o=c.document,p=c.location,i=document.URL||"",e=c.performance,j=c.light,b,q,s=p.protocol,x=s+"//kcart.alipay.com/web/bi.do",y=s+"//kcart.alipay.com/web/1.do",z=s+"//log.mmstat.com/5.gif";e&&e.timing?q=e.timing.navigationStart:c._to&&_to.start&&(q=_to.start.getTime());c.Tracker=b=function(){};b.prototype={watch:function(){b.click("tracker-watch")}};b.extend=function(a){for(var b=1,t=arguments.length;b<t;b++)for(var c in arguments[b])arguments[b].hasOwnProperty(c)&&(a[c]=
arguments[b][c]);return a};b.version="1.0";b.enabled=!0;b.debug=!1;b.seedName="seed";b.minInterval=1E3;for(var u,m=o.getElementsByTagName("meta"),n=0,f,A=m.length;n<A;n++)if((f=m[n].getAttribute("name"))&&"abtest"==f.toLowerCase()){u=m[n].getAttribute("content");break}b.dispatchEvent=function(a,b,c){a.attachEvent?a.attachEvent("on"+b,function(b){c.call(a,b)}):a.addEventListener?a.addEventListener(b,c,!1):a["on"+b]=function(b){c.call(a,b)}};b.getTarget=function(a){a=a.target||a.srcElement;try{if(a&&
3===a.nodeType)return a.parentNode}catch(b){}return a};b.send=function(a,c,t){if("string"!==typeof a||!a)throw Error("Invalid page");a={ref:c||"-",pg:a||"",r:(new Date).getTime(),v:b.version};u&&(a.ABTest=u,a.pg+=(0<=a.pg.indexOf("?")?"&":"?")+"ABTest="+u);t&&j.extend(a,t);a=j.param(a);if("file:"!=s&&!b.debug){var d=new Image(1,1);d.onload=function(){d.onload=null};d.src=x+"?"+a;/\bcna=/.test(document.cookie)||(a=j.param({url:y+"?"+a}),(new Image(1,1)).src=z+"?"+a)}else j.log("Tracker debug: %s.",
a)};if(o&&p){var m=o.referrer,v=Math.random(),d={screen:"-x-",color:"-",BIProfile:"page"};c.screen&&(d.screen=screen.width+"x"+screen.height,d.sc=screen.colorDepth+"-bit");d.utmhn=p.hostname;d.rnd=v;c.analytic_var&&(d.ana=analytic_var);j.client&&(f=j.client.info,p=(f.os.name||"na")+"/"+(f.os.version||[-1]).join("."),n=(f.browser.name||"na")+"/"+(f.browser.version||[-1]).join("."),f=(f.engine.name||"na")+"/"+(f.engine.version||[-1]).join("."),d._clnt=p+"|"+f+"|"+n);b.send(i,m,d);m=i;d={};if(!parseInt(8*
Math.random(),10)){d.BIProfile="load";var g=0,r=0,k=function(){if(!k.invoked){k.invoked=true;g=(c._to&&_to.ready?_to.ready.getTime():(new Date).getTime())-q;if(g>2E4){l.invoked=true;d.tm="-x-";b.send(i,"",d)}}},l=function(){if(!(g>2E4||l.invoked)){l.invoked=true;r=(c._to&&_to.end?_to.end.getTime():(new Date).getTime())-q;if(e&&e.timing){g=e.timing.domContentLoadedEventStart-e.timing.navigationStart;r=e.timing.loadEventStart-e.timing.navigationStart}w()}},w=function(){if(g){g>r&&(g=r-100);if(!(g<10)){d.tm=
""+g+"x"+r;d.rnd=v;b.send(i,"",d)}}else setTimeout(w,50)};if(q){if(c._to&&_to.ready)k();else if(c.YAHOO&&YAHOO.util&&YAHOO.util.Event)YAHOO.util.Event.onDOMReady(k);else if(c.jQuery)jQuery(k);else if(c.Y&&Y.on)Y.on("domready",k);else k();c._to&&_to.end?l():(c.setTimeout(l,16E4),b.dispatchEvent(c,"load",l),b.dispatchEvent(c,"unload",l))}}}o&&b.dispatchEvent(o,"mousedown",function(a){if((a=b.getTarget(a))&&a.nodeType){for(;a&&"HTML"!=a.nodeName&&a.getAttribute&&!a.getAttribute(b.seedName);)a=a.parentNode;
if(a&&!(1!==a.nodeType||"HTML"==a.nodeName)){var c,d;if("A"===a.nodeName){d=a.getAttribute("href",2)||"";if(d===i||0===d.indexOf(i+"#"))d="";(d=d.match(/[?&]_scType=([^&#]+)/))&&(c={_scType:d[1]})}b.click(a.getAttribute(b.seedName),c)}}});b.click=function(){var a={},c=i.split("?").shift(),d=c.indexOf(";jsessionid=");0<=d&&(c=c.substr(0,d));a.clk=i;return function(d,f){if(d){var h=d.split(":",2);2<=h.length||h.unshift("clk");var e=h[0];if(d=h[1]){h={seed:d};f&&j.extend(h,f);var h=j.param(h),h=c+"?"+
h,g;e&&(g={BIProfile:e});b.send(h,a[e]||"",g);a[e]=h}}}}();b.log=function(a,c){b.click((c||"syslog")+":"+a)};b.error=function(a){b.click("syserr:"+a)};b.calc=function(a,c){b.click("calc:"+a,{value:c})}}(this);
!window.monitor||(function(){if(0!=Math.floor(Math.random()/window.monitor._rate)){return
}var win=window,loc=location,doc=document,M=win.monitor,MODE={ONLINE:0,SIT:1,DEBUG:2,DEV:3,LOCAL:4},mode,scriptBase,scriptB,LOG_SERVER,SEND_STATUS={COMPLETE:0,SENDING:1},sendState=SEND_STATUS.COMPLETE,readyTime=new Date()-M._startTime,loadTime=readyTime*1.7;
M.version="1.3";M._loc={protocol:loc.protocol,hostname:loc.hostname,pathname:loc.pathname,href:loc.href,hash:loc.hash};
function addEvent(target,evt,handler){if(target.addEventListener){target.addEventListener(evt,handler,false)
}else{if(target.attachEvent){target.attachEvent("on"+evt,handler)}}}function loadHandler(){if(loadHandler.invoked){return
}loadHandler.invoked=true;loadTime=M._now()-M._startTime}function readyHandler(){if(readyHandler.invoked){return
}readyHandler.invoked=true;loadTime=M._now()-M._startTime}if(window.addEventListener){window.addEventListener("DOMContentLoaded",readyHandler,false)
}else{if(window.$E&&$E.domReady){$E.domReady(readyHandler)}else{if(win.YAHOO&&YAHOO.util&&YAHOO.util.Event){YAHOO.util.Event.onDOMReady(readyHandler)
}else{if(win.jQuery){jQuery(readyHandler)}else{if(win.Y&&Y.on){Y.on("domready",readyHandler)
}}}}}addEvent(window,"load",loadHandler);addEvent(window,"unload",loadHandler);M.S={startsWith:function(str,ch){if(typeof(str)=="undefined"||typeof(ch)=="undefined"){return false
}return str.indexOf(ch)==0},endsWith:function(str,ch){if(typeof(str)=="undefined"||typeof(ch)=="undefined"){return false
}return str.lastIndexOf(ch)==(str.length-ch.length)},byteLength:function(str){if(!str){return 0
}return str.replace(/[^\x00-\xff]/g,"xx").length},isLower:function(str){if(typeof(str)=="undefined"){return false
}return str==str.toLowerCase()},repeat:function(str,times){return new Array((times||0)+1).join(str)
},trim:function(str){return str.replace(/^\s+/,"").replace(/\s+$/,"")},camelize:function(str){return str.replace(/\-+([a-z])/g,function($0,$1){return $1.toUpperCase()
})},rand:function(){var s=""+Math.random(),l=s.length;return s.substr(2,2)+s.substr(l-2)
}};if(M.S.endsWith(M._loc.hostname,".alipay.com")){mode=MODE.ONLINE;LOG_SERVER="https://magentmng.alipay.com/m.gif";
scriptBase="https://assets.alipay.com/ar/??";scriptB=["alipay.fmsmng.monitor-1.0-b.js"];
M.nocache=false}else{if(M._loc.hostname=="m.sit.alipay.net"){if(mode==MODE.LOCAL){scriptBase="http://m.sit.alipay.net/js/";
scriptB=["domlint2.js","monitor-b.src.js"]}else{mode=MODE.DEV;scriptBase="http://dev.assets.alipay.net/ar/??";
scriptB=["alipay.fmsmng.monitor-1.0-b.js"]}LOG_SERVER="http://fmsmng.sit.alipay.net:7788/m.gif";
M.nocache=true}else{if(M.S.endsWith(M._loc.hostname,".sit.alipay.net")){mode=MODE.SIT;
LOG_SERVER="http://fmsmng.sit.alipay.net:7788/m.gif";scriptBase="http://assets.sit.alipay.net/ar/??";
scriptB=["alipay.fmsmng.monitor-1.0-b.js"];M.nocache=false}else{return}}}M.debug=mode==MODE.DEV||"#debug"==M._loc.hash||false;
M.checkProtocol="https:"==M._loc.protocol;M.rethrow=true;M.delay=1800;M.timeout=2000;
var idx=M._loc.pathname.indexOf(";jsessionid=");M.url=M._loc.protocol+"//"+M._loc.hostname+(idx<0?M._loc.pathname:M._loc.pathname.substr(0,idx));
M.res={img:[],css:[],js:[],fla:[]};var $JSON={escape:function(str){return str.replace(/\r|\n/g,"").replace(/\\/g,"\\\\").replace(/\"/g,'\\"')
},toString:function(obj){switch(typeof obj){case"string":return'"'+$JSON.escape(obj)+'"';
case"number":return isFinite(obj)?String(obj):"null";case"boolean":case"null":return String(obj);
case"undefined":return"null";case"object":if(null==obj){return"null"}var type=Object.prototype.toString.call(obj);
if("[object Array]"==type){var a=[];for(var i=0,l=obj.length;i<l;i++){a[i]=$JSON.toString(obj[i])
}return"["+a.join(",")+"]"}else{if("[object RegExp]"==type){return"/"+obj.source+"/"+(obj.ignoreCase?"i":"")+(obj.multiline?"m":"")+(obj.global?"g":"")
}else{var o=[];for(var k in obj){if(Object.prototype.hasOwnProperty.call(obj,k)){o.push('"'+$JSON.escape(k)+'":'+$JSON.toString(obj[k]))
}}return"{"+o.join(",")+"}"}}default:}}};M.URI={reFolderExt:/[^\/]*$/,reProtocol:/^\w+:/,reDataURI:/^data:/,abs:function(uri){if(!M.URI.reProtocol.test(uri)){if(uri.indexOf("/")==0){uri=M._loc.protocol+"//"+M._loc.hostname+uri
}else{if(uri.indexOf(".")==0){uri=M._loc.protocol+"//"+M._loc.hostname+M._loc.pathname.replace(M.URI.reProtocol,uri)
}else{uri=M.URI.folder(M._loc.href)+uri}}}return uri},parse:function(uri){if(undefined===uri||typeof(uri)!="string"){return""
}var host=M._loc.protocol+"//"+M._loc.hostname,base=host+M._loc.pathname.replace(M.URI.reFolderExt,uri);
var a=doc.createElement("a");a.setAttribute("href",M.URI.abs(uri));return a},isExternalRes:function(uri){if(undefined===uri||typeof(uri)!="string"){return false
}return 0==uri.indexOf("https:")||0==uri.indexOf("http:")||0==uri.indexOf("file:")
},path:function(uri){if(undefined===uri||typeof(uri)!="string"){return""}var idx=uri.indexOf(";jsessionid=");
if(idx>=0){return uri.substr(0,idx)}if(uri.indexOf("/min/?")>=0){return uri}do{idx=uri.indexOf("?",idx);
if(idx<0){break}if("?"==uri.charAt(idx+1)){idx+=2}else{break}}while(idx>=0);return idx<0?uri:uri.substr(0,idx)
},folder:function(uri){if(!uri){return""}var idx=uri.lastIndexOf("/");return idx<0?"":uri.substr(0,idx+1)
}};function identify(){var b=doc.cookie+navigator.userAgent+navigator.plugins.length+Math.random(),n=0,rand=""+Math.random();
for(var i=0,l=b.length;i<l;i++){n+=i*b.charCodeAt(i)}return n.toString(parseInt(Math.random()*10+16))
}var UNKNOW_INFO={name:"",version:[]};M.client=!!window.light?light.client.info:{os:UNKNOW_INFO,browser:UNKNOW_INFO,device:UNKNOW_INFO,engine:UNKNOW_INFO};
var clientInfo={dev:M.client.device.name+"/"+M.client.device.version.join("."),os:M.client.os.name+"/"+M.client.os.version.join("."),scr:screen.width+"x"+screen.height+"x"+screen.colorDepth,bro:M.client.browser.name+"/"+M.client.browser.version.join("."),eng:M.client.engine.name+"/"+M.client.engine.version.join(".")};
var URLLength=!!M.client.engine.trident?2083:8190;var servName=doc.getElementById("ServerNum");
servName=(servName?servName.innerHTML:"").split("-");servName=servName[0]||M._loc.hostname;
var DATA={url:M.url,ref:doc.referrer,sys:servName,client:clientInfo};function send(url,data,callback){if(!callback){callback=function(){}
}if(!data){callback();return}var d=encodeURIComponent(data);var url=url+(url.indexOf("?")<0?"?":"&")+d;
try{var img=new Image(1,1);function clearImage(){clearTimeout(timer);timer=null;if(!img.aborted){callback();
img.aborted=true}img.onload=img.onerror=img.onabort=null;img=null}img.onload=clearImage;
img.onerror=clearImage;img.onabort=clearImage;img.src=url;var timer=window.setTimeout(function(){try{img.src=null;
img.aborted=true;clearImage()}catch(ex){}},M.timeout)}catch(ex){}}function part(datas,len){var datas=datas.slice(0),list=[[]],idx=0;
while(datas.length>0){if(encodeURIComponent($JSON.toString(list[idx].concat(datas[0]))).length<len){list[idx].push(datas.shift())
}else{list[++idx]=[];list[idx].push(datas.shift())}}return list}M.report=function(data){if(!data){return
}if(data.hasOwnProperty("htmlError")){var list=part(data.htmlError,URLLength-encodeURIComponent($JSON.toString(DATA)).length-150);
for(var i=0,l=list.length;i<l;i++){M._errors.push({htmlError:list[i]})}}else{M._errors.push(data)
}M.timedSend()};function jsLoader(src){if(M.nocache){src+=(src.indexOf("?")>=0?"&":"?")+M.S.rand()
}var script=doc.createElement("script");script.setAttribute("type","text/javascript");
script.setAttribute("charset","utf-8");script.setAttribute("src",src);var hd=doc.getElementsByTagName("head");
if(hd&&hd.length>0){hd=hd[0]}hd=hd&&hd.length>0?hd[0]:doc.documentElement;hd.appendChild(script)
}function clone(o){var r;if(null==o){return null}switch(typeof o){case"string":case"number":case"boolean":r=o;
break;case"object":if(o instanceof Array){r=[];for(var i=o.length-1;i>=0;i--){r[i]=clone(o[i])
}}else{if(o instanceof RegExp){r=new RegExp(o.source,(o.ignoreCase?"i":"")+(o.global?"g":"")+(o.multiline?"m":""))
}else{if(o instanceof Date){r=new Date(o.valueOf())}else{if(o instanceof Error){o=r
}else{if(o instanceof Object){r={};for(var k in o){if(o.hasOwnProperty(k)){r[k]=clone(o[k])
}}}}}}}break;default:throw new Error("Not support the type.")}return r}function merge(t,o){for(var k in o){if(Object.prototype.hasOwnProperty.call(o,k)){t[k]=o[k]
}}return t}M.timedSend=function(){if(sendState==SEND_STATUS.SENDING){return}var e=M._errors.shift();
if(!e){sendState=SEND_STATUS.COMPLETE;return}sendState=SEND_STATUS.SENDING;var data=clone(DATA);
if(Object.prototype.hasOwnProperty.call(e,"jsError")){e.jsError.file=M.URI.path(e.jsError.file)
}data=merge(data,e);data.rnd=M.S.rand();try{send(LOG_SERVER,$JSON.toString(data),function(){sendState=SEND_STATUS.COMPLETE;
M.timedSend()})}catch(ex){}};window.setTimeout(function(){try{M._errors.push({pv:1,domready:readyTime,load:loadTime});
M.timedSend();if(M.client.engine.name=="trident"&&M.client.engine.version[0]<=7){return
}if(mode==MODE.LOCAL){for(var i=0,l=scriptB.length;i<l;i++){jsLoader(scriptBase+scriptB[i])
}}else{jsLoader(scriptBase+scriptB.join(","))}}catch(ex){}},M.delay)})();