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
package org.chocosolver.choco.checker.correctness;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.choco.checker.Modeler;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;
import static org.chocosolver.choco.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.solver.Solver.writeInFile;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/02/11
 */
public class CorrectnessChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger("test");

    public static void checkCorrectness(Modeler modeler, int nbVar, int lowerB, int upperB, long seed, Object parameters) {
        Random r = new Random(seed);
//        System.out.printf("Running %s\n", modeler.name());
        THashMap<int[], IntVar> map = new THashMap<>();
        double[] densities = {0.1, 0.25, 0.5, 0.75, 1.0};
        boolean[] homogeneous = {true, false};
        int loop = 0;
        for (int ds = lowerB; ds <= upperB; ds++) {
            for (int ide = 0; ide < densities.length; ide++) {
                for (int h = 0; h < homogeneous.length; h++) {
                    map.clear();
                    int[][] domains = buildFullDomains(nbVar, lowerB, ds, r, densities[ide], homogeneous[h]);
                    Solver ref = referencePropagation(modeler, nbVar, domains, map, parameters);
                    if (ref == null) break; // no solution found for this generated problem
                    // otherwise, link original domains with reference one.
                    IntVar[] rvars = new IntVar[nbVar];
                    for (int k = 0; k < nbVar; k++) {
                        rvars[k] = map.get(domains[k]);
                    }
                    for (int d = 0; d < domains.length; d++) {
                        int[] values = getRemovedValues(rvars[d], domains[d]);
                        for (int v = 0; v < values.length; v++) {
                            loop++;
                            int val = values[v];
                            int[][] _domains = new int[nbVar][];

                            arraycopy(domains, 0, _domains, 0, d);
                            _domains[d] = new int[]{val};
                            arraycopy(domains, d + 1, _domains, d + 1, nbVar - (d + 1));

                            Solver test = modeler.model(nbVar, _domains, null, parameters);
                            try {
                                if (test.findSolution()) {
                                    LOGGER.error("ds :{}, ide:{}, h:{}, var:{}, val:{}, loop:{}, seed: {}",
                                            ds, ide, h, rvars[d], val, loop, seed);
                                    LOGGER.error("REF:\n{}\n", ref);
                                    ref.getEnvironment().worldPop();
                                    LOGGER.error("REF:\n{}\nTEST:\n{}", ref, test);
                                    File f = new File("SOLVER_ERROR.ser");
                                    try {
                                        writeInFile(ref, f);
                                    } catch (IOException ee) {
                                        ee.printStackTrace();
                                    }
                                    LOGGER.error("{}", f.getAbsolutePath());
                                    fail("one solution found");
                                }
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage());
                                LOGGER.error("ds :{}, ide:{}, h:{}, var:{}, val:{}, loop:{}, seed: {}",
                                        ds, ide, h, rvars[d], val, loop, seed);
                                LOGGER.error("REF:\n{}\nTEST:\n{}", ref, test);
                                File f = new File("SOLVER_ERROR.ser");
                                try {
                                    writeInFile(ref, f);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                                LOGGER.error("{}", f.getAbsolutePath());
                                fail();
                            }
                        }
                    }
                }
            }
        }
//        System.out.printf("loop: %d\n", loop);
    }

    private static Solver referencePropagation(Modeler modeler, int nbVar, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
        Solver ref = modeler.model(nbVar, domains, map, parameters);
        ref.getEnvironment().worldPush();
        try {
            ref.propagate();
        } catch (ContradictionException e) {
            LOGGER.debug("Pas de solution pour ce probleme => rien a tester !");
            return null;
        } catch (Exception e) {
            File f = new File("SOLVER_ERROR.ser");
            try {
                writeInFile(ref, f);
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            LOGGER.error(e.getMessage());
            LOGGER.error("REF:\n{}\n", ref);
            LOGGER.error("{}", f.getAbsolutePath());
            fail();
        }
        return ref;
    }

    private static int[] getRemovedValues(IntVar variable, int[] domain) {
        int[] _values = domain.clone();
        int k = 0;
        for (int i = 0; i < domain.length; i++) {
            if (!variable.contains(domain[i])) {
                _values[k++] = domain[i];
            }
        }
        return copyOfRange(_values, 0, k);
    }

}
