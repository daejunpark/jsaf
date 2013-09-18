  ;
  window.Modernizr = (function (a, b, c) 
  {
    function v(a) 
    {
      j.cssText = a;
    }
    function w(a, b) 
    {
      return v(prefixes.join(a + ";") + (b || ""));
    }
    function x(a, b) 
    {
      return typeof a === b;
    }
    function y(a, b) 
    {
      return ! ! ~ ("" + a).indexOf(b);
    }
    function z(a, b, d) 
    {
      for(var e in a)
      {
        var f = b[a[e]];
        if(f !== c)
          return d === ! 1 ? a[e] : x(f, "function") ? f.bind(d || b) : f;
      }
      return ! 1;
    }
    var d = "2.6.2", e = {
      
    }, f = ! 0, g = b.documentElement, 
    h = "modernizr"; 
    var i = b.createElement(h);
    var j = i.style, 
    k, 
    l = {
      
    }.toString, 
    m = {
      svg : "http://www.w3.org/2000/svg"
    }, 
    n = {
      
    }, 
    o = {
      
    }, 
    p = {
      
    }, 
    q = [], 
    r = q.slice, 
    s, 
    t = {
      
    }.hasOwnProperty, 
    u;
    ! x(t, "undefined") && ! x(t.call, "undefined") ? u = (function (a, b) 
    {
      return t.call(a, b);
    }) : u = (function (a, b) 
    {
      return b in a && x(a.constructor.prototype[b], "undefined");
    }), 
    Function.prototype.bind || (Function.prototype.bind = (function (b) 
    {
      var c = this;
      if(typeof c != "function")
        throw new TypeError;
      var d = r.call(arguments, 1), e = (function () 
      {
        if(this instanceof e)
        {
          var a = (function () 
          {
            
          });
          a.prototype = c.prototype;
          var f = new a, g = c.apply(f, d.concat(r.call(arguments)));
          return Object(g) === g ? g : f;
        }
        return c.apply(b, d.concat(r.call(arguments)));
      });
      return e;
    })), 
    n.svg = (function () 
    {
      return ! ! b.createElementNS && ! ! b.createElementNS(m.svg, "svg").createSVGRect;
    });
    for(var A in n)
      u(n, A) && (s = A.toLowerCase(), e[s] = n[A](), q.push((e[s] ? "" : "no-") + s));
    return e.addTest = (function (a, b) 
    {
      if(typeof a == "object")
        for(var d in a)
          u(a, d) && e.addTest(d, a[d]);
      else
      {
        a = a.toLowerCase();
        if(e[a] !== c)
          return e;
        b = typeof b == "function" ? b() : b, typeof f != "undefined" && f && (g.className += " " + (b ? "" : "no-") + a), 
        e[a] = b;
      }
      return e;
    }), 
    v(""), 
    i = k = null, 
    (function (a, b) 
    {
      function k(a, b) 
      {
        var c = a.createElement("p"), d = a.getElementsByTagName("head")[0] || a.documentElement;
        return c.innerHTML = "x<style>" + b + "</style>", d.insertBefore(c.lastChild, d.firstChild);
      }
      function l() 
      {
        var a = r.elements;
        return typeof a == "string" ? a.split(" ") : a;
      }
      function m(a) 
      {
        var b = i[a[g]];
        return b || (b = {
          
        }, h ++, a[g] = h, i[h] = b), 
        b;
      }
      function n(a, c, f) 
      {
        c || (c = b);
        if(j)
          return c.createElement(a);
        f || (f = m(c));
        var g;
        return f.cache[a] ? g = f.cache[a].cloneNode() : e.test(a) ? g = (f.cache[a] = f.createElem(a)).cloneNode() : g = f.createElem(a), 
        g.canHaveChildren && ! d.test(a) ? f.frag.appendChild(g) : g;
      }
      function o(a, c) 
      {
        a || (a = b);
        if(j)
          return a.createDocumentFragment();
        c = c || m(a);
        var d = c.frag.cloneNode(), e = 0, f = l(), g = f.length;
        for(;e < g;e ++)
          d.createElement(f[e]);
        return d;
      }
      function p(a, b) 
      {
        b.cache || (b.cache = {
          
        }, b.createElem = a.createElement, 
        b.createFrag = a.createDocumentFragment, 
        b.frag = b.createFrag()), 
        a.createElement = (function (c) 
        {
          return r.shivMethods ? n(c, a, b) : b.createElem(c);
        }), 
        a.createDocumentFragment = Function("h,f", "return function(){var n=f.cloneNode(),c=n.createElement;h.shivMethods&&(" + l().join().replace(/\w+/g, (function (a) 
        {
          return b.createElem(a), b.frag.createElement(a), 'c("' + a + '")';
        })) + ");return n}")(r, b.frag);
      }
      function q(a) 
      {
        a || (a = b);
        var c = m(a);
        return r.shivCSS && ! f && ! c.hasCSS && (c.hasCSS = ! ! k(a, "article,aside,figcaption,figure,footer,header,hgroup,nav,section{display:block}mark{background:#FF0;color:#000}")), 
        j || p(a, c), 
        a;
      }
      var c = a.html5 || {
        
      }, d = /^<|^(?:button|map|select|textarea|object|iframe|option|optgroup)$/i, 
      e = /^(?:a|b|code|div|fieldset|h1|h2|h3|h4|h5|h6|i|label|li|ol|p|q|span|strong|style|table|tbody|td|th|tr|ul)$/i, 
      f, 
      g = "_html5shiv", 
      h = 0, 
      i = {
        
      }, 
      j;
      (function () 
      {
        try
{          var a = b.createElement("a");
          a.innerHTML = "<xyz></xyz>", f = "hidden" in a, j = a.childNodes.length == 1 || (function () 
          {
            b.createElement("a");
            var a = b.createDocumentFragment();
            return typeof a.cloneNode == "undefined" || typeof a.createDocumentFragment == "undefined" || typeof a.createElement == "undefined";
          })();}
        catch(c)
{          f = ! 0, j = ! 0;}

      })();
      var r = {
        elements : c.elements || "abbr article aside audio bdi canvas data datalist details figcaption figure footer header hgroup mark meter nav output progress section summary time video",
        shivCSS : c.shivCSS !== ! 1,
        supportsUnknownElements : j,
        shivMethods : c.shivMethods !== ! 1,
        type : "default",
        shivDocument : q,
        createElement : n,
        createDocumentFragment : o
      };
      a.html5 = r, q(b);
    })(this, b), 
    e._version = d, 
    g.className = g.className.replace(/(^|\s)no-js(\s|$)/, "$1$2") + (f ? " js " + q.join(" ") : ""), 
    e;
  })(this, this.document), 
  (function (a, b, c) 
  {
    function d(a) 
    {
      return "[object Function]" == o.call(a);
    }
    function e(a) 
    {
      return "string" == typeof a;
    }
    function f() 
    {
      
    }
    function g(a) 
    {
      return ! a || "loaded" == a || "complete" == a || "uninitialized" == a;
    }
    function h() 
    {
      var a = p.shift();
      q = 1, a ? a.t ? m((function () 
      {
        ("c" == a.t ? B.injectCss : B.injectJs)(a.s, 0, a.a, a.x, a.e, 1);
      }), 
      0) : (a(), h()) : q = 0;
    }
    function i(a, c, d, e, f, i, j) 
    {
      function k(b) 
      {
        if(! o && g(l.readyState) && (u.r = o = 1, ! q && h(), l.onload = l.onreadystatechange = null, 
        b))
        {
          "img" != a && m((function () 
          {
            t.removeChild(l);
          }), 
          50);
          for(var d in y[c])
            y[c].hasOwnProperty(d) && y[c][d].onload();
        }
      }
      var j = j || B.errorTimeout, l = b.createElement(a), o = 0, 
      r = 0, 
      u = {
        t : d,
        s : c,
        e : f,
        a : i,
        x : j
      };
      1 === y[c] && (r = 1, y[c] = []), "object" == a ? l.data = c : (l.src = c, l.type = a), 
      l.width = l.height = "0", 
      l.onerror = l.onload = l.onreadystatechange = (function () 
      {
        k.call(this, r);
      }), 
      p.splice(e, 0, u), 
      "img" != a && (r || 2 === y[c] ? (t.insertBefore(l, s ? null : n), m(k, j)) : y[c].push(l));
    }
    function j(a, b, c, d, f) 
    {
      return q = 0, b = b || "j", e(a) ? i("c" == b ? v : u, a, b, this.i ++, c, d, f) : (p.splice(this.i ++, 0, a), 1 == p.length && h()), 
      this;
    }
    function k() 
    {
      var a = B;
      return a.loader = {
        load : j,
        i : 0
      }, 
      a;
    }
    var l = b.documentElement, m = a.setTimeout, n = b.getElementsByTagName("script")[0], 
    o = {
      
    }.toString, 
    p = [], 
    q = 0, 
    r = "MozAppearance" in l.style, 
    s = r && ! ! b.createRange().compareNode, 
    t = s ? l : n.parentNode, 
    l = a.opera && "[object Opera]" == o.call(a.opera), 
    l = ! ! b.attachEvent && ! l, 
    u = r ? "object" : l ? "script" : "img", 
    v = l ? "script" : u, 
    w = Array.isArray || (function (a) 
    {
      return "[object Array]" == o.call(a);
    }), 
    x = [], 
    y = {
      
    }, 
    z = {
      timeout : (function (a, b) 
      {
        return b.length && (a.timeout = b[0]), a;
      })
    }, 
    A, 
    B;
    B = (function (a) 
    {
      function b(a) 
      {
        var a = a.split("!"), b = x.length, c = a.pop(), d = a.length, 
        c = {
          url : c,
          origUrl : c,
          prefixes : a
        }, 
        e, 
        f, 
        g;
        for(f = 0;f < d;f ++)
          g = a[f].split("="), (e = z[g.shift()]) && (c = e(c, g));
        for(f = 0;f < b;f ++)
          c = x[f](c);
        return c;
      }
      function g(a, e, f, g, h) 
      {
        var i = b(a), j = i.autoCallback;
        i.url.split(".").pop().split("?").shift(), i.bypass || (e && (e = d(e) ? e : e[a] || e[g] || e[a.split("/").pop().split("?")[0]]), 
        i.instead ? i.instead(a, e, f, g, h) : (y[i.url] ? i.noexec = ! 0 : y[i.url] = 1, f.load(i.url, i.forceCSS || ! i.forceJS && "css" == i.url.split(".").pop().split("?").shift() ? "c" : c, 
        i.noexec, 
        i.attrs, 
        i.timeout), 
        (d(e) || d(j)) && f.load((function () 
        {
          k(), e && e(i.origUrl, h, g), j && j(i.origUrl, h, g), 
          y[i.url] = 2;
        }))));
      }
      function h(a, b) 
      {
        function c(a, c) 
        {
          if(a)
          {
            if(e(a))
              c || (j = (function () 
              {
                var a = [].slice.call(arguments);
                k.apply(this, a), l();
              })), 
              g(a, j, b, 0, h);
            else
              if(Object(a) === a)
                for(n in m = (function () 
                {
                  var b = 0, c;
                  for(c in a)
                    a.hasOwnProperty(c) && b ++;
                  return b;
                })(), 
                a)
                  a.hasOwnProperty(n) && (! c && ! -- m && (d(j) ? j = (function () 
                  {
                    var a = [].slice.call(arguments);
                    k.apply(this, a), l();
                  }) : j[n] = (function (a) 
                  {
                    return (function () 
                    {
                      var b = [].slice.call(arguments);
                      a && a.apply(this, b), l();
                    });
                  })(k[n])), 
                  g(a[n], j, b, n, h));
          }
          else
            ! c && l();
        }
        var h = ! ! a.test, i = a.load || a.both, j = a.callback || f, 
        k = j, 
        l = a.complete || f, 
        m, 
        n;
        c(h ? a.yep : a.nope, ! ! i), i && c(i);
      }
      var i, j, l = this.yepnope.loader;
      if(e(a))
        g(a, 0, l, 0);
      else
        if(w(a))
          for(i = 0;i < a.length;i ++)
            j = a[i], e(j) ? g(j, 0, l, 0) : w(j) ? B(j) : Object(j) === j && h(j, l);
        else
          Object(a) === a && h(a, l);
    }), 
    B.addPrefix = (function (a, b) 
    {
      z[a] = b;
    }), 
    B.addFilter = (function (a) 
    {
      x.push(a);
    }), 
    B.errorTimeout = 10000, 
    null == b.readyState && b.addEventListener && (b.readyState = "loading", b.addEventListener("DOMContentLoaded", A = (function () 
    {
      b.removeEventListener("DOMContentLoaded", A, 0), b.readyState = "complete";
    }), 
    0)), 
    a.yepnope = k(), 
    a.yepnope.executeStack = h, 
    a.yepnope.injectJs = (function (a, c, d, e, i, j) 
    {
      var k = b.createElement("script"), l, o, e = e || B.errorTimeout;
      k.src = a;
      for(o in d)
        k.setAttribute(o, d[o]);
      c = j ? h : c || f, k.onreadystatechange = k.onload = (function () 
      {
        ! l && g(k.readyState) && (l = 1, c(), k.onload = k.onreadystatechange = null);
      }), 
      m((function () 
      {
        l || (l = 1, c(1));
      }), 
      e), 
      i ? k.onload() : n.parentNode.insertBefore(k, n);
    }), 
    a.yepnope.injectCss = (function (a, c, d, e, g, i) 
    {
      var e = b.createElement("link"), j, c = i ? h : c || f;
      e.href = a, e.rel = "stylesheet", e.type = "text/css";
      for(j in d)
        e.setAttribute(j, d[j]);
      g || (n.parentNode.insertBefore(e, n), m(c, 0));
    });
  })(this, document), 
  Modernizr.load = (function () 
  {
    yepnope.apply(window, [].slice.call(arguments, 0));
  });
  (function (w, d) 
  {
    var $ = (function (el) 
    {
      return d.getElementById(el);
    }), 
    on = (function (evnt, el, func) 
    {
      if(el.addEventListener)
        el.addEventListener(evnt, func, false);
      else
        if(el.attachEvent)
          el.attachEvent("on" + evnt, func);
    }), 
    off = (function (evnt, el, func) 
    {
      if(el.removeEventListener)
        el.removeEventListener(evnt, func, false);
      else
        if(el.detachEvent)
          el.detachEvent("on" + evnt, func);
    }), 
    wpcom = {
      backgrounds : ['a', 'b', 'c', 'd', 'e', 'f', ],
      init : (function () 
      {
        on('load', w, wpcom.setBackgroundClass);
        on('load', w, wpcom.addWpcomStatsGif);
        on('load', w, wpcom.addQuantcastGif);
        on('load', w, wpcom.addGoogleAnalytics);
        on('load', w, wpcom.bind);
      }),
      bind : (function () 
      {
        km.init();
        on('click', $('home-intro'), wpcom.showVideo);
      }),
      setBackgroundClass : (function () 
      {
        d.body.className += ' pic-' + wpcom.backgrounds[Math.floor(Math.random() * wpcom.backgrounds.length)];
      }),
      showVideo : (function () 
      {
        var useHTML5 = (null != navigator.userAgent.match(/(Mobile|Android|CriOS|webOS|iPhone|iPad|iPod|BlackBerry|Windows Phone|AppleWebKit)/i)) ? true : false;
        var vidWidth = ($('home-intro').offsetWidth - 26), vidHeight = Math.round(vidWidth * .56);
        if(vidHeight + 17 > $('home-intro').offsetHeight)
        {
          var vidHeight = $('home-intro').offsetHeight - 34, vidWidth = Math.round(vidHeight * 1.7857);
        }
        var vidHTML = '<embed id="wpcom-vid" type="application/x-shockwave-flash" src="http://s0.videopress.com/player.swf?v=1.03" width="' + vidWidth + '" height="' + vidHeight + '" wmode="direct" seamlesstabbing="true" allowfullscreen="true" allowscriptaccess="always" overstretch="true" autoplay="true" flashvars="guid=BnChgpLD&amp;isDynamicSeeking=true&autoPlay=true"></embed>';
        if(useHTML5)
          vidHTML = '<video id="wpcom-vid" width="' + vidWidth + '" height="' + vidHeight + '"  controls autoplay> \t\t\t\t\t\t\t<source src="http://videos.videopress.com/BnChgpLD/homepagevideomk2.mp4"> \t\t\t\t\t\t   \t<source src="http://videos.videopress.com/BnChgpLD/homepagevideomk2_fmt1.ogv"> \t\t\t\t\t\t   </video>';
        $('home-intro').innerHTML += vidHTML;
        $('wpcom-vid').className = 'show';
        $('home-intro').className += ' hide';
        off('click', $('home-intro'), wpcom.showVideo);
      }),
      addWpcomStatsGif : (function () 
      {
        d.body.innerHTML += '<img id="wpcom-stats" src="//stats.wordpress.com/g.gif?host=wordpress.com&rand=' + Math.random() + '&blog=1&v=wpcom&tz=0&user_id=0&subd=wordpress.com">';
      }),
      addQuantcastGif : (function () 
      {
        d.body.innerHTML += '<img src="//pixel.quantserve.com/pixel/p-18-mFEk4J448M.gif?labels=%2Clanguage.en%2Ctype.wpcom%2Cwp.homepage" height="1" width="1">';
      }),
      addGoogleAnalytics : (function () 
      {
        w._gaq = w._gaq || [];
        w._gaq.push(['pageload._setAccount', 'UA-10673494-4', ]);
        w._gaq.push(['pageload._setDomainName', '.wordpress.com', ]);
        w._gaq.push(['pageload._setSiteSpeedSampleRate', 1, ]);
        w._gaq.push(['pageload._trackPageview', ]);
        var ga = d.createElement('script');
        ga.async = true;
        ga.src = '//google-analytics.com/ga.js';
        d.body.appendChild(ga);
      })
    }, 
    km = {
      init : (function () 
      {
        km.addJS();
        w._kmq = w._kmq || [];
        km.eventViewedHomePage();
        on('click', $('home-intro'), km.eventVideoClicked);
      }),
      addJS : (function () 
      {
        var kjs = d.createElement('script');
        kjs.async = true;
        kjs.src = '//i.kissmetrics.com/i.js';
        d.body.appendChild(kjs);
        var cfjs = d.createElement('script');
        cfjs.async = true;
        cfjs.src = '//doug1izaerwt3.cloudfront.net/39e3e583aca28fab00c85ad9019b51d9f073fc6d.1.js';
        d.body.appendChild(cfjs);
      }),
      eventViewedHomePage : (function () 
      {
        var props = {
          
        };
        props['Viewed_Homepage | Language'] = 'en';
        props['Viewed_Homepage | Homepage_Version_20130405'] = 'existing';
        w._kmq.push(['record', 'Viewed_Homepage', props, ]);
      }),
      eventVideoClicked : (function () 
      {
        var props = {
          
        };
        props['Viewed_Homepage | Language'] = 'en';
        props['Viewed_Homepage | Homepage_Version_20130405'] = 'existing';
        w._kmq.push(['record', 'Homepage_Clicked_Video', props, ]);
        off('click', $('home-intro'), km.eventVideoClicked);
      })
    };
    wpcom.init();
  })(window, document);
  
