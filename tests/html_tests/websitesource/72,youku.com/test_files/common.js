//hack console error
(function(){
	if(window['console']){ return; }
	window['console'] = {
		 log: function(){}
		,clear: function(){}
		,debug: function(){}
		,error: function(){}
		,info: function(){}
		,count: function(){}
		,time: function(){}
		,trace: function(){}
		,warn: function(){}
	}
})();

//page load time for log
var logPageStartTime = (new Date()).getTime();//毫秒
window.uepflag = 1;//uep统计发送开关

if((window.location.href.indexOf("www.youku.com") !== -1 || window.location.href.indexOf("v.youku.com") !== -1) && Math.round(Math.random()*1000) > 100) {
	window.uepflag = 0;//抽样发送
}

window["nova_init_hook_logpagetime"] = function(){
	var logPageEndTime = (new Date()).getTime();//毫秒
	var loadTime = logPageEndTime - window.logPageStartTime;
	if(loadTime <= 0 || isNaN(loadTime)) {
		loadTime = -1;
		var err = 1001;
	}
	if(loadTime >= 100 * 1000 || isNaN(loadTime)){
		loadTime = -1;
		var err = 1002;
	}
	var logParam = "pid="+window.logPvid+"&m=web&st="+window.logPageStartTime+"&s="+loadTime+"&et="+logPageEndTime+"&u="+encodeURIComponent(window.location.href);
	if(err){
		logParam += "&e="+err;
	}
	var type1 = '';
	var pagetype1 = '';
	if(new RegExp('^http:\/\/www.youku.com(\/)?$').test(window.location.href)){
		if(window.uepflag == 0) return false;
		type1 = 'homepage';//index
		pagetype1 = 1;
	}else if(new RegExp('^http:\/\/v.youku.com/v_show/.*$').test(window.location.href)){
		if(window.uepflag == 0) return false;//抽样发送
		type1 = 'playerpage';//video play page
		pagetype1 = 2;
		var vid = window.videoId || 0;
		var ct = window.cateStr || '';
		var cs = window.subCatId || '';
		logParam += "&ct="+ct+"&cs="+cs+"&v="+vid;
	}else if(new RegExp('^http:\/\/i.youku.com/u/home.*$').test(window.location.href)){
		type1 = 'usercenter';//user center page
		pagetype1 = 4;
		var ct = window.cateStr || '';
		logParam += "&ct="+ct;
	}else if(!new RegExp('^http:\/\/(www)|(v)|(i).youku.com/.*$').test(window.location.href)){
		type1 = 'channelpage';//channel
		pagetype1 = 3;
		var ct = window.cateStr || '';
		logParam += "&ct="+ct;
	}else{
		type1 = 'channelpage';//channel
		pagetype1 = 3;
		var ct = window.cateStr || '';
		logParam += "&ct="+ct;
	}
	logParam += "&t="+type1+"&p="+pagetype1+"&"+Math.random();
	var img = new Image();
	img.src = "http://v.l.youku.com/uep?"+logParam;
};


