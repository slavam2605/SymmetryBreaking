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


import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class SatTest {

    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver();
        BoolVar b1, b2;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        SatFactory.addBoolEq(b1, b2);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver();
        BoolVar b1, b2;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        SatFactory.addBoolNot(b1, b2);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver();
        BoolVar b1, b2;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        SatFactory.addBoolLe(b1, b2);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 3);
    }


    @Test(groups = "1s")
    public void test4() {
        Solver solver = new Solver();
        BoolVar b1, b2, r;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        r = VF.bool("r", solver);
        SatFactory.addBoolIsEqVar(b1, b2, r);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void test5() {
        Solver solver = new Solver();
        BoolVar b1, b2, r;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        r = VF.bool("r", solver);
        SatFactory.addBoolAndEqVar(b1, b2, r);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void test6() {
        Solver solver = new Solver();
        BoolVar b1, b2, r;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        r = VF.bool("r", solver);
        SatFactory.addBoolOrEqVar(b1, b2, r);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void test7() {
        Solver solver = new Solver();
        BoolVar b1, b2;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        SatFactory.addBoolLt(b1, b2);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test8() {
        Solver solver = new Solver();
        BoolVar b1, b2, r;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        r = VF.bool("r", solver);
        SatFactory.addBoolIsLeVar(b1, b2, r);
//        SMF.log(solver, true, true);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void test9() {
        Solver solver = new Solver();
        BoolVar b1, b2, r;
        b1 = VF.bool("b1", solver);
        b2 = VF.bool("b2", solver);
        r = VF.bool("r", solver);
        SatFactory.addBoolIsLtVar(b1, b2, r);
//        SMF.log(solver, true, true);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }


    @Test(groups = "1s")
    public void test10() {
        Solver solver = new Solver();
        BoolVar b1;
        b1 = VF.bool("b1", solver);
        SatFactory.addTrue(b1);
        //        SMF.log(solver, true, true);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(b1.getBooleanValue(), ESat.TRUE);
    }

    @Test(groups = "1s")
    public void test11() {
        Solver solver = new Solver();
        BoolVar b1;
        b1 = VF.bool("b1", solver);
        SatFactory.addFalse(b1);
        //        SMF.log(solver, true, true);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(b1.getBooleanValue(), ESat.FALSE);
    }

    @Test(groups = "1s")
    public void testAlexLoboda() throws ContradictionException {
        Solver solver = new Solver();
        // VARS
        IntVar var = VF.enumerated("var", new int[]{0, 2}, solver);
        BoolVar eq2 = VF.bool("eq2", solver);
        BoolVar bvar = VF.bool("already", solver);
        BoolVar bvar2 = VF.bool("bvar2", solver);
        BoolVar cond = VF.bool("cond", solver);
        // CSTRS
        SatFactory.addFalse(bvar);
        ICF.arithm(var, "=", 2).reifyWith(eq2);
        SatFactory.addBoolAndArrayEqVar(new BoolVar[]{eq2, bvar.not()}, cond);
        SatFactory.addBoolOrArrayEqualTrue(new BoolVar[]{eq2.not(), cond});
        SatFactory.addBoolOrArrayEqVar(new BoolVar[]{bvar, cond}, bvar2);
        // SEARCH
        solver.set(ISF.lexico_LB(var));

        solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, var);
        Assert.assertEquals(var.getValue(), 2);

    }
}
