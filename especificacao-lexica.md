# Especificação léxica — Parte 1

Integrantes: Felipe Canabarro e Guilherme Otoni

## 1. Tokens

| Token | Notação | Exemplo |
|---|---|---|
| Identificador | `[A-Za-z][A-Za-z0-9_]{0,49}` | `valor_total` |
| Palavra reservada | Identificador presente na lista abaixo | `int` |
| String | `aspas_duplas caractere_string* aspas_duplas` | `"ola"` |
| Caractere | `aspas_simples caractere_char aspas_simples` | `'a'` |
| Número | `[0-9]+(\.[0-9]+)?` | `3.14` |
| Operador | Símbolos da seção 3 | `>=` |
| Delimitador | `[(){};,]` | `;` |

## 2. Regras dos tokens

- **Identificadores:** começam com letra e depois aceitam letras, números e `_`. Limite de 50 caracteres.
- **Maiúsculas e minúsculas:** fazem diferença. `int` é reservada; `Int` é identificador.
- **Strings:** usam aspas duplas e podem ser vazias. O lexema retornado contém apenas o conteúdo, sem as aspas.
- **Char:** usa aspas simples e contém exatamente um caractere.
- **Números:** inteiros ou decimais. Decimais exigem dígitos antes e depois do ponto. Sem notação científica ou hexadecimal. O sinal é separado: `-10` gera `-` e `10`.

Na tabela, `aspas_duplas` é `"` e `aspas_simples` é `'`. `caractere_string` e `caractere_char` aceitam qualquer caractere, exceto a aspa que fecha o literal e quebras de linha. Acentos, tabulações e barras invertidas comuns são permitidos. Não há interpretação de escapes: uma barra seguida da letra n continua sendo dois caracteres, portanto não forma um literal char válido. Os espaços da notação apenas separam os nomes.

Palavras reservadas:

```text
int double bool char string void if else while return true false
```

O scanner consulta a lista após validar o identificador completo. `true` e `false` são booleanos. Funções aceitam parâmetros e retorno; `void` indica ausência de valor de retorno.

## 3. Operadores

- Aritméticos: `+`, `-`, `*`, `/`, `%`.
- Atribuição: `=`.
- Relacionais e de igualdade: `<`, `>`, `<=`, `>=`, `==`, `!=`.
- Lógicos: `!`, `&&`, `||`.

`&` e `|` sozinhos são inválidos.

## 4. Alfabeto, espaços e comentários

Strings, caracteres e comentários não têm restrição ASCII. Identificadores continuam usando letras de A a Z e de a a z, dígitos e sublinhado conforme a regra da seção 1. Fora dos literais e comentários, caracteres que não formam um token válido geram erro. São aceitas quebras de linha `LF`, `CRLF` e `CR`, cada uma contando como uma quebra.

Espaços, tabulações e quebras entre tokens são ignorados, mas linha e coluna são atualizadas. O contexto importa: `@` é válido em strings e comentários, mas não sozinho no código.

Comentários usam `//` até o fim da linha ou do arquivo. Aceitam qualquer conteúdo até a quebra de linha ou EOF, sem gerar tokens. Não há comentários de bloco nem aninhamento.

## 5. Reconhecimento e erros

O scanner reconhece o maior token possível (**maximal munch**). Assim, `>=` é um operador e `integer` é um identificador.

Erros informam linha e coluna. Depois do descarte, a leitura continua:

- **Identificador acima de 50 caracteres:** lê e descarta toda a sequência de letras, números e `_`, sem gerar tokens. Erro no início.
- **Caractere inválido:** informa sua posição e o descarta.
- **String ou char inválido:** erro na aspa inicial. Descarta até a aspa final, incluindo-a, ou até a quebra de linha ou fim do arquivo. A falta de fechamento também gera erro.

Em `10.`, falta um dígito após o ponto: o scanner registra um erro na posição inicial e descarta o literal inteiro, sem emitir o número `10`. O próximo caractere fica disponível para leitura. Em `.5`, o ponto gera erro e depois é emitido o número `5`. Em `10.0a`, são emitidos o número `10.0` e o identificador `a`. `1nome` gera `1` e `nome`; a análise sintática verifica se essa sequência é permitida.
