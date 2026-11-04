public final class Token {
    public final TipoToken tipo;
    public final String lexema;
    public final int linha;
    public final int coluna;

    public Token(TipoToken tipo, String lexema, int linha, int coluna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linha = linha;
        this.coluna = coluna;
    }

    @Override
    public String toString() {
        String texto = lexema.replace("\\", "\\\\").replace("\"", "\\\"");
        return String.format("(%d:%d) %-18s \"%s\"", linha, coluna, tipo, texto);
    }
}
