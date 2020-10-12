package nl.han.ica.icss.ast;

import nl.han.ica.icss.ast.literals.BoolLiteral;

public abstract class Literal extends Expression {

    public abstract boolean evaluate(Literal other, ComparisonOperator operator);
}
