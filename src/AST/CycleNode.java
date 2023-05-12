package AST;

import Lexer.Token;

public class CycleNode  extends ExpressionNode{
    private final Token cycleOperator;
    private final ExpressionNode conditionNode;
    private final ExpressionNode bodyNode;

    public CycleNode(Token cycleOperator, ExpressionNode conditionNode, ExpressionNode bodyNode) {
        this.cycleOperator = cycleOperator;
        this.conditionNode = conditionNode;
        this.bodyNode = bodyNode;
    }

    public Token getCycleOperator() {
        return cycleOperator;
    }

    public ExpressionNode getConditionNode() {
        return conditionNode;
    }

    public ExpressionNode getBodyNode() {
        return bodyNode;
    }
}
