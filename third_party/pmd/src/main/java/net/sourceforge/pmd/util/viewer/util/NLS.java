package net.sourceforge.pmd.util.viewer.util;

import java.util.ResourceBundle;

/**
 * helps with internationalization
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: NLS.java 7476 2011-11-21 20:45:07Z rpelisse $
 */
public class NLS {
    private final static ResourceBundle BUNDLE;

    static {
        BUNDLE = ResourceBundle.getBundle("net.sourceforge.pmd.util.viewer.resources.viewer_strings");
    }

    /**
     * translates the given key to the message
     *
     * @param key key to be translated
     * @return translated string
     */
    public static String nls(String key) {
        return BUNDLE.getString(key);
    }
}

