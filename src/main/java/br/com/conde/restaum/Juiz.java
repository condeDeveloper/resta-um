package br.com.conde.restaum;

import java.util.List;

/**
 * Confere uma partida jogada por outra pessoa, sem acreditar em nada de quem jogou.
 *
 * <p>A varredura e a busca podem estar as duas erradas do mesmo jeito. O juiz
 * refaz a partida do zero, jogada por jogada, e so devolve a posicao final se cada
 * passo couber: peca que pula, peca pulada e casa vazia do outro lado.
 */
public final class Juiz {

    private Juiz() {
    }

    /** Refaz a partida e devolve a posicao final; reclama no primeiro passo torto. */
    public static long confere(Tabuleiro tabuleiro, long comeco, List<int[]> jogadas) {
        long posicao = comeco;
        int passo = 0;
        for (int[] jogada : jogadas) {
            passo++;
            if (jogada.length != 3) {
                throw new IllegalArgumentException("jogada " + passo + " nao tem tres casas");
            }
            if (!temEssaJogada(tabuleiro, jogada)) {
                throw new IllegalArgumentException("jogada " + passo + " nao existe no tabuleiro: "
                        + emTexto(tabuleiro, jogada));
            }
            if (!tabuleiro.cabe(posicao, jogada)) {
                throw new IllegalArgumentException("jogada " + passo + " nao cabe: "
                        + emTexto(tabuleiro, jogada));
            }
            posicao = tabuleiro.aplica(posicao, jogada);
        }
        return posicao;
    }

    /** A partida termina com uma peca so na casa pedida? */
    public static boolean terminaEm(Tabuleiro tabuleiro, long comeco, List<int[]> jogadas, int casa) {
        return confere(tabuleiro, comeco, jogadas) == (1L << casa);
    }

    private static boolean temEssaJogada(Tabuleiro tabuleiro, int[] jogada) {
        for (int[] conhecida : tabuleiro.jogadas()) {
            if (conhecida[0] == jogada[0] && conhecida[1] == jogada[1] && conhecida[2] == jogada[2]) {
                return true;
            }
        }
        return false;
    }

    /** A jogada escrita como "de casa por cima de casa para casa". */
    public static String emTexto(Tabuleiro tabuleiro, int[] jogada) {
        return tabuleiro.nomeDaCasa(jogada[0]) + "-" + tabuleiro.nomeDaCasa(jogada[1])
                + "-" + tabuleiro.nomeDaCasa(jogada[2]);
    }

    /** A partida inteira numa linha. */
    public static String emTexto(Tabuleiro tabuleiro, List<int[]> jogadas) {
        StringBuilder texto = new StringBuilder();
        for (int[] jogada : jogadas) {
            if (texto.length() > 0) {
                texto.append(' ');
            }
            texto.append(emTexto(tabuleiro, jogada));
        }
        return texto.toString();
    }
}
