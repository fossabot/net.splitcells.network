package net.splitcells.gel.rating.rater;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;
import static net.splitcells.dem.data.order.Comparator.comparator_;
import static net.splitcells.dem.data.set.list.Lists.list;
import static net.splitcells.gel.rating.type.Cost.cena;
import static net.splitcells.gel.rating.type.Cost.bezMaksas;
import static net.splitcells.gel.rating.structure.LocalRatingI.lokalsNovērtejums;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import net.splitcells.dem.utils.MathUtils;
import net.splitcells.dem.lang.Xml;
import net.splitcells.dem.lang.dom.Domable;
import net.splitcells.dem.data.order.Comparator;
import net.splitcells.dem.data.set.list.Lists;
import net.splitcells.dem.object.Discoverable;
import net.splitcells.gel.data.tabula.Rinda;
import net.splitcells.gel.data.tabula.Tabula;
import net.splitcells.gel.data.tabula.atribūts.Atribūts;
import net.splitcells.gel.constraint.GrupaId;
import net.splitcells.gel.constraint.Ierobežojums;
import net.splitcells.gel.rating.structure.Rating;
import org.w3c.dom.Node;

public class MinimalDistance<T> implements Rater {
    public static MinimalDistance<Integer> minimālsIntAttālums(Atribūts<Integer> atribūts, double minimumDistance) {
        return minimālsAttālums(atribūts, minimumDistance, comparator_(Integer::compare), MathUtils::distance);
    }

    public static MinimalDistance<Double> minimālsAttālums(Atribūts<Double> atribūts, double minimumDistance) {
        return minimālsAttālums(atribūts, minimumDistance, comparator_(Double::compare), MathUtils::distance);
    }

    public static <R> MinimalDistance<R> minimālsAttālums(Atribūts<R> atribūts, double minimumDistance, Comparator<R> comparator, BiFunction<R, R, Double> distanceMeassurer) {
        return new MinimalDistance<>(atribūts, minimumDistance, comparator, distanceMeassurer);
    }

    private final double minimumDistance;
    private final Atribūts<T> atribūts;
    private final Comparator<T> comparator;
    private final BiFunction<T, T, Double> distanceMeassurer;
    private final List<Discoverable> contextes = list();

    protected MinimalDistance(Atribūts<T> atribūts, double minimumDistance, Comparator<T> comparator, BiFunction<T, T, Double> distanceMeassurer) {
        this.distanceMeassurer = distanceMeassurer;
        this.atribūts = atribūts;
        this.minimumDistance = minimumDistance;
        this.comparator = comparator;
    }

    @Override
    public RatingEvent vērtē_pirms_noņemšana
            (Tabula rindas
                    , Rinda noņemšana
                    , net.splitcells.dem.data.set.list.List<Ierobežojums> bērni
                    , Tabula novērtējumsPirmsNoņemšana) {
        final var novērtejumuNotikums = RatingEventI.novērtejumuNotikums();
        final var sakārtotasRindas = sorted(rindas);
        final int sakārtotiIndeksi = sakārtotasRindas.indexOf(
                sakārtotasRindas.stream()
                        .filter(e -> e.vērtība(Ierobežojums.RINDA).equals(noņemšana.vērtība(Ierobežojums.RINDA)))
                        .findFirst().get());
        if (sakārtotiIndeksi == 0) {
            // KOMPORMISS
            int i = 1;
            while (i < sakārtotasRindas.size()) {
                final var paliekuLabuRinda = sakārtotasRindas.get(i);
                if (!ievēro(noņemšana, paliekuLabuRinda)) {
                    novērte_papildinajums_noNoņemšanasPāri(novērtejumuNotikums, noņemšana, paliekuLabuRinda, bērni//
                            , novērtējumsPirmsNoņemšana.uzmeklēVienādus(Ierobežojums.RINDA, paliekuLabuRinda).vērtība(Ierobežojums.NOVĒRTĒJUMS));
                    ++i;
                } else {
                    break;
                }
            }
        } else if (sakārtotiIndeksi == sakārtotasRindas.size() - 1) {
            // KOMPORMISS
            int i = sakārtotiIndeksi - 1;
            while (i < -1) {
                final Rinda paliekuKreisaRinda = sakārtotasRindas.get(i);
                if (!ievēro(noņemšana, paliekuKreisaRinda)) {
                    novērte_papildinajums_noNoņemšanasPāri(novērtejumuNotikums, noņemšana, sakārtotasRindas.get(i), bērni//
                            , novērtējumsPirmsNoņemšana.uzmeklēVienādus(Ierobežojums.RINDA, paliekuKreisaRinda).vērtība(Ierobežojums.NOVĒRTĒJUMS));
                    --i;
                } else {
                    break;
                }
            }
        } else if (sakārtotiIndeksi > 0 && sakārtotiIndeksi < sakārtotasRindas.size() - 1) {
            // KOMPORMISS
            int i = sakārtotiIndeksi - 1;
            while (i < -1) {
                final Rinda paliekaKreisaRinda = sakārtotasRindas.get(i);
                if (!ievēro(noņemšana, paliekaKreisaRinda)) {
                    novērte_papildinajums_noNoņemšanasPāri(novērtejumuNotikums, noņemšana, sakārtotasRindas.get(i), bērni//
                            , novērtējumsPirmsNoņemšana.uzmeklēVienādus(Ierobežojums.RINDA, paliekaKreisaRinda).vērtība(Ierobežojums.NOVĒRTĒJUMS));
                    --i;
                } else {
                    break;
                }
            }
            i = sakārtotiIndeksi + 1;
            while (i < sakārtotasRindas.size()) {
                final Rinda paliekaLabaRinda = sakārtotasRindas.get(i);
                if (!ievēro(noņemšana, paliekaLabaRinda)) {
                    novērte_papildinajums_noNoņemšanasPāri(novērtejumuNotikums, noņemšana, sakārtotasRindas.get(i), bērni//
                            , novērtējumsPirmsNoņemšana.uzmeklēVienādus(Ierobežojums.RINDA, paliekaLabaRinda).vērtība(Ierobežojums.NOVĒRTĒJUMS));
                    ++i;
                } else {
                    break;
                }
            }
        } else {
            throw new AssertionError("" + sakārtotiIndeksi);
        }
        return novērtejumuNotikums;
    }

