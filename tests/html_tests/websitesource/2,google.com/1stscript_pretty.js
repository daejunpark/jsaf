  (function () 
  {
    window.google = {
      kEI : "ykHWUfP5Ko7VkQWhoYHoDA",
      getEI : (function (a) 
      {
        for(var b;a && (! a.getAttribute || ! (b = a.getAttribute("eid")));)
          a = a.parentNode;
        return b || google.kEI;
      }),
      https : (function () 
      {
        return "https:" == window.location.protocol;
      }),
      kEXPI : "25657,4000116,4001076,4002693,4003242,4004320,4004334,4004844,4004949,4004953,4005865,4005875,4006038,4006426,4006442,4006466,4006727,4007055,4007080,4007117,4007158,4007173,4007229,4007244,4007463,4007533,4007566,4007638,4007661,4007668,4007762,4007779,4007798,4007804,4007818,4007874,4007893,4007917,4007943,4007972,4008028,4008079,4008115,4008133,4008191,4008297,4008337,4008396,4008403,4008409",
      kCSI : {
        e : "25657,4000116,4001076,4002693,4003242,4004320,4004334,4004844,4004949,4004953,4005865,4005875,4006038,4006426,4006442,4006466,4006727,4007055,4007080,4007117,4007158,4007173,4007229,4007244,4007463,4007533,4007566,4007638,4007661,4007668,4007762,4007779,4007798,4007804,4007818,4007874,4007893,4007917,4007943,4007972,4008028,4008079,4008115,4008133,4008191,4008297,4008337,4008396,4008403,4008409",
        ei : "ykHWUfP5Ko7VkQWhoYHoDA"
      },
      authuser : 0,
      ml : (function () 
      {
        
      }),
      kHL : "ko",
      time : (function () 
      {
        return (new Date).getTime();
      }),
      log : (function (a, b, c, l, k) 
      {
        var d = new Image, f = google.lc, e = google.li, g = "", 
        h = "gen_204";
        k && (h = k);
        d.onerror = d.onload = d.onabort = (function () 
        {
          delete f[e];
        });
        f[e] = d;
        c || - 1 != b.search("&ei=") || (g = "&ei=" + google.getEI(l));
        c = c || "/" + h + "?atyp=i&ct=" + a + "&cad=" + b + g + "&zx=" + google.time();
        a = /^http:/i;
        a.test(c) && google.https() ? (google.ml(Error("GLMM"), ! 1, {
          src : c
        }), 
        delete f[e]) : (d.src = c, google.li = e + 1);
      }),
      lc : [],
      li : 0,
      j : {
        en : 1,
        b : ! ! location.hash && ! ! location.hash.match("[#&]((q|fp)=|tbs=simg|tbs=sbi)"),
        bv : 21,
        cf : "",
        pm : "p",
        u : "c9c918f0"
      },
      Toolbelt : {
        
      },
      y : {
        
      },
      x : (function (a, b) 
      {
        google.y[a.id] = [a, b, ];
        return ! 1;
      }),
      load : (function (a, b) 
      {
        google.x({
          id : a + m ++
        }, (function () 
        {
          google.load(a, b);
        }));
      })
    };
    var m = 0;
    window.onpopstate = (function () 
    {
      google.j.psc = 1;
    });
    window.chrome || (window.chrome = {
      
    });
    window.chrome.sv = 2.00;
    window.chrome.searchBox || (window.chrome.searchBox = {
      
    });
    var n = (function () 
    {
      google.x({
        id : "psyapi"
      }, (function () 
      {
        var a = encodeURIComponent(window.chrome.searchBox.value);
        google.nav.search({
          q : a,
          sourceid : "chrome-psyapi2"
        });
      }));
    });
    window.chrome.searchBox.onsubmit = n;
  })();
  (function () 
  {
    google.sn = "webhp";
    google.timers = {
      
    };
    google.startTick = (function (a, b) 
    {
      google.timers[a] = {
        t : {
          start : google.time()
        },
        bfr : ! ! b
      };
    });
    google.tick = (function (a, b, g) 
    {
      google.timers[a] || google.startTick(a);
      google.timers[a].t[b] = g || google.time();
    });
    google.startTick("load", ! 0);
    try
{      google.pt = window.external && window.external.pageT;}
    catch(d)
{      }

  })();
  (function () 
  {
    'use strict';
    var c = this, g = Date.now || (function () 
    {
      return + new Date;
    });
    var m = (function (d, k) 
    {
      return (function (a) 
      {
        a || (a = window.event);
        return k.call(d, a);
      });
    }), 
    t = "undefined" != typeof navigator && /Macintosh/.test(navigator.userAgent), 
    u = "undefined" != typeof navigator && ! /Opera/.test(navigator.userAgent) && /WebKit/.test(navigator.userAgent), 
    v = "undefined" != typeof navigator && ! /Opera|WebKit/.test(navigator.userAgent) && /Gecko/.test(navigator.product), 
    x = v ? "keypress" : "keydown";
    var y = (function () 
    {
      this.g = [];
      this.a = [];
      this.e = {
        
      };
      this.d = null;
      this.c = [];
    }), 
    z = "undefined" != typeof navigator && /iPhone|iPad|iPod/.test(navigator.userAgent), 
    A = /\s*;\s*/, 
    B = (function (d, k) 
    {
      return (function (a) 
      {
        var b;
        i : {
          b = k;
          if("click" == b && (t && a.metaKey || ! t && a.ctrlKey || 2 == a.which || null == a.which && 4 == a.button || a.shiftKey))
            b = "clickmod";
          else
          {
            var e = a.which || a.keyCode || a.key, f;
            if(f = a.type == x)
            {
              f = a.srcElement || a.target;
              var n = f.tagName.toUpperCase();
              f = ! ("TEXTAREA" == n || "BUTTON" == n || "INPUT" == n || "A" == n || f.isContentEditable) && ! (a.ctrlKey || a.shiftKey || a.altKey || a.metaKey) && (13 == e || 32 == e || u && 3 == e);
            }
            f && (b = "clickkey");
          }
          for(f = e = a.srcElement || a.target;f && f != this;f = f.parentNode)
          {
            var n = f, l;
            var h = n;
            l = b;
            var p = h.__jsaction;
            if(! p)
            {
              p = {
                
              };
              h.__jsaction = p;
              var r = null;
              "getAttribute" in h && (r = h.getAttribute("jsaction"));
              if(h = r)
                for(var h = h.split(A), r = 0, P = h ? h.length : 0;r < P;r ++)
                {
                  var q = h[r];
                  if(q)
                  {
                    var w = q.indexOf(":"), H = - 1 != w, Q = H ? q.substr(0, w).replace(/^\s+/, "").replace(/\s+$/, "") : "click", 
                    q = H ? q.substr(w + 1).replace(/^\s+/, "").replace(/\s+$/, "") : q;
                    p[Q] = q;
                  }
                }
            }
            h = void 0;
            "clickkey" == l ? l = "click" : "click" == l && (h = p.click || p.clickonly);
            l = (h = h || p[l]) ? {
              h : l,
              action : h
            } : void 0;
            if(l)
            {
              b = {
                eventType : l.h,
                event : a,
                targetElement : e,
                action : l.action,
                actionElement : n
              };
              break i;
            }
          }
          b = null;
        }
        if(b)
          if(a.stopPropagation ? a.stopPropagation() : a.cancelBubble = ! 0, 
          "A" == b.actionElement.tagName && "click" == k && (a.preventDefault ? a.preventDefault() : a.returnValue = ! 1), 
          d.d)
            d.d(b);
          else
          {
            var s;
            if((e = c.document) && ! e.createEvent && e.createEventObject)
              try
{                s = e.createEventObject(a);}
              catch(U)
{                s = a;}

            else
              s = a;
            v && (s.timeStamp = g());
            b.event = s;
            d.c.push(b);
          }
      });
    }), 
    C = (function (d, k) 
    {
      return (function (a) 
      {
        var b = d, e = k, f = ! 1;
        if(a.addEventListener)
        {
          if("focus" == b || "blur" == b)
            f = ! 0;
          a.addEventListener(b, e, f);
        }
        else
          a.attachEvent && ("focus" == b ? b = "focusin" : "blur" == b && (b = "focusout"), 
          e = m(a, e), 
          a.attachEvent("on" + b, e));
        return {
          h : b,
          i : e,
          capture : f
        };
      });
    }), 
    D = (function (d, k) 
    {
      if(! d.e.hasOwnProperty(k))
      {
        var a = B(d, k), b = C(k, a);
        d.e[k] = a;
        d.g.push(b);
        for(a = 0;a < d.a.length;++ a)
        {
          var e = d.a[a];
          e.c.push(b.call(null, e.a));
        }
        "click" == k && D(d, x);
      }
    });
    y.prototype.i = (function (d) 
    {
      return this.e[d];
    });
    var F = (function () 
    {
      this.a = E;
      this.c = [];
    });
    var G = new y, E = window.document.documentElement, I;
    i : {
      for(var J = 0;J < G.a.length;J ++)
      {
        for(var K = G.a[J].a, L = E;K != L && L.parentNode;)
          L = L.parentNode;
        if(K == L)
        {
          I = ! 0;
          break i;
        }
      }
      I = ! 1;
    }
    if(! I)
    {
      z && (E.style.cursor = "pointer");
      for(var M = new F, N = 0;N < G.g.length;++ N)
        M.c.push(G.g[N].call(null, M.a));
      G.a.push(M);
    }
    D(G, "click");
    D(G, "focus");
    D(G, "focusin");
    D(G, "blur");
    D(G, "focusout");
    D(G, "change");
    D(G, "keydown");
    D(G, "keypress");
    D(G, "mousedown");
    D(G, "mouseout");
    D(G, "mouseover");
    D(G, "mouseup");
    D(G, "touchstart");
    D(G, "touchmove");
    D(G, "touchend");
    var O = (function (d) 
    {
      G.d = d;
      G.c && (0 < G.c.length && d(G.c), G.c = null);
    }), 
    R = ["google", "jsad", ], 
    S = c;
    R[0] in S || ! S.execScript || S.execScript("var " + R[0]);
    for(var T;R.length && (T = R.shift());)
      R.length || void 0 === O ? S = S[T] ? S[T] : S[T] = {
        
      } : S[T] = O;
  }).call(window);
  google.arwt = (function (a) 
  {
    a.href = document.getElementById(a.id.substring(1)).href;
    return ! 0;
  });
  