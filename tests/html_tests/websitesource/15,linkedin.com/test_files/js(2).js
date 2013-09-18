

/* 6rei9ktvfprzc38327x3gt0u3 */

LI.define("RUM.activeTimers");
LI.define("RUM.finishedTimers");
LI.define("RUM.timeMarks");
(function(b){var a=0;
b.startTimer=function(c,e){var d=b.activeTimers;
d[c]=d[c]||[];
d[c].push(e||+new Date())
};
b.markTime=function(c,e){var d=b.timeMarks;
d[c+"ClientTimestampMs"]=e||+new Date()
};
b.stopTimer=function(e,f){var d=f||+new Date(),c=b.activeTimers,h=b.finishedTimers;
h[e]=h[e]||[];
if(c[e]===undefined){return
}var g=c[e].pop();
h[e].push(d-g)
};
b.monkeyTimer=function(e,c,d){return function(){b.startTimer(c);
var f=e.apply(d||this,arguments);
b.stopTimer(c);
return f
}
};
b.monkeyTimeByName=function(d,c,e){var h=d.split("."),g,f=window;
for(g=0;
g<h.length-1;
g++){f=f[h[g]];
if(typeof f!="object"){return
}}if(typeof f[h[g]]!=="function"){return
}f[h[g]]=b.monkeyTimer(f[h[g]],c,e)
};
b.monkeyTimeList=function(d){for(var c in d){if(d.hasOwnProperty(c)){b.monkeyTimeByName(c,d[c])
}}};
b.getNumTimes=function(c){var d=b.finishedTimers;
if(d[c]===undefined){return undefined
}return d[c].length
};
b.getTotalTimes=function(){var e=b.finishedTimers,d={};
for(var c in e){if(e.hasOwnProperty(c)){d[c]=b.getTotalTime(c)
}}return d
};
b.getTotalTime=function(d){var e,c,g=b.finishedTimers,f=0;
if(g[d]===undefined){return undefined
}for(e=0,c=g[d].length;
e<c;
e++){f+=g[d][e]
}return f
};
b.trackEmbeds=function(f){var e,c,d,g=b.finishedTimers;
if(typeof fs=="undefined"){return
}if(typeof f=="string"){f=arguments
}for(e=0;
e<f.length;
e++){(function(h,i){fs.after(f[e],function(){b.markTime("embedsReady");
h()
});
i&&fs.timing(f[e],function(k){for(var l in k){if(k.hasOwnProperty(l)){var j="fizzy"+l.substr(0,1).toUpperCase()+l.substr(1)+"DurationMs";
g[j]=g[j]||[];
g[j].push(k[l])
}}i()
})
})(b.defer(),fs.timing&&b.defer())
}};
b.defer=function(c){a+=1;
return function(){a-=1;
if(a===0&&b.fire){b.fire()
}}
};
b.canFire=function(){return a===0
};
if(LI.TalkIn){b.adTimers=[];
LI.TalkIn.register("adperf",(function(c){return{endTimer:function(d){try{BOOMR.plugins.Ads.endTimer(d)
}catch(f){c.push(d)
}}}
}(b.adTimers)))
}}(LI.RUM));

/* c61ck8yq8xgf9ji3h55bmaux8 */

LI.RUM.monkeyTimeList({"fs.embed":"totalFizzyTime","dust.render":"totalDustRenderTime","dust.register":"totalDustTemplateParseTime"});