package net.splitcells.dem;

import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.environment.EnvironmentI;
import net.splitcells.dem.environment.EnvironmentV;
import net.splitcells.dem.lang.Xml;
import net.splitcells.dem.lang.namespace.NameSpaces;
import net.splitcells.dem.resource.host.interaction.Domsole;
import net.splitcells.dem.resource.host.interaction.LogLevel;
import net.splitcells.dem.resource.host.interaction.MessageFilter;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.function.Consumer;

import static net.splitcells.dem.lang.Xml.textNode;
import static net.splitcells.dem.lang.namespace.NameSpaces.DEN;
import static net.splitcells.dem.resource.host.interaction.LogLevel.UNKNOWN_ERROR;
import static net.splitcells.dem.utils.reflection.ClassesRelated.callerClass;

/**
 * This is the starting point of all process definitions.
 * For a process it defines the program that is executed and the environment in which it is executed.
 * <p/>
 * One of the main goals is to make the environment of a program as side effect free as possible.
 * This framework does not try to enforce freedom of side effects on the language level,
 * as this is not possible.
 * It just gives tools in order to minimize side effects.
 * This is done, by having 1 and only one variable representing the state of the environment
 * and passing it through everywhere.
 * <p/>
 */
public final class Dem {
    /**
     * Currently it would be enough to use a static variable.
     * Thread locals are required in order to implement a tree of programs as a cactus stack (https://wiki.c2.com/?CactusStack).
     * It generally allows to execute multiple instances of a Dem program, without having interference between them.
     */
    private static final InheritableThreadLocal<Environment> CURRENT = new InheritableThreadLocal<Environment>();

    public static void process(Runnable program) {
        process(program, m -> {
            // Default configured is not changed.
        });
    }

    /**
     * Defines and executes a program.
     * <p>
     * TODO Support stacking.
     * <p>
     * TODO Support cactus stacking.
     */
    public static void process(Runnable program, Consumer<Environment> configurator) {
        Thread root = new Thread(() -> {
            initializeProcess(program.getClass(), configurator);
            try {
                // TOFIX Does not write log file on short programs that throws an exception.
                program.run();
            } catch (Throwable t) {
                // TOFIX Additional namespace decleration should not be needed.
                final var error = Xml.rElement(DEN, "error");
                final var stackTrace = Xml.element(DEN, "stack-trace");
                final var errorMessage = Xml.element(DEN, "message");
                {
                    final var stackTraceValue = new ByteArrayOutputStream();
                    t.printStackTrace(new PrintWriter(stackTraceValue));
                    stackTrace.appendChild(textNode(new String(stackTraceValue.toByteArray())));
                }
                errorMessage.appendChild(textNode(t.getMessage()));
                {
                    // TOFIX Error message and stack trace are empty.
                    error.appendChild(errorMessage);
                    error.appendChild(stackTrace);
                }
                Domsole.domsole().append(error, Optional.empty(), LogLevel.CRITICAL);
                throw t;
            } finally {
                environment().close();
                CURRENT.remove();
            }
        });
        // A thread is used in order to not contaminate the current context/process.
        root.start();
        try {
            root.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * REFACTOR name
     */
    private static Environment initializeProcess(Class<?> programRepresentative,
                                                 Consumer<Environment> configurator) {
        final var rVal = EnvironmentI.create(programRepresentative);
        // IDEA Invalidate write access to configuration through down casting after configuration via a wrapper.
        configurator.accept(rVal);
        CURRENT.set(rVal);
        return rVal;
    }

    @Deprecated
    public static EnvironmentV ensuredInitialized(Consumer<Environment> configurator) {
        EnvironmentV rVal;
        if (CURRENT.get() == null) {
            rVal = initializeProcess(callerClass(1), configurator);
        } else {
            rVal = CURRENT.get();
        }
        return rVal;
    }

    private static void configureByEnvironment(Environment dem) {
        if ("true".equals(System.getProperty("net.splitcells.mode.build"))) {
            dem
                    .config()
                    .withConfigValue
                            (MessageFilter.class
                                    , logMessage -> logMessage.priority().greaterThanOrEqual(UNKNOWN_ERROR));
        }
    }

    public static EnvironmentV ensuredInitialized() {
        EnvironmentV rVal;
        if (CURRENT.get() == null) {
            rVal = initializeProcess(callerClass(1), dem -> {
                configureByEnvironment(dem);
            });
        } else {
            rVal = CURRENT.get();
        }
        return rVal;
    }

    /**
     * TODO If the user does not care, how it is initialized he does not care about
     * output. But this only is true for certain output. Logging level should be
     * WARNING by default.
     */
    public static EnvironmentV environment() {
        if (null == CURRENT.get()) {
            return ensuredInitialized();
        }
        return CURRENT.get();
    }
}
