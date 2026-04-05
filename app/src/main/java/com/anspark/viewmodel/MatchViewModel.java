package com.anspark.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anspark.models.MatchResponse;
import com.anspark.repository.MatchRepository;
import com.anspark.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class MatchViewModel extends AndroidViewModel {
    private final MatchRepository repository;
    private final MutableLiveData<List<MatchResponse>> matches = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public MatchViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MatchRepository(application);
    }

    public LiveData<List<MatchResponse>> getMatches() {
        return matches;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadMatches() {
        repository.getMatches(new RepositoryCallback<List<MatchResponse>>() {
            @Override
            public void onSuccess(List<MatchResponse> data) {
                matches.postValue(data);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}