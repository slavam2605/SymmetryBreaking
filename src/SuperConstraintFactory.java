import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.variables.*;

/**
 * @author Моклев Вячеслав
 */
public class SuperConstraintFactory {

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
        solver.post(new Constraint("AdjacencyMatrix", new PropAdjacencyMatrix(graph, t)));

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
        solver.post(new Constraint("AdjacencyMatrix", new PropAdjacencyUndirectedMatrix(graph, t)));

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
}