//ucenter APT
(function(o){
	
	if(!o || o.QUcenter){ return; }
	
	var winsub = null;
	var uidsub = 0;//记录当前请求订阅的uid
	var handle_fake = null;//定位参考点
	o.QUcenter = {
		bindFollow: function(handle, uid, callback, config){
			if(!arguments[0] || !arguments[1]){ return this; }
			var callback = typeof(arguments[2]) == 'function' ?  callback : null;
			var config_default = { showwin: true }; 
			var config = arguments[3] ? arguments[3] : config_default;
			config.showwin = typeof(config.showwin) != 'undefined' ? Boolean(config.showwin) : config_default.showwin;
			var _this = this;
			if(document.domain != 'youku.com'){ document.domain = 'youku.com'; }
			handle.observe('click', function(e){
				if(typeof(window['passport'] == 'undefined')){ window.passport = '1'; }
				if(!window['islogin'] && !window['Qwindow']	){ return; }
				if(!islogin()){ login(); return; }
				uidsub = uid;
				if(handle.getAttribute('lock')){ return; }//防止重复点击锁定
				handle.setAttribute('lock', 'true');
				_this.follow(uid, function(rs){
					handle.removeAttribute('lock');
					if(config.showwin){ _this.showFollowWin(handle, rs, uid); }
					if(callback){
						if(winsub && uidsub == uid){
							winsub.setHideCallback(function(){
								callback(handle, rs, uid);
								winsub.setHideCallback(null);
							});
						}else{
							callback(handle, rs, uid);
						}
					}
				}); 
			});
			return this;
		},
		follow: function(uid, callback){
			if(document.domain != 'youku.com'){ document.domain = 'youku.com'; }
			var funcname = 'jsoncallback' + new Date().getTime();
			this[funcname] = function(rs){
				if(typeof(callback) == 'function'){
					callback(rs);
				}
				delete QUcenter[funcname];
			}
			var url = this.getUrlBase() + '/u/subToUpdates?follow=' + uid+ '&callback=QUcenter.'+ funcname;
			jsonp(url);
			
			return this;
		},
		unfollow: function(uid, callback){
			if(document.domain != 'youku.com'){ document.domain = 'youku.com'; }
			var funcname = 'jsoncallback' + new Date().getTime();
			this[funcname] = function(rs){
				if(typeof(callback) == 'function'){
					callback(rs);
				}
				delete QUcenter[funcname];
			}
			var url = this.getUrlBase() + '/u/subToUpdates?unfollow=' + uid + '&callback=QUcenter.'+ funcname;
			jsonp(url);
			
			return this;
		},
		showFollowWin: function(handle, rs, uid){
			if(!window['Qwindow'] || !handle){ return this; }
			var w = 360, h = 280, bw=10, poswin = {top: -bw, left: -bw};
			if(!winsub){ 
				var params = {
					size: {width: w, height: h}
					,posrefer: handle
					,pos: poswin
					,showmask: false
					,elements: ''
				}
				winsub = new Qwindow(params);
			}else{
				winsub.hide();
			}
					
			var lr = 'left';
			var pos = this._getOffsetPos(handle)
				,range = {
					'width'	: handle.offsetWidth,
					'height': handle.offsetHeight,
					'top'	: pos.top,
					'left'	: pos.left	
				}
				, edge = this._getWindowREdge() 
			
			if((w + bw*2) + range.left > edge){
				var l = range.width - (w + bw)
				poswin = {top: -bw, left: l }	
			}
			
			winsub.setPosrefer(handle).setPos(poswin.top, poswin.left);
			if(!uidsub || uid == uidsub){//异步返回填充正确的窗口
				var url = this.getUrlBase() + '/u/showSubResult?rs=' + rs + '&follow=' + uid;
				winsub.setContent('iframe', url).show();
			}
			return this;
		},
		hideFollowWin: function(){
			if(!winsub){ return; }
			winsub.hide();
			return this;
		},
		getUrlBase: function(){
			var domain = window['UC_DOMAIN'];
			if(!domain){ domain = 'i.youku.com'; }
			return 'http://' + domain;
		},
		_getOffsetPos: function(o) { 
			if (o.getBoundingClientRect) {
				var x=0, y=0;
				try{
					var box = o.getBoundingClientRect();
					var D = document.documentElement;
					x = box.left + Math.max(D.scrollLeft, document.body.scrollLeft) - D.clientLeft;
					y = box.top + Math.max(D.scrollTop, document.body.scrollTop) - D.clientTop;
				}catch(e){}
				return {'top': y, 'left': x};
			}else{
				function pageX(o){ try {return o.offsetParent ? o.offsetLeft +  pageX(o.offsetParent) : o.offsetLeft; } catch(e){ return 0; } }
				function pageY(o){ try {return o.offsetParent ? o.offsetTop + pageY(o.offsetParent) : o.offsetTop; } catch(e){ return 0; } }
				return {'top': pageY(o), 'left': pageX(o)};
			} 
		},
		//获取当前窗口右边缘x坐标
		_getWindowREdge: function(){
			var l = document.documentElement ? document.documentElement.scrollLeft + document.body.scrollLeft : document.body.scrollLeft;
			var w = document.documentElement ? document.documentElement.clientWidth : document.body.clientWidth;
			return l + w;
		}
	}
	
	
	var jsonp = function(src, callback){
		if(typeof(arguments[0]) != 'string'){ return; }
		var callback = typeof(arguments[1]) == 'function' ? callback : function(){};
		var head = document.getElementsByTagName('HEAD')[0];
		var script = document.createElement('SCRIPT');
		script.type = 'text/javascript'; 
		script.src = src;
		head.appendChild(script);
		if(!/*@cc_on!@*/0) {
			script.onload = function(){ callback(); this.parentNode.removeChild(this); }
		}else{
			script.onreadystatechange = function () {
				if (this.readyState == 'loaded' || this.readyState == 'complete') { 
					callback();
					this.parentNode.removeChild(this);
				}
			}
		}
	}	
	
})(window);

/*paycenter API*/
(function(o){
	
	if(!o){ return; }
	if(o.QFee){ return; }	
	window['nova_init_hook_Fee_buyAutobind'] = function(){ o.Fee.buyAutobind(); }
	
	o.QFee = {
		
		buy: function(query, hzclick){
			if(!query){ return; }
			if(document.domain != 'youku.com'){ document.domain = 'youku.com'; }
			if(typeof(window['passport'] == 'undefined')){ window.passport = '1'; }
			if(!window['islogin'] &&!window['Qwindow']	){ return; }
			
			var baseurl = 'http://pay.youku.com/buy/redirect.html';
			var url = arguments[1] 
					? hzclick + baseurl + '?' + query 
					: baseurl + '?' + query;
			window.location = url;
			/**		
			if(islogin()){ window.location = url; }
			else{ 
				login({type: 'fee',callBack: function(){ window.location = url; }}); 
			}

			return this;**/
		},
		
		buyAutobind: function(){
			var flag_query = 'buy_query';
			var flag_hzclick = 'buy_hzclick';
			
			var handles = $$('['+ flag_query +']');
			var _this = this;
			for(var i=0, len=handles.length; i<handles.length; i++){
				handles[i].observe('click', function(evt){
					var query = this.getAttribute(flag_query);
					var hzclick = this.getAttribute(flag_hzclick);
					if(query){
						if(!hzclick){ _this.buy(query); }
						else{ _this.buy(query, hzclick) }
					}
				});
			}
			
			return this;
		}
				
	}
	 
})(window);


