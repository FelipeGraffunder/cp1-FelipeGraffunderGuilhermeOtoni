import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Scanner {
    private enum EstadoId { ID_INICIO, ID_CORPO }
    private enum EstadoNumero { NUM_INICIO, NUM_INTEIRO, NUM_PONTO, NUM_DECIMAL }
    private enum EstadoString { STR_INICIO, STR_CORPO, STR_FIM, STR_ERRO }
    private enum EstadoChar { CHAR_INICIO, CHAR_VAZIO, CHAR_LIDO, CHAR_FIM, CHAR_ERRO }
    private enum EstadoOperador { OP_INICIO, OP_OPCIONAL, OP_E, OP_OU, OP_BARRA, OP_COMENTARIO, OP_FIM }
    private enum EstadoIgnorado { IGN_INICIO, IGN_ESPACO }
    private enum EstadoDelimitador { DEL_INICIO, DEL_FIM }

    private static final Set<String> RESERVADAS = new HashSet<>(Arrays.asList(
        "int", "double", "bool", "char", "string", "void",
        "if", "else", "while", "return", "true", "false"
    ));

    private final String fonte;
    private final ArrayList<String> erros = new ArrayList<>();
    private int indice = 0;
    private int linha = 1;
    private int coluna = 1;

    public Scanner(String fonte) {
        this.fonte = fonte;
    }

    public boolean hasNext() {
        return indice < fonte.length();
    }

    // -1 representa EOF.
    public int peek() {
        return hasNext() ? fonte.charAt(indice) : -1;
    }

    public int advance() {
        if (!hasNext()) return -1;
        char c = fonte.charAt(indice++);
        if (c == '\r') {
            linha++;
            coluna = 1;
        } else if (c == '\n') {
            if (indice < 2 || fonte.charAt(indice - 2) != '\r') linha++;
            coluna = 1;
        } else {
            coluna++;
        }
        return c;
    }

    public ArrayList<String> getErros() {
        return erros;
    }

    public Token nextToken() {
        while (true) {
            ignorarSeparadores();
            if (!hasNext()) return new Token(TipoToken.EOF, "", linha, coluna);

            int c = peek();
            Token token;
            if (letra(c)) token = lerIdentificador();
            else if (digito(c)) token = lerNumero();
            else if (c == '"') token = lerString();
            else if (c == '\'') token = lerCaractere();
            else if ("+-*/%=!<>&|".indexOf(c) >= 0) token = lerOperador();
            else if ("(){};,".indexOf(c) >= 0) token = lerDelimitador();
            else {
                registrar("Caractere invalido: " + mostrar(c), linha, coluna);
                advance();
                token = null;
            }
            if (token != null) return token;
        }
    }

    private Token lerIdentificador() {
        int inicio = indice, lin = linha, col = coluna;
        EstadoId estado = EstadoId.ID_INICIO;
        while (true) {
            switch (estado) {
                case ID_INICIO:
                    advance();
                    estado = EstadoId.ID_CORPO;
                    break;
                case ID_CORPO:
                    if (letra(peek()) || digito(peek()) || peek() == '_') {
                        advance();
                    } else {
                        String lexema = fonte.substring(inicio, indice);
                        if (lexema.length() > 50) {
                            registrar("Identificador excede o limite de 50 caracteres.", lin, col);
                            return null;
                        }
                        TipoToken tipo = RESERVADAS.contains(lexema)
                            ? TipoToken.PALAVRA_RESERVADA : TipoToken.IDENTIFICADOR;
                        return new Token(tipo, lexema, lin, col);
                    }
                    break;
            }
        }
    }

    private Token lerNumero() {
        int inicio = indice, lin = linha, col = coluna;
        EstadoNumero estado = EstadoNumero.NUM_INICIO;
        while (true) {
            switch (estado) {
                case NUM_INICIO:
                    advance();
                    estado = EstadoNumero.NUM_INTEIRO;
                    break;
                case NUM_INTEIRO:
                    if (digito(peek())) advance();
                    else if (peek() == '.') {
                        advance();
                        estado = EstadoNumero.NUM_PONTO;
                    } else return token(TipoToken.NUMERO, inicio, lin, col);
                    break;
                case NUM_PONTO:
                    if (digito(peek())) {
                        advance();
                        estado = EstadoNumero.NUM_DECIMAL;
                    } else {
                        registrar("Numero incompleto: falta digito depois do ponto.", lin, col);
                        return null;
                    }
                    break;
                case NUM_DECIMAL:
                    if (digito(peek())) advance();
                    else return token(TipoToken.NUMERO, inicio, lin, col);
                    break;
            }
        }
    }

    private Token lerString() {
        int inicio = indice, lin = linha, col = coluna;
        EstadoString estado = EstadoString.STR_INICIO;
        while (true) {
            switch (estado) {
                case STR_INICIO:
                    advance();
                    estado = EstadoString.STR_CORPO;
                    break;
                case STR_CORPO:
                    if (peek() == '"') {
                        advance();
                        estado = EstadoString.STR_FIM;
                    } else if (peek() != -1 && !quebra(peek())) advance();
                    else {
                        registrar("String sem fechamento.", lin, col);
                        estado = EstadoString.STR_ERRO;
                    }
                    break;
                case STR_FIM:
                    return new Token(TipoToken.STRING, fonte.substring(inicio + 1, indice - 1), lin, col);
                case STR_ERRO:
                    recuperarLiteral('"');
                    return null;
            }
        }
    }

    private Token lerCaractere() {
        int inicio = indice, lin = linha, col = coluna;
        EstadoChar estado = EstadoChar.CHAR_INICIO;
        while (true) {
            switch (estado) {
                case CHAR_INICIO:
                    advance();
                    estado = EstadoChar.CHAR_VAZIO;
                    break;
                case CHAR_VAZIO:
                    if (peek() != -1 && !quebra(peek()) && peek() != '\'') {
                        advance();
                        estado = EstadoChar.CHAR_LIDO;
                    } else {
                        registrar("Literal char precisa de um caractere permitido.", lin, col);
                        estado = EstadoChar.CHAR_ERRO;
                    }
                    break;
                case CHAR_LIDO:
                    if (peek() == '\'') {
                        advance();
                        estado = EstadoChar.CHAR_FIM;
                    } else {
                        registrar("Literal char deve conter um caractere e fechar com aspa.", lin, col);
                        estado = EstadoChar.CHAR_ERRO;
                    }
                    break;
                case CHAR_FIM:
                    return token(TipoToken.CARACTERE, inicio, lin, col);
                case CHAR_ERRO:
                    recuperarLiteral('\'');
                    return null;
            }
        }
    }

    private Token lerOperador() {
        int inicio = indice, lin = linha, col = coluna;
        EstadoOperador estado = EstadoOperador.OP_INICIO;
        while (true) {
            switch (estado) {
                case OP_INICIO:
                    int primeiro = advance();
                    if ("=!<>".indexOf(primeiro) >= 0) estado = EstadoOperador.OP_OPCIONAL;
                    else if (primeiro == '&') estado = EstadoOperador.OP_E;
                    else if (primeiro == '|') estado = EstadoOperador.OP_OU;
                    else if (primeiro == '/') estado = EstadoOperador.OP_BARRA;
                    else estado = EstadoOperador.OP_FIM;
                    break;
                case OP_OPCIONAL:
                    if (peek() == '=') {
                        advance();
                        estado = EstadoOperador.OP_FIM;
                    } else return token(TipoToken.OPERADOR, inicio, lin, col);
                    break;
                case OP_E:
                    if (peek() == '&') {
                        advance();
                        estado = EstadoOperador.OP_FIM;
                    } else {
                        registrar("Operador & isolado; use &&.", lin, col);
                        return null;
                    }
                    break;
                case OP_OU:
                    if (peek() == '|') {
                        advance();
                        estado = EstadoOperador.OP_FIM;
                    } else {
                        registrar("Operador | isolado; use ||.", lin, col);
                        return null;
                    }
                    break;
                case OP_BARRA:
                    // A primeira barra ja foi consumida; peek olha a seguinte.
                    if (peek() == '/') {
                        advance();
                        estado = EstadoOperador.OP_COMENTARIO;
                    } else return token(TipoToken.OPERADOR, inicio, lin, col);
                    break;
                case OP_COMENTARIO:
                    if (peek() == -1 || quebra(peek())) return null;
                    advance();
                    break;
                case OP_FIM:
                    return token(TipoToken.OPERADOR, inicio, lin, col);
            }
        }
    }

    private Token lerDelimitador() {
        int inicio = indice, lin = linha, col = coluna;
        EstadoDelimitador estado = EstadoDelimitador.DEL_INICIO;
        while (true) {
            switch (estado) {
                case DEL_INICIO:
                    advance();
                    estado = EstadoDelimitador.DEL_FIM;
                    break;
                case DEL_FIM:
                    return token(TipoToken.DELIMITADOR, inicio, lin, col);
            }
        }
    }

    private void ignorarSeparadores() {
        EstadoIgnorado estado = EstadoIgnorado.IGN_INICIO;
        while (true) {
            switch (estado) {
                case IGN_INICIO:
                    if (espaco(peek())) estado = EstadoIgnorado.IGN_ESPACO;
                    else return;
                    break;
                case IGN_ESPACO:
                    if (espaco(peek())) advance();
                    else estado = EstadoIgnorado.IGN_INICIO;
                    break;
            }
        }
    }

    private void recuperarLiteral(char aspa) {
        while (hasNext() && !quebra(peek())) {
            if (advance() == aspa) return;
        }
    }

    private Token token(TipoToken tipo, int inicio, int lin, int col) {
        return new Token(tipo, fonte.substring(inicio, indice), lin, col);
    }

    private void registrar(String mensagem, int lin, int col) {
        erros.add("Erro lexico em " + lin + ":" + col + " - " + mensagem);
    }

    private static boolean letra(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    private static boolean digito(int c) { return c >= '0' && c <= '9'; }
    private static boolean quebra(int c) { return c == '\n' || c == '\r'; }
    private static boolean espaco(int c) { return c == ' ' || c == '\t' || quebra(c); }

    private static String mostrar(int c) {
        if (c == -1) return "EOF";
        if (c == '\t') return "tabulacao";
        if (c == '\n') return "quebra de linha";
        if (c == '\r') return "retorno de carro";
        if (Character.isISOControl(c)) return String.format("U+%04X", c);
        return "'" + (char) c + "'";
    }
}
