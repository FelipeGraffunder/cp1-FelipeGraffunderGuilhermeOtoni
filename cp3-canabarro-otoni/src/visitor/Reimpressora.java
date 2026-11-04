package visitor;
import ast.*;
import java.util.List;
import java.util.stream.Collectors;
public final class Reimpressora implements Visitor<String> {
    public String imprimir(Node n) { return n.accept(this); }
    private String p(Node n) { return n.accept(this); }
    private String linhas(List<? extends Node> ns) {
        return ns.stream().map(this::p).collect(Collectors.joining("\n"));
    }
    private String separados(List<? extends Node> ns) {
        return ns.stream().map(this::p).collect(Collectors.joining(", "));
    }
    public String visitPrograma(Programa n) { return linhas(n.itens); }
    public String visitVariavel(Variavel n) { return p(n.tipo)+" "+n.nome+(n.inicial==null?"":" = "+p(n.inicial))+";"; }
    public String visitFuncao(Funcao n) { return p(n.tipo)+" "+n.nome+"("+separados(n.parametros)+") "+p(n.corpo); }
    public String visitProcedimento(Procedimento n) { return "void "+n.nome+"("+separados(n.parametros)+") "+p(n.corpo); }
    public String visitParametro(Parametro n) { return p(n.tipo)+" "+n.nome; }
    public String visitTipo(Tipo n) { return n.nome; }
    public String visitBloco(Bloco n) { return "{\n"+linhas(n.elementos)+"\n}"; }
    public String visitAtribuicao(Atribuicao n) { return p(n.alvo)+" = "+p(n.valor)+";"; }
    public String visitSe(Se n) { return "if ("+p(n.condicao)+") "+p(n.entao)+(n.senao==null?"":" else "+p(n.senao)); }
    public String visitEnquanto(Enquanto n) { return "while ("+p(n.condicao)+") "+p(n.corpo); }
    public String visitChamadaCmd(ChamadaCmd n) { return p(n.chamada)+";"; }
    public String visitRetorno(Retorno n) { return "return"+(n.valor==null?"":" "+p(n.valor))+";"; }
    public String visitId(Id n) { return n.nome; }
    public String visitLiteral(Literal n) { return n.tipo.equals("string")?"\""+n.lexema+"\"":n.lexema; }
    public String visitBin(Bin n) { return "("+p(n.esquerda)+" "+n.operador+" "+p(n.direita)+")"; }
    public String visitUnaria(Unaria n) { return "("+n.operador+" "+p(n.operando)+")"; }
    public String visitChamada(Chamada n) { return p(n.funcao)+"("+separados(n.argumentos)+")"; }
}
