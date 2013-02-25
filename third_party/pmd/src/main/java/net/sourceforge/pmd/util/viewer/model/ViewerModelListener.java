package net.sourceforge.pmd.util.viewer.model;

/**
 * identiefie a listener of the ViewerModel
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: ViewerModelListener.java 7476 2011-11-21 20:45:07Z rpelisse $
 */
public interface ViewerModelListener {
    void viewerModelChanged(ViewerModelEvent e);
}
