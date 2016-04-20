package org.chocosolver.solver.sbcstrs.test;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.sbcstrs.SBCF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterableSet.ItSet;
import org.chocosolver.util.tools.ArrayUtils;

import java.io.*;

/**
 * @author Моклев Вячеслав
 */
public class GirthProblemImplementation {
    // OEIS, A006856
    private static final int[] f4 = new int[] {0, 0, 1, 2, 3, 5, 6, 8, 10, 12, 15, 16, 18, 21, 23, 36, 28, 31};

    public static final int n = 12;
    public static final int m = f4[n];

    // SAT:
    // 11 -- 9s
    // 12 -- 61s → 49s → 47s
    public static final PrintStream DUMMY_OUT = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
    });
    public static final PrintStream OUT = System.out;

    public static void main(String[] args) throws FileNotFoundException {
        Solver solver = new Solver();
        BoolVar[][] A = new BoolVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j)
                    A[i][j] = VF.zero(solver);
                else {
                    if (i < j)
                        A[i][j] = VF.bool("A[" + i + "][" + j + "]", solver);
                    else
                        A[i][j] = A[j][i];
                }

            }
        }
        // Problem constraints
        IntVar TWO = VF.fixed(2, solver);
        BoolVar[][] pairX = VF.boolMatrix("pairX", n, n, solver);
        BoolVar[][][] tripleX = new BoolVar[n][n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    tripleX[i][j][k] = VF.bool("tripleX[" + i + "][" + j + "][" + k +"]", solver);
                    solver.post(ICF.arithm(tripleX[i][j][k], "=", LCF.and(A[i][j], A[j][k]).reif()));
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (i == k) {
                    solver.post(ICF.arithm(pairX[i][k], "=", VF.zero(solver)));
                } else {
                    BoolVar[] temp = new BoolVar[n - 2];
                    int pos = 0;
                    for (int j = 0; j < n; j++) {
                        if (j != i && j != k) {
                            temp[pos++] = tripleX[i][j][k];
                        }
                    }
                    solver.post(ICF.arithm(pairX[i][k], "=", LCF.or(temp).reif()));
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (i != k) {
                    IntVar sum = VF.integer("sum_" + i + "_" + k, 0, 2, solver);
                    solver.post(ICF.sum(new BoolVar[]{A[i][k], pairX[i][k]}, sum));
                    solver.post(ICF.arithm(sum, "<", TWO));
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (i != k) {
                    BoolVar[] sliceTripleX = new BoolVar[n];
                    for (int j = 0; j < n; j++) {
                        sliceTripleX[j] = tripleX[i][j][k];
                    }
                    IntVar sum = VF.integer("sum2_" + i + "_ " + k, 0, n, solver);
                    solver.post(ICF.sum(sliceTripleX, sum));
                    solver.post(ICF.arithm(sum, "<", TWO));
                }
            }
        }
        BoolVar[] ALowTriangle = new BoolVar[n * (n - 1) / 2];
        int pos = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                ALowTriangle[pos++] = A[i][j];
            }
        }
        IntVar nbEdges = VF.fixed(m, solver);
        solver.post(ICF.sum(ALowTriangle, nbEdges));
        // Definition of δ and Δ
        IntVar Δ = VF.integer("Δ", 1, n, solver);
        IntVar δ = VF.integer("δ", 1, n, solver);
        IntVar[] deg = VF.integerArray("deg", n, 0, n, solver);
        for (int i = 0; i < n; i++) {
            solver.post(ICF.sum(A[i], deg[i]));
            solver.post(ICF.arithm(deg[i], ">=", δ));
            solver.post(ICF.arithm(deg[i], "<=", Δ));
        }
        solver.post(ICF.minimum(δ, deg));
        solver.post(ICF.maximum(Δ, deg));
        // Problem-specific δ and Δ constraints
        IntVar justTimes = VF.integer("Δδ", 0, n, solver);
        solver.post(ICF.times(Δ, δ, justTimes));
        solver.post(ICF.member(justTimes, 1, n - 1));
        solver.post(ICF.arithm(δ, "<=", Δ));
        solver.post(ICF.arithm(δ, ">=", m - f4[n - 1]));
        solver.post(ICF.arithm(Δ, ">=", ceilDiv(2 * m, n)));

        SBCF.postSymmetryBreaking1T2(solver, n, A);

        solver.set(ISF.lexico_LB(ArrayUtils.append(ALowTriangle, new IntVar[] {δ, Δ})));

        System.out.println("Started...");
        System.setOut(DUMMY_OUT);
        performSolutions(solver);
        System.setOut(OUT);
        Chatterbox.printStatistics(solver);
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (A[i][j].isInstantiated())
                    System.out.print(A[i][j].getBooleanValue() == ESat.TRUE ? 1 : 0);
                else
                    System.out.print('?');
            }
            System.out.println();
        }
        PrintWriter out = new PrintWriter("C:\\Users\\Home\\Downloads\\graphs\\out.graph");
        out.println("graph {");
        out.print("    {\n        node [shape=circle]\n        ");
        for (int i = 0; i < n; i++) {
            out.print(i + " ");
        }
        out.println("\n    }");
        for (int i = 0; i < n; i++) {
            for (int v = i + 1; v < n; v++) {
                if (A[i][v].getBooleanValue() == ESat.TRUE) {
                    out.println("    " + i + " -- " + v);
                }
            }
        }
        out.println("}");
        out.close();
    }

    private static final boolean ONCE = true;

    private static void performSolutions(Solver solver) {
        solver.findSolution();
        if (solver.isFeasible() == ESat.TRUE) {
            do {
                System.setOut(OUT);
                reportSolution(solver);
                System.setOut(DUMMY_OUT);
                if (ONCE) return;
            } while (solver.nextSolution());
        }
    }

    private static void reportSolution(Solver solver) {
        Integer δ = null, Δ = null;
        for (int i = 0; i < solver.getNbVars(); i++) {
            if (solver.getVar(i).getName().equals("δ")) {
                IntVar delta = ((IntVar) solver.getVar(i));
                if (!delta.isInstantiated())
                    δ = null;
                else
                    δ = delta.getLB();
            }
            if (solver.getVar(i).getName().equals("Δ")) {
                IntVar Delta = ((IntVar) solver.getVar(i));
                if (!Delta.isInstantiated())
                    Δ = null;
                else
                    Δ = Delta.getLB();
            }
        }
        System.out.println("(" + (Δ == null ? "?" : "" + Δ) + ", " + (δ == null ? "?" : "" + δ) + ")");
    }

    private static int ceilDiv(int a, int b) {
        if (a % b == 0)
            return a / b;
        else
            return a / b + 1;
    }
}
