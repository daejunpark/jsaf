// 1. 2) C) ERROR : Parameter Type - TypeMismatchError (tuneDown)

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

webapis.tv.channel.tuneDown(successCB, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL, "mismatch");

