package br.com.conde.restaum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RotuloTest {

    @Test
    @DisplayName("em toda jogada de todo tabuleiro os tres rotulos somam zero")
    void oRotuloNaoMuda() {
        for (Tabuleiro t : new Tabuleiro[] {Tabuleiros.TRIANGULO_3, Tabuleiros.TRIANGULO_4,
                Tabuleiros.TRIANGULO_5, Tabuleiros.TRIANGULO_6, Tabuleiros.QUADRADO_3,
                Tabuleiros.QUADRADO_4, Tabuleiros.CRUZ_INGLESA, Tabuleiro.cruzEuropeia(),
                Tabuleiro.cruzAlema(), Tabuleiro.triangulo(8)}) {
            Rotulo r = new Rotulo(t);
            for (int qual = 0; qual < r.quantosRotulos(); qual++) {
                for (int[] jogada : t.jogadas()) {
                    int soma = r.rotuloDaCasa(qual, jogada[0]) ^ r.rotuloDaCasa(qual, jogada[1])
                            ^ r.rotuloDaCasa(qual, jogada[2]);
                    assertThat(soma).as(t.nome() + " " + Juiz.emTexto(t, jogada)).isZero();
                }
            }
        }
    }

    @Test
    @DisplayName("todo rotulo e 1, 2 ou 3: o zero do corpo nunca entra")
    void osTresValores() {
        Rotulo r = new Rotulo(Tabuleiros.TRIANGULO_5);
        for (int casa = 0; casa < Tabuleiros.TRIANGULO_5.casas(); casa++) {
            assertThat(r.rotuloDaCasa(0, casa)).isBetween(1, 3);
            assertThat(r.rotuloDaCasa(1, casa)).isBetween(1, 3);
        }
    }

    @Test
    @DisplayName("a soma dos rotulos e a mesma em todas as 3016 posicoes alcancaveis")
    void asomaNaoMudaEmPosicaoNenhuma() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        Rotulo r = new Rotulo(t);
        Varredura v = Varreduras.triangulo5(0);
        int[] doComeco = r.somas(v.comeco());
        int conferidas = 0;
        for (long posicao = 0; posicao < (1L << t.casas()); posicao++) {
            if (!v.alcanca(posicao)) {
                continue;
            }
            assertThat(r.somas(posicao)).isEqualTo(doComeco);
            conferidas++;
        }
        assertThat(conferidas).isEqualTo(3016);
    }

    @Test
    @DisplayName("par de expoente que nao serve e recusado na hora de montar")
    void parQueNaoServe() {
        // Expoente multiplo de 3 numa das direcoes: duas casas da trinca ficam com o
        // mesmo rotulo e a soma deixa de fechar.
        assertThatThrownBy(() -> new Rotulo(Tabuleiros.QUADRADO_4, new int[][] {{0, 1}}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao serve");
        assertThatThrownBy(() -> new Rotulo(Tabuleiros.QUADRADO_4, new int[][] {{3, 1}}))
                .isInstanceOf(IllegalArgumentException.class);
        // No triangulo a jogada da linha anda (+1,-1): a - b tambem precisa valer.
        assertThatThrownBy(() -> new Rotulo(Tabuleiros.TRIANGULO_5, new int[][] {{1, 1}}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new Rotulo(Tabuleiros.TRIANGULO_5, new int[][] {{1, 2}}).quantosRotulos())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("na cruz inglesa com o centro vazio o rotulo deixa cinco casas, sem jogar nada")
    void asCincoCasasDaCruz() {
        Tabuleiro t = Tabuleiros.CRUZ_INGLESA;
        Rotulo r = new Rotulo(t);
        List<String> casas = new ArrayList<>();
        for (int casa : r.casasPermitidas(t.cheioMenos(t.casaChamada("d4")))) {
            casas.add(t.nomeDaCasa(casa));
        }
        assertThat(casas).containsExactlyInAnyOrder("d4", "d1", "d7", "a4", "g4");
    }

    @Test
    @DisplayName("no triangulo de 15 o rotulo deixa cinco casas e a varredura usa quatro")
    void oRotuloSobraNoTrianguloDe15() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        Rotulo r = new Rotulo(t);
        assertThat(r.casasPermitidas(t.cheioMenos(0))).containsExactly(0, 4, 6, 9, 12);
        assertThat(Varreduras.triangulo5(0).finais()).containsExactly(0, 6, 9, 12);
        assertThat(r.deixaTerminarEm(t.cheioMenos(0), 4)).as("a casa 5 passa pelo rotulo").isTrue();
        assertThat(Varreduras.triangulo5(0).alcanca(1L << 4)).as("e mesmo assim nao acontece")
                .isFalse();
    }

    @Test
    @DisplayName("no triangulo de 21 e no quadrado de 16 o rotulo acerta casa por casa")
    void quandoORotuloAcertaTudo() {
        for (Tabuleiro t : new Tabuleiro[] {Tabuleiros.TRIANGULO_6, Tabuleiros.QUADRADO_4}) {
            Rotulo r = new Rotulo(t);
            for (int casa = 0; casa < t.casas(); casa++) {
                assertThat(r.casasPermitidas(t.cheioMenos(casa)))
                        .as(t.nome() + ", vazia " + t.nomeDaCasa(casa))
                        .containsExactly(Varreduras.de(t, casa).finais());
            }
        }
    }

    @Test
    @DisplayName("no triangulo de 6 e no quadrado de 9 o rotulo deixa casas que nunca acontecem")
    void quandoORotuloSoErra() {
        for (Tabuleiro t : new Tabuleiro[] {Tabuleiros.TRIANGULO_3, Tabuleiros.QUADRADO_3}) {
            Rotulo r = new Rotulo(t);
            int permitidas = 0;
            for (int casa = 0; casa < t.casas(); casa++) {
                permitidas += r.casasPermitidas(t.cheioMenos(casa)).length;
                assertThat(Varreduras.de(t, casa).finais()).isEmpty();
            }
            assertThat(permitidas).as(t.nome()).isPositive();
        }
    }

    @Test
    @DisplayName("no quadrado de 9 o rotulo so deixa a propria casa que comecou vazia")
    void oQuadradoDe9() {
        Tabuleiro t = Tabuleiros.QUADRADO_3;
        Rotulo r = new Rotulo(t);
        for (int casa = 0; casa < t.casas(); casa++) {
            assertThat(r.casasPermitidas(t.cheioMenos(casa))).containsExactly(casa);
        }
    }

    @Test
    @DisplayName("o rotulo tambem fala de posicoes com muitas pecas, nao so das de uma")
    void posicoesDoMeio() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        Rotulo r = new Rotulo(t);
        Varredura v = Varreduras.triangulo5(0);
        int negadas = 0;
        int alcancadas = 0;
        for (long posicao = 0; posicao < (1L << t.casas()); posicao++) {
            if (Long.bitCount(posicao) != 7) {
                continue;
            }
            boolean deixa = r.deixaChegarEm(v.comeco(), posicao);
            if (v.alcanca(posicao)) {
                alcancadas++;
                assertThat(deixa).as("alcancada e negada pelo rotulo").isTrue();
            } else if (!deixa) {
                negadas++;
            }
        }
        assertThat(alcancadas).isPositive();
        assertThat(negadas).as("o rotulo sozinho ja corta um monte de posicao").isPositive();
    }
}
