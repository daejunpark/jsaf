// 1. 3) B) a) ERROR : Callback Method Cannot Found (tuneDown)

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

webapis.tv.channel.tuneDown(successCBNoNamed, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL, 0);

