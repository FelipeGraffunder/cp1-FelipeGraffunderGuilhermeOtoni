package ast;
import java.util.List;
import visitor.Visitor;
public final class Chamada extends Expr {
    public final Id funcao;
    public final List<Expr> argumentos;
    public Chamada(int linha, int coluna, Id funcao, List<Expr> argumentos) {
        super(linha,coluna);
        this.funcao = funcao;
        this.argumentos = List.copyOf(argumentos);
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitChamada(this); }
}
