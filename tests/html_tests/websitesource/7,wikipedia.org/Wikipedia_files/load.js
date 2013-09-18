(function () {
    "use strict";

    function $(id) {
        return document.getElementById(id);
    }

    function addEvent(obj, evt, fn) {
        if (!obj) {
            return;
        }
        if (obj.addEventListener) {
            obj.addEventListener(evt, fn, false);
        } else if (obj.attachEvent) {
            obj.attachEvent("on" + evt, fn);
        }
    }

    function addLoadEvent(fn) {
        addEvent(window, "load", fn);
    }

    function getLang() {
        var uiLang = navigator.language || navigator.userLanguage,
            results = document.cookie.match(/(?:^|\W)searchLang=([^;]+)/);
        return (results ? results[1] : uiLang).toLowerCase();
    }
    addLoadEvent(function () {
        var iso639, select, options, i, len, lang = getLang();
        if (!lang) {
            return;
        }
        iso639 = lang.match(/^\w+/);
        if (!iso639) {
            return;
        }
        iso639 = (iso639[0] === "nb") ? "no" : iso639[0];
        select = $("searchLanguage");
        if (select) {
            options = select.getElementsByTagName("option");
            for (i = 0, len = options.length; i < len; i += 1) {
                if (options[i].value === lang) {
                    select.value = lang;
                    break;
                }
            }
        }
    });

    function convertChinese(lang) {
        var i, elt, txtAttr = "data-convert-Hans",
            titleAttr = "data-convertTitle-Hans";
        if ("zh-hans,zh-cn,zh-sg,zh-my,".indexOf(lang + ",") === -1) {
            return;
        }
        var ids = ["zh_art", "zh_others", "zh_search", "zh_tag", "zh_top10"];
        for (i = 0; i < ids.length; i += 1) {
            if ((elt = $(ids[i]))) {
                if (elt.hasAttribute(txtAttr)) {
                    elt.innerHTML = elt.getAttribute(txtAttr);
                }
                if (elt.hasAttribute(titleAttr)) {
                    elt.title = elt.getAttribute(titleAttr);
                }
            }
        }
    }

    function convertZhLinks() {
        var locale, lang = getLang();
        if (lang.indexOf("zh") !== 0) {
            return;
        }
        locale = lang.substring(3);
        if (locale === "mo") {
            locale = "hk";
        } else if (locale === "my") {
            locale = "sg";
        }
        if ("cn,tw,hk,sg,".indexOf(locale) >= 0) {
            $("zh_wiki").href += "zh-" + locale + "/";
            $("zh_others").href = $("zh_others").href.replace("wiki/", "zh-" + locale + "/");
        }
        convertChinese(lang);
    }

    function setupSuggestions() {
        if (window.HTMLDataListElement === undefined) {
            return;
        }
        var list = document.createElement("datalist"),
            search = $("searchInput");
        list.id = "suggestions";
        document.body.appendChild(list);
        search.autocomplete = "off";
        search.setAttribute("list", "suggestions");
        addEvent(search, "input", function () {
            var head = document.getElementsByTagName("head")[0],
                hostname = window.location.hostname.replace("www.", $("searchLanguage").value + "."),
                script = $("api_opensearch");
            if (script) {
                head.removeChild(script);
            }
            script = document.createElement("script");
            script.id = "api_opensearch";
            script.src = "//" + hostname + "/w/api.php?action=opensearch&limit=10&format=json&callback=portalOpensearchCallback&search=" + this.value;
            head.appendChild(script);
        });
    }
    window.portalOpensearchCallback = function (xhrResults) {
        var i, suggestions = $("suggestions"),
            oldOptions = suggestions.children;
        for (i = 0; i < xhrResults[1].length; i += 1) {
            var option = oldOptions[i] || document.createElement("option");
            option.value = xhrResults[1][i];
            if (!oldOptions[i]) {
                suggestions.appendChild(option);
            }
        }
        for (i = suggestions.children.length - 1; i >= xhrResults[1].length; i -= 1) {
            suggestions.removeChild(suggestions.children[i]);
        }
    };

    function setLang(lang) {
        var uiLang = navigator.language || navigator.userLanguage,
            date = new Date();
        if (uiLang.match(/^\w+/) === lang) {
            date.setTime(date.getTime() - 1);
        } else {
            date.setFullYear(date.getFullYear() + 1);
        }
        document.cookie = "searchLang=" + lang + ";expires=" + date.
        toUTCString() + ";domain=" + location.host + ";";
    }
    addLoadEvent(function () {
        var params, i, param, search = $("searchInput");
        convertZhLinks();
        if (search) {
            search.setAttribute("results", "10");
            setupSuggestions();
            if (search.autofocus === undefined) {
                search.focus();
            } else {
                window.scroll(0, 0);
            }
            params = location.search && location.search.substr(1).split("&");
            for (i = 0; i < params.length; i += 1) {
                param = params[i].split("=");
                if (param[0] === "search" && param[1]) {
                    search.value = decodeURIComponent(param[1]);
                    return;
                }
            }
        }
        addEvent($("searchLanguage"), "change", function () {
            setLang(this.value);
        });
    });
    addLoadEvent(function () {
        var uselang = document.searchwiki && document.searchwiki.elements.uselang;
        if (uselang) {
            uselang.value = (navigator.language || navigator.userLanguage).toLowerCase().split("-")[0];
        }
    });
}());
if (!window.mw) {
    window.mw = window.mediaWiki = {
        loader: {
            state: function () { }
        }
    };
};
mw.loader.state({
    "ext.gadget.wm-portal": "ready"
});
/* cache key: metawiki:resourceloader:filter:minify-js:7:d03342439ab21ca9ae110a678b29415e */
