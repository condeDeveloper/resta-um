package br.com.conde.restaum;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os numeros que estao escritos no README, cada um preso a uma medida. */
class MedidasTest {

    @Test
    @DisplayName("README: a tabela dos tabuleiros")
    void tabelaDosTabuleiros() {
        assertThat(Tabuleiros.TRIANGULO_3.casas()).isEqualTo(6);
        assertThat(Tabuleiros.TRIANGULO_3.jogadas()).hasNumberOfRows(6);
        assertThat(Tabuleiros.TRIANGULO_4.casas()).isEqualTo(10);
        assertThat(Tabuleiros.TRIANGULO_4.jogadas()).hasNumberOfRows(18);
        assertThat(Tabuleiros.TRIANGULO_5.casas()).isEqualTo(15);
        assertThat(Tabuleiros.TRIANGULO_5.jogadas()).hasNumberOfRows(36);
        assertThat(Tabuleiros.TRIANGULO_6.casas()).isEqualTo(21);
        assertThat(Tabuleiros.TRIANGULO_6.jogadas()).hasNumberOfRows(60);
        assertThat(Tabuleiros.QUADRADO_3.jogadas()).hasNumberOfRows(12);
        assertThat(Tabuleiros.QUADRADO_4.jogadas()).hasNumberOfRows(32);
        assertThat(Tabuleiros.CRUZ_INGLESA.casas()).isEqualTo(33);
        assertThat(Tabuleiros.CRUZ_INGLESA.jogadas()).hasNumberOfRows(76);
        assertThat(Tabuleiro.cruzEuropeia().casas()).isEqualTo(37);
        assertThat(Tabuleiro.cruzAlema().casas()).isEqualTo(45);
    }

    @Test
    @DisplayName("README: 3016 posicoes, 118 becos e 8 pecas travadas no triangulo de 15")
    void aVarreduraDoTrianguloDe15() {
        Varredura v = Varreduras.triangulo5(0);
        assertThat(v.posicoes()).isEqualTo(3016);
        assertThat(v.becos()).isEqualTo(118);
        assertThat(v.piorBeco()).isEqualTo(8);
        assertThat(v.partidasQueResolvem()).isEqualTo(29_760);
    }

    @Test
    @DisplayName("README: os tres tipos de comeco do triangulo de 15")
    void osTresTiposDeComeco() {
        assertThat(Varreduras.triangulo5(0).partidasQueResolvem()).as("canto").isEqualTo(29_760);
        assertThat(Varreduras.triangulo5(0).finais()).hasSize(4);
        assertThat(Varreduras.triangulo5(3).partidasQueResolvem()).as("meio de lado")
                .isEqualTo(85_258);
        assertThat(Varreduras.triangulo5(3).finais()).hasSize(5);
        assertThat(Varreduras.triangulo5(4).partidasQueResolvem()).as("meio do triangulo")
                .isEqualTo(1_550);
        assertThat(Varreduras.triangulo5(4).finais()).containsExactly(12);
        assertThat(Varreduras.triangulo5(1).partidasQueResolvem()).as("vizinho do canto")
                .isEqualTo(14_880);
    }

    @Test
    @DisplayName("README: 55 vezes mais partidas comecando pelo meio de um lado que pelo meio do triangulo")
    void aRazaoEntreOsComecos() {
        long meioDoLado = Varreduras.triangulo5(3).partidasQueResolvem();
        long meioDoTriangulo = Varreduras.triangulo5(4).partidasQueResolvem();
        assertThat((double) meioDoLado / meioDoTriangulo).isCloseTo(55.0, within(0.5));
    }

    @Test
    @DisplayName("README: 43.419.942.138 partidas vencedoras no triangulo de 21")
    void oTrianguloDe21() {
        Varredura v = Varreduras.de(Tabuleiros.TRIANGULO_6, 0);
        assertThat(v.posicoes()).isEqualTo(291_987);
        assertThat(v.partidasQueResolvem()).isEqualTo(43_419_942_138L);
        assertThat(v.becos()).isEqualTo(1429);
        assertThat(v.piorBeco()).isEqualTo(12);
    }

