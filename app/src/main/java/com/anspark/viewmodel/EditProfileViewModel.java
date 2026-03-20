package com.anspark.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.repository.ProfileRepository;
import com.anspark.repository.RepositoryCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditProfileViewModel extends AndroidViewModel {
    private final ProfileRepository repository;
    private final MutableLiveData<Profile> profile = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

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

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadProfile() {
        loading.setValue(true);
        repository.getMyProfile(new RepositoryCallback<Profile>() {
            @Override
            public void onSuccess(Profile data) {
                loading.postValue(false);
                profile.postValue(data);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void updateProfile(Profile updated) {
        loading.setValue(true);
        submitProfileUpdate(updated);
    }

    public void completeProfile(Profile updated, File avatarFile) {
        loading.setValue(true);
        if (avatarFile == null) {
            submitProfileUpdate(updated);
            return;
        }

        repository.uploadPhoto(avatarFile, new RepositoryCallback<Photo>() {
            @Override
            public void onSuccess(Photo photo) {
                if (photo != null && photo.getUrl() != null) {
                    photo.setPrimary(true);
                    updated.setAvatarUrl(photo.getUrl());

                    List<Photo> currentPhotos = updated.getPhotos();
                    List<Photo> mergedPhotos = currentPhotos != null ? new ArrayList<>(currentPhotos) : new ArrayList<>();
                    if (mergedPhotos.isEmpty()) {
                        mergedPhotos.add(photo);
                    } else {
                        mergedPhotos.set(0, photo);
                    }
                    updated.setPhotos(mergedPhotos);
                }
                submitProfileUpdate(updated);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    private void submitProfileUpdate(Profile updated) {
        repository.updateProfile(updated, new RepositoryCallback<Profile>() {
            @Override
            public void onSuccess(Profile data) {
                loading.postValue(false);
                profile.postValue(data);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }
}
