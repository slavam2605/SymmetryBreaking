package org.chocosolver.solver.sbcstrs.test.util;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.sbcstrs.test.util.Pair;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;

import java.util.HashSet;

/**
 * @author Моклев Вячеслав
 */
public class PropGirth extends Propagator<Variable> {
    IUndirectedGraphVar graph;
    int n;
    IntVar girth;

    public PropGirth(IUndirectedGraphVar graphVar, IntVar girth) {
        super(new Variable[] {girth, graphVar}, PropagatorPriority.LINEAR, false);
        graph = graphVar;
        n = graphVar.getNbMaxNodes();
        this.girth = girth;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int upperGraphGirth = getUpperGraphGirth();
        int lowerGraphGirth = getLowerGraphGirth();
        System.out.println("$org.chocosolver.solver.sbcstrs.test.util.PropGirth::propagate$> " + lowerGraphGirth + ", " + upperGraphGirth);
        if (upperGraphGirth < girth.getUB()) {
            girth.updateUpperBound(upperGraphGirth, aCause);
        }
        if (upperGraphGirth < girth.getLB()) {
            throw new ContradictionException();
        }
        if (lowerGraphGirth > girth.getLB()) {
            girth.updateLowerBound(lowerGraphGirth, aCause);
        }
        if (lowerGraphGirth > girth.getUB()) {
            throw new ContradictionException();
        }
    }

    @Override
    public ESat isEntailed() {
        int upperGraphGirth = getUpperGraphGirth();
        int lowerGraphGirth = getLowerGraphGirth();
        if (upperGraphGirth < girth.getLB()) {
            return ESat.FALSE;
        }
        if (lowerGraphGirth > girth.getUB()) {
            return ESat.FALSE;
        }
        if (graph.isInstantiated() && girth.isInstantiatedTo(lowerGraphGirth)) {
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

    private int getLowerGraphGirth() {
        int g = n + 1;
        for (int i = 0; i < n; i++) {
            int pg = getLowerGraphGirth(i);
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

    private int getLowerGraphGirth(int vertex) {
        HashSet<Pair<Integer, Integer>> reachable = new HashSet<>();
        reachable.add(new Pair<>(vertex, -1));
        for (int i = 1; i <= n; i++) {
            HashSet<Pair<Integer, Integer>> set = new HashSet<>();
            for (Pair<Integer, Integer> u: reachable) {
                for (int v: new ItSet(graph.getPotNeighOf(u.getA()))) {
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
