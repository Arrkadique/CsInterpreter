package AST;

public class ConditionNode  extends ExpressionNode{
    private ExpressionNode elseNode;
    private ExpressionNode leftNode;
    private ExpressionNode rightNode;

    public ConditionNode(ExpressionNode leftNode, ExpressionNode rightNode, ExpressionNode ifToken) {
        this.elseNode = ifToken;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public ExpressionNode getIfToken() {
        return elseNode;
    }

    public ExpressionNode getLeftNode() {
        return leftNode;
    }

    public ExpressionNode getRightNode() {
        return rightNode;
    }

}