function ltrim(s){ return s.replace( /^(\s*|　*)/, ""); } 
function rtrim(s){ return s.replace( /(\s*|　*)$/, ""); } 
function trim(s){ return ltrim(rtrim(s));} 
/**
 * 判断变量是否空值
 * undefined, null, '', false, 0, [], {} 均返回true，否则返回false
 */
function empty(v){
    switch (typeof v){
        case 'undefined' : return true;
        case 'string'    : if(trim(v).length == 0) return true; break;
        case 'boolean'   : if(!v) return true; break;
        case 'number'    : if(0 === v) return true; break;
        case 'object'    : 
            if(null === v) return true;
            if(undefined !== v.length && v.length==0) return true;
            for(var k in v){return false;} return true;
            break;
    }
    return false;
}

//check domain begin
var dURL = window.location.href.toLowerCase();
if(dURL.indexOf("youku") <0 && dURL.indexOf("yoqoo")<0 && dURL.indexOf("soku")<0){
	var path = document.location.pathname
	window.location.href = 'http://www.youku.com'+path;
}
//check domain end

// JavaScript Document
function MM_goToURL() { //v3.0
  var i, args=MM_goToURL.arguments; document.MM_returnValue = false;
  for (i=0; i<(args.length-1); i+=2) eval(args[i]+".location='"+args[i+1]+"'");
}
function MM_findObj(n, d) { //v4.01
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}
function MM_changeProp(objName,x,theProp,theValue) { //v6.0
  var obj = MM_findObj(objName);
  if (obj && (theProp.indexOf("style.")==-1 || obj.style)){
    if (theValue == true || theValue == false)
      eval("obj."+theProp+"="+theValue);
    else eval("obj."+theProp+"='"+theValue+"'");
  }
}
function U8_16(_1) {
	var i, len, c;
	var char2, char3;
	var ary = [];
	len = _1.length;
	i = 0;
	while (i < len) {
		c = _1.charCodeAt(i++);
		switch (c >> 4) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
			// 0xxxxxxx
			ary.push(_1.charAt(i - 1));
			break;
		case 12:
		case 13:
			// 110x xxxx   10xx xxxx
			char2 = _1.charCodeAt(i++);
			ary.push(String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F)));
			break;
		case 14:
			// 1110 xxxx 10xx xxxx 10xx xxxx
			char2 = _1.charCodeAt(i++);
			char3 = _1.charCodeAt(i++);
			ary.push(String.fromCharCode(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)));
			break;
		}
	}
	return ary.join('');
}
function decode64(_1) {
	if(!_1) return '';
	var _2 = "ABCDEFGHIJKLMNOP"+"QRSTUVWXYZabcdef"+"ghijklmnopqrstuv"+"wxyz0123456789+/"+"=";
	var _3 = "";
	var _4, _5, _6;
	var _7, _8, _9, _a;
	var i = 0;
	_1 = _1.replace(/[^A-Za-z0-9\+\/\=]/g, "");
	do {
		_7 = _2.indexOf(_1.charAt(i++));
		_8 = _2.indexOf(_1.charAt(i++));
		_9 = _2.indexOf(_1.charAt(i++));
		_a = _2.indexOf(_1.charAt(i++));
		_4 = (_7 << 2) | (_8 >> 4);
		_5 = ((_8 & 15) << 4) | (_9 >> 2);
		_6 = ((_9 & 3) << 6) | _a;
		_3 = _3 + String.fromCharCode(_4);
		if (_9 != 64) {
			_3 = _3 + String.fromCharCode(_5);
		}
		if (_a != 64) {
			_3 = _3 + String.fromCharCode(_6);
		}
	} while (i < _1.length);
	return U8_16(_3);
}
function encode64(str)
{
	if(!str) return '';
	str = str.toString();
	var base64EncodeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	var base64DecodeChars = new Array(
       -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
       52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
       -1, 0,   1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
       15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
       -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
       41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
	);
	var out, i, len;
	var c1, c2, c3;
    len = str.length;
    i = 0;
    out = "";
	while(i < len) {
		c1 = str.charCodeAt(i++) & 0xff;
		if(i == len)
		{
		    out += base64EncodeChars.charAt(c1 >> 2);
		    out += base64EncodeChars.charAt((c1 & 0x3) << 4);
		    out += "==";
		    break;
		}
		c2 = str.charCodeAt(i++);
		if(i == len)
		{
		    out += base64EncodeChars.charAt(c1 >> 2);
		    out += base64EncodeChars.charAt(((c1 & 0x3)<< 4) | ((c2 & 0xF0) >> 4));
		    out += base64EncodeChars.charAt((c2 & 0xF) << 2);
		    out += "=";
		    break;
		}
		c3 = str.charCodeAt(i++);
		out += base64EncodeChars.charAt(c1 >> 2);
		out += base64EncodeChars.charAt(((c1 & 0x3)<< 4) | ((c2 & 0xF0) >> 4));
		out += base64EncodeChars.charAt(((c2 & 0xF) << 2) | ((c3 & 0xC0) >>6));
		out += base64EncodeChars.charAt(c3 & 0x3F);
	}
	return out;
}
function encodeUid(uid)
{
	if(!uid) return '';
	var enUid = 'U' + encode64(uid << 2);
	return enUid;
}

