package org.chocosolver.solver.sbcstrs.test;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.sbcstrs.SBCF;
import org.chocosolver.solver.sbcstrs.test.util.PropGirth;
import org.chocosolver.solver.search.GraphStrategyFactory;
import org.chocosolver.solver.variables.GraphVarFactory;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;
import org.testng.Assert;

import java.io.*;

/**
 * @author Моклев Вячеслав
 */
public class TimeMeasurer {
    private static boolean solutionExists(int n, int m, int l, int symmetryBreakingKind) {
        Solver solver = new Solver();
        UndirectedGraph GLB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        UndirectedGraph GUB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                GUB.addEdge(i, j);
            }
        }
        IUndirectedGraphVar graph = GraphVarFactory.undirected_graph_var("G", GLB, GUB, solver);
        // graph mush contains n nodes, m edges and have girth exactly l
        solver.set(GraphStrategyFactory.lexico(graph));
        solver.post(GCF.nb_edges(graph, VF.fixed(m, solver)));
        solver.post(GCF.connected(graph)); // GCF.postSymmetryBreaking is sb predicate only for connected undirected graphs
        solver.post(new Constraint("GirthConstraint", new PropGirth(graph, VF.fixed(l, solver))));
        // add symmetry breaking constraint if necessary
        switch (symmetryBreakingKind) {
            case 2: SBCF.postSymmetryBreaking(graph, solver); break;
            case 3: SBCF.postSymmetryBreaking2(graph, solver); break;
            case 4: SBCF.postSymmetryBreaking3(graph, solver); break;
        }
        return solver.findSolution();
    }

    private static void measure(int n, int m, int l, int symmetryBreakingKind) throws FileNotFoundException {
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {}
        }));
        System.gc();
        long time = System.nanoTime();
        solutionExists(n, m, l, symmetryBreakingKind);
        System.setOut(oldOut);
        PrintWriter pw = new PrintWriter("(" + n + ", " + m + ", " + l + ", " + symmetryBreakingKind + ").time");
        pw.print("(n, m, l, kind) = " + n + ", " + m + ", " + l + ", " + symmetryBreakingKind
                + "; time: " + (System.nanoTime() - time) / 1_000_000.0 + " milliseconds");
        pw.close();
        System.out.println("(n, m, l, kind) = " + n + ", " + m + ", " + l + ", " + symmetryBreakingKind
                + "; time: " + (System.nanoTime() - time) / 1_000_000.0 + " milliseconds");
    }

    // OEIS, A006856
    private static final int[] a = new int[] {0, 0, 1, 2, 3, 5, 6, 8, 10, 12, 15, 16, 18, 21, 23, 36, 28, 31};

    public static void main(String[] args) throws FileNotFoundException {
//        measure(13, 13, 12, 3);
//        measure(13, 13, 12, 4);
//        measure(13, a[13], 5, 3);
//        measure(13, a[13], 5, 2);
//        measure(9, a[9] + 1, 5, 1);
//        measure(12, a[12] + 1, 5, 2);
//        measure(12, a[12] + 1, 5, 3);
//        measure(12, a[12] + 1, 5, 4);
//        measure(12, 14, 7, 1); // never
//        measure(14, 16, 8, 4);
//        measure(14, 16, 8, 2);
//        measure(14, 16, 8, 3);
    }

}
