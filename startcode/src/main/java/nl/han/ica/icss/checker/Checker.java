package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.*;


public class Checker {
    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        checkChildren(stylesheet);
    }

    private void checkChildren(ASTNode currentNode) {
        List<ASTNode> children = currentNode.getChildren();

        generatingScope(currentNode);
        checkingScope(currentNode);
        checkingSemantic(currentNode);
        removingScope();

        children.forEach(this::checkChildren);
    }

    private void removingScope() {
        variableTypes.removeLast();
    }


    private void generatingScope(ASTNode currentNode) {
        if (currentNode instanceof Stylesheet
                || currentNode instanceof Stylerule
                || currentNode instanceof IfClause
                || currentNode instanceof ElseClause) {
            HashMap<String, ExpressionType> currentScope = new HashMap<>();
            variableTypes.add(currentScope);
        }

        if (currentNode instanceof VariableAssignment) {
            HashMap<String, ExpressionType> currentScope = variableTypes.getLast();
            String variableName = ((VariableAssignment) currentNode).name.name;
            ExpressionType variableExpressionType = determineExpressionType(((VariableAssignment) currentNode).expression);
            currentScope.put(variableName, variableExpressionType);
        }
    }

    private void checkingSemantic(ASTNode currentNode) {
        if (currentNode instanceof Declaration) {
            final String propertyName = ((Declaration) currentNode).property.name;

            if (isPropertyIllegal(propertyName)) {
                currentNode.setError(String.format("%s is not a legal property name!", propertyName));
            }

            if (!isValueTypeAllowed(currentNode)) {
                currentNode.setError(String.format("%s has an illegal value type!", propertyName));
            }
        }
    }

    private boolean isPropertyIllegal(String propertyName) {
        List<String> allowedProperties = new ArrayList<>(Arrays.asList("background-color", "color", "width", "height"));

        return !allowedProperties.contains(propertyName);
    }

    private boolean isValueTypeAllowed(ASTNode currentNode) {
        final String propertyName = ((Declaration) currentNode).property.name;
        final Expression expression = ((Declaration) currentNode).expression;
        ExpressionType propertyExpressionType = determineExpressionType(((Declaration) currentNode).expression);

        if (propertyName.equals("background-color") || propertyName.equals("color")) {
            if (propertyExpressionType == ExpressionType.UNDEFINED) {
                if (expression instanceof VariableReference) {
                    final String variableName = ((VariableReference) expression).name;
                    propertyExpressionType = getVariableExpressionType(variableName);
                }
            }

            return propertyExpressionType == ExpressionType.COLOR;
        } else {
            return propertyExpressionType == ExpressionType.PERCENTAGE
                    || propertyExpressionType == ExpressionType.PIXEL
                    || propertyExpressionType == ExpressionType.UNDEFINED;
        }


//        throw new IllegalArgumentException("Something went wrong with checking the value type!");
    }


    private void checkingScope(ASTNode currentNode) {
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
        } else {
            return ExpressionType.UNDEFINED;
        }

    }
}
