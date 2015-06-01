package com.raizlabs.android.coreutils.functions;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Groups predicates together and enables them to act as one large {@link Predicate}
 * @param <Data>
 */
public class PredicateGroup<Data> implements Predicate<Data> {

    private Set<Predicate<Data>> predicates = new LinkedHashSet<>();

    private Map<Predicate<Data>, Boolean> predicateRequiredMap = new HashMap<>();

    private boolean allTrue = false;

    @SafeVarargs
    public PredicateGroup(Predicate<Data>... optionalPredicates) {
        for (Predicate<Data> predicate : optionalPredicates) {
            addPredicate(false, predicate);
        }
    }

    /**
     * @param allTrue if true, alll contained {@link Predicate} must be true.  If false,
     *                at least one {@link Predicate} can be evaluated to true without failing
     */
    public void setAllTrue(boolean allTrue) {
        this.allTrue = allTrue;
    }

    public void addPredicate(boolean isRequired, Predicate<Data> predicate) {
        predicates.add(predicate);
        predicateRequiredMap.put(predicate, isRequired);
    }

    public void removePredicate(Predicate<Data> predicate) {
        predicates.remove(predicate);
        predicateRequiredMap.remove(predicate);
    }

    public boolean contains(Object tag) {
        return predicates.contains(tag);
    }

    @Override
    public boolean evaluate(Data item) {
        boolean valid = false;

        if (item != null) {
            for (Predicate<Data> predicate : predicates) {
                if (predicate != null) {
                    valid = predicate.evaluate(item);

                    if (!valid && predicateRequiredMap.get(predicate)) {
                        break;
                    }
                }

                if (allTrue) {
                    if (!valid) {
                        break;
                    }
                }
            }
        }
        return valid;
    }
}
