import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphEventType;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;

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
        ItSet nodes = new ItSet(graph.getPotentialNodes());
        ItSet succ;
        for (int i: nodes) {
            succ = new ItSet(graph.getPotNeighOf(i));
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
        ItSet nodes = new ItSet(graph.getMandatoryNodes());
        ItSet succ;
        for (int i: nodes) {
            succ = new ItSet(graph.getMandNeighOf(i));
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
