//for statistics
try {
	aptracker.request();
} catch(e) {
	//
}

(function(){
	var jsUrl = 'http://m0.ifengimg.com/ifeng/sources/120330-ifeng-m.js';
	var picPages = ['photo','gaoqingtu','bigpicture'];
	var thisDomain = location.hostname;
	var thisPathName = location.pathname;
	var thisFileName = thisPathName.substr(thisPathName.lastIndexOf('/') + 1);

	if (thisFileName != '' && thisFileName != 'index.shtml' && thisDomain != 'v.ifeng.com') { // 非首页,非视频页
		var isPic = false;
		for(var j=0;j < picPages.length;j++){
			if(new RegExp(picPages[j]).test(thisPathName)){
				isPic = true;
				break;
			}
		}
		if(!isPic){ // 非图片页
			var mv = document.createElement('script');
			mv.type = 'text/javascript';
			mv.async = true;
			mv.src = jsUrl;
			var s = document.getElementsByTagName('script')[0];
			s.parentNode.insertBefore(mv, s);
		}
	}
})();
