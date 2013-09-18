// You can define an _AnemoneParams global var with the following members (all are optional):
// uniqueUser: unique user ID (${eventRecord.uniqueUserId}) -- if missing, will generate a value
// appId: application ID (${eventRecord.application}) -- if missing, will use current hostname
// appVersion: application build version (${eventRecord.appBuildVersion})
// appDate: application build date (${eventRecord.appBuildDate}) -- if missing, will attempt to use document.lastModified
// logPageView: if true, each page view will be logged (only do this if you are not using server-side logging -- otherwise each page view will be logged twice)
// updateSession: if true, the session and referrer info in the cookie will be updated (only do this if you are not using server-side logging at all, or not using the Java servlet filter -- otherwise events may be double-counted and sessions may expire unexpectedly)
// domain: cookie domain (if not present, will use the last two components of the current hostname)
// url: base URL for callback (if not present, will use the current URL with "anemone.jhtml" instead of the page)
// getAppParams: function which returns an object whose property names/values will be logged for page views and events (values will be URL-encoded)
// getAppCookieChips: function which returns an object whose property names/values will be added to the Anemone cookie (names should begin with "x")
// getUserSegments: function which accepts an array of segment IDs and returns the array, possibly adding or removing segment IDs
// backFillRequired: (if !logPageView) true if anx cookie missing or empty (${eventRecord.backFillRequired})
// eventId: (if !logPageView) event ID for backfill (${eventRecord.eventId})
// maxSession: number of minutes after which a session will be deemed to have expired (default 30)
// cobrandParam: name of URL parameter containing cobrand ID (defaults to "anxrb")
// campaignParam: name of URL parameter containing campaign ID (defaults to "anxrc")
// partnerParam: name of URL parameter containing full partner ID (no default) -- only parsed if partner ID JS is included
// subIdParam: name of URL parameter containing partner sub-ID (defaults to "anxrs")
// mediumParam: name of URL parameter containing medium (defaults to "anxrm")
// suppressCookies: if true, will not set any cookies
// cookieExpirationMinutes: how long the anx cookie will persist
// newSessionOnDomainChange: if true, will treat an event with a referrer on a different domain as a new session

// Assumes toolbar detect script has already been loaded AND RUN with appropriate options for this product.

// Utilities

if (typeof(anxDebug) === 'undefined') {
	anxDebug = function (msg) {};
}

