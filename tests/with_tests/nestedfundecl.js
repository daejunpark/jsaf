var x = 50;
with ({ x : 10, y : 20 }) {
  var fe = function ff(){
             var x=8;
             function nestedfun(k){
               var z=5;
               x = y + z + k;
               return x;
             }
             return nestedfun(4);
           }
  actual = fe();
}
expect=29;
actual;
