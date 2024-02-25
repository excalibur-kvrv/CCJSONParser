import java.io.File;
import java.util.Arrays;
import java.util.Objects;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Provide tests directory as cli argument
        File file = new File(args[0]);
        for (String fileName: Objects.requireNonNull(file.list())) {
            System.out.println(fileName);
            try {
                Lexer lex = new Lexer(args[0] + fileName);
                Parser parser = new Parser(lex);
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
}