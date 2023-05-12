package AST;

public class ElseNode  extends ExpressionNode{
    private final ExpressionNode elseCodeNode;

    public ElseNode(ExpressionNode elseCodeNode) {
        this.elseCodeNode = elseCodeNode;
    }

    public ExpressionNode getElseCodeNode() {
        return elseCodeNode;
    }
}
