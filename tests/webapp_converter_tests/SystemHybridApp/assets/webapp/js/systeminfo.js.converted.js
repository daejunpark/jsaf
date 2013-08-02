  var batterylistener = null;
  var cpulistener = null;
  var storagelistener = null;
  var displaylistener = null;
  function addbatteryListener(obj) 
  {
    function onSuccessCallback(system) 
    {
      con.result("Event, battery level : " + system.level);
      con.result("Event, battery isCharging : " + system.isCharging);
    }
    if(obj.checked)
    {
      try
{        batterylistener = tizen.systeminfo.addPropertyValueChangeListener("BATTERY", onSuccessCallback);}
      catch(e)
{        con.log("error while adding listener" + e.name + e.message);}

      con.result("BATTERY listener added!");
    }
    else
    {
      try
{        tizen.systeminfo.removePropertyValueChangeListener(batterylistener);}
      catch(e)
{        con.log("error while removing listener" + e.name + e.message);}

      con.result("BATTERY listener removed!");
    }
  }
  function addcpuListener(obj) 
  {
    function onSuccessCallback(system) 
    {
      con.result("Event, CPU load : " + system.load);
    }
    if(obj.checked)
    {
      try
{        cpulistener = tizen.systeminfo.addPropertyValueChangeListener("CPU", onSuccessCallback);}
      catch(e)
{        con.log("error while adding listener" + e.name + e.message);}

      con.result("CPU listener added!");
    }
    else
    {
      try
{        tizen.systeminfo.removePropertyValueChangeListener(cpulistener);}
      catch(e)
{        con.log("error while removing listener" + e.name + e.message);}

      con.result("CPU listener removed!");
    }
  }
  function addstorageListener(obj) 
  {
    function onSuccessCallback(system) 
    {
      con.result("Event, Total Storage : " + system.units.length);
      for(var i = 0;i < system.units.length;i ++)
      {
        con.result("Event, Storage\tentry : " + i);
        con.result("Event, Storage\ttype : " + system.units[i].type);
        con.result("Event, Storage\tcapacity : " + system.units[i].capacity);
        con.result("Event, Storage\tavailable capacity : " + system.units[i].availableCapacity);
        con.result("Event, Storage\tisRemoveable : " + system.units[i].isRemoveable);
      }
    }
    if(obj.checked)
    {
      try
{        storagelistener = tizen.systeminfo.addPropertyValueChangeListener("STORAGE", onSuccessCallback);}
      catch(e)
{        con.log("error while adding listener" + e.name + e.message);}

      con.result("STORAGE listener added!");
    }
    else
    {
      try
{        tizen.systeminfo.removePropertyValueChangeListener(storagelistener);}
      catch(e)
{        con.log("error while removing listener" + e.name + e.message);}

      con.result("STORAGE listener removed!");
    }
  }
  function adddisplayListener(obj) 
  {
    function onSuccessCallback(system) 
    {
      con.result("Event, Display resolution width : " + system.resolutionWidth);
      con.result("Event, Display resolution height : " + system.resolutionHeight);
      con.result("Event, Display dots per inch width : " + system.dotsPerInchWidth);
      con.result("Event, Display dots per inch height : " + system.dotsPerInchHeight);
      con.result("Event, Display physical width : " + system.physicalWidth);
      con.result("Event, Display physical height : " + system.physicalHeight);
      con.result("Event, Display brightness : " + system.brightness);
    }
    if(obj.checked)
    {
      try
{        displaylistener = tizen.systeminfo.addPropertyValueChangeListener("DISPLAY", onSuccessCallback);}
      catch(e)
{        con.log("error while adding listener" + e.name + e.message);}

      con.result("DISPLAY listener added!");
    }
    else
    {
      try
{        tizen.systeminfo.removePropertyValueChangeListener(displaylistener);}
      catch(e)
{        con.log("error while removing listener" + e.name + e.message);}

      con.result("DISPLAY listener removed!");
    }
  }
  function getPropertyValue() 
  {
    var temp_property;
    function onSuccessCallback(system) 
    {
      con.log("in getPropertyValue successcallback");
      if(temp_property == "BATTERY")
      {
        con.result("battery level : " + system.level);
        con.result("battery isCharging : " + system.isCharging);
      }
      else
        if(temp_property == "CPU")
        {
          con.result("CPU load : " + system.load);
        }
        else
          if(temp_property == "STORAGE")
          {
            con.result("Total Storage : " + system.units.length);
            for(var i = 0;i < system.units.length;i ++)
            {
              con.result("Storage\tentry : " + i);
              con.result("Storage\ttype : " + system.units[i].type);
              con.result("Storage\tcapacity : " + system.units[i].capacity);
              con.result("Storage\tavailable capacity : " + system.units[i].availableCapacity);
              con.result("Storage\tisRemoveable : " + system.units[i].isRemoveable);
            }
          }
          else
            if(temp_property == "DISPLAY")
            {
              con.result("Display resolution width : " + system.resolutionWidth);
              con.result("Display resolution height : " + system.resolutionHeight);
              con.result("Display dots per inch width : " + system.dotsPerInchWidth);
              con.result("Display dots per inch height : " + system.dotsPerInchHeight);
              con.result("Display physical width : " + system.physicalWidth);
              con.result("Display physical height : " + system.physicalHeight);
              con.result("Display brightness : " + system.brightness);
            }
            else
            {
              con.result("Wrong property");
            }
    }
    function onErrorCallback() 
    {
      con.log("in getPropertyValue errorcallback");
    }
    temp_property = document.getElementById("property").value;
    con.log("invoking getPropertyValue");
    try
{      tizen.systeminfo.getPropertyValue(temp_property, onSuccessCallback, onErrorCallback);}
    catch(e)
{      con.log("error while invoking getPropertyValue" + e.name + e.message);}

  }