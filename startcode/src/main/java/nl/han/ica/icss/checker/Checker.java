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
    private LinkedList<ExpressionType> expressionTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        expressionTypes = new LinkedList<>();
        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        checkChildren(stylesheet);
    }

    private void checkChildren(ASTNode currentNode) {
        List<ASTNode> children = currentNode.getChildren();

        final boolean scopeCreated = generateScope(currentNode);
        registerVariables(currentNode);
        checkScope(currentNode);
        checkSemantic(currentNode);

        children.forEach(this::checkChildren);

        if (scopeCreated) {
            variableTypes.removeFirst();
        }
    }

    private boolean generateScope(ASTNode currentNode) {
        if (currentNode instanceof Stylesheet
                || currentNode instanceof Stylerule
                || currentNode instanceof IfClause
                || currentNode instanceof ElseClause) {
            HashMap<String, ExpressionType> currentScope = new HashMap<>();
            variableTypes.addFirst(currentScope);
            return true;
        }
        return false;
    }

    private void registerVariables(ASTNode currentNode) {
        if (currentNode instanceof VariableAssignment) {
            HashMap<String, ExpressionType> currentScope = variableTypes.getFirst();
            String variableName = ((VariableAssignment) currentNode).name.name;
            ExpressionType existingVariableExpressionType = getVariableExpressionType(variableName);
            ExpressionType variableExpressionType = determineExpressionType(((VariableAssignment) currentNode).expression);

            final boolean variableDoesNotExistYet = existingVariableExpressionType == ExpressionType.UNDEFINED;
            final boolean referencingUndefinedVariable = variableDoesNotExistYet && variableExpressionType == ExpressionType.UNDEFINED;

            if (!referencingUndefinedVariable && variableDoesNotExistYet) {
                currentScope.put(variableName, variableExpressionType);
            } else if (!variableDoesNotExistYet) {
                final boolean newValueIsOfSameType = variableExpressionType != ExpressionType.UNDEFINED && existingVariableExpressionType == variableExpressionType;

                if (newValueIsOfSameType) {
                    currentScope.put(variableName, variableExpressionType);
                } else {
                    currentNode
                            .setError(String.format(
                                    "You can not change the data type of an existing variable! Expected type: %s Actual type: %s"
                                    , existingVariableExpressionType
                                    , variableExpressionType));
                }
            } else {
                currentScope.put(variableName, ExpressionType.UNDEFINED);
            }
        }
    }

    private void checkSemantic(ASTNode currentNode) {
        if (currentNode instanceof Operation) {
            checkOperationSemantic(currentNode);
        } else if (currentNode instanceof Declaration) {
            checkDeclarationSemantic(currentNode);
        } else if (currentNode instanceof IfClause) {
            checkIfClauseSemantic(currentNode);
        } else if (currentNode instanceof BooleanComparison) {
            checkBooleanComparisonSemantic(currentNode);
        } else if (currentNode instanceof BooleanExpression) {
            checkBooleanExpressionSemantic(currentNode);
        }
    }

    private void checkBooleanExpressionSemantic(ASTNode currentNode) {
        BooleanExpression booleanExpression = (BooleanExpression) currentNode;
        Expression expression = booleanExpression.getExpression();
        ExpressionType expressionType = determineExpressionType(expression);

        if (expressionType != ExpressionType.BOOL) {
            currentNode.setError(String.format("BooleanExpression: Expected type: %s, Actual type: %s", ExpressionType.BOOL, expressionType));
        }
    }

    private void checkBooleanComparisonSemantic(ASTNode currentNode) {
        BooleanComparison booleanComparison = (BooleanComparison) currentNode;
        ExpressionType left = determineExpressionType(booleanComparison.getLeft());
        ExpressionType right = determineExpressionType(booleanComparison.getRight());
        ComparisonOperator operator = booleanComparison.getOperator();

        final boolean bothSidesSameType = left == right;

        if (bothSidesSameType) {
            checkValidityOperatorWithExpressionType(currentNode, left, right, operator);
        } else {
            currentNode.setError("BooleanComparison: Both sides must be of the same data type!");
        }
    }

    private void checkValidityOperatorWithExpressionType(ASTNode currentNode, ExpressionType left, ExpressionType right, ComparisonOperator operator) {
        final boolean isNotEqualityOperator = operator != ComparisonOperator.EQ && operator != ComparisonOperator.NQ;
        final boolean isNotLogicOperator = operator != ComparisonOperator.AND && operator != ComparisonOperator.OR;
        final boolean isNonNumericExpressionType = left == ExpressionType.COLOR || right == ExpressionType.BOOL;

        final boolean isIllegalBooleanComparison = (isNotEqualityOperator && isNotLogicOperator) && isNonNumericExpressionType;

        if (isIllegalBooleanComparison) {
            currentNode.setError(String.format("BooleanComparison: Non numeric literals can not be used with the %s operator", operator));
        }
    }

    private void checkOperationSemantic(ASTNode currentNode) {
        if (!isOperationAllowed((Operation) currentNode)) {
            currentNode.setError("Illegal operation");
        }
    }

    private void checkDeclarationSemantic(ASTNode currentNode) {
        final String propertyName = ((Declaration) currentNode).property.name;

        if (isPropertyIllegal(propertyName)) {
            currentNode.setError(String.format("%s is not a legal property name!", propertyName));
        }

        if (!isPropertyValueTypeAllowed(currentNode)) {
            currentNode.setError(String.format("%s has an illegal value type or expression!", propertyName));
        }
    }

    private void checkIfClauseSemantic(ASTNode currentNode) {
        final Expression conditionalExpression = ((IfClause) currentNode).conditionalExpression;
        final ExpressionType expressionType = determineExpressionType(conditionalExpression);

        if (expressionType != ExpressionType.BOOL) {
            currentNode.setError(String.format("If Clause: Expected type: %s. Actual type: %s", ExpressionType.BOOL, expressionType));
        }
    }

    private boolean isPropertyIllegal(String propertyName) {
        List<String> allowedProperties = new ArrayList<>(Arrays.asList("background-color", "color", "width", "height"));

        return !allowedProperties.contains(propertyName);
    }

    private boolean isPropertyValueTypeAllowed(ASTNode currentNode) {
        final String propertyName = ((Declaration) currentNode).property.name;
        final Expression expression = ((Declaration) currentNode).expression;
        ExpressionType propertyExpressionType = determineExpressionType(expression);

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

    private boolean isOperationAllowed(Operation operation) {
        expressionTypes.clear();
        return validateOperation(operation);
    }

    private boolean validateOperation(Operation operation) {
        Expression left = operation.lhs;
        Expression right = operation.rhs;

        if (addExpressionTypeToList(left)) {
            return false;
        }

        if (addExpressionTypeToList(right)) {
            return false;
        }

        return validateOperationExpressionTypes(operation);
    }

    private boolean addExpressionTypeToList(Expression expression) {
        if (expression instanceof Operation) {
            return !validateOperation((Operation) expression);
        } else if (expression instanceof VariableReference) {
            ExpressionType expressionType = getVariableExpressionType(((VariableReference) expression).name);
            if (expressionType != ExpressionType.UNDEFINED) {
                expressionTypes.addFirst(expressionType);
            } else {
                return true;
            }
        } else if (expression instanceof Literal) {
            ExpressionType expressionType = determineExpressionType(expression);
            if (expressionType != ExpressionType.UNDEFINED) {
                expressionTypes.addFirst(expressionType);
            }
        }
        return false;
    }

    private boolean validateOperationExpressionTypes(Operation operation) {
        ExpressionType leftType = expressionTypes.removeFirst();
        ExpressionType rightType = expressionTypes.removeFirst();

        if (hasIllegalExpressionTypes(leftType, rightType)) {
            return false;
        }

        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            return validateAddSubtractOperationExpressionTypes(leftType, rightType);
        } else if (operation instanceof MultiplyOperation) {
            return validateMultiplyExpressionTypes(leftType, rightType);
        }

        return false;
    }

    private boolean hasIllegalExpressionTypes(ExpressionType leftType, ExpressionType rightType) {
        return (leftType == ExpressionType.COLOR
                || rightType == ExpressionType.COLOR)
                || (leftType == ExpressionType.BOOL || rightType == ExpressionType.BOOL);
    }

    private boolean validateMultiplyExpressionTypes(ExpressionType leftType, ExpressionType rightType) {
        final boolean hasAtLeastOneScalar = leftType == ExpressionType.SCALAR || rightType == ExpressionType.SCALAR;
        final boolean hasValidExpressionType = (leftType == ExpressionType.PIXEL || rightType == ExpressionType.PIXEL)
                || (leftType == ExpressionType.PERCENTAGE || rightType == ExpressionType.PERCENTAGE)
                || (rightType == ExpressionType.SCALAR);

        if (hasAtLeastOneScalar && hasValidExpressionType) {
            if (leftType != ExpressionType.SCALAR) {
                expressionTypes.addFirst(leftType);
            } else {
                expressionTypes.addFirst(rightType);
            }

            return true;
        }
        return false;
    }

    private boolean validateAddSubtractOperationExpressionTypes(ExpressionType leftType, ExpressionType rightType) {
        final boolean isMatchingExpressionType = leftType == rightType;

        if (isMatchingExpressionType) {
            expressionTypes.addFirst(leftType);
            return true;
        }
        return false;
    }

    private ExpressionType determineOperationExpressionType(Operation operation) {
        if (operation.lhs instanceof PercentageLiteral || operation.rhs instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (operation.lhs instanceof PixelLiteral || operation.rhs instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (operation.lhs instanceof ScalarLiteral && operation.rhs instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (operation.lhs instanceof Operation) {
            return determineOperationExpressionType((Operation) operation.lhs);
        } else if (operation.rhs instanceof Operation) {
            return determineOperationExpressionType((Operation) operation.rhs);
        }

        return ExpressionType.UNDEFINED;
    }

    private ExpressionType getExpressionType(Expression expression) {
        final String variableName = ((VariableReference) expression).name;
        return getVariableExpressionType(variableName);
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
        for (HashMap<String, ExpressionType> currentScope : variableTypes) {
            ExpressionType expressionType = currentScope.get(variableName);

            final boolean expressionTypeDetermined = expressionType != null;

            if (expressionTypeDetermined) {
                return expressionType;
            }
        }

        return ExpressionType.UNDEFINED;
    }

    private ExpressionType determineExpressionType(Expression expression) {
        if (expression instanceof BoolLiteral || expression instanceof BooleanExpression || expression instanceof BooleanComparison) {
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
            if (expression instanceof Operation) {
                return determineOperationExpressionType((Operation) expression);
            }
            return ExpressionType.UNDEFINED;
        }
    }
}
