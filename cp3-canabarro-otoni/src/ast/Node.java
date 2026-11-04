package ast;
import visitor.Visitor;
public abstract class Node {
    public final int linha, coluna;
    protected Node(int linha, int coluna) { this.linha=linha; this.coluna=coluna; }
    public abstract <T> T accept(Visitor<T> v);
}