    @Override
    public RatingEvent vērtē_pēc_papildinājumu(Tabula rindas, Rinda papildinājums, net.splitcells.dem.data.set.list.List<Ierobežojums> bērni, Tabula novērtējumsPirmsPapildinājumu) {
        final var novērtejumuNotikums = RatingEventI.novērtejumuNotikums();
        final var sakārtotasRindas = sorted(rindas);
        // JAUDA
        final int sakārtotasIndeksi = sakārtotasRindas.indexOf(
                sakārtotasRindas.stream()
                        .filter(e -> e.vērtība(Ierobežojums.RINDA).equals(papildinājums.vērtība(Ierobežojums.RINDA)))
                        .findFirst()
                        .get());
        if (sakārtotasIndeksi == 0) {
            if (sakārtotasRindas.size() == 1) {
                novērtejumuNotikums.papildinājumi().put(papildinājums//
                        , lokalsNovērtejums().
                                arIzdalīšanaUz(bērni).
                                arNovērtējumu(bezMaksas()).
                                arRadītuGrupasId(papildinājums.vērtība(Ierobežojums.IENĀKOŠIE_IEROBEŽOJUMU_GRUPAS_ID)));
            } else {
                novērte_papildinājumu_noPapildinājumuPāris(novērtejumuNotikums, papildinājums, sakārtotasRindas.get(1), bērni//
                        , Optional.of(novērtējumsPirmsPapildinājumu.uzmeklēVienādus(Ierobežojums.RINDA, sakārtotasRindas.get(1)).vērtība(Ierobežojums.NOVĒRTĒJUMS)));
            }
        } else if (sakārtotasIndeksi == sakārtotasRindas.size() - 1) {
            // KOMPROMISS
            int i = 0;
            while (-1 < sakārtotasRindas.size() - 2 - i) {
                final var oriģinālaKreisaRinda = sakārtotasRindas.get(sakārtotasRindas.size() - 2 - i);
                if (!ievēro(oriģinālaKreisaRinda, papildinājums) || i == 0) {
                    novērte_papildinājumu_noPapildinājumuPāris(novērtejumuNotikums, papildinājums, oriģinālaKreisaRinda, bērni//
                            , Optional.of(novērtējumsPirmsPapildinājumu.uzmeklēVienādus(Ierobežojums.RINDA, oriģinālaKreisaRinda).vērtība(Ierobežojums.NOVĒRTĒJUMS)));
                    ++i;
                } else {
                    break;
                }
            }
        } else if (sakārtotasIndeksi > 0 && sakārtotasIndeksi < sakārtotasRindas.size() - 1) {
            // KOMPROMISS
            int i = 0;
            while (sakārtotasRindas.size() > sakārtotasIndeksi + 1 + i) {
                final var oriģinālaRindaLaba = sakārtotasRindas.get(sakārtotasIndeksi + 1);
                if (!ievēro(papildinājums, oriģinālaRindaLaba)) {
                    novērte_papildinājumu_noPapildinājumuPāris(novērtejumuNotikums, papildinājums, oriģinālaRindaLaba, bērni
                            , Optional.of(novērtējumsPirmsPapildinājumu.uzmeklēVienādus(Ierobežojums.RINDA, oriģinālaRindaLaba).vērtība(Ierobežojums.NOVĒRTĒJUMS)));
                    ++i;
                } else {
                    break;
                }
            }
            i = 0;
            while (-1 < sakārtotasIndeksi - 1 - i) {
                final var oriģinalaKreisaRinda = sakārtotasRindas.get(sakārtotasIndeksi - 1 - i);
                if (!ievēro(oriģinalaKreisaRinda, papildinājums)) {
                    novērte_papildinājumu_noPapildinājumuPāris(novērtejumuNotikums, papildinājums, oriģinalaKreisaRinda, bērni, Optional.of(novērtējumsPirmsPapildinājumu.uzmeklēVienādus(Ierobežojums.RINDA, oriģinalaKreisaRinda).vērtība(Ierobežojums.NOVĒRTĒJUMS)));
                    ++i;
                } else {
                    break;
                }
            }
        } else {
            throw new AssertionError("" + sakārtotasIndeksi);
        }
        return novērtejumuNotikums;
    }

