try {
    var program = webapis.tv.channel.getCurrentProgram();
    var title = program.title;
    console.log("current channel shows "  + title);
} catch (error) {
    console.log(error.name);
}
 
