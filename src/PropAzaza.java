import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphEventType;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.util.ESat;

/**
 * @author Моклев Вячеслав
 */
public class PropAzaza extends Propagator<IUndirectedGraphVar> {

    IUndirectedGraphVar graph;
    int n;

    protected PropAzaza(IUndirectedGraphVar var) {
        super(var);
        graph = var;
        n = graph.getNbMaxNodes();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        MySet nodes = new MySet(graph.getPotentialNodes());
        MySet succ;
        for (int i: nodes) {
            succ = new MySet(graph.getPotNeighOf(i));
            for (int j: succ) {
                if (i == 0 && j == 1) {
                    graph.removeArc(i, j, aCause);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        System.out.println("WUUUUUUUUUUUUT");
        MySet nodes = new MySet(graph.getMandatoryNodes());
        MySet succ;
        for (int i: nodes) {
            succ = new MySet(graph.getMandNeighOf(i));
            for (int j: succ) {
                if (i == 0 && j == 1) {
                    return ESat.FALSE;
                }
            }
        }
        if (graph.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    protected int getPropagationConditions(int vIdx) {
        return GraphEventType.ADD_ARC.getMask();
    }
}
