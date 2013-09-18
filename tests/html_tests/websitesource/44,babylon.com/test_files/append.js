
//append header html & footer html to the body
var append = (function () {
	if ( !window.babylon ) {
	
		//load scripts
		var loadScripts = function () {
		
				if ( !$('[href*="/style.css"]') ) {
					var loadCss = document.createElement("link");
					loadCss.href = "http://img.babylon.com/site/images/babylon10/style.css";
					loadCss.setAttribute("rel" ,"stylesheet");
					document.getElementsByTagName('head')[0].appendChild(loadCss);
				}
				
				if ( !$('[href*="/fonts.css"]') ) {
					var loadFonts = document.createElement("link");
					loadFonts.href = "/fonts.css";
					loadFonts.setAttribute("rel" ,"stylesheet");
					document.getElementsByTagName('head')[0].appendChild(loadFonts);
				}
				
				if ( !$.browser.msie ) {
					var loadScripth = document.createElement("script");
					loadScripth.src = "http://img.babylon.com/site/images/babylon10/history.js";
					document.getElementsByTagName('head')[0].appendChild(loadScripth);
				}
				
		};
		window.onload = loadScripts;
		return true	
	}
})();

var loadCSShp = function (css) {
	var loadCSS = document.createElement("link");
	loadCSS.href = css;
	loadCSS.setAttribute("class", "removePageCss");
	loadCSS.setAttribute("rel", "stylesheet");
	document.getElementsByTagName('head')[0].appendChild(loadCSS);
	
	function checkIfjQuery() {
		if (!window.jQuery)
			setTimeout(function () { checkIfjQuery() }, 50);
		else
			resumeLoad();
	}
	
	checkIfjQuery();
			
	function resumeLoad () {
	
		if ( $('.divWithoutJavascript .wrap_footer').html() != undefined ) {
			function removeFootr (){
				$('.divWithoutJavascript').remove();
			}
			function recheckFunction() {
				var oneTime = true;
				if ( $('.wrap_footer').length > 1 ) {
					removeFootr();
					oneTime = false;
				}
				else if ( oneTime ){
					setTimeout(function(){recheckFunction()},50);
				}
			}
			recheckFunction();
		}
	
		$('body').removeAttr("style");
		$('.nav li a').removeAttr('class');
		//$(".wrapNav").css('top','655px');
		$('.wrapFooter').show();
		//$(".wrapNav").css('position','absolute');
	}	
};

var loadJS = function (js) {
	function checkIfjQuery() {
		if (!window.jQuery)
			setTimeout(function () { checkIfjQuery() }, 50);
		else
			resumeLoad();
	}
	
	checkIfjQuery();
			
	function resumeLoad () {
		var loadJS = document.createElement("script");
		loadJS.src = js;
		loadJS.setAttribute("class", "removePageCss");
		document.getElementsByTagName('head')[0].appendChild(loadJS);
	}
};

var loadGlobalStyle = function () {
	(function checkIfbabylonGlobalStyle() {
		if (!window.babylon)
			setTimeout(function () { checkIfbabylonGlobalStyle() }, 50);
		else {
			if ( $('.divWithoutJavascript .wrap_footer').html() != undefined ) {
				function removeFootr (){
					$('.divWithoutJavascript').remove();
				}
				function recheckFunction() {
					var oneTime = true;
					if ( $('.wrap_footer').length > 1 ) {
						removeFootr();
						oneTime = false;
					}
					else if ( oneTime ){
						setTimeout(function(){recheckFunction()},50);
					}
				}
				recheckFunction();
			}
			$('body').removeAttr("style");
			$('.nav li a').removeAttr('class');
		}
	})();
	
};

/*! waitForImages jQuery Plugin - v1.4.2 - 2013-01-19
* https://github.com/alexanderdickson/waitForImages
* Copyright (c) 2013 Alex Dickson; Licensed MIT */
(function(e){var t="waitForImages";e.waitForImages={hasImageProperties:["backgroundImage","listStyleImage","borderImage","borderCornerImage"]},e.expr[":"].uncached=function(t){if(!e(t).is('img[src!=""]'))return!1;var n=new Image;return n.src=t.src,!n.complete},e.fn.waitForImages=function(n,r,i){var s=0,o=0;e.isPlainObject(arguments[0])&&(i=arguments[0].waitForAll,r=arguments[0].each,n=arguments[0].finished),n=n||e.noop,r=r||e.noop,i=!!i;if(!e.isFunction(n)||!e.isFunction(r))throw new TypeError("An invalid callback was supplied.");return this.each(function(){var u=e(this),a=[],f=e.waitForImages.hasImageProperties||[],l=/url\(\s*(['"]?)(.*?)\1\s*\)/g;i?u.find("*").andSelf().each(function(){var t=e(this);t.is("img:uncached")&&a.push({src:t.attr("src"),element:t[0]}),e.each(f,function(e,n){var r=t.css(n),i;if(!r)return!0;while(i=l.exec(r))a.push({src:i[2],element:t[0]})})}):u.find("img:uncached").each(function(){a.push({src:this.src,element:this})}),s=a.length,o=0,s===0&&n.call(u[0]),e.each(a,function(i,a){var f=new Image;e(f).bind("load."+t+" error."+t,function(e){o++,r.call(a.element,o,s,e.type=="load");if(o==s)return n.call(u[0]),!1}),f.src=a.src})})}})(jQuery);

