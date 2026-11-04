# Checkpoint 2: gramática e análise LL(1)

Integrantes do CP1: Felipe Canabarro e Guilherme Otoni.

## 1. Base e decisões da linguagem

O parser verifica a sintaxe e constrói a árvore de sintaxe abstrata (AST), sem realizar análise semântica. O scanner, as categorias de tokens e a gramática foram preservados.

A linguagem adota as seguintes regras:
- O programa pode estar vazio ou conter declarações, funções e comandos no nível global. Funções só podem ser definidas nesse nível.
- O tipo `void` é permitido apenas como retorno de procedimentos, não em variáveis ou parâmetros.
- Os corpos de `if`, `else` e `while` exigem chaves. Essa escolha evita o conflito do `else` na análise LL(1).

## 2. Transformações: antes e depois

Os exemplos abaixo mostram as regras antes e depois dos ajustes. São exemplos de construção da gramática, já que o CP1 não tinha uma gramática sintática.

### Recursão à esquerda

```bnf
Antes:  Aditiva ::= Aditiva "+" Multiplicativa
                   | Aditiva "-" Multiplicativa | Multiplicativa
Depois: Aditiva ::= Multiplicativa MaisAditiva
        MaisAditiva ::= "+" Multiplicativa MaisAditiva
                       | "-" Multiplicativa MaisAditiva | ε
```

A transformação `A ::= A α | β` em `A ::= β A'` e `A' ::= α A' | ε` também foi usada em Ou, E, Igualdade, Relacional e Multiplicativa. Nos unários, o operador é consumido antes da chamada recursiva. Nas listas, isso acontece com o item ou a vírgula. A verificação dos símbolos que podem aparecer à esquerda, incluindo os que geram vazio, não encontrou ciclos. Assim, não há recursão à esquerda direta nem indireta.

### Fatoração de declaração/função, atribuição/chamada e átomo/chamada

```bnf
Antes: Item ::= Tipo id Funcao | Tipo id Inicializacao ";" | ...
Depois: Item ::= Tipo id RestoItem | ...
        RestoItem ::= Funcao | Inicializacao ";"

Antes: Comando ::= id "=" Expressao ";" | id "(" Argumentos ")" ";" | ...
Depois: Comando ::= id RestoComando ";" | ...
        RestoComando ::= "=" Expressao | "(" Argumentos ")"

Antes: Primaria ::= id | id "(" Argumentos ")" | ...
Depois: Primaria ::= id ChamadaOpcional | ...
        ChamadaOpcional ::= "(" Argumentos ")" | ε
```

Antes da fatoração, as alternativas começavam com os mesmos tokens: FIRST(Tipo), `{id}` e `{id}`, respectivamente. Depois dela, basta olhar o próximo token: RestoItem distingue `(`, `=` e `;`; RestoComando distingue `=` e `(`; ChamadaOpcional usa `(` para reconhecer a chamada e FOLLOW para escolher ε.

### Tratamento do else

No exemplo da Aula 5, slide 7, a fatoração não resolve sozinha o problema do `else`. Se o corpo aceitar qualquer comando, as regras `Se ::= if (...) Comando Senao` e `Senao ::= else Comando | ε` permitem ifs aninhados que colocam `else` em FOLLOW(Senao). Como `{else} ∩ FOLLOW(Senao)` não é vazio, há um conflito LL(1). Associar o `else` ao `if` mais próximo resolve a escolha no parser, mas não torna essa gramática LL(1).

Para evitar esse conflito, os corpos precisam de chaves: `Se ::= if "(" Expressao ")" Bloco Senao` e `Senao ::= else Bloco | ε`. O `}` marca o fim do corpo, e `else` deixa de pertencer a FOLLOW(Senao), como aparece na tabela.

## 3. Gramática final em BNF

O símbolo inicial é `<Programa>`. `ε` indica vazio e `EOF` indica o fim da entrada, equivalente ao `$` dos slides. O método `parse()` verifica EOF depois de `<Programa>`. Na gramática, `id`, `numero`, `texto` e `caractere` representam categorias de tokens, não palavras que devem ser escritas no código.

