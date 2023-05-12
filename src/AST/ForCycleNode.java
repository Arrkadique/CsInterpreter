package AST;

public class ForCycleNode  extends ExpressionNode{
    private ExpressionNode conditionNode1;
    private ExpressionNode conditionNode2;
    private ExpressionNode conditionNode3;
    private ExpressionNode bodyNode;

    public ForCycleNode(ExpressionNode conditionNode1,
                        ExpressionNode conditionNode2, ExpressionNode conditionNode3, ExpressionNode bodyNode) {
        this.conditionNode1 = conditionNode1;
        this.conditionNode2 = conditionNode2;
        this.conditionNode3 = conditionNode3;
        this.bodyNode = bodyNode;
    }

    public ExpressionNode getConditionNode1() {
        return conditionNode1;
    }

    public ExpressionNode getConditionNode2() {
        return conditionNode2;
    }

    public ExpressionNode getConditionNode3() {
        return conditionNode3;
    }

    public ExpressionNode getBodyNode() {
        return bodyNode;
    }
}
