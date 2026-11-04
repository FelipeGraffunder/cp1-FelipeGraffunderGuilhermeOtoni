# Árvore de Sintaxe Abstrata e Visitor

Integrantes: Felipe Canabarro e Guilherme Otoni.

## 1. Objetivo

O parser constrói uma árvore de sintaxe abstrata (AST) que representa as estruturas relevantes do programa. A gramática utilizada está descrita em `gramatica-first-follow.md`.

A árvore permite realizar operações por meio do padrão Visitor. Foram implementadas uma impressora da estrutura da AST e uma reimpressora de código-fonte.

Não fazem parte desta implementação a análise semântica, a tabela de símbolos, a execução do programa ou a geração de código.

## 2. Hierarquia e representação dos nós

A classe abstrata `Node` contém os atributos `int linha` e `int coluna`, além do método abstrato `<T> T accept(Visitor<T> v)`.

As classes abstratas `Declaracao`, `Comando` e `Expr` derivam de `Node` e agrupam as principais categorias da linguagem. `Programa`, `Tipo` e `Parametro` também derivam diretamente de `Node`.

Cada tipo concreto representa uma categoria sintática relevante. Os não-terminais usados apenas para organizar a gramática não recebem classes próprias.

| Nó | Classe base | Atributos além da posição | Produções que originam o nó |
|---|---|---|---|
| `Programa` | `Node` | `List<Node> itens` | `Programa → Itens` |
| `Variavel` | `Declaracao` | `Tipo tipo`, `String nome`, `Expr inicial` | `Item → Tipo id RestoItem`, com `RestoItem → Inicializacao ";"`; ou `Elemento → Tipo id Inicializacao ";"` |
| `Funcao` | `Declaracao` | `Tipo tipo`, `String nome`, `List<Parametro> parametros`, `Bloco corpo` | `Item → Tipo id RestoItem`, com `RestoItem → Funcao` e `Funcao → "(" Parametros ")" Bloco` |
| `Procedimento` | `Declaracao` | `String nome`, `List<Parametro> parametros`, `Bloco corpo` | `Item → void id Funcao` |
| `Parametro` | `Node` | `Tipo tipo`, `String nome` | `Parametro → Tipo id` |
| `Tipo` | `Node` | `String nome` | Alternativas de `Tipo`: `int`, `double`, `bool`, `char` e `string` |
| `Bloco` | `Comando` | `List<Node> elementos` | `Bloco → "{" Elementos "}"` |
| `Atribuicao` | `Comando` | `Id alvo`, `Expr valor` | `Comando → id RestoComando ";"`, com `RestoComando → "=" Expressao` |
| `Se` | `Comando` | `Expr condicao`, `Bloco entao`, `Bloco senao` | `Se → if "(" Expressao ")" Bloco Senao` |
| `Enquanto` | `Comando` | `Expr condicao`, `Bloco corpo` | `Enquanto → while "(" Expressao ")" Bloco` |
| `ChamadaCmd` | `Comando` | `Chamada chamada` | `Comando → id RestoComando ";"`, com `RestoComando → "(" Argumentos ")"` |
| `Retorno` | `Comando` | `Expr valor` | `Retorno → return ValorRetorno ";"` |
| `Id` | `Expr` | `String nome` | `Primaria → id ChamadaOpcional`, quando não há chamada; também representa o alvo de atribuição e o identificador de uma chamada |
| `Literal` | `Expr` | `String tipo`, `String lexema` | Alternativas de `Primaria`: `numero`, `texto`, `caractere`, `true` e `false` |
| `Bin` | `Expr` | `Expr esquerda`, `String operador`, `Expr direita` | Operações dos níveis `Ou`, `E`, `Igualdade`, `Relacional`, `Aditiva` e `Multiplicativa` |
| `Unaria` | `Expr` | `String operador`, `Expr operando` | `Unaria → "!" Unaria`, `Unaria → "+" Unaria` e `Unaria → "-" Unaria` |
| `Chamada` | `Expr` | `Id funcao`, `List<Expr> argumentos` | `Primaria → id ChamadaOpcional`, com chamada; ou chamada reconhecida em `RestoComando` |

As listas preservam a ordem do código-fonte e são copiadas com `List.copyOf`. Os campos estruturais são públicos e finais.

Os atributos opcionais usam `null`:

- `Variavel.inicial`, quando não existe inicialização;
- `Se.senao`, quando não existe `else`;
- `Retorno.valor`, quando o retorno não possui expressão.

Uma declaração com `void` é representada por `Procedimento`. As funções com retorno possuem um nó `Tipo`, que representa o tipo escrito na declaração.

