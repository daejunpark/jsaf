var numOfScreenState = 4;
var numOfCPUState = 1;
var previousSelectedResource = "SCREEN";

function selectLoadFunction() {
	var selected = document.getElementById("powerResource");
    var selectedValue = selected.options[selected.selectedIndex].value;
    var state = document.getElementById("PowerState");
    
    // erase previous option
    if(previousSelectedResource == "SCREEN") {
    	for(var i = numOfScreenState-1; i > -1 ; i--) {
    		state.options[i] = null;		
    	}
    } else if(previousSelectedResource == "CPU") {
    	state.options[0] = null;		
    }
    
    if(selectedValue == "SCREEN") {
    	previousSelectedResource = "SCREEN";
    	state.options[0] = new Option("SCREEN_OFF", "SCREEN_OFF");
		state.options[1] = new Option("SCREEN_DIM", "SCREEN_DIM");
		state.options[2] = new Option("SCREEN_NORMAL", "SCREEN_NORMAL");
		state.options[3] = new Option("SCREEN_BRIGHT", "SCREEN_BRIGHT");
    } else {
    	previousSelectedResource = "CPU";
    	state.options[0] = new Option("CPU_AWAKE", "CPU_AWAKE");
    }
}

function request() {	 
	var resourceParam = document.getElementById("powerResource").value;
	var stateParam = document.getElementById("PowerState").value;

	con.log("request resource "+resourceParam+", state "+stateParam);
	try {
		webapis.power.request(resourceParam, stateParam); 
		con.log("requested successfully");
	} catch (e) {
		con.log("exception while invoking request" + e.name + e.message);
	}
}

function release() {	 
	var resourceParam = document.getElementById("powerResource").value;
	
	con.log("release resource "+resourceParam);
	try {
		webapis.power.release(resourceParam); 
		con.log("released successfully");
	} catch (e) {
		con.log("exception while invoking release" + e.name + e.message);
	}
}

function showValue(newValue)
{
	document.getElementById("range").innerHTML=newValue/10;
	con.log("set screen brightness to "+newValue/10);
	try {
		webapis.power.setScreenBrightness(newValue/10); 
	} catch (e) {
		con.log("exception while invoking setScreenBrightness" + e.name + e.message);
	} 
}

function getScreenBrightness() {
	var brightness;
	try {
		brightness = webapis.power.getScreenBrightness(); 
	} catch (e) {
		con.log("exception while invoking getScreenBrightness" + e.name + e.message);
	} 
	con.result("current brightness : "+brightness);
}

function restoreScreenBrightness() {
	try {
		webapis.power.restoreScreenBrightness();  
	} catch (e) {
		con.log("exception while invoking restoreScreenBrightness" + e.name + e.message);
	} 
	con.result("restoreScreenBrightness success");
}

function isScreenOn() {
	var result;
	try {
		result = webapis.power.isScreenOn();  
	} catch (e) {
		con.log("exception while invoking isScreenOn" + e.name + e.message);
	} 
	con.result("isScreenOn : "+ result);
}

function turnScreenOn() {
	try {
		webapis.power.turnScreenOn();
		con.result("turnScreenOn success");
	} catch (e) {
		con.log("exception while invoking turnScreenOn" + e.name + e.message);
	}	
}

function turnScreenOff() {
	try {
		webapis.power.turnScreenOff();
		con.result("turnScreenOff success");
	} catch (e) {
		con.log("exception while invoking turnScreenOff" + e.name + e.message);
	}	
}

function setScreenStateChangeListener(obj) {
	function onScreenStateChanged (previousState, changedState) {    
		con.log("[Event] Screen state changed from " + previousState + " to " + changedState);  
	}	

	if (obj.checked){		
		try {
			webapis.power.setScreenStateChangeListener(onScreenStateChanged);
			con.result("screen change listener added!");
		} catch (e){
			con.log("error while adding listener" + e.name + e.message);
		}
		
	}
	else {
		try {
			webapis.power.unsetScreenStateChangeListener();
			con.result("screen change listener removed!");
		} catch (e){
			con.log("error while removing listener" + e.name + e.message);
		}
		
	}
}