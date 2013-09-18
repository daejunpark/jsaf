var crocos = {
	entry : {
		anemos : 'http://yjaxc.yahoo.co.jp/oi',
		iframe : 'http://i.yimg.jp/images/listing/tool/crocos/crocos_ytop.html',
		crocos : 'http://crocosad.c.yimg.jp/yahoo/ydn/cad/'
	},
	anemosParam : {
		ytop_crocos : {
			t : 'm',
			s : 'anemos_ytopcrocos01695',
			i : 'toppage'
		},
		ytop_ams_rmdl_utf8 : {
			t : 'j',
			s : 'ytop_ams_rmdl_utf8',
			i : 'toppage'
		}
	},
	util : {
		obj2query : function(in_obj) {
			var query = [];
			for (var field in in_obj) {
				query.push(field + '=' + encodeURIComponent(in_obj[field]));
			}
			return query.join('&');
		},
		html : function(in_name, in_async, in_attrs, in_styles) {
			if (in_async) {
				var attrs = [];
				for (var prop in in_attrs) {
					attrs.push(prop + '="' + in_attrs[prop] + '"');
				}
				var styles = [];
				for (var prop in in_styles) {
					styles.push(prop + ': ' + in_styles[prop] + ';');
				}
				if (styles.length > 0) {
					attrs.push('style="' + styles.join(' ') + '"');
				}
				document.write('<' + in_name);
				if (attrs.length > 0) {
					document.write(' ' + attrs.join(' '));
				}
				document.write('></' + in_name + '>');
			} else {
				var elem = document.createElement(in_name);
				for (var prop in in_attrs) {
					elem[prop] = in_attrs[prop];
				}
				for (var prop in in_styles) {
					elem.style[prop] = in_styles[prop];
				}
				var target = document.getElementsByTagName('SCRIPT').item(0);
				target.parentNode.insertBefore(elem, target);
			}
		},
		random : function(numerator, denominator) {
			return Math.floor(Math.random() * denominator) < numerator;
		},
		bind : function (callback, self) {
			return function() {
				callback.apply(self, arguments);
			};
		},
		debugPrint : function(in_data) {
/*
			if (typeof(console) == 'undefined') {
				console = {};
				console.log = function(in_string) {
					var debug = document.getElementById('crocos_debug');
					if (debug) {
						debug.innerHTML += in_string + '<br />';
					}
				};
			}
			var _inspect = function(in_obj, in_indent) {
				for (var prop in in_obj) {
					var child = false;
					if ((typeof(in_obj[prop]) == 'object') && (in_obj[prop])) {
						child = true;
					}
					console.log(in_indent + prop + ' : ' + in_obj[prop]);
					if (child) {
						_inspect(in_obj[prop], in_indent + '  ');
					}
				}
			};
			_inspect({DP : in_data}, '');
*/
		}
	},
	iframeElement : null,
	crocosCtxt : null,
	crocosDone : function(e) {
		if (e.data == this.crocosCtxt.api) {
			this.util.debugPrint('crocosDone');
			this.iframeElement.style.display = 'block';
		} else {
			this.util.debugPrint('recv message : ' + e.data);
		}
	},
	iframeDone : function() {
		this.util.debugPrint('iframeDone');
		var done = this.util.bind(this.crocosDone, this);
		if (window.addEventListener) {
			window.addEventListener('message', done, false);
		} else {
			if (window.attachEvent) {
				window.attachEvent('onmessage', done);
			}
		}
		var msg = this.util.obj2query(this.crocosCtxt);
		this.iframeElement.contentWindow.postMessage(msg, '*');
	},
	lookupAnemos : function(in_obj) {
		this.util.debugPrint(in_obj);
		if (in_obj[0].key) {
			this.crocosCtxt = in_obj[0];
			this.crocosCtxt.api = this.entry.crocos + in_obj[0].key;
			var iframeId = 'crocos' + Math.floor(Math.random() * 9999);
			var iframeAttr = {
				'id' : iframeId,
				'class' : 'bxNa',
				'scrolling' : 'no',
				'frameborder' : '0'
			};
			var iframeStyle = {
				'width' : '100%',
				'height' : '116px',
				'border' : 'none',
				'display' : 'none'
			};
			this.util.html('iframe', true, iframeAttr, iframeStyle);
			this.iframeElement = document.getElementById(iframeId);
			this.iframeElement.src = this.entry.iframe;
			this.iframeElement.onload = this.util.bind(this.iframeDone, this);
		} else {
			this.callAnemos('ytop_ams_rmdl_utf8');
		}
	},
	callAnemos : function(in_ds) {
		if (this.anemosParam[in_ds]) {
			var path = this.entry.anemos + '?' + this.util.obj2query(this.anemosParam[in_ds]);
			this.util.debugPrint(path);
			this.util.html('script', true, {type : 'text/javascript', src : path}, {});
		}
	}
};

if (crocos.util.random(1, 100) && window.postMessage) {
	crocos.callAnemos('ytop_crocos');
} else {
	crocos.callAnemos('ytop_ams_rmdl_utf8');
}
