# Árvore de Sintaxe Abstrata e Visitor

Integrantes: Felipe Canabarro e Guilherme Otoni.

O projeto realiza a análise léxica e sintática de programas, constrói uma árvore de sintaxe abstrata (AST) e oferece duas formas de saída: a representação textual da árvore e a reimpressão do código-fonte.

## 1. Requisitos

- JDK 11 ou superior.
- Não são necessárias bibliotecas externas.

## 2. Executar os testes

Abra um terminal na pasta do projeto.

No Windows:

```powershell
.\testar.bat
```

No Linux ou macOS:

```bash
sh testar.sh
```

Os scripts compilam o projeto e executam os testes do parser e da AST.

Caso seja necessário selecionar uma instalação específica do Java, configure a variável `JAVA_HOME` antes de executar o script. Exemplo no PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-26.0.1'
.\testar.bat
```

Ajuste o caminho conforme a instalação disponível no computador.

## 3. Executar o analisador

Após a compilação, utilize o comando abaixo para analisar um arquivo e imprimir sua AST:

```bash
java -cp build Main test/entradas/cp3-controle.txt
```

Para reimprimir o programa em sua forma canônica, acrescente `--fonte`:

```bash
java -cp build Main test/entradas/cp3-controle.txt --fonte
```

Substitua o caminho do exemplo pelo arquivo que deseja analisar. `ParserMain` também pode ser utilizado com os mesmos argumentos.

Se a entrada contiver erros léxicos ou sintáticos, o programa apresenta os diagnósticos e termina com código de saída `1`. Falhas de leitura do arquivo retornam código `2`.

## 4. Organização dos arquivos

| Caminho | Conteúdo |
|---|---|
| `ast-design.md` | Hierarquia dos nós, mapeamento da gramática, decisões de representação e exemplo de árvore. |
| `gramatica-first-follow.md` | Gramática e conjuntos FIRST e FOLLOW. |
| `src/` | Analisador léxico, parser, classes de resultado e pontos de entrada. |
| `src/ast/` | Classes dos nós da AST. |
| `src/visitor/` | Interface Visitor, impressora da árvore e reimpressora de código-fonte. |
| `test/ParserTest.java` | Testes de reconhecimento e recuperação de erros. |
| `test/AstTest.java` | Testes de construção, impressão e reimpressão da AST. |
| `test/entradas/` | Programas utilizados nos testes. |
| `test/esperado/` | Árvores de referência para comparação com a saída da impressora. |
| `testar.bat` | Script de compilação e testes para Windows. |
| `testar.sh` | Script de compilação e testes para Linux e macOS. |
| `verificacao.txt` | Registro dos resultados de validação. |
| `log-uso-ia.md` | Registro de uso de ferramentas de assistência no desenvolvimento. |

## 5. Cobertura dos testes

A suíte verifica:

- declarações e literais dos cinco tipos básicos;
- funções, procedimentos, parâmetros e chamadas;
- atribuições, seleções, repetições e retornos;
- precedência e associatividade dos operadores;
- agrupamento de expressões e eliminação de parênteses redundantes na AST;
- comparação das árvores com os arquivos esperados;
- posições dos nós;
- estabilidade da reimpressão após nova análise;
- rejeição de entradas inválidas e recuperação de múltiplos erros;
- entradas aleatórias determinísticas.

Quando a análise encontra erros, a árvore disponibilizada no resultado é `null`.