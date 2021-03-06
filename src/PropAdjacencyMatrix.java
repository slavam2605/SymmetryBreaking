import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IDirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * @author Моклев Вячеслав
 */
public class PropAdjacencyMatrix extends Propagator<Variable> {
    IDirectedGraphVar graph;
    int n;
    IntVar[] t;

    public PropAdjacencyMatrix(IDirectedGraphVar graphVar, IntVar[] t) {
        super(ArrayUtils.append(t, new Variable[]{graphVar}), PropagatorPriority.LINEAR, false);
        graph = graphVar;
        n = graphVar.getNbMaxNodes();
        this.t = t;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (t[i + j * n].isInstantiatedTo(1)) {
                    graph.enforceArc(i, j, aCause);
                }
                if (t[i + j * n].isInstantiatedTo(0)) {
                    graph.removeArc(i, j, aCause);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            ISet children = graph.getMandSuccOf(i);
            for (int j = 0; j < n; j++) {
                if (t[i + j * n].isInstantiatedTo(0) && children.contain(j)) {
                    return ESat.FALSE;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            ISet children = graph.getPotSuccOf(i);
            for (int j = 0; j < n; j++) {
                if (t[i + j * n].isInstantiatedTo(1) && !children.contain(j)) {
                    return ESat.FALSE;
                }
            }
        }
        if (graph.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
