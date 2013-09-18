/*
 * netease ADbox base v1.0
 * Creation date: 2012/5/17
 * Modified date: xxxx/xxxx/xxxxx
*/

/************[adBox������Ϊһ��]************
 * adBox.getClientInfo (param) param:
                                 height:    ����ҳ��ɼ���
				                 width:     ����ҳ��ɼ���
				                 top:       ����ҳ�汻���ߵĸ߶�
				                 root:      ����������ں�����
 * adBox.createSwf  (id,width,height,src)
				                  id:       swf id
				 				  width:    swf�߶�
								  height:   swf�ļ��߶�
								  src:      swf�ļ���ַ
 * adBox.createDiv (width,height)
								  width:    div ���
								  height:    div�߶�
 * adBox.getElementPosition(e) ����Ԫ��e�ľ���λ��

 * adBox.swfLoadPross(name) ����flash������ؽ���
 * adBox.cookieCount(key) ����key�������cookie����
*********************����*******************/


/**************[adBox:����]***************
 **
 * ��������adBox
 */
//������������
var adBox = new Object();
/*****************����*********************/


/**********[adbox:�ͻ��˷ֱ�����Ϣ]********
 */
//��ȡ�������Ϣ
adBox.getClientInfo = function(param)
{
	if(param == "height")
	{
		return (document.body.clientHeight>document.documentElement.clientHeight)&&(document.documentElement.clientHeight!=0)?document.documentElement.clientHeight:document.body.clientHeight;
	}else if(param == "width")
	{
		return (document.documentElement.clientWidth>document.body.clientWidth)&&(document.documentElement.clientWidth!=0)?document.documentElement.clientWidth:document.body.clientWidth;
	}else if(param == "top")
	{
		return document.documentElement.scrollTop?document.documentElement.scrollTop:document.body.scrollTop;
	}else if(param == "root")
	{
		var Sys = {};
		var ua = navigator.userAgent.toLowerCase();
		var s;
		(s = ua.match(/msie ([\d.]+)/)) ? Sys.ie = s[1] :
		(s = ua.match(/firefox\/([\d.]+)/)) ? Sys.firefox = s[1] :
		(s = ua.match(/chrome\/([\d.]+)/)) ? Sys.chrome = s[1] :
		(s = ua.match(/opera.([\d.]+)/)) ? Sys.opera = s[1] :
		(s = ua.match(/version\/([\d.]+).*safari/)) ? Sys.safari = s[1] : 0;
		if(Sys.ie){	if(parseInt(Sys.ie)>=7){return "ie8";}else{return "ie6";}}
		else if(Sys.firefox){return "ff";}
		else if(Sys.chrome){return "chr";}
		else{return 0;}
	}
}
/*****************����********************/


