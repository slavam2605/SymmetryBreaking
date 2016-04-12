import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.sbcstrs.PropIncrementalAdjacencyMatrix;
import org.chocosolver.solver.sbcstrs.PropIncrementalAdjacencyUndirectedMatrix;
import org.chocosolver.solver.sbcstrs.SBCF;
import org.chocosolver.solver.sbcstrs.test.util.PropGirth;
import org.chocosolver.solver.search.GraphStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

public class GraphStatistics extends AbstractProblem {

    IUndirectedGraphVar graph;
    private int count = 0;

    public GraphStatistics() {
        super();
        level = Level.QUIET;
    }

    @Override
    public void createSolver() {
        solver = new Solver();
    }

    private static final int n = 7;//31;

    @Override
    public void buildModel() {
        UndirectedGraph GLB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        UndirectedGraph GUB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                GUB.addEdge(i, j);        // potential edge
            }
        }
        graph = GraphVarFactory.undirected_graph_var("G", GLB, GUB, solver);
        solver.post(GCF.connected(graph));
    }

    @Override
    public void configureSearch() {
        solver.set(GraphStrategyFactory.lexico(graph));
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
        count++;
        prettyOut();
    }

    @Override
    public void solve() {
        solver.findSolution();
//        reportSolution();
        if (solver.isFeasible() == ESat.TRUE) {
            do {
                reportSolution();
            } while (solver.nextSolution());
        }
    }

    @Override
    public void prettyOut() {
        prettyOut(pw);
    }

    public int total = 0;

    public void prettyOut(PrintStream out) {
//        out.println("$#$>");
//        for (int i = 0; i < n; i++) {
//            out.print(i + " -> {");
//            for (int v: new ItSet(graph.getMandNeighOf(i))) {
//                out.print(v + ", ");
//            }
//            out.println("}");
//        }
        List<Integer> degs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int deg = 0;
            for (int ignored : new ItSet(graph.getMandNeighOf(i))) {
                deg++;
            }
            degs.add(deg);
        }
        Collections.sort(degs);
        int highCount = 0;
        for (int x: degs) {
            if (x == degs.get(degs.size() - 1)) {
                highCount++;
            }
        }

        total += highCount;
    }

    public static void main(String[] args) throws FileNotFoundException {
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {}
        }));
        long time = System.nanoTime();
        GraphStatistics main = new GraphStatistics();
        main.level = Level.QUIET;
        main.execute();
        //pw.println("Solutions: " + main.count);
        pw.println((double) main.total / main.count);
        pw.close();
        System.setOut(oldOut);
        Chatterbox.printStatistics(main.solver);
        System.out.println("Time: " + (System.nanoTime() - time) / 1_000_000.0 + " milliseconds");
    }
}
