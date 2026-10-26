package br.com.conde.restaum;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** As tabelas do README, uma por argumento. */
public final class Medidor {

    private Medidor() {
    }

    private static final Tabuleiro[] VARRIVEIS = {
        Tabuleiro.triangulo(3), Tabuleiro.triangulo(4), Tabuleiro.triangulo(5),
        Tabuleiro.quadrado(3), Tabuleiro.quadrado(4), Tabuleiro.triangulo(6),
    };

    public static void main(String[] args) {
        String qual = args.length == 0 ? "tudo" : args[0];
        switch (qual) {
            case "tabuleiros" -> tabuleiros();
            case "inicios" -> inicios();
            case "rotulo" -> rotulo();
            case "provas" -> provas();
            case "espelho" -> espelho();
            case "cruz" -> cruz();
            default -> {
                tabuleiros();
                inicios();
                rotulo();
                provas();
                espelho();
                cruz();
            }
        }
    }

    static void tabuleiros() {
        System.out.println("== os tabuleiros ==");
        System.out.println("tabuleiro      casas jogadas espelhos partida posicoes  becos pior comecos parados");
        for (Tabuleiro t : new Tabuleiro[] {
                Tabuleiro.triangulo(3), Tabuleiro.triangulo(4), Tabuleiro.triangulo(5),
                Tabuleiro.triangulo(6), Tabuleiro.quadrado(3), Tabuleiro.quadrado(4),
                Tabuleiro.cruzInglesa(), Tabuleiro.cruzEuropeia(), Tabuleiro.cruzAlema()}) {
            String posicoes = "-";
            String becos = "-";
            String pior = "-";
            String parados = "-";
            if (t.casas() <= Varredura.LIMITE_DE_CASAS) {
                Varredura v = new Varredura(t, 0);
                posicoes = String.valueOf(v.posicoes());
                becos = String.valueOf(v.becos());
                pior = String.valueOf(v.piorBeco());
                int quantos = 0;
                for (int casa = 0; casa < t.casas(); casa++) {
                    if (new Varredura(t, casa).posicoes() == 1) {
                        quantos++;
                    }
                }
                parados = String.valueOf(quantos);
            }
            System.out.printf(Locale.ROOT, "%-14s %5d %7d %8d %7d %8s %6s %4s %15s%n",
                    t.nome(), t.casas(), t.jogadas().length, t.simetrias().length,
                    t.casas() - 2, posicoes, becos, pior, parados);
        }
    }

    static void inicios() {
        Tabuleiro t = Tabuleiro.triangulo(5);
        System.out.println("== triangulo de 15 casas: cada casa vazia no comeco ==");
        System.out.println("vazia posicoes minimo becos  partidas finais");
        for (int casa = 0; casa < t.casas(); casa++) {
            Varredura v = new Varredura(t, casa);
            StringBuilder finais = new StringBuilder();
            for (int f : v.finais()) {
                finais.append(t.nomeDaCasa(f)).append(' ');
            }
            System.out.printf(Locale.ROOT, "%5s %8d %6d %5d %9d %s%n",
                    t.nomeDaCasa(casa), v.posicoes(), v.minimoDePecas(), v.becos(),
                    v.partidasQueResolvem(), finais.toString().trim());
        }
    }

