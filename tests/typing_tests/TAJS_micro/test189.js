jQuery = function() {}

jQuery.extend = function() {
    for (i = 0; i < 1; i++ ) {
	    options = arguments[ 0 ]
//	    dumpObject(options)
	    for ( name in options ) {
	      this[name] = options[name]
	    }
    }
}

jQuery.extend({
    each: function( ) {},
    browser: {}
});

//dumpObject(jQuery)
//dumpValue(jQuery.browser)
var __result1 = jQuery.browser;  // for SAFE
var __expect1 = options.browser;  // for SAFE

//dumpValue(jQuery.each)
var __result2 = jQuery.each;  // for SAFE
var __expect2 = options.each;  // for SAFE
