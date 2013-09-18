function DispImgByIp(disp_img_arr_t, is_scroll_t, scroll_delay_t, var_name_sign, loc_t){
	var pause = false;
	var curid = 1;
	var lastid = 1;
	var sw = 1;
	var disp_opacity = 100;
	var is_scroll=1;
	var scroll_delay=10000;
	var disp_delay = 10;
	var disp_img_arr=new Array();
	var disp_img_num = 1;
	var rand_idx = 1;
	var oi;
	var disp_img_url = new Array();
	var _self = this;
	var s1, s2, s3, s4;
	var City;
	var Province;
	var Loc;

	var rand=function(num){
		return Math.ceil(Math.random()*num);
	}

	this.StartScroll=function(){
		s2 = setInterval(function(){_self.ScrollImg()}, disp_delay);
	}

	this.CheckLoad=function(){
		clearInterval(s1);
		s3 = setTimeout(function(){_self.CheckLoad()}, scroll_delay);
		s4 = setTimeout(function(){_self.StartScroll()}, scroll_delay);
	}

	var IpCheck=function(){
		if (Loc)
		{
			City = Loc;
			Province=Loc.substr(0,4);
			DataResetByIp();
		}
		else
		{
			var s = document.createElement("SCRIPT");
			s.src="http://ip.cms.sohu.com/ip.do?type=json&_="+new Date().getTime();
			s.type = "text/javascript";
			s.language = "javascript";
			document.getElementsByTagName("HEAD")[0].appendChild(s);
			if(document.all){
				s.onreadystatechange = function(){//IE用
					var state = s.readyState;
					if (state == "loaded") {
						City = returnCitySN.cname.split("-")[1];
						Province = returnCitySN.cname.split("-")[0];
						DataResetByIp();
					}
				};
			} else {
				s.onload = function() {//FF用 
					City = returnCitySN.cname.split("-")[1];
					Province = returnCitySN.cname.split("-")[0];
					DataResetByIp();
				};
			}
		}
	}

	var DataResetByIp=function(){
		var disp_img_arr_t = new Array();
		for(var n = 1; n <= disp_img_num; n++)
		{
			for (var i in disp_img_arr[n])
			{
				if (Object.prototype.toString.apply(disp_img_arr[n][i]) === '[object Array]')
				{
					if (i.indexOf(Province) != -1)
					{
						disp_img_arr_t[n] = disp_img_arr[n][i];
					}
					if (i.indexOf(City) != -1)
					{
						disp_img_arr_t[n] = disp_img_arr[n][i];
					}
				}
				else
				{
					disp_img_arr_t[n] = disp_img_arr[n];
				}
			}
		}
		disp_img_arr = disp_img_arr_t;
		for(var n = 1; n <= disp_img_num; n++)
		{
			disp_img_url[n] = new Image();
			disp_img_url[n].src = disp_img_arr[n][0];
		}

		oi.innerHTML = "<a href='" + disp_img_arr[rand_idx][1]+"' target='_blank'><img src='" + disp_img_url[rand_idx].src + "' border='0' width='" + disp_img_arr[rand_idx][3] + "' height='"+disp_img_arr[rand_idx][4]+"' style='filter:Alpha(opacity=100)' alt='"+disp_img_arr[rand_idx][2]+"'></a>";
	}

	this.initialize = function(){
		is_scroll=is_scroll_t;
		scroll_delay=scroll_delay_t;
		disp_img_arr=disp_img_arr_t;
		disp_img_num = disp_img_arr.length - 1;
		rand_idx = rand(disp_img_num);
		if (loc_t && loc_t != "undefined")
		{
			Loc = loc_t;
		}

		var str = "<div style='position:relative;display:inline' id='dimg"+var_name_sign+"' onmouseover='var dti=eval(\"display_by_ip_"+var_name_sign+"\");dti.Pause(true)' onmouseout='var dti=eval(\"display_by_ip_"+var_name_sign+"\");dti.Pause(false)'>　</div>";
		document.write(str);
		oi = document.getElementById("dimg"+var_name_sign);

		IpCheck();
		if (is_scroll)
			s1 = setInterval(function(){_self.CheckLoad()}, 1);
	}

	var SetAlpha=function(){
		if(document.all){
		if(oi.filters && oi.filters.Alpha) oi.filters.Alpha.opacity = disp_opacity;
		}else{
		oi.style.MozOpacity = ((disp_opacity >= 100)? 99:disp_opacity) / 100;
		}
	}

	this.ImgSwitch=function(id, p){
		if(p){
			pause = true;
			disp_opacity = 100;
			SetAlpha();
		}
		oi.innerHTML = "<a href='" + disp_img_arr[id][1]+"' target='_blank'><img src='" + disp_img_url[id].src + "' border='0' width='" + disp_img_arr[id][3] + "' height='"+disp_img_arr[id][4]+"' style='filter:Alpha(opacity=100)' alt='"+disp_img_arr[id][2]+"'></a>";
		curid = lastid = id;
	}

	this.ScrollImg=function(){
		if(pause && disp_opacity >= 100) return;
		if(sw == 0){
			disp_opacity += 2;
			if(disp_opacity > disp_delay){ disp_opacity = 100; sw = 1; clearInterval(s2);}
		}
		if(sw == 1){
			disp_opacity -= 3;
			if(disp_opacity < 10){ disp_opacity = 10; sw = 3;}
		}
		SetAlpha();
		if(sw != 3) return;
		sw = 0;
		curid++;

		if(curid > (disp_img_num)) curid = 1;
		this.ImgSwitch(curid, false);
	}

	this.Pause=function(s){
		if (s)
		{
			clearInterval(s2);
			clearTimeout(s3);
			clearTimeout(s4);
		}
		else if (is_scroll)
		{
			this.CheckLoad();
		}
		pause = s;
	}

	this.initialize();
}


