package com.anspark.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anspark.models.Profile;
import com.anspark.repository.DiscoverRepository;
import com.anspark.repository.MatchRepository;
import com.anspark.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiscoverViewModel extends AndroidViewModel {
    private static final String TAG = "DISCOVER_VM";
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
        Log.d(TAG, "load() called");
        discoverRepository.getDiscoverProfiles(0, new RepositoryCallback<List<Profile>>() {   // ← зміна тут
            @Override
            public void onSuccess(List<Profile> data) {
                Log.d(TAG, "onSuccess, profiles count: " + (data != null ? data.size() : 0));
                profiles.postValue(data);
                index = 0;
                if (data != null && !data.isEmpty()) {
                    currentProfile.postValue(data.get(0));
                } else {
                    Log.d(TAG, "No profiles found!");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: " + message);
                error.postValue(message);
            }
        });
    }

    public void nextProfile() {
        List<Profile> list = profiles.getValue();
        if (list == null || list.isEmpty()) {
            Log.d(TAG, "nextProfile: list is empty");
            return;
        }
        index = (index + 1) % list.size();
        currentProfile.setValue(list.get(index));
    }

    public void sendDecision(boolean liked) {
        Profile profile = currentProfile.getValue();
        if (profile == null) {
            Log.d(TAG, "sendDecision: profile is null");
            return;
        }

        if (!liked) {
            Log.d(TAG, "sendDecision: dislike - next profile");
            nextProfile();
            return;
        }

        Log.d(TAG, "sendDecision: like for profile " + profile.getId());
        matchRepository.sendLike(profile, new RepositoryCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                Boolean isMatch = (Boolean) data.get("isMatch");
                if (isMatch != null && isMatch) {
                    Log.d(TAG, "It's a match!");
                    error.postValue("To jest match!");
                }
                nextProfile();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "sendLike error: " + message);
                error.postValue(message);
                nextProfile();
            }
        });
    }
}