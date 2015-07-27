package com.raizlabs.coreutils.collections;

import com.raizlabs.coreutils.events.WeakDelegateListObserverListener;
import com.raizlabs.coreutils.util.observable.lists.ListObserver;
import com.raizlabs.coreutils.util.observable.lists.ObservableListWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Class which takes a {@link CategorizedList} and "flattens" it into one list of categories and
 * items such that it can be inserted into an adapter
 *
 * @param <Data> The type of data in the categorized list.
 */
public class CategorizedListFlattener<Data> extends ObservableListWrapper<CategorizedListFlattener.FlattenedItem<Data>> {

    public interface FlattenedItem<Data> {
        /**
         * @return True if this item represents a category.
         */
        public boolean isCategory();

        /**
         * @return The name of this category if it is one, or null.
         */
        public String getCategoryName();

        /**
         * @return The data of this item if it is a data item, or null.
         */
        public Data getData();
    }

    private CategorizedList<Data> categorizedList;
    private Comparator<String> categoryComparator;
    private Comparator<? super Data> dataComparator;

    /**
     * Constructs a new flattener which will reflect the contents of the given
     * categorized list, updating itself accordingly.
     * @param categorizedList The categorized list to flatten the elements of.
     */
    public CategorizedListFlattener(CategorizedList<Data> categorizedList) {
        this(categorizedList, null, null);
    }

    /**
     * Constructs a new flattener which will reflect the contents of the given
     * categorized list, updating itself accordingly.
     * @param categorizedList The categorized list to flatten the elements of.
     * @param categoryComparator A comparator to use to sort the categories.
     * @param dataComparator A comparator to use to sort the data elements.
     */
    public CategorizedListFlattener(CategorizedList<Data> categorizedList,
                                    Comparator<String> categoryComparator,
                                    Comparator<? super Data> dataComparator) {
        super();
        this.categorizedList = categorizedList;
        this.categoryComparator = categoryComparator;
        this.dataComparator = dataComparator;

        // Listen for any updates from the categories or the items themselves as
        // either will invalidate our data.
        // Use weak listeners to ourself so we don't get "leaked" / retained by
        // the other list
        categorizedList.getAllCategories().getListObserver().addListener(new ChangeListener<String, Data>(this));
        categorizedList.getAllData().getListObserver().addListener(new ChangeListener<Data, Data>(this));

        // Do an initial update to populate our contents
        update();
    }

    /**
     * Rebuilds our list so that we contain all of the flattened items.
     */
    private void update() {
        synchronized (this) {
            beginTransaction();
            clear();
            List<String> categories = new ArrayList<>(categorizedList.getAllCategories());
            if (categoryComparator != null) {
                Collections.sort(categories, categoryComparator);
            }

            for (String category : categories) {
                add(new CategoryItem<Data>(category));

                List<Data> items = new ArrayList<>(categorizedList.getDataForCategory(category));
                if (dataComparator != null) {
                    Collections.sort(items, dataComparator);
                }

                for (Data data : categorizedList.getDataForCategory(category)) {
                    add(new DataItem<>(data));
                }
            }
            endTransaction();
        }
    }

    /**
     * General listener class which causes updates when the data changes. Uses
     * weak references to avoid leaks of the flattener.
     * @param <Observed> The type of data being observed for changes.
     * @param <Data> The data type of the flattener.
     */
    private static class ChangeListener<Observed, Data> extends WeakDelegateListObserverListener<Observed, CategorizedListFlattener<Data>> {

        /**
         * Constructs a listener which will cause updates on the given flattener
         * when it is notified of changes.
         */
        public ChangeListener(CategorizedListFlattener<Data> flattener) {
            super(flattener);
        }

        private void onChange(ListObserver<Observed> observer) {
            CategorizedListFlattener<Data> flattener = getDelegate(observer);
            if (flattener != null) {
                flattener.update();
            }
        }

        @Override
        public void onItemRangeChanged(ListObserver<Observed> observer, int startPosition, int itemCount) {
            onChange(observer);
        }

        @Override
        public void onItemRangeInserted(ListObserver<Observed> observer, int startPosition, int itemCount) {
            onChange(observer);
        }

        @Override
        public void onItemRangeRemoved(ListObserver<Observed> observer, int startPosition, int itemCount) {
            onChange(observer);
        }

        @Override
        public void onGenericChange(ListObserver<Observed> observer) {
            onChange(observer);
        }
    }

    private static class CategoryItem<Data> implements FlattenedItem<Data> {

        private String name;

        public CategoryItem(String categoryName) {
            name = categoryName;
        }

        @Override
        public boolean isCategory() {
            return true;
        }

        @Override
        public String getCategoryName() {
            return name;
        }

        @Override
        public Data getData() {
            return null;
        }
    }

    private static class DataItem<Data> implements FlattenedItem<Data> {

        private Data item;

        public DataItem(Data item) {
            this.item = item;
        }

        @Override
        public boolean isCategory() {
            return false;
        }

        @Override
        public String getCategoryName() {
            return null;
        }

        @Override
        public Data getData() {
            return item;
        }
    }
}
