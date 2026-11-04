package ast;
import java.util.List;
import visitor.Visitor;
public final class Programa extends Node {
    public final List<Node> itens;
    public Programa(int linha, int coluna, List<Node> itens) {
        super(linha,coluna);
        this.itens = List.copyOf(itens);
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitPrograma(this); }
}
