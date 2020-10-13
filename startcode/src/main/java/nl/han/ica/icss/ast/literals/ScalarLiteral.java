package nl.han.ica.icss.ast.literals;

import nl.han.ica.icss.ast.ComparisonOperator;
import nl.han.ica.icss.ast.Literal;

import java.util.Objects;

public class ScalarLiteral extends Literal {
    public int value;

    public ScalarLiteral(int value) {
        this.value = value;
    }

    public ScalarLiteral(String text) {
        this.value = Integer.parseInt(text);
    }

    @Override
    public String getNodeLabel() {
        return "Scalar literal (" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ScalarLiteral that = (ScalarLiteral) o;
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
        if (!(other instanceof ScalarLiteral))
            throw new IllegalArgumentException("Comparing two different literals is not allowed");

        if (operator == ComparisonOperator.LT) {
            return this.value < ((ScalarLiteral) other).value;
        } else if (operator == ComparisonOperator.LET) {
            return this.value <= ((ScalarLiteral) other).value;
        } else if (operator == ComparisonOperator.EQ) {
            return this.value == ((ScalarLiteral) other).value;
        } else if (operator == ComparisonOperator.NQ) {
            return this.value < ((ScalarLiteral) other).value;
        } else if (operator == ComparisonOperator.GET) {
            return this.value >= ((ScalarLiteral) other).value;
        } else if (operator == ComparisonOperator.GT) {
            return this.value > ((ScalarLiteral) other).value;
        }

        throw new IllegalStateException(String.format("Unimplemented comparison operator: %s", operator));
    }

    @Override
    public int getNumericValue() {
        return this.value;
    }
}
