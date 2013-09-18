//------------------- SWF 공통 제어함수 시작
/**
 *	version 1.5
 *	최종수정일 : 2008. 11. 10
 *
 *	-- 플래시로더 객체생성 기본사용방법 ---
 *	<script language="javascript" type="Text/JavaScript">
 *		var setFlash = new SWFLoader();
 *		setFlash.init( '넓이', '높이', '파일경로', 총 매개변수값);
 *		setFlash.parameter('파람이름','값'); //이미 기본옵션 사용중
 *		setFlash.wmode('window'); //이미 기본옵션('transparent') 사용중
 *		setFlash.id('아이디이름'); //예) ID_SWF파일이름
 *		setFlash.alt('값');	// 플래시 대체 텍스트 값 입력
 *		setFlash.layer('div 아이디 이름')  //예) <div id='SWF파일명Layer'></div> 
 *		setFlash.show( );
 *	</script>
 *
 *	SWF파일 아이디 표준화 사용방법 : ID_파일명(대소문자구분) 예제 : 파일명이 navi.swf 인경우 --> 'ID_navi'
 *
 *	-- setFlash.layer() 사용시 방법 - 기본사용방법을 showSWFLayer() 함수로
 *     객체를 감싸주고 메서드호출 시 인자로 Div 아이디 값을 넘겨준다.
 *	
 *	<script language="javascript" type="Text/JavaScript">
 *		function showSWFLayer( layername) {
 *			var setFlash = new SWFLoader();
 *			setFlash.init( '넓이', '높이', '파일경로', 총 매개변수값);
 *			setFlash.parameter('파람이름','값'); //이미 기본옵션 사용중
 *			setFlash.id('아이디이름'); //예) ID_SWF파일이름
 *			setFlash.layer(layername)  //예) <div id='SWF파일명Layer'></div>
 *			setFlash.show();
 *		}
 *  </script>
 *
 *	<a href="javascript:showSWFLayer('siteMapLayer')">열기</a>
 *
 *	-- 최종 추가수정 내용
 *	url 입력시 "&" 엠퍼센드 기호 --> "&amp;" 자동치환기능 추가 
 *
 */

function SWFLoader() {
	var obj = new String;
	var parameter = new String;
	var embed = new String;
	
	var classId = new String;
    var codeBase = new String;
	var pluginSpage = new String;
	var embedType = new String;	
	var allParameter = new String;	
	
	var src = new String;
	var width = new String;
	var height = new String;
	var id = new String;
	var layer = new String;
	var arg = new String;
	var altText = new String;
	var wmode = new String;

	this.init = function ( w, h, s, a ) {
		width = w; //넓이
		height = h; //높이
		src = s; //파일경로
		arg = String(a).replace(/\&/gi, '%26'); // 매개변수

		wmode = 'transparent'; //모드설정

		classId = 'clsid:d27cdb6e-ae6d-11cf-96b8-444553540000';
		codeBase = 'http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,0,0';
		pluginSpage = 'http://www.macromedia.com/go/getflashplayer';
		embedType = 'application/x-shockwave-flash';

		parameter += "<param name='allowScriptAccess' value='always'>\n";
		parameter += "<param name='allowFullScreen' value='false'\n>";
		parameter += "<param name='movie' value='"+ s + "'>\n";
		parameter += "<param name='quality' value='high'>\n";
		parameter += "<param name='base' value='.'>\n";
		parameter += "<param name=FlashVars value='arg="+arg+"'>\n";
	}
	
	//플래시 오브젝트 옵션설정
	this.parameter = function ( param, value ) {
		 parameter += "<param name='"+param +"' value='"+ value + "'>\n";
	}

	// 플래시 wmode 설정 setFlash.wmode('window')
	this.wmode = function ( value ) {
		wmode = value;
	}

	// 플래시 아이디 설정
	this.id = function ( value ) {
		id = value;
	}
	
	// 플래시 대체텍스트 설정
	this.alt = function ( value ) {
		altText = value;
	}

	// 플래시 삽입 레이어 설정
	this.layer = function ( value ) {
		if(value == undefined) {
			layer = "";
		} else {
			layer = value;
		}
	}

	this.show = function () {
		obj = '<object id="'+id+'" width="'+width+'" height="'+height+'" classid="'+classId+'" codebase="'+codeBase+'">\n'+
			parameter +
			'<param name="wmode" value="'+wmode+'">\n'+
			'<!--[if !IE]>-->\n' +
			'<object type="application/x-shockwave-flash" data="' + src + '" width="' + width + '" height="' + height + '" name="' + id + '">\n' +
				parameter +
				'<param name="wmode" value="'+wmode+'">\n'+
			'<!--<![endif]-->\n' +
				'<div class="alt-content alt-' + id + '">' + altText + '</div>\n' +
			'<!--[if !IE]>-->\n' +
			'</object>\n' +
			'<!--<![endif]-->\n' +
		'</object>';

		if(layer == "") {
			document.write(obj);
		}else{
			var div = document.getElementById( layer);
			div.style.display = "";
			div.innerHTML = obj;
		}
	}
}

