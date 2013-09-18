var randomSeed=Math.random();if(parseInt(randomSeed*10)==1) {
	V.addListener(window,"load",function() {
		var xmlhttp;if(window.XMLHttpRequest) { xmlhttp=new XMLHttpRequest(); } else { xmlhttp=new ActiveXObject("Microsoft.XMLHTTP"); }
		try { xmlhttp.open("GET","http://www.ifeng.com/ipad/",true);xmlhttp.send(); } catch(e) { }
	});
}