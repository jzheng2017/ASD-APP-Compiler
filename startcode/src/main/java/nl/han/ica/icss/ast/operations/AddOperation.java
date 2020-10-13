package nl.han.ica.icss.ast.operations;

import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
public class AddOperation extends Operation {

    @Override
    public String getNodeLabel() {
        return "Add";
    }

    @Override
    public int evaluate() {
        return ((Literal)lhs).getNumericValue() + ((Literal)rhs).getNumericValue();
    }
}