function DispTxtByIp(disp_txt_arr_t, is_scroll_t, scroll_delay_t, var_name_sign, loc_t){
	var pause = false;
	var curid = 1;
	var lastid = 1;
	var sw = 1;
	var disp_opacity = 100;
	var is_scroll=1;
	var scroll_delay=10000;
	var disp_delay = 10;
	var disp_txt_arr=new Array();
	var disp_txt_num = 1;
	var rand_idx = 1;
	var oi;
	var disp_txt_url = new Array();
	var _self = this;
	var s1, s2, s3, s4;
	var City;
	var Province;
	var Loc;

	var rnd=function(){
		seed = (seed*9301+49297) % 233280;
		return seed/(233280.0);
	}

	var rand=function(num){
		return Math.ceil(Math.random()*num);
	}

	this.StartScroll=function(){
		s2 = setInterval(function(){_self.ScrollTxt()}, disp_delay);
	}

	this.CheckLoad=function(){
		clearInterval(s1);
		s3 = setTimeout(function(){_self.CheckLoad()}, scroll_delay);
		s4 = setTimeout(function(){_self.StartScroll()}, scroll_delay);
	}

	var IpCheck=function(){
		if (Loc)
		{
			City = Loc;
			Province=Loc.substr(0,4);
			DataResetByIp();
		}
		else
		{
			var s = document.createElement("SCRIPT");
			s.src="http://ip.cms.sohu.com/ip.do?type=json&_="+new Date().getTime();
			s.type = "text/javascript";
			s.language = "javascript";
			document.getElementsByTagName("HEAD")[0].appendChild(s);
			if(document.all){
				s.onreadystatechange = function(){//IE用
					var state = s.readyState;
					if (state == "loaded") {
						City = returnCitySN.cname.split("-")[1];
						Province = returnCitySN.cname.split("-")[0];
						DataResetByIp();
					}
				};
			} else {
				s.onload = function() {//FF用 
					City = returnCitySN.cname.split("-")[1];
					Province = returnCitySN.cname.split("-")[0];
					DataResetByIp();
				};
			}
		}
	}

	var IpUpdate=function(){
//		document.getElementsByTagName("HEAD")[0].removeChild(document.getElementById(ip_script_id)); 

		var str = "", jsonData, jsonLength = 0;
		jsonData = IpInfo;
		City = jsonData.City;
		Province = jsonData.Province;
		var date=new Date();
		var expireDays=10;
		date.setTime(date.getTime()+expireDays*24*3600*1000);
		document.cookie="CityByIp="+City+"; ProvinceByIp="+Province+"; expires="+date.toGMTString();

/*
		var xmlHttp;
		if(window.ActiveXObject){
			xmlHttp = new ActiveXObject("Microsoft.XMLHTTP"); 
		} 
		else if(window.XMLHttpRequest){ 
			xmlHttp = new XMLHttpRequest(); 
		} 

		xmlHttp.onreadystatechange = function(){
			if(xmlHttp.readyState == 4){
				if (xmlHttp.status == 200 || xmlHttp.status == 0){ 
					var json_str = xmlHttp.responseText;
					eval(json_str);
					City = IpInfo.City;
					Province = IpInfo.Province;

//					var xmlDOM = xmlHttp.responseXML;
//					var root = xmlDOM.documentElement;
//					City = root.getElementsByTagName('City')[0].firstChild.data;
//					Province = root.getElementsByTagName('Province')[0].firstChild.data;

				}
			}
		};
		xmlHttp.open("GET", "http://m.sohu.com/ip/ip_json.php?_="+new Date().getTime()+"&sip=", false); 
		xmlHttp.setRequestHeader("If-Modified-Since","0");
		xmlHttp.send(null); 
*/
	}

	var GetCity = function(){
		var strCookie=document.cookie;
		var arrCookie=strCookie.split("; ");
		for(var i=0;i<arrCookie.length;i++){
			var arr=arrCookie[i].split("=");
			if("CityByIp"==arr[0]){
				City=arr[1];
				break;
			}
		}
	}

	var GetProvince = function(){
		var strCookie=document.cookie;
		var arrCookie=strCookie.split("; ");
		for(var i=0;i<arrCookie.length;i++){
			var arr=arrCookie[i].split("=");
			if("ProvinceByIp"==arr[0]){
				Province=arr[1];
				break;
			}
		}
	}

	var DataResetByIp=function(){
		var disp_txt_arr_t = new Array();
		for(var n = 1; n <= disp_txt_num; n++)
		{
			for (var i in disp_txt_arr[n])
			{
				if (Object.prototype.toString.apply(disp_txt_arr[n][i]) === '[object Array]')
				{
					if (i.indexOf(Province) != -1)
					{
						disp_txt_arr_t[n] = disp_txt_arr[n][i];
					}
					if (i.indexOf(City) != -1)
					{
						disp_txt_arr_t[n] = disp_txt_arr[n][i];
					}
				}
				else
				{
					disp_txt_arr_t[n] = disp_txt_arr[n];
				}
			}
		}
		disp_txt_arr = disp_txt_arr_t;
		oi.innerHTML = "<a href='" + disp_txt_arr[rand_idx][1]+"' target='_blank' title=" + disp_txt_arr[rand_idx][2]+">" + disp_txt_arr[rand_idx][0]+"</a>";
	}


	this.initialize = function(){
		is_scroll=is_scroll_t;
		scroll_delay=scroll_delay_t;
		disp_txt_arr=disp_txt_arr_t;
		disp_txt_num = disp_txt_arr.length - 1;
		rand_idx = rand(disp_txt_num);
		if (loc_t && loc_t != "undefined")
		{
			Loc = loc_t;
		}

		var str = "<div style='position:relative;display:inline' id='dtxt"+var_name_sign+"' onmouseover='var dti=eval(\"display_by_ip_"+var_name_sign+"\");dti.Pause(true)' onmouseout='var dti=eval(\"display_by_ip_"+var_name_sign+"\");dti.Pause(false)'>　</div>";
		document.write(str);
		oi = document.getElementById("dtxt"+var_name_sign);

		IpCheck();
		if (is_scroll)
			s1 = setInterval(function(){_self.CheckLoad()}, 1);
	}

	var SetAlpha=function(){
		if(document.all){
		if(oi.filters && oi.filters.Alpha) oi.filters.Alpha.opacity = disp_opacity;
		}else{
		oi.style.MozOpacity = ((disp_opacity >= 100)? 99:disp_opacity) / 100;
		}
	}

	this.TxtSwitch=function(id, p){
		if(p){
			pause = true;
			disp_opacity = 100;
			SetAlpha();
		}
		oi.innerHTML = "<a href='" + disp_txt_arr[id][1]+"' target='_blank' title=" + disp_txt_arr[id][2]+">" + disp_txt_arr[id][0]+"</a>";
		curid = lastid = id;
	}

	this.ScrollTxt=function(){
		if(pause && disp_opacity >= 100) return;
		if(sw == 0){
			disp_opacity += 2;
			if(disp_opacity > disp_delay){ disp_opacity = 100; sw = 1; clearInterval(s2);}
		}
		if(sw == 1){
			disp_opacity -= 3;
			if(disp_opacity < 10){ disp_opacity = 10; sw = 3;}
		}
		SetAlpha();
		if(sw != 3) return;
		sw = 0;
		curid++;

		if(curid > (disp_txt_num)) curid = 1;
		this.TxtSwitch(curid, false);
	}

	this.Pause=function(s){
		if (s)
		{
			clearInterval(s2);
			clearTimeout(s3);
			clearTimeout(s4);
		}
		else if (is_scroll)
		{
			this.CheckLoad();
		}
		pause = s;
	}

	this.initialize();
}

