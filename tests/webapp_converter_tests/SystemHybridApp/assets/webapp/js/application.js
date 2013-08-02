function launchApplication() {
	function onSuccessCallback(system) {
		con.log("in launch successcallback");
		
	}

	function onErrorCallback(e) {
		con.log("in launch errorcallback"+e.name+e.message);
	}

	var appid = document.getElementById("appid").value;
	con.log("invoking launch()");
	try {
		webapis.application.launch(appid,onSuccessCallback,onErrorCallback);
	} catch (e) {
		con.log("error while invoking getPropertyValue" + e.name + e.message);
	}
}

function launchAppControl() {
	/*function onSuccessCallback() {
		con.log("in launchAppControl successcallback");
		
	}

	function onErrorCallback(e) {
		con.log("in launchAppControl errorcallback"+e.name+e.message);
	}

	var appControl = new webapis.ApplicationControl("http://samsungapps.com/appcontrol/operation/view","http://www.naver.com");

	//("http://samsungapps.com/appcontrol/operation/user-defined");
	//("http://samsungapps.com/appcontrol/operation/view","http://www.naver.com");
	//"http://tizen.org/appcontrol/operation/user-defined"
	var appControlReplyCB = 
	{ 
	   // Reply is sent if the requested operation is successfully delivered 
	   onsuccess: function(reply) 
	   { 
		   alert(reply+", length of reply"+reply.length);
	      for (var num = 0; num < reply.length; num++) 
	      { 
	         con.log("reply.data["+num+"].key = "+ reply[num].key); 
	         con.log("reply.data["+num+"].value = "+ reply[num].value); 
	      }
	   },
	    // Something went wrong
	    onfailure: function() {
	       console.log('The launch application control failed');
	    } 
	}
	con.log("invoking launchAppControl()");
	con.log("operation : "+appControl.operation);
	con.log("uri : "+appControl.uri);
	try {
		webapis.application.launchAppControl(appControl, null,
                onSuccessCallback, 
                onErrorCallback,                 appControlReplyCB);
	} catch (e) {
		con.log("error while invoking launchAppControl" +e.code + e.name + e.message);
	}*/
	var testAppControl = new webapis.ApplicationControl("http://samsungapps.com/appcontrol/operation/view","http://www.naver.com");

  	function successCB()
  	{
		alert("launchAppControl pass");		
  	}
  	
  	function errCB ()
  	{
		alert("errorCB, launchAppControl failed");
		
  	}
  	
  	var replyCallBack = 
  		{
  			onsuccess: function(data)
  			{
  				if(data == null)
				{
  					alert(data);
				}
  			},
  			onfailure:function()
  			{
  				alert("onfail");
  			}
  	
  		};
	try {
		webapis.application.launchAppControl(testAppControl, null, successCB, errCB, replyCallBack);						
	} catch (e) {
		alert(e.name + e.message);
	}

}

function getAppsContext() {
	function onRunningAppsContext(contexts) {
	     for (var i = 0; i < contexts.length; i++) {
	         con.log(i+" contextID : " + contexts[i].id);
	         con.log(i+" appID : " + contexts[i].appId);
	     }
	}
	webapis.application.getAppsContext(onRunningAppsContext);
}

function getCurrentAppId() {
	try{
		alert(webapis.application);
		var app = webapis.application.getCurrentApplication();
		
	} catch(e){
		con.log("getCuurentApplication error : " + e.name+e.message);
	}
	con.log("Current application's app id is " + app.appInfo.id);
}