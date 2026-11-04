package ast;
import java.util.List;
import visitor.Visitor;
public final class Funcao extends Declaracao {
    public final Tipo tipo;
    public final String nome;
    public final List<Parametro> parametros;
    public final Bloco corpo;
    public Funcao(int linha, int coluna, Tipo tipo, String nome, List<Parametro> parametros, Bloco corpo) {
        super(linha,coluna);
        this.tipo = tipo;
        this.nome = nome;
        this.parametros = List.copyOf(parametros);
        this.corpo = corpo;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitFuncao(this); }
}
