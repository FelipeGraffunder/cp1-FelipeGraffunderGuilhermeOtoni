import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java -cp build Main exemplos/programa.txt");
            System.exit(2);
        }
        try {
            String fonte = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
            Scanner scanner = new Scanner(fonte);
            Token token;
            do {
                token = scanner.nextToken();
                System.out.println(token);
            } while (token.tipo != TipoToken.EOF);

            for (String erro : scanner.getErros()) System.err.println(erro);
            if (!scanner.getErros().isEmpty()) System.exit(1);
        } catch (IOException erro) {
            System.err.println("Nao foi possivel ler o arquivo: " + erro.getMessage());
            System.exit(2);
        }
    }
}
