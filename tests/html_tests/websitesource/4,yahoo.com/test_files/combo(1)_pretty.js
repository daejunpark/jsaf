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
    YFPAD.lang = {
      bind : (function (f, b, e) 
      {
        var d, c;
        if(! Function.prototype.bind)
        {
          d = (function (g) 
          {
            e.apply(f, [g, b, ]);
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
    var c = a.className.toString(), d = YFPAD.dom, b = {
      html : a,
      isIE : (function () 
      {
        if(d.hasClass(a, "ua-ie"))
        {
          return c.match(/ua-ie(\d+)/)[1] || 1;
        }
        return 0;
      })(),
      isFireFox : (function () 
      {
        if(d.hasClass(a, "ua-ff"))
        {
          return c.match(/ua-ff(\d+)/)[1] || 1;
        }
        return 0;
      })(),
      isWebKit : (function () 
      {
        if(d.hasClass(a, "ua-wk"))
        {
          return c.match(/ua-wk(\d+)/)[1] || 1;
        }
        return 0;
      })(),
      isNarrow : (function () 
      {
        var e = d.hasClass(a, "lite-page") ? true : false;
        return e;
      })(),
      isFlash : (function () 
      {
        var e = 0;
        if(/flash-[0-9]+/.test(c))
        {
          e = parseInt(c.match(/flash-([0-9]+)/)[1], 10) || 0;
        }
        return e;
      })()
    };
    YFPAD.ua = b;
  })(document.documentElement);
  (function () 
  {
    if(! window.YFPAD)
    {
      window.YFPAD = {
        ua : {
          
        }
      };
    }
    function c(e) 
    {
      var d = e.match(/[0-9]+.?[0-9]*/);
      if(d && d[0])
      {
        return parseFloat(d[0]);
      }
      return 0;
    }
    var b = navigator.userAgent.toString(), a = {
      os : (function () 
      {
        var d = "other";
        if((/windows|win32/i).test(b))
        {
          d = "windows";
        }
        else
        {
          if((/macintosh/i).test(b))
          {
            d = "macintosh";
          }
          else
          {
            if((/iphone|ipod/i).test(b))
            {
              d = "ios";
            }
            else
            {
              if((/linux/i).test(b))
              {
                d = "linux";
              }
            }
          }
        }
        return d;
      })(),
      isFlash : (function () 
      {
        var d = "0";
        if(navigator.plugins && typeof navigator.plugins["Shockwave Flash"] == "object")
        {
          d = navigator.plugins["Shockwave Flash"].description;
          if(typeof (d) != undefined)
          {
            d = (d.replace(/^.*\s+(\S+\s+\S+$)/, "$1")).replace(" ", ".");
          }
        }
        else
        {
          if(window.ActiveXObject)
          {
            try
{              oActiveX = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.7");}
            catch(f)
{              try
{                oActiveX = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.6");
                oActiveX.AllowScriptAccess = "always";}
              catch(f)
{                return 6;}

              try
{                oActiveX = new ActiveXObject("ShockwaveFlash.ShockwaveFlash");}
              catch(f)
{                }
}

            if(typeof oActiveX == "object")
            {
              d = oActiveX.GetVariable("$version");
              if(typeof d != undefined)
              {
                d = d.replace(/^\S+\s+(.*)$/, "$1").replace(/,/g, ".");
              }
            }
          }
        }
        return c(d.split(".")[0]);
      })(),
      isIE : (function () 
      {
        var d = b.match(/MSIE\s([^;]*)/);
        if(d && d[1])
        {
          return c(d[1]);
        }
        return 0;
      })(),
      isSafari : (function () 
      {
        var d = b.match(/Version\/([0-9.]+)\s?Safari/);
        if(d && d[1])
        {
          return c(d[1]);
        }
        return 0;
      })(),
      isFireFox : (function () 
      {
        var d = b.match(/Firefox\/([0-9.]+)/);
        if(d && d[1])
        {
          return c(d[1]);
        }
        return 0;
      })(),
      isChrome : (function () 
      {
        var d = b.match(/Chrome\/([0-9.]+)/);
        if(d && d[1])
        {
          return parseInt(d[1]);
        }
        return 0;
      })(),
      isWebKit : (function () 
      {
        var d = b.match(/AppleWebKit\/([0-9.]+)/);
        if(d && d[1])
        {
          return c(d[1]);
        }
        return 0;
      })(),
      isNarrow : (function () 
      {
        if(document.documentElement.className.toString().match(/lite-page/))
        {
          return true;
        }
        return false;
      })()
    };
    window.YFPAD.ua = a;
  })();
  (function () 
  {
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
        document.cookie = g + "=t=" + h + "; expires=" + d.toGMTString() + "; domain=" + f + "; path=/";
      }),
      get : (function (g) 
      {
        var d = " " + document.cookie + ";", f = new RegExp(" " + g + "=t=([a-zA-Z0-9&=]+);"), 
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
    var d = YFPAD.dom, c = YFPAD.lang, b = YFPAD.ua, a = {
      params : (function (f, g) 
      {
        var e = [], i, h = f.match(/embed/g);
        for(i in g)
        {
          if(h)
          {
            e.push(i + '="' + g[i] + '"');
          }
          else
          {
            e.push("<param name=" + i + ' value="' + g[i] + '">');
          }
        }
        return [f, e.join(" "), ];
      }),
      objectEmbed : (function (r, m, i, e, p, v, g, t, w, j) 
      {
        var h = j || {
          
        }, u = g || "transparent", 
        q = (w) ? w : b.isIE, 
        f = (q) ? "object" : "embed", 
        k, 
        o = (typeof (e) === "object") ? c.createHash(e) : e, 
        n = {
          movie : i,
          play : "true"
        }, 
        l = {
          src : i,
          id : "swf" + v,
          width : r,
          height : m,
          type : "application/x-shockwave-flash",
          pluginspage : "http://www.adobe.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"
        }, 
        s = {
          wmode : u,
          quality : "high",
          loop : "false",
          menu : "false",
          allowFullScreen : "true",
          allowScriptAccess : "always",
          FlashVars : o
        };
        if(q)
        {
          s = c.mix(s, n);
        }
        else
        {
          s = c.mix(s, l);
        }
        s = c.mix(s, h);
        k = a.ad_embedObj("swf", v, p, r, m, a.params(f, s), t);
      }),
      ad_embedObj : (function (m, j, k, f, o, l, p) 
      {
        var h = "", i = p || "", e = d.byId(k) || null, n = (m + j) || "", 
        g = "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000";
        if(! m.match(/swf/gi) || ! l[0].match(/object|embed/gi) || e === null)
        {
          return false;
        }
        if(l[0].match(/object/gi))
        {
          h = '<object style="display:block;" alt="' + i + '" classid="' + g + '" id="' + n + '" width="' + f + '" height="' + o + '">' + l[1] + "</object>";
        }
        else
        {
          h = '<embed style="display:block;" alt="' + i + '" name="' + n + '_name" id="' + n + '" ' + l[1] + "></embed>";
        }
        e.innerHTML = h;
        return e.firstChild;
      })
    };
    YFPAD.flash = a;
  })();
  (function () 
  {
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
  