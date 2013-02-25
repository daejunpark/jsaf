var x = {
  "test string": abc,
  set prop1(p1) {
    alert('test1');
  },
  set prop2(eval) {
    alert('test2');
  },
  set prop3(arguments) {
    alert('test3');
  }
};
