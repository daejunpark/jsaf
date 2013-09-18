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

