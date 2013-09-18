function getprms()
{
	var prms = {};
	var loc=location.search;
	if (loc){	 
		loc=loc.substring(1);
		var parms=loc.split('&');	 
		for(var i=0;i<parms.length;i++){	 
			kv=parms[i].split('=');
			prms[kv[0]]=unescape(kv.slice(1).join("="));	 
		}	 
	}  
	return prms;
}
function toQueryString(obj) {
    var parts = [];
    for (var i in obj) {
        if (obj.hasOwnProperty(i)) {
            parts.push(encodeURIComponent(i) + "=" + encodeURIComponent(obj[i]));
        }
    }
    return parts.join("&");
}
function adextent_Get_Cookie( check_name ) {
			var a_all_cookies = document.cookie.split( ';' );
			var a_temp_cookie = '';
			var cookie_name = '';
			var cookie_value = '';
			var b_cookie_found = false; 
			for ( i = 0; i < a_all_cookies.length; i++ ){
				a_temp_cookie = a_all_cookies[i].split( '=' );
				cookie_name = a_temp_cookie[0].replace(/^\s+|\s+$/g, '');
				if ( cookie_name == check_name ){
					b_cookie_found = true;
					if ( a_temp_cookie.length > 1 ){
						cookie_value = unescape(a_temp_cookie.slice(1).join("=").replace(/^\s+|\s+$/g, '') );
					}
					return cookie_value;
					break;
				}
				a_temp_cookie = null;
				cookie_name = '';
			}
			if ( !b_cookie_found ){
				return null;
			}
		}

function addScript(scriptPath)
{
	var th = document.getElementsByTagName("body")[0];
	if(!th)
		return;
	var s = document.createElement('script');
	s.setAttribute('type','text/javascript');
	s.setAttribute('src',scriptPath);
	s.onError = s.onerror = function() 
	{		
		return false;
	}
	th.appendChild(s);	
}
function cleanUrl(url)
{
	//url = encodeURIComponent(url.replace("://www.","://").replace(/\/$/,"")).toLowerCase().replace(/%/g,"");	
	return url.replace(/\/$/,"").replace(/\+/g,"2b").replace(/,/g,"2c") + ".js";
}
function SetSeg(url, type) {
    type = type || 'img';
    var th = document.getElementsByTagName("body")[0];
	if(!th)
		return;
    var s = document.createElement(type);
    if(type == 'script'){
        s.async = true;
    }
    s.setAttribute('width', '1');
    s.setAttribute('height', '1');
    s.setAttribute('style', 'visibility:hidden;position:absolute;z-index:-50;');
    s.setAttribute('src', url);
    th.appendChild(s);
}
var adextent_use_exp = false;
var adextent_exp = "";
var global_ansn_urlparams = "";
var adextent_secured = ('https:' == document.location.protocol);

