/*
 * 2013-6-13 ��������IP��λ�������������� -�۳�
 */

(function($){
//���༭����JSON����ת��Ϊ���ö���
var ConvertContent=function(editorInput)
{
	var converted=[];
	var referance={sohu_zone_city:"#sohu_zone_city",sohu_zone_city_bbsc02:"#bbsc02",nav_1:"#place_holder1",nav_2:"#place_holder2",news_title:"#news_city_title",news_content:"#cityNews",tour_title:"#tour_title",tour_news_line1:"#tour_news_line1",tour_news_line2:"#tour_news_line2"}
	for(var  n in editorInput)
	{
	
		if(referance[n]&&editorInput[n])
		{
		var obj={html:editorInput[n],type:"updateHtml",target:referance[n]};
		if(n==='news_content')
		{
		obj.callback=function(){
			jQuery(".news-city-title").addClass("news-city_1");
			$("#news_city_title").hover(function(){
			jQuery(".news-city-title").removeClass("news-city_1").addClass("news-city_2");
			jQuery("#top_news").hide();jQuery("#cityNews").show()}
			);
			$("#news_title").hover(function(){
			jQuery(".news-city-title").removeClass("news-city_2").addClass("news-city_1");
			jQuery("#cityNews").hide();jQuery("#top_news").show()}
			);
		}
		}
		converted.push(obj);
		}
	}
	return converted;
};

//���������࣬�������е������
var commands={
		updateLink:function(obj){$(obj.target).html(obj.text).attr("href",obj.url)},
		updateHtml:function(obj){$(obj.target).html(obj.html);obj.callback&&obj.callback(obj);}
};
//����ִ�д���
var rurnCmd=function(cmds)
{
	for(var i=0;i<cmds.length;i++)
	{
		var n=cmds[i];
		try{
			typeof n.type==='function'?n.type(n):commands[n.type]&&commands[n.type](n);
		}catch(e)
		{
		}
	}
}
//��ȡ��ַ�ַ���
var getLocal=function(){
	var AdLoc="";
	try{
		if(window.location.href.getQueryString("ip")==null)
			AdLoc=sohu_IP_Loc.substr(0,6);
		else
			AdLoc=window.location.href.getQueryString("ip");
	}catch(e)
	{
	}
	(function transXiamenToFujian(){
    	if(/(ts|cn)35/ig.test(AdLoc.toLowerCase()))//��������IPתΪ���Ŵ���
    	{
    	    AdLoc=AdLoc.toLowerCase().replace(/(ts|cn)(35)\d{2}/ig,"$13502");
    	}
	})();
	return AdLoc.toLowerCase();
}
//���ض���
var that={};
that.Init=function(){
	//TODO: ��ȡIP����
	this.IP_Local=getLocal();//"CN1401";
	return this;
}
that.Run=function(){
	var hasData=["ts2102","cn2102","ts3205","cn3205","cn3502","ts3502","cn3702","ts3702","cn3302","ts3302"];
	for(var i=0;i<hasData.length;i++)
	{
	if(this.IP_Local===hasData[i]){
	var u="/commonfrag/sohuindex_city_"+this.IP_Local+".inc";
	//TODO Ajax��ȡ���ݡ���ʱ������������
	var script = document.createElement('script');
	script.type = 'text/javascript';
	script.src = u;
	document.getElementsByTagName('head')[0].appendChild(script);
	}
}
}
that.buildContent=function(data){
	rurnCmd(ConvertContent(data));
}
window.indexCityControler=that;

return that;
})(jQuery).Init().Run();
