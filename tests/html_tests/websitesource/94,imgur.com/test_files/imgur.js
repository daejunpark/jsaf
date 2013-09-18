if(typeof(async_site_pixel_request_sent) == "undefined") {
    
var fmJsHost = (("https:" == document.location.protocol) ? "https://" : "http://");


var fm_query_string = window.location.search.substr(1).split('&');
var fm_pairs = {};
for (var i = 0; i < fm_query_string.length; i++) {
  var pair = fm_query_string[i].split("=");
  fm_pairs[pair[0]] = pair[1];
}


if (typeof __fm_enc_u === "undefined") {
  var __fm_url = document.URL;
  if (top !== self) {
    if (typeof fm_pairs['fm_url'] === "string") {
      __fm_url = fm_pairs['fm_url'];
    } else if (typeof document.referrer === "string" && document.referrer !== "") {
      __fm_url = document.referrer;
    }
  }
  __fm_enc_u = (typeof encodeURIComponent === 'function') ? encodeURIComponent(__fm_url) : escape(__fm_url);
}
if (typeof(fm_pairs['federated_media_section']) == "string") {
	var federated_media_section = fm_pairs['federated_media_section'];
}

var federated_media_section_source = '';
if (typeof(federated_media_section) == "string") {
  federated_media_section_source = federated_media_section.replace(/([^a-zA-Z0-9_\-\/])|(^\/)/g, "");
  var federated_media_sections = [];
  var section_match = 0;
  for (i = 0; i < federated_media_sections.length; i++) {
    if (federated_media_section_source.toLowerCase() == federated_media_sections[i].toLowerCase()) {
      federated_media_section_source = federated_media_sections[i];
      section_match = 1;
      break;
    }
  }
  if (!section_match) {
    federated_media_section_source = '';
  }
}
document.write('<div style="position:absolute;left:0px;top:0px;visibility:hidden;"><img src="' + fmJsHost + 'tenzing.fmpub.net/?t=s&amp;n=430' + (federated_media_section_source != "" ? "&amp;s=" + federated_media_section_source : "") + '&amp;fleur_de_sel=' + Math.floor(Math.random()*10000000000000000) + '" alt="" style="width:0px;height:0px;" height="0" width="0" /></div>');


// comScore publisher tag
var _comscore = _comscore || [];
_comscore.push({ c1: "2", c2: "3005693", c3: "8", c4: "http%3A%2F%2Fimgur.com%2F" });

(function() {
    var s = document.createElement("script"), el = document.getElementsByTagName("script")[0]; s.async = true;
    s.src = (document.location.protocol == "https:" ? "https://sb" : "http://b") + ".scorecardresearch.com/beacon.js";
    el.parentNode.insertBefore(s, el);
})();


// Quantcast
_qoptions = { tags: 'Tech', qacct: 'p-9dsZX-5zhUuTg' };
var __qc_req = document.createElement('script'); __qc_req.type = 'text/javascript'; __qc_req.async = true; __qc_req.src = fmJsHost +'pixel.quantserve.com/seg/' + _qoptions.qacct + '.js';
var __qcs = document.getElementsByTagName('script')[0]; __qcs.parentNode.insertBefore(__qc_req, __qcs);
var __qc_req2 = document.createElement('script'); __qc_req2.type = 'text/javascript'; __qc_req2.async = true; __qc_req2.src = fmJsHost +'edge.quantserve.com/quant.js';
var __qcs2 = document.getElementsByTagName('script')[0]; __qcs2.parentNode.insertBefore(__qc_req2, __qcs2);

// comScore network tag
var _comscore = _comscore || [];
_comscore.push({ c1: "8", c2: "3005693", c3: "8", c4: "http%3A%2F%2Fimgur.com%2F" });

(function() {
  var s = document.createElement("script"), el = document.getElementsByTagName("script")[0]; s.async = true;
  s.src = (document.location.protocol == "https:" ? "https://sb" : "http://b") + ".scorecardresearch.com/beacon.js";
  el.parentNode.insertBefore(s, el);
})();

// Crowdscience tracking
var cs = document.createElement('script'); cs.type = 'text/javascript'; cs.async = true; cs.src = ('https:' == document.location.protocol ?  'https://secure-' : 'http://') + 'static.crowdscience.com/start-5c5c650d27.js';
var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(cs,s);

// Crowdscience targeting
if (!document.cookie || document.cookie.indexOf("_fm_crowdscience=") < 0) {
  _crowdscience_callback = function(data) {
    if (data && data.segments) {
      var segments = [];
      for (var i=data.segments.length; i--; )
          segments.push('csseg='+data.segments[i])
      segments = segments.join("|");
      var exdate = new Date();
      exdate.setDate(exdate.getDate() + 1);
      document.cookie = "_fm_crowdscience=" + segments + '; expired=' + exdate.toUTCString() + '; path=/';
    }
  }
  
  document.write('<' + 'script type="text/javascript" src="' + (document.location.protocol == 'https:' ? 'https://' : 'http://') + 'static.crowdscience.com/max-5c5c650d27.js?callback=_crowdscience_callback"></scr' + 'ipt>');
}

// Lijit
document.write('<' + 'script type="text/javascript" src="http://www.lijit.com/blog_wijits?json=0&id=trakr&uri=http%3A%2F%2Fwww.lijit.com%2Fusers%2Ffm_audience&js=1"></scr' + 'ipt>');
    var async_site_pixel_request_sent = 1;
}
