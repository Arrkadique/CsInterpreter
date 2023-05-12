package AST;

import Lexer.Token;

public class BinOperationNode  extends ExpressionNode{
    private Token operator;
    private ExpressionNode leftNode;
    private ExpressionNode rightNode;

    public ExpressionNode getLeftNode() {
        return leftNode;
    }

    public ExpressionNode getRightNode() {
        return rightNode;
    }

    public Token getOperator() {
        return operator;
    }

    public BinOperationNode(Token operator, ExpressionNode leftNode, ExpressionNode rightNode) {
        this.operator = operator;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }
}
