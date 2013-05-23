try {
    var channel = webapis.tv.channel.getCurrentChannel();
    console.log("current channel is "  + channel.channelName);
} catch (error) {
    console.log(error.name);
}
 
