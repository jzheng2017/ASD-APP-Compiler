package nl.han.ica.icss.ast;

public abstract class Literal extends Expression {

    public abstract boolean evaluate(Literal other, ComparisonOperator operator);
    public abstract int getNumericValue();
}
