package net.splitcells.gel.solution.history.meta;

import static net.splitcells.dem.lang.Xml.textNode;
import static net.splitcells.dem.lang.Xml.toPrettyString;
import static net.splitcells.dem.data.set.map.Maps.map;
import static net.splitcells.gel.common.Language.*;

import java.util.Optional;

import net.splitcells.dem.lang.Xml;
import net.splitcells.dem.lang.dom.Domable;
import net.splitcells.dem.data.set.map.Map;
import org.w3c.dom.Node;

public class MetaDataI implements MetaDataView, MetaDataWriter {
    public static MetaDataI refleksijasDatī() {
        return new MetaDataI();
    }

    private final Map<Class<?>, Object> dati = map();

    private MetaDataI() {
    }

    @Override
    public <A> MetaDataWriter ar(Class<A> tips, A vertība) {
        if (dati.containsKey(tips)) {
            throw new IllegalArgumentException(tips.getName());
        }
        dati.put(tips, vertība);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> value(Class<T> tips) {
        return (Optional<T>) Optional.ofNullable(dati.get(tips));
    }

    @Override
    public String toString() {
        return toPrettyString(toDom());
    }

    @Override
    public Node toDom() {
        final var dom = Xml.element(META_DATA.value());
        dati.forEach((atslēga, vertība) -> {
            final var dati = Xml.element(META_DATA.value());
            final var atslēgasDati = Xml.element(KEY.value());
            atslēgasDati.appendChild(textNode(atslēga.getName()));
            final var vertībasDati = Xml.element(VALUE.value());
            {
                if (vertība instanceof Domable) {
                    vertībasDati.appendChild(((Domable) vertība).toDom());
                } else {
                    vertībasDati.appendChild(textNode(vertība.toString()));
                }
            }
            dom.appendChild(atslēgasDati);
            dom.appendChild(vertībasDati);
        });
        return dom;
    }
}
