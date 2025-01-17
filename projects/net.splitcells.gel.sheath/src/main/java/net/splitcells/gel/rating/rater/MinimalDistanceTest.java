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
package net.splitcells.gel.rating.rater;

import net.splitcells.dem.data.set.list.Lists;
import net.splitcells.gel.data.database.Databases;
import net.splitcells.gel.solution.optimization.StepType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static net.splitcells.dem.data.set.list.Lists.list;
import static net.splitcells.dem.testing.TestTypes.INTEGRATION_TEST;
import static net.splitcells.gel.constraint.type.Then.then;
import static net.splitcells.gel.data.table.attribute.AttributeI.attribute;
import static net.splitcells.gel.data.table.attribute.AttributeI.integerAttribute;
import static net.splitcells.gel.rating.rater.MinimalDistance.has_minimal_distance_of;
import static net.splitcells.gel.rating.rater.MinimalDistance.minimalDistance;
import static net.splitcells.gel.rating.type.Cost.cost;
import static net.splitcells.gel.rating.type.Cost.noCost;
import static net.splitcells.gel.solution.SolutionBuilder.defineProblem;
import static net.splitcells.gel.solution.optimization.OptimizationEvent.optimizationEvent;
import static net.splitcells.gel.solution.optimization.primitive.LinearInitialization.linearInitialization;
import static org.assertj.core.api.Assertions.assertThat;

public class MinimalDistanceTest {

    @Tag(INTEGRATION_TEST)
    @Test
    public void test_multiple_line_addition_and_removal() {
        final var integer = integerAttribute("integer");
        final var testSubject = defineProblem()
                .withDemandAttributes()
                .withEmptyDemands(3)
                .withSupplyAttributes(integer)
                .withSupplies(list(
                        list(2)
                        , list(2)
                        , list(9)
                ))
                .withConstraint
                        (then(has_minimal_distance_of(integer, 3.0)))
                .toProblem()
                .asSolution();
        testSubject.optimize(linearInitialization());
        testSubject.optimize
                (optimizationEvent
                        (StepType.REMOVAL
                                , testSubject.demands().getLines(1).toLinePointer()
                                , testSubject.supplies().getLines(1).toLinePointer()));
        testSubject.optimize
                (optimizationEvent
                        (StepType.REMOVAL
                                , testSubject.demands().getLines(2).toLinePointer()
                                , testSubject.supplies().getLines(2).toLinePointer()));
        testSubject.optimize
                (optimizationEvent
                        (StepType.ADDITION
                                , testSubject.demands().getLines(1).toLinePointer()
                                , testSubject.supplies().getLines(2).toLinePointer()));
        testSubject.optimize
                (optimizationEvent
                        (StepType.REMOVAL
                                , testSubject.demands().getLines(1).toLinePointer()
                                , testSubject.supplies().getLines(2).toLinePointer()));

    }

    @Test
    public void testRating() {
        final var attribute = attribute(Double.class);
        final var lineSupplier = Databases.database(attribute);
        final var testSubject = then(minimalDistance(attribute, 2));
        {
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).isEmpty();
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
        final var testValues = Lists.list
                (lineSupplier.addTranslated(list(1.0))
                        , lineSupplier.addTranslated(list(3.0))
                        , lineSupplier.addTranslated(list(4.0))
                        , lineSupplier.addTranslated(list(1.0))
                );
        {
            testSubject.register(testValues.get(0));
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).hasSize(1);
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
        {
            testSubject.register(testValues.get(1));
            assertThat(testSubject.lines().size()).isEqualTo(2);
            assertThat(testSubject.lineProcessing().size()).isEqualTo(2);
            assertThat(testSubject.complying()).hasSize(2);
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
        {
            testSubject.register(testValues.get(2));
            assertThat(testSubject.defying()).hasSize(2);
            assertThat(testSubject.complying()).hasSize(1);
            assertThat(testSubject.rating()).isEqualTo(cost(1));
        }
        {
            testSubject.register(testValues.get(3));
            assertThat(testSubject.complying()).isEmpty();
            assertThat(testSubject.defying()).hasSize(4);
            assertThat(testSubject.rating()).isEqualTo(cost(3));
        }
        {
            testSubject.register_before_removal(testValues.get(0));
            assertThat(testSubject.defying()).hasSize(2);
            assertThat(testSubject.complying()).hasSize(1);
            assertThat(testSubject.rating()).isEqualTo(cost(1));
        }
        {
            testSubject.register_before_removal(testValues.get(1));
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).hasSize(2);
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
        {
            testSubject.register_before_removal(testValues.get(2));
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).hasSize(1);
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
        {
            testSubject.register_before_removal(testValues.get(3));
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).isEmpty();
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
    }

    @Test
    public void test_simple_neighbour_defiance() {
        final var attribute = attribute(Double.class);
        final var lineProducer = Databases.database(attribute);
        final var testSubject = then(minimalDistance(attribute, 2));
        {
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).isEmpty();
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
        final var testValues = list
                (lineProducer.addTranslated(list(1.0))
                        , lineProducer.addTranslated(list(1.0))
                        , lineProducer.addTranslated(list(1.0))
                );
        {
            testSubject.register(testValues.get(0));
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).hasSize(1);
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
        {
            testSubject.register(testValues.get(1));
            assertThat(testSubject.complying()).isEmpty();
            assertThat(testSubject.defying()).hasSize(2);
            assertThat(testSubject.rating()).isEqualTo(cost(2));
        }
        {
            testSubject.register(testValues.get(2));
            assertThat(testSubject.defying()).hasSize(3);
            assertThat(testSubject.complying()).isEmpty();
            assertThat(testSubject.rating()).isEqualTo(cost(6));
        }
        {
            testSubject.register_before_removal(testValues.get(0));
            assertThat(testSubject.defying()).hasSize(2);
            assertThat(testSubject.complying()).isEmpty();
            assertThat(testSubject.rating()).isEqualTo(cost(2));
        }
        {
            testSubject.register_before_removal(testValues.get(1));
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).hasSize(1);
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
        {
            testSubject.register_before_removal(testValues.get(2));
            assertThat(testSubject.defying()).isEmpty();
            assertThat(testSubject.complying()).isEmpty();
            assertThat(testSubject.rating()).isEqualTo(noCost());
        }
    }
}
