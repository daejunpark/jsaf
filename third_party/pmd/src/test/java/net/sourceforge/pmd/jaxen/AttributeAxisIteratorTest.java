/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package net.sourceforge.pmd.jaxen;

import net.sourceforge.pmd.lang.ast.xpath.AttributeAxisIterator;
import net.sourceforge.pmd.lang.java.ast.DummyJavaNode;

import org.junit.Test;

public class AttributeAxisIteratorTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
	DummyJavaNode n = new DummyJavaNode(0);
        n.testingOnly__setBeginColumn(1);
        n.testingOnly__setBeginLine(1);
        AttributeAxisIterator iter = new AttributeAxisIterator(n);
        iter.remove();
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(AttributeAxisIteratorTest.class);
    }
}
