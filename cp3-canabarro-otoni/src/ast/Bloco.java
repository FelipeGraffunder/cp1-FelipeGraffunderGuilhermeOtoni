package ast;
import java.util.List;
import visitor.Visitor;
public final class Bloco extends Comando {
    public final List<Node> elementos;
    public Bloco(int linha, int coluna, List<Node> elementos) {
        super(linha,coluna);
        this.elementos = List.copyOf(elementos);
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitBloco(this); }
}
