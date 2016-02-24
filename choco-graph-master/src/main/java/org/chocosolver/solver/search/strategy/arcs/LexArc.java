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
package org.chocosolver.solver.search.strategy.arcs;

import org.chocosolver.solver.search.strategy.ArcStrategy;
import org.chocosolver.solver.variables.IGraphVar;
import org.chocosolver.util.objects.setDataStructures.ISet;

public class LexArc extends ArcStrategy<IGraphVar> {

	public LexArc(IGraphVar g) {
		super(g);
	}

	@Override
	public boolean computeNextArc() {
		ISet envSuc, kerSuc;
		for (int i = envNodes.getFirstElement(); i >= 0; i = envNodes.getNextElement()) {
			envSuc = g.getPotSuccOrNeighOf(i);
			kerSuc = g.getMandSuccOrNeighOf(i);
			if (envSuc.getSize() != kerSuc.getSize()) {
				for (int j = envSuc.getFirstElement(); j >= 0; j = envSuc.getNextElement()) {
					if (!kerSuc.contain(j)) {
						this.from = i;
						this.to = j;
						return true;
					}
				}
			}
		}
		this.from = this.to = -1;
		return false;
	}
}
