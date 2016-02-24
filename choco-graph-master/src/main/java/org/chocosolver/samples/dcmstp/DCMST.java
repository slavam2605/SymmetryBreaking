package org.chocosolver.samples.dcmstp;
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

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.cstrs.GraphConstraintFactory;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.GraphStrategies;
import org.chocosolver.solver.variables.GraphVarFactory;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.io.*;

/**
 * Solves the Degree Constrained Minimum Spanning Tree Problem
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class DCMST extends AbstractProblem {

	//***********************************************************************************
	// BENCHMARK
	//***********************************************************************************

	public static void main(String[] args) {
		String dir = "src/main/java/samples/dcmstp";
		String inst = "r123_300_1";
		new DCMST(dir, inst).execute();
	}

	private static final String OUT_PUT_FILE = "DR.csv";

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// input
	private int n;
	private int[] dMax;
	private int[][] dist;
	private int lb, ub;
	private String instance;
	// model
	private IntVar totalCost;
	private IntVar[] degrees;
	private IUndirectedGraphVar graph;
	// parameters
	public static long TIMELIMIT = 300000;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public DCMST(String dir, String inst) {
		parse_T_DE_DR(new File(dir + "/" + inst));
		instance = inst;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void createSolver() {
		this.level = Level.QUIET;
		solver = new Solver("DCMSTP");
	}

	@Override
	public void buildModel() {
		totalCost = VariableFactory.bounded("obj", lb, ub, solver);
		// graph var domain
		UndirectedGraph GLB = new UndirectedGraph(solver,n,SetType.LINKED_LIST,true);
		UndirectedGraph GUB = new UndirectedGraph(solver,n,SetType.BIPARTITESET,true);
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (dist[i][j] != -1 && !(dMax[i] == 1 && dMax[j] == 1)) {
					GUB.addEdge(i, j); // possible edge
				}
			}
		}
		graph = GraphVarFactory.undirected_graph_var("G", GLB, GUB, solver);
		degrees = GraphVarFactory.degrees(graph);
		for (int i = 0; i < n; i++) {
			solver.post(ICF.arithm(degrees[i], "<=", dMax[i]));
		}

		// degree constrained-minimum spanning tree constraint
		solver.post(GraphConstraintFactory.dcmst(graph,degrees,totalCost,dist,2));
	}

	@Override
	public void configureSearch() {
		final GraphStrategies mainSearch = new GraphStrategies(graph, dist);
		// find the first solution by selecting cheap edges
		mainSearch.configure(GraphStrategies.MIN_COST, true);
		// then select the most expensive ones (fail first principle, with last conflict)
		solver.plugMonitor((IMonitorSolution) () -> {
            mainSearch.useLastConflict();
            mainSearch.configure(GraphStrategies.MIN_P_DEGREE, true);
            System.out.println("Solution found : "+totalCost);
        });
		// bottom-up optimization : find a first solution then reach the global minimum from below
		solver.set(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), mainSearch);
		SearchMonitorFactory.limitSolution(solver, 2); // therefore there is at most two solutions
		SearchMonitorFactory.limitTime(solver, TIMELIMIT); // time limit
	}

	@Override
	public void solve() {
		// find optimum
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		if (solver.getMeasures().getSolutionCount() == 0
				&& solver.getMeasures().getTimeCount() < TIMELIMIT/1000) {
			throw new UnsupportedOperationException("Provided instances are feasible!");
		}
		IMeasures m = solver.getMeasures();
		String output = instance+";"+m.getSolutionCount()+";"+m.getBestSolutionValue()+";"
				+m.getNodeCount()+";"+m.getFailCount()+";"+m.getTimeCount()+";\n";
		write(output,OUT_PUT_FILE,false);
	}

	@Override
	public void prettyOut() {}

	private static void write(String text, String file, boolean clearFirst){
		try{
			FileWriter writer = new FileWriter(file, !clearFirst);
			writer.write(text);
			writer.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

	//***********************************************************************************
	// PARSING
	//***********************************************************************************

	public boolean parse_T_DE_DR(File file) {
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String[] numbers;
			n = Integer.parseInt(line);
			dist = new int[n][n];
			dMax = new int[n];
			for (int i = 0; i < n; i++) {
				line = buf.readLine();
				numbers = line.split(" ");
				if (Integer.parseInt(numbers[0]) != i + 1) {
					throw new UnsupportedOperationException();
				}
				dMax[i] = Integer.parseInt(numbers[1]);
				for (int j = 0; j < n; j++) {
					dist[i][j] = -1;
				}
			}
			line = buf.readLine();
			int from, to, cost;
			int min = 1000000;
			int max = 0;
			while (line != null) {
				numbers = line.split(" ");
				from = Integer.parseInt(numbers[0]) - 1;
				to = Integer.parseInt(numbers[1]) - 1;
				cost = Integer.parseInt(numbers[2]);
				min = Math.min(min, cost);
				max = Math.max(max, cost);
				if (dist[from][to] != -1) {
					throw new UnsupportedOperationException();
				}
				dist[from][to] = dist[to][from] = cost;
				line = buf.readLine();
			}
			lb = (n - 1) * min;
			ub = (n - 1) * max;
			//            setUB(dirOpt, s);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		throw new UnsupportedOperationException();
	}
}
