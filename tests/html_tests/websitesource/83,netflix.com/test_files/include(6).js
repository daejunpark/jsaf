$(function() {
    var formSelector = "#regform.inline-registration",
        form = $(formSelector),
        useAjaxRequest = true,
        smartSubmit = function() {
            var submitButton = $("#regFormButton");
            submitButton.html(submitButton.attr("data-wait")).attr("disabled", "disabled");
            var onSuccess = function(data) {
                var  parsedData = null;
                try {
                    parsedData  = $.parseJSON(data);
                } catch(e) { }
                if (parsedData && parsedData.status && parsedData.status.responseCode && parsedData.status.responseCode == 200) {
                    location.href = parsedData.successUrl;
                }
                else {
                    var newForm = $( data ).find("#regform");
                    if (newForm.length > 0) {
                        form.html(newForm.html());
                    }
                    else {
                        useAjaxRequest = false;
                        form.submit();
                    }
                } 
            },
            onFailure = function(data) {
                useAjaxRequest = false;
                form.submit();
            };
            $.ajax({
                    type: "post",
                    url: "/Registration",
                    dataType: "html",
                    data: form.serialize(),
                    success: onSuccess,
                    error: onFailure
                });
        },
        onFormSubmit = function(event) {
            if (useAjaxRequest) {
                event.preventDefault();
                smartSubmit();
            }
        };
    form.unbind("submit");
    form.submit(onFormSubmit);
});if (!netflix.html5shim) netflix.html5shim = {};
jQuery.extend(netflix.html5shim, {
    placeholder: function() {
        var _clearInput = function(inputEle, placeholderText) {
            if(inputEle.attr('value') == placeholderText) {
                if (inputEle.hasClass('waspassword')) {
                    var replaceEl = _getSwappedPwdField(inputEle);

                    inputEle.replaceWith(replaceEl);
                    inputEle = replaceEl;
                    inputEle.focus();
                    inputEle.focus();   // yes, we need two
                }

                inputEle.attr('value', '').removeClass('placeholder');
            }
        },
        _handleBlur = function (inputEle, placeholderText) {
            if(inputEle.attr('value') == '') {
                if (inputEle.hasClass('waspassword')) {
                    inputEle.unbind();

                    var replaceEl = _getSwappedPwdField(inputEle);

                    inputEle.replaceWith(replaceEl);
                    inputEle = replaceEl;
                }
                inputEle.attr('value', placeholderText).addClass('placeholder');
            }
        },
        _handleDefaultText = function(inputEle, placeholderText) {
            inputEle.addClass('placeholder')

            if ('password'.toLowerCase() == inputEle.attr('type')) {
                var replaceEl = _getSwappedPwdField(inputEle);

                inputEle.replaceWith(replaceEl);
                inputEle = replaceEl;
            }

            if (inputEle.attr('value') == '') {
                inputEle.attr('value', placeholderText);
            }

            inputEle.bind({
                focus: function() { _clearInput(inputEle, placeholderText) },
                click: function() { _clearInput(inputEle, placeholderText) },
                keydown: function() { _clearInput(inputEle, placeholderText) },
                blur: function() { _handleBlur(inputEle, placeholderText) }
            });

            //Make sure on submit we clear out the input value
            inputEle.parents('form').bind('submit', function() {
                _clearInput(inputEle, placeholderText);
            });
        },
        _getSwappedPwdField = function (field) {
            var replacement = jQuery('<input>');

            replacement.attr("type", field.attr("type") === "password" ? "text" : "password");
            replacement.attr("class", field.attr("class"));
            replacement.attr("id", field.attr("id"));
            replacement.attr("name", field.attr("name"));
            replacement.attr("placeholder", field.attr("placeholder"));
            replacement.attr("size", field.attr("size"));
            replacement.attr("maxLength", field.attr("maxLength"));
            replacement.attr("autocomplete", field.attr("autocomplete"));
            replacement.attr("value", field.attr("value"));

            if (field.attr("type") === "password") {
                replacement.addClass("waspassword");
            } else {
                replacement.removeClass("waspassword");
            }

            return replacement;
        };

        //Check for placeholder support. Do only if placeholder is not natively supported by browser
        if (!Modernizr.input['placeholder']) {
            $('input').each(function(index, inputEle) {
                var inputEle = $(inputEle),
                    placeholderText = inputEle.attr('placeholder');

                if(placeholderText) {
                    _handleDefaultText(inputEle, placeholderText);
                }
            });
        }
    }
});
if (!netflix.utils) netflix.utils = {};
jQuery.extend(netflix.utils, {
    /**
       Prevents form double submit by disabling the form submit button.
       usage: jQuery("#login-form-contBtn").bind('click', {formId: "login-form"}, netflix.utils.preventDoubleSubmit);
    **/
    preventDoubleSubmit: function(evt) {
        var target = evt.target;  //expected target to be an HTMLButtonElement or HTMLInputElement
        if(target && target.disabled == false) {
            if(evt.data && evt.data.formId) {
                target.innerHTML = target.getAttribute("data-wait");
                target.disabled = true;
                document.getElementById(evt.data.formId).submit();
            }
        }
    },
    /**
      Popup a new window. Example usage: 
      $('#whatsthis").click(function(evt) {
          netflix.utils.popupWin(evt.target.href, {width:350, height:410});
          evt.preventDefault();
      });

     In bootstrap, we globally handle appending a class of .popwin to your link.
    **/
    popupWin: function(whereto, override_cfg) {
       var cfg = {
         width: 500,
         height: 450,
         name: "nf_static_popup",
         resizable: 1,
         scrollbars: 1,
         top: (window.screenTop === undefined ? window.screenY : window.screenTop)+10,
         left: (window.screenLeft === undefined ? window.screenX : window.screenLeft)+10
       },
       sFeatures = "";
       if(override_cfg) {
           cfg = jQuery.extend(cfg, override_cfg);
       }
       sFeatures = ["toolbars=0,statusbars=0,menubars=0,location=0,scrollbars=", cfg.scrollbars, ",resizable=", cfg.resizable, ",width=", cfg.width, ",height=", cfg.height, ",top=", cfg.top, ",left=", cfg.left].join("");
       pWin=window.open(whereto, cfg.name, sFeatures);
       pWin.focus();
    },
    /** Width MUST be set in data-width, else it will take default in function. Height, dialogclass and alt url are optional. **/
    popupLayer: function(whereto, override_cfg) {
       var cfg = {
            width:700,
            height: "auto",
            dclass: "nflx-layer",
            pop: false,
            url: whereto
       }
       if(override_cfg) {
           cfg = jQuery.extend(cfg, override_cfg);
       }
       $("#nflx-layer").dialog('option', 'width', cfg.width).dialog('option', 'height', cfg.height).dialog('option', 'dialogClass', cfg.dclass);
       if(cfg.pop) {
         $("#nflx-layer").load(cfg.url + " #popupbody", function() { $(this).dialog("open"); });
       } else {
         $("#nflx-layer").load(cfg.url, function() { $(this).dialog("open"); });
       }
    },
    heightEqualizer: function() {
        var biggestHeight = 0;
        $('.heighteq').each(function(){  
            if($(this).height() > biggestHeight){  
                biggestHeight = $(this).height();  
            }  
        });  
        $('.heighteq').height(biggestHeight); 
    },
    heightMatcher: function (sharedParent) {
        var ref = jQuery('.heightsrc', sharedParent);
        var tgts = jQuery('.heightdst', sharedParent);

        jQuery.each(tgts, function (index, tgt) {
            jQuery(tgt).height(ref.height());
        });
    },
    isLinkSecure: function() { 
        return location.protocol == "https:"; 
    } ,
    nowTime: function() {
        now = new Date();
        return now.getMinutes() + "-" + now.getSeconds();
    },
    setCookie: function (name, value, hoursToLive, domain, path) {
        var cookie = name + "=" + escape(value);

        if (typeof hoursToLive != 'undefined') {
            var exdate = new Date(new Date().valueOf() + (hoursToLive * 60 * 60 * 1000));
            cookie += '; expires=' + exdate.toUTCString();
        }

        cookie += '; path=' + (typeof path != 'undefined' ? path : '/');

        if (typeof domain != 'undefined') {
            cookie += '; domain=' + domain;
        }

        document.cookie = cookie;
    },
    getCookie: function(name) {
        var i,x,y,cookies=document.cookie.split(";");
        for (i=0;i<cookies.length;i++) {
            x=cookies[i].substr(0,cookies[i].indexOf("="));
            y=cookies[i].substr(cookies[i].indexOf("=")+1);
            x=x.replace(/^\s+|\s+$/g,"");
            if (x==name) {
                return unescape(y);
            }
        }
    },
    loadScript: function (src, async, callback) {
        if (typeof src != 'undefined') {
            if (src.indexOf('http:') != -1) {
                if (typeof console != 'undefined' && console.log) {
                    console.log("ignoring script load directive: script's protocol is HTTP, not HTTPS");
                }
                jQuery.get("/beacons?loadScriptSkipped=" + escape(src));
            } else {
                var sNew = document.createElement("script");
                sNew.async = typeof async == 'undefined' ? false : async;
                sNew.src = src;
                var s0 = document.getElementsByTagName('script')[0];

                if (typeof callback != 'undefined') {
                    // most browsers
                    sNew.onload = function() {
                        if ( ! sNew.onloadDone ) {
                            sNew.onloadDone = true;
                            callback();
                        }
                    };
                    // IE 6 & 7
                    sNew.onreadystatechange = function() {
                        if ( ( "loaded" === sNew.readyState || "complete" === sNew.readyState ) && ! sNew.onloadDone ) {
                            sNew.onloadDone = true;
                            callback();
                        }
                    }
                }

                s0.parentNode.insertBefore(sNew, s0);
            }
        }
    },
    decodeHtml: function (str) {
        return jQuery('<div/>').html(str).text();
    },
    beacon: function (str) {
        var page = jQuery('body').attr('id');
        jQuery.get('https://signup.netflix.com/beacons?' + str + '&page=' + page + '&tstamp=' + new Date().getTime());
    }
});netflix = typeof netflix != 'undefined' ? netflix : {};
netflix.bootstrap = typeof netflix.bootstrap != 'undefined' ? netflix.bootstrap : {};

