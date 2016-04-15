package org.chocosolver.solver.sbcstrs.test;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.sbcstrs.SBCF;
import org.chocosolver.solver.search.GraphStrategyFactory;
import org.chocosolver.solver.variables.GVF;
import org.chocosolver.solver.variables.GraphVarFactory;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Моклев Вячеслав
 */
public class StraightSymmetryBreakingTest {
    private PrintStream ps;
    private PrintStream dummyOut;

    {
        dummyOut = new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {
            }
        });
        ps = System.out;
    }

    private IUndirectedGraphVar permutateGraph(IUndirectedGraphVar inputGraph, int[] p, Solver solver) {
        int n = inputGraph.getNbMaxNodes();
        UndirectedGraph LB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        UndirectedGraph UB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        for (int u = 0; u < n; u++) {
            for (int v: new ItSet(inputGraph.getMandNeighOf(u))) {
                LB.addEdge(p[u], p[v]);
            }
            for (int v: new ItSet(inputGraph.getPotNeighOf(u))) {
                UB.addEdge(p[u], p[v]);
            }
        }
        return GVF.undirected_graph_var("G", LB, UB, solver);
    }

    private boolean entailed(IUndirectedGraphVar inputGraph, int[] p) {
        Solver solver = new Solver();
        IUndirectedGraphVar copyGraph = permutateGraph(inputGraph, p, solver);
        solver.set(GraphStrategyFactory.lexico(copyGraph));
        SBCF.postSymmetryBreaking(copyGraph, solver);
        return solver.findSolution();
    }

    private void processSolution(IUndirectedGraphVar graph) {
        int n = graph.getNbMaxNodes();
        int[] p = new int[n];
        for (int i = 0; i < n; i++) {
            p[i] = i;
        }
        boolean flag = false;
        while (next_permutation(p)) { // skip ID permutation
            if (entailed(graph, p)) {
                flag = true;
                break;
            }
        }
        Assert.assertTrue(flag, "n = " + n);
    }

    private void test(int n) {
        System.setOut(dummyOut);
        Solver solver = new Solver();
        UndirectedGraph GLB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        UndirectedGraph GUB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                GUB.addEdge(i, j);
            }
        }
        IUndirectedGraphVar graph = GraphVarFactory.undirected_graph_var("G", GLB, GUB, solver);
        solver.set(GraphStrategyFactory.lexico(graph));
        solver.post(GCF.connected(graph));
        solver.findSolution();
        if (solver.isFeasible() == ESat.TRUE) {
            do {
                processSolution(graph);
            } while (solver.nextSolution());
        }
        System.setOut(ps);
    }

    private static boolean next_permutation(int[] p) {
        for (int a = p.length - 2; a >= 0; --a) {
            if (p[a] < p[a + 1]) {
                for (int b = p.length - 1; ; --b) {
                    if (p[b] > p[a]) {
                        int t = p[a];
                        p[a] = p[b];
                        p[b] = t;
                        for (++a, b = p.length - 1; a < b; ++a, --b) {
                            t = p[a];
                            p[a] = p[b];
                            p[b] = t;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Test
    public void testAll() {
        for (int n = 2; n < 6; n++) {
            test(n);
        }
    }

}