//for search
function dosearch(f){
	if(trim( f.q.value.replace(/[\/_]/g,' ') )==''){
		location.href='http://www.soku.com/?inner';
		return false;
	}
	var q = encodeURIComponent(f.q.value.replace(/[\/_]/g,' '));//
	
	if(f.socondition && f.socondition[1].id=='outer' && f.socondition[1].checked){//全网搜索
		var url="http://www.soku.com/v?keyword="+q;
	}else{//站内搜索
		var innersearchdomain = f.searchdomain.value;
		if(!innersearchdomain)innersearchdomain="http://www.soku.com";
		var btype = f.sbts;//看吧搜索选项
		if(f.searchType.value == "bar" && btype != undefined && btype.value != ""){
			q = q+"_sbt_"+btype.value;
		}
		var url= innersearchdomain+"/search_"+f.searchType.value+"/q_"+q;
	}
	if(typeof(search_prompt_flag) != 'undefined' && search_prompt_flag){//使用下拉提示统计代码
		(new Image()).src="http://lstat.youku.com/sokuHintKeyword.php?keyword="+q;
	}
	
	location.href=url;
	return false;
	
}
function search_show(pos,searchType,href){
    document.getElementById(pos+"SearchType").value=searchType;
    document.getElementById(pos+"Sel").style.display="none";
    document.getElementById(pos+"Slected").innerHTML=href.innerHTML;
    document.getElementById(pos+'q').focus();
    
    var s2 = document.getElementById('soswitch');
	var sl = document.getElementById('sorelated');
    var s0 = document.getElementById("searchextend0");
    if(s0 != undefined && searchType == "bar" && pos=="head"){
    	s0.style.display="block";
		if(sl) sl.style.display = 'none';
		if(s2) s2.style.display="none";
    }else if(s0 != undefined && pos=="head"){
    	s0.style.display="none";
		if(sl) sl.style.display = '';
		if(s2) s2.style.display = '';
    }
    var s1 = document.getElementById("searchextend1");
    if(s1 != undefined && (searchType == "video" || searchType == "playlist") && pos=="head"){
    	s1.style.display="block";
		if(sl) sl.style.display = 'none';
    }else if(s1 != undefined && pos=="head"){
    	s1.style.display="none";
		if(sl) sl.style.display = '';
    }
    
    var s2 = document.getElementById("searchextend2");
    if(s1 != undefined && searchType == "user" && pos=="head"){
    	s2.style.display="block";
		if(sl) sl.style.display = 'none';
    }else if(s1 != undefined && pos=="head"){
    	s2.style.display="none";
		if(sl) sl.style.display = '';
    }
    
	try{window.clearTimeout(timer);}catch(e){}
	return false;
}
function csbt(sbt,sbts){
	if(sbt.value == sbts.value){
		sbt.checked = false;
		sbts.value='bar';
	}else{
		sbts.value=sbt.value;
	}
}
function advancedsearch(){
	var type=document.getElementById("headSearchType").value;
	if(type!=="video" && type!="playlist")type="video";
	var searchdomain = document.getElementById("searchdomain").value;
	if(!searchdomain)searchdomain="http://www.soku.com";
	var url=searchdomain+"/search_advanced"+type;
	if(document.getElementById("headq").value!=''){
		url+="/q_"+encodeURIComponent(document.getElementById("headq").value);
	}
	window.location=url;
	
}
function drop_mouseover(pos){
	try{window.clearTimeout(timer);}catch(e){}
}
function drop_mouseout(pos){
	var posSel=$(pos+"Sel").style.display;
	if(posSel=="block"){
		timer = setTimeout("drop_hide('"+pos+"')", 1000);
	}
}
function drop_hide(pos){
	$(pos+"Sel").style.display="none";
}

window.nova_init_hook_initsearch = function() {
	var fullPath = document.location.pathname.replace('_','/');
	var path = fullPath.split('/');
	var module='index';
	if(path[1]){module=path[1];}
	
    var search=module;
	if(path[1] && path[1].indexOf('_')>0){
		search =  path[1].split("_")[1];
	}else if(path[2] && path[2].indexOf('_')>0){
		search =  path[2];
	}else if(path[2] && module=="search"){
		search =  path[2];
	}
    switch (module) {
    	case 'search':
		    module=search;
		    break;
        case 'my':
			search = "user";
            if('bar'==path[2]) search = "bar";
            else if('friend'==path[2]) search = "user";
            else if('playlist'==path[2]) search = "playlist";
            break;
		case 'user':
			search = "user";
			if('v'==path[2]||'video'==path[2]) search = 'video';
			else if('friend'==path[2]) search = "user";
			else if('fav'==path[2]) search = "video";
			else if('playlist'==path[2]) search = "playlist";
			else if('bar'==path[2]) search = "bar";
			break;
		case 'v':
			search = "video";
            if('playlist'==path[2]) search = "playlist";
			break;
    }
    try{
    	initsearch(search);
    }catch(e){};
    loadAds(module);
}

