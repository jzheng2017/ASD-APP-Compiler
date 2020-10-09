package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;

import java.util.List;

public class Generator {
    StringBuilder css = new StringBuilder();

    public String generate(AST ast) {
        List<ASTNode> stylesheetChildren = ast.root.getChildren();

        stylesheetChildren.stream()
                .filter(c -> c instanceof Stylerule)
                .map(c -> (Stylerule) c)
                .forEach(this::generateStyleRule);

        return css.toString();
    }

    private void generateStyleRule(Stylerule stylerule) {
        final String selector = stylerule.selectors.get(0).toString();
        List<ASTNode> children = stylerule.getChildren();

        css.append(selector)
                .append(" {")
                .append(System.lineSeparator());

        generateDeclarations(children);

        css.append(System.lineSeparator())
                .append("}")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
    }

    private void generateDeclarations(List<ASTNode> children) {
        String newLine = "";

        for (ASTNode child : children) {
            if (child instanceof Declaration) {
                final String propertyName = ((Declaration) child).property.name;
                final Expression expression = ((Declaration) child).expression;
                css.append(newLine)
                        .append("\t")
                        .append(propertyName)
                        .append(": ")
                        .append(expression);
                newLine = System.lineSeparator();
            }
        }
    }

}
