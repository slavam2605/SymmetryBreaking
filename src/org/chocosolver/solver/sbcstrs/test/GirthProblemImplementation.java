package org.chocosolver.solver.sbcstrs.test;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Моклев Вячеслав
 */
public class GirthProblemImplementation {
    public static final int n = 5;
    public static final int m = 5;

    // OEIS, A006856
    private static final int[] f4 = new int[] {0, 0, 1, 2, 3, 5, 6, 8, 10, 12, 15, 16, 18, 21, 23, 36, 28, 31};

    public static void main(String[] args) {
        Solver solver = new Solver();
        BoolVar[] A = new BoolVar[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j)
                    A[i + j * n] = VF.zero(solver);
                else
                    A[i + j * n] = VF.bool("A[" + i + "][" + j + "]", solver);
            }
        }
        // Problem constraints
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                solver.post(ICF.arithm(A[i + j * n], "=", A[j + i * n]));
            }
        }
        IntVar THREE = VF.fixed(3, solver);
        IntVar FOUR = VF.fixed(4, solver);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (i != j && j != k && i != k)
                        solver.post(ICF.sum(new BoolVar[]{A[i + j * n], A[j + k * n], A[k + i * n]}, "<", THREE));
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    for (int l = 0; l < n; l++) {
                        if (i != j && i != k && i != l && j != k && j != l && k != l)
                            solver.post(ICF.sum(new BoolVar[]{A[i + j * n], A[j + k * n], A[k + l * n], A[l + i * n]}, "<", FOUR));
                    }
                }
            }
        }
        BoolVar[] ALowTriangle = new BoolVar[n * (n - 1) / 2];
        int pos = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                ALowTriangle[pos++] = A[i + j * n];
            }
        }
        IntVar nbEdges = VF.fixed(m, solver);
        solver.post(ICF.sum(ALowTriangle, nbEdges));
        // Problem-specific ? and ? constraints
        IntVar ? = VF.integer("?", 0, n, solver);
        IntVar ? = VF.integer("?", 0, n, solver);



        solver.set(ISF.lexico_UB(A));

        System.out.println("Started...");
        PrintStream ps = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }));
        solver.findSolution();
        System.setOut(ps);
        Chatterbox.printStatistics(solver);
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (A[i + j * n].isInstantiated())
                    System.out.print(A[i + j * n].getBooleanValue() == ESat.TRUE ? 1 : 0);
                else
                    System.out.print('?');
            }
            System.out.println();
        }
    }
}
