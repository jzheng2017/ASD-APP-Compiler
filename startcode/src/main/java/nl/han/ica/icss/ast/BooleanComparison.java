package nl.han.ica.icss.ast;

import java.util.ArrayList;

public class BooleanComparison extends Expression {
    private boolean isNegated;
    private ComparisonOperator operator;
    private Expression left;
    private Expression right;

    public BooleanComparison(boolean isNegated) {
        this.isNegated = isNegated;
    }

    public BooleanComparison(ComparisonOperator operator) {
        this.operator = operator;
    }

    public BooleanComparison(boolean isNegated, ComparisonOperator operator, Expression left, Expression right) {
        this.isNegated = isNegated;
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public String getNodeLabel() {
        return (isNegated ? "Negated" : "") + "BooleanComparison (" + operator + ")";
    }

    @Override
    public ArrayList<ASTNode> getChildren() {
        ArrayList<ASTNode> children = new ArrayList<>();
        if (left != null)
            children.add(left);
        if (right != null)
            children.add(right);
        return children;
    }

    @Override
    public ASTNode addChild(ASTNode child) {
        if (this.left == null) {
            this.left = (Expression) child;
        } else if (this.right == null) {
            this.right = (Expression) child;
        }
        return this;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public void setOperator(ComparisonOperator operator) {
        this.operator = operator;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public void setNegated(boolean negated) {
        isNegated = negated;
    }
}