function initsearch(m){
	if(m!="video" && m!="playlist" && m!="user" && m!="bar" ){return true;}
	var names={"video":"视频","playlist":"专辑","user":"空间","bar":"看吧"};
	$("headSearchType").value=m;
	$("headSlected").innerHTML=names[m];
	$("footSearchType").value=m;
	$("footSlected").innerHTML=names[m];
	return true;
}

//for search end

//回答问题???
function q_answer(){
	    if($('answer').value.length==0){
	    	alert("答案不能为空");
	    	return;
	    }
		new Ajax.Request(
         "/user/resetpwd/step/2/",
         {method: 'post',
          parameters: Form.serialize('form_resetpwd2'),
          onSuccess: function(o){
          	if(o.responseText=='0'){
			alert('你的生日不对吧？');
          	}else{
					$('resetpwd_content').innerHTML='<p >&nbsp;</p>\
					    <p >&nbsp;</p>\
					        <p>请在你注册的邮箱里，按提示修改你的密码</p>';

          	};
          }
          }
	    );
}
//找回密码提交???
function change_bg(obj,x,theProp,theValue) {
  if (obj && (theProp.indexOf("style.")==-1 || obj.style)){
    if (theValue == true || theValue == false)
      eval("obj."+theProp+"="+theValue);
    else eval("obj."+theProp+"='"+theValue+"'");
  }
}
function loadAds(module) {
    /* Google */
    if(typeof VERSION =='undefined' || VERSION==null){VERSION="";}
	Nova.addScript("http://urchin.lstat.youku.com"+VERSION+"/index/js/urchin.js");
    setTimeout("run_google();", 500);
}
function run_google() {
    if (!window.urchinTracker) {
        setTimeout(run_google, 500);
        return;
    }
    _uacct = "UA-455269-3";
    var trackerParam = (typeof cateStr == 'undefined') ? false : cateStr;
    urchinTracker(trackerParam);
}
function checkLogin(func) {
       var args = new Array();
       if(arguments.length>1){
               args = Array.prototype.slice.call(arguments);
       }
       if(!islogin()){
               if(func && func!=''){
                       login.apply(login,args);
               }else{
                       login();
               }
       }else{
               if(func && func!=''){
                       if(args.length>0){
                               args.shift(); // 去掉arguments[0] (func)
                       }
                       if(typeof func == 'string'){
                               func = eval(func);
                       }
                       if(typeof func == 'function'){
                               func.apply(func,args);
                       }
               }
       }
}
function MM_openBrWindow(theURL,winName,features) { //v2.0
	window.open(theURL,winName,features);
}
function sendVideoLink(videoId){
       var url = 'http://www.youku.com/contact/sendlink?obj=v&id='+videoId;
       checkLogin(MM_openBrWindow,url,'','scrollbars=yes,width=695,height=540,resizable=yes');
}
function sendPlayListLink(plid){
       var url = 'http://www.youku.com/contact/sendlink?obj=playlist&id='+plid;
       checkLogin(MM_openBrWindow,url,'','scrollbars=yes,width=695,height=540,resizable=yes');
} 
function isEmail(mail){
	return(new RegExp(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z]{1,4}$/).test(mail)); 
} 
// add by Peak for TJCNC
window.nova_init_hook_partner = function() {
	var partner = Nova.Cookie.get("PARTNER");
	if(partner == "tjcnc"){
		var allA = document.getElementsByTagName("a");
		for(var i=0;i<allA.length;i++){
			if(allA[i].target != "" && allA[i].target != "_self"){
				allA[i].target = "_self";
			}
		}
	}
}





var pop=null;
var login_callback_user = null;
var login_callback_user_args = new Array();
var popwin = null;

/**
 * 登录小窗口
 */
function login(callBack){
	if( passport=='1' ){
		//add UI destroy, 但未对window赋值null
		if(popwin==null || (popwin && popwin.isDestroy)) popwin = new Qwindow();
		var isFee = (typeof callBack!='undefined' && typeof callBack.type!='undefined' && typeof callBack.callBack!='undefined') ? true : false;
		var url = typeof passport_url=='undefined' ? '' : passport_url;
		popwin.setContent('iframe', url + '/index_login/'+(isFee ? ('?from='+callBack.type) : '')).setSize(600, 410).show();
	}else{
		if(pop!=null) pop.close();
		pop = new Popup({contentType:1,isSupportDraging:false,isReloadOnClose:false,width:540,height:300});
		pop.setContents({'title':'登录','contentUrl':'/index_login/'});
		pop.build().show();
	}
	login_callback_user = isFee ? callBack.callBack : callBack;
	
	// 参数
	if(arguments.length>1){
		login_callback_user_args = Array.prototype.slice.call(arguments);
		if(login_callback_user_args.length>0){
			login_callback_user_args.shift(); // 去掉arguments[0] (callBack)
		}
	}
}

