package Lexer;

public enum TokenTypes implements ITokenType {
    KEYWORD("\\b(?:switch|new|case|return|public|static|void|private|break|default)\\b"),
    TRUE_FALSE("\\b(?:true|false)\\b"),
    VARIABLE_TYPE("\\b(?:int|double|var|string|float|short|boolean)\\b"),
    PRINT("\\b(?:Console.Write|Console.WriteLine)\\b"),
    CYCLES("\\b(?:while|for|do|foreach)\\b"),
    IF_STATEMENT("\\b(?:if)\\b"),
    ELSE_STATEMENT("\\b(?:else)\\b"),
    VARIABLE("[A-Za-z][A-Za-z_0-9]*"),
    REAL_NUMBER("[0-9]+\\.[0-9]*"),
    NUMBER("[0-9]+"),
    SPACE("\\s+"),
    COMMENT("\\/\\/"),
    INC("\\+{2}"),
    DEC("\\-{2}"),
    OPERATION("[\\%\\+\\-\\*/.]"),
    BIN_OPERATION("[\\<\\>]"),
    EQUAL("={1,2}"),
    SYNTAX_SYMBOL("[\\,]"),
    K_LBRACKET("[\\[]"),
    K_RBRACKET("[\\]]"),
    COLON("[\\:]"),
    SEMICOLON("[\\;]"),
    LBRACKET("[\\(]"),
    RBRACKET("[\\)]"),
    F_LBRACKET("[\\{]"),
    F_RBRACKET("[\\}]");

    private final String regex;

    TokenTypes(String regex) {
        this.regex = regex;
    }

    @Override
    public String getRegex() {
        return regex;
    }
}
