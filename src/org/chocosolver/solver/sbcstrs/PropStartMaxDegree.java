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
        degrees = new IntVar[n - 1];
        for (int i = 0; i < n - 1; i++) {
            degrees[i] = VF.integer("degrees[" + (i + 1) + "]", graph.getMandNeighOf(i + 1).getSize(), graph.getPotNeighOf(i + 1).getSize(), solver);
        }
    }



    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int degStartUpperBound = graph.getPotNeighOf(0).getSize();
        for (int i = 1; i < n; i++) {
            int lowerBound = graph.getMandNeighOf(i).getSize();
            int upperBound = graph.getPotNeighOf(i).getSize();
            if (degrees[i - 1].getLB() != lowerBound) {
                degrees[i - 1].updateLowerBound(lowerBound, this);
            }
            if (degrees[i - 1].getUB() != upperBound) {
                degrees[i - 1].updateUpperBound(upperBound, this);
            }
            if (degrees[i - 1].getUB() > degStartUpperBound) {
                degrees[i - 1].updateUpperBound(degStartUpperBound, this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int degStartUpperBound = graph.getPotNeighOf(0).getSize();
        for (int i = 1; i < n; i++) {
            if (degrees[i - 1].getLB() > degStartUpperBound) {
                return ESat.FALSE;
            }
        }
        if (graph.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
