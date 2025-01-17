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
import static net.splitcells.gel.rating.rater.classification.RaterBasedOnGrouping.raterBasedGrouping;

import net.splitcells.gel.constraint.type.framework.ConstraintBasedOnLocalGroupsAI;
import net.splitcells.gel.constraint.Constraint;
import net.splitcells.gel.constraint.Report;
import net.splitcells.gel.rating.rater.Rater;
import net.splitcells.gel.rating.rater.classification.ForAllAttributeValues;
import net.splitcells.gel.rating.rater.classification.Propagation;
import net.splitcells.gel.rating.rater.classification.RaterBasedOnGrouping;

public class ForAll extends ConstraintBasedOnLocalGroupsAI {

    public static ForAll forAll(Rater classifier) {
        return create(classifier);
    }

    @Deprecated
    public static ForAll create(Rater classifier) {
        return new ForAll(classifier);
    }

    protected ForAll(Rater classifier) {
        super(raterBasedGrouping(classifier));
    }

    public Rater classification() {
        return rater;
    }

    @Override
    public Class<? extends Constraint> type() {
        return ForAll.class;
    }

    @Override
    protected String localNaturalArgumentation(Report report) {
        final var raterBasedOnGrouping = rater.casted(RaterBasedOnGrouping.class);
        if (raterBasedOnGrouping.isPresent()) {
            if (raterBasedOnGrouping.get().classifier().type().equals(Propagation.class)) {
                return raterBasedOnGrouping.get().classifier().toSimpleDescription(report.line()
                        , lineProcessing
                                .columnView(Constraint.INCOMING_CONSTRAINT_GROUP)
                                .lookup(report.group())
                        , report.group());
            }
        }
        if (rater.type().equals(ForAllAttributeValues.class)) {
            return rater.toSimpleDescription(report.line()
                    , lineProcessing.columnView(Constraint.INCOMING_CONSTRAINT_GROUP).lookup(report.group())
                    , report.group());
        } else {
            return "For all " + rater.toSimpleDescription(report.line()
                    , lineProcessing.columnView(Constraint.INCOMING_CONSTRAINT_GROUP).lookup(report.group())
                    , report.group());
        }
    }
}
