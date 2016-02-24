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
package org.chocosolver.solver.constraints.nary;

/**
 * Created by IntelliJ IDEA.
 * User: Ashish
 * Date: Jun 26, 2008
 * Time: 1:31:37 PM
 * LexChain test file
 */

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

public class LexChainTest {

    @Test(groups = "10s")
    public void lexChainTest1() {
        Solver s = new Solver();

        IntVar[] ar1 = VariableFactory.boundedArray("v1", 3, 0, 10, s);
        IntVar[] ar2 = VariableFactory.boundedArray("v2", 3, -1, 9, s);

        Constraint c = ICF.lex_chain_less_eq(ar1, ar2);
        s.post(c);
        //SearchMonitorFactory.log(s, true, true);
        if (s.findSolution()) {
            do {
                Assert.assertEquals(ESat.TRUE, c.isSatisfied());
            } while (s.nextSolution());
        }

    }


    private ILogical reformulate(int i, IntVar[] X, IntVar[] Y, Solver solver) {
        BoolVar b1 = VariableFactory.bool("A" + i, solver);
        LogicalConstraintFactory.ifThenElse(b1, IntConstraintFactory.arithm(Y[i], ">", X[i]), IntConstraintFactory.arithm(Y[i], "<=", X[i]));
        if (i == X.length - 1) {
            return b1;
        } else {
            BoolVar b2 = VariableFactory.bool("B" + i, solver);
            LogicalConstraintFactory.ifThenElse(b2, IntConstraintFactory.arithm(Y[i], "=", X[i]), IntConstraintFactory.arithm(X[i], "!=", Y[i]));
            return LogOp.or(b1, LogOp.and(b2, reformulate(i + 1, X, Y, solver)));
        }
    }

    private Solver reformulate(int n, int m, int k, int seed, boolean bounded) {
        Solver solver = new Solver();
        IntVar[][] X = new IntVar[n][m];
        for (int i = 0; i < n; i++) {
            X[i] = bounded ?
                    VariableFactory.boundedArray("X_" + i, m, 0, k, solver) :
                    VariableFactory.enumeratedArray("X_" + i, m, 0, k, solver);
        }
        ILogical[] trees = new ILogical[n - 1];
        for (int i = 0; i < n - 1; i++) {
            trees[i] = reformulate(0, X[i], X[i + 1], solver);
            //refor.post(new SatConstraint(reformulate(0, X[i], X[i + 1], refor), refor));
        }

        SatFactory.addClauses(LogOp.and(trees), solver);
		if(bounded){
			solver.set(IntStrategyFactory.random_bound(ArrayUtils.flatten(X), seed));
		}else{
			solver.set(IntStrategyFactory.random_value(ArrayUtils.flatten(X), seed));
		}
        return solver;
    }

    private Solver lex(int n, int m, int k, int seed, boolean bounded) {
        Solver solver = new Solver();
        IntVar[][] X = new IntVar[n][m];
        for (int i = 0; i < n; i++) {
            X[i] = bounded ?
                    VariableFactory.boundedArray("X_" + i, m, 0, k, solver) :
                    VariableFactory.enumeratedArray("X_" + i, m, 0, k, solver);
        }
        solver.post(ICF.lex_chain_less(X));
		if(bounded){
			solver.set(IntStrategyFactory.random_bound(ArrayUtils.flatten(X), seed));
		}else{
			solver.set(IntStrategyFactory.random_value(ArrayUtils.flatten(X), seed));
		}
        return solver;
    }

    @Test(groups = "10s")
    public void testE() {
        Random random = new Random();
        for (int seed = 0; seed < 1000; seed++) {
            random.setSeed(seed);
            int n = 2 + random.nextInt(2);
            int m = 2 + random.nextInt(2);
            int k = 1 + random.nextInt(2);

            Solver refor = reformulate(n, m, k, seed, false);
            Solver lex = lex(n, m, k, seed, false);

            refor.findAllSolutions();
            lex.findAllSolutions();

            Assert.assertEquals(refor.getMeasures().getSolutionCount(), lex.getMeasures().getSolutionCount(), String.format("seed:%d", seed));
        }
    }

    @Test(groups = "10s")
    public void testB() {
        Random random = new Random();
        for (int seed = 0; seed < 1000; seed++) {
            random.setSeed(seed);
            int n = 2 + random.nextInt(2);
            int m = 2 + random.nextInt(2);
            int k = 1 + random.nextInt(2);

            Solver refor = reformulate(n, m, k, seed, true);
            Solver lex = lex(n, m, k, seed, true);

            refor.findAllSolutions();
            lex.findAllSolutions();

            Assert.assertEquals(refor.getMeasures().getSolutionCount(), lex.getMeasures().getSolutionCount(), String.format("seed:%d", seed));
        }
    }

    @Test(groups = "1s")
    public void testB1() {
        int n = 3, m = 2, k = 2, seed = 47;
        Solver refor = reformulate(n, m, k, seed, true);
        Solver lex = lex(n, m, k, seed, true);
        refor.findAllSolutions();
        lex.findAllSolutions();
        Assert.assertEquals(refor.getMeasures().getSolutionCount(), lex.getMeasures().getSolutionCount(), String.format("seed:%d", seed));
    }

    @Test(groups = "1s")
    public void testB2() {
        Solver solver = new Solver();
        IntVar[][] X = new IntVar[3][2];
        for (int i = 0; i < 3; i++) {
            X[i] = VariableFactory.boundedArray("X_" + i, 2, 0, 2, solver);
        }

        solver.post(ICF.lex_chain_less(X));


        try {
            solver.propagate();
            X[0][0].updateLowerBound(1, Cause.Null);
            X[0][1].updateLowerBound(1, Cause.Null);
            X[1][0].updateLowerBound(1, Cause.Null);
            X[2][1].updateLowerBound(1, Cause.Null);
            solver.propagate();
            X[2][1].instantiateTo(1, Cause.Null);
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

}


