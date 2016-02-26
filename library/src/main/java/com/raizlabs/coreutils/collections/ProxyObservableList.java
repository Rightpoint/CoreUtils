package com.raizlabs.coreutils.collections;

import android.support.annotation.NonNull;

import com.raizlabs.coreutils.events.WeakDelegateListObserverListener;
import com.raizlabs.coreutils.util.observable.lists.ListObserver;
import com.raizlabs.coreutils.util.observable.lists.ObservableList;
import com.raizlabs.coreutils.util.observable.lists.ObservableListWrapper;
import com.raizlabs.coreutils.util.observable.lists.SimpleListObserver;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ProxyObservableList<T> implements ObservableList<T> {

    //region Members
    private ObservableList<T> sourceList;
    private SimpleListObserver<T> listObserver;
    private SourceListListener<T> sourceListListener;

    /**
     * @return The underlying source list.
     */
    protected ObservableList<T> getSourceList() { return sourceList; }

    /**
     * Sets the underlying source list to be the given list and binds to it.
     * @param sourceList The list to use as a source.
     */
    public void setSourceList(ObservableList<T> sourceList) {
        synchronized (this) {
            // Don't bother if we're already bound to the given list
            // This is a != and not !list.equals because we care about particular
            // instances and events over "equality". We need to make sure we are
            // bound to the specific instance that was passed to us and not just
            // one which "looks" the same.
            if (this.sourceList != sourceList) {
                if (this.sourceList != null) {
                    this.sourceList.getListObserver().removeListener(sourceListListener);
                }
                sourceList.getListObserver().addListener(sourceListListener);
                this.sourceList = sourceList;
                this.listObserver.notifyGenericChange();
            }
        }
    }

    /**
     * Constructs a {@link ProxyObservableList} which is bound to the given
     * list.
     */
    public ProxyObservableList(ObservableList<T> sourceList) {
        // We can't have no list to proxy, so if we weren't given one, make an
        // empty one.
        if (sourceList == null) sourceList = new ObservableListWrapper<>();

        this.sourceListListener = new SourceListListener<>(this);
        this.listObserver = new SimpleListObserver<>();
        setSourceList(sourceList);
    }

    //region Inherited Methods
    @Override
    public ListObserver<T> getListObserver() { return listObserver; }

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
        sourceList.add(location, object);
    }

    @Override
    public boolean add(T object) {
        return sourceList.add(object);
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        return sourceList.addAll(location, collection);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return sourceList.addAll(collection);
    }

    @Override
    public void clear() {
        sourceList.clear();
    }

    @Override
    public boolean contains(Object object) {
        return sourceList.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return sourceList.containsAll(collection);
    }

    @Override
    public T get(int location) {
        return sourceList.get(location);
    }

    @Override
    public int indexOf(Object object) {
        return sourceList.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return sourceList.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return sourceList.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return sourceList.lastIndexOf(object);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        return sourceList.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int location) {
        return sourceList.listIterator(location);
    }

    @Override
    public T remove(int location) {
        return sourceList.remove(location);
    }

    @Override
    public boolean remove(Object object) {
        return sourceList.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return sourceList.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return sourceList.retainAll(collection);
    }

    @Override
    public T set(int location, T object) {
        return sourceList.set(location, object);
    }

    @Override
    public int size() {
        return sourceList.size();
    }

    @NonNull
    @Override
    public List<T> subList(int start, int end) {
        return sourceList.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return sourceList.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(T1[] array) {
        return sourceList.toArray(array);
    }
    //endregion

    /**
     * Listener class that we send to the source list which prevents leaks of
     * the outer list by using a {@link WeakReference}.
     * @param <U> The type of data being listened to.
     */
    private static class SourceListListener<U> extends WeakDelegateListObserverListener<U, ProxyObservableList<U>> {

        /**
         * Creates a new listener which proxies back to the given list.
         * @param list The list to delegate calls back to.
         */
        public SourceListListener(ProxyObservableList<U> list) {
            super(list);
        }

        @Override
        public void onItemRangeChanged(ListObserver<U> observer, int startPosition, int itemCount) {
            ProxyObservableList<U> list = getDelegate(observer);
            if (list != null) {
                list.listObserver.notifyItemRangeChanged(startPosition, itemCount);
            }
        }

        @Override
        public void onItemRangeInserted(ListObserver<U> observer, int startPosition, int itemCount) {
            ProxyObservableList<U> list = getDelegate(observer);
            if (list != null) {
                list.listObserver.notifyItemRangeInserted(startPosition, itemCount);
            }
        }

        @Override
        public void onItemRangeRemoved(ListObserver<U> observer, int startPosition, int itemCount) {
            ProxyObservableList<U> list = getDelegate(observer);
            if (list != null) {
                list.listObserver.notifyItemRangeRemoved(startPosition, itemCount);
            }
        }

        @Override
        public void onGenericChange(ListObserver<U> observer) {
            ProxyObservableList<U> list = getDelegate(observer);
            if (list != null) {
                list.listObserver.notifyGenericChange();
            }
        }
    }
}