function hideSWFLayer( div) {
	var div = document.getElementById( div);
	div.style.display = "";
	div.innerHTML = "";
}

function thisMovie(movieName) {
	if (navigator.appName.indexOf("Microsoft") != -1) {
		return window[movieName];
	}
	else {
		return document[movieName];
	}
 }

 function callExternalInterface(movieId) {
    thisMovie(movieId).moveMc();	
}

function resizeSWF( id, height){
	document.getElementById( id ).height = height;
}
//------------------- SWF 공통 제어함수 끝

/* png 이미지 투명 적용 script */
function setPng24(obj) {
 obj.width=obj.height="1";
 obj.className=obj.className.replace(/\bpng24\b/i,"");
 obj.style.filter= "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+ obj.src +"',sizingMethod='image');"
 obj.src=""; 
 return "";
}

/* tab Type 01 */
	function tabType1(num){
		//alert(document.getElementById("tab2"));
		for(var i = 1; i < 5; i++){ //탭메뉴수 + 1 
		var tabjo = document.getElementById("tab"+i);
		var tabimg = document.getElementById("tabimg"+i);
		var tabimgbottom = document.getElementById("tabimgbottom"+i);
		//alert(i+"\n"+div);
		
			if(num == i){
				tabjo.style.display = "block";  // 활성화
				tabimg.src="/images/product/tab01_0"+i+"on.gif"; //탭내용이 보여질때의 탭이미지
				tabimgbottom.src="/images/product/tab01fo_0"+i+"on.gif"; //탭내용이 보여질때의 탭이미지
			} 
			else {
				tabjo.style.display ="none";
				tabimg.src="/images/product/tab01_0"+i+".gif";	//탭내용이 보여지지 않을 때의 탭이미지
				tabimgbottom.src="/images/product/tab01fo_0"+i+".gif";	//탭내용이 보여지지 않을 때의 탭이미지
			}	
		}
	}
/* tab Type 02 */
	function tabType2(num){
		//alert(document.getElementById("tab2"));
		for(var i = 1; i < 4; i++){ //탭메뉴수 + 1 
		var tabjo = document.getElementById("tab"+i);
		var tabimg = document.getElementById("tabimg"+i);
		var tabimgbottom = document.getElementById("tabimgbottom"+i);
		//alert(i+"\n"+div);
		
			if(num == i){
				tabjo.style.display = "block";  // 활성화
				tabimg.src="/images/product/tab01_0"+i+"on.gif"; //탭내용이 보여질때의 탭이미지
				tabimgbottom.src="/images/product/tab01fo_0"+i+"on.gif"; //탭내용이 보여질때의 탭이미지
			} 
			else {
				tabjo.style.display ="none";
				tabimg.src="/images/product/tab01_0"+i+".gif";	//탭내용이 보여지지 않을 때의 탭이미지
				tabimgbottom.src="/images/product/tab01fo_0"+i+".gif";	//탭내용이 보여지지 않을 때의 탭이미지
			}			
		
		}
	}
