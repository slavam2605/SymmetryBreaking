/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.checked;

import org.testng.annotations.Test;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.cstrs.GraphConstraintFactory;
import org.chocosolver.solver.search.GraphStrategyFactory;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.variables.GraphVarFactory;
import org.chocosolver.solver.variables.IDirectedGraphVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ArborescenceTest {

	private static SetType graphTypeEnv = SetType.BOOL_ARRAY;
	private static SetType graphTypeKer = SetType.BOOL_ARRAY;

	public static Solver model(int n, int seed, boolean gac) {
		final Solver s = new Solver();
		DirectedGraph GLB = new DirectedGraph(s,n,graphTypeKer,false);
		DirectedGraph GUB = new DirectedGraph(s,n,graphTypeEnv,false);
		for (int i = 0; i < n; i++) {
			for (int j = 1; j < n; j++) {
				GUB.addArc(i, j);
			}
		}
		GLB.addNode(0);
		final IDirectedGraphVar g = GraphVarFactory.directed_graph_var("G", GLB, GUB, s);
		int[] preds = new int[n];
		for (int i = 0; i < n; i++) {
			preds[i] = 1;
		}
		preds[0] = 0;
		System.out.println("%%%%%%%%%");
		if(gac) {
			s.post(GraphConstraintFactory.directed_tree(g, 0));
		}else{
			s.post(GraphConstraintFactory.directed_forest(g));
			int[] indeg = new int[n];
			for(int i=0;i<n;i++) {
				indeg[i] = 1;
			}
			indeg[0] = 0;
			s.post(GraphConstraintFactory.min_in_degrees(g, indeg));
		}
		s.post(GraphConstraintFactory.nb_nodes(g, VariableFactory.bounded("nbNodes", n / 3, n, s)));
		s.set(GraphStrategyFactory.random(g, seed));
		SearchMonitorFactory.limitSolution(s, 1000);
		s.findAllSolutions();
		return s;
	}

	@Test(groups = "10s")
	public static void smallTrees() {
		for (int s = 0; s < 3; s++) {
			for (int n = 3; n < 8; n++) {
                System.out.println("Test n=" + n + ", with seed=" + s);
				Solver good = model(n, s, true);
				assertEquals(good.getMeasures().getFailCount(), 0);
				assertTrue(good.getMeasures().getSolutionCount() > 0);
				Solver slow = model(n, s, false);
				assertEquals(good.getMeasures().getSolutionCount(), slow.getMeasures().getSolutionCount());
			}
		}
	}

	@Test(groups = "10s")
	public static void bigTrees() {
		for (int s = 0; s < 3; s++) {
			int n = 60;
            System.out.println("Test n=" + n + ", with seed=" + s);
			Solver good = model(n, s, true);
			assertEquals(good.getMeasures().getFailCount(), 0);
			assertTrue(good.getMeasures().getSolutionCount()>0);
			Solver slow = model(n, s, false);
			assertEquals(good.getMeasures().getSolutionCount(), slow.getMeasures().getSolutionCount());
		}
	}

	@Test(groups = "1m")
	public static void testAllDataStructure() {
		for (SetType ge : SetType.values()) {
			graphTypeEnv = ge;
			graphTypeKer = ge;
            System.out.println("env:" + ge + " ker :" + ge);
			smallTrees();
		}
	}
}
