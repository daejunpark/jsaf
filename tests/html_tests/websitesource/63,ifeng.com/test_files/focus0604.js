BX.Dom={
  _batch:function(el,func){
	  var _el=$A(el);
	  for( var i=0;i!=_el.length;i++){
		  if(_el[i])
			  func(_el[i]);
	  }
  },
  hide:function(els){
	  var _run=function(el){
		  el.style.display="none";
	  }
	  V.addListener($("welcome"),"click",function(){
		  if($("enterPro").style.display=="none"){
			  $("enterPro").style.display="";
			  $("username").className="nameup";
		  }else{
			  $("enterPro").style.display="none";
			  $("username").className="name";
		  }
	  });
	  this._batch(els,_run);
  },
  show:function(els){
	  var _run=function(el){
		  el.style.display="block";
	  }
	  this._batch(els,_run);
  },
  addClass:function(els,val){
	  if(!val)
		  return;
	  var _run=function(el){
		  var _cln=el.className.split(" ");
		  for( var i=0;i!=_cln.length;i++){
			  if(_cln[i]==val)
				  return;
		  }
		  el.className.length>0?(el.className=el.className+" "+val):(el.className=val);
	  }
	  this._batch(els,_run);
  },
  setClass:function(els,val){
	  var _run=function(el){
		  el.className=val;
	  }
	  this._batch(els,_run);
  },
  removeClass:function(els,val){
	  if(!val)
		  return;
	  var _run=function(el){
		  var _cln=el.className.split(" ");
		  var _s="";
		  for( var i=0;i!=_cln.length;i++){
			  if(_cln[i]!=val)
				  _s+=_cln[i]+" ";
		  }
		  if(_s==" ")
			  _s="";
		  if(_s.length!=0)
			  _s=_s.substr(0,_s.length-1);
		  el.className=_s;
	  }
	  this._batch(els,_run);
  },
  getElementsByClassName:function(parentEl,className,tagName){
	  if(!parentEl||!className)
		  return null;
	  var els=cds=[];
	  cds=$(parentEl).childNodes;
	  className=className.toUpperCase();
	  for( var i=0;i<cds.length;i++){
		  var _type=cds[i].nodeType;
		  if(_type!=3&&_type!=8&&cds[i].className.toUpperCase()==className){
			  if(!tagName||cds[i].nodeName.toUpperCase()==tagName.toUpperCase())
				  els[els.length]=cds[i];
		  }
	  }
	  return els;
  }
}
var O=BX.Dom;
V.addListener($("btnLoginClose"),"click",function(){
	$("loginbox").style.display="none";
});
function CheckInfo(){
	var cookieEnabled=(navigator.cookieEnabled)?true:false;
	if(!cookieEnabled){
		alert("请开启浏览器COOKIE功能！");
		return false;
	}
	if($("uname").value==""||$("uname").value==null){
		alert("请输入用户名");
		$("uname").focus();
		return false;
	}
	if($("pass").value==""||$("pass").value==null){
		alert("请输入密码");
		$("pass").focus();
		return false;
	}
	document.forms.item(0).submit();
	document.forms.item(0).reset();
	$("loginbox").style.display=="none";
	return false;
}
V.addListener($("form1"),"submit",function(){
	return CheckInfo();
});
V.addListener($("btnSwapLogin"),"mouseover",function(){
	O.addClass($("btnSwapLogin"),"btn03aHover");
});
V.addListener($("btnSwapLogin"),"mouseout",function(){
	O.removeClass($("btnSwapLogin"),"btn03aHover");
});
V.addListener($("btnSwapLogin"),"click",function(){
	($("loginbox").style.display=="block")?($("loginbox").style.display="none"):($("loginbox").style.display="block");
});
function cb_login(){
	$("welcome").style.display="none";
	$("loginbox").style.display="";
}
var sid=C.getCookie("sid");
function cb_userdetail(){
	var msg='欢迎您 <span class="name" id="username">'+sso_username+'</span><span class="quit"><a href="http://my.ifeng.com/?_c=index&_a=logout&backurl='+escape(window.location.href)+'" id="btnlogout">退出</a></span><ul id="enterPro" style="display:none;"><li><a href="http://my.ifeng.com" target="_blank">进入个人中心</a></li><li><a href="http://t.ifeng.com" target="_blank">进入微博</a></li><li><a href="http://blog.ifeng.com/user/personal_entrance.php?n='+encodeURIComponent(sso_username)+'" target="_blank">进入我的博客</a></li><li><a href="http://bbs.ifeng.com/space.php?action=viewpro&username='+encodeURIComponent(sso_username)+'" target="_blank">进入我的论坛</a></li></ul>';
	$("welcome").innerHTML=msg;
	$("login").style.display="none";
	$("welcome").style.display="block";
}
if(sid&&sid.length>32){
	var d=new Date();
	getScript("http://blog.ifeng.com/misc.php?script=getusername&tm="+d.getTime());
}
getScript("http://m0.ifengimg.com/keywords.js",function(){
	getScript("http://y1.ifengimg.com/www/index_nav_search_v130305r1.js",function(){
		V.addListener($("keyword"),"click",function(){
			header_search.clean_default($("keyword").value);
		});
		V.addListener($("keyword"),"blur",function(){
			header_search.set_default($("keyword").value);
		});
		V.addListener($("loginUl"),"click",function(){
			header_search.show_option();
		});
		V.addListener($("loginFldselectop"),"mouseout",function(){
			header_search.out_option();
		});
		V.addListener($("loginFldselectop"),"mouseover",function(){
			header_search.over_option();
		});
		var searchCategory=$("loginFldselectop").getElementsByTagName("li");
		for( var i=0;i<searchCategory.length;i++){
			V.addListener(searchCategory[i],"mouseover",function(e){
				(e.target||e.srcElement).style.backgroundColor="#e7e7e7";
			});
			V.addListener(searchCategory[i],"mouseout",function(e){
				(e.target||e.srcElement).style.backgroundColor="white";
			});
			V.addListener(searchCategory[i].getElementsByTagName("a")[0],"click",function(e){
				header_search.select_option((e.target||e.srcElement).innerHTML);
			});
		}
		V.addListener($("suggest_list"),"focus",function(){
			finance_suggest.display("inline");
		});
		V.addListener($("suggest_list"),"mouseover",function(){
			finance_suggest.display("inline");
		});
		V.addListener($("suggest_list"),"mouseout",function(){
			finance_suggest.display("none");
		});
	});
});
function Collection(){
	this.items=[];
}
V.addListener($("btnSearch1"),"mouseover",function(){
	O.addClass($("btnSearch1"),"button_hover");
});
V.addListener($("btnSearch2"),"mouseover",function(){
	O.addClass($("btnSearch2"),"button_hover");
});
V.addListener($("btnLogin"),"mouseover",function(){
	O.addClass($("btnLogin"),"btn04_hover");
});
V.addListener($("btnLogin"),"mouseout",function(){
	O.removeClass($("btnLogin"),"btn04_hover");
});
V.addListener($("btnSearch1"),"mouseout",function(){
	O.removeClass($("btnSearch1"),"button_hover");
});
V.addListener($("btnSearch2"),"mouseout",function(){
	O.removeClass($("btnSearch2"),"button_hover");
});
Collection.prototype={
  add:function(col){
	  this.items.push(col);
  },
  clear:function(){
	  this.items=[];
  },
  getCount:function(){
	  return this.items.length;
  },
  each:function(func){
	  for( var i=0;i<this.getCount();i++){
		  func(this.items[i]);
	  }
  },
  indexOf:function(item){
	  var r=-1;
	  for(i=0;i<this.getCount();i++){
		  if(item==this.items[i]){
			  r=i;
			  break;
		  }
	  }
	  return r;
  },
  find:function(func){
	  var r=null;
	  for( var i=0;i<this.getCount();i++){
		  if(func(this.items[i])==true){
			  r=this.items[i];
			  break;
		  }
	  }
	  return r;
  }
}
function TabPage(triggerId,sheetId){
	this.trigger=$(triggerId);
	this.sheet=$(sheetId);
}
function TabControl(){
	this.styleName=null;
	this.tabPages=new Collection();
	this.currentTabPage=null;
	this.triggerType="click";
	this.defaultPage=0;
	this.enableSlide=false;
	this.slideInterval=3000;
	this.onChanging=new Collection();
	this.onChanging.add(this.defaultChangingHandler);
	this.onInit=new Collection();
	this.onInit.add(this.defaultInitHandler);
	this.onInit.add(this.autoSlideInitHandler);
	this.onAdding=new Collection();
	this.onAdding.add(this.defaultAddingHandler);
	this._autoSlideEv=null;
	this._preButton=null;
	this._nextButton=null;
}
TabControl.prototype={
  add:function(tabPage){
	  this.tabPages.add(tabPage);
	  var handler=function(func){
		  func(tabPage);
	  };
	  this.onAdding.each(handler);
  },
  addRange:function(triggers,sheets){
	  if(triggers.length==0||triggers.length!=sheets.length){
		  return;
	  }
	  for( var i=0;i<triggers.length;i++){
		  var tabPage=new TabPage(triggers[i],sheets[i]);
		  this.add(tabPage);
	  }
  },
  pre:function(){
	  var i=this.indexOf(this.currentTabPage.trigger);
	  this.select(i-1);
  },
  next:function(){
	  var i=this.indexOf(this.currentTabPage.trigger);
	  this.select(i+1);
  },
  defaultAddingHandler:function(tabPage){},
  init:function(){
	  var _=this;
	  var handler=function(func){
		  func(_);
	  };
	  if(this.tabPages.getCount()==0){
		  return;
	  }
	  if(this.currentTabPage==null){
		  this.currentTabPage=this.tabPages.items[this.defaultPage];
	  }
	  this.onInit.each(handler);
	  if($(this.preButton))
		  $(this.preButton).onclick=this.GetFunction(this,"pre");
	  if($(this.nextButton))
		  $(this.nextButton).onclick=this.GetFunction(this,"next");
  },
  defaultInitHandler:function(obj){
	  var handler=function(item){
		  V.addListener(item.trigger,obj.triggerType,obj.selectHanlder,obj);
		  O.hide(item.sheet);
	  };
	  obj.tabPages.each(handler);
	  obj.select(obj.defaultPage);
  },
  autoSlideInitHandler:function(o){
	  if(!o.enableSlide){
		  return;
	  }
	  var delayStartEv=null;
	  var delayStartHandler=function(){
		  delayStartEv=setTimeout(function(){
			  o.autoSlideHandler(o);
		  },300);
	  };
	  var clearHandler=function(){
		  clearTimeout(delayStartEv);
		  clearInterval(o._autoSlideEv);
	  };
	  var handler=function(item){
		  V.addListener(item.trigger,o.triggerType,clearHandler,o);
		  V.addListener(item.sheet,'mouseover',clearHandler,o);
		  V.addListener([item.trigger,item.sheet],'mouseout',delayStartHandler,o);
	  };
	  o.tabPages.each(handler);
	  o.autoSlideHandler(o);
  },
  autoSlideHandler:function(o){
	  var count=o.tabPages.getCount();
	  clearInterval(o._autoSlideEv);
	  o._autoSlideEv=setInterval(function(){
		  var i=o.indexOf(o.currentTabPage.trigger);
		  if(i==-1){
			  return;
		  }
		  i++;
		  if(i>=count){
			  i=0;
		  }
		  o.select(i);
	  },o.slideInterval);
  },
  selectHanlder:function(e,o){
	  var i=this.indexOf(o);
	  this.select(i);
  },
  select:function(i){
	  var page=null;
	  if(this.autoLoop){
		  if(i<0){
			  page=this.tabPages.items[this.tabPages.getCount()-1];
		  }else if(i>=this.tabPages.getCount()){
			  page=this.tabPages.items[0];
		  }else{
			  page=this.tabPages.items[i];
		  }
	  }else{
		  if(i<0||i>=this.tabPages.getCount()){
			  return;
		  }
		  page=this.tabPages.items[i];
	  }
	  var _=this;
	  var handler=function(func){
		  func(_.currentTabPage,page);
	  };
	  this.onChanging.each(handler);
	  this.currentTabPage=page;
	  if($(this.preButton)){
		  $(this.preButton).className="enable";
		  if(i==0)
			  $(this.preButton).className="unenable";
	  }
	  if($(this.nextButton)){
		  $(this.nextButton).className="enable";
		  if(i==this.tabPages.getCount()-1)
			  $(this.nextButton).className="unenable";
	  }
	  if(typeof (this.onComplete)=="function"){
		  this.onComplete(this.options,i,this.currentTabPage);
	  }
  },
  defaultChangingHandler:function(oldTabPage,newTabPage){
	  if(oldTabPage.sheet){
		  O.hide(oldTabPage.sheet);
	  }
	  if(newTabPage.sheet){
		  O.show(newTabPage.sheet);
	  }
	  O.removeClass(oldTabPage.trigger,'current');
	  O.addClass(newTabPage.trigger,'current');
  },
  indexOf:function(trigger){
	  var r=-1;
	  var handler=function(item){
		  return item.trigger==trigger;
	  };
	  var item=this.tabPages.find(handler);
	  if(item!=null){
		  r=this.tabPages.indexOf(item);
	  }
	  return r;
  },
  GetFunction:function(variable,method,param){
	  return function(){
		  variable[method](param);
	  }
  }
}
function TabControlForFocusImg(){
	this.autoImpressioned = 0;
	this.fashionImpressioned = 0;
	this.autoImpression = '';
	this.fashionImpression = '';
	this.styleName=null;
	this.tabPages=new Collection();
	this.currentTabPage=null;
	this.triggerType="click";
	this.defaultPage=0;
	this.enableSlide=false;
	this.slideInterval=3000;
	this.onChanging=new Collection();
	this.onChanging.add(this.defaultChangingHandler);
	this.onInit=new Collection();
	this.onInit.add(this.defaultInitHandler);
	this.onInit.add(this.autoSlideInitHandler);
	this.onAdding=new Collection();
	this.onAdding.add(this.defaultAddingHandler);
	this._autoSlideEv=null;
	this._preButton=null;
	this._nextButton=null;
}
TabControlForFocusImg.prototype={
  add:function(tabPage){
	  this.tabPages.add(tabPage);
	  var handler=function(func){
		  func(tabPage);
	  };
	  this.onAdding.each(handler);
  },
  addRange:function(triggers,sheets){
	  if(triggers.length==0||triggers.length!=sheets.length){
		  return;
	  }
	  for( var i=0;i<triggers.length;i++){
		  var tabPage=new TabPage(triggers[i],sheets[i]);
		  this.add(tabPage);
	  }
  },
  pre:function(){
	  var i=this.indexOf(this.currentTabPage.trigger);
	  this.select(i-1);
  },
  next:function(){
	  var i=this.indexOf(this.currentTabPage.trigger);
	  this.select(i+1);
  },
  defaultAddingHandler:function(tabPage){},
  init:function(){
	  var _=this;
	  var handler=function(func){
		  func(_);
	  };
	  if(this.tabPages.getCount()==0){
		  return;
	  }
	  if(this.currentTabPage==null){
		  this.currentTabPage=this.tabPages.items[this.defaultPage];
	  }
	  this.onInit.each(handler);
	  if($(this.preButton))
		  $(this.preButton).onclick=this.GetFunction(this,"pre");
	  if($(this.nextButton))
		  $(this.nextButton).onclick=this.GetFunction(this,"next");
  },
  defaultInitHandler:function(obj){
	  var handler=function(item){
		  V.addListener(item.trigger,obj.triggerType,obj.selectHanlder,obj);
		  O.hide(item.sheet);
	  };
	  obj.tabPages.each(handler);
	  obj.select(obj.defaultPage);
  },
  autoSlideInitHandler:function(o){
	  if(!o.enableSlide){
		  return;
	  }
	  var delayStartEv=null;
	  var delayStartHandler=function(){
		  delayStartEv=setTimeout(function(){
			  o.autoSlideHandler(o);
		  },300);
	  };
	  var clearHandler=function(){
		  clearTimeout(delayStartEv);
		  clearInterval(o._autoSlideEv);
	  };
	  var handler=function(item){
		  V.addListener(item.trigger,o.triggerType,clearHandler,o);
		  V.addListener(item.sheet,'mouseover',clearHandler,o);
		  V.addListener([item.trigger,item.sheet],'mouseout',delayStartHandler,o);
	  };
	  o.tabPages.each(handler);
	  o.autoSlideHandler(o);
  },
  autoSlideHandler:function(o){
	  var count=o.tabPages.getCount();
	  clearInterval(o._autoSlideEv);
	  o._autoSlideEv=setInterval(function(){
		  var i=o.indexOf(o.currentTabPage.trigger);
		  if(i==-1){
			  return;
		  }
		  i++;
		  if(i>=count){
			  i=0;
		  }
		  o.select(i);
	  },o.slideInterval);
  },
  selectHanlder:function(e,o){
	  var i=this.indexOf(o);
	  this.select(i);
  },
  select:function(i){
	var imgImpressionForAd = '';
	if(i == 3) {
		if(this.autoImpression != '' && this.autoImpressioned == 0){
			imgImpressionForAd = document.createElement('img');
			imgImpressionForAd.style.display = 'none';
			imgImpressionForAd.src = this.autoImpression;
			document.body.appendChild(imgImpressionForAd);
			this.autoImpressioned = 1;
		}
	}else if (i == 4) {
		if(this.fashionImpression != '' && this.fashionImpressioned == 0){
			 imgImpressionForAd = document.createElement('img');
			 imgImpressionForAd.style.display = 'none';
			 imgImpressionForAd.src = this.fashionImpression;
			 document.body.appendChild(imgImpressionForAd);
			 this.fashionImpressioned = 1;
		}
	}
	  var page=null;
	  if(this.autoLoop){
		  if(i<0){
			  page=this.tabPages.items[this.tabPages.getCount()-1];
		  }else if(i>=this.tabPages.getCount()){
			  page=this.tabPages.items[0];
		  }else{
			  page=this.tabPages.items[i];
		  }
	  }else{
		  if(i<0||i>=this.tabPages.getCount()){
			  return;
		  }
		  page=this.tabPages.items[i];
	  }
	  var _=this;
	  var handler=function(func){
		  func(_.currentTabPage,page);
	  };
	  this.onChanging.each(handler);
	  this.currentTabPage=page;
	  if($(this.preButton)){
		  $(this.preButton).className="enable";
		  if(i==0)
			  $(this.preButton).className="unenable";
	  }
	  if($(this.nextButton)){
		  $(this.nextButton).className="enable";
		  if(i==this.tabPages.getCount()-1)
			  $(this.nextButton).className="unenable";
	  }
	  if(typeof (this.onComplete)=="function"){
		  this.onComplete(this.options,i,this.currentTabPage);
	  }
  },
  defaultChangingHandler:function(oldTabPage,newTabPage){
	  if(oldTabPage.sheet){
		  O.hide(oldTabPage.sheet);
	  }
	  if(newTabPage.sheet){
		  O.show(newTabPage.sheet);
	  }
	  O.removeClass(oldTabPage.trigger,'current');
	  O.addClass(newTabPage.trigger,'current');
  },
  indexOf:function(trigger){
	  var r=-1;
	  var handler=function(item){
		  return item.trigger==trigger;
	  };
	  var item=this.tabPages.find(handler);
	  if(item!=null){
		  r=this.tabPages.indexOf(item);
	  }
	  return r;
  },
  GetFunction:function(variable,method,param){
	  return function(){
		  variable[method](param);
	  }
  }
}

