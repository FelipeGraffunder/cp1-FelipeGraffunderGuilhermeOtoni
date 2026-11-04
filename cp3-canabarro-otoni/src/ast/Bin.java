package ast;
import java.util.List;
import visitor.Visitor;
public final class Bin extends Expr {
    public final Expr esquerda;
    public final String operador;
    public final Expr direita;
    public Bin(int linha, int coluna, Expr esquerda, String operador, Expr direita) {
        super(linha,coluna);
        this.esquerda = esquerda;
        this.operador = operador;
        this.direita = direita;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitBin(this); }
}
