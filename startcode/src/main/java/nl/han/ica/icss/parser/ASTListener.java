package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
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
    public void enterCondition(ICSSParser.ConditionContext ctx) {
        super.enterCondition(ctx);
    }

    @Override
    public void exitCondition(ICSSParser.ConditionContext ctx) {
        super.exitCondition(ctx);
    }

    @Override
    public void enterConditionalBody(ICSSParser.ConditionalBodyContext ctx) {
        super.enterConditionalBody(ctx);
    }

    @Override
    public void exitConditionalBody(ICSSParser.ConditionalBodyContext ctx) {
        super.exitConditionalBody(ctx);
    }

    @Override
    public void enterHardcodedPropertyValue(ICSSParser.HardcodedPropertyValueContext ctx) {
        final String propertyValue = ctx.getChild(0).getText();

        this.determineValueAndPushToContainer(propertyValue);
    }

    @Override
    public void exitHardcodedPropertyValue(ICSSParser.HardcodedPropertyValueContext ctx) {
        Literal currentLiteral = (Literal) currentContainer.pop();
        Declaration currentDeclaration = (Declaration) currentContainer.peek();

        currentDeclaration.addChild(currentLiteral);
    }

    @Override
    public void enterCalculation(ICSSParser.CalculationContext ctx) {
        super.enterCalculation(ctx);
    }

    @Override
    public void exitCalculation(ICSSParser.CalculationContext ctx) {
        super.exitCalculation(ctx);
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
        Stylesheet currentStylesheet = (Stylesheet) currentContainer.peek();

        currentStylesheet.addChild(currentVariableDeclaration);
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
        VariableReference currentVariableIdentifier = (VariableReference) currentContainer.pop();
        if (currentContainer.peek() instanceof Declaration) {
            Declaration currentDeclaration = (Declaration) currentContainer.peek();
            currentDeclaration.addChild(currentVariableIdentifier);
        } else if (currentContainer.peek() instanceof IfClause) {
            IfClause currentIfClause = (IfClause) currentContainer.peek();
            currentIfClause.addChild(currentVariableIdentifier);
        } else if (currentContainer.peek() instanceof ElseClause) {
            ElseClause currentElseClause = (ElseClause) currentContainer.peek();
            currentElseClause.addChild(currentVariableIdentifier);
        } else if (currentContainer.peek() instanceof VariableAssignment) {
            VariableAssignment currentVariableAssignment = (VariableAssignment) currentContainer.peek();
            currentVariableAssignment.addChild(currentVariableIdentifier);
        }
    }


    @Override
    public void enterVariableValue(ICSSParser.VariableValueContext ctx) {
        final String variableValue = ctx.getChild(0).getText();
        this.determineValueAndPushToContainer(variableValue);
    }

    @Override
    public void exitVariableValue(ICSSParser.VariableValueContext ctx) {
        if (currentContainer.peek() instanceof Literal) {
            Literal currentVariableValue = (Literal) currentContainer.pop();
            VariableAssignment currentVariableDeclaration = (VariableAssignment) currentContainer.peek();

            currentVariableDeclaration.addChild(currentVariableValue);
        }
    }

    public AST getAST() {
        return ast;
    }

    private void determineValueAndPushToContainer(final String value) {
        if (value.startsWith("#")) {
            currentContainer.push(new ColorLiteral(value));
        } else if (value.endsWith("%")) {
            currentContainer.push(new PercentageLiteral(value));
        } else if (value.endsWith("px")) {
            currentContainer.push(new PixelLiteral(value));
        } else if (value.equals("TRUE") || value.equals("FALSE")) {
            currentContainer.push(new BoolLiteral(value));
        } else if (isPositiveNumber(value)) {
            currentContainer.push(new ScalarLiteral(value));
        }
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
}