/* tab Type 03 */
function tabType3(num){
	//alert(document.getElementById("tab2"));
	for(var i = 5; i < 8; i++){ //탭메뉴수 + 1 
	var tabjo = document.getElementById("tab"+i);
	var tabimg = document.getElementById("tabimg"+i);
	var tabimgbottom = document.getElementById("tabimgbottom"+i);
	//alert(i+"\n"+div);
	
		if(num == i){
			tabjo.style.display = "block";  // 활성화
			tabimg.src="/images/product/tab01_0"+i+"on.gif"; //탭내용이 보여질때의 탭이미지
			tabimgbottom.src="/images/product/tab01fo_0"+i+"on.gif"; //탭내용이 보여질때의 탭이미지
		} 
		else {
			tabjo.style.display ="none";
			tabimg.src="/images/product/tab01_0"+i+".gif";	//탭내용이 보여지지 않을 때의 탭이미지
			tabimgbottom.src="/images/product/tab01fo_0"+i+".gif";	//탭내용이 보여지지 않을 때의 탭이미지
		}	
	}
}

/* tab Type 04(faq) */
function tabType4(num){
	//alert(document.getElementById("tab2"));
	for(var i = 1; i < 4; i++){ //탭메뉴수 + 1 
	var tabjo = document.getElementById("tab"+i);
	var tabimg = document.getElementById("tabimg"+i);
	//alert(i+"\n"+div);
	
		if(num == i){
			tabjo.style.display = "block";  // 활성화 tab_faq01_on.gif
			tabimg.src="/images/customer/tab_faq0"+i+"_on.gif"; //탭내용이 보여질때의 탭이미지
		} 
		else {
			tabjo.style.display ="none";
			tabimg.src="/images/customer/tab_faq0"+i+"_off.gif";	//탭내용이 보여지지 않을 때의 탭이미지
		}	
	}
}

/* tab Type 05(faq) */
function tabType5(num){
	//alert(document.getElementById("tab2"));
	for(var i = 1; i < 4; i++){ //탭메뉴수 + 1 
	var tabjo = document.getElementById("tab"+i);
	var pagejo = document.getElementById("paging"+i);
	var tabimg = document.getElementById("tabimg"+i);
	//alert(i+"\n"+div);
	
		if(num == i){
			tabjo.style.display = "block";  // 활성화 tab_faq01_on.gif
			pagejo.style.display = "block";
			tabimg.src="/images/customer/tab_faq0"+i+"_on.gif"; //탭내용이 보여질때의 탭이미지
		} 
		else {
			tabjo.style.display ="none";
			pagejo.style.display = "none";
			tabimg.src="/images/customer/tab_faq0"+i+"_off.gif";	//탭내용이 보여지지 않을 때의 탭이미지
		}
	}
	document.mainForm.faq_tab_type.value = num;
}

/* tab Type 06(Customer_main) */
function tabType6(num){
	//alert(document.getElementById("tab2"));
	for(var i = 1; i < 3; i++){ //탭메뉴수 + 1 
	var tabjo = document.getElementById("tab"+i);
	var tabimg = document.getElementById("tabimg"+i);
	//alert(i+"\n"+div);
	
		if(num == i){
			tabjo.style.display = "block";  // 활성화 tab_faq01_on.gif
			tabimg.src="/images/customer/tab_mainFaq0"+i+"_on.gif"; //탭내용이 보여질때의 탭이미지
		} 
		else {
			tabjo.style.display ="none";
			tabimg.src="/images/customer/tab_mainFaq0"+i+"_off.gif";	//탭내용이 보여지지 않을 때의 탭이미지
		}	
	}
}

/*레이어 띄우기 // 제품 비교하기는 10번으로 가자 11번은 다른나라국가언어*/
	function ViewLayer(e){
		if(document.getElementById("Pop" + e).style.display=="none"){
		   document.getElementById("Pop" + e).style.display='';
		}else{
		   document.getElementById("Pop"+ e).style.display='none';
		}
	}

/* 탭 메뉴 업데이트부분1 개인 */
function tabSwap1(num) {
	for (i = 1; i < 5; i++){
		if (num == i){
			document.getElementById('tab1_0'+i+'').style.display='';
				} 
		else {
			document.getElementById('tab1_0'+i+'').style.display='none';
			}
				}
}
/* 탭 메뉴 업데이트부분2 비즈니스 */
function tabSwap2(num) {
	for (i = 1; i < 6; i++){
		if (num == i){
			document.getElementById('tab2_0'+i+'').style.display='';
				} 
		else {
			document.getElementById('tab2_0'+i+'').style.display='none';
			}
				}
}

