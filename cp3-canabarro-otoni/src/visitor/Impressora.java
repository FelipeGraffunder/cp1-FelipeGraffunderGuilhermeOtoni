package visitor;
import ast.*;
public final class Impressora implements Visitor<String> {
    public String imprimir(Node n) { return n.accept(this); }
    private String filho(Node n) {
        if(n==null) return "";
        return n.accept(this).replaceAll("(?m)^", "  ");
    }
    private String lista(java.util.List<? extends Node> ns) {
        StringBuilder s=new StringBuilder();
        for(Node n:ns) s.append(filho(n));
        return s.toString();
    }
    // Escape attributes independently from the scanner's literal syntax.
    private String q(String s) {
        return "\""+s.replace("\\","\\\\").replace("\"","\\\"")
            .replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";
    }
    public String visitPrograma(Programa n) { return "Programa" + "\n" + lista(n.itens); }
    public String visitVariavel(Variavel n) { return "Variavel nome="+q(n.nome)+" inicial="+(n.inicial!=null) + "\n" + filho(n.tipo) + filho(n.inicial); }
    public String visitFuncao(Funcao n) { return "Funcao nome="+q(n.nome)+" parametros="+n.parametros.size() + "\n" + filho(n.tipo) + lista(n.parametros) + filho(n.corpo); }
    public String visitProcedimento(Procedimento n) { return "Procedimento nome="+q(n.nome)+" parametros="+n.parametros.size() + "\n" + lista(n.parametros) + filho(n.corpo); }
    public String visitParametro(Parametro n) { return "Parametro nome="+q(n.nome) + "\n" + filho(n.tipo); }
    public String visitTipo(Tipo n) { return "Tipo nome="+q(n.nome) + "\n"; }
    public String visitBloco(Bloco n) { return "Bloco" + "\n" + lista(n.elementos); }
    public String visitAtribuicao(Atribuicao n) { return "Atribuicao" + "\n" + filho(n.alvo) + filho(n.valor); }
    public String visitSe(Se n) { return "Se senao="+(n.senao!=null) + "\n" + filho(n.condicao) + filho(n.entao) + filho(n.senao); }
    public String visitEnquanto(Enquanto n) { return "Enquanto" + "\n" + filho(n.condicao) + filho(n.corpo); }
    public String visitChamadaCmd(ChamadaCmd n) { return "ChamadaCmd" + "\n" + filho(n.chamada); }
    public String visitRetorno(Retorno n) { return "Retorno valor="+(n.valor!=null) + "\n" + filho(n.valor); }
    public String visitId(Id n) { return "Id nome="+q(n.nome) + "\n"; }
    public String visitLiteral(Literal n) { return "Literal tipo="+q(n.tipo)+" lexema="+q(n.lexema) + "\n"; }
    public String visitBin(Bin n) { return "Bin operador="+q(n.operador) + "\n" + filho(n.esquerda) + filho(n.direita); }
    public String visitUnaria(Unaria n) { return "Unaria operador="+q(n.operador) + "\n" + filho(n.operando); }
    public String visitChamada(Chamada n) { return "Chamada argumentos="+n.argumentos.size() + "\n" + filho(n.funcao) + lista(n.argumentos); }
}
