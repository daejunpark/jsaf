/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

var appControl = new tizen.ApplicationControl(
               "http://tizen.org/appcontrol/operation/create_content",
               null,
               "image/jpg",
               null);
var notificationDict = {
          content : "This is a simple notification.",
          iconPath : "images/image1.jpg",
          soundPath : "music/Over the horizon.mp3",
          vibration : true,
          appControl : appControl};

var noti = new tizen.StatusNotification("SIMPLE",
          "Simple notification", notificationDict);

var __result2 = tizen.notification.post(noti);

var __result1 = noti.id;
var __expect1 = "a";
var __expect2 = undefined;