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

package org.chocosolver.solver.cstrs.basic;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IGraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Propagator to catch the set of loops in a set variable
 *
 * @author Jean-Guillaume Fages
 */
public class PropLoopSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IGraphVar g;
    private SetVar loops;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropLoopSet(IGraphVar graph, SetVar loops) {
        super(new Variable[]{graph, loops}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.loops = loops;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		ISet nodes = g.getPotentialNodes();
		for(int i=nodes.getFirstElement();i>=0;i=nodes.getNextElement()){
			if(g.getMandSuccOrNeighOf(i).contain(i)){ // mandatory loop detected
				loops.addToKernel(i,aCause);
			}else if(!g.getPotSuccOrNeighOf(i).contain(i)){ // no potential loop
				loops.removeFromEnvelope(i,aCause);
			}
			else if(loops.kernelContains(i)){
				g.enforceArc(i,i,aCause);
			}else if(!loops.envelopeContains(i)){
				g.removeArc(i,i,aCause);
			}
		}
		for(int i=loops.getEnvelopeFirst();i>=0;i=loops.getEnvelopeNext()){
			if(!nodes.contain(i)){
				loops.removeFromEnvelope(i,aCause);
			}
		}
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
		for(int i=loops.getKernelFirst();i>=0;i=loops.getKernelNext()){
			if(!g.getPotSuccOrNeighOf(i).contain(i)){
				return ESat.FALSE;
			}
		}
		for(int i=g.getMandatoryNodes().getFirstElement();i>=0;i=g.getMandatoryNodes().getNextElement()){
			if(g.getMandSuccOrNeighOf(i).contain(i) && !loops.envelopeContains(i)){
				return ESat.FALSE;
			}
		}
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
