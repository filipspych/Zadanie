package com.example.zadanie_20481024;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

public class DetailActivityViewModel extends ViewModel {
    private final Repository repository = Repository.getInstance();

    public DetailActivityViewModel() {}

    /**
     * Warning: this is a blocking method. It might give you cached result.
     * @return  full post with proper id or null if method failed due to network or endpoint error.
     */
    @Nullable
    FullPost getFullPost(int postId){
        return repository.getFullPost(postId);
    }
}
