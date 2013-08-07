function successCB() {
    alert("tuning is successful");
}

function errorCB(error) {
    alert(error.name);
}

try {
    webapis.calendar.getCalendars(0, successCB, errorCB);
    webapis.calendar.getCalendars(0, successCB);
    webapis.calendar.getCalendars(0);
} catch (ex) {}
