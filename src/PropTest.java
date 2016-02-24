import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IDirectedGraphVar;
import org.chocosolver.util.ESat;

/**
 * @author Моклев Вячеслав
 */
public class PropTest extends Propagator<IDirectedGraphVar> {

    IDirectedGraphVar graph;
    int n;

    protected PropTest(IDirectedGraphVar graph) {
        super(new IDirectedGraphVar[] {graph}, PropagatorPriority.LINEAR, false);
        this.graph = graph;
        n = graph.getNbMaxNodes();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {

    }

    @Override
    public ESat isEntailed() {
        return null;
    }
}
