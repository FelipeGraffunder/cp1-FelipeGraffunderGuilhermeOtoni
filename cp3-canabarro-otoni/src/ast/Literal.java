package ast;
import java.util.List;
import visitor.Visitor;
public final class Literal extends Expr {
    public final String tipo;
    public final String lexema;
    public Literal(int linha, int coluna, String tipo, String lexema) {
        super(linha,coluna);
        this.tipo = tipo;
        this.lexema = lexema;
    }
    @Override public <T> T accept(Visitor<T> v) { return v.visitLiteral(this); }
}
