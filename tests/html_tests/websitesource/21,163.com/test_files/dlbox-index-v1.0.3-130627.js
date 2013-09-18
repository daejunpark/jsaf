/*
 * netease ADbox apply v1.0
 * Creation date: 2012/5/17
 * Modified date: xxxx/xxxx/xxxxx
*/
//优先判断浏览器
function tempCheckBroswer()
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
//浮层对象
var dlBox = new Object();
dlBox.broswer = tempCheckBroswer();
dlBox.isShow = 0;

if(dlBox.broswer)
{
	
	/*************浮层广告执行***********
	*/
	//配置信息
	//随机轮替
	//计算轮替总量
	dlBox.num = 0;
	for(dlBox.i=1; dlBox.i<=3; dlBox.i++)
	{
		if(typeof(this["adInfoTempDL"]["lsSrc"+dlBox.i])!="undefined")
		{
			dlBox.num ++;
		}
	}
	dlBox.num2 = Math.floor(Math.random()*adInfoTempDL.loop)+1;

	if(dlBox.num < dlBox.num2)
	{
		dlBox.num2 = 0;
	}else
	{
		dlBox.num = Math.floor(Math.random()*dlBox.num)+1;
	}

	dlBox.top = (typeof(adInfoTempDL.top)=="undefined")?0:adInfoTempDL.top;

	dlBox.lsWidth = (typeof(adInfoTempDL.lsWidth)=="undefined")?20:adInfoTempDL.lsWidth;
	dlBox.lsHeight = (typeof(adInfoTempDL.lsHeight)=="undefined")?300:adInfoTempDL.lsHeight;
	dlBox.lsSrc = (typeof(this["adInfoTempDL"]["lsSrc"+dlBox.num])=="undefined")?"":(this["adInfoTempDL"]["lsSrc"+dlBox.num]);

	dlBox.rsWidth = (typeof(adInfoTempDL.rsWidth)=="undefined")?20:adInfoTempDL.rsWidth;
	dlBox.rsHeight = (typeof(adInfoTempDL.rsHeight)=="undefined")?300:adInfoTempDL.rsHeight;
	dlBox.rsSrc = (typeof(this["adInfoTempDL"]["rsSrc"+dlBox.num])=="undefined")?"":(this["adInfoTempDL"]["rsSrc"+dlBox.num]);

	dlBox.lbWidth = (typeof(adInfoTempDL.lbWidth)=="undefined")?110:adInfoTempDL.lbWidth;
	dlBox.lbHeight = (typeof(adInfoTempDL.lbHeight)=="undefined")?300:adInfoTempDL.lbHeight;
	dlBox.lbSrc = (typeof(this["adInfoTempDL"]["lbSrc"+dlBox.num])=="undefined")?"":(this["adInfoTempDL"]["lbSrc"+dlBox.num]);

	dlBox.rbWidth = (typeof(adInfoTempDL.rbWidth)=="undefined")?110:adInfoTempDL.rbWidth;
	dlBox.rbHeight = (typeof(adInfoTempDL.rbHeight)=="undefined")?300:adInfoTempDL.rbHeight;
	dlBox.rbSrc = (typeof(this["adInfoTempDL"]["rbSrc"+dlBox.num])=="undefined")?"":(this["adInfoTempDL"]["rbSrc"+dlBox.num]);

	dlBox.lgSrc = (typeof(this["adInfoTempDL"]["lgSrc"+dlBox.num])=="undefined")?"":(this["adInfoTempDL"]["lgSrc"+dlBox.num]);
	dlBox.rgSrc =(typeof(this["adInfoTempDL"]["rgSrc"+dlBox.num])=="undefined")?"":(this["adInfoTempDL"]["rgSrc"+dlBox.num]);

	dlBox.url = (typeof(this["adInfoTempDL"]["url"+dlBox.num])=="undefined")?"":(this["adInfoTempDL"]["url"+dlBox.num]);

	//生成广告
	dlBox.createElement = function()
	{		
		if(this.isWidth == 1)
		{
			dlBox.lbWidth = 140;
			dlBox.lbHeight = 400;
			dlBox.rbWidth = 140;
			dlBox.rbHeight = 400;
			dlBox.lbSrc = dlBox.lgSrc;
			dlBox.rbSrc = dlBox.rgSrc;

			//left great
			this.leftBigFlag = adBox.createDiv(this.lbWidth,this.lbHeight);
			this.leftBigFlag.style.zIndex = 9999;
			this.leftBigStr = "<div id=\"dllbFrame\" style=\"width:"+this.lbWidth+"px; height:"+this.lbHeight+"px;\"></div><div id=\"dllbClickL\" onclick=\"dlBox.getURL()\"  style=\"width:"+this.lbWidth+"px; height:"+this.lbHeight+"px; background:#0CC; position:relative; left:0; top:-"+this.lbHeight+"px; cursor:pointer; filter:alpha(opacity=0); opacity:0; \"></div><div id=\"dllbClose\" onclick=\"dlBox.action('sClose')\" style=\"width:18px; height:18px;  position:relative; left:0; top:-"+this.lbHeight*2+"px; cursor:pointer;  \"><img src=\"http://img1.126.net/channel7/js/couplet/x.png\" height=\"18\" width=\"18\" border=\"0\" title=\"关闭广告\" alt=\"关闭按钮\" /></div>";
			this.leftBigFlag.innerHTML = this.leftBigStr;
			this.replayFrameL = document.getElementById("dllbFrame");
			this.replayFrameL.innerHTML = adBox.createSwf("dllbMovie",this.lbWidth,this.lbHeight,this.lbSrc);
			//this.leftBigFlag.style.display = "none";
			this.leftBigFlag.style.overflow = "hidden";

			//right great
			this.rightBigFlag = adBox.createDiv(this.rbWidth,this.rbHeight);
			this.rightBigFlag.style.zIndex = 9999;
			this.rightBigStr = "<div id=\"dlrbFrame\" style=\"width:"+this.rbWidth+"px; height:"+this.rbHeight+"px;\"></div><div id=\"dlrbClickL\" onclick=\"dlBox.getURL()\"  style=\"width:"+this.rbWidth+"px; height:"+this.rbHeight+"px; background:#0CC; position:relative; left:0; top:-"+this.rbHeight+"px; cursor:pointer; filter:alpha(opacity=0); opacity:0; \"></div><div id=\"dlrbClose\" onclick=\"dlBox.action('sClose')\" style=\"width:18px; height:18px;  position:relative; left:"+(this.rbWidth-18)+"px; top:-"+this.rbHeight*2+"px; cursor:pointer;  \"><img src=\"http://img1.126.net/channel7/js/couplet/x.png\" height=\"18\" width=\"18\" border=\"0\" title=\"关闭广告\" alt=\"关闭按钮\" /></div>";
			this.rightBigFlag.innerHTML = this.rightBigStr;
			this.replayFrameL = document.getElementById("dlrbFrame");
			this.replayFrameL.innerHTML = adBox.createSwf("dlrbMovie",this.rbWidth,this.rbHeight,this.rbSrc);
			//this.rightBigFlag.style.display = "none";
			this.rightBigFlag.style.overflow = "hidden";

			if(this.url == "")
			{
				document.getElementById("dllbClickL").style.visibility  =  "hidden"; 
				document.getElementById("dlrbClickL").style.visibility  =  "hidden"; 
			}
		}else
		{
			//left small
			this.leftSmallFlag = adBox.createDiv(this.lsWidth,this.lsHeight);
			this.leftSmallStr = "<div id=\"dllsFrame\" style=\"width:"+this.lsWidth+"px; height:"+this.lsHeight+"px;\"></div><div id=\"dllsClickL\" onmouseover=\"dlBox.action('showB')\" style=\"width:"+this.lsWidth+"px; height:"+this.lsHeight+"px; background:#0CC; position:relative; left:0; top:-"+this.lsHeight+"px; cursor:pointer; filter:alpha(opacity=0); opacity:0; \"></div>";
			this.leftSmallFlag.innerHTML = this.leftSmallStr;
			this.leftSmallFlag.style.zIndex = 9999;
			this.replayFrameL = document.getElementById("dllsFrame");
			//this.replayFrameL.innerHTML = adBox.createSwf("dllsMovie",this.lsWidth,this.lsHeight,this.lsSrc);
			this.replayFrameL.innerHTML = "<img src="+this.lsSrc+">";
			//this.leftSmallFlag.style.display = "none";
			this.leftSmallFlag.style.overflow = "hidden";

			//right small
			this.rightSmallFlag = adBox.createDiv(this.rsWidth,this.rsHeight);
			this.rightSmallStr = "<div id=\"dlrsFrame\" style=\"width:"+this.rsWidth+"px; height:"+this.rsHeight+"px;\"></div><div id=\"dlrsClickL\" onmouseover=\"dlBox.action('showB')\" style=\"width:"+this.rsWidth+"px; height:"+this.rsHeight+"px; background:#0CC; position:relative; left:0; top:-"+this.rsHeight+"px; cursor:pointer; filter:alpha(opacity=0); opacity:0; \"></div>";
			this.rightSmallFlag.innerHTML = this.rightSmallStr;
			this.rightSmallFlag.style.zIndex = 9999;
			this.replayFrameL = document.getElementById("dlrsFrame");
			this.replayFrameL.innerHTML = adBox.createSwf("dlrsMovie",this.rsWidth,this.rsHeight,this.rsSrc);
			//this.rightSmallFlag.style.display = "none";
			this.rightSmallFlag.style.overflow = "hidden";

			//left big
			this.leftBigFlag = adBox.createDiv(this.lbWidth,this.lbHeight);
			this.leftBigStr = "<div id=\"dllbFrame\" onmouseout=\"dlBox.action('hideS')\" style=\"width:"+this.lbWidth+"px; height:"+this.lbHeight+"px;\"></div><div id=\"dllbClickL\" onclick=\"dlBox.getURL()\" onmouseout=\"dlBox.action('hideS')\" style=\"width:"+this.lbWidth+"px; height:"+this.lbHeight+"px; background:#0CC; position:relative; left:0; top:-"+this.lbHeight+"px; cursor:pointer; filter:alpha(opacity=0); opacity:0; \"></div>";
			this.leftBigFlag.innerHTML = this.leftBigStr;
			this.leftBigFlag.style.zIndex = 9999;
			this.replayFrameL = document.getElementById("dllbFrame");
			this.replayFrameL.innerHTML = adBox.createSwf("dllbMovie",this.lbWidth,this.lbHeight,this.lbSrc);
			this.leftBigFlag.style.display = "none";
			this.leftBigFlag.style.overflow = "hidden";

			//right big
			this.rightBigFlag = adBox.createDiv(this.rbWidth,this.rbHeight);
			this.rightBigStr = "<div id=\"dlrbFrame\" onmouseout=\"dlBox.action('hideS')\"  style=\"width:"+this.rbWidth+"px; height:"+this.rbHeight+"px;\"></div><div id=\"dlrbClickL\" onclick=\"dlBox.getURL()\" onmouseout=\"dlBox.action('hideS')\" style=\"width:"+this.rbWidth+"px; height:"+this.rbHeight+"px; background:#0CC; position:relative; left:0; top:-"+this.rbHeight+"px; cursor:pointer; filter:alpha(opacity=0); opacity:0; \"></div>";
			this.rightBigFlag.innerHTML = this.rightBigStr;
			this.rightBigFlag.style.zIndex = 9999;
			this.replayFrameL = document.getElementById("dlrbFrame");
			this.replayFrameL.innerHTML = adBox.createSwf("dlrbMovie",this.rbWidth,this.rbHeight,this.rbSrc);
			this.rightBigFlag.style.display = "none";
			this.rightBigFlag.style.overflow = "hidden";

			if(this.url == "")
			{
				document.getElementById("dllbClickL").style.visibility  =  "hidden"; 
				document.getElementById("dlrbClickL").style.visibility  =  "hidden"; 
			}
		}
	}
	//设置位置
	dlBox.reSetPosition = function()
	{
		if(adBox.getClientInfo("top") >= this.top)
		{		
 			if(this.broswer == "ie6")
			{
				this.leftBigFlag.style.position = "absolute";
				this.leftBigFlag.style.top = adBox.getClientInfo("top")  +"px";
				this.rightBigFlag.style.position = "absolute";
				this.rightBigFlag.style.top = adBox.getClientInfo("top") +"px";
			}else
			{
				this.leftBigFlag.style.position = "fixed";
				this.leftBigFlag.style.top = 55 +"px";
				this.rightBigFlag.style.position = "fixed";
				this.rightBigFlag.style.top = 55 +"px";
			}
		}else
		{
			this.leftBigFlag.style.position = "absolute";
			this.leftBigFlag.style.top = this.top +"px";
			this.rightBigFlag.style.position = "absolute";
			this.rightBigFlag.style.top = this.top +"px";
		}
		this.leftBigFlag.style.left = "0px";
		this.rightBigFlag.style.left = adBox.getClientInfo("width") -  dlBox.rbWidth + "px";

		//小分辨率小对联
		if(this.isWidth == 0)
		{
			if(adBox.getClientInfo("top") >= this.top)
			{		
				if(this.broswer == "ie6")
				{
					this.leftSmallFlag.style.position = "absolute";
					this.leftSmallFlag.style.top = adBox.getClientInfo("top")  +"px";
					this.rightSmallFlag.style.position = "absolute";
					this.rightSmallFlag.style.top = adBox.getClientInfo("top") +"px";
				}else
				{
					this.leftSmallFlag.style.position = "fixed";
					this.leftSmallFlag.style.top = 55 +"px";
					this.rightSmallFlag.style.position = "fixed";
					this.rightSmallFlag.style.top = 55 +"px";
				}
			}else
			{
				this.leftSmallFlag.style.position = "absolute";
				this.leftSmallFlag.style.top = this.top +"px";
				this.rightSmallFlag.style.position = "absolute";
				this.rightSmallFlag.style.top = this.top +"px";
			}
			this.leftSmallFlag.style.left = "0px";
			this.rightSmallFlag.style.left = adBox.getClientInfo("width") -  dlBox.lsWidth + "px";
		}

		this.reSetPositionNum = setTimeout("dlBox.reSetPosition()",1000);
	}
	//交互
	dlBox.action = function(code)
	{
		if(code == "sClose")
		{
			this.leftBigFlag.style.display = "none";
			this.rightBigFlag.style.display = "none";
		}else if(code == "showB")
		{
			this.leftBigFlag.style.display = "block";
			this.rightBigFlag.style.display = "block";
		}else if(code == "hideS")
		{
			this.leftBigFlag.style.display = "none";
			this.rightBigFlag.style.display = "none";
		}
	}
	//打开链接
	dlBox.getURL = function()
	{
		window.open(this.url,"_blank");
	}
	//综合调用
	dlBox.go = function()
	{
		if(dlBox.isShow == 0)
		{
			if(dlBox.num2 != 0)
			{
				//判断分辨率
				if(screen.width >= 1280)
				{
					this.isWidth = 1;
				}else
				{
					this.isWidth = 0;
				}

				//生成元素
				this.createElement();
				//设置位置
				this.reSetPosition();
			}
			dlBox.isShow = 1;
		}
	}
	//dlBox.go();
	//执行结束
	/***************结束***************/
}