Os cinco tipos de literais são representados pela classe `Literal`, diferenciados pelo atributo `tipo`. Literais numéricos com ponto são classificados como `double`; os demais, como `int`. O valor permanece como texto em `lexema`, sem conversão para um tipo numérico de Java.

O scanner remove as aspas dos literais de string e preserva as aspas dos literais de char. A AST mantém o lexema recebido, e a reimpressora recoloca as aspas das strings. Como o scanner não define sequências de escape, as barras são preservadas literalmente.

Comentários e espaços de formatação não fazem parte da AST.

## 3. Mapeamento da gramática para a AST

| Não-terminal | Representação na AST |
|---|---|
| `Programa` | Nó `Programa`, que contém os itens em ordem. |
| `Itens` | Lista `Programa.itens`. Não precisa de um nó próprio para representar a lista. |
| `Item` | Nó da declaração ou do comando reconhecido, sem um nó intermediário `Item`. |
| `Tipo` | Nó `Tipo`, usado em declarações e parâmetros. |
| `RestoItem` | Auxiliar de fatoração que distingue variável e função. Não cria nó próprio. |
| `Inicializacao` | Atributo `Variavel.inicial`, com uma expressão ou `null`. O sinal de igualdade fica implícito nesse atributo. |
| `Funcao` | Fornece parâmetros e corpo para um nó `Funcao` ou `Procedimento`, conforme a declaração iniciada. |
| `Parametros` | Lista de parâmetros de uma função ou procedimento. A alternativa vazia gera uma lista vazia. |
| `Parametro` | Nó `Parametro`, com nome e tipo. |
| `MaisParametros` | Continua a lista de parâmetros. Não gera nó próprio, pois a lista já mantém a ordem dos elementos. |
| `Bloco` | Nó `Bloco`. |
| `Elementos` | Lista `Bloco.elementos`, sem nós auxiliares para encadear seus elementos. |
| `Elemento` | Nó `Variavel` ou nó do comando reconhecido. |
| `Comando` | Nó `Atribuicao`, `ChamadaCmd`, `Se`, `Enquanto`, `Retorno` ou `Bloco`. |
| `RestoComando` | Auxiliar de fatoração que completa uma atribuição ou chamada. Não cria nó próprio. |
| `Se` | Nó `Se`. |
| `Senao` | Atributo `Se.senao`, que recebe um `Bloco` ou `null`. |
| `Enquanto` | Nó `Enquanto`. |
| `Retorno` | Nó `Retorno`. |
| `ValorRetorno` | Atributo `Retorno.valor`, que recebe uma expressão ou `null`. |
| `Argumentos` | Lista `Chamada.argumentos`. A alternativa vazia gera uma lista vazia. |
| `MaisArgumentos` | Continua a lista de argumentos, sem criar nós para os separadores. |
| `Expressao` | Repassa a expressão produzida por `Ou`, sem criar um nó intermediário. |
| `Ou` | Repassa a expressão de `E` ou constrói nós `Bin` para o operador OU lógico. |
| `E` | Repassa a expressão de `Igualdade` ou constrói nós `Bin` para `&&`. |
| `Igualdade` | Repassa a expressão de `Relacional` ou constrói nós `Bin` para `==` e `!=`. |
| `Relacional` | Repassa a expressão de `Aditiva` ou constrói nós `Bin` para `<`, `>`, `<=` e `>=`. |
| `Aditiva` | Repassa a expressão de `Multiplicativa` ou constrói nós `Bin` para `+` e `-`. |
| `Multiplicativa` | Repassa a expressão de `Unaria` ou constrói nós `Bin` para `*`, `/` e `%`. |
| `MaisOu` | Acumula operações de OU lógico à esquerda, sem nó auxiliar. |
| `MaisE` | Acumula operações de `&&` à esquerda, sem nó auxiliar. |
| `MaisIgualdade` | Acumula operações de `==` e `!=` à esquerda, sem nó auxiliar. |
| `MaisRelacional` | Acumula operações de comparação à esquerda, sem nó auxiliar. |
| `MaisAditiva` | Acumula operações de `+` e `-` à esquerda, sem nó auxiliar. |
| `MaisMultiplicativa` | Acumula operações de `*`, `/` e `%` à esquerda, sem nó auxiliar. |
| `Unaria` | Cria um nó `Unaria` por operador ou repassa o resultado de `Primaria`. |
| `Primaria` | Produz `Id`, `Literal`, `Chamada` ou repassa a expressão entre parênteses. |
| `ChamadaOpcional` | Auxiliar de fatoração que distingue identificador e chamada. Não cria nó próprio. |

Os três grupos de auxiliares descartados são:

1. **Auxiliares de precedência:** os níveis `Ou`, `E`, `Igualdade`, `Relacional`, `Aditiva`, `Multiplicativa` e `Primaria` organizam o reconhecimento das expressões, desempenhando o papel de níveis como `T` e `F`. Não aparecem como nós próprios; a organização dos operadores e operandos preserva a precedência.

