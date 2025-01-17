/*
 * Copyright (c) 2021 Mārtiņš Avots (Martins Avots) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the MIT License,
 * which is available at https://spdx.org/licenses/MIT.html.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * or any later versions with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT OR GPL-2.0-or-later WITH Classpath-exception-2.0
 */
package net.splitcells.gel.solution.optimization.meta;

import net.splitcells.dem.data.set.list.List;
import net.splitcells.gel.solution.SolutionView;
import net.splitcells.gel.solution.optimization.Optimization;
import net.splitcells.gel.solution.optimization.OptimizationEvent;

import static net.splitcells.dem.utils.NotImplementedYet.notImplementedYet;
import static net.splitcells.dem.data.set.list.Lists.list;

public class Repeater implements Optimization {
    public static Repeater repeater(Optimization optimization, int maximalRepetitionCount) {
        return new Repeater(optimization, maximalRepetitionCount);
    }

    private final Optimization optimization;
    private final int maximalRepetitionCount;
    private int repetitionCount = 0;

    private Repeater(Optimization optimization, int maximalRepetitionCount) {
        this.optimization = optimization;
        this.maximalRepetitionCount = maximalRepetitionCount;
    }

    @Override
    public List<OptimizationEvent> optimize(SolutionView solution) {
        if (repetitionCount >= maximalRepetitionCount) {
            return list();
        }
        repetitionCount += 1;
        return optimize(solution);
    }
}
