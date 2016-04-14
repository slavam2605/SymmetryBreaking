package org.chocosolver.solver.sbcstrs.test;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Моклев Вячеслав
 */
public class PropBFS2 extends Propagator<IUndirectedGraphVar> {
    IUndirectedGraphVar graph;

    public PropBFS2(IUndirectedGraphVar graph) {
        super(new IUndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, false);
        this.graph = graph;
    }

    private enum BFS {ENUMERATED, NOT_CONNECTED, NOT_ENUMERATED}

    private BFS bfsEnumerated() {
        Queue<Integer> Q = new ArrayDeque<>();
        int n = graph.getNbMaxNodes();
        boolean[] used = new boolean[n];
        Q.add(0);
        used[0] = true;
        int count = -1;
        BFS result = BFS.ENUMERATED;
        while (!Q.isEmpty()) {
            int u = Q.poll();
            count++;
            if (u != count) {
                result = BFS.NOT_ENUMERATED;
            }
            for (int v: new ItSet(graph.getMandNeighOf(u))) {
                if (!used[v]) {
                    Q.add(v);
                    used[v] = true;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (!used[i]) {
                return BFS.NOT_CONNECTED;
            }
        }
        return result;
    }

    public BFS entailed() {
        return bfsEnumerated();
    }

    @Override
    public void propagate(int i) throws ContradictionException {
        if (graph.isInstantiated()) {
            if (entailed() != BFS.ENUMERATED) {
                throw new ContradictionException();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (graph.isInstantiated()) {
            return entailed() == BFS.ENUMERATED ? ESat.TRUE : ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    public static void main(String[] args) {
        Solver solver = new Solver();
        UndirectedGraph gg = new UndirectedGraph(3, SetType.BITSET, true);
        gg.addEdge(0, 1);
        gg.addEdge(0, 2);
        gg.addEdge(1, 2);
        IUndirectedGraphVar g = new UndirectedGraphVar("lel", solver, gg, gg);
        PropBFS2 pb = new PropBFS2(g);
        System.out.println(pb.entailed());
    }
}
