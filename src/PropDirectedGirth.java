import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IDirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;

import static java.lang.Math.*;

/**
 * @author Моклев Вячеслав
 */
public class PropDirectedGirth extends Propagator<Variable> {
    IDirectedGraphVar graph;
    int n;
    IntVar girth;
    int[][] w;

    private static final int INF = 1_000_000_000;

    public PropDirectedGirth(IDirectedGraphVar graphVar, IntVar girth) {
        super(new Variable[] {girth, graphVar}, PropagatorPriority.LINEAR, false);
        graph = graphVar;
        n = graphVar.getNbMaxNodes();
        this.girth = girth;
        initFloyd();
    }

    private void initFloyd() {
        w = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                w[i][j] = INF;
            }
            w[i][i] = 0;
        }
        for (int u = 0; u < n; u++) {
            for (int v: new ItSet(graph.getMandSuccOf(u))) {
                w[u][v] = 1;
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    w[i][j] = min(w[i][j], w[i][k] + w[k][j]);
                }
            }
        }
    }

    private void updateFloyd() {
        // TODO научиться в инкрементальные propagator'ы и сделать обновление w:
        // for u:
        //     for v:
        //         w[u][v] = min(w[u][v], w[u][a] + 1 + w[b][v]);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        updateFloyd();
        int upperGraphGirth = getUpperGraphGirth();
        int lowerGraphGirth = getLowerGraphGirth();
        System.out.println("$PropGirth::propagate$> " + lowerGraphGirth + ", " + upperGraphGirth);
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
        return n + 1;
    }

    private int getLowerGraphGirth() {
        return 3;
    }

}
