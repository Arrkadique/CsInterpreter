package AST;

import Lexer.Token;

public class VariableNode  extends ExpressionNode{
    private Token variableType;
    private Token variable;
    private String data;

    public void setVariableType(Token variableType) {
        this.variableType = variableType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Token getVariableType() {
        return variableType;
    }

    public Token getVariable() {
        return variable;
    }

    public VariableNode(Token variableType, Token variable) {
        this.variable = variable;
        this.variableType = variableType;
    }
}