var tcSlide=new TabControl();
tcSlide.addRange($("controlSlide").getElementsByTagName("li"),O.getElementsByClassName($("tabSlide"),"tabDiv","div"));
tcSlide.preButton='prevSlide';
tcSlide.nextButton='nextSlide';
tcSlide.triggerType="mouseover";
tcSlide.autoLoop=true;
tcSlide.enableSlide=true;
tcSlide.slideInterval=5000;
tcSlide.init();
V.addListener($("tabSlide"),"mouseover",function(){
	O.show($("prevSlide"));
	O.show($("nextSlide"));
});
V.addListener($("tabSlide"),"mouseout",function(){
	O.hide($("prevSlide"));
	O.hide($("nextSlide"));
});
$("prevSlide").onmouseover=function(){
	O.setClass(this.getElementsByTagName("img")[0],"");
};
$("nextSlide").onmouseover=function(){
	O.setClass(this.getElementsByTagName("img")[0],"");
};
$("prevSlide").onmouseout=function(){
	O.setClass(this.getElementsByTagName("img")[0],"imgHover");
};
$("nextSlide").onmouseout=function(){
	O.setClass(this.getElementsByTagName("img")[0],"imgHover");
};
V.addListener($("financeKeyword"),"focus",function(){
	if($("financeKeyword").value=="代码/拼音/名称")
		$("financeKeyword").value="";
});
V.addListener($("financeKeyword"),"blur",function(){
	if($("financeKeyword").value=="")
		$("financeKeyword").value="代码/拼音/名称";
});
var channelTab=new TabControl();
channelTab.addRange(["c1","c2","c3"],["content1","content2","content3"]);
channelTab.triggerType="mouseover";
channelTab.init();
var tvTab=new TabControl();
tvTab.addRange(["m_label","g3_label"],["m_box","g3_box"]);
tvTab.triggerType="mouseover";
tvTab.init();
var imageSwap=new TabControl();
imageSwap.addRange($("imageSwapHandler").getElementsByTagName("li"),["gs1","gs2","gs3","gs4"]);
imageSwap.triggerType="mouseover";
imageSwap.init();