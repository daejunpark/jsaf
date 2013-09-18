(function(){
    var mkey='30q1d000r0000000q2R000q79';
    var at=1;
    if(typeof(require)!='function')return;
    require(["sjs/matrix/ad/passion"], function(passion){     
        if( typeof(SohuAdPv_CPD) =='object' ){
            for(key in SohuAdPv_CPD)
            {   
                if(jQuery(key).size()>0 && jQuery('#beans_'+SohuAdPv_CPD[key]).size()==0){
                    passion.report("pv", {ext:'ping',adsrc:at,adid:mkey,monitorkey:mkey,cont_id :'beans_'+SohuAdPv_CPD[key]}); 
                    (function(nkey){
						jQuery(nkey).bind("mousedown.report", function(event){
							if(event.button === 2) return;
							var x = event.layerX || event.offsetX || -33,
								y = event.layerY || event.offsetY || -33;
							passion.report("click", {ext:'ping',cx:x,cy:y,adsrc:at,adid:mkey,monitorkey:mkey,cont_id :'beans_'+SohuAdPv_CPD[nkey]});
						});
					})(key);
                }
            }
        }
        if( typeof(SohuAdFly_nCPD) =='object' ){
            var fshow = (document.body.clientWidth - cWidth)>200;
            var cshow = (document.body.clientWidth - cWidth)>240;
            if(typeof(_O)!='undefined'){
                for(var b=0;b<_O.length;b++){
                    if(typeof(_O[b])=="object" && ('id' in _O[b]) && _O[b].id=='COUPLET' && typeof(_O[b].src)!='undefined'){
                        cshow = false;
                    }
                    if(typeof(_O[b])=="object" && ('id2' in _O[b]) &&_O[b].id=='FLOAT2' && typeof(_O[b].src2)!='undefined'){
                        fshow = false;
                    }
                }
            }
            if(('couple' in SohuAdFly_nCPD) && cshow){
                var couple = {query:{ itemspaceid : SohuAdFly_nCPD.couple, adps : "12000270", adsrc:1 }, 
                    local : {forms : {},exts:{}}};
                couple.local.forms[SohuAdFly_nCPD['couple']] = "couplet";
                couple.local.exts[SohuAdFly_nCPD['couple']]  = {top : 180,zIndex : 11};
           //     passion.ones(couple);
            }
            if(('float_r' in SohuAdFly_nCPD) && fshow){
                var float_r = {query:{ itemspaceid :SohuAdFly_nCPD.float_r, adps : "1000100", adsrc:1 }, 
                    local : {forms : {},exts:{}}};
                float_r.local.forms[SohuAdFly_nCPD['float_r']] = "flyer";
                float_r.local.exts[SohuAdFly_nCPD['float_r']]  = {position : "fixed",bottom : 50,right : 0,zIndex : 10,width : 100,height : 100,bt_replay : false,bt_shut : true,txt_align : "right"};
             //   passion.ones(float_r);
            }
        }
    });
})();