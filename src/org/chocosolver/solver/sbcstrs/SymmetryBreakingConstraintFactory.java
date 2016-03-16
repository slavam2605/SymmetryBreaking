package org.chocosolver.solver.sbcstrs;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * This factory contains symmetry breaking predicated for
 * connected undirected graphs and for directed graphs, where all nodes
 * are reachable from node 0. It based on method, introduced by TODO add authors
 * and consists in checking whether graph is BFS-enumerated.
 *
 * @author Моклев Вячеслав
 */
public class SymmetryBreakingConstraintFactory {

    /**
     * Post a symmetry breaking constraint. This constraint is a symmetry breaking for
     * class of directed graphs which contain a directed tree with root in node 0.
     * (All nodes must be reachable from node 0)
     * Note, that this method post this constraint directly, so it cannot be reified.
     * If you need to get a single {@link Constraint} use {@link #symmetryBreaking(IDirectedGraphVar, Solver)}.
     *
     * @param graph graph to be constrainted
     * @param solver solver to post constraint
     */
    public static void postSymmetryBreaking(IDirectedGraphVar graph, Solver solver) {
        // ---------------------- variables ------------------------
        int n = graph.getNbMaxNodes();
        // t[i, j]
        BoolVar[] t = VF.boolArray("T[]", n * n, solver);
        // p[i]
        IntVar[] p = new IntVar[n];
        p[0] = VF.fixed("P[0]", 0, solver);
        for (int i = 1; i < n; i++) {
            p[i] = VF.integer("P[" + i + "]", 0, i - 1, solver);
        }
        // ---------------------- constraints -----------------------
        // t[i, j] <-> G
        solver.post(new Constraint("AdjacencyMatrix", new PropIncrementalAdjacencyMatrix(graph, t)));

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

        // p[i] ≤ p[i + 1]
        for (int i = 1; i < n - 1; i++) {
            solver.post(ICF.arithm(p[i], "<=", p[i + 1]));
        }
    }

    /**
     * Post a symmetry breaking constraint. This constraint is a symmetry breaking for
     * class of undirected connected graphs.
     * Note, that this method post this constraint directly, so it cannot be reified.
     * If you need to get a single {@link Constraint} use {@link #symmetryBreaking(IUndirectedGraphVar, Solver)}.
     *
     * @param graph graph to be constrainted
     * @param solver solver to post constraint
     */
    public static void postSymmetryBreaking(IUndirectedGraphVar graph, Solver solver) {
        // ---------------------- variables ------------------------
        int n = graph.getNbMaxNodes();

        // t[i, j]
        BoolVar[] t = VF.boolArray("T[]", n * n, solver);

        // p[i]
        IntVar[] p = new IntVar[n];
        p[0] = VF.fixed("P[0]", 0, solver);
        for (int i = 1; i < n; i++) {
            p[i] = VF.integer("P[" + i + "]", 0, i - 1, solver);
        }
        // ---------------------- constraints -----------------------
        // t[i, j] <-> G
        solver.post(new Constraint("AdjacencyMatrix", new PropIncrementalAdjacencyUndirectedMatrix(graph, t)));

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

        // p[i] ≤ p[i + 1]
        for (int i = 1; i < n - 1; i++) {
            solver.post(ICF.arithm(p[i], "<=", p[i + 1]));
        }
    }

