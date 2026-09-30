## Como executar

É necessário ter um **JDK 17 ou mais recente** instalado. Não são usadas bibliotecas externas, Maven, JUnit ou JFlex.

### Compilar

```sh
javac -encoding UTF-8 -d build @fontes.txt
```

### Rodar os testes

```sh
java -cp build ScannerTest
```

O resultado esperado é `15 passaram; 0 falharam.` Os testes usam verificações que lançam `AssertionError`, então não é necessário usar a opção `-ea`.

No Windows, também é possível executar `testar.bat`. No Linux ou macOS, use `bash testar.sh`. Esses scripts compilam, executam os testes e salvam a saída em `evidencias/testes-local.txt`.

### Analisar um arquivo

```sh
java -cp build Main exemplos/programa.txt
java -cp build Main exemplos/com-erros.txt
```
