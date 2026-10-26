package br.com.conde.restaum;

import java.util.ArrayList;
import java.util.List;

/**
 * A varredura completa de um comeco: todas as posicoes que da para alcancar,
 * em ordem de peca removida.
 *
 * <p>Toda jogada tira exatamente uma peca, entao a busca em largura visita as
 * posicoes em ordem de quantidade de pecas. Essa ordem e o que deixa contar
 * caminho com uma passada so, sem recursao e sem repeticao.
 */
public final class Varredura {

    /** Acima disso a tabela de posicoes nao cabe na memoria de um runner. */
    public static final int LIMITE_DE_CASAS = 22;

    private final Tabuleiro tabuleiro;
    private final int inicio;
    private final int[] ordem;
    private final int[] veioDe;
    private final int[] porJogada;
    private final boolean[] visto;
    private final int minimo;
    private final int[] porPecas;
    private final int travadas;
    private final int piorTravada;
    private long[] caminhos;

    public Varredura(Tabuleiro tabuleiro, int casaVazia) {
        this(tabuleiro, tabuleiro.cheioMenos(casaVazia));
    }

    public Varredura(Tabuleiro tabuleiro, long comeco) {
        if (tabuleiro.casas() > LIMITE_DE_CASAS) {
            throw new IllegalArgumentException(
                    "varredura completa so ate " + LIMITE_DE_CASAS + " casas, e "
                            + tabuleiro.nome() + " tem " + tabuleiro.casas());
        }
        this.tabuleiro = tabuleiro;
        this.inicio = (int) comeco;
        int tamanho = 1 << tabuleiro.casas();
        this.visto = new boolean[tamanho];
        this.veioDe = new int[tamanho];
        this.porJogada = new int[tamanho];
        int[] fila = new int[tamanho];
        int fim = 0;
        fila[fim++] = inicio;
        visto[inicio] = true;
        veioDe[inicio] = -1;
        porJogada[inicio] = -1;
        int[][] jogadas = tabuleiro.jogadas();
        int menos = Integer.bitCount(inicio);
        int travadas = 0;
        int pior = 0;
        for (int cabeca = 0; cabeca < fim; cabeca++) {
            int posicao = fila[cabeca];
            boolean parado = true;
            for (int j = 0; j < jogadas.length; j++) {
                int[] jogada = jogadas[j];
                if (!tabuleiro.cabe(posicao, jogada)) {
                    continue;
                }
                parado = false;
                int filho = (int) tabuleiro.aplica(posicao, jogada);
                if (!visto[filho]) {
                    visto[filho] = true;
                    veioDe[filho] = posicao;
                    porJogada[filho] = j;
                    fila[fim++] = filho;
                }
            }
            if (parado) {
                int pecas = Integer.bitCount(posicao);
                menos = Math.min(menos, pecas);
                if (pecas > 1) {
                    travadas++;
                    pior = Math.max(pior, pecas);
                }
            }
        }
        this.ordem = java.util.Arrays.copyOf(fila, fim);
        this.minimo = menos;
        this.travadas = travadas;
        this.piorTravada = pior;
        int[] porPecas = new int[tabuleiro.casas() + 1];
        for (int posicao : ordem) {
            porPecas[Integer.bitCount(posicao)]++;
        }
        this.porPecas = porPecas;
    }

    public Tabuleiro tabuleiro() {
        return tabuleiro;
    }

    public long comeco() {
        return inicio & 0xFFFFFFFFL;
    }

    /** Quantas posicoes diferentes da para alcancar, contando a do comeco. */
    public int posicoes() {
        return ordem.length;
    }

    /** Quantas posicoes alcancaveis tem cada quantidade de pecas. */
    public int[] posicoesPorPecas() {
        return porPecas.clone();
    }

    /** O menor numero de pecas que sobra, no melhor jogo possivel. */
    public int minimoDePecas() {
        return minimo;
    }

    public boolean restaUma() {
        return minimo == 1;
    }

    /** Quantas posicoes alcancaveis travam com duas pecas ou mais. */
    public int becos() {
        return travadas;
    }

    /** O maior numero de pecas que da para deixar travado no tabuleiro. */
    public int piorBeco() {
        return piorTravada;
    }

    public boolean alcanca(long posicao) {
        return posicao >= 0 && posicao < visto.length && visto[(int) posicao];
    }

    /** As casas onde a ultima peca pode parar. */
    public int[] finais() {
        List<Integer> casas = new ArrayList<>();
        for (int c = 0; c < tabuleiro.casas(); c++) {
            if (visto[1 << c]) {
                casas.add(c);
            }
        }
        int[] fora = new int[casas.size()];
        for (int i = 0; i < fora.length; i++) {
            fora[i] = casas.get(i);
        }
        return fora;
    }

    /** Quantas sequencias de jogadas diferentes levam a essa posicao. */
    public long caminhosAte(long posicao) {
        if (!alcanca(posicao)) {
            return 0;
        }
        if (caminhos == null) {
            contaCaminhos();
        }
        return caminhos[(int) posicao];
    }

    /** Quantas partidas diferentes terminam com uma peca so. */
    public long partidasQueResolvem() {
        long total = 0;
        for (int casa : finais()) {
            total += caminhosAte(1L << casa);
        }
        return total;
    }

    private void contaCaminhos() {
        long[] conta = new long[visto.length];
        conta[inicio] = 1;
        int[][] jogadas = tabuleiro.jogadas();
        for (int posicao : ordem) {
            long quantos = conta[posicao];
            if (quantos == 0) {
                continue;
            }
            for (int[] jogada : jogadas) {
                if (tabuleiro.cabe(posicao, jogada)) {
                    conta[(int) tabuleiro.aplica(posicao, jogada)] += quantos;
                }
            }
        }
        caminhos = conta;
    }

    /** Uma sequencia de jogadas do comeco ate a posicao, do jeito que a busca achou. */
    public List<int[]> caminhoAte(long posicao) {
        if (!alcanca(posicao)) {
            throw new IllegalArgumentException("posicao fora do alcance deste comeco");
        }
        List<int[]> passos = new ArrayList<>();
        int atual = (int) posicao;
        while (atual != inicio) {
            passos.add(tabuleiro.jogadas()[porJogada[atual]]);
            atual = veioDe[atual];
        }
        java.util.Collections.reverse(passos);
        return passos;
    }
}
