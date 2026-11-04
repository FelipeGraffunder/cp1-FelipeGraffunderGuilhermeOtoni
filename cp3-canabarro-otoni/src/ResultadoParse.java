import ast.Programa;
import java.util.List;
public final class ResultadoParse {
    public final Programa arvore;
    public final List<String> erros;
    public ResultadoParse(Programa arvore,List<String> erros) {
        this.erros=List.copyOf(erros); this.arvore=this.erros.isEmpty()?arvore:null;
    }
    public boolean aceito() { return arvore!=null && erros.isEmpty(); }
}
