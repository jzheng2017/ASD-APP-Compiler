package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EvalExpressions implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public EvalExpressions() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        traverseTreeAndEvaluate(ast.root);
    }

    private void traverseTreeAndEvaluate(ASTNode currentNode) {
        List<ASTNode> children = currentNode.getChildren();

        final boolean scopeCreated = generateScope(currentNode);

        if (currentNode instanceof VariableAssignment) {
            evaluateVariableAssignment((VariableAssignment) currentNode);
        } else if (currentNode instanceof IfClause) {
            evaluateIfClause((IfClause) currentNode);
        } else if (currentNode instanceof Declaration) {
            evaluateDeclaration((Declaration) currentNode);
        }

        children.forEach(this::traverseTreeAndEvaluate);

        if (scopeCreated) {
            variableValues.removeLast();
        }
    }

    private void evaluateIfClause(IfClause currentNode) {
        final Expression conditionalExpression = currentNode.conditionalExpression;

        if (conditionalExpression instanceof VariableReference) {
            final String expressionName = ((VariableReference) conditionalExpression).name;

            currentNode.conditionalExpression = getVariableValue(expressionName);
        } else if (conditionalExpression instanceof BooleanExpression) {
            final BooleanExpression booleanExpression = ((BooleanExpression) conditionalExpression);
            currentNode.conditionalExpression = evaluateBooleanExpression(booleanExpression);
        } else if (conditionalExpression instanceof BooleanComparison) {
            final BooleanComparison booleanComparison = (BooleanComparison) conditionalExpression;
            currentNode.conditionalExpression = evaluateBooleanComparison(booleanComparison);
        }
    }

    private void evaluateVariableAssignment(VariableAssignment currentNode) {
        final String variableName = currentNode.name.name;
        final Expression expression = currentNode.expression;
        HashMap<String, Literal> currentScope = variableValues.getLast();
        Literal variableValue = null;

        if (expression instanceof Operation) {
            variableValue = evaluateVariableAssignmentOperation((Operation) expression);
        } else if (expression instanceof Literal) {
            currentScope.put(variableName, (Literal) expression);
            return;
        } else if (expression instanceof VariableReference) {
            variableValue = evaluateVariableReference((VariableReference) expression);
        } else if (expression instanceof BooleanExpression) {
            variableValue = evaluateBooleanExpression((BooleanExpression) expression);
        } else if (expression instanceof BooleanComparison) {
            variableValue = evaluateBooleanComparison((BooleanComparison) expression);
        }

        currentNode.expression = variableValue;
        if (variableValue != null) {
            currentScope.put(variableName, variableValue);
        }
    }

    private Literal evaluateBooleanComparison(BooleanComparison expression) {
        Expression leftExpression = expression.getLeft();
        Expression rightExpression = expression.getRight();

        ComparisonOperator operator = expression.getOperator();

        if (leftExpression instanceof VariableReference) {
            leftExpression = getVariableValue(((VariableReference) leftExpression).name);
        }

        if (rightExpression instanceof VariableReference) {
            rightExpression = getVariableValue(((VariableReference) rightExpression).name);
        }

        Literal leftLiteral = (Literal) leftExpression;
        Literal rightLiteral = (Literal) rightExpression;

        return new BoolLiteral(leftLiteral.evaluate(rightLiteral, operator));

    }


    private Literal evaluateBooleanExpression(BooleanExpression expression) {
        final Expression booleanExpression = expression.getExpression();
        Literal variableValue;
        if (booleanExpression instanceof VariableReference) {
            final String booleanExpressionName = ((VariableReference) booleanExpression).name;
            variableValue = copyLiteral(getVariableValue(booleanExpressionName));
            variableValue = expression.isNegated() ? negateValue(variableValue) : variableValue;

            return variableValue;

        } else if (booleanExpression instanceof Literal) {
            variableValue = expression.isNegated() ? negateValue((Literal) booleanExpression) : (Literal) booleanExpression;

            return variableValue;
        }
        return null;
    }

    private Literal copyLiteral(Literal literal) {
        if (literal instanceof BoolLiteral) {
            return new BoolLiteral(((BoolLiteral) literal).value);
        }

        return null;
    }

    private Literal evaluateVariableReference(VariableReference expression) {
        final String expressionName = expression.name;
        return getVariableValue(expressionName);
    }

    private Literal evaluateVariableAssignmentOperation(Operation operation) {
        return evaluateOperation(operation);
    }

    private Literal negateValue(Literal value) {
        if (value instanceof BoolLiteral) {
            boolean internalValue = ((BoolLiteral) value).value;
            ((BoolLiteral) value).value = !internalValue;
        }
        return value;
    }

    private void evaluateDeclaration(Declaration currentNode) {
        final Expression expression = currentNode.expression;
        if (expression instanceof VariableReference) {
            final String variableName = ((VariableReference) expression).name;
            currentNode.expression = getVariableValue(variableName);
        }
    }

    private boolean generateScope(ASTNode currentNode) {
        if (currentNode instanceof Stylesheet
                || currentNode instanceof Stylerule
                || currentNode instanceof IfClause
                || currentNode instanceof ElseClause) {
            HashMap<String, Literal> currentScope = new HashMap<>();
            variableValues.add(currentScope);
            return true;
        }
        return false;
    }

    private Literal evaluateOperation(Operation operation) {
        Expression lhs = operation.lhs;
        Expression rhs = operation.rhs;

        if (lhs instanceof Operation) {
            lhs = evaluateOperation((Operation) lhs);
        }

        if (rhs instanceof Operation) {
            rhs = evaluateOperation((Operation) rhs);
        }

        if (lhs instanceof VariableReference) {
            lhs = getVariableValue(((VariableReference) lhs).name);
        }

        if (rhs instanceof VariableReference) {
            rhs = getVariableValue(((VariableReference) rhs).name);
        }
//        if (operation instanceof AddOperation) {
//            if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral) {
//                return new PixelLiteral(((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value);
//            } else if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral) {
//                return new PercentageLiteral(((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value);
//            } else if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
//                return new ScalarLiteral(((ScalarLiteral) lhs).value + ((ScalarLiteral) rhs).value);
//            }
//        } else if (operation instanceof SubtractOperation) {
//            if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral) {
//                return new PixelLiteral(((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value);
//            } else if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral) {
//                return new PercentageLiteral(((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value);
//            } else if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
//                return new ScalarLiteral(((ScalarLiteral) lhs).value - ((ScalarLiteral) rhs).value);
//            }
//        } else if (operation instanceof MultiplyOperation) {
//            if (lhs instanceof PixelLiteral && rhs instanceof ScalarLiteral) {
//                return new PixelLiteral(((PixelLiteral) lhs).value * ((ScalarLiteral) rhs).value);
//
//            } else if (lhs instanceof PercentageLiteral && rhs instanceof ScalarLiteral) {
//                return new PixelLiteral(((PercentageLiteral) lhs).value * ((ScalarLiteral) rhs).value);
//
//            } else if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
//                return new PixelLiteral(((ScalarLiteral) lhs).value * ((ScalarLiteral) rhs).value);
//            }
//        }

        if (lhs instanceof Literal) {
            return (Literal) lhs;
        } else if (rhs instanceof Literal) {
            return (Literal) rhs;
        }


        return null;
    }

    private Literal getVariableValue(String variableName) {
        Iterator<HashMap<String, Literal>> iterator = variableValues.descendingIterator();

        //iterating backwards because the last scope in the list is the most recent scope
        while (iterator.hasNext()) {
            HashMap<String, Literal> currentScope = iterator.next();

            Literal value = currentScope.get(variableName);

            final boolean valueFound = value != null;

            if (valueFound) {
                return value;
            }
        }

        return null;
    }
}
