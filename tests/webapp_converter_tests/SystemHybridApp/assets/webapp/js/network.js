var retrievedNetworks;

function getAvailableNetworks() {
	function onSuccessCallback(networks) {
		con.log("in success of getAvailableNetworks");
		
		// save networks object for future use
		retrievedNetworks = networks;
		
		// enable buttons
		enableNetworkButtons();
				
		var select = document.getElementById("availableNetwork");
		for ( var i = 0; i < networks.length; i++) {
			select.options[i] = new Option(networks[i].interfaceType, networks[i].interfaceType);
		}
	}

	function onErrorCallback(e) {
		con.log("in errorcallback" + e.name + e.message);
	}

	try {
		webapis.network
				.getAvailableNetworks(onSuccessCallback, onErrorCallback);
	} catch (e) {
		con.log("exception while invoking getAvailableNetworks" + e.name
				+ e.message);
	}
}

function getNetworkInformation() {
	var interfaceType = document.getElementById("availableNetwork").value;
	
	for ( var i = 0; i < retrievedNetworks.length; i++) {
		if(interfaceType == retrievedNetworks[i].interfaceType) {
			con.result("dns : "+retrievedNetworks[i].dns);
			con.result("dnsMode : "+retrievedNetworks[i].dnsMode);
			con.result("gateway : "+retrievedNetworks[i].gateway);
			con.result("subnetMask : "+retrievedNetworks[i].subnetMask);
			con.result("ip : "+retrievedNetworks[i].ip);
			con.result("ipMode : "+retrievedNetworks[i].ipMode);
			con.result("mac : "+retrievedNetworks[i].mac);
			con.result("interfaceType : "+retrievedNetworks[i].interfaceType);		
			break;
		}
	}
}

function enableNetworkButtons() {
	// enable getNetworkInformation button
	document.getElementById("getNetworkInformation").disabled = false;
	document.getElementById("isActive").disabled = false;
	document.getElementById("networkCheckbox").disabled = false;
}

function isActive() {
	var interfaceType = document.getElementById("availableNetwork").value;
	for ( var i = 0; i < retrievedNetworks.length; i++) {
		if(interfaceType == retrievedNetworks[i].interfaceType){
			con.result(retrievedNetworks[i].interfaceType+" isActive : "+retrievedNetworks[i].isActive());
			break;
		}
	}
}
	
function setNetworkWatchListener(obj) {
	var onSuccessCallback = {
			onconnect : function (type) {
				con.log("[Event] "+type + " is connected successfully");      
			},      
			ondisconnect : function(type) {          
				con.log("[Event] "+type + " is disconnected");      
			}
	}
		
	function onErrorCallback(e) {
		con.log("in errorcallback" + e.name + e.message);
	}

	if (obj.checked){		
		try {
			var interfaceType = document.getElementById("availableNetwork").value;
			for ( var i = 0; i < retrievedNetworks.length; i++) {
				if(interfaceType == retrievedNetworks[i].interfaceType) {
					if(retrievedNetworks[i].isActive()){
						retrievedNetworks[i].setWatchListener(onSuccessCallback, onErrorCallback);
						break;
					}
					else{
						con.log(retrievedNetworks[i].interfaceType+" is not Active.");
						break;
					}
				}
			}
		} catch (e){
			con.log("error while adding listener" + e.name + e.message);
		}
		con.result("Watch listener added!");
	}
	else {
		try {
			var interfaceType = document.getElementById("availableNetwork").value;
			for ( var i = 0; i < retrievedNetworks.length; i++) {
				if(interfaceType == retrievedNetworks[i].interfaceType) {
					if(retrievedNetworks[i].isActive()){
						retrievedNetworks[i].unsetWatchListener();
						break;
					}
					else{
						con.log(retrievedNetworks[i].interfaceType+" is not Active.");
						break;
					}
				}
			}
		} catch (e){
			con.log("error while removing listener" + e.name + e.message);
		}
		con.result("Watch listener removed!");
	}
}