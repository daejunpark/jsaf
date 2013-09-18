/**
 * 广告技术前端常用脚本库
 * @since 2013.02.05
 * @author iAMS Team
 *
 * 公用对象
 * 对象内需严格遵守以下定义：
 * s String
 * o Object
 * f Function
 * e Event
 */
if(typeof(INice) == 'undefined') {
	var INice = {};
}
INice = {
	version : '0.7',

	/**
	 * 多事件兼容函数
	 * @param Object o 对象, eg. window
	 * @param Event e 事件名称,eg. load
	 * @param Function f 要执行的方法名, eg. toShow
	 */
	attachEvent : function(o, e, f){
		if(o.addEventListener) {
			return o.addEventListener(e, f, false);
		}
		if(o.attachEvent) {
			return o.attachEvent('on' + e, f);
		}
		return o['on' + e] = f;
	},

	/**
	 * 去头尾空格
	 * @param String s
	 */
	trim : function(s) {
		s = s.replace(/(^\s*)|(\s*$)/g, '');
		s = s.replace(/(^　*)|(　*$)/g, '');
		return s;
	},

	/**
	 * 截取文件后缀名,返回文件后缀名小写形式  ，如'swf','jpg'
	 * @param String s
	 */
	ext : function(s) {
		var pairs;
		var file;

		var s = s.replace(/(\\+)/g, '#');

		pairs = s.split('#');
		file = pairs[pairs.length - 1];

		pairs = file.split('.');
		ext = pairs[pairs.length - 1];

		return ext.toLowerCase();
	} ,

	/**
	 * 写cookie,设置一个规定时间到期的cookie
	 * @param String name
	 * @param mixed value
	 * @param Integer expire
	 */
	setCookie : function(name, value, expire) {
		var date = new Date();
		expire = new Date(date.getTime() + expire * 60000);
		document.cookie = name + '=' + escape(value) + ';path=/;expires=' + expire.toGMTString() + ';'
	},

	/**
	 * 取cookie操作函数,返回指定cookie名称的值
	 * @param String name
	 */
	getCookie : function(name) {
		var value = '';
		var part;

		var pairs = document.cookie.split('; ');
		for(var i = 0; i < pairs.length; i ++) {
			part = pairs[i].split('=');
			if(part[0] == name){
				value = unescape(part[1]);
			}
		}
		return value;
	},


	/**
	 * 获取浏览器类型
	 */
	getBrowser : function() {
		 var browser = {};
		 var userAgent = navigator.userAgent.toLowerCase();

		 browser.IE = /msie/.test(userAgent);
		 browser.OPERA = /opera/.test(userAgent);
		 browser.MOZ = /gecko/.test(userAgent);
		 browser.IE6 = /msie 6/.test(userAgent);
		 browser.IE7 = /msie 7/.test(userAgent);
		 browser.IE8 = /msie 8/.test(userAgent);
		 browser.IE9 = browser.IE && !browser.IE6 && !browser.IE7 && !browser.IE8;
		 browser.SAFARI = /safari/.test(userAgent);
		 browser.CHROME = /chrome/.test(userAgent);
		 browser.IPHONE = /iphone os/.test(userAgent);
		 browser.MAXTHON = /maxthon/.test(userAgent);
		 browser.IPAD = /ipad/.test(userAgent);
		 browser.IPHONE = /iphone/.test(userAgent);
		 if (!browser.IPAD && browser.IPHONE) browser.IPAD = true;

		 return browser;
	},

	/**
	 * 获取网页已经滚动的高度
	 */
	getScrollTop : function() {
		return document.documentElement.scrollTop || window.pageYOffset || document.body.scrollTop;
	},

	/**
	 * 获取浏览器窗口网页区域的宽
	 */
	getWindowWidth : function() {
		return document.documentElement.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth;
	},

	/**
	 * 获取浏览器窗口网页区域的高
	 */
	getWindowHeight : function() {
		return document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight;
	},

	/**
	 * 加载javascript
	 * @param String source
	 * @param String callback
	 * @param String identifier
	 */
	loadScript : function(source, callback, identifier) {
		var element = document.createElement('script');

		if(typeof(source) == 'undefined' || source == '') {
			return false;
		}
		element.setAttribute('src', source);

		if(typeof(identifier) != 'undefined') {
			element.setAttribute('id', identifier);
		}

		element.onload = element.onreadystatechange = function() {
			if(!this.readyState || this.readyState === 'loaded' || this.readyState === 'complete') {
				if(typeof(callback) == 'function') {
					callback();
				}
				if(typeof(callback) == 'string') {
					eval(callback)();
				}
				element.onload = element.onreadystatechange = null;
			}
		};
		document.getElementsByTagName('head')[0].appendChild(element);
		return true;
	},

	/**
	 * 合并对象
	 */
	merge : function(source, target) {
		if(!source) {
			return {};
		}
		if(!target) {
			return source;
		}
		var o = {};
		for(var i in source) {
			o[i] = typeof(target[i]) == 'undefined' ? source[i] : target[i];
		}
		return o;
	}
};

