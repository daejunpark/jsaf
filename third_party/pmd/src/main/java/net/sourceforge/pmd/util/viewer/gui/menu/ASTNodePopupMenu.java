package net.sourceforge.pmd.util.viewer.gui.menu;

import javax.swing.JPopupMenu;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.viewer.model.ViewerModel;

/**
 * context sensetive menu for the AST Panel
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: ASTNodePopupMenu.java 7476 2011-11-21 20:45:07Z rpelisse $
 */
public class ASTNodePopupMenu extends JPopupMenu {
    private ViewerModel model;
    private Node node;

    public ASTNodePopupMenu(ViewerModel model, Node node) {
        this.model = model;
        this.node = node;
        init();
    }

    private void init() {
        add(new SimpleNodeSubMenu(model, node));
        addSeparator();
        add(new AttributesSubMenu(model, node));
    }
}
