try {
    var program = webapis.tv.channel.getCurrentProgram();
    console.log("current channel shows "  + program.title);
} catch (error) {
    console.log(error.name);
}
 
