function successCB() {
    console.log("tuning is successful");
}

function errorCB(error) {
    console.log(error.name);
}

try {
  webapis.tv.channel.tune({
    ptc: 9,  
    major: 9,
    minor: 0,
    sourceID : 9,
    programNumber: 9,
    transportStreamID : 90,
    tunecallback: {
        onsuccess: function(programList) { console.log("getting program list is successfully"); }, 
        onerror: function(channelList) { console.log("getting program list is successfully");  }
    }
  }, successCB, errorCB, 0);
} catch (error) {
    console.log(error.name);
}

