import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.variables.*;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Computes the transitive closure (TC) of t given directed graphs G
 * by finding the smallest (w.r.t. the number of arcs) transitive supergraph of G
 *
 * @author Jean-Guillaume Fages
 * @since 30/07/13
 */
public class Main extends AbstractProblem {

    private static final int SYMBOLS_COUNT = 2;
    IDirectedGraphVar tc;
    IntVar nbArcs;
    IntVar nbArcs2;
    BoolVar[] t;
    IntVar[] p;
    IntVar[] m;
    IntVar[] y;

    public Main() {
        super();
        level = Level.QUIET;
    }

    @Override
    public void createSolver() {
        solver = new Solver("transitive closure sample");
    }

    private static final int n = 3;

    @Override
    public void buildModel() {

//        DirectedGraph GLB = new DirectedGraph(solver, n, SetType.BITSET, true);
//        DirectedGraph GUB = new DirectedGraph(solver, n, SetType.BITSET, true);
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++) {
//                if (i != j) {
//                    GUB.addArc(i, j);        // potential edge
//                }
//            }
//        }

        // --------------- variables -------------------

        // G
        //graph = GraphVarFactory.directed_graph_var("G", GLB, GUB, solver);

        // t[i, j]
        t = VF.boolArray("T[]", n * n, solver);

        // p[i]
        //p = VF.integerArray("P[]", n, 0, n - 2, solver);
        p = new IntVar[n];
        p[0] = VF.fixed("P[0]", 0, solver);
        for (int i = 1; i < n; i++) {
            p[i] = VF.integer("P[" + i + "]", 0, i - 1, solver);
        }

        // m[i, j]
        m = VF.integerArray("M[]", n * n, -1, SYMBOLS_COUNT - 1, solver);

        // y[i, e]
        y = VF.integerArray("Y[]", n * SYMBOLS_COUNT, 0, n - 1, solver);

        // ---------------- constraints ------------------

        // t[i, j] <-> G
        //solver.post(new Constraint("EDGE_BOUND", new org.chocosolver.solver.sbcstrs.PropAdjacencyMatrix(graph, t)));

        // TODO remove
//        solver.post(ICF.arithm(y[0], "=", VF.fixed(1, solver)));
//        solver.post(ICF.arithm(y[1], "=", VF.fixed(1, solver)));
//        solver.post(ICF.arithm(y[2], "=", VF.fixed(2, solver)));
//        solver.post(ICF.arithm(y[3], "=", VF.fixed(2, solver)));
//        solver.post(ICF.arithm(y[4], "=", VF.fixed(2, solver)));
//        solver.post(ICF.arithm(y[5], "=", VF.fixed(2, solver)));

        // t[i, j] = OR(y[i, e] == j, e ∈ Σ)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Constraint[] eq = new Constraint[SYMBOLS_COUNT];
                IntVar J = VF.fixed(j, solver);
                for (int e = 0; e < SYMBOLS_COUNT; e++) {
                    eq[e] = ICF.arithm(y[e + i * SYMBOLS_COUNT], "=", J);
                }
                BoolVar orAll = LCF.or(eq).reif();
                System.out.println(LCF.or(eq));
                solver.post(ICF.arithm(t[i + j * n], "=", orAll));
            }
        }

        // (p[j] == i) ⇔ t[i, j] and AND(!t[k, j], 0 ≤ k < j)
        for (int i = 0; i < n - 1; i++) {
            IntVar I = VF.fixed(i, solver);
            for (int j = 1; j < n; j++) {
                BoolVar[] clause = new BoolVar[i + 1];
                clause[i] = t[i + j * n];
                for (int k = 0; k < i; k++) {
                    clause[k] = t[k + j * n].not();
                }
                Constraint c = LCF.and(clause);
                Constraint pij = ICF.arithm(p[j], "=", I);
                LCF.ifThen(pij, c);
                LCF.ifThen(c, pij);
            }
        }

        // costeel: p[0] is useless
