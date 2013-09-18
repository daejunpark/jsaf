  (function () 
  {
    var n = "ocid", t = "mailsignout", i = "#pagedate, #miniweather, #tg .br1, #tg .br2, #tg .br3, #tg .br4, #fbtwh2", 
    r = ".obhide, #opensh", 
    u = window.location.search.toLowerCase(), 
    f = window.Msn ? window.Msn.Page.signedIn : typeof jQuery != "undefined" ? jQuery.signedIn : "False";
    u.indexOf(n + "=" + t) != - 1 && f === "False" && document.write("<style type='text/css'>" + i + "{visibility:hidden}" + r + "{display:none !important}</style>");
  })(), 
  (function (n, t, i) 
  {
    n.Msn.add("async", (function (n) 
    {
      function c() 
      {
        for(var t, n = 0;n < arguments.length;++ n)
          t = arguments[n], t && (t.d = 1, e(t));
      }
      function e(n) 
      {
        function y() 
        {
          var t = n.dep, r, u;
          if(t && t[0])
          {
            for(r = 0;r < t.length;++ r)
            {
              u = t[r];
              if(typeof u == "string")
                if((n.p || this)[u] !== i)
                  t.splice(r --, 1);
                else
                  break;
              else
                if(u._)
                  t.splice(r --, 1);
                else
                {
                  o(u, y);
                  return;
                }
            }
            t.length ? setTimeout(y, h) : s(n);
          }
          else
            s(n);
        }
        function b() 
        {
          l.onreadystatechange = l.onload = l.onerror = null;
          var n = 0, t = r[c];
          for(delete r[c];t[n];++ n)
            t[n]();
        }
        function d() 
        {
          var n = l.readyState;
          (n == "loaded" || n == "complete") && b();
        }
        function g() 
        {
          var u = 1, f = 0, t, r;
          if(a && a[0])
            for(t = 0;t < a.length;++ t)
            {
              r = a[t];
              if(typeof r == "string")
              {
                u = 0;
                if((n.p || this)[r] === i)
                {
                  f = 1;
                  break;
                }
              }
            }
          return u || f;
        }
        var c = n.url, a = n.dep, p, w, v, l, k;
        if(c || a)
        {
          p = c && f[c];
          if(p)
            p._ ? u(n) : o(p, (function () 
            {
              u(n);
            }));
          else
          {
            c && (f[c] = n);
            if(a)
              for(w = 0;w < a.length;++ w)
                v = a[w], v.url && (v.d = g()), (v.url || v.dep) && e(v);
            c && n.d ? r[c] ? r[c].push(y) : (r[c] = [y, ], l = t.createElement("script"), l.type = "text/javascript", 
            l.onreadystatechange = d, 
            l.onerror = l.onload = b, 
            l.src = c, 
            k = t.getElementsByTagName("HEAD")[0], 
            k.appendChild(l)) : y();
          }
        }
      }
      function o(n, t) 
      {
        if(n.cb)
        {
          var i = n.cb;
          n.cb = (function () 
          {
            i(), t();
          });
        }
        else
          n.cb = t;
      }
      function u(n) 
      {
        n._ = 1, n.cb && (n.cb(), n.cb = i);
      }
      function s(n) 
      {
        u(n);
      }
      var f = {
        
      }, h = 50, r = {
        
      };
      n.async = c;
    }))(n.Msn);
  })(window, document), 
  (function (n) 
  {
    String.prototype.format = (function () 
    {
      for(var t = this, n = 0;n < arguments.length;++ n)
        t = t.replace(new RegExp("\\{" + n + "\\}", "g"), arguments[n]);
      return t;
    }), 
    String.prototype.getCookie = (function () 
    {
      var t = new RegExp("\\b" + this + "\\s*=\\s*([^;]*)", "i"), 
      n = t.exec(document.cookie);
      return n && n.length > 1 ? n[1] : "";
    }), 
    String.prototype.setCookie = (function (n, t, i, r, u) 
    {
      var e, f = [this, "=", n, ], o;
      - 1 == t ? e = "Fri, 31 Dec 1999 23:59:59 GMT" : t && (o = new Date, o.setTime(o.getTime() + t * 86400000), 
      e = o.toUTCString()), 
      e && f.push(";expires=", e), 
      i && f.push(";domain=", i), 
      r && f.push(";path=", r), 
      u && f.push(";secure"), 
      document.cookie = f.join("");
    }), 
    n.getCookie = (function (n) 
    {
      return n.getCookie();
    }), 
    n.setCookie = (function (n, t, i, r, u, f) 
    {
      n.setCookie(t, i, r, u, f);
    });
  })(window.Msn), 
  window.Msn.add("dom", (function (n) 
  {
    var t = document, i = /[\n\t]/g, r = {
      attr : (function (n, t) 
      {
        return n && (n.getAttribute ? n.getAttribute(t, 2) : n[t]) || "";
      }),
      name : (function (n) 
      {
        return n && n.nodeName || "";
      }),
      text : (function (n) 
      {
        return n && (n.textContent || n.innerText) || "";
      }),
      children : (function (n) 
      {
        return n && n.children || [];
      }),
      parent : (function (n) 
      {
        return n && n.parentNode;
      }),
      getElementsByTagName : (function (n) 
      {
        return t.getElementsByTagName(n);
      }),
      create : (function (n) 
      {
        return t.createElement(n);
      }),
      containsClass : (function (n, t) 
      {
        return n && (" " + (n.className || n.getAttribute("class")) + " ").replace(i, " ").indexOf(" " + t + " ") > - 1;
      }),
      getTarget : (function (n) 
      {
        return n && (n.customTarget || n.target || n.srcElement) || document;
      })
    };
    n.dom = r;
  }))(window.Msn), 
  window.Msn.add("event", (function (n) 
  {
    function u(n, i, u) 
    {
      n.attachEvent ? (u[t] || (u[t] = f, r[f ++] = (function () 
      {
        var t = window.event;
        t.customTarget = t.target || t.srcElement || document;
        try
{          t.target = t.customTarget;}
        catch(i)
{          }

        u.call(n, t);
      })), 
      n.attachEvent("on" + i, r[u[t]])) : n.addEventListener && n.addEventListener(i, u, ! 1);
    }
    function e(n, i, u) 
    {
      n.detachEvent ? n.detachEvent("on" + i, r[u[t]]) : n.removeEventListener && n.removeEventListener(i, u, ! 1);
    }
    function o(n, t) 
    {
      i.isLoaded ? setTimeout(n, t) : u(i, "load", (function () 
      {
        setTimeout(n, t);
      }));
    }
    var f = 1, r = [0, ], t = "handlerId", i = window;
    u(i, "load", (function () 
    {
      i.isLoaded = 1;
    })), 
    n.bind = u, 
    n.unbind = e, 
    n.winLoad = o;
  }))(window.Msn), 
  window.Msn.add("dap", (function (n, t, i, r, u, f) 
  {
    function h(n) 
    {
      for(var i = u.length, t = 0;t < i;t ++)
        if(n == u[t])
          return 1;
    }
    function s(n, s, l, a, v) 
    {
      t({
        dep : [{
          url : i
        }, "dapMgr", ],
        cb : (function () 
        {
          var p = r.dapMgr, t, i, y;
          o || f != ! 0 || (t = window.dapMgr.enableUnblockingOnload, t && t(! 0), 
          o = ! 0), 
          i = (function () 
          {
            p.enableACB(a, ! ! (v && v.acb)), p.renderAd(a, n, s, l);
          }), 
          y = u && c.exec(n) && RegExp.$1, 
          y && h(y) ? e.push(i) : i();
        }),
        p : window
      });
    }
    var c = /PG=([^&]*)&/, e = [], o = ! 1;
    s.run = (function () 
    {
      u = 0;
      for(var t = e.length, n = 0;n < t;n ++)
        e[n]();
    }), 
    n.dap = s, 
    t({
      dep : [{
        url : i
      }, ]
    });
  }))(window.Msn, window.Msn.async, window.Msn.Page.dapUrl, 
  window, 
  window.Msn.Page.dapDelay, 
  window.Msn.Page.dapUnblockOnload);
  document.write("<style type='text/css'>.srchh1 .shupsell{display:none}</style>");
  document.write("<style type='text/css'>.cogr .co{display:none}.cogr .cof .co{display:block}</style>");
  window.Msn.add("track", (function (n, t, i, r, u, f, e, o, s, h) 
  {
    function it(n, t) 
    {
      var i, r = 0;
      for(bt(), b.incrementEventNumber();r < et.length;r ++)
        i = et[r], i && i[n] && i.samplingRate >= v && ri(i[n]());
      c.curAop = "", wt(t, c.spinTimeout);
    }
    function bt() 
    {
      var n, t;
      return - 1 == v && (t = c.smpCookie, v = parseInt(u(t)), v = isNaN(v) ? Math.floor(Math.random() * 100) : v % 100, 
      n = location.hostname.match(/([^.]+\.[^.]*)$/), 
      n = n ? n[0] : "", 
      f(t, v, 182, n)), 
      v;
    }
    function wt(n, t) 
    {
      t || (t = c.spinTimeout);
      var i;
      if(n && ! a.ActiveXObject && ! y)
      {
        i = + new Date + t;
        while(+ new Date < i)
          ;
      }
    }
    function dt(n) 
    {
      for(var i = ut(n) || [], t = 0, r;t < i.length;t ++)
      {
        r = l(i[t], "alt") || l(i[t], "title") || dt(i[t]);
        if(r)
          return r;
      }
    }
    function yt(n) 
    {
      if(n)
      {
        var r = ot(n), t = l(r, "id"), i;
        if(c.wrapperId == t)
          return;
        return i = yt(r), i && t ? [i, t, ].join(c.cmSeparator) : t || i;
      }
    }
    function lt(n) 
    {
      if(! n)
        return;
      var u = ot(n), i, t = 0, r = 0;
      if(! l(u, "id"))
      {
        t = lt(u);
        if(t)
          t --;
        else
          return 0;
      }
      for(i = ut(u) || [];r < i.length;r ++)
      {
        if(i[r] == n)
        {
          t ++;
          break;
        }
        t += vt(i[r]);
      }
      return t;
    }
    function vt(n) 
    {
      var t = 0, i = 0, r;
      if(n && ! l(n, "id"))
        for(r = ut(n) || [], l(n, "href") && ! l(n, c.notrack) && t ++;i < r.length;i ++)
          t += vt(r[i]);
      return t;
    }
    function p() 
    {
      nt && clearTimeout(nt), o(a, "scroll", p), setTimeout((function () 
      {
        e(a, "scroll", p);
        var n = a.pageYOffset || w.documentElement.scrollTop;
        c.maxScrollTop < n && (c.maxScrollTop = n), nt = setTimeout((function () 
        {
          nt = null, c.epft = 0, it("getPageScrollTrackingUrl"), 
          c.epft || o(a, "scroll", p);
        }), 
        ci);
      }), 
      hi);
    }
    function d(n, t, i, u, f, e, o) 
    {
      var s = b.trackInfo, h, c, a;
      ! t && n && (t = n.target);
      if(! t || l(t, s.notrack))
        return;
      t.jquery && (t = t[0]), t && n && ! n.customTarget && (n.customTarget = t), 
      s.event = n, 
      h = l(t, "href"), 
      h == "#" && (h = t.href), 
      i = i || l(t, s.piiurl) || h || l(t, "action") || "", 
      u = u || l(t, s.piitxt) || ("FORM" == kt(t) ? s.defaultFormHeadline : r.text(t).replace(/^\s+/, "").replace(/\s+$/, "") || l(t, "alt") || l(t, "title") || dt(t) || ""), 
      f = f || yt(t) || s.defaultModule, 
      c = s.userDynamic.getTrackingParam(s.trackTcm), 
      c && c.length && (f = [f, c, ].join(s.cmSeparator)), 
      e = e || (l(t, "id") ? 1 : lt(t)), 
      a = t.className || l(t, "class"), 
      o = o || (/GT1-(\d+)\b/i.exec(a) ? RegExp.$1 : "") || (/[?&]GT1=(\d+)\b/i.exec(h) ? RegExp.$1 : ""), 
      s.report = {
        destinationUrl : i,
        headline : u,
        contentModule : f,
        contentElement : e,
        campaignId : o,
        sourceIndex : t.sourceIndex || "",
        nodeName : t.nodeName || ""
      }, 
      it("getEventTrackingUrl", 1);
    }
    function oi(n) 
    {
      var i = null, t = ui.exec(n);
      return t && t.length >= 1 && t[1] && (i = t[1]), i;
    }
    function ei(n) 
    {
      var i = null, r, t;
      return n && (n.indexOf(li) == - 1 ? (r = n.substring(0, n.indexOf("#")).toLowerCase(), t = s.location.href.toLowerCase(), 
      (t == r || t.substring(0, t.indexOf("#")) == r) && (i = ft)) : i = oi(n)), 
      i;
    }
    function fi(n, t, i) 
    {
      var r = null;
      return w.createEvent ? (r = w.createEvent("Events"), r.initEvent(t, ! 1, ! 0, i || s, 0, 0, 0, 0, 0, ! 1, ! 1, ! 1, ! 1, 
      0, 
      null)) : w.createEventObject && (r = w.createEventObject(n), r.type = t), 
      r && (r.customTarget = i), 
      r;
    }
    function ht(n) 
    {
      n.preventDefault ? n.preventDefault() : n.returnValue = ! 1;
    }
    function ct(n) 
    {
      var t, i, u, f;
      if(2 == n.button)
        return;
      try
{        n.customTarget && n.customTarget.useMap && (n.customTarget = n.customTarget.document.activeElement, 
        n.target = n.customTarget);}
      catch(e)
{        }

      g = 0, t = r.getTarget(n);
      while(t && ! l(t, "href"))
        t = ot(t);
      t && (i = n, u = t.href, u.length && (n.type == "click" && u.indexOf("#") != - 1 && (f = ei(u), f && (i = fi(n, ft, t))), 
      i.type == ft ? ht(n) : i && ! i.defaultPrevented && i.type == "click" && ni(i, u) && (r.containsClass(t, "skipOOB") || ii(n, u)), 
      d(i, t)));
    }
    function ri(n) 
    {
      var t, i, r;
      n && (t = new Image, i = rt, t.onload = t.onerror = t.onabort = (function () 
      {
        g --, t.onload = t.onerror = t.onabort = null, y && g <= 0 && i && i();
      }), 
      r = n.replace(/&amp;/gi, "&"), 
      g ++, 
      t.src = r);
    }
    function ii(n, t) 
    {
      g = 0, tt = t, ht(n), rt = ti(tt), y = a.setTimeout(rt, c.oobWaitTime), 
      pt = + new Date;
    }
    function ti(n) 
    {
      var t;
      return (function () 
      {
        y && (a.clearTimeout(y), y = 0);
        if(tt == n && ! t)
        {
          t = 1;
          var i = c.event, r = + new Date - pt;
          r < c.oobWaitTime && wt(! 0, c.oobWaitTime - r), i && i.type == "click" && (a.location = tt);
        }
      });
    }
    function ni(n) 
    {
      return n && n.type == "click" && k == 1 || at() ? ! 0 : ! 1;
    }
    function at() 
    {
      var n, t;
      try
{        if(c.client.isIE() || c.enableOOB == 0)
          return k = 0, ! 1;
        return n = ai(), ! n || ! c.bwVerTable ? (k = 0, c.enableOOB = 0, ! 1) : (t = null, n.browser == "mozilla" && c.bwVerTable.mozilla ? t = c.bwVerTable.mozilla : n.browser == "webkit" && c.bwVerTable.webkit && (t = c.bwVerTable.webkit), 
        t && si(n.version, t) ? (k = 1, ! 0) : (k = 0, c.enableOOB = 0, ! 1));}
      catch(i)
{        return k = 0, c.enableOOB = 0, ! 1;}

      return ! 0;
    }
    function st(n) 
    {
      var t = n.split("."), i = parseInt(t[0]) || 0, r = parseInt(t[1]) || 0, 
      u = parseInt(t[2]) || 0;
      return {
        major : i,
        minor : r,
        patch : u
      };
    }
    function si(n, t) 
    {
      var i = st(n), r = st(t);
      return i.major != r.major ? i.major > r.major : i.minor != r.minor ? i.minor > r.minor : i.patch != r.patch ? i.patch > r.patch : ! 0;
    }
    function ai() 
    {
      var i = /(webkit)[ \/]([\w.]+)/, r = /(opera)(?:.*version)?[ \/]([\w.]+)/, 
      u = /(msie) ([\w.]+)/, 
      f = /(mozilla)(?:.*? rv:([\w.]+))?/, 
      n = navigator.userAgent, 
      t;
      return n = n.toLowerCase(), t = i.exec(n) || r.exec(n) || u.exec(n) || n.indexOf("compatible") < 0 && f.exec(n) || [], 
      {
        browser : t[1] || "",
        version : t[2] || "0"
      };
    }
    var l = r.attr, kt = r.name, ot = r.parent, ut = r.children, 
    et = [], 
    v = - 1, 
    gt, 
    a = s, 
    w = h, 
    li = "tevt=", 
    ft = "click_nonnav", 
    ui = /#tevt=([A-Za-z0-9]+_[A-Za-z0-9]+)(;*)/g, 
    g = 0, 
    k = 0, 
    y, 
    tt, 
    c, 
    pt, 
    rt, 
    nt, 
    hi = "500", 
    ci = "200", 
    b = {
      onClick : ct,
      trackEvent : d,
      trackPageFold : p,
      trackPage : (function () 
      {
        it("getPageViewTrackingUrl"), at();
      }),
      register : (function () 
      {
        var t = 0, n;
        while(n = arguments[t ++])
          isNaN(n.samplingRate) && (n.samplingRate = 99), et.push(n);
      }),
      incrementEventNumber : (function () 
      {
        c.userDynamic.eventNumber ++;
      }),
      isSampled : (function (n) 
      {
        return ! (bt() > n);
      }),
      generateUrl : (function (n, t, r, u, f) 
      {
        var s, h, e, o, c = [], l = b.trackInfo;
        u = i({
          
        }, t, u), f = i(! 0, {
          
        }, r, f);
        for(s in f)
          if(l[s])
          {
            h = f[s];
            for(e in h)
              o = l[s][h[e]], gt != o && (typeof o == "function" && (o = o()), u[e] = o);
          }
        for(e in u)
          c.push(encodeURIComponent(e) + "=" + encodeURIComponent(u[e]));
        return n + c.join("&").replace(/%20/g, "+");
      }),
      trackInfo : {
        report : {
          
        }
      },
      extend : (function (n) 
      {
        i(! 0, b.trackInfo, n);
      }),
      form : (function (n) 
      {
        n && n.length || (n = [n, ]);
        var t, i = 0;
        while(t = n[i ++])
          "FORM" == kt(t) && e(t, "submit", d);
      })
    };
    e(w, "click", ct), e(a, "load", d), e(a, "unload", d), 
    e(a, "load", p), 
    e(a, "scroll", p), 
    n.track = b, 
    c = b.trackInfo;
  }))(window.Msn, window.Msn.afire, window.Msn.extend, window.Msn.dom, 
  window.Msn.getCookie, 
  window.Msn.setCookie, 
  window.Msn.bind, 
  window.Msn.unbind, 
  window, 
  document), 
  window.Msn.add("signinstate", (function (n, t, i) 
  {
    function r(n, t) 
    {
      return [n, i(t).length ? "t" : "f", ].join(":");
    }
    n({
      userDynamic : {
        settings : (function () 
        {
          var n = [r("fb", "facebook_userid"), r("tw", "twitter_userid"), ], 
          i = (t.userStatic || {
            
          }).settings;
          return i && n.push(i), n.join(",");
        })
      }
    });
  }))(window.Msn.track.extend, window.Msn.track.trackInfo, 
  window.Msn.getCookie), 
  window.Msn.add("getvisibleslottree", (function (n, t) 
  {
    n({
      userDynamic : {
        defaultSlotTrees : (function () 
        {
          return t.userStatic.defaultSlotTrees;
        })
      }
    });
  }))(window.Msn.track.extend, window.Msn.track.trackInfo), 
  window.Msn.add("trackInfo", (function (n, t, i, r, u, f, e) 
  {
    function c() 
    {
      f.innerWidth ? (s = f.innerWidth, o = f.innerHeight) : (s = e.documentElement.clientWidth, o = e.documentElement.clientHeight);
    }
    function a(n) 
    {
      var t, r = 0;
      if(n)
        for(t = y(n) || [];r < t.length;r ++)
          if(i.containsClass(t[r], p))
            return t[r];
    }
    function b(n) 
    {
      var r, i, u = [], f;
      if(n)
      {
        i = l(n), f = e.getElementById(t.wrapperId);
        while(i && ! (i === f))
          r = a(i), r && u.push(r), i = l(i);
      }
      return u;
    }
    var w = i.attr, l = i.parent, y = i.children, h, v, s, o, 
    p = "trak";
    n({
      notrack : "notrack",
      cmSeparator : ">",
      defaultModule : "body",
      defaultFormHeadline : "[form submit]",
      piitxt : "piitxt",
      piiurl : "piiurl",
      wrapperId : "wrapper",
      defaultConnectionType : "LAN",
      smpCookie : "Sample",
      smpExp : 182,
      MUIDCookie : "MUID",
      spinTimeout : 150,
      trackTcm : "tcm",
      trackAop : "aop",
      curAop : "",
      maxScrollTop : 0,
      event : {
        
      },
      sitePage : {
        
      },
      oobWaitTime : 150,
      enableOOB : 1,
      bwVerTable : {
        webkit : "530.0.0",
        mozilla : "1.9.0"
      },
      client : {
        clientId : (function () 
        {
          return h || h === "" || (h = r(t.MUIDCookie) || t.userStatic.clientRequestId() || ""), 
          h;
        }),
        colorDepth : u.colorDepth,
        connectionType : (function () 
        {
          return t.defaultConnectionType;
        }),
        cookieSupport : (function () 
        {
          return e.cookie ? "Y" : "N";
        }),
        height : (function () 
        {
          return o || c(), o;
        }),
        pageUrl : f.location.href,
        referrer : e.referrer,
        screenResolution : (function () 
        {
          return [u.width, u.height, ].join("x");
        }),
        width : (function () 
        {
          return s || c(), s;
        }),
        timezone : (function () 
        {
          var i = new Date, r = new Date, n, t;
          return r.setMonth(i.getMonth() + 6), n = Math.round(i.getTimezoneOffset() / 60) * - 1, 
          t = Math.round(r.getTimezoneOffset() / 60) * - 1, 
          n < t ? n : t;
        }),
        isIE : (function () 
        {
          return f.ActiveXObject ? ! 0 : ! 1;
        }),
        plusType : f.Msn.Page.cplus && f.Msn.Page.cplus.current ? f.Msn.Page.cplus.current : "default"
      },
      userDynamic : {
        anid : (function () 
        {
          return r("ANON");
        }),
        isHomePage : (function () 
        {
          var n = e.documentElement, t = 0;
          if(n.addBehavior && (v || n.addBehavior("#default#homePage") && (v = 1)))
            try
{              t = n.isHomePage(f.location.href) ? "Y" : "N";}
            catch(i)
{              }

          return t;
        }),
        timeStamp : (function () 
        {
          return + new Date;
        }),
        getJSonFromElement : (function (n) 
        {
          var t;
          if(n)
            return t = w(n, "value"), typeof t != "string" || ! t ? null : /^[\],:{}\s]*$/.test(t.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, "@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, 
            "]").replace(/(?:^|:|,)(?:\s*\[)+/g, "")) ? f.JSON && f.JSON.parse ? f.JSON.parse(t) : new Function("return " + t)() : null;
        }),
        AOP : (function () 
        {
          if(t.curAop != null)
            if(t.curAop == "")
            {
              var n = t.userDynamic.getTrackingParam(t.trackAop);
              n && n.length && (t.curAop = n.join(t.cmSeparator));
            }
          return t.curAop;
        }),
        getTrackingParam : (function (n) 
        {
          var r = t.event;
          if(r)
            return t.userDynamic.getAllHiddenInputFields(i.getTarget(r), n);
        }),
        getAllHiddenInputFields : (function (n, i) 
        {
          var u = [], r, o, f = [], e;
          o = a(n), r = t.userDynamic.getJSonFromElement(o);
          if(r)
          {
            r.taop && i == t.trackAop && u.splice(0, 0, r.taop);
            if(r.tcm && i == t.trackTcm)
              return u.splice(0, 0, r.tcm), u;
          }
          f = b(n);
          if(f && f.length)
            for(e = 0;e < f.length;e ++)
            {
              r = t.userDynamic.getJSonFromElement(f[e]);
              if(r)
              {
                r.taop && i == t.trackAop && u.splice(0, 0, r.taop);
                if(r.tcm && i == t.trackTcm)
                {
                  u.splice(0, 0, r.tcm);
                  break;
                }
              }
            }
          return u;
        }),
        eventNumber : 0
      },
      userStatic : {
        clientRequestId : (function () 
        {
          var n = t.userStatic;
          return n.requestId || (function () 
          {
            for(var n = [], u = "0123456789ABCDEF", r, i = 0;i < 32;i ++)
              n[i] = u.substr(Math.floor(Math.random() * 16), 1);
            return n[12] = "4", n[16] = u.substr(n[16] & 3 | 8, 1), r = n.join(""), 
            t.userStatic.requestId = r, 
            r;
          })();
        })
      }
    });
  }))(window.Msn.track.extend, window.Msn.track.trackInfo, 
  window.Msn.dom, 
  window.Msn.getCookie, 
  screen, 
  window, 
  document), 
  window.Msn.add("trackInfoSilverlight", (function (n, t, i) 
  {
    function u() 
    {
      var u, e, s, o, h, n;
      if(r < 0)
      {
        r = 0;
        try
{          u = t.navigator.plugins;
          if(u && u.length)
            e = u["Silverlight Plug-In"], e && (r = /^\d+\.\d+/.exec(e.description)[0], e = 0);
          else
            if(t.ActiveXObject)
            {
              s = new t.ActiveXObject("AgControl.AgControl"), h = 0;
              if(s)
              {
                r = 1, n = i("object"), n.codeType = "application/x-silverlight-2";
                if(typeof n.IsVersionSupported != "undefined")
                  while(o = f[h ++])
                    if(n.IsVersionSupported(o))
                    {
                      r = o;
                      break;
                    }
                n = 0;
              }
            }}
        catch(c)
{          }

      }
      return r;
    }
    var f = ["5.0", "4.0", "3.0", "2.0", ], r = - 1;
    n({
      client : {
        silverlightEnabled : (function () 
        {
          return u() != 0 ? 1 : 0;
        }),
        silverlightVersion : u()
      }
    });
  }))(window.Msn.track.extend, window, window.Msn.dom.create), 
  window.Msn.add("trackInfoSps", (function (n, t) 
  {
    function i(n) 
    {
      return t.userStatic && t.userStatic[n] || r;
    }
    var r = "default", u = "userGroup", f, e, o;
    n({
      client : {
        flightKey : (function () 
        {
          return o || (o = i(u).split(":")[0] || r);
        }),
        groupAssignment : (function () 
        {
          return e || (e = isNaN(parseInt(i(u).split(":")[1])) ? "P" : "S");
        }),
        optKey : (function () 
        {
          return f || (f = i("optKey"));
        })
      }
    });
  }))(window.Msn.track.extend, window.Msn.track.trackInfo);
  window.Msn.add("generictracking", (function (n, t, i, r) 
  {
    function u(n) 
    {
      this.defaultOpts = i(! 0, {
        
      }, f, n), 
      this.samplingRate = this.defaultOpts.samplingRate;
    }
    var f = {
      base : "",
      samplingRate : 100,
      eventAlias : {
        submit : "click",
        mouseenter : "click",
        mouseleave : "click",
        click_nonnav : "click",
        mouseenter_nav : "click"
      }
    };
    u.prototype = {
      getEventTrackingUrl : (function (n) 
      {
        var e = "", u = this.defaultOpts, i, f;
        return n || (n = (r.event || {
          
        }).type), 
        i = u[n], 
        ! i && u.eventAlias && (i = u[u.eventAlias[n]]), 
        i ? (f = u.base + (i.url ? i.url : ""), t(f, u.common, u.commonMap, i.param, i.paramMap)) : e;
      }),
      getPageViewTrackingUrl : (function () 
      {
        return this.getEventTrackingUrl("impr");
      }),
      getPageScrollTrackingUrl : (function () 
      {
        var u = this.defaultOpts.scroll, c, f, i;
        if(u && u.paramMap && u.paramMap.foldMap)
        {
          var n = "", e = u.paramMap.foldMap, t = document, l = window, 
          o = "scroll", 
          s = r.maxScrollTop, 
          h = l.innerHeight || t.documentElement.clientHeight, 
          a = t.documentElement.scrollHeight;
          for(c in e)
            f = parseInt(c), r.epft = 1, f > s + h || (n != "" && (n = n + ","), n = n + e[f], delete e[f]);
          a > s + h || (r.epft = 0);
          if(n != "")
            return t.createEvent ? (i = t.createEvent("Events"), i.initEvent(o, ! 1, ! 0)) : t.createEventObject && (i = t.createEventObject(), i.type = o), 
            r.curAop = n, 
            r.event = i, 
            this.getEventTrackingUrl(o);
        }
      })
    }, 
    n.generictracking = u;
  }))(window.Msn.track, window.Msn.track.generateUrl, window.Msn.extend, 
  window.Msn.track.trackInfo);
  window.Msn.add("omnitracking", (function (n, t, i, r) 
  {
    function o(n) 
    {
      this.defaultOpts = i(! 0, {
        
      }, s, n), 
      this.samplingRate = this.defaultOpts.samplingRate;
    }
    var u = new Date, f = [u.getDate(), "/", u.getMonth(), "/", u.getFullYear(), " ", u.getHours(), ":", u.getMinutes(), ":", u.getSeconds(), " ", u.getDay(), " ", u.getTimezoneOffset(), ].join(""), 
    s = {
      base : "",
      linkTrack : 1,
      samplingRate : 100,
      common : {
        v : "Y",
        j : "1.3"
      },
      commonMap : {
        client : {
          c : "colorDepth"
        }
      },
      page : {
        v1 : u.getMonth() + 1 + "/" + u.getFullYear(),
        v2 : u.getMonth() + 1 + "/" + u.getDate() + "/" + u.getFullYear(),
        t : f
      },
      pageMap : {
        sitePage : {
          c3 : "pageVersion"
        }
      },
      link : {
        t : f,
        ndh : 1,
        pidt : 1,
        pe : "lnk_o",
        events : "events4"
      },
      linkMap : {
        sitePage : {
          c38 : "pageVersion"
        }
      }
    }, 
    e = {
      click : "click",
      mouseenter : "hover",
      mouseleave : "hover",
      submit : "submit",
      click_nonnav : "click",
      mouseenter_nav : "click"
    };
    o.prototype = {
      getEventTrackingUrl : (function (n) 
      {
        var u = "", i = this.defaultOpts;
        return n || (n = (r.event || {
          
        }).type), 
        i.linkTrack && e[n] && (i.link.c11 = e[n], u = i.base.format(r.userDynamic.timeStamp(), t("", i.common, i.commonMap, i.link, i.linkMap))), 
        u;
      }),
      getPageViewTrackingUrl : (function () 
      {
        var i = "", n = this.defaultOpts;
        return i = n.base.format(r.userDynamic.timeStamp(), t("", n.common, n.commonMap, n.page, n.pageMap));
      })
    }, 
    n.omnitracking = o;
  }))(window.Msn.track, window.Msn.track.generateUrl, window.Msn.extend, 
  window.Msn.track.trackInfo);
  Msn.track.extend({
    sitePage : {
      lang : "en-us",
      siteGroupId : "MSFT",
      pageName : "US HPMSFT3Wdefault",
      pageVersion : "V14",
      omniPageName : "US HPMSFT3Wdefault:MSFT",
      domainId : "340",
      propertyId : "7317",
      propertySpecific : "95101",
      sourceUrl : "http://www.msn.com/defaultwpe3w.aspx",
      pageId : "6901517",
      hops_pageId : "690151710"
    },
    userStatic : {
      signedIn : "False",
      userGroup : "W:default",
      optKey : "",
      requestId : "3fc5834f9dc147fc956568240cfeb17d",
      defaultSlotTrees : "infopane_hops:na,localtg:local,stgsearch:popsrchnew,socialtg:facebook",
      expContext : "msn3:d-msn3",
      topsKey : "",
      topsUserGroup : "C:default"
    },
    spinTimeout : 150,
    oobWaitTime : 150,
    enableOOB : 1
  });
  Msn.track.register(new Msn.track.generictracking({
    base : "http://g.msn.com/_0USHP/32?",
    linkTrack : 1,
    click : {
      paramMap : {
        client : {
          fk : 'flightKey'
        },
        sitePage : {
          di : 'domainId',
          pi : 'propertyId',
          ps : 'propertySpecific',
          su : 'sourceUrl'
        },
        report : {
          ce : 'contentElement',
          cm : 'contentModule',
          hl : 'headline',
          gt1 : 'campaignId',
          du : 'destinationUrl'
        },
        userStatic : {
          rid : 'requestId'
        }
      }
    }
  }), 
  new Msn.track.generictracking({
    base : "http://udc.msn.com/c.gif?",
    linkTrack : 1,
    samplingRate : 99,
    common : {
      parsergroup : 'hops'
    },
    commonMap : {
      event : {
        evt : 'type'
      },
      userStatic : {
        rid : 'clientRequestId',
        exa : 'expContext'
      },
      userDynamic : {
        cts : 'timeStamp',
        expac : 'expCookie'
      },
      client : {
        fk : 'flightKey',
        gp : 'groupAssignment',
        optkey : 'optKey',
        clid : 'clientId',
        cp : 'plusType'
      },
      sitePage : {
        di : 'domainId',
        pi : 'propertyId',
        ps : 'propertySpecific',
        mk : 'lang',
        pn : 'pageName',
        pid : 'pageId',
        su : 'sourceUrl',
        pageid : 'hops_pageId'
      }
    },
    impr : {
      param : {
        evt : 'impr',
        js : '1'
      },
      paramMap : {
        client : {
          rf : 'referrer',
          cu : 'pageUrl',
          sl : 'silverlightEnabled',
          slv : 'silverlightVersion',
          bh : 'height',
          bw : 'width',
          cu : 'pageUrl',
          scr : 'screenResolution',
          sd : 'colorDepth',
          cp : 'plusType'
        },
        sitePage : {
          di : 'domainId',
          pi : 'propertyId',
          ps : 'propertySpecific',
          br : 'siteGroupId',
          mk : 'lang',
          pn : 'pageName',
          pid : 'pageId',
          mv : 'pageVersion',
          su : 'sourceUrl',
          pageid : 'hops_pageId'
        },
        userStatic : {
          pp : 'signedIn'
        },
        userDynamic : {
          "dv.SNLogin" : 'settings',
          "dv.GrpFrMod" : 'defaultSlotTrees',
          hp : 'isHomePage'
        }
      }
    },
    click : {
      paramMap : {
        sitePage : {
          su : 'sourceUrl',
          pn : 'pageName',
          pageid : 'hops_pageId'
        },
        report : {
          ce : 'contentElement',
          cm : 'contentModule',
          hl : 'headline',
          gt1 : 'campaignId',
          du : 'destinationUrl'
        },
        userDynamic : {
          aop : 'AOP'
        },
        client : {
          cu : 'pageUrl',
          cp : 'plusType'
        }
      }
    },
    unload : {
      
    },
    br : {
      paramMap : {
        event : {
          evt : 'type'
        },
        report : {
          ce : 'contentElement',
          hl : 'headline',
          cm : 'contentModule'
        }
      }
    }
  }), 
  new Msn.track.generictracking({
    base : "http://view.atdmt.com/action/MSN_Homepage_Remessaging_111808/nc?",
    linkTrack : 0,
    impr : {
      param : {
        a : '1'
      }
    }
  }), 
  new Msn.track.generictracking({
    base : "http://b.scorecardresearch.com/b?",
    linkTrack : 0,
    impr : {
      param : {
        c1 : '2',
        c2 : '3000001'
      },
      paramMap : {
        client : {
          c7 : 'pageUrl',
          c9 : 'referrer'
        },
        userDynamic : {
          rn : 'timeStamp'
        }
      }
    }
  }), 
  new Msn.track.generictracking({
    base : "http://c.msn.com/c.gif?",
    linkTrack : 0,
    impr : {
      param : {
        udc : 'true'
      },
      paramMap : {
        sitePage : {
          di : 'domainId',
          pi : 'propertyId',
          ps : 'propertySpecific',
          lng : 'lang',
          tp : 'sourceUrl'
        },
        userStatic : {
          rid : 'clientRequestId'
        },
        userDynamic : {
          rnd : 'timeStamp'
        },
        client : {
          rf : 'referrer',
          scr : 'screenResolution'
        }
      }
    }
  }), 
  new window.Msn.track.omnitracking({
    base : "http://msnportal.112.2o7.net/b/ss/msnportalhome/1/H.7-pdv-2/{0}?[AQB]&{1}&[AQE]",
    linkTrack : 1,
    samplingRate : 9,
    common : {
      ns : "msnportalhome"
    },
    commonMap : {
      client : {
        bh : 'height',
        bw : 'width',
        g : 'pageUrl',
        s : 'screenResolution',
        k : 'cookieSupport'
      },
      sitePage : {
        pageName : 'pageName'
      },
      userDynamic : {
        hp : 'isHomePage'
      }
    },
    page : {
      server : "Msn.com",
      cc : "USD",
      c1 : "Portal"
    },
    pageMap : {
      client : {
        c29 : 'pageUrl',
        c42 : 'silverlightVersion',
        ct : 'connectionType',
        r : 'referrer'
      },
      sitePage : {
        c2 : 'lang',
        ch : 'siteGroupId'
      },
      userStatic : {
        c22 : 'signedIn'
      },
      userDynamic : {
        c19 : 'settings',
        c7 : 'defaultSlotTrees',
        c23 : 'anid'
      }
    },
    link : {
      events : "events4"
    },
    linkMap : {
      report : {
        c12 : 'destinationUrl',
        c13 : 'contentModule',
        c15 : 'contentElement',
        c16 : 'headline',
        c18 : 'campaignId',
        oi : 'sourceIndex',
        oid : 'destinationUrl',
        ot : 'nodeName',
        pev1 : 'destinationUrl',
        pev2 : 'headline',
        v11 : 'headline',
        v12 : 'destinationUrl'
      },
      sitePage : {
        pid : 'pageName',
        c17 : 'omniPageName'
      }
    }
  }));
  (function () 
  {
    function a() 
    {
      if(jQuery && Msn.jsClosure)
      {
        Msn.jsClosure();
        Msn.jsClosure = 0;
      }
    }
    Msn.async({
      url : Msn.Page.jsUrl,
      dep : [{
        url : Msn.Page.frameworkUrl,
        cb : a
      }, "$", ],
      p : window,
      cb : a
    });
  })();
  