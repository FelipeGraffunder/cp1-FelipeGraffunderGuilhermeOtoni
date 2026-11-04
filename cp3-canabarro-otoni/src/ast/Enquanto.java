package ast;
import java.util.List;
import visitor.Visitor;
public final class Enquanto extends Comando {
    public final Expr condicao;
    public final Bloco corpo;
    public Enquanto(int linha, int coluna, Expr condicao, Bloco corpo) {
        super(linha,coluna);
        this.condicao = condicao;
        this.corpo = corpo;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitEnquanto(this); }
}