var JSUtil = {
	isNull: function (v) {
		return typeof(v) === 'undefined' || v === null;
	},

	isNotNull: function (v) {
		return typeof(v) !== 'undefined' && v !== null;
	},

	// Returns true if the argument is undefined, null, an empty string, or an
	// empty array.
	isEmpty: function (v) {
		return typeof(v) === 'undefined' || v === null || v.length === 0;
	},

	// Returns true if the argument is defined, not null, and not an empty string
	// or an empty array.
	isNotEmpty: function (v) {
		return typeof(v) !== 'undefined' && v !== null && v.length > 0;
	},

    isBlank: function (s) {
        return this.isEmpty(this.trim(s));
    },

	isNumber: function (v) {
		return typeof(v) === 'number';
	},

	isFunction: function (f) {
		return f !== null && typeof(f) === 'function';
	},

    trim: function (s) {
        if (this.isNull(s)) {
            return null;
        }
        if (s.length === 0) {
            return s;
        }
        return s.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    },

	// Same as "v ? v : defaultValue" or "v || defaultValue", but doesn't evaluate
	// v twice, and doesn't use defaultValue for 0 or false.
	defaultVal: function (v, defaultValue) {
		if (typeof(v) === 'undefined' || v === null || v === '') {
			if (typeof(defaultValue) === 'undefined') {
				return null;
            }
			return defaultValue;
		}
		return v;
	},

	// If v is undefined, null, empty, or not a number, returns defaultValue;
	// otherwise, converts v to an integer (using parseInt) and returns it.
	defaultIntVal: function (v, defaultValue) {
		var n = parseInt(v, 10);
		if (isNaN(n)) {
			return defaultValue;
        }
		return n;
	},

	// Returns the value of the global variable named x (a string, not a JS identifier),
	// or defaultValue if it does not exist or is null.  defaultValue can be omitted,
	// in which case it will be treated as null.
	optionalVal: function (x, defaultValue) {
		return this.defaultVal(window[x], defaultValue);
	},

	// Returns the value of obj.property, or defaultValue if obj does not exist
	// or is null or does not have such a property.  defaultValue can be omitted,
	// in which case it will be treated as null.  obj is a string, not a JS identifier.
	optionalProperty: function (obj, property, defaultValue) {
		var o = this.optionalVal(obj);
		if (o === null) {
			return defaultValue;
        }

		return this.defaultVal(o[property], defaultValue);
	},

	// Returns a new object containing the union of the properties of o1 and o2.
	// If o1 and o2 have a property with the same name but different values, the
	// value of o2 will be used.
	merge: function (o1, o2) {
		var o = {}, p;
		if (this.isNotNull(o1)) {
			for (p in o1) {
				if (o1.hasOwnProperty(p)) {
					o[p] = o1[p];
                }
			}
		}
		if (this.isNotNull(o2)) {
			for (p in o2) {
				if (o2.hasOwnProperty(p)) {
					o[p] = o2[p];
                }
			}
		}
		return o;
	},

    // Hashes a string using Johannes Baagoe's Mash algorithm v0.9 (2010)
    // From http://baagoe.com/en/RandomMusings/javascript/
    hash: function (s) {
        return JSUtil.masher()(s);
    },

    // Returns a stateful hash function using Baagoe's Mash algorithm
    masher: function () {
        var n = 0xefc8249d;

        return function(data) {
          data = data.toString();
          for (var i = 0; i < data.length; i++) {
            n += data.charCodeAt(i);
            var h = 0.02519603282416938 * n;
            n = h >>> 0;
            h -= n;
            h *= n;
            n = h >>> 0;
            h -= n;
            n += h * 0x100000000; // 2^32
          }
          return (n >>> 0) * 2.3283064365386963e-10; // 2^-32
        };
    },

    randomGenerator: null,

    // Arguments can be any data or objects, the less predictable, the better
    setRandomSeed: function () {
        // Johannes Baagoe's Alea algorithm v0.9 (2010)
        // From http://baagoe.com/en/RandomMusings/javascript/
        // This implementation passes l'Ecuyer's "Rabbit" test (http://www.iro.umontreal.ca/~simardr/testu01/tu01.html)
        // and Walker's ENT test (http://www.fourmilab.ch/random/).  I couldn't get enough output from Javascript to
        // satisfy Marsaglia's DIEHARD test.  It is very fast.
        JSUtil.randomGenerator = (function (args) {
            var s0 = 0;
            var s1 = 0;
            var s2 = 0;
            var c = 1;

            if (args.length == 0) {
              args = [+new Date(), window.location.href, document.cookie, navigator.userAgent];
            }
            var mash = JSUtil.masher();
            s0 = mash(' ');
            s1 = mash(' ');
            s2 = mash(' ');

            for (var i = 0; i < args.length; i++) {
              s0 -= mash(args[i]);
              if (s0 < 0) {
                s0 += 1;
              }
              s1 -= mash(args[i]);
              if (s1 < 0) {
                s1 += 1;
              }
              s2 -= mash(args[i]);
              if (s2 < 0) {
                s2 += 1;
              }
            }
            mash = null;

            var r = function() {
              var t = 2091639 * s0 + c * 2.3283064365386963e-10; // 2^-32
              s0 = s1;
              s1 = s2;
              return s2 = t - (c = t | 0);
            };
            r.uint32 = function() {
              return r() * 0x100000000; // 2^32
            };
            r.seed = args;
            return r;
        } (Array.prototype.slice.call(arguments)));
    },

    getRandomGenerator: function () {
        if (JSUtil.randomGenerator === null) {
            JSUtil.setRandomSeed();
        }
        return JSUtil.randomGenerator;
    },

    // Returns a random floating-point value between 0 and 1 (like Math.random but
    // consistent across platforms)
    random: function () {
        return JSUtil.getRandomGenerator()();
    },

	// Returns a random integer between 0 and max, inclusive
	// If max is omitted, uses 2^31 - 1 (Java Integer.MAX_VALUE)
	// Using Math.round() will give you a non-uniform distribution!
	randomInt: function (max) {
        var n;
        if (JSUtil.isNotNull(window.crypto) && JSUtil.isNotNull(window.crypto.getRandomValues) && JSUtil.isNotNull(window.Uint32Array)) {
            // Use cryptographically secure random number if available
            var buf = new Uint32Array(1);
            window.crypto.getRandomValues(buf);
            n = buf[0];
        }
        else {
            n = JSUtil.getRandomGenerator().uint32();
        }
        n = n % (JSUtil.defaultVal(max, 2147483646) + 1);
        return n;
	},

	// Returns a random bucket ID from a set of n equal buckets of size m%, or null
	// if the random value did not fall into any bucket.
	// If bucketPercent is null or omitted, assumes each bucket is 100%/numBuckets.
	// If bucketIds is omitted, returns the integer bucket index (0..n - 1).
	// If nullBucketId is present, it will be returned instead of null if the
	// selected value did not fall into any bucket.
	randomBucket: function (numBuckets, bucketPercent, bucketIds, nullBucketId) {
		var size = this.defaultVal(bucketPercent, 100.0 / numBuckets);
        // Don't use randomInt (unless you pick from a much larger range than 100)
		var i = Math.floor(JSUtil.random() * 100.0 / size);
		if (i >= numBuckets) {
			return this.defaultVal(nullBucketId);
        }

		if (this.isEmpty(bucketIds)) {
			return i;
        }

		return bucketIds[i];
	},

	// Returns an ISO 8601 representation of the specified Date object, for
	// example, "2011-01-19T15:43:22.123Z", or an empty string if the parameter
	// is null or undefined or an invalid date.  Based on code by Douglas Crockford
	// in json2.js.
	formatDateISO: function (d) {
	    var f = function (n) {
	        // Format integers to have at least two digits.
	        return n < 10 ? '0' + n : n;
	    };

        var formatMillis = function(n) {
            //Format to make sure there are at least 3 digits.
            return n < 100 ? (n < 10 ? '00' : '0') + n : n;
        };

		if (this.isNull(d) || !this.isFunction(d.getTime) || !isFinite(d.getTime())) {
			return '';
        }

        return d.getUTCFullYear()   + '-' +
            f(d.getUTCMonth() + 1) + '-' +
            f(d.getUTCDate())      + 'T' +
            f(d.getUTCHours())     + ':' +
            f(d.getUTCMinutes())   + ':' +
            f(d.getUTCSeconds())   + '.' +
            formatMillis(d.getUTCMilliseconds()) + 'Z';
	},

    once: function(func) {
        var funcCalled = false;
        var result;
        return function() {
            if (funcCalled) {
                return result;
            }
            funcCalled = true;
            result = func.apply(this, arguments);
            func = null;
            return result;
        }
    }
};

