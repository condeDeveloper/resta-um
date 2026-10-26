package br.com.conde.restaum;

import java.util.HashMap;
import java.util.Map;

/**
 * As varreduras usadas pelos testes, montadas uma vez so.
 *
 * <p>A varredura do triangulo de 21 casas leva uns segundos e aparece em teste
 * atras de teste. Sem este guarda-tudo a bateria roda a mesma busca umas vinte
 * vezes, e o CI roda a bateria tres vezes, uma por sistema.
 */
final class Varreduras {

    private static final Map<String, Varredura> GUARDADAS = new HashMap<>();

    private Varreduras() {
    }

    static synchronized Varredura de(Tabuleiro tabuleiro, int casaVazia) {
        return GUARDADAS.computeIfAbsent(tabuleiro.nome() + "/" + casaVazia,
                chave -> new Varredura(tabuleiro, casaVazia));
    }

    static Varredura triangulo5(int casaVazia) {
        return de(Tabuleiros.TRIANGULO_5, casaVazia);
    }
}
