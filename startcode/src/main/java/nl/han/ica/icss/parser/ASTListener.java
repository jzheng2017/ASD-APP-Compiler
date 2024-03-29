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
import org.antlr.v4.runtime.misc.ParseCancellationException;

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

    public AST getAST() {
        return ast;
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

        if (currentContainer.peek() instanceof BooleanComparison) {
            currentContainer.push(currentVariableReference);
        } else {
            currentContainer.peek().addChild(currentVariableReference);
        }
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
    public void exitSubCalculation(ICSSParser.SubCalculationContext ctx) {
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
    public void enterBooleanExpressions(ICSSParser.BooleanExpressionsContext ctx) {
        final boolean isBooleanComparison = ctx.getChildCount() == 3;

        if (isBooleanComparison) {
            currentContainer.push(new BooleanComparison(determineComparisonOperator(ctx.getChild(1).getText())));
        }
    }

    @Override
    public void exitBooleanExpressions(ICSSParser.BooleanExpressionsContext ctx) {
        ASTNode node = currentContainer.pop();
        currentContainer.peek().addChild(node);
    }

    @Override
    public void enterBooleanExpression(ICSSParser.BooleanExpressionContext ctx) {
        final boolean isNegated = ctx.getChildCount() == 2;
        boolean isComparison;
        
        if (ctx.getChildCount() == 0) throw new ParseCancellationException("Incomplete expression");

        if (isNegated) {
            isComparison = ctx.getChild(1).getChildCount() >= 3 && ctx.getChild(1) instanceof ICSSParser.EqualityContext;
        } else {
            isComparison = ctx.getChild(0).getChildCount() >= 3 && ctx.getChild(0) instanceof ICSSParser.EqualityContext;
        }

        if (isComparison) {
            constructBooleanComparison(ctx, isNegated);
        } else {
            constructBooleanExpression(ctx, isNegated);
        }
    }

    @Override
    public void exitBooleanExpression(ICSSParser.BooleanExpressionContext ctx) {
        ASTNode right = currentContainer.pop();

        if (currentContainer.peek() instanceof BooleanExpression
                || currentContainer.peek() instanceof Literal
                || currentContainer.peek() instanceof VariableReference
                || currentContainer.peek() instanceof Operation) {
            ASTNode left = currentContainer.pop();
            currentContainer.peek().addChild(left);
            currentContainer.peek().addChild(right);
            return;
        }

        currentContainer.push(right);
    }

    @Override
    public void enterEquality(ICSSParser.EqualityContext ctx) {
        if (ctx.getChildCount() < 3) return;

        BooleanComparison booleanComparison = (BooleanComparison) currentContainer.peek();
        ComparisonOperator operator;
        final boolean hasAtLeastOneNegation = ctx.getChildCount() > 3;

        if (hasAtLeastOneNegation) {
            final boolean leftExpressionNegated = ctx.getChild(0).getText().equals("!");

            if (leftExpressionNegated) {
                operator = determineComparisonOperator(ctx.getChild(2).getText());
            } else {
                operator = determineComparisonOperator(ctx.getChild(1).getText());
            }
        } else {
            operator = determineComparisonOperator(ctx.getChild(1).getText());
        }

        booleanComparison.setOperator(operator);
    }

    @Override
    public void exitEquality(ICSSParser.EqualityContext ctx) {
        if (ctx.getChildCount() < 3) return;

        Expression right = (Expression) currentContainer.pop();
        Expression left = (Expression) currentContainer.pop();

        boolean leftNegated = ctx.getChild(0).getText().equals("!");
        boolean rightNegated;

        if (leftNegated) {
            rightNegated = ctx.getChild(3).getText().equals("!");
            currentContainer.push(new BooleanExpression(true, left));
        } else {
            rightNegated = ctx.getChild(2).getText().equals("!");
            currentContainer.push(left);
        }

        if (rightNegated) {
            currentContainer.push(new BooleanExpression(true, right));
        } else {
            currentContainer.push(right);
        }
    }

    private void constructBooleanComparison(ICSSParser.BooleanExpressionContext ctx, boolean isNegated) {
        if (ctx.getText().startsWith("!!!"))
            throw new ParseCancellationException("Not a valid boolean expression!");

        currentContainer.push(new BooleanComparison(isNegated));
    }

    private void constructBooleanExpression(ICSSParser.BooleanExpressionContext ctx, boolean isNegated) {
        if (isNegated) {
            constructNegatedBooleanExpression(ctx);
        } else {
            constructBooleanExpression(ctx);
        }
    }

    private void constructBooleanExpression(ICSSParser.BooleanExpressionContext ctx) {
        Expression expression;
        expression = determineValue(ctx.getChild(0).getText());
        if (expression instanceof BoolLiteral) {
            currentContainer.push(expression);
        }
    }

    private void constructNegatedBooleanExpression(ICSSParser.BooleanExpressionContext ctx) {
        Expression expression;
        expression = determineValue(ctx.getChild(1).getText());
        final boolean isInvalidExpression = !(expression instanceof BoolLiteral || expression instanceof VariableReference) || ctx.getText().startsWith("!!");

        if (isInvalidExpression)
            throw new ParseCancellationException("Not a valid boolean expression!");

        final boolean isNotVariableReference = !(expression instanceof VariableReference);
        if (isNotVariableReference) {
            currentContainer.push(new BooleanExpression(true, expression));
        } else {
            currentContainer.push(new BooleanExpression(true));
        }
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
            case "&&":
                return ComparisonOperator.AND;
            case "||":
                return ComparisonOperator.OR;
            default:
                return null;
        }
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
        try {
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
        } catch (Exception ex) {
            throw new ParseCancellationException(String.format("Unexpected value: %s ", value));
        }

        throw new ParseCancellationException(String.format("Unrecognizable value: %s", value));
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
}