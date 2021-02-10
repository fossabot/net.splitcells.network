package net.splitcells.dem.resource.communication.interaction;

import net.splitcells.dem.environment.config.ProgramName;
import net.splitcells.dem.data.set.SetWA;
import net.splitcells.dem.data.set.list.ListWA;
import net.splitcells.dem.lang.Xml;
import net.splitcells.dem.lang.dom.Domable;
import net.splitcells.dem.object.Discoverable;
import net.splitcells.dem.resource.communication.Flushable;
import net.splitcells.dem.resource.communication.Sender;
import net.splitcells.dem.resource.host.interaction.LogLevel;
import net.splitcells.dem.resource.host.interaction.LogMessage;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static net.splitcells.dem.Dem.environment;
import static net.splitcells.dem.lang.Xml.*;
import static net.splitcells.dem.lang.namespace.NameSpaces.DEN;
import static net.splitcells.dem.object.Discoverable.NO_CONTEXT;
import static net.splitcells.dem.resource.host.interaction.LogMessageI.logMessage;
import static net.splitcells.dem.utils.Not_implemented_yet.not_implemented_yet;

/**
 * IDEA Support recursive stacking.
 * <p>
 * TOFIX Remove duplicate name space declaration.
 * <p>
 * DSUI ^= Dom Stream and Stack based User Interface
 */
public class Dsui implements Sui<LogMessage<Node>>, Flushable {
    private static final String ENTRY_POINT = "ENTRY.POINT.237048021";

    public static Dsui dsui(Sender<String> output, Predicate<LogMessage<Node>> messageFilter) {
        Element execution = element(//
                rElement(DEN, "execution"), //
                element(DEN, "name", environment().config().configValue(ProgramName.class)), //
                textNode(ENTRY_POINT));
        return dsui(output, execution, messageFilter);
    }

    private static Dsui dsui(Sender<String> output, Element root, Predicate<LogMessage<Node>> messageFilter) {
        return new Dsui(output, root, messageFilter);
    }

    private final Sender<String> baseOutput;
    private final Sender<String> contentOutput;
    private final Element root;
    private final Predicate<LogMessage<Node>> messageFilter;
    private boolean isClosed = false;

    public Dsui(Sender<String> output, Element root, Predicate<LogMessage<Node>> messageFilter) {
        this.messageFilter = messageFilter;
        baseOutput = requireNonNull(output);
        this.root = requireNonNull(root);
        {
            // HACK
            String tmp = Xml.toDocumentString(root);
            if (!tmp.contains(Dsui.ENTRY_POINT)) {
                throw new IllegalArgumentException(tmp);
            }
            // FIXME Remove last line if only whitespace.
            baseOutput.append(tmp.split(Dsui.ENTRY_POINT)[0]);
        }
        contentOutput = Sender.extend(baseOutput, "   ", "");
    }

    public <R extends ListWA<LogMessage<Node>>> R append(Node domable, Optional<Discoverable> context,
                                                         LogLevel logLevel) {
        return append(logMessage(domable, context.orElse(NO_CONTEXT), logLevel));
    }

    public <R extends ListWA<LogMessage<Node>>> R append(Node domable, Discoverable context, LogLevel logLevel) {
        return append(logMessage(domable, context, logLevel));
    }

    public <R extends ListWA<LogMessage<Node>>> R append(Domable domable, Discoverable context, LogLevel logLevel) {
        return append(domable.toDom(), context, logLevel);
    }

    public <R extends ListWA<LogMessage<Node>>> R append(Domable domable, Optional<Discoverable> context,
                                                         LogLevel logLevel) {
        return append(domable.toDom(), context, logLevel);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public <R extends ListWA<LogMessage<Node>>> R append(LogMessage<Node> arg) {
        if (messageFilter.test(arg)) {
            asList(
                    Xml.toPrettyWithoutHeaderString(arg.content())
                            .split("\\R"))
                    .forEach(contentOutput::append);
        }
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public <R extends ListWA<LogMessage<Node>>> R append(String text) {
        // HACK
        contentOutput.append(text);
        return (R) this;
    }

    @Override
    public void close() {
        if (isClosed) {
            throw new IllegalStateException();
        }
        String endingMessage = Xml.toPrettyString(root);
        if (!endingMessage.contains(Dsui.ENTRY_POINT)) {
            throw new IllegalArgumentException(endingMessage);
        }
        baseOutput.append(endingMessage.split(Dsui.ENTRY_POINT)[1]);

        contentOutput.flush();
        contentOutput.close();

        isClosed = true;
    }

    @Override
    public void flush() {
        contentOutput.flush();
    }

    @Override
    public <R extends SetWA<LogMessage<Node>>> R add(LogMessage<Node> value) {
        throw not_implemented_yet();
    }

}