/* 탭 메뉴 업데이트부분2 무료 */
function tabSwap3(num) {
	for (i = 1; i < 3; i++){
		if (num == i){
			document.getElementById('tab3_0'+i+'').style.display='';
				} 
		else {
			document.getElementById('tab3_0'+i+'').style.display='none';
			}
				}
}

/* input 택스트 보이기 */
	function clearField(field){
		if (field.value == field.defaultValue) {
			field.value = '';
		}
	}
	function checkField(field){ 
		if (field.value == '') {
			field.value = field.defaultValue;
		}
	}

/* 팝업창 띄우기 가운데로 */
function fNewWin(name, width, height)  {
 cw=screen.availWidth; // 화면 너비
 ch=screen.availHeight; // 화면 높이

 sw=width;// 띄울 창의 너비
 sh=height;// 띄울 창의 높이

 ml=(cw-sw)/2;// 가운데 띄우기위한 창의 x위치
 mt=(ch-sh)/2;// 가운데 띄우기위한 창의 y위치

  NewWindow=window.open(name,'','width='+sw+',height='+sh+',top='+mt+',left='+ml+',toobar=no,scrollbars=yes,menubar=no,status=no ,directories=no,location=no');
  NewWindow.focus();
}

/* 팝업뜰때 뒤에 어둡게 하기 */
 var flashMovieCnt = 0;
 function openAlpha() {
  var alphaDiv = parent.document.getElementById("alphaDiv");
  alphaDiv.style.display = "block";
  document.getElementById("alphaDiv").style.height = document.body.scrollHeight;   //요건 movieDiv 세로값을 바디의 세로값으로 불러오기
  if(flashMovieCnt < 30) {
   flashMovieCnt+=30;  //요거 높이면 빨라짐
   setOpacity(alphaDiv, flashMovieCnt*2);  //알파값 변경 (숫자로 변경)
   setTimeout("openAlpha()", 5);
  }
  else {
   flashMovieCnt = 0;
  }
 }
 function setOpacity(o,alpha){
  if(o.filters)o.filters.alpha.opacity = alpha;
  else o.style.opacity = alpha;
 }
 function closeAlpha() {
  var alphaDiv = parent.document.getElementById("alphaDiv");
 
  setOpacity(alphaDiv, 0);
  alphaDiv.style.display = "none";
 }

function gonotice_view(action_name,idx){
	forms = document.noticeview;
	forms.encoding = "application/x-www-form-urlencoded";
	forms.idx.value = idx;
	forms.action = action_name;
	forms.submit();
}

/* quicks */
function quicks(ele){
	var target = document.getElementById(ele); /* 퀵메뉴 */
	var limitB = false; /* 퀵메뉴가 하단 어느부분까지 내려올지 */
	var intTop = 0; /* 퀵메뉴 초기 top위치 기억 */
	var _root = this;
	var yMenuFrom, yMenuTo, speed;
	
	this.getStyle = function(element, what){
		var value = "";
		if(element.currentStyle) value = element.currentStyle[what];
		else if(window.getComputedStyle) value = window.getComputedStyle(element,null)[what];
		return value;
	}

	this.int = function(){
		var temp = _root.getStyle(target, "top");
		target.style.top = temp;
		intTop = parseFloat(temp);
	}

	this.limitBottom = function(num){
		limitB = (document.body.scrollHeight || document.documentElement.scrollHeight) - target.offsetHeight - num;
	}

	this.move = function(){
		yMenuFrom = parseFloat(target.style.top);
		yMenuTo = intTop + parseFloat(document.body.scrollTop || document.documentElement.scrollTop);
		if(limitB){
			if(yMenuTo >= limitB) yMenuTo = limitB;
		}
		if(yMenuFrom == yMenuTo){
			return setTimeout(function(){_root.move();}, 40); /* 스크롤의 반응속도 */
		}else{
			speed = Math.floor((yMenuTo - yMenuFrom) *0.2);
			target.style.top = (yMenuFrom + speed) + "px";
			return setTimeout(function(){_root.move();}, 40); /* 퀵속도 */
		}
	}
}


