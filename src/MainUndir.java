import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.search.GraphStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainUndir extends AbstractProblem {

    IUndirectedGraphVar graph;
    private int count = 0;

    public MainUndir() {
        super();
        level = Level.QUIET;
    }

    @Override
    public void createSolver() {
        solver = new Solver("prost");
    }

    private static final int n = 8;//31;
    private static final int m = 10;//81;
    private static final int l = 7;//4;

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
        solver.post(GCF.nb_edges(graph, VF.fixed(m, solver)));
        solver.post(new Constraint("GirthConstraint", new PropGirth(graph, VF.fixed(l, solver))));
        SuperConstraintFactory.postSymmetryBreaking(graph, solver);
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
        reportSolution();
//        if (solver.isFeasible() == ESat.TRUE) {
//            do {
//                reportSolution();
//            } while (solver.nextSolution());
//        }
    }

    @Override
    public void prettyOut() {
        prettyOut(pw);
    }

    public void prettyOut(PrintStream out) {
        //out.println("$#$> " + Arrays.toString(solver.getVars()));
        out.println("$#$>");
        for (int i = 0; i < n; i++) {
            out.print(i + " -> {");
            for (int v: new MySet(graph.getMandNeighOf(i))) {
                out.print(v + ", ");
            }
            out.println("}");
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {}
        }));
        long time = System.nanoTime();
        MainUndir main = new MainUndir();
        main.level = Level.QUIET;
        main.execute(args);
        pw.println("Solutions: " + main.count);
        pw.close();
        System.setOut(oldOut);
        Chatterbox.printStatistics(main.solver);
        System.out.println("Time: " + (System.nanoTime() - time) / 1_000_000.0 + " seconds");
    }
}