netflix.bootstrap.initBOB = function () {
    $("ol.bobMovieList,ul.bobMovieList,td.titleCol,.bobAble").delegate(".mdpLink", "mouseover", function(evt) {
        if (netflix.BobMovieManager) {
            var targetEl = $(evt.target);
            if(!targetEl.is('a')) {
                targetEl = targetEl.parent('.mdpLink');
            }
            if(!targetEl) {
                return;
            }
            netflix.BobMovieManager.attach(targetEl);
            netflix.BobMovieManager.show(targetEl);
        }
    });
};

jQuery(document).ready(function($) {

        $("body").delegate('.popupwin', 'click', function(evt) {
            var linkEle = $(evt.target);
            var whereto = linkEle.attr('href');
            if (typeof whereto == 'undefined') {
                while (!linkEle.is('a') && !linkEle.is('body')) {
                    linkEle = linkEle.parent();
                }
                whereto = linkEle.attr('href');
            }
            netflix.utils.popupWin( whereto, {width:linkEle.attr('data-width'), height:linkEle.attr('data-height')});
            evt.preventDefault();
        });
        
        netflix.bootstrap.initBOB();

        /** Width MUST be set in data-width. Others are optional. **/
        $("body").delegate('.popuplayer', 'click', function(evt) {
                var linkEle = $(evt.target);
                if (linkEle.prop("tagName") != "A") {
                    linkEle = linkEle.closest("a")
                }
                if (linkEle.length > 0) {
                    evt.preventDefault();
                    netflix.utils.popupLayer( linkEle.attr('href'), {width:linkEle.attr('data-width'), height:linkEle.attr('data-height'), dclass:linkEle.attr('data-class'), url:linkEle.attr('data-url'), pop:linkEle.attr('data-pop')});
                }
            });

        $("body").delegate(".popframe-iframe", "click", function(event) {
            var target = $(event.target);
            if (target.prop("tagName") != "A") {
                target = target.closest("A");
            }
            if (target.length > 0) {
                event.preventDefault();
                
                var dialogLayer = $("#nflx-layer");
                var iframe = '<iframe id="nflx_iframe_id" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="no" />';
                dialogLayer.html(iframe);
                if (target.data("width")) {
                   dialogLayer.dialog("option", "width", target.data("width"));
                }
                if (target.data("height")) {
                   dialogLayer.dialog("option", "height", target.data("height"));
                }

                $("#nflx_iframe_id").attr("src", target.data("iframe")).one("load", function(){
                    $("#nflx-layer").dialog("open").dialog({
                        height: $(this.contentDocument.body).height() + 42,
                        width: $(this.contentDocument.body).width(),
                        position: "center"
                    }).dialog('option', 'position', 'center');
                });
            }
        });

        $("body").append('<div id="nflx-layer"></div>');
        $("#nflx-layer").dialog({
             dialogClass:"nflx-layer",
             modal: true,
             resizable: true,
             draggable: false,
             position: "center",
             autoOpen: false,
             title:"&nbsp;"
        });
        $(".ui-dialog-title").append('<img src="https://netflix.hs.llnwd.net/e1/us/layout/headers/nflogo_small.gif" border="0"/>');
        $(".ui-dialog-titlebar-close").html("<a id='devices-layer-close' class='close svf-button svfb-default' href='#'><span></span></a>");
        
        $(".ui-widget-overlay").live("click", function() {
                $("#nflx-layer").dialog("close");
            });
        $(window).bind("resize scroll", function() {
            if ($("#nflx-layer").dialog("isOpen")) {
                 $("#nflx-layer").dialog('option', 'position', 'center');
            }
        });

        if(jQuery.browser.msie) {
            //hide extra comma. IE browser doesn't support last-child css property
            $('.delimitedList li:last-child span').css('display', 'none');
        }
        netflix.html5shim.placeholder();

        jQuery(window).trigger('bootstrapped');
});

$(function() {
    $(".abtest-info").click(function(event) {
        var ctrlClick = event.ctrlKey;
        if (ctrlClick) {
            $(this).css({"width": "auto"});
        }
    });
});
netflix.IntlLanding = {};
netflix.IntlLanding.init = function () {
    jQuery("a#linkUs").click(function (event) {
        var buttonOpts = {}
        buttonOpts[netflix.layertext['button_continue']] = function() {
            window.location = "http://www.netflix.com/?fcld=true";
        };
        buttonOpts[netflix.layertext['button_cancel']] = function() {
            jQuery(this).dialog("close");
        };
        jQuery("<div>" + netflix.layertext['layer_text'] + "</div>").dialog({
            modal: true,
            width: 400,
            position: 'center',
            buttons : buttonOpts,
            close: function(event, ui) {
                jQuery(this).remove();
            }
        });

        event.preventDefault();
    });
};

(function($) {
    $(document).ready(function() {
        netflix.IntlLanding.init();
    });
})(jQuery);