package ast;
import java.util.List;
import visitor.Visitor;
public final class Id extends Expr {
    public final String nome;
    public Id(int linha, int coluna, String nome) {
        super(linha,coluna);
        this.nome = nome;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitId(this); }
}
