package AST;

import Lexer.Token;

public class ConstNode  extends ExpressionNode{
    private Token constOperator;

    public ConstNode(Token constOperator) {
        this.constOperator = constOperator;
    }

    public Token getConstOperator() {
        return constOperator;
    }
}
