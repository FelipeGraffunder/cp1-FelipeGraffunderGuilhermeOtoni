# Esboço da AST

Para o programa:

```c
int f(int x) {
 if (x > 0) { while (x > 1) { x = x - 1; } } else { return 0; }
 return x;
}
```

O esboço abaixo representa os 26 nós da AST. Os rótulos entre colchetes
indicam o papel dos filhos; não são nós adicionais.

```text
Programa
└── Funcao: f
    ├── Tipo: int [retorno]
    ├── Parametro: x
    │   └── Tipo: int
    └── Bloco [corpo da função]
        ├── Se
        │   ├── Bin: > [condição]
        │   │   ├── Id: x
        │   │   └── Literal int: 0
        │   ├── Bloco [então]
        │   │   └── Enquanto
        │   │       ├── Bin: > [condição]
        │   │       │   ├── Id: x
        │   │       │   └── Literal int: 1
        │   │       └── Bloco [corpo da repetição]
        │   │           └── Atribuicao
        │   │               ├── Id: x [alvo]
        │   │               └── Bin: - [valor]
        │   │                   ├── Id: x
        │   │                   └── Literal int: 1
        │   └── Bloco [senão]
        │       └── Retorno
        │           └── Literal int: 0
        └── Retorno
            └── Id: x
```

O último `Retorno` pertence ao corpo da função, depois da seleção; ele não
é filho do `Se` nem do `Enquanto`. Na atribuição, o primeiro `Id(x)` é o
alvo e o `Bin(-)` representa o valor calculado.

Ao lado dessa estrutura, podem-se identificar três partes descartadas da
árvore de derivação:

1. A cadeia `Expressao → Ou → E → Igualdade → Relacional → Aditiva →
   Multiplicativa → Unaria → Primaria → id ChamadaOpcional`, com auxiliares
   vazios, torna-se apenas `Id(x)` quando a expressão é um identificador.
2. Em `x - 1`, `MaisAditiva` e sua alternativa vazia não aparecem: fica
   somente `Bin(-)` com os dois operandos. Os níveis de precedência são
   preservados pela ligação entre os nós, sem nós auxiliares.
3. `RestoItem`, usado para fatorar a declaração da função, é descartado;
   parênteses, chaves e ponto e vírgula também não viram nós, pois os campos
   e os blocos já representam seus limites e agrupamentos.

