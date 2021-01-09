package net.splitcells.gel.data.allocation;

import static java.util.Objects.requireNonNull;
import static net.splitcells.dem.lang.Xml.element;
import static net.splitcells.dem.lang.Xml.textNode;
import static net.splitcells.dem.utils.Not_implemented_yet.not_implemented_yet;
import static net.splitcells.dem.data.set.Sets.setOfUniques;
import static net.splitcells.dem.data.set.list.Lists.concat;
import static net.splitcells.dem.data.set.list.Lists.list;
import static net.splitcells.dem.data.set.list.Lists.listWithValuesOf;
import static net.splitcells.dem.data.set.map.Maps.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Map;
import java.util.Set;

import net.splitcells.dem.data.set.list.List;
import net.splitcells.dem.data.set.list.ListView;
import net.splitcells.gel.data.table.Line;
import net.splitcells.gel.data.table.attribute.Attribute;
import net.splitcells.gel.data.table.column.Column;
import net.splitcells.gel.data.table.column.ColumnView;
import org.w3c.dom.Element;
import net.splitcells.gel.data.database.AfterAdditionSubscriber;
import net.splitcells.gel.data.database.Database;
import net.splitcells.gel.data.database.DatabaseI;
import net.splitcells.gel.data.database.BeforeRemovalSubscriber;

public class AllocationsI implements Allocations {
    protected final String vārds;
    protected final Database piešķiršanas;

    protected final List<AfterAdditionSubscriber> papildinājumsKlausītājs = list();
    protected final List<BeforeRemovalSubscriber> primsNoņemšanaAbonēšanas = list();
    protected final List<BeforeRemovalSubscriber> pēcNoņemšanaAbonēšanas = list();

    protected final Database piedāvājumi;
    protected final Database piedāvājumi_lietoti;
    protected final Database piedāvājumi_nelietoti;

    protected final Database prāsibas;
    protected final Database prāsibas_lietoti;
    protected final Database prāsibas_nelietoti;

    protected final Map<Integer, Integer> piešķiršanasIndekss_uz_lietotuPrāsibuIndekss = map();
    protected final Map<Integer, Integer> piešķiršanasIndekss_uz_lietotuPiedāvājumuIndekss = map();

    protected final Map<Integer, Set<Integer>> lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu = map();
    protected final Map<Integer, Set<Integer>> lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu = map();

    protected final Map<Integer, Set<Integer>> lietotasPrāsibuIndekss_uz_lietotuPiedāvājumuIndekssu = map();
    protected final Map<Integer, Set<Integer>> lietotasPiedāvājumuIndekss_uz_lietotuPrāsibuIndekssu = map();

