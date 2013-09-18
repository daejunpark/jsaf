//gnb
$(document).ready(function() {
	$('.rollover').hover(function() {
		var currentImg = $(this).attr('src');
		$(this).attr('src', $(this).attr('hover'));
		$(this).attr('hover', currentImg);
	}, function() {
		var currentImg = $(this).attr('src');
		$(this).attr('src', $(this).attr('hover'));
		$(this).attr('hover', currentImg);
	});
	
	//로그인 utility 이미지
	var gnb_layerlist1 = $('ul.t_login li a img');
	gnb_layerlist1.hover(function(){
		var img = $(this);
		var img_name = img.attr('src').split('.gif')[0];
		img.attr('src', img_name + '_on.gif');
	});
	gnb_layerlist1.mouseleave(function(){
		var img = $(this);
		var img_name = img.attr('src').split('_on')[0];
		img.attr('src', img_name + '.gif');
	});
	
	//gnb
	var gnbLi = $('#gnb ul.gnb_menu li.gnb_mmenu');
	
	// mouse over
	gnbLi.mouseover(function(){
		var gnbLi_idx =  gnbLi.index(this); //현재 클릭한 탭
		for(var i=0; i<gnbLi.length; i++){
			if(i == gnbLi_idx){
				$('#gnbSub' + gnbLi_idx).show();
				nowTab = gnbLi_idx;
			}else {
				$('#gnbSub' + i).hide();
			}
		}
	});
	

	gnbLi.mouseleave(function(){
		var gnbLi_idx =  gnbLi.index(this); //현재 클릭한 탭		

		for(var i=0; i<gnbLi.length; i++){
			$('#gnbSub' + i).hide();
		}
	});
	
});

/*
jQuery(function(){	
	var gnb_layer = $('ul.gnb_layer');
	var gnbLi = $('div#gnb ul.gnb_menu li');
	
	// mouse over
	gnbLi.hover(function(){
		$(this).find('> ul').show();
		$(this).find('> ul').css('z-index','150');
		//$(this).find('> .gnb_layerbg').show();
	});		
	// mouse out
	var gnbLi_idx = gnbLi.index(this);
	gnbLi.mouseleave(function(){
		for(var i = ; i<gnbLi.length ; i++){
			if(gnbLi_idx == i){		
			}else{
				$(this).find('> ul').delay(100).hide();		
			}
		}
		$(this).find('> ul').css('z-index','100');
		//$(this).find('> .gnb_layerbg').delay(100).hide();
	});
	// focus
	$("div#gnb ul.gnb_menu li a").focus(function(){
		$(this.parentNode).find('> ul').show();
		//$(this.parentNode).find('> .gnb_layerbg').show();
	});
	
	//로그인 utility 이미지
	var gnb_layerlist1 = $('ul.t_login li a img');
	gnb_layerlist1.hover(function(){
		var img = $(this);
		var img_name = img.attr('src').split('.gif')[0];
		img.attr('src', img_name + '_on.gif');
	});
	gnb_layerlist1.mouseleave(function(){
		var img = $(this);
		var img_name = img.attr('src').split('_on')[0];
		img.attr('src', img_name + '.gif');
	});
		
	//gnb 이미지
	var gnb_layerlist2 = $('ul.gnb_menu li a img');
	gnb_layerlist2.hover(function(){
		var img = $(this);
		var img_name = img.attr('src').split('_off.gif')[0];
		img.attr('src', img_name + '_on.gif');
	});
	gnb_layerlist2.mouseleave(function(){
		var img = $(this);
		var img_name = img.attr('src').split('_on')[0];
		img.attr('src', img_name + '_off.gif');
	});
	
	//gnb 서브이미지
	var gnb_layerlist_img = $('ul.gnb_menu li ul li a img');
	gnb_layerlist_img.hover(function(){
		var img = $(this);
		var img_name = img.attr('src').split('.gif')[0];
		img.attr('src', img_name + '_over.gif');
	});
	gnb_layerlist_img.mouseleave(function(){
		var img = $(this);
		var img_name = img.attr('src').split('_over')[0];
		img.attr('src', img_name + '.gif');
	});
});
*/

