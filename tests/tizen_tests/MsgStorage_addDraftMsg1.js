/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/
var __result1, __result2;
function successCallback() {
  __result1 = 1;
}
// Define the error callback.
function errorCallback(error) {
  __result2 = error.name;
}
function serviceListCB(services) {
   if (services.length > 0) {
     var msg = new tizen.Message("messaging.sms", {to: ["+34666666666", "+34888888888"]});
     services[0].messageStorage.addDraftMessage(msg, successCallback, errorCallback);
   }
 }
tizen.messaging.getMessageServices("messaging.sms", serviceListCB);

var __expect1 = 1
var __expect2 = "NetworkError"