function addSetSegmentIFrame(domain,segment,networksegment, logic, aexp, network, multiproduct, numproducts)
{
	var th = document.getElementsByTagName("body")[0];
	if(!th)
		return;
	var s = document.createElement('iframe');	
	logic = logic || "last";
	aexp = aexp || "never";
	if(adextent_use_exp)
	{
		aexp = adextent_exp;
	}
	var adextent_utmz = adextent_Get_Cookie("__utmz");
	var adextent_utmzparam = "";
	if(adextent_utmz){
		adextent_utmzparam ="&utmz="+escape(adextent_utmz);
	}
	var p = network ? network.toLocaleLowerCase() + 'segment' : 'ansegment';
	var urldomain = adextent_secured ? "https://ssl.adextent.com" : "http://dynads.adextent.com";
	var par = {};
	par['domain'] = domain;
	par['segment'] = segment;
	par[p] = networksegment;
	par['exp'] = aexp;
	par['logic'] = logic;
	if(adextent_utmz)
		par['utmz'] = adextent_utmz;
	par['moreparams'] = global_ansn_urlparams;
	if(multiproduct)
		par['multiproduct'] = multiproduct;
	if(numproducts)
		par['numproducts'] = numproducts;
	var iframeurl = urldomain + "/ansn-creative/dynads/ServeS3.ashx/SetSegment.html?" + toQueryString(par); 
	s.setAttribute('width','1px;');
	s.setAttribute('height','1px;');
	s.setAttribute('style','visibility:hidden;position:absolute;z-index:-50;');	
	s.setAttribute('src',iframeurl);	
	th.appendChild(s);	
}
function addSetRMSegmentIFrame(domain,segment,rmsegment, logic, aexp)
{
	var th = document.getElementsByTagName("body")[0];
	if(!th)
		return;
	var s = document.createElement('iframe');	
	logic = logic || "last";
	aexp = aexp || "never";
	if(adextent_use_exp)
	{
		aexp = adextent_exp;
	}
	var adextent_utmz = adextent_Get_Cookie("__utmz");
	var adextent_utmzparam = "";
	if(adextent_utmz){
		adextent_utmzparam ="&utmz="+escape(adextent_utmz);
	}
	var iframeurl = "http://dynads.adextent.com/ansn-creative/dynads/ServeS3.ashx/SetSegment.html?domain=" + domain + "&segment=" + segment + "&rmsegment=" + rmsegment + "&exp=" + aexp + "&logic=" + logic +adextent_utmzparam+"&moreparams=" + global_ansn_urlparams; 
	if(adextent_secured){
	  iframeurl = "https://ssl.adextent.com/ansn-creative/dynads/ServeS3.ashx/SetSegment.html?domain=" + domain + "&segment=" + segment + "&rmsegment=" + rmsegment + "&exp=" + aexp + "&logic=" + logic +adextent_utmzparam+"&moreparams=" + global_ansn_urlparams; 
	}
	s.setAttribute('width','1px;');
	s.setAttribute('height','1px;');
	s.setAttribute('style','visibility:hidden;position:absolute;z-index:-50;');	
	s.setAttribute('src',iframeurl);	
	th.appendChild(s);	
}

var excludeDomains = ['babylon.com','orbitz.com','cheaptickets.com','file-extractor.com'];
function fireNetworkSegment()
{	
	for(var i=0; i<excludeDomains.length; ++i)
		if(location.href.indexOf(excludeDomains[i]) != -1)
			return;
	var th = document.getElementsByTagName("body")[0];
	if(!th)
		return;
	var s = document.createElement('img');				
	var domain = typeof(adextent_secured) == "undefined" || !adextent_secured ? 
		"http://ib.adnxs.com/" : "https://secure.adnxs.com/";
	
	var imgurl = domain + "seg?add=705716&t=2";
	s.setAttribute('width','1;');
	s.setAttribute('height','1;');
	s.setAttribute('style','visibility:hidden;position:absolute;z-index:-50;');	
	s.setAttribute('src',imgurl);	
	th.appendChild(s);
}

		
function addHeliumPixel(refererUrl, secured) {
    var url = "helium.adextent.com/vj?url=" + refererUrl;
    if (!secured)
        url = "http://" + url;
    else
        url = "https://" + url;

    SetSeg(url, "iframe");
}

