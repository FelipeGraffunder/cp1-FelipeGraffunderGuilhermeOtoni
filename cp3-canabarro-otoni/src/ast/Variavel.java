package ast;
import java.util.List;
import visitor.Visitor;
public final class Variavel extends Declaracao {
    public final Tipo tipo;
    public final String nome;
    public final Expr inicial;
    public Variavel(int linha, int coluna, Tipo tipo, String nome, Expr inicial) {
        super(linha,coluna);
        this.tipo = tipo;
        this.nome = nome;
        this.inicial = inicial;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitVariavel(this); }
}
