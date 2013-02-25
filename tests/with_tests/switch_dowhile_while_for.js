{
  with({a:7}) {
    switch (a) {
      case 3 : ;
      default : with({}) ;
      case 7 : 8;
    }
    do { with({}); } while("d"=="c")
    while("a"=="b") {}
    for(i=0; i<3; i++) with({});
  }
}