/**
 * 找回密码
 */
function findpwd(cp){
	if(pop!=null) pop.close();
	var popUrl = '/index_findpwd';
	if (typeof(cp) == 'number') {
		var img = new Image();
		img.src = 'http://hz.youku.com/red/click.php?tp=1&cp='+cp+'&cpp=1000427&url=';
	}
	pop=new Popup({contentType:1,isSupportDraging:false,isReloadOnClose:false,width:540,height:300});
	pop.setContents({"title":"忘记密码",'contentUrl':popUrl});
	pop.build().show();
}

openLoginDialog=login;


/**
 * 取消登录并返回当前页
 */
var default_logout_callback = function(result){if(result)top.location.reload()};
var logout = function(callback){
	callback = callback || "default_logout_callback";
	if(empty(youku_homeurl)) youku_homeurl = 'u.youku.com';
	nova_call('http://'+youku_homeurl+'/QUser/~ajax/logout', '', callback, undefined, 1);
}

/**
 * 根据cookie信息判断用户是否登录
 */
var islogin = function(){
	var username = '';
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf('u=') == 0 || c.indexOf('k=') == 0) var _c = c;
		if(c.indexOf('yktk=') == 0){
			var u_info = decode64(decodeURIComponent(c).split("|")[3]);
			if(u_info.indexOf(",") > -1 && u_info.indexOf("nn:") > -1 && u_info.indexOf("id:") > -1){
				 var username = u_info.split(",")[1].split(":")[1];
				 if(username != '') break;
			}
		}
	}
	if(username == ''){
		if(_c){
			var username = _c.substring(2,_c.length);
			if(username == '__LOGOUT__') username = '';
		}
	}
	return (username == '') ? false : true; 
}
/**
 *获取用户名,返回decodeURIComponent过的username
 */
var get_username = function(){
	var username = '';
	var ca = document.cookie.split(';');
	var len = arguments.length;
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf('u=') == 0 || c.indexOf('k=') == 0) var _c = c;
		if (c.indexOf('_l_lgi=') == 0) var _l = c;
		if(c.indexOf('yktk=') == 0){
			var u_info = decode64(decodeURIComponent(c).split("|")[3]);
			if(u_info.indexOf(",") > -1 && u_info.indexOf("nn:") > -1 && u_info.indexOf("id:") > -1){
				 var username = u_info.split(",")[1].split(":")[1];
				 var userid = u_info.split(",")[0].split(":")[1];
				 if(username != '') break;
			}
		}
	}
	if(username == '' || userid == ''){
		if(_c){
			var username = _c.substring(2,_c.length);
			if(username == '__LOGOUT__') username = '';
			else username = decodeURIComponent(username);
		}
		if(_l){
			var userid = _l.substring(7,_l.length);
		}
	}
	if(len == 0){
		return (username == '') ? '' : username; 
	}else if(len == 1 && arguments[0] == 'all'){
		return {'username':username,'userid':userid};
	}
}
/**
 * Feed pubshli tips
 */
var FeedPublishTip = function(feedtype){
	if(!empty(pop)) pop.close();
	setTimeout(function(){
		pop=new Popup({contentType:1,isSupportDraging:false,isReloadOnClose:false,width:248,height:130,isBackgroundCanClick:true});
		pop.setContent("title","同步到合作方网站");
		pop.setContent("contentUrl", "/partner_feedtips.html?type="+encodeURIComponent(feedtype));
		pop.build().show();
		document.domain = 'youku.com';
	},1000);
}
/**
 * 发送站内消息
 */
var mailto = function(username){
    if(!islogin()) {
        login(mailto.bind(this, username));return;
    }
    window.open('/my_mail/type_mini_receive_'+encodeURIComponent(username),'','location=no,scrollbars=no,width=410,height=320');
}
/**
 * 增加好友
 */
function addFriend(friendId,module) {
    if(!islogin()){
        login(addFriend.bind(this, friendId, module));return;
    }
	var url = '/my_friend/type_addFriend';
    if(!empty(module)){
    	if(module=="bar"){
    		url = '/bar_friend/type_addFriend';
    	}else if(module=="search"){
    		url = '/search_friend/type_addFriend';
    	}
    }
	nova_request(function(result) {
			switch(result) {
			case 'error':
				alert('页面载入错误，请刷新本页后重试！');
				break;
			case 'self':
				alert('不能将自己加为好友！');
				break;
            case 'already':
                alert('当前用户已经是您的好友了！');
                break;
			default:
				if($('groupDialog'))
					$('groupDialog').parentNode.removeChild($('groupDialog'));
                window.__addFriendFromModule = module;
				var dialog = document.createElement('div');
				dialog.innerHTML = result;
				dialog = dialog.firstChild;
				while(dialog.nodeType == 3)
					dialog = dialog.nextSibling;
                var winWidth;
                var winHeight;
                var scrollTop;
                //获取窗口宽度
                if (window.innerWidth)
                    winWidth = window.innerWidth;
                else if ((document.body) && (document.body.clientWidth))
                    winWidth = document.body.clientWidth;
                //获取窗口高度
                if (window.innerHeight)
                    winHeight = window.innerHeight;
                else if ((document.documentElement) && (document.documentElement.clientHeight))
                    winHeight = document.documentElement.clientHeight;
                if(document.documentElement.scrollTop)
                    scrollTop = document.documentElement.scrollTop;
                else if((document.body) && (document.body.scrollTop))
                    scrollTop = document.body.scrollTop;
				document.body.appendChild(dialog);
				if(!scrollTop) scrollTop = 0;
                $('groupDialog').style.top = winHeight/2 - $('groupDialog').clientHeight/2 + scrollTop + 'px';
                $('groupDialog').style.left = winWidth/2 - $('groupDialog').clientWidth/2 + 'px';
				$('groupDialog').style.zIndex = 100000;
				break;
			}
		}.bind(module), url, {'friendId': friendId}, 'get');
}

