with ({a:1, b:2, c:3}){
  (function() {
     var a=4;
     return (function() {
        var c=5;
        with ({a:6, b:7}){
          return a+c;
        } // end of the second with
     }) (); // the second function call
  }) (); // the first function call
} // end of the first with
