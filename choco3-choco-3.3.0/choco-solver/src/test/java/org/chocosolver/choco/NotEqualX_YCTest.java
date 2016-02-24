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
package org.chocosolver.choco;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User: cprudhom
 * Mail: cprudhom(a)emn.fr
 * Date: 15 juin 2010
 * Since: Choco 2.1.1
 */
public class NotEqualX_YCTest {
    @Test(groups = "1s")
    public void test1() {
        int n = 2;

        Solver s = new Solver();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, 0, n, s);
        }
        s.post(IntConstraintFactory.arithm(vars[0], "!=", vars[1]));

        s.set(IntStrategyFactory.lexico_LB(vars));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 6, "nb sol incorrect");

    }

    @Test(groups = "1s")
    public void test2() {
        int n = 2;

        Solver s = new Solver();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.bounded("v_" + i, 0, n, s);
        }
        s.post(IntConstraintFactory.arithm(vars[0], "!=", vars[1]));
        s.set(IntStrategyFactory.lexico_LB(vars));
//        ChocoLogging.toSolution();
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 6, "nb sol incorrect");
    }

    @Test(groups = "1s")
    public void test3() {
        int n = 2;

        Solver s = new Solver();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.bounded("v_" + i, 0, n, s);
        }
        s.post(IntConstraintFactory.arithm(vars[0], "!=", vars[1]));
        s.set(IntStrategyFactory.lexico_LB(vars));

        try {
            s.propagate();
            vars[0].instantiateTo(1, Cause.Null);
            s.propagate();
            Assert.assertEquals(vars[1].getLB(), 0);
            Assert.assertEquals(vars[1].getUB(), 2);
            vars[1].removeValue(2, Cause.Null);
            s.propagate();
            Assert.assertEquals(vars[1].getLB(), 0);
            Assert.assertEquals(vars[1].getUB(), 0);
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

}
