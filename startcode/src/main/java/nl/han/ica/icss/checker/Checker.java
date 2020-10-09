package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.*;


public class Checker {
    private LinkedList<HashMap<String, ExpressionType>> variableTypes;
    private LinkedList<ScopeTypes> scopeTypes = new LinkedList<>();

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        scopeTypes = new LinkedList<>();
        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        checkChildren(stylesheet);
    }

    private void checkChildren(ASTNode currentNode) {
        List<ASTNode> children = currentNode.getChildren();

        generateScope(currentNode);
        scanVariables(currentNode);
        checkScope(currentNode);
        checkSemantic(currentNode);

        children.forEach(this::checkChildren);

        removeScope(currentNode);
    }

    private void removeScope(ASTNode currentNode) {
        if (determineScopeType(currentNode) != null) {
            variableTypes.removeLast();
        }
    }

    private void generateScope(ASTNode currentNode) {
        if (currentNode instanceof Stylesheet
                || currentNode instanceof Stylerule
                || currentNode instanceof IfClause
                || currentNode instanceof ElseClause) {
            HashMap<String, ExpressionType> currentScope = new HashMap<>();
            variableTypes.add(currentScope);
            scopeTypes.add(determineScopeType(currentNode));
        }
    }

    private void scanVariables(ASTNode currentNode) {
        if (currentNode instanceof VariableAssignment) {
            HashMap<String, ExpressionType> currentScope = variableTypes.getLast();
            String variableName = ((VariableAssignment) currentNode).name.name;
            ExpressionType variableExpressionType = determineExpressionType(((VariableAssignment) currentNode).expression);
            currentScope.put(variableName, variableExpressionType);
        }
    }

    private void checkSemantic(ASTNode currentNode) {
        if (currentNode instanceof Declaration) {
            final String propertyName = ((Declaration) currentNode).property.name;

            if (isPropertyIllegal(propertyName)) {
                currentNode.setError(String.format("%s is not a legal property name!", propertyName));
            }

            if (!isPropertyValueTypeAllowed(currentNode)) {
                currentNode.setError(String.format("%s has an illegal value type or expression!", propertyName));
            }
        } else if (currentNode instanceof IfClause) {
            final Expression conditionalExpression = ((IfClause) currentNode).conditionalExpression;
            final ExpressionType expressionType = determineExpressionType(conditionalExpression);

            if (expressionType != ExpressionType.BOOL) {
                currentNode.setError(String.format("Expected type: %s. Actual type: %s", ExpressionType.BOOL, expressionType));
            }
        }
    }

    private boolean isPropertyIllegal(String propertyName) {
        List<String> allowedProperties = new ArrayList<>(Arrays.asList("background-color", "color", "width", "height"));

        return !allowedProperties.contains(propertyName);
    }

    private boolean isPropertyValueTypeAllowed(ASTNode currentNode) {
        final String propertyName = ((Declaration) currentNode).property.name;
        final Expression expression = ((Declaration) currentNode).expression;
        ExpressionType propertyExpressionType = determineExpressionType(((Declaration) currentNode).expression);

        final boolean isExpression = propertyExpressionType == ExpressionType.UNDEFINED;

        if (propertyName.equals("background-color") || propertyName.equals("color")) {
            if (isExpression) {
                if (expression instanceof VariableReference) {
                    propertyExpressionType = getExpressionType(expression);
                } else if (expression instanceof Operation) {
                    return isOperationAllowed((Operation) expression);
                }
            }

            return propertyExpressionType == ExpressionType.COLOR;
        } else {
            if (isExpression) {
                if (expression instanceof VariableReference) {
                    propertyExpressionType = getExpressionType(expression);
                } else if (expression instanceof Operation) {
                    return isOperationAllowed((Operation) expression);
                }
            }
            return propertyExpressionType == ExpressionType.PERCENTAGE
                    || propertyExpressionType == ExpressionType.PIXEL;
        }
    }

    private ExpressionType getExpressionType(Expression expression) {
        final String variableName = ((VariableReference) expression).name;
        return getVariableExpressionType(variableName);
    }

    private boolean isOperationAllowed(Operation operation) {
        Expression left = operation.lhs;
        Expression right = operation.rhs;

        if (left instanceof Operation) {
            return isOperationAllowed((Operation) left);
        }

        if (right instanceof Operation) {
            return isOperationAllowed((Operation) right);
        }


        return evaluateOperation(operation);
    }

    private boolean evaluateOperation(Operation operation) {
        Expression left = operation.lhs;
        Expression right = operation.rhs;

        ExpressionType leftExpressionType = determineExpressionType(left);
        ExpressionType rightExpressionType = determineExpressionType(right);

        if (leftExpressionType == ExpressionType.COLOR || rightExpressionType == ExpressionType.COLOR) { //operation can not contain a color literal
            return false;
        } else {
            boolean isAddOrSubtract = operation instanceof AddOperation || operation instanceof SubtractOperation;
            boolean isMultiplication = operation instanceof MultiplyOperation;

            if (isAddOrSubtract) {
                boolean hasPixelLiteralOnBothSides = leftExpressionType == ExpressionType.PIXEL && rightExpressionType == ExpressionType.PIXEL;
                boolean hasPercentageLiteralOnBothSides = leftExpressionType == ExpressionType.PERCENTAGE && rightExpressionType == ExpressionType.PERCENTAGE;

                return hasPixelLiteralOnBothSides || hasPercentageLiteralOnBothSides;
            } else if (isMultiplication) {
                return leftExpressionType == ExpressionType.SCALAR || rightExpressionType == ExpressionType.SCALAR;
            }
        }

        return false;
    }


    private void checkScope(ASTNode currentNode) {
        if (currentNode instanceof VariableReference) {
            final String variableName = ((VariableReference) currentNode).name;
            if (!isVariableInScope(variableName)) {
                currentNode.setError(String.format("%s variable has not been initialized!", variableName));
            }
        }
    }

    private boolean isVariableInScope(String variableName) {
        for (HashMap<String, ExpressionType> scope : variableTypes) {
            if (scope.get(variableName) != null) {
                return true;
            }
        }

        return false;
    }

    private ExpressionType getVariableExpressionType(String variableName) {
        Iterator<HashMap<String, ExpressionType>> iterator = variableTypes.descendingIterator();

        while (iterator.hasNext()) {
            HashMap<String, ExpressionType> currentScope = iterator.next();

            ExpressionType expressionType = currentScope.get(variableName);

            if (expressionType != null) {
                return expressionType;
            }
        }

        throw new IllegalArgumentException("Variable not in scope!");
    }

    private ScopeTypes determineScopeType(ASTNode currentNode) {
        if (currentNode instanceof Stylesheet) {
            return ScopeTypes.STYLESHEET;
        } else if (currentNode instanceof Stylerule) {
            return ScopeTypes.STYLE_RULE;
        } else if (currentNode instanceof IfClause) {
            return ScopeTypes.IF;
        } else if (currentNode instanceof ElseClause) {
            return ScopeTypes.ELSE;
        }

        return null;
    }

    private ExpressionType determineExpressionType(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof VariableReference) {
            return getVariableExpressionType(((VariableReference) expression).name);
        } else {
            return ExpressionType.UNDEFINED;
        }
    }
}
