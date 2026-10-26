# resta-um

Resta um em Java 21, com o espaço de posições varrido inteiro nos tabuleiros pequenos e um
juiz algébrico que decide, **sem abrir uma posição sequer**, onde a última peça pode parar.

O jogo é aquele: tabuleiro cheio de pinos com um buraco, cada jogada pula um vizinho em
linha reta e cai na casa vazia do outro lado, e o pino pulado sai. A pergunta é sempre a
mesma: dá para terminar com um pino só, e onde?

Aqui a pergunta é respondida de dois jeitos que não conversam entre si. Um varre todas as
posições alcançáveis e sabe a verdade. O outro é uma conta de uma linha que não joga nada e
mesmo assim recusa a maior parte dos destinos. Os dois são confrontados casa por casa, e a
diferença entre eles é o assunto deste repositório.

## O que tem aqui

- `Tabuleiro`: as casas, as jogadas e as simetrias de cada desenho. Triângulos de lado 3 a 8,
  quadrados, a cruz inglesa de 33 casas, a europeia de 37 e a alemã de 45. Uma posição é um
  `long` com um bit por casa.
- `Varredura`: a busca em largura que alcança tudo o que dá para alcançar. Como toda jogada
  tira exatamente uma peça, a largura já visita as posições em ordem de peça removida, e essa
  ordem é o que deixa **contar partidas** com uma passada só.
- `Rotulo`: o juiz que não joga. Cada casa recebe um dos três elementos não nulos de um corpo
  de quatro, escolhidos de modo que os três de cada trinca em linha sempre somem zero. Aí
  tirar duas peças e pôr uma terceira não mexe na soma do tabuleiro, e a soma do começo já
  diz onde a última peça pode parar.
- `Busca`: a busca em profundidade para a cruz inglesa, grande demais para varrer (8,6
  bilhões de posições). Memória das posições perdidas, rótulo e simetria entram e saem por
  fora, para dar para medir o que cada um faz.
- `Juiz`: refaz a partida jogada por jogada e não acredita em ninguém.
- `Medidor`: as tabelas abaixo.

## Como rodar

```bash
mvn -B verify
mvn -q exec:java -Dexec.args="tabuleiros"   # os desenhos e o tamanho de cada um
mvn -q exec:java -Dexec.args="inicios"      # o triângulo de 15 casa a casa
mvn -q exec:java -Dexec.args="rotulo"       # o juiz contra a varredura
mvn -q exec:java -Dexec.args="provas"       # quanto custa provar que não dá
mvn -q exec:java -Dexec.args="espelho"      # o estrago de espelhar a memória errado
mvn -q exec:java -Dexec.args="cruz"         # a cruz inglesa de 33 casas
```

## Cinco achados

### 1. Uma soma de uma linha elimina 28 das 33 casas

Na cruz inglesa com o buraco no centro, o rótulo diz que a última peça só pode parar em
**d1, a4, d4, g4 ou d7**. São 5 casas de 33, decididas por uma soma sobre a posição inicial,
sem abrir uma única posição da busca.

E não é um palpite frouxo: a busca acha partida de verdade para as cinco. Nesse tabuleiro o
juiz não sobra nem falta.

### 2. O mesmo juiz é inútil nos tabuleiros pequenos e exato nos grandes

| tabuleiro | pares (começo, destino) que o rótulo libera | que acontecem de verdade |
|---|---|---|
| triângulo de 6 casas | 12 | **0** |
| quadrado de 9 casas | 9 | **0** |
| triângulo de 10 casas | 18 | 6 |
| triângulo de 15 casas | 75 | 54 |
| quadrado de 16 casas | 16 | **16** |
| triângulo de 21 casas | 147 | **147** |

No triângulo de 6 casas o rótulo libera doze combinações e **nenhuma** acontece, porque esse
tabuleiro não resolve de jeito nenhum. No triângulo de 21 e no quadrado de 16 ele acerta casa
por casa. O palpite melhora conforme o tabuleiro cresce, e isso não é coincidência: com mais
casas, mais jogadas para gastar a folga que o rótulo não vê.

O que o rótulo **nunca** faz, em nenhum dos seis tabuleiros varridos, é deixar de fora um
destino que acontece. Ele erra sempre para mais.

### 3. Um invariante não serve de poda

Eu queria usar o rótulo dentro da busca, cortando galho no meio do caminho. Medi:

| pergunta | nós sem o rótulo | nós com o rótulo |
|---|---|---|
| triângulo de 15, do canto para a casa 13 (dá) | 771 | 771 |
| triângulo de 15, do canto para a casa 5 (não dá) | 10.307 | 10.307 |
| triângulo de 15, do canto para a casa 2 (não dá) | 10.307 | **1** |
| triângulo de 21, do canto para a casa 20 (dá) | 2.515 | 2.515 |
| triângulo de 21, do canto para a casa 2 (não dá) | **1.858.849** | **1** |

