package br.com.conde.restaum;

import java.util.ArrayList;
import java.util.List;

/**
 * Um tabuleiro de resta um: as casas, as jogadas, a malha que o rotulo usa e as
 * simetrias do desenho.
 *
 * <p>Uma posicao e um {@code int} com um bit por casa: bit ligado, casa com peca.
 * Como nenhum tabuleiro aqui passa de 37 casas, a posicao vira {@code long} quando
 * precisa de mais de 31 bits; a varredura completa so aceita ate 24 casas.
 */
public final class Tabuleiro {

    private final String nome;
    private final String[] nomes;
    private final int[][] jogadas;
    private final int[] u;
    private final int[] v;
    private final int[][] paresDeRotulo;
    private final int[][] simetrias;
    private final int[][] grade;

    private Tabuleiro(String nome, String[] nomes, int[][] jogadas, int[] u, int[] v,
                      int[][] paresDeRotulo, int[][] simetrias, int[][] grade) {
        this.nome = nome;
        this.nomes = nomes;
        this.jogadas = jogadas;
        this.u = u;
        this.v = v;
        this.paresDeRotulo = paresDeRotulo;
        this.simetrias = simetrias;
        this.grade = grade;
    }

    /**
     * O triangulo do restaurante: {@code lado} linhas, a primeira com uma casa.
     * Lado 5 da as 15 casas do jogo de mesa mais conhecido.
     */
    public static Tabuleiro triangulo(int lado) {
        if (lado < 3 || lado > 8) {
            throw new IllegalArgumentException("lado fora de 3..8: " + lado);
        }
        int casas = lado * (lado + 1) / 2;
        int[][] indice = new int[lado][];
        String[] nomes = new String[casas];
        int[] u = new int[casas];
        int[] v = new int[casas];
        int n = 0;
        for (int i = 0; i < lado; i++) {
            indice[i] = new int[i + 1];
            for (int j = 0; j <= i; j++) {
                indice[i][j] = n;
                nomes[n] = String.valueOf(n + 1);
                u[n] = j;
                v[n] = i - j;
                n++;
            }
        }

        List<int[]> jogadas = new ArrayList<>();
        int[][] direcoes = {{0, 1}, {1, 0}, {1, 1}};
        for (int i = 0; i < lado; i++) {
            for (int j = 0; j <= i; j++) {
                for (int[] d : direcoes) {
                    int i3 = i + 2 * d[0];
                    int j3 = j + 2 * d[1];
                    if (i3 >= lado || j3 > i3) {
                        continue;
                    }
                    int a = indice[i][j];
                    int b = indice[i + d[0]][j + d[1]];
                    int c = indice[i3][j3];
                    jogadas.add(new int[] {a, b, c});
                    jogadas.add(new int[] {c, b, a});
                }
            }
        }

        // As seis simetrias do triangulo sao as seis trocas de ordem de (u, v, w),
        // com w = lado - 1 - i. A soma u + v + w e a mesma em toda casa.
        int[][] trocas = {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}};
        int[][] simetrias = new int[trocas.length][casas];
        for (int s = 0; s < trocas.length; s++) {
            for (int casa = 0; casa < casas; casa++) {
                int[] coord = {u[casa], v[casa], lado - 1 - u[casa] - v[casa]};
                int novoU = coord[trocas[s][0]];
                int novoV = coord[trocas[s][1]];
                simetrias[s][casa] = indice[novoU + novoV][novoU];
            }
        }

        int largura = 2 * lado - 1;
        int[][] grade = new int[lado][largura];
        for (int[] linha : grade) {
            java.util.Arrays.fill(linha, -1);
        }
        for (int i = 0; i < lado; i++) {
            for (int j = 0; j <= i; j++) {
                grade[i][lado - 1 - i + 2 * j] = indice[i][j];
            }
        }

