package org.chocosolver.solver.sbcstrs.test;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.sbcstrs.SBCF;
import org.chocosolver.solver.sbcstrs.test.util.PropGirth;
import org.chocosolver.solver.search.GraphStrategyFactory;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class SymmetryBreakingDirectedTest {
    private static PrintStream oldOut;

    private static Constraint containsDirectedTree(IDirectedGraphVar graph) {
        return new Constraint("subTree", new Propagator<IDirectedGraphVar>(new IDirectedGraphVar[] {graph}, PropagatorPriority.LINEAR, false) {
            IDirectedGraphVar graph = vars[0];
            int n = graph.getNbMaxNodes();

            void dfs(boolean[] used, int u) {
                used[u] = true;
                for (int v: new ItSet(graph.getPotSuccOf(u))) {
                    if (!used[v]) {
                        dfs(used, v);
                    }
                }
            }

            boolean entailed() {
                boolean[] used = new boolean[n];
                dfs(used, 0);
                for (int i = 0; i < n; i++) {
                    if (!used[i]) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void propagate(int i) throws ContradictionException {
                if (!entailed()) {
                    throw new ContradictionException();
                }
            }

            @Override
            public ESat isEntailed() {
                if (entailed()) {
                    if (graph.isInstantiated()) {
                        return ESat.TRUE;
                    }
                    return ESat.UNDEFINED;
                } else {
                    return ESat.FALSE;
                }
            }
        });
    }

    /**
     * Tries to find a solution of the given problem
     *
     * @param n count of nodes
     * @param m count of arcs
     * @param addSymmetryBreaking enable symmetry breaking predicates or not
     * @return true, if solution exists and false otherwise
     */
    private static boolean solutionExists(int n, int m, boolean addSymmetryBreaking) {
        Solver solver = new Solver();
        DirectedGraph GLB = new DirectedGraph(solver, n, SetType.BITSET, true);
        DirectedGraph GUB = new DirectedGraph(solver, n, SetType.BITSET, true);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                GUB.addArc(i, j);
            }
        }
        IDirectedGraphVar graph = GraphVarFactory.directed_graph_var("G", GLB, GUB, solver);

        solver.set(GraphStrategyFactory.lexico(graph));

        solver.post(containsDirectedTree(graph));
        solver.post(GCF.nb_arcs(graph, VF.fixed(m, solver)));
        solver.post(GCF.no_circuit(graph));

        // add symmetry breaking constraint if necessary
        if (addSymmetryBreaking) {
            SBCF.postSymmetryBreaking(graph, solver);
        }
        return solver.findSolution();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        oldOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {}
        }));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        System.setOut(oldOut);
    }

    /**
     * Checks equivalence of existance of solution
     * with and without symmetry breaking.
     *
     * @param n count of nodes
     * @param m count of arcs
     */
    public static void test(int n, int m) {
        System.err.println(solutionExists(n, m, true) + " :: " + solutionExists(n, m, false));
        Assert.assertEquals(
                solutionExists(n, m, true),
                solutionExists(n, m, false),
                "symmetry breaking: " + n + ", " + m
        );
    }

    @Test
    public static void testAllSimple() {
        for (int n = 0; n < 10; n++) {
            for (int m = 0; m < n * n; m++) {
                test(n, m);
            }
        }
    }

}
