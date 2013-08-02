  var webcam = {
    filePath : null,
    begin : (function () 
    {
      this.filePath = null;
      return webcam.sendCmd("cam/start");
    }),
    stop : (function () 
    {
      return webcam.sendCmd("cam/stop");
    }),
    save : (function () 
    {
      var cmd = "cam/save?name=" + this.filePath;
      return webcam.sendCmd(cmd);
    }),
    saveAndBegin : (function () 
    {
      webcam.stop();
      setTimeout("webcam.save()", 1000);
      setTimeout("webcam.begin()", 2000);
    }),
    restart : (function (fn) 
    {
      if(fn)
      {
        rootpath = "";
        if(Config.rootpath)
        {
          rootpath = Config.rootpath;
        }
        this.filePath = rootpath + fn + ".avi";
      }
      if(this.filePath == null)
      {
        webcam.stop();
        setTimeout("webcam.begin()", 1000);
      }
      else
      {
        setTimeout(webcam.saveAndBegin, Config.recordTime / 2);
      }
    }),
    serverurl : "http://109.123.112.190:8080/",
    setUrl : (function (url) 
    {
      webcam.serverurl = url;
    }),
    setRootPath : (function (path) 
    {
      webcam.rootpath = path;
    }),
    createXmlHttp : (function () 
    {
      xmlhttp = null;
      if(window.XMLHttpRequest)
      {
        xmlhttp = new XMLHttpRequest();
      }
      else
        if(window.ActiveXObject)
        {
          xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        }
      return xmlhttp;
    }),
    sendCmd : (function (cmd) 
    {
      try
{        var sendURL;
        var myQuery = cmd;
        sendURL = webcam.serverurl;
        if(myQuery == '')
        {
          alert('No query available');
          return false;
        }
        sendURL += myQuery;
        xmlhttp = webcam.createXmlHttp();
        xmlhttp.open('GET', sendURL, true);
        xmlhttp.send(null);
        return (xmlhttp.status == 200);}
      catch(e)
{        }

    }),
    getResponse : (function () 
    {
      var responseData;
      if(this.readyState == 4)
      {
        if(this.status == 200)
        {
          alert("send command success");
        }
        else
        {
          alert("send command fail");
        }
      }
    })
  };