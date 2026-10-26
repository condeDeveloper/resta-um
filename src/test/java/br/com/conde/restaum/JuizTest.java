package br.com.conde.restaum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JuizTest {

    private final Tabuleiro t = Tabuleiros.TRIANGULO_5;

    @Test
    @DisplayName("o juiz aceita a partida que a varredura achou e diz onde ela parou")
    void aceitaPartidaBoa() {
        Varredura v = Varreduras.triangulo5(0);
        List<int[]> partida = v.caminhoAte(1L << 12);
        assertThat(Juiz.confere(t, v.comeco(), partida)).isEqualTo(1L << 12);
        assertThat(Juiz.terminaEm(t, v.comeco(), partida, 12)).isTrue();
        assertThat(Juiz.terminaEm(t, v.comeco(), partida, 0)).isFalse();
    }

    @Test
    @DisplayName("jogada que o tabuleiro nao tem e recusada, mesmo cabendo nos bits")
    void recusaJogadaInventada() {
        // 1 pula 3 e cai em 4: as tres casas existem e os bits ate batem, mas as
        // tres nao estao em linha nenhuma do tabuleiro.
        List<int[]> partida = List.of(new int[] {0, 2, 3});
        assertThatThrownBy(() -> Juiz.confere(t, t.cheioMenos(3), partida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao existe no tabuleiro");
    }

    @Test
    @DisplayName("jogada que nao cabe na posicao e recusada, com o numero do passo")
    void recusaJogadaQueNaoCabe() {
        List<int[]> partida = new ArrayList<>(Varreduras.triangulo5(0).caminhoAte(1L << 12));
        partida.add(partida.get(0));
        assertThatThrownBy(() -> Juiz.confere(t, t.cheioMenos(0), partida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jogada 14 nao cabe");
    }

    @Test
    @DisplayName("jogada sem tres casas nao passa")
    void recusaJogadaTorta() {
        List<int[]> partida = List.of(new int[] {0, 1});
        assertThatThrownBy(() -> Juiz.confere(t, t.cheio(), partida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao tem tres casas");
    }

    @Test
    @DisplayName("partida vazia devolve a posicao do comeco")
    void partidaVazia() {
        assertThat(Juiz.confere(t, t.cheioMenos(0), List.of())).isEqualTo(t.cheioMenos(0));
    }

    @Test
    @DisplayName("a partida sai escrita casa a casa")
    void emTexto() {
        int[] jogada = t.jogadas()[0];
        assertThat(Juiz.emTexto(t, jogada)).isEqualTo("1-2-4");
        assertThat(Juiz.emTexto(t, List.of(jogada, jogada))).isEqualTo("1-2-4 1-2-4");
        assertThat(Juiz.emTexto(t, List.<int[]>of())).isEmpty();
    }
}