        // Na malha do triangulo as tres direcoes andam (+1,-1), (0,+1) e (+1,0) em
        // (u, v). O expoente a*u + b*v precisa mudar em toda jogada, entao a, b e
        // a - b nao podem ser multiplos de 3: (1,2) e (2,1) servem.
        return new Tabuleiro("triangulo " + lado, nomes, jogadas.toArray(new int[0][]), u, v,
                new int[][] {{1, 2}, {2, 1}}, simetrias, grade);
    }

    /** A cruz de 33 casas, o tabuleiro ingles. */
    public static Tabuleiro cruzInglesa() {
        return deMapa("cruz inglesa", new String[] {
            "  ooo  ",
            "  ooo  ",
            "ooooooo",
            "ooooooo",
            "ooooooo",
            "  ooo  ",
            "  ooo  ",
        });
    }

    /** A cruz de 37 casas, o tabuleiro europeu. */
    public static Tabuleiro cruzEuropeia() {
        return deMapa("cruz europeia", new String[] {
            "  ooo  ",
            " ooooo ",
            "ooooooo",
            "ooooooo",
            "ooooooo",
            " ooooo ",
            "  ooo  ",
        });
    }

    /** O tabuleiro alemao de 45 casas. */
    public static Tabuleiro cruzAlema() {
        return deMapa("cruz alema", new String[] {
            "   ooo   ",
            "   ooo   ",
            "   ooo   ",
            "ooooooooo",
            "ooooooooo",
            "ooooooooo",
            "   ooo   ",
            "   ooo   ",
            "   ooo   ",
        });
    }

    /** Um quadrado cheio de lado {@code n}, sem buraco nenhum no desenho. */
    public static Tabuleiro quadrado(int n) {
        String[] mapa = new String[n];
        for (int i = 0; i < n; i++) {
            mapa[i] = "o".repeat(n);
        }
        return deMapa("quadrado " + n, mapa);
    }

    /**
     * Monta um tabuleiro de grade a partir de um desenho, com {@code 'o'} onde ha casa.
     * Nas grades so valem as jogadas de linha e de coluna.
     */
    public static Tabuleiro deMapa(String nome, String[] mapa) {
        int linhas = mapa.length;
        int colunas = 0;
        for (String linha : mapa) {
            colunas = Math.max(colunas, linha.length());
        }
        int[][] indice = new int[linhas][colunas];
        for (int[] linha : indice) {
            java.util.Arrays.fill(linha, -1);
        }
        List<String> nomes = new ArrayList<>();
        List<Integer> us = new ArrayList<>();
        List<Integer> vs = new ArrayList<>();
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < mapa[i].length(); j++) {
                if (mapa[i].charAt(j) != 'o') {
                    continue;
                }
                indice[i][j] = nomes.size();
                nomes.add("" + (char) ('a' + j) + (i + 1));
                us.add(j);
                vs.add(i);
            }
        }
        int casas = nomes.size();

        List<int[]> jogadas = new ArrayList<>();
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (indice[i][j] < 0) {
                    continue;
                }
                for (int[] d : new int[][] {{0, 1}, {1, 0}}) {
                    int i2 = i + d[0];
                    int j2 = j + d[1];
                    int i3 = i + 2 * d[0];
                    int j3 = j + 2 * d[1];
                    if (i3 >= linhas || j3 >= colunas || indice[i2][j2] < 0 || indice[i3][j3] < 0) {
                        continue;
                    }
                    jogadas.add(new int[] {indice[i][j], indice[i2][j2], indice[i3][j3]});
                    jogadas.add(new int[] {indice[i3][j3], indice[i2][j2], indice[i][j]});
                }
            }
        }

        int[] u = new int[casas];
        int[] v = new int[casas];
        for (int c = 0; c < casas; c++) {
            u[c] = us.get(c);
            v[c] = vs.get(c);
        }

        List<int[]> simetrias = new ArrayList<>();
        if (linhas == colunas) {
            for (int s = 0; s < 8; s++) {
                int[] perm = new int[casas];
                boolean vale = true;
                for (int c = 0; c < casas && vale; c++) {
                    int i = v[c];
                    int j = u[c];
                    int ni = (s & 1) == 0 ? i : linhas - 1 - i;
                    int nj = (s & 2) == 0 ? j : colunas - 1 - j;
                    if ((s & 4) != 0) {
                        int troca = ni;
                        ni = nj;
                        nj = troca;
                    }
                    if (indice[ni][nj] < 0) {
                        vale = false;
                    } else {
                        perm[c] = indice[ni][nj];
                    }
                }
                if (vale) {
                    simetrias.add(perm);
                }
            }
        }
        if (simetrias.isEmpty()) {
            int[] identidade = new int[casas];
            for (int c = 0; c < casas; c++) {
                identidade[c] = c;
            }
            simetrias.add(identidade);
        }

        // Na grade as duas direcoes andam (+1,0) e (0,+1) em (u, v): basta a e b
        // fora dos multiplos de 3.
        return new Tabuleiro(nome, nomes.toArray(new String[0]), jogadas.toArray(new int[0][]),
                u, v, new int[][] {{1, 1}, {1, 2}}, simetrias.toArray(new int[0][]), indice);
    }

    public String nome() {
        return nome;
    }

    public int casas() {
        return nomes.length;
    }

    public String nomeDaCasa(int casa) {
        return nomes[casa];
    }

    /** O indice da casa pelo nome, ou -1. */
    public int casaChamada(String nome) {
        for (int c = 0; c < nomes.length; c++) {
            if (nomes[c].equals(nome)) {
                return c;
            }
        }
        return -1;
    }

    /** Cada jogada como {@code {de, por cima de, para}}; ida e volta contam separado. */
    public int[][] jogadas() {
        return jogadas;
    }

    public int[] malhaU() {
        return u;
    }

    public int[] malhaV() {
        return v;
    }

    public int[][] paresDeRotulo() {
        return paresDeRotulo;
    }

    /** As simetrias do desenho, cada uma como uma troca de lugar das casas. */
    public int[][] simetrias() {
        return simetrias;
    }

    /** Todas as casas cheias menos {@code vazia}. */
    public long cheioMenos(int vazia) {
        return cheio() & ~(1L << vazia);
    }

    public long cheio() {
        return casas() == 64 ? -1L : (1L << casas()) - 1;
    }

    /** Vale a jogada nessa posicao? */
    public boolean cabe(long posicao, int[] jogada) {
        return (posicao & (1L << jogada[0])) != 0
                && (posicao & (1L << jogada[1])) != 0
                && (posicao & (1L << jogada[2])) == 0;
    }

    /** A posicao depois da jogada, sem conferir se ela cabia. */
    public long aplica(long posicao, int[] jogada) {
        return posicao ^ (1L << jogada[0]) ^ (1L << jogada[1]) ^ (1L << jogada[2]);
    }

    /** As casas de uma posicao, em ordem. */
    public int[] pecas(long posicao) {
        int[] fora = new int[Long.bitCount(posicao)];
        int n = 0;
        for (int c = 0; c < casas(); c++) {
            if ((posicao & (1L << c)) != 0) {
                fora[n++] = c;
            }
        }
        return fora;
    }

    /** A posicao com as casas trocadas de lugar por uma simetria. */
    public long peloEspelho(long posicao, int[] simetria) {
        long nova = 0;
        for (int c = 0; c < casas(); c++) {
            if ((posicao & (1L << c)) != 0) {
                nova |= 1L << simetria[c];
            }
        }
        return nova;
    }

    /** O desenho da posicao: {@code O} com peca, {@code .} vazia. */
    public String desenho(long posicao) {
        StringBuilder texto = new StringBuilder();
        for (int[] linha : grade) {
            StringBuilder fora = new StringBuilder();
            for (int casa : linha) {
                if (casa < 0) {
                    fora.append(' ');
                } else {
                    fora.append((posicao & (1L << casa)) != 0 ? 'O' : '.');
                }
            }
            texto.append(fora.toString().stripTrailing()).append('\n');
        }
        return texto.toString();
    }

    @Override
    public String toString() {
        return nome + " (" + casas() + " casas, " + jogadas.length + " jogadas)";
    }
}
