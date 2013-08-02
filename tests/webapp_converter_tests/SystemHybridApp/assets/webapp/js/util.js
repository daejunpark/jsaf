var checklog = false;

function enablelog(obj) {
	if (obj.checked){		
		checklog = true;
		con.log("log enabled");
	}
	else {
		con.log("log disabled");
		checklog = false;
	}
}

function cleartext() {
	var textarea = document.getElementById("log");
	textarea.value = "";
}

var con = {
	log : function(content, append, id) {

		if (id == null) {
			id = "log";
		}

		if (append == null) {
			append = true;
		}

		var logEle = document.getElementById(id);
		if (checklog == true) {
			if (append) {
				// logEle.innerHTML += "[LOG:] " + content + "<br/>";
				logEle.value += "[LOG:] " + content + "\n";
			} else {
				// logEle.innerHTML = content;
				logEle.value = content;
			}
		}

	},
	result : function(content) {
		var logEle = document.getElementById("log");
		logEle.value += "[Result:] " + content + "\n";
	}
};
