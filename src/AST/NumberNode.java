package AST;

import Lexer.Token;

public class NumberNode  extends ExpressionNode{
    private Token number;

    public Token getNumber() {
        return number;
    }

    public void setNumber(Token number) {
        this.number = number;
    }

    public NumberNode(Token number) {
        this.number = number;
    }
}
