window.mailRuToolbarDriver = (function (w, d) {
    var active = false;

    var tp, l, m, mb, oclass = 'portal-headline__projects_open',
        tbTimeout, ht, _s,
        _bubbls = { mail_cnt: "g_mail_events", my_cnt: "g_my_events", games_cnt: "g_games_events" };

    function eventCancel(e) {
        e.stopPropagation ? e.stopPropagation() : e.cancelBubble = true;
        e.preventDefault ? e.preventDefault() : e.returnValue = false;
    }
    function event(a, o, t, f) {
        if (o.addEventListener) o[a ? 'addEventListener' : 'removeEventListener'](t, f, false);
        else if (o.attachEvent) o[a ? 'attachEvent' : 'detachEvent']('on' + t, f);
    }
    function hasClass(ele, cls) {
        return ele.className.match(new RegExp('(\\s|^)' + cls + '(\\s|$)'));
    }
    function addClass(ele, cls) {
        if (!hasClass(ele, cls)) ele.className += ' ' + cls;
    }
    function removeClass(ele, cls) {
        if (hasClass(ele, cls)) {
            var reg = new RegExp('(\\s+|^)' + cls + '(\\s+|$)');
            ele.className = ele.className.replace(reg, ' ');
        }
    }
    function menuToogle(e) {
        clearTimeout(ht);
        ht = null;
        eventCancel(e);
        hasClass(mb, oclass) ? menuHide() : menuShow();
    }
    function menuShow() {
        tp = d.getElementById('topPanel');
        event(1, d, 'click', menuHide);
        if (tp) tp.style.zIndex = 5000;
        addClass(mb, oclass);
    }
    function menuHide() {
        event(0, d, 'click', menuHide);
        if (tp) tp.style.zIndex = 2501;
        removeClass(mb, oclass);
    }
    function menuOver() {
        clearTimeout(ht);
        ht = null;
    }
    function menuOut() {
        clearTimeout(ht);
        ht = setTimeout(menuHide, 4000);
    }

    function updateToolbar(xx, yy, onlyOne) {
        tbTimeout = null;
        var head = d.getElementsByTagName("head")[0];
        if (_s) head.removeChild(_s);

        var _ut = w.top.MRG_toolbarUpdateTimeout;
        if (_ut > 0 && d.getElementById('mailRuToolbar')) {
            _s = d.createElement('script');
            _s.type = 'text/javascript';
            _s.src = "http://swa.mail.ru/cgi-bin/counters?JSONP_call=MRG_updateToolbar&gamescnt=1&rnd=" + Math.random();
            head.appendChild(_s);
            if (!onlyOne) {
                tbTimeout = setTimeout(updateToolbar, _ut);
            }
        } else {
            w.top.MRG_updateToolbar = null;
        }
    }

    // startup interval
    var bindFn = function () {
        if (active) {
            return;
        }
        active = true;
        l = d.getElementById('portal-headline__menulink');
        m = d.getElementById('portal-headline__menu');
        mb = d.getElementById('portal-headline__box');
        if (l == null || m == null || mb == null) return;
        event(1, l, 'click', menuToogle);
        event(1, l, 'mouseout', menuOut);
        event(1, l, 'mouseover', menuOver);
        event(1, m, 'mouseout', menuOut);
        event(1, m, 'mouseover', menuOver);

        if (!w.top.MRG_updateToolbar) {
            w.top.MRG_updateToolbar = function (a) {
                var cl_ = "portal-headline__link_ext";
                for (var k in _bubbls) {
                    var e = d.getElementById(_bubbls[k]);
                    if (!e) continue;
                    if (a && a.status === 'ok' && a.data && (a.data[k] > 0 || a.data[k] === '99+')) {
                        addClass(e.parentNode, cl_);
                        e.innerHTML = a.data[k];
                    } else {
                        removeClass(e.parentNode, cl_);
                    }
                }
            };
            updateToolbar();
        } else updateToolbar(0, 0, true);

    },
    unbindFn = function () {
        active = false;
        if (ht != null) {
            clearTimeout(ht);
            ht = null;
        }
        if (tbTimeout != null) {
            clearTimeout(tbTimeout);
            tbTimeout = null;
        }
    },
    rebindFn = function () {
        unbindFn();
        bindFn();
    };

    setTimeout(bindFn, 1000);

    return {
        bind: bindFn,
        unbind: unbindFn,
        rebind: rebindFn
    };
})(window, document);
