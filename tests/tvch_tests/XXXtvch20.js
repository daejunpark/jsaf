// 1. 2) C) ERROR : Parameter Type - TypeMismatchError (tuneDown)

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

try {
  var program = webapis.tv.channel.getCurrentProgram();
  webapis.tv.channel.tuneDown(successCB, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL, program.title);
} catch (error) {
  console.log(error.name);
}

