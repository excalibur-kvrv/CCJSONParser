public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private boolean parseRaw(String token) {
        System.out.println("Parsing raw " + token);
        for (int i = 0; i < token.length(); i++) {
            if (lexer.peek(1).token() != token.charAt(i)) {
                return false;
            } else {
                System.out.println(lexer.getNextToken());
            }
        }
        return true;
    }

    public boolean parseJSON() {
        // json -> element
        System.out.println("Parsing JSON");
        return parseElement();
    }

    private boolean parseValue() {
        // value -> object | array | string | number | 'true' | 'false' | 'null'
        System.out.println("Parsing Value");
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
        System.out.println("Parsing Object");
        System.out.println(lexer.getNextToken());
        boolean match;
        match = parseMembers() || parseWhiteSpace();
        return match && parseRaw("}");
    }

    private boolean parseMembers() {
        // members -> member | member ',' members
        System.out.println("Parsing members");
        boolean match;
        match = parseMember();
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == ',') {
            System.out.println(lexer.getNextToken());
            return match && parseMembers();
        }
        return match;
    }

    private boolean parseMember() {
        System.out.println("Parsing member");
        // member -> ws string ws ':' element
        return parseWhiteSpace() && parseString() && parseWhiteSpace() && lexer.getNextToken().token() == ':' && parseElement();
    }

    private boolean parseArray() {
        // array -> '[' ws ']' | '[' elements ']'
        System.out.println("Parsing array");
        Token token = lexer.getNextToken();
        System.out.println(token);
        boolean match = token.token() == '[';
        match = match && (parseElements() || parseWhiteSpace());
        token = lexer.getNextToken();
        System.out.println(token);
        match = match && token.token() == ']';
        return match;
    }

    private boolean parseElements() {
        // elements -> element | element ',' elements
        System.out.println("Parsing elements");
        boolean match = parseElement();
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == ',') {
            System.out.println(lexer.getNextToken());
            return match && parseElements();
        }
        return match;
    }

    private boolean parseElement() {
        // element -> ws value ws
        System.out.println("Parsing element");
        return parseWhiteSpace() && parseValue() && parseWhiteSpace();
    }

    private boolean parseString() {
        // string -> '"' characters '"'
        System.out.println("Parsing string");
        Token token = lexer.getNextToken();
        System.out.println(token);
        boolean match = token.token() == '"';
        match = match && parseCharacters();
        token = lexer.getNextToken();
        System.out.println(token);
        match = match && token.token() == '"';
        return match;
    }

    private boolean parseCharacters() {
        // characters -> "" | character characters
        System.out.println("Parsing characters");
        if (parseCharacter()) {
            System.out.println(lexer.getNextToken());
            parseCharacters();
//            return true;
        }
        return true;
    }

    private boolean parseCharacter() {
        // character -> 'U+0020' . 'U+10FFFF' - '"' - '\' | '\' escape
        System.out.println("Parsing characters");
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == '\\') {
            System.out.println(lexer.getNextToken());
            return parseEscape();
        } else if (token == '"') {
            return false;
        }
        return (int)token <= 256;
    }

    private boolean parseEscape() {
        // escape -> '"' | '\' | '/' | 'b' | 'f' | 'n' | 'r' | 't' | 'u' hex hex hex hex
        System.out.println("Parsing escape");
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        return switch (token) {
            case '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> {
                System.out.println(lexer.getNextToken());
                yield true;
            }
            case 'u' -> {
                System.out.println(lexer.getNextToken());
                yield parseHex() && parseHex() && parseHex() && parseHex();
            }
            default -> false;
        };
    }

    private boolean parseHex() {
        // hex -> digit | 'A' . 'F' | 'a' . 'f'
        System.out.println("Parsing hex");
        Token currentToken = lexer.peek(1);
        int token = currentToken.token();
        return parseDigit() || switch (token) {
            case 65, 66, 67, 68, 69, 70, 97, 98, 99, 100, 101, 102 -> {
                System.out.println(lexer.getNextToken());
                yield true;
            }
            default -> false;
        };
    }

    private boolean parseNumber() {
        // number -> integer fraction exponent
        System.out.println("Parsing number");
        return parseInteger() && parseFraction() && parseExponent();
    }

    private boolean parseInteger() {
        // integer -> digit | onenine digits | '-' digit | '-' onenine digits
        System.out.println("Parsing integer");
        Token currentToken = lexer.peek(1);
        char current = currentToken.token();
        if (current == '-') {
            System.out.println(lexer.getNextToken());
            return (parseOneNine() && parseDigits()) || parseDigit();
        }
        return (parseOneNine() && parseDigits()) || parseDigit();
    }

    private boolean parseDigits() {
        // digits -> digit | digit digits
        System.out.println("Parsing digits");
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
        System.out.println("Parsing digit");
        Token currentToken = lexer.peek(1);
        char current = currentToken.token();
        if (current == '0') {
            System.out.println(lexer.getNextToken());
            return true;
        }
        return parseOneNine();
    }

    private boolean parseOneNine() {
        // onenine -> '1' . '9'
        System.out.println("Parsing one nine");
        Token currentToken = lexer.peek(1);
        char current = currentToken.token();
        return switch (current) {
            case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                System.out.println(lexer.getNextToken());
                yield true;
            }
            default -> false;
        };
    }

    private boolean parseFraction() {
        // fraction -> '' | '.' digits
        System.out.println("Parsing fraction");
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == '.') {
            System.out.println(lexer.getNextToken());
            return parseDigits();
        }
        return true;
    }

    private boolean parseExponent() {
        // exponent -> '' | 'E' sign digits | 'e' sign digits
        System.out.println("Parsing exponent");
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == 'E' || token == 'e') {
            System.out.println(lexer.getNextToken());
            return parseSign() && parseDigits();
        }
        return true;
    }

    private boolean parseSign() {
        // sign -> '' | '+' | '-'
        System.out.println("Parsing sign");
        Token currentToken = lexer.peek(1);
        char token = currentToken.token();
        if (token == '+' || token == '-') {
            System.out.println(lexer.getNextToken());
        }
        return true;
    }

    private boolean parseWhiteSpace() {
        // ws -> '' | ' ' ws | '\n' ws | '\r' ws | '\t' ws
        System.out.println("Parsing whitespace");
        Token currentToken = lexer.peek(1);
        if (currentToken == null) return true;
        char token = currentToken.token();
        if (token == ' ' || token == '\n' || token == '\r' || token == '\t') {
            System.out.println(lexer.getNextToken());
            return parseWhiteSpace();
        }
        return true;
    }
}