var iNiceBrowser = INice.getBrowser();

/**
 * 轮播控制器脚本
 */
if(typeof(AdRotator) == 'undefined') {
	var AdRotator = function(){};
}
AdRotator = function(RotatorConfig) {
	(function() {
		var identifier = RotatorConfig.identifier;
		var maxTimes = RotatorConfig.maxTimes;
		var cookieFlag = 'ifengRotator_'+ RotatorConfig.identifier;

		var adContent = '';
		var wrapper = document.getElementById(identifier);
		var elements = wrapper.getElementsByTagName('code');
		var fixtures = wrapper.getElementsByTagName('cite');
		var current = INice.getCookie(cookieFlag);

		if(typeof(current) === 'undefined' || current === '') {
			current = parseInt(Math.random() * 100000) % maxTimes;
		}
		INice.setCookie(cookieFlag, ((parseInt(current) + 1) % maxTimes), 60);

		var element;
		//取广告代码
		if(typeof(elements[current]) != 'undefined') {
			element = elements[current];
			adContent = element.innerHTML;
		}

		//如果取不到，取铺底代码
		if(adContent == '') {
			if(typeof(fixtures[0]) != 'undefined') {
				element = fixtures[0];
				adContent = element.innerHTML;
			}
		}
		adContent = adContent.replace('<!--BOF', '');
		adContent = adContent.replace('EOF-->', '');
		document.write(adContent);
	})();
};

/**
 *flash 播放器生成类
 */
INice.Flash = function(settings) {
	this.settings = INice.merge({ url : '', width : 300, height : 225, id : '' }, settings);
	this.params = {};
	this.variables = {};
	this.flashvars = '';
	this.addParam = function(name, value) {
		this.params[name] = value;
	};

	this.addVariable = function(name, value) {
		this.variables[name] = value;
	};

	this.getVariables = function() {
		var a = [], v = this.variables;
		for(var i in v) {
			a.push(i + '=' + v[i]);
		}
		return a.join('&');
	};

	this.getParamString = function(isIE) {
		var a = [], p = this.params;
		if(isIE) {
			for(var i in p) {
				a.push('<param name="' + i + '" value="' + p[i] + '">');
			}
		}else {
			for(var i in p) {
				a.push(i + "=" + p[i] + " ");
			}
		}
		return a.join("");
	};

	this.addFlashVars = function(str) {
		this.flashvars = str;
	};

	//与flash程序进行交互调用
	this.callExternal = function(movieName, method, param, mathodCallback) {
		var fo = navigator.appName.indexOf("Microsoft") != -1 ? window[movieName] : document[movieName];
		fo[method](param, mathodCallback);
	};

	this.play = function() {
		var flashVersion = this.getVersion();
		if(!(parseInt(flashVersion[0] + flashVersion[1] + flashVersion[2]) > 901)) {
			return '<a style="display:block;height:31px;width:165px;line-height:31px;font-size:12px;text-decoration:none;text-align:center;margin:10px auto;border:2px outset #999;" href="http://get.adobe.com/flashplayer/" target="_blank">请安装最新Flash播放器</a>';
		}
		var f = [];
		if(!!window.ActiveXObject) {
			f.push('<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0"');
			f.push(' width="' + this.settings.width + '"');
			f.push(' height="' + this.settings.height + '"');
			f.push(' id="' + this.settings.id + '">');
			f.push('<param name="movie" value="' + this.settings.url + '">');
			f.push('<param name="flashvars" value="' + !this.flashvars ? this.getVariables() : this.flashvars + '">');
			f.push(this.getParamString(true));
			f.push("</object>");
		} else {
			f.push('<embed pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash"');
			f.push(' src="' + this.settings.url + '"');
			f.push(' height="' + this.settings.height + '"');
			f.push(' width="' + this.settings.width + '"');
			f.push(' flashvars="' + !this.flashvars ? this.getVariables() : this.flashvars + '" ');
			f.push(this.getParamString(false));
			f.push(">");
		}
		return f.join("");
	};

	this.getVersion = function() {
		var flashVersion = [ 0, 0, 0 ];
		if(navigator.plugins && navigator.mimeTypes.length) {
			var plugins = navigator.plugins["Shockwave Flash"];
			if(plugins && plugins.description) {
				return plugins.description.replace(/^\D+/, "").replace(/\s*r/, ".").replace(/\s*[a-z]+\d*/, ".0").split(".");
			}
		}
		if(navigator.userAgent && navigator.userAgent.indexOf("Windows CE") != -1) {
			var o = 1, version = 3;
			while(o) {
				try {
					o = new ActiveXObject("ShockwaveFlash.ShockwaveFlash." + (++version));
					return [ version, 0, 0 ];
				} catch(d) {
					o = null;
				}
			}
		}

		try {
			var o = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.7");
		} catch(d) {
			try {
				var o = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.6");
				flashVersion = [ 6, 0, 21 ];
				o.AllowScriptAccess = "always";
			} catch(d) {
				if(flashVersion.major == 6) return flashVersion;
			}
			try {
				o = new ActiveXObject("ShockwaveFlash.ShockwaveFlash");
			} catch(d) {
			}
		}
		if(o) {
			flashVersion = o.GetVariable("$version").split(" ")[1].split(",");
		}
		return flashVersion;
	};

};