function fireAdextentPixel(overrideUrl){
	var usedparam = false;
	var params = getprms();
	var adextent_usekw = false;

	var reurl = overrideUrl || (window.self === window.top ? location.href : document.referrer);
	var scripts = document.getElementsByTagName('script');
	for (var i = 0; i < scripts.length; ++i) {
		if(scripts[i].src.indexOf("/dynads/ServeS3.ashx/autotag") != -1){
			if(scripts[i].src.indexOf("?") != -1){
				var prms1=new Array();
				var loc = scripts[i].src.split("?")[1];
				var parms = loc.split('&');	 
				for(var i=0;i<parms.length;i++){	 
					kv=parms[i].split('=');
					prms1[kv[0]]=unescape(kv.slice(1).join("="));	 
				}
				if(prms1["url"] && !overrideUrl){
					reurl = prms1["url"];
					usedparam = true;
				}
				if(prms1["i"]){
					imode = prms1["i"];
					if(imode == "kw")
					{
						 adextent_usekw = true;
					}
				}
				if(prms1["expire"])
				{
					adextent_use_exp = true;
					adextent_exp = prms1["expire"];
				}
				global_ansn_urlparams = escape(loc);
			}
			break;
		}
	}

	var adextent_tag_domain =reurl.split("/")[2];
	var adextent_tag_url = reurl;
	if(params["domain"] && !overrideUrl)
		adextent_tag_domain = params["domain"];
	if(params["url"] && !overrideUrl)
		adextent_tag_url = params["url"];

	var canonical = "";
	var links = document.getElementsByTagName("link");
	for (var i = 0; i < links.length; i ++) {
		if (links[i].getAttribute("rel") === "canonical") {
			canonical = links[i].getAttribute("href")
		}
	}
	if(!usedparam && canonical != "" && reurl.indexOf('symantec.com') == -1 && reurl.indexOf('pimkie.es') == -1 && !overrideUrl)
	{
		adextent_tag_url = canonical;	
	}
	if((typeof(adextent_dynads_url) != "undefined")  && !overrideUrl)
		adextent_tag_url = adextent_dynads_url;

	if(adextent_tag_url.match(/^\//gi)){
		adextent_tag_url = "http://" + adextent_tag_domain + adextent_tag_url;
		if(adextent_secured){
			adextent_tag_url = "https://" + adextent_tag_domain + adextent_tag_url;
		}		
	}
	adextent_tag_url = cleanUrl(adextent_tag_url);
	adextent_tag_domain = adextent_tag_domain.toLowerCase().replace("www.","");
	if(adextent_tag_url.indexOf('http://') != 0 && adextent_tag_url.indexOf('https://') != 0){
		adextent_tag_url = document.location.protocol + '//' + adextent_tag_domain + '/' + adextent_tag_url
	}

	var scriptForPagePath = "http://dynads.adextent.com/ansn-creative/dynads/ServeS3.ashx/";
	if(adextent_secured){
		 scriptForPagePath = "https://ssl.adextent.com/ansn-creative/dynads/ServeS3.ashx/";
	}
	if(adextent_usekw){
		var adextent_utmz = adextent_Get_Cookie("__utmz");	
		if(adextent_utmz){
			var utmparts = adextent_utmz.split('|')
			for(var ii=0; ii < utmparts.length;ii++)
			{
				var utmkwpart =  utmparts[ii];
				var kwspl = utmkwpart.split('=');
				var kwprm = kwspl[0];
				if(kwprm == 'utmctr')
				{
					if(kwspl.length < 2)
						continue;

					var kwstr = kwspl[1].toLowerCase();	
					scriptForPagePath = scriptForPagePath + "k/" +adextent_tag_domain+"/"+ escape(kwstr.replace(/ /g,"2b")) +".js";
					addScript(scriptForPagePath);
					break;
				}			
			}
		}
	}
	else{
		if(usedparam)
		{
			scriptForPagePath = scriptForPagePath + "r";
		}
		else
		{
			scriptForPagePath = scriptForPagePath + "u";
		}
		scriptForPagePath = scriptForPagePath + "/" + adextent_tag_domain + "/" + escape(adextent_tag_url);

    //var testHeliumGroupSize = 10;
    // helium test group
    var test_number = Math.floor(Math.random() * 100);
    if ((reurl.indexOf('bestbuyeyeglasses.com') != -1) && !adextent_secured ) { // && test_number <= testHeliumGroupSize
        addHeliumPixel(escape(adextent_tag_url), adextent_secured);
    }
//    if ((reurl.indexOf('monstercable.com') != -1) && !adextent_secured && test_number <= 10) {
//        addHeliumPixel(escape(adextent_tag_url), adextent_secured);
//    }
    //else
	addScript(scriptForPagePath);
	}
}

fireAdextentPixel();
fireNetworkSegment();