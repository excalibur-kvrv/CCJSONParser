public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private boolean parseRaw(String token) {
        for (int i = 0; i < token.length(); i++) {
            if (lexer.peek(1).token() != token.charAt(i)) {
                return false;
            } else {
                lexer.getNextToken();
            }
        }
        return true;
    }

    public boolean parseJSON() {
        // json -> element
        return parseElement();
    }

    private boolean parseValue() {
        // value -> object | array | string | number | 'true' | 'false' | 'null'
        Token currentToken = lexer.peek(1);
        boolean match = switch (currentToken.tokenType()) {
            case LEFT_CURLY_BRACE -> parseObject();
            case LEFT_SQ_BRACE -> parseArray();
            case QUOTE -> parseString();
            default -> false;
        };
        match = match || parseNumber();
        match = match || parseRaw("true");
        match = match || parseRaw("false");
        match = match || parseRaw("null");
        return match;
    }

    private boolean parseObject() {
        // object -> '{' ws '}' | '{' members '}'
        lexer.getNextToken();
        boolean match;
        match = parseMembers() || parseWhiteSpace();
        return match && parseRaw("}");
    }

    private boolean parseMembers() {
        // members -> member | member ',' members
        boolean match;
        match = parseMember();
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == ',') {
            lexer.getNextToken();
            return match && parseMembers();
        }
        return match;
    }

    private boolean parseMember() {
        // member -> ws string ws ':' element
        return parseWhiteSpace() && parseString() && parseWhiteSpace() && lexer.getNextToken().token() == ':' && parseElement();
    }

    private boolean parseArray() {
        // array -> '[' ws ']' | '[' elements ']'
        Token token = lexer.getNextToken();
        boolean match = token.token() == '[';
        match = match && (parseElements() || parseWhiteSpace());
        token = lexer.getNextToken();
        match = match && token.token() == ']';
        return match;
    }

    private boolean parseElements() {
        // elements -> element | element ',' elements
        boolean match = parseElement();
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == ',') {
            lexer.getNextToken();
            return match && parseElements();
        }
        return match;
    }

    private boolean parseElement() {
        // element -> ws value ws
        return parseWhiteSpace() && parseValue() && parseWhiteSpace();
    }

    private boolean parseString() {
        // string -> '"' characters '"'
        Token token = lexer.getNextToken();
        boolean match = token.token() == '"';
        match = match && parseCharacters();
        token = lexer.getNextToken();
        match = match && token.token() == '"';
        return match;
    }

    private boolean parseCharacters() {
        // characters -> "" | character characters
        if (parseCharacter()) {
            lexer.getNextToken();
            parseCharacters();
        }
        return true;
    }

    private boolean parseCharacter() {
        // character -> 'U+0020' . 'U+10FFFF' - '"' - '\' | '\' escape
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == '\\') {
            lexer.getNextToken();
            return parseEscape();
        } else if (token == '"') {
            return false;
        }
        return (int)token <= 256;
    }

    private boolean parseEscape() {
        // escape -> '"' | '\' | '/' | 'b' | 'f' | 'n' | 'r' | 't' | 'u' hex hex hex hex
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        return switch (token) {
            case '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> {
                lexer.getNextToken();
                yield true;
            }
            case 'u' -> {
                lexer.getNextToken();
                yield parseHex() && parseHex() && parseHex() && parseHex();
            }
            default -> false;
        };
    }

    private boolean parseHex() {
        // hex -> digit | 'A' . 'F' | 'a' . 'f'
        Token currentToken = lexer.peek(1);
        int token = currentToken.token();
        return parseDigit() || switch (token) {
            case 65, 66, 67, 68, 69, 70, 97, 98, 99, 100, 101, 102 -> {
                lexer.getNextToken();
                yield true;
            }
            default -> false;
        };
    }

    private boolean parseNumber() {
        // number -> integer fraction exponent
        return parseInteger() && parseFraction() && parseExponent();
    }

    private boolean parseInteger() {
        // integer -> digit | onenine digits | '-' digit | '-' onenine digits
        Token currentToken = lexer.peek(1);
        char current = currentToken.token();
        if (current == '-') {
            lexer.getNextToken();
            return (parseOneNine() && parseDigits()) || parseDigit();
        }
        return (parseOneNine() && parseDigits()) || parseDigit();
    }

    private boolean parseDigits() {
        // digits -> digit | digit digits
        boolean match = parseDigit();
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        switch (token) {
            case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                return parseDigits();
            }
        }
        return match;
    }

    private boolean parseDigit() {
        // digit -> '0' | onenine
        Token currentToken = lexer.peek(1);
        char current = currentToken.token();
        if (current == '0') {
            lexer.getNextToken();
            return true;
        }
        return parseOneNine();
    }

    private boolean parseOneNine() {
        // onenine -> '1' . '9'
        Token currentToken = lexer.peek(1);
        char current = currentToken.token();
        return switch (current) {
            case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                lexer.getNextToken();
                yield true;
            }
            default -> false;
        };
    }

    private boolean parseFraction() {
        // fraction -> '' | '.' digits
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == '.') {
            lexer.getNextToken();
            return parseDigits();
        }
        return true;
    }

    private boolean parseExponent() {
        // exponent -> '' | 'E' sign digits | 'e' sign digits
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == 'E' || token == 'e') {
            lexer.getNextToken();
            return parseSign() && parseDigits();
        }
        return true;
    }

    private boolean parseSign() {
        // sign -> '' | '+' | '-'
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == '+' || token == '-') {
            lexer.getNextToken();
        }
        return true;
    }

    private boolean parseWhiteSpace() {
        // ws -> '' | ' ' ws | '\n' ws | '\r' ws | '\t' ws
        Token currentToken = lexer.peek(1);
        if (currentToken == null) return true;
        char token = currentToken.token();
        if (token == ' ' || token == '\n' || token == '\r' || token == '\t') {
            lexer.getNextToken();
            return parseWhiteSpace();
        }
        return true;
    }
}
