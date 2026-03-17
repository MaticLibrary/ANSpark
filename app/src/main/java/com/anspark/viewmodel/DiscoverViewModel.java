package com.anspark.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anspark.models.Match;
import com.anspark.models.Profile;
import com.anspark.repository.DiscoverRepository;
import com.anspark.repository.MatchRepository;
import com.anspark.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class DiscoverViewModel extends AndroidViewModel {
    private final DiscoverRepository discoverRepository;
    private final MatchRepository matchRepository;

    private final MutableLiveData<List<Profile>> profiles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Profile> currentProfile = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private int index = 0;

    public DiscoverViewModel(@NonNull Application application) {
        super(application);
        this.discoverRepository = new DiscoverRepository(application);
        this.matchRepository = new MatchRepository(application);
    }

    public LiveData<List<Profile>> getProfiles() {
        return profiles;
    }

    public LiveData<Profile> getCurrentProfile() {
        return currentProfile;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void load() {
        discoverRepository.getDiscoverProfiles(1, new RepositoryCallback<List<Profile>>() {
            @Override
            public void onSuccess(List<Profile> data) {
                profiles.postValue(data);
                index = 0;
                if (data != null && !data.isEmpty()) {
                    currentProfile.postValue(data.get(0));
                }
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void nextProfile() {
        List<Profile> list = profiles.getValue();
        if (list == null || list.isEmpty()) {
            return;
        }
        index = (index + 1) % list.size();
        currentProfile.setValue(list.get(index));
    }

    public void sendDecision(boolean liked) {
        Profile profile = currentProfile.getValue();
        if (profile == null) {
            return;
        }

        matchRepository.sendDecision(profile, liked, new RepositoryCallback<Match>() {
            @Override
            public void onSuccess(Match data) {
                nextProfile();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                nextProfile();
            }
        });
    }
}