function _addFriend(event, friendId, groupId) {
	if(!event) event = window.event;
	Event.stop(event);
    var url = '/QMy/~ajax/addFriend';
	if(typeof window.__addFriendFromModule == 'undefined') var module = '';
	else module = window.__addFriendFromModule;
    if(!empty(module)){
    	if(module=="bar"){
    		url = '/QBar/~ajax/addFriend';
    	}
    }
    var AJAXAddFriend = function(param, callback, id) { return nova_call(url, param, callback, id); }
    var __call = function(result){
		if($('groupDialog'))
			$('groupDialog').parentNode.removeChild($('groupDialog'));
        switch(result){
            case 'ok'        : alert('添加好友成功'); return;
            case 'validate'  : alert('该用户已设置好友验证，已经成功发送好友请求！'); return;
            case 'unallowed' : alert('该用户已设置不允许任何人加他为好友！'); return;
            case 'self'      : alert('不能将自己加为好友！'); return;
            case 'already'   : alert('当前用户已经是你的好友了！');return;
            case false       : alert('增加好友失败，请与系统管理员联系！'); return;
            default          : alert('未知的返回值类型，请与系统管理员联系！'+result);
        }
    }
    AJAXAddFriend({'friendId':friendId, 'groupId': groupId},__call);
}
/**
 * 登录浮动层回调函数
 */
function login_callback(){
	// 用户自定义回调函数
	if(login_callback_user && login_callback_user != 'mynull'){
		if(typeof login_callback_user == 'string'){
			login_callback_user = eval(login_callback_user);
		}
		if(typeof login_callback_user == 'function'){
			login_callback_user.apply(login_callback_user,login_callback_user_args);
		}
	}

	// 更新登录状态
	update_login_status();
}

/**
 * 更新用户状态：
 *
 */
function update_login_status(){
	for(var key in window){
		if(typeof window[key]=='function' && key.indexOf('update_login_status_hook_')!=-1){
			var o = window[key];
			o();
		}
	}	
}

window.nova_init_hook_main_login_status = update_login_status;
//{{{subscribe function

var subscribe_obj=null;
function subscribe_callback(result){
	if(!subscribe_obj){return;}
	var obj = (subscribe_obj.subtype == '0') ? '用户' : (subscribe_obj.subtype == '1' ? '标签' : '专辑');
	switch (result){
		case 'ok2'	 : FeedPublishTip('收藏' + obj);
        case 'ok'    : alert('已收藏！'); return;
	    case 'error' :
	        alert('数据库操作异常！');return;
	    case -1 :
	        alert('参数校验失败！');return;
	    case 1 :
	        alert('您收藏的'+obj+'不存在！');return;
	    case 2 :
	        var msg = subscribe_obj.subtype == '0' ? '不能收藏自己！' : '不能收藏自己的专辑！';
	        alert(msg);return;
	    case 3 :
	        alert('该'+obj+'已经收藏！',null,false,true);return;
	    case 4 :
	        alert('标签含有禁忌词不能被收藏！');return;
	    default :
	        alert('未知的错误类型！');
	}
}
function subscribe(subtype,target){
    if(!islogin()){
        login(subscribe.bind(this, subtype, target));return;
    }
	subscribe_obj = {'subtype':subtype,'target':encodeURIComponent(target)};
	if(empty(udomain)) udomain = 'u.youku.com';
	var url = 'http://'+udomain+'/QMy/~ajax/addSubscribe';
    nova_call(url, subscribe_obj , "subscribe_callback", undefined,1); 
}
//}}}
/**
 * 订阅用户
 */
var subscribeUser = function(userName){
		subscribe(0,userName);
}

