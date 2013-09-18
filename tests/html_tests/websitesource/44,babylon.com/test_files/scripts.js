var babylon = (function () {
	var pagesTitle = "Babylon 10 Translation Software and Dictionary Tool";
	var disFromTop, pageToLoad, topHP, isHP, hpAnimatevar;
	var refUrlIE = true;
	var isJSfooter = false;
	var page = location.pathname;
	if ( !$.browser.msie ) {
		var num665 = "655px";
	}
	if ( $.browser.msie ) {
		var num665 = "690px";
	}
	
	backToHomePage = true;
	
	var hrefHP1 = "/";
	var hrefHP2 = "/index";
	var hrefHP3 = "/index.html";
	var hrefHP4 = "#index";
	
	var urlHashIE = function () {
		if ( window.location.hash ) {
			var a = window.location.hash;
			a = a.split("#");
			a = a[1].split('&')[0];
			window.location.hash = a;
		}
		return a;
	};
	
	
	var loadHashIE = function () {
		if ( window.location.hash ) {
			var a = window.location.hash;
			a = a.split("#");
			a = a[1].split('&')[0];
			a = a.split("?")[0];
		}
		return a;
	};
	
	var checkHomePage = function () {
	
		
		if ( location.pathname == hrefHP1 || location.pathname == hrefHP2 || location.pathname == hrefHP3 ) {
			isHP = true;
			$('.wrapNav').css('top','450px !important');
			
		}
		
	};

	var jsFotter = function () { 
	    return isJSfooter;
	};
	
	var menuLangBar = function (HC) {
		
		$('.menuLangBar').html($(HC).children('.menuLangBar2').html());
		$('.menuLangBar2').remove();
		
	};
	
	// click on navigation bar and ajax loader
	var clickLinkIe = true;
	$('a[data-link="inner"]').live('click', function (e) {
		$('body').children().each(function(){
			if ( $(this).attr('class') != "wrapheader" && $(this).attr('class') != "content" && $(this).attr('class') != "wrapfooter" ) {
				 $(this).remove()
			}
		});
		historyBackButton();
		clickLinkIe = true;
		if ( $.browser.msie )
			page = location.hash;
		$('.nav li a').removeAttr('class');
		
		refUrlIE = false;
		$('#tohomepage').attr("data-link", "inner");
		$('#tohomepage').attr("href", "http://www.babylon.com");
		var disFromTopOS = ($(".wraphead").outerHeight()+"px");
		var homePage = false;
		var skip = true;
		
		pageToLoad = $(this).attr('href');
		
		if ( $(this).attr('href') == "http://www.babylon.com" ) {
			pageToLoad = "/index";
		}
		
		// checking about HP
		if ( ( $(this).attr("href") == "index" || $(this).attr("href") == "/" || $(this).attr("href") == "index.html" ) && ( location.pathname == hrefHP1 || location.pathname == hrefHP2 || location.pathname == hrefHP3 ) ) {
			loadContent(pageToLoad);
		}
		else {
			if ( !$.browser.msie ) {
				if ( pageToLoad == "/index" && isHP ) {
					loadContent(pageToLoad);
					homePage = true;
					isHP = false;
				} // go to HP
				if ( pageToLoad == "/index" ) {
					backToHP(pageToLoad, num665);
					$('#tohomepage').removeAttr("data-link");
					$('#tohomepage').removeAttr("href");
					homePage = true;
					skip = false;
				} // leaving HP
				if ( ( location.pathname == hrefHP1 || location.pathname == hrefHP2 || location.pathname == hrefHP3 || location.hash == hrefHP4 ) && skip ) {
					hpAnimate(pageToLoad, disFromTopOS);
					topHP = num665;
					homePage = true;
				}
				else if ( !homePage ) {
					loadContent(pageToLoad);
				}
			}
			
			if ( $.browser.msie ) {
				urlHashIE();
				if ( pageToLoad == "/index" ) {
					backToHP(pageToLoad , num665);
					$('#tohomepage').removeAttr("data-link");
					$('#tohomepage').removeAttr("href");
					homePage = true;
					isHP = false;
				} // go to HP
				if ( page.replace('#','') == hrefHP4.replace('#','/') || ( location.pathname == hrefHP2 && !window.location.hash ) || ( location.pathname == hrefHP1 && !window.location.hash ) ) {
					hpAnimate(pageToLoad, disFromTopOS);
					homePage = true;
					skip = false;
				}
				else if ( !homePage && ( location.hash != hrefHP4 ) && page != "#/index" ) {
					loadContent(pageToLoad);
				}
			}
		}
		if ( !$.browser.msie )
			window.history.pushState(null, pagesTitle, pageToLoad);
		if ( $.browser.msie ) {
			page = location.hash;
			location.hash = pageToLoad;
		}
		e.preventDefault();
	});
	
	// make sure the back and forword working
	var historyBackButton = function () {
		(function resumeLoadHistory () {
			window.onpopstate = function (event) {
				var skip = true;
				var beforeUrlChange = true;
				pageToLoad = location.pathname;
				
				if ( true ) {
					$('.wrap_footer').hide();
					loadContent(pageToLoad);
					
					if ( pageToLoad != "/index" && pageToLoad != "/" ) {
						//$(".wrapNav").css('position','fixed');
						//$(".wrapNav").css('top','64px');
						$('#tohomepage').attr("data-link", "inner");
						$('#tohomepage').attr("href", "http://www.babylon.com");
					}
					else if ( pageToLoad == "/index" || pageToLoad == "/" ) {
						$('#tohomepage').removeAttr("data-link");
						$('#tohomepage').removeAttr("href");
						//$(".wrapNav").css('position','absolute');
						//$(".wrapNav").css('top','655px');
					}
					$('.deletPageCss').remove();
				}
				History.pushState(null, pagesTitle, pageToLoad);
				hpAnimatevar = true;
				event.preventDefault();
			};
		})();
	};
	if ( $.browser.msie ) {
		var hashChangeIE = (function () { // back button in IE
			$(window).bind('hashchange', function () {
				if ( !clickLinkIe ) {
					loadContent(location.hash.replace('#',''));
					if ( location.hash == "#/index" || location.hash == "#/" ) {
						setTimeout(function(){
							//$(".wrapNav").css('position','absolute');
							//$(".wrapNav").css('top','655px');
						},100);
					}
					if ( location.hash == "" && (location.pathname == "/" || location.pathname == "/index" || location.pathname == "//index") ) {
						setTimeout(function(){
							//$(".wrapNav").css('position','absolute');
							//$(".wrapNav").css('top','655px');
						},100);
					}
					else if ( location.hash != "" ) {
						//$(".wrapNav").css('position','fixed');
						//$(".wrapNav").css('top','64px');
					}
				}
				clickLinkIe = false;
			});
		})();
	}
	
	var checkIfContentEmpty = function (e,url) {
		if ( e == "" ) {
			window.location.href = "http://" + location.host + url;
		}
	};
	// if leave Hp
	var hpAnimate = function (url, headHiegth) {
		$('.wrap_footer').hide();
		var holdHTML = $('.content').html();
		$(".content").before('<div class="holdHTML"></div>');
		$('.content').html('');
		$(".content").hide();
		$('.wrap_footer').hide();
		$(".holdHTML").html(holdHTML);
		$(".holdHTML").show(); 
		$(".holdHTML").animate({ marginTop: '-1150px' }, 1700, function () {
			var HC = "";
			var holdHead = "";
			var holdBody = "";
			$.post(url + '.html', function(e){
				HC = e;
				pagesTitle = e.split(/(?:<title>|<\/title>)/ig)[1];
				document.title = pagesTitle;
				setTimeout(function(){
					menuLangBar(HC)
				},2000);
				$(".nav a[href='"+url+"']").attr('class','navCurrent');
				holdHead = HC.split(/(?:<head>|<\/head>)/ig)[1];
				holdHead = $(holdHead).filter('.removePageCss')
				holdBody = HC.split(/(?:<body>|<\/body>)/ig)[1];
				var splitBody = "<script type='text/javascript'>" + $(holdBody).filter('[data-loadScript]').html() + "</script>";
				var splitBody2 = $(holdBody).filter('.content').html();
				holdBody = splitBody2 + splitBody;
				$('.content').hide();
				$('.wrap_footer').hide();
				$('.content').html(holdBody);
				checkIfContentEmpty(e, url);
				checkStyleCSS(holdHead);
			});
			
			$(".holdHTML").remove(); 
			$(".content").css('margin-top','0px');
			$('.deletPageCss').remove(); 
			
		}); 
		$('.content').delay(2000).fadeIn(); 
		$('.wrapcontiner').delay(2000).fadeIn(); 
		$('.wrap_footer').delay(2800).fadeIn(); 
		//$(".wrapNav").css('position','fixed');
		/*if ( !$.browser.msie ) {
			$(".wrapNav").css('top','753px');
		}
		if ( $.browser.msie ) {
			$(".wrapNav").css('top','788px');
		}*/
		//$(".wrapNav").animate({ top: headHiegth }, 1250);
		$('.content title').remove();
		hpAnimatevar = false;
	};
	
	// if back to HP
	var backToHP = function (url, topHP) {
		$('body,html').animate({ scrollTop: 0 }, 100);
		$('.wrap_footer').hide();
		var contentHeight = 955;    // HP height
		var holdHTML = $('.content').html();
		$(".content").after('<div class="holdHTML"></div>');
		$(".holdHTML").css('visibility','hidden');
		$('.content').html('');
		
		var HC = "";
		var holdHead = "";
		var holdBody = "";
		$.post(url + '.html', function(e){
			HC = e;
			pagesTitle = e.split(/(?:<title>|<\/title>)/ig)[1];
			document.title = pagesTitle;
			setTimeout(function(){
				menuLangBar(HC)
			},2000);
			$(".nav a[href='"+url+"']").attr('class','navCurrent');
			holdHead = HC.split(/(?:<head>|<\/head>)/ig)[1];
			holdHead = $(holdHead).filter('.removePageCss')
			holdBody = HC.split(/(?:<body>|<\/body>)/ig)[1];
			var splitBody = "<script type='text/javascript'>" + $(holdBody).filter('[data-loadScript]').html() + "</script>";
			var splitBody2 = $(holdBody).filter('.content').html();
			holdBody = splitBody2 + splitBody;
			$('.content').html(holdBody);
			checkIfContentEmpty(e, url);
			checkStyleCSS(holdHead);
		});
		$(".holdHTML").css('height','5000px');
		$(".content").css("margin-top", "-1150px");
		$(".holdHTML").html(holdHTML);
		$(".content").animate({ marginTop: "0px" }, 1500, 
			function () {
				$(".holdHTML").remove();
				$('.deletPageCss').remove();
			});
		$(".content").animate({ marginTop: "0px" }, 1500);
		//$(".wrapNav").css('top','-35px');
		//$(".wrapNav").css('position','absolute');
		//$(".wrapNav").animate({ top: topHP }, 1500);
		$('.wrap_footer').show();
		$('.content title').remove();
		hpAnimatevar = false;
	};
	
	// if regular loading pages
	var loadContent = function (url) {
		$('.wrap_footer').hide();
		if ( url != "/index" ) {
			$('.content').hide();
		}
		$('.content').empty();
		var HC = "";
		var holdHead = "";
		var holdBody = "";
		$.post(url + '.html', function(e){
			HC = e;
			pagesTitle = e.split(/(?:<title>|<\/title>)/ig)[1];
			document.title = pagesTitle;
			menuLangBar(HC);
			$(".nav a[href='"+url+"']").attr('class','navCurrent');
			checkIfContentEmpty(e, url);
			holdHead = HC.split(/(?:<head>|<\/head>)/ig)[1];
			holdHead = $(holdHead).filter('.removePageCss')
			holdBody = HC.split(/(?:<body>|<\/body>)/ig)[1];
			var splitBody = "<script type='text/javascript'>" + $(holdBody).filter('[data-loadScript]').html() + "</script>";
			var splitBody2 = $(holdBody).filter('.content').html();
			holdBody = splitBody2 + splitBody;
			$('.content').html(holdBody);
			checkStyleCSS(holdHead);
		});
		
		$('.wrap_footer').before('<div class="ajaxLoad"><img src="http://img.babylon.com/site/images/babylon10/nav/ajax-loader.gif" width="15"/></div>');
		$('.content title').remove();
		hpAnimatevar = false;
		if ( $.browser.msie ) {
			$('.menuLangBar a').click(function(){
				$(this).attr('href', $(this).attr('href') + location.hash);
				
			});
		}
	};
	
	var after_LoadContent = function () {
				$('.ajaxLoad').remove();
				$('.content').show(); 
				$('.wrap_footer').show();
	};
	
	var checkStyleCSS = function (holdHead) {
		var dontAdd1	= "http://img.babylon.com/site/images/babylon10/append.js";
		var dontAdd2 = "http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.js";
		var dontAdd3 = "http://img.babylon.com/site/images/babylon10/modernizr.js";
		var dontAdd4 = "http://img.babylon.com/site/images/babylon10/history.js";
		var dontAdd5 = "http://img.babylon.com/site/images/babylon10/scripts.js";
		$('.removePageCss[rel="stylesheet"]').remove();
		for (var arr = 0; arr < holdHead.length; arr++){
		
			if ( ($('head').find('.removePageCss[type="text/javascript"]').attr('src') != holdHead[arr].src && holdHead[arr].src != undefined && dontAdd1 != holdHead[arr].src && dontAdd2 != holdHead[arr].src && dontAdd3 != holdHead[arr].src && dontAdd4 != holdHead[arr].src && dontAdd5 != holdHead[arr].src) || ($('head').find('.removePageCss[rel="stylesheet"]').attr('href') != holdHead[arr].href && holdHead[arr].href != undefined)  ) {
			
				$(holdHead[arr]).addClass('removePageCss');
				holdHead[arr].onload = function () { 
					window.setTimeout(function () {
						after_LoadContent();
					}, $.browser.msie || $.browser.mozilla ? 700 : 0)
				};
				$(holdHead[arr]).appendTo('head');
			}
		}
	}
	
	var urlForIE = function () {
		
		if ( $.browser.msie && window.location.hash && refUrlIE ) {
			
			var hash = window.location.hash.split("#");
			hash = hash[hash.length-1].split("?");
			hash[0] = hash[0].replace('.','').replace('html','');
			if ( hash[0][0] == "/" ) {
				window.location.href = "http://" + window.location.hostname + hash[0];
			}
		}
		
	};
	
	$('#tohomepage').click(function(){
		backToHomePage = false;
	});
	
	$(window).ready(function() {
		
		if ( $.browser.msie && window.location.hash ) {
			urlForIE();
		}

		$(".wrapheader").load(function(){
			$('.menuLangBar').html($('.menuLangBar2').html());
			$('.menuLangBar2').remove();
			if ( location.pathname == hrefHP1 || location.pathname == hrefHP2 || location.pathname == hrefHP3 ) {
				$('#tohomepage').removeAttr("data-link");
				$('#tohomepage').removeAttr("href");
			}
			var locPathname = window.location.pathname;
			$(".nav a[href='"+locPathname+"']").attr('class', 'navCurrent');
		});
		$(".wrapfooter").load(function () {
			$('.divWithoutJavascript').remove();
		    isJSfooter = true;
		});
		
		
		var checkCart = function () {
			var cookie = document.cookie;
			if ( cookie != ""  ) {
				cookie = cookie.split(";");
				for ( var i=0; i < cookie.length; i++ ) {
					if ( cookie[i].indexOf("cart") == 1 )
						return true;
				}
			}
		};
		
		var changeCartColor = (function () {
		var buyTrans = ['Buy',	'شراء',	'Acheter',	'Kaufen',	'קנה',	'Acquista',	'購入',	'Comprar',	'Comprar'];
		var cartTrans = ['Cart',	'عربة التسوق',	'Panier',	'Einkaufswagen',	'סל קניות',	'Carrello',	'カート',	'Carrinho',	'Cesta de compra'];
		
		if ( !checkCart() ) {
			$('.cart').css('background-color','#80b946');
			for ( var i=0; i < cartTrans.length; i++ ) {
				$('.cart').html('<img src="http://img.babylon.com/site/images/babylon10/nav/cart_img.png" alt="Cart" width="16" height="16" style="margin-right:15px;">'+$('.cart').text().replace(cartTrans[i],buyTrans[i]).replace('(0)',''));
				if ( location.hostname == "www.babylon.com" )
					$('.cart').css("width", "70px");
				if ( location.hostname == "deutsch.babylon.com" ) {
					$('.cart').css("font-size", "14px");
					$('.cart').css("width", "80px");
				}
				if ( location.hostname == "espanol.babylon.com" ) {
					$('.cart').css("font-size", "14px");
					$('.cart').css("width", "90px");
				}
				if ( location.hostname == "italiano.babylon.com" )
					$('.cart').css("width", "98px");
				if ( location.hostname == "francais.babylon.com" )
					$('.cart').css("width", "90px");
				if ( location.hostname == "portugues.babylon.com" )
					$('.cart').css("width", "95px");
				if ( location.hostname == "hebrew.babylon.com" )
					$('.cart').css("width", "75px");
				if ( location.hostname == "japanese.babylon.com" )
					$('.cart').css("width", "72px");
				if ( location.hostname == "arabic.babylon.com" )
					$('.cart').css("width", "70px");
			} 
			$('.cart').hover(function(){$(this).css('background-color','#74ab3e')},function(){$(this).css('background-color','#80b946')});
			$('.cart').parent().attr('href','https://store.babylon.com/?trid=HPBUY');
			}
			else {
			$('.cart').css('background-color','#10c2e8');
			for ( var i=0; i < cartTrans.length; i++ ) {
				$('.cart').html('<img src="http://img.babylon.com/site/images/babylon10/nav/cart_img.png" alt="Cart" width="16" height="16" style="margin-right:15px;">'+$('.cart').text().replace(buyTrans[i],cartTrans[i]));
			}
			if ( location.hostname == "portugues.babylon.com" )
					$('.cart').css("width", "115px");
			$('.cart').hover(function(){$(this).css('background-color','#10afd1')},function(){$(this).css('background-color','#10c2e8')});
			$('.cart').parent().attr('href',$('.cart').parent().attr('href')+'&trid=HPBUY');
			}
		})();
		
		var img = document.createElement('img');
		img.src = "http://img.babylon.com/site/images/babylon10/homepage/bg_img.jpg";
	});

	return { hpAnimate: hpAnimate, menuLangBar: menuLangBar, jsFotter: jsFotter }
	
})();