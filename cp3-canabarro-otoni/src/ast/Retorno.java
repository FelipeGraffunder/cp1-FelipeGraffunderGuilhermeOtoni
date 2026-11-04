package ast;
import java.util.List;
import visitor.Visitor;
public final class Retorno extends Comando {
    public final Expr valor;
    public Retorno(int linha, int coluna, Expr valor) {
        super(linha,coluna);
        this.valor = valor;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitRetorno(this); }
}
