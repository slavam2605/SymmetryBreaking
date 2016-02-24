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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 13/02/13
 * Time: 22:08
 */

package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.setDataStructures.ISet;

public interface IncidentSet {

	ISet getPotSet(IGraphVar graph, int i);

	ISet getMandSet(IGraphVar graph, int i);

	boolean enforce(IGraphVar g, int from, int to, ICause cause) throws ContradictionException;

	boolean remove(IGraphVar g, int from, int to, ICause cause) throws ContradictionException;

	public class SuccOrNeighSet implements IncidentSet {

		@Override
		public ISet getPotSet(IGraphVar graph, int i) {
			return graph.getPotSuccOrNeighOf(i);
		}

		@Override
		public ISet getMandSet(IGraphVar graph, int i) {
			return graph.getMandSuccOrNeighOf(i);
		}

		@Override
		public boolean enforce(IGraphVar g, int from, int to, ICause cause) throws ContradictionException {
			return g.enforceArc(from, to, cause);
		}

		@Override
		public boolean remove(IGraphVar g, int from, int to, ICause cause) throws ContradictionException {
			return g.removeArc(from, to, cause);
		}
	}

	public class PredOrNeighSet implements IncidentSet {
		@Override
		public ISet getPotSet(IGraphVar graph, int i) {
			return graph.getPotPredOrNeighOf(i);
		}
		@Override
		public ISet getMandSet(IGraphVar graph, int i) {
			return graph.getMandPredOrNeighOf(i);
		}
		@Override
		public boolean enforce(IGraphVar g, int from, int to, ICause cause) throws ContradictionException {
			return g.enforceArc(to, from, cause);
		}
		@Override
		public boolean remove(IGraphVar g, int from, int to, ICause cause) throws ContradictionException {
			return g.removeArc(to, from, cause);
		}
	}
}
