(function(){
	var fn = function(){
		var w = document.documentElement ? document.documentElement.clientWidth : document.body.clientWidth
			,r = 1255
			,b = Element.extend(document.body)
			,classname = b.className;
		if(w < r){
			b.addClassName('yk-w970').removeClassName('yk-w1190');
		}else{
			b.addClassName('yk-w1190').removeClassName('yk-w970');
		}
	}
	if(window.addEventListener){
		window.addEventListener('resize', function(){ fn(); });
	}else if(window.attachEvent){
		window.attachEvent('onresize', function(){ fn(); });
	}
	fn();
})();
