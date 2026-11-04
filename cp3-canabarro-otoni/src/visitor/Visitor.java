package visitor;
import ast.*;
public interface Visitor<T> {
    T visitPrograma(Programa n);
    T visitVariavel(Variavel n);
    T visitFuncao(Funcao n);
    T visitProcedimento(Procedimento n);
    T visitParametro(Parametro n);
    T visitTipo(Tipo n);
    T visitBloco(Bloco n);
    T visitAtribuicao(Atribuicao n);
    T visitSe(Se n);
    T visitEnquanto(Enquanto n);
    T visitChamadaCmd(ChamadaCmd n);
    T visitRetorno(Retorno n);
    T visitId(Id n);
    T visitLiteral(Literal n);
    T visitBin(Bin n);
    T visitUnaria(Unaria n);
    T visitChamada(Chamada n);
}
