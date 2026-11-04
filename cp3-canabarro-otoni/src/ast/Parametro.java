package ast;
import java.util.List;
import visitor.Visitor;
public final class Parametro extends Node {
    public final Tipo tipo;
    public final String nome;
    public Parametro(int linha, int coluna, Tipo tipo, String nome) {
        super(linha,coluna);
        this.tipo = tipo;
        this.nome = nome;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitParametro(this); }
}
