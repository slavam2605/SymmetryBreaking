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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package org.chocosolver.solver.cstrs.cost.trees;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphEventType;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

import java.util.BitSet;

/**
 * Simple NoSubtour applied to (undirected) tree/forest
 */
public class PropTreeNoSubtour extends Propagator<IUndirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    IUndirectedGraphVar g;
    IGraphDeltaMonitor gdm;
    int n;
    private PairProcedure arcEnforced;
    private IStateInt[] color, size;
    // list
    int[] fifo;
    int[] mate;
    BitSet in;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that graph has no cycle
     * runs in O(n) per instantiation event
     *
     * @param graph
     */
    public PropTreeNoSubtour(IUndirectedGraphVar graph) {
        super(new IUndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, true);
        g = graph;
        gdm = g.monitorDelta(this);
        this.n = g.getNbMaxNodes();
        arcEnforced = new EnfArc();
        fifo = new int[n];
        mate = new int[n];
        in = new BitSet(n);
        color = new IStateInt[n];
        size = new IStateInt[n];
		IEnvironment environment = solver.getEnvironment();
        for (int i = 0; i < n; i++) {
            color[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            color[i].set(i);
            size[i].set(1);
            mate[i] = -1;
        }
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getMandNeighOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    enforce(i, j);
                }
            }
        }
        gdm.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.freeze();
        gdm.forEachArc(arcEnforced, GraphEventType.ADD_ARC);
        gdm.unfreeze();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.ADD_ARC.getMask();
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; //not implemented
    }

    private void enforce(int i, int j) throws ContradictionException {
        if (size[color[i].get()].get() > size[color[j].get()].get()) {
            enforce(j, i);
            return;
        }
        if (i == j) {
            throw new UnsupportedOperationException();
        }
        int ci = color[i].get();
        int cj = color[j].get();
        if (ci == cj) {
            contradiction(g, "");
        }
        int idxFirst = 0;
        int idxLast = 0;
        in.clear();
        in.set(i);
        fifo[idxLast++] = i;
        int x, ck;
        mate[i] = j;
        while (idxFirst < idxLast) {
            x = fifo[idxFirst++];
            ISet nei = g.getPotNeighOf(x);
            for (int k = nei.getFirstElement(); k >= 0; k = nei.getNextElement()) {
                if (k != mate[x]) {
                    ck = color[k].get();
                    if (ck == cj) {
                        g.removeArc(x, k, aCause);
                    } else {
                        if (ck == ci && !in.get(k)) {
                            in.set(k);
                            fifo[idxLast++] = k;
                            mate[k] = x;
                        }
                    }
                }
            }
            color[x].set(cj);
        }
        size[cj].add(size[ci].get());
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private class EnfArc implements PairProcedure {
        @Override
        public void execute(int i, int j) throws ContradictionException {
            enforce(i, j);
        }
    }
}
