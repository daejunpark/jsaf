// outer p is shadowed
function f(p) {
    function g(p) {
        // p is used
        p;
    }
    g(3);
}

// outer p is shadowed
function h(p) {
    function i(p) {
        // p is not used
    }
    i(4);
}

f(1);
h(2);