2. **Auxiliares de eliminação da recursão à esquerda:** os seis não-terminais `Mais*` dos operadores cumprem o papel de `E'` e `T'`. Cada nova operação utiliza a expressão acumulada como filho esquerdo de um novo `Bin`.

3. **Auxiliares de fatoração:** `RestoItem`, `RestoComando` e `ChamadaOpcional` permitem decidir entre alternativas com prefixo comum. Depois dessa decisão, não precisam permanecer na árvore.

Parênteses, vírgulas, pontos e vírgulas e chaves não recebem nós próprios. A estrutura da AST já representa o agrupamento, os limites dos blocos e a ordem dos elementos.

## 4. Posição dos nós e tratamento de erros

Todos os nós armazenam linha e coluna. A posição é definida a partir do token que inicia a construção representada.

| Nó ou categoria | Referência de posição |
|---|---|
| Declaração de variável ou função | Token do tipo declarado. |
| Procedimento | Palavra-chave `void`. |
| Parâmetro | Token do tipo do parâmetro. |
| Tipo | Token que identifica o tipo. |
| Seleção, repetição e retorno | Palavra-chave do comando. |
| Bloco | Chave de abertura. |
| Identificador, chamada e atribuição | Identificador inicial. |
| Literal | Token do literal. |
| Operação unária | Token do operador. |
| Operação binária | Posição do operando esquerdo. |
| Programa | Início do programa; em programa vazio, posição de EOF. |

Parênteses não geram nós e não alteram a posição do nó interno. Assim, em `(((a)))`, o nó `Id` mantém a posição do identificador `a`. Os visitors não modificam essas posições.

O método `parse()` retorna um `ResultadoParse`, que contém a árvore e uma lista imutável de erros. O método `aceito()` considera esses dois componentes.

Quando há erro léxico ou sintático, a árvore exposta no resultado é `null`. Nós parciais usados durante a recuperação não são encaminhados aos visitors nem às fases seguintes.

As operações binárias são construídas somente quando os dois operandos existem, e as listas não incluem resultados nulos. O reconhecimento utiliza FIRST e FOLLOW, e a recuperação de erros ocorre por sincronização em modo pânico.

Uma nova chamada a `parse()` na mesma instância devolve o resultado já obtido.

## 5. Visitor e operações sobre a árvore

A interface `Visitor<T>` possui um método para cada tipo concreto de nó. Cada implementação de `accept` chama o método correspondente ao próprio nó.

Essa combinação realiza o *double dispatch*: o nó direciona a chamada para o método específico do visitor. Não é necessário usar `instanceof` nem selecionar o comportamento por um `switch` sobre o tipo do nó.

Foram definidas duas operações:

- `Impressora`: produz a representação textual da AST;
- `Reimpressora`: produz código-fonte na sintaxe da linguagem.

A inclusão da `Reimpressora` não exigiu alterações nas classes dos nós. Os atributos existentes e a interface `Visitor<T>` já forneciam as informações necessárias.

### 5.1. Formato da impressora

A `Impressora` percorre a árvore em pré-ordem, imprimindo um nó por linha, com dois espaços de indentação por nível e quebra de linha LF ao final.

Cada linha contém o nome do nó e seus atributos relevantes. Campos textuais aparecem entre aspas, com escape de barras, aspas, tabulações e quebras de linha.

Os filhos são impressos em ordem fixa:

- `Programa`: itens na ordem da fonte;
- `Variavel`: tipo e, quando presente, inicialização;
- `Funcao`: tipo de retorno, parâmetros e corpo;
- `Procedimento`: parâmetros e corpo;
- `Parametro`: tipo;
- `Bloco`: elementos na ordem da fonte;
- `Atribuicao`: alvo e valor;
- `Se`: condição, bloco principal e, quando presente, bloco `else`;
- `Enquanto`: condição e corpo;
- `ChamadaCmd`: chamada;
- `Retorno`: expressão, quando presente;
- `Bin`: operando esquerdo e operando direito;
- `Unaria`: operando;
- `Chamada`: identificador da função e argumentos.

As contagens de parâmetros e argumentos identificam essas listas. As marcas `inicial`, `senao` e `valor` indicam a presença de atributos opcionais.

As posições são omitidas da impressão para permitir a comparação estrutural após a reformatação do código. Árvores estruturalmente diferentes produzem representações diferentes, enquanto mudanças apenas de localização ou comentários não alteram a saída.

### 5.2. Código-fonte canônico

A `Reimpressora` preserva os identificadores e os lexemas dos literais. Expressões binárias e unárias recebem parênteses, mantendo explícito o agrupamento das operações.

