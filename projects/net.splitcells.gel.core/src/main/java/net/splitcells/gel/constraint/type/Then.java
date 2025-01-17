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
package net.splitcells.gel.constraint.type;

import static net.splitcells.dem.data.set.list.Lists.list;
import static net.splitcells.gel.rating.rater.ConstantRater.constantRater;

import net.splitcells.gel.constraint.type.framework.ConstraintBasedOnLocalGroupsAI;
import net.splitcells.gel.constraint.Constraint;
import net.splitcells.gel.constraint.Report;
import net.splitcells.gel.rating.framework.Rating;
import net.splitcells.gel.rating.rater.Rater;

public class Then extends ConstraintBasedOnLocalGroupsAI {

    public static Then then(Rater rater) {
        return new Then(rater);
    }

    public static Then then(Rating rating) {
        return new Then(constantRater(rating));
    }

    protected Then(Rater rater) {
        super(rater, rater.getClass().getSimpleName());
    }

    @Override
    public Class<? extends Constraint> type() {
        return Then.class;
    }

    @Override
    protected String localNaturalArgumentation(Report report) {
        return "Then " + rater.toSimpleDescription(report.line()
                , lineProcessing.columnView(Constraint.INCOMING_CONSTRAINT_GROUP).lookup(report.group())
                , report.group());
    }
}