//        solver.post(ICF.arithm(p[0], "=", VF.fixed(0, solver)));

        // p[i] ≤ p[i + 1]
        for (int i = 1; i < n - 1; i++) {
            solver.post(ICF.arithm(p[i], "<=", p[i + 1]));
        }

        // (t[i, j] == 0) ⇒ (m[i, j] == -1)
        IntVar ZERO = VF.fixed(0, solver);
        IntVar NEG_UNIT = VF.fixed(-1, solver);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                LCF.ifThen(
                        ICF.arithm(t[i + j * n], "=", ZERO),
                        ICF.arithm(m[i + j * n], "=", NEG_UNIT)
                );
            }
        }

        // (t[i, j] == 1) ⇒ [(m[i, j] == e) ⇔ (y[i, e] == j and AND(y[i, e1] != j, 0 ≤ e1 < e)]
        IntVar ONE = VF.fixed(1, solver);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                IntVar J = VF.fixed(j, solver);
                for (int e = 0; e < SYMBOLS_COUNT; e++) {
                    Constraint mij = ICF.arithm(m[i + j * n], "=", VF.fixed(e, solver));
                    Constraint[] neq = new Constraint[e + 1];
                    neq[e] = ICF.arithm(y[e + i * SYMBOLS_COUNT], "=", J);
                    for (int e1 = 0; e1 < e; e1++) {
                        neq[e1] = ICF.arithm(y[e1 + i * SYMBOLS_COUNT], "!=", J);
                    }
                    Constraint and = LCF.and(neq);
                    Constraint to = LCF.ifThen_reifiable(mij, and);
                    Constraint from = LCF.ifThen_reifiable(and, mij);
                    LCF.ifThen(
                            ICF.arithm(t[i + j * n], "=", ONE),
                            LCF.and(to, from)
                    );
                }
            }
        }

        // (p[j] == i && p[j + 1] == i) ⇒ (m[i][j] < m[i][j + 1])
        for (int i = 0; i < n - 1; i++) {
            IntVar I = VF.fixed(i, solver);
            for (int j = i + 1; j < n - 1; j++) {
                LCF.ifThen(
                        LCF.and(
                                ICF.arithm(p[j], "=", I),
                                ICF.arithm(p[j + 1], "=", I)
                        ),
                        ICF.arithm(m[i + j * n], "<", m[i + (j + 1) * n])
                );
            }
        }

//        int[] unity = new int[n * SYMBOLS_COUNT];
//        Arrays.fill(unity, 1);
//        IntVar countOfEdges = VF.fixed("countOfEdges_VAR", n, solver);
//        solver.post(ICF.scalar(y, unity, ">", countOfEdges));
    }

    @Override
    public void configureSearch() {
//        solver.set(ISF.lexico_LB(y));
    }

    private static PrintStream pw;
    private static Set<String> sols;

    static {
        try {
            pw = new PrintStream("keks.log");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        sols = new HashSet<>();
    }

    private void reportSolution() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(i).append(" -> {");
            for (int e = 0; e < SYMBOLS_COUNT; e++) {
                if (y[e + i * SYMBOLS_COUNT].isInstantiated()) {
                    sb.append(y[e + i * SYMBOLS_COUNT].getValue()).append(", ");
                } else {
                    throw new IllegalStateException();
                }
            }
            sb.append("} ");
        }

        int[][] y2d = new int[n][SYMBOLS_COUNT];
        for (int i = 0; i < n; i++) {
            for (int e = 0; e < SYMBOLS_COUNT; e++) {
                if (y[e + i * SYMBOLS_COUNT].isInstantiated()) {
                    y2d[i][e] = y[e + i * SYMBOLS_COUNT].getValue();
                } else {
                    throw new IllegalStateException();
                }
            }
        }
        if (BruteForceChecker.BFSEnumerated(y2d) != BruteForceChecker.NOT_CONNECTED) {
            sols.add(sb.toString());
        }
        if (BruteForceChecker.BFSEnumerated(y2d) == BruteForceChecker.NOT_BFS_ENUMERATED) {
            pw.println(sb.toString());
            prettyOut(pw);
            pw.println("-------------------------------");
        }
    }

    @Override
    public void solve() {
        //solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, nbArcs);
//        solver.findSolution();
        solver.findAllSolutions();
//        boolean first = solver.findSolution();
//        if (first) {
//            reportSolution();
//        }
//        while (solver.nextSolution()) {
//            reportSolution();
//        }
//        pw.println(sols.size());
    }

    @Override
    public void prettyOut() {
        prettyOut(System.out);
    }

    public void prettyOut(PrintStream out) {
        out.println("fuck: " + Arrays.toString(solver.getVars()));
        out.println("P[i]: ");
        for (int i = 0; i < n; i++) {
//            if (i == 1230) {
//                System.out.print("*, ");
//            } else {
                if (p[i].isInstantiated()) {
                    out.print(p[i].getValue() + ", ");
                } else {
                    out.print("?, ");
                }
//            }
        }
        out.println();
        out.println("T[i, j]: ");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                out.print(t[i + j * n].getValue() + " ");
            }
            out.println();
        }
        out.println("Y[i, e]: ");
        for (int i = 0; i < n; i++) {
            out.print(i + " -> {");
            for (int e = 0; e < SYMBOLS_COUNT; e++) {
                if (y[e + i * SYMBOLS_COUNT].isInstantiated()) {
                    out.print(y[e + i * SYMBOLS_COUNT].getValue() + ", ");
                } else {
                    out.print("?, ");
                }
            }
            out.println("}");
        }
        out.println("M[i, j]: ");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (m[i + j * n].isInstantiated()) {
                    out.print(m[i + j * n].getValue() + " ");
                } else {
                    out.print("? ");
                }
            }
            out.println();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        //System.setOut(new PrintStream("out.log"));
        Main main = new Main();
        main.level = Level.QUIET;
        main.execute(args);
        main.prettyOut();
        pw.close();
    }
}
