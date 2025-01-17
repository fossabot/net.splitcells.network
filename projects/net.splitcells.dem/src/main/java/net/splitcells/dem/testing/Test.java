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
package net.splitcells.dem.testing;

import net.splitcells.dem.Dem;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import static net.splitcells.dem.testing.FailureDetector.failureDetector;
import static net.splitcells.dem.testing.LiveReporter.liveReporter;
import static net.splitcells.dem.testing.TestTypes.FUNCTIONAL_TEST;
import static net.splitcells.dem.testing.TestTypes.INTEGRATION_TEST;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
import static org.junit.platform.launcher.TagFilter.includeTags;

/**
 * Executes any test by default.
 * This does not filter tests by names like Maven by default.
 * If a developer wrote a test, than it most likely wants to also execute the test, if it is not stated otherwise.
 * Tests are also not filtered, because the developer might not know, that such a thing happens.
 * He needs at least a warning for such things during test execution.
 * <p>
 * TODO Simplify code by removing duplicate code.
 */
public class Test {
    public static void main(String... arg) {
        if (!test()) {
            System.exit(1);
        }
    }

    public static boolean testFunctionality() {
        // TODO REMOVE
        System.setProperty("net.splitcells.mode.build", "true");
        Dem.ensuredInitialized();
        final var testDiscovery = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(""))
                .filters(includeTags(FUNCTIONAL_TEST))
                .build();
        final var testExecutor = LauncherFactory.create();
        final var failureDetector = failureDetector();
        testExecutor.discover(testDiscovery);
        testExecutor.execute(testDiscovery, liveReporter(), failureDetector);
        return !failureDetector.hasWatchedErrors();
    }

    public static boolean testIntegration() {
        // TODO REMOVE
        System.setProperty("net.splitcells.mode.build", "true");
        Dem.ensuredInitialized();
        final var testDiscovery = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(""))
                .filters(includeTags(INTEGRATION_TEST))
                .build();
        final var testExecutor = LauncherFactory.create();
        final var failureDetector = failureDetector();
        testExecutor.discover(testDiscovery);
        testExecutor.execute(testDiscovery, liveReporter(), failureDetector);
        return !failureDetector.hasWatchedErrors();
    }

    public static boolean testUnits() {
        // TODO REMOVE
        System.setProperty("net.splitcells.mode.build", "true");
        Dem.ensuredInitialized();
        final var testDiscovery = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(""))
                .filters(includeTags("none() | " + TestTypes.UNIT_TEST))
                .build();
        final var testExecutor = LauncherFactory.create();
        final var failureDetector = failureDetector();
        testExecutor.discover(testDiscovery);
        testExecutor.execute(testDiscovery, liveReporter(), failureDetector);
        return !failureDetector.hasWatchedErrors();
    }

    public static boolean test() {
        // TODO REMOVE
        System.setProperty("net.splitcells.mode.build", "true");
        Dem.ensuredInitialized();
        final var testDiscovery = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(""))
                .build();
        final var testExecutor = LauncherFactory.create();
        final var failureDetector = failureDetector();
        testExecutor.discover(testDiscovery);
        testExecutor.execute(testDiscovery, liveReporter(), failureDetector);
        return !failureDetector.hasWatchedErrors();
    }

    public static boolean test(Class<?> type) {
        System.setProperty("net.splitcells.mode.build", "true");
        Dem.ensuredInitialized();
        final var testDiscovery = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(type))
                .build();
        final var testExecutor = LauncherFactory.create();
        final var failureDetector = failureDetector();
        testExecutor.discover(testDiscovery);
        testExecutor.execute(testDiscovery, liveReporter(), failureDetector);
        return !failureDetector.hasWatchedErrors();
    }

    public static boolean testMethod(Class<?> type, String methodeName) {
        System.setProperty("net.splitcells.mode.build", "true");
        Dem.ensuredInitialized();
        final var testDiscovery = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectMethod(type, methodeName))
                .build();
        final var testExecutor = LauncherFactory.create();
        final var failureDetector = failureDetector();
        testExecutor.discover(testDiscovery);
        testExecutor.execute(testDiscovery, liveReporter(), failureDetector);
        return !failureDetector.hasWatchedErrors();
    }
}