    /**
     * Does the same as {@link #postSymmetryBreaking(IDirectedGraphVar, Solver)}, but
     * post nothing and return single {@link Constraint}.
     * It can be reified, but beware: it should be posted manually and it can be slower.
     *
     * @param graph graph to be constrainted
     * @param solver solver, connected to graph
     */
    public static Constraint symmetryBreaking(IUndirectedGraphVar graph, Solver solver) {
        // ---------------------- variables ------------------------
        int n = graph.getNbMaxNodes();

        // t[i, j]
        BoolVar[] t = VF.boolArray("T[]", n * n, solver);

        // p[i]
        IntVar[] p = new IntVar[n];
        p[0] = VF.fixed("P[0]", 0, solver);
        for (int i = 1; i < n; i++) {
            p[i] = VF.integer("P[" + i + "]", 0, i - 1, solver);
        }
        // ---------------------- constraints -----------------------
        // t[i, j] <-> G
        Constraint first = new Constraint("AdjacencyMatrix", new PropIncrementalAdjacencyUndirectedMatrix(graph, t));

        // (p[j] == i) ⇔ t[i, j] and AND(!t[k, j], 0 ≤ k < j)
        Constraint[] secondA = new Constraint[(n - 1) * (n - 1)];
        Constraint[] secondB = new Constraint[(n - 1) * (n - 1)];
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
                secondA[i + (j - 1) * (n - 1)] = LCF.ifThen_reifiable(pij, c);
                secondB[i + (j - 1) * (n - 1)] = LCF.ifThen_reifiable(c, pij);
            }
        }

        // p[i] ≤ p[i + 1]
        Constraint[] third = new Constraint[n - 1];
        for (int i = 1; i < n - 1; i++) {
            third[i - 1] = ICF.arithm(p[i], "<=", p[i + 1]);
        }
        Constraint[] all = ArrayUtils.append(new Constraint[] {first}, secondA, secondB, third);
        return LCF.and(all);
    }

    /**
     * Does the same as {@link #postSymmetryBreaking(IUndirectedGraphVar, Solver)}, but
     * post nothing and return single {@link Constraint}.
     * It can be reified, but beware: it should be posted manually and it can be slower.
     *
     * @param graph graph to be constrainted
     * @param solver solver, connected to graph
     */
    public static Constraint symmetryBreaking(IDirectedGraphVar graph, Solver solver) {
        // ---------------------- variables ------------------------
        int n = graph.getNbMaxNodes();

        // t[i, j]
        BoolVar[] t = VF.boolArray("T[]", n * n, solver);

        // p[i]
        IntVar[] p = new IntVar[n];
        p[0] = VF.fixed("P[0]", 0, solver);
        for (int i = 1; i < n; i++) {
            p[i] = VF.integer("P[" + i + "]", 0, i - 1, solver);
        }
        // ---------------------- constraints -----------------------
        // t[i, j] <-> G
        Constraint first = new Constraint("AdjacencyMatrix", new PropIncrementalAdjacencyMatrix(graph, t));

        // (p[j] == i) ⇔ t[i, j] and AND(!t[k, j], 0 ≤ k < j)
        Constraint[] secondA = new Constraint[(n - 1) * (n - 1)];
        Constraint[] secondB = new Constraint[(n - 1) * (n - 1)];
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
                secondA[i + (j - 1) * (n - 1)] = LCF.ifThen_reifiable(pij, c);
                secondB[i + (j - 1) * (n - 1)] = LCF.ifThen_reifiable(c, pij);
            }
        }

        // p[i] ≤ p[i + 1]
        Constraint[] third = new Constraint[n - 1];
        for (int i = 1; i < n - 1; i++) {
            third[i - 1] = ICF.arithm(p[i], "<=", p[i + 1]);
        }
        Constraint[] all = ArrayUtils.append(new Constraint[] {first}, secondA, secondB, third);
        return LCF.and(all);
    }

    public static void postSymmetryBreaking2(IUndirectedGraphVar graph, Solver solver) {
        int n = graph.getNbMaxNodes();
        BoolVar[] t = VF.boolArray("T[]", n * n, solver);
        solver.post(new Constraint("AdjacencyMatrix", new PropIncrementalAdjacencyUndirectedMatrix(graph, t)));
        solver.post(new Constraint("SymmBreak", new PropSymmetryBreaking(t)));
    }

    public static void postSymmetryBreaking3(IUndirectedGraphVar graph, Solver solver) {
        int n = graph.getNbMaxNodes();
        BoolVar[] t = VF.boolArray("T[]", n * n, solver);
        solver.post(new Constraint("AdjacencyMatrix", new PropIncrementalAdjacencyUndirectedMatrix(graph, t)));
        solver.post(new Constraint("SymmBreak", new PropSymmetryBreakingEx(t)));
    }

}
