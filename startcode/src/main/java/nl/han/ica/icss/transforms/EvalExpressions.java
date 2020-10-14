package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

import java.util.HashMap;
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
            variableValues.removeFirst();
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
        HashMap<String, Literal> currentScope = variableValues.getFirst();
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
        final Expression left = expression.getLeft();
        final Expression right = expression.getRight();

        ComparisonOperator operator = expression.getOperator();

        if (left instanceof BooleanComparison) {
            expression.setLeft(evaluateBooleanComparison((BooleanComparison) left));
        }

        if (right instanceof BooleanComparison) {
            expression.setRight(evaluateBooleanComparison((BooleanComparison) right));
        }

        if (left instanceof BooleanExpression) {
            expression.setLeft(evaluateBooleanExpression((BooleanExpression) left));
        }

        if (right instanceof BooleanExpression) {
            expression.setRight(evaluateBooleanExpression((BooleanExpression) right));
        }

        if (left instanceof VariableReference) {
            expression.setLeft(getVariableValue(((VariableReference) left).name));
        }

        if (right instanceof VariableReference) {
            expression.setRight(getVariableValue(((VariableReference) right).name));
        }

        if (left instanceof Operation) {
            expression.setLeft(evaluateOperation((Operation) left));
        }

        if (right instanceof Operation) {
            expression.setRight(evaluateOperation((Operation) right));
        }

        Literal leftLiteral = (Literal) expression.getLeft();
        Literal rightLiteral = (Literal) expression.getRight();


        if (expression.isNegated()) {
            return new BoolLiteral(!(leftLiteral.evaluate(rightLiteral, operator)));
        } else {
            return new BoolLiteral(leftLiteral.evaluate(rightLiteral, operator));
        }
    }


    private Literal evaluateBooleanExpression(BooleanExpression expression) {
        final Expression booleanExpression = expression.getExpression();
        Literal variableValue;
        if (booleanExpression instanceof VariableReference) {
            final String booleanExpressionName = ((VariableReference) booleanExpression).name;
            variableValue = getVariableValue(booleanExpressionName);
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
            value = copyLiteral(value);
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
        } else if (expression instanceof Operation) {
            currentNode.expression = evaluateOperation((Operation) expression);
        }
    }

    private boolean generateScope(ASTNode currentNode) {
        if (currentNode instanceof Stylesheet
                || currentNode instanceof Stylerule
                || currentNode instanceof IfClause
                || currentNode instanceof ElseClause) {
            HashMap<String, Literal> currentScope = new HashMap<>();
            variableValues.addFirst(currentScope);
            return true;
        }
        return false;
    }

    private Literal evaluateOperation(Operation operation) {
        if (operation.lhs instanceof Operation) {
            operation.lhs = evaluateOperation((Operation) operation.lhs);
        }

        if (operation.rhs instanceof Operation) {
            operation.rhs = evaluateOperation((Operation) operation.rhs);
        }

        if (operation.lhs instanceof VariableReference) {
            operation.lhs = getVariableValue(((VariableReference) operation.lhs).name);
        }

        if (operation.rhs instanceof VariableReference) {
            operation.rhs = getVariableValue(((VariableReference) operation.rhs).name);
        }

        if (operation.lhs instanceof PixelLiteral || operation.rhs instanceof PixelLiteral) {
            return new PixelLiteral(operation.evaluate());
        } else if (operation.lhs instanceof PercentageLiteral || operation.rhs instanceof PercentageLiteral) {
            return new PercentageLiteral(operation.evaluate());
        } else if (operation.lhs instanceof ScalarLiteral || operation.rhs instanceof ScalarLiteral) {
            return new ScalarLiteral(operation.evaluate());
        }


        return null;
    }

    private Literal getVariableValue(String variableName) {
        for (HashMap<String, Literal> currentScope : variableValues) {
            Literal value = currentScope.get(variableName);

            final boolean valueFound = value != null;

            if (valueFound) {
                return value;
            }
        }

        return null;
    }
}