    @Deprecated
    protected AllocationsI(String vārds, Database prasības, Database piedāvājumi) {
        this.vārds = vārds;
        piešķiršanas = new DatabaseI("piešķiršanas", this, concat(prasības.headerView(), piedāvājumi.headerView()));
        // DARĪT Noņemiet kodu un komentāru dublēšanos.
        {
            this.prāsibas = prasības;
            prāsibas_nelietoti = new DatabaseI("prasības_nelietoti", this, prasības.headerView());
            prāsibas_lietoti = new DatabaseI("prasības_lietoti", this, prasības.headerView());
            prasības.jēlaRindasSkats().forEach(prāsibas_nelietoti::pielikt);
            prasības.abonē_uz_papildinājums(prāsibas_nelietoti::pielikt);
            prasības.abonē_uz_iepriekšNoņemšana(removalOf -> {
                if (lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.containsKey(removalOf.indekss())) {
                    listWithValuesOf(
                            lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.get(removalOf.indekss()))
                            .forEach(allocation_of_demand -> noņemt(piešķiršanas.jēlaRindasSkats().get(allocation_of_demand)));
                }
                if (prāsibas_nelietoti.satur(removalOf)) {
                    prāsibas_nelietoti.noņemt(removalOf);
                }
                // SALABOT Vai alternatīvā gadījumā būtu jādara kaut kas cits.
                if (prāsibas_lietoti.satur(removalOf)) {
                    prāsibas_lietoti.noņemt(removalOf);
                }
            });
        }
        {
            this.piedāvājumi = requireNonNull(piedāvājumi);
            piedāvājumi_nelietoti = new DatabaseI("piedāvājumi_nelietoti", this, piedāvājumi.headerView());
            piedāvājumi_lietoti = new DatabaseI("piedāvājumi_lietoti", this, piedāvājumi.headerView());
            piedāvājumi.jēlaRindasSkats().forEach(piedāvājumi_nelietoti::pielikt);
            piedāvājumi.abonē_uz_papildinājums(i -> {
                piedāvājumi_nelietoti.pielikt(i);
            });
            piedāvājumi.abonē_uz_iepriekšNoņemšana(noņemšanaNo -> {
                if (lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.containsKey(noņemšanaNo.indekss())) {
                    listWithValuesOf
                            (lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.get(noņemšanaNo.indekss()))
                            .forEach(piešķiršanas_no_piedāvāijumu
                                    -> noņemt(piešķiršanas.jēlaRindasSkats().get(piešķiršanas_no_piedāvāijumu)));
                }
                if (piedāvājumi_nelietoti.satur(noņemšanaNo)) {
                    piedāvājumi_nelietoti.noņemt(noņemšanaNo);
                }
                // SALABOT Vai alternatīvā gadījumā būtu jādara kaut kas cits.
                if (piedāvājumi_lietoti.satur(noņemšanaNo)) {
                    piedāvājumi_lietoti.noņemt(noņemšanaNo);
                }
            });
        }
    }

    @Override
    public Database piedāvājums() {
        return piedāvājumi;
    }

    @Override
    public Database piedāvājumi_lietoti() {
        return piedāvājumi_lietoti;
    }

    @Override
    public Database supplies_unused() {
        return piedāvājumi_nelietoti;
    }

    @Override
    public Database demands() {
        return prāsibas;
    }

    @Override
    public Database prasība_lietots() {
        return prāsibas_lietoti;
    }

    @Override
    public Database demands_unused() {
        return prāsibas_nelietoti;
    }

    @Override
    public Line piešķirt(Line prasība, Line piedāvājums) {
        final var piešķiršana = piešķiršanas.pieliktUnPārtulkot(Line.saķēdet(prasība, piedāvājums));
        if (!lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.containsKey(piedāvājums.indekss())) {
            piedāvājumi_lietoti.pielikt(piedāvājums);
            piedāvājumi_nelietoti.noņemt(piedāvājums);
        }
        if (!lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.containsKey(prasība.indekss())) {
            prāsibas_lietoti.pielikt(prasība);
            prāsibas_nelietoti.noņemt(prasība);
        }
        {
            piešķiršanasIndekss_uz_lietotuPrāsibuIndekss.put(piešķiršana.indekss(), prasība.indekss());
            piešķiršanasIndekss_uz_lietotuPiedāvājumuIndekss.put(piešķiršana.indekss(), piedāvājums.indekss());
        }
        {
            {
                if (!lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.containsKey(prasība.indekss())) {
                    lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.put(prasība.indekss(), setOfUniques());
                }
                lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.get(prasība.indekss()).add(piešķiršana.indekss());
                if (!lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.containsKey(piedāvājums.indekss())) {
                    lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.put(piedāvājums.indekss(), setOfUniques());
                }
                lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.get(piedāvājums.indekss()).add(piešķiršana.indekss());
            }
        }
        {
            {
                if (!lietotasPrāsibuIndekss_uz_lietotuPiedāvājumuIndekssu.containsKey(prasība.indekss())) {
                    lietotasPrāsibuIndekss_uz_lietotuPiedāvājumuIndekssu.put(prasība.indekss(), setOfUniques());
                }
                lietotasPrāsibuIndekss_uz_lietotuPiedāvājumuIndekssu.get(prasība.indekss()).add(piedāvājums.indekss());
            }
            {
                if (!lietotasPiedāvājumuIndekss_uz_lietotuPrāsibuIndekssu.containsKey(piedāvājums.indekss())) {
                    lietotasPiedāvājumuIndekss_uz_lietotuPrāsibuIndekssu.put(piedāvājums.indekss(), setOfUniques());
                }
                lietotasPiedāvājumuIndekss_uz_lietotuPrāsibuIndekssu.get(piedāvājums.indekss()).add(prasība.indekss());
            }
        }
        papildinājumsKlausītājs.forEach(listener -> listener.reģistrē_papildinājumi(piešķiršana));
        return piešķiršana;
    }

