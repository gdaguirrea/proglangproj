package lex;

public class Token {

    public enum TokenType {
        BEGIN("begin"), RETURN("return"),
        EQ("=="), LEQT("<="), GEQT(">="), DIFF("<>"), LT("<"), GT(">"),
        MULT("[*]"), DIV("[/]"), ADD("[+]"), SUB("[-]"),
        LPAREN("\\("), RPAREN("\\)"), LBRA("\\{"), RBRA("\\}"), LSBRA("\\["), RSBRA("\\]"),
        COMMA(","), PEROID("\\."), ASSIGN("="), SEMICOLON(";"),
        WHILE("while"), IF("if"), ELSE("else"), COMMENT("//"),
        INTEGER("int"),
        PUBLIC("public"), PRIVATE("private"), PACKAGE("package"), IMPORT("import"), ENUM("enum"),
        NUMBER("\\d+"), ID("\\w+"), LSKIP("[\\n]"), SKIP("[\\t\\x0b\\r\\f\\p{Z}]+"),
        EOF("\\z"), INVALID(".*");

        public final String pattern;

        private TokenType(String pattern) {
            this.pattern = pattern;
        }
    }

    public TokenType type;
    public String data;
    public int line;

    public Token(TokenType type, String data, int line) {
        this.type = type;
        this.data = data;
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format("[ %s, %s, %d ]", type.name(), this.data, this.line);
    }
}

