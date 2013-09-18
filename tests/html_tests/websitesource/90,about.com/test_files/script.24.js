var userInTextArea = false;
var currentSection = "editors-picks";
var currentPage = 0;
var totalPages = 0;
var ignoreHashChange = false;
var displayType = "landscape";
var displaySize = 960;
var loadInterval;
var allLoaded;
var firstSlide = true;
var paginationUclicks = {
	"editors-picks": ["18/1qF", "18/1qG", "18/1qJ"],
	"explore-topics": ["18/1qH", "18/1qI", "18/1qK"],
	"browse-categories": ["", "", ""]
};
var sections = [{
	url: "editors-picks-2.htm",
	parent: "#editors-picks > .viewport",
	loaded: false
}, {
	url: "editors-picks-3.htm",
	parent: "#editors-picks > .viewport",
	loaded: false
}, {
	url: "editors-picks-1.htm",
	parent: "#editors-picks > .viewport",
	loaded: false
}];
$(document).ready(function () {
	$(window).resize(function () {
		determineDisplayType()
	});
	$('nav[role="navigation"] a').click(function (e) {
		changeSection($(this).attr("href").replace("#", ""), 0);
		e.preventDefault()
	});
	$(".section-nav li a").live("click", function (e) {
		setPage($(this).index(".section-nav li a"));
		e.preventDefault()
	});
	$(".section-nav .prev a").click(function (e) {
		var f = (currentPage == 0) ? totalPages : currentPage - 1;
		setPage(f);
		e.preventDefault()
	});
	$(".section-nav .next a").click(function (e) {
		var f = (currentPage == totalPages) ? 0 : currentPage + 1;
		setPage(f);
		e.preventDefault()
	});
	$(window).keypress(function (e) {
		if (userInTextArea) {
			return
		}
		if (e.which == 106) {
			$(".section-nav .prev a").trigger("click")
		} else {
			if (e.which == 107) {
				$(".section-nav .next a").trigger("click")
			}
		}
	});
	if (Modernizr.touch) {
		$(".viewport").each(function (e) {
			$(this).swipe({
				swipeRight: function () {
					$(".section-nav .prev a").trigger("click")
				},
				swipeLeft: function () {
					$(".section-nav .next a").trigger("click")
				}
			})
		})
	}
	$("a[data-uclick]").live("click", function () {
		dropPixel($(this).attr("data-uclick"))
	});
	$('.az').each(function () {
		var letter, _this;
		$(this).children('li').each(function () {
			_this = $(this);
			letter = $(this).children('a').first();
			letterInList = $(letter.attr('href'));
			if (letterInList.children().length == 0) {
				letter.addClass('inactive');
			}
		});
	});
	$(".az").click(function (e) {
		e.preventDefault();
		var sectionChange = false;
		if (currentSection != "editors-picks") {
			sectionChange = true;
		}
		if (displaySize != 480 && !$(e.target).hasClass('inactive')) {
			changeSection("explore-topics", $(e.target).attr("href").replace("#", ""), sectionChange);
		}
	});
	if (!Modernizr.input.placeholder) {
		var a = document.body.getElementsByTagName("input");
		for (var b = 0; b < a.length; b++) {
			if (a[b].type == "text") {
				if (!a[b].getAttribute("placeholder")) {
					continue
				}
				if (a[b].value == "") {
					a[b].value = a[b].getAttribute("placeholder")
				}
				a[b].onfocus = function () {
					if (this.value == this.getAttribute("placeholder")) {
						this.value = ""
					}
				};
				a[b].onblur = function () {
					if (this.value == "") {
						this.value = this.getAttribute("placeholder")
					}
				}
			}
		}
	}
	$("form[role=search] input").each(function () {
		if ($(this).attr("value") != $(this).attr("placeholder") && $(this).prop("value") != "") {
			$(this).addClass("focus")
		}
	});
	$("form[role=search] input").focus(function () {
		$(this).addClass("focus")
	});
	$("form[role=search] input").blur(function () {
		if ($(this).attr("value") == $(this).attr("placeholder") || $(this).prop("value") == "") {
			$(this).removeClass("focus")
		}
	});
	$("textarea, input").live("focus", function () {
		userInTextArea = true
	});
	$("textarea, input").live("blur", function () {
		userInTextArea = false
	});
	if (location.hash) {
		var d = getHash();
		if (d[0] != "") {
			currentSection = d[0]
		}
		if (d[1] != "") {
			currentPage = d[1]
		}
	}
	if (currentSection == 'editors-picks') {
		currentPage = Math.floor(Math.random() * 3);
	}
	determineDisplayType();
	changeSection(currentSection, currentPage)
});
$(window).load(function () {
	getTweets()
});

