import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.cstrs.GCF;
import org.chocosolver.solver.sbcstrs.SBCF;
import org.chocosolver.solver.sbcstrs.test.util.PropGirth;
import org.chocosolver.solver.sbcstrs.test.util.PropIncrementalGirth;
import org.chocosolver.solver.search.GraphStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.GraphVarFactory;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * @author Моклев Вячеслав
 */
public class MainDebug {
    public static void main(String[] args) {
        int n = 4;
        int m = 3;
        int l = 3;
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
        solver.post(GCF.nb_edges(graph, VF.fixed(m, solver)));
        solver.post(new Constraint("GirthConstraint", new PropIncrementalGirth(graph, VF.fixed(l, solver))));
        // add symmetry breaking constraint if necessary
        SBCF.postSymmetryBreaking(graph, solver);
        solver.set(GraphStrategyFactory.lexico(graph));
        System.out.println(solver.findSolution());
    }
}
