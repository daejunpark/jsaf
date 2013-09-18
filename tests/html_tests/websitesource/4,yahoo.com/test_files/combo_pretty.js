  (function () 
  {
    if(! window.YFPAD)
    {
      window.YFPAD = {
        
      };
    }
    var a = [];
    YFPAD.debug = (function () 
    {
      
    });
    YFPAD.ff_debug = (function () 
    {
      if(window.chocsickle)
      {
        chocsickle.add.apply(window, arguments);
      }
    });
    YFPAD.lang = {
      bind : (function (f, b, e) 
      {
        var d, c;
        if(! Function.prototype.bind)
        {
          d = (function (g) 
          {
            if(b)
            {
              e.apply(f, [g, b, ]);
            }
            else
            {
              e.apply(f, arguments);
            }
          });
        }
        else
        {
          d = e.bind(f);
        }
        return d;
      }),
      mix : (function (d, c) 
      {
        var b;
        for(b in c)
        {
          if(c.hasOwnProperty(b))
          {
            d[b] = c[b];
          }
        }
        return d;
      }),
      delay : (function (e, g, d, b) 
      {
        var c = YFPAD.lang.bind(d, b, e), f = setTimeout(c, g);
        return f;
      }),
      createHash : (function (c) 
      {
        var d = [], e, b = encodeURIComponent;
        for(e in c)
        {
          if(c.hasOwnProperty(e))
          {
            d.push(b(e) + "=" + b(String(c[e])));
          }
        }
        return d.join("&");
      }),
      parseHash : (function (f) 
      {
        if(f === null)
        {
          return false;
        }
        var e = f.split("&"), h = null, d = {
          
        }, 
        c = decodeURIComponent, 
        g = 0, 
        b = 0;
        if(f.length)
        {
          for(g, b = e.length;g < b;g ++)
          {
            h = e[g].split("=");
            d[c(h[0])] = c(h[1]);
          }
        }
        return d;
      })
    };
    YFPAD.dom = {
      byId : (function (b) 
      {
        return document.getElementById(b);
      }),
      addListener : (function (f, e, b, d) 
      {
        var c = e;
        if(d)
        {
          c = YFPAD.lang.bind(d, [], e);
        }
        if(window.addEventListener)
        {
          b.addEventListener(f, c, 0);
        }
        else
        {
          if(window.attachEvent)
          {
            b.attachEvent("on" + f, c);
          }
        }
        a.push({
          element : b,
          "function" : e,
          scope : d,
          event : f,
          bound : c
        });
      }),
      removeListenerHelper : (function (d, c, b) 
      {
        if(window.removeEventListener)
        {
          b.removeEventListener(d, c, 0);
        }
        else
        {
          if(window.detachEvent)
          {
            b.detachEvent("on" + d, c);
          }
        }
      }),
      removeListener : (function (e, d, c) 
      {
        for(var f = 0, b = a.length;f < b;f ++)
        {
          if(a[f] && a[f].element === c && a[f].event === e)
          {
            if(a[f]["function"] === d || a[f].bound === d)
            {
              YFPAD.dom.removeListenerHelper(a[f].event, a[f].bound, a[f].element);
              a[f] = null;
            }
          }
        }
      }),
      purgeListeners : (function (c) 
      {
        var d = YFPAD.dom.listeners;
        for(var e = 0, b = a.length;e < b;e ++)
        {
          if(a[e] && a[e].element === c)
          {
            YFPAD.dom.removeListenerHelper(a[e].event, a[e].bound, a[e].element);
            a[e] = null;
          }
        }
      }),
      preventDefault : (function (b) 
      {
        if(b.preventDefault)
        {
          b.preventDefault();
        }
        else
        {
          b.returnValue = false;
        }
      }),
      hasClass : (function (b, c) 
      {
        var d = " ", e = b.className.toString().replace(/\s+/g, d);
        return c && (d + e + d).indexOf(d + c + d) > - 1;
      }),
      byClassName : (function (f, h) 
      {
        var g = h || document, c = [], d, e, b;
        if(g.getElementsByClassName)
        {
          c = g.getElementsByClassName(f);
        }
        else
        {
          if(g && g.getElementsByTagName)
          {
            e = g.getElementsByTagName("*");
            if(! e)
            {
              return [];
            }
            for(d = 0, b = e.length;d < b;++ d)
            {
              if(YFPAD.dom.hasClass(e[d], f))
              {
                c[c.length] = e[d];
              }
            }
          }
        }
        return c;
      }),
      nodeCreate : (function (d) 
      {
        var e = document.createElement("DIV"), c = document.createDocumentFragment(), 
        b;
        e.innerHTML = d;
        b = e.childNodes;
        while(b[0])
        {
          c.appendChild(b[0].parentNode.removeChild(b[0]));
        }
        return c;
      }),
      getParent : (function (b, c) 
      {
        if(YFPAD.dom.hasClass(c, b))
        {
          return c;
        }
        else
        {
          if(c.parentNode)
          {
            return YFPAD.dom.getParent(b, c.parentNode);
          }
        }
        return null;
      }),
      clear : (function (b) 
      {
        if(b)
        {
          b.innerHTML = "";
        }
      }),
      visible : (function (c, b) 
      {
        if(c)
        {
          if(b)
          {
            c.style.visibility = "visible";
          }
          else
          {
            c.style.visibility = "hidden";
          }
        }
      }),
      display : (function (c, b) 
      {
        if(c)
        {
          if(b)
          {
            c.style.display = b;
          }
          else
          {
            c.style.display = "";
          }
        }
      }),
      isHidden : (function (b) 
      {
        return (b && b.style.display !== "none" && b.style.display !== "");
      }),
      isVisible : (function (b) 
      {
        return (b && b.style.visibility === "visible");
      })
    };
    YFPAD.Base = (function (c) 
    {
      var e = YFPAD.dom, d = YFPAD.lang, b = YFPAD.util, f;
      f = (function (g) 
      {
        d.mix(this, g.extend);
        this.config = d.mix(this.config, g.conf);
        YFPAD.ff_debug("ad_config", this.config);
        this.start();
      });
      f.prototype = {
        interaction_track : (function (i, q, o) 
        {
          var h = o ? o : "1", j = this.config, m = j.nspace, l = j.beap, 
          p = j.rd, 
          g = "", 
          k = Math.random();
          if(j.apt)
          {
            g = l[0] + "seq$" + i + ",label$" + q + ",type$click,time$" + k + l[1];
          }
          else
          {
            g = p + m + i + "/id=" + q + "/" + k + "/*" + h;
          }
          b.pixelTrack(g);
        }),
        detect : (function () 
        {
          var h = this.config, g = document.body, i = YFPAD.ua;
          h.cap = i.isIE;
          h.fv = i.isFlash;
          if(h.cap > 0)
          {
            g.style.behavior = "url(#default#clientCaps)";
            h.lan = (g.connectionType === "lan") ? 1 : 0;
          }
          else
          {
            h.ncap = i.isFireFox;
          }
          h.flash = (i.isFlash <= h.nfv);
          this.config = h;
        }),
        init : (function () 
        {
          
        }),
        start : (function () 
        {
          var h, g = this.config.pixels, j = (function () 
          {
            if(typeof (g) === "object")
            {
              for(h = 0;h < g.length;h ++)
              {
                YFPAD.util.pixelTrack(g[h]);
              }
            }
          });
          if(this.config.apt || (this.config.r0.indexOf("/X=3/") !== - 1 || this.config.r0.indexOf("/X=6/") !== - 1))
          {
            this.config.apt = true;
            this.config.clickTags.apt = true;
          }
          else
          {
            if(this.config.rd === "")
            {
              YFPAD.debug("config.rd is blank");
            }
            else
            {
              if(/\/$/.test(this.config.rd))
              {
                YFPAD.debug("config.rd has a trailing slash: " + this.config.rd);
              }
            }
            this.config.apt = false;
            this.config.clickTags.apt = "";
          }
          this.detect();
          this.init();
          e.addListener("load", this.ad_init, window, this);
          e.addListener("load", j, window);
        }),
        config : {
          domain : "yahoo.com",
          expires : 172800000,
          key1 : "CRZY" + (new Date()).getDay(),
          rd : "",
          z1 : "",
          nspace : "",
          beap : [],
          flash : false,
          apt : false,
          fv : 0,
          nv : 0,
          lan : 0,
          cap : 0,
          ncap : 0,
          done : 0,
          auto : 0,
          narrow : YFPAD.ua.isNarrow,
          flashvars : ""
        }
      };
      return new f(c);
    });
  })();
  (function (a) 
  {
    function e(h) 
    {
      var g = h.match(/[0-9]+.?[0-9]*/);
      if(g && g[0])
      {
        return parseFloat(g[0]);
      }
      return 0;
    }
    var d = a.className.toString(), c = navigator.userAgent.toString(), 
    f = YFPAD.dom, 
    b = {
      html : a,
      os : (function () 
      {
        var g = "other";
        if((/windows|win32/i).test(c))
        {
          g = "windows";
        }
        else
        {
          if((/macintosh/i).test(c))
          {
            g = "macintosh";
          }
          else
          {
            if((/iphone|ipod/i).test(c))
            {
              g = "ios";
            }
            else
            {
              if((/linux/i).test(c))
              {
                g = "linux";
              }
            }
          }
        }
        return g;
      })(),
      isIE : (function () 
      {
        if(f.hasClass(a, "ua-ie"))
        {
          return d.match(/ua-ie(\d+)/)[1] || 1;
        }
        return 0;
      })(),
      isFireFox : (function () 
      {
        if(f.hasClass(a, "ua-ff"))
        {
          return d.match(/ua-ff(\d+)/)[1] || 1;
        }
        return 0;
      })(),
      isSafari : (function () 
      {
        var g = c.match(/Version\/([0-9.]+)\s?Safari/);
        if(g && g[1])
        {
          return e(g[1]);
        }
        return 0;
      })(),
      isChrome : (function () 
      {
        var g = c.match(/Chrome\/([0-9.]+)/);
        if(g && g[1])
        {
          return parseInt(g[1]);
        }
        return 0;
      })(),
      isWebKit : (function () 
      {
        if(f.hasClass(a, "ua-wk"))
        {
          return d.match(/ua-wk(\d+)/)[1] || 1;
        }
        return 0;
      })(),
      isFlash : (function () 
      {
        var g = 0;
        if(/flash-[0-9]+/.test(d))
        {
          g = parseInt(d.match(/flash-([0-9]+)/)[1], 10) || 0;
        }
        return g;
      })(),
      isNarrow : (function () 
      {
        var g = f.hasClass(a, "lite-page") ? true : false;
        return g;
      })()
    };
    YFPAD.ua = b;
  })(document.documentElement);
  (function () 
  {
    if(! window.YFPAD)
    {
      window.YFPAD = {
        
      };
    }
    var b = YFPAD.lang, a = {
      test : (function (c) 
      {
        var d = (navigator.cookieEnabled) ? true : false;
        if(typeof navigator.cookieEnabled == "undefined" && ! d)
        {
          document.cookie = "fpadtestcookie";
          d = (document.cookie.indexOf("fpadtestcookie") != - 1) ? true : false;
        }
        return d;
      }),
      set : (function (g, e, f, c) 
      {
        var d = new Date(), h = b.createHash(e);
        f = f || "yahoo.com";
        c = c || 172800000;
        d.setTime(d.getTime() + c);
        document.cookie = g + "=" + h + "; expires=" + d.toGMTString() + "; domain=" + f + "; path=/";
      }),
      get : (function (g) 
      {
        var d = " " + document.cookie + ";", f = new RegExp(" " + g + "=([a-zA-Z0-9&=_]+);"), 
        e = d.match(f);
        if(e)
        {
          e = e[1];
        }
        return b.parseHash(e) || 0;
      }),
      remove : (function (d, c) 
      {
        a.set(d, "", c, - 345600000);
      })
    };
    YFPAD.cookie = a;
  })();
  (function () 
  {
    if(! window.YFPAD)
    {
      window.YFPAD = {
        
      };
    }
    var c = YFPAD.lang, a = {
      toggleTakeover : (function (d) 
      {
        if(window.ad_takeover)
        {
          ad_takeover(d);
        }
      }),
      adRunning : (function (d) 
      {
        if(window.ad_running)
        {
          window.ad_running(d);
        }
      }),
      YAD_init : (function (d) 
      {
        window.YAD = {
          play : (function (e) 
          {
            d("play", e);
          }),
          track : (function (e) 
          {
            d("track", e);
          }),
          button : {
            close : (function (e) 
            {
              d("close", e);
            })
          },
          close : (function (e) 
          {
            d("close", e);
          }),
          show : (function (e) 
          {
            d("show", e);
          }),
          hide : (function (e) 
          {
            d("hide", e);
          })
        };
      }),
      adAction : {
        
      },
      registerAdAction : (function (d) 
      {
        if(d.ad)
        {
          a.adAction[d.ad] = d;
        }
      }),
      inString : (function (d, e) 
      {
        return (d.indexOf(e) !== - 1);
      }),
      getInt : (function (e) 
      {
        var d = e.match(/[0-9]+/gi);
        if(d !== null)
        {
          return parseInt(d.join(""), 10);
        }
        return 0;
      }),
      generateFlashRedirects : (function (d) 
      {
        return c.createHash(d);
      }),
      pixelTrack : (function (d) 
      {
        if(d)
        {
          var e = new Image();
          e.onload = (function () 
          {
            e = null;
          });
          YFPAD.ff_debug("pixels", d);
          e.src = d;
        }
      })
    };
    function b(e) 
    {
      var f, d = YFPAD.util.adAction;
      for(f in d)
      {
        if(typeof d[f].callback === "function")
        {
          d[f].callback.call(d[f].context, e);
        }
      }
    }
    YFPAD.util = a;
    window.ad_action = b;
  })();
  