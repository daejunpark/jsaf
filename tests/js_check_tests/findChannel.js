function successCB(channels) {
    console.log("getting channels is successful");
}

function errorCB(error) {
    console.log(error.name);
}
 
try {
    webapis.tv.channel.findChannel(9, 0, successCB, errorCB);
} catch (error) {
    console.log(error.name);
}
 
