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

package org.chocosolver.solver.cstrs.cost.tsp.lagrangianRelaxation;

import org.chocosolver.solver.cstrs.cost.GraphLagrangianRelaxation;
import org.chocosolver.solver.cstrs.cost.trees.lagrangianRelaxation.PrimMSTFinder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.setDataStructures.ISet;

public class PrimOneTreeFinder extends PrimMSTFinder {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    protected int oneNode;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PrimOneTreeFinder(int nbNodes, GraphLagrangianRelaxation propagator) {
        super(nbNodes, propagator);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    protected void prim() throws ContradictionException {
        minVal = propHK.getMinArcVal();
        if (FILTER) {
            maxTArc = minVal;
        }
        chooseOneNode();
        inTree.set(oneNode);
        ISet nei = g.getNeighOf(oneNode);
        int min1 = -1;
        int min2 = -1;
        boolean b1 = false, b2 = false;
        for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
            if (!b1) {
                if (min1 == -1) {
                    min1 = j;
                }
                if (costs[oneNode][j] < costs[oneNode][min1]) {
                    min2 = min1;
                    min1 = j;
                }
                if (propHK.isMandatory(oneNode, j)) {
                    if (min1 != j) {
                        min2 = min1;
                    }
                    min1 = j;
                    b1 = true;
                }
            }
            if (min1 != j && !b2) {
                if (min2 == -1 || costs[oneNode][j] < costs[oneNode][min2]) {
                    min2 = j;
                }
                if (propHK.isMandatory(oneNode, j)) {
                    min2 = j;
                    b2 = true;
                }
            }
        }
        if (min1 == -1 || min2 == -1) {
            propHK.contradiction();
        }
        if (FILTER) {
            if (!propHK.isMandatory(oneNode, min1)) {
                maxTArc = Math.max(maxTArc, costs[oneNode][min1]);
            }
            if (!propHK.isMandatory(oneNode, min2)) {
                maxTArc = Math.max(maxTArc, costs[oneNode][min2]);
            }
        }
        int first = -1, sizeFirst = n + 1;
        for (int i = 0; i < n; i++) {
            if (i != oneNode && g.getNeighOf(i).getSize() < sizeFirst) {
                first = i;
                sizeFirst = g.getNeighOf(i).getSize();
            }
        }
        if (first == -1) {
            propHK.contradiction();
        }
        addNode(first);
        int from, to;
        while (tSize < n - 2 && !heap.isEmpty()) {
            to = heap.removeFirstElement();
            from = mate[to];
            addArc(from, to);
        }
        if (tSize != n - 2) {
            propHK.contradiction();
        }
        addArc(oneNode, min1);
        addArc(oneNode, min2);
        if (Tree.getNeighOf(oneNode).getSize() != 2) {
            throw new UnsupportedOperationException();
        }
    }

    private void chooseOneNode() {
        oneNode = 0;
    }
}
