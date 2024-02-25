import java.io.*;

record Token(char token, TokenType tokenType) {}

enum TokenType {
    LEFT_CURLY_BRACE, RIGHT_CURLY_BRACE, OTHER, COMMA,
    LEFT_SQ_BRACE, RIGHT_SQ_BRACE, COLON, QUOTE,
}

public class Lexer {
    private RandomAccessFile reader;
    private long length = 0;
    private long currentIndex = 0;

    Lexer(String filePath) {
        try {
            File jsonFile = new File(filePath);
            length = jsonFile.length();
            reader = new RandomAccessFile(jsonFile, "r");
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred" + e);
        }
    }

    public Token getNextToken() {
        try {
            int current = reader.read();
            if (current == -1) return null;
            TokenType detectedTokenType = getTokenType((char) current);
            currentIndex++;
            return new Token((char)current, detectedTokenType);
        } catch (IOException e) {
            System.out.println("Error during fetching next token");
        }
        return null;
    }

    private static TokenType getTokenType(char current) {
        return switch (current) {
            case '{' -> TokenType.LEFT_CURLY_BRACE;
            case '}' -> TokenType.RIGHT_CURLY_BRACE;
            case '[' -> TokenType.LEFT_SQ_BRACE;
            case ']' -> TokenType.RIGHT_SQ_BRACE;
            case '"' -> TokenType.QUOTE;
            case ':' -> TokenType.COLON;
            case ',' -> TokenType.COMMA;
            default -> TokenType.OTHER;
        };
    }

    public boolean hasNext() {
        return currentIndex != length;
    }

    public Token peek(int lookahead) {
        try {
            Token nextToken = null;
            while (lookahead != 0) {
                nextToken = getNextToken();
                --currentIndex;
                --lookahead;
            }
            reader.seek(currentIndex - lookahead);
            return nextToken;
        } catch (IOException e) {
            System.out.println("Error during fetching peek");
        }
        
        return null;
    }
}
