var test11 = {p1: "123", p2: 789, get p1() {return p1;}, p1: 0};
var test12 = {p1: "123", p2: 789, get p1() {return p1;}, "p1": 0};

var test21 = {"p1": "123", p2: 789, p3: 789, get p1() {return p1;}, "p1": 0};
var test22 = {"p1": "123", p2: 789, p3: 789, get p1() {return p1;}, p1: 0};

var test31 = {123: "abc", 456: "def", 123: "ghi"};

var test41 = {123.4: "abc", 567.8: "def", get 123.4() {return 123.4;}, 123.4: "ghi"};
var test42 = {13.1212121212121212125555: "abc", 567.8: "def", get 123.4() {return 123.4;}, "13.121212121212121": "ghi"};