var popChannelListTimer = null;
/* pop miniHeader channelList */
function mhPopChannelList(listId, event, clearTimerOnly) {
	try{window.clearTimeout(popChannelListTimer);}catch(e){};

	if(Position.within($(listId), Event.pointerX(event), Event.pointerY(event))) return;
	if(typeof clearTimerOnly == undefined || !clearTimerOnly)
		popChannelListTimer = window.setTimeout('_mhPopChannelList("'+listId+'")', 700);
}
function _mhPopChannelList(listId) {
	var elList = $(listId);
	if(elList && elList.style.display != 'block')
		elList.style.display = 'block';
	else
		elList.style.display = 'none';
}
//{{{IKU-194
var ikuagent;
function getIkuAgent(){
	if (!ikuagent && navigator.userAgent.indexOf('MSIE') != -1){
		if (window.ActiveXObject){
			try{
				ikuagent = new ActiveXObject("iKuAgent.KuAgent2");
			}catch(ex){}
		}
	}
	return ikuagent;
}
function getP2PState(){
		if((iku = getIkuAgent())!=undefined){
			return iku.GetP2PPort();
		}return "";
}
function getIkuId(){
		if((iku = getIkuAgent())!=undefined){
			return ikuagent.Youku_Hao;
		}return "";
}
getP2PState();
//}}}

if (!window.Log) 
    window.Log={}; 
Log.logCookieKey="logCookieKey";
Log._addScript=function(src) {
	var g = document.createElement("script");
	g.type = "text/javascript";
	g.src = src;
	document.getElementsByTagName('head')[0].appendChild(g);
};
Log.addScript=function(src)//此方法解析数字代表的url
{ var reg1=new RegExp("@"+1+"@","g"); //创建正则RegExp对象    
  var reg2=new RegExp("@"+2+"@","g");
  var reg3=new RegExp("@"+3+"@","g");
  var reg4=new RegExp("@"+4+"@","g");
  var reg5=new RegExp("@"+5+"@","g");
  src=src.replace(reg1,"http://hz.youku.com/red/click.php").replace(reg2,"http://hz.youku.com/red/relatedVideoClick.php").replace(reg3,"http://lstat.youku.com/nstay.php").replace(reg4,"http://e.stat.youku.com/hot/click").replace(reg5," http://e.stat.youku.com/recommond/log");
  src+="&="+Math.floor(Math.random()*1000000);
	Log._addScript(src);
}
Log.log=function(category,args,sendType)
{  if(typeof category!="number")
	return;
	var type=sendType?sendType:0//请求处理的方式,0为cookie,1为直接发送请求,默认为0  

	var url="";
	if(type==0)
	{
		var strCookie=document.cookie;
		var arrCookie=strCookie.split("; ");
		for(var i=0;i<arrCookie.length;i++){ 
			var arr=arrCookie[i].split("="); 

			if(Log.logCookieKey==arr[0]){ 
				if(arr[1]!='invalid')

					url=unescape(arrCookie[i].substring(Log.logCookieKey.length+1,arrCookie[i].length));

				break; 
			} 

		} 
	}     
	url+="@"+category+"@";


	if(typeof args=='object')
	{  argUrl="";
		for (arg in args)
		{ 
			argUrl+="&"+arg+"="+args[arg]
		}
		if(category==4)
		{
			document.cookie="__utmarea="+args.charset+";path=/;domain=youku.com";

		}

		url+="?"+argUrl.substring(1,argUrl.length)+"^";}
	else
	{
		if(category==4)
		{
			document.cookie="__utmarea="+args.substring(8,args.length)+";path=/;domain=youku.com";

		}

		url+="?"+args+"^";

	}
	if(type==0)
	{
		document.cookie=Log.logCookieKey+"="+escape(url)+";path=/;domain=youku.com"; 

	}


	else
	{
		Log.addScript(url);  //直接发送的代码 
	}
}
Log.readLogCookie=function()
{
	var strCookie=document.cookie;
	var arrCookie=strCookie.split("; "); 
	var url=""; 

	found=0;
	for(var i=0;i<arrCookie.length;i++){ 
		var arr=arrCookie[i].split("="); 

		if(Log.logCookieKey==arr[0]){ 
			found=1;
			if(arr[1]=='invalid')
			{ 
				break;
			}


			url=unescape(arrCookie[i].substring(Log.logCookieKey.length+1,arrCookie[i].length));

			requestUrl=url.substring(0,url.length-1).split("^");
			for(var i=0;i<requestUrl.length;i++)
			{
				Log.addScript(requestUrl[i]);
			}


			document.cookie=Log.logCookieKey+"=invalid"+";path=/;domain=youku.com";
			break; 
		} 

	} 




}

var logUnCookie=0;
var logFrame=0;
var logpvid="-";
var logExt = '';
if (!navigator.cookieEnabled)
{
		   logUnCookie=1;
}
if (top.location != self.location){
		   logFrame=1;
}
var getPvid = function(len){
		var randchar=["0","1","2","3","4","5","6","7","8","9",
			"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
			"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
					];
		var i=0;
		var r="";
		var d=new Date();
		for (i=0;i<len;i++){
				         var index=parseInt(Math.random()*Math.pow(10,6))%randchar.length;
						          r+=randchar[index];
		}
		return d.getTime()+r;
}
logPvid=getPvid(3);

if(navigator.userAgent.indexOf("Android") !== -1) logExt = "mtype=adr";
else if(navigator.userAgent.indexOf("iPad") !== -1) logExt = "mtype=ipa";
else if(navigator.userAgent.indexOf("iPhone") !== -1) logExt = "mtype=iph";
else if(navigator.userAgent.indexOf("iPod") !== -1) logExt = "mtype=ipo";
else logExt = "mtype=oth";