```bnf
<Programa> ::= <Itens>
<Itens> ::= <Item> <Itens> | ε
<Item> ::= <Tipo> "id" <RestoItem> | "void" "id" <Funcao> | <Comando>
<Tipo> ::= "int" | "double" | "bool" | "char" | "string"
<RestoItem> ::= <Funcao> | <Inicializacao> ";"
<Inicializacao> ::= "=" <Expressao> | ε
<Funcao> ::= "(" <Parametros> ")" <Bloco>
<Parametros> ::= <Parametro> <MaisParametros> | ε
<Parametro> ::= <Tipo> "id"
<MaisParametros> ::= "," <Parametro> <MaisParametros> | ε
<Bloco> ::= "{" <Elementos> "}"
<Elementos> ::= <Elemento> <Elementos> | ε
<Elemento> ::= <Tipo> "id" <Inicializacao> ";" | <Comando>
<Comando> ::= "id" <RestoComando> ";" | <Se> | <Enquanto> | <Retorno> | <Bloco>
<RestoComando> ::= "=" <Expressao> | "(" <Argumentos> ")"
<Se> ::= "if" "(" <Expressao> ")" <Bloco> <Senao>
<Senao> ::= "else" <Bloco> | ε
<Enquanto> ::= "while" "(" <Expressao> ")" <Bloco>
<Retorno> ::= "return" <ValorRetorno> ";"
<ValorRetorno> ::= <Expressao> | ε
<Argumentos> ::= <Expressao> <MaisArgumentos> | ε
<MaisArgumentos> ::= "," <Expressao> <MaisArgumentos> | ε
<Expressao> ::= <Ou>
<Ou> ::= <E> <MaisOu>
<MaisOu> ::= "||" <E> <MaisOu> | ε
<E> ::= <Igualdade> <MaisE>
<MaisE> ::= "&&" <Igualdade> <MaisE> | ε
<Igualdade> ::= <Relacional> <MaisIgualdade>
<MaisIgualdade> ::= "==" <Relacional> <MaisIgualdade> | "!=" <Relacional> <MaisIgualdade> | ε
<Relacional> ::= <Aditiva> <MaisRelacional>
<MaisRelacional> ::= "<" <Aditiva> <MaisRelacional> | ">" <Aditiva> <MaisRelacional> | "<=" <Aditiva> <MaisRelacional> | ">=" <Aditiva> <MaisRelacional> | ε
<Aditiva> ::= <Multiplicativa> <MaisAditiva>
<MaisAditiva> ::= "+" <Multiplicativa> <MaisAditiva> | "-" <Multiplicativa> <MaisAditiva> | ε
<Multiplicativa> ::= <Unaria> <MaisMultiplicativa>
<MaisMultiplicativa> ::= "*" <Unaria> <MaisMultiplicativa> | "/" <Unaria> <MaisMultiplicativa> | "%" <Unaria> <MaisMultiplicativa> | ε
<Unaria> ::= "!" <Unaria> | "+" <Unaria> | "-" <Unaria> | <Primaria>
<Primaria> ::= "id" <ChamadaOpcional> | "numero" | "texto" | "caractere" | "true" | "false" | "(" <Expressao> ")"
<ChamadaOpcional> ::= "(" <Argumentos> ")" | ε
```

Mapeamento dos terminais para o scanner:

| Terminal | TipoToken | Critério adicional |
|---|---|---|
| id | IDENTIFICADOR | qualquer lexema |
| numero | NUMERO | inteiro ou decimal |
| texto | STRING | conteúdo sem aspas |
| caractere | CARACTERE | literal conforme CP1 |
| EOF | EOF | fim da entrada |
| palavras reservadas | PALAVRA_RESERVADA | lexema exato |
| operadores | OPERADOR | lexema exato |
| delimitadores | DELIMITADOR | lexema exato |

O parser verifica primeiro a categoria do token e depois seu lexema. Assim, `"if"` é um literal de texto, não a palavra reservada `if`. O mesmo vale para `";"`, que não é tratado como delimitador.

