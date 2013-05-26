// 1. 3) A) b) ERROR : dictionary member - No Such Member (tune)

function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

webapis.tv.channel.tune({
    ptc: 9,
    major: 9,
    minor: 0,
    sourceID: 9,
    programNumber : 9,
    transportStreamID : 90,
    noMember : 0,
    tunecallback: {
        onsucess: function(programList) { console.log("getting program list is successfully"); }, 
        onerror: function(channelList) { console.log("getting program list is successfully");  }
    }
}, successCB, errorCB, 0);

