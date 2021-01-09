package net.splitcells.gel.rating.rater;

import static net.splitcells.dem.utils.Not_implemented_yet.not_implemented_yet;

import net.splitcells.gel.data.table.Rinda;
import net.splitcells.gel.data.table.Tabula;
import net.splitcells.gel.constraint.GrupaId;
import net.splitcells.gel.constraint.Ierobežojums;
import net.splitcells.gel.common.Vārdi;
import org.w3c.dom.Node;
import net.splitcells.dem.lang.Xml;
import net.splitcells.dem.lang.dom.Domable;
import net.splitcells.dem.data.set.list.List;
import net.splitcells.dem.object.DiscoverableFromMultiplePathsSetter;
import net.splitcells.dem.utils.reflection.PubliclyConstructed;
import net.splitcells.dem.utils.reflection.PubliclyTyped;

public interface Rater extends PubliclyTyped<Rater>
        , PubliclyConstructed<Domable>
        , DiscoverableFromMultiplePathsSetter
        , Domable {
    RatingEvent vērtē_pēc_papildinājumu(Tabula rindas, Rinda papildinājums, List<Ierobežojums> bērni, Tabula novērtējumsPirmsPapildinājumu);

    /**
     * @param rindas
     * @param noņemšana
     * @param bērni
     * @param novērtējumsPirmsNoņemšana
     * @return
     * @see Ierobežojums#rēgistrē_pirms_noņemšanas(Rinda)
     */
    @Deprecated
    RatingEvent vērtē_pirms_noņemšana(Tabula rindas, Rinda noņemšana, List<Ierobežojums> bērni, Tabula novērtējumsPirmsNoņemšana);

    @Override
    default Class<? extends Rater> type() {
        return Rater.class;
    }

    default Node argumentacija(GrupaId grupa, Tabula piešķiršanas) {
        throw not_implemented_yet(getClass().getName());
    }

    @Override
    default Node toDom() {
        final var dom = Xml.element(getClass().getSimpleName());
        if (!arguments().isEmpty()) {
            dom.appendChild(Xml.element2(Vārdi.ARGUMENTI, arguments().stream().map(arg -> arg.toDom())));
        }
        return dom;
    }

    default String uzVienkāršuAprakstu(Rinda rinda, GrupaId grupa) {
        throw not_implemented_yet(getClass().getName());
    }
}
