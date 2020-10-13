package nl.han.ica.icss.transforms;

//BEGIN UITWERKING

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.List;
//EIND UITWERKING

public class RemoveIf implements Transform {
    private ASTNode scope;
    private ASTNode parentOfCurrentNode;

    @Override
    public void apply(AST ast) {
        scope = ast.root;
        traverseTree(ast.root);
    }

    private void traverseTree(ASTNode currentNode) {
        List<ASTNode> children = currentNode.getChildren();

        if (currentNode instanceof IfClause) {
            Expression expression = ((IfClause) currentNode).conditionalExpression;
            ElseClause elseClause = ((IfClause) currentNode).elseClause;
            if (expression instanceof BoolLiteral) {
                final boolean value = ((BoolLiteral) expression).value;

                if (value) {
                    children.forEach(scope::addChild);
                } else {
                    parentOfCurrentNode.removeChild(currentNode);
                    if (elseClause != null) {
                        List<ASTNode> elseChildren = elseClause.getChildren();
                        elseChildren.forEach(scope::addChild);
                    }
                    return;
                }
            }
        }

        if (currentNode instanceof Stylerule) {
            scope = currentNode;
        }

        if (currentNode instanceof Stylerule
                || currentNode instanceof IfClause
                || currentNode instanceof ElseClause) {
            parentOfCurrentNode = currentNode;
        }
        
        children.forEach(this::traverseTree);
    }

}
