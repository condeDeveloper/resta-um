package br.com.conde.restaum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TabuleiroTest {

    @Test
    @DisplayName("o triangulo de cinco linhas tem as 15 casas e as 36 jogadas do jogo de mesa")
    void triangulo5() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        assertThat(t.casas()).isEqualTo(15);
        assertThat(t.jogadas()).hasNumberOfRows(36);
        assertThat(t.simetrias()).hasNumberOfRows(6);
        assertThat(t.nomeDaCasa(0)).isEqualTo("1");
        assertThat(t.nomeDaCasa(14)).isEqualTo("15");
    }

    @Test
    @DisplayName("cada triangulo tem 3 jogadas por trinca, nos dois sentidos")
    void contagemDeJogadas() {
        // Uma trinca em linha por direcao: a linha, a descida da esquerda e a da
        // direita. Lado n tem (n-2)(n-1)/2 trincas por direcao, e cada trinca vale
        // nos dois sentidos.
        for (int lado = 3; lado <= 8; lado++) {
            int trincas = (lado - 2) * (lado - 1) / 2;
            assertThat(Tabuleiro.triangulo(lado).jogadas())
                    .as("lado " + lado)
                    .hasNumberOfRows(2 * 3 * trincas);
        }
    }

    @Test
    @DisplayName("a cruz inglesa tem 33 casas, 76 jogadas e as 8 simetrias do quadrado")
    void cruzInglesa() {
        Tabuleiro t = Tabuleiros.CRUZ_INGLESA;
        assertThat(t.casas()).isEqualTo(33);
        assertThat(t.jogadas()).hasNumberOfRows(76);
        assertThat(t.simetrias()).hasNumberOfRows(8);
        assertThat(t.casaChamada("d4")).isEqualTo(16);
        assertThat(t.casaChamada("a1")).isEqualTo(-1);
    }

    @Test
    @DisplayName("as outras cruzes tem as casas que o tabuleiro de verdade tem")
    void outrasCruzes() {
        assertThat(Tabuleiro.cruzEuropeia().casas()).isEqualTo(37);
        assertThat(Tabuleiro.cruzEuropeia().jogadas()).hasNumberOfRows(92);
        assertThat(Tabuleiro.cruzAlema().casas()).isEqualTo(45);
        assertThat(Tabuleiro.cruzAlema().jogadas()).hasNumberOfRows(108);
        assertThat(Tabuleiros.QUADRADO_4.casas()).isEqualTo(16);
        assertThat(Tabuleiros.QUADRADO_4.jogadas()).hasNumberOfRows(32);
    }

    @Test
    @DisplayName("toda jogada tira duas pecas e devolve uma")
    void aJogadaTiraUma() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        for (int[] jogada : t.jogadas()) {
            long antes = t.cheioMenos(jogada[2]);
            assertThat(t.cabe(antes, jogada)).isTrue();
            long depois = t.aplica(antes, jogada);
            assertThat(Long.bitCount(depois)).isEqualTo(Long.bitCount(antes) - 1);
            assertThat(t.cabe(depois, jogada)).isFalse();
        }
    }

    @Test
    @DisplayName("a jogada so cabe com peca, peca e casa vazia, nessa ordem")
    void quandoNaoCabe() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        int[] jogada = t.jogadas()[0];
        long cheio = t.cheio();
        assertThat(t.cabe(cheio, jogada)).as("sem casa vazia").isFalse();
        long semOPulador = t.cheioMenos(jogada[2]) & ~(1L << jogada[0]);
        assertThat(t.cabe(semOPulador, jogada)).as("sem quem pula").isFalse();
        long semOPulado = t.cheioMenos(jogada[2]) & ~(1L << jogada[1]);
        assertThat(t.cabe(semOPulado, jogada)).as("sem quem e pulado").isFalse();
    }

    @Test
    @DisplayName("cada simetria leva jogada em jogada, nos dois tipos de tabuleiro")
    void asSimetriasRespeitamAsJogadas() {
        for (Tabuleiro t : new Tabuleiro[] {Tabuleiros.TRIANGULO_5, Tabuleiros.TRIANGULO_6,
                Tabuleiros.CRUZ_INGLESA, Tabuleiros.QUADRADO_4}) {
            Set<String> jogadas = new HashSet<>();
            for (int[] jogada : t.jogadas()) {
                jogadas.add(jogada[0] + "," + jogada[1] + "," + jogada[2]);
            }
            for (int[] simetria : t.simetrias()) {
                for (int[] jogada : t.jogadas()) {
                    String espelhada = simetria[jogada[0]] + "," + simetria[jogada[1]]
                            + "," + simetria[jogada[2]];
                    assertThat(jogadas).as(t.nome() + ": " + espelhada).contains(espelhada);
                }
            }
        }
    }

    @Test
    @DisplayName("espelhar duas vezes pela mesma simetria pode nao voltar, mas espelhar sempre conserva as pecas")
    void espelhoConservaPecas() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        long posicao = t.cheioMenos(0);
        for (int[] simetria : t.simetrias()) {
            long espelhada = t.peloEspelho(posicao, simetria);
            assertThat(Long.bitCount(espelhada)).isEqualTo(Long.bitCount(posicao));
        }
        assertThat(t.peloEspelho(posicao, t.simetrias()[0])).isEqualTo(posicao);
    }

    @Test
    @DisplayName("o desenho mostra onde tem peca e onde nao tem")
    void desenho() {
        Tabuleiro t = Tabuleiros.TRIANGULO_3;
        assertThat(t.desenho(t.cheioMenos(0)))
                .isEqualTo(String.join("\n", "  .", " O O", "O O O") + "\n");
        assertThat(t.desenho(1L)).startsWith("  O");
    }

    @Test
    @DisplayName("as pecas de uma posicao saem em ordem")
    void pecas() {
        Tabuleiro t = Tabuleiros.TRIANGULO_3;
        assertThat(t.pecas(0b001011)).containsExactly(0, 1, 3);
        assertThat(t.pecas(0)).isEmpty();
        assertThat(t.pecas(t.cheio())).hasSize(6);
    }

    @Test
    @DisplayName("triangulo fora de 3 a 8 e recusado na hora")
    void ladoInvalido() {
        assertThatThrownBy(() -> Tabuleiro.triangulo(2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lado fora");
        assertThatThrownBy(() -> Tabuleiro.triangulo(9))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
