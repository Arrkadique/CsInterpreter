package Lexer;

public interface ITokenType {
    /**
     * Регулярное выражение для данного типа токена
     */
    String getRegex();
}