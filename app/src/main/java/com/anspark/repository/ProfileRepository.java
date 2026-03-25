package com.anspark.repository;

import android.content.Context;

import com.anspark.api.ApiService;
import com.anspark.api.ProfileApi;
import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {
    private static Profile cachedProfile;
    private final ProfileApi api;

    public ProfileRepository(Context context) {
        this.api = ApiService.profile(context);
    }

    public void getMyProfile(RepositoryCallback<Profile> callback) {
        if (Constants.USE_MOCK_DATA) {
            Profile profile = MockData.sampleProfile();
            cacheProfile(profile);
            callback.onSuccess(new Profile(profile));
            return;
        }

        api.getMyProfile().enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Profile merged = mergeLocalState(response.body());
                    cacheProfile(merged);
                    callback.onSuccess(new Profile(merged));
                } else {
                    callback.onError(extractErrorMessage(response, "Nie udalo sie pobrac profilu"));
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                if (cachedProfile != null) {
                    callback.onSuccess(new Profile(cachedProfile));
                    return;
                }
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void updateProfile(Profile profile, RepositoryCallback<Profile> callback) {
        if (Constants.USE_MOCK_DATA) {
            saveLocalProfile(profile);
            callback.onSuccess(new Profile(profile));
            return;
        }

        api.updateProfile(profile).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful()) {
                    Profile result = response.body() != null ? response.body() : profile;
                    result.setVerified(profile.isVerified());
                    cacheProfile(result);
                    callback.onSuccess(new Profile(result));
                } else {
                    callback.onError(extractErrorMessage(response, "Nie udalo sie zapisac profilu"));
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void uploadPhoto(File photoFile, RepositoryCallback<Photo> callback) {
        if (Constants.USE_MOCK_DATA) {
            Photo photo = new Photo();
            photo.setId("mock_photo_" + System.currentTimeMillis());
            photo.setUrl(photoFile.toURI().toString());
            callback.onSuccess(photo);
            return;
        }

        RequestBody requestBody = RequestBody.create(photoFile, MediaType.parse("image/*"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", photoFile.getName(), requestBody);

        api.uploadPhoto(filePart).enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response, "Nie udalo sie dodac zdjecia"));
                }
            }

            @Override
            public void onFailure(Call<Photo> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void saveLocalProfile(Profile profile) {
        cacheProfile(profile);
        if (Constants.USE_MOCK_DATA) {
            MockData.updateSampleProfile(profile);
        }
    }

    private void cacheProfile(Profile profile) {
        cachedProfile = profile != null ? new Profile(profile) : null;
    }

    private Profile mergeLocalState(Profile remoteProfile) {
        Profile merged = new Profile(remoteProfile);
        if (cachedProfile != null && cachedProfile.isVerified()) {
            merged.setVerified(true);
        }
        return merged;
    }

    private String extractErrorMessage(Response<?> response, String fallback) {
        if (response == null || response.errorBody() == null) {
            return fallback;
        }

        try {
            String body = response.errorBody().string();
            if (body != null && !body.trim().isEmpty()) {
                return body;
            }
        } catch (IOException ignored) {
        }

        return fallback;
    }
}