    @Test
    @DisplayName("README: a tabela do rotulo contra a varredura")
    void aTabelaDoRotulo() {
        int[][] esperado = {
            // permite, acontece, erra
            {12, 0, 12},
            {18, 6, 12},
            {75, 54, 21},
            {9, 0, 9},
            {16, 16, 0},
            {147, 147, 0},
        };
        Tabuleiro[] tabuleiros = {Tabuleiros.TRIANGULO_3, Tabuleiros.TRIANGULO_4,
            Tabuleiros.TRIANGULO_5, Tabuleiros.QUADRADO_3, Tabuleiros.QUADRADO_4,
            Tabuleiros.TRIANGULO_6};
        for (int i = 0; i < tabuleiros.length; i++) {
            Tabuleiro t = tabuleiros[i];
            Rotulo r = new Rotulo(t);
            int permite = 0;
            int acontece = 0;
            int erra = 0;
            for (int casa = 0; casa < t.casas(); casa++) {
                Set<Integer> verdade = new HashSet<>();
                for (int f : Varreduras.de(t, casa).finais()) {
                    verdade.add(f);
                }
                acontece += verdade.size();
                for (int alvo : r.casasPermitidas(t.cheioMenos(casa))) {
                    permite++;
                    if (!verdade.contains(alvo)) {
                        erra++;
                    }
                }
            }
            assertThat(new int[] {permite, acontece, erra}).as(t.nome()).isEqualTo(esperado[i]);
        }
    }

    @Test
    @DisplayName("README: o rotulo nunca deixa de fora um final que acontece")
    void oRotuloNuncaErraParaMenos() {
        for (Tabuleiro t : new Tabuleiro[] {Tabuleiros.TRIANGULO_3, Tabuleiros.TRIANGULO_4,
                Tabuleiros.TRIANGULO_5, Tabuleiros.QUADRADO_3, Tabuleiros.QUADRADO_4,
                Tabuleiros.TRIANGULO_6}) {
            Rotulo r = new Rotulo(t);
            for (int casa = 0; casa < t.casas(); casa++) {
                for (int alvo : Varreduras.de(t, casa).finais()) {
                    assertThat(r.deixaTerminarEm(t.cheioMenos(casa), alvo))
                            .as(t.nome() + ": " + t.nomeDaCasa(casa) + " para "
                                    + t.nomeDaCasa(alvo))
                            .isTrue();
                }
            }
        }
    }

    @Test
    @DisplayName("README: 1.858.849 posicoes para provar na marra o que o rotulo resolve em uma conta")
    void aTabelaDasProvas() {
        conferePodas(Tabuleiros.TRIANGULO_5, "1", "13", true, 771, 771);
        conferePodas(Tabuleiros.TRIANGULO_5, "1", "5", true, 10_307, 10_307);
        conferePodas(Tabuleiros.TRIANGULO_5, "1", "2", false, 10_307, 1);
        conferePodas(Tabuleiros.TRIANGULO_6, "1", "20", true, 2515, 2515);
        conferePodas(Tabuleiros.TRIANGULO_6, "1", "2", false, 1_858_849, 1);
        conferePodas(Tabuleiros.QUADRADO_4, "b1", "c1", true, 179, 179);
        conferePodas(Tabuleiros.QUADRADO_4, "b1", "b1", false, 6168, 1);
    }

    private static void conferePodas(Tabuleiro t, String vazia, String alvo, boolean deixa,
            long semRotulo, long comRotulo) {
        int casaVazia = t.casaChamada(vazia);
        int casaAlvo = t.casaChamada(alvo);
        long comeco = t.cheioMenos(casaVazia);
        String onde = t.nome() + " " + vazia + " para " + alvo;
        assertThat(new Rotulo(t).deixaTerminarEm(comeco, casaAlvo)).as(onde).isEqualTo(deixa);
        Busca sem = new Busca(t, true, false, Busca.Espelho.NENHUM);
        sem.resolve(comeco, casaAlvo);
        assertThat(sem.nos()).as(onde + ", sem rotulo").isEqualTo(semRotulo);
        Busca com = new Busca(t, true, true, Busca.Espelho.NENHUM);
        com.resolve(comeco, casaAlvo);
        assertThat(com.nos()).as(onde + ", com rotulo").isEqualTo(comRotulo);
    }

