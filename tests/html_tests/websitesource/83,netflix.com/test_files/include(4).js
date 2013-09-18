if (Modernizr.cssanimations && Modernizr.opacity) {
    jQuery('#content, footer').addClass('loaded');
}
if (!Modernizr.backgroundsize) {
    var adjustBkg = function () {
        var w_width = jQuery(window).width();
        if (w_width < 1450) {
            jQuery('img.fill-bkg, #fill-bkg-container, .call-to-action').addClass('min-size');
            jQuery('img.fill-bkg, #fill-bkg-container, .call-to-action').removeClass('max-size');

            var l_offset = Math.max(0 - ((1450 - w_width) / 2), -220);

            jQuery('img.fill-bkg').css('left', l_offset + 'px');
            jQuery('#fill-bkg-container.min-size').css('width', w_width + 'px');
            jQuery('#fill-bkg-container.min-size').css('height', jQuery(window).height() + 'px');

        } else if (w_width > 1920) {
            jQuery('img.fill-bkg, #fill-bkg-container, .call-to-action').addClass('max-size');
            jQuery('img.fill-bkg, #fill-bkg-container, .call-to-action').removeClass('min-size');

            var l_offset = (w_width - 1920) / 2;

            jQuery('img.fill-bkg').css('left', l_offset + 'px');
            jQuery('#fill-bkg-container.max-size').css('width', w_width + 'px');
            jQuery('#fill-bkg-container.max-size').css('height', jQuery(window).height() + 'px');

        } else {
            jQuery('img.fill-bkg, #fill-bkg-container, .call-to-action').removeClass('min-size');
            jQuery('img.fill-bkg, #fill-bkg-container, .call-to-action').removeClass('max-size');
            jQuery('img.fill-bkg').css('left', '0px');
            jQuery('#fill-bkg-container').css('width', 'auto');
            jQuery('#fill-bkg-container').css('height', 'auto');
        }
    };

    jQuery(window).resize(adjustBkg);

    adjustBkg();
}