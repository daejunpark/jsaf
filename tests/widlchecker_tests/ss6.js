function successCB() {
    alert("OK");
}

function errorCB(error) {
   alert("Error : " + error);
}

var channelInfo = new Object();
channelInfo.ptc = 0;
channelInfo.major = 1;
channelInfo.minor = 2;
channelInfo.lcn = 3;
channelInfo.sourceID = 4;
channelInfo.programNumber = 5;
channelInfo.transportStreamID = 6;
channelInfo.originalNetworkID = 7;
channelInfo.serviceName = "8";
channelInfo.channelName = "9";

var startTime = 10;
var duration = 11;
var forError = new Object();

try {
    // 1. Error on argument #1 : channelInfo
    webapis.tv.channel.getProgramList(forError, startTime, successCB, errorCB, duration);
    // 2. Error on argumnet #2 : startTime
    webapis.tv.channel.getProgramList(channelInfo, forError, successCB, errorCB, duration);
    // 3. Error on argument #3 : duration
    webapis.tv.channel.getProgramList(channelInfo, startTime, successCB, errorCB, forError);
   channelInfo.abc = "ERROR";
   // 4. Error on argument #1 : channelInfo (properties are different)
    webapis.tv.channel.getProgramList(channelInfo, startTime, successCB, errorCB, duration);
} catch(ex) {
}