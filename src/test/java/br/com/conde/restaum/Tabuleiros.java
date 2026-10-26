package br.com.conde.restaum;

/** Os tabuleiros dos testes, montados uma vez so. */
final class Tabuleiros {

    static final Tabuleiro TRIANGULO_3 = Tabuleiro.triangulo(3);
    static final Tabuleiro TRIANGULO_4 = Tabuleiro.triangulo(4);
    static final Tabuleiro TRIANGULO_5 = Tabuleiro.triangulo(5);
    static final Tabuleiro TRIANGULO_6 = Tabuleiro.triangulo(6);
    static final Tabuleiro QUADRADO_3 = Tabuleiro.quadrado(3);
    static final Tabuleiro QUADRADO_4 = Tabuleiro.quadrado(4);
    static final Tabuleiro CRUZ_INGLESA = Tabuleiro.cruzInglesa();

    private Tabuleiros() {
    }
}
