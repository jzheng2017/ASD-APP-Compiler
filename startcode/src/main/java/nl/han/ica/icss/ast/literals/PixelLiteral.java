package nl.han.ica.icss.ast.literals;

import nl.han.ica.icss.ast.ComparisonOperator;
import nl.han.ica.icss.ast.Literal;

import java.util.Objects;

public class PixelLiteral extends Literal {
    public int value;

    public PixelLiteral(int value) {
        this.value = value;
    }

    public PixelLiteral(String text) {
        this.value = Integer.parseInt(text.substring(0, text.length() - 2));
    }

    @Override
    public String getNodeLabel() {
        return "Pixel literal (" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PixelLiteral that = (PixelLiteral) o;
        return value == that.value;
    }

    @Override
    public String toString() {
        return "" + value + "px";
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean evaluate(Literal other, ComparisonOperator operator) {
        if (!(other instanceof PixelLiteral))
            throw new IllegalArgumentException("Comparing two different literals is not allowed");

        if (operator == ComparisonOperator.LT) {
            return this.value < ((PixelLiteral) other).value;
        } else if (operator == ComparisonOperator.LET) {
            return this.value <= ((PixelLiteral) other).value;
        } else if (operator == ComparisonOperator.EQ) {
            return this.value == ((PixelLiteral) other).value;
        } else if (operator == ComparisonOperator.NQ) {
            return this.value < ((PixelLiteral) other).value;
        } else if (operator == ComparisonOperator.GET) {
            return this.value >= ((PixelLiteral) other).value;
        } else if (operator == ComparisonOperator.GT) {
            return this.value > ((PixelLiteral) other).value;
        }

        throw new IllegalStateException(String.format("Unimplemented comparison operator: %s", operator));
    }
}
