// 2. 1) A) ERROR : Member Cannot Found (interface name)

try {
    var channel = webapis.tv.channel.getCurrentChannel();
    console.log("current channel is "  + channel.whatever);
} catch (error) {
    console.log(error.name);
}
