package com.example.zadanie_20481024;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MainActivityViewModel extends ViewModel {
    private final Repository repository = Repository.getInstance();

    public MainActivityViewModel(){}

    /**
     * Warning: this is a blocking method.
     * @param forceReload true: then this method will not used cached data; false: this method will use cached data when it's available;
     * @return List of post summaries or null if method failed due to network or endpoint error.
     */
    @Nullable
    List<PostSummary> getPostSummaries(boolean forceReload){
        return repository.getPostSummaries(forceReload);
    }
}

