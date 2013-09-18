(function(){

    var $sidebar = $('sideBar');
    if(!$sidebar)return;
    function onSroll() {
        var winHeight = window.innerHeight || document.documentElement.clientHeight,
            scrollTop = document.body.scrollTop || document.documentElement.scrollTop;
        //document.getElementById("test").innerHTML = scrollTop+ "," + winHeight;
        if( scrollTop > winHeight * 0.8 ){
            //console.log('show');
            $sidebar.style.display="block";
        }else{
            //console.log('hide');
            $sidebar.style.display="none";
        }
    }
    Event.observe($('btn-gotop'),'click', function(event){
        event.preventDefault();
        // var scrollTop = document.body.scrollTop || document.documentElement.scrollTop;
        // var ani=new Ani({},{
        //     time: scrollTop * 0.3 > 800 ? 800 : scrollTop * 0.3,
        //     sepTime: 5,
        //     length: scrollTop,
        //     onstart:function(el){

        //     },
        //     onevery:function(el, percent){
        //         window.scrollTo(0, (1 - this._valueFn(percent) ) * this.length);
        //     },
        //     onstop:function(el){
                window.scrollTo(0, 0);
        //     }
        // });
        //console.log(ani);
    })
    Event.observe(document, 'dom:loaded', onSroll);
    Event.observe(window, 'scroll', onSroll);
    Event.observe(window, 'resize', onSroll);

})();