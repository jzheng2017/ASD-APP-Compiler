package nl.han.ica.icss.ast;

import java.util.ArrayList;

public class BooleanExpression extends Expression {
    private boolean isNegated;
    private Expression expression;

    public BooleanExpression() {

    }

    public BooleanExpression(boolean isNegated) {
        this.isNegated = isNegated;
    }

    public BooleanExpression(boolean isNegated, Expression expression) {
        this.isNegated = isNegated;
        this.expression = expression;
    }

    @Override
    public ASTNode addChild(ASTNode child) {
        if (child instanceof Expression) {
            this.expression = (Expression) child;
        }
        return this;
    }

    @Override
    public String getNodeLabel() {
        return (isNegated ? "Negated" : "") + "BooleanExpression";
    }

    @Override
    public ArrayList<ASTNode> getChildren() {
        ArrayList<ASTNode> children = new ArrayList<>();
        if (expression != null)
            children.add(expression);
        return children;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public void setNegated(boolean negated) {
        isNegated = negated;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }
}
