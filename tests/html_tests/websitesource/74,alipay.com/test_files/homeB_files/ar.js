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
}};var advanced=!!storage.defaultStorage;light.module("client/storage",storage)}(window,light);this.Tracker||function(c){var p=c.document,q=c.location,j=document.URL||"",h=c.performance,k=c.light,b,r,t=q.protocol,z=t+"//kcart.alipay.com/web/bi.do",A=t+"//kcart.alipay.com/web/1.do",B=t+"//log.mmstat.com/5.gif";h&&h.timing?r=h.timing.navigationStart:c._to&&_to.start&&(r=_to.start.getTime());c.Tracker=b=function(){};b.prototype={watch:function(){b.click("tracker-watch")}};b.extend=function(a){for(var b=1,d=arguments.length;b<d;b++)for(var c in arguments[b])arguments[b].hasOwnProperty(c)&&(a[c]=
arguments[b][c]);return a};b.version="1.0";b.enabled=!0;b.debug=!1;b.seedName="seed";b.minInterval=1E3;for(var u,n=p.getElementsByTagName("meta"),o=0,v,f=n.length;o<f;o++)if((v=n[o].getAttribute("name"))&&"abtest"==v.toLowerCase()){u=n[o].getAttribute("content");break}var w=function(a){var b=new Image(1,1),d="_img_"+Math.random();window[d]=b;b.onload=b.onerror=b.onabort=function(){var a=window[d];a.onload=null;a.onerror=null;a.onabort=null;window[d]=null};b.src=a};b.dispatchEvent=function(a,b,d){a.attachEvent?
a.attachEvent("on"+b,function(b){d.call(a,b)}):a.addEventListener?a.addEventListener(b,d,!1):a["on"+b]=function(b){d.call(a,b)}};b.getTarget=function(a){a=a.target||a.srcElement;try{if(a&&3===a.nodeType)return a.parentNode}catch(b){}return a};b.send=function(a,c,d){if("string"!==typeof a||!a)throw Error("Invalid page");a={ref:c||"-",pg:a||"",r:(new Date).getTime(),v:b.version};u&&(a.ABTest=u,a.pg+=(0<=a.pg.indexOf("?")?"&":"?")+"ABTest="+u);d&&k.extend(a,d);a=k.param(a);"file:"!=t&&!b.debug?(d=z+
"?"+a,w(d),/\bcna=/.test(document.cookie)||(a=k.param({url:A+"?"+a}),d=B+"?"+a,w(d))):k.log("Tracker debug: %s.",a)};if(p&&q){var n=p.referrer,x=Math.random(),e={screen:"-x-",color:"-",BIProfile:"page"};window.parent!=window&&(e.BIProfile="iframe");c.screen&&(e.screen=screen.width+"x"+screen.height,e.sc=screen.colorDepth+"-bit");e.utmhn=q.hostname;e.rnd=x;c.analytic_var&&(e.ana=analytic_var);k.client&&(f=k.client.info,q=(f.os.name||"na")+"/"+(f.os.version||[-1]).join("."),o=(f.browser.name||"na")+
"/"+(f.browser.version||[-1]).join("."),v=(f.engine.name||"na")+"/"+(f.engine.version||[-1]).join("."),f=(f.device.name||"na")+"/"+(f.device.version||[-1]).join("."),e._clnt=q+"|"+v+"|"+o+"|"+f);b.send(j,n,e);n=j;e={};if(!parseInt(8*Math.random(),10)){e.BIProfile="load";var i=0,s=0,l=function(){if(!l.invoked){l.invoked=true;i=(c._to&&_to.ready?_to.ready.getTime():(new Date).getTime())-r;if(i>2E4){m.invoked=true;e.tm="-x-";b.send(j,"",e)}}},m=function(){if(!(i>2E4||m.invoked)){m.invoked=true;s=(c._to&&
_to.end?_to.end.getTime():(new Date).getTime())-r;if(h&&h.timing){i=h.timing.domContentLoadedEventStart-h.timing.navigationStart;s=h.timing.loadEventStart-h.timing.navigationStart}y()}},y=function(){if(i){i>s&&(i=s-100);if(!(i<10)){e.tm=""+i+"x"+s;e.rnd=x;b.send(j,"",e)}}else setTimeout(y,50)};if(r){if(c._to&&_to.ready)l();else if(c.YAHOO&&YAHOO.util&&YAHOO.util.Event)YAHOO.util.Event.onDOMReady(l);else if(c.jQuery)jQuery(l);else if(c.Y&&Y.on)Y.on("domready",l);else l();c._to&&_to.end?m():(c.setTimeout(m,
16E4),b.dispatchEvent(c,"load",m),b.dispatchEvent(c,"unload",m))}}}p&&b.dispatchEvent(p,"mousedown",function(a){if((a=b.getTarget(a))&&a.nodeType){for(;a&&"HTML"!=a.nodeName&&a.getAttribute&&!a.getAttribute(b.seedName);)a=a.parentNode;if(a&&!(1!==a.nodeType||"HTML"==a.nodeName)){var c,d;if("A"===a.nodeName){d=a.getAttribute("href",2)||"";if(d===j||0===d.indexOf(j+"#"))d="";(d=d.match(/[?&]_scType=([^&#]+)/))&&(c={_scType:d[1]})}b.click(a.getAttribute(b.seedName),c)}}});b.click=function(){var a={},
c=j.split("?").shift(),d=c.indexOf(";jsessionid=");0<=d&&(c=c.substr(0,d));a.clk=j;return function(d,e){if(d){var g=d.split(":",2);2<=g.length||g.unshift("clk");var f=g[0];if(d=g[1]){g=[];e&&g.push(k.param(e));g.push("seed="+encodeURIComponent(d));var g=g.join("&"),g=c+"?"+g,h;f&&(h={BIProfile:f});b.send(g,a[f]||"",h);a[f]=g}}}}();b.log=function(a,c){b.click((c||"syslog")+":"+a)};b.error=function(a){b.click("syserr:"+a)};b.calc=function(a,c){b.click("calc:"+a,{value:c})}}(this);