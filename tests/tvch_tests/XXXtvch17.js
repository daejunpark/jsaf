// ERROR : Member Cannot Find (interface name)

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

try {
  var program = webapis.tv.channel.getCurrentProgram();
  webapis.tv.channel.tuneDown(successCB, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL, program.whatever);
} catch (error) {
  console.log(error.name);
}

