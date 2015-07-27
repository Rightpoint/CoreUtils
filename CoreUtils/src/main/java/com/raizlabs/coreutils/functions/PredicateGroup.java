package com.raizlabs.coreutils.functions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Groups predicates together and enables them to act as one large {@link Predicate}
 * @param <Data> the type of item associated with this {@link PredicateGroup}
 */
public class
        PredicateGroup<Data> implements Predicate<Data> {

    private Map<Predicate<Data>, Boolean> predicateRequiredMap = new HashMap<>();
    private HashSet<Predicate<Data>> predicateSet = new HashSet<>();

    private boolean allPredicatesRequired = false;

    @SafeVarargs
    public PredicateGroup(boolean allPredicatesRequired, Predicate<Data>... optionalPredicates) {
        for (Predicate<Data> predicate : optionalPredicates) {
            addPredicate(predicate);
        }

        setAllPredicatesRequired(allPredicatesRequired);
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
     * @param predicate {@link Predicate} to add to group
     */
    public void addPredicate(Predicate<Data> predicate) {
        predicateSet.add(predicate);
    }

    /**
     * Removes {@link Predicate} from group
     * @param predicate {@link Predicate} to remove from group
     */
    public void removePredicate(Predicate<Data> predicate) {
        predicateSet.remove(predicate);
    }

    /**
     * Checks to see if group contains the indicated {@link Predicate}
     * @param predicate {@link Predicate} to check for
     * @return true if group contains the indicated {@link Predicate}, false otherwise
     */
    public boolean contains(Predicate<Data> predicate) {
        return predicateSet.contains(predicate);
    }

    /**
     * Evaluates the group of predicates using an & or | strategy depending on the allPredicatesRequired
     * variable
     * @param item The item to evaluate.
     * @return true if the item fulfills the predicates, false otherwise
     */
    @Override
    public boolean evaluate(Data item) {

        if (item != null) {
            if (allPredicatesRequired) {
                for (Predicate<Data> predicate : predicateRequiredMap.keySet()) {
                    if (predicate != null) {
                        if (!predicate.evaluate(item)) {
                            return false;
                        }
                    }
                }
                return true;

            } else {
                for (Predicate<Data> predicate : predicateRequiredMap.keySet()) {
                    if (predicate != null) {
                        if (predicate.evaluate(item)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } else {
            return false;
        }
    }
}
