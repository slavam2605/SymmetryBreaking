/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.choco.propagation.thread;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.thread.ThreadSolver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 6 oct. 2010
 */
public class ThreadSolverTest {

    protected Solver modeler(int n) {
        Solver solver = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, 1, n, solver);
        }


        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = IntConstraintFactory.arithm(vars[i], "!=", vars[j]);
                solver.post(neq);
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", -k));
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", k));
            }
        }
        return solver;
    }


    @Test(groups = "1s")
    public void test1() throws InterruptedException {
        int n = 12;

        Solver sref = modeler(n);
        sref.findAllSolutions();

        int n1 = n / 2;

        Solver sm1 = modeler(n);
        sm1.post(IntConstraintFactory.arithm((IntVar) sm1.getVars()[0], "<=", n1));

        Solver sm2 = modeler(n);
        sm2.post(IntConstraintFactory.arithm((IntVar) sm2.getVars()[0], ">=", n1 + 1));

        ThreadSolver ts1 = new ThreadSolver(sm1);
        ThreadSolver ts2 = new ThreadSolver(sm2);
//

        ts1.findAllSolutions();
        ts2.findAllSolutions();

        ts1.join();
        ts2.join();

        int nbSol = (int) sref.getMeasures().getSolutionCount();
        Assert.assertEquals(ts1.solver.getMeasures().getSolutionCount()
                + ts2.solver.getMeasures().getSolutionCount(), nbSol);
    }

    @Test(groups = "1s")
    public void test2() throws InterruptedException {
        int n = 10;

        ThreadSolver[] solvers = new ThreadSolver[n];
        for (int i = 0; i < n; i++) {
            solvers[i] = new ThreadSolver(modeler(n));
        }
        for (int i = 0; i < n; i++) {
            solvers[i].findAllSolutions();
        }
        for (int i = 0; i < n; i++) {
            solvers[i].join();
        }
        for (int i = 1; i < n; i++) {
            Assert.assertEquals(solvers[i].solver.getMeasures().getSolutionCount(), solvers[0].solver.getMeasures().getSolutionCount());
            Assert.assertEquals(solvers[i].solver.getMeasures().getNodeCount(), solvers[0].solver.getMeasures().getNodeCount());
        }
    }

}
