package nl.han.ica.icss.ast;

public enum ComparisonOperator {
    LT("<"),
    LET("<="),
    EQ("=="),
    NQ("!="),
    GET(">="),
    GT(">");

    private final String stringValue;

    ComparisonOperator(String s) {
        this.stringValue = s;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}
