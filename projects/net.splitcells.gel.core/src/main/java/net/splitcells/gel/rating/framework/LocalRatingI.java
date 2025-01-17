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
package net.splitcells.gel.rating.framework;

import java.util.List;

import net.splitcells.gel.constraint.GroupId;
import net.splitcells.gel.constraint.Constraint;

public class LocalRatingI implements LocalRating {

    private GroupId resultingConstraintGroupId;
    private Rating rating;
    private List<Constraint> propagationTo;

    public static LocalRating localRating() {
        return new LocalRatingI();
    }

    private LocalRatingI() {

    }

    @Override
    public GroupId resultingConstraintGroupId() {
        return resultingConstraintGroupId;
    }

    @Override
    public Rating rating() {
        return rating;
    }

    @Override
    public List<Constraint> propagateTo() {
        return propagationTo;
    }

    @Override
    public LocalRatingI withPropagationTo(List<Constraint> propagationTo) {
        this.propagationTo = propagationTo;
        return this;
    }

    @Override
    public LocalRatingI withRating(Rating rating) {
        this.rating = rating;
        return this;
    }

    @Override
    public LocalRatingI withResultingGroupId(GroupId resultingConstraintGroupId) {
        this.resultingConstraintGroupId = resultingConstraintGroupId;
        return this;
    }

}
