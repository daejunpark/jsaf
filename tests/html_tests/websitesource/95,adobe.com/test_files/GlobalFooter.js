var hideEvidon = false;

adobe.fn.initGeorouting = function() {
    if ( ($('#Georouting').length) && !($.cookies.get('georouting_presented')) && ($(window).width() > 750) ) {
         if ( (URLParser.siteLevel == "solutions") || (URLParser.siteLevel == "products") || (URLParser.siteLevel == "") || ($.string(URLParser.siteLevel).startsWith('solutions.html')) || ($.string(URLParser.siteLevel).startsWith('index.html')) || ($.string(URLParser.siteLevel).startsWith('?')) ) {
			 adobe.fn.georoutingModalSearch();
         }
    }
}

$(document).ready(function() {
	adobe.fn.initGlobalFooter();
	adobe.fn.evidon('_bapw-link');
	
	adobe.fn.initGeorouting();
});
changeRegion = adobe.fn.changeRegionFooter;