// 视频广告播放器全局函数
if (typeof(returnProvToAdPlayer) == 'undefined') {
    var returnProvToAdPlayer,returnCityToAdPlayer,returnColumnToAdPlayer,returnLocationHref,clickToClient;
    returnColumnToAdPlayer = function (){
        var ret = '';
        var path = document.location.pathname;
		if(path.length > 1){
			path = path.replace(/^\//,"");
			var position = path.indexOf('/');
			if(position > 0){
				ret = path.substring(0 , position);
			}
		}
		// 神十文章页判断
		if (window.location.href.indexOf('http://news.ifeng.com/mainland/special/shenzhoushihao/') >= 0) {
		    ret = 'shenzhoushihao';
        }
		return ret;
    };

    returnProvToAdPlayer = function() {
    	return typeof(regionOrientProv) != 'undefined' ? regionOrientProv : "";
    };
    returnCityToAdPlayer = function() {
    	return typeof(regionOrientCity) != 'undefined' ? regionOrientCity : "";
    };
    returnLocationHref = function() {
    	return window.location.href;
    };
    clickToClient = function(url) {
        if (typeof(url) != 'undefined' && url != '') {
            window.open(url);
        }
        return 1;
    };
}
// 同步加载地域定向js
document.write('<scr'+'ipt type="text/javascript" src="http://h2.ifengimg.com/ifeng/sources/region_v1.js"><'+'/script>');
//end

/**
 * 广告位数据采集前端
 *  url 当前页面地址
 *  ap 广告位ID集合
 *  ar 地域信息
 *  cu 用户ID cookie userid
 * @version 0.1.4 2013.02.28
 * @author Sundj
 */
(function() {
	//统计地址
	var URL = 'http://stadig.ifeng.com/apstat.js?';
	//广告位ID集合
	var adplacementIds = [];

	/**
	 * 构造参数
	 * @returns
	 */
	function buildQuery() {
		var queryString = '';
		var province = '';
		var city = '';
		var userId = '';
		var documentUrl = encodeURIComponent(document.URL);

		try {
			province = INice.getCookie('prov');
			city = INice.getCookie('city');
			userId = INice.getCookie('userid');
		} catch(e) {
			//
		}
		queryString += 'url='+ documentUrl;
		queryString += '&ap='+ adplacementIds.join('|');
		queryString += '&ar='+ province;	//目前只取到省
		queryString += '&cu='+ userId;
		return queryString;
	}

	var aptracker = {
		add : function(adplacementId) {
			adplacementIds.push(adplacementId);
			return this;
		},

		get : function() {
			return URL + buildQuery();
		},

		request : function() {
			if(adplacementIds.length == 0) {
				return false;
			}
			var queryString = buildQuery();
			var image = new Image();
			image.src = URL + queryString;
		}
	};
	return window.aptracker = aptracker;
})();
