package AST;

import Lexer.Token;

public class RealNumberNode  extends ExpressionNode{
    private Token number;

    public Token getNumber() {
        return number;
    }

    public void setNumber(Token number) {
        this.number = number;
    }

    public RealNumberNode(Token number) {
        this.number = number;
    }
}
