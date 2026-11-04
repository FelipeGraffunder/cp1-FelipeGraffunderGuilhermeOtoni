import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java -cp build Main arquivo");
            System.exit(2);
        }
        try {
            String fonte = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
            Parser parser = new Parser(new Scanner(fonte));
            boolean aceito = parser.parse();
            System.out.println(aceito ? "ACEITO" : "REJEITADO");
            for (String erro : parser.getErros()) System.out.println(erro);
            if (!aceito) System.exit(1);
        } catch (IOException erro) {
            System.err.println("Nao foi possivel ler o arquivo: " + erro.getMessage());
            System.exit(2);
        }
    }
}
