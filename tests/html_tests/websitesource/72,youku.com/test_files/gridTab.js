/**
 * GridTab专用 js 文件
 */
var GridTabEvent = Class.create();
GridTabEvent.prototype = {
	initialize: function() {},
	drawerTabberInit: function() {
		var handlers = $A(document.getElementsByTagName('li'));
		this._drawerTabber = this.drawerTabber.bindAsEventListener(this)
		handlers.each(function(o){if(o.getAttribute('tabIdx')) Event.observe(o, 'mouseover', this._drawerTabber)}.bind(this));
		handlers.each(function(o){if(o.getAttribute('tabIdx')) Event.observe(o, 'mouseout', this._drawerTabber)}.bind(this));
		handlers.each(function(o){if(o.getAttribute('tabIdx')) Event.observe(o, 'click', this._drawerTabber)}.bind(this));
		//初始化随机选中
		handlers.each(function(o){
				//tabIdx 是标签, random 该容器需要随机, isRandomed 该容器已经被随机过
				if(o.getAttribute('tabIdx') && o.parentNode.getAttribute('random') && !o.parentNode.getAttribute('isRandomed')){
					o = Element.extend(o);
					var curr = null;
					var lis = [];
					o.parentNode.setAttribute('isRandomed', 'true');
					lis.push(o);
					if(o.className.indexOf('current') >= 0){
						curr = o;
					}
					c = o;
					while(c = c.previous('li')){
						if(c.className.indexOf('current') >= 0){
							curr = c;
						}
						lis.push(c);
					}
					c = o;
					while(c = c.next('li')){
						if(c.className.indexOf('current') >= 0){
							curr = c;
						}
						lis.push(c);
					}
					var selected = Math.floor(Math.random() * lis.length);
					if(lis[selected] != curr)
						QGridTab.switchTab(lis[selected].getAttribute('tabIdx'), curr.getAttribute('tabIdx'));
				}
		});
	},
	drawerTabber: function(evt) {
		if(this.tabTimeout && !isNaN(this.tabTimeout)) {
			window.clearTimeout(this.tabTimeout);
			this.tabTimeout = null;
		}
		if(evt.type.indexOf('over') <= 0) return false;
		var handler = Event.element(evt);
		while(handler.nodeName != 'LI') handler = handler.parentNode;
		if(handler.className.indexOf('current') >= 0) return false;
		handler = Element.extend(handler);
		var current = handler.previous('li.current');
		if(current == undefined) current = handler.next('li.current');
		this.tabTimeout = window.setTimeout('QGridTab.switchTab(\''+handler.getAttribute('tabIdx')+'\', \''+current.getAttribute('tabIdx')+'\')', 100);
	},
	switchTab: function(curr, old) {
		if(isNaN(curr) && isNaN(old)) {
			var elmcurr = $(curr);
			var elmold = $(old);
			$('th'+curr).className = 'current';
			$('th'+old).className = '';
		} else {
			var elmcurr = $('tabber'+curr);
			var elmold = $('tabber'+old);
		}
		if($(curr+'tabarea')){
			$(curr+'tabarea').show();
			imgs = $A(elmcurr.getElementsByTagName('img'));
			imgs.each(function(o){
				Element.extend(o);
				if (o.getAttribute('_src')) {
					o.src = o.getAttribute('_src');
				}
			});
		}
		elmcurr.show();
		elmold.hide();
	}
}

var QGridTab = new GridTabEvent();

// 页面加载完成后初始化页面事件
window.nova_init_hook_event = function(){

	QGridTab.drawerTabberInit();
	
	var s = "MSIE", u = navigator.userAgent, i = -1;
	if ((i = u.indexOf(s)) >= 0) {
		var v = parseFloat(u.substr(i + s.length));
		if(v == 6){ try{ document.execCommand("BackgroundImageCache", false, true); } catch(e){} }
	}

}