package net.splitcells.gel.rating.structure;

import static java.util.Arrays.asList;
import static net.splitcells.dem.utils.Not_implemented_yet.not_implemented_yet;
import static net.splitcells.dem.lang.Xml.element;
import static net.splitcells.dem.data.order.Ordering.EQUAL;
import static net.splitcells.dem.data.set.map.Maps.map;
import static net.splitcells.gel.rating.structure.MetaRatingMergerI.metaRatingMerger;
import static net.splitcells.gel.rating.structure.RatingTranslatorI.ratingTranslator;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import net.splitcells.gel.rating.type.Cost;
import net.splitcells.gel.rating.type.Optimality;
import net.splitcells.gel.rating.type.Profit;
import org.w3c.dom.Node;
import net.splitcells.dem.lang.Xml;
import net.splitcells.dem.data.order.Ordering;
import net.splitcells.dem.data.set.map.Map;

public class MetaRatingI implements MetaRating {
    protected final Map<Class<? extends Rating>, Rating> ratingMap;
    protected final RatingTranslator translator;
    protected final MetaRatingMerger merger;

    public static MetaRating metaRating(Map<Class<? extends Rating>, Rating> rating) {
        return new MetaRatingI(rating);
    }

    public static MetaRating metaRating() {
        return new MetaRatingI();
    }

    public static MetaRating metaRating(Rating... ratings) {
        final Map<Class<? extends Rating>, Rating> ratingMap = map();
        asList(ratings).forEach(rating -> ratingMap.put(rating.getClass(), rating));
        final MetaRatingI metaRating = new MetaRatingI(ratingMap);
        return metaRating;
    }

    protected MetaRatingI() {
        this(map());
    }

    @SuppressWarnings("unlikely-arg-type")
    protected MetaRatingI(Map<Class<? extends Rating>, Rating> ratingMap) {
        this.ratingMap = ratingMap;
        translator = ratingTranslator(ratingMap);
        merger = metaRatingMerger(ratingMap);
        /**
         * Combine multiple simple {@link Rating} with ar {@link MetaRating}.
         */
        registerMerger(
                (base, addition) -> base.isEmpty()
                        && addition.size() == 1
                        && addition.values().iterator().next() instanceof MetaRating
                        && ((MetaRating) addition.values().iterator().next()).content().size() == 1
                , (base, addition) -> {
                    MetaRating metaAddition
                            = (MetaRating) addition.values().iterator().next();
                    final Map<Class<? extends Rating>, Rating> metaRating = map();
                    metaRating.put(metaAddition.content().keySet().iterator().next()
                            , metaAddition.content().values().iterator().next());
                    return metaRating;
                }
        );
        /**
         * Combine 2 primitive {@link Rating}.
         */
        registerMerger(
                (base, addition) -> base.size() == 1
                        && addition.size() == 1
                        && !(addition.values().iterator().next() instanceof MetaRating)
                        && !(base.values().iterator().next() instanceof MetaRating)
                , (base, addition) -> {
                    final Map<Class<? extends Rating>, Rating> metaRating = map();
                    final Rating primitiveAddition = base.values().iterator().next()
                            .combine(addition.values().iterator().next());
                    metaRating.put(primitiveAddition.getClass()
                            , primitiveAddition);
                    return metaRating;
                }
        );
        /**
         * Combine primitive {@link Rating} with {@link MetaRating}.
         */
        registerMerger(
                (base, addition) -> base.size() == 1
                        && addition.size() == 1
                        && !(base.values().iterator().next() instanceof MetaRating)
                        && addition.values().iterator().next() instanceof MetaRating
                , (base, addition) -> {
                    final Map<Class<? extends Rating>, Rating> metaRating = map();
                    Rating primitiveAddition = base.values().iterator().next()
                            .combine(((MetaRating)
                                    addition.values().iterator().next()).content().values().iterator().next());
                    metaRating.put(primitiveAddition.getClass()
                            , primitiveAddition);
                    return metaRating;
                }
        );
        /**
         * Combine primitive {@link Rating} with neutral element.
         */
        registerMerger(
                (base, addition) -> base.isEmpty()
                        && addition.size() == 1
                        && !(addition.values().iterator().next() instanceof MetaRating)
                , (base, addition) -> {
                    final Map<Class<? extends Rating>, Rating> metaRating = map();
                    final var ratingClass = addition.keySet().iterator().next();
                    /**
                     * Without usage of {@link net.splitcells.dem.object.DeepCloneable#deepClone}
                     * negative side effects would be created.
                     */
                    metaRating.put(ratingClass, addition.get(ratingClass));
                    return metaRating;
                }
        );
    }

