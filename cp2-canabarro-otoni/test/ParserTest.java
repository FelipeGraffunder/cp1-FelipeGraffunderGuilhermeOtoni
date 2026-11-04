import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class ParserTest {
    private static int passou = 0;
    private static int falhou = 0;

    private static final class Resultado {
        final boolean aceito;
        final List<String> erros;

        Resultado(String fonte) {
            Parser parser = new Parser(new Scanner(fonte));
            aceito = parser.parse();
            erros = parser.getErros();
        }
    }

    public static void main(String[] args) {
        System.out.println("Testes do parser - Java " + System.getProperty("java.version"));

        teste("Programa valido com cinco tipos e operadores", () ->
            esperar("valido-tipos.txt", true));

        teste("Programa valido com if/else e while", () ->
            esperar("valido-controle.txt", true));

        teste("Programa valido com procedimentos, funcoes e retornos", () ->
            esperar("valido-funcoes.txt", true));

        teste("Erro: falta ponto e virgula", () -> {
            Resultado r = esperar("invalido-sem-ponto-virgula-eof.txt", false);
            verificar(r.erros.size() == 1, "Esperava apenas um erro: " + r.erros);
            verificar(contem(r.erros, "esperava ponto e virgula"),
                "Esperava erro sobre o ponto e virgula.");
            erroEm(r, 1, 14);
        });

        teste("Erro: falta fechar parenteses", () ->
            esperar("invalido-parenteses.txt", false));

        teste("Erro: falta operando", () -> {
            Resultado r = esperar("invalido-operando.txt", false);
            erroEm(r, 1, 13);
            verificar(contem(r.erros, "esperava operando"),
                "Esperava erro sobre o operando ausente.");
        });

        teste("Programa com quatro erros e recuperacao", () -> {
            Resultado r = esperar("multiplos-erros.txt", false);
            verificar(r.erros.size() == 4, "Esperava quatro erros na mesma analise: " + r.erros);
            erroEm(r, 1, 9);
            erroEm(r, 2, 10);
            erroEm(r, 3, 13);
            erroEm(r, 5, 5);
        });

        System.out.println();
        System.out.println("Resultado: " + passou + " passaram; " + falhou + " falharam.");
        if (falhou > 0) System.exit(1);
    }

    private static String lerArquivo(String nome) {
        try {
            return Files.readString(Paths.get("test", "entradas", nome), StandardCharsets.UTF_8);
        } catch (IOException erro) {
            throw new IllegalStateException("Nao foi possivel ler a entrada de teste: " + nome, erro);
        }
    }

    private static Resultado esperar(String arquivo, boolean aceitoEsperado) {
        Resultado r = new Resultado(lerArquivo(arquivo));
        verificar(r.aceito == aceitoEsperado,
            "Aceitacao: esperado " + aceitoEsperado + ", recebido " + r.aceito + ". Erros: " + r.erros);
        verificar(r.aceito == r.erros.isEmpty(),
            "O resultado deve ser aceito somente quando nao houver erros.");
        return r;
    }

    private static void erroEm(Resultado r, int linha, int coluna) {
        verificar(contem(r.erros, "Erro sintatico em " + linha + ":" + coluna + " ["),
            "Esperava erro sintatico em " + linha + ":" + coluna + ", recebido " + r.erros);
    }

    private static boolean contem(List<String> mensagens, String trecho) {
        for (String mensagem : mensagens) {
            if (mensagem.contains(trecho)) return true;
        }
        return false;
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static void teste(String nome, Runnable executar) {
        try {
            executar.run();
            passou++;
            System.out.println("[OK] " + nome);
        } catch (AssertionError | RuntimeException erro) {
            falhou++;
            System.out.println("[FALHOU] " + nome + " -> " + erro.getMessage());
        }
    }
}