Os números iguais não são erro de medida. Se o rótulo deixa o começo, ele deixa toda posição
que vier do começo: é essa a graça de ser invariante. Ou ele corta no primeiro nó, ou não
corta nunca.

A compensação está na última linha: dois milhões de posições abertas, contra uma conta.

### 4. Onde você começa decide quase tudo

No triângulo de 15 casas, o do restaurante, toda casa vazia inicial resolve. Só que não
resolve do mesmo jeito:

| buraco inicial | partidas que terminam com um pino | destinos possíveis |
|---|---|---|
| num canto (1, 11, 15) | 29.760 | 4 |
| no meio de um lado (4, 6, 13) | 85.258 | 5 |
| vizinho de canto (2, 3, 7, 10, 12, 14) | 14.880 | 4 |
| numa das três casas do miolo (5, 8, 9) | **1.550** | **1** |

Começar pelo miolo dá 55 vezes menos caminhos que começar pelo meio de um lado, e o último
pino fica obrigado a parar numa casa só, do outro lado do tabuleiro.

### 5. A mesma pergunta, girada, custa dez vezes mais

As quatro pontas da cruz inglesa são o mesmo problema virado. Para a busca, não são:

| destino | nós abertos até achar |
|---|---|
| d4 (o centro) | 832.033 |
| d1 | 914.284 |
| a4 | 3.809.956 |
| d7 | 9.342.453 |

A diferença é inteira da ordem em que as jogadas estão guardadas. Girar o tabuleiro 90 graus
não muda o problema e multiplica o trabalho por dez.

## As tabelas

### Os tabuleiros

```
tabuleiro      casas jogadas espelhos partida posicoes  becos pior comecos parados
triangulo 3        6       6        6       4        6      1    2               3
triangulo 4       10      18        6       8       42      6    3               1
triangulo 5       15      36        6      13     3016    118    8               0
triangulo 6       21      60        6      19   291987   1429   12               0
quadrado 3         9      12        8       7       16      4    5               1
quadrado 4        16      32        8      14     1687     65   12               0
cruz inglesa      33      76        8      31        -      -    -               -
cruz europeia     37      92        8      35        -      -    -               -
cruz alema        45     108        8      43        -      -    -               -
```

`partida` é o tamanho de qualquer partida completa: duas jogadas a menos que o número de
casas, sempre, porque cada jogada tira uma peça e a última fica. `posicoes`, `becos` e `pior`
são da varredura com o canto vazio. `comecos parados` conta as casas vazias iniciais que não
têm **nenhuma** jogada possível: três no triângulo de 6 casas, e o centro do quadrado de 9,
que trava com oito peças no tabuleiro e nada a fazer.

### O triângulo de 15, casa por casa

```
vazia posicoes minimo becos  partidas finais
    1     3016      1   118     29760 1 7 10 13
    2     2377      1   113     14880 2 6 11 14
    3     2377      1   113     14880 3 4 12 15
    4     4237      1   137     85258 3 4 9 12 15
    5     1651      1    88      1550 13
    6     4237      1   137     85258 2 6 8 11 14
    7     2377      1   113     14880 1 7 10 13
    8     1651      1    88      1550 6
    9     1651      1    88      1550 4
   10     2377      1   113     14880 1 7 10 13
   11     3016      1   118     29760 2 6 11 14
   12     2377      1   113     14880 3 4 12 15
   13     4237      1   137     85258 1 5 7 10 13
   14     2377      1   113     14880 2 6 11 14
   15     3016      1   118     29760 3 4 12 15
```

As casas são numeradas de cima para baixo, 1 no topo e 11 a 15 na linha de baixo. `becos` são
as posições alcançáveis em que não resta jogada nenhuma com duas peças ou mais: 118 jeitos de
travar saindo do canto, e dá para travar com **oito** peças ainda no tabuleiro.

No triângulo de 21 casas o canto vazio dá 291.987 posições alcançáveis e
**43.419.942.138** partidas que terminam com um pino.

### A memória espelhada do jeito errado

A busca guarda "esta posição está perdida" para não repetir trabalho. Como o tabuleiro tem
simetrias, a tentação é guardar a menor das posições espelhadas e tratar as oito como uma.

Só que o destino exigido também está no tabuleiro. Espelhar a posição e não espelhar o
destino troca a pergunta no meio do caminho:

```
tabuleiro      pares da  | so os que fixam o alvo: erros  nos | todos: erros  nos
triangulo 3       36   0 |                             0     60 |            0     60
triangulo 4      100   6 |                             0   1517 |            0   1271
triangulo 5      225  54 |                             0 185676 |           12 209520
quadrado 3        81   0 |                             0    141 |            0    141
quadrado 4       256  16 |                             0   1993 |            0   1946
triangulo 6      441 147 |                             0 941170 |            0 980279
```

Com os espelhos certos (só os que deixam o destino onde estava), zero erros em qualquer
tabuleiro. Com os oito, **12 respostas erradas** nos 225 pares do triângulo de 15, e zero nos
outros cinco: o defeito sabe se esconder. E o erro é sempre o mesmo, dar por perdido o que
tem solução.

