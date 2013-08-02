/* 
 * NOTE:
 * DO NOT REMOVE below code!
 */

window.eventData = [];

// initialize textarea.
document.addEventListener('DOMContentLoaded', function() {
    var textareas = document.querySelectorAll("textarea");
    for(var i = 0; i < textareas.length; i++) {
        var textarea = textareas[i];
        var value = textarea.getAttribute("data-text");
        if (value != null) {
            textarea.value = value;
        }
    }
}, false);

// adjust page width to device width.
function setMetaDefaultPageSize() {
    var target = $("head script[data-framework-viewport-scale]");
    
    if (target.length == 0) {
        return;
    }
    
    var isNormal = (target.attr("data-framework-viewport-scale") == "true") ? false:true;
    var width = (isNormal) ? 320:720;

    var gameScreen = width;
    var gameDensity = 160 * window.devicePixelRatio;
    var sSize = (window.screen.width > 0) ? window.screen.width : width;
    
    var dpi = 0;
    
    if (sSize != gameScreen) {
      dpi = parseInt(gameScreen * gameDensity / sSize, 10);

      var heads = document.getElementsByTagName('head');
      if (heads && heads[0]) {
        if (heads[0].myReady) {
            return;
        }
        var metaTag = document.createElement("meta");
        metaTag.setAttribute("name", "viewport");
        metaTag.setAttribute("content", "target-densitydpi="+dpi);
        heads[0].appendChild(metaTag);
        heads[0].myReady = true;
      }
    }
    
    // add class to classify screen sizes for body.
    var bodyElem = document.querySelectorAll("body")[0];
    if(bodyElem) {
        var scaleClass = (isNormal == true)?" scale360":" scale720";
        bodyElem.className +=  scaleClass;
    }
    
}

// for galaxyS3 - HTML load issue
document.addEventListener('DOMContentLoaded', setMetaDefaultPageSize, false);

setMetaDefaultPageSize();


function addUserEvent(type, id, event, event2) {
    var functionName = id + "_" + event;
    var evtString = (event2) ? event2.substring(2) : event.substring(2);
	$("#" + id).bind(evtString, window[functionName]);
}

$(document).bind("pagecreate",function(evt) {
    var data = null;
    var remains = [];
    while(eventData.length > 0) {
        data = window.eventData.pop();
        if (evt.target.querySelector("#" + data[1]) && $(evt.target).attr("data-role") == "page") {
            addUserEvent.apply(this, data);
        }
        else {
            remains.push(data);
        }
    }
    window.eventData = remains;
});

function addEventData(type, id, event, event2) {
    window.eventData.push([type, id, event, event2]);
}

/* 
 * NOTE:
 * Additional events are to be placed here.
 */