# Como executar

É necessário ter um **JDK 17 ou mais recente** instalado. Não são usadas bibliotecas externas, Maven ou JUnit.

Abra um terminal dentro da pasta `cp2-canabarro-otoni` e execute os comandos abaixo.

### Compilar

```sh
javac --release 17 -encoding UTF-8 -d build "@fontes.txt"
```

### Rodar os testes

```sh
java -cp build ParserTest
```

O resultado esperado é `7 passaram; 0 falharam.` Os testes usam verificações que lançam `AssertionError`, então não é necessário usar a opção `-ea`.

No Windows, também é possível executar `testar.bat`. No Linux ou macOS, use `bash testar.sh`. Esses scripts compilam, executam os testes e mostram os resultados no terminal, sem salvar arquivos de evidências.

### Analisar um arquivo

```sh
java -cp build Main test/entradas/valido-tipos.txt
java -cp build Main test/entradas/invalido-sem-ponto-virgula-eof.txt
```

O programa mostra `ACEITO` ou `REJEITADO`. Quando há erros, mostra as mensagens com linha e coluna.

Para analisar outro código, substitua o caminho pelo nome do seu arquivo `.txt`.

Sempre recompile depois de alterar um arquivo Java.
