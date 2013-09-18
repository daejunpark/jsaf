function _$(a) {
    return (typeof a == "object") ? a : d.getElementById(a)
}

function _$c(a) {
    return d.createElement(a)
}

function addParamForUnion(a) {
    var b = _$("unc");
    var e = (a.indexOf("?") != -1) ? "&" : "?";
    if (b && b.name != "") {
        a += e + "unc=" + b.value
    }
    return a
}

function tp(g) {
    if (d.flpage.w.value.length > 0) {
        var e = g.href;
        g.href = g.href.replace(/w=.+/g, "w=");
        g.href = g.href.replace(/SearchSolved\.e\?sp=S.+/g, "SearchSolved.e?sp=S");
        var f = d.flpage.w.value.trim();
        g.href = g.href + encodeURIComponent(f)
    } else {
        if (g.href.indexOf("more.shtml") != -1) {
            g.href = "http://www.soso.com/more.shtml"
        } else {
            g.href = "http://" + g.hostname
        }
    }
}
if (!String.prototype.trim) {
    String.prototype.trim = function () {
        return this.replace(/(^\s*)|(\s*$)/g, "")
    }
}

function logryl(e, b, j, h, f) {
    var g = window.__searchId || "-";
    var a = new Image(1, 1);
    a.src = "http://pr.soso.com/pingd?srctype=web_click&ckey=" + e + "&seq=" + b + "&pos=" + j + "&resid=" + h + "&qkey=" + f + "&searchId=" + g + "&rand=" + Math.random()
}

function urlJump(e) {
    if (!-[1, ]) {
        var b = document.createElement("a");
        b.href = e;
        document.body.appendChild(b);
        b.click()
    } else {
        location.href = e
    }
} (function () {
    var a = ["img", "video", "music", "ask", "baike", "news", "map", "more"];
    var f = _$("tb");
    var e = f.getElementsByTagName("a");
    for (var b = 0; b < e.length; b++) {
        (function (g) {
            e[g].onclick = function () {
                st_get(this, "web." + a[g] + ".r", 0);
                tp(this);
                this.href = addParamForUnion(this.href);
                if (typeof __sd == "undefined") {
                    return
                } else {
                    if (d.flpage.w.value.length > 0 && (a[g] == "blog" || a[g] == "news")) {
                        this.href = this.href + "&sd=" + __sd
                    }
                }
            }
        })(b)
    }
})();
var _view = (d.compatMode.toLowerCase() == "css1compat") ? d.documentElement : d.body;

function openLogin() {
    var h, l, a;
    var j = parseInt(_view.clientWidth),
        m = parseInt(_view.clientHeight),
        f = parseInt(_view.scrollWidth),
        n = parseInt(_view.scrollHeight);
    var b = _$("pageMask"),
        g = _$("loginDiv");
    g.innerHTML = '<iframe name="login_frame" frameborder="0" scrolling="auto" width="100%" height="100%"></iframe>';
    var e = g.getElementsByTagName("iframe")[0];
    if (arguments.length > 0 && arguments[0] == 1) {
        h = "self";
        l = "http://www.soso.com/jump.html?action=" + ((arguments.length > 1) ? arguments[1].toString() : "")
    } else {
        h = "top";
        l = location.href
    } if (l.indexOf("unc") >= 0) {
        var k = l.indexOf("?") == -1 ? "?" : "&";
        l += k + "uck=p"
    }
    a = "http://ui.ptlogin2.soso.com/cgi-bin/login?appid=6000902&s_url=" + encodeURIComponent(l) + "&f_url=loginerroralert&target=" + h + "&link_target=blank&low_login=1&rd" + Math.random();
    if (b) {
        b.style.display = "block";
        b.style.width = f + "px";
        b.style.height = n + "px"
    }
    if (g && e) {
        g.style.left = (j - parseInt(g.style.width)) / 2 + "px";
        g.style.top = (m - parseInt(g.style.height)) / 2 + "px";
        e.src = a;
        g.style.display = ""
    }
    st_get("web", "w.func", 0)
}

function ptlogin2_onResize(f, a) {
    var e = _$("loginDiv"),
        b = e.getElementsByTagName("iframe")[0];
    if (e) {
        if (/safari/.test(window.navigator.userAgent.toLowerCase()) == true) {
            e.style.width = f + 10 + "px";
            e.style.height = a + 60 + "px"
        } else {
            e.style.width = f + "px";
            e.style.height = a + "px"
        }
        e.style.visibility = "hidden";
        e.style.visibility = "visible"
    }
    if (b.document) {
        document.frames.login_frame.document.getElementById("u").focus()
    } else {
        b.contentWindow.document.getElementById("u").focus()
    }
}

