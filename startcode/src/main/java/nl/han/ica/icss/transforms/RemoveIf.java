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
            if (evaluateIfClause(currentNode, children)) {
                return;
            }
        }

        setScope(currentNode);

        setParent(currentNode);

        children.forEach(this::traverseTree);
    }

    private boolean evaluateIfClause(ASTNode currentNode, List<ASTNode> children) {
        Expression expression = ((IfClause) currentNode).conditionalExpression;
        ElseClause elseClause = ((IfClause) currentNode).elseClause;

        if (expression instanceof BoolLiteral) {
            final boolean value = ((BoolLiteral) expression).value;

            if (value) {
                evaluateChildren(children);
            } else if (elseClause != null) {
                List<ASTNode> elseChildren = elseClause.getChildren();
                evaluateChildren(elseChildren);
            }

            parentOfCurrentNode.removeChild(currentNode);
            return true;
        }

        return false;
    }

    private void evaluateChildren(List<ASTNode> children) {
        children.stream()
                .filter(node -> node instanceof Declaration)
                .forEach(scope::addChild);
        children.stream().filter(node -> node instanceof IfClause).forEach(this::traverseTree);
    }

    private void setScope(ASTNode currentNode) {
        if (currentNode instanceof Stylerule) {
            scope = currentNode;
        }
    }

    private void setParent(ASTNode currentNode) {
        if (currentNode instanceof Stylerule
                || currentNode instanceof IfClause
                || currentNode instanceof ElseClause) {
            parentOfCurrentNode = currentNode;
        }
    }

}
