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

package org.chocosolver.solver.search;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IGraphVar;
import org.chocosolver.util.PoolManager;

public class GraphDecision extends Decision<IGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected GraphAssignment assignment;
    protected int from, to;
    protected final PoolManager<GraphDecision> poolManager;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public GraphDecision(PoolManager<GraphDecision> poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public Object getDecisionValue() {
        if (to == -1) {
            return from;
        } else {
            return new int[]{from, to};
        }
    }

    public void setNode(IGraphVar variable, int node, GraphAssignment graph_ass) {
        super.set(variable);
        this.from = node;
        this.to = -1;
        assignment = graph_ass;
    }

    public void setArc(IGraphVar variable, int from, int to, GraphAssignment graph_ass) {
        super.set(variable);
        this.from = from;
        this.to = to;
        assignment = graph_ass;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            if (to == -1) {
                assignment.apply(var, from, this);
            } else {
                assignment.apply(var, from, to, this);
            }
        } else if (branch == 2) {
            if (to == -1) {
                assignment.unapply(var, from, this);
            } else {
                assignment.unapply(var, from, to, this);
            }
        }
    }

    @Override
    public void free() {
        previous = null;
        poolManager.returnE(this);
    }

    @Override
    public String toString() {
        if (to == -1) {
            return " node " + from + assignment.toString();
        }
        return " arc (" + from + "," + to + ")" + assignment.toString();
    }

    @Override
    public void explain(ExplanationEngine xengine, Deduction d, Explanation e) {
        throw new UnsupportedOperationException("GraphDecision is not equipped for explanations");
    }
}
