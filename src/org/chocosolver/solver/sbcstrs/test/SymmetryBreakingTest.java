package org.chocosolver.solver.sbcstrs.test;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.sbcstrs.SBCF;
import org.chocosolver.solver.sbcstrs.test.util.PropGirth;
import org.chocosolver.solver.sbcstrs.test.util.PropIncrementalGirth;
import org.chocosolver.solver.search.GraphStrategyFactory;
import org.chocosolver.solver.search.strategy.GraphStrategies;
import org.chocosolver.solver.variables.GraphVarFactory;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.testng.Assert.*;

/**
 * @author Моклев Вячеслав
 */
public class SymmetryBreakingTest {
    private static PrintStream oldOut;

    public static boolean solutionExists(int n, int m, int l, boolean addSymmetryBreaking) {
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

    public static void test(int n, int m, int l) {
        Assert.assertEquals(
                solutionExists(n, m, l, true),
                solutionExists(n, m, l, false),
                "symmetry breaking: " + n + ", " + m + ", " + l
        );
    }

    @Test
    public static void testSimple1() {
        test(1, 1, 1);
    }

    @Test
    public static void testSimple2() {
        test(5, 4, 2);
    }

    @Test
    public static void testSimple3() {
        test(3, 5, 4);
    }

    @Test
    public static void testSimple4() {
        test(2, 1, 3);
    }

    @Test
    public static void testSimple5() {
        test(3, 2, 3);
    }

    @Test
    public static void testMedium1() {
        test(4, 3, 3);
    }

    @Test
    public static void testHard1() {
        Assert.assertEquals(
                solutionExists(10, 10, 9, true),
                true // it's preprocessed value of solutionExists(10, 10, 9, false), 225 seconds @ AMD FX-8150 (3.6 GHz, 8 Gb RAM)
        );
    }

    @Test
    public static void testAllSmall() {
        for (int n = 1; n <= 6; n++) {
            for (int m = 1; m <= 6; m++) {
                for (int l = 1; l <= 6; l++) {
                    System.out.println("$!$> " + n + ", " + m + ", " + l);
                    test(n, m, l);
                }
            }
        }
    }

    @Test
    public static void testSpeedWithSymmetryBreaking() {
        Assert.assertEquals(
                solutionExists(8, 10, 6, true),
                false // it's preprocessed value of solutionExists(8, 10, 6, false), 70 seconds @ AMD FX-8150 (3.6 GHz, 8 Gb RAM)
        );
    }

}