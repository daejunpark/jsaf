// 1. 3) A) a) ERROR : dictionary member - Type Not Matched (tune)

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

webapis.tv.channel.tune({
    ptc: 9,
    major: 9,
    minor: "error occurred",
    sourceID: 9,
    programNumber : 9,
    transportStreamID : 90,
    tunecallback: {
        onsucess: function(programList) { console.log("getting program list is successfully"); }, 
        onerror: function(channelList) { console.log("getting program list is successfully");  }
    }
}, successCB, errorCB, 0);

