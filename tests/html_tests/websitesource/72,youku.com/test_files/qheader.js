(function(o){
if(!o || o.QHeader){ return; }

//global domain
document.domain = 'youku.com';

//define variable
var toDomain = function(s){ s = s.replace('http://', ''); if(s[s.length -1] == '/'){ s = s.substr(0, s.length-1); }; return s; }
var DOMAIN_NC = toDomain(nc_domain)
	,DOMAIN_NOTICE = toDomain(notice_domain)
	,LOADING = '<img src="http://static.youku.com/index/img/ico_loading_16.gif" width="16" height="16" border="0">';

//header class
var QHeader = {
	ids: {'headerbox': 'qheader_box', 'header': 'qheader'},
	dropmenuGroup: null,
	node: null,
	jsres: typeof(qheaderjs) == 'object' ? qheaderjs : null,
	ready: false,
	status: 'static',
	rule: 'fixed',
	init: function(){
		this.header = document.getElementById(this.ids.header);
		this.headerbox = document.getElementById(this.ids.headerbox);
		if(!this.header){ return; }
		//优先执行的功能不依赖资源加载
		this.Nav.findStick();//导航替换
		this.Search.init();//搜索功能
		//登录状态 common.js ready
		if(typeof(islogin) == 'function' && !islogin()){
			document.getElementById('qheader_logbefore').style.display = 'block';
		}
		this.bind();
		//依赖打印代码中的资源声明打印
		if(!this.jsres){ return; }
		var _this = this, canrun = false, runed = false;
		
		//运行时检测依赖脚本, 如加载立即运行
		var timer = setInterval(function(){
			if(_this.chkres('relyon')){ 
				canrun = true; 
				clearInterval(timer);
				if(!runed){
					_this.bindfns(); runed = true;
				}
			}
		}, 10);
		
		//domready后检测依赖脚本, 添加未包含的脚本, 并加载附加功能
		domReady(function(){
			clearInterval(timer); timer = null;
			canrun = canrun || _this.chkres('relyon');
			
			var addons = function(){
				_this.chkres('addons');
				_this.loadres('addons', function(){
					if(typeof(XBox) != 'undefined'){
						XBox.init({"site":14});	
						var f = document.getElementById('qheader_search');
						if(f){
							var b = f.getElementsByTagName('button')[0];
							if(b){
								addEvent(b, 'click', function(){
									_this.Search.doSearch();	
								});
							}
						}
					}
				});	
			}
			
			if(!canrun){
				_this.loadres('relyon', function(){
					var relyon = _this.jsres.relyon;
					for(var i=0; i<relyon.length; i++){
						if(relyon[i].ready !== true){ return; }
					}
					if(!runed){ _this.bindfns(); runed = true;}
					addons();
				});
			}else{
				if(!runed){ _this.bindfns(); runed = true; }
				addons();
			}
		});
	},
	bind: function(){
		var _this = this;
		addEvent(window, 'scroll', function(){
			if(_this.rule == 'fixed'){
				_this.changePos('scroll');
			}
		});
	},
	bindfns: function(){
		this.ready = true;
		this.dropmenuGroup = new DropmenuGroup();
		this.Userlog.init();
		this.Looking.init();
		this.Channel.init();
		this.Nav.init();
		this.Upload.init();
	},
	dofix: function(){
		return this.changeRule('fixed');
	},
	unfix: function(){
		return this.changeRule('static');
	},
	changeRule: function(rule){
		if(rule != this.rule){
			this.rule = rule;
			this.changePos('rule');
		}
		return this;	
	},
	changePos: function(type){
		var ready = typeof(Element) == 'function' ? true : false;//prototype ready
		var fixpos = false, inview = true;
		var scrolltop = document.documentElement ? document.documentElement.scrollTop + document.body.scrollTop : document.body.scrollTop;
		var posheader = getElementPos(this.header);
		
		if(scrolltop >= this.header.offsetHeight + posheader.y){ inview = false; }
		if(scrolltop > posheader.y){ fixpos = true; }
		
		var _this = this
			,header = ready ? Element.extend(this.header) : this.header
			,headerbox = ready ? Element.extend(this.headerbox) : this.headerbox;
		
		if(this.rule == 'fixed'){
			if(fixpos){
				if(this.status != 'fixed'){
					if(!inview && type == 'setrule'){
						if(ready){ headerbox.setStyle({'opacity': 0}); }
						headerbox.style.position = 'fixed';
						this.status = 'fixed';
						new FX(
							headerbox, { opacity: {to: 1} }, 
							0.2, 'linear', 
							function(){}
						).start();
					}else{
						headerbox.style.position = 'fixed';
						this.status = 'fixed';
					}
				}
			}else{
				if(this.status != 'static'){
					headerbox.style.position = 'relative';
					this.status = 'static';	
				}
			}	
		}else{
			if(this.status != 'static'){
				if(!inview){
					if(ready){ headerbox.setStyle({'opacity': 1}); }
					new FX(
						headerbox, { opacity: {to: 0} }, 
						0.2, 'linear', 
						function(){
							setTimeout(function(){
								if(ready){ headerbox.setStyle({'opacity': 1}); }
								headerbox.style.position = 'relative';
								_this.status = 'static';
							}, 25);
						}
					).start();
				}else{
					headerbox.style.position = 'relative';
					this.status = 'static';		
				}
			}
		}

		return this;
	},
	loadres: function(key, callback){
		var res = this.jsres[key];
		var _this = this;
		var callback = typeof(callback) == 'function' ? callback : function(){};
		for(var i=0; i<res.length; i++){
			(function(i){
				if(res[i].ready === false){
					_this.jsres[key][i].ready = 'loading';
					addScript(_this.jsres[key][i].src, function(){
						_this.jsres[key][i].ready = true;
						callback();
					});	
				}
			})(i);
		}
	},
	chkres: function(key){//同步加载状态下 检测依赖的JS资源
		var res = this.jsres[key];
		if(!res){ return true; }
		var _this = this;
		var scripts = document.getElementsByTagName('script');
		for(var i=0; i<scripts.length; i++){
			var script = scripts[i];
			for(var j=0; j<res.length; j++){
				if(script.src && script.src == res[j].src){

					(function(script, key, j){
						if(!_this.jsres[key][j].ready && eval(_this.jsres[key][j].condition)){
							_this.jsres[key][j].ready = true;	
						}
					})(script, key, j);
				}
			}	 	
		}
		for(var i=0; i<this.jsres[key].length; i++){
			if(this.jsres[key][i].ready !== true){
				return false;
			}
		}
		return true;
	}
}

QHeader.Userlog = {
	uid: 0,
	lock: false,
	first: true,
	init: function(){
		this.logbefore = $('qheader_logbefore');
		this.logafter = $('qheader_logafter');
		if(!this.logbefore || !this.logafter){ return; }
		var node =  this.logbefore.select('.dropdown')[0];
		if(!node){ return; }
		this.dp_before = new Dropmenu({
			'group': QHeader.dropmenuGroup,
			'node': node,
			'fire': 'hover'
		});
		
		this.update();
		this.bind();
	},
	bind: function(){
		var _this = this;
		$('qheader_login').observe('click', function(e){
			login(); preventDefault(e);	
		});
		$('qheader_login2').observe('click', function(e){
			login(); preventDefault(e);	
		});
		window['update_login_status_hook_qheader'] = function(){
			var uid = _this.getUID();
			if(!_this.first && (_this.uid != uid)){//第一次header自身获取信息， 其他区域登录且更换账号再次获取
				_this.update(); 
			}
			_this.first = false;
		}
	},
	show: function(){
		if(this.dp_after){ this.dp_after.show(); }
		return this;
	},
	hide: function(){
		if(this.dp_after){ this.dp_after.hide(); }
		return this;
	},
	getLogStatus: function(){
		if(islogin()){ return true; }
		return false;
	},
	update: function(){
		var st = this.getLogStatus();
		this.uid = this.getUID();
		if(st){
			this.logbefore.hide();
			this.logafter.show();
			this.getUserinfo();
		}else{
			this.logbefore.show();
			this.logafter.hide();
			this.uid = 0;			
		}
		return this;	
	},
	drawUserinfo: function(html){
		this.lock = false;
		if(html == 'null'){ //exception
			this.logbefore.show();
			this.logafter.hide().update('');
			this.uid = 0;
			return this;
		}
		this.logafter.update(html);
	
		//更新上传面板下的空间链接
		var href = $('qheader_spaceurl').href;
		$('qheader_upload_userspace').href = href;
				
		var username = $('qheader_username').select('a')[0];
		var name = truncate(username.innerHTML, 5, '...');
		username.update(name);
		//for IE6 not support max-width
		var w = 70;
		if(username.offsetWidth <= 70){ w = username.offsetWidth; }
		username.parentNode.style.width = w + 'px';
		
		var userphoto = $('qheader_userphoto');
		new FX(
			userphoto, { opacity: {to: 1} }, 
			0.4, 'linear', 
			function(){}
		).start();
		
		var node =  this.logafter.select('.dropdown')[0];
		if(!node){ return; }
		this.dp_after = new Dropmenu({
			'group': QHeader.dropmenuGroup,
			'node': node,
			'fire': 'hover'
		});
		$('qheader_logout').observe('click', function(e){
			logout(); preventDefault(e);	
		});
		//登录后开启通知更新
		QHeader.Notice.init();
		
		return this;
	},
	getUserinfo: function(){
		if(this.lock){ return; }
		this.lock = true;
		this.logafter.update('<div style="text-align:center;margin-top:22px;">'+ LOADING +'</div>');
		var url = 'http://'+ DOMAIN_NC + '/index_QHeaderJSONP?function[]=userinfo&callback[]=QHeader.Userlog.drawUserinfo'	
		addScript(url, null, true);
	},
	getUID: function(){
		if(!islogin()){ return 0; }
		var ckie = Nova.Cookie.get('yktk');
		var uid = 0;
		if(ckie){
			try{
				var u_info = decode64(decodeURIComponent(ckie).split('|')[3]);
				if(u_info.indexOf(',') > -1 && u_info.indexOf('nn:') > -1 && u_info.indexOf('id:') > -1){
					uid = u_info.split(',')[0].split(':')[1];
				}
			}catch(e){ }
		}
		
		return parseInt(uid);
	}
}

QHeader.Looking = {
	noticelock: false,
	init: function(){
		var node = $('qheader_looking');
		if(!node){ return; }
		this.dp = new Dropmenu({
			'group': QHeader.dropmenuGroup,
			'node': node,
			'fire': 'hover'
		});
		this.bind();
	},
	bind: function(){
		var _this = this;
		this.dp.setCallback('show', function(){ 
			if(!_this.noticelock){ _this.show(); }
		});
	},
	showNotice: function(params){
		if(!arguments[0] || !params.showid){ return this; }
		var pos_target = getElementPos(this.dp.handle)
			,pos_source = {x:0, y:0}
			,w = this.dp.handle.offsetWidth
			,h = this.dp.handle.offsetHeight;
		
		if(params.event){
			pos_source = getMousePos(params.event);
		}else if(params.element){
			pos_source = getElementPos(params.element);
		}
		var _this = this;
		var fake = new Element('div');
		fake.style.cssText = 'z-index:5000;zoom:1;position:absolute;top:'+ pos_source.y +'px;left:'+ pos_source.x +'px;width:10px;height:5px;overflow:hidden;background:#f1faef;border:1px solid #cbe090;opacity:1;filter:alpha(opacity=100);';
		document.body.appendChild(fake);
		new FX(
			fake, {
				opacity: {to: 0.8},
				width: {to: w-2},
				height: {to: h-2},
				left: {to: pos_target.x},
				top: {to: pos_target.y}
			}, 
			0.8, 
			'fadeIn', 
			function(){				
				fake.remove();
				_this.getNotice(params);
			}
		).start();
		
		return this;
	},
	show: function(){
		if(!this.dp){ return this; }
		this.getList();
		if(this.dp.getStatus() != 'show'){ this.dp.show(); }
		return this;
	},
	hide: function(){
		if(this.dp){ this.dp.hide(); }
		return this;
	},
	getList: function(){
		if(!this.dp){ return this; }
		this.dp.update('<div style="text-align:center;padding:33px 0;height:16px;overflow:hidden;">'+ LOADING +'</div>');
		var t = +new Date();
		var url = 'http://'+ DOMAIN_NC + '/index_QHeaderJSONP?function[]=looking&callback[]=QHeader.Looking.drawList&t=' + t;	
		addScript(url, null, true);
	},
	getNotice: function(params){
		if(!this.dp){ return this; }
		if(this.noticelock){ return this; }
		this.noticelock = true; 
		if(this.dp.getStatus() != 'show'){ this.dp.show(); }
		this.dp.update('<div style="text-align:center;padding:33px 0;height:16px;overflow:hidden;">'+ LOADING +'</div>');
		var t = +new Date();
		var url = 'http://'+ DOMAIN_NC + '/index_QHeaderJSONP?function[]=subnotice&callback[]=QHeader.Looking.drawNotice&sid='+ params.showid +'&t=' + t;	
		addScript(url, null, true);
	},
	drawList: function(html){
		this.dp.update(html);
		this.bindList();
		return this;
	},
	drawNotice: function(html){
		this.noticelock = false;
		this.dp.update(html);
		this.bindNotice();
		return this;
	},
	bindNotice: function(){
		var handle = $('qheader_looking_golist');
		var _this = this;
		if(handle){ 
			handle.observe('click', function(e){
				_this.show(); preventDefault(e);
			}); 
		}
		return this;	
	},
	bindList: function(){
		var container = $('qheader_looking_container')
			,panel = $('qheader_looking_panel')
			,list = $('qheader_looking_list')
			,nulll = $('qheader_looking_null')
			,_this = this
			,current = '';
		if(!container){ return; }
		
		var token = container.getAttribute('token');
		//同步遮罩
		var f5mask = function(){ _this.dp.f5mask(); }		
		//获取删除信息
		var delinfo = function(li){
			var info = li.getAttribute('delinfo');
			if(!info){ return null; }
			var data = eval('(' + decodeURIComponent(info) + ')');
			return data;
		}
		//检查是否被清空
		var chkempty = function(){
			var b1 = false, b2 = false;
			if(panel.select('li').length == 0){
				b2 = true;
				if(list){ list.remove(); list = null; }
				nulll.show();
			}
		}
		//删除列表项
		var remove = function(o){		
			var d =  delinfo(o);
			if(!d){ return this; }
			//删除本身
			o.remove();
			//删除相同项
			container.select('li').each(function(o1){
				var dd = delinfo(o1);
				if(dd){
					if(dd.videoid == d.videoid){
						o1.remove();
					}	
				}
			});
			
			var dl = 'http://yus.navi.youku.com/playlog/delete.json?'
					+ 'token=' + token
					+ '&v=' + d.videoid
					+ '&fid=' + d.folderid
					+ '&shid=' + d.showid
					+ '&' + Math.random();
			var img = new Image();
			img.src = dl;
			
			chkempty();
			
			return this;
		}
		//清空
		var clear = function(){
			list.remove();
			nulll.show();
			
			var cl = 'http://yus.navi.youku.com/playlog/clear.json?'
					+ 'token=' + token 
					+ '&' + Math.random();
			var img = new Image();
			img.src = cl;
		}
		
		//删除列表项
		var isTouch = ("createTouch" in document);
		container.select('li').each(function(o){
			var del = o.select('.r-del')[0];
			if(del){
				if(isTouch){
					del.show();
				}else{
				o.observe('mouseenter', function(){ o.addClassName('hover'); })
			 	 .observe('mouseleave', function(){ o.removeClassName('hover'); });
				}
				del.observe('click', function(){ remove(o); })
				   .observe('select', function(e){ Event.stop(e); });	
			} 
		});
		
		//列表滚动事件
		container.select('.records-list').each(function(o){ o.observe('scroll', function(e){ cancelBubble(e);} ) });
		
		return this;
	}
}

QHeader.Notice = {
	maxnum: 99,
	freq: 30000,
	polled: false,
	timer: null,
	itemDetails:{'comment_reply':{'label':'','count':0,'cpp':4004471,'url':'/u/comments/gt_reces_ct_2'},
                 'video_reply':{'label':'','count':0,'cpp':4004474,'url':'/u/comments/gt_reces_ct_2'},
                 'comment_mentions':{'label':'','count':0,'cpp':4004472,'url':'/u/comments/gt_reces_ct_1'},
                 'followers':{'label':'新粉丝','count':0,'cpp':4004476,'url':'/u/profile/type_friends_s_fans'},
                 'statuses_mentions':{'label':'新@提到我','count':0,'cpp':4004482,'url':'/u/home/type_mentions'},
                 'statuses_comments':{'label':'','count':0,'cpp':400483,'url':'/u/comments'},
                 'star_statuses':{'label':'明星动态更新','count':0,'url':'/u/router/?ut=1','cpp':4004479},
                 //'show_statuses':{'label':'剧集更新','count':0,'url':'/u/router/?ut=2','cpp':4004478},
                 'subscribe':{'label':'好友更新','count':0,'url':'/u/router/?ut=3','cpp':4006336},
                 'upvideo_succeed':{'cpp':4006884},
                 'upvideo_failed':{'cpp':4006884},
                 'upvideo_blocked':{'cpp':4006884},
                 'upvideo_unblocked':{'cpp':4006884},
                 'fee_expire':{'cpp':4007028},
                 'dms':{'count':0,'url':'/u/home/type_privatemsg','cpp':'4007612'},
                 'sysmsg': {'label': '系统消息', 'count': 0, 'url':'/u/home/type_sysmsg', 'cpp': '4007611'}
	},
	commentItems : ['sysmsg','dms','statuses_mentions','comment_reply','video_reply','comment_mentions','statuses_comments'],
	init: function(){
		this.notice =  $('qheader_notice');
		this.noticenum = $('qheader_noticenum'); 
		if(!this.notice || !this.noticenum){ return; }
		this.lks = [];
		if(islogin()){
			this.notice.show(); 
			this.start();
		}
	},
	show: function(){
		if(this.notice){ this.notice.show(); }
		return this;
	},
	hide: function(){
		if(this.notice){ this.notice.hide(); }
		return this;
	},
	update: function(){
		var uid = QHeader.Userlog.getUID();
		this.polled = false;
		if(uid){
			var ap = '{"uid":'+ uid +',"rand":'+ Math.random() + '}';	
			var cb =  'QHeader.Notice.drawNum';
			var url = 'http://'+ DOMAIN_NOTICE + '/notice/js_notify.json?__ap=' + ap + '&__callback=' + cb;
			addScript(url, null, true);
		}else{
			this.polled = true;	
		}
		return this;
	},
	updateNum: function(num){
		if(num == 0 || isNaN(num) || typeof(num) != 'number'){
			this.noticenum.update(0).hide();	
		}else{
			if(num > this.maxnum){ num = this.maxnum + '+'; }
			this.noticenum.update(num).show();
		}
		return this;
	},
	updateLink: function(info){
		if(info.notice){
			for(var item in info.notice){
				if(this.commentItems.include(item)){
					var url = 'http://'+UC_DOMAIN+this.itemDetails[item].url;
					this.lks.push({'item':item,'href':url});
					
				}
			}
		}
		var link = this.notice.select('a')[0];
		if(this.lks.length > 0){
			link.href = this.lks[0].href;
			link.setAttribute('attr',this.lks[0].item);
		}
	},
	changeLink: function(item){
		if(!item){return;}
		var link = this.notice.select('a')[0];
		if(this.lks.length >0){
			this.lks.each(function(o){
					if(o.item != item){
						link.href = o.href;	
					}});
		}
	},
	drawNum: function(info){
		this.polled = true;
		if(!info){ return; }
		try{
			if(typeof(info)!="object"){
				info  = JSON.parse(info);
			}
		}catch(e){return;}
		
		var total = total2 = 0;
		for(var item in this.itemDetails){
			//新消息总数
			total += (  info.notice[item]==null 
						|| info.notice[item]=='undefined' 
						|| !this.commentItems.include(item) 
					) ? 0 : parseInt(info.notice[item]);
			total2 += (  info.notice[item]==null 
						|| info.notice[item]=='undefined' 
						|| ['show_statuses'].include(item) 
						|| (['star_statuses','subscribe'].include(item) && (parseInt(info.notice[item+"_reset"]) < 3600))
					) ? 0 : parseInt(info.notice[item]);

			
		}
		
		this.updateNum(total);
		this.total = total;
		this.lks = [];
		this.updateLink(info);
		QHeader.NoticeDetail.update(info,total2);
		
		return this;
	},
	start: function(){
		if(this.timer){ return; }
		this.update();
		var _this = this;
		this.timer = setInterval(function(){
			if(_this.polled){
				_this.update();
			}
		}, this.freq); 	
		return this;
	},
	stop: function(){
		if(this.timer){
			clearInterval(this.timer);
			this.timer = null;	
		}
		return this;
	}
}

QHeader.NoticeDetail = {
	init: function(info){
		this.bindClose();
		this.bindClick(info);
		this.container = $('yk-msgbox');
	},
	update:function(info,total){
		if(!this.config.init){
			this.config.init = true;
			this.init(info);
		}
		this.tmpNotice = [];
		this.cmt_reply_count = 0;
		for(var item in QHeader.Notice.itemDetails){
			if(info.notice[item]==null || info.notice[item]=='undefined') info.notice[item] = 0;
			if(['star_statuses','subscribe','show_statuses'].include(item)){
				QHeader.Notice.itemDetails[item].count =  (typeof info.notice[item+"_reset"] != 'undefined' && parseInt(info.notice[item+"_reset"]) >= 3600) ? parseInt(info.notice[item]) : 0;
			}else{
				QHeader.Notice.itemDetails[item].count =  parseInt(info.notice[item]);
			}
			if(info.notice[item] > 0){
				//视频评论相关的提醒
				if(['statuses_comments','comment_reply','video_reply','comment_mentions'].include(item)){
					this.cmt_reply_count += parseInt(info.notice[item]);
					if(parseInt(info.notice[item]) > 0 && item != 'statuses_comments') var cmt_href = item;
				}else{
					if(['subscribe','star_statuses','show_statuses','upvideo_succeed','upvideo_failed','upvideo_blocked','upvideo_unblocked','fee_expire','dms'].include(item)){
						this.request_detail = true;
					}
					if(['star_statuses','show_statuses','subscribe'].include(item)){
						if(typeof info.notice[item+"_reset"] != 'undefined' && parseInt(info.notice[item+"_reset"]) >= 3600){
							this.tmpNotice.push({'item':item,'count':info.notice[item]});
						}
					}else{
						this.tmpNotice.push({'item':item,'count':info.notice[item]});
					}
				}
			}
		}
		this.total = total;
		this.show_statuses_num  = (info.notice['show_statuses']) ? parseInt(info.notice['show_statuses']) : 0;
		this.config.call = this.config.call+1;
		if(this.cmt_reply_count > 0) this.tmpNotice.unshift({'item':'cmt_reply','count':this.cmt_reply_count,'href':cmt_href});
		if(this.show_statuses_num ==0 && this.total > 0){this.config.cases = 1;this.otherStatusLabel();return;};
		if(this.show_statuses_num > 0 && this.total == 0){this.config.cases = 2;this.showStatusLabel(total);return;};
		if(this.show_statuses_num > 0 && this.total > 0){
			if(this.config.call > 1){
				if(this.config.cases != 2){
					this.otherStatusLabel();return;
				}
			}else{
				if(([true,false])[(parseInt(10*Math.random())+1)%2 == 0 ? 0 : 1] == true){
					this.config.cases = 2;
					this.showStatusLabel(total);return;
				}else{
					this.config.cases = 1;
					this.otherStatusLabel();return;
				}
			}
		}

	},
	config: {'cases':0,'call':0,'init':false},
	bindClose: function(){
		var handle = $('close_handle');
		if(handle){
			handle.observe('click',function(e){
					handle.up().style.display='none';
					if(QHeader.NoticeDetail.config.cases == 1){
						QHeader.NoticeDetail.close();
					}else{
						QHeader.NoticeDetail.closeStatus();
					}
				});
		}
	},
	bindClick: function(info){
		var tips1 = $('tab_msg');
		var tips2 = $('notice_show');
		var msgbox = $('yk-msgbox');
		if(!tips1 || !tips2) return;
		var _this = this;
		[tips1,tips2].each(function(tips){
			tips.observe('click',function(event){
			if(event.target.tagName === 'A'){
				var element = Event.element(event);
				var type = event.target.getAttribute('attr');
				if(type){
					tips.style.display='none';
					msgbox.style.display='none';
					_this.clickLabel(type,event.target);
				}
			}
		});

		});
		QHeader.Notice.notice.observe('click',function(event){
					var link = QHeader.Notice.notice.select('a')[0];
					var type = link.getAttribute('attr');
					if(type){
						var allNum = parseInt(QHeader.Notice.noticenum.innerText);
						if(info.notice[type]){
							var num = info.notice[type];
							allNum = (allNum - num) > 0 ? (allNum - num) : 0;
							QHeader.Notice.updateNum(allNum);
							if(QHeader.Notice.lks.length > 0){
								QHeader.Notice.lks.shift();
								if(QHeader.Notice.lks.length > 0){
									link.href = QHeader.Notice.lks[0].href;
									link.setAttribute('attr',QHeader.Notice.lks[0].item);
								}
							}
						}
					}
				});
		
	},
	clickLabel: function(type,ele){
		QHeader.NoticeDetail.callUpdateNum(type);
		QHeader.NoticeDetail.clearSelf(ele);
	},
	showStatusLabel: function(total){
		var uid = QHeader.Userlog.getUID();
		if(!uid) return;
		var ap = '{"uid":'+ uid +',"rand":'+ Math.random() + '}';	
		var cb =  'QHeader.NoticeDetail.parseShowItem';
		var url = 'http://'+ DOMAIN_NOTICE + '/notice/js_detail.json?__ap=' + ap + '&__callback=' + cb;
		addScript(url, null, true);
	},
	otherStatusLabel: function(){
		if(this.tmpNotice.length == 0) return;
		if(!this.request_detail){
			this.noticeItemMerge(this.parseCommonItem(),[]);
		}else{
			var uid = QHeader.Userlog.getUID();
			if(!uid) return;
			var ap = '{"uid":'+ uid +',"rand":'+ Math.random() + '}';	
			var cb =  'QHeader.NoticeDetail.parseDetailItem';
			var url = 'http://'+ DOMAIN_NOTICE + '/notice/js_detail.json?__ap=' + ap + '&__callback=' + cb;
			addScript(url, null, true);
		}
		return;
	},
	noticeItemMerge:function(part1,part2){
		var len1 = len2 = 0;
		var len1 = part1.length;
		var len2 = part2.length;
		$('notice_show').style.display='none';
		if(len1 == 0 && len2 == 0){
			$('tab_msg').style.display='none'; return;
		}else
			$('tab_msg').style.display='block';
		if(len2 > 0){
			var n = Math.floor(Math.random() * len2 + 1) -1;  
			var randOne = part2[n];
			if(len1 == 0){
				this.container.select('div.videomsg')[0].setStyle({'border-bottom':'0px'});
			}
			this.container.select('div.videomsg')[0].update(randOne).show();
		}else{
			this.container.select('div.videomsg')[0].hide();
		}
		if(len1 >0 ){
			var msglist = part1[0];
			if(len1 == 2){
				msglist += '<span class="break">|</span>'+part1[1];
			}else if(len1 == 3){
				msglist += '<span class="break">|</span>'+part1[1];
				msglist += '<span class="break">|</span>'+part1[2];
			}else if(len1 > 3){
				msglist += '<span class="break">|</span>'+part1[1];
				msglist += '<span class="break">|</span><a attr="other" target="_blank" href="http://'+UC_DOMAIN+QHeader.Notice.itemDetails.statuses_comments.url+'">其他通知<b>'+this.max99(this.other_count)+'</b></a>'; 
			}
			this.container.select('div.msglist')[0].update(msglist).show();
			if(len2 > 0) {
				this.container.select('div.videomsg')[0].setStyle({'border-bottom':'1px dotted #DFDFDF'});
			}
		}else{
			this.container.select('div.msglist')[0].hide();
		}
		this.container.style.display='block';
		this.f5mask();
	},
	f5mask: function(){
		var container = $('yk-msgbox');
		var mask = container.select('iframe')[0];
		if(!mask){
			var attr = {'scrolling': '0', 'frameborder': '0', 'width': 0, 'height': 0};
			var style = {'position': 'absolute', 'top': '-1px', 'left': '-1px', 'zIndex': -1, 'opacity': 0};
			mask = new Element('iframe').writeAttribute(attr).setStyle(style);
			container.appendChild(mask);
		}
		var w = container.offsetWidth
			,h = container.offsetHeight
		mask.writeAttribute({'width': w+'px', 'height': h+'px'});
	},
	bulidLabel:function(ret,attr,index){
		var span_new= '<span class="hint"><i class="ico-new">新</i></span>';
		var span_suc='<span class="hint"><i class="ico-success">上传成功</i></span>';
		var span_failed ='<span class="hint"><i class="ico-notice">发布失败</i></span>';
		var label = null;
		var len = this.tmpNotice[index].count;
		switch(attr){
			case 'subscribe':
				var nick_name = ret.notice[attr][0].n;
				if(ret.notice[attr].length >= 1){
					var upinfo = ' 等好友有'+len+'条视频更新';
					var trun_len = 18-upinfo.length;
					var action = truncate(nick_name,trun_len,'...')+upinfo;
					var label = span_new+'<a attr="subscribe" href="http://'+UC_DOMAIN+'/u/home?ut=3&mn='+len+'" target="_blank">'+action+'</a><a class="flink" attr="subscribe" href="http://'+UC_DOMAIN+'/u/home?ut=3&mn='+len+'" target="_blank"> 查看</>';
				}
				break;
			case 'star_statuses':
				var nick_name = ret.notice[attr][0].n;
				var title = ret.notice[attr][0].t;
				var action_type = ret.notice[attr][0].a;
				var id = ret.notice[attr][0].s;
				if(ret.notice[attr].length >= 1){
					if (len == 1 && ret.notice[attr].length == 1){
						if (action_type == 1){
							var action = nick_name+'：参加了'+title+'';
						}else if(action_type == 2){
							var action = nick_name+'：作品'+title+'上映';
						}else{
							var action = nick_name+'：有了新动态';
						}
					}else{
						var action = nick_name+' 等'+len+'位明星有最新动向';
					}
					if(action.length > 18)
						action = truncate(action,18,'...');
					var label = span_new+'<span attr="star_statuses">'+action+'</span>';
					if(action_type == 3){
						var label = span_new+'<a attr="star_statuses" href="http://v.youku.com/v_show/id_'+id+'" target="_blank">'+action+'</a><a class="flink" target="_blank"  attr="star_statuses" href="http://v.youku.com/v_show/id_'+id+'" > 查看</a>';
					}
				}
				break;
			case 'upvideo_succeed':
				var play_url = 'http://v.youku.com/v_show/id_';
				var video_title = ret.notice[attr][0].t;
				var video_id = ret.notice[attr][0].v;
				var trun_video_title = truncate(video_title,10,'...');
				if(len > 1)
					var inf = '你有'+len+'个视频发布成功';
				else
					var inf = '《'+trun_video_title+'》发布成功';
				var label = span_suc+'<a attr="upvideo_succeed" href="http://'+UC_DOMAIN+'/u/videos/" target="_blank"><span>'+inf+'</span></a><a class="flink" attr="upvideo_succeed" href="http://'+UC_DOMAIN+'/u/videos/" target="_blank"> 查看记录</a>';
				break;
			case 'upvideo_failed':
				var video_title = ret.notice[attr][0].t;
				var video_id = ret.notice[attr][0].v;
				var trun_video_title = truncate(video_title,10,'...');
				if(len > 1)
					var inf = '你有'+len+'个视频发布失败';
				else
					var inf = '《'+trun_video_title+'》发布失败';
				var label = span_failed+'<a attr="upvideo_failed" href="http://'+UC_DOMAIN+'/u/videos/lost" target="_blank">'+inf+'</a><a class="flink"  attr="upvideo_failed" href="http://'+UC_DOMAIN+'/u/videos/lost" target="_blank"> 查看原因</a>';
				break;
			case 'upvideo_blocked':
				var video_title = ret.notice[attr][0].t;
				var video_id = ret.notice[attr][0].v;
				var trun_video_title = truncate(video_title,11,'...');
				if(len > 1)
					var inf = '你有'+len+'个视频被屏蔽';
				else
					var inf = '《'+trun_video_title+'》被屏蔽';
				var label = span_failed+'<a attr="upvideo_blocked" href="http://'+UC_DOMAIN+'/u/videos/lost" target="_blank">'+inf+'</a><a class="flink" attr="upvideo_blocked" href="http://'+UC_DOMAIN+'/u/videos/lost" target="_blank"> 查看原因</a>';
				break;
			case 'upvideo_unblocked':
				var video_title = ret.notice[attr][0].t;
				var video_id = ret.notice[attr][0].v;
				var trun_video_title = truncate(video_title,11,'...');
				if(len > 1)
					var inf = '你有'+len+'个视频被解除屏蔽';
				else
					var inf = '《'+trun_video_title+'》被解除屏蔽';
				var label = span_suc+'<a attr="upvideo_unblocked" href="http://'+UC_DOMAIN+'/u/videos/" target="_blank">'+inf+'</a><a class="flink"  attr="upvideo_unblocked" href="http://'+UC_DOMAIN+'/u/videos/" target="_blank"> 查看原因</a>';
				break;
			case 'fee_expire':
				var b = ret.notice[attr][0].b;
				var u = ret.notice[attr][0].u;
				var a = truncate(ret.notice[attr][0].a,22-b.length,'...');
				var label = span_failed+'<a attr="fee_expire" href="'+u+'" target="_blank"><span>'+a+'</span> '+b+'</a>';
				break;
			case 'dms':
				var sender = truncate(ret.notice[attr][0].n,8,'...');
				if(len == 1)
					var inf = sender+'给你发送1条站内信';
				else
					var inf = sender+'等给你发送'+len+'条站内信';
				var label = span_new+'<a attr="dms" href="http://'+UC_DOMAIN+QHeader.Notice.itemDetails.dms.url+'" target="_blank">'+inf+'</a><a class="flink"  attr="dms" href="http://'+UC_DOMAIN+QHeader.Notice.itemDetails.dms.url+'" target="_blank"> 查看</a>';
				break;
			default:
				var label = '';
		}
		return label;
	},

	parseCommonItem:function(){
		//no need call server for detail
		var detail = this.tmpNotice;
		if(detail.length == 0) return [];
		var storage = [];
		var partItemTotal = 0
		for(var j=0;j<detail.length;j++){
				if(detail[j].item == 'cmt_reply'){
					switch(detail[j].href){
						case 'comment_reply':
							 var subtype = 2;
							 break;
						case 'video_reply':
							 var subtype = 1;
							 break;
						case 'comment_mentions':
							 var subtype = 3;
							 break;
						default:
							 var subtype = 2;
					}
					var labels = '<a attr="comment_all" target="_blank" href="http://'+UC_DOMAIN+QHeader.Notice.itemDetails.statuses_comments.url+'" >新评论<b>'+this.max99(detail[j].count)+'</b></a>';
					storage.push(labels);
					if(storage.length > 2)
						partItemTotal += detail[j].count; 
				}else{
					var index = detail[j].item;
					if(['followers','statuses_mentions', 'sysmsg'].include(index)){
						if(index == 'followers'){
							var labels = '<a  attr='+index+' target="_blank" href="http://'+UC_DOMAIN+QHeader.Notice.itemDetails.followers.url+'">'+QHeader.Notice.itemDetails[index].label+'<b>'+this.max99(detail[j].count)+'</b></a>';
						}else if(index == 'statuses_mentions'){
							var labels = '<a  attr='+index+' target="_blank" href="http://'+UC_DOMAIN+QHeader.Notice.itemDetails.statuses_mentions.url+' ">'+QHeader.Notice.itemDetails[index].label+'<b>'+this.max99(detail[j].count)+'</b></a>';
						}else{
							var tgt = (['statuses_mentions','subscribe'].include(index) && this.isHomePage() == true) ? '' : '_blank'; 
							var labels = '<a attr='+index+' href="http://'+UC_DOMAIN+QHeader.Notice.itemDetails[index].url+'" target="'+tgt+'">'+QHeader.Notice.itemDetails[index].label+'<b>'+this.max99(detail[j].count)+'</b></a>';
						}
						storage.push(labels);
						if(storage.length > 2)
							partItemTotal += detail[j].count; 
					}
				}
			}
			this.other_count = partItemTotal;
			return storage;
	},
	parseDetailItem:function(ret){
		//--exclude:show_status
		try{
			if(typeof(ret)!="object") ret  = JSON.parse(ret);
		}catch(e){return;}
		var DetailItem = [];
		if(ret.notice){
			for(var p in ret.notice){
				if(['subscribe','star_statuses','upvideo_succeed','upvideo_failed','upvideo_blocked','upvideo_unblocked','fee_expire','dms'].include(p) && ret.notice[p].length > 0){//-show_statuses
					for(var l=0;l<this.tmpNotice.length;l++){
							if(this.tmpNotice[l].item == p && this.tmpNotice[l].count > 0){
								var lebel = this.bulidLabel(ret,p,l);
								if(lebel) DetailItem.push(this.bulidLabel(ret,p,l));
							}
					}

				}
			}
		}
		this.noticeItemMerge(this.parseCommonItem(),DetailItem);
	},
	parseShowItem:function(ret){
		try{
			if(typeof(ret)!="object") ret  = JSON.parse(ret);
		}catch(e){return;}
		if(ret.notice){
			for(var p in ret.notice){
				if(p == 'show_statuses'){
					var attr = 'show_statuses';
					var video_title = ret.notice[attr][0].t;
					var update_num = ret.notice[attr][0].i;
					var id = ret.notice[attr][0].s;
					var video_type = ret.notice[attr][0].a;
					var play_url = 'http://v.youku.com/v_show/id_';
					var vid = (ret.notice[attr][0].v) ? ret.notice[attr][0].v : null;
					var strsid = (ret.notice[attr][0].h) ? ret.notice[attr][0].h :null;
					var len = ret.notice['show_statuses'].length;
					var tmp = [];
					if(ret.notice[attr].length >= 1){
						if (len == 1){
							if(video_type == 85){
								var t = '综艺';
								var trun_len = 17-(t+update_num).length;
								var action = truncate(video_title,trun_len,'...')+'更新到'+update_num;
							}else if(video_type == 96){
								var t = '电影';
								var action = truncate(video_title,10,'...')+'上映';
							}else if(video_type == 97){
								var t = '电视剧';
								var trun_len = 17-(t+update_num).length;
								var action = truncate(video_title,trun_len,'...')+'更新到'+update_num;
							}else{
								var t ='节目';
								var trun_len = 17-(t+update_num).length;
								var action = truncate(video_title,trun_len,'...')+'更新到'+update_num;
							}
						}else{
							ret.notice[attr].each(function(o){
									tmp.push(o.t);
							});
							tmp.unique();
							if(len > tmp.length){
								var shows = (tmp.length ==1) ? tmp[0] : tmp[0]+tmp[1];
								if(tmp.length > 2){
									shows = (shows.length+tmp[2].length).length > 36 ? shows : shows+tmp[2];
								}
							}else{
								var show2 = ret.notice[attr][0].t+ret.notice[attr][1].t;
								if(ret.notice[attr].length >= 3){
									var show3 = ret.notice[attr][2].t;
									shows = (show2.length+show3.length > 36) ? show2 : show2+show3;
								}else{
									shows = show2;
								}
							}
							if(tmp.length == 1){shows = truncate(shows,7,'...')};
							var count = (QHeader.NoticeDetail.show_statuses_num) ? QHeader.NoticeDetail.show_statuses_num : 0;
							var count = (count >= tmp.length) ? count : tmp.length;
							var upinfo = ' 等'+count+'个节目有更新';
							var action = shows+upinfo;
						}
						if(len == 1 || tmp.length ==1){
							var see = (vid != null) ? '<a class="flink" href="'+play_url+vid+'" target="_blank" id="seesee" attr="show_statuses">继续看</a><span class="break">|</span>' : '';
							var html='<span class="updateinfo"><a target="_blank" attr="show_statuses" href="http://'+UC_DOMAIN+'/u/home?type=showfriends_timeline">你正在看的'+action+'</a></span><span class="status" id="m_status"></span><span class="action">'+see+'<a class="flink" id="see" target="_blank" href="http://'+UC_DOMAIN+'/u/home?type=showfriends_timeline" attr="show_statuses">查看详情 </a></span>';
						}else{
							var html='<span class="updateinfo"><a attr="show_statuses" target="_blank" href="http://'+UC_DOMAIN+'/u/home?type=showfriends_timeline">你正在看的'+action+'</a></span><span class="action"><a id="see" attr="show_statuses" target="_blank" href="http://'+UC_DOMAIN+'/u/home?type=showfriends_timeline">查看详情 </a></span>';
						}
						document.getElementById('yk-msgbox').style.display='block';
						document.getElementById('tab_msg').style.display='none';
						document.getElementById('watchmsg_v').innerHTML=html;
						document.getElementById('notice_show').style.display='block';
						this.f5mask();
						if(vid && strsid && (len == 1 || tmp.length ==1)){
							var url = "http://"+nc_domain+"/index/showNotice?vid="+vid+"&showid="+strsid+"&__callback=QHeader.NoticeDetail.continueSee";
							addScript(url,null,true);
						}

					}
			}
			}
		}
	},
	continueSee : function(ret){
		try{
			if(typeof(ret)!="object") ret  = JSON.parse(ret);
		}catch(e){return;}
		if(ret.err && (ret.err == 1 || !ret.stage)){
			if(document.getElementById('seesee'))
				document.getElementById('seesee').innerHTML='去观看';
		       	return;
		}
		var label = '';
		if(ret.stage) var label = '已看到第'+ret.stage+'集';
		if(ret.percent == 100){ label = '已看完第'+ret.stage+'集';}
		else if(ret.percent == 0){label = '已看到第'+ret.stage+'集';}
		else {label += '的'+ret.percent+'%';}
		if(document.getElementById('m_status')) document.getElementById('m_status').innerHTML=label;
		if(ret.encodeVid){
			var play_url = 'http://v.youku.com/v_show/id_';
			var see_url = play_url+ret.encodeVid+'.html';
			if(ret.sec) see_url += '?firsttime='+ret.sec;
			if(document.getElementById('seesee')) document.getElementById('seesee').href=see_url;
		}
	}, 
	isHomePage:function(){
		if(typeof window.location.href == 'undefined') return false;
		if(window.location.href.indexOf('/u/home') > 0) return true;
		return false;
	},
	callUpdateNum:function(type){
		if(!type) return;
		if(QHeader.Notice.itemDetails[type] && QHeader.Notice.itemDetails[type].count){
			var num = parseInt(QHeader.Notice.itemDetails[type].count);
			var upNum = (QHeader.Notice.total - num) > 0 ? (QHeader.Notice.total - num) : 0;
			if(QHeader.Notice.commentItems.include(type)){
				QHeader.Notice.updateNum(upNum);
				QHeader.Notice.total = upNum;
				QHeader.Notice.changeLink(type);
			}
		}

	},
	clearSelf:function(el){
		if(el) {
			el.style.display='none';
			var type = el.getAttribute('attr');
			var cp = this.logType(type);
			if(['comment_all','star_statuses','show_statuses','subscribe','upvideo_succeed','upvideo_failed','upvideo_blocked','upvideo_unblocked','fee_expire','statuses_mentions','followers','video_reply','comment_reply','comment_mentions','dms', 'sysmsg'].include(type)){
				if(type=='comment_all') type='["video_reply","comment_reply","comment_mentions","statuses_comments"]';
				else type = '["'+type+'"]';
				var ap = '{"type":'+type+',"rand":'+Math.random() + '}';
				var url = comments_domain+'/comments/~ajax/clearReset.html?__ap=' + ap;
				addScript(url, null, true);
			}
			if(cp)
				hz(cp,'1000404');
		}
	},
	logType:function(type){
		if(type){
			if(QHeader.Notice.itemDetails[type] && QHeader.Notice.itemDetails[type].cpp){
				return QHeader.Notice.itemDetails[type].cpp;
			}else if(type == "show_statuses"){
				return 4004478;
			}else if(type == "other"){
				return 4004592;
			}else if(type == "comment_all"){
				return 4004474;
			}else{
				return false;
			}
		}else
			return false;
	},
	closeLogType:function(type){
		if('type'.indexOf('upvideo') > 0) var type = 'upvideo';
		if(!['star_statuses','show_statuses','upvideo'].include(type)) var type = 'other';
		var closeLogs = {'star_statuses':4006922,'show_statuses':4006921,'upvideo':4006924,'other':4006920};
		return closeLogs[type];
	},
	close:function(){
		var videomsg_show = this.container.select('div.videomsg')[0].visible();
		var msglist_show = this.container.select('div.msglist')[0].visible();
		var t = [];
		if(videomsg_show){
			if(this.container.select('div.videomsg')[0] && this.container.select('div.videomsg')[0].select('a').length > 0)
				var label_a = this.container.select('div.videomsg')[0].select('a')[0].getAttribute('attr');
			else if(this.container.select('div.videomsg')[0] && this.container.select('div.videomsg')[0].select('span')[1])
				var label_a = this.container.select('div.videomsg')[0].select('span')[1].getAttribute('attr');
			if(label_a) var t = '["'+label_a+'"]';
		}else if(!videomsg_show && msglist_show){
			var t = '["video_reply","comment_reply","comment_mentions","subscribe","followers","statuses_mentions"]';
			var label_a = 'other';
		}
		if(t.length > 0){
			var uid = QHeader.Userlog.getUID();
			if(!uid) return;
			var cp = this.closeLogType(label_a);
			var uid = QHeader.Userlog.getUID();
			if(uid){
				var ap = '{"type":'+ t + ',"rand":'+Math.random() + '}';	
				var url = comments_domain+'/comments/~ajax/clearReset.html?__ap=' + ap;
				addScript(url, null, true);
				hz(cp,'1000404');
			}
		}
	},
	closeStatus:function(){
		document.getElementById('notice_show').style.display='none';
		var uid = QHeader.Userlog.getUID();
		if(uid){
			var ap = '{"type":["show_statuses"],"rand":'+Math.random() + '}';	
			var url = comments_domain+'/comments/~ajax/clearReset.html?__ap=' + ap;
			addScript(url, null, true);
			hz('4006920','1000404');
		}
	},
	max99:function(num){
		return (num > this.maxNotice) ? this.maxNotice+'+' : (num > 0 ? num : 0);
	}
}
QHeader.Search = {
	defaultKey: '',
	init: function(){
		this.form = document.getElementById('qheader_search');
		if(!this.form){ return; }
		this.input = this.form.getElementsByTagName('input')[0];
		this.button = this.form.getElementsByTagName('button')[0];
		var defkey = this.findFirstKey();
		if(defkey){
			this.setDefaultKey(defkey);	
		}
		this.bind();
	},
	bind: function(){
		var form = this.form
			,input = this.input
			,button = this.button
			,_this = this
		addEvent(input, 'focus', function(){
			var def = _this.getDefaultKey()
				,val = trim(input.value);
			if(val == def && def){
				input.className = '';			
				input.value = '';
			}
		});
		addEvent(input, 'blur', function(){
			var def = _this.getDefaultKey()
				,val = trim(input.value);
			if((val== '' || val == def) && def){
				input.className = 'input-default';
				input.value = def;
			}
		});
	},
	getKey: function(key){
		var val = trim(this.input.value);
		return val;
	},
	setKey: function(key){
		if(typeof(key)=='string' || typeof(key)=='number'){
			var key = trim(key.toString());
			this.input.value = key;
			this.input.className = '';
		}
		return this;
	},
	getDefaultKey: function(){
		return this.defaultKey;
	},
	setDefaultKey: function(key){
		if(typeof(key)=='string' || typeof(key)=='number'){
			var key = trim(key.toString());
			this.input.value = key;
			if(key){ this.input.className = 'input-default'; }
			else{ this.input.className = ''; }
			this.defaultKey = key;
		}
		return this;
	},
	findFirstKey: function(){
		var key = '';
		var get = function(links){
			for(var i=0; i<links.length; i++){
				var k = trim(links[i].innerHTML);
				if(k != ''){
					return k; 	
				}
			}	
		} 
		var area = document.getElementById('qheader_keywords');
		if(area){
			var links = area.getElementsByTagName('A');
			if(links.length){ 
				var k = get(links);
				if(k){
					key = k;
				} 
			}	
		}
		return key;
	},
	doSearch: function(){
		var q = trim(this.input.value);
		var url = '';
		if(q == ''){
			url = 'http://www.soku.com?inner';
		}else{
			url= this.form.action + '/q_'+q;
		}
		window.open(url);
		return false;
	}
}

QHeader.Channel = {
	init: function(){
		var node = $('qheader_channel');
		if(!node){ return; }
		this.dp = new Dropmenu({
			'group': QHeader.dropmenuGroup,
			'node': node,
			'fire': 'hover'	
		});
	},
	show: function(){
		if(this.dp){ this.dp.show(); }
		return this;	
	},
	hide: function(){
		if(this.dp){ this.dp.hide(); }
		return this;
	}	
}

QHeader.Nav = {
	init: function(){
		var nav = $('qheader_nav');
		if(!nav){ return; }
		var nodes = nav.select('.dropdown');
		nodes.each(function(node){
			var dp = new Dropmenu({
				'group': QHeader.dropmenuGroup,
				'node': node,
				'fire': 'hover'
			});
		});
	},
	findStick: function(){
		var nav = document.getElementById('qheader_nav');
		if(!nav){ return; }
		var divs = nav.getElementsByTagName('div');
		var fhandle=null, fpanel = null, fcurrent = null;
		for(var i=0, len=divs.length; i<len; i++){
			var div = divs[i];
			if(div.className && div.className == 'panel'){//find dropmenu
				var lis = div.getElementsByTagName('li');
				var flag = true;
				for(var j=0, len1=lis.length; j<len1; j++){//find current
					var li = lis[j]	;
					if(li.className && li.className == 'current'){
						flag = false;
						fpanel = div; fcurrent = li;
						var o = fpanel.parentNode.getElementsByTagName('div')[0];
						if(o && o.className && o.className == 'handle'){
							fhandle = o; 
						}
						break;	
					}
				}
				if(!flag){ break; }
			}
		}
		if(fcurrent && fhandle){//replace
			var a1 = fhandle.getElementsByTagName('a')[0]
				,a2 = fcurrent.getElementsByTagName('a')[0]
				,a1_text = a1.innerHTML 
				,a1_href = a1.href
				,a2_text = a2.innerHTML
				,a2_href = a2.href;
			
			a1.href = a2_href; a1.innerHTML = a2_text;	a1.className = 'current';	
			a2.href = a1_href; a2.innerHTML = a1_text;
			
			var ul = fcurrent.parentNode, li = document.createElement('li');
			
			li.appendChild(a2);
			ul.insertBefore(li, ul.firstChild);
			ul.removeChild(fcurrent);
		}
	}
}

QHeader.Upload = {
	init: function(){
		var node = $('qheader_upload');
		if(!node){ return; }
		this.dp = new Dropmenu({
			'group': QHeader.dropmenuGroup,
			'node': node,
			'fire': 'hover'
		});
	},
	show: function(){
		if(this.dp){ this.dp.show(); }
		return this;	
	},
	hide: function(){
		if(this.dp){ this.dp.hide(); }
		return this;
	}
}
var DropmenuGroup = function(){
	this.coll = [];	
	this.bind();
}
DropmenuGroup.prototype = {
	bind: function(){
		var _this = this;
		document.observe('click', function(){ 
			for(var i=0; i<_this.coll.length; i++){
				if(_this.coll[i].fire == 'click'){
					_this.coll[i].hdie();
				}
			}
		})	
	},
	getLength: function(){
		return this.coll.length;
	},
	isExist: function(dropmenu){
		var len = this.getLength();
		for(var i=0; i<len; i++){
			if(this.coll[i] == dropmenu){
				return true;
			}
		}
		return false;	
	},
	add: function(dropmenu){
		if(dropmenu instanceof Dropmenu && !this.isExist(dropmenu)){
			this.coll.push(dropmenu);
		}
		return this;
	},
	remove: function(dropmenu){
		var len = this.getLength();
		for(var i=0; i<len; i++){
			if(this.coll[i] == dropmenu){
				this.coll.splice(i, 1);
				break;	
			}
		}
		return true;
	},
	hideAll: function(){
		var len = this.getLength();
		for(var i=0; i<len; i++){
			this.coll[i].hide();
		}
		return this;
	},
	hideOther: function(dropmenu){
		var len = this.getLength();
		for(var i=0; i<len; i++){
			if(this.coll[i] != dropmenu){
				this.coll[i].hide();	
			}
		}
		return this;
	}
}

var Dropmenu = function(params){
	var params = typeof(arguments[0]) == 'object' ? params : {}
	this.group = params.group ? params.group : new DropmenuGroup();
	this.fire = params.fire ? params.fire : 'click';
	this.fire = this.fire=='hover' && !("createTouch" in document) ? 'hover' : 'click';
	this.node = params.node ? params.node : null;
	this.delay = params.delay ? params.delay : 200; 
	this.callback = params.callback ? params.callback : {};
	this.mask = null;
	this.status = 'hide';
	this.classname = {'drop': 'dropdown-open',	'mask': 'mask'};
	if(!this.node){ return; }
	this.handle = this.node.select('.handle')[0];
	this.panel = this.node.select('.panel')[0];
	
	this.callback =  {
		'show': typeof(this.callback.show) == 'function' ? this.callback.show : null,
		'hide':	typeof(this.callback.hide) == 'function' ? this.callback.hide : null
	}
	if(!this.handle || !this.panel){ return; }
	
	this.init();
}
Dropmenu.prototype = {
	init: function(){
		this.group.add(this);//向菜单组添加	
		this.bind();
	},
	bind: function(){
		var _this = this;
		if(this.fire == 'click'){
			this.handle.observe('click', function(e){	_this.toggle(); cancelBubble(e); });
			this.panel.observe('click', function(e){ cancelBubble(e); });
		}else if(this.fire == 'hover'){
			var time = 0;
			var timer = null;
			this.node
			.observe('click', function(e){	cancelBubble(e); })
			.observe('mouseenter', function(e){ 
				clearInterval(timer); time = 0;
				timer = setInterval(function(){ 
					if(time>=_this.delay){ _this.show(); clearInterval(timer); time = 0; return; }
					time += 10;
				}, 10);
			})
			.observe('mouseleave', function(e){ 
				clearInterval(timer); time = 0;
				timer = setInterval(function(){ 
					if(time>=_this.delay){ _this.hide(); clearInterval(timer); time = 0; return; }
					time += 10;
				}, 10);
			});
		}
	},
	f5mask: function(){
		if(this.mask){
			var w = this.panel.offsetWidth,
				h = this.panel.offsetHeight,
				style = {'width': w+'px', 'height': h+'px'};
			this.mask.setStyle(style);
		}
		return this;
	},
	setCallback: function(type, func){
		if(type == 'show' && typeof(func) == 'function'){ 
			this.callback.show = func;
		}
		if(type == 'hide' && typeof(func) == 'function'){ 
			this.callback.hide = func;
		}
		return this;
	},
	update: function(html){
		this.panel.update(html);
		this.f5mask();
		return this;
	},
	show: function(){
		if(this.status == 'show'){ return this; }
		this.node.addClassName(this.classname.drop);
		if(!this.mask){
			var attr = {'scrolling': '0', 'frameborder': '0'};
			this.mask = new Element('iframe')
						.addClassName(this.classname.mask)
						.writeAttribute(attr);
			this.node.appendChild(this.mask);		
		}
		this.f5mask();
		//点击触发收起其他菜单
		if(this.fire == 'click'){ this.group.hideOther(this); }
		this.status = 'show';
		if(this.callback.show){ this.callback.show(); }
		return this;
	},
	hide: function(){
		if(this.status == 'hide'){ return this; }		
		this.node.removeClassName(this.classname.drop);
		this.status = 'hide';
		if(this.callback.hide){ this.callback.hide(); }
		return this;
	},
	toggle: function(){
		var status = this.getStatus();
		if(status == 'hide'){
			return this.show();	
		}else{
			return this.hide();
		}
	},
	getStatus: function(){
		return this.status;	
	}
}

//private method
var domReady = function(callback){
	var timer = null;
	var isready = false;
	var callback = typeof(callback) == 'function' ? callback : function(){};
	if(document.addEventListener){
		document.addEventListener("DOMContentLoaded", function(){ 
			if(!isready){ isready = true; callback(); }
		}, false);
	}else if(document.attachEvent){
		document.attachEvent("onreadystatechange", function(){
			if((/loaded|complete/).test(document.readyState)){
				if(!isready){ isready = true; callback(); }
			}
		});
		if(window == window.top){
			timer = setInterval(function(){
				if(isready){ clearInterval(timer); timer=null; return; }
				try{
					document.documentElement.doScroll('left');	
				}catch(e){
					return;
				}
				if(!isready){ isready = true; callback(); }
			},5);
		}
	}
}

var addScript = function(src, callback, isremove){
	if(typeof(arguments[0]) != 'string'){ return; }
	var callback = typeof(arguments[1]) == 'function' ? callback : function(){};
	var isremove = typeof(arguments[2]) == 'boolean' ? isremove : false;
	var head = document.getElementsByTagName('HEAD')[0];
	var script = document.createElement('SCRIPT');
	script.type = 'text/javascript'; 
	script.src = src;
	head.appendChild(script);
	if(!/*@cc_on!@*/0) {
		script.onerror = script.onload = function(){ 
			callback();
			if(isremove){ script.parentNode.removeChild(this); } 
		}
	}else{
		script.onreadystatechange = function () {
			if (this.readyState == 'loaded' || this.readyState == 'complete') { 
				callback();
				if(isremove){ this.parentNode.removeChild(this); } 
			}
		}
	}
}

var addEvent = function(dom, eventname, func){
	if(window.addEventListener){
		if(eventname == 'mouseenter' || eventname == 'mouseleave'){
			function fn(e){
				var a = e.currentTarget, b = e.relatedTarget;
				if(!elContains(a, b) && a!=b){
					func.call(e.currentTarget,e);
				}	
			}
			function elContains(a, b){
				try{ return a.contains ? a != b && a.contains(b) : !!(a.compareDocumentPosition(b) & 16); }catch(e){}
			}
			if(eventname == 'mouseenter'){
				dom.addEventListener('mouseover', fn, false);
			}else{
				dom.addEventListener('mouseout', fn, false);
			}
		}else{
			dom.addEventListener(eventname, func, false);
		}
	}else if(window.attachEvent){
		dom.attachEvent('on' + eventname, func);
	}
}

var cancelBubble = function(evt){
	var evt = window.event || evt;
	if(evt.stopPropagation){      
		evt.stopPropagation();    
	}else{    
		evt.cancelBubble=true;   
	}
	return false;
}

var preventDefault = function(evt){
	var evt = window.event || evt;
	if(evt.preventDefault){
		evt.preventDefault();
	}else{
		event.returnValue = false;
	}
	return false;
}

var getElementPos = function(o){
	var point = {x:0, y:0};
	if (o.getBoundingClientRect) {
		var x=0, y=0;
		try{
			var box = o.getBoundingClientRect();
			var D = document.documentElement;
			x = box.left + Math.max(D.scrollLeft, document.body.scrollLeft) - D.clientLeft;
			y = box.top + Math.max(D.scrollTop, document.body.scrollTop) - D.clientTop;
		}catch(e){}
		point.x = x;
		point.y = y;
	}else{
		function pageX(o){ try {return o.offsetParent ? o.offsetLeft +  pageX(o.offsetParent) : o.offsetLeft; } catch(e){ return 0; } }
		function pageY(o){ try {return o.offsetParent ? o.offsetTop + pageY(o.offsetParent) : o.offsetTop; } catch(e){ return 0; } }
		point.x = pageX(o);
		point.y = pageY(o);
	}
	return point;
}

var getMousePos = function(e){
	var point = {x:0, y:0};
	if(typeof window.pageYOffset != 'undefined') {
		point.x = window.pageXOffset;
		point.y = window.pageYOffset;
	}else if(typeof document.compatMode != 'undefined' && document.compatMode != 'BackCompat') {
		point.x = document.documentElement.scrollLeft;
		point.y = document.documentElement.scrollTop;
	}else if(typeof document.body != 'undefined') {
		point.x = document.body.scrollLeft;
		point.y = document.body.scrollTop;
	}
	point.x += e.clientX;
	point.y += e.clientY;
	
	return point;
}

var trim = function(s){
	s = s.replace( /^(\s*|　*)/, '');
	s = s.replace( /(\s*|　*)$/, '');
	return s;
}
//cn
var truncate = function(s,length, truncation){
	var reg = /[\u4e00-\u9fa5]/;
	var count = 0, t = '';
	for(var i=0, len=s.length; i<len; i++){
		var char = s.charAt(i);
		if(reg.test(char)){ count ++; }
		else{ count += 0.8;	}
		t += char;
		if(count>=len || count + 0.1 > length){ 
			if(i != len-1){ t+= truncation; } break; 
		}
	}
	return t;
}

var hz = function(cp, cpp){
	var url = 'http://hz.youku.com/red/click.php?tp=1&cp=' + cp +'&cpp=' + cpp + '&tp='+Math.random()	
	var img = new Image();
	img.src = url;
}

//init
o.QHeader = QHeader;
QHeader.init();

//FX
;(function(){this.FX=function(b,c,d,e,f,g){this.el=a.get(b),this.attributes=c,this.duration=d||.7,this.transition=e&&e in FX.transitions?e:"easeInOut",this.callback=f||function(){},this.ctx=g||window,this.units={},this.frame={},this.endAttr={},this.startAttr={}},this.FX.transitions={linear:function(a,b,c,d){return c*a/d+b},easeIn:function(a,b,c,d){return-c*Math.cos(a/d*(Math.PI/2))+c+b},easeOut:function(a,b,c,d){return c*Math.sin(a/d*(Math.PI/2))+b},easeInOut:function(a,b,c,d){return-c/2*(Math.cos(Math.PI*a/d)-1)+b}},this.FX.prototype={start:function(){var a=this;this.getAttributes(),this.duration=1e3*this.duration,this.time=(new Date).getTime(),this.animating=!0,this.timer=setInterval(function(){var b=(new Date).getTime();a.time+a.duration>b?(a.elapsed=b-a.time,a.setCurrentFrame()):(a.frame=a.endAttr,a.complete()),a.setAttributes()},10)},ease:function(a,b){return FX.transitions[this.transition](this.elapsed,a,b-a,this.duration)},complete:function(){clearInterval(this.timer),this.timer=null,this.animating=!1,this.callback.call(this.ctx)},setCurrentFrame:function(){for(attr in this.startAttr)if(this.startAttr[attr]instanceof Array){this.frame[attr]=[];for(var a=0;this.startAttr[attr].length>a;a++)this.frame[attr][a]=this.ease(this.startAttr[attr][a],this.endAttr[attr][a])}else this.frame[attr]=this.ease(this.startAttr[attr],this.endAttr[attr])},getAttributes:function(){for(var b in this.attributes)switch(b){case"color":case"borderColor":case"border-color":case"backgroundColor":case"background-color":this.startAttr[b]=c(this.attributes[b].from||a.getStyle(this.el,b)),this.endAttr[b]=c(this.attributes[b].to);break;case"scrollTop":case"scrollLeft":var d=this.el==document.body?document.documentElement||document.body:this.el;this.startAttr[b]=this.attributes[b].from||d[b],this.endAttr[b]=this.attributes[b].to;break;default:var e,f=this.attributes[b].to,g=this.attributes[b].units||"px";this.attributes[b].from?e=this.attributes[b].from:(e=parseFloat(a.getStyle(this.el,b))||0,"px"!=g&&document.defaultView&&(a.setStyle(this.el,b,(f||1)+g),e=(f||1)/parseFloat(a.getStyle(this.el,b))*e,a.setStyle(this.el,b,e+g))),this.units[b]=g,this.endAttr[b]=f,this.startAttr[b]=e}},setAttributes:function(){for(var b in this.frame)switch(b){case"opacity":a.setStyle(this.el,b,this.frame[b]);break;case"scrollLeft":case"scrollTop":var c=this.el==document.body?document.documentElement||document.body:this.el;c[b]=this.frame[b];break;case"color":case"borderColor":case"border-color":case"backgroundColor":case"background-color":var d="rgb("+Math.floor(this.frame[b][0])+","+Math.floor(this.frame[b][1])+","+Math.floor(this.frame[b][2])+")";a.setStyle(this.el,b,d);break;default:a.setStyle(this.el,b,this.frame[b]+this.units[b])}}};var a={get:function(a){return"string"==typeof a?document.getElementById(a):a},getStyle:function(a,c){c=b(c);var d=document.defaultView;if(d&&d.getComputedStyle)return d.getComputedStyle(a,"")[c]||null;if("opacity"==c){var e=a.filters("alpha").opacity;return isNaN(e)?1:e?e/100:0}return a.currentStyle[c]||null},setStyle:function(a,c,d){"opacity"==c?(a.style.filter="alpha(opacity="+100*d+")",a.style.opacity=d):(c=b(c),a.style[c]=d)}},b=function(){var a={};return function(b){if(a[b])return a[b];var c=b.split("-"),d=c[0];if(c.length>1)for(var e=1,f=c.length;f>e;e++)d+=c[e].charAt(0).toUpperCase()+c[e].substring(1);return a[b]=d}}(),c=function(){var a=/^#?(\w{2})(\w{2})(\w{2})$/,b=/^#?(\w{1})(\w{1})(\w{1})$/,c=/^rgb\((\d{1,3}),\s*(\d{1,3}),\s*(\d{1,3})\)$/;return function(d){var e=d.match(a);return e&&4==e.length?[parseInt(e[1],16),parseInt(e[2],16),parseInt(e[3],16)]:(e=d.match(c),e&&4==e.length?[parseInt(e[1],10),parseInt(e[2],10),parseInt(e[3],10)]:(e=d.match(b),e&&4==e.length?[parseInt(e[1]+e[1],16),parseInt(e[2]+e[2],16),parseInt(e[3]+e[3],16)]:void 0))}}()})(),FX.transitions.quadIn=function(a,b,c,d){return c*(a/=d)*a+b},FX.transitions.quadOut=function(a,b,c,d){return-c*(a/=d)*(a-2)+b},FX.transitions.quadInOut=function(a,b,c,d){return 1>(a/=d/2)?c/2*a*a+b:-c/2*(--a*(a-2)-1)+b},FX.transitions.cubicIn=function(a,b,c,d){return c*(a/=d)*a*a+b},FX.transitions.cubicOut=function(a,b,c,d){return c*((a=a/d-1)*a*a+1)+b},FX.transitions.cubicInOut=function(a,b,c,d){return 1>(a/=d/2)?c/2*a*a*a+b:c/2*((a-=2)*a*a+2)+b},FX.transitions.quartIn=function(a,b,c,d){return c*(a/=d)*a*a*a+b},FX.transitions.quartOut=function(a,b,c,d){return-c*((a=a/d-1)*a*a*a-1)+b},FX.transitions.quartInOut=function(a,b,c,d){return 1>(a/=d/2)?c/2*a*a*a*a+b:-c/2*((a-=2)*a*a*a-2)+b},FX.transitions.quintIn=function(a,b,c,d){return c*(a/=d)*a*a*a*a+b},FX.transitions.quintOut=function(a,b,c,d){return c*((a=a/d-1)*a*a*a*a+1)+b},FX.transitions.quintInOut=function(a,b,c,d){return 1>(a/=d/2)?c/2*a*a*a*a*a+b:c/2*((a-=2)*a*a*a*a+2)+b},FX.transitions.expoIn=function(a,b,c,d){return 0==a?b:c*Math.pow(2,10*(a/d-1))+b-.001*c},FX.transitions.expoOut=function(a,b,c,d){return a==d?b+c:1.001*c*(-Math.pow(2,-10*a/d)+1)+b},FX.transitions.expoInOut=function(a,b,c,d){return 0==a?b:a==d?b+c:1>(a/=d/2)?c/2*Math.pow(2,10*(a-1))+b-5e-4*c:1.0005*(c/2)*(-Math.pow(2,-10*--a)+2)+b},FX.transitions.circIn=function(a,b,c,d){return-c*(Math.sqrt(1-(a/=d)*a)-1)+b},FX.transitions.circOut=function(a,b,c,d){return c*Math.sqrt(1-(a=a/d-1)*a)+b},FX.transitions.circInOut=function(a,b,c,d){return 1>(a/=d/2)?-c/2*(Math.sqrt(1-a*a)-1)+b:c/2*(Math.sqrt(1-(a-=2)*a)+1)+b},FX.transitions.backIn=function(a,b,c,d,e){return e=e||1.70158,c*(a/=d)*a*((e+1)*a-e)+b},FX.transitions.backOut=function(a,b,c,d,e){return e=e||1.70158,c*((a=a/d-1)*a*((e+1)*a+e)+1)+b},FX.transitions.backBoth=function(a,b,c,d,e){return e=e||1.70158,1>(a/=d/2)?c/2*a*a*(((e*=1.525)+1)*a-e)+b:c/2*((a-=2)*a*(((e*=1.525)+1)*a+e)+2)+b},FX.transitions.elasticIn=function(a,b,c,d,e,f){if(0==a)return b;if(1==(a/=d))return b+c;if(f||(f=.3*d),!e||Math.abs(c)>e){e=c;var g=f/4}else var g=f/(2*Math.PI)*Math.asin(c/e);return-(e*Math.pow(2,10*(a-=1))*Math.sin((a*d-g)*2*Math.PI/f))+b},FX.transitions.elasticOut=function(a,b,c,d,e,f){if(0==a)return b;if(1==(a/=d))return b+c;if(f||(f=.3*d),!e||Math.abs(c)>e){e=c;var g=f/4}else var g=f/(2*Math.PI)*Math.asin(c/e);return e*Math.pow(2,-10*a)*Math.sin((a*d-g)*2*Math.PI/f)+c+b},FX.transitions.elasticBoth=function(a,b,c,d,e,f){if(0==a)return b;if(2==(a/=d/2))return b+c;if(f||(f=d*.3*1.5),!e||Math.abs(c)>e){e=c;var g=f/4}else var g=f/(2*Math.PI)*Math.asin(c/e);return 1>a?-.5*e*Math.pow(2,10*(a-=1))*Math.sin((a*d-g)*2*Math.PI/f)+b:.5*e*Math.pow(2,-10*(a-=1))*Math.sin((a*d-g)*2*Math.PI/f)+c+b},FX.transitions.backIn=function(a,b,c,d,e){return e===void 0&&(e=1.70158),c*(a/=d)*a*((e+1)*a-e)+b},FX.transitions.backOut=function(a,b,c,d,e){return e===void 0&&(e=1.70158),c*((a=a/d-1)*a*((e+1)*a+e)+1)+b},FX.transitions.backBoth=function(a,b,c,d,e){return e===void 0&&(e=1.70158),1>(a/=d/2)?c/2*a*a*(((e*=1.525)+1)*a-e)+b:c/2*((a-=2)*a*(((e*=1.525)+1)*a+e)+2)+b},FX.transitions.bounceIn=function(a,b,c,d){return c-FX.transitions.bounceOut(d-a,0,c,d)+b},FX.transitions.bounceOut=function(a,b,c,d){return 1/2.75>(a/=d)?c*7.5625*a*a+b:2/2.75>a?c*(7.5625*(a-=1.5/2.75)*a+.75)+b:2.5/2.75>a?c*(7.5625*(a-=2.25/2.75)*a+.9375)+b:c*(7.5625*(a-=2.625/2.75)*a+.984375)+b},FX.transitions.bounceBoth=function(a,b,c,d){return d/2>a?.5*FX.transitions.bounceIn(2*a,0,c,d)+b:.5*FX.transitions.bounceOut(2*a-d,0,c,d)+.5*c+b};
	
})(window);
