package br.com.conde.restaum;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuscaTest {

    @Test
    @DisplayName("a busca acha a partida e o juiz confere jogada por jogada")
    void achaEConfere() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        long comeco = t.cheioMenos(0);
        for (int alvo : new int[] {0, 6, 9, 12}) {
            List<int[]> partida = Busca.completa(t).resolve(comeco, alvo);
            assertThat(partida).as("alvo " + t.nomeDaCasa(alvo)).isNotNull();
            assertThat(partida).hasSize(13);
            assertThat(Juiz.confere(t, comeco, partida)).isEqualTo(1L << alvo);
        }
    }

    @Test
    @DisplayName("a busca com alvo livre termina onde der")
    void alvoLivre() {
        Tabuleiro t = Tabuleiros.TRIANGULO_4;
        long comeco = t.cheioMenos(1);
        List<int[]> partida = Busca.completa(t).resolve(comeco, -1);
        assertThat(Long.bitCount(Juiz.confere(t, comeco, partida))).isEqualTo(1);
    }

    @Test
    @DisplayName("a busca concorda com a varredura nos 225 pares do triangulo de 15")
    void aBuscaConcordaComAVarredura() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        int pares = 0;
        int dao = 0;
        for (int vazia = 0; vazia < t.casas(); vazia++) {
            Set<Integer> verdade = new HashSet<>();
            for (int f : Varreduras.triangulo5(vazia).finais()) {
                verdade.add(f);
            }
            for (int alvo = 0; alvo < t.casas(); alvo++) {
                pares++;
                boolean achou = Busca.completa(t).resolve(t.cheioMenos(vazia), alvo) != null;
                assertThat(achou).as("vazia " + t.nomeDaCasa(vazia) + ", alvo " + t.nomeDaCasa(alvo))
                        .isEqualTo(verdade.contains(alvo));
                if (achou) {
                    dao++;
                }
            }
        }
        assertThat(pares).isEqualTo(225);
        assertThat(dao).isEqualTo(54);
    }

    @Test
    @DisplayName("espelhar a memoria sem olhar o alvo responde errado em 12 dos 225 pares")
    void oEspelhoErrado() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        int errados = 0;
        for (int vazia = 0; vazia < t.casas(); vazia++) {
            Set<Integer> verdade = new HashSet<>();
            for (int f : Varreduras.triangulo5(vazia).finais()) {
                verdade.add(f);
            }
            for (int alvo = 0; alvo < t.casas(); alvo++) {
                Busca torta = new Busca(t, true, true, Busca.Espelho.TODOS);
                boolean achou = torta.resolve(t.cheioMenos(vazia), alvo) != null;
                if (achou != verdade.contains(alvo)) {
                    errados++;
                    assertThat(achou).as("o erro e sempre dar por perdido o que da").isFalse();
                }
            }
        }
        assertThat(errados).isEqualTo(12);
    }

    @Test
    @DisplayName("no quadrado de 16 o mesmo espelho errado nao erra nada: o defeito se esconde")
    void oEspelhoErradoSeEsconde() {
        Tabuleiro t = Tabuleiros.QUADRADO_4;
        for (int vazia = 0; vazia < t.casas(); vazia++) {
            Set<Integer> verdade = new HashSet<>();
            for (int f : Varreduras.de(t, vazia).finais()) {
                verdade.add(f);
            }
            for (int alvo = 0; alvo < t.casas(); alvo++) {
                Busca torta = new Busca(t, true, true, Busca.Espelho.TODOS);
                boolean achou = torta.resolve(t.cheioMenos(vazia), alvo) != null;
                assertThat(achou).isEqualTo(verdade.contains(alvo));
            }
        }
    }

    @Test
    @DisplayName("so entram os espelhos que deixam o alvo no lugar")
    void quantosEspelhos() {
        Tabuleiro t = Tabuleiros.CRUZ_INGLESA;
        Busca b = Busca.completa(t);
        b.resolve(t.cheioMenos(t.casaChamada("d4")), t.casaChamada("d4"));
        assertThat(b.espelhosEmUso()).as("o centro fica parado nos oito espelhos").isEqualTo(8);
        b.resolve(t.cheioMenos(t.casaChamada("d4")), t.casaChamada("d1"));
        assertThat(b.espelhosEmUso()).as("o topo so fica parado em dois").isEqualTo(2);
    }

    @Test
    @DisplayName("o rotulo nao poda nada no meio da busca: ou corta no primeiro no, ou nunca")
    void oRotuloNaoPoda() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        long comeco = t.cheioMenos(0);
        for (int alvo : new int[] {12, 4}) {
            Busca sem = new Busca(t, true, false, Busca.Espelho.NENHUM);
            sem.resolve(comeco, alvo);
            Busca com = new Busca(t, true, true, Busca.Espelho.NENHUM);
            com.resolve(comeco, alvo);
            assertThat(com.nos()).as("alvo " + t.nomeDaCasa(alvo)).isEqualTo(sem.nos());
            assertThat(com.cortesDoRotulo()).isZero();
        }
        Busca cortada = new Busca(t, true, true, Busca.Espelho.NENHUM);
        assertThat(cortada.resolve(comeco, 1)).isNull();
        assertThat(cortada.nos()).as("alvo proibido: um no so").isEqualTo(1);
        assertThat(cortada.cortesDoRotulo()).isEqualTo(1);
    }

    @Test
    @DisplayName("a memoria e que faz a busca caber: ela reconhece posicao perdida aos milhares")
    void aMemoriaTrabalha() {
        Tabuleiro t = Tabuleiros.TRIANGULO_5;
        Busca b = new Busca(t, true, false, Busca.Espelho.NENHUM);
        b.resolve(t.cheioMenos(0), 4);
        assertThat(b.nos()).isEqualTo(10_307);
        assertThat(b.cortesDaMemoria()).isPositive();
        assertThat(b.tamanhoDaMemoria()).isPositive();
    }
}
