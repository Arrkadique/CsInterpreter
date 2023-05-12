package Lexer;

import AST.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }

    public static String repeat(int count) {
        return repeat(count, " ");
    }

    public static void main(String[] args) throws IOException {
        String dir = "/home/arkady/dev/java/lab2matran/";
        String in;
        StringBuilder sb = new StringBuilder();
        try{
            FileReader fr = new FileReader(dir + "code.txt");

            while (fr.ready()) {
                char ch = (char)fr.read();
                sb.append(ch);
            }
        }
        catch(Exception e) {
            e.getMessage();
        }
        in = sb.toString();
        sb = new StringBuilder("");
        Lexer lx = new Lexer(in, TokenTypes.values());
        List<Token> tokens = new ArrayList<>();
        while(lx.hasMoreElements()){
            Token token = lx.nextElement();
            if(token.getType() == TokenTypes.SPACE){
                continue;
            }
            tokens.add(token);
            //System.out.println(token.getText() + " : " + token.getType());
            sb.append(token.getText()).append(" : ").append(token.getType()).append("\n");
        }
        Path pt = Path.of("result.txt");
        Files.writeString(pt, sb.toString());


        Parser parser = new Parser(tokens);
        ExpressionNode rootNode = parser.parseCode();
        //printNode(rootNode);
        parser.execute(rootNode);
    }

    public static void printNode(ExpressionNode expressionNode){
        if(expressionNode.getClass() == NumberNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            NumberNode numberNode = (NumberNode) expressionNode;
            System.out.println("-- number:");
            System.out.println(numberNode.getNumber().getText());
            return;
        }
        if(expressionNode.getClass() == RealNumberNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            RealNumberNode realNumberNode = (RealNumberNode) expressionNode;
            System.out.println("-- number:");
            System.out.println(realNumberNode.getNumber().getText());
            return;
        }
        if(expressionNode.getClass() == UnarOperationNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            UnarOperationNode unarOperationNode = (UnarOperationNode) expressionNode;
            System.out.println(unarOperationNode.getOperator().getText());
            printNode(unarOperationNode.getOperand());
            return;
        }
        if(expressionNode.getClass() == BinOperationNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            BinOperationNode binOperationNode = (BinOperationNode) expressionNode;
            System.out.println("-- left part:");
            printNode(binOperationNode.getLeftNode());
            System.out.println("-- operator:");
            System.out.println(binOperationNode.getOperator().getText());
            System.out.println("-- right part:");
            printNode(binOperationNode.getRightNode());
            return;
        }
        if(expressionNode.getClass() == BooleanNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            BooleanNode booleanNode = (BooleanNode) expressionNode;
            System.out.println("-- left part:");
            printNode(booleanNode.getLeftNode());
            System.out.println("-- operator:");
            System.out.println(booleanNode.getOperator().getText());
            System.out.println("-- right part:");
            printNode(booleanNode.getRightNode());
            return;
        }
        if(expressionNode.getClass() == VariableNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            VariableNode variableNode = (VariableNode) expressionNode;
            if(variableNode.getVariableType().getText() == null){
                System.out.println(variableNode.getVariable().getText());
            } else {
                ;
                System.out.println("-- variable type: \n" + variableNode.getVariableType().getText() + "\n" +
                        "-- variable: \n" + variableNode.getVariable().getText());
            }
            return;
        }
        if(expressionNode.getClass() == ElseNode.class){
            ElseNode elseNode = (ElseNode) expressionNode;
            //printNode(elseNode.getIfNode());
            System.out.println("-- else body:");
            printNode(elseNode.getElseCodeNode());
            System.out.println("-- end else body:");
            return;
        }
        if(expressionNode.getClass() == ConditionNode.class){
            ConditionNode conditionNode = (ConditionNode) expressionNode;
            System.out.println("-- condition:");
            printNode(conditionNode.getLeftNode());
            System.out.println("-- if body:");
            printNode(conditionNode.getRightNode());
            System.out.println("-- end if body:");
            printNode(conditionNode.getIfToken());
            return;
        }

        if(expressionNode.getClass() == CycleNode.class){
            CycleNode conditionNode = (CycleNode) expressionNode;
            System.out.println(conditionNode.getCycleOperator().getText());
            System.out.println("-- condition:");
            printNode(conditionNode.getConditionNode());
            System.out.println("-- cycle body:");
            printNode(conditionNode.getBodyNode());
            System.out.println("-- end cycle body:");
            return;
        }
        if(expressionNode.getClass() == ConstNode.class){
            ConstNode constNode = (ConstNode) expressionNode;
            System.out.println(constNode.getConstOperator().getText());
            return;
        }

        if(expressionNode.getClass() == ForCycleNode.class){
            ForCycleNode forCycleNode = (ForCycleNode) expressionNode;
            BinOperationNode localVarDec = (BinOperationNode) forCycleNode.getConditionNode1();
            BooleanNode cycleCond = (BooleanNode) forCycleNode.getConditionNode2();
            printNode(forCycleNode.getConditionNode1());
            printNode(forCycleNode.getConditionNode2());
            printNode(forCycleNode.getConditionNode3());
            return;
        }
        if(expressionNode.getClass() == IndexArrayNode.class){
            IndexArrayNode indexArrayNode = (IndexArrayNode) expressionNode;
            System.out.println(indexArrayNode.getArrayName().getText() + "[" +
                    "]");
            return;
        }
        if(expressionNode.getClass() == ArrayNode.class){
            ArrayNode arrayNode = (ArrayNode) expressionNode;
            printNode(arrayNode.getArrayName());
            System.out.println("-- array size: " + arrayNode.getArraySize());
            if(arrayNode.getArrayData() != null){
                for (ExpressionNode a: arrayNode.getArrayData()) {
                    printNode(a);
                }
            }
            return;
        }
        if(expressionNode.getClass() == StatementsNode.class){
            StatementsNode statementsNode = (StatementsNode) expressionNode;
            for (ExpressionNode a: statementsNode.getCode()) {
                printNode(a);
            }
            return;
        }
        System.out.println(expressionNode.getClass());
        throw new RuntimeException("Error");
    }

}
