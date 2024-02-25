//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Lexer lex = new Lexer("/Users/kevinrvaz/Downloads/test/fail4.json");
        Parser parser = new Parser(lex);
        try {
            if (parser.parseJSON()) {
                System.out.println("Valid JSON structure");
            } else {
                System.out.println("Invalid JSON structure");
            }
        } catch (Exception e) {
            System.out.println("Invalid JSON structure");
        }
    }
}