/*************[adbox:Ԫ�ز���]*************/
//����swf
adBox.createSwf = function (id,width,height,src)
{
	var swf="<object id=\""+id+"\" classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" width=\""+width+"\" height=\""+height+"\">";
	swf+="<param name=\"movie\" value=\""+src+"\" />";
	swf+="<param name=\"wmode\" value=\"transparent\" />";
	swf+="<param name=\"quality\" value=\"high\" />";
	swf+="<param name=\"allowScriptAccess\" value=\"always\" />";
	swf+="<embed wmode=\"transparent\" src=\"" + src + "\" quality=\"high\" width=\"" + width + "\" height=\"" + height + "\" type=\"application/x-shockwave-flash\" allowScriptAccess=\"always\" name=\"" + id + "\"></embed>";
	swf+="</object>";
	return swf;
}
//���ɲ�����div
adBox.createDiv = function (width,height)
{
	var divObj = document.createElement("div");
	divObj.style.width = width+"px";
	divObj.style.height = height+"px";
	divObj.style.zIndex = 100;
	document.body.appendChild(divObj);
	return divObj;
}
//����Ԫ�ؾ���λ��
adBox.getElementPosition = function(e)   
{  
    var t = e.offsetTop;  
    var l = e.offsetLeft;  
    while(e = e.offsetParent)  
    {  
        t += e.offsetTop;  
    	l += e.offsetLeft;  
    }  
    return {left:l,top:t};  
}  
//����swf���ؽ���
adBox.swfLoadPross = function(list)
{
	var leight = list.leight;
	var pross = 100;
	for(var i =0; i<leight; i++)
	{
		pross *= window[list[i]].PercentLoaded()*0.01;
	}
	return pross;
}
//��������
adBox.getUrl = function(link)
{
	window.open(link,"_target");
}
//͸���Ȳ���
/*adBox.setAlpha = function(id,begin,end,action)
{
	var cur = begin;
	var num;
	if(begin>end)
	{
		num = -10;
		cur += num;
		alert(cur+":"+end)
		if(cur <= end)
		{
			alert(1)
			action();
			clearTimeout(adBox.alphaNum)
			cur = end;
		}else
		{
			//adBox.alphaNum = setTimeout("adBox.setAlpha('"+id+"','"+begin+"','"+end+"','"+action+"')",500);	
		}
	}else if(begin<end)
	{
		num = 10;
		cur += num;
		if(cur >= end)
		{
			cur = end;
		}else
		{
			action();
			clearTimeout(adBox.alphaNum)
			adBox.alphaNum = setTimeout("adBox.setAlpha('"+id+"','"+begin+"','"+end+"','"+action+"')",500);		
		}
	}else
	{
		return;
	}
	
	if(adBox.getClientInfo("root")=="ie6")
	{
		id.style.filter="alpha(opacity="+cur+")";
	}else
	{
		id.style.opacity = cur*0.01;
	}
}*/
/*****************����*******************/


/*******[adbox:cookie��ز���]************/
//����cookie key����
adBox.cookieCount = function(key,time)
{
	var today = new Date();
	var executeYear=today.getYear();
	var executeMonth=today.getMonth()+1;
	var executeDay=today.getDate();
	var executeDate=executeYear+""+executeMonth+""+executeDay;
	var date=executeDate;
	var channel="channel";
	var cookiesid="neteaseAD"+date+channel+"cookies"+key;
	var count = this.getCookie2(cookiesid);
	var expDays = 1;
	var exp = new Date();
	//�жϹ���ʱ��
	if(typeof(time) == "undefined")
	{
		exp.setTime(exp.getTime() + (expDays*24*60*60*1000));
	}else
	{
		exp.setTime(exp.getTime() + (expDays*time*1000));
	}
	
	if(count == null) 
	{
		this.setCookie2(cookiesid,'2',exp);
		count = 1;
	}else
	{
		var newcount = parseInt(count) + 1;
		if(newcount<2) count=1;
		this.setCookie2(cookiesid,newcount,exp);
	}
	return count;
}
//��ȡcookie
adBox.getCookie2 = function(name) 
{ 
	var arg = name + "="; 
	var alen = arg.length;
	var clen = document.cookie.length;
	var i = 0; 
	while (i < clen) 
	{ 
		var j   = i + alen; 
		if (document.cookie.substring(i, j) == arg)
		{
			return this.getCookieVal2 (j);
		}
		i = document.cookie.indexOf(" ", i) + 1;
		if (i == 0)
		{
			break;
		}
	}
	return null;
}
//������ȡcookie
adBox.getCookieVal2 = function(offset) 
{
	var endstr = document.cookie.indexOf (";", offset);
	if (endstr == -1)
	{
		endstr = document.cookie.length;
	}
	return unescape(document.cookie.substring(offset, endstr));
}
//����cookie
adBox.setCookie2 = function(name,value) 
{ 
	var argv = this.setCookie2.arguments;
	var argc = this.setCookie2.arguments.length;
	var expires = (argc > 2) ? argv[2] : null; 
	var path = (argc > 3) ? argv[3] : null; 
	var domain = (argc > 4) ? argv[4] : null; 
	var secure = (argc > 5) ? argv[5] : false; 
	document.cookie = name + "=" + escape (value) + 
	((expires == null) ? "" : ("; expires=" + expires.toGMTString())) + 
	((path == null) ? "" : ("; path=" + path)) + 
	((domain == null) ? "" : ("; domain=" + domain)) + 
	((secure == true) ? "; secure" : "");
}
/***************************����****************************/