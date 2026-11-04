import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ScannerTest {
    private static int passou = 0;
    private static int falhou = 0;

    private static final class Resultado {
        final List<Token> tokens = new ArrayList<>();
        final List<String> erros;
        final Token eof;

        Resultado(String fonte) {
            Scanner scanner = new Scanner(fonte);
            Token atual = scanner.nextToken();
            while (atual.tipo != TipoToken.EOF) {
                tokens.add(atual);
                atual = scanner.nextToken();
            }
            eof = atual;
            erros = scanner.getErros();
        }
    }

    public static void main(String[] args) {
        System.out.println("Testes do scanner - Java " + System.getProperty("java.version"));

        teste("Identificadores validos", () -> esperar("identificadores.txt",
            0,
                "IDENTIFICADOR:idade",
                "IDENTIFICADOR:x1",
                "IDENTIFICADOR:valor_total",
                "IDENTIFICADOR:A",
                "IDENTIFICADOR:nome"));

        teste("Lista completa de palavras reservadas", () -> esperar(
            "palavras-reservadas.txt", 0,
                "PALAVRA_RESERVADA:int",
                "PALAVRA_RESERVADA:double",
                "PALAVRA_RESERVADA:bool",
                "PALAVRA_RESERVADA:char",
                "PALAVRA_RESERVADA:string",
                "PALAVRA_RESERVADA:void",
                "PALAVRA_RESERVADA:if",
                "PALAVRA_RESERVADA:else",
                "PALAVRA_RESERVADA:while",
                "PALAVRA_RESERVADA:return",
                "PALAVRA_RESERVADA:true",
                "PALAVRA_RESERVADA:false"));

        teste("Strings, string vazia e // dentro de string", () -> esperar(
            "strings.txt", 0,
                "STRING:ola",
                "STRING:",
                "STRING:x // y"));

        teste("Literais char, inclusive espaco e aspas duplas", () -> esperar(
            "caracteres.txt", 0,
                "CARACTERE:'a'",
                "CARACTERE:'7'",
                "CARACTERE:' '",
                "CARACTERE:'\"'"));

        teste("Inteiros e decimais", () -> esperar("numeros.txt", 0,
                "NUMERO:0",
                "NUMERO:10",
                "NUMERO:3.14",
                "NUMERO:0.5",
                "NUMERO:007"));

        teste("Todos os operadores e maximal munch", () -> esperar(
            "operadores.txt", 0,
                "OPERADOR:+",
                "OPERADOR:-",
                "OPERADOR:*",
                "OPERADOR:/",
                "OPERADOR:%",
                "OPERADOR:=",
                "OPERADOR:==",
                "OPERADOR:!=",
                "OPERADOR:<",
                "OPERADOR:>",
                "OPERADOR:<=",
                "OPERADOR:>=",
                "OPERADOR:&&",
                "OPERADOR:||",
                "OPERADOR:!"));

        teste("Delimitadores", () -> esperar("delimitadores.txt", 0,
                "DELIMITADOR:(",
                "DELIMITADOR:)",
                "DELIMITADOR:{",
                "DELIMITADOR:}",
                "DELIMITADOR:;",
                "DELIMITADOR:,"));

        teste("Programa realista com declaracoes, expressao e comentario", () -> esperar(
            "programa-declaracoes.txt", 0,
                "PALAVRA_RESERVADA:int",
                "IDENTIFICADOR:x",
                "OPERADOR:=",
                "NUMERO:10",
                "DELIMITADOR:;",
                "PALAVRA_RESERVADA:double",
                "IDENTIFICADOR:y",
                "OPERADOR:=",
                "NUMERO:3.14",
                "DELIMITADOR:;",
                "IDENTIFICADOR:x",
                "OPERADOR:=",
                "IDENTIFICADOR:x",
                "OPERADOR:+",
                "NUMERO:1",
                "DELIMITADOR:;",
                "PALAVRA_RESERVADA:if",
                "DELIMITADOR:(",
                "IDENTIFICADOR:x",
                "OPERADOR:>=",
                "NUMERO:10",
                "OPERADOR:&&",
                "PALAVRA_RESERVADA:true",
                "DELIMITADOR:)",
                "DELIMITADOR:{",
                "PALAVRA_RESERVADA:return",
                "DELIMITADOR:;",
                "DELIMITADOR:}"));

        teste("Funcao, parametros, chamada e retorno", () -> esperar(
            "programa-funcao.txt", 0,
                "PALAVRA_RESERVADA:int",
                "IDENTIFICADOR:soma",
                "DELIMITADOR:(",
                "PALAVRA_RESERVADA:int",
                "IDENTIFICADOR:a",
                "DELIMITADOR:,",
                "PALAVRA_RESERVADA:int",
                "IDENTIFICADOR:b",
                "DELIMITADOR:)",
                "DELIMITADOR:{",
                "PALAVRA_RESERVADA:return",
                "IDENTIFICADOR:a",
                "OPERADOR:+",
                "IDENTIFICADOR:b",
                "DELIMITADOR:;",
                "DELIMITADOR:}",
                "IDENTIFICADOR:soma",
                "DELIMITADOR:(",
                "NUMERO:1",
                "DELIMITADOR:,",
                "NUMERO:2",
                "DELIMITADOR:)",
                "DELIMITADOR:;"));

        teste("Procedimento, while e if/else", () -> esperar(
            "programa-controle.txt", 0,
                "PALAVRA_RESERVADA:void",
                "IDENTIFICADOR:f",
                "DELIMITADOR:(",
                "DELIMITADOR:)",
                "DELIMITADOR:{",
                "PALAVRA_RESERVADA:while",
                "DELIMITADOR:(",
                "PALAVRA_RESERVADA:false",
                "DELIMITADOR:)",
                "DELIMITADOR:{",
                "PALAVRA_RESERVADA:if",
                "DELIMITADOR:(",
                "PALAVRA_RESERVADA:true",
                "DELIMITADOR:)",
                "PALAVRA_RESERVADA:return",
                "DELIMITADOR:;",
                "PALAVRA_RESERVADA:else",
                "PALAVRA_RESERVADA:return",
                "DELIMITADOR:;",
                "DELIMITADOR:}",
                "DELIMITADOR:}"));

        teste("Programa realista com tres erros e recuperacao", () -> {
            Resultado r = esperar("programa-tres-erros.txt", 3,
                "PALAVRA_RESERVADA:int",
                "IDENTIFICADOR:x",
                "OPERADOR:=",
                "NUMERO:10",
                "DELIMITADOR:;",
                "PALAVRA_RESERVADA:double",
                "IDENTIFICADOR:y",
                "OPERADOR:=",
                "DELIMITADOR:;",
                "PALAVRA_RESERVADA:string",
                "IDENTIFICADOR:mensagem",
                "OPERADOR:=",
                "IDENTIFICADOR:x",
                "OPERADOR:=",
                "IDENTIFICADOR:x",
                "OPERADOR:+",
                "NUMERO:1",
                "DELIMITADOR:;",
                "PALAVRA_RESERVADA:return",
                "IDENTIFICADOR:x",
                "DELIMITADOR:;");
            erroEm(r, 0, 2, 12);
            erroEm(r, 1, 3, 19);
            erroEm(r, 2, 4, 1);
            verificar(r.erros.get(0).contains("Numero incompleto"), "Esperava erro no numero.");
            verificar(r.erros.get(1).contains("String sem fechamento"), "Esperava erro na string.");
            verificar(r.erros.get(2).contains("Caractere invalido: '@'"), "Esperava erro no simbolo.");
            posicao(r.tokens.get(18), 5, 1);
        });
        teste("Erro: string sem fechamento ate EOF", () -> {
            Resultado r = esperar("erro-string-eof.txt", 1);
            erroEm(r, 0, 1, 1);
            posicao(r.eof, 1, 5);
        });

        teste("Erro: string sem fechamento ate fim de linha e recuperacao", () -> {
            Resultado r = esperar("erro-string-linha.txt", 1,
                "PALAVRA_RESERVADA:int",
                "IDENTIFICADOR:x",
                "DELIMITADOR:;");
            erroEm(r, 0, 1, 1);
            posicao(r.tokens.get(0), 2, 1);
        });

        teste("Erro: simbolo em contexto invalido", () -> {
            Resultado r = esperar("erro-simbolo.txt", 1,
                "IDENTIFICADOR:x",
                "DELIMITADOR:;");
            erroEm(r, 0, 1, 1);
        });

        teste("Limite de 50 caracteres e recuperacao", () -> {
            esperar("identificador-50.txt", 0,
                "IDENTIFICADOR:" + "a".repeat(50));
            Resultado r = esperar("erro-identificador-51.txt", 1,
                "DELIMITADOR:;",
                "IDENTIFICADOR:x");
            erroEm(r, 0, 1, 3);
            posicao(r.tokens.get(0), 1, 54);
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
    private static Resultado esperar(String arquivo, int totalErros, String... esperados) {
        Resultado r = new Resultado(lerArquivo(arquivo));
        verificar(r.erros.size() == totalErros,
            "Erros: esperado " + totalErros + ", recebido " + r.erros);
        verificar(r.tokens.size() == esperados.length,
            "Quantidade de tokens: esperado " + esperados.length + ", recebido " + r.tokens);
        for (int i = 0; i < esperados.length; i++) {
            Token t = r.tokens.get(i);
            String atual = t.tipo + ":" + t.lexema;
            verificar(atual.equals(esperados[i]),
                "Token " + i + ": esperado " + esperados[i] + ", recebido " + atual);
        }
        return r;
    }

    private static void posicao(Token t, int linha, int coluna) {
        verificar(t.linha == linha && t.coluna == coluna,
            "Posicao de " + t.lexema + ": esperada " + linha + ":" + coluna + ", recebida " + t.linha + ":" + t.coluna);
    }

    private static void erroEm(Resultado r, int indice, int linha, int coluna) {
        String e = r.erros.get(indice);
        verificar(e.startsWith("Erro lexico em " + linha + ":" + coluna + " - "),
            "Posicao do erro: esperada " + linha + ":" + coluna + ", recebida " + e);
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
