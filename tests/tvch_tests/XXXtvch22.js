// WARNING : Use Try-Catch for (method name)

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

webapis.tv.channel.tuneUp(successCB, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL, 0);


