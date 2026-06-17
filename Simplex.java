import java.util.Scanner;

/**
 * Algoritmo Simplex para Programação Linear
 *
 * Resolve problemas na forma:
 *   Maximizar/Minimizar: c1*x1 + c2*x2 + ... + cn*xn
 *   Sujeito a:           a_i1*x1 + ... + a_in*xn <= b_i  (para cada restrição i)
 *                        x1, x2, ..., xn >= 0
 */
public class Simplex {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------
    private static final double EPSILON = 1e-9;

    // -------------------------------------------------------------------------
    // Campos do solver
    // -------------------------------------------------------------------------
    private double[][] tab;   // tableau (m+1) x (n+m+1)
    private int[]     basis;  // índice da variável básica em cada linha
    private int       m;      // número de restrições
    private int       n;      // número de variáveis de decisão
    private boolean   verbose;

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------
    public Simplex(double[][] A, double[] b, double[] c, boolean verbose) {
        this.m = A.length;
        this.n = c.length;
        this.verbose = verbose;

        // tableau: m+1 linhas, n+m+1 colunas
        //   colunas 0..n-1    → variáveis de decisão x1..xn
        //   colunas n..n+m-1  → variáveis de folga   s1..sm
        //   coluna  n+m       → lado direito (b)
        //   linha   m         → função objetivo (negada)
        tab   = new double[m + 1][n + m + 1];
        basis = new int[m];

        // Preenche linhas de restrição
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++)
                tab[i][j] = A[i][j];
            tab[i][n + i]     = 1.0;   // variável de folga
            tab[i][n + m]     = b[i];  // RHS
            basis[i]          = n + i; // base inicial: variáveis de folga
        }

        // Preenche linha objetivo (coeficientes negados → maximização)
        for (int j = 0; j < n; j++)
            tab[m][j] = -c[j];
    }

    // -------------------------------------------------------------------------
    // Resolve o problema
    // -------------------------------------------------------------------------
    public enum Status { OPTIMAL, UNBOUNDED, INFEASIBLE }

    public Status solve() {
        if (verbose) {
            System.out.println("\n" + linha('─', 56));
            System.out.println(" Tableau Inicial");
            printTableau();
        }

        int iteracao = 0;
        while (true) {
            iteracao++;

            // 1. Escolhe coluna pivô: coeficiente mais negativo na linha objetivo
            int pCol = colunaPivo();
            if (pCol == -1) break;   // solução ótima encontrada

            // 2. Escolhe linha pivô: teste da razão mínima
            int pLin = linhaPivo(pCol);
            if (pLin == -1) {
                System.out.println("\n[!] Problema ILIMITADO — sem solução finita.");
                return Status.UNBOUNDED;
            }

            // 3. Pivoteamento
            pivotar(pLin, pCol);
            basis[pLin] = pCol;

            if (verbose) {
                System.out.printf("%n" + linha('─', 56) + "%n");
                System.out.printf(" Iteração %d  |  Pivot: linha=%d, coluna=%s%n",
                        iteracao, pLin + 1, nomeVariavel(pCol));
                printTableau();
            }
        }
        return Status.OPTIMAL;
    }

    // -------------------------------------------------------------------------
    // Acesso aos resultados
    // -------------------------------------------------------------------------
    /** Retorna o valor ótimo da função objetivo. */
    public double valorOtimo() {
        return tab[m][n + m];
    }

    /** Retorna o vetor solução para as variáveis de decisão. */
    public double[] solucao() {
        double[] x = new double[n];
        for (int i = 0; i < m; i++)
            if (basis[i] < n)
                x[basis[i]] = tab[i][n + m];
        return x;
    }

    /** Retorna os valores das variáveis de folga. */
    public double[] folgas() {
        double[] s = new double[m];
        for (int i = 0; i < m; i++)
            if (basis[i] >= n)
                s[basis[i] - n] = tab[i][n + m];
        return s;
    }

    // -------------------------------------------------------------------------
    // Lógica interna
    // -------------------------------------------------------------------------
    private int colunaPivo() {
        int col = -1;
        double min = -EPSILON;
        for (int j = 0; j < n + m; j++) {
            if (tab[m][j] < min) {
                min = tab[m][j];
                col = j;
            }
        }
        return col;
    }

    private int linhaPivo(int col) {
        int lin = -1;
        double minRatio = Double.MAX_VALUE;
        for (int i = 0; i < m; i++) {
            if (tab[i][col] > EPSILON) {
                double ratio = tab[i][n + m] / tab[i][col];
                if (ratio < minRatio - EPSILON) {
                    minRatio = ratio;
                    lin = i;
                }
            }
        }
        return lin;
    }

    private void pivotar(int lin, int col) {
        double pivo = tab[lin][col];
        for (int j = 0; j <= n + m; j++)
            tab[lin][j] /= pivo;

        for (int i = 0; i <= m; i++) {
            if (i != lin && Math.abs(tab[i][col]) > EPSILON) {
                double fator = tab[i][col];
                for (int j = 0; j <= n + m; j++)
                    tab[i][j] -= fator * tab[lin][j];
            }
        }
    }

    // -------------------------------------------------------------------------
    // Exibição do tableau
    // -------------------------------------------------------------------------
    private void printTableau() {
        int total = n + m;

        // Cabeçalho de colunas
        System.out.printf("%n  %-6s |", "Base");
        for (int j = 0; j < total; j++)
            System.out.printf(" %-8s", nomeVariavel(j));
        System.out.printf(" | %-8s%n", "RHS");

        System.out.println("  " + linha('─', 7) + "+" + linha('─', 9 * total + 1) + "+" + linha('─', 9));

        // Linhas de restrição
        for (int i = 0; i < m; i++) {
            System.out.printf("  %-6s |", nomeVariavel(basis[i]));
            for (int j = 0; j <= total; j++) {
                if (j == total) System.out.printf(" | %-8.4f", tab[i][j]);
                else            System.out.printf(" %-8.4f", tab[i][j]);
            }
            System.out.println();
        }

        System.out.println("  " + linha('─', 7) + "+" + linha('─', 9 * total + 1) + "+" + linha('─', 9));

        // Linha objetivo
        System.out.printf("  %-6s |", "Z");
        for (int j = 0; j <= total; j++) {
            if (j == total) System.out.printf(" | %-8.4f", tab[m][j]);
            else            System.out.printf(" %-8.4f", tab[m][j]);
        }
        System.out.println();
    }

    private String nomeVariavel(int idx) {
        if (idx < n)     return "x" + (idx + 1);
        else             return "s" + (idx - n + 1);
    }

    private static String linha(char c, int len) {
        return String.valueOf(c).repeat(len);
    }

    // =========================================================================
    // MAIN — Interface de terminal
    // =========================================================================
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        cabecalho();

        // ── Tipo de otimização ───────────────────────────────────────────────
        System.out.println("Tipo de problema:");
        System.out.println("  1. Maximização");
        System.out.println("  2. Minimização");
        int tipo = lerInteiro(sc, "Escolha (1 ou 2): ", 1, 2);
        boolean minimizar = (tipo == 2);

        // ── Dimensões ────────────────────────────────────────────────────────
        System.out.println();
        int numVars = lerInteiro(sc, "Número de variáveis de decisão: ", 1, 50);
        int numRest = lerInteiro(sc, "Número de restrições (<=):       ", 1, 50);

        // ── Função objetivo ──────────────────────────────────────────────────
        System.out.println();
        System.out.println("─".repeat(56));
        System.out.printf("Coeficientes da função objetivo (%s)%n",
                objetivoStr(numVars));
        double[] c = lerVetor(sc, numVars, "c");

        if (minimizar)
            for (int j = 0; j < numVars; j++) c[j] = -c[j];

        // ── Restrições ───────────────────────────────────────────────────────
        System.out.println();
        System.out.println("─".repeat(56));
        System.out.printf("Restrições (formato: a1 a2 ... a%d <= b)%n", numVars);
        double[][] A = new double[numRest][numVars];
        double[]   b = new double[numRest];

        for (int i = 0; i < numRest; i++) {
            System.out.printf("%nRestrição %d:%n", i + 1);
            System.out.printf("  Coeficientes (%s): ", ladoEsq(i + 1, numVars));
            A[i] = lerVetorInline(sc, numVars);
            b[i] = lerDouble(sc, "  Lado direito (b" + (i + 1) + "):          ");
            if (b[i] < 0) {
                // Multiplica linha por -1 para manter b >= 0
                for (int j = 0; j < numVars; j++) A[i][j] = -A[i][j];
                b[i] = -b[i];
            }
        }

        // ── Modo verbose ─────────────────────────────────────────────────────
        System.out.println();
        System.out.print("Mostrar tableau a cada iteração? (s/n): ");
        String resp = sc.nextLine().trim().toLowerCase();
        boolean verbose = resp.equals("s") || resp.equals("sim");

        // ── Resolve ──────────────────────────────────────────────────────────
        System.out.println("\n" + "═".repeat(56));
        System.out.println(" RESOLVENDO...");
        System.out.println("═".repeat(56));

        Simplex solver = new Simplex(A, b, c, verbose);
        Status status  = solver.solve();

        // ── Exibe resultado ──────────────────────────────────────────────────
        System.out.println("\n" + "═".repeat(56));
        System.out.println(" RESULTADO");
        System.out.println("═".repeat(56));

        if (status == Status.OPTIMAL) {
            double[] x = solver.solucao();
            double   z = solver.valorOtimo();
            double[] s = solver.folgas();

            System.out.printf("%nStatus: SOLUÇÃO ÓTIMA ENCONTRADA%n");
            System.out.printf("%nValor ótimo:  Z* = %.6f%n", minimizar ? -z : z);

            System.out.println("\nVariáveis de decisão:");
            for (int j = 0; j < numVars; j++)
                System.out.printf("  x%-3d = %.6f%n", j + 1,
                        Math.abs(x[j]) < EPSILON ? 0 : x[j]);

            System.out.println("\nVariáveis de folga:");
            for (int i = 0; i < numRest; i++)
                System.out.printf("  s%-3d = %.6f%n", i + 1,
                        Math.abs(s[i]) < EPSILON ? 0 : s[i]);

        } else if (status == Status.UNBOUNDED) {
            System.out.println("\nStatus: PROBLEMA ILIMITADO");
            System.out.println("A função objetivo pode crescer indefinidamente.");
        } else {
            System.out.println("\nStatus: PROBLEMA INVIÁVEL");
            System.out.println("Não existe solução que satisfaça todas as restrições.");
        }

        System.out.println("\n" + "═".repeat(56));
        sc.close();
    }

    // -------------------------------------------------------------------------
    // Utilitários de E/S
    // -------------------------------------------------------------------------
    private static void cabecalho() {
        System.out.println("╔" + "═".repeat(54) + "╗");
        System.out.println("║        ALGORITMO SIMPLEX — Programação Linear        ║");
        System.out.println("╚" + "═".repeat(54) + "╝");
        System.out.println();
    }

    private static int lerInteiro(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.printf("  [!] Digite um valor entre %d e %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    private static double lerDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("  [!] Entrada inválida. Digite um número.");
            }
        }
    }

    private static double[] lerVetor(Scanner sc, int n, String prefixo) {
        System.out.printf("  Insira %d valores separados por espaço:%n  > ", n);
        return lerVetorInline(sc, n);
    }

    private static double[] lerVetorInline(Scanner sc, int n) {
        while (true) {
            String linha = sc.nextLine().trim().replace(',', '.');
            String[] tokens = linha.split("\\s+");
            if (tokens.length == n) {
                double[] v = new double[n];
                boolean ok = true;
                for (int i = 0; i < n; i++) {
                    try { v[i] = Double.parseDouble(tokens[i]); }
                    catch (NumberFormatException e) { ok = false; break; }
                }
                if (ok) return v;
            }
            System.out.printf("  [!] Esperados %d números. Tente novamente:%n  > ", n);
        }
    }

    private static String objetivoStr(int n) {
        StringBuilder sb = new StringBuilder("c1·x1");
        for (int i = 2; i <= Math.min(n, 3); i++) sb.append(" + c").append(i).append("·x").append(i);
        if (n > 3) sb.append(" + ...");
        return sb.toString();
    }

    private static String ladoEsq(int i, int n) {
        StringBuilder sb = new StringBuilder("a").append(i).append("1·x1");
        for (int j = 2; j <= Math.min(n, 3); j++)
            sb.append(" + a").append(i).append(j).append("·x").append(j);
        if (n > 3) sb.append(" + ...");
        return sb.toString();
    }
}