function loadSection(a) {
	if (sections[a] && !sections[a].loaded) {
		sections[a].loaded = true;
		$.ajax({
			url: sections[a].url,
			async: true,
			success: function (d) {
				var b = $(this)[0].url;
				$.each(sections, function (e) {
					if (sections[e].url == b) {
						$(sections[e].parent).append(innerShiv(d))
					}
					return true
				})
			},
			error: function () {},
			complete: function () {
				postLoad(a)
			}
		})
	} else {
		postLoad(a, c)
	}
	return sections[a]
}
function postLoad(a) {
	allLoaded = true;
	$(sections).each(function (b, d) {
		if (d.loaded === false) {
			allLoaded = false;
			return
		}
	});
	if (allLoaded) {
		$("#main").addClass("loaded");
		setTimeout(function() { $("#editors-picks").css('background', 'none'); }, 1000);
		window.clearInterval(loadInterval)
	}
}
function getScrollPosition() {
	return [window.pageXOffset, window.pageYOffset]
}
function determineDisplayType() {
	var a = document.documentElement.clientWidth;
	if (a >= 1218 && displaySize != 1150) {
		$(window).unbind("scroll");
		displayType = "landscape";
		displaySize = 1150;
		//createNavigation(currentPage)
	} else {
		if (a > 998 && a < 1218 && displaySize != 960) {
			$(window).unbind("scroll");
			displayType = "landscape";
			displaySize = 960;
			createNavigation(currentPage)
		}
	}
	if (a <= 998 && a > 480 && displaySize != 860) {
		if (!allLoaded && !loadInterval) {
			loadInterval = window.setInterval(function () {
				if (currentSection != "editors-picks") {
					return
				}
				var b = getScrollPosition();
				if (b[1] + window.innerHeight >= $("body").outerHeight() - 150) {
					dropPixel(paginationUclicks[currentSection][1]);
					currentPage = (currentPage + 1) % sections.length;
					loadSection(currentPage)
				}
			}, 500)
		}
		$(window).unbind("scroll.explore-topics");
		displayType = "portrait";
		displaySize = 860;
		createNavigation(currentPage)
	} else {
		if (a <= 480 && displaySize != 480) {
			if (!allLoaded && !loadInterval) {
				loadInterval = window.setInterval(function () {
					if (currentSection != "editors-picks") {
						return
					}
					var b = getScrollPosition();
					if (b[1] + window.innerHeight >= $("body").outerHeight() - 150) {
						dropPixel(paginationUclicks[currentSection][1]);
						currentPage = (currentPage + 1) % sections.length;
						loadSection(currentPage)
					}
				}, 500)
			}
			$(window).bind("scroll.explore-topics", function (b) {
				if (currentSection != "explore-topics") {
					return
				}
				if (loadInterval) {
					clearTimeout(loadInterval)
				}
				loadInterval = setTimeout(function () {
					if (window.pageYOffset > $("#explore-topics").offset().top) {
						if (Modernizr.cssanimations) {
							$("#explore-topics nav").css("top", window.pageYOffset - $("#explore-topics").offset().top + "px")
						} else {
							$("#explore-topics nav").animate({
								top: window.pageYOffset - $("#explore-topics").offset().top + "px"
							}, 250, "linear")
						}
					} else {
						$("#explore-topics nav").css("top", "")
					}
				}, 500)
			});
			displayType = "portrait";
			displaySize = 480;
			createNavigation(currentPage)
		}
	}
}
function getHash() {
	return location.hash.substring(3).split("/")
}
function setHash(b, d) {
	var a = getHash();
	ignoreHashChange = true;
	if (Modernizr.history) {
		window.history.replaceState(null, "", "#!/" + ((b) ? b : a[0]) + "/" + ((d) ? d : ""))
	}
}
function convertNumToSection(a) {
	var a = a || convertSectionToNum();
	return $(".container > section").eq(a).attr("id").replace("#", "")
}
function convertSectionToNum(a) {
	var a = a || currentSection;
	return ($("#" + a).index()) - 1
}
function changeSection(d, b, azChange) {
	var azChange = azChange || false;
	var d = (!isNaN(d)) ? convertNumToSection(d) : d;
	var sectionChange;
	
	if (!azChange) {
		sectionChange = true;
	}
	else {
		sectionChange = false;
	}

	$('.container > section, nav[role="navigation"] li').removeClass("on");
	var a = $('.container > section[id*="' + d + '"]');
	a.addClass("on");
	$('nav[role="navigation"] li a[href*="' + d + '"]').parent().addClass("on");
	currentSection = d;
	setHash(d);
	createNavigation(b, sectionChange);
	return a
}
function createNavigation(a, sectionChange) {
	var sectionChange = sectionChange || false;
	totalPages = Math.ceil($(".on .viewport").width() / $("#main section.on").width() - 1);
	$(".section-nav ol").empty();
	if (Number(totalPages) <= 0) {
		$(".section-nav").addClass("hidden")
	} else {
		$(".section-nav").removeClass("hidden");
		$(".section-nav .prev a").attr("data-uclick", paginationUclicks[currentSection][0]);
		$(".section-nav .next a").attr("data-uclick", paginationUclicks[currentSection][1]);
		$(".section-nav ol").html(function () {
			var d = "";
			for (var b = 0; b <= totalPages; b++) {
				d += '<li><a href="#" data-uclick="' + paginationUclicks[currentSection][2] + '">Section ' + (b + 1) + "</a></li>"
			}
			return d
		})
	}
	setPage(a, sectionChange)
}
function convertLettertoPageNum(a) {
	return Math.floor($("#" + a).position().left / $("#explore-topics").width())
}
function setPage(a, sectionChange) {
	var sectionChange = sectionChange || false;
	currentPage = (isNaN(a)) ? convertLettertoPageNum(a) : Number(a);

	if (currentSection == "editors-picks" && !sections[a].loaded) {
		loadSection(a);
	}
	if ((displayType == "landscape" || currentSection == "explore-topics")) {
		if ((displaySize == 960 || displaySize == 1150) && currentPage == 0 && firstSlide) {
			$("#main .on > .viewport section").hide();
			$("#main .on > .viewport").hide().fadeIn(400);
			$("#main .on > .viewport section").fadeIn(400);
		}
		else {
			if (sectionChange) {
				$("#main .on > .viewport section").hide();
				$("#main .on > .viewport").hide();
				$("#main .on > .viewport").css("left", "-" + (currentPage * 100) + "%");
			}
			else {
				$("#main .on > .viewport section").fadeOut(400);
				$("#main .on > .viewport").fadeOut(400, function(){
					$("#main .on > .viewport").css("left", "-" + (currentPage * 100) + "%");
				});
			}
			if (sectionChange) {
				$("#main .on > .viewport section").show();
				$("#main .on > .viewport").show();
			}
			else {
				$("#main .on > .viewport").fadeIn(400);
				$("#main .on > .viewport section").fadeIn(400);
			}
		}
	}
	$(".section-nav li").each(function (b) {
		$(this).removeClass("on");
		if (b == currentPage) {
			$(this).addClass("on")
		}
	})
	firstSlide = false;
}
function azPagination(a) {
	if (currentSection != 1) {
		changeSection(1, a, true)
	} else {
		setPage(a)
	}
}
var tweetTimeout;
var tweetMoving = false;

