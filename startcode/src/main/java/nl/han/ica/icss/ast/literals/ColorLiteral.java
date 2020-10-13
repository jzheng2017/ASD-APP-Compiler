package nl.han.ica.icss.ast.literals;

import nl.han.ica.icss.ast.ComparisonOperator;
import nl.han.ica.icss.ast.Literal;

import java.util.Objects;

public class ColorLiteral extends Literal {
    public String value;

    public ColorLiteral(String value) {
        this.value = value;
    }

    @Override
    public String getNodeLabel() {
        return "Color literal (" + value + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorLiteral that = (ColorLiteral) o;
        return Objects.equals(value, that.value);
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
        if (!(other instanceof ColorLiteral))
            throw new IllegalArgumentException("Comparing two different literals is not allowed");


        if (operator == ComparisonOperator.EQ) {
            return this.value.equals(((ColorLiteral) other).value);
        } else if (operator == ComparisonOperator.NQ) {
            return !(this.value.equals(((ColorLiteral) other).value));
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
        throw new UnsupportedOperationException("Color literal can not be evaluated to a numeric value");
    }
}
