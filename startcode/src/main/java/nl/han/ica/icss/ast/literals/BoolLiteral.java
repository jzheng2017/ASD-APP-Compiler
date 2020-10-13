package nl.han.ica.icss.ast.literals;

import nl.han.ica.icss.ast.ComparisonOperator;
import nl.han.ica.icss.ast.Literal;

import java.util.Objects;

public class BoolLiteral extends Literal {
    public boolean value;

    public BoolLiteral(boolean value) {
        this.value = value;
    }

    public BoolLiteral(String text) {
        this.value = text.equals("TRUE");
    }

    @Override
    public String getNodeLabel() {
        String textValue = value ? "TRUE" : "FALSE";
        return "Bool Literal (" + textValue + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BoolLiteral that = (BoolLiteral) o;
        return value == that.value;
    }

    @Override
    public String toString() {
        return "" + value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean evaluate(Literal other, ComparisonOperator operator) {
        if (!(other instanceof BoolLiteral))
            throw new IllegalArgumentException("Comparing two different literals is not allowed");


        if (operator == ComparisonOperator.EQ) {
            return this.value == ((BoolLiteral) other).value;
        } else if (operator == ComparisonOperator.NQ) {
            return this.value != ((BoolLiteral) other).value;
        } else if (operator == ComparisonOperator.AND) {
            return this.value && ((BoolLiteral) other).value;
        } else if (operator == ComparisonOperator.OR) {
            return this.value || ((BoolLiteral) other).value;
        } else if (operator == ComparisonOperator.LT
                || operator == ComparisonOperator.LET
                || operator == ComparisonOperator.GET
                || operator == ComparisonOperator.GT) {
            throw new IllegalArgumentException(String.format("Non numeric literals can not be used with the %s operator", operator));
        }

        throw new IllegalStateException(String.format("Unimplemented comparison operator: %s", operator));
    }

    @Override
    public int getNumericValue() {
        throw new UnsupportedOperationException("Bool literal can not be evaluated to a numeric value");
    }
}
