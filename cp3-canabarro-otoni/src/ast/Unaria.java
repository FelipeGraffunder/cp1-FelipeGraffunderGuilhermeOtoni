package ast;
import java.util.List;
import visitor.Visitor;
public final class Unaria extends Expr {
    public final String operador;
    public final Expr operando;
    public Unaria(int linha, int coluna, String operador, Expr operando) {
        super(linha,coluna);
        this.operador = operador;
        this.operando = operando;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitUnaria(this); }
}
