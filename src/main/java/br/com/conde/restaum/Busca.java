package br.com.conde.restaum;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A busca em profundidade para os tabuleiros grandes demais para varrer inteiros.
 *
 * <p>A cruz inglesa tem 33 casas, ou seja, 8,6 bilhoes de posicoes possiveis: nao
 * da para marcar todas. Entao em vez de visitar tudo a busca desce ate o fim e
 * volta, guardando so as posicoes que ja se mostraram sem solucao.
 *
 * <p>As ajudas ligam e desligam de fora, para dar para medir o que cada uma
 * economiza: a memoria das posicoes perdidas, o rotulo como poda e a simetria do
 * tabuleiro para tratar posicoes espelhadas como a mesma.
 */
public final class Busca {

    /** Como a busca usa as simetrias do tabuleiro para encurtar a memoria. */
    public enum Espelho {
        /** Cada posicao e ela mesma. */
        NENHUM,
        /** So os espelhos que deixam a casa do alvo no lugar. O certo. */
        QUE_FIXAM_O_ALVO,
        /** Todos os espelhos do tabuleiro, o alvo que se vire. Mede o estrago. */
        TODOS
    }

    private final Tabuleiro tabuleiro;
    private final boolean usaMemoria;
    private final boolean usaRotulo;
    private final Espelho espelho;
    private final Rotulo rotulo;
    private final Set<Long> perdidas = new HashSet<>();
    private final Deque<int[]> caminho = new ArrayDeque<>();
    private int[][] espelhosEmUso = new int[0][];
    private long nos;
    private long cortesDoRotulo;
    private long cortesDaMemoria;
    private int alvo;

    public Busca(Tabuleiro tabuleiro, boolean usaMemoria, boolean usaRotulo, Espelho espelho) {
        this.tabuleiro = tabuleiro;
        this.usaMemoria = usaMemoria;
        this.usaRotulo = usaRotulo;
        this.espelho = espelho;
        this.rotulo = usaRotulo ? new Rotulo(tabuleiro) : null;
    }

    /** A busca com as ajudas todas ligadas e a simetria do jeito certo. */
    public static Busca completa(Tabuleiro tabuleiro) {
        return new Busca(tabuleiro, true, true, Espelho.QUE_FIXAM_O_ALVO);
    }

    /**
     * Procura uma partida que saia de {@code comeco} e termine com uma peca so na
     * casa {@code alvo} ({@code -1} para qualquer casa). Devolve as jogadas em
     * ordem, ou {@code null} se nao existir.
     */
    public List<int[]> resolve(long comeco, int alvo) {
        this.alvo = alvo;
        nos = 0;
        cortesDoRotulo = 0;
        cortesDaMemoria = 0;
        perdidas.clear();
        caminho.clear();
        this.espelhosEmUso = escolheEspelhos(alvo);
        if (!desce(comeco)) {
            return null;
        }
        return new ArrayList<>(caminho);
    }

    /**
     * Os espelhos que valem para este alvo.
     *
     * <p>Guardar "esta posicao esta perdida" com a chave espelhada so e honesto se o
     * espelho deixar o alvo onde estava. Senao a posicao espelhada tem outro alvo, e
     * a memoria passa a dar por perdido o que nao esta.
     */
    private int[][] escolheEspelhos(int alvo) {
        if (espelho == Espelho.NENHUM) {
            return new int[0][];
        }
        if (espelho == Espelho.TODOS || alvo < 0) {
            return tabuleiro.simetrias();
        }
        List<int[]> servem = new ArrayList<>();
        for (int[] simetria : tabuleiro.simetrias()) {
            if (simetria[alvo] == alvo) {
                servem.add(simetria);
            }
        }
        return servem.toArray(new int[0][]);
    }

    private boolean desce(long posicao) {
        nos++;
        if (Long.bitCount(posicao) == 1) {
            return alvo < 0 || posicao == (1L << alvo);
        }
        if (usaRotulo && alvo >= 0 && !rotulo.deixaTerminarEm(posicao, alvo)) {
            cortesDoRotulo++;
            return false;
        }
        long chave = canonica(posicao);
        if (usaMemoria && perdidas.contains(chave)) {
            cortesDaMemoria++;
            return false;
        }
        for (int[] jogada : tabuleiro.jogadas()) {
            if (!tabuleiro.cabe(posicao, jogada)) {
                continue;
            }
            caminho.addLast(jogada);
            if (desce(tabuleiro.aplica(posicao, jogada))) {
                return true;
            }
            caminho.removeLast();
        }
        if (usaMemoria) {
            perdidas.add(chave);
        }
        return false;
    }

    /** A menor das posicoes espelhadas, para as simetricas virarem uma so. */
    public long canonica(long posicao) {
        long menor = posicao;
        for (int[] simetria : espelhosEmUso) {
            long espelhada = tabuleiro.peloEspelho(posicao, simetria);
            if (espelhada < menor) {
                menor = espelhada;
            }
        }
        return menor;
    }

    /** Quantos espelhos a ultima busca usou. */
    public int espelhosEmUso() {
        return espelhosEmUso.length;
    }

    /** Quantas posicoes a busca abriu. */
    public long nos() {
        return nos;
    }

    /** Quantas vezes o rotulo cortou um galho inteiro. */
    public long cortesDoRotulo() {
        return cortesDoRotulo;
    }

    /** Quantas vezes a memoria reconheceu uma posicao ja perdida. */
    public long cortesDaMemoria() {
        return cortesDaMemoria;
    }

    /** Quantas posicoes perdidas a memoria guardou. */
    public int tamanhoDaMemoria() {
        return perdidas.size();
    }
}
