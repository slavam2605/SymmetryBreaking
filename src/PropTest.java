import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;
import org.chocosolver.util.procedure.PairProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Моклев Вячеслав
 */
public class PropTest extends Propagator<Variable> {
    IUndirectedGraphVar graph;
    IGraphDeltaMonitor gdm;
    PairProcedure enforce;
    PairProcedure remove;
    int n;
    BoolVar[] t;

    public PropTest(IUndirectedGraphVar graphVar, BoolVar[] t) {
        super(ArrayUtils.append(new Variable[]{graphVar}, t), PropagatorPriority.LINEAR, true);
        graph = graphVar;
        gdm = graph.monitorDelta(this);
        enforce = (PairProcedure) (from, to) -> {
            t[from + to * n].instantiateTo(1, this);
            t[to + from * n].instantiateTo(1, this);
            printT();
        };
        remove = (PairProcedure) (from, to) -> {
            t[from + to * n].instantiateTo(0, this);
            t[to + from * n].instantiateTo(0, this);
            printT();
        };
        n = graphVar.getNbMaxNodes();
        this.t = t;
    }

    private void printT() {
        System.out.println("MyOwnUpper:");
        for (int i = 0; i < n; i++) {
            System.out.print(i + " ⇒ [");
            for (int j = 0; j < n; j++) {
                if (!t[i + j * n].isInstantiatedTo(0)) {
                    System.out.print(j + ", ");
                }
            }
            System.out.println("]");
        }
        System.out.println("MyOwnLower:");
        for (int i = 0; i < n; i++) {
            System.out.print(i + " ⇒ [");
            for (int j = 0; j < n; j++) {
                if (t[i + j * n].isInstantiatedTo(1)) {
                    System.out.print(j + ", ");
                }
            }
            System.out.println("]");
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // first, nonincremental propagation
        for (int i = 0; i < n; i++) {
            t[i + i * n].instantiateTo(0, this);
        }
        propagateGraphChanged();
        propagateTChanged();
        // initializing incremental data-structures
        gdm.unfreeze();
    }

    private void propagateGraphChanged() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (t[i + j * n].isInstantiatedTo(1)) {
                    graph.enforceArc(i, j, aCause);
                }
                if (t[i + j * n].isInstantiatedTo(0)) {
                    graph.removeArc(i, j, aCause);
                }
            }
        }
    }

    private void propagateTChanged() throws ContradictionException {
        for (int u = 0; u < n; u++) {
            for (int v: new ItSet(graph.getMandNeighOf(u))) {
                t[u + v * n].instantiateTo(1, this);
            }
        }
        for (int u = 0; u < n; u++) {
            Set<Integer> set = new HashSet<>();
            for (int v: new ItSet(graph.getPotNeighOf(u))) {
                set.add(v);
            }
            for (int v = 0; v < n; v++) {
                if (!set.contains(v)) {
                    t[u + v * n].instantiateTo(0, this);
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        // incremental propagation
        if (idxVarInProp == 0) {
            gdm.freeze();
            gdm.forEachArc(enforce, GraphEventType.ADD_ARC);
            gdm.forEachArc(remove, GraphEventType.REMOVE_ARC);
            gdm.unfreeze();
        } else {
            propagateTChanged();
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
        System.out.println(graph);
        System.out.println(Arrays.toString(t));
        for (int i = 0; i < n; i++) {
            ISet children = graph.getMandNeighOf(i);
            for (int j = 0; j < n; j++) {
                if ((t[i + j * n].isInstantiatedTo(0) || t[j + i * n].isInstantiatedTo(0)) && children.contain(j)) {
                    return ESat.FALSE;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            ISet children = graph.getPotNeighOf(i);
            for (int j = 0; j < n; j++) {
                if ((t[i + j * n].isInstantiatedTo(1) || t[j + i * n].isInstantiatedTo(1)) && !children.contain(j)) {
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