No código, id, numero, texto e caractere correspondem a `TipoToken.IDENTIFICADOR.name()`, `TipoToken.NUMERO.name()`, `TipoToken.STRING.name()` e `TipoToken.CARACTERE.name()`. Palavras reservadas, operadores e delimitadores são comparados pelo lexema dentro da categoria correta. Os conjuntos usados pelo parser ficam em `src/FirstFollow.java`. As listas FIRST guardam os terminais de comparação; a escolha de ε depende de FOLLOW. O método `nomeTerminal`, em `Parser.java`, escreve os nomes dos tokens nas mensagens de erro.

## 4. FIRST e FOLLOW de todos os não-terminais

O cálculo de FIRST considera os símbolos iniciais de cada produção, avançando quando um deles pode gerar ε. FOLLOW começa com EOF no símbolo inicial. Para cada regra `A ::= α B β`, FIRST(β), sem ε, entra em FOLLOW(B). Se β puder gerar vazio, FOLLOW(A) também entra em FOLLOW(B). O processo se repete até os conjuntos pararem de mudar (Aula 5, slides 9 e 14).

| Não-terminal | FIRST | FOLLOW |
|---|---|---|
| Programa | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `ε` } | { `EOF` } |
| Itens | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `ε` } | { `EOF` } |
| Item | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{` } | { `EOF`, `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{` } |
| Tipo | { `bool`, `char`, `double`, `int`, `string` } | { `id` } |
| RestoItem | { `(`, `;`, `=` } | { `EOF`, `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{` } |
| Inicializacao | { `=`, `ε` } | { `;` } |
| Funcao | { `(` } | { `EOF`, `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{` } |
| Parametros | { `bool`, `char`, `double`, `int`, `string`, `ε` } | { `)` } |
| Parametro | { `bool`, `char`, `double`, `int`, `string` } | { `)`, `,` } |
| MaisParametros | { `,`, `ε` } | { `)` } |
| Bloco | { `{` } | { `EOF`, `bool`, `char`, `double`, `else`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `}` } |
| Elementos | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `while`, `{`, `ε` } | { `}` } |
| Elemento | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `while`, `{` } | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `while`, `{`, `}` } |
| Comando | { `id`, `if`, `return`, `while`, `{` } | { `EOF`, `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `}` } |
| RestoComando | { `(`, `=` } | { `;` } |
| Se | { `if` } | { `EOF`, `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `}` } |
| Senao | { `else`, `ε` } | { `EOF`, `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `}` } |
| Enquanto | { `while` } | { `EOF`, `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `}` } |
| Retorno | { `return` } | { `EOF`, `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `}` } |
| ValorRetorno | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true`, `ε` } | { `;` } |
| Argumentos | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true`, `ε` } | { `)` } |
| MaisArgumentos | { `,`, `ε` } | { `)` } |
| Expressao | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `)`, `,`, `;` } |
| Ou | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `)`, `,`, `;` } |
| MaisOu | { `&#124;&#124;`, `ε` } | { `)`, `,`, `;` } |
| E | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `)`, `,`, `;`, `&#124;&#124;` } |
| MaisE | { `&&`, `ε` } | { `)`, `,`, `;`, `&#124;&#124;` } |
| Igualdade | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `&&`, `)`, `,`, `;`, `&#124;&#124;` } |
| MaisIgualdade | { `!=`, `==`, `ε` } | { `&&`, `)`, `,`, `;`, `&#124;&#124;` } |
| Relacional | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `!=`, `&&`, `)`, `,`, `;`, `==`, `&#124;&#124;` } |
| MaisRelacional | { `<`, `<=`, `>`, `>=`, `ε` } | { `!=`, `&&`, `)`, `,`, `;`, `==`, `&#124;&#124;` } |
| Aditiva | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `!=`, `&&`, `)`, `,`, `;`, `<`, `<=`, `==`, `>`, `>=`, `&#124;&#124;` } |
| MaisAditiva | { `+`, `-`, `ε` } | { `!=`, `&&`, `)`, `,`, `;`, `<`, `<=`, `==`, `>`, `>=`, `&#124;&#124;` } |
| Multiplicativa | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `!=`, `&&`, `)`, `+`, `,`, `-`, `;`, `<`, `<=`, `==`, `>`, `>=`, `&#124;&#124;` } |
| MaisMultiplicativa | { `%`, `*`, `/`, `ε` } | { `!=`, `&&`, `)`, `+`, `,`, `-`, `;`, `<`, `<=`, `==`, `>`, `>=`, `&#124;&#124;` } |
| Unaria | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `!=`, `%`, `&&`, `)`, `*`, `+`, `,`, `-`, `/`, `;`, `<`, `<=`, `==`, `>`, `>=`, `&#124;&#124;` } |
| Primaria | { `(`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | { `!=`, `%`, `&&`, `)`, `*`, `+`, `,`, `-`, `/`, `;`, `<`, `<=`, `==`, `>`, `>=`, `&#124;&#124;` } |
| ChamadaOpcional | { `(`, `ε` } | { `!=`, `%`, `&&`, `)`, `*`, `+`, `,`, `-`, `/`, `;`, `<`, `<=`, `==`, `>`, `>=`, `&#124;&#124;` } |

## 5. Verificação LL(1)

A tabela segue a ordem das alternativas da BNF. Para verificar LL(1), os FIRST das alternativas não podem ter terminais em comum. Além disso, no máximo uma alternativa pode gerar vazio; quando isso acontece, o FIRST das demais não pode ter interseção com FOLLOW. O símbolo `∅` indica que essa interseção é vazia. Regras com uma só alternativa não têm disputa entre alternativas.

| Não-terminal | FIRST de cada alternativa | Alternativas anuláveis | FIRST não vazio ∩ FOLLOW, quando anulável | Resultado |
|---|---|---|---|---|
| Programa | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{`, `ε` } | 1 | ∅ | LL(1) |
| Itens | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `void`, `while`, `{` } / { `ε` } | 1 | ∅ | LL(1) |
| Item | { `bool`, `char`, `double`, `int`, `string` } / { `void` } / { `id`, `if`, `return`, `while`, `{` } | 0 | não se aplica | LL(1) |
| Tipo | { `int` } / { `double` } / { `bool` } / { `char` } / { `string` } | 0 | não se aplica | LL(1) |
| RestoItem | { `(` } / { `;`, `=` } | 0 | não se aplica | LL(1) |
| Inicializacao | { `=` } / { `ε` } | 1 | ∅ | LL(1) |
| Funcao | { `(` } | 0 | não se aplica | LL(1) |
| Parametros | { `bool`, `char`, `double`, `int`, `string` } / { `ε` } | 1 | ∅ | LL(1) |
| Parametro | { `bool`, `char`, `double`, `int`, `string` } | 0 | não se aplica | LL(1) |
| MaisParametros | { `,` } / { `ε` } | 1 | ∅ | LL(1) |
| Bloco | { `{` } | 0 | não se aplica | LL(1) |
| Elementos | { `bool`, `char`, `double`, `id`, `if`, `int`, `return`, `string`, `while`, `{` } / { `ε` } | 1 | ∅ | LL(1) |
| Elemento | { `bool`, `char`, `double`, `int`, `string` } / { `id`, `if`, `return`, `while`, `{` } | 0 | não se aplica | LL(1) |
| Comando | { `id` } / { `if` } / { `while` } / { `return` } / { `{` } | 0 | não se aplica | LL(1) |
| RestoComando | { `=` } / { `(` } | 0 | não se aplica | LL(1) |
| Se | { `if` } | 0 | não se aplica | LL(1) |
| Senao | { `else` } / { `ε` } | 1 | ∅ | LL(1) |
| Enquanto | { `while` } | 0 | não se aplica | LL(1) |
| Retorno | { `return` } | 0 | não se aplica | LL(1) |
| ValorRetorno | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } / { `ε` } | 1 | ∅ | LL(1) |
| Argumentos | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } / { `ε` } | 1 | ∅ | LL(1) |
| MaisArgumentos | { `,` } / { `ε` } | 1 | ∅ | LL(1) |
| Expressao | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | 0 | não se aplica | LL(1) |
| Ou | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | 0 | não se aplica | LL(1) |
| MaisOu | { `&#124;&#124;` } / { `ε` } | 1 | ∅ | LL(1) |
| E | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | 0 | não se aplica | LL(1) |
| MaisE | { `&&` } / { `ε` } | 1 | ∅ | LL(1) |
| Igualdade | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | 0 | não se aplica | LL(1) |
| MaisIgualdade | { `==` } / { `!=` } / { `ε` } | 1 | ∅ | LL(1) |
| Relacional | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | 0 | não se aplica | LL(1) |
| MaisRelacional | { `<` } / { `>` } / { `<=` } / { `>=` } / { `ε` } | 1 | ∅ | LL(1) |
| Aditiva | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | 0 | não se aplica | LL(1) |
| MaisAditiva | { `+` } / { `-` } / { `ε` } | 1 | ∅ | LL(1) |
| Multiplicativa | { `!`, `(`, `+`, `-`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | 0 | não se aplica | LL(1) |
| MaisMultiplicativa | { `*` } / { `/` } / { `%` } / { `ε` } | 1 | ∅ | LL(1) |
| Unaria | { `!` } / { `+` } / { `-` } / { `(`, `caractere`, `false`, `id`, `numero`, `texto`, `true` } | 0 | não se aplica | LL(1) |
| Primaria | { `id` } / { `numero` } / { `texto` } / { `caractere` } / { `true` } / { `false` } / { `(` } | 0 | não se aplica | LL(1) |
| ChamadaOpcional | { `(` } / { `ε` } | 1 | ∅ | LL(1) |

SELECT(A → α) contém FIRST(α) sem ε. Se α puder gerar vazio, inclui também FOLLOW(A). Os conjuntos SELECT das alternativas de cada não-terminal não têm interseção, portanto não há conflitos LL(1).

As mensagens de erro usam nomes como operador aritmético, operador lógico, operando e identificador para facilitar a leitura. Essa simplificação é só na mensagem: as decisões do parser usam os conjuntos completos.

## 6. Parser e recuperação de erros

Cada não-terminal `<X>` tem um método `parseX()` em `src/Parser.java`. Os terminais são reconhecidos com `match()`, e os não-terminais chamam seus respectivos métodos. A escolha das alternativas usa FIRST; a escolha de ε usa FOLLOW. As repetições de listas e operadores foram implementadas com laços, evitando chamadas recursivas desnecessárias, como no exemplo de EBNF e `while` da Aula 4, slide 6.

Quando não encontra uma alternativa válida, o parser registra o não-terminal, a linha, a coluna e os tokens esperado e encontrado. Depois, tenta retomar a análise por sincronização. O método `match()` retorna `true` ao consumir o token esperado. Se o token for diferente, registra o erro e retorna `false`, sem lançar exceção. O método que o chamou trata esse retorno, sincroniza e termina. Quando a escolha da alternativa já confirmou o token, `match()` pode consumi-lo diretamente.

A sincronização usa FOLLOW(X) ∪ {`;`, `}`, EOF}. Nas listas Itens e Elementos, usa FIRST da lista ∪ FOLLOW da lista ∪ {EOF}, para procurar o próximo item. Tokens inválidos são descartados até encontrar um desses pontos. Se o token atual já for um ponto de sincronização, ele fica para o método chamador. Se uma lista encontrar EOF antes da hora, registra o erro e termina; o chamador pode então indicar a falta de `}`.

As repetições não vazias consomem tokens antes de começar outra volta. A sincronização avança até um ponto de retomada ou EOF. Se ela parar sem consumir nada, o método retorna e o chamador continua. As listas só repetem quando o próximo token inicia um item. Essas condições evitam que o parser fique preso no mesmo token após um erro.

Os erros do scanner também entram no resultado final. Mesmo que os tokens restantes formem uma sequência válida, o programa é rejeitado se houver erro léxico. O método `parse()` retorna um `ResultadoParse`, contendo a árvore e a lista de erros. Quando há erro léxico ou sintático, a árvore disponibilizada é `null`. O método `aceito()` indica se a análise foi bem-sucedida. O resultado é guardado para consultas posteriores à mesma instância.