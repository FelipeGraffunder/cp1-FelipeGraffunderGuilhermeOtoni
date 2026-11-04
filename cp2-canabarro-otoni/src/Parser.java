import java.util.ArrayList;
import java.util.List;

/** Reconhecedor LL(1) do CP2. Uma instância analisa uma única fonte. */
public final class Parser {
    private final Scanner scanner;
    private Token lookahead;
    private Token ultimoTokenComErro;
    private final List<String> erros = new ArrayList<>();
    private Boolean resultado;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.lookahead = scanner.nextToken();
    }

    public boolean parse() {
        if (resultado != null) return resultado;
        parsePrograma();
        if (!match("EOF")) sincronizar(FirstFollow.FOLLOW_PROGRAMA);
        erros.addAll(scanner.getErros());
        resultado = erros.isEmpty();
        return resultado;
    }

    public List<String> getErros() { return List.copyOf(erros); }

    // Categoria antes do lexema: uma string "if" nunca é a palavra reservada if.
    private String terminal() {
        if (lookahead.tipo == TipoToken.PALAVRA_RESERVADA
                || lookahead.tipo == TipoToken.OPERADOR
                || lookahead.tipo == TipoToken.DELIMITADOR) {
            return lookahead.lexema;
        }
        return lookahead.tipo.name();
    }

    private boolean em(List<String> conjunto) { return conjunto.contains(terminal()); }
    private void avancar() {
        if (lookahead.tipo != TipoToken.EOF) lookahead = scanner.nextToken();
    }

    // Em caso de erro, registra a mensagem e devolve false ao chamador.
    private boolean match(String esperado) {
        if (terminal().equals(esperado)) {
            avancar();
            return true;
        }
        erro("match", "esperava " + nomeTerminal(esperado));
        return false;
    }

    private String nomeTerminal(String simbolo) {
        switch (simbolo) {
            case "IDENTIFICADOR": return "identificador";
            case "NUMERO": return "numero";
            case "STRING": return "literal de texto";
            case "CARACTERE": return "literal de caractere";
            case "EOF": return "fim do arquivo";
            case "(": return "abertura de parenteses";
            case ")": return "fechamento de parenteses";
            case "{": return "abertura de bloco";
            case "}": return "fechamento de bloco";
            case ";": return "ponto e virgula";
            case ",": return "virgula";
            case "=": return "operador de atribuicao";
            case "+": return "operador de adicao";
            case "-": return "operador de subtracao";
            case "*": return "operador de multiplicacao";
            case "/": return "operador de divisao";
            case "%": return "operador de resto";
            case "!": return "operador de negacao logica";
            case "&&": return "operador logico E";
            case "||": return "operador logico OU";
            case "==": return "operador de igualdade";
            case "!=": return "operador de diferenca";
            case "<": return "operador menor que";
            case ">": return "operador maior que";
            case "<=": return "operador menor ou igual";
            case ">=": return "operador maior ou igual";
            default: return "palavra reservada " + simbolo;
        }
    }

    private void erro(String regra, String mensagem) {
        // Os chamadores podem reencontrar o mesmo token durante a recuperacao.
        if (lookahead == ultimoTokenComErro) return;
        ultimoTokenComErro = lookahead;
        erros.add("Erro sintatico em " + lookahead.linha + ":" + lookahead.coluna
            + " [" + regra + "] - " + mensagem + ", encontrou " + lookahead);
    }

    private void sincronizar(List<String> sync) {
        while (!em(sync) && lookahead.tipo != TipoToken.EOF) {
            avancar();
        }
    }

    private void recuperar(List<String> follow) {
        List<String> sync = new ArrayList<>(follow);
        sync.addAll(FirstFollow.ANCORAS);
        sincronizar(sync);
    }

    // <Programa> ::= <Itens>
    private void parsePrograma() {
        parseItens();
    }

    // <Itens> ::= <Item> <Itens> | ε
    private void parseItens() {
        while (true) {
            if (em(FirstFollow.FIRST_ITENS)) {
                parseItem();
            } else if (em(FirstFollow.FOLLOW_ITENS)) {
                return; // epsilon somente em FOLLOW(Itens)
            } else {
                erro("Itens", "esperava declaracao, comando ou fim do arquivo");
                sincronizar(FirstFollow.SYNC_ITENS);
                if (lookahead.tipo == TipoToken.EOF) return;
            }
        }
    }

    // <Item> ::= <Tipo> "IDENTIFICADOR" <RestoItem> | "void" "IDENTIFICADOR" <Funcao> | <Comando>
    private void parseItem() {
        if (em(FirstFollow.FIRST_TIPO)) {
            parseTipo();
            if (!match("IDENTIFICADOR")) {
                recuperar(FirstFollow.FOLLOW_ITEM);
                return;
            }
            parseRestoItem();
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_VOID)) {
            match("void");
            if (!match("IDENTIFICADOR")) {
                recuperar(FirstFollow.FOLLOW_ITEM);
                return;
            }
            parseFuncao();
            return;
        }
        else if (em(FirstFollow.FIRST_COMANDO)) {
            parseComando();
            return;
        }
        else {
            erro("Item", "esperava declaracao, funcao ou comando");
            recuperar(FirstFollow.FOLLOW_ITEM);
            return;
        }
    }

    // <Tipo> ::= "int" | "double" | "bool" | "char" | "string"
    private void parseTipo() {
        if (em(FirstFollow.PALAVRA_RESERVADA_INT)) {
            match("int");
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_DOUBLE)) {
            match("double");
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_BOOL)) {
            match("bool");
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_CHAR)) {
            match("char");
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_STRING)) {
            match("string");
            return;
        }
        else {
            erro("Tipo", "esperava tipo de dado");
            recuperar(FirstFollow.FOLLOW_TIPO);
            return;
        }
    }

    // <RestoItem> ::= <Funcao> | <Inicializacao> ";"
    private void parseRestoItem() {
        if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            parseFuncao();
            return;
        }
        else if (em(FirstFollow.SELECT_RESTO_ITEM_2)) {
            parseInicializacao();
            if (!match(";")) {
                recuperar(FirstFollow.FOLLOW_RESTO_ITEM);
                return;
            }
            return;
        }
        else {
            erro("RestoItem", "esperava definicao de funcao, inicializacao ou fim de declaracao");
            recuperar(FirstFollow.FOLLOW_RESTO_ITEM);
            return;
        }
    }

    // <Inicializacao> ::= "=" <Expressao> | ε
    private void parseInicializacao() {
        if (em(FirstFollow.OPERADOR_ATRIBUICAO)) {
            match("=");
            parseExpressao();
            return;
        }
        else if (em(FirstFollow.FOLLOW_INICIALIZACAO)) {
            // epsilon: lookahead pertence a FOLLOW(Inicializacao).
            return;
        }
        else {
            erro("Inicializacao", "esperava operador de atribuicao ou ponto e virgula");
            recuperar(FirstFollow.FOLLOW_INICIALIZACAO);
            return;
        }
    }

    // <Funcao> ::= "(" <Parametros> ")" <Bloco>
    private void parseFuncao() {
        if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            match("(");
            parseParametros();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_FUNCAO);
                return;
            }
            parseBloco();
            return;
        }
        else {
            erro("Funcao", "esperava abertura de parenteses");
            recuperar(FirstFollow.FOLLOW_FUNCAO);
            return;
        }
    }

    // <Parametros> ::= <Parametro> <MaisParametros> | ε
    private void parseParametros() {
        if (em(FirstFollow.FIRST_PARAMETROS)) {
            parseParametro();
            parseMaisParametros();
            return;
        }
        else if (em(FirstFollow.FOLLOW_PARAMETROS)) {
            // epsilon: lookahead pertence a FOLLOW(Parametros).
            return;
        }
        else {
            erro("Parametros", "esperava parametro ou fechamento de parenteses");
            recuperar(FirstFollow.FOLLOW_PARAMETROS);
            return;
        }
    }

    // <Parametro> ::= <Tipo> "IDENTIFICADOR"
    private void parseParametro() {
        if (em(FirstFollow.FIRST_PARAMETRO)) {
            parseTipo();
            if (!match("IDENTIFICADOR")) {
                recuperar(FirstFollow.FOLLOW_PARAMETRO);
                return;
            }
            return;
        }
        else {
            erro("Parametro", "esperava tipo do parametro");
            recuperar(FirstFollow.FOLLOW_PARAMETRO);
            return;
        }
    }

    // <MaisParametros> ::= "," <Parametro> <MaisParametros> | ε
    private void parseMaisParametros() {
        while (true) {
            if (em(FirstFollow.DELIMITADOR_VIRGULA)) {
                match(",");
                parseParametro();
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_PARAMETROS)) {
                // epsilon: lookahead pertence a FOLLOW(MaisParametros).
                return;
            }
            else {
                erro("MaisParametros", "esperava virgula ou fechamento de parenteses");
                recuperar(FirstFollow.FOLLOW_MAIS_PARAMETROS);
                return;
            }
        }
    }

    // <Bloco> ::= "{" <Elementos> "}"
    private void parseBloco() {
        if (em(FirstFollow.DELIMITADOR_ABRE_BLOCO)) {
            match("{");
            parseElementos();
            if (!match("}")) {
                recuperar(FirstFollow.FOLLOW_BLOCO);
                return;
            }
            return;
        }
        else {
            erro("Bloco", "esperava abertura de bloco");
            recuperar(FirstFollow.FOLLOW_BLOCO);
            return;
        }
    }

    // <Elementos> ::= <Elemento> <Elementos> | ε
    private void parseElementos() {
        while (true) {
            if (em(FirstFollow.FIRST_ELEMENTOS)) {
                parseElemento();
            } else if (em(FirstFollow.FOLLOW_ELEMENTOS)) {
                return; // epsilon somente em FOLLOW(Elementos)
            } else {
                erro("Elementos", "esperava declaracao, comando ou fechamento de bloco");
                sincronizar(FirstFollow.SYNC_ELEMENTOS);
                if (lookahead.tipo == TipoToken.EOF) return;
            }
        }
    }

    // <Elemento> ::= <Tipo> "IDENTIFICADOR" <Inicializacao> ";" | <Comando>
    private void parseElemento() {
        if (em(FirstFollow.FIRST_TIPO)) {
            parseTipo();
            if (!match("IDENTIFICADOR")) {
                recuperar(FirstFollow.FOLLOW_ELEMENTO);
                return;
            }
            parseInicializacao();
            if (!match(";")) {
                recuperar(FirstFollow.FOLLOW_ELEMENTO);
                return;
            }
            return;
        }
        else if (em(FirstFollow.FIRST_COMANDO)) {
            parseComando();
            return;
        }
        else {
            erro("Elemento", "esperava declaracao ou comando");
            recuperar(FirstFollow.FOLLOW_ELEMENTO);
            return;
        }
    }

    // <Comando> ::= "IDENTIFICADOR" <RestoComando> ";" | <Se> | <Enquanto> | <Retorno> | <Bloco>
    private void parseComando() {
        if (em(FirstFollow.IDENTIFICADOR)) {
            match("IDENTIFICADOR");
            parseRestoComando();
            if (!match(";")) {
                recuperar(FirstFollow.FOLLOW_COMANDO);
                return;
            }
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_IF)) {
            parseSe();
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_WHILE)) {
            parseEnquanto();
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_RETURN)) {
            parseRetorno();
            return;
        }
        else if (em(FirstFollow.DELIMITADOR_ABRE_BLOCO)) {
            parseBloco();
            return;
        }
        else {
            erro("Comando", "esperava comando");
            recuperar(FirstFollow.FOLLOW_COMANDO);
            return;
        }
    }

    // <RestoComando> ::= "=" <Expressao> | "(" <Argumentos> ")"
    private void parseRestoComando() {
        if (em(FirstFollow.OPERADOR_ATRIBUICAO)) {
            match("=");
            parseExpressao();
            return;
        }
        else if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            match("(");
            parseArgumentos();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_RESTO_COMANDO);
                return;
            }
            return;
        }
        else {
            erro("RestoComando", "esperava operador de atribuicao ou chamada de funcao");
            recuperar(FirstFollow.FOLLOW_RESTO_COMANDO);
            return;
        }
    }

    // <Se> ::= "if" "(" <Expressao> ")" <Bloco> <Senao>
    private void parseSe() {
        if (em(FirstFollow.PALAVRA_RESERVADA_IF)) {
            match("if");
            if (!match("(")) {
                recuperar(FirstFollow.FOLLOW_SE);
                return;
            }
            parseExpressao();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_SE);
                return;
            }
            parseBloco();
            parseSenao();
            return;
        }
        else {
            erro("Se", "esperava palavra reservada if");
            recuperar(FirstFollow.FOLLOW_SE);
            return;
        }
    }

    // <Senao> ::= "else" <Bloco> | ε
    private void parseSenao() {
        if (em(FirstFollow.PALAVRA_RESERVADA_ELSE)) {
            match("else");
            parseBloco();
            return;
        }
        else if (em(FirstFollow.FOLLOW_SENAO)) {
            // epsilon: lookahead pertence a FOLLOW(Senao).
            return;
        }
        else {
            erro("Senao", "esperava else, declaracao, comando ou fim do bloco/programa");
            recuperar(FirstFollow.FOLLOW_SENAO);
            return;
        }
    }

    // <Enquanto> ::= "while" "(" <Expressao> ")" <Bloco>
    private void parseEnquanto() {
        if (em(FirstFollow.PALAVRA_RESERVADA_WHILE)) {
            match("while");
            if (!match("(")) {
                recuperar(FirstFollow.FOLLOW_ENQUANTO);
                return;
            }
            parseExpressao();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_ENQUANTO);
                return;
            }
            parseBloco();
            return;
        }
        else {
            erro("Enquanto", "esperava palavra reservada while");
            recuperar(FirstFollow.FOLLOW_ENQUANTO);
            return;
        }
    }

    // <Retorno> ::= "return" <ValorRetorno> ";"
    private void parseRetorno() {
        if (em(FirstFollow.PALAVRA_RESERVADA_RETURN)) {
            match("return");
            parseValorRetorno();
            if (!match(";")) {
                recuperar(FirstFollow.FOLLOW_RETORNO);
                return;
            }
            return;
        }
        else {
            erro("Retorno", "esperava palavra reservada return");
            recuperar(FirstFollow.FOLLOW_RETORNO);
            return;
        }
    }

    // <ValorRetorno> ::= <Expressao> | ε
    private void parseValorRetorno() {
        if (em(FirstFollow.FIRST_VALOR_RETORNO)) {
            parseExpressao();
            return;
        }
        else if (em(FirstFollow.FOLLOW_VALOR_RETORNO)) {
            // epsilon: lookahead pertence a FOLLOW(ValorRetorno).
            return;
        }
        else {
            erro("ValorRetorno", "esperava expressao de retorno ou ponto e virgula");
            recuperar(FirstFollow.FOLLOW_VALOR_RETORNO);
            return;
        }
    }

    // <Argumentos> ::= <Expressao> <MaisArgumentos> | ε
    private void parseArgumentos() {
        if (em(FirstFollow.FIRST_ARGUMENTOS)) {
            parseExpressao();
            parseMaisArgumentos();
            return;
        }
        else if (em(FirstFollow.FOLLOW_ARGUMENTOS)) {
            // epsilon: lookahead pertence a FOLLOW(Argumentos).
            return;
        }
        else {
            erro("Argumentos", "esperava argumento ou fechamento de parenteses");
            recuperar(FirstFollow.FOLLOW_ARGUMENTOS);
            return;
        }
    }

    // <MaisArgumentos> ::= "," <Expressao> <MaisArgumentos> | ε
    private void parseMaisArgumentos() {
        while (true) {
            if (em(FirstFollow.DELIMITADOR_VIRGULA)) {
                match(",");
                parseExpressao();
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_ARGUMENTOS)) {
                // epsilon: lookahead pertence a FOLLOW(MaisArgumentos).
                return;
            }
            else {
                erro("MaisArgumentos", "esperava virgula ou fechamento de parenteses");
                recuperar(FirstFollow.FOLLOW_MAIS_ARGUMENTOS);
                return;
            }
        }
    }

    // <Expressao> ::= <Ou>
    private void parseExpressao() {
        if (em(FirstFollow.FIRST_EXPRESSAO)) {
            parseOu();
            return;
        }
        else {
            erro("Expressao", "esperava operando");
            recuperar(FirstFollow.FOLLOW_EXPRESSAO);
            return;
        }
    }

    // <Ou> ::= <E> <MaisOu>
    private void parseOu() {
        if (em(FirstFollow.FIRST_OU)) {
            parseE();
            parseMaisOu();
            return;
        }
        else {
            erro("Ou", "esperava operando");
            recuperar(FirstFollow.FOLLOW_OU);
            return;
        }
    }

    // <MaisOu> ::= "||" <E> <MaisOu> | ε
    private void parseMaisOu() {
        while (true) {
            if (em(FirstFollow.OPERADOR_OU)) {
                match("||");
                parseE();
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_OU)) {
                // epsilon: lookahead pertence a FOLLOW(MaisOu).
                return;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return;
            }
            else {
                erro("MaisOu", "esperava operador logico OU ou fim da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_OU);
                return;
            }
        }
    }

    // <E> ::= <Igualdade> <MaisE>
    private void parseE() {
        if (em(FirstFollow.FIRST_E)) {
            parseIgualdade();
            parseMaisE();
            return;
        }
        else {
            erro("E", "esperava operando");
            recuperar(FirstFollow.FOLLOW_E);
            return;
        }
    }

    // <MaisE> ::= "&&" <Igualdade> <MaisE> | ε
    private void parseMaisE() {
        while (true) {
            if (em(FirstFollow.OPERADOR_E)) {
                match("&&");
                parseIgualdade();
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_E)) {
                // epsilon: lookahead pertence a FOLLOW(MaisE).
                return;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return;
            }
            else {
                erro("MaisE", "esperava operador logico ou fim da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_E);
                return;
            }
        }
    }

    // <Igualdade> ::= <Relacional> <MaisIgualdade>
    private void parseIgualdade() {
        if (em(FirstFollow.FIRST_IGUALDADE)) {
            parseRelacional();
            parseMaisIgualdade();
            return;
        }
        else {
            erro("Igualdade", "esperava operando");
            recuperar(FirstFollow.FOLLOW_IGUALDADE);
            return;
        }
    }

    // <MaisIgualdade> ::= "==" <Relacional> <MaisIgualdade> | "!=" <Relacional> <MaisIgualdade> | ε
    private void parseMaisIgualdade() {
        while (true) {
            if (em(FirstFollow.OPERADOR_IGUALDADE)) {
                match("==");
                parseRelacional();
                continue;
            }
            else if (em(FirstFollow.OPERADOR_DIFERENCA)) {
                match("!=");
                parseRelacional();
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_IGUALDADE)) {
                // epsilon: lookahead pertence a FOLLOW(MaisIgualdade).
                return;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return;
            }
            else {
                erro("MaisIgualdade", "esperava operador de igualdade ou continuacao da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_IGUALDADE);
                return;
            }
        }
    }

    // <Relacional> ::= <Aditiva> <MaisRelacional>
    private void parseRelacional() {
        if (em(FirstFollow.FIRST_RELACIONAL)) {
            parseAditiva();
            parseMaisRelacional();
            return;
        }
        else {
            erro("Relacional", "esperava operando");
            recuperar(FirstFollow.FOLLOW_RELACIONAL);
            return;
        }
    }

    // <MaisRelacional> ::= "<" <Aditiva> <MaisRelacional> | ">" <Aditiva> <MaisRelacional> | "<=" <Aditiva> <MaisRelacional> | ">=" <Aditiva> <MaisRelacional> | ε
    private void parseMaisRelacional() {
        while (true) {
            if (em(FirstFollow.OPERADOR_MENOR)) {
                match("<");
                parseAditiva();
                continue;
            }
            else if (em(FirstFollow.OPERADOR_MAIOR)) {
                match(">");
                parseAditiva();
                continue;
            }
            else if (em(FirstFollow.OPERADOR_MENOR_IGUAL)) {
                match("<=");
                parseAditiva();
                continue;
            }
            else if (em(FirstFollow.OPERADOR_MAIOR_IGUAL)) {
                match(">=");
                parseAditiva();
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_RELACIONAL)) {
                // epsilon: lookahead pertence a FOLLOW(MaisRelacional).
                return;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return;
            }
            else {
                erro("MaisRelacional", "esperava operador relacional ou continuacao da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_RELACIONAL);
                return;
            }
        }
    }

    // <Aditiva> ::= <Multiplicativa> <MaisAditiva>
    private void parseAditiva() {
        if (em(FirstFollow.FIRST_ADITIVA)) {
            parseMultiplicativa();
            parseMaisAditiva();
            return;
        }
        else {
            erro("Aditiva", "esperava operando");
            recuperar(FirstFollow.FOLLOW_ADITIVA);
            return;
        }
    }

    // <MaisAditiva> ::= "+" <Multiplicativa> <MaisAditiva> | "-" <Multiplicativa> <MaisAditiva> | ε
    private void parseMaisAditiva() {
        while (true) {
            if (em(FirstFollow.OPERADOR_ADICAO)) {
                match("+");
                parseMultiplicativa();
                continue;
            }
            else if (em(FirstFollow.OPERADOR_SUBTRACAO)) {
                match("-");
                parseMultiplicativa();
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_ADITIVA)) {
                // epsilon: lookahead pertence a FOLLOW(MaisAditiva).
                return;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return;
            }
            else {
                erro("MaisAditiva", "esperava operador aritmetico ou continuacao da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_ADITIVA);
                return;
            }
        }
    }

    // <Multiplicativa> ::= <Unaria> <MaisMultiplicativa>
    private void parseMultiplicativa() {
        if (em(FirstFollow.FIRST_MULTIPLICATIVA)) {
            parseUnaria();
            parseMaisMultiplicativa();
            return;
        }
        else {
            erro("Multiplicativa", "esperava operando");
            recuperar(FirstFollow.FOLLOW_MULTIPLICATIVA);
            return;
        }
    }

    // <MaisMultiplicativa> ::= "*" <Unaria> <MaisMultiplicativa> | "/" <Unaria> <MaisMultiplicativa> | "%" <Unaria> <MaisMultiplicativa> | ε
    private void parseMaisMultiplicativa() {
        while (true) {
            if (em(FirstFollow.OPERADOR_MULTIPLICACAO)) {
                match("*");
                parseUnaria();
                continue;
            }
            else if (em(FirstFollow.OPERADOR_DIVISAO)) {
                match("/");
                parseUnaria();
                continue;
            }
            else if (em(FirstFollow.OPERADOR_RESTO)) {
                match("%");
                parseUnaria();
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_MULTIPLICATIVA)) {
                // epsilon: lookahead pertence a FOLLOW(MaisMultiplicativa).
                return;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return;
            }
            else {
                erro("MaisMultiplicativa", "esperava operador aritmetico ou continuacao da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_MULTIPLICATIVA);
                return;
            }
        }
    }

    // <Unaria> ::= "!" <Unaria> | "+" <Unaria> | "-" <Unaria> | <Primaria>
    private void parseUnaria() {
        while (true) {
            if (em(FirstFollow.OPERADOR_NEGACAO)) {
                match("!");
                continue;
            }
            else if (em(FirstFollow.OPERADOR_ADICAO)) {
                match("+");
                continue;
            }
            else if (em(FirstFollow.OPERADOR_SUBTRACAO)) {
                match("-");
                continue;
            }
            else if (em(FirstFollow.FIRST_PRIMARIA)) {
                parsePrimaria();
                return;
            }
            else {
                erro("Unaria", "esperava operando");
                recuperar(FirstFollow.FOLLOW_UNARIA);
                return;
            }
        }
    }

    // <Primaria> ::= "IDENTIFICADOR" <ChamadaOpcional> | "NUMERO" | "STRING" | "CARACTERE" | "true" | "false" | "(" <Expressao> ")"
    private void parsePrimaria() {
        if (em(FirstFollow.IDENTIFICADOR)) {
            match("IDENTIFICADOR");
            parseChamadaOpcional();
            return;
        }
        else if (em(FirstFollow.NUMERO)) {
            match("NUMERO");
            return;
        }
        else if (em(FirstFollow.STRING)) {
            match("STRING");
            return;
        }
        else if (em(FirstFollow.CARACTERE)) {
            match("CARACTERE");
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_TRUE)) {
            match("true");
            return;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_FALSE)) {
            match("false");
            return;
        }
        else if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            match("(");
            parseExpressao();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_PRIMARIA);
                return;
            }
            return;
        }
        else {
            erro("Primaria", "esperava operando");
            recuperar(FirstFollow.FOLLOW_PRIMARIA);
            return;
        }
    }

    // <ChamadaOpcional> ::= "(" <Argumentos> ")" | ε
    private void parseChamadaOpcional() {
        if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            match("(");
            parseArgumentos();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_CHAMADA_OPCIONAL);
                return;
            }
            return;
        }
        else if (em(FirstFollow.FOLLOW_CHAMADA_OPCIONAL)) {
            // epsilon: lookahead pertence a FOLLOW(ChamadaOpcional).
            return;
        }
        else {
            erro("ChamadaOpcional", "esperava chamada de funcao ou continuacao da expressao");
            recuperar(FirstFollow.FOLLOW_CHAMADA_OPCIONAL);
            return;
        }
    }
}
