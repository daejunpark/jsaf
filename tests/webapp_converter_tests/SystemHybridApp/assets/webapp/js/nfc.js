var adapter = null;

function getAdapter() {
	try {
		con.log("getting adapter"+webapis.nfc);
		adapter = webapis.nfc.getDefaultAdapter();
		con.log("get adapter success, adapter power : "+adapter.powered);
	} catch(e){
		con.log("error while getting NFC default adapter" + e.name + e.message);
	}
	
	if (adapter != null)
		enableButtons();
}

function setPowered() {
	var state;
	if (adapter.powered == true)
		state = false;
	else
		state = true;
	
	function onSuccessCallback() {
		con.log("power : "+state);
	}

	function onErrorCallback(e) {
		con.log("in setPowered errorcallback "+e.name);
	}
	
	con.log("setPowered to "+state+" state");
	con.log(adapter.powered);
	con.log(adapter.setPowered);
	con.log(adapter.setTagListener);
	try {		
		adapter.setPowered(false, onSuccessCallback, onErrorCallback);
	} catch (e) {
		con.log("error while invoking setPowered" + e.name + e.message);
	}
}

function enableButtons(){
	//document.getElementById("nfcadapter").disabled = true;
	document.getElementById("nfcsetpowered").disabled = false;
	
}

function readNFCTag(nfcTag){
	function readCallback(ndefMessage){
		con.result("ReadNDEF, record count : "+ndefMessage.records.length);
		con.result("ReadNDEF, record count : "+ndefMessage.recordCount);
		//con.result("ReadNDEF, ndefMessage in Bytes : "+ndefMessage.toByte());
		for(var i = 0; i < ndefMessage.recordCount; i++) {
			con.result("NDEFRecord #" + i + ", tnf : " + ndefMessage.records[i].tnf);
			con.result("NDEFRecord #" + i + ", type : " + ndefMessage.records[i].type);
			con.result("NDEFRecord #" + i + ", id : " + ndefMessage.records[i].id);
			con.result("NDEFRecord #" + i + ", payload : " + ndefMessage.records[i].payload);
			con.result("NDEFRecord #" + i + ", text : " + ndefMessage.records[i].text);
			con.result("NDEFRecord #" + i + ", uri : " + ndefMessage.records[i].uri);
			con.result("NDEFRecord #" + i + ", mime : " + ndefMessage.records[i].mime);
		}
		unsetTagListener();
	}
	
	function errorCallback(e){
		con.result("ErrorCallback, readNDEF : "+e.name);
		unsetTagListener();
	}
	
	try {
		nfcTag.readNDEF(readCallback, errorCallback);
	} catch(e){
		con.log("error while readNDEF" + e.name + e.message);
		unsetTagListener();
	}
}

function writeNFCTag(nfcTag){
	function successCallback(){
		con.result("WriteNDEF, success!");
		//unsetTagListener();
	}
	
	function errorCallback(e){
		con.result("ErrorCallback, writeNDEF : "+e.name);
		unsetTagListener();
	}
	
	try {
		var temprecord = new webapis.NDEFRecordText("testStringKwon", "ko-KR");
		var tempmessage = new webapis.NDEFMessage([temprecord]);
		//var tempmessage = new webapis.NDEFMessage();
		//tempmessage.records[0] = temprecord;
		
		nfcTag.writeNDEF(tempmessage, successCallback, errorCallback);
	} catch(e){
		con.log("error while writeNDEF" + e.name + e.message);
		unsetTagListener();
	}
}

function unsetTagListener(){
	try {
		//adapter.unsetTagListener();
		con.result("Tag listener removed!");
	} catch (e){
		con.log("error while removing listener" + e.name + e.message);
	}		
}
function setTagListener() {
	var operation = document.getElementById("TagSelection").value;
			
	var onSuccessCallback = {
			onattach : function(nfcTag){
				con.result("Event, Tag Type : " + nfcTag.type);
				con.result("Event, Tag isSupportedNDEF : " + nfcTag.isSupportedNDEF);
				con.result("Event, Tag ndefSize : " + nfcTag.type);
				//con.result("Event, Tag properties length : " + properties.length);
				con.result("Event, Tag isConnected : " + nfcTag.isConnected);
				
				if (operation == "READ") {
					alert("inREAD");
					readNFCTag(nfcTag);
				} else if(operation == "WRITE") {
					alert("inWRITE");
					writeNFCTag(nfcTag);
				} else if(operation == "TRANSCEIVE") {
					con.log("transceive not supported");
					unsetTagListener();
				} else
					con.log("ERROR getting NFC Tag operation");
					unsetTagListener();
			},
			ondetach : function(){
				con.result("Event, Tag is detached.");
			}
	 }
		
	try {
		adapter.setTagListener(onSuccessCallback);
		con.result("Tag listener added!");
	} catch (e){
		con.log("error while adding listener" + e.name + e.message);
	}			
}

function setPeerListener(obj) {
	var onSuccessCallback = {
			onattach : function(nfcPeer){
				con.result("Event, isConnected : " + nfcPeer.isConnected);
			},
			ondetach : function(){
				con.result("Event, Peer is detached.");
			}
	 }

	if (obj.checked){		
		try {
			adapter.setPeerListener(onSuccessCallback);
			con.result("Peer listener added!");
		} catch (e){
			con.log("error while adding listener" + e.name + e.message);
		}		
	}
	else {
		try {
			adapter.unsetPeerListener();
			con.result("Peer listener removed!");
		} catch (e){
			con.log("error while removing listener" + e.name + e.message);
		}		
	}
}