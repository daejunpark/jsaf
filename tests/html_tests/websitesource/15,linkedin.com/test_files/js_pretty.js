  window.track = window.track || {
    
  };
  (function (track, window) 
  {
    var FN_TYPE = typeof (function () 
    {
      
    });
    var load, head;
    head = document.getElementsByTagName('head')[0];
    track.load = load = {
      
    };
    load.script = (function (url, callQueue) 
    {
      var s = document.createElement('script');
      s.src = url;
      s.async = true;
      s.onreadystatechange = s.onload = (function () 
      {
        var i = 0, callback;
        if(! s.readyState || (s.readyState in {
          loaded : 1,
          complete : 1
        }))
        {
          s.onload = s.onreadystatechange = null;
          head.removeChild(s);
          s = null;
          if(typeof (callQueue) === FN_TYPE)
          {
            callQueue();
          }
          else
          {
            while(! ! (callback = callQueue[i ++]))
            {
              callback();
            }
          }
        }
      });
      head.appendChild(s);
    });
  })(window.track, window);
  window.track = window.track || {
    
  };
  (function (track, window) 
  {
    var FN_TYPE = typeof (function () 
    {
      
    });
    var vars, callQueue;
    function bootstrap(name) 
    {
      var i, head, node, nodes, ret;
      ret = {
        reportUrl : null,
        libUrl : null,
        originTreeId : null
      };
      head = document.getElementsByTagName('head')[0];
      nodes = head.getElementsByTagName('meta');
      for(i = nodes.length - 1;i >= 0;-- i)
      {
        node = nodes[i];
        if(name === node.name)
        {
          ret.libUrl = node.content;
          if(! ret.libUrl)
          {
            return;
          }
          head.removeChild(node);
        }
        else
          if('lnkd-track-error' === node.name)
          {
            ret.reportUrl = node.content;
            if(! ret.reportUrl)
            {
              return;
            }
            head.removeChild(node);
          }
          else
            if('treeID' === node.name)
            {
              ret.originTreeId = node.content;
            }
            else
              if('appName' === node.name)
              {
                ret.appName = node.content;
              }
      }
      if(! ret.reportUrl)
      {
        return;
      }
      return ret;
    }
    function pagekey() 
    {
      var nodes, node, i;
      if(! vars.pageKey)
      {
        if(document.body.id && (document.body.id.indexOf('pagekey') === 0))
        {
          vars.pageKey = document.body.id.substring(8);
        }
        else
        {
          nodes = document.getElementsByTagName('head')[0].getElementsByTagName('meta');
          for(i = nodes.length - 1;i >= 0;-- i)
          {
            node = nodes[i];
            if('pageKey' === node.name)
            {
              vars.pageKey = node.content;
              break;
            }
          }
        }
      }
      return vars.pageKey;
    }
    vars = bootstrap(window.JSON ? 'lnkd-track-lib' : 'lnkd-track-json-lib');
    if(! vars)
    {
      return;
    }
    if(! track.xhr)
    {
      if(! track.load || ! vars.libUrl)
      {
        return;
      }
      callQueue = [];
    }
    track.errors = {
      
    };
    track.errors.onMethod = (function (method, err) 
    {
      return (function () 
      {
        try
{          method.apply(window, arguments);}
        catch(e)
{          err.message = err.message || e.message;
          track.errors.push(err);}

      });
    });
    track.errors.onMethodName = (function (methodName, err) 
    {
      var obj = window, i, len, parts, part;
      parts = methodName.split('.');
      part = parts[0];
      len = parts.length;
      for(i = 0;i < len - 1;i ++)
      {
        part = parts[i];
        obj = obj[part];
      }
      if(FN_TYPE !== (typeof obj[part]))
      {
        return;
      }
      obj[part] = track.errors.onMethod(obj[part], err);
    });
    track.errors.push = (function (err) 
    {
      if(vars.libUrl)
      {
        track.load.script(vars.libUrl, callQueue);
        vars.libUrl = null;
        delete vars.libUrl;
      }
      if(! track.xhr)
      {
        callQueue.push((function () 
        {
          track.errors.push(err);
        }));
        return;
      }
      else
        if(callQueue)
        {
          callQueue = null;
        }
      track.xhr.post({
        url : vars.reportUrl,
        data : new UserAgentError(err)
      });
    });
    function UserAgentError(err) 
    {
      this.code = err.code + '';
      this.message = err.message;
      this.unique = err.unique;
      this.originTreeId = vars.originTreeId;
      this.appName = vars.appName;
      this.pageKey = pagekey();
    }
  })(window.track, window);
  (function (track) 
  {
    if(! track || ! track.errors)
    {
      return;
    }
    track.errors.codes = {
      FZ_CACHE_MISS : 601,
      FZ_EMPTY_NODE : 602,
      FZ_DUST_RENDER : 603,
      FZ_DUST_CHUNK : 604,
      FZ_DUST_MISSING_TL : 605,
      FZ_RENDER : 606,
      FZ_XHR_BAD_STATUS : 607,
      FZ_XHR_BAD_CONTENT_TYPE : 608,
      FZ_JSON_PARSE : 609,
      CTRL_INIT : 701
    };
  })(window.track);
  (function (track, window) 
  {
    if(! track || ! track.errors)
    {
      return;
    }
    track.errors.bootstrap = (function () 
    {
      if(window.fs && track.errors)
      {
        window.fs.on('error', (function (e) 
        {
          var unique;
          if(window.JSON)
          {
            unique = {
              id : e.id
            };
            if(e.xhr)
            {
              unique.xhr = e.xhr;
            }
            try
{              e.unique = window.JSON.stringify(unique);}
            catch(err)
{              }

          }
          track.errors.push(e);
        }));
      }
    });
    track.errors.bootstrap();
  })(window.track, window);
  