AC.PromoManager=Class.create();AC.PromoManager.instances=$A();Object.extend(AC.PromoManager.prototype,{initialize:function(b,h){if(typeof AC.Storage=="undefined"){return
}AC.PromoManager.instances.push(this);this.storageName="pm-"+b;this.promos=(typeof h=="string")?$$("."+h):$A(h);
var a=0,d=-1,g=$A();this.promos.each(function(j,k){if(j.nodeType){this.promos[k]={promo:j};
j=this.promos[k]}j.promo.hide();if(typeof j.weight=="number"){a+=j.weight;if(d==-1||j.weight<d){d=j.weight
}}else{g.push(k)}if(typeof j.historicalWeight!="number"){j.historicalWeight=0}}.bind(this));
if(g.length){var e=(1-a)/g.length;g.each(function(j){this.promos[j].weight=e}.bind(this));
if(d==-1||e<d){d=e}}this.maxHistory=Math.floor(1/d)-1;var f=AC.Storage.getItem(this.storageName);
if(f){this.history=$A(f.split(","));var e=1/this.history.length;this.history.each(function(j){if(!this.promos[j]){return
}this.promos[j].historicalWeight+=e}.bind(this))}else{this.history=$A()}var c=-1;
this.promos.each(function(j,k){j.weightDiff=j.weight-j.historicalWeight;if(k!=this.history[0]&&(c==-1||j.weightDiff>this.promos[c].weightDiff)){c=k
}}.bind(this));this.selectPromo(c)},selectPromo:function(b,d){if(typeof this.selectedPromoIndex=="number"){var c=this.promos[this.selectedPromoIndex];
if(d&&parseInt(d)!=="NaN"){if(AC.Detector.isCSSAvailable("transition")){c.promo.setVendorPrefixStyle("transition","opacity "+d+"s");
c.promo.style.opacity=0;var e=function(f){if(f.target==c.promo&&f.propertyName=="opacity"){c.promo.setAttribute("style","display: none;");
c.promo.removeVendorEventListener("transitionEnd",e)}};c.promo.addVendorEventListener("transitionEnd",e)
}else{c.promo.fade({duration:d})}}else{c.promo.hide()}}this.selectedPromoIndex=b;
var a=this.promos[b];if(d&&parseInt(d)!=="NaN"){if(AC.Detector.isCSSAvailable("transition")){a.promo.style.opacity=0;
a.promo.show();a.promo.setVendorPrefixStyle("transition","opacity "+d+"s");var e=function(f){if(f.target==a.promo&&f.propertyName=="opacity"){a.promo.setAttribute("style","");
a.promo.removeVendorEventListener("transitionEnd",e)}};a.promo.addVendorEventListener("transitionEnd",e);
window.setTimeout(function(){a.promo.style.opacity=1},10)}else{a.promo.appear({duration:d})
}}else{a.promo.show()}if(typeof d=="function"){d(this,c,a)}this.history.unshift(b);
if(this.history.length>this.maxHistory){this.history.splice(this.maxHistory)}AC.Storage.setItem(this.storageName,this.history.join(","))
},selectNextPromo:function(b){var a=this.selectedPromoIndex+1;if(a>this.promos.length-1){a=0
}this.selectPromo(a,b)}});AC.PromoManager.Rotation=Class.create();AC.PromoManager.Rotation.options={interval:3,animationDuration:0.5};
Object.extend(AC.PromoManager.Rotation.prototype,{initialize:function(b,a){this.managers=b;
this.options=Object.extend(Object.clone(AC.PromoManager.Rotation.options),a);this.rotating=-1;
this.play()},play:function(){this.interval=window.setInterval(this.selectNextPromo.bind(this),this.options.interval*1000)
},pause:function(){window.clearInterval(this.interval)},selectNextPromo:function(){this.rotating++;
if(this.rotating>this.managers.length-1){this.rotating=0}this.managers[this.rotating].selectNextPromo(this.options.animation||this.options.animationDuration)
}});AC.Storage.removeExpired();