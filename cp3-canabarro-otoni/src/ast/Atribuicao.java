package ast;
import java.util.List;
import visitor.Visitor;
public final class Atribuicao extends Comando {
    public final Id alvo;
    public final Expr valor;
    public Atribuicao(int linha, int coluna, Id alvo, Expr valor) {
        super(linha,coluna);
        this.alvo = alvo;
        this.valor = valor;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitAtribuicao(this); }
}
