(function(){var toStringProto=Object.prototype.toString;var toString=function(obj){return toStringProto.call(obj)};var boxedString=Object("a"),splitString=boxedString[0]!="a"||!(0 in boxedString);var toObject=function(o){if(o==null){throw new TypeError("can't convert "+o+" to object");}
return Object(o);};var ws="\x09\x0A\x0B\x0C\x0D\x20\xA0\u1680\u180E\u2000\u2001\u2002\u2003"+
"\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000\u2028"+
"\u2029\uFEFF";Object.keys=Object.keys||(function(){var hasOwnProperty=Object.prototype.hasOwnProperty,hasDontEnumBug=!{toString:null}.propertyIsEnumerable("toString"),DontEnums=['toString','toLocaleString','valueOf','hasOwnProperty','isPrototypeOf','propertyIsEnumerable','constructor'],DontEnumsLength=DontEnums.length;return function(o){if(typeof o!="object"&&typeof o!="function"||o===null){throw new TypeError("Object.keys called on a non-object");}
var result=[];for(var name in o){if(hasOwnProperty.call(o,name)){result.push(name);}}
if(hasDontEnumBug){for(var i=0;i<DontEnumsLength;i++){if(hasOwnProperty.call(o,DontEnums[i])){result.push(DontEnums[i]);}}}
return result;};})();Array.isArray=Array.isArray||function(obj){return toString(obj)=="[object Array]";};Array.prototype.forEach=Array.prototype.forEach||function forEach(fun){var object=toObject(this),self=splitString&&toString(this)=="[object String]"?this.split(""):object,thisp=arguments[1],i=-1,length=self.length>>>0;if(toString(fun)!="[object Function]"){throw new TypeError();}
while(++i<length){if(i in self){fun.call(thisp,self[i],i,object);}}};if(!String.prototype.trim||ws.trim()){ws="["+ws+"]";var trimBeginRegexp=new RegExp("^"+ws+ws+"*"),trimEndRegexp=new RegExp(ws+ws+"*$");String.prototype.trim=function trim(){if(this===void 0||this===null){throw new TypeError("can't convert "+this+" to object");}
return String(this)
.replace(trimBeginRegexp,"")
.replace(trimEndRegexp,"");};}}());Base64=(function(){var _PADCHAR="=",_ALPHA="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",_VERSION="1.0";var _getbyte64=function(s,i){var idx=_ALPHA.indexOf(s.charAt(i));if(idx===-1){throw"Cannot decode base64";}
return idx;}
var _decode=typeof window.atob==='function'?function(s){return window.atob(s);}:function(s){var pads=0,i,b10,imax=s.length,x=[];s=String(s);if(imax===0){return s;}
if(imax%4!==0){throw"Cannot decode base64";}
if(s.charAt(imax-1)===_PADCHAR){pads=1;if(s.charAt(imax-2)===_PADCHAR){pads=2;}
imax-=4;}
for(i=0;i<imax;i+=4){b10=(_getbyte64(s,i)<<18)|(_getbyte64(s,i+1)<<12)|(_getbyte64(s,i+2)<<6)|_getbyte64(s,i+3);x.push(String.fromCharCode(b10>>16,(b10>>8)&0xff,b10&0xff));}
switch(pads){case 1:b10=(_getbyte64(s,i)<<18)|(_getbyte64(s,i+1)<<12)|(_getbyte64(s,i+2)<<6);x.push(String.fromCharCode(b10>>16,(b10>>8)&0xff));break;case 2:b10=(_getbyte64(s,i)<<18)|(_getbyte64(s,i+1)<<12);x.push(String.fromCharCode(b10>>16));break;}
return x.join("");};var _getbyte=function(s,i){var x=s.charCodeAt(i);if(x>255){throw"INVALID_CHARACTER_ERR: DOM Exception 5";}
return x;}
var _encode=typeof window.btoa==='function'?function(s){return window.btoa(s);}:function(s){if(arguments.length!==1){throw"SyntaxError: exactly one argument required";}
s=String(s);var i,b10,x=[],imax=s.length-s.length%3;if(s.length===0){return s;}
for(i=0;i<imax;i+=3){b10=(_getbyte(s,i)<<16)|(_getbyte(s,i+1)<<8)|_getbyte(s,i+2);x.push(_ALPHA.charAt(b10>>18));x.push(_ALPHA.charAt((b10>>12)&0x3F));x.push(_ALPHA.charAt((b10>>6)&0x3f));x.push(_ALPHA.charAt(b10&0x3f));}
switch(s.length-imax){case 1:b10=_getbyte(s,i)<<16;x.push(_ALPHA.charAt(b10>>18)+_ALPHA.charAt((b10>>12)&0x3F)+_PADCHAR+_PADCHAR);break;case 2:b10=(_getbyte(s,i)<<16)|(_getbyte(s,i+1)<<8);x.push(_ALPHA.charAt(b10>>18)+_ALPHA.charAt((b10>>12)&0x3F)+_ALPHA.charAt((b10>>6)&0x3f)+_PADCHAR);break;}
return x.join("");};return{decode:_decode,encode:_encode,VERSION:_VERSION};}());(function($){var memo={};$.urlParam=function(key){var result;if(typeof memo[key]==='undefined'){result=new RegExp('[\?&]'+key+'=([^&]*)','i').exec(window.location.search);memo[key]=result&&result[1]||'';}
return memo[key];};})(jQuery);var CL={extend:function(namespace,obj){if(typeof namespace==='object'&&typeof obj==='undefined'){obj=namespace;namespace=[];}else if(typeof obj!=='object'){return;}else{namespace=namespace.split('.');}
var self=this;var objToExtend=self;var newNS;while(newNS=namespace.shift()){objToExtend[newNS]=objToExtend[newNS]||{};objToExtend=objToExtend[newNS];}
$.extend(true,objToExtend,obj);}};CL.extend({browser:{androidVersion:(function(){return navigator.userAgent.match(/Android (\d)\.(\d)\.(\d)/);}()),ieVersion:(function(){var v=3,div=document.createElement('div'),all=div.getElementsByTagName('i');while(div.innerHTML='<!--[if gt IE '+(++v)+']><i></i><![endif]-->',all[0]);return v>4?v:undefined;}()),touchCapable:'ontouchstart'in window,localStorageAvailable:(function(){try{var item='isLocalStorageAvailable';localStorage.setItem(item,'a');if(localStorage.getItem(item)!=='a'){throw new Error();}
localStorage.removeItem(item);return true;}catch(e){return false;}}())},swipe:{makeGallery:function($container){$container=$container||$('body');var total,current,$sliderNav=$container.find('.slidernav'),$sliderInfo=$sliderNav.find('.sliderinfo'),$back=$sliderNav.find('.back'),$forward=$sliderNav.find('.forward'),updateInfo=function(current,total){$sliderInfo.text(current+' of '+total);if(current==1){$back.prop('disabled',true);}
if(current==2){$back.prop('disabled',false);}
if(current==total){$forward.prop('disabled',true);}
if(current==(total-1)){$forward.prop('disabled',false);}};sw=new Swipe($('.swipe')[0],{continuous:false,callback:function(i,el){updateInfo(i+1,total);}});total=sw.getNumSlides();updateInfo(1,total);$back.on('touchstart mousedown',function(e){e.preventDefault();sw.prev();});$forward.on('touchstart mousedown',function(e){e.preventDefault();sw.next();});}},util:{isoDateString:function(d){function pad(n){return n<10?'0'+n:n}
return d.getUTCFullYear()+'-'
+pad(d.getUTCMonth()+1)+'-'
+pad(d.getUTCDate())+'T'
+pad(d.getUTCHours())+':'
+pad(d.getUTCMinutes())+':'
+pad(d.getUTCSeconds())+'Z';}}});var pagemode=(function(){return(document.cookie.match('(^|; )cl_fmt=([^;]*)')||0)[2];}());$(document).ready(function(){formats_autosize(pagetype);ie_tweaks();});function ie_tweaks(){if(CL.browser.ieVersion<=7){var $crumbs=$('.crumb');if($crumbs.length>1){$crumbs.length=$crumbs.length-1;$crumbs.append(' >');}}}
function get_domain(hostname){var m=((hostname||'')+'').match(/craigslist.[\w.]+$/);return m?m[0]:null;}
function formats_autosize(pagetype){if(pagemode==='mobile'){$('meta[name=viewport]').attr('content','width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0');var mobile_handler={homepage:homepage_size_mobile,toc:toc_size_mobile,posting:posting_size_mobile,post:post_size_mobile,simple:simple_size_mobile,sites:sites_size_mobile,account:account_size_mobile}[pagetype];if(typeof mobile_handler==="function"){mobile_handler();}
mobile_header();}
activate_format_selector();}
function issue_format_cookie_and_reload(format){var date=new Date();var domain=get_domain(document.location.hostname);date.setTime(date.getTime()+(365*24*60*60*1000));document.cookie='cl_fmt='+format+
'; domain='+domain+'; expires='+
date.toGMTString()+'; path=/';window.location.href=window.location.href;}
function activate_format_selector(target){var $obj=target?target:$('body');var $fmtsel=$('#fmtsel');var $footer=$obj.find('footer,#footer');if($footer.length!=0){$footer.before($fmtsel);}else{$obj.append($fmtsel);}
update_format_selector();$fmtsel.show();}
function update_format_selector(){$('#fmtsel .fsel').each(function(){if($(this).attr('data-mode')===pagemode){$(this).contents().wrap('<b>');}else{$(this).contents().wrap('<a href="#">');}}).click(function(e){e.preventDefault();var target_mode=$(this).attr('data-mode');if(target_mode!==pagemode){issue_format_cookie_and_reload(target_mode);}});}
function mobile_header(){var $header=$('.mobile').not('.post').find('header');var $contents=$header.find('.contents');var $breadcrumbs=$header.find('.breadcrumbs');var $breadcrumbLinks=$breadcrumbs.find('a');var $backButton=$header.find('.back');var closedHeight,openHeight;var isOpen=false;var headerActions=function(e){if(e.target===$backButton[0]){}else if(isOpen&&$breadcrumbLinks.filter(e.target).length){}else{e.preventDefault();if(typeof closedHeight==='undefined'){closedHeight=$contents.height();openHeight=$breadcrumbs.height();}
if(isOpen){$contents.height(closedHeight).removeClass('open').addClass('closed');}else{$contents.height(openHeight).addClass('open').removeClass('closed');}
isOpen=!isOpen;}};$contents.on('touchstart mousedown',headerActions);$backButton.on('click',function(e){window.history.go(-1);});$header.siblings().on('touchstart mousedown',function(e){if(isOpen){headerActions(e);}});var androidVersion=CL.browser.androidVersion;if(androidVersion&&androidVersion[1]==2){var timer;var $body=$('body');var bodyHeight=$body.height();$(window).on('scroll',function(){if(!timer){timer=setTimeout(function(){$body.height(bodyHeight++);timer=undefined;},300);}});}}
function build_sorted_cat_list(catabbr,catname){var catlist=[];$('#'+catabbr+'0>li>a,#'+catabbr+'1>li>a').each(function(){catlist.push($(this).html());});catlist=catlist.sort();var excludePattern=/^\[/;var catlist=new Object;$('#center #'+catabbr+' li').each(function(){var html=$(this).find('a').html();if(!html.match(excludePattern)){catlist[html]=$(this);}});var s=Object.keys(catlist).sort();for(var i=s.length-1;i>=0;i--){catlist[s[i]].prependTo('#'+catabbr+'0');}
$('#'+catabbr+'1').remove();$('#'+catabbr+'0 li').first().clone().prependTo('#'+catabbr+'0');var allHref=$('#'+catabbr+' .ban a').attr('href');$('#'+catabbr+'0 a').first().attr('href',allHref).html('all '+catname);$('#'+catabbr+' .ban').click(function(e){e.preventDefault();$('#'+catabbr+' .cats').slideToggle();});}
function homepage_size_mobile(){$('#topban').prependTo('#pagecontainer').removeClass('ban');$.map(['center','rightbar'],function(v){$("#"+v).contents().unwrap().wrapAll('<div id="'+v+'"></div>');$("#"+v).prependTo('.body');});$('.sublinks').prependTo('.body').hide();$('#topban h2').prepend('CL &gt; ').click(function(){$('#rightbar,.sublinks').slideToggle();});$('#search>div:first').remove();$('#search').insertAfter('#rightbar');$('#postlks').insertAfter('#search');$.map(['sss','jjj','hhh','ppp','ccc','bbb','ggg'],function(v){$("#"+v).contents().unwrap().wrapAll('<div id="'+v+'"></div>');$("#"+v).insertBefore('#main');});build_sorted_cat_list('sss','for sale');build_sorted_cat_list('jjj','jobs');build_sorted_cat_list('hhh','housing');build_sorted_cat_list('ppp','personals');$('#ppp0>li').first().remove();build_sorted_cat_list('ccc','community');build_sorted_cat_list('bbb','services');build_sorted_cat_list('ggg','gigs');$('#res,#forums,#calban,.cal').each(function(){$(this).appendTo('#center').removeClass('col');});$('#center').append('<br>');$('.cal').appendTo('#center');$('#calban').addClass('ban').click(function(e){e.preventDefault();$('.cal').slideToggle();});$('.leftlinks').appendTo('#center');$('#center').show();$('#main,#container,#leftbar').remove();}
function toc_size_mobile(){$(window).bind('orientationchange',function(){$('#pagecontainer').css('width','100%');});build_toc_searchform();build_toc_results();}
function toc_orientation_flip(){if(window.innerWidth>window.innerHeight){$('body').removeClass('portrait');$('#tocright').appendTo('#pagecontainer');}else{$('body').addClass('portrait');$('#tocleft').appendTo('#pagecontainer');}}
function build_toc_searchform(){var $searchfieldset=$('#searchfieldset');var $query=$('#query').attr('size','');var $satabs=$('#satabs');var $expsearch;$searchfieldset
.find(':submit')
.remove()
.end()
.append('<button id="topsubmit" type="submit">&gt;</button>')
.append($('<div class="leftside"></div>')
.append('<div class="expando"><button id="expsrch" type="button">+</button></div>')
.append($('<div class="searchbox"></div>').append($query)));$expsearch=$('#expsrch');$expsearch.on('click',function(e){$('#searchdrop').slideToggle();$expsearch.html($expsearch.html()==='+'?'&ndash;':'+');});$('#searchtable')
.wrap('<div id="searchdrop" />')
.find('td:first-child')
.remove();if($satabs.length){$satabs
.removeClass('tabcontainer')
.prependTo($('#searchdrop'))
.find('b')
.prependTo($satabs)
.end().end()
.find('>*')
.not(':first')
.wrapAll('<div class="tog" />')
.end().end()
.show()
.filter(':first')
.on('click',function(e){e.preventDefault();$satabs.find('.tog').slideToggle();});}
$searchfieldset.show();}
function build_toc_results(){$('#toc_rows').find('.row')
.find('.gc').text(function(i,text){return text;}).end()
.on('click',function(e){var href=$(this).find('.pl').find('a').attr('href');if(href){window.location.href=href;}});}
function posting_size_mobile(){$('.cltags').before($('#attributes'));$('.flags').insertAfter('.postinginfos');$('date')
.attr('title',function(i,dt){var isoDate='';var rawDate=new Date(+dt);if(dt&&!isNaN(rawDate.getTime())){isoDate=CL.util.isoDateString(rawDate);}
return isoDate;})
.timeago()
.on('click',function(){var temp,$this=$(this);temp=$this.text();$this.text($this.attr('title'));$this.attr('title',temp);});var $thumbs=$('#thumbs a');var $figure=$('figure.iw');var sliderHtml='';var sw;if($thumbs.length>1){sliderHtml+='<div class="slidernav"><button class="back" disabled="disabled">&lt;</button>'+
'<span class="sliderinfo"></span><button class="forward">&gt;</button></div>'+
'<div class="swipe"><div class="swipe-wrap">';for(var i=0,len=imgList.length;i<len;i++){sliderHtml+='<div><img src="'+imgList[i]+'" /></div>';}
sliderHtml+='</div></div>';$figure.html(sliderHtml);CL.swipe.makeGallery($figure);}}
function post_size_mobile(){$('header aside.highlight').appendTo('.post > header section.contents');$('header > br:last,#accountBlurb br').remove();$('.managestatus a').prepend('<br>');$('.managestatus form').prepend('<br>');$('.managestatus table td').wrap('<tr />');$('blockquote>i').each(function(){$(this).find('sup').each(function(){$(this).replaceWith($(this).html());});$(this).prev('label').append('<br>');$(this).appendTo($(this).prev('label'));});if($('form table').attr('summary')==='neighborhood picker'){$('form table td:last').prependTo('form table td blockquote');}
if($('form table').attr('summary')==='flava picka'){$('form table td fieldset').last().appendTo($('form table td:first'));$('form table td:first').append('<br>');$('form table td:last').children().appendTo($('form table td:first'));}
if($('textarea.toutext').attr('cols')==='80'){$('textarea').attr('cols',null).css('width','100%');$('table form').append('<br><br>').appendTo('body');}
if($('form').first().attr('id')==='postingForm'){$('input[size=80]').css('width','100%').attr('size',null);$('input[size=30]').css('width','100%').attr('size',null);$('input[size=20]').css('width','100%').attr('size',null);}
$('.userbody').append('<div width="100%" class="imagehole"></div>');$('.iw').remove();var imagesDiv=$('.imagehole');$('.tn a').each(function(){var imgHref=$(this).attr('href');imagesDiv.append('<a href="'+imgHref+'"><img class="postingimg" width="100%" src="'+imgHref+'"></a><br>');});}
function simple_size_mobile(){$('body').addClass('mobile');if($('table:first').css('width')==='706px'){$('td').each(function(){$(this).children().appendTo($('form'));});}
if($('table:first').css('width')==='500px'){$('td').each(function(){$(this).append('<br/>').children().prependTo('#content>div:first');});}
return false;}
function sites_size_mobile(){$('.box').children().unwrap();$('h1,h4').click(function(e){var menu=$(this).next('ul,.colmask');menu.slideToggle();$(this).parent().children('ul:visible,,colmask:visible').not(menu).slideUp();});}
function account_size_mobile(){$('body').removeClass('toc').addClass('mobile');$('.bchead').appendTo('body');$('.bchead>#ef>a:first').appendTo('.bchead>#satabs');$('.bchead>#satabs').append(' ');$('.bchead>#ef>a:first').appendTo('.bchead>#satabs');$('.bchead>#ef').remove();$('.bchead>#satabs').appendTo('body');$('blockquote>br').remove();$('blockquote').children().appendTo('body');$('form').each(function(){$(this).find('table td').children().appendTo($(this).find('table td:first'));});$('select').before('<br>');$('#paginator>table>tbody>tr').first().remove();$('#paginator>table>tbody>tr').each(function(){var newDiv=$('<div class="postingrow"></div>');var posttitle=$(this).find('.title');newDiv.append(posttitle.html())
.append($(this).find('.areacat').html())
.append('&bull;')
.append($(this).find('.dates').html())
.append('<br>')
.append($(this).find('.status').html());newDiv.css({'background':posttitle.css('background'),'border':posttitle.css('border'),'font-size':posttitle.css('font-size'),'font-family':posttitle.css('font-family')});$('#paginator').append(newDiv);newDiv.click(function(e){e.preventDefault;window.location.href=posttitle.find('a').attr('href');});});$('#paginator>table').remove();$('#paginator>.postingrow').appendTo('body');$('#paginator').clone().appendTo('body');$('p>em').appendTo('body');$('#footer').appendTo('body');return false;}