    @Test
    @DisplayName("README: a memoria espelhada sem olhar o alvo erra 12 dos 225 pares do triangulo de 15")
    void aTabelaDoEspelho() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        long nosCerto = 0;
        long nosTodos = 0;
        int errosTodos = 0;
        for (int vazia = 0; vazia < t.casas(); vazia++) {
            Set<Integer> verdade = new HashSet<>();
            for (int f : Varreduras.triangulo5(vazia).finais()) {
                verdade.add(f);
            }
            for (int alvo = 0; alvo < t.casas(); alvo++) {
                Busca certa = Busca.completa(t);
                assertThat(certa.resolve(t.cheioMenos(vazia), alvo) != null)
                        .isEqualTo(verdade.contains(alvo));
                nosCerto += certa.nos();
                Busca todos = new Busca(t, true, true, Busca.Espelho.TODOS);
                if ((todos.resolve(t.cheioMenos(vazia), alvo) != null)
                        != verdade.contains(alvo)) {
                    errosTodos++;
                }
                nosTodos += todos.nos();
            }
        }
        assertThat(errosTodos).isEqualTo(12);
        assertThat(nosCerto).isEqualTo(185_676);
        assertThat(nosTodos).as("errar nao economizou nada").isEqualTo(209_520);
    }

    @Test
    @DisplayName("README: na cruz inglesa o rotulo corta 28 das 33 casas sem abrir uma posicao")
    void aCruzInglesa() {
        Tabuleiro t = Tabuleiros.CRUZ_INGLESA;
        Rotulo r = new Rotulo(t);
        long comeco = t.cheioMenos(t.casaChamada("d4"));
        List<String> casas = new ArrayList<>();
        for (int casa : r.casasPermitidas(comeco)) {
            casas.add(t.nomeDaCasa(casa));
        }
        assertThat(casas).containsExactlyInAnyOrder("d4", "d1", "d7", "a4", "g4");
        assertThat(t.casas() - casas.size()).isEqualTo(28);
    }

    @Test
    @DisplayName("README: a mesma pergunta girada custa dez vezes mais na cruz inglesa")
    void aMesmaPerguntaGirada() {
        Tabuleiro t = Tabuleiros.CRUZ_INGLESA;
        long comeco = t.cheioMenos(t.casaChamada("d4"));
        long[] esperado = {832_033, 914_284, 3_809_956, 9_342_453};
        String[] alvos = {"d4", "d1", "a4", "d7"};
        for (int i = 0; i < alvos.length; i++) {
            Busca b = Busca.completa(t);
            List<int[]> partida = b.resolve(comeco, t.casaChamada(alvos[i]));
            assertThat(partida).as("alvo " + alvos[i]).isNotNull();
            assertThat(partida).hasSize(31);
            assertThat(Juiz.confere(t, comeco, partida))
                    .isEqualTo(1L << t.casaChamada(alvos[i]));
            assertThat(b.nos()).as("nos ate achar " + alvos[i]).isEqualTo(esperado[i]);
        }
        assertThat((double) esperado[3] / esperado[1]).isCloseTo(10.2, within(0.1));
    }

    @Test
    @DisplayName("README: os oito espelhos do centro cortam 11% dos nos da cruz inglesa")
    void oEspelhoDoCentro() {
        Tabuleiro t = Tabuleiros.CRUZ_INGLESA;
        int centro = t.casaChamada("d4");
        long comeco = t.cheioMenos(centro);
        Busca sem = new Busca(t, true, true, Busca.Espelho.NENHUM);
        sem.resolve(comeco, centro);
        assertThat(sem.nos()).isEqualTo(939_631);
        Busca com = Busca.completa(t);
        com.resolve(comeco, centro);
        assertThat(com.nos()).isEqualTo(832_033);
        assertThat(100.0 * (sem.nos() - com.nos()) / sem.nos()).isCloseTo(11.4, within(0.1));
    }

    @Test
    @DisplayName("README: toda partida tem duas jogadas a menos que o tabuleiro tem casas")
    void oTamanhoDaPartida() {
        assertThat(Tabuleiros.TRIANGULO_5.casas() - 2).isEqualTo(13);
        assertThat(Tabuleiros.CRUZ_INGLESA.casas() - 2).isEqualTo(31);
        Varredura v = Varreduras.triangulo5(0);
        for (int casa : v.finais()) {
            assertThat(v.caminhoAte(1L << casa)).hasSize(13);
        }
    }

    private static org.assertj.core.data.Offset<Double> within(double folga) {
        return org.assertj.core.data.Offset.offset(folga);
    }
}
