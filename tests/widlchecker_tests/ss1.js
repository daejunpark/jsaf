function successCB() {
    alert("tuning is successful");
}

function errorCB(error) {
    alert(error.name);
}

webapis.tv.tuneUp(successCB, errorCB, 0, 0);