var WebUtil = {
	// Extracts the parts of a URL (protocol, host, port, path, page, query string, fragment)
	// as properties of an object.  This does not extract usernames, passwords,
	// URL path parameters, etc.
	parseUrl: function (s) {
		var u = { host: null, port: null, page: null, fragment: null };

		var nextToken = function (delim, eatDelim, eatIfAbsent) {
			if (JSUtil.isEmpty(s)) {
				return null;
            }
			var i = s.indexOf(delim);
			var token = s;
			if (i < 0) {
				if (JSUtil.defaultVal(eatIfAbsent, true)) {
					s = null;
                }
				else {
					return null;
                }
			}
			else {
                token = (i === 0) ? null : s.substring(0, i);
				if (JSUtil.defaultVal(eatDelim, true)) {
					i += delim.length;
                }
				s = s.substring(i);
			}
			return token;
		};

		u.scheme = nextToken('://', true, false);

		var h = nextToken('/', false);
		if (JSUtil.isNotEmpty(h)) {
			var hp = h.split(':');
			u.host = hp[0];
			u.port = (hp.length === 1) ? null : hp[1];
		}

		var pq = nextToken('#');
		u.fragment = s;
		s = pq;

		u.path = nextToken('?');
		if (u.path !== null) {
			var j = u.path.lastIndexOf('/');
			if (j >= 0) {
				u.page = u.path.substring(j + 1);
                if (u.page.length === 0) {
                    u.page = null;
                }
            }
		}

		u.query = s;
		return u;
	},

	// Extracts and decodes the value of a URL parameter from a query string
	// (uses the current URL if omitted).  Returns null if the parameter is not
	// present.  If the parameter has multiple values, only the first is returned.
	getParamValue: function (name, queryString) {
		var q = queryString;
		if (JSUtil.isNull(q)) {
			q = window.location.search;
			if (JSUtil.isEmpty(q)) {
				return null;
            }
			if (q.charAt(0) === '?') {
				q = '&' + q.substring(1);
            }
		}
		else if (JSUtil.isEmpty(q)) {
			return null;
        }
		else if (q.charAt(0) !== '&') {
			q = '&' + q;
        }

		var i = q.indexOf('&' + name + '=');
		if (i < 0) {
			return null;
        }

		i += name.length + 2;
		var j = q.indexOf('&', i);
		if (j < 0) {
			j = q.length;
        }
		if (i === j) {
			return null;
        }

		return decodeURIComponent(q.substring(i, j));
	},

	// Returns an object whose properties are the decoded values of the URL
	// parameters from a query string (uses the current URL if omitted).  If the
	// query string is empty, returns an object with no properties.  If any
	// parameter has multiple values, only the last one will be returned.
	getAllParams: function (queryString) {
		var obj = {};
		var q = queryString;
		if (JSUtil.isNull(q)) {
			q = window.location.search;
        }
		if (JSUtil.isEmpty(q)) {
			return obj;
        }
		if (q.charAt(0) === '?') {
			q = q.substring(1);
        }

		var params = q.split('&');
        var i, j, p;
		for (i = 0; i < params.length; i++) {
			p = params[i];
			j = p.indexOf('=');
			if (j > 0 && j < p.length - 1) {
				var v = p.substring(j + 1).replace(/\+/g, '%20');
				obj[p.substring(0, j)] = decodeURIComponent(v);
			}
		}

		return obj;
	},

	// Returns a query string whose parameters are the properties of the
	// specified object, with no leading '?'.  Property values will be
	// URL-encoded, but not names.
	makeQueryString: function (params) {
		if (JSUtil.isNull(params)) {
			return '';
        }

		var p, q = '';
		for (p in params) {
			if (params.hasOwnProperty(p)) {
				var v = params[p];
				if (JSUtil.isNotNull(v)) {
					q += '&' + p + '=' + encodeURIComponent(v);
                }
			}
		}
		return q.substring(1);
	},

	getBrowserLanguage: function () {
		if (JSUtil.isNotNull(navigator.language)) {
			return navigator.language;
        }
		if (JSUtil.isNotNull(navigator.userLanguage)) {
			return navigator.userLanguage;
        }
		if (JSUtil.isNotNull(navigator.browserLanguage)) {
			return navigator.browserLanguage;
        }
		if (JSUtil.isNotNull(navigator.systemLanguage)) {
			return navigator.systemLanguage;
        }
		return null;
	}
};

