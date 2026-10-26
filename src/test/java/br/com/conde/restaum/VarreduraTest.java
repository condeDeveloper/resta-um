package br.com.conde.restaum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VarreduraTest {

    @Test
    @DisplayName("o triangulo de 15 casas com o canto vazio tem 3016 posicoes e 29.760 partidas vencedoras")
    void oCantoDoTrianguloDe15() {
        Varredura v = Varreduras.triangulo5(0);
        assertThat(v.posicoes()).isEqualTo(3016);
        assertThat(v.minimoDePecas()).isEqualTo(1);
        assertThat(v.partidasQueResolvem()).isEqualTo(29_760);
        assertThat(nomes(v)).containsExactly("1", "7", "10", "13");
    }

    @Test
    @DisplayName("comecar pelo meio do triangulo deixa um final so, e o mais raro")
    void oMeioDoTriangulo() {
        for (int[] caso : new int[][] {{4, 12}, {7, 5}, {8, 3}}) {
            Varredura v = Varreduras.triangulo5(caso[0]);
            assertThat(v.posicoes()).isEqualTo(1651);
            assertThat(v.partidasQueResolvem()).isEqualTo(1_550);
            assertThat(v.finais()).as("vazia " + (caso[0] + 1)).containsExactly(caso[1]);
        }
    }

    @Test
    @DisplayName("comecar pelo meio de um lado e o que mais rende: 85.258 partidas")
    void oMeioDoLado() {
        for (int casa : new int[] {3, 5, 12}) {
            Varredura v = Varreduras.triangulo5(casa);
            assertThat(v.posicoes()).isEqualTo(4237);
            assertThat(v.partidasQueResolvem()).isEqualTo(85_258);
            assertThat(v.finais()).as("vazia " + (casa + 1)).hasSize(5);
        }
    }

    @Test
    @DisplayName("toda casa vazia do triangulo de 15 resolve")
    void todasResolvemNoTrianguloDe15() {
        for (int casa = 0; casa < 15; casa++) {
            assertThat(Varreduras.triangulo5(casa).restaUma()).as("vazia " + (casa + 1)).isTrue();
        }
    }

    @Test
    @DisplayName("o triangulo de 6 casas nunca resta uma, e tres comecos nao tem jogada nenhuma")
    void oTrianguloDe6NuncaResolve() {
        Tabuleiro t = Tabuleiros.TRIANGULO_3;
        int parados = 0;
        for (int casa = 0; casa < t.casas(); casa++) {
            Varredura v = Varreduras.de(t, casa);
            assertThat(v.restaUma()).as("vazia " + (casa + 1)).isFalse();
            if (v.posicoes() == 1) {
                parados++;
            }
        }
        assertThat(parados).isEqualTo(3);
        assertThat(Varreduras.de(t, 1).minimoDePecas()).isEqualTo(5);
        assertThat(Varreduras.de(t, 0).minimoDePecas()).isEqualTo(2);
    }

    @Test
    @DisplayName("no triangulo de 10 casas so os seis comecos do meio das linhas resolvem")
    void oTrianguloDe10() {
        Tabuleiro t = Tabuleiros.TRIANGULO_4;
        int resolvem = 0;
        for (int casa = 0; casa < t.casas(); casa++) {
            Varredura v = Varreduras.de(t, casa);
            if (v.restaUma()) {
                resolvem++;
                assertThat(v.finais()).as("vazia " + (casa + 1)).hasSize(1);
                assertThat(v.partidasQueResolvem()).isEqualTo(14);
            }
        }
        assertThat(resolvem).isEqualTo(6);
        assertThat(Varreduras.de(t, 0).minimoDePecas()).as("canto").isEqualTo(3);
        assertThat(Varreduras.de(t, 4).posicoes()).as("o centro trava de cara").isEqualTo(1);
        assertThat(Varreduras.de(t, 4).minimoDePecas()).isEqualTo(9);
    }

    @Test
    @DisplayName("o quadrado de 9 casas nunca resolve e o de 16 resolve em metade dos comecos")
    void osQuadrados() {
        Tabuleiro nove = Tabuleiros.QUADRADO_3;
        for (int casa = 0; casa < nove.casas(); casa++) {
            assertThat(Varreduras.de(nove, casa).restaUma()).isFalse();
        }
        assertThat(Varreduras.de(nove, 4).posicoes()).as("centro vazio, nenhuma jogada").isEqualTo(1);

        Tabuleiro dezesseis = Tabuleiros.QUADRADO_4;
        int resolvem = 0;
        for (int casa = 0; casa < dezesseis.casas(); casa++) {
            Varredura v = Varreduras.de(dezesseis, casa);
            if (v.restaUma()) {
                resolvem++;
                assertThat(v.partidasQueResolvem()).isEqualTo(210_422);
                assertThat(v.finais()).hasSize(2);
            }
        }
        assertThat(resolvem).isEqualTo(8);
        assertThat(Varreduras.de(dezesseis, 0).minimoDePecas()).as("canto").isEqualTo(2);
    }

    @Test
    @DisplayName("o triangulo de 21 casas resolve de qualquer canto, com 43 bilhoes de partidas")
    void oTrianguloDe21() {
        Varredura v = Varreduras.de(Tabuleiros.TRIANGULO_6, 0);
        assertThat(v.posicoes()).isEqualTo(291_987);
        assertThat(v.restaUma()).isTrue();
        assertThat(v.partidasQueResolvem()).isEqualTo(43_419_942_138L);
        assertThat(v.finais()).hasSize(7);
    }

    @Test
    @DisplayName("toda partida tem o mesmo tamanho: duas casas a menos que o tabuleiro")
    void oTamanhoEFixo() {
        Varredura v = Varreduras.triangulo5(0);
        for (int casa : v.finais()) {
            List<int[]> partida = v.caminhoAte(1L << casa);
            assertThat(partida).as("ate " + (casa + 1)).hasSize(13);
            assertThat(Juiz.confere(Tabuleiros.TRIANGULO_5, v.comeco(), partida))
                    .isEqualTo(1L << casa);
        }
    }

    @Test
    @DisplayName("as posicoes alcancaveis se repartem por quantidade de pecas")
    void posicoesPorPecas() {
        Varredura v = Varreduras.triangulo5(0);
        int[] porPecas = v.posicoesPorPecas();
        assertThat(porPecas[14]).as("a do comeco").isEqualTo(1);
        assertThat(porPecas[1]).as("as de uma peca").isEqualTo(4);
        assertThat(porPecas[0]).as("nenhuma partida esvazia o tabuleiro").isZero();
        int total = 0;
        for (int quantas : porPecas) {
            total += quantas;
        }
        assertThat(total).isEqualTo(v.posicoes());
    }

    @Test
    @DisplayName("da para travar com oito pecas ainda no triangulo de 15")
    void becos() {
        Varredura v = Varreduras.triangulo5(0);
        assertThat(v.becos()).isEqualTo(118);
        assertThat(v.piorBeco()).isEqualTo(8);
        assertThat(Varreduras.de(Tabuleiros.TRIANGULO_6, 0).piorBeco()).isEqualTo(12);
        assertThat(Varreduras.de(Tabuleiros.QUADRADO_4, 0).piorBeco()).isEqualTo(12);
    }

    @Test
    @DisplayName("posicao fora do alcance nao tem caminho nem conta como alcancada")
    void foraDoAlcance() {
        Varredura v = Varreduras.triangulo5(0);
        long impossivel = 1L << 1;
        assertThat(v.alcanca(impossivel)).isFalse();
        assertThat(v.caminhosAte(impossivel)).isZero();
        assertThatThrownBy(() -> v.caminhoAte(impossivel))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(v.alcanca(0)).as("tabuleiro vazio").isFalse();
    }

    @Test
    @DisplayName("a varredura completa se recusa a montar tabela para a cruz inglesa")
    void limiteDeCasas() {
        assertThatThrownBy(() -> new Varredura(Tabuleiros.CRUZ_INGLESA, 16))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("22 casas");
    }

    private static String[] nomes(Varredura v) {
        int[] finais = v.finais();
        String[] fora = new String[finais.length];
        for (int i = 0; i < finais.length; i++) {
            fora[i] = v.tabuleiro().nomeDaCasa(finais[i]);
        }
        return fora;
    }
}
