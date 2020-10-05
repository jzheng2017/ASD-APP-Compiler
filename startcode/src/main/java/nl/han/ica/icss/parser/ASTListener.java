package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
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
        Stylerule currentStyleRule = (Stylerule) currentContainer.peek();

        currentStyleRule.addChild(currentStyleDeclaration);
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
        super.enterIfClause(ctx);
    }

    @Override
    public void exitIfClause(ICSSParser.IfClauseContext ctx) {
        super.exitIfClause(ctx);
    }

    @Override
    public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
        super.enterElseClause(ctx);
    }

    @Override
    public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
        super.exitElseClause(ctx);
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
    public void enterPropertyValue(ICSSParser.PropertyValueContext ctx) {
        super.enterPropertyValue(ctx);
    }

    @Override
    public void exitPropertyValue(ICSSParser.PropertyValueContext ctx) {
        super.exitPropertyValue(ctx);
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
    public void enterGeneralValue(ICSSParser.GeneralValueContext ctx) {
        super.enterGeneralValue(ctx);
    }

    @Override
    public void exitGeneralValue(ICSSParser.GeneralValueContext ctx) {
        super.exitGeneralValue(ctx);
    }

    @Override
    public void enterHardcodedValue(ICSSParser.HardcodedValueContext ctx) {
        super.enterHardcodedValue(ctx);
    }

    @Override
    public void exitHardcodedValue(ICSSParser.HardcodedValueContext ctx) {
        super.exitHardcodedValue(ctx);
    }

    @Override
    public void enterDimensionSize(ICSSParser.DimensionSizeContext ctx) {
        super.enterDimensionSize(ctx);
    }

    @Override
    public void exitDimensionSize(ICSSParser.DimensionSizeContext ctx) {
        super.exitDimensionSize(ctx);
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
        super.enterVariableDeclaration(ctx);
    }

    @Override
    public void exitVariableDeclaration(ICSSParser.VariableDeclarationContext ctx) {
        super.exitVariableDeclaration(ctx);
    }

    @Override
    public void enterVariableIdentifier(ICSSParser.VariableIdentifierContext ctx) {
        super.enterVariableIdentifier(ctx);
    }

    @Override
    public void exitVariableIdentifier(ICSSParser.VariableIdentifierContext ctx) {
        super.exitVariableIdentifier(ctx);
    }

    @Override
    public void enterVariableValue(ICSSParser.VariableValueContext ctx) {
        super.enterVariableValue(ctx);
    }

    @Override
    public void exitVariableValue(ICSSParser.VariableValueContext ctx) {
        super.exitVariableValue(ctx);
    }

    public AST getAST() {
        return ast;
    }

}