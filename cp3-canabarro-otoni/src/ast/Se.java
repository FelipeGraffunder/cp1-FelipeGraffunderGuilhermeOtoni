package ast;
import java.util.List;
import visitor.Visitor;
public final class Se extends Comando {
    public final Expr condicao;
    public final Bloco entao;
    public final Bloco senao;
    public Se(int linha, int coluna, Expr condicao, Bloco entao, Bloco senao) {
        super(linha,coluna);
        this.condicao = condicao;
        this.entao = entao;
        this.senao = senao;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitSe(this); }
}
