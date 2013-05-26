// 1. 2) B) ERROR : The Number of Parameters - Not Matched (tuneDown) 

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

webapis.tv.channel.tuneDown(successCB);