Os blocos são delimitados por chaves. Declarações e comandos recebem os delimitadores exigidos pela gramática.

O código usa quebras de linha nos blocos, sem indentação adicional. O objetivo é produzir uma representação estável que possa ser analisada novamente pelo parser.

## 6. Exemplo de árvore

O programa abaixo contém uma função, uma seleção e uma repetição.

```text
int f(int x) {
    if (x > 0) {
        while (x > 1) {
            x = x - 1;
        }
    } else {
        return 0;
    }
    return x;
}
```

Sua AST possui 26 nós:

```text
Programa
  Funcao nome="f" parametros=1
    Tipo nome="int"
    Parametro nome="x"
      Tipo nome="int"
    Bloco
      Se senao=true
        Bin operador=">"
          Id nome="x"
          Literal tipo="int" lexema="0"
        Bloco
          Enquanto
            Bin operador=">"
              Id nome="x"
              Literal tipo="int" lexema="1"
            Bloco
              Atribuicao
                Id nome="x"
                Bin operador="-"
                  Id nome="x"
                  Literal tipo="int" lexema="1"
        Bloco
          Retorno valor=true
            Literal tipo="int" lexema="0"
      Retorno valor=true
        Id nome="x"
```

Três exemplos de estruturas da derivação descartadas são:

1. **Níveis de precedência de um identificador:** para reconhecer `x` como expressão, a gramática passa pelos níveis `Expressao`, `Ou`, `E`, `Igualdade`, `Relacional`, `Aditiva`, `Multiplicativa`, `Unaria` e `Primaria`. Os auxiliares `Mais*` e `ChamadaOpcional` derivam vazio nesse caso. A AST mantém somente `Id(x)`.

2. **Auxiliar de recursão à esquerda na subtração:** em `x - 1`, a produção `Aditiva → Multiplicativa MaisAditiva` reconhece o primeiro operando. `MaisAditiva → "-" Multiplicativa MaisAditiva` reconhece a subtração, e o último `MaisAditiva` deriva vazio. A AST mantém `Bin(-)` com os filhos `Id(x)` e `Literal(1)`, sem nós `Aditiva`, `Multiplicativa` ou `MaisAditiva`.

3. **Fatoração na declaração de função:** a produção `Item → Tipo id RestoItem`, seguida de `RestoItem → Funcao`, seleciona a declaração de função. O não-terminal `RestoItem` não permanece na AST. O nó `Funcao` reúne o nome, o tipo, os parâmetros e o corpo, sem nós próprios para os parênteses e os separadores.

## 7. Extensões da estrutura

A estrutura permite acrescentar informações semânticas sem alterar as relações entre os nós.

O nó `Id` poderá receber uma referência ao símbolo correspondente na tabela de símbolos. A classe `Expr` poderá receber um campo para o tipo semântico da expressão.

Esses campos e suas operações não estão implementados. O atributo `Literal.tipo` identifica a categoria do literal, e o nó `Tipo` representa um tipo escrito no programa; nenhum deles realiza inferência ou verificação de tipos.

## 8. Testes

Os arquivos de `test/esperado/` contêm as árvores de referência de três programas completos e de três casos específicos de expressões. Essas referências foram elaboradas a partir das entradas, sem copiar a saída da impressora.

| Caso | Objetivo |
|---|---|
| Tipos e literais | Representar os cinco tipos básicos e seus literais. |
| Controle de fluxo | Representar função com parâmetro, seleção, repetição, atribuição e retorno. |
| Funções e procedimentos | Representar parâmetros, chamadas e retorno sem valor. |
| Precedência | Verificar que `a + b * c` agrupa a multiplicação antes da soma. |
| Associatividade | Verificar que `a - b - c` representa `(a - b) - c`. |
| Agrupamento | Verificar que `a - (b - c)` mantém a subtração da direita como subárvore. |

A suíte também verifica que parênteses redundantes, como em `(((a)))`, não criam nós adicionais.

O teste de ida e volta analisa o programa, reimprime o código, analisa o texto produzido e reimprime novamente. As duas versões canônicas devem ser iguais, assim como as impressões estruturais das duas árvores.

`ParserTest` contém sete casos de reconhecimento e recuperação de erros. Os programas inválidos devem ser rejeitados, e o caso com múltiplos erros deve apresentar os diagnósticos em uma única análise.

`AstTest` inclui comparações com as árvores esperadas, testes de posição, programas vazios, atributos opcionais, operadores, literais e 1.500 entradas aleatórias determinísticas. Nos casos inválidos, o resultado deve conter erros e expor a árvore como `null`.

Os testes verificam os casos previstos na suíte, sem constituir uma prova de correção para toda entrada possível.