function getTweets() {
	if ($(".twitter").length > 0) {
		$(".twitter nav a").click(function (a) {
			a.target.style.webkitAnimationName = "";
			window.setTimeout(function () {
				a.target.style.webkitAnimationName = "spin"
			}, 0);
			if (!tweetMoving) {
				scrollTweets()
			}
			a.preventDefault()
		});
		$.getJSON("/z/hp/home.json", function (a) {
			shuffle(a);
			$.each(a, function (d, f) {
				if (f) {
					var g = f.text.replace(/((https?|s?ftp|ssh)\:\/\/[^"\s\<\>]*[^.,;'">\:\s\<\>\)\]\!])/g, function (i) {
						return '<a href="' + i + '" target="_top">' + i + "</a>"
					}).replace(/\B@([_a-z0-9]+)/ig, function (i) {
						return i.charAt(0) + '<a href="http://twitter.com/' + i.substring(1) + '" target="_top">' + i.substring(1) + "</a>"
					});
					var h = (d < 3) ? ' class="show"' : "";
					var b = (f.image) ? f.image : "http://0.tqn.com/d/gp/" + f.site_domain_name + "65x65.gif";
					var e = "<section" + h + '><figure><a href="http://twitter.com/' + f.screen_name + '"><img src="' + b + '" alt=""></a><figurecaption><a class="ir" href="http://twitter.com/' + f.screen_name + '">Follow ' + f.guide_name + " on Twitter</a></figurecaption></figure><hgroup><h1>" + f.guide_name + "</h1><h2>" + f.site_display_name + "</h2></hgroup><p>" + g + "</p></section>";
					$(".twitter-viewport").prepend(innerShiv(e))
				}
			})
		});
		$(".twitter").addClass("loaded")
	} else {
		tweetTimeout = window.setTimeout(getTweets, 1000, true)
	}
	return
}
function scrollTweets() {
	$(".twitter-viewport").animate({
		scrollTop: 0
	}, 250, "linear");
	$(".twitter-viewport section.show").eq(0).prev().addClass("show")
}
function shuffle(d) {
	for (var e = 0; e < d.length - 1; e++) {
		var b = e + Math.floor(Math.random() * (d.length - e));
		var a = d[b];
		d[b] = d[e];
		d[e] = a
	}
	return d
};