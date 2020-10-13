package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
    private IHANStack<ASTNode> currentContainer;

    public ASTListener() {
        ast = new AST();
        currentContainer = new HANStack<>();
    }

    @Override
    public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
        currentContainer.push(new Stylesheet());
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        if (currentContainer.peek() instanceof Stylesheet) {
            this.ast.setRoot((Stylesheet) currentContainer.pop());
        } else {
            throw new IllegalStateException("Incorrect parse tree");
        }
    }

    @Override
    public void enterStylerule(ICSSParser.StyleruleContext ctx) {
        currentContainer.push(new Stylerule());
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        ASTNode currentStyleRule = currentContainer.pop();

        ASTNode stylesheet = currentContainer.peek();

        stylesheet.addChild(currentStyleRule);
    }

    @Override
    public void enterStyleDeclaration(ICSSParser.StyleDeclarationContext ctx) {
        currentContainer.push(new Declaration());
    }

    @Override
    public void exitStyleDeclaration(ICSSParser.StyleDeclarationContext ctx) {
        ASTNode currentStyleDeclaration = currentContainer.pop();
        currentContainer.peek().addChild(currentStyleDeclaration);
    }

    @Override
    public void enterPropertyIdentifier(ICSSParser.PropertyIdentifierContext ctx) {
        PropertyName propertyName = new PropertyName(ctx.getChild(0).getText());

        currentContainer.push(propertyName);
    }

    @Override
    public void exitPropertyIdentifier(ICSSParser.PropertyIdentifierContext ctx) {
        ASTNode currentPropertyName = currentContainer.pop();

        ASTNode currentStyleDeclaration = currentContainer.peek();

        currentStyleDeclaration.addChild(currentPropertyName);
    }

    @Override
    public void enterIfClause(ICSSParser.IfClauseContext ctx) {
        currentContainer.push(new IfClause());
    }

    @Override
    public void exitIfClause(ICSSParser.IfClauseContext ctx) {
        ASTNode currentIfClause = currentContainer.pop();
        currentContainer.peek().addChild(currentIfClause);
    }

    @Override
    public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
        currentContainer.push(new ElseClause());
    }

    @Override
    public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
        ASTNode currentElseClause = currentContainer.pop();
        ASTNode currentIfClause = currentContainer.peek();

        currentIfClause.addChild(currentElseClause);
    }

    @Override
    public void exitSubCalculation(ICSSParser.SubCalculationContext ctx) {
//        final boolean isExpression = ctx.getChildCount() == 0;
//
//        if (isExpression) {
//            Expression expression = this.determineValue(ctx.getChild(0).getText());
//            currentContainer.push(expression);
//        }

        final boolean isOperation = ctx.getChildCount() == 3;

        if (isOperation) {
            Operation operation = this.determineOperator(ctx.getChild(1).getText());

            operation.rhs = (Expression) currentContainer.pop();
            operation.lhs = (Expression) currentContainer.pop();

            currentContainer.push(operation);
        }
    }

    @Override
    public void exitCalculation(ICSSParser.CalculationContext ctx) {
        ASTNode parentOperation = currentContainer.pop();

        currentContainer.peek().addChild(parentOperation);
    }


    @Override
    public void enterHardcodedValue(ICSSParser.HardcodedValueContext ctx) {
        final String value = ctx.getChild(0).getText();

        this.determineValueAndPushToContainer(value);
    }

    @Override
    public void exitHardcodedValue(ICSSParser.HardcodedValueContext ctx) {
        if (isChildOfSubCalculation(ctx)) {
            return;
        }

        ASTNode currentLiteral = currentContainer.pop();

        currentContainer.peek().addChild(currentLiteral);
    }

    @Override
    public void enterTagSelector(ICSSParser.TagSelectorContext ctx) {
        final String selectorText = ctx.getChild(0).getText();

        currentContainer.push(new TagSelector(selectorText));
    }

    @Override
    public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
        ASTNode currentTagSelector = currentContainer.pop();
        ASTNode currentStyleRule = currentContainer.peek();

        currentStyleRule.addChild(currentTagSelector);
    }

    @Override
    public void enterClassSelector(ICSSParser.ClassSelectorContext ctx) {
        final String selectorText = ctx.getChild(0).getText();

        currentContainer.push(new ClassSelector(selectorText));

    }

    @Override
    public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
        ASTNode currentClassSelector = currentContainer.pop();
        ASTNode currentStyleRule = currentContainer.peek();

        currentStyleRule.addChild(currentClassSelector);
    }

    @Override
    public void enterIdSelector(ICSSParser.IdSelectorContext ctx) {
        final String selectorText = ctx.getChild(0).getText();
        currentContainer.push(new IdSelector(selectorText));
    }

    @Override
    public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
        ASTNode currentIdSelector = currentContainer.pop();
        ASTNode currentStyleRule = currentContainer.peek();

        currentStyleRule.addChild(currentIdSelector);
    }

    @Override
    public void enterVariableDeclaration(ICSSParser.VariableDeclarationContext ctx) {
        currentContainer.push(new VariableAssignment());
    }

    @Override
    public void exitVariableDeclaration(ICSSParser.VariableDeclarationContext ctx) {
        ASTNode currentVariableAssignment = currentContainer.pop();

        currentContainer.peek().addChild(currentVariableAssignment);
    }

    @Override
    public void enterVariableIdentifier(ICSSParser.VariableIdentifierContext ctx) {
        final String variableName = ctx.getChild(0).getText();

        currentContainer.push(new VariableReference(variableName));
    }

    @Override
    public void exitVariableIdentifier(ICSSParser.VariableIdentifierContext ctx) {
        ASTNode currentVariableIdentifier = currentContainer.pop();

        currentContainer.peek().addChild(currentVariableIdentifier);
    }

    @Override
    public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
        final String variableName = ctx.getChild(0).getText();

        currentContainer.push(new VariableReference(variableName));
    }

    @Override
    public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
        if (isChildOfSubCalculation(ctx)) {
            return;
        }

        ASTNode currentVariableReference = currentContainer.pop();

        currentContainer.peek().addChild(currentVariableReference);
    }

    @Override
    public void enterVariableHardcodedValue(ICSSParser.VariableHardcodedValueContext ctx) {
        final String variableValue = ctx.getChild(0).getText();
        this.determineValueAndPushToContainer(variableValue);
    }

    @Override
    public void exitVariableHardcodedValue(ICSSParser.VariableHardcodedValueContext ctx) {
        ASTNode currentVariableValue = currentContainer.pop();

        currentContainer.peek().addChild(currentVariableValue);
    }

    @Override
    public void exitBooleanExpression(ICSSParser.BooleanExpressionContext ctx) {
        final boolean isNegated = ctx.getChildCount() == 2;
        boolean isComparison;

        if (isNegated) {
            isComparison = ctx.getChild(1).getChildCount() > 1;
            if (isComparison) {
                String operator = ctx.getChild(1).getChild(1).getText();

                Expression rightExpression = (Expression)currentContainer.pop();
                Expression leftExpression = (Expression)currentContainer.pop();
                ComparisonOperator comparisonOperator = determineComparisonOperator(operator);
                currentContainer.push(new BooleanComparison(true, comparisonOperator, leftExpression, rightExpression));
            } else {
                Expression expression = determineValue(ctx.getChild(1).getText());
                currentContainer.push(new BooleanExpression(true, expression));
            }
        } else {
            isComparison = ctx.getChild(0).getChildCount() > 1;
            if (isComparison) {
                String operator = ctx.getChild(0).getChild(1).getText();

                Expression rightExpression = (Expression)currentContainer.pop();
                Expression leftExpression = (Expression)currentContainer.pop();

                ComparisonOperator comparisonOperator = determineComparisonOperator(operator);
                currentContainer.push(new BooleanComparison(false, comparisonOperator, leftExpression, rightExpression));
            } else {
                Expression expression = determineValue(ctx.getChild(0).getText());
                currentContainer.push(new BooleanExpression(false, expression));
            }
        }

        ASTNode currentBooleanExpression = currentContainer.pop();

        currentContainer.peek().addChild(currentBooleanExpression);
    }

    private ComparisonOperator determineComparisonOperator(String operator) {
        switch (operator.strip()) {
            case "<":
                return ComparisonOperator.LT;
            case "<=":
                return ComparisonOperator.LET;
            case "==":
                return ComparisonOperator.EQ;
            case "!=":
                return ComparisonOperator.NQ;
            case ">=":
                return ComparisonOperator.GET;
            case ">":
                return ComparisonOperator.GT;
            default:
                return null;
        }
    }

    public AST getAST() {
        return ast;
    }

    private void determineOperatorAndPushToContainer(final String operator) {
        currentContainer.push(determineOperator(operator));
    }

    private Operation determineOperator(final String operator) {
        if (operator.equals("*")) {
            return new MultiplyOperation();
        } else if (operator.equals("+")) {
            return new AddOperation();
        } else if (operator.equals("-")) {
            return new SubtractOperation();
        }

        throw new IllegalArgumentException("No matching operator found!");
    }

    private void determineValueAndPushToContainer(final String value) {
        currentContainer.push(determineValue(value));
    }

    private Expression determineValue(final String value) {
        if (value.startsWith("#")) {
            return new ColorLiteral(value);
        } else if (value.endsWith("%")) {
            return new PercentageLiteral(value);
        } else if (value.endsWith("px")) {
            return new PixelLiteral(value);
        } else if (value.equals("TRUE") || value.equals("FALSE")) {
            return new BoolLiteral(value);
        } else if (isPositiveNumber(value)) {
            return new ScalarLiteral(value);
        } else if (isAllCharacters(value)) {
            return new VariableReference(value);
        }
        return null;
    }

    public boolean isAllCharacters(String value) {
        char[] chars = value.toCharArray();

        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    private boolean isPositiveNumber(String value) {
        try {
            if (Integer.parseInt(value) >= 0) {
                return true;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return false;
    }

    private boolean isChildOfSubCalculation(ICSSParser.VariableReferenceContext ctx) {
        return ctx.parent.parent instanceof ICSSParser.SubCalculationContext;
    }

    private boolean isChildOfSubCalculation(ICSSParser.HardcodedValueContext ctx) {
        return ctx.parent.parent instanceof ICSSParser.SubCalculationContext;
    }

    private boolean isChildOfSubCalculation(ICSSParser.HardcodedPropertyValueContext ctx) {
        return ctx.parent.parent instanceof ICSSParser.SubCalculationContext;
    }
}