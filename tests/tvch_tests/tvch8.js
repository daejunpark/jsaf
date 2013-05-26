function successCB(programs) {
    console.log("getting programs is successful");
}

function errorCB(error) {
    console.log(error.name);
}

try { 
    var channel = webapis.tv.channel.getCurrentChannel();
    webapis.tv.channel.getProgramList(channel, webapis.tv.info.getEpochTime(), successCB, errorCB, 3600);
} catch (error) {
    console.log(error.name);
}