// 배너롤링
function PhRolling(obj){
	var _root = this;
	var to = 0;
	var from = 0;
	var mvCnt = obj.mvCnt || 1; // 리스트가 움직일 갯수
	var sFlag = true;

	var ele = document.getElementById(obj.ele);
	var eleCvWidth = ele.parentNode.offsetWidth; /* 리스트부모의 총넓이(리스트레이어의 딱 넓이 만큼) */
	var list = ele.getElementsByTagName("LI"); /* 상품리스트들 */
	var listWidth = list[0].offsetWidth;
	var visibleListCnt =  parseFloat(eleCvWidth/listWidth);
	var oldAxis = 0;
	
	var btnLchk = true; // <<, >> 버튼 활성화 관련
	var btnRchk = true;

	ele.style.position = "absolute";
	ele.style.width = (listWidth*list.length) + "px";
	ele.style.left = 0;

	var btnLeft = document.getElementById(obj.btnLeft);
	var btnRight = document.getElementById(obj.btnRight);
	btnLeft.onclick = function(){ _root.leftMove() };
	btnRight.onclick = function(){ _root.rightMove() };
	
	// 리스트이동
	this.gotoNum = function(num){
		var lengths = list.length;
		
		// >> 움직임
		if(oldAxis >= lengths-visibleListCnt && num>lengths-visibleListCnt || num>=lengths)
			num = 0;
		else if(num<lengths && num>lengths-visibleListCnt)
			num = lengths-visibleListCnt;
		
		// << 움직임
		if(num<0 && num>-mvCnt)
			num = 0;
		else if(num<0 && num<=-mvCnt)
			num = lengths-visibleListCnt;

		from = _root.getStyle(ele, "left");
		to = -(listWidth*num);
		oldAxis = num;

		_root.btnVisibility();
		_root.action();
	}
	
	// 리스트의 움직일 갯수변경
	this.mvCnt = function(num){
		mvCnt = Number(num);
	}

	// css스타일을 얻을때 사용하는 함수
	this.getStyle = function(el, what){
		var target = el;
		var value = "";
		if(target.currentStyle){
			value = target.currentStyle[what];
		}else if(window.getComputedStyle){
			value = window.getComputedStyle(target,null)[what];
		}
		return parseFloat(value);
	}

	// 왼쪽방향 << 이미지가 클릭되었을때
	this.leftMove = function(){
		if(!sFlag) return false;
		sFlag = false;
		_root.gotoNum(oldAxis-mvCnt);
	}
	
	// 오른쪽방향 >> 이미지가 클릭되었을때
	this.rightMove = function(){
		if(!sFlag) return false;
		sFlag = false;
		_root.gotoNum(oldAxis+mvCnt);
	}
	
	// 화살표 보이기/가리기
	this.btnVisibility = function(){
		if(btnLeft.alpha != "no"){
			btnLeft.alpha = "no"
			btnLeft.style.filter = "alpha(opacity=40)";
			btnLeft.style.opacity = 0.4;
			
			btnRight.style.filter = "alpha(opacity=40)";
			btnRight.style.opacity = 0.4;
		}else{
			btnLeft.alpha = "yes"
			if(oldAxis!=0){
				btnLeft.style.filter = "alpha(opacity=100)";
				btnLeft.style.opacity = 1.;
			}
			
			if(oldAxis!=list.length-visibleListCnt){
				btnRight.style.filter = "alpha(opacity=100)";
				btnRight.style.opacity = 1.;
			}
		}
	}
	
	// 리스트들 움직이는 동작부분
	this.action = function(){
		var func_PhRolling = setInterval(function(){
			var speed = (to-from)*0.2;
			speed = (to>from) ? Math.ceil(speed) : Math.floor(speed);
			
			from = _root.getStyle(ele, "left");
			ele.style.left = from+speed+"px";
			
			if(to==from){
				_root.btnVisibility();
				clearInterval(func_PhRolling);
				sFlag = true;
			}
		}, 36);
	}
}

function downRiaDoc_F(DNid) {
 
	f = document.downRiaForm_F;
	f.downtype.value = "";	
	f.action = "";
	
	f.downtype.value = DNid;	
	f.action = "/download.asp";
		
	f.target ="actionFrame"
	f.submit();
}