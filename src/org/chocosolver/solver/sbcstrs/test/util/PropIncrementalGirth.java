package org.chocosolver.solver.sbcstrs.test.util;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphEventType;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;
import org.chocosolver.util.procedure.PairProcedure;

import static java.lang.Math.*;

import java.util.HashSet;

/**
 * @author Моклев Вячеслав
 */
public class PropIncrementalGirth extends Propagator<Variable> {
    IUndirectedGraphVar graph;
    IGraphDeltaMonitor gdm;
    PairProcedure enforce;
    PairProcedure remove;
    int n;
    IntVar girth;
    int realUpperGirth;
    int[][] d;

    public PropIncrementalGirth(IUndirectedGraphVar graphVar, IntVar girth) {
        super(new Variable[] {graphVar, girth}, PropagatorPriority.QUADRATIC, true);
        graph = graphVar;
        gdm = graph.monitorDelta(this);
        enforce = (PairProcedure) (from, to) -> {
            // update girth bound
            realUpperGirth = min(d[from][to] + 1, realUpperGirth);
            System.out.println("UPPER_GIRTH: " + realUpperGirth + " (" + ((d[from][to] + 1) + ") :: " + from + " → " + to));
            if (girth.getUB() > realUpperGirth) {
                girth.updateUpperBound(realUpperGirth, this);
                System.out.println("UPD [" + girth.getLB() + ", " + girth.getUB() + "]");
            }
            if (graph.isInstantiated()) {
                girth.instantiateTo(realUpperGirth, this);
            }
            // update Floyd matrix
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    d[u][v] = min(d[u][v], min(d[u][from] + d[to][v] + 1, d[u][to] + d[from][v] + 1));
                }
            }
            // TODO remove print
            System.out.println("FLOYDEC: ");
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    System.out.print(d[u][v] + " ");
                }
                System.out.println();
            }
        };
        remove = (PairProcedure) (from, to) -> {
            if (graph.isInstantiated()) {
                System.out.println("MDA [" + girth.getLB() + ", " + girth.getUB() + "]");
                girth.instantiateTo(realUpperGirth, this);
            }
        };
        n = graphVar.getNbMaxNodes();
        this.girth = girth;
        realUpperGirth = n + 1;
        d = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = 1_000_000;
            }
            d[i][i] = 0;
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // initially set lower bound
        girth.updateLowerBound(3, this);
        // initial propagation
        int realUpperGirth = getUpperGraphGirth();
        if (realUpperGirth < girth.getUB()) {
            girth.updateUpperBound(realUpperGirth, aCause);
        }
        // init Floyd matrix
        initFloyd();
        // init incremental
        gdm.unfreeze();
    }

    private void initFloyd() {
        for (int u = 0; u < n; u++) {
            for (int v: new ItSet(graph.getMandNeighOf(u))) {
                d[u][v] = 1;
                d[v][u] = 1; // useless
            }
        }
        // find all lengths of shortest paths (Floyd algorithm)
        for (int k = 0; k < n; k++) {
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    d[u][v] = min(d[u][v], d[u][k] + d[k][v]);
                }
            }
        }
        // TODO remove print
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                System.out.print(d[u][v] + " ");
            }
            System.out.println();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        // incremental propagation
        System.out.println("KOKS&^&");
        if (idxVarInProp == 0) {
            gdm.freeze();
            gdm.forEachArc(enforce, GraphEventType.ADD_ARC);
            gdm.forEachArc(remove, GraphEventType.REMOVE_ARC);
            gdm.unfreeze();
        }
    }

    @Override
    protected int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return GraphEventType.ADD_ARC.getMask() | GraphEventType.REMOVE_ARC.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public ESat isEntailed() {
        if (realUpperGirth < girth.getLB()) {
            return ESat.FALSE;
        }
        if (graph.isInstantiated() && girth.isInstantiatedTo(realUpperGirth)) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private int getUpperGraphGirth() {
        int g = n + 1;
        for (int i = 0; i < n; i++) {
            int pg = getUpperGraphGirth(i);
            if (pg < g) {
                g = pg;
            }
        }
        return g;
    }

    private int getUpperGraphGirth(int vertex) {
        HashSet<Pair<Integer, Integer>> reachable = new HashSet<>();
        reachable.add(new Pair<>(vertex, -1));
        for (int i = 1; i <= n; i++) {
            HashSet<Pair<Integer, Integer>> set = new HashSet<>();
            for (Pair<Integer, Integer> u: reachable) {
                for (int v: new ItSet(graph.getMandNeighOf(u.getA()))) {
                    if (v != u.getB()) {
                        if (v == vertex) {
                            return i;
                        }
                        set.add(new Pair<>(v, u.getA()));
                    }
                }
            }
            reachable = set;
        }
        return n + 1;
    }
}
