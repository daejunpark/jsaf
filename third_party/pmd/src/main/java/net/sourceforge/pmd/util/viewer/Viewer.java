package net.sourceforge.pmd.util.viewer;

import net.sourceforge.pmd.lang.xpath.Initializer;
import net.sourceforge.pmd.util.viewer.gui.MainFrame;

/**
 * viewer's starter
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: Viewer.java 7476 2011-11-21 20:45:07Z rpelisse $
 */
public class Viewer {
    public static void main(String[] args) {
	Initializer.initialize();
        new MainFrame();
    }
}
