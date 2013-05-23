function successCB(channels) {
    console.log("getting channels is successful");
}

function errorCB(error) {
    console.log(error.name);
}
 
try {
    // gets 10 channel list of all channels
    webapis.tv.channel.getChannelList(successCB, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL, 0, 10);
} catch (error) {
    console.log(error.name);
}
