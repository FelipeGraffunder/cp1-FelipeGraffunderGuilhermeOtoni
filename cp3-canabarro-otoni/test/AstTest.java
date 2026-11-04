import ast.*;
import visitor.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class AstTest {
    private static int total;
    static void check(boolean c,String msg) { if(!c) throw new AssertionError(msg); total++; }
    static String read(Path p) throws Exception { return Files.readString(p,StandardCharsets.UTF_8).replace("\r\n","\n"); }
    static Programa parse(String s) {
        Parser p=new Parser(new Scanner(s)); ResultadoParse r=p.parse();
        check(r.aceito(),"Fonte rejeitada: "+s+" -> "+r.erros);
        check(r==p.parse(),"parse deve ser idempotente");
        return r.arvore;
    }
    static String tree(Node n) { return new Impressora().imprimir(n); }
    static void round(String s) {
        Programa a=parse(s); String f=new Reimpressora().imprimir(a);
        Programa b=parse(f); String g=new Reimpressora().imprimir(b);
        check(f.equals(g),"Fonte canonica instavel: "+s);
        check(tree(a).equals(tree(b)),"Arvore mudou no round-trip: "+s);
    }
    public static void main(String[] args) throws Exception {
        for(String name:List.of("cp3-tipos","cp3-controle","cp3-chamadas","precedencia","associatividade","parenteses")) {
            String s=read(Path.of("test/entradas",name+".txt"));
            String expected=read(Path.of("test/esperado",name+".txt"));
            check(tree(parse(s)).equals(expected),"Oraculo diferente: "+name+"\n"+tree(parse(s)));
            round(s);
        }
        for(String name:List.of("valido-tipos","valido-controle","valido-funcoes")) round(read(Path.of("test/entradas",name+".txt")));
        check(tree(parse("x = (((a))); ")).equals(tree(parse("x = a;"))),"Parenteses redundantes criaram nos");
        round(""); round("int a; {} void p() {} if (true) {} while(false) {} return;");
        round("x = !-+a || b && c == d != e < f <= g > h >= i + j - k * l / m % n;");
        round("string a = \"a\\b\"; char c = '\"'; string z = \"\";");
        Programa pos=parse("\n  int f(int x) { return -x; }");
        Funcao f=(Funcao)pos.itens.get(0);
        check(f.linha==2 && f.coluna==3,"Posicao da funcao");
        check(f.tipo.coluna==3 && f.parametros.get(0).coluna==9 && f.corpo.coluna==16,"Posicoes de filhos");
        Retorno ret=(Retorno)f.corpo.elementos.get(0);
        check(ret.coluna==18 && ret.valor.coluna==25,"Posicao do retorno e unaria");
        Programa par=parse("x = (((a))); ");
        check(((Atribuicao)par.itens.get(0)).valor.coluna==8,"Id conserva sua posicao, nao a dos parenteses");
        for(String name:List.of("invalido-sem-ponto-virgula-eof","invalido-parenteses","invalido-operando","multiplos-erros")) {
            ResultadoParse r=new Parser(new Scanner(read(Path.of("test/entradas",name+".txt")))).parse();
            check(!r.aceito() && r.arvore==null && !r.erros.isEmpty(),"Arvore invalida exposta: "+name);
        }
        
        Random rand=new Random(3); String[] tokens={"int","void","x","1","+","-","(",")","{","}",";",",","if","else","while","return","=","@"};
        for(int i=0;i<1500;i++) {
            StringBuilder s=new StringBuilder(); for(int j=rand.nextInt(50);j>0;j--) s.append(tokens[rand.nextInt(tokens.length)]).append(' ');
            ResultadoParse r=new Parser(new Scanner(s.toString())).parse();
            check(r.aceito()==(r.arvore!=null && r.erros.isEmpty()),"Invariante em entrada aleatoria");
            if(r.aceito()) round(s.toString());
        }
        System.out.println("[OK] AST: "+total+" verificacoes (inclui 1500 entradas aleatorias).");
    }
}
