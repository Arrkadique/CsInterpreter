package AST;

import Lexer.Token;

public class IndexArrayNode  extends ExpressionNode{
    private ExpressionNode index;
    private Token arrayName;

    public IndexArrayNode(ExpressionNode index, Token arrayName) {
        this.index = index;
        this.arrayName = arrayName;
    }

    public ExpressionNode getIndex() {
        return index;
    }

    public Token getArrayName() {
        return arrayName;
    }
}