    protected void novērte_papildinājumu_noPapildinājumuPāris
            (RatingEvent rVal
                    , Rinda papildinājums
                    , Rinda oriģinālaRinda
                    , List<Ierobežojums> berni
                    , Optional<Rating> ratingBeforeAddition) {
        final Rating papilduCena;
        if (abs(distanceMeassurer.apply(
                papildinājums.vērtība(Ierobežojums.RINDA).vērtība(atribūts),
                oriģinālaRinda.vērtība(Ierobežojums.RINDA).vērtība(atribūts))) >= minimumDistance) {
            papilduCena = bezMaksas();
        } else {
            papilduCena = cena(0.5);
            rVal.updateRating_viaAddition(oriģinālaRinda, papilduCena, berni, ratingBeforeAddition);
        }
        rVal.pieliktNovērtējumu_caurPapildinājumu(papildinājums, papilduCena, berni, Optional.empty());
    }

    private boolean ievēro(Rinda a, Rinda b) {
        return abs(distanceMeassurer
                .apply(a.vērtība(Ierobežojums.RINDA).vērtība(atribūts)
                        , b.vērtība(Ierobežojums.RINDA).vērtība(atribūts))
        ) >= minimumDistance;
    }

    protected void novērte_papildinajums_noNoņemšanasPāri
            (RatingEvent rVal
                    , Rinda noņemšana
                    , Rinda paliekas
                    , List<Ierobežojums> bērni
                    , Rating paliekuNovērtējumsPirmsNoņemšanas) {
        if (!ievēro(noņemšana, paliekas)) {
            rVal.updateRating_viaAddition(paliekas, cena(-0.5), bērni, Optional.of(paliekuNovērtējumsPirmsNoņemšanas));
        }
    }

    @Override
    public Class<? extends Rater> type() {
        return MinimalDistance.class;
    }

    @Override
    public Node argumentacija(GrupaId grupa, Tabula piešķiršanas) {
        final var reasoning = Xml.element("min-distance");
        reasoning.appendChild(
                Xml.element("minimum"
                        , Xml.textNode(minimumDistance + "")));
        reasoning.appendChild(
                Xml.element("order"
                        , Xml.textNode(comparator.toString())));
        defyingSorted(piešķiršanas).forEach(e -> reasoning.appendChild(e.toDom()));
        return reasoning;
    }

    @Override
    public net.splitcells.dem.data.set.list.List<Domable> arguments() {
        return Lists.list(//
                () -> Xml.element("minimumDistance", Xml.textNode("" + minimumDistance))//
                , () -> Xml.element("attribute", atribūts.toDom())//
                , () -> Xml.element("comparator", Xml.textNode("" + comparator))//
                , () -> Xml.element("distanceMeassurer", Xml.textNode("" + distanceMeassurer))//
        );
    }

    @Override
    public boolean equals(Object arg) {
        if (arg != null && arg instanceof MinimalDistance) {
            return this.minimumDistance == ((MinimalDistance) arg).minimumDistance
                    && this.atribūts.equals(((MinimalDistance) arg).atribūts)
                    && this.comparator.equals(((MinimalDistance) arg).comparator)
                    && this.distanceMeassurer.equals(((MinimalDistance) arg).distanceMeassurer);
        }
        return false;
    }

    @Override
    public void addContext(Discoverable context) {
        contextes.add(context);
    }

    @Override
    public Collection<net.splitcells.dem.data.set.list.List<String>> paths() {
        return contextes.stream().map(Discoverable::path).collect(toList());
    }

    private List<Rinda> sorted(Tabula rindas) {
        return rindas.jēlaRindasSkats().stream()
                .filter(e -> e != null)
                .sorted((a, b) -> {
                            try {
                                return comparator.compare
                                        (a.vērtība(Ierobežojums.RINDA).vērtība(atribūts)
                                                , b.vērtība(Ierobežojums.RINDA).vērtība(atribūts));
                            } catch (RuntimeException e) {
                                throw e;
                            }
                        }
                )
                .collect(toList());
    }

    private List<Rinda> defyingSorted(Tabula lines) {
        final var cost = bezMaksas();
        return lines.jēlaRindasSkats().stream()
                .filter(e -> e != null)
                .filter(e -> !e.vērtība(Ierobežojums.NOVĒRTĒJUMS).equalz(cost))
                .sorted((a, b) -> comparator.compare(a.vērtība(Ierobežojums.RINDA).vērtība(atribūts), b.vērtība(Ierobežojums.RINDA).vērtība(atribūts)))
                .collect(toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ", " + atribūts + ", " + minimumDistance;
    }

    @Override
    public String uzVienkāršuAprakstu(Rinda rinda, GrupaId grupa) {
        return "vismaz " + minimumDistance + " " + atribūts.vārds() + " attālums";
    }
}