var CookieUtil = {
    checkCookiesEnabled: function () {
        var result = JSUtil.defaultVal(navigator.cookieEnabled);
        if (result !== null) {
            return result;
        }
        this.setCookie('anxTest', '1');
        return (this.getCookieValue('anxTest') !== null);
    },

	getCookieValue: function (name) {
		var c = ' ' + document.cookie;
		var i = c.indexOf(' ' + name + '=');
		if (i < 0) {
			return null;
        }

		i += name.length + 2;
		var delim = ';';
		if (c.charAt(i) === '"') {
			i++;
			delim = '"';
		}

		var j = c.indexOf(delim, i);
		if (j < 0) {
			j = c.length;
        }
		return JSUtil.defaultVal(c.substring(i, j));
	},

	getEncodedCookieValue: function (name) {
		var c = this.getCookieValue(name);
		if (c !== null) {
			return decodeURIComponent(c);
        }

		return null;
	},

	// domain, expires, and encode are optional (default is session, no encoding)
	// Note that if the value contains a semicolon, you MUST specify encode = true,
	// or it will be truncated.
	setCookie: function (name, value, domain, expires, encode) {
		var c = name + "=";
        c += encode ? encodeURIComponent(value) : value;
		if (JSUtil.isNotEmpty(domain)) {
			c += "; domain=" + domain;
        }
		c += "; path=/";
		if (JSUtil.isNotNull(expires)) {
			c += "; expires=" + expires.toGMTString();
        }
		anxDebug('Set cookie ' + c);
		document.cookie = c;
	},

	// domain and encode are optional (default no encoding)
	setSessionCookie: function (name, value, domain, encode) {
		this.setCookie(name, value, domain, null, encode);
	},

	// domain, encode, and maxAgeSecs are optional (default 10 years, no encoding)
	setPermanentCookie: function (name, value, domain, encode, maxAgeSecs) {
		var expires = new Date().getTime();
		expires += (JSUtil.isNotNull(maxAgeSecs)) ? maxAgeSecs * 1000 : 315000000000;
		this.setCookie(name, value, domain, new Date(expires), encode);
	},

	deleteCookie: function (name) {
		document.cookie = name + "=; path=/; expires=Wed, 01 Apr 1970 00:00:00 GMT";
	},

	getChipValue: function (cookieName, chipName) {
		return WebUtil.getParamValue(chipName, this.getCookieValue(cookieName));
	},

	// Returns an object whose properties are the chips of the specified cookie.
	getAllChips: function (cookieName) {
		var c = this.getCookieValue(cookieName);
		if (c === null) {
			return {};
        }

		return WebUtil.getAllParams(c);
	},

	// Returns a new cookie value with the chip set or added (URL-encodes the
	// chip value).  Does not set the cookie -- use set[Session|Permanent|]Cookie().
	setChipValue: function (cookieValue, chipName, chipValue) {
		var c = '';
		if (JSUtil.isNotEmpty(cookieValue)) {
			c = '&' + cookieValue;
			var i = c.indexOf('&' + chipName + '=');
			if (i >= 0) {
				var j = c.indexOf('&', i + 1);
				if (j < 0) {
					j = c.length;
                }
				c = c.substring(0, i) + c.substring(j, c.length);
			}
		}

		return chipName + '=' + encodeURIComponent(chipValue) + c;
	}
};

