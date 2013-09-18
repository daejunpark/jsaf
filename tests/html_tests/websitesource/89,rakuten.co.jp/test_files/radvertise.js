function radvertise(config){
	var hostname = "grp10.ias.rakuten.co.jp";
	var layout_root = "adlayout";

	var rad_width;
	var rad_height;
	var rad_nw;
	var rad_site;
	var rad_cont;
	var rad_format;
	var rad_ftype;
	var rad_month;
	var rad_ssl = "-1";
	var rad_shuffle;
	var rad_tg;
	var param = "";
	for (key in config.params) {
		var val = config.params[key];
		if      (key == "rad_width")  { rad_width=val; }
		else if (key == "rad_height") { rad_height=val; }
		else if (key == "rad_nw")     { rad_nw=val; }
		else if (key == "rad_site")   { rad_site=val; }
		else if (key == "rad_cont")   { rad_cont=val; }
		else if (key == "rad_format") { rad_format=val; }
		else if (key == "rad_ftype")  { rad_ftype=val; }
		else if (key == "rad_month")  { rad_month=val; }
		else if (key == "rad_ssl")    { rad_ssl=val; }
		else if (key == "rad_shuffle"){ rad_shuffle=val; }
		else if (key == "rad_tg")     { rad_tg=val; }
		param += (param == "" ? "" : "&") + encodeURIComponent(key) + "=" + encodeURIComponent(val);
	}

	var protocol = "http:";
	var layout = "advertise.html";
	if (location.protocol == "https:") {
		protocol = "https:";
		layout = "advertises.html";
	}
	switch (rad_ssl) {
		case "0":
			protocol = "http:";
			layout = "advertise.html";
			break;
		case "1":
			protocol = "https:";
			layout = "advertises.html";
			break;
		default:
			break;
	}

	var src = protocol + "//" + hostname + "/" + layout_root + "/";
	if (rad_ftype == "COM") {
		src += rad_nw;
	} else if (rad_month && rad_month.match(/[0-9][0-9][0-9][0-9][0-9][0-9]/)) {
		src += rad_nw + "/" + rad_month;
	} else {
		src += rad_nw + "/" + rad_site + "/" + rad_cont;
	}
	src += "/" + rad_format + "/" + layout + "?" + param;

	var width = rad_width;
	if (rad_width.indexOf("%") == -1) {
		width = rad_width + "px";
	}
	var height = rad_height;
	if (rad_height.indexOf("%") == -1) {
		height = rad_height + "px";
	}

	document.write("<iframe name=\"prov_frame\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\" src=\"" + src + "\" marginwidth=\"0\" marginheight=\"0\" vspace=\"0\" hspace=\"0\" allowtransparency=\"true\" scrolling=\"no\"></iframe>");
}

function radvertisesc(config) {
	var hostname = "grp10.ias.rakuten.co.jp";
	var queryStr = "";
	var frameFlg = 0;
	var charsetParam = "";
	var sslFlg = "-1";
	for (key in config.params) {
		var value = config.params[key];

		if (key == "rad_nw")           { queryStr += "rad_nw="     + encodeURIComponent(value) + "&"; }
		else if (key == "rad_site")    { queryStr += "rad_site="   + encodeURIComponent(value) + "&"; }
		else if (key == "rad_cont")    { queryStr += "rad_cont="   + encodeURIComponent(value) + "&"; }
		else if (key == "rad_type")    { queryStr += "rad_type="   + encodeURIComponent(value) + "&"; }
		else if (key == "rad_pos")     { queryStr += "rad_pos="    + encodeURIComponent(value) + "&"; }
		else if (key == "rad_format")  { queryStr += "rad_format=" + encodeURIComponent(value) + "&"; }
		else if (key == "rad_ftype")   { queryStr += "rad_ftype="  + encodeURIComponent(value) + "&"; }
		else if (key == "rad_month")   { queryStr += "rad_month="  + encodeURIComponent(value) + "&"; }
		else if (key == "rad_charset") { queryStr += "rad_charset="  + encodeURIComponent(value) + "&"; charsetParam = value; }
		else if (key == "rad_frame")   { queryStr += "rad_frame="  + encodeURIComponent(value) + "&"; frameFlg = 1; }
		else if (key == "rad_ssl")     { sslFlg = value; }
		else if (key == "rad_shuffle") { queryStr += "rad_shuffle="+ encodeURIComponent(value) + "&"; }
		else if (key == "rad_tg")      { queryStr += "rad_tg="     + encodeURIComponent(value) + "&"; }
	}
	queryStr += "rad_script=" + encodeURIComponent(0);

	try {
		var protocol = "http:";
		if (location.protocol == "https:") {
			protocol = "https:";
		}
		switch (sslFlg) {
			case "0":
				protocol = "http:";
				break;
			case "1":
				protocol = "https:";
				break;
			default:
				break;
		}
		var target = "innerBodyAdv";
		if (frameFlg == 1) {
			target = "innerBodyMassAdv";
		}
		var url = protocol + "//" + hostname + "/" + target + "/?" + queryStr;
		var js = "<script language=\"JavaScript1.1\" charset=\"" + rgetCharset(charsetParam) + "\" src=\"" + url + "\"></script>";
		document.write(js);
	} catch(e) {
		return null;
	}
}

function radvertisescwrite(html) {
	document.write(html);
}

function rgetCharset(charsetParam) {
	if (charsetParam.match(/^Shift_JIS$/i)) { return "Shift_JIS"; }
	else if (charsetParam.match(/^EUC-JP$/i)) { return "EUC-JP"; }
	else { return "UTF-8"; }
}
