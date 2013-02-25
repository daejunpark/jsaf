package net.sourceforge.pmd.util.viewer.model;


import net.sourceforge.pmd.lang.ast.xpath.Attribute;


/**
 * A toolkit for vaious attribute translations
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: AttributeToolkit.java 7476 2011-11-21 20:45:07Z rpelisse $
 */

public class AttributeToolkit {

    /**
     * formats a value for its usage in XPath expressions
     *
     * @param attribute atribute which value should be formatted
     * @return formmated value
     */
    public static String formatValueForXPath(Attribute attribute) {
        return '\'' + attribute.getStringValue() + '\'';
    }

    /**
     * constructs a predicate from the given attribute
     *
     * @param attribute attribute to be formatted as predicate
     * @return predicate
     */
    public static String constructPredicate(Attribute attribute) {
        return "[@" + attribute.getName() + '=' +
                formatValueForXPath(attribute) + ']';
    }
}