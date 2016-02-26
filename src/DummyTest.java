import org.chocosolver.solver.Solver;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.sbcstrs.SymmetryBreakingConstraintFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.GraphVarFactory;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Моклев Вячеслав
 */
public class DummyTest {
    public static void main(String[] args) {
        IUndirectedGraphVar graph;
        int n = 4;
        Solver solver = new Solver();
        UndirectedGraph GLB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        UndirectedGraph GUB = new UndirectedGraph(solver, n, SetType.BITSET, true);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                GUB.addEdge(i, j);        // potential edge
            }
        }
        graph = GraphVarFactory.undirected_graph_var("G", GLB, GUB, solver);
        SymmetryBreakingConstraintFactory.postSymmetryBreaking(graph, solver);
        solver.post(GCF.connected(graph));
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {}
        }));
        long time = System.nanoTime();
        solver.findAllSolutions();
        System.setOut(oldOut);
        Chatterbox.printStatistics(solver);
        System.out.println("Time: " + (System.nanoTime() - time) / 1_000_000.0 + " milliseconds");
    }
}
