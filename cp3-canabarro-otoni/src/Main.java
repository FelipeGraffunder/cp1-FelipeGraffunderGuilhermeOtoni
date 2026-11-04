import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import visitor.*;
public final class Main {
    public static void main(String[] args) {
        if(args.length<1) { System.err.println("Uso: java -cp build Main arquivo [--fonte]"); System.exit(2); }
        try {
            ResultadoParse r=new Parser(new Scanner(Files.readString(Path.of(args[0]),StandardCharsets.UTF_8))).parse();
            if(!r.aceito()) { r.erros.forEach(System.err::println); System.exit(1); }
            System.out.print(args.length>1 && args[1].equals("--fonte") ? new Reimpressora().imprimir(r.arvore) : new Impressora().imprimir(r.arvore));
        } catch(java.io.IOException e) { System.err.println(e.getMessage()); System.exit(2); }
    }
}
