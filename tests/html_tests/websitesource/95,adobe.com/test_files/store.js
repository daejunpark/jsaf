var conversionpod = function() {
    var PRICE_MAX_THRESHOLD = 10;
    var PRICE_MAIN_CLASS = "CPodCostMain";
    var PRICE_SMALL_CLASS = "CPodCostSmall";
    var REQUEST_TIMEOUT = 30000//30 seconds;
    
    this.displayPrices = function (countryCode, productPath, vollversion, upgrade, storeUrl, fromText, upgradeText, upgradeTextRequired, cartButtonId, productName) {
        //Service does not accept full locale...remove the language
        var newCC = countryCode.substring(countryCode.indexOf('_')+1, countryCode.length);
        
        var key = countryCode + '-' + productPath;
        var cachedItem = $.jCacher.get(key);
        
        if(cachedItem) {
            printPrices(cachedItem);
        } else {
            $.ajax({
            	url: storeUrl + '/cfusion/store/services/stateless/jsonshoppingservice.cfc',
            	data: 'method=getCategoryPricesByCountry&countryCode='+newCC+'&categoryPath='+productPath+'&uc=1',
            	dataType: 'jsonp',
            	jsonp: 'callback',
            	jsonCallback: 'getData',
            	timeout: REQUEST_TIMEOUT,
                success: function(prices) {
                    $.each(prices, function(i,item){
                        
                    	//A hack because the current service doesn't return this and the api throws an error w/o it!!!
                    	if(!item.CURRENCYSYMBOL)
                            item.CURRENCYSYMBOL = "US $";
                        
                        var price_obj = adobe2.ecomm.Format.getFormattedPriceObj(item, countryCode.toLowerCase());
                        
                        var symbol = price_obj.symbol;
                        var mPrice = price_obj.dollarAmtTaxExc;
                        var sPrice = price_obj.centAmtTaxExc;
                        var taxLabel = price_obj.taxLabelExc;
                        if(price_obj.taxLabelInc && price_obj.taxLabelInc.length > 0) {
                        	//replace with 
                            mPrice = price_obj.dollarAmtTaxInc;
                            sPrice = price_obj.centAmtTaxInc;
                            taxLabel = price_obj.taxLabelInc;
                        }
                        
                        //Select to choose Main or Small class
                        var priceClass = PRICE_MAIN_CLASS;
                        if(PRICE_MAX_THRESHOLD < (symbol.lenght + mPrice.length + sPrice.length)) {
                            priceClass = PRICE_SMALL_CLASS;
                        }
                        
                        //TODO: Add tax label
                        if (item.LABEL == "Upgrade") {
                            if(cart.isOverlaySupported()) {
                                $("#"+cartButtonId).bind("click", {distmethod: "UPGRADE", storetype: "COM", categorypath: productPath, productname: productName}, adobe.fn.handleCartButton);
                            }
                            
                            if(upgradeTextRequired && upgradeTextRequired != '') {
                            	$("#"+vollversion).addClass('CPodUpgrade');
                            	//$("#"+vollversion).html(upgradeText + " </div><div class=\"CPodCost\">" + fromText.toLowerCase() + " " + symbol + '<span class="'+priceClass+'">' + mPrice + '</span>' + sPrice);
                            	$("#"+vollversion).html(upgradeText + " </div><div class=\"CPodCost\">" + conversionpod.getPriceDisplayHTML(fromText.toLowerCase(), symbol, priceClass, mPrice, sPrice));
                            } else {
                            	//$("#"+vollversion).html(fromText + " " + symbol + '<span class="'+priceClass+'">' + mPrice + '</span>' + sPrice);
                            	$("#"+vollversion).html(conversionpod.getPriceDisplayHTML(fromText, symbol, priceClass, mPrice, sPrice));
                            }
                            
                            return false;
                        } else {
                        	//$("#"+vollversion).html(fromText + " " + symbol + '<span class="'+priceClass+'">' + mPrice + '</span>' + sPrice);
                            $("#"+vollversion).html(conversionpod.getPriceDisplayHTML(fromText, symbol, priceClass, mPrice, sPrice));
                            if(cart.isOverlaySupported()) {
                                $("#"+cartButtonId).bind("click", {distmethod: "FULL", storetype: "COM", categorypath: productPath, productname: productName}, adobe.fn.handleCartButton);
                            } 
                        }
                    });
                }
            })
        }
    };
    
    this.getPriceDisplayHTML = function (from, symbol, priceClass, mPrice, sPrice) {
    	var priceDisplay = "";
    	var mPriceStr = new String(mPrice);
    	if(mPriceStr.length > 2){
    		priceDisplay = from + " <div>" + symbol + '<span class="'+priceClass+'">' + mPrice + '</span>' + sPrice + "</div>";
    	} else {
    		priceDisplay = from + " " + symbol + '<span class="'+priceClass+'">' + mPrice + '</span>' + sPrice;
    	}
    	return priceDisplay;
    };
    
    return this;
}();