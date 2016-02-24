import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainKek extends AbstractProblem {

    IDirectedGraphVar graph;
    private int count = 0;

    public MainKek() {
        super();
        level = Level.SOLUTION;
    }

    @Override
    public void createSolver() {
        solver = new Solver("prost");
    }

    private static final int n = 4;

    @Override
    public void buildModel() {
        DirectedGraph GLB = new DirectedGraph(solver, n, SetType.BITSET, true);
        DirectedGraph GUB = new DirectedGraph(solver, n, SetType.BITSET, true);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                GUB.addArc(i, j);        // potential edge
            }
        }
        graph = GraphVarFactory.directed_graph_var("G", GLB, GUB, solver);
        solver.post(GCF.directed_tree(graph, 0));
        solver.post(GCF.max_in_degrees(graph, 1));
        SuperConstraintFactory.postSymmetryBreaking(graph, solver);
    }

    @Override
    public void configureSearch() {

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
        boolean first = solver.findSolution();
        if (first) {
            reportSolution();
        }
        while (solver.nextSolution()) {
            reportSolution();
        }
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
            for (int v: new MySet(graph.getMandSuccOf(i))) {
                out.print(v + ", ");
            }
            out.println("}");
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.setOut(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {}
        }));
        MainKek main = new MainKek();
        main.level = Level.QUIET;
        main.execute(args);
        pw.println("Solutions: " + main.count);
        pw.close();
    }
}
