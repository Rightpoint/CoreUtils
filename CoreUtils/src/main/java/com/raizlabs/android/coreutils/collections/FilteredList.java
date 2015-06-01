package com.raizlabs.android.coreutils.collections;

import android.support.annotation.NonNull;

import com.raizlabs.android.coreutils.functions.Predicate;
import com.raizlabs.android.coreutils.functions.PredicateGroup;
import com.raizlabs.android.coreutils.util.observable.lists.ListObserver;
import com.raizlabs.android.coreutils.util.observable.lists.ObservableList;
import com.raizlabs.android.coreutils.util.observable.lists.SimpleListObserver;
import com.raizlabs.android.coreutils.util.observable.lists.SimpleListObserverListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class FilteredList<T> implements ObservableList<T> {

    //region Members
    private ObservableList<T> sourceList;
    private PredicateGroup<T> filters;
    private SimpleListObserver<T> listObserver;

    /**
     * Local list which contains all the items which currently pass our filter
     */
    private final List<T> filteredList;
    //endregion

    /**
     * Constructs a new {@link FilteredList} based off the given source list
     * and filter.
     *
     * @param sourceList The list to be filtered.
     * @param filter     The filter to apply to the list. Returning true keeps the
     *                   item in this list.
     */
    public FilteredList(ObservableList<T> sourceList, Predicate<T> filter) {
        this(sourceList, new PredicateGroup<>(filter));
    }

    /**
     * Constructs a new {@link FilteredList} based off the given source list
     * and filter.
     *
     * @param sourceList The list to be filtered.
     * @param filters    The group of filters to apply to the list. Returning true keeps
     *                   the item in this list if all filters are true.
     */
    public FilteredList(ObservableList<T> sourceList, PredicateGroup<T> filters) {
        this.sourceList = sourceList;
        this.filters = new PredicateGroup<>(filters);
        this.filteredList = new LinkedList<>();
        this.listObserver = new SimpleListObserver<>();
        new SourceListListener<>(this);
        update();
    }

    protected void addFilter(Predicate<T> filter) {
        this.filters.addPredicate(true, filter);
        update();
    }

    protected void removeFilter(Predicate<T> filter) {
        this.filters.removePredicate(filter);
        update();
    }

    //region Inherited Methods
    @Override
    public ListObserver<T> getListObserver() {
        return listObserver;
    }

    @Override
    public void beginTransaction() {
        sourceList.beginTransaction();
    }

    @Override
    public void endTransaction() {
        sourceList.endTransaction();
    }

    @Override
    public void add(int location, T object) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support add(int, T)");
    }

    @Override
    public boolean add(T object) {
        return sourceList.add(object);
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support addAll(int, Collection)");
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return sourceList.addAll(collection);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support clear()");
    }

    @Override
    public boolean contains(Object object) {
        return filteredList.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return filteredList.containsAll(collection);
    }

    @Override
    public T get(int location) {
        synchronized (filteredList) {
            return filteredList.get(location);
        }
    }

    @Override
    public int indexOf(Object object) {
        return filteredList.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return filteredList.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return filteredList.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return filteredList.lastIndexOf(object);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support listIterator()");
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int location) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support listIterator(int)");
    }

    @Override
    public T remove(int location) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support remove(int)");
    }

    @Override
    public boolean remove(Object object) {
        if (contains(object)) {
            return sourceList.remove(object);
        } else {
            // Don't remove it if it doesn't exist in this list
            // Even if it exists in the source list
            return false;
        }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support removeAll(Collection)");
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support retainAll(Collection)");
    }

    @Override
    public T set(int location, T object) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support set(int, T)");
    }

    @Override
    public int size() {
        return filteredList.size();
    }

    @NonNull
    @Override
    public List<T> subList(int start, int end) {
        return filteredList.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return filteredList.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(T1[] array) {
        return filteredList.toArray(array);
    }
    //endregion

    /**
     * Called to update the contents based on the filter and the source list.
     */
    protected final void update() {
        synchronized (filteredList) {
            filteredList.clear();
            // Copy the list to avoid potential concurrency issues
            // If the list is modified during this time, we've locked this
            // method, therefore the update will come through later and
            // fix this again
            Collection<T> items = new ArrayList<T>(sourceList);
            for (T item : items) {
                if (filters.evaluate(item)) {
                    filteredList.add(item);
                }
            }
            doUpdates(filteredList);
            listObserver.notifyGenericChange();
        }
    }

    protected void doUpdates(List<T> itemList) {

    }

    /**
     * Class which watches a {@link FilteredList}'s source list for updates and
     * updates the {@link FilteredList}.
     *
     * @param <U>
     */
    private static class SourceListListener<U> extends SimpleListObserverListener<U> {

        // Keep a weak reference so we don't hang on to the FilteredList.
        private WeakReference<FilteredList<U>> listRef;
        private ObservableList<U> sourceList;

        public SourceListListener(FilteredList<U> list) {
            listRef = new WeakReference<>(list);
            sourceList = list.sourceList;
            sourceList.getListObserver().addListener(this);
        }

        @Override
        public void onGenericChange(ListObserver observer) {
            FilteredList<?> list = listRef.get();
            if (list != null) {
                list.update();
            } else {
                // We don't have a reference to the filtered list anymore
                // so we don't do anything. Unsubscribe.
                sourceList.getListObserver().removeListener(this);
            }
        }
    }
}
