package AST;

import java.util.ArrayList;
import java.util.List;

public class StatementsNode extends ExpressionNode {
    private List<ExpressionNode> code = new ArrayList<>();

    public List<ExpressionNode> getCode() {
        return code;
    }

    public void addNode(ExpressionNode expressionNode){
        this.code.add(expressionNode);
    }
}
