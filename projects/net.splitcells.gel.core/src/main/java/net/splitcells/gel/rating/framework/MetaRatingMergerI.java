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

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static net.splitcells.dem.lang.Xml.elementWithChildren;
import static net.splitcells.dem.data.set.map.Maps.map;
import static net.splitcells.dem.data.set.map.Maps.typeMapping;
import static net.splitcells.dem.utils.NotImplementedYet.notImplementedYet;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import net.splitcells.dem.lang.Xml;
import org.w3c.dom.Element;
import net.splitcells.dem.data.order.Ordering;
import net.splitcells.dem.data.set.map.Map;

public class MetaRatingMergerI implements MetaRatingMerger {
    protected final Map<Class<? extends Rating>, Rating> ratings;
    protected final Map<BiPredicate
            <Map
                    <Class<? extends Rating>, Rating>
                    , Map<Class<? extends Rating>, Rating>>
            , BiFunction
            <Map<Class<? extends Rating>, Rating>
                    , Map<Class<? extends Rating>, Rating>
                    , Map<Class<? extends Rating>, Rating>>> combiners = map();

    public static MetaRatingMerger metaRatingMerger
            (Map<Class<? extends Rating>, Rating> ratings) {
        return new MetaRatingMergerI(ratings);
    }

    protected MetaRatingMergerI(Map<Class<? extends Rating>, Rating> ratings) {
        this.ratings = requireNonNull(ratings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Rating> R combine(Rating... additionalRatings) {
        // TODO Make it immutble.
        final var additionalRatingMap = typeMapping(simplify(additionalRatings));
        return (R) MetaRatingI.metaRating(combiners.entrySet().stream()
                .filter(combiner -> combiner.getKey().test(ratings, additionalRatingMap))
                .findFirst()
                .get()
                .getValue()
                .apply(ratings, additionalRatingMap));
    }

    @Override
    public <T extends Rating> void registerMerger
            (BiPredicate<Map<Class<? extends Rating>, Rating>, Map<Class<? extends Rating>, Rating>> condition
                    , BiFunction<Map<Class<? extends Rating>, Rating>, Map<Class<? extends Rating>, Rating>, Map<Class<? extends Rating>, Rating>> combiner) {
        if (combiners.containsKey(condition)) {
            throw new IllegalArgumentException();
        }
        combiners.put(condition, combiner);
    }

    private java.util.List<Rating> simplify(Rating... ratings) {
        return asList(ratings).stream().flatMap(rating -> {
            if (rating instanceof MetaRating) {
                return ((MetaRating) rating).content().values().stream();
            }
            return Stream.of(rating);
        }).collect(toList());
    }

    @Override
    public <R extends Rating> R _clone() {
        throw notImplementedYet();
    }

    @Override
    public boolean betterThan(Rating rating) {
        return smallerThan(rating);
    }

    @Override
    public Optional<Ordering> compare_partially_to(Rating arg) {
        throw notImplementedYet();
    }

    @Override
    public Element toDom() {
        return Xml.elementWithChildren(MetaRatingMerger.class.getSimpleName());
    }
}
