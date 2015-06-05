package com.raizlabs.android.coreutils.functions;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Groups predicates together and enables them to act as one large {@link Predicate}
 * @param <Data> the type of item associated with this {@link PredicateGroup}
 */
public class PredicateGroup<Data> implements Predicate<Data> {

    private Map<Predicate<Data>, Boolean> predicateRequiredMap = new HashMap<>();

    private boolean allPredicatesRequired = false;

    @SafeVarargs
    public PredicateGroup(Predicate<Data>... optionalPredicates) {
        for (Predicate<Data> predicate : optionalPredicates) {
            addPredicate(false, predicate);
        }
    }

    /**
     * @param allPredicatesRequired if true, all contained {@link Predicate} must be true.  If false,
     *                at least one {@link Predicate} can be evaluated to true without failing
     */
    public void setAllPredicatesRequired(boolean allPredicatesRequired) {
        this.allPredicatesRequired = allPredicatesRequired;
    }

    /**
     * Adds {@link Predicate} to group and indicated whether {@link Predicate} is required or not
     * @param isRequired whether the {@link Predicate} is required
     * @param predicate {@link Predicate} to add to group
     */
    public void addPredicate(boolean isRequired, Predicate<Data> predicate) {
        predicateRequiredMap.put(predicate, isRequired);
    }

    /**
     * Removes {@link Predicate} from group
     * @param predicate {@link Predicate} to remove from group
     */
    public void removePredicate(Predicate<Data> predicate) {
        predicateRequiredMap.remove(predicate);
    }

    /**
     * Checks to see if group contains the indicated {@link Predicate}
     * @param tag {@link Predicate} to check for
     * @return true if group contains the indicated {@link Predicate}, false otherwise
     */
    public boolean contains(Predicate<Data> tag) {
        return predicateRequiredMap.containsKey(tag);
    }

    /**
     * Evaluates the group of predicates using an & or | strategy depending on the allPredicatesRequired
     * variable
     * @param item The item to evaluate.
     * @return true if the item fulfills the predicates, false otherwise
     */
    @Override
    public boolean evaluate(Data item) {
        boolean valid = false;

        if (item != null) {
            for (Predicate<Data> predicate : predicateRequiredMap.keySet()) {
                if (predicate != null) {
                    if (predicate.evaluate(item) && (allPredicatesRequired || predicateRequiredMap.get(predicate))) {
                        valid = true;
                    }
                }
            }
        }
        return valid;
    }
}
