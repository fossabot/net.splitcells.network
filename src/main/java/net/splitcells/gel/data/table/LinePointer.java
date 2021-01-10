package net.splitcells.gel.data.table;

import net.splitcells.dem.lang.dom.Domable;
import org.w3c.dom.Node;

import java.util.Optional;

import static net.splitcells.dem.lang.Xml.element;
import static net.splitcells.dem.lang.Xml.textNode;

public interface LinePointer extends Domable {
    Table konteksts();

    int indekss();

    default Optional<Line> interpret() {
        return interpretē(konteksts());
    }

    @Deprecated
    Optional<Line> interpretē(Table context);

    @Override
    default Node toDom() {
        final var dom = element(LinePointer.class.getSimpleName());
        final var rinda = interpret();
        if (rinda.isPresent()) {
            dom.appendChild(rinda.get().toDom());
        } else {
            dom.appendChild(element("indekss", textNode(indekss() + "")));
        }
        return dom;
    }
}
