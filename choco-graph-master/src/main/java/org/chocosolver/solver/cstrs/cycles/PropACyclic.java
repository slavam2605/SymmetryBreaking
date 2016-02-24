/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.chocosolver.solver.cstrs.cycles;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphEventType;
import org.chocosolver.solver.variables.IGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

import java.util.BitSet;


/**
 * Propagator for the no-cycle constraint (general case)
 *
 * @author Jean-Guillaume Fages
 */
public class PropACyclic extends Propagator<IGraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private IGraphVar g;
	private IGraphDeltaMonitor gdm;
	private PairProcedure arcEnf;
	private int n;
	private BitSet rfFrom, rfTo;
	private int[] fifo;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropACyclic(IGraphVar g){
		super(new IGraphVar[]{g}, PropagatorPriority.LINEAR,true);
		this.g = g;
		this.n = g.getNbMaxNodes();
		this.fifo = new int[n];
		this.rfFrom = new BitSet(n);
		this.rfTo = new BitSet(n);
		this.gdm = g.monitorDelta(this);
		this.arcEnf = this::propagateIJ;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int idx){
		return GraphEventType.ADD_ARC.getMask();
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			g.removeArc(i,i,aCause);
			if(g.getMandSuccOrNeighOf(i).getSize()>0) {
				for(int j=0;j<n;j++){
					if(g.getMandSuccOrNeighOf(i).contain(j)) {
						propagateIJ(i, j);
					}
				}
			}
		}
		gdm.unfreeze();
	}

	@Override
	public void propagate(int idx, int mask) throws ContradictionException {
		gdm.freeze();
		gdm.forEachArc(arcEnf, GraphEventType.ADD_ARC);
		gdm.unfreeze();
	}


	public void propagateIJ(int from, int to) throws ContradictionException {
		if(g.isDirected()){
			g.removeArc(to,from,aCause);
		}
		int first, last, i;
		// mark reachable from 'To'
		first = 0;
		last = 0;
		i = to;
		rfTo.clear();
		fifo[last++] = i;
		rfTo.set(i);
		while(first<last){
			i = fifo[first++];
			ISet nei = g.getMandSuccOrNeighOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j!=from && !rfTo.get(j)){
					rfTo.set(j);
					fifo[last++] = j;
				}
			}
		}
		// mark reachable from 'From'
		first = 0;
		last = 0;
		i = from;
		rfFrom.clear();
		fifo[last++] = i;
		rfFrom.set(i);
		while(first<last){
			i = fifo[first++];
			ISet nei = g.getMandPredOrNeighOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j!=to && !rfFrom.get(j)){
					rfFrom.set(j);
					fifo[last++] = j;
				}
			}
		}
		// filter arcs that would create a circuit
		ISet nodes = g.getPotentialNodes();
		for(i=nodes.getFirstElement();i>=0;i=nodes.getNextElement()){
			if(rfTo.get(i)) {
				ISet nei = g.getPotSuccOrNeighOf(i);
				for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
					if (rfFrom.get(j)) {
						if((i!=from || j!=to) && (i!=to || j!=from)) {
							g.removeArc(i, j, aCause);
						}
					}
				}
			}
		}
	}

	@Override
	public ESat isEntailed() {
		for(int from=0;from<n;from++){
			ISet neigh = g.getMandSuccOrNeighOf(from);
			for(int to=neigh.getFirstElement();to>=0;to=neigh.getNextElement()){
				int first, last, i;
				// mark reachable from 'To'
				first = 0;
				last = 0;
				i = to;
				rfTo.clear();
				fifo[last++] = i;
				rfTo.set(i);
				while(first<last){
					i = fifo[first++];
					ISet nei = g.getMandSuccOrNeighOf(i);
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(j!=from && !rfTo.get(j)){
							rfTo.set(j);
							fifo[last++] = j;
						}
					}
				}
				// mark reachable from 'From'
				first = 0;
				last = 0;
				i = from;
				rfFrom.clear();
				fifo[last++] = i;
				rfFrom.set(i);
				while(first<last){
					i = fifo[first++];
					ISet nei = g.getMandPredOrNeighOf(i);
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(j!=to && !rfFrom.get(j)){
							rfFrom.set(j);
							fifo[last++] = j;
						}
					}
				}
				// filter arcs that would create a circuit
				ISet nodes = g.getMandatoryNodes();
				for(i=nodes.getFirstElement();i>=0;i=nodes.getNextElement()){
					if(rfTo.get(i)) {
						ISet nei = g.getMandSuccOrNeighOf(i);
						for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
							if (rfFrom.get(j)) {
								if((i!=from || j!=to) && (i!=to || j!=from)) {
									return ESat.FALSE;
								}
							}
						}
					}
				}
			}
		}
		if (!isCompletelyInstantiated()) {
			return ESat.UNDEFINED;
		}
		return ESat.TRUE;
	}
}
