  var JSON;
  JSON || (window.JSON = JSON = {
    
  });
  (function () 
  {
    function f(a) 
    {
      return a < 10 ? "0" + a : a;
    }
    function quote(a) 
    {
      escapable.lastIndex = 0;
      return escapable.test(a) ? '"' + a.replace(escapable, (function (a) 
      {
        var b = meta[a];
        return typeof b == "string" ? b : "\\u" + ("0000" + a.charCodeAt(0).toString(16)).slice(- 4);
      })) + '"' : '"' + a + '"';
    }
    function str(a, b) 
    {
      var c, d, e, f, g = gap, i, j = b[a];
      j && typeof j == "object" && typeof j.toJSON == "function" && (j = j.toJSON(a));
      typeof rep == "function" && (j = rep.call(b, a, j));
      switch(typeof j){
        case "string":
          return quote(j);

        case "number":
          return isFinite(j) ? String(j) : "null";

        case "boolean":
          

        case "null":
          return String(j);

        case "object":
          if(! j)
            return "null";
          gap += indent;
          i = [];
          if(Object.prototype.toString.apply(j) === "[object Array]")
          {
            f = j.length;
            for(c = 0;c < f;c += 1)
              i[c] = str(c, j) || "null";
            e = i.length === 0 ? "[]" : gap ? "[\n" + gap + i.join(",\n" + gap) + "\n" + g + "]" : "[" + i.join(",") + "]";
            gap = g;
            return e;
          }
          if(rep && typeof rep == "object")
          {
            f = rep.length;
            for(c = 0;c < f;c += 1)
              if(typeof rep[c] == "string")
              {
                d = rep[c];
                e = str(d, j);
                e && i.push(quote(d) + (gap ? ": " : ":") + e);
              }
          }
          else
            for(d in j)
              if(Object.prototype.hasOwnProperty.call(j, d))
              {
                e = str(d, j);
                e && i.push(quote(d) + (gap ? ": " : ":") + e);
              }
          e = i.length === 0 ? "{}" : gap ? "{\n" + gap + i.join(",\n" + gap) + "\n" + g + "}" : "{" + i.join(",") + "}";
          gap = g;
          return e;

        
      }
    }
    "use strict";
    if(typeof Date.prototype.toJSON != "function")
    {
      Date.prototype.toJSON = (function (a) 
      {
        return isFinite(this.valueOf()) ? this.getUTCFullYear() + "-" + f(this.getUTCMonth() + 1) + "-" + f(this.getUTCDate()) + "T" + f(this.getUTCHours()) + ":" + f(this.getUTCMinutes()) + ":" + f(this.getUTCSeconds()) + "Z" : null;
      });
      String.prototype.toJSON = Number.prototype.toJSON = Boolean.prototype.toJSON = (function (a) 
      {
        return this.valueOf();
      });
    }
    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, 
    escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, 
    gap, 
    indent, 
    meta = {
      "\\b" : "\\b",
      "	" : "\\t",
      "\\n" : "\\n",
      "\\f" : "\\f",
      "\\r" : "\\r",
      '"' : '\\"',
      "\\\\" : "\\\\"
    }, 
    rep;
    typeof JSON.stringify != "function" && (JSON.stringify = (function (a, b, c) 
    {
      var d;
      gap = "";
      indent = "";
      if(typeof c == "number")
        for(d = 0;d < c;d += 1)
          indent += " ";
      else
        typeof c == "string" && (indent = c);
      rep = b;
      if(! b || typeof b == "function" || typeof b == "object" && typeof b.length == "number")
        return str("", {
          "" : a
        });
      throw new Error("JSON.stringify");
    }));
    typeof JSON.parse != "function" && (JSON.parse = (function (text, reviver) 
    {
      function walk(a, b) 
      {
        var c, d, e = a[b];
        if(e && typeof e == "object")
          for(c in e)
            if(Object.prototype.hasOwnProperty.call(e, c))
            {
              d = walk(e, c);
              d !== undefined ? e[c] = d : delete e[c];
            }
        return reviver.call(a, b, e);
      }
      var j;
      text = String(text);
      cx.lastIndex = 0;
      cx.test(text) && (text = text.replace(cx, (function (a) 
      {
        return "\\u" + ("0000" + a.charCodeAt(0).toString(16)).slice(- 4);
      })));
      if(/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, "@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, 
      "]").replace(/(?:^|:|,)(?:\s*\[)+/g, "")))
      {
        j = eval("(" + text + ")");
        return typeof reviver == "function" ? walk({
          "" : j
        }, "") : j;
      }
      throw new SyntaxError("JSON.parse");
    }));
  })();
  