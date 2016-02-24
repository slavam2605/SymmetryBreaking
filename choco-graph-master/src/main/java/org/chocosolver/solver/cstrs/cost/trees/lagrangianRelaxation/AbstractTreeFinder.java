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

package org.chocosolver.solver.cstrs.cost.trees.lagrangianRelaxation;

import org.chocosolver.solver.cstrs.cost.GraphLagrangianRelaxation;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

public abstract class AbstractTreeFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected final static boolean FILTER = false;
    // INPUT
    protected UndirectedGraph g;    // graph
    protected int n;                // number of nodes
    // OUTPUT
    protected UndirectedGraph Tree;
    protected double treeCost;
    // PROPAGATOR
    protected GraphLagrangianRelaxation propHK;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AbstractTreeFinder(int nbNodes, GraphLagrangianRelaxation propagator) {
        n = nbNodes;
        Tree = new UndirectedGraph(n, SetType.LINKED_LIST, false);
        propHK = propagator;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public abstract void computeMST(double[][] costMatrix, UndirectedGraph graph) throws ContradictionException;

    public abstract void performPruning(double UB) throws ContradictionException;

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    public UndirectedGraph getMST() {
        return Tree;
    }

    public double getBound() {
        return treeCost;
    }

    public double getRepCost(int from, int to) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
