function download_timeout(theFile ,timeout) { // links to download PHP by type
    if(!timeout) timeout=0;
	var post_download = '/systems/affiliates/landing/download.php';
    document.location.href = theFile;
    setTimeout(function() {
        document.location.href = post_download;
    }, timeout);

}

function download(theFile, Lang, urchin) { // links to download PHP by type
    return downloadurchin(theFile, Lang, urchin);
}
function downloadurchin(theFile, Lang, urchin) { // links to download PHP by type
    var post_download = '/systems/affiliates/landing/download.php';
    //post_download += '?id=55555';
    //post_download += '&lang=' + (Lang || '');
    //		window.open(theFile, 'Download', 'scrollbars=no,status=no,width=1,height=1');
    //  document.location.href = post_download;
    document.location.href = theFile;
    setTimeout(function() {
        document.location.href = post_download;
    }, 4000);

}
function purchase(theFile, urchin) { // links to download PHP by type
    document.location.href = theFile;
}
function stopPropagation(e) {
    e = e || window.event;
    if (e.stopPropagation) {
        e.stopPropagation();
    } else {
        e.cancelBubble = true;
    }
}
function _openMenu(id) {
    var el = document.getElementById('tree-item-' + id);
    if (!el) {
        return;
    }
    var child = el.getElementsByTagName('ul')[0], navigation = document.getElementById('navigation');
    if (child) {
        child.style.display = 'block';
    }
    if (el.className != 'section-header') {
        el.className = 'uberlink';
    }
    el.style.display = 'list-item';
    el = el.parentNode;
    while (el) {
        if (el == navigation) break;
        el.style.display = (el.tagName.toLowerCase() == 'li' && el.className != 'section-header') ? 'list-item' : 'block';
        el = el.parentNode;
    }
}

var adult_warning = "This glossary is for adults only!\n\n\
You must be 18 years old to register to it, since it contains sexually explicit material.\n\
By choosing to download or subscribe to this glossary you are aware that you will be nviewing material of a sexual nature.\n\n\
I am voluntarily choosing to register to this glossary, because I want to view,\n\
read and/or hear the various materials, which are available.\n\n\
I will not permit any person(s) under 18 years of age to have access to any of the\n\
materials contained within this glossary.\n\n\
I do not find images of nude adults, adults engaged in sexual acts, or other sexual\n\
material to be offensive or objectionable.";

function downloadDict(uri, adult) {

    if (adult && !confirm(adult_warning)) { return; }

    uri = uri.replace('+', '%2B');
    uri = uri.replace('-', '%2D');

    document.location = 'http://www.babylon.com/redirects/download.cgi?type=7327&uri='+uri;
};

function menuHover(el,onout) {
  if (onout=='on') {
    el.style.color = '#96c627';el.style.backgroundColor = '#2e3133';
  }
  else {
    el.style.color = '#fff';el.style.backgroundColor = '#393d3f';
  }
}

function downloadPostDownload(theFile, postDownload) {
    document.location.href = theFile;
    setTimeout(function() {
        document.location.href = postDownload || '/systems/affiliates/landing/download.php';
    }, 4000);
}
