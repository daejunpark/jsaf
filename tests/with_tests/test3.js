with ({x:3, z:12}) {
  with ({x:7, u:9}) {
    function car_rent(days, miles) {
      this.rent_days = days;
      this.rent_miles = miles;
      this.cost = 100000;
      var option = this.rent_days * this.rent_miles;
      if (option < 10000) {
        ;
      } else if (option < 50000) {
        this.cost += 50000;
      } else if (option < 100000) {
        this.cost *= 2;
      } else {
        this.cost = 100000000;
      }
    
      return this.cost;
    }
    with ({v:8, w:4}) {
      function f(x) {var x = 20000; return x; }
      [1, x, f(3)]
      var a = { x: 3, v: 5 }
      var this_much_money = car_rent(a.x * v, f(x));
      "You owe me " + this_much_money + "$."
    }
  }
}
