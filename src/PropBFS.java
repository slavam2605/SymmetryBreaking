import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphEventType;
import org.chocosolver.solver.variables.IDirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.*;

/**
 * @author Моклев Вячеслав
 */
public class PropBFS extends Propagator<IDirectedGraphVar> {

    IDirectedGraphVar graph;
    int n;

    protected PropBFS(IDirectedGraphVar var) {
        super(new IDirectedGraphVar[]{var}, PropagatorPriority.LINEAR, false);
        graph = var;
        n = graph.getNbMaxNodes();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        MySet nodes = new MySet(graph.getPotentialNodes());
        for (int node: nodes) {
            MySet succs = new MySet(graph.getPotSuccOf(node));
            for (int succ: succs) {
                if (succ <= node) {
                    graph.removeArc(node, succ, aCause);
                }
            }
        }
    }

    /**
     * Checks if <code>graph</code> is bfs-enumerated
     * @return true, iff exists bfs-tree with root in vertex 0
     */
    private boolean bfsEnumerated1() {
        for (int v = 0; v < n; v++) {
            System.out.print(">>> " + v + " -> ");
            MySet arcs = new MySet(graph.getMandSuccOf(v));
            SortedSet<Integer> sortedArcs = new TreeSet<>();
            for (int u: arcs) {
                sortedArcs.add(u);
            }
            for (int u: sortedArcs) {
                System.out.print(u + ", ");
            }
            System.out.println();
        }
        // ------------------------------------------
        Queue<Integer> q = new ArrayDeque<>();
        boolean[] used = new boolean[n];
        int id = 0;
        q.add(0);
        used[0] = true;
        while (!q.isEmpty()) {
            int v = q.poll();
            MySet arcs = new MySet(graph.getMandSuccOf(v));
            SortedSet<Integer> sortedArcs = new TreeSet<>();
            for (int u: arcs) {
                sortedArcs.add(u);
            }
            for (int u: sortedArcs) {
                if (!used[u]) {
                    if (++id != u) {
                        return false;
                    }
                    q.add(u);
                    used[u] = true;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (!used[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean bfsEnumerated() {
        boolean result = bfsEnumerated1();
        System.out.println(">>> " + result);
        return result;
    }

    @Override
    public ESat isEntailed() {
        MySet nodes = new MySet(graph.getMandatoryNodes());
        for (int node: nodes) {
            MySet succs = new MySet(graph.getMandSuccOf(node));
            for (int succ: succs) {
                if (succ <= node) {
                    return ESat.FALSE;
                }
            }
        }
//        if (!bfsEnumerated()) {
//            return ESat.FALSE;
//        }
        if (graph.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    protected int getPropagationConditions(int vIdx) {
        return GraphEventType.ADD_ARC.getMask() + GraphEventType.REMOVE_ARC.getMask();
    }
}
