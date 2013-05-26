// 2. 1) B) ERROR : Cannot Assign any values to readonly member (interface name)

try {
    var program = webapis.tv.channel.getCurrentProgram();
    console.log("current channel shows "  + program.title);
    program.title = "new title";
} catch (error) {
    console.log(error.name);
}
