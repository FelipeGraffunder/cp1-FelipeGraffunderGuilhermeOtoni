package ast;
import java.util.List;
import visitor.Visitor;
public final class Tipo extends Node {
    public final String nome;
    public Tipo(int linha, int coluna, String nome) {
        super(linha,coluna);
        this.nome = nome;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitTipo(this); }
}
