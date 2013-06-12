// ERROR : Callback Method Parameter - Not Matched (tuneDown)

function successCB(param) {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

try {
  webapis.tv.channel.tuneDown(successCB, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL, 0);
} catch (error) {
  console.log(error.name);
}

