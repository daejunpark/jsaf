  try
{    var dateObject = tizen.time.getCurrentDateTime();}
  catch(e)
{    con.log("exception while invoking getCurrentDateTime" + e.name + e.message);}

  try
{    var dateObjectforUTC = tizen.time.getCurrentDateTime();}
  catch(e)
{    con.log("exception while invoking getCurrentDateTime" + e.name + e.message);}

  function getCurrentDateTime() 
  {
    con.log("get current time");
    try
{      var current_date = tizen.time.getCurrentDateTime();}
    catch(e)
{      con.log("exception while invoking getCurrentDateTime" + e.name + e.message);}

    con.result("current date(toLocaleDateString) : " + current_date.toLocaleDateString());
    con.result("current date(toLocaleTimeString) : " + current_date.toLocaleTimeString());
    con.result("current date(toLocaleString)     : " + current_date.toLocaleString());
    con.result("current date(toDateString)       : " + current_date.toDateString());
    con.result("current date(toTimeString)       : " + current_date.toTimeString());
    con.result("current date(toString)           : " + current_date.toString());
    con.result("current date(toUTC)           : " + current_date.toUTC().toString());
  }
  function getLocalTimezone() 
  {
    con.log("get local timezone");
    try
{      var localTimezone = tizen.time.getLocalTimezone();}
    catch(e)
{      con.log("exception while invoking getLocalTimezone" + e.name + e.message);}

    con.result("The local time zone is " + localTimezone);
  }
  function getAvailableTimezones() 
  {
    con.log("get available timezones");
    try
{      var availTimezones = tizen.time.getAvailableTimezones();}
    catch(e)
{      con.log("exception while invoking getAvailableTimezones" + e.name + e.message);}

    con.result("The device supports " + availTimezones.length + " time zones.");
    for(var i = 0;i < availTimezones.length;i ++)
      con.result(i + " : " + availTimezones[i]);
  }
  function getDateFormat() 
  {
    con.log("get DateFormat");
    try
{      var shortformat = tizen.time.getDateFormat(true);
      var longformat = tizen.time.getDateFormat(false);}
    catch(e)
{      con.log("exception while invoking getDateFormat" + e.name + e.message);}

    con.result("The short format is " + shortformat);
    con.result("The long format is " + longformat);
  }
  function getTimeFormat() 
  {
    con.log("get TimeFormat");
    try
{      var timeformat = tizen.time.getTimeFormat();}
    catch(e)
{      con.log("exception while invoking getTimeFormat" + e.name + e.message);}

    con.result("The time format is " + timeformat);
  }
  function isLeapYear() 
  {
    con.log("calling isLeapYear");
    try
{      var isleap = tizen.time.isLeapYear(2016);}
    catch(e)
{      con.log("exception while invoking isLeapYear" + e.name + e.message);}

    con.result("Is 2016 leap year? : " + isleap);
  }
  function getDate() 
  {
    var radio = document.getElementsByName("time");
    var sizes = radio.length;
    var result;
    for(i = 0;i < sizes;i ++)
    {
      if(radio[i].checked == true)
      {
        switch(radio[i].value){
          case "date":
            result = dateObject.getDate();
            break;

          case "day":
            result = dateObject.getDay();
            break;

          case "fullyear":
            result = dateObject.getFullYear();
            break;

          case "hours":
            result = dateObject.getHours();
            break;

          case "msec":
            result = dateObject.getMilliseconds();
            break;

          case "min":
            result = dateObject.getMinutes();
            break;

          case "month":
            result = dateObject.getMonth() + 1;
            break;

          case "sec":
            result = dateObject.getSeconds();
            break;

          
        }
      }
    }
    con.result("result : " + result);
  }
  function setDate() 
  {
    var text = document.getElementById("setTime").value;
    var radio = document.getElementsByName("time");
    var sizes = radio.length;
    text = Number(text);
    for(i = 0;i < sizes;i ++)
    {
      if(radio[i].checked == true)
      {
        switch(radio[i].value){
          case "date":
            dateObject.setDate(text);
            break;

          case "day":
            dateObject.setDay(text);
            break;

          case "fullyear":
            try
{              dateObject.setFullYear(text);}
            catch(e)
{              alert(e.name + e.message);}

            break;

          case "hours":
            dateObject.setHours(text);
            break;

          case "msec":
            dateObject.setMilliseconds(text);
            break;

          case "min":
            dateObject.setMinutes(text);
            break;

          case "month":
            dateObject.setMonth(text + 1);
            break;

          case "sec":
            dateObject.setSeconds(text);
            break;

          
        }
      }
    }
    try
{      document.getElementById("TZObject").innerHTML = dateObject.toString();}
    catch(e)
{      alert("exception while invoking getCurrentDateTime" + e.name + e.message);}

  }
  function getUTCDate() 
  {
    var radio = document.getElementsByName("time");
    var sizes = radio.length;
    var result;
    for(i = 0;i < sizes;i ++)
    {
      if(radio[i].checked == true)
      {
        switch(radio[i].value){
          case "date":
            result = dateObject.getUTCDate();
            break;

          case "day":
            result = dateObject.getUTCDay();
            break;

          case "fullyear":
            result = dateObject.getUTCFullYear();
            break;

          case "hours":
            result = dateObject.getUTCHours();
            break;

          case "msec":
            result = dateObject.getUTCMilliseconds();
            break;

          case "min":
            result = dateObject.getUTCMinutes();
            break;

          case "month":
            result = dateObject.getUTCMonth() + 1;
            break;

          case "sec":
            result = dateObject.getUTCSeconds();
            break;

          
        }
      }
    }
    con.result("result : " + result);
  }
  function setUTCDate() 
  {
    var text = document.getElementById("setTime").value;
    var radio = document.getElementsByName("time");
    var sizes = radio.length;
    text = Number(text);
    for(i = 0;i < sizes;i ++)
    {
      if(radio[i].checked == true)
      {
        switch(radio[i].value){
          case "date":
            dateObjectforUTC.setUTCDate(text);
            break;

          case "day":
            dateObjectforUTC.setUTCDay(text);
            break;

          case "fullyear":
            try
{              dateObjectforUTC.setUTCFullYear(text);}
            catch(e)
{              alert(e.name + e.message);}

            break;

          case "hours":
            dateObjectforUTC.setUTCHours(text);
            break;

          case "msec":
            dateObjectforUTC.setUTCMilliseconds(text);
            break;

          case "min":
            dateObjectforUTC.setUTCMinutes(text);
            break;

          case "month":
            dateObjectforUTC.setUTCMonth(text + 1);
            break;

          case "sec":
            dateObjectforUTC.setUTCSeconds(text);
            break;

          
        }
      }
    }
    try
{      document.getElementById("TZObjectUTC").innerHTML = dateObjectforUTC.toString();}
    catch(e)
{      alert("exception while invoking getCurrentDateTime" + e.name + e.message);}

  }
  function getTimezone() 
  {
    con.log("get timezone");
    var result;
    try
{      result = dateObject.getTimezone();}
    catch(e)
{      con.log("exception while invoking getTimezone" + e.name + e.message);}

    con.result("The timezone is " + result);
  }
  function toTimezone() 
  {
    con.log("to timezone");
    try
{      dateObject = dateObject.toTimezone("Asia/Hong_Kog");}
    catch(e)
{      con.log("exception while invoking toTimezone" + e.name + e.message);}

    con.result("The timezone is " + dateObject.getTimezone());
  }
  function toLocalTimezone() 
  {
    con.log("to Local Timezone");
    try
{      dateObject = dateObject.toLocalTimezone();}
    catch(e)
{      con.log("exception while invoking toLocalTimezone" + e.name + e.message);}

    con.result("The timezone is " + dateObject.getTimezone());
  }
  function difference() 
  {
    con.log("get difference ");
    var diff_TZDate = new tizen.TZDate(2012, 11, 25, 10, 24, 5, 354, "Asia/Hong_Kong");
    var duration;
    try
{      duration = dateObject.difference(diff_TZDate);}
    catch(e)
{      con.log("exception while invoking difference" + e.name + e.message);}

    con.result("The difference is " + duration.length + duration.unit);
  }
  function equalsTo() 
  {
    con.log("equalsTo ");
    try
{      var diff_TZDate = new tizen.TZDate(2012, 11, 25, 10, 24, 5, 354, "Asia/Hong_Kong");}
    catch(e)
{      con.log("exception while crating TZDate " + e.name + e.message);}

    var result;
    try
{      result = dateObject.equalsTo(diff_TZDate);}
    catch(e)
{      con.log("exception while invoking equalsTo" + e.name + e.message);}

    con.result("Is " + dateObject.toString() + " and " + diff_TZDate.toString() + " equal? " + result);
  }
  function earlierThan() 
  {
    con.log("earlierThan ");
    var diff_TZDate = new tizen.TZDate(2012, 11, 25, 10, 24, 5, 354, "Asia/Hong_Kong");
    var result;
    try
{      result = dateObject.earlierThan(diff_TZDate);}
    catch(e)
{      con.log("exception while invoking earlierThan" + e.name + e.message);}

    con.result("Is " + dateObject.toString() + " earlier than " + diff_TZDate.toString() + " ? " + result);
  }
  function laterThan() 
  {
    con.log("laterThan ");
    var diff_TZDate = new tizen.TZDate(2012, 11, 25, 10, 24, 5, 354, "Asia/Hong_Kong");
    var result;
    try
{      result = dateObject.laterThan(diff_TZDate);}
    catch(e)
{      con.log("exception while invoking  laterThan" + e.name + e.message);}

    con.result("Is " + dateObject.toString() + "  later than " + diff_TZDate.toString() + " ? " + result);
  }
  function addDuration() 
  {
    con.log("add Duration ");
    try
{      var duration = new tizen.TimeDuration(1, "DAYS");}
    catch(e)
{      con.log("error while creating TimeDuration object" + e.name + e.message);}

    try
{      dateObject = dateObject.addDuration(duration);}
    catch(e)
{      con.log("exception while invoking  addDuration" + e.name + e.message);}

    con.result(duration.length + duration.unit + " added " + dateObject.toString());
  }
  function getTimezoneAbbreviation() 
  {
    con.log("getTimezoneAbbreviation");
    try
{      var result = dateObject.getTimezoneAbbreviation();}
    catch(e)
{      con.log("exception while invoking getTimezoneAbbreviation" + e.name + e.message);}

    con.result("The timezone abbreviation is " + result);
  }
  function secondsFromUTC() 
  {
    con.log("secondsFromUTC");
    try
{      var result = dateObject.secondsFromUTC();}
    catch(e)
{      con.log("exception while invoking secondsFromUTC" + e.name + e.message);}

    con.result("The seconds from UTC are " + result);
  }
  function isDST() 
  {
    con.log("isDST");
    try
{      var result = dateObject.isDST();}
    catch(e)
{      con.log("exception while invoking isDST" + e.name + e.message);}

    con.result("is DST?  " + result);
  }
  function getPreviousDSTTransition() 
  {
    con.log("getPreviousDSTTransition");
    try
{      var result = dateObject.getPreviousDSTTransition();}
    catch(e)
{      con.log("exception while invoking getPreviousDSTTransition" + e.name + e.message);}

    if(result == null)
      con.result("PreviousDSTTransition" + result);
    else
      con.result("PreviousDSTTransition" + result.toString());
  }
  function getNextDSTTransition() 
  {
    con.log("getNextDSTTransition");
    try
{      var result = dateObject.getNextDSTTransition();}
    catch(e)
{      con.log("exception while invoking getNextDSTTransition" + e.name + e.message);}

    if(result == null)
      con.result("Next DST Transition" + result);
    else
      con.result("Next DST Transition" + result.toString());
  }
  function test() 
  {
    alert("here");
    var testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear(undefined);}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log("converstion from undefined to long success");
    }
    else
    {
      con.log("setFullYear Error undefined" + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear(null);}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log("converstion from null to long success");
    }
    else
    {
      con.log("setFullYear Error null" + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear(true);}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log("converstion from boolean to long success");
    }
    else
    {
      con.log("setFullYear Error boolean" + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear("");}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log('converstion from ""(empty string) to long success');
    }
    else
    {
      con.log('setFullYear Error ""(empty string) ' + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear("2015");}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 2015)
    {
      con.log('converstion from "2015"(numeric string) to long success');
    }
    else
    {
      con.log('setFullYear Error "2015"(numeric string) ' + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear("asdf");}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log('converstion from "asdf"(non-numeric string) to long success');
    }
    else
    {
      con.log('setFullYear Error "asdf"(non-numeric string) ' + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear(Infinity);}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log('converstion from Infinity to long success');
    }
    else
    {
      con.log('setFullYear Error Infinity ' + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear(NaN);}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log('converstion from NaN to long success');
    }
    else
    {
      con.log('setFullYear Error NaN ' + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear([2015, ]);}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 2015)
    {
      con.log('converstion from numeric elt to long success');
    }
    else
    {
      con.log('setFullYear Error numeric elt ' + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear(['a', ]);}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log('converstion from any other array to long success');
    }
    else
    {
      con.log('setFullYear Error any other array ' + e.name + e.message);
    }
    testingTZDateObj = new tizen.TZDate(2012, 1, 1, 1, 1, 1, 1);
    try
{      testingTZDateObj.setFullYear((function () 
      {
        
      }));}
    catch(e)
{      con.log("setFullYear Error" + e.name + e.message);}

    if(testingTZDateObj.getFullYear() == 1)
    {
      con.log('converstion from function to long success');
    }
    else
    {
      con.log('setFullYear Error function ' + e.name + e.message);
    }
  }