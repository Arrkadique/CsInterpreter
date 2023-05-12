package Lexer;

import AST.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Parser{
    private final List<Token> tokens;
    private int pos = 0;
    private final List<VariableNode> variables = new ArrayList<>();
    private final List<ArrayNode> arrays = new ArrayList<>();
    private ConditionNode lastIfStatement;


    public Parser(List<Token> token) {
        this.tokens = token;
    }

    public Token match(TokenTypes ...tokenTypes){
        if(this.pos < this.tokens.size()){
            Token currentToken = this.tokens.get(pos);
            if(Arrays.stream(tokenTypes).anyMatch(element ->
                    element == currentToken.getType())){
                this.pos++;
                return currentToken;
            }
        }
        return null;
    }


    public Token require(TokenTypes ...tokenTypes){
        Token token = match(tokenTypes);
        if(token == null){
            throw new RuntimeException("expected " + tokenTypes[0] + " on position " + this.pos);
        }
        return token;
    }

    public ExpressionNode parsePrint(){
        Token operator = this.match(TokenTypes.PRINT);
        this.require(TokenTypes.LBRACKET);
        ExpressionNode toWriteData = this.parseFormula();
        this.require(TokenTypes.RBRACKET);
        if(operator != null){
            return new UnarOperationNode(operator, toWriteData);
        }
        throw new RuntimeException("Expected Console.Write on position " + pos);
    }

    public ExpressionNode parseVariableOrNumber(Token variableType){
        Token bool = this.match(TokenTypes.TRUE_FALSE);
        if(bool != null){
            return new ConstNode(bool);
        }
        Token number = this.match(TokenTypes.NUMBER);
        if(number != null){
            return new NumberNode(number);
        }
        Token realNumber = this.match(TokenTypes.REAL_NUMBER);
        if(realNumber != null){
            return new RealNumberNode(realNumber);
        }
        Token variable = this.match(TokenTypes.VARIABLE);
        if(variable != null){
            if(this.match(TokenTypes.K_LBRACKET) != null){
                ExpressionNode index = this.parseFormula();
                require(TokenTypes.K_RBRACKET);
                return new IndexArrayNode(index, variable);
            }
            return new VariableNode(variableType, variable);
        }
        throw new RuntimeException("expected number or variable on position " + pos);
    }

    public ExpressionNode parseExpression(){
        if(this.match(TokenTypes.VARIABLE_TYPE) != null){
            if(this.match(TokenTypes.VARIABLE) != null){
                this.pos -= 2;
                Token variableType = match(TokenTypes.VARIABLE_TYPE);
                ExpressionNode variableNode = this.parseVariableOrNumber(variableType);

                VariableNode toSave = (VariableNode) variableNode;
                if(isVariableExistToOverwrite(toSave.getVariable()) != null){
                    variables.remove(isVariableExistToOverwrite(toSave.getVariable()));
                }
                variables.add(new VariableNode(toSave.getVariableType(),
                        toSave.getVariable()));
                Token assignOperator = match(TokenTypes.EQUAL);
                if(assignOperator != null ){
                    ExpressionNode rightPartNode;
                    if(((VariableNode) variableNode).getVariableType().getText().equals("boolean")){
                        rightPartNode = this.parseCondition();
                    } else {
                        rightPartNode = this.parseFormula();
                    }
                    checkType(rightPartNode, variableNode);
                    return new BinOperationNode(assignOperator, variableNode, rightPartNode);
                }
            }
            if(this.match(TokenTypes.K_LBRACKET)!= null){
                pos -= 2;
                Token arrayType = match(TokenTypes.VARIABLE_TYPE);
                this.require(TokenTypes.K_LBRACKET);
                this.require(TokenTypes.K_RBRACKET);
                ExpressionNode arrayNode = this.parseVariableOrNumber(arrayType);
                Token assignOperator = match(TokenTypes.EQUAL);
                if(assignOperator != null ){
                    if(this.match(TokenTypes.F_LBRACKET) != null){
                        List<ExpressionNode> arrayData = new ArrayList<>();
                        int arraySize = 0;
                        arrayData.add(parseVariableOrNumber(arrayType));
                        Token synt = this.match(TokenTypes.SYNTAX_SYMBOL);
                        arraySize++;
                        while (synt != null){
                            ExpressionNode data = this.parseVariableOrNumber(arrayType);
                            arrayData.add(data);
                            arraySize++;
                            synt = this.match(TokenTypes.SYNTAX_SYMBOL);
                        }
                        require(TokenTypes.F_RBRACKET);
                        ArrayNode toSave = new ArrayNode(arrayNode, arraySize, arrayData);
                        arrays.add(toSave);
                        return toSave;
                    }
                    if(this.match(TokenTypes.KEYWORD) != null){
                        if(!Objects.equals(this.require(TokenTypes.VARIABLE_TYPE).getText(), arrayType.getText())){
                            throw new RuntimeException("different types in array:(");
                        }
                        this.require(TokenTypes.K_LBRACKET);
                        int arraySize = Integer.parseInt(this.match(TokenTypes.NUMBER).getText());
                        this.require(TokenTypes.K_RBRACKET);
                        ArrayNode toSave = new ArrayNode(arrayNode, arraySize, new ArrayList<>());
                        arrays.add(toSave);
                        return toSave;
                    }
                }
            }
        }
        if(this.match(TokenTypes.VARIABLE) != null){
            if(this.match(TokenTypes.K_LBRACKET) != null){
                this.pos-=2;
                Token arrayName = this.match(TokenTypes.VARIABLE);
                this.require(TokenTypes.K_LBRACKET);
                ExpressionNode arrayIndex = this.parseFormula();
                this.require(TokenTypes.K_RBRACKET);
                ArrayNode array = this.getArrayByName(arrayName.getText());
//                if(arrayIndex.getType() != TokenTypes.VARIABLE){
//                    if(Integer.parseInt(arrayIndex.getText()) >= array.getArraySize()){
//                        throw new RuntimeException("out of bounds in array");
//                    }
//                }
                IndexArrayNode indexArrayNode = new IndexArrayNode(arrayIndex, arrayName);
                Token assignOperator = match(TokenTypes.EQUAL);
                if(assignOperator != null ){
                    ExpressionNode rightPartNode = this.parseFormula();
                    //checkType(rightPartNode, variableNode);
                    return new BinOperationNode(assignOperator, indexArrayNode, rightPartNode);
                } else {
                    return indexArrayNode;
                }
            }
            if(this.match(TokenTypes.INC, TokenTypes.DEC) != null){
                this.pos-=2;
                Token buf = this.match(TokenTypes.VARIABLE);
                this.pos--;
                VariableNode variable = (VariableNode) this.parseVariableOrNumber(getTypeVariableNode(buf).getVariableType());
                isVariableExist(variable.getVariable());
                Token operator = this.match(TokenTypes.INC, TokenTypes.DEC);
                switch (operator.getText()){
                    case "++" -> {
                        return new BinOperationNode(new Token("=", TokenTypes.EQUAL, 0),
                                variable, new BinOperationNode(new Token("+", TokenTypes.OPERATION, 0),
                                variable, new NumberNode(new Token("1", TokenTypes.NUMBER, 0))));
                    }
                    case "--" -> {
                        return new BinOperationNode(new Token("=", TokenTypes.EQUAL, 0),
                                variable, new BinOperationNode(new Token("-", TokenTypes.OPERATION, 0),
                                variable, new NumberNode(new Token("1", TokenTypes.NUMBER, 0))));
                    }
                }

            }
            this.pos-=1;
            Token variable = match(TokenTypes.VARIABLE);
            this.pos--;
            isVariableExist(variable);
            ExpressionNode variableNode =
                    this.parseVariableOrNumber(getTypeVariableNode(variable).getVariableType());
            Token assignOperator = match(TokenTypes.EQUAL);
            if(assignOperator != null ){
                ExpressionNode rightPartNode = this.parseFormula();
                checkType(rightPartNode, variableNode);
                return new BinOperationNode(assignOperator, variableNode, rightPartNode);
            }
            return variableNode;
        }
        if(this.match(TokenTypes.IF_STATEMENT) != null){
            this.pos--;
            Token ifToken = match(TokenTypes.IF_STATEMENT);
            if(ifToken != null){
                this.require(TokenTypes.LBRACKET);
                ExpressionNode ifCondition = this.parseCondition();
                this.require(TokenTypes.RBRACKET);
                this.require(TokenTypes.F_LBRACKET);
                ExpressionNode ifBody =  this.parseBody();
                require(TokenTypes.F_RBRACKET);
                if(this.match(TokenTypes.ELSE_STATEMENT) != null){
                    this.require(TokenTypes.F_LBRACKET);
                    ExpressionNode elseBody =  this.parseBody();
                    ExpressionNode last = lastIfStatement;
                    return new ConditionNode(ifCondition, ifBody, new ElseNode(elseBody));
                }
                lastIfStatement = new ConditionNode(ifCondition, ifBody, null);
                this.pos--;
                return lastIfStatement;
            }
        }
        if(this.match(TokenTypes.CYCLES) != null){
            this.pos--;
            Token cycleOperator = this.match(TokenTypes.CYCLES);
            switch (cycleOperator.getText()) {
                case "for" -> {
                    this.require(TokenTypes.LBRACKET);
                    ExpressionNode leftNode, middleNode, rightNode;
                    leftNode = parseExpression();
                    this.require(TokenTypes.SEMICOLON);
                    middleNode = parseCondition();
                    this.require(TokenTypes.SEMICOLON);
                    rightNode = parseExpression();
                    this.require(TokenTypes.RBRACKET);
                    this.require(TokenTypes.F_LBRACKET);
                    ExpressionNode cycleBody = this.parseBody();
                    return new ForCycleNode(leftNode,middleNode,rightNode,cycleBody);
                }
                case "while" -> {
                    this.require(TokenTypes.LBRACKET);
                    ExpressionNode cycleCondition = this.parseCondition();
                    this.require(TokenTypes.RBRACKET);
                    this.require(TokenTypes.F_LBRACKET);
                    ExpressionNode cycleBody = this.parseBody();
                    return new CycleNode(cycleOperator, cycleCondition, cycleBody);
                }
                case "do" -> {
                    this.require(TokenTypes.F_LBRACKET);
                    ExpressionNode cycleBody = this.parseBody();
                    this.require(TokenTypes.F_RBRACKET);
                    this.require(TokenTypes.CYCLES);
                    this.require(TokenTypes.LBRACKET);
                    ExpressionNode cycleCondition = this.parseCondition();
                    this.require(TokenTypes.RBRACKET);
                    return new CycleNode(cycleOperator, cycleCondition, cycleBody);
                }
            }
        }
        if(this.match(TokenTypes.PRINT) != null){
            this.pos--;
            return this.parsePrint();
        }
        if(this.match(TokenTypes.F_LBRACKET) != null){
            return null;
        }
        if(this.match(TokenTypes.F_RBRACKET) != null){
            return null;
        }
        throw new RuntimeException(tokens.get(pos).getText() + " Wtf?!?!?!?! at pos " + pos);
    }

    private ArrayNode getArrayByName(String text) {
        for (ArrayNode a : arrays) {
            VariableNode variableNode = (VariableNode) a.getArrayName();
            if(variableNode.getVariable().getText().equals(text)){
                return a;
            }
        }
        throw new RuntimeException("using undefined array");
    }

    private ExpressionNode parseBody(){
        StatementsNode bodyRoot = new StatementsNode();
        ExpressionNode cycleCode = this.parseExpression();
        this.match(TokenTypes.SEMICOLON);
        bodyRoot.addNode(cycleCode);
        while(this.match(TokenTypes.F_RBRACKET) == null){
            cycleCode = this.parseExpression();
            this.match(TokenTypes.SEMICOLON);
            bodyRoot.addNode(cycleCode);
        }
        this.pos--;
        return bodyRoot;
    }

    private ExpressionNode parseCondition() {
        ExpressionNode ifConditon = this.parseBrackets();
        if(this.match(TokenTypes.OPERATION) != null){
            throw new RuntimeException("expected bool operation at pos " + pos);
        }
        Token operator = this.match(TokenTypes.BIN_OPERATION, TokenTypes.EQUAL);
        if(ifConditon.getClass() == NumberNode.class && operator == null){
            throw new RuntimeException("expected boolean at pos " + pos);
        }
        while (operator != null){
            ExpressionNode rightNode = this.parseBrackets();
            ifConditon = new BooleanNode(operator, ifConditon, rightNode);
            operator = this.match(TokenTypes.BIN_OPERATION, TokenTypes.EQUAL);
        }
        return ifConditon;
    }

    public ExpressionNode parseFormula(){
        ExpressionNode leftNode = this.parseBrackets();
        if(this.match(TokenTypes.BIN_OPERATION) != null){
            throw new RuntimeException("expected bin operation at pos " + pos);
        }
        Token operator = this.match(TokenTypes.OPERATION);
        while (operator != null){
            ExpressionNode rightNode = this.parseBrackets();
            leftNode = new BinOperationNode(operator, leftNode, rightNode);
            operator = this.match(TokenTypes.OPERATION);
        }
        return leftNode;
    }

    private ExpressionNode parseBrackets() {
        if(this.match(TokenTypes.LBRACKET) != null){
            ExpressionNode node = parseFormula();
            this.require(TokenTypes.RBRACKET);
            return node;
        } else {
            ExpressionNode variable = this.parseVariableOrNumber(null);
            if(variable.getClass() == IndexArrayNode.class){
                return variable;
            }
            if(variable.getClass() == ConstNode.class){
                return variable;
            }
            if(variable.getClass() == RealNumberNode.class){
                return variable;
            }
            if(variable.getClass() == NumberNode.class){
                return variable;
            } else {
                VariableNode variablee = (VariableNode) variable;
                VariableNode isExist = getTypeVariableNode(variablee.getVariable());
                if(isExist == null){
                    throw new RuntimeException("using undeclared variable at pos " + pos);
                }
                variablee.setVariableType(getTypeVariableNode(variablee.getVariable()).getVariableType());
            }
            return variable;
        }
    }

    public ExpressionNode parseCode(){
        StatementsNode root = new StatementsNode();
        while(this.pos < this.tokens.size()){
            if(this.match(TokenTypes.F_RBRACKET) != null){
                continue;
            } else{
                ExpressionNode codeNode = this.parseExpression();
                this.require(TokenTypes.SEMICOLON, TokenTypes.F_RBRACKET);
                root.addNode(codeNode);
            }
        }
        return root;
    }

    public void isVariableExist(Token variable){
        int counter = 0;
        for (VariableNode a: variables) {
            if(!Objects.equals(a.getVariable().getText(), variable.getText())){
                counter++;
            }
        }
        if(counter >= variables.size() || variables.size() == 0){
            throw new RuntimeException("Used undeclared variable " + variable.getText());
        }
    }

    public VariableNode isVariableExistToOverwrite(Token variable){
        for (VariableNode a: variables) {
            if(Objects.equals(a.getVariable().getText(), variable.getText())){
               return a;
            }
        }
        return null;
    }

    public VariableNode getTypeVariableNode(Token variable){
        for (VariableNode a: variables) {
            if(a.getVariable().getText().equals(variable.getText())){
                return a;
            }
        }
        return null;
    }

    public void setVariableData(String name, String data){
        for (VariableNode a: variables) {
            if(a.getVariable().getText().equals(name)){
                variables.get(variables.indexOf(a)).setData(data);
                return;
            }
        }
        throw new RuntimeException("don't try to use undeclared variables");
    }

    public String getVariableData(String name){
        for (VariableNode a: variables) {
            if(a.getVariable().getText().equals(name)){
                return a.getData();
            }
        }
        throw new RuntimeException("don't try to use undeclared variables");
    }

    public void checkType(ExpressionNode expressionNode, ExpressionNode variable){
        if(expressionNode.getClass() == NumberNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            NumberNode numberNode = (NumberNode) expressionNode;
            return;
        }
        if(expressionNode.getClass() == RealNumberNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            VariableNode variableNode = (VariableNode) variable;
            if(Objects.equals(variableNode.getVariableType().getText(), "int")){
                throw new RuntimeException("Integer can't contain real nubmer");
            }
            return;
        }
        if(expressionNode.getClass() == BinOperationNode.class){
            BinOperationNode binOperationNode = (BinOperationNode) expressionNode;
            //System.out.println(binOperationNode.getOperator().getType());
            checkType(binOperationNode.getLeftNode(), variable);
            checkType(binOperationNode.getRightNode(), variable);
            return;
        }
        if(expressionNode.getClass() == BooleanNode.class){
            BooleanNode booleanNode = (BooleanNode) expressionNode;
            //System.out.println(booleanNode.getOperator().getType());
            checkType(booleanNode.getLeftNode(), variable);
            checkType(booleanNode.getRightNode(), variable);
            return;
        }
        if(expressionNode.getClass() == VariableNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            VariableNode variableNode = (VariableNode) expressionNode;
            VariableNode mainVariable = (VariableNode) variable;
//            System.out.println(mainVariable.getVariableType().getText() + " " +
//                    "--- " + variableNode.getVariableType().getText());
            if(Objects.equals(mainVariable.getVariableType().getText(), "int")
                    && !Objects.equals(variableNode.getVariableType().getText(),
                    mainVariable.getVariableType().getText())){
                throw new RuntimeException("different types in int");
            }
            if(Objects.equals(mainVariable.getVariableType().getText(), "double")
                    && Objects.equals(variableNode.getVariableType().getText(), "int")){
                return;
            }
            if(Objects.equals(mainVariable.getVariableType().getText(), "double")
                    && !Objects.equals(variableNode.getVariableType().getText(),
                    mainVariable.getVariableType().getText())){
                throw new RuntimeException("different types in double");
            }
            return;
        }
        if(expressionNode.getClass() == ConstNode.class){
            VariableNode variableNode = (VariableNode) variable;
            if(Objects.equals(variableNode.getVariableType().getText(), "int")){
                throw new RuntimeException("Integer can't contain boolean");
            }
            return;
        }
        if(expressionNode.getClass() == IndexArrayNode.class){
//            IndexArrayNode indexArrayNode = (IndexArrayNode) expressionNode;
//            ArrayNode arrayNode = getArrayByName(indexArrayNode.getArrayName().getText());
//            VariableNode variableNode = (VariableNode) arrayNode.getArrayName();
//            if(Objects.equals(variableNode.getVariableType().getText(), "int")){
//                throw new RuntimeException("Integer can't contain boolean");
//            }
            return;
        }
        if(expressionNode.getClass() == StatementsNode.class){
            StatementsNode statementsNode = (StatementsNode) expressionNode;
            for (ExpressionNode a: statementsNode.getCode()) {
                checkType(a,variable);
            }
            return;
        }
        System.out.println(expressionNode.getClass());
        throw new RuntimeException("Error");
    }

    public String execute(ExpressionNode expressionNode){
        if(expressionNode.getClass() == NumberNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            NumberNode numberNode = (NumberNode) expressionNode;
            return numberNode.getNumber().getText();
        }
        if(expressionNode.getClass() == RealNumberNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            RealNumberNode realNumberNode = (RealNumberNode) expressionNode;
            return realNumberNode.getNumber().getText();
        }
        if(expressionNode.getClass() == UnarOperationNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            UnarOperationNode unarOperationNode = (UnarOperationNode) expressionNode;
            if(unarOperationNode.getOperator().getText().equals("Console.Write")){
                System.out.print(execute(unarOperationNode.getOperand()));
            } else {
                System.out.println(execute(unarOperationNode.getOperand()));
            }
            return null;
        }
        if(expressionNode.getClass() == BinOperationNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            BinOperationNode binOperationNode = (BinOperationNode) expressionNode;
            switch (binOperationNode.getOperator().getText()){
                case "+" ->{
                    if((binOperationNode.getLeftNode().getClass() == NumberNode.class &&
                            binOperationNode.getRightNode().getClass() == NumberNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == VariableNode.class &&
                            binOperationNode.getRightNode().getClass() == NumberNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == NumberNode.class &&
                            binOperationNode.getRightNode().getClass() == VariableNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == VariableNode.class &&
                                    binOperationNode.getRightNode().getClass() == VariableNode.class)){
                        return String.valueOf(Integer.parseInt(execute(binOperationNode.getLeftNode())) +
                                Integer.parseInt(execute(binOperationNode.getRightNode())));
                    }

                    return String.valueOf(Double.parseDouble(execute(binOperationNode.getLeftNode())) +
                            Double.parseDouble(execute(binOperationNode.getRightNode())));
                }
                case "-" ->{
                    if((binOperationNode.getLeftNode().getClass() == NumberNode.class &&
                            binOperationNode.getRightNode().getClass() == NumberNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == VariableNode.class &&
                                    binOperationNode.getRightNode().getClass() == NumberNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == NumberNode.class &&
                                    binOperationNode.getRightNode().getClass() == VariableNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == VariableNode.class &&
                                    binOperationNode.getRightNode().getClass() == VariableNode.class)){
                        return String.valueOf(Integer.parseInt(execute(binOperationNode.getLeftNode())) -
                                Integer.parseInt(execute(binOperationNode.getRightNode())));
                    }
                    return String.valueOf(Double.parseDouble(execute(binOperationNode.getLeftNode())) -
                            Double.parseDouble(execute(binOperationNode.getRightNode())));
                }
                case "*" ->{
                    if((binOperationNode.getLeftNode().getClass() == NumberNode.class &&
                            binOperationNode.getRightNode().getClass() == NumberNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == VariableNode.class &&
                                    binOperationNode.getRightNode().getClass() == NumberNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == NumberNode.class &&
                                    binOperationNode.getRightNode().getClass() == VariableNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == VariableNode.class &&
                                    binOperationNode.getRightNode().getClass() == VariableNode.class)){
                        return String.valueOf(Integer.parseInt(execute(binOperationNode.getLeftNode())) *
                                Integer.parseInt(execute(binOperationNode.getRightNode())));
                    }
                    return String.valueOf(Double.parseDouble(execute(binOperationNode.getLeftNode())) *
                            Double.parseDouble(execute(binOperationNode.getRightNode())));
                }
                case "/" ->{
                    if((binOperationNode.getLeftNode().getClass() != RealNumberNode.class &&
                            binOperationNode.getRightNode().getClass() != RealNumberNode.class)){
                        return String.valueOf(Integer.parseInt(execute(binOperationNode.getLeftNode())) /
                                Integer.parseInt(execute(binOperationNode.getRightNode())));
                    }
                    return String.valueOf(Double.parseDouble(execute(binOperationNode.getLeftNode())) /
                            Double.parseDouble(execute(binOperationNode.getRightNode())));
                }
                case "%" ->{
                    if((binOperationNode.getLeftNode().getClass() == NumberNode.class &&
                            binOperationNode.getRightNode().getClass() == NumberNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == VariableNode.class &&
                                    binOperationNode.getRightNode().getClass() == NumberNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == NumberNode.class &&
                                    binOperationNode.getRightNode().getClass() == VariableNode.class) ||
                            (binOperationNode.getLeftNode().getClass() == VariableNode.class &&
                                    binOperationNode.getRightNode().getClass() == VariableNode.class)){
                        return String.valueOf(Integer.parseInt(execute(binOperationNode.getLeftNode())) %
                                Integer.parseInt(execute(binOperationNode.getRightNode())));
                    }
                    return String.valueOf(Double.parseDouble(execute(binOperationNode.getLeftNode())) %
                            Double.parseDouble(execute(binOperationNode.getRightNode())));
                }
                case "=" ->{
                    String result = execute(binOperationNode.getRightNode());
                    if(binOperationNode.getLeftNode().getClass() == VariableNode.class){
                        VariableNode variableNode = (VariableNode) binOperationNode.getLeftNode();
                        setVariableData(variableNode.getVariable().getText(), result);
                        return result;
                    } else {
                        IndexArrayNode indexArrayNode = (IndexArrayNode) binOperationNode.getLeftNode();
                        ArrayNode arrayNode = getArrayByName(indexArrayNode.getArrayName().getText());
                        arrayNode.setByIndex(Integer.parseInt(execute(indexArrayNode.getIndex())), result);
                        return result;
                    }

                }
            }
        }
        if(expressionNode.getClass() == BooleanNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            BooleanNode booleanNode = (BooleanNode) expressionNode;
            switch (booleanNode.getOperator().getText()) {
                case ">" -> {
                    return String.valueOf(Integer.parseInt(execute(booleanNode.getLeftNode())) >
                            Integer.parseInt(execute(booleanNode.getRightNode())));
                }
                case "<" -> {
                    return String.valueOf(Integer.parseInt(execute(booleanNode.getLeftNode())) <
                            Integer.parseInt(execute(booleanNode.getRightNode())));
                }
                case "==" -> {
                    return String.valueOf(Integer.parseInt(execute(booleanNode.getLeftNode())) ==
                            Integer.parseInt(execute(booleanNode.getRightNode())));
                }
            }
        }
        if(expressionNode.getClass() == VariableNode.class){
            //System.out.println("--- " + expressionNode.getClass());
            VariableNode variableNode = (VariableNode) expressionNode;
            isVariableExist(variableNode.getVariable());
            return getVariableData(variableNode.getVariable().getText());
        }
        if(expressionNode.getClass() == ElseNode.class){
            ElseNode elseNode = (ElseNode) expressionNode;
            return execute(elseNode.getElseCodeNode());
        }
        if(expressionNode.getClass() == ConditionNode.class){
            ConditionNode conditionNode = (ConditionNode) expressionNode;
            if(Boolean.parseBoolean(execute(conditionNode.getLeftNode()))){
                return execute(conditionNode.getRightNode());
            } else if(conditionNode.getIfToken() != null){
                return execute(conditionNode.getIfToken());
            }
            return null;
        }
        if(expressionNode.getClass() == ForCycleNode.class){
            ForCycleNode forCycleNode = (ForCycleNode) expressionNode;
            BinOperationNode localVarDec = (BinOperationNode) forCycleNode.getConditionNode1();
            BooleanNode cycleCond = (BooleanNode) forCycleNode.getConditionNode2();
            switch (cycleCond.getOperator().getText()){
                case ">" ->{
                    for(int i = Integer.parseInt(execute(localVarDec));
                        Integer.parseInt(execute(cycleCond.getLeftNode())) >
                                Integer.parseInt(execute(cycleCond.getRightNode()));
                        execute(forCycleNode.getConditionNode3())){
                        execute(forCycleNode.getBodyNode());
                    }
                    return null;
                }
                case "<" ->{
                    for(int i = Integer.parseInt(execute(localVarDec));
                        Integer.parseInt(execute(cycleCond.getLeftNode())) <
                                Integer.parseInt(execute(cycleCond.getRightNode()));
                        execute(forCycleNode.getConditionNode3())){
                        execute(forCycleNode.getBodyNode());
                    }
                    return null;
                }
                case "==" ->{
                    for(int i = Integer.parseInt(execute(localVarDec));
                        Integer.parseInt(execute(cycleCond.getLeftNode())) ==
                                Integer.parseInt(execute(cycleCond.getRightNode()));
                        execute(forCycleNode.getConditionNode3())){
                        execute(forCycleNode.getBodyNode());
                    }
                    return null;
                }
            }
            return null;
        }
        if(expressionNode.getClass() == CycleNode.class){
            CycleNode cycleNode = (CycleNode) expressionNode;
            BooleanNode cycleCond = (BooleanNode) cycleNode.getConditionNode();
            switch (cycleNode.getCycleOperator().getText()){
                case "while" ->{
                    switch (cycleNode.getCycleOperator().getText()){
                        case ">" ->{
                            while(Integer.parseInt(execute(cycleCond.getLeftNode())) >
                                    Integer.parseInt(execute(cycleCond.getRightNode()))){
                                execute(cycleNode.getBodyNode());
                            }
                            return null;
                        }
                        case "<" ->{
                            while(Integer.parseInt(execute(cycleCond.getLeftNode())) <
                                    Integer.parseInt(execute(cycleCond.getRightNode()))){
                                execute(cycleNode.getBodyNode());
                            }
                            return null;
                        }
                        case "==" ->{
                            while(Integer.parseInt(execute(cycleCond.getLeftNode())) ==
                                    Integer.parseInt(execute(cycleCond.getRightNode()))){
                                execute(cycleNode.getBodyNode());
                            }
                            return null;
                        }
                    }
                }
                case "do" ->{
                    switch (cycleNode.getCycleOperator().getText()){
                        case ">" ->{
                            do{
                                execute(cycleNode.getBodyNode());
                            } while(Integer.parseInt(execute(cycleCond.getLeftNode())) >
                                    Integer.parseInt(execute(cycleCond.getRightNode())));
                            return null;
                        }
                        case "<" ->{
                            do{
                                execute(cycleNode.getBodyNode());
                            } while(Integer.parseInt(execute(cycleCond.getLeftNode())) <
                                    Integer.parseInt(execute(cycleCond.getRightNode())));
                            return null;
                        }
                        case "==" ->{
                            do{
                                execute(cycleNode.getBodyNode());
                            } while(Integer.parseInt(execute(cycleCond.getLeftNode())) ==
                                    Integer.parseInt(execute(cycleCond.getRightNode())));
                            return null;
                        }
                    }
                }
            }
            return null;
        }
        if(expressionNode.getClass() == ConstNode.class){
            ConstNode constNode = (ConstNode) expressionNode;
            return constNode.getConstOperator().getText();
        }
        if(expressionNode.getClass() == ArrayNode.class){
            return null;
        }
        if(expressionNode.getClass() == IndexArrayNode.class){
            IndexArrayNode indexArrayNode = (IndexArrayNode) expressionNode;
            ArrayNode arrayNode = getArrayByName(indexArrayNode.getArrayName().getText());
            if(arrayNode.getByIndex(Integer.parseInt(execute(indexArrayNode.getIndex()))).getClass() ==
                    NumberNode.class){
                NumberNode numberNode = (NumberNode) arrayNode
                        .getByIndex(Integer.parseInt(execute(indexArrayNode.getIndex())));
                return numberNode.getNumber().getText();
            } else {
                RealNumberNode realNumberNode = (RealNumberNode) arrayNode
                        .getByIndex(Integer.parseInt(execute(indexArrayNode.getIndex())));
                return realNumberNode.getNumber().getText();
            }
        }
        if(expressionNode.getClass() == StatementsNode.class){
            StatementsNode statementsNode = (StatementsNode) expressionNode;
            for (ExpressionNode a: statementsNode.getCode()) {
                execute(a);
            }
            return null;
        }
        System.out.println(expressionNode.getClass());
        throw new RuntimeException("Error");
    }


}
