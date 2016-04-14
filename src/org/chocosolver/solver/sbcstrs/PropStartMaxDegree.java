package org.chocosolver.solver.sbcstrs;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * @author Моклев Вячеслав
 */
public class PropStartMaxDegree extends Propagator<IUndirectedGraphVar> {
    private IUndirectedGraphVar graph;
    private int n;
    private IntVar[] degrees;

    public PropStartMaxDegree(IUndirectedGraphVar graph, Solver solver) {
        super(new IUndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, false);
        this.graph = graph;
        n = graph.getNbMaxNodes();
        degrees = new IntVar[n];
        for (int i = 0; i < n; i++) {
            degrees[i] = VF.integer("degrees[" + i + "]", graph.getMandNeighOf(i).getSize(), graph.getPotNeighOf(i).getSize(), solver);
        }
    }



    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (degrees[0].getLB() != graph.getMandNeighOf(0).getSize()) {
            degrees[0].updateLowerBound(graph.getMandNeighOf(0).getSize(), this);
        }
        if (degrees[0].getUB() != graph.getPotNeighOf(0).getSize()) {
            degrees[0].updateUpperBound(graph.getPotNeighOf(0).getSize(), this);
        }
        int degStartUpperBound = degrees[0].getUB();
        for (int i = 1; i < n; i++) {
            int lowerBound = graph.getMandNeighOf(i).getSize();
            int upperBound = graph.getPotNeighOf(i).getSize();
            if (degrees[i].getLB() != lowerBound) {
                degrees[i].updateLowerBound(lowerBound, this);
            }
            if (degrees[i].getUB() != upperBound) {
                degrees[i].updateUpperBound(upperBound, this);
            }
            if (degrees[i].getUB() > degStartUpperBound) {
                degrees[i].updateUpperBound(degStartUpperBound, this);
            }
            if (degrees[0].getLB() < degrees[i].getLB()) {
                degrees[0].updateLowerBound(degrees[i].getLB(), this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 1; i < n; i++) {
            if (degrees[i].getLB() > degrees[0].getUB()) {
                return ESat.FALSE;
            }
        }
        if (graph.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
