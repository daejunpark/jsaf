  if(self != top)
  {
    try
{      top.location.replace(location.href);}
    catch(e)
{      try
{        top.location = location;}
      catch(e)
{        }
}

    document.write('<plaintext style="display:none">');
  }
  