    @Override
    public Line prasība_no_piešķiršana(Line piešķiršana) {
        return prāsibas.jēlaRindasSkats()
                .get(piešķiršanasIndekss_uz_lietotuPrāsibuIndekss.get(piešķiršana.indekss()));
    }

    @Override
    public Line piedāvājums_no_piešķiršana(Line allocation) {
        return piedāvājumi.jēlaRindasSkats()
                .get(piešķiršanasIndekss_uz_lietotuPiedāvājumuIndekss.get(allocation.indekss()));
    }

    @Override
    public Line pieliktUnPārtulkot(List<?> vertības) {
        throw not_implemented_yet();
    }

    @Override
    public Line pielikt(Line rinda) {
        throw not_implemented_yet();
    }

    @Override
    public void noņemt(Line piešķiršana) {
        final var prasība = prasība_no_piešķiršana(piešķiršana);
        final var piedāvājums = piedāvājums_no_piešķiršana(piešķiršana);
        primsNoņemšanaAbonēšanas.forEach(pirmsNoņemšanasKlausītājs -> pirmsNoņemšanasKlausītājs.rēgistrē_pirms_noņemšanas(piešķiršana));
        piešķiršanas.noņemt(piešķiršana);
        // TODO Make following code a remove subscription to allocations.
        {
            piešķiršanasIndekss_uz_lietotuPrāsibuIndekss.remove(piešķiršana.indekss());
            piešķiršanasIndekss_uz_lietotuPiedāvājumuIndekss.remove(piešķiršana.indekss());
        }
        {
            {
                lietotasPrāsibuIndekss_uz_lietotuPiedāvājumuIndekssu.get(prasība.indekss()).remove(piedāvājums.indekss());
                if (lietotasPrāsibuIndekss_uz_lietotuPiedāvājumuIndekssu.get(prasība.indekss()).isEmpty()) {
                    lietotasPrāsibuIndekss_uz_lietotuPiedāvājumuIndekssu.remove(prasība.indekss());
                }
                lietotasPiedāvājumuIndekss_uz_lietotuPrāsibuIndekssu.get(piedāvājums.indekss()).remove(prasība.indekss());
                if (lietotasPiedāvājumuIndekss_uz_lietotuPrāsibuIndekssu.get(piedāvājums.indekss()).isEmpty()) {
                    lietotasPiedāvājumuIndekss_uz_lietotuPrāsibuIndekssu.remove(piedāvājums.indekss());
                }
            }
            {
                lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.get(piedāvājums.indekss()).remove(piešķiršana.indekss());
                if (lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.get(piedāvājums.indekss()).isEmpty()) {
                    lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu.remove(piedāvājums.indekss());
                }
                lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.get(prasība.indekss()).remove(piešķiršana.indekss());
                if (lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.get(prasība.indekss()).isEmpty()) {
                    lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu.remove(prasība.indekss());
                }
            }
        }
        piešķiršanasIndekss_uz_lietotuPrāsibuIndekss.remove(piešķiršana.indekss());
        piešķiršanasIndekss_uz_lietotuPiedāvājumuIndekss.remove(piešķiršana.indekss());
        if (!lietotasPrāsibuIndekss_uz_lietotuPiedāvājumuIndekssu.containsKey(prasība.indekss())) {
            prāsibas_lietoti.noņemt(prasība);
            prāsibas_nelietoti.pielikt(prasība);
        }
        if (!lietotasPiedāvājumuIndekss_uz_lietotuPrāsibuIndekssu.containsKey(piedāvājums.indekss())) {
            piedāvājumi_lietoti.noņemt(piedāvājums);
            piedāvājumi_nelietoti.pielikt(piedāvājums);
        }
        pēcNoņemšanaAbonēšanas.forEach(listener -> listener.rēgistrē_pirms_noņemšanas(piešķiršana));
    }

