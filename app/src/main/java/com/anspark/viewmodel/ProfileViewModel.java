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

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {
    private final ProfileRepository repository;
    private final MutableLiveData<Profile> profile = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
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

    public void setPrimaryPhoto(Profile currentProfile, Photo selectedPhoto) {
        if (currentProfile == null || selectedPhoto == null) {
            error.setValue("Wybierz zdjecie, ktore ma zostac glowne");
            return;
        }

        List<Photo> currentPhotos = currentProfile.getPhotos();
        if (currentPhotos == null || currentPhotos.isEmpty()) {
            error.setValue("Najpierw dodaj zdjecia do profilu");
            return;
        }

        Profile updatedProfile = new Profile(currentProfile);
        List<Photo> normalizedPhotos = new ArrayList<>();
        String selectedUrl = selectedPhoto.getUrl();

        for (Photo photo : currentPhotos) {
            if (photo == null) {
                continue;
            }
            Photo copy = new Photo(photo);
            boolean isPrimary = selectedUrl != null && selectedUrl.equals(photo.getUrl());
            copy.setPrimary(isPrimary);
            normalizedPhotos.add(copy);
            if (isPrimary) {
                updatedProfile.setAvatarUrl(copy.getUrl());
            }
        }

        updatedProfile.setPhotos(normalizedPhotos);
        loading.setValue(true);
        repository.updateProfile(updatedProfile, new RepositoryCallback<Profile>() {
            @Override
            public void onSuccess(Profile data) {
                loading.postValue(false);
                profile.postValue(data != null ? data : updatedProfile);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void verifyProfile(Profile currentProfile) {
        if (currentProfile == null) {
            error.setValue("Profil nie jest jeszcze gotowy do weryfikacji");
            return;
        }
        if (!currentProfile.hasMinimumPhotosForVerification()) {
            error.setValue("Dodaj minimum 2 zdjecia profilowe, aby przejsc weryfikacje");
            return;
        }

        Profile verifiedProfile = new Profile(currentProfile);
        verifiedProfile.setVerified(true);
        repository.saveLocalProfile(verifiedProfile);
        profile.setValue(verifiedProfile);
    }
}