function ptlogin2_onClose() {
    var a = _$("loginDiv"),
        b = _$("pageMask");
    a.style.display = "none";
    b.style.display = "none"
}

function execfcall(b) {
    var a = _$c("script");
    a.setAttribute("type", "text/javascript");
    a.setAttribute("src", b);
    d.getElementsByTagName("head")[0].appendChild(a)
}

function getcookie(b) {
    var a = d.cookie.match(new RegExp("(^| )" + b + "=([^;]*)(;|$)"));
    return a ? unescape(a[2]) : null
}

function delCookie(b) {
    for (var a = 0; a < b.length; a++) {
        if (getcookie(b[a])) {
            d.cookie = b[a] + "=;path=/;domain=.soso.com;expires=" + (new Date("1970/1/1").toGMTString())
        }
    }
}

function logout() {
    delCookie(["uin", "skey", "luin", "lskey", "__nick", "__face"]);
    st_get("web", "w.func", 0);
    var a = "";
    a = location.href;
    urlJump(a)
}

function HTMLEncode(a) {
    return a.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/'/g, "&#146;").replace(/\ /g, "&nbsp;").replace(/\n/g, "").replace(/\t/g, "&nbsp;&nbsp;&nbsp;&nbsp;")
}

function getUin() {
    var a = getcookie("uin") ? getcookie("uin") : getcookie("luin");
    return a ? a.replace(/^[o0]+/i, "") : a
}

function getSkey() {
    return getcookie("skey") ? getcookie("skey") : getcookie("lskey")
}

function callback(b) {
    var g = getUin(),
        a, e = _$("ua"),
        f = "";
    a = getcookie("__nick") ? getcookie("__nick") : g;
    if (a) {
        f = '<a href="http://faxin.soso.com/?ch=soso.my.id&cid=soso.my.id">' + HTMLEncode(a) + "</a>" + f + '&nbsp;|&nbsp;<a id="s_logout" href="javascript:logout();">退出</a>';
        e.innerHTML = f
    } else {
        e.innerHTML = '<a href="javascript:;" onclick="openLogin();return false;">登录</a>'
    }
    Observer.fire(b)
}
var Observer = (function () {
    var a = new Array(),
        b = false,
        e;
    return {
        register: function (f) {
            if (b) {
                f(e)
            } else {
                a.push(f)
            }
        },
        fire: function (g) {
            b = true;
            e = g;
            for (var f = 0; f < a.length; f++) {
                a[f](g)
            }
        }
    }
})();
//d.domain = "soso.com";
(function () {
    var e = _$("ua"),
        j = _$("pageMask"),
        g, h = getUin(),
        b = getSkey();
    if (e) {
        if (h != null && b != null && /\d{5,}/.test(h)) {
            var a, f = "";
            a = getcookie("__nick") ? getcookie("__nick") : h;
            a = decodeURIComponent(a);
            if (a) {
                f = HTMLEncode(a) + f + '&nbsp;|&nbsp;<a id="s_logout" href="javascript:logout();">退出</a>';
                e.innerHTML = f
            }
        } else {
            e.innerHTML = '<a href="javascript:;" onclick="openLogin();return false;">登录</a>'
        }
    }
})();
window.onresize = function () {
    var a = parseInt(_view.clientWidth);
    var e = parseInt(_view.clientHeight);
    var b = _$("loginDiv");
    if (b) {
        b.style.left = (a - parseInt(b.style.width)) / 2 + "px";
        b.style.top = (e - parseInt(b.style.height)) / 2 + "px"
    }
};
var SMART_CLICK_URL = "/q?ie=utf-8";

