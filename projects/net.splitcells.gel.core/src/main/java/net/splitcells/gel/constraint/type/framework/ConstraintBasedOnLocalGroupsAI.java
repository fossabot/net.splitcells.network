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
package net.splitcells.gel.constraint.type.framework;

import static net.splitcells.dem.data.set.list.Lists.list;

import java.util.function.Function;

import net.splitcells.dem.lang.dom.Domable;
import net.splitcells.dem.data.set.list.List;
import net.splitcells.gel.data.table.Line;
import net.splitcells.gel.constraint.Constraint;
import net.splitcells.gel.constraint.GroupId;
import net.splitcells.gel.rating.rater.Rater;
import net.splitcells.gel.rating.rater.RatingEvent;

@Deprecated
public abstract class ConstraintBasedOnLocalGroupsAI extends ConstraintAI {
    protected final Rater rater;

    protected ConstraintBasedOnLocalGroupsAI(Function<Constraint, Rater> raterFactory) {
        super(Constraint.standardGroup());
        rater = raterFactory.apply(this);
    }

    protected ConstraintBasedOnLocalGroupsAI(Rater rater, String name) {
        this(Constraint.standardGroup(), rater, name);
    }

    protected ConstraintBasedOnLocalGroupsAI(Rater rater) {
        this(Constraint.standardGroup(), rater, "");
    }

    protected ConstraintBasedOnLocalGroupsAI(GroupId standardGroup, Rater rater, String name) {
        super(standardGroup, name);
        this.rater = rater;
    }

    @Override
    public void processLineAddition(Line addition) {
        final var incomingGroup = addition.value(INCOMING_CONSTRAINT_GROUP);
        processRatingEvent(
                rater.ratingAfterAddition(
                        lines.columnView(INCOMING_CONSTRAINT_GROUP)
                                .lookup(incomingGroup)
                        , addition
                        , children
                        , lineProcessing
                                .columnView(INCOMING_CONSTRAINT_GROUP)
                                .lookup(incomingGroup)));
    }

    protected void processRatingEvent(RatingEvent ratingEvent) {
        ratingEvent.removal().forEach(removal ->
                lineProcessing.allocationsOfDemand(removal).forEach(lineProcessing::remove));
        ratingEvent.additions().forEach((line, resultUpdate) -> {
            final var r = addResult(resultUpdate);
            int i = r.index();
            lineProcessing.allocate(line, r);
        });
    }

    @Override
    protected void processLinesBeforeRemoval(GroupId incomingGroup, Line removal) {
        processRatingEvent(
                rater.rating_before_removal(
                        lines.columnView(INCOMING_CONSTRAINT_GROUP).lookup(incomingGroup)
                        , lines.columnView(INCOMING_CONSTRAINT_GROUP)
                                .lookup(incomingGroup)
                                .columnView(LINE)
                                .lookup(removal)
                                .getLines(0)
                        , children
                        , lineProcessing.columnView(INCOMING_CONSTRAINT_GROUP).lookup(incomingGroup)));
        super.processLinesBeforeRemoval(incomingGroup, removal);
    }

    @Override
    public List<String> path() {
        return mainContext
                .map(context -> context.path())
                .orElseGet(() -> list())
                .withAppended(this.getClass().getSimpleName());
    }

    @Override
    public List<Domable> arguments() {
        return list(rater);
    }

}