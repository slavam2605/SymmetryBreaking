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
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/02/13
 */
public class BoolNotViewTest {

    @Test(groups = "1s")
    public void test1() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            Solver ref = new Solver();
            {
                BoolVar[] xs = new BoolVar[2];
                xs[0] = VariableFactory.bool("x", ref);
                xs[1] = VariableFactory.bool("y", ref);
                ref.post(IntConstraintFactory.sum(xs, VariableFactory.fixed(1, ref)));
                ref.set(IntStrategyFactory.random_bound(xs, seed));
            }
            Solver solver = new Solver();
            {
                BoolVar[] xs = new BoolVar[2];
                xs[0] = VariableFactory.bool("x", solver);
                xs[1] = VariableFactory.not(xs[0]);
                solver.post(IntConstraintFactory.sum(xs, VariableFactory.fixed(1, solver)));
                solver.set(IntStrategyFactory.random_bound(xs, seed));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());

        }
    }

    @Test(groups = "1s")
    public void testIt() {
        Solver ref = new Solver();
        BoolVar o = VariableFactory.bool("b", ref);
        BoolVar v = VariableFactory.not(o);
        DisposableValueIterator vit = v.getValueIterator(true);
        while (vit.hasNext()) {
            Assert.assertTrue(o.contains(vit.next()));
        }
        vit.dispose();
        vit = v.getValueIterator(false);
        while (vit.hasNext()) {
            Assert.assertTrue(o.contains(vit.next()));
        }
        vit.dispose();
        DisposableRangeIterator rit = v.getRangeIterator(true);
        while (rit.hasNext()) {
            rit.next();
            Assert.assertTrue(o.contains(rit.min()));
            Assert.assertTrue(o.contains(rit.max()));
        }
        rit = v.getRangeIterator(false);
        while (rit.hasNext()) {
            rit.next();
            Assert.assertTrue(o.contains(rit.min()));
            Assert.assertTrue(o.contains(rit.max()));
        }
    }

    @Test(groups = "1s")
    public void testPrevNext() {
        Solver solver = new Solver();
        BoolVar a = VF.bool("a", solver);
        BoolVar b = VF.bool("b", solver);
        solver.post(ICF.arithm(a, "+", VF.not(b), "=", 2));
        Assert.assertTrue(solver.findSolution());
    }
}
