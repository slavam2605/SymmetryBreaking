import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;
import sun.rmi.runtime.Log;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class Main2 {
    public static void main(String[] args) {
        Solver solver = new Solver("MY_SUPER_SOLVER");
        IntVar a = VF.bounded("A", 0, 100, solver);
        IntVar b = VF.bounded("B", 0, 100, solver);
        IntVar c = VF.bounded("C", 0, 100, solver);
        solver.post(ICF.scalar(new IntVar[] {a, b}, new int[] {1, 1}, c));
        solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, c);
        Chatterbox.printStatistics(solver);
        System.out.println(Arrays.toString(solver.getVars()));



//        int n = 5;
//        IntVar[] vars = new IntVar[n * n];
//        for (int i = 0; i < n; i++)
//            for (int j = 0; j < n; j++) {
//                vars[i * n + j] = VF.bounded("C" + i + "_" + j, 1, n * n, solver);
//            }
//        IntVar sum = VF.fixed("S", n * (n * n + 1) / 2, solver);
//        for (int i = 0; i < n * n; i++)
//            for (int j = 0; j < i; j++)
//                solver.post(ICF.arithm(vars[i], "!=", vars[j]));
//
//        int[] coeffs = new int[n];
//        for (int i = 0; i < n; i++) {
//            coeffs[i] = 1;
//        }
//
//        for (int i = 0; i < n; i++) {
//            IntVar[] col = new IntVar[n];
//            IntVar[] row = new IntVar[n];
//
//            for (int j = 0; j < n; j++) {
//                col[j] = vars[i * n + j];
//                row[j] = vars[j * n + i];
//            }
//
//            solver.post(ICF.scalar(row, coeffs, sum));
//            solver.post(ICF.scalar(col, coeffs, sum));
//        }
//        solver.findSolution();
//        System.out.println(Arrays.toString(solver.getVars()));

    }
}