function SoSmart() {
    var k = this;
    var n = _$("s_input");
    var s = {};
    var x = false;
    var y = -1;
    var h = [];
    var p = [];
    var g = "";
    var A = "";
    var a = null;
    var l = false;
    var o = 0;
    var f = "";
    var q = "-";
    var u = function () {
        for (pos in h) {
            h[pos].className = "mouseout"
        }
    };
    var z = n.value.trim();
    var v = function () {
        var D = n.getAttribute("smartPid");
        var B = n.getAttribute("smartCh");
        if (y == -1 || Number(y) < p.length) {
            logryl(encodeURIComponent(f), Number(y) + 1, "smartbox", q, encodeURIComponent(n.value))
        }
        var C = SMART_CLICK_URL + "&w=" + encodeURIComponent(n.value) + "&pid=" + D + "&ch=" + B + "&cid=s.idx.smb";
        C = addParamForUnion(C);
        urlJump(C)
    };
    var e = function () {
        l = false;
        y = -1;
        h = [];
        p = [];
        g = "";
        var B = _$("smart_pop");
        if (B != null) {
            B.innerHTML = "";
            B.style.display = "none"
        }
    };
    var b = function () {
        var E = _$("smart_pop");
        var C = 0;
        for (i in p) {
            if (w(p[i].word) == "") {
                return
            }
            if (w(p[i].word).indexOf(n.value) != -1) {
                C = 1;
                break
            }
        }
        for (i in p) {
            var D = _$c("div");
            D.seq = parseInt(i);
            (function () {
                var F = D;
                k.Event.add(D, "mouseover", function () {
                    u();
                    F.className = "mouseover";
                    y = F.seq
                })
            })();
            (function () {
                k.Event.add(D, "mouseout", function () {
                    u();
                    y = -1
                })
            })();
            (function () {
                k.Event.add(D, "mousedown", function () {
                    z = A = n.value = p[y].word;
                    v()
                })
            })();
            var B = _$c("div");
            if (C == 1) {
                B.innerHTML = w(p[i].word).replace(n.value, "<b>" + n.value + "</b>")
            } else {
                B.innerHTML = "<b>" + w(p[i].word) + "</b>"
            }
            D.appendChild(B);
            E.appendChild(D);
            h.push(D)
        }
        E.style.display = "";
        st_get("smartbox", "sb.i", 1);
        return E
    };
    var r = function (B) {
        if (!l && (B.keyCode == 38 || B.keyCode == 40)) {
            x = true;
            m()
        }
        if (h.length == 0) {
            return
        }
        if (B.keyCode == 13 && y != -1) {
            k.Event.stop(B);
            z = n.value;
            v()
        } else {
            if (B.keyCode == 38) {
                k.Event.stop(B);
                u();
                y = (y < 0) ? (h.length - 1) : (y - 1);
                if (y == -1) {
                    g = n.value = A
                } else {
                    h[y].className = "mouseover";
                    g = n.value = p[y].word
                }
            } else {
                if (B.keyCode == 40) {
                    k.Event.stop(B);
                    u();
                    y = (y > h.length - 1) ? 0 : (y + 1);
                    if (y == h.length) {
                        g = n.value = A
                    } else {
                        h[y].className = "mouseover";
                        g = n.value = p[y].word
                    }
                } else {
                    if (B.keyCode == 27) {
                        k.Event.stop(B);
                        u();
                        e();
                        n.value = A
                    }
                }
            }
        }
    };
    var t = function () {
        x = false;
        var B = n.value;
        var C = ++o;
        new k.Ajax({
            type: "GET",
            url: "/smart_web.q?w=" + encodeURIComponent(n.value.trim()),
            timeout: 1000,
            onSuccess: function (D) {
                if (C == o && n.value.trim().length > 0) {
                    x = true;
                    e();
                    A = g = B;
                    if (p = j(D)) {
                        c = b()
                    }
                }
            }
        })
    };
    var w = function (B) {
        if (B == undefined) {
            return ""
        }
        return B.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    };
    var j = function (B) {
        if (B.trim() == "") {
            return false
        }
        B = B.split("\n");
        f = B[0];
        q = B[1];
        res = [];
        for (i = 0; i < B.length - 2; i++) {
            temp = [];
            temp[i] = B[i + 2].split("\t");
            res.push({
                word: temp[i][1],
                hint: temp[i][0],
                type: temp[i][2]
            })
        }
        return res
    };
    var m = function () {
        if (a != null) {
            clearTimeout(a);
            a = null
        }
        a = setTimeout(function () {
            if (!x || n.value.trim().length == 0) {
                e()
            } else {
                if (g != n.value) {
                    t()
                }
            }
            a = null
        }, 100)
    };
    (function () {
        k.Event.add(n, "keydown", function (E) {
            r(E)
        });
        k.Event.add(n, "blur", function () {
            x = false;
            m();
            B()
        });
        k.Event.add(window, "resize", function () {
            x && t()
        });
        var D;
        var C = function () {
            D = setTimeout(function () {
                if (z != n.value.trim()) {
                    z = n.value.trim();
                    x = true;
                    m()
                }
                D = setTimeout(arguments.callee, 80)
            }, 80)
        };
        var B = function () {
            clearTimeout(D)
        };
        k.Event.add(n, "focus", function () {
            C()
        })
    })()
}
var ua = window.navigator.userAgent.toLowerCase();
SoSmart.prototype.Ajax = function (options) {
    var _httpSuccess = function (r) {
        try {
            return !r.status && location.protocol == "file:" || (r.status >= 200 && r.status < 300) || r.status == 304 || ua.indexOf("safari") >= 0 && typeof r.status == "undefined"
        } catch (e) { }
        return false
    };
    var _httpData = function (r, type) {
        var ct = r.getResponseHeader("content-type");
        var data = (!type && ct && ct.indexOf("xml") >= 0);
        data = (type == "xml" || data) ? r.responseXML : r.responseText;
        if (type == "script") {
            eval.call(window, data)
        }
        return data
    };
    options = {
        type: options.type || "POST",
        url: options.url || "",
        timeout: options.timeout || 5000,
        onComplete: options.onComplete || function () { },
        onError: options.onError || function () { },
        onSuccess: options.onSuccess || function () { },
        data: options.data || ""
    };
    if (typeof XMLHttpRequest == "undefined") {
        XMLHttpRequest = function () {
            return new ActiveXObject("Microsoft.XMLHTTP")
        }
    }
    var xml = new XMLHttpRequest();
    xml.open(options.type, options.url, true);
    var timeout = options.timeout;
    var requestDone = false;
    setTimeout(function () {
        requestDone = true
    }, timeout);
    xml.onreadystatechange = function () {
        if (xml.readyState == 4 && !requestDone) {
            if (_httpSuccess(xml)) {
                options.onSuccess(_httpData(xml, options.type))
            } else {
                options.onError()
            }
            options.onComplete();
            xml = null
        }
    };
    xml.send(null)
};
SoSmart.prototype.Event = {
    add: function (a, e, b) {
        if (a.addEventListener) {
            a.addEventListener(e, b, false)
        } else {
            a.attachEvent("on" + e, b)
        }
    },
    remove: function (a, e, b) {
        if (a.removeEventListener) {
            a.removeEventListener(e, b, false)
        } else {
            a.detachEvent("on" + e, b)
        }
    },
    stop: function (a) {
        if (a.preventDefault) {
            a.preventDefault();
            a.stopPropagation()
        } else {
            a.cancelBubble = true;
            a.returnValue = false
        }
    }
};
new SoSmart;
(function () {
    var j = window,
        g = document,
        k = j.location;
    var f = "__sosostat";
    var h = {
        server: "http://dr.soso.com/p1.gif",
        errServer: "http://pr.soso.com/pingd?srctype=exception",
        cookiePrefix: "sost_",
        imgArr: [],
        suidV: "1.0",
        cookie: "",
        query: "",
        hostname: k.hostname,
        referrer: g.referrer,
        domain: "",
        cookieInfo: {},
        queryInfo: {},
        divHandlers: [],
        data: {
            sspos: []
        },
        gatherTypes: {
            ss_c: "div"
        },
        groupTypes: {
            ch: "ch",
            pid: "pid"
        },
        persistentTypes: {
            pid: {
                entr: "ss_pidf"
            },
            cid: {
                entr: "ss_cidf"
            }
        },
        kvSplit: function (r, e, v) {
            if (typeof r != "string" || r === "") {
                return {}
            }
            e = e || "&";
            v = v || "=";
            var q = {};
            var t = r.split(e);
            for (var s = 0; s < t.length; ++s) {
                if (t[s].length == 0) {
                    continue
                }
                var a = t[s];
                var w = a.indexOf(v);
                if (w < 0) {
                    continue
                }
                var b = a.substring(0, w);
                var u = a.substring(w + v.length);
                q[b] = u
            }
            return q
        },
        getDomain: function (a) {
            if (a === undefined && this.domain) {
                return this.domain
            }
            if (a === undefined) {
                a = this.hostname
            }
            var b = a.split(".");
            if (b[b.length - 1].match(/^\d+$/)) {
                domain = a
            } else {
                domain = b.slice(-2).join(".")
            } if (a === this.hostname) {
                this.domain = domain
            }
            return domain
        },
        getCookie: function (a) {
            if (g.cookie !== this.cookie) {
                this.cookie = g.cookie;
                this.cookieInfo = this.kvSplit(g.cookie, "; ")
            }
            return (this.cookieInfo[a]) ? decodeURIComponent(this.cookieInfo[a]) : this.cookieInfo[a]
        },
        getQuery: function (a) {
            if (k.search !== this.query) {
                this.query = k.search;
                this.queryInfo = this.kvSplit(k.search.substring(1))
            }
            return this.queryInfo[a]
        },
        setCookie: function (n, b, p, a, e) {
            a = a || "/";
            e = e || this.getDomain();
            var o = function (l) {
                var q = new Date();
                if (l == "unlimited") {
                    q.setFullYear(2038, 0, 1)
                } else {
                    if (typeof l == "number") {
                        if (l <= 0) {
                            q.setFullYear(1970, 1, 1)
                        } else {
                            q.setTime(q.getTime() + l * 1000)
                        }
                    }
                }
                return q.toGMTString()
            };
            var m = n + "=" + encodeURIComponent(b) + (p || p === 0 ? ("; expires=" + o(p)) : "") + (a ? ";path=" + a : "") + (e ? ";domain=" + e : "");
            g.cookie = m
        },
        delCookie: function (e, a, b) {
            this.setCookie(e, "", 0, a, b)
        },
        loopDivs: function () {
            var a = g.getElementsByTagName("div");
            for (var b = 0; b < a.length; ++b) {
                for (var e = 0; e < this.divHandlers.length; ++e) {
                    this.divHandlers[e](a[b])
                }
            }
        },
        regDivHandler: function (a) {
            this.divHandlers.push(a)
        },
        regDivHandlers: function () {
            for (var a in this.gatherTypes) {
                this.regDivHandler(this.getGatherFunc(a))
            }
            for (var a in this.groupTypes) {
                this.regDivHandler(this.getGroupFunc(a))
            }
        },
        getGatherFunc: function (a, e) {
            var e = e || "^";
            var b = this;
            return function (l) {
                var m = l.getAttribute(a);
                if (m) {
                    b.data[a] = b.data[a] === undefined ? "" : b.data[a];
                    b.data[a] += (b.data[a] === "" ? "" : e) + m;
                    if (a == "ss_c") {
                        b.data.sspos.push(b.getPosition(l))
                    }
                }
            }
        },
        getGroupFunc: function (b, a) {
            var a = a || this.getDomain();
            var e = this;
            return function (t) {
                var s = t.getAttribute(b);
                if (!s) {
                    return
                }
                var q = b + "=" + s;
                var u = t.getElementsByTagName("a");
                var p = new RegExp("[&?]" + b + "=");
                for (var r = 0; r < u.length; ++r) {
                    url = u[r];
                    if (e.getDomain(url.hostname) == a) {
                        if (p.test(url.search)) {
                            continue
                        }
                        url.href = " " + url.href + (url.search ? "&" : "?") + q
                    }
                }
            }
        },
        random: function () {
            var a = 0;
            var b = 0;
            while (a < 5) {
                b = Math.ceil(Math.random().toFixed(8) * 100000000);
                if (b != 0) {
                    break
                }
                a++
            }
            return b
        },
        getRunNum: function (e) {
            e = e || 2;
            var l = (new Date()).valueOf() + e;
            var a = 0;
            var b = 0;
            while ((a = (new Date()).valueOf()) < l) {
                b++
            }
            return b
        },
        reverseStr: function (o, n) {
            var l = ["!", "@", "#", "$", "%", ",", "+", "*", ".", "/"];
            var q = n.toString(2);
            q = q.substr(1);
            var m = o.length;
            var p = q.length;
            var e = 0;
            var a = "";
            while (e < m && e < p) {
                if (q.charAt(e) == "1") {
                    var b = o.charAt(e).toUpperCase();
                    if (b != o.charAt(e)) {
                        a += b
                    } else {
                        if (l[b]) {
                            a += l[b]
                        } else {
                            a += b
                        }
                    }
                } else {
                    a += o.charAt(e)
                }
                e++
            }
            a += o.substr(e);
            return a
        },
        genSuid: function () {
            var l = this.getRunNum(2);
            var e = new Date().getUTCMilliseconds();
            var b = (Math.round(Math.random() * 2147483647) * e) % 10000000000;
            var a = (new Date()).valueOf().toString().substr(5) + b;
            return a
        },
        submit: function (e, o) {
            o = o || this.server;
            var s = "";
            if (e.ref !== undefined) {
                s = "ref=" + e.ref;
                delete (e.ref)
            }
            for (var q in e) {
                s += (s ? "&" : "") + q + "=" + e[q]
            }
            s += (s ? "&" : "") + "rand=" + Math.random();
            var b = o + "?" + s;
            var a = new Image();
            var r = this.imgArr.push(a);
            var p = this.imgArr;
            a.onload = (a.onerror = function () {
                p[r - 1] = null
            });
            a.src = b;
            a = null
        },
        setPersistentParams: function () {
            for (var b in this.persistentTypes) {
                var e = this.persistentTypes[b];
                var a = e.entr || (this.cookiePrefix + b + "_f");
                var l = this.getQuery(b);
                if (l) {
                    if (l == this.getCookie(b)) {
                        this.setCookie(a, "1")
                    } else {
                        this.delCookie(a);
                        this.setCookie(b, l)
                    }
                } else {
                    if (this.getCookie(b)) {
                        this.setCookie(a, "1")
                    }
                }
            }
        },
        getPosition: function (a) {
            var e = a.parentNode;
            var b = 0;
            while (e && e.tagName.toLowerCase() != "body") {
                if (e.tagName.toLowerCase() == "li" && e.getAttribute("loc")) {
                    b = e.getAttribute("loc");
                    break
                }
                e = e.parentNode
            }
            return b
        },
        isSetSuid: function () {
            var b = this.getCookie("suid");
            if (!b) {
                return false
            }
            if (b.length < 13) {
                return false
            }
            var a = /^\d+(\d+)?$/;
            if (a.test(b) && b.length < 20) {
                return true
            } else {
                return false
            }
        },
        run: function (b, a) {
            if (!a) {
                this.data = {
                    sspos: []
                }
            }
            if (!this.isSetSuid()) {
                this.setCookie("suid", this.genSuid(), "unlimited")
            }
            this.setPersistentParams();
            if (!a) {
                this.regDivHandlers();
                this.loopDivs()
            }
            if (typeof (b) != "object") {
                b = {}
            }
            b.ver = 3.2;
            b.ref = encodeURIComponent(this.referrer);
            b.searchId = window.__searchId || 0;
            b.tagId = window.__tagId || 0;
            b.tagId = "xin81^" + b.tagId;
            b.sspos = this.data.sspos.join("^");
            for (var e in this.gatherTypes) {
                var l = this.gatherTypes[e];
                if (this.data[e]) {
                    b[l] = this.data[e]
                }
            }
            this.submit(b)
        },
        prget: function (n, l, m, a) {
            a = a || "web";
            m = m || 0;
            var e = {
                srctype: "getsret",
                ourl: escape(n),
                lurl: escape(window.location),
                suid: this.getCookie("suid"),
                ch: l,
                sort: m,
                sc: a,
                searchId: window.__searchId,
                tagId: window.__tagId || 0
            };
            var b = "http://pr.soso.com/pingd";
            this.submit(e, b)
        }
    };
    window[f] = h;
    h.run()
})();

