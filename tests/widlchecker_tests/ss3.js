function successCB() {
    alert("tuning is successful");
}

function errorCB(error) {
    alert(error.name);
}

webapis.tv.channel.tuneUp(successCB, errorCB, webapis.tv.channel.NAVIGATOR_MODE_ALL);