function DispImgTxtByIp(disp_img_arr_t, disp_txt_arr_t, is_scroll_t, scroll_delay_t, var_name_sign, loc_t){
	var pause = false;
	var curid = 1;
	var lastid = 1;
	var sw = 1;
	var disp_opacity = 100;
	var is_scroll=1;
	var scroll_delay=10000;
	var disp_delay = 10;
	var disp_img_arr=new Array();
	var disp_txt_arr=new Array();
	var disp_img_num = 1;
	var rand_idx = 1;
	var oi_img;
	var oi_txt;
	var disp_img_url = new Array();
	var _self = this;
	var s1, s2, s3, s4;
	var str_img;
	var str_txt;
	var City;
	var Province;
	var Loc;

	var rand=function(num){
		return Math.ceil(Math.random()*num);
	}

	this.StartScroll=function(){
		s2 = setInterval(function(){_self.Scroll()}, disp_delay);
	}

	this.CheckLoad=function(){
		clearInterval(s1);
		s3 = setTimeout(function(){_self.CheckLoad()}, scroll_delay);
		s4 = setTimeout(function(){_self.StartScroll()}, scroll_delay);
	}

	var IpCheck=function(){
		if (Loc)
		{
			City = Loc;
			Province=Loc.substr(0,4);
			DataResetByIp();
		}
		else
		{
			var s = document.createElement("SCRIPT");
			s.src="http://ip.cms.sohu.com/ip.do?type=json&_="+new Date().getTime();
			s.type = "text/javascript";
			s.language = "javascript";
			document.getElementsByTagName("HEAD")[0].appendChild(s);
			if(document.all){
				s.onreadystatechange = function(){//IE用
					var state = s.readyState;
					if (state == "loaded") {
						City = returnCitySN.cname.split("-")[1];
						Province = returnCitySN.cname.split("-")[0];
						DataResetByIp();
					}
				};
			} else {
				s.onload = function() {//FF用 
					City = returnCitySN.cname.split("-")[1];
					Province = returnCitySN.cname.split("-")[0];
					DataResetByIp();
				};
			}
		}
	}

	var DataResetByIp=function(){
		var disp_txt_arr_t = new Array();
		for(var n = 1; n <= disp_txt_num; n++)
		{
			for (var i in disp_txt_arr[n])
			{
				if (Object.prototype.toString.apply(disp_txt_arr[n][i]) === '[object Array]')
				{
					if (i.indexOf(Province) != -1)
					{
						disp_txt_arr_t[n] = disp_txt_arr[n][i];
					}
					if (i.indexOf(City) != -1)
					{
						disp_txt_arr_t[n] = disp_txt_arr[n][i];
					}
				}
				else
				{
					disp_txt_arr_t[n] = disp_txt_arr[n];
				}
			}
		}
		disp_txt_arr = disp_txt_arr_t;
		oi_txt.innerHTML = "<a href='" + disp_txt_arr[rand_idx][1]+"' target='_blank' title=" + disp_txt_arr[rand_idx][2]+">" + disp_txt_arr[rand_idx][0]+"</a>";

		var disp_img_arr_t = new Array();
		for(var n = 1; n <= disp_img_num; n++)
		{
			for (var i in disp_img_arr[n])
			{
				if (Object.prototype.toString.apply(disp_img_arr[n][i]) === '[object Array]')
				{
					if (i.indexOf(Province) != -1)
					{
						disp_img_arr_t[n] = disp_img_arr[n][i];
					}
					if (i.indexOf(City) != -1)
					{
						disp_img_arr_t[n] = disp_img_arr[n][i];
					}
				}
				else
				{
					disp_img_arr_t[n] = disp_img_arr[n];
				}
			}
		}
		disp_img_arr = disp_img_arr_t;
		for(var n = 1; n <= disp_img_num; n++)
		{
			disp_img_url[n] = new Image();
			disp_img_url[n].src = disp_img_arr[n][0];
		}
		oi_img.innerHTML = "<a href='" + disp_img_arr[rand_idx][1]+"' target='_blank'><img src='" + disp_img_url[rand_idx].src + "' border='0' width='" + disp_img_arr[rand_idx][3] + "' height='"+disp_img_arr[rand_idx][4]+"' style='filter:Alpha(opacity=100)' alt='"+disp_img_arr[rand_idx][2]+"'></a>";
	}


	this.initialize = function(){
		is_scroll=is_scroll_t;
		scroll_delay=scroll_delay_t;
		disp_img_arr=disp_img_arr_t;
		disp_txt_arr=disp_txt_arr_t;
		disp_img_num = disp_img_arr.length - 1;
		disp_txt_num = disp_txt_arr.length - 1;
		rand_idx = rand(disp_img_num);
		if (loc_t && loc_t != "undefined")
		{
			Loc = loc_t;
		}

		str_img = "<div style='position:relative;display:inline' id='dimg"+var_name_sign+"' onmouseover='var dti=eval(\"display_by_ip_"+var_name_sign+"\");dti.Pause(true)' onmouseout='var dti=eval(\"display_by_ip_"+var_name_sign+"\");dti.Pause(false)'>　</div>";
		str_txt = "<div style='position:relative;display:inline' id='dtxt"+var_name_sign+"' onmouseover='var dti=eval(\"display_by_ip_"+var_name_sign+"\");dti.Pause(true)' onmouseout='var dti=eval(\"display_by_ip_"+var_name_sign+"\");dti.Pause(false)'>　</div>";
	}

	this.Start=function(){
		IpCheck();
		if (is_scroll)
			s1 = setInterval(function(){_self.CheckLoad()}, 1);
	}


	var SetAlphaImg=function(){
		if(document.all){
		if(oi_img.filters && oi_img.filters.Alpha) oi_img.filters.Alpha.opacity = disp_opacity;
		}else{
		oi_img.style.MozOpacity = ((disp_opacity >= 100)? 99:disp_opacity) / 100;
		}
	}

	var SetAlphaTxt=function(){
		if(document.all){
		if(oi_txt.filters && oi_txt.filters.Alpha) oi_txt.filters.Alpha.opacity = disp_opacity;
		}else{
		oi_txt.style.MozOpacity = ((disp_opacity >= 100)? 99:disp_opacity) / 100;
		}
	}

	this.TxtShow=function(){
		document.write(str_txt);
		oi_txt = document.getElementById("dtxt"+var_name_sign);
	}

	this.TxtSwitch=function(id, p){
		if(p){
			pause = true;
			disp_opacity = 100;
			SetAlphaTxt();
		}
		oi_txt.innerHTML = "<a href='" + disp_txt_arr[id][1]+"' target='_blank' title=" + disp_txt_arr[id][2]+">" + disp_txt_arr[id][0]+"</a>";
		curid = lastid = id;
	}

	this.ImgShow=function(){
		document.write(str_img);
		oi_img = document.getElementById("dimg"+var_name_sign);
	}

	this.ImgSwitch=function(id, p){
		if(p){
			pause = true;
			disp_opacity = 100;
			SetAlphaImg();
		}
		oi_img.innerHTML = "<a href='" + disp_img_arr[id][1]+"' target='_blank'><img src='" + disp_img_url[id].src + "' border='0' width='" + disp_img_arr[id][3] + "' height='"+disp_img_arr[id][4]+"' style='filter:Alpha(opacity=100)' alt='"+disp_img_arr[id][2]+"'></a>";
		curid = lastid = id;
	}

	this.Scroll=function(){
		if(pause && disp_opacity >= 100) return;
		if(sw == 0){
			disp_opacity += 2;
			if(disp_opacity > disp_delay){ disp_opacity = 100; sw = 1; clearInterval(s2);}
		}
		if(sw == 1){
			disp_opacity -= 3;
			if(disp_opacity < 10){ disp_opacity = 10; sw = 3;}
		}
		SetAlphaImg();
		if(sw != 3) return;
		sw = 0;
		curid++;

		if(curid > (disp_img_num)) curid = 1;
		this.ImgSwitch(curid, false);
		this.TxtSwitch(curid, false);
	}

	this.Pause=function(s){
		if (s)
		{
			clearInterval(s2);
			clearTimeout(s3);
			clearTimeout(s4);
		}
		else if (is_scroll)
		{
			this.CheckLoad();
		}
		pause = s;
	}

	this.initialize();
}