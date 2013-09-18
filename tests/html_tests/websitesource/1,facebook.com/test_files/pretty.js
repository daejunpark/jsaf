  if(self.CavalryLogger)
  {
    CavalryLogger.start_js(["FHcQn", ]);
  }
  if(! Array.isArray)
    Array.isArray = (function (a) 
    {
      return Object.prototype.toString.call(a) == '[object Array]';
    });
  if(! Array.prototype.map)
    Array.prototype.map = (function (a, b) 
    {
      if(typeof a != 'function')
        throw new TypeError();
      var c, d = this.length, e = new Array(d);
      for(c = 0;c < d;++ c)
        if(c in this)
          e[c] = a.call(b, this[c], c, this);
      return e;
    });
  if(! Array.prototype.forEach)
    Array.prototype.forEach = (function (a, b) 
    {
      this.map(a, b);
    });
  if(! Array.prototype.filter)
    Array.prototype.filter = (function (a, b) 
    {
      if(typeof a != 'function')
        throw new TypeError();
      var c, d, e = this.length, f = [];
      for(c = 0;c < e;++ c)
        if(c in this)
        {
          d = this[c];
          if(a.call(b, d, c, this))
            f.push(d);
        }
      return f;
    });
  if(! Array.prototype.every)
    Array.prototype.every = (function (a, b) 
    {
      if(typeof a != 'function')
        throw new TypeError();
      var c = new Object(this), d = c.length;
      for(var e = 0;e < d;e ++)
        if(e in c)
          if(! a.call(b, c[e], e, c))
            return false;
      return true;
    });
  if(! Array.prototype.some)
    Array.prototype.some = (function (a, b) 
    {
      if(typeof a != 'function')
        throw new TypeError();
      var c = new Object(this), d = c.length;
      for(var e = 0;e < d;e ++)
        if(e in c)
          if(a.call(b, c[e], e, c))
            return true;
      return false;
    });
  if(! Array.prototype.indexOf)
    Array.prototype.indexOf = (function (a, b) 
    {
      var c = this.length;
      b |= 0;
      if(b < 0)
        b += c;
      for(;b < c;b ++)
        if(b in this && this[b] === a)
          return b;
      return - 1;
    });
  if(! Date.now)
    Date.now = (function () 
    {
      return new Date().getTime();
    });
  self.__DEV__ = self.__DEV__ || 0;
  if(! Function.prototype.bind)
    Function.prototype.bind = (function (a) 
    {
      if(typeof this != 'function')
        throw new TypeError('Bind must be called on a function');
      var b = this, c = Array.prototype.slice.call(arguments, 1);
      function d() 
      {
        return b.apply(a, c.concat(Array.prototype.slice.call(arguments)));
      }
      d.displayName = 'bound:' + (b.displayName || b.name || '(?)');
      d.toString = (function e() 
      {
        return 'bound: ' + b;
      });
      return d;
    });
  var JSON;
  if(! JSON)
    JSON = {
      
    };
  (function () 
  {
    function a(j) 
    {
      return j < 10 ? '0' + j : j;
    }
    if(typeof Date.prototype.toJSON !== 'function')
    {
      Date.prototype.toJSON = (function (j) 
      {
        return isFinite(this.valueOf()) ? this.getUTCFullYear() + '-' + a(this.getUTCMonth() + 1) + '-' + a(this.getUTCDate()) + 'T' + a(this.getUTCHours()) + ':' + a(this.getUTCMinutes()) + ':' + a(this.getUTCSeconds()) + 'Z' : null;
      });
      String.prototype.toJSON = Number.prototype.toJSON = Boolean.prototype.toJSON = (function (j) 
      {
        return this.valueOf();
      });
    }
    var b = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, 
    c = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, 
    d, 
    e, 
    f = {
      "\\b" : '\\b',
      "\\t" : '\\t',
      "\\n" : '\\n',
      "\\f" : '\\f',
      "\\r" : '\\r',
      '"' : '\\"',
      "\\\\" : '\\\\'
    }, 
    g;
    function h(j) 
    {
      c.lastIndex = 0;
      return c.test(j) ? '"' + j.replace(c, (function (k) 
      {
        var l = f[k];
        return typeof l === 'string' ? l : '\\u' + ('0000' + k.charCodeAt(0).toString(16)).slice(- 4);
      })) + '"' : '"' + j + '"';
    }
    function i(j, k) 
    {
      var l, m, n, o, p = d, q, r = k[j];
      if(r && typeof r === 'object' && typeof r.toJSON === 'function')
        r = r.toJSON(j);
      if(typeof g === 'function')
        r = g.call(k, j, r);
      switch(typeof r){
        case 'string':
          return h(r);

        case 'number':
          return isFinite(r) ? String(r) : 'null';

        case 'boolean':
          

        case 'null':
          return String(r);

        case 'object':
          if(! r)
            return 'null';
          d += e;
          q = [];
          if(Object.prototype.toString.apply(r) === '[object Array]')
          {
            o = r.length;
            for(l = 0;l < o;l += 1)
              q[l] = i(l, r) || 'null';
            n = q.length === 0 ? '[]' : d ? '[\n' + d + q.join(',\n' + d) + '\n' + p + ']' : '[' + q.join(',') + ']';
            d = p;
            return n;
          }
          if(g && typeof g === 'object')
          {
            o = g.length;
            for(l = 0;l < o;l += 1)
              if(typeof g[l] === 'string')
              {
                m = g[l];
                n = i(m, r);
                if(n)
                  q.push(h(m) + (d ? ': ' : ':') + n);
              }
          }
          else
            for(m in r)
              if(Object.prototype.hasOwnProperty.call(r, m))
              {
                n = i(m, r);
                if(n)
                  q.push(h(m) + (d ? ': ' : ':') + n);
              }
          n = q.length === 0 ? '{}' : d ? '{\n' + d + q.join(',\n' + d) + '\n' + p + '}' : '{' + q.join(',') + '}';
          d = p;
          return n;

        
      }
    }
    if(typeof JSON.stringify !== 'function')
      JSON.stringify = (function (j, k, l) 
      {
        var m;
        d = '';
        e = '';
        if(typeof l === 'number')
        {
          for(m = 0;m < l;m += 1)
            e += ' ';
        }
        else
          if(typeof l === 'string')
            e = l;
        g = k;
        if(k && typeof k !== 'function' && (typeof k !== 'object' || typeof k.length !== 'number'))
          throw new Error('JSON.stringify');
        return i('', {
          "" : j
        });
      });
    if(typeof JSON.parse !== 'function')
      JSON.parse = (function (j, k) 
      {
        var l;
        function m(n, o) 
        {
          var p, q, r = n[o];
          if(r && typeof r === 'object')
            for(p in r)
              if(Object.prototype.hasOwnProperty.call(r, p))
              {
                q = m(r, p);
                if(q !== undefined)
                {
                  r[p] = q;
                }
                else
                  delete r[p];
              }
          return k.call(n, o, r);
        }
        j = String(j);
        b.lastIndex = 0;
        if(b.test(j))
          j = j.replace(b, (function (n) 
          {
            return '\\u' + ('0000' + n.charCodeAt(0).toString(16)).slice(- 4);
          }));
        if(/^[\],:{}\s]*$/.test(j.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@').replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, 
        ']').replace(/(?:^|:|,)(?:\s*\[)+/g, '')))
        {
          l = (eval)('(' + j + ')');
          return typeof k === 'function' ? m({
            "" : l
          }, '') : l;
        }
        throw new SyntaxError('JSON.parse');
      });
  })();
  if(JSON.stringify(["\u2028\u2029", ]) === '["\u2028\u2029"]')
    JSON.stringify = (function (a) 
    {
      var b = /\u2028/g, c = /\u2029/g;
      return (function (d, e, f) 
      {
        var g = a.call(this, d, e, f);
        if(g)
        {
          if(- 1 < g.indexOf('\u2028'))
            g = g.replace(b, '\\u2028');
          if(- 1 < g.indexOf('\u2029'))
            g = g.replace(c, '\\u2029');
        }
        return g;
      });
    })(JSON.stringify);
  if(! Object.create)
    Object.create = (function (a) 
    {
      var b = typeof a;
      if(b != 'object' && b != 'function')
        throw new TypeError('Object prototype may only be a Object or null');
      var c = new Function();
      c.prototype = a;
      return new c();
    });
  if(! Object.keys)
    Object.keys = (function (a) 
    {
      var b = typeof a;
      if(b != 'object' && b != 'function' || a === null)
        throw new TypeError('Object.keys called on non-object');
      var c = [];
      for(var d in a)
        if(Object.prototype.hasOwnProperty.call(a, d))
          c.push(d);
      var e = ! ({
        toString : true
      }).propertyIsEnumerable('toString'), 
      f = ['toString', 'toLocaleString', 'valueOf', 'hasOwnProperty', 'isPrototypeOf', 'prototypeIsEnumerable', 'constructor', ];
      if(e)
        for(var g = 0;g < f.length;g ++)
        {
          var h = f[g];
          if(Object.prototype.hasOwnProperty.call(a, h))
            c.push(h);
        }
      return c;
    });
  if(! Object.freeze)
    Object.freeze = (function (a) 
    {
      
    });
  if(! String.prototype.trim)
    String.prototype.trim = (function () 
    {
      if(this == null)
        throw new TypeError('String.prototype.trim called on null or undefined');
      return String.prototype.replace.call(this, /^\s+|\s+$/g, '');
    });
  (function () 
  {
    var a, b = String.prototype.split, c = /()??/.exec("")[1] === a;
    String.prototype.split = (function (d, e) 
    {
      var f = this;
      if(Object.prototype.toString.call(d) !== "[object RegExp]")
        return b.call(f, d, e);
      var g = [], h = (d.ignoreCase ? "i" : "") + (d.multiline ? "m" : "") + (d.extended ? "x" : "") + (d.sticky ? "y" : ""), 
      i = 0, 
      d = new RegExp(d.source, h + "g"), 
      j, 
      k, 
      l, 
      m;
      f += "";
      if(! c)
        j = new RegExp("^" + d.source + "$(?!\\s)", h);
      e = e === a ? - 1 >>> 0 : e >>> 0;
      while(k = d.exec(f))
      {
        l = k.index + k[0].length;
        if(l > i)
        {
          g.push(f.slice(i, k.index));
          if(! c && k.length > 1)
            k[0].replace(j, (function () 
            {
              for(var n = 1;n < arguments.length - 2;n ++)
                if(arguments[n] === a)
                  k[n] = a;
            }));
          if(k.length > 1 && k.index < f.length)
            Array.prototype.push.apply(g, k.slice(1));
          m = k[0].length;
          i = l;
          if(g.length >= e)
            break;
        }
        if(d.lastIndex === k.index)
          d.lastIndex ++;
      }
      if(i === f.length)
      {
        if(m || ! d.test(""))
          g.push("");
      }
      else
        g.push(f.slice(i));
      return g.length > e ? g.slice(0, e) : g;
    });
  })();
  (function () 
  {
    var a, b, c = Object.prototype.toString, d = {
      DOMElement : ['DOMEventTarget', ],
      DOMDocument : ['DOMEventTarget', ],
      DOMWindow : ['DOMEventTarget', ]
    };
    function e(i, j) 
    {
      var k = typeof i, l = c.call(i).slice(8, - 1), m, n, o, p = /^\?/.test(j);
      if(p)
        j = j.substring(1);
      var q = j.indexOf('function') !== 0 ? j.indexOf('<') : - 1;
      if(q !== - 1)
      {
        n = j.substring(q + 1, j.lastIndexOf('>'));
        j = j.substring(0, q);
      }
      if(i === undefined)
      {
        k = 'undefined';
      }
      else
        if(i === null)
        {
          k = 'null';
        }
        else
          if(l === 'Function')
          {
            k = i.__TCmeta && j !== 'function' ? i.__TCmeta.signature : 'function';
          }
          else
            if(k === 'object' || k === 'function')
            {
              var r = i.constructor;
              if(r && r.__TCmeta)
              {
                if(j === 'object')
                {
                  k = 'object';
                }
                else
                  while(r && r.__TCmeta)
                  {
                    if(r.__TCmeta.type == j)
                    {
                      k = j;
                      break;
                    }
                    r = r.__TCmeta.superClass;
                  }
              }
              else
                if((i.nodeType === 1 || i.nodeType === 11) && typeof i.nodeName === 'string')
                {
                  k = 'DOMElement';
                  m = i.nodeType === 11 ? 'FRAGMENT' : i.nodeName.toUpperCase();
                }
                else
                  if(i.nodeType === 9)
                  {
                    k = 'DOMDocument';
                  }
                  else
                    if(i.nodeType === 3)
                    {
                      k = 'DOMTextNode';
                    }
                    else
                      if(i == i.window && i == i.self)
                      {
                        k = 'DOMWindow';
                      }
                      else
                        if(l == 'XMLHttpRequest' || 'setRequestHeader' in i)
                        {
                          k = 'XMLHttpRequest';
                        }
                        else
                          switch(l){
                            case 'Error':
                              k = j === 'Error' ? 'Error' : i.name;
                              break;

                            case 'Array':
                              if(i.length)
                                o = i[0];

                            case 'Object':
                              

                            case 'RegExp':
                              

                            case 'Date':
                              k = l.toLowerCase();
                              break;

                            
                          }
            }
      if(p && /undefined|null/.test(k))
        return true;
      if(k in d)
      {
        var s = d[k], t = s.length;
        while(t --)
          if(s[t] === j)
          {
            k = j;
            break;
          }
      }
      b.push(k);
      return o && n ? j === k && e(o, n) : m && n ? j === k && m === n : j === k;
    }
    function f(i, j) 
    {
      var k = j.split('|'), l = k.length;
      while(l --)
      {
        b = [];
        if(e(i, k[l]))
          return true;
      }
      return false;
    }
    function g() 
    {
      var i = Array.prototype.slice.call(arguments), j = i.length;
      while(j --)
      {
        var k = i[j][0], l = i[j][1], m = i[j][2] || 'return value';
        if(! f(k, l))
        {
          var n = b.shift();
          while(b.length)
            n += '<' + b.shift() + '>';
          var o = new TypeError('Type Mismatch for ' + m + ': expected `' + l + '`, actual `' + n + '` (' + c.call(k) + ')');
          try
{            throw o;}
          catch(p)
{            p.framesToPop = i[j][2] ? 2 : 1;
            if(a)
              a(p);}

        }
      }
      return i[0][0];
    }
    g.setHandler = (function (i) 
    {
      a = i;
    });
    function h(i, j) 
    {
      i.__TCmeta = j;
      return i;
    }
    this.__t = g;
    this.__w = h;
  })();
  if(typeof console == 'undefined')
    (function () 
    {
      function a() 
      {
        
      }
      console = {
        log : a,
        info : a,
        warn : a,
        debug : a,
        dir : a,
        error : a
      };
    })();
  (function (a) 
  {
    if(a.require)
      return;
    var b = Object.prototype.toString, c = {
      
    }, d = {
      
    }, 
    e = {
      
    }, 
    f = 0, 
    g = 1, 
    h = 2, 
    i = Object.prototype.hasOwnProperty;
    function j(s) 
    {
      if(a.ErrorUtils && ! a.ErrorUtils.inGuard())
        return ErrorUtils.applyWithGuard(j, this, arguments);
      var t = c[s], u, v, w;
      if(! c[s])
      {
        w = 'Requiring unknown module "' + s + '"';
        throw new Error(w);
      }
      if(t.hasError)
        throw new Error('Requiring module "' + s + '" which threw an exception');
      if(t.waiting)
      {
        w = 'Requiring module "' + s + '" with unresolved dependencies';
        throw new Error(w);
      }
      if(! t.exports)
      {
        var x = t.exports = {
          
        }, y = t.factory;
        if(b.call(y) === '[object Function]')
        {
          var z = [], aa = t.dependencies, ba = aa.length, ca;
          if(t.special & h)
            ba = Math.min(ba, y.length);
          try
{            for(v = 0;v < ba;v ++)
            {
              u = aa[v];
              z.push(u === 'module' ? t : (u === 'exports' ? x : j(u)));
            }
            ca = y.apply(t.context || a, z);}
          catch(da)
{            t.hasError = true;
            throw da;}

          if(ca)
            t.exports = ca;
        }
        else
          t.exports = y;
      }
      if(t.refcount -- === 1)
        delete c[s];
      return t.exports;
    }
    function k(s, t, u, v, w, x) 
    {
      if(t === undefined)
      {
        t = [];
        u = s;
        s = n();
      }
      else
        if(u === undefined)
        {
          u = t;
          if(b.call(s) === '[object Array]')
          {
            t = s;
            s = n();
          }
          else
            t = [];
        }
      var y = {
        cancel : l.bind(this, s)
      }, z = c[s];
      if(z)
      {
        if(x)
          z.refcount += x;
        return y;
      }
      else
        if(! t && ! u && x)
        {
          e[s] = (e[s] || 0) + x;
          return y;
        }
        else
        {
          z = {
            id : s
          };
          z.refcount = (e[s] || 0) + (x || 0);
          delete e[s];
        }
      z.factory = u;
      z.dependencies = t;
      z.context = w;
      z.special = v;
      z.waitingMap = {
        
      };
      z.waiting = 0;
      z.hasError = false;
      c[s] = z;
      p(s);
      return y;
    }
    function l(s) 
    {
      if(! c[s])
        return;
      var t = c[s];
      delete c[s];
      for(var u in t.waitingMap)
        if(t.waitingMap[u])
          delete d[u][s];
      for(var v = 0;v < t.dependencies.length;v ++)
      {
        u = t.dependencies[v];
        if(c[u])
        {
          if(c[u].refcount -- === 1)
            l(u);
        }
        else
          if(e[u])
            e[u] --;
      }
    }
    function m(s, t, u) 
    {
      return k(s, t, undefined, g, u, 1);
    }
    function n() 
    {
      return '__mod__' + f ++;
    }
    function o(s, t) 
    {
      if(! s.waitingMap[t] && s.id !== t)
      {
        s.waiting ++;
        s.waitingMap[t] = 1;
        d[t] || (d[t] = {
          
        });
        d[t][s.id] = 1;
      }
    }
    function p(s) 
    {
      var t = [], u = c[s], v, w, x;
      for(w = 0;w < u.dependencies.length;w ++)
      {
        v = u.dependencies[w];
        if(! c[v])
        {
          o(u, v);
        }
        else
          if(c[v].waiting)
            for(x in c[v].waitingMap)
              if(c[v].waitingMap[x])
                o(u, x);
      }
      if(u.waiting === 0 && u.special & g)
        t.push(s);
      if(d[s])
      {
        var y = d[s], z;
        d[s] = undefined;
        for(v in y)
        {
          z = c[v];
          for(x in u.waitingMap)
            if(u.waitingMap[x])
              o(z, x);
          if(z.waitingMap[s])
          {
            z.waitingMap[s] = undefined;
            z.waiting --;
          }
          if(z.waiting === 0 && z.special & g)
            t.push(v);
        }
      }
      for(w = 0;w < t.length;w ++)
        j(t[w]);
    }
    function q(s, t) 
    {
      c[s] = {
        id : s
      };
      c[s].exports = t;
    }
    q('module', 0);
    q('exports', 0);
    q('define', k);
    q('global', a);
    q('require', j);
    q('requireDynamic', j);
    q('requireLazy', m);
    k.amd = {
      
    };
    a.define = k;
    a.require = j;
    a.requireDynamic = j;
    a.requireLazy = m;
    j.__debug = {
      modules : c,
      deps : d
    };
    var r = (function (s, t, u, v) 
    {
      k(s, t, u, v || h);
    });
    a.__d = (function (s, t, u, v) 
    {
      t = ['global', 'require', 'requireDynamic', 'requireLazy', 'module', 'exports', ].concat(t);
      r(s, t, u, v);
    });
  })(this);

  __d("SidebarPrelude", [], (function (a, b, c, d, e, f) 
  {
    var g = {
      addSidebarMode : (function (h) 
      {
        var i = document.documentElement;
        if(i.clientWidth > h)
          i.className = i.className + ' sidebarMode';
      })
    };
    e.exports = g;
  }));
  __d("eprintf", [], (function (a, b, c, d, e, f) 
  {
    var g = (function (h) 
    {
      var i = Array.prototype.slice.call(arguments).map((function (l) 
      {
        return String(l);
      })), 
      j = h.split('%s').length - 1;
      if(j !== i.length - 1)
        return g('eprintf args number mismatch: %s', JSON.stringify(i));
      var k = 1;
      return h.replace(/%s/g, (function (l) 
      {
        return String(i[k ++]);
      }));
    });
    e.exports = g;
  }));
  __d("ex", [], (function (a, b, c, d, e, f) 
  {
    var g = (function (h) 
    {
      var i = Array.prototype.slice.call(arguments).map((function (k) 
      {
        return String(k);
      })), 
      j = h.split('%s').length - 1;
      if(j !== i.length - 1)
        return g('ex args number mismatch: %s', JSON.stringify(i));
      return g._prefix + JSON.stringify(i) + g._suffix;
    });
    g._prefix = '<![EX[';
    g._suffix = ']]>';
    e.exports = g;
  }));
  __d("erx", ["ex", ], (function (a, b, c, d, e, f) 
  {
    var g = b('ex'), h = (function (i) 
    {
      if(typeof i !== 'string')
        return i;
      var j = i.indexOf(g._prefix), k = i.lastIndexOf(g._suffix);
      if(j < 0 || k < 0)
        return [i, ];
      var l = j + g._prefix.length, m = k + g._suffix.length;
      if(l >= k)
        return ['erx slice failure: %s', i, ];
      var n = i.substring(0, j), o = i.substring(m);
      i = i.substring(l, k);
      var p;
      try
{        p = JSON.parse(i);
        p[0] = n + p[0] + o;}
      catch(q)
{        return ['erx parse failure: %s', i, ];}

      return p;
    });
    e.exports = h;
  }));
  __d("copyProperties", [], (function (a, b, c, d, e, f) 
  {
    function g(h, i, j, k, l, m, n) 
    {
      h = h || {
        
      };
      var o = [i, j, k, l, m, ], p = 0, q;
      while(o[p])
      {
        q = o[p ++];
        for(var r in q)
          h[r] = q[r];
        if(q.hasOwnProperty && q.hasOwnProperty('toString') && (typeof q.toString != 'undefined') && (h.toString !== q.toString))
          h.toString = q.toString;
      }
      return h;
    }
    e.exports = g;
  }));
  __d("Env", ["copyProperties", ], (function (a, b, c, d, e, f) 
  {
    var g = b('copyProperties'), h = {
      start : Date.now()
    };
    if(a.Env)
    {
      g(h, a.Env);
      a.Env = undefined;
    }
    e.exports = h;
  }));
  __d("ErrorUtils", ["eprintf", "erx", "Env", ], (function (a, b, c, d, e, f) 
  {
    var g = b('eprintf'), h = b('erx'), i = b('Env'), j = '<anonymous guard>', 
    k = '<generated guard>', 
    l = '<window.onerror>', 
    m = [], 
    n = [], 
    o = 50, 
    p = window.chrome && 'type' in new Error(), 
    q = false;
    function r(da) 
    {
      if(! da)
        return;
      var ea = da.split(/\n\n/)[0].replace(/[\(\)]|\[.*?\]|^\w+:\s.*?\n/g, '').split('\n').map((function (fa) 
      {
        var ga, ha, ia;
        fa = fa.trim();
        if(/(:(\d+)(:(\d+))?)$/.test(fa))
        {
          ha = RegExp.$2;
          ia = RegExp.$4;
          fa = fa.slice(0, - RegExp.$1.length);
        }
        if(/(.*)(@|\s)[^\s]+$/.test(fa))
        {
          fa = fa.substring(RegExp.$1.length + 1);
          ga = /(at)?\s*(.*)([^\s]+|$)/.test(RegExp.$1) ? RegExp.$2 : '';
        }
        return '    at' + (ga ? ' ' + ga + ' (' : ' ') + fa.replace(/^@/, '') + (ha ? ':' + ha : '') + (ia ? ':' + ia : '') + (ga ? ')' : '');
      }));
      return ea.join('\n');
    }
    function s(da) 
    {
      if(! da)
      {
        return {
          
        };
      }
      else
        if(da._originalError)
          return da;
      var ea = {
        line : da.lineNumber || da.line,
        column : da.columnNumber || da.column,
        name : da.name,
        message : da.message,
        script : da.fileName || da.sourceURL || da.script,
        stack : r(da.stackTrace || da.stack),
        guard : da.guard
      };
      if(typeof ea.message === 'string')
      {
        ea.messageWithParams = h(ea.message);
        ea.message = g.apply(a, ea.messageWithParams);
      }
      else
      {
        ea.messageObject = ea.message;
        ea.message = String(ea.message);
      }
      ea._originalError = da;
      if(da.framesToPop && ea.stack)
      {
        var fa = ea.stack.split('\n');
        fa.shift();
        if(da.framesToPop === 2)
          da.message += ' ' + fa.shift().trim();
        ea.stack = fa.join('\n');
        if(/(\w{3,5}:\/\/[^:]+):(\d+)/.test(fa[0]))
        {
          ea.script = RegExp.$1;
          ea.line = parseInt(RegExp.$2, 10);
        }
        delete da.framesToPop;
      }
      if(p && /(\w{3,5}:\/\/[^:]+):(\d+)/.test(da.stack))
      {
        ea.script = RegExp.$1;
        ea.line = parseInt(RegExp.$2, 10);
      }
      for(var ga in ea)
        (ea[ga] == null && delete ea[ga]);
      return ea;
    }
    function t() 
    {
      try
{        throw new Error();}
      catch(da)
{        var ea = s(da).stack;
        return ea && ea.replace(/[\s\S]*__getTrace__.*\n/, '');}

    }
    function u(da, ea) 
    {
      if(q)
        return;
      da = s(da);
      ! ea;
      if(n.length > o)
        n.splice(o / 2, 1);
      n.push(da);
      q = true;
      for(var fa = 0;fa < m.length;fa ++)
        try
{          m[fa](da);}
        catch(ga)
{          }

      q = false;
    }
    var v = false;
    function w() 
    {
      return v;
    }
    function x() 
    {
      v = false;
    }
    function y(da, ea, fa, ga, ha) 
    {
      var ia = ! v;
      if(ia)
        v = true;
      var ja, ka = i.nocatch || (/nocatch/).test(location.search);
      if(ka)
      {
        ja = da.apply(ea, fa || []);
        if(ia)
          x();
        return ja;
      }
      try
{        ja = da.apply(ea, fa || []);
        if(ia)
          x();
        return ja;}
      catch(la)
{        if(ia)
          x();
        var ma = s(la);
        if(ga)
          ga(ma);
        if(da)
          ma.callee = da.toString().substring(0, 100);
        if(fa)
          ma.args = String(fa).substring(0, 100);
        ma.guard = ha || j;
        u(ma);}

    }
    function z(da, ea) 
    {
      ea = ea || da.name || k;
      function fa() 
      {
        return y(da, this, arguments, null, ea);
      }
      return fa;
    }
    function aa(da, ea, fa, ga) 
    {
      u({
        message : da,
        script : ea,
        line : fa,
        column : ga,
        guard : l
      }, 
      true);
    }
    window.onerror = aa;
    function ba(da, ea) 
    {
      m.push(da);
      if(! ea)
        n.forEach(da);
    }
    var ca = {
      ANONYMOUS_GUARD_TAG : j,
      GENERATED_GUARD_TAG : k,
      GLOBAL_ERROR_HANDLER_TAG : l,
      addListener : ba,
      applyWithGuard : y,
      getTrace : t,
      guard : z,
      history : n,
      inGuard : w,
      normalizeError : s,
      onerror : aa,
      reportError : u
    };
    e.exports = a.ErrorUtils = ca;
    if(typeof __t !== 'undefined')
      __t.setHandler(u);
  }));
  __d("CallbackDependencyManager", ["ErrorUtils", ], (function (a, b, c, d, e, f) 
  {
    var g = b('ErrorUtils');
    function h() 
    {
      this.$CallbackDependencyManager0 = {
        
      };
      this.$CallbackDependencyManager1 = {
        
      };
      this.$CallbackDependencyManager2 = 1;
      this.$CallbackDependencyManager3 = {
        
      };
    }
    __w(h, {
      type : "CallbackDependencyManager"
    });
    h.prototype.$CallbackDependencyManager4 = (__w((function (i, j) 
    {
      __t([i, 'number', 'callbackID', ], [j, 'array<string>', 'deps', ]);
      var k = 0, l = {
        
      };
      for(var m = 0, n = j.length;m < n;m ++)
        l[j[m]] = 1;
      for(var o in l)
      {
        if(this.$CallbackDependencyManager3[o])
          continue;
        k ++;
        if(this.$CallbackDependencyManager0[o] === undefined)
          this.$CallbackDependencyManager0[o] = {
            
          };
        this.$CallbackDependencyManager0[o][i] = (this.$CallbackDependencyManager0[o][i] || 0) + 1;
      }
      return k;
    }), 
    {
      signature : "function(number,array<string>)"
    }));
    h.prototype.$CallbackDependencyManager5 = (__w((function (i) 
    {
      __t([i, 'string', 'depName', ]);
      if(! this.$CallbackDependencyManager0[i])
        return;
      for(var j in this.$CallbackDependencyManager0[i])
      {
        this.$CallbackDependencyManager0[i][j] --;
        if(this.$CallbackDependencyManager0[i][j] <= 0)
          delete this.$CallbackDependencyManager0[i][j];
        this.$CallbackDependencyManager1[j].$CallbackDependencyManager6 --;
        if(this.$CallbackDependencyManager1[j].$CallbackDependencyManager6 <= 0)
        {
          var k = this.$CallbackDependencyManager1[j].$CallbackDependencyManager7;
          delete this.$CallbackDependencyManager1[j];
          g.applyWithGuard(k);
        }
      }
    }), 
    {
      signature : "function(string)"
    }));
    h.prototype.addDependenciesToExistingCallback = (__w((function (i, j) 
    {
      __t([i, 'number', 'callbackID', ], [j, 'array<string>', 'newDeps', ]);
      return __t([(function () 
      {
        if(! this.$CallbackDependencyManager1[i])
          return null;
        var k = this.$CallbackDependencyManager4(i, j);
        this.$CallbackDependencyManager1[i].$CallbackDependencyManager6 += k;
        return i;
      }).apply(this, arguments), '?number', ]);
    }), 
    {
      signature : "function(number,array<string>):number?"
    }));
    h.prototype.isPersistentDependencySatisfied = (__w((function (i) 
    {
      __t([i, 'string', 'depName', ]);
      return __t([(function () 
      {
        return ! ! this.$CallbackDependencyManager3[i];
      }).apply(this, arguments), 'boolean', ]);
    }), 
    {
      signature : "function(string):boolean"
    }));
    h.prototype.satisfyPersistentDependency = (__w((function (i) 
    {
      __t([i, 'string', 'depName', ]);
      this.$CallbackDependencyManager3[i] = 1;
      this.$CallbackDependencyManager5(i);
    }), 
    {
      signature : "function(string)"
    }));
    h.prototype.satisfyNonPersistentDependency = (__w((function (i) 
    {
      __t([i, 'string', 'depName', ]);
      var j = this.$CallbackDependencyManager3[i] === 1;
      if(! j)
        this.$CallbackDependencyManager3[i] = 1;
      this.$CallbackDependencyManager5(i);
      if(! j)
        delete this.$CallbackDependencyManager3[i];
    }), 
    {
      signature : "function(string)"
    }));
    h.prototype.registerCallback = (__w((function (i, j) 
    {
      __t([i, 'function', 'callback', ], [j, 'array<string>', 'deps', ]);
      return __t([(function () 
      {
        var k = this.$CallbackDependencyManager2;
        this.$CallbackDependencyManager2 ++;
        var l = this.$CallbackDependencyManager4(k, j);
        if(l === 0)
        {
          g.applyWithGuard(i);
          return null;
        }
        this.$CallbackDependencyManager1[k] = {
          $CallbackDependencyManager7 : i,
          $CallbackDependencyManager6 : l
        };
        return k;
      }).apply(this, arguments), '?number', ]);
    }), 
    {
      signature : "function(function,array<string>):number?"
    }));
    h.prototype.unsatisfyPersistentDependency = (__w((function (i) 
    {
      __t([i, 'string', 'depName', ]);
      delete this.$CallbackDependencyManager3[i];
    }), 
    {
      signature : "function(string)"
    }));
    e.exports = h;
  }));
  __d("hasArrayNature", [], (function (a, b, c, d, e, f) 
  {
    function g(h) 
    {
      return (! ! h && (typeof h == 'object' || typeof h == 'function') && ('length' in h) && ! ('setInterval' in h) && (Object.prototype.toString.call(h) === "[object Array]" || ('callee' in h) || ('item' in h)));
    }
    e.exports = g;
  }));
  __d("createArrayFrom", ["hasArrayNature", ], (function (a, b, c, d, e, f) 
  {
    var g = b('hasArrayNature');
    function h(i) 
    {
      if(! g(i))
        return [i, ];
      if(i.item)
      {
        var j = i.length, k = new Array(j);
        while(j --)
          k[j] = i[j];
        return k;
      }
      return Array.prototype.slice.call(i);
    }
    e.exports = h;
  }));
  __d("invariant", [], (function (a, b, c, d, e, f) 
  {
    function g(h) 
    {
      if(! h)
        throw new Error('Invariant Violation');
    }
    e.exports = g;
  }));
  __d("EventEmitter", ["createArrayFrom", "invariant", ], (function (a, b, c, d, e, f) 
  {
    var g = b('createArrayFrom'), h = b('invariant');
    function i() 
    {
      this.$EventEmitter0 = {
        
      };
      this.$EventEmitter1 = {
        
      };
      this.$EventEmitter2 = {
        
      };
    }
    __w(i, {
      type : "EventEmitter"
    });
    i.prototype.addListener = (__w((function (k, l, m) 
    {
      __t([k, 'string', 'eventType', ], [l, 'function', 'listener', ]);
      if(! this.$EventEmitter0[k])
        this.$EventEmitter0[k] = [];
      var n = this.$EventEmitter0[k].length;
      this.$EventEmitter0[k].push(l);
      if(m !== undefined)
      {
        if(! this.$EventEmitter1[k])
          this.$EventEmitter1[k] = [];
        this.$EventEmitter1[k][n] = m;
      }
      return new j(this, k, n);
    }), 
    {
      signature : "function(string,function)"
    }));
    i.prototype.once = (__w((function (k, l, m) 
    {
      __t([k, 'string', 'eventType', ], [l, 'function', 'listener', ]);
      var n = this;
      return this.addListener(k, (function () 
      {
        n.removeCurrentListener();
        l.apply(m, arguments);
      }));
    }), 
    {
      signature : "function(string,function)"
    }));
    i.prototype.removeAllListeners = (__w((function (k) 
    {
      __t([k, '?string', 'eventType', ]);
      if(k === undefined)
      {
        this.$EventEmitter0 = {
          
        };
        this.$EventEmitter1 = {
          
        };
      }
      else
      {
        delete this.$EventEmitter0[k];
        delete this.$EventEmitter1[k];
      }
    }), 
    {
      signature : "function(?string)"
    }));
    i.prototype.removeCurrentListener = (function () 
    {
      h(this.$EventEmitter2.key !== undefined);
      this.removeSubscription(this.$EventEmitter2);
    });
    i.prototype.removeSubscription = (__w((function (k) 
    {
      __t([k, 'object', 'subscription', ]);
      var l = k.eventType, m = k.key, n = this.$EventEmitter0[l];
      if(n)
        delete n[m];
      var o = this.$EventEmitter1[l];
      if(o)
        delete o[m];
    }), 
    {
      signature : "function(object)"
    }));
    i.prototype.listeners = (__w((function (k) 
    {
      __t([k, 'string', 'eventType', ]);
      var l = this.$EventEmitter0[k];
      return l ? g(l) : [];
    }), 
    {
      signature : "function(string)"
    }));
    i.prototype.emit = (__w((function (k, l, m, n, o, p, q) 
    {
      __t([k, 'string', 'eventType', ]);
      h(q === undefined);
      var r = this.$EventEmitter0[k];
      if(r)
      {
        var s = this.$EventEmitter1[k];
        this.$EventEmitter2.eventType = k;
        var t = Object.keys(r);
        for(var u = 0;u < t.length;u ++)
        {
          var v = t[u], w = r[v];
          if(w)
          {
            var x = s ? s[v] : undefined;
            this.$EventEmitter2.key = v;
            if(x === undefined)
            {
              w(l, m, n, o, p);
            }
            else
              w.call(x, l, m, n, o, p);
          }
        }
        this.$EventEmitter2.eventType = undefined;
        this.$EventEmitter2.key = undefined;
      }
    }), 
    {
      signature : "function(string)"
    }));
    function j(k, l, m) 
    {
      this.$ListenerSubscription0 = k;
      this.eventType = l;
      this.key = m;
    }
    __w(j, {
      type : "ListenerSubscription"
    });
    j.prototype.remove = (function () 
    {
      this.$ListenerSubscription0.removeSubscription(this);
    });
    e.exports = i;
  }));
  __d("EventEmitterWithHolding", [], (function (a, b, c, d, e, f) 
  {
    function g(h, i) 
    {
      __t([h, 'object', 'emitter', ], [i, 'object', 'holder', ]);
      this.$EventEmitterWithHolding0 = h;
      this.$EventEmitterWithHolding1 = i;
      this.$EventEmitterWithHolding2 = null;
      this.$EventEmitterWithHolding3 = false;
    }
    __w(g, {
      type : "EventEmitterWithHolding",
      signature : "function(object,object)"
    });
    g.prototype.addListener = (function (h, i, j) 
    {
      return this.$EventEmitterWithHolding0.addListener(h, i, j);
    });
    g.prototype.once = (function (h, i, j) 
    {
      return this.$EventEmitterWithHolding0.once(h, i, j);
    });
    g.prototype.addRetroactiveListener = (__w((function (h, i, j) 
    {
      __t([h, 'string', 'eventType', ], [i, 'function', 'listener', ]);
      var k = this.$EventEmitterWithHolding0.addListener(h, i, j);
      this.$EventEmitterWithHolding3 = true;
      this.$EventEmitterWithHolding1.emitToListener(h, i, j);
      this.$EventEmitterWithHolding3 = false;
      return k;
    }), 
    {
      signature : "function(string,function)"
    }));
    g.prototype.removeAllListeners = (function (h) 
    {
      this.$EventEmitterWithHolding0.removeAllListeners(h);
    });
    g.prototype.removeCurrentListener = (function () 
    {
      this.$EventEmitterWithHolding0.removeCurrentListener();
    });
    g.prototype.removeSubscription = (function (h) 
    {
      this.$EventEmitterWithHolding0.removeSubscription(h);
    });
    g.prototype.listeners = (function (h) 
    {
      return this.$EventEmitterWithHolding0.listeners(h);
    });
    g.prototype.emit = (function (h, i, j, k, l, m, n) 
    {
      this.$EventEmitterWithHolding0.emit(h, i, j, k, l, m, n);
    });
    g.prototype.emitAndHold = (__w((function (h, i, j, k, l, m, n) 
    {
      __t([h, 'string', 'eventType', ]);
      this.$EventEmitterWithHolding2 = this.$EventEmitterWithHolding1.holdEvent(h, i, j, k, l, m, n);
      this.$EventEmitterWithHolding0.emit(h, i, j, k, l, m, n);
      this.$EventEmitterWithHolding2 = null;
    }), 
    {
      signature : "function(string)"
    }));
    g.prototype.releaseCurrentEvent = (function () 
    {
      if(this.$EventEmitterWithHolding2 !== null)
      {
        this.$EventEmitterWithHolding1.releaseEvent(this.$EventEmitterWithHolding2);
      }
      else
        if(this.$EventEmitterWithHolding3)
          this.$EventEmitterWithHolding1.releaseCurrentEvent();
    });
    e.exports = g;
  }));
  __d("EventHolder", ["invariant", ], (function (a, b, c, d, e, f) 
  {
    var g = b('invariant');
    function h() 
    {
      this.$EventHolder0 = [];
      this.$EventHolder1 = [];
      this.$EventHolder2 = null;
    }
    __w(h, {
      type : "EventHolder"
    });
    h.prototype.holdEvent = (__w((function (i, j, k, l, m, n, o) 
    {
      __t([i, 'string', 'eventType', ]);
      var p = this.$EventHolder0.length, event = [i, j, k, l, m, n, o, ];
      this.$EventHolder0.push(event);
      return p;
    }), 
    {
      signature : "function(string)"
    }));
    h.prototype.emitToListener = (__w((function (i, j, k) 
    {
      __t([i, '?string', 'eventType', ], [j, 'function', 'listener', ], 
      [k, '?object', 'context', ]);
      this.forEachHeldEvent((function (l, m, n, o, p, q, r) 
      {
        if(l === i)
          j.call(k, m, n, o, p, q, r);
      }));
    }), 
    {
      signature : "function(?string,function,?object)"
    }));
    h.prototype.forEachHeldEvent = (__w((function (i, j) 
    {
      __t([i, 'function', 'callback', ], [j, '?object', 'context', ]);
      this.$EventHolder0.forEach((function (event, k) 
      {
        this.$EventHolder2 = k;
        i.apply(j, event);
      }), 
      this);
      this.$EventHolder2 = null;
    }), 
    {
      signature : "function(function,?object)"
    }));
    h.prototype.releaseCurrentEvent = (function () 
    {
      g(this.$EventHolder2 !== null);
      delete this.$EventHolder0[this.$EventHolder2];
    });
    h.prototype.releaseEvent = (function (i) 
    {
      delete this.$EventHolder0[i];
    });
    e.exports = h;
  }));
  __d("asyncCallback", [], (function (a, b, c, d, e, f) 
  {
    function g(h, i) 
    {
      if(a.ArbiterMonitor)
        return a.ArbiterMonitor.asyncCallback(h, i);
      return h;
    }
    e.exports = g;
  }));
  __d("emptyFunction", ["copyProperties", ], (function (a, b, c, d, e, f) 
  {
    var g = b('copyProperties');
    function h(j) 
    {
      return (function () 
      {
        return j;
      });
    }
    function i() 
    {
      
    }
    g(i, {
      thatReturns : h,
      thatReturnsFalse : h(false),
      thatReturnsTrue : h(true),
      thatReturnsNull : h(null),
      thatReturnsThis : (function () 
      {
        return this;
      }),
      thatReturnsArgument : (function (j) 
      {
        return j;
      })
    });
    e.exports = i;
  }));
  __d("Arbiter", ["CallbackDependencyManager", "ErrorUtils", "EventEmitter", "EventEmitterWithHolding", "EventHolder", "asyncCallback", "copyProperties", "createArrayFrom", "emptyFunction", "hasArrayNature", "invariant", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('CallbackDependencyManager'), h = b('ErrorUtils'), 
    i = b('EventEmitter'), 
    j = b('EventEmitterWithHolding'), 
    k = b('EventHolder'), 
    l = b('asyncCallback'), 
    m = b('copyProperties'), 
    n = b('createArrayFrom'), 
    o = b('emptyFunction'), 
    p = b('hasArrayNature'), 
    q = b('invariant');
    function r() 
    {
      var x = new i();
      this.$Arbiter0 = new u();
      this.$Arbiter1 = new j(x, this.$Arbiter0);
      this.$Arbiter2 = new g();
      this.$Arbiter3 = [];
    }
    r.prototype.subscribe = (function (x, y, z) 
    {
      x = n(x);
      q(x.every(o.thatReturnsArgument));
      z = z || r.SUBSCRIBE_ALL;
      q(z === r.SUBSCRIBE_NEW || z === r.SUBSCRIBE_ALL);
      var aa = x.map((function (ba) 
      {
        var ca = this.$Arbiter4.bind(this, y, ba);
        if(z === r.SUBSCRIBE_NEW)
          return this.$Arbiter1.addListener(ba, ca);
        this.$Arbiter3.push({
          
        });
        var da = this.$Arbiter1.addRetroactiveListener(ba, ca);
        this.$Arbiter3.pop();
        return da;
      }), 
      this);
      return new w(this, aa);
    });
    r.prototype.$Arbiter4 = (function (x, y, z) 
    {
      var aa = this.$Arbiter3[this.$Arbiter3.length - 1];
      if(aa[y] === false)
        return;
      var ba = h.applyWithGuard(x, null, [y, z, ]);
      if(ba === false)
        this.$Arbiter1.releaseCurrentEvent();
      aa[y] = ba;
    });
    r.prototype.subscribeOnce = (function (x, y, z) 
    {
      var aa = this.subscribe(x, (function (ba, ca) 
      {
        aa && aa.unsubscribe();
        return y(ba, ca);
      }), 
      z);
      return aa;
    });
    r.prototype.unsubscribe = (function (x) 
    {
      q(x.isForArbiterInstance(this));
      x.unsubscribe();
    });
    r.prototype.inform = (function (x, y, z) 
    {
      var aa = p(x);
      x = n(x);
      z = z || r.BEHAVIOR_EVENT;
      var ba = (z === r.BEHAVIOR_STATE) || (z === r.BEHAVIOR_PERSISTENT), 
      ca = a.ArbiterMonitor;
      this.$Arbiter3.push({
        
      });
      for(var da = 0;da < x.length;da ++)
      {
        var ea = x[da];
        q(ea);
        this.$Arbiter0.setHoldingBehavior(ea, z);
        ca && ca.record('event', ea, y, this);
        this.$Arbiter1.emitAndHold(ea, y);
        this.$Arbiter5(ea, y, ba);
        ca && ca.record('done', ea, y, this);
      }
      var fa = this.$Arbiter3.pop();
      return aa ? fa : fa[x[0]];
    });
    r.prototype.query = (function (x) 
    {
      var y = this.$Arbiter0.getHoldingBehavior(x);
      q(! y || y === r.BEHAVIOR_STATE);
      var z = null;
      this.$Arbiter0.emitToListener(x, (function (aa) 
      {
        z = aa;
      }));
      return z;
    });
    r.prototype.registerCallback = (function (x, y) 
    {
      if(typeof x === 'function')
      {
        return this.$Arbiter2.registerCallback(l(x, 'arbiter'), y);
      }
      else
        return this.$Arbiter2.addDependenciesToExistingCallback(x, y);
    });
    r.prototype.$Arbiter5 = (function (x, y, z) 
    {
      if(y === null)
        return;
      if(z)
      {
        this.$Arbiter2.satisfyPersistentDependency(x);
      }
      else
        this.$Arbiter2.satisfyNonPersistentDependency(x);
    });
    var s = k, t = s && s.prototype ? s.prototype : s;
    function u() 
    {
      s.call(this);
      this.$ArbiterEventHolder0 = {
        
      };
    }
    for(var v in s)
      if(v !== "_metaprototype" && s.hasOwnProperty(v))
        u[v] = s[v];
    u.prototype = Object.create(t);
    u.prototype.constructor = u;
    u.prototype.setHoldingBehavior = (function (x, y) 
    {
      this.$ArbiterEventHolder0[x] = y;
    });
    u.prototype.getHoldingBehavior = (function (x) 
    {
      return this.$ArbiterEventHolder0[x];
    });
    u.prototype.holdEvent = (function (x, y, z, aa, ba) 
    {
      var ca = this.$ArbiterEventHolder0[x];
      if(ca !== r.BEHAVIOR_PERSISTENT)
        this.$ArbiterEventHolder1(x);
      if(ca !== r.BEHAVIOR_EVENT)
        return t.holdEvent.call(this, x, y, z, aa, ba);
    });
    u.prototype.$ArbiterEventHolder1 = (function (x) 
    {
      this.emitToListener(x, this.releaseCurrentEvent, this);
    });
    m(r, {
      SUBSCRIBE_NEW : 'new',
      SUBSCRIBE_ALL : 'all',
      BEHAVIOR_EVENT : 'event',
      BEHAVIOR_STATE : 'state',
      BEHAVIOR_PERSISTENT : 'persistent'
    });
    function w(x, y) 
    {
      this.$ArbiterToken0 = x;
      this.$ArbiterToken1 = y;
    }
    w.prototype.unsubscribe = (function () 
    {
      for(var x = 0;x < this.$ArbiterToken1.length;x ++)
        this.$ArbiterToken1[x].remove();
      this.$ArbiterToken1.length = 0;
    });
    w.prototype.isForArbiterInstance = (function (x) 
    {
      q(this.$ArbiterToken0);
      return this.$ArbiterToken0 === x;
    });
    Object.keys(r.prototype).forEach((function (x) 
    {
      r[x] = (function () 
      {
        var y = (this instanceof r) ? this : r;
        return r.prototype[x].apply(y, arguments);
      });
    }));
    r.call(r);
    e.exports = r;
  }));
  __d("ArbiterMixin", ["Arbiter", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter'), h = {
      _getArbiterInstance : (function () 
      {
        return this._arbiter || (this._arbiter = new g());
      }),
      inform : (function (i, j, k) 
      {
        return this._getArbiterInstance().inform(i, j, k);
      }),
      subscribe : (function (i, j, k) 
      {
        return this._getArbiterInstance().subscribe(i, j, k);
      }),
      subscribeOnce : (function (i, j, k) 
      {
        return this._getArbiterInstance().subscribeOnce(i, j, k);
      }),
      unsubscribe : (function (i) 
      {
        this._getArbiterInstance().unsubscribe(i);
      }),
      registerCallback : (function (i, j) 
      {
        this._getArbiterInstance().registerCallback(i, j);
      }),
      query : (function (i) 
      {
        return this._getArbiterInstance().query(i);
      })
    };
    e.exports = h;
  }));
  __d("legacy:ArbiterMixin", ["ArbiterMixin", ], (function (a, b, c, d) 
  {
    a.ArbiterMixin = b('ArbiterMixin');
  }), 
  3);
  __d("ge", [], (function (a, b, c, d, e, f) 
  {
    function g(j, k, l) 
    {
      return typeof j != 'string' ? j : ! k ? document.getElementById(j) : h(j, k, l);
    }
    function h(j, k, l) 
    {
      var m, n, o;
      if(i(k) == j)
      {
        return k;
      }
      else
        if(k.getElementsByTagName)
        {
          n = k.getElementsByTagName(l || '*');
          for(o = 0;o < n.length;o ++)
            if(i(n[o]) == j)
              return n[o];
        }
        else
        {
          n = k.childNodes;
          for(o = 0;o < n.length;o ++)
          {
            m = h(j, n[o]);
            if(m)
              return m;
          }
        }
      return null;
    }
    function i(j) 
    {
      var k = j.getAttributeNode && j.getAttributeNode('id');
      return k ? k.value : null;
    }
    e.exports = g;
  }));
  __d("$", ["ge", "ex", ], (function (a, b, c, d, e, f) 
  {
    var g = b('ge'), h = b('ex');
    function i(j) 
    {
      __t([j, 'string|DOMDocument|DOMElement|DOMTextNode', 'id', ]);
      return __t([(function () 
      {
        var k = g(j);
        if(! k)
          throw new Error(h('Tried to get element with id of "%s" but it is not present on the page.', 
          j));
        return k;
      }).apply(this, arguments), 'DOMDocument|DOMElement|DOMTextNode', ]);
    }
    __w(i, {
      signature : "function(string|DOMDocument|DOMElement|DOMTextNode):DOMDocument|DOMElement|DOMTextNode"
    });
    e.exports = i;
  }));
  __d("CSSCore", ["invariant", ], (function (a, b, c, d, e, f) 
  {
    var g = b('invariant');
    function h(j, k) 
    {
      __t([j, 'DOMElement', 'element', ], [k, 'string', 'className', ]);
      if(j.classList)
        return ! ! k && j.classList.contains(k);
      return (' ' + j.className + ' ').indexOf(' ' + k + ' ') > - 1;
    }
    __w(h, {
      signature : "function(DOMElement,string)"
    });
    var i = {
      addClass : (__w((function (j, k) 
      {
        __t([j, 'DOMElement', 'element', ], [k, 'string', 'className', ]);
        return __t([(function () 
        {
          g(! /\s/.test(k));
          if(k)
            if(j.classList)
            {
              j.classList.add(k);
            }
            else
              if(! h(j, k))
                j.className = j.className + ' ' + k;
          return j;
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement,string):DOMElement"
      })),
      removeClass : (__w((function (j, k) 
      {
        __t([j, 'DOMElement', 'element', ], [k, 'string', 'className', ]);
        return __t([(function () 
        {
          g(! /\s/.test(k));
          if(k)
            if(j.classList)
            {
              j.classList.remove(k);
            }
            else
              if(h(j, k))
                j.className = j.className.replace(new RegExp('(^|\\s)' + k + '(?:\\s|$)', 'g'), '$1').replace(/\s+/g, ' ').replace(/^\s*|\s*$/g, '');
          return j;
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement,string):DOMElement"
      })),
      conditionClass : (__w((function (j, k, l) 
      {
        __t([j, 'DOMElement', 'element', ], [k, 'string', 'className', ]);
        return __t([(function () 
        {
          return (l ? i.addClass : i.removeClass)(j, k);
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement,string):DOMElement"
      }))
    };
    e.exports = i;
  }));
  __d("CSS", ["$", "CSSCore", ], (function (a, b, c, d, e, f) 
  {
    var g = b('$'), h = b('CSSCore'), i = 'hidden_elem', j = {
      setClass : (__w((function (k, l) 
      {
        __t([k, 'DOMElement|string', 'element', ], [l, 'string', 'className', ]);
        return __t([(function () 
        {
          g(k).className = l || '';
          return k;
        }).apply(this, arguments), 'DOMElement|string', ]);
      }), 
      {
        signature : "function(DOMElement|string,string):DOMElement|string"
      })),
      hasClass : (__w((function (k, l) 
      {
        __t([k, 'DOMElement|DOMTextNode|DOMDocument|string', 'element', ], 
        [l, 'string', 'className', ]);
        k = g(k);
        if(k.classList)
          return ! ! l && k.classList.contains(l);
        return (' ' + k.className + ' ').indexOf(' ' + l + ' ') > - 1;
      }), 
      {
        signature : "function(DOMElement|DOMTextNode|DOMDocument|string,string)"
      })),
      addClass : (__w((function (k, l) 
      {
        __t([k, 'DOMElement|string', 'element', ], [l, 'string', 'className', ]);
        return __t([(function () 
        {
          return h.addClass(g(k), l);
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement|string,string):DOMElement"
      })),
      removeClass : (__w((function (k, l) 
      {
        __t([k, 'DOMElement|string', 'element', ], [l, 'string', 'className', ]);
        return __t([(function () 
        {
          return h.removeClass(g(k), l);
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement|string,string):DOMElement"
      })),
      conditionClass : (__w((function (k, l, m) 
      {
        __t([k, 'DOMElement|string', 'element', ], [l, 'string', 'className', ]);
        return __t([(function () 
        {
          return h.conditionClass(g(k), l, m);
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement|string,string):DOMElement"
      })),
      toggleClass : (__w((function (k, l) 
      {
        __t([k, 'DOMElement|string', 'element', ], [l, 'string', 'className', ]);
        return __t([(function () 
        {
          return j.conditionClass(k, l, ! j.hasClass(k, l));
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement|string,string):DOMElement"
      })),
      shown : (__w((function (k) 
      {
        __t([k, 'DOMElement|string', 'element', ]);
        return __t([(function () 
        {
          return ! j.hasClass(k, i);
        }).apply(this, arguments), 'boolean', ]);
      }), 
      {
        signature : "function(DOMElement|string):boolean"
      })),
      hide : (__w((function (k) 
      {
        __t([k, 'DOMElement|string', 'element', ]);
        return __t([(function () 
        {
          return j.addClass(k, i);
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement|string):DOMElement"
      })),
      show : (__w((function (k) 
      {
        __t([k, 'DOMElement|string', 'element', ]);
        return __t([(function () 
        {
          return j.removeClass(k, i);
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement|string):DOMElement"
      })),
      toggle : (__w((function (k) 
      {
        __t([k, 'DOMElement|string', 'element', ]);
        return __t([(function () 
        {
          return j.toggleClass(k, i);
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement|string):DOMElement"
      })),
      conditionShow : (__w((function (k, l) 
      {
        __t([k, 'DOMElement|string', 'element', ]);
        return __t([(function () 
        {
          return j.conditionClass(k, i, ! l);
        }).apply(this, arguments), 'DOMElement', ]);
      }), 
      {
        signature : "function(DOMElement|string):DOMElement"
      }))
    };
    e.exports = j;
  }));
  __d("legacy:css-core", ["CSS", ], (function (a, b, c, d) 
  {
    a.CSS = b('CSS');
  }), 
  3);
  __d("legacy:dom-core", ["$", "ge", ], (function (a, b, c, d) 
  {
    a.$ = b('$');
    a.ge = b('ge');
  }), 
  3);
  __d("Parent", ["CSS", ], (function (a, b, c, d, e, f) 
  {
    var g = b('CSS'), h = {
      byTag : (function (i, j) 
      {
        j = j.toUpperCase();
        while(i && i.nodeName != j)
          i = i.parentNode;
        return i;
      }),
      byClass : (function (i, j) 
      {
        while(i && ! g.hasClass(i, j))
          i = i.parentNode;
        return i;
      }),
      byAttribute : (function (i, j) 
      {
        while(i && (! i.getAttribute || ! i.getAttribute(j)))
          i = i.parentNode;
        return i;
      })
    };
    e.exports = h;
  }));
  __d("legacy:parent", ["Parent", ], (function (a, b, c, d) 
  {
    a.Parent = b('Parent');
  }), 
  3);
  __d("legacy:emptyFunction", ["emptyFunction", ], (function (a, b, c, d) 
  {
    a.emptyFunction = b('emptyFunction');
  }), 
  3);
  __d("isEmpty", [], (function (a, b, c, d, e, f) 
  {
    function g(h) 
    {
      if(Array.isArray(h))
      {
        return h.length === 0;
      }
      else
        if(typeof h === 'object')
        {
          for(var i in h)
            return false;
          return true;
        }
        else
          return ! h;
    }
    e.exports = g;
  }));
  __d("CSSLoader", ["isEmpty", ], (function (a, b, c, d, e, f) 
  {
    var g = b('isEmpty'), h = 20, i = 5000, j, k, l = {
      
    }, 
    m = [], 
    n, 
    o = {
      
    };
    function p(t) 
    {
      if(k)
        return;
      k = true;
      var u = document.createElement('link');
      u.onload = (function () 
      {
        j = true;
        u.parentNode.removeChild(u);
      });
      u.rel = 'stylesheet';
      u.href = 'data:text/css;base64,';
      t.appendChild(u);
    }
    function q() 
    {
      var t, u = [], v = [];
      if(Date.now() >= n)
      {
        for(t in o)
        {
          v.push(o[t].signal);
          u.push(o[t].error);
        }
        o = {
          
        };
      }
      else
        for(t in o)
        {
          var w = o[t].signal, x = window.getComputedStyle ? getComputedStyle(w, null) : w.currentStyle;
          if(x && parseInt(x.height, 10) > 1)
          {
            u.push(o[t].load);
            v.push(w);
            delete o[t];
          }
        }
      for(var y = 0;y < v.length;y ++)
        v[y].parentNode.removeChild(v[y]);
      if(! g(u))
      {
        for(y = 0;y < u.length;y ++)
          u[y]();
        n = Date.now() + i;
      }
      return g(o);
    }
    function r(t, u, v, w) 
    {
      var x = document.createElement('meta');
      x.id = 'bootloader_' + t.replace(/[^a-z0-9]/ig, '_');
      u.appendChild(x);
      var y = ! g(o);
      n = Date.now() + i;
      o[t] = {
        signal : x,
        load : v,
        error : w
      };
      if(! y)
        var z = setInterval((function aa() 
        {
          if(q())
            clearInterval(z);
        }), 
        h, 
        false);
    }
    var s = {
      loadStyleSheet : (function (t, u, v, w, x) 
      {
        if(l[t])
          throw new Error('CSS component ' + t + ' has already been requested.');
        if(document.createStyleSheet)
        {
          var y;
          for(var z = 0;z < m.length;z ++)
            if(m[z].imports.length < 31)
            {
              y = z;
              break;
            }
          if(y === undefined)
          {
            m.push(document.createStyleSheet());
            y = m.length - 1;
          }
          m[y].addImport(u);
          l[t] = {
            styleSheet : m[y],
            uri : u
          };
          r(t, v, w, x);
          return;
        }
        var aa = document.createElement('link');
        aa.rel = 'stylesheet';
        aa.type = 'text/css';
        aa.href = u;
        l[t] = {
          link : aa
        };
        if(j)
        {
          aa.onload = (function () 
          {
            aa.onload = aa.onerror = null;
            w();
          });
          aa.onerror = (function () 
          {
            aa.onload = aa.onerror = null;
            x();
          });
        }
        else
        {
          r(t, v, w, x);
          if(j === undefined)
            p(v);
        }
        v.appendChild(aa);
      }),
      registerLoadedStyleSheet : (function (t, u) 
      {
        if(l[t])
          throw new Error('CSS component ' + t + ' has been requested and should not be ' + 'loaded more than once.');
        l[t] = {
          link : u
        };
      }),
      unloadStyleSheet : (function (t) 
      {
        if(! t in l)
          return;
        var u = l[t], v = u.link;
        if(v)
        {
          v.onload = v.onerror = null;
          v.parentNode.removeChild(v);
        }
        else
        {
          var w = u.styleSheet;
          for(var x = 0;x < w.imports.length;x ++)
            if(w.imports[x].href == u.uri)
            {
              w.removeImport(x);
              break;
            }
        }
        delete o[t];
        delete l[t];
      })
    };
    e.exports = s;
  }));
  __d("Bootloader", ["CSSLoader", "CallbackDependencyManager", "createArrayFrom", "ErrorUtils", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('CSSLoader'), h = b('CallbackDependencyManager'), 
    i = b('createArrayFrom'), 
    j = b('ErrorUtils'), 
    k = {
      
    }, 
    l = {
      
    }, 
    m = {
      
    }, 
    n = null, 
    o = {
      
    }, 
    p = {
      
    }, 
    q = {
      
    }, 
    r = {
      
    }, 
    s = false, 
    t = [], 
    u = new h(), 
    v = [];
    j.addListener((function (ca) 
    {
      ca.loadingUrls = Object.keys(p);
    }), 
    true);
    function w(ca, da, ea, fa) 
    {
      var ga = ba.done.bind(null, [ea, ], ca === 'css', da);
      p[da] = Date.now();
      if(ca == 'js')
      {
        var ha = document.createElement('script');
        ha.src = da;
        ha.async = true;
        var ia = o[ea];
        if(ia && ia.crossOrigin)
          ha.crossOrigin = 'anonymous';
        ha.onload = ga;
        ha.onerror = (function () 
        {
          q[da] = true;
          ga();
        });
        ha.onreadystatechange = (function () 
        {
          if(this.readyState in {
            loaded : 1,
            complete : 1
          })
            ga();
        });
        fa.appendChild(ha);
      }
      else
        if(ca == 'css')
          g.loadStyleSheet(ea, da, fa, ga, (function () 
          {
            q[da] = true;
            ga();
          }));
    }
    function x(ca) 
    {
      __t([ca, 'string', 'name', ]);
      if(! o[ca])
        return;
      if(o[ca].type == 'css')
      {
        g.unloadStyleSheet(ca);
        delete k[ca];
        u.unsatisfyPersistentDependency(ca);
      }
    }
    __w(x, {
      signature : "function(string)"
    });
    function y(ca, da) 
    {
      __t([ca, 'array<string>|string', 'components', ], [da, 'function|number', 'callback', ]);
      if(! s)
      {
        t.push([ca, da, ]);
        return;
      }
      ca = i(ca);
      var ea = [];
      for(var fa = 0;fa < ca.length;++ fa)
      {
        if(! ca[fa])
          continue;
        var ga = m[ca[fa]];
        if(ga)
        {
          var ha = ga.resources;
          for(var ia = 0;ia < ha.length;++ ia)
            ea.push(ha[ia]);
        }
      }
      ba.loadResources(ea, da);
    }
    __w(y, {
      signature : "function(array<string>|string,function|number)"
    });
    function z(ca) 
    {
      __t([ca, 'string|array', 'resourceIDs', ]);
      ca = i(ca);
      for(var da = 0;da < ca.length;++ da)
        if(ca[da] !== undefined)
          k[ca[da]] = true;
    }
    __w(z, {
      signature : "function(string|array)"
    });
    function aa(ca) 
    {
      __t([ca, 'array<string>|array<object>', 'resources', ]);
      return __t([(function () 
      {
        if(! ca)
          return [];
        var da = [];
        for(var ea = 0;ea < ca.length;++ ea)
          if(typeof ca[ea] == 'string')
          {
            if(ca[ea] in o)
              da.push(o[ca[ea]]);
          }
          else
            da.push(ca[ea]);
        return da;
      }).apply(this, arguments), 'array<object>', ]);
    }
    __w(aa, {
      signature : "function(array<string>|array<object>):array<object>"
    });
    var ba = {
      configurePage : (__w((function (ca) 
      {
        __t([ca, 'array<string>', 'css_resources', ]);
        var da = {
          
        }, ea = aa(ca), fa;
        for(fa = 0;fa < ea.length;fa ++)
        {
          da[ea[fa].src] = ea[fa];
          z(ea[fa].name);
        }
        var ga = document.getElementsByTagName('link');
        for(fa = 0;fa < ga.length;++ fa)
        {
          if(ga[fa].rel != 'stylesheet')
            continue;
          for(var ha in da)
            if(ga[fa].href.indexOf(ha) !== - 1)
            {
              var ia = da[ha].name;
              if(da[ha].permanent)
                l[ia] = true;
              delete da[ha];
              g.registerLoadedStyleSheet(ia, ga[fa]);
              ba.done([ia, ], true);
              break;
            }
        }
      }), 
      {
        signature : "function(array<string>)"
      })),
      loadComponents : (function (ca, da) 
      {
        ca = i(ca);
        var ea = [], fa = [];
        for(var ga = 0;ga < ca.length;ga ++)
        {
          var ha = m[ca[ga]];
          if(ha && ! ha.module)
            continue;
          var ia = 'legacy:' + ca[ga];
          if(m[ia])
          {
            ca[ga] = ia;
            ea.push(ia);
          }
          else
            if(ha && ha.module)
            {
              ea.push(ca[ga]);
              if(! ha.runWhenReady)
                fa.push(ca[ga]);
            }
        }
        y(ca, ea.length ? d.bind(null, ea, da) : da);
      }),
      loadModules : (function (ca, da) 
      {
        var ea = [], fa = [];
        for(var ga = 0;ga < ca.length;ga ++)
        {
          var ha = m[ca[ga]];
          if(! ha || ha.module)
            ea.push(ca[ga]);
        }
        y(ca, d.bind(null, ea, da));
      }),
      loadResources : (function (ca, da, ea, fa) 
      {
        var ga;
        ca = aa(i(ca));
        if(ea)
        {
          var ha = {
            
          };
          for(ga = 0;ga < ca.length;++ ga)
            ha[ca[ga].name] = true;
          for(var ia in k)
            if(! (ia in l) && ! (ia in ha) && ! (ia in r))
              x(ia);
          r = {
            
          };
        }
        var ja = [], ka = [];
        for(ga = 0;ga < ca.length;++ ga)
        {
          var la = ca[ga];
          if(la.permanent)
            l[la.name] = true;
          if(u.isPersistentDependencySatisfied(la.name))
            continue;
          if(! la.nonblocking)
            ka.push(la.name);
          if(! k[la.name])
          {
            z(la.name);
            ja.push(la);
            window.CavalryLogger && window.CavalryLogger.getInstance().measureResources(la, fa);
          }
        }
        var ma;
        if(da)
          if(typeof da === 'function')
          {
            ma = u.registerCallback(da, ka);
          }
          else
            ma = u.addDependenciesToExistingCallback(da, ka);
        var na = document.documentMode || + (/MSIE.(\d+)/.exec(navigator.userAgent) || [])[1], 
        oa = ba.getHardpoint(), 
        pa = na ? oa : document.createDocumentFragment();
        for(ga = 0;ga < ja.length;++ ga)
          w(ja[ga].type, ja[ga].src, ja[ga].name, pa);
        if(oa !== pa)
          oa.appendChild(pa);
        return ma;
      }),
      requestJSResource : (__w((function (ca) 
      {
        __t([ca, 'string', 'source', ]);
        var da = ba.getHardpoint();
        w('js', ca, null, da);
      }), 
      {
        signature : "function(string)"
      })),
      done : (function (ca, da, ea) 
      {
        if(ea)
          delete p[ea];
        z(ca);
        if(! da)
          for(var fa = 0, ga = v.length;fa < ga;fa ++)
            v[fa]();
        for(var ha = 0;ha < ca.length;++ ha)
        {
          var ia = ca[ha];
          if(ia !== undefined)
            u.satisfyPersistentDependency(ia);
        }
      }),
      subscribeToLoadedResources_DEPRECATED : (function (ca) 
      {
        v.push(ca);
      }),
      enableBootload : (function (ca) 
      {
        for(var da in ca)
          if(! m[da])
            m[da] = ca[da];
        if(! s)
        {
          s = true;
          for(var ea = 0;ea < t.length;ea ++)
            y.apply(null, t[ea]);
          t = [];
        }
      }),
      getHardpoint : (function () 
      {
        if(! n)
        {
          var ca = document.getElementsByTagName('head');
          n = ca.length && ca[0] || document.body;
        }
        return n;
      }),
      setResourceMap : (__w((function (ca) 
      {
        __t([ca, 'object', 'resources', ]);
        for(var da in ca)
          if(! o[da])
          {
            ca[da].name = da;
            o[da] = ca[da];
          }
      }), 
      {
        signature : "function(object)"
      })),
      loadEarlyResources : (__w((function (ca) 
      {
        __t([ca, 'object', 'resources', ]);
        ba.setResourceMap(ca);
        var da = [];
        for(var ea in ca)
        {
          var fa = o[ea];
          da.push(fa);
          if(! fa.permanent)
            r[fa.name] = fa;
        }
        ba.loadResources(da);
      }), 
      {
        signature : "function(object)"
      })),
      getLoadingUrls : (function () 
      {
        var ca = {
          
        }, da = Date.now();
        for(var ea in p)
          ca[ea] = da - p[ea];
        return ca;
      }),
      getErrorUrls : (function () 
      {
        return Object.keys(q);
      })
    };
    e.exports = ba;
  }));
  __d("BlueBarController", ["Bootloader", "CSS", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Bootloader'), h = b('CSS');
    f.init = (function (i) 
    {
      if('getBoundingClientRect' in i)
      {
        var j = (function () 
        {
          var k = i.getBoundingClientRect(), l = Math.round(k.top) - document.documentElement.clientTop;
          h.conditionClass(i.firstChild, 'fixed_elem', l <= 0);
        });
        j();
        g.loadModules(['Event', ], (function (k) 
        {
          k.listen(window, 'scroll', j);
        }));
      }
    });
  }));
  __d("legacy:arbiter", ["Arbiter", ], (function (a, b, c, d) 
  {
    a.Arbiter = b('Arbiter');
  }), 
  3);
  __d("event-form-bubbling", [], (function (a, b, c, d, e, f) 
  {
    a.Event = a.Event || (function () 
    {
      
    });
    a.Event.__inlineSubmit = (function (g, event) 
    {
      var h = (a.Event.__getHandler && a.Event.__getHandler(g, 'submit'));
      return h ? null : a.Event.__bubbleSubmit(g, event);
    });
    a.Event.__bubbleSubmit = (function (g, event) 
    {
      if(document.documentElement.attachEvent)
      {
        var h;
        while(h !== false && (g = g.parentNode))
          h = g.onsubmit ? g.onsubmit(event) : a.Event.__fire && a.Event.__fire(g, 'submit', event);
        return h;
      }
    });
  }), 
  3);
  __d("OnloadEvent", [], (function (a, b, c, d, e, f) 
  {
    var g = {
      ONLOAD : 'onload/onload',
      ONLOAD_CALLBACK : 'onload/onload_callback',
      ONLOAD_DOMCONTENT : 'onload/dom_content_ready',
      ONLOAD_DOMCONTENT_CALLBACK : 'onload/domcontent_callback',
      ONBEFOREUNLOAD : 'onload/beforeunload',
      ONUNLOAD : 'onload/unload'
    };
    e.exports = g;
  }));
  __d("Run", ["Arbiter", "OnloadEvent", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter'), h = b('OnloadEvent'), i = 'onunloadhooks', 
    j = 'onafterunloadhooks', 
    k = g.BEHAVIOR_STATE;
    function l(ba) 
    {
      var ca = a.CavalryLogger;
      ca && ca.getInstance().setTimeStamp(ba);
    }
    function m() 
    {
      return ! window.loading_page_chrome;
    }
    function n(ba) 
    {
      var ca = a.OnloadHooks;
      if(window.loaded && ca)
      {
        ca.runHook(ba, 'onlateloadhooks');
      }
      else
        u('onloadhooks', ba);
    }
    function o(ba) 
    {
      var ca = a.OnloadHooks;
      if(window.afterloaded && ca)
      {
        setTimeout((function () 
        {
          ca.runHook(ba, 'onlateafterloadhooks');
        }), 
        0);
      }
      else
        u('onafterloadhooks', ba);
    }
    function p(ba, ca) 
    {
      if(ca === undefined)
        ca = m();
      ca ? u('onbeforeleavehooks', ba) : u('onbeforeunloadhooks', ba);
    }
    function q(ba, ca) 
    {
      if(! window.onunload)
        window.onunload = (function () 
        {
          g.inform(h.ONUNLOAD, true, k);
        });
      u(ba, ca);
    }
    function r(ba) 
    {
      q(i, ba);
    }
    function s(ba) 
    {
      q(j, ba);
    }
    function t(ba) 
    {
      u('onleavehooks', ba);
    }
    function u(ba, ca) 
    {
      window[ba] = (window[ba] || []).concat(ca);
    }
    function v(ba) 
    {
      window[ba] = [];
    }
    function w() 
    {
      g.inform(h.ONLOAD_DOMCONTENT, true, k);
    }
    a._domcontentready = w;
    function x() 
    {
      var ba = document, ca = window;
      if(ba.addEventListener)
      {
        var da = /AppleWebKit.(\d+)/.exec(navigator.userAgent);
        if(da && da[1] < 525)
        {
          var ea = setInterval((function () 
          {
            if(/loaded|complete/.test(ba.readyState))
            {
              w();
              clearInterval(ea);
            }
          }), 
          10);
        }
        else
          ba.addEventListener("DOMContentLoaded", w, true);
      }
      else
      {
        var fa = 'javascript:void(0)';
        if(ca.location.protocol == 'https:')
          fa = '//:';
        ba.write('<script onreadystatechange="if (this.readyState==\'complete\') {' + 'this.parentNode.removeChild(this);_domcontentready();}" ' + 'defer="defer" src="' + fa + '"><\/script\>');
      }
      var ga = ca.onload;
      ca.onload = (function () 
      {
        l('t_layout');
        ga && ga();
        g.inform(h.ONLOAD, true, k);
      });
      ca.onbeforeunload = (function () 
      {
        var ha = {
          
        };
        g.inform(h.ONBEFOREUNLOAD, ha, k);
        if(! ha.warn)
          g.inform('onload/exit', true);
        return ha.warn;
      });
    }
    var y = g.registerCallback((function () 
    {
      l('t_onload');
      g.inform(h.ONLOAD_CALLBACK, true, k);
    }), 
    [h.ONLOAD, ]), 
    z = g.registerCallback((function () 
    {
      l('t_domcontent');
      var ba = {
        timeTriggered : Date.now()
      };
      g.inform(h.ONLOAD_DOMCONTENT_CALLBACK, ba, k);
    }), 
    [h.ONLOAD_DOMCONTENT, ]);
    x();
    var aa = {
      onLoad : n,
      onAfterLoad : o,
      onLeave : t,
      onBeforeUnload : p,
      onUnload : r,
      onAfterUnload : s,
      __domContentCallback : z,
      __onloadCallback : y,
      __removeHook : v
    };
    e.exports = aa;
  }));
  __d("legacy:onload", ["Run", "OnloadEvent", ], (function (a, b, c, d) 
  {
    var e = b('Run');
    a.OnloadEvent = b('OnloadEvent');
    a.onloadRegister_DEPRECATED = e.onLoad;
    a.onloadRegister = (function () 
    {
      return e.onLoad.apply(this, arguments);
    });
    a.onafterloadRegister_DEPRECATED = e.onAfterLoad;
    a.onafterloadRegister = (function () 
    {
      return e.onAfterLoad.apply(this, arguments);
    });
    a.onleaveRegister = e.onLeave;
    a.onbeforeunloadRegister = e.onBeforeUnload;
    a.onunloadRegister = e.onUnload;
  }), 
  3);
  __d("wait_for_load", ["Bootloader", "Run", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Bootloader'), h = b('Run');
    function i(l, m) 
    {
      return window.loaded && m.call(l);
    }
    function j(l, m, n) 
    {
      g.loadComponents.call(g, m, n.bind(l));
      return false;
    }
    function k(l, m, n) 
    {
      n = n.bind(l, m);
      if(window.loaded)
        return n();
      switch((m || event).type){
        case 'load':
          

        case 'focus':
          h.onAfterLoad(n);
          return;

        case 'click':
          var o = l.style, p = document.body.style;
          o.cursor = p.cursor = 'progress';
          h.onAfterLoad((function () 
          {
            o.cursor = p.cursor = '';
            if(l.tagName.toLowerCase() == 'a')
            {
              if(false !== n() && l.href)
                window.location.href = l.href;
            }
            else
              if(l.click)
                l.click();
          }));
          break;

        
      }
      return false;
    }
    a.run_if_loaded = i;
    a.run_with = j;
    a.wait_for_load = k;
  }), 
  3);
  __d("markJSEnabled", [], (function (a, b, c, d, e, f) 
  {
    var g = document.documentElement;
    g.className = g.className.replace('no_js', '');
  }));
  __d("JSCC", [], (function (a, b, c, d, e, f) 
  {
    var g = {
      
    };
    function h(j) 
    {
      var k, l = false;
      return (function () 
      {
        if(! l)
        {
          k = j();
          l = true;
        }
        return k;
      });
    }
    var i = {
      get : (function (j) 
      {
        if(! g[j])
          throw new Error('JSCC entry is missing');
        return g[j]();
      }),
      init : (function (j) 
      {
        for(var k in j)
          g[k] = h(j[k]);
        return (function l() 
        {
          for(var m in j)
            delete g[m];
        });
      })
    };
    e.exports = i;
  }));
  __d("PageletSet", ["Arbiter", "copyProperties", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter'), h = b('copyProperties'), i = {
      
    }, 
    j = {
      hasPagelet : (function (m) 
      {
        return i.hasOwnProperty(m);
      }),
      getPagelet : (function (m) 
      {
        return i[m];
      }),
      getOrCreatePagelet : (function (m) 
      {
        if(! j.hasPagelet(m))
        {
          var n = new l(m);
          i[m] = n;
        }
        return j.getPagelet(m);
      }),
      getPageletIDs : (function () 
      {
        return Object.keys(i);
      }),
      removePagelet : (function (m) 
      {
        if(j.hasPagelet(m))
        {
          i[m].destroy();
          delete i[m];
        }
      })
    };
    function k(m, n) 
    {
      return m.contains ? m.contains(n) : m.compareDocumentPosition(n) & 16;
    }
    function l(m) 
    {
      this.id = m;
      this._root = null;
      this._destructors = [];
      this.addDestructor((function n() 
      {
        g.inform('pagelet/destroy', {
          id : this.id,
          root : this._root
        });
      }).bind(this));
    }
    h(l.prototype, {
      setRoot : (function (m) 
      {
        this._root = m;
      }),
      _getDescendantPagelets : (function () 
      {
        var m = [];
        if(! this._root)
          return m;
        var n = j.getPageletIDs();
        for(var o = 0;o < n.length;o ++)
        {
          var p = n[o];
          if(p === this.id)
            continue;
          var q = i[p];
          if(q._root && k(this._root, q._root))
            m.push(q);
        }
        return m;
      }),
      addDestructor : (function (m) 
      {
        this._destructors.push(m);
      }),
      destroy : (function () 
      {
        var m = this._getDescendantPagelets();
        for(var n = 0;n < m.length;n ++)
        {
          var o = m[n];
          if(j.hasPagelet(o.id))
            j.removePagelet(o.id);
        }
        for(n = 0;n < this._destructors.length;n ++)
          this._destructors[n]();
        if(this._root)
          while(this._root.firstChild)
            this._root.removeChild(this._root.firstChild);
      })
    });
    e.exports = j;
  }));
  __d("repeatString", ["invariant", ], (function (a, b, c, d, e, f) 
  {
    var g = b('invariant');
    function h(i, j) 
    {
      if(j === 1)
        return i;
      g(j >= 0);
      var k = '';
      while(j)
      {
        if(j & 1)
          k += i;
        if((j >>= 1))
          i += i;
      }
      return k;
    }
    e.exports = h;
  }));
  __d("BitMap", ["copyProperties", "repeatString", ], (function (a, b, c, d, e, f) 
  {
    var g = b('copyProperties'), h = b('repeatString'), i = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_';
    function j() 
    {
      this._bits = [];
    }
    g(j.prototype, {
      set : (function (m) 
      {
        this._bits[m] = 1;
        return this;
      }),
      toString : (function () 
      {
        var m = [];
        for(var n = 0;n < this._bits.length;n ++)
          m.push(this._bits[n] ? 1 : 0);
        return m.length ? l(m.join('')) : '';
      }),
      toCompressedString : (function () 
      {
        if(this._bits.length === 0)
          return '';
        var m = [], n = 1, o = this._bits[0] || 0, p = o.toString(2);
        for(var q = 1;q < this._bits.length;q ++)
        {
          var r = this._bits[q] || 0;
          if(r === o)
          {
            n ++;
          }
          else
          {
            m.push(k(n));
            o = r;
            n = 1;
          }
        }
        if(n)
          m.push(k(n));
        return l(p + m.join(''));
      })
    });
    function k(m) 
    {
      var n = m.toString(2), o = h('0', n.length - 1);
      return o + n;
    }
    function l(m) 
    {
      var n = (m + '00000').match(/[01]{6}/g), o = '';
      for(var p = 0;p < n.length;p ++)
        o += i[parseInt(n[p], 2)];
      return o;
    }
    e.exports = j;
  }));
  __d("ServerJS", ["BitMap", "ErrorUtils", "copyProperties", "ge", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('BitMap'), h = b('ErrorUtils'), i = b('copyProperties'), 
    j = b('ge'), 
    k = 0, 
    l = new g();
    function m() 
    {
      this._moduleMap = {
        
      };
      this._relativeTo = null;
      this._moduleIDsToCleanup = {
        
      };
    }
    m.getLoadedModuleHash = (function () 
    {
      return l.toCompressedString();
    });
    i(m.prototype, {
      handle : (function (q) 
      {
        if(q.__guard)
          throw new Error('ServerJS.handle called on data that has already been handled');
        q.__guard = true;
        n(q.define || [], this._handleDefine, this);
        n(q.markup || [], this._handleMarkup, this);
        n(q.elements || [], this._handleElement, this);
        n(q.instances || [], this._handleInstance, this);
        var r = n(q.require || [], this._handleRequire, this);
        return {
          cancel : (function () 
          {
            for(var s = 0;s < r.length;s ++)
              if(r[s])
                r[s].cancel();
          })
        };
      }),
      handlePartial : (function (q) 
      {
        (q.instances || []).forEach(o.bind(null, this._moduleMap, 3));
        (q.markup || []).forEach(o.bind(null, this._moduleMap, 2));
        return this.handle(q);
      }),
      setRelativeTo : (function (q) 
      {
        this._relativeTo = q;
        return this;
      }),
      cleanup : (function () 
      {
        var q = [];
        for(var r in this._moduleMap)
          q.push(r);
        d.call(null, q, p);
        this._moduleMap = {
          
        };
        function s(u) 
        {
          var v = this._moduleIDsToCleanup[u], w = v[0], x = v[1];
          delete this._moduleIDsToCleanup[u];
          var y = x ? 'JS::call("' + w + '", "' + x + '", ...)' : 'JS::requireModule("' + w + '")', 
          z = y + ' did not fire because it has missing dependencies.';
          throw new Error(z);
        }
        for(var t in this._moduleIDsToCleanup)
          h.applyWithGuard(s, this, [t, ]);
      }),
      _handleDefine : (function (q, r, s, t) 
      {
        if(t >= 0)
          l.set(t);
        define(q, r, (function () 
        {
          this._replaceTransportMarkers(s);
          return s;
        }).bind(this));
      }),
      _handleRequire : (function (q, r, s, t) 
      {
        var u = [q, ].concat(s || []), v = (r ? '__call__' : '__requireModule__') + k ++;
        this._moduleIDsToCleanup[v] = [q, r, ];
        return define(v, u, (function (w) 
        {
          delete this._moduleIDsToCleanup[v];
          t && this._replaceTransportMarkers(t);
          if(r)
          {
            if(! w[r])
              throw new TypeError('Module ' + q + ' has no method ' + r);
            w[r].apply(w, t || []);
          }
        }), 
        1, 
        this, 
        1);
      }),
      _handleInstance : (function (q, r, s, t) 
      {
        var u = null;
        if(r)
          u = (function (v) 
          {
            this._replaceTransportMarkers(s);
            var w = Object.create(v.prototype);
            v.apply(w, s);
            return w;
          }).bind(this);
        define(q, r, u, 0, null, t);
      }),
      _handleMarkup : (function (q, r, s) 
      {
        define(q, ['HTML', ], (function (t) 
        {
          return t.replaceJSONWrapper(r).getRootNode();
        }), 
        0, 
        null, 
        s);
      }),
      _handleElement : (function (q, r, s, t) 
      {
        var u = [], v = 0;
        if(t)
        {
          u.push(t);
          v = 1;
          s ++;
        }
        define(q, u, (function (w) 
        {
          var x = j(r, w);
          if(! x)
          {
            var y = 'Could not find element ' + r;
            throw new Error(y);
          }
          return x;
        }), 
        v, 
        null, 
        s);
      }),
      _replaceTransportMarkers : (function (q, r) 
      {
        var s = (typeof r !== 'undefined') ? q[r] : q, t;
        if(Array.isArray(s))
        {
          for(t = 0;t < s.length;t ++)
            this._replaceTransportMarkers(s, t);
        }
        else
          if(s && typeof s == 'object')
            if(s.__m)
            {
              q[r] = b.call(null, s.__m);
            }
            else
              if(s.__e)
              {
                q[r] = j(s.__e);
              }
              else
                if(s.__rel)
                {
                  q[r] = this._relativeTo;
                }
                else
                  for(var u in s)
                    this._replaceTransportMarkers(s, u);
      })
    });
    function n(q, r, s) 
    {
      return q.map((function (t) 
      {
        return h.applyWithGuard(r, s, t);
      }));
    }
    function o(q, r, s) 
    {
      var t = s[0];
      if(! (t in q))
        s[r] = (s[r] || 0) + 1;
      q[t] = true;
    }
    function p() 
    {
      return {
        
      };
    }
    e.exports = m;
  }));
  __d("invokeCallbacks", ["ErrorUtils", ], (function (a, b, c, d, e, f) 
  {
    var g = b('ErrorUtils');
    function h(i, j) 
    {
      if(i)
        for(var k = 0;k < i.length;k ++)
          g.applyWithGuard(new Function(i[k]), j);
    }
    e.exports = h;
  }));
  __d("ix", ["copyProperties", ], (function (a, b, c, d, e, f) 
  {
    var g = b('copyProperties'), h = {
      
    };
    function i(j) 
    {
      return h[j];
    }
    i.add = g.bind(null, h);
    e.exports = i;
  }));
  __d("BigPipe", ["Arbiter", "Bootloader", "Env", "ErrorUtils", "JSCC", "OnloadEvent", "PageletSet", "Run", "ServerJS", "$", "copyProperties", "ge", "invokeCallbacks", "ix", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter'), h = b('Bootloader'), i = b('Env'), 
    j = b('ErrorUtils'), 
    k = b('JSCC'), 
    l = b('OnloadEvent'), 
    m = b('PageletSet'), 
    n = b('Run'), 
    o = b('ServerJS'), 
    p = b('$'), 
    q = b('copyProperties'), 
    r = b('ge'), 
    s = b('invokeCallbacks'), 
    t = b('ix'), 
    u = document.documentMode || + (/MSIE.(\d+)/.exec(navigator.userAgent) || [])[1], 
    v = g.BEHAVIOR_STATE, 
    w = g.BEHAVIOR_PERSISTENT;
    function x(ba) 
    {
      q(this, {
        arbiter : g,
        rootNodeID : 'content',
        lid : 0,
        isAjax : false,
        domContentCallback : n.__domContentCallback,
        onloadCallback : n.__onloadCallback,
        domContentEvt : l.ONLOAD_DOMCONTENT_CALLBACK,
        onloadEvt : l.ONLOAD_CALLBACK,
        forceFinish : false,
        _phaseDoneCallbacks : [],
        _currentPhase : 0,
        _lastPhase : - 1,
        _livePagelets : {
          
        }
      });
      q(this, ba);
      if(this.automatic)
      {
        this._relevant_instance = x._current_instance;
      }
      else
        x._current_instance = this;
      this._serverJS = new o();
      g.inform('BigPipe/init', {
        lid : this.lid,
        arbiter : this.arbiter
      }, 
      w);
      this.arbiter.registerCallback(this.domContentCallback, ['pagelet_displayed_all', ]);
      this._informEventExternal('phase_begin', {
        phase : 0
      });
      this.arbiter.inform('phase_begin_0', true, v);
      this.onloadCallback = this.arbiter.registerCallback(this.onloadCallback, ['pagelet_displayed_all', ]);
      this.arbiter.registerCallback(this._serverJS.cleanup.bind(this._serverJS), [this.onloadEvt, ]);
    }
    x.getCurrentInstance = (function () 
    {
      return x._current_instance;
    });
    q(x.prototype, {
      onPageletArrive : j.guard((function (ba) 
      {
        this._informPageletEvent('arrive', ba.id, ba.phase);
        ba.content = ba.content || {
          
        };
        var ca = ba.phase;
        if(! this._phaseDoneCallbacks[ca])
          this._phaseDoneCallbacks[ca] = this.arbiter.registerCallback(this._onPhaseDone.bind(this), ['phase_complete_' + ca, ]);
        this.arbiter.registerCallback(this._phaseDoneCallbacks[ca], [ba.id + '_displayed', ]);
        var da = this._getPageletRootID(ba), ea = m.getOrCreatePagelet(da);
        if(ba.the_end)
          this._lastPhase = ca;
        if(ba.tti_phase !== undefined)
          this._ttiPhase = ba.tti_phase;
        if(ba.is_second_to_last_phase)
          this._secondToLastPhase = ca;
        this._livePagelets[ea.id] = true;
        ea.addDestructor((function () 
        {
          delete this._livePagelets[ea.id];
        }).bind(this));
        if(ba.jscc_map)
        {
          var fa = (eval)(ba.jscc_map), ga = k.init(fa);
          ea.addDestructor(ga);
        }
        if(ba.resource_map)
          h.setResourceMap(ba.resource_map);
        if(ba.bootloadable)
          h.enableBootload(ba.bootloadable);
        t.add(ba.ixData);
        this._informPageletEvent('setup', ba.id);
        var ha = new g();
        ha.registerCallback(this._displayPageletHandler.bind(this, ba), ['preceding_pagelets_displayed', 'display_resources_downloaded', ]);
        var ia = ba.display_dependency || [], ja = ia.map((function (la) 
        {
          return la + '_displayed';
        }));
        this.arbiter.registerCallback((function () 
        {
          ha.inform('preceding_pagelets_displayed');
        }), 
        ja);
        this.arbiter.registerCallback((function () 
        {
          this._informPageletEvent('css', ba.id);
          var la = (ba.css || []).concat(ba.displayJS || []);
          h.loadResources(la, (function () 
          {
            this._informPageletEvent('css_load', ba.id);
            ha.inform('display_resources_downloaded');
          }).bind(this), 
          false, 
          ba.id);
        }).bind(this), 
        ['phase_begin_' + ca, ]);
        this.arbiter.registerCallback(this.onloadCallback, ['pagelet_onload', ]);
        var ka = [ba.id + '_displayed', ];
        if(! this.jsNonBlock)
          ka.push(this.domContentEvt);
        this.arbiter.registerCallback(this._downloadJsForPagelet.bind(this, ba), ka);
        if(ba.is_last)
          this._endPhase(ca);
      })),
      _beginPhase : (function (ba) 
      {
        this._informEventExternal('phase_begin', {
          phase : ba
        });
        this.arbiter.inform('phase_begin_' + ba, true, v);
      }),
      _endPhase : (function (ba) 
      {
        this.arbiter.inform('phase_complete_' + ba, true, v);
      }),
      _displayPageletHandler : (function (ba) 
      {
        if(this.displayCallback)
        {
          this.displayCallback(this._displayPagelet.bind(this, ba));
        }
        else
          this._displayPagelet(ba);
      }),
      _displayPagelet : (function (ba) 
      {
        this._informPageletEvent('display_start', ba.id);
        var ca = this._getPagelet(ba);
        for(var da in ba.content)
        {
          var ea = ba.content[da];
          if(ba.append)
            da = this._getPageletRootID(ba);
          var fa = r(da);
          if(! fa)
            continue;
          if(da === ca.id)
            ca.setRoot(fa);
          ea = y(ea);
          if(ea)
            if(ba.append || u < 8)
            {
              if(! ba.append)
                while(fa.firstChild)
                  fa.removeChild(fa.firstChild);
              aa(fa, ea);
            }
            else
              fa.innerHTML = ea;
          var ga = fa.getAttribute('data-referrer');
          if(! ga)
            fa.setAttribute('data-referrer', da);
          if(ba.cache_hit && i.pc_debug)
            fa.style.border = '1px red solid';
        }
        if(ba.jsmods)
        {
          var ha = JSON.parse(JSON.stringify(ba.jsmods)), ia = this._serverJS.handlePartial(ha);
          ca.addDestructor(ia.cancel.bind(ia));
        }
        this._informPageletEvent('display', ba.id);
        this.arbiter.inform(ba.id + '_displayed', true, v);
      }),
      _onPhaseDone : (function () 
      {
        if(this._currentPhase === this._ttiPhase)
          this._informEventExternal('tti_bigpipe', {
            phase : this._ttiPhase
          });
        if(this._currentPhase === this._lastPhase && this._isRelevant())
          this.arbiter.inform('pagelet_displayed_all', true, v);
        this._currentPhase ++;
        if(u <= 8)
        {
          setTimeout(this._beginPhase.bind(this, this._currentPhase), 20);
        }
        else
          this._beginPhase(this._currentPhase);
      }),
      _downloadJsForPagelet : (function (ba) 
      {
        this._informPageletEvent('jsstart', ba.id);
        h.loadResources(ba.js || [], (function () 
        {
          this._informPageletEvent('jsdone', ba.id);
          ba.requires = ba.requires || [];
          if(! this.isAjax || ba.phase >= 1)
            ba.requires.push('uipage_onload');
          var ca = (function () 
          {
            this._informPageletEvent('preonload', ba.id);
            if(this._isRelevantPagelet(ba))
              s(ba.onload);
            this._informPageletEvent('onload', ba.id);
            this.arbiter.inform('pagelet_onload', true, g.BEHAVIOR_EVENT);
            ba.provides && this.arbiter.inform(ba.provides, true, v);
          }).bind(this), 
          da = (function () 
          {
            this._isRelevantPagelet(ba) && s(ba.onafterload);
          }).bind(this);
          this.arbiter.registerCallback(ca, ba.requires);
          this.arbiter.registerCallback(da, [this.onloadEvt, ]);
        }).bind(this), 
        false, 
        ba.id);
      }),
      _getPagelet : (function (ba) 
      {
        var ca = this._getPageletRootID(ba);
        return m.getPagelet(ca);
      }),
      _getPageletRootID : (function (ba) 
      {
        var ca = ba.append;
        if(ca)
          return (ca === 'bigpipe_root') ? this.rootNodeID : ca;
        return Object.keys(ba.content)[0] || null;
      }),
      _isRelevant : (function () 
      {
        return this == x._current_instance || (this.automatic && this._relevant_instance == x._current_instance) || this.jsNonBlock || this.forceFinish;
      }),
      _isRelevantPagelet : (function (ba) 
      {
        if(! this._isRelevant())
          return false;
        var ca = this._getPageletRootID(ba);
        return ! ! this._livePagelets[ca];
      }),
      _informEventExternal : (function (ba, ca) 
      {
        ca = ca || {
          
        };
        ca.ts = Date.now();
        ca.lid = this.lid;
        this.arbiter.inform(ba, ca, w);
      }),
      _informPageletEvent : (function (ba, ca, da) 
      {
        var ea = {
          event : ba,
          id : ca
        };
        if(da)
          ea.phase = da;
        this._informEventExternal('pagelet_event', ea);
      })
    });
    function y(ba) 
    {
      if(! ba || typeof ba === 'string')
        return ba;
      if(ba.container_id)
      {
        var ca = p(ba.container_id);
        ba = z(ca) || '';
        ca.parentNode.removeChild(ca);
        return ba;
      }
      return null;
    }
    function z(ba) 
    {
      if(! ba.firstChild)
      {
        h.loadModules(['ErrorSignal', ], (function (da) 
        {
          da.sendErrorSignal('bigpipe', 'Pagelet markup container is empty.');
        }));
        return null;
      }
      if(ba.firstChild.nodeType !== 8)
        return null;
      var ca = ba.firstChild.nodeValue;
      ca = ca.substring(1, ca.length - 1);
      return ca.replace(/\\([\s\S]|$)/g, '$1');
    }
    function aa(ba, ca) 
    {
      var da = document.createElement('div'), ea = u < 7;
      if(ea)
        ba.appendChild(da);
      da.innerHTML = ca;
      var fa = document.createDocumentFragment();
      while(da.firstChild)
        fa.appendChild(da.firstChild);
      ba.appendChild(fa);
      if(ea)
        ba.removeChild(da);
    }
    e.exports = x;
  }));
  __d("legacy:bootloader", ["Bootloader", ], (function (a, b, c, d) 
  {
    a.Bootloader = b('Bootloader');
  }), 
  3);
  __d("Class", ["CallbackDependencyManager", "Bootloader", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('CallbackDependencyManager'), h = b('Bootloader'), 
    i = 'bootload_done', 
    j = false, 
    k = new g(), 
    l = {
      
    }, 
    m = {
      extend : (function (u, v) 
      {
        if(! j)
        {
          h.subscribeToLoadedResources_DEPRECATED(o);
          j = true;
        }
        if(typeof v == 'string')
        {
          n(u, v);
        }
        else
          p(u, v);
      })
    };
    function n(u, v) 
    {
      u.__class_extending = true;
      var w = k.registerCallback(p.bind(null, u, v), [v, i, ]);
      if(w !== null)
        l[v] = true;
    }
    function o() 
    {
      k.satisfyNonPersistentDependency(i);
      for(var u in l)
        if(! ! a[u])
        {
          delete l[u];
          if(! a[u].__class_extending)
          {
            k.satisfyNonPersistentDependency(u);
          }
          else
            a[u].__class_name = u;
        }
    }
    function p(u, v) 
    {
      delete u.__class_extending;
      v = typeof v == 'string' ? a[v] : v;
      var w = q(v, 0), x = q(u, w.prototype.__level + 1);
      x.parent = w;
      if(! ! u.__class_name)
        k.satisfyNonPersistentDependency(u.__class_name);
    }
    function q(u, v) 
    {
      if(u._metaprototype)
        return u._metaprototype;
      var w = new Function();
      w.construct = r;
      w.prototype.construct = t(u, v, true);
      w.prototype.__level = v;
      w.base = u;
      u.prototype.parent = w;
      u._metaprototype = w;
      return w;
    }
    function r(u) 
    {
      s(u.parent);
      var v = [], w = u;
      while(w.parent)
      {
        var x = new w.parent();
        v.push(x);
        x.__instance = u;
        w = w.parent;
      }
      u.parent = v[1];
      v.reverse();
      v.pop();
      u.__parents = v;
      u.__instance = u;
      return u.parent.construct.apply(u.parent, arguments);
    }
    function s(u) 
    {
      if(u.initialized)
        return;
      var v = u.base.prototype;
      if(u.parent)
      {
        s(u.parent);
        var w = u.parent.prototype;
        for(var x in w)
          if(x != '__level' && x != 'construct' && v[x] === undefined)
            v[x] = u.prototype[x] = w[x];
      }
      u.initialized = true;
      var y = u.prototype.__level;
      for(var x in v)
        if(x != 'parent')
          v[x] = u.prototype[x] = t(v[x], y);
    }
    function t(u, v, w) 
    {
      if(typeof u != 'function' || u.__prototyped)
        return u;
      var x = (function () 
      {
        var y = this.__instance;
        if(y)
        {
          var z = y.parent;
          y.parent = v ? y.__parents[v - 1] : null;
          var aa = arguments;
          if(w)
          {
            aa = [];
            for(var ba = 1;ba < arguments.length;ba ++)
              aa.push(arguments[ba]);
          }
          var ca = u.apply(y, aa);
          y.parent = z;
          return ca;
        }
        else
          return u.apply(this, arguments);
      });
      x.__prototyped = true;
      return x;
    }
    e.exports = m;
  }));
  __d("legacy:Class", ["Class", ], (function (a, b, c, d) 
  {
    a.Class = b('Class');
  }), 
  3);
  __d("legacy:constructor-cache", ["JSCC", ], (function (a, b, c, d) 
  {
    a.JSCC = b('JSCC');
  }), 
  3);
  __d("function-extensions", ["createArrayFrom", ], (function (a, b, c, d, e, f) 
  {
    var g = b('createArrayFrom');
    Function.prototype.curry = (function () 
    {
      var h = g(arguments);
      return this.bind.apply(this, [null, ].concat(h));
    });
    Function.prototype.defer = (function (h, i) 
    {
      if(typeof this != 'function')
        throw new TypeError();
      h = h || 0;
      return setTimeout(this, h, i);
    });
  }), 
  3);
  __d("goURI", [], (function (a, b, c, d, e, f) 
  {
    function g(h, i, j) 
    {
      h = h.toString();
      if(! i && a.PageTransitions && PageTransitions.isInitialized())
      {
        PageTransitions.go(h, j);
      }
      else
        if(window.location.href == h)
        {
          window.location.reload();
        }
        else
          window.location.href = h;
    }
    e.exports = g;
  }));
  __d("legacy:goURI", ["goURI", ], (function (a, b, c, d) 
  {
    a.goURI = b('goURI');
  }), 
  3);
  __d("InitialJSLoader", ["Arbiter", "Bootloader", "OnloadEvent", "Run", "ServerJS", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter'), h = b('Bootloader'), i = b('OnloadEvent'), 
    j = b('Run'), 
    k = b('ServerJS'), 
    l = {
      INITIAL_JS_READY : 'BOOTLOAD/JSREADY',
      loadOnDOMContentReady : (function (m, n) 
      {
        g.subscribe(i.ONLOAD_DOMCONTENT_CALLBACK, (function () 
        {
          function o() 
          {
            h.loadResources(m, (function () 
            {
              g.inform(l.INITIAL_JS_READY, true, g.BEHAVIOR_STATE);
            }));
          }
          if(n)
          {
            setTimeout(o, n);
          }
          else
            o();
        }));
      }),
      handleServerJS : (function (m) 
      {
        var n = new k();
        n.handle(m);
        j.onAfterLoad(n.cleanup.bind(n));
      })
    };
    e.exports = l;
  }));
  __d("lowerDomain", [], (function (a, b, c, d, e, f) 
  {
    if(document.domain.toLowerCase().match(/(^|\.)facebook\..*/))
      document.domain = "facebook.com";
  }));
  __d("legacy:object-core-utils", ["isEmpty", "copyProperties", ], 
  (function (a, b, c, d) 
  {
    a.is_empty = b('isEmpty');
    a.copyProperties = b('copyProperties');
  }), 
  3);
  __d("PlaceholderListener", ["Arbiter", "CSS", "Parent", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter'), h = b('CSS'), i = b('Parent'), j = document.documentElement, 
    k = (function (m) 
    {
      m = m || window.event;
      var n = m.target || m.srcElement;
      if(n.getAttribute('data-silentPlaceholderListener'))
        return;
      var o = n.getAttribute('placeholder');
      if(o)
      {
        var p = i.byClass(n, 'focus_target');
        if('focus' == m.type || 'focusin' == m.type)
        {
          var q = n.value.replace(/\r\n/g, '\n'), r = o.replace(/\r\n/g, '\n');
          if(q == r && h.hasClass(n, 'DOMControl_placeholder'))
          {
            n.value = '';
            h.removeClass(n, 'DOMControl_placeholder');
          }
          if(p)
            l.expandInput(p);
        }
        else
        {
          if(n.value === '')
          {
            h.addClass(n, 'DOMControl_placeholder');
            n.value = o;
            p && h.removeClass(p, 'child_is_active');
            n.style.direction = '';
          }
          p && h.removeClass(p, 'child_is_focused');
        }
      }
    });
    if(j.addEventListener)
    {
      j.addEventListener('focus', k, true);
      j.addEventListener('blur', k, true);
    }
    else
    {
      j.attachEvent('onfocusin', k);
      j.attachEvent('onfocusout', k);
    }
    var l = {
      expandInput : (function (m) 
      {
        h.addClass(m, 'child_is_active');
        h.addClass(m, 'child_is_focused');
        h.addClass(m, 'child_was_focused');
        g.inform('reflow');
      })
    };
    e.exports = l;
  }));
  __d("clickRefAction", ["Arbiter", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter');
    function h(l, m, n, o, p) 
    {
      var q = l + '/' + m;
      this.ue = q;
      this._ue_ts = l;
      this._ue_count = m;
      this._context = n;
      this._ns = null;
      this._node = o;
      this._type = p;
    }
    h.prototype.set_namespace = (function (l) 
    {
      this._ns = l;
      return this;
    });
    h.prototype.coalesce_namespace = (function (l) 
    {
      if(this._ns === null)
        this._ns = l;
      return this;
    });
    h.prototype.add_event = (function () 
    {
      return this;
    });
    var i = 0, j = [];
    function k(l, m, event, n, o) 
    {
      var p = Date.now(), q = event && event.type;
      o = o || {
        
      };
      if(! m && event)
        m = event.getTarget();
      var r = 50;
      if(m && n != "FORCE")
        for(var s = j.length - 1;s >= 0 && ((p - j[s]._ue_ts) < r);-- s)
          if(j[s]._node == m && j[s]._type == q)
            return j[s];
      var t = new h(p, i, l, m, q);
      j.push(t);
      while(j.length > 10)
        j.shift();
      g.inform("ClickRefAction/new", {
        cfa : t,
        node : m,
        mode : n,
        event : event,
        extra_data : o
      }, 
      g.BEHAVIOR_PERSISTENT);
      i ++;
      return t;
    }
    e.exports = a.clickRefAction = k;
  }));
  __d("trackReferrer", ["Parent", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Parent');
    function h(i, j) 
    {
      i = g.byAttribute(i, 'data-referrer');
      if(i)
      {
        var k = /^(?:(?:[^:\/?#]+):)?(?:\/\/(?:[^\/?#]*))?([^?#]*)(?:\?([^#]*))?(?:#(.*))?/.exec(j)[1] || '';
        if(! k)
          return;
        var l = k + '|' + i.getAttribute('data-referrer'), m = new Date();
        m.setTime(Date.now() + 1000);
        document.cookie = "x-src=" + encodeURIComponent(l) + "; " + "expires=" + m.toGMTString() + ";path=/; domain=" + window.location.hostname.replace(/^.*(\.facebook\..*)$/i, '$1');
      }
      return i;
    }
    e.exports = h;
  }));
  __d("Miny", [], (function (a, b, c, d, e, f) 
  {
    var g = 'Miny1', h = {
      encode : [],
      decode : {
        
      }
    }, 
    i = 'wxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_'.split('');
    function j(n) 
    {
      for(var o = h.encode.length;o < n;o ++)
      {
        var p = o.toString(32).split('');
        p[p.length - 1] = i[parseInt(p[p.length - 1], 32)];
        p = p.join('');
        h.encode[o] = p;
        h.decode[p] = o;
      }
      return h;
    }
    function k(n) 
    {
      var o = n.match(/\w+|\W+/g), p = {
        
      };
      for(var q = 0;q < o.length;q ++)
        p[o[q]] = (p[o[q]] || 0) + 1;
      var r = Object.keys(p);
      r.sort((function (u, v) 
      {
        return p[u] < p[v] ? 1 : (p[v] < p[u] ? - 1 : 0);
      }));
      var s = j(r.length).encode;
      for(q = 0;q < r.length;q ++)
        p[r[q]] = s[q];
      var t = [];
      for(q = 0;q < o.length;q ++)
        t[q] = p[o[q]];
      for(q = 0;q < r.length;q ++)
        r[q] = r[q].replace(/'~'/g, '\\~');
      return [g, r.length, ].concat(r).concat(t.join('')).join('~');
    }
    function l(n) 
    {
      var o = n.split('~');
      if(o.shift() != g)
        throw new Error('Not a Miny stream');
      var p = parseInt(o.shift(), 10), q = o.pop();
      q = q.match(/[0-9a-v]*[\-w-zA-Z_]/g);
      var r = o, s = j(p).decode, t = [];
      for(var u = 0;u < q.length;u ++)
        t[u] = r[s[q[u]]];
      return t.join('');
    }
    var m = {
      encode : k,
      decode : l
    };
    e.exports = m;
  }));
  __d("QueryString", [], (function (a, b, c, d, e, f) 
  {
    function g(k) 
    {
      __t([k, 'object', 'bag', ]);
      return __t([(function () 
      {
        var l = [];
        Object.keys(k).forEach((function (m) 
        {
          var n = k[m];
          if(typeof n === 'undefined')
            return;
          if(n === null)
          {
            l.push(m);
            return;
          }
          l.push(encodeURIComponent(m) + '=' + encodeURIComponent(n));
        }));
        return l.join('&');
      }).apply(this, arguments), 'string', ]);
    }
    __w(g, {
      signature : "function(object):string"
    });
    function h(k, l) 
    {
      __t([k, 'string', 'str', ], [l, '?boolean', 'strict', ]);
      return __t([(function () 
      {
        var m = {
          
        };
        if(k === '')
          return m;
        var n = k.split('&');
        for(var o = 0;o < n.length;o ++)
        {
          var p = n[o].split('=', 2), q = decodeURIComponent(p[0]);
          if(l && m.hasOwnProperty(q))
            throw new URIError('Duplicate key: ' + q);
          m[q] = p.length === 2 ? decodeURIComponent(p[1]) : null;
        }
        return m;
      }).apply(this, arguments), 'object', ]);
    }
    __w(h, {
      signature : "function(string,?boolean):object"
    });
    function i(k, l) 
    {
      __t([k, 'string', 'url', ]);
      return __t([(function () 
      {
        return k + (~ k.indexOf('?') ? '&' : '?') + (typeof l === 'string' ? l : j.encode(l));
      }).apply(this, arguments), 'string', ]);
    }
    __w(i, {
      signature : "function(string):string"
    });
    var j = {
      encode : g,
      decode : h,
      appendToUrl : i
    };
    e.exports = j;
  }));
  __d("UserAgent", [], (function (a, b, c, d, e, f) 
  {
    var g = false, h, i, j, k, l, m, n, o, p, q, r, s, t, u;
    function v() 
    {
      if(g)
        return;
      g = true;
      var x = navigator.userAgent, y = /(?:MSIE.(\d+\.\d+))|(?:(?:Firefox|GranParadiso|Iceweasel).(\d+\.\d+))|(?:Opera(?:.+Version.|.)(\d+\.\d+))|(?:AppleWebKit.(\d+(?:\.\d+)?))/.exec(x), 
      z = /(Mac OS X)|(Windows)|(Linux)/.exec(x);
      r = /\b(iPhone|iP[ao]d)/.exec(x);
      s = /\b(iP[ao]d)/.exec(x);
      p = /Android/i.exec(x);
      t = /FBAN\/\w+;/i.exec(x);
      u = /Mobile/i.exec(x);
      q = ! ! (/Win64/.exec(x));
      if(y)
      {
        h = y[1] ? parseFloat(y[1]) : NaN;
        if(h && document.documentMode)
          h = document.documentMode;
        i = y[2] ? parseFloat(y[2]) : NaN;
        j = y[3] ? parseFloat(y[3]) : NaN;
        k = y[4] ? parseFloat(y[4]) : NaN;
        if(k)
        {
          y = /(?:Chrome\/(\d+\.\d+))/.exec(x);
          l = y && y[1] ? parseFloat(y[1]) : NaN;
        }
        else
          l = NaN;
      }
      else
        h = i = j = l = k = NaN;
      if(z)
      {
        if(z[1])
        {
          var aa = /(?:Mac OS X (\d+(?:[._]\d+)?))/.exec(x);
          m = aa ? parseFloat(aa[1].replace('_', '.')) : true;
        }
        else
          m = false;
        n = ! ! z[2];
        o = ! ! z[3];
      }
      else
        m = n = o = false;
    }
    var w = {
      ie : (function () 
      {
        return v() || h;
      }),
      ie64 : (function () 
      {
        return w.ie() && q;
      }),
      firefox : (function () 
      {
        return v() || i;
      }),
      opera : (function () 
      {
        return v() || j;
      }),
      webkit : (function () 
      {
        return v() || k;
      }),
      safari : (function () 
      {
        return w.webkit();
      }),
      chrome : (function () 
      {
        return v() || l;
      }),
      windows : (function () 
      {
        return v() || n;
      }),
      osx : (function () 
      {
        return v() || m;
      }),
      linux : (function () 
      {
        return v() || o;
      }),
      iphone : (function () 
      {
        return v() || r;
      }),
      mobile : (function () 
      {
        return v() || (r || s || p || u);
      }),
      nativeApp : (function () 
      {
        return v() || t;
      }),
      android : (function () 
      {
        return v() || p;
      }),
      ipad : (function () 
      {
        return v() || s;
      })
    };
    e.exports = w;
  }));
  __d("XHR", ["Env", "ServerJS", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Env'), h = b('ServerJS'), i = 1, j = {
      create : (function () 
      {
        try
{          return a.XMLHttpRequest ? new a.XMLHttpRequest() : new ActiveXObject("MSXML2.XMLHTTP.3.0");}
        catch(k)
{          }

      }),
      getAsyncParams : (function (k) 
      {
        var l = {
          __user : g.user,
          __a : 1,
          __dyn : h.getLoadedModuleHash(),
          __req : (i ++).toString(36)
        };
        if(k == 'POST' && g.fb_dtsg)
          l.fb_dtsg = g.fb_dtsg;
        if(g.fb_isb)
          l.fb_isb = g.fb_isb;
        return l;
      })
    };
    e.exports = j;
  }));
  __d("BanzaiAdapter", ["Arbiter", "Env", "Miny", "QueryString", "Run", "UserAgent", "XHR", "BanzaiConfig", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter'), h = b('Env'), i = b('Miny'), j = b('QueryString'), 
    k = b('Run'), 
    l = b('UserAgent'), 
    m = b('XHR'), 
    n = null, 
    o = new g(), 
    p = b('BanzaiConfig'), 
    q = '/ajax/bz', 
    r = {
      
    }, 
    s = r.adapter = {
      config : p,
      getUserID : (function () 
      {
        return h.user;
      }),
      inform : (function (t) 
      {
        o.inform(t);
      }),
      subscribe : (function (t, u) 
      {
        o.subscribe(t, u);
      }),
      cleanup : (function () 
      {
        if(n && n.readyState < 4)
          n.abort();
        if(n)
        {
          delete n.onreadystatechange;
          n = null;
        }
      }),
      readyToSend : (function () 
      {
        var t = l.ie() <= 8 ? true : navigator.onLine;
        return ! n && t;
      }),
      send : (function (t, u, v) 
      {
        var w = 'POST';
        n = m.create();
        n.open(w, q, true);
        n.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        n.onreadystatechange = (function () 
        {
          if(n.readyState >= 4)
          {
            var aa = n.status;
            s.cleanup();
            if(aa == 200)
            {
              if(u)
                u();
              s.inform(r.OK);
            }
            else
            {
              if(v)
                v(aa);
              s.inform(r.ERROR);
            }
          }
        });
        setTimeout(s.cleanup, r.SEND_TIMEOUT, false);
        var x = m.getAsyncParams(w);
        x.q = JSON.stringify(t);
        x.ts = Date.now();
        x.ph = h.push_phase;
        if(r.FBTRACE)
          x.fbtrace = r.FBTRACE;
        if(r.isEnabled('miny_compression'))
        {
          var y = Date.now(), z = i.encode(x.q);
          if(z.length < x.q.length)
          {
            x.q = z;
            x.miny_encode_ms = Date.now() - y;
          }
        }
        n.send(j.encode(x));
      }),
      onUnload : (function (t) 
      {
        k.onAfterUnload(t);
      })
    };
    e.exports = r;
  }));
  __d("pageID", [], (function (a, b, c, d, e, f) 
  {
    e.exports = Math.floor(2147483648 * Math.random()).toString(36);
  }));
  __d("Banzai", ["BanzaiAdapter", "pageID", "copyProperties", "emptyFunction", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('BanzaiAdapter'), h = g.adapter, i = b('pageID'), 
    j = b('copyProperties'), 
    k = b('emptyFunction'), 
    l = 'Banzai', 
    m = 'sequencer', 
    n, 
    o, 
    p, 
    q = [], 
    r = {
      
    }, 
    s = a != a.top;
    function t() 
    {
      if(p && p.posts.length > 0)
        q.push(p);
      p = {
        user : h.getUserID(),
        page_id : i,
        trigger : null,
        time : Date.now(),
        posts : []
      };
      if(g.isEnabled(m))
        p.sequence = [];
    }
    function u(z) 
    {
      var aa = Date.now() + z;
      if(! o || aa < o)
      {
        o = aa;
        clearTimeout(n);
        n = setTimeout(v, z, false);
        return true;
      }
    }
    function v() 
    {
      o = null;
      u(g.BASIC.delay);
      if(! h.readyToSend())
        return;
      h.inform(g.SEND);
      if(q.length <= 0 && p.posts.length <= 0)
      {
        h.inform(g.OK);
        return;
      }
      t();
      var z = q;
      q = [];
      h.send(z, null, (function (aa) 
      {
        var ba = Date.now() - (h.config.EXPIRY || g.EXPIRY), ca = aa >= 400 && aa < 600, 
        da = z.map((function (ea) 
        {
          ea.posts = ea.posts.filter((function (fa) 
          {
            var ga = ca || fa.__meta.options.retry;
            fa.__meta.retryCount = (fa.__meta.retryCount || 0) + 1;
            fa[3] = fa.__meta.retryCount;
            return ga && fa.__meta.timestamp > ba;
          }));
          return ea;
        }));
        da = da.filter((function (ea) 
        {
          return ea.posts.length > 0;
        }));
        q = da.concat(q);
      }));
    }
    var w, x;
    try
{      x = a.sessionStorage;}
    catch(y)
{      }

    if(x && ! s)
    {
      w = {
        store : (function z() 
        {
          try
{            t();
            var ba = h.getUserID(), ca = q.filter((function (ea) 
            {
              return ea.user == ba;
            })).map((function (ea) 
            {
              ea = j({
                
              }, ea);
              ea.posts = ea.posts.map((function (fa) 
              {
                return [fa[0], fa[1], fa[2], fa.__meta, ];
              }));
              return ea;
            })), 
            da = JSON.stringify(ca);
            x.setItem(l, da);}
          catch(aa)
{            }

        }),
        restore : (function z() 
        {
          try
{            var ba = x.getItem(l);
            if(ba)
            {
              x.removeItem(l);
              var ca = h.getUserID(), da = JSON.parse(ba);
              da = da.filter((function (ea) 
              {
                ea.posts.forEach((function (fa) 
                {
                  fa.__meta = fa.pop();
                  if('retryCount' in fa.__meta)
                    fa[3] = fa.__meta.retryCount;
                }));
                return ea.user == ca;
              }));
              q = q.concat(da);
            }}
          catch(aa)
{            }

        })
      };
    }
    else
      w = {
        store : k,
        restore : k
      };
    g.SEND = 'Banzai:SEND';
    g.OK = 'Banzai:OK';
    g.ERROR = 'Banzai:ERROR';
    g.SHUTDOWN = 'Banzai:SHUTDOWN';
    g.SEND_TIMEOUT = 15000;
    g.VITAL_WAIT = 1000;
    g.BASIC_WAIT = 60000;
    g.EXPIRY = 30 * 60000;
    g.VITAL = {
      delay : h.config.MIN_WAIT || g.VITAL_WAIT
    };
    g.BASIC = {
      delay : h.config.MAX_WAIT || g.BASIC_WAIT
    };
    g.FBTRACE = h.config.fbtrace, g.isEnabled = (function (z) 
    {
      return h.config.gks && h.config.gks[z];
    });
    g.post = (function (z, aa, ba) 
    {
      ba = ba || {
        
      };
      if(s)
      {
        if(document.domain == 'facebook.com')
          try
{            var da = a.top.require('Banzai');
            da.post.apply(da, arguments);}
          catch(ca)
{            }

        return;
      }
      if(h.config.disabled)
        return;
      var ea = h.config.blacklist;
      if(ea)
      {
        if(ea && ea.join && ! ea._regex)
          ea._regex = new RegExp('^(?:' + ea.join('|') + ')');
        if(ea._regex && ea._regex.test(z))
          return;
      }
      if(p.user != h.getUserID())
        t();
      var fa = Date.now(), ga = [z, aa, fa - p.time, ];
      ga.__meta = {
        options : ba,
        timestamp : fa
      };
      p.posts.push(ga);
      var ha = ba.delay;
      if(ha == null)
        ha = g.BASIC_WAIT;
      if(g.isEnabled(m))
      {
        if(! (z in r))
        {
          r[z] = 0;
        }
        else
          r[z] ++;
        p.sequence.push([z, r[z], ]);
      }
      if(u(ha) || ! p.trigger)
        p.trigger = z;
    });
    g.subscribe = h.subscribe;
    g._testState = (function () 
    {
      return {
        wad : p,
        wads : q
      };
    });
    h.onUnload((function () 
    {
      h.cleanup();
      h.inform(g.SHUTDOWN);
      w.store();
    }));
    t();
    w.restore();
    u(g.BASIC.delay);
    e.exports = g;
  }));
  __d("userAction", ["Arbiter", "Banzai", "copyProperties", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('Arbiter'), h = b('Banzai'), i = b('copyProperties'), 
    j = 50, 
    k = [], 
    l = {
      
    }, 
    m = {
      
    };
    function n(v, w, x, y, event) 
    {
      var z = v + '/' + w, aa = u(y);
      i(this, {
        ue : z,
        _uai_logged : false,
        _uai_timeout : null,
        _primary : {
          
        },
        _fallback : {
          
        },
        _default_ua_id : aa || '-',
        _default_action_type : event ? event.type : '-',
        _ts : v,
        _ns : x,
        _start_ts : v,
        _prev_event : 's',
        _ue_ts : v,
        _ue_count : w,
        _data_version : 1,
        _event_version : 2,
        _info_version : 2
      });
      this._log('ua:n', [1, z, ]);
    }
    function o(v, w, x, y) 
    {
      var z = v in m ? m[v] : {
        
      }, aa = w in z ? z[w] : {
        
      }, 
      ba;
      if(x in aa)
        if('*' in aa[x])
        {
          ba = aa[x]['*'];
        }
        else
          if(y in aa[x])
            ba = aa[x][y];
      return ba;
    }
    var p = {
      store : true,
      delay : 3000,
      retry : true
    };
    i(n.prototype, {
      _log : (function (v, w) 
      {
        var x = l[v] === true, y = o(v, this._ns, 'ua_id', this._get_ua_id()), 
        z = o(v, this._ns, 'action', this._get_action_type()), 
        aa = (y !== undefined || z !== undefined), 
        ba = aa ? (y || z) : x;
        if(h.isEnabled('useraction') && ba)
          h.post(v, w, p);
      }),
      _get_action_type : (function () 
      {
        return (this._primary._action_type || this._fallback._action_type || this._default_action_type);
      }),
      _get_ua_id : (function () 
      {
        return (this._primary._ua_id || this._fallback._ua_id || this._default_ua_id);
      }),
      _log_uai : (function () 
      {
        var v = [this._info_version, this.ue, this._ns, this._get_ua_id(), this._get_action_type(), ];
        this._log('ua:i', v);
        this._uai_logged = true;
        this._uai_timeout = null;
      }),
      uai : (function (v, w, x) 
      {
        if(! this._uai_logged)
        {
          this._uai_timeout && clearTimeout(this._uai_timeout);
          this._primary._ua_id = w;
          this._primary._action_type = v;
          if(x === undefined)
          {
            this._log_uai();
          }
          else
            if(x === false)
            {
              this._uai_logged = true;
            }
            else
            {
              var y = this;
              x = x || 0;
              this._uai_timeout = setTimeout((function () 
              {
                y._log_uai.apply(y);
              }), 
              x);
            }
        }
        return this;
      }),
      uai_fallback : (function (v, w, x) 
      {
        if(! this._uai_logged)
        {
          var y = this;
          this._uai_timeout && clearTimeout(this._uai_timeout);
          this._fallback._ua_id = w;
          this._fallback._action_type = v;
          x = (x === undefined) ? j : x;
          this._uai_timeout = setTimeout((function () 
          {
            y._log_uai.apply(y);
          }), 
          x);
        }
        return this;
      }),
      add_event : (function (v, w, x) 
      {
        w = w || 0;
        var y = (Date.now() - w), z = y - this._ts, aa = y - (x ? x : this._ue_ts), 
        ba = [this._event_version, this.ue, this._ns, this._get_ua_id(), this._prev_event, v, z, aa, ];
        if(this._get_ua_id())
        {
          this._log('ua:e', ba);
          this._ts = y;
          this._prev_event = v;
        }
        return this;
      }),
      add_data : (function (v) 
      {
        var w = [this._data_version, this.ue, v, ];
        this._log('ua:d', w);
        return this;
      })
    });
    var q = 0, r = 0, s = null;
    function t(v, w, event, x) 
    {
      x = x || {
        
      };
      var y = Date.now();
      if(! w && event)
        w = event.getTarget();
      if(w && s)
        if(y - r < j && w == s && x.mode == "DEDUP")
          return k[k.length - 1];
      var z = new n(y, q, v, w, event);
      s = w;
      k.push(z);
      while(k.length > 10)
        k.shift();
      g.inform("UserAction/new", {
        ua : z,
        node : w,
        mode : x.mode,
        event : event
      });
      r = y;
      q ++;
      return z;
    }
    function u(v) 
    {
      if(! v || ! v.nodeName)
        return null;
      return v.nodeName.toLowerCase();
    }
    t.setUATypeConfig = (function (v) 
    {
      i(l, v);
    });
    t.setCustomSampleConfig = (function (v) 
    {
      i(m, v);
    });
    t.getCurrentUECount = (function () 
    {
      return q;
    });
    e.exports = a.userAction = t;
  }));
  __d("Primer", ["function-extensions", "Bootloader", "CSS", "ErrorUtils", "Parent", "clickRefAction", "trackReferrer", "userAction", ], 
  (function (a, b, c, d, e, f) 
  {
    b('function-extensions');
    var g = b('Bootloader'), h = b('CSS'), i = b('ErrorUtils'), 
    j = b('Parent'), 
    k = b('clickRefAction'), 
    l = b('trackReferrer'), 
    m = b('userAction'), 
    n = null, 
    o = /async(?:-post)?|dialog(?:-post)?|theater|toggle/, 
    p = document.documentElement;
    function q(t, u) 
    {
      t = j.byAttribute(t, u);
      if(! t)
        return;
      do
      {
        var v = t.getAttribute(u);
        JSON.parse(v).forEach((function (w) 
        {
          var x = t;
          g.loadModules.call(g, [w[0], ], (function (y) 
          {
            y[w[1]](x);
          }));
        }));
      }while(t = j.byAttribute(t.parentNode, u));
      return false;
    }
    p.onclick = i.guard((function (t) 
    {
      t = t || window.event;
      n = t.target || t.srcElement;
      var u = q(n, 'data-onclick'), v = j.byTag(n, 'A');
      if(! v)
        return u;
      var w = v.getAttribute('ajaxify'), x = v.href, y = w || x;
      if(y)
      {
        k('a', v, t).coalesce_namespace('primer');
        var z = m('primer', v, t, {
          mode : 'DEDUP'
        }).uai_fallback('click');
        if(a.ArbiterMonitor)
          a.ArbiterMonitor.initUA(z, [v, ]);
      }
      if(w && x && ! (/#$/).test(x))
      {
        var aa = t.which && t.which === 2, ba = t.altKey || t.ctrlKey || t.metaKey || t.shiftKey;
        if(aa || ba)
          return;
      }
      l(v, y);
      var ca = v.rel && v.rel.match(o);
      ca = ca && ca[0];
      switch(ca){
        case 'dialog':
          

        case 'dialog-post':
          g.loadModules(['AsyncDialog', ], (function (da) 
          {
            da.bootstrap(y, v, ca);
          }));
          break;

        case 'async':
          

        case 'async-post':
          g.loadModules(['AsyncRequest', ], (function (da) 
          {
            da.bootstrap(y, v);
          }));
          break;

        case 'theater':
          g.loadModules(['PhotoSnowlift', ], (function (da) 
          {
            da.bootstrap(y, v);
          }));
          break;

        case 'toggle':
          h.toggleClass(v.parentNode, 'openToggler');
          g.loadModules(['Toggler', ], (function (da) 
          {
            da.bootstrap(v);
          }));
          break;

        default:
          return u;
        
      }
      return false;
    }));
    p.onsubmit = i.guard((function (t) 
    {
      t = t || window.event;
      var u = t.target || t.srcElement;
      if(u && u.nodeName == 'FORM' && u.getAttribute('rel') == 'async')
      {
        k('f', u, t).coalesce_namespace('primer');
        var v = m('primer', u, t, {
          mode : 'DEDUP'
        }).uai_fallback('submit');
        if(a.ArbiterMonitor)
          a.ArbiterMonitor.initUA(v, [u, ]);
        var w = n;
        g.loadModules(['Form', ], (function (x) 
        {
          x.bootstrap(u, w);
        }));
        return false;
      }
    }));
    var r = null, s = i.guard((function (t, u) 
    {
      u = u || window.event;
      r = u.target || u.srcElement;
      q(r, 'data-on' + t);
      var v = j.byAttribute(r, 'data-hover');
      if(! v)
        return;
      switch(v.getAttribute('data-hover')){
        case 'tooltip':
          g.loadModules(['Tooltip', ], (function (w) 
          {
            w.process(v, r);
          }));
          break;

        
      }
    }));
    p.onmouseover = s.curry('mouseover');
    if(p.addEventListener)
    {
      p.addEventListener('focus', s.curry('focus'), true);
    }
    else
      p.attachEvent('onfocusin', s.curry('focus'));
  }));
  __d("ScriptPath", ["Banzai", "ErrorUtils", ], (function (a, b, c, d, e, f) 
  {
    var g = b("Banzai"), h = b("ErrorUtils"), i = 'script_path_change', 
    j = {
      scriptPath : null,
      categoryToken : null
    }, 
    k = {
      PAGE_LOAD : 'load',
      PAGE_UNLOAD : 'unload',
      TRANSITION : 'transition'
    }, 
    l = null, 
    m = null, 
    n = {
      
    }, 
    o = 0, 
    p = false, 
    q = null;
    function r(z) 
    {
      __t([z, 'function', 'callback', ]);
      return __t([(function () 
      {
        var aa = ++ o;
        n[aa] = z;
        return aa;
      }).apply(this, arguments), 'number', ]);
    }
    __w(r, {
      signature : "function(function):number"
    });
    function s(z) 
    {
      __t([z, 'number', 'token', ]);
      if(n[z])
        delete n[z];
    }
    __w(s, {
      signature : "function(number)"
    });
    function t() 
    {
      Object.keys(n).forEach((function (z) 
      {
        h.applyWithGuard(n[z], null, [{
          source : l,
          dest : m
        }, ]);
      }));
    }
    function u(z, aa, ba) 
    {
      if(! p)
        return;
      var ca = {
        source_path : z.scriptPath,
        source_token : z.categoryToken,
        dest_path : aa.scriptPath,
        dest_token : aa.categoryToken,
        navigation : q,
        cause : ba
      };
      g.post(i, ca);
    }
    function v() 
    {
      u(j, m, k.PAGE_LOAD);
    }
    function w(z, aa) 
    {
      u(z, aa, k.TRANSITION);
    }
    function x() 
    {
      u(m, j, k.PAGE_UNLOAD);
    }
    g.subscribe(g.SHUTDOWN, x);
    var y = {
      set : (__w((function (z, aa) 
      {
        __t([z, 'string', 'scriptPath', ], [aa, 'string', 'categoryToken', ]);
        var ba = m;
        m = {
          scriptPath : z,
          categoryToken : aa
        };
        window._script_path = z;
        t();
        if(p)
          if(ba)
          {
            w(ba, m);
          }
          else
            v();
      }), 
      {
        signature : "function(string,string)"
      })),
      setNavigation : (function (z) 
      {
        q = z;
      }),
      startLogging : (function () 
      {
        p = true;
        if(m)
          v();
      }),
      stopLogging : (function () 
      {
        p = false;
      }),
      getScriptPath : (__w((function () 
      {
        return __t([(function () 
        {
          return m ? m.scriptPath : undefined;
        }).apply(this, arguments), '?string', ]);
      }), 
      {
        signature : "function():?string"
      })),
      getCategoryToken : (__w((function () 
      {
        return __t([(function () 
        {
          return m ? m.categoryToken : undefined;
        }).apply(this, arguments), '?string', ]);
      }), 
      {
        signature : "function():?string"
      })),
      subscribe : (__w((function (z) 
      {
        __t([z, 'function', 'callback', ]);
        return __t([(function () 
        {
          return r(z);
        }).apply(this, arguments), 'number', ]);
      }), 
      {
        signature : "function(function):number"
      })),
      unsubscribe : (__w((function (z) 
      {
        __t([z, 'number', 'token', ]);
        s(z);
      }), 
      {
        signature : "function(number)"
      }))
    };
    y.CAUSE = k;
    y.BANZAI_LOGGING_ROUTE = i;
    e.exports = y;
  }));
  __d("URLFragmentPrelude", ["ScriptPath", "URLFragmentPreludeConfig", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('ScriptPath'), h = b('URLFragmentPreludeConfig'), 
    i = /^(?:(?:[^:\/?#]+):)?(?:\/\/(?:[^\/?#]*))?([^?#]*)(?:\?([^#]*))?(?:#(.*))?/, 
    j = '', 
    k = /^[^\/\\#!\.\?\*\&\^]+$/;
    window.location.href.replace(i, (function (l, m, n, o) 
    {
      var p, q, r, s;
      p = q = m + (n ? '?' + n : '');
      if(o)
      {
        if(h.incorporateQuicklingFragment)
        {
          var t = o.replace(/^(!|%21)/, '');
          r = t.charAt(0);
          if(r == '/' || r == '\\')
            p = t.replace(/^[\\\/]+/, '/');
        }
        if(h.hashtagRedirect)
          if(q == p)
          {
            var u = o.match(k);
            if(u && ! n && m == '/')
              p = '/hashtag/' + o;
          }
      }
      if(p != q)
      {
        s = g.getScriptPath();
        if(s)
          document.cookie = "rdir=" + s + "; path=/; domain=" + window.location.hostname.replace(/^.*(\.facebook\..*)$/i, '$1');
        window.location.replace(j + p);
      }
    }));
  }));
  __d("removeArrayReduce", [], (function (a, b, c, d, e, f) 
  {
    Array.prototype.reduce = undefined;
    Array.prototype.reduceRight = undefined;
  }));
  __d("cx", [], (function (a, b, c, d, e, f) 
  {
    function g(h) 
    {
      throw new Error('cx' + '(...): Unexpected class transformation.');
    }
    e.exports = g;
  }));
  __d("LitestandSidebarPrelude", ["CSS", "cx", ], (function (a, b, c, d, e, f) 
  {
    var g = b('CSS'), h = b('cx');
    e.exports = {
      init : (function (i, j, k) 
      {
        var l = document.documentElement;
        l.className = l.className + ' sidebarMode';
        if(j || l.clientWidth <= k)
          l.className = l.className + ' ' + "_4kdq";
        g.show(i);
      })
    };
  }));
  __d("SubmitOnEnterListener", ["Bootloader", "CSS", ], (function (a, b, c, d, e, f) 
  {
    var g = b('Bootloader'), h = b('CSS');
    document.documentElement.onkeydown = (function (i) 
    {
      i = i || window.event;
      var j = i.target || i.srcElement, k = i.keyCode == 13 && ! i.altKey && ! i.ctrlKey && ! i.metaKey && ! i.shiftKey && h.hasClass(j, 'enter_submit');
      if(k)
      {
        g.loadModules(['DOM', 'Input', 'trackReferrer', 'Form', ], (function (l, m, n, o) 
        {
          if(! m.isEmpty(j))
          {
            var p = j.form, q = l.scry(p, '.enter_submit_target')[0] || l.scry(p, '[type="submit"]')[0];
            if(q)
            {
              var r = o.getAttribute(p, 'ajaxify') || o.getAttribute(p, 'action');
              if(r)
                n(p, r);
              q.click();
            }
          }
        }));
        return false;
      }
    });
  }));
  __d("CommentPrelude", ["CSS", "Parent", "clickRefAction", "userAction", ], 
  (function (a, b, c, d, e, f) 
  {
    var g = b('CSS'), h = b('Parent'), i = b('clickRefAction'), 
    j = b('userAction');
    function k(o, p) 
    {
      j('ufi', o).uai('click');
      i('ufi', o, null, 'FORCE');
      return l(o, p);
    }
    function l(o, p) 
    {
      var q = h.byTag(o, 'form');
      m(q);
      var r = g.removeClass.curry(q, 'hidden_add_comment');
      if(window.ScrollAwareDOM)
      {
        window.ScrollAwareDOM.monitor(q, r);
      }
      else
        r();
      if(p !== false)
      {
        var s = q.add_comment_text_text || q.add_comment_text, t = s.length;
        if(t)
        {
          if(! h.byClass(s[t - 1], 'UFIReplyList'))
          {
            s[t - 1].focus();
          }
          else
            if(! h.byClass(s[0], 'UFIReplyList'))
              s[0].focus();
        }
        else
          s.focus();
      }
      return false;
    }
    function m(o) 
    {
      var p = g.removeClass.curry(o, 'collapsed_comments');
      if(window.ScrollAwareDOM)
      {
        window.ScrollAwareDOM.monitor(o, p);
      }
      else
        p();
    }
    var n = {
      click : k,
      expand : l,
      uncollapse : m
    };
    e.exports = n;
  }));
  __d("legacy:ufi-comment-prelude-js", ["CommentPrelude", ], 
  (function (a, b, c, d) 
  {
    var e = b('CommentPrelude');
    a.fc_click = e.click;
    a.fc_expand = e.expand;
  }), 
  3);
  __d("ScriptMonitor", [], (function (a, b, c, d, e, f) 
  {
    var g, h = [], i = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
    e.exports = {
      activate : (function () 
      {
        if(! i)
          return;
        g = new i((function (j) 
        {
          for(var k = 0;k < j.length;k ++)
          {
            var l = j[k];
            if(l.type == 'childList')
            {
              for(var m = 0;m < l.addedNodes.length;m ++)
              {
                var n = l.addedNodes[m];
                if((n.tagName == 'SCRIPT' || n.tagName == 'IFRAME') && n.src)
                  h.push(n.src);
              }
            }
            else
              if(l.type == 'attributes' && l.attributeName == 'src')
                h.push(l.target.src);
          }
        }));
        g.observe(document, {
          attributes : true,
          childList : true,
          subtree : true
        });
      }),
      stop : (function () 
      {
        g && g.disconnect();
        return h;
      })
   };
  }));

