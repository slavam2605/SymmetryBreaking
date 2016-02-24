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

/**
 * @author Jean-Guillaume Fages
 * @since 08/10/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SetIntUnion {

	@Test(groups = "1s")
	public void test1() {
        Solver s = new Solver();
        IntVar[] x = VF.enumeratedArray("ints", 4, 0, 5, s);
        SetVar values = VF.fixed("values", new int[]{0, 1, 4}, s);
        s.post(SCF.int_values_union(x, values));
        Chatterbox.showStatistics(s);
        Chatterbox.showSolutions(s);
        s.set(ISF.lexico_LB(x));
        s.findAllSolutions();
    }

	@Test(groups = "1s")
	public void test2() {
        Solver s = new Solver();
        IntVar[] x = new IntVar[]{
                VF.fixed(0, s)
                , VF.fixed(2, s)
                , VF.fixed(5, s)
                , VF.fixed(0, s)
                , VF.fixed(2, s)
        };
        SetVar values = VF.fixed("values", new int[]{0, 1, 4}, s);
        s.post(SCF.int_values_union(x, values));
        Chatterbox.showStatistics(s);
        Chatterbox.showSolutions(s);
        s.set(ISF.lexico_LB(x));
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 0);
    }

	@Test(groups = "1s")
	public void test3() {
        Solver s = new Solver();
        IntVar[] x = new IntVar[]{
                VF.fixed(0, s)
                , VF.fixed(2, s)
                , VF.fixed(5, s)
                , VF.fixed(0, s)
                , VF.fixed(2, s)
        };
        SetVar values = VF.set("values", -1, 6, s);
        s.post(SCF.int_values_union(x, values));
        Chatterbox.showStatistics(s);
        Chatterbox.showSolutions(s);
        s.set(ISF.lexico_LB(x));
        s.findAllSolutions();
        System.out.println(values);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 1);
    }
}
