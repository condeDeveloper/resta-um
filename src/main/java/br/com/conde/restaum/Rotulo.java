package br.com.conde.restaum;

import java.util.ArrayList;
import java.util.List;

/**
 * O juiz que nao procura nada: um rotulo por casa, tirado de um corpo de quatro
 * elementos, que nenhuma jogada consegue mexer.
 *
 * <p>Os quatro elementos sao 0, 1, x e x+1, com x*x = x+1 e soma bit a bit. Os tres
 * que nao sao zero somam zero: 1 + x + (x+1) = 0. Entao, se cada trinca de uma
 * jogada receber os tres rotulos diferentes de zero, tirar duas pecas e por uma
 * terceira nao muda a soma dos rotulos das pecas no tabuleiro.
 *
 * <p>Com isso da para dizer, sem jogar nada, que a ultima peca nunca vai parar em
 * certas casas: a soma do comeco tem de bater com o rotulo da casa que sobrar.
 */
public final class Rotulo {

    private final Tabuleiro tabuleiro;
    private final int[][] pares;
    private final int[][] valor;

    /** Usa os pares de expoente que o proprio tabuleiro oferece. */
    public Rotulo(Tabuleiro tabuleiro) {
        this(tabuleiro, tabuleiro.paresDeRotulo());
    }

    /**
     * Monta o rotulo com os pares dados e confere, jogada por jogada, que ele de
     * fato nao muda. Par que nao serve e recusado na hora, nao no resultado.
     */
    public Rotulo(Tabuleiro tabuleiro, int[][] pares) {
        this.tabuleiro = tabuleiro;
        this.pares = pares;
        this.valor = new int[pares.length][tabuleiro.casas()];
        int[] u = tabuleiro.malhaU();
        int[] v = tabuleiro.malhaV();
        for (int p = 0; p < pares.length; p++) {
            for (int casa = 0; casa < tabuleiro.casas(); casa++) {
                int expoente = Math.floorMod(pares[p][0] * u[casa] + pares[p][1] * v[casa], 3);
                valor[p][casa] = expoente + 1;
            }
        }
        for (int p = 0; p < pares.length; p++) {
            for (int[] jogada : tabuleiro.jogadas()) {
                int soma = valor[p][jogada[0]] ^ valor[p][jogada[1]] ^ valor[p][jogada[2]];
                if (soma != 0) {
                    throw new IllegalArgumentException(
                            "o par (" + pares[p][0] + ", " + pares[p][1] + ") nao serve em "
                                    + tabuleiro.nome() + ": a jogada "
                                    + tabuleiro.nomeDaCasa(jogada[0]) + " para "
                                    + tabuleiro.nomeDaCasa(jogada[2]) + " mexe na soma");
                }
            }
        }
    }

    public int quantosRotulos() {
        return pares.length;
    }

    /** O rotulo da casa no enesimo par, como numero de 1 a 3. */
    public int rotuloDaCasa(int qual, int casa) {
        return valor[qual][casa];
    }

    /** A soma dos rotulos das pecas de uma posicao, no enesimo par. */
    public int soma(int qual, long posicao) {
        int soma = 0;
        for (int casa = 0; casa < tabuleiro.casas(); casa++) {
            if ((posicao & (1L << casa)) != 0) {
                soma ^= valor[qual][casa];
            }
        }
        return soma;
    }

    /** As somas de uma posicao, uma por par. */
    public int[] somas(long posicao) {
        int[] fora = new int[pares.length];
        for (int p = 0; p < pares.length; p++) {
            fora[p] = soma(p, posicao);
        }
        return fora;
    }

    /** O rotulo deixa a partida que comeca assim terminar nessa casa? */
    public boolean deixaTerminarEm(long comeco, int casa) {
        for (int p = 0; p < pares.length; p++) {
            if (soma(p, comeco) != valor[p][casa]) {
                return false;
            }
        }
        return true;
    }

    /** As casas em que a ultima peca ainda pode parar, pelo rotulo. */
    public int[] casasPermitidas(long comeco) {
        List<Integer> casas = new ArrayList<>();
        for (int c = 0; c < tabuleiro.casas(); c++) {
            if (deixaTerminarEm(comeco, c)) {
                casas.add(c);
            }
        }
        int[] fora = new int[casas.size()];
        for (int i = 0; i < fora.length; i++) {
            fora[i] = casas.get(i);
        }
        return fora;
    }

    /**
     * O rotulo deixa essa posicao acontecer, saindo daquele comeco? Vale para
     * qualquer posicao, nao so para as de uma peca.
     */
    public boolean deixaChegarEm(long comeco, long posicao) {
        for (int p = 0; p < pares.length; p++) {
            if (soma(p, comeco) != soma(p, posicao)) {
                return false;
            }
        }
        return true;
    }
}