    @Override
    public <R extends Rating> R combine(Rating... additionalRatings) {
        return (R) merger.combine(additionalRatings);
    }

    @Override
    public <R extends Rating> R translate(Class<R> type) {
        if (ratingMap.size() == 1) {
            if (ratingMap.containsKey(Profit.class)) {
                return (R) Profit.profit(getContentValue(Profit.class).value());
            }
        }
        return (R) this;
    }

    @Override
    public <T extends Rating> void registerMerger
            (BiPredicate<Map<Class<? extends Rating>, Rating>, Map<Class<? extends Rating>, Rating>> condition
                    , BiFunction
                     <Map<Class<? extends Rating>, Rating>
                             , Map<Class<? extends Rating>, Rating>
                             , Map<Class<? extends Rating>, Rating>> combiner) {
        this.merger.registerMerger(condition, combiner);
    }

    @Override
    public void registerTranslator
            (Class<? extends Rating> targetType
                    , Predicate<Map<Class<? extends Rating>, Rating>> condition
                    , Function<Map<Class<? extends Rating>, Rating>, Rating> translator) {
        this.translator.registerTranslator(targetType, condition, translator);
    }

    @Override
    public boolean equals(Object arg) {
        if (arg instanceof Rating) {
            return compare_partially_to((Rating) arg).get().equals(EQUAL);
        }
        throw not_implemented_yet();
    }

    @Override
    public Optional<Ordering> compare_partially_to(Rating arg) {
        if (arg instanceof MetaRating) {
            final MetaRating other = (MetaRating) arg;
            if (other.content().isEmpty() && ratingMap.isEmpty()) {
                return Optional.of(EQUAL);
            }
            if (other.content().size() == 1 && ratingMap.size() == 1) {
                return other.content().values().iterator().next()
                        .compare_partially_to(ratingMap.values().iterator().next());
            }
        }
        if (arg instanceof Profit) {
            if (content().containsKey(Profit.class)) {
                return this.getContentValue(Profit.class).compare_partially_to(arg);
            }
            throw not_implemented_yet();
        }
        if (arg instanceof Cost) {
            if (content().containsKey(Cost.class)) {
                return this.getContentValue(Cost.class).compare_partially_to(arg);
            }
            if (content().isEmpty() && 0 == ((Cost) arg).value()) {
                return Optional.of(EQUAL);
            }
            if (content().containsKey(Optimality.class)) {
                return this.getContentValue(Optimality.class).compare_partially_to(arg);
            }
            throw not_implemented_yet();
        }
        if (arg instanceof Optimality) {
            if (content().containsKey(Cost.class)) {
                return this.getContentValue(Cost.class).compare_partially_to(arg);
            }
        }
        throw not_implemented_yet();
    }

    @Override
    public Map<Class<? extends Rating>, Rating> content() {
        return ratingMap;
    }

    @Override
    public <R extends Rating> R _clone() {
        final MetaRatingI clone = new MetaRatingI();
        clone.content().forEach((key, value) -> {
            clone.content().put(key, value._clone());
        });
        return (R) clone;
    }

    @Override
    public boolean betterThan(Rating rating) {
        return smallerThan(rating);
    }

    @Override
    public Node toDom() {
        if (1 == ratingMap.size()) {
            return ratingMap.values().iterator().next().toDom();
        }
        final var dom = element(MetaRating.class.getSimpleName());
        return dom;
    }

    @Override
    public String toString() {
        return Xml.toPrettyString(toDom());
    }
}
