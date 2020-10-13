package nl.han.ica.icss.ast.operations;

import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.transforms.EvalExpressions;

import java.util.HashMap;
import java.util.LinkedList;

public class MultiplyOperation extends Operation {

    @Override
    public String getNodeLabel() {
        return "Multiply";
    }

    @Override
    public int evaluate() {
        return ((Literal)lhs).getNumericValue() * ((Literal)rhs).getNumericValue();
    }
}