    @Override
    public void abonē_uz_papildinājums(AfterAdditionSubscriber klausītājs) {
        papildinājumsKlausītājs.add(klausītājs);
    }

    @Override
    public List<Attribute<Object>> headerView() {
        return piešķiršanas.headerView();
    }

    @Override
    public <T> ColumnView<T> kolonnaSkats(Attribute<T> atribūts) {
        return piešķiršanas.kolonnaSkats(atribūts);
    }

    @Override
    public ListView<Line> jēlaRindasSkats() {
        return piešķiršanas.jēlaRindasSkats();
    }

    @Override
    public void abonē_uz_iepriekšNoņemšana(BeforeRemovalSubscriber pirmsNoņemšanasKlausītājs) {
        primsNoņemšanaAbonēšanas.add(pirmsNoņemšanasKlausītājs);
    }

    @Override
    public int size() {
        return piešķiršanas.size();
    }

    @Override
    public void noņemt(int rindasIndekss) {
        try {
            noņemt(piešķiršanas.jēlaRindasSkats().get(rindasIndekss));
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void abonē_uz_pēcNoņemšana(BeforeRemovalSubscriber pirmsNoņemšanasKlausītājs) {
        pēcNoņemšanaAbonēšanas.add(pirmsNoņemšanasKlausītājs);
    }

    @Override
    public Set<Line> piešķiršanas_no_piedāvājuma(Line piedāvājums) {
        final Set<Line> piešķiršanas_no_piedāvājuma = setOfUniques();
        try {
            lietotasPiedāvājumuIndekss_uz_piešķiršanasIndekssu
                    .get(piedāvājums.indekss())
                    .forEach(piešķiršanasIndekss ->
                            piešķiršanas_no_piedāvājuma.add(piešķiršanas.jēlaRindasSkats().get(piešķiršanasIndekss)));
        } catch (RuntimeException e) {
            throw e;
        }
        return piešķiršanas_no_piedāvājuma;
    }

    @Override
    public Set<Line> piešķiršanas_no_prasības(Line prasība) {
        final Set<Line> piešķiršanas_no_prasības = setOfUniques();
        lietotasPrāsibasIndekss_uz_piešķiršanasIndekssu
                .get(prasība.indekss())
                .forEach(piešķiršanasIndekss ->
                    piešķiršanas_no_prasības.add(piešķiršanas.jēlaRindasSkats().get(piešķiršanasIndekss)));
        return piešķiršanas_no_prasības;
    }

    @Override
    public List<Column<Object>> kolonnaSkats() {
        return piešķiršanas.kolonnaSkats();
    }

    @Override
    public String toString() {
        return Allocations.class.getSimpleName() + path().toString();
    }

    @Override
    public net.splitcells.dem.data.set.list.List<String> path() {
        final net.splitcells.dem.data.set.list.List<String> path = list();
        path.addAll(prāsibas.path());
        path.add(vārds);
        return path;
    }

    @Override
    public Element toDom() {
        final var dom = element(Allocations.class.getSimpleName());
        dom.appendChild(textNode(path().toString()));
        jēlaRindasSkats().stream()
                .filter(rinda -> rinda != null)
                .forEach(rinda -> dom.appendChild(rinda.toDom()));
        return dom;
    }

    @Override
    public List<Line> jēlasRindas() {
        throw not_implemented_yet();
    }

    @Override
    public Line uzmeklēVienādus(Attribute<Line> atribūts, Line cits) {
        return piešķiršanas.uzmeklēVienādus(atribūts, cits);
    }
}