    static void rotulo() {
        System.out.println("== o rotulo contra a varredura ==");
        System.out.println("tabuleiro      pares permite acontece acerta erra");
        for (Tabuleiro t : VARRIVEIS) {
            Rotulo r = new Rotulo(t);
            int permite = 0;
            int acontece = 0;
            int erra = 0;
            for (int casa = 0; casa < t.casas(); casa++) {
                Varredura v = new Varredura(t, casa);
                Set<Integer> verdade = new HashSet<>();
                for (int f : v.finais()) {
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
            System.out.printf(Locale.ROOT, "%-14s %5d %7d %8d %6d %4d%n",
                    t.nome(), t.casas() * t.casas(), permite, acontece, permite - erra, erra);
        }
    }

    static void provas() {
        System.out.println("== quanto custa provar que nao da ==");
        System.out.println("tabuleiro      vazia alvo rotulo resposta     nos sem rotulo  nos com rotulo");
        prova(Tabuleiro.triangulo(5), "1", "13");
        prova(Tabuleiro.triangulo(5), "1", "5");
        prova(Tabuleiro.triangulo(5), "1", "2");
        prova(Tabuleiro.triangulo(6), "1", "20");
        prova(Tabuleiro.triangulo(6), "1", "2");
        prova(Tabuleiro.quadrado(4), "b1", "c1");
        prova(Tabuleiro.quadrado(4), "b1", "b1");
    }

    private static void prova(Tabuleiro t, String vazia, String alvo) {
        int casaVazia = t.casaChamada(vazia);
        int casaAlvo = t.casaChamada(alvo);
        long comeco = t.cheioMenos(casaVazia);
        boolean deixa = new Rotulo(t).deixaTerminarEm(comeco, casaAlvo);
        Busca sem = new Busca(t, true, false, Busca.Espelho.NENHUM);
        boolean achou = sem.resolve(comeco, casaAlvo) != null;
        Busca com = new Busca(t, true, true, Busca.Espelho.NENHUM);
        com.resolve(comeco, casaAlvo);
        System.out.printf(Locale.ROOT, "%-14s %5s %4s %6b %-10s %14d %15d%n",
                t.nome(), vazia, alvo, deixa, achou ? "da" : "nao da", sem.nos(), com.nos());
    }

    static void espelho() {
        System.out.println("== a memoria espelhada do jeito errado ==");
        System.out.println("tabuleiro      pares da  | so os que fixam o alvo: erros  nos | todos: erros  nos");
        for (Tabuleiro t : VARRIVEIS) {
            int pares = 0;
            int da = 0;
            int errosCerto = 0;
            int errosTodos = 0;
            long nosCerto = 0;
            long nosTodos = 0;
            for (int vazia = 0; vazia < t.casas(); vazia++) {
                Varredura v = new Varredura(t, vazia);
                Set<Integer> verdade = new HashSet<>();
                for (int f : v.finais()) {
                    verdade.add(f);
                }
                for (int alvo = 0; alvo < t.casas(); alvo++) {
                    pares++;
                    boolean deveria = verdade.contains(alvo);
                    if (deveria) {
                        da++;
                    }
                    Busca certa = Busca.completa(t);
                    boolean achouCerta = certa.resolve(t.cheioMenos(vazia), alvo) != null;
                    nosCerto += certa.nos();
                    Busca todos = new Busca(t, true, true, Busca.Espelho.TODOS);
                    boolean achouTodos = todos.resolve(t.cheioMenos(vazia), alvo) != null;
                    nosTodos += todos.nos();
                    if (achouCerta != deveria) {
                        errosCerto++;
                    }
                    if (achouTodos != deveria) {
                        errosTodos++;
                    }
                }
            }
            System.out.printf(Locale.ROOT, "%-14s %5d %3d | %29d %6d | %12d %6d%n",
                    t.nome(), pares, da, errosCerto, nosCerto, errosTodos, nosTodos);
        }
    }

    static void cruz() {
        Tabuleiro t = Tabuleiro.cruzInglesa();
        Rotulo r = new Rotulo(t);
        int centro = t.casaChamada("d4");
        long comeco = t.cheioMenos(centro);
        System.out.println("== a cruz inglesa, 33 casas, buraco no centro ==");
        StringBuilder permitidas = new StringBuilder();
        for (int casa : r.casasPermitidas(comeco)) {
            permitidas.append(t.nomeDaCasa(casa)).append(' ');
        }
        System.out.println("o rotulo deixa a ultima peca parar em: " + permitidas.toString().trim()
                + "  (de " + t.casas() + " casas, sem abrir uma posicao sequer)");
        System.out.println("alvo  espelhos resposta         nos guardadas");
        String[][] linhas = {{"d4", "NENHUM"}, {"d4", "QUE_FIXAM_O_ALVO"}, {"d1", "QUE_FIXAM_O_ALVO"},
            {"a4", "QUE_FIXAM_O_ALVO"}, {"d7", "QUE_FIXAM_O_ALVO"}};
        for (String[] linha : linhas) {
            String nome = linha[0];
            int alvo = t.casaChamada(nome);
            Busca b = new Busca(t, true, true, Busca.Espelho.valueOf(linha[1]));
            List<int[]> partida = b.resolve(comeco, alvo);
            String resposta = partida == null ? "nao da"
                    : "achou em " + partida.size() + " jogadas";
            if (partida != null) {
                Juiz.confere(t, comeco, partida);
            }
            System.out.printf(Locale.ROOT, "%-5s %8d %-22s %9d %9d%n",
                    nome, b.espelhosEmUso(), resposta, b.nos(), b.tamanhoDaMemoria());
        }
        System.out.println();
        Busca b = Busca.completa(t);
        List<int[]> partida = b.resolve(comeco, centro);
        System.out.println("uma partida do centro para o centro:");
        System.out.println(Juiz.emTexto(t, partida));
        System.out.println();
        System.out.print(t.desenho(Juiz.confere(t, comeco, partida)));
    }
}
