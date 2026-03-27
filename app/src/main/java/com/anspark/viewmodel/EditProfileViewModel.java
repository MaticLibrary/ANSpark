package com.anspark.viewmodel;

import android.app.Application;
import android.util.Log;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileViewModel extends AndroidViewModel {
    private static final String TAG = "EDIT_PROFILE_VM";
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
        Log.d(TAG, "loadProfile called");
        loading.setValue(true);
        repository.getMyProfile(new RepositoryCallback<Profile>() {
            @Override
            public void onSuccess(Profile data) {
                Log.d(TAG, "loadProfile success, bio: " + (data.getBio() != null ? data.getBio() : "null"));
                loading.postValue(false);
                profile.postValue(data);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "loadProfile error: " + message);
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void updateProfile(Profile updated) {
        Log.d(TAG, "updateProfile called");
        loading.setValue(true);
        submitProfileUpdate(updated);
    }

    public void completeProfile(Profile updated, List<File> photoFiles, int primaryPhotoIndex) {
        Log.d(TAG, "completeProfile called");
        Log.d(TAG, "Profile bio: " + updated.getBio());
        Log.d(TAG, "Photo files count: " + (photoFiles != null ? photoFiles.size() : 0));
        Log.d(TAG, "Primary photo index: " + primaryPhotoIndex);

        loading.setValue(true);
        if (photoFiles == null || photoFiles.isEmpty()) {
            Log.d(TAG, "No new photos, applying primary and updating");
            applyPrimaryPhoto(updated, primaryPhotoIndex);
            submitProfileUpdate(updated);
            return;
        }

        uploadPhotosSequentially(updated, photoFiles, primaryPhotoIndex, 0, new HashMap<>());
    }

    private void uploadPhotosSequentially(
            Profile updated,
            List<File> photoFiles,
            int primaryPhotoIndex,
            int slotIndex,
            Map<Integer, Photo> uploadedBySlot
    ) {
        if (slotIndex >= photoFiles.size()) {
            Log.d(TAG, "All photos uploaded, merging and submitting");
            mergeUploadedPhotos(updated, uploadedBySlot);
            applyPrimaryPhoto(updated, primaryPhotoIndex);
            submitProfileUpdate(updated);
            return;
        }

        File photoFile = photoFiles.get(slotIndex);
        if (photoFile == null) {
            uploadPhotosSequentially(updated, photoFiles, primaryPhotoIndex, slotIndex + 1, uploadedBySlot);
            return;
        }

        Log.d(TAG, "Uploading photo " + slotIndex + ", file: " + photoFile.getName());
        repository.uploadPhoto(photoFile, new RepositoryCallback<Photo>() {
            @Override
            public void onSuccess(Photo photo) {
                Log.d(TAG, "Photo " + slotIndex + " uploaded successfully, url: " + photo.getUrl());
                if (photo != null && photo.getUrl() != null && !photo.getUrl().trim().isEmpty()) {
                    uploadedBySlot.put(slotIndex, photo);
                }
                uploadPhotosSequentially(updated, photoFiles, primaryPhotoIndex, slotIndex + 1, uploadedBySlot);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Photo upload failed for slot " + slotIndex + ": " + message);
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    private void mergeUploadedPhotos(Profile updated, Map<Integer, Photo> uploadedBySlot) {
        List<Photo> mergedPhotos = updated.getPhotos() != null ? new ArrayList<>(updated.getPhotos()) : new ArrayList<>();
        List<Integer> slotIndexes = new ArrayList<>(uploadedBySlot.keySet());
        Collections.sort(slotIndexes);
        for (Integer slotIndex : slotIndexes) {
            Photo photo = uploadedBySlot.get(slotIndex);
            if (slotIndex < mergedPhotos.size()) {
                mergedPhotos.set(slotIndex, photo);
            } else {
                mergedPhotos.add(photo);
            }
        }
        updated.setPhotos(mergedPhotos);
        Log.d(TAG, "Merged photos, total count: " + mergedPhotos.size());
    }

    private void applyPrimaryPhoto(Profile updated, int primaryPhotoIndex) {
        List<Photo> photos = updated.getPhotos() != null ? new ArrayList<>(updated.getPhotos()) : new ArrayList<>();
        if (photos.isEmpty()) {
            Log.d(TAG, "No photos to apply primary");
            return;
        }

        int safePrimaryIndex = primaryPhotoIndex >= 0 && primaryPhotoIndex < photos.size() ? primaryPhotoIndex : 0;
        for (int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);
            if (photo != null) {
                photo.setPrimary(i == safePrimaryIndex);
            }
        }

        Photo primaryPhoto = photos.get(safePrimaryIndex);
        if (primaryPhoto != null && primaryPhoto.getUrl() != null && !primaryPhoto.getUrl().trim().isEmpty()) {
            updated.setAvatarUrl(primaryPhoto.getUrl());
            Log.d(TAG, "Primary photo set to index " + safePrimaryIndex + ", url: " + primaryPhoto.getUrl());
        }
        updated.setPhotos(photos);
    }

    private void submitProfileUpdate(Profile updated) {
        Log.d(TAG, "submitProfileUpdate called, bio: " + updated.getBio());
        repository.updateProfile(updated, new RepositoryCallback<Profile>() {
            @Override
            public void onSuccess(Profile data) {
                Log.d(TAG, "Profile update successful!");
                loading.postValue(false);
                profile.postValue(data);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Profile update failed: " + message);
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }
}