package com.anspark.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anspark.models.Profile;
import com.anspark.repository.ProfileRepository;
import com.anspark.repository.RepositoryCallback;

public class EditProfileViewModel extends AndroidViewModel {
    private final ProfileRepository repository;
    private final MutableLiveData<Profile> profile = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public EditProfileViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ProfileRepository(application);
    }

    public LiveData<Profile> getProfile() {
        return profile;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void updateProfile(Profile updated) {
        repository.updateProfile(updated, new RepositoryCallback<Profile>() {
            @Override
            public void onSuccess(Profile data) {
                profile.postValue(data);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}
