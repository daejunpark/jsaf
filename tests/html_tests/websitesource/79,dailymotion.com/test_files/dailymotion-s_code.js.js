/* SiteCatalyst code version: H.20.3.
Copyright 1997-2009 Omniture, Inc. More info available at
http://www.omniture.com */
/************************ ADDITIONAL FEATURES ************************
     Plugins
*/
(function s_code(window)
{
    var s = s_gi(window.s_account);
    window.SiteCatalyst =
    {
        getInstance: function(id)
        {
            return (!id) ? s : s_gi(id);
        }
    };

/************************** CONFIG SECTION **************************/
/* You may add or alter any code config here. */
/* Conversion Config */
s.currencyCode="EUR"
/* Link Tracking Config */
s.trackDownloadLinks=true
s.trackExternalLinks=true
s.trackInlineStats=true
s.linkDownloadFileTypes="exe,zip,wav,mp3,mov,mpg,avi,wmv,pdf,doc,docx,xls,xlsx,ppt,pptx"
s.linkInternalFilters="javascript:,dailymotion.com"
s.linkLeaveQueryString=false
s.linkTrackVars=""
s.linkTrackEvents="event20,event21"
/* Plugin Config */
s.usePlugins=true
function s_doPlugins(s) {
    /* Add calls to plugins here */
    
    //Prop Copy in eVar
    if(s.pageName)s.eVar1="D=pageName"; if(s.prop1)s.eVar2="D=c1";  if(s.prop3)s.eVar3="D=c3";      
    if(s.prop4)s.eVar4="D=c4";      if(s.prop5)s.eVar5="D=c5";      if(s.prop6)s.eVar6="D=c6";      
    if(s.channel)s.eVar8="D=ch";    if(s.prop2)s.eVar9="D=c2";      if(s.prop9)s.eVar10="D=c9";     
    if(s.prop12)s.eVar15="D=c12";   if(s.prop13)s.eVar16="D=c13";
    if(s.prop14)s.eVar17="D=c14";   if(s.prop15)s.eVar18="D=c15";   if(s.prop16)s.eVar19="D=c16";
    if(s.prop17)s.eVar20="D=c17";   if(s.prop18)s.eVar21="D=c18";   if(s.prop19)s.eVar22="D=c19";
    if(s.prop20)s.eVar23="D=c20";   if(s.prop21)s.eVar24="D=c21";   if(s.prop22)s.eVar25="D=c22";
    if(s.prop23)s.eVar26="D=c23";   if(s.prop24)s.eVar27="D=c24";   if(s.prop25)s.eVar28="D=c25";
    if(s.prop26)s.eVar29="D=c26";   if(s.prop27)s.eVar30="D=c27";   if(s.prop28)s.eVar31="D=c28";
    if(s.prop29)s.eVar32="D=c29";   if(s.prop30)s.eVar33="D=c30";   if(s.prop31)s.eVar34="D=c31";
    if(s.prop32)s.eVar49="D=c32";   if(s.prop33)s.eVar50="D=c33";   if(s.prop36)s.eVar51="D=c36";
    if(s.prop33)s.eVar52="D=c33";   if(s.prop34)s.eVar53="D=c34";   if(s.prop31)s.eVar59="D=c31";
    if(s.prop32)s.eVar60="D=c32";   if(s.prop37)s.eVar61="D=c37";   if(s.prop38)s.eVar62="D=c38";
    
    
    // Plugin Example: using linkInternalFilters, returns link object
    var linkObject=s.exitLinkHandler('','true');
    if(linkObject){
        s.prop1=linkObject.href;
        s.events="event99";
        s.linkTrackVars="prop99,events";
        s.linkTrackEvents="event99";
    }
    
    
    /*Time Parting*/
    currDate = new Date();
    s.prop50 = s.getTimeParting('h', '+1', currDate.getFullYear()) // Set hour 
    s.prop51 = s.getTimeParting('d', '+1', currDate.getFullYear()) // Set day
    s.prop52 = s.getTimeParting('w', '+1', currDate.getFullYear()) // Set Weekend / Weekday

    if (s.prop50 && s.prop51) s.eVar49 = s.prop50 + '-' + s.prop51;

    //GetPercentView
    s.prop53 = s.getPreviousValue(s.pageName, "s_pv");
    s.prop55 = s.getPreviousValue(s.prop9, "s_p9");
    if (s.prop53) {        s.prop54 = s.getPercentPageViewed();    }

    /*Zoning Tracking*/
    s.hbx_lt = "manual"                                     //Set Link Tracking to Manual
    s.setupLinkTrack("eVar54", "SC_LNK_GZ");                //Call Plug-in with eVar40
    if (s.eVar54 == "no &lid") { s.eVar54 = "no vszone"; }

    //s.disableTracking = false;  
    s.sampratio = s.c_r('sc_ratio');
    if(!s.sampratio){s.sampratio = 25;}
    s.sampratio = Math.round((100/(Math.floor(s.sampratio)+1)));


    //Extrapolated events
    if(s.events){if(s.events.indexOf("event20",0)==-1){
    s.linkTrackEvents = s.apl(s.events, 'event22,event20,event21' , ',');
    s.events = s.apl(s.events, 'event22,event20,event21=' + s.sampratio , ',');
    }}

    if(!s.events) {s.events=s.apl(s.events, 'event22,event20,event21=' + s.sampratio , ',');  }

//External Campaigns
    s.campaign=s.getQueryParam('cid');
    s.campaign=s.getValOnce(s.campaign,"s_campaign",0)

    /* Event Recherches Internes */
    if (s.prop11){
        s.prop11 = s.prop11.toLowerCase()
        s.eVar11 = s.prop11
        var t_search = s.getValOnce(s.eVar11, 'ev11', 0)
        if (t_search)
            if (s.prop11) {
                if (s.prop40 == "0") {
                    s.prop40 = "zero";
                }
                if (s.prop40 == "zero") {
                    s.events = s.apl(s.events, 'event1,event24', ',');
                }
                else {
                    s.events = s.apl(s.events, 'event1', ',');
                }
            }
        if (!t_search) {
            if (!s.events) { s.events = ''; }
            if (!s.products) { s.products = ';'; }
        }
    }


}
s.doPlugins=s_doPlugins
/************************** PLUGINS SECTION *************************/
/* You may insert any plugins you wish to use here.                 */

/*
 * Plugin: exitLinkHandler 0.8 - identify and report exit links
 */
s.exitLinkHandler=new Function("p","e",""
+"var s=this,o=s.p_gh(),h=o.href,n='linkInternalFilters',i,t;if(!h||("
+"s.linkType&&(h||s.linkName)))return'';i=h.indexOf('?');t=s[n];s[n]="
+"p?p:t;h=s.linkLeaveQueryString||i<0?h:h.substring(0,i);if(s.lt(h)=="
+"'e')s.linkType='e';else h='';s[n]=t;return e?o:h;");
s.p_gh=new Function("",""
+"var s=this;if(!s.eo&&!s.lnk)return'';var o=s.eo?s.eo:s.lnk,y=s.ot(o"
+"),n=s.oid(o),x=o.s_oidt;if(s.eo&&o==s.eo){while(o&&!n&&y!='BODY'){o"
+"=o.parentElement?o.parentElement:o.parentNode;if(!o)return'';y=s.ot"
+"(o);n=s.oid(o);x=o.s_oidt;}}return o?o:'';");

/*Replace*/
s.repl=new Function("x","o","n",""
+"var i=x.indexOf(o),l=n.length;while(x&&i>=0){x=x.substring(0,i)+n+x."
+"substring(i+o.length);i=x.indexOf(o,i+l)}return x");

/*
 * Plugin: getQueryParam 2.3
 */
s.getQueryParam=new Function("p","d","u",""
+"var s=this,v='',i,t;d=d?d:'';u=u?u:(s.pageURL?s.pageURL:s.wd.locati"
+"on);if(u=='f')u=s.gtfs().location;while(p){i=p.indexOf(',');i=i<0?p"
+".length:i;t=s.p_gpv(p.substring(0,i),u+'');if(t){t=t.indexOf('#')>-"
+"1?t.substring(0,t.indexOf('#')):t;}if(t)v+=v?d+t:t;p=p.substring(i="
+"=p.length?i:i+1)}return v");
s.p_gpv=new Function("k","u",""
+"var s=this,v='',i=u.indexOf('?'),q;if(k&&i>-1){q=u.substring(i+1);v"
+"=s.pt(q,'&','p_gvf',k)}return v");
s.p_gvf=new Function("t","k",""
+"if(t){var s=this,i=t.indexOf('='),p=i<0?t:t.substring(0,i),v=i<0?'T"
+"rue':t.substring(i+1);if(p.toLowerCase()==k.toLowerCase())return s."
+"epa(v)}return ''");

/*
 * Plugin: getValOnce 0.2 - get a value once per session or number of days
 */
s.getValOnce=new Function("v","c","e",""
+"var s=this,k=s.c_r(c),a=new Date;e=e?e:0;if(v){a.setTime(a.getTime("
+")+e*86400000);s.c_w(c,v,e?a:0);}return v==k?'':v");

/*
 * Plugin: getTimeParting 3.0 - Set timeparting values based on time zone - valid through 2014 
 */
s.getTimeParting=new Function("t","z",""
+"var s=this,d,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T;d=new Date();A"
+"=d.getFullYear();if(A=='2009'){B='08';C='01'}if(A=='2010'){B='14';C"
+"='07'}if(A=='2011'){B='13';C='06'}if(A=='2012'){B='11';C='04'}if(A="
+"='2013'){B='10';C='03'}if(A=='2014'){B='09';C='02'}if(!B||!C){B='08"
+"';C='01'}B='03/'+B+'/'+A;C='11/'+C+'/'+A;D=new Date('1/1/2000');if("
+"D.getDay()!=6||D.getMonth()!=0){return'Data Not Available'}else{z=p"
+"arseFloat(z);E=new Date(B);F=new Date(C);G=F;H=new Date();if(H>E&&H"
+"<G){z=z+1}else{z=z};I=H.getTime()+(H.getTimezoneOffset()*60000);J=n"
+"ew Date(I+(3600000*z));K=['Sunday','Monday','Tuesday','Wednesday','"
+"Thursday','Friday','Saturday'];L=J.getHours();M=J.getMinutes();N=J."
+"getDay();O=K[N];P='AM';Q='Weekday';R='00';if(M>30){R='30'}if(L>=12)"
+"{P='PM';L=L-12};if(L==0){L=12};if(N==6||N==0){Q='Weekend'}T=L+':'+R"
+"+P;if(t=='h'){return T}if(t=='d'){return O}if(t=='w'){return Q}}");


/*
 * Utility Function: split v1.5 (JS 1.0 compatible)
 */
s.split=new Function("l","d",""
+"var i,x=0,a=new Array;while(l){i=l.indexOf(d);i=i>-1?i:l.length;a[x"
+"++]=l.substring(0,i);l=l.substring(i+d.length);}return a");

/*
 * Plugin Utility: apl v1.1
 */
s.apl=new Function("L","v","d","u",""
+"var s=this,m=0;if(!L)L='';if(u){var i,n,a=s.split(L,d);for(i=0;i<a."
+"length;i++){n=a[i];m=m||(u==1?(n==v):(n.toLowerCase()==v.toLowerCas"
+"e()));}}if(!m)L=L?L+d+v:v;return L");

/*
 * Plugin getPercentPageViewed v1.4 - determine percent of page viewed
 */
s.handlePPVevents=new Function (""
+"var s=s_c_il["+s._in+"];"
+"if(!s.getPPVid)return;var dh=Math.max(Math.max(s.d.body.scrollHeigh"
+"t,s.d.documentElement.scrollHeight),Math.max(s.d.body.offsetHeight,"
+"s.d.documentElement.offsetHeight),Math.max(s.d.body.clientHeight,s."
+"d.documentElement.clientHeight));var vph=s.wd.innerHeight||(s.d.doc"
+"umentElement.clientHeight||s.d.body.clientHeight),st=s.wd.pageYOffs"
+"et||(s.wd.document.documentElement.scrollTop||s.wd.document.body.sc"
+"rollTop),vh=st+vph,pv=Math.min(Math.round(vh/dh*100),100),c=s.c_r('"
+"s_ppv'),a=(c.indexOf(',')>-1)?c.split(',',4):[],id=(a.length>0)?(a["
+"0]):escape(s.getPPVid),cv=(a.length>1)?parseInt(a[1]):(0),p0=(a.len"
+"gth>2)?parseInt(a[2]):(pv),cy=(a.length>3)?parseInt(a[3]):(0),cn=(p"
+"v>0)?(id+','+((pv>cv)?pv:cv)+','+p0+','+((vh>cy)?vh:cy)):('');s.c_w"
+"('s_ppv',cn);");

/*
* Plugin: setupLinkTrack 2.0 - return links for HBX-based link
*         tracking in SiteCatalyst (requires s.split and s.apl)
*/
s.setupLinkTrack = new Function("vl", "c", ""
+ "var s=this;var l=s.d.links,cv,cva,vla,h,i,l,t,b,o,y,n,oc,d='';cv=s."
+ "c_r(c);if(vl&&cv!=''){cva=s.split(cv,'^^');vla=s.split(vl,',');for("
+ "x in vla)s._hbxm(vla[x])?s[vla[x]]=cva[x]:'';}s.c_w(c,'',0);if(!s.e"
+ "o&&!s.lnk)return '';o=s.eo?s.eo:s.lnk;y=s.ot(o);n=s.oid(o);if(s.eo&"
+ "&o==s.eo){while(o&&!n&&y!='BODY'){o=o.parentElement?o.parentElement"
+ ":o.parentNode;if(!o)return '';y=s.ot(o);n=s.oid(o);}for(i=0;i<4;i++"
+ ")var ltp=setTimeout(function(){},10);if(o.tagName)if(o.tagName.toLowerCase()!='a')if(o.tagName.toLowerC"
+ "ase()!='area')o=o.parentElement;}b=s._LN(o);o.lid=b[0];o.lpos=b[1];"
+ "if(s.hbx_lt&&s.hbx_lt!='manual'){if((o.tagName&&s._TL(o.tagName)=='"
+ "area')){if(!s._IL(o.lid)){if(o.parentNode){if(o.parentNode.dataset.vszone)o.l"
+ "id=o.parentNode.dataset.vszone;else o.lid=o.parentNode.id}}if(!s._IL(o.lpos))"
+ "o.lpos=o.coords}else{if(s._IL(o.lid)<1)o.lid=s._LS(o.lid=o.text?o.t"
+ "ext:o.innerText?o.innerText:'');if(!s._IL(o.lid)||s._II(s._TL(o.lid"
+ "),'<img')>-1){h=''+o.innerHTML;bu=s._TL(h);i=s._II(bu,'<img');if(bu"
+ "&&i>-1){eval(\"__f=/ src\s*=\s*[\'\\\"]?([^\'\\\" ]+)[\'\\\"]?/i\")"
+ ";__f.exec(h);if(RegExp.$1)h=RegExp.$1}o.lid=h}}}h=o.href?o.href:'';"
+ "i=h.indexOf('?');h=s.linkLeaveQueryString||i<0?h:h.substring(0,i);l"
+ "=s.linkName?s.linkName:s._hbxln(h);t=s.linkType?s.linkType.toLowerC"
+ "ase():s.lt(h);oc=o.onclick?''+o.onclick:'';cv=(o.lid=o.lid?o.lid:'n"

+ "o &lid')+'^^'+o.lpos;if(t&&(h||l)){cva=s.split(cv,'^^');vla=s.split"
+ "(vl,',');for(x in vla)s._hbxm(vla[x])?s[vla[x]]=cva[x]:'';}else if("
+ "!t&&oc.indexOf('.tl(')<0){s.c_w(c,cv,0);}else return ''");
s._IL = new Function("a", "var s=this;return a!='undefined'?a.length:0");
s._II = new Function("a", "b", "c", "var s=this;a=a.toLowerCase();return a"
+ ".indexOf(b.toLowerCase(),c?c:0)");
s._IS = new Function("a", "b", "c", "var s=this;a=a.toLowerCase();return b"
+ ">s._IL(a)?'':a.substring(b,c!=null?c:s._IL(a))");
s._LN = new Function("a", "b", "c", "d", ""
+ "var s=this;try{c=a.dataset.vszone}catch(err){c=''};d=''"
+ ";r"
+ "eturn[c,d]");
s._LVP = new Function("a", "b", "c", "d", "e", ""
+ "var s=this;c=s._II(a,'&'+b+'=');c=c<0?s._II(a,'?'+b+'='):c;if(c>-1)"
+ "{d=s._II(a,'&',c+s._IL(b)+2);e=s._IS(a,c+s._IL(b)+2,d>-1?d:s._IL(a)"
+ ");return e}return ''");
s._LS = new Function("a", ""
+ "var s=this,b,c=100,d,e,f,g;b=(s._IL(a)>c)?escape(s._IS(a,0,c)):esca"
+ "pe(a);b=s._LSP(b,'%0A','%20');b=s._LSP(b,'%0D','%20');b=s._LSP(b,'%"
+ "09','%20');c=s._IP(b,'%20');d=s._NA();e=0;for(f=0;f<s._IL(c);f++){g"
+ "=s._RP(c[f],'%20','');if(s._IL(g)>0){d[e++]=g}}b=d.join('%20');retu"
+ "rn unescape(b)");
s._LSP = new Function("a", "b", "c", "d", "var s=this;d=s._IP(a,b);return d"
+ ".join(c)");
s._IP = new Function("a", "b", "var s=this;return a.split(b)");
s._RP = new Function("a", "b", "c", "d", ""
+ "var s=this;d=s._II(a,b);if(d>-1){a=s._RP(s._IS(a,0,d)+','+s._IS(a,d"
+ "+s._IL(b),s._IL(a)),b,c)}return a");
s._TL = new Function("a", "var s=this;return a.toLowerCase()");
s._NA = new Function("a", "var s=this;return new Array(a?a:0)");
s._hbxm = new Function("m", "var s=this;return (''+m).indexOf('{')<0");
s._hbxln = new Function("h", "var s=this,n=s.linkNames;if(n)return s.pt("
+ "n,',','lnf',h);return ''");

s.getPercentPageViewed=new Function("pgid",""
+"var s=this,pgid=(arguments.length>0)?(arguments[0]):('-'),ist=(!s.ge"
+"tPPVid)?(true):(false);if(typeof(s.linkType)!='undefined'&&s.linkTy"
+"pe!='e')return'';var v=s.c_r('s_ppv'),a=(v.indexOf(',')>-1)?v.split"
+"(',',4):[];if(a.length<4){for(var i=3;i>0;i--)a[i]=(i<a.length)?(a["
+"i-1]):('');a[0]='';}a[0]=unescape(a[0]);s.getPPVpid=pgid;s.c_w('s_p"
+"pv',escape(pgid));if(ist){s.getPPVid=(pgid)?(pgid):(s.pageName?s.pa"
+"geName:document.location.href);s.c_w('s_ppv',escape(s.getPPVid));if"
+"(s.wd.addEventListener){s.wd.addEventListener('load',s.handlePPVeve"
+"nts,false);s.wd.addEventListener('scroll',s.handlePPVevents,false);"
+"s.wd.addEventListener('resize',s.handlePPVevents,false);}else if(s."
+"wd.attachEvent){s.wd.attachEvent('onload',s.handlePPVevents);s.wd.a"
+"ttachEvent('onscroll',s.handlePPVevents);s.wd.attachEvent('onresize"
+"',s.handlePPVevents);}}return(pgid!='-')?(a):(a[1]);");

/*
 * Plugin: getPreviousValue v1.0 - return previous value of designated
 *   variable (requires split utility)
 */
s.getPreviousValue=new Function("v","c","el",""
+"var s=this,t=new Date,i,j,r='';t.setTime(t.getTime()+1800000);if(el"
+"){if(s.events){i=s.split(el,',');j=s.split(s.events,',');for(x in i"
+"){for(y in j){if(i[x]==j[y]){if(s.c_r(c)) r=s.c_r(c);v?s.c_w(c,v,t)"
+":s.c_w(c,'no value',t);return r}}}}}else{if(s.c_r(c)) r=s.c_r(c);v?"
+"s.c_w(c,v,t):s.c_w(c,'no value',t);return r}");

/*Configure Media Module Functions */
s.loadModule("Media")
s.Media.autoTrack= false;
s.Media.trackVars="events,prop9,prop12,prop13,prop14,prop15,prop16,prop17,prop18,prop19,prop20,prop21,prop22,prop24,prop25,prop26,prop27,prop28,prop29,prop30,eVar10,eVar15,eVar16,eVar17,eVar18,eVar19,eVar20,eVar21,eVar22,eVar23,eVar24,eVar25,eVar26,eVar27,eVar28,eVar29,eVar30,eVar31,eVar32,eVar33,eVar35,eVar36,eVar37,eVar38,"; 
s.Media.trackEvents="event8,event20,event10,event21,event12,event13,event15";
s.Media.playerName="HTML5";
s.Media.trackUsingContextData = true;
s.Media.contextDataMapping = {
"a.media.name":"eVar22,prop25",
"a.contentType":"eVar9,prop10",
"a.media.view":"",
"a.media.segmentView":"event11",
"a.media.timePlayed":"event9",
"a.media.complete":"",
"a.media.ad.name":"eVar35", // ad name
    "a.media.ad.pod":"eVar36", // pod id
    "a.media.ad.podPosition":"eVar37", // ad position in pod
    "a.media.ad.CPMID":"eVar38", // cpm id
    "a.media.ad.view":"", // ad start
    "a.media.ad.complete":"", // ad complete
    "a.media.ad.timePlayed":"event14", // ad time played
    "a.media.ad.clicked":"event15" // ad click
};

s.Media.monitor = function (s,media){

    if(media.event=="OPEN") {
        if(s.prop9=="videoad"){s.events = "event12,event20,event21=" + s.sampratio ;}
        else{s.events = "event8,event20,event21=" + s.sampratio ; }
        s.Media.track(media.name);
    }

    if(media.event=="CLICKED") {
        s.events = "event15,event20,event21=" + s.sampratio ;
        s.Media.track(media.name);
    }

    if(media.event=="COMPLETE") {
        if(s.prop9=="videoad"){s.events = "event13,event20,event21=" + s.sampratio ;}
        else{s.events = "event10,event20,event21=" + s.sampratio ; }
        s.Media.track(media.name);
    }
        if(media.event=="COMPLETE") {
        if(s.prop9=="videoad"){s.events = "event13,event20,event21=" + s.sampratio ;}
        else{s.events = "event10,event20,event21=" + s.sampratio ; }
        s.Media.track(media.name);
    }


}

/* WARNING: Changing any of the below variables will cause drastic
changes to how your visitor data is collected.  Changes should only be
made when instructed to do so by your account manager.*/
s.visitorNamespace="dailymotion"
s.trackingServer="metrics.dailymotion.com"

/************* DO NOT ALTER ANYTHING BELOW THIS LINE ! **************/
var s_code='',s_objectID;function s_gi(un,pg,ss){var c=".substring(~=fun`u(~){`gs=@8~`S#w~.indexOf(~;#w~@f ~.length~new Fun`u('~`gs#x#K[$8@mn+'],~.toLowerCase()~){#w~#EObject~orcedLink#cing~};s.~ight"
+"Profile~.toUpperCase~visitor~','~^j!Object||!Object.proto^k||!Object.proto^k[~s.wd~=='~);s.~')q='~ookieDomainPeriods~^Pi=~#EArray~.location~else ~MigrationServer~^MingServer~^fightData~link~dynamic"
+"Account~}#w~s.m_~=''~}c$p(e){~s.apv>=~^LListener~contextData~lightStoreForSeconds~var ~:'')~;$i^A('#w@h^W#K)@h^W#K[$8@mn+'].~;i++){~Element~#EDate~s_objectID~referrer~s.ape(~^BTime()~s.maxDelay~s.b"
+"c~Math.floor(Math.random()*~onclick~ction~s.apv=parseFloat(~lightIncrementBy~.protocol~){s.~pageURL~while(~#M(~oc.charAt(oc~Name~&&s.~){#k~cookieLifetime~javaEnabled~s.adms~isibilityState~Timeout~."
+"get~ternalFilters~javascript~,arguments))@f~@Ns.b#eBehavior(\"# default# ~.target~s.pt(~Type~s.dl~=s.ppu=#N=#Nv1=#Nv2=#Nv3~Event~track~events~String~for(~}else{~s.un~'){q='~$ss.vl_~Attribute('data-"
+"s-object-id')~tfs~.s_~browser~colorDepth~cookiesEnabled~.host~.lastIndexOf('~s.sq~parseInt(~.href~retrieveL~t=s.ot(o)~#cVars~nload~&&(~type~screen~escape(~')sk='~ersion~lugins~.create~s.useF~harCod"
+"e~');~')>=~dynamicVariablePrefix~+=',prop'+n+',eVar'+n~tcf~document~)#w~s.fl(~='0123456789ABCDEF',~s.oun~s.c_r(k)~Sampling~Year();e.$iYear(y+~o.data$i.sObjectId~codeURIComponent(x)~this~name~resolu"
+"tion~loadModule~s.va_g~s.eh~s.isie~s.vl_t~qs+=s.s2q(~Secure~Height~!='~charSet~isopera~ismac~;try{~s.mpq~s.lnk~.parent~='+~true~timestamp~variableProvider~'s_~s.eo~(\"click\",~+(y<1900?~BufferedReq"
+"uests~Propagation();e.~!v)v=s.d.webkitV~)?'Y':'N'~u=m[t+1](~isibilitychange~return~apply~window~campaign~homepage~[un]~,#t)~._i~s.em=~s.epa(~s.c_w(~s.ssl~s.nrs~s.vl_l~W#Lh~.inner~qs+='&~[y+'_c']~)+"
+"' '+tm~s.gg('objectID~Key,e.~(\"'+~(''+~&&!~+':'+~fid~o.textContent~un){s_gi(un,1~ExternalLinks~'+s~&&o~onerror~http~channel~currencyCode~.src~ in ~(vo)~.toPrecision~s.rl~=\"m_\"+n~;s.gl(s.vl_g~',s"
+"[k],fv,k,~deleteL~MigrationKey~)}}}~'||~'=')~f',~){t=~):''~'+n;~r=s[f](~u=m[t](~n=1;n<=~false~Opera~s.fsg~s.ns6~n.userAgent~conne~space~agContainer~k,v,vf,vfp,~InlineStats~set~c+='s.'+k+'~nfl~ocq~t"
+"ransa~s.num(~client~atch~m._d~s.vl_m~=s.sp(~n=s.oid(o)~,'sqs',q);~LeaveQuery~&&t~+1);~+'.'~[i];~\",''),~255~ocb~('click',~=s.oh(o);~Default()~vo['!'+k]~)){~&&l~;i++)s.t(~:'';h=h?h~;'+(n?'o.~logDebu"
+"g~lif~=new ~AUTO'~=un~SESSION'~mn]~1900:~c_il~idt~s.rep~s.pe~s.c_d~d.cookie~s.rc~oc=o~s.pl~t.lmq~t.mmq~=(apn~b.remove~fv)fv~vo._t~vo[k]~b.attach~2o7.net'~Track~Rest~.add~=s.n.app~+';'~n++)~_'+~)+'/"
+"~j='1.~n]=m;~[i]=~[i])~||t==\\'_~://')j+=~]){y=s[x~){v=s.n.~nfn~100~ocx~sampled~if(~=s_~s.apv>3~o.value~~s.v^o='H.25.3';s.an#xan;s.#C`1m`2,^x=`8`ge@Nconsole.log$0^1#M(^1m,\"\\\\\",\"\\\\\\\\\"),\""
+"\\n\",\"\\\\n\"),\"\\\"\",\"\\\\\\\"\")+'\");`b}^t^x()`Ecls`1x,c){`gi,y`a`5!c)c=@8.an;`P0;i<x`7`jn=x`0i,i+1)`5c`4n)>=0)y+=n}`6y`Efl`1x,l){`6x?$1x)`00,l):x`Eco`1o){`6o`Enum`1x){x`a+x;^P`gp=0;p<x`7;p"
+"++^z('0123456789')`4x`0p,p+1))<0)`60;`61`Erep#xrep;s.sp#xsp;s.jn#xjn;s.ape`1x`2,h@1f=\"+~!*()'\",i,c=s.@K,n,l,e,y`a;c=c?c`G($T`5x){x`a+x`5@n=3){x=en@7;`P0;i<f`7;i++) {n=f`0i,i+1)`5x`4n)>=0)x=^1x,n,"
+"\"%\"+n.c^sAt(0).to^O(16)`G())}}`3c`L#F^j'').c^sAt){`P0;i<x`7`jc=x`0i,i$xn=x.c^sAt(i)`5n>127){l=0;e`a;^0n||l<4){e=h`0n%16,n%16+1)+e;n=(n-n%16)/16;l++}y+='%u'+e}`3c`L+')y+='%2B';`Sy+=^mc)}x=y}`Sx=^1"
+"^m''+x),'+`I%2B')`5c&&c@J#F^4em==1&&x`4'%u')<0&&x`4'%U')<0){i=x`4'%^t^0i>=0){i++`5h`08)`4x`0i,i+1)`G())>=0)`6x`00,i)+'u00'+x`0i);i=x`4'%',i$O`6x`Eepa`1x`2,y,^x`5x){x=^1''+x,'+`I ')`5@n=3){^x=`8x`I`"
+"gy,e@Ny=de@7`by#G^mx)}`6y^t`6^x(x)}`S`6un^mx)}`6y`Ept`1x,d,f,a`2,t=x,z=0,y,r;^0t){y=t`4d);y=y<0?t`7:y;t=t`00,y);$Vt,a)`5r)`6r;z+=y+d`7;t=x`0z,x`7);t=z<x`7?t:''}`6''`Eisf`1t,a){`gc=a`4':')`5c>=0)a=a"
+"`00,c);c=a`4$Q`5c>=0)a=a`00,c)`5t`00,2)`Ls_')t=t`02);`6(t!`a$w==a)`Efsf`1t,a`2`5^Ha,`I,'is$Rt))$a+=($a!`a?`I`h+t;`60`Efs`1x,f`2;$a`a;^Hx,`I,'fs$Rf);`6$a`Empc`1m,a`2,c,l,n,v;v=s.d.v^9`5@b^9`5v&&v`Lp"
+"rerender'`B!@O`ympq`Q;l$s'webkitv@e,v@e`I,^t^Pn=0;n<l`7;n++`yd#e`d(l[n],`8`9c,v;v=s.d.v^9`5@b^9`5@O&&v==\"visible\"){^0@O`7>0){c=@O.shift();s[c.m].@g(s,c.a)}@O=0}'),$Y)}}c`C;c.m=m;c.a=a;@O.push(c);"
+"`61}`60`Esi`1`2,i,k,v,c#xgi+'`gs#xgi$0@2+'\"`Msa$0^R+'\");';`P0;i<@C`7`jk=@C$zv=s[k]`5v!#Gdefined`B^kof(v)@Jnumber')$j=\"$8_fe(v)+'\";';`S$j@Rv#g}}c+=\"@P=@W=s.`W^3=s.`W^I=`K.`m^K`a;\";`6c`Ec_d`a;s"
+".c_gdf`1t,a`2`5!$nt))`61;`60`Ec_gd`1`2,d=`K`R^a@9,n=s.fpC`O,p`5!n)n=s.c`O`5d$2#O){n=n?^dn):2;n=n>2?n:2;p=d^b.')`5p>=0){^0p>=0&&n>1){p=d^b.',p-1);n--}#O=p>0^4pt(d,'.`Ic_gd$R0)?d`0p):d}}`6#O`Ec_r`1k`"
+"2;k=`ok);`gc=' $8.#P,i=c`4' '+k+$Q,e=i<0?i:c`4';',i),v=i<0?'':@oc`0i+2+k`7,e<0?c`7:e));`6v@J[[B]]'?v:''`Ec_w`1k,v,e`2,d=s.c_gd(),l=s.^6,t;v`a+v;l=l?$1l)`G($T`5e#8@J#H#8@JNONE'$S(v!`a?^dl?l:0):-60)`"
+"5t){e`l;e.$iTime(e`p+(t*#t0))}`Yk#8@JNONE'`y#P=k+'@R`ov!`a?v:'[[B]]')+'; path=/;'+(e#8@J#H?' expires@Re.toGMT^O()#g`h+(d?' domain@Rd#g`h;`6@3==v}`60`Eeh`1o,e,r,f`2,b=@V'+e+'_$8@mn,n=-1,l,i,x`5!@Dl)"
+"@Dl`Q;l=@Dl;`P0;i<l`7&&n<0;i++`Bl[i].o==o#8[i].e==e)n=i`Yn<0){n=i;l[n]`C}x=l[n];x.o=o;x.e=e;f=r?x.b:f`5r||f){x.b=r?0:o[e];x.o[e]=f`Yx.b){x.o[b]=x.b;`6b}`60`Ecet`1f,a,t,o,b`2,r,^x`5`c5^j!s.@L||`c7#7"
+"^x=`8s`If`Ia`It`I`ge,r@N$Va)`br=s[t](e)}`6r^tr=^x(s,f,a,t)^Q#ws.@M^4u`4'MSIE 4^u0)r=s[b](a);else{@D(`K,'$A',0,o);$Va`Meh(`K,'$A',1)}}`6r`Eg^Vet`1e`2;`6s.^V`Eg^Voe=`8e`I`9c;@D(@h,\"$A\",1`Me^V=1;c=s"
+".t()`5c)s.d.write(c`Me^V=0;`6@S'`Mg^Vfb`1a){`6@h`Eg^Vf`1w`2,p=w@Q,l=w`R;s.^V=w`5p&&p`R!=l&&p`R^a==l^a`y^V=p;`6s.g^Vf(s.^V)}`6s.^V`Eg^V`1`2`5!s.^V`y^V=`K`5!s.e^V)s.^V=s.cet('g^V$Rs.^V,'g^Vet',s.g^Vo"
+"e,'g^Vfb')}`6s.^V`Emrq`1u`2,l=$I[u],n,r;$I[u]=0`5l)^Pn=0;n<l`7;#h{r=l[n];s.mr(0,0,r.r,r.t,r.u)}`Eflush@Z`1){`Emr`1sess,q,rs,ta,u`2,dc=s.dc,t1=s.`U,t2=s.`U@H,tb=s.`UBase,p='.sc',ns=s.`H^3$e,un=s.cls"
+"(u?u:(ns?ns:s.fun)),r`C,l,imn=@Vi#i(un),im,b,e`5!rs`Bt1`Bt2^4ssl)t1=t2^Q#w!tb)tb='#b`5dc)dc=$1dc)`A;`Sdc='d1'`5tb`L#b`Bdc`Ld1')dc='112';`3dc`Ld2')dc='122';p`a}t1#G$y+dc$y+p+tb}rs='$B'+(@q?'s'`h+':/"
+"/'+t1+'/b/ss/'+^R+'/'+(s.mobile?'5.1':'1'#j$8.v^o+(s.tcn?'T'`h+'/$8ess+'?AQB=1&ndh=1'+(q?q`h+'&AQE=1'`5@E$2s.@M)rs=@0rs,2047)`Ys.d.images&&`c3^j!s.@L||`c7)^j$b<0||`c6.1)`B!#Q)#Q`C`5!#Q@k`yrc@k=1`5!"
+"$I)$I`C;$I@k`Q`imrq$0un+'\")',750)^Ql=$I@k`5l){r.t=ta;r.u#G;r.r=rs;l[l`7]=r;`6''}imn+='_$8.rc@k;#Q@k++`Ys.debug#cing){`gd='AppMeasurement Debug: '+rs,dl$srs,'&'),dln;^Pdln=0;dln<dl`7;dl#hd+=\"\\n\\"
+"t\"+@odl[dln]`M#C(d)}im=`K[i#I`5!im)im=`K[i#I#EImage;im^Wl=0;im.o^i=`8e`I@8^Wl=1;`gwd=@h,s`5wd^W#K){s=wd^W#K[$8@mn+'];`rr(`Mmrq$0un+'\"`Mnrs--`5!@r)`Zm(\"rr\")}')`5!@r`ynrs=1;`Zm('rs')}`S@r++;im$E="
+"rs`5^r`D||`rf`B!s.f`D^A)s.f`D^A=250`ibcr()',s.f`D^A);}`3(@P||@W)^j!ta||ta`L_self$Pta`L_top$P(`K.@9$wa==`K.@9)#7b=e`l;^0!im^Wl&&e`p-b`p<500)e`l}`6''}`6'<im'+'g sr'+'c=\"'+rs+'\" w#Lh=1 height=1 bord"
+"er=0 alt=\"\">'`Egg`1v`2`5!`K[@V'+v])`K[@V'+v]`a;`6`K[@V'+v]`Eglf`1t,a`Bt`00,2)`Ls_')t=t`02);`gs=@8,v=s.gg(t)`5v)s[t]=v`Egl`1v`2`5s.pg)^Hv,`I,'gl$R0)`Erf`1x`2,y,i,j,h,p,l=0,q,a,b`a,c`a,t`5x&&x`7>#1"
+"){y`a+x;i=y`4'?')`5i>0){q=y`0i$xy=y`00,i);h=y`A;j=0`5h`00,7)`L$B#p7;`3h`00,8)`L$Bs#p8;i=h`4\"/\",j)`5i>0){h=h`0j,i);p=y`0i);y=y`00,i)`5h`4'google^u0)l=',q,ie,start,search_key,word,kw,cd,';`3h`4'yah"
+"oo.co^u0)l=',p,ei,'`5l&&q){a$sq,'&')`5a&&a`7>1){^Pj=0;j<a`7;j++$Sa[j];i=t`4$Q`5i>0#8`4`I+t`00,i)+`I)>=0)b+=(b?'&'`h+t;`Sc+=(c?'&'`h+t`Yb&&c)q=b+'&'+c;`Sc`a}i=253-(q`7-c`7)-y`7;x=y+(i>0?p`00,i)`h+'?"
+"'+q}}}}`6x`Es2q`1$gf`2,qs`a,sk,sv,sp,ss,nke,nk,nf,$k=0,#s,nfm`5k==\"`e\")k=\"c\"`5v){^Psk$Fv^z(!f||sk`00,f`7)==f)&&v[sk]^j!vf||vf`4`I+(vfp?vfp$y`h+sk+`I)>=0)`Jsk]#7nfm=0`5$k)^P#s=0;#s<$k`7;#s++^zsk"
+"`00,$k[#s]`7)==$k[#s])nfm=1`5!nfm`Bqs`L')@v'+k$y;sv=v[sk]`5f)sk=sk`0f`7)`5sk`7>0){nke=sk`4'.')`5nke>0){nk=sk`00,nke);nf=(f?f`h+nk$y`5!$k)$k`Q;$k[$k`7]=nf;@Gn$gnf)^Q#w^kof(sv)`Lboolean'`Bsv)sv='@S';"
+"`Ssv='$Y'`Ysv`Bvfp`L`V'&&f`4'.`e.')<0){sp=sk`00,4);ss=sk`04)`5sk`L$m`uID^nxact';`3sk`L$C^nch';`3sk`L@i^nv0';`3$nss)`Bsp`Lprop^nc$8s;`3sp`LeVar^nv$8s;`3sp`Llist^nl$8s;`3sp`Lhier'){sk='h$8s;sv=sv`00,"
+"#1$O@v'+`osk)+'@R`osv$O}`Yqs!`a)@v.'+k}`6qs`Ehav`1`2,qs`a,l,fv`a,fe`a,mn,i,e`5s.l`FID){l=s.va_m;fv=s.light^h`5#X=`I+fv+`I+$rr+`I^Ql=s.va_t`5#N||s.`W^I){fv=s.`W^h;fe=s.`W#c^Ls`5#N){mn=#N`00,1)`G()+#"
+"N`01)`5s[#I){fv=s[#I.^MVars;fe=s[#I.^M^Ls}}`Y#X=`I+fv+`I+@s+`I+@s2`5fe){fe=`I+fe+`I`5#X+=',^N,'}if (s.^N2)e=(e?`I`h+s.^N2}`P0;i<l`7`j`gk=l[i],v=s[k],b=k`00,4),x=k`04),n=^dx),q=k`5!v^zk`L^N'&&e){v=e"
+";e`a`Yv^j!fv||fv`4`I+k+`I)>=0)&&k@J`W^3'&&k@J`W^I'`Bk`L@T`Nts';`3k`L^v`ND';`3k`L`HID`Nvid';`3k`L`z^Sg'`5v`7>#1`y`z#d=v`0#1);v=v`00,#1);}}`3k`L`z#d`N-g';`3k`L`n^Sr';v=@0s.rf(v),#1)}`3k`Lvmk$Pk`L`H$N"
+"`Nvmt';`3k`L`H`T^Svmf'`5@q^4`H`T@H)v`a}`3k`L`H`T@H^Svmf'`5!@q^4`H`T)v`a}`3k`L@K^Sce'`5v`G()`L#F)v='ISO8859-1';`3@n=2||@n=3)v='UTF-8'}`3k`L`H^3$e`Nns';`3k`Lc`O`Ncdp';`3k`L^6`Ncl';`3k`L@U`Nvvp';`3k`L"
+"$D`Ncc';`3k`L$C`Nch';`3k`L$m`uID`Nxact';`3k`L@i`Nv0';`3k`L@A`Ns';`3k`L^Y`Nc';`3k`L^DV^o`Nj';`3k`L^7`Nv';`3k`L^Z`Nk';`3k`L^X@t`Nbw';`3k`L^X@I`Nbh';`3k`L$d`u^I`Nct';`3k`L@j`Nhp';`3k`Lp^p`Np';`3k`L^N'"
+"`Be)v+=(v?`I`h+e`5fe)v=s.fs(v,fe)}`3k`L^N2')v`a;`3k`L`e'){@G'c$L0);v`a}`3k`Ll`FID`Nmtp';`3k`L`f^Smtss'`5!s.l`FID)v`a}`3k`L`w^Smti'`5!s.l`FID)v`a}`3k`L^f`Fs`Nmtsr';`3k`L$M`Fs`Nmtsd';`3k`L`V'`Bs.^f`F"
+"s)@G'mts$L0);v`a}`3$nx)`Bb`Lprop`Nc$U`3b`LeVar`Nv$U`3b`Llist`Nl$U`3b`Lhier^Sh$Uv=@0v,#1)}`Yv)@v'+`oq)+'@R(k`00,3)@Jpev'?`ov):v)}}`6qs`Eltdf`1t,h$St?t`A#A`A:'';`gqi=h`4'?^th=qi>=0?h`00,qi):h`5t&&h`0"
+"h`7-(t`7+1))`L.'+t)`61;`60`Eltef`1t,h$St?t`A#A`A:''`5t&&h`4t)>=0)`61;`60`Elt`1h`2,lft=s.`WDow^iFile^Is,lef=s.`WEx^C,#D=s.`WIn^C;#D=#D?#D:`K`R^a@9;h=h`A`5s.^MDow^iLinks#8ft^4pt(lft,`I,'ltd$Rh))`6'd'"
+"`5s.^M$7&&h`00,1)@J# '^jlef||#D)^j!lef||^Hlef,`I,'lte$Rh))^j!#D||!^H#D,`I,'lte$Rh)))`6'e';`6''`Elc=`8e`I`9b=@D(@8,\"`t\"`Mlnk=@8;s.t(`Mlnk=0`5b)`6@8[b](e);`6@S'`Mbcr`1`2`5`rt&&`re)`rt.disp$p^L(`re)"
+"`5`rf`B^kof(`rf)`Lfun`u')`rf();`3`rt&&`rt^e)s.d`R=`rt^e}`rt=`re=`rf=0`Ebc=`8e`I#we&&e^Wfe)@f;`9f,^x,t,n,nrs`5s.d^4d.all^4d.all.cppXYctnr)@f`5!s.bbc)^r`D=0;`3!^r`D`y#W`d@X`r,@S`Mbbc=^r`D=0;@f}`Ss.#W"
+"`d@X`r,$Y`Meo=e$E`k?e$E`k:e^G;nrs=@r;s.t(`Meo=0`5@r>nrs^4useF`D&&e^G$Se^G.target`5e^G.disp$p^L^j!t#oself\\'#otop\\'||(`K.@9$w==`K.@9)#7e.stop@astopImmediate@aprevent#5;n=s.d^q^L(\"Mouse^Ls\");n.ini"
+"tMouse^L@Xe.bubbles,e.cancelable,e.view,e.detail,e.^lX,e.^lY,e.$oX,e.$oY,e.ctrl@zalt@zshift@zmeta@zbutton,e.relatedTarget);n^Wfe=1;`rt=e^G;`re=n;}}'`Moh`1o`2,l=`K`R,h=o^e?o^e:'',i,j,k,p;i=h`4':^tj="
+"h`4'?^tk=h`4'/')`5h^ji<0||(j>=0&&i>j)||(k>=0&&i>k)#7p=o`x$9`x`7>1?o`x:(l`x?l`x`h;i=l.path@9^b/^th=(p?p+'//'`h+(o^a?o^a:(l^a?l^a`h)+(h`00,1)@J/'?l.path@9`00,i<0?0:i#j'`h+h}`6h`Eot`1o){`gt=o.tag^3`5o"
+".tagUrn||(o.scope^3$9.scope^3`G()@JHTML'))`6'';t=t$w`G?t`G($T`5t`LSHAPE')t`a`5t`B(t`LINPUT$Pt`LBUTTON')$9.^k$9.^k`G)t=o.^k`G();`3!t$9^e)t='A';}`6t`Eoid`1o`2,^g,p,c,n`a,x=0`5t$2o^Woid){p=o`x;c=o.`t`"
+"5o^e^jt`LA$Pt`LAREA')^j!c||!p||p`A`4'^D')<0))n#4`3c){n=^1#M(^1#M$1c,\"\\r#0\"\\n#0\"\\t#0' `I^tx=2}`3t`LINPUT$Pt`LSUBMIT'`B#z)n=#z;`3o@uText)n=o@uText;`3$5)n=$5;x=3}`3o$E$w`LIMAGE')n=o$E`5n){o^Woid"
+"=@0n@l;o^Wo#L=x}}`6o^Woid`Erqf`1t,un`2,e=t`4$Q,u=e>=0?t`00,e$T,q=e>=0?@ot`0e+1)$T`5u&&q^j`I+u+`I)`4`I+un+`I)>=0`Bu!=^R^4un`4`I)>=0)q='&u@Ru+q+'&u=0';`6q}`6''`Erq`1un`B!un)un=thi^R;`gs=@8,c#G`4`I),v"
+"=s.c_r(@Vsq'),q`a`5c<0)`6^Hv,'&`Irq$Run);`6^Hun,`I,'rq',0)`Esqp`1t,a`2,e=t`4$Q,q=e<0?'':@ot`0e+1)`Msqq[q]`a`5e>=0)^Ht`00,e),`I$u`60`Esqs`1un,q`2;^cu@k=q;`60`Esq`1q`2,k=@Vsq',v=@3,x,c=0;^cq`C;^cu`C;"
+"^cq[q]`a;^Hv,'&`Isqp',0`Mpt(^R,`I$uv`a;^Px$F^cu^zx`Jx]))^cq[^cu[x]]+=(^cq[^cu[x]]?`I`h+x;^Px$F^cq^zx`Jx])^4sqq[x]^jx==q||c<2#7v+=(v?'&'`h+^cq[x]+'@R`ox);c++}`6@pk,v,0)`Ewdl=`8e`I`9r=@S,b=@D(`K,\"o^"
+"i\"),i,o,oc`5b)r=@8[b](e);`P0;i<s.d.`Ws`7`jo=s.d.`Ws$z#R.`t?\"\"+o.`t:\"\"`5(oc`4\"s_gs(\")<0||oc`4\"^Woc(\")>=0)$9c`4\".tl(\")<0)@D(o,\"`t\",0,s.lc);}`6r^t`Ks`1`2`5#y^j!@E||!s.@M||`c5)`Bs.b^4#a^L)"
+"s.#a^L('`t',`r);`3s.b^4b#e`d`Bs.n^4$c`4'WebKit^u0^4d^q^L`ybbc=1;^r`D=1;s.b#e`d#3`r,@S)}s.b#e`d#3`r,$Y)}`S@D(`K,'o^i',0,`Kl)}`Evs`1x`2,v=s.`H@4,g=s.`H@4Group,k=@Vvsn#i^R+(g?'#ig`h,n=@3,e`l,y=e^B@510"
+"@Y#J0))`5v){v*=#t`5!n`B!@pk,x,e))`60;n=x`Yn%#t00>v)`60}`61`Edyasmf`1t,m`Bt&&m&&m`4t)>=0)`61;`60`Edyasf`1t,m`2,i=t?t`4$Q:-1,n,x`5i>=0&&m){`gn=t`00,i),x=t`0i+1)`5^Hx,`I,'dyasm$Rm))`6n}`60`Euns`1`2,x="
+"s.`XSele`u,l=s.`XList,m=s.`XM$p,n,i;^R=^R`A`5x#8`B!m)m=`K`R^a`5!m.toLowerCase)m`a+m;l=l`A;m=m`A;n=^Hl,';`Idyas$Rm)`5n)^R=n}i=^R`4`I`Mfun=i<0?^R:^R`00,i)`Esa`1un`2`5^R^4mpc('sa'^E;^R#G`5!@2)@2#G;`3("
+"`I+@2+`I)`4`I+un+`I)<0)@2+=`I+un;^Rs()`Em_i`1n,a`2,m,f=n`00,1),r,l,i`5!`Zl)`Zl`C`5!`Znl)`Znl`Q;m=`Zl[n]`5!a&&m&&m._e$2m@m)`Za(n)`5!m){m`C,m._c=@Vm';m@mn=`K^Wc_in;m@ml=s@ml;m@ml[m@m#l`K^Wc_in++;m.s="
+"s;m._n=n;m._l`Q('_c`I_in`I_il`I_i`I_e`I_d`I_dl`Is`In`I_r`I_g`I_g1`I_t`I_t1`I_x`I_x1`I_rs`I_rr`I_l'`Mm_l[#l`Znl[`Znl`7]=n}`3m._r$2m._m){r=m._r;r._m=m;l=m._l;`P0;i<l`7;i++^zm[l[i]])r[l[i]]=m[l[i]];r@"
+"ml[r@mn]=r;m=`Zl[n]=r`Yf==f`G())s[#l`6m`Em_a=`8n`Ig`Ie`I#w!g)g$J;`9c=s[g+\"_c\"],m,x,f=0`5s.mpc(\"m_a\"^E`5!c)c=`K[\"s_\"+g+\"_c\"]`5c&&s_d)s[g]#EFun`u(\"s\",s_ft(s_d(c)));x=s[g]`5!x)x=`K[\\'s_\\'+"
+"g]`5!x)x=`K[g];m=`Zi(n,1)`5x^j!m@m||g!$J#7m@m=f=1`5(\"\"+x)`4\"fun`u\")>=0)x(s);`S`Zm(\"x\",n,x,e)}m=`Zi(n,1)`5$ql)$ql=$q=0;^Jt();`6f'`Mm_m`1t,n,d,e$S'#it;`gs=@8,i,x,m,f='#it,r=0,u`5`Zl&&`Znl)`P0;i"
+"<`Znl`7`jx=`Znl[i]`5!n||x==n){m=`Zi(x);u=m[t]`5u`B$1u)`4'fun`u^u0`Bd&&e)$Wd,e);`3d)$Wd);`S$W)}`Yu)r=1;u=m[t+1]`5u$2m[f]`B$1u)`4'fun`u^u0`Bd&&e)@dd,e);`3d)@dd);`S@d)}}m[f]=1`5u)r=1}}`6r`Em_ll`1`2,g="
+"`Zdl,i,o`5g)`P0;i<g`7`jo=g[i]`5o)s.@B(o.n,o.u,o.d,o.l,o.e,1);g#m0}`E@B`1n,u,d,l,e,ln`2,m=0,i,g,o=0,f1,f2,c=s.h?s.h:s.b,b,^x`5n){i=n`4':')`5i>=0){g=n`0i$xn=n`00,i)}`Sg$J;m=`Zi(n)`Y(l||(n$2`Za(n,g)))"
+"&&u^4d&&c^4d^q`k`Bd){$q=1;$ql=1`Yln`B@q)u=^1u,'$B:`I$Bs:^ti=@Vs:$8@mn$3n$3g;b='`9o=s.d^B`kById$0i+'\")`5s$9`B!o.l&&`K.'+g+'){o.l=1`5o.i)clear^A(o.i);o.i=0;`Za$0n+'\",\"'+g+'\"'+(e?',\"'+e+'\"'`h+')"
+"}';f2=b+'o.c++`5!`q)`q=250`5!o.l$9.c<(`q*2)/#t)o.i=$i^A(o.f2@l}';f1=`8e',b+'}^t^x=`8s`Ic`Ii`Iu`If1`If2`I`ge,o=0@No=s.d^q`k(\"script\")`5o){o.^k=\"text/^D\"#Bid=i;o.defer=@S;o.o^i=o.onreadystatechan"
+"ge=f1;o.f2=f2;o.l=0;'`h+'o$E=u;c.appendChild(o)#Bc=0;o.i=$i^A(f2@l'`h+'}`bo=0}`6o^to=^x(s,c,i,u,f1,f2)^Qo`C;o.n=n$3g;o.u=u;o.d=d;o.l=l;o.e=e;g=`Zdl`5!g)g=`Zdl`Q;i=0;^0i<g`7&&g#ni++;g#mo}}`3n){m=`Zi"
+"(n);m._e=1}`6m`Evoa`1vo,r`2,l=@C,i,k,v,x;`P0;i<l`7`jk=l$zv=#Z`5v||#6`B!r^jk==\"`e\"||k==\"`V\")&&s[k])^Px$Fs[k]^z!v[x])v[x]=s[k][x];s[k]=v}}`Evob`1vo`2,l=@C,i,k;`P0;i<l`7`jk=l$z#Z=s[k]`5!#Z)#6=1}`E"
+"dlt=`8`9d`l,i,vo,f=0`5^Jl)`P0;i<^Jl`7`jvo=^Jl[i]`5vo`B!`Zm(\"d\")||d`p-#Y>=`q`ydll#m0;s.t$G}`Sf=1}`Y^Ji)clear^A(^Ji`Mdli=0`5f`B!^Ji)^Ji=$i^A(^Jt,`q)}`S^Jl=0'`Mdl`1vo`2,d`l`5!vo)vo`C;s.vob$G;#Y=d`p`"
+"5!^Jl)^Jl`Q;^Jl[^Jl`7]=vo`5!`q)`q=250;^Jt()`Eg$4`1`2,d@1k=@V$4',$4=@3,h`a,l`a,i,j,m=8,n=4,e`l,y`5!$4||$4`4'-')<0){`P0;i<16`jj=`sm);h+=d`0j,j$xj=`sn);l+=d`0j,j$xm=n=16}$4=h+'-'+l;}y=e^B@52@Y#J0))`5!"
+"@pk,$4,e))$4=0;`6$4`E@gADMS`1`2,vb`C`5`K.ADMS$2s.`HID$2^8c`B!^8)^8=ADMS^B#5`5!^8q`y`HID=^8^BVisitorID(`8v`I`9l=^8q,i`5v==-1)v=0`5v)s.`HID=v;^8q=0`5l`yadmsc=1;`P0;i<l`7#9l[i]`Madmsc=0;}'))`5!s.`HID)"
+"^8q`Q`Y^8q`yvob(vb);vb['!`HID']=0;^8q.push(vb);`61^Q#ws.`HID==-1)s.`HID=0}}`60`E^M=s.t`1vo`2,trk=1,tm`l,sed=Math&&Math.random?`s#t00000000000):tm`p,sess='s'+Math.floor(tm`p/10800000)%10+sed,y=tm^BY"
+"ear(),vt=tm^BDate(#j'+tm^BMonth(#j'@Yy+#Jy@x^BHours()$3tm^BMinutes()$3tm^BSeconds(@x^BDay(@x^BTimezoneOff$i(),^x,^V=s.g^V(),ta=-1,q`a,qs`a,code`a,vb`C`5s.mpc('t'^E$K`Muns(`Mm_ll()`5!s.td){`gtl=^V`R"
+",a,o,i,x`a,c`a,v`a,p`a,bw`a,bh`a,#k0',k=@p@Vcc`I@S',0@c,hp`a,ct`a,pn=0,ps`5^O&&^O.proto^k^51'`5j.m$p^52'`5tm.$iUTCDate^53'`5@E^4@M&&`c5)#k4'`5pn$H^55';a`Q`5a.forEach^56';i=0;o`C;^x=`8o`I`ge,i=0@Ni#"
+"EIterator(o)`b}`6i^ti=^x(o)`5i&&i.next^57'`5a.reduce^58'`5j.trim^58.1'`5Date.parse^58.2'`5Object^q)#k8.5'}}}}}}}}`Y`c4)x=^l.w#Lh+'x'+^l.height`5s.isns||s.@L`B`c3#r^7(@c`5`c4){c=^l.pixelDepth;bw=`K@"
+"u@t;bh=`K@u@I}}#S=s.n.p^p}`3@E`B`c4#r^7(@c;c=^l.^Y`5`c5){bw=s.d.^y`k.off$i@t;bh=s.d.^y`k.off$i@I`5!s.@M^4b){^x=`8s`Itl`I`ge,hp=0^FhomePage\");hp=s.b.isHomePage(tl)?\"Y\":\"N\"`b}`6hp^thp=^x(s,tl);^"
+"x=`8s`I`ge,ct=0^F$oCaps\");ct=s.b.$d`u^I`b}`6ct^tct=^x(s$O`Sr`a`Y#S)^0pn<#S`7&&pn<30){ps=@0#S[pn].@9@l#g`5p`4ps)<0)p+=ps;pn++}s.@A=x;s.^Y=c;s.^DV^o=j;s.^7=v;s.^Z=k;s.^X@t=bw;s.^X@I=bh;s.$d`u^I=ct;s"
+".@j=hp;s.p^p=p;s.td=1`Yvo`yvob(vb`Mvoa$G}s.$4=s.g$4()`5s.@gADMS())`6''`5(vo&&#Y)||!`Zm('d')`Bs.useP^p)s.doP^p(s)`5!s.abort){`gl=`K`R,r=^V.^y.`n`5!s.`z)s.`z=l^e?l^e:l`5!s.`n$2s._1_`n`y`n=r;s._1_`n=1"
+"}`Zm('g')`5@P||@W){`go=@W?@W:@P,p=s.page^3,w=1,^g,$t,x=o^Wo#L,h,l,i,oc`5@W$9==@W){^0o$2n$w@JBODY'){o=o@Q`k?o@Q`k:o@QNode`5o){^g;$t;x=o^Wo#L}`Y!n||t`LBODY')o`a`5o){#R.`t?''+o.`t:''`5(oc`4@Vgs(^u0$9c"
+"`4'^Woc(')<0)||oc`4'.tl(^u0)o=0}`Yo`Bn)ta=o^G;h#4i=h`4'?^th=s.`W$v^O||i<0?h:h`00,i);l=s.`W^3;t=s.`W^I?s.`W^I`A:s.lt(h)`5t^jh||l)`ype='lnk#i(t`Ld$Pt`Le'?t:'o'`Mpev1=(h?`oh$T`Mpev2=(l?`ol)`h}`Strk=0`"
+"5s.^M$h`B!p){p=s.`z;w=0}^g;i=o.sourceIndex`5o.data$i&&@6){`K.`m=@6;}`3o^BAttribute$9^B^U){`K.`m=o^B^U;}`3^r`D){`K.`m`a;#R.`t?''+o.`t:''`5oc){`g#2=oc`4'`m'),oce,$l,#u`5#2>=0){#2+=10;^0#2<oc`7^j\"= "
+"\\t\\r\\n\")`4^2b))>=0)#2++`5#2<oc`7){oce=#2;$l=#u=0;^0oce<oc`7^j^2e)@J;$P$l)`B$l`B^2e)==$l$2#u)$l=0;`3^2e)==\"\\\\\")#u=!#u;`S#u=0;^Q$l=^2e)`5$l@J\"'$9cq!=\"'\")$l=0}oce++;}#Rc`0#2,oce)`5oc){o^Wso"
+"id=`8s`I`ge@N`K.`m@Roc+'`b}^to^Wsoid(s$O}`Y@y'#7n=@y^tx=1;i=1`Yp&&n$w)qs='&pid@R`o@0p,#1))+(w?'&p#L@Rw`h+'&oid@R`o@0n@l)+(x?'&o#L@Rx`h+'&ot@R`ot)+(i?'&oi@Ri`h}}`Strk=0`Ytrk||qs`y#v=s.vs(sed)`5trk`B"
+"s.#v)code=s.mr(sess,(vt?'&t@R`ovt)`h+s.hav()+q+(qs?qs:s.rq()),0,ta);qs`a;`Zm('t')`5s.p_r)s.p_r(`M`n=s.l`FID=s.^f`Fs=s.$M`Fs`a}^c(qs$O`S^J$G`5vo)s.voa(vb,1`Mabort=0;s.`z#d=@P=@W=s.`W^3=s.`W^I=`K.`m^"
+"K`a`5s.pg)`K^Wlnk=`K^Weo=`K^W`W^3=`K^W`W^I`a;`6code`E^MLink=s.tl`1o,t,n,vo,f`2;@P=o;s.`W^I=t;s.`W^3=n`5f){`rt=o;`rf=f}s.t$G`E^MLight`1p,ss,i,vo`2;s.l`FID=p;s.`f=ss;s.`w=i;s.t$G`E$iT$f`1n`2,l=`K^W#K"
+",i,t,x,y;s.tcn=n`5l)`P0;i<l`7`jt=l[i]`5t$w._c`Ls_l'$w.t$f^3==n`yvoa(t)`5#T)`P0;i<#T`7`jx=#T$zy='m#ix.n`5!s[y]$2s@w){s[y]=t[y];s@w=t@w}s.@B(x.n,x.u,x.d)`Yt.ml)^Px$Ft.ml^zs[x#q];x=t.ml[x];^Pi$Fx^z!Ob"
+"ject.proto^k[i]`B^kof(x#n@Jfun`u$P$1x#n`4@V#K')<0)y#mx[i]}`Y#U)`P0;i<#U`7`jx=#U[i]`5s[x.m#q.m]`5y[x.f]&&^kof(y[x.f])`Lfun`u'`Bx.a)y[x.f].@g(y,x.a);`Sy[x.f].@g(y)}}`Yt.tq)`P0;i<t.tq`7#9t.tq#n;t.s=s;"
+"@f}}`Ewd=@h;@q=(`K`R`x`A`4'$Bs^u0`Md=^y;s.b=s.d.body`5s.d^B`ksByTag^3`yh=s.d^B`ksByTag^3('HEAD')`5s.h)s.h=s.h[0]}s.n=navigator;s.u=s.$c;$b=s.u`4'Netscape6/^t`gapn#f^3,v#fV^o,ie=v`4'MSIE '),o=s.u`4'"
+"$Z '),i`5v`4'$Z^u0||o>0)apn='$Z';@E#V`LMicrosoft Internet Explorer'`Misns#V`LNetscape'`M@L#V`L$Z'`M@M=(s.u`4'Mac^u0)`5o>0)`vs.u`0o+6));`3ie>0`yapv=^di=v`0ie+5))`5#y)`vi)}`3$b>0)`vs.u`0$b+10));`S`vv"
+"`Mem=0`5s.em$H)@n3;`3^O.fromC^s){i=^m^O.fromC^s(256))`G(`Mem=(i`L%C4%80'?2:(i`L%U0#t'?1:0))`Y@2)s.sa(@2`Msa(un`Mvl_l='@T,^v,`HID,$4,vmk,`H$N,`H`T,`H`T@H,ppu,@K,`H^3$e,c`O,^6,page^3,`z,`n,`e,$D,l`FI"
+"D,`f,`w,^f`Fs,$M`Fs,`V';s.va_l^Tl,`I`Mvl_mr=$r='@T,@K,`H^3$e,c`O,^6,`e,l`FID,`f,`w';@F=@s+',@U,$C,server,page^I,$m`uID,purchaseID,@i,state,zip,^N,^N2,products,`W^3,`W^I';`gn;^P$X75;n++`yvl_t^w;$r^w"
+"}^P$X5;#h@F+=',hier$U^P$X3;#h@F+=',list$Us.va_m^Tm,`I`Mvl_l2=',tnt,pe,pev1,pev2,pev3,@A,^Y,^DV^o,^7,^Z,^X@t,^X@I,$d`u^I,@j,`z#d,p^p';@F+=@s2;s.va_t^Tt,`I`Mvl_g=@F+',`U,`U@H,`UBase,fpC`O,disable@Z,m"
+"obile,`H@4,`H@4Group,`XSele`u,`XList,`XM$p,^MDow^iLinks,^M$7,^M$h,`W$v^O,`WDow^iFile^Is,`WEx^C,`WIn^C,`W^h,`W#c^Ls,`W^3s,lnk,eo,light^h,_1_`n,un';@C^Tg,`I`Mpg=pg$K`M`e`C;s.`V`C`5!ss)`Ks()`5pg){`K^W"
+"co`1o){`6o`Ewd^Wgs`1$6,1).t()`Ewd^Wdc`1$6).t()}}",
w=window,l=w.s_c_il,n=navigator,u=n.userAgent,v=n.appVersion,e=v.indexOf('MSIE '),m=u.indexOf('Netscape6/'),a,i,j,x,s;if(un){un=un.toLowerCase();if(l)for(j=0;j<2;j++)for(i=0;i<l.length;i++){s=l[i];x=s._c;if((!x||x=='s_c'||(j>0&&x=='s_l'))&&(s.oun==un||(s.fs&&s.sa&&s.fs(s.oun,un)))){if(s.sa)s.sa(un);if(x=='s_c')return s}else s=0}}w.s_an='0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
w.s_sp=new Function("x","d","var a=new Array,i=0,j;if(x){if(x.split)a=x.split(d);else if(!d)for(i=0;i<x.length;i++)a[a.length]=x.substring(i,i+1);else while(i>=0){j=x.indexOf(d,i);a[a.length]=x.subst"
+"ring(i,j<0?x.length:j);i=j;if(i>=0)i+=d.length}}return a");
w.s_jn=new Function("a","d","var x='',i,j=a.length;if(a&&j>0){x=a[0];if(j>1){if(a.join)x=a.join(d);else for(i=1;i<j;i++)x+=d+a[i]}}return x");
w.s_rep=new Function("x","o","n","return s_jn(s_sp(x,o),n)");
w.s_d=new Function("x","var t='`^@$#',l=s_an,l2=new Object,x2,d,b=0,k,i=x.lastIndexOf('~~'),j,v,w;if(i>0){d=x.substring(0,i);x=x.substring(i+2);l=s_sp(l,'');for(i=0;i<62;i++)l2[l[i]]=i;t=s_sp(t,'');d"
+"=s_sp(d,'~');i=0;while(i<5){v=0;if(x.indexOf(t[i])>=0) {x2=s_sp(x,t[i]);for(j=1;j<x2.length;j++){k=x2[j].substring(0,1);w=t[i]+k;if(k!=' '){v=1;w=d[b+l2[k]]}x2[j]=w+x2[j].substring(1)}}if(v)x=s_jn("
+"x2,'');else{w=t[i]+' ';if(x.indexOf(w)>=0)x=s_rep(x,w,t[i]);i++;b+=62}}}return x");
w.s_fe=new Function("c","return s_rep(s_rep(s_rep(c,'\\\\','\\\\\\\\'),'\"','\\\\\"'),\"\\n\",\"\\\\n\")");
w.s_fa=new Function("f","var s=f.indexOf('(')+1,e=f.indexOf(')'),a='',c;while(s>=0&&s<e){c=f.substring(s,s+1);if(c==',')a+='\",\"';else if((\"\\n\\r\\t \").indexOf(c)<0)a+=c;s++}return a?'\"'+a+'\"':"
+"a");
w.s_ft=new Function("c","c+='';var s,e,o,a,d,q,f,h,x;s=c.indexOf('=function(');while(s>=0){s++;d=1;q='';x=0;f=c.substring(s);a=s_fa(f);e=o=c.indexOf('{',s);e++;while(d>0){h=c.substring(e,e+1);if(q){i"
+"f(h==q&&!x)q='';if(h=='\\\\')x=x?0:1;else x=0}else{if(h=='\"'||h==\"'\")q=h;if(h=='{')d++;if(h=='}')d--}if(d>0)e++}c=c.substring(0,s)+'new Function('+(a?a+',':'')+'\"'+s_fe(c.substring(o+1,e))+'\")"
+"'+c.substring(e+1);s=c.indexOf('=function(')}return c;");
c=s_d(c);if(e>0){a=parseInt(i=v.substring(e+5));if(a>3)a=parseFloat(i)}else if(m>0)a=parseFloat(u.substring(m+10));else a=parseFloat(v);if(a<5||v.indexOf('Opera')>=0||u.indexOf('Opera')>=0)c=s_ft(c);if(!s){s=new Object;if(!w.s_c_in){w.s_c_il=new Array;w.s_c_in=0}s._il=w.s_c_il;s._in=w.s_c_in;s._il[s._in]=s;w.s_c_in++;}s._c='s_c';(new Function("s","un","pg","ss",c))(s,un,pg,ss);return s}
function s_giqf(){var w=window,q=w.s_giq,i,t,s;if(q)for(i=0;i<q.length;i++){t=q[i];s=s_gi(t.oun);s.sa(t.un);s.setTagContainer(t.tagContainerName)}w.s_giq=0}s_giqf()

if("function"!=typeof DIL)DIL=function(a,c){var d=[],b,f,e,h,m,p,r,o;a!==Object(a)&&(a={});o=!0===a.enableErrorReporting;m=!!a.disableDestinationPublishingIframe;p=a.mappings;r=a.uuidCookie;o&&DIL.errorModule.activate();(b=c)&&d.push(b+"");f=a.partner;if(!f||"string"!=typeof f)return b="DIL partner is invalid or not specified in initConfig",DIL.errorModule.handleError({name:"error",message:b,filename:"dil.js"}),Error(b);b="DIL containerNSID is invalid or not specified in initConfig, setting to default of 0";
if((e=a.containerNSID)||"number"==typeof e)e=parseInt(e,10),!isNaN(e)&&0<=e&&(b="");b&&(e=0,d.push(b),b="");h=DIL.getDil(f,e);if(h instanceof DIL&&h.api.getPartner()==f&&h.api.getContainerNSID()==e)return h;if(this instanceof DIL)DIL.registerDil(this,f,e);else return new DIL(a,"DIL was not instantiated with the 'new' operator, returning a valid instance with partner = "+f+" and containerNSID = "+e);var t={IS_HTTPS:"https:"==document.location.protocol,POST_MESSAGE_ENABLED:!!window.postMessage,COOKIE_MAX_EXPIRATION_DATE:"Tue, 19 Jan 2038 03:14:07 UTC"},
s={},i={},l={firingQueue:[],fired:[],firing:!1,sent:[],errored:[],reservedKeys:{sids:!0,pdata:!0,logdata:!0,callback:!0,postCallbackFn:!0,useImageRequest:!0},callbackPrefix:"demdexRequestCallback",firstRequestHasFired:!1,useJSONP:!0,abortRequests:!1,num_of_jsonp_responses:0,num_of_jsonp_errors:0,num_of_img_responses:0,num_of_img_errors:0,adms:{TIME_TO_CATCH_ALL_REQUESTS_RELEASE:500,calledBack:!1,uuid:null,noADMS:!1,instanceType:null,releaseType:"no ADMS",admsProcessingStarted:!1,process:function(g){try{if(!this.admsProcessingStarted){var n=
this,b=a.visitorService,d,c,f,e;if("function"==typeof g&&"function"==typeof g.getDefault&&"function"==typeof g.getInstance&&(b===Object(b)&&(d=b.namespace)&&"string"==typeof d?(this.instanceType="namespace: "+d,c=g.getInstance(d)):(this.instanceType="default",c=g.getDefault()),c===Object(c)&&"function"==typeof c.getVisitorID)){this.admsProcessingStarted=!0;f=function(g){if("ADMS"!=n.releaseType)n.uuid=g,n.releaseType="ADMS",n.releaseRequests()};e=c.getVisitorID(f);if(-1==e){this.releaseType="failed ADMS";
this.releaseRequests();return}if("string"==typeof e&&e.length){f(e);return}setTimeout(function(){if("ADMS"!=n.releaseType)n.releaseType="timeout",n.releaseRequests()},this.TIME_TO_CATCH_ALL_REQUESTS_RELEASE);return}this.noADMS=!0;this.releaseRequests()}}catch(j){this.releaseRequests()}},releaseRequests:function(){this.calledBack=!0;l.registerRequest()}},registerRequest:function(g){var a=this.firingQueue;g===Object(g)&&a.push(g);if(!this.firing&&a.length)if(this.adms.calledBack){if(g=a.shift(),w.fireRequest(g),
!this.firstRequestHasFired&&"script"==g.tag)this.firstRequestHasFired=!0}else this.processADMS()},processADMS:function(){this.adms.process(window.ADMS)}};h=function(){var g="http://fast.";t.IS_HTTPS&&(g=!0===a.iframeAkamaiHTTPS?"https://fast.":"https://");return g+f+".demdex.net/dest4.html?d_nsid="+e+"#"+encodeURIComponent(document.location.href)};var u={THROTTLE_START:3E4,throttleTimerSet:!1,id:"destination_publishing_iframe_"+f+"_"+e,url:h(),iframe:null,iframeHasLoaded:!1,sendingMessages:!1,messages:[],
messagesPosted:[],messageSendingInterval:t.POST_MESSAGE_ENABLED?15:100,jsonProcessed:[],attachIframe:function(){var g=this,a=document.createElement("iframe");a.id=this.id;a.style.cssText="display: none; width: 0; height: 0;";a.src=this.url;k.addListener(a,"load",function(){g.iframeHasLoaded=!0;g.requestToProcess()});document.body.appendChild(a);this.iframe=a},requestToProcess:function(g){var a=this;g&&!q.isEmptyObject(g)&&this.process(g);if(this.iframeHasLoaded&&this.messages.length&&!this.sendingMessages){if(!this.throttleTimerSet)this.throttleTimerSet=
!0,setTimeout(function(){a.messageSendingInterval=t.POST_MESSAGE_ENABLED?15:150},this.THROTTLE_START);this.sendingMessages=!0;this.sendMessages()}},process:function(g){var a=this.messages,b=encodeURIComponent,c=o?b("---destpub-debug---"):b("---destpub---"),d,f,e,j,h;if((d=g.dests)&&d instanceof Array&&(f=d.length))for(e=0;e<f;e++)j=d[e],j=[b("dests"),b(j.id||""),b(j.y||""),b(j.c||"")],a.push(c+j.join("|"));if((d=g.ibs)&&d instanceof Array&&(f=d.length))for(e=0;e<f;e++)j=d[e],j=[b("ibs"),b(j.id||""),
b(j.tag||""),k.encodeAndBuildRequest(j.url||[],","),b(j.ttl||"")],a.push(c+j.join("|"));if((d=g.dpcalls)&&d instanceof Array&&(f=d.length))for(e=0;e<f;e++)j=d[e],h=j.callback||{},h=[h.obj||"",h.fn||"",h.key||"",h.tag||"",h.url||""],j=[b("dpm"),b(j.id||""),b(j.tag||""),k.encodeAndBuildRequest(j.url||[],","),b(j.ttl||""),k.encodeAndBuildRequest(h,",")],a.push(c+j.join("|"));this.jsonProcessed.push(g)},sendMessages:function(){var g=this,a;this.messages.length?(a=this.messages.shift(),DIL.xd.postMessage(a,
this.url,this.iframe.contentWindow),this.messagesPosted.push(a),setTimeout(function(){g.sendMessages()},this.messageSendingInterval)):this.sendingMessages=!1}},y={traits:function(g){if(q.isValidPdata(g)){if(!(i.sids instanceof Array))i.sids=[];k.extendArray(i.sids,g)}return this},pixels:function(g){if(q.isValidPdata(g)){if(!(i.pdata instanceof Array))i.pdata=[];k.extendArray(i.pdata,g)}return this},logs:function(g){if(q.isValidLogdata(g)){if(i.logdata!==Object(i.logdata))i.logdata={};k.extendObject(i.logdata,
g)}return this},customQueryParams:function(g){q.isEmptyObject(g)||k.extendObject(i,g,l.reservedKeys);return this},signals:function(g,a){var b,d=g;if(!q.isEmptyObject(d)){if(a&&"string"==typeof a)for(b in d={},g)g.hasOwnProperty(b)&&(d[a+b]=g[b]);k.extendObject(i,d,l.reservedKeys)}return this},result:function(g){if("function"==typeof g)i.callback=g;return this},afterResult:function(g){if("function"==typeof g)i.postCallbackFn=g;return this},useImageRequest:function(){i.useImageRequest=!0;return this},
clearData:function(){i={};return this},submit:function(){w.submitRequest(i);i={};return this},getPartner:function(){return f},getContainerNSID:function(){return e},getEventLog:function(){return d},getState:function(){var g={},a={};k.extendObject(g,l,{callbackPrefix:!0,useJSONP:!0,registerRequest:!0});k.extendObject(a,u,{attachIframe:!0,requestToProcess:!0,process:!0,sendMessages:!0});return{pendingRequest:i,otherRequestInfo:g,destinationPublishingInfo:a}},idSync:function(g){if(g!==Object(g)||"string"!=
typeof g.dpid||!g.dpid.length)return"Error: config or config.dpid is empty";if("string"!=typeof g.url||!g.url.length)return"Error: config.url is empty";var a=g.url,b=g.minutesToLive,d=encodeURIComponent,a=a.replace(/^https:/,"").replace(/^http:/,"");if("undefined"==typeof b)b=20160;else if(b=parseInt(b,10),isNaN(b)||0>=b)return"Error: config.minutesToLive needs to be a positive number";g=["ibs",d(g.dpid),"img",d(a),b];u.messages.push(g.join("|"));l.firstRequestHasFired&&u.requestToProcess();return"Successfully queued"},
aamIdSync:function(g){if(g!==Object(g)||"string"!=typeof g.dpuuid||!g.dpuuid.length)return"Error: config or config.dpuuid is empty";g.url="//dpm.demdex.net/ibs:dpid="+g.dpid+"&dpuuid="+g.dpuuid;return this.idSync(g)}},w={submitRequest:function(g){l.registerRequest(w.createQueuedRequest(g));return!0},createQueuedRequest:function(g){var a=l,b,c=g.callback,f="img";if(!q.isEmptyObject(p)){var e,h,j;for(e in p)if(p.hasOwnProperty(e)&&(h=p[e],!(null==h||""===h)&&e in g&&!(h in g)&&!(h in l.reservedKeys)))j=
g[e],null==j||""===j||(g[h]=j)}if(!q.isValidPdata(g.sids))d.push("requestProcs.createQueuedRequest(): sids is not valid, converting to an empty array"),g.sids=[];if(!q.isValidPdata(g.pdata))d.push("requestProcs.createQueuedRequest(): pdata is not valid, converting to an empty array"),g.pdata=[];if(!q.isValidLogdata(g.logdata))d.push("requestProcs.createQueuedRequest(): logdata is not valid, converting to an empty object"),g.logdata={};g.logdataArray=k.convertObjectToKeyValuePairs(g.logdata,"=",!0);
g.logdataArray.push("_ts="+(new Date).getTime());if("function"!=typeof c)c=this.defaultCallback;if(a.useJSONP=!g.useImageRequest||"boolean"!=typeof g.useImageRequest)f="script",b=a.callbackPrefix+(new Date).getTime();return{tag:f,src:w.makeRequestSrc(g,b),internalCallbackName:b,callbackFn:c,postCallbackFn:g.postCallbackFn,useImageRequest:g.useImageRequest,requestData:g}},defaultCallback:function(g){var a,b,d,c,e,f,h,o,i;if((a=g.stuff)&&a instanceof Array&&(b=a.length))for(d=0;d<b;d++)if((c=a[d])&&
c===Object(c)){e=c.cn;f=c.cv;h=c.ttl;if("undefined"==typeof h||""===h)h=Math.floor(k.getMaxCookieExpiresInMinutes()/60/24);o=c.dmn||"."+document.domain;i=c.type;if(e&&(f||"number"==typeof f))"var"!=i&&(h=parseInt(h,10))&&!isNaN(h)&&k.setCookie(e,f,1440*h,"/",o,!1),s[e]=f}a=g.uuid;if("string"==typeof a&&a.length&&!q.isEmptyObject(r)){b=r.path;if("string"!=typeof b||!b.length)b="/";d=parseInt(r.days,10);isNaN(d)&&(d=100);k.setCookie(r.name||"aam_did",a,1440*d,b,r.domain||"."+document.domain,!0===r.secure)}!m&&
!l.abortRequests&&u.requestToProcess(g)},makeRequestSrc:function(g,a){g.sids=q.removeEmptyArrayValues(g.sids||[]);g.pdata=q.removeEmptyArrayValues(g.pdata||[]);var b=l,d=k.encodeAndBuildRequest(g.sids,","),c=k.encodeAndBuildRequest(g.pdata,","),h=(g.logdataArray||[]).join("&");delete g.logdataArray;var o=t.IS_HTTPS?"https://":"http://",j;j=[];var i,m,p,r;for(i in g)if(!(i in b.reservedKeys)&&g.hasOwnProperty(i))if(m=g[i],i=encodeURIComponent(i),m instanceof Array)for(p=0,r=m.length;p<r;p++)j.push(i+
"="+encodeURIComponent(m[p]));else j.push(i+"="+encodeURIComponent(m));j=j.length?"&"+j.join("&"):"";return o+f+".demdex.net/event?d_nsid="+e+(d.length?"&d_sid="+d:"")+(c.length?"&d_px="+c:"")+(h.length?"&d_ld="+encodeURIComponent(h):"")+j+(b.useJSONP?"&d_rtbd=json&d_jsonv="+DIL.jsonVersion+"&d_dst=1&d_cts=1&d_cb="+(a||""):"")},fireRequest:function(g){"img"==g.tag?this.fireImage(g):"script"==g.tag&&this.fireScript(g)},fireImage:function(g){var a=l,c,f;if(!a.abortRequests)a.firing=!0,c=new Image(0,
0),a.sent.push(g),c.onload=function(){a.firing=!1;a.fired.push(g);a.num_of_img_responses++;a.registerRequest()},f=function(c){b="imgAbortOrErrorHandler received the event of type "+c.type;d.push(b);a.abortRequests=!0;a.firing=!1;a.errored.push(g);a.num_of_img_errors++;a.registerRequest()},c.addEventListener?(c.addEventListener("error",f,!1),c.addEventListener("abort",f,!1)):c.attachEvent&&(c.attachEvent("onerror",f),c.attachEvent("onabort",f)),c.src=g.src},fireScript:function(a){var c=this,e=l,h,
o,i=a.src,m=a.postCallbackFn,j="function"==typeof m;if(!e.abortRequests)e.firing=!0,window[a.internalCallbackName]=function(c){try{c||(c={});var h=a.callbackFn;e.firing=!1;e.fired.push(a);e.num_of_jsonp_responses++;h(c);j&&m(c)}catch(n){n.message="DIL jsonp callback caught error with message "+n.message;b=n.message;d.push(b);n.filename=n.filename||"dil.js";n.partner=f;DIL.errorModule.handleError(n);try{h({error:n.name+"|"+n.message}),j&&m({error:n.name+"|"+n.message})}catch(o){}}finally{e.registerRequest()}},
o=document.createElement("script"),o.addEventListener&&o.addEventListener("error",function(d){b="jsonp script tag error listener received the event of type "+d.type+" with src "+i;c.handleScriptError(b,a)},!1),o.type="text/javascript",o.src=i,h=document.getElementsByTagName("script")[0],h.parentNode.insertBefore(o,h),e.sent.push(a)},handleScriptError:function(a,b){var c=l;d.push(a);c.abortRequests=!0;c.firing=!1;c.errored.push(b);c.num_of_jsonp_errors++;c.registerRequest()}},q={isValidPdata:function(a){return a instanceof
Array&&this.removeEmptyArrayValues(a).length?!0:!1},isValidLogdata:function(a){return!this.isEmptyObject(a)},isEmptyObject:function(a){if(a!==Object(a))return!0;for(var b in a)if(a.hasOwnProperty(b))return!1;return!0},removeEmptyArrayValues:function(a){for(var b=0,c=a.length,d,e=[],b=0;b<c;b++)d=a[b],"undefined"!=typeof d&&null!=d&&e.push(d);return e}},k={addListener:function(){if(document.addEventListener)return function(a,b,c){a.addEventListener(b,function(a){"function"==typeof c&&c(a)},!1)};if(document.attachEvent)return function(a,
b,c){a.attachEvent("on"+b,function(a){"function"==typeof c&&c(a)})}}(),convertObjectToKeyValuePairs:function(a,b,c){var d=[],b=b||"=",e,f;for(e in a)f=a[e],"undefined"!=typeof f&&null!=f&&d.push(e+b+(c?encodeURIComponent(f):f));return d},encodeAndBuildRequest:function(a,b){return this.map(a,function(a){return encodeURIComponent(a)}).join(b)},map:function(a,b){if(Array.prototype.map)return a.map(b);if(void 0===a||null===a)throw new TypeError;var c=Object(a),d=c.length>>>0;if("function"!==typeof b)throw new TypeError;
for(var e=Array(d),f=0;f<d;f++)f in c&&(e[f]=b.call(b,c[f],f,c));return e},filter:function(a,b){if(!Array.prototype.filter){if(void 0===a||null===a)throw new TypeError;var c=Object(a),d=c.length>>>0;if("function"!==typeof b)throw new TypeError;for(var f=[],e=0;e<d;e++)if(e in c){var h=c[e];b.call(b,h,e,c)&&f.push(h)}return f}return a.filter(b)},getCookie:function(a){var a=a+"=",b=document.cookie.split(";"),c,d,e;for(c=0,d=b.length;c<d;c++){for(e=b[c];" "==e.charAt(0);)e=e.substring(1,e.length);if(0==
e.indexOf(a))return decodeURIComponent(e.substring(a.length,e.length))}return null},setCookie:function(a,b,c,d,e,f){var h=new Date;c&&(c*=6E4);document.cookie=a+"="+encodeURIComponent(b)+(c?";expires="+(new Date(h.getTime()+c)).toUTCString():"")+(d?";path="+d:"")+(e?";domain="+e:"")+(f?";secure":"")},extendArray:function(a,b){return a instanceof Array&&b instanceof Array?(Array.prototype.push.apply(a,b),!0):!1},extendObject:function(a,b,c){var d;if(a===Object(a)&&b===Object(b)){for(d in b)if(b.hasOwnProperty(d)&&
(q.isEmptyObject(c)||!(d in c)))a[d]=b[d];return!0}return!1},getMaxCookieExpiresInMinutes:function(){return((new Date(t.COOKIE_MAX_EXPIRATION_DATE)).getTime()-(new Date).getTime())/1E3/60}};"error"==f&&0==e&&k.addListener(window,"load",function(){DIL.windowLoaded=!0});var x=function(){A();!m&&!l.abortRequests&&u.attachIframe()},A=function(){m||setTimeout(function(){!l.firstRequestHasFired&&!l.adms.admsProcessingStarted&&!l.adms.calledBack&&y.submit()},DIL.constants.TIME_TO_DEFAULT_REQUEST)},z=document,
v=a.iframeAttachmentDelay;"error"!=f&&(DIL.windowLoaded?x():"complete"!=z.readyState&&"loaded"!=z.readyState?k.addListener(window,"load",x):DIL.isAddedPostWindowLoadWasCalled?k.addListener(window,"load",x):(v="number"==typeof v?parseInt(v,10):0,0>v&&(v=0),setTimeout(x,v||DIL.constants.TIME_TO_CATCH_ALL_DP_IFRAME_ATTACHMENT)));this.api=y;this.getStuffedVariable=function(a){var b=s[a];!b&&"number"!=typeof b&&(b=k.getCookie(a),!b&&"number"!=typeof b&&(b=""));return b};this.validators=q;this.helpers=
k;this.constants=t;if(window._dil_unit_tests)this.pendingRequest=i,this.requestController=l,this.setDestinationPublishingUrl=h,this.destinationPublishing=u,this.requestProcs=w,this.log=d},function(){var a=document,c;if(null==a.readyState&&a.addEventListener)a.readyState="loading",a.addEventListener("DOMContentLoaded",c=function(){a.removeEventListener("DOMContentLoaded",c,!1);a.readyState="complete"},!1)}(),DIL.extendStaticPropertiesAndMethods=function(a){var c;if(a===Object(a))for(c in a)a.hasOwnProperty(c)&&
(this[c]=a[c])},DIL.extendStaticPropertiesAndMethods({version:"3.0",jsonVersion:1,constants:{TIME_TO_DEFAULT_REQUEST:50,TIME_TO_CATCH_ALL_DP_IFRAME_ATTACHMENT:500},windowLoaded:!1,dils:{},isAddedPostWindowLoadWasCalled:!1,isAddedPostWindowLoad:function(a){this.isAddedPostWindowLoadWasCalled=!0;this.windowLoaded="function"==typeof a?!!a():"boolean"==typeof a?a:!0},create:function(a){try{return new DIL(a)}catch(c){return(new Image(0,0)).src="http://error.demdex.net/event?d_nsid=0&d_px=14137&d_ld=name%3Derror%26filename%3Ddil.js%26partner%3Dno_partner%26message%3DError%2520in%2520attempt%2520to%2520create%2520DIL%2520instance%2520with%2520DIL.create()%26_ts%3D"+
(new Date).getTime(),Error("Error in attempt to create DIL instance with DIL.create()")}},registerDil:function(a,c,d){c=c+"$"+d;c in this.dils||(this.dils[c]=a)},getDil:function(a,c){var d;"string"!=typeof a&&(a="");c||(c=0);d=a+"$"+c;return d in this.dils?this.dils[d]:Error("The DIL instance with partner = "+a+" and containerNSID = "+c+" was not found")},dexGetQSVars:function(a,c,d){c=this.getDil(c,d);return c instanceof this?c.getStuffedVariable(a):""},xd:{postMessage:function(a,c,d){var b=1;if(c)if(window.postMessage)d.postMessage(a,
c.replace(/([^:]+:\/\/[^\/]+).*/,"$1"));else if(c)d.location=c.replace(/#.*$/,"")+"#"+ +new Date+b++ +"&"+a}}}),DIL.errorModule=function(){var a=DIL.create({partner:"error",containerNSID:0,disableDestinationPublishingIframe:!0}),c={harvestererror:14138,destpuberror:14139,dpmerror:14140,generalerror:14137,error:14137,noerrortypedefined:15021,evalerror:15016,rangeerror:15017,referenceerror:15018,typeerror:15019,urierror:15020},d=!1;return{activate:function(){d=!0},handleError:function(b){if(!d)return"DIL error module has not been activated";
b!==Object(b)&&(b={});var f=b.name?(new String(b.name)).toLowerCase():"",e=[],b={name:f,filename:b.filename?b.filename+"":"",partner:b.partner?b.partner+"":"no_partner",site:b.site?b.site+"":document.location.href,message:b.message?b.message+"":""};e.push(f in c?c[f]:c.noerrortypedefined);a.api.pixels(e).logs(b).useImageRequest().submit();return"DIL error report sent"},pixelMap:c}}();DIL.tools={};
DIL.tools.getSearchReferrer=function(a,c){var d=DIL.getDil("error"),b=DIL.tools.decomposeURI(a||document.referrer),f="",e="",h={queryParam:"q"},f=d.helpers.filter([c===Object(c)?c:{},{hostPattern:/aol\./},{hostPattern:/ask\./},{hostPattern:/bing\./},{hostPattern:/google\./},{hostPattern:/yahoo\./,queryParam:"p"}],function(a){return!(!a.hasOwnProperty("hostPattern")||!b.hostname.match(a.hostPattern))}).shift();return!f?{valid:!1,name:"",keywords:""}:{valid:!0,name:b.hostname,keywords:(d.helpers.extendObject(h,
f),e=h.queryPattern?(f=(""+b.search).match(h.queryPattern))?f[1]:"":b.uriParams[h.queryParam],decodeURIComponent(e||"").replace(/\+|%20/g," "))}};
DIL.tools.decomposeURI=function(a){var c=DIL.getDil("error"),d=document.createElement("a");d.href=a||document.referrer;return{hash:d.hash,host:d.host.split(":").shift(),hostname:d.hostname,href:d.href,pathname:d.pathname.replace(/^\//,""),protocol:d.protocol,search:d.search,uriParams:function(a,d){c.helpers.map(d.split("&"),function(c){c=c.split("=");a[c.shift()]=c.shift()});return a}({},d.search.replace(/^(\/|\?)?|\/$/g,""))}};
DIL.tools.getMetaTags=function(){var a={},c=document.getElementsByTagName("meta"),d,b,f,e,h;for(d=0,f=arguments.length;d<f;d++)if(e=arguments[d],null!==e)for(b=0;b<c.length;b++)if(h=c[b],h.name==e){a[e]=h.content;break}return a};DIL.modules={};
DIL.modules.siteCatalyst={init:function(a,c,d){try{var b={name:"DIL Site Catalyst Module Error"},f;if(!(c instanceof DIL))return f="dilInstance is not a valid instance of DIL",b.message=f,DIL.errorModule.handleError(b),f;b.partner=c.api.getPartner();if(a!==Object(a))return f="siteCatalystReportingSuite is not an object",b.message=f,DIL.errorModule.handleError(b),f;if("function"!=typeof a.m_i||"function"!=typeof a.loadModule)return f="s.m_i is not a function or s.loadModule is not a function",b.message=
f,DIL.errorModule.handleError(b),f;var e=a.m_i("DIL");if(e!==Object(e))return f="m is not an object",b.message=f,DIL.errorModule.handleError(b),f;e.trackVars=this.constructTrackVars(d);e.d=0;e._t=function(){var a,c,d=","+this.trackVars+",",e=this.s,h,s=[];h=[];var i={},l=!1;if(e!==Object(e)||!(e.va_t instanceof Array))return f="Error in m._t function: s is not an object or s.va_t is not an array",b.message=f,DIL.errorModule.handleError(b),f;if(this.d){if(e.lightProfileID)(a=e.lightTrackVars)&&(a=
","+a+","+e.vl_mr+",");else if(e.pe||e.linkType){a=e.linkTrackVars;if(e.pe&&(c=e.pe.substring(0,1).toUpperCase()+e.pe.substring(1),e[c]))a=e[c].trackVars;a&&(a=","+a+","+e.vl_l+","+e.vl_l2+",")}if(a){for(c=0,s=a.split(",");c<s.length;c++)0<=d.indexOf(","+s[c]+",")&&h.push(s[c]);h.length&&(d=","+h.join(",")+",")}for(h=0,c=e.va_t.length;h<c;h++)a=e.va_t[h],0<=d.indexOf(","+a+",")&&null!=e[a]&&""!==e[a]&&(i[a]=e[a],l=!0);l&&this.d.api.signals(i,"c_").submit()}};e.setup=function(){this.d=c};a.loadModule("DIL");
if(a.DIL!==Object(a.DIL)||"function"!=typeof a.DIL.setup)return f="s.DIL is not an object or s.DIL.setup is not a function",b.message=f,DIL.errorModule.handleError(b),f;a.DIL.setup()}catch(h){h.message="DIL Site Catalyst module caught error with message "+h.message;if(c instanceof DIL)h.partner=c.api.getPartner();DIL.errorModule.handleError(h);return h.message}},constructTrackVars:function(a){var c=[],d,b,f,e,h;if(a===Object(a)){d=a.names;if(d instanceof Array&&(f=d.length))for(b=0;b<f;b++)e=d[b],
"string"==typeof e&&e.length&&c.push(e);a=a.iteratedNames;if(a instanceof Array&&(f=a.length))for(b=0;b<f;b++)if(d=a[b],d===Object(d)&&(e=d.name,h=parseInt(d.maxIndex,10),"string"==typeof e&&e.length&&!isNaN(h)&&0<=h))for(d=0;d<=h;d++)c.push(e+d);if(c.length)return c.join(",")}return this.constructTrackVars({names:"pageName,channel,campaign,products,events,pe,pev1,pev2,pev3".split(","),iteratedNames:[{name:"prop",maxIndex:75},{name:"eVar",maxIndex:75}]})}};
DIL.modules.GA={dil:null,arr:null,tv:null,errorMessage:"",defaultTrackVars:["_setAccount","_setCustomVar","_addItem","_addTrans","_trackSocial"],defaultTrackVarsObj:null,signals:{},hasSignals:!1,init:function(a,c,d){try{this.tv=this.arr=this.dil=null;this.errorMessage="";this.signals={};this.hasSignals=!1;var b={name:"DIL GA Module Error"},f="";c instanceof DIL?(this.dil=c,b.partner=this.dil.api.getPartner()):(f="dilInstance is not a valid instance of DIL",b.message=f,DIL.errorModule.handleError(b));
!(a instanceof Array)||!a.length?(f="gaArray is not an array or is empty",b.message=f,DIL.errorModule.handleError(b)):this.arr=a;this.tv=this.constructTrackVars(d);this.errorMessage=f}catch(e){e.message="DIL GA module caught error with message "+e.message;if(c instanceof DIL)e.partner=c.api.getPartner();DIL.errorModule.handleError(e);this.errorMessage=e.message}finally{return this}},constructTrackVars:function(a){var c=[],d,b,f,e;if(this.defaultTrackVarsObj!==Object(this.defaultTrackVarsObj)){f=this.defaultTrackVars;
e={};for(d=0,b=f.length;d<b;d++)e[f[d]]=!0;this.defaultTrackVarsObj=e}else e=this.defaultTrackVarsObj;if(a===Object(a)){a=a.names;if(a instanceof Array&&(b=a.length))for(d=0;d<b;d++)f=a[d],"string"==typeof f&&f.length&&f in e&&c.push(f);if(c.length)return c}return this.defaultTrackVars},constructGAObj:function(a){var c={},a=a instanceof Array?a:this.arr,d,b,f,e;for(d=0,b=a.length;d<b;d++)f=a[d],f instanceof Array&&f.length&&(e=f.shift(),"string"==typeof e&&e.length&&(c[e]instanceof Array||(c[e]=[]),
c[e].push(f)));return c},addToSignals:function(a,c){if("string"!=typeof a||""===a||null==c||""===c)return!1;this.signals[a]instanceof Array||(this.signals[a]=[]);this.signals[a].push(c);return this.hasSignals=!0},constructSignals:function(){var a=this.constructGAObj(),c={_setAccount:function(a){this.addToSignals("c_accountId",a)},_setCustomVar:function(a,b,c){"string"==typeof b&&b.length&&this.addToSignals("c_"+b,c)},_addItem:function(a,b,c,d,e,f){this.addToSignals("c_itemOrderId",a);this.addToSignals("c_itemSku",
b);this.addToSignals("c_itemName",c);this.addToSignals("c_itemCategory",d);this.addToSignals("c_itemPrice",e);this.addToSignals("c_itemQuantity",f)},_addTrans:function(a,b,c,d,e,f,h,m){this.addToSignals("c_transOrderId",a);this.addToSignals("c_transAffiliation",b);this.addToSignals("c_transTotal",c);this.addToSignals("c_transTax",d);this.addToSignals("c_transShipping",e);this.addToSignals("c_transCity",f);this.addToSignals("c_transState",h);this.addToSignals("c_transCountry",m)},_trackSocial:function(a,
b,c,d){this.addToSignals("c_socialNetwork",a);this.addToSignals("c_socialAction",b);this.addToSignals("c_socialTarget",c);this.addToSignals("c_socialPagePath",d)}},d=this.tv,b,f,e,h,m,p;for(b=0,f=d.length;b<f;b++)if(e=d[b],a.hasOwnProperty(e)&&c.hasOwnProperty(e)&&(p=a[e],p instanceof Array))for(h=0,m=p.length;h<m;h++)c[e].apply(this,p[h])},submit:function(){try{if(""!==this.errorMessage)return this.errorMessage;this.constructSignals();this.hasSignals&&this.dil.api.signals(this.signals).submit()}catch(a){a.message=
"DIL GA module caught error with message "+a.message;if(this.dil instanceof DIL)a.partner=this.dil.api.getPartner();DIL.errorModule.handleError(a);return this.errorMessage=a.message}}};
DIL.modules.Peer39={aid:"",dil:null,optionals:null,errorMessage:"",calledBack:!1,script:null,scriptsSent:[],returnedData:[],init:function(a,c,d){try{this.dil=null;this.errorMessage="";this.calledBack=!1;this.optionals=d===Object(d)?d:{};var d={name:"DIL Peer39 Module Error"},b=[],f="";if(this.isSecurePageButNotEnabled(document.location.protocol))f="Module has not been enabled for a secure page",b.push(f),d.message=f,DIL.errorModule.handleError(d);c instanceof DIL?(this.dil=c,d.partner=this.dil.api.getPartner()):
(f="dilInstance is not a valid instance of DIL",b.push(f),d.message=f,DIL.errorModule.handleError(d));"string"!=typeof a||!a.length?(f="aid is not a string or is empty",b.push(f),d.message=f,DIL.errorModule.handleError(d)):this.aid=a;this.errorMessage=b.join("\n")}catch(e){e.message="DIL Peer39 module init() caught error with message "+e.message;if(c instanceof DIL)e.partner=c.api.getPartner();DIL.errorModule.handleError(e);this.errorMessage=e.message}finally{return this}},isSecurePageButNotEnabled:function(a){return"https:"==
a&&!0!==this.optionals.enableHTTPS?!0:!1},constructSignals:function(){var a=this,c=this.constructScript(),d=document.getElementsByTagName("script")[0];window["afterFinished_"+this.aid]=function(){try{var b=a.processData(p39_KVP_Short("c_p","|").split("|"));b.hasSignals&&a.dil.api.signals(b.signals).submit()}catch(c){}finally{a.calledBack=!0,"function"==typeof a.optionals.afterResult&&a.optionals.afterResult()}};d.parentNode.insertBefore(c,d);this.scriptsSent.push(c);return"Request sent to Peer39"},
processData:function(a){var c,d,b,f,e={},h=!1;this.returnedData.push(a);if(a instanceof Array)for(c=0,d=a.length;c<d;c++)b=a[c].split("="),f=b[0],b=b[1],f&&isFinite(b)&&!isNaN(parseInt(b,10))&&(e[f]instanceof Array||(e[f]=[]),e[f].push(b),h=!0);return{hasSignals:h,signals:e}},constructScript:function(){var a=document.createElement("script"),c=this.optionals,d=c.scriptId,b=c.scriptSrc,c=c.scriptParams;a.id="string"==typeof d&&d.length?d:"peer39ScriptLoader";a.type="text/javascript";"string"==typeof b&&
b.length?a.src=b:(a.src=(this.dil.constants.IS_HTTPS?"https:":"http:")+"//stags.peer39.net/"+this.aid+"/trg_"+this.aid+".js","string"==typeof c&&c.length&&(a.src+="?"+c));return a},submit:function(){try{return""!==this.errorMessage?this.errorMessage:this.constructSignals()}catch(a){a.message="DIL Peer39 module submit() caught error with message "+a.message;if(this.dil instanceof DIL)a.partner=this.dil.api.getPartner();DIL.errorModule.handleError(a);return this.errorMessage=a.message}}};
//v3.0
var _scObj = s_gi(s_account);
var dmDil = DIL.create({
    partner : "dailymotion",
    uuidCookie:{
     name:'aam_uuid',
     days:30
     }
    });
DIL.modules.siteCatalyst.init(_scObj, dmDil);


})(this);