function successCB(channels) {
    console.log("getting channels is successful");
}

function errorCB(error) {
    console.log(error.name);
}
 
try {
    // gets 10 channel list of all channels
    var c = webapis.tv.channel.NAVIGATOR_MODE_ALL;
    webapis.tv.channel.getChannelList(successCB, errorCB, c, 0, 10);
} catch (error) {
    console.log(error.name);
}