// LNB
jQuery(function(){	
	var lnb = $('#lnb');
	var depth1li = $('#lnb .lnb_submenu > li');
	var depth2ul = $('#lnb .lnb_submenu li ul').addClass("submenu"); // ul
	$('#lnb .lnb_submenu li ul').find('li:first-child').addClass('first-child');
	$('#lnb .lnb_submenu li ul').find('li:last-child').addClass('last-child');

	// 1depth 활성화
	var current1li = $('#lnb .lnb_submenu > li.current');
	current1li.find('> a > img').addClass('imgon');

	// 2depth 활성화
	var current2li = $('#lnb .lnb_submenu > li > ul > li.current');
	current2li.find('> a > img').addClass('imgon');
	current2li.find('> ul').css('display','block');
	current2li.parent('ul').css('height','auto');
	current2li.parent('ul').parent('li').addClass('on').addClass('open');
	current2li.parent('ul').parent('li').find('> a > img').addClass('imgover');

	// 3depth 활성화
	var current3li = $('#lnb .lnb_submenu > li > ul > li > ul > li.current');
	current3li.find('> a > img').addClass('imgon');
	current3li.parent('ul').css('display','block');
	current3li.parent('ul').parent('li').addClass('on');
	current3li.parent('ul').parent('li').find('> a > img').addClass('imgover2');
	current3li.parent('ul').parent('li').parent('ul').css('height','auto');
	current3li.parent('ul').parent('li').parent('ul').parent('li').addClass('on').addClass('open');
	current3li.parent('ul').parent('li').parent('ul').parent('li').find('> a > img').addClass('imgover');

	//활성화 온이미지변환
	var imgon = $('#lnb .lnb_submenu li a img.imgon');
	if(imgon.hasClass('imgon')){
		imgon.attr('src', imgon.attr('src').split('.gif')[0] + '_on.gif');
	}
	else {
	};

	//활성화 오버이미지변환
	var imgover = $('#lnb .lnb_submenu li a img.imgover');
	if(imgover.hasClass('imgover')){
		imgover.attr('src', imgover.attr('src').split('.gif')[0] + '_over.gif');
	}
	else {
	};

	//활성화 오버이미지변환
	var imgover2 = $('#lnb .lnb_submenu li a img.imgover2');
	if(imgover2.hasClass('imgover2')){
		imgover2.attr('src', imgover2.attr('src').split('.gif')[0] + '_over.gif');
	}
	else {
	};

	//메뉴 오버 이미지변환
	var lnb_menulist = $('ul.lnb_submenu li');
	lnb_menulist.hover(function(){
		if($(this).hasClass('current') || $(this).hasClass('on')){
		}
		else {
			var img = $(this).find('> a > img');
			var img_name = img.attr('src').split('.gif')[0];
			img.attr('src', img_name + '_over.gif');

		};
	});

	//메뉴 아웃 이미지변환
	lnb_menulist.mouseleave(function(){
		if($(this).hasClass('current') || $(this).hasClass('on')){
		}
		else {
			var img = $(this).find('> a > img');
			var img_name = img.attr('src').split('_over')[0];
			img.attr('src', img_name + '.gif');
		};
	});

	//메뉴 오버 움직임
	depth1li.hover(function(){
		var o = $(this).find('> ul > li'); // 2depth li들의 높이 계산
		var tmpH = 0;
		for(var i=0; i<o.length; i++){
			tmpH += o[i].offsetHeight;
		};
		//alert(tmpH);
		$(this).find('> ul').stop(true, false).delay(200).animate({ height:tmpH+"px" },"slow");
		if($(this).hasClass('current') || $(this).hasClass('on')){
		}
		else {
//			imgover.attr('src', imgover.attr('src').split('_over')[0] + '.gif');
			$('#lnb .lnb_submenu > li.open').find('> ul').delay(200).animate({ height:+"0" },"slow");
		};
	}, function(){
		$(this).find('> ul').stop(true, false).delay(200).animate({ height:"0" },"slow");
//		imgover.attr('src', imgover.attr('src').split('.gif')[0] + '_over.gif');
		if($(this).hasClass('current') || $(this).hasClass('on')){
		}
		else {
//			imgover.attr('src', imgover.attr('src').split('.gif')[0] + '_over.gif');
		};
	});

	//메뉴 아웃 움직임
	lnb.mouseleave(function(){
//		imgover.attr('src', imgover.attr('src').split('.gif')[0] + '_over.gif');
		var openli = $('#lnb .lnb_submenu > li.open');
		var o = openli.find('> ul > li');
		var tmpH = 0;
		for(var i=0; i<o.length; i++){
			tmpH += o[i].offsetHeight;
		};
		openli.find('> ul').stop(true, false).delay(300).animate({ height:tmpH+"px" },"slow");
	});
});


