package AST;

import Lexer.Token;
import Lexer.TokenTypes;

import java.util.List;

public class ArrayNode extends ExpressionNode{
    private ExpressionNode arrayName;
    private int arraySize;
    private List<ExpressionNode> arrayData;

    public ArrayNode(ExpressionNode arrayName, int arraySize, List<ExpressionNode> arrayData) {
        this.arrayName = arrayName;
        this.arraySize = arraySize;
        this.arrayData = arrayData;
    }

    public ExpressionNode getArrayName() {
        return arrayName;
    }

    public List<ExpressionNode> getArrayData() {
        return arrayData;
    }

    public int getArraySize() {
        return arraySize;
    }

    public void setByIndex(int index, String data) {
        if(index >= arrayData.size()){
            arrayData.add( new NumberNode(new Token(data, TokenTypes.NUMBER, 0)));
            return;
        }
        if (arrayData.get(index).getClass() == NumberNode.class) {
            NumberNode numberNode = (NumberNode) arrayData.get(index);
            numberNode.setNumber(new Token(data, TokenTypes.NUMBER, 0));
            arrayData.set(index, numberNode);
            return;
        }
        if (arrayData.get(index).getClass() == RealNumberNode.class) {
            RealNumberNode numberNode = (RealNumberNode) arrayData.get(index);
            numberNode.setNumber(new Token(data, TokenTypes.NUMBER, 0));
            arrayData.set(index, numberNode);
        }
    }

    public ExpressionNode getByIndex(int index){
        return arrayData.get(index);
    }
}

