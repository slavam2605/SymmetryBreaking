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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;
import org.testng.annotations.Test;

/**
 * Tests the various filtering algorithms of the cumulative constraint
 * @author Thierry Petit, Jean-Guillaume Fages
 */
public class CumulativeTest {

	public static final boolean VERBOSE = false;
	// too long, but can be used manually
	public void testLong(){
		for(int mode:new int[]{1})
			for(int n=1;n<100;n*=2){
				for(int dmin = 0; dmin<5;dmin++){
					for(int hmax = 0; hmax<5;hmax++){
						for(int capamax = 0; capamax<6;capamax++){
							for(long seed = 0; seed<5;seed++){
								test(n,capamax,dmin,hmax,seed,mode);
							}
						}
					}
				}
			}
	}

	@Test(groups = "1s")
	public void test1(){
		test(4,5,0,2,0,0);
		test(4,5,0,2,0,1);
		test(4,5,1,2,0,0);
		test(4,5,1,2,0,1);
		test(4,5,2,2,0,0);
		test(4,5,2,2,0,1);
	}

	@Test(groups = "1s")
	public void test2(){
		test(4,9,2,4,2,1);
	}

	@Test(groups = "1s")
	public void test3(){
		test(32,3,0,2,3,0);
	}

	@Test(groups = "1s")
	public void test4(){
		test(16,6,2,4,9,0);
	}

	@Test(groups = "1s")
	public void test5(){
		test(32,3,2,4,1,0);
	}

	@Test(groups = "10s")
	public void test6(){
		// this tests raises an exception which is in fact due to the time limit
		// and unlucky random heuristic (fixed by adding last conflict)
		test(16,3,2,4,4,1);
		test(32,3,2,2,3,0);
	}

	@Test(groups = "1m")
	public void testMed(){
		for(int mode:new int[]{0,1})
			for(int n=1;n<20;n*=2){
				for(int dmin = 0; dmin<5;dmin+=2){
					for(int hmax = 0; hmax<5;hmax+=2){
						for(int capamax = 0; capamax<10;capamax+=3){
							for(long seed = 0; seed<10;seed++){
//								long seed = System.currentTimeMillis();
								test(n,capamax,dmin,hmax,seed,mode);
							}
						}
					}
				}
			}
	}

	public void test(int n, int capamax, int dmin, int hmax, long seed, int mode){
		if(VERBOSE)System.out.println(n+" - "+capamax+" - "+dmin+" - "+hmax+" - "+seed+" - "+mode);
		Cumulative.Filter[][] filters = new Cumulative.Filter[][]{
				{Cumulative.Filter.TIME},
				{Cumulative.Filter.TIME,Cumulative.Filter.NRJ},
				{Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP},
				{Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP_HEI_SORT},
				{Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP,Cumulative.Filter.NRJ},
				{Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP_HEI_SORT,Cumulative.Filter.NRJ},
				{Cumulative.Filter.TIME,Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP,Cumulative.Filter.NRJ},
				{Cumulative.Filter.TIME,Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP_HEI_SORT,Cumulative.Filter.NRJ},
				{Cumulative.Filter.TIME,Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP,Cumulative.Filter.SWEEP_HEI_SORT,Cumulative.Filter.NRJ}
		};
		long ref = solve(n,capamax,dmin,hmax,seed,true, mode);
		if(ref==-1)return;
		for(boolean g : new boolean[]{true,false})	// graph-based
			for(int f=1;f<filters.length;f++){
				long val = solve(n,capamax,dmin,hmax,seed,g, mode);
				assert ref == val || val==-1 :"filter "+f+" failed (can be due to the heuristic in case of timeout)";
			}
	}

	public static long solve(int n, int capamax, int dmin, int hmax, long seed,
							 boolean graph, int mode) {
		final Solver solver = new Solver();
		int dmax = 5+dmin*2;
		final IntVar[] s = VF.enumeratedArray("s",n,0,n*dmax,solver);
		final IntVar[] d = VF.enumeratedArray("d",n,dmin,dmax,solver);
		final IntVar[] e = VF.enumeratedArray("e",n,0,n*dmax,solver);
		final IntVar[] h = VF.enumeratedArray("h",n,0,hmax,solver);
		final IntVar capa = VF.enumerated("capa", 0, capamax, solver);
		final IntVar last = VF.enumerated("last", 0, n * dmax, solver);
		Task[] t = new Task[n];
		for(int i=0;i<n;i++){
			t[i] = new Task(s[i],d[i],e[i]);
			solver.post(ICF.arithm(e[i],"<=",last));
		}
		Constraint c = ICF.cumulative(t,h,capa,graph);
		solver.post(c);
		solver.set(ISF.random_bound(solver.retrieveIntVars(), seed));
		solver.set(ISF.lastConflict(solver,solver.getStrategy()));
		SMF.limitTime(solver,5000);
		switch (mode){
			case 0:	solver.findSolution();
				if(solver.hasReachedLimit())return -1;
				return solver.getMeasures().getSolutionCount();
			case 1:	solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,last);
				if(solver.hasReachedLimit())return -1;
				return solver.getMeasures().getBestSolutionValue().longValue();
			case 2:	solver.findAllSolutions();// too many solutions to be used
				if(solver.hasReachedLimit())return -1;
				return solver.getMeasures().getSolutionCount();
			default:throw new UnsupportedOperationException();
		}
	}

}
