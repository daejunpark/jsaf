// 1. 1) ERROR : Cannot Found (tune2)

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

try {
  webapis.tv.channel.tune2(successCB, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL, 0);
} catch (error) {
  console.log(error.name);
}

