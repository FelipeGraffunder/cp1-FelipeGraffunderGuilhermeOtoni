package ast;
import java.util.List;
import visitor.Visitor;
public final class ChamadaCmd extends Comando {
    public final Chamada chamada;
    public ChamadaCmd(int linha, int coluna, Chamada chamada) {
        super(linha,coluna);
        this.chamada = chamada;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitChamadaCmd(this); }
}
