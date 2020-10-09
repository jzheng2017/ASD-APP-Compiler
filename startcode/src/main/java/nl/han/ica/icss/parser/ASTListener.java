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
        Stylerule currentStyleRule = (Stylerule) currentContainer.pop();

        Stylesheet stylesheet = (Stylesheet) currentContainer.peek();

        stylesheet.addChild(currentStyleRule);
    }

    @Override
    public void enterStyleDeclaration(ICSSParser.StyleDeclarationContext ctx) {
        currentContainer.push(new Declaration());
    }

    @Override
    public void exitStyleDeclaration(ICSSParser.StyleDeclarationContext ctx) {
        Declaration currentStyleDeclaration = (Declaration) currentContainer.pop();
        if (currentContainer.peek() instanceof Stylerule) {
            Stylerule currentStyleRule = (Stylerule) currentContainer.peek();
            currentStyleRule.addChild(currentStyleDeclaration);
        } else if (currentContainer.peek() instanceof IfClause) {
            IfClause currentIfClause = (IfClause) currentContainer.peek();
            currentIfClause.addChild(currentStyleDeclaration);
        } else if (currentContainer.peek() instanceof ElseClause) {
            ElseClause currentElseClause = (ElseClause) currentContainer.peek();
            currentElseClause.addChild(currentStyleDeclaration);
        }
    }

    @Override
    public void enterPropertyIdentifier(ICSSParser.PropertyIdentifierContext ctx) {
        PropertyName propertyName = new PropertyName(ctx.getChild(0).getText());

        currentContainer.push(propertyName);
    }

    @Override
    public void exitPropertyIdentifier(ICSSParser.PropertyIdentifierContext ctx) {
        PropertyName currentPropertyName = (PropertyName) currentContainer.pop();

        Declaration currentStyleDeclaration = (Declaration) currentContainer.peek();

        currentStyleDeclaration.addChild(currentPropertyName);
    }

    @Override
    public void enterIfClause(ICSSParser.IfClauseContext ctx) {
        currentContainer.push(new IfClause());
    }

    @Override
    public void exitIfClause(ICSSParser.IfClauseContext ctx) {
        IfClause currentIfClause = (IfClause) currentContainer.pop();
        if (currentContainer.peek() instanceof Stylerule) {
            Stylerule currentStyleRule = (Stylerule) currentContainer.peek();
            currentStyleRule.addChild(currentIfClause);
        } else if (currentContainer.peek() instanceof IfClause) {
            IfClause parentIfClause = (IfClause) currentContainer.peek();
            parentIfClause.addChild(currentIfClause);
        }
    }

    @Override
    public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
        currentContainer.push(new ElseClause());
    }

    @Override
    public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
        ElseClause currentElseClause = (ElseClause) currentContainer.pop();
        IfClause currentIfClause = (IfClause) currentContainer.peek();

        currentIfClause.addChild(currentElseClause);
    }

    @Override
    public void exitSubCalculation(ICSSParser.SubCalculationContext ctx) {
        final boolean isExpression = ctx.getChildCount() == 0;

        if (isExpression) {
            Expression expression = this.determineValue(ctx.getChild(0).getText());
            currentContainer.push(expression);
        }

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
        Expression parentOperation = (Expression) currentContainer.pop();

        if (currentContainer.peek() instanceof Declaration) {
            Declaration currentDeclaration = (Declaration) currentContainer.peek();

            currentDeclaration.addChild(parentOperation);
        } else if (currentContainer.peek() instanceof VariableAssignment) {
            VariableAssignment currentVariableAssignment = (VariableAssignment) currentContainer.peek();

            currentVariableAssignment.addChild(parentOperation);
        }
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

        Literal currentLiteral = (Literal) currentContainer.pop();

        if (currentContainer.peek() instanceof Expression) {
            Expression currentExpression = (Expression) currentContainer.peek();

            currentExpression.addChild(currentLiteral);
        } else if (currentContainer.peek() instanceof Declaration) {
            Declaration currentDeclaration = (Declaration) currentContainer.peek();

            currentDeclaration.addChild(currentLiteral);
        }
    }

    @Override
    public void enterTagSelector(ICSSParser.TagSelectorContext ctx) {
        final String selectorText = ctx.getChild(0).getText();

        currentContainer.push(new TagSelector(selectorText));
    }

    @Override
    public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
        TagSelector currentSelector = (TagSelector) currentContainer.pop();
        Stylerule currentStyleRule = (Stylerule) currentContainer.peek();

        currentStyleRule.addChild(currentSelector);
    }

    @Override
    public void enterClassSelector(ICSSParser.ClassSelectorContext ctx) {
        final String selectorText = ctx.getChild(0).getText();

        currentContainer.push(new ClassSelector(selectorText));

    }

    @Override
    public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
        ClassSelector currentSelector = (ClassSelector) currentContainer.pop();
        Stylerule currentStyleRule = (Stylerule) currentContainer.peek();

        currentStyleRule.addChild(currentSelector);
    }

    @Override
    public void enterIdSelector(ICSSParser.IdSelectorContext ctx) {
        final String selectorText = ctx.getChild(0).getText();
        currentContainer.push(new IdSelector(selectorText));
    }

    @Override
    public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
        IdSelector currentSelector = (IdSelector) currentContainer.pop();
        Stylerule currentStyleRule = (Stylerule) currentContainer.peek();

        currentStyleRule.addChild(currentSelector);
    }

    @Override
    public void enterVariableDeclaration(ICSSParser.VariableDeclarationContext ctx) {
        currentContainer.push(new VariableAssignment());
    }

    @Override
    public void exitVariableDeclaration(ICSSParser.VariableDeclarationContext ctx) {
        VariableAssignment currentVariableDeclaration = (VariableAssignment) currentContainer.pop();

        if (currentContainer.peek() instanceof Stylesheet) {
            Stylesheet currentStylesheet = (Stylesheet) currentContainer.peek();
            currentStylesheet.addChild(currentVariableDeclaration);
        } else if (currentContainer.peek() instanceof Stylerule) {
            Stylerule currentStyleRule = (Stylerule) currentContainer.peek();
            currentStyleRule.addChild(currentVariableDeclaration);
        } else if (currentContainer.peek() instanceof IfClause) {
            IfClause currentIfClause = (IfClause) currentContainer.peek();
            currentIfClause.addChild(currentVariableDeclaration);
        } else if (currentContainer.peek() instanceof ElseClause) {
            ElseClause currentElseClause = (ElseClause) currentContainer.peek();
            currentElseClause.addChild(currentVariableDeclaration);
        }
    }

    @Override
    public void enterVariableIdentifier(ICSSParser.VariableIdentifierContext ctx) {
        final String variableName = ctx.getChild(0).getText();

        currentContainer.push(new VariableReference(variableName));
    }

    @Override
    public void exitVariableIdentifier(ICSSParser.VariableIdentifierContext ctx) {
        VariableReference currentVariableIdentifier = (VariableReference) currentContainer.pop();
        VariableAssignment currentVariableDeclaration = (VariableAssignment) currentContainer.peek();

        currentVariableDeclaration.addChild(currentVariableIdentifier);
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

        VariableReference currentVariableReference = (VariableReference) currentContainer.pop();
        if (currentContainer.peek() instanceof Declaration) {
            Declaration currentDeclaration = (Declaration) currentContainer.peek();
            currentDeclaration.addChild(currentVariableReference);
        } else if (currentContainer.peek() instanceof IfClause) {
            IfClause currentIfClause = (IfClause) currentContainer.peek();
            currentIfClause.addChild(currentVariableReference);
        } else if (currentContainer.peek() instanceof ElseClause) {
            ElseClause currentElseClause = (ElseClause) currentContainer.peek();
            currentElseClause.addChild(currentVariableReference);
        } else if (currentContainer.peek() instanceof VariableAssignment) {
            VariableAssignment currentVariableAssignment = (VariableAssignment) currentContainer.peek();
            currentVariableAssignment.addChild(currentVariableReference);
        }
    }

    @Override
    public void enterVariableHardcodedValue(ICSSParser.VariableHardcodedValueContext ctx) {
        final String variableValue = ctx.getChild(0).getText();
        this.determineValueAndPushToContainer(variableValue);
    }

    @Override
    public void exitVariableHardcodedValue(ICSSParser.VariableHardcodedValueContext ctx) {
        if (currentContainer.peek() instanceof Expression) {
            Expression currentVariableValue = (Expression) currentContainer.pop();
            VariableAssignment currentVariableDeclaration = (VariableAssignment) currentContainer.peek();

            currentVariableDeclaration.addChild(currentVariableValue);
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

        throw new IllegalArgumentException("No matching literal found!");
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