var _Anemone = {
	config: null,
    cookiesEnabled: CookieUtil.checkCookiesEnabled(),
    cookieExpirationSecs: 7776000,   // 90 days
	shouldLogPageView: false,
	shouldUpdateSession: false,
    isNewSession: false,
    lastEvent: 0,
	appId: null,
	appVersion: null,
	appDate: null,
	domain: null,
	pixelUrl: null,
	uniqueUserId: null,
	referrerDomain: null,
	referrerPage: null,
	referrerKeyword: null,
	referrerMedium: null,
	referrerCobrand: null,
	referrerCampaign: null,
	referrerSubId: null,
	cookie: null,
	sessionCookie: null,
	now: null,
    sequence: 1,
    jsStartTime: new Date().getTime(),
    partnerIdFactory: JSUtil.optionalVal('PartnerIdFactory') === null ? null : new PartnerIdFactory(),

	cookieChips: ['u', 'fv', 'lv', 'nv', 't', 'v', 'p', 'si', 'sn', 'od', 'op', 'ok', 'om', 'ob', 'oc', 'os', 'w', 'h', 'cd', 'f', 'g'],
	sessionCookieChips: ['s', 'sv', 'sd', 'sp', 'sk', 'sm', 'sb', 'sc', 'ss'],

	searchEngines: [
		['.google.', 'q'],
		['.yahoo.', 'p'],
		['.baidu.', 'wd'],
		['.bing.', 'q'],
		['.ask.', 'q'],
		['.aol.', 'q'],
		['.mywebsearch.', 'searchfor'],
		['.excite.', null, /\/excite\/ws\/results\/Web\/([^\/?]+)/i],
        ['.altavista.', 'p'],   // now Yahoo
		['.lycos.', 'query'],
		['.teoma.', 'q'],
		['.search.', 'q'],
		['.search-results.', 'q'],
		['.earthlink.', 'q'],
		['.cnn.', 'query'],
		['.about.', 'q'],
		['.ehow.', 's'],
		['.dogpile.', null, /\/dogpile\/ws\/results\/Web\/([^\/?]+)/i],
		['.blekko.', null, /\/ws\/([^\/?]+)/i],
		['.mamma.', 'q'],
		['.gigablast.', 'q'],
		['.snap.', 'query'],
		['.voila.', 'rdata'],
		['.virgilio.', 'qs']
	],

    determineSystemTime: function() {
        var currentTime = new Date().getTime();
        var timeElapsed = currentTime - this.jsStartTime;
        var startTime = JSUtil.defaultIntVal(this.config.systemTime, this.jsStartTime);
        return (startTime + timeElapsed);
    },

	logPageView: function () {
		this.loadConfig();
        this.now = this.determineSystemTime();

		this.loadInfo();
		this.setCookies();

		var params = this.buildPixelUrlParams();
		var eventType;
		if (this.shouldLogPageView) {
			eventType = 'PageView';
        }
		else if (JSUtil.defaultVal(this.config.backFillRequired, false)) {
			eventType = 'backFill';
			params.anxi = JSUtil.defaultVal(this.config.eventId, '');
		}
		else {
			return;
        }

		this.logEvent(eventType, params);

		// Clear referrer for subsequent pixel calls
		this.referrerDomain = null;
		this.referrerPage = null;
		this.referrerKeyword = null;
		this.referrerMedium = null;
		this.referrerCobrand = null;
		this.referrerCampaign = null;
		this.referrerSubId = null;
	},

    updateCookies: function() {
        var oldCookie = this.cookie;
        var oldSessionCookie = this.sessionCookie;
        this.parseCookies();
        this.cookie = JSUtil.merge(oldCookie, this.cookie);
        this.sessionCookie = JSUtil.merge(oldSessionCookie, this.sessionCookie);
    },

	logEvent: function (eventType, params, callback) {
        this.updateCookies();
		this.now = this.determineSystemTime();
		this.updateSession(true);
		this.setCookies();
		this.callPixel(eventType, this.buildPixelUrlParams(params), callback);
        this.isNewSession = false;
	},

	getUserSegments: function (segs, owner) {
		if (JSUtil.isNull(segs)) {
			return null;
        }

		return JSUtil.defaultVal(segs[owner]);
	},

	getUserSegment: function (segs, owner, testId) {
		var ownerSegs = this.getUserSegments(segs, owner);
		if (ownerSegs === null) {
			return null;
        }

		return JSUtil.defaultVal(ownerSegs[testId]);
	},

	setUserSegment: function (segs, owner, testId, bucketId) {
		if (JSUtil.isNull(segs)) {
			segs = {};
        }

		var ownerSegs = JSUtil.defaultVal(segs[owner]);
		if (ownerSegs === null) {
			ownerSegs = {};
			segs[owner] = ownerSegs;
		}

		ownerSegs[testId] = bucketId;
		return segs;
	},

	// Internal methods

	loadConfig: function () {
		if (this.config !== null) {
			return;
        }

		var config1 = JSUtil.optionalVal('_AnemoneParams');
		var config2 = JSUtil.optionalVal('_AnemoneParams2');
		this.config = JSUtil.merge(config2, config1);

		// For backwards compatibility, look for individual config items
		var allParams = ['uniqueUser', 'appId', 'appVersion', 'appDate', 'logPageView',
			'updateSession', 'domain', 'url', 'getAppParams', 'getAppCookieChips',
			'getUserSegments', 'backFillRequired', 'maxSession', 'cobrandParam',
			'campaignParam', 'subIdParam', 'mediumParam'];
        var i, len = allParams.length;
		for (i = 0; i < len; i++) {
			var s = allParams[i];
			var s1 = '_anx' + s.substring(0, 1).toUpperCase() + s.substring(1);
			var o = JSUtil.optionalVal(s1);
			if (o !== null) {
				this.config[s] = o;
            }
		}

        if (this.config.suppressCookies) {
            this.cookiesEnabled = false;
        }
        if (this.config.cookieExpirationMinutes) {
            this.cookieExpirationSecs = this.config.cookieExpirationMinutes * 60;
        }
	},

	loadInfo: function () {
		this.shouldLogPageView = JSUtil.defaultVal(this.config.logPageView, false);
		this.shouldUpdateSession = JSUtil.defaultVal(this.config.updateSession, false);

		this.parseCookies();
        this.getReferrer();   // should happen before updateSession
		this.updateSession(false);

		var c = this.cookie;
		var toolbar = JSUtil.optionalVal('TOOLBAR');
		if (toolbar !== null && toolbar.bInstalled) {
			// Only set these if there is a toolbar object -- if there's not one, it doesn't mean
			// the toolbar is not installed: could just be the toolbar detect JS is not present
			c.t = JSUtil.defaultVal(toolbar.sUID, '-');
			c.v = JSUtil.defaultVal(toolbar.sVersion, '-');
			c.p = JSUtil.defaultVal(toolbar.sPartnerID, '-');
			// TOOLBAR object doesn't have sub-ID, so get it from settings control
			if (JSUtil.defaultVal(toolbar.oSettingsCtl) !== null) {
				c.si = JSUtil.defaultVal(toolbar.oSettingsCtl.S, '-');
            }
		}

		if (JSUtil.defaultVal(c.f) === null) {
			c.f = this.getFlashVersion();
        }

        var scr = JSUtil.optionalVal('screen');
		if (scr !== null) {
		    c.w = scr.width;
		    c.h = scr.height;
		    c.cd = scr.colorDepth;
		}
	},

	parseCookies: function () {
		this.cookie = CookieUtil.getAllChips('anx');
		this.sessionCookie = CookieUtil.getAllChips('anxs');

		this.uniqueUserId = JSUtil.defaultVal(this.config.uniqueUser);
		if (JSUtil.isBlank(this.uniqueUserId)) {
			this.uniqueUserId = JSUtil.defaultVal(this.cookie.u);
			if (JSUtil.isBlank(this.uniqueUserId) && this.cookiesEnabled) {
                // If cookies are disabled, don't generate a user ID -- will cause overcounting
				this.uniqueUserId = this.generateUserId();
            }
		}
        this.cookie.u = this.uniqueUserId;

		var segs = this.parseSegments(JSUtil.defaultVal(this.cookie.g, '-'));

		var getUserSegments = JSUtil.defaultVal(this.config.getUserSegments);
		if (getUserSegments !== null) {
			try {
				segs = getUserSegments(segs);
			}
			catch (e) {
				anxDebug('Exception in getUserSegments: ' + e);
			}
		}

		this.cookie.g = this.stringifySegments(segs);
	},

	// Would be better to use native JSON.parse or json2.js
	parseSegments: function (s) {
		var segs = {};
		if (JSUtil.isEmpty(s) || s === '-') {
			return segs;
        }

		var state = 'OWNER';
		var tokens = s.split('"');
		var owner = null;
		var osegs = {};
		var testId = null;
        var i, t;
		for (i = 0; i < tokens.length; i++) {
			t = tokens[i];
			if (t.length === 0) {
				continue;
            }
			var c = t.charAt(0);
			if (c === '}') {
				state = 'OWNER';
            }
			else if (c === ':' || c === ',' || c === '{') {
				continue;
            }
			else if (state === 'OWNER') {
				owner = t;
				osegs = {};
				segs[owner] = osegs;
				state = 'TEST';
			}
			else if (state === 'TEST') {
				testId = t;
				state = 'BUCKET';
			}
			else if (state === 'BUCKET') {
				osegs[testId] = t;
				state = 'TEST';
			}
		}
		return segs;
	},

	stringifySegments: function (segs) {
		if (JSUtil.isNull(segs)) {
			return '-';
        }

		var owner, owners = [];
		for (owner in segs) {
            if (segs.hasOwnProperty(owner)) {
                var osegs = [];
                var testId, tests = segs[owner];
                for (testId in tests) {
                    if (tests.hasOwnProperty(testId)) {
                        osegs.push('"' + testId + '":"' + tests[testId] + '"');
                    }
                }
                owners.push('"' + owner + '":{' + osegs.join(',') + '}');
            }
		}
		if (JSUtil.isEmpty(owners)) {
			return '-';
        }
		return '{' + owners.join(',') + '}';
	},

	buildPixelUrlParams: function (params) {
		var url = window.location.href;
		var i = url.indexOf('?');
		if (i > 0) {
			url = url.substring(0, i);
        }

		var p1 = {
			anxuu: this.uniqueUserId,
			anxa: this.getAppId(),
			anxv: this.getAppVersion(),
			anxd: this.getAppDate(),
            anxsn: this.getServerName(),
			anxu: url,
			anxl: JSUtil.defaultVal(this.config.browserLanguage, WebUtil.getBrowserLanguage()),
            anxlv: this.lastEvent,
			anxrd: this.referrerDomain,
			anxrp: this.referrerPage,
			anxrk: this.referrerKeyword,
			anxrm: this.referrerMedium,
			anxrb: this.referrerCobrand,
			anxrc: this.referrerCampaign,
			anxrs: this.referrerSubId,
            anxsq: this.sequence++
		};
		var p2 = null;
		var getAppParams = JSUtil.defaultVal(this.config.getAppParams);
		if (getAppParams !== null) {
			try {
				p2 = getAppParams();
			}
			catch (e) {
				anxDebug('Exception in getAppParams: ' + e);
			}
		}

		if (JSUtil.isNotNull(p2)) {
			p1 = JSUtil.merge(p1, p2);
        }

		if (JSUtil.isNotNull(params)) {
			p1 = JSUtil.merge(p1, params);
        }

		return p1;
	},

	emptyCookie: function (chipNames) {
		var i, c = {}, len = chipNames.length;
		for (i = 0; i < len; i++) {
			c[chipNames[i]] = '-';
		}
		return c;
	},

	setCookies: function () {
        if (!this.cookiesEnabled) {
            return;
        }

		this.cookie = JSUtil.merge(this.emptyCookie(this.cookieChips), this.cookie);
        // Make sure the unique user ID in the cookie matches the value we pass in the anxuu param
        this.cookie.u = this.uniqueUserId;

		var getAppCookieChips = JSUtil.defaultVal(this.config.getAppCookieChips);
		if (getAppCookieChips !== null) {
			var appChips = null;
			try {
				appChips = getAppCookieChips();
			}
			catch (e) {
				anxDebug('Exception in getAppCookieChips: ' + e);
			}
			if (JSUtil.isNotNull(appChips)) {
				this.cookie = JSUtil.merge(this.cookie, appChips);
            }
		}

		var c = '"' + WebUtil.makeQueryString(this.cookie) + '"';   // Tomcat 6 doesn't parse it correctly unless quoted
		CookieUtil.setPermanentCookie('anx', c, this.getDomain(), false, this.cookieExpirationSecs);

		this.sessionCookie = JSUtil.merge(this.emptyCookie(this.sessionCookieChips), this.sessionCookie);
		c = '"' + WebUtil.makeQueryString(this.sessionCookie) + '"';
		CookieUtil.setSessionCookie('anxs', c, this.getDomain());
	},

	callPixel: function (eventType, params, callback) {
        var callbackIsNull = JSUtil.isNull(callback);
        if (callbackIsNull) {
            callback = anxDummy;
        } else {
            //Make sure the callback is run just once.  This is necessary because we will be using a timeout to make sure sure the callback is called
            callback = JSUtil.once(callback);
        }

		params.anxe = eventType;
		params.anxr = JSUtil.randomInt();
		var url = this.getPixelUrl() + '?' + WebUtil.makeQueryString(params);
		var p = new Image(1, 1);
		p.src = url;
        //Call the callback when the pixel loads or errors out.
		p.onload = function () { callback(true); };
		p.onerror = function () { callback(false); };
        if (!callbackIsNull) {
            //In the very rare case that neither the onload or onerror handler is called, fire the callback in 2 seconds
            setTimeout(function() {
                callback(false);
            }, 2000);
        }
		anxDebug('Calling pixel ' + url);
		return url;
	},

	updateSession: function (isClientEvent) {
        if (!this.cookiesEnabled) {
            return;
        }

        this.lastEvent = Number(JSUtil.defaultIntVal(this.cookie.lv, 0));
        if (!this.isNewSession) {
            if (JSUtil.defaultVal(this.sessionCookie.s, '-') === '-' || this.now - this.lastEvent > JSUtil.defaultVal(this.config.maxSession, 30) * 60000) {
                anxDebug("No session or session expired: start new session");
                this.setNewSession();
            }
        }

        // This will be called during initialization to set the session ID if necessary, and also for each
        // client event.  However, during initialization we don't want to update fv, lv, nv.
		if (isClientEvent) {
            this.cookie.fv = JSUtil.defaultIntVal(this.cookie.fv, this.now);
            this.cookie.lv = this.now;   // actual last event time will be passed as anxlv request parameter
            this.cookie.nv = Number(JSUtil.defaultIntVal(this.cookie.nv, 0)) + 1;
        }
	},

    setNewSession: function () {
        this.sessionCookie.s = JSUtil.randomInt();
        this.sessionCookie.sv = this.now;

        this.sessionCookie.sd = this.referrerDomain;
        this.sessionCookie.sp = this.referrerPage;
        this.sessionCookie.sk = this.referrerKeyword;
        this.sessionCookie.sm = this.referrerMedium;
        this.sessionCookie.sb = this.referrerCobrand;
        this.sessionCookie.sc = this.referrerCampaign;
        this.sessionCookie.ss = this.referrerSubId;

        this.isNewSession = true;
    },

	getReferrer: function () {
        var d = 'none';
        var p = '-';
        var k = '-';
        var defaultMedium = '-';
        var r = document.referrer;
        if (JSUtil.isNotEmpty(r)) {
            var u = WebUtil.parseUrl(r);
            d = u.host;   // server uses top private domain, but we don't have a good way to extract it on client
            p = u.page;
            k = this.extractKeyword(u);
            if (!this.isSameDomain(d)) {
                defaultMedium = (k === '-') ? 'referral' : 'organic';
            }
        }
        var m = JSUtil.defaultVal(WebUtil.getParamValue(JSUtil.defaultVal(this.config.mediumParam, 'anxrm')), defaultMedium);
        var cb = JSUtil.defaultVal(WebUtil.getParamValue(JSUtil.defaultVal(this.config.cobrandParam, 'anxrb')), '-');
        var c = JSUtil.defaultVal(WebUtil.getParamValue(JSUtil.defaultVal(this.config.campaignParam, 'anxrc')), '-');
        var si = JSUtil.defaultVal(WebUtil.getParamValue(JSUtil.defaultVal(this.config.subIdParam, 'anxrs')), '-');
        if (this.partnerIdFactory !== null) {
            var partnerParam = JSUtil.defaultVal(this.config.partnerParam);
            if (partnerParam !== null) {
                var partner = JSUtil.defaultVal(WebUtil.getParamValue(partnerParam));
                if (partner !== null) {
                    var partnerObj = this.partnerIdFactory.parse(partner);
                    if (partnerObj.hasCobrand()) {
                        cb = partnerObj.getCobrand();
                    }
                    if (partnerObj.hasCampaign()) {
                        c = partnerObj.getCampaign();
                    }
                }
            }
        }

        this.referrerDomain = d;
        this.referrerPage = p;
        this.referrerKeyword = k;
        this.referrerMedium = m;
        this.referrerCobrand = cb;
        this.referrerCampaign = c;
        this.referrerSubId = si;

        if (!this.cookiesEnabled) {
            return;
        }

        if (!this.isNewSession && this.config.newSessionOnDomainChange && d !== 'none' && !this.isSameDomain(d)) {
            // Case 1: No session ID.  We will already have set session ID in updateSession.
            // Case 2: Expired session ID.  We will already have set new session ID on server or in updateSession.
            // Case 3: Different referrer, using server logging.  Server will have set new session ID; we should retain it.
            // Case 4: Different referrer, not using server logging.  We need to set new session ID.
            if (this.shouldUpdateSession) {
                anxDebug("Different domain (" + d + "): start new session");
                this.setNewSession();
            }
        }

		if (JSUtil.isNull(this.cookie.od)) {
			this.cookie.od = d;
			this.cookie.op = p;
			this.cookie.ok = k;
			this.cookie.om = m;
			this.cookie.ob = cb;
			this.cookie.oc = c;
			this.cookie.os = si;
		}
	},

	extractKeyword: function (u) {
		if (u.host === null) {
			return '-';
        }

		var q = WebUtil.getAllParams(u.query);
		if (u.query === null && u.fragment !== null) {   // Google
			q = WebUtil.getAllParams(u.fragment);
        }

        var i, d, p;
		for (i = 0; i < this.searchEngines.length; i++) {
			d = this.searchEngines[i][0];
			if (u.host.indexOf(d) >= 0 || u.host.indexOf(d.substring(1)) === 0) {   // sub.domain.com or domain.com
				p = this.searchEngines[i][1];
				if (p === null) {
					var match = this.searchEngines[i][2].exec(u.path);
					if (match !== null) {
						return match[1];
                    }
				}
				else {
					var k = JSUtil.defaultVal(q[p], null);
					if (k !== null) {
						return k;
                    }
				}
			}
		}

		return '-';
	},

	isSameDomain: function (d) {
		var d1 = this.getDomain();
		if (d.charAt(0) !== '.') {
			d = '.' + d;
        }
		if (d.length < d1.length) {
			return false;
        }
		return d.substring(d.length - d1.length) === d1;
	},

	getPixelUrl: function () {
		if (this.pixelUrl === null) {
			this.pixelUrl = JSUtil.defaultVal(this.config.url);
			if (this.pixelUrl === null) {
				var s = document.location.href;
				var i = s.indexOf('?');
				if (i > 0) {
					s = s.substring(0, i);
                }
                i = s.indexOf('#');
                if (i > 0) {
                    s = s.substring(0, i);
                }
				i = s.lastIndexOf('/');
				if (i > 0) {
					s = s.substring(0, i);
                }
				this.pixelUrl = s + "/anemone.jhtml";
				anxDebug('Using pixel URL ' + this.pixelUrl);
			}
		}

		return this.pixelUrl;
	},

	getAppId: function () {
		if (this.appId === null) {
			this.appId = JSUtil.defaultVal(this.config.appId);
			if (this.appId === null) {
				this.appId = document.location.hostname;
				anxDebug('Using app ID ' + this.appId);
			}
		}

		return this.appId;
	},

	getAppVersion: function () {
		if (this.appVersion === null) {
			this.appVersion = JSUtil.defaultVal(this.config.appVersion);
			if (this.appVersion === null) {
				this.appVersion = '-';
				anxDebug('Using app version ' + this.appVersion);
			}
		}

		return this.appVersion;
	},

	getAppDate: function () {
		if (this.appDate === null) {
			this.appDate = JSUtil.defaultVal(this.config.appDate);
			if (this.appDate === null) {
				var d = JSUtil.optionalVal('document.lastModified');
				anxDebug('document.lastModified = ' + d);
				if (d !== null) {
					this.appDate = JSUtil.formatDateISO(new Date(d));
				}
				if (this.appDate === null) {
					this.appDate = '-';
                }
				anxDebug('Using app date ' + this.appDate);
			}
		}

		return this.appDate;
	},

	getDomain: function () {
		if (this.domain === null) {
			this.domain = JSUtil.defaultVal(this.config.domain);
			if (this.domain === null) {
				var h = document.location.hostname;
				var i = h.lastIndexOf('.');
				if (i > 0) {
					i = h.lastIndexOf('.', i - 1);
					if (i > 0) {   // "www.foo.com"
						h = h.substring(i);
                    }
					else {   // "foo.com"
						h = '.' + h;
                    }
				}
				this.domain = h;
				anxDebug('Using domain ' + h);
			}
		}

		return this.domain;
	},

    getServerName: function () {
        return JSUtil.defaultVal(this.config.serverName, '');
    },

	generateUserId: function () {
		// Based on code by Kevin Hakanson
		// http://www.ietf.org/rfc/rfc4122.txt
	    var hexDigits = "0123456789ABCDEF";
        var i, s = [];
	    for (i = 0; i < 36; i++) {
            if (i === 8 || i === 13 || i === 18 || i === 23) {
                s[i] = '-';
            }
            else {
                s[i] = hexDigits.substr(JSUtil.randomInt(15), 1);
            }
	    }
	    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
	    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01

	    return s.join("");
	},

	// From __utm.js
	getFlashVersion: function () {
        var f = "-", n = navigator, ii;
        if (n.plugins && n.plugins.length) {
            for (ii = 0; ii < n.plugins.length; ii++) {
                if (n.plugins[ii].name.indexOf('Shockwave Flash') !== -1) {
                    f = n.plugins[ii].description.split('Shockwave Flash ')[1];
                    break;
                }
            }
        } else if (window.ActiveXObject) {
            for (ii = 10; ii >= 2; ii--) {
                try {
                    var fl = eval("new ActiveXObject('ShockwaveFlash.ShockwaveFlash." + ii + "');");
                    if (fl) { f = ii + '.0'; break; }
                }
                catch(e) {}
            }
        }
        return f;
	}
};

try {
	_Anemone.logPageView();
}
catch (e) {
	anxDebug('Exception in logPageView: ' + e);
}

function anxDummy() {
	// Do nothing
}
