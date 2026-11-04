import ast.*;
import java.util.ArrayList;
import java.util.List;

public final class Parser {
    private final Scanner scanner;
    private Token lookahead;
    private Token ultimoTokenComErro;
    private final List<String> erros = new ArrayList<>();
    private ResultadoParse resultado;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.lookahead = scanner.nextToken();
    }

    public ResultadoParse parse() {
        if (resultado != null) return resultado;
        Programa arvore = parsePrograma();
        if (!match("EOF")) sincronizar(FirstFollow.FOLLOW_PROGRAMA);
        erros.addAll(scanner.getErros());
        resultado = new ResultadoParse(erros.isEmpty() ? arvore : null, erros);
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
    private Programa parsePrograma() {
        Programa r = null;
        Token inicio = lookahead;
        r = new Programa(inicio.linha, inicio.coluna, parseItens());
        return r;
    }

    // <Itens> ::= <Item> <Itens> | ε
    private List<Node> parseItens() {
        List<Node> r = new ArrayList<>();
        while (true) {
            if (em(FirstFollow.FIRST_ITENS)) {
                Node n = parseItem();
                if (n != null) r.add(n);
            } else if (em(FirstFollow.FOLLOW_ITENS)) {
                return r; // epsilon somente em FOLLOW(Itens)
            } else {
                erro("Itens", "esperava declaracao, comando ou fim do arquivo");
                sincronizar(FirstFollow.SYNC_ITENS);
                if (lookahead.tipo == TipoToken.EOF) return r;
            }
        }
    }

    // <Item> ::= <Tipo> "IDENTIFICADOR" <RestoItem> | "void" "IDENTIFICADOR" <Funcao> | <Comando>
    private Node parseItem() {
        Node r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_TIPO)) {
            Tipo tipo = parseTipo();
            Token nome = lookahead;
            if (!match("IDENTIFICADOR")) {
                recuperar(FirstFollow.FOLLOW_ITEM);
                return r;
            }
            r = parseRestoItem(inicio, tipo, nome);
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_VOID)) {
            match("void");
            Token nome = lookahead;
            if (!match("IDENTIFICADOR")) {
                recuperar(FirstFollow.FOLLOW_ITEM);
                return r;
            }
            r = parseFuncao(inicio, null, nome);
            return r;
        }
        else if (em(FirstFollow.FIRST_COMANDO)) {
            r = parseComando();
            return r;
        }
        else {
            erro("Item", "esperava declaracao, funcao ou comando");
            recuperar(FirstFollow.FOLLOW_ITEM);
            return r;
        }
    }

    // <Tipo> ::= "int" | "double" | "bool" | "char" | "string"
    private Tipo parseTipo() {
        Tipo r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.PALAVRA_RESERVADA_INT)) {
            match("int");
            r = new Tipo(inicio.linha,inicio.coluna,inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_DOUBLE)) {
            match("double");
            r = new Tipo(inicio.linha,inicio.coluna,inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_BOOL)) {
            match("bool");
            r = new Tipo(inicio.linha,inicio.coluna,inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_CHAR)) {
            match("char");
            r = new Tipo(inicio.linha,inicio.coluna,inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_STRING)) {
            match("string");
            r = new Tipo(inicio.linha,inicio.coluna,inicio.lexema);
            return r;
        }
        else {
            erro("Tipo", "esperava tipo de dado");
            recuperar(FirstFollow.FOLLOW_TIPO);
            return r;
        }
    }

    // <RestoItem> ::= <Funcao> | <Inicializacao> ";"
    private Node parseRestoItem(Token inicio, Tipo tipo, Token nome) {
        Node r = null;
        if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            r = parseFuncao(inicio, tipo, nome);
            return r;
        }
        else if (em(FirstFollow.SELECT_RESTO_ITEM_2)) {
            Expr valor = parseInicializacao();
            r = new Variavel(inicio.linha,inicio.coluna,tipo,nome.lexema,valor);
            if (!match(";")) {
                recuperar(FirstFollow.FOLLOW_RESTO_ITEM);
                return r;
            }
            return r;
        }
        else {
            erro("RestoItem", "esperava definicao de funcao, inicializacao ou fim de declaracao");
            recuperar(FirstFollow.FOLLOW_RESTO_ITEM);
            return r;
        }
    }

    // <Inicializacao> ::= "=" <Expressao> | ε
    private Expr parseInicializacao() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.OPERADOR_ATRIBUICAO)) {
            match("=");
            r = parseExpressao();
            return r;
        }
        else if (em(FirstFollow.FOLLOW_INICIALIZACAO)) {
            // epsilon: lookahead pertence a FOLLOW(Inicializacao).
            return r;
        }
        else {
            erro("Inicializacao", "esperava operador de atribuicao ou ponto e virgula");
            recuperar(FirstFollow.FOLLOW_INICIALIZACAO);
            return r;
        }
    }

    // <Funcao> ::= "(" <Parametros> ")" <Bloco>
    private Node parseFuncao(Token inicio, Tipo tipo, Token nome) {
        Node r = null;
        if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            match("(");
            List<Parametro> parametros = parseParametros();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_FUNCAO);
                return r;
            }
            Bloco corpo = parseBloco();
            r = tipo == null ? new Procedimento(inicio.linha,inicio.coluna,nome.lexema,parametros,corpo)
                : new Funcao(inicio.linha,inicio.coluna,tipo,nome.lexema,parametros,corpo);
            return r;
        }
        else {
            erro("Funcao", "esperava abertura de parenteses");
            recuperar(FirstFollow.FOLLOW_FUNCAO);
            return r;
        }
    }

    // <Parametros> ::= <Parametro> <MaisParametros> | ε
    private List<Parametro> parseParametros() {
        List<Parametro> r = new ArrayList<>();
        if (em(FirstFollow.FIRST_PARAMETROS)) {
            Parametro p = parseParametro();
            if(p != null) r.add(p);
            r.addAll(parseMaisParametros());
            return r;
        }
        else if (em(FirstFollow.FOLLOW_PARAMETROS)) {
            // epsilon: lookahead pertence a FOLLOW(Parametros).
            return r;
        }
        else {
            erro("Parametros", "esperava parametro ou fechamento de parenteses");
            recuperar(FirstFollow.FOLLOW_PARAMETROS);
            return r;
        }
    }

    // <Parametro> ::= <Tipo> "IDENTIFICADOR"
    private Parametro parseParametro() {
        Parametro r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_PARAMETRO)) {
            Tipo tipo = parseTipo();
            Token nome = lookahead;
            if (!match("IDENTIFICADOR")) {
                recuperar(FirstFollow.FOLLOW_PARAMETRO);
                return r;
            }
            r = new Parametro(inicio.linha,inicio.coluna,tipo,nome.lexema);
            return r;
        }
        else {
            erro("Parametro", "esperava tipo do parametro");
            recuperar(FirstFollow.FOLLOW_PARAMETRO);
            return r;
        }
    }

    // <MaisParametros> ::= "," <Parametro> <MaisParametros> | ε
    private List<Parametro> parseMaisParametros() {
        List<Parametro> r = new ArrayList<>();
        while (true) {
            if (em(FirstFollow.DELIMITADOR_VIRGULA)) {
                match(",");
                Parametro p = parseParametro();
            if(p != null) r.add(p);
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_PARAMETROS)) {
                // epsilon: lookahead pertence a FOLLOW(MaisParametros).
                return r;
            }
            else {
                erro("MaisParametros", "esperava virgula ou fechamento de parenteses");
                recuperar(FirstFollow.FOLLOW_MAIS_PARAMETROS);
                return r;
            }
        }
    }

    // <Bloco> ::= "{" <Elementos> "}"
    private Bloco parseBloco() {
        Bloco r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.DELIMITADOR_ABRE_BLOCO)) {
            match("{");
            r = new Bloco(inicio.linha,inicio.coluna,parseElementos());
            if (!match("}")) {
                recuperar(FirstFollow.FOLLOW_BLOCO);
                return r;
            }
            return r;
        }
        else {
            erro("Bloco", "esperava abertura de bloco");
            recuperar(FirstFollow.FOLLOW_BLOCO);
            return r;
        }
    }

    // <Elementos> ::= <Elemento> <Elementos> | ε
    private List<Node> parseElementos() {
        List<Node> r = new ArrayList<>();
        while (true) {
            if (em(FirstFollow.FIRST_ELEMENTOS)) {
                Node n = parseElemento();
                if (n != null) r.add(n);
            } else if (em(FirstFollow.FOLLOW_ELEMENTOS)) {
                return r; // epsilon somente em FOLLOW(Elementos)
            } else {
                erro("Elementos", "esperava declaracao, comando ou fechamento de bloco");
                sincronizar(FirstFollow.SYNC_ELEMENTOS);
                if (lookahead.tipo == TipoToken.EOF) return r;
            }
        }
    }

    // <Elemento> ::= <Tipo> "IDENTIFICADOR" <Inicializacao> ";" | <Comando>
    private Node parseElemento() {
        Node r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_TIPO)) {
            Tipo tipo = parseTipo();
            Token nome = lookahead;
            if (!match("IDENTIFICADOR")) {
                recuperar(FirstFollow.FOLLOW_ELEMENTO);
                return r;
            }
            r = new Variavel(inicio.linha,inicio.coluna,tipo,nome.lexema,parseInicializacao());
            if (!match(";")) {
                recuperar(FirstFollow.FOLLOW_ELEMENTO);
                return r;
            }
            return r;
        }
        else if (em(FirstFollow.FIRST_COMANDO)) {
            r = parseComando();
            return r;
        }
        else {
            erro("Elemento", "esperava declaracao ou comando");
            recuperar(FirstFollow.FOLLOW_ELEMENTO);
            return r;
        }
    }

    // <Comando> ::= "IDENTIFICADOR" <RestoComando> ";" | <Se> | <Enquanto> | <Retorno> | <Bloco>
    private Comando parseComando() {
        Comando r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.IDENTIFICADOR)) {
            match("IDENTIFICADOR");
            r = parseRestoComando(inicio);
            if (!match(";")) {
                recuperar(FirstFollow.FOLLOW_COMANDO);
                return r;
            }
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_IF)) {
            r = parseSe();
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_WHILE)) {
            r = parseEnquanto();
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_RETURN)) {
            r = parseRetorno();
            return r;
        }
        else if (em(FirstFollow.DELIMITADOR_ABRE_BLOCO)) {
            r = parseBloco();
            return r;
        }
        else {
            erro("Comando", "esperava comando");
            recuperar(FirstFollow.FOLLOW_COMANDO);
            return r;
        }
    }

    // <RestoComando> ::= "=" <Expressao> | "(" <Argumentos> ")"
    private Comando parseRestoComando(Token inicio) {
        Comando r = null;
        if (em(FirstFollow.OPERADOR_ATRIBUICAO)) {
            match("=");
            r = new Atribuicao(inicio.linha,inicio.coluna,new Id(inicio.linha,inicio.coluna,inicio.lexema),parseExpressao());
            return r;
        }
        else if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            match("(");
            r = new ChamadaCmd(inicio.linha,inicio.coluna,new Chamada(inicio.linha,inicio.coluna,new Id(inicio.linha,inicio.coluna,inicio.lexema),parseArgumentos()));
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_RESTO_COMANDO);
                return r;
            }
            return r;
        }
        else {
            erro("RestoComando", "esperava operador de atribuicao ou chamada de funcao");
            recuperar(FirstFollow.FOLLOW_RESTO_COMANDO);
            return r;
        }
    }

    // <Se> ::= "if" "(" <Expressao> ")" <Bloco> <Senao>
    private Se parseSe() {
        Se r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.PALAVRA_RESERVADA_IF)) {
            match("if");
            if (!match("(")) {
                recuperar(FirstFollow.FOLLOW_SE);
                return r;
            }
            Expr condicao = parseExpressao();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_SE);
                return r;
            }
            Bloco entao = parseBloco();
            r = new Se(inicio.linha,inicio.coluna,condicao,entao,parseSenao());
            return r;
        }
        else {
            erro("Se", "esperava palavra reservada if");
            recuperar(FirstFollow.FOLLOW_SE);
            return r;
        }
    }

    // <Senao> ::= "else" <Bloco> | ε
    private Bloco parseSenao() {
        Bloco r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.PALAVRA_RESERVADA_ELSE)) {
            match("else");
            r = parseBloco();
            return r;
        }
        else if (em(FirstFollow.FOLLOW_SENAO)) {
            // epsilon: lookahead pertence a FOLLOW(Senao).
            return r;
        }
        else {
            erro("Senao", "esperava else, declaracao, comando ou fim do bloco/programa");
            recuperar(FirstFollow.FOLLOW_SENAO);
            return r;
        }
    }

    // <Enquanto> ::= "while" "(" <Expressao> ")" <Bloco>
    private Enquanto parseEnquanto() {
        Enquanto r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.PALAVRA_RESERVADA_WHILE)) {
            match("while");
            if (!match("(")) {
                recuperar(FirstFollow.FOLLOW_ENQUANTO);
                return r;
            }
            Expr condicao = parseExpressao();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_ENQUANTO);
                return r;
            }
            r = new Enquanto(inicio.linha,inicio.coluna,condicao,parseBloco());
            return r;
        }
        else {
            erro("Enquanto", "esperava palavra reservada while");
            recuperar(FirstFollow.FOLLOW_ENQUANTO);
            return r;
        }
    }

    // <Retorno> ::= "return" <ValorRetorno> ";"
    private Retorno parseRetorno() {
        Retorno r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.PALAVRA_RESERVADA_RETURN)) {
            match("return");
            r = new Retorno(inicio.linha,inicio.coluna,parseValorRetorno());
            if (!match(";")) {
                recuperar(FirstFollow.FOLLOW_RETORNO);
                return r;
            }
            return r;
        }
        else {
            erro("Retorno", "esperava palavra reservada return");
            recuperar(FirstFollow.FOLLOW_RETORNO);
            return r;
        }
    }

    // <ValorRetorno> ::= <Expressao> | ε
    private Expr parseValorRetorno() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_VALOR_RETORNO)) {
            r = parseExpressao();
            return r;
        }
        else if (em(FirstFollow.FOLLOW_VALOR_RETORNO)) {
            // epsilon: lookahead pertence a FOLLOW(ValorRetorno).
            return r;
        }
        else {
            erro("ValorRetorno", "esperava expressao de retorno ou ponto e virgula");
            recuperar(FirstFollow.FOLLOW_VALOR_RETORNO);
            return r;
        }
    }

    // <Argumentos> ::= <Expressao> <MaisArgumentos> | ε
    private List<Expr> parseArgumentos() {
        List<Expr> r = new ArrayList<>();
        if (em(FirstFollow.FIRST_ARGUMENTOS)) {
            Expr e = parseExpressao();
            if(e != null) r.add(e);
            r.addAll(parseMaisArgumentos());
            return r;
        }
        else if (em(FirstFollow.FOLLOW_ARGUMENTOS)) {
            // epsilon: lookahead pertence a FOLLOW(Argumentos).
            return r;
        }
        else {
            erro("Argumentos", "esperava argumento ou fechamento de parenteses");
            recuperar(FirstFollow.FOLLOW_ARGUMENTOS);
            return r;
        }
    }

    // <MaisArgumentos> ::= "," <Expressao> <MaisArgumentos> | ε
    private List<Expr> parseMaisArgumentos() {
        List<Expr> r = new ArrayList<>();
        while (true) {
            if (em(FirstFollow.DELIMITADOR_VIRGULA)) {
                match(",");
                Expr e = parseExpressao();
            if(e != null) r.add(e);
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_ARGUMENTOS)) {
                // epsilon: lookahead pertence a FOLLOW(MaisArgumentos).
                return r;
            }
            else {
                erro("MaisArgumentos", "esperava virgula ou fechamento de parenteses");
                recuperar(FirstFollow.FOLLOW_MAIS_ARGUMENTOS);
                return r;
            }
        }
    }

    // <Expressao> ::= <Ou>
    private Expr parseExpressao() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_EXPRESSAO)) {
            r = parseOu();
            return r;
        }
        else {
            erro("Expressao", "esperava operando");
            recuperar(FirstFollow.FOLLOW_EXPRESSAO);
            return r;
        }
    }

    // <Ou> ::= <E> <MaisOu>
    private Expr parseOu() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_OU)) {
            r = parseE();
            r = parseMaisOu(r);
            return r;
        }
        else {
            erro("Ou", "esperava operando");
            recuperar(FirstFollow.FOLLOW_OU);
            return r;
        }
    }

    // <MaisOu> ::= "||" <E> <MaisOu> | ε
    private Expr parseMaisOu(Expr r) {
        while (true) {
            if (em(FirstFollow.OPERADOR_OU)) {
                Token op = lookahead;
                match("||");
                Expr direita = parseE();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_OU)) {
                // epsilon: lookahead pertence a FOLLOW(MaisOu).
                return r;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return r;
            }
            else {
                erro("MaisOu", "esperava operador logico OU ou fim da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_OU);
                return r;
            }
        }
    }

    // <E> ::= <Igualdade> <MaisE>
    private Expr parseE() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_E)) {
            r = parseIgualdade();
            r = parseMaisE(r);
            return r;
        }
        else {
            erro("E", "esperava operando");
            recuperar(FirstFollow.FOLLOW_E);
            return r;
        }
    }

    // <MaisE> ::= "&&" <Igualdade> <MaisE> | ε
    private Expr parseMaisE(Expr r) {
        while (true) {
            if (em(FirstFollow.OPERADOR_E)) {
                Token op = lookahead;
                match("&&");
                Expr direita = parseIgualdade();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_E)) {
                // epsilon: lookahead pertence a FOLLOW(MaisE).
                return r;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return r;
            }
            else {
                erro("MaisE", "esperava operador logico ou fim da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_E);
                return r;
            }
        }
    }

    // <Igualdade> ::= <Relacional> <MaisIgualdade>
    private Expr parseIgualdade() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_IGUALDADE)) {
            r = parseRelacional();
            r = parseMaisIgualdade(r);
            return r;
        }
        else {
            erro("Igualdade", "esperava operando");
            recuperar(FirstFollow.FOLLOW_IGUALDADE);
            return r;
        }
    }

    // <MaisIgualdade> ::= "==" <Relacional> <MaisIgualdade> | "!=" <Relacional> <MaisIgualdade> | ε
    private Expr parseMaisIgualdade(Expr r) {
        while (true) {
            if (em(FirstFollow.OPERADOR_IGUALDADE)) {
                Token op = lookahead;
                match("==");
                Expr direita = parseRelacional();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.OPERADOR_DIFERENCA)) {
                Token op = lookahead;
                match("!=");
                Expr direita = parseRelacional();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_IGUALDADE)) {
                // epsilon: lookahead pertence a FOLLOW(MaisIgualdade).
                return r;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return r;
            }
            else {
                erro("MaisIgualdade", "esperava operador de igualdade ou continuacao da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_IGUALDADE);
                return r;
            }
        }
    }

    // <Relacional> ::= <Aditiva> <MaisRelacional>
    private Expr parseRelacional() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_RELACIONAL)) {
            r = parseAditiva();
            r = parseMaisRelacional(r);
            return r;
        }
        else {
            erro("Relacional", "esperava operando");
            recuperar(FirstFollow.FOLLOW_RELACIONAL);
            return r;
        }
    }

    // <MaisRelacional> ::= "<" <Aditiva> <MaisRelacional> | ">" <Aditiva> <MaisRelacional> | "<=" <Aditiva> <MaisRelacional> | ">=" <Aditiva> <MaisRelacional> | ε
    private Expr parseMaisRelacional(Expr r) {
        while (true) {
            if (em(FirstFollow.OPERADOR_MENOR)) {
                Token op = lookahead;
                match("<");
                Expr direita = parseAditiva();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.OPERADOR_MAIOR)) {
                Token op = lookahead;
                match(">");
                Expr direita = parseAditiva();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.OPERADOR_MENOR_IGUAL)) {
                Token op = lookahead;
                match("<=");
                Expr direita = parseAditiva();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.OPERADOR_MAIOR_IGUAL)) {
                Token op = lookahead;
                match(">=");
                Expr direita = parseAditiva();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_RELACIONAL)) {
                // epsilon: lookahead pertence a FOLLOW(MaisRelacional).
                return r;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return r;
            }
            else {
                erro("MaisRelacional", "esperava operador relacional ou continuacao da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_RELACIONAL);
                return r;
            }
        }
    }

    // <Aditiva> ::= <Multiplicativa> <MaisAditiva>
    private Expr parseAditiva() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_ADITIVA)) {
            r = parseMultiplicativa();
            r = parseMaisAditiva(r);
            return r;
        }
        else {
            erro("Aditiva", "esperava operando");
            recuperar(FirstFollow.FOLLOW_ADITIVA);
            return r;
        }
    }

    // <MaisAditiva> ::= "+" <Multiplicativa> <MaisAditiva> | "-" <Multiplicativa> <MaisAditiva> | ε
    private Expr parseMaisAditiva(Expr r) {
        while (true) {
            if (em(FirstFollow.OPERADOR_ADICAO)) {
                Token op = lookahead;
                match("+");
                Expr direita = parseMultiplicativa();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.OPERADOR_SUBTRACAO)) {
                Token op = lookahead;
                match("-");
                Expr direita = parseMultiplicativa();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_ADITIVA)) {
                // epsilon: lookahead pertence a FOLLOW(MaisAditiva).
                return r;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return r;
            }
            else {
                erro("MaisAditiva", "esperava operador aritmetico ou continuacao da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_ADITIVA);
                return r;
            }
        }
    }

    // <Multiplicativa> ::= <Unaria> <MaisMultiplicativa>
    private Expr parseMultiplicativa() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.FIRST_MULTIPLICATIVA)) {
            r = parseUnaria();
            r = parseMaisMultiplicativa(r);
            return r;
        }
        else {
            erro("Multiplicativa", "esperava operando");
            recuperar(FirstFollow.FOLLOW_MULTIPLICATIVA);
            return r;
        }
    }

    // <MaisMultiplicativa> ::= "*" <Unaria> <MaisMultiplicativa> | "/" <Unaria> <MaisMultiplicativa> | "%" <Unaria> <MaisMultiplicativa> | ε
    private Expr parseMaisMultiplicativa(Expr r) {
        while (true) {
            if (em(FirstFollow.OPERADOR_MULTIPLICACAO)) {
                Token op = lookahead;
                match("*");
                Expr direita = parseUnaria();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.OPERADOR_DIVISAO)) {
                Token op = lookahead;
                match("/");
                Expr direita = parseUnaria();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.OPERADOR_RESTO)) {
                Token op = lookahead;
                match("%");
                Expr direita = parseUnaria();
                if(r != null && direita != null) r = new Bin(r.linha,r.coluna,r,op.lexema,direita);
                continue;
            }
            else if (em(FirstFollow.FOLLOW_MAIS_MULTIPLICATIVA)) {
                // epsilon: lookahead pertence a FOLLOW(MaisMultiplicativa).
                return r;
            }
            else if (em(FirstFollow.RECUPERACAO_FIM_EXPRESSAO)) {
                // Recuperacao de delimitador ausente: o chamador reporta o que faltou.
                return r;
            }
            else {
                erro("MaisMultiplicativa", "esperava operador aritmetico ou continuacao da expressao");
                recuperar(FirstFollow.FOLLOW_MAIS_MULTIPLICATIVA);
                return r;
            }
        }
    }

    // <Unaria> ::= "!" <Unaria> | "+" <Unaria> | "-" <Unaria> | <Primaria>
    private Expr parseUnaria() {
        Expr r = null;
        Token inicio = lookahead;
        List<Token> operadores = new ArrayList<>();
        while (true) {
            if (em(FirstFollow.OPERADOR_NEGACAO)) {
                operadores.add(lookahead);
                match("!");
                continue;
            }
            else if (em(FirstFollow.OPERADOR_ADICAO)) {
                operadores.add(lookahead);
                match("+");
                continue;
            }
            else if (em(FirstFollow.OPERADOR_SUBTRACAO)) {
                operadores.add(lookahead);
                match("-");
                continue;
            }
            else if (em(FirstFollow.FIRST_PRIMARIA)) {
                r = parsePrimaria();
                for(int i=operadores.size()-1; i>=0 && r!=null; i--) {
                    Token op=operadores.get(i); r=new Unaria(op.linha,op.coluna,op.lexema,r);
                }
                return r;
            }
            else {
                erro("Unaria", "esperava operando");
                recuperar(FirstFollow.FOLLOW_UNARIA);
                return r;
            }
        }
    }

    // <Primaria> ::= "IDENTIFICADOR" <ChamadaOpcional> | "NUMERO" | "STRING" | "CARACTERE" | "true" | "false" | "(" <Expressao> ")"
    private Expr parsePrimaria() {
        Expr r = null;
        Token inicio = lookahead;
        if (em(FirstFollow.IDENTIFICADOR)) {
            match("IDENTIFICADOR");
            r = parseChamadaOpcional(inicio);
            return r;
        }
        else if (em(FirstFollow.NUMERO)) {
            match("NUMERO");
            r = new Literal(inicio.linha,inicio.coluna,(inicio.lexema.contains(".") ? "double" : "int"),inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.STRING)) {
            match("STRING");
            r = new Literal(inicio.linha,inicio.coluna,"string",inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.CARACTERE)) {
            match("CARACTERE");
            r = new Literal(inicio.linha,inicio.coluna,"char",inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_TRUE)) {
            match("true");
            r = new Literal(inicio.linha,inicio.coluna,"bool",inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.PALAVRA_RESERVADA_FALSE)) {
            match("false");
            r = new Literal(inicio.linha,inicio.coluna,"bool",inicio.lexema);
            return r;
        }
        else if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            match("(");
            r = parseExpressao();
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_PRIMARIA);
                return r;
            }
            return r;
        }
        else {
            erro("Primaria", "esperava operando");
            recuperar(FirstFollow.FOLLOW_PRIMARIA);
            return r;
        }
    }

    // <ChamadaOpcional> ::= "(" <Argumentos> ")" | ε
    private Expr parseChamadaOpcional(Token inicio) {
        Expr r = new Id(inicio.linha,inicio.coluna,inicio.lexema);
        if (em(FirstFollow.DELIMITADOR_ABRE_PARENTESES)) {
            match("(");
            r = new Chamada(inicio.linha,inicio.coluna,new Id(inicio.linha,inicio.coluna,inicio.lexema),parseArgumentos());
            if (!match(")")) {
                recuperar(FirstFollow.FOLLOW_CHAMADA_OPCIONAL);
                return r;
            }
            return r;
        }
        else if (em(FirstFollow.FOLLOW_CHAMADA_OPCIONAL)) {
            // epsilon: lookahead pertence a FOLLOW(ChamadaOpcional).
            return r;
        }
        else {
            erro("ChamadaOpcional", "esperava chamada de funcao ou continuacao da expressao");
            recuperar(FirstFollow.FOLLOW_CHAMADA_OPCIONAL);
            return r;
        }
    }
}
