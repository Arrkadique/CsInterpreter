package AST;

import Lexer.Token;

public class BooleanNode  extends ExpressionNode{
    private Token operator;
    private ExpressionNode leftNode;
    private ExpressionNode rightNode;

    public BooleanNode(Token ifToken, ExpressionNode leftNode, ExpressionNode rightNode) {
        this.operator = ifToken;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public Token getOperator() {
        return operator;
    }


    public ExpressionNode getLeftNode() {
        return leftNode;
    }

    public ExpressionNode getRightNode() {
        return rightNode;
    }

}
