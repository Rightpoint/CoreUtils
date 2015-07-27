package com.raizlabs.coreutils.collections;

import com.raizlabs.coreutils.functions.Predicate;
import com.raizlabs.coreutils.threading.ThreadingUtils;
import com.raizlabs.coreutils.util.observable.lists.ListObserver;
import com.raizlabs.coreutils.util.observable.lists.ObservableList;
import com.raizlabs.coreutils.util.observable.lists.ObservableListWrapper;
import com.raizlabs.coreutils.util.observable.lists.SimpleListObserverListener;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Container that places a list of items into different categories
 * @param <Data> The type of item contained in the list
 */
public class CategorizedList<Data> {

    /**
     * Categorize data by having the data item specify what specific field or fields to use
     *
     * @param <Data> type of data item from which to retrieve the categories
     */
    public interface Categorizer<Data> {

        /**
         *
         * @param data The item to retrieve a category from.
         * @return The array of categories for this item
         */
        public String[] getCategories(Data data);
    }

    private ProxyObservableList<Data> proxyDataList;
    private Categorizer<Data> categorizer;

    private final HashMap<String, FilteredList<Data>> filteredLists = new HashMap<>();
    private ObservableList<String> categories = new ObservableListWrapper<>();

    public ObservableList<String> getAllCategories() {
        return categories;
    }

    public ObservableList<Data> getAllData() {
        return proxyDataList;
    }

    /**
     * Populates this {@link CategorizedList} with the provided source list and a {@link Categorizer}
     * that dictates how the items are to be categorized
     * @param sourceList list containing the original data
     * @param categorizer {@link Categorizer} that dicates how the items are to be categorized
     */
    public CategorizedList(ObservableList<Data> sourceList, Categorizer<Data> categorizer) {
        this.proxyDataList = new ProxyObservableList<>(sourceList);
        this.categorizer = categorizer;

        this.proxyDataList.getListObserver().addListener(new SimpleListObserverListener<Data>() {
            @Override
            public void onGenericChange(ListObserver<Data> observer) {
                updateCategories();
            }
        });
        updateCategories();
    }

    /**
     * Loads items from the provided {@link ObservableList} into this container
     * @param sourceList list containing the data items to insert
     */
    public void loadData(ObservableList<Data> sourceList) {
        if (sourceList == null) {
            sourceList = new ObservableListWrapper<>();
        }

        final ObservableList<Data> finalSource = sourceList;

        ThreadingUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                proxyDataList.setSourceList(finalSource);
            }
        });
    }

    /**
     * Gets a {@link ObservableList} that contains the data associated with the given category
     * @param category Name of the category
     * @return {@link ObservableList} of items that are associated with the given category
     */
    public ObservableList<Data> getDataForCategory(final String category) {
        synchronized (filteredLists) {
            FilteredList<Data> categoryList = filteredLists.get(category);

            if (categoryList == null) {
                categoryList = new FilteredList<>(proxyDataList, new Predicate<Data>() {
                    @Override
                    public boolean evaluate(Data item) {
                        String[] categoriesArray = categorizer.getCategories(item);

                        if (categoriesArray == null) {
                            return true;
                        } else {
                            for (String categoryItem : categoriesArray) {
                                if (category.equals(categoryItem)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                });

                filteredLists.put(category, categoryList);
            }
            return categoryList;
        }
    }

    private void updateCategories() {

        HashSet addedTypes = new HashSet<>();
        categories.beginTransaction();
        categories.clear();
        for(Data data : proxyDataList) {
            String[] categoriesArray = categorizer.getCategories(data);
            for(String category : categoriesArray) {
                addedTypes.add(category);
            }
        }
        categories.addAll(addedTypes);
        categories.endTransaction();
    }
}
