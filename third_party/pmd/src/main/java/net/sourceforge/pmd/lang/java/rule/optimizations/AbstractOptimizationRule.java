/*
 * Created on Jan 11, 2005 
 *
 * $Id: AbstractOptimizationRule.java 7476 2011-11-21 20:45:07Z rpelisse $
 */
package net.sourceforge.pmd.lang.java.rule.optimizations;

import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.NameOccurrence;

/**
 * Base class with utility methods for optimization rules
 *
 * @author mgriffa
 */
public class AbstractOptimizationRule extends AbstractJavaRule {

    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (node.isInterface()) {
            return data;
        }
        return super.visit(node, data);
    }

    protected boolean assigned(List<NameOccurrence> usages) {
        for (NameOccurrence occ: usages) {
            if (occ.isOnLeftHandSide() || occ.isSelfAssignment()) {
                return true;
            }
        }
        return false;
    }

}
