package com.raizlabs.android.coreutils.collections;

import com.raizlabs.android.coreutils.functions.Predicate;
import com.raizlabs.android.coreutils.threading.ThreadingUtils;
import com.raizlabs.android.coreutils.util.observable.lists.ListObserver;
import com.raizlabs.android.coreutils.util.observable.lists.ObservableList;
import com.raizlabs.android.coreutils.util.observable.lists.ObservableListWrapper;
import com.raizlabs.android.coreutils.util.observable.lists.SimpleListObserverListener;

import java.util.HashMap;
import java.util.HashSet;

public class CategorizedList<Data> {

    /**
     * Categorized data by having them specify what specific field or fields to use
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

    public ObservableList<Data> getDataForCategory(final String category) {
        synchronized (filteredLists) {
            FilteredList<Data> categoryList = filteredLists.get(category);

            if (categoryList == null) {
                categoryList = new FilteredList<>(proxyDataList, new Predicate<Data>() {
                    @Override
                    public boolean evaluate(Data item) {
                        String[] categoriesArray = categorizer.getCategories(item);

                        if (categoriesArray == null) {
                            return (categoriesArray == null || categoriesArray.length == 0);
                        } else {
                            for (String categoryItem : categoriesArray) {
                                if (category.equals(categoryItem)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                });

                filteredLists.put(category, categoryList);
            }
            return categoryList;
        }
    }

    private void updateCategories() {

        HashSet<String> addedTypes = new HashSet<>();
        categories.beginTransaction();
        categories.clear();
        for (Data data : proxyDataList) {
            String[] categories = categorizer.getCategories(data);
            for (String category : categories) {
                if (!addedTypes.contains(category)) {
                    addedTypes.add(category);
                    this.categories.add(category);

                }
            }
        }
        categories.endTransaction();
    }
}