function pr_getdt1(o, f, n, h, g) {
    var a = document,
        p = "-",
        e = window.__sosostat;
    if (!e.isSetSuid("suid")) {
        e.setCookie("suid", e.genSuid(), "unlimited")
    }
    p = e.getCookie("suid") || "-";
    g = g || "";
    var j = new Image(1, 1);
    var b = window.__searchId || 0;
    var l = window.__pn || 0;
    var k = window.__tagId || 0;
    k = "xin81^" + k;
    h = h || 0;
    j.src = "http://pr.soso.com/pingd?srctype=getsret&ourl=" + escape(o) + "&lurl=" + escape(a.location) + "&suid=" + p + "&ch=" + f + "&sort=" + n + "&subloc=" + h + "&sc=" + g + "&searchId=" + b + "&pn=" + l + "&tagId=" + k + "&rand=" + Math.random()
}

function st_get(f, q, g, b, p) {
    if (f.length == 0 || q.length == 0) {
        return
    }
    p = p || 2;
    if (p == 1) {
        var a = f.parentNode;
        var e = 0;
        while (a && a.tagName.toLowerCase() != "body") {
            if (a.tagName.toLowerCase() == "li" && a.getAttribute("loc")) {
                e = a.getAttribute("loc");
                break
            }
            a = a.parentNode
        }
        g = e || g
    }
    pr_getdt1(f, q, g, b, "web")
}
