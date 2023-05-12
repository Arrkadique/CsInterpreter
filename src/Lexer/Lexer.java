package Lexer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Lexer implements Enumeration<Token> {

    private final String code;
    private final ITokenType[] tokenTypes;
    private final Matcher matcher;
    private int pos = 0;
    private ITokenType tt = null;

    public Lexer(String code, ITokenType[] tokenTypes) {
        this.code = code;
        this.tokenTypes = tokenTypes;

        List<String> fullRegex = new ArrayList<>();
        for (int i = 0; i < tokenTypes.length; i++) {
            ITokenType tokenType = tokenTypes[i];
            fullRegex.add("(?<g" + i + ">" + tokenType.getRegex() + ")");
        }
        String regex = String.join("|", fullRegex);

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(code);
        matcher.find();
    }

    @Override
    public boolean hasMoreElements() {
        return pos < code.length();
    }

    @Override
    public Token nextElement() {
        boolean found = pos > matcher.start() ? matcher.find() : true;

        int start = found ? matcher.start() : code.length();
        int end = found ? matcher.end() : code.length();

        if(found && pos == start) {
            pos = end;
            for (int i = 0; i < tokenTypes.length; i++) {
                String si = "g" + i;
                if (matcher.start(si) == start && matcher.end(si) == end) {
                    checkType(tokenTypes[i]);
                    if(tokenTypes[i] != TokenTypes.SPACE) {
                        tt = tokenTypes[i];
                    }
                    return createToken(code, tokenTypes[i], start, end);
                }
            }
        }
        throw new IllegalStateException("Undefined lex in position " + pos + " :(");
    }

    protected Token createToken(String content, ITokenType tokenType, int start, int end) {
        return new Token(content.substring(start, end), tokenType, start);
    }

    public void checkType(ITokenType tokenType){
        if(tokenType == TokenTypes.NUMBER){
            if(tt == TokenTypes.VARIABLE_TYPE){
                throw new RuntimeException("Id can't start from number in position " + pos +" :(");
            }
        }
        if(tokenType == TokenTypes.VARIABLE_TYPE && tt == TokenTypes.VARIABLE_TYPE){
            throw new RuntimeException("Double type in position " + pos +" :(");
        }
        if(tokenType == TokenTypes.OPERATION && tt == TokenTypes.OPERATION){
            throw new RuntimeException("Check your operations in position " + pos + " :(");
        }
        if(tokenType == TokenTypes.VARIABLE && tt == TokenTypes.VARIABLE){
            throw new RuntimeException("Syntax error in position " + pos + " :(");
        }
        if(tokenType == TokenTypes.LBRACKET && tt == TokenTypes.VARIABLE){
            throw new RuntimeException("Syntax error in position " + pos + " :(");
        }
        if(tokenType == TokenTypes.NUMBER && tt == TokenTypes.VARIABLE){
            throw new RuntimeException("Syntax error in position " + pos + " :(");
        }
    }
}
