package ast;
import java.util.List;
import visitor.Visitor;
public final class Procedimento extends Declaracao {
    public final String nome;
    public final List<Parametro> parametros;
    public final Bloco corpo;
    public Procedimento(int linha, int coluna, String nome, List<Parametro> parametros, Bloco corpo) {
        super(linha,coluna);
        this.nome = nome;
        this.parametros = List.copyOf(parametros);
        this.corpo = corpo;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitProcedimento(this); }
}
