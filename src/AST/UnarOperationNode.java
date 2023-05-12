package AST;

import Lexer.Token;

public class UnarOperationNode  extends ExpressionNode{
    private Token operator;
    private ExpressionNode operand;

    public Token getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }


    public UnarOperationNode(Token operator, ExpressionNode operand) {
        this.operator = operator;
        this.operand = operand;
    }
}