Na cruz inglesa o mesmo defeito respondeu **"não dá"** para o destino a4 depois de abrir
186.020.936 posições em cinco minutos. A4 dá, em 31 jogadas.

### A cruz inglesa

```
o rotulo deixa a ultima peca parar em: d1 a4 d4 g4 d7  (de 33 casas, sem abrir uma posicao sequer)
alvo  espelhos resposta         nos guardadas
d4           0 achou em 31 jogadas       939631    232216
d4           8 achou em 31 jogadas       832033    193902
d1           2 achou em 31 jogadas       914284    222570
a4           2 achou em 31 jogadas      3809956    857656
d7           2 achou em 31 jogadas      9342453   1898471
```

O centro é a única casa que os oito espelhos deixam parada, então é a única em que a memória
pode usar os oito: 11,4% de nós a menos. Nas pontas sobram dois espelhos.

E a partida do centro para o centro, que o `Medidor` imprime e o `Juiz` confere jogada por
jogada:

```
d2-d3-d4 b3-c3-d3 c1-c2-c3 e1-d1-c1 e3-e2-e1 c4-c3-c2 c1-c2-c3 d4-d3-d2 b5-b4-b3 b3-c3-d3
g3-f3-e3 e4-e3-e2 e1-e2-e3 d3-e3-f3 g5-g4-g3 g3-f3-e3 d5-c5-b5 a5-b5-c5 a3-a4-a5 f5-e5-d5
d5-c5-b5 a5-b5-c5 d7-d6-d5 d5-c5-b5 c7-c6-c5 b5-c5-d5 e7-e6-e5 d5-e5-f5 f5-f4-f3 f3-e3-d3
d2-d3-d4
```

```
  ...
  ...
.......
...O...
.......
  ...
  ...
```

## O que as medidas me corrigiram

**Eu queria o rótulo como poda da busca.** Era a ideia inteira: um teste barato que mata galho
no meio. Medi e os números vieram idênticos com e sem ele, 771 e 771, 2.515 e 2.515. Demorei a
aceitar que não era bug de medição: um invariante que vale no começo vale em toda posição que
vem do começo, então ele nunca corta no meio. Ou corta no primeiro nó, ou não corta nunca. O
contador `cortesDoRotulo` ficou no código justamente para isso ficar à vista: dá zero em toda
busca que o rótulo deixa começar.

**Canonizei a posição pelos oito espelhos com o destino fixo.** Parecia economia óbvia, e na
bancada pequena passou: zero erros no triângulo de 10, no de 21, nos dois quadrados. Só o
triângulo de 15 denunciou, com 12 respostas erradas em 225. Na cruz inglesa o mesmo defeito
cuspiu "não dá" para o a4 depois de cinco minutos e 186 milhões de posições. O conserto é uma
linha: usar só os espelhos que deixam o destino no lugar. Deixei o jeito errado no código como
`Espelho.TODOS`, porque a tabela que mede o estrago vale mais que o parágrafo que o descreve.

**Achei que o triângulo de 6 casas fosse um resta um pequeno e inofensivo.** Não resolve
nunca, e três dos seis começos não têm **nenhuma** jogada: o tabuleiro já nasce travado. O
mesmo no quadrado de 9 com o centro vazio, oito peças paradas. A varredura virou a primeira
coisa que eu rodo num tabuleiro novo, antes de qualquer busca.

**Achei que a vantagem do canto no jogo do restaurante fosse folclore.** É o contrário do que
eu supunha: o canto não é o melhor começo (29.760 partidas), o meio de um lado é (85.258). E o
pior começo, o do miolo, não é ruim por não resolver, e sim por resolver de um jeito só:
1.550 partidas, todas terminando na mesma casa.

**A varredura por largura me deu a contagem de partidas de graça.** Eu ia escrever uma
recursão com memória para contar caminho. Não precisou: como toda jogada tira exatamente uma
peça, a ordem da largura já é a ordem de quantidade de peças, e uma passada por essa ordem
soma os caminhos sem repetir nada. Foi a única parte do projeto que ficou mais simples do que
eu tinha planejado.

## Como está organizado

```
src/main/java/br/com/conde/restaum/
  Tabuleiro.java   desenhos, casas, jogadas, simetrias, malha do rótulo
  Varredura.java   busca em largura completa, contagem de partidas, becos
  Rotulo.java      o juiz algébrico, com conferência de cada jogada na montagem
  Busca.java       busca em profundidade com memória, rótulo e espelho
  Juiz.java        refaz a partida e confere passo a passo
  Medidor.java     as tabelas
src/test/java/br/com/conde/restaum/
  TabuleiroTest, VarreduraTest, RotuloTest, BuscaTest, JuizTest, MedidasTest
```

61 testes. O `MedidasTest` prende cada número deste README a uma medida, inclusive as
contagens de nós da busca, que são determinísticas.